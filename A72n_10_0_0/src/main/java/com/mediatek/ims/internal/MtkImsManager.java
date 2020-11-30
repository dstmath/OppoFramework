package com.mediatek.ims.internal;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.telephony.Rlog;
import android.telephony.ServiceState;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.telephony.ims.ImsCallProfile;
import android.telephony.ims.ImsCallSession;
import android.telephony.ims.ImsMmTelManager;
import android.telephony.ims.ImsReasonInfo;
import android.telephony.ims.aidl.IImsCallSessionListener;
import android.telephony.ims.aidl.IImsCapabilityCallback;
import android.telephony.ims.feature.CapabilityChangeRequest;
import android.telephony.ims.feature.MmTelFeature;
import android.text.TextUtils;
import android.util.Log;
import com.android.ims.ImsCall;
import com.android.ims.ImsException;
import com.android.ims.ImsManager;
import com.android.ims.ImsUtInterface;
import com.android.ims.internal.IImsCallSession;
import com.android.ims.internal.IImsRegistrationListener;
import com.android.ims.internal.IImsUt;
import com.mediatek.ims.MtkImsCall;
import com.mediatek.ims.MtkImsConnectionStateListener;
import com.mediatek.ims.MtkImsUt;
import com.mediatek.ims.internal.IMtkImsRegistrationListener;
import com.mediatek.ims.internal.IMtkImsService;
import com.mediatek.ims.internal.ext.IImsManagerExt;
import com.mediatek.ims.internal.ext.OpImsCustomizationUtils;
import com.mediatek.internal.telephony.IMtkPhoneSubInfoEx;
import com.mediatek.internal.telephony.MtkIccCardConstants;
import com.mediatek.internal.telephony.MtkSubscriptionManager;
import com.mediatek.telephony.MtkTelephonyManagerEx;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import mediatek.telephony.MtkServiceState;

public class MtkImsManager extends ImsManager {
    public static final String ACTION_IMS_INCOMING_CALL_INDICATION = "com.android.ims.IMS_INCOMING_CALL_INDICATION";
    public static final String ACTION_IMS_NOT_RINGING_INCOMING_CALL = "com.mediatek.ims.NOT_RINGING_INCOMING_CALL";
    public static final String ACTION_IMS_RADIO_STATE_CHANGED = "com.android.ims.IMS_RADIO_STATE_CHANGED";
    public static final String ACTION_IMS_RTP_INFO = "com.android.ims.IMS_RTP_INFO";
    public static final String ACTION_IMS_SERVICE_DEREGISTERED = "com.android.ims.IMS_SERVICE_DEREGISTERED";
    public static final String DATA_ENABLED_PROP = "net.lte.ims.data.enabled";
    public static final String DATA_ROAMING_PROP = "net.lte.data.roaming";
    public static final String DATA_ROAMING_SETTING_PROP = "net.lte.data.roaming.setting";
    private static final boolean DBG = true;
    public static final String ENHANCED_4G_MODE_ENABLED_SIM2 = "volte_vt_enabled_sim2";
    public static final String ENHANCED_4G_MODE_ENABLED_SIM3 = "volte_vt_enabled_sim3";
    public static final String ENHANCED_4G_MODE_ENABLED_SIM4 = "volte_vt_enabled_sim4";
    public static final String EXTRA_CALL_MODE = "android:imsCallMode";
    public static final String EXTRA_DIAL_STRING = "android:imsDialString";
    public static final String EXTRA_IMS_DISABLE_CAP_KEY = "android:disablecap";
    public static final String EXTRA_IMS_ENABLE_CAP_KEY = "android:enablecap";
    public static final String EXTRA_IMS_RADIO_STATE = "android:imsRadioState";
    public static final String EXTRA_IMS_REG_ERROR_KEY = "android:regError";
    public static final String EXTRA_IMS_REG_STATE_KEY = "android:regState";
    public static final String EXTRA_MT_TO_NUMBER = "mediatek:mtToNumber";
    public static final String EXTRA_PHONE_ID = "android:phoneId";
    public static final String EXTRA_RTP_NETWORK_ID = "android:rtpNetworkId";
    public static final String EXTRA_RTP_PDN_ID = "android:rtpPdnId";
    public static final String EXTRA_RTP_RECV_PKT_LOST = "android:rtpRecvPktLost";
    public static final String EXTRA_RTP_SEND_PKT_LOST = "android:rtpSendPktLost";
    public static final String EXTRA_RTP_TIMER = "android:rtpTimer";
    public static final String EXTRA_RTT_INCOMING_CALL = "rtt_feature:rtt_incoming_call";
    public static final String EXTRA_SEQ_NUM = "android:imsSeqNum";
    public static final int IMS_REGISTERED = 1;
    public static final int IMS_REGISTERING = 0;
    public static final int IMS_REGISTER_FAIL = 2;
    public static final String MTK_IMS_SERVICE = "mtkIms";
    private static final String MULTI_IMS_SUPPORT = "persist.vendor.mims_support";
    public static final int OOS_END_WITH_DISCONN = 0;
    public static final int OOS_END_WITH_RESUME = 2;
    public static final int OOS_START = 1;
    public static final String PREFERRED_TTY_MODE = "preferred_tty_mode";
    public static final String PREFERRED_TTY_MODE_SIM2 = "preferred_tty_mode_sim2";
    public static final String PREFERRED_TTY_MODE_SIM3 = "preferred_tty_mode_sim3";
    public static final String PREFERRED_TTY_MODE_SIM4 = "preferred_tty_mode_sim4";
    private static final String PROPERTY_CAPABILITY_SWITCH = "persist.vendor.radio.simswitch";
    private static final String PROPERTY_CT_VOLTE_SUPPORT = "persist.vendor.mtk_ct_volte_support";
    private static final String PROPERTY_DYNAMIC_IMS_SWITCH = "persist.vendor.mtk_dynamic_ims_switch";
    private static final String[] PROPERTY_ICCID_SIM = {"vendor.ril.iccid.sim1", "vendor.ril.iccid.sim2", "vendor.ril.iccid.sim3", "vendor.ril.iccid.sim4"};
    private static final String PROPERTY_IMSCONFIG_FORCE_NOTIFY = "vendor.ril.imsconfig.force.notify";
    private static final String PROPERTY_IMS_SUPPORT = "persist.vendor.ims_support";
    private static final String PROPERTY_MTK_VILTE_SUPPORT = "persist.vendor.vilte_support";
    private static final String PROPERTY_MTK_VOLTE_SUPPORT = "persist.vendor.volte_support";
    private static final String PROPERTY_MTK_WFC_SUPPORT = "persist.vendor.mtk_wfc_support";
    private static final String PROPERTY_TEST_SIM1 = "vendor.gsm.sim.ril.testsim";
    private static final String PROPERTY_TEST_SIM2 = "vendor.gsm.sim.ril.testsim.2";
    private static final String PROPERTY_TEST_SIM3 = "vendor.gsm.sim.ril.testsim.3";
    private static final String PROPERTY_TEST_SIM4 = "vendor.gsm.sim.ril.testsim.4";
    private static final String PROPERTY_VILTE_ENALBE = "persist.vendor.mtk.vilte.enable";
    private static final String PROPERTY_VIWIFI_ENALBE = "persist.vendor.mtk.viwifi.enable";
    private static final String PROPERTY_VOLTE_ENALBE = "persist.vendor.mtk.volte.enable";
    private static final String PROPERTY_WFC_ENALBE = "persist.vendor.mtk.wfc.enable";
    public static final int SERVICE_REG_CAPABILITY_EVENT_ADDED = 1;
    public static final int SERVICE_REG_CAPABILITY_EVENT_ECC_NOT_SUPPORT = 4;
    public static final int SERVICE_REG_CAPABILITY_EVENT_ECC_SUPPORT = 2;
    public static final int SERVICE_REG_CAPABILITY_EVENT_REMOVED = 0;
    public static final int SERVICE_REG_EVENT_WIFI_PDN_OOS_END_WITH_DISCONN = 6;
    public static final int SERVICE_REG_EVENT_WIFI_PDN_OOS_END_WITH_RESUME = 7;
    public static final int SERVICE_REG_EVENT_WIFI_PDN_OOS_START = 5;
    protected static final int SIM_ID_1 = 0;
    protected static final int SIM_ID_2 = 1;
    protected static final int SIM_ID_3 = 2;
    protected static final int SIM_ID_4 = 3;
    private static final String TAG = "MtkImsManager";
    private static final String TTY_MODE = "tty_mode";
    private static final String VILTE_SETTING = "vilte_setting";
    private static final String VOLTE_SETTING = "volte_setting";
    public static final String VT_IMS_ENABLED_SIM2 = "vt_ims_enabled_sim2";
    public static final String VT_IMS_ENABLED_SIM3 = "vt_ims_enabled_sim3";
    public static final String VT_IMS_ENABLED_SIM4 = "vt_ims_enabled_sim4";
    public static final String WFC_IMS_ENABLED_SIM2 = "wfc_ims_enabled_sim2";
    public static final String WFC_IMS_ENABLED_SIM3 = "wfc_ims_enabled_sim3";
    public static final String WFC_IMS_ENABLED_SIM4 = "wfc_ims_enabled_sim4";
    public static final String WFC_IMS_MODE_SIM2 = "wfc_ims_mode_sim2";
    public static final String WFC_IMS_MODE_SIM3 = "wfc_ims_mode_sim3";
    public static final String WFC_IMS_MODE_SIM4 = "wfc_ims_mode_sim4";
    public static final String WFC_IMS_ROAMING_ENABLED_SIM2 = "wfc_ims_roaming_enabled_sim2";
    public static final String WFC_IMS_ROAMING_ENABLED_SIM3 = "wfc_ims_roaming_enabled_sim3";
    public static final String WFC_IMS_ROAMING_ENABLED_SIM4 = "wfc_ims_roaming_enabled_sim4";
    public static final String WFC_IMS_ROAMING_MODE_SIM2 = "wfc_ims_roaming_mode_sim2";
    public static final String WFC_IMS_ROAMING_MODE_SIM3 = "wfc_ims_roaming_mode_sim3";
    public static final String WFC_IMS_ROAMING_MODE_SIM4 = "wfc_ims_roaming_mode_sim4";
    private static final String WFC_MODE_SETTING = "wfc_mode_setting";
    private static final String WFC_ROAMING_MODE_SETTING = "wfc_roaming_mode_setting";
    private static final String WFC_ROAMING_SETTING = "wfc_roaming_setting";
    private static final String WFC_SETTING = "wfc_setting";
    private static final boolean mSupportImsiSwitch = SystemProperties.get("ro.vendor.mtk_imsi_switch_support", "0").equals("1");
    private ArrayList<ImsMmTelManager.RegistrationCallback> mCallbacks = new ArrayList<>();
    protected IImsRegistrationListener mListener = null;
    private MtkImsServiceDeathRecipient mMtkDeathRecipient = new MtkImsServiceDeathRecipient();
    protected IMtkImsRegistrationListener mMtkImsListener = null;
    private IMtkImsService mMtkImsService = null;
    private MtkImsUt mMtkUt = null;
    private boolean mNotifyOnly = DBG;
    private Op15ImsManagerExt mOp15ImsManagerExt = null;

