package com.android.server.policy.keyguard;

import android.app.ActivityManager;
import android.app.IColorKeyguardSessionCallback;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.os.IBinder;
import android.os.IBinder.DeathRecipient;
import android.os.RemoteException;
import android.os.UserHandle;
import android.util.ArrayMap;
import android.util.Log;
import android.util.Slog;
import android.view.WindowManagerPolicy.OnKeyguardExitResult;
import com.android.internal.policy.IColorOSKeyguardService.Stub;
import com.android.internal.policy.IKeyguardDismissCallback;
import com.android.internal.policy.IKeyguardDrawnCallback;
import com.android.internal.policy.IKeyguardExitCallback;
import com.android.internal.policy.IKeyguardService;
import com.android.server.UiThread;
import com.android.server.policy.keyguard.KeyguardStateMonitor.StateCallback;
import java.io.PrintWriter;

public class KeyguardServiceDelegate {
    private static final boolean DEBUG = true;
    private static final int INTERACTIVE_STATE_AWAKE = 2;
    private static final int INTERACTIVE_STATE_GOING_TO_SLEEP = 3;
    private static final int INTERACTIVE_STATE_SLEEP = 0;
    private static final int INTERACTIVE_STATE_WAKING = 1;
    private static final String PERMISSION = "android.permission.CONTROL_KEYGUARD";
    private static final String REQUEST_COMMAND_ON_SYSTEM_REBOOTED = "system.rebooted";
    private static final int SCREEN_STATE_OFF = 0;
    private static final int SCREEN_STATE_ON = 2;
    private static final int SCREEN_STATE_TURNING_OFF = 3;
    private static final int SCREEN_STATE_TURNING_ON = 1;
    private static final String TAG = "KeyguardServiceDelegate";
    private final StateCallback mCallback;
    private ArrayMap<IBinder, KeyguardSessionData> mClients;
    private final Context mContext;
    private DrawnListener mDrawnListenerWhenConnect;
    private final Handler mHandler;
    private final ServiceConnection mKeyguardConnection;
    KeyguardDoneDelegate mKeyguardDoneDelegate;
    protected KeyguardServiceWrapper mKeyguardService;
    private final KeyguardState mKeyguardState;
    private boolean mOnSystemRebooted;

    public interface DrawnListener {
        void onDrawn();
    }

    private final class KeyguardDoneDelegate extends Stub {
        KeyguardDoneDelegate() {
        }

        public void onKeyguardDoneForColorOS(boolean keyguardDone) {
        }

        public void setNotificationListener(boolean isChanged) {
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

        /* synthetic */ KeyguardSessionData(KeyguardSessionData -this0) {
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
            }
        }
    }

    private static final class KeyguardState {
        public boolean bootCompleted;
        public int currentUser;
        boolean deviceHasKeyguard;
        boolean dreaming;
        public boolean enabled;
        boolean inputRestricted;
        public int interactiveState;
        boolean occluded;
        public int offReason;
        public int screenState;
        boolean secure;
        boolean showing;
        boolean showingAndNotOccluded;
        boolean systemIsReady;

        KeyguardState() {
            reset();
        }

