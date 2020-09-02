package com.mediatek.internal.telephony.uicc;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncResult;
import android.os.Message;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.telephony.Rlog;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.MccTable;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneFactory;
import com.android.internal.telephony.SubscriptionController;
import com.android.internal.telephony.uicc.IccCardApplicationStatus;
import com.android.internal.telephony.uicc.IccRefreshResponse;
import com.android.internal.telephony.uicc.RuimRecords;
import com.android.internal.telephony.uicc.UiccController;
import com.android.internal.telephony.uicc.UiccProfile;
import com.mediatek.internal.telephony.datasub.DataSubConstants;
import com.mediatek.internal.telephony.phb.CsimPhbUtil;
import com.mediatek.internal.telephony.phb.MtkAdnRecordCache;
import com.mediatek.internal.telephony.ppl.PplControlData;
import com.mediatek.internal.telephony.uicc.IccServiceInfo;
import java.util.Arrays;

public class MtkRuimRecords extends RuimRecords implements MtkIccConstants {
    public static final int C2K_PHB_NOT_READY = 2;
    public static final int C2K_PHB_READY = 3;
    private static final int CSIM_FDN_SERVICE_MASK_ACTIVE = 1;
    private static final int CSIM_FDN_SERVICE_MASK_EXIST = 2;
    private static final int EVENT_DELAYED_SEND_PHB_CHANGE = 503;
    private static final int EVENT_GET_EST_DONE = 501;
    private static final int EVENT_PHB_READY = 504;
    private static final int EVENT_RADIO_STATE_CHANGED = 502;
    public static final int GSM_PHB_NOT_READY = 0;
    public static final int GSM_PHB_READY = 1;
    static final String LOG_TAG = "MtkRuimRecords";
    private static final int MCC_LEN = 3;
    public static final int PHB_DELAY_SEND_TIME = 500;
    static final String PROPERTY_RIL_C2K_PHB_READY = "vendor.cdma.sim.ril.phbready";
    private static final String[] PROPERTY_RIL_FULL_UICC_TYPE = {"vendor.gsm.ril.fulluicctype", "vendor.gsm.ril.fulluicctype.2", "vendor.gsm.ril.fulluicctype.3", "vendor.gsm.ril.fulluicctype.4"};
    static final String PROPERTY_RIL_GSM_PHB_READY = "vendor.gsm.sim.ril.phbready";
    private static final int RUIM_FDN_SERVICE_MASK_EXIST_ACTIVE = 48;
    private static final int RUIM_FDN_SERVICE_MASK_EXIST_INACTIVE = 16;
    private boolean mDispose = false;
    private byte[] mEnableService;
    private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        /* class com.mediatek.internal.telephony.uicc.MtkRuimRecords.AnonymousClass1 */