    public static boolean isSupportMims() {
        if (SystemProperties.getInt(MULTI_IMS_SUPPORT, 1) > 1) {
            return DBG;
        }
        return false;
    }

    public static int getMainPhoneIdForSingleIms(Context context) {
        int phoneId = SystemProperties.getInt(PROPERTY_CAPABILITY_SWITCH, 1) - 1;
        log("[getMainPhoneIdForSingleIms] : " + phoneId);
        return phoneId;
    }

    public boolean isEnhanced4gLteModeSettingEnabledByUser() {
        MtkIccCardConstants.CardType cardType;
        int sub = getSubId();
        int setting = SubscriptionManager.getIntegerSubscriptionProperty(sub, "volte_vt_enabled", -1, this.mContext);
        boolean onByDefault = getBooleanCarrierConfig("enhanced_4g_lte_on_by_default_bool");
        boolean isPSsupport = isPhoneIdSupportIms(this.mPhoneId);
        if (sub == -1) {
            if (((TelephonyManager) this.mContext.getSystemService("phone")).getSimState(this.mPhoneId) != 1) {
                onByDefault = false;
            } else {
                log("isEnhanced4gLteModeSettingEnabledByUser, sim absent");
            }
        }
        if (!getBooleanCarrierConfig("editable_enhanced_4g_lte_bool") || getBooleanCarrierConfig("hide_enhanced_4g_lte_bool") || setting == -1) {
            if (!onByDefault) {
                return onByDefault;
            }
            String iccid = SystemProperties.get(PROPERTY_ICCID_SIM[this.mPhoneId], "N/A");
            if (TextUtils.isEmpty(iccid) || !isOp09SimCard(iccid) || (cardType = MtkTelephonyManagerEx.getDefault().getCdmaCardType(this.mPhoneId)) == null || cardType.is4GCard()) {
                return onByDefault;
            }
            log("isEnhanced4gLteModeSettingEnabledByUser, CT 3G card case");
            return false;
        } else if (setting != 1 || !isPSsupport) {
            return false;
        } else {
            return DBG;
        }
    }

    /* access modifiers changed from: protected */
    public boolean shouldForceUpdated() {
        if (SystemProperties.getInt(PROPERTY_IMSCONFIG_FORCE_NOTIFY, 0) != 0) {
            return DBG;
        }
        return false;
    }

    public static boolean isNonTtyOrTtyOnVolteEnabled(Context context, int phoneId) {
        if (!isSupportMims()) {
            phoneId = getMainPhoneIdForSingleIms(context);
        }
        return getAppropriateManagerForPlugin(context, phoneId).isNonTtyOrTtyOnVolteEnabled();
    }

    public boolean isVolteEnabledByPlatform() {
        if (isTestSim()) {
            return DBG;
        }
        if (SystemProperties.getInt("persist.dbg.volte_avail_ovr" + Integer.toString(this.mPhoneId), -1) == 1 || SystemProperties.getInt("persist.dbg.volte_avail_ovr", -1) == 1) {
            return DBG;
        }
        boolean isVolteResourceSupport = isImsResourceSupport(0);
        boolean isVonrResourceSupport = isImsResourceSupport(6);
        boolean isCarrierConfigSupport = getBooleanCarrierConfig("carrier_volte_available_bool");
        boolean isGbaValidSupport = isGbaValid();
        boolean isFeatureEnableByPlatformExt = isFeatureEnabledByPlatformExt(0);
        boolean isPSsupport = isPhoneIdSupportIms(this.mPhoneId);
        log("Volte, isVolteResourceSupport:" + isVolteResourceSupport + ", isVonrResourceSupport:" + isVonrResourceSupport + ", isCarrierConfigSupport:" + isCarrierConfigSupport + ", isGbaValidSupport:" + isGbaValidSupport + ", isFeatureEnableByPlatformExt:" + isFeatureEnableByPlatformExt + ", isPSsupport:" + isPSsupport);
        if (SystemProperties.getInt(PROPERTY_MTK_VOLTE_SUPPORT, 0) != 1 || !isLteSupported() || ((!isVolteResourceSupport && !isVonrResourceSupport) || !isCarrierConfigSupport || !isGbaValidSupport || !isFeatureEnableByPlatformExt || !isPSsupport)) {
            return false;
        }
        return DBG;
    }

    public void setVoltePreferSetting(int mode) {
        try {
            MtkImsConfig config = getConfigInterfaceEx();
            if (config != null) {
                config.setVoltePreference(mode);
            }
        } catch (ImsException e) {
            loge("setVoltePreferSetting(): " + e);
        }
    }

    public void setWfcSetting(boolean enabled) {
        if (!enabled || isWfcProvisionedOnDevice()) {
            int subId = getSubId();
            if (isSubIdValid(subId)) {
                SubscriptionManager.setSubscriptionProperty(subId, "wfc_ims_enabled", booleanToPropertyString(enabled));
            } else {
                loge("setWfcSetting: invalid sub id, can not set WFC setting in siminfo db; subId=" + subId);
            }
            boolean isRoaming = ((TelephonyManager) this.mContext.getSystemService("phone")).isNetworkRoaming(subId);
            if (isRoaming && isConvertRoamingStateForSpecificOP()) {
                isRoaming = false;
            }
            setWfcNonPersistent(enabled, getWfcMode(isRoaming));
            return;
        }
        log("setWfcSetting: Not possible to enable WFC due to provisioning.");
    }

    /* access modifiers changed from: protected */
    public boolean isDataRoaming() {
        TelephonyManager tm = (TelephonyManager) this.mContext.getSystemService("phone");
        if (tm == null) {
            loge("isDataRoaming(): TelephonyManager null");
            return false;
        }
        ServiceState ss = tm.getServiceStateForSubscriber(getSubId());
        if (ss == null) {
            loge("isDataRoaming(): ServiceState null");
            return false;
        }
        log("[" + this.mPhoneId + "][SubId=" + getSubId() + "] isDataRoaming(): " + ss.getDataRoaming());
        return ss.getDataRoaming();
    }

    /* access modifiers changed from: protected */
    public boolean isDataRoamingSettingsEnabled() {
        boolean isDataRoamingSettingsEnabled = SystemProperties.getBoolean(DATA_ROAMING_SETTING_PROP + String.valueOf(getSubId()), false);
        log("[" + this.mPhoneId + "][SubId=" + getSubId() + "] isDataRoamingSettingsEnabled(): " + isDataRoamingSettingsEnabled);
        return isDataRoamingSettingsEnabled;
    }

    public void setDataRoamingSettingsEnabled(boolean enabled) {
        log("[" + this.mPhoneId + "][SubId=" + getSubId() + "] setDataRoamingSettingsEnabled(): " + enabled);
        SystemProperties.set(DATA_ROAMING_SETTING_PROP + String.valueOf(getSubId()), enabled ? "true" : "false");
    }

    public boolean isVtEnabledByPlatform() {
        if (isTestSim()) {
            return DBG;
        }
        if (SystemProperties.getInt("persist.dbg.vt_avail_ovr" + Integer.toString(this.mPhoneId), -1) == 1 || SystemProperties.getInt("persist.dbg.vt_avail_ovr", -1) == 1) {
            return DBG;
        }
        boolean isVilteResourceSupport = DBG;
        boolean isVinrResourceSupport = DBG;
        if (!isTestSim()) {
            isVilteResourceSupport = isImsResourceSupport(1);
            isVinrResourceSupport = isImsResourceSupport(7);
        }
        boolean isCarrierConfigSupport = getBooleanCarrierConfig("carrier_vt_available_bool");
        boolean isGbaValidSupport = isGbaValid();
        boolean isFeatureEnableByPlatformExt = isFeatureEnabledByPlatformExt(1);
        log("Vt, isVilteResourceSupport:" + isVilteResourceSupport + ", isVinrResourceSupport:" + isVinrResourceSupport + ", isCarrierConfigSupport:" + isCarrierConfigSupport + ", isGbaValidSupport:" + isGbaValidSupport + ", isFeatureEnableByPlatformExt:" + isFeatureEnableByPlatformExt);
        if (SystemProperties.getInt(PROPERTY_MTK_VILTE_SUPPORT, 0) != 1 || !isLteSupported() || ((!isVilteResourceSupport && !isVinrResourceSupport) || !isCarrierConfigSupport || !isGbaValidSupport || !isFeatureEnableByPlatformExt)) {
            return false;
        }
        return DBG;
    }

