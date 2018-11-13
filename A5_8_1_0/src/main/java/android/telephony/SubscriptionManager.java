package android.telephony;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.INetworkPolicyManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import com.android.internal.telephony.IOnSubscriptionsChangedListener;
import com.android.internal.telephony.IOnSubscriptionsChangedListener.Stub;
import com.android.internal.telephony.ISub;
import com.android.internal.telephony.ITelephonyRegistry;
import com.android.internal.telephony.PhoneConstants;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.codeaurora.internal.IExtTelephony;

public class SubscriptionManager {
    public static final String ACCESS_RULES = "access_rules";
    public static final String ACTION_DEFAULT_SMS_SUBSCRIPTION_CHANGED = "android.telephony.action.DEFAULT_SMS_SUBSCRIPTION_CHANGED";
    public static final String ACTION_DEFAULT_SUBSCRIPTION_CHANGED = "android.telephony.action.DEFAULT_SUBSCRIPTION_CHANGED";
    public static final int CARD_NOT_PRESENT = -2;
    public static final String CARRIER_NAME = "carrier_name";
    public static final String CB_ALERT_REMINDER_INTERVAL = "alert_reminder_interval";
    public static final String CB_ALERT_SOUND_DURATION = "alert_sound_duration";
    public static final String CB_ALERT_SPEECH = "enable_alert_speech";
    public static final String CB_ALERT_VIBRATE = "enable_alert_vibrate";
    public static final String CB_AMBER_ALERT = "enable_cmas_amber_alerts";
    public static final String CB_CHANNEL_50_ALERT = "enable_channel_50_alerts";
    public static final String CB_CMAS_TEST_ALERT = "enable_cmas_test_alerts";
    public static final String CB_EMERGENCY_ALERT = "enable_emergency_alerts";
    public static final String CB_ETWS_TEST_ALERT = "enable_etws_test_alerts";
    public static final String CB_EXTREME_THREAT_ALERT = "enable_cmas_extreme_threat_alerts";
    public static final String CB_OPT_OUT_DIALOG = "show_cmas_opt_out_dialog";
    public static final String CB_SEVERE_THREAT_ALERT = "enable_cmas_severe_threat_alerts";
    public static final String COLOR = "color";
    public static final int COLOR_1 = 0;
    public static final int COLOR_2 = 1;
    public static final int COLOR_3 = 2;
    public static final int COLOR_4 = 3;
    public static final int COLOR_DEFAULT = 0;
    public static final Uri CONTENT_URI = Uri.parse("content://telephony/siminfo");
    public static final String DATA_ROAMING = "data_roaming";
    public static final int DATA_ROAMING_DEFAULT = 0;
    public static final int DATA_ROAMING_DISABLE = 0;
    public static final int DATA_ROAMING_ENABLE = 1;
    private static final boolean DBG = false;
    public static final int DEFAULT_NAME_RES = 17039374;
    public static final int DEFAULT_PHONE_INDEX = Integer.MAX_VALUE;
    public static final int DEFAULT_SIM_SLOT_INDEX = Integer.MAX_VALUE;
    public static final int DEFAULT_SUBSCRIPTION_ID = Integer.MAX_VALUE;
    public static final String DISPLAY_NAME = "display_name";
    public static final int DISPLAY_NUMBER_DEFAULT = 1;
    public static final int DISPLAY_NUMBER_FIRST = 1;
    public static final String DISPLAY_NUMBER_FORMAT = "display_number_format";
    public static final int DISPLAY_NUMBER_LAST = 2;
    public static final int DISPLAY_NUMBER_NONE = 0;
    public static final int DUMMY_SUBSCRIPTION_ID_BASE = -2;
    public static final int ERROR_GENERAL = -1;
    public static final int ERROR_NAME_EXIST = -2;
    public static final String EXTRA_SUBSCRIPTION_INDEX = "android.telephony.extra.SUBSCRIPTION_INDEX";
    public static final String ICC_ID = "icc_id";
    public static final int INVALID_PHONE_INDEX = -1;
    public static final int INVALID_SIM_SLOT_INDEX = -1;
    public static final int INVALID_SLOT_ID = -1000;
    public static final int INVALID_STATE = -1;
    public static final int INVALID_SUBSCRIPTION_ID = -1;
    public static final int INVALID_SUB_ID = -1000;
    public static final String IS_EMBEDDED = "is_embedded";
    public static final String IS_REMOVABLE = "is_removable";
    private static final String LOG_TAG = "SubscriptionManager";
    public static final int MAX_SUBSCRIPTION_ID_VALUE = 2147483646;
    public static final String MCC = "mcc";
    public static final int MIN_SUBSCRIPTION_ID_VALUE = 0;
    public static final String MNC = "mnc";
    public static final String NAME_SOURCE = "name_source";
    public static final int NAME_SOURCE_DEFAULT_SOURCE = 0;
    public static final int NAME_SOURCE_SIM_SOURCE = 1;
    public static final int NAME_SOURCE_UNDEFINDED = -1;
    public static final int NAME_SOURCE_USER_INPUT = 2;
    public static final int NOT_PROVISIONED = 0;
    public static final String NUMBER = "number";
    public static final int PROVISIONED = 1;
    public static final String SIM_ID = "sim_id";
    public static final int SIM_NOT_INSERTED = -1;
    public static final int SIM_PROVISIONED = 0;
    public static final String SIM_PROVISIONING_STATUS = "sim_provisioning_status";
    public static final String SIM_SLOT_INDEX = "sim_id";
    public static final String SLOT = "slot";
    public static final int SLOT_NONE = -1;
    public static final String SUB_DEFAULT_CHANGED_ACTION = "android.intent.action.SUB_DEFAULT_CHANGED";
    public static final String UNIQUE_KEY_SUBSCRIPTION_ID = "_id";
    private static final boolean VDBG = false;
    public static final String WAP_PUSH = "wap_push";
    public static final int WAP_PUSH_DEFAULT = -1;
    public static final int WAP_PUSH_DISABLE = 0;
    public static final int WAP_PUSH_ENABLE = 1;
    private final Context mContext;

