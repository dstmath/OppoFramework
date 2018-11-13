package com.android.internal.telephony.dataconnection;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.ConnectivityManager.NetworkCallback;
import android.net.LinkProperties;
import android.net.NetworkCapabilities;
import android.net.NetworkConfig;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.net.NetworkRequest;
import android.net.NetworkUtils;
import android.net.ProxyInfo;
import android.net.TrafficStats;
import android.net.Uri;
import android.os.AsyncResult;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.OppoManager;
import android.os.OppoUsageManager;
import android.os.RegistrantList;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.preference.PreferenceManager;
import android.provider.Settings.Global;
import android.provider.Settings.SettingNotFoundException;
import android.provider.Settings.System;
import android.provider.Telephony.Carriers;
import android.provider.oppo.Telephony.SimInfo;
import android.telephony.CellLocation;
import android.telephony.PcoData;
import android.telephony.Rlog;
import android.telephony.ServiceState;
import android.telephony.SubscriptionManager;
import android.telephony.SubscriptionManager.OnSubscriptionsChangedListener;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.text.TextUtils;
import android.util.EventLog;
import android.util.LocalLog;
import android.util.Pair;
import android.util.SparseArray;
import com.android.ims.ImsManager;
import com.android.internal.telephony.CommandException;
import com.android.internal.telephony.CommandException.Error;
import com.android.internal.telephony.DctConstants.Activity;
import com.android.internal.telephony.DctConstants.State;
import com.android.internal.telephony.EventLogTags;
import com.android.internal.telephony.GsmCdmaPhone;
import com.android.internal.telephony.ITelephony.Stub;
import com.android.internal.telephony.OemConstant;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.PhoneConstants.DataState;
import com.android.internal.telephony.PhoneFactory;
import com.android.internal.telephony.PhoneInternalInterface;
import com.android.internal.telephony.RIL;
import com.android.internal.telephony.ServiceStateTracker;
import com.android.internal.telephony.SubscriptionController;
import com.android.internal.telephony.TelephonyDevController;
import com.android.internal.telephony.TelephonyProto.TelephonyEvent.RilSetupDataCallResponse.RilDataCallFailCause;
import com.android.internal.telephony.metrics.TelephonyMetrics;
import com.android.internal.telephony.uicc.IccRecords;
import com.android.internal.telephony.uicc.UiccCardApplication;
import com.android.internal.telephony.uicc.UiccController;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.AsyncChannel;
import com.google.android.mms.pdu.CharacterSets;
import com.mediatek.common.MPlugin;
import com.mediatek.common.telephony.IGsmDCTExt;
import com.mediatek.common.telephony.ITelephonyExt;
import com.mediatek.internal.telephony.ITelephonyEx;
import com.mediatek.internal.telephony.ImsSwitchController;
import com.mediatek.internal.telephony.dataconnection.DataConnectionHelper;
import com.mediatek.internal.telephony.dataconnection.DataSubSelector;
import com.mediatek.internal.telephony.dataconnection.DcFailCauseManager;
import com.mediatek.internal.telephony.dataconnection.FdManager;
import com.mediatek.internal.telephony.dataconnection.IaExtendParam;
import com.mediatek.internal.telephony.gsm.GsmVTProvider;
import com.mediatek.internal.telephony.uicc.UsimPBMemInfo;
import com.mediatek.internal.telephony.worldphone.WorldPhoneUtil;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

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
public class DcTracker extends Handler {
    /* renamed from: -com-android-internal-telephony-DctConstants$StateSwitchesValues */
    private static final /* synthetic */ int[] f14-com-android-internal-telephony-DctConstants$StateSwitchesValues = null;
    private static final int APN_CHANGE_MILLIS = 1000;
    private static final int APN_CLASS_0 = 0;
    private static final int APN_CLASS_1 = 1;
    private static final int APN_CLASS_2 = 2;
    private static final int APN_CLASS_3 = 3;
    private static final int APN_CLASS_4 = 4;
    private static final int APN_CLASS_5 = 5;
    static final String APN_ID = "apn_id";
    private static final boolean BSP_PACKAGE = false;
    private static final int DATA_STALL_ALARM_AGGRESSIVE_DELAY_IN_MS_DEFAULT = 60000;
    private static final int DATA_STALL_ALARM_NON_AGGRESSIVE_DELAY_IN_MS_DEFAULT = 360000;
    private static final String DATA_STALL_ALARM_TAG_EXTRA = "data.stall.alram.tag";
    private static final boolean DATA_STALL_NOT_SUSPECTED = false;
    private static final boolean DATA_STALL_SUSPECTED = true;
    private static final boolean DBG = true;
    private static final String DEBUG_PROV_APN_ALARM = "persist.debug.prov_apn_alarm";
    private static final int DEFAULT_DATA_SIM_IDX = 2;
    private static final String[] HIGH_THROUGHPUT_APN = null;
    private static final String[] IMS_APN = null;
    private static final String INTENT_DATA_STALL_ALARM = "com.android.internal.telephony.data-stall";
    private static final String INTENT_PROVISIONING_APN_ALARM = "com.android.internal.telephony.provisioning_apn_alarm";
    private static final String INTENT_RECONNECT_ALARM = "com.android.internal.telephony.data-reconnect";
    private static final String INTENT_RECONNECT_ALARM_EXTRA_REASON = "reconnect_alarm_extra_reason";
    private static final String INTENT_RECONNECT_ALARM_EXTRA_TYPE = "reconnect_alarm_extra_type";
    private static final String LOG_TAG = "DCT";
    private static final int LTE_AS_CONNECTED = 1;
    private static final int MAX_ID_HIGH_TROUGHPUT = 1;
    private static final int MAX_ID_IMS_TROUGHPUT = 6;
    private static final int MAX_ID_OTHERS_TROUGHPUT = 3;
    protected static final String[] MCC_TABLE_DOMESTIC = null;
    protected static final String[] MCC_TABLE_TEST = null;
    private static final int MIN_ID_HIGH_TROUGHPUT = 0;
    private static final int MIN_ID_IMS_TROUGHPUT = 4;
    private static final int MIN_ID_OTHERS_TROUGHPUT = 2;
    private static final int MOBILE_DATA_IDX = 0;
    private static final boolean MTK_APNSYNC_TEST_SUPPORT = false;
    protected static boolean MTK_CC33_SUPPORT = false;
    private static final boolean MTK_DUAL_APN_SUPPORT = false;
    private static final boolean MTK_IMS_SUPPORT = false;
    protected static final boolean MTK_IMS_TESTMODE_SUPPORT = false;
    private static final String NETWORK_TYPE_MOBILE_IMS = "MOBILEIMS";
    private static final String NETWORK_TYPE_WIFI = "WIFI";
    private static final String NO_SIM_VALUE = "N/A";
    private static final int NUMBER_SENT_PACKETS_OF_HANG = 10;
    private static final String OPERATOR_OM = "OM";
    private static final int PDP_CONNECTION_POOL_SIZE = 3;
    private static final String PLMN_OP12 = "311480";
    private static final int POLL_NETSTAT_MILLIS = 1000;
    private static final int POLL_NETSTAT_SCREEN_OFF_MILLIS = 600000;
    private static final int POLL_PDP_MILLIS = 5000;
    static final Uri PREFERAPN_NO_UPDATE_URI_USING_SUBID = null;
    private static final String PROPERTY_FORCE_APN_CHANGE = "ril.force_apn_change";
    protected static final String PROPERTY_MOBILE_DATA_ENABLE = "persist.radio.mobile.data";
    private static final String PROPERTY_OPERATOR = "persist.operator.optr";
    private static final String PROPERTY_THROTTLING_APN_ENABLED = "ril.throttling.enabled";
    private static final String PROPERTY_THROTTLING_TIME = "persist.radio.throttling_time";
    private static final String PROPERTY_VSIM_ENABLE = "gsm.external.sim.inserted";
    private static final String PROP_APN_CLASS = "ril.md_changed_apn_class";
    private static final String PROP_APN_CLASS_ICCID = "ril.md_changed_apn_class.iccid";
    private static final int PROVISIONING_APN_ALARM_DELAY_IN_MS_DEFAULT = 900000;
    private static final String PROVISIONING_APN_ALARM_TAG_EXTRA = "provisioning.apn.alarm.tag";
    private static final int PROVISIONING_SPINNER_TIMEOUT_MILLIS = 120000;
    private static final String PUPPET_MASTER_RADIO_STRESS_TEST = "gsm.defaultpdpcontext.active";
    private static final boolean RADIO_TESTS = false;
    protected static final int REGION_DOMESTIC = 1;
    protected static final int REGION_FOREIGN = 2;
    protected static final int REGION_UNKNOWN = 0;
    private static final int ROAMING_DATA_IDX = 1;
    private static final int SKIP_DATA_SETTINGS = -2;
    private static final String SKIP_DATA_STALL_ALARM = "persist.skip.data.stall.alarm";
    private static final int TEL_DBG = 0;
    private static final boolean THROTTLING_APN_ENABLED = false;
    private static final int THROTTLING_MAX_PDP_SIZE = 8;
    private static final int THROTTLING_TIME_DEFAULT = 900;
    private static final boolean VDBG = false;
    private static final boolean VDBG_STALL = false;
    private static final String VZW_800_NI = "VZW800";
    private static final String VZW_ADMIN_NI = "VZWADMIN";
    private static final String VZW_APP_NI = "VZWAPP";
    private static final String VZW_EMERGENCY_NI = "VZWEMERGENCY";
    private static final boolean VZW_FEATURE = false;
    private static final String VZW_IMS_NI = "VZWIMS";
    private static final String VZW_INTERNET_NI = "VZWINTERNET";
    private static final String WIFI_SCORE_CHANGE = "android.net.wifi.WIFI_SCORE_CHANGE";
    public static boolean mDelayMeasure;
    public static int mLastDataRadioTech;
    public static NetworkCallback mMeasureDCCallback;
    public static boolean mMeasureDataState;
    public static boolean mVsimIgnoreUserDataSetting;
    private static int sEnableFailFastRefCounter;
    private final int LINGER_TIMER;
    private String[] MCCMNC_OP18;
    private String[] PLMN_EMPTY_APN_PCSCF_SET;
    private String[] PROPERTY_ICCID;
    protected String PROP_IMS_HANDOVER;
    private String RADIO_RESET_PROPERTY;
    private final int RETRY_TIMES;
    private final int WAITTING_TIMEOUT;
    private boolean bNeedTryDefaultForSinglePDN;
    public AtomicBoolean isCleanupRequired;
    private Activity mActivity;
    private final AlarmManager mAlarmManager;
    private ArrayList<ApnSetting> mAllApnSettings;
    private RegistrantList mAllDataDisconnectedRegistrants;
    private boolean mAllowConfig;
    private final ConcurrentHashMap<String, ApnContext> mApnContexts;
    private final SparseArray<ApnContext> mApnContextsById;
    private ApnChangeObserver mApnObserver;
    private HashMap<String, Integer> mApnToDataConnectionId;
    private AtomicBoolean mAttached;
    private AtomicBoolean mAutoAttachOnCreation;
    private boolean mAutoAttachOnCreationConfig;
    private boolean mCanSetPreferApn;
    private final ConnectivityManager mCm;
    private HashMap<Integer, DcAsyncChannel> mDataConnectionAcHashMap;
    private final Handler mDataConnectionTracker;
    private HashMap<Integer, DataConnection> mDataConnections;
    private final DataEnabledSettings mDataEnabledSettings;
    private PendingIntent mDataStallAlarmIntent;
    private int mDataStallAlarmTag;
    private volatile boolean mDataStallDetectionEnabled;
    private TxRxSum mDataStallTxRxSum;
    protected DcFailCauseManager mDcFcMgr;
    private HandlerThread mDcHandlerThread;
    private DcTesterFailBringUpAll mDcTesterFailBringUpAll;
    private DcController mDcc;
    private int mDefaultRefCount;
    private ArrayList<Message> mDisconnectAllCompleteMsgList;
    private int mDisconnectPendingCount;
    private ApnSetting mEmergencyApn;
    private volatile boolean mFailFast;
    protected FdManager mFdMgr;
    private IGsmDCTExt mGsmDctExt;
    private boolean mHasPsEverAttached;
    private AtomicInteger mHighThroughputIdGenerator;
    private final AtomicReference<IccRecords> mIccRecords;
    public boolean mImsRegistrationState;
    private ContentObserver mImsSwitchChangeObserver;
    private AtomicInteger mImsUniqueIdGenerator;
    private boolean mInVoiceCall;
    protected ApnSetting mInitialAttachApnSetting;
    private final BroadcastReceiver mIntentReceiver;
    private boolean mIsDisposed;
    private boolean mIsImsHandover;
    private boolean mIsLte;
    private boolean mIsProvisioning;
    private boolean mIsPsRestricted;
    private boolean mIsScreenOn;
    private boolean mIsSharedDefaultApn;
    private boolean mIsWifiConnected;
    private String mLteAccessStratumDataState;
    private ApnSetting mMdChangedAttachApn;
    private boolean mMeteredApnDisabled;
    private boolean mMvnoMatched;
    protected boolean mNeedsResumeModem;
    protected Object mNeedsResumeModemLock;
    private boolean mNetStatPollEnabled;
    private int mNetStatPollPeriod;
    private int mNetworkType;
    private int mNoRecvPollCount;
    private final OnSubscriptionsChangedListener mOnSubscriptionsChangedListener;
    private OppoUsageManager mOppoUsageManager;
    private AtomicInteger mOthersUniqueIdGenerator;
    private final Phone mPhone;
    private final Runnable mPollNetStat;
    private ApnSetting mPreferredApn;
    ArrayList<ApnContext> mPrioritySortedApnContexts;
    private final String mProvisionActionName;
    private BroadcastReceiver mProvisionBroadcastReceiver;
    private PendingIntent mProvisioningApnAlarmIntent;
    private int mProvisioningApnAlarmTag;
    private ProgressDialog mProvisioningSpinner;
    private String mProvisioningUrl;
    private PendingIntent mReconnectIntent;
    protected int mRegion;
    private AsyncChannel mReplyAc;
    private String mRequestedApnType;
    private boolean mReregisterOnReconnectFailure;
    private ContentResolver mResolver;
    private long mRxPkts;
    private long mSentSinceLastRecv;
    private final SettingsObserver mSettingsObserver;
    private State mState;
    private SubscriptionManager mSubscriptionManager;
    protected int mSuspendId;
    private TelephonyDevController mTelDevController;
    private ITelephonyExt mTelephonyExt;
    private long mTxPkts;
    protected AtomicReference<UiccCardApplication> mUiccCardApplication;
    private final UiccController mUiccController;
    private AtomicInteger mUniqueIdGenerator;
    private long mWifiConnectTimeStamp;
    private DetailedState mWifiOldState;
    private final int[] waitToRetry;

    /* renamed from: com.android.internal.telephony.dataconnection.DcTracker$1 */
    class AnonymousClass1 extends BroadcastReceiver {
        final /* synthetic */ DcTracker this$0;

        /* renamed from: com.android.internal.telephony.dataconnection.DcTracker$1$1 */
        class AnonymousClass1 extends Thread {
            /* renamed from: -com-android-internal-telephony-DctConstants$StateSwitchesValues */
            private static final /* synthetic */ int[] f15-com-android-internal-telephony-DctConstants$StateSwitchesValues = null;
            final /* synthetic */ int[] $SWITCH_TABLE$com$android$internal$telephony$DctConstants$State;
            final /* synthetic */ AnonymousClass1 this$1;

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.dataconnection.DcTracker.1.1.-getcom-android-internal-telephony-DctConstants$StateSwitchesValues():int[], dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 11 more
                */
            /* renamed from: -getcom-android-internal-telephony-DctConstants$StateSwitchesValues */
            private static /* synthetic */ int[] m41xf0fbc33d() {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.dataconnection.DcTracker.1.1.-getcom-android-internal-telephony-DctConstants$StateSwitchesValues():int[], dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.dataconnection.DcTracker.1.1.-getcom-android-internal-telephony-DctConstants$StateSwitchesValues():int[]");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.dataconnection.DcTracker.1.1.<init>(com.android.internal.telephony.dataconnection.DcTracker$1):void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 11 more
                */
            AnonymousClass1(com.android.internal.telephony.dataconnection.DcTracker.AnonymousClass1 r1) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.dataconnection.DcTracker.1.1.<init>(com.android.internal.telephony.dataconnection.DcTracker$1):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.dataconnection.DcTracker.1.1.<init>(com.android.internal.telephony.dataconnection.DcTracker$1):void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.internal.telephony.dataconnection.DcTracker.1.1.run():void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 11 more
                */
            public void run() {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.internal.telephony.dataconnection.DcTracker.1.1.run():void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.dataconnection.DcTracker.1.1.run():void");
            }
        }

        AnonymousClass1(DcTracker this$0) {
            this.this$0 = this$0;
        }

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            NetworkInfo networkInfo;
            if (action.equals("android.intent.action.SCREEN_ON")) {
                this.this$0.log("screen on");
                this.this$0.mIsScreenOn = true;
                this.this$0.stopNetStatPoll();
                this.this$0.startNetStatPoll();
                this.this$0.restartDataStallAlarm();
            } else if (action.equals("android.intent.action.SCREEN_OFF")) {
                this.this$0.log("screen off");
                this.this$0.mIsScreenOn = false;
                this.this$0.stopNetStatPoll();
                this.this$0.startNetStatPoll();
                this.this$0.restartDataStallAlarm();
            } else if (action.startsWith(DcTracker.INTENT_RECONNECT_ALARM)) {
                this.this$0.log("Reconnect alarm. Previous state was " + this.this$0.mState);
                this.this$0.onActionIntentReconnectAlarm(intent);
            } else if (action.equals(DcTracker.INTENT_DATA_STALL_ALARM)) {
                this.this$0.log("Data stall alarm");
                this.this$0.onActionIntentDataStallAlarm(intent);
            } else if (action.equals(DcTracker.INTENT_PROVISIONING_APN_ALARM)) {
                this.this$0.log("Provisioning apn alarm");
                this.this$0.onActionIntentProvisioningApnAlarm(intent);
            } else if (action.equals("android.net.wifi.STATE_CHANGE")) {
                networkInfo = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                this.this$0.mIsWifiConnected = networkInfo != null ? networkInfo.isConnected() : false;
                DetailedState state = networkInfo.getDetailedState();
                this.this$0.log("NETWORK_STATE_CHANGED_ACTION: mIsWifiConnected=" + this.this$0.mIsWifiConnected + " state:" + state + " mWifiOldState:" + this.this$0.mWifiOldState);
                if (OemConstant.getWlanAssistantEnable(this.this$0.mPhone.getContext())) {
                    if (!this.this$0.mIsWifiConnected && state == DetailedState.DISCONNECTED) {
                        DcTracker.mMeasureDataState = false;
                        this.this$0.mCm.shouldKeepCelluarNetwork(DcTracker.mMeasureDataState);
                    } else if (!(this.this$0.mWifiOldState == DetailedState.CONNECTED || state != DetailedState.CONNECTED || DcTracker.mMeasureDCCallback == null)) {
                        this.this$0.mWifiConnectTimeStamp = SystemClock.elapsedRealtime();
                        this.this$0.log("WLAN+ NETWORK_STATE_CHANGED_ACTION release DC: mMeasureDataState=" + DcTracker.mMeasureDataState);
                        try {
                            this.this$0.mCm.unregisterNetworkCallback(DcTracker.mMeasureDCCallback);
                        } catch (IllegalArgumentException e) {
                            this.this$0.log("WLAN+ " + e.toString());
                        } catch (Exception e2) {
                            this.this$0.log("WLAN+ Exception:" + e2.toString());
                        }
                        DcTracker.mMeasureDCCallback = null;
                    }
                }
                if (state == DetailedState.DISCONNECTED && this.this$0.mWifiOldState == DetailedState.CONNECTED && (this.this$0.getDataEnabled() || this.this$0.haveVsimIgnoreUserDataSetting())) {
                    this.this$0.setupDataOnConnectableApns("android.net.wifi.STATE_CHANGE");
                }
                this.this$0.mWifiOldState = state;
            } else if (action.equals("android.net.wifi.WIFI_STATE_CHANGED")) {
                this.this$0.log("Wifi state changed");
                boolean enabled = intent.getIntExtra("wifi_state", 4) == 3;
                if (!enabled) {
                    this.this$0.mIsWifiConnected = false;
                }
                this.this$0.log("WIFI_STATE_CHANGED_ACTION: enabled=" + enabled + " mIsWifiConnected=" + this.this$0.mIsWifiConnected);
                if (OemConstant.getWlanAssistantEnable(this.this$0.mPhone.getContext()) && !enabled) {
                    DcTracker.mMeasureDataState = false;
                    this.this$0.mCm.shouldKeepCelluarNetwork(DcTracker.mMeasureDataState);
                }
            } else if (action.equals(DcTracker.WIFI_SCORE_CHANGE)) {
                SubscriptionManager su = SubscriptionManager.from(this.this$0.mPhone.getContext());
                boolean isDefaultDataPhone = this.this$0.mPhone.getSubId() == SubscriptionManager.getDefaultDataSubId();
                DcTracker.mMeasureDataState = intent.getBooleanExtra("enableData", false);
                this.this$0.mCm.shouldKeepCelluarNetwork(DcTracker.mMeasureDataState);
                if (isDefaultDataPhone) {
                    new AnonymousClass1(this).start();
                }
            } else if (!action.equals("android.net.conn.CONNECTIVITY_CHANGE")) {
                this.this$0.log("onReceive: Unknown action=" + action);
            } else if (!this.this$0.hasOperatorIaCapability()) {
                networkInfo = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                int apnType = networkInfo.getType();
                String typeName = networkInfo.getTypeName();
                this.this$0.logd("onReceive: ConnectivityService action change apnType = " + apnType + " typename =" + typeName);
                if (apnType == 11 && typeName.equals(DcTracker.NETWORK_TYPE_WIFI)) {
                    this.this$0.onAttachApnChangedByHandover(true);
                } else if (apnType == 11 && typeName.equals(DcTracker.NETWORK_TYPE_MOBILE_IMS)) {
                    this.this$0.onAttachApnChangedByHandover(false);
                }
            }
        }
    }

    /* renamed from: com.android.internal.telephony.dataconnection.DcTracker$2 */
    class AnonymousClass2 implements Runnable {
        final /* synthetic */ DcTracker this$0;

        AnonymousClass2(DcTracker this$0) {
            this.this$0 = this$0;
        }

        public void run() {
            this.this$0.updateDataActivity();
            if (this.this$0.mIsScreenOn) {
                this.this$0.mNetStatPollPeriod = Global.getInt(this.this$0.mResolver, "pdp_watchdog_poll_interval_ms", 1000);
            } else {
                this.this$0.mNetStatPollPeriod = Global.getInt(this.this$0.mResolver, "pdp_watchdog_long_poll_interval_ms", 600000);
            }
            if (this.this$0.mNetStatPollEnabled) {
                this.this$0.mDataConnectionTracker.postDelayed(this, (long) this.this$0.mNetStatPollPeriod);
            }
        }
    }

    /* renamed from: com.android.internal.telephony.dataconnection.DcTracker$3 */
    class AnonymousClass3 extends OnSubscriptionsChangedListener {
        public final AtomicInteger mPreviousSubId;
        final /* synthetic */ DcTracker this$0;

        AnonymousClass3(DcTracker this$0) {
            this.this$0 = this$0;
            this.mPreviousSubId = new AtomicInteger(-1);
        }

        public void onSubscriptionsChanged() {
            this.this$0.log("SubscriptionListener.onSubscriptionInfoChanged start");
            int subId = this.this$0.mPhone.getSubId();
            if (SubscriptionManager.isValidSubscriptionId(subId)) {
                this.this$0.registerSettingsObserver();
            }
            IccRecords r = (IccRecords) this.this$0.mIccRecords.get();
            String operatorNumericIcc = r != null ? r.getOperatorNumeric() : UsimPBMemInfo.STRING_NOT_SET;
            String operatorNumericProp = TelephonyManager.getDefault().getSimOperatorNumericForPhone(this.this$0.mPhone.getPhoneId());
            if (this.mPreviousSubId.getAndSet(subId) != subId && SubscriptionManager.isValidSubscriptionId(subId) && !TextUtils.isEmpty(operatorNumericIcc) && !TextUtils.isEmpty(operatorNumericProp)) {
                this.this$0.onRecordsLoadedOrSubIdChanged();
            }
        }
    }

    /* renamed from: com.android.internal.telephony.dataconnection.DcTracker$5 */
    class AnonymousClass5 extends Thread {
        final /* synthetic */ DcTracker this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.dataconnection.DcTracker.5.<init>(com.android.internal.telephony.dataconnection.DcTracker):void, dex: 
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
        AnonymousClass5(com.android.internal.telephony.dataconnection.DcTracker r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.dataconnection.DcTracker.5.<init>(com.android.internal.telephony.dataconnection.DcTracker):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.dataconnection.DcTracker.5.<init>(com.android.internal.telephony.dataconnection.DcTracker):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.internal.telephony.dataconnection.DcTracker.5.run():void, dex: 
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
        public void run() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.internal.telephony.dataconnection.DcTracker.5.run():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.dataconnection.DcTracker.5.run():void");
        }
    }

