package com.mediatek.internal.telephony;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.telephony.RadioAccessFamily;
import android.telephony.Rlog;
import android.telephony.TelephonyManager;
import com.android.internal.telecom.ITelecomService;
import com.android.internal.telephony.CommandException;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneSwitcher;
import com.android.internal.telephony.ProxyController;
import com.android.internal.telephony.RadioCapability;
import com.android.internal.telephony.uicc.UiccController;
import com.mediatek.internal.telephony.devreg.DeviceRegisterController;
import com.mediatek.internal.telephony.phb.MtkUiccPhoneBookController;
import com.mediatek.internal.telephony.worldphone.WorldPhoneUtil;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;

public class MtkProxyController extends ProxyController {
    private static final int C6M_1RILD = 2;
    private static final int C6M_3RILD = 1;
    private static final int EVENT_ON_REQUEST = 7;
    private static final int EVENT_RADIO_AVAILABLE = 6;
    private static final int G5M_1RILD = 0;
    private static final String PROPERTY_CAPABILITY_SWITCH = "persist.vendor.radio.simswitch";
    private static final String PROPERTY_CAPABILITY_SWITCH_STATE = "persist.vendor.radio.simswitchstate";
    private static final int RC_CANNOT_SWITCH = 2;
    private static final int RC_DO_SWITCH = 0;
    private static final int RC_NO_NEED_SWITCH = 1;
    private static final int RC_RETRY_CAUSE_AIRPLANE_MODE = 5;
    private static final int RC_RETRY_CAUSE_CAPABILITY_SWITCHING = 2;
    private static final int RC_RETRY_CAUSE_IN_CALL = 3;
    private static final int RC_RETRY_CAUSE_NONE = 0;
    private static final int RC_RETRY_CAUSE_RADIO_UNAVAILABLE = 4;
    private static final int RC_RETRY_CAUSE_RESULT_ERROR = 6;
    private static final int RC_RETRY_CAUSE_WORLD_MODE_SWITCHING = 1;
    private BroadcastReceiver mCallStateReceiver = new BroadcastReceiver() {
        /* class com.mediatek.internal.telephony.MtkProxyController.AnonymousClass3 */

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            MtkProxyController mtkProxyController = MtkProxyController.this;
            mtkProxyController.mtkLogd("mCallStateReceiver: action = " + action);
            if (TelephonyManager.getDefault().getCallState() == 0 && !MtkProxyController.this.isEccInProgress() && MtkProxyController.this.mNextRafs != null && MtkProxyController.this.mSetRafRetryCause == 3) {
                MtkProxyController.this.unRegisterCallStateReceiver();
                try {
                    if (!MtkProxyController.this.setRadioCapability(MtkProxyController.this.mNextRafs)) {
                        MtkProxyController.this.sendCapabilityFailBroadcast();
                    }
                } catch (RuntimeException e) {
                    MtkProxyController.this.sendCapabilityFailBroadcast();
                }
            }
        }
    };
    RadioAccessFamily[] mCurrRafs = null;
    private DeviceRegisterController mDeviceRegisterController;
    private boolean mHasRegisterCallStateReceiver = false;
    private boolean mHasRegisterWorldModeReceiver = false;
    private boolean mIsCapSwitching = false;
    private Handler mMtkHandler = new Handler() {
        /* class com.mediatek.internal.telephony.MtkProxyController.AnonymousClass1 */

        public void handleMessage(Message msg) {
            MtkProxyController mtkProxyController = MtkProxyController.this;
            mtkProxyController.mtkLogd("mtkHandleMessage msg.what=" + msg.what);
            int i = msg.what;
            if (i != 6) {
                if (i == 7) {
                    MtkProxyController mtkProxyController2 = MtkProxyController.this;
                    mtkProxyController2.onSetRadioCapabilityRequest(mtkProxyController2.mCurrRafs);
                }
            } else if (MtkProxyController.this.mRildMode != 2) {
                MtkProxyController.this.onRetryWhenRadioAvailable(msg);
            }
        }
    };
    private MtkPhoneSubInfoControllerEx mMtkPhoneSubInfoControllerEx;
    protected MtkUiccPhoneBookController mMtkUiccPhoneBookController;
    private MtkUiccSmsController mMtkUiccSmsController;
    RadioAccessFamily[] mNextRafs = null;
    private IMtkProxyControllerExt mProxyControllerExt = null;
    private int mRildMode;
    private int mSetRafRetryCause;
    private OpTelephonyCustomizationFactoryBase mTelephonyCustomizationFactory = null;
    private BroadcastReceiver mWorldModeReceiver = new BroadcastReceiver() {
        /* class com.mediatek.internal.telephony.MtkProxyController.AnonymousClass2 */

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            MtkProxyController mtkProxyController = MtkProxyController.this;
            mtkProxyController.mtkLogd("mWorldModeReceiver: action = " + action);
            if (!WorldPhoneUtil.isWorldModeSupport() && WorldPhoneUtil.isWorldPhoneSupport() && ModemSwitchHandler.ACTION_MODEM_SWITCH_DONE.equals(action) && MtkProxyController.this.mNextRafs != null && MtkProxyController.this.mSetRafRetryCause == 1) {
                try {
                    if (!MtkProxyController.this.setRadioCapability(MtkProxyController.this.mNextRafs)) {
                        MtkProxyController.this.sendCapabilityFailBroadcast();
                    }
                } catch (RuntimeException e) {
                    MtkProxyController.this.sendCapabilityFailBroadcast();
                }
            }
        }
    };
    private int onExceptionCount = 0;

    public MtkProxyController(Context context, Phone[] phone, UiccController uiccController, CommandsInterface[] ci, PhoneSwitcher phoneSwitcher) {
        super(context, phone, uiccController, ci, phoneSwitcher);
        String rilMode = SystemProperties.get("ro.vendor.mtk_ril_mode", "c6m_1rild");
        if (rilMode.equals("c6m_1rild")) {
            this.mRildMode = 2;
        } else if (rilMode.equals("c6m_3rild")) {
            this.mRildMode = 1;
        } else {
            this.mRildMode = 0;
        }
        mtkLogd("Constructor - Enter, rild mode = " + this.mRildMode);
        this.mMtkUiccPhoneBookController = new MtkUiccPhoneBookController(this.mPhones);
        this.mMtkPhoneSubInfoControllerEx = new MtkPhoneSubInfoControllerEx(this.mContext, this.mPhones);
        this.mMtkUiccSmsController = new MtkUiccSmsController(this.mPhones);
        mtkLogd("Constructor - Exit");
        this.mDeviceRegisterController = new DeviceRegisterController(this.mContext, this.mPhones, this.mMtkUiccSmsController);
        if (this.mRildMode != 2) {
            try {
                this.mTelephonyCustomizationFactory = OpTelephonyCustomizationUtils.getOpFactory(context);
                this.mProxyControllerExt = this.mTelephonyCustomizationFactory.makeMtkProxyControllerExt(context);
            } catch (Exception e) {
                mtkLogd("mProxyControllerExt init fail");
                e.printStackTrace();
            }
        }
    }

    public DeviceRegisterController getDeviceRegisterController() {
        return this.mDeviceRegisterController;
    }

    public boolean setRadioCapability(RadioAccessFamily[] rafs) {
        if (SystemProperties.getBoolean("ro.vendor.mtk_disable_cap_switch", false)) {
            completeRadioCapabilityTransaction();
            mtkLogd("skip switching because mtk_disable_cap_switch is true");
        } else {
            Message tmsg = this.mMtkHandler.obtainMessage(7);
            this.mCurrRafs = rafs;
            this.mMtkHandler.sendMessage(tmsg);
        }
        return true;
    }

    public boolean onSetRadioCapabilityRequest(RadioAccessFamily[] rafs) {
        if (this.mRildMode == 2) {
            return MtkProxyController.super.setRadioCapability(rafs);
        }
        if (rafs.length == this.mPhones.length) {
            int i = 0;
            while (true) {
                if (i >= rafs.length) {
                    break;
                } else if ((rafs[i].getRadioAccessFamily() & 1) > 0) {
                    SystemProperties.set(PROPERTY_CAPABILITY_SWITCH_STATE, String.valueOf(i));
                    break;
                } else {
                    i++;
                }
            }
            if (checkRadioCapabilitySwitchConditions(rafs) == 0) {
                return MtkProxyController.super.setRadioCapability(rafs);
            }
            return true;
        }
        throw new RuntimeException("Length of input rafs must equal to total phone count");
    }

    /* access modifiers changed from: protected */
    public boolean doSetRadioCapabilities(RadioAccessFamily[] rafs) {
        if (this.mRildMode == 2) {
            return MtkProxyController.super.doSetRadioCapabilities(rafs);
        }
        synchronized (this) {
            this.mIsCapSwitching = true;
        }
        this.onExceptionCount = 0;
        return MtkProxyController.super.doSetRadioCapabilities(rafs);
    }

    /* access modifiers changed from: protected */
    public void onStartRadioCapabilityResponse(Message msg) {
        synchronized (this.mSetRadioAccessFamilyStatus) {
            AsyncResult ar = (AsyncResult) msg.obj;
            boolean z = true;
            if (ar.exception != null) {
                if (this.onExceptionCount == 0 && this.mRildMode != 2) {
                    CommandException.Error err = null;
                    this.onExceptionCount = 1;
                    if (ar.exception instanceof CommandException) {
                        err = ar.exception.getCommandError();
                    }
                    if (err == CommandException.Error.RADIO_NOT_AVAILABLE) {
                        this.mSetRafRetryCause = 4;
                        for (int i = 0; i < this.mPhones.length; i++) {
                            this.mCi[i].registerForAvailable(this.mMtkHandler, 6, (Object) null);
                        }
                        mtkLoge("onStartRadioCapabilityResponse: Retry later due to modem off");
                    }
                }
                mtkLogd("onStartRadioCapabilityResponse got exception=" + ar.exception);
                this.mRadioCapabilitySessionId = this.mUniqueIdGenerator.getAndIncrement();
                sendCapabilityFailBroadcast();
                resetSimSwitchState();
                return;
            }
            RadioCapability rc = (RadioCapability) ((AsyncResult) msg.obj).result;
            if (rc != null) {
                if (rc.getSession() == this.mRadioCapabilitySessionId) {
                    this.mRadioAccessFamilyStatusCounter--;
                    int id = rc.getPhoneId();
                    if (((AsyncResult) msg.obj).exception != null) {
                        mtkLogd("onStartRadioCapabilityResponse: Error response session=" + rc.getSession());
                        mtkLogd("onStartRadioCapabilityResponse: phoneId=" + id + " status=FAIL");
                        this.mSetRadioAccessFamilyStatus[id] = 5;
                        this.mTransactionFailed = true;
                    } else {
                        mtkLogd("onStartRadioCapabilityResponse: phoneId=" + id + " status=STARTED");
                        this.mSetRadioAccessFamilyStatus[id] = 2;
                    }
                    if (this.mRadioAccessFamilyStatusCounter == 0) {
                        StringBuilder sb = new StringBuilder();
                        sb.append("onStartRadioCapabilityResponse: success=");
                        if (this.mTransactionFailed) {
                            z = false;
                        }
                        sb.append(z);
                        mtkLogd(sb.toString());
                        if (this.mTransactionFailed) {
                            issueFinish(this.mRadioCapabilitySessionId);
                        } else {
                            resetRadioAccessFamilyStatusCounter();
                            for (int i2 = 0; i2 < this.mPhones.length; i2++) {
                                sendRadioCapabilityRequest(i2, this.mRadioCapabilitySessionId, 2, this.mNewRadioAccessFamily[i2], this.mNewLogicalModemIds[i2], 0, 3);
                                mtkLogd("onStartRadioCapabilityResponse: phoneId=" + i2 + " status=APPLYING");
                                this.mSetRadioAccessFamilyStatus[i2] = 3;
                            }
                        }
                    }
                    return;
                }
            }
            mtkLogd("onStartRadioCapabilityResponse: Ignore session=" + this.mRadioCapabilitySessionId + " rc=" + rc);
        }
    }

    /* access modifiers changed from: protected */
    public void onApplyRadioCapabilityErrorHandler(Message msg) {
        if (this.mRildMode == 2) {
            this.mRadioCapabilitySessionId = this.mUniqueIdGenerator.getAndIncrement();
            sendCapabilityFailBroadcast();
            resetSimSwitchState();
            return;
        }
        RadioCapability rc = (RadioCapability) ((AsyncResult) msg.obj).result;
        AsyncResult ar = (AsyncResult) msg.obj;
        CommandException.Error err = null;
        if (rc == null && ar.exception != null && this.onExceptionCount == 0) {
            this.onExceptionCount = 1;
            if (ar.exception instanceof CommandException) {
                err = ar.exception.getCommandError();
            }
            if (err == CommandException.Error.RADIO_NOT_AVAILABLE) {
                this.mSetRafRetryCause = 4;
                for (int i = 0; i < this.mPhones.length; i++) {
                    this.mCi[i].registerForAvailable(this.mMtkHandler, 6, (Object) null);
                }
                mtkLoge("onApplyRadioCapabilityResponse: Retry due to RADIO_NOT_AVAILABLE");
            } else {
                mtkLoge("onApplyRadioCapabilityResponse: exception=" + ar.exception);
            }
            this.mRadioCapabilitySessionId = this.mUniqueIdGenerator.getAndIncrement();
            sendCapabilityFailBroadcast();
            resetSimSwitchState();
        }
    }

    /* access modifiers changed from: protected */
    public void onApplyExceptionHandler(Message msg) {
        if (this.mRildMode == 2) {
            MtkProxyController.super.onApplyExceptionHandler(msg);
            return;
        }
        AsyncResult ar = (AsyncResult) msg.obj;
        int id = ((RadioCapability) ((AsyncResult) msg.obj).result).getPhoneId();
        CommandException.Error err = null;
        if (ar.exception instanceof CommandException) {
            err = ar.exception.getCommandError();
        }
        if (err == CommandException.Error.RADIO_NOT_AVAILABLE) {
            this.mSetRafRetryCause = 4;
            this.mCi[id].registerForAvailable(this.mMtkHandler, 6, (Object) null);
            mtkLoge("onApplyRadioCapabilityResponse: Retry later due to modem off");
            return;
        }
        mtkLoge("onApplyRadioCapabilityResponse: exception=" + ar.exception);
    }

    /* access modifiers changed from: protected */
    public void onNotificationRadioCapabilityChanged(Message msg) {
        RadioCapability rc = (RadioCapability) ((AsyncResult) msg.obj).result;
        if (rc == null) {
            logd("onNotificationRadioCapabilityChanged: rc == null");
            return;
        }
        logd("onNotificationRadioCapabilityChanged: rc=" + rc);
        int id = rc.getPhoneId();
        if (((AsyncResult) msg.obj).exception == null) {
            logd("onNotificationRadioCapabilityChanged: update phone capability");
            this.mPhones[id].radioCapabilityUpdated(rc);
        }
        MtkProxyController.super.onNotificationRadioCapabilityChanged(msg);
    }

    /* access modifiers changed from: protected */
    public void onFinishRadioCapabilityResponse(Message msg) {
        RadioCapability rc = (RadioCapability) ((AsyncResult) msg.obj).result;
        if ((rc == null || rc.getSession() != this.mRadioCapabilitySessionId) && rc == null && ((AsyncResult) msg.obj).exception != null) {
            synchronized (this.mSetRadioAccessFamilyStatus) {
                mtkLogd("onFinishRadioCapabilityResponse C2K mRadioAccessFamilyStatusCounter=" + this.mRadioAccessFamilyStatusCounter);
                this.mRadioAccessFamilyStatusCounter = this.mRadioAccessFamilyStatusCounter - 1;
                if (this.mRadioAccessFamilyStatusCounter == 0) {
                    completeRadioCapabilityTransaction();
                }
            }
        } else if (this.mRildMode == 2) {
            MtkProxyController.super.onFinishRadioCapabilityResponse(msg);
        } else {
            int phoneId = SystemProperties.getInt(PROPERTY_CAPABILITY_SWITCH_STATE, -1);
            if (phoneId >= 0 && phoneId < this.mPhones.length && this.mRadioAccessFamilyStatusCounter == 1) {
                int raf = this.mPhones[phoneId].getRadioAccessFamily();
                if ((raf & 1) == 0) {
                    mtkLogd("onFinishRadioCapabilityResponse, main phone raf[" + phoneId + "]=" + raf);
                    this.mSetRafRetryCause = 6;
                }
            }
            MtkProxyController.super.onFinishRadioCapabilityResponse(msg);
        }
    }

    /* access modifiers changed from: protected */
    public void onTimeoutRadioCapability(Message msg) {
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(5, this.mRadioCapabilitySessionId, 0), 45000);
    }

    /* access modifiers changed from: protected */
    public void issueFinish(int sessionId) {
        if (this.mRildMode == 2) {
            MtkProxyController.super.issueFinish(sessionId);
            return;
        }
        synchronized (this.mSetRadioAccessFamilyStatus) {
            resetRadioAccessFamilyStatusCounter();
            for (int i = 0; i < this.mPhones.length; i++) {
                mtkLogd("issueFinish: phoneId=" + i + " sessionId=" + sessionId + " mTransactionFailed=" + this.mTransactionFailed);
                sendRadioCapabilityRequest(i, sessionId, 4, this.mOldRadioAccessFamily[i], this.mCurrentLogicalModemIds[i], this.mTransactionFailed ? 2 : 1, 4);
                if (this.mTransactionFailed) {
                    mtkLogd("issueFinish: phoneId: " + i + " status: FAIL");
                    this.mSetRadioAccessFamilyStatus[i] = 5;
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void completeRadioCapabilityTransaction() {
        Intent intent;
        StringBuilder sb = new StringBuilder();
        sb.append("onFinishRadioCapabilityResponse: success=");
        sb.append(!this.mTransactionFailed);
        mtkLogd(sb.toString());
        SystemProperties.set(PROPERTY_CAPABILITY_SWITCH_STATE, "-1");
        if (!this.mTransactionFailed) {
            ArrayList<RadioAccessFamily> phoneRAFList = new ArrayList<>();
            for (int i = 0; i < this.mPhones.length; i++) {
                int raf = this.mPhones[i].getRadioAccessFamily();
                mtkLogd("radioAccessFamily[" + i + "]=" + raf);
                phoneRAFList.add(new RadioAccessFamily(i, raf));
            }
            intent = new Intent("android.intent.action.ACTION_SET_RADIO_CAPABILITY_DONE");
            intent.putParcelableArrayListExtra("rafs", phoneRAFList);
            this.mRadioCapabilitySessionId = this.mUniqueIdGenerator.getAndIncrement();
            resetSimSwitchState();
        } else {
            intent = new Intent("android.intent.action.ACTION_SET_RADIO_CAPABILITY_FAILED");
            this.mTransactionFailed = false;
            resetSimSwitchState();
        }
        this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL, "android.permission.READ_PHONE_STATE");
        if (this.mRildMode != 2 && this.mNextRafs != null) {
            int i2 = this.mSetRafRetryCause;
            if (i2 == 2 || i2 == 6) {
                mtkLogd("has next request, trigger it, cause = " + this.mSetRafRetryCause);
                try {
                    if (!setRadioCapability(this.mNextRafs)) {
                        sendCapabilityFailBroadcast();
                        return;
                    }
                    this.mSetRafRetryCause = 0;
                    this.mNextRafs = null;
                } catch (RuntimeException e) {
                    sendCapabilityFailBroadcast();
                }
            }
        }
    }

    private void resetSimSwitchState() {
        if (isCapabilitySwitching()) {
            this.mHandler.removeMessages(5);
        }
        if (this.mRildMode == 2) {
            clearTransaction();
            return;
        }
        synchronized (this) {
            this.mIsCapSwitching = false;
        }
        clearTransaction();
    }

    /* access modifiers changed from: protected */
    public void sendRadioCapabilityRequest(int phoneId, int sessionId, int rcPhase, int radioFamily, String logicalModemId, int status, int eventId) {
        if (this.mRildMode == 2) {
            MtkProxyController.super.sendRadioCapabilityRequest(phoneId, sessionId, rcPhase, radioFamily, logicalModemId, status, eventId);
            return;
        }
        if (logicalModemId == null || logicalModemId.equals("")) {
            logicalModemId = "modem_sys3";
        }
        MtkProxyController.super.sendRadioCapabilityRequest(phoneId, sessionId, rcPhase, radioFamily, logicalModemId, status, eventId);
    }

    public int getMaxRafSupported() {
        int[] iArr = new int[this.mPhones.length];
        int maxRaf = 0;
        if (this.mRildMode == 2) {
            return MtkProxyController.super.getMaxRafSupported();
        }
        for (int len = 0; len < this.mPhones.length; len++) {
            if ((this.mPhones[len].getRadioAccessFamily() & 1) == 1) {
                maxRaf = this.mPhones[len].getRadioAccessFamily();
            }
        }
        mtkLogd("getMaxRafSupported: maxRafBit=0 maxRaf=" + maxRaf + " flag=" + (maxRaf & 1));
        if (maxRaf == 0) {
            return maxRaf | 1;
        }
        return maxRaf;
    }

    public int getMinRafSupported() {
        int[] iArr = new int[this.mPhones.length];
        int minRaf = 0;
        if (this.mRildMode == 2) {
            return MtkProxyController.super.getMinRafSupported();
        }
        for (int len = 0; len < this.mPhones.length; len++) {
            if ((this.mPhones[len].getRadioAccessFamily() & 1) == 0) {
                minRaf = this.mPhones[len].getRadioAccessFamily();
            }
        }
        mtkLogd("getMinRafSupported: minRafBit=0 minRaf=" + minRaf + " flag=" + (minRaf & 1));
        return minRaf;
    }

    /* access modifiers changed from: protected */
    public void mtkLogd(String string) {
        Rlog.d("MtkProxyController", string);
    }

    /* access modifiers changed from: protected */
    public void mtkLoge(String string) {
        Rlog.e("MtkProxyController", string);
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        try {
            this.mPhoneSwitcher.dump(fd, pw, args);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isCapabilitySwitching() {
        boolean z;
        if (this.mRildMode == 2) {
            synchronized (this.mSetRadioAccessFamilyStatus) {
                for (int i = 0; i < this.mPhones.length; i++) {
                    if (!(this.mSetRadioAccessFamilyStatus[i] == 2 || this.mSetRadioAccessFamilyStatus[i] == 3)) {
                        if (this.mSetRadioAccessFamilyStatus[i] != 4) {
                        }
                    }
                    mtkLogd("isCapabilitySwitching: Phone[" + i + "] status is " + this.mSetRadioAccessFamilyStatus[i]);
                    return true;
                }
                return false;
            }
        }
        synchronized (this) {
            z = this.mIsCapSwitching;
        }
        return z;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:101:0x0162, code lost:
        if (r9 != false) goto L_0x0168;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:102:0x0164, code lost:
        r10 = r1;
        r9 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:103:0x0168, code lost:
        mtkLogd("set more than one 3G phone, fail");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:104:0x016d, code lost:
        monitor-enter(r14);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:106:?, code lost:
        r14.mIsCapSwitching = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:107:0x0170, code lost:
        monitor-exit(r14);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:109:0x0178, code lost:
        throw new java.lang.RuntimeException("input parameter is incorrect");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:114:0x018a, code lost:
        if (r14.mPhones[r8].getRadioAccessFamily() == r15[r8].getRadioAccessFamily()) goto L_0x018e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:115:0x018c, code lost:
        r7 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:116:0x018e, code lost:
        r8 = r8 + 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:117:0x0191, code lost:
        if (r7 == false) goto L_0x01a0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:118:0x0193, code lost:
        mtkLogd("setRadioCapability: Already in requested configuration, nothing to do.");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:119:0x0198, code lost:
        monitor-enter(r14);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:121:?, code lost:
        r14.mIsCapSwitching = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:122:0x019b, code lost:
        monitor-exit(r14);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:123:0x019c, code lost:
        return 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:127:0x01a0, code lost:
        if (r9 == false) goto L_0x0274;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:129:0x01a8, code lost:
        if (android.os.SystemProperties.getInt("ro.vendor.mtk_external_sim_support", 0) != 1) goto L_0x0243;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:130:0x01aa, code lost:
        r1 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:132:0x01ae, code lost:
        if (r1 >= r14.mPhones.length) goto L_0x01fb;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:133:0x01b0, code lost:
        android.telephony.TelephonyManager.getDefault();
        r5 = android.telephony.TelephonyManager.getTelephonyProperty(r1, "vendor.gsm.external.sim.enabled", "0");
        android.telephony.TelephonyManager.getDefault();
        r8 = android.telephony.TelephonyManager.getTelephonyProperty(r1, "vendor.gsm.external.sim.inserted", "0");
        r11 = com.mediatek.internal.telephony.MtkSubscriptionController.getMtkInstance().getPhoneId(com.mediatek.internal.telephony.MtkSubscriptionController.getMtkInstance().getDefaultDataSubId());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:134:0x01dc, code lost:
        if ("1".equals(r5) == false) goto L_0x01f8;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:136:0x01e4, code lost:
        if ("0".equals(r8) != false) goto L_0x01ee;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:138:0x01ec, code lost:
        if ("".equals(r8) == false) goto L_0x01f8;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:139:0x01ee, code lost:
        if (r10 == r11) goto L_0x01f8;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0028, code lost:
        if (android.os.SystemProperties.getBoolean("ro.vendor.mtk_disable_cap_switch", false) != true) goto L_0x0035;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:140:0x01f0, code lost:
        monitor-enter(r14);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:142:?, code lost:
        r14.mIsCapSwitching = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:143:0x01f3, code lost:
        monitor-exit(r14);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:144:0x01f4, code lost:
        return 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:148:0x01f8, code lost:
        r1 = r1 + 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:149:0x01fb, code lost:
        r1 = com.mediatek.internal.telephony.RadioCapabilitySwitchUtil.getMainCapabilityPhoneId();
        android.telephony.TelephonyManager.getDefault();
        r5 = android.telephony.TelephonyManager.getTelephonyProperty(r1, "vendor.gsm.external.sim.enabled", "0");
        android.telephony.TelephonyManager.getDefault();
        r8 = android.telephony.TelephonyManager.getTelephonyProperty(r1, "vendor.gsm.external.sim.inserted", "0");
        r11 = com.mediatek.telephony.internal.telephony.vsim.ExternalSimManager.getPreferedRsimSlot();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x002a, code lost:
        r14.mNextRafs = null;
        completeRadioCapabilityTransaction();
        mtkLogd("skip switching because mtk_disable_cap_switch is true");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:150:0x021f, code lost:
        if (r5.equals("1") == false) goto L_0x0229;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:152:0x0227, code lost:
        if (r8.equals(com.mediatek.internal.telephony.MtkGsmCdmaPhone.ACT_TYPE_UTRAN) != false) goto L_0x022e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:154:0x022a, code lost:
        if (r11 == -1) goto L_0x0236;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:155:0x022c, code lost:
        if (r10 == r11) goto L_0x0236;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:156:0x022e, code lost:
        monitor-enter(r14);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:158:?, code lost:
        r14.mIsCapSwitching = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:159:0x0231, code lost:
        monitor-exit(r14);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0034, code lost:
        return 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:160:0x0232, code lost:
        return 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:165:0x023c, code lost:
        if (android.os.SystemProperties.getInt("ro.vendor.mtk_non_dsda_rsim_support", 0) != 1) goto L_0x0243;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:166:0x023e, code lost:
        if (r11 == -1) goto L_0x0243;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:167:0x0240, code lost:
        if (r11 != r10) goto L_0x0243;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:168:0x0242, code lost:
        return 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:170:0x024c, code lost:
        if (r14.mProxyControllerExt.isNeedSimSwitch(r10, r14.mPhones.length) != false) goto L_0x025b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:171:0x024e, code lost:
        logd("check sim card type and skip setRadioCapability");
        r14.mSetRafRetryCause = 0;
        r14.mNextRafs = null;
        completeRadioCapabilityTransaction();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:172:0x025a, code lost:
        return 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:174:0x025f, code lost:
        if (com.mediatek.internal.telephony.worldphone.WorldPhoneUtil.isWorldModeSupport() != false) goto L_0x026e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:176:0x0265, code lost:
        if (com.mediatek.internal.telephony.worldphone.WorldPhoneUtil.isWorldPhoneSupport() == false) goto L_0x026e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:177:0x0267, code lost:
        com.mediatek.internal.telephony.worldphone.WorldPhoneUtil.getWorldPhone().notifyRadioCapabilityChange(r10);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:178:0x026e, code lost:
        mtkLogd("checkRadioCapabilitySwitchConditions, do switch");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:179:0x0273, code lost:
        return 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x003b, code lost:
        if (android.os.SystemProperties.getInt("vendor.gsm.gcf.testmode", 0) != 2) goto L_0x0048;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:180:0x0274, code lost:
        monitor-enter(r14);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:182:?, code lost:
        r14.mIsCapSwitching = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:183:0x0277, code lost:
        monitor-exit(r14);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:185:0x027f, code lost:
        throw new java.lang.RuntimeException("input parameter is incorrect - no 3g phone");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:189:0x0283, code lost:
        mtkLogd("setCapability in calling, fail to set RAT for phones");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x003d, code lost:
        r14.mNextRafs = null;
        completeRadioCapabilityTransaction();
        mtkLogd("skip switching because FTA mode");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:190:0x028a, code lost:
        if (r14.mHasRegisterCallStateReceiver != false) goto L_0x028f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:191:0x028c, code lost:
        registerCallStateReceiver();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:192:0x028f, code lost:
        r14.mSetRafRetryCause = 3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:193:0x0291, code lost:
        monitor-enter(r14);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:195:?, code lost:
        r14.mIsCapSwitching = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:196:0x0294, code lost:
        monitor-exit(r14);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:197:0x0295, code lost:
        return 2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0047, code lost:
        return 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x004e, code lost:
        if (android.os.SystemProperties.getInt("persist.vendor.radio.simswitch.emmode", 1) != 0) goto L_0x005b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0050, code lost:
        r14.mNextRafs = null;
        completeRadioCapabilityTransaction();
        mtkLogd("skip switching because EM disable mode");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x005a, code lost:
        return 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x005f, code lost:
        if (com.mediatek.internal.telephony.worldphone.WorldPhoneUtil.isWorldPhoneSupport() == false) goto L_0x0090;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0065, code lost:
        if (com.mediatek.internal.telephony.worldphone.WorldPhoneUtil.isWorldModeSupport() != false) goto L_0x0083;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x006b, code lost:
        if (com.mediatek.internal.telephony.ModemSwitchHandler.isModemTypeSwitching() == false) goto L_0x0090;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x006d, code lost:
        logd("world mode switching.");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x0074, code lost:
        if (r14.mHasRegisterWorldModeReceiver != false) goto L_0x0079;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x0076, code lost:
        registerWorldModeReceiverFor90Modem();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x0079, code lost:
        r14.mSetRafRetryCause = 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x007b, code lost:
        monitor-enter(r14);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:?, code lost:
        r14.mIsCapSwitching = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x007e, code lost:
        monitor-exit(r14);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x007f, code lost:
        return 2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:0x0085, code lost:
        if (r14.mSetRafRetryCause != 1) goto L_0x0090;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:45:0x0089, code lost:
        if (r14.mHasRegisterWorldModeReceiver == false) goto L_0x0090;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:46:0x008b, code lost:
        unRegisterWorldModeReceiver();
        r14.mSetRafRetryCause = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:48:0x0099, code lost:
        if (android.telephony.TelephonyManager.getDefault().getCallState() != 0) goto L_0x0283;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:50:0x009f, code lost:
        if (isEccInProgress() == false) goto L_0x00a3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:52:0x00a5, code lost:
        if (r14.mSetRafRetryCause != 3) goto L_0x00b0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:54:0x00a9, code lost:
        if (r14.mHasRegisterCallStateReceiver == false) goto L_0x00b0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:55:0x00ab, code lost:
        unRegisterCallStateReceiver();
        r14.mSetRafRetryCause = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:56:0x00b0, code lost:
        r0 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:58:0x00b4, code lost:
        if (r0 >= r14.mPhones.length) goto L_0x0106;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:60:0x00bf, code lost:
        if (r14.mPhones[r0].isRadioAvailable() != false) goto L_0x00ee;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:61:0x00c1, code lost:
        r14.mSetRafRetryCause = 4;
        r14.mCi[r0].registerForAvailable(r14.mMtkHandler, 6, (java.lang.Object) null);
        mtkLogd("setCapability fail,Phone" + r0 + " is not available");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:62:0x00e6, code lost:
        monitor-enter(r14);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:64:?, code lost:
        r14.mIsCapSwitching = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:65:0x00e9, code lost:
        monitor-exit(r14);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:66:0x00ea, code lost:
        return 2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:71:0x00f0, code lost:
        if (r14.mSetRafRetryCause != 4) goto L_0x0103;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:72:0x00f2, code lost:
        r14.mCi[r0].unregisterForAvailable(r14.mMtkHandler);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:73:0x00ff, code lost:
        if (r0 != (r14.mPhones.length - 1)) goto L_0x0103;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:74:0x0101, code lost:
        r14.mSetRafRetryCause = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:75:0x0103, code lost:
        r0 = r0 + 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:76:0x0106, code lost:
        r0 = java.lang.Integer.valueOf(android.os.SystemProperties.get("persist.vendor.radio.simswitch", "1")).intValue();
        r7 = true;
        r8 = 0;
        r9 = false;
        r10 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:78:0x0120, code lost:
        if (r8 >= r15.length) goto L_0x0191;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:80:0x012a, code lost:
        if ((r15[r8].getRadioAccessFamily() & 1) <= 0) goto L_0x012f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:81:0x012c, code lost:
        r6 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:82:0x012f, code lost:
        r6 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:83:0x0130, code lost:
        if (r6 == false) goto L_0x017c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:84:0x0132, code lost:
        r1 = r15[r8].getPhoneId();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:85:0x013a, code lost:
        if (r1 != (r0 - 1)) goto L_0x0162;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:86:0x013c, code lost:
        r14.mSetRafRetryCause = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:87:0x013e, code lost:
        monitor-enter(r14);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:90:0x0141, code lost:
        if (r14.mNextRafs == null) goto L_0x0155;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:92:0x014b, code lost:
        if (r1 != r14.mNextRafs[r1].getPhoneId()) goto L_0x0155;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:93:0x014d, code lost:
        r14.mNextRafs = null;
        mtkLogd("no change, skip setRadioCapability");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:94:0x0155, code lost:
        mtkLogd("no change, skip setRadioCapability and trigger next");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:95:0x015a, code lost:
        monitor-exit(r14);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:96:0x015b, code lost:
        completeRadioCapabilityTransaction();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:97:0x015e, code lost:
        return 1;
     */
    private int checkRadioCapabilitySwitchConditions(RadioAccessFamily[] rafs) {
        synchronized (this) {
            this.mNextRafs = rafs;
            if (this.mIsCapSwitching) {
                mtkLogd("keep it and return,because capability swithing");
                this.mSetRafRetryCause = 2;
                return 1;
            }
            if (this.mSetRafRetryCause == 2) {
                mtkLogd("setCapability, mIsCapSwitching is not switching, can switch");
                this.mSetRafRetryCause = 0;
            }
            this.mIsCapSwitching = true;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onRetryWhenRadioAvailable(Message msg) {
        mtkLogd("onRetryWhenRadioAvailable,mSetRafRetryCause:" + this.mSetRafRetryCause);
        for (int i = 0; i < this.mPhones.length; i++) {
            if (RadioManager.isModemPowerOff(i)) {
                mtkLogd("onRetryWhenRadioAvailable, Phone" + i + " modem off");
                return;
            }
        }
        RadioAccessFamily[] radioAccessFamilyArr = this.mNextRafs;
        if (radioAccessFamilyArr != null && this.mSetRafRetryCause == 4) {
            try {
                setRadioCapability(radioAccessFamilyArr);
            } catch (RuntimeException e) {
                e.printStackTrace();
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendCapabilityFailBroadcast() {
        if (this.mContext != null) {
            this.mContext.sendBroadcastAsUser(new Intent("android.intent.action.ACTION_SET_RADIO_CAPABILITY_FAILED"), UserHandle.ALL);
        }
    }

    private void registerWorldModeReceiverFor90Modem() {
        if (this.mContext == null) {
            logd("registerWorldModeReceiverFor90Modem, context is null => return");
            return;
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction(ModemSwitchHandler.ACTION_MODEM_SWITCH_DONE);
        this.mContext.registerReceiver(this.mWorldModeReceiver, filter);
        this.mHasRegisterWorldModeReceiver = true;
    }

    private void unRegisterWorldModeReceiver() {
        if (this.mContext == null) {
            mtkLogd("unRegisterWorldModeReceiver, context is null => return");
            return;
        }
        this.mContext.unregisterReceiver(this.mWorldModeReceiver);
        this.mHasRegisterWorldModeReceiver = false;
    }

    private void registerCallStateReceiver() {
        if (this.mContext == null) {
            mtkLogd("registerCallStateReceiver, context is null => return");
            return;
        }
        IntentFilter filter = new IntentFilter("android.intent.action.PHONE_STATE");
        filter.addAction("android.intent.action.EMERGENCY_CALLBACK_MODE_CHANGED");
        this.mContext.registerReceiver(this.mCallStateReceiver, filter);
        this.mHasRegisterCallStateReceiver = true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void unRegisterCallStateReceiver() {
        if (this.mContext == null) {
            mtkLogd("unRegisterCallStateReceiver, context is null => return");
            return;
        }
        this.mContext.unregisterReceiver(this.mCallStateReceiver);
        this.mHasRegisterCallStateReceiver = false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isEccInProgress() {
        String value = SystemProperties.get("ril.cdma.inecmmode", "");
        boolean inEcm = value.contains("true");
        boolean isInEcc = false;
        ITelecomService tm = ITelecomService.Stub.asInterface(ServiceManager.getService("telecom"));
        if (tm != null) {
            try {
                isInEcc = tm.isInEmergencyCall();
            } catch (RemoteException e) {
                loge("Exception of isEccInProgress");
            }
        }
        logd("isEccInProgress, value:" + value + ", inEcm:" + inEcm + ", isInEcc:" + isInEcc);
        return inEcm || isInEcc;
    }
}
