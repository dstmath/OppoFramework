package com.oppo.internal.telephony;

import android.os.AsyncResult;
import android.os.Binder;
import android.os.Handler;
import android.os.Message;
import android.os.PersistableBundle;
import android.os.SystemProperties;
import android.telephony.CarrierConfigManager;
import android.telephony.Rlog;
import android.telephony.ServiceState;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import com.android.internal.telephony.Call;
import com.android.internal.telephony.GsmCdmaPhone;
import com.android.internal.telephony.IOppoGsmCdmaPhone;
import com.android.internal.telephony.OemConstant;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.PhoneFactory;
import com.android.internal.telephony.ServiceStateTracker;
import com.android.internal.telephony.imsphone.ImsPhone;
import com.android.internal.telephony.uicc.IccCardApplicationStatus;
import com.android.internal.telephony.uicc.IccCardStatus;
import com.android.internal.telephony.uicc.IccRecords;
import com.android.internal.telephony.uicc.UiccCard;
import com.android.internal.telephony.uicc.UiccCardApplication;
import com.android.internal.telephony.uicc.UiccController;
import com.android.internal.telephony.util.ReflectionHelper;
import com.oppo.internal.telephony.dataconnection.OppoFdManager;
import com.oppo.internal.telephony.utils.OppoPhoneUtil;
import com.oppo.internal.telephony.utils.OppoPolicyController;
import java.util.concurrent.atomic.AtomicReference;

public class OppoGsmCdmaPhoneReference implements IOppoGsmCdmaPhone {
    protected static final String[][] DEFAULT_VM_BY_MCCMNC = {new String[]{"50501", "+61101"}, new String[]{"50571", "+61101"}, new String[]{"50572", "+61101"}};
    protected static final int EVENT_GET_LTEWIFI_COEXIST = 1;
    protected static final int EVENT_SAVE_LTEWIFI_CONFIG = 3;
    protected static final int EVENT_SET_LTEWIFI_CONFIG = 2;
    private String LOG_TAG = "OppoGsmCdmaPhone";
    private final int MAXRETRYTIMES = 3;
    private OppoFdManager mFdManager;
    private Handler mHandler = new Handler() {
        /* class com.oppo.internal.telephony.OppoGsmCdmaPhoneReference.AnonymousClass1 */

        public void handleMessage(Message msg) {
            int i = msg.what;
            boolean z = false;
            boolean enabled1 = true;
            if (i == 1) {
                AsyncResult ar = (AsyncResult) msg.obj;
                if (ar.exception != null) {
                    Rlog.e(OppoGsmCdmaPhoneReference.this.LOG_TAG, "get lte_wifi_coexist fail");
                    return;
                }
                Rlog.e(OppoGsmCdmaPhoneReference.this.LOG_TAG, "get lte_wifi_coexist...");
                try {
                    String[] coexist = (String[]) ar.result;
                    if (SystemProperties.getInt("persist.sys.ltewificoexist", 1) != 1) {
                        enabled1 = false;
                    }
                    boolean enabled2 = true;
                    if (!coexist[0].equals("+ESBP: 0")) {
                        enabled2 = false;
                    }
                    Rlog.d(OppoGsmCdmaPhoneReference.this.LOG_TAG, "coexist[0]:" + coexist[0] + "enabled1:" + enabled1 + "enabled2:" + enabled2);
                    if (enabled1 != enabled2) {
                        OppoGsmCdmaPhoneReference.this.updateLteWifiCoexist(enabled1);
                    }
                } catch (Exception ex) {
                    Rlog.e(OppoGsmCdmaPhoneReference.this.LOG_TAG, "get lte_wifi_coexist", ex);
                }
            } else if (i != 2) {
                if (i == 3) {
                    if (((AsyncResult) msg.obj).exception != null) {
                        Rlog.e(OppoGsmCdmaPhoneReference.this.LOG_TAG, "save lte_wifi_coexist fail");
                        OppoGsmCdmaPhoneReference.access$308(OppoGsmCdmaPhoneReference.this);
                        if (OppoGsmCdmaPhoneReference.this.retrySaveConfig < 3) {
                            OppoGsmCdmaPhoneReference oppoGsmCdmaPhoneReference = OppoGsmCdmaPhoneReference.this;
                            if (SystemProperties.getInt("persist.sys.ltewificoexist", 1) == 1) {
                                z = true;
                            }
                            oppoGsmCdmaPhoneReference.saveLteWifiConfig(z);
                            return;
                        }
                        return;
                    }
                    OppoGsmCdmaPhoneReference.this.retrySaveConfig = 3;
                    Rlog.e(OppoGsmCdmaPhoneReference.this.LOG_TAG, "save lte_wifi_coexist success");
                }
            } else if (((AsyncResult) msg.obj).exception != null) {
                Rlog.e(OppoGsmCdmaPhoneReference.this.LOG_TAG, "set lte_wifi_coexist fail");
                OppoGsmCdmaPhoneReference.access$108(OppoGsmCdmaPhoneReference.this);
                if (OppoGsmCdmaPhoneReference.this.retrySetConfig < 3) {
                    OppoGsmCdmaPhoneReference oppoGsmCdmaPhoneReference2 = OppoGsmCdmaPhoneReference.this;
                    if (SystemProperties.getInt("persist.sys.ltewificoexist", 1) == 1) {
                        z = true;
                    }
                    oppoGsmCdmaPhoneReference2.setLteWifiConfig(z);
                }
            } else {
                OppoGsmCdmaPhoneReference.this.retrySetConfig = 3;
                Rlog.e(OppoGsmCdmaPhoneReference.this.LOG_TAG, "set lte_wifi_coexist success");
            }
        }
    };
    private GsmCdmaPhone mPhone;
    private int mPhoneId = 0;
    private TelephonyManager mTelephonyManager;
    private int retrySaveConfig = 3;
    private int retrySetConfig = 3;