    public static class OnSubscriptionsChangedListener {
        IOnSubscriptionsChangedListener callback;
        private final Handler mHandler;

        public OnSubscriptionsChangedListener() {
            this.mHandler = new Handler(Looper.myLooper() == null ? Looper.getMainLooper() : Looper.myLooper()) {
                public void handleMessage(Message msg) {
                    OnSubscriptionsChangedListener.this.onSubscriptionsChanged();
                }
            };
            this.callback = new Stub() {
                public void onSubscriptionsChanged() {
                    OnSubscriptionsChangedListener.this.mHandler.sendEmptyMessage(0);
                }
            };
        }

        public void onSubscriptionsChanged() {
        }

        private void log(String s) {
            Rlog.d(SubscriptionManager.LOG_TAG, s);
        }
    }

    public SubscriptionManager(Context context) {
        this.mContext = context;
    }

    public static SubscriptionManager from(Context context) {
        return (SubscriptionManager) context.getSystemService("telephony_subscription_service");
    }

    public void addOnSubscriptionsChangedListener(OnSubscriptionsChangedListener listener) {
        String pkgForDebug = this.mContext != null ? this.mContext.getOpPackageName() : MediaStore.UNKNOWN_STRING;
        try {
            ITelephonyRegistry tr = ITelephonyRegistry.Stub.asInterface(ServiceManager.getService("telephony.registry"));
            if (tr != null) {
                tr.addOnSubscriptionsChangedListener(pkgForDebug, listener.callback);
            }
        } catch (RemoteException e) {
        }
    }

    public void removeOnSubscriptionsChangedListener(OnSubscriptionsChangedListener listener) {
        String pkgForDebug = this.mContext != null ? this.mContext.getOpPackageName() : MediaStore.UNKNOWN_STRING;
        try {
            ITelephonyRegistry tr = ITelephonyRegistry.Stub.asInterface(ServiceManager.getService("telephony.registry"));
            if (tr != null) {
                tr.removeOnSubscriptionsChangedListener(pkgForDebug, listener.callback);
            }
        } catch (RemoteException e) {
        }
    }

