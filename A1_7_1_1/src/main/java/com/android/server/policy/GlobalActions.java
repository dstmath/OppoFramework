package com.android.server.policy;

import android.app.ActivityManager;
import android.app.ActivityManagerNative;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.UserInfo;
import android.database.ContentObserver;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.Vibrator;
import android.provider.Settings.Global;
import android.service.dreams.IDreamManager;
import android.service.dreams.IDreamManager.Stub;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.ArraySet;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import android.view.WindowManagerGlobal;
import android.view.WindowManagerPolicy.WindowManagerFuncs;
import android.view.accessibility.AccessibilityEvent;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.ListView;
import android.widget.TextView;
import com.android.internal.app.AlertController;
import com.android.internal.app.AlertController.AlertParams;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.policy.EmergencyAffordanceManager;
import com.android.internal.widget.LockPatternUtils;
import com.android.server.display.OppoBrightUtils;
import com.android.server.oppo.IElsaManager;
import java.util.ArrayList;
import java.util.List;

class GlobalActions implements OnDismissListener, OnClickListener {
    private static final int DIALOG_DISMISS_DELAY = 300;
    private static final String GLOBAL_ACTION_KEY_AIRPLANE = "airplane";
    private static final String GLOBAL_ACTION_KEY_ASSIST = "assist";
    private static final String GLOBAL_ACTION_KEY_BUGREPORT = "bugreport";
    private static final String GLOBAL_ACTION_KEY_LOCKDOWN = "lockdown";
    private static final String GLOBAL_ACTION_KEY_POWER = "power";
    private static final String GLOBAL_ACTION_KEY_RESTART = "restart";
    private static final String GLOBAL_ACTION_KEY_SETTINGS = "settings";
    private static final String GLOBAL_ACTION_KEY_SILENT = "silent";
    private static final String GLOBAL_ACTION_KEY_USERS = "users";
    private static final String GLOBAL_ACTION_KEY_VOICEASSIST = "voiceassist";
    private static final int MESSAGE_DISMISS = 0;
    private static final int MESSAGE_REFRESH = 1;
    private static final int MESSAGE_SHOW = 2;
    private static final boolean SHOW_SILENT_TOGGLE = true;
    private static final String TAG = "GlobalActions";
    private MyAdapter mAdapter;
    private ContentObserver mAirplaneModeObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange) {
            GlobalActions.this.onAirplaneModeChanged();
        }
    };
    private ToggleAction mAirplaneModeOn;
    private State mAirplaneState = State.Off;
    private final AudioManager mAudioManager;
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("android.intent.action.CLOSE_SYSTEM_DIALOGS".equals(action) || "android.intent.action.SCREEN_OFF".equals(action)) {
                if (!PhoneWindowManager.SYSTEM_DIALOG_REASON_GLOBAL_ACTIONS.equals(intent.getStringExtra(PhoneWindowManager.SYSTEM_DIALOG_REASON_KEY))) {
                    GlobalActions.this.mHandler.sendEmptyMessage(0);
                }
            } else if ("android.intent.action.EMERGENCY_CALLBACK_MODE_CHANGED".equals(action) && !intent.getBooleanExtra("PHONE_IN_ECM_STATE", false) && GlobalActions.this.mIsWaitingForEcmExit) {
                GlobalActions.this.mIsWaitingForEcmExit = false;
                GlobalActions.this.changeAirplaneModeSystemSetting(true);
            }
        }
    };
    private final Context mContext;
    private boolean mDeviceProvisioned = false;
    private GlobalActionsDialog mDialog;
    private final IDreamManager mDreamManager;
    private final EmergencyAffordanceManager mEmergencyAffordanceManager;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    if (GlobalActions.this.mDialog != null) {
                        GlobalActions.this.mDialog.dismiss();
                        GlobalActions.this.mDialog = null;
                        return;
                    }
                    return;
                case 1:
                    GlobalActions.this.refreshSilentMode();
                    GlobalActions.this.mAdapter.notifyDataSetChanged();
                    return;
                case 2:
                    GlobalActions.this.handleShow();
                    return;
                default:
                    return;
            }
        }
    };
    private boolean mHasTelephony;
    private boolean mHasVibrator;
    private boolean mIsWaitingForEcmExit = false;
    private ArrayList<Action> mItems;
    private boolean mKeyguardShowing = false;
    PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
        public void onServiceStateChanged(ServiceState serviceState) {
            if (GlobalActions.this.mHasTelephony) {
                GlobalActions.this.mAirplaneState = serviceState.getState() == 3 ? State.On : State.Off;
                GlobalActions.this.mAirplaneModeOn.updateState(GlobalActions.this.mAirplaneState);
                GlobalActions.this.mAdapter.notifyDataSetChanged();
            }
        }
    };
    private BroadcastReceiver mRingerModeReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("android.media.RINGER_MODE_CHANGED")) {
                GlobalActions.this.mHandler.sendEmptyMessage(1);
            }
        }
    };
    private final boolean mShowSilentToggle;
    private Action mSilentModeAction;
    private final WindowManagerFuncs mWindowManagerFuncs;

    private interface Action {
        View create(Context context, View view, ViewGroup viewGroup, LayoutInflater layoutInflater);

        CharSequence getLabelForAccessibility(Context context);

        boolean isEnabled();

        void onPress();

        boolean showBeforeProvisioning();

        boolean showDuringKeyguard();
    }

    private static abstract class SinglePressAction implements Action {
        private final Drawable mIcon;
        private final int mIconResId;
        private final CharSequence mMessage;
        private final int mMessageResId;

        public abstract void onPress();

        protected SinglePressAction(int iconResId, int messageResId) {
            this.mIconResId = iconResId;
            this.mMessageResId = messageResId;
            this.mMessage = null;
            this.mIcon = null;
        }

        protected SinglePressAction(int iconResId, Drawable icon, CharSequence message) {
            this.mIconResId = iconResId;
            this.mMessageResId = 0;
            this.mMessage = message;
            this.mIcon = icon;
        }

        public boolean isEnabled() {
            return true;
        }

        public String getStatus() {
            return null;
        }

        public CharSequence getLabelForAccessibility(Context context) {
            if (this.mMessage != null) {
                return this.mMessage;
            }
            return context.getString(this.mMessageResId);
        }

        public View create(Context context, View convertView, ViewGroup parent, LayoutInflater inflater) {
            View v = inflater.inflate(17367142, parent, false);
            ImageView icon = (ImageView) v.findViewById(16908294);
            TextView messageView = (TextView) v.findViewById(16908299);
            TextView statusView = (TextView) v.findViewById(16909162);
            String status = getStatus();
            if (TextUtils.isEmpty(status)) {
                statusView.setVisibility(8);
            } else {
                statusView.setText(status);
            }
            if (this.mIcon != null) {
                icon.setImageDrawable(this.mIcon);
                icon.setScaleType(ScaleType.CENTER_CROP);
            } else if (this.mIconResId != 0) {
                icon.setImageDrawable(context.getDrawable(this.mIconResId));
            }
            if (this.mMessage != null) {
                messageView.setText(this.mMessage);
            } else {
                messageView.setText(this.mMessageResId);
            }
            return v;
        }
    }

    private static abstract class ToggleAction implements Action {
        protected int mDisabledIconResid;
        protected int mDisabledStatusMessageResId;
        protected int mEnabledIconResId;
        protected int mEnabledStatusMessageResId;
        protected int mMessageResId;
        protected State mState = State.Off;

        /*  JADX ERROR: NullPointerException in pass: ReSugarCode
            java.lang.NullPointerException
            	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
            	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
            	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
            	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$0(DepthTraversal.java:13)
            	at java.util.ArrayList.forEach(ArrayList.java:1251)
            	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:13)
            	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$0(DepthTraversal.java:13)
            	at java.util.ArrayList.forEach(ArrayList.java:1251)
            	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:13)
            	at jadx.core.ProcessClass.process(ProcessClass.java:32)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            */
        /*  JADX ERROR: NullPointerException in pass: EnumVisitor
            java.lang.NullPointerException
            	at jadx.core.dex.visitors.EnumVisitor.visit(EnumVisitor.java:102)
            	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
            	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$0(DepthTraversal.java:13)
            	at java.util.ArrayList.forEach(ArrayList.java:1251)
            	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:13)
            	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$0(DepthTraversal.java:13)
            	at java.util.ArrayList.forEach(ArrayList.java:1251)
            	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:13)
            	at jadx.core.ProcessClass.process(ProcessClass.java:32)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            */
        enum State {
            ;
            
            private final boolean inTransition;

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.policy.GlobalActions.ToggleAction.State.<clinit>():void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.ProcessClass.process(ProcessClass.java:29)
                	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
                	at jadx.api.JavaClass.decompile(JavaClass.java:62)
                	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
                Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
                	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
                	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
                	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
                	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
                	... 7 more
                */
            static {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.policy.GlobalActions.ToggleAction.State.<clinit>():void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.server.policy.GlobalActions.ToggleAction.State.<clinit>():void");
            }

            private State(boolean intermediate) {
                this.inTransition = intermediate;
            }

            public boolean inTransition() {
                return this.inTransition;
            }
        }

        abstract void onToggle(boolean z);

        public ToggleAction(int enabledIconResId, int disabledIconResid, int message, int enabledStatusMessageResId, int disabledStatusMessageResId) {
            this.mEnabledIconResId = enabledIconResId;
            this.mDisabledIconResid = disabledIconResid;
            this.mMessageResId = message;
            this.mEnabledStatusMessageResId = enabledStatusMessageResId;
            this.mDisabledStatusMessageResId = disabledStatusMessageResId;
        }

        void willCreate() {
        }

        public CharSequence getLabelForAccessibility(Context context) {
            return context.getString(this.mMessageResId);
        }

        public View create(Context context, View convertView, ViewGroup parent, LayoutInflater inflater) {
            willCreate();
            View v = inflater.inflate(17367142, parent, false);
            ImageView icon = (ImageView) v.findViewById(16908294);
            TextView messageView = (TextView) v.findViewById(16908299);
            TextView statusView = (TextView) v.findViewById(16909162);
            boolean enabled = isEnabled();
            if (messageView != null) {
                messageView.setText(this.mMessageResId);
                messageView.setEnabled(enabled);
            }
            boolean on = this.mState == State.On || this.mState == State.TurningOn;
            if (icon != null) {
                icon.setImageDrawable(context.getDrawable(on ? this.mEnabledIconResId : this.mDisabledIconResid));
                icon.setEnabled(enabled);
            }
            if (statusView != null) {
                statusView.setText(on ? this.mEnabledStatusMessageResId : this.mDisabledStatusMessageResId);
                statusView.setVisibility(0);
                statusView.setEnabled(enabled);
            }
            v.setEnabled(enabled);
            return v;
        }

        public final void onPress() {
            if (this.mState.inTransition()) {
                Log.w(GlobalActions.TAG, "shouldn't be able to toggle when in transition");
                return;
            }
            boolean nowOn = this.mState != State.On;
            onToggle(nowOn);
            changeStateFromPress(nowOn);
        }

        public boolean isEnabled() {
            return !this.mState.inTransition();
        }

        protected void changeStateFromPress(boolean buttonOn) {
            this.mState = buttonOn ? State.On : State.Off;
        }

        public void updateState(State state) {
            this.mState = state;
        }
    }

    private interface LongPressAction extends Action {
        boolean onLongPress();
    }

    private class BugReportAction extends SinglePressAction implements LongPressAction {
        public BugReportAction() {
            super(17302386, 17039667);
        }

        public void onPress() {
            if (!ActivityManager.isUserAMonkey()) {
                GlobalActions.this.mHandler.postDelayed(new Runnable() {
                    public void run() {
                        try {
                            MetricsLogger.action(GlobalActions.this.mContext, 292);
                            ActivityManagerNative.getDefault().requestBugReport(1);
                        } catch (RemoteException e) {
                        }
                    }
                }, 500);
            }
        }

        public boolean onLongPress() {
            if (ActivityManager.isUserAMonkey()) {
                return false;
            }
            try {
                MetricsLogger.action(GlobalActions.this.mContext, 293);
                ActivityManagerNative.getDefault().requestBugReport(0);
            } catch (RemoteException e) {
            }
            return false;
        }

        public boolean showDuringKeyguard() {
            return true;
        }

        public boolean showBeforeProvisioning() {
            return false;
        }

        public String getStatus() {
            return GlobalActions.this.mContext.getString(17039673, new Object[]{VERSION.RELEASE, Build.ID});
        }
    }

    private static final class GlobalActionsDialog extends Dialog implements DialogInterface {
        private final MyAdapter mAdapter;
        private final AlertController mAlert = AlertController.create(this.mContext, this, getWindow());
        private boolean mCancelOnUp;
        private final Context mContext = getContext();
        private EnableAccessibilityController mEnableAccessibilityController;
        private boolean mIntercepted;
        private final int mWindowTouchSlop;

        public GlobalActionsDialog(Context context, AlertParams params) {
            super(context, getDialogTheme(context));
            this.mAdapter = (MyAdapter) params.mAdapter;
            this.mWindowTouchSlop = ViewConfiguration.get(context).getScaledWindowTouchSlop();
            params.apply(this.mAlert);
        }

        private static int getDialogTheme(Context context) {
            TypedValue outValue = new TypedValue();
            context.getTheme().resolveAttribute(16843529, outValue, true);
            return outValue.resourceId;
        }

        protected void onStart() {
            if (EnableAccessibilityController.canEnableAccessibilityViaGesture(this.mContext)) {
                this.mEnableAccessibilityController = new EnableAccessibilityController(this.mContext, new Runnable() {
                    public void run() {
                        GlobalActionsDialog.this.dismiss();
                    }
                });
                super.setCanceledOnTouchOutside(false);
            } else {
                this.mEnableAccessibilityController = null;
                super.setCanceledOnTouchOutside(true);
            }
            super.onStart();
        }

        protected void onStop() {
            if (this.mEnableAccessibilityController != null) {
                this.mEnableAccessibilityController.onDestroy();
            }
            super.onStop();
        }

        public boolean dispatchTouchEvent(MotionEvent event) {
            if (this.mEnableAccessibilityController != null) {
                int action = event.getActionMasked();
                if (action == 0) {
                    View decor = getWindow().getDecorView();
                    int eventX = (int) event.getX();
                    int eventY = (int) event.getY();
                    if (eventX < (-this.mWindowTouchSlop) || eventY < (-this.mWindowTouchSlop) || eventX >= decor.getWidth() + this.mWindowTouchSlop || eventY >= decor.getHeight() + this.mWindowTouchSlop) {
                        this.mCancelOnUp = true;
                    }
                }
                try {
                    if (this.mIntercepted) {
                        boolean onTouchEvent = this.mEnableAccessibilityController.onTouchEvent(event);
                        if (action == 1) {
                            if (this.mCancelOnUp) {
                                cancel();
                            }
                            this.mCancelOnUp = false;
                            this.mIntercepted = false;
                        }
                        return onTouchEvent;
                    }
                    this.mIntercepted = this.mEnableAccessibilityController.onInterceptTouchEvent(event);
                    if (this.mIntercepted) {
                        long now = SystemClock.uptimeMillis();
                        event = MotionEvent.obtain(now, now, 3, OppoBrightUtils.MIN_LUX_LIMITI, OppoBrightUtils.MIN_LUX_LIMITI, 0);
                        event.setSource(4098);
                        this.mCancelOnUp = true;
                    }
                    if (action == 1) {
                        if (this.mCancelOnUp) {
                            cancel();
                        }
                        this.mCancelOnUp = false;
                        this.mIntercepted = false;
                    }
                } catch (Throwable th) {
                    if (action == 1) {
                        if (this.mCancelOnUp) {
                            cancel();
                        }
                        this.mCancelOnUp = false;
                        this.mIntercepted = false;
                    }
                }
            }
            return super.dispatchTouchEvent(event);
        }

        public ListView getListView() {
            return this.mAlert.getListView();
        }

        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            this.mAlert.installContent();
        }

        public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
            if (event.getEventType() == 32) {
                for (int i = 0; i < this.mAdapter.getCount(); i++) {
                    CharSequence label = this.mAdapter.getItem(i).getLabelForAccessibility(getContext());
                    if (label != null) {
                        event.getText().add(label);
                    }
                }
            }
            return super.dispatchPopulateAccessibilityEvent(event);
        }

        public boolean onKeyDown(int keyCode, KeyEvent event) {
            if (this.mAlert.onKeyDown(keyCode, event)) {
                return true;
            }
            return super.onKeyDown(keyCode, event);
        }

        public boolean onKeyUp(int keyCode, KeyEvent event) {
            if (this.mAlert.onKeyUp(keyCode, event)) {
                return true;
            }
            return super.onKeyUp(keyCode, event);
        }
    }

    private class MyAdapter extends BaseAdapter {
        /* synthetic */ MyAdapter(GlobalActions this$0, MyAdapter myAdapter) {
            this();
        }

        private MyAdapter() {
        }

        public int getCount() {
            int count = 0;
            for (int i = 0; i < GlobalActions.this.mItems.size(); i++) {
                Action action = (Action) GlobalActions.this.mItems.get(i);
                if ((!GlobalActions.this.mKeyguardShowing || action.showDuringKeyguard()) && (GlobalActions.this.mDeviceProvisioned || action.showBeforeProvisioning())) {
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
            for (int i = 0; i < GlobalActions.this.mItems.size(); i++) {
                Action action = (Action) GlobalActions.this.mItems.get(i);
                if ((!GlobalActions.this.mKeyguardShowing || action.showDuringKeyguard()) && (GlobalActions.this.mDeviceProvisioned || action.showBeforeProvisioning())) {
                    if (filteredPos == position) {
                        return action;
                    }
                    filteredPos++;
                }
            }
            throw new IllegalArgumentException("position " + position + " out of range of showable actions" + ", filtered count=" + getCount() + ", keyguardshowing=" + GlobalActions.this.mKeyguardShowing + ", provisioned=" + GlobalActions.this.mDeviceProvisioned);
        }

        public long getItemId(int position) {
            return (long) position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            return getItem(position).create(GlobalActions.this.mContext, convertView, parent, LayoutInflater.from(GlobalActions.this.mContext));
        }
    }

    private final class PowerAction extends SinglePressAction implements LongPressAction {
        /* synthetic */ PowerAction(GlobalActions this$0, PowerAction powerAction) {
            this();
        }

        private PowerAction() {
            super(17301552, 17039663);
        }

        public boolean onLongPress() {
            if (((UserManager) GlobalActions.this.mContext.getSystemService("user")).hasUserRestriction("no_safe_boot")) {
                return false;
            }
            GlobalActions.this.mWindowManagerFuncs.rebootSafeMode(true);
            return true;
        }

        public boolean showDuringKeyguard() {
            return true;
        }

        public boolean showBeforeProvisioning() {
            return true;
        }

        public void onPress() {
            GlobalActions.this.mWindowManagerFuncs.shutdown(false);
        }
    }

    private final class RestartAction extends SinglePressAction implements LongPressAction {
        /* synthetic */ RestartAction(GlobalActions this$0, RestartAction restartAction) {
            this();
        }

        private RestartAction() {
            super(17302575, 17039664);
        }

        public boolean onLongPress() {
            if (((UserManager) GlobalActions.this.mContext.getSystemService("user")).hasUserRestriction("no_safe_boot")) {
                return false;
            }
            GlobalActions.this.mWindowManagerFuncs.rebootSafeMode(true);
            return true;
        }

        public boolean showDuringKeyguard() {
            return true;
        }

        public boolean showBeforeProvisioning() {
            return true;
        }

        public void onPress() {
            GlobalActions.this.mWindowManagerFuncs.reboot(false);
        }
    }

    private class SilentModeToggleAction extends ToggleAction {
        public SilentModeToggleAction() {
            super(17302262, 17302261, 17039674, 17039675, 17039676);
        }

        void onToggle(boolean on) {
            if (on) {
                GlobalActions.this.mAudioManager.setRingerMode(0);
            } else {
                GlobalActions.this.mAudioManager.setRingerMode(2);
            }
        }

        public boolean showDuringKeyguard() {
            return true;
        }

        public boolean showBeforeProvisioning() {
            return false;
        }
    }

    private static class SilentModeTriStateAction implements Action, View.OnClickListener {
        private final int[] ITEM_IDS = new int[]{16909163, 16909164, 16909165};
        private final AudioManager mAudioManager;
        private final Context mContext;
        private final Handler mHandler;

        SilentModeTriStateAction(Context context, AudioManager audioManager, Handler handler) {
            this.mAudioManager = audioManager;
            this.mHandler = handler;
            this.mContext = context;
        }

        private int ringerModeToIndex(int ringerMode) {
            return ringerMode;
        }

        private int indexToRingerMode(int index) {
            return index;
        }

        public CharSequence getLabelForAccessibility(Context context) {
            return null;
        }

        public View create(Context context, View convertView, ViewGroup parent, LayoutInflater inflater) {
            View v = inflater.inflate(17367143, parent, false);
            int selectedIndex = ringerModeToIndex(this.mAudioManager.getRingerMode());
            for (int i = 0; i < 3; i++) {
                boolean z;
                View itemView = v.findViewById(this.ITEM_IDS[i]);
                if (selectedIndex == i) {
                    z = true;
                } else {
                    z = false;
                }
                itemView.setSelected(z);
                itemView.setTag(Integer.valueOf(i));
                itemView.setOnClickListener(this);
            }
            return v;
        }

        public void onPress() {
        }

        public boolean showDuringKeyguard() {
            return true;
        }

        public boolean showBeforeProvisioning() {
            return false;
        }

        public boolean isEnabled() {
            return true;
        }

        void willCreate() {
        }

        public void onClick(View v) {
            if (v.getTag() instanceof Integer) {
                this.mAudioManager.setRingerMode(indexToRingerMode(((Integer) v.getTag()).intValue()));
                this.mHandler.sendEmptyMessageDelayed(0, 300);
            }
        }
    }

    public GlobalActions(Context context, WindowManagerFuncs windowManagerFuncs) {
        boolean z = false;
        this.mContext = context;
        this.mWindowManagerFuncs = windowManagerFuncs;
        this.mAudioManager = (AudioManager) this.mContext.getSystemService("audio");
        this.mDreamManager = Stub.asInterface(ServiceManager.getService("dreams"));
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.CLOSE_SYSTEM_DIALOGS");
        filter.addAction("android.intent.action.SCREEN_OFF");
        filter.addAction("android.intent.action.EMERGENCY_CALLBACK_MODE_CHANGED");
        context.registerReceiver(this.mBroadcastReceiver, filter);
        this.mHasTelephony = ((ConnectivityManager) context.getSystemService("connectivity")).isNetworkSupported(0);
        ((TelephonyManager) context.getSystemService("phone")).listen(this.mPhoneStateListener, 1);
        this.mContext.getContentResolver().registerContentObserver(Global.getUriFor("airplane_mode_on"), true, this.mAirplaneModeObserver);
        Vibrator vibrator = (Vibrator) this.mContext.getSystemService("vibrator");
        this.mHasVibrator = vibrator != null ? vibrator.hasVibrator() : false;
        if (!this.mContext.getResources().getBoolean(17956994)) {
            z = true;
        }
        this.mShowSilentToggle = z;
        this.mEmergencyAffordanceManager = new EmergencyAffordanceManager(context);
    }

    public void showDialog(boolean keyguardShowing, boolean isDeviceProvisioned) {
        this.mKeyguardShowing = keyguardShowing;
        this.mDeviceProvisioned = isDeviceProvisioned;
        if (this.mDialog != null) {
            this.mDialog.dismiss();
            this.mDialog = null;
            this.mHandler.sendEmptyMessage(2);
            return;
        }
        handleShow();
    }

    private void awakenIfNecessary() {
        if (this.mDreamManager != null) {
            try {
                if (this.mDreamManager.isDreaming()) {
                    this.mDreamManager.awaken();
                }
            } catch (RemoteException e) {
            }
        }
    }

    private void handleShow() {
        awakenIfNecessary();
        this.mDialog = createDialog();
        prepareDialog();
        if (this.mAdapter.getCount() == 1 && (this.mAdapter.getItem(0) instanceof SinglePressAction) && !(this.mAdapter.getItem(0) instanceof LongPressAction)) {
            ((SinglePressAction) this.mAdapter.getItem(0)).onPress();
            return;
        }
        LayoutParams attrs = this.mDialog.getWindow().getAttributes();
        attrs.setTitle(TAG);
        this.mDialog.getWindow().setAttributes(attrs);
        this.mDialog.show();
        this.mDialog.getWindow().getDecorView().setSystemUiVisibility(DumpState.DUMP_INSTALLS);
    }

    private GlobalActionsDialog createDialog() {
        if (this.mHasVibrator) {
            this.mSilentModeAction = new SilentModeTriStateAction(this.mContext, this.mAudioManager, this.mHandler);
        } else {
            this.mSilentModeAction = new SilentModeToggleAction();
        }
        this.mAirplaneModeOn = new ToggleAction(17302382, 17302384, 17039677, 17039678, 17039679) {
            void onToggle(boolean on) {
                if (GlobalActions.this.mHasTelephony && Boolean.parseBoolean(SystemProperties.get("ril.cdma.inecmmode"))) {
                    GlobalActions.this.mIsWaitingForEcmExit = true;
                    Intent ecmDialogIntent = new Intent("android.intent.action.ACTION_SHOW_NOTICE_ECM_BLOCK_OTHERS", null);
                    ecmDialogIntent.addFlags(268435456);
                    GlobalActions.this.mContext.startActivity(ecmDialogIntent);
                    return;
                }
                GlobalActions.this.changeAirplaneModeSystemSetting(on);
            }

            protected void changeStateFromPress(boolean buttonOn) {
                if (GlobalActions.this.mHasTelephony && !Boolean.parseBoolean(SystemProperties.get("ril.cdma.inecmmode"))) {
                    this.mState = buttonOn ? State.TurningOn : State.TurningOff;
                    GlobalActions.this.mAirplaneState = this.mState;
                }
            }

            public boolean showDuringKeyguard() {
                return true;
            }

            public boolean showBeforeProvisioning() {
                return false;
            }
        };
        onAirplaneModeChanged();
        this.mItems = new ArrayList();
        String[] defaultActions = this.mContext.getResources().getStringArray(17236032);
        ArraySet<String> addedKeys = new ArraySet();
        for (String actionKey : defaultActions) {
            if (!addedKeys.contains(actionKey)) {
                if (GLOBAL_ACTION_KEY_POWER.equals(actionKey)) {
                    this.mItems.add(new PowerAction(this, null));
                } else if (GLOBAL_ACTION_KEY_AIRPLANE.equals(actionKey)) {
                    this.mItems.add(this.mAirplaneModeOn);
                } else if (GLOBAL_ACTION_KEY_BUGREPORT.equals(actionKey)) {
                    if (Global.getInt(this.mContext.getContentResolver(), "bugreport_in_power_menu", 0) != 0 && isCurrentUserOwner()) {
                        this.mItems.add(new BugReportAction());
                    }
                } else if (GLOBAL_ACTION_KEY_SILENT.equals(actionKey)) {
                    if (this.mShowSilentToggle) {
                        this.mItems.add(this.mSilentModeAction);
                    }
                } else if ("users".equals(actionKey)) {
                    if (SystemProperties.getBoolean("fw.power_user_switcher", false)) {
                        addUsersToMenu(this.mItems);
                    }
                } else if (GLOBAL_ACTION_KEY_SETTINGS.equals(actionKey)) {
                    this.mItems.add(getSettingsAction());
                } else if (GLOBAL_ACTION_KEY_LOCKDOWN.equals(actionKey)) {
                    this.mItems.add(getLockdownAction());
                } else if (GLOBAL_ACTION_KEY_VOICEASSIST.equals(actionKey)) {
                    this.mItems.add(getVoiceAssistAction());
                } else if ("assist".equals(actionKey)) {
                    this.mItems.add(getAssistAction());
                } else if (GLOBAL_ACTION_KEY_RESTART.equals(actionKey)) {
                    this.mItems.add(new RestartAction(this, null));
                } else {
                    Log.e(TAG, "Invalid global action key " + actionKey);
                }
                addedKeys.add(actionKey);
            }
        }
        if (this.mEmergencyAffordanceManager.needsEmergencyAffordance()) {
            this.mItems.add(getEmergencyAction());
        }
        this.mAdapter = new MyAdapter(this, null);
        AlertParams params = new AlertParams(this.mContext);
        params.mAdapter = this.mAdapter;
        params.mOnClickListener = this;
        params.mForceInverseBackground = true;
        GlobalActionsDialog dialog = new GlobalActionsDialog(this.mContext, params);
        dialog.setCanceledOnTouchOutside(false);
        dialog.getListView().setItemsCanFocus(true);
        dialog.getListView().setLongClickable(true);
        dialog.getListView().setOnItemLongClickListener(new OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
                Action action = GlobalActions.this.mAdapter.getItem(position);
                if (action instanceof LongPressAction) {
                    return ((LongPressAction) action).onLongPress();
                }
                return false;
            }
        });
        dialog.getWindow().setType(2009);
        dialog.setOnDismissListener(this);
        return dialog;
    }

    private Action getSettingsAction() {
        return new SinglePressAction(17302582, 17039680) {
            public void onPress() {
                Intent intent = new Intent("android.settings.SETTINGS");
                intent.addFlags(335544320);
                GlobalActions.this.mContext.startActivity(intent);
            }

            public boolean showDuringKeyguard() {
                return true;
            }

            public boolean showBeforeProvisioning() {
                return true;
            }
        };
    }

    private Action getEmergencyAction() {
        return new SinglePressAction(17302169, 17039665) {
            public void onPress() {
                GlobalActions.this.mEmergencyAffordanceManager.performEmergencyCall();
            }

            public boolean showDuringKeyguard() {
                return true;
            }

            public boolean showBeforeProvisioning() {
                return true;
            }
        };
    }

    private Action getAssistAction() {
        return new SinglePressAction(17302247, 17039681) {
            public void onPress() {
                Intent intent = new Intent("android.intent.action.ASSIST");
                intent.addFlags(335544320);
                GlobalActions.this.mContext.startActivity(intent);
            }

            public boolean showDuringKeyguard() {
                return true;
            }

            public boolean showBeforeProvisioning() {
                return true;
            }
        };
    }

    private Action getVoiceAssistAction() {
        return new SinglePressAction(17302603, 17039682) {
            public void onPress() {
                Intent intent = new Intent("android.intent.action.VOICE_ASSIST");
                intent.addFlags(335544320);
                GlobalActions.this.mContext.startActivity(intent);
            }

            public boolean showDuringKeyguard() {
                return true;
            }

            public boolean showBeforeProvisioning() {
                return true;
            }
        };
    }

    private Action getLockdownAction() {
        return new SinglePressAction(17301551, 17039683) {
            public void onPress() {
                new LockPatternUtils(GlobalActions.this.mContext).requireCredentialEntry(-1);
                try {
                    WindowManagerGlobal.getWindowManagerService().lockNow(null);
                } catch (RemoteException e) {
                    Log.e(GlobalActions.TAG, "Error while trying to lock device.", e);
                }
            }

            public boolean showDuringKeyguard() {
                return true;
            }

            public boolean showBeforeProvisioning() {
                return false;
            }
        };
    }

    private UserInfo getCurrentUser() {
        try {
            return ActivityManagerNative.getDefault().getCurrentUser();
        } catch (RemoteException e) {
            return null;
        }
    }

    private boolean isCurrentUserOwner() {
        UserInfo currentUser = getCurrentUser();
        return currentUser != null ? currentUser.isPrimary() : true;
    }

    /* JADX WARNING: Removed duplicated region for block: B:24:0x0072  */
    /* JADX WARNING: Removed duplicated region for block: B:13:0x003a  */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x0074  */
    /* JADX WARNING: Removed duplicated region for block: B:16:0x004b  */
    /* JADX WARNING: Removed duplicated region for block: B:26:0x0078  */
    /* JADX WARNING: Removed duplicated region for block: B:19:0x0053  */
    /* JADX WARNING: Removed duplicated region for block: B:13:0x003a  */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x0072  */
    /* JADX WARNING: Removed duplicated region for block: B:16:0x004b  */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x0074  */
    /* JADX WARNING: Removed duplicated region for block: B:19:0x0053  */
    /* JADX WARNING: Removed duplicated region for block: B:26:0x0078  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void addUsersToMenu(ArrayList<Action> items) {
        UserManager um = (UserManager) this.mContext.getSystemService("user");
        if (um.isUserSwitcherEnabled()) {
            List<UserInfo> users = um.getUsers();
            UserInfo currentUser = getCurrentUser();
            for (final UserInfo user : users) {
                if (user.supportsSwitchToByUser()) {
                    boolean isCurrentUser;
                    Drawable icon;
                    if (currentUser == null) {
                        isCurrentUser = false;
                        if (user.iconPath != null) {
                            icon = Drawable.createFromPath(user.iconPath);
                        } else {
                            icon = null;
                        }
                        items.add(new SinglePressAction(17302474, icon, (user.name != null ? user.name : "Primary") + (isCurrentUser ? " ✔" : IElsaManager.EMPTY_PACKAGE)) {
                            public void onPress() {
                                try {
                                    ActivityManagerNative.getDefault().switchUser(user.id);
                                } catch (RemoteException re) {
                                    Log.e(GlobalActions.TAG, "Couldn't switch user " + re);
                                }
                            }

                            public boolean showDuringKeyguard() {
                                return true;
                            }

                            public boolean showBeforeProvisioning() {
                                return false;
                            }
                        });
                    } else {
                        isCurrentUser = false;
                        if (user.iconPath != null) {
                        }
                        if (user.name != null) {
                        }
                        if (isCurrentUser) {
                        }
                        items.add(/* anonymous class already generated */);
                    }
                    isCurrentUser = true;
                    if (user.iconPath != null) {
                    }
                    if (user.name != null) {
                    }
                    if (isCurrentUser) {
                    }
                    items.add(/* anonymous class already generated */);
                }
            }
        }
    }

    private void prepareDialog() {
        refreshSilentMode();
        this.mAirplaneModeOn.updateState(this.mAirplaneState);
        this.mAdapter.notifyDataSetChanged();
        this.mDialog.getWindow().setType(2009);
        if (this.mShowSilentToggle) {
            this.mContext.registerReceiver(this.mRingerModeReceiver, new IntentFilter("android.media.RINGER_MODE_CHANGED"));
        }
    }

    private void refreshSilentMode() {
        if (!this.mHasVibrator) {
            ((ToggleAction) this.mSilentModeAction).updateState(this.mAudioManager.getRingerMode() != 2 ? State.On : State.Off);
        }
    }

    public void onDismiss(DialogInterface dialog) {
        if (this.mShowSilentToggle) {
            try {
                this.mContext.unregisterReceiver(this.mRingerModeReceiver);
            } catch (IllegalArgumentException ie) {
                Log.w(TAG, ie);
            }
        }
    }

    public void onClick(DialogInterface dialog, int which) {
        if (!(this.mAdapter.getItem(which) instanceof SilentModeTriStateAction)) {
            dialog.dismiss();
        }
        this.mAdapter.getItem(which).onPress();
    }

    private void onAirplaneModeChanged() {
        boolean airplaneModeOn = true;
        if (!this.mHasTelephony) {
            if (Global.getInt(this.mContext.getContentResolver(), "airplane_mode_on", 0) != 1) {
                airplaneModeOn = false;
            }
            this.mAirplaneState = airplaneModeOn ? State.On : State.Off;
            this.mAirplaneModeOn.updateState(this.mAirplaneState);
        }
    }

    private void changeAirplaneModeSystemSetting(boolean on) {
        Global.putInt(this.mContext.getContentResolver(), "airplane_mode_on", on ? 1 : 0);
        Intent intent = new Intent("android.intent.action.AIRPLANE_MODE");
        intent.addFlags(536870912);
        intent.putExtra("state", on);
        this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
        if (!this.mHasTelephony) {
            State state;
            if (on) {
                state = State.On;
            } else {
                state = State.Off;
            }
            this.mAirplaneState = state;
        }
    }
}
