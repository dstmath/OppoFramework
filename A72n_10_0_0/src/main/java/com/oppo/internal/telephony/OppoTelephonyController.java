package com.oppo.internal.telephony;

import android.content.Context;
import android.hardware.display.DisplayManager;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Registrant;
import android.os.RegistrantList;
import android.os.SystemProperties;
import android.telephony.Rlog;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.view.Display;
import com.android.internal.telephony.AbstractPhone;
import com.android.internal.telephony.OemConstant;
import com.android.internal.telephony.OemFeature;
import com.android.internal.telephony.OemSimProtect;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneFactory;
import com.android.internal.telephony.util.OemTelephonyUtils;
import com.oppo.internal.telephony.nwdiagnose.NetworkDiagnoseService;
import com.oppo.internal.telephony.recovery.OppoFastRecovery;
import com.oppo.internal.telephony.rf.OemProximitySensorManager;
import com.oppo.internal.telephony.rf.WifiOemProximitySensorManager;
import com.oppo.internal.telephony.rus.RusInitProcess;

public class OppoTelephonyController {
    public static final int CMD_OPPO_REG_FREQ_HOP_IND = 22;
    private static final int EVENT_EXECUTE_REBOOT_DELAYED = 1;
    private static final int EVENT_RADIO_AVAILABLE = 3;
    private static final int EVENT_RIL_CONNECTED = 2;
    public static final String PROPERTY_QCOM_FREQHOP = "persist.sys.qcom_freqhop";
    public static final String PROPERTY_QCOM_FREQHOP_DEFAULT = "1";
    protected static final int REBOOT_DELAYED_TIME = 6000;
    public static final String TAG = "OppoTelephonyController";
    public static boolean mFreqHopEnable = false;
    private static OppoRIL[] mOppoRIL = null;
    private static OppoTelephonyController sMe = null;
    private Context mContext = null;
    private final DisplayManager.DisplayListener mDisplayListener = new DisplayManager.DisplayListener() {
        /* class com.oppo.internal.telephony.OppoTelephonyController.AnonymousClass1 */

        public void onDisplayAdded(int displayId) {
        }

        public void onDisplayRemoved(int displayId) {
        }

        public void onDisplayChanged(int displayId) {
            OppoTelephonyController.this.updateScreenState();
            if (OppoTelephonyController.this.mScreenFirstState || OppoTelephonyController.this.mIsScreenOn != OppoTelephonyController.this.mScreenLastState) {
                OppoTelephonyController.this.mScreenFirstState = false;
                OppoTelephonyController oppoTelephonyController = OppoTelephonyController.this;
                oppoTelephonyController.mScreenLastState = oppoTelephonyController.mIsScreenOn;
                OppoTelephonyController oppoTelephonyController2 = OppoTelephonyController.this;
                oppoTelephonyController2.notifyOemScreenChange(oppoTelephonyController2.mIsScreenOn);
                OppoTelephonyController.this.onUpdateDeviceState();
            }
        }
    };
    private Handler mEventHandler;
    private boolean mIsScreenOn;
    RegistrantList mOemScreenRegistrants = new RegistrantList();
    private boolean mScreenFirstState = true;
    private boolean mScreenLastState;
    private HandlerThread sHandlerThread = new HandlerThread("TeleController");

    public static OppoTelephonyController getInstance(Context context) {
        if (sMe == null) {
            sMe = new OppoTelephonyController(context);
        }
        return sMe;
    }

    private class EventHandler extends Handler {
        public EventHandler(Looper looper) {
            super(looper, null, false);
        }

        public void handleMessage(Message msg) {
            OppoTelephonyController oppoTelephonyController = OppoTelephonyController.this;
            oppoTelephonyController.logd("EventHandler:" + msg.what);
            int i = msg.what;
            if (i == 1) {
                OppoTelephonyController.this.initFetureAfterDelay();
            } else if (i == 2 || i == 3) {
                OppoTelephonyController.this.onReset();
            }
        }
    }