    public void setVtSetting(boolean enabled) {
        if (!enabled || isVtProvisionedOnDevice()) {
            int subId = getSubId();
            if (isSubIdValid(subId)) {
                SubscriptionManager.setSubscriptionProperty(subId, "vt_ims_enabled", booleanToPropertyString(enabled));
            } else {
                loge("setVtSetting: sub id invalid, skip modifying vt state in subinfo db; subId=" + subId);
            }
            try {
                changeMmTelCapability(2, 0, enabled);
                changeMmTelCapability(2, 1, enabled);
                if (enabled) {
                    log("setVtSetting(b) : turnOnIms");
                    turnOnIms();
                } else if (!isTurnOffImsAllowedByPlatform()) {
                } else {
                    if (!isVolteEnabledByPlatform() || !isEnhanced4gLteModeSettingEnabledByUser()) {
                        log("setVtSetting(b) : imsServiceAllowTurnOff -> turnOffIms");
                        turnOffIms();
                    }
                }
            } catch (ImsException e) {
                loge("setVtSetting(b): ", e);
            }
        } else {
            log("setVtSetting: Not possible to enable Vt due to provisioning.");
        }
    }

    public void setVtSettingOnly(boolean enabled) {
        SubscriptionManager.setSubscriptionProperty(getSubId(), "vt_ims_enabled", booleanToPropertyString(enabled));
    }

    public void setWfcNonPersistent(boolean enabled, int wfcMode) {
        MtkImsManager.super.setWfcNonPersistent(enabled, wfcMode);
        CapabilityChangeRequest request = new CapabilityChangeRequest();
        updateVideoCallFeatureValue(request);
        try {
            changeMmTelCapability(request);
        } catch (ImsException e) {
            loge("setWfcNonPersistent(): ", e);
        }
    }

    private boolean isConvertRoamingStateForSpecificOP() {
        TelephonyManager tm = (TelephonyManager) this.mContext.getSystemService("phone");
        boolean isWfcModeHomeForDomRoaming = getBooleanCarrierConfig("mtk_carrier_wfc_mode_domestic_roaming_to_home");
        log("isWfcModeHomeForDomRoaming:" + isWfcModeHomeForDomRoaming);
        if (!isWfcModeHomeForDomRoaming) {
            return false;
        }
        if (tm != null) {
            ServiceState ss = tm.getServiceStateForSubscriber(getSubId());
            if (ss != null) {
                int voiceRoamingType = ss.getVoiceRoamingType();
                int dataRoamingType = ss.getDataRoamingType();
                if (voiceRoamingType != 2 && dataRoamingType != 2) {
                    return false;
                }
                log("Convert roaming to HOME if it's domestic roaming,  voiceRoamingType: " + voiceRoamingType + " dataRoamingType: " + dataRoamingType);
                return DBG;
            }
            loge("isCellularDataRoaming(): ServiceState null");
            return false;
        }
        loge("isCellularDataRoaming(): TelephonyManager null");
        return false;
    }

    public boolean isWfcEnabledByPlatform() {
        if (SystemProperties.getInt("persist.dbg.wfc_avail_ovr" + Integer.toString(this.mPhoneId), -1) == 1 || SystemProperties.getInt("persist.dbg.wfc_avail_ovr", -1) == 1) {
            return DBG;
        }
        boolean isResourceSupport = isImsResourceSupport(2);
        boolean isCarrierConfigSupport = getBooleanCarrierConfig("carrier_wfc_ims_available_bool");
        boolean isGbaValidSupport = isGbaValid();
        boolean isFeatureEnableByPlatformExt = isFeatureEnabledByPlatformExt(2);
        log("Wfc, isResourceSupport:" + isResourceSupport + ", isCarrierConfigSupport:" + isCarrierConfigSupport + ", isGbaValidSupport:" + isGbaValidSupport + ", isFeatureEnableByPlatformExt:" + isFeatureEnableByPlatformExt);
        if (SystemProperties.getInt(PROPERTY_MTK_WFC_SUPPORT, 0) != 1 || !isLteSupported() || !isResourceSupport || !isCarrierConfigSupport || !isGbaValidSupport || !isFeatureEnableByPlatformExt) {
            return false;
        }
        return DBG;
    }

    private String getTtyModeSettingKeyForSlot() {
        if (this.mPhoneId == 1) {
            return PREFERRED_TTY_MODE_SIM2;
        }
        if (this.mPhoneId == 2) {
            return PREFERRED_TTY_MODE_SIM3;
        }
        if (this.mPhoneId == SIM_ID_4) {
            return PREFERRED_TTY_MODE_SIM4;
        }
        return PREFERRED_TTY_MODE;
    }

    private MtkImsManager(Context context, int phoneId) {
        super(context, phoneId);
        createMtkImsService(DBG);
        this.mOp15ImsManagerExt = new Op15ImsManagerExt(context);
    }

    public boolean isServiceAvailable() {
        if (!MtkImsManager.super.isServiceAvailable()) {
            logw("ImsService binder is not available and rebind again");
            createImsService();
        }
        IMtkImsService iMtkImsService = this.mMtkImsService;
        boolean available = DBG;
        if (iMtkImsService == null) {
            createMtkImsService(DBG);
        }
        if (this.mMtkImsService == null) {
            available = false;
        }
        log("isServiceAvailable=" + available);
        return available;
    }

    public void close() {
        log("close");
        MtkImsManager.super.close();
        this.mMtkUt = null;
    }

    public ImsUtInterface getSupplementaryServiceConfiguration() throws ImsException {
        MtkImsUt mtkImsUt = this.mMtkUt;
        if (mtkImsUt == null || !mtkImsUt.isBinderAlive()) {
            try {
                checkAndThrowExceptionIfServiceUnavailable();
                try {
                    IImsUt iUt = this.mMmTelFeatureConnection.getUtInterface();
                    IMtkImsUt iMtkUt = this.mMtkImsService.getMtkUtInterface(this.mPhoneId);
                    if (iUt != null) {
                        this.mMtkUt = new MtkImsUt(iUt, iMtkUt);
                    } else {
                        throw new ImsException("getSupplementaryServiceConfiguration()", 801);
                    }
                } catch (RemoteException e) {
                    throw new ImsException("getSupplementaryServiceConfiguration()", e, 106);
                }
            } catch (ImsException e2) {
                loge("getSupplementaryServiceConfiguration(): ", e2);
                return null;
            }
        }
        return this.mMtkUt;
    }

    public ImsCall makeCall(ImsCallProfile profile, String[] callees, ImsCall.Listener listener) throws ImsException {
        log("makeCall :: profile=" + profile + ", callees=" + sensitiveEncode(Arrays.toString(callees)));
        checkAndThrowExceptionIfServiceUnavailable();
        ImsCall call = new MtkImsCall(this.mContext, profile);
        call.setListener(listener);
        ImsCallSession session = createCallSession(profile);
        if (callees == null || callees.length != 1 || profile.getCallExtraBoolean("conference")) {
            call.start(session, callees);
        } else {
            call.start(session, callees[0]);
        }
        return call;
    }

