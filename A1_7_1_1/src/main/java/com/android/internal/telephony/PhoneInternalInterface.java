package com.android.internal.telephony;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.CellLocation;
import android.telephony.ServiceState;
import com.android.internal.telephony.PhoneConstants.DataState;
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
public interface PhoneInternalInterface {
    public static final int BM_10_800M_2 = 15;
    public static final int BM_4_450M = 10;
    public static final int BM_7_700M2 = 12;
    public static final int BM_8_1800M = 13;
    public static final int BM_9_900M = 14;
    public static final int BM_AUS2_BAND = 5;
    public static final int BM_AUS_BAND = 4;
    public static final int BM_AWS = 17;
    public static final int BM_CELL_800 = 6;
    public static final int BM_EURO_BAND = 1;
    public static final int BM_EURO_PAMR = 16;
    public static final int BM_IMT2000 = 11;
    public static final int BM_JPN_BAND = 3;
    public static final int BM_JTACS = 8;
    public static final int BM_KOREA_PCS = 9;
    public static final int BM_NUM_BAND_MODES = 19;
    public static final int BM_PCS = 7;
    public static final int BM_UNSPECIFIED = 0;
    public static final int BM_US_2500M = 18;
    public static final int BM_US_BAND = 2;
    public static final int CDMA_OTA_PROVISION_STATUS_A_KEY_EXCHANGED = 2;
    public static final int CDMA_OTA_PROVISION_STATUS_COMMITTED = 8;
    public static final int CDMA_OTA_PROVISION_STATUS_IMSI_DOWNLOADED = 6;
    public static final int CDMA_OTA_PROVISION_STATUS_MDN_DOWNLOADED = 5;
    public static final int CDMA_OTA_PROVISION_STATUS_NAM_DOWNLOADED = 4;
    public static final int CDMA_OTA_PROVISION_STATUS_OTAPA_ABORTED = 11;
    public static final int CDMA_OTA_PROVISION_STATUS_OTAPA_STARTED = 9;
    public static final int CDMA_OTA_PROVISION_STATUS_OTAPA_STOPPED = 10;
    public static final int CDMA_OTA_PROVISION_STATUS_PRL_DOWNLOADED = 7;
    public static final int CDMA_OTA_PROVISION_STATUS_SPC_RETRIES_EXCEEDED = 1;
    public static final int CDMA_OTA_PROVISION_STATUS_SPL_UNLOCKED = 0;
    public static final int CDMA_OTA_PROVISION_STATUS_SSD_UPDATED = 3;
    public static final int CDMA_RM_AFFILIATED = 1;
    public static final int CDMA_RM_ANY = 2;
    public static final int CDMA_RM_HOME = 0;
    public static final int CDMA_SUBSCRIPTION_NV = 1;
    public static final int CDMA_SUBSCRIPTION_RUIM_SIM = 0;
    public static final int CDMA_SUBSCRIPTION_UNKNOWN = -1;
    public static final boolean DEBUG_PHONE = true;
    public static final String FEATURE_ENABLE_CBS = "enableCBS";
    public static final String FEATURE_ENABLE_DUN = "enableDUN";
    public static final String FEATURE_ENABLE_DUN_ALWAYS = "enableDUNAlways";
    public static final String FEATURE_ENABLE_EMERGENCY = "enableEmergency";
    public static final String FEATURE_ENABLE_FOTA = "enableFOTA";
    public static final String FEATURE_ENABLE_HIPRI = "enableHIPRI";
    public static final String FEATURE_ENABLE_IMS = "enableIMS";
    public static final String FEATURE_ENABLE_MMS = "enableMMS";
    public static final String FEATURE_ENABLE_SUPL = "enableSUPL";
    public static final int NT_MODE_CDMA = 4;
    public static final int NT_MODE_CDMA_NO_EVDO = 5;
    public static final int NT_MODE_EVDO_NO_CDMA = 6;
    public static final int NT_MODE_GLOBAL = 7;
    public static final int NT_MODE_GSM_ONLY = 1;
    public static final int NT_MODE_GSM_UMTS = 3;
    public static final int NT_MODE_LTE_CDMA_AND_EVDO = 8;
    public static final int NT_MODE_LTE_CDMA_EVDO_GSM_WCDMA = 10;
    public static final int NT_MODE_LTE_GSM = 30;
    public static final int NT_MODE_LTE_GSM_WCDMA = 9;
    public static final int NT_MODE_LTE_ONLY = 11;
    public static final int NT_MODE_LTE_TDD_ONLY = 31;
    public static final int NT_MODE_LTE_TDSCDMA = 15;
    public static final int NT_MODE_LTE_TDSCDMA_CDMA_EVDO_GSM_WCDMA = 22;
    public static final int NT_MODE_LTE_TDSCDMA_GSM = 17;
    public static final int NT_MODE_LTE_TDSCDMA_GSM_WCDMA = 20;
    public static final int NT_MODE_LTE_TDSCDMA_WCDMA = 19;
    public static final int NT_MODE_LTE_WCDMA = 12;
    public static final int NT_MODE_TDSCDMA_CDMA_EVDO_GSM_WCDMA = 21;
    public static final int NT_MODE_TDSCDMA_GSM = 16;
    public static final int NT_MODE_TDSCDMA_GSM_WCDMA = 18;
    public static final int NT_MODE_TDSCDMA_ONLY = 13;
    public static final int NT_MODE_TDSCDMA_WCDMA = 14;
    public static final int NT_MODE_WCDMA_ONLY = 2;
    public static final int NT_MODE_WCDMA_PREF = 0;
    public static final int PREFERRED_CDMA_SUBSCRIPTION = 1;
    public static final int PREFERRED_NT_MODE = 0;
    public static final String REASON_APN_CHANGED = "apnChanged";
    public static final String REASON_APN_FAILED = "apnFailed";
    public static final String REASON_APN_SWITCHED = "apnSwitched";
    public static final String REASON_CARRIER_ACTION_DISABLE_METERED_APN = "carrierActionDisableMeteredApn";
    public static final String REASON_CARRIER_CHANGE = "carrierChange";
    public static final String REASON_CDMA_DATA_ATTACHED = "cdmaDataAttached";
    public static final String REASON_CDMA_DATA_DETACHED = "cdmaDataDetached";
    public static final String REASON_CONNECTED = "connected";
    public static final String REASON_DATA_ALLOWED = "dataAllowed";
    public static final String REASON_DATA_ATTACHED = "dataAttached";
    public static final String REASON_DATA_DEPENDENCY_MET = "dependencyMet";
    public static final String REASON_DATA_DEPENDENCY_UNMET = "dependencyUnmet";
    public static final String REASON_DATA_DETACHED = "dataDetached";
    public static final String REASON_DATA_DISABLED = "dataDisabled";
    public static final String REASON_DATA_ENABLED = "dataEnabled";
    public static final String REASON_DATA_SPECIFIC_DISABLED = "specificDisabled";
    public static final String REASON_FDN_DISABLED = "FdnDisabled";
    public static final String REASON_FDN_ENABLED = "FdnEnabled";
    public static final String REASON_IWLAN_AVAILABLE = "iwlanAvailable";
    public static final String REASON_LOST_DATA_CONNECTION = "lostDataConnection";
    public static final String REASON_MD_DATA_RETRY_COUNT_RESET = "modemDataCountReset";
    public static final String REASON_NW_TYPE_CHANGED = "nwTypeChanged";
    public static final String REASON_PCSCF_ADDRESS_FAILED = "pcscfFailed";
    public static final String REASON_PDP_RESET = "pdpReset";
    public static final String REASON_PS_RESTRICT_DISABLED = "psRestrictDisabled";
    public static final String REASON_PS_RESTRICT_ENABLED = "psRestrictEnabled";
    public static final String REASON_RADIO_TURNED_OFF = "radioTurnedOff";
    public static final String REASON_RA_FAILED = "raFailed";
    public static final String REASON_RESTORE_DEFAULT_APN = "restoreDefaultApn";
    public static final String REASON_ROAMING_OFF = "roamingOff";
    public static final String REASON_ROAMING_ON = "roamingOn";
    public static final String REASON_SIM_LOADED = "simLoaded";
    public static final String REASON_SIM_NOT_READY = "simNotReady";
    public static final String REASON_SINGLE_PDN_ARBITRATION = "SinglePdnArbitration";
    public static final String REASON_VOICE_CALL_ENDED = "2GVoiceCallEnded";
    public static final String REASON_VOICE_CALL_STARTED = "2GVoiceCallStarted";
    public static final int TTY_MODE_FULL = 1;
    public static final int TTY_MODE_HCO = 2;
    public static final int TTY_MODE_OFF = 0;
    public static final int TTY_MODE_VCO = 3;