    public SubscriptionInfo getActiveSubscriptionInfo(int subId) {
        if (!isValidSubscriptionId(subId)) {
            return null;
        }
        SubscriptionInfo subInfo = null;
        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                subInfo = iSub.getActiveSubscriptionInfo(subId, this.mContext.getOpPackageName());
            }
        } catch (RemoteException e) {
        }
        return subInfo;
    }

    public SubscriptionInfo getActiveSubscriptionInfoForIccIndex(String iccId) {
        if (iccId == null) {
            logd("[getActiveSubscriptionInfoForIccIndex]- null iccid");
            return null;
        }
        SubscriptionInfo result = null;
        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                result = iSub.getActiveSubscriptionInfoForIccId(iccId, this.mContext.getOpPackageName());
            }
        } catch (RemoteException e) {
        }
        return result;
    }

    public SubscriptionInfo getActiveSubscriptionInfoForSimSlotIndex(int slotIndex) {
        if (isValidSlotIndex(slotIndex)) {
            SubscriptionInfo result = null;
            try {
                ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
                if (iSub != null) {
                    result = iSub.getActiveSubscriptionInfoForSimSlotIndex(slotIndex, this.mContext.getOpPackageName());
                }
            } catch (RemoteException e) {
            }
            return result;
        }
        logd("[getActiveSubscriptionInfoForSimSlotIndex]- invalid slotIndex");
        return null;
    }

    public List<SubscriptionInfo> getAllSubscriptionInfoList() {
        List<SubscriptionInfo> result = null;
        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                result = iSub.getAllSubInfoList(this.mContext.getOpPackageName());
            }
        } catch (RemoteException e) {
        }
        if (result == null) {
            return new ArrayList();
        }
        return result;
    }

    public List<SubscriptionInfo> getActiveSubscriptionInfoList() {
        List<SubscriptionInfo> result = null;
        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                result = iSub.getActiveSubscriptionInfoList(this.mContext.getOpPackageName());
            }
        } catch (RemoteException e) {
        }
        if (result == null) {
            return new ArrayList();
        }
        return result;
    }

    public List<SubscriptionInfo> getAvailableSubscriptionInfoList() {
        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                return iSub.getAvailableSubscriptionInfoList(this.mContext.getOpPackageName());
            }
            return null;
        } catch (RemoteException e) {
            return null;
        }
    }

    public List<SubscriptionInfo> getAccessibleSubscriptionInfoList() {
        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                return iSub.getAccessibleSubscriptionInfoList(this.mContext.getOpPackageName());
            }
            return null;
        } catch (RemoteException e) {
            return null;
        }
    }

    public void requestEmbeddedSubscriptionInfoListRefresh() {
        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                iSub.requestEmbeddedSubscriptionInfoListRefresh();
            }
        } catch (RemoteException e) {
        }
    }

    public int getAllSubscriptionInfoCount() {
        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                return iSub.getAllSubInfoCount(this.mContext.getOpPackageName());
            }
            return 0;
        } catch (RemoteException e) {
            return 0;
        }
    }

    public int getActiveSubscriptionInfoCount() {
        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                return iSub.getActiveSubInfoCount(this.mContext.getOpPackageName());
            }
            return 0;
        } catch (RemoteException e) {
            return 0;
        }
    }

    public int getActiveSubscriptionInfoCountMax() {
        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                return iSub.getActiveSubInfoCountMax();
            }
            return 0;
        } catch (RemoteException e) {
            return 0;
        }
    }

    public Uri addSubscriptionInfoRecord(String iccId, int slotIndex) {
        if (iccId == null) {
            logd("[addSubscriptionInfoRecord]- null iccId");
        }
        if (!isValidSlotIndex(slotIndex)) {
            logd("[addSubscriptionInfoRecord]- invalid slotIndex");
        }
        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                iSub.addSubInfoRecord(iccId, slotIndex);
            }
        } catch (RemoteException e) {
        }
        return null;
    }

    public int setIconTint(int tint, int subId) {
        if (isValidSubscriptionId(subId)) {
            int result = 0;
            try {
                ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
                if (iSub != null) {
                    result = iSub.setIconTint(tint, subId);
                }
            } catch (RemoteException e) {
            }
            return result;
        }
        logd("[setIconTint]- fail");
        return -1;
    }

    public int setDisplayName(String displayName, int subId) {
        return setDisplayName(displayName, subId, -1);
    }

    public int setDisplayName(String displayName, int subId, long nameSource) {
        if (isValidSubscriptionId(subId)) {
            int result = 0;
            try {
                ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
                if (iSub != null) {
                    result = iSub.setDisplayNameUsingSrc(displayName, subId, nameSource);
                }
            } catch (RemoteException e) {
            }
            return result;
        }
        logd("[setDisplayName]- fail");
        return -1;
    }

    public int setDisplayNumber(String number, int subId) {
        if (number == null || (isValidSubscriptionId(subId) ^ 1) != 0) {
            logd("[setDisplayNumber]- fail");
            return -1;
        }
        int result = 0;
        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                result = iSub.setDisplayNumber(number, subId);
            }
        } catch (RemoteException e) {
        }
        return result;
    }

    public int setDataRoaming(int roaming, int subId) {
        if (roaming < 0 || (isValidSubscriptionId(subId) ^ 1) != 0) {
            logd("[setDataRoaming]- fail");
            return -1;
        }
        int result = 0;
        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                result = iSub.setDataRoaming(roaming, subId);
            }
        } catch (RemoteException e) {
        }
        return result;
    }

    public static int getSlotIndex(int subId) {
        boolean isValidSubscriptionId = isValidSubscriptionId(subId);
        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                return iSub.getSlotIndex(subId);
            }
            return -1;
        } catch (RemoteException e) {
            return -1;
        }
    }

    public static int[] getSubId(int slotIndex) {
        if (isValidSlotIndex(slotIndex)) {
            int[] subId = null;
            try {
                ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
                if (iSub != null) {
                    subId = iSub.getSubId(slotIndex);
                }
            } catch (RemoteException e) {
            }
            return subId;
        }
        logd("[getSubId]- fail");
        return null;
    }

    public static int getPhoneId(int subId) {
        if (!isValidSubscriptionId(subId)) {
            return -1;
        }
        int result = -1;
        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                result = iSub.getPhoneId(subId);
            }
        } catch (RemoteException e) {
        }
        return result;
    }

    private static void logd(String msg) {
        Rlog.d(LOG_TAG, msg);
    }

    public static int getDefaultSubscriptionId() {
        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                return iSub.getDefaultSubId();
            }
            return -1;
        } catch (RemoteException e) {
            return -1;
        }
    }

    public static int getDefaultSubId() {
        return getDefaultSubscriptionId();
    }

    public static int getDefaultVoiceSubscriptionId() {
        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                return iSub.getDefaultVoiceSubId();
            }
            return -1;
        } catch (RemoteException e) {
            return -1;
        }
    }

    public static int getDefaultVoiceSubId() {
        return getDefaultVoiceSubscriptionId();
    }

    public void setDefaultVoiceSubId(int subId) {
        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                iSub.setDefaultVoiceSubId(subId);
            }
        } catch (RemoteException e) {
        }
    }

    public SubscriptionInfo getDefaultVoiceSubscriptionInfo() {
        return getActiveSubscriptionInfo(getDefaultVoiceSubscriptionId());
    }

    public static int getDefaultVoicePhoneId() {
        return getPhoneId(getDefaultVoiceSubscriptionId());
    }

    public static int getDefaultSmsSubscriptionId() {
        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                return iSub.getDefaultSmsSubId();
            }
            return -1;
        } catch (RemoteException e) {
            return -1;
        }
    }

    public static int getDefaultSmsSubId() {
        return getDefaultSmsSubscriptionId();
    }

    public void setDefaultSmsSubId(int subId) {
        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                iSub.setDefaultSmsSubId(subId);
            }
        } catch (RemoteException e) {
        }
    }

    public SubscriptionInfo getDefaultSmsSubscriptionInfo() {
        return getActiveSubscriptionInfo(getDefaultSmsSubscriptionId());
    }

    public int getDefaultSmsPhoneId() {
        return getPhoneId(getDefaultSmsSubscriptionId());
    }

    public static int getDefaultDataSubscriptionId() {
        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                return iSub.getDefaultDataSubId();
            }
            return -1;
        } catch (RemoteException e) {
            return -1;
        }
    }

    public static int getDefaultDataSubId() {
        return getDefaultDataSubscriptionId();
    }

    public void setDefaultDataSubId(int subId) {
        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                iSub.setDefaultDataSubId(subId);
            }
        } catch (RemoteException e) {
        }
    }

    public SubscriptionInfo getDefaultDataSubscriptionInfo() {
        return getActiveSubscriptionInfo(getDefaultDataSubscriptionId());
    }

    public int getDefaultDataPhoneId() {
        return getPhoneId(getDefaultDataSubscriptionId());
    }

    public void clearSubscriptionInfo() {
        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                iSub.clearSubInfo();
            }
        } catch (RemoteException e) {
        }
    }

    public boolean allDefaultsSelected() {
        if (isValidSubscriptionId(getDefaultDataSubscriptionId()) && isValidSubscriptionId(getDefaultSmsSubscriptionId()) && isValidSubscriptionId(getDefaultVoiceSubscriptionId())) {
            return true;
        }
        return false;
    }

    public void clearDefaultsForInactiveSubIds() {
        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                iSub.clearDefaultsForInactiveSubIds();
            }
        } catch (RemoteException e) {
        }
    }

    public static boolean isValidSubscriptionId(int subId) {
        return subId > -1;
    }

    public static boolean isUsableSubIdValue(int subId) {
        return subId >= 0 && subId <= 2147483646;
    }

    public static boolean isValidSlotIndex(int slotIndex) {
        return slotIndex >= 0 && slotIndex < TelephonyManager.getDefault().getSimCount();
    }

    public static boolean isValidPhoneId(int phoneId) {
        return phoneId >= 0 && phoneId < TelephonyManager.getDefault().getPhoneCount();
    }

    public static void putPhoneIdAndSubIdExtra(Intent intent, int phoneId) {
        int[] subIds = getSubId(phoneId);
        if (subIds == null || subIds.length <= 0) {
            logd("putPhoneIdAndSubIdExtra: no valid subs");
        } else {
            putPhoneIdAndSubIdExtra(intent, phoneId, subIds[0]);
        }
    }

    public static void putPhoneIdAndSubIdExtra(Intent intent, int phoneId, int subId) {
        intent.putExtra("subscription", subId);
        intent.putExtra(PhoneConstants.COLOR_INT_SUBID, subId);
        intent.putExtra(EXTRA_SUBSCRIPTION_INDEX, subId);
        intent.putExtra("phone", phoneId);
        intent.putExtra("slot", phoneId);
    }

    public int[] getActiveSubscriptionIdList() {
        int[] subId = null;
        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                subId = iSub.getActiveSubIdList();
            }
        } catch (RemoteException e) {
        }
        if (subId == null) {
            return new int[0];
        }
        return subId;
    }

    public boolean isNetworkRoaming(int subId) {
        if (getPhoneId(subId) < 0) {
            return false;
        }
        return TelephonyManager.getDefault().isNetworkRoaming(subId);
    }

    public static int getSimStateForSlotIndex(int slotIndex) {
        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                return iSub.getSimStateForSlotIndex(slotIndex);
            }
            return 0;
        } catch (RemoteException e) {
            return 0;
        }
    }

    public static void setSubscriptionProperty(int subId, String propKey, String propValue) {
        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                iSub.setSubscriptionProperty(subId, propKey, propValue);
            }
        } catch (RemoteException e) {
        }
    }

    private static String getSubscriptionProperty(int subId, String propKey, Context context) {
        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                return iSub.getSubscriptionProperty(subId, propKey, context.getOpPackageName());
            }
            return null;
        } catch (RemoteException e) {
            return null;
        }
    }

    public static boolean getBooleanSubscriptionProperty(int subId, String propKey, boolean defValue, Context context) {
        boolean z = true;
        String result = getSubscriptionProperty(subId, propKey, context);
        if (result != null) {
            try {
                if (Integer.parseInt(result) != 1) {
                    z = false;
                }
                return z;
            } catch (NumberFormatException e) {
                logd("getBooleanSubscriptionProperty NumberFormat exception");
            }
        }
        return defValue;
    }

    public static int getIntegerSubscriptionProperty(int subId, String propKey, int defValue, Context context) {
        String result = getSubscriptionProperty(subId, propKey, context);
        if (result != null) {
            try {
                return Integer.parseInt(result);
            } catch (NumberFormatException e) {
                logd("getBooleanSubscriptionProperty NumberFormat exception");
            }
        }
        return defValue;
    }

    public static Resources getResourcesForSubId(Context context, int subId) {
        SubscriptionInfo subInfo = from(context).getActiveSubscriptionInfo(subId);
        Configuration config = context.getResources().getConfiguration();
        Configuration newConfig = new Configuration();
        newConfig.setTo(config);
        if (subInfo != null) {
            newConfig.mcc = subInfo.getMcc();
            newConfig.mnc = subInfo.getMnc();
            if (newConfig.mnc == 0) {
                newConfig.mnc = 65535;
            }
        }
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        DisplayMetrics newMetrics = new DisplayMetrics();
        newMetrics.setTo(metrics);
        return new Resources(context.getResources().getAssets(), newMetrics, newConfig);
    }

    public boolean isActiveSubId(int subId) {
        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                return iSub.isActiveSubId(subId);
            }
        } catch (RemoteException e) {
        }
        return false;
    }

    public List<SubscriptionPlan> getSubscriptionPlans(int subId) {
        try {
            SubscriptionPlan[] subscriptionPlans = INetworkPolicyManager.Stub.asInterface(ServiceManager.getService("netpolicy")).getSubscriptionPlans(subId, this.mContext.getOpPackageName());
            return subscriptionPlans == null ? Collections.emptyList() : Arrays.asList(subscriptionPlans);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setSubscriptionPlans(int subId, List<SubscriptionPlan> plans) {
        try {
            INetworkPolicyManager.Stub.asInterface(ServiceManager.getService("netpolicy")).setSubscriptionPlans(subId, (SubscriptionPlan[]) plans.toArray(new SubscriptionPlan[plans.size()]), this.mContext.getOpPackageName());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public List<SubInfoRecord> getActiveSubInfoList() {
        List<SubInfoRecord> result = null;
        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                result = iSub.getActiveSubInfoList(this.mContext.getOpPackageName());
            }
        } catch (RemoteException e) {
        }
        if (result == null) {
            return new ArrayList();
        }
        return result;
    }

    public List<SubInfoRecord> getSubInfoUsingSlotId(int slotId) {
        if (isValidSlotIndex(slotId)) {
            List<SubInfoRecord> result = null;
            try {
                ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
                if (iSub != null) {
                    result = iSub.getSubInfoUsingSlotId(slotId, this.mContext.getOpPackageName());
                }
            } catch (RemoteException e) {
            }
            if (result == null) {
                result = new ArrayList();
            }
            return result;
        }
        logd("[getSubInfoUsingSlotId]- invalid slotId");
        return null;
    }

    public SubInfoRecord getSubInfoForSubscriber(int subId) {
        if (isValidSubscriptionId(subId)) {
            SubInfoRecord subInfo = null;
            try {
                ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
                if (iSub != null) {
                    subInfo = iSub.getSubInfoForSubscriber(subId, this.mContext.getOpPackageName());
                }
            } catch (RemoteException e) {
            }
            return subInfo;
        }
        logd("[getSubInfoForSubscriberx]- invalid subId");
        return null;
    }

    public void setDefaultApplication(String packageName) {
        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                iSub.setDefaultApplication(packageName);
            }
        } catch (RemoteException e) {
        }
    }

    public static boolean isCTCCard(int slotId) {
        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                return iSub.isCTCCard(slotId);
            }
            return false;
        } catch (RemoteException e) {
            return false;
        }
    }

    public static boolean colorisSMSPromptEnabled() {
        boolean vRet = false;
        try {
            return IExtTelephony.Stub.asInterface(ServiceManager.getService("extphone")).isSMSPromptEnabled();
        } catch (RemoteException e) {
            return vRet;
        }
    }

    public static void activateSubId(int subId) {
        try {
            IExtTelephony.Stub.asInterface(ServiceManager.getService("extphone")).activateUiccCard(getSlotIndex(subId));
        } catch (RemoteException e) {
        }
    }

    public static void deactivateSubId(int subId) {
        try {
            IExtTelephony.Stub.asInterface(ServiceManager.getService("extphone")).deactivateUiccCard(getSlotIndex(subId));
        } catch (RemoteException e) {
        }
    }

    public static int getSubState(int subId) {
        int subStatus = 0;
        int slotId = getSlotIndex(subId);
        if (slotId < 0) {
            return 0;
        }
        try {
            subStatus = IExtTelephony.Stub.asInterface(ServiceManager.getService("extphone")).getCurrentUiccCardProvisioningStatus(slotId);
        } catch (RemoteException e) {
        }
        return subStatus;
    }

    public static int getUiccCardProvisioningUserPreference(int slotId) {
        int subStatus = 0;
        try {
            return IExtTelephony.Stub.asInterface(ServiceManager.getService("extphone")).getUiccCardProvisioningUserPreference(slotId);
        } catch (RemoteException e) {
            return subStatus;
        }
    }

    public static boolean isVsimEnabled(int subId) {
        boolean z = false;
        if (!isValidSubscriptionId(subId)) {
            return false;
        }
        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub == null) {
                return false;
            }
            int slotId = getSlotIndex(subId);
            if (!isValidSlotIndex(slotId)) {
                return false;
            }
            if (iSub.getSoftSimCardSlotId() == slotId) {
                z = true;
            }
            return z;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static String getOperatorNumericForData(int subId) {
        String result = "";
        if (!isValidSubscriptionId(subId)) {
            result = null;
        }
        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub == null) {
            }
            int phoneId = getPhoneId(subId);
            if (!isValidPhoneId(phoneId)) {
            }
            return iSub.getOperatorNumericForData(phoneId);
        } catch (Exception e) {
            e.printStackTrace();
            return result;
        }
    }
}