    /* access modifiers changed from: protected */
    public void updateVideoCallFeatureValue(CapabilityChangeRequest request) {
        boolean ignoreDataEnabledChanged;
        Object obj;
        boolean available = isVtEnabledByPlatform();
        boolean vilteEnabled = (!isEnhanced4gLteModeSettingEnabledByUser() || !isVtEnabledByUser()) ? false : DBG;
        boolean isNonTty = isNonTtyOrTtyOnVolteEnabled();
        boolean isDataEnabled = isDataEnabled();
        if (isTestSim()) {
            ignoreDataEnabledChanged = isVTIgnoreDataChangedByOpid(SystemProperties.get("persist.vendor.operator.optr", "OM"));
        } else {
            ignoreDataEnabledChanged = getBooleanCarrierConfig("ignore_data_enabled_changed_for_video_calls");
        }
        boolean isDataRoamingEnable = (!isDataRoaming() || isDataRoamingSettingsEnabled()) ? DBG : false;
        boolean ignoreDataRoaming = getBooleanCarrierConfig("mtk_ignore_data_roaming_for_video_calls");
        boolean isCameraAvailable = DBG;
        try {
            if (this.mMtkImsService != null) {
                isCameraAvailable = this.mMtkImsService.isCameraAvailable();
            } else {
                log("mMtkImsService is not ready yet");
            }
        } catch (RemoteException e) {
            log("mMtkImsService exception");
        }
        boolean isVilteFeatureOn = (!available || !vilteEnabled || !isNonTty || !isCameraAvailable || (!ignoreDataEnabledChanged && (!isDataEnabled || (!ignoreDataRoaming && !isDataRoamingEnable)))) ? false : DBG;
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        sb.append(this.mPhoneId);
        sb.append("] updateVideoCallFeatureValue ViLTE: available = ");
        sb.append(available);
        sb.append(", vilteEnabled = ");
        sb.append(vilteEnabled);
        sb.append(", nonTTY = ");
        sb.append(isNonTty);
        sb.append(", ignoreDataEnabledChanged = ");
        sb.append(ignoreDataEnabledChanged);
        sb.append(", data enabled = ");
        sb.append(isDataEnabled);
        sb.append(", ignoreDataRoaming = ");
        sb.append(ignoreDataRoaming);
        sb.append(", data roaming enabled = ");
        sb.append(isDataRoamingEnable);
        sb.append(", camera support = ");
        sb.append(isCameraAvailable);
        sb.append(", is test sim = ");
        sb.append(isTestSim());
        sb.append(" (ignore data = ");
        if (!isTestSim()) {
            obj = "No need to check";
        } else {
            obj = Boolean.valueOf(isVTIgnoreDataChangedByOpid(SystemProperties.get("persist.vendor.operator.optr", "OM")));
        }
        sb.append(obj);
        sb.append(")");
        log(sb.toString());
        if (isVilteFeatureOn) {
            request.addCapabilitiesToEnableForTech(2, 0);
        } else {
            request.addCapabilitiesToDisableForTech(2, 0);
        }
        boolean isViWifiNeedCheckWfcEnabled = getBooleanCarrierConfig("mtk_vt_over_wifi_check_wfc_enable_bool");
        boolean isViWifiNeedCheckVolteEnabled = getBooleanCarrierConfig("mtk_vt_over_wifi_check_volte_enable_bool");
        boolean viwifiEnabled = (!isVtEnabledByUser() || (isViWifiNeedCheckVolteEnabled && !isEnhanced4gLteModeSettingEnabledByUser()) || ((isViWifiNeedCheckWfcEnabled && !isWfcEnabledByUser()) || !getBooleanCarrierConfig("config_oppo_support_viwifi_bool"))) ? false : DBG;
        boolean isViWifiNeedCheckDataEnabled = getBooleanCarrierConfig("mtk_vt_over_wifi_check_data_enable_bool");
        boolean isViwifiFeatureOn = (!available || !viwifiEnabled || !isNonTty || !isCameraAvailable || (isViWifiNeedCheckDataEnabled && !isDataEnabled)) ? false : DBG;
        log("[" + this.mPhoneId + "] updateVideoCallFeatureValue ViWiFi: available = " + available + ", viwifiEnabled = " + viwifiEnabled + ", isViWifiNeedCheckWfcEnabled = " + isViWifiNeedCheckWfcEnabled + ", isWfcEnabledByUser = " + isWfcEnabledByUser() + ", isViWifiNeedCheckDataEnabled = " + isViWifiNeedCheckDataEnabled + ", isViWifiNeedCheckVolteEnabled = " + isViWifiNeedCheckVolteEnabled);
        if (isViwifiFeatureOn) {
            request.addCapabilitiesToEnableForTech(2, 1);
        } else {
            request.addCapabilitiesToDisableForTech(2, 1);
        }
    }

    /* access modifiers changed from: protected */
    public void updateWfcFeatureAndProvisionedValues(CapabilityChangeRequest request) {
        boolean isNetworkRoaming = new TelephonyManager(this.mContext, getSubId()).isNetworkRoaming();
        boolean available = isWfcEnabledByPlatform();
        boolean enabled = isWfcEnabledByUser();
        if (isNetworkRoaming && isConvertRoamingStateForSpecificOP()) {
            isNetworkRoaming = false;
        }
        int mode = getWfcMode(isNetworkRoaming);
        boolean roaming = isWfcRoamingEnabledByUser();
        boolean isFeatureOn = available && enabled;
        log("updateWfcFeatureAndProvisionedValues: available = " + available + ", enabled = " + enabled + ", mode = " + mode + ", roaming = " + roaming);
        if (isFeatureOn) {
            request.addCapabilitiesToEnableForTech(1, 1);
        } else {
            request.addCapabilitiesToDisableForTech(1, 1);
        }
        if (!isFeatureOn) {
            mode = 1;
            roaming = false;
        }
        setWfcModeInternal(mode);
        setWfcRoamingSettingInternal(roaming);
    }

    private void checkAndThrowExceptionIfServiceUnavailable() throws ImsException {
        if (this.mMmTelFeatureConnection == null || !this.mMmTelFeatureConnection.isBinderAlive()) {
            createImsService();
            if (this.mMmTelFeatureConnection == null) {
                throw new ImsException("Service is unavailable", 106);
            }
        }
        if (this.mMtkImsService == null) {
            createMtkImsService(DBG);
            if (this.mMtkImsService == null) {
                throw new ImsException("MtkImsService is unavailable", 106);
            }
        }
    }

    private static String getMtkImsServiceName(int phoneId) {
        return "mtkIms";
    }

    private void createMtkImsService(boolean checkService) {
        if (!checkService || ServiceManager.checkService(getMtkImsServiceName(this.mPhoneId)) != null) {
            IBinder b = ServiceManager.getService(getMtkImsServiceName(this.mPhoneId));
            if (b != null) {
                try {
                    b.linkToDeath(this.mMtkDeathRecipient, 0);
                } catch (RemoteException e) {
                }
            }
            this.mMtkImsService = IMtkImsService.Stub.asInterface(b);
            log("mMtkImsService = " + this.mMtkImsService);
            return;
        }
        log("createMtkImsService binder is null");
    }

    /* access modifiers changed from: private */
    public static void log(String s) {
        Rlog.d(TAG, s);
    }

    private static void logw(String s) {
        Rlog.w(TAG, s);
    }

    /* access modifiers changed from: private */
    public static void loge(String s) {
        Rlog.e(TAG, s);
    }

    private static void loge(String s, Throwable t) {
        Rlog.e(TAG, s, t);
    }

    private static void logi(String s) {
        Rlog.i(TAG, s);
    }

    private static String sensitiveEncode(String s) {
        return Rlog.pii(TAG, s);
    }

    /* access modifiers changed from: private */
    public class MtkImsServiceDeathRecipient implements IBinder.DeathRecipient {
        private MtkImsServiceDeathRecipient() {
        }

        public void binderDied() {
            MtkImsManager.this.mMtkImsService = null;
            MtkImsManager.this.mMtkUt = null;
            MtkImsManager.this.mNotifyOnly = false;
            MtkImsManager.loge("[" + MtkImsManager.this.mPhoneId + "]MtkImsService binder died!");
        }
    }

    private String getCallNum(Intent incomingCallIntent) {
        if (incomingCallIntent == null) {
            return null;
        }
        return incomingCallIntent.getStringExtra(EXTRA_DIAL_STRING);
    }

    private int getSeqNum(Intent incomingCallIntent) {
        if (incomingCallIntent == null) {
            return -1;
        }
        return incomingCallIntent.getIntExtra(EXTRA_SEQ_NUM, -1);
    }

    private String getMtToNumber(Intent incomingCallIntent) {
        if (incomingCallIntent == null) {
            return null;
        }
        return incomingCallIntent.getStringExtra(EXTRA_MT_TO_NUMBER);
    }

    public void setCallIndication(int phoneId, Intent incomingCallIndication, boolean isAllow, int cause) throws ImsException {
        RemoteException e;
        log("setCallIndication :: phoneId=" + phoneId + ", incomingCallIndication=" + incomingCallIndication + ", isAllow=" + isAllow + ", cause=" + cause);
        checkAndThrowExceptionIfServiceUnavailable();
        if (incomingCallIndication == null) {
            throw new ImsException("Can't retrieve session with null intent", 101);
        } else if (phoneId == getPhoneId(incomingCallIndication)) {
            String callId = getCallId(incomingCallIndication.getExtras());
            if (callId != null) {
                String callNum = getCallNum(incomingCallIndication);
                if (callNum != null) {
                    int seqNum = getSeqNum(incomingCallIndication);
                    if (seqNum != -1) {
                        try {
                            try {
                                this.mMtkImsService.setCallIndication(phoneId, callId, callNum, seqNum, getMtToNumber(incomingCallIndication), isAllow, cause);
                            } catch (RemoteException e2) {
                                e = e2;
                            }
                        } catch (RemoteException e3) {
                            e = e3;
                            throw new ImsException("setCallIndication()", e, 106);
                        }
                    } else {
                        throw new ImsException("seqNum missing in the incoming call intent", 101);
                    }
                } else {
                    throw new ImsException("Call Num missing in the incoming call intent", 101);
                }
            } else {
                throw new ImsException("Call ID missing in the incoming call intent", 101);
            }
        } else {
            throw new ImsException("Service id is mismatched in the incoming call intent", 101);
        }
    }

    public void hangupAllCall(int phoneId) throws ImsException {
        checkAndThrowExceptionIfServiceUnavailable();
        try {
            this.mMtkImsService.hangupAllCall(phoneId);
        } catch (RemoteException e) {
            throw new ImsException("hangupAll()", e, 106);
        }
    }

    private static int getPhoneId(Intent incomingCallIntent) {
        if (incomingCallIntent == null) {
            return -1;
        }
        return incomingCallIntent.getIntExtra(EXTRA_PHONE_ID, -1);
    }

    /* access modifiers changed from: protected */
    public boolean isImsResourceSupport(int feature) {
        boolean support = DBG;
        log("isImsResourceSupport, feature:" + feature);
        if ("1".equals(SystemProperties.get(PROPERTY_DYNAMIC_IMS_SWITCH))) {
            if (!SubscriptionManager.isValidPhoneId(this.mPhoneId)) {
                loge("Invalid main phone " + this.mPhoneId + ", return true as don't care");
                return DBG;
            }
            try {
                MtkImsConfig config = getConfigInterfaceEx();
                if (config != null) {
                    int imsResCapability = config.getImsResCapability(feature);
                    boolean z = DBG;
                    if (imsResCapability != 1) {
                        z = false;
                    }
                    support = z;
                }
            } catch (ImsException e) {
                loge("isImsResourceSupport() failed!" + e);
            }
            log("isImsResourceSupport(" + feature + ") return " + support + " on phone: " + this.mPhoneId);
        }
        return support;
    }

