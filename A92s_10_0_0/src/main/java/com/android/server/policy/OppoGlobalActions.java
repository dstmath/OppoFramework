package com.android.server.policy;

import android.app.ActivityManager;
import android.app.ActivityManagerNative;
import android.app.StatusBarManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.UserInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.server.LocalServices;
import com.android.server.policy.WindowManagerPolicy;
import com.color.view.ColorSurfaceControl;
import com.color.widget.ShutdownView;
import com.color.widget.ShutdownViewControl;
import java.util.ArrayList;
import java.util.List;

class OppoGlobalActions extends GlobalActions {
    private static final int DIALOG_DISMISS_DELAY = 300;
    private static final int MESSAGE_CALL_EMERGENCY = 1;
    private static final int MESSAGE_DISMISS = 0;
    private static final float ROTATION_180 = 180.0f;
    private static final float ROTATION_270 = 90.0f;
    private static final float ROTATION_90 = 270.0f;
    private static final int SCALETIMES = 4;
    private static final int SHOT_DOWN_DELAY = 1500;
    private static final String TAG = "OppoGlobalActions";
    static boolean isPRStatus = false;
    private static final int mDefaultBackground = -872415232;
    private ActivityManager mActivityManager;
    private MyAdapter mAdapter;
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        /* class com.android.server.policy.OppoGlobalActions.AnonymousClass4 */

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("android.intent.action.CLOSE_SYSTEM_DIALOGS".equals(action) || "android.intent.action.SCREEN_OFF".equals(action)) {
                String reason = intent.getStringExtra("reason");
                if (!"globalactions".equals(reason) && !"recentapps".equals(reason)) {
                    OppoGlobalActions.this.mHandler.sendEmptyMessage(0);
                }
            } else if ("android.intent.action.EMERGENCY_CALLBACK_MODE_CHANGED".equals(action) && !intent.getBooleanExtra("PHONE_IN_ECM_STATE", false) && OppoGlobalActions.this.mIsWaitingForEcmExit) {
                boolean unused = OppoGlobalActions.this.mIsWaitingForEcmExit = false;
            }
        }
    };
    /* access modifiers changed from: private */
    public final Context mContext;
    /* access modifiers changed from: private */
    public boolean mDeviceProvisioned = false;
    private Display mDisplay;
    private DisplayMetrics mDisplayMetrics;
    private final GlobalActionsProvider mGlobalActionsProvider;
    /* access modifiers changed from: private */
    public Handler mHandler = new Handler() {
        /* class com.android.server.policy.OppoGlobalActions.AnonymousClass5 */

        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i != 0) {
                if (i == 1) {
                    OppoGlobalActions.this.handleEmergencyCall();
                }
            } else if (OppoGlobalActions.this.mPowerView != null) {
                OppoGlobalActions.this.removePowerView();
            }
        }
    };
    private InnovativeV2HAction mInnovativeV2HAction;
    private boolean mIsReceiverRegisted = false;
    /* access modifiers changed from: private */
    public boolean mIsWaitingForEcmExit = false;
    /* access modifiers changed from: private */
    public ArrayList<Action> mItems;
    /* access modifiers changed from: private */
    public boolean mKeyguardShowing = false;
    /* access modifiers changed from: private */
    public ShutdownViewControl.OnCancelListener mOnCancel = new ShutdownViewControl.OnCancelListener() {
        /* class com.android.server.policy.OppoGlobalActions.AnonymousClass8 */

        public void onCancel() {
            OppoGlobalActions.this.mHandler.sendEmptyMessageDelayed(0, 0);
        }
    };
    /* access modifiers changed from: private */
    public ShutdownViewControl.OnEmergencyListener mOnEmergency = new ShutdownViewControl.OnEmergencyListener() {
        /* class com.android.server.policy.OppoGlobalActions.AnonymousClass9 */

        public void onEmergency() {
            OppoGlobalActions.this.mHandler.sendEmptyMessageDelayed(1, 0);
            OppoGlobalActions.this.mHandler.sendEmptyMessageDelayed(0, 300);
        }
    };
    /* access modifiers changed from: private */
    public ShutdownViewControl.OnRebootListener mOnReboot = new ShutdownViewControl.OnRebootListener() {
        /* class com.android.server.policy.OppoGlobalActions.AnonymousClass7 */

        public void onReboot() {
            Log.d(OppoGlobalActions.TAG, "PRESS REBOOT OPTION");
            if (OppoGlobalActions.this.mPowerView != null) {
                OppoGlobalActions.this.mWindowManagerFuncs.reboot(false);
            }
            OppoGlobalActions.this.mHandler.sendEmptyMessageDelayed(0, 1500);
        }
    };
    /* access modifiers changed from: private */
    public ShutdownViewControl.OnShutdownListener mOnShutDown = new ShutdownViewControl.OnShutdownListener() {
        /* class com.android.server.policy.OppoGlobalActions.AnonymousClass6 */

        public void onShutdown() {
            Log.d(OppoGlobalActions.TAG, "PRESS SHUTDOW OPTION");
            if (OppoGlobalActions.this.mPowerView != null) {
                boolean quickbootEnabled = true;
                OppoGlobalActions.isPRStatus = true;
                if (Settings.System.getInt(OppoGlobalActions.this.mContext.getContentResolver(), "enable_quickboot", 0) != 1) {
                    quickbootEnabled = false;
                }
                if (quickbootEnabled) {
                    OppoGlobalActions.this.startQuickBoot();
                    return;
                }
                OppoGlobalActions.this.mWindowManagerFuncs.shutdown(false);
            }
            OppoGlobalActions.this.mHandler.sendEmptyMessageDelayed(0, 1500);
        }
    };
    private View.OnTouchListener mOnTouchListener = new View.OnTouchListener() {
        /* class com.android.server.policy.OppoGlobalActions.AnonymousClass3 */

        public boolean onTouch(View v, MotionEvent event) {
            return true;
        }
    };
    /* access modifiers changed from: private */
    public PowerViewContainer mPowerView = null;
    private int mPreviousOrientation = 0;
    private Bitmap mScreenBitmap;
    /* access modifiers changed from: private */
    public ShutdownViewControl mShutdownViewControl;
    private StatusBarManager mStatusBarManager;
    private boolean mSystemUiVisibility;
    private WindowManager mWindowManager;
    /* access modifiers changed from: private */
    public final WindowManagerPolicy.WindowManagerFuncs mWindowManagerFuncs;

    /* access modifiers changed from: private */
    public interface Action {
        View create(Context context, View view, ViewGroup viewGroup, LayoutInflater layoutInflater);

        boolean isEnabled();

        boolean onLongPress();

        void onPress();

        boolean showBeforeProvisioning();

        boolean showDuringKeyguard();
    }

    public OppoGlobalActions(Context context, WindowManagerPolicy.WindowManagerFuncs windowManagerFuncs) {
        super(context, windowManagerFuncs);
        this.mContext = context;
        this.mWindowManagerFuncs = windowManagerFuncs;
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.CLOSE_SYSTEM_DIALOGS");
        filter.addAction("android.intent.action.SCREEN_OFF");
        filter.addAction("android.intent.action.EMERGENCY_CALLBACK_MODE_CHANGED");
        context.registerReceiver(this.mBroadcastReceiver, filter);
        this.mDisplayMetrics = new DisplayMetrics();
        this.mWindowManager = (WindowManager) context.getSystemService("window");
        this.mDisplay = this.mWindowManager.getDefaultDisplay();
        this.mDisplay.getRealMetrics(this.mDisplayMetrics);
        this.mStatusBarManager = (StatusBarManager) this.mContext.getSystemService("statusbar");
        this.mActivityManager = (ActivityManager) this.mContext.getSystemService("activity");
        this.mGlobalActionsProvider = (GlobalActionsProvider) LocalServices.getService(GlobalActionsProvider.class);
    }

    public void showDialog(boolean keyguardShowing, boolean isDeviceProvisioned) {
        GlobalActionsProvider globalActionsProvider;
        if (!(Settings.Global.getInt(this.mContext.getContentResolver(), "KEY_DISABLE_GLOBAL_ACTION", 0) != 0)) {
            if (this.mActivityManager.getLockTaskModeState() != 1 || (globalActionsProvider = this.mGlobalActionsProvider) == null || !globalActionsProvider.isGlobalActionsDisabled()) {
                this.mKeyguardShowing = keyguardShowing;
                this.mDeviceProvisioned = isDeviceProvisioned;
                if (this.mPowerView == null) {
                    long begin = System.currentTimeMillis();
                    setShortcutsPanelState(this.mStatusBarManager, false);
                    handleShow();
                    Log.d(TAG, "GlobalActions handleShow cost=" + (System.currentTimeMillis() - begin) + "ms");
                }
                this.mPreviousOrientation = this.mContext.getResources().getConfiguration().orientation;
                return;
            }
            Log.d(TAG, " showDialog false. lockTaskMode = " + this.mActivityManager.getLockTaskModeState() + ", GlobalActionDisabled = " + this.mGlobalActionsProvider.isGlobalActionsDisabled());
        }
    }

    private void handleShow() {
        this.mPowerView = (PowerViewContainer) View.inflate(this.mContext, 201917521, null);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(-1, -1, 2300, 198912, -3);
        lp.privateFlags |= 16;
        initialize();
        try {
            this.mPowerView.getViewRootImpl();
            this.mWindowManager.addView(this.mPowerView, lp);
            this.mPowerView.deliveryValue(this.mWindowManager);
            this.mPowerView.setOnChangeListener(new PowerViewContainer.OnChangeListener() {
                /* class com.android.server.policy.OppoGlobalActions.AnonymousClass1 */

                @Override // com.android.server.policy.OppoGlobalActions.PowerViewContainer.OnChangeListener
                public void onChange() {
                    if (OppoGlobalActions.this.mShutdownViewControl != null) {
                        OppoGlobalActions.this.mShutdownViewControl.tearDown();
                    }
                    PowerViewContainer unused = OppoGlobalActions.this.mPowerView = null;
                }
            });
        } catch (Exception exce) {
            Log.e(TAG, "addView failed!", exce);
            this.mPowerView = null;
        }
    }

    private void setGaussianBlur() {
        this.mDisplay.getRealMetrics(this.mDisplayMetrics);
        float[] dims = {(float) this.mDisplayMetrics.widthPixels, (float) this.mDisplayMetrics.heightPixels};
        if (getDegreesForRotation(this.mDisplay.getRotation()) > 0.0f) {
            dims[0] = Math.abs(dims[0]);
            dims[1] = Math.abs(dims[1]);
        }
        int statusBarHeight = 0;
        if (this.mSystemUiVisibility) {
            statusBarHeight = this.mContext.getResources().getDimensionPixelSize(201654274);
        }
        this.mScreenBitmap = ColorSurfaceControl.screenshot((int) (dims[0] / 4.0f), (int) (dims[1] / 4.0f));
        Bitmap bitmap = this.mScreenBitmap;
        if (bitmap != null) {
            this.mScreenBitmap = Bitmap.createBitmap(bitmap, 0, statusBarHeight / 4, bitmap.getWidth(), this.mScreenBitmap.getHeight() - (statusBarHeight / 4));
            return;
        }
        PowerViewContainer powerViewContainer = this.mPowerView;
        if (powerViewContainer != null) {
            powerViewContainer.setBackgroundColor(mDefaultBackground);
        }
    }

    private void initialize() {
        PowerViewContainer powerViewContainer;
        RemoteException re;
        this.mItems = new ArrayList<>();
        this.mInnovativeV2HAction = new InnovativeV2HAction(this.mContext, this.mHandler);
        this.mItems.add(this.mInnovativeV2HAction);
        List<UserInfo> users = ((UserManager) this.mContext.getSystemService("user")).getUsers();
        boolean isMultiUser = true;
        if (users.size() == 1) {
            isMultiUser = false;
        } else if (users.size() == 2) {
            int i = 0;
            while (true) {
                if (i >= users.size()) {
                    break;
                } else if (users.get(i).id == 999) {
                    isMultiUser = false;
                    break;
                } else {
                    i++;
                }
            }
        } else if (users.size() > 2) {
            isMultiUser = true;
        }
        if (isMultiUser) {
            try {
                re = ActivityManagerNative.getDefault().getCurrentUser();
            } catch (RemoteException e) {
                re = null;
            }
            for (final UserInfo user : users) {
                boolean isCurrentUser = false;
                if (re == null) {
                    if (user.id == 0) {
                        isCurrentUser = true;
                    }
                } else if (((UserInfo) re).id == user.id) {
                    isCurrentUser = true;
                }
                StringBuilder sb = new StringBuilder();
                sb.append(user.name != null ? user.name : "Primary");
                sb.append(isCurrentUser ? " ✔" : "");
                this.mItems.add(new SinglePressAction(201852032, sb.toString()) {
                    /* class com.android.server.policy.OppoGlobalActions.AnonymousClass2 */

                    @Override // com.android.server.policy.OppoGlobalActions.SinglePressAction, com.android.server.policy.OppoGlobalActions.Action
                    public void onPress() {
                        try {
                            ActivityManagerNative.getDefault().switchUser(user.id);
                        } catch (RemoteException re) {
                            Log.e(OppoGlobalActions.TAG, "Couldn't switch user " + re);
                        }
                    }

                    @Override // com.android.server.policy.OppoGlobalActions.Action
                    public boolean showDuringKeyguard() {
                        return true;
                    }

                    @Override // com.android.server.policy.OppoGlobalActions.Action
                    public boolean showBeforeProvisioning() {
                        return false;
                    }
                });
            }
        }
        this.mAdapter = new MyAdapter();
        for (int i2 = 0; i2 < this.mAdapter.getCount() && (powerViewContainer = this.mPowerView) != null; i2++) {
            powerViewContainer.addView(this.mAdapter.getView(i2, null, null));
        }
        this.mAdapter.notifyDataSetChanged();
    }

    public boolean getPRStatus() {
        return isPRStatus;
    }

    public void removePowerView() {
        if (this.mPowerView != null) {
            try {
                if (this.mShutdownViewControl != null) {
                    this.mShutdownViewControl.tearDown();
                    this.mShutdownViewControl = null;
                }
                if (this.mPowerView.isAttachedToWindow()) {
                    this.mWindowManager.removeView(this.mPowerView);
                    setShortcutsPanelState(this.mStatusBarManager, true);
                    this.mPowerView = null;
                }
            } catch (Exception exce) {
                Log.e(TAG, "dismissDialog failed!", exce);
                this.mPowerView = null;
            }
        }
    }

    private class MyAdapter extends BaseAdapter {
        private MyAdapter() {
        }

        public int getCount() {
            int count = 0;
            for (int i = 0; i < OppoGlobalActions.this.mItems.size(); i++) {
                Action action = (Action) OppoGlobalActions.this.mItems.get(i);
                if (!OppoGlobalActions.this.mKeyguardShowing || action.showDuringKeyguard()) {
                    count++;
                }
            }
            return count;
        }

        public boolean isEnabled(int position) {
            return getItem(position).isEnabled();
        }

        public boolean areAllItemsEnabled() {
            return false;
        }

        public Action getItem(int position) {
            int filteredPos = 0;
            for (int i = 0; i < OppoGlobalActions.this.mItems.size(); i++) {
                Action action = (Action) OppoGlobalActions.this.mItems.get(i);
                if (!OppoGlobalActions.this.mKeyguardShowing || action.showDuringKeyguard()) {
                    if (filteredPos == position) {
                        return action;
                    }
                    filteredPos++;
                }
            }
            throw new IllegalArgumentException("position " + position + " out of range of showable actions, filtered count=" + getCount() + ", keyguardshowing=" + OppoGlobalActions.this.mKeyguardShowing + ", provisioned=" + OppoGlobalActions.this.mDeviceProvisioned);
        }

        public long getItemId(int position) {
            return (long) position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            return getItem(position).create(OppoGlobalActions.this.mContext, convertView, parent, LayoutInflater.from(OppoGlobalActions.this.mContext));
        }
    }

    private static abstract class SinglePressAction implements Action {
        private final int mIconResId;
        private final CharSequence mMessage;
        private final int mMessageResId;

        @Override // com.android.server.policy.OppoGlobalActions.Action
        public abstract void onPress();

        protected SinglePressAction(int iconResId, int messageResId) {
            this.mIconResId = iconResId;
            this.mMessageResId = messageResId;
            this.mMessage = null;
        }

        protected SinglePressAction(int iconResId, CharSequence message) {
            this.mIconResId = iconResId;
            this.mMessageResId = 0;
            this.mMessage = message;
        }

        @Override // com.android.server.policy.OppoGlobalActions.Action
        public boolean isEnabled() {
            return true;
        }

        @Override // com.android.server.policy.OppoGlobalActions.Action
        public boolean onLongPress() {
            return false;
        }

        /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
         method: ClspMth{android.view.LayoutInflater.inflate(int, android.view.ViewGroup, boolean):android.view.View}
         arg types: [int, android.view.ViewGroup, int]
         candidates:
          ClspMth{android.view.LayoutInflater.inflate(org.xmlpull.v1.XmlPullParser, android.view.ViewGroup, boolean):android.view.View}
          ClspMth{android.view.LayoutInflater.inflate(int, android.view.ViewGroup, boolean):android.view.View} */
        @Override // com.android.server.policy.OppoGlobalActions.Action
        public View create(Context context, View convertView, ViewGroup parent, LayoutInflater inflater) {
            View v = inflater.inflate(201917507, parent, false);
            TextView messageView = (TextView) v.findViewById(201458841);
            v.findViewById(201458722).setVisibility(8);
            ((ImageView) v.findViewById(201458740)).setImageDrawable(context.getResources().getDrawable(this.mIconResId));
            CharSequence charSequence = this.mMessage;
            if (charSequence != null) {
                messageView.setText(charSequence);
            } else {
                messageView.setText(this.mMessageResId);
            }
            return v;
        }
    }

    /* access modifiers changed from: private */
    public void startQuickBoot() {
        Intent intent = new Intent("org.codeaurora.action.QUICKBOOT");
        intent.putExtra("mode", 0);
        try {
            this.mContext.startActivityAsUser(intent, UserHandle.CURRENT);
        } catch (ActivityNotFoundException e) {
        }
    }

    private class InnovativeV2HAction implements Action {
        private final int ITEM_IDS = 201458828;
        private final Context mContext;
        private final Handler mHandler;

        InnovativeV2HAction(Context context, Handler handler) {
            this.mHandler = handler;
            this.mContext = context;
        }

        @Override // com.android.server.policy.OppoGlobalActions.Action
        public View create(Context context, View convertView, ViewGroup parent, LayoutInflater inflater) {
            ShutdownViewControl unused = OppoGlobalActions.this.mShutdownViewControl = new ShutdownViewControl(context);
            ShutdownView shutdownView = OppoGlobalActions.this.mShutdownViewControl.getShutdownView();
            shutdownView.setTag(201458828);
            OppoGlobalActions.this.mShutdownViewControl.setOnShutdownListener(OppoGlobalActions.this.mOnShutDown);
            OppoGlobalActions.this.mShutdownViewControl.setOnRebootListener(OppoGlobalActions.this.mOnReboot);
            OppoGlobalActions.this.mShutdownViewControl.setOnCancelListener(OppoGlobalActions.this.mOnCancel);
            OppoGlobalActions.this.mShutdownViewControl.startEnterAnimator();
            if (OppoGlobalActions.isIndiaRegion() && OppoGlobalActions.this.mShutdownViewControl != null) {
                OppoGlobalActions.this.mShutdownViewControl.setOnEmergencyListener(OppoGlobalActions.this.mOnEmergency);
            }
            return shutdownView;
        }

        @Override // com.android.server.policy.OppoGlobalActions.Action
        public void onPress() {
        }

        @Override // com.android.server.policy.OppoGlobalActions.Action
        public boolean onLongPress() {
            return false;
        }

        @Override // com.android.server.policy.OppoGlobalActions.Action
        public boolean showDuringKeyguard() {
            return true;
        }

        @Override // com.android.server.policy.OppoGlobalActions.Action
        public boolean showBeforeProvisioning() {
            return true;
        }

        @Override // com.android.server.policy.OppoGlobalActions.Action
        public boolean isEnabled() {
            return true;
        }

        /* access modifiers changed from: package-private */
        public void willCreate() {
        }
    }

    public static boolean isIndiaRegion() {
        if ("IN".equalsIgnoreCase(SystemProperties.get("persist.sys.oppo.region", "CN"))) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: private */
    public void handleEmergencyCall() {
        Intent intent = new Intent("android.intent.action.CALL_EMERGENCY");
        intent.setData(Uri.fromParts("tel", "112", null));
        intent.setFlags(268435456);
        this.mContext.startActivityAsUser(intent, UserHandle.CURRENT);
    }

    /* access modifiers changed from: private */
    public static void setShortcutsPanelState(StatusBarManager statusBarManager, boolean enabled) {
    }

    /* access modifiers changed from: private */
    public static class PowerViewContainer extends LinearLayout {
        private View.OnKeyListener mKeyListener;
        private OnChangeListener mListener;
        private StatusBarManager mStatusBarManager;
        private WindowManager mWm;

        public interface OnChangeListener {
            void onChange();
        }

        public PowerViewContainer(Context context, AttributeSet attrs) {
            super(context, attrs);
            this.mStatusBarManager = (StatusBarManager) context.getSystemService("statusbar");
        }

        public boolean dispatchKeyEvent(KeyEvent event) {
            Log.i(OppoGlobalActions.TAG, "dispatchKeyEvent event.getKeyCode()= " + event.getKeyCode());
            if (event.getKeyCode() != 4) {
                return super.dispatchKeyEvent(event);
            }
            OppoGlobalActions.setShortcutsPanelState(this.mStatusBarManager, true);
            KeyEvent.DispatcherState state = getKeyDispatcherState();
            Log.i(OppoGlobalActions.TAG, "dispatchKeyEvent  state= " + state);
            if (state == null) {
                return super.dispatchKeyEvent(event);
            }
            int count = event.getRepeatCount();
            Log.i(OppoGlobalActions.TAG, "dispatchKeyEvent  count= " + count + " event.getAction()= " + event.getAction());
            if (event.getAction() == 0 && count == 0) {
                state.startTracking(event, this);
                return true;
            }
            if (event.getAction() == 1) {
                boolean isTracking = state.isTracking(event);
                boolean isCanceled = event.isCanceled();
                Log.i(OppoGlobalActions.TAG, "dispatchKeyEvent  isTracking= " + isTracking + " isCanceled= " + isCanceled + " this= " + this);
                if (isTracking && !isCanceled) {
                    removePowerPicture();
                    return true;
                }
            }
            return super.dispatchKeyEvent(event);
        }

        public void deliveryValue(WindowManager mWm2) {
            this.mWm = mWm2;
        }

        public void removePowerPicture() {
            this.mWm.removeView(this);
            OnChangeListener onChangeListener = this.mListener;
            if (onChangeListener != null) {
                onChangeListener.onChange();
            }
        }

        public void setOnChangeListener(OnChangeListener listener) {
            this.mListener = listener;
        }
    }

    public void setSystemUiVisibility(boolean systemUiVisibility) {
        this.mSystemUiVisibility = systemUiVisibility;
    }

    private float getDegreesForRotation(int value) {
        if (value == 1) {
            return ROTATION_90;
        }
        if (value == 2) {
            return ROTATION_180;
        }
        if (value != 3) {
            return 0.0f;
        }
        return ROTATION_270;
    }

    public float getDegreesForRotation() {
        return getDegreesForRotation(this.mDisplay.getRotation());
    }

    public boolean isShowing() {
        return this.mPowerView != null;
    }
}
