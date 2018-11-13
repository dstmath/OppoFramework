package com.android.internal.telephony;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.display.DisplayManager;
import android.hardware.display.DisplayManager.DisplayListener;
import android.net.ConnectivityManager;
import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.net.LocalSocketAddress.Namespace;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.OppoManager;
import android.os.Parcel;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.RegistrantList;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.provider.Settings.Global;
import android.service.carrier.CarrierIdentifier;
import android.telephony.CellInfo;
import android.telephony.ModemActivityInfo;
import android.telephony.NeighboringCellInfo;
import android.telephony.PcoData;
import android.telephony.PhoneNumberUtils;
import android.telephony.PhoneNumberUtils.EccEntry;
import android.telephony.RadioAccessFamily;
import android.telephony.Rlog;
import android.telephony.SignalStrength;
import android.telephony.SmsMessage;
import android.telephony.SmsParameters;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyHistogram;
import android.telephony.TelephonyManager;
import android.telephony.TelephonyManager.MultiSimVariants;
import android.text.TextUtils;
import android.util.Base64;
import android.util.SparseArray;
import android.view.Display;
import com.android.internal.telephony.CommandsInterface.RadioState;
import com.android.internal.telephony.TelephonyProto.TelephonyEvent.RilSetupDataCallResponse.RilDataCallFailCause;
import com.android.internal.telephony.cdma.CdmaCallWaitingNotification;
import com.android.internal.telephony.cdma.CdmaInformationRecords;
import com.android.internal.telephony.cdma.CdmaInformationRecords.CdmaDisplayInfoRec;
import com.android.internal.telephony.cdma.CdmaInformationRecords.CdmaLineControlInfoRec;
import com.android.internal.telephony.cdma.CdmaInformationRecords.CdmaNumberInfoRec;
import com.android.internal.telephony.cdma.CdmaInformationRecords.CdmaRedirectingNumberInfoRec;
import com.android.internal.telephony.cdma.CdmaInformationRecords.CdmaSignalInfoRec;
import com.android.internal.telephony.cdma.CdmaInformationRecords.CdmaT53AudioControlInfoRec;
import com.android.internal.telephony.cdma.CdmaInformationRecords.CdmaT53ClirInfoRec;
import com.android.internal.telephony.cdma.CdmaSmsBroadcastConfigInfo;
import com.android.internal.telephony.dataconnection.DataCallResponse;
import com.android.internal.telephony.dataconnection.DataProfile;
import com.android.internal.telephony.dataconnection.DcFailCause;
import com.android.internal.telephony.gsm.SmsBroadcastConfigInfo;
import com.android.internal.telephony.gsm.SsData;
import com.android.internal.telephony.gsm.SuppCrssNotification;
import com.android.internal.telephony.gsm.SuppServiceNotification;
import com.android.internal.telephony.metrics.TelephonyMetrics;
import com.android.internal.telephony.uicc.IccCardApplicationStatus;
import com.android.internal.telephony.uicc.IccCardStatus;
import com.android.internal.telephony.uicc.IccIoResult;
import com.android.internal.telephony.uicc.IccRefreshResponse;
import com.android.internal.telephony.uicc.IccUtils;
import com.android.internal.telephony.uicc.SIMRecords;
import com.android.internal.telephony.uicc.SpnOverride;
import com.android.internal.telephony.uicc.UiccController;
import com.google.android.mms.pdu.CharacterSets;
import com.mediatek.common.MPlugin;
import com.mediatek.common.telephony.IServiceStateExt;
import com.mediatek.common.telephony.gsm.PBEntry;
import com.mediatek.common.telephony.gsm.PBMemStorage;
import com.mediatek.internal.telephony.CellBroadcastConfigInfo;
import com.mediatek.internal.telephony.EtwsNotification;
import com.mediatek.internal.telephony.FemtoCellInfo;
import com.mediatek.internal.telephony.IccSmsStorageStatus;
import com.mediatek.internal.telephony.NetworkInfoWithAcT;
import com.mediatek.internal.telephony.PseudoBSRecord;
import com.mediatek.internal.telephony.SrvccCallContext;
import com.mediatek.internal.telephony.dataconnection.IaExtendParam;
import com.mediatek.internal.telephony.gsm.GsmVTProvider;
import com.mediatek.internal.telephony.ppl.IPplSmsFilter;
import com.mediatek.internal.telephony.uicc.PhbEntry;
import com.mediatek.internal.telephony.uicc.UsimPBMemInfo;
import com.mediatek.internal.telephony.worldphone.WorldMode;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;

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
    	at jadx.core.utils.BlockUtils.isAllBlocksEmpty(BlockUtils.java:546)
    	at jadx.core.dex.visitors.ExtractFieldInit.getConstructorsList(ExtractFieldInit.java:221)
    	at jadx.core.dex.visitors.ExtractFieldInit.moveCommonFieldsInit(ExtractFieldInit.java:121)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:46)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:42)
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
/*  JADX ERROR: NullPointerException in pass: ClassModifier
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ClassModifier.removeFieldUsageFromConstructor(ClassModifier.java:100)
    	at jadx.core.dex.visitors.ClassModifier.removeSyntheticFields(ClassModifier.java:75)
    	at jadx.core.dex.visitors.ClassModifier.visit(ClassModifier.java:48)
    	at jadx.core.dex.visitors.ClassModifier.visit(ClassModifier.java:40)
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
public final class RIL extends BaseCommands implements CommandsInterface {
    private static final int CARD_TYPE_CSIM = 4;
    private static final int CARD_TYPE_RUIM = 8;
    private static final int CARD_TYPE_SIM = 1;
    private static final int CARD_TYPE_USIM = 2;
    private static final int CDMA_BROADCAST_SMS_NO_OF_SERVICE_CATEGORIES = 31;
    private static final int CDMA_BSI_NO_OF_INTS_STRUCT = 3;
    private static final int DEFAULT_ACK_WAKE_LOCK_TIMEOUT_MS = 200;
    private static final int DEFAULT_BLOCKING_MESSAGE_RESPONSE_TIMEOUT_MS = 2000;
    private static final int DEFAULT_WAKE_LOCK_TIMEOUT_MS = 60000;
    static final int EVENT_ACK_WAKE_LOCK_TIMEOUT = 4;
    static final int EVENT_BLOCKING_RESPONSE_TIMEOUT = 5;
    static final int EVENT_SEND = 1;
    static final int EVENT_SEND_ACK = 3;
    static final int EVENT_WAKE_LOCK_TIMEOUT = 2;
    public static final int FOR_ACK_WAKELOCK = 1;
    public static final int FOR_WAKELOCK = 0;
    public static final int INVALID_WAKELOCK = -1;
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_AS_FAILED = "as_failed";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_AUTHENTICATION_REJECT = "authentication_reject";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_CARD_DROP_RX_BREAK = "card_drop_rx_break";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_CARD_DROP_TIME_OUT = "card_drop_time_out";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_DATA_NOT_ALLOWED = "data_not_allowed";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_DATA_NO_AVAILABLE_APN = "data_no_available_apn";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_DATA_SET_UP_DATA_ERROR = "data_setup_data_error";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_GSM_T3126_EXPIRED = "gsm_t3126_expired";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_LTE_AS_FAILED = "lte_as_failed";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_LTE_AUTHENTICATION_REJECT = "lte_authentication_reject";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_LTE_REG_REJECT = "lte_reg_reject";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_LTE_REG_WITHOUT_LTE = "lte_reg_without_lte";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_MCFG_ICCID_FAILED = "mcfg_iccid_failed";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_MO_DROP = "mo_drop";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_MT_CSFB = "mt_csfb";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_MT_PCH = "mt_pch";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_MT_RACH = "mt_rach";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_MT_REJECT = "mt_reject";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_MT_RLF = "mt_rlf";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_MT_RRC = "mt_rrc";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_REG_REJECT = "reg_rejet";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_RF_MIPI_HW_FAILED = "rf_mipi_hw_failed";
    private static final int LENGTH_1_BYTE = 1;
    private static final int LENGTH_2_BYTES = 2;
    private static final int LENGTH_4_BYTES = 4;
    private static final String[] PROPERTY_RIL_FULL_UICC_TYPE = null;
    private static long PreviousCurrentValue = 0;
    static final int RADIO_SCREEN_OFF = 0;
    static final int RADIO_SCREEN_ON = 1;
    static final int RADIO_SCREEN_UNSET = -1;
    static final int RESPONSE_SOLICITED = 0;
    static final int RESPONSE_SOLICITED_ACK = 2;
    static final int RESPONSE_SOLICITED_ACK_EXP = 3;
    static final int RESPONSE_UNSOLICITED = 1;
    static final int RESPONSE_UNSOLICITED_ACK_EXP = 4;
    static final String RILJ_ACK_WAKELOCK_NAME = "RILJ_ACK_WL";
    static final boolean RILJ_LOGD = true;
    static final boolean RILJ_LOGV = false;
    static final String RILJ_LOG_TAG = "RILJ";
    static final int RIL_HISTOGRAM_BUCKET_COUNT = 5;
    static final int RIL_MAX_COMMAND_BYTES = 20480;
    static final String[] SOCKET_NAME_RIL = null;
    static final int SOCKET_OPEN_RETRY_MILLIS = 4000;
    private static final int SYS_MTK_URC_AUTHENTICATION_REJECT = 395;
    private static final int SYS_MTK_URC_CARD_DROP = 89;
    private static final int SYS_MTK_URC_LTE_AUTHENTICATION_REJECT = 628;
    private static final int SYS_MTK_URC_LTE_REG_REJECT = 625;
    private static final int SYS_MTK_URC_MT_CSFB = 393;
    private static final int SYS_MTK_URC_MT_RACH = 25;
    private static final int SYS_MTK_URC_MT_REJECT = 256;
    private static final int SYS_MTK_URC_MT_RLF_PCH = 5;
    private static final int SYS_MTK_URC_MT_RRC = 133;
    private static final int SYS_MTK_URC_REG_REJECT = 394;
    private static final int SYS_MTK_URC_RF_MIPI_HW_FAILED = 108;
    private static final int SYS_OEM_NW_DIAG_CAUSE_AS_FAILED = 65;
    private static final int SYS_OEM_NW_DIAG_CAUSE_AUTHENTICATION_REJECT = 64;
    private static final int SYS_OEM_NW_DIAG_CAUSE_CALL_BASE = 10;
    private static final int SYS_OEM_NW_DIAG_CAUSE_CARD_DROP_RX_BREAK = 160;
    private static final int SYS_OEM_NW_DIAG_CAUSE_CARD_DROP_TIME_OUT = 161;
    private static final int SYS_OEM_NW_DIAG_CAUSE_DATA_BASE = 110;
    private static final int SYS_OEM_NW_DIAG_CAUSE_DATA_NOT_ALLOWED = 110;
    private static final int SYS_OEM_NW_DIAG_CAUSE_DATA_NO_AVAILABLE_APN = 111;
    private static final int SYS_OEM_NW_DIAG_CAUSE_DATA_SET_UP_DATA_ERROR = 112;
    private static final int SYS_OEM_NW_DIAG_CAUSE_GSM_T3126_EXPIRED = 66;
    private static final int SYS_OEM_NW_DIAG_CAUSE_LTE_AS_FAILED = 60;
    private static final int SYS_OEM_NW_DIAG_CAUSE_LTE_AUTHENTICATION_REJECT = 68;
    private static final int SYS_OEM_NW_DIAG_CAUSE_LTE_REG_REJECT = 61;
    private static final int SYS_OEM_NW_DIAG_CAUSE_LTE_REG_WITHOUT_LTE = 62;
    private static final int SYS_OEM_NW_DIAG_CAUSE_MCFG_ICCID_FAILED = 67;
    private static final int SYS_OEM_NW_DIAG_CAUSE_MO_DROP = 10;
    private static final int SYS_OEM_NW_DIAG_CAUSE_MT_CSFB = 14;
    private static final int SYS_OEM_NW_DIAG_CAUSE_MT_PCH = 13;
    private static final int SYS_OEM_NW_DIAG_CAUSE_MT_RACH = 11;
    private static final int SYS_OEM_NW_DIAG_CAUSE_MT_REJECT = 15;
    private static final int SYS_OEM_NW_DIAG_CAUSE_MT_RLF = 12;
    private static final int SYS_OEM_NW_DIAG_CAUSE_MT_RRC = 16;
    private static final int SYS_OEM_NW_DIAG_CAUSE_REG_BASE = 60;
    private static final int SYS_OEM_NW_DIAG_CAUSE_REG_REJECT = 63;
    private static final int SYS_OEM_NW_DIAG_CAUSE_RF_BASE = 210;
    private static final int SYS_OEM_NW_DIAG_CAUSE_RF_MIPI_HW_FAILED = 210;
    private static final int SYS_OEM_NW_DIAG_CAUSE_UIM_BASE = 160;
    private static final int SYS_OEM_NW_DIAG_RAT_CDMA = 3;
    private static final int SYS_OEM_NW_DIAG_RAT_GSM = 0;
    private static final int SYS_OEM_NW_DIAG_RAT_HDR = 4;
    private static final int SYS_OEM_NW_DIAG_RAT_LTE = 5;
    private static final int SYS_OEM_NW_DIAG_RAT_NONE = -1;
    private static final int SYS_OEM_NW_DIAG_RAT_TDS = 1;
    private static final int SYS_OEM_NW_DIAG_RAT_WCDMA = 2;
    static SparseArray<TelephonyHistogram> mRilTimeHistograms;
    final WakeLock mAckWakeLock;
    final int mAckWakeLockTimeout;
    volatile int mAckWlSequenceNum;
    private final BroadcastReceiver mAirplaneModeListener;
    private final BroadcastReceiver mBatteryStateListener;
    Display mDefaultDisplay;
    int mDefaultDisplayState;
    private final DisplayListener mDisplayListener;
    private dtmfQueueHandler mDtmfReqQueue;
    private final Handler mHandler;
    private Integer mInstanceId;
    BroadcastReceiver mIntentReceiver;
    boolean mIsDevicePlugged;
    Object[] mLastNITZTimeInfo;
    private TelephonyMetrics mMetrics;
    private int mPreviousPreferredType;
    int mRadioScreenState;
    RILReceiver mReceiver;
    Thread mReceiverThread;
    SparseArray<RILRequest> mRequestList;
    RILSender mSender;
    HandlerThread mSenderThread;
    private IServiceStateExt mServiceStateExt;
    LocalSocket mSocket;
    AtomicBoolean mTestingEmergencyCall;
    final WakeLock mWakeLock;
    int mWakeLockCount;
    final int mWakeLockTimeout;
    volatile int mWlSequenceNum;

    /* renamed from: com.android.internal.telephony.RIL$3 */
    class AnonymousClass3 extends BroadcastReceiver {
        final /* synthetic */ RIL this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.RIL.3.<init>(com.android.internal.telephony.RIL):void, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        AnonymousClass3(com.android.internal.telephony.RIL r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.RIL.3.<init>(com.android.internal.telephony.RIL):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.RIL.3.<init>(com.android.internal.telephony.RIL):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.RIL.3.onReceive(android.content.Context, android.content.Intent):void, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public void onReceive(android.content.Context r1, android.content.Intent r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.RIL.3.onReceive(android.content.Context, android.content.Intent):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.RIL.3.onReceive(android.content.Context, android.content.Intent):void");
        }
    }

    /* renamed from: com.android.internal.telephony.RIL$4 */
    class AnonymousClass4 extends BroadcastReceiver {
        private static final int MODE_CDMA_ASSERT = 31;
        private static final int MODE_CDMA_RESET = 32;
        private static final int MODE_CDMA_RILD_NE = 103;
        private static final int MODE_GSM_RILD_NE = 101;
        private static final int MODE_PHONE_PROCESS_JE = 100;
        final /* synthetic */ RIL this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.RIL.4.<init>(com.android.internal.telephony.RIL):void, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        AnonymousClass4(com.android.internal.telephony.RIL r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.RIL.4.<init>(com.android.internal.telephony.RIL):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.RIL.4.<init>(com.android.internal.telephony.RIL):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.RIL.4.onReceive(android.content.Context, android.content.Intent):void, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public void onReceive(android.content.Context r1, android.content.Intent r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.RIL.4.onReceive(android.content.Context, android.content.Intent):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.RIL.4.onReceive(android.content.Context, android.content.Intent):void");
        }
    }

    private class CriticalLogInfo {
        long errcode;
        String extra;
        String issue;
        long rat;
        final /* synthetic */ RIL this$0;
        long type;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.RIL.CriticalLogInfo.<init>(com.android.internal.telephony.RIL, int, int, int, java.lang.String, java.lang.String):void, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public CriticalLogInfo(com.android.internal.telephony.RIL r1, int r2, int r3, int r4, java.lang.String r5, java.lang.String r6) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.RIL.CriticalLogInfo.<init>(com.android.internal.telephony.RIL, int, int, int, java.lang.String, java.lang.String):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.RIL.CriticalLogInfo.<init>(com.android.internal.telephony.RIL, int, int, int, java.lang.String, java.lang.String):void");
        }
    }

    class RILReceiver implements Runnable {
        byte[] buffer;
        final /* synthetic */ RIL this$0;

        RILReceiver(RIL this$0) {
            this.this$0 = this$0;
            this.buffer = new byte[RIL.RIL_MAX_COMMAND_BYTES];
        }

        /* JADX WARNING: Removed duplicated region for block: B:26:0x0128 A:{SYNTHETIC, Splitter: B:26:0x0128} */
        /* JADX WARNING: Removed duplicated region for block: B:37:0x016c A:{SKIP} */
        /* JADX WARNING: Removed duplicated region for block: B:30:0x0131 A:{SYNTHETIC, Splitter: B:30:0x0131} */
        /* JADX WARNING: Missing block: B:61:?, code:
            android.telephony.Rlog.i(com.android.internal.telephony.RIL.RILJ_LOG_TAG, "(" + com.android.internal.telephony.RIL.-get1(r22.this$0) + ") Disconnected from '" + r14 + "' socket");
            com.android.internal.telephony.RIL.-wrap4(r22.this$0, 1, false);
            r22.this$0.setRadioState(com.android.internal.telephony.CommandsInterface.RadioState.RADIO_UNAVAILABLE);
     */
        /* JADX WARNING: Missing block: B:63:?, code:
            r22.this$0.mSocket.close();
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void run() {
            LocalSocket localSocket;
            LocalSocket s;
            Throwable tr;
            int retryCount = 0;
            String rilSocket = "rild";
            while (true) {
                localSocket = null;
                try {
                    if (this.this$0.mInstanceId == null || this.this$0.mInstanceId.intValue() == 0) {
                        rilSocket = RIL.SOCKET_NAME_RIL[0];
                    } else {
                        rilSocket = RIL.SOCKET_NAME_RIL[this.this$0.mInstanceId.intValue()];
                    }
                    this.this$0.riljLog("rilSocket[" + this.this$0.mInstanceId + "] = " + rilSocket);
                    try {
                        s = new LocalSocket();
                        try {
                            s.connect(new LocalSocketAddress(rilSocket, Namespace.RESERVED));
                            retryCount = 0;
                            this.this$0.mSocket = s;
                            Rlog.i(RIL.RILJ_LOG_TAG, "(" + this.this$0.mInstanceId + ") Connected to '" + rilSocket + "' socket");
                            synchronized (this.this$0.mDtmfReqQueue) {
                                this.this$0.riljLog("queue size  " + this.this$0.mDtmfReqQueue.size());
                                for (int i = this.this$0.mDtmfReqQueue.size() - 1; i >= 0; i--) {
                                    this.this$0.mDtmfReqQueue.remove(i);
                                }
                                this.this$0.riljLog("queue size  after " + this.this$0.mDtmfReqQueue.size());
                                if (this.this$0.mDtmfReqQueue.getPendingRequest() != null) {
                                    this.this$0.riljLog("reset pending switch request");
                                    RILRequest pendingRequest = this.this$0.mDtmfReqQueue.getPendingRequest();
                                    if (pendingRequest.mResult != null) {
                                        AsyncResult.forMessage(pendingRequest.mResult, null, null);
                                        pendingRequest.mResult.sendToTarget();
                                    }
                                    this.this$0.mDtmfReqQueue.resetSendChldRequest();
                                    this.this$0.mDtmfReqQueue.setPendingRequest(null);
                                }
                            }
                            int length = 0;
                            try {
                                InputStream is = this.this$0.mSocket.getInputStream();
                                while (true) {
                                    length = RIL.readRilMessage(is, this.buffer);
                                    if (length >= 0) {
                                        Parcel p = Parcel.obtain();
                                        p.unmarshall(this.buffer, 0, length);
                                        p.setDataPosition(0);
                                        this.this$0.processResponse(p);
                                        p.recycle();
                                    }
                                    break;
                                }
                            } catch (IOException ex) {
                                Rlog.i(RIL.RILJ_LOG_TAG, "'" + rilSocket + "' socket closed", ex);
                            } catch (Throwable tr2) {
                                Rlog.e(RIL.RILJ_LOG_TAG, "Uncaught exception read length=" + length + "Exception:" + tr2.toString());
                            }
                        } catch (IOException e) {
                            localSocket = s;
                            if (localSocket != null) {
                            }
                            if (retryCount != 8) {
                            }
                            try {
                                Thread.sleep(4000);
                            } catch (InterruptedException e2) {
                            }
                            retryCount++;
                        }
                    } catch (IOException e3) {
                        if (localSocket != null) {
                            try {
                                localSocket.close();
                            } catch (IOException e4) {
                            }
                        }
                        if (retryCount != 8) {
                            Rlog.e(RIL.RILJ_LOG_TAG, "Couldn't find '" + rilSocket + "' socket after " + retryCount + " times, continuing to retry silently");
                        } else if (retryCount >= 0 && retryCount < 8) {
                            Rlog.i(RIL.RILJ_LOG_TAG, "Couldn't find '" + rilSocket + "' socket; retrying after timeout");
                        }
                        Thread.sleep(4000);
                        retryCount++;
                    }
                } catch (Throwable th) {
                    tr2 = th;
                }
            }
            try {
                this.this$0.mSocket = null;
                RILRequest.resetSerial();
            } catch (Throwable th2) {
                tr2 = th2;
                localSocket = s;
            }
            Rlog.e(RIL.RILJ_LOG_TAG, "Uncaught exception", tr2);
            this.this$0.notifyRegistrantsRilConnectionChanged(-1);
        }
    }

    class RILSender extends Handler implements Runnable {
        byte[] dataLength;
        final /* synthetic */ RIL this$0;

        public RILSender(RIL this$0, Looper looper) {
            this.this$0 = this$0;
            super(looper);
            this.dataLength = new byte[4];
        }

        public void run() {
        }

        public void handleMessage(Message msg) {
            RILRequest rr = msg.obj;
            switch (msg.what) {
                case 1:
                case 3:
                    try {
                        LocalSocket s = this.this$0.mSocket;
                        if (s == null) {
                            rr.onError(1, null);
                            this.this$0.decrementWakeLock(rr);
                            rr.release();
                            return;
                        }
                        byte[] data = rr.mParcel.marshall();
                        if (msg.what != 3) {
                            synchronized (this.this$0.mRequestList) {
                                rr.mStartTimeMs = SystemClock.elapsedRealtime();
                                this.this$0.mRequestList.append(rr.mSerial, rr);
                                rr.mParcel.recycle();
                                rr.mParcel = null;
                            }
                        } else {
                            rr.mParcel.recycle();
                            rr.mParcel = null;
                        }
                        if (data.length > RIL.RIL_MAX_COMMAND_BYTES) {
                            throw new RuntimeException("Parcel larger than max bytes allowed! " + data.length);
                        }
                        byte[] bArr = this.dataLength;
                        this.dataLength[1] = (byte) 0;
                        bArr[0] = (byte) 0;
                        this.dataLength[2] = (byte) ((data.length >> 8) & 255);
                        this.dataLength[3] = (byte) (data.length & 255);
                        s.getOutputStream().write(this.dataLength);
                        s.getOutputStream().write(data);
                        if (msg.what == 3) {
                            rr.release();
                            return;
                        }
                    } catch (IOException ex) {
                        Rlog.e(RIL.RILJ_LOG_TAG, "IOException", ex);
                        if (this.this$0.findAndRemoveRequestFromList(rr.mSerial) != null) {
                            rr.onError(1, null);
                            this.this$0.decrementWakeLock(rr);
                            rr.release();
                            return;
                        }
                    } catch (RuntimeException exc) {
                        Rlog.e(RIL.RILJ_LOG_TAG, "Uncaught exception ", exc);
                        if (this.this$0.findAndRemoveRequestFromList(rr.mSerial) != null) {
                            rr.onError(2, null);
                            this.this$0.decrementWakeLock(rr);
                            rr.release();
                            return;
                        }
                    }
                    break;
                case 2:
                    synchronized (this.this$0.mRequestList) {
                        if (msg.arg1 == this.this$0.mWlSequenceNum && this.this$0.clearWakeLock(0)) {
                            int count = this.this$0.mRequestList.size();
                            Rlog.d(RIL.RILJ_LOG_TAG, "WAKE_LOCK_TIMEOUT  mRequestList=" + count);
                            for (int i = 0; i < count; i++) {
                                rr = (RILRequest) this.this$0.mRequestList.valueAt(i);
                                Rlog.d(RIL.RILJ_LOG_TAG, i + ": [" + rr.mSerial + "] " + RIL.requestToString(rr.mRequest));
                            }
                        }
                    }
                case 4:
                    if (msg.arg1 != this.this$0.mAckWlSequenceNum || this.this$0.clearWakeLock(1)) {
                    }
                case 5:
                    rr = this.this$0.findAndRemoveRequestFromList(msg.arg1);
                    if (rr != null) {
                        if (rr.mResult != null) {
                            AsyncResult.forMessage(rr.mResult, RIL.getResponseForTimedOutRILRequest(rr), null);
                            rr.mResult.sendToTarget();
                            this.this$0.mMetrics.writeOnRilTimeoutResponse(this.this$0.mInstanceId.intValue(), rr.mSerial, rr.mRequest);
                        }
                        this.this$0.decrementWakeLock(rr);
                        rr.release();
                        break;
                    }
                    break;
            }
        }
    }

    private class dtmfQueueHandler {
        private final boolean DTMF_STATUS_START;
        private final boolean DTMF_STATUS_STOP;
        public final int MAXIMUM_DTMF_REQUEST;
        private Vector mDtmfQueue;
        private boolean mDtmfStatus;
        private boolean mIsSendChldRequest;
        private RILRequest mPendingCHLDRequest;
        final /* synthetic */ RIL this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.RIL.dtmfQueueHandler.<init>(com.android.internal.telephony.RIL):void, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public dtmfQueueHandler(com.android.internal.telephony.RIL r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.RIL.dtmfQueueHandler.<init>(com.android.internal.telephony.RIL):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.RIL.dtmfQueueHandler.<init>(com.android.internal.telephony.RIL):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.internal.telephony.RIL.dtmfQueueHandler.add(com.android.internal.telephony.RILRequest):void, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public void add(com.android.internal.telephony.RILRequest r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.internal.telephony.RIL.dtmfQueueHandler.add(com.android.internal.telephony.RILRequest):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.RIL.dtmfQueueHandler.add(com.android.internal.telephony.RILRequest):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.internal.telephony.RIL.dtmfQueueHandler.get():com.android.internal.telephony.RILRequest, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public com.android.internal.telephony.RILRequest get() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.internal.telephony.RIL.dtmfQueueHandler.get():com.android.internal.telephony.RILRequest, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.RIL.dtmfQueueHandler.get():com.android.internal.telephony.RILRequest");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.internal.telephony.RIL.dtmfQueueHandler.getPendingRequest():com.android.internal.telephony.RILRequest, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public com.android.internal.telephony.RILRequest getPendingRequest() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.internal.telephony.RIL.dtmfQueueHandler.getPendingRequest():com.android.internal.telephony.RILRequest, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.RIL.dtmfQueueHandler.getPendingRequest():com.android.internal.telephony.RILRequest");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: com.android.internal.telephony.RIL.dtmfQueueHandler.hasSendChldRequest():boolean, dex:  in method: com.android.internal.telephony.RIL.dtmfQueueHandler.hasSendChldRequest():boolean, dex: 
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
            Caused by: jadx.core.utils.exceptions.DecodeException: null in method: com.android.internal.telephony.RIL.dtmfQueueHandler.hasSendChldRequest():boolean, dex: 
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
            	... 10 more
            Caused by: java.io.EOFException
            	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
            	at com.android.dx.io.instructions.ShortArrayCodeInput.readInt(ShortArrayCodeInput.java:62)
            	at com.android.dx.io.instructions.InstructionCodec$24.decode(InstructionCodec.java:550)
            	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
            	... 11 more
            */
        public boolean hasSendChldRequest() {
            /*
            // Can't load method instructions: Load method exception: null in method: com.android.internal.telephony.RIL.dtmfQueueHandler.hasSendChldRequest():boolean, dex:  in method: com.android.internal.telephony.RIL.dtmfQueueHandler.hasSendChldRequest():boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.RIL.dtmfQueueHandler.hasSendChldRequest():boolean");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00ef in method: com.android.internal.telephony.RIL.dtmfQueueHandler.isStart():boolean, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00ef
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public boolean isStart() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00ef in method: com.android.internal.telephony.RIL.dtmfQueueHandler.isStart():boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.RIL.dtmfQueueHandler.isStart():boolean");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.internal.telephony.RIL.dtmfQueueHandler.remove(int):void, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public void remove(int r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.internal.telephony.RIL.dtmfQueueHandler.remove(int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.RIL.dtmfQueueHandler.remove(int):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.internal.telephony.RIL.dtmfQueueHandler.remove(com.android.internal.telephony.RILRequest):void, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public void remove(com.android.internal.telephony.RILRequest r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.internal.telephony.RIL.dtmfQueueHandler.remove(com.android.internal.telephony.RILRequest):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.RIL.dtmfQueueHandler.remove(com.android.internal.telephony.RILRequest):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: com.android.internal.telephony.RIL.dtmfQueueHandler.resetSendChldRequest():void, dex:  in method: com.android.internal.telephony.RIL.dtmfQueueHandler.resetSendChldRequest():void, dex: 
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
            Caused by: jadx.core.utils.exceptions.DecodeException: null in method: com.android.internal.telephony.RIL.dtmfQueueHandler.resetSendChldRequest():void, dex: 
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
            	... 10 more
            Caused by: java.io.EOFException
            	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
            	at com.android.dx.io.instructions.ShortArrayCodeInput.readInt(ShortArrayCodeInput.java:62)
            	at com.android.dx.io.instructions.InstructionCodec$24.decode(InstructionCodec.java:550)
            	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
            	... 11 more
            */
        public void resetSendChldRequest() {
            /*
            // Can't load method instructions: Load method exception: null in method: com.android.internal.telephony.RIL.dtmfQueueHandler.resetSendChldRequest():void, dex:  in method: com.android.internal.telephony.RIL.dtmfQueueHandler.resetSendChldRequest():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.RIL.dtmfQueueHandler.resetSendChldRequest():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.RIL.dtmfQueueHandler.setPendingRequest(com.android.internal.telephony.RILRequest):void, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public void setPendingRequest(com.android.internal.telephony.RILRequest r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.RIL.dtmfQueueHandler.setPendingRequest(com.android.internal.telephony.RILRequest):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.RIL.dtmfQueueHandler.setPendingRequest(com.android.internal.telephony.RILRequest):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: com.android.internal.telephony.RIL.dtmfQueueHandler.setSendChldRequest():void, dex:  in method: com.android.internal.telephony.RIL.dtmfQueueHandler.setSendChldRequest():void, dex: 
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
            Caused by: jadx.core.utils.exceptions.DecodeException: null in method: com.android.internal.telephony.RIL.dtmfQueueHandler.setSendChldRequest():void, dex: 
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
            	... 10 more
            Caused by: java.io.EOFException
            	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
            	at com.android.dx.io.instructions.ShortArrayCodeInput.readInt(ShortArrayCodeInput.java:62)
            	at com.android.dx.io.instructions.InstructionCodec$24.decode(InstructionCodec.java:550)
            	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
            	... 11 more
            */
        public void setSendChldRequest() {
            /*
            // Can't load method instructions: Load method exception: null in method: com.android.internal.telephony.RIL.dtmfQueueHandler.setSendChldRequest():void, dex:  in method: com.android.internal.telephony.RIL.dtmfQueueHandler.setSendChldRequest():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.RIL.dtmfQueueHandler.setSendChldRequest():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.internal.telephony.RIL.dtmfQueueHandler.size():int, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public int size() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.internal.telephony.RIL.dtmfQueueHandler.size():int, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.RIL.dtmfQueueHandler.size():int");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00eb in method: com.android.internal.telephony.RIL.dtmfQueueHandler.start():void, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00eb
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public void start() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00eb in method: com.android.internal.telephony.RIL.dtmfQueueHandler.start():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.RIL.dtmfQueueHandler.start():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00eb in method: com.android.internal.telephony.RIL.dtmfQueueHandler.stop():void, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00eb
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public void stop() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00eb in method: com.android.internal.telephony.RIL.dtmfQueueHandler.stop():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.RIL.dtmfQueueHandler.stop():void");
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.internal.telephony.RIL.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.internal.telephony.RIL.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.RIL.<clinit>():void");
    }

    public static List<TelephonyHistogram> getTelephonyRILTimingHistograms() {
        List<TelephonyHistogram> list;
        synchronized (mRilTimeHistograms) {
            list = new ArrayList(mRilTimeHistograms.size());
            for (int i = 0; i < mRilTimeHistograms.size(); i++) {
                list.add(new TelephonyHistogram((TelephonyHistogram) mRilTimeHistograms.valueAt(i)));
            }
        }
        return list;
    }

    private static Object getResponseForTimedOutRILRequest(RILRequest rr) {
        if (rr == null) {
            return null;
        }
        Object timeoutResponse = null;
        switch (rr.mRequest) {
            case 135:
                timeoutResponse = new ModemActivityInfo(0, 0, 0, new int[5], 0, 0);
                break;
        }
        return timeoutResponse;
    }

    private static int readRilMessage(InputStream is, byte[] buffer) throws IOException {
        int countRead;
        int offset = 0;
        int remaining = 4;
        do {
            countRead = is.read(buffer, offset, remaining);
            if (countRead < 0) {
                Rlog.e(RILJ_LOG_TAG, "Hit EOS reading message length");
                return -1;
            }
            offset += countRead;
            remaining -= countRead;
        } while (remaining > 0);
        int messageLength = ((((buffer[0] & 255) << 24) | ((buffer[1] & 255) << 16)) | ((buffer[2] & 255) << 8)) | (buffer[3] & 255);
        offset = 0;
        remaining = messageLength;
        do {
            countRead = is.read(buffer, offset, remaining);
            if (countRead < 0) {
                Rlog.e(RILJ_LOG_TAG, "Hit EOS reading message.  messageLength=" + messageLength + " remaining=" + remaining);
                return -1;
            }
            offset += countRead;
            remaining -= countRead;
        } while (remaining > 0);
        return messageLength;
    }

    public RIL(Context context, int preferredNetworkType, int cdmaSubscription) {
        this(context, preferredNetworkType, cdmaSubscription, null);
    }

    public RIL(Context context, int preferredNetworkType, int cdmaSubscription, Integer instanceId) {
        super(context);
        this.mDefaultDisplayState = 0;
        this.mRadioScreenState = -1;
        this.mIsDevicePlugged = false;
        this.mWlSequenceNum = 0;
        this.mAckWlSequenceNum = 0;
        this.mRequestList = new SparseArray();
        this.mTestingEmergencyCall = new AtomicBoolean(false);
        this.mMetrics = TelephonyMetrics.getInstance();
        this.mHandler = new Handler();
        this.mDisplayListener = new DisplayListener() {
            public void onDisplayAdded(int displayId) {
            }

            public void onDisplayRemoved(int displayId) {
            }

            public void onDisplayChanged(int displayId) {
                boolean z = false;
                RIL.this.riljLog("onDisplayChanged: displayId = " + displayId);
                if (displayId == 0) {
                    int oldState = RIL.this.mDefaultDisplayState;
                    RIL.this.mDefaultDisplayState = RIL.this.mDefaultDisplay.getState();
                    if (RIL.this.mDefaultDisplayState != oldState) {
                        RIL.this.updateScreenState();
                        RegistrantList registrantList = RIL.this.mOemScreenRegistrants;
                        if (RIL.this.mDefaultDisplayState == 2) {
                            z = true;
                        }
                        registrantList.notifyRegistrants(new AsyncResult(null, Boolean.valueOf(z), null));
                    }
                }
            }
        };
        this.mBatteryStateListener = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                boolean z = false;
                boolean oldState = RIL.this.mIsDevicePlugged;
                RIL ril = RIL.this;
                if (intent.getIntExtra("plugged", 0) != 0) {
                    z = true;
                }
                ril.mIsDevicePlugged = z;
                if (RIL.this.mIsDevicePlugged != oldState) {
                    RIL.this.updateScreenState();
                }
            }
        };
        this.mAirplaneModeListener = new AnonymousClass3(this);
        this.mPreviousPreferredType = -1;
        this.mDtmfReqQueue = new dtmfQueueHandler(this);
        this.mIntentReceiver = new AnonymousClass4(this);
        riljLog("RIL(context, preferredNetworkType=" + preferredNetworkType + " cdmaSubscription=" + cdmaSubscription + ")");
        this.mContext = context;
        this.mCdmaSubscription = cdmaSubscription;
        this.mPreferredNetworkType = preferredNetworkType;
        this.mPhoneType = 0;
        this.mInstanceId = instanceId;
        PowerManager pm = (PowerManager) context.getSystemService("power");
        this.mWakeLock = pm.newWakeLock(1, RILJ_LOG_TAG);
        this.mWakeLock.setReferenceCounted(false);
        this.mAckWakeLock = pm.newWakeLock(1, RILJ_ACK_WAKELOCK_NAME);
        this.mAckWakeLock.setReferenceCounted(false);
        this.mWakeLockTimeout = SystemProperties.getInt("ro.ril.wake_lock_timeout", 60000);
        this.mAckWakeLockTimeout = SystemProperties.getInt("ro.ril.wake_lock_timeout", 200);
        this.mWakeLockCount = 0;
        this.mSenderThread = new HandlerThread("RILSender" + this.mInstanceId);
        this.mSenderThread.start();
        this.mSender = new RILSender(this, this.mSenderThread.getLooper());
        if (((ConnectivityManager) context.getSystemService("connectivity")).isNetworkSupported(0)) {
            riljLog("Starting RILReceiver" + this.mInstanceId);
            this.mReceiver = new RILReceiver(this);
            this.mReceiverThread = new Thread(this.mReceiver, "RILReceiver" + this.mInstanceId);
            this.mReceiverThread.start();
            DisplayManager dm = (DisplayManager) context.getSystemService("display");
            this.mDefaultDisplay = dm.getDisplay(0);
            this.mDefaultDisplayState = this.mDefaultDisplay.getState();
            dm.registerDisplayListener(this.mDisplayListener, null);
            this.mDefaultDisplayState = this.mDefaultDisplay.getState();
            if (this.mInstanceId.intValue() == 0) {
                IntentFilter filter = new IntentFilter();
                filter.addAction("com.mtk.TEST_TRM");
                context.registerReceiver(this.mIntentReceiver, filter);
            }
            IntentFilter filterAirplane = new IntentFilter();
            filterAirplane.addAction("android.intent.action.AIRPLANE_MODE");
            context.registerReceiver(this.mAirplaneModeListener, filterAirplane);
        } else {
            riljLog("Not starting RILReceiver: wifi-only");
        }
        if (!SystemProperties.get("ro.mtk_bsp_package").equals("1")) {
            try {
                this.mServiceStateExt = (IServiceStateExt) MPlugin.createInstance(IServiceStateExt.class.getName(), context);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void getVoiceRadioTechnology(Message result) {
        RILRequest rr = RILRequest.obtain(108, result);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    public void getImsRegistrationState(Message result) {
        RILRequest rr = RILRequest.obtain(112, result);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    public void setOnNITZTime(Handler h, int what, Object obj) {
        super.setOnNITZTime(h, what, obj);
        if (this.mLastNITZTimeInfo != null) {
            this.mNITZTimeRegistrant.notifyRegistrant(new AsyncResult(null, this.mLastNITZTimeInfo, null));
        }
    }

    public void getIccCardStatus(Message result) {
        RILRequest rr = RILRequest.obtain(1, result);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    public void setUiccSubscription(int slotId, int appIndex, int subId, int subStatus, Message result) {
        RILRequest rr = RILRequest.obtain(RilDataCallFailCause.PDP_FAIL_AUTH_FAILURE_ON_EMERGENCY_CALL, result);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " slot: " + slotId + " appIndex: " + appIndex + " subId: " + subId + " subStatus: " + subStatus);
        rr.mParcel.writeInt(slotId);
        rr.mParcel.writeInt(appIndex);
        rr.mParcel.writeInt(subId);
        rr.mParcel.writeInt(subStatus);
        send(rr);
    }

    public void setDataAllowed(boolean allowed, Message result) {
        int i = 1;
        RILRequest rr = RILRequest.obtain(123, result);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " allowed: " + allowed);
        rr.mParcel.writeInt(1);
        Parcel parcel = rr.mParcel;
        if (!allowed) {
            i = 0;
        }
        parcel.writeInt(i);
        send(rr);
    }

    public void setPsRegistration(boolean register, int mode, Message result) {
        int i = 1;
        RILRequest rr = RILRequest.obtain(2151, result);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " register: " + register + ", mode: " + mode);
        rr.mParcel.writeInt(1);
        Parcel parcel = rr.mParcel;
        if (!register) {
            i = 0;
        }
        parcel.writeInt(i);
        rr.mParcel.writeInt(mode);
        send(rr);
    }

    public void supplyIccPin(String pin, Message result) {
        supplyIccPinForApp(pin, null, result);
    }

    public void supplyIccPinForApp(String pin, String aid, Message result) {
        RILRequest rr = RILRequest.obtain(2, result);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        rr.mParcel.writeInt(2);
        rr.mParcel.writeString(pin);
        rr.mParcel.writeString(aid);
        send(rr);
    }

    public void supplyIccPuk(String puk, String newPin, Message result) {
        supplyIccPukForApp(puk, newPin, null, result);
    }

    public void supplyIccPukForApp(String puk, String newPin, String aid, Message result) {
        RILRequest rr = RILRequest.obtain(3, result);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        rr.mParcel.writeInt(3);
        rr.mParcel.writeString(puk);
        rr.mParcel.writeString(newPin);
        rr.mParcel.writeString(aid);
        send(rr);
    }

    public void supplyIccPin2(String pin, Message result) {
        supplyIccPin2ForApp(pin, null, result);
    }

    public void supplyIccPin2ForApp(String pin, String aid, Message result) {
        RILRequest rr = RILRequest.obtain(4, result);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        rr.mParcel.writeInt(2);
        rr.mParcel.writeString(pin);
        rr.mParcel.writeString(aid);
        send(rr);
    }

    public void supplyIccPuk2(String puk2, String newPin2, Message result) {
        supplyIccPuk2ForApp(puk2, newPin2, null, result);
    }

    public void supplyIccPuk2ForApp(String puk, String newPin2, String aid, Message result) {
        RILRequest rr = RILRequest.obtain(5, result);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        rr.mParcel.writeInt(3);
        rr.mParcel.writeString(puk);
        rr.mParcel.writeString(newPin2);
        rr.mParcel.writeString(aid);
        send(rr);
    }

    public void changeIccPin(String oldPin, String newPin, Message result) {
        changeIccPinForApp(oldPin, newPin, null, result);
    }

    public void changeIccPinForApp(String oldPin, String newPin, String aid, Message result) {
        RILRequest rr = RILRequest.obtain(6, result);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        rr.mParcel.writeInt(3);
        rr.mParcel.writeString(oldPin);
        rr.mParcel.writeString(newPin);
        rr.mParcel.writeString(aid);
        send(rr);
    }

    public void changeIccPin2(String oldPin2, String newPin2, Message result) {
        changeIccPin2ForApp(oldPin2, newPin2, null, result);
    }

    public void changeIccPin2ForApp(String oldPin2, String newPin2, String aid, Message result) {
        RILRequest rr = RILRequest.obtain(7, result);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        rr.mParcel.writeInt(3);
        rr.mParcel.writeString(oldPin2);
        rr.mParcel.writeString(newPin2);
        rr.mParcel.writeString(aid);
        send(rr);
    }

    public void changeBarringPassword(String facility, String oldPwd, String newPwd, Message result) {
        RILRequest rr = RILRequest.obtain(44, result);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        rr.mParcel.writeInt(3);
        rr.mParcel.writeString(facility);
        rr.mParcel.writeString(oldPwd);
        rr.mParcel.writeString(newPwd);
        send(rr);
    }

    public void supplyNetworkDepersonalization(String netpin, Message result) {
        RILRequest rr = RILRequest.obtain(8, result);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        rr.mParcel.writeInt(1);
        rr.mParcel.writeString(netpin);
        send(rr);
    }

    public void getCurrentCalls(Message result) {
        RILRequest rr = RILRequest.obtain(9, result);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        if (Global.getInt(this.mContext.getContentResolver(), "airplane_mode_on", 0) > 0) {
            riljLog("Return RADIO_NOT_AVAILABLE in airplane mode");
            rr.onError(1, null);
            rr.release();
            return;
        }
        send(rr);
    }

    @Deprecated
    public void getPDPContextList(Message result) {
        getDataCallList(result);
    }

    public void getDataCallList(Message result) {
        RILRequest rr = RILRequest.obtain(57, result);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    public void dial(String address, int clirMode, Message result) {
        dial(address, clirMode, null, result);
    }

    public void dial(String address, int clirMode, UUSInfo uusInfo, Message result) {
        RILRequest rr;
        if (PhoneNumberUtils.isUriNumber(address)) {
            rr = RILRequest.obtain(2087, result);
            rr.mParcel.writeString(address);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            this.mMetrics.writeRilDial(this.mInstanceId.intValue(), rr.mSerial, clirMode, uusInfo);
            send(rr);
            return;
        }
        rr = RILRequest.obtain(10, result);
        rr.mParcel.writeString(address);
        rr.mParcel.writeInt(clirMode);
        if (uusInfo == null) {
            rr.mParcel.writeInt(0);
        } else {
            rr.mParcel.writeInt(1);
            rr.mParcel.writeInt(uusInfo.getType());
            rr.mParcel.writeInt(uusInfo.getDcs());
            rr.mParcel.writeByteArray(uusInfo.getUserData());
        }
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        this.mMetrics.writeRilDial(this.mInstanceId.intValue(), rr.mSerial, clirMode, uusInfo);
        send(rr);
    }

    public void getIMSI(Message result) {
        getIMSIForApp(null, result);
    }

    public void getIMSIForApp(String aid, Message result) {
        RILRequest rr = RILRequest.obtain(11, result);
        rr.mParcel.writeInt(1);
        rr.mParcel.writeString(aid);
        riljLog(rr.serialString() + "> getIMSI: " + requestToString(rr.mRequest) + " aid: " + aid);
        send(rr);
    }

    public void getIMEI(Message result) {
        RILRequest rr = RILRequest.obtain(38, result);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    public void getIMEISV(Message result) {
        RILRequest rr = RILRequest.obtain(39, result);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    public void hangupConnection(int gsmIndex, Message result) {
        riljLog("hangupConnection: gsmIndex=" + gsmIndex);
        RILRequest rr = RILRequest.obtain(12, result);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " " + gsmIndex);
        this.mMetrics.writeRilHangup(this.mInstanceId.intValue(), rr.mSerial, gsmIndex);
        rr.mParcel.writeInt(1);
        rr.mParcel.writeInt(gsmIndex);
        send(rr);
    }

    public void hangupWaitingOrBackground(Message result) {
        RILRequest rr = RILRequest.obtain(13, result);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        this.mMetrics.writeRilHangup(this.mInstanceId.intValue(), rr.mSerial, -1);
        send(rr);
    }

    public void hangupForegroundResumeBackground(Message result) {
        RILRequest rr = RILRequest.obtain(14, result);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        this.mMetrics.writeRilHangup(this.mInstanceId.intValue(), rr.mSerial, -1);
        send(rr);
    }

    public void switchWaitingOrHoldingAndActive(Message result) {
        RILRequest rr = RILRequest.obtain(15, result);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        handleChldRelatedRequest(rr);
    }

    public void conference(Message result) {
        RILRequest rr = RILRequest.obtain(16, result);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        handleChldRelatedRequest(rr);
    }

    public void setPreferredVoicePrivacy(boolean enable, Message result) {
        int i = 1;
        RILRequest rr = RILRequest.obtain(82, result);
        rr.mParcel.writeInt(1);
        Parcel parcel = rr.mParcel;
        if (!enable) {
            i = 0;
        }
        parcel.writeInt(i);
        send(rr);
    }

    public void getPreferredVoicePrivacy(Message result) {
        send(RILRequest.obtain(83, result));
    }

    public void separateConnection(int gsmIndex, Message result) {
        RILRequest rr = RILRequest.obtain(52, result);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " " + gsmIndex);
        rr.mParcel.writeInt(1);
        rr.mParcel.writeInt(gsmIndex);
        handleChldRelatedRequest(rr);
    }

    public void acceptCall(Message result) {
        RILRequest rr = RILRequest.obtain(40, result);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        this.mMetrics.writeRilAnswer(this.mInstanceId.intValue(), rr.mSerial);
        send(rr);
    }

    public void rejectCall(Message result) {
        RILRequest rr = RILRequest.obtain(17, result);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    public void explicitCallTransfer(Message result) {
        RILRequest rr = RILRequest.obtain(72, result);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        handleChldRelatedRequest(rr);
    }

    public void getLastCallFailCause(Message result) {
        RILRequest rr = RILRequest.obtain(18, result);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    @Deprecated
    public void getLastPdpFailCause(Message result) {
        getLastDataCallFailCause(result);
    }

    public void getLastDataCallFailCause(Message result) {
        RILRequest rr = RILRequest.obtain(56, result);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    public void setMute(boolean enableMute, Message response) {
        int i = 1;
        RILRequest rr = RILRequest.obtain(53, response);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " " + enableMute);
        rr.mParcel.writeInt(1);
        Parcel parcel = rr.mParcel;
        if (!enableMute) {
            i = 0;
        }
        parcel.writeInt(i);
        send(rr);
    }

    public void getMute(Message response) {
        RILRequest rr = RILRequest.obtain(54, response);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    public void getSignalStrength(Message result) {
        RILRequest rr = RILRequest.obtain(19, result);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    public void getVoiceRegistrationState(Message result) {
        RILRequest rr = RILRequest.obtain(20, result);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    public void getDataRegistrationState(Message result) {
        RILRequest rr = RILRequest.obtain(21, result);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    public void getOperator(Message result) {
        RILRequest rr = RILRequest.obtain(22, result);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    public void getHardwareConfig(Message result) {
        RILRequest rr = RILRequest.obtain(124, result);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    public void sendDtmf(char c, Message result) {
        RILRequest rr = RILRequest.obtain(24, result);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        rr.mParcel.writeString(Character.toString(c));
        send(rr);
    }

    public void startDtmf(char c, Message result) {
        synchronized (this.mDtmfReqQueue) {
            if (!this.mDtmfReqQueue.hasSendChldRequest()) {
                int size = this.mDtmfReqQueue.size();
                this.mDtmfReqQueue.getClass();
                if (size < 32) {
                    if (this.mDtmfReqQueue.isStart()) {
                        riljLog("DTMF status conflict, want to start DTMF when status is " + this.mDtmfReqQueue.isStart());
                    } else {
                        RILRequest rr = RILRequest.obtain(49, result);
                        rr.mParcel.writeString(Character.toString(c));
                        this.mDtmfReqQueue.start();
                        this.mDtmfReqQueue.add(rr);
                        if (this.mDtmfReqQueue.size() == 1) {
                            riljLog("send start dtmf");
                            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
                            send(rr);
                        }
                    }
                }
            }
        }
    }

    public void stopDtmf(Message result) {
        synchronized (this.mDtmfReqQueue) {
            if (!this.mDtmfReqQueue.hasSendChldRequest()) {
                int size = this.mDtmfReqQueue.size();
                this.mDtmfReqQueue.getClass();
                if (size < 32) {
                    if (this.mDtmfReqQueue.isStart()) {
                        RILRequest rr = RILRequest.obtain(50, result);
                        this.mDtmfReqQueue.stop();
                        this.mDtmfReqQueue.add(rr);
                        if (this.mDtmfReqQueue.size() == 1) {
                            riljLog("send stop dtmf");
                            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
                            send(rr);
                        }
                    } else {
                        riljLog("DTMF status conflict, want to start DTMF when status is " + this.mDtmfReqQueue.isStart());
                    }
                }
            }
        }
    }

    public void sendBurstDtmf(String dtmfString, int on, int off, Message result) {
        RILRequest rr = RILRequest.obtain(85, result);
        rr.mParcel.writeInt(3);
        rr.mParcel.writeString(dtmfString);
        rr.mParcel.writeString(Integer.toString(on));
        rr.mParcel.writeString(Integer.toString(off));
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " : " + dtmfString);
        send(rr);
    }

    private void constructGsmSendSmsRilRequest(RILRequest rr, String smscPDU, String pdu) {
        rr.mParcel.writeInt(2);
        rr.mParcel.writeString(smscPDU);
        rr.mParcel.writeString(pdu);
    }

    public void sendSMS(String smscPDU, String pdu, Message result) {
        RILRequest rr = RILRequest.obtain(25, result);
        constructGsmSendSmsRilRequest(rr, smscPDU, pdu);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        this.mMetrics.writeRilSendSms(this.mInstanceId.intValue(), rr.mSerial, 1, 1);
        send(rr);
    }

    public void sendSMSExpectMore(String smscPDU, String pdu, Message result) {
        RILRequest rr = RILRequest.obtain(26, result);
        constructGsmSendSmsRilRequest(rr, smscPDU, pdu);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        this.mMetrics.writeRilSendSms(this.mInstanceId.intValue(), rr.mSerial, 1, 1);
        send(rr);
    }

    private void constructCdmaSendSmsRilRequest(RILRequest rr, byte[] pdu) {
        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(pdu));
        try {
            int i;
            rr.mParcel.writeInt(dis.readInt());
            rr.mParcel.writeByte((byte) dis.readInt());
            rr.mParcel.writeInt(dis.readInt());
            rr.mParcel.writeInt(dis.read());
            rr.mParcel.writeInt(dis.read());
            rr.mParcel.writeInt(dis.read());
            rr.mParcel.writeInt(dis.read());
            int address_nbr_of_digits = (byte) dis.read();
            rr.mParcel.writeByte((byte) address_nbr_of_digits);
            for (i = 0; i < address_nbr_of_digits; i++) {
                rr.mParcel.writeByte(dis.readByte());
            }
            rr.mParcel.writeInt(dis.read());
            rr.mParcel.writeByte((byte) dis.read());
            int subaddr_nbr_of_digits = (byte) dis.read();
            rr.mParcel.writeByte((byte) subaddr_nbr_of_digits);
            for (i = 0; i < subaddr_nbr_of_digits; i++) {
                rr.mParcel.writeByte(dis.readByte());
            }
            int bearerDataLength = dis.read();
            rr.mParcel.writeInt(bearerDataLength);
            for (i = 0; i < bearerDataLength; i++) {
                rr.mParcel.writeByte(dis.readByte());
            }
        } catch (IOException ex) {
            riljLog("sendSmsCdma: conversion from input stream to object failed: " + ex);
        }
    }

    public void sendCdmaSms(byte[] pdu, Message result) {
        RILRequest rr = RILRequest.obtain(87, result);
        constructCdmaSendSmsRilRequest(rr, pdu);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        this.mMetrics.writeRilSendSms(this.mInstanceId.intValue(), rr.mSerial, 2, 2);
        send(rr);
    }

    public void sendImsGsmSms(String smscPDU, String pdu, int retry, int messageRef, Message result) {
        RILRequest rr = RILRequest.obtain(113, result);
        rr.mParcel.writeInt(1);
        rr.mParcel.writeByte((byte) retry);
        rr.mParcel.writeInt(messageRef);
        constructGsmSendSmsRilRequest(rr, smscPDU, pdu);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        this.mMetrics.writeRilSendSms(this.mInstanceId.intValue(), rr.mSerial, 3, 1);
        send(rr);
    }

    public void sendImsCdmaSms(byte[] pdu, int retry, int messageRef, Message result) {
        RILRequest rr = RILRequest.obtain(113, result);
        rr.mParcel.writeInt(2);
        rr.mParcel.writeByte((byte) retry);
        rr.mParcel.writeInt(messageRef);
        constructCdmaSendSmsRilRequest(rr, pdu);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        this.mMetrics.writeRilSendSms(this.mInstanceId.intValue(), rr.mSerial, 3, 2);
        send(rr);
    }

    public void deleteSmsOnSim(int index, Message response) {
        RILRequest rr = RILRequest.obtain(64, response);
        rr.mParcel.writeInt(1);
        rr.mParcel.writeInt(index);
        send(rr);
    }

    public void deleteSmsOnRuim(int index, Message response) {
        RILRequest rr = RILRequest.obtain(97, response);
        rr.mParcel.writeInt(1);
        rr.mParcel.writeInt(index);
        send(rr);
    }

    public void writeSmsToSim(int status, String smsc, String pdu, Message response) {
        status = translateStatus(status);
        RILRequest rr = RILRequest.obtain(63, response);
        rr.mParcel.writeInt(status);
        rr.mParcel.writeString(pdu);
        rr.mParcel.writeString(smsc);
        send(rr);
    }

    public void writeSmsToRuim(int status, String pdu, Message response) {
        try {
            status = translateStatus(status);
            RILRequest rr = RILRequest.obtain(96, response);
            rr.mParcel.writeInt(status);
            constructCdmaSendSmsRilRequest(rr, IccUtils.hexStringToBytes(pdu));
            send(rr);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private int translateStatus(int status) {
        switch (status & 7) {
            case 1:
                return 1;
            case 3:
                return 0;
            case 5:
                return 3;
            case 7:
                return 2;
            default:
                return 1;
        }
    }

    public void syncApnTableToRds(String[] strings, Message response) {
        RILRequest rr = RILRequest.obtain(2155, response);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        rr.mParcel.writeStringArray(strings);
        send(rr);
    }

    public void setupDataCall(int radioTechnology, int profile, String apn, String user, String password, int authType, String protocol, Message result) {
        setupDataCall(radioTechnology, profile, apn, user, password, authType, protocol, 1, result);
    }

    public void setupDataCall(int radioTechnology, int profile, String apn, String user, String password, int authType, String protocol, int interfaceId, Message result) {
        RILRequest rr = RILRequest.obtain(27, result);
        rr.mParcel.writeInt(8);
        rr.mParcel.writeString(Integer.toString(radioTechnology + 2));
        rr.mParcel.writeString(Integer.toString(profile));
        rr.mParcel.writeString(apn);
        rr.mParcel.writeString(user);
        rr.mParcel.writeString(password);
        rr.mParcel.writeString(Integer.toString(authType));
        rr.mParcel.writeString(protocol);
        rr.mParcel.writeString(Integer.toString(interfaceId));
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " " + radioTechnology + " " + profile + " " + apn + " " + user + " " + password + " " + authType + " " + protocol + " " + interfaceId);
        this.mMetrics.writeRilSetupDataCall(this.mInstanceId.intValue(), rr.mSerial, radioTechnology, profile, apn, authType, protocol);
        send(rr);
    }

    public void deactivateDataCall(int cid, int reason, Message result) {
        RILRequest rr = RILRequest.obtain(41, result);
        rr.mParcel.writeInt(2);
        rr.mParcel.writeString(Integer.toString(cid));
        rr.mParcel.writeString(Integer.toString(reason));
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " " + cid + " " + reason);
        this.mMetrics.writeRilDeactivateDataCall(this.mInstanceId.intValue(), rr.mSerial, cid, reason);
        send(rr);
    }

    public void setRadioPower(boolean on, Message result) {
        int i = 1;
        RILRequest rr = RILRequest.obtain(23, result);
        rr.mParcel.writeInt(1);
        Parcel parcel = rr.mParcel;
        if (!on) {
            i = 0;
        }
        parcel.writeInt(i);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + (on ? " on" : " off"));
        send(rr);
    }

    public void requestShutdown(Message result) {
        RILRequest rr = RILRequest.obtain(129, result);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    public void setModemPower(boolean power, Message result) {
        RILRequest rr;
        riljLog("Set Modem power as: " + power);
        if (power) {
            rr = RILRequest.obtain(2020, result);
        } else {
            rr = RILRequest.obtain(2010, result);
        }
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    public void setSuppServiceNotifications(boolean enable, Message result) {
        int i = 1;
        RILRequest rr = RILRequest.obtain(62, result);
        rr.mParcel.writeInt(1);
        Parcel parcel = rr.mParcel;
        if (!enable) {
            i = 0;
        }
        parcel.writeInt(i);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    public void acknowledgeLastIncomingGsmSms(boolean success, int cause, Message result) {
        RILRequest rr = RILRequest.obtain(37, result);
        rr.mParcel.writeInt(2);
        rr.mParcel.writeInt(success ? 1 : 0);
        rr.mParcel.writeInt(cause);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " " + success + " " + cause);
        send(rr);
    }

    public void acknowledgeLastIncomingCdmaSms(boolean success, int cause, Message result) {
        RILRequest rr = RILRequest.obtain(88, result);
        rr.mParcel.writeInt(success ? 0 : 1);
        rr.mParcel.writeInt(cause);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " " + success + " " + cause);
        send(rr);
    }

    public void acknowledgeIncomingGsmSmsWithPdu(boolean success, String ackPdu, Message result) {
        RILRequest rr = RILRequest.obtain(106, result);
        rr.mParcel.writeInt(2);
        rr.mParcel.writeString(success ? "1" : "0");
        rr.mParcel.writeString(ackPdu);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + ' ' + success + " [" + ackPdu + ']');
        send(rr);
    }

    public void iccIO(int command, int fileid, String path, int p1, int p2, int p3, String data, String pin2, Message result) {
        iccIOForApp(command, fileid, path, p1, p2, p3, data, pin2, null, result);
    }

    public void iccIOForApp(int command, int fileid, String path, int p1, int p2, int p3, String data, String pin2, String aid, Message result) {
        RILRequest rr = RILRequest.obtain(28, result);
        rr.mParcel.writeInt(command);
        rr.mParcel.writeInt(fileid);
        rr.mParcel.writeString(path);
        rr.mParcel.writeInt(p1);
        rr.mParcel.writeInt(p2);
        rr.mParcel.writeInt(p3);
        rr.mParcel.writeString(data);
        rr.mParcel.writeString(pin2);
        rr.mParcel.writeString(aid);
        riljLog(rr.serialString() + "> iccIO: " + requestToString(rr.mRequest) + " 0x" + Integer.toHexString(command) + " 0x" + Integer.toHexString(fileid) + " " + " path: " + path + "," + p1 + "," + p2 + "," + p3 + " aid: " + aid);
        send(rr);
    }

    public void getCLIR(Message result) {
        RILRequest rr = RILRequest.obtain(31, result);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    public void setCLIR(int clirMode, Message result) {
        RILRequest rr = RILRequest.obtain(32, result);
        rr.mParcel.writeInt(1);
        rr.mParcel.writeInt(clirMode);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " " + clirMode);
        send(rr);
    }

    public void queryCallWaiting(int serviceClass, Message response) {
        RILRequest rr = RILRequest.obtain(35, response);
        rr.mParcel.writeInt(1);
        rr.mParcel.writeInt(serviceClass);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " " + serviceClass);
        send(rr);
    }

    public void setCallWaiting(boolean enable, int serviceClass, Message response) {
        RILRequest rr = RILRequest.obtain(36, response);
        rr.mParcel.writeInt(2);
        rr.mParcel.writeInt(enable ? 1 : 0);
        rr.mParcel.writeInt(serviceClass);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " " + enable + ", " + serviceClass);
        send(rr);
    }

    public void getCOLP(Message result) {
        RILRequest rr = RILRequest.obtain(2000, result);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    public void setCOLP(boolean enable, Message result) {
        RILRequest rr = RILRequest.obtain(2001, result);
        rr.mParcel.writeInt(1);
        if (enable) {
            rr.mParcel.writeInt(1);
        } else {
            rr.mParcel.writeInt(0);
        }
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " " + enable);
        send(rr);
    }

    public void getCOLR(Message result) {
        RILRequest rr = RILRequest.obtain(2002, result);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    public void setNetworkSelectionModeAutomatic(Message response) {
        RILRequest rr = RILRequest.obtain(46, response);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    public void setNetworkSelectionModeManual(String operatorNumeric, Message response) {
        RILRequest rr = RILRequest.obtain(47, response);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " " + operatorNumeric);
        rr.mParcel.writeString(operatorNumeric);
        send(rr);
    }

    public void getNetworkSelectionMode(Message response) {
        RILRequest rr = RILRequest.obtain(45, response);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    public void getAvailableNetworks(Message response) {
        RILRequest rr = RILRequest.obtain(2074, response);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    public void cancelAvailableNetworks(Message response) {
        RILRequest rr = RILRequest.obtain(2062, response);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    public void setCallForward(int action, int cfReason, int serviceClass, String number, int timeSeconds, Message response) {
        RILRequest rr = RILRequest.obtain(34, response);
        rr.mParcel.writeInt(action);
        rr.mParcel.writeInt(cfReason);
        rr.mParcel.writeInt(serviceClass);
        rr.mParcel.writeInt(PhoneNumberUtils.toaFromString(number));
        rr.mParcel.writeString(number);
        rr.mParcel.writeInt(timeSeconds);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " " + action + " " + cfReason + " " + serviceClass + timeSeconds);
        send(rr);
    }

    public void queryCallForwardStatus(int cfReason, int serviceClass, String number, Message response) {
        RILRequest rr = RILRequest.obtain(33, response);
        rr.mParcel.writeInt(2);
        rr.mParcel.writeInt(cfReason);
        rr.mParcel.writeInt(serviceClass);
        rr.mParcel.writeInt(PhoneNumberUtils.toaFromString(number));
        rr.mParcel.writeString(number);
        rr.mParcel.writeInt(0);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " " + cfReason + " " + serviceClass);
        send(rr);
    }

    public void queryCLIP(Message response) {
        RILRequest rr = RILRequest.obtain(55, response);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    public void getBasebandVersion(Message response) {
        RILRequest rr = RILRequest.obtain(51, response);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    public void queryFacilityLock(String facility, String password, int serviceClass, Message response) {
        queryFacilityLockForApp(facility, password, serviceClass, null, response);
    }

    public void queryFacilityLockForApp(String facility, String password, int serviceClass, String appId, Message response) {
        RILRequest rr = RILRequest.obtain(42, response);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " [" + facility + " " + serviceClass + " " + appId + "]");
        rr.mParcel.writeInt(4);
        rr.mParcel.writeString(facility);
        rr.mParcel.writeString(password);
        rr.mParcel.writeString(Integer.toString(serviceClass));
        rr.mParcel.writeString(appId);
        send(rr);
    }

    public void setFacilityLock(String facility, boolean lockState, String password, int serviceClass, Message response) {
        setFacilityLockForApp(facility, lockState, password, serviceClass, null, response);
    }

    public void setFacilityLockForApp(String facility, boolean lockState, String password, int serviceClass, String appId, Message response) {
        RILRequest rr = RILRequest.obtain(43, response);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " [" + facility + " " + lockState + " " + serviceClass + " " + appId + "]");
        rr.mParcel.writeInt(5);
        rr.mParcel.writeString(facility);
        rr.mParcel.writeString(lockState ? "1" : "0");
        rr.mParcel.writeString(password);
        rr.mParcel.writeString(Integer.toString(serviceClass));
        rr.mParcel.writeString(appId);
        send(rr);
    }

    public void sendUSSD(String ussdString, Message response) {
        RILRequest rr = RILRequest.obtain(29, response);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " " + "*******");
        rr.mParcel.writeString(ussdString);
        send(rr);
    }

    public void sendCNAPSS(String cnapssString, Message response) {
        RILRequest rr = RILRequest.obtain(2075, response);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " " + cnapssString);
        rr.mParcel.writeString(cnapssString);
        send(rr);
    }

    public void cancelPendingUssd(Message response) {
        RILRequest rr = RILRequest.obtain(30, response);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    public void resetRadio(Message result) {
        RILRequest rr = RILRequest.obtain(58, result);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    public void invokeOemRilRequestRaw(byte[] data, Message response) {
        RILRequest rr = RILRequest.obtain(59, response);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + "[" + IccUtils.bytesToHexString(data) + "]" + ", rsp=" + response);
        rr.mParcel.writeByteArray(data);
        send(rr);
    }

    public void invokeOemRilRequestStrings(String[] strings, Message response) {
        RILRequest rr = RILRequest.obtain(60, response);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + ", rsp=" + response);
        rr.mParcel.writeStringArray(strings);
        send(rr);
    }

    public void setBandMode(int bandMode, Message response) {
        RILRequest rr = RILRequest.obtain(65, response);
        rr.mParcel.writeInt(1);
        rr.mParcel.writeInt(bandMode);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " " + bandMode);
        send(rr);
    }

    public void setBandMode(int[] bandMode, Message response) {
        RILRequest rr = RILRequest.obtain(65, response);
        rr.mParcel.writeInt(3);
        rr.mParcel.writeInt(bandMode[0]);
        rr.mParcel.writeInt(bandMode[1]);
        rr.mParcel.writeInt(bandMode[2]);
        Rlog.d(RILJ_LOG_TAG, "Set band modes: " + bandMode[1] + ", " + bandMode[2]);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " " + bandMode);
        send(rr);
    }

    public void queryAvailableBandMode(Message response) {
        RILRequest rr = RILRequest.obtain(66, response);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    public void sendTerminalResponse(String contents, Message response) {
        RILRequest rr = RILRequest.obtain(70, response);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        rr.mParcel.writeString(contents);
        send(rr);
    }

    public void sendEnvelope(String contents, Message response) {
        RILRequest rr = RILRequest.obtain(69, response);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        rr.mParcel.writeString(contents);
        send(rr);
    }

    public void sendEnvelopeWithStatus(String contents, Message response) {
        RILRequest rr = RILRequest.obtain(Phone.OEM_PRODUCT_17373, response);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + '[' + contents + ']');
        rr.mParcel.writeString(contents);
        send(rr);
    }

    public void handleCallSetupRequestFromSim(boolean accept, int resCode, Message response) {
        int i = 1;
        RILRequest rr = RILRequest.obtain(71, response);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        int[] param = new int[1];
        if (resCode == 33 || resCode == 32) {
            param[0] = resCode;
        } else {
            if (!accept) {
                i = 0;
            }
            param[0] = i;
        }
        rr.mParcel.writeIntArray(param);
        send(rr);
    }

    public void setPreferredNetworkType(int networkType, Message response) {
        RILRequest rr = RILRequest.obtain(73, response);
        rr.mParcel.writeInt(1);
        rr.mParcel.writeInt(networkType);
        this.mPreviousPreferredType = this.mPreferredNetworkType;
        this.mPreferredNetworkType = networkType;
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " : " + networkType);
        this.mMetrics.writeSetPreferredNetworkType(this.mInstanceId.intValue(), networkType);
        send(rr);
    }

    public void getPreferredNetworkType(Message response) {
        RILRequest rr = RILRequest.obtain(74, response);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    public void getNeighboringCids(Message response) {
        RILRequest rr = RILRequest.obtain(75, response);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    public void setLocationUpdates(boolean enable, Message response) {
        int i = 1;
        if (!((PowerManager) this.mContext.getSystemService("power")).isScreenOn() || enable) {
            RILRequest rr = RILRequest.obtain(76, response);
            rr.mParcel.writeInt(1);
            Parcel parcel = rr.mParcel;
            if (!enable) {
                i = 0;
            }
            parcel.writeInt(i);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + ": " + enable);
            send(rr);
        }
    }

    public void getSmscAddress(Message result) {
        RILRequest rr = RILRequest.obtain(100, result);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    public void setSmscAddress(String address, Message result) {
        RILRequest rr = RILRequest.obtain(101, result);
        rr.mParcel.writeString(address);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " : " + address);
        send(rr);
    }

    public void reportSmsMemoryStatus(boolean available, Message result) {
        int i = 1;
        RILRequest rr = RILRequest.obtain(102, result);
        rr.mParcel.writeInt(1);
        Parcel parcel = rr.mParcel;
        if (!available) {
            i = 0;
        }
        parcel.writeInt(i);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + ": " + available);
        send(rr);
    }

    public void reportStkServiceIsRunning(Message result) {
        RILRequest rr = RILRequest.obtain(Phone.OEM_PRODUCT_16391, result);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    public void getGsmBroadcastConfig(Message response) {
        RILRequest rr = RILRequest.obtain(SYS_MTK_URC_CARD_DROP, response);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    public void getGsmBroadcastConfigEx(Message response) {
        RILRequest rr = RILRequest.obtain(2171, response);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    public void setGsmBroadcastConfig(SmsBroadcastConfigInfo[] config, Message response) {
        int i;
        RILRequest rr = RILRequest.obtain(90, response);
        rr.mParcel.writeInt(numOfConfig);
        for (i = 0; i < numOfConfig; i++) {
            int i2;
            rr.mParcel.writeInt(config[i].getFromServiceId());
            rr.mParcel.writeInt(config[i].getToServiceId());
            rr.mParcel.writeInt(config[i].getFromCodeScheme());
            rr.mParcel.writeInt(config[i].getToCodeScheme());
            Parcel parcel = rr.mParcel;
            if (config[i].isSelected()) {
                i2 = 1;
            } else {
                i2 = 0;
            }
            parcel.writeInt(i2);
        }
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " with " + numOfConfig + " configs : ");
        for (SmsBroadcastConfigInfo smsBroadcastConfigInfo : config) {
            riljLog(smsBroadcastConfigInfo.toString());
        }
        send(rr);
    }

    public void setGsmBroadcastConfigEx(SmsBroadcastConfigInfo[] config, Message response) {
        int i;
        RILRequest rr = RILRequest.obtain(2170, response);
        rr.mParcel.writeInt(numOfConfig);
        for (i = 0; i < numOfConfig; i++) {
            int i2;
            rr.mParcel.writeInt(config[i].getFromServiceId());
            rr.mParcel.writeInt(config[i].getToServiceId());
            rr.mParcel.writeInt(config[i].getFromCodeScheme());
            rr.mParcel.writeInt(config[i].getToCodeScheme());
            Parcel parcel = rr.mParcel;
            if (config[i].isSelected()) {
                i2 = 1;
            } else {
                i2 = 0;
            }
            parcel.writeInt(i2);
        }
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " with " + numOfConfig + " configs : ");
        for (SmsBroadcastConfigInfo smsBroadcastConfigInfo : config) {
            riljLog(smsBroadcastConfigInfo.toString());
        }
        send(rr);
    }

    public void setGsmBroadcastActivation(boolean activate, Message response) {
        int i = 1;
        RILRequest rr = RILRequest.obtain(91, response);
        rr.mParcel.writeInt(1);
        Parcel parcel = rr.mParcel;
        if (activate) {
            i = 0;
        }
        parcel.writeInt(i);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    private void updateScreenState() {
        int i;
        boolean z = true;
        int oldState = this.mRadioScreenState;
        riljLog("defaultDisplayState: " + this.mDefaultDisplayState + ", isDevicePlugged: " + this.mIsDevicePlugged);
        if (this.mDefaultDisplayState != 1) {
            i = 1;
        } else {
            i = 0;
        }
        this.mRadioScreenState = i;
        if (this.mRadioScreenState != oldState) {
            if (this.mRadioScreenState != 1) {
                z = false;
            }
            sendScreenState(z);
        }
    }

    public void sendScreenState(boolean on) {
        int i = 1;
        RILRequest rr = RILRequest.obtain(61, null);
        rr.mParcel.writeInt(1);
        Parcel parcel = rr.mParcel;
        if (!on) {
            i = 0;
        }
        parcel.writeInt(i);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + ": " + on);
        send(rr);
    }

    protected void onRadioAvailable() {
        boolean z;
        boolean z2 = true;
        updateScreenState();
        if (this.mRadioScreenState == 1) {
            z = true;
        } else {
            z = false;
        }
        sendScreenState(z);
        RegistrantList registrantList = this.mOemScreenRegistrants;
        if (this.mRadioScreenState != 1) {
            z2 = false;
        }
        registrantList.notifyRegistrants(new AsyncResult(null, Boolean.valueOf(z2), null));
    }

    private RadioState getRadioStateFromInt(int stateInt) {
        switch (stateInt) {
            case 0:
                return RadioState.RADIO_OFF;
            case 1:
                return RadioState.RADIO_UNAVAILABLE;
            case 10:
                return RadioState.RADIO_ON;
            default:
                throw new RuntimeException("Unrecognized RIL_RadioState: " + stateInt);
        }
    }

    private void switchToRadioState(RadioState newState) {
        RadioState oldState = this.mState;
        setRadioState(newState);
        if (newState != oldState) {
            Intent intent = new Intent("android.intent.action.RADIO_STATE_CHANGED");
            intent.putExtra("radio_state", newState);
            intent.putExtra(IPplSmsFilter.KEY_SUB_ID, SubscriptionManager.getSubIdUsingPhoneId(this.mInstanceId.intValue()));
            this.mContext.sendBroadcast(intent);
        }
    }

    private void acquireWakeLock(RILRequest rr, int wakeLockType) {
        synchronized (rr) {
            if (rr.mWakeLockType != -1) {
                Rlog.d(RILJ_LOG_TAG, "Failed to aquire wakelock for " + rr.serialString());
                return;
            }
            Message msg;
            switch (wakeLockType) {
                case 0:
                    synchronized (this.mWakeLock) {
                        if (this.mWakeLockCount == 0) {
                            this.mWakeLock.acquire();
                        }
                        this.mWakeLockCount++;
                        this.mWlSequenceNum++;
                        msg = this.mSender.obtainMessage(2);
                        msg.arg1 = this.mWlSequenceNum;
                        this.mSender.sendMessageDelayed(msg, (long) this.mWakeLockTimeout);
                    }
                case 1:
                    synchronized (this.mAckWakeLock) {
                        if (!this.mAckWakeLock.isHeld()) {
                            this.mAckWakeLock.acquire();
                        }
                        this.mAckWlSequenceNum++;
                        msg = this.mSender.obtainMessage(4);
                        msg.arg1 = this.mAckWlSequenceNum;
                        this.mSender.sendMessageDelayed(msg, (long) this.mAckWakeLockTimeout);
                    }
                default:
                    Rlog.w(RILJ_LOG_TAG, "Acquiring Invalid Wakelock type " + wakeLockType);
                    return;
            }
            rr.mWakeLockType = wakeLockType;
        }
    }

    private void decrementWakeLock(RILRequest rr) {
        synchronized (rr) {
            switch (rr.mWakeLockType) {
                case -1:
                case 1:
                    break;
                case 0:
                    synchronized (this.mWakeLock) {
                        if (this.mWakeLockCount > 1) {
                            this.mWakeLockCount--;
                        } else {
                            this.mWakeLockCount = 0;
                            this.mWakeLock.release();
                        }
                    }
                default:
                    Rlog.w(RILJ_LOG_TAG, "Decrementing Invalid Wakelock type " + rr.mWakeLockType);
                    break;
            }
            rr.mWakeLockType = -1;
        }
    }

    private boolean clearWakeLock(int wakeLockType) {
        if (wakeLockType == 0) {
            synchronized (this.mWakeLock) {
                if (this.mWakeLockCount != 0 || this.mWakeLock.isHeld()) {
                    Rlog.d(RILJ_LOG_TAG, "NOTE: mWakeLockCount is " + this.mWakeLockCount + "at time of clearing");
                    this.mWakeLockCount = 0;
                    this.mWakeLock.release();
                    return true;
                }
                return false;
            }
        }
        synchronized (this.mAckWakeLock) {
            if (this.mAckWakeLock.isHeld()) {
                this.mAckWakeLock.release();
                return true;
            }
            return false;
        }
    }

    private void send(RILRequest rr) {
        if (this.mSocket == null) {
            rr.onError(1, null);
            rr.release();
            return;
        }
        Message msg = this.mSender.obtainMessage(1, rr);
        acquireWakeLock(rr, 0);
        msg.sendToTarget();
    }

    private void processResponse(Parcel p) {
        int type = p.readInt();
        RILRequest rr;
        if (type == 1 || type == 4) {
            processUnsolicited(p, type);
        } else if (type == 0 || type == 3) {
            rr = processSolicited(p, type);
            if (rr != null) {
                if (type == 0) {
                    decrementWakeLock(rr);
                }
                rr.release();
            }
        } else if (type == 2) {
            int serial = p.readInt();
            synchronized (this.mRequestList) {
                rr = (RILRequest) this.mRequestList.get(serial);
            }
            if (rr == null) {
                Rlog.w(RILJ_LOG_TAG, "Unexpected solicited ack response! sn: " + serial);
            } else {
                decrementWakeLock(rr);
                riljLog(rr.serialString() + " Ack < " + requestToString(rr.mRequest));
            }
        }
    }

    private void clearRequestList(int error, boolean loggable) {
        synchronized (this.mRequestList) {
            int count = this.mRequestList.size();
            if (loggable) {
                Rlog.d(RILJ_LOG_TAG, "clearRequestList  mWakeLockCount=" + this.mWakeLockCount + " mRequestList=" + count);
            }
            for (int i = 0; i < count; i++) {
                RILRequest rr = (RILRequest) this.mRequestList.valueAt(i);
                if (loggable) {
                    Rlog.d(RILJ_LOG_TAG, i + ": [" + rr.mSerial + "] " + requestToString(rr.mRequest));
                }
                rr.onError(error, null);
                decrementWakeLock(rr);
                rr.release();
            }
            this.mRequestList.clear();
        }
    }

    private RILRequest findAndRemoveRequestFromList(int serial) {
        RILRequest rr;
        synchronized (this.mRequestList) {
            rr = (RILRequest) this.mRequestList.get(serial);
            if (rr != null) {
                this.mRequestList.remove(serial);
            }
        }
        return rr;
    }

    private void addToRilHistogram(RILRequest rr) {
        int totalTime = (int) (SystemClock.elapsedRealtime() - rr.mStartTimeMs);
        synchronized (mRilTimeHistograms) {
            TelephonyHistogram entry = (TelephonyHistogram) mRilTimeHistograms.get(rr.mRequest);
            if (entry == null) {
                entry = new TelephonyHistogram(1, rr.mRequest, 5);
                mRilTimeHistograms.put(rr.mRequest, entry);
            }
            entry.addTimeTaken(totalTime);
        }
    }

    private RILRequest processSolicited(Parcel p, int type) {
        int serial = p.readInt();
        int error = p.readInt();
        RILRequest rr = findAndRemoveRequestFromList(serial);
        if (rr == null) {
            Rlog.w(RILJ_LOG_TAG, "Unexpected solicited response! sn: " + serial + " error: " + error);
            return null;
        }
        addToRilHistogram(rr);
        if (getRilVersion() >= 13 && type == 3) {
            Message msg = this.mSender.obtainMessage(3, RILRequest.obtain(800, null));
            acquireWakeLock(rr, 1);
            msg.sendToTarget();
            riljLog("Response received for " + rr.serialString() + " " + requestToString(rr.mRequest) + " Sending ack to ril.cpp");
        }
        if (rr.mRequest == 49 || rr.mRequest == 50) {
            synchronized (this.mDtmfReqQueue) {
                this.mDtmfReqQueue.remove(rr);
                riljLog("remove first item in dtmf queue done, size = " + this.mDtmfReqQueue.size());
                if (this.mDtmfReqQueue.size() > 0) {
                    RILRequest rr2 = this.mDtmfReqQueue.get();
                    riljLog(rr2.serialString() + "> " + requestToString(rr2.mRequest));
                    send(rr2);
                } else if (this.mDtmfReqQueue.getPendingRequest() != null) {
                    riljLog("send pending switch request");
                    send(this.mDtmfReqQueue.getPendingRequest());
                    this.mDtmfReqQueue.setSendChldRequest();
                    this.mDtmfReqQueue.setPendingRequest(null);
                }
            }
        }
        Object ret = null;
        if (rr.mRequest == 48 || rr.mRequest == 2074) {
            this.mGetAvailableNetworkDoneRegistrant.notifyRegistrants();
        }
        if (rr.mRequest == 73) {
            if (!(error == 0 || this.mPreviousPreferredType == -1)) {
                riljLog("restore mPreferredNetworkType from " + this.mPreferredNetworkType + " to " + this.mPreviousPreferredType);
                this.mPreferredNetworkType = this.mPreviousPreferredType;
            }
            this.mPreviousPreferredType = -1;
        }
        if (rr.mRequest == 15 || rr.mRequest == 16 || rr.mRequest == 52 || rr.mRequest == 72) {
            riljLog("clear mIsSendChldRequest");
            this.mDtmfReqQueue.resetSendChldRequest();
        }
        if (error == 0 || p.dataAvail() > 0) {
            try {
                switch (rr.mRequest) {
                    case 1:
                        ret = responseIccCardStatus(p);
                        break;
                    case 2:
                        ret = responseInts(p);
                        break;
                    case 3:
                        ret = responseInts(p);
                        break;
                    case 4:
                        ret = responseInts(p);
                        break;
                    case 5:
                        ret = responseInts(p);
                        break;
                    case 6:
                        ret = responseInts(p);
                        break;
                    case 7:
                        ret = responseInts(p);
                        break;
                    case 8:
                        ret = responseInts(p);
                        break;
                    case 9:
                        ret = responseCallList(p);
                        break;
                    case 10:
                        ret = responseVoid(p);
                        break;
                    case 11:
                        ret = responseString(p);
                        break;
                    case 12:
                        ret = responseVoid(p);
                        break;
                    case 13:
                        ret = responseVoid(p);
                        break;
                    case 14:
                        if (this.mTestingEmergencyCall.getAndSet(false) && this.mEmergencyCallbackModeRegistrant != null) {
                            riljLog("testing emergency call, notify ECM Registrants");
                            this.mEmergencyCallbackModeRegistrant.notifyRegistrant();
                        }
                        ret = responseVoid(p);
                        break;
                    case 15:
                        ret = responseVoid(p);
                        break;
                    case 16:
                        ret = responseVoid(p);
                        break;
                    case 17:
                        ret = responseVoid(p);
                        break;
                    case 18:
                        ret = responseFailCause(p);
                        break;
                    case 19:
                        ret = responseSignalStrength(p);
                        break;
                    case 20:
                        ret = responseStrings(p);
                        break;
                    case 21:
                        ret = responseStrings(p);
                        break;
                    case 22:
                        ret = responseStrings(p);
                        break;
                    case 23:
                        ret = responseVoid(p);
                        break;
                    case 24:
                        ret = responseVoid(p);
                        break;
                    case 25:
                        ret = responseSMS(p);
                        break;
                    case 26:
                        ret = responseSMS(p);
                        break;
                    case 27:
                        ret = responseSetupDataCall(p);
                        oppoResetSwitchDssState(ret, error);
                        break;
                    case 28:
                        ret = responseICC_IO(p);
                        break;
                    case 29:
                        ret = responseVoid(p);
                        break;
                    case 30:
                        ret = responseVoid(p);
                        break;
                    case 31:
                        ret = responseInts(p);
                        break;
                    case 32:
                        ret = responseVoid(p);
                        break;
                    case 33:
                        ret = responseCallForward(p);
                        break;
                    case 34:
                        ret = responseVoid(p);
                        break;
                    case 35:
                        ret = responseInts(p);
                        break;
                    case 36:
                        ret = responseVoid(p);
                        break;
                    case 37:
                        ret = responseVoid(p);
                        break;
                    case 38:
                        ret = responseString(p);
                        break;
                    case 39:
                        ret = responseString(p);
                        break;
                    case 40:
                        ret = responseVoid(p);
                        break;
                    case 41:
                        ret = responseVoid(p);
                        break;
                    case 42:
                        ret = responseInts(p);
                        break;
                    case 43:
                        ret = responseInts(p);
                        break;
                    case 44:
                        ret = responseVoid(p);
                        break;
                    case RilDataCallFailCause.PDP_FAIL_FILTER_SYTAX_ERROR /*45*/:
                        ret = responseInts(p);
                        break;
                    case 46:
                        ret = responseVoid(p);
                        break;
                    case 47:
                        ret = responseVoid(p);
                        break;
                    case 48:
                        ret = responseOperatorInfos(p);
                        break;
                    case 49:
                        ret = responseVoid(p);
                        break;
                    case 50:
                        ret = responseVoid(p);
                        break;
                    case 51:
                        ret = responseString(p);
                        break;
                    case 52:
                        ret = responseVoid(p);
                        break;
                    case 53:
                        ret = responseVoid(p);
                        break;
                    case 54:
                        ret = responseInts(p);
                        break;
                    case 55:
                        ret = responseInts(p);
                        break;
                    case 56:
                        ret = responseInts(p);
                        break;
                    case 57:
                        ret = responseDataCallList(p);
                        break;
                    case 58:
                        ret = responseVoid(p);
                        break;
                    case RadioNVItems.RIL_NV_CDMA_EHRPD_FORCED /*59*/:
                        ret = responseRaw(p);
                        break;
                    case 60:
                        ret = responseStrings(p);
                        break;
                    case 61:
                        ret = responseVoid(p);
                        break;
                    case 62:
                        ret = responseVoid(p);
                        break;
                    case 63:
                        ret = responseInts(p);
                        break;
                    case 64:
                        ret = responseVoid(p);
                        break;
                    case 65:
                        ret = responseVoid(p);
                        break;
                    case 66:
                        ret = responseInts(p);
                        break;
                    case SYS_OEM_NW_DIAG_CAUSE_MCFG_ICCID_FAILED /*67*/:
                        ret = responseString(p);
                        break;
                    case 68:
                        ret = responseVoid(p);
                        break;
                    case CallFailCause.FACILITY_NOT_IMPLEMENT /*69*/:
                        ret = responseString(p);
                        break;
                    case 70:
                        ret = responseVoid(p);
                        break;
                    case 71:
                        ret = responseInts(p);
                        break;
                    case 72:
                        ret = responseVoid(p);
                        break;
                    case 73:
                        ret = responseSetPreferredNetworkType(p);
                        break;
                    case RadioNVItems.RIL_NV_LTE_SCAN_PRIORITY_25 /*74*/:
                        ret = responseGetPreferredNetworkType(p);
                        break;
                    case RadioNVItems.RIL_NV_LTE_SCAN_PRIORITY_26 /*75*/:
                        ret = responseCellList(p);
                        break;
                    case RadioNVItems.RIL_NV_LTE_SCAN_PRIORITY_41 /*76*/:
                        ret = responseVoid(p);
                        break;
                    case RadioNVItems.RIL_NV_LTE_HIDDEN_BAND_PRIORITY_25 /*77*/:
                        ret = responseVoid(p);
                        break;
                    case RadioNVItems.RIL_NV_LTE_HIDDEN_BAND_PRIORITY_26 /*78*/:
                        ret = responseVoid(p);
                        break;
                    case 79:
                        ret = responseInts(p);
                        break;
                    case RadioNVItems.RIL_NV_LTE_NEXT_SCAN /*80*/:
                        ret = responseVoid(p);
                        break;
                    case 81:
                        ret = responseInts(p);
                        break;
                    case RadioNVItems.RIL_NV_LTE_BSR_MAX_TIME /*82*/:
                        ret = responseVoid(p);
                        break;
                    case 83:
                        ret = responseInts(p);
                        break;
                    case 84:
                        ret = responseVoid(p);
                        break;
                    case 85:
                        ret = responseVoid(p);
                        break;
                    case 86:
                        ret = responseVoid(p);
                        break;
                    case 87:
                        ret = responseSMS(p);
                        break;
                    case CallFailCause.INCOMPATIBLE_DESTINATION /*88*/:
                        ret = responseVoid(p);
                        break;
                    case SYS_MTK_URC_CARD_DROP /*89*/:
                        ret = responseGmsBroadcastConfig(p);
                        break;
                    case 90:
                        ret = responseVoid(p);
                        break;
                    case CallFailCause.INVALID_TRANSIT_NETWORK_SELECTION /*91*/:
                        ret = responseVoid(p);
                        break;
                    case 92:
                        ret = responseCdmaBroadcastConfig(p);
                        break;
                    case 93:
                        ret = responseVoid(p);
                        break;
                    case 94:
                        ret = responseVoid(p);
                        break;
                    case 95:
                        ret = responseStrings(p);
                        break;
                    case 96:
                        ret = responseInts(p);
                        break;
                    case 97:
                        ret = responseVoid(p);
                        break;
                    case 98:
                        ret = responseStrings(p);
                        break;
                    case 99:
                        ret = responseVoid(p);
                        break;
                    case 100:
                        ret = responseString(p);
                        break;
                    case 101:
                        ret = responseVoid(p);
                        break;
                    case 102:
                        ret = responseVoid(p);
                        break;
                    case Phone.OEM_PRODUCT_16391 /*103*/:
                        ret = responseVoid(p);
                        break;
                    case 104:
                        ret = responseInts(p);
                        break;
                    case 105:
                        if (!SystemProperties.get("ro.mtk_tc1_feature").equals("1")) {
                            ret = responseString(p);
                            break;
                        }
                        ret = responseStringEncodeBase64(p);
                        break;
                    case 106:
                        ret = responseVoid(p);
                        break;
                    case Phone.OEM_PRODUCT_17373 /*107*/:
                        ret = responseICC_IO(p);
                        break;
                    case 108:
                        ret = responseInts(p);
                        break;
                    case 109:
                        ret = responseCellInfoList(p);
                        break;
                    case 110:
                        ret = responseVoid(p);
                        break;
                    case 111:
                        ret = responseVoid(p);
                        break;
                    case 112:
                        ret = responseInts(p);
                        break;
                    case 113:
                        ret = responseSMS(p);
                        break;
                    case 114:
                        ret = responseICC_IO(p);
                        break;
                    case RilDataCallFailCause.PDP_FAIL_EMM_ACCESS_BARRED /*115*/:
                        ret = responseInts(p);
                        break;
                    case RilDataCallFailCause.PDP_FAIL_EMERGENCY_IFACE_ONLY /*116*/:
                        ret = responseVoid(p);
                        break;
                    case RilDataCallFailCause.PDP_FAIL_IFACE_MISMATCH /*117*/:
                        ret = responseICC_IO(p);
                        break;
                    case RilDataCallFailCause.PDP_FAIL_COMPANION_IFACE_IN_USE /*118*/:
                        ret = responseString(p);
                        break;
                    case RilDataCallFailCause.PDP_FAIL_IP_ADDRESS_MISMATCH /*119*/:
                        ret = responseVoid(p);
                        break;
                    case RilDataCallFailCause.PDP_FAIL_IFACE_AND_POL_FAMILY_MISMATCH /*120*/:
                        ret = responseVoid(p);
                        break;
                    case RilDataCallFailCause.PDP_FAIL_EMM_ACCESS_BARRED_INFINITE_RETRY /*121*/:
                        ret = responseVoid(p);
                        break;
                    case RilDataCallFailCause.PDP_FAIL_AUTH_FAILURE_ON_EMERGENCY_CALL /*122*/:
                        ret = responseVoid(p);
                        break;
                    case 123:
                        ret = responseVoid(p);
                        break;
                    case 124:
                        ret = responseHardwareConfig(p);
                        break;
                    case 125:
                        ret = responseICC_IOBase64(p);
                        break;
                    case 128:
                        ret = responseVoid(p);
                        break;
                    case 129:
                        ret = responseVoid(p);
                        break;
                    case 130:
                        ret = responseRadioCapability(p);
                        break;
                    case 131:
                        ret = responseRadioCapability(p);
                        break;
                    case 132:
                        ret = responseLceStatus(p);
                        break;
                    case 133:
                        ret = responseLceStatus(p);
                        break;
                    case 134:
                        ret = responseLceData(p);
                        break;
                    case 135:
                        ret = responseActivityData(p);
                        break;
                    case 136:
                        ret = responseInts(p);
                        break;
                    case 137:
                        ret = responseCarrierIdentifiers(p);
                        break;
                    case 2000:
                        ret = responseInts(p);
                        break;
                    case 2001:
                        ret = responseVoid(p);
                        break;
                    case 2002:
                        ret = responseInts(p);
                        break;
                    case 2010:
                        ret = responseVoid(p);
                        break;
                    case 2012:
                        ret = responseInts(p);
                        break;
                    case 2013:
                        ret = responseVoid(p);
                        break;
                    case 2014:
                        ret = responsePhbEntries(p);
                        break;
                    case 2016:
                        ret = responseInts(p);
                        break;
                    case 2017:
                        ret = responseInts(p);
                        break;
                    case 2018:
                        ret = responseVoid(p);
                        break;
                    case 2020:
                        ret = responseVoid(p);
                        break;
                    case 2021:
                        ret = responseSimSmsMemoryStatus(p);
                        break;
                    case 2022:
                        ret = responseInts(p);
                        break;
                    case 2023:
                        ret = responseNetworkInfoWithActs(p);
                        break;
                    case 2024:
                        ret = responseVoid(p);
                        break;
                    case CharacterSets.GB_2312 /*2025*/:
                        ret = responseInts(p);
                        break;
                    case CharacterSets.BIG5 /*2026*/:
                        ret = responseVoid(p);
                        break;
                    case CharacterSets.MACINTOSH /*2027*/:
                        ret = responseVoid(p);
                        break;
                    case 2028:
                        ret = responseStrings(p);
                        break;
                    case 2029:
                        ret = responseInts(p);
                        break;
                    case 2030:
                        ret = responseVoid(p);
                        break;
                    case 2031:
                        ret = responseInts(p);
                        break;
                    case 2033:
                        ret = responseInts(p);
                        break;
                    case 2034:
                        ret = responseGetPhbMemStorage(p);
                        break;
                    case 2035:
                        responseVoid(p);
                        break;
                    case 2036:
                        ret = responseReadPhbEntryExt(p);
                        break;
                    case 2037:
                        ret = responseVoid(p);
                        break;
                    case 2038:
                        ret = responseSmsParams(p);
                        break;
                    case 2039:
                        ret = responseVoid(p);
                        break;
                    case 2042:
                        ret = responseString(p);
                        break;
                    case 2043:
                        ret = responseVoid(p);
                        break;
                    case 2044:
                        ret = responseVoid(p);
                        break;
                    case 2045:
                        ret = responseCbConfig(p);
                        break;
                    case 2047:
                        ret = responseVoid(p);
                        break;
                    case 2048:
                        ret = responseVoid(p);
                        break;
                    case 2050:
                        ret = responseVoid(p);
                        break;
                    case CharacterSets.CP864 /*2051*/:
                        ret = responseVoid(p);
                        break;
                    case 2058:
                        ret = responseVoid(p);
                        break;
                    case 2059:
                        ret = responseFemtoCellInfos(p);
                        break;
                    case 2060:
                        ret = responseVoid(p);
                        break;
                    case 2061:
                        ret = responseVoid(p);
                        break;
                    case 2062:
                        ret = responseVoid(p);
                        break;
                    case 2063:
                        ret = responseVoid(p);
                        break;
                    case 2065:
                        ret = responseVoid(p);
                        break;
                    case 2066:
                        ret = responseVoid(p);
                        break;
                    case 2067:
                        ret = responseVoid(p);
                        break;
                    case 2068:
                        ret = responseVoid(p);
                        break;
                    case 2069:
                        ret = responseICC_IO(p);
                        break;
                    case 2070:
                        ret = responseInts(p);
                        break;
                    case 2071:
                        ret = responseIccCardStatus(p);
                        break;
                    case 2072:
                        ret = responseICC_IO(p);
                        break;
                    case 2073:
                        ret = responseVoid(p);
                        break;
                    case 2074:
                        ret = responseOperatorInfosWithAct(p);
                        break;
                    case 2076:
                        ret = responseVoid(p);
                        break;
                    case 2083:
                        ret = responseVoid(p);
                        break;
                    case CharacterSets.KOI8_R /*2084*/:
                        ret = responseVoid(p);
                        break;
                    case CharacterSets.HZ_GB_2312 /*2085*/:
                        responseString(p);
                        break;
                    case 2086:
                        responseString(p);
                        break;
                    case 2087:
                        ret = responseVoid(p);
                        break;
                    case CharacterSets.KOI8_U /*2088*/:
                        ret = responseVoid(p);
                        break;
                    case 2089:
                        ret = responseVoid(p);
                        break;
                    case 2090:
                        ret = responseVoid(p);
                        break;
                    case 2091:
                        ret = responseVoid(p);
                        break;
                    case 2092:
                        ret = responseVoid(p);
                        break;
                    case 2093:
                        ret = responseVoid(p);
                        break;
                    case 2094:
                        ret = responseVoid(p);
                        break;
                    case 2095:
                        ret = responseVoid(p);
                        break;
                    case 2100:
                        ret = responseVoid(p);
                        break;
                    case CharacterSets.BIG5_HKSCS /*2101*/:
                        ret = responseVoid(p);
                        break;
                    case 2102:
                        ret = responseVoid(p);
                        break;
                    case 2103:
                        ret = responseVoid(p);
                        break;
                    case 2104:
                        ret = responseVoid(p);
                        break;
                    case 2108:
                        ret = responseVoid(p);
                        break;
                    case 2110:
                        ret = responseVoid(p);
                        break;
                    case 2111:
                        ret = responseVoid(p);
                        break;
                    case 2131:
                        ret = responseVoid(p);
                        break;
                    case 2134:
                        ret = responseVoid(p);
                        break;
                    case 2142:
                        ret = responseVoid(p);
                        break;
                    case 2143:
                        ret = responseInts(p);
                        break;
                    case 2144:
                        ret = responseString(p);
                        break;
                    case 2145:
                        ret = responseString(p);
                        break;
                    case 2146:
                        ret = responsePhbEntries(p);
                        break;
                    case 2147:
                        ret = responseStrings(p);
                        break;
                    case 2151:
                        ret = responseVoid(p);
                        break;
                    case 2152:
                        ret = responseVoid(p);
                        break;
                    case 2153:
                        ret = responseVoid(p);
                        break;
                    case 2154:
                        ret = responseInts(p);
                        break;
                    case 2155:
                        ret = responseVoid(p);
                        break;
                    case 2157:
                        ret = responseInts(p);
                        break;
                    case 2158:
                        ret = responseVoid(p);
                        break;
                    case 2159:
                        ret = responseAntennaConf(p);
                        break;
                    case 2160:
                        ret = responseAntennaInfo(p);
                        break;
                    case 2161:
                        ret = responseVoid(p);
                        break;
                    case 2162:
                        ret = responseSimSmsMemoryStatus(p);
                        break;
                    case 2163:
                        ret = responseVoid(p);
                        break;
                    case 2164:
                        ret = responseString(p);
                        break;
                    case 2170:
                        ret = responseVoid(p);
                        break;
                    case 2171:
                        ret = responseGmsBroadcastConfig(p);
                        break;
                    case 2173:
                        ret = responseVoid(p);
                        break;
                    case 2174:
                        ret = responseInts(p);
                        break;
                    case 2183:
                        ret = responseVoid(p);
                        break;
                    case 2184:
                        ret = responseVoid(p);
                        break;
                    case 2187:
                        ret = responseInts(p);
                        break;
                    case 2189:
                        ret = responseCallForwardEx(p);
                        break;
                    case 2190:
                        ret = responseVoid(p);
                        break;
                    default:
                        throw new RuntimeException("Unrecognized solicited response: " + rr.mRequest);
                }
            } catch (Throwable tr) {
                Rlog.w(RILJ_LOG_TAG, rr.serialString() + "< " + requestToString(rr.mRequest) + " exception, possible invalid RIL response", tr);
                if (rr.mResult != null) {
                    AsyncResult.forMessage(rr.mResult, null, tr);
                    rr.mResult.sendToTarget();
                }
                return rr;
            }
        }
        if (rr.mRequest == 129) {
            riljLog("Response to RIL_REQUEST_SHUTDOWN received. Error is " + error + " Setting Radio State to Unavailable regardless of error.");
            setRadioState(RadioState.RADIO_UNAVAILABLE);
        }
        switch (rr.mRequest) {
            case 3:
            case 5:
                if (this.mIccStatusChangedRegistrants != null) {
                    riljLog("ON enter sim puk fakeSimStatusChanged: reg count=" + this.mIccStatusChangedRegistrants.size());
                    this.mIccStatusChangedRegistrants.notifyRegistrants();
                    break;
                }
                break;
        }
        if (error != 0) {
            switch (rr.mRequest) {
                case 2:
                case 4:
                case 6:
                case 7:
                case 43:
                    if (this.mIccStatusChangedRegistrants != null) {
                        riljLog("ON some errors fakeSimStatusChanged: reg count=" + this.mIccStatusChangedRegistrants.size());
                        this.mIccStatusChangedRegistrants.notifyRegistrants();
                        break;
                    }
                    break;
                case 130:
                    if (6 == error || 2 == error) {
                        ret = makeStaticRadioCapability();
                        error = 0;
                        break;
                    }
                case 135:
                    ret = new ModemActivityInfo(0, 0, 0, new int[5], 0, 0);
                    error = 0;
                    break;
            }
            switch (rr.mRequest) {
                case 27:
                    Intent intent = new Intent("com.mediatek.log2server.EXCEPTION_HAPPEND");
                    intent.putExtra("Reason", "SmartLogging");
                    intent.putExtra("from_where", "RIL");
                    this.mContext.sendBroadcast(intent);
                    riljLog("Broadcast for SmartLogging -DATA failure - may recover");
                    break;
            }
            if (error != 0) {
                rr.onError(error, ret);
            }
        }
        if (error == 0) {
            riljLog(rr.serialString() + "< " + requestToString(rr.mRequest) + " " + retToString(rr.mRequest, ret) + ",rsp=" + rr.mResult);
            if (rr.mResult != null) {
                AsyncResult.forMessage(rr.mResult, ret, null);
                rr.mResult.sendToTarget();
            }
        }
        this.mMetrics.writeOnRilSolicitedResponse(this.mInstanceId.intValue(), rr.mSerial, error, rr.mRequest, ret);
        return rr;
    }

    private RadioCapability makeStaticRadioCapability() {
        int raf = 1;
        String rafString = this.mContext.getResources().getString(17039467);
        if (!TextUtils.isEmpty(rafString)) {
            raf = RadioAccessFamily.rafTypeFromString(rafString);
        }
        RadioCapability rc = new RadioCapability(this.mInstanceId.intValue(), 0, 0, raf, UsimPBMemInfo.STRING_NOT_SET, 1);
        riljLog("Faking RIL_REQUEST_GET_RADIO_CAPABILITY response using " + raf);
        return rc;
    }

    static String retToString(int req, Object ret) {
        if (ret == null) {
            return UsimPBMemInfo.STRING_NOT_SET;
        }
        switch (req) {
            case 11:
            case 38:
            case 39:
            case RilDataCallFailCause.PDP_FAIL_EMM_ACCESS_BARRED /*115*/:
            case RilDataCallFailCause.PDP_FAIL_IFACE_MISMATCH /*117*/:
                return UsimPBMemInfo.STRING_NOT_SET;
            default:
                String s;
                int length;
                StringBuilder stringBuilder;
                int i;
                int i2;
                if (ret instanceof int[]) {
                    int[] intArray = (int[]) ret;
                    length = intArray.length;
                    stringBuilder = new StringBuilder("{");
                    if (length > 0) {
                        stringBuilder.append(intArray[0]);
                        i = 1;
                        while (i < length) {
                            i2 = i + 1;
                            stringBuilder.append(", ").append(intArray[i]);
                            i = i2;
                        }
                    }
                    stringBuilder.append("}");
                    s = stringBuilder.toString();
                } else if (ret instanceof String[]) {
                    String[] strings = (String[]) ret;
                    length = strings.length;
                    stringBuilder = new StringBuilder("{");
                    if (length > 0) {
                        stringBuilder.append(strings[0]);
                        i = 1;
                        while (i < length) {
                            i2 = i + 1;
                            stringBuilder.append(", ").append(strings[i]);
                            i = i2;
                        }
                    }
                    stringBuilder.append("}");
                    s = stringBuilder.toString();
                } else if (req == 9) {
                    ArrayList<DriverCall> calls = (ArrayList) ret;
                    stringBuilder = new StringBuilder("{");
                    for (DriverCall dc : calls) {
                        stringBuilder.append("[").append(dc).append("] ");
                    }
                    stringBuilder.append("}");
                    s = stringBuilder.toString();
                } else if (req == 75) {
                    ArrayList<NeighboringCellInfo> cells = (ArrayList) ret;
                    stringBuilder = new StringBuilder("{");
                    for (NeighboringCellInfo cell : cells) {
                        stringBuilder.append("[").append(cell).append("] ");
                    }
                    stringBuilder.append("}");
                    s = stringBuilder.toString();
                } else if (req == 33) {
                    stringBuilder = new StringBuilder("{");
                    for (Object append : (CallForwardInfo[]) ret) {
                        stringBuilder.append("[").append(append).append("] ");
                    }
                    stringBuilder.append("}");
                    s = stringBuilder.toString();
                } else if (req == 124) {
                    ArrayList<HardwareConfig> hwcfgs = (ArrayList) ret;
                    stringBuilder = new StringBuilder(" ");
                    for (HardwareConfig hwcfg : hwcfgs) {
                        stringBuilder.append("[").append(hwcfg).append("] ");
                    }
                    s = stringBuilder.toString();
                } else {
                    s = ret.toString();
                }
                return s;
        }
    }

    private void processUnsolicited(Parcel p, int type) {
        Object ret;
        int response = p.readInt();
        if (getRilVersion() >= 13 && type == 4) {
            RILRequest rr = RILRequest.obtain(800, null);
            Message msg = this.mSender.obtainMessage(3, rr);
            acquireWakeLock(rr, 1);
            msg.sendToTarget();
            riljLog("Unsol response received for " + responseToString(response) + " Sending ack to ril.cpp");
        }
        switch (response) {
            case 1000:
                ret = responseVoid(p);
                break;
            case 1001:
                ret = responseVoid(p);
                break;
            case 1002:
                ret = responseStrings(p);
                break;
            case 1003:
                ret = responseString(p);
                break;
            case 1004:
                ret = responseString(p);
                break;
            case 1005:
                ret = responseInts(p);
                break;
            case 1006:
                ret = responseStrings(p);
                break;
            case 1008:
                ret = responseString(p);
                break;
            case 1009:
                ret = responseSignalStrength(p);
                break;
            case GsmVTProvider.SESSION_EVENT_START_COUNTER /*1010*/:
                ret = responseDataCallList(p);
                break;
            case 1011:
                ret = responseSuppServiceNotification(p);
                break;
            case 1012:
                ret = responseVoid(p);
                break;
            case CharacterSets.UTF_16BE /*1013*/:
                ret = responseString(p);
                break;
            case CharacterSets.UTF_16LE /*1014*/:
                ret = responseString(p);
                break;
            case CharacterSets.UTF_16 /*1015*/:
                ret = responseInts(p);
                break;
            case CharacterSets.CESU_8 /*1016*/:
                ret = responseVoid(p);
                break;
            case CharacterSets.UTF_32 /*1017*/:
                ret = responseSimRefresh(p);
                break;
            case CharacterSets.UTF_32BE /*1018*/:
                ret = responseCallRing(p);
                break;
            case CharacterSets.UTF_32LE /*1019*/:
                ret = responseVoid(p);
                break;
            case CharacterSets.BOCU_1 /*1020*/:
                ret = responseCdmaSms(p);
                break;
            case 1021:
                ret = responseRaw(p);
                break;
            case 1022:
                ret = responseVoid(p);
                break;
            case 1023:
                ret = responseInts(p);
                break;
            case 1024:
                ret = responseVoid(p);
                break;
            case 1025:
                ret = responseCdmaCallWaiting(p);
                break;
            case 1026:
                ret = responseInts(p);
                break;
            case 1027:
                ret = responseCdmaInformationRecord(p);
                break;
            case 1028:
                ret = responseRaw(p);
                break;
            case 1029:
                ret = responseInts(p);
                break;
            case 1030:
                ret = responseVoid(p);
                break;
            case 1031:
                ret = responseInts(p);
                break;
            case 1032:
                ret = responseInts(p);
                break;
            case 1033:
                ret = responseVoid(p);
                break;
            case 1034:
                ret = responseInts(p);
                break;
            case 1035:
                ret = responseInts(p);
                break;
            case 1036:
                ret = responseCellInfoList(p);
                break;
            case 1037:
                ret = responseVoid(p);
                break;
            case 1038:
                ret = responseInts(p);
                break;
            case 1039:
                ret = responseInts(p);
                break;
            case 1040:
                ret = responseHardwareConfig(p);
                break;
            case 1042:
                ret = responseRadioCapability(p);
                break;
            case 1043:
                ret = responseSsData(p);
                break;
            case 1044:
                ret = responseStrings(p);
                break;
            case 1045:
                ret = responseLceData(p);
                break;
            case 1046:
                ret = responsePcoData(p);
                break;
            case 3000:
                ret = responseStrings(p);
                break;
            case 3001:
                ret = responseStrings(p);
                break;
            case 3002:
                ret = responseInts(p);
                break;
            case 3004:
                ret = responseVoid(p);
                break;
            case 3005:
                ret = responseVoid(p);
                break;
            case 3006:
                ret = responseInts(p);
                break;
            case 3008:
                ret = responseInts(p);
                break;
            case 3009:
                ret = responseInts(p);
                break;
            case 3010:
                ret = responseInts(p);
                break;
            case 3011:
                ret = responseStrings(p);
                break;
            case 3012:
                ret = responseInts(p);
                break;
            case 3013:
                ret = responseInts(p);
                break;
            case 3015:
                ret = responseVoid(p);
                break;
            case 3016:
                ret = responseInts(p);
                break;
            case 3017:
                ret = responseVoid(p);
                break;
            case 3018:
                ret = responseVoid(p);
                break;
            case 3019:
                ret = responseEtwsNotification(p);
                break;
            case 3020:
                ret = responseStrings(p);
                break;
            case 3021:
                ret = responseInts(p);
                break;
            case 3022:
                ret = responseInts(p);
                break;
            case 3023:
                ret = responseStrings(p);
                break;
            case 3024:
                ret = responseVoid(p);
                break;
            case 3025:
                ret = responseInts(p);
                break;
            case 3026:
                ret = responseInts(p);
                break;
            case 3027:
                ret = responseInts(p);
                break;
            case 3028:
                ret = responseInts(p);
                break;
            case 3029:
                ret = responseInts(p);
                break;
            case 3033:
                ret = responseStrings(p);
                break;
            case 3034:
                ret = responseInts(p);
                break;
            case 3035:
                ret = responseInts(p);
                break;
            case 3036:
                ret = responseCrssNotification(p);
                break;
            case 3037:
                ret = responseStrings(p);
                break;
            case 3038:
                ret = responseStrings(p);
                break;
            case 3039:
                ret = responseVoid(p);
                break;
            case 3040:
                ret = responseInts(p);
                break;
            case 3041:
                ret = responseStrings(p);
                break;
            case 3042:
                ret = responseInts(p);
                break;
            case 3043:
                ret = responseStrings(p);
                break;
            case 3044:
                ret = responseInts(p);
                break;
            case 3045:
                ret = responseInts(p);
                break;
            case 3046:
                ret = responseInts(p);
                break;
            case 3047:
                ret = responseInts(p);
                break;
            case 3048:
                ret = responseVoid(p);
                break;
            case 3049:
                ret = responseInts(p);
                break;
            case 3051:
                ret = responseStrings(p);
                break;
            case 3052:
                ret = responseInts(p);
                break;
            case 3054:
                ret = responseInts(p);
                break;
            case 3058:
                ret = responseVoid(p);
                break;
            case 3060:
                ret = responseString(p);
                break;
            case 3061:
                ret = responseInts(p);
                break;
            case 3062:
                ret = responseInts(p);
                break;
            case 3063:
                ret = responseVoid(p);
                break;
            case 3065:
                ret = responseVoid(p);
                break;
            case 3068:
                ret = responseVoid(p);
                break;
            case 3071:
                ret = responseInts(p);
                break;
            case 3074:
                ret = responseVoid(p);
                break;
            case 3081:
                ret = responseInts(p);
                break;
            case 3082:
                ret = responseInts(p);
                break;
            case 3083:
                ret = responseInts(p);
                break;
            case 3093:
                ret = responseInts(p);
                break;
            case 3095:
                ret = responseInts(p);
                break;
            case 3096:
                ret = responseInts(p);
                break;
            case 3098:
                ret = responseVoid(p);
                break;
            case 3099:
                ret = responseInts(p);
                break;
            case 3100:
                ret = responseString(p);
                break;
            case 3103:
                ret = responseInts(p);
                break;
            case 3116:
                ret = responseVoid(p);
                break;
            case 3118:
                ret = responseStrings(p);
                break;
            case 3122:
                ret = responseInts(p);
                break;
            default:
                try {
                    throw new RuntimeException("Unrecognized unsol response: " + response);
                } catch (Throwable tr) {
                    Rlog.e(RILJ_LOG_TAG, "Exception processing unsol response: " + response + "Exception:" + tr.toString());
                    return;
                }
        }
        SmsMessage sms;
        Intent intent;
        Object obj;
        switch (response) {
            case 1000:
                RadioState newState = getRadioStateFromInt(p.readInt());
                unsljLogMore(response, newState.toString());
                switchToRadioState(newState);
                break;
            case 1001:
                unsljLog(response);
                this.mCallStateRegistrants.notifyRegistrants(new AsyncResult(null, null, null));
                break;
            case 1002:
                unsljLogvRet(response, ret);
                this.mVoiceNetworkStateRegistrants.notifyRegistrants(new AsyncResult(null, ret, null));
                break;
            case 1003:
                try {
                    unsljLog(response);
                    this.mMetrics.writeRilNewSms(this.mInstanceId.intValue(), 1, 1);
                    String[] a = new String[2];
                    a[1] = (String) ret;
                    sms = SmsMessage.newFromCMT(a);
                    if (this.mGsmSmsRegistrant == null) {
                        Rlog.d(RILJ_LOG_TAG, "Cache NEW_SMS event");
                        this.mSms = sms;
                        break;
                    }
                    this.mGsmSmsRegistrant.notifyRegistrant(new AsyncResult(null, sms, null));
                    break;
                } catch (Exception eNewSms) {
                    eNewSms.printStackTrace();
                    break;
                }
            case 1004:
                try {
                    unsljLogRet(response, ret);
                    if (this.mSmsStatusRegistrant == null) {
                        Rlog.d(RILJ_LOG_TAG, "Cache NEW_SMS_STATUS_REPORT event");
                        this.mStatusSms = ret;
                        break;
                    }
                    this.mSmsStatusRegistrant.notifyRegistrant(new AsyncResult(null, ret, null));
                    break;
                } catch (Exception eNewSmsSR) {
                    eNewSmsSR.printStackTrace();
                    break;
                }
            case 1005:
                try {
                    unsljLogRet(response, ret);
                    Object smsIndex = (int[]) ret;
                    if (smsIndex.length == 1) {
                        if (this.mSmsOnSimRegistrant == null) {
                            Rlog.d(RILJ_LOG_TAG, "Cache NEW_SMS_ON_SIM event");
                            this.mSimSms = smsIndex;
                            break;
                        }
                        this.mSmsOnSimRegistrant.notifyRegistrant(new AsyncResult(null, smsIndex, null));
                        break;
                    }
                    riljLog(" NEW_SMS_ON_SIM ERROR with wrong length " + smsIndex.length);
                    break;
                } catch (Exception eNewSmsOS) {
                    eNewSmsOS.printStackTrace();
                    break;
                }
            case 1006:
                Object resp = (String[]) ret;
                if (resp.length < 2) {
                    resp = new String[2];
                    resp[0] = ((String[]) ret)[0];
                    resp[1] = null;
                }
                unsljLogMore(response, resp[0]);
                if (this.mUSSDRegistrant != null) {
                    this.mUSSDRegistrant.notifyRegistrant(new AsyncResult(null, resp, null));
                    break;
                }
                break;
            case 1008:
                unsljLogRet(response, ret);
                long nitzReceiveTime = p.readLong();
                Object result = new Object[2];
                result[0] = ret;
                result[1] = Long.valueOf(nitzReceiveTime);
                if (!SystemProperties.getBoolean("telephony.test.ignore.nitz", false)) {
                    if (this.mNITZTimeRegistrant != null) {
                        this.mNITZTimeRegistrant.notifyRegistrant(new AsyncResult(null, result, null));
                    }
                    this.mLastNITZTimeInfo = result;
                    break;
                }
                riljLog("ignoring UNSOL_NITZ_TIME_RECEIVED");
                break;
            case 1009:
                if (this.mSignalStrengthRegistrant != null) {
                    this.mSignalStrengthRegistrant.notifyRegistrant(new AsyncResult(null, ret, null));
                    break;
                }
                break;
            case GsmVTProvider.SESSION_EVENT_START_COUNTER /*1010*/:
                unsljLogRet(response, ret);
                this.mDataNetworkStateRegistrants.notifyRegistrants(new AsyncResult(null, ret, null));
                break;
            case 1011:
                unsljLogRet(response, ret);
                if (this.mSsnRegistrant != null) {
                    this.mSsnRegistrant.notifyRegistrant(new AsyncResult(null, ret, null));
                    break;
                }
                break;
            case 1012:
                unsljLog(response);
                if (this.mCatSessionEndRegistrant != null) {
                    this.mCatSessionEndRegistrant.notifyRegistrant(new AsyncResult(null, ret, null));
                    break;
                }
                break;
            case CharacterSets.UTF_16BE /*1013*/:
                unsljLog(response);
                if (this.mCatProCmdRegistrant != null) {
                    this.mCatProCmdRegistrant.notifyRegistrant(new AsyncResult(null, ret, null));
                    break;
                }
                break;
            case CharacterSets.UTF_16LE /*1014*/:
                unsljLog(response);
                if (this.mCatEventRegistrant != null) {
                    this.mCatEventRegistrant.notifyRegistrant(new AsyncResult(null, ret, null));
                    break;
                }
                break;
            case CharacterSets.UTF_16 /*1015*/:
                unsljLogRet(response, ret);
                if (this.mCatCallSetUpRegistrant != null) {
                    this.mCatCallSetUpRegistrant.notifyRegistrant(new AsyncResult(null, ret, null));
                    break;
                }
                break;
            case CharacterSets.CESU_8 /*1016*/:
                unsljLog(response);
                if (this.mIccSmsFullRegistrant == null) {
                    Rlog.d(RILJ_LOG_TAG, "Cache sim sms full event");
                    this.mIsSmsSimFull = true;
                    break;
                }
                this.mIccSmsFullRegistrant.notifyRegistrant();
                break;
            case CharacterSets.UTF_32 /*1017*/:
                unsljLogRet(response, ret);
                if (this.mIccRefreshRegistrants != null) {
                    this.mIccRefreshRegistrants.notifyRegistrants(new AsyncResult(null, ret, null));
                    break;
                }
                break;
            case CharacterSets.UTF_32BE /*1018*/:
                unsljLogRet(response, ret);
                if (this.mRingRegistrant != null) {
                    this.mRingRegistrant.notifyRegistrant(new AsyncResult(null, ret, null));
                    break;
                }
                break;
            case CharacterSets.UTF_32LE /*1019*/:
                unsljLog(response);
                if (this.mIccStatusChangedRegistrants != null) {
                    this.mIccStatusChangedRegistrants.notifyRegistrants();
                    break;
                }
                break;
            case CharacterSets.BOCU_1 /*1020*/:
                try {
                    unsljLog(response);
                    this.mMetrics.writeRilNewSms(this.mInstanceId.intValue(), 2, 2);
                    sms = (SmsMessage) ret;
                    if (this.mCdmaSmsRegistrant == null) {
                        Rlog.d(RILJ_LOG_TAG, "Cache NEW_CDMA_SMS event");
                        this.mCdmaSms = sms;
                        break;
                    }
                    this.mCdmaSmsRegistrant.notifyRegistrant(new AsyncResult(null, sms, null));
                    break;
                } catch (Exception eNewSmsCdma) {
                    eNewSmsCdma.printStackTrace();
                    break;
                }
            case 1021:
                try {
                    unsljLogvRet(response, IccUtils.bytesToHexString((byte[]) ret));
                    if (this.mGsmBroadcastSmsRegistrant != null) {
                        this.mGsmBroadcastSmsRegistrant.notifyRegistrant(new AsyncResult(null, ret, null));
                        break;
                    }
                } catch (Exception eNewBrSms) {
                    eNewBrSms.printStackTrace();
                    break;
                }
                break;
            case 1022:
                unsljLog(response);
                if (this.mIccSmsFullRegistrant == null) {
                    Rlog.d(RILJ_LOG_TAG, "Cache sim sms full event");
                    this.mIsSmsSimFull = true;
                    break;
                }
                this.mIccSmsFullRegistrant.notifyRegistrant();
                break;
            case 1023:
                unsljLogvRet(response, ret);
                if (this.mRestrictedStateRegistrant != null) {
                    this.mRestrictedStateRegistrant.notifyRegistrant(new AsyncResult(null, ret, null));
                    break;
                }
                break;
            case 1024:
                unsljLog(response);
                if (this.mEmergencyCallbackModeRegistrant != null) {
                    this.mEmergencyCallbackModeRegistrant.notifyRegistrant();
                    break;
                }
                break;
            case 1025:
                unsljLogRet(response, ret);
                if (this.mCallWaitingInfoRegistrants != null) {
                    this.mCallWaitingInfoRegistrants.notifyRegistrants(new AsyncResult(null, ret, null));
                    break;
                }
                break;
            case 1026:
                unsljLogRet(response, ret);
                if (this.mOtaProvisionRegistrants != null) {
                    this.mOtaProvisionRegistrants.notifyRegistrants(new AsyncResult(null, ret, null));
                    break;
                }
                break;
            case 1027:
                try {
                    for (CdmaInformationRecords rec : (ArrayList) ret) {
                        unsljLogRet(response, rec);
                        notifyRegistrantsCdmaInfoRec(rec);
                    }
                    break;
                } catch (Throwable e) {
                    Rlog.e(RILJ_LOG_TAG, "Unexpected exception casting to listInfoRecs", e);
                    break;
                }
            case 1028:
                unsljLogvRet(response, IccUtils.bytesToHexString((byte[]) ret));
                if (this.mUnsolOemHookRawRegistrant != null) {
                    this.mUnsolOemHookRawRegistrant.notifyRegistrant(new AsyncResult(null, ret, null));
                    break;
                }
                break;
            case 1029:
                unsljLogvRet(response, ret);
                if (this.mRingbackToneRegistrants != null) {
                    this.mRingbackToneRegistrants.notifyRegistrants(new AsyncResult(null, Boolean.valueOf(((int[]) ret)[0] == 1), null));
                    break;
                }
                break;
            case 1030:
                unsljLogRet(response, ret);
                if (this.mResendIncallMuteRegistrants != null) {
                    this.mResendIncallMuteRegistrants.notifyRegistrants(new AsyncResult(null, ret, null));
                    break;
                }
                break;
            case 1031:
                unsljLogRet(response, ret);
                if (this.mCdmaSubscriptionChangedRegistrants != null) {
                    this.mCdmaSubscriptionChangedRegistrants.notifyRegistrants(new AsyncResult(null, ret, null));
                    break;
                }
                break;
            case 1032:
                unsljLogRet(response, ret);
                if (this.mCdmaPrlChangedRegistrants != null) {
                    this.mCdmaPrlChangedRegistrants.notifyRegistrants(new AsyncResult(null, ret, null));
                    break;
                }
                break;
            case 1033:
                unsljLogRet(response, ret);
                if (this.mExitEmergencyCallbackModeRegistrants != null) {
                    this.mExitEmergencyCallbackModeRegistrants.notifyRegistrants(new AsyncResult(null, null, null));
                    break;
                }
                break;
            case 1034:
                unsljLogRet(response, ret);
                if (UiccController.getVsimState() == 1) {
                    UiccController.setVsimState(0);
                }
                if (TelephonyManager.getDefault().getMultiSimConfiguration() == MultiSimVariants.DSDA || this.mInstanceId.intValue() == 0) {
                    setEccList();
                }
                setCdmaSubscriptionSource(this.mCdmaSubscription, null);
                setCellInfoListRate(Integer.MAX_VALUE, null);
                notifyRegistrantsRilConnectionChanged(((int[]) ret)[0]);
                if (this.mDefaultDisplayState != 2) {
                    if (this.mDefaultDisplayState != 1) {
                        riljLog("not setScreenState mDefaultDisplayState=" + this.mDefaultDisplayState);
                        break;
                    } else {
                        sendScreenState(false);
                        break;
                    }
                }
                sendScreenState(true);
                break;
            case 1035:
                unsljLogRet(response, ret);
                this.mNewVoiceTech[0] = ((int[]) ret)[0];
                if (this.mVoiceRadioTechChangedRegistrants != null) {
                    this.mVoiceRadioTechChangedRegistrants.notifyRegistrants(new AsyncResult(null, ret, null));
                    break;
                }
                break;
            case 1036:
                unsljLogRet(response, ret);
                if (this.mRilCellInfoListRegistrants != null) {
                    this.mRilCellInfoListRegistrants.notifyRegistrants(new AsyncResult(null, ret, null));
                    break;
                }
                break;
            case 1037:
                unsljLog(response);
                this.mImsNetworkStateChangedRegistrants.notifyRegistrants(new AsyncResult(null, null, null));
                break;
            case 1038:
                unsljLogRet(response, ret);
                if (this.mSubscriptionStatusRegistrants != null) {
                    this.mSubscriptionStatusRegistrants.notifyRegistrants(new AsyncResult(null, ret, null));
                    break;
                }
                break;
            case 1039:
                unsljLogRet(response, ret);
                this.mMetrics.writeRilSrvcc(this.mInstanceId.intValue(), ((int[]) ret)[0]);
                if (this.mSrvccStateRegistrants != null) {
                    this.mSrvccStateRegistrants.notifyRegistrants(new AsyncResult(null, ret, null));
                    break;
                }
                break;
            case 1040:
                unsljLogRet(response, ret);
                if (this.mHardwareConfigChangeRegistrants != null) {
                    this.mHardwareConfigChangeRegistrants.notifyRegistrants(new AsyncResult(null, ret, null));
                    break;
                }
                break;
            case 1042:
                unsljLogRet(response, ret);
                this.mRadioCapability = (RadioCapability) ret;
                if (this.mPhoneRadioCapabilityChangedRegistrants != null) {
                    this.mPhoneRadioCapabilityChangedRegistrants.notifyRegistrants(new AsyncResult(null, ret, null));
                    break;
                }
                break;
            case 1043:
                unsljLogRet(response, ret);
                if (this.mSsRegistrant != null) {
                    this.mSsRegistrant.notifyRegistrant(new AsyncResult(null, ret, null));
                    break;
                }
                break;
            case 1044:
                unsljLogRet(response, ret);
                if (this.mCatCcAlphaRegistrant != null) {
                    this.mCatCcAlphaRegistrant.notifyRegistrant(new AsyncResult(null, ret, null));
                    break;
                }
                break;
            case 1045:
                unsljLogRet(response, ret);
                if (this.mLceInfoRegistrant != null) {
                    this.mLceInfoRegistrant.notifyRegistrant(new AsyncResult(null, ret, null));
                    break;
                }
                break;
            case 1046:
                unsljLogRet(response, ret);
                this.mPcoDataRegistrants.notifyRegistrants(new AsyncResult(null, ret, null));
                break;
            case 3000:
                unsljLogvRet(response, ret);
                if (this.mNeighboringInfoRegistrants != null) {
                    this.mNeighboringInfoRegistrants.notifyRegistrants(new AsyncResult(null, ret, null));
                    break;
                }
                break;
            case 3001:
                unsljLogvRet(response, ret);
                if (ret != null) {
                    int nwinfo_type = Integer.parseInt(((String[]) ret)[0]);
                    if (nwinfo_type == 401 || nwinfo_type == 402 || nwinfo_type == 403) {
                        intent = new Intent("com.mediatek.log2server.EXCEPTION_HAPPEND");
                        intent.putExtra("Reason", "SmartLogging");
                        intent.putExtra("from_where", "RIL");
                        this.mContext.sendBroadcast(intent);
                        riljLog("Broadcast for SmartLogging " + nwinfo_type);
                        break;
                    }
                    oppoProcessUnsolOemKeyLogErrMsg(ret);
                }
                if (this.mNetworkInfoRegistrants != null) {
                    this.mNetworkInfoRegistrants.notifyRegistrants(new AsyncResult(null, ret, null));
                    break;
                }
                break;
            case 3002:
                unsljLogRet(response, ret);
                if (this.mPhbReadyRegistrants != null) {
                    this.mPhbReadyRegistrants.notifyRegistrants(new AsyncResult(null, ret, null));
                    break;
                }
                break;
            case 3004:
                unsljLog(response);
                if (this.mMeSmsFullRegistrant != null) {
                    this.mMeSmsFullRegistrant.notifyRegistrant();
                    break;
                }
                break;
            case 3005:
                unsljLog(response);
                if (this.mSmsReadyRegistrants.size() == 0) {
                    Rlog.d(RILJ_LOG_TAG, "Cache sms ready event");
                    this.mIsSmsReady = true;
                    break;
                }
                this.mSmsReadyRegistrants.notifyRegistrants();
                break;
            case 3006:
                unsljLogRet(response, ret);
                if (this.mSimMissing != null) {
                    this.mSimMissing.notifyRegistrants(new AsyncResult(null, ret, null));
                    break;
                }
                break;
            case 3008:
                unsljLogRet(response, ret);
                if (this.mSimRecovery != null) {
                    this.mSimRecovery.notifyRegistrants(new AsyncResult(null, ret, null));
                    break;
                }
                break;
            case 3009:
                unsljLogRet(response, ret);
                if (this.mVirtualSimOn != null) {
                    this.mVirtualSimOn.notifyRegistrants(new AsyncResult(null, ret, null));
                    break;
                }
                break;
            case 3010:
                unsljLogRet(response, ret);
                if (this.mVirtualSimOff != null) {
                    this.mVirtualSimOff.notifyRegistrants(new AsyncResult(null, ret, null));
                    break;
                }
                break;
            case 3011:
                unsljLogvRet(response, ret);
                if (this.mInvalidSimInfoRegistrant != null) {
                    this.mInvalidSimInfoRegistrant.notifyRegistrants(new AsyncResult(null, ret, null));
                    break;
                }
                break;
            case 3012:
                unsljLog(response);
                Object stat = null;
                if (ret != null) {
                    stat = (int[]) ret;
                }
                this.mPsNetworkStateRegistrants.notifyRegistrants(new AsyncResult(null, stat, null));
                break;
            case 3013:
                unsljLog(response);
                if (ret != null) {
                    int[] acmt = (int[]) ret;
                    if (acmt.length == 2) {
                        int error_type = Integer.valueOf(acmt[0]).intValue();
                        int error_cause = acmt[1];
                        if (!SystemProperties.get("ro.mtk_bsp_package").equals("1")) {
                            try {
                                if (this.mServiceStateExt.needBrodcastAcmt(error_type, error_cause)) {
                                    intent = new Intent("mediatek.intent.action.acmt_nw_service_status");
                                    intent.putExtra("CauseCode", acmt[1]);
                                    intent.putExtra("CauseType", acmt[0]);
                                    this.mContext.sendBroadcast(intent);
                                    riljLog("Broadcast for ACMT: com.VendorName.CauseCode " + acmt[1] + "," + acmt[0]);
                                    break;
                                }
                            } catch (RuntimeException e2) {
                                e2.printStackTrace();
                                break;
                            }
                        }
                    }
                }
                break;
            case 3015:
                unsljLog(response);
                if (this.mImeiLockRegistrant != null) {
                    this.mImeiLockRegistrant.notifyRegistrants(new AsyncResult(null, null, null));
                    break;
                }
                break;
            case 3016:
                unsljLog(response);
                if (ret != null) {
                    int ps_status = Integer.valueOf(((int[]) ret)[0]).intValue();
                    if (!SystemProperties.get("ro.mtk_bsp_package").equals("1")) {
                        try {
                            if (this.mServiceStateExt.isBroadcastEmmrrsPsResume(ps_status)) {
                                riljLog("Broadcast for EMMRRS: android.intent.action.EMMRRS_PS_RESUME ");
                                break;
                            }
                        } catch (RuntimeException e22) {
                            e22.printStackTrace();
                            break;
                        }
                    }
                }
                break;
            case 3017:
                unsljLogRet(response, ret);
                if (this.mSimPlugOutRegistrants != null) {
                    this.mSimPlugOutRegistrants.notifyRegistrants(new AsyncResult(null, ret, null));
                }
                this.mCfuReturnValue = null;
                Object retCfValue = new int[2];
                retCfValue[0] = 0;
                retCfValue[1] = 1;
                riljLog("Notify CFU change to disable due to sim plug out.");
                this.mCallForwardingInfoRegistrants.notifyRegistrants(new AsyncResult(null, retCfValue, null));
                break;
            case 3018:
                unsljLogRet(response, ret);
                if (this.mSimPlugInRegistrants != null) {
                    this.mSimPlugInRegistrants.notifyRegistrants(new AsyncResult(null, ret, null));
                    break;
                }
                break;
            case 3019:
                unsljLog(response);
                if (this.mEtwsNotificationRegistrant != null) {
                    this.mEtwsNotificationRegistrant.notifyRegistrant(new AsyncResult(null, ret, null));
                    break;
                }
                break;
            case 3020:
                unsljLogvRet(response, ret);
                obj = this.mWPMonitor;
                synchronized (obj) {
                    if (this.mPlmnChangeNotificationRegistrant.size() <= 0) {
                        this.mEcopsReturnValue = ret;
                        break;
                    }
                    riljLog("ECOPS,notify mPlmnChangeNotificationRegistrant");
                    this.mPlmnChangeNotificationRegistrant.notifyRegistrants(new AsyncResult(null, ret, null));
                    break;
                }
            case 3021:
                unsljLogvRet(response, ret);
                obj = this.mWPMonitor;
                synchronized (obj) {
                    if (this.mRegistrationSuspendedRegistrant == null) {
                        this.mEmsrReturnValue = ret;
                        break;
                    }
                    riljLog("EMSR, notify mRegistrationSuspendedRegistrant");
                    this.mRegistrationSuspendedRegistrant.notifyRegistrant(new AsyncResult(null, ret, null));
                    break;
                }
            case 3022:
                if (!SystemProperties.get("ro.mtk_bsp_package").equals("1")) {
                    unsljLogvRet(response, ret);
                    if (this.mStkEvdlCallRegistrant != null) {
                        this.mStkEvdlCallRegistrant.notifyRegistrant(new AsyncResult(null, ret, null));
                        break;
                    }
                }
                break;
            case 3023:
                unsljLogvRet(response, ret);
                this.mFemtoCellInfoRegistrants.notifyRegistrants(new AsyncResult(null, ret, null));
                break;
            case 3024:
                unsljLogRet(response, ret);
                if (this.mStkSetupMenuResetRegistrant != null) {
                    this.mStkSetupMenuResetRegistrant.notifyRegistrant(new AsyncResult(null, ret, null));
                    break;
                }
                break;
            case 3025:
                unsljLog(response);
                if (this.mSessionChangedRegistrants != null) {
                    this.mSessionChangedRegistrants.notifyRegistrants(new AsyncResult(null, ret, null));
                    break;
                }
                break;
            case 3026:
                unsljLog(response);
                if (this.mEconfSrvccRegistrants != null) {
                    this.mEconfSrvccRegistrants.notifyRegistrants(new AsyncResult(null, ret, null));
                    break;
                }
                break;
            case 3027:
                unsljLog(response);
                if (this.mImsEnableRegistrants != null) {
                    this.mImsEnableRegistrants.notifyRegistrants(new AsyncResult(null, ret, null));
                    break;
                }
                break;
            case 3028:
                unsljLog(response);
                if (this.mImsDisableRegistrants != null) {
                    this.mImsDisableRegistrants.notifyRegistrants(new AsyncResult(null, ret, null));
                    break;
                }
                break;
            case 3029:
                unsljLog(response);
                if (this.mImsRegistrationInfoRegistrants != null) {
                    this.mImsRegistrationInfoRegistrants.notifyRegistrants(new AsyncResult(null, ret, null));
                    break;
                }
                break;
            case 3033:
                unsljLog(response);
                if (this.mEconfResultRegistrants != null) {
                    riljLog("Notify ECONF result");
                    riljLog("ECONF result = " + ((String[]) ret)[3]);
                    this.mEconfResultRegistrants.notifyRegistrants(new AsyncResult(null, ret, null));
                    break;
                }
                break;
            case 3034:
                unsljLogRet(response, ret);
                if (this.mMelockRegistrants != null) {
                    this.mMelockRegistrants.notifyRegistrants(new AsyncResult(null, ret, null));
                    break;
                }
                break;
            case 3035:
                unsljLogRet(response, ret);
                if (this.mCallForwardingInfoRegistrants != null) {
                    if (((int[]) ret)[0] == 1) {
                    }
                    if (((int[]) ret)[1] == 1) {
                        this.mCfuReturnValue = ret;
                        this.mCallForwardingInfoRegistrants.notifyRegistrants(new AsyncResult(null, ret, null));
                        break;
                    }
                }
                break;
            case 3036:
                unsljLogRet(response, ret);
                if (this.mCallRelatedSuppSvcRegistrant != null) {
                    this.mCallRelatedSuppSvcRegistrant.notifyRegistrant(new AsyncResult(null, ret, null));
                    break;
                }
                break;
            case 3037:
                unsljLogvRet(response, ret);
                if (this.mIncomingCallIndicationRegistrant != null) {
                    this.mIncomingCallIndicationRegistrant.notifyRegistrant(new AsyncResult(null, ret, null));
                    break;
                }
                break;
            case 3038:
                unsljLogvRet(response, ret);
                int simCipherStatus = Integer.parseInt(((String[]) ret)[0]);
                int sessionStatus = Integer.parseInt(((String[]) ret)[1]);
                int csStatus = Integer.parseInt(((String[]) ret)[2]);
                int psStatus = Integer.parseInt(((String[]) ret)[3]);
                riljLog("RIL_UNSOL_CIPHER_INDICATION :" + simCipherStatus + " " + sessionStatus + " " + csStatus + " " + psStatus);
                int[] cipherResult = new int[3];
                cipherResult[0] = simCipherStatus;
                cipherResult[1] = csStatus;
                cipherResult[2] = psStatus;
                if (this.mCipherIndicationRegistrant != null) {
                    this.mCipherIndicationRegistrant.notifyRegistrants(new AsyncResult(null, cipherResult, null));
                    break;
                }
                break;
            case 3039:
                unsljLog(response);
                if (this.mCommonSlotNoChangedRegistrants != null) {
                    this.mCommonSlotNoChangedRegistrants.notifyRegistrants(new AsyncResult(null, null, null));
                    break;
                }
                break;
            case 3040:
                unsljLog(response);
                if (this.mDataAllowedRegistrants != null) {
                    this.mDataAllowedRegistrants.notifyRegistrants(new AsyncResult(null, ret, null));
                    break;
                }
                break;
            case 3041:
                unsljLogvRet(response, ret);
                if (this.mStkCallCtrlRegistrant != null) {
                    this.mStkCallCtrlRegistrant.notifyRegistrant(new AsyncResult(null, ret, null));
                    break;
                }
                break;
            case 3042:
                unsljLogRet(response, ret);
                if (this.mEpsNetworkFeatureSupportRegistrants != null) {
                    this.mEpsNetworkFeatureSupportRegistrants.notifyRegistrants(new AsyncResult(null, ret, null));
                    break;
                }
                break;
            case 3043:
                unsljLog(response);
                if (this.mCallInfoRegistrants != null) {
                    this.mCallInfoRegistrants.notifyRegistrants(new AsyncResult(null, ret, null));
                    break;
                }
                break;
            case 3044:
                unsljLog(response);
                if (this.mEpsNetworkFeatureInfoRegistrants != null) {
                    this.mEpsNetworkFeatureInfoRegistrants.notifyRegistrants(new AsyncResult(null, ret, null));
                    break;
                }
                break;
            case 3045:
                unsljLog(response);
                if (this.mSrvccHandoverInfoIndicationRegistrants != null) {
                    this.mSrvccHandoverInfoIndicationRegistrants.notifyRegistrants(new AsyncResult(null, ret, null));
                    break;
                }
                break;
            case 3046:
                unsljLogvRet(response, ret);
                if (this.mSpeechCodecInfoRegistrant != null) {
                    this.mSpeechCodecInfoRegistrant.notifyRegistrant(new AsyncResult(null, ret, null));
                    break;
                }
                break;
            case 3047:
                unsljLogRet(response, ret);
                break;
            case 3048:
                unsljLog(response);
                this.mRemoveRestrictEutranRegistrants.notifyRegistrants(new AsyncResult(null, null, null));
                break;
            case 3049:
                unsljLog(response);
                if (this.mSsacBarringInfoRegistrants != null) {
                    this.mSsacBarringInfoRegistrants.notifyRegistrants(new AsyncResult(null, ret, null));
                    break;
                }
                break;
            case 3051:
                unsljLogvRet(response, ret);
                if (this.mAbnormalEventRegistrant != null) {
                    this.mAbnormalEventRegistrant.notifyRegistrants(new AsyncResult(null, ret, null));
                    break;
                }
                break;
            case 3052:
                unsljLog(response);
                if (this.mEmergencyBearerSupportInfoRegistrants != null) {
                    this.mEmergencyBearerSupportInfoRegistrants.notifyRegistrants(new AsyncResult(null, ret, null));
                    break;
                }
                break;
            case 3054:
                unsljLogvRet(response, ret);
                Object rat = (int[]) ret;
                riljLog("Notify RIL_UNSOL_GMSS_RAT_CHANGED result rat = " + rat);
                if (this.mGmssRatChangedRegistrant != null) {
                    this.mGmssRatChangedRegistrant.notifyRegistrants(new AsyncResult(null, rat, null));
                    break;
                }
                break;
            case 3058:
                unsljLog(response);
                if (this.mImsiRefreshDoneRegistrant != null) {
                    this.mImsiRefreshDoneRegistrant.notifyRegistrants(new AsyncResult(null, ret, null));
                    break;
                }
                break;
            case 3060:
                unsljLog(response);
                if (this.mBipProCmdRegistrant != null) {
                    this.mBipProCmdRegistrant.notifyRegistrant(new AsyncResult(null, ret, null));
                    break;
                }
                break;
            case 3061:
                unsljLog(response);
                if (ret != null) {
                    boolean retvalue;
                    int state = ((int[]) ret)[0];
                    if (state == 2) {
                        retvalue = WorldMode.resetSwitchingState(state);
                        state = 1;
                    } else if (state == 0) {
                        retvalue = WorldMode.updateSwitchingState(true);
                    } else {
                        retvalue = WorldMode.updateSwitchingState(false);
                    }
                    if (retvalue) {
                        intent = new Intent("android.intent.action.ACTION_WORLD_MODE_CHANGED");
                        intent.putExtra("worldModeState", Integer.valueOf(state));
                        this.mContext.sendBroadcast(intent);
                        riljLog("Broadcast for WorldModeChanged: state=" + state);
                        break;
                    }
                }
                break;
            case 3062:
                unsljLogvRet(response, ret);
                if (this.mVtStatusInfoRegistrants != null) {
                    this.mVtStatusInfoRegistrants.notifyRegistrants(new AsyncResult(null, ret, null));
                    break;
                }
                break;
            case 3063:
                unsljLogvRet(response, ret);
                if (this.mVtRingRegistrants != null) {
                    this.mVtRingRegistrants.notifyRegistrants(new AsyncResult(null, ret, null));
                    break;
                }
                break;
            case 3065:
                unsljLog(response);
                this.mResetAttachApnRegistrants.notifyRegistrants(new AsyncResult(null, null, null));
                break;
            case 3068:
                unsljLogRet(response, ret);
                if (this.mTrayPlugInRegistrants != null) {
                    this.mTrayPlugInRegistrants.notifyRegistrants(new AsyncResult(null, ret, null));
                    break;
                }
                break;
            case 3071:
                unsljLogRet(response, ret);
                if (this.mLteAccessStratumStateRegistrants != null) {
                    this.mLteAccessStratumStateRegistrants.notifyRegistrants(new AsyncResult(null, ret, null));
                    break;
                }
                break;
            case 3074:
                unsljLogRet(response, ret);
                if (this.mAcceptedRegistrant != null) {
                    this.mAcceptedRegistrant.notifyRegistrants(new AsyncResult(null, ret, null));
                    break;
                }
                break;
            case 3081:
                unsljLogRet(response, ret);
                if (this.mNetworkExistRegistrants != null) {
                    this.mNetworkExistRegistrants.notifyRegistrants(new AsyncResult(null, ret, null));
                    break;
                }
                break;
            case 3082:
                unsljLogRet(response, ret);
                if (this.mModulationRegistrants != null) {
                    this.mModulationRegistrants.notifyRegistrants(new AsyncResult(null, ret, null));
                    break;
                }
                break;
            case 3083:
                unsljLogRet(response, ret);
                if (this.mNetworkEventRegistrants != null) {
                    this.mNetworkEventRegistrants.notifyRegistrants(new AsyncResult(null, ret, null));
                    break;
                }
                break;
            case 3093:
                unsljLogRet(response, ret);
                if (ret != null) {
                    int[] status = (int[]) ret;
                    intent = new Intent("mediatek.intent.action.EMBMS_SESSION_STATUS_CHANGED");
                    intent.putExtra("phone", this.mInstanceId);
                    intent.putExtra("isActived", status[0]);
                    this.mContext.sendBroadcast(intent);
                    break;
                }
                break;
            case 3095:
                unsljLogRet(response, ret);
                if (this.mPcoStatusRegistrant != null) {
                    this.mPcoStatusRegistrant.notifyRegistrants(new AsyncResult(null, ret, null));
                    break;
                }
                break;
            case 3096:
                unsljLogRet(response, ret);
                this.mAttachApnChangedRegistrants.notifyRegistrants(new AsyncResult(null, ret, null));
                break;
            case 3098:
                unsljLog(response);
                String[] testTriggerOtasp = new String[3];
                testTriggerOtasp[0] = "AT+CDV=*22899";
                testTriggerOtasp[1] = UsimPBMemInfo.STRING_NOT_SET;
                testTriggerOtasp[2] = "DESTRILD:C2K";
                invokeOemRilRequestStrings(testTriggerOtasp, null);
                break;
            case 3099:
                unsljLogvRet(response, ret);
                if (this.mCallRedialStateRegistrants != null) {
                    this.mCallRedialStateRegistrants.notifyRegistrants(new AsyncResult(null, Integer.valueOf(((int[]) ret)[0]), null));
                    break;
                }
                break;
            case 3100:
                unsljLog(response);
                if (this.mCDMACardEsnMeidRegistrant == null) {
                    this.mEspOrMeid = ret;
                    break;
                } else {
                    this.mCDMACardEsnMeidRegistrant.notifyRegistrant(new AsyncResult(null, ret, null));
                    break;
                }
            case 3103:
                unsljLogRet(response, ret);
                int phoneId = this.mInstanceId.intValue();
                int[] msgs = (int[]) ret;
                int size = msgs[0];
                riljLog("PseudoBSRecord: phoneId=" + phoneId + ", size=" + size);
                if (size > 0 && size <= 2) {
                    PseudoBSRecord[] list = new PseudoBSRecord[size];
                    for (int i = 0; i < size; i++) {
                        list[i] = new PseudoBSRecord(msgs[(i * 6) + 1], msgs[(i * 6) + 2], msgs[(i * 6) + 3], msgs[(i * 6) + 4], msgs[(i * 6) + 5], msgs[(i * 6) + 6]);
                    }
                    if (list[0].getType() != 2 && list[0].getType() != 3) {
                        intent = new Intent("android.intent.action.ACTION_PSEUDO_BS_DETECTED");
                        intent.putExtra("phoneId", phoneId);
                        intent.putExtra("pseudoInfo", list);
                        this.mContext.sendBroadcast(intent);
                        intent = new Intent("android.intent.action.FAKE_BS_BLOCKED");
                        intent.putExtra("arfcn", list[0].getArfcn());
                        this.mContext.sendBroadcast(intent, "oppo.permission.OPPO_COMPONENT_SAFE");
                        riljLog("fake BS blocked, arfcn:" + list[0].getArfcn());
                        break;
                    }
                    long timeStamp = System.currentTimeMillis();
                    if (list[0].getType() == 2) {
                        SystemProperties.set("ril.apc.cell.timestamp" + phoneId, Long.toString(timeStamp));
                    } else {
                        SystemProperties.set("ril.apc.cell.timestamp" + phoneId, "0");
                    }
                    intent = new Intent("state");
                    intent.putExtra("state", list[0].getType() == 2 ? 1 : 0);
                    intent.putExtra("info", list);
                    intent.putExtra("timeStamp", timeStamp);
                    this.mContext.sendBroadcast(intent);
                    return;
                }
                return;
            case 3116:
                unsljLog(response);
                this.mMdDataRetryCountResetRegistrants.notifyRegistrants(new AsyncResult(null, null, null));
                break;
            case 3118:
                unsljLogvRet(response, ret);
                if (this.mCsNetworkStateRegistrants != null) {
                    this.mCsNetworkStateRegistrants.notifyRegistrants(new AsyncResult(null, ret, null));
                    break;
                }
                break;
            case 3122:
                unsljLogRet(response, ret);
                if (this.mTxPowerRegistrant != null) {
                    this.mTxPowerRegistrant.notifyRegistrants(new AsyncResult(null, ret, null));
                    break;
                }
                break;
        }
    }

    private void notifyRegistrantsRilConnectionChanged(int rilVer) {
        this.mRilVersion = rilVer;
        if (this.mRilConnectedRegistrants != null) {
            this.mRilConnectedRegistrants.notifyRegistrants(new AsyncResult(null, new Integer(rilVer), null));
        }
    }

    private Object responseInts(Parcel p) {
        int numInts = p.readInt();
        int[] response = new int[numInts];
        for (int i = 0; i < numInts; i++) {
            response[i] = p.readInt();
        }
        return response;
    }

    private Object responseFailCause(Parcel p) {
        LastCallFailCause failCause = new LastCallFailCause();
        failCause.causeCode = p.readInt();
        if (p.dataAvail() > 0) {
            failCause.vendorCause = p.readString();
        }
        return failCause;
    }

    private Object responseVoid(Parcel p) {
        return null;
    }

    private Object responseCallForward(Parcel p) {
        int numInfos = p.readInt();
        CallForwardInfo[] infos = new CallForwardInfo[numInfos];
        for (int i = 0; i < numInfos; i++) {
            infos[i] = new CallForwardInfo();
            infos[i].status = p.readInt();
            infos[i].reason = p.readInt();
            infos[i].serviceClass = p.readInt();
            infos[i].toa = p.readInt();
            infos[i].number = p.readString();
            infos[i].timeSeconds = p.readInt();
        }
        return infos;
    }

    private Object responseSuppServiceNotification(Parcel p) {
        SuppServiceNotification notification = new SuppServiceNotification();
        notification.notificationType = p.readInt();
        notification.code = p.readInt();
        notification.index = p.readInt();
        notification.type = p.readInt();
        notification.number = p.readString();
        return notification;
    }

    private Object responseCdmaSms(Parcel p) {
        return SmsMessage.newFromParcel(p);
    }

    private Object responseString(Parcel p) {
        return p.readString();
    }

    private Object responseStrings(Parcel p) {
        return p.readStringArray();
    }

    private Object responseStringEncodeBase64(Parcel p) {
        String response = p.readString();
        riljLog("responseStringEncodeBase64 - Response = " + response);
        byte[] auth_output = new byte[(response.length() / 2)];
        for (int i = 0; i < auth_output.length; i++) {
            auth_output[i] = (byte) (auth_output[i] | (Character.digit(response.charAt(i * 2), 16) * 16));
            auth_output[i] = (byte) (auth_output[i] | Character.digit(response.charAt((i * 2) + 1), 16));
        }
        response = Base64.encodeToString(auth_output, 2);
        riljLog("responseStringEncodeBase64 - Encoded Response = " + response);
        return response;
    }

    private Object responseRaw(Parcel p) {
        return p.createByteArray();
    }

    private Object responseSMS(Parcel p) {
        return new SmsResponse(p.readInt(), p.readString(), p.readInt());
    }

    private Object responseICC_IO(Parcel p) {
        return new IccIoResult(p.readInt(), p.readInt(), p.readString());
    }

    private Object responseICC_IOBase64(Parcel p) {
        return new IccIoResult(p.readInt(), p.readInt(), Base64.decode(p.readString(), 0));
    }

    private Object responseIccCardStatus(Parcel p) {
        IccCardStatus cardStatus = new IccCardStatus();
        cardStatus.setCardState(p.readInt());
        cardStatus.setUniversalPinState(p.readInt());
        cardStatus.mGsmUmtsSubscriptionAppIndex = p.readInt();
        cardStatus.mCdmaSubscriptionAppIndex = p.readInt();
        cardStatus.mImsSubscriptionAppIndex = p.readInt();
        int numApplications = p.readInt();
        if (numApplications > 8) {
            numApplications = 8;
        }
        cardStatus.mApplications = new IccCardApplicationStatus[numApplications];
        for (int i = 0; i < numApplications; i++) {
            IccCardApplicationStatus appStatus = new IccCardApplicationStatus();
            appStatus.app_type = appStatus.AppTypeFromRILInt(p.readInt());
            appStatus.app_state = appStatus.AppStateFromRILInt(p.readInt());
            appStatus.perso_substate = appStatus.PersoSubstateFromRILInt(p.readInt());
            appStatus.aid = p.readString();
            appStatus.app_label = p.readString();
            appStatus.pin1_replaced = p.readInt();
            appStatus.pin1 = appStatus.PinStateFromRILInt(p.readInt());
            appStatus.pin2 = appStatus.PinStateFromRILInt(p.readInt());
            cardStatus.mApplications[i] = appStatus;
        }
        return cardStatus;
    }

    private Object responseSimRefresh(Parcel p) {
        IccRefreshResponse response = new IccRefreshResponse();
        response.refreshResult = p.readInt();
        response.efId = p.readInt();
        response.aid = p.readString();
        return response;
    }

    private Object responseCallList(Parcel p) {
        int num = p.readInt();
        ArrayList<DriverCall> response = new ArrayList(num);
        for (int i = 0; i < num; i++) {
            boolean z;
            DriverCall dc = new DriverCall();
            dc.state = DriverCall.stateFromCLCC(p.readInt());
            dc.index = p.readInt();
            dc.TOA = p.readInt();
            if (p.readInt() != 0) {
                z = true;
            } else {
                z = false;
            }
            dc.isMpty = z;
            if (p.readInt() != 0) {
                z = true;
            } else {
                z = false;
            }
            dc.isMT = z;
            dc.als = p.readInt();
            if (p.readInt() == 0) {
                z = false;
            } else {
                z = true;
            }
            dc.isVoice = z;
            if (dc.isVoice) {
                z = false;
            } else {
                z = true;
            }
            dc.isVideo = z;
            riljLog("isVoice = " + dc.isVoice + ", isVideo = " + dc.isVideo);
            if (p.readInt() != 0) {
                z = true;
            } else {
                z = false;
            }
            dc.isVoicePrivacy = z;
            dc.number = p.readString();
            dc.numberPresentation = DriverCall.presentationFromCLIP(p.readInt());
            dc.name = p.readString();
            dc.namePresentation = DriverCall.presentationFromCLIP(p.readInt());
            if (p.readInt() == 1) {
                dc.uusInfo = new UUSInfo();
                dc.uusInfo.setType(p.readInt());
                dc.uusInfo.setDcs(p.readInt());
                dc.uusInfo.setUserData(p.createByteArray());
                Object[] objArr = new Object[3];
                objArr[0] = Integer.valueOf(dc.uusInfo.getType());
                objArr[1] = Integer.valueOf(dc.uusInfo.getDcs());
                objArr[2] = Integer.valueOf(dc.uusInfo.getUserData().length);
                riljLogv(String.format("Incoming UUS : type=%d, dcs=%d, length=%d", objArr));
                riljLogv("Incoming UUS : data (string)=" + new String(dc.uusInfo.getUserData()));
                riljLogv("Incoming UUS : data (hex): " + IccUtils.bytesToHexString(dc.uusInfo.getUserData()));
            } else {
                riljLogv("Incoming UUS : NOT present!");
            }
            dc.number = PhoneNumberUtils.stringFromStringAndTOA(dc.number, dc.TOA);
            response.add(dc);
            if (dc.isVoicePrivacy) {
                this.mVoicePrivacyOnRegistrants.notifyRegistrants();
                riljLog("InCall VoicePrivacy is enabled");
            } else {
                this.mVoicePrivacyOffRegistrants.notifyRegistrants();
                riljLog("InCall VoicePrivacy is disabled");
            }
        }
        Collections.sort(response);
        if (num == 0 && this.mTestingEmergencyCall.getAndSet(false) && this.mEmergencyCallbackModeRegistrant != null) {
            riljLog("responseCallList: call ended, testing emergency call, notify ECM Registrants");
            this.mEmergencyCallbackModeRegistrant.notifyRegistrant();
        }
        return response;
    }

    private DataCallResponse getDataCallResponse(Parcel p, int version) {
        DataCallResponse dataCall = new DataCallResponse();
        dataCall.version = version;
        String addresses;
        if (version < 5) {
            dataCall.cid = p.readInt();
            dataCall.active = p.readInt();
            dataCall.type = p.readString();
            addresses = p.readString();
            if (!TextUtils.isEmpty(addresses)) {
                dataCall.addresses = addresses.split(" ");
            }
        } else {
            dataCall.status = p.readInt();
            dataCall.suggestedRetryTime = p.readInt();
            dataCall.cid = p.readInt();
            dataCall.active = p.readInt();
            dataCall.type = p.readString();
            dataCall.ifname = p.readString();
            if (dataCall.status == DcFailCause.NONE.getErrorCode() && TextUtils.isEmpty(dataCall.ifname)) {
                throw new RuntimeException("getDataCallResponse, no ifname");
            }
            addresses = p.readString();
            if (!TextUtils.isEmpty(addresses)) {
                dataCall.addresses = addresses.split(" ");
            }
            String dnses = p.readString();
            if (!TextUtils.isEmpty(dnses)) {
                dataCall.dnses = dnses.split(" ");
            }
            String gateways = p.readString();
            if (!TextUtils.isEmpty(gateways)) {
                dataCall.gateways = gateways.split(" ");
            }
            String pcscf = p.readString();
            if (!TextUtils.isEmpty(pcscf)) {
                dataCall.pcscf = pcscf.split(" ");
            }
            dataCall.mtu = p.readInt();
            dataCall.rat = p.readInt();
        }
        return dataCall;
    }

    private Object responseDataCallList(Parcel p) {
        int ver = p.readInt();
        int num = p.readInt();
        riljLog("responseDataCallList ver=" + ver + " num=" + num);
        ArrayList<DataCallResponse> response = new ArrayList(num);
        for (int i = 0; i < num; i++) {
            response.add(getDataCallResponse(p, ver));
        }
        return response;
    }

    private Object responseSetupDataCall(Parcel p) {
        int ver = p.readInt();
        int num = p.readInt();
        if (ver < 5) {
            DataCallResponse dataCall = new DataCallResponse();
            dataCall.version = ver;
            dataCall.cid = Integer.parseInt(p.readString());
            dataCall.ifname = p.readString();
            if (TextUtils.isEmpty(dataCall.ifname)) {
                throw new RuntimeException("RIL_REQUEST_SETUP_DATA_CALL response, no ifname");
            }
            String addresses = p.readString();
            if (!TextUtils.isEmpty(addresses)) {
                dataCall.addresses = addresses.split(" ");
            }
            if (num >= 4) {
                String dnses = p.readString();
                riljLog("responseSetupDataCall got dnses=" + dnses);
                if (!TextUtils.isEmpty(dnses)) {
                    dataCall.dnses = dnses.split(" ");
                }
            }
            if (num >= 5) {
                String gateways = p.readString();
                riljLog("responseSetupDataCall got gateways=" + gateways);
                if (!TextUtils.isEmpty(gateways)) {
                    dataCall.gateways = gateways.split(" ");
                }
            }
            if (num < 6) {
                return dataCall;
            }
            String pcscf = p.readString();
            riljLog("responseSetupDataCall got pcscf=" + pcscf);
            if (TextUtils.isEmpty(pcscf)) {
                return dataCall;
            }
            dataCall.pcscf = pcscf.split(" ");
            return dataCall;
        } else if (num == 1) {
            return getDataCallResponse(p, ver);
        } else {
            throw new RuntimeException("RIL_REQUEST_SETUP_DATA_CALL resp. expecting 1 RIL_Data_Call_response_v5 got " + num);
        }
    }

    private Object responseOperatorInfos(Parcel p) {
        String[] strings = (String[]) responseStrings(p);
        SpnOverride spnOverride = SpnOverride.getInstance();
        if (strings.length % 4 != 0) {
            throw new RuntimeException("RIL_REQUEST_QUERY_AVAILABLE_NETWORKS: invalid response. Got " + strings.length + " strings, expected multible of 4");
        }
        ArrayList<OperatorInfo> ret = new ArrayList(strings.length / 4);
        for (int i = 0; i < strings.length; i += 4) {
            String strOperatorLong;
            if (spnOverride.containsCarrierEx(strings[i + 2])) {
                strOperatorLong = spnOverride.getSpnEx(strings[i + 2]);
            } else {
                strOperatorLong = strings[i + 0];
            }
            ret.add(new OperatorInfo(strOperatorLong, strings[i + 1], strings[i + 2], strings[i + 3]));
        }
        return ret;
    }

    private Object responseOperatorInfosWithAct(Parcel p) {
        String[] strings = (String[]) responseStrings(p);
        if (strings.length % 5 != 0) {
            throw new RuntimeException("RIL_REQUEST_QUERY_AVAILABLE_NETWORKS_WITH_ACT: invalid response. Got " + strings.length + " strings, expected multible of 5");
        }
        String lacStr = SystemProperties.get("gsm.cops.lac");
        boolean lacValid = false;
        int lacIndex = 0;
        Rlog.d(RILJ_LOG_TAG, "lacStr = " + lacStr + " lacStr.length=" + lacStr.length() + " strings.length=" + strings.length);
        if (lacStr.length() > 0 && lacStr.length() % 4 == 0 && lacStr.length() / 4 == strings.length / 5) {
            Rlog.d(RILJ_LOG_TAG, "lacValid set to true");
            lacValid = true;
        }
        SystemProperties.set("gsm.cops.lac", UsimPBMemInfo.STRING_NOT_SET);
        ArrayList<OperatorInfo> arrayList = new ArrayList(strings.length / 5);
        int i = 0;
        while (i < strings.length) {
            if (strings[i + 2] != null) {
                strings[i + 0] = SpnOverride.getInstance().lookupOperatorName(SubscriptionManager.getSubIdUsingPhoneId(this.mInstanceId.intValue()), strings[i + 2], true, this.mContext);
                strings[i + 1] = SpnOverride.getInstance().lookupOperatorName(SubscriptionManager.getSubIdUsingPhoneId(this.mInstanceId.intValue()), strings[i + 2], false, this.mContext);
                riljLog("lookup RIL responseOperator(), longAlpha= " + strings[i + 0] + ",shortAlpha= " + strings[i + 1] + ",numeric=" + strings[i + 2]);
            }
            String longName = lookupOperatorNameFromNetwork((long) SubscriptionManager.getSubIdUsingPhoneId(this.mInstanceId.intValue()), strings[i + 2], true);
            String shortName = lookupOperatorNameFromNetwork((long) SubscriptionManager.getSubIdUsingPhoneId(this.mInstanceId.intValue()), strings[i + 2], false);
            if (oppoIsOperatorNameEmpty(longName) && !oppoIsOperatorNameEmpty(shortName)) {
                strings[i + 0] = shortName;
                strings[i + 1] = shortName;
            } else if (!oppoIsOperatorNameEmpty(longName) && oppoIsOperatorNameEmpty(shortName)) {
                strings[i + 0] = longName;
                strings[i + 1] = longName;
            } else if (!(oppoIsOperatorNameEmpty(longName) || oppoIsOperatorNameEmpty(shortName))) {
                strings[i + 0] = longName;
                strings[i + 1] = shortName;
            }
            if (!SystemProperties.get("ro.mtk_bsp_package").equals("1")) {
                try {
                    strings[i + 0] = this.mServiceStateExt.updateOpAlphaLongForHK(strings[i + 0], strings[i + 2], this.mInstanceId.intValue());
                } catch (RuntimeException e) {
                    e.printStackTrace();
                }
            }
            riljLog("lookupOperatorNameFromNetwork in responseOperatorInfosWithAct(),updated longAlpha= " + strings[i + 0] + ",shortAlpha= " + strings[i + 1] + ",numeric=" + strings[i + 2]);
            if (lacValid && strings[i + 0] != null) {
                int phoneId = this.mInstanceId.intValue();
                SIMRecords simRecord = (SIMRecords) UiccController.getInstance().getIccRecords(this.mInstanceId.intValue(), 1);
                String lac = lacStr.substring(lacIndex, lacIndex + 4);
                Rlog.d(RILJ_LOG_TAG, "lacIndex=" + lacIndex + " lacValue=" + -1 + " lac=" + lac + " plmn numeric=" + strings[i + 2] + " plmn name" + strings[i + 0]);
                if (lac != UsimPBMemInfo.STRING_NOT_SET) {
                    int lacValue = Integer.parseInt(lac, 16);
                    lacIndex += 4;
                    if (lacValue != 65534) {
                        String sEons = simRecord.getEonsIfExist(strings[i + 2], lacValue, true);
                        if (TextUtils.isEmpty(sEons) || sEons.equals(" ")) {
                            String mSimOperatorNumeric = simRecord.getOperatorNumeric();
                            if (mSimOperatorNumeric != null && mSimOperatorNumeric.equals(strings[i + 2])) {
                                String sCphsOns = simRecord.getSIMCPHSOns();
                                if (!(sCphsOns == null || sCphsOns.equals(UsimPBMemInfo.STRING_NOT_SET))) {
                                    strings[i + 0] = sCphsOns;
                                    Rlog.d(RILJ_LOG_TAG, "plmn name update to CPHS Ons: " + strings[i + 0]);
                                }
                            }
                        } else {
                            strings[i + 0] = sEons;
                            strings[i + 1] = sEons;
                            Rlog.d(RILJ_LOG_TAG, "plmn name update to Eons: " + strings[i + 0]);
                        }
                    } else {
                        Rlog.d(RILJ_LOG_TAG, "invalid lac ignored");
                    }
                }
            }
            strings[i + 0] = strings[i + 0].concat(" " + strings[i + 4]);
            strings[i + 1] = strings[i + 1].concat(" " + strings[i + 4]);
            arrayList.add(new OperatorInfo(strings[i + 0], strings[i + 1], strings[i + 2], strings[i + 3]));
            i += 5;
        }
        return arrayList;
    }

    private Object responseCellList(Parcel p) {
        int num = p.readInt();
        ArrayList<NeighboringCellInfo> response = new ArrayList();
        int radioType = SystemProperties.getInt("gsm.enbr.rat", 1);
        riljLog("gsm.enbr.rat=" + radioType);
        if (radioType != 0) {
            for (int i = 0; i < num; i++) {
                response.add(new NeighboringCellInfo(p.readInt(), p.readString(), radioType));
            }
        }
        return response;
    }

    private Object responseSetPreferredNetworkType(Parcel p) {
        int count = getRequestCount(73);
        if (count == 0) {
            Intent intent = new Intent("android.intent.action.ACTION_RAT_CHANGED");
            intent.putExtra("phone", this.mInstanceId);
            intent.putExtra("rat", this.mPreferredNetworkType);
            this.mContext.sendBroadcast(intent);
        }
        riljLog("SetRatRequestCount: " + count);
        return null;
    }

    private Object responseGetPreferredNetworkType(Parcel p) {
        int[] response = (int[]) responseInts(p);
        if (response.length >= 1) {
        }
        return response;
    }

    private int getRequestCount(int reuestId) {
        int count = 0;
        synchronized (this.mRequestList) {
            int s = this.mRequestList.size();
            for (int i = 0; i < s; i++) {
                RILRequest rr = (RILRequest) this.mRequestList.valueAt(i);
                if (rr != null && rr.mRequest == reuestId) {
                    count++;
                }
            }
        }
        return count;
    }

    private Object responseGmsBroadcastConfig(Parcel p) {
        int num = p.readInt();
        ArrayList<SmsBroadcastConfigInfo> response = new ArrayList(num);
        for (int i = 0; i < num; i++) {
            response.add(new SmsBroadcastConfigInfo(p.readInt(), p.readInt(), p.readInt(), p.readInt(), p.readInt() == 1));
        }
        return response;
    }

    private Object responseCdmaBroadcastConfig(Parcel p) {
        int[] response;
        int numServiceCategories = p.readInt();
        int i;
        if (numServiceCategories == 0) {
            response = new int[94];
            response[0] = 31;
            for (i = 1; i < 94; i += 3) {
                response[i + 0] = i / 3;
                response[i + 1] = 1;
                response[i + 2] = 0;
            }
        } else {
            int numInts = (numServiceCategories * 3) + 1;
            response = new int[numInts];
            response[0] = numServiceCategories;
            for (i = 1; i < numInts; i++) {
                response[i] = p.readInt();
            }
        }
        return response;
    }

    private Object responseSignalStrength(Parcel p) {
        return SignalStrength.makeSignalStrengthFromRilParcel(p);
    }

    private ArrayList<CdmaInformationRecords> responseCdmaInformationRecord(Parcel p) {
        int numberOfInfoRecs = p.readInt();
        ArrayList<CdmaInformationRecords> response = new ArrayList(numberOfInfoRecs);
        for (int i = 0; i < numberOfInfoRecs; i++) {
            response.add(new CdmaInformationRecords(p));
        }
        return response;
    }

    private Object responseCdmaCallWaiting(Parcel p) {
        CdmaCallWaitingNotification notification = new CdmaCallWaitingNotification();
        notification.number = p.readString();
        notification.numberPresentation = CdmaCallWaitingNotification.presentationFromCLIP(p.readInt());
        notification.name = p.readString();
        notification.namePresentation = notification.numberPresentation;
        notification.isPresent = p.readInt();
        notification.signalType = p.readInt();
        notification.alertPitch = p.readInt();
        notification.signal = p.readInt();
        notification.numberType = p.readInt();
        notification.numberPlan = p.readInt();
        return notification;
    }

    private Object responseCallRing(Parcel p) {
        char[] response = new char[4];
        response[0] = (char) p.readInt();
        response[1] = (char) p.readInt();
        response[2] = (char) p.readInt();
        response[3] = (char) p.readInt();
        this.mMetrics.writeRilCallRing(this.mInstanceId.intValue(), response);
        return response;
    }

    private Object responseFemtoCellInfos(Parcel p) {
        String[] strings = (String[]) responseStrings(p);
        if (strings.length % 6 != 0) {
            throw new RuntimeException("RIL_REQUEST_GET_FEMTOCELL_LIST: invalid response. Got " + strings.length + " strings, expected multible of 6");
        }
        ArrayList<FemtoCellInfo> ret = new ArrayList(strings.length / 6);
        int i = 0;
        while (i < strings.length) {
            String actStr;
            int rat;
            if (strings[i + 1] != null && strings[i + 1].startsWith("uCs2")) {
                Rlog.d(RILJ_LOG_TAG, "responseOperatorInfos handling UCS2 format name");
                try {
                    strings[i + 0] = new String(IccUtils.hexStringToBytes(strings[i + 1].substring(4)), "UTF-16");
                } catch (UnsupportedEncodingException e) {
                    Rlog.d(RILJ_LOG_TAG, "responseOperatorInfos UnsupportedEncodingException");
                }
            }
            if (strings[i + 1] != null && (strings[i + 1].equals(UsimPBMemInfo.STRING_NOT_SET) || strings[i + 1].equals(strings[i + 0]))) {
                Rlog.d(RILJ_LOG_TAG, "lookup RIL responseFemtoCellInfos() for plmn id= " + strings[i + 0]);
            }
            if (strings[i + 2].equals(Phone.ACT_TYPE_LTE)) {
                actStr = Phone.LTE_INDICATOR;
                rat = 14;
            } else if (strings[i + 2].equals("2")) {
                actStr = Phone.UTRAN_INDICATOR;
                rat = 3;
            } else {
                actStr = "2G";
                rat = 1;
            }
            String property_name = "gsm.baseband.capability";
            if (this.mInstanceId.intValue() > 0) {
                property_name = property_name + (this.mInstanceId.intValue() + 1);
            }
            int basebandCapability = SystemProperties.getInt(property_name, 3);
            Rlog.d(RILJ_LOG_TAG, "property_name=" + property_name + ",basebandCapability=" + basebandCapability);
            if (3 < basebandCapability) {
                strings[i + 1] = strings[i + 1].concat(" " + actStr);
            }
            String hnbName = new String(IccUtils.hexStringToBytes(strings[i + 5]));
            Rlog.d(RILJ_LOG_TAG, "FemtoCellInfo(" + strings[i + 3] + "," + strings[i + 4] + "," + strings[i + 5] + "," + strings[i + 0] + "," + strings[i + 1] + "," + rat + ")" + "hnbName=" + hnbName);
            ret.add(new FemtoCellInfo(Integer.parseInt(strings[i + 3]), Integer.parseInt(strings[i + 4]), hnbName, strings[i + 0], strings[i + 1], rat));
            i += 6;
        }
        return ret;
    }

    private void notifyRegistrantsCdmaInfoRec(CdmaInformationRecords infoRec) {
        if (infoRec.record instanceof CdmaDisplayInfoRec) {
            if (this.mDisplayInfoRegistrants != null) {
                unsljLogRet(1027, infoRec.record);
                this.mDisplayInfoRegistrants.notifyRegistrants(new AsyncResult(null, infoRec.record, null));
            }
        } else if (infoRec.record instanceof CdmaSignalInfoRec) {
            if (this.mSignalInfoRegistrants != null) {
                unsljLogRet(1027, infoRec.record);
                this.mSignalInfoRegistrants.notifyRegistrants(new AsyncResult(null, infoRec.record, null));
            }
        } else if (infoRec.record instanceof CdmaNumberInfoRec) {
            if (this.mNumberInfoRegistrants != null) {
                unsljLogRet(1027, infoRec.record);
                this.mNumberInfoRegistrants.notifyRegistrants(new AsyncResult(null, infoRec.record, null));
            }
        } else if (infoRec.record instanceof CdmaRedirectingNumberInfoRec) {
            if (this.mRedirNumInfoRegistrants != null) {
                unsljLogRet(1027, infoRec.record);
                this.mRedirNumInfoRegistrants.notifyRegistrants(new AsyncResult(null, infoRec.record, null));
            }
        } else if (infoRec.record instanceof CdmaLineControlInfoRec) {
            if (this.mLineControlInfoRegistrants != null) {
                unsljLogRet(1027, infoRec.record);
                this.mLineControlInfoRegistrants.notifyRegistrants(new AsyncResult(null, infoRec.record, null));
            }
        } else if (infoRec.record instanceof CdmaT53ClirInfoRec) {
            if (this.mT53ClirInfoRegistrants != null) {
                unsljLogRet(1027, infoRec.record);
                this.mT53ClirInfoRegistrants.notifyRegistrants(new AsyncResult(null, infoRec.record, null));
            }
        } else if ((infoRec.record instanceof CdmaT53AudioControlInfoRec) && this.mT53AudCntrlInfoRegistrants != null) {
            unsljLogRet(1027, infoRec.record);
            this.mT53AudCntrlInfoRegistrants.notifyRegistrants(new AsyncResult(null, infoRec.record, null));
        }
    }

    private ArrayList<CellInfo> responseCellInfoList(Parcel p) {
        int numberOfInfoRecs = p.readInt();
        ArrayList<CellInfo> response = new ArrayList(numberOfInfoRecs);
        for (int i = 0; i < numberOfInfoRecs; i++) {
            response.add((CellInfo) CellInfo.CREATOR.createFromParcel(p));
        }
        return response;
    }

    private Object responseHardwareConfig(Parcel p) {
        int num = p.readInt();
        ArrayList<HardwareConfig> response = new ArrayList(num);
        for (int i = 0; i < num; i++) {
            HardwareConfig hw;
            int type = p.readInt();
            switch (type) {
                case 0:
                    hw = new HardwareConfig(type);
                    hw.assignModem(p.readString(), p.readInt(), p.readInt(), p.readInt(), p.readInt(), p.readInt(), p.readInt());
                    break;
                case 1:
                    hw = new HardwareConfig(type);
                    hw.assignSim(p.readString(), p.readInt(), p.readString());
                    break;
                default:
                    throw new RuntimeException("RIL_REQUEST_GET_HARDWARE_CONFIG invalid hardward type:" + type);
            }
            response.add(hw);
        }
        return response;
    }

    private Object responseRadioCapability(Parcel p) {
        int version = p.readInt();
        int session = p.readInt();
        int phase = p.readInt();
        int rat = p.readInt();
        String logicModemUuid = p.readString();
        int status = p.readInt();
        riljLog("responseRadioCapability: version= " + version + ", session=" + session + ", phase=" + phase + ", rat=" + rat + ", logicModemUuid=" + logicModemUuid + ", status=" + status);
        return new RadioCapability(this.mInstanceId.intValue(), session, phase, rat, logicModemUuid, status);
    }

    private Object responseLceData(Parcel p) {
        ArrayList<Integer> capacityResponse = new ArrayList();
        int capacityDownKbps = p.readInt();
        int confidenceLevel = p.readByte();
        int lceSuspended = p.readByte();
        riljLog("LCE capacity information received: capacity=" + capacityDownKbps + " confidence=" + confidenceLevel + " lceSuspended=" + lceSuspended);
        capacityResponse.add(Integer.valueOf(capacityDownKbps));
        capacityResponse.add(Integer.valueOf(confidenceLevel));
        capacityResponse.add(Integer.valueOf(lceSuspended));
        return capacityResponse;
    }

    private Object responseLceStatus(Parcel p) {
        ArrayList<Integer> statusResponse = new ArrayList();
        int lceStatus = p.readByte();
        int actualInterval = p.readInt();
        riljLog("LCE status information received: lceStatus=" + lceStatus + " actualInterval=" + actualInterval);
        statusResponse.add(Integer.valueOf(lceStatus));
        statusResponse.add(Integer.valueOf(actualInterval));
        return statusResponse;
    }

    private Object responseActivityData(Parcel p) {
        int sleepModeTimeMs = p.readInt();
        int idleModeTimeMs = p.readInt();
        int[] txModeTimeMs = new int[5];
        for (int i = 0; i < 5; i++) {
            txModeTimeMs[i] = p.readInt();
        }
        int rxModeTimeMs = p.readInt();
        riljLog("Modem activity info received: sleepModeTimeMs=" + sleepModeTimeMs + " idleModeTimeMs=" + idleModeTimeMs + " txModeTimeMs[]=" + Arrays.toString(txModeTimeMs) + " rxModeTimeMs=" + rxModeTimeMs);
        return new ModemActivityInfo(SystemClock.elapsedRealtime(), sleepModeTimeMs, idleModeTimeMs, txModeTimeMs, rxModeTimeMs, 0);
    }

    private Object responseCarrierIdentifiers(Parcel p) {
        List<CarrierIdentifier> retVal = new ArrayList();
        int len_allowed_carriers = p.readInt();
        int len_excluded_carriers = p.readInt();
        for (int i = 0; i < len_allowed_carriers; i++) {
            String mcc = p.readString();
            String mnc = p.readString();
            String spn = null;
            String imsi = null;
            String gid1 = null;
            String gid2 = null;
            int matchType = p.readInt();
            String matchData = p.readString();
            if (matchType == 1) {
                spn = matchData;
            } else if (matchType == 2) {
                imsi = matchData;
            } else if (matchType == 3) {
                gid1 = matchData;
            } else if (matchType == 4) {
                gid2 = matchData;
            }
            retVal.add(new CarrierIdentifier(mcc, mnc, spn, imsi, gid1, gid2));
        }
        return retVal;
    }

    private Object responsePcoData(Parcel p) {
        return new PcoData(p);
    }

    static String requestToString(int request) {
        OemConstant.printStack("requestToString");
        switch (request) {
            case 1:
                return "GET_SIM_STATUS";
            case 2:
                return "ENTER_SIM_PIN";
            case 3:
                return "ENTER_SIM_PUK";
            case 4:
                return "ENTER_SIM_PIN2";
            case 5:
                return "ENTER_SIM_PUK2";
            case 6:
                return "CHANGE_SIM_PIN";
            case 7:
                return "CHANGE_SIM_PIN2";
            case 8:
                return "ENTER_NETWORK_DEPERSONALIZATION";
            case 9:
                return "GET_CURRENT_CALLS";
            case 10:
                return "DIAL";
            case 11:
                return "GET_IMSI";
            case 12:
                return "HANGUP";
            case 13:
                return "HANGUP_WAITING_OR_BACKGROUND";
            case 14:
                return "HANGUP_FOREGROUND_RESUME_BACKGROUND";
            case 15:
                return "REQUEST_SWITCH_WAITING_OR_HOLDING_AND_ACTIVE";
            case 16:
                return "CONFERENCE";
            case 17:
                return "UDUB";
            case 18:
                return "LAST_CALL_FAIL_CAUSE";
            case 19:
                return "SIGNAL_STRENGTH";
            case 20:
                return "VOICE_REGISTRATION_STATE";
            case 21:
                return "DATA_REGISTRATION_STATE";
            case 22:
                return "OPERATOR";
            case 23:
                return "RADIO_POWER";
            case 24:
                return "DTMF";
            case 25:
                return "SEND_SMS";
            case 26:
                return "SEND_SMS_EXPECT_MORE";
            case 27:
                return "SETUP_DATA_CALL";
            case 28:
                return "SIM_IO";
            case 29:
                return "SEND_USSD";
            case 30:
                return "CANCEL_USSD";
            case 31:
                return "GET_CLIR";
            case 32:
                return "SET_CLIR";
            case 33:
                return "QUERY_CALL_FORWARD_STATUS";
            case 34:
                return "SET_CALL_FORWARD";
            case 35:
                return "QUERY_CALL_WAITING";
            case 36:
                return "SET_CALL_WAITING";
            case 37:
                return "SMS_ACKNOWLEDGE";
            case 38:
                return "GET_IMEI";
            case 39:
                return "GET_IMEISV";
            case 40:
                return "ANSWER";
            case 41:
                return "DEACTIVATE_DATA_CALL";
            case 42:
                return "QUERY_FACILITY_LOCK";
            case 43:
                return "SET_FACILITY_LOCK";
            case 44:
                return "CHANGE_BARRING_PASSWORD";
            case RilDataCallFailCause.PDP_FAIL_FILTER_SYTAX_ERROR /*45*/:
                return "QUERY_NETWORK_SELECTION_MODE";
            case 46:
                return "SET_NETWORK_SELECTION_AUTOMATIC";
            case 47:
                return "SET_NETWORK_SELECTION_MANUAL";
            case 48:
                return "QUERY_AVAILABLE_NETWORKS ";
            case 49:
                return "DTMF_START";
            case 50:
                return "DTMF_STOP";
            case 51:
                return "BASEBAND_VERSION";
            case 52:
                return "SEPARATE_CONNECTION";
            case 53:
                return "SET_MUTE";
            case 54:
                return "GET_MUTE";
            case 55:
                return "QUERY_CLIP";
            case 56:
                return "LAST_DATA_CALL_FAIL_CAUSE";
            case 57:
                return "DATA_CALL_LIST";
            case 58:
                return "RESET_RADIO";
            case RadioNVItems.RIL_NV_CDMA_EHRPD_FORCED /*59*/:
                return "OEM_HOOK_RAW";
            case 60:
                return "OEM_HOOK_STRINGS";
            case 61:
                return "SCREEN_STATE";
            case 62:
                return "SET_SUPP_SVC_NOTIFICATION";
            case 63:
                return "WRITE_SMS_TO_SIM";
            case 64:
                return "DELETE_SMS_ON_SIM";
            case 65:
                return "SET_BAND_MODE";
            case 66:
                return "QUERY_AVAILABLE_BAND_MODE";
            case SYS_OEM_NW_DIAG_CAUSE_MCFG_ICCID_FAILED /*67*/:
                return "REQUEST_STK_GET_PROFILE";
            case 68:
                return "REQUEST_STK_SET_PROFILE";
            case CallFailCause.FACILITY_NOT_IMPLEMENT /*69*/:
                return "REQUEST_STK_SEND_ENVELOPE_COMMAND";
            case 70:
                return "REQUEST_STK_SEND_TERMINAL_RESPONSE";
            case 71:
                return "REQUEST_STK_HANDLE_CALL_SETUP_REQUESTED_FROM_SIM";
            case 72:
                return "REQUEST_EXPLICIT_CALL_TRANSFER";
            case 73:
                return "REQUEST_SET_PREFERRED_NETWORK_TYPE";
            case RadioNVItems.RIL_NV_LTE_SCAN_PRIORITY_25 /*74*/:
                return "REQUEST_GET_PREFERRED_NETWORK_TYPE";
            case RadioNVItems.RIL_NV_LTE_SCAN_PRIORITY_26 /*75*/:
                return "REQUEST_GET_NEIGHBORING_CELL_IDS";
            case RadioNVItems.RIL_NV_LTE_SCAN_PRIORITY_41 /*76*/:
                return "REQUEST_SET_LOCATION_UPDATES";
            case RadioNVItems.RIL_NV_LTE_HIDDEN_BAND_PRIORITY_25 /*77*/:
                return "RIL_REQUEST_CDMA_SET_SUBSCRIPTION_SOURCE";
            case RadioNVItems.RIL_NV_LTE_HIDDEN_BAND_PRIORITY_26 /*78*/:
                return "RIL_REQUEST_CDMA_SET_ROAMING_PREFERENCE";
            case 79:
                return "RIL_REQUEST_CDMA_QUERY_ROAMING_PREFERENCE";
            case RadioNVItems.RIL_NV_LTE_NEXT_SCAN /*80*/:
                return "RIL_REQUEST_SET_TTY_MODE";
            case 81:
                return "RIL_REQUEST_QUERY_TTY_MODE";
            case RadioNVItems.RIL_NV_LTE_BSR_MAX_TIME /*82*/:
                return "RIL_REQUEST_CDMA_SET_PREFERRED_VOICE_PRIVACY_MODE";
            case 83:
                return "RIL_REQUEST_CDMA_QUERY_PREFERRED_VOICE_PRIVACY_MODE";
            case 84:
                return "RIL_REQUEST_CDMA_FLASH";
            case 85:
                return "RIL_REQUEST_CDMA_BURST_DTMF";
            case 86:
                return "RIL_REQUEST_CDMA_VALIDATE_AND_WRITE_AKEY";
            case 87:
                return "RIL_REQUEST_CDMA_SEND_SMS";
            case CallFailCause.INCOMPATIBLE_DESTINATION /*88*/:
                return "RIL_REQUEST_CDMA_SMS_ACKNOWLEDGE";
            case SYS_MTK_URC_CARD_DROP /*89*/:
                return "RIL_REQUEST_GSM_GET_BROADCAST_CONFIG";
            case 90:
                return "RIL_REQUEST_GSM_SET_BROADCAST_CONFIG";
            case CallFailCause.INVALID_TRANSIT_NETWORK_SELECTION /*91*/:
                return "RIL_REQUEST_GSM_BROADCAST_ACTIVATION";
            case 92:
                return "RIL_REQUEST_CDMA_GET_BROADCAST_CONFIG";
            case 93:
                return "RIL_REQUEST_CDMA_SET_BROADCAST_CONFIG";
            case 94:
                return "RIL_REQUEST_CDMA_BROADCAST_ACTIVATION";
            case 95:
                return "RIL_REQUEST_CDMA_SUBSCRIPTION";
            case 96:
                return "RIL_REQUEST_CDMA_WRITE_SMS_TO_RUIM";
            case 97:
                return "RIL_REQUEST_CDMA_DELETE_SMS_ON_RUIM";
            case 98:
                return "RIL_REQUEST_DEVICE_IDENTITY";
            case 99:
                return "REQUEST_EXIT_EMERGENCY_CALLBACK_MODE";
            case 100:
                return "RIL_REQUEST_GET_SMSC_ADDRESS";
            case 101:
                return "RIL_REQUEST_SET_SMSC_ADDRESS";
            case 102:
                return "RIL_REQUEST_REPORT_SMS_MEMORY_STATUS";
            case Phone.OEM_PRODUCT_16391 /*103*/:
                return "RIL_REQUEST_REPORT_STK_SERVICE_IS_RUNNING";
            case 104:
                return "RIL_REQUEST_CDMA_GET_SUBSCRIPTION_SOURCE";
            case 105:
                return "RIL_REQUEST_ISIM_AUTHENTICATION";
            case 106:
                return "RIL_REQUEST_ACKNOWLEDGE_INCOMING_GSM_SMS_WITH_PDU";
            case Phone.OEM_PRODUCT_17373 /*107*/:
                return "RIL_REQUEST_STK_SEND_ENVELOPE_WITH_STATUS";
            case 108:
                return "RIL_REQUEST_VOICE_RADIO_TECH";
            case 109:
                return "RIL_REQUEST_GET_CELL_INFO_LIST";
            case 110:
                return "RIL_REQUEST_SET_CELL_INFO_LIST_RATE";
            case 111:
                return "RIL_REQUEST_SET_INITIAL_ATTACH_APN";
            case 112:
                return "RIL_REQUEST_IMS_REGISTRATION_STATE";
            case 113:
                return "RIL_REQUEST_IMS_SEND_SMS";
            case 114:
                return "RIL_REQUEST_SIM_TRANSMIT_APDU_BASIC";
            case RilDataCallFailCause.PDP_FAIL_EMM_ACCESS_BARRED /*115*/:
                return "RIL_REQUEST_SIM_OPEN_CHANNEL";
            case RilDataCallFailCause.PDP_FAIL_EMERGENCY_IFACE_ONLY /*116*/:
                return "RIL_REQUEST_SIM_CLOSE_CHANNEL";
            case RilDataCallFailCause.PDP_FAIL_IFACE_MISMATCH /*117*/:
                return "RIL_REQUEST_SIM_TRANSMIT_APDU_CHANNEL";
            case RilDataCallFailCause.PDP_FAIL_COMPANION_IFACE_IN_USE /*118*/:
                return "RIL_REQUEST_NV_READ_ITEM";
            case RilDataCallFailCause.PDP_FAIL_IP_ADDRESS_MISMATCH /*119*/:
                return "RIL_REQUEST_NV_WRITE_ITEM";
            case RilDataCallFailCause.PDP_FAIL_IFACE_AND_POL_FAMILY_MISMATCH /*120*/:
                return "RIL_REQUEST_NV_WRITE_CDMA_PRL";
            case RilDataCallFailCause.PDP_FAIL_EMM_ACCESS_BARRED_INFINITE_RETRY /*121*/:
                return "RIL_REQUEST_NV_RESET_CONFIG";
            case RilDataCallFailCause.PDP_FAIL_AUTH_FAILURE_ON_EMERGENCY_CALL /*122*/:
                return "RIL_REQUEST_SET_UICC_SUBSCRIPTION";
            case 123:
                return "RIL_REQUEST_ALLOW_DATA";
            case 124:
                return "GET_HARDWARE_CONFIG";
            case 125:
                return "RIL_REQUEST_SIM_AUTHENTICATION";
            case 128:
                return "RIL_REQUEST_SET_DATA_PROFILE";
            case 129:
                return "RIL_REQUEST_SHUTDOWN";
            case 130:
                return "RIL_REQUEST_GET_RADIO_CAPABILITY";
            case 131:
                return "RIL_REQUEST_SET_RADIO_CAPABILITY";
            case 132:
                return "RIL_REQUEST_START_LCE";
            case 133:
                return "RIL_REQUEST_STOP_LCE";
            case 134:
                return "RIL_REQUEST_PULL_LCEDATA";
            case 135:
                return "RIL_REQUEST_GET_ACTIVITY_INFO";
            case 136:
                return "RIL_REQUEST_SET_ALLOWED_CARRIERS";
            case 137:
                return "RIL_REQUEST_GET_ALLOWED_CARRIERS";
            case 800:
                return "RIL_RESPONSE_ACKNOWLEDGEMENT";
            case 2000:
                return "GET_COLP";
            case 2001:
                return "SET_COLP";
            case 2002:
                return "GET_COLR";
            case 2010:
                return "MODEM_POWEROFF";
            case 2012:
                return "RIL_REQUEST_QUERY_PHB_STORAGE_INFO";
            case 2013:
                return "RIL_REQUEST_WRITE_PHB_ENTRY";
            case 2014:
                return "RIL_REQUEST_READ_PHB_ENTRY";
            case 2016:
                return "QUERY_SIM_NETWORK_LOCK";
            case 2017:
                return "SET_SIM_NETWORK_LOCK";
            case 2018:
                return "SET_NETWORK_SELECTION_MANUAL_WITH_ACT";
            case 2020:
                return "MODEM_POWERON";
            case 2021:
                return "RIL_REQUEST_GET_SMS_SIM_MEM_STATUS";
            case 2022:
                return "RIL_REQUEST_GET_POL_CAPABILITY";
            case 2023:
                return "RIL_REQUEST_GET_POL_LIST";
            case 2024:
                return "RIL_REQUEST_SET_POL_ENTRY";
            case CharacterSets.GB_2312 /*2025*/:
                return "RIL_REQUEST_QUERY_UPB_CAPABILITY";
            case CharacterSets.BIG5 /*2026*/:
                return "RIL_REQUEST_EDIT_UPB_ENTRY";
            case CharacterSets.MACINTOSH /*2027*/:
                return "RIL_REQUEST_DELETE_UPB_ENTRY";
            case 2028:
                return "RIL_REQUEST_READ_UPB_GAS_LIST";
            case 2029:
                return "RIL_REQUEST_READ_UPB_GRP";
            case 2030:
                return "RIL_REQUEST_WRITE_UPB_GRP";
            case 2031:
                return "RIL_REQUEST_SET_TRM";
            case 2033:
                return "RIL_REQUEST_GET_PHB_STRING_LENGTH";
            case 2034:
                return "RIL_REQUEST_GET_PHB_MEM_STORAGE";
            case 2035:
                return "RIL_REQUEST_SET_PHB_MEM_STORAGE";
            case 2036:
                return "RIL_REQUEST_READ_PHB_ENTRY_EXT";
            case 2037:
                return "RIL_REQUEST_WRITE_PHB_ENTRY_EXT";
            case 2038:
                return "RIL_REQUEST_GET_SMS_PARAMS";
            case 2039:
                return "RIL_REQUEST_SET_SMS_PARAMS";
            case 2042:
                return "SIM_GET_ATR";
            case 2043:
                return "RIL_REQUEST_SET_CB_CHANNEL_CONFIG_INFO";
            case 2044:
                return "RIL_REQUEST_SET_CB_LANGUAGE_CONFIG_INFO";
            case 2045:
                return "RIL_REQUEST_GET_CB_CONFIG_INFO";
            case 2047:
                return "RIL_REQUEST_SET_ETWS";
            case 2048:
                return "RIL_REQUEST_SET_FD_MODE";
            case 2050:
                return "RIL_REQUEST_RESUME_REGISTRATION";
            case CharacterSets.CP864 /*2051*/:
                return "RIL_REQUEST_STORE_MODEM_TYPE";
            case 2058:
                return "RIL_REQUEST_STK_EVDL_CALL_BY_AP";
            case 2059:
                return "RIL_REQUEST_GET_FEMTOCELL_LIST";
            case 2060:
                return "RIL_REQUEST_ABORT_FEMTOCELL_LIST";
            case 2061:
                return "RIL_REQUEST_SELECT_FEMTOCELL";
            case 2062:
                return "ABORT_QUERY_AVAILABLE_NETWORKS";
            case 2063:
                return "HANGUP_ALL";
            case 2065:
                return "SET_CALL_INDICATION";
            case 2066:
                return "EMERGENCY_DIAL";
            case 2067:
                return "SET_ECC_SERVICE_CATEGORY";
            case 2068:
                return "SET_ECC_LIST";
            case 2069:
                return "RIL_REQUEST_GENERAL_SIM_AUTH";
            case 2070:
                return "RIL_REQUEST_OPEN_ICC_APPLICATION";
            case 2071:
                return "RIL_REQUEST_GET_ICC_APPLICATION_STATUS";
            case 2072:
                return "SIM_IO_EX";
            case 2073:
                return "RIL_REQUEST_SET_IMS_ENABLE";
            case 2074:
                return "QUERY_AVAILABLE_NETWORKS_WITH_ACT";
            case 2075:
                return "SEND_CNAP";
            case 2076:
                return "RIL_REQUEST_SET_CLIP";
            case 2083:
                return "RIL_REQUEST_REMOVE_CB_MESSAGE";
            case CharacterSets.KOI8_R /*2084*/:
                return "RIL_REQUEST_SET_DATA_CENTRIC";
            case CharacterSets.HZ_GB_2312 /*2085*/:
                return "RIL_REQUEST_ADD_IMS_CONFERENCE_CALL_MEMBER";
            case 2086:
                return "RIL_REQUEST_REMOVE_IMS_CONFERENCE_CALL_MEMBER";
            case 2087:
                return "RIL_REQUEST_DIAL_WITH_SIP_URI";
            case CharacterSets.KOI8_U /*2088*/:
                return "RIL_REQUEST_RESUNME_CALL";
            case 2089:
                return "SET_SPEECH_CODEC_INFO";
            case 2090:
                return "RIL_REQUEST_SET_DATA_ON_TO_MD";
            case 2091:
                return "RIL_REQUEST_SET_REMOVE_RESTRICT_EUTRAN_MODE";
            case 2092:
                return "RIL_REQUEST_SET_IMS_CALL_STATUS";
            case 2093:
                return "RIL_REQUEST_VT_DIAL";
            case 2094:
                return "VOICE_ACCEPT";
            case 2095:
                return "RIL_REQUEST_REPLACE_VT_CALL";
            case 2100:
                return "RIL_REQUEST_CONFERENCE_DIAL";
            case CharacterSets.BIG5_HKSCS /*2101*/:
                return "RIL_REQUEST_SET_SRVCC_CALL_CONTEXT_TRANSFER";
            case 2102:
                return "RIL_REQUEST_UPDATE_IMS_REGISTRATION_STATUS";
            case 2103:
                return "RIL_REQUEST_RELOAD_MODEM_TYPE";
            case 2104:
                return "RIL_REQUEST_HOLD_CALL";
            case 2108:
                return "RIL_REQUEST_ENABLE_MD3_SLEEP";
            case 2110:
                return "RIL_REQUEST_SET_LTE_ACCESS_STRATUM_REPORT";
            case 2111:
                return "RIL_REQUEST_SET_LTE_UPLINK_DATA_TRANSFER";
            case 2131:
                return "RIL_REQUEST_VT_DIAL_WITH_SIP_URI";
            case 2134:
                return "RIL_REQUEST_SYNC_APN_TABLE";
            case 2142:
                return "RIL_REQUEST_SWITCH_MODE_FOR_ECC";
            case 2143:
                return "RIL_REQUEST_QUERY_UPB_AVAILABLE";
            case 2144:
                return "RIL_REQUEST_READ_EMAIL_ENTRY";
            case 2145:
                return "RIL_REQUEST_READ_SNE_ENTRY";
            case 2146:
                return "RIL_REQUEST_READ_ANR_ENTRY";
            case 2147:
                return "RIL_REQUEST_READ_UPB_AAS_LIST";
            case 2148:
                return "RIL_REQUEST_SET_STK_UTK_MODE";
            case 2151:
                return "RIL_REQUEST_SET_PS_REGISTRATION";
            case 2152:
                return "RIL_REQUEST_SYNC_DATA_SETTINGS_TO_MD";
            case 2153:
                return "RIL_REQUEST_SET_PSEUDO_BS_ENABLE";
            case 2154:
                return "RIL_REQUEST_GET_PSEUDO_BS_RECORDS";
            case 2155:
                return "RIL_REQUEST_SYNC_APN_TABLE_TO_RDS";
            case 2157:
                return "RIL_REQUEST_QUERY_FEMTOCELL_SYSTEM_SELECTION_MODE";
            case 2158:
                return "RIL_REQUEST_SET_FEMTOCELL_SYSTEM_SELECTION_MODE";
            case 2159:
                return "RIL_REQUEST_VSS_ANTENNA_CONF";
            case 2160:
                return "RIL_REQUEST_VSS_ANTENNA_INFO";
            case 2161:
                return "RIL_REQUEST_CURRENT_STATUS";
            case 2162:
                return "RIL_REQUEST_GET_SMS_RUIM_MEM_STATUS";
            case 2163:
                return "GSM_SET_BROADCAST_LANGUAGE";
            case 2164:
                return "GSM_GET_BROADCAST_LANGUAGE";
            case 2170:
                return "RIL_REQUEST_GSM_SET_BROADCAST_CONFIG_EX";
            case 2171:
                return "RIL_REQUEST_GSM_GET_BROADCAST_CONFIG_EX";
            case 2173:
                return "RIL_REQUEST_SET_CARRIER_RESTRICTION_STATE";
            case 2174:
                return "RIL_REQUEST_GET_CARRIER_RESTRICTION_STATE";
            case 2183:
                return "RIL_REQUEST_RESET_MD_DATA_RETRY_COUNT";
            case 2184:
                return "RIL_REQUEST_ECC_PREFERRED_RAT";
            case 2187:
                return "GET_GSM_SMS_BROADCAST_ACTIVATION";
            case 2189:
                return "QUERY_CALL_FORWARD_IN_TIME_SLOT";
            case 2190:
                return "SET_CALL_FORWARD_IN_TIME_SLOT";
            default:
                return "<unknown request>";
        }
    }

    static String responseToString(int request) {
        switch (request) {
            case 1000:
                return "UNSOL_RESPONSE_RADIO_STATE_CHANGED";
            case 1001:
                return "UNSOL_RESPONSE_CALL_STATE_CHANGED";
            case 1002:
                return "UNSOL_RESPONSE_VOICE_NETWORK_STATE_CHANGED";
            case 1003:
                return "UNSOL_RESPONSE_NEW_SMS";
            case 1004:
                return "UNSOL_RESPONSE_NEW_SMS_STATUS_REPORT";
            case 1005:
                return "UNSOL_RESPONSE_NEW_SMS_ON_SIM";
            case 1006:
                return "UNSOL_ON_USSD";
            case 1007:
                return "UNSOL_ON_USSD_REQUEST";
            case 1008:
                return "UNSOL_NITZ_TIME_RECEIVED";
            case 1009:
                return "UNSOL_SIGNAL_STRENGTH";
            case GsmVTProvider.SESSION_EVENT_START_COUNTER /*1010*/:
                return "UNSOL_DATA_CALL_LIST_CHANGED";
            case 1011:
                return "UNSOL_SUPP_SVC_NOTIFICATION";
            case 1012:
                return "UNSOL_STK_SESSION_END";
            case CharacterSets.UTF_16BE /*1013*/:
                return "UNSOL_STK_PROACTIVE_COMMAND";
            case CharacterSets.UTF_16LE /*1014*/:
                return "UNSOL_STK_EVENT_NOTIFY";
            case CharacterSets.UTF_16 /*1015*/:
                return "UNSOL_STK_CALL_SETUP";
            case CharacterSets.CESU_8 /*1016*/:
                return "UNSOL_SIM_SMS_STORAGE_FULL";
            case CharacterSets.UTF_32 /*1017*/:
                return "UNSOL_SIM_REFRESH";
            case CharacterSets.UTF_32BE /*1018*/:
                return "UNSOL_CALL_RING";
            case CharacterSets.UTF_32LE /*1019*/:
                return "UNSOL_RESPONSE_SIM_STATUS_CHANGED";
            case CharacterSets.BOCU_1 /*1020*/:
                return "UNSOL_RESPONSE_CDMA_NEW_SMS";
            case 1021:
                return "UNSOL_RESPONSE_NEW_BROADCAST_SMS";
            case 1022:
                return "UNSOL_CDMA_RUIM_SMS_STORAGE_FULL";
            case 1023:
                return "UNSOL_RESTRICTED_STATE_CHANGED";
            case 1024:
                return "UNSOL_ENTER_EMERGENCY_CALLBACK_MODE";
            case 1025:
                return "UNSOL_CDMA_CALL_WAITING";
            case 1026:
                return "UNSOL_CDMA_OTA_PROVISION_STATUS";
            case 1027:
                return "UNSOL_CDMA_INFO_REC";
            case 1028:
                return "UNSOL_OEM_HOOK_RAW";
            case 1029:
                return "UNSOL_RINGBACK_TONE";
            case 1030:
                return "UNSOL_RESEND_INCALL_MUTE";
            case 1031:
                return "CDMA_SUBSCRIPTION_SOURCE_CHANGED";
            case 1032:
                return "UNSOL_CDMA_PRL_CHANGED";
            case 1033:
                return "UNSOL_EXIT_EMERGENCY_CALLBACK_MODE";
            case 1034:
                return "UNSOL_RIL_CONNECTED";
            case 1035:
                return "UNSOL_VOICE_RADIO_TECH_CHANGED";
            case 1036:
                return "UNSOL_CELL_INFO_LIST";
            case 1037:
                return "UNSOL_RESPONSE_IMS_NETWORK_STATE_CHANGED";
            case 1038:
                return "RIL_UNSOL_UICC_SUBSCRIPTION_STATUS_CHANGED";
            case 1039:
                return "UNSOL_SRVCC_STATE_NOTIFY";
            case 1040:
                return "RIL_UNSOL_HARDWARE_CONFIG_CHANGED";
            case 1042:
                return "RIL_UNSOL_RADIO_CAPABILITY";
            case 1043:
                return "UNSOL_ON_SS";
            case 1044:
                return "UNSOL_STK_CC_ALPHA_NOTIFY";
            case 1045:
                return "UNSOL_LCE_INFO_RECV";
            case 1046:
                return "UNSOL_PCO_DATA";
            case 2159:
                return "RIL_REQUEST_VSS_ANTENNA_CONF";
            case 2160:
                return "RIL_REQUEST_VSS_ANTENNA_INFO";
            case 3000:
                return "UNSOL_NEIGHBORING_CELL_INFO";
            case 3001:
                return "UNSOL_NETWORK_INFO";
            case 3002:
                return "UNSOL_PHB_READY_NOTIFICATION";
            case 3004:
                return "RIL_UNSOL_ME_SMS_STORAGE_FULL";
            case 3005:
                return "RIL_UNSOL_SMS_READY_NOTIFICATION";
            case 3006:
                return "UNSOL_SIM_MISSING";
            case 3008:
                return "UNSOL_SIM_RECOVERY";
            case 3009:
                return "UNSOL_VIRTUAL_SIM_ON";
            case 3010:
                return "UNSOL_VIRTUAL_SIM_ON_OFF";
            case 3011:
                return "RIL_UNSOL_INVALID_SIM";
            case 3012:
                return "UNSOL_RESPONSE_PS_NETWORK_STATE_CHANGED";
            case 3013:
                return "UNSOL_ACMT_INFO";
            case 3015:
                return "UNSOL_IMEI_LOCK";
            case 3016:
                return "UNSOL_RESPONSE_MMRR_STATUS_CHANGED";
            case 3017:
                return "UNSOL_SIM_PLUG_OUT";
            case 3018:
                return "UNSOL_SIM_PLUG_IN";
            case 3019:
                return "RIL_UNSOL_RESPONSE_ETWS_NOTIFICATION";
            case 3020:
                return "RIL_UNSOL_RESPONSE_PLMN_CHANGED";
            case 3021:
                return "RIL_UNSOL_RESPONSE_REGISTRATION_SUSPENDED";
            case 3022:
                return "RIL_UNSOL_STK_EVDL_CALL";
            case 3023:
                return "RIL_UNSOL_FEMTOCELL_INFO";
            case 3024:
                return "RIL_UNSOL_STK_SETUP_MENU_RESET";
            case 3025:
                return "RIL_UNSOL_APPLICATION_SESSION_ID_CHANGED";
            case 3026:
                return "RIL_UNSOL_ECONF_SRVCC_INDICATION";
            case 3027:
                return "RIL_UNSOL_IMS_ENABLE_DONE";
            case 3028:
                return "RIL_UNSOL_IMS_DISABLE_DONE";
            case 3029:
                return "RIL_UNSOL_IMS_REGISTRATION_INFO";
            case 3033:
                return "RIL_UNSOL_ECONF_RESULT_INDICATION";
            case 3034:
                return "RIL_UNSOL_MELOCK_NOTIFICATION";
            case 3035:
                return "UNSOL_CALL_FORWARDING";
            case 3036:
                return "UNSOL_CRSS_NOTIFICATION";
            case 3037:
                return "UNSOL_INCOMING_CALL_INDICATION";
            case 3038:
                return "UNSOL_CIPHER_INDICATION";
            case 3039:
                return "RIL_UNSOL_SIM_COMMON_SLOT_NO_CHANGED";
            case 3040:
                return "RIL_UNSOL_DATA_ALLOWED";
            case 3041:
                return "RIL_UNSOL_STK_CALL_CTRL";
            case 3043:
                return "RIL_UNSOL_CALL_INFO_INDICATION";
            case 3044:
                return "RIL_UNSOL_VOLTE_EPS_NETWORK_FEATURE_INFO";
            case 3045:
                return "RIL_UNSOL_SRVCC_HANDOVER_INFO_INDICATION";
            case 3046:
                return "UNSOL_SPEECH_CODEC_INFO";
            case 3047:
                return "RIL_UNSOL_MD_STATE_CHANGE";
            case 3048:
                return "RIL_UNSOL_REMOVE_RESTRICT_EUTRAN";
            case 3049:
                return "RIL_UNSOL_SSAC_BARRING_INFO";
            case 3052:
                return "RIL_UNSOL_EMERGENCY_BEARER_SUPPORT_NOTIFY";
            case 3054:
                return "RIL_UNSOL_GMSS_RAT_CHANGED";
            case 3058:
                return "RIL_UNSOL_IMSI_REFRESH_DONE";
            case 3059:
                return "UNSOL_EUSIM_READY";
            case 3060:
                return "UNSOL_STK_BIP_PROACTIVE_COMMAND";
            case 3061:
                return "RIL_UNSOL_WORLD_MODE_CHANGED";
            case 3062:
                return "UNSOL_VT_STATUS_INFO";
            case 3063:
                return "UNSOL_VT_RING_INFO";
            case 3065:
                return "RIL_UNSOL_SET_ATTACH_APN";
            case 3068:
                return "UNSOL_TRAY_PLUG_IN";
            case 3071:
                return "RIL_UNSOL_LTE_ACCESS_STRATUM_STATE_CHANGE";
            case 3074:
                return "RIL_UNSOL_CDMA_CALL_ACCEPTED";
            case 3081:
                return "UNSOL_NETWORK_EXIST";
            case 3082:
                return "RIL_UNSOL_MODULATION_INFO";
            case 3083:
                return "UNSOL_NETWORK_EVENT";
            case 3093:
                return "RIL_UNSOL_EMBMS_SESSION_STATUS";
            case 3095:
                return "RIL_UNSOL_PCO_STATUS";
            case 3096:
                return "RIL_UNSOL_DATA_ATTACH_APN_CHANGED";
            case 3098:
                return "RIL_UNSOL_TRIGGER_OTASP";
            case 3099:
                return "UNSOL_CALL_REDIAL_STATE";
            case 3100:
                return "RIL_UNSOL_CDMA_CARD_INITIAL_ESN_OR_MEID";
            case 3103:
                return "RIL_UNSOL_PSEUDO_BS_INFO_LIST";
            case 3116:
                return "RIL_UNSOL_MD_DATA_RETRY_COUNT_RESET";
            case 3118:
                return "UNSOL_RESPONSE_CS_NETWORK_STATE_CHANGED";
            case 3122:
                return "RIL_UNSOL_TX_POWER";
            default:
                return "<unknown response>";
        }
    }

    private void riljLog(String msg) {
        Rlog.d(RILJ_LOG_TAG, msg + (this.mInstanceId != null ? " [SUB" + this.mInstanceId + "]" : UsimPBMemInfo.STRING_NOT_SET));
    }

    private void riljLogv(String msg) {
        Rlog.v(RILJ_LOG_TAG, msg + (this.mInstanceId != null ? " [SUB" + this.mInstanceId + "]" : UsimPBMemInfo.STRING_NOT_SET));
    }

    private void unsljLog(int response) {
        riljLog("[UNSL]< " + responseToString(response));
    }

    private void unsljLogMore(int response, String more) {
        riljLog("[UNSL]< " + responseToString(response) + " " + more);
    }

    private void unsljLogRet(int response, Object ret) {
        riljLog("[UNSL]< " + responseToString(response) + " " + retToString(response, ret));
    }

    private void unsljLogvRet(int response, Object ret) {
        riljLogv("[UNSL]< " + responseToString(response) + " " + retToString(response, ret));
    }

    private Object responseSsData(Parcel p) {
        SsData ssData = new SsData();
        ssData.serviceType = ssData.ServiceTypeFromRILInt(p.readInt());
        ssData.requestType = ssData.RequestTypeFromRILInt(p.readInt());
        ssData.teleserviceType = ssData.TeleserviceTypeFromRILInt(p.readInt());
        ssData.serviceClass = p.readInt();
        ssData.result = p.readInt();
        int num = p.readInt();
        int i;
        if (ssData.serviceType.isTypeCF() && ssData.requestType.isTypeInterrogation()) {
            ssData.cfInfo = new CallForwardInfo[num];
            for (i = 0; i < num; i++) {
                ssData.cfInfo[i] = new CallForwardInfo();
                ssData.cfInfo[i].status = p.readInt();
                ssData.cfInfo[i].reason = p.readInt();
                ssData.cfInfo[i].serviceClass = p.readInt();
                ssData.cfInfo[i].toa = p.readInt();
                ssData.cfInfo[i].number = p.readString();
                ssData.cfInfo[i].timeSeconds = p.readInt();
                riljLog("[SS Data] CF Info " + i + " : " + ssData.cfInfo[i]);
            }
        } else {
            ssData.ssInfo = new int[num];
            for (i = 0; i < num; i++) {
                ssData.ssInfo[i] = p.readInt();
                riljLog("[SS Data] SS Info " + i + " : " + ssData.ssInfo[i]);
            }
        }
        return ssData;
    }

    public void getDeviceIdentity(Message response) {
        RILRequest rr = RILRequest.obtain(98, response);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    public void getCDMASubscription(Message response) {
        RILRequest rr = RILRequest.obtain(95, response);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    public void setPhoneType(int phoneType) {
        riljLog("setPhoneType=" + phoneType + " old value=" + this.mPhoneType);
        this.mPhoneType = phoneType;
    }

    public void queryCdmaRoamingPreference(Message response) {
        RILRequest rr = RILRequest.obtain(79, response);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    public void setCdmaRoamingPreference(int cdmaRoamingType, Message response) {
        RILRequest rr = RILRequest.obtain(78, response);
        rr.mParcel.writeInt(1);
        rr.mParcel.writeInt(cdmaRoamingType);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " : " + cdmaRoamingType);
        send(rr);
    }

    public void setCdmaSubscriptionSource(int cdmaSubscription, Message response) {
        RILRequest rr = RILRequest.obtain(77, response);
        rr.mParcel.writeInt(1);
        rr.mParcel.writeInt(cdmaSubscription);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " : " + cdmaSubscription);
        send(rr);
    }

    public void getCdmaSubscriptionSource(Message response) {
        RILRequest rr = RILRequest.obtain(104, response);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    public void queryTTYMode(Message response) {
        RILRequest rr = RILRequest.obtain(81, response);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    public void setTTYMode(int ttyMode, Message response) {
        RILRequest rr = RILRequest.obtain(80, response);
        rr.mParcel.writeInt(1);
        rr.mParcel.writeInt(ttyMode);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " : " + ttyMode);
        send(rr);
    }

    public void sendCDMAFeatureCode(String FeatureCode, Message response) {
        RILRequest rr = RILRequest.obtain(84, response);
        rr.mParcel.writeString(FeatureCode);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " : " + FeatureCode);
        send(rr);
    }

    public void getCdmaBroadcastConfig(Message response) {
        send(RILRequest.obtain(92, response));
    }

    public void setCdmaBroadcastConfig(CdmaSmsBroadcastConfigInfo[] configs, Message response) {
        int i;
        int i2;
        RILRequest rr = RILRequest.obtain(93, response);
        ArrayList<CdmaSmsBroadcastConfigInfo> processedConfigs = new ArrayList();
        for (CdmaSmsBroadcastConfigInfo config : configs) {
            for (i2 = config.getFromServiceCategory(); i2 <= config.getToServiceCategory(); i2++) {
                processedConfigs.add(new CdmaSmsBroadcastConfigInfo(i2, i2, config.getLanguage(), config.isSelected()));
            }
        }
        CdmaSmsBroadcastConfigInfo[] rilConfigs = (CdmaSmsBroadcastConfigInfo[]) processedConfigs.toArray(configs);
        rr.mParcel.writeInt(rilConfigs.length);
        for (i2 = 0; i2 < rilConfigs.length; i2++) {
            rr.mParcel.writeInt(rilConfigs[i2].getFromServiceCategory());
            rr.mParcel.writeInt(rilConfigs[i2].getLanguage());
            Parcel parcel = rr.mParcel;
            if (rilConfigs[i2].isSelected()) {
                i = 1;
            } else {
                i = 0;
            }
            parcel.writeInt(i);
        }
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " with " + rilConfigs.length + " configs : ");
        for (CdmaSmsBroadcastConfigInfo cdmaSmsBroadcastConfigInfo : rilConfigs) {
            riljLog(cdmaSmsBroadcastConfigInfo.toString());
        }
        send(rr);
    }

    public void setCdmaBroadcastActivation(boolean activate, Message response) {
        int i = 1;
        RILRequest rr = RILRequest.obtain(94, response);
        rr.mParcel.writeInt(1);
        Parcel parcel = rr.mParcel;
        if (activate) {
            i = 0;
        }
        parcel.writeInt(i);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    public void exitEmergencyCallbackMode(Message response) {
        RILRequest rr = RILRequest.obtain(99, response);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    public void requestIsimAuthentication(String nonce, Message response) {
        RILRequest rr = RILRequest.obtain(105, response);
        if (SystemProperties.get("ro.mtk_tc1_feature").equals("1")) {
            byte[] result = Base64.decode(nonce, 0);
            StringBuilder mStringBuilder = new StringBuilder(result.length * 2);
            for (byte mByte : result) {
                Object[] objArr = new Object[1];
                objArr[0] = Integer.valueOf(mByte & 255);
                mStringBuilder.append(String.format("%02x", objArr));
            }
            nonce = mStringBuilder.toString();
            riljLog("requestIsimAuthentication - nonce = " + nonce);
        }
        rr.mParcel.writeString(nonce);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    public void requestIccSimAuthentication(int authContext, String data, String aid, Message response) {
        RILRequest rr = RILRequest.obtain(125, response);
        rr.mParcel.writeInt(authContext);
        rr.mParcel.writeString(data);
        rr.mParcel.writeString(aid);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    public void getCellInfoList(Message result) {
        RILRequest rr = RILRequest.obtain(109, result);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    public void setCellInfoListRate(int rateInMillis, Message response) {
        riljLog("setCellInfoListRate: " + rateInMillis);
        RILRequest rr = RILRequest.obtain(110, response);
        rr.mParcel.writeInt(1);
        rr.mParcel.writeInt(rateInMillis);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    public void setInitialAttachApn(String apn, String protocol, int authType, String username, String password, Message result) {
        setInitialAttachApn(apn, protocol, authType, username, password, new IaExtendParam(), result);
    }

    public void setInitialAttachApn(String apn, String protocol, int authType, String username, String password, Object obj, Message result) {
        RILRequest rr = RILRequest.obtain(111, result);
        riljLog("Set RIL_REQUEST_SET_INITIAL_ATTACH_APN");
        rr.mParcel.writeString(apn);
        rr.mParcel.writeString(protocol);
        IaExtendParam param = (IaExtendParam) obj;
        rr.mParcel.writeString(param.mRoamingProtocol);
        rr.mParcel.writeInt(authType);
        rr.mParcel.writeString(username);
        rr.mParcel.writeString(password);
        rr.mParcel.writeString(param.mOperatorNumeric);
        rr.mParcel.writeInt(param.mCanHandleIms ? 1 : 0);
        rr.mParcel.writeStringArray(param.mDualApnPlmnList);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + ", apn:" + apn + ", protocol:" + protocol + ", authType:" + authType + ", username:" + username + ", password:" + password + " ," + param);
        send(rr);
    }

    public void setDataProfile(DataProfile[] dps, Message result) {
        riljLog("Set RIL_REQUEST_SET_DATA_PROFILE");
        RILRequest rr = RILRequest.obtain(128, null);
        DataProfile.toParcel(rr.mParcel, dps);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " with " + dps + " Data Profiles : ");
        for (DataProfile dataProfile : dps) {
            riljLog(dataProfile.toString());
        }
        send(rr);
    }

    public void testingEmergencyCall() {
        riljLog("testingEmergencyCall");
        this.mTestingEmergencyCall.set(true);
    }

    /* JADX WARNING: Unexpected end of synchronized block */
    /* JADX WARNING: Missing block: B:6:?, code:
            r9.println(" mWakeLockCount=" + r7.mWakeLockCount);
     */
    /* JADX WARNING: Missing block: B:9:0x00e6, code:
            r0 = r7.mRequestList.size();
            r9.println(" mRequestList count=" + r0);
            r1 = 0;
     */
    /* JADX WARNING: Missing block: B:10:0x0104, code:
            if (r1 >= r0) goto L_0x0141;
     */
    /* JADX WARNING: Missing block: B:11:0x0106, code:
            r2 = (com.android.internal.telephony.RILRequest) r7.mRequestList.valueAt(r1);
            r9.println("  [" + r2.mSerial + "] " + requestToString(r2.mRequest));
            r1 = r1 + 1;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("RIL: " + this);
        pw.println(" mSocket=" + this.mSocket);
        pw.println(" mSenderThread=" + this.mSenderThread);
        pw.println(" mSender=" + this.mSender);
        pw.println(" mReceiverThread=" + this.mReceiverThread);
        pw.println(" mReceiver=" + this.mReceiver);
        pw.println(" mWakeLock=" + this.mWakeLock);
        pw.println(" mWakeLockTimeout=" + this.mWakeLockTimeout);
        synchronized (this.mRequestList) {
            synchronized (this.mWakeLock) {
            }
        }
        pw.println(" mLastNITZTimeInfo=" + Arrays.toString(this.mLastNITZTimeInfo));
        pw.println(" mTestingEmergencyCall=" + this.mTestingEmergencyCall.get());
    }

    public void iccOpenLogicalChannel(String AID, Message response) {
        RILRequest rr = RILRequest.obtain(RilDataCallFailCause.PDP_FAIL_EMM_ACCESS_BARRED, response);
        rr.mParcel.writeString(AID);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    public void iccCloseLogicalChannel(int channel, Message response) {
        RILRequest rr = RILRequest.obtain(RilDataCallFailCause.PDP_FAIL_EMERGENCY_IFACE_ONLY, response);
        rr.mParcel.writeInt(1);
        rr.mParcel.writeInt(channel);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    public void iccTransmitApduLogicalChannel(int channel, int cla, int instruction, int p1, int p2, int p3, String data, Message response) {
        if (channel <= 0) {
            throw new RuntimeException("Invalid channel in iccTransmitApduLogicalChannel: " + channel);
        }
        iccTransmitApduHelper(RilDataCallFailCause.PDP_FAIL_IFACE_MISMATCH, channel, cla, instruction, p1, p2, p3, data, response);
    }

    public void iccTransmitApduBasicChannel(int cla, int instruction, int p1, int p2, int p3, String data, Message response) {
        iccTransmitApduHelper(114, 0, cla, instruction, p1, p2, p3, data, response);
    }

    private void iccTransmitApduHelper(int rilCommand, int channel, int cla, int instruction, int p1, int p2, int p3, String data, Message response) {
        RILRequest rr = RILRequest.obtain(rilCommand, response);
        rr.mParcel.writeInt(channel);
        rr.mParcel.writeInt(cla);
        rr.mParcel.writeInt(instruction);
        rr.mParcel.writeInt(p1);
        rr.mParcel.writeInt(p2);
        rr.mParcel.writeInt(p3);
        rr.mParcel.writeString(data);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    public void nvReadItem(int itemID, Message response) {
        RILRequest rr = RILRequest.obtain(RilDataCallFailCause.PDP_FAIL_COMPANION_IFACE_IN_USE, response);
        rr.mParcel.writeInt(itemID);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + ' ' + itemID);
        send(rr);
    }

    public void nvWriteItem(int itemID, String itemValue, Message response) {
        RILRequest rr = RILRequest.obtain(RilDataCallFailCause.PDP_FAIL_IP_ADDRESS_MISMATCH, response);
        rr.mParcel.writeInt(itemID);
        rr.mParcel.writeString(itemValue);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + ' ' + itemID + ": " + itemValue);
        send(rr);
    }

    public void nvWriteCdmaPrl(byte[] preferredRoamingList, Message response) {
        RILRequest rr = RILRequest.obtain(RilDataCallFailCause.PDP_FAIL_IFACE_AND_POL_FAMILY_MISMATCH, response);
        rr.mParcel.writeByteArray(preferredRoamingList);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " (" + preferredRoamingList.length + " bytes)");
        send(rr);
    }

    public void nvResetConfig(int resetType, Message response) {
        RILRequest rr = RILRequest.obtain(RilDataCallFailCause.PDP_FAIL_EMM_ACCESS_BARRED_INFINITE_RETRY, response);
        rr.mParcel.writeInt(1);
        rr.mParcel.writeInt(resetType);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + ' ' + resetType);
        send(rr);
    }

    public void setRadioCapability(RadioCapability rc, Message response) {
        RILRequest rr = RILRequest.obtain(131, response);
        rr.mParcel.writeInt(rc.getVersion());
        rr.mParcel.writeInt(rc.getSession());
        rr.mParcel.writeInt(rc.getPhase());
        rr.mParcel.writeInt(rc.getRadioAccessFamily());
        rr.mParcel.writeString(rc.getLogicalModemUuid());
        rr.mParcel.writeInt(rc.getStatus());
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " " + rc.toString());
        send(rr);
    }

    public void getRadioCapability(Message response) {
        RILRequest rr = RILRequest.obtain(130, response);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    public void startLceService(int reportIntervalMs, boolean pullMode, Message response) {
        RILRequest rr = RILRequest.obtain(132, response);
        rr.mParcel.writeInt(2);
        rr.mParcel.writeInt(reportIntervalMs);
        rr.mParcel.writeInt(pullMode ? 1 : 0);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    public void stopLceService(Message response) {
        RILRequest rr = RILRequest.obtain(133, response);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    public void pullLceData(Message response) {
        RILRequest rr = RILRequest.obtain(134, response);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    public void getModemActivityInfo(Message response) {
        RILRequest rr = RILRequest.obtain(135, response);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
        Message msg = this.mSender.obtainMessage(5);
        msg.obj = null;
        msg.arg1 = rr.mSerial;
        this.mSender.sendMessageDelayed(msg, 2000);
    }

    public void setAllowedCarriers(List<CarrierIdentifier> carriers, Message response) {
        RILRequest rr = RILRequest.obtain(136, response);
        rr.mParcel.writeInt(carriers.size());
        rr.mParcel.writeInt(0);
        for (CarrierIdentifier ci : carriers) {
            rr.mParcel.writeString(ci.getMcc());
            rr.mParcel.writeString(ci.getMnc());
            int matchType = 0;
            String matchData = null;
            if (!TextUtils.isEmpty(ci.getSpn())) {
                matchType = 1;
                matchData = ci.getSpn();
            } else if (!TextUtils.isEmpty(ci.getImsi())) {
                matchType = 2;
                matchData = ci.getImsi();
            } else if (!TextUtils.isEmpty(ci.getGid1())) {
                matchType = 3;
                matchData = ci.getGid1();
            } else if (!TextUtils.isEmpty(ci.getGid2())) {
                matchType = 4;
                matchData = ci.getGid2();
            }
            rr.mParcel.writeInt(matchType);
            rr.mParcel.writeString(matchData);
        }
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    public void getAllowedCarriers(Message response) {
        RILRequest rr = RILRequest.obtain(137, response);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    public void setTrm(int mode, Message result) {
        RILRequest rr = RILRequest.obtain(2031, null);
        rr.mParcel.writeInt(1);
        rr.mParcel.writeInt(mode);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    public void setStkEvdlCallByAP(int enabled, Message response) {
        RILRequest rr = RILRequest.obtain(2058, response);
        riljLog(rr.serialString() + ">>> " + requestToString(rr.mRequest));
        rr.mParcel.writeInt(1);
        rr.mParcel.writeInt(enabled);
        send(rr);
    }

    public void hangupAll(Message result) {
        RILRequest rr = RILRequest.obtain(2063, result);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    public void setCallIndication(int mode, int callId, int seqNumber, Message result) {
        RILRequest rr = RILRequest.obtain(2065, result);
        rr.mParcel.writeInt(3);
        rr.mParcel.writeInt(mode);
        rr.mParcel.writeInt(callId);
        rr.mParcel.writeInt(seqNumber);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " " + mode + ", " + callId + ", " + seqNumber);
        send(rr);
    }

    public void emergencyDial(String address, int clirMode, UUSInfo uusInfo, Message result) {
        RILRequest rr = RILRequest.obtain(2066, result);
        rr.mParcel.writeString(address);
        rr.mParcel.writeInt(clirMode);
        if (uusInfo == null) {
            rr.mParcel.writeInt(0);
        } else {
            rr.mParcel.writeInt(1);
            rr.mParcel.writeInt(uusInfo.getType());
            rr.mParcel.writeInt(uusInfo.getDcs());
            rr.mParcel.writeByteArray(uusInfo.getUserData());
        }
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    public void setEccServiceCategory(int serviceCategory) {
        RILRequest rr = RILRequest.obtain(2067, null);
        rr.mParcel.writeInt(1);
        rr.mParcel.writeInt(serviceCategory);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " " + serviceCategory);
        send(rr);
    }

    private void setEccList() {
        RILRequest rr = RILRequest.obtain(2068, null);
        ArrayList<EccEntry> eccList = PhoneNumberUtils.getEccList();
        rr.mParcel.writeInt(eccList.size() * 3);
        for (EccEntry entry : eccList) {
            rr.mParcel.writeString(entry.getEcc());
            rr.mParcel.writeString(entry.getCategory());
            String strCondition = entry.getCondition();
            if (strCondition.equals("2") || !TextUtils.isEmpty(entry.getPlmn())) {
                strCondition = "0";
            }
            rr.mParcel.writeString(strCondition);
        }
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    public void setSpeechCodecInfo(boolean enable, Message response) {
        int i = 1;
        RILRequest rr = RILRequest.obtain(2089, response);
        rr.mParcel.writeInt(1);
        Parcel parcel = rr.mParcel;
        if (!enable) {
            i = 0;
        }
        parcel.writeInt(i);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " " + enable);
        send(rr);
    }

    public void vtDial(String address, int clirMode, UUSInfo uusInfo, Message result) {
        RILRequest rr;
        if (PhoneNumberUtils.isUriNumber(address)) {
            rr = RILRequest.obtain(2131, result);
            rr.mParcel.writeString(address);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            send(rr);
            return;
        }
        rr = RILRequest.obtain(2093, result);
        rr.mParcel.writeString(address);
        rr.mParcel.writeInt(clirMode);
        if (uusInfo == null) {
            rr.mParcel.writeInt(0);
        } else {
            rr.mParcel.writeInt(1);
            rr.mParcel.writeInt(uusInfo.getType());
            rr.mParcel.writeInt(uusInfo.getDcs());
            rr.mParcel.writeByteArray(uusInfo.getUserData());
        }
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    public void acceptVtCallWithVoiceOnly(int callId, Message result) {
        RILRequest rr = RILRequest.obtain(2094, result);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " " + callId);
        rr.mParcel.writeInt(1);
        rr.mParcel.writeInt(callId);
        send(rr);
    }

    public void replaceVtCall(int index, Message result) {
        RILRequest rr = RILRequest.obtain(2095, result);
        rr.mParcel.writeInt(1);
        rr.mParcel.writeInt(index);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    public void setRemoveRestrictEutranMode(boolean enable, Message result) {
        RILRequest rr = RILRequest.obtain(2091, result);
        int type = enable ? 1 : 0;
        rr.mParcel.writeInt(1);
        rr.mParcel.writeInt(type);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + ": " + type);
        send(rr);
    }

    public void syncApnTable(String[] apnlist, Message result) {
        RILRequest rr = RILRequest.obtain(2134, result);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        rr.mParcel.writeStringArray(apnlist);
        send(rr);
    }

    public void syncDataSettingsToMd(int[] dataSetting, Message result) {
        RILRequest rr = RILRequest.obtain(2152, result);
        rr.mParcel.writeIntArray(dataSetting);
        send(rr);
    }

    public void resetMdDataRetryCount(String apnName, Message result) {
        RILRequest rr = RILRequest.obtain(2183, result);
        rr.mParcel.writeInt(1);
        rr.mParcel.writeString(apnName);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + ": " + apnName);
        send(rr);
    }

    public void openIccApplication(int application, Message response) {
        RILRequest rr = RILRequest.obtain(2070, response);
        rr.mParcel.writeInt(1);
        rr.mParcel.writeInt(application);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + ", application = " + application);
        send(rr);
    }

    public void getIccApplicationStatus(int sessionId, Message result) {
        RILRequest rr = RILRequest.obtain(2071, result);
        rr.mParcel.writeInt(1);
        rr.mParcel.writeInt(sessionId);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + ", session = " + sessionId);
        send(rr);
    }

    public void queryNetworkLock(int category, Message response) {
        RILRequest rr = RILRequest.obtain(2016, response);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        riljLog("queryNetworkLock:" + category);
        rr.mParcel.writeInt(1);
        rr.mParcel.writeInt(category);
        send(rr);
    }

    public void setNetworkLock(int catagory, int lockop, String password, String data_imsi, String gid1, String gid2, Message response) {
        RILRequest rr = RILRequest.obtain(2017, response);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        riljLog("setNetworkLock:" + catagory + ", " + lockop + ", " + password + ", " + data_imsi + ", " + gid1 + ", " + gid2);
        rr.mParcel.writeInt(6);
        rr.mParcel.writeString(Integer.toString(catagory));
        rr.mParcel.writeString(Integer.toString(lockop));
        if (password != null) {
            rr.mParcel.writeString(password);
        } else {
            rr.mParcel.writeString(UsimPBMemInfo.STRING_NOT_SET);
        }
        rr.mParcel.writeString(data_imsi);
        rr.mParcel.writeString(gid1);
        rr.mParcel.writeString(gid2);
        send(rr);
    }

    public void doGeneralSimAuthentication(int sessionId, int mode, int tag, String param1, String param2, Message response) {
        String length;
        RILRequest rr = RILRequest.obtain(2069, response);
        rr.mParcel.writeInt(sessionId);
        rr.mParcel.writeInt(mode);
        if (param1 == null || param1.length() <= 0) {
            rr.mParcel.writeString(param1);
        } else {
            length = Integer.toHexString(param1.length() / 2);
            rr.mParcel.writeString(sessionId == 0 ? param1 : ((length.length() % 2 == 1 ? "0" : UsimPBMemInfo.STRING_NOT_SET) + length) + param1);
        }
        if (param2 == null || param2.length() <= 0) {
            rr.mParcel.writeString(param2);
        } else {
            length = Integer.toHexString(param2.length() / 2);
            rr.mParcel.writeString(sessionId == 0 ? param2 : ((length.length() % 2 == 1 ? "0" : UsimPBMemInfo.STRING_NOT_SET) + length) + param2);
        }
        if (mode == 1) {
            rr.mParcel.writeInt(tag);
        }
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + ": " + "session = " + sessionId + ",mode = " + mode + ",tag = " + tag + ", " + param1 + ", " + param2);
        send(rr);
    }

    public void iccGetATR(Message result) {
        RILRequest rr = RILRequest.obtain(2042, result);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    public void setSimPower(int mode, Message result) {
        RILRequest rr = RILRequest.obtain(2133, result);
        rr.mParcel.writeInt(1);
        rr.mParcel.writeInt(mode);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + mode);
        send(rr);
    }

    public void queryPhbStorageInfo(int type, Message response) {
        RILRequest rr = RILRequest.obtain(2012, response);
        rr.mParcel.writeInt(1);
        rr.mParcel.writeInt(type);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + ": " + type);
        send(rr);
    }

    public void writePhbEntry(PhbEntry entry, Message result) {
        RILRequest rr = RILRequest.obtain(2013, result);
        rr.mParcel.writeInt(entry.type);
        rr.mParcel.writeInt(entry.index);
        rr.mParcel.writeString(entry.number);
        rr.mParcel.writeInt(entry.ton);
        rr.mParcel.writeString(entry.alphaId);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + ": " + entry);
        send(rr);
    }

    public void ReadPhbEntry(int type, int bIndex, int eIndex, Message response) {
        RILRequest rr = RILRequest.obtain(2014, response);
        rr.mParcel.writeInt(3);
        rr.mParcel.writeInt(type);
        rr.mParcel.writeInt(bIndex);
        rr.mParcel.writeInt(eIndex);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + ": " + type + " begin: " + bIndex + " end: " + eIndex);
        send(rr);
    }

    private Object responsePhbEntries(Parcel p) {
        int numerOfEntries = p.readInt();
        PhbEntry[] response = new PhbEntry[numerOfEntries];
        Rlog.d(RILJ_LOG_TAG, "Number: " + numerOfEntries);
        for (int i = 0; i < numerOfEntries; i++) {
            response[i] = new PhbEntry();
            response[i].type = p.readInt();
            response[i].index = p.readInt();
            response[i].number = p.readString();
            response[i].ton = p.readInt();
            response[i].alphaId = p.readString();
        }
        return response;
    }

    public void queryUPBCapability(Message response) {
        RILRequest rr = RILRequest.obtain(CharacterSets.GB_2312, response);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    public void editUPBEntry(int entryType, int adnIndex, int entryIndex, String strVal, String tonForNum, String aasAnrIndex, Message response) {
        RILRequest rr = RILRequest.obtain(CharacterSets.BIG5, response);
        if (entryType == 0) {
            rr.mParcel.writeInt(6);
        } else {
            rr.mParcel.writeInt(4);
        }
        rr.mParcel.writeString(Integer.toString(entryType));
        rr.mParcel.writeString(Integer.toString(adnIndex));
        rr.mParcel.writeString(Integer.toString(entryIndex));
        rr.mParcel.writeString(strVal);
        if (entryType == 0) {
            rr.mParcel.writeString(tonForNum);
            rr.mParcel.writeString(aasAnrIndex);
        }
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    public void editUPBEntry(int entryType, int adnIndex, int entryIndex, String strVal, String tonForNum, Message response) {
        editUPBEntry(entryType, adnIndex, entryIndex, strVal, tonForNum, null, response);
    }

    public void deleteUPBEntry(int entryType, int adnIndex, int entryIndex, Message response) {
        RILRequest rr = RILRequest.obtain(CharacterSets.MACINTOSH, response);
        rr.mParcel.writeInt(3);
        rr.mParcel.writeInt(entryType);
        rr.mParcel.writeInt(adnIndex);
        rr.mParcel.writeInt(entryIndex);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    public void readUPBGasList(int startIndex, int endIndex, Message response) {
        RILRequest rr = RILRequest.obtain(2028, response);
        rr.mParcel.writeInt(2);
        rr.mParcel.writeInt(startIndex);
        rr.mParcel.writeInt(endIndex);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    public void readUPBGrpEntry(int adnIndex, Message response) {
        RILRequest rr = RILRequest.obtain(2029, response);
        rr.mParcel.writeInt(1);
        rr.mParcel.writeInt(adnIndex);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    public void writeUPBGrpEntry(int adnIndex, int[] grpIds, Message response) {
        RILRequest rr = RILRequest.obtain(2030, response);
        rr.mParcel.writeInt(nLen + 1);
        rr.mParcel.writeInt(adnIndex);
        for (int writeInt : grpIds) {
            rr.mParcel.writeInt(writeInt);
        }
        riljLog("writeUPBGrpEntry nLen is " + nLen);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    public void queryUPBAvailable(int eftype, int fileIndex, Message response) {
        RILRequest rr = RILRequest.obtain(2143, response);
        rr.mParcel.writeInt(2);
        rr.mParcel.writeInt(eftype);
        rr.mParcel.writeInt(fileIndex);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    public void readUPBEmailEntry(int adnIndex, int fileIndex, Message response) {
        RILRequest rr = RILRequest.obtain(2144, response);
        rr.mParcel.writeInt(2);
        rr.mParcel.writeInt(adnIndex);
        rr.mParcel.writeInt(fileIndex);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    public void readUPBSneEntry(int adnIndex, int fileIndex, Message response) {
        RILRequest rr = RILRequest.obtain(2145, response);
        rr.mParcel.writeInt(2);
        rr.mParcel.writeInt(adnIndex);
        rr.mParcel.writeInt(fileIndex);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    public void readUPBAnrEntry(int adnIndex, int fileIndex, Message response) {
        RILRequest rr = RILRequest.obtain(2146, response);
        rr.mParcel.writeInt(2);
        rr.mParcel.writeInt(adnIndex);
        rr.mParcel.writeInt(fileIndex);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    public void readUPBAasList(int startIndex, int endIndex, Message response) {
        RILRequest rr = RILRequest.obtain(2147, response);
        rr.mParcel.writeInt(2);
        rr.mParcel.writeInt(startIndex);
        rr.mParcel.writeInt(endIndex);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    private Object responseGetPhbMemStorage(Parcel p) {
        PBMemStorage response = PBMemStorage.createFromParcel(p);
        riljLog("responseGetPhbMemStorage:" + response);
        return response;
    }

    private Object responseReadPhbEntryExt(Parcel p) {
        int numerOfEntries = p.readInt();
        PBEntry[] response = new PBEntry[numerOfEntries];
        Rlog.d(RILJ_LOG_TAG, "responseReadPhbEntryExt Number: " + numerOfEntries);
        for (int i = 0; i < numerOfEntries; i++) {
            response[i] = new PBEntry();
            response[i].setIndex1(p.readInt());
            response[i].setNumber(p.readString());
            response[i].setType(p.readInt());
            response[i].setText(getAdnRecordFromPBEntry(p.readString()));
            response[i].setHidden(p.readInt());
            response[i].setGroup(p.readString());
            response[i].setAdnumber(p.readString());
            response[i].setAdtype(p.readInt());
            response[i].setSecondtext(p.readString());
            response[i].setEmail(getEmailRecordFromPBEntry(p.readString()));
            Rlog.d(RILJ_LOG_TAG, "responseReadPhbEntryExt[" + i + "] " + response[i].toString());
        }
        return response;
    }

    public static String convertKSC5601(String input) {
        Rlog.d(RILJ_LOG_TAG, "convertKSC5601");
        String output = UsimPBMemInfo.STRING_NOT_SET;
        try {
            byte[] inData = IccUtils.hexStringToBytes(input.substring(4));
            if (inData == null) {
                return output;
            }
            String strKSC = new String(inData, "KSC5601");
            if (strKSC == null) {
                return output;
            }
            int ucslen = strKSC.length();
            while (ucslen > 0 && strKSC.charAt(ucslen - 1) == 63735) {
                ucslen--;
            }
            return strKSC.substring(0, ucslen);
        } catch (UnsupportedEncodingException ex) {
            Rlog.d(RILJ_LOG_TAG, "Implausible UnsupportedEncodingException : " + ex);
            return output;
        }
    }

    public static String getEmailRecordFromPBEntry(String text) {
        if (text == null) {
            return null;
        }
        String email = UsimPBMemInfo.STRING_NOT_SET;
        if (text.trim().length() <= 2 || !text.startsWith("FEFE")) {
            email = text;
        } else {
            email = convertKSC5601(text);
        }
        Rlog.d(RILJ_LOG_TAG, "getEmailRecordFromPBEntry - email = " + email);
        return email;
    }

    public static String getAdnRecordFromPBEntry(String text) {
        if (text == null) {
            return null;
        }
        String alphaId = UsimPBMemInfo.STRING_NOT_SET;
        if (text.trim().length() <= 2 || !text.startsWith("FEFE")) {
            Rlog.d(RILJ_LOG_TAG, "getRecordFromPBEntry - Not KSC5601 Data");
            try {
                byte[] ba = IccUtils.hexStringToBytes(text);
                if (ba == null) {
                    return null;
                }
                alphaId = new String(ba, 0, text.length() / 2, "utf-16be");
            } catch (UnsupportedEncodingException ex) {
                Rlog.d(RILJ_LOG_TAG, "Implausible UnsupportedEncodingException : " + ex);
            }
        } else {
            alphaId = convertKSC5601(text);
        }
        Rlog.d(RILJ_LOG_TAG, "getRecordFromPBEntry - alphaId = " + alphaId);
        return alphaId;
    }

    public void getPhoneBookStringsLength(Message result) {
        RILRequest rr = RILRequest.obtain(2033, result);
        riljLog(rr.serialString() + "> :::" + requestToString(rr.mRequest));
        send(rr);
    }

    public void getPhoneBookMemStorage(Message result) {
        RILRequest rr = RILRequest.obtain(2034, result);
        riljLog(rr.serialString() + "> :::" + requestToString(rr.mRequest));
        send(rr);
    }

    public void setPhoneBookMemStorage(String storage, String password, Message result) {
        RILRequest rr = RILRequest.obtain(2035, result);
        rr.mParcel.writeInt(2);
        rr.mParcel.writeString(storage);
        rr.mParcel.writeString(password);
        riljLog(rr.serialString() + "> :::" + requestToString(rr.mRequest));
        send(rr);
    }

    public void readPhoneBookEntryExt(int index1, int index2, Message result) {
        RILRequest rr = RILRequest.obtain(2036, result);
        rr.mParcel.writeInt(2);
        rr.mParcel.writeInt(index1);
        rr.mParcel.writeInt(index2);
        riljLog(rr.serialString() + "> :::" + requestToString(rr.mRequest));
        send(rr);
    }

    public void writePhoneBookEntryExt(PBEntry entry, Message result) {
        RILRequest rr = RILRequest.obtain(2037, result);
        rr.mParcel.writeInt(entry.getIndex1());
        rr.mParcel.writeString(entry.getNumber());
        rr.mParcel.writeInt(entry.getType());
        rr.mParcel.writeString(entry.getText());
        rr.mParcel.writeInt(entry.getHidden());
        rr.mParcel.writeString(entry.getGroup());
        rr.mParcel.writeString(entry.getAdnumber());
        rr.mParcel.writeInt(entry.getAdtype());
        rr.mParcel.writeString(entry.getSecondtext());
        rr.mParcel.writeString(entry.getEmail());
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + ": " + entry);
        send(rr);
    }

    public void getSmsParameters(Message response) {
        RILRequest rr = RILRequest.obtain(2038, response);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    private Object responseSmsParams(Parcel p) {
        return new SmsParameters(p.readInt(), p.readInt(), p.readInt(), p.readInt());
    }

    public void setSmsParameters(SmsParameters params, Message response) {
        RILRequest rr = RILRequest.obtain(2039, response);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        rr.mParcel.writeInt(4);
        rr.mParcel.writeInt(params.format);
        rr.mParcel.writeInt(params.vp);
        rr.mParcel.writeInt(params.pid);
        rr.mParcel.writeInt(params.dcs);
        send(rr);
    }

    public void getSmsSimMemoryStatus(Message result) {
        RILRequest rr = RILRequest.obtain(2021, result);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    public void getSmsRuimMemoryStatus(Message result) {
        RILRequest rr = RILRequest.obtain(2162, result);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    private Object responseSimSmsMemoryStatus(Parcel p) {
        IccSmsStorageStatus response = new IccSmsStorageStatus();
        response.mUsed = p.readInt();
        response.mTotal = p.readInt();
        return response;
    }

    public void setEtws(int mode, Message result) {
        RILRequest rr = RILRequest.obtain(2047, result);
        rr.mParcel.writeInt(1);
        rr.mParcel.writeInt(mode);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " " + mode);
        send(rr);
    }

    public void setCellBroadcastChannelConfigInfo(String config, int cb_set_type, Message response) {
        RILRequest rr = RILRequest.obtain(2043, response);
        rr.mParcel.writeInt(2);
        rr.mParcel.writeString(config);
        rr.mParcel.writeString(Integer.toString(cb_set_type));
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    public void setCellBroadcastLanguageConfigInfo(String config, Message response) {
        RILRequest rr = RILRequest.obtain(2044, response);
        rr.mParcel.writeString(config);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    public void queryCellBroadcastConfigInfo(Message response) {
        RILRequest rr = RILRequest.obtain(2045, response);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    private Object responseCbConfig(Parcel p) {
        return new CellBroadcastConfigInfo(p.readInt(), p.readString(), p.readString(), p.readInt() == 1);
    }

    public void removeCellBroadcastMsg(int channelId, int serialId, Message response) {
        RILRequest rr = RILRequest.obtain(2083, response);
        rr.mParcel.writeInt(2);
        rr.mParcel.writeInt(channelId);
        rr.mParcel.writeInt(serialId);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " " + channelId + ", " + serialId);
        send(rr);
    }

    private Object responseEtwsNotification(Parcel p) {
        EtwsNotification response = new EtwsNotification();
        response.warningType = p.readInt();
        response.messageId = p.readInt();
        response.serialNumber = p.readInt();
        response.plmnId = p.readString();
        response.securityInfo = p.readString();
        return response;
    }

    public void storeModemType(int modemType, Message response) {
        RILRequest rr = RILRequest.obtain(CharacterSets.CP864, response);
        rr.mParcel.writeInt(1);
        rr.mParcel.writeInt(modemType);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    public void reloadModemType(int modemType, Message response) {
        RILRequest rr = RILRequest.obtain(2103, response);
        rr.mParcel.writeInt(1);
        rr.mParcel.writeInt(modemType);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    private Object responseCrssNotification(Parcel p) {
        SuppCrssNotification notification = new SuppCrssNotification();
        notification.code = p.readInt();
        notification.type = p.readInt();
        notification.number = p.readString();
        notification.alphaid = p.readString();
        notification.cli_validity = p.readInt();
        return notification;
    }

    private void handleChldRelatedRequest(RILRequest rr) {
        synchronized (this.mDtmfReqQueue) {
            int queueSize = this.mDtmfReqQueue.size();
            if (queueSize > 0) {
                int i;
                if (this.mDtmfReqQueue.get().mRequest == 49) {
                    int j;
                    riljLog("DTMF queue isn't 0, first request is START, send stop dtmf and pending switch");
                    if (queueSize > 1) {
                        j = 2;
                    } else {
                        j = 1;
                    }
                    riljLog("queue size  " + this.mDtmfReqQueue.size());
                    for (i = queueSize - 1; i >= j; i--) {
                        this.mDtmfReqQueue.remove(i);
                    }
                    riljLog("queue size  after " + this.mDtmfReqQueue.size());
                    if (this.mDtmfReqQueue.size() == 1) {
                        RILRequest rr3 = RILRequest.obtain(50, null);
                        riljLog("add dummy stop dtmf request");
                        this.mDtmfReqQueue.stop();
                        this.mDtmfReqQueue.add(rr3);
                    }
                } else {
                    riljLog("DTMF queue isn't 0, first request is STOP, penging switch");
                    for (i = queueSize - 1; i >= 1; i--) {
                        this.mDtmfReqQueue.remove(i);
                    }
                }
                if (this.mDtmfReqQueue.getPendingRequest() != null) {
                    RILRequest pendingRequest = this.mDtmfReqQueue.getPendingRequest();
                    if (pendingRequest.mResult != null) {
                        AsyncResult.forMessage(pendingRequest.mResult, null, null);
                        pendingRequest.mResult.sendToTarget();
                    }
                }
                this.mDtmfReqQueue.setPendingRequest(rr);
            } else {
                riljLog("DTMF queue is 0, send switch Immediately");
                this.mDtmfReqQueue.setSendChldRequest();
                send(rr);
            }
        }
    }

    public void conferenceDial(String[] participants, int clirMode, boolean isVideoCall, Message result) {
        RILRequest rr = RILRequest.obtain(2100, result);
        int numberOfParticipants = participants.length;
        int numberOfStrings = (numberOfParticipants + 2) + 1;
        List<String> participantList = Arrays.asList(participants);
        Rlog.d(RILJ_LOG_TAG, "conferenceDial: numberOfParticipants " + numberOfParticipants + "numberOfStrings:" + numberOfStrings);
        rr.mParcel.writeInt(numberOfStrings);
        if (isVideoCall) {
            rr.mParcel.writeString(Integer.toString(1));
        } else {
            rr.mParcel.writeString(Integer.toString(0));
        }
        rr.mParcel.writeString(Integer.toString(numberOfParticipants));
        for (String dialNumber : participantList) {
            rr.mParcel.writeString(dialNumber);
            Rlog.d(RILJ_LOG_TAG, "conferenceDial: dialnumber " + dialNumber);
        }
        rr.mParcel.writeString(Integer.toString(clirMode));
        Rlog.d(RILJ_LOG_TAG, "conferenceDial: clirMode " + clirMode);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    public void addConferenceMember(int confCallId, String address, int callIdToAdd, Message response) {
        RILRequest rr = RILRequest.obtain(CharacterSets.HZ_GB_2312, response);
        rr.mParcel.writeInt(3);
        rr.mParcel.writeString(Integer.toString(confCallId));
        rr.mParcel.writeString(address);
        rr.mParcel.writeString(Integer.toString(callIdToAdd));
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    public void removeConferenceMember(int confCallId, String address, int callIdToRemove, Message response) {
        RILRequest rr = RILRequest.obtain(2086, response);
        rr.mParcel.writeInt(3);
        rr.mParcel.writeString(Integer.toString(confCallId));
        rr.mParcel.writeString(address);
        rr.mParcel.writeString(Integer.toString(callIdToRemove));
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    public void resumeCall(int callIdToResume, Message response) {
        RILRequest rr = RILRequest.obtain(CharacterSets.KOI8_U, response);
        rr.mParcel.writeInt(1);
        rr.mParcel.writeInt(callIdToResume);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    public void holdCall(int callIdToHold, Message response) {
        RILRequest rr = RILRequest.obtain(2104, response);
        rr.mParcel.writeInt(1);
        rr.mParcel.writeInt(callIdToHold);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    public void changeBarringPassword(String facility, String oldPwd, String newPwd, String newCfm, Message result) {
        RILRequest rr = RILRequest.obtain(44, result);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        rr.mParcel.writeInt(4);
        rr.mParcel.writeString(facility);
        rr.mParcel.writeString(oldPwd);
        rr.mParcel.writeString(newPwd);
        rr.mParcel.writeString(newCfm);
        send(rr);
    }

    public void setCLIP(boolean enable, Message result) {
        RILRequest rr = RILRequest.obtain(2076, result);
        rr.mParcel.writeInt(1);
        if (enable) {
            rr.mParcel.writeInt(1);
        } else {
            rr.mParcel.writeInt(0);
        }
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " " + enable);
        send(rr);
    }

    public String lookupOperatorNameFromNetwork(long subId, String numeric, boolean desireLongName) {
        String nitzOperatorName;
        int phoneId = SubscriptionManager.getPhoneId((int) subId);
        String nitzOperatorName2 = null;
        String nitzOperatorNumeric = TelephonyManager.getTelephonyProperty(phoneId, "persist.radio.nitz_oper_code", UsimPBMemInfo.STRING_NOT_SET);
        if (numeric != null) {
            if (numeric.equals(nitzOperatorNumeric)) {
                if (desireLongName) {
                    nitzOperatorName2 = TelephonyManager.getTelephonyProperty(phoneId, "persist.radio.nitz_oper_lname", UsimPBMemInfo.STRING_NOT_SET);
                    if (nitzOperatorName2 == null) {
                        nitzOperatorName = nitzOperatorName2;
                    } else if (nitzOperatorName2.equals("VODAFONE IN")) {
                        nitzOperatorName = "Vodafone IN";
                    }
                } else {
                    nitzOperatorName = TelephonyManager.getTelephonyProperty(phoneId, "persist.radio.nitz_oper_sname", UsimPBMemInfo.STRING_NOT_SET);
                }
            }
            nitzOperatorName = nitzOperatorName2;
        } else {
            nitzOperatorName = null;
        }
        if (nitzOperatorName == null || !nitzOperatorName.startsWith("uCs2")) {
            nitzOperatorName2 = nitzOperatorName;
        } else {
            riljLog("lookupOperatorNameFromNetwork handling UCS2 format name");
            try {
                nitzOperatorName2 = new String(IccUtils.hexStringToBytes(nitzOperatorName.substring(4)), "UTF-16");
            } catch (UnsupportedEncodingException e) {
                riljLog("lookupOperatorNameFromNetwork UnsupportedEncodingException");
                nitzOperatorName2 = nitzOperatorName;
            }
        }
        riljLog("lookupOperatorNameFromNetwork numeric= " + numeric + ",subId= " + subId + ",nitzOperatorNumeric= " + nitzOperatorNumeric + ",nitzOperatorName= " + nitzOperatorName2);
        return nitzOperatorName2;
    }

    public void setNetworkSelectionModeManualWithAct(String operatorNumeric, String act, Message response) {
        RILRequest rr = RILRequest.obtain(2018, response);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " " + operatorNumeric + UsimPBMemInfo.STRING_NOT_SET + act);
        rr.mParcel.writeInt(3);
        rr.mParcel.writeString(operatorNumeric);
        rr.mParcel.writeString(act);
        rr.mParcel.writeString("0");
        send(rr);
    }

    private Object responseNetworkInfoWithActs(Parcel p) {
        String[] strings = (String[]) responseStrings(p);
        if (strings.length % 4 != 0) {
            throw new RuntimeException("RIL_REQUEST_GET_POL_LIST: invalid response. Got " + strings.length + " strings, expected multible of 5");
        }
        ArrayList<NetworkInfoWithAcT> ret = new ArrayList(strings.length / 4);
        int nAct = 0;
        int nIndex = 0;
        for (int i = 0; i < strings.length; i += 4) {
            String strOperName = null;
            String strOperNumeric = null;
            if (strings[i] != null) {
                nIndex = Integer.parseInt(strings[i]);
            } else {
                Rlog.d(RILJ_LOG_TAG, "responseNetworkInfoWithActs: no invalid index. i is " + i);
            }
            if (strings[i + 1] != null) {
                switch (Integer.parseInt(strings[i + 1])) {
                    case 0:
                    case 1:
                        strOperName = strings[i + 2];
                        break;
                    case 2:
                        if (strings[i + 2] != null) {
                            strOperNumeric = strings[i + 2];
                            strOperName = SpnOverride.getInstance().lookupOperatorName(SubscriptionManager.getSubIdUsingPhoneId(this.mInstanceId.intValue()), strings[i + 2], true, this.mContext);
                            break;
                        }
                        break;
                }
            }
            if (strings[i + 3] != null) {
                nAct = Integer.parseInt(strings[i + 3]);
            } else {
                Rlog.d(RILJ_LOG_TAG, "responseNetworkInfoWithActs: no invalid Act. i is " + i);
            }
            if (strOperNumeric == null || strOperNumeric.equals("?????")) {
                Rlog.d(RILJ_LOG_TAG, "responseNetworkInfoWithActs: invalid oper. i is " + i);
            } else {
                ret.add(new NetworkInfoWithAcT(strOperName, strOperNumeric, nAct, nIndex));
            }
        }
        return ret;
    }

    public void setNetworkSelectionModeSemiAutomatic(String operatorNumeric, String act, Message response) {
        RILRequest rr = RILRequest.obtain(2018, response);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " " + operatorNumeric + UsimPBMemInfo.STRING_NOT_SET + act);
        rr.mParcel.writeInt(3);
        rr.mParcel.writeString(operatorNumeric);
        rr.mParcel.writeString(act);
        rr.mParcel.writeString("1");
        send(rr);
    }

    public void getPOLCapabilty(Message response) {
        RILRequest rr = RILRequest.obtain(2022, response);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    public void getCurrentPOLList(Message response) {
        RILRequest rr = RILRequest.obtain(2023, response);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    public void setPOLEntry(int index, String numeric, int nAct, Message response) {
        RILRequest rr = RILRequest.obtain(2024, response);
        if (numeric == null || numeric.length() == 0) {
            rr.mParcel.writeInt(1);
            rr.mParcel.writeString(Integer.toString(index));
        } else {
            rr.mParcel.writeInt(3);
            rr.mParcel.writeString(Integer.toString(index));
            rr.mParcel.writeString(numeric);
            rr.mParcel.writeString(Integer.toString(nAct));
        }
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    public void getFemtoCellList(String operatorNumeric, int rat, Message response) {
        RILRequest rr = RILRequest.obtain(2059, response);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        rr.mParcel.writeInt(2);
        rr.mParcel.writeString(operatorNumeric);
        rr.mParcel.writeString(Integer.toString(rat));
        send(rr);
    }

    public void abortFemtoCellList(Message response) {
        RILRequest rr = RILRequest.obtain(2060, response);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    public void selectFemtoCell(FemtoCellInfo femtocell, Message response) {
        RILRequest rr = RILRequest.obtain(2061, response);
        int act = femtocell.getCsgRat();
        if (act == 14) {
            act = 7;
        } else if (act == 3) {
            act = 2;
        } else {
            act = 0;
        }
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " csgId=" + femtocell.getCsgId() + " plmn=" + femtocell.getOperatorNumeric() + " rat=" + femtocell.getCsgRat() + " act=" + act);
        rr.mParcel.writeInt(3);
        rr.mParcel.writeString(femtocell.getOperatorNumeric());
        rr.mParcel.writeString(Integer.toString(act));
        rr.mParcel.writeString(Integer.toString(femtocell.getCsgId()));
        send(rr);
    }

    public void queryFemtoCellSystemSelectionMode(Message response) {
        RILRequest rr = RILRequest.obtain(2157, response);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    public void setFemtoCellSystemSelectionMode(int mode, Message response) {
        RILRequest rr = RILRequest.obtain(2158, response);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " mode=" + mode);
        rr.mParcel.writeInt(1);
        rr.mParcel.writeInt(mode);
        send(rr);
    }

    public void setCurrentStatus(int airplaneMode, int imsReg, Message response) {
        RILRequest rr = RILRequest.obtain(2161, response);
        rr.mParcel.writeInt(2);
        rr.mParcel.writeInt(airplaneMode);
        rr.mParcel.writeInt(imsReg);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    public void setEccPreferredRat(int phoneType, Message response) {
        RILRequest rr = RILRequest.obtain(2184, response);
        rr.mParcel.writeInt(1);
        rr.mParcel.writeInt(phoneType);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + ": " + phoneType);
        send(rr);
    }

    public void setLteAccessStratumReport(boolean enable, Message result) {
        RILRequest rr = RILRequest.obtain(2110, result);
        int type = enable ? 1 : 0;
        rr.mParcel.writeInt(1);
        rr.mParcel.writeInt(type);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + ": " + type);
        send(rr);
    }

    public void setLteUplinkDataTransfer(int state, int interfaceId, Message result) {
        RILRequest rr = RILRequest.obtain(2111, result);
        rr.mParcel.writeInt(2);
        rr.mParcel.writeInt(state);
        rr.mParcel.writeInt(interfaceId);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " state = " + state + ", interfaceId = " + interfaceId);
        send(rr);
    }

    public boolean isGettingAvailableNetworks() {
        synchronized (this.mRequestList) {
            int i = 0;
            int s = this.mRequestList.size();
            while (i < s) {
                RILRequest rr = (RILRequest) this.mRequestList.valueAt(i);
                if (rr == null || !(rr.mRequest == 48 || rr.mRequest == 2074)) {
                    i++;
                } else {
                    return true;
                }
            }
            return false;
        }
    }

    public void setIMSEnabled(boolean enable, Message response) {
        RILRequest rr = RILRequest.obtain(2073, response);
        rr.mParcel.writeInt(1);
        if (enable) {
            rr.mParcel.writeInt(1);
        } else {
            rr.mParcel.writeInt(0);
        }
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    public void setFDMode(int mode, int parameter1, int parameter2, Message response) {
        RILRequest rr = RILRequest.obtain(2048, response);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        if (mode == 0 || mode == 1) {
            rr.mParcel.writeInt(1);
            rr.mParcel.writeInt(mode);
        } else if (mode == 3) {
            rr.mParcel.writeInt(2);
            rr.mParcel.writeInt(mode);
            rr.mParcel.writeInt(parameter1);
        } else if (mode == 2) {
            rr.mParcel.writeInt(3);
            rr.mParcel.writeInt(mode);
            rr.mParcel.writeInt(parameter1);
            rr.mParcel.writeInt(parameter2);
        }
        send(rr);
    }

    public void setDataCentric(boolean enable, Message response) {
        riljLog("setDataCentric");
        RILRequest rr = RILRequest.obtain(CharacterSets.KOI8_R, response);
        rr.mParcel.writeInt(1);
        if (enable) {
            rr.mParcel.writeInt(1);
        } else {
            rr.mParcel.writeInt(0);
        }
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    public void setImsCallStatus(boolean existed, Message response) {
        int i = 1;
        RILRequest rr = RILRequest.obtain(2092, null);
        rr.mParcel.writeInt(1);
        Parcel parcel = rr.mParcel;
        if (!existed) {
            i = 0;
        }
        parcel.writeInt(i);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    public void setSrvccCallContextTransfer(int numberOfCall, SrvccCallContext[] callList) {
        RILRequest rr = RILRequest.obtain(CharacterSets.BIG5_HKSCS, null);
        if (numberOfCall > 0 && callList != null) {
            rr.mParcel.writeInt((numberOfCall * 9) + 1);
            rr.mParcel.writeString(Integer.toString(numberOfCall));
            for (int i = 0; i < numberOfCall; i++) {
                rr.mParcel.writeString(Integer.toString(callList[i].getCallId()));
                rr.mParcel.writeString(Integer.toString(callList[i].getCallMode()));
                rr.mParcel.writeString(Integer.toString(callList[i].getCallDirection()));
                rr.mParcel.writeString(Integer.toString(callList[i].getCallState()));
                rr.mParcel.writeString(Integer.toString(callList[i].getEccCategory()));
                rr.mParcel.writeString(Integer.toString(callList[i].getNumberType()));
                rr.mParcel.writeString(callList[i].getNumber());
                rr.mParcel.writeString(callList[i].getName());
                rr.mParcel.writeString(Integer.toString(callList[i].getCliValidity()));
            }
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            send(rr);
        }
    }

    public void updateImsRegistrationStatus(int regState, int regType, int reason) {
        RILRequest rr = RILRequest.obtain(2102, null);
        rr.mParcel.writeInt(3);
        rr.mParcel.writeInt(regState);
        rr.mParcel.writeInt(regType);
        rr.mParcel.writeInt(reason);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    public int getDisplayState() {
        return this.mDefaultDisplayState;
    }

    public void setRegistrationSuspendEnabled(int enabled, Message response) {
        RILRequest rr = RILRequest.obtain(2049, response);
        rr.mParcel.writeInt(1);
        rr.mParcel.writeInt(enabled);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    public void setResumeRegistration(int sessionId, Message response) {
        RILRequest rr = RILRequest.obtain(2050, response);
        rr.mParcel.writeInt(1);
        rr.mParcel.writeInt(sessionId);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    public void triggerModeSwitchByEcc(int mode, Message response) {
        RILRequest rr = RILRequest.obtain(2142, response);
        rr.mParcel.writeInt(1);
        rr.mParcel.writeInt(mode);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + ", mode:" + mode);
        send(rr);
        Message msg = this.mSender.obtainMessage(5);
        msg.obj = null;
        msg.arg1 = rr.mSerial;
        this.mSender.sendMessageDelayed(msg, 2000);
    }

    public void enablePseudoBSMonitor(boolean reportOn, int reportRateInMinutes, Message response) {
        int i = 1;
        RILRequest rr = RILRequest.obtain(2153, response);
        rr.mParcel.writeInt(3);
        rr.mParcel.writeInt(1);
        Parcel parcel = rr.mParcel;
        if (!reportOn) {
            i = 0;
        }
        parcel.writeInt(i);
        rr.mParcel.writeInt(reportRateInMinutes);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    public void disablePseudoBSMonitor(Message response) {
        RILRequest rr = RILRequest.obtain(2153, response);
        rr.mParcel.writeInt(3);
        rr.mParcel.writeInt(0);
        rr.mParcel.writeInt(0);
        rr.mParcel.writeInt(0);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    public void queryPseudoBSRecords(Message response) {
        RILRequest rr = RILRequest.obtain(2154, response);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    public void enablePseudoBSMonitor(int apcMode, boolean reportOn, int reportRateInSeconds, Message response) {
        if (apcMode == 0) {
            disablePseudoBSMonitor(response);
        } else if (apcMode == 1) {
            enablePseudoBSMonitor(reportOn, reportRateInSeconds, response);
        } else if (apcMode == 2) {
            RILRequest rr = RILRequest.obtain(2153, response);
            rr.mParcel.writeInt(3);
            rr.mParcel.writeInt(2);
            rr.mParcel.writeInt(0);
            rr.mParcel.writeInt(0);
            riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            send(rr);
        }
    }

    public void setRxTestConfig(int AntType, Message result) {
        RILRequest rr = RILRequest.obtain(2159, result);
        rr.mParcel.writeInt(1);
        rr.mParcel.writeInt(AntType);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + "> " + " Type:" + AntType);
        send(rr);
    }

    private Object responseAntennaConf(Parcel p) {
        int respLen = p.readInt();
        int[] respAntConf = new int[respLen];
        for (int i = 0; i < respLen; i++) {
            respAntConf[i] = p.readInt();
            Rlog.d(RILJ_LOG_TAG, "responseAntennaConf() Response: " + respAntConf[i]);
        }
        return respAntConf;
    }

    public void getRxTestResult(Message result) {
        RILRequest rr = RILRequest.obtain(2160, result);
        rr.mParcel.writeInt(1);
        rr.mParcel.writeInt(0);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    private Object responseAntennaInfo(Parcel p) {
        int respLen = p.readInt();
        int[] respAntInfo = new int[respLen];
        for (int i = 0; i < respLen; i++) {
            respAntInfo[i] = p.readInt();
            Rlog.d(RILJ_LOG_TAG, "responseAntennaInfo() Response: " + respAntInfo[i]);
        }
        return respAntInfo;
    }

    public void setGsmBroadcastLangs(String lang, Message response) {
        RILRequest rr = RILRequest.obtain(2163, response);
        rr.mParcel.writeString(lang);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + ", lang:" + lang);
        send(rr);
    }

    public void getGsmBroadcastLangs(Message response) {
        RILRequest rr = RILRequest.obtain(2164, response);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    public void getGsmBroadcastActivation(Message response) {
        RILRequest rr = RILRequest.obtain(2187, response);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    public void setCarrierRestrictionState(int state, String password, Message response) {
        RILRequest rr = RILRequest.obtain(2173, response);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        rr.mParcel.writeInt(2);
        rr.mParcel.writeString(Integer.toString(state));
        rr.mParcel.writeString(password);
        send(rr);
    }

    public void getCarrierRestrictionState(Message response) {
        RILRequest rr = RILRequest.obtain(2174, response);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
        send(rr);
    }

    public void queryCallForwardInTimeSlotStatus(int cfReason, int serviceClass, Message response) {
        String number = UsimPBMemInfo.STRING_NOT_SET;
        String timeSlotBegin = UsimPBMemInfo.STRING_NOT_SET;
        String timeSlotEnd = UsimPBMemInfo.STRING_NOT_SET;
        RILRequest rr = RILRequest.obtain(2189, response);
        rr.mParcel.writeInt(2);
        rr.mParcel.writeInt(cfReason);
        rr.mParcel.writeInt(serviceClass);
        rr.mParcel.writeInt(PhoneNumberUtils.toaFromString(number));
        rr.mParcel.writeString(number);
        rr.mParcel.writeInt(0);
        rr.mParcel.writeString(timeSlotBegin);
        rr.mParcel.writeString(timeSlotEnd);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " " + cfReason + " " + serviceClass);
        send(rr);
    }

    public void setCallForwardInTimeSlot(int action, int cfReason, int serviceClass, String number, int timeSeconds, long[] timeSlot, Message response) {
        String timeSlotBegin = UsimPBMemInfo.STRING_NOT_SET;
        String timeSlotEnd = UsimPBMemInfo.STRING_NOT_SET;
        RILRequest rr = RILRequest.obtain(2190, response);
        rr.mParcel.writeInt(action);
        rr.mParcel.writeInt(cfReason);
        rr.mParcel.writeInt(serviceClass);
        rr.mParcel.writeInt(PhoneNumberUtils.toaFromString(number));
        rr.mParcel.writeString(number);
        rr.mParcel.writeInt(timeSeconds);
        if (timeSlot != null && timeSlot.length == 2) {
            for (int i = 0; i < timeSlot.length; i++) {
                Date date = new Date(timeSlot[i]);
                SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
                dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+8"));
                if (i == 0) {
                    timeSlotBegin = dateFormat.format(date);
                } else {
                    timeSlotEnd = dateFormat.format(date);
                }
            }
        }
        rr.mParcel.writeString(timeSlotBegin);
        rr.mParcel.writeString(timeSlotEnd);
        riljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " " + action + " " + cfReason + " " + serviceClass + timeSeconds + " timeSlot[" + timeSlotBegin + ":" + timeSlotEnd + "]");
        send(rr);
    }

    private Object responseCallForwardEx(Parcel p) {
        int numInfos = p.readInt();
        CallForwardInfoEx[] infos = new CallForwardInfoEx[numInfos];
        for (int i = 0; i < numInfos; i++) {
            long[] timeSlot = new long[2];
            String[] timeSlotStr = new String[2];
            infos[i] = new CallForwardInfoEx();
            infos[i].status = p.readInt();
            infos[i].reason = p.readInt();
            infos[i].serviceClass = p.readInt();
            infos[i].toa = p.readInt();
            infos[i].number = p.readString();
            infos[i].timeSeconds = p.readInt();
            timeSlotStr[0] = p.readString();
            timeSlotStr[1] = p.readString();
            if (timeSlotStr[0] == null || timeSlotStr[1] == null) {
                infos[i].timeSlot = null;
            } else {
                int j = 0;
                while (j < 2) {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
                    dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+8"));
                    try {
                        timeSlot[j] = dateFormat.parse(timeSlotStr[j]).getTime();
                        j++;
                    } catch (ParseException e) {
                        e.printStackTrace();
                        timeSlot = null;
                    }
                }
                infos[i].timeSlot = timeSlot;
            }
        }
        return infos;
    }

    private static long getValueFromByte(String data, int start, int bytes, boolean signed) {
        if (data.length() < (bytes * 2) + start) {
            return 0;
        }
        switch (bytes) {
            case 1:
                try {
                    String sub = data.substring(start, start + 2);
                    if (signed) {
                        return (long) Byte.valueOf((byte) Short.valueOf(sub, 16).shortValue()).byteValue();
                    }
                    return (long) Short.valueOf(sub, 16).shortValue();
                } catch (NumberFormatException e) {
                    return 0;
                }
            case 2:
                String reverse = data.substring(start + 2, start + 4) + data.substring(start, start + 2);
                if (signed) {
                    return (long) Short.valueOf((short) Integer.valueOf(reverse, 16).intValue()).shortValue();
                }
                return (long) Integer.valueOf(reverse, 16).intValue();
            case 4:
                String reverse2 = data.substring(start + 6, start + 8) + data.substring(start + 4, start + 6) + data.substring(start + 2, start + 4) + data.substring(start, start + 2);
                if (signed) {
                    return (long) Integer.valueOf((int) Long.valueOf(reverse2, 16).longValue()).intValue();
                }
                return Long.valueOf(reverse2, 16).longValue();
            default:
                return 0;
        }
    }

    private void oppoGetInfoFromURC(String[] data, CriticalLogInfo CLInfo) {
        try {
            long serv_cell_arfcn;
            long current_rat;
            switch (Integer.parseInt(data[0])) {
                case 5:
                    long is_dsf = getValueFromByte(data[1], 12, 1, true);
                    long call_type = getValueFromByte(data[1], 20, 1, true);
                    long current_value = getValueFromByte(data[1], 4, 2, true);
                    if (is_dsf == 0 && call_type == 1 && current_value == 0) {
                        serv_cell_arfcn = getValueFromByte(data[1], 16, 2, true);
                        CLInfo.type = 12;
                        CLInfo.extra = String.valueOf(serv_cell_arfcn);
                        CLInfo.rat = (long) oppoGetRatFromType(12);
                        CLInfo.issue = oppoGetStringFromType(12);
                        break;
                    }
                case 25:
                    if (getValueFromByte(data[1], 0, 1, true) == 1) {
                        serv_cell_arfcn = getValueFromByte(data[1], 2, 2, true);
                        CLInfo.type = 11;
                        CLInfo.extra = String.valueOf(serv_cell_arfcn);
                        CLInfo.rat = (long) oppoGetRatFromType(11);
                        CLInfo.issue = oppoGetStringFromType(11);
                        break;
                    }
                    break;
                case SYS_MTK_URC_CARD_DROP /*89*/:
                    long event_type = getValueFromByte(data[1], 0, 4, true);
                    if (event_type == 0 || event_type == 1 || event_type == 4) {
                        CLInfo.type = 160;
                        CLInfo.errcode = event_type;
                        CLInfo.rat = (long) oppoGetRatFromType(160);
                        CLInfo.issue = oppoGetStringFromType(160);
                        break;
                    }
                case 108:
                    if (getValueFromByte(data[1], 0, 1, true) == 1) {
                        CLInfo.type = 210;
                        CLInfo.rat = (long) oppoGetRatFromType(210);
                        CLInfo.issue = oppoGetStringFromType(210);
                        break;
                    }
                    break;
                case 133:
                    long rrc_conn_status = getValueFromByte(data[1], 0, 1, true);
                    long est_cause = getValueFromByte(data[1], 2, 1, true);
                    if (rrc_conn_status == 3 && est_cause == 5) {
                        long rrc_cause = getValueFromByte(data[1], 4, 1, true);
                        CLInfo.type = 16;
                        CLInfo.errcode = rrc_cause;
                        CLInfo.rat = (long) oppoGetRatFromType(16);
                        CLInfo.issue = oppoGetStringFromType(16);
                        break;
                    }
                case 256:
                    long service_request_type = getValueFromByte(data[1], 0, 1, true);
                    long service_request_cause = getValueFromByte(data[1], 2, 1, true);
                    if (service_request_type == 2 && service_request_cause == 2) {
                        long ext_service_reject_cause = getValueFromByte(data[1], 8, 1, true);
                        CLInfo.type = 15;
                        CLInfo.errcode = ext_service_reject_cause;
                        CLInfo.rat = (long) oppoGetRatFromType(15);
                        CLInfo.issue = oppoGetStringFromType(15);
                        break;
                    }
                case SYS_MTK_URC_MT_CSFB /*393*/:
                    if (getValueFromByte(data[1], 2, 1, true) == 1) {
                        long Is_mt_csfb_lu_needed = getValueFromByte(data[1], 0, 1, true);
                        CLInfo.type = 14;
                        CLInfo.extra = String.valueOf(Is_mt_csfb_lu_needed);
                        CLInfo.rat = (long) oppoGetRatFromType(14);
                        CLInfo.issue = oppoGetStringFromType(14);
                        break;
                    }
                    break;
                case SYS_MTK_URC_REG_REJECT /*394*/:
                    long lu_type = getValueFromByte(data[1], 2, 1, true);
                    long attach_type = getValueFromByte(data[1], 6, 1, true);
                    long rau_type = getValueFromByte(data[1], 10, 1, true);
                    if (attach_type != 5 || rau_type != 4 || lu_type == 3) {
                        if (lu_type != 3 || rau_type != 4 || attach_type == 5) {
                            if (lu_type == 3 && attach_type == 5 && rau_type != 4) {
                                long rau_rej_cause = getValueFromByte(data[1], 12, 1, true);
                                current_rat = getValueFromByte(data[1], 0, 1, true);
                                CLInfo.type = 63;
                                CLInfo.errcode = rau_rej_cause;
                                CLInfo.rat = current_rat;
                                CLInfo.extra = "RAUR";
                                CLInfo.issue = oppoGetStringFromType(63);
                                break;
                            }
                        }
                        long attach_rej_cause = getValueFromByte(data[1], 8, 1, true);
                        current_rat = getValueFromByte(data[1], 0, 1, true);
                        CLInfo.type = 63;
                        CLInfo.errcode = attach_rej_cause;
                        CLInfo.rat = current_rat;
                        CLInfo.extra = "GAR";
                        CLInfo.issue = oppoGetStringFromType(63);
                        break;
                    }
                    long lu_rej_cause = getValueFromByte(data[1], 4, 1, true);
                    current_rat = getValueFromByte(data[1], 0, 1, true);
                    CLInfo.type = 63;
                    CLInfo.errcode = lu_rej_cause;
                    CLInfo.rat = current_rat;
                    CLInfo.extra = "LUR";
                    CLInfo.issue = oppoGetStringFromType(63);
                    break;
                    break;
                case SYS_MTK_URC_AUTHENTICATION_REJECT /*395*/:
                    long auth_rej_type = getValueFromByte(data[1], 2, 1, true);
                    current_rat = getValueFromByte(data[1], 0, 1, true);
                    CLInfo.type = 64;
                    CLInfo.errcode = auth_rej_type;
                    CLInfo.rat = current_rat;
                    CLInfo.issue = oppoGetStringFromType(64);
                    break;
                case SYS_MTK_URC_LTE_REG_REJECT /*625*/:
                    long emm_attach_rej_cause = getValueFromByte(data[1], 2, 1, true);
                    CLInfo.type = 61;
                    CLInfo.errcode = emm_attach_rej_cause;
                    CLInfo.rat = (long) oppoGetRatFromType(61);
                    CLInfo.issue = oppoGetStringFromType(61);
                    break;
                case SYS_MTK_URC_LTE_AUTHENTICATION_REJECT /*628*/:
                    long is_auth_rej = getValueFromByte(data[1], 0, 1, true);
                    if (is_auth_rej == 1) {
                        CLInfo.type = 68;
                        CLInfo.errcode = is_auth_rej;
                        CLInfo.rat = (long) oppoGetRatFromType(68);
                        CLInfo.issue = oppoGetStringFromType(68);
                        break;
                    }
                    break;
                default:
                    CLInfo.type = -1;
                    break;
            }
        } catch (NumberFormatException e) {
            riljLog("Return EM_ID type error");
        }
    }

    private String oppoGetStringFromType(int type) {
        switch (type) {
            case 11:
                return ISSUE_SYS_OEM_NW_DIAG_CAUSE_MT_RACH;
            case 12:
                return ISSUE_SYS_OEM_NW_DIAG_CAUSE_MT_RLF;
            case 13:
                return ISSUE_SYS_OEM_NW_DIAG_CAUSE_MT_PCH;
            case 14:
                return ISSUE_SYS_OEM_NW_DIAG_CAUSE_MT_CSFB;
            case 15:
                return ISSUE_SYS_OEM_NW_DIAG_CAUSE_MT_REJECT;
            case 16:
                return ISSUE_SYS_OEM_NW_DIAG_CAUSE_MT_RRC;
            case 61:
                return ISSUE_SYS_OEM_NW_DIAG_CAUSE_LTE_REG_REJECT;
            case 63:
                return ISSUE_SYS_OEM_NW_DIAG_CAUSE_REG_REJECT;
            case 64:
                return ISSUE_SYS_OEM_NW_DIAG_CAUSE_AUTHENTICATION_REJECT;
            case 68:
                return ISSUE_SYS_OEM_NW_DIAG_CAUSE_LTE_AUTHENTICATION_REJECT;
            case 160:
                return ISSUE_SYS_OEM_NW_DIAG_CAUSE_CARD_DROP_RX_BREAK;
            case 210:
                return ISSUE_SYS_OEM_NW_DIAG_CAUSE_RF_MIPI_HW_FAILED;
            default:
                return UsimPBMemInfo.STRING_NOT_SET;
        }
    }

    private int oppoGetRatFromType(int type) {
        switch (type) {
            case 11:
                return 0;
            case 12:
                return 0;
            case 13:
                return 0;
            case 14:
                return 0;
            case 15:
                return 5;
            case 16:
                return 1;
            case 61:
                return 5;
            case 63:
                return -1;
            case 64:
                return -1;
            case 68:
                return 5;
            case 108:
                return -1;
            case 160:
                return -1;
            default:
                return -1;
        }
    }

    public void oppoProcessUnsolOemKeyLogErrMsg(Object ret) {
        String[] data = (String[]) ret;
        CriticalLogInfo CLInfo = new CriticalLogInfo(this, -1, -1, -1, UsimPBMemInfo.STRING_NOT_SET, UsimPBMemInfo.STRING_NOT_SET);
        oppoGetInfoFromURC(data, CLInfo);
        if (CLInfo.type == -1) {
            riljLog("EM_ID does not belong to critical log!");
            return;
        }
        riljLog("Get message, issue:" + CLInfo.issue + ", type:" + CLInfo.type + ", rat:" + CLInfo.rat + ", errcode:" + CLInfo.errcode + ", extra:" + CLInfo.extra);
        if (!(CLInfo.issue == null || CLInfo.issue.equals(UsimPBMemInfo.STRING_NOT_SET))) {
            int log_type = -1;
            String log_desc = UsimPBMemInfo.STRING_NOT_SET;
            try {
                String[] log_array = this.mContext.getString(this.mContext.getResources().getIdentifier("zz_oppo_critical_log_" + CLInfo.type, "string", "android")).split(",");
                log_type = Integer.valueOf(log_array[0]).intValue();
                log_desc = log_array[1];
            } catch (Exception e) {
                riljLog("Can not get resource of identifier zz_oppo_critical_log_" + CLInfo.type);
            }
            riljLog("Write log, return:" + OppoManager.writeLogToPartition(log_type, "type:" + CLInfo.type + "_rat:" + CLInfo.rat + "_errcode:" + CLInfo.errcode + "_extra:" + CLInfo.extra, "NETWORK", CLInfo.issue, log_desc));
        }
    }

    public void oppoResetSwitchDssState(Object ret, int error) {
        if (error == 0 && ret != null && ((DataCallResponse) ret).status == 0) {
            riljLog("oppoResetSwitchDssState SETUP_DATA_CALL Completed,SwitchingDssState set to false");
            SubscriptionController.getInstance().setSwitchingDssState(0, false);
            SubscriptionController.getInstance().setSwitchingDssState(1, false);
            return;
        }
        riljLog("oppoResetSwitchDssState SETUP_DATA_CALL return error");
    }

    private boolean oppoIsOperatorNameEmpty(String Name) {
        return TextUtils.isEmpty(Name) || " ".equals(Name);
    }
}