    static /* synthetic */ int access$108(OppoGsmCdmaPhoneReference x0) {
        int i = x0.retrySetConfig;
        x0.retrySetConfig = i + 1;
        return i;
    }

    static /* synthetic */ int access$308(OppoGsmCdmaPhoneReference x0) {
        int i = x0.retrySaveConfig;
        x0.retrySaveConfig = i + 1;
        return i;
    }

    public OppoGsmCdmaPhoneReference(GsmCdmaPhone phone) {
        this.mPhone = phone;
        this.mPhoneId = this.mPhone.getPhoneId();
        this.LOG_TAG += "/" + this.mPhoneId;
        this.mTelephonyManager = (TelephonyManager) this.mPhone.getContext().getSystemService("phone");
        OppoCallStateMonitor.getInstance(phone.getContext()).initGsmCdma(phone);
        this.mFdManager = OppoFdManager.getInstance(phone);
    }

    public boolean handleCalloutControl(boolean isEmergencyNumber) {
        logd("handleCalloutControl");
        if (OppoPolicyController.isCallOutEnable(this.mPhone, isEmergencyNumber)) {
            return false;
        }
        logd("ctmm vo block");
        GsmCdmaPhone gsmCdmaPhone = this.mPhone;
        if (gsmCdmaPhone == null || !OppoPolicyController.getCallOutRestricted(gsmCdmaPhone.getPhoneId())) {
            return true;
        }
        OppoSimlockManagerImpl.broadcastCalloutRestrict(this.mPhone.getContext(), this.mPhone.getPhoneId());
        return true;
    }

    public boolean handleImsForUtCheck(boolean useImsForUt, String dialString, AtomicReference<UiccCardApplication> atomicReference) {
        return useImsForUt;
    }

    public boolean isOemInCall() {
        return OppoCallStateMonitor.getInstance(this.mPhone.getContext()).isCurrPhoneInCall(this.mPhone.getPhoneId());
    }