    public void factoryReset() {
        MtkIccCardConstants.CardType cardType;
        boolean value = getBooleanCarrierConfig("enhanced_4g_lte_on_by_default_bool");
        if (SystemProperties.getInt(PROPERTY_CT_VOLTE_SUPPORT, 0) != 0) {
            SubscriptionInfo subInfo = SubscriptionManager.from(this.mContext).getActiveSubscriptionInfo(MtkSubscriptionManager.getSubIdUsingPhoneId(this.mPhoneId));
            String iccid = subInfo != null ? subInfo.getIccId() : null;
            if (!TextUtils.isEmpty(iccid) && isOp09SimCard(iccid) && (cardType = MtkTelephonyManagerEx.getDefault().getCdmaCardType(this.mPhoneId)) != null && !cardType.is4GCard()) {
                log("factoryReset, CT 3G card case");
                value = false;
            }
        }
        SubscriptionManager.setSubscriptionProperty(getSubId(), "volte_vt_enabled", booleanToPropertyString(value));
        SubscriptionManager.setSubscriptionProperty(getSubId(), "wfc_ims_enabled", booleanToPropertyString(getBooleanCarrierConfig("carrier_default_wfc_ims_enabled_bool")));
        SubscriptionManager.setSubscriptionProperty(getSubId(), "wfc_ims_mode", Integer.toString(getIntCarrierConfig("carrier_default_wfc_ims_mode_int")));
        SubscriptionManager.setSubscriptionProperty(getSubId(), "wfc_ims_roaming_enabled", booleanToPropertyString(getBooleanCarrierConfig("carrier_default_wfc_ims_roaming_enabled_bool")));
        SubscriptionManager.setSubscriptionProperty(getSubId(), "vt_ims_enabled", booleanToPropertyString(DBG));
        updateImsServiceConfig(DBG);
    }

    public MtkImsConfig getConfigInterfaceEx() throws ImsException {
        checkAndThrowExceptionIfServiceUnavailable();
        try {
            IMtkImsConfig binder = this.mMtkImsService.getConfigInterfaceEx(this.mPhoneId);
            if (binder != null) {
                return new MtkImsConfig(binder, this.mContext);
            }
            throw new ImsException("getConfigInterfaceEx()", 131);
        } catch (RemoteException e) {
            throw new ImsException("getConfigInterfaceEx()", e, 106);
        }
    }

    public ImsCall takeCall(IImsCallSession session, Bundle incomingCallExtras, ImsCall.Listener listener) throws ImsException {
        StringBuilder sb = new StringBuilder();
        sb.append("takeCall :: incomingCall=");
        sb.append(sensitiveEncode("" + incomingCallExtras));
        log(sb.toString());
        checkAndThrowExceptionIfServiceUnavailable();
        if (incomingCallExtras == null) {
            throw new ImsException("Can't retrieve session with null intent", 101);
        } else if (incomingCallExtras.getBoolean("android:ussd", false)) {
            log("takeCall :: isUssd = true, invoke original AOPS's takeCall()");
            return MtkImsManager.super.takeCall(session, incomingCallExtras, listener);
        } else {
            String callId = getCallId(incomingCallExtras);
            if (callId != null) {
                try {
                    IMtkImsCallSession mtkSession = this.mMtkImsService.getPendingMtkCallSession(this.mPhoneId, callId);
                    IImsCallSession session2 = mtkSession.getIImsCallSession();
                    ImsCallProfile callProfile = mtkSession.getCallProfile();
                    if (callProfile != null) {
                        ImsCall call = new MtkImsCall(this.mContext, callProfile);
                        call.attachSession(new MtkImsCallSession(session2, mtkSession));
                        call.setListener(listener);
                        return call;
                    }
                    throw new ImsException("takeCall(): profile is null", 0);
                } catch (Throwable t) {
                    throw new ImsException("takeCall()", t, 0);
                }
            } else {
                throw new ImsException("Call ID missing in the incoming call intent", 101);
            }
        }
    }

    /* access modifiers changed from: protected */
    public ImsCallSession createCallSession(ImsCallProfile profile) throws ImsException {
        try {
            log("createCallSession: profile = " + profile);
            ImsCallSession imsCallSession = MtkImsManager.super.createCallSession(profile);
            log("createCallSession: imsCallSession = " + imsCallSession);
            log("createCallSession: imsCallSession.getSession() = " + imsCallSession.getSession());
            return new MtkImsCallSession(imsCallSession.getSession(), this.mMtkImsService.createMtkCallSession(this.mPhoneId, profile, (IImsCallSessionListener) null, imsCallSession.getSession()));
        } catch (RemoteException e) {
            Rlog.w(TAG, "CreateCallSession: Error, remote exception: " + e.getMessage());
            throw new ImsException("createCallSession()", e, 106);
        }
    }

    /* access modifiers changed from: protected */
    public void setLteFeatureValues(boolean turnOn) {
        boolean ignoreDataEnabledChanged;
        log("setLteFeatureValues: " + turnOn);
        CapabilityChangeRequest request = new CapabilityChangeRequest();
        if (turnOn) {
            request.addCapabilitiesToEnableForTech(1, 0);
        } else {
            request.addCapabilitiesToDisableForTech(1, 0);
        }
        if (isVolteEnabledByPlatform() && isVtEnabledByPlatform()) {
            if (isTestSim()) {
                ignoreDataEnabledChanged = isVTIgnoreDataChangedByOpid(SystemProperties.get("persist.vendor.operator.optr", "OM"));
            } else {
                ignoreDataEnabledChanged = getBooleanCarrierConfig("ignore_data_enabled_changed_for_video_calls");
            }
            boolean isDataRoamingEnable = !isDataRoaming() || isDataRoamingSettingsEnabled();
            boolean ignoreDataRoaming = getBooleanCarrierConfig("mtk_ignore_data_roaming_for_video_calls");
            boolean isCameraAvailable = DBG;
            try {
                if (this.mMtkImsService != null) {
                    isCameraAvailable = this.mMtkImsService.isCameraAvailable();
                } else {
                    log("mMtkImsService is not ready yet");
                }
            } catch (RemoteException e) {
                Log.e(TAG, "setLteFeatureValues: isCameraAvailable() Exception: " + e.getMessage());
            }
            boolean enableViLte = turnOn && isVtEnabledByUser() && isCameraAvailable && (ignoreDataEnabledChanged || (isDataEnabled() && (ignoreDataRoaming || isDataRoamingEnable)));
            boolean enableViWifi = (isVtEnabledByUser() && ((!getBooleanCarrierConfig("mtk_vt_over_wifi_check_volte_enable_bool") || turnOn) && ((!getBooleanCarrierConfig("mtk_vt_over_wifi_check_wfc_enable_bool") || isWfcEnabledByUser()) && getBooleanCarrierConfig("config_oppo_support_viwifi_bool")))) && isCameraAvailable && (!getBooleanCarrierConfig("mtk_vt_over_wifi_check_data_enable_bool") || isDataEnabled());
            if (enableViLte) {
                request.addCapabilitiesToEnableForTech(2, 0);
            } else {
                request.addCapabilitiesToDisableForTech(2, 0);
            }
            if (enableViWifi) {
                request.addCapabilitiesToEnableForTech(2, 1);
            } else {
                request.addCapabilitiesToDisableForTech(2, 1);
            }
        }
        try {
            this.mMmTelFeatureConnection.changeEnabledCapabilities(request, (IImsCapabilityCallback) null);
        } catch (RemoteException e2) {
            Log.e(TAG, "setLteFeatureValues: Exception: " + e2.getMessage());
            changeMmTelCapabilityInternally(request);
        }
    }

    private boolean isFeatureEnabledByPlatformExt(int feature) {
        if (this.mContext == null) {
            logw("Invalid: context=" + this.mContext + ", return " + DBG);
            return DBG;
        }
        getImsManagerPluginInstance(this.mContext);
        Op15ImsManagerExt op15ImsManagerExt = this.mOp15ImsManagerExt;
        if (op15ImsManagerExt != null) {
            return op15ImsManagerExt.isFeatureEnabledByPlatform(this.mContext, feature, this.mPhoneId);
        }
        return DBG;
    }

    /* access modifiers changed from: protected */
    public int getMainCapabilityPhoneId(Context context) {
        return getMainPhoneIdForSingleIms(context);
    }

    private static IImsManagerExt getImsManagerPluginInstance(Context context) {
        log("getImsManagerPluginInstance");
        IImsManagerExt imsMgrExt = OpImsCustomizationUtils.getOpFactory(context).makeImsManagerExt(context);
        if (imsMgrExt == null) {
            log("Unable to create ImsManagerPluginInstane");
        }
        return imsMgrExt;
    }