    /* renamed from: com.android.internal.telephony.dataconnection.DcTracker$6 */
    class AnonymousClass6 implements Comparator<ApnContext> {
        final /* synthetic */ DcTracker this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.dataconnection.DcTracker.6.<init>(com.android.internal.telephony.dataconnection.DcTracker):void, dex: 
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
        AnonymousClass6(com.android.internal.telephony.dataconnection.DcTracker r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.dataconnection.DcTracker.6.<init>(com.android.internal.telephony.dataconnection.DcTracker):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.dataconnection.DcTracker.6.<init>(com.android.internal.telephony.dataconnection.DcTracker):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: com.android.internal.telephony.dataconnection.DcTracker.6.compare(com.android.internal.telephony.dataconnection.ApnContext, com.android.internal.telephony.dataconnection.ApnContext):int, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e3
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public int compare(com.android.internal.telephony.dataconnection.ApnContext r1, com.android.internal.telephony.dataconnection.ApnContext r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: com.android.internal.telephony.dataconnection.DcTracker.6.compare(com.android.internal.telephony.dataconnection.ApnContext, com.android.internal.telephony.dataconnection.ApnContext):int, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.dataconnection.DcTracker.6.compare(com.android.internal.telephony.dataconnection.ApnContext, com.android.internal.telephony.dataconnection.ApnContext):int");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.dataconnection.DcTracker.6.compare(java.lang.Object, java.lang.Object):int, dex: 
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
        public /* bridge */ /* synthetic */ int compare(java.lang.Object r1, java.lang.Object r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.dataconnection.DcTracker.6.compare(java.lang.Object, java.lang.Object):int, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.dataconnection.DcTracker.6.compare(java.lang.Object, java.lang.Object):int");
        }
    }

    /* renamed from: com.android.internal.telephony.dataconnection.DcTracker$7 */
    class AnonymousClass7 extends Thread {
        final /* synthetic */ DcTracker this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.dataconnection.DcTracker.7.<init>(com.android.internal.telephony.dataconnection.DcTracker):void, dex: 
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
        AnonymousClass7(com.android.internal.telephony.dataconnection.DcTracker r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.dataconnection.DcTracker.7.<init>(com.android.internal.telephony.dataconnection.DcTracker):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.dataconnection.DcTracker.7.<init>(com.android.internal.telephony.dataconnection.DcTracker):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.internal.telephony.dataconnection.DcTracker.7.run():void, dex: 
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
        public void run() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.internal.telephony.dataconnection.DcTracker.7.run():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.dataconnection.DcTracker.7.run():void");
        }
    }

    /* renamed from: com.android.internal.telephony.dataconnection.DcTracker$8 */
    class AnonymousClass8 implements Runnable {
        final /* synthetic */ DcTracker this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.dataconnection.DcTracker.8.<init>(com.android.internal.telephony.dataconnection.DcTracker):void, dex: 
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
        AnonymousClass8(com.android.internal.telephony.dataconnection.DcTracker r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.dataconnection.DcTracker.8.<init>(com.android.internal.telephony.dataconnection.DcTracker):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.dataconnection.DcTracker.8.<init>(com.android.internal.telephony.dataconnection.DcTracker):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.internal.telephony.dataconnection.DcTracker.8.run():void, dex: 
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
        public void run() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.internal.telephony.dataconnection.DcTracker.8.run():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.dataconnection.DcTracker.8.run():void");
        }
    }

    /* renamed from: com.android.internal.telephony.dataconnection.DcTracker$9 */
    class AnonymousClass9 extends Thread {
        final /* synthetic */ DcTracker this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.dataconnection.DcTracker.9.<init>(com.android.internal.telephony.dataconnection.DcTracker, java.lang.String):void, dex: 
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
        AnonymousClass9(com.android.internal.telephony.dataconnection.DcTracker r1, java.lang.String r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.dataconnection.DcTracker.9.<init>(com.android.internal.telephony.dataconnection.DcTracker, java.lang.String):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.dataconnection.DcTracker.9.<init>(com.android.internal.telephony.dataconnection.DcTracker, java.lang.String):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.internal.telephony.dataconnection.DcTracker.9.run():void, dex: 
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
        public void run() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.internal.telephony.dataconnection.DcTracker.9.run():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.dataconnection.DcTracker.9.run():void");
        }
    }

    private class ApnChangeObserver extends ContentObserver {
        final /* synthetic */ DcTracker this$0;

        public ApnChangeObserver(DcTracker this$0) {
            this.this$0 = this$0;
            super(this$0.mDataConnectionTracker);
        }

        public void onChange(boolean selfChange) {
            this.this$0.removeMessages(270355);
            this.this$0.sendMessageDelayed(this.this$0.obtainMessage(270355), 1000);
        }
    }

    public static class DataAllowFailReason {
        private HashSet<DataAllowFailReasonType> mDataAllowFailReasonSet;

        public DataAllowFailReason() {
            this.mDataAllowFailReasonSet = new HashSet();
        }

        public void addDataAllowFailReason(DataAllowFailReasonType type) {
            this.mDataAllowFailReasonSet.add(type);
        }

        public String getDataAllowFailReason() {
            StringBuilder failureReason = new StringBuilder();
            failureReason.append("isDataAllowed: No");
            for (DataAllowFailReasonType reason : this.mDataAllowFailReasonSet) {
                failureReason.append(reason.mFailReasonStr);
            }
            return failureReason.toString();
        }

        public boolean isFailForSingleReason(DataAllowFailReasonType failReasonType) {
            if (this.mDataAllowFailReasonSet.size() == 1) {
                return this.mDataAllowFailReasonSet.contains(failReasonType);
            }
            return false;
        }

        public boolean isFailForReason(DataAllowFailReasonType failReasonType) {
            return this.mDataAllowFailReasonSet.contains(failReasonType);
        }

        public void clearAllReasons() {
            this.mDataAllowFailReasonSet.clear();
        }

        public boolean isFailed() {
            return this.mDataAllowFailReasonSet.size() > 0;
        }

        public int getSizeOfFailReason() {
            return this.mDataAllowFailReasonSet.size();
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
    public enum DataAllowFailReasonType {
        ;
        
        public String mFailReasonStr;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.internal.telephony.dataconnection.DcTracker.DataAllowFailReasonType.<clinit>():void, dex: 
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
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.internal.telephony.dataconnection.DcTracker.DataAllowFailReasonType.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.dataconnection.DcTracker.DataAllowFailReasonType.<clinit>():void");
        }

        private DataAllowFailReasonType(String reason) {
            this.mFailReasonStr = reason;
        }
    }

    private class ProvisionNotificationBroadcastReceiver extends BroadcastReceiver {
        private final String mNetworkOperator;
        private final String mProvisionUrl;
        final /* synthetic */ DcTracker this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: com.android.internal.telephony.dataconnection.DcTracker.ProvisionNotificationBroadcastReceiver.<init>(com.android.internal.telephony.dataconnection.DcTracker, java.lang.String, java.lang.String):void, dex:  in method: com.android.internal.telephony.dataconnection.DcTracker.ProvisionNotificationBroadcastReceiver.<init>(com.android.internal.telephony.dataconnection.DcTracker, java.lang.String, java.lang.String):void, dex: 
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
            Caused by: jadx.core.utils.exceptions.DecodeException: null in method: com.android.internal.telephony.dataconnection.DcTracker.ProvisionNotificationBroadcastReceiver.<init>(com.android.internal.telephony.dataconnection.DcTracker, java.lang.String, java.lang.String):void, dex: 
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
            	... 10 more
            Caused by: java.io.EOFException
            	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
            	at com.android.dx.io.instructions.ShortArrayCodeInput.readInt(ShortArrayCodeInput.java:62)
            	at com.android.dx.io.instructions.InstructionCodec$22.decode(InstructionCodec.java:490)
            	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
            	... 11 more
            */
        public ProvisionNotificationBroadcastReceiver(com.android.internal.telephony.dataconnection.DcTracker r1, java.lang.String r2, java.lang.String r3) {
            /*
            // Can't load method instructions: Load method exception: null in method: com.android.internal.telephony.dataconnection.DcTracker.ProvisionNotificationBroadcastReceiver.<init>(com.android.internal.telephony.dataconnection.DcTracker, java.lang.String, java.lang.String):void, dex:  in method: com.android.internal.telephony.dataconnection.DcTracker.ProvisionNotificationBroadcastReceiver.<init>(com.android.internal.telephony.dataconnection.DcTracker, java.lang.String, java.lang.String):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.dataconnection.DcTracker.ProvisionNotificationBroadcastReceiver.<init>(com.android.internal.telephony.dataconnection.DcTracker, java.lang.String, java.lang.String):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.internal.telephony.dataconnection.DcTracker.ProvisionNotificationBroadcastReceiver.enableMobileProvisioning():void, dex: 
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
        private void enableMobileProvisioning() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.internal.telephony.dataconnection.DcTracker.ProvisionNotificationBroadcastReceiver.enableMobileProvisioning():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.dataconnection.DcTracker.ProvisionNotificationBroadcastReceiver.enableMobileProvisioning():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.internal.telephony.dataconnection.DcTracker.ProvisionNotificationBroadcastReceiver.setEnableFailFastMobileData(int):void, dex: 
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
        private void setEnableFailFastMobileData(int r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.internal.telephony.dataconnection.DcTracker.ProvisionNotificationBroadcastReceiver.setEnableFailFastMobileData(int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.dataconnection.DcTracker.ProvisionNotificationBroadcastReceiver.setEnableFailFastMobileData(int):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.internal.telephony.dataconnection.DcTracker.ProvisionNotificationBroadcastReceiver.onReceive(android.content.Context, android.content.Intent):void, dex: 
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
        public void onReceive(android.content.Context r1, android.content.Intent r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.internal.telephony.dataconnection.DcTracker.ProvisionNotificationBroadcastReceiver.onReceive(android.content.Context, android.content.Intent):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.dataconnection.DcTracker.ProvisionNotificationBroadcastReceiver.onReceive(android.content.Context, android.content.Intent):void");
        }
    }

    private static class RecoveryAction {
        public static final int CLEANUP = 1;
        public static final int GET_DATA_CALL_LIST = 0;
        public static final int RADIO_RESTART = 3;
        public static final int RADIO_RESTART_WITH_PROP = 4;
        public static final int REREGISTER = 2;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.internal.telephony.dataconnection.DcTracker.RecoveryAction.<init>():void, dex: 
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
        private RecoveryAction() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.internal.telephony.dataconnection.DcTracker.RecoveryAction.<init>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.dataconnection.DcTracker.RecoveryAction.<init>():void");
        }

        private static boolean isAggressiveRecovery(int value) {
            if (value == 1 || value == 2 || value == 3 || value == 4) {
                return true;
            }
            return false;
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
    private enum RetryFailures {
        ;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.internal.telephony.dataconnection.DcTracker.RetryFailures.<clinit>():void, dex: 
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
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.internal.telephony.dataconnection.DcTracker.RetryFailures.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.dataconnection.DcTracker.RetryFailures.<clinit>():void");
        }
    }

    private static class SettingsObserver extends ContentObserver {
        private static final String TAG = "DcTracker.SettingsObserver";
        private final Context mContext;
        private final Handler mHandler;
        private final HashMap<Uri, Integer> mUriEventMap;

        SettingsObserver(Context context, Handler handler) {
            super(null);
            this.mUriEventMap = new HashMap();
            this.mContext = context;
            this.mHandler = handler;
        }

        void observe(Uri uri, int what) {
            this.mUriEventMap.put(uri, Integer.valueOf(what));
            this.mContext.getContentResolver().registerContentObserver(uri, false, this);
        }

        void unobserve() {
            this.mContext.getContentResolver().unregisterContentObserver(this);
        }

        public void onChange(boolean selfChange) {
            Rlog.e(TAG, "Should never be reached.");
        }

        public void onChange(boolean selfChange, Uri uri) {
            Integer what = (Integer) this.mUriEventMap.get(uri);
            if (what != null) {
                this.mHandler.obtainMessage(what.intValue()).sendToTarget();
            } else {
                Rlog.e(TAG, "No matching event to send for URI=" + uri);
            }
        }
    }

    public static class TxRxSum {
        public long rxPkts;
        public long txPkts;

        public TxRxSum() {
            reset();
        }

        public TxRxSum(long txPkts, long rxPkts) {
            this.txPkts = txPkts;
            this.rxPkts = rxPkts;
        }

        public TxRxSum(TxRxSum sum) {
            this.txPkts = sum.txPkts;
            this.rxPkts = sum.rxPkts;
        }

        public void reset() {
            this.txPkts = -1;
            this.rxPkts = -1;
        }

        public String toString() {
            return "{txSum=" + this.txPkts + " rxSum=" + this.rxPkts + "}";
        }

        public void updateTxRxSum() {
            if (OemConstant.FEATURE_MTK_CTA_SUPPORT) {
                this.txPkts = TrafficStats.getMobileTxPackets();
                this.rxPkts = TrafficStats.getMobileRxPackets();
                return;
            }
            this.txPkts = TrafficStats.getMobileTcpTxPackets();
            this.rxPkts = TrafficStats.getMobileTcpRxPackets();
        }
    }

    /* renamed from: -getcom-android-internal-telephony-DctConstants$StateSwitchesValues */
    private static /* synthetic */ int[] m40xf0fbc33d() {
        if (f14-com-android-internal-telephony-DctConstants$StateSwitchesValues != null) {
            return f14-com-android-internal-telephony-DctConstants$StateSwitchesValues;
        }
        int[] iArr = new int[State.values().length];
        try {
            iArr[State.CONNECTED.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[State.CONNECTING.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[State.DISCONNECTING.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[State.FAILED.ordinal()] = 4;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[State.IDLE.ordinal()] = 5;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[State.RETRYING.ordinal()] = 6;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[State.SCANNING.ordinal()] = 7;
        } catch (NoSuchFieldError e7) {
        }
        f14-com-android-internal-telephony-DctConstants$StateSwitchesValues = iArr;
        return iArr;
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.dataconnection.DcTracker.<clinit>():void, dex: 
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
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 9 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.dataconnection.DcTracker.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.dataconnection.DcTracker.<clinit>():void");
    }

    private void registerSettingsObserver() {
        this.mSettingsObserver.unobserve();
        String simSuffix = UsimPBMemInfo.STRING_NOT_SET;
        if (TelephonyManager.getDefault().getSimCount() > 1) {
            simSuffix = Integer.toString(this.mPhone.getSubId());
        }
        this.mSettingsObserver.observe(Global.getUriFor(SimInfo.DATA_ROAMING + simSuffix), 270347);
        this.mSettingsObserver.observe(Global.getUriFor("device_provisioned"), 270379);
        this.mSettingsObserver.observe(Global.getUriFor("device_provisioning_mobile_data"), 270379);
    }

    private void onActionIntentReconnectAlarm(Intent intent) {
        String reason = intent.getStringExtra(INTENT_RECONNECT_ALARM_EXTRA_REASON);
        String apnType = intent.getStringExtra(INTENT_RECONNECT_ALARM_EXTRA_TYPE);
        int phoneSubId = this.mPhone.getSubId();
        int currSubId = intent.getIntExtra("subscription", -1);
        log("onActionIntentReconnectAlarm: currSubId = " + currSubId + " phoneSubId=" + phoneSubId);
        if (SubscriptionManager.isValidSubscriptionId(currSubId) && currSubId == phoneSubId) {
            ApnContext apnContext = (ApnContext) this.mApnContexts.get(apnType);
            log("onActionIntentReconnectAlarm: mState=" + this.mState + " reason=" + reason + " apnType=" + apnType + " apnContext=" + apnContext + " mDataConnectionAsyncChannels=" + this.mDataConnectionAcHashMap);
            if (apnContext != null && apnContext.isEnabled()) {
                apnContext.setReason(reason);
                State apnContextState = apnContext.getState();
                log("onActionIntentReconnectAlarm: apnContext state=" + apnContextState);
                if (apnContextState == State.FAILED || apnContextState == State.IDLE) {
                    log("onActionIntentReconnectAlarm: state is FAILED|IDLE, disassociate");
                    DcAsyncChannel dcac = apnContext.getDcAc();
                    if (dcac != null) {
                        log("onActionIntentReconnectAlarm: tearDown apnContext=" + apnContext);
                        dcac.tearDown(apnContext, UsimPBMemInfo.STRING_NOT_SET, null);
                    }
                    apnContext.setDataConnectionAc(null);
                    apnContext.setState(State.IDLE);
                } else {
                    log("onActionIntentReconnectAlarm: keep associated");
                }
                sendMessage(obtainMessage(270339, apnContext));
                apnContext.setReconnectIntent(null);
            }
            return;
        }
        log("receive ReconnectAlarm but subId incorrect, ignore");
    }

    private void onActionIntentDataStallAlarm(Intent intent) {
        if (VDBG_STALL) {
            log("onActionIntentDataStallAlarm: action=" + intent.getAction());
        }
        Message msg = obtainMessage(270353, intent.getAction());
        msg.arg1 = intent.getIntExtra(DATA_STALL_ALARM_TAG_EXTRA, 0);
        sendMessage(msg);
    }

    public DcTracker(Phone phone) {
        this.isCleanupRequired = new AtomicBoolean(false);
        this.mRequestedApnType = "default";
        this.mDataEnabledSettings = new DataEnabledSettings();
        this.RADIO_RESET_PROPERTY = "gsm.radioreset";
        this.mWifiOldState = DetailedState.IDLE;
        this.mWifiConnectTimeStamp = 0;
        this.LINGER_TIMER = 3000;
        this.RETRY_TIMES = 3;
        this.waitToRetry = new int[]{3, 30, 60};
        this.WAITTING_TIMEOUT = 60;
        this.mPrioritySortedApnContexts = new ArrayList();
        this.mAllApnSettings = null;
        this.mPreferredApn = null;
        this.mIsPsRestricted = false;
        this.mEmergencyApn = null;
        this.mIsDisposed = false;
        this.mIsProvisioning = false;
        this.mProvisioningUrl = null;
        this.mProvisioningApnAlarmIntent = null;
        this.mProvisioningApnAlarmTag = (int) SystemClock.elapsedRealtime();
        this.mReplyAc = new AsyncChannel();
        String[] strArr = new String[4];
        strArr[0] = "ril.iccid.sim1";
        strArr[1] = "ril.iccid.sim2";
        strArr[2] = "ril.iccid.sim3";
        strArr[3] = "ril.iccid.sim4";
        this.PROPERTY_ICCID = strArr;
        this.mIsImsHandover = false;
        this.PROP_IMS_HANDOVER = "ril.imshandover";
        this.mMdChangedAttachApn = null;
        this.mLteAccessStratumDataState = "unknown";
        this.mNetworkType = -1;
        this.mIsLte = false;
        this.mIsSharedDefaultApn = false;
        this.mDefaultRefCount = 0;
        this.mSuspendId = 0;
        this.mRegion = 0;
        this.mNeedsResumeModemLock = new Object();
        this.mNeedsResumeModem = false;
        strArr = new String[2];
        strArr[0] = "26201";
        strArr[1] = "44010";
        this.PLMN_EMPTY_APN_PCSCF_SET = strArr;
        strArr = new String[22];
        strArr[0] = "405840";
        strArr[1] = "405854";
        strArr[2] = "405855";
        strArr[3] = "405856";
        strArr[4] = "405857";
        strArr[5] = "405858";
        strArr[6] = "405859";
        strArr[7] = "405860";
        strArr[8] = "405861";
        strArr[9] = "405862";
        strArr[10] = "405863";
        strArr[11] = "405864";
        strArr[12] = "405865";
        strArr[13] = "405866";
        strArr[14] = "405867";
        strArr[15] = "405868";
        strArr[16] = "405869";
        strArr[17] = "405870";
        strArr[18] = "405871";
        strArr[19] = "405872";
        strArr[20] = "405873";
        strArr[21] = "405874";
        this.MCCMNC_OP18 = strArr;
        this.mHasPsEverAttached = false;
        this.mTelDevController = TelephonyDevController.getInstance();
        this.mIntentReceiver = new AnonymousClass1(this);
        this.mPollNetStat = new AnonymousClass2(this);
        this.mOnSubscriptionsChangedListener = new AnonymousClass3(this);
        this.mDisconnectAllCompleteMsgList = new ArrayList();
        this.mAllDataDisconnectedRegistrants = new RegistrantList();
        this.mIccRecords = new AtomicReference();
        this.mUiccCardApplication = new AtomicReference();
        this.mActivity = Activity.NONE;
        this.mState = State.IDLE;
        this.mNetStatPollEnabled = false;
        this.mDataStallTxRxSum = new TxRxSum(0, 0);
        this.mDataStallAlarmTag = (int) SystemClock.elapsedRealtime();
        this.mDataStallAlarmIntent = null;
        this.mNoRecvPollCount = 0;
        this.mDataStallDetectionEnabled = true;
        this.mFailFast = false;
        this.mInVoiceCall = false;
        this.mIsWifiConnected = false;
        this.mReconnectIntent = null;
        this.mAutoAttachOnCreationConfig = false;
        this.mAutoAttachOnCreation = new AtomicBoolean(false);
        this.mIsScreenOn = true;
        this.mMvnoMatched = false;
        this.mUniqueIdGenerator = new AtomicInteger(0);
        this.mDataConnections = new HashMap();
        this.mDataConnectionAcHashMap = new HashMap();
        this.mApnToDataConnectionId = new HashMap();
        this.mApnContexts = new ConcurrentHashMap();
        this.mApnContextsById = new SparseArray();
        this.mDisconnectPendingCount = 0;
        this.mMeteredApnDisabled = false;
        this.mAllowConfig = false;
        this.mOppoUsageManager = null;
        this.mImsSwitchChangeObserver = new ContentObserver(this, new Handler()) {
            final /* synthetic */ DcTracker this$0;

            public void onChange(boolean selfChange) {
                this.this$0.log("mImsSwitchChangeObserver: onChange=" + selfChange);
                if (this.this$0.isOp17IaSupport()) {
                    this.this$0.log("IA : OP17, set IA");
                    this.this$0.setInitialAttachApn();
                }
            }
        };
        this.mReregisterOnReconnectFailure = false;
        this.mCanSetPreferApn = false;
        this.mAttached = new AtomicBoolean(false);
        this.mImsRegistrationState = false;
        this.mHighThroughputIdGenerator = new AtomicInteger(0);
        this.mOthersUniqueIdGenerator = new AtomicInteger(2);
        this.mImsUniqueIdGenerator = new AtomicInteger(4);
        this.bNeedTryDefaultForSinglePDN = false;
        this.mPhone = phone;
        log("DCT.constructor");
        this.mResolver = this.mPhone.getContext().getContentResolver();
        this.mUiccController = UiccController.getInstance();
        this.mUiccController.registerForIccChanged(this, 270369, null);
        this.mAlarmManager = (AlarmManager) this.mPhone.getContext().getSystemService("alarm");
        this.mCm = (ConnectivityManager) this.mPhone.getContext().getSystemService("connectivity");
        IntentFilter filter = new IntentFilter();
        if (OemConstant.getWlanAssistantEnable(this.mPhone.getContext())) {
            filter.addAction(WIFI_SCORE_CHANGE);
        }
        filter.addAction("android.net.wifi.STATE_CHANGE");
        filter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
        filter.addAction(INTENT_DATA_STALL_ALARM);
        filter.addAction(INTENT_PROVISIONING_APN_ALARM);
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        this.mDataEnabledSettings.setUserDataEnabled(getDataEnabled());
        this.mPhone.getContext().registerReceiver(this.mIntentReceiver, filter, null, this.mPhone);
        this.mAutoAttachOnCreation.set(PreferenceManager.getDefaultSharedPreferences(this.mPhone.getContext()).getBoolean(Phone.DATA_DISABLED_ON_BOOT_KEY, false));
        this.mSubscriptionManager = SubscriptionManager.from(this.mPhone.getContext());
        this.mSubscriptionManager.addOnSubscriptionsChangedListener(this.mOnSubscriptionsChangedListener);
        this.mDcHandlerThread = new HandlerThread("DcHandlerThread");
        this.mDcHandlerThread.start();
        Handler dcHandler = new Handler(this.mDcHandlerThread.getLooper());
        this.mDcc = DcController.makeDcc(this.mPhone, this, dcHandler);
        this.mDcTesterFailBringUpAll = new DcTesterFailBringUpAll(this.mPhone, dcHandler);
        logd("DualApnSupport = " + MTK_DUAL_APN_SUPPORT);
        this.mDataConnectionTracker = this;
        registerForAllEvents();
        update();
        this.mApnObserver = new ApnChangeObserver(this);
        this.mOppoUsageManager = OppoUsageManager.getOppoUsageManager();
        phone.getContext().getContentResolver().registerContentObserver(Carriers.CONTENT_URI, true, this.mApnObserver);
        if (!hasOperatorIaCapability()) {
            phone.getContext().getContentResolver().registerContentObserver(Global.getUriFor("volte_vt_enabled"), true, this.mImsSwitchChangeObserver);
        }
        initApnContexts();
        for (ApnContext apnContext : this.mApnContexts.values()) {
            filter = new IntentFilter();
            filter.addAction("com.android.internal.telephony.data-reconnect." + apnContext.getApnType());
            this.mPhone.getContext().registerReceiver(this.mIntentReceiver, filter, null, this.mPhone);
        }
        this.mDcFcMgr = DcFailCauseManager.getInstance(this.mPhone);
        initEmergencyApnSettingAsync();
        this.mFdMgr = FdManager.getInstance(phone);
        if (!BSP_PACKAGE) {
            try {
                this.mGsmDctExt = (IGsmDCTExt) MPlugin.createInstance(IGsmDCTExt.class.getName(), this.mPhone.getContext());
                this.mTelephonyExt = (ITelephonyExt) MPlugin.createInstance(ITelephonyExt.class.getName(), this.mPhone.getContext());
                this.mTelephonyExt.init(this.mPhone.getContext());
                this.mTelephonyExt.startDataRoamingStrategy(this.mPhone);
            } catch (Exception e) {
                logw("mGsmDctExt or mTelephonyExt init fail");
                e.printStackTrace();
            }
        }
        this.mProvisionActionName = "com.android.internal.telephony.PROVISION" + phone.getPhoneId();
        this.mSettingsObserver = new SettingsObserver(this.mPhone.getContext(), this);
        registerSettingsObserver();
        mVsimIgnoreUserDataSetting = OemConstant.isVsimIgnoreUserDataSetting(this.mPhone.getContext());
    }

    public DcTracker() {
        this.isCleanupRequired = new AtomicBoolean(false);
        this.mRequestedApnType = "default";
        this.mDataEnabledSettings = new DataEnabledSettings();
        this.RADIO_RESET_PROPERTY = "gsm.radioreset";
        this.mWifiOldState = DetailedState.IDLE;
        this.mWifiConnectTimeStamp = 0;
        this.LINGER_TIMER = 3000;
        this.RETRY_TIMES = 3;
        this.waitToRetry = new int[]{3, 30, 60};
        this.WAITTING_TIMEOUT = 60;
        this.mPrioritySortedApnContexts = new ArrayList();
        this.mAllApnSettings = null;
        this.mPreferredApn = null;
        this.mIsPsRestricted = false;
        this.mEmergencyApn = null;
        this.mIsDisposed = false;
        this.mIsProvisioning = false;
        this.mProvisioningUrl = null;
        this.mProvisioningApnAlarmIntent = null;
        this.mProvisioningApnAlarmTag = (int) SystemClock.elapsedRealtime();
        this.mReplyAc = new AsyncChannel();
        String[] strArr = new String[4];
        strArr[0] = "ril.iccid.sim1";
        strArr[1] = "ril.iccid.sim2";
        strArr[2] = "ril.iccid.sim3";
        strArr[3] = "ril.iccid.sim4";
        this.PROPERTY_ICCID = strArr;
        this.mIsImsHandover = false;
        this.PROP_IMS_HANDOVER = "ril.imshandover";
        this.mMdChangedAttachApn = null;
        this.mLteAccessStratumDataState = "unknown";
        this.mNetworkType = -1;
        this.mIsLte = false;
        this.mIsSharedDefaultApn = false;
        this.mDefaultRefCount = 0;
        this.mSuspendId = 0;
        this.mRegion = 0;
        this.mNeedsResumeModemLock = new Object();
        this.mNeedsResumeModem = false;
        strArr = new String[2];
        strArr[0] = "26201";
        strArr[1] = "44010";
        this.PLMN_EMPTY_APN_PCSCF_SET = strArr;
        strArr = new String[22];
        strArr[0] = "405840";
        strArr[1] = "405854";
        strArr[2] = "405855";
        strArr[3] = "405856";
        strArr[4] = "405857";
        strArr[5] = "405858";
        strArr[6] = "405859";
        strArr[7] = "405860";
        strArr[8] = "405861";
        strArr[9] = "405862";
        strArr[10] = "405863";
        strArr[11] = "405864";
        strArr[12] = "405865";
        strArr[13] = "405866";
        strArr[14] = "405867";
        strArr[15] = "405868";
        strArr[16] = "405869";
        strArr[17] = "405870";
        strArr[18] = "405871";
        strArr[19] = "405872";
        strArr[20] = "405873";
        strArr[21] = "405874";
        this.MCCMNC_OP18 = strArr;
        this.mHasPsEverAttached = false;
        this.mTelDevController = TelephonyDevController.getInstance();
        this.mIntentReceiver = new AnonymousClass1(this);
        this.mPollNetStat = new AnonymousClass2(this);
        this.mOnSubscriptionsChangedListener = new AnonymousClass3(this);
        this.mDisconnectAllCompleteMsgList = new ArrayList();
        this.mAllDataDisconnectedRegistrants = new RegistrantList();
        this.mIccRecords = new AtomicReference();
        this.mUiccCardApplication = new AtomicReference();
        this.mActivity = Activity.NONE;
        this.mState = State.IDLE;
        this.mNetStatPollEnabled = false;
        this.mDataStallTxRxSum = new TxRxSum(0, 0);
        this.mDataStallAlarmTag = (int) SystemClock.elapsedRealtime();
        this.mDataStallAlarmIntent = null;
        this.mNoRecvPollCount = 0;
        this.mDataStallDetectionEnabled = true;
        this.mFailFast = false;
        this.mInVoiceCall = false;
        this.mIsWifiConnected = false;
        this.mReconnectIntent = null;
        this.mAutoAttachOnCreationConfig = false;
        this.mAutoAttachOnCreation = new AtomicBoolean(false);
        this.mIsScreenOn = true;
        this.mMvnoMatched = false;
        this.mUniqueIdGenerator = new AtomicInteger(0);
        this.mDataConnections = new HashMap();
        this.mDataConnectionAcHashMap = new HashMap();
        this.mApnToDataConnectionId = new HashMap();
        this.mApnContexts = new ConcurrentHashMap();
        this.mApnContextsById = new SparseArray();
        this.mDisconnectPendingCount = 0;
        this.mMeteredApnDisabled = false;
        this.mAllowConfig = false;
        this.mOppoUsageManager = null;
        this.mImsSwitchChangeObserver = /* anonymous class already generated */;
        this.mReregisterOnReconnectFailure = false;
        this.mCanSetPreferApn = false;
        this.mAttached = new AtomicBoolean(false);
        this.mImsRegistrationState = false;
        this.mHighThroughputIdGenerator = new AtomicInteger(0);
        this.mOthersUniqueIdGenerator = new AtomicInteger(2);
        this.mImsUniqueIdGenerator = new AtomicInteger(4);
        this.bNeedTryDefaultForSinglePDN = false;
        this.mAlarmManager = null;
        this.mCm = null;
        this.mPhone = null;
        this.mUiccController = null;
        this.mDataConnectionTracker = null;
        this.mProvisionActionName = null;
        this.mSettingsObserver = new SettingsObserver(null, this);
    }

    public void registerServiceStateTrackerEvents() {
        this.mPhone.getServiceStateTracker().registerForDataConnectionAttached(this, 270352, null);
        this.mPhone.getServiceStateTracker().registerForDataConnectionDetached(this, 270345, null);
        this.mPhone.getServiceStateTracker().registerForDataRoamingOn(this, 270347, null);
        this.mPhone.getServiceStateTracker().registerForDataRoamingOff(this, 270348, null);
        this.mPhone.getServiceStateTracker().registerForDataRoamingTypeChange(this, 270854, null);
        this.mPhone.getServiceStateTracker().registerForPsRestrictedEnabled(this, 270358, null);
        this.mPhone.getServiceStateTracker().registerForPsRestrictedDisabled(this, 270359, null);
        this.mPhone.getServiceStateTracker().registerForDataRegStateOrRatChanged(this, 270377, null);
    }

    public void unregisterServiceStateTrackerEvents() {
        this.mPhone.getServiceStateTracker().unregisterForDataConnectionAttached(this);
        this.mPhone.getServiceStateTracker().unregisterForDataConnectionDetached(this);
        this.mPhone.getServiceStateTracker().unregisterForDataRoamingOn(this);
        this.mPhone.getServiceStateTracker().unregisterForDataRoamingOff(this);
        this.mPhone.getServiceStateTracker().unregisterForDataRoamingTypeChange(this);
        this.mPhone.getServiceStateTracker().unregisterForPsRestrictedEnabled(this);
        this.mPhone.getServiceStateTracker().unregisterForPsRestrictedDisabled(this);
        this.mPhone.getServiceStateTracker().unregisterForDataRegStateOrRatChanged(this);
    }

    private void registerForAllEvents() {
        logd("registerForAllEvents: mPhone = " + this.mPhone);
        this.mPhone.mCi.registerForAvailable(this, 270337, null);
        this.mPhone.mCi.registerForOffOrNotAvailable(this, 270342, null);
        this.mPhone.mCi.registerForDataNetworkStateChanged(this, 270340, null);
        this.mPhone.mCi.registerForOemScreenChanged(this, 270383, null);
        registerServiceStateTrackerEvents();
        this.mPhone.mCi.registerForPcoData(this, 270381, null);
        this.mPhone.mCi.registerForRemoveRestrictEutran(this, 270842, null);
        this.mPhone.mCi.registerForMdDataRetryCountReset(this, 270855, null);
        if (!hasOperatorIaCapability()) {
            if (!(WorldPhoneUtil.isWorldPhoneSupport() || "OP01".equals(SystemProperties.get("ro.operator.optr")))) {
                this.mPhone.mCi.setOnPlmnChangeNotification(this, 270848, null);
                this.mPhone.mCi.setOnRegistrationSuspended(this, 270849, null);
            }
            this.mPhone.mCi.registerForResetAttachApn(this, 270844, null);
            this.mPhone.mCi.registerForAttachApnChanged(this, 270852, null);
        }
        this.mPhone.mCi.registerForPcoStatus(this, 270851, null);
        this.mPhone.mCi.registerForLteAccessStratumState(this, 270847, null);
        this.mPhone.mCi.registerSetDataAllowed(this, 270853, null);
        registerForDataEnabledChanged(this, 270856, null);
    }

    public void dispose() {
        log("DCT.dispose");
        if (this.mTelephonyExt != null) {
            this.mTelephonyExt.stopDataRoamingStrategy();
        }
        if (this.mProvisionBroadcastReceiver != null) {
            this.mPhone.getContext().unregisterReceiver(this.mProvisionBroadcastReceiver);
            this.mProvisionBroadcastReceiver = null;
        }
        if (this.mProvisioningSpinner != null) {
            this.mProvisioningSpinner.dismiss();
            this.mProvisioningSpinner = null;
        }
        cleanUpAllConnections(true, null);
        for (DcAsyncChannel dcac : this.mDataConnectionAcHashMap.values()) {
            dcac.disconnect();
        }
        this.mDataConnectionAcHashMap.clear();
        this.mIsDisposed = true;
        this.mPhone.getContext().unregisterReceiver(this.mIntentReceiver);
        this.mUiccController.unregisterForIccChanged(this);
        this.mSettingsObserver.unobserve();
        this.mSubscriptionManager.removeOnSubscriptionsChangedListener(this.mOnSubscriptionsChangedListener);
        this.mDcc.dispose();
        this.mDcTesterFailBringUpAll.dispose();
        this.mPhone.getContext().getContentResolver().unregisterContentObserver(this.mApnObserver);
        this.mPhone.getContext().getContentResolver().unregisterContentObserver(this.mImsSwitchChangeObserver);
        unregisterForAllEvents();
        this.mApnContexts.clear();
        this.mApnContextsById.clear();
        this.mPrioritySortedApnContexts.clear();
        unregisterForAllEvents();
        destroyDataConnections();
        if (this.mDcHandlerThread != null) {
            this.mDcHandlerThread.quitSafely();
        }
        this.mDcFcMgr.dispose();
    }

    private void unregisterForAllEvents() {
        logd("unregisterForAllEvents: mPhone = " + this.mPhone);
        this.mPhone.mCi.unregisterForAvailable(this);
        this.mPhone.mCi.unregisterForOffOrNotAvailable(this);
        this.mPhone.mCi.unregisterOemScreenChanged(this);
        IccRecords r = (IccRecords) this.mIccRecords.get();
        if (r != null) {
            r.unregisterForRecordsLoaded(this);
            this.mIccRecords.set(null);
        }
        this.mPhone.mCi.unregisterForDataNetworkStateChanged(this);
        unregisterServiceStateTrackerEvents();
        this.mPhone.mCi.unregisterForPcoData(this);
        this.mPhone.mCi.unregisterForRemoveRestrictEutran(this);
        this.mPhone.mCi.unregisterForMdDataRetryCountReset(this);
        if (!hasOperatorIaCapability()) {
            if (!(WorldPhoneUtil.isWorldPhoneSupport() || "OP01".equals(SystemProperties.get("ro.operator.optr")))) {
                this.mPhone.mCi.unSetOnPlmnChangeNotification(this);
                this.mPhone.mCi.unSetOnRegistrationSuspended(this);
            }
            this.mPhone.mCi.unregisterForResetAttachApn(this);
            this.mPhone.mCi.unregisterForAttachApnChanged(this);
        }
        this.mPhone.mCi.unregisterForPcoStatus(this);
        this.mPhone.mCi.unregisterForLteAccessStratumState(this);
        this.mPhone.mCi.unregisterSetDataAllowed(this);
        unregisterForDataEnabledChanged(this);
    }

    private void onResetDone(AsyncResult ar) {
        log("EVENT_RESET_DONE");
        String str = null;
        if (ar.userObj instanceof String) {
            str = ar.userObj;
        }
        gotoIdleAndNotifyDataConnection(str);
    }

    public void setDataEnabled(boolean enable) {
        if (this.mPhone == null || !OemConstant.isPoliceVersion(this.mPhone) || OemConstant.canSwitchByUser(this.mPhone) || enable == OemConstant.isDataAllow(this.mPhone)) {
            Message msg = obtainMessage(270366);
            msg.arg1 = enable ? 1 : 0;
            log("setDataEnabled: sendMessage: enable=" + enable);
            sendMessage(msg);
            return;
        }
        log("---data-enable-return---");
    }

    private void onSetUserDataEnabled(boolean enabled) {
        boolean isDefaultDataPhone = true;
        boolean myMeasureDataState = false;
        synchronized (this.mDataEnabledSettings) {
            if (this.mDataEnabledSettings.isUserDataEnabled() == enabled && enabled != getDataEnabled()) {
                this.mDataEnabledSettings.setUserDataEnabled(getDataEnabled());
                log("onSetUserDataEnabled mUserDataEnabled = " + this.mDataEnabledSettings.isUserDataEnabled());
            }
            if (this.mDataEnabledSettings.isUserDataEnabled() != enabled) {
                int i;
                this.mDataEnabledSettings.setUserDataEnabled(enabled);
                ContentResolver contentResolver = this.mResolver;
                String str = "mobile_data";
                if (enabled) {
                    i = 1;
                } else {
                    i = 0;
                }
                Global.putInt(contentResolver, str, i);
                for (int slotIdx = 0; slotIdx < TelephonyManager.getDefault().getPhoneCount(); slotIdx++) {
                    Phone phone = PhoneFactory.getPhone(slotIdx);
                    if (phone != null) {
                        contentResolver = this.mResolver;
                        str = "mobile_data" + phone.getSubId();
                        if (enabled) {
                            i = 1;
                        } else {
                            i = 0;
                        }
                        Global.putInt(contentResolver, str, i);
                    }
                }
                log("onSetUserDataEnabled enabled = " + enabled + " PhoneId = " + this.mPhone.getPhoneId());
                setUserDataProperty(enabled);
                if (enabled) {
                    i = 1;
                } else {
                    i = 0;
                }
                notifyMobileDataChange(i);
                if (OemConstant.getWlanAssistantEnable(this.mPhone.getContext())) {
                    SubscriptionManager su = SubscriptionManager.from(this.mPhone.getContext());
                    if (this.mPhone.getSubId() != SubscriptionManager.getDefaultDataSubId()) {
                        isDefaultDataPhone = false;
                    }
                    if (isDefaultDataPhone) {
                        boolean isRomming = this.mPhone.getServiceState().getRoaming();
                        log("WLAN+ CMD_SET_USER_DATA_ENABLE: mMeasureDataState=" + mMeasureDataState + " Roaming=" + isRomming + " DataEnabled=" + enabled + " isDefaultDataPhone=" + isDefaultDataPhone);
                        if (mMeasureDataState && this.mIsWifiConnected && !isRomming) {
                            myMeasureDataState = enabled;
                        }
                        if (myMeasureDataState) {
                            new AnonymousClass5(this).start();
                        }
                    }
                }
            }
        }
    }

    private void teardownRestrictedMeteredConnections() {
        if (this.mDataEnabledSettings.isDataEnabled(true)) {
            for (ApnContext apnContext : this.mApnContexts.values()) {
                if (apnContext.isConnectedOrConnecting() && apnContext.getApnSetting().isMetered(this.mPhone.getContext(), this.mPhone.getSubId(), this.mPhone.getServiceState().getDataRoaming())) {
                    DcAsyncChannel dataConnectionAc = apnContext.getDcAc();
                    if (dataConnectionAc != null) {
                        NetworkCapabilities nc = dataConnectionAc.getNetworkCapabilitiesSync();
                        if (nc != null && nc.hasCapability(13)) {
                            log("not tearing down unrestricted metered net:" + apnContext);
                        }
                    }
                    log("tearing down restricted metered net: " + apnContext);
                    apnContext.setReason(PhoneInternalInterface.REASON_DATA_ENABLED);
                    cleanUpConnection(true, apnContext);
                }
            }
        }
    }

    private void onDeviceProvisionedChange() {
        if (getDataEnabled()) {
            this.mDataEnabledSettings.setUserDataEnabled(true);
            teardownRestrictedMeteredConnections();
            onTrySetupData(PhoneInternalInterface.REASON_DATA_ENABLED);
            return;
        }
        this.mDataEnabledSettings.setUserDataEnabled(false);
        onCleanUpAllConnections(PhoneInternalInterface.REASON_DATA_SPECIFIC_DISABLED);
    }

    public long getSubId() {
        return (long) this.mPhone.getSubId();
    }

    public Activity getActivity() {
        return this.mActivity;
    }

    private void setActivity(Activity activity) {
        log("setActivity = " + activity);
        this.mActivity = activity;
        this.mPhone.notifyDataActivity();
    }

    public void requestNetwork(NetworkRequest networkRequest, LocalLog log) {
        ApnContext apnContext = (ApnContext) this.mApnContextsById.get(ApnContext.apnIdForNetworkRequest(networkRequest));
        log.log("DcTracker.requestNetwork for " + networkRequest + " found " + apnContext);
        if (apnContext != null) {
            apnContext.requestNetwork(networkRequest, log);
        }
    }

    public void releaseNetwork(NetworkRequest networkRequest, LocalLog log) {
        ApnContext apnContext = (ApnContext) this.mApnContextsById.get(ApnContext.apnIdForNetworkRequest(networkRequest));
        log.log("DcTracker.releaseNetwork for " + networkRequest + " found " + apnContext);
        if (apnContext != null) {
            apnContext.releaseNetwork(networkRequest, log);
        }
    }

    public boolean isApnSupported(String name) {
        if (name == null) {
            loge("isApnSupported: name=null");
            return false;
        } else if (((ApnContext) this.mApnContexts.get(name)) != null) {
            return true;
        } else {
            loge("Request for unsupported mobile name: " + name);
            return false;
        }
    }

    public int getApnPriority(String name) {
        ApnContext apnContext = (ApnContext) this.mApnContexts.get(name);
        if (apnContext == null) {
            loge("Request for unsupported mobile name: " + name);
        }
        return apnContext.priority;
    }

    private void setRadio(boolean on) {
        try {
            Stub.asInterface(ServiceManager.checkService("phone")).setRadio(on);
        } catch (Exception e) {
        }
    }

    public boolean isDataPossible(String apnType) {
        ApnContext apnContext = (ApnContext) this.mApnContexts.get(apnType);
        if (apnContext == null) {
            return false;
        }
        boolean dataAllowed;
        boolean apnContextIsEnabled = apnContext.isEnabled();
        State apnContextState = apnContext.getState();
        boolean apnTypePossible = (apnContextIsEnabled && apnContextState == State.FAILED) ? false : true;
        if (apnContext.getApnType().equals("emergency")) {
            dataAllowed = true;
        } else {
            dataAllowed = isDataAllowed(null);
        }
        boolean possible = dataAllowed ? apnTypePossible : false;
        if ((apnContext.getApnType().equals("default") || apnContext.getApnType().equals("ia")) && this.mPhone.getServiceState().getRilDataRadioTechnology() == 18) {
            log("Default data call activation not possible in iwlan.");
            possible = false;
        }
        if (VDBG) {
            Object[] objArr = new Object[6];
            objArr[0] = apnType;
            objArr[1] = Boolean.valueOf(possible);
            objArr[2] = Boolean.valueOf(dataAllowed);
            objArr[3] = Boolean.valueOf(apnTypePossible);
            objArr[4] = Boolean.valueOf(apnContextIsEnabled);
            objArr[5] = apnContextState;
            log(String.format("isDataPossible(%s): possible=%b isDataAllowed=%b apnTypePossible=%b apnContextisEnabled=%b apnContextState()=%s", objArr));
        }
        return possible;
    }

    protected void finalize() {
        if (this.mPhone != null) {
            log("finalize");
        }
    }

    private ApnContext addApnContext(String type, NetworkConfig networkConfig) {
        ApnContext apnContext = new ApnContext(this.mPhone, type, LOG_TAG, networkConfig, this);
        this.mApnContexts.put(type, apnContext);
        this.mApnContextsById.put(ApnContext.apnIdForApnName(type), apnContext);
        this.mPrioritySortedApnContexts.add(apnContext);
        return apnContext;
    }

    private void initApnContexts() {
        log("initApnContexts: E");
        for (String networkConfigString : this.mPhone.getContext().getResources().getStringArray(17235985)) {
            ApnContext apnContext;
            NetworkConfig networkConfig = new NetworkConfig(networkConfigString);
            switch (networkConfig.type) {
                case 0:
                    apnContext = addApnContext("default", networkConfig);
                    break;
                case 2:
                    apnContext = addApnContext("mms", networkConfig);
                    break;
                case 3:
                    apnContext = addApnContext("supl", networkConfig);
                    break;
                case 4:
                    apnContext = addApnContext("dun", networkConfig);
                    break;
                case 5:
                    apnContext = addApnContext("hipri", networkConfig);
                    break;
                case 10:
                    apnContext = addApnContext("fota", networkConfig);
                    break;
                case 11:
                    apnContext = addApnContext(ImsSwitchController.IMS_SERVICE, networkConfig);
                    break;
                case 12:
                    apnContext = addApnContext("cbs", networkConfig);
                    break;
                case 14:
                    apnContext = addApnContext("ia", networkConfig);
                    break;
                case 15:
                    apnContext = addApnContext("emergency", networkConfig);
                    break;
                case 34:
                    apnContext = addApnContext("dm", networkConfig);
                    break;
                case 35:
                    apnContext = addApnContext("wap", networkConfig);
                    break;
                case 36:
                    apnContext = addApnContext("net", networkConfig);
                    break;
                case 37:
                    apnContext = addApnContext("cmmail", networkConfig);
                    break;
                case 39:
                    apnContext = addApnContext("rcse", networkConfig);
                    break;
                case 40:
                    apnContext = addApnContext("xcap", networkConfig);
                    break;
                case 41:
                    apnContext = addApnContext("rcs", networkConfig);
                    break;
                case 42:
                    apnContext = addApnContext("bip", networkConfig);
                    break;
                default:
                    log("initApnContexts: skipping unknown type=" + networkConfig.type);
                    continue;
            }
            log("initApnContexts: apnContext=" + apnContext);
        }
        Collections.sort(this.mPrioritySortedApnContexts, new AnonymousClass6(this));
        logd("initApnContexts: mPrioritySortedApnContexts=" + this.mPrioritySortedApnContexts);
        if (VDBG) {
            log("initApnContexts: X mApnContexts=" + this.mApnContexts);
        }
    }

    public LinkProperties getLinkProperties(String apnType) {
        ApnContext apnContext = (ApnContext) this.mApnContexts.get(apnType);
        if (apnContext != null) {
            DcAsyncChannel dcac = apnContext.getDcAc();
            if (dcac != null) {
                log("return link properites for " + apnType);
                return dcac.getLinkPropertiesSync();
            }
        }
        log("return new LinkProperties");
        return new LinkProperties();
    }

    public NetworkCapabilities getNetworkCapabilities(String apnType) {
        ApnContext apnContext = (ApnContext) this.mApnContexts.get(apnType);
        if (apnContext != null) {
            DcAsyncChannel dataConnectionAc = apnContext.getDcAc();
            if (dataConnectionAc != null) {
                log("get active pdp is not null, return NetworkCapabilities for " + apnType);
                return dataConnectionAc.getNetworkCapabilitiesSync();
            }
        }
        log("return new NetworkCapabilities");
        return new NetworkCapabilities();
    }

    public String[] getActiveApnTypes() {
        log("get all active apn types");
        ArrayList<String> result = new ArrayList();
        for (ApnContext apnContext : this.mApnContexts.values()) {
            if (this.mAttached.get() && apnContext.isReady()) {
                result.add(apnContext.getApnType());
            }
        }
        return (String[]) result.toArray(new String[0]);
    }

    public String getActiveApnString(String apnType) {
        if (VDBG) {
            logv("get active apn string for type:" + apnType);
        }
        ApnContext apnContext = (ApnContext) this.mApnContexts.get(apnType);
        if (apnContext != null) {
            ApnSetting apnSetting = apnContext.getApnSetting();
            if (apnSetting != null) {
                return apnSetting.apn;
            }
        }
        return null;
    }

    public State getState(String apnType) {
        ApnContext apnContext = (ApnContext) this.mApnContexts.get(apnType);
        if (apnContext != null) {
            return apnContext.getState();
        }
        return State.FAILED;
    }

    private boolean isProvisioningApn(String apnType) {
        ApnContext apnContext = (ApnContext) this.mApnContexts.get(apnType);
        if (apnContext != null) {
            return apnContext.isProvisioningApn();
        }
        return false;
    }

    public State getOverallState() {
        boolean isConnecting = false;
        boolean isFailed = true;
        boolean isAnyEnabled = false;
        StringBuilder builder = new StringBuilder();
        for (ApnContext apnContext : this.mApnContexts.values()) {
            if (apnContext != null) {
                builder.append(apnContext.toString()).append(", ");
            }
        }
        logd("overall state is " + builder);
        for (ApnContext apnContext2 : this.mApnContexts.values()) {
            if (apnContext2.isEnabled()) {
                isAnyEnabled = true;
                switch (m40xf0fbc33d()[apnContext2.getState().ordinal()]) {
                    case 1:
                    case 3:
                        if (VDBG) {
                            log("overall state is CONNECTED");
                        }
                        return State.CONNECTED;
                    case 2:
                    case 6:
                        isConnecting = true;
                        isFailed = false;
                        break;
                    case 5:
                    case 7:
                        isFailed = false;
                        break;
                    default:
                        isAnyEnabled = true;
                        break;
                }
            }
        }
        if (!isAnyEnabled) {
            if (VDBG) {
                log("overall state is IDLE");
            }
            return State.IDLE;
        } else if (isConnecting) {
            if (VDBG) {
                log("overall state is CONNECTING");
            }
            return State.CONNECTING;
        } else if (isFailed) {
            if (VDBG) {
                log("overall state is FAILED");
            }
            return State.FAILED;
        } else {
            if (VDBG) {
                log("overall state is IDLE");
            }
            return State.IDLE;
        }
    }

    public boolean isApnTypeAvailable(String type) {
        if ((!type.equals("dun") || fetchDunApn() == null) && !type.equals("emergency")) {
            if (this.mAllApnSettings != null) {
                for (ApnSetting apn : this.mAllApnSettings) {
                    if (apn.canHandleType(type)) {
                        return true;
                    }
                }
            }
            return false;
        }
        logd("isApnTypeAvaiable, apn: " + type);
        return true;
    }

    public boolean getAnyDataEnabled() {
        if (!this.mDataEnabledSettings.isDataEnabled(true)) {
            return false;
        }
        DataAllowFailReason failureReason = new DataAllowFailReason();
        if (isDataAllowed(failureReason)) {
            for (ApnContext apnContext : this.mApnContexts.values()) {
                if (isDataAllowedForApn(apnContext)) {
                    logd("getAnyDataEnabled1 return true, apn=" + apnContext.getApnType());
                    return true;
                }
            }
            log("getAnyDataEnabled1 return false");
            return false;
        }
        log(failureReason.getDataAllowFailReason());
        return false;
    }

    public boolean isDataEnabled(boolean checkUserDataEnabled) {
        return this.mDataEnabledSettings.isDataEnabled(checkUserDataEnabled);
    }

    private boolean isDataAllowedForApn(ApnContext apnContext) {
        if ((apnContext.getApnType().equals("default") || apnContext.getApnType().equals("ia")) && this.mPhone.getServiceState().getRilDataRadioTechnology() == 18) {
            log("Default data call activation not allowed in iwlan.");
            return false;
        }
        if (apnContext.getApnType().equals("default")) {
            int dataSub = SubscriptionManager.getDefaultDataSubId();
            if (!(!SubscriptionManager.isValidSubscriptionId(dataSub) || this.mPhone == null || this.mPhone.getSubId() == dataSub)) {
                log("Default apn not allowed in non data sub");
                return false;
            }
        }
        return apnContext.isReady();
    }

    private void onDataConnectionDetached() {
        log("onDataConnectionDetached: stop polling and notify detached");
        stopNetStatPoll();
        stopDataStallAlarm();
        notifyDataConnection(PhoneInternalInterface.REASON_DATA_DETACHED);
        this.mAttached.set(false);
        if (this.mAutoAttachOnCreationConfig) {
            this.mAutoAttachOnCreation.set(false);
        }
    }

    private void onDataConnectionAttached() {
        if (!this.mHasPsEverAttached) {
            logi("onDataConnectionAttached: optimization done");
            this.mHasPsEverAttached = true;
        }
        log("onDataConnectionAttached");
        this.mAttached.set(true);
        if (getOverallState() == State.CONNECTED) {
            log("onDataConnectionAttached: start polling notify attached");
            startNetStatPoll();
            startDataStallAlarm(false);
            notifyDataConnection(PhoneInternalInterface.REASON_DATA_ATTACHED);
        } else {
            notifyOffApnsOfAvailability(PhoneInternalInterface.REASON_DATA_ATTACHED);
        }
        if (this.mAutoAttachOnCreationConfig) {
            this.mAutoAttachOnCreation.set(true);
        }
        setupDataOnConnectableApns(PhoneInternalInterface.REASON_DATA_ATTACHED);
    }

    private boolean isDataAllowed(DataAllowFailReason failureReason) {
        try {
            if ((OemConstant.isPoliceVersion(this.mPhone) || OemConstant.isDeviceLockVersion()) && !OemConstant.isDataAllow(this.mPhone)) {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        boolean internalDataEnabled = this.mDataEnabledSettings.isInternalDataEnabled();
        boolean attachedState = this.mAttached.get();
        boolean desiredPowerState = this.mPhone.getServiceStateTracker().getDesiredPowerState();
        boolean radioStateFromCarrier = this.mPhone.getServiceStateTracker().getPowerStateFromCarrier();
        if (this.mPhone.getServiceState().getRilDataRadioTechnology() == 18) {
            desiredPowerState = true;
            radioStateFromCarrier = true;
        }
        IccRecords r = (IccRecords) this.mIccRecords.get();
        boolean recordsLoaded = false;
        if (r != null) {
            recordsLoaded = r.getRecordsLoaded();
            if (!recordsLoaded) {
                log("isDataAllowed getRecordsLoaded=" + recordsLoaded);
            }
        }
        boolean bIsFdnEnabled = isFdnEnabled();
        boolean defaultDataSelected = SubscriptionManager.isValidSubscriptionId(SubscriptionManager.getDefaultDataSubscriptionId());
        PhoneConstants.State state = PhoneConstants.State.IDLE;
        if (this.mPhone.getCallTracker() != null) {
            state = this.mPhone.getCallTracker().getState();
        }
        DataConnectionHelper dcHelper = DataConnectionHelper.getInstance();
        if (failureReason != null) {
            failureReason.clearAllReasons();
        }
        if (!(!attachedState ? this.mAutoAttachOnCreation.get() : true)) {
            if (failureReason == null) {
                return false;
            }
            failureReason.addDataAllowFailReason(DataAllowFailReasonType.NOT_ATTACHED);
        }
        if (!recordsLoaded) {
            if (failureReason == null) {
                return false;
            }
            failureReason.addDataAllowFailReason(DataAllowFailReasonType.RECORD_NOT_LOADED);
        }
        if (!(dcHelper.isAllCallingStateIdle() || dcHelper.isDataSupportConcurrent(this.mPhone.getPhoneId()))) {
            if (failureReason == null) {
                return false;
            }
            failureReason.addDataAllowFailReason(DataAllowFailReasonType.INVALID_PHONE_STATE);
            failureReason.addDataAllowFailReason(DataAllowFailReasonType.CONCURRENT_VOICE_DATA_NOT_ALLOWED);
        }
        if (!internalDataEnabled) {
            if (failureReason == null) {
                return false;
            }
            failureReason.addDataAllowFailReason(DataAllowFailReasonType.INTERNAL_DATA_DISABLED);
        }
        if (!defaultDataSelected) {
            if (failureReason == null) {
                return false;
            }
            failureReason.addDataAllowFailReason(DataAllowFailReasonType.DEFAULT_DATA_UNSELECTED);
        }
        if ((this.mPhone.getServiceState().getDataRoaming() || this.mPhone.getServiceStateTracker().isPsRegStateRoamByUnsol()) && !getDataOnRoamingEnabled()) {
            if (failureReason == null) {
                return false;
            }
            failureReason.addDataAllowFailReason(DataAllowFailReasonType.ROAMING_DISABLED);
        }
        if (this.mIsPsRestricted) {
            if (failureReason == null) {
                return false;
            }
            failureReason.addDataAllowFailReason(DataAllowFailReasonType.PS_RESTRICTED);
        }
        if (!desiredPowerState) {
            if (failureReason == null) {
                return false;
            }
            failureReason.addDataAllowFailReason(DataAllowFailReasonType.UNDESIRED_POWER_STATE);
        }
        if (!radioStateFromCarrier) {
            if (failureReason == null) {
                return false;
            }
            failureReason.addDataAllowFailReason(DataAllowFailReasonType.RADIO_DISABLED_BY_CARRIER);
        }
        if (bIsFdnEnabled) {
            if (failureReason == null) {
                return false;
            }
            failureReason.addDataAllowFailReason(DataAllowFailReasonType.FDN_ENABLED);
        }
        if (!getAllowConfig()) {
            if (failureReason == null) {
                return false;
            }
            failureReason.addDataAllowFailReason(DataAllowFailReasonType.NOT_ALLOWED);
        }
        boolean z = failureReason == null || !failureReason.isFailed();
        return z;
    }

    private boolean isDataAllowedExt(DataAllowFailReason failureReason, String apnType) {
        int nFailReasonSize = failureReason.getSizeOfFailReason();
        boolean allow = false;
        if (failureReason.mDataAllowFailReasonSet.contains(DataAllowFailReasonType.DEFAULT_DATA_UNSELECTED)) {
            if (!ignoreDefaultDataUnselected(apnType)) {
                return false;
            }
            nFailReasonSize--;
        }
        if (failureReason.mDataAllowFailReasonSet.contains(DataAllowFailReasonType.ROAMING_DISABLED)) {
            if (!ignoreDataRoaming(apnType) && !getDomesticRoamingEnabled()) {
                return false;
            }
            nFailReasonSize--;
        }
        if (failureReason.mDataAllowFailReasonSet.contains(DataAllowFailReasonType.NOT_ALLOWED)) {
            if (!ignoreDataAllow(apnType)) {
                return false;
            }
            nFailReasonSize--;
        }
        if (nFailReasonSize == 0) {
            allow = true;
        }
        if (VDBG) {
            log("isDataAllowedExt: " + allow);
        }
        return allow;
    }

    private void setupDataOnConnectableApns(String reason) {
        setupDataOnConnectableApns(reason, RetryFailures.ALWAYS);
    }

    private void setupDataOnConnectableApns(String reason, RetryFailures retryFailures) {
        if (VDBG) {
            log("setupDataOnConnectableApns: " + reason);
        }
        if (!VDBG) {
            StringBuilder sb = new StringBuilder(RilDataCallFailCause.PDP_FAIL_IFACE_AND_POL_FAMILY_MISMATCH);
            for (ApnContext apnContext : this.mPrioritySortedApnContexts) {
                sb.append(apnContext.getApnType());
                sb.append(":[state=");
                sb.append(apnContext.getState());
                sb.append(",enabled=");
                sb.append(apnContext.isEnabled());
                sb.append("] ");
            }
            log("setupDataOnConnectableApns: " + reason + " " + sb);
        }
        ArrayList<ApnContext> aryApnContext = new ArrayList();
        String strTempIA = SystemProperties.get("ril.radio.ia-apn");
        for (ApnContext tmpApnContext : this.mPrioritySortedApnContexts) {
            if ((TextUtils.equals(strTempIA, VZW_IMS_NI) && TextUtils.equals(tmpApnContext.getApnType(), ImsSwitchController.IMS_SERVICE)) || (TextUtils.equals(strTempIA, VZW_INTERNET_NI) && TextUtils.equals(tmpApnContext.getApnType(), "default"))) {
                aryApnContext.add(0, tmpApnContext);
            } else {
                aryApnContext.add(tmpApnContext);
            }
        }
        for (ApnContext apnContext2 : aryApnContext) {
            if (VDBG) {
                logv("setupDataOnConnectableApns: apnContext " + apnContext2);
            }
            if (this.mTelDevController.getModem(0) != null && !this.mTelDevController.getModem(0).hasMdAutoSetupImsCapability() && (ImsSwitchController.IMS_SERVICE.equals(apnContext2.getApnType()) || "emergency".equals(apnContext2.getApnType()))) {
                logd("setupDataOnConnectableApns: ignore apnContext " + apnContext2);
            } else if (apnContext2.getState() != State.SCANNING || this.mDcFcMgr == null || !this.mDcFcMgr.canIgnoredReason(reason)) {
                if (apnContext2.getState() == State.FAILED || apnContext2.getState() == State.SCANNING) {
                    if (retryFailures == RetryFailures.ALWAYS) {
                        apnContext2.releaseDataConnection(reason);
                    } else if (apnContext2.isConcurrentVoiceAndDataAllowed() || !this.mPhone.getServiceStateTracker().isConcurrentVoiceAndDataAllowed()) {
                        int radioTech = this.mPhone.getServiceState().getRilDataRadioTechnology();
                        ArrayList<ApnSetting> originalApns = apnContext2.getWaitingApns();
                        if (!(originalApns == null || originalApns.isEmpty())) {
                            ArrayList<ApnSetting> waitingApns = buildWaitingApns(apnContext2.getApnType(), radioTech);
                            if (originalApns.size() != waitingApns.size() || !originalApns.containsAll(waitingApns)) {
                                apnContext2.releaseDataConnection(reason);
                            }
                        }
                    } else {
                        apnContext2.releaseDataConnection(reason);
                    }
                }
                if (TextUtils.equals(apnContext2.getApnType(), "default") && TextUtils.equals(strTempIA, VZW_IMS_NI)) {
                    ApnContext apnContextIms = (ApnContext) this.mApnContexts.get(ImsSwitchController.IMS_SERVICE);
                    if (!(apnContextIms == null || apnContextIms.isEnabled())) {
                        if (!TextUtils.equals(reason, PhoneInternalInterface.REASON_DATA_ATTACHED)) {
                            if (!TextUtils.equals(reason, PhoneInternalInterface.REASON_DATA_ENABLED)) {
                                if (!TextUtils.equals(reason, PhoneInternalInterface.REASON_APN_CHANGED)) {
                                    if (!TextUtils.equals(reason, PhoneInternalInterface.REASON_VOICE_CALL_ENDED)) {
                                        if (!TextUtils.equals(reason, PhoneInternalInterface.REASON_SIM_LOADED)) {
                                            log("setupDataOnConnectableApns: ignore default pdn setup");
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                if (apnContext2.isConnectable()) {
                    log("setupDataOnConnectableApns: isConnectable() call trySetupData");
                    if (PhoneInternalInterface.REASON_SINGLE_PDN_ARBITRATION.equals(reason) && apnContext2.getApnType().equals("default")) {
                        if (this.bNeedTryDefaultForSinglePDN) {
                            log("setupDataOnConnectableApns: bNeedTryDefaultForSinglePDN = true");
                        } else {
                            log("setupDataOnConnectableApns: not try default APN.reason is REASON_SINGLE_PDN_ARBITRATION");
                            return;
                        }
                    }
                    String apnType = apnContext2.getApnType();
                    if (OemConstant.getWlanAssistantEnable(this.mPhone.getContext()) && mDelayMeasure && mMeasureDataState && this.mIsWifiConnected && "default".equals(apnType)) {
                        log("setupDataOnConnectableApns: " + reason + "ignore! block for WLAN+");
                    } else {
                        apnContext2.setReason(reason);
                        trySetupData(apnContext2, null);
                    }
                } else {
                    continue;
                }
            }
        }
    }

    boolean isEmergency() {
        boolean result = !this.mPhone.isInEcm() ? this.mPhone.isInEmergencyCall() : true;
        log("isEmergency: result=" + result);
        return result;
    }

    private boolean trySetupData(ApnContext apnContext) {
        return trySetupData(apnContext, null);
    }

    private boolean trySetupData(ApnContext apnContext, ArrayList<ApnSetting> waitingApns) {
        if ("default".equals(apnContext.getApnType()) && this.mPreferredApn == null && needManualSelectAPN(getOperatorNumeric())) {
            log("trySetupData: mPreferredApn == null, need Manual Select APN from UI, can not set up data!");
            return false;
        }
        logi("trySetupData for type:" + apnContext.getApnType() + " due to " + apnContext.getReason() + ", mIsPsRestricted=" + this.mIsPsRestricted);
        apnContext.requestLog("trySetupData due to " + apnContext.getReason());
        if (this.mPhone.getSimulatedRadioControl() != null) {
            apnContext.setState(State.CONNECTED);
            this.mPhone.notifyDataConnection(apnContext.getReason(), apnContext.getApnType());
            log("trySetupData: X We're on the simulator; assuming connected retValue=true");
            return true;
        }
        boolean checkUserDataEnabled;
        boolean isDataAllowed;
        boolean isEmergencyApn = apnContext.getApnType().equals("emergency");
        ServiceStateTracker sst = this.mPhone.getServiceStateTracker();
        if (!ApnSetting.isMeteredApnType(apnContext.getApnType(), this.mPhone.getContext(), this.mPhone.getSubId(), this.mPhone.getServiceState().getDataRoaming()) || isDataAllowedAsOff(apnContext.getApnType())) {
            checkUserDataEnabled = false;
        } else {
            checkUserDataEnabled = apnContext.hasNoRestrictedRequests(true);
        }
        checkUserDataEnabled = checkUserDataEnabled && !haveVsimIgnoreUserDataSetting();
        DataAllowFailReason failureReason = new DataAllowFailReason();
        if (isDataAllowed(failureReason) || (failureReason.isFailForSingleReason(DataAllowFailReasonType.ROAMING_DISABLED) && !ApnSetting.isMeteredApnType(apnContext.getApnType(), this.mPhone.getContext(), this.mPhone.getSubId(), this.mPhone.getServiceState().getDataRoaming()))) {
            isDataAllowed = true;
        } else {
            isDataAllowed = isDataAllowedExt(failureReason, apnContext.getApnType());
        }
        synchronized (this.mDataEnabledSettings) {
            if (this.mDataEnabledSettings.isUserDataEnabled() != getDataEnabled()) {
                log("trySetupData before mUserDataEnabled = " + this.mDataEnabledSettings.isUserDataEnabled());
                this.mDataEnabledSettings.setUserDataEnabled(getDataEnabled());
                log("trySetupData after mUserDataEnabled = " + this.mDataEnabledSettings.isUserDataEnabled());
            }
        }
        if (apnContext.isConnectable() && (isEmergencyApn || (isDataAllowed && isDataAllowedForApn(apnContext) && this.mDataEnabledSettings.isDataEnabled(checkUserDataEnabled) && !isEmergency()))) {
            String str;
            if (apnContext.getState() == State.FAILED) {
                str = "trySetupData: make a FAILED ApnContext IDLE so its reusable";
                log(str);
                apnContext.requestLog(str);
                apnContext.setState(State.IDLE);
            }
            int radioTech = this.mPhone.getServiceState().getRilDataRadioTechnology();
            apnContext.setConcurrentVoiceAndDataAllowed(sst.isConcurrentVoiceAndDataAllowed());
            if (apnContext.getState() == State.IDLE) {
                if (waitingApns == null) {
                    if (!(this.mTelDevController.getModem(0) == null || this.mTelDevController.getModem(0).hasMdAutoSetupImsCapability() || !TextUtils.equals(apnContext.getApnType(), "emergency"))) {
                        if (this.mAllApnSettings == null) {
                            logi("mAllApnSettings is null, create first and add emergency one");
                            createAllApnList();
                        } else if (this.mAllApnSettings.isEmpty()) {
                            logi("add mEmergencyApn: " + this.mEmergencyApn + " to mAllApnSettings");
                            addEmergencyApnSetting();
                        }
                    }
                    waitingApns = buildWaitingApns(apnContext.getApnType(), radioTech);
                }
                if (waitingApns.isEmpty()) {
                    notifyNoData(DcFailCause.MISSING_UNKNOWN_APN, apnContext);
                    notifyOffApnsOfAvailability(apnContext.getReason());
                    str = "trySetupData: X No APN found retValue=false";
                    log(str);
                    apnContext.requestLog(str);
                    int log_type = -1;
                    String log_desc = UsimPBMemInfo.STRING_NOT_SET;
                    try {
                        String[] log_array = this.mPhone.getContext().getString(this.mPhone.getContext().getResources().getIdentifier("zz_oppo_critical_log_111", "string", "android")).split(",");
                        log_type = Integer.valueOf(log_array[0]).intValue();
                        log_desc = log_array[1];
                    } catch (Exception e) {
                    }
                    OppoManager.writeLogToPartition(log_type, "trySetupData: X No APN found", "NETWORK", RIL.ISSUE_SYS_OEM_NW_DIAG_CAUSE_DATA_NO_AVAILABLE_APN, log_desc);
                    return false;
                }
                apnContext.setWaitingApns(waitingApns);
                apnContext.setWifiApns(buildWifiApns(apnContext.getApnType()));
                log("trySetupData: Create from mAllApnSettings : " + apnListToString(this.mAllApnSettings));
            }
            logd("trySetupData: call setupData, waitingApns : " + apnListToString(apnContext.getWaitingApns()) + ", wifiApns : " + apnListToString(apnContext.getWifiApns()));
            boolean retValue = setupData(apnContext, radioTech);
            notifyOffApnsOfAvailability(apnContext.getReason());
            log("trySetupData: X retValue=" + retValue);
            return retValue;
        }
        if (!apnContext.getApnType().equals("default") && apnContext.isConnectable()) {
            if (apnContext.getApnType().equals("mms") && TelephonyManager.getDefault().isMultiSimEnabled() && !this.mAttached.get()) {
                log("Wait for attach");
                return true;
            }
            this.mPhone.notifyDataConnectionFailed(apnContext.getReason(), apnContext.getApnType());
        }
        notifyOffApnsOfAvailability(apnContext.getReason());
        StringBuilder str2 = new StringBuilder();
        str2.append("trySetupData failed. apnContext = [type=").append(apnContext.getApnType()).append(", mState=").append(apnContext.getState()).append(", mDataEnabled=").append(apnContext.isEnabled()).append(", mDependencyMet=").append(apnContext.getDependencyMet()).append("] ");
        if (!apnContext.isConnectable()) {
            str2.append("isConnectable = false. ");
        }
        if (!isDataAllowed) {
            str2.append("data not allowed: ").append(failureReason.getDataAllowFailReason()).append(". ");
        }
        if (!isDataAllowedForApn(apnContext)) {
            str2.append("isDataAllowedForApn = false. RAT = ").append(this.mPhone.getServiceState().getRilDataRadioTechnology());
        }
        if (!this.mDataEnabledSettings.isDataEnabled(checkUserDataEnabled)) {
            str2.append("isDataEnabled(").append(checkUserDataEnabled).append(") = false. ").append("isInternalDataEnabled = ").append(this.mDataEnabledSettings.isInternalDataEnabled()).append(", userDataEnabled = ").append(this.mDataEnabledSettings.isUserDataEnabled()).append(", isPolicyDataEnabled = ").append(this.mDataEnabledSettings.isPolicyDataEnabled()).append(", isCarrierDataEnabled = ").append(this.mDataEnabledSettings.isCarrierDataEnabled());
        }
        if (isEmergency()) {
            str2.append("emergency = true");
        }
        logi(str2.toString());
        apnContext.requestLog(str2.toString());
        return false;
    }

    private void notifyOffApnsOfAvailability(String reason) {
        if (!this.mHasPsEverAttached) {
            boolean doOptimize = false;
            if (!TextUtils.isEmpty(reason)) {
                if (reason.equals(PhoneInternalInterface.REASON_DATA_DETACHED)) {
                    doOptimize = true;
                } else {
                    doOptimize = reason.equals(PhoneInternalInterface.REASON_ROAMING_OFF);
                }
            }
            if (!this.mAttached.get() && doOptimize) {
                logi("notifyOffApnsOfAvailability optimize reason: " + reason + ", notify only for type default and emergency");
                this.mPhone.notifyDataConnection(reason, "default", DataState.DISCONNECTED);
                this.mPhone.notifyDataConnection(reason, "emergency", DataState.DISCONNECTED);
                return;
            }
        }
        DataAllowFailReason failureReason = new DataAllowFailReason();
        if (!isDataAllowed(failureReason)) {
            log(failureReason.getDataAllowFailReason());
        }
        for (ApnContext apnContext : this.mApnContexts.values()) {
            if (!(this.mAttached.get() && apnContext.isReady()) && apnContext.isNeedNotify()) {
                String str;
                String apnType = apnContext.getApnType();
                if (VDBG) {
                    logv("notifyOffApnOfAvailability type:" + apnType + " reason: " + reason);
                }
                Phone phone = this.mPhone;
                if (reason != null) {
                    str = reason;
                } else {
                    str = apnContext.getReason();
                }
                phone.notifyDataConnection(str, apnType, DataState.DISCONNECTED);
            } else if (VDBG) {
                logv("notifyOffApnsOfAvailability skipped apn due to attached && isReady " + apnContext.toString());
            }
        }
    }

    private boolean cleanUpAllConnections(boolean tearDown, String reason) {
        log("cleanUpAllConnections: tearDown=" + tearDown + " reason=" + reason);
        boolean didDisconnect = false;
        boolean disableMeteredOnly = false;
        if (!TextUtils.isEmpty(reason)) {
            if (reason.equals(PhoneInternalInterface.REASON_DATA_SPECIFIC_DISABLED) || reason.equals(PhoneInternalInterface.REASON_ROAMING_ON)) {
                disableMeteredOnly = true;
            } else {
                disableMeteredOnly = reason.equals(PhoneInternalInterface.REASON_CARRIER_ACTION_DISABLE_METERED_APN);
            }
            if (!(this.mTelDevController.getModem(0) == null || this.mTelDevController.getModem(0).hasMdAutoSetupImsCapability())) {
                if (disableMeteredOnly || reason.equals(PhoneInternalInterface.REASON_RADIO_TURNED_OFF)) {
                    disableMeteredOnly = true;
                } else {
                    disableMeteredOnly = reason.equals(PhoneInternalInterface.REASON_PDP_RESET);
                }
            }
        }
        for (ApnContext apnContext : this.mApnContexts.values()) {
            if (!apnContext.isDisconnected()) {
                didDisconnect = true;
            }
            if (disableMeteredOnly) {
                ApnSetting apnSetting = apnContext.getApnSetting();
                if (apnSetting != null && apnSetting.isMetered(this.mPhone.getContext(), this.mPhone.getSubId(), this.mPhone.getServiceState().getDataRoaming())) {
                    log("clean up metered ApnContext Type: " + apnContext.getApnType());
                    apnContext.setReason(reason);
                    cleanUpConnection(tearDown, apnContext);
                }
            } else if (reason != null && reason.equals(PhoneInternalInterface.REASON_ROAMING_ON) && ignoreDataRoaming(apnContext.getApnType())) {
                log("cleanUpConnection: Ignore Data Roaming for apnType = " + apnContext.getApnType());
            } else {
                apnContext.setReason(reason);
                cleanUpConnection(tearDown, apnContext);
            }
        }
        stopNetStatPoll();
        stopDataStallAlarm();
        this.mRequestedApnType = "default";
        log("cleanUpConnection: mDisconnectPendingCount = " + this.mDisconnectPendingCount);
        if (tearDown && this.mDisconnectPendingCount == 0) {
            notifyDataDisconnectComplete();
            notifyAllDataDisconnected();
        }
        return didDisconnect;
    }

    private void onCleanUpAllConnections(String cause) {
        cleanUpAllConnections(true, cause);
    }

    void sendCleanUpConnection(boolean tearDown, ApnContext apnContext) {
        int i;
        log("sendCleanUpConnection: tearDown=" + tearDown + " apnContext=" + apnContext);
        Message msg = obtainMessage(270360);
        if (tearDown) {
            i = 1;
        } else {
            i = 0;
        }
        msg.arg1 = i;
        msg.arg2 = 0;
        msg.obj = apnContext;
        sendMessage(msg);
    }

    private void cleanUpConnection(boolean tearDown, ApnContext apnContext) {
        if (apnContext == null) {
            log("cleanUpConnection: apn context is null");
            return;
        }
        DcAsyncChannel dcac = apnContext.getDcAc();
        String str = "cleanUpConnection: tearDown=" + tearDown + " reason=" + apnContext.getReason();
        if (VDBG) {
            log(str + " apnContext=" + apnContext);
        }
        apnContext.requestLog(str);
        if (!tearDown) {
            boolean needNotify = true;
            int phoneCount = TelephonyManager.getDefault().getPhoneCount();
            if (apnContext.isDisconnected() && phoneCount > 2) {
                needNotify = false;
            }
            if (dcac != null) {
                dcac.reqReset();
            }
            apnContext.setState(State.IDLE);
            if (apnContext.isNeedNotify() && needNotify) {
                this.mPhone.notifyDataConnection(apnContext.getReason(), apnContext.getApnType());
            }
            apnContext.setDataConnectionAc(null);
        } else if (apnContext.isDisconnected()) {
            apnContext.setState(State.IDLE);
            if (!apnContext.isReady()) {
                if (dcac != null) {
                    str = "cleanUpConnection: teardown, disconnected, !ready";
                    logi(str + " apnContext=" + apnContext);
                    apnContext.requestLog(str);
                    dcac.tearDown(apnContext, UsimPBMemInfo.STRING_NOT_SET, null);
                }
                apnContext.setDataConnectionAc(null);
            }
        } else if (dcac == null) {
            apnContext.setState(State.IDLE);
            apnContext.requestLog("cleanUpConnection: connected, bug no DCAC");
            if (apnContext.isNeedNotify()) {
                this.mPhone.notifyDataConnection(apnContext.getReason(), apnContext.getApnType());
            }
        } else if (apnContext.getState() != State.DISCONNECTING) {
            boolean disconnectAll = false;
            if ("dun".equals(apnContext.getApnType()) && teardownForDun()) {
                log("cleanUpConnection: disconnectAll DUN connection");
                disconnectAll = true;
            }
            int generation = apnContext.getConnectionGeneration();
            str = "cleanUpConnection: tearing down" + (disconnectAll ? " all" : UsimPBMemInfo.STRING_NOT_SET) + " using gen#" + generation;
            logi(str + "apnContext=" + apnContext);
            apnContext.requestLog(str);
            Message msg = obtainMessage(270351, new Pair(apnContext, Integer.valueOf(generation)));
            if (disconnectAll) {
                apnContext.getDcAc().tearDownAll(apnContext.getReason(), msg);
            } else {
                apnContext.getDcAc().tearDown(apnContext, apnContext.getReason(), msg);
            }
            apnContext.setState(State.DISCONNECTING);
            this.mDisconnectPendingCount++;
        }
        if (dcac != null) {
            cancelReconnectAlarm(apnContext);
        }
        str = "cleanUpConnection: X tearDown=" + tearDown + " reason=" + apnContext.getReason();
        if (apnContext.isNeedNotify()) {
            log(str + " apnContext=" + apnContext + " dcac=" + apnContext.getDcAc());
        }
        apnContext.requestLog(str);
    }

    ApnSetting fetchDunApn() {
        int i = 0;
        if (SystemProperties.getBoolean("net.tethering.noprovisioning", false)) {
            log("fetchDunApn: net.tethering.noprovisioning=true ret: null");
            return null;
        }
        ApnSetting dunSetting;
        int bearer = this.mPhone.getServiceState().getRilDataRadioTechnology();
        ApnSetting retDunSetting = null;
        IccRecords r = (IccRecords) this.mIccRecords.get();
        for (ApnSetting dunSetting2 : ApnSetting.arrayFromString(Global.getString(this.mResolver, "tether_dun_apn"))) {
            String operator = r != null ? r.getOperatorNumeric() : UsimPBMemInfo.STRING_NOT_SET;
            if (ServiceState.bitmaskHasTech(dunSetting2.bearerBitmask, bearer) && dunSetting2.numeric.equals(operator)) {
                if (dunSetting2.hasMvnoParams()) {
                    if (r != null && ApnSetting.mvnoMatches(r, dunSetting2.mvnoType, dunSetting2.mvnoMatchData)) {
                        if (VDBG) {
                            log("fetchDunApn: global TETHER_DUN_APN dunSetting=" + dunSetting2);
                        }
                        return dunSetting2;
                    }
                } else if (!this.mMvnoMatched) {
                    if (VDBG) {
                        log("fetchDunApn: global TETHER_DUN_APN dunSetting=" + dunSetting2);
                    }
                    return dunSetting2;
                }
            }
        }
        String[] apnArrayData = getDunApnByMccMnc(this.mPhone.getContext());
        int length = apnArrayData.length;
        while (i < length) {
            dunSetting2 = ApnSetting.fromString(apnArrayData[i]);
            if (dunSetting2 != null && ServiceState.bitmaskHasTech(dunSetting2.bearerBitmask, bearer)) {
                if (dunSetting2.hasMvnoParams()) {
                    if (r != null && ApnSetting.mvnoMatches(r, dunSetting2.mvnoType, dunSetting2.mvnoMatchData)) {
                        if (VDBG) {
                            log("fetchDunApn: config_tether_apndata mvno dunSetting=" + dunSetting2);
                        }
                        return dunSetting2;
                    }
                } else if (!this.mMvnoMatched) {
                    retDunSetting = dunSetting2;
                }
            }
            i++;
        }
        if (VDBG) {
            log("fetchDunApn: config_tether_apndata dunSetting=" + retDunSetting);
        }
        return retDunSetting;
    }

    public boolean hasMatchedTetherApnSetting() {
        ApnSetting matched = fetchDunApn();
        log("hasMatchedTetherApnSetting: APN=" + matched);
        return matched != null;
    }

    private String[] getDunApnByMccMnc(Context context) {
        IccRecords r = (IccRecords) this.mIccRecords.get();
        String operator = r != null ? r.getOperatorNumeric() : UsimPBMemInfo.STRING_NOT_SET;
        int mcc = 0;
        int mnc = 0;
        if (operator != null && operator.length() > 3) {
            mcc = Integer.parseInt(operator.substring(0, 3));
            mnc = Integer.parseInt(operator.substring(3, operator.length()));
        }
        Resources sysResource = context.getResources();
        int sysMcc = sysResource.getConfiguration().mcc;
        int sysMnc = sysResource.getConfiguration().mnc;
        logd("fetchDunApn: Resource mccmnc=" + sysMcc + "," + sysMnc + "; OperatorNumeric mccmnc=" + mcc + "," + mnc);
        Resources resource = null;
        try {
            Configuration configuration = new Configuration();
            configuration = context.getResources().getConfiguration();
            configuration.mcc = mcc;
            configuration.mnc = mnc;
            resource = context.createConfigurationContext(configuration).getResources();
        } catch (Exception e) {
            e.printStackTrace();
            loge("getResourcesUsingMccMnc fail");
        }
        if (TelephonyManager.getDefault().getSimCount() == 1 || resource == null) {
            logd("fetchDunApn: get sysResource mcc=" + sysMcc + ", mnc=" + sysMnc);
            return sysResource.getStringArray(17235998);
        }
        logd("fetchDunApn: get resource from mcc=" + mcc + ", mnc=" + mnc);
        return resource.getStringArray(17235998);
    }

    private boolean teardownForDun() {
        boolean z = true;
        if (ServiceState.isCdma(this.mPhone.getServiceState().getRilDataRadioTechnology())) {
            return true;
        }
        if (fetchDunApn() == null) {
            z = false;
        }
        return z;
    }

    private void cancelReconnectAlarm(ApnContext apnContext) {
        if (apnContext != null) {
            PendingIntent intent = apnContext.getReconnectIntent();
            if (intent != null) {
                ((AlarmManager) this.mPhone.getContext().getSystemService("alarm")).cancel(intent);
                apnContext.setReconnectIntent(null);
            }
        }
    }

    private String[] parseTypes(String types) {
        if (types != null && !types.equals(UsimPBMemInfo.STRING_NOT_SET)) {
            return types.split(",");
        }
        String[] result = new String[1];
        result[0] = CharacterSets.MIMENAME_ANY_CHARSET;
        return result;
    }

    boolean isPermanentFail(DcFailCause dcFailCause) {
        boolean z = true;
        boolean z2 = false;
        if (129 == DataConnectionHelper.getInstance().getSbpIdFromNetworkOperator(this.mPhone.getPhoneId())) {
            if (dcFailCause.isPermanentFail() || dcFailCause == DcFailCause.TCM_ESM_TIMER_TIMEOUT) {
                if (this.mAttached.get() && dcFailCause == DcFailCause.SIGNAL_LOST) {
                    z = false;
                }
                z2 = z;
            }
            return z2;
        }
        if (!dcFailCause.isPermanentFail() || !isPermanentFailByOp(dcFailCause)) {
            z = false;
        } else if (this.mAttached.get() && dcFailCause == DcFailCause.SIGNAL_LOST) {
            z = false;
        }
        return z;
    }

    private ApnSetting makeApnSetting(Cursor cursor) {
        int inactiveTimer = 0;
        try {
            inactiveTimer = cursor.getInt(cursor.getColumnIndexOrThrow(Carriers.INACTIVE_TIMER));
        } catch (IllegalArgumentException e) {
            log("makeApnSetting: parsing inactive timer failed. " + e);
        }
        return new ApnSetting(cursor.getInt(cursor.getColumnIndexOrThrow("_id")), cursor.getString(cursor.getColumnIndexOrThrow("numeric")), cursor.getString(cursor.getColumnIndexOrThrow("name")), cursor.getString(cursor.getColumnIndexOrThrow("apn")), NetworkUtils.trimV4AddrZeros(cursor.getString(cursor.getColumnIndexOrThrow("proxy"))), cursor.getString(cursor.getColumnIndexOrThrow("port")), NetworkUtils.trimV4AddrZeros(cursor.getString(cursor.getColumnIndexOrThrow("mmsc"))), NetworkUtils.trimV4AddrZeros(cursor.getString(cursor.getColumnIndexOrThrow("mmsproxy"))), cursor.getString(cursor.getColumnIndexOrThrow("mmsport")), cursor.getString(cursor.getColumnIndexOrThrow("user")), cursor.getString(cursor.getColumnIndexOrThrow("password")), cursor.getInt(cursor.getColumnIndexOrThrow("authtype")), parseTypes(cursor.getString(cursor.getColumnIndexOrThrow("type"))), cursor.getString(cursor.getColumnIndexOrThrow("protocol")), cursor.getString(cursor.getColumnIndexOrThrow("roaming_protocol")), cursor.getInt(cursor.getColumnIndexOrThrow("carrier_enabled")) == 1, cursor.getInt(cursor.getColumnIndexOrThrow("bearer")), cursor.getInt(cursor.getColumnIndexOrThrow("bearer_bitmask")), cursor.getInt(cursor.getColumnIndexOrThrow("profile_id")), cursor.getInt(cursor.getColumnIndexOrThrow("modem_cognitive")) == 1, cursor.getInt(cursor.getColumnIndexOrThrow("max_conns")), cursor.getInt(cursor.getColumnIndexOrThrow("wait_time")), cursor.getInt(cursor.getColumnIndexOrThrow("max_conns_time")), cursor.getInt(cursor.getColumnIndexOrThrow("mtu")), cursor.getString(cursor.getColumnIndexOrThrow("mvno_type")), cursor.getString(cursor.getColumnIndexOrThrow("mvno_match_data")), inactiveTimer);
    }

    private ArrayList<ApnSetting> createApnList(Cursor cursor) {
        ApnSetting apn;
        ArrayList<ApnSetting> result;
        ArrayList<ApnSetting> mnoApns = new ArrayList();
        ArrayList<ApnSetting> mvnoApns = new ArrayList();
        IccRecords r = (IccRecords) this.mIccRecords.get();
        boolean hasMvnoImsApn = false;
        if (cursor.moveToFirst()) {
            do {
                apn = makeApnSetting(cursor);
                if (apn != null) {
                    if (!apn.hasMvnoParams()) {
                        mnoApns.add(apn);
                    } else if (r != null && ApnSetting.mvnoMatches(r, apn.mvnoType, apn.mvnoMatchData)) {
                        mvnoApns.add(apn);
                        if (ArrayUtils.contains(apn.types, ImsSwitchController.IMS_SERVICE)) {
                            hasMvnoImsApn = true;
                        }
                    }
                }
            } while (cursor.moveToNext());
        }
        if (mvnoApns.isEmpty()) {
            result = mnoApns;
            this.mMvnoMatched = false;
        } else {
            result = mvnoApns;
            if (!hasMvnoImsApn) {
                for (ApnSetting apn2 : mnoApns) {
                    if (ArrayUtils.contains(apn2.types, ImsSwitchController.IMS_SERVICE)) {
                        mvnoApns.add(apn2);
                    }
                }
            }
            this.mMvnoMatched = true;
        }
        log("createApnList: X result=" + result);
        return result;
    }

    private boolean dataConnectionNotInUse(DcAsyncChannel dcac) {
        log("dataConnectionNotInUse: check if dcac is inuse dcac=" + dcac);
        for (ApnContext apnContext : this.mApnContexts.values()) {
            if (apnContext.getDcAc() == dcac) {
                log("dataConnectionNotInUse: in use by apnContext=" + apnContext);
                return false;
            }
        }
        log("dataConnectionNotInUse: not in use return true");
        return true;
    }

    private DcAsyncChannel findFreeDataConnection(String reqApnType, ApnSetting apnSetting) {
        for (DcAsyncChannel dcac : this.mDataConnectionAcHashMap.values()) {
            DcAsyncChannel dcac2;
            if (dcac2.isInactiveSync() && dataConnectionNotInUse(dcac2)) {
                DcAsyncChannel dcacForTeardown = dcac2;
                if (isSupportThrottlingApn()) {
                    int id;
                    for (String apn : HIGH_THROUGHPUT_APN) {
                        if (!(apnSetting == null || !apnSetting.canHandleType(apn) || "emergency".equals(reqApnType) || apnSetting.canHandleType(ImsSwitchController.IMS_SERVICE) || dcac2 == null)) {
                            id = dcac2.getDataConnectionIdSync();
                            if (id < 0 || id > 1) {
                                dcac2 = null;
                            }
                        }
                    }
                    if (Arrays.asList(IMS_APN).indexOf(reqApnType) > -1 && apnSetting != null && apnSetting.canHandleType(reqApnType) && dcac2 != null) {
                        id = dcac2.getDataConnectionIdSync();
                        logi("Data connection's interface is: " + id);
                        if ((id == 4 && ImsSwitchController.IMS_SERVICE.equals(reqApnType)) || (id == 5 && "emergency".equals(reqApnType))) {
                            logd("findFreeDataConnection: find connection to handle: " + reqApnType);
                        } else {
                            dcac2 = null;
                        }
                    }
                    if (!("emergency".equals(reqApnType) || ImsSwitchController.IMS_SERVICE.equals(reqApnType) || dcac2 == null)) {
                        id = dcac2.getDataConnectionIdSync();
                        if (id >= 4 && id <= 6) {
                            log("findFreeDataConnection: free dcac for non-IMS APN");
                            dcac2 = null;
                        }
                    }
                }
                if (dcac2 != null) {
                    log("findFreeDataConnection: found free DataConnection= dcac=" + dcac2);
                    return dcac2;
                }
            }
        }
        log("findFreeDataConnection: NO free DataConnection");
        return null;
    }

    private boolean setupData(ApnContext apnContext, int radioTech) {
        log("setupData: apnContext=" + apnContext);
        apnContext.requestLog("setupData");
        DcAsyncChannel dcac = null;
        ApnSetting apnSetting = apnContext.getNextApnSetting();
        if (apnSetting == null) {
            log("setupData: return for no apn found!");
            return false;
        }
        ApnSetting dcacApnSetting;
        int profileId = apnSetting.profileId;
        profileId = getApnProfileID(apnContext.getApnType());
        if (!(apnContext.getApnType() == "dun" && teardownForDun())) {
            dcac = checkForCompatibleConnectedApnContext(apnContext);
            if (dcac != null) {
                dcacApnSetting = dcac.getApnSettingSync();
                if (dcacApnSetting != null) {
                    apnSetting = dcacApnSetting;
                }
            }
        }
        if (dcac == null) {
            if (isOnlySingleDcAllowed(radioTech)) {
                if (isHigherPriorityApnContextActive(apnContext)) {
                    log("setupData: Higher priority ApnContext active.  Ignoring call");
                    return false;
                }
                if (cleanUpAllConnections(true, PhoneInternalInterface.REASON_SINGLE_PDN_ARBITRATION)) {
                    log("setupData: Some calls are disconnecting first.  Wait and retry");
                    return false;
                }
                log("setupData: Single pdp. Continue setting up data call.");
            }
            if (!(isSupportThrottlingApn() || isOnlySingleDcAllowed(radioTech))) {
                boolean isHighThroughputApn = false;
                for (String apn : HIGH_THROUGHPUT_APN) {
                    if (apnSetting.canHandleType(apn)) {
                        isHighThroughputApn = true;
                        break;
                    }
                }
                if (!isHighThroughputApn) {
                    boolean lastDcAlreadyInUse = false;
                    for (DcAsyncChannel asyncChannel : this.mDataConnectionAcHashMap.values()) {
                        if (asyncChannel.getDataConnectionIdSync() == getPdpConnectionPoolSize()) {
                            if (asyncChannel.isInactiveSync() && dataConnectionNotInUse(asyncChannel)) {
                                logd("setupData: find the last dc for non-high-throughput apn, execute tearDownAll to the dc");
                                dcac = asyncChannel;
                                asyncChannel.tearDownAll("No connection", null);
                            } else {
                                log("setupData: the last data connection is already in-use");
                                lastDcAlreadyInUse = true;
                            }
                        }
                    }
                    if (dcac == null && !lastDcAlreadyInUse) {
                        DataConnection conn = DataConnection.makeDataConnection(this.mPhone, getPdpConnectionPoolSize(), this, this.mDcTesterFailBringUpAll, this.mDcc);
                        this.mDataConnections.put(Integer.valueOf(getPdpConnectionPoolSize()), conn);
                        dcac = new DcAsyncChannel(conn, LOG_TAG);
                        int status = dcac.fullyConnectSync(this.mPhone.getContext(), this, conn.getHandler());
                        if (status == 0) {
                            logd("setupData: create the last data connection");
                            this.mDataConnectionAcHashMap.put(Integer.valueOf(dcac.getDataConnectionIdSync()), dcac);
                        } else {
                            loge("setupData: createDataConnection (last) could not connect to dcac=" + dcac + " status=" + status);
                        }
                    }
                }
            }
            if (dcac == null) {
                log("setupData: No ready DataConnection found!");
                dcac = findFreeDataConnection(apnContext.getApnType(), apnSetting);
            }
            if (dcac == null && (apnContext.getApnType() == "default" || apnContext.getApnType() == "mms")) {
                DcAsyncChannel prevDcac = apnContext.getDcAc();
                if (prevDcac != null && prevDcac.isInactiveSync()) {
                    dcac = prevDcac;
                    dcacApnSetting = prevDcac.getApnSettingSync();
                    log("setupData: reuse previous DCAC: dcacApnSetting = " + dcacApnSetting);
                    if (dcacApnSetting != null) {
                        apnSetting = dcacApnSetting;
                    }
                }
            }
            if (dcac == null) {
                dcac = createDataConnection(apnContext.getApnType(), apnSetting);
            }
            if (dcac == null) {
                log("setupData: No free DataConnection and couldn't create one, WEIRD");
                return false;
            }
        }
        int generation = apnContext.incAndGetConnectionGeneration();
        log("setupData: dcac=" + dcac + " apnSetting=" + apnSetting + " gen#=" + generation);
        apnContext.setDataConnectionAc(dcac);
        apnContext.setApnSetting(apnSetting);
        apnContext.setState(State.CONNECTING);
        this.mPhone.notifyDataConnection(apnContext.getReason(), apnContext.getApnType());
        Message msg = obtainMessage();
        msg.what = 270336;
        msg.obj = new Pair(apnContext, Integer.valueOf(generation));
        dcac.bringUp(apnContext, profileId, radioTech, msg, generation);
        log("setupData: initing!");
        return true;
    }

    private void onMdChangedAttachApn(AsyncResult ar) {
        logv("onMdChangedAttachApn");
        int apnId = ar.result[0];
        if (apnId == 1 || apnId == 3) {
            int phoneId = this.mPhone.getPhoneId();
            if (SubscriptionManager.isValidPhoneId(phoneId)) {
                String iccId = SystemProperties.get(this.PROPERTY_ICCID[phoneId], UsimPBMemInfo.STRING_NOT_SET);
                SystemProperties.set(PROP_APN_CLASS_ICCID + phoneId, iccId);
                SystemProperties.set(PROP_APN_CLASS + phoneId, String.valueOf(apnId));
                log("onMdChangedAttachApn, set " + iccId + ", " + apnId);
            }
            updateMdChangedAttachApn(apnId);
            if (this.mMdChangedAttachApn != null) {
                setInitialAttachApn();
            } else {
                logw("onMdChangedAttachApn: MdChangedAttachApn is null, not found APN");
            }
            return;
        }
        logw("onMdChangedAttachApn: Not handle APN Class:" + apnId);
    }

    private void updateMdChangedAttachApn(int apnId) {
        if (this.mAllApnSettings != null && !this.mAllApnSettings.isEmpty()) {
            for (ApnSetting apn : this.mAllApnSettings) {
                if (apnId == 1 && ArrayUtils.contains(apn.types, ImsSwitchController.IMS_SERVICE)) {
                    this.mMdChangedAttachApn = apn;
                    log("updateMdChangedAttachApn: MdChangedAttachApn=" + apn);
                    return;
                } else if (apnId == 3 && ArrayUtils.contains(apn.types, "default")) {
                    this.mMdChangedAttachApn = apn;
                    log("updateMdChangedAttachApn: MdChangedAttachApn=" + apn);
                    return;
                }
            }
        }
    }

    private boolean isMdChangedAttachApnEnabled() {
        if (!(this.mMdChangedAttachApn == null || this.mAllApnSettings == null || this.mAllApnSettings.isEmpty())) {
            for (ApnSetting apn : this.mAllApnSettings) {
                if (TextUtils.equals(this.mMdChangedAttachApn.apn, apn.apn)) {
                    log("isMdChangedAttachApnEnabled: " + apn);
                    return apn.carrierEnabled;
                }
            }
        }
        return false;
    }

    private void setInitialAttachApn() {
        if (hasOperatorIaCapability()) {
            ApnSetting iaApnSetting = null;
            ApnSetting defaultApnSetting = null;
            ApnSetting firstApnSetting = null;
            if (this.mPreferredApn == null && needManualSelectAPN(getOperatorNumeric())) {
                log("setInitialAttachApn: mPreferredApn == null, need Manual Select APN from UI, can not set up data!");
                return;
            }
            log("setInitialApn: E mPreferredApn=" + this.mPreferredApn);
            if (this.mAllApnSettings != null && !this.mAllApnSettings.isEmpty()) {
                firstApnSetting = (ApnSetting) this.mAllApnSettings.get(0);
                log("setInitialApn: firstApnSetting=" + firstApnSetting);
                for (ApnSetting apn : this.mAllApnSettings) {
                    if (ArrayUtils.contains(apn.types, "ia") && apn.carrierEnabled) {
                        log("setInitialApn: iaApnSetting=" + apn);
                        iaApnSetting = apn;
                        break;
                    } else if (defaultApnSetting == null && apn.canHandleType("default")) {
                        log("setInitialApn: defaultApnSetting=" + apn);
                        defaultApnSetting = apn;
                    }
                }
            }
            ApnSetting initialAttachApnSetting = null;
            if (iaApnSetting != null) {
                log("setInitialAttachApn: using iaApnSetting");
                initialAttachApnSetting = iaApnSetting;
            } else if (this.mPreferredApn != null) {
                log("setInitialAttachApn: using mPreferredApn");
                initialAttachApnSetting = this.mPreferredApn;
            } else if (defaultApnSetting != null) {
                log("setInitialAttachApn: using defaultApnSetting");
                initialAttachApnSetting = defaultApnSetting;
            } else if (firstApnSetting != null) {
                log("setInitialAttachApn: using firstApnSetting");
                initialAttachApnSetting = firstApnSetting;
            }
            if (initialAttachApnSetting == null) {
                log("setInitialAttachApn: X There in no available apn");
            } else {
                log("setInitialAttachApn: X selected Apn=" + initialAttachApnSetting);
                this.mPhone.mCi.setInitialAttachApn(initialAttachApnSetting.apn, initialAttachApnSetting.protocol, initialAttachApnSetting.authType, initialAttachApnSetting.user, initialAttachApnSetting.password, null);
            }
            return;
        }
        log("setInitialApn: MD Not support OP IA, do setInitialAttachApnExt");
        setInitialAttachApnExt();
    }

    /* JADX WARNING: Removed duplicated region for block: B:96:0x039b  */
    /* JADX WARNING: Removed duplicated region for block: B:93:0x0390  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void setInitialAttachApnExt() {
        boolean needsResumeModem = false;
        boolean isIaApn = false;
        ApnSetting previousAttachApn = this.mInitialAttachApnSetting;
        IccRecords r = (IccRecords) this.mIccRecords.get();
        String operatorNumeric = r != null ? r.getOperatorNumeric() : UsimPBMemInfo.STRING_NOT_SET;
        if (operatorNumeric == null || operatorNumeric.length() == 0) {
            log("setInitialApn: but no operator numeric");
            return;
        }
        synchronized (this.mNeedsResumeModemLock) {
            if (this.mNeedsResumeModem) {
                this.mNeedsResumeModem = false;
                needsResumeModem = true;
            }
        }
        String currentMcc = operatorNumeric.substring(0, 3);
        log("setInitialApn: currentMcc = " + currentMcc + ", needsResumeModem = " + needsResumeModem);
        String[] dualApnPlmnList = null;
        if (MTK_DUAL_APN_SUPPORT) {
            dualApnPlmnList = this.mPhone.getContext().getResources().getStringArray(134479875);
        }
        log("setInitialAttachApnExt: current attach Apn [" + this.mInitialAttachApnSetting + "]");
        ApnSetting iaApnSetting = null;
        ApnSetting defaultApnSetting = null;
        ApnSetting firstApnSetting = null;
        ApnSetting manualChangedAttachApn = null;
        log("setInitialApn: E mPreferredApn=" + this.mPreferredApn);
        if (this.mIsImsHandover || MTK_IMS_TESTMODE_SUPPORT) {
            manualChangedAttachApn = getClassTypeApn(3);
            if (manualChangedAttachApn != null) {
                log("setInitialAttachApn: manualChangedAttachApn = " + manualChangedAttachApn);
            }
        }
        if (this.mMdChangedAttachApn == null) {
            int phoneId = this.mPhone.getPhoneId();
            if (SubscriptionManager.isValidPhoneId(phoneId)) {
                int apnClass = SystemProperties.getInt(PROP_APN_CLASS + phoneId, -1);
                if (apnClass >= 0) {
                    String iccId = SystemProperties.get(this.PROPERTY_ICCID[phoneId], UsimPBMemInfo.STRING_NOT_SET);
                    String apnClassIccId = SystemProperties.get(PROP_APN_CLASS_ICCID + phoneId, UsimPBMemInfo.STRING_NOT_SET);
                    log("setInitialAttachApn: " + iccId + " , " + apnClassIccId + ", " + apnClass);
                    if (TextUtils.equals(iccId, apnClassIccId)) {
                        updateMdChangedAttachApn(apnClass);
                    } else {
                        SystemProperties.set(PROP_APN_CLASS_ICCID + phoneId, UsimPBMemInfo.STRING_NOT_SET);
                        SystemProperties.set(PROP_APN_CLASS + phoneId, UsimPBMemInfo.STRING_NOT_SET);
                    }
                }
            }
        }
        ApnSetting mdChangedAttachApn = this.mMdChangedAttachApn;
        if (this.mMdChangedAttachApn != null) {
            if (getClassType(this.mMdChangedAttachApn) == 1 && !isMdChangedAttachApnEnabled()) {
                mdChangedAttachApn = null;
            }
        }
        if (mdChangedAttachApn == null && manualChangedAttachApn == null && this.mAllApnSettings != null && !this.mAllApnSettings.isEmpty()) {
            firstApnSetting = (ApnSetting) this.mAllApnSettings.get(0);
            log("setInitialApn: firstApnSetting=" + firstApnSetting);
            for (ApnSetting apn : this.mAllApnSettings) {
                if (ArrayUtils.contains(apn.types, "ia") && apn.carrierEnabled && checkIfDomesticInitialAttachApn(currentMcc)) {
                    log("setInitialApn: iaApnSetting=" + apn);
                    iaApnSetting = apn;
                    if (ArrayUtils.contains(this.PLMN_EMPTY_APN_PCSCF_SET, operatorNumeric)) {
                        isIaApn = true;
                    }
                } else if (defaultApnSetting == null && apn.canHandleType("default")) {
                    log("setInitialApn: defaultApnSetting=" + apn);
                    defaultApnSetting = apn;
                }
            }
        }
        this.mInitialAttachApnSetting = null;
        if (manualChangedAttachApn != null) {
            log("setInitialAttachApn: using manualChangedAttachApn");
            this.mInitialAttachApnSetting = manualChangedAttachApn;
        } else if (mdChangedAttachApn != null) {
            log("setInitialAttachApn: using mMdChangedAttachApn");
            this.mInitialAttachApnSetting = mdChangedAttachApn;
        } else if (iaApnSetting != null) {
            log("setInitialAttachApn: using iaApnSetting");
            this.mInitialAttachApnSetting = iaApnSetting;
        } else if (this.mPreferredApn != null) {
            log("setInitialAttachApn: using mPreferredApn");
            this.mInitialAttachApnSetting = this.mPreferredApn;
        } else if (defaultApnSetting != null) {
            log("setInitialAttachApn: using defaultApnSetting");
            this.mInitialAttachApnSetting = defaultApnSetting;
        } else if (firstApnSetting != null) {
            boolean canHandleType;
            log("setInitialAttachApn: using firstApnSetting");
            if (!firstApnSetting.canHandleType("default")) {
                if (!firstApnSetting.canHandleType("emergency")) {
                    canHandleType = firstApnSetting.canHandleType("ia");
                    if (canHandleType) {
                        log("setInitialAttachApn: don't set not default/emergency/IA APN as IA APN to avoid rejected by NW");
                        return;
                    }
                    this.mInitialAttachApnSetting = firstApnSetting;
                }
            }
            canHandleType = true;
            if (canHandleType) {
            }
        }
        if (this.mInitialAttachApnSetting == null) {
            log("setInitialAttachApn: X There in no available apn, use empty");
            this.mPhone.mCi.setInitialAttachApn(UsimPBMemInfo.STRING_NOT_SET, "IPV4V6", -1, UsimPBMemInfo.STRING_NOT_SET, UsimPBMemInfo.STRING_NOT_SET, new IaExtendParam(operatorNumeric, dualApnPlmnList, "IPV4V6"), null);
        } else {
            log("setInitialAttachApn: X selected Apn=" + this.mInitialAttachApnSetting);
            String iaApn = this.mInitialAttachApnSetting.apn;
            if (isIaApn) {
                log("setInitialAttachApn: ESM flag false, change IA APN to empty");
                iaApn = UsimPBMemInfo.STRING_NOT_SET;
            }
            Message msg = null;
            if (needsResumeModem) {
                log("setInitialAttachApn: DCM IA support");
                msg = obtainMessage(270850);
            }
            String iaApnProtocol = this.mInitialAttachApnSetting.protocol;
            if (isOp18Sim() && this.mPhone.getServiceState().getDataRoaming()) {
                iaApnProtocol = this.mInitialAttachApnSetting.roamingProtocol;
            }
            this.mPhone.mCi.setInitialAttachApn(iaApn, iaApnProtocol, this.mInitialAttachApnSetting.authType, this.mInitialAttachApnSetting.user, this.mInitialAttachApnSetting.password, new IaExtendParam(operatorNumeric, this.mInitialAttachApnSetting.canHandleType(ImsSwitchController.IMS_SERVICE), dualApnPlmnList, this.mInitialAttachApnSetting.roamingProtocol), msg);
        }
        log("setInitialAttachApn: new attach Apn [" + this.mInitialAttachApnSetting + "]");
    }

    private void onApnChanged() {
        if (this.mPhone instanceof GsmCdmaPhone) {
            ((GsmCdmaPhone) this.mPhone).updateCurrentCarrierInProvider();
        }
        ArrayList<ApnSetting> prevAllApns = this.mAllApnSettings;
        ApnSetting prevPreferredApn = this.mPreferredApn;
        log("onApnChanged: createAllApnList and set initial attach APN");
        createAllApnList();
        ApnSetting previousAttachApn = this.mInitialAttachApnSetting;
        if (SystemProperties.getInt(PROPERTY_FORCE_APN_CHANGE, 0) == 0) {
            boolean ignoreName = !VZW_FEATURE;
            String prevPreferredApnString = prevPreferredApn == null ? UsimPBMemInfo.STRING_NOT_SET : prevPreferredApn.toStringIgnoreName(ignoreName);
            String curPreferredApnString = this.mPreferredApn == null ? UsimPBMemInfo.STRING_NOT_SET : this.mPreferredApn.toStringIgnoreName(ignoreName);
            String prevAttachApnSettingString;
            if (previousAttachApn == null) {
                prevAttachApnSettingString = UsimPBMemInfo.STRING_NOT_SET;
            } else {
                prevAttachApnSettingString = previousAttachApn.toStringIgnoreName(ignoreName);
            }
            String curAttachApnSettingString;
            if (this.mInitialAttachApnSetting == null) {
                curAttachApnSettingString = UsimPBMemInfo.STRING_NOT_SET;
            } else {
                curAttachApnSettingString = this.mInitialAttachApnSetting.toStringIgnoreName(ignoreName);
            }
            if (TextUtils.equals(prevPreferredApnString, curPreferredApnString) && isApnSettingExist(previousAttachApn)) {
                if ((prevPreferredApn == null || previousAttachApn == null) && !TextUtils.equals(ApnSetting.toStringIgnoreNameForList(prevAllApns, ignoreName), ApnSetting.toStringIgnoreNameForList(this.mAllApnSettings, ignoreName))) {
                    log("onApnChanged: all APN setting changed.");
                } else if (MTK_IMS_SUPPORT && isIMSApnSettingChanged(prevAllApns, this.mAllApnSettings)) {
                    sendOnApnChangedDone(true);
                    log("onApnChanged: IMS apn setting changed!!");
                    return;
                } else {
                    log("onApnChanged: not changed");
                    return;
                }
            }
        }
        IccRecords r = (IccRecords) this.mIccRecords.get();
        String operator = r != null ? r.getOperatorNumeric() : UsimPBMemInfo.STRING_NOT_SET;
        if (operator == null || operator.length() <= 0) {
            log("onApnChanged: but no operator numeric");
        } else {
            setInitialAttachApn();
        }
        logd("onApnChanged: cleanUpAllConnections and setup connectable APN");
        sendOnApnChangedDone(false);
    }

    private void sendOnApnChangedDone(boolean bImsApnChanged) {
        Message msg = obtainMessage(270839);
        msg.arg1 = bImsApnChanged ? 1 : 0;
        sendMessage(msg);
    }

    private void onApnChangedDone() {
        boolean isDisconnected;
        boolean z = false;
        State overallState = getOverallState();
        if (overallState == State.IDLE) {
            isDisconnected = true;
        } else if (overallState == State.FAILED) {
            isDisconnected = true;
        } else {
            isDisconnected = false;
        }
        if (!isDisconnected) {
            z = true;
        }
        cleanUpConnectionsOnUpdatedApns(z);
        logd("onApnChanged: phone.getsubId=" + this.mPhone.getSubId() + "getDefaultDataSubscriptionId()" + SubscriptionManager.getDefaultDataSubscriptionId());
        if (this.mPhone.getSubId() == SubscriptionManager.getDefaultDataSubscriptionId()) {
            setupDataOnConnectableApns(PhoneInternalInterface.REASON_APN_CHANGED);
        }
    }

    private DcAsyncChannel findDataConnectionAcByCid(int cid) {
        for (DcAsyncChannel dcac : this.mDataConnectionAcHashMap.values()) {
            if (dcac.getCidSync() == cid) {
                return dcac;
            }
        }
        return null;
    }

    private void gotoIdleAndNotifyDataConnection(String reason) {
        log("gotoIdleAndNotifyDataConnection: reason=" + reason);
        notifyDataConnection(reason);
    }

    private boolean isHigherPriorityApnContextActive(ApnContext apnContext) {
        for (ApnContext otherContext : this.mPrioritySortedApnContexts) {
            if (apnContext.getApnType().equalsIgnoreCase(otherContext.getApnType())) {
                return false;
            }
            if (otherContext.isEnabled() && otherContext.getState() != State.FAILED) {
                return true;
            }
        }
        return false;
    }

    private boolean isOnlySingleDcAllowed(int rilRadioTech) {
        int[] singleDcRats = this.mPhone.getContext().getResources().getIntArray(17236023);
        boolean z = false;
        if (!(BSP_PACKAGE || this.mTelephonyExt == null)) {
            try {
                z = this.mTelephonyExt.isOnlySingleDcAllowed();
                if (z) {
                    log("isOnlySingleDcAllowed: " + z);
                    return true;
                }
            } catch (Exception ex) {
                loge("Fail to create or use plug-in");
                ex.printStackTrace();
            }
        }
        if (Build.IS_DEBUGGABLE && SystemProperties.getBoolean("persist.telephony.test.singleDc", false)) {
            z = true;
        }
        if (singleDcRats != null) {
            for (int i = 0; i < singleDcRats.length && !r2; i++) {
                if (rilRadioTech == singleDcRats[i]) {
                    z = true;
                }
            }
        }
        log("isOnlySingleDcAllowed(" + rilRadioTech + "): " + z);
        return z;
    }

    void sendRestartRadio() {
        log("sendRestartRadio:");
        sendMessage(obtainMessage(270362));
    }

    private void restartRadio() {
        log("restartRadio: ************TURN OFF RADIO**************");
        cleanUpAllConnections(true, PhoneInternalInterface.REASON_RADIO_TURNED_OFF);
        this.mPhone.getServiceStateTracker().powerOffRadioSafely(this);
        SystemProperties.set("net.ppp.reset-by-timeout", String.valueOf(Integer.parseInt(SystemProperties.get("net.ppp.reset-by-timeout", "0")) + 1));
    }

    private boolean retryAfterDisconnected(ApnContext apnContext) {
        boolean retry = true;
        String reason = apnContext.getReason();
        if (PhoneInternalInterface.REASON_RADIO_TURNED_OFF.equals(reason) || PhoneInternalInterface.REASON_FDN_ENABLED.equals(reason) || (isOnlySingleDcAllowed(this.mPhone.getServiceState().getRilDataRadioTechnology()) && isHigherPriorityApnContextActive(apnContext))) {
            retry = false;
        }
        if (!"default".equals(apnContext.getApnType()) || !this.mIsWifiConnected || mMeasureDataState) {
            return retry;
        }
        if (VDBG) {
            log("wifi have conneted, set default apn type retry false!!");
        }
        return false;
    }

    private void startAlarmForReconnect(long delay, ApnContext apnContext) {
        String apnType = apnContext.getApnType();
        Intent intent = new Intent("com.android.internal.telephony.data-reconnect." + apnType);
        intent.putExtra(INTENT_RECONNECT_ALARM_EXTRA_REASON, apnContext.getReason());
        intent.putExtra(INTENT_RECONNECT_ALARM_EXTRA_TYPE, apnType);
        intent.addFlags(268435456);
        int subId = this.mPhone.getSubId();
        intent.putExtra("subscription", subId);
        log("startAlarmForReconnect: delay=" + delay + " action=" + intent.getAction() + " apn=" + apnContext + " subId = " + subId);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(this.mPhone.getContext(), 0, intent, 134217728);
        apnContext.setReconnectIntent(alarmIntent);
        this.mAlarmManager.setExact(2, SystemClock.elapsedRealtime() + delay, alarmIntent);
    }

    private void notifyNoData(DcFailCause lastFailCauseCode, ApnContext apnContext) {
        log("notifyNoData: type=" + apnContext.getApnType());
        if (isPermanentFail(lastFailCauseCode) && !apnContext.getApnType().equals("default")) {
            this.mPhone.notifyDataConnectionFailed(apnContext.getReason(), apnContext.getApnType());
        }
    }

    public boolean getAutoAttachOnCreation() {
        return this.mAutoAttachOnCreation.get();
    }

    private void onRecordsLoadedOrSubIdChanged() {
        int i;
        int i2 = 0;
        log("onRecordsLoadedOrSubIdChanged: createAllApnList");
        this.mAutoAttachOnCreationConfig = this.mPhone.getContext().getResources().getBoolean(17957015);
        if (MTK_CC33_SUPPORT) {
            this.mPhone.mCi.setRemoveRestrictEutranMode(true, null);
        }
        int[] iArr = new int[3];
        if (getDataEnabled() || haveVsimIgnoreUserDataSetting()) {
            i = true;
        } else {
            i = 0;
        }
        iArr[0] = i;
        if (getDataOnRoamingEnabled() || SubscriptionManager.isVsimEnabled(this.mPhone.getSubId())) {
            i2 = 1;
        }
        iArr[1] = i2;
        iArr[2] = -2;
        syncDataSettingsToMd(iArr);
        DataConnectionHelper.getInstance().syncDefaultDataSlotId(SubscriptionManager.getSlotId(SubscriptionController.getInstance().getDefaultDataSubId()));
        createAllApnList();
        setInitialAttachApn();
        if (this.mPhone.mCi.getRadioState().isOn()) {
            log("onRecordsLoadedOrSubIdChanged: notifying data availability");
            notifyOffApnsOfAvailability(PhoneInternalInterface.REASON_SIM_LOADED);
        }
        setupDataOnConnectableApns(PhoneInternalInterface.REASON_SIM_LOADED);
    }

    private boolean isFdnEnableSupport() {
        if (BSP_PACKAGE || this.mGsmDctExt == null) {
            return false;
        }
        return this.mGsmDctExt.isFdnEnableSupport();
    }

    private boolean isFdnEnabled() {
        if (isFdnEnableSupport()) {
            return getFdnStatus();
        }
        return false;
    }

    private boolean getFdnStatus() {
        boolean bIsFdnEnabled = false;
        ITelephonyEx telephonyEx = ITelephonyEx.Stub.asInterface(ServiceManager.getService("phoneEx"));
        if (telephonyEx != null) {
            try {
                return telephonyEx.isFdnEnabled(this.mPhone.getSubId());
            } catch (RemoteException ex) {
                ex.printStackTrace();
                return bIsFdnEnabled;
            }
        }
        loge("getFdnStatus get telephonyEx failed!!");
        return bIsFdnEnabled;
    }

    private void onFdnChanged() {
        if (isFdnEnableSupport()) {
            logd("onFdnChanged");
            if (getFdnStatus()) {
                logd("fdn enabled, cleanUpAllConnections!");
                cleanUpAllConnections(true, PhoneInternalInterface.REASON_FDN_ENABLED);
                return;
            }
            logd("fdn disabled, setupDataOnConnectableApns!");
            setupDataOnConnectableApns(PhoneInternalInterface.REASON_FDN_DISABLED);
            return;
        }
        logd("not support fdn enabled, skip onFdnChanged");
    }

    public void setApnsEnabledByCarrier(boolean enabled) {
        Message msg = obtainMessage(270382);
        msg.arg1 = enabled ? 1 : 0;
        sendMessage(msg);
    }

    private void onSetCarrierDataEnabled(boolean enabled) {
        synchronized (this.mDataEnabledSettings) {
            if (enabled != this.mDataEnabledSettings.isCarrierDataEnabled()) {
                log("carrier Action: set metered apns enabled: " + enabled);
                this.mDataEnabledSettings.setCarrierDataEnabled(enabled);
                if (enabled) {
                    teardownRestrictedMeteredConnections();
                    setupDataOnConnectableApns(PhoneInternalInterface.REASON_DATA_ENABLED);
                } else {
                    this.mPhone.notifyOtaspChanged(5);
                    cleanUpAllConnections(true, PhoneInternalInterface.REASON_CARRIER_ACTION_DISABLE_METERED_APN);
                }
            }
        }
    }

    public void carrierActionSetRadioEnabled(boolean enabled) {
        log("carrier Action: set radio enabled: " + enabled);
        this.mPhone.getServiceStateTracker().setRadioPowerFromCarrier(enabled);
    }

    private void onSimNotReady() {
        log("onSimNotReady");
        cleanUpAllConnections(true, PhoneInternalInterface.REASON_SIM_NOT_READY);
        if (this.mAllApnSettings != null) {
            this.mAllApnSettings.clear();
        }
        this.mAutoAttachOnCreationConfig = false;
    }

    private void onSetDependencyMet(String apnType, boolean met) {
        if (!"hipri".equals(apnType)) {
            ApnContext apnContext = (ApnContext) this.mApnContexts.get(apnType);
            if (apnContext == null) {
                loge("onSetDependencyMet: ApnContext not found in onSetDependencyMet(" + apnType + ", " + met + ")");
                return;
            }
            applyNewState(apnContext, apnContext.isEnabled(), met);
            if ("default".equals(apnType)) {
                apnContext = (ApnContext) this.mApnContexts.get("hipri");
                if (apnContext != null) {
                    applyNewState(apnContext, apnContext.isEnabled(), met);
                }
            }
        }
    }

    public void setPolicyDataEnabled(boolean enabled) {
        log("setPolicyDataEnabled: " + enabled);
        Message msg = obtainMessage(270368);
        msg.arg1 = enabled ? 1 : 0;
        sendMessage(msg);
    }

    private void onSetPolicyDataEnabled(boolean enabled) {
        synchronized (this.mDataEnabledSettings) {
            boolean prevEnabled = getAnyDataEnabled();
            if (this.mDataEnabledSettings.isPolicyDataEnabled() != enabled) {
                this.mDataEnabledSettings.setPolicyDataEnabled(enabled);
                if (prevEnabled != getAnyDataEnabled()) {
                    if (prevEnabled) {
                        onCleanUpAllConnections(PhoneInternalInterface.REASON_DATA_SPECIFIC_DISABLED);
                    } else {
                        teardownRestrictedMeteredConnections();
                        onTrySetupData(PhoneInternalInterface.REASON_DATA_ENABLED);
                    }
                }
            }
        }
    }

    private void applyNewState(ApnContext apnContext, boolean enabled, boolean met) {
        boolean cleanup = false;
        boolean trySetup = false;
        String str = "applyNewState(" + apnContext.getApnType() + ", " + enabled + "(" + apnContext.isEnabled() + "), " + met + "(" + apnContext.getDependencyMet() + "))";
        log(str);
        apnContext.requestLog(str);
        if (apnContext.isReady()) {
            cleanup = true;
            if (enabled && met) {
                State state = apnContext.getState();
                switch (m40xf0fbc33d()[state.ordinal()]) {
                    case 1:
                    case 2:
                    case 3:
                    case 7:
                        log("applyNewState: 'ready' so return");
                        apnContext.requestLog("applyNewState state=" + state + ", so return");
                        return;
                    case 4:
                    case 5:
                    case 6:
                        trySetup = true;
                        apnContext.setReason(PhoneInternalInterface.REASON_DATA_ENABLED);
                        break;
                }
            } else if (!enabled) {
                cleanup = true;
                apnContext.setReason(PhoneInternalInterface.REASON_DATA_DISABLED);
            } else if (met) {
                apnContext.setReason(PhoneInternalInterface.REASON_DATA_DISABLED);
                cleanup = apnContext.getApnType() == "dun" && teardownForDun();
            } else {
                apnContext.setReason(PhoneInternalInterface.REASON_DATA_DEPENDENCY_UNMET);
            }
        } else if (enabled && met) {
            if (apnContext.isEnabled()) {
                apnContext.setReason(PhoneInternalInterface.REASON_DATA_DEPENDENCY_MET);
            } else {
                apnContext.setReason(PhoneInternalInterface.REASON_DATA_ENABLED);
            }
            if (apnContext.getState() == State.FAILED) {
                apnContext.setState(State.IDLE);
            }
            trySetup = true;
        }
        apnContext.setEnabled(enabled);
        apnContext.setDependencyMet(met);
        if (cleanup) {
            cleanUpConnection(true, apnContext);
        }
        if (trySetup) {
            apnContext.resetErrorCodeRetries();
            trySetupData(apnContext);
        }
    }

    private DcAsyncChannel checkForCompatibleConnectedApnContext(ApnContext apnContext) {
        String apnType = apnContext.getApnType();
        ApnSetting dunSetting = null;
        if ("dun".equals(apnType)) {
            dunSetting = fetchDunApn();
        }
        log("checkForCompatibleConnectedApnContext: apnContext=" + apnContext);
        DcAsyncChannel potentialDcac = null;
        Object potentialApnCtx = null;
        for (ApnContext curApnCtx : this.mApnContexts.values()) {
            DcAsyncChannel curDcac = curApnCtx.getDcAc();
            if (curDcac != null) {
                ApnSetting apnSetting = curApnCtx.getApnSetting();
                log("apnSetting: " + apnSetting);
                if (dunSetting == null) {
                    if (apnSetting != null && apnSetting.canHandleType(apnType)) {
                        switch (m40xf0fbc33d()[curApnCtx.getState().ordinal()]) {
                            case 1:
                                log("checkForCompatibleConnectedApnContext: found canHandle conn=" + curDcac + " curApnCtx=" + curApnCtx);
                                return curDcac;
                            case 2:
                            case 6:
                            case 7:
                                potentialDcac = curDcac;
                                potentialApnCtx = curApnCtx;
                                break;
                            default:
                                break;
                        }
                    }
                } else if (dunSetting.equals(apnSetting)) {
                    switch (m40xf0fbc33d()[curApnCtx.getState().ordinal()]) {
                        case 1:
                            log("checkForCompatibleConnectedApnContext: found dun conn=" + curDcac + " curApnCtx=" + curApnCtx);
                            return curDcac;
                        case 2:
                        case 6:
                            potentialDcac = curDcac;
                            potentialApnCtx = curApnCtx;
                            break;
                        default:
                            break;
                    }
                } else {
                    continue;
                }
            } else if (VDBG) {
                log("checkForCompatibleConnectedApnContext: not conn curApnCtx=" + curApnCtx);
            }
        }
        if (potentialDcac != null) {
            log("checkForCompatibleConnectedApnContext: found potential conn=" + potentialDcac + " curApnCtx=" + potentialApnCtx);
            return potentialDcac;
        }
        log("checkForCompatibleConnectedApnContext: NO conn apnContext=" + apnContext);
        return null;
    }

    public void setEnabled(int id, boolean enable) {
        Message msg = obtainMessage(270349);
        msg.arg1 = id;
        msg.arg2 = enable ? 1 : 0;
        sendMessage(msg);
    }

    private void onEnableApn(int apnId, int enabled) {
        boolean z = true;
        ApnContext apnContext = (ApnContext) this.mApnContextsById.get(apnId);
        if (apnContext == null) {
            loge("onEnableApn(" + apnId + ", " + enabled + "): NO ApnContext");
            return;
        }
        log("onEnableApn: apnContext=" + apnContext + " call applyNewState");
        if (enabled != 1) {
            z = false;
        }
        applyNewState(apnContext, z, apnContext.getDependencyMet());
    }

    private boolean onTrySetupData(String reason) {
        log("onTrySetupData: reason=" + reason);
        setupDataOnConnectableApns(reason);
        return true;
    }

    private boolean onTrySetupData(ApnContext apnContext) {
        log("onTrySetupData: apnContext=" + apnContext);
        return trySetupData(apnContext);
    }

    public boolean getDataEnabled() {
        int i = 0;
        int device_provisioned = Global.getInt(this.mResolver, "device_provisioned", 0);
        boolean retVal = "true".equalsIgnoreCase(SystemProperties.get("ro.com.android.mobiledata", "true"));
        ContentResolver contentResolver = this.mResolver;
        String str = "mobile_data";
        if (retVal) {
            i = 1;
        }
        retVal = Global.getInt(contentResolver, str, i) != 0;
        log("getDataEnabled: getIntWithSubId retVal=" + retVal);
        return retVal;
    }

    public void setDataOnRoamingEnabled(boolean enabled) {
        int i = 0;
        int phoneSubId = this.mPhone.getSubId();
        if (getDataOnRoamingEnabled() != enabled) {
            int roaming = enabled ? 1 : 0;
            if (TelephonyManager.getDefault().getSimCount() == 1) {
                Global.putInt(this.mResolver, SimInfo.DATA_ROAMING, roaming);
            } else {
                Global.putInt(this.mResolver, SimInfo.DATA_ROAMING + phoneSubId, roaming);
            }
            int[] iArr = new int[3];
            int i2 = (getDataEnabled() || haveVsimIgnoreUserDataSetting()) ? 1 : 0;
            iArr[0] = i2;
            if (enabled || SubscriptionManager.isVsimEnabled(this.mPhone.getSubId())) {
                i = 1;
            }
            iArr[1] = i;
            iArr[2] = -2;
            syncDataSettingsToMd(iArr);
            this.mSubscriptionManager.setDataRoaming(roaming, phoneSubId);
            log("setDataOnRoamingEnabled: set phoneSubId=" + phoneSubId + " isRoaming=" + enabled);
            return;
        }
        log("setDataOnRoamingEnabled: unchanged phoneSubId=" + phoneSubId + " isRoaming=" + enabled);
    }

    public boolean getDataOnRoamingEnabled() {
        int i = 1;
        boolean isDataRoamingEnabled = "true".equalsIgnoreCase(SystemProperties.get("ro.com.android.dataroaming", "false"));
        int phoneSubId = this.mPhone.getSubId();
        try {
            if (TelephonyManager.getDefault().getSimCount() == 1) {
                ContentResolver contentResolver = this.mResolver;
                String str = SimInfo.DATA_ROAMING;
                if (!isDataRoamingEnabled) {
                    i = 0;
                }
                isDataRoamingEnabled = Global.getInt(contentResolver, str, i) != 0;
            } else {
                isDataRoamingEnabled = TelephonyManager.getIntWithSubId(this.mResolver, SimInfo.DATA_ROAMING, phoneSubId) != 0;
            }
        } catch (SettingNotFoundException snfe) {
            log("getDataOnRoamingEnabled: SettingNofFoundException snfe=" + snfe);
        }
        if (VDBG) {
            logTel("getDataOnRoamingEnabled: phoneSubId=" + phoneSubId + " isDataRoamingEnabled=" + isDataRoamingEnabled);
        }
        return isDataRoamingEnabled;
    }

    private boolean ignoreDataRoaming(String apnType) {
        logd("ignoreDataRoaming: apnType = " + apnType);
        boolean ignoreDataRoaming = false;
        try {
            ignoreDataRoaming = this.mTelephonyExt.ignoreDataRoaming(apnType);
        } catch (Exception e) {
            loge("get ignoreDataRoaming fail!");
            e.printStackTrace();
        }
        if (ignoreDataRoaming) {
            logd("ignoreDataRoaming: " + ignoreDataRoaming + ", apnType = " + apnType);
        }
        return ignoreDataRoaming;
    }

    private boolean getDomesticRoamingEnabled() {
        boolean isDomesticRoaming = isDomesticRoaming();
        boolean bDomesticRoamingEnabled = getDomesticRoamingEnabledBySim();
        log("getDomesticRoamingEnabled: isDomesticRoaming=" + isDomesticRoaming + ", bDomesticRoamingEnabled=" + bDomesticRoamingEnabled);
        return isDomesticRoaming ? bDomesticRoamingEnabled : false;
    }

    private boolean getIntlRoamingEnabled() {
        boolean isIntlRoaming = isIntlRoaming();
        boolean bIntlRoamingEnabled = getIntlRoamingEnabledBySim();
        log("getIntlRoamingEnabled: isIntlRoaming=" + isIntlRoaming + ", bIntlRoamingEnabled=" + bIntlRoamingEnabled);
        return isIntlRoaming ? bIntlRoamingEnabled : false;
    }

    private boolean isDomesticRoaming() {
        return this.mPhone.getServiceState().getDataRoamingType() == 2;
    }

    private boolean isIntlRoaming() {
        return this.mPhone.getServiceState().getDataRoamingType() == 3;
    }

    private boolean ignoreDataAllow(String apnType) {
        if (ImsSwitchController.IMS_SERVICE.equals(apnType)) {
            return true;
        }
        return false;
    }

    private boolean ignoreDefaultDataUnselected(String apnType) {
        boolean ignoreDefaultDataUnselected = false;
        try {
            ignoreDefaultDataUnselected = this.mTelephonyExt.ignoreDefaultDataUnselected(apnType);
        } catch (Exception e) {
            loge("get ignoreDefaultDataUnselected fail!");
            e.printStackTrace();
        }
        if (!ignoreDefaultDataUnselected && TextUtils.equals(apnType, "default") && isVsimActive(this.mPhone.getPhoneId())) {
            logd("Vsim is enabled, set ignoreDefaultDataUnselected as true");
            ignoreDefaultDataUnselected = true;
        }
        if (ignoreDefaultDataUnselected) {
            logd("ignoreDefaultDataUnselected: " + ignoreDefaultDataUnselected + ", apnType = " + apnType);
        }
        return ignoreDefaultDataUnselected;
    }

    private void onRoamingOff() {
        boolean bDataOnRoamingEnabled = getDataOnRoamingEnabled();
        logd("onRoamingOff bDataOnRoamingEnabled=" + bDataOnRoamingEnabled);
        if (this.mDataEnabledSettings.isUserDataEnabled()) {
            if (!hasOperatorIaCapability() && isOp18Sim()) {
                setInitialAttachApn();
            }
            if (bDataOnRoamingEnabled) {
                notifyDataConnection(PhoneInternalInterface.REASON_ROAMING_OFF);
            } else {
                notifyOffApnsOfAvailability(PhoneInternalInterface.REASON_ROAMING_OFF);
                setupDataOnConnectableApns(PhoneInternalInterface.REASON_ROAMING_OFF);
            }
        }
    }

    private void onRoamingOn() {
        boolean bDataOnRoamingEnabled = getDataOnRoamingEnabled();
        boolean bDomesticRoamingEnabled = getDomesticRoamingEnabled();
        boolean bIntlRoamingEnabled = getIntlRoamingEnabled();
        boolean forceSetup = false;
        log("onRoamingOn bDataOnRoamingEnabled=" + bDataOnRoamingEnabled + ", bDomesticRoamingEnabled= " + bDomesticRoamingEnabled + ", bIntlRoamingEnabled= " + bIntlRoamingEnabled);
        if (this.mDataEnabledSettings.isUserDataEnabled()) {
            if (bDomesticRoamingEnabled) {
                forceSetup = true;
            }
        } else if (!bDomesticRoamingEnabled) {
            if (!bIntlRoamingEnabled) {
                log("data not enabled by user");
                return;
            }
        } else {
            return;
        }
        if (this.mPhone.getServiceState().getDataRoaming()) {
            if (!hasOperatorIaCapability() && isOp18Sim()) {
                setInitialAttachApn();
            }
            if (bDataOnRoamingEnabled || forceSetup) {
                log("onRoamingOn: setup data on roaming");
                setupDataOnConnectableApns(PhoneInternalInterface.REASON_ROAMING_ON);
                notifyDataConnection(PhoneInternalInterface.REASON_ROAMING_ON);
            } else {
                log("onRoamingOn: Tear down data connection on roaming.");
                cleanUpAllConnections(true, PhoneInternalInterface.REASON_ROAMING_ON);
                notifyOffApnsOfAvailability(PhoneInternalInterface.REASON_ROAMING_ON);
            }
            return;
        }
        log("device is not roaming. ignored the request.");
    }

    private void onRoamingTypeChanged() {
        boolean bDataOnRoamingEnabled = getDataOnRoamingEnabled();
        boolean bUserDataEnabled = this.mDataEnabledSettings.isUserDataEnabled();
        boolean bDomesticSpecialSim = getDomesticRoamingEnabledBySim();
        boolean bIntlSpecialSim = getIntlRoamingEnabledBySim();
        boolean trySetup = false;
        log("onRoamingTypeChanged: bDataOnRoamingEnabled=" + bDataOnRoamingEnabled + ", bUserDataEnabled=" + bUserDataEnabled + ", bDomesticSpecialSim=" + bDomesticSpecialSim + ", bIntlSpecialSim=" + bIntlSpecialSim + ", roamingType=" + this.mPhone.getServiceState().getDataRoamingType());
        if (!bDomesticSpecialSim && !bIntlSpecialSim) {
            log("onRoamingTypeChanged: is not specific SIM. ignored the request.");
        } else if (this.mPhone.getServiceState().getDataRoaming()) {
            if (isDomesticRoaming()) {
                trySetup = bDomesticSpecialSim ? bUserDataEnabled : bUserDataEnabled ? bDataOnRoamingEnabled : false;
            } else if (isIntlRoaming()) {
                trySetup = bIntlSpecialSim ? bDataOnRoamingEnabled : bUserDataEnabled ? bDataOnRoamingEnabled : false;
            } else {
                loge("onRoamingTypeChanged error: unexpected roaming type");
            }
            if (trySetup) {
                log("onRoamingTypeChanged: setup data on roaming");
                setupDataOnConnectableApns(PhoneInternalInterface.REASON_ROAMING_ON);
                notifyDataConnection(PhoneInternalInterface.REASON_ROAMING_ON);
            } else {
                log("onRoamingTypeChanged: Tear down data connection on roaming.");
                cleanUpAllConnections(true, PhoneInternalInterface.REASON_ROAMING_ON);
                notifyOffApnsOfAvailability(PhoneInternalInterface.REASON_ROAMING_ON);
            }
        } else {
            log("onRoamingTypeChanged: device is not roaming. ignored the request.");
        }
    }

    private void onRadioAvailable() {
        log("onRadioAvailable");
        if (this.mPhone.getSimulatedRadioControl() != null) {
            notifyDataConnection(null);
            log("onRadioAvailable: We're on the simulator; assuming data is connected");
        }
        IccRecords r = (IccRecords) this.mIccRecords.get();
        if (r != null && r.getRecordsLoaded()) {
            notifyOffApnsOfAvailability(null);
        }
        if (getOverallState() != State.IDLE) {
            cleanUpConnection(true, null);
        }
    }

    private void onRadioOffOrNotAvailable() {
        this.mReregisterOnReconnectFailure = false;
        this.mAutoAttachOnCreation.set(false);
        this.mMdChangedAttachApn = null;
        if (this.mPhone.getSimulatedRadioControl() != null) {
            log("We're on the simulator; assuming radio off is meaningless");
            notifyOffApnsOfAvailability(null);
            return;
        }
        logd("onRadioOffOrNotAvailable: is off and clean up all connections");
        cleanUpAllConnections(false, PhoneInternalInterface.REASON_RADIO_TURNED_OFF);
    }

    private void completeConnection(ApnContext apnContext) {
        log("completeConnection: successful, notify the world apnContext=" + apnContext);
        if (this.mIsProvisioning && !TextUtils.isEmpty(this.mProvisioningUrl)) {
            log("completeConnection: MOBILE_PROVISIONING_ACTION url=" + this.mProvisioningUrl);
            Intent newIntent = Intent.makeMainSelectorActivity("android.intent.action.MAIN", "android.intent.category.APP_BROWSER");
            newIntent.setData(Uri.parse(this.mProvisioningUrl));
            newIntent.setFlags(272629760);
            try {
                this.mPhone.getContext().startActivity(newIntent);
            } catch (ActivityNotFoundException e) {
                loge("completeConnection: startActivityAsUser failed" + e);
            }
        }
        this.mIsProvisioning = false;
        this.mProvisioningUrl = null;
        if (this.mProvisioningSpinner != null) {
            sendMessage(obtainMessage(270378, this.mProvisioningSpinner));
        }
        this.mPhone.notifyDataConnection(apnContext.getReason(), apnContext.getApnType());
        startNetStatPoll();
        startDataStallAlarm(false);
    }

    private void onDataSetupComplete(AsyncResult ar) {
        DcFailCause cause = DcFailCause.UNKNOWN;
        boolean handleError = false;
        ApnContext apnContext = getValidApnContext(ar, "onDataSetupComplete");
        if (apnContext != null) {
            ApnSetting apn;
            Intent intent;
            if (ar.exception == null) {
                DcAsyncChannel dcac = apnContext.getDcAc();
                if (dcac == null) {
                    log("onDataSetupComplete: no connection to DC, handle as error");
                    cause = DcFailCause.CONNECTION_TO_DATACONNECTIONAC_BROKEN;
                    apnContext.setState(State.FAILED);
                    handleError = true;
                } else {
                    apn = apnContext.getApnSetting();
                    log("onDataSetupComplete: success apn=" + (apn == null ? "unknown" : apn.apn));
                    if (!(apn == null || apn.proxy == null || apn.proxy.length() == 0)) {
                        try {
                            String port = apn.port;
                            if (TextUtils.isEmpty(port)) {
                                port = "8080";
                            }
                            dcac.setLinkPropertiesHttpProxySync(new ProxyInfo(apn.proxy, Integer.parseInt(port), null));
                        } catch (NumberFormatException e) {
                            loge("onDataSetupComplete: NumberFormatException making ProxyProperties (" + apn.port + "): " + e);
                        }
                    }
                    if (TextUtils.equals(apnContext.getApnType(), "default")) {
                        try {
                            SystemProperties.set(PUPPET_MASTER_RADIO_STRESS_TEST, "true");
                        } catch (RuntimeException e2) {
                            log("Failed to set PUPPET_MASTER_RADIO_STRESS_TEST to true");
                        }
                        if (this.mCanSetPreferApn && this.mPreferredApn == null) {
                            log("onDataSetupComplete: PREFERRED APN is null");
                            this.mPreferredApn = apn;
                            if (this.mPreferredApn != null) {
                                setPreferredApn(this.mPreferredApn.id);
                            }
                        }
                    } else {
                        try {
                            SystemProperties.set(PUPPET_MASTER_RADIO_STRESS_TEST, "false");
                        } catch (RuntimeException e3) {
                            loge("Failed to set PUPPET_MASTER_RADIO_STRESS_TEST to false");
                        }
                    }
                    apnContext.setState(State.CONNECTED);
                    boolean isProvApn = apnContext.isProvisioningApn();
                    ConnectivityManager cm = ConnectivityManager.from(this.mPhone.getContext());
                    if (this.mProvisionBroadcastReceiver != null) {
                        this.mPhone.getContext().unregisterReceiver(this.mProvisionBroadcastReceiver);
                        this.mProvisionBroadcastReceiver = null;
                    }
                    if (!isProvApn || this.mIsProvisioning) {
                        cm.setProvisioningNotificationVisible(false, 0, this.mProvisionActionName);
                        completeConnection(apnContext);
                    } else {
                        log("onDataSetupComplete: successful, BUT send connected to prov apn as mIsProvisioning:" + this.mIsProvisioning + " == false" + " && (isProvisioningApn:" + isProvApn + " == true");
                        this.mProvisionBroadcastReceiver = new ProvisionNotificationBroadcastReceiver(this, cm.getMobileProvisioningUrl(), TelephonyManager.getDefault().getNetworkOperatorName());
                        this.mPhone.getContext().registerReceiver(this.mProvisionBroadcastReceiver, new IntentFilter(this.mProvisionActionName));
                        cm.setProvisioningNotificationVisible(true, 0, this.mProvisionActionName);
                        setRadio(false);
                    }
                    log("onDataSetupComplete: SETUP complete type=" + apnContext.getApnType() + ", reason:" + apnContext.getReason());
                    if (Build.IS_DEBUGGABLE) {
                        int pcoVal = SystemProperties.getInt("persist.radio.test.pco", -1);
                        if (pcoVal != -1) {
                            log("PCO testing: read pco value from persist.radio.test.pco " + pcoVal);
                            byte[] value = new byte[1];
                            value[0] = (byte) pcoVal;
                            intent = new Intent("android.intent.action.CARRIER_SIGNAL_PCO_VALUE");
                            intent.putExtra("apnType", "default");
                            intent.putExtra("apnProto", "IPV4V6");
                            intent.putExtra("pcoId", 65280);
                            intent.putExtra("pcoValue", value);
                            this.mPhone.getCarrierSignalAgent().notifyCarrierSignalReceivers(intent);
                        }
                    }
                }
            } else {
                cause = ar.result;
                apn = apnContext.getApnSetting();
                String str = "onDataSetupComplete: error apn=%s cause=%s";
                Object[] objArr = new Object[2];
                objArr[0] = apn == null ? "unknown" : apn.apn;
                objArr[1] = cause;
                log(String.format(str, objArr));
                if (cause.isEventLoggable()) {
                    int cid = getCellLocationId();
                    Integer[] numArr = new Object[3];
                    numArr[0] = Integer.valueOf(cause.ordinal());
                    numArr[1] = Integer.valueOf(cid);
                    numArr[2] = Integer.valueOf(TelephonyManager.getDefault().getNetworkType());
                    EventLog.writeEvent(EventLogTags.PDP_SETUP_FAIL, numArr);
                }
                apn = apnContext.getApnSetting();
                this.mPhone.notifyPreciseDataConnectionFailed(apnContext.getReason(), apnContext.getApnType(), apn != null ? apn.apn : "unknown", cause.toString());
                intent = new Intent("android.intent.action.CARRIER_SIGNAL_REQUEST_NETWORK_FAILED");
                intent.putExtra("errorCode", cause.getErrorCode());
                intent.putExtra("apnType", apnContext.getApnType());
                this.mPhone.getCarrierSignalAgent().notifyCarrierSignalReceivers(intent);
                if (cause.isRestartRadioFail() || apnContext.restartOnError(cause.getErrorCode())) {
                    log("Modem restarted.");
                    sendRestartRadio();
                }
                if (isPermanentFail(cause) || (this.mGsmDctExt != null && this.mGsmDctExt.isIgnoredCause(cause))) {
                    log("cause = " + cause + ", mark apn as permanent failed. apn = " + apn);
                    apnContext.markApnPermanentFailed(apn);
                }
                handleError = true;
            }
            if (handleError) {
                onDataSetupCompleteError(ar);
            }
            if (!this.mDataEnabledSettings.isInternalDataEnabled()) {
                cleanUpAllConnections(PhoneInternalInterface.REASON_DATA_DISABLED);
            }
        }
    }

    private ApnContext getValidApnContext(AsyncResult ar, String logString) {
        if (ar != null && (ar.userObj instanceof Pair)) {
            Pair<ApnContext, Integer> pair = ar.userObj;
            ApnContext apnContext = pair.first;
            if (apnContext != null) {
                int generation = apnContext.getConnectionGeneration();
                log("getValidApnContext (" + logString + ") on " + apnContext + " got " + generation + " vs " + pair.second);
                if (generation == ((Integer) pair.second).intValue()) {
                    return apnContext;
                }
                log("ignoring obsolete " + logString);
                return null;
            }
        }
        log(logString + ": No apnContext");
        return null;
    }

    private void onDataSetupCompleteError(AsyncResult ar) {
        ApnContext apnContext = getValidApnContext(ar, "onDataSetupCompleteError");
        if (apnContext != null) {
            long delay = apnContext.getDelayForNextApn(this.mFailFast);
            if (delay >= 0) {
                log("onDataSetupCompleteError: Try next APN. delay = " + delay);
                apnContext.setState(State.SCANNING);
                startAlarmForReconnect(delay, apnContext);
            } else {
                apnContext.setState(State.FAILED);
                this.mPhone.notifyDataConnection(PhoneInternalInterface.REASON_APN_FAILED, apnContext.getApnType());
                apnContext.setDataConnectionAc(null);
                log("onDataSetupCompleteError: Stop retrying APNs.");
            }
            DcFailCause cause = DcFailCause.UNKNOWN;
            ApnSetting apn = apnContext.getApnSetting();
            cause = ar.result;
            String error_info = "onDataSetupComplete: error apn=" + (apn == null ? "unknown" : apn.apn) + " cause=%s" + cause + " cid=" + getCellLocationId() + ",nwType=" + TelephonyManager.getDefault().getNetworkType();
            int log_type = -1;
            String log_desc = UsimPBMemInfo.STRING_NOT_SET;
            try {
                String[] log_array = this.mPhone.getContext().getString(this.mPhone.getContext().getResources().getIdentifier("zz_oppo_critical_log_110", "string", "android")).split(",");
                log_type = Integer.valueOf(log_array[0]).intValue();
                log_desc = log_array[1];
            } catch (Exception e) {
            }
            OppoManager.writeLogToPartition(log_type, error_info, "NETWORK", RIL.ISSUE_SYS_OEM_NW_DIAG_CAUSE_DATA_SET_UP_DATA_ERROR, log_desc);
        }
    }

    private void onDataConnectionRedirected(String redirectUrl) {
        if (!TextUtils.isEmpty(redirectUrl)) {
            Intent intent = new Intent("android.intent.action.CARRIER_SIGNAL_REDIRECTED");
            intent.putExtra("redirectionUrl", redirectUrl);
            if (this.mPhone.getCarrierSignalAgent().notifyCarrierSignalReceivers(intent)) {
                log("Notify carrier signal receivers with redirectUrl: " + redirectUrl);
            }
        }
    }

    private void onDisconnectDone(AsyncResult ar) {
        ApnContext apnContext = getValidApnContext(ar, "onDisconnectDone");
        if (apnContext != null) {
            log("onDisconnectDone: EVENT_DISCONNECT_DONE apnContext=" + apnContext);
            apnContext.setState(State.IDLE);
            this.mPhone.notifyDataConnection(apnContext.getReason(), apnContext.getApnType());
            if ((isDisconnected() || isOnlyIMSorEIMSPdnConnected()) && this.mPhone.getServiceStateTracker().processPendingRadioPowerOffAfterDataOff()) {
                log("onDisconnectDone: radio will be turned off, no retries");
                apnContext.setApnSetting(null);
                apnContext.setDataConnectionAc(null);
                if (this.mDisconnectPendingCount > 0) {
                    this.mDisconnectPendingCount--;
                }
                if (this.mDisconnectPendingCount == 0) {
                    notifyDataDisconnectComplete();
                    notifyAllDataDisconnected();
                }
                return;
            }
            if (this.mAttached.get() && apnContext.isReady() && retryAfterDisconnected(apnContext)) {
                try {
                    SystemProperties.set(PUPPET_MASTER_RADIO_STRESS_TEST, "false");
                } catch (RuntimeException e) {
                    log("Failed to set PUPPET_MASTER_RADIO_STRESS_TEST to false");
                }
                if (this.mTelDevController.getModem(0) == null || this.mTelDevController.getModem(0).hasMdAutoSetupImsCapability() || !(ImsSwitchController.IMS_SERVICE.equals(apnContext.getApnType()) || "emergency".equals(apnContext.getApnType()))) {
                    log("onDisconnectDone: attached, ready and retry after disconnect");
                    long delay = getDisconnectDoneRetryTimer(apnContext.getReason(), apnContext.getInterApnDelay(this.mFailFast));
                    if (delay > 0) {
                        startAlarmForReconnect(delay, apnContext);
                    }
                } else {
                    logd("onDisconnectDone: not to retry for " + apnContext.getApnType() + " PDN");
                    return;
                }
            }
            boolean restartRadioAfterProvisioning = this.mPhone.getContext().getResources().getBoolean(17956991);
            if (apnContext.isProvisioningApn() && restartRadioAfterProvisioning) {
                log("onDisconnectDone: restartRadio after provisioning");
                restartRadio();
            }
            boolean isDefaultApn = false;
            if (apnContext.getApnType().equals("default")) {
                isDefaultApn = true;
            }
            apnContext.setApnSetting(null);
            apnContext.setDataConnectionAc(null);
            if (isOnlySingleDcAllowed(this.mPhone.getServiceState().getRilDataRadioTechnology())) {
                log("onDisconnectDone: isOnlySigneDcAllowed true so setup single apn");
                if ("default".equals(apnContext.getApnType()) && this.mIsWifiConnected) {
                    log("wifi have been conneted, set default apn type retry false--do nothing!");
                } else {
                    if (isDefaultApn) {
                        this.bNeedTryDefaultForSinglePDN = false;
                    } else {
                        this.bNeedTryDefaultForSinglePDN = true;
                    }
                    setupDataOnConnectableApns(PhoneInternalInterface.REASON_SINGLE_PDN_ARBITRATION);
                }
            } else {
                log("onDisconnectDone: not retrying");
            }
            if (this.mDisconnectPendingCount > 0) {
                this.mDisconnectPendingCount--;
            }
            if (this.mDisconnectPendingCount == 0) {
                apnContext.setConcurrentVoiceAndDataAllowed(this.mPhone.getServiceStateTracker().isConcurrentVoiceAndDataAllowed());
                notifyDataDisconnectComplete();
                notifyAllDataDisconnected();
            }
        }
    }

    private long getDisconnectDoneRetryTimer(String reason, long delay) {
        long timer = delay;
        if (PhoneInternalInterface.REASON_APN_CHANGED.equals(reason)) {
            return 3000;
        }
        if (BSP_PACKAGE || this.mGsmDctExt == null) {
            return timer;
        }
        try {
            return this.mGsmDctExt.getDisconnectDoneRetryTimer(reason, delay);
        } catch (Exception e) {
            loge("GsmDCTExt.getDisconnectDoneRetryTimer fail!");
            e.printStackTrace();
            return timer;
        }
    }

    private void onDisconnectDcRetrying(AsyncResult ar) {
        ApnContext apnContext = getValidApnContext(ar, "onDisconnectDcRetrying");
        if (apnContext != null) {
            apnContext.setState(State.RETRYING);
            log("onDisconnectDcRetrying: apnContext=" + apnContext);
            this.mPhone.notifyDataConnection(apnContext.getReason(), apnContext.getApnType());
        }
    }

    public void onVoiceCallStarted() {
        this.mInVoiceCall = true;
        boolean isSupportConcurrent = DataConnectionHelper.getInstance().isDataSupportConcurrent(this.mPhone.getPhoneId());
        logd("onVoiceCallStarted:isDataSupportConcurrent = " + isSupportConcurrent);
        SubscriptionManager su = SubscriptionManager.from(this.mPhone.getContext());
        if ((this.mPhone.getSubId() == SubscriptionManager.getDefaultDataSubId()) && !isSupportConcurrent) {
            removeMessages(270384);
            mDelayMeasure = true;
            log("WLAN+ onVoiceCallStarted mDelayMeasure:true");
        }
        if (isConnected() && !isSupportConcurrent) {
            logd("onVoiceCallStarted stop polling");
            stopNetStatPoll();
            stopDataStallAlarm();
            notifyDataConnection(PhoneInternalInterface.REASON_VOICE_CALL_STARTED);
        }
        notifyVoiceCallEventToDataConnection(this.mInVoiceCall, isSupportConcurrent);
    }

    public void onVoiceCallEnded() {
        this.mInVoiceCall = false;
        boolean isSupportConcurrent = DataConnectionHelper.getInstance().isDataSupportConcurrent(this.mPhone.getPhoneId());
        logd("onVoiceCallEnded:isDataSupportConcurrent = " + isSupportConcurrent);
        if (!(getDataEnabled() || haveVsimIgnoreUserDataSetting())) {
            logd("onVoiceCallEnded: default data disable, cleanup default apn.");
            onCleanUpConnection(true, 0, PhoneInternalInterface.REASON_DATA_DISABLED);
        }
        if (isConnected()) {
            if (isSupportConcurrent) {
                resetPollStats();
            } else {
                startNetStatPoll();
                startDataStallAlarm(false);
                notifyDataConnection(PhoneInternalInterface.REASON_VOICE_CALL_ENDED);
            }
        }
        SubscriptionManager su = SubscriptionManager.from(this.mPhone.getContext());
        boolean isDefaultDataPhone = this.mPhone.getSubId() == SubscriptionManager.getDefaultDataSubId();
        if (OemConstant.getWlanAssistantEnable(this.mPhone.getContext()) && mMeasureDataState && ((getDataEnabled() || haveVsimIgnoreUserDataSetting()) && !this.mPhone.getServiceState().getRoaming() && this.mIsWifiConnected && isDefaultDataPhone && !isSupportConcurrent)) {
            removeMessages(270384);
            sendMessageDelayed(obtainMessage(270384, null), 60000);
            return;
        }
        setupDataOnConnectableApns(PhoneInternalInterface.REASON_VOICE_CALL_ENDED);
        notifyVoiceCallEventToDataConnection(this.mInVoiceCall, isSupportConcurrent);
        mDelayMeasure = false;
        log("WLAN+ onVoiceCallEnded mDelayMeasure:false");
    }

    private void onCleanUpConnection(boolean tearDown, int apnId, String reason) {
        log("onCleanUpConnection");
        ApnContext apnContext = (ApnContext) this.mApnContextsById.get(apnId);
        if (apnContext != null) {
            apnContext.setReason(reason);
            cleanUpConnection(tearDown, apnContext);
        }
    }

    private boolean isConnected() {
        for (ApnContext apnContext : this.mApnContexts.values()) {
            if (apnContext.getState() == State.CONNECTED) {
                return true;
            }
        }
        return false;
    }

    public boolean isDisconnected() {
        for (ApnContext apnContext : this.mApnContexts.values()) {
            if (!apnContext.isDisconnected()) {
                return false;
            }
        }
        return true;
    }

    private void notifyDataConnection(String reason) {
        log("notifyDataConnection: reason=" + reason);
        if (this.mAttached.get()) {
            for (ApnContext apnContext : this.mApnContexts.values()) {
                if (apnContext.isReady() && apnContext.isNeedNotify()) {
                    log("notifyDataConnection: type:" + apnContext.getApnType());
                    this.mPhone.notifyDataConnection(reason != null ? reason : apnContext.getReason(), apnContext.getApnType());
                }
            }
        }
        notifyOffApnsOfAvailability(reason);
    }

    private void setDataProfilesAsNeeded() {
        log("setDataProfilesAsNeeded");
        if (this.mAllApnSettings != null && !this.mAllApnSettings.isEmpty()) {
            ArrayList<DataProfile> dps = new ArrayList();
            for (ApnSetting apn : this.mAllApnSettings) {
                if (apn.modemCognitive) {
                    DataProfile dp = new DataProfile(apn, this.mPhone.getServiceState().getDataRoaming());
                    boolean isDup = false;
                    for (DataProfile dpIn : dps) {
                        if (dp.equals(dpIn)) {
                            isDup = true;
                            break;
                        }
                    }
                    if (!isDup) {
                        dps.add(dp);
                    }
                }
            }
            if (dps.size() > 0) {
                this.mPhone.mCi.setDataProfile((DataProfile[]) dps.toArray(new DataProfile[0]), null);
            }
        }
    }

    private void createAllApnList() {
        this.mMvnoMatched = false;
        this.mAllApnSettings = new ArrayList();
        IccRecords r = (IccRecords) this.mIccRecords.get();
        String operator = r != null ? r.getOperatorNumeric() : UsimPBMemInfo.STRING_NOT_SET;
        log("createAllApnList: operator = " + operator);
        if (this.mTelephonyExt != null) {
            operator = this.mTelephonyExt.getOperatorNumericFromImpi(operator, this.mPhone.getPhoneId());
        }
        if (operator != null) {
            String selection = "numeric = '" + operator + "'";
            log("createAllApnList: selection=" + selection);
            Cursor cursor = this.mPhone.getContext().getContentResolver().query(Carriers.CONTENT_URI, null, selection, null, "_id");
            if (cursor != null) {
                if (cursor.getCount() > 0) {
                    this.mAllApnSettings = createApnList(cursor);
                }
                cursor.close();
            }
        }
        initEmergencyApnSetting();
        addEmergencyApnSetting();
        dedupeApnSettings();
        if (this.mAllApnSettings.isEmpty()) {
            log("createAllApnList: No APN found for carrier: " + operator);
            this.mPreferredApn = null;
        } else {
            this.mPreferredApn = getPreferredApn();
            if (!(this.mPreferredApn == null || this.mPreferredApn.numeric.equals(operator))) {
                this.mPreferredApn = null;
                setPreferredApn(-1);
            }
            log("createAllApnList: mPreferredApn=" + this.mPreferredApn);
        }
        logi("createAllApnList: X mAllApnSettings=" + this.mAllApnSettings);
        setDataProfilesAsNeeded();
        if (operator != null) {
            syncApnToMd();
        }
        syncApnTableToRds(this.mAllApnSettings);
    }

    private void dedupeApnSettings() {
        ArrayList<ApnSetting> resultApns = new ArrayList();
        for (int i = 0; i < this.mAllApnSettings.size() - 1; i++) {
            ApnSetting first = (ApnSetting) this.mAllApnSettings.get(i);
            int j = i + 1;
            while (j < this.mAllApnSettings.size()) {
                ApnSetting second = (ApnSetting) this.mAllApnSettings.get(j);
                if (apnsSimilar(first, second)) {
                    ApnSetting newApn = mergeApns(first, second);
                    this.mAllApnSettings.set(i, newApn);
                    first = newApn;
                    this.mAllApnSettings.remove(j);
                } else {
                    j++;
                }
            }
        }
    }

    private boolean apnTypeSameAny(ApnSetting first, ApnSetting second) {
        if (VDBG) {
            StringBuilder apnType1 = new StringBuilder(first.apn + ": ");
            for (String append : first.types) {
                apnType1.append(append);
                apnType1.append(",");
            }
            StringBuilder apnType2 = new StringBuilder(second.apn + ": ");
            for (String append2 : second.types) {
                apnType2.append(append2);
                apnType2.append(",");
            }
            log("APN1: is " + apnType1);
            log("APN2: is " + apnType2);
        }
        int index1 = 0;
        while (index1 < first.types.length) {
            int index2 = 0;
            while (index2 < second.types.length) {
                if (first.types[index1].equals(CharacterSets.MIMENAME_ANY_CHARSET) || second.types[index2].equals(CharacterSets.MIMENAME_ANY_CHARSET) || first.types[index1].equals(second.types[index2])) {
                    if (VDBG) {
                        log("apnTypeSameAny: return true");
                    }
                    return true;
                }
                index2++;
            }
            index1++;
        }
        if (VDBG) {
            log("apnTypeSameAny: return false");
        }
        return false;
    }

    private boolean apnsSimilar(ApnSetting first, ApnSetting second) {
        if (!first.canHandleType("dun") && !second.canHandleType("dun") && first.apn != null && second.apn != null && first.apn.equalsIgnoreCase(second.apn) && !apnTypeSameAny(first, second) && xorEquals(first.proxy, second.proxy) && xorEquals(first.port, second.port) && first.carrierEnabled == second.carrierEnabled && first.bearerBitmask == second.bearerBitmask && first.profileId == second.profileId && Objects.equals(first.mvnoType, second.mvnoType) && Objects.equals(first.mvnoMatchData, second.mvnoMatchData) && xorEquals(first.mmsc, second.mmsc) && xorEquals(first.mmsProxy, second.mmsProxy)) {
            return xorEquals(first.mmsPort, second.mmsPort);
        }
        return false;
    }

    private boolean xorEquals(String first, String second) {
        if (Objects.equals(first, second) || TextUtils.isEmpty(first)) {
            return true;
        }
        return TextUtils.isEmpty(second);
    }

    private ApnSetting mergeApns(ApnSetting dest, ApnSetting src) {
        String roamingProtocol;
        int id = dest.id;
        ArrayList<String> resultTypes = new ArrayList();
        resultTypes.addAll(Arrays.asList(dest.types));
        for (String srcType : src.types) {
            if (!resultTypes.contains(srcType)) {
                resultTypes.add(srcType);
            }
            if (srcType.equals("default")) {
                id = src.id;
            }
        }
        String mmsc = TextUtils.isEmpty(dest.mmsc) ? src.mmsc : dest.mmsc;
        String mmsProxy = TextUtils.isEmpty(dest.mmsProxy) ? src.mmsProxy : dest.mmsProxy;
        String mmsPort = TextUtils.isEmpty(dest.mmsPort) ? src.mmsPort : dest.mmsPort;
        String proxy = TextUtils.isEmpty(dest.proxy) ? src.proxy : dest.proxy;
        String port = TextUtils.isEmpty(dest.port) ? src.port : dest.port;
        String protocol = src.protocol.equals("IPV4V6") ? src.protocol : dest.protocol;
        if (src.roamingProtocol.equals("IPV4V6")) {
            roamingProtocol = src.roamingProtocol;
        } else {
            roamingProtocol = dest.roamingProtocol;
        }
        int bearerBitmask = (dest.bearerBitmask == 0 || src.bearerBitmask == 0) ? 0 : dest.bearerBitmask | src.bearerBitmask;
        return new ApnSetting(id, dest.numeric, dest.carrier, dest.apn, proxy, port, mmsc, mmsProxy, mmsPort, dest.user, dest.password, dest.authType, (String[]) resultTypes.toArray(new String[0]), protocol, roamingProtocol, dest.carrierEnabled, 0, bearerBitmask, dest.profileId, !dest.modemCognitive ? src.modemCognitive : true, dest.maxConns, dest.waitTime, dest.maxConnsTime, dest.mtu, dest.mvnoType, dest.mvnoMatchData, dest.inactiveTimer);
    }

    private DcAsyncChannel createDataConnection(String reqApnType, ApnSetting apnSetting) {
        int id;
        log("createDataConnection E");
        if (isSupportThrottlingApn()) {
            id = generateDataConnectionId(reqApnType, apnSetting);
            if (id < 0) {
                return null;
            }
        }
        id = this.mUniqueIdGenerator.getAndIncrement();
        if (id >= getPdpConnectionPoolSize()) {
            loge("Max PDP count is " + getPdpConnectionPoolSize() + ",but request " + (id + 1));
            this.mUniqueIdGenerator.getAndDecrement();
            return null;
        }
        DataConnection conn = DataConnection.makeDataConnection(this.mPhone, id, this, this.mDcTesterFailBringUpAll, this.mDcc);
        this.mDataConnections.put(Integer.valueOf(id), conn);
        DcAsyncChannel dcac = new DcAsyncChannel(conn, LOG_TAG);
        int status = dcac.fullyConnectSync(this.mPhone.getContext(), this, conn.getHandler());
        if (status == 0) {
            this.mDataConnectionAcHashMap.put(Integer.valueOf(dcac.getDataConnectionIdSync()), dcac);
        } else {
            loge("createDataConnection: Could not connect to dcac=" + dcac + " status=" + status);
        }
        log("createDataConnection() X id=" + id + " dc=" + conn);
        return dcac;
    }

    private void destroyDataConnections() {
        if (this.mDataConnections != null) {
            log("destroyDataConnections: clear mDataConnectionList");
            this.mDataConnections.clear();
            return;
        }
        log("destroyDataConnections: mDataConnecitonList is empty, ignore");
    }

    private ArrayList<ApnSetting> buildWaitingApns(String requestedApnType, int radioTech) {
        boolean usePreferred;
        log("buildWaitingApns: E requestedApnType=" + requestedApnType);
        ArrayList<ApnSetting> apnList = new ArrayList();
        if (requestedApnType.equals("dun")) {
            ApnSetting dun = fetchDunApn();
            if (dun != null) {
                apnList.add(dun);
                log("buildWaitingApns: X added APN_TYPE_DUN apnList=" + apnList);
                return apnList;
            }
        }
        IccRecords r = (IccRecords) this.mIccRecords.get();
        String operator = r != null ? r.getOperatorNumeric() : UsimPBMemInfo.STRING_NOT_SET;
        try {
            usePreferred = !this.mPhone.getContext().getResources().getBoolean(17956990);
        } catch (NotFoundException e) {
            log("buildWaitingApns: usePreferred NotFoundException set to true");
            usePreferred = true;
        }
        if (usePreferred) {
            this.mPreferredApn = getPreferredApn();
        }
        log("buildWaitingApns: usePreferred=" + usePreferred + " canSetPreferApn=" + this.mCanSetPreferApn + " mPreferredApn=" + this.mPreferredApn + " operator=" + operator + " radioTech=" + radioTech + " IccRecords r=" + r);
        if (usePreferred && this.mCanSetPreferApn && this.mPreferredApn != null && this.mPreferredApn.canHandleType(requestedApnType)) {
            log("buildWaitingApns: Preferred APN:" + operator + ":" + this.mPreferredApn.numeric + ":" + this.mPreferredApn);
            if (!this.mPreferredApn.numeric.equals(operator)) {
                log("buildWaitingApns: no preferred APN");
                setPreferredApn(-1);
                this.mPreferredApn = null;
            } else if (ServiceState.bitmaskHasTech(this.mPreferredApn.bearerBitmask, radioTech)) {
                apnList.add(this.mPreferredApn);
                log("buildWaitingApns: X added preferred apnList=" + apnList);
                return apnList;
            } else {
                log("buildWaitingApns: no preferred APN");
                setPreferredApn(-1);
                this.mPreferredApn = null;
            }
        }
        if (this.mAllApnSettings != null) {
            log("buildWaitingApns: mAllApnSettings=" + this.mAllApnSettings);
            for (ApnSetting apn : this.mAllApnSettings) {
                if (!apn.canHandleType(requestedApnType)) {
                    log("buildWaitingApns: couldn't handle requested ApnType=" + requestedApnType);
                } else if (ServiceState.bitmaskHasTech(apn.bearerBitmask, radioTech)) {
                    log("buildWaitingApns: adding apn=" + apn);
                    apnList.add(apn);
                } else {
                    log("buildWaitingApns: bearerBitmask:" + apn.bearerBitmask + " does " + "not include radioTech:" + radioTech);
                }
            }
        } else {
            loge("mAllApnSettings is null!");
        }
        log("buildWaitingApns: " + apnList.size() + " APNs in the list: " + apnList);
        return apnList;
    }

    private ArrayList<ApnSetting> buildWifiApns(String requestedApnType) {
        log("buildWifiApns: E requestedApnType=" + requestedApnType);
        ArrayList<ApnSetting> apnList = new ArrayList();
        if (this.mAllApnSettings != null) {
            log("buildWaitingApns: mAllApnSettings=" + this.mAllApnSettings);
            for (ApnSetting apn : this.mAllApnSettings) {
                if (apn.canHandleType(requestedApnType) && isWifiOnlyApn(apn.bearerBitmask)) {
                    apnList.add(apn);
                }
            }
        }
        log("buildWifiApns: X apnList=" + apnList);
        return apnList;
    }

    private String apnListToString(ArrayList<ApnSetting> apns) {
        StringBuilder result = new StringBuilder();
        try {
            int size = apns.size();
            for (int i = 0; i < size; i++) {
                result.append('[').append(((ApnSetting) apns.get(i)).toString()).append(']');
            }
            return result.toString();
        } catch (NullPointerException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    private void setPreferredApn(int pos) {
        if (this.mCanSetPreferApn) {
            String subId = Long.toString((long) this.mPhone.getSubId());
            Uri uri = Uri.withAppendedPath(PREFERAPN_NO_UPDATE_URI_USING_SUBID, subId);
            log("setPreferredApn: delete subId=" + subId);
            ContentResolver resolver = this.mPhone.getContext().getContentResolver();
            resolver.delete(uri, null, null);
            if (pos >= 0) {
                log("setPreferredApn: insert pos=" + pos + ", subId=" + subId);
                ContentValues values = new ContentValues();
                values.put(APN_ID, Integer.valueOf(pos));
                resolver.insert(uri, values);
            }
            return;
        }
        log("setPreferredApn: X !canSEtPreferApn");
    }

    private ApnSetting getPreferredApn() {
        if (this.mAllApnSettings == null || this.mAllApnSettings.isEmpty()) {
            log("getPreferredApn: mAllApnSettings is " + (this.mAllApnSettings == null ? "null" : "empty"));
            return null;
        }
        int count;
        String subId = Long.toString((long) this.mPhone.getSubId());
        Uri uri = Uri.withAppendedPath(PREFERAPN_NO_UPDATE_URI_USING_SUBID, subId);
        ContentResolver contentResolver = this.mPhone.getContext().getContentResolver();
        String[] strArr = new String[3];
        strArr[0] = "_id";
        strArr[1] = "name";
        strArr[2] = "apn";
        Cursor cursor = contentResolver.query(uri, strArr, null, null, "name ASC");
        if (cursor != null) {
            this.mCanSetPreferApn = true;
        } else {
            this.mCanSetPreferApn = false;
        }
        StringBuilder append = new StringBuilder().append("getPreferredApn: mRequestedApnType=").append(this.mRequestedApnType).append(" cursor=").append(cursor).append(" cursor.count=");
        if (cursor != null) {
            count = cursor.getCount();
        } else {
            count = 0;
        }
        log(append.append(count).append(" subId=").append(subId).toString());
        if (this.mCanSetPreferApn && cursor.getCount() > 0) {
            cursor.moveToFirst();
            int pos = cursor.getInt(cursor.getColumnIndexOrThrow("_id"));
            for (ApnSetting p : this.mAllApnSettings) {
                log("getPreferredApn: apnSetting=" + p + ", pos=" + pos + ", subId=" + subId);
                if (p.id == pos && p.canHandleType(this.mRequestedApnType)) {
                    log("getPreferredApn: X found apnSetting" + p);
                    cursor.close();
                    return p;
                }
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        log("getPreferredApn: X not found");
        return null;
    }

    public void handleMessage(Message msg) {
        if (VDBG) {
            log("handleMessage msg=" + msg);
        }
        boolean enabled;
        Bundle bundle;
        AsyncResult ar;
        switch (msg.what) {
            case 69636:
                log("DISCONNECTED_CONNECTED: msg=" + msg);
                DcAsyncChannel dcac = msg.obj;
                this.mDataConnectionAcHashMap.remove(Integer.valueOf(dcac.getDataConnectionIdSync()));
                dcac.disconnected();
                return;
            case 270336:
                onDataSetupComplete((AsyncResult) msg.obj);
                return;
            case 270337:
                break;
            case 270338:
                int subId = this.mPhone.getSubId();
                if (SubscriptionManager.isValidSubscriptionId(subId)) {
                    onRecordsLoadedOrSubIdChanged();
                    return;
                } else {
                    log("Ignoring EVENT_RECORDS_LOADED as subId is not valid: " + subId);
                    return;
                }
            case 270339:
                if (msg.obj instanceof ApnContext) {
                    onTrySetupData((ApnContext) msg.obj);
                    return;
                } else if (msg.obj instanceof String) {
                    onTrySetupData((String) msg.obj);
                    return;
                } else {
                    loge("EVENT_TRY_SETUP request w/o apnContext or String");
                    return;
                }
            case 270340:
                return;
            case 270342:
                onRadioOffOrNotAvailable();
                return;
            case 270343:
                onVoiceCallStarted();
                return;
            case 270344:
                onVoiceCallEnded();
                return;
            case 270345:
                onDataConnectionDetached();
                return;
            case 270347:
                onRoamingOn();
                return;
            case 270348:
                onRoamingOff();
                return;
            case 270349:
                onEnableApn(msg.arg1, msg.arg2);
                return;
            case 270351:
                log("DataConnectionTracker.handleMessage: EVENT_DISCONNECT_DONE msg=" + msg);
                onDisconnectDone((AsyncResult) msg.obj);
                return;
            case 270352:
                onDataConnectionAttached();
                return;
            case 270353:
                onDataStallAlarm(msg.arg1);
                return;
            case 270354:
                doRecovery();
                return;
            case 270355:
                onApnChanged();
                return;
            case 270358:
                log("EVENT_PS_RESTRICT_ENABLED " + this.mIsPsRestricted);
                stopNetStatPoll();
                stopDataStallAlarm();
                this.mIsPsRestricted = true;
                return;
            case 270359:
                ConnectivityManager cnnm = (ConnectivityManager) this.mPhone.getContext().getSystemService("connectivity");
                log("EVENT_PS_RESTRICT_DISABLED " + this.mIsPsRestricted);
                this.mIsPsRestricted = false;
                if (isConnected()) {
                    startNetStatPoll();
                    startDataStallAlarm(false);
                    return;
                }
                if (this.mState == State.FAILED) {
                    cleanUpAllConnections(false, PhoneInternalInterface.REASON_PS_RESTRICT_ENABLED);
                    this.mReregisterOnReconnectFailure = false;
                }
                ApnContext apnContext = (ApnContext) this.mApnContextsById.get(0);
                if (apnContext == null) {
                    loge("**** Default ApnContext not found ****");
                    loge("Default ApnContext not found");
                    return;
                } else if (this.mPhone.getServiceStateTracker().getCurrentDataConnectionState() == 0) {
                    apnContext.setReason(PhoneInternalInterface.REASON_PS_RESTRICT_ENABLED);
                    trySetupData(apnContext);
                    return;
                } else {
                    log("EVENT_PS_RESTRICT_DISABLED, data not attached, skip.");
                    return;
                }
            case 270360:
                boolean tearDown = msg.arg1 != 0;
                log("EVENT_CLEAN_UP_CONNECTION tearDown=" + tearDown);
                if (msg.obj instanceof ApnContext) {
                    cleanUpConnection(tearDown, (ApnContext) msg.obj);
                    return;
                } else {
                    onCleanUpConnection(tearDown, msg.arg2, (String) msg.obj);
                    return;
                }
            case 270362:
                restartRadio();
                return;
            case 270363:
                onSetInternalDataEnabled(msg.arg1 == 1, (Message) msg.obj);
                return;
            case 270364:
                log("EVENT_RESET_DONE");
                onResetDone((AsyncResult) msg.obj);
                return;
            case 270365:
                if (!(msg.obj == null || (msg.obj instanceof String))) {
                    msg.obj = null;
                }
                onCleanUpAllConnections((String) msg.obj);
                return;
            case 270366:
                enabled = msg.arg1 == 1;
                log("CMD_SET_USER_DATA_ENABLE enabled=" + enabled);
                onSetUserDataEnabled(enabled);
                return;
            case 270367:
                boolean met = msg.arg1 == 1;
                log("CMD_SET_DEPENDENCY_MET met=" + met);
                bundle = msg.getData();
                if (bundle != null) {
                    String apnType = (String) bundle.get("apnType");
                    if (apnType != null) {
                        onSetDependencyMet(apnType, met);
                        return;
                    }
                    return;
                }
                return;
            case 270368:
                onSetPolicyDataEnabled(msg.arg1 == 1);
                return;
            case 270369:
                onUpdateIcc();
                return;
            case 270370:
                log("DataConnectionTracker.handleMessage: EVENT_DISCONNECT_DC_RETRYING msg=" + msg);
                onDisconnectDcRetrying((AsyncResult) msg.obj);
                return;
            case 270371:
                onDataSetupCompleteError((AsyncResult) msg.obj);
                return;
            case 270372:
                sEnableFailFastRefCounter = (msg.arg1 == 1 ? 1 : -1) + sEnableFailFastRefCounter;
                log("CMD_SET_ENABLE_FAIL_FAST_MOBILE_DATA:  sEnableFailFastRefCounter=" + sEnableFailFastRefCounter);
                if (sEnableFailFastRefCounter < 0) {
                    loge("CMD_SET_ENABLE_FAIL_FAST_MOBILE_DATA: sEnableFailFastRefCounter:" + sEnableFailFastRefCounter + " < 0");
                    sEnableFailFastRefCounter = 0;
                }
                enabled = sEnableFailFastRefCounter > 0;
                log("CMD_SET_ENABLE_FAIL_FAST_MOBILE_DATA: enabled=" + enabled + " sEnableFailFastRefCounter=" + sEnableFailFastRefCounter);
                if (this.mFailFast != enabled) {
                    this.mFailFast = enabled;
                    this.mDataStallDetectionEnabled = !enabled;
                    if (this.mDataStallDetectionEnabled && getOverallState() == State.CONNECTED && (!this.mInVoiceCall || this.mPhone.getServiceStateTracker().isConcurrentVoiceAndDataAllowed())) {
                        log("CMD_SET_ENABLE_FAIL_FAST_MOBILE_DATA: start data stall");
                        stopDataStallAlarm();
                        startDataStallAlarm(false);
                        return;
                    }
                    log("CMD_SET_ENABLE_FAIL_FAST_MOBILE_DATA: stop data stall");
                    stopDataStallAlarm();
                    return;
                }
                return;
            case 270373:
                bundle = msg.getData();
                if (bundle != null) {
                    try {
                        this.mProvisioningUrl = (String) bundle.get("provisioningUrl");
                    } catch (ClassCastException e) {
                        loge("CMD_ENABLE_MOBILE_PROVISIONING: provisioning url not a string" + e);
                        this.mProvisioningUrl = null;
                    }
                }
                if (TextUtils.isEmpty(this.mProvisioningUrl)) {
                    loge("CMD_ENABLE_MOBILE_PROVISIONING: provisioning url is empty, ignoring");
                    this.mIsProvisioning = false;
                    this.mProvisioningUrl = null;
                    return;
                }
                loge("CMD_ENABLE_MOBILE_PROVISIONING: provisioningUrl=" + this.mProvisioningUrl);
                this.mIsProvisioning = true;
                startProvisioningApnAlarm();
                return;
            case 270374:
                boolean isProvApn;
                log("CMD_IS_PROVISIONING_APN");
                Object apnType2 = null;
                try {
                    bundle = msg.getData();
                    if (bundle != null) {
                        apnType2 = (String) bundle.get("apnType");
                    }
                    if (TextUtils.isEmpty(apnType2)) {
                        loge("CMD_IS_PROVISIONING_APN: apnType is empty");
                        isProvApn = false;
                    } else {
                        isProvApn = isProvisioningApn(apnType2);
                    }
                } catch (ClassCastException e2) {
                    loge("CMD_IS_PROVISIONING_APN: NO provisioning url ignoring");
                    isProvApn = false;
                }
                log("CMD_IS_PROVISIONING_APN: ret=" + isProvApn);
                this.mReplyAc.replyToMessage(msg, 270374, isProvApn ? 1 : 0);
                return;
            case 270375:
                log("EVENT_PROVISIONING_APN_ALARM");
                ApnContext apnCtx = (ApnContext) this.mApnContextsById.get(0);
                if (apnCtx.isProvisioningApn() && apnCtx.isConnectedOrConnecting()) {
                    if (this.mProvisioningApnAlarmTag == msg.arg1) {
                        log("EVENT_PROVISIONING_APN_ALARM: Disconnecting");
                        this.mIsProvisioning = false;
                        this.mProvisioningUrl = null;
                        stopProvisioningApnAlarm();
                        sendCleanUpConnection(true, apnCtx);
                        return;
                    }
                    log("EVENT_PROVISIONING_APN_ALARM: ignore stale tag, mProvisioningApnAlarmTag:" + this.mProvisioningApnAlarmTag + " != arg1:" + msg.arg1);
                    return;
                }
                log("EVENT_PROVISIONING_APN_ALARM: Not connected ignore");
                return;
            case 270376:
                if (msg.arg1 == 1) {
                    handleStartNetStatPoll((Activity) msg.obj);
                    return;
                } else if (msg.arg1 == 0) {
                    handleStopNetStatPoll((Activity) msg.obj);
                    return;
                } else {
                    return;
                }
            case 270377:
                onUpdateIcc();
                if (this.mPhone.getServiceState().getVoiceRegState() == 0 || this.mPhone.getServiceState().getDataRegState() == 0) {
                    setupDataOnConnectableApns(PhoneInternalInterface.REASON_NW_TYPE_CHANGED, RetryFailures.ONLY_ON_CHANGE);
                }
                if (OemConstant.getWlanAssistantEnable(this.mPhone.getContext())) {
                    int dataRadioTech = this.mPhone.getServiceState().getRilDataRadioTechnology();
                    if ((dataRadioTech == 19 && mLastDataRadioTech == 14) || (dataRadioTech == 14 && mLastDataRadioTech == 19)) {
                        mLastDataRadioTech = dataRadioTech;
                        return;
                    }
                    mLastDataRadioTech = dataRadioTech;
                    SubscriptionManager su = SubscriptionManager.from(this.mPhone.getContext());
                    boolean isDefaultDataPhone = this.mPhone.getSubId() == SubscriptionManager.getDefaultDataSubId();
                    if (isDefaultDataPhone) {
                        boolean myMeasureDataState;
                        boolean isRomming = this.mPhone.getServiceState().getRoaming();
                        log("WLAN+ EVENT_DATA_RAT_CHANGED: mMeasureDataState=" + mMeasureDataState + " Roaming=" + isRomming + " DataEnabled=" + (!getDataEnabled() ? haveVsimIgnoreUserDataSetting() : true) + " isDefaultDataPhone=" + isDefaultDataPhone);
                        if (!mMeasureDataState || mDelayMeasure || isRomming) {
                            myMeasureDataState = false;
                        } else {
                            myMeasureDataState = !getDataEnabled() ? haveVsimIgnoreUserDataSetting() : true;
                        }
                        if (myMeasureDataState) {
                            new AnonymousClass7(this).start();
                            return;
                        }
                        return;
                    }
                    return;
                }
                return;
            case 270378:
                if (this.mProvisioningSpinner == msg.obj) {
                    this.mProvisioningSpinner.dismiss();
                    this.mProvisioningSpinner = null;
                    return;
                }
                return;
            case 270379:
                onDeviceProvisionedChange();
                return;
            case 270380:
                String url = msg.obj;
                log("dataConnectionTracker.handleMessage: EVENT_REDIRECTION_DETECTED=" + url);
                onDataConnectionRedirected(url);
                break;
            case 270381:
                handlePcoData((AsyncResult) msg.obj);
                return;
            case 270382:
                onSetCarrierDataEnabled(msg.arg1 == 1);
                return;
            case 270383:
                AsyncResult arscreen = msg.obj;
                if (arscreen != null) {
                    this.mIsScreenOn = ((Boolean) arscreen.result).booleanValue();
                    log("screen on:" + this.mIsScreenOn);
                } else {
                    log("leon EVENT_OEM_SCREEN_CHANGED error");
                }
                stopNetStatPoll();
                startNetStatPoll();
                restartDataStallAlarm();
                return;
            case 270384:
                mDelayMeasure = false;
                log("WLAN+ handlemessage CMD_DELAY_SETUP_DATA mDelayMeasure:false");
                boolean isSupportConcurrent = DataConnectionHelper.getInstance().isDataSupportConcurrent(this.mPhone.getPhoneId());
                logd("onVoiceCallEnded:isDataSupportConcurrent = " + isSupportConcurrent);
                setupDataOnConnectableApns(PhoneInternalInterface.REASON_VOICE_CALL_ENDED);
                notifyVoiceCallEventToDataConnection(this.mInVoiceCall, isSupportConcurrent);
                return;
            case 270839:
                boolean bImsApnChanged = msg.arg1 != 0;
                logd("EVENT_APN_CHANGED_DONE");
                if (bImsApnChanged) {
                    log("ims apn changed");
                    cleanUpConnection(true, (ApnContext) this.mApnContexts.get(ImsSwitchController.IMS_SERVICE));
                    return;
                }
                onApnChangedDone();
                return;
            case 270841:
                onFdnChanged();
                return;
            case 270842:
                if (MTK_CC33_SUPPORT) {
                    logd("EVENT_REMOVE_RESTRICT_EUTRAN");
                    this.mReregisterOnReconnectFailure = false;
                    setupDataOnConnectableApns(PhoneInternalInterface.REASON_PS_RESTRICT_DISABLED);
                    return;
                }
                return;
            case 270843:
                logd("EVENT_RESET_PDP_DONE cid=" + msg.arg1);
                return;
            case 270844:
                if (this.mAllApnSettings == null || this.mAllApnSettings.isEmpty()) {
                    log("EVENT_RESET_ATTACH_APN: Ignore due to null APN list");
                    return;
                } else {
                    setInitialAttachApn();
                    return;
                }
            case 270846:
                onSharedDefaultApnState(msg.arg1);
                return;
            case 270847:
                ar = msg.obj;
                if (ar.exception == null) {
                    int lteAccessStratumDataState;
                    int[] ints = (int[]) ar.result;
                    if (ints.length > 0) {
                        lteAccessStratumDataState = ints[0];
                    } else {
                        lteAccessStratumDataState = -1;
                    }
                    int networkType = ints.length > 1 ? ints[1] : -1;
                    if (lteAccessStratumDataState != 1) {
                        notifyPsNetworkTypeChanged(networkType);
                    } else {
                        this.mPhone.notifyPsNetworkTypeChanged(13);
                    }
                    logd("EVENT_LTE_ACCESS_STRATUM_STATE lteAccessStratumDataState = " + lteAccessStratumDataState + ", networkType = " + networkType);
                    notifyLteAccessStratumChanged(lteAccessStratumDataState);
                    return;
                }
                loge("LteAccessStratumState exception: " + ar.exception);
                return;
            case 270848:
                log("handleMessage : <EVENT_REG_PLMN_CHANGED>");
                if (isOp129IaSupport() || isOp17IaSupport()) {
                    handlePlmnChange((AsyncResult) msg.obj);
                    return;
                }
                return;
            case 270849:
                log("handleMessage : <EVENT_REG_SUSPENDED>");
                if ((isOp129IaSupport() || isOp17IaSupport()) && isNeedToResumeMd()) {
                    handleRegistrationSuspend((AsyncResult) msg.obj);
                    return;
                }
                return;
            case 270850:
                log("handleMessage : <EVENT_SET_RESUME>");
                if (isOp129IaSupport() || isOp17IaSupport()) {
                    handleSetResume();
                    return;
                }
                return;
            case 270851:
                onPcoStatus((AsyncResult) msg.obj);
                return;
            case 270852:
                onMdChangedAttachApn((AsyncResult) msg.obj);
                return;
            case 270853:
                ar = (AsyncResult) msg.obj;
                if (ar == null || ar.result == null) {
                    loge("Parameter error: ret should not be NULL");
                    return;
                } else {
                    onAllowChanged(((int[]) ar.result)[0] == 1);
                    return;
                }
            case 270854:
                onRoamingTypeChanged();
                return;
            case 270855:
                logd("EVENT_MD_DATA_RETRY_COUNT_RESET");
                setupDataOnConnectableApns(PhoneInternalInterface.REASON_MD_DATA_RETRY_COUNT_RESET);
                return;
            case 270856:
                ar = (AsyncResult) msg.obj;
                if (ar.result instanceof Pair) {
                    Pair<Boolean, Integer> p = ar.result;
                    onDataEnabledSettings(((Boolean) p.first).booleanValue(), ((Integer) p.second).intValue());
                    return;
                }
                return;
            default:
                Rlog.e("DcTracker", "Unhandled event=" + msg);
                return;
        }
        onRadioAvailable();
    }

    private int getApnProfileID(String apnType) {
        if (TextUtils.equals(apnType, ImsSwitchController.IMS_SERVICE)) {
            return 2;
        }
        if (TextUtils.equals(apnType, "fota")) {
            return 3;
        }
        if (TextUtils.equals(apnType, "cbs")) {
            return 4;
        }
        if (TextUtils.equals(apnType, "ia")) {
            return 0;
        }
        if (TextUtils.equals(apnType, "dun")) {
            return 1;
        }
        if (TextUtils.equals(apnType, "mms")) {
            return 1001;
        }
        if (TextUtils.equals(apnType, "supl")) {
            return 1002;
        }
        if (TextUtils.equals(apnType, "hipri")) {
            return 1003;
        }
        if (TextUtils.equals(apnType, "dm")) {
            return 1004;
        }
        if (TextUtils.equals(apnType, "wap")) {
            return 1005;
        }
        if (TextUtils.equals(apnType, "net")) {
            return 1006;
        }
        if (TextUtils.equals(apnType, "cmmail")) {
            return 1007;
        }
        if (TextUtils.equals(apnType, "rcse")) {
            return 1008;
        }
        if (TextUtils.equals(apnType, "emergency")) {
            return 1009;
        }
        if (TextUtils.equals(apnType, "xcap")) {
            return GsmVTProvider.SESSION_EVENT_START_COUNTER;
        }
        if (TextUtils.equals(apnType, "rcs")) {
            return 1011;
        }
        if (TextUtils.equals(apnType, "default")) {
            return 0;
        }
        return -1;
    }

    private int getCellLocationId() {
        CellLocation loc = this.mPhone.getCellLocation();
        if (loc == null) {
            return -1;
        }
        if (loc instanceof GsmCellLocation) {
            return ((GsmCellLocation) loc).getCid();
        }
        if (loc instanceof CdmaCellLocation) {
            return ((CdmaCellLocation) loc).getBaseStationId();
        }
        return -1;
    }

    private IccRecords getUiccRecords(int appFamily) {
        return this.mUiccController.getIccRecords(this.mPhone.getPhoneId(), appFamily);
    }

    private void onUpdateIcc() {
        if (this.mUiccController != null) {
            String name;
            int i;
            IccRecords newIccRecords = getUiccRecords(1);
            int dataRat = this.mPhone.getServiceState().getRilDataRadioTechnology();
            newIccRecords = getUiccRecords(getUiccFamilyByRat(dataRat));
            if (newIccRecords == null) {
                newIccRecords = this.mPhone.getIccRecords();
            }
            StringBuilder append = new StringBuilder().append("onUpdateIcc: dataRat= ").append(dataRat).append(" newIccRecords ");
            if (newIccRecords != null) {
                name = newIccRecords.getClass().getName();
            } else {
                name = null;
            }
            log(append.append(name).toString());
            IccRecords r = (IccRecords) this.mIccRecords.get();
            if (!(newIccRecords == null && r == null)) {
                logd("onUpdateIcc: newIccRecords=" + newIccRecords + ", r=" + r);
            }
            if (r != newIccRecords) {
                if (r != null) {
                    log("Removing stale icc objects.");
                    r.unregisterForRecordsLoaded(this);
                    this.mIccRecords.set(null);
                }
                if (newIccRecords == null) {
                    onSimNotReady();
                } else if (SubscriptionManager.isValidSubscriptionId(this.mPhone.getSubId())) {
                    log("New records found.");
                    this.mIccRecords.set(newIccRecords);
                    newIccRecords.registerForRecordsLoaded(this, 270338, null);
                    this.mPhone.getServiceStateTracker().setRadioPowerFromCarrier(true);
                    this.mDataEnabledSettings.setCarrierDataEnabled(true);
                    this.mPhone.getCarrierSignalAgent().reset();
                }
            }
            if (this.mAllApnSettings != null && r == null && newIccRecords == null) {
                post(new AnonymousClass8(this));
            }
            UiccCardApplication app = (UiccCardApplication) this.mUiccCardApplication.get();
            UiccController uiccController = this.mUiccController;
            if (this.mPhone.getPhoneType() == 2) {
                i = 2;
            } else {
                i = 1;
            }
            UiccCardApplication newUiccCardApp = uiccController.getUiccCardApplication(i);
            if (app != newUiccCardApp) {
                if (app != null) {
                    log("Removing stale UiccCardApplication objects.");
                    app.unregisterForFdnChanged(this);
                    this.mUiccCardApplication.set(null);
                }
                if (newUiccCardApp != null) {
                    log("New UiccCardApplication found");
                    newUiccCardApp.registerForFdnChanged(this, 270841, null);
                    this.mUiccCardApplication.set(newUiccCardApp);
                }
            }
        }
    }

    public void update() {
        log("update sub = " + this.mPhone.getSubId());
        onUpdateIcc();
        synchronized (this.mDataEnabledSettings) {
            this.mDataEnabledSettings.setUserDataEnabled(getDataEnabled());
        }
        this.mAutoAttachOnCreation.set(false);
        ((GsmCdmaPhone) this.mPhone).updateCurrentCarrierInProvider();
    }

    public void cleanUpAllConnections(String cause) {
        cleanUpAllConnections(cause, null);
    }

    public void updateRecords() {
        onUpdateIcc();
    }

    public void cleanUpAllConnections(String cause, Message disconnectAllCompleteMsg) {
        log("cleanUpAllConnections");
        if (disconnectAllCompleteMsg != null) {
            this.mDisconnectAllCompleteMsgList.add(disconnectAllCompleteMsg);
        }
        Message msg = obtainMessage(270365);
        msg.obj = cause;
        sendMessage(msg);
    }

    private void notifyDataDisconnectComplete() {
        log("notifyDataDisconnectComplete");
        for (Message m : this.mDisconnectAllCompleteMsgList) {
            m.sendToTarget();
        }
        this.mDisconnectAllCompleteMsgList.clear();
    }

    private void notifyAllDataDisconnected() {
        sEnableFailFastRefCounter = 0;
        this.mFailFast = false;
        this.mAllDataDisconnectedRegistrants.notifyRegistrants();
    }

    public void registerForAllDataDisconnected(Handler h, int what, Object obj) {
        this.mAllDataDisconnectedRegistrants.addUnique(h, what, obj);
        if (isDisconnected()) {
            log("notify All Data Disconnected");
            notifyAllDataDisconnected();
        }
    }

    public void unregisterForAllDataDisconnected(Handler h) {
        this.mAllDataDisconnectedRegistrants.remove(h);
    }

    public void registerForDataEnabledChanged(Handler h, int what, Object obj) {
        this.mDataEnabledSettings.registerForDataEnabledChanged(h, what, obj);
    }

    public void unregisterForDataEnabledChanged(Handler h) {
        this.mDataEnabledSettings.unregisterForDataEnabledChanged(h);
    }

    private void onSetInternalDataEnabled(boolean enabled, Message onCompleteMsg) {
        synchronized (this.mDataEnabledSettings) {
            log("onSetInternalDataEnabled: enabled=" + enabled);
            boolean sendOnComplete = true;
            this.mDataEnabledSettings.setInternalDataEnabled(enabled);
            if (enabled) {
                log("onSetInternalDataEnabled: changed to enabled, try to setup data call");
                onTrySetupData(PhoneInternalInterface.REASON_DATA_ENABLED);
            } else {
                sendOnComplete = false;
                log("onSetInternalDataEnabled: changed to disabled, cleanUpAllConnections");
                cleanUpAllConnections(PhoneInternalInterface.REASON_DATA_DISABLED, onCompleteMsg);
            }
            if (sendOnComplete && onCompleteMsg != null) {
                onCompleteMsg.sendToTarget();
            }
        }
    }

    public boolean setInternalDataEnabled(boolean enable) {
        return setInternalDataEnabled(enable, null);
    }

    public boolean setInternalDataEnabled(boolean enable, Message onCompleteMsg) {
        log("setInternalDataEnabled(" + enable + ")");
        Message msg = obtainMessage(270363, onCompleteMsg);
        msg.arg1 = enable ? 1 : 0;
        sendMessage(msg);
        return true;
    }

    private void log(String s) {
        logd(s);
    }

    private void loge(String s) {
        Rlog.e(LOG_TAG, "[" + this.mPhone.getPhoneId() + "]" + s);
    }

    private void logw(String s) {
        Rlog.w(LOG_TAG, "[" + this.mPhone.getPhoneId() + "]" + s);
    }

    private void logi(String s) {
        Rlog.i(LOG_TAG, "[" + this.mPhone.getPhoneId() + "]" + s);
    }

    private void logd(String s) {
        Rlog.d(LOG_TAG, "[" + this.mPhone.getPhoneId() + "]" + s);
    }

    private void logv(String s) {
        Rlog.v(LOG_TAG, "[" + this.mPhone.getPhoneId() + "]" + s);
    }

    private void logTel(String s) {
        if (TEL_DBG > 0) {
            logd(s);
        } else {
            logv(s);
        }
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        Object[] objArr;
        pw.println("DcTracker:");
        pw.println(" RADIO_TESTS=false");
        pw.println(" isInternalDataEnabled=" + this.mDataEnabledSettings.isInternalDataEnabled());
        pw.println(" isUserDataEnabled=" + this.mDataEnabledSettings.isUserDataEnabled());
        pw.println(" isPolicyDataEnabled=" + this.mDataEnabledSettings.isPolicyDataEnabled());
        pw.flush();
        pw.println(" mRequestedApnType=" + this.mRequestedApnType);
        pw.println(" mPhone=" + this.mPhone.getPhoneName());
        pw.println(" mActivity=" + this.mActivity);
        pw.println(" mState=" + this.mState);
        pw.println(" mTxPkts=" + this.mTxPkts);
        pw.println(" mRxPkts=" + this.mRxPkts);
        pw.println(" mNetStatPollPeriod=" + this.mNetStatPollPeriod);
        pw.println(" mNetStatPollEnabled=" + this.mNetStatPollEnabled);
        pw.println(" mDataStallTxRxSum=" + this.mDataStallTxRxSum);
        pw.println(" mDataStallAlarmTag=" + this.mDataStallAlarmTag);
        pw.println(" mDataStallDetectionEnabled=" + this.mDataStallDetectionEnabled);
        pw.println(" mSentSinceLastRecv=" + this.mSentSinceLastRecv);
        pw.println(" mNoRecvPollCount=" + this.mNoRecvPollCount);
        pw.println(" mResolver=" + this.mResolver);
        pw.println(" mIsWifiConnected=" + this.mIsWifiConnected);
        pw.println(" mReconnectIntent=" + this.mReconnectIntent);
        pw.println(" mAutoAttachOnCreation=" + this.mAutoAttachOnCreation.get());
        pw.println(" mIsScreenOn=" + this.mIsScreenOn);
        pw.println(" mUniqueIdGenerator=" + this.mUniqueIdGenerator);
        pw.flush();
        pw.println(" ***************************************");
        DcController dcc = this.mDcc;
        if (dcc != null) {
            dcc.dump(fd, pw, args);
        } else {
            pw.println(" mDcc=null");
        }
        pw.println(" ***************************************");
        if (this.mDataConnections != null) {
            Set<Entry<Integer, DataConnection>> mDcSet = this.mDataConnections.entrySet();
            pw.println(" mDataConnections: count=" + mDcSet.size());
            for (Entry<Integer, DataConnection> entry : mDcSet) {
                objArr = new Object[1];
                objArr[0] = entry.getKey();
                pw.printf(" *** mDataConnection[%d] \n", objArr);
                ((DataConnection) entry.getValue()).dump(fd, pw, args);
            }
        } else {
            pw.println("mDataConnections=null");
        }
        pw.println(" ***************************************");
        pw.flush();
        HashMap<String, Integer> apnToDcId = this.mApnToDataConnectionId;
        if (apnToDcId != null) {
            Set<Entry<String, Integer>> apnToDcIdSet = apnToDcId.entrySet();
            pw.println(" mApnToDataConnectonId size=" + apnToDcIdSet.size());
            for (Entry<String, Integer> entry2 : apnToDcIdSet) {
                objArr = new Object[2];
                objArr[0] = entry2.getKey();
                objArr[1] = entry2.getValue();
                pw.printf(" mApnToDataConnectonId[%s]=%d\n", objArr);
            }
        } else {
            pw.println("mApnToDataConnectionId=null");
        }
        pw.println(" ***************************************");
        pw.flush();
        ConcurrentHashMap<String, ApnContext> apnCtxs = this.mApnContexts;
        if (apnCtxs != null) {
            Set<Entry<String, ApnContext>> apnCtxsSet = apnCtxs.entrySet();
            pw.println(" mApnContexts size=" + apnCtxsSet.size());
            for (Entry<String, ApnContext> entry3 : apnCtxsSet) {
                ((ApnContext) entry3.getValue()).dump(fd, pw, args);
            }
            pw.println(" ***************************************");
        } else {
            pw.println(" mApnContexts=null");
        }
        pw.flush();
        ArrayList<ApnSetting> apnSettings = this.mAllApnSettings;
        if (apnSettings != null) {
            pw.println(" mAllApnSettings size=" + apnSettings.size());
            for (int i = 0; i < apnSettings.size(); i++) {
                Integer[] numArr = new Object[2];
                numArr[0] = Integer.valueOf(i);
                numArr[1] = apnSettings.get(i);
                pw.printf(" mAllApnSettings[%d]: %s\n", numArr);
            }
            pw.flush();
        } else {
            pw.println(" mAllApnSettings=null");
        }
        pw.println(" mPreferredApn=" + this.mPreferredApn);
        pw.println(" mIsPsRestricted=" + this.mIsPsRestricted);
        pw.println(" mIsDisposed=" + this.mIsDisposed);
        pw.println(" mIntentReceiver=" + this.mIntentReceiver);
        pw.println(" mReregisterOnReconnectFailure=" + this.mReregisterOnReconnectFailure);
        pw.println(" canSetPreferApn=" + this.mCanSetPreferApn);
        pw.println(" mApnObserver=" + this.mApnObserver);
        pw.println(" getOverallState=" + getOverallState());
        pw.println(" mDataConnectionAsyncChannels=%s\n" + this.mDataConnectionAcHashMap);
        pw.println(" mAttached=" + this.mAttached.get());
        pw.flush();
    }

    public String[] getPcscfAddress(String apnType) {
        log("getPcscfAddress()");
        if (apnType == null) {
            log("apnType is null, return null");
            return null;
        }
        ApnContext apnContext;
        if (TextUtils.equals(apnType, "emergency")) {
            apnContext = (ApnContext) this.mApnContextsById.get(9);
        } else if (TextUtils.equals(apnType, ImsSwitchController.IMS_SERVICE)) {
            apnContext = (ApnContext) this.mApnContextsById.get(5);
        } else {
            log("apnType is invalid, return null");
            return null;
        }
        if (apnContext == null) {
            log("apnContext is null, return null");
            return null;
        }
        DcAsyncChannel dcac = apnContext.getDcAc();
        if (dcac == null) {
            return null;
        }
        String[] result = dcac.getPcscfAddr();
        for (int i = 0; i < result.length; i++) {
            log("Pcscf[" + i + "]: " + result[i]);
        }
        return result;
    }

    private void initEmergencyApnSetting() {
        if (this.mEmergencyApn == null) {
            Cursor cursor = this.mPhone.getContext().getContentResolver().query(Carriers.CONTENT_URI, null, "type=\"emergency\" and numeric=''", null, null);
            if (cursor != null) {
                if (cursor.getCount() <= 0) {
                    log("No record for emergency APN");
                } else if (cursor.moveToFirst()) {
                    this.mEmergencyApn = makeApnSetting(cursor);
                    log("Loaded default emergency APN: " + this.mEmergencyApn);
                }
                cursor.close();
                return;
            }
            log("No emergency APN found in DB");
            return;
        }
        log("mEmergencyApn already loaded: " + this.mEmergencyApn);
    }

    private void addEmergencyApnSetting() {
        if (this.mEmergencyApn == null) {
            return;
        }
        if (this.mAllApnSettings == null) {
            this.mAllApnSettings = new ArrayList();
            return;
        }
        boolean hasEmergencyApn = false;
        for (ApnSetting apn : this.mAllApnSettings) {
            if (ArrayUtils.contains(apn.types, "emergency")) {
                hasEmergencyApn = true;
                break;
            }
        }
        if (hasEmergencyApn) {
            log("addEmergencyApnSetting - E-APN setting is already present");
        } else {
            this.mAllApnSettings.add(this.mEmergencyApn);
        }
    }

    private void cleanUpConnectionsOnUpdatedApns(boolean tearDown) {
        log("cleanUpConnectionsOnUpdatedApns: tearDown=" + tearDown);
        if (this.mAllApnSettings.isEmpty()) {
            cleanUpAllConnections(tearDown, PhoneInternalInterface.REASON_APN_CHANGED);
        } else {
            for (ApnContext apnContext : this.mApnContexts.values()) {
                if (VDBG) {
                    log("cleanUpConnectionsOnUpdatedApns for " + apnContext);
                }
                boolean cleanUpApn = true;
                ArrayList<ApnSetting> currentWaitingApns = apnContext.getWaitingApns();
                if (!(currentWaitingApns == null || apnContext.isDisconnected())) {
                    ArrayList<ApnSetting> waitingApns = buildWaitingApns(apnContext.getApnType(), this.mPhone.getServiceState().getRilDataRadioTechnology());
                    if (VDBG) {
                        log("new waitingApns:" + waitingApns);
                    }
                    if (waitingApns.size() == currentWaitingApns.size()) {
                        cleanUpApn = false;
                        int i = 0;
                        while (i < waitingApns.size()) {
                            boolean z;
                            ApnSetting apnSetting = (ApnSetting) currentWaitingApns.get(i);
                            if (VZW_FEATURE) {
                                z = false;
                            } else {
                                z = true;
                            }
                            String currentWaitingApn = apnSetting.toStringIgnoreName(z);
                            apnSetting = (ApnSetting) waitingApns.get(i);
                            if (VZW_FEATURE) {
                                z = false;
                            } else {
                                z = true;
                            }
                            if (TextUtils.equals(currentWaitingApn, apnSetting.toStringIgnoreName(z))) {
                                i++;
                            } else {
                                if (VDBG) {
                                    log("new waiting apn is different at " + i);
                                }
                                cleanUpApn = true;
                                apnContext.setWaitingApns(waitingApns);
                            }
                        }
                    }
                }
                if (cleanUpApn) {
                    apnContext.setReason(PhoneInternalInterface.REASON_APN_CHANGED);
                    cleanUpConnection(true, apnContext);
                }
            }
        }
        if (!isConnected()) {
            stopNetStatPoll();
            stopDataStallAlarm();
        }
        this.mRequestedApnType = "default";
        log("mDisconnectPendingCount = " + this.mDisconnectPendingCount);
        if (tearDown && this.mDisconnectPendingCount == 0) {
            notifyDataDisconnectComplete();
            notifyAllDataDisconnected();
        }
    }

    protected int getPdpConnectionPoolSize() {
        if (isSupportThrottlingApn()) {
            return 8;
        }
        return 2;
    }

    private boolean isSupportThrottlingApn() {
        return THROTTLING_APN_ENABLED || SystemProperties.getInt(PROPERTY_THROTTLING_APN_ENABLED, 0) == 1;
    }

    private int generateDataConnectionId(String reqApnType, ApnSetting apnSetting) {
        int i = 0;
        AtomicInteger idGenerator = this.mOthersUniqueIdGenerator;
        for (String apn : HIGH_THROUGHPUT_APN) {
            if (apnSetting != null && apnSetting.canHandleType(apn) && !"emergency".equals(reqApnType) && !apnSetting.canHandleType(ImsSwitchController.IMS_SERVICE)) {
                idGenerator = this.mHighThroughputIdGenerator;
                logd("generateDataConnectionId use high throughput DataConnection id generator");
                break;
            }
        }
        if (idGenerator != this.mHighThroughputIdGenerator) {
            String[] strArr = IMS_APN;
            int length = strArr.length;
            while (i < length) {
                String apn2 = strArr[i];
                if ((!"emergency".equals(apn2) || "emergency".equals(reqApnType)) && apnSetting != null && apnSetting.canHandleType(apn2)) {
                    int idStart = 4;
                    if ("emergency".equals(apn2)) {
                        idStart = 5;
                    }
                    this.mImsUniqueIdGenerator.set(idStart);
                    idGenerator = this.mImsUniqueIdGenerator;
                    logd("generateDataConnectionId use ims DataConnection id generator");
                } else {
                    i++;
                }
            }
        }
        int id = idGenerator.getAndIncrement();
        if (idGenerator == this.mHighThroughputIdGenerator && id > 1) {
            loge("Max id of highthrouthput is 1, but generated id is " + id);
            idGenerator.getAndDecrement();
            id = -1;
        } else if (idGenerator == this.mOthersUniqueIdGenerator && id > 3) {
            loge("Max id of others is 3, but generated id is " + id);
            idGenerator.getAndDecrement();
            id = -1;
        } else if (idGenerator == this.mImsUniqueIdGenerator && id > 6) {
            loge("Max id of others is 6, but generated id is " + id);
            idGenerator.getAndDecrement();
            id = -1;
        }
        log("generateDataConnectionId id = " + id);
        return id;
    }

    public void deactivatePdpByCid(int cid) {
        this.mPhone.mCi.deactivateDataCall(cid, 2, obtainMessage(270843, cid, 0));
    }

    public boolean isVsimActive(int phoneId) {
        int phoneNum = TelephonyManager.getDefault().getPhoneCount();
        for (int id = 0; id < phoneNum; id++) {
            if (id != phoneId) {
                TelephonyManager.getDefault();
                String vsimEnabled = TelephonyManager.getTelephonyProperty(id, PROPERTY_VSIM_ENABLE, "0");
                if ((vsimEnabled.isEmpty() ? 0 : Integer.parseInt(vsimEnabled)) == 2) {
                    logd("Remote Vsim enabled on phone " + id + " and downloaded by phone" + phoneId);
                    return true;
                }
            }
        }
        return false;
    }

    private void syncApnToMd() {
        ArrayList<ApnSetting> tmpAllApnSettings = new ArrayList();
        ApnSetting dunApnSetting = fetchDunApn();
        boolean bAddDunApnSettingToList = true;
        if (this.mAllApnSettings != null) {
            tmpAllApnSettings.addAll(this.mAllApnSettings);
        }
        if (dunApnSetting != null) {
            for (ApnSetting apn : tmpAllApnSettings) {
                if (TextUtils.equals(apn.apn, dunApnSetting.apn)) {
                    bAddDunApnSettingToList = false;
                    break;
                }
            }
            log("syncApnToMd: bAddToApnSettingList = " + bAddDunApnSettingToList);
            if (bAddDunApnSettingToList) {
                tmpAllApnSettings.add(dunApnSetting);
            }
        }
        if (tmpAllApnSettings.isEmpty()) {
            log("syncApnToMd: tmpAllApnSettings is empty!");
            return;
        }
        ArrayList<String> aryApn = new ArrayList();
        for (ApnSetting apn2 : tmpAllApnSettings) {
            int i;
            StringBuilder sb = new StringBuilder();
            if (apn2.apn != null) {
                sb.append(apn2.apn.replace(";", "/3B"));
            }
            sb.append(";");
            if (apn2.user != null) {
                sb.append(apn2.user.replace(";", "/3B"));
            }
            sb.append(";");
            if (apn2.password != null) {
                sb.append(apn2.password.replace(";", "/3B"));
            }
            sb.append(";");
            int j;
            if (ArrayUtils.contains(apn2.types, CharacterSets.MIMENAME_ANY_CHARSET)) {
                for (j = 0; j < PhoneConstants.APN_TYPES.length; j++) {
                    sb.append(PhoneConstants.APN_TYPES[j]);
                    if (j < PhoneConstants.APN_TYPES.length - 1) {
                        sb.append(",");
                    }
                }
            } else {
                for (j = 0; j < apn2.types.length; j++) {
                    sb.append(apn2.types[j]);
                    if (j < apn2.types.length - 1) {
                        sb.append(",");
                    }
                }
            }
            sb.append(";");
            sb.append(apn2.protocol);
            sb.append(";");
            sb.append(apn2.roamingProtocol);
            sb.append(";");
            int authType = apn2.authType;
            if (authType == -1) {
                if (TextUtils.isEmpty(apn2.user)) {
                    authType = 0;
                } else {
                    authType = 3;
                }
            }
            sb.append(authType);
            sb.append(";");
            if (apn2.carrierEnabled) {
                i = 1;
            } else {
                i = 0;
            }
            sb.append(i);
            sb.append(";");
            sb.append(apn2.maxConns);
            sb.append(";");
            sb.append(apn2.maxConnsTime);
            sb.append(";");
            sb.append(apn2.waitTime);
            sb.append(";");
            sb.append(apn2.bearerBitmask);
            sb.append(";");
            sb.append(apn2.inactiveTimer);
            log("syncApnToMd: apn: " + sb.toString());
            aryApn.add(sb.toString());
        }
        if (aryApn.size() > 0) {
            this.mPhone.mCi.syncApnTable((String[]) aryApn.toArray(new String[aryApn.size()]), null);
        }
    }

    public int getClassType(ApnSetting apn) {
        int classType = 3;
        if (ArrayUtils.contains(apn.types, "emergency") || VZW_EMERGENCY_NI.compareToIgnoreCase(apn.apn) == 0) {
            classType = 0;
        } else if (ArrayUtils.contains(apn.types, ImsSwitchController.IMS_SERVICE) || VZW_IMS_NI.compareToIgnoreCase(apn.apn) == 0) {
            classType = 1;
        } else if (VZW_ADMIN_NI.compareToIgnoreCase(apn.apn) == 0) {
            classType = 2;
        } else if (VZW_APP_NI.compareToIgnoreCase(apn.apn) == 0) {
            classType = 4;
        } else if (VZW_800_NI.compareToIgnoreCase(apn.apn) == 0) {
            classType = 5;
        } else if (ArrayUtils.contains(apn.types, "default")) {
            classType = 3;
        } else {
            log("getClassType: set to default class 3");
        }
        logd("getClassType:" + classType);
        return classType;
    }

    public ApnSetting getClassTypeApn(int classType) {
        ApnSetting classTypeApn = null;
        String apnName = UsimPBMemInfo.STRING_NOT_SET;
        if (classType == 0) {
            apnName = VZW_EMERGENCY_NI;
        } else if (1 == classType) {
            apnName = VZW_IMS_NI;
        } else if (2 == classType) {
            apnName = VZW_ADMIN_NI;
        } else if (3 == classType) {
            apnName = VZW_INTERNET_NI;
        } else if (4 == classType) {
            apnName = VZW_APP_NI;
        } else if (5 == classType) {
            apnName = VZW_800_NI;
        } else {
            log("getClassTypeApn: can't handle class:" + classType);
            return null;
        }
        if (this.mAllApnSettings != null) {
            for (ApnSetting apn : this.mAllApnSettings) {
                if (apnName.compareToIgnoreCase(apn.apn) == 0) {
                    classTypeApn = apn;
                }
            }
        }
        logd("getClassTypeApn:" + classTypeApn + ", class:" + classType);
        return classTypeApn;
    }

    private void onSharedDefaultApnState(int newDefaultRefCount) {
        logd("onSharedDefaultApnState: newDefaultRefCount = " + newDefaultRefCount + ", curDefaultRefCount = " + this.mDefaultRefCount);
        if (newDefaultRefCount != this.mDefaultRefCount) {
            if (newDefaultRefCount > 1) {
                this.mIsSharedDefaultApn = true;
            } else {
                this.mIsSharedDefaultApn = false;
            }
            this.mDefaultRefCount = newDefaultRefCount;
            logd("onSharedDefaultApnState: mIsSharedDefaultApn = " + this.mIsSharedDefaultApn);
            notifySharedDefaultApn(this.mIsSharedDefaultApn);
        }
    }

    public void onSetLteAccessStratumReport(boolean enabled, Message response) {
        this.mPhone.mCi.setLteAccessStratumReport(enabled, response);
    }

    public void onSetLteUplinkDataTransfer(int timeMillis, Message response) {
        for (ApnContext apnContext : this.mApnContexts.values()) {
            if ("default".equals(apnContext.getApnType())) {
                try {
                    this.mPhone.mCi.setLteUplinkDataTransfer(timeMillis, apnContext.getDcAc().getCidSync(), response);
                } catch (Exception e) {
                    loge("getDcAc fail!");
                    e.printStackTrace();
                    if (response != null) {
                        AsyncResult.forMessage(response, null, new CommandException(Error.GENERIC_FAILURE));
                        response.sendToTarget();
                    }
                }
            }
        }
    }

    private void notifySharedDefaultApn(boolean isSharedDefaultApn) {
        this.mPhone.notifySharedDefaultApnStateChanged(isSharedDefaultApn);
    }

    private void notifyLteAccessStratumChanged(int lteAccessStratumDataState) {
        String str;
        if (lteAccessStratumDataState == 1) {
            str = "connected";
        } else {
            str = "idle";
        }
        this.mLteAccessStratumDataState = str;
        logd("notifyLteAccessStratumChanged mLteAccessStratumDataState = " + this.mLteAccessStratumDataState);
        this.mPhone.notifyLteAccessStratumChanged(this.mLteAccessStratumDataState);
    }

    private void notifyPsNetworkTypeChanged(int newRilNwType) {
        int newNwType = this.mPhone.getServiceState().rilRadioTechnologyToNetworkTypeEx(newRilNwType);
        logd("notifyPsNetworkTypeChanged mNetworkType = " + this.mNetworkType + ", newNwType = " + newNwType + ", newRilNwType = " + newRilNwType);
        if (newNwType != this.mNetworkType) {
            this.mNetworkType = newNwType;
            this.mPhone.notifyPsNetworkTypeChanged(this.mNetworkType);
        }
    }

    public String getLteAccessStratumState() {
        return this.mLteAccessStratumDataState;
    }

    public boolean isSharedDefaultApn() {
        return this.mIsSharedDefaultApn;
    }

    private void resetPollStats() {
        this.mTxPkts = -1;
        this.mRxPkts = -1;
        this.mNetStatPollPeriod = 1000;
    }

    private void startNetStatPoll() {
        if (getOverallState() == State.CONNECTED && !this.mNetStatPollEnabled) {
            log("startNetStatPoll");
            resetPollStats();
            this.mNetStatPollEnabled = true;
            this.mPollNetStat.run();
        }
        if (this.mPhone != null) {
            this.mPhone.notifyDataActivity();
        }
    }

    private void stopNetStatPoll() {
        this.mNetStatPollEnabled = false;
        removeCallbacks(this.mPollNetStat);
        log("stopNetStatPoll");
        if (this.mPhone != null) {
            this.mPhone.notifyDataActivity();
        }
    }

    public void sendStartNetStatPoll(Activity activity) {
        Message msg = obtainMessage(270376);
        msg.arg1 = 1;
        msg.obj = activity;
        sendMessage(msg);
    }

    private void handleStartNetStatPoll(Activity activity) {
        startNetStatPoll();
        startDataStallAlarm(false);
        setActivity(activity);
    }

    public void sendStopNetStatPoll(Activity activity) {
        Message msg = obtainMessage(270376);
        msg.arg1 = 0;
        msg.obj = activity;
        sendMessage(msg);
    }

    private void handleStopNetStatPoll(Activity activity) {
        stopNetStatPoll();
        stopDataStallAlarm();
        setActivity(activity);
    }

    private void updateDataActivity() {
        TxRxSum preTxRxSum = new TxRxSum(this.mTxPkts, this.mRxPkts);
        TxRxSum curTxRxSum = new TxRxSum();
        curTxRxSum.updateTxRxSum();
        this.mTxPkts = curTxRxSum.txPkts;
        this.mRxPkts = curTxRxSum.rxPkts;
        if (VDBG) {
            log("updateDataActivity: curTxRxSum=" + curTxRxSum + " preTxRxSum=" + preTxRxSum);
        }
        if (!this.mNetStatPollEnabled) {
            return;
        }
        if (preTxRxSum.txPkts > 0 || preTxRxSum.rxPkts > 0) {
            long sent = this.mTxPkts - preTxRxSum.txPkts;
            long received = this.mRxPkts - preTxRxSum.rxPkts;
            if (VDBG) {
                log("updateDataActivity: sent=" + sent + " received=" + received);
            }
            Activity newActivity = (sent <= 0 || received <= 0) ? (sent <= 0 || received != 0) ? (sent != 0 || received <= 0) ? this.mActivity == Activity.DORMANT ? this.mActivity : Activity.NONE : Activity.DATAIN : Activity.DATAOUT : Activity.DATAINANDOUT;
            if (this.mActivity != newActivity && this.mIsScreenOn) {
                if (VDBG) {
                    log("updateDataActivity: newActivity=" + newActivity);
                }
                this.mActivity = newActivity;
                this.mPhone.notifyDataActivity();
            }
        }
    }

    private void handlePcoData(AsyncResult ar) {
        if (ar.exception != null) {
            Rlog.e(LOG_TAG, "PCO_DATA exception: " + ar.exception);
            return;
        }
        PcoData pcoData = ar.result;
        ArrayList<DataConnection> dcList = new ArrayList();
        DataConnection temp = this.mDcc.getActiveDcByCid(pcoData.cid);
        if (temp != null) {
            dcList.add(temp);
        }
        if (dcList.size() == 0) {
            Rlog.e(LOG_TAG, "PCO_DATA for unknown cid: " + pcoData.cid + ", inferring");
            for (DataConnection dc : this.mDataConnections.values()) {
                int cid = dc.getCid();
                if (cid == pcoData.cid) {
                    if (VDBG) {
                        Rlog.d(LOG_TAG, "  found " + dc);
                    }
                    dcList.clear();
                    dcList.add(dc);
                } else if (cid == -1) {
                    for (ApnContext apnContext : dc.mApnContexts.keySet()) {
                        if (apnContext.getState() == State.CONNECTING) {
                            if (VDBG) {
                                Rlog.d(LOG_TAG, "  found potential " + dc);
                            }
                            dcList.add(dc);
                        }
                    }
                }
            }
        }
        if (dcList.size() == 0) {
            Rlog.e(LOG_TAG, "PCO_DATA - couldn't infer cid");
            return;
        }
        for (DataConnection dc2 : dcList) {
            if (dc2.mApnContexts.size() == 0) {
                break;
            }
            for (ApnContext apnContext2 : dc2.mApnContexts.keySet()) {
                String apnType = apnContext2.getApnType();
                Intent intent = new Intent("android.intent.action.CARRIER_SIGNAL_PCO_VALUE");
                intent.putExtra("apnType", apnType);
                intent.putExtra("apnProto", pcoData.bearerProto);
                intent.putExtra("pcoId", pcoData.pcoId);
                intent.putExtra("pcoValue", pcoData.contents);
                this.mPhone.getCarrierSignalAgent().notifyCarrierSignalReceivers(intent);
            }
        }
    }

    private int getRecoveryAction() {
        int action = System.getInt(this.mResolver, "radio.data.stall.recovery.action", 0);
        if (VDBG_STALL) {
            log("getRecoveryAction: " + action);
        }
        return action;
    }

    private void putRecoveryAction(int action) {
        System.putInt(this.mResolver, "radio.data.stall.recovery.action", action);
        if (VDBG_STALL) {
            log("putRecoveryAction: " + action);
        }
    }

    private void doRecovery() {
        if (getOverallState() == State.CONNECTED) {
            int recoveryAction = getRecoveryAction();
            TelephonyMetrics.getInstance().writeDataStallEvent(this.mPhone.getPhoneId(), recoveryAction);
            switch (recoveryAction) {
                case 0:
                    EventLog.writeEvent(EventLogTags.DATA_STALL_RECOVERY_GET_DATA_CALL_LIST, this.mSentSinceLastRecv);
                    log("doRecovery() get data call list");
                    this.mPhone.mCi.getDataCallList(obtainMessage(270340));
                    putRecoveryAction(1);
                    break;
                case 1:
                    EventLog.writeEvent(EventLogTags.DATA_STALL_RECOVERY_CLEANUP, this.mSentSinceLastRecv);
                    Intent intent = new Intent("com.mediatek.log2server.EXCEPTION_HAPPEND");
                    intent.putExtra("Reason", "SmartLogging");
                    intent.putExtra("from_where", LOG_TAG);
                    this.mPhone.getContext().sendBroadcast(intent);
                    log("Broadcast for SmartLogging - NO DATA");
                    log("doRecovery() cleanup all connections");
                    cleanUpAllConnections(PhoneInternalInterface.REASON_PDP_RESET);
                    putRecoveryAction(2);
                    break;
                case 2:
                    EventLog.writeEvent(EventLogTags.DATA_STALL_RECOVERY_REREGISTER, this.mSentSinceLastRecv);
                    log("doRecovery() re-register");
                    this.mPhone.getServiceStateTracker().reRegisterNetwork(null);
                    putRecoveryAction(3);
                    break;
                case 3:
                    EventLog.writeEvent(EventLogTags.DATA_STALL_RECOVERY_RADIO_RESTART, this.mSentSinceLastRecv);
                    log("restarting radio");
                    putRecoveryAction(4);
                    restartRadio();
                    break;
                case 4:
                    EventLog.writeEvent(EventLogTags.DATA_STALL_RECOVERY_RADIO_RESTART_WITH_PROP, -1);
                    log("restarting radio with gsm.radioreset to true");
                    SystemProperties.set(this.RADIO_RESET_PROPERTY, "true");
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                    }
                    restartRadio();
                    putRecoveryAction(0);
                    break;
                default:
                    loge("doRecovery: Invalid recoveryAction=" + recoveryAction);
                    break;
            }
            this.mSentSinceLastRecv = 0;
        }
    }

    private void updateDataStallInfo() {
        TxRxSum preTxRxSum = new TxRxSum(this.mDataStallTxRxSum);
        this.mDataStallTxRxSum.updateTxRxSum();
        if (VDBG_STALL) {
            log("updateDataStallInfo: mDataStallTxRxSum=" + this.mDataStallTxRxSum + " preTxRxSum=" + preTxRxSum);
        }
        long sent = this.mDataStallTxRxSum.txPkts - preTxRxSum.txPkts;
        long received = this.mDataStallTxRxSum.rxPkts - preTxRxSum.rxPkts;
        if (sent > 0 && received > 0) {
            if (VDBG_STALL) {
                log("updateDataStallInfo: IN/OUT");
            }
            this.mSentSinceLastRecv = 0;
            putRecoveryAction(0);
        } else if (sent > 0 && received == 0) {
            if (this.mPhone.getState() == PhoneConstants.State.IDLE) {
                this.mSentSinceLastRecv += sent;
            } else {
                this.mSentSinceLastRecv = 0;
            }
            log("updateDataStallInfo: OUT sent=" + sent + " mSentSinceLastRecv=" + this.mSentSinceLastRecv);
        } else if (sent == 0 && received > 0) {
            if (VDBG_STALL) {
                log("updateDataStallInfo: IN");
            }
            this.mSentSinceLastRecv = 0;
            putRecoveryAction(0);
        } else if (VDBG_STALL) {
            log("updateDataStallInfo: NONE");
        }
    }

    private void onDataStallAlarm(int tag) {
        if (this.mDataStallAlarmTag != tag) {
            log("onDataStallAlarm: ignore, tag=" + tag + " expecting " + this.mDataStallAlarmTag);
            return;
        }
        updateDataStallInfo();
        int hangWatchdogTrigger = Global.getInt(this.mResolver, "pdp_watchdog_trigger_packet_count", 10);
        boolean suspectedStall = false;
        if (this.mSentSinceLastRecv >= ((long) hangWatchdogTrigger)) {
            log("onDataStallAlarm: tag=" + tag + " do recovery action=" + getRecoveryAction());
            if (isOnlyIMSorEIMSPdnConnected() || skipDataStallAlarm()) {
                log("onDataStallAlarm: only IMS or EIMS Connected, or switch data-stall off, skip it!");
            } else {
                suspectedStall = true;
                sendMessage(obtainMessage(270354));
            }
        } else if (VDBG_STALL) {
            log("onDataStallAlarm: tag=" + tag + " Sent " + String.valueOf(this.mSentSinceLastRecv) + " pkts since last received, < watchdogTrigger=" + hangWatchdogTrigger);
        }
        startDataStallAlarm(suspectedStall);
    }

    private void startDataStallAlarm(boolean suspectedStall) {
        int nextAction = getRecoveryAction();
        if (this.mDataStallDetectionEnabled && getOverallState() == State.CONNECTED) {
            int delayInMs;
            if (this.mIsScreenOn || suspectedStall || RecoveryAction.isAggressiveRecovery(nextAction)) {
                delayInMs = Global.getInt(this.mResolver, "data_stall_alarm_aggressive_delay_in_ms", 60000);
            } else {
                delayInMs = Global.getInt(this.mResolver, "data_stall_alarm_non_aggressive_delay_in_ms", DATA_STALL_ALARM_NON_AGGRESSIVE_DELAY_IN_MS_DEFAULT);
            }
            this.mDataStallAlarmTag++;
            if (VDBG_STALL) {
                log("startDataStallAlarm: tag=" + this.mDataStallAlarmTag + " delay=" + (delayInMs / 1000) + "s");
            }
            Intent intent = new Intent(INTENT_DATA_STALL_ALARM);
            intent.putExtra(DATA_STALL_ALARM_TAG_EXTRA, this.mDataStallAlarmTag);
            this.mDataStallAlarmIntent = PendingIntent.getBroadcast(this.mPhone.getContext(), 0, intent, 134217728);
            this.mAlarmManager.set(2, SystemClock.elapsedRealtime() + ((long) delayInMs), this.mDataStallAlarmIntent);
        } else if (VDBG_STALL) {
            log("startDataStallAlarm: NOT started, no connection tag=" + this.mDataStallAlarmTag);
        }
    }

    private void stopDataStallAlarm() {
        if (VDBG_STALL) {
            log("stopDataStallAlarm: current tag=" + this.mDataStallAlarmTag + " mDataStallAlarmIntent=" + this.mDataStallAlarmIntent);
        }
        this.mDataStallAlarmTag++;
        if (this.mDataStallAlarmIntent != null) {
            this.mAlarmManager.cancel(this.mDataStallAlarmIntent);
            this.mDataStallAlarmIntent = null;
        }
    }

    private void restartDataStallAlarm() {
        if (!isConnected()) {
            return;
        }
        if (RecoveryAction.isAggressiveRecovery(getRecoveryAction())) {
            log("restartDataStallAlarm: action is pending. not resetting the alarm.");
            return;
        }
        if (VDBG_STALL) {
            log("restartDataStallAlarm: stop then start.");
        }
        stopDataStallAlarm();
        startDataStallAlarm(false);
    }

    private void onActionIntentProvisioningApnAlarm(Intent intent) {
        log("onActionIntentProvisioningApnAlarm: action=" + intent.getAction());
        Message msg = obtainMessage(270375, intent.getAction());
        msg.arg1 = intent.getIntExtra(PROVISIONING_APN_ALARM_TAG_EXTRA, 0);
        sendMessage(msg);
    }

    private void startProvisioningApnAlarm() {
        int delayInMs = Global.getInt(this.mResolver, "provisioning_apn_alarm_delay_in_ms", PROVISIONING_APN_ALARM_DELAY_IN_MS_DEFAULT);
        if (Build.IS_DEBUGGABLE) {
            try {
                delayInMs = Integer.parseInt(System.getProperty(DEBUG_PROV_APN_ALARM, Integer.toString(delayInMs)));
            } catch (NumberFormatException e) {
                loge("startProvisioningApnAlarm: e=" + e);
            }
        }
        this.mProvisioningApnAlarmTag++;
        log("startProvisioningApnAlarm: tag=" + this.mProvisioningApnAlarmTag + " delay=" + (delayInMs / 1000) + "s");
        Intent intent = new Intent(INTENT_PROVISIONING_APN_ALARM);
        intent.putExtra(PROVISIONING_APN_ALARM_TAG_EXTRA, this.mProvisioningApnAlarmTag);
        this.mProvisioningApnAlarmIntent = PendingIntent.getBroadcast(this.mPhone.getContext(), 0, intent, 134217728);
        this.mAlarmManager.set(2, SystemClock.elapsedRealtime() + ((long) delayInMs), this.mProvisioningApnAlarmIntent);
    }

    private void stopProvisioningApnAlarm() {
        log("stopProvisioningApnAlarm: current tag=" + this.mProvisioningApnAlarmTag + " mProvsioningApnAlarmIntent=" + this.mProvisioningApnAlarmIntent);
        this.mProvisioningApnAlarmTag++;
        if (this.mProvisioningApnAlarmIntent != null) {
            this.mAlarmManager.cancel(this.mProvisioningApnAlarmIntent);
            this.mProvisioningApnAlarmIntent = null;
        }
    }

    public boolean isOnlyIMSorEIMSPdnConnected() {
        if (this.mTelDevController.getModem(0) == null || !this.mTelDevController.getModem(0).hasMdAutoSetupImsCapability()) {
            boolean bIsOnlyIMSorEIMSConnected = false;
            if (MTK_IMS_SUPPORT) {
                for (ApnContext apnContext : this.mApnContexts.values()) {
                    String apnType = apnContext.getApnType();
                    if (!apnContext.isDisconnected()) {
                        if (!apnType.equals(ImsSwitchController.IMS_SERVICE) && !apnType.equals("emergency")) {
                            logd("apnType: " + apnType + " is still conntected!!");
                            bIsOnlyIMSorEIMSConnected = false;
                            break;
                        }
                        bIsOnlyIMSorEIMSConnected = true;
                    }
                }
            }
            return bIsOnlyIMSorEIMSConnected;
        }
        logd("ignore IMS/EIMS special handle on 93MD");
        return false;
    }

    private String getIMSApnSetting(ArrayList<ApnSetting> apnSettings) {
        if (apnSettings == null || apnSettings.size() == 0) {
            return UsimPBMemInfo.STRING_NOT_SET;
        }
        StringBuilder sb = new StringBuilder();
        for (ApnSetting t : apnSettings) {
            if (t.canHandleType(ImsSwitchController.IMS_SERVICE)) {
                sb.append(apnToStringIgnoreName(t));
            }
        }
        logd("getIMSApnSetting, apnsToStringIgnoreName: sb = " + sb.toString());
        return sb.toString();
    }

    private boolean isIMSApnSettingChanged(ArrayList<ApnSetting> prevApnList, ArrayList<ApnSetting> currApnList) {
        String prevIMSApn = getIMSApnSetting(prevApnList);
        String currIMSApn = getIMSApnSetting(currApnList);
        if (prevIMSApn.isEmpty() || TextUtils.equals(prevIMSApn, currIMSApn)) {
            return false;
        }
        return true;
    }

    private String apnToStringIgnoreName(ApnSetting apnSetting) {
        if (apnSetting == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(apnSetting.id).append(", ").append(apnSetting.numeric).append(", ").append(apnSetting.apn).append(", ").append(apnSetting.proxy).append(", ").append(apnSetting.mmsc).append(", ").append(apnSetting.mmsProxy).append(", ").append(apnSetting.mmsPort).append(", ").append(apnSetting.port).append(", ").append(apnSetting.authType).append(", ");
        for (int i = 0; i < apnSetting.types.length; i++) {
            sb.append(apnSetting.types[i]);
            if (i < apnSetting.types.length - 1) {
                sb.append(" | ");
            }
        }
        sb.append(", ").append(apnSetting.protocol);
        sb.append(", ").append(apnSetting.roamingProtocol);
        sb.append(", ").append(apnSetting.carrierEnabled);
        sb.append(", ").append(apnSetting.bearerBitmask);
        logd("apnToStringIgnoreName: sb = " + sb.toString());
        return sb.toString();
    }

    private boolean isDataAllowedAsOff(String apnType) {
        boolean isDataAllowedAsOff = false;
        if (!(BSP_PACKAGE || this.mGsmDctExt == null)) {
            isDataAllowedAsOff = this.mGsmDctExt.isDataAllowedAsOff(apnType);
        }
        if (TextUtils.equals(apnType, "default") && isVsimActive(this.mPhone.getPhoneId())) {
            logd("Vsim is enabled, set isDataAllowedAsOff true");
            isDataAllowedAsOff = true;
        }
        if (!getIntlRoamingEnabledBySim()) {
            return isDataAllowedAsOff;
        }
        boolean bDataOnRoamingEnabled = getDataOnRoamingEnabled();
        boolean bIsIntlRoaming = isIntlRoaming();
        log("isDataAllowedAsOff: bDataOnRoamingEnabled=" + bDataOnRoamingEnabled + ", bIsIntlRoaming=" + bIsIntlRoaming);
        if (bIsIntlRoaming && bDataOnRoamingEnabled) {
            return true;
        }
        return isDataAllowedAsOff;
    }

    protected void notifyMobileDataChange(int enabled) {
        logd("notifyMobileDataChange, enable = " + enabled);
        Intent intent = new Intent(DataSubSelector.ACTION_MOBILE_DATA_ENABLE);
        intent.putExtra(DataSubSelector.EXTRA_MOBILE_DATA_ENABLE_REASON, enabled);
        this.mPhone.getContext().sendBroadcast(intent);
    }

    private void setUserDataProperty(boolean enabled) {
        int phoneId = this.mPhone.getPhoneId();
        String dataOnIccid = "0";
        if (SubscriptionManager.isValidPhoneId(phoneId)) {
            if (enabled) {
                dataOnIccid = SystemProperties.get(this.PROPERTY_ICCID[phoneId], "0");
            }
            logd("setUserDataProperty:" + dataOnIccid);
            TelephonyManager.getDefault();
            TelephonyManager.setTelephonyProperty(phoneId, PROPERTY_MOBILE_DATA_ENABLE, dataOnIccid);
            return;
        }
        log("invalid phone id, don't update");
    }

    private void handleSetResume() {
        if (SubscriptionManager.isValidPhoneId(this.mPhone.getPhoneId())) {
            this.mPhone.mCi.setResumeRegistration(this.mSuspendId, null);
        }
    }

    private void handleRegistrationSuspend(AsyncResult ar) {
        if (ar.exception != null || ar.result == null) {
            log("handleRegistrationSuspend: AsyncResult is wrong " + ar.exception);
            return;
        }
        log("handleRegistrationSuspend: createAllApnList and set initial attach APN");
        this.mSuspendId = ((int[]) ar.result)[0];
        log("handleRegistrationSuspend: suspending with Id=" + this.mSuspendId);
        synchronized (this.mNeedsResumeModemLock) {
            this.mNeedsResumeModem = true;
        }
        createAllApnList();
        setInitialAttachApn();
    }

    private void handlePlmnChange(AsyncResult ar) {
        if (ar.exception != null || ar.result == null) {
            log("AsyncResult is wrong " + ar.exception);
            return;
        }
        String[] plmnString = ar.result;
        for (int i = 0; i < plmnString.length; i++) {
            logd("plmnString[" + i + "]=" + plmnString[i]);
        }
        this.mRegion = getRegion(plmnString[0]);
        IccRecords r = (IccRecords) this.mIccRecords.get();
        if (TextUtils.isEmpty(r != null ? r.getOperatorNumeric() : UsimPBMemInfo.STRING_NOT_SET) || isNeedToResumeMd() || this.mPhone.getPhoneId() != SubscriptionManager.getPhoneId(SubscriptionController.getInstance().getDefaultDataSubId())) {
            logd("No need to update APN for Operator");
            return;
        }
        logd("handlePlmnChange: createAllApnList and set initial attach APN");
        createAllApnList();
        setInitialAttachApn();
    }

    private int getRegion(String plmn) {
        if (plmn == null || plmn.equals(UsimPBMemInfo.STRING_NOT_SET) || plmn.length() < 5) {
            logd("[getRegion] Invalid PLMN");
            return 0;
        }
        String currentMcc = plmn.substring(0, 3);
        for (String mcc : MCC_TABLE_TEST) {
            if (currentMcc.equals(mcc)) {
                logd("[getRegion] Test PLMN");
                return 0;
            }
        }
        String[] strArr = MCC_TABLE_DOMESTIC;
        if (strArr.length <= 0) {
            logd("[getRegion] REGION_UNKNOWN");
            return 0;
        } else if (currentMcc.equals(strArr[0])) {
            logd("[getRegion] REGION_DOMESTIC");
            return 1;
        } else {
            logd("[getRegion] REGION_FOREIGN");
            return 2;
        }
    }

    public boolean getImsEnabled() {
        boolean isImsEnabled;
        if (ImsManager.isVolteEnabledByPlatform(this.mPhone.getContext())) {
            isImsEnabled = ImsManager.isEnhanced4gLteModeSettingEnabledByUser(this.mPhone.getContext());
        } else {
            isImsEnabled = false;
        }
        logd("getImsEnabled: getInt isImsEnabled=" + isImsEnabled);
        return isImsEnabled;
    }

    private void syncApnTableToRds(ArrayList<ApnSetting> apnlist) {
        log("syncApnTableToRds: E");
        if (apnlist != null && apnlist.size() > 0) {
            ArrayList<String> aryApn = new ArrayList();
            for (int i = 0; i < apnlist.size(); i++) {
                ApnSetting apn = (ApnSetting) apnlist.get(i);
                if (TextUtils.isEmpty(apn.apn)) {
                    log("syncApnTableToRds: apn name is empty");
                } else {
                    StringBuilder sb = new StringBuilder();
                    sb.append(apn.apn);
                    sb.append(";");
                    int numOfProfileId = 0;
                    for (String apnProfileID : apn.types) {
                        int profileId = getApnProfileID(apnProfileID);
                        if (profileId != -1) {
                            if (numOfProfileId > 0) {
                                sb.append("|");
                            }
                            sb.append(profileId);
                            numOfProfileId++;
                        }
                    }
                    sb.append(";");
                    int rat = getApnRatByBearer(apn.bearerBitmask);
                    log("apn.rat: " + rat);
                    sb.append(rat);
                    sb.append(";");
                    sb.append(apn.protocol);
                    log("syncApnTableToRds: apn: " + sb.toString());
                    aryApn.add(sb.toString());
                }
            }
            if (aryApn.size() > 0) {
                this.mPhone.mCi.syncApnTableToRds((String[]) aryApn.toArray(new String[aryApn.size()]), null);
            }
        }
        log("syncApnTableToRds: X");
    }

    private int getApnRatByBearer(int bearerBitMask) {
        log("getApnRatByBearer: " + bearerBitMask);
        if (bearerBitMask == 0 || !ServiceState.bitmaskHasTech(bearerBitMask, 18)) {
            return 1;
        }
        if (isWifiOnlyApn(bearerBitMask)) {
            return 2;
        }
        return 3;
    }

    private boolean isWifiOnlyApn(int bearerBitMask) {
        boolean z = false;
        if (bearerBitMask == 0) {
            return false;
        }
        if ((16646143 & bearerBitMask) == 0) {
            z = true;
        }
        return z;
    }

    public boolean checkIfDomesticInitialAttachApn(String currentMcc) {
        boolean z = true;
        boolean isMccDomestic = false;
        for (String mcc : MCC_TABLE_DOMESTIC) {
            if (currentMcc.equals(mcc)) {
                isMccDomestic = true;
                break;
            }
        }
        if (isOp17IaSupport() && isMccDomestic) {
            if (!getImsEnabled()) {
                return false;
            }
            if (this.mRegion != 1) {
                z = false;
            }
            return z;
        } else if (enableOpIA()) {
            if (this.mRegion != 1) {
                z = false;
            }
            return z;
        } else {
            log("checkIfDomesticInitialAttachApn: Not OP129 or MCC is not in domestic for OP129");
            return true;
        }
    }

    public boolean enableOpIA() {
        IccRecords r = (IccRecords) this.mIccRecords.get();
        String operatorNumeric = r != null ? r.getOperatorNumeric() : UsimPBMemInfo.STRING_NOT_SET;
        if (TextUtils.isEmpty(operatorNumeric)) {
            return false;
        }
        String simOperator = operatorNumeric.substring(0, 3);
        log("enableOpIA: currentMcc = " + simOperator);
        for (String mcc : MCC_TABLE_DOMESTIC) {
            if (simOperator.equals(mcc)) {
                return true;
            }
        }
        return false;
    }

    private void onPcoStatus(AsyncResult ar) {
        int i = 0;
        if (ar.exception == null) {
            int[] aryPcoStatus = ar.result;
            if (aryPcoStatus == null || aryPcoStatus.length != 6) {
                logw("onPcoStatus: pco status is null");
                return;
            }
            log("onPcoStatus: PCO_MCC = " + aryPcoStatus[0] + ", PCO_MNC = " + aryPcoStatus[1] + ", PCO_VAL = " + aryPcoStatus[2] + ", PCO_TECH = " + aryPcoStatus[3] + ", PCO_PDN_ID = " + aryPcoStatus[5]);
            DcAsyncChannel dcac = (DcAsyncChannel) this.mDataConnectionAcHashMap.get(Integer.valueOf(aryPcoStatus[5]));
            if (dcac != null) {
                String[] aryApnType = dcac.getApnTypeSync();
                if (aryApnType != null) {
                    int length = aryApnType.length;
                    while (i < length) {
                        String apnType = aryApnType[i];
                        Intent intent = new Intent("com.mediatek.intent.action.ACTION_PCO_STATUS");
                        intent.putExtra("apnType", apnType);
                        intent.putExtra("pcoType", aryPcoStatus[2]);
                        this.mPhone.getContext().sendStickyBroadcastAsUser(intent, UserHandle.ALL);
                        i++;
                    }
                    return;
                }
                logw("onPcoStatus: dcac.getApnTypeSync() return null");
                return;
            }
            return;
        }
        loge("onPcoStatus exception: " + ar.exception);
    }

    private void onAllowChanged(boolean allow) {
        log("onAllowChanged: Allow = " + allow);
        this.mAllowConfig = allow;
        if (allow) {
            setupDataOnConnectableApns(PhoneInternalInterface.REASON_DATA_ALLOWED);
        }
    }

    private boolean getAllowConfig() {
        boolean z = true;
        DataConnectionHelper.getInstance();
        if (!DataConnectionHelper.isMultiPsAttachSupport()) {
            return true;
        }
        if (!hasModemDeactPdnCapabilityForMultiPS()) {
            z = this.mAllowConfig;
        }
        return z;
    }

    private boolean isPermanentFailByOp(DcFailCause dcFailCause) {
        if (this.mDcFcMgr != null) {
            return this.mDcFcMgr.isPermanentFailByOp(dcFailCause);
        }
        loge("mDcFcMgr should not be null, something wrong");
        return true;
    }

    public void syncDefaultDataSlotId(int slotId) {
        log("syncDefaultDataSlotId slot: " + slotId);
        int[] iArr = new int[3];
        iArr[0] = -2;
        iArr[1] = -2;
        iArr[2] = slotId;
        syncDataSettingsToMd(iArr);
    }

    private void syncDataSettingsToMd(int[] dataSettings) {
        logd("syncDataSettingsToMd(), " + dataSettings[0] + ", " + dataSettings[1] + ", " + dataSettings[2]);
        this.mPhone.mCi.syncDataSettingsToMd(dataSettings, null);
    }

    private boolean skipDataStallAlarm() {
        boolean isTestSim = false;
        int phoneId = this.mPhone.getPhoneId();
        DataConnectionHelper dcHelper = DataConnectionHelper.getInstance();
        if (SubscriptionManager.isValidPhoneId(phoneId) && dcHelper != null && dcHelper.isTestIccCard(phoneId)) {
            isTestSim = true;
        }
        if (isTestSim) {
            if (SystemProperties.get(SKIP_DATA_STALL_ALARM).equals("0")) {
                return false;
            }
            return true;
        } else if (SystemProperties.get(SKIP_DATA_STALL_ALARM).equals("1")) {
            return true;
        } else {
            return false;
        }
    }

    private void notifyVoiceCallEventToDataConnection(boolean bInVoiceCall, boolean bSupportConcurrent) {
        logd("notifyVoiceCallEventToDataConnection: bInVoiceCall = " + bInVoiceCall + ", bSupportConcurrent = " + bSupportConcurrent);
        for (DcAsyncChannel dcac : this.mDataConnectionAcHashMap.values()) {
            dcac.notifyVoiceCallEvent(bInVoiceCall, bSupportConcurrent);
        }
    }

    private boolean isApnSettingExist(ApnSetting apnSetting) {
        if (!(apnSetting == null || this.mAllApnSettings == null || this.mAllApnSettings.isEmpty())) {
            for (ApnSetting apn : this.mAllApnSettings) {
                if (TextUtils.equals(apnSetting.toStringIgnoreName(false), apn.toStringIgnoreName(false))) {
                    log("isApnSettingExist: " + apn);
                    return true;
                }
            }
        }
        return false;
    }

    private void onAttachApnChangedByHandover(boolean isImsHandover) {
        this.mIsImsHandover = isImsHandover;
        log("onAttachApnChangedByHandover: mIsImsHandover = " + this.mIsImsHandover);
        SystemProperties.set(this.PROP_IMS_HANDOVER, this.mIsImsHandover ? "1" : "2");
        setInitialAttachApn();
    }

    private boolean isOp17IaSupport() {
        return TelephonyManager.getTelephonyProperty(this.mPhone.getPhoneId(), "gsm.ril.sim.op17", "0").equals("1");
    }

    private boolean isOp129IaSupport() {
        return SystemProperties.get("gsm.ril.sim.op129").equals("1");
    }

    private boolean isNeedToResumeMd() {
        return SystemProperties.get("gsm.ril.data.op.suspendmd").equals("1");
    }

    private boolean isOp18Sim() {
        IccRecords r = (IccRecords) this.mIccRecords.get();
        String operator = r != null ? r.getOperatorNumeric() : UsimPBMemInfo.STRING_NOT_SET;
        if (operator != null) {
            for (String startsWith : this.MCCMNC_OP18) {
                if (operator.startsWith(startsWith)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean getDomesticRoamingEnabledBySim() {
        DataConnectionHelper dcHelper = DataConnectionHelper.getInstance();
        if (dcHelper == null) {
            loge("getDomesticRoamingEnabledBySim: dcHelper is null");
            return false;
        }
        boolean bDomesticRoamingEnabled = dcHelper.getDomesticRoamingEnabledBySim(this.mPhone.getPhoneId());
        log("getDomesticRoamingEnabledBySim: bDomesticRoamingEnabled=" + bDomesticRoamingEnabled);
        return bDomesticRoamingEnabled;
    }

    private boolean getIntlRoamingEnabledBySim() {
        DataConnectionHelper dcHelper = DataConnectionHelper.getInstance();
        if (dcHelper == null) {
            loge("getIntlRoamingEnabledBySim: dcHelper is null");
            return false;
        }
        boolean bIntlRoamingEnabled = dcHelper.getIntlRoamingEnabledBySim(this.mPhone.getPhoneId());
        log("getIntlRoamingEnabledBySim: bIntlRoamingEnabled>=" + bIntlRoamingEnabled);
        return bIntlRoamingEnabled;
    }

    private boolean hasOperatorIaCapability() {
        if (this.mTelDevController == null || this.mTelDevController.getModem(0) == null || !this.mTelDevController.getModem(0).hasOperatorIaCapability()) {
            return false;
        }
        log("hasOpIaCapability: true");
        return true;
    }

    private boolean hasModemDeactPdnCapabilityForMultiPS() {
        if (this.mTelDevController == null || this.mTelDevController.getModem(0) == null || !this.mTelDevController.getModem(0).hasModemDeactPdnCapabilityForMultiPS()) {
            return false;
        }
        log("hasModemDeactPdnCapabilityForMultiPS: true");
        return true;
    }

    private void onDataEnabledSettings(boolean enabled, int reason) {
        int i = 0;
        log("onDataEnabledSettings: enabled=" + enabled + ", reason=" + reason);
        if (reason == 2) {
            if (!getDataOnRoamingEnabled() && this.mPhone.getServiceState().getDataRoaming()) {
                if (enabled) {
                    notifyOffApnsOfAvailability(PhoneInternalInterface.REASON_ROAMING_ON);
                } else {
                    notifyOffApnsOfAvailability(PhoneInternalInterface.REASON_DATA_DISABLED);
                }
            }
            int[] iArr = new int[3];
            int i2 = (enabled || haveVsimIgnoreUserDataSetting()) ? true : 0;
            iArr[0] = i2;
            if (getDataOnRoamingEnabled() || SubscriptionManager.isVsimEnabled(this.mPhone.getSubId())) {
                i = 1;
            }
            iArr[1] = i;
            iArr[2] = -2;
            syncDataSettingsToMd(iArr);
            if (enabled) {
                teardownRestrictedMeteredConnections();
                onTrySetupData(PhoneInternalInterface.REASON_DATA_ENABLED);
            } else if (!haveVsimIgnoreUserDataSetting()) {
                if (BSP_PACKAGE) {
                    onCleanUpAllConnections(PhoneInternalInterface.REASON_DATA_SPECIFIC_DISABLED);
                    return;
                }
                for (ApnContext apnContext : this.mApnContexts.values()) {
                    if (!isDataAllowedAsOff(apnContext.getApnType())) {
                        apnContext.setReason(PhoneInternalInterface.REASON_DATA_SPECIFIC_DISABLED);
                        onCleanUpConnection(true, ApnContext.apnIdForApnName(apnContext.getApnType()), PhoneInternalInterface.REASON_DATA_SPECIFIC_DISABLED);
                    }
                }
            }
        }
    }

    /*  JADX ERROR: NullPointerException in pass: ModVisitor
        java.lang.NullPointerException
        	at jadx.core.dex.visitors.ModVisitor.getParentInsnSkipMove(ModVisitor.java:320)
        	at jadx.core.dex.visitors.ModVisitor.getArgsToFieldsMapping(ModVisitor.java:294)
        	at jadx.core.dex.visitors.ModVisitor.processAnonymousConstructor(ModVisitor.java:253)
        	at jadx.core.dex.visitors.ModVisitor.processInvoke(ModVisitor.java:235)
        	at jadx.core.dex.visitors.ModVisitor.replaceStep(ModVisitor.java:83)
        	at jadx.core.dex.visitors.ModVisitor.visit(ModVisitor.java:68)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:27)
        	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$1(DepthTraversal.java:14)
        	at java.util.ArrayList.forEach(ArrayList.java:1251)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
        	at jadx.core.ProcessClass.process(ProcessClass.java:32)
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    private void initEmergencyApnSettingAsync() {
        /*
        r2 = this;
        r0 = new com.android.internal.telephony.dataconnection.DcTracker$9;
        r1 = "initEmcApn";
        r0.<init>(r2, r1);
        r0.start();
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.dataconnection.DcTracker.initEmergencyApnSettingAsync():void");
    }

    public boolean haveVsimIgnoreUserDataSetting() {
        return mVsimIgnoreUserDataSetting ? SubscriptionManager.isVsimEnabled(this.mPhone.getSubId()) : false;
    }

    /* JADX WARNING: Missing block: B:4:0x000b, code:
            return 1;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static int getUiccFamilyByRat(int dataRat) {
        if (ServiceState.isGsm(dataRat) || dataRat == 13 || !ServiceState.isCdma(dataRat)) {
            return 1;
        }
        return 2;
    }

    public String getOperatorNumeric() {
        IccRecords r = (IccRecords) this.mIccRecords.get();
        String result = r != null ? r.getOperatorNumeric() : UsimPBMemInfo.STRING_NOT_SET;
        log("getOperatorNumberic - returning from card: " + result);
        return result;
    }

    protected boolean needManualSelectAPN(String operator) {
        Object obj = null;
        if (operator != null) {
            Cursor cursor = null;
            try {
                String selection = "numeric = '" + operator + "'";
                log("isOppoManualSelectAPN: selection=" + selection);
                cursor = this.mPhone.getContext().getContentResolver().query(Carriers.CONTENT_URI, null, selection, null, "_id");
                if (cursor != null && cursor.moveToFirst()) {
                    while (true) {
                        obj = cursor.getString(cursor.getColumnIndexOrThrow("oppo_manual_select"));
                        if (!"1".equals(obj)) {
                            if (!cursor.moveToNext()) {
                                break;
                            }
                        } else {
                            break;
                        }
                    }
                }
                if (cursor != null) {
                    cursor.close();
                }
            } catch (Exception e) {
                log("needManualSelectAPNException:" + e);
                if (cursor != null) {
                    cursor.close();
                }
            } catch (Throwable th) {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        return "1".equals(obj);
    }
}