    /*  JADX ERROR: NullPointerException in pass: EnumVisitor
        java.lang.NullPointerException
        	at jadx.core.dex.visitors.EnumVisitor.visit(EnumVisitor.java:102)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
        	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$0(DepthTraversal.java:13)
        	at java.util.ArrayList.forEach(ArrayList.java:1251)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:13)
        	at jadx.core.ProcessClass.process(ProcessClass.java:32)
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    public enum DataActivityState {
        ;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.internal.telephony.PhoneInternalInterface.DataActivityState.<clinit>():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 10 more
            */
        static {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.internal.telephony.PhoneInternalInterface.DataActivityState.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.PhoneInternalInterface.DataActivityState.<clinit>():void");
        }
    }

    /*  JADX ERROR: NullPointerException in pass: EnumVisitor
        java.lang.NullPointerException
        	at jadx.core.dex.visitors.EnumVisitor.visit(EnumVisitor.java:102)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
        	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$0(DepthTraversal.java:13)
        	at java.util.ArrayList.forEach(ArrayList.java:1251)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:13)
        	at jadx.core.ProcessClass.process(ProcessClass.java:32)
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    public enum SuppService {
        ;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.internal.telephony.PhoneInternalInterface.SuppService.<clinit>():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 10 more
            */
        static {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.internal.telephony.PhoneInternalInterface.SuppService.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.PhoneInternalInterface.SuppService.<clinit>():void");
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.internal.telephony.PhoneInternalInterface.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.internal.telephony.PhoneInternalInterface.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.PhoneInternalInterface.<clinit>():void");
    }

    void acceptCall(int i) throws CallStateException;

    void activateCellBroadcastSms(int i, Message message);

    boolean canConference();

    boolean canTransfer();

    void changeBarringPassword(String str, String str2, String str3, Message message);

    void clearDisconnected();

    void conference() throws CallStateException;

    Connection dial(String str, int i) throws CallStateException;

    Connection dial(String str, UUSInfo uUSInfo, int i, Bundle bundle) throws CallStateException;

    void disableLocationUpdates();

    void doGeneralSimAuthentication(int i, int i2, int i3, String str, String str2, Message message);

    void enableLocationUpdates();

    void explicitCallTransfer() throws CallStateException;

    void explicitCallTransfer(String str, int i);

    void getAvailableNetworks(Message message);

    Call getBackgroundCall();

    void getCallForwardingOption(int i, Message message);

    void getCallForwardingOptionForServiceClass(int i, int i2, Message message);

    void getCallWaiting(Message message);

    void getCellBroadcastSmsConfig(Message message);

    CellLocation getCellLocation();

    int getCsFallbackStatus();

    DataActivityState getDataActivityState();

    void getDataCallList(Message message);

    DataState getDataConnectionState(String str);

    boolean getDataEnabled();

    boolean getDataRoamingEnabled();

    String getDeviceId();

    String getDeviceSvn();

    String getEsn();

    void getFacilityLock(String str, String str2, Message message);

    void getFacilityLockForServiceClass(String str, String str2, int i, Message message);

    Call getForegroundCall();

    String getGroupIdLevel1();

    String getGroupIdLevel2();

    IccPhoneBookInterfaceManager getIccPhoneBookInterfaceManager();

    String getImei();

    String getLine1AlphaTag();

    String getLine1Number();

    String getMeid();

    boolean getMute();

    String getMvnoMatchType();

    String getMvnoPattern(String str);

    void getNeighboringCids(Message message);

    void getOutgoingCallerIdDisplay(Message message);

    List<? extends MmiCode> getPendingMmiCodes();

    Call getRingingCall();

    ServiceState getServiceState();

    String getSubscriberId();

    String getVoiceMailAlphaTag();

    String getVoiceMailNumber();

    boolean handleInCallMmiCommands(String str) throws CallStateException;

    boolean handlePinMmi(String str);

    void hangupAll() throws CallStateException;

    void queryPhbStorageInfo(int i, Message message);

    void registerForCipherIndication(Handler handler, int i, Object obj);

    void registerForCrssSuppServiceNotification(Handler handler, int i, Object obj);

    void registerForSuppServiceNotification(Handler handler, int i, Object obj);

    void rejectCall() throws CallStateException;

    void sendDtmf(char c);

    void sendUssdResponse(String str);

    void setCallForwardingOption(int i, int i2, String str, int i3, Message message);

    void setCallForwardingOptionForServiceClass(int i, int i2, String str, int i3, int i4, Message message);

    void setCallWaiting(boolean z, Message message);

    void setCellBroadcastSmsConfig(int[] iArr, Message message);

    void setDataEnabled(boolean z);

    void setDataRoamingEnabled(boolean z);

    void setFacilityLock(String str, boolean z, String str2, Message message);

    void setFacilityLockForServiceClass(String str, boolean z, String str2, int i, Message message);

    boolean setLine1Number(String str, String str2, Message message);

    void setMute(boolean z);

    void setOutgoingCallerIdDisplay(int i, Message message);

    void setRadioPower(boolean z);

    void setVoiceMailNumber(String str, String str2, Message message);

    void startDtmf(char c);

    void stopDtmf();

    void switchHoldingAndActive() throws CallStateException;

    void triggerModeSwitchByEcc(int i, Message message);

    void unregisterForCipherIndication(Handler handler);

    void unregisterForCrssSuppServiceNotification(Handler handler);

    void unregisterForSuppServiceNotification(Handler handler);

    void updateServiceLocation();
}