        public void onReceive(Context context, Intent intent) {
            String strPhbReady;
            String action = intent.getAction();
            if (action.equals("android.intent.action.RADIO_TECHNOLOGY")) {
                int phoneId = intent.getIntExtra("phone", -1);
                MtkRuimRecords mtkRuimRecords = MtkRuimRecords.this;
                mtkRuimRecords.log("[onReceive] ACTION_RADIO_TECHNOLOGY_CHANGED phoneId : " + phoneId);
                if (MtkRuimRecords.this.mParentApp != null && MtkRuimRecords.this.mParentApp.getPhoneId() == phoneId) {
                    String activePhoneName = intent.getStringExtra("phoneName");
                    int subId = intent.getIntExtra("subscription", -1);
                    MtkRuimRecords mtkRuimRecords2 = MtkRuimRecords.this;
                    mtkRuimRecords2.log("[onReceive] ACTION_RADIO_TECHNOLOGY_CHANGED activePhoneName: " + activePhoneName + ", subId : " + subId + ", phoneId: " + phoneId);
                    if ("CDMA".equals(activePhoneName)) {
                        MtkRuimRecords.this.broadcastPhbStateChangedIntent(false, true);
                        MtkRuimRecords mtkRuimRecords3 = MtkRuimRecords.this;
                        mtkRuimRecords3.sendMessageDelayed(mtkRuimRecords3.obtainMessage(MtkRuimRecords.EVENT_DELAYED_SEND_PHB_CHANGE), 500);
                        MtkRuimRecords.this.mAdnCache.reset();
                    }
                }
            } else if (action.equals("android.intent.action.ACTION_SUBINFO_RECORD_UPDATED") && MtkRuimRecords.this.mParentApp != null) {
                MtkRuimRecords mtkRuimRecords4 = MtkRuimRecords.this;
                mtkRuimRecords4.log("[onReceive] onReceive ACTION_SUBINFO_RECORD_UPDATED mPhbWaitSub: " + MtkRuimRecords.this.mPhbWaitSub);
                if (MtkRuimRecords.this.mPhbWaitSub) {
                    boolean unused = MtkRuimRecords.this.mPhbWaitSub = false;
                    MtkRuimRecords mtkRuimRecords5 = MtkRuimRecords.this;
                    mtkRuimRecords5.broadcastPhbStateChangedIntent(mtkRuimRecords5.mPhbReady, false);
                }
            } else if (action.equals("android.intent.action.SIM_STATE_CHANGED") && MtkRuimRecords.this.mParentApp != null) {
                int id = intent.getIntExtra("phone", 0);
                String simState = intent.getStringExtra("ss");
                if (id == MtkRuimRecords.this.mPhoneId) {
                    if (CsimPhbUtil.isUsingGsmPhbReady(MtkRuimRecords.this.mFh)) {
                        strPhbReady = TelephonyManager.getTelephonyProperty(MtkRuimRecords.this.mPhoneId, MtkRuimRecords.PROPERTY_RIL_GSM_PHB_READY, "false");
                    } else {
                        strPhbReady = TelephonyManager.getTelephonyProperty(MtkRuimRecords.this.mPhoneId, MtkRuimRecords.PROPERTY_RIL_C2K_PHB_READY, "false");
                    }
                    MtkRuimRecords mtkRuimRecords6 = MtkRuimRecords.this;
                    mtkRuimRecords6.log("sim state: " + simState + ", mPhbReady: " + MtkRuimRecords.this.mPhbReady + ",strPhbReady: " + strPhbReady.equals("true"));
                    if (!"READY".equals(simState)) {
                        return;
                    }
                    if (!MtkRuimRecords.this.mPhbReady && strPhbReady.equals("true")) {
                        boolean unused2 = MtkRuimRecords.this.mPhbReady = true;
                        MtkRuimRecords mtkRuimRecords7 = MtkRuimRecords.this;
                        mtkRuimRecords7.broadcastPhbStateChangedIntent(mtkRuimRecords7.mPhbReady, false);
                    } else if (true == MtkRuimRecords.this.mPhbWaitSub && strPhbReady.equals("true")) {
                        MtkRuimRecords mtkRuimRecords8 = MtkRuimRecords.this;
                        mtkRuimRecords8.log("mPhbWaitSub is " + MtkRuimRecords.this.mPhbWaitSub + ", broadcast if need");
                        boolean unused3 = MtkRuimRecords.this.mPhbWaitSub = false;
                        MtkRuimRecords mtkRuimRecords9 = MtkRuimRecords.this;
                        mtkRuimRecords9.broadcastPhbStateChangedIntent(mtkRuimRecords9.mPhbReady, false);
                    }
                }
            } else if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
                MtkRuimRecords mtkRuimRecords10 = MtkRuimRecords.this;
                mtkRuimRecords10.log("[onReceive] ACTION_BOOT_COMPLETED mPendingPhbNotify : " + MtkRuimRecords.this.mPendingPhbNotify);
                if (MtkRuimRecords.this.mPendingPhbNotify) {
                    MtkRuimRecords mtkRuimRecords11 = MtkRuimRecords.this;
                    mtkRuimRecords11.broadcastPhbStateChangedIntent(mtkRuimRecords11.isPhbReady(), false);
                    boolean unused4 = MtkRuimRecords.this.mPendingPhbNotify = false;
                }
            }
        }
    };
    /* access modifiers changed from: private */
    public boolean mPendingPhbNotify = false;
    /* access modifiers changed from: private */
    public boolean mPhbReady = false;
    /* access modifiers changed from: private */
    public boolean mPhbWaitSub = false;
    private Phone mPhone;
    /* access modifiers changed from: private */
    public int mPhoneId = -1;
    private String mRuimImsi = null;
    private byte[] mSimService;
    private int mSubId = -1;

    public MtkRuimRecords(MtkUiccCardApplication app, Context c, CommandsInterface ci) {
        super(app, c, ci);
        this.mPhoneId = app.getPhoneId();
        this.mPhone = PhoneFactory.getPhone(app.getPhoneId());
        log("MtkRuimRecords X ctor this=" + this);
        this.mAdnCache = new MtkAdnRecordCache(this.mFh, ci, app);
        this.mCi.registerForPhbReady(this, EVENT_PHB_READY, null);
        this.mCi.registerForRadioStateChanged(this, (int) EVENT_RADIO_STATE_CHANGED, (Object) null);
        this.mAdnCache.reset();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.ACTION_SUBINFO_RECORD_UPDATED");
        filter.addAction("android.intent.action.RADIO_TECHNOLOGY");
        filter.addAction("android.intent.action.SIM_STATE_CHANGED");
        filter.addAction("android.intent.action.BOOT_COMPLETED");
        this.mContext.registerReceiver(this.mIntentReceiver, filter);
        log("updateIccRecords in IccPhoneBookeInterfaceManager");
        Phone phone = this.mPhone;
        if (!(phone == null || phone.getIccPhoneBookInterfaceManager() == null)) {
            this.mPhone.getIccPhoneBookInterfaceManager().updateIccRecords(this);
        }
        if (isPhbReady()) {
            this.mPhbReady = true;
            broadcastPhbStateChangedIntent(this.mPhbReady, false);
        }
    }

    public void dispose() {
        log("Disposing MtkRuimRecords " + this);
        this.mDispose = true;
        if (!isCdma4GDualModeCard()) {
            log("dispose, reset operator numeric, name and country iso");
            this.mTelephonyManager.setSimOperatorNumericForPhone(this.mParentApp.getPhoneId(), "");
            this.mTelephonyManager.setSimOperatorNameForPhone(this.mParentApp.getPhoneId(), "");
            this.mTelephonyManager.setSimCountryIsoForPhone(this.mParentApp.getPhoneId(), "");
        }
        if (this.mPhbReady || this.mPendingPhbNotify) {
            log("MtkRuimRecords Disposing set PHB unready mPendingPhbNotify=" + this.mPendingPhbNotify + "mPhbReady=" + this.mPhbReady);
            this.mPhbReady = false;
            this.mPendingPhbNotify = false;
            broadcastPhbStateChangedIntent(this.mPhbReady, false);
        }
        this.mParentApp.unregisterForReady(this);
        this.mPhbWaitSub = false;
        this.mCi.unregisterForRadioStateChanged(this);
        this.mCi.unregisterForPhbReady(this);
        this.mContext.unregisterReceiver(this.mIntentReceiver);
        this.mPhone.getIccPhoneBookInterfaceManager().dispose();
        MtkRuimRecords.super.dispose();
    }

    /* access modifiers changed from: protected */
    public void resetRecords() {
        MtkRuimRecords.super.resetRecords();
    }

    public String getOperatorNumeric() {
        try {
            String imsi = getIMSI();
            if (imsi == null) {
                return null;
            }
            if (this.mMncLength == -1 || this.mMncLength == 0) {
                return imsi.substring(0, MccTable.smallestDigitsMccForMnc(Integer.parseInt(imsi.substring(0, 3))) + 3);
            }
            return imsi.substring(0, this.mMncLength + 3);
        } catch (NumberFormatException e) {
            Rlog.d(LOG_TAG, e.toString());
            return null;
        } catch (Exception e2) {
            Rlog.d(LOG_TAG, e2.toString());
            return null;
        }
    }

    public void handleMessage(Message msg) {
        boolean isSimLocked;
        boolean isRecordLoadResponse = false;
        if (this.mDestroyed.get()) {
            loge("Received message " + msg + "[" + msg.what + "] while being destroyed. Ignoring.");
            return;
        }
        try {
            int i = msg.what;
            if (i == 10) {
                AsyncResult ar = (AsyncResult) msg.obj;
                String[] localTemp = (String[]) ar.result;
                if (ar.exception == null) {
                    this.mMyMobileNumber = localTemp[0];
                    this.mMin2Min1 = localTemp[3];
                    this.mPrlVersion = localTemp[4];
                    log("MDN: " + MtkIccUtilsEx.getPrintableString(this.mMyMobileNumber, 8) + " MIN: " + MtkIccUtilsEx.getPrintableString(this.mMin2Min1, 8));
                }
            } else if (i == 17) {
                log("Event EVENT_GET_SST_DONE Received");
                isRecordLoadResponse = false;
                AsyncResult ar2 = (AsyncResult) msg.obj;
                if (ar2.exception != null) {
                    logi("EVENT_GET_SST_DONE failed");
                } else {
                    this.mSimService = (byte[]) ar2.result;
                    log("mSimService[0]: " + ((int) this.mSimService[0]) + ", data.length: " + this.mSimService.length);
                    updateIccFdnStatus();
                }
            } else if (i == EVENT_GET_EST_DONE) {
                isRecordLoadResponse = false;
                log("Event EVENT_GET_EST_DONE Received");
                AsyncResult ar3 = (AsyncResult) msg.obj;
                if (ar3.exception != null) {
                    logi("EVENT_GET_EST_DONE failed");
                } else {
                    this.mEnableService = (byte[]) ar3.result;
                    log("mEnableService[0]: " + ((int) this.mEnableService[0]) + ", mEnableService.length: " + this.mEnableService.length);
                    updateIccFdnStatus();
                }
            } else if (i == EVENT_DELAYED_SEND_PHB_CHANGE) {
                this.mPhbReady = isPhbReady();
                log("[EVENT_DELAYED_SEND_PHB_CHANGE] isReady : " + this.mPhbReady);
                broadcastPhbStateChangedIntent(this.mPhbReady, false);
            } else if (i != EVENT_PHB_READY) {
                MtkRuimRecords.super.handleMessage(msg);
            } else {
                AsyncResult ar4 = (AsyncResult) msg.obj;
                log("[DBG]EVENT_PHB_READY ar:" + ar4);
                if (!(ar4 == null || ar4.exception != null || ar4.result == null)) {
                    int[] phbReadyState = (int[]) ar4.result;
                    this.mParentApp.getPhoneId();
                    int curSimState = SubscriptionController.getInstance().getSimStateForSlotIndex(this.mPhoneId);
                    if (curSimState != 4) {
                        if (curSimState != 2) {
                            isSimLocked = false;
                            updatePhbStatus(phbReadyState[0], isSimLocked);
                            updateIccFdnStatus();
                        }
                    }
                    isSimLocked = true;
                    updatePhbStatus(phbReadyState[0], isSimLocked);
                    updateIccFdnStatus();
                }
            }
            if (!isRecordLoadResponse) {
                return;
            }
        } catch (RuntimeException exc) {
            Rlog.w(LOG_TAG, "Exception parsing RUIM record", exc);
            if (0 == 0) {
                return;
            }
        } catch (Throwable th) {
            if (0 != 0) {
                onRecordLoaded();
            }
            throw th;
        }
        onRecordLoaded();
    }

    /* access modifiers changed from: protected */
    public void onAllRecordsLoaded() {
        MtkRuimRecords.super.onAllRecordsLoaded();
        log("onAllRecordsLoaded, mParentApp.getType() = " + this.mParentApp.getType());
        if (this.mParentApp.getType() == IccCardApplicationStatus.AppType.APPTYPE_RUIM) {
            this.mFh.loadEFTransparent(28466, obtainMessage(17));
        } else if (this.mParentApp.getType() == IccCardApplicationStatus.AppType.APPTYPE_CSIM) {
            this.mFh.loadEFTransparent(28466, obtainMessage(17));
            this.mFh.loadEFTransparent((int) MtkIccConstants.EF_EST, obtainMessage(EVENT_GET_EST_DONE));
        }
    }

    public int getCarrierNameDisplayCondition() {
        String spn = getServiceProviderName();
        UiccProfile uiccProfile = UiccController.getInstance().getUiccProfileForPhone(this.mPhoneId);
        StringBuilder sb = new StringBuilder();
        sb.append("getCarrierNameDisplayCondition uiccProfile is ");
        sb.append(uiccProfile != null ? uiccProfile : "null");
        log(sb.toString());
        if (uiccProfile != null && uiccProfile.getOperatorBrandOverride() != null) {
            log("getCarrierNameDisplayCondition, getOperatorBrandOverride is not null");
            return 1;
        } else if (!this.mCsimSpnDisplayCondition) {
            log("getCarrierNameDisplayCondition, no EF_SPN");
            return 1;
        } else if (TextUtils.isEmpty(spn) || spn.equals("")) {
            log("getCarrierNameDisplayCondition, show plmn");
            return 1;
        } else {
            log("getCarrierNameDisplayCondition, show spn");
            return 2;
        }
    }

    /* access modifiers changed from: protected */
    public void log(String s) {
        Rlog.d("RuimRecords", "[RuimRecords] " + s + " (phoneId " + this.mPhoneId + ")");
    }

    /* access modifiers changed from: protected */
    public void loge(String s) {
        Rlog.e(LOG_TAG, "[MtkRuimRecords] " + s + " (phoneId " + this.mPhoneId + ")");
    }

    /* access modifiers changed from: protected */
    public void logi(String s) {
        Rlog.i(LOG_TAG, "[MtkRuimRecords] " + s + " (phoneId " + this.mPhoneId + ")");
    }

    public IccServiceInfo.IccServiceStatus getSIMServiceStatus(IccServiceInfo.IccService enService) {
        IccServiceInfo.IccServiceStatus simServiceStatus = IccServiceInfo.IccServiceStatus.UNKNOWN;
        if (this.mParentApp == null) {
            log("getSIMServiceStatus enService: " + enService + ", mParentApp = null.");
            return simServiceStatus;
        }
        log("getSIMServiceStatus enService: " + enService + ", mParentApp.getType(): " + this.mParentApp.getType());
        if (enService == IccServiceInfo.IccService.FDN && this.mSimService != null && this.mParentApp.getType() == IccCardApplicationStatus.AppType.APPTYPE_RUIM) {
            log("getSIMServiceStatus mSimService[0]: " + ((int) this.mSimService[0]));
            byte[] bArr = this.mSimService;
            if ((bArr[0] & RUIM_FDN_SERVICE_MASK_EXIST_ACTIVE) == RUIM_FDN_SERVICE_MASK_EXIST_ACTIVE) {
                return IccServiceInfo.IccServiceStatus.ACTIVATED;
            }
            if ((bArr[0] & PplControlData.STATUS_WIPE_REQUESTED) == 16) {
                return IccServiceInfo.IccServiceStatus.INACTIVATED;
            }
            return IccServiceInfo.IccServiceStatus.NOT_EXIST_IN_SIM;
        } else if (enService != IccServiceInfo.IccService.FDN || this.mSimService == null || this.mEnableService == null || this.mParentApp.getType() != IccCardApplicationStatus.AppType.APPTYPE_CSIM) {
            return simServiceStatus;
        } else {
            log("getSIMServiceStatus mSimService[0]: " + ((int) this.mSimService[0]) + ", mEnableService[0]: " + ((int) this.mEnableService[0]));
            if ((this.mSimService[0] & 2) == 2 && (this.mEnableService[0] & 1) == 1) {
                return IccServiceInfo.IccServiceStatus.ACTIVATED;
            }
            if ((this.mSimService[0] & 2) == 2) {
                return IccServiceInfo.IccServiceStatus.INACTIVATED;
            }
            return IccServiceInfo.IccServiceStatus.NOT_EXIST_IN_USIM;
        }
    }

    public boolean isCdmaOnly() {
        String[] values = null;
        int i = this.mPhoneId;
        if (i >= 0) {
            String[] strArr = PROPERTY_RIL_FULL_UICC_TYPE;
            if (i < strArr.length) {
                String prop = SystemProperties.get(strArr[i]);
                if (prop != null && prop.length() > 0) {
                    values = prop.split(",");
                }
                StringBuilder sb = new StringBuilder();
                sb.append("isCdmaOnly PhoneId ");
                sb.append(this.mPhoneId);
                sb.append(", prop value= ");
                sb.append(prop);
                sb.append(", size= ");
                sb.append(values != null ? values.length : 0);
                log(sb.toString());
                if (values == null || Arrays.asList(values).contains("USIM") || Arrays.asList(values).contains("SIM")) {
                    return false;
                }
                return true;
            }
        }
        log("isCdmaOnly: invalid PhoneId " + this.mPhoneId);
        return false;
    }

    public boolean isCdma4GDualModeCard() {
        String[] values = null;
        int i = this.mPhoneId;
        if (i >= 0) {
            String[] strArr = PROPERTY_RIL_FULL_UICC_TYPE;
            if (i < strArr.length) {
                String prop = SystemProperties.get(strArr[i]);
                if (prop != null && prop.length() > 0) {
                    values = prop.split(",");
                }
                StringBuilder sb = new StringBuilder();
                sb.append("isCdma4GDualModeCard PhoneId ");
                sb.append(this.mPhoneId);
                sb.append(", prop value= ");
                sb.append(prop);
                sb.append(", size= ");
                sb.append(values != null ? values.length : 0);
                log(sb.toString());
                if (values == null || !Arrays.asList(values).contains("USIM") || !Arrays.asList(values).contains("CSIM")) {
                    return false;
                }
                return true;
            }
        }
        log("isCdma4GDualModeCard: invalid PhoneId " + this.mPhoneId);
        return false;
    }

    /* access modifiers changed from: protected */
    public void updateIccFdnStatus() {
        log("updateIccFdnStatus mParentAPP=" + this.mParentApp + "  getSIMServiceStatus(Phone.IccService.FDN)=" + getSIMServiceStatus(IccServiceInfo.IccService.FDN) + "  IccServiceStatus.ACTIVATE=" + IccServiceInfo.IccServiceStatus.ACTIVATED);
        if (this.mParentApp != null && getSIMServiceStatus(IccServiceInfo.IccService.FDN) == IccServiceInfo.IccServiceStatus.ACTIVATED) {
            this.mParentApp.queryFdn();
        }
    }

    public boolean isPhbReady() {
        String strPhbReady;
        String strCurSimState = "";
        StringBuilder sb = new StringBuilder();
        sb.append("[phbReady] Start mPhbReady: ");
        sb.append(this.mPhbReady ? "true" : "false");
        log(sb.toString());
        if (this.mParentApp == null) {
            return false;
        }
        if (CsimPhbUtil.isUsingGsmPhbReady(this.mFh)) {
            strPhbReady = TelephonyManager.getTelephonyProperty(this.mPhoneId, PROPERTY_RIL_GSM_PHB_READY, "false");
        } else {
            strPhbReady = TelephonyManager.getTelephonyProperty(this.mPhoneId, PROPERTY_RIL_C2K_PHB_READY, "false");
        }
        if (SystemProperties.get("ro.vendor.mtk_ril_mode").equals("c6m_1rild")) {
            return strPhbReady.equals("true");
        }
        String strAllSimState = SystemProperties.get("gsm.sim.state");
        if (strAllSimState != null && strAllSimState.length() > 0) {
            String[] values = strAllSimState.split(",");
            int i = this.mPhoneId;
            if (i >= 0 && i < values.length && values[i] != null) {
                strCurSimState = values[i];
            }
        }
        boolean isSimLocked = strCurSimState.equals("NETWORK_LOCKED") || strCurSimState.equals("PIN_REQUIRED");
        log("[phbReady] End strPhbReady: " + strPhbReady + ", strAllSimState: " + strAllSimState);
        if (!strPhbReady.equals("true") || isSimLocked) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: private */
    public void broadcastPhbStateChangedIntent(boolean isReady, boolean isForceSendIntent) {
        int[] subIds;
        Phone phone = this.mPhone;
        if (phone == null || phone.getPhoneType() == 2 || (this.mDispose && !isReady)) {
            log("broadcastPhbStateChangedIntent, mPhbReady " + this.mPhbReady + ", " + this.mSubId);
            if (isReady) {
                int[] subIds2 = SubscriptionManager.getSubId(this.mPhoneId);
                if (subIds2 != null && subIds2.length > 0) {
                    this.mSubId = subIds2[0];
                }
                if (this.mSubId <= 0) {
                    log("broadcastPhbStateChangedIntent, mSubId <= 0");
                    this.mPhbWaitSub = true;
                    return;
                }
            } else {
                if (isForceSendIntent && this.mPhbReady && (subIds = SubscriptionManager.getSubId(this.mPhoneId)) != null && subIds.length > 0) {
                    this.mSubId = subIds[0];
                }
                if (this.mSubId <= 0) {
                    log("broadcastPhbStateChangedIntent, isReady == false and mSubId <= 0");
                    return;
                }
            }
            boolean isUnlock = ((UserManager) this.mContext.getSystemService(DataSubConstants.REASON_MOBILE_DATA_ENABLE_USER)).isUserUnlocked();
            if (!SystemProperties.get("sys.boot_completed").equals("1") || !isUnlock) {
                log("broadcastPhbStateChangedIntent, boot not completed, isUnlock:" + isUnlock);
                this.mPendingPhbNotify = true;
                return;
            }
            Intent intent = new Intent("mediatek.intent.action.PHB_STATE_CHANGED");
            intent.putExtra("ready", isReady);
            intent.putExtra("subscription", this.mSubId);
            log("Broadcasting intent ACTION_PHB_STATE_CHANGED " + isReady + " sub id " + this.mSubId + " phoneId " + this.mParentApp.getPhoneId());
            this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
            Intent bootIntent = new Intent("mediatek.intent.action.PHB_STATE_CHANGED");
            bootIntent.putExtra("ready", isReady);
            bootIntent.putExtra("subscription", this.mSubId);
            bootIntent.setPackage("com.mediatek.simprocessor");
            log("Broadcasting intent ACTION_PHB_STATE_CHANGED to package: simprocessor");
            this.mContext.sendBroadcastAsUser(bootIntent, UserHandle.ALL);
            if (!isReady) {
                this.mSubId = -1;
                return;
            }
            return;
        }
        this.mPendingPhbNotify = true;
        log("broadcastPhbStateChangedIntent, No active Phone will notfiy when dispose");
    }

    private void updatePhbStatus(int status, boolean isSimLocked) {
        boolean simLockedState;
        boolean isReady;
        log("[PhbStatus] status: " + status + ", isSimLocked: " + isSimLocked + ", mPhbReady: " + this.mPhbReady);
        if (SystemProperties.get("ro.vendor.mtk_ril_mode").equals("c6m_1rild")) {
            simLockedState = false;
        } else {
            simLockedState = isSimLocked;
        }
        if (CsimPhbUtil.isUsingGsmPhbReady(this.mFh)) {
            if (status == 1) {
                isReady = true;
            } else if (status == 0) {
                isReady = false;
            } else {
                log("[PhbStatus] not GSM PHB status");
                return;
            }
        } else if (status == 3) {
            isReady = true;
        } else if (status == 2) {
            isReady = false;
        } else {
            log("[PhbStatus] not C2K PHB status");
            return;
        }
        if (!isReady) {
            boolean z = this.mPhbReady;
            if (z) {
                this.mAdnCache.reset();
                this.mPhbReady = false;
                broadcastPhbStateChangedIntent(this.mPhbReady, false);
                return;
            }
            broadcastPhbStateChangedIntent(z, false);
        } else if (!simLockedState) {
            boolean z2 = this.mPhbReady;
            if (!z2) {
                this.mPhbReady = true;
                broadcastPhbStateChangedIntent(this.mPhbReady, false);
                return;
            }
            broadcastPhbStateChangedIntent(z2, false);
        } else {
            log("[PhbStatus] phb ready but sim is not ready.");
            this.mPhbReady = false;
            broadcastPhbStateChangedIntent(this.mPhbReady, false);
        }
    }

    /* access modifiers changed from: protected */
    public void onGetImsiDone(String imsi) {
        if (this.mImsi != null && !this.mImsi.equals("") && this.mImsi.length() >= 3) {
            SystemProperties.set("vendor.cdma.icc.operator.mcc", this.mImsi.substring(0, 3));
        }
        if (this.mImsi != null && !this.mImsi.equals(this.mRuimImsi)) {
            this.mRuimImsi = this.mImsi;
            this.mImsiReadyRegistrants.notifyRegistrants();
            log("MtkRuimRecords: mImsiReadyRegistrants.notifyRegistrants");
        }
    }

    /* access modifiers changed from: protected */
    public void handleRefresh(IccRefreshResponse refreshResponse) {
        if (refreshResponse == null) {
            log("handleRefresh received without input");
        } else if (refreshResponse.aid == null || TextUtils.isEmpty(refreshResponse.aid) || refreshResponse.aid.equals(this.mParentApp.getAid()) || refreshResponse.refreshResult == 4) {
            int i = refreshResponse.refreshResult;
            if (i == 1) {
                log("handleRefresh with SIM_REFRESH_INIT");
                handleFileUpdate(-1);
            } else if (i == 2) {
                log("handleRefresh with SIM_REFRESH_RESET");
            } else if (i != 4) {
                log("handleRefresh,callback to super");
                MtkRuimRecords.super.handleRefresh(refreshResponse);
            } else {
                log("handleRefresh with REFRESH_INIT_FULL_FILE_UPDATED");
                handleFileUpdate(-1);
            }
        }
    }

    public void onReady() {
        this.mLockedRecordsReqReason = 0;
        MtkRuimRecords.super.onReady();
    }

    /* access modifiers changed from: protected */
    public void onLocked(int msg) {
        int i;
        this.mRecordsRequested = false;
        this.mLoaded.set(false);
        if (this.mLockedRecordsReqReason != 0) {
            if (msg == 32) {
                i = 1;
            } else {
                i = 2;
            }
            this.mLockedRecordsReqReason = i;
            this.mRecordsToLoad++;
            onRecordLoaded();
            return;
        }
        MtkRuimRecords.super.onLocked(msg);
    }

    /* access modifiers changed from: protected */
    public void handleFileUpdate(int efid) {
        this.mLoaded.set(false);
        MtkRuimRecords.super.handleFileUpdate(efid);
    }
}
