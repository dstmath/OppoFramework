package com.android.server.policy.keyguard;

import android.annotation.OppoHook;
import android.annotation.OppoHook.OppoHookType;
import android.annotation.OppoHook.OppoRomType;
import android.app.IColorKeyguardSessionCallback;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.os.IBinder;
import android.os.IBinder.DeathRecipient;
import android.os.Message;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.UserHandle;
import android.provider.Settings.Secure;
import android.util.ArrayMap;
import android.util.Log;
import android.util.Slog;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.view.WindowManagerPolicy.OnKeyguardExitResult;
import com.android.internal.policy.IColorOSKeyguardService;
import com.android.internal.policy.IKeyguardDrawnCallback;
import com.android.internal.policy.IKeyguardExitCallback;
import com.android.internal.policy.IKeyguardService.Stub;
import com.android.server.UiThread;
import com.android.server.oppo.IElsaManager;
import com.android.server.policy.keyguard.KeyguardStateMonitor.OnShowingStateChangedCallback;
import java.io.PrintWriter;
import java.util.HashSet;

public class KeyguardServiceDelegate {
    private static final boolean DEBUG = true;
    private static final int INTERACTIVE_STATE_AWAKE = 1;
    private static final int INTERACTIVE_STATE_GOING_TO_SLEEP = 2;
    private static final int INTERACTIVE_STATE_SLEEP = 0;
    private static final String KEYGUARD_LOG_SWITCH_ACTION = "android.intent.action.KEYGUARD_LOG_SWITCH";
    private static final String KEYGUARD_STARTPROC_TIMEOUT_ACTION = "android.intent.action.KEYGUARD_TIMEOUT";
    @OppoHook(level = OppoHookType.NEW_FIELD, note = "zhangkai@Plf.DesktopApp.Keyguard, 2014/12/01, Add for change provider enabled_notification_listeners", property = OppoRomType.OPPO)
    private static final String NOTIFICATION_LISTENER_PACKAGE_NAME = "com.coloros.keyguard.notification";
    @OppoHook(level = OppoHookType.NEW_FIELD, note = "zhangkai@Plf.DesktopApp.Keyguard, 2014/12/01, Add for change provider enabled_notification_listeners", property = OppoRomType.OPPO)
    private static final String NOTIFICATION_LISTENER_SERVICE_NAME = "com.coloros.keyguard.notification.KeyguardNotificationListener";
    private static final String PERMISSION = "android.permission.CONTROL_KEYGUARD";
    private static final String REQUEST_COMMAND_ON_SYSTEM_REBOOTED = "system.rebooted";
    private static final int SCREEN_STATE_OFF = 0;
    private static final int SCREEN_STATE_ON = 2;
    private static final int SCREEN_STATE_TURNING_ON = 1;
    @OppoHook(level = OppoHookType.NEW_FIELD, note = "zhangkai@Plf.DesktopApp.Keyguard, 2014/12/01, Add for change provider enabled_notification_listeners", property = OppoRomType.OPPO)
    private static final int SET_NOTIFICATION_LISTENER = 10;
    private static final String TAG = "KeyguardServiceDelegate";
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(KeyguardServiceDelegate.TAG, "onReceive " + action);
            if (KeyguardServiceDelegate.KEYGUARD_STARTPROC_TIMEOUT_ACTION.equals(action)) {
                if (KeyguardServiceDelegate.this.mKeyguardService == null) {
                    Log.i(KeyguardServiceDelegate.TAG, "Because of restart Keygaurd service timeout, we need bind service by ourself.");
                    KeyguardServiceDelegate.this.bindService(KeyguardServiceDelegate.this.mContext);
                }
            } else if (KeyguardServiceDelegate.KEYGUARD_LOG_SWITCH_ACTION.equals(action)) {
                KeyguardServiceDelegate.this.dump();
            }
        }
    };
    @OppoHook(level = OppoHookType.NEW_METHOD, note = "YuHao@Plf.DesktopApp.Keyguard, 2016/01/30, Add for Color Keyguard bridge with apps", property = OppoRomType.OPPO)
    private ArrayMap<IBinder, KeyguardSessionData> mClients = new ArrayMap();
    private final Context mContext;
    private DrawnListener mDrawnListenerWhenConnect;
    @OppoHook(level = OppoHookType.NEW_FIELD, note = "JiFeng.Tan@EXP.Midware.MidWare 2015/02/11 Added to fix function error about enabled_notification_listeners", property = OppoRomType.OPPO)
    private final HashSet<ComponentName> mEnabledListeners = new HashSet();
    @OppoHook(level = OppoHookType.NEW_FIELD, note = "zhangkai@Plf.DesktopApp.Keyguard, 2014/12/01, Add for change provider enabled_notification_listeners", property = OppoRomType.OPPO)
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            boolean z = false;
            switch (msg.what) {
                case 10:
                    KeyguardServiceDelegate keyguardServiceDelegate = KeyguardServiceDelegate.this;
                    if (msg.arg1 != 0) {
                        z = true;
                    }
                    keyguardServiceDelegate.setNotificationListener(z);
                    return;
                default:
                    return;
            }
        }
    };
    private final ServiceConnection mKeyguardConnection = new ServiceConnection() {
        @OppoHook(level = OppoHookType.CHANGE_CODE, note = "Jingyuan.Chen@Plf.DesktopApp, 2014-09-02 : Add for ColorOS keyguard", property = OppoRomType.OPPO)
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.v(KeyguardServiceDelegate.TAG, "*** Keyguard connected (yay!)");
            KeyguardServiceDelegate.this.mKeyguardService = new KeyguardServiceWrapper(KeyguardServiceDelegate.this.mContext, Stub.asInterface(service), KeyguardServiceDelegate.this.mShowingStateChangedCallback);
            KeyguardServiceDelegate.this.mKeyguardService.setColorOSKeyguardService(KeyguardServiceDelegate.this.mKeyguardDoneDelegate);
            if (KeyguardServiceDelegate.this.mKeyguardState.systemIsReady) {
                if (KeyguardServiceDelegate.this.mOnSystemRebooted) {
                    KeyguardServiceDelegate.this.mKeyguardService.requestKeyguard(KeyguardServiceDelegate.REQUEST_COMMAND_ON_SYSTEM_REBOOTED);
                    KeyguardServiceDelegate.this.mOnSystemRebooted = false;
                }
                KeyguardServiceDelegate.this.sendPendingRecognitionCommandIfNeed();
                if (KeyguardServiceDelegate.this.mKeyguardState.keyguarddone) {
                    KeyguardServiceDelegate.this.mKeyguardState.keyguarddone = false;
                    KeyguardServiceDelegate.this.mKeyguardService.onSystemReadyForColorOS(true);
                } else {
                    KeyguardServiceDelegate.this.mKeyguardService.onSystemReady();
                }
                if (KeyguardServiceDelegate.this.mKeyguardState.currentUser != -10000) {
                    KeyguardServiceDelegate.this.mKeyguardService.setCurrentUser(KeyguardServiceDelegate.this.mKeyguardState.currentUser);
                }
                if (KeyguardServiceDelegate.this.mKeyguardState.interactiveState == 1) {
                    KeyguardServiceDelegate.this.mKeyguardService.onStartedWakingUp();
                }
                if (KeyguardServiceDelegate.this.mKeyguardState.screenState == 2 || KeyguardServiceDelegate.this.mKeyguardState.screenState == 1) {
                    KeyguardServiceDelegate.this.mKeyguardService.onScreenTurningOn(new KeyguardShowDelegate(KeyguardServiceDelegate.this.mDrawnListenerWhenConnect));
                }
                if (KeyguardServiceDelegate.this.mKeyguardState.screenState == 2) {
                    KeyguardServiceDelegate.this.mKeyguardService.onScreenTurnedOn();
                }
                KeyguardServiceDelegate.this.mDrawnListenerWhenConnect = null;
            }
            if (KeyguardServiceDelegate.this.mKeyguardState.bootCompleted) {
                Log.v(KeyguardServiceDelegate.TAG, "*** Keyguard connected, mKeyguardService.onBootCompleted");
                KeyguardServiceDelegate.this.mKeyguardService.onBootCompleted();
            } else {
                Log.v(KeyguardServiceDelegate.TAG, "*** Keyguard connected, mKeyguardState.bootCompleted = " + KeyguardServiceDelegate.this.mKeyguardState.bootCompleted);
            }
            if (KeyguardServiceDelegate.this.mKeyguardState.occluded) {
                KeyguardServiceDelegate.this.mKeyguardService.setOccluded(KeyguardServiceDelegate.this.mKeyguardState.occluded, false);
            }
        }

        @OppoHook(level = OppoHookType.CHANGE_CODE, note = "Jingyuan.Chen@Plf.DesktopApp, 2014-09-02 : Add for ColorOS keyguard", property = OppoRomType.OPPO)
        public void onServiceDisconnected(ComponentName name) {
            Log.v(KeyguardServiceDelegate.TAG, "*** Keyguard disconnected (boo!)");
            KeyguardServiceDelegate.this.mKeyguardService = null;
            KeyguardServiceDelegate.this.mKeyguardState.showing = false;
            Log.v(KeyguardServiceDelegate.TAG, "*** Keyguard disconnected mKeyguardState.showing = " + KeyguardServiceDelegate.this.mKeyguardState.showing);
            Log.v(KeyguardServiceDelegate.TAG, "*** Keyguard disconnected mKeyguardState.occluded = " + KeyguardServiceDelegate.this.mKeyguardState.occluded);
        }
    };
    @OppoHook(level = OppoHookType.NEW_FIELD, note = "Jingyuan.Chen@Plf.DesktopApp, 2014-09-02 : Add for ColorOS keyguard", property = OppoRomType.OPPO)
    KeyguardDoneDelegate mKeyguardDoneDelegate = new KeyguardDoneDelegate();
    protected KeyguardServiceWrapper mKeyguardService;
    private final KeyguardState mKeyguardState = new KeyguardState();
    private boolean mOnSystemRebooted = false;
    private String mPendingRecognitionCommand;
    private PowerManager mPowerManager;
    private final Handler mScrimHandler;
    private final OnShowingStateChangedCallback mShowingStateChangedCallback;

    public interface DrawnListener {
        void onDrawn();
    }

    @OppoHook(level = OppoHookType.NEW_CLASS, note = "Jingyuan.Chen@Plf.DesktopApp, 2014-09-02 : Add for ColorOS keyguard", property = OppoRomType.OPPO)
    private final class KeyguardDoneDelegate extends IColorOSKeyguardService.Stub {
        KeyguardDoneDelegate() {
        }

        public void onKeyguardDoneForColorOS(boolean keyguardDone) {
            int i;
            KeyguardState -get4 = KeyguardServiceDelegate.this.mKeyguardState;
            if (KeyguardServiceDelegate.this.mPowerManager.isScreenOn()) {
                i = 2;
            } else {
                i = 0;
            }
            -get4.screenState = i;
            Log.v(KeyguardServiceDelegate.TAG, "onKeyguardDoneForColorOS keyguardDone = " + keyguardDone);
            Log.v(KeyguardServiceDelegate.TAG, "onKeyguardDoneForColorOS mKeyguardState.screenState = " + KeyguardServiceDelegate.this.mKeyguardState.screenState);
            if (KeyguardServiceDelegate.this.mKeyguardState.screenState == 2) {
                KeyguardServiceDelegate.this.mKeyguardState.keyguarddone = keyguardDone;
                return;
            }
            Log.v(KeyguardServiceDelegate.TAG, "onKeyguardDoneForColorOS screen is off keyguarddone = false");
            KeyguardServiceDelegate.this.mKeyguardState.keyguarddone = false;
        }

        public void setNotificationListener(boolean isChanged) {
            Message msg = new Message();
            msg.what = 10;
            if (isChanged) {
                msg.arg1 = 1;
            } else {
                msg.arg1 = 0;
            }
            KeyguardServiceDelegate.this.mHandler.sendMessage(msg);
        }

        public void sendCommandToApps(String command) {
            Log.d(KeyguardServiceDelegate.TAG, "KSD sendCommandToApps, command = " + command);
            if (command != null) {
                String[] commands = parseCommand(command);
                for (int i = 0; i < KeyguardServiceDelegate.this.mClients.size(); i++) {
                    KeyguardSessionData sessionData = (KeyguardSessionData) KeyguardServiceDelegate.this.mClients.valueAt(i);
                    if (sessionData == null || sessionData.session == null || sessionData.module == null) {
                        Log.v(KeyguardServiceDelegate.TAG, "sessionData at " + i + " is invalid!!");
                    } else if (sessionData.module.equals(commands[0])) {
                        try {
                            sessionData.session.onCommand(commands[1]);
                        } catch (RemoteException e) {
                        }
                    }
                }
                return;
            }
            Log.d(KeyguardServiceDelegate.TAG, "sendCommandToApps, do nothing.");
        }

        private String[] parseCommand(String command) {
            return command.split(":");
        }
    }

    private final class KeyguardExitDelegate extends IKeyguardExitCallback.Stub {
        private OnKeyguardExitResult mOnKeyguardExitResult;

        KeyguardExitDelegate(OnKeyguardExitResult onKeyguardExitResult) {
            this.mOnKeyguardExitResult = onKeyguardExitResult;
        }

        public void onKeyguardExitResult(boolean success) throws RemoteException {
            Log.v(KeyguardServiceDelegate.TAG, "**** onKeyguardExitResult(" + success + ") CALLED ****");
            if (this.mOnKeyguardExitResult != null) {
                this.mOnKeyguardExitResult.onKeyguardExitResult(success);
            }
        }
    }

    private static class KeyguardSessionData {
        public String module;
        public IColorKeyguardSessionCallback session;
        public TokenWatcher tokenWatcher;
        public int userId;

        /* synthetic */ KeyguardSessionData(KeyguardSessionData keyguardSessionData) {
            this();
        }

        private KeyguardSessionData() {
        }

        public IBinder getToken() {
            return this.tokenWatcher.getToken();
        }
    }

    private final class KeyguardShowDelegate extends IKeyguardDrawnCallback.Stub {
        private DrawnListener mDrawnListener;

        KeyguardShowDelegate(DrawnListener drawnListener) {
            this.mDrawnListener = drawnListener;
        }

        public void onDrawn() throws RemoteException {
            Log.v(KeyguardServiceDelegate.TAG, "**** SHOWN CALLED ****");
            if (this.mDrawnListener != null) {
                this.mDrawnListener.onDrawn();
            } else {
                Log.i(KeyguardServiceDelegate.TAG, "onShown, Not invoke onDrawn mDrawnListener = " + this.mDrawnListener);
            }
        }
    }

    private static final class KeyguardState {
        public boolean bootCompleted;
        public int currentUser = -10000;
        boolean deviceHasKeyguard = true;
        public boolean dismissable;
        boolean dreaming;
        public boolean enabled;
        boolean inputRestricted;
        public int interactiveState;
        boolean keyguarddone;
        boolean occluded;
        public int offReason;
        public int screenState;
        boolean secure = true;
        boolean showing = false;
        boolean showingAndNotOccluded = true;
        boolean systemIsReady;

        KeyguardState() {
        }
    }

    private class TokenWatcher implements DeathRecipient {
        IBinder token;

        TokenWatcher(IBinder token) {
            this.token = token;
        }

        IBinder getToken() {
            return this.token;
        }

        public void binderDied() {
            Log.v(KeyguardServiceDelegate.TAG, "binderDied(" + this.token + ")");
            KeyguardSessionData toBeRemovedClient = (KeyguardSessionData) KeyguardServiceDelegate.this.mClients.get(this.token);
            if (toBeRemovedClient != null) {
                KeyguardServiceDelegate.this.requestKeyguard(toBeRemovedClient.module + ":" + "died");
            }
            KeyguardServiceDelegate.this.mClients.remove(this.token);
            this.token = null;
        }
    }

    public void dump() {
        Log.i(TAG, "mKeyguardState.showing = " + this.mKeyguardState.showing);
        Log.i(TAG, "mKeyguardState.inputRestricted = " + this.mKeyguardState.inputRestricted);
        Log.i(TAG, "mKeyguardState.occluded = " + this.mKeyguardState.occluded);
        Log.i(TAG, "mKeyguardState.secure = " + this.mKeyguardState.secure);
        Log.i(TAG, "mKeyguardState.dreaming = " + this.mKeyguardState.dreaming);
        Log.i(TAG, "mKeyguardState.systemIsReady = " + this.mKeyguardState.systemIsReady);
        Log.i(TAG, "mKeyguardState.deviceHasKeyguard = " + this.mKeyguardState.deviceHasKeyguard);
        Log.i(TAG, "mKeyguardState.enabled = " + this.mKeyguardState.enabled);
        Log.i(TAG, "mKeyguardState.dismissable = " + this.mKeyguardState.dismissable);
        Log.i(TAG, "mKeyguardState.offReason = " + this.mKeyguardState.offReason);
        Log.i(TAG, "mKeyguardState.currentUser = " + this.mKeyguardState.currentUser);
        Log.i(TAG, "mKeyguardState.bootCompleted = " + this.mKeyguardState.bootCompleted);
        Log.i(TAG, "mKeyguardState.keyguarddone = " + this.mKeyguardState.keyguarddone);
    }

    @OppoHook(level = OppoHookType.CHANGE_CODE, note = "Jingyuan.Chen@Plf.DesktopApp, 2014-09-02 : Add for ColorOS keyguard", property = OppoRomType.OPPO)
    public KeyguardServiceDelegate(Context context, OnShowingStateChangedCallback showingStateChangedCallback) {
        int i;
        this.mPowerManager = (PowerManager) context.getSystemService("power");
        this.mOnSystemRebooted = true;
        KeyguardState keyguardState = this.mKeyguardState;
        if (this.mPowerManager.isScreenOn()) {
            i = 2;
        } else {
            i = 0;
        }
        keyguardState.screenState = i;
        Log.v(TAG, "KeyguardServiceDelegate phone is on, mKeyguardState.screenState = " + this.mKeyguardState.screenState);
        this.mScrimHandler = UiThread.getHandler();
        bindService(context);
        IntentFilter filter = new IntentFilter();
        filter.addAction(KEYGUARD_STARTPROC_TIMEOUT_ACTION);
        filter.addAction(KEYGUARD_LOG_SWITCH_ACTION);
        context.registerReceiver(this.mBroadcastReceiver, filter);
        this.mContext = context;
        this.mShowingStateChangedCallback = showingStateChangedCallback;
    }

    public void bindService(Context context) {
        Intent intent = new Intent();
        ComponentName keyguardComponent = ComponentName.unflattenFromString(context.getApplicationContext().getResources().getString(17039465));
        intent.addFlags(256);
        intent.setComponent(keyguardComponent);
        if (context.bindServiceAsUser(intent, this.mKeyguardConnection, 1, this.mScrimHandler, UserHandle.SYSTEM)) {
            Log.v(TAG, "*** Keyguard started");
            return;
        }
        Log.v(TAG, "*** Keyguard: can't bind to " + keyguardComponent);
        this.mKeyguardState.showing = false;
        this.mKeyguardState.showingAndNotOccluded = false;
        this.mKeyguardState.secure = false;
        synchronized (this.mKeyguardState) {
            this.mKeyguardState.deviceHasKeyguard = false;
        }
    }

    public boolean isShowing() {
        if (this.mKeyguardService != null) {
            this.mKeyguardState.showing = this.mKeyguardService.isShowing();
        }
        return this.mKeyguardState.showing;
    }

    public boolean isTrusted() {
        if (this.mKeyguardService != null) {
            return this.mKeyguardService.isTrusted();
        }
        return false;
    }

    public boolean hasLockscreenWallpaper() {
        if (this.mKeyguardService != null) {
            return this.mKeyguardService.hasLockscreenWallpaper();
        }
        return false;
    }

    public boolean isInputRestricted() {
        if (this.mKeyguardService != null) {
            this.mKeyguardState.inputRestricted = this.mKeyguardService.isInputRestricted();
        }
        return this.mKeyguardState.inputRestricted;
    }

    public void verifyUnlock(OnKeyguardExitResult onKeyguardExitResult) {
        if (this.mKeyguardService != null) {
            this.mKeyguardService.verifyUnlock(new KeyguardExitDelegate(onKeyguardExitResult));
        }
    }

    public void keyguardDone(boolean authenticated, boolean wakeup) {
        if (this.mKeyguardService != null) {
            this.mKeyguardService.keyguardDone(authenticated, wakeup);
        }
    }

    public void setOccluded(boolean isOccluded, boolean animate) {
        if (this.mKeyguardService != null) {
            Log.v(TAG, "setOccluded(" + isOccluded + ") animate=" + animate);
            this.mKeyguardService.setOccluded(isOccluded, animate);
        }
        this.mKeyguardState.occluded = isOccluded;
    }

    public void dismiss(boolean allowWhileOccluded) {
        if (this.mKeyguardService != null) {
            this.mKeyguardService.dismiss(allowWhileOccluded);
        }
    }

    public boolean isSecure(int userId) {
        if (this.mKeyguardService != null) {
            this.mKeyguardState.secure = this.mKeyguardService.isSecure(userId);
        }
        return this.mKeyguardState.secure;
    }

    public void onDreamingStarted() {
        if (this.mKeyguardService != null) {
            this.mKeyguardService.onDreamingStarted();
        }
        this.mKeyguardState.dreaming = true;
    }

    public void onDreamingStopped() {
        if (this.mKeyguardService != null) {
            this.mKeyguardService.onDreamingStopped();
        }
        this.mKeyguardState.dreaming = false;
    }

    public void onStartedWakingUp() {
        if (this.mKeyguardService != null) {
            Log.v(TAG, "onStartedWakingUp()");
            this.mKeyguardService.onStartedWakingUp();
        } else {
            Log.v(TAG, "onStartedWakingUp() mKeyguardService = null");
        }
        if (this.mKeyguardState.keyguarddone) {
            this.mKeyguardState.keyguarddone = false;
        }
        this.mKeyguardState.interactiveState = 1;
        Log.v(TAG, "onStartedWakingUp mKeyguardState.interactiveState = " + this.mKeyguardState.interactiveState);
    }

    public void onScreenTurnedOff() {
        try {
            if (this.mKeyguardService != null) {
                Log.v(TAG, "onScreenTurnedOff()");
                this.mKeyguardService.onScreenTurnedOff();
            } else {
                Log.v(TAG, "onScreenTurnedOff() mKeyguardService = null");
            }
        } catch (Exception e) {
            Log.v(TAG, "onScreenTurnedOff() Exception e = " + e);
        }
        if (this.mKeyguardState.keyguarddone) {
            this.mKeyguardState.keyguarddone = false;
        }
        this.mKeyguardState.screenState = 0;
        Log.v(TAG, "onScreenTurnedOff mKeyguardState.screenState = " + this.mKeyguardState.screenState);
    }

    public void onScreenTurningOn(DrawnListener drawnListener) {
        try {
            if (this.mKeyguardService != null) {
                Log.v(TAG, "onScreenTurningOn(drawnListener = " + drawnListener + ")");
                this.mKeyguardService.onScreenTurningOn(new KeyguardShowDelegate(drawnListener));
            } else {
                Slog.w(TAG, "onScreenTurningOn(): no keyguard service!");
                this.mDrawnListenerWhenConnect = drawnListener;
            }
        } catch (Exception e) {
            Log.v(TAG, "onScreenTurningOn() Exception e = " + e);
        }
        if (this.mKeyguardState.keyguarddone) {
            this.mKeyguardState.keyguarddone = false;
        }
        this.mKeyguardState.screenState = 1;
        Log.v(TAG, "onScreenTurningOn mKeyguardState.screenState = " + this.mKeyguardState.screenState);
    }

    public void onScreenTurnedOn() {
        if (this.mKeyguardService != null) {
            Log.v(TAG, "onScreenTurnedOn()");
            this.mKeyguardService.onScreenTurnedOn();
        } else {
            Log.v(TAG, "onScreenTurnedOn() mKeyguardService = null");
        }
        if (this.mKeyguardState.keyguarddone) {
            this.mKeyguardState.keyguarddone = false;
        }
        this.mKeyguardState.screenState = 2;
        Log.v(TAG, "onScreenTurnedOn mKeyguardState.screenState = " + this.mKeyguardState.screenState);
    }

    public void onStartedGoingToSleep(int why) {
        if (this.mKeyguardService != null) {
            this.mKeyguardService.onStartedGoingToSleep(why);
        } else {
            Log.v(TAG, "onStartedGoingToSleep() mKeyguardService = null");
        }
        if (this.mKeyguardState.keyguarddone) {
            this.mKeyguardState.keyguarddone = false;
        }
        Log.v(TAG, "onStartedGoingToSleep() clear mKeyguardState.keyguarddone = false");
        this.mKeyguardState.offReason = why;
        this.mKeyguardState.interactiveState = 2;
        Log.v(TAG, "onStartedGoingToSleep mKeyguardState.interactiveState = " + this.mKeyguardState.interactiveState);
    }

    public void onFinishedGoingToSleep(int why, boolean cameraGestureTriggered) {
        if (this.mKeyguardService != null) {
            this.mKeyguardService.onFinishedGoingToSleep(why, cameraGestureTriggered);
        } else {
            Log.v(TAG, "onFinishedGoingToSleep() mKeyguardService = null");
        }
        if (this.mKeyguardState.keyguarddone) {
            this.mKeyguardState.keyguarddone = false;
        }
        this.mKeyguardState.interactiveState = 0;
        Log.v(TAG, "onFinishedGoingToSleep mKeyguardState.interactiveState = " + this.mKeyguardState.interactiveState);
    }

    public void setKeyguardEnabled(boolean enabled) {
        if (this.mKeyguardService != null) {
            this.mKeyguardService.setKeyguardEnabled(enabled);
        }
        this.mKeyguardState.enabled = enabled;
    }

    public void onSystemReady() {
        if (this.mKeyguardService != null) {
            this.mKeyguardService.onSystemReady();
            return;
        }
        Log.v(TAG, "onSystemReady() mKeyguardService = null");
        this.mKeyguardState.systemIsReady = true;
    }

    public void doKeyguardTimeout(Bundle options) {
        if (this.mKeyguardService != null) {
            this.mKeyguardService.doKeyguardTimeout(options);
        }
    }

    public void dispatchWakeUp(boolean isWakeUpByFingerprint) {
        if (this.mKeyguardService != null) {
            this.mKeyguardService.dispatchWakeUp(isWakeUpByFingerprint);
        }
    }

    public void onWakeUp(String wakeUpReason) {
        if (this.mKeyguardService != null) {
            sendPendingRecognitionCommandIfNeed();
            this.mKeyguardService.onWakeUp(wakeUpReason);
            return;
        }
        this.mPendingRecognitionCommand = wakeUpReason;
    }

    public void sendPendingRecognitionCommandIfNeed() {
        if (this.mPendingRecognitionCommand != null) {
            this.mKeyguardService.requestKeyguard(this.mPendingRecognitionCommand);
            Log.v(TAG, "sendPendingRecognitionCommandIfNeed = " + this.mPendingRecognitionCommand);
            this.mPendingRecognitionCommand = null;
        }
    }

    public void setCurrentUser(int newUserId) {
        if (this.mKeyguardService != null) {
            this.mKeyguardService.setCurrentUser(newUserId);
        }
        this.mKeyguardState.currentUser = newUserId;
    }

    public void startKeyguardExitAnimation(long startTime, long fadeoutDuration) {
        if (this.mKeyguardService != null) {
            this.mKeyguardService.startKeyguardExitAnimation(startTime, fadeoutDuration);
        }
    }

    private static View createScrim(Context context, Handler handler) {
        final View view = new View(context);
        final LayoutParams lp = new LayoutParams(-1, -1, 2029, 1116416, -3);
        lp.softInputMode = 16;
        lp.screenOrientation = 5;
        lp.privateFlags |= 1;
        lp.setTitle("KeyguardScrim");
        final WindowManager wm = (WindowManager) context.getSystemService("window");
        view.setSystemUiVisibility(56688640);
        handler.post(new Runnable() {
            public void run() {
                wm.addView(view, lp);
                view.setVisibility(8);
            }
        });
        return view;
    }

    @OppoHook(level = OppoHookType.CHANGE_CODE, note = "Jingyuan.Chen@Plf.DesktopApp, 2014-09-02 : Avoid Keyguard blink", property = OppoRomType.OPPO)
    public void showScrim() {
    }

    @OppoHook(level = OppoHookType.CHANGE_CODE, note = "Jingyuan.Chen@Plf.DesktopApp, 2014-09-02 : Avoid Keyguard blink", property = OppoRomType.OPPO)
    public void hideScrim() {
    }

    public void onBootCompleted() {
        if (this.mKeyguardService != null) {
            this.mKeyguardService.onBootCompleted();
        } else {
            Log.v(TAG, "onBootCompleted() mKeyguardService = null");
        }
        this.mKeyguardState.bootCompleted = true;
    }

    public void onActivityDrawn() {
        if (this.mKeyguardService != null) {
            this.mKeyguardService.onActivityDrawn();
        }
    }

    @OppoHook(level = OppoHookType.NEW_FIELD, note = "zhangkai@Plf.DesktopApp.Keyguard, 2014/12/01, Add for change provider enabled_notification_listeners", property = OppoRomType.OPPO)
    public void setNotificationListener(boolean isChanged) {
        ComponentName cn = new ComponentName(NOTIFICATION_LISTENER_PACKAGE_NAME, NOTIFICATION_LISTENER_SERVICE_NAME);
        loadEnabledListeners();
        if (isChanged) {
            this.mEnabledListeners.add(cn);
        } else {
            this.mEnabledListeners.remove(cn);
        }
        saveEnabledListeners();
    }

    @OppoHook(level = OppoHookType.NEW_FIELD, note = "JiFeng.Tan@EXP.Midware.MidWare 2015/02/11 Added to fix function error about enabled_notification_listeners", property = OppoRomType.OPPO)
    void loadEnabledListeners() {
        this.mEnabledListeners.clear();
        String flat = Secure.getString(this.mContext.getContentResolver(), "enabled_notification_listeners");
        Log.d(TAG, "loadEnabledListeners --> flat = " + flat);
        if (flat != null && !IElsaManager.EMPTY_PACKAGE.equals(flat)) {
            String[] names = flat.split(":");
            for (String unflattenFromString : names) {
                ComponentName cn = ComponentName.unflattenFromString(unflattenFromString);
                if (cn != null) {
                    this.mEnabledListeners.add(cn);
                }
            }
        }
    }

    @OppoHook(level = OppoHookType.NEW_FIELD, note = "JiFeng.Tan@EXP.Midware.MidWare 2015/02/11 Added to fix function error about enabled_notification_listeners", property = OppoRomType.OPPO)
    void saveEnabledListeners() {
        StringBuilder sb = null;
        for (ComponentName cn : this.mEnabledListeners) {
            if (sb == null) {
                sb = new StringBuilder();
            } else {
                sb.append(':');
            }
            sb.append(cn.flattenToString());
        }
        Secure.putString(this.mContext.getContentResolver(), "enabled_notification_listeners", sb != null ? sb.toString() : IElsaManager.EMPTY_PACKAGE);
    }

    public boolean isDismissable(int userId) {
        if (this.mKeyguardService != null) {
            this.mKeyguardState.dismissable = this.mKeyguardService.isDismissable(userId);
        }
        return this.mKeyguardState.dismissable;
    }

    @OppoHook(level = OppoHookType.NEW_METHOD, note = "LiGuan@Plf.DesktopApp.Keyguard, 2015/09/29, Add for request command to keyguard, for user 690904", property = OppoRomType.OPPO)
    public void requestKeyguard(String command) {
        if (this.mKeyguardService != null) {
            this.mKeyguardService.requestKeyguard(command);
        }
    }

    private void checkPermission() {
        if (this.mContext.checkCallingOrSelfPermission(PERMISSION) != 0) {
            Log.w(TAG, "Caller needs permission 'android.permission.CONTROL_KEYGUARD' to call " + Debug.getCaller());
            throw new SecurityException("Access denied to process: " + Binder.getCallingPid() + ", must have permission " + PERMISSION);
        }
    }

    public boolean openKeyguardSession(IColorKeyguardSessionCallback callback, IBinder appToken, String module) {
        checkPermission();
        Log.v(TAG, "openKeyguardSession(" + appToken + ", module(" + module + ")" + ", pid(" + Binder.getCallingPid() + ")");
        if (this.mClients.get(appToken) == null) {
            KeyguardSessionData sessionData = new KeyguardSessionData();
            sessionData.session = callback;
            sessionData.userId = Binder.getCallingUid();
            sessionData.module = module;
            sessionData.tokenWatcher = new TokenWatcher(appToken);
            try {
                appToken.linkToDeath(sessionData.tokenWatcher, 0);
                this.mClients.put(appToken, sessionData);
            } catch (RemoteException e) {
                Log.w(TAG, "caught remote exception in linkToDeath: ", e);
            }
        } else {
            Log.v(TAG, "listener already registered for " + appToken);
        }
        return true;
    }

    public void dump(String prefix, PrintWriter pw) {
        pw.println(prefix + TAG);
        prefix = prefix + "  ";
        pw.println(prefix + "showing=" + this.mKeyguardState.showing);
        pw.println(prefix + "showingAndNotOccluded=" + this.mKeyguardState.showingAndNotOccluded);
        pw.println(prefix + "inputRestricted=" + this.mKeyguardState.inputRestricted);
        pw.println(prefix + "occluded=" + this.mKeyguardState.occluded);
        pw.println(prefix + "secure=" + this.mKeyguardState.secure);
        pw.println(prefix + "dreaming=" + this.mKeyguardState.dreaming);
        pw.println(prefix + "systemIsReady=" + this.mKeyguardState.systemIsReady);
        pw.println(prefix + "deviceHasKeyguard=" + this.mKeyguardState.deviceHasKeyguard);
        pw.println(prefix + "enabled=" + this.mKeyguardState.enabled);
        pw.println(prefix + "offReason=" + this.mKeyguardState.offReason);
        pw.println(prefix + "currentUser=" + this.mKeyguardState.currentUser);
        pw.println(prefix + "bootCompleted=" + this.mKeyguardState.bootCompleted);
        pw.println(prefix + "screenState=" + this.mKeyguardState.screenState);
        pw.println(prefix + "interactiveState=" + this.mKeyguardState.interactiveState);
        if (this.mKeyguardService != null) {
            this.mKeyguardService.dump(prefix, pw);
        }
    }
}