    public boolean isManualSelectNetworksAllowed(ServiceStateTracker mSST) {
        if (mSST == null || mSST.mSS == null) {
            loge("isManualSelectNetworkAllowed[false]: mSST/ mSST.mSS is null");
            return false;
        }
        Phone oPhone = PhoneFactory.getPhone(1 - this.mPhone.getPhoneId());
        boolean isOtherPhoneIncall = (oPhone == null || oPhone.getState() == PhoneConstants.State.IDLE) ? false : true;
        boolean isMPhoneIncall = this.mPhone.getState() != PhoneConstants.State.IDLE;
        if (isOtherPhoneIncall || isMPhoneIncall) {
            return false;
        }
        boolean roaming = mSST.mSS.getRoaming();
        Object NetworkType = ReflectionHelper.callMethod(this.mPhone.mCi, "com.android.internal.telephony.CommandsInterface", "oppoGetPreferredNetworkType", (Class[]) null, (Object[]) null);
        int preferredNetworkType = Integer.parseInt(NetworkType == null ? "" : NetworkType.toString());
        if ((!this.mPhone.isPhoneTypeCdmaLte() || 11 != preferredNetworkType) && ((!this.mPhone.isPhoneTypeCdma() || 11 != preferredNetworkType) && ((!OppoPhoneUtil.isCtCard((Phone) this.mPhone) || !roaming) && (!this.mPhone.isPhoneTypeGsm() || OppoPhoneUtil.isCtCard((Phone) this.mPhone))))) {
            loge("isManualSelectNetworkAllowed[false]: ct card:=" + OppoPhoneUtil.isCtCard((Phone) this.mPhone) + " roaming:" + roaming);
            return false;
        }
        loge("isManualSelectNetworkAllowed[true]. with ct card:=" + OppoPhoneUtil.isCtCard((Phone) this.mPhone));
        return true;
    }

