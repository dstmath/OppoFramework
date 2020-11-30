package com.mediatek.internal.telephony.dataconnection;

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
import android.telephony.Rlog;
import android.telephony.ServiceState;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import com.android.internal.telephony.Call;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.PhoneSwitcher;
import com.android.internal.telephony.TelephonyDevController;
import com.mediatek.internal.telephony.IMtkTelephonyEx;
import com.mediatek.internal.telephony.MtkGsmCdmaCallTracker;
import com.mediatek.internal.telephony.MtkIccCardConstants;
import com.mediatek.internal.telephony.MtkPhoneSwitcher;
import com.mediatek.internal.telephony.datasub.SmartDataSwitchAssistant;
import com.mediatek.internal.telephony.imsphone.MtkImsPhoneCallTracker;
import com.mediatek.telephony.MtkTelephonyManagerEx;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MtkDcHelper extends Handler {
    private static final int DATA_CONFIG_MULTI_PS = 1;
    private static final boolean DBG = true;
    private static final int EVENT_CALL_ADDITIONAL_INFO = 70;
    private static final int EVENT_DSDA_STATE_CHANGED = 50;
    private static final int EVENT_ID_INTVL = 10;
    private static final int EVENT_NO_CS_CALL_AFTER_SRVCC = 40;
    private static final int EVENT_RIL_CONNECTED = 10;
    private static final int EVENT_SUBSCRIPTION_CHANGED = 0;
    private static final int EVENT_VOICE_CALL_ENDED = 30;
    private static final int EVENT_VOICE_CALL_OFFHOOK = 60;
    private static final int EVENT_VOICE_CALL_STARTED = 20;
    private static final String INVALID_ICCID = "N/A";
    private static final String LOG_TAG = "DcHelper";
    public static final boolean MTK_SRLTE_SUPPORT;
    public static final boolean MTK_SVLTE_SUPPORT = (SystemProperties.getInt(PROP_MTK_CDMA_LTE_MODE, 0) == 1);
    private static final int MT_CALL_MISSED = 2;
    private static final int MT_CALL_NUMREDIRECT = 3;
    private static final int MT_CALL_REJECTED = 1;
    private static final int MT_CALL_RQ = 4;
    private static String[] PROPERTY_ICCID_SIM = {"vendor.ril.iccid.sim1", "vendor.ril.iccid.sim2", "vendor.ril.iccid.sim3", "vendor.ril.iccid.sim4"};
    private static final String[] PROPERTY_RIL_FULL_UICC_TYPE = {"vendor.gsm.ril.fulluicctype", "vendor.gsm.ril.fulluicctype.2", "vendor.gsm.ril.fulluicctype.3", "vendor.gsm.ril.fulluicctype.4"};
    private static final String[] PROPERTY_RIL_TEST_SIM = {"vendor.gsm.sim.ril.testsim", "vendor.gsm.sim.ril.testsim.2", "vendor.gsm.sim.ril.testsim.3", "vendor.gsm.sim.ril.testsim.4"};
    private static final String PROP_DATA_CONFIG = "ro.vendor.mtk_data_config";
    private static final String PROP_MTK_CDMA_LTE_MODE = "ro.boot.opt_c2k_lte_mode";
    private static final String RIL_CDMA_DUALACT_SUPPORT = "vendor.ril.cdma.3g.dualact";
    private static final boolean VDBG = SystemProperties.get("ro.build.type").equals("eng");
    private static final Map<Operator, List> mOperatorMap = new HashMap<Operator, List>() {
        /* class com.mediatek.internal.telephony.dataconnection.MtkDcHelper.AnonymousClass1 */

        {
            put(Operator.OP156, Arrays.asList("23802", "23877"));
        }
    };
    private static MtkDcHelper sMtkDcHelper = null;
    protected final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        /* class com.mediatek.internal.telephony.dataconnection.MtkDcHelper.AnonymousClass2 */

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null && action.equals("android.intent.action.PHONE_STATE")) {
                String phoneState = intent.getStringExtra("state");
                if (phoneState.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
                    MtkDcHelper.logd("onPhoneStateChanged: phone is IDLE");
                    MtkDcHelper.this.mIsPhoneOffhook = false;
                } else if (phoneState.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
                    MtkDcHelper.logd("onPhoneStateChanged: phone is OFFHOOK");
                    MtkDcHelper.this.mRspHandler.obtainMessage(60).sendToTarget();
                }
            }
        }
    };
    private int mCallingPhoneId = -1;
    private Context mContext;
    private int mDsdaMode = 0;
    private boolean mGwsdDualSimStatus = false;
    private boolean mHasFetchMpsAttachSupport = false;
    private boolean mIsPhoneOffhook = false;
    private boolean mMpsAttachSupport = false;
    protected int mPhoneNum;
    private final BroadcastReceiver mPhoneSwitchReceiver = new BroadcastReceiver() {
        /* class com.mediatek.internal.telephony.dataconnection.MtkDcHelper.AnonymousClass4 */

        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.RADIO_TECHNOLOGY".equals(intent.getAction())) {
                int phoneId = intent.getIntExtra("phone", -1);
                MtkDcHelper.logd("mPhoneSwitchReceiver: phoneId = " + phoneId);
                if (MtkDcHelper.isCdma4GDualModeCard(phoneId) || MtkDcHelper.isCdma3GDualModeCard(phoneId)) {
                    MtkDcHelper.this.mPhones[phoneId].getDcTracker(1).update();
                }
            }
        }
    };
    protected Phone[] mPhones;
    private int mPrevCallingPhoneId = -1;
    private Handler mRspHandler = new Handler() {
        /* class com.mediatek.internal.telephony.dataconnection.MtkDcHelper.AnonymousClass3 */

        public void handleMessage(Message msg) {
            int phoneId = msg.what % 10;
            int eventId = msg.what - phoneId;
            int restCallingPhoneId = -1;
            if (eventId == 10) {
                MtkDcHelper.logd("EVENT_PHONE" + phoneId + "_EVENT_RIL_CONNECTED");
                MtkDcHelper.this.onCheckIfRetriggerDataAllowed(phoneId);
            } else if (eventId != 20) {
                boolean z = false;
                if (eventId != 30) {
                    if (eventId == 40) {
                        MtkDcHelper.logd("Got 'no CS calls after SRVCC' notification, tunnel it to VOICE_CALL_END");
                        MtkDcHelper.this.mSrvccState = Call.SrvccState.NONE;
                        MtkPhoneSwitcher phoneSwitcher = MtkPhoneSwitcher.getInstance();
                        if (phoneSwitcher != null) {
                            phoneSwitcher.sendEmptyMessage(109);
                        }
                    } else if (eventId == 50) {
                        AsyncResult ar = (AsyncResult) msg.obj;
                        if (!(ar == null || ar.result == null)) {
                            MtkDcHelper.this.mDsdaMode = ((Integer) ar.result).intValue();
                            MtkDcHelper.logd("mDsdaMode = " + MtkDcHelper.this.mDsdaMode);
                            if (MtkDcHelper.this.mCallingPhoneId != -1) {
                                for (int i = 0; i < MtkDcHelper.this.mPhoneNum; i++) {
                                    MtkDcHelper.this.mPhones[i].getDcTracker(1).onDsdaStateChanged();
                                }
                            }
                            SmartDataSwitchAssistant mSmartDataSwitchAssistant = SmartDataSwitchAssistant.getInstance();
                            if (mSmartDataSwitchAssistant != null) {
                                mSmartDataSwitchAssistant.onDsdaStateChanged();
                                return;
                            }
                            return;
                        }
                        return;
                    } else if (eventId != 60) {
                        if (eventId != 70) {
                            MtkDcHelper.logd("Unhandled message with number: " + msg.what);
                            return;
                        }
                        String[] callAdditionalInfo = (String[]) ((AsyncResult) msg.obj).result;
                        if (Integer.parseInt(callAdditionalInfo[0]) == 4) {
                            int mtCallRq = Integer.parseInt(callAdditionalInfo[1]);
                            MtkDcHelper mtkDcHelper = MtkDcHelper.this;
                            if (mtCallRq == 1) {
                                z = true;
                            }
                            mtkDcHelper.mGwsdDualSimStatus = z;
                            MtkDcHelper.logd("MT_CALL_RQ, mtCallRq = " + mtCallRq + ", mGwsdDualSimStatus = " + MtkDcHelper.this.mGwsdDualSimStatus);
                            return;
                        }
                        return;
                    } else if (((MtkDcHelper) MtkDcHelper.this).mCallingPhoneId != -1) {
                        MtkDcHelper.this.mIsPhoneOffhook = true;
                        MtkDcHelper.logd("Voice Call OffHook, re-evaluate call start");
                        MtkDcHelper.this.onVoiceCallStarted();
                        return;
                    } else {
                        return;
                    }
                }
                MtkDcHelper.this.mIsPhoneOffhook = false;
                Call.SrvccState preSrvccState = MtkDcHelper.this.mSrvccState;
                AsyncResult ar2 = (AsyncResult) msg.obj;
                if (ar2 == null || ar2.result == null) {
                    MtkDcHelper.this.mSrvccState = Call.SrvccState.NONE;
                } else {
                    MtkDcHelper.this.mSrvccState = (Call.SrvccState) ar2.result;
                }
                MtkDcHelper.logd("mSrvccState = " + MtkDcHelper.this.mSrvccState);
                if (!MtkDcHelper.this.isInSRVCC() || preSrvccState == Call.SrvccState.NONE) {
                    MtkDcHelper.logd("Voice Call Ended, mCallingPhoneId = " + MtkDcHelper.this.mCallingPhoneId);
                    if (MtkDcHelper.MTK_SVLTE_SUPPORT && MtkDcHelper.this.mPrevCallingPhoneId != -1) {
                        if (phoneId == MtkDcHelper.this.mCallingPhoneId) {
                            restCallingPhoneId = MtkDcHelper.this.mPrevCallingPhoneId;
                        } else {
                            restCallingPhoneId = MtkDcHelper.this.mCallingPhoneId;
                        }
                        MtkDcHelper.this.mCallingPhoneId = restCallingPhoneId;
                        MtkDcHelper.this.mPrevCallingPhoneId = -1;
                        MtkDcHelper.logd("SVLTE Voice Call2 Ended, mCallingPhoneId = " + MtkDcHelper.this.mCallingPhoneId);
                    }
                    MtkDcHelper.this.onVoiceCallEnded();
                    MtkDcHelper.this.mCallingPhoneId = -1;
                    if (MtkDcHelper.MTK_SVLTE_SUPPORT && restCallingPhoneId != -1) {
                        MtkDcHelper.this.mCallingPhoneId = restCallingPhoneId;
                        MtkDcHelper.logd("SVLTE Voice Call Ended, restore first mCallingPhoneId = " + MtkDcHelper.this.mCallingPhoneId);
                    }
                }
            } else if (!MtkDcHelper.this.isInSRVCC()) {
                if (MtkDcHelper.MTK_SVLTE_SUPPORT && MtkDcHelper.this.mCallingPhoneId != -1) {
                    MtkDcHelper mtkDcHelper2 = MtkDcHelper.this;
                    mtkDcHelper2.mPrevCallingPhoneId = mtkDcHelper2.mCallingPhoneId;
                    MtkDcHelper.logd("SVLTE Voice Call2 Started, save first mPrevCallingPhoneId = " + MtkDcHelper.this.mPrevCallingPhoneId);
                }
                MtkDcHelper.this.mCallingPhoneId = phoneId;
                MtkDcHelper.logd("Voice Call Started, mCallingPhoneId = " + MtkDcHelper.this.mCallingPhoneId);
                MtkDcHelper.this.onVoiceCallStarted();
            }
        }
    };
    private Call.SrvccState mSrvccState = Call.SrvccState.NONE;
    private TelephonyDevController mTelDevController = TelephonyDevController.getInstance();

    public enum Operator {
        OP156
    }

    static {
        boolean z = false;
        if (SystemProperties.getInt(PROP_MTK_CDMA_LTE_MODE, 0) == 2) {
            z = true;
        }
        MTK_SRLTE_SUPPORT = z;
    }

    protected MtkDcHelper(Context context, Phone[] phones) {
        this.mContext = context;
        this.mPhones = phones;
        this.mPhoneNum = phones.length;
        registerEvents();
    }

    public void dispose() {
        logd("MtkDcHelper.dispose");
        unregisterEvents();
    }

    public static MtkDcHelper makeMtkDcHelper(Context context, Phone[] phones) {
        if (context == null || phones == null) {
            throw new RuntimeException("param is null");
        }
        if (sMtkDcHelper == null) {
            logd("makeMtkDcHelper: phones.length=" + phones.length);
            sMtkDcHelper = new MtkDcHelper(context, phones);
        }
        logd("makesMtkDcHelper: X sMtkDcHelper =" + sMtkDcHelper);
        return sMtkDcHelper;
    }

    public static MtkDcHelper getInstance() {
        MtkDcHelper mtkDcHelper = sMtkDcHelper;
        if (mtkDcHelper != null) {
            return mtkDcHelper;
        }
        throw new RuntimeException("Should not be called before makesMtkDcHelper");
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onCheckIfRetriggerDataAllowed(int phoneId) {
        logd("onCheckIfRetriggerDataAllowed: retriggerDataAllowed: mPhone[" + phoneId + "]");
        if (MtkPhoneSwitcher.getInstance() != null) {
            MtkPhoneSwitcher.getInstance().onRadioCapChanged(phoneId);
        }
    }

    public boolean isOperatorMccMnc(Operator opt, int phoneId) {
        String mccMnc = TelephonyManager.getDefault().getSimOperatorNumericForPhone(phoneId);
        boolean bMatched = mOperatorMap.get(opt).contains(mccMnc);
        logd("isOperatorMccMnc: mccmnc=" + mccMnc + ", bMatched=" + bMatched);
        return bMatched;
    }

    private void registerEvents() {
        logd("registerEvents");
        for (int i = 0; i < this.mPhoneNum; i++) {
            this.mPhones[i].mCi.registerForRilConnected(this.mRspHandler, i + 10, (Object) null);
            this.mPhones[i].getCallTracker().registerForVoiceCallStarted(this.mRspHandler, i + 20, (Object) null);
            this.mPhones[i].getCallTracker().registerForVoiceCallEnded(this.mRspHandler, i + 30, (Object) null);
            this.mPhones[i].mCi.registerForDsdaStateChanged(this.mRspHandler, i + 50, null);
            this.mPhones[i].mCi.registerForCallAdditionalInfo(this.mRspHandler, i + 70, null);
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.RADIO_TECHNOLOGY");
        this.mContext.registerReceiver(this.mPhoneSwitchReceiver, filter);
        IntentFilter filter2 = new IntentFilter();
        filter2.addAction("android.intent.action.PHONE_STATE");
        this.mContext.registerReceiver(this.mBroadcastReceiver, filter2);
        logd("registered phone change event.");
    }

    public void registerImsEvents(int phoneId) {
        if (!SubscriptionManager.isValidPhoneId(phoneId)) {
            logd("registerImsEvents, invalid phoneId");
            return;
        }
        logd("registerImsEvents, phoneId = " + phoneId);
        Phone imsPhone = this.mPhones[phoneId].getImsPhone();
        if (imsPhone != null) {
            MtkImsPhoneCallTracker imsCt = imsPhone.getCallTracker();
            imsCt.registerForVoiceCallStarted(this.mRspHandler, phoneId + 20, null);
            imsCt.registerForVoiceCallEnded(this.mRspHandler, phoneId + 30, null);
            imsCt.registerForCallsDisconnectedDuringSrvcc(this.mRspHandler, phoneId + 40, null);
            return;
        }
        logd("Not register IMS phone calling state yet.");
    }

    private void unregisterEvents() {
        logd("unregisterEvents");
        for (int i = 0; i < this.mPhoneNum; i++) {
            this.mPhones[i].getCallTracker().unregisterForVoiceCallStarted(this.mRspHandler);
            this.mPhones[i].getCallTracker().unregisterForVoiceCallEnded(this.mRspHandler);
            this.mPhones[i].mCi.unregisterForDsdaStateChanged(this.mRspHandler);
        }
    }

    public void unregisterImsEvents(int phoneId) {
        if (!SubscriptionManager.isValidPhoneId(phoneId)) {
            logd("unregisterImsEvents, invalid phoneId");
            return;
        }
        logd("unregisterImsEvents, phoneId = " + phoneId);
        Phone imsPhone = this.mPhones[phoneId].getImsPhone();
        if (imsPhone != null) {
            MtkImsPhoneCallTracker imsCt = imsPhone.getCallTracker();
            imsCt.unregisterForVoiceCallStarted(this.mRspHandler);
            imsCt.unregisterForVoiceCallEnded(this.mRspHandler);
            imsCt.unregisterForCallsDisconnectedDuringSrvcc(this.mRspHandler);
            return;
        }
        logd("Not unregister IMS phone calling state yet.");
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onVoiceCallStarted() {
        for (int i = 0; i < this.mPhoneNum; i++) {
            logd("onVoiceCallStarted: mPhone[ " + i + "]");
            this.mPhones[i].getDcTracker(1).onVoiceCallStartedEx();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onVoiceCallEnded() {
        for (int i = 0; i < this.mPhoneNum; i++) {
            logd("onVoiceCallEnded: mPhone[ " + i + "]");
            this.mPhones[i].getDcTracker(1).onVoiceCallEndedEx();
        }
    }

    public boolean isDataSupportConcurrent(int phoneId) {
        boolean isDataAvailable;
        boolean isConcurrent;
        ArrayList<Integer> callingPhoneIdList = new ArrayList<>();
        for (int i = 0; i < this.mPhoneNum; i++) {
            if (this.mPhones[i].getState() != PhoneConstants.State.IDLE) {
                callingPhoneIdList.add(Integer.valueOf(i));
            }
        }
        boolean z = true;
        if (callingPhoneIdList.size() == 0) {
            logd("isDataSupportConcurrent: no calling phone!");
            return true;
        } else if (MTK_SVLTE_SUPPORT && callingPhoneIdList.size() > 1) {
            logd("isDataSupportConcurrent: SVLTE and >1 calling phone.");
            return this.mPhones[phoneId].getServiceStateTracker().isConcurrentVoiceAndDataAllowed();
        } else if (phoneId == callingPhoneIdList.get(0).intValue()) {
            boolean inSrvcc = false;
            MtkGsmCdmaCallTracker ct = this.mPhones[phoneId].getCallTracker();
            Phone imsPhone = this.mPhones[phoneId].getImsPhone();
            boolean inPsEcc = imsPhone == null ? false : imsPhone.isInEmergencyCall();
            PhoneConstants.State csCallState = PhoneConstants.State.IDLE;
            if (ct != null) {
                if (ct.getHandoverConnectionSize() == 0) {
                    z = false;
                }
                inSrvcc = z;
                csCallState = ct.getState();
            }
            if (inPsEcc || inSrvcc || csCallState != PhoneConstants.State.IDLE) {
                isConcurrent = this.mPhones[phoneId].getServiceStateTracker().isConcurrentVoiceAndDataAllowed();
            } else {
                isConcurrent = true;
            }
            logd("isDataSupportConcurrent: (voice/data on the same phone) isConcurrent = " + isConcurrent + ", phoneId = " + phoneId + ", callingPhoneId = " + callingPhoneIdList.get(0) + ", inPsEcc = " + inPsEcc + ", inSrvcc = " + inSrvcc + ", csCallState = " + csCallState);
            return isConcurrent;
        } else if (this.mDsdaMode == 1) {
            logd("DSDA mode, support concurrent");
            return true;
        } else {
            MtkTelephonyManagerEx tmEx = MtkTelephonyManagerEx.getDefault();
            if (tmEx != null && (isDataAvailable = tmEx.isDataAvailableForGwsdDualSim(this.mGwsdDualSimStatus))) {
                logd("isDataAvailable: " + isDataAvailable + ", mGwsdDualSimStatus: " + this.mGwsdDualSimStatus);
                return true;
            } else if (MTK_SRLTE_SUPPORT) {
                logd("isDataSupportConcurrent: support SRLTE ");
                return false;
            } else if (MTK_SVLTE_SUPPORT) {
                int phoneType = this.mPhones[callingPhoneIdList.get(0).intValue()].getPhoneType();
                if (phoneType == 2) {
                    return true;
                }
                int rilRat = this.mPhones[phoneId].getServiceState().getRilDataRadioTechnology();
                logd("isDataSupportConcurrent: support SVLTE RilRat = " + rilRat + "calling phoneType: " + phoneType);
                return ServiceState.isCdma(rilRat);
            } else {
                logd("isDataSupportConcurrent: not SRLTE or SVLTE ");
                return false;
            }
        }
    }

    public boolean isAllCallingStateIdle() {
        PhoneConstants.State[] state = new PhoneConstants.State[this.mPhoneNum];
        boolean allCallingState = false;
        int i = 0;
        while (true) {
            if (i >= this.mPhoneNum) {
                break;
            }
            state[i] = this.mPhones[i].getState();
            if (state[i] == null || state[i] != PhoneConstants.State.IDLE) {
                allCallingState = false;
            } else {
                allCallingState = true;
                i++;
            }
        }
        allCallingState = false;
        if (!allCallingState && VDBG) {
            for (int i2 = 0; i2 < this.mPhoneNum; i2++) {
                logd("isAllCallingStateIdle: state[" + i2 + "]=" + state[i2] + " allCallingState = " + allCallingState);
            }
        }
        return allCallingState;
    }

    public boolean isWifiCallingEnabled() {
        boolean isWifiCallingEnabled = false;
        int callingPhoneId = this.mCallingPhoneId;
        int callingPhoneId2 = this.mPrevCallingPhoneId;
        IMtkTelephonyEx telephonyEx = IMtkTelephonyEx.Stub.asInterface(ServiceManager.getService("phoneEx"));
        if (telephonyEx == null) {
            return false;
        }
        try {
            if (SubscriptionManager.isValidPhoneId(callingPhoneId)) {
                isWifiCallingEnabled = telephonyEx.isWifiCallingEnabled(this.mPhones[callingPhoneId].getSubId());
            }
            if (!MTK_SVLTE_SUPPORT || isWifiCallingEnabled || !SubscriptionManager.isValidPhoneId(callingPhoneId2)) {
                return isWifiCallingEnabled;
            }
            return telephonyEx.isWifiCallingEnabled(this.mPhones[callingPhoneId2].getSubId());
        } catch (RemoteException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public static boolean isImsOrEmergencyApn(String[] apnTypes) {
        if (apnTypes == null) {
            loge("isImsOrEmergencyApn: apnTypes is null");
            return false;
        } else if (apnTypes.length == 0) {
            return false;
        } else {
            for (String type : apnTypes) {
                if (!("ims".equals(type) || "emergency".equals(type))) {
                    return false;
                }
            }
            return true;
        }
    }

    public boolean isDataAllowedForConcurrent(int phoneId) {
        if (!SubscriptionManager.isValidPhoneId(phoneId)) {
            logd("isDataAllowedForConcurrent: invalid calling phone id");
            return false;
        } else if (isAllCallingStateIdle() || isDataSupportConcurrent(phoneId)) {
            return true;
        } else {
            if (!isWifiCallingEnabled() || this.mPhones[phoneId].isInEmergencyCall()) {
                return false;
            }
            return true;
        }
    }

    public static boolean hasVsimApn(String[] apnTypes) {
        if (apnTypes == null) {
            loge("hasVsimApn: apnTypes is null");
            return false;
        } else if (apnTypes.length == 0) {
            return false;
        } else {
            for (String type : apnTypes) {
                if (TextUtils.equals("vsim", type)) {
                    return true;
                }
            }
            return false;
        }
    }

    public boolean isSimInserted(int phoneId) {
        logd("isSimInserted:phoneId =" + phoneId);
        String iccid = SystemProperties.get(PROPERTY_ICCID_SIM[phoneId], "");
        return !TextUtils.isEmpty(iccid) && !"N/A".equals(iccid);
    }

    public boolean isTestIccCard(int phoneId) {
        String testCard = SystemProperties.get(PROPERTY_RIL_TEST_SIM[phoneId], "");
        if (VDBG) {
            logd("isTestIccCard: phoneId id = " + phoneId + ", iccType = " + testCard);
        }
        return testCard != null && testCard.equals("1");
    }

    public boolean isMultiPsAttachSupport() {
        if (!this.mHasFetchMpsAttachSupport) {
            if ((SystemProperties.getInt(PROP_DATA_CONFIG, 0) & 1) == 1) {
                this.mMpsAttachSupport = true;
            }
            this.mHasFetchMpsAttachSupport = true;
        }
        return this.mMpsAttachSupport;
    }

    public boolean hasMdAutoSetupImsCapability() {
        TelephonyDevController telephonyDevController = this.mTelDevController;
        if (telephonyDevController == null || telephonyDevController.getModem(0) == null || !this.mTelDevController.getModem(0).hasMdAutoSetupImsCapability()) {
            logd("hasMdAutoSetupImsCapability: false");
            return false;
        }
        logd("hasMdAutoSetupImsCapability: true");
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isInSRVCC() {
        return this.mSrvccState == Call.SrvccState.COMPLETED;
    }

    public static boolean isCdmaDualActivationSupport() {
        return SystemProperties.get(RIL_CDMA_DUALACT_SUPPORT).equals("1");
    }

    public static boolean isCdma4GDualModeCard(int phoneId) {
        if (phoneId < 0 || phoneId >= TelephonyManager.getDefault().getPhoneCount()) {
            logd("isCdma4GDualModeCard invalid phoneId = " + phoneId);
            return false;
        }
        MtkIccCardConstants.CardType cardType = MtkTelephonyManagerEx.getDefault().getCdmaCardType(phoneId);
        if (cardType == MtkIccCardConstants.CardType.CT_4G_UICC_CARD || cardType == MtkIccCardConstants.CardType.NOT_CT_UICC_CARD) {
            return true;
        }
        return false;
    }

    public static boolean isCdma3GDualModeCard(int phoneId) {
        if (phoneId < 0 || phoneId >= TelephonyManager.getDefault().getPhoneCount()) {
            logd("isCdma3GDualModeCard invalid phoneId = " + phoneId);
            return false;
        }
        MtkIccCardConstants.CardType cardType = MtkTelephonyManagerEx.getDefault().getCdmaCardType(phoneId);
        if (cardType == MtkIccCardConstants.CardType.UIM_SIM_CARD || cardType == MtkIccCardConstants.CardType.CT_UIM_SIM_CARD) {
            return true;
        }
        return false;
    }

    public static boolean isCdma3GCard(int phoneId) {
        if (phoneId < 0 || phoneId >= TelephonyManager.getDefault().getPhoneCount()) {
            logd("isCdma3GCard invalid phoneId = " + phoneId);
            return false;
        }
        MtkIccCardConstants.CardType cardType = MtkTelephonyManagerEx.getDefault().getCdmaCardType(phoneId);
        if (cardType == MtkIccCardConstants.CardType.UIM_CARD || cardType == MtkIccCardConstants.CardType.CT_3G_UIM_CARD) {
            return true;
        }
        return false;
    }

    public static int decodeRat(int param) {
        if (param < 0) {
            return -1;
        }
        return (param / 1000) + 1;
    }

    public boolean isSimMeLockAllowed(int phoneId) {
        if (MtkPhoneSwitcher.getInstance() == null || !MtkPhoneSwitcher.getInstance().getSimLockMode() || MtkPhoneSwitcher.getInstance().getPsAllowedByPhoneId(phoneId)) {
            return true;
        }
        return false;
    }

    public int getDsdaMode() {
        return this.mDsdaMode;
    }

    public static boolean isPreferredDataPhone(Phone phone) {
        int preferredDataPhoneId = PhoneSwitcher.getInstance() != null ? PhoneSwitcher.getInstance().getPreferredDataPhoneId() : -1;
        int curPhoneId = phone.getPhoneId();
        if (preferredDataPhoneId == curPhoneId) {
            return true;
        }
        logd("Current phone is not preferred phone: curPhoneId = " + curPhoneId + ", preferredDataPhoneId = " + preferredDataPhoneId);
        return false;
    }

    protected static void logv(String s) {
        Rlog.v(LOG_TAG, s);
    }

    protected static void logd(String s) {
        Rlog.d(LOG_TAG, s);
    }

    protected static void loge(String s) {
        Rlog.e(LOG_TAG, s);
    }

    protected static void logi(String s) {
        Rlog.i(LOG_TAG, s);
    }
}
