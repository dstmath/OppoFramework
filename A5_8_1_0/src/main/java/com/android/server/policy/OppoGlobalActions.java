package com.android.server.policy;

import android.app.ActivityManagerNative;
import android.app.StatusBarManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.UserInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings.System;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.KeyEvent.DispatcherState;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceControl;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewRootImpl;
import android.view.ViewRootImpl.ConfigChangedCallback;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.view.WindowManagerPolicy.WindowManagerFuncs;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.server.am.OppoMultiAppManager;
import com.android.server.am.OppoProcessManager;
import com.android.server.display.OppoBrightUtils;
import com.color.widget.ColorGlobalActionView;
import com.color.widget.ColorGlobalActionView.OnCancelListener;
import com.color.widget.ColorGlobalActionView.OnEmergencyListener;
import com.color.widget.ColorGlobalActionView.OnShutDownListener;
import java.util.ArrayList;
import java.util.List;

class OppoGlobalActions implements ConfigChangedCallback {
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
    private MyAdapter mAdapter;
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("android.intent.action.CLOSE_SYSTEM_DIALOGS".equals(action) || "android.intent.action.SCREEN_OFF".equals(action)) {
                String reason = intent.getStringExtra(PhoneWindowManager.SYSTEM_DIALOG_REASON_KEY);
                if (!PhoneWindowManager.SYSTEM_DIALOG_REASON_GLOBAL_ACTIONS.equals(reason) && (PhoneWindowManager.SYSTEM_DIALOG_REASON_RECENT_APPS.equals(reason) ^ 1) != 0) {
                    OppoGlobalActions.this.mHandler.sendEmptyMessage(0);
                }
            } else if ("android.intent.action.EMERGENCY_CALLBACK_MODE_CHANGED".equals(action) && !intent.getBooleanExtra("PHONE_IN_ECM_STATE", false) && OppoGlobalActions.this.mIsWaitingForEcmExit) {
                OppoGlobalActions.this.mIsWaitingForEcmExit = false;
            }
        }
    };
    private final Context mContext;
    private boolean mDeviceProvisioned = false;
    private Display mDisplay;
    private DisplayMetrics mDisplayMetrics;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    if (OppoGlobalActions.this.mPowerView != null) {
                        OppoGlobalActions.this.removePowerView();
                        return;
                    }
                    return;
                case 1:
                    OppoGlobalActions.this.handleEmergencyCall();
                    return;
                default:
                    return;
            }
        }
    };
    private InnovativeV2HAction mInnovativeV2HAction;
    private boolean mIsReceiverRegisted = false;
    private boolean mIsWaitingForEcmExit = false;
    private ArrayList<Action> mItems;
    private boolean mKeyguardShowing = false;
    private OnCancelListener mOnCancel = new OnCancelListener() {
        public void onCancel() {
            OppoGlobalActions.this.mHandler.sendEmptyMessageDelayed(0, 0);
        }
    };
    private OnEmergencyListener mOnEmergency = new OnEmergencyListener() {
        public void onEmergency() {
            OppoGlobalActions.this.mHandler.sendEmptyMessageDelayed(1, 0);
            OppoGlobalActions.this.mHandler.sendEmptyMessageDelayed(0, 300);
        }
    };
    private OnShutDownListener mOnShutDown = new OnShutDownListener() {
        public void onShutDown() {
            Log.d(OppoGlobalActions.TAG, "PRESS SHUTDOW OPTION");
            if (OppoGlobalActions.this.mPowerView != null) {
                OppoGlobalActions.isPRStatus = true;
                if (System.getInt(OppoGlobalActions.this.mContext.getContentResolver(), "enable_quickboot", 0) == 1) {
                    OppoGlobalActions.this.startQuickBoot();
                    return;
                }
                OppoGlobalActions.this.mWindowManagerFuncs.shutdown(false);
            }
            OppoGlobalActions.this.mHandler.sendEmptyMessageDelayed(0, 1500);
        }
    };
    private OnTouchListener mOnTouchListener = new OnTouchListener() {
        public boolean onTouch(View v, MotionEvent event) {
            return true;
        }
    };
    private PowerViewContainer mPowerView = null;
    private int mPreviousOrientation = 0;
    private Bitmap mScreenBitmap;
    private StatusBarManager mStatusBarManager;
    private boolean mSystemUiVisibility;
    private WindowManager mWindowManager;
    private final WindowManagerFuncs mWindowManagerFuncs;

    private interface Action {
        View create(Context context, View view, ViewGroup viewGroup, LayoutInflater layoutInflater);

        boolean isEnabled();

        boolean onLongPress();

        void onPress();

        boolean showBeforeProvisioning();

        boolean showDuringKeyguard();
    }

    private static abstract class SinglePressAction implements Action {
        private final int mIconResId;
        private final CharSequence mMessage;
        private final int mMessageResId;

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

        public boolean isEnabled() {
            return true;
        }

        public boolean onLongPress() {
            return false;
        }

        public View create(Context context, View convertView, ViewGroup parent, LayoutInflater inflater) {
            View v = inflater.inflate(201917507, parent, false);
            ImageView icon = (ImageView) v.findViewById(201458740);
            TextView messageView = (TextView) v.findViewById(201458841);
            v.findViewById(201458722).setVisibility(8);
            icon.setImageDrawable(context.getResources().getDrawable(this.mIconResId));
            if (this.mMessage != null) {
                messageView.setText(this.mMessage);
            } else {
                messageView.setText(this.mMessageResId);
            }
            return v;
        }
    }

    private class InnovativeV2HAction implements Action {
        private final int ITEM_IDS = 201458828;
        private final Context mContext;
        private final Handler mHandler;
        private View mView;

        InnovativeV2HAction(Context context, Handler handler) {
            this.mHandler = handler;
            this.mContext = context;
        }

        public View create(Context context, View convertView, ViewGroup parent, LayoutInflater inflater) {
            this.mView = inflater.inflate(201917502, parent, false);
            ColorGlobalActionView itemView = (ColorGlobalActionView) this.mView.findViewById(201458828);
            if (itemView != null) {
                itemView.setTag(Integer.valueOf(201458828));
                itemView.startAutoDownAnim();
                itemView.setOnShutDownListener(OppoGlobalActions.this.mOnShutDown);
                itemView.setOnCancelListener(OppoGlobalActions.this.mOnCancel);
                if (OppoGlobalActions.isIndiaRegion()) {
                    itemView.setOnEmergencyListener(OppoGlobalActions.this.mOnEmergency);
                }
            }
            return this.mView;
        }

        public void onPress() {
        }

        public boolean onLongPress() {
            return false;
        }

        public boolean showDuringKeyguard() {
            return true;
        }

        public boolean showBeforeProvisioning() {
            return true;
        }

        public boolean isEnabled() {
            return true;
        }

        void willCreate() {
        }
    }

    private class MyAdapter extends BaseAdapter {
        /* synthetic */ MyAdapter(OppoGlobalActions this$0, MyAdapter -this1) {
            this();
        }

        private MyAdapter() {
        }

        public int getCount() {
            int count = 0;
            for (int i = 0; i < OppoGlobalActions.this.mItems.size(); i++) {
                Action action = (Action) OppoGlobalActions.this.mItems.get(i);
                if (!OppoGlobalActions.this.mKeyguardShowing || (action.showDuringKeyguard() ^ 1) == 0) {
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
                if (!OppoGlobalActions.this.mKeyguardShowing || (action.showDuringKeyguard() ^ 1) == 0) {
                    if (filteredPos == position) {
                        return action;
                    }
                    filteredPos++;
                }
            }
            throw new IllegalArgumentException("position " + position + " out of range of showable actions" + ", filtered count=" + getCount() + ", keyguardshowing=" + OppoGlobalActions.this.mKeyguardShowing + ", provisioned=" + OppoGlobalActions.this.mDeviceProvisioned);
        }

        public long getItemId(int position) {
            return (long) position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            return getItem(position).create(OppoGlobalActions.this.mContext, convertView, parent, LayoutInflater.from(OppoGlobalActions.this.mContext));
        }
    }

    private static class PowerViewContainer extends LinearLayout {
        private OnKeyListener mKeyListener;
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
            DispatcherState state = getKeyDispatcherState();
            Log.i(OppoGlobalActions.TAG, "dispatchKeyEvent  state= " + state);
            if (state == null) {
                return super.dispatchKeyEvent(event);
            }
            int count = event.getRepeatCount();
            Log.i(OppoGlobalActions.TAG, "dispatchKeyEvent  count= " + count + " event.getAction()= " + event.getAction());
            if (event.getAction() == 0 && count == 0) {
                if (state != null) {
                    state.startTracking(event, this);
                }
                return true;
            }
            if (event.getAction() == 1) {
                boolean isTracking = state.isTracking(event);
                boolean isCanceled = event.isCanceled();
                Log.i(OppoGlobalActions.TAG, "dispatchKeyEvent  isTracking= " + isTracking + " isCanceled= " + isCanceled + " this= " + this);
                if (!(state == null || !isTracking || (isCanceled ^ 1) == 0 || this == null)) {
                    removePowerPicture();
                    return true;
                }
            }
            return super.dispatchKeyEvent(event);
        }

        public void deliveryValue(WindowManager mWm) {
            this.mWm = mWm;
        }

        public void removePowerPicture() {
            if (this != null) {
                this.mWm.removeView(this);
                if (this.mListener != null) {
                    this.mListener.onChange();
                }
            }
        }

        public void setOnChangeListener(OnChangeListener listener) {
            this.mListener = listener;
        }
    }

    public OppoGlobalActions(Context context, WindowManagerFuncs windowManagerFuncs) {
        this.mContext = context;
        this.mWindowManagerFuncs = windowManagerFuncs;
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.CLOSE_SYSTEM_DIALOGS");
        filter.addAction("android.intent.action.SCREEN_OFF");
        filter.addAction("android.intent.action.EMERGENCY_CALLBACK_MODE_CHANGED");
        context.registerReceiver(this.mBroadcastReceiver, filter);
        this.mDisplayMetrics = new DisplayMetrics();
        this.mWindowManager = (WindowManager) context.getSystemService(OppoProcessManager.RESUME_REASON_VISIBLE_WINDOW_STR);
        this.mDisplay = this.mWindowManager.getDefaultDisplay();
        this.mDisplay.getRealMetrics(this.mDisplayMetrics);
        this.mStatusBarManager = (StatusBarManager) this.mContext.getSystemService("statusbar");
    }

    public void showDialog(boolean keyguardShowing, boolean isDeviceProvisioned) {
        this.mKeyguardShowing = keyguardShowing;
        this.mDeviceProvisioned = isDeviceProvisioned;
        if (this.mPowerView == null) {
            long begin = System.currentTimeMillis();
            setShortcutsPanelState(this.mStatusBarManager, false);
            handleShow();
            Log.d(TAG, "GlobalActions handleShow cost=" + (System.currentTimeMillis() - begin) + "ms");
        }
        this.mPreviousOrientation = this.mContext.getResources().getConfiguration().orientation;
    }

    public void onConfigurationChanged(Configuration globalConfig) {
        if (this.mPreviousOrientation != globalConfig.orientation) {
            removePowerView();
        }
    }

    private void handleShow() {
        this.mPowerView = (PowerViewContainer) View.inflate(this.mContext, 201917521, null);
        LayoutParams lp = new LayoutParams(-1, -1, 2300, 198912, -3);
        initialize();
        try {
            ViewRootImpl viewRoot = this.mPowerView.getViewRootImpl();
            ViewRootImpl.addConfigCallback(this);
            this.mWindowManager.addView(this.mPowerView, lp);
            this.mPowerView.deliveryValue(this.mWindowManager);
            this.mPowerView.setOnChangeListener(new OnChangeListener() {
                public void onChange() {
                    OppoGlobalActions.this.mPowerView = null;
                }
            });
        } catch (Exception exce) {
            Log.e(TAG, "addView failed!", exce);
            this.mPowerView = null;
        }
    }

    private void setGaussianBlur() {
        this.mDisplay.getRealMetrics(this.mDisplayMetrics);
        float[] dims = new float[]{(float) this.mDisplayMetrics.widthPixels, (float) this.mDisplayMetrics.heightPixels};
        if (getDegreesForRotation(this.mDisplay.getRotation()) > OppoBrightUtils.MIN_LUX_LIMITI) {
            dims[0] = Math.abs(dims[0]);
            dims[1] = Math.abs(dims[1]);
        }
        int statusBarHeight = 0;
        if (this.mSystemUiVisibility) {
            statusBarHeight = this.mContext.getResources().getDimensionPixelSize(201654274);
        }
        this.mScreenBitmap = SurfaceControl.screenshot((int) (dims[0] / 4.0f), (int) (dims[1] / 4.0f));
        if (this.mScreenBitmap != null) {
            this.mScreenBitmap = Bitmap.createBitmap(this.mScreenBitmap, 0, statusBarHeight / 4, this.mScreenBitmap.getWidth(), this.mScreenBitmap.getHeight() - (statusBarHeight / 4));
            return;
        }
        if (this.mPowerView != null) {
            this.mPowerView.setBackgroundColor(mDefaultBackground);
        }
    }

    private void initialize() {
        int i;
        this.mItems = new ArrayList();
        this.mInnovativeV2HAction = new InnovativeV2HAction(this.mContext, this.mHandler);
        this.mItems.add(this.mInnovativeV2HAction);
        List<UserInfo> users = ((UserManager) this.mContext.getSystemService("user")).getUsers();
        boolean isMultiUser = true;
        if (users.size() == 1) {
            isMultiUser = false;
        } else if (users.size() == 2) {
            for (i = 0; i < users.size(); i++) {
                if (((UserInfo) users.get(i)).id == OppoMultiAppManager.USER_ID) {
                    isMultiUser = false;
                    break;
                }
            }
        } else if (users.size() > 2) {
            isMultiUser = true;
        }
        if (isMultiUser) {
            UserInfo currentUser;
            try {
                currentUser = ActivityManagerNative.getDefault().getCurrentUser();
            } catch (RemoteException e) {
                currentUser = null;
            }
            for (final UserInfo user : users) {
                boolean isCurrentUser = currentUser == null ? user.id == 0 : currentUser.id == user.id;
                this.mItems.add(new SinglePressAction(201852032, (user.name != null ? user.name : "Primary") + (isCurrentUser ? " ✔" : "")) {
                    public void onPress() {
                        try {
                            ActivityManagerNative.getDefault().switchUser(user.id);
                        } catch (RemoteException re) {
                            Log.e(OppoGlobalActions.TAG, "Couldn't switch user " + re);
                        }
                    }

                    public boolean showDuringKeyguard() {
                        return true;
                    }

                    public boolean showBeforeProvisioning() {
                        return false;
                    }
                });
            }
        }
        this.mAdapter = new MyAdapter(this, null);
        for (i = 0; i < this.mAdapter.getCount() && this.mPowerView != null; i++) {
            this.mPowerView.addView(this.mAdapter.getView(i, null, null));
        }
        this.mAdapter.notifyDataSetChanged();
    }

    public boolean getPRStatus() {
        return isPRStatus;
    }

    public void removePowerView() {
        if (this.mPowerView != null) {
            try {
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

    private void startQuickBoot() {
        Intent intent = new Intent("org.codeaurora.action.QUICKBOOT");
        intent.putExtra("mode", 0);
        try {
            this.mContext.startActivityAsUser(intent, UserHandle.CURRENT);
        } catch (ActivityNotFoundException e) {
        }
    }

    public static boolean isIndiaRegion() {
        if ("IN".equalsIgnoreCase(SystemProperties.get("persist.sys.oppo.region", "CN"))) {
            return true;
        }
        return false;
    }

    private void handleEmergencyCall() {
        Intent intent = new Intent("android.intent.action.CALL_EMERGENCY");
        intent.setData(Uri.fromParts("tel", "112", null));
        intent.setFlags(268435456);
        this.mContext.startActivity(intent);
    }

    private static void setShortcutsPanelState(StatusBarManager statusBarManager, boolean enabled) {
    }

    public void setSystemUiVisibility(boolean systemUiVisibility) {
        this.mSystemUiVisibility = systemUiVisibility;
    }

    private float getDegreesForRotation(int value) {
        switch (value) {
            case 1:
                return ROTATION_90;
            case 2:
                return ROTATION_180;
            case 3:
                return ROTATION_270;
            default:
                return OppoBrightUtils.MIN_LUX_LIMITI;
        }
    }

    public float getDegreesForRotation() {
        return getDegreesForRotation(this.mDisplay.getRotation());
    }

    public boolean isShowing() {
        return this.mPowerView != null;
    }
}
