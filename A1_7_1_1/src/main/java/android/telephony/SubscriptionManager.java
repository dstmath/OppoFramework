package android.telephony;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.DisplayMetrics;
import com.android.internal.telephony.IOnSubscriptionsChangedListener;
import com.android.internal.telephony.IOnSubscriptionsChangedListener.Stub;
import com.android.internal.telephony.ISub;
import com.android.internal.telephony.ITelephonyRegistry;
import com.android.internal.telephony.PhoneConstants;
import java.util.ArrayList;
import java.util.List;

/*  JADX ERROR: NullPointerException in pass: ReSugarCode
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
    	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ExtractFieldInit.checkStaticFieldsInit(ExtractFieldInit.java:58)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class SubscriptionManager {
    public static final int ACTIVE = 1;
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
    public static final Uri CONTENT_URI = null;
    public static final String DATA_ROAMING = "data_roaming";
    public static final int DATA_ROAMING_DEFAULT = 0;
    public static final int DATA_ROAMING_DISABLE = 0;
    public static final int DATA_ROAMING_ENABLE = 1;
    private static final boolean DBG = false;
    public static final int DEFAULT_INT_VALUE = -100;
    public static final int DEFAULT_NAME_RES = 17039374;
    public static final int DEFAULT_NW_MODE = -1;
    public static final int DEFAULT_PHONE_INDEX = Integer.MAX_VALUE;
    public static final int DEFAULT_SIM_SLOT_INDEX = Integer.MAX_VALUE;
    public static final String DEFAULT_STRING_VALUE = "N/A";
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
    public static final int EXTRA_VALUE_NEW_SIM = 1;
    public static final int EXTRA_VALUE_NOCHANGE = 4;
    public static final int EXTRA_VALUE_REMOVE_SIM = 2;
    public static final int EXTRA_VALUE_REPOSITION_SIM = 3;
    public static final String ICC_ID = "icc_id";
    public static final int INACTIVE = 0;
    public static final String INTENT_KEY_DETECT_STATUS = "simDetectStatus";
    public static final String INTENT_KEY_NEW_SIM_SLOT = "newSIMSlot";
    public static final String INTENT_KEY_NEW_SIM_STATUS = "newSIMStatus";
    public static final String INTENT_KEY_SIM_COUNT = "simCount";
    public static final int INVALID_PHONE_INDEX = -1;
    public static final int INVALID_SIM_SLOT_INDEX = -1;
    public static final int INVALID_STATE = -1;
    public static final int INVALID_SUBSCRIPTION_ID = -1;
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
    public static final String NETWORK_MODE = "network_mode";
    public static final int NOT_PROVISIONED = 0;
    public static final String NUMBER = "number";
    public static final int PROVISIONED = 1;
    public static final int SIM_NOT_INSERTED = -1;
    public static final int SIM_PROVISIONED = 0;
    public static final String SIM_PROVISIONING_STATUS = "sim_provisioning_status";
    public static final String SIM_SLOT_INDEX = "sim_id";
    public static final String SLOT = "slot";
    public static final int SLOT_NONE = -1;
    public static final int SUB_CONFIGURATION_IN_PROGRESS = 2;
    public static final String SUB_DEFAULT_CHANGED_ACTION = "android.intent.action.SUB_DEFAULT_CHANGED";
    public static final String SUB_STATE = "sub_state";
    public static final String UNIQUE_KEY_SUBSCRIPTION_ID = "_id";
    private static final boolean VDBG = false;
    public static final String WAP_PUSH = "wap_push";
    public static final int WAP_PUSH_DEFAULT = -1;
    public static final int WAP_PUSH_DISABLE = 0;
    public static final int WAP_PUSH_ENABLE = 1;
    private final Context mContext;

    public static class OnSubscriptionsChangedListener {
        IOnSubscriptionsChangedListener callback = new Stub() {
            public void onSubscriptionsChanged() {
                if (SubscriptionManager.DBG) {
                    OnSubscriptionsChangedListener.this.log("callback: received, sendEmptyMessage(0) to handler");
                }
                OnSubscriptionsChangedListener.this.mHandler.sendEmptyMessage(0);
            }
        };
        private final Handler mHandler = new Handler() {
            public void handleMessage(Message msg) {
                if (SubscriptionManager.DBG) {
                    OnSubscriptionsChangedListener.this.log("handleMessage: invoke the overriden onSubscriptionsChanged()");
                }
                OnSubscriptionsChangedListener.this.onSubscriptionsChanged();
            }
        };

        public void onSubscriptionsChanged() {
            if (SubscriptionManager.DBG) {
                log("onSubscriptionsChanged: NOT OVERRIDDEN");
            }
        }

        private void log(String s) {
            Rlog.d(SubscriptionManager.LOG_TAG, s);
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.telephony.SubscriptionManager.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 9 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.telephony.SubscriptionManager.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.telephony.SubscriptionManager.<clinit>():void");
    }

    public SubscriptionManager(Context context) {
        if (DBG) {
            logd("SubscriptionManager created");
        }
        this.mContext = context;
    }

    public static SubscriptionManager from(Context context) {
        return (SubscriptionManager) context.getSystemService("telephony_subscription_service");
    }

    public void addOnSubscriptionsChangedListener(OnSubscriptionsChangedListener listener) {
        String pkgForDebug = this.mContext != null ? this.mContext.getOpPackageName() : "<unknown>";
        if (DBG) {
            logd("register OnSubscriptionsChangedListener pkgForDebug=" + pkgForDebug + " listener=" + listener);
        }
        try {
            ITelephonyRegistry tr = ITelephonyRegistry.Stub.asInterface(ServiceManager.getService("telephony.registry"));
            if (tr != null) {
                tr.addOnSubscriptionsChangedListener(pkgForDebug, listener.callback);
            }
        } catch (RemoteException e) {
        }
    }

    public void removeOnSubscriptionsChangedListener(OnSubscriptionsChangedListener listener) {
        String pkgForDebug = this.mContext != null ? this.mContext.getOpPackageName() : "<unknown>";
        if (DBG) {
            logd("unregister OnSubscriptionsChangedListener pkgForDebug=" + pkgForDebug + " listener=" + listener);
        }
        try {
            ITelephonyRegistry tr = ITelephonyRegistry.Stub.asInterface(ServiceManager.getService("telephony.registry"));
            if (tr != null) {
                tr.removeOnSubscriptionsChangedListener(pkgForDebug, listener.callback);
            }
        } catch (RemoteException e) {
        }
    }

    public SubscriptionInfo getActiveSubscriptionInfo(int subId) {
        if (VDBG) {
            logd("[getActiveSubscriptionInfo]+ subId=" + subId);
        }
        if (isValidSubscriptionId(subId)) {
            SubscriptionInfo subInfo = null;
            String pkgForDebug = this.mContext != null ? this.mContext.getOpPackageName() : "<unknown>";
            try {
                ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
                if (iSub != null) {
                    subInfo = iSub.getActiveSubscriptionInfo(subId, pkgForDebug);
                }
            } catch (RemoteException e) {
            }
            return subInfo;
        }
        if (DBG) {
            logd("[getActiveSubscriptionInfo]- invalid subId");
        }
        return null;
    }

    public SubscriptionInfo getActiveSubscriptionInfoForIccIndex(String iccId) {
        if (VDBG) {
            logd("[getActiveSubscriptionInfoForIccIndex]+ iccId=" + iccId);
        }
        if (iccId == null) {
            logd("[getActiveSubscriptionInfoForIccIndex]- null iccid");
            return null;
        }
        SubscriptionInfo result = null;
        String pkgForDebug = this.mContext != null ? this.mContext.getOpPackageName() : "<unknown>";
        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                result = iSub.getActiveSubscriptionInfoForIccId(iccId, pkgForDebug);
            }
        } catch (RemoteException e) {
        }
        return result;
    }

    public SubscriptionInfo getActiveSubscriptionInfoForSimSlotIndex(int slotIdx) {
        if (VDBG) {
            logd("[getActiveSubscriptionInfoForSimSlotIndex]+ slotIdx=" + slotIdx);
        }
        if (isValidSlotId(slotIdx)) {
            SubscriptionInfo result = null;
            String pkgForDebug = this.mContext != null ? this.mContext.getOpPackageName() : "<unknown>";
            try {
                ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
                if (iSub != null) {
                    result = iSub.getActiveSubscriptionInfoForSimSlotIndex(slotIdx, pkgForDebug);
                }
            } catch (RemoteException e) {
            }
            return result;
        }
        logd("[getActiveSubscriptionInfoForSimSlotIndex]- invalid slotIdx, slotIdx = " + slotIdx);
        return null;
    }

    public List<SubscriptionInfo> getAllSubscriptionInfoList() {
        if (VDBG) {
            logd("[getAllSubscriptionInfoList]+");
        }
        List<SubscriptionInfo> result = null;
        String pkgForDebug = this.mContext != null ? this.mContext.getOpPackageName() : "<unknown>";
        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                result = iSub.getAllSubInfoList(pkgForDebug);
            }
        } catch (RemoteException e) {
        }
        if (result == null) {
            return new ArrayList();
        }
        return result;
    }

    public List<SubscriptionInfo> getActiveSubscriptionInfoList() {
        String pkgForDebug = this.mContext != null ? this.mContext.getOpPackageName() : "<unknown>";
        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                return iSub.getActiveSubscriptionInfoList(pkgForDebug);
            }
            return null;
        } catch (RemoteException e) {
            return null;
        }
    }

    public int getAllSubscriptionInfoCount() {
        if (VDBG) {
            logd("[getAllSubscriptionInfoCount]+");
        }
        String pkgForDebug = this.mContext != null ? this.mContext.getOpPackageName() : "<unknown>";
        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                return iSub.getAllSubInfoCount(pkgForDebug);
            }
            return 0;
        } catch (RemoteException e) {
            return 0;
        }
    }

    public int getActiveSubscriptionInfoCount() {
        String pkgForDebug = this.mContext != null ? this.mContext.getOpPackageName() : "<unknown>";
        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                return iSub.getActiveSubInfoCount(pkgForDebug);
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

    public Uri addSubscriptionInfoRecord(String iccId, int slotId) {
        if (VDBG) {
            logd("[addSubscriptionInfoRecord]+ iccId:" + iccId + " slotId:" + slotId);
        }
        if (iccId == null) {
            logd("[addSubscriptionInfoRecord]- null iccId");
        }
        if (!isValidSlotId(slotId)) {
            logd("[addSubscriptionInfoRecord]- invalid slotId = " + slotId);
        }
        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                iSub.addSubInfoRecord(iccId, slotId);
            }
        } catch (RemoteException e) {
        }
        return null;
    }

    public int setIconTint(int tint, int subId) {
        if (VDBG) {
            logd("[setIconTint]+ tint:" + tint + " subId:" + subId);
        }
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
        logd("[setIconTint]- fail, subId = " + subId + ", tint = " + tint);
        return -1;
    }

    public int setDisplayName(String displayName, int subId) {
        return setDisplayName(displayName, subId, -1);
    }

    public int setDisplayName(String displayName, int subId, long nameSource) {
        if (VDBG) {
            logd("[setDisplayName]+  displayName:" + displayName + " subId:" + subId + " nameSource:" + nameSource);
        }
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
        logd("[setDisplayName]- fail, subId = " + subId);
        return -1;
    }

    public int setDisplayNumber(String number, int subId) {
        if (number == null || !isValidSubscriptionId(subId)) {
            logd("[setDisplayNumber]- fail, subId = " + subId);
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
        if (VDBG) {
            logd("[setDataRoaming]+ roaming:" + roaming + " subId:" + subId);
        }
        if (roaming < 0 || !isValidSubscriptionId(subId)) {
            logd("[setDataRoaming]- fail, subId = " + subId);
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

    public static int getSlotId(int subId) {
        if (!isValidSubscriptionId(subId)) {
            if (DBG) {
                logd("[getSlotId]- fail");
            }
            logd("[getSlotId]- fail, subId = " + subId);
        }
        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                return iSub.getSlotId(subId);
            }
            return -1;
        } catch (RemoteException e) {
            return -1;
        }
    }

    public static int[] getSubId(int slotId) {
        if (isValidSlotId(slotId)) {
            int[] subId = null;
            try {
                ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
                if (iSub != null) {
                    subId = iSub.getSubId(slotId);
                }
            } catch (RemoteException e) {
            }
            return subId;
        }
        logd("[getSubId]- fail, slotId = " + slotId);
        return null;
    }

    public static int getSubIdUsingPhoneId(int phoneId) {
        if (VDBG) {
            logd("[getSubIdUsingPhoneId]+ phoneId:" + phoneId);
        }
        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                return iSub.getSubIdUsingPhoneId(phoneId);
            }
            return -1;
        } catch (RemoteException e) {
            return -1;
        }
    }

    public static int getPhoneId(int subId) {
        if (isValidSubscriptionId(subId)) {
            int result = -1;
            try {
                ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
                if (iSub != null) {
                    result = iSub.getPhoneId(subId);
                }
            } catch (RemoteException e) {
            }
            if (VDBG) {
                logd("[getPhoneId]- phoneId=" + result);
            }
            return result;
        }
        if (DBG) {
            logd("[getPhoneId]- fail");
        }
        if (subId <= -2 - TelephonyManager.getDefault().getSimCount()) {
            return -1;
        }
        if (VDBG) {
            logd("[getPhoneId]- return dummy value, subId = " + subId);
        }
        return -2 - subId;
    }

    private static void logd(String msg) {
        Rlog.d(LOG_TAG, msg);
    }

    public static void setDefaultSubId(int subId) {
        if (VDBG) {
            logd("setDefaultSubId sub id = " + subId);
        }
        if (subId <= 0) {
            printStackTrace("setDefaultSubId subId 0");
        }
        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                iSub.setDefaultFallbackSubId(subId);
            }
        } catch (RemoteException e) {
        }
    }

    public static int getDefaultSubId() {
        int subId = -1;
        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                subId = iSub.getDefaultSubId();
            }
        } catch (RemoteException e) {
        }
        if (VDBG) {
            logd("getDefaultSubId, sub id = " + subId);
        }
        return subId;
    }

    public static int getDefaultSubscriptionId() {
        int subId = -1;
        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                subId = iSub.getDefaultSubId();
            }
        } catch (RemoteException e) {
        }
        if (VDBG) {
            logd("getDefaultSubId, sub id = " + subId);
        }
        return subId;
    }

    public static int getDefaultVoiceSubscriptionId() {
        int subId = -1;
        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                subId = iSub.getDefaultVoiceSubId();
            }
        } catch (RemoteException e) {
        }
        if (VDBG) {
            logd("getDefaultVoiceSubscriptionId, sub id = " + subId);
        }
        return subId;
    }

    public static int getDefaultVoiceSubId() {
        return getDefaultVoiceSubscriptionId();
    }

    public void setDefaultVoiceSubId(int subId) {
        if (VDBG) {
            logd("setDefaultVoiceSubId sub id = " + subId);
        }
        if (subId <= 0) {
            printStackTrace("setDefaultVoiceSubId subId 0");
        }
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
        int subId = -1;
        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                subId = iSub.getDefaultSmsSubId();
            }
        } catch (RemoteException e) {
        }
        if (VDBG) {
            logd("getDefaultSmsSubscriptionId, sub id = " + subId);
        }
        return subId;
    }

    public static int getDefaultSmsSubId() {
        return getDefaultSmsSubscriptionId();
    }

    public void setDefaultSmsSubId(int subId) {
        if (VDBG) {
            logd("setDefaultSmsSubId sub id = " + subId);
        }
        if (subId <= 0) {
            printStackTrace("setDefaultSmsSubId subId 0");
        }
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
        int subId = -1;
        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                subId = iSub.getDefaultDataSubId();
            }
        } catch (RemoteException e) {
        } catch (SecurityException e2) {
        }
        if (VDBG) {
            logd("getDefaultDataSubscriptionId, sub id = " + subId);
        }
        return subId;
    }

    public static int getDefaultDataSubId() {
        return getDefaultDataSubscriptionId();
    }

    public void setDefaultDataSubId(int subId) {
        if (VDBG) {
            logd("setDataSubscription sub id = " + subId);
        }
        if (subId <= 0) {
            printStackTrace("setDefaultDataSubId subId 0");
        }
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
        if (VDBG) {
            logd("clearDefaultsForInactiveSubIds");
        }
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
        return subId >= 0 && subId <= MAX_SUBSCRIPTION_ID_VALUE;
    }

    public static boolean isValidSlotId(int slotId) {
        return slotId >= 0 && slotId < TelephonyManager.getDefault().getSimCount();
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
        if (VDBG) {
            logd("putPhoneIdAndSubIdExtra: phoneId=" + phoneId + " subId=" + subId);
        }
        intent.putExtra(PhoneConstants.COLOR_INT_SUBID, subId);
        intent.putExtra("subscription", subId);
        intent.putExtra(PhoneConstants.PHONE_KEY, phoneId);
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

    private static void printStackTrace(String msg) {
        RuntimeException re = new RuntimeException();
        logd("StackTrace - " + msg);
        for (StackTraceElement ste : re.getStackTrace()) {
            logd(ste.toString());
        }
    }

    public boolean isNetworkRoaming(int subId) {
        if (getPhoneId(subId) < 0) {
            return false;
        }
        return TelephonyManager.getDefault().isNetworkRoaming(subId);
    }

    public static int getSimStateForSlotIdx(int slotIdx) {
        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                return iSub.getSimStateForSlotIdx(slotIdx);
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

    public SubscriptionInfo getSubscriptionInfo(int subId) {
        if (VDBG) {
            logd("[getSubscriptionInfo]+ subId=" + subId);
        }
        if (isValidSubscriptionId(subId)) {
            SubscriptionInfo subInfo = null;
            try {
                ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
                if (iSub != null) {
                    subInfo = iSub.getSubscriptionInfo(subId);
                }
            } catch (RemoteException e) {
            }
            return subInfo;
        }
        logd("[getSubscriptionInfo]- invalid subId, subId = " + subId);
        return null;
    }

    public SubscriptionInfo getSubscriptionInfoForIccId(String iccId) {
        if (VDBG) {
            logd("[getSubscriptionInfoForIccId]+ iccId=" + iccId);
        }
        if (iccId == null) {
            logd("[getSubscriptionInfoForIccId]- null iccid");
            return null;
        }
        SubscriptionInfo result = null;
        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                result = iSub.getSubscriptionInfoForIccId(iccId);
            }
        } catch (RemoteException e) {
        }
        return result;
    }

    public void setDefaultDataSubIdWithoutCapabilitySwitch(int subId) {
        if (VDBG) {
            logd("setDefaultDataSubIdWithoutCapabilitySwitch sub id = " + subId);
        }
        if (subId <= 0) {
            printStackTrace("setDefaultDataSubIdWithoutCapabilitySwitch subId 0");
        }
        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                iSub.setDefaultDataSubIdWithoutCapabilitySwitch(subId);
            }
        } catch (RemoteException e) {
        }
    }

    public List<SubInfoRecord> getActiveSubInfoList() {
        List<SubInfoRecord> result = null;
        String pkgForDebug = this.mContext != null ? this.mContext.getOpPackageName() : "<unknown>";
        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                result = iSub.getActiveSubInfoList(pkgForDebug);
            }
        } catch (RemoteException e) {
        }
        if (result == null) {
            return new ArrayList();
        }
        return result;
    }

    public List<SubInfoRecord> getAllSubInfoList() {
        if (VDBG) {
            logd("[getAllSubInfoList]+");
        }
        List<SubInfoRecord> result = null;
        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                result = iSub.colorgetAllSubInfoList();
            }
        } catch (RemoteException e) {
        }
        if (result == null) {
            return new ArrayList();
        }
        return result;
    }

    public SubInfoRecord colorgetDefaultSmsSubInfo() {
        return colorgetActiveSubscriptionInfo(getDefaultSmsSubId());
    }

    public SubInfoRecord colorgetDefaultVoiceSubscriptionInfo() {
        return colorgetActiveSubscriptionInfo(getDefaultVoiceSubId());
    }

    public SubInfoRecord colorgetDefaultDataSubscriptionInfo() {
        return colorgetActiveSubscriptionInfo(getDefaultDataSubId());
    }

    public SubInfoRecord colorgetActiveSubscriptionInfo(int subId) {
        if (VDBG) {
            logd("[colorgetActiveSubscriptionInfo]+ subId=" + subId);
        }
        if (isValidSubscriptionId(subId)) {
            SubInfoRecord subInfo = null;
            try {
                ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
                if (iSub != null) {
                    subInfo = iSub.colorgetActiveSubscriptionInfo(subId);
                }
            } catch (RemoteException e) {
            }
            return subInfo;
        }
        logd("[colorgetActiveSubscriptionInfo]- invalid subId");
        return null;
    }

    public void temporarySwitchDataSubId(int subId) {
        logd("temporarySwitchDataSubId sub id = " + subId);
        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                iSub.setDefaultDataSubId(subId);
            }
        } catch (RemoteException e) {
        }
    }

    public SubInfoRecord colorgetActiveSubscriptionInfoForIccIndex(String iccId) {
        if (VDBG) {
            logd("[getActiveSubscriptionInfoForIccIndex]+ iccId=" + iccId);
        }
        if (iccId == null) {
            logd("[getActiveSubscriptionInfoForIccIndex]- null iccid");
            return null;
        }
        SubInfoRecord result = null;
        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                result = iSub.colorgetActiveSubscriptionInfoForIccId(iccId);
            }
        } catch (RemoteException e) {
        }
        return result;
    }

    public List<SubInfoRecord> getSubInfoUsingSlotId(int slotId) {
        if (isValidSlotId(slotId)) {
            List<SubInfoRecord> result = null;
            String pkgForDebug = this.mContext != null ? this.mContext.getOpPackageName() : "<unknown>";
            try {
                ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
                if (iSub != null) {
                    result = iSub.getSubInfoUsingSlotId(slotId, pkgForDebug);
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
            String pkgForDebug = this.mContext != null ? this.mContext.getOpPackageName() : "<unknown>";
            try {
                ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
                if (iSub != null) {
                    subInfo = iSub.getSubInfoForSubscriber(subId, pkgForDebug);
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

    public static boolean checkUsimIs4g(int slotId) {
        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                return iSub.checkUsimIs4g(slotId);
            }
            return false;
        } catch (RemoteException e) {
            return false;
        }
    }

    public static boolean isSMSPromptEnabled() {
        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                return iSub.isSMSPromptEnabled();
            }
        } catch (RemoteException e) {
        }
        return false;
    }

    public static void setSMSPromptEnabled(boolean enabled) {
        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                iSub.setSMSPromptEnabled(enabled);
            }
        } catch (RemoteException e) {
        }
    }

    public static boolean isVoicePromptEnabled() {
        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                return iSub.isVoicePromptEnabled();
            }
        } catch (RemoteException e) {
        }
        return false;
    }

    public static void setVoicePromptEnabled(boolean enabled) {
        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                iSub.setVoicePromptEnabled(enabled);
            }
        } catch (RemoteException e) {
        }
    }

    public static int getOnDemandDataSubId() {
        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                return iSub.getOnDemandDataSubId();
            }
            return Integer.MAX_VALUE;
        } catch (RemoteException e) {
            return Integer.MAX_VALUE;
        }
    }

    public static void activateSubId(int subId) {
        logd("activateSubId sub id = " + subId);
        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                iSub.activateSubId(subId);
            }
        } catch (RemoteException e) {
        }
    }

    public static void deactivateSubId(int subId) {
        logd("deactivateSubId sub id = " + subId);
        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                iSub.deactivateSubId(subId);
            }
        } catch (RemoteException e) {
        }
    }

    public static int getSubState(int subId) {
        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                return iSub.getSubState(subId);
            }
            return 0;
        } catch (RemoteException e) {
            return 0;
        }
    }

    public static int setSubState(int subId, int subState) {
        logd("setSubState sub id = " + subId + " state = " + subState);
        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                return iSub.setSubState(subId, subState);
            }
            return 0;
        } catch (RemoteException e) {
            return 0;
        }
    }

    private static ISub getISubInfo() {
        return ISub.Stub.asInterface(ServiceManager.getService("isub"));
    }

    public static boolean isUsimWithCsim(int slotId) {
        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                return iSub.isUsimWithCsim(slotId);
            }
            return false;
        } catch (RemoteException e) {
            return false;
        }
    }

    public boolean colorIsImsRegistered(int slotId) {
        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub != null) {
                return iSub.colorIsImsRegistered(slotId);
            }
            return false;
        } catch (RemoteException e) {
            return false;
        }
    }

    public static boolean isVsimEnabled(int subId) {
        if (!isValidSubscriptionId(subId)) {
            return false;
        }
        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            if (iSub == null) {
                return false;
            }
            int slotId = getSlotId(subId);
            if (!isValidSlotId(slotId)) {
                return false;
            }
            boolean ifCurrentsoftsim = iSub.getSoftSimCardSlotId() == slotId;
            if (!iSub.isHasSoftSimCard()) {
                ifCurrentsoftsim = false;
            }
            return ifCurrentsoftsim;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static String getOperatorNumericForData(int subId) {
        String result = PhoneConstants.MVNO_TYPE_NONE;
        if (!isValidSubscriptionId(subId)) {
            return PhoneConstants.MVNO_TYPE_NONE;
        }
        try {
            ISub iSub = ISub.Stub.asInterface(ServiceManager.getService("isub"));
            int phoneId = getPhoneId(subId);
            result = !isValidPhoneId(phoneId) ? PhoneConstants.MVNO_TYPE_NONE : iSub != null ? iSub.getOperatorNumericForData(phoneId) : PhoneConstants.MVNO_TYPE_NONE;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