    public String oemGetFullIccSerialNumber(IccRecords mIccRecord, UiccController mUiccController) {
        String str = null;
        String ret = mIccRecord != null ? mIccRecord.getFullIccId() : null;
        if (!TextUtils.isEmpty(ret)) {
            return ret;
        }
        if (!this.mPhone.isPhoneTypeGsm() && mUiccController != null) {
            IccRecords r = mUiccController.getIccRecords(this.mPhone.getPhoneId(), 1);
            if (r != null) {
                str = r.getFullIccId();
            }
            ret = str;
            if (!TextUtils.isEmpty(ret)) {
                return ret;
            }
        }
        long identity = Binder.clearCallingIdentity();
        try {
            SubscriptionInfo subInfo = SubscriptionManager.from(this.mPhone.getContext()).getActiveSubscriptionInfo(this.mPhone.getSubId());
            if (subInfo != null) {
                ret = subInfo.getIccId();
            }
            return ret;
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    public String colorGetIccCardType() {
        String cardType;
        UiccCard card = this.mPhone.getUiccCard();
        if (card == null || card.getCardState() == IccCardStatus.CardState.CARDSTATE_ABSENT) {
            logd("[colorGetIccCardType]Card is null or absent");
            return "SIM";
        }
        boolean Is3gCard = false;
        boolean IsCsim = false;
        boolean IsRuim = false;
        for (int i = 0; i < card.getNumApplications(); i++) {
            UiccCardApplication app = card.getApplicationIndex(i);
            if (app != null) {
                IccCardApplicationStatus.AppType type = app.getType();
                if (type == IccCardApplicationStatus.AppType.APPTYPE_CSIM || type == IccCardApplicationStatus.AppType.APPTYPE_USIM || type == IccCardApplicationStatus.AppType.APPTYPE_ISIM) {
                    logd("Card is 3G");
                    Is3gCard = true;
                }
                if (type == IccCardApplicationStatus.AppType.APPTYPE_CSIM) {
                    IsCsim = true;
                }
                if (type == IccCardApplicationStatus.AppType.APPTYPE_RUIM) {
                    IsRuim = true;
                }
            }
        }
        if (true == IsCsim) {
            cardType = "CSIM";
        } else if (true == IsRuim) {
            cardType = "RUIM";
        } else if (true == Is3gCard) {
            cardType = "USIM";
        } else {
            cardType = "SIM";
        }
        logd("[colorGetIccCardType]-->" + cardType);
        return cardType;
    }

    public void resetImsSS(Phone imsPhone) {
        ServiceState imsSS;
        if (imsPhone != null && (imsSS = imsPhone.getServiceState()) != null && imsSS.getState() == 3) {
            imsSS.setStateOutOfService();
            logd("handleRadioOn set imsphone servicestate oos");
        }
    }

    public boolean isUssdEnabledInVolteCall() {
        PersistableBundle b = null;
        CarrierConfigManager configManager = (CarrierConfigManager) this.mPhone.getContext().getSystemService("carrier_config");
        if (configManager != null) {
            b = configManager.getConfigForSubId(this.mPhone.getSubId());
        }
        if (b == null || !b.getBoolean("carrier_allow_ussd_in_volte_call")) {
            logd("Can not execute ussd code during volte call!");
            return false;
        }
        logd("carrier_allow_ussd_in_volte_call enable");
        return true;
    }

    public boolean isInImsCall() {
        Phone phone = this.mPhone.getImsPhone();
        if (phone == null || !(phone instanceof ImsPhone)) {
            return false;
        }
        ImsPhone mImsphone = (ImsPhone) phone;
        Call.State foregroundCallState = mImsphone.getForegroundCall().getState();
        Call.State backgroundCallState = mImsphone.getBackgroundCall().getState();
        Call.State ringingCallState = mImsphone.getRingingCall().getState();
        if (foregroundCallState.isAlive() || backgroundCallState.isAlive() || ringingCallState.isAlive()) {
            return true;
        }
        return false;
    }

    public String getDefaultVMByImsi(String imsi) {
        if (imsi == null || DEFAULT_VM_BY_MCCMNC == null) {
            return null;
        }
        int i = 0;
        while (true) {
            String[][] strArr = DEFAULT_VM_BY_MCCMNC;
            if (i >= strArr.length) {
                return null;
            }
            if (imsi.startsWith(strArr[i][0])) {
                String defaultVM = DEFAULT_VM_BY_MCCMNC[i][1];
                logd("getDefaultVMByImsi: defaultVM = " + defaultVM);
                return defaultVM;
            }
            i++;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setLteWifiConfig(boolean enabled) {
        String atCommand;
        if (enabled) {
            atCommand = "AT+EGCMD = 6, 0, \"EL1_EL1_IDC_DISABLE_CUSTOM_IT\"";
        } else {
            atCommand = "AT+EGCMD = 6, 5, \"EL1_EL1_IDC_DISABLE_CUSTOM_IT\"";
        }
        try {
            this.mPhone.invokeOemRilRequestStrings(new String[]{atCommand, ""}, this.mHandler.obtainMessage(2));
            this.retrySaveConfig = 0;
        } catch (Exception e) {
            loge("LteWifiCoexist at fail.");
            e.printStackTrace();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void saveLteWifiConfig(boolean enabled) {
        String atCommand;
        if (enabled) {
            atCommand = "AT+ESBP=6, \"SBP_IDC_DISABLE_CUSTOM\",0";
        } else {
            atCommand = "AT+ESBP=6, \"SBP_IDC_DISABLE_CUSTOM\",5";
        }
        try {
            this.mPhone.invokeOemRilRequestStrings(new String[]{atCommand, ""}, this.mHandler.obtainMessage(3));
            this.retrySetConfig = 0;
        } catch (Exception e) {
            loge("LteWifiCoexist at fail.");
            e.printStackTrace();
        }
    }

    public void updateLteWifiCoexist(boolean enabled) {
        String str = this.LOG_TAG;
        Rlog.e(str, "updateLteWifiCoexist:" + enabled);
        setLteWifiConfig(enabled);
        saveLteWifiConfig(enabled);
    }

    public void getLteWifiCoexistStatus() {
        Rlog.e(this.LOG_TAG, "getLteWifiCoexistStatus.");
        try {
            this.mPhone.invokeOemRilRequestStrings(new String[]{"AT+ESBP=8,\"SBP_IDC_DISABLE_CUSTOM\"", "+ESBP:"}, this.mHandler.obtainMessage(1));
        } catch (Exception e) {
            loge("LteWifiCoexist at fail.");
            e.printStackTrace();
        }
    }

    /* access modifiers changed from: package-private */
    public void logd(String s) {
        if (OemConstant.SWITCH_LOG) {
            Rlog.d(this.LOG_TAG, s);
        }
    }

    /* access modifiers changed from: package-private */
    public void loge(String s) {
        Rlog.e(this.LOG_TAG, s);
    }
}