    private OppoTelephonyController(Context context) {
        this.mContext = context;
        mOppoRIL = new OppoRIL[TelephonyManager.getDefault().getPhoneCount()];
        ((DisplayManager) this.mContext.getSystemService("display")).registerDisplayListener(this.mDisplayListener, null);
        updateScreenState();
        this.mScreenLastState = this.mIsScreenOn;
        this.sHandlerThread.start();
        this.mEventHandler = new EventHandler(this.sHandlerThread.getLooper());
        Handler handler = this.mEventHandler;
        handler.sendMessageDelayed(handler.obtainMessage(1, null), 6000);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void initFetureAfterDelay() {
        int numPhones = TelephonyManager.getDefault().getPhoneCount();
        RusInitProcess.execute(this.mContext);
        for (int i = 0; i < numPhones; i++) {
            Phone phone = PhoneFactory.getPhone(i);
            if (phone != null) {
                ((AbstractPhone) OemTelephonyUtils.typeCasting(AbstractPhone.class, phone)).oppoSetCaSwitch();
            }
        }
        if (OemFeature.FEATURE_OPPO_COMM_PROXIMITY) {
            OemProximitySensorManager.getDefault(this.mContext);
            if (OemConstant.isSupportWifiSingleSar()) {
                WifiOemProximitySensorManager.getDefault(this.mContext);
            }
        }
        OemConstant.checkCallState("false");
        OemSimProtect.getInstance().registerSimProtectObserver(this.mContext);
        OppoLinkLatencyManagerService.make(this.mContext);
        NetworkDiagnoseService.make(this.mContext);
        OppoFastRecovery.make(this.mContext);
    }

    public void initByPhone(Phone phone) {
        int phoneId = phone.getPhoneId();
        if (SubscriptionManager.isValidPhoneId(phoneId)) {
            mOppoRIL[phoneId] = new OppoRIL(phone.getContext(), phone);
        }
        if (phoneId == 0) {
            initFreqHopEnable();
            phone.mCi.registerForRilConnected(this.mEventHandler, 2, (Object) null);
            phone.mCi.registerForAvailable(this.mEventHandler, 3, (Object) null);
        }
    }

    public OppoRIL getOppoRIL(int phoneId) {
        if (SubscriptionManager.isValidPhoneId(phoneId)) {
            return mOppoRIL[phoneId];
        }
        return null;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateScreenState() {
        Display[] displays = ((DisplayManager) this.mContext.getSystemService("display")).getDisplays();
        if (displays != null) {
            for (Display display : displays) {
                if (display.getState() == 2) {
                    logd("Screen " + Display.typeToString(display.getType()) + " on");
                    this.mIsScreenOn = true;
                    return;
                }
            }
            logd("Screens all off");
            this.mIsScreenOn = false;
            return;
        }
        logd("No displays found");
        this.mIsScreenOn = false;
    }

    public boolean isScreenOn() {
        return this.mIsScreenOn;
    }

    public void registerForOemScreenChanged(Handler h, int what, Object obj) {
        this.mOemScreenRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterOemScreenChanged(Handler h) {
        this.mOemScreenRegistrants.remove(h);
    }

    public void updateFreqHopEnable(boolean enable) {
        if (enable) {
            SystemProperties.set("persist.sys.qcom_freqhop", "1");
        } else {
            SystemProperties.set("persist.sys.qcom_freqhop", "0");
        }
        initFreqHopEnable();
    }

    private boolean shouldTurnOnFreqHopInd() {
        return this.mIsScreenOn && mFreqHopEnable;
    }

    public void initFreqHopEnable() {
        mFreqHopEnable = SystemProperties.get("persist.sys.qcom_freqhop", "1").equals("1");
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onUpdateDeviceState() {
        notifyOemRotation();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void notifyOemScreenChange(boolean isScreenOn) {
        this.mOemScreenRegistrants.notifyRegistrants(new AsyncResult((Object) null, Boolean.valueOf(isScreenOn), (Throwable) null));
    }

    private void notifyOemRotation() {
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onReset() {
        logd("onReset.");
    }

    /* access modifiers changed from: package-private */
    public void logd(String s) {
        if (OemConstant.SWITCH_LOG) {
            Rlog.d(TAG, s);
        }
    }

    /* access modifiers changed from: package-private */
    public void loge(String s) {
        Rlog.e(TAG, s);
    }
}