    /* access modifiers changed from: protected */
    public boolean isVTIgnoreDataChangedByOpid(String optr) {
        if ("OP01".equals(optr) || "OP02".equals(optr) || "OP09".equals(optr) || "OP17".equals(optr) || "OP50".equals(optr) || "OP149".equals(optr) || "OP149".equals(optr)) {
            return DBG;
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean isTestSim() {
        int phoneId = this.mPhoneId;
        if (SystemProperties.getInt(MULTI_IMS_SUPPORT, 1) == 1) {
            phoneId = getMainCapabilityPhoneId(this.mContext);
        }
        boolean isTestSim = false;
        if (phoneId == 0) {
            isTestSim = "1".equals(SystemProperties.get(PROPERTY_TEST_SIM1, "0"));
        } else if (phoneId == 1) {
            isTestSim = "1".equals(SystemProperties.get(PROPERTY_TEST_SIM2, "0"));
        } else if (phoneId == 2) {
            isTestSim = "1".equals(SystemProperties.get(PROPERTY_TEST_SIM3, "0"));
        } else if (phoneId == SIM_ID_4) {
            isTestSim = "1".equals(SystemProperties.get(PROPERTY_TEST_SIM4, "0"));
        }
        if (!isTestSim) {
            return isTestSim;
        }
        String currentMccMnc = ((TelephonyManager) this.mContext.getSystemService("phone")).getSimOperatorNumericForPhone(phoneId);
        log("isTestSim, currentMccMnc:" + currentMccMnc);
        if (currentMccMnc == null || currentMccMnc.equals("") || "00101".equals(currentMccMnc) || "11111".equals(currentMccMnc) || "46011".equals(currentMccMnc)) {
            return isTestSim;
        }
        return false;
    }

    private boolean isOp09SimCard(String iccId) {
        String currentMccMnc;
        if (iccId.startsWith("898603") || iccId.startsWith("898611") || iccId.startsWith("8985302") || iccId.startsWith("8985307") || iccId.startsWith("8985231")) {
            return DBG;
        }
        if (!iccId.startsWith("894900")) {
            return false;
        }
        if (this.mPhoneId == 0) {
            currentMccMnc = SystemProperties.get("vendor.gsm.ril.uicc.mccmnc", "");
        } else {
            currentMccMnc = SystemProperties.get("vendor.gsm.ril.uicc.mccmnc." + this.mPhoneId, "");
        }
        log("isOp09SimCard, currentMccMnc:" + currentMccMnc);
        if (currentMccMnc == null) {
            return false;
        }
        if ("46011".equals(currentMccMnc) || "46003".equals(currentMccMnc)) {
            return DBG;
        }
        return false;
    }

    private static MtkImsManager getAppropriateManagerForPlugin(Context context, int phoneId) {
        IImsManagerExt imsMgrExt = getImsManagerPluginInstance(context);
        if (imsMgrExt != null) {
            phoneId = imsMgrExt.getImsPhoneId(context, phoneId);
        }
        return ImsManager.getInstance(context, phoneId);
    }

    public void notifyRegServiceCapabilityChangedEvent(int event) {
        if (event == 2) {
            synchronized (this.mCallbacks) {
                Iterator<ImsMmTelManager.RegistrationCallback> it = this.mCallbacks.iterator();
                while (it.hasNext()) {
                    ImsMmTelManager.RegistrationCallback callback = it.next();
                    if (callback instanceof MtkImsConnectionStateListener) {
                        ((MtkImsConnectionStateListener) callback).onImsEmergencyCapabilityChanged(DBG);
                    }
                }
            }
        } else if (event == 4) {
            synchronized (this.mCallbacks) {
                Iterator<ImsMmTelManager.RegistrationCallback> it2 = this.mCallbacks.iterator();
                while (it2.hasNext()) {
                    ImsMmTelManager.RegistrationCallback callback2 = it2.next();
                    if (callback2 instanceof MtkImsConnectionStateListener) {
                        ((MtkImsConnectionStateListener) callback2).onImsEmergencyCapabilityChanged(false);
                    }
                }
            }
        } else if (event == 5) {
            synchronized (this.mCallbacks) {
                Iterator<ImsMmTelManager.RegistrationCallback> it3 = this.mCallbacks.iterator();
                while (it3.hasNext()) {
                    ImsMmTelManager.RegistrationCallback callback3 = it3.next();
                    if (callback3 instanceof MtkImsConnectionStateListener) {
                        ((MtkImsConnectionStateListener) callback3).onWifiPdnOOSStateChanged(1);
                    }
                }
            }
        } else if (event == 6) {
            synchronized (this.mCallbacks) {
                Iterator<ImsMmTelManager.RegistrationCallback> it4 = this.mCallbacks.iterator();
                while (it4.hasNext()) {
                    ImsMmTelManager.RegistrationCallback callback4 = it4.next();
                    if (callback4 instanceof MtkImsConnectionStateListener) {
                        ((MtkImsConnectionStateListener) callback4).onWifiPdnOOSStateChanged(0);
                    }
                }
            }
        } else if (event == 7) {
            synchronized (this.mCallbacks) {
                Iterator<ImsMmTelManager.RegistrationCallback> it5 = this.mCallbacks.iterator();
                while (it5.hasNext()) {
                    ImsMmTelManager.RegistrationCallback callback5 = it5.next();
                    if (callback5 instanceof MtkImsConnectionStateListener) {
                        ((MtkImsConnectionStateListener) callback5).onWifiPdnOOSStateChanged(2);
                    }
                }
            }
        }
    }

    private boolean isLteSupported() {
        return SystemProperties.get("ro.vendor.mtk_ps1_rat", "").contains("L");
    }

    private static int getFeaturePropValue(String propName, int phoneId) {
        int featureValue = SystemProperties.getInt(propName, 0);
        int propResult = 0;
        if (isSupportMims()) {
            if (((1 << phoneId) & featureValue) > 0) {
                propResult = 1;
            }
            return propResult;
        }
        if ((featureValue & 1) > 0) {
            propResult = 1;
        }
        return propResult;
    }

    private void setComboFeatureValue(int volte_en, int vt_en, int wfc_en) {
        int[] features = {0, 1, SIM_ID_4, 2};
        int[] networks = {13, 13, 18, 18};
        int[] setvalues = {0, 0, 0, 0};
        int oldvolteValue = getFeaturePropValue(PROPERTY_VOLTE_ENALBE, this.mPhoneId);
        int oldvilteValue = getFeaturePropValue(PROPERTY_VILTE_ENALBE, this.mPhoneId);
        int oldviwifiValue = getFeaturePropValue(PROPERTY_VIWIFI_ENALBE, this.mPhoneId);
        int oldWfcValue = getFeaturePropValue(PROPERTY_WFC_ENALBE, this.mPhoneId);
        setvalues[0] = volte_en != -1 ? volte_en : oldvolteValue;
        setvalues[1] = vt_en != -1 ? vt_en : oldvilteValue;
        setvalues[2] = vt_en != -1 ? vt_en : oldviwifiValue;
        setvalues[SIM_ID_4] = wfc_en != -1 ? wfc_en : oldWfcValue;
        try {
            MtkImsConfig config = ((MtkImsManager) ImsManager.getInstance(this.mContext, this.mPhoneId)).getConfigInterfaceEx();
            if (config != null) {
                config.setMultiFeatureValues(features, networks, setvalues, this.mImsConfigListener);
            }
        } catch (ImsException e) {
            loge("setComboFeatureValue(): " + e);
        }
    }

    public void setEnhanced4gLteModeVtSetting(Context context, boolean e4genabled, boolean vtenabled) {
        int prevSetting;
        boolean ignoreDataEnabledChanged;
        int volte_value = e4genabled ? 1 : 0;
        boolean enableViLte = false;
        ImsManager imsManager = ImsManager.getInstance(context, this.mPhoneId);
        if (imsManager != null) {
            try {
                imsManager.getConfigInterface();
                int i = 1;
                if (!isSupportMims()) {
                    int prevSetting2 = isEnhanced4gLteModeSettingEnabledByUser() ? 1 : 0;
                    SubscriptionManager.setSubscriptionProperty(getSubId(), "volte_vt_enabled", Integer.toString(volte_value));
                    setVtSettingOnly(vtenabled);
                    prevSetting = prevSetting2;
                } else {
                    int value = getBooleanCarrierConfig("editable_enhanced_4g_lte_bool") ? volte_value : 1;
                    prevSetting = SubscriptionManager.getIntegerSubscriptionProperty(getSubId(), "volte_vt_enabled", -1, this.mContext);
                    if (prevSetting == value && SystemProperties.getInt(PROPERTY_IMSCONFIG_FORCE_NOTIFY, 0) == 0) {
                        volte_value = prevSetting;
                    } else {
                        SubscriptionManager.setSubscriptionProperty(getSubId(), "volte_vt_enabled", Integer.toString(volte_value));
                    }
                    SubscriptionManager.setSubscriptionProperty(getSubId(), "volte_vt_enabled", Integer.toString(volte_value));
                    setVtSettingOnly(vtenabled);
                }
                if (!isNonTtyOrTtyOnVolteEnabled(context, this.mPhoneId)) {
                    volte_value = prevSetting;
                } else if (isVolteEnabledByPlatform() && isVtEnabledByPlatform()) {
                    if (isTestSim()) {
                        ignoreDataEnabledChanged = isVTIgnoreDataChangedByOpid(SystemProperties.get("persist.vendor.operator.optr", "OM"));
                    } else {
                        ignoreDataEnabledChanged = getBooleanCarrierConfig("ignore_data_enabled_changed_for_video_calls");
                    }
                    enableViLte = e4genabled && isVtEnabledByUser() && (ignoreDataEnabledChanged || isDataEnabled());
                }
                if (!vtenabled || !enableViLte) {
                    i = 0;
                }
                setComboFeatureValue(volte_value, i, -1);
                if (e4genabled || vtenabled) {
                    log("setEnhanced4gLteModeVtSetting() : turnOnIms");
                    turnOnIms();
                } else if (!ImsManager.isTurnOffImsAllowedByPlatform(context)) {
                } else {
                    if (imsManager.isVolteEnabledByPlatform() && isEnhanced4gLteModeSettingEnabledByUser()) {
                        return;
                    }
                    if (!imsManager.isWfcEnabledByPlatform() || !isWfcEnabledByUser()) {
                        log("setEnhanced4gLteModeVtSetting() : imsServiceAllowTurnOff -> turnOffIms");
                        turnOffIms();
                    }
                }
            } catch (ImsException e) {
                loge("setEnhanced4gLteModeVtSetting error");
            }
        } else {
            loge("setEnhanced4gLteModeVtSetting error");
            loge("getInstance null for phoneId=" + this.mPhoneId);
        }
    }

    public MmTelFeature.MmTelCapabilities queryCapabilityStatus() {
        StringBuilder sb;
        MmTelFeature.MmTelCapabilities capabilities = null;
        try {
            capabilities = this.mMmTelFeatureConnection.queryCapabilityStatus();
            sb = new StringBuilder();
        } catch (RemoteException e) {
            loge("Fail to queryCapabilityStatus " + e.getMessage());
            capabilities = new MmTelFeature.MmTelCapabilities();
            sb = new StringBuilder();
        } catch (Throwable th) {
            sb = new StringBuilder();
        }
        sb.append("queryCapabilityStatus = ");
        sb.append(capabilities);
        log(sb.toString());
        return capabilities;
    }

    /* access modifiers changed from: protected */
    public boolean shouldEnableImsForIR() {
        if (!mSupportImsiSwitch) {
            log("[IR] IMSI switch feature not supported");
            return DBG;
        }
        boolean enableIms = DBG;
        int subId = MtkSubscriptionManager.getSubIdUsingPhoneId(this.mPhoneId);
        if (subId == -1) {
            log("[IR] shouldEnableImsForIR: Invalid subId so return");
            return DBG;
        }
        String permanentMccMnc = getOperatorNumericFromImpi("0");
        String currentMccMnc = getMccMncForSubId(subId, SubscriptionManager.from(this.mContext));
        if (!permanentMccMnc.equals(currentMccMnc) && !"0".equals(permanentMccMnc)) {
            enableIms = false;
        }
        log("[IR] updateVolteFeatureValue: subId = " + subId + ", phoneId = " + this.mPhoneId + ", Current currentMccMnc = " + currentMccMnc + ", permanentMccMnc = " + permanentMccMnc + ", enableIms = " + enableIms);
        return enableIms;
    }

    private String getOperatorNumericFromImpi(String defaultValue) {
        String[] mImsMccMncList = {"405840", "405854", "405855", "405856", "405857", "405858", "405859", "405860", "405861", "405862", "405863", "405864", "405865", "405866", "405867", "405868", "405869", "405870", "405871", "405872", "405873", "405874"};
        if (mImsMccMncList.length == 0) {
            log("[IR] mImsMccMncList is null, returning default mccmnc");
            return defaultValue;
        }
        log("[IR] IMPI requested by phoneId: " + this.mPhoneId);
        String impi = getIsimImpi(MtkSubscriptionManager.getSubIdUsingPhoneId(this.mPhoneId));
        log("[IR] IMPI : " + impi);
        if (impi == null || impi.equals("")) {
            log("[IR] impi is null/empty, returning default mccmnc");
            return defaultValue;
        }
        int mccPosition = impi.indexOf("mcc");
        int mncPosition = impi.indexOf("mnc");
        if (mccPosition == -1 || mncPosition == -1) {
            log("[IR] mcc/mnc position -1, returning default mccmnc");
            return defaultValue;
        }
        String masterMccMnc = impi.substring("mcc".length() + mccPosition, "mcc".length() + mccPosition + SIM_ID_4) + impi.substring("mnc".length() + mncPosition, "mnc".length() + mncPosition + SIM_ID_4);
        log("[IR] MccMnc fetched from IMPI: " + masterMccMnc);
        if (masterMccMnc == null || masterMccMnc.equals("")) {
            log("[IR] IMPI MccMnc is null/empty, Returning default mccmnc: " + defaultValue);
            return defaultValue;
        }
        for (String mccMnc : mImsMccMncList) {
            if (masterMccMnc.equals(mccMnc)) {
                log("[IR] mccMnc matched, Returning mccmnc from IMPI: " + masterMccMnc);
                return masterMccMnc;
            }
        }
        log("[IR] IMPI mcc/mnc not matched, returning default mccmnc");
        return defaultValue;
    }

    private IMtkPhoneSubInfoEx getMtkSubscriberInfoEx() {
        return IMtkPhoneSubInfoEx.Stub.asInterface(ServiceManager.getService("iphonesubinfoEx"));
    }

    private String getIsimImpi(int subId) {
        if (subId == -1) {
            log("[IR] getIsimImpi: Invalid subId so return");
            return null;
        }
        try {
            return getMtkSubscriberInfoEx().getIsimImpiForSubscriber(subId);
        } catch (RemoteException e) {
            return null;
        } catch (NullPointerException e2) {
            return null;
        }
    }

    private static String getMccMncForSubId(int subId, SubscriptionManager subsMgr) {
        String mccMnc = TelephonyManager.getDefault().getSimOperator(subId);
        if (mccMnc == null || mccMnc.length() <= 0) {
            List<SubscriptionInfo> sil = subsMgr.getActiveSubscriptionInfoList();
            log("[IR] Getting mcc mnc from from subinfo for subId = " + subId);
            if (sil != null && sil.size() > 0) {
                for (SubscriptionInfo subInfo : sil) {
                    if (subInfo.getSubscriptionId() == subId) {
                        int mcc = subInfo.getMcc();
                        int mnc = subInfo.getMnc();
                        String mccMnc2 = String.valueOf(mcc) + String.valueOf(mnc);
                        log("[IR] getMccMncForSubId from subInfo = " + mccMnc2);
                        return mccMnc2;
                    }
                }
            }
            return mccMnc;
        }
        log("[IR] Getting mcc mnc from TelephonyManager.getSimOperator");
        return mccMnc;
    }

    private void hookProprietaryImsListener() throws ImsException {
        if (this.mMtkImsService == null) {
            log("hookProprietaryImsListener get NULL mMtkImsService so create it");
            createMtkImsService(DBG);
        }
        if (this.mListener == null) {
            log("[" + this.mPhoneId + "] hook proprietary IMS listener");
            this.mNotifyOnly = false;
            this.mListener = new IImsRegistrationListener.Stub() {
                /* class com.mediatek.ims.internal.MtkImsManager.AnonymousClass1 */

                public void registrationConnected() throws RemoteException {
                }

                public void registrationProgressing() throws RemoteException {
                }

                public void registrationConnectedWithRadioTech(int imsRadioTech) throws RemoteException {
                    MtkImsManager.log("registrationConnectedWithRadioTech :: imsRadioTech=" + imsRadioTech);
                    synchronized (MtkImsManager.this.mCallbacks) {
                        Iterator it = MtkImsManager.this.mCallbacks.iterator();
                        while (it.hasNext()) {
                            ImsMmTelManager.RegistrationCallback callback = (ImsMmTelManager.RegistrationCallback) it.next();
                            if (callback instanceof MtkImsConnectionStateListener) {
                                ((MtkImsConnectionStateListener) callback).onImsConnected(imsRadioTech);
                            }
                        }
                    }
                }

                public void registrationProgressingWithRadioTech(int imsRadioTech) throws RemoteException {
                }

                public void registrationDisconnected(ImsReasonInfo imsReasonInfo) throws RemoteException {
                    synchronized (MtkImsManager.this.mCallbacks) {
                        Iterator it = MtkImsManager.this.mCallbacks.iterator();
                        while (it.hasNext()) {
                            ImsMmTelManager.RegistrationCallback callback = (ImsMmTelManager.RegistrationCallback) it.next();
                            if (callback instanceof MtkImsConnectionStateListener) {
                                ((MtkImsConnectionStateListener) callback).onImsDisconnected(imsReasonInfo);
                            }
                        }
                    }
                }

                public void registrationResumed() throws RemoteException {
                }

                public void registrationSuspended() throws RemoteException {
                }

                public void registrationServiceCapabilityChanged(int serviceClass, int event) throws RemoteException {
                    MtkImsManager.this.notifyRegServiceCapabilityChangedEvent(event);
                }

                public void registrationFeatureCapabilityChanged(int serviceClass, int[] enabledFeatures, int[] disabledFeatures) throws RemoteException {
                    MmTelFeature.MmTelCapabilities capabilities = MtkImsManager.this.convertCapabilities(enabledFeatures);
                    MtkImsManager.log("registrationFeatureCapabilityChanged :: enabledFeatures=" + capabilities);
                    synchronized (MtkImsManager.this.mCallbacks) {
                        Iterator it = MtkImsManager.this.mCallbacks.iterator();
                        while (it.hasNext()) {
                            ImsMmTelManager.RegistrationCallback callback = (ImsMmTelManager.RegistrationCallback) it.next();
                            if (callback instanceof MtkImsConnectionStateListener) {
                                ((MtkImsConnectionStateListener) callback).onCapabilitiesStatusChanged(capabilities);
                            }
                        }
                    }
                }

                public void voiceMessageCountUpdate(int count) throws RemoteException {
                }

                public void registrationAssociatedUriChanged(Uri[] uris) throws RemoteException {
                }

                public void registrationChangeFailed(int targetAccessTech, ImsReasonInfo imsReasonInfo) throws RemoteException {
                }
            };
        } else {
            log("mListener was created");
        }
        if (this.mMtkImsListener == null) {
            this.mMtkImsListener = new IMtkImsRegistrationListener.Stub() {
                /* class com.mediatek.ims.internal.MtkImsManager.AnonymousClass2 */

                public void onRegistrationImsStateChanged(int state, Uri[] uris, int expireTime, ImsReasonInfo imsReasonInfo) throws RemoteException {
                    MtkImsManager.log("onRegistrationImsStateChanged, state: " + state + ", uri: " + uris + ", expireTime: " + expireTime + ", imsReasonInfo: " + imsReasonInfo);
                    synchronized (MtkImsManager.this.mCallbacks) {
                        Iterator it = MtkImsManager.this.mCallbacks.iterator();
                        while (it.hasNext()) {
                            ImsMmTelManager.RegistrationCallback callback = (ImsMmTelManager.RegistrationCallback) it.next();
                            if (callback instanceof MtkImsConnectionStateListener) {
                                ((MtkImsConnectionStateListener) callback).onRegistrationImsStateInd(state, uris, expireTime, imsReasonInfo.getCode(), imsReasonInfo.getExtraMessage());
                            }
                        }
                    }
                }

                public void onRedirectIncomingCallIndication(int phoneId, String[] info) {
                    MtkImsManager.log("redirectIncomingCallIndication, phoneId: " + phoneId + ", info: " + info);
                    synchronized (MtkImsManager.this.mCallbacks) {
                        Iterator it = MtkImsManager.this.mCallbacks.iterator();
                        while (it.hasNext()) {
                            ImsMmTelManager.RegistrationCallback callback = (ImsMmTelManager.RegistrationCallback) it.next();
                            if (callback instanceof MtkImsConnectionStateListener) {
                                ((MtkImsConnectionStateListener) callback).onRedirectIncomingCallInd(phoneId, info);
                            }
                        }
                    }
                }
            };
        } else {
            log("mMtkListener was created");
        }
        try {
            if (this.mMtkImsService != null) {
                this.mMtkImsService.registerProprietaryImsListener(this.mPhoneId, this.mListener, this.mMtkImsListener, this.mNotifyOnly);
                this.mNotifyOnly = DBG;
                return;
            }
            log("mMtkImsService is not ready yet");
        } catch (RemoteException e) {
            throw new ImsException("registerProprietaryImsListener(listener)", e, 106);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private MmTelFeature.MmTelCapabilities convertCapabilities(int[] enabledFeatures) {
        boolean[] featuresEnabled = new boolean[enabledFeatures.length];
        int i = 0;
        while (i <= 5 && i < enabledFeatures.length) {
            if (enabledFeatures[i] == i) {
                featuresEnabled[i] = DBG;
            } else if (enabledFeatures[i] == -1) {
                featuresEnabled[i] = false;
            }
            i++;
        }
        MmTelFeature.MmTelCapabilities capabilities = new MmTelFeature.MmTelCapabilities();
        if (featuresEnabled[0] || featuresEnabled[2]) {
            capabilities.addCapabilities(1);
        }
        if (featuresEnabled[1] || featuresEnabled[SIM_ID_4]) {
            capabilities.addCapabilities(2);
        }
        if (featuresEnabled[4] || featuresEnabled[5]) {
            capabilities.addCapabilities(4);
        }
        return capabilities;
    }

    public void addImsConnectionStateListener(ImsMmTelManager.RegistrationCallback callback) throws ImsException {
        synchronized (this.mCallbacks) {
            log("ImsConnectionStateListener added: " + callback);
            this.mCallbacks.add(callback);
            hookProprietaryImsListener();
        }
    }

    public void removeImsConnectionStateListener(ImsMmTelManager.RegistrationCallback callback) throws ImsException {
        synchronized (this.mCallbacks) {
            this.mCallbacks.remove(callback);
            log("ImsConnectionStateListener removed: " + callback + ", size: " + this.mCallbacks.size());
        }
    }

    private boolean isPhoneIdSupportIms(int phoneId) {
        int isImsSupport = SystemProperties.getInt(PROPERTY_IMS_SUPPORT, 0);
        int mimsCount = SystemProperties.getInt(MULTI_IMS_SUPPORT, 1);
        if (TelephonyManager.getDefault().getMultiSimConfiguration() != TelephonyManager.MultiSimVariants.TSTS) {
            return DBG;
        }
        if (isImsSupport == 0 || !SubscriptionManager.isValidPhoneId(phoneId)) {
            log("[" + phoneId + "] isPhoneIdSupportIms, not support IMS");
            return false;
        } else if (mimsCount != 1) {
            int protocalStackId = MtkTelephonyManagerEx.getDefault().getProtocolStackId(phoneId);
            log("isPhoneIdSupportIms(), mimsCount:" + mimsCount + ", phoneId:" + phoneId + ", protocalStackId:" + protocalStackId + ", MainCapabilityPhoneId:" + getMainCapabilityPhoneId(this.mContext));
            if (protocalStackId <= mimsCount) {
                return DBG;
            }
            return false;
        } else if (getMainCapabilityPhoneId(this.mContext) == phoneId) {
            return DBG;
        } else {
            return false;
        }
    }

    private boolean isCellularDataRoaming() {
        TelephonyManager tm = (TelephonyManager) this.mContext.getSystemService("phone");
        if (tm == null) {
            loge("isCellularDataRoaming(): TelephonyManager null");
            return false;
        }
        ServiceState ss = tm.getServiceStateForSubscriber(getSubId());
        if (ss == null) {
            loge("isCellularDataRoaming(): ServiceState null");
            return false;
        }
        if (ss instanceof MtkServiceState) {
            MtkServiceState mtkSs = (MtkServiceState) ss;
            int regState = mtkSs.getCellularDataRegState();
            boolean isDataroaming = mtkSs.getCellularDataRoaming();
            log("isCellularDataRoaming(): regState = " + regState + ", isDataroaming = " + isDataroaming);
            if (regState != 0 || !isDataroaming) {
                return false;
            }
            return DBG;
        }
        loge("isCellularDataRoaming(): not MtkServiceState");
        return false;
    }

    public void setMTRedirect(boolean enable) throws ImsException {
        log("setMTRedirect: " + enable);
        checkAndThrowExceptionIfServiceUnavailable();
        try {
            this.mMtkImsService.setMTRedirect(this.mPhoneId, enable);
        } catch (RemoteException e) {
            throw new ImsException("setMTRedirect()", e, 106);
        }
    }

    public void fallBackAospMTFlow() throws ImsException {
        log("fallBackAospMTFlow");
        checkAndThrowExceptionIfServiceUnavailable();
        try {
            this.mMtkImsService.fallBackAospMTFlow(this.mPhoneId);
        } catch (RemoteException e) {
            throw new ImsException("fallBackAospMTFlow()", e, 106);
        }
    }

    public void setSipHeader(HashMap<String, String> extraHeaders, String fromUri) throws ImsException {
        log("setSipHeader fromUri: " + sensitiveEncode(fromUri) + ", extraHeaders: " + extraHeaders);
        checkAndThrowExceptionIfServiceUnavailable();
        try {
            this.mMtkImsService.setSipHeader(this.mPhoneId, extraHeaders, fromUri);
        } catch (RemoteException e) {
            throw new ImsException("setSipHeader()", e, 106);
        }
    }

    public void setCallIndication(int phoneId, String callId, String callNum, int seqNum, String toNumber, boolean isAllow) throws ImsException {
        log("setCallIndication phoneId:" + phoneId + ", callId:" + callId + ",callNum:" + sensitiveEncode(callNum) + ",seqNum:" + seqNum + ",toNumber:" + sensitiveEncode(toNumber) + ",isAllow:" + isAllow);
        checkAndThrowExceptionIfServiceUnavailable();
        try {
            this.mMtkImsService.setCallIndication(phoneId, callId, callNum, seqNum, toNumber, isAllow, -1);
        } catch (RemoteException e) {
            throw new ImsException("setCallIndication()", e, 106);
        }
    }

    public IMtkImsCallSession getPendingMtkCallSession(String callId) throws ImsException {
        log("getPendingMtkCallSession callId: " + callId);
        checkAndThrowExceptionIfServiceUnavailable();
        try {
            return this.mMtkImsService.getPendingMtkCallSession(this.mPhoneId, callId);
        } catch (RemoteException e) {
            throw new ImsException("getPendingMtkCallSession()", e, 106);
        }
    }

    public ImsCallSession createCallSession(int serviceType, int callType) throws ImsException {
        try {
            ImsCallProfile profile = new ImsCallProfile(serviceType, callType);
            log("createCallSession: profile = " + profile);
            ImsCallSession imsCallSession = MtkImsManager.super.createCallSession(profile);
            log("createCallSession: imsCallSession = " + imsCallSession);
            log("createCallSession: imsCallSession.getSession() = " + imsCallSession.getSession());
            return new MtkImsCallSession(imsCallSession.getSession(), this.mMtkImsService.createMtkCallSession(this.mPhoneId, profile, (IImsCallSessionListener) null, imsCallSession.getSession()));
        } catch (RemoteException e) {
            Rlog.w(TAG, "CreateCallSession: Error, remote exception: " + e.getMessage());
            throw new ImsException("createCallSession()", e, 106);
        }
    }

    /* access modifiers changed from: protected */
    public void changeMmTelCapabilityInternally(CapabilityChangeRequest r) {
        try {
            if (this.mMtkImsService != null) {
                log("[" + this.mPhoneId + "] changeMmTelCapabilityInternally " + r);
                this.mMtkImsService.changeEnabledCapabilities(this.mPhoneId, r);
            }
        } catch (RemoteException e) {
            loge("Fail to changeMmTelCapabilityInternally " + e);
        }
    }

    private int getSubId(int phoneId) {
        int[] subIds = SubscriptionManager.getSubId(phoneId);
        if (subIds == null || subIds.length < 1) {
            return -1;
        }
        return subIds[0];
    }

    public MtkImsConfig oppoGetConfigInterface(int phoneId, Context context) throws ImsException {
        return getInstance(context, phoneId).getConfigInterfaceEx();
    }
}