        private void reset() {
            this.showing = true;
            this.showingAndNotOccluded = true;
            this.secure = true;
            this.deviceHasKeyguard = true;
            this.enabled = true;
            this.currentUser = -10000;
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

    public KeyguardServiceDelegate(Context context, StateCallback callback) {
        this.mKeyguardState = new KeyguardState();
        this.mOnSystemRebooted = false;
        this.mKeyguardDoneDelegate = new KeyguardDoneDelegate();
        this.mKeyguardConnection = new ServiceConnection() {
            public void onServiceConnected(ComponentName name, IBinder service) {
                Log.v(KeyguardServiceDelegate.TAG, "*** Keyguard connected (yay!)");
                KeyguardServiceDelegate.this.mKeyguardService = new KeyguardServiceWrapper(KeyguardServiceDelegate.this.mContext, IKeyguardService.Stub.asInterface(service), KeyguardServiceDelegate.this.mCallback);
                KeyguardServiceDelegate.this.mKeyguardService.setColorOSKeyguardService(KeyguardServiceDelegate.this.mKeyguardDoneDelegate);
                if (KeyguardServiceDelegate.this.mKeyguardState.systemIsReady) {
                    if (KeyguardServiceDelegate.this.mOnSystemRebooted) {
                        KeyguardServiceDelegate.this.mKeyguardService.requestKeyguard(KeyguardServiceDelegate.REQUEST_COMMAND_ON_SYSTEM_REBOOTED);
                        KeyguardServiceDelegate.this.mOnSystemRebooted = false;
                    }
                    KeyguardServiceDelegate.this.mKeyguardService.onSystemReady();
                    if (KeyguardServiceDelegate.this.mKeyguardState.currentUser != -10000) {
                        KeyguardServiceDelegate.this.mKeyguardService.setCurrentUser(KeyguardServiceDelegate.this.mKeyguardState.currentUser);
                    }
                    if (KeyguardServiceDelegate.this.mKeyguardState.interactiveState == 2 || KeyguardServiceDelegate.this.mKeyguardState.interactiveState == 1) {
                        KeyguardServiceDelegate.this.mKeyguardService.onStartedWakingUp();
                    }
                    if (KeyguardServiceDelegate.this.mKeyguardState.interactiveState == 2) {
                        KeyguardServiceDelegate.this.mKeyguardService.onFinishedWakingUp();
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
                    KeyguardServiceDelegate.this.mKeyguardService.onBootCompleted();
                }
                if (KeyguardServiceDelegate.this.mKeyguardState.occluded) {
                    KeyguardServiceDelegate.this.mKeyguardService.setOccluded(KeyguardServiceDelegate.this.mKeyguardState.occluded, false);
                }
                if (!KeyguardServiceDelegate.this.mKeyguardState.enabled) {
                    KeyguardServiceDelegate.this.mKeyguardService.setKeyguardEnabled(KeyguardServiceDelegate.this.mKeyguardState.enabled);
                }
            }

            public void onServiceDisconnected(ComponentName name) {
                Log.v(KeyguardServiceDelegate.TAG, "*** Keyguard disconnected (boo!)");
                KeyguardServiceDelegate.this.mKeyguardService = null;
                KeyguardServiceDelegate.this.mKeyguardState.reset();
                KeyguardServiceDelegate.this.mHandler.post(-$Lambda$Y51_Ove_GXVnR3eKXY3FwHEwaWM.$INST$0);
            }

            /* renamed from: lambda$-com_android_server_policy_keyguard_KeyguardServiceDelegate$1_11498 */
            static /* synthetic */ void m200xc57f546b() {
                try {
                    ActivityManager.getService().setLockScreenShown(true, -1);
                } catch (RemoteException e) {
                }
            }
        };
        this.mClients = new ArrayMap();
        this.mOnSystemRebooted = true;
        this.mContext = context;
        this.mHandler = UiThread.getHandler();
        this.mCallback = callback;
    }

    public void bindService(Context context) {
        Intent intent = new Intent();
        ComponentName keyguardComponent = ComponentName.unflattenFromString(context.getApplicationContext().getResources().getString(17039697));
        intent.addFlags(256);
        intent.setComponent(keyguardComponent);
        if (context.bindServiceAsUser(intent, this.mKeyguardConnection, 1, this.mHandler, UserHandle.SYSTEM)) {
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

    public void setOccluded(boolean isOccluded, boolean animate) {
        if (this.mKeyguardService != null) {
            Log.v(TAG, "setOccluded(" + isOccluded + ") animate=" + animate);
            this.mKeyguardService.setOccluded(isOccluded, animate);
        }
        this.mKeyguardState.occluded = isOccluded;
    }

    public void dismiss(IKeyguardDismissCallback callback) {
        if (this.mKeyguardService != null) {
            Log.v(TAG, "dismiss()");
            this.mKeyguardService.dismiss(callback);
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
        }
        this.mKeyguardState.interactiveState = 1;
    }

    public void onFinishedWakingUp() {
        if (this.mKeyguardService != null) {
            Log.v(TAG, "onFinishedWakingUp()");
            this.mKeyguardService.onFinishedWakingUp();
        }
        this.mKeyguardState.interactiveState = 2;
    }

    public void onScreenTurningOff() {
        if (this.mKeyguardService != null) {
            Log.v(TAG, "onScreenTurningOff()");
            this.mKeyguardService.onScreenTurningOff();
        }
        this.mKeyguardState.screenState = 3;
    }

    public void onScreenTurnedOff() {
        if (this.mKeyguardService != null) {
            Log.v(TAG, "onScreenTurnedOff()");
            this.mKeyguardService.onScreenTurnedOff();
        }
        this.mKeyguardState.screenState = 0;
    }

    public void onScreenTurningOn(DrawnListener drawnListener) {
        if (this.mKeyguardService != null) {
            Log.v(TAG, "onScreenTurnedOn(showListener = " + drawnListener + ")");
            this.mKeyguardService.onScreenTurningOn(new KeyguardShowDelegate(drawnListener));
        } else {
            Slog.w(TAG, "onScreenTurningOn(): no keyguard service!");
            this.mDrawnListenerWhenConnect = drawnListener;
        }
        this.mKeyguardState.screenState = 1;
    }

    public void onScreenTurnedOn() {
        if (this.mKeyguardService != null) {
            Log.v(TAG, "onScreenTurnedOn()");
            this.mKeyguardService.onScreenTurnedOn();
        }
        this.mKeyguardState.screenState = 2;
    }

    public void onStartedGoingToSleep(int why) {
        if (this.mKeyguardService != null) {
            this.mKeyguardService.onStartedGoingToSleep(why);
        }
        this.mKeyguardState.offReason = why;
        this.mKeyguardState.interactiveState = 3;
    }

    public void onFinishedGoingToSleep(int why, boolean cameraGestureTriggered) {
        if (this.mKeyguardService != null) {
            this.mKeyguardService.onFinishedGoingToSleep(why, cameraGestureTriggered);
        }
        this.mKeyguardState.interactiveState = 0;
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
        } else {
            this.mKeyguardState.systemIsReady = true;
        }
    }

    public void doKeyguardTimeout(Bundle options) {
        if (this.mKeyguardService != null) {
            this.mKeyguardService.doKeyguardTimeout(options);
        }
    }

    public void onWakeUp(String wakeUpReason) {
        if (this.mKeyguardService != null) {
            this.mKeyguardService.onWakeUp(wakeUpReason);
        }
    }

    public void setCurrentUser(int newUserId) {
        if (this.mKeyguardService != null) {
            this.mKeyguardService.setCurrentUser(newUserId);
        }
        this.mKeyguardState.currentUser = newUserId;
    }

    public void setSwitchingUser(boolean switching) {
        if (this.mKeyguardService != null) {
            this.mKeyguardService.setSwitchingUser(switching);
        }
    }

    public void startKeyguardExitAnimation(long startTime, long fadeoutDuration) {
        if (this.mKeyguardService != null) {
            this.mKeyguardService.startKeyguardExitAnimation(startTime, fadeoutDuration);
        }
    }

    public void onBootCompleted() {
        if (this.mKeyguardService != null) {
            this.mKeyguardService.onBootCompleted();
        }
        this.mKeyguardState.bootCompleted = true;
    }

    public void onShortPowerPressedGoHome() {
        if (this.mKeyguardService != null) {
            this.mKeyguardService.onShortPowerPressedGoHome();
        }
    }

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
