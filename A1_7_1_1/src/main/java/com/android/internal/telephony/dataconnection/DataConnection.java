package com.android.internal.telephony.dataconnection;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.INetworkManagementEventObserver;
import android.net.LinkAddress;
import android.net.LinkProperties;
import android.net.NetworkAgent;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.net.NetworkMisc;
import android.net.ProxyInfo;
import android.os.AsyncResult;
import android.os.INetworkManagementService;
import android.os.INetworkManagementService.Stub;
import android.os.Looper;
import android.os.Message;
import android.os.PersistableBundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.system.OsConstants;
import android.telephony.CarrierConfigManager;
import android.telephony.Rlog;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Pair;
import android.util.Patterns;
import android.util.TimeUtils;
import com.android.internal.telephony.CommandException;
import com.android.internal.telephony.CommandException.Error;
import com.android.internal.telephony.DctConstants;
import com.android.internal.telephony.OemConstant;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneInternalInterface;
import com.android.internal.telephony.ServiceStateTracker;
import com.android.internal.telephony.SubscriptionController;
import com.android.internal.telephony.TelephonyDevController;
import com.android.internal.telephony.cdma.sms.SmsEnvelope;
import com.android.internal.telephony.dataconnection.DataCallResponse.SetupResult;
import com.android.internal.util.AsyncChannel;
import com.android.internal.util.State;
import com.android.internal.util.StateMachine;
import com.android.server.net.BaseNetworkObserver;
import com.google.android.mms.pdu.CharacterSets;
import com.mediatek.common.MPlugin;
import com.mediatek.common.telephony.IGsmDCTExt;
import com.mediatek.internal.telephony.ImsSwitchController;
import com.mediatek.internal.telephony.dataconnection.DataConnectionHelper;
import com.mediatek.internal.telephony.dataconnection.DcFailCauseManager;
import com.mediatek.internal.telephony.dataconnection.DcFailCauseManager.Operator;
import com.mediatek.internal.telephony.uicc.UsimPBMemInfo;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

/*  JADX ERROR: NullPointerException in pass: ReSugarCode
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
    	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
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
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class DataConnection extends StateMachine {
    static final int BASE = 262144;
    private static final boolean BSP_PACKAGE = false;
    private static final int CMD_TO_STRING_COUNT = 22;
    private static final boolean DBG = true;
    static final int EVENT_ADDRESS_REMOVED = 262162;
    static final int EVENT_BW_REFRESH_RESPONSE = 262158;
    static final int EVENT_CONNECT = 262144;
    static final int EVENT_DATA_CONNECTION_DRS_OR_RAT_CHANGED = 262155;
    static final int EVENT_DATA_CONNECTION_ROAM_OFF = 262157;
    static final int EVENT_DATA_CONNECTION_ROAM_ON = 262156;
    static final int EVENT_DATA_STATE_CHANGED = 262151;
    static final int EVENT_DATA_STATE_CHANGED_FOR_LOADED = 262159;
    static final int EVENT_DEACTIVATE_DONE = 262147;
    static final int EVENT_DISCONNECT = 262148;
    static final int EVENT_DISCONNECT_ALL = 262150;
    static final int EVENT_FALLBACK_RETRY_CONNECTION = 262164;
    static final int EVENT_GET_LAST_FAIL_DONE = 262146;
    static final int EVENT_IPV4_ADDRESS_REMOVED = 262160;
    static final int EVENT_IPV6_ADDRESS_REMOVED = 262161;
    static final int EVENT_IPV6_ADDRESS_UPDATED = 262165;
    static final int EVENT_LOST_CONNECTION = 262153;
    static final int EVENT_RIL_CONNECTED = 262149;
    static final int EVENT_SETUP_DATA_CONNECTION_DONE = 262145;
    static final int EVENT_TEAR_DOWN_NOW = 262152;
    static final int EVENT_VOICE_CALL = 262163;
    private static final String INTENT_RETRY_ALARM_TAG = "tag";
    private static final String INTENT_RETRY_ALARM_WHAT = "what";
    private static final String NETWORK_TYPE = "MOBILE";
    private static final String NULL_IP = "0.0.0.0";
    private static final int RA_GET_IPV6_VALID_FAIL = -1000;
    private static final int RA_INITIAL_FAIL = -1;
    private static final int RA_LIFE_TIME_EXPIRED = 0;
    private static final int RA_REFRESH_FAIL = -2;
    private static final String TCP_BUFFER_SIZES_1XRTT = "4096,32768,131072,4096,8192,16384";
    private static final String TCP_BUFFER_SIZES_EDGE = "4093,26280,70800,4096,16384,70800";
    private static final String TCP_BUFFER_SIZES_EHRPD = "131072,262144,1048576,4096,16384,524288";
    private static final String TCP_BUFFER_SIZES_EVDO = "4094,87380,262144,4096,16384,262144";
    private static final String TCP_BUFFER_SIZES_GPRS = "4092,8760,48000,4096,8760,48000";
    private static final String TCP_BUFFER_SIZES_HSDPA = "61167,367002,1101005,8738,52429,262114";
    private static final String TCP_BUFFER_SIZES_HSPA = "40778,244668,734003,16777,100663,301990";
    private static final String TCP_BUFFER_SIZES_HSPAP = "122334,734003,2202010,32040,192239,576717";
    private static final String TCP_BUFFER_SIZES_LTE = "2097152,4194304,8388608,262144,524288,1048576";
    private static final String TCP_BUFFER_SIZES_UMTS = "58254,349525,1048576,58254,349525,1048576";
    private static final boolean VDBG = false;
    private static AtomicInteger mInstanceNumber;
    private static String[] sCmdToString;
    private AsyncChannel mAc;
    private String mActionRetry;
    private DcActivatingState mActivatingState;
    private DcActiveState mActiveState;
    private AlarmManager mAlarmManager;
    private INetworkManagementEventObserver mAlertObserver;
    public HashMap<ApnContext, ConnectionParams> mApnContexts;
    private ApnSetting mApnSetting;
    public int mCid;
    private ConnectionParams mConnectionParams;
    private long mCreateTime;
    private int mDataRegState;
    private DcController mDcController;
    private DcFailCause mDcFailCause;
    protected DcFailCauseManager mDcFcMgr;
    private DcTesterFailBringUpAll mDcTesterFailBringUpAll;
    private DcTracker mDct;
    private DcDefaultState mDefaultState;
    private DisconnectParams mDisconnectParams;
    private DcDisconnectionErrorCreatingConnection mDisconnectingErrorCreatingConnection;
    private DcDisconnectingState mDisconnectingState;
    private AddressInfo mGlobalV6AddrInfo;
    private IGsmDCTExt mGsmDCTExt;
    private int mId;
    private DcInactiveState mInactiveState;
    private BroadcastReceiver mIntentReceiver;
    private String mInterfaceName;
    private boolean mIsInVoiceCall;
    private boolean mIsSupportConcurrent;
    private DcFailCause mLastFailCause;
    private long mLastFailTime;
    private LinkProperties mLinkProperties;
    private NetworkAgent mNetworkAgent;
    private NetworkInfo mNetworkInfo;
    private final INetworkManagementService mNetworkManager;
    protected String[] mPcscfAddr;
    private Phone mPhone;
    int mRat;
    PendingIntent mReconnectIntent;
    private boolean mRestrictedNetworkOverride;
    private int mRetryCount;
    private int mRilRat;
    private SubscriptionController mSubController;
    int mTag;
    private TelephonyDevController mTelDevController;
    private Object mUserData;
    private long mValid;

    /* renamed from: com.android.internal.telephony.dataconnection.DataConnection$1 */
    class AnonymousClass1 extends BroadcastReceiver {
        final /* synthetic */ DataConnection this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.dataconnection.DataConnection.1.<init>(com.android.internal.telephony.dataconnection.DataConnection):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        AnonymousClass1(com.android.internal.telephony.dataconnection.DataConnection r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.dataconnection.DataConnection.1.<init>(com.android.internal.telephony.dataconnection.DataConnection):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.dataconnection.DataConnection.1.<init>(com.android.internal.telephony.dataconnection.DataConnection):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.dataconnection.DataConnection.1.onReceive(android.content.Context, android.content.Intent):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        public void onReceive(android.content.Context r1, android.content.Intent r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.dataconnection.DataConnection.1.onReceive(android.content.Context, android.content.Intent):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.dataconnection.DataConnection.1.onReceive(android.content.Context, android.content.Intent):void");
        }
    }

    /* renamed from: com.android.internal.telephony.dataconnection.DataConnection$2 */
    class AnonymousClass2 extends BaseNetworkObserver {
        final /* synthetic */ DataConnection this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.dataconnection.DataConnection.2.<init>(com.android.internal.telephony.dataconnection.DataConnection):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        AnonymousClass2(com.android.internal.telephony.dataconnection.DataConnection r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.dataconnection.DataConnection.2.<init>(com.android.internal.telephony.dataconnection.DataConnection):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.dataconnection.DataConnection.2.<init>(com.android.internal.telephony.dataconnection.DataConnection):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.internal.telephony.dataconnection.DataConnection.2.addressRemoved(java.lang.String, android.net.LinkAddress):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        public void addressRemoved(java.lang.String r1, android.net.LinkAddress r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.internal.telephony.dataconnection.DataConnection.2.addressRemoved(java.lang.String, android.net.LinkAddress):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.dataconnection.DataConnection.2.addressRemoved(java.lang.String, android.net.LinkAddress):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.internal.telephony.dataconnection.DataConnection.2.addressUpdated(java.lang.String, android.net.LinkAddress):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        public void addressUpdated(java.lang.String r1, android.net.LinkAddress r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.internal.telephony.dataconnection.DataConnection.2.addressUpdated(java.lang.String, android.net.LinkAddress):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.dataconnection.DataConnection.2.addressUpdated(java.lang.String, android.net.LinkAddress):void");
        }
    }

    /* renamed from: com.android.internal.telephony.dataconnection.DataConnection$3 */
    class AnonymousClass3 extends PrintWriter {
        final /* synthetic */ DataConnection this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.dataconnection.DataConnection.3.<init>(com.android.internal.telephony.dataconnection.DataConnection, java.io.Writer):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        AnonymousClass3(com.android.internal.telephony.dataconnection.DataConnection r1, java.io.Writer r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.dataconnection.DataConnection.3.<init>(com.android.internal.telephony.dataconnection.DataConnection, java.io.Writer):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.dataconnection.DataConnection.3.<init>(com.android.internal.telephony.dataconnection.DataConnection, java.io.Writer):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.internal.telephony.dataconnection.DataConnection.3.flush():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        public void flush() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.internal.telephony.dataconnection.DataConnection.3.flush():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.dataconnection.DataConnection.3.flush():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.internal.telephony.dataconnection.DataConnection.3.println(java.lang.String):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        public void println(java.lang.String r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.internal.telephony.dataconnection.DataConnection.3.println(java.lang.String):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.dataconnection.DataConnection.3.println(java.lang.String):void");
        }
    }

    private class AddressInfo {
        String mIntfName;
        LinkAddress mLinkAddr;
        final /* synthetic */ DataConnection this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.dataconnection.DataConnection.AddressInfo.<init>(com.android.internal.telephony.dataconnection.DataConnection, java.lang.String, android.net.LinkAddress):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        public AddressInfo(com.android.internal.telephony.dataconnection.DataConnection r1, java.lang.String r2, android.net.LinkAddress r3) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.dataconnection.DataConnection.AddressInfo.<init>(com.android.internal.telephony.dataconnection.DataConnection, java.lang.String, android.net.LinkAddress):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.dataconnection.DataConnection.AddressInfo.<init>(com.android.internal.telephony.dataconnection.DataConnection, java.lang.String, android.net.LinkAddress):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.dataconnection.DataConnection.AddressInfo.toString():java.lang.String, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        public java.lang.String toString() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.dataconnection.DataConnection.AddressInfo.toString():java.lang.String, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.dataconnection.DataConnection.AddressInfo.toString():java.lang.String");
        }
    }

    public static class ConnectionParams {
        ApnContext mApnContext;
        final int mConnectionGeneration;
        Message mOnCompletedMsg;
        int mProfileId;
        int mRilRat;
        int mTag;

        ConnectionParams(ApnContext apnContext, int profileId, int rilRadioTechnology, Message onCompletedMsg, int connectionGeneration) {
            this.mApnContext = apnContext;
            this.mProfileId = profileId;
            this.mRilRat = rilRadioTechnology;
            this.mOnCompletedMsg = onCompletedMsg;
            this.mConnectionGeneration = connectionGeneration;
        }

        public String toString() {
            return "{mTag=" + this.mTag + " mApnContext=" + this.mApnContext + " mProfileId=" + this.mProfileId + " mRat=" + this.mRilRat + " mOnCompletedMsg=" + DataConnection.msgToString(this.mOnCompletedMsg) + "}";
        }
    }

    private class DcActivatingState extends State {
        /* renamed from: -com-android-internal-telephony-dataconnection-DataCallResponse$SetupResultSwitchesValues */
        private static final /* synthetic */ int[] f38x5f2cdfda = null;
        final /* synthetic */ int[] $SWITCH_TABLE$com$android$internal$telephony$dataconnection$DataCallResponse$SetupResult;
        final /* synthetic */ DataConnection this$0;

        /* renamed from: -getcom-android-internal-telephony-dataconnection-DataCallResponse$SetupResultSwitchesValues */
        private static /* synthetic */ int[] m160xa73f2fb6() {
            if (f38x5f2cdfda != null) {
                return f38x5f2cdfda;
            }
            int[] iArr = new int[SetupResult.values().length];
            try {
                iArr[SetupResult.ERR_BadCommand.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                iArr[SetupResult.ERR_GetLastErrorFromRil.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                iArr[SetupResult.ERR_RilError.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                iArr[SetupResult.ERR_Stale.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                iArr[SetupResult.ERR_UnacceptableParameter.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
            try {
                iArr[SetupResult.SUCCESS.ordinal()] = 6;
            } catch (NoSuchFieldError e6) {
            }
            f38x5f2cdfda = iArr;
            return iArr;
        }

        /* synthetic */ DcActivatingState(DataConnection this$0, DcActivatingState dcActivatingState) {
            this(this$0);
        }

        private DcActivatingState(DataConnection this$0) {
            this.this$0 = this$0;
        }

        public void enter() {
            this.this$0.log("DcActivatingState: enter dc=" + this.this$0);
        }

        public void exit() {
            this.this$0.log("DcActivatingState: exit dc=" + this);
        }

        public boolean processMessage(Message msg) {
            this.this$0.log("DcActivatingState: msg=" + DataConnection.msgToString(msg));
            ConnectionParams cp;
            AsyncResult ar;
            switch (msg.what) {
                case SmsEnvelope.TELESERVICE_MWI /*262144*/:
                    cp = msg.obj;
                    this.this$0.mApnContexts.put(cp.mApnContext, cp);
                    this.this$0.log("DcActivatingState: mApnContexts size=" + this.this$0.mApnContexts.size());
                    break;
                case DataConnection.EVENT_SETUP_DATA_CONNECTION_DONE /*262145*/:
                    ar = msg.obj;
                    cp = (ConnectionParams) ar.userObj;
                    SetupResult result = this.this$0.onSetupConnectionCompleted(ar);
                    if (!(result == SetupResult.ERR_Stale || this.this$0.mConnectionParams == cp)) {
                        this.this$0.loge("DcActivatingState: WEIRD mConnectionsParams:" + this.this$0.mConnectionParams + " != cp:" + cp);
                    }
                    this.this$0.log("DcActivatingState onSetupConnectionCompleted result=" + result + " dc=" + this.this$0);
                    if (cp.mApnContext != null) {
                        cp.mApnContext.requestLog("onSetupConnectionCompleted result=" + result);
                    }
                    switch (m160xa73f2fb6()[result.ordinal()]) {
                        case 1:
                            this.this$0.mInactiveState.setEnterNotificationParams(cp, result.mFailCause);
                            this.this$0.transitionTo(this.this$0.mInactiveState);
                            break;
                        case 2:
                            this.this$0.mPhone.mCi.getLastDataCallFailCause(this.this$0.obtainMessage(DataConnection.EVENT_GET_LAST_FAIL_DONE, cp));
                            break;
                        case 3:
                            long delay = this.this$0.getSuggestedRetryDelay(ar);
                            cp.mApnContext.setModemSuggestedDelay(delay);
                            String str = "DcActivatingState: ERR_RilError  delay=" + delay + " result=" + result + " result.isRestartRadioFail=" + result.mFailCause.isRestartRadioFail() + " result.isPermanentFail=" + this.this$0.mDct.isPermanentFail(result.mFailCause);
                            this.this$0.log(str);
                            if (cp.mApnContext != null) {
                                cp.mApnContext.requestLog(str);
                            }
                            if (result.mFailCause != DcFailCause.PDP_FAIL_FALLBACK_RETRY) {
                                this.this$0.mInactiveState.setEnterNotificationParams(cp, result.mFailCause);
                                this.this$0.transitionTo(this.this$0.mInactiveState);
                                break;
                            }
                            this.this$0.onSetupFallbackConnection(ar);
                            this.this$0.mDcFailCause = DcFailCause.PDP_FAIL_FALLBACK_RETRY;
                            if (this.this$0.mDcFcMgr == null || !this.this$0.mDcFcMgr.isSpecificNetworkAndSimOperator(Operator.OP19)) {
                                this.this$0.deferMessage(this.this$0.obtainMessage(DataConnection.EVENT_FALLBACK_RETRY_CONNECTION, this.this$0.mTag));
                            } else {
                                DataConnection dataConnection = this.this$0;
                                dataConnection.mRetryCount = dataConnection.mRetryCount + 1;
                                long retryTime = this.this$0.mDcFcMgr.getRetryTimeByIndex(this.this$0.mRetryCount, Operator.OP19);
                                if (retryTime < 0) {
                                    this.this$0.log("DcActiveState_FALLBACK_Retry: No retry but at least one IPv4 or IPv6 is accepted");
                                    this.this$0.mDcFailCause = DcFailCause.NONE;
                                    this.this$0.resetRetryCount();
                                } else {
                                    this.this$0.startRetryAlarm(DataConnection.EVENT_FALLBACK_RETRY_CONNECTION, this.this$0.mTag, retryTime);
                                }
                            }
                            this.this$0.transitionTo(this.this$0.mActiveState);
                            break;
                        case 4:
                            this.this$0.loge("DcActivatingState: stale EVENT_SETUP_DATA_CONNECTION_DONE tag:" + cp.mTag + " != mTag:" + this.this$0.mTag);
                            break;
                        case 5:
                            this.this$0.tearDownData(cp);
                            this.this$0.transitionTo(this.this$0.mDisconnectingErrorCreatingConnection);
                            break;
                        case 6:
                            this.this$0.mDcFailCause = DcFailCause.NONE;
                            this.this$0.resetRetryCount();
                            this.this$0.transitionTo(this.this$0.mActiveState);
                            break;
                        default:
                            this.this$0.loge("Unknown SetupResult, should not happen");
                            break;
                    }
                    return true;
                case DataConnection.EVENT_GET_LAST_FAIL_DONE /*262146*/:
                    ar = (AsyncResult) msg.obj;
                    cp = (ConnectionParams) ar.userObj;
                    if (cp.mTag == this.this$0.mTag) {
                        if (this.this$0.mConnectionParams != cp) {
                            this.this$0.loge("DcActivatingState: WEIRD mConnectionsParams:" + this.this$0.mConnectionParams + " != cp:" + cp);
                        }
                        DcFailCause cause = DcFailCause.UNKNOWN;
                        if (ar.exception == null) {
                            cause = DcFailCause.fromInt(((int[]) ar.result)[0]);
                            if (cause == DcFailCause.NONE) {
                                this.this$0.log("DcActivatingState msg.what=EVENT_GET_LAST_FAIL_DONE BAD: error was NONE, change to UNKNOWN");
                                cause = DcFailCause.UNKNOWN;
                            }
                        }
                        this.this$0.mDcFailCause = cause;
                        this.this$0.log("DcActivatingState msg.what=EVENT_GET_LAST_FAIL_DONE cause=" + cause + " dc=" + this.this$0);
                        this.this$0.mInactiveState.setEnterNotificationParams(cp, cause);
                        this.this$0.transitionTo(this.this$0.mInactiveState);
                    } else {
                        this.this$0.loge("DcActivatingState: stale EVENT_GET_LAST_FAIL_DONE tag:" + cp.mTag + " != mTag:" + this.this$0.mTag);
                    }
                    return true;
                case DataConnection.EVENT_DISCONNECT /*262148*/:
                    DisconnectParams dp = msg.obj;
                    if (this.this$0.mApnContexts.containsKey(dp.mApnContext)) {
                        this.this$0.deferMessage(msg);
                    } else {
                        this.this$0.log("DcActivatingState ERROR no such apnContext=" + dp.mApnContext + " in this dc=" + this.this$0);
                        this.this$0.notifyDisconnectCompleted(dp, false);
                    }
                    return true;
                case DataConnection.EVENT_DATA_CONNECTION_DRS_OR_RAT_CHANGED /*262155*/:
                    break;
                case DataConnection.EVENT_IPV4_ADDRESS_REMOVED /*262160*/:
                case DataConnection.EVENT_IPV6_ADDRESS_REMOVED /*262161*/:
                case DataConnection.EVENT_IPV6_ADDRESS_UPDATED /*262165*/:
                    this.this$0.log("DcActivatingState deferMsg: " + this.this$0.getWhatToString(msg.what) + ", address info: " + ((AddressInfo) msg.obj));
                    this.this$0.deferMessage(msg);
                    return true;
                default:
                    if (DataConnection.VDBG) {
                        this.this$0.log("DcActivatingState not handled msg.what=" + this.this$0.getWhatToString(msg.what) + " RefCount=" + this.this$0.mApnContexts.size());
                    }
                    return false;
            }
            this.this$0.deferMessage(msg);
            return true;
        }
    }

    private class DcActiveState extends State {
        /* renamed from: -com-android-internal-telephony-dataconnection-DataCallResponse$SetupResultSwitchesValues */
        private static final /* synthetic */ int[] f39x5f2cdfda = null;
        final /* synthetic */ int[] $SWITCH_TABLE$com$android$internal$telephony$dataconnection$DataCallResponse$SetupResult;
        final /* synthetic */ DataConnection this$0;

        /* renamed from: -getcom-android-internal-telephony-dataconnection-DataCallResponse$SetupResultSwitchesValues */
        private static /* synthetic */ int[] m161xa73f2fb6() {
            if (f39x5f2cdfda != null) {
                return f39x5f2cdfda;
            }
            int[] iArr = new int[SetupResult.values().length];
            try {
                iArr[SetupResult.ERR_BadCommand.ordinal()] = 4;
            } catch (NoSuchFieldError e) {
            }
            try {
                iArr[SetupResult.ERR_GetLastErrorFromRil.ordinal()] = 5;
            } catch (NoSuchFieldError e2) {
            }
            try {
                iArr[SetupResult.ERR_RilError.ordinal()] = 1;
            } catch (NoSuchFieldError e3) {
            }
            try {
                iArr[SetupResult.ERR_Stale.ordinal()] = 2;
            } catch (NoSuchFieldError e4) {
            }
            try {
                iArr[SetupResult.ERR_UnacceptableParameter.ordinal()] = 6;
            } catch (NoSuchFieldError e5) {
            }
            try {
                iArr[SetupResult.SUCCESS.ordinal()] = 3;
            } catch (NoSuchFieldError e6) {
            }
            f39x5f2cdfda = iArr;
            return iArr;
        }

        /* synthetic */ DcActiveState(DataConnection this$0, DcActiveState dcActiveState) {
            this(this$0);
        }

        private DcActiveState(DataConnection this$0) {
            this.this$0 = this$0;
        }

        public void enter() {
            ServiceState ss = this.this$0.mPhone.getServiceState();
            int networkType = ss.getDataNetworkType();
            if (this.this$0.mNetworkInfo.getSubtype() != networkType) {
                this.this$0.log("DcActiveState with incorrect subtype (" + this.this$0.mNetworkInfo.getSubtype() + ", " + networkType + "), updating.");
            }
            this.this$0.mNetworkInfo.setSubtype(networkType, TelephonyManager.getNetworkTypeName(networkType));
            boolean roaming = ss.getDataRoaming();
            if (roaming != this.this$0.mNetworkInfo.isRoaming()) {
                this.this$0.log("DcActiveState with incorrect roaming (" + this.this$0.mNetworkInfo.isRoaming() + ", " + roaming + "), updating.");
            }
            this.this$0.mNetworkInfo.setRoaming(roaming);
            boolean createNetworkAgent = true;
            if (((this.this$0.hasMessages(DataConnection.EVENT_DISCONNECT) || this.this$0.hasDeferredMessages(DataConnection.EVENT_DISCONNECT)) && this.this$0.mApnContexts.size() == 1) || this.this$0.hasMessages(DataConnection.EVENT_DISCONNECT_ALL) || this.this$0.hasDeferredMessages(DataConnection.EVENT_DISCONNECT_ALL)) {
                this.this$0.log("DcActiveState: skipping notifyAllOfConnected()");
                createNetworkAgent = false;
            } else {
                this.this$0.notifyAllOfConnected("connected");
            }
            this.this$0.mDcController.addActiveDcByCid(this.this$0);
            if (this.this$0.isNwNeedSuspended()) {
                this.this$0.mNetworkInfo.setDetailedState(DetailedState.SUSPENDED, this.this$0.mNetworkInfo.getReason(), null);
            } else {
                this.this$0.mNetworkInfo.setDetailedState(DetailedState.CONNECTED, this.this$0.mNetworkInfo.getReason(), null);
            }
            this.this$0.mNetworkInfo.setExtraInfo(this.this$0.mApnSetting.apn);
            this.this$0.updateTcpBufferSizes(this.this$0.mRilRat);
            NetworkMisc misc = new NetworkMisc();
            if (this.this$0.mPhone.getCarrierSignalAgent().hasRegisteredCarrierSignalReceivers()) {
                misc.provisioningNotificationDisabled = true;
            }
            misc.subscriberId = this.this$0.mPhone.getSubscriberId();
            this.this$0.logi("DcActiveState: enter dc = " + this.this$0 + " createNetworkAgent = " + createNetworkAgent + " mNetworkInfo = " + this.this$0.mNetworkInfo);
            if (createNetworkAgent) {
                this.this$0.setNetworkRestriction();
                if (OemConstant.getWlanAssistantEnable(this.this$0.mPhone.getContext())) {
                    this.this$0.mNetworkAgent = new DcNetworkAgent(this.this$0, this.this$0.getHandler().getLooper(), this.this$0.mPhone.getContext(), "DcNetworkAgent", this.this$0.mNetworkInfo, this.this$0.makeNetworkCapabilities(), this.this$0.mLinkProperties, 10, misc);
                } else {
                    this.this$0.mNetworkAgent = new DcNetworkAgent(this.this$0, this.this$0.getHandler().getLooper(), this.this$0.mPhone.getContext(), "DcNetworkAgent", this.this$0.mNetworkInfo, this.this$0.makeNetworkCapabilities(), this.this$0.mLinkProperties, 50, misc);
                }
            }
            if (!DataConnection.BSP_PACKAGE) {
                try {
                    this.this$0.mGsmDCTExt.onDcActivated(this.this$0.mApnSetting == null ? null : this.this$0.mApnSetting.types, this.this$0.mLinkProperties == null ? UsimPBMemInfo.STRING_NOT_SET : this.this$0.mLinkProperties.getInterfaceName());
                } catch (Exception e) {
                    this.this$0.loge("onDcActivated fail!");
                    e.printStackTrace();
                }
            }
        }

        public void exit() {
            this.this$0.log("DcActiveState: exit dc=" + this);
            String reason = this.this$0.mNetworkInfo.getReason();
            if (this.this$0.mDcController.isExecutingCarrierChange()) {
                reason = PhoneInternalInterface.REASON_CARRIER_CHANGE;
            } else if (this.this$0.mDisconnectParams != null && this.this$0.mDisconnectParams.mReason != null) {
                reason = this.this$0.mDisconnectParams.mReason;
            } else if (this.this$0.mDcFailCause != null) {
                reason = this.this$0.mDcFailCause.toString();
            }
            if (!DataConnection.BSP_PACKAGE) {
                try {
                    this.this$0.mGsmDCTExt.onDcDeactivated(this.this$0.mApnSetting == null ? null : this.this$0.mApnSetting.types, this.this$0.mLinkProperties == null ? UsimPBMemInfo.STRING_NOT_SET : this.this$0.mLinkProperties.getInterfaceName());
                } catch (Exception e) {
                    this.this$0.loge("onDcDeactivated fail!");
                    e.printStackTrace();
                }
            }
            this.this$0.mNetworkInfo.setDetailedState(DetailedState.DISCONNECTED, reason, this.this$0.mNetworkInfo.getExtraInfo());
            if (this.this$0.mNetworkAgent != null) {
                this.this$0.mNetworkAgent.sendNetworkInfo(this.this$0.mNetworkInfo);
                this.this$0.mNetworkAgent = null;
            }
        }

        public boolean processMessage(Message msg) {
            ConnectionParams cp;
            NetworkCapabilities cap;
            AsyncResult ar;
            DisconnectParams dp;
            AddressInfo addrV6Info;
            switch (msg.what) {
                case SmsEnvelope.TELESERVICE_MWI /*262144*/:
                    cp = msg.obj;
                    this.this$0.mApnContexts.put(cp.mApnContext, cp);
                    this.this$0.log("DcActiveState: EVENT_CONNECT cp=" + cp + " dc=" + this.this$0);
                    this.this$0.checkIfDefaultApnReferenceCountChanged();
                    if (this.this$0.mNetworkAgent != null) {
                        cap = this.this$0.makeNetworkCapabilities();
                        this.this$0.mNetworkAgent.sendNetworkCapabilities(cap);
                        this.this$0.log("DcActiveState update Capabilities:" + cap);
                    }
                    this.this$0.notifyConnectCompleted(cp, DcFailCause.NONE, false);
                    return true;
                case DataConnection.EVENT_SETUP_DATA_CONNECTION_DONE /*262145*/:
                    ar = (AsyncResult) msg.obj;
                    cp = (ConnectionParams) ar.userObj;
                    SetupResult result = this.this$0.onSetupConnectionCompleted(ar);
                    if (!(result == SetupResult.ERR_Stale || this.this$0.mConnectionParams == cp)) {
                        this.this$0.loge("DcActiveState_FALLBACK_Retry: WEIRD mConnectionsParams:" + this.this$0.mConnectionParams + " != cp:" + cp);
                    }
                    this.this$0.log("DcActiveState_FALLBACK_Retry onSetupConnectionCompleted result=" + result + " dc=" + this.this$0);
                    switch (m161xa73f2fb6()[result.ordinal()]) {
                        case 1:
                            this.this$0.log("DcActiveState_FALLBACK_Retry: ERR_RilError  result=" + result + " result.isRestartRadioFail=" + result.mFailCause.isRestartRadioFail() + " result.isPermanentFail=" + this.this$0.mDct.isPermanentFail(result.mFailCause));
                            if (result.mFailCause == DcFailCause.PDP_FAIL_FALLBACK_RETRY) {
                                if (this.this$0.mDcFcMgr != null && this.this$0.mDcFcMgr.isSpecificNetworkAndSimOperator(Operator.OP19)) {
                                    DataConnection dataConnection = this.this$0;
                                    dataConnection.mRetryCount = dataConnection.mRetryCount + 1;
                                    long retryTime = this.this$0.mDcFcMgr.getRetryTimeByIndex(this.this$0.mRetryCount, Operator.OP19);
                                    if (retryTime >= 0) {
                                        this.this$0.mDcFailCause = DcFailCause.PDP_FAIL_FALLBACK_RETRY;
                                        this.this$0.startRetryAlarm(DataConnection.EVENT_FALLBACK_RETRY_CONNECTION, this.this$0.mTag, retryTime);
                                        break;
                                    }
                                    this.this$0.log("DcActiveState_FALLBACK_Retry: No retry but at least one IPv4 or IPv6 is accepted");
                                    this.this$0.mDcFailCause = DcFailCause.NONE;
                                    break;
                                }
                            }
                            this.this$0.log("DcActiveState_FALLBACK_Retry: ERR_RilError Not retry anymore");
                            break;
                        case 2:
                            this.this$0.loge("DcActiveState_FALLBACK_Retry: stale EVENT_SETUP_DATA_CONNECTION_DONE tag:" + cp.mTag + " != mTag:" + this.this$0.mTag + " Not retry anymore");
                            break;
                        case 3:
                            this.this$0.mDcFailCause = DcFailCause.NONE;
                            this.this$0.resetRetryCount();
                            break;
                        default:
                            this.this$0.log("DcActiveState_FALLBACK_Retry: Another error cause, Not retry anymore");
                            break;
                    }
                    return true;
                case DataConnection.EVENT_DISCONNECT /*262148*/:
                    dp = msg.obj;
                    this.this$0.log("DcActiveState: EVENT_DISCONNECT dp=" + dp + " dc=" + this.this$0);
                    if (this.this$0.mApnContexts.containsKey(dp.mApnContext)) {
                        this.this$0.log("DcActiveState msg.what=EVENT_DISCONNECT RefCount=" + this.this$0.mApnContexts.size());
                        if (this.this$0.mApnContexts.size() == 1) {
                            this.this$0.handlePcscfErrorCause(dp);
                            this.this$0.mApnContexts.clear();
                            this.this$0.mDisconnectParams = dp;
                            this.this$0.mConnectionParams = null;
                            dp.mTag = this.this$0.mTag;
                            this.this$0.tearDownData(dp);
                            this.this$0.transitionTo(this.this$0.mDisconnectingState);
                        } else {
                            this.this$0.mApnContexts.remove(dp.mApnContext);
                            if (this.this$0.mNetworkAgent != null) {
                                cap = this.this$0.makeNetworkCapabilities();
                                this.this$0.mNetworkAgent.sendNetworkCapabilities(cap);
                                this.this$0.log("DcActiveState update Capabilities:" + cap);
                            }
                            this.this$0.checkIfDefaultApnReferenceCountChanged();
                            this.this$0.notifyDisconnectCompleted(dp, false);
                        }
                    } else {
                        this.this$0.log("DcActiveState ERROR no such apnContext=" + dp.mApnContext + " in this dc=" + this.this$0);
                        this.this$0.notifyDisconnectCompleted(dp, false);
                    }
                    return true;
                case DataConnection.EVENT_DISCONNECT_ALL /*262150*/:
                    this.this$0.log("DcActiveState EVENT_DISCONNECT_ALL clearing apn contexts, dc=" + this.this$0);
                    dp = (DisconnectParams) msg.obj;
                    this.this$0.mDisconnectParams = dp;
                    this.this$0.mConnectionParams = null;
                    dp.mTag = this.this$0.mTag;
                    this.this$0.tearDownData(dp);
                    this.this$0.transitionTo(this.this$0.mDisconnectingState);
                    return true;
                case DataConnection.EVENT_LOST_CONNECTION /*262153*/:
                    this.this$0.log("DcActiveState EVENT_LOST_CONNECTION dc=" + this.this$0);
                    this.this$0.mInactiveState.setEnterNotificationParams(DcFailCause.LOST_CONNECTION);
                    this.this$0.transitionTo(this.this$0.mInactiveState);
                    return true;
                case DataConnection.EVENT_DATA_CONNECTION_ROAM_ON /*262156*/:
                    this.this$0.mNetworkInfo.setRoaming(true);
                    if (this.this$0.mNetworkAgent != null) {
                        this.this$0.mNetworkAgent.sendNetworkInfo(this.this$0.mNetworkInfo);
                    }
                    return true;
                case DataConnection.EVENT_DATA_CONNECTION_ROAM_OFF /*262157*/:
                    this.this$0.mNetworkInfo.setRoaming(false);
                    if (this.this$0.mNetworkAgent != null) {
                        this.this$0.mNetworkAgent.sendNetworkInfo(this.this$0.mNetworkInfo);
                    }
                    return true;
                case DataConnection.EVENT_BW_REFRESH_RESPONSE /*262158*/:
                    ar = msg.obj;
                    if (ar.exception != null) {
                        this.this$0.log("EVENT_BW_REFRESH_RESPONSE: error ignoring, e=" + ar.exception);
                    } else {
                        int lceBwDownKbps = ((Integer) ar.result.get(0)).intValue();
                        NetworkCapabilities nc = this.this$0.makeNetworkCapabilities();
                        if (this.this$0.mPhone.getLceStatus() == 1 && lceBwDownKbps > 0) {
                            nc.setLinkDownstreamBandwidthKbps(lceBwDownKbps);
                            if (this.this$0.mNetworkAgent != null) {
                                this.this$0.mNetworkAgent.sendNetworkCapabilities(nc);
                            }
                        }
                    }
                    return true;
                case DataConnection.EVENT_IPV4_ADDRESS_REMOVED /*262160*/:
                    this.this$0.log("DcActiveState: " + this.this$0.getWhatToString(msg.what) + ": " + msg.obj);
                    return true;
                case DataConnection.EVENT_IPV6_ADDRESS_REMOVED /*262161*/:
                    addrV6Info = msg.obj;
                    this.this$0.log("DcActiveState: " + this.this$0.getWhatToString(msg.what) + ": " + addrV6Info);
                    if (!(this.this$0.mInterfaceName == null || !this.this$0.mInterfaceName.equals(addrV6Info.mIntfName) || DataConnection.BSP_PACKAGE)) {
                        try {
                            this.this$0.mValid = this.this$0.mGsmDCTExt.getIPv6Valid(addrV6Info.mLinkAddr);
                        } catch (Exception e) {
                            this.this$0.loge("DcActiveState: getIPv6Valid fail!");
                            this.this$0.mValid = -1000;
                            e.printStackTrace();
                        }
                        if (this.this$0.mValid == 0 || this.this$0.mValid == -1 || this.this$0.mValid == -2) {
                            this.this$0.log("DcActiveState: RA is failed or life time expired, valid:" + this.this$0.mValid);
                            this.this$0.onAddressRemoved();
                        }
                    }
                    if (this.this$0.mGlobalV6AddrInfo != null && this.this$0.mGlobalV6AddrInfo.mIntfName.equals(addrV6Info.mIntfName)) {
                        this.this$0.mGlobalV6AddrInfo = null;
                    }
                    return true;
                case DataConnection.EVENT_VOICE_CALL /*262163*/:
                    this.this$0.mIsInVoiceCall = msg.arg1 != 0;
                    this.this$0.mIsSupportConcurrent = msg.arg2 != 0;
                    if (this.this$0.updateNetworkInfoSuspendState() && this.this$0.mNetworkAgent != null) {
                        this.this$0.mNetworkAgent.sendNetworkInfo(this.this$0.mNetworkInfo);
                    }
                    return true;
                case DataConnection.EVENT_FALLBACK_RETRY_CONNECTION /*262164*/:
                    if (msg.arg1 != this.this$0.mTag) {
                        this.this$0.log("DcActiveState stale EVENT_FALLBACK_RETRY_CONNECTION tag:" + msg.arg1 + " != mTag:" + this.this$0.mTag);
                    } else if (this.this$0.mDataRegState != 0) {
                        this.this$0.log("DcActiveState: EVENT_FALLBACK_RETRY_CONNECTION not in service");
                    } else {
                        this.this$0.log("DcActiveState EVENT_FALLBACK_RETRY_CONNECTION mConnectionParams=" + this.this$0.mConnectionParams);
                        this.this$0.onConnect(this.this$0.mConnectionParams);
                    }
                    return true;
                case DataConnection.EVENT_IPV6_ADDRESS_UPDATED /*262165*/:
                    addrV6Info = (AddressInfo) msg.obj;
                    if (this.this$0.mInterfaceName != null && this.this$0.mInterfaceName.equals(addrV6Info.mIntfName)) {
                        int scope = addrV6Info.mLinkAddr.getScope();
                        int flag = addrV6Info.mLinkAddr.getFlags();
                        this.this$0.log("EVENT_IPV6_ADDRESS_UPDATED, scope: " + scope + ", flag: " + flag);
                        if (OsConstants.RT_SCOPE_UNIVERSE != scope || (flag & 1) == OsConstants.IFA_F_TEMPORARY || this.this$0.mNetworkAgent == null) {
                            this.this$0.log("EVENT_IPV6_ADDRESS_UPDATED, not notify global ipv6 address update");
                        } else {
                            this.this$0.mGlobalV6AddrInfo = addrV6Info;
                            this.this$0.mNetworkAgent.sendLinkProperties(this.this$0.getLinkProperties());
                            this.this$0.log("EVENT_IPV6_ADDRESS_UPDATED, notify global ipv6 address update");
                        }
                    }
                    return true;
                default:
                    if (DataConnection.VDBG) {
                        this.this$0.log("DcActiveState not handled msg.what=" + this.this$0.getWhatToString(msg.what));
                    }
                    return false;
            }
        }
    }

    private class DcDefaultState extends State {
        final /* synthetic */ DataConnection this$0;

        /* synthetic */ DcDefaultState(DataConnection this$0, DcDefaultState dcDefaultState) {
            this(this$0);
        }

        private DcDefaultState(DataConnection this$0) {
            this.this$0 = this$0;
        }

        public void enter() {
            this.this$0.log("DcDefaultState: enter");
            this.this$0.mPhone.getServiceStateTracker().registerForDataRegStateOrRatChanged(this.this$0.getHandler(), DataConnection.EVENT_DATA_CONNECTION_DRS_OR_RAT_CHANGED, null);
            this.this$0.mPhone.getServiceStateTracker().registerForDataRoamingOn(this.this$0.getHandler(), DataConnection.EVENT_DATA_CONNECTION_ROAM_ON, null);
            this.this$0.mPhone.getServiceStateTracker().registerForDataRoamingOff(this.this$0.getHandler(), DataConnection.EVENT_DATA_CONNECTION_ROAM_OFF, null);
            if (!(this.this$0.mTelDevController == null || this.this$0.mTelDevController.getModem(0) == null || this.this$0.mTelDevController.getModem(0).hasRaCapability())) {
                this.this$0.registerNetworkAlertObserver();
            }
            this.this$0.mDcController.addDc(this.this$0);
            IntentFilter filter = new IntentFilter();
            filter.addAction(this.this$0.mActionRetry);
            this.this$0.log("DcDefaultState: register for intent action=" + this.this$0.mActionRetry);
            this.this$0.mPhone.getContext().registerReceiver(this.this$0.mIntentReceiver, filter, null, this.this$0.getHandler());
        }

        public void exit() {
            this.this$0.log("DcDefaultState: exit");
            this.this$0.mPhone.getServiceStateTracker().unregisterForDataRegStateOrRatChanged(this.this$0.getHandler());
            this.this$0.mPhone.getServiceStateTracker().unregisterForDataRoamingOn(this.this$0.getHandler());
            this.this$0.mPhone.getServiceStateTracker().unregisterForDataRoamingOff(this.this$0.getHandler());
            this.this$0.mDcController.removeDc(this.this$0);
            if (this.this$0.mAc != null) {
                this.this$0.mAc.disconnected();
                this.this$0.mAc = null;
            }
            this.this$0.mApnContexts = null;
            this.this$0.mReconnectIntent = null;
            this.this$0.mDct = null;
            this.this$0.mApnSetting = null;
            this.this$0.mPhone = null;
            this.this$0.mLinkProperties = null;
            this.this$0.mLastFailCause = null;
            this.this$0.mUserData = null;
            this.this$0.mDcController = null;
            this.this$0.mDcTesterFailBringUpAll = null;
            if (!(this.this$0.mTelDevController == null || this.this$0.mTelDevController.getModem(0) == null || this.this$0.mTelDevController.getModem(0).hasRaCapability())) {
                this.this$0.unregisterNetworkAlertObserver();
            }
            this.this$0.mPhone.getContext().unregisterReceiver(this.this$0.mIntentReceiver);
        }

        public boolean processMessage(Message msg) {
            if (DataConnection.VDBG) {
                this.this$0.log("DcDefault msg=" + this.this$0.getWhatToString(msg.what) + " RefCount=" + this.this$0.mApnContexts.size());
            }
            switch (msg.what) {
                case 69633:
                    if (this.this$0.mAc == null) {
                        this.this$0.mAc = new AsyncChannel();
                        this.this$0.mAc.connected(null, this.this$0.getHandler(), msg.replyTo);
                        if (DataConnection.VDBG) {
                            this.this$0.log("DcDefaultState: FULL_CONNECTION reply connected");
                        }
                        this.this$0.mAc.replyToMessage(msg, 69634, 0, this.this$0.mId, "hi");
                        break;
                    }
                    if (DataConnection.VDBG) {
                        this.this$0.log("Disconnecting to previous connection mAc=" + this.this$0.mAc);
                    }
                    this.this$0.mAc.replyToMessage(msg, 69634, 3);
                    break;
                case 69636:
                    this.this$0.log("DcDefault: CMD_CHANNEL_DISCONNECTED before quiting call dump");
                    this.this$0.dumpToLog();
                    this.this$0.quit();
                    break;
                case SmsEnvelope.TELESERVICE_MWI /*262144*/:
                    this.this$0.log("DcDefaultState: msg.what=EVENT_CONNECT, fail not expected");
                    this.this$0.notifyConnectCompleted(msg.obj, DcFailCause.UNKNOWN, false);
                    break;
                case DataConnection.EVENT_DISCONNECT /*262148*/:
                    this.this$0.log("DcDefaultState deferring msg.what=EVENT_DISCONNECT RefCount=" + this.this$0.mApnContexts.size());
                    this.this$0.deferMessage(msg);
                    break;
                case DataConnection.EVENT_DISCONNECT_ALL /*262150*/:
                    this.this$0.log("DcDefaultState deferring msg.what=EVENT_DISCONNECT_ALL RefCount=" + this.this$0.mApnContexts.size());
                    this.this$0.deferMessage(msg);
                    break;
                case DataConnection.EVENT_TEAR_DOWN_NOW /*262152*/:
                    this.this$0.log("DcDefaultState EVENT_TEAR_DOWN_NOW");
                    this.this$0.mPhone.mCi.deactivateDataCall(this.this$0.mCid, 0, null);
                    break;
                case DataConnection.EVENT_LOST_CONNECTION /*262153*/:
                    this.this$0.logAndAddLogRec("DcDefaultState ignore EVENT_LOST_CONNECTION tag=" + msg.arg1 + ":mTag=" + this.this$0.mTag);
                    break;
                case DataConnection.EVENT_DATA_CONNECTION_DRS_OR_RAT_CHANGED /*262155*/:
                    Pair<Integer, Integer> drsRatPair = msg.obj.result;
                    this.this$0.mDataRegState = ((Integer) drsRatPair.first).intValue();
                    if (this.this$0.mRilRat != ((Integer) drsRatPair.second).intValue()) {
                        this.this$0.updateTcpBufferSizes(((Integer) drsRatPair.second).intValue());
                    }
                    this.this$0.mRilRat = ((Integer) drsRatPair.second).intValue();
                    this.this$0.log("DcDefaultState: EVENT_DATA_CONNECTION_DRS_OR_RAT_CHANGED drs=" + this.this$0.mDataRegState + " mRilRat=" + this.this$0.mRilRat);
                    int networkType = this.this$0.mPhone.getServiceState().getDataNetworkType();
                    this.this$0.mNetworkInfo.setSubtype(networkType, TelephonyManager.getNetworkTypeName(networkType));
                    if (this.this$0.mNetworkAgent != null) {
                        if (this.this$0.mIsInVoiceCall) {
                            this.this$0.mIsSupportConcurrent = DataConnectionHelper.getInstance().isDataSupportConcurrent(this.this$0.mPhone.getPhoneId());
                        }
                        this.this$0.updateNetworkInfoSuspendState();
                        this.this$0.mNetworkAgent.sendNetworkCapabilities(this.this$0.makeNetworkCapabilities());
                        this.this$0.mNetworkAgent.sendNetworkInfo(this.this$0.mNetworkInfo);
                        this.this$0.mNetworkAgent.sendLinkProperties(this.this$0.getLinkProperties());
                        break;
                    }
                    break;
                case DataConnection.EVENT_DATA_CONNECTION_ROAM_ON /*262156*/:
                    this.this$0.mNetworkInfo.setRoaming(true);
                    break;
                case DataConnection.EVENT_DATA_CONNECTION_ROAM_OFF /*262157*/:
                    this.this$0.mNetworkInfo.setRoaming(false);
                    break;
                case DataConnection.EVENT_IPV4_ADDRESS_REMOVED /*262160*/:
                    this.this$0.log("DcDefaultState: ignore EVENT_IPV4_ADDRESS_REMOVED not in ActiveState");
                    break;
                case DataConnection.EVENT_IPV6_ADDRESS_REMOVED /*262161*/:
                    this.this$0.log("DcDefaultState: ignore EVENT_IPV6_ADDRESS_REMOVED not in ActiveState");
                    break;
                case DataConnection.EVENT_ADDRESS_REMOVED /*262162*/:
                    this.this$0.log("DcDefaultState: " + this.this$0.getWhatToString(msg.what));
                    break;
                case DataConnection.EVENT_VOICE_CALL /*262163*/:
                    this.this$0.mIsInVoiceCall = msg.arg1 != 0;
                    this.this$0.mIsSupportConcurrent = msg.arg2 != 0;
                    break;
                case DataConnection.EVENT_IPV6_ADDRESS_UPDATED /*262165*/:
                    this.this$0.log("DcDefaultState: ignore EVENT_IPV6_ADDRESS_UPDATED not in ActiveState");
                    break;
                case 266240:
                    boolean val = this.this$0.getIsInactive();
                    if (DataConnection.VDBG) {
                        this.this$0.log("REQ_IS_INACTIVE  isInactive=" + val);
                    }
                    this.this$0.mAc.replyToMessage(msg, DcAsyncChannel.RSP_IS_INACTIVE, val ? 1 : 0);
                    break;
                case DcAsyncChannel.REQ_GET_CID /*266242*/:
                    int cid = this.this$0.getCid();
                    if (DataConnection.VDBG) {
                        this.this$0.log("REQ_GET_CID  cid=" + cid);
                    }
                    this.this$0.mAc.replyToMessage(msg, DcAsyncChannel.RSP_GET_CID, cid);
                    break;
                case DcAsyncChannel.REQ_GET_APNSETTING /*266244*/:
                    ApnSetting apnSetting = this.this$0.getApnSetting();
                    if (DataConnection.VDBG) {
                        this.this$0.log("REQ_GET_APNSETTING  mApnSetting=" + apnSetting);
                    }
                    this.this$0.mAc.replyToMessage(msg, DcAsyncChannel.RSP_GET_APNSETTING, apnSetting);
                    break;
                case DcAsyncChannel.REQ_GET_LINK_PROPERTIES /*266246*/:
                    LinkProperties lp = this.this$0.getCopyLinkProperties();
                    if (DataConnection.VDBG) {
                        this.this$0.log("REQ_GET_LINK_PROPERTIES linkProperties" + lp);
                    }
                    this.this$0.mAc.replyToMessage(msg, DcAsyncChannel.RSP_GET_LINK_PROPERTIES, lp);
                    break;
                case DcAsyncChannel.REQ_SET_LINK_PROPERTIES_HTTP_PROXY /*266248*/:
                    ProxyInfo proxy = msg.obj;
                    if (DataConnection.VDBG) {
                        this.this$0.log("REQ_SET_LINK_PROPERTIES_HTTP_PROXY proxy=" + proxy);
                    }
                    this.this$0.setLinkPropertiesHttpProxy(proxy);
                    this.this$0.mAc.replyToMessage(msg, DcAsyncChannel.RSP_SET_LINK_PROPERTIES_HTTP_PROXY);
                    if (this.this$0.mNetworkAgent != null) {
                        this.this$0.mNetworkAgent.sendLinkProperties(this.this$0.getLinkProperties());
                        break;
                    }
                    break;
                case DcAsyncChannel.REQ_GET_NETWORK_CAPABILITIES /*266250*/:
                    NetworkCapabilities nc = this.this$0.getCopyNetworkCapabilities();
                    if (DataConnection.VDBG) {
                        this.this$0.log("REQ_GET_NETWORK_CAPABILITIES networkCapabilities" + nc);
                    }
                    this.this$0.mAc.replyToMessage(msg, DcAsyncChannel.RSP_GET_NETWORK_CAPABILITIES, nc);
                    break;
                case DcAsyncChannel.REQ_RESET /*266252*/:
                    if (DataConnection.VDBG) {
                        this.this$0.log("DcDefaultState: msg.what=REQ_RESET");
                    }
                    this.this$0.transitionTo(this.this$0.mInactiveState);
                    break;
                case DcAsyncChannel.REQ_GET_APNTYPE /*266254*/:
                    String[] aryApnType = this.this$0.getApnType();
                    if (DataConnection.VDBG) {
                        this.this$0.log("REQ_GET_APNTYPE  aryApnType=" + aryApnType);
                    }
                    this.this$0.mAc.replyToMessage(msg, DcAsyncChannel.RSP_GET_APNTYPE, aryApnType);
                    break;
                default:
                    this.this$0.log("DcDefaultState: shouldn't happen but ignore msg.what=" + this.this$0.getWhatToString(msg.what));
                    break;
            }
            return true;
        }
    }

    private class DcDisconnectingState extends State {
        final /* synthetic */ DataConnection this$0;

        /* synthetic */ DcDisconnectingState(DataConnection this$0, DcDisconnectingState dcDisconnectingState) {
            this(this$0);
        }

        private DcDisconnectingState(DataConnection this$0) {
            this.this$0 = this$0;
        }

        public boolean processMessage(Message msg) {
            switch (msg.what) {
                case SmsEnvelope.TELESERVICE_MWI /*262144*/:
                    this.this$0.log("DcDisconnectingState msg.what=EVENT_CONNECT. Defer. RefCount = " + this.this$0.mApnContexts.size());
                    this.this$0.deferMessage(msg);
                    return true;
                case DataConnection.EVENT_DEACTIVATE_DONE /*262147*/:
                    AsyncResult ar = msg.obj;
                    DisconnectParams dp = ar.userObj;
                    String str = "DcDisconnectingState msg.what=EVENT_DEACTIVATE_DONE RefCount=" + this.this$0.mApnContexts.size();
                    this.this$0.log(str);
                    if (dp.mApnContext != null) {
                        dp.mApnContext.requestLog(str);
                    }
                    if (dp.mTag == this.this$0.mTag) {
                        this.this$0.mInactiveState.setEnterNotificationParams((DisconnectParams) ar.userObj);
                        this.this$0.transitionTo(this.this$0.mInactiveState);
                    } else {
                        this.this$0.log("DcDisconnectState stale EVENT_DEACTIVATE_DONE dp.tag=" + dp.mTag + " mTag=" + this.this$0.mTag);
                    }
                    return true;
                default:
                    if (DataConnection.VDBG) {
                        this.this$0.log("DcDisconnectingState not handled msg.what=" + this.this$0.getWhatToString(msg.what));
                    }
                    return false;
            }
        }
    }

    private class DcDisconnectionErrorCreatingConnection extends State {
        final /* synthetic */ DataConnection this$0;

        /* synthetic */ DcDisconnectionErrorCreatingConnection(DataConnection this$0, DcDisconnectionErrorCreatingConnection dcDisconnectionErrorCreatingConnection) {
            this(this$0);
        }

        private DcDisconnectionErrorCreatingConnection(DataConnection this$0) {
            this.this$0 = this$0;
        }

        public boolean processMessage(Message msg) {
            switch (msg.what) {
                case DataConnection.EVENT_DEACTIVATE_DONE /*262147*/:
                    ConnectionParams cp = msg.obj.userObj;
                    if (cp.mTag == this.this$0.mTag) {
                        String str = "DcDisconnectionErrorCreatingConnection msg.what=EVENT_DEACTIVATE_DONE";
                        this.this$0.log(str);
                        if (cp.mApnContext != null) {
                            cp.mApnContext.requestLog(str);
                        }
                        this.this$0.mInactiveState.setEnterNotificationParams(cp, DcFailCause.UNACCEPTABLE_NETWORK_PARAMETER);
                        this.this$0.transitionTo(this.this$0.mInactiveState);
                    } else {
                        this.this$0.log("DcDisconnectionErrorCreatingConnection stale EVENT_DEACTIVATE_DONE dp.tag=" + cp.mTag + ", mTag=" + this.this$0.mTag);
                    }
                    return true;
                default:
                    if (DataConnection.VDBG) {
                        this.this$0.log("DcDisconnectionErrorCreatingConnection not handled msg.what=" + this.this$0.getWhatToString(msg.what));
                    }
                    return false;
            }
        }
    }

    private class DcInactiveState extends State {
        final /* synthetic */ DataConnection this$0;

        /* synthetic */ DcInactiveState(DataConnection this$0, DcInactiveState dcInactiveState) {
            this(this$0);
        }

        private DcInactiveState(DataConnection this$0) {
            this.this$0 = this$0;
        }

        public void setEnterNotificationParams(ConnectionParams cp, DcFailCause cause) {
            if (DataConnection.VDBG) {
                this.this$0.log("DcInactiveState: setEnterNotificationParams cp,cause");
            }
            this.this$0.mConnectionParams = cp;
            this.this$0.mDisconnectParams = null;
            this.this$0.mDcFailCause = cause;
        }

        public void setEnterNotificationParams(DisconnectParams dp) {
            if (DataConnection.VDBG) {
                this.this$0.log("DcInactiveState: setEnterNotificationParams dp");
            }
            this.this$0.mConnectionParams = null;
            this.this$0.mDisconnectParams = dp;
            this.this$0.mDcFailCause = DcFailCause.NONE;
        }

        public void setEnterNotificationParams(DcFailCause cause) {
            this.this$0.mConnectionParams = null;
            this.this$0.mDisconnectParams = null;
            this.this$0.mDcFailCause = cause;
        }

        public void enter() {
            DataConnection dataConnection = this.this$0;
            dataConnection.mTag++;
            this.this$0.log("DcInactiveState: enter() mTag=" + this.this$0.mTag);
            if (this.this$0.mConnectionParams != null) {
                this.this$0.log("DcInactiveState: enter notifyConnectCompleted +ALL failCause=" + this.this$0.mDcFailCause);
                this.this$0.notifyConnectCompleted(this.this$0.mConnectionParams, this.this$0.mDcFailCause, true);
            }
            if (this.this$0.mDisconnectParams != null) {
                this.this$0.log("DcInactiveState: enter notifyDisconnectCompleted +ALL failCause=" + this.this$0.mDcFailCause);
                this.this$0.notifyDisconnectCompleted(this.this$0.mDisconnectParams, true);
            }
            if (this.this$0.mDisconnectParams == null && this.this$0.mConnectionParams == null && this.this$0.mDcFailCause != null) {
                this.this$0.log("DcInactiveState: enter notifyAllDisconnectCompleted failCause=" + this.this$0.mDcFailCause);
                this.this$0.notifyAllDisconnectCompleted(this.this$0.mDcFailCause);
            }
            this.this$0.mDcController.removeActiveDcByCid(this.this$0);
            this.this$0.clearSettings();
        }

        public void exit() {
        }

        public boolean processMessage(Message msg) {
            switch (msg.what) {
                case SmsEnvelope.TELESERVICE_MWI /*262144*/:
                    this.this$0.log("DcInactiveState: mag.what=EVENT_CONNECT");
                    ConnectionParams cp = msg.obj;
                    if (this.this$0.initConnection(cp)) {
                        this.this$0.onConnect(this.this$0.mConnectionParams);
                        this.this$0.transitionTo(this.this$0.mActivatingState);
                    } else {
                        this.this$0.log("DcInactiveState: msg.what=EVENT_CONNECT initConnection failed");
                        this.this$0.notifyConnectCompleted(cp, DcFailCause.UNACCEPTABLE_NETWORK_PARAMETER, false);
                    }
                    return true;
                case DataConnection.EVENT_DISCONNECT /*262148*/:
                    this.this$0.log("DcInactiveState: msg.what=EVENT_DISCONNECT");
                    this.this$0.notifyDisconnectCompleted((DisconnectParams) msg.obj, false);
                    return true;
                case DataConnection.EVENT_DISCONNECT_ALL /*262150*/:
                    this.this$0.log("DcInactiveState: msg.what=EVENT_DISCONNECT_ALL");
                    this.this$0.notifyDisconnectCompleted((DisconnectParams) msg.obj, false);
                    return true;
                case DcAsyncChannel.REQ_RESET /*266252*/:
                    this.this$0.log("DcInactiveState: msg.what=RSP_RESET, ignore we're already reset");
                    return true;
                default:
                    if (DataConnection.VDBG) {
                        this.this$0.log("DcInactiveState nothandled msg.what=" + this.this$0.getWhatToString(msg.what));
                    }
                    return false;
            }
        }
    }

    private class DcNetworkAgent extends NetworkAgent {
        final /* synthetic */ DataConnection this$0;

        public DcNetworkAgent(DataConnection this$0, Looper l, Context c, String TAG, NetworkInfo ni, NetworkCapabilities nc, LinkProperties lp, int score, NetworkMisc misc) {
            this.this$0 = this$0;
            super(l, c, TAG, ni, nc, lp, score, misc);
        }

        protected void unwanted() {
            if (this.this$0.mNetworkAgent != this) {
                log("DcNetworkAgent: unwanted found mNetworkAgent=" + this.this$0.mNetworkAgent + ", which isn't me.  Aborting unwanted");
                return;
            }
            log("DcNetworkAgent unwanted!");
            if (this.this$0.mApnContexts != null) {
                for (ConnectionParams cp : this.this$0.mApnContexts.values()) {
                    ApnContext apnContext = cp.mApnContext;
                    Pair<ApnContext, Integer> pair = new Pair(apnContext, Integer.valueOf(cp.mConnectionGeneration));
                    log("DcNetworkAgent: [unwanted]: disconnect apnContext=" + apnContext);
                    this.this$0.sendMessage(this.this$0.obtainMessage(DataConnection.EVENT_DISCONNECT, new DisconnectParams(apnContext, apnContext.getReason(), this.this$0.mDct.obtainMessage(270351, pair))));
                }
            }
        }

        protected void pollLceData() {
            if (this.this$0.mPhone.getLceStatus() == 1) {
                this.this$0.mPhone.mCi.pullLceData(this.this$0.obtainMessage(DataConnection.EVENT_BW_REFRESH_RESPONSE));
            }
        }

        protected void networkStatus(int status, String redirectUrl) {
            if (!TextUtils.isEmpty(redirectUrl)) {
                log("validation status: " + status + " with redirection URL: " + redirectUrl);
                this.this$0.mDct.obtainMessage(270380, redirectUrl).sendToTarget();
            }
        }
    }

    public static class DisconnectParams {
        public ApnContext mApnContext;
        Message mOnCompletedMsg;
        String mReason;
        int mTag;

        DisconnectParams(ApnContext apnContext, String reason, Message onCompletedMsg) {
            this.mApnContext = apnContext;
            this.mReason = reason;
            this.mOnCompletedMsg = onCompletedMsg;
        }

        public String toString() {
            return "{mTag=" + this.mTag + " mApnContext=" + this.mApnContext + " mReason=" + this.mReason + " mOnCompletedMsg=" + DataConnection.msgToString(this.mOnCompletedMsg) + "}";
        }
    }

    public static class UpdateLinkPropertyResult {
        public LinkProperties newLp;
        public LinkProperties oldLp;
        public SetupResult setupResult;

        public UpdateLinkPropertyResult(LinkProperties curLp) {
            this.setupResult = SetupResult.SUCCESS;
            this.oldLp = curLp;
            this.newLp = curLp;
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.dataconnection.DataConnection.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.dataconnection.DataConnection.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.dataconnection.DataConnection.<clinit>():void");
    }

    static String cmdToString(int cmd) {
        String value;
        cmd -= SmsEnvelope.TELESERVICE_MWI;
        if (cmd < 0 || cmd >= sCmdToString.length) {
            value = DcAsyncChannel.cmdToString(cmd + SmsEnvelope.TELESERVICE_MWI);
        } else {
            value = sCmdToString[cmd];
        }
        if (value == null) {
            return "0x" + Integer.toHexString(cmd + SmsEnvelope.TELESERVICE_MWI);
        }
        return value;
    }

    public static DataConnection makeDataConnection(Phone phone, int id, DcTracker dct, DcTesterFailBringUpAll failBringUpAll, DcController dcc) {
        DataConnection dc = new DataConnection(phone, "DC-" + mInstanceNumber.incrementAndGet(), id, dct, failBringUpAll, dcc);
        dc.start();
        dc.log("Made " + dc.getName());
        return dc;
    }

    void dispose() {
        log("dispose: call quiteNow()");
        quitNow();
    }

    NetworkCapabilities getCopyNetworkCapabilities() {
        return makeNetworkCapabilities();
    }

    LinkProperties getCopyLinkProperties() {
        return new LinkProperties(this.mLinkProperties);
    }

    boolean getIsInactive() {
        return getCurrentState() == this.mInactiveState;
    }

    int getCid() {
        return this.mCid;
    }

    ApnSetting getApnSetting() {
        return this.mApnSetting;
    }

    String[] getApnType() {
        log("getApnType: mApnContexts.size() = " + this.mApnContexts.size());
        if (this.mApnContexts.size() == 0) {
            return null;
        }
        String[] aryApnType = new String[this.mApnContexts.values().size()];
        int i = 0;
        for (ConnectionParams cp : this.mApnContexts.values()) {
            String apnType = cp.mApnContext.getApnType();
            log("getApnType: apnType = " + apnType);
            aryApnType[i] = new String(apnType);
            i++;
        }
        return aryApnType;
    }

    void setLinkPropertiesHttpProxy(ProxyInfo proxy) {
        this.mLinkProperties.setHttpProxy(proxy);
    }

    public boolean isIpv4Connected() {
        for (InetAddress addr : this.mLinkProperties.getAddresses()) {
            log("isIpv4Connected(), addr:" + addr);
            if (addr instanceof Inet4Address) {
                Inet4Address i4addr = (Inet4Address) addr;
                log("isAnyLocalAddress:" + i4addr.isAnyLocalAddress() + "/isLinkLocalAddress()" + i4addr.isLinkLocalAddress() + "/isLoopbackAddress()" + i4addr.isLoopbackAddress() + "/isMulticastAddress()" + i4addr.isMulticastAddress());
                if (!(i4addr.isAnyLocalAddress() || i4addr.isLinkLocalAddress() || i4addr.isLoopbackAddress() || i4addr.isMulticastAddress())) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isIpv6Connected() {
        for (InetAddress addr : this.mLinkProperties.getAddresses()) {
            log("isIpv6Connected(), addr:" + addr);
            if (addr instanceof Inet6Address) {
                Inet6Address i6addr = (Inet6Address) addr;
                if (!(i6addr.isAnyLocalAddress() || i6addr.isLinkLocalAddress() || i6addr.isLoopbackAddress() || i6addr.isMulticastAddress())) {
                    return true;
                }
            }
        }
        return false;
    }

    public UpdateLinkPropertyResult updateLinkProperty(DataCallResponse newState) {
        UpdateLinkPropertyResult result = new UpdateLinkPropertyResult(this.mLinkProperties);
        if (newState == null) {
            return result;
        }
        result.newLp = new LinkProperties();
        result.setupResult = setLinkProperties(newState, result.newLp);
        if (result.setupResult != SetupResult.SUCCESS) {
            log("updateLinkProperty failed : " + result.setupResult);
            return result;
        }
        result.newLp.setHttpProxy(this.mLinkProperties.getHttpProxy());
        checkSetMtu(this.mApnSetting, result.newLp);
        this.mLinkProperties = result.newLp;
        updateTcpBufferSizes(this.mRilRat);
        if (!result.oldLp.equals(result.newLp)) {
            log("updateLinkProperty old LP=" + result.oldLp);
            log("updateLinkProperty new LP=" + result.newLp);
        }
        if (!(result.newLp.equals(result.oldLp) || this.mNetworkAgent == null)) {
            this.mNetworkAgent.sendLinkProperties(getLinkProperties());
        }
        return result;
    }

    private void checkSetMtu(ApnSetting apn, LinkProperties lp) {
        if (lp != null && apn != null && lp != null) {
            if (lp.getMtu() != 0) {
                log("MTU set by call response to: " + lp.getMtu());
            } else if (apn == null || apn.mtu == 0) {
                int mtu = this.mPhone.getContext().getResources().getInteger(17694863);
                try {
                    String defaultMccMnc = TelephonyManager.getDefault().getSimOperatorNumeric();
                    if (!TextUtils.isEmpty(defaultMccMnc) && defaultMccMnc.length() >= 3) {
                        int mcc = Integer.parseInt(defaultMccMnc.substring(0, 3));
                        int mnc = Integer.parseInt(defaultMccMnc.substring(3));
                        if (mcc == 460) {
                            mtu = 1410;
                            if (mnc == 3 || mnc == 5 || mnc == 11) {
                                mtu = 1460;
                            }
                        }
                    }
                } catch (Exception e) {
                    log("MTU set error");
                }
                if (mtu != 0) {
                    lp.setMtu(mtu);
                    log("MTU set by config resource to: " + mtu);
                }
            } else {
                lp.setMtu(apn.mtu);
                log("MTU set by APN to: " + apn.mtu);
            }
        }
    }

    private DataConnection(Phone phone, String name, int id, DcTracker dct, DcTesterFailBringUpAll failBringUpAll, DcController dcc) {
        super(name, dcc.getHandler());
        this.mDct = null;
        this.mSubController = SubscriptionController.getInstance();
        this.mRetryCount = 0;
        this.mInterfaceName = null;
        this.mTelDevController = TelephonyDevController.getInstance();
        this.mLinkProperties = new LinkProperties();
        this.mRilRat = Integer.MAX_VALUE;
        this.mDataRegState = Integer.MAX_VALUE;
        this.mIsInVoiceCall = false;
        this.mIsSupportConcurrent = false;
        this.mGlobalV6AddrInfo = null;
        this.mApnContexts = null;
        this.mReconnectIntent = null;
        this.mIntentReceiver = new AnonymousClass1(this);
        this.mRestrictedNetworkOverride = false;
        this.mDefaultState = new DcDefaultState(this, null);
        this.mInactiveState = new DcInactiveState(this, null);
        this.mActivatingState = new DcActivatingState(this, null);
        this.mActiveState = new DcActiveState(this, null);
        this.mDisconnectingState = new DcDisconnectingState(this, null);
        this.mDisconnectingErrorCreatingConnection = new DcDisconnectionErrorCreatingConnection(this, null);
        this.mAlertObserver = new AnonymousClass2(this);
        setLogRecSize(300);
        setLogOnlyTransitions(true);
        log("DataConnection created");
        this.mPhone = phone;
        this.mDct = dct;
        this.mDcTesterFailBringUpAll = failBringUpAll;
        this.mDcController = dcc;
        this.mId = id;
        this.mCid = -1;
        this.mRat = 1;
        ServiceState ss = this.mPhone.getServiceState();
        this.mRilRat = ss.getRilDataRadioTechnology();
        this.mDataRegState = this.mPhone.getServiceState().getDataRegState();
        int networkType = ss.getDataNetworkType();
        this.mNetworkInfo = new NetworkInfo(0, networkType, NETWORK_TYPE, TelephonyManager.getNetworkTypeName(networkType));
        this.mNetworkInfo.setRoaming(ss.getDataRoaming());
        this.mNetworkInfo.setIsAvailable(true);
        this.mNetworkInfo.setMetered(true);
        if (!BSP_PACKAGE) {
            try {
                this.mGsmDCTExt = (IGsmDCTExt) MPlugin.createInstance(IGsmDCTExt.class.getName(), this.mPhone.getContext());
            } catch (Exception e) {
                log("mGsmDCTExt init fail");
                e.printStackTrace();
            }
        }
        addState(this.mDefaultState);
        addState(this.mInactiveState, this.mDefaultState);
        addState(this.mActivatingState, this.mDefaultState);
        addState(this.mActiveState, this.mDefaultState);
        addState(this.mDisconnectingState, this.mDefaultState);
        addState(this.mDisconnectingErrorCreatingConnection, this.mDefaultState);
        setInitialState(this.mInactiveState);
        this.mApnContexts = new HashMap();
        log("get INetworkManagementService");
        this.mNetworkManager = Stub.asInterface(ServiceManager.getService("network_management"));
        this.mDcFcMgr = DcFailCauseManager.getInstance(this.mPhone);
        this.mAlarmManager = (AlarmManager) this.mPhone.getContext().getSystemService("alarm");
        this.mActionRetry = getClass().getCanonicalName() + "." + getName() + ".action_retry";
        resetRetryCount();
    }

    private void onConnect(ConnectionParams cp) {
        DataCallResponse response;
        Message msg;
        logi("onConnect: carrier='" + this.mApnSetting.carrier + "' APN='" + this.mApnSetting.apn + "' proxy='" + this.mApnSetting.proxy + "' port='" + this.mApnSetting.port + "'");
        if (cp.mApnContext != null) {
            cp.mApnContext.requestLog("DataConnection.onConnect");
        }
        try {
            if (OemConstant.isPoliceVersion(this.mPhone) || OemConstant.isDeviceLockVersion()) {
                boolean isDataAllow = OemConstant.isDataAllow(this.mPhone);
                String apntype = UsimPBMemInfo.STRING_NOT_SET;
                if (!(cp == null || cp.mApnContext == null)) {
                    apntype = cp.mApnContext.getApnType();
                }
                boolean isSpecialApn = !TextUtils.isEmpty(apntype) ? !ImsSwitchController.IMS_SERVICE.equals(apntype) ? "emergency".equals(apntype) : true : false;
                Rlog.d("data", "onConnect:isDataAllow=" + isDataAllow + " apntype=" + apntype + " isSpecialApn=" + isSpecialApn);
                if (!(isDataAllow || isSpecialApn)) {
                    response = new DataCallResponse();
                    response.version = this.mPhone.mCi.getRilVersion();
                    response.status = 65535;
                    response.cid = 0;
                    response.active = 0;
                    response.type = UsimPBMemInfo.STRING_NOT_SET;
                    response.ifname = UsimPBMemInfo.STRING_NOT_SET;
                    response.addresses = new String[0];
                    response.dnses = new String[0];
                    response.gateways = new String[0];
                    response.suggestedRetryTime = -1;
                    response.pcscf = new String[0];
                    response.mtu = 0;
                    msg = obtainMessage(EVENT_SETUP_DATA_CONNECTION_DONE, cp);
                    AsyncResult.forMessage(msg, response, null);
                    sendMessage(msg);
                    return;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (this.mDcTesterFailBringUpAll.getDcFailBringUp().mCounter > 0) {
            response = new DataCallResponse();
            response.version = this.mPhone.mCi.getRilVersion();
            response.status = this.mDcTesterFailBringUpAll.getDcFailBringUp().mFailCause.getErrorCode();
            response.cid = 0;
            response.active = 0;
            response.type = UsimPBMemInfo.STRING_NOT_SET;
            response.ifname = UsimPBMemInfo.STRING_NOT_SET;
            response.addresses = new String[0];
            response.dnses = new String[0];
            response.gateways = new String[0];
            response.suggestedRetryTime = this.mDcTesterFailBringUpAll.getDcFailBringUp().mSuggestedRetryTime;
            response.pcscf = new String[0];
            response.mtu = 0;
            msg = obtainMessage(EVENT_SETUP_DATA_CONNECTION_DONE, cp);
            AsyncResult.forMessage(msg, response, null);
            sendMessage(msg);
            log("onConnect: FailBringUpAll=" + this.mDcTesterFailBringUpAll.getDcFailBringUp() + " send error response=" + response);
            DcFailBringUp dcFailBringUp = this.mDcTesterFailBringUpAll.getDcFailBringUp();
            dcFailBringUp.mCounter--;
            return;
        }
        String protocol;
        this.mCreateTime = -1;
        this.mLastFailTime = -1;
        this.mLastFailCause = DcFailCause.NONE;
        msg = obtainMessage(EVENT_SETUP_DATA_CONNECTION_DONE, cp);
        msg.obj = cp;
        int authType = this.mApnSetting.authType;
        if (authType == -1) {
            if (TextUtils.isEmpty(this.mApnSetting.user)) {
                authType = 0;
            } else {
                authType = 3;
            }
        }
        if (this.mPhone.getServiceState().getDataRoamingFromRegistration()) {
            protocol = this.mApnSetting.roamingProtocol;
        } else {
            protocol = this.mApnSetting.protocol;
        }
        this.mPhone.mCi.setupDataCall(cp.mRilRat, cp.mProfileId, this.mApnSetting.apn, this.mApnSetting.user, this.mApnSetting.password, authType, protocol, this.mId + 1, msg);
    }

    private void tearDownData(Object o) {
        int discReason = 0;
        ApnContext apnContext = null;
        if (o != null && (o instanceof DisconnectParams)) {
            DisconnectParams dp = (DisconnectParams) o;
            apnContext = dp.mApnContext;
            if (TextUtils.equals(dp.mReason, PhoneInternalInterface.REASON_RADIO_TURNED_OFF)) {
                discReason = 1;
            } else if (TextUtils.equals(dp.mReason, PhoneInternalInterface.REASON_PDP_RESET)) {
                discReason = 2;
            } else if (TextUtils.equals(dp.mReason, PhoneInternalInterface.REASON_RA_FAILED)) {
                if (this.mValid == 0) {
                    discReason = 2001;
                } else if (this.mValid == -1) {
                    discReason = 2002;
                } else if (this.mValid == -2) {
                    discReason = 2004;
                }
            } else if (TextUtils.equals(dp.mReason, PhoneInternalInterface.REASON_PCSCF_ADDRESS_FAILED)) {
                discReason = 2003;
            }
        }
        String str;
        if (this.mPhone.mCi.getRadioState().isOn() || this.mPhone.getServiceState().getRilDataRadioTechnology() == 18) {
            str = "tearDownData radio is on, call deactivateDataCall";
            log(str);
            if (apnContext != null) {
                apnContext.requestLog(str);
            }
            this.mPhone.mCi.deactivateDataCall(this.mCid, discReason, obtainMessage(EVENT_DEACTIVATE_DONE, this.mTag, 0, o));
            return;
        }
        str = "tearDownData radio is off sendMessage EVENT_DEACTIVATE_DONE immediately";
        log(str);
        if (apnContext != null) {
            apnContext.requestLog(str);
        }
        sendMessage(obtainMessage(EVENT_DEACTIVATE_DONE, this.mTag, 0, new AsyncResult(o, null, null)));
    }

    private void notifyAllWithEvent(ApnContext alreadySent, int event, String reason) {
        this.mNetworkInfo.setDetailedState(this.mNetworkInfo.getDetailedState(), reason, this.mNetworkInfo.getExtraInfo());
        for (ConnectionParams cp : this.mApnContexts.values()) {
            ApnContext apnContext = cp.mApnContext;
            if (apnContext != alreadySent) {
                if (reason != null) {
                    apnContext.setReason(reason);
                }
                Message msg = this.mDct.obtainMessage(event, new Pair(apnContext, Integer.valueOf(cp.mConnectionGeneration)));
                AsyncResult.forMessage(msg);
                msg.sendToTarget();
            }
        }
    }

    private void notifyAllOfConnected(String reason) {
        notifyAllWithEvent(null, 270336, reason);
    }

    private void notifyAllOfDisconnectDcRetrying(String reason) {
        notifyAllWithEvent(null, 270370, reason);
    }

    private void notifyAllDisconnectCompleted(DcFailCause cause) {
        notifyAllWithEvent(null, 270351, cause.toString());
    }

    private void notifyDefaultApnReferenceCountChanged(int refCount, int event) {
        Message msg = this.mDct.obtainMessage(event);
        msg.arg1 = refCount;
        AsyncResult.forMessage(msg);
        msg.sendToTarget();
    }

    private void notifyConnectCompleted(ConnectionParams cp, DcFailCause cause, boolean sendAll) {
        ApnContext alreadySent = null;
        if (!(cp == null || cp.mOnCompletedMsg == null)) {
            Message connectionCompletedMsg = cp.mOnCompletedMsg;
            cp.mOnCompletedMsg = null;
            alreadySent = cp.mApnContext;
            long timeStamp = System.currentTimeMillis();
            connectionCompletedMsg.arg1 = this.mCid;
            if (cause == DcFailCause.NONE) {
                this.mCreateTime = timeStamp;
                AsyncResult.forMessage(connectionCompletedMsg);
            } else {
                this.mLastFailCause = cause;
                this.mLastFailTime = timeStamp;
                if (cause == null) {
                    cause = DcFailCause.UNKNOWN;
                }
                AsyncResult.forMessage(connectionCompletedMsg, cause, new Throwable(cause.toString()));
            }
            logi("notifyConnectCompleted at " + timeStamp + " cause=" + cause + " connectionCompletedMsg=" + msgToString(connectionCompletedMsg));
            connectionCompletedMsg.sendToTarget();
        }
        if (sendAll) {
            log("Send to all. " + alreadySent + " " + cause.toString());
            notifyAllWithEvent(alreadySent, 270371, cause.toString());
        }
    }

    private void notifyDisconnectCompleted(DisconnectParams dp, boolean sendAll) {
        if (VDBG) {
            log("NotifyDisconnectCompleted");
        }
        ApnContext alreadySent = null;
        String reason = null;
        if (!(dp == null || dp.mOnCompletedMsg == null)) {
            Message msg = dp.mOnCompletedMsg;
            dp.mOnCompletedMsg = null;
            if (msg.obj instanceof ApnContext) {
                alreadySent = msg.obj;
                for (ConnectionParams cp : this.mApnContexts.values()) {
                    ApnContext apnContext = cp.mApnContext;
                    if (apnContext == alreadySent && PhoneInternalInterface.REASON_RA_FAILED.equals(dp.mReason)) {
                        log("set reason:" + dp.mReason);
                        apnContext.setReason(dp.mReason);
                    }
                }
            }
            reason = dp.mReason;
            if (VDBG) {
                String str = "msg=%s msg.obj=%s";
                Object[] objArr = new Object[2];
                objArr[0] = msg.toString();
                objArr[1] = msg.obj instanceof String ? (String) msg.obj : "<no-reason>";
                log(String.format(str, objArr));
            }
            AsyncResult.forMessage(msg);
            msg.sendToTarget();
        }
        if (sendAll) {
            if (reason == null) {
                reason = DcFailCause.UNKNOWN.toString();
            }
            notifyAllWithEvent(alreadySent, 270351, reason);
        }
        log("NotifyDisconnectCompleted DisconnectParams=" + dp);
    }

    public int getDataConnectionId() {
        return this.mId;
    }

    private void clearSettings() {
        log("clearSettings");
        resetRetryCount();
        this.mCreateTime = -1;
        this.mLastFailTime = -1;
        this.mLastFailCause = DcFailCause.NONE;
        this.mCid = -1;
        this.mRat = 1;
        this.mPcscfAddr = new String[5];
        this.mLinkProperties = new LinkProperties();
        this.mApnContexts.clear();
        this.mApnSetting = null;
        this.mDcFailCause = null;
        this.mGlobalV6AddrInfo = null;
    }

    private SetupResult onSetupConnectionCompleted(AsyncResult ar) {
        SetupResult result;
        DataCallResponse response = ar.result;
        ConnectionParams cp = ar.userObj;
        if (cp.mTag != this.mTag) {
            log("onSetupConnectionCompleted stale cp.tag=" + cp.mTag + ", mtag=" + this.mTag);
            result = SetupResult.ERR_Stale;
        } else if (ar.exception != null) {
            log("onSetupConnectionCompleted failed, ar.exception=" + ar.exception + " response=" + response);
            if ((ar.exception instanceof CommandException) && ((CommandException) ar.exception).getCommandError() == Error.RADIO_NOT_AVAILABLE) {
                result = SetupResult.ERR_BadCommand;
                result.mFailCause = DcFailCause.RADIO_NOT_AVAILABLE;
            } else if (response == null || response.version < 4) {
                result = SetupResult.ERR_GetLastErrorFromRil;
            } else {
                result = SetupResult.ERR_RilError;
                result.mFailCause = DcFailCause.fromInt(response.status);
            }
        } else if (response.status != 0) {
            result = SetupResult.ERR_RilError;
            result.mFailCause = DcFailCause.fromInt(response.status);
        } else {
            log("onSetupConnectionCompleted received successful DataCallResponse");
            this.mCid = response.cid;
            this.mRat = response.rat;
            this.mPcscfAddr = response.pcscf;
            result = updateLinkProperty(response).setupResult;
            this.mInterfaceName = response.ifname;
            log("onSetupConnectionCompleted: ifname-" + this.mInterfaceName);
        }
        ServiceStateTracker.mDataCallCount++;
        log("[POWERSTATE]mDataCallCount:" + ServiceStateTracker.mDataCallCount);
        return result;
    }

    private SetupResult onSetupFallbackConnection(AsyncResult ar) {
        DataCallResponse response = ar.result;
        ConnectionParams cp = ar.userObj;
        if (cp.mTag != this.mTag) {
            log("onSetupFallbackConnection stale cp.tag=" + cp.mTag + ", mtag=" + this.mTag);
            return SetupResult.ERR_Stale;
        }
        log("onSetupFallbackConnection received DataCallResponse: " + response);
        this.mCid = response.cid;
        this.mRat = response.rat;
        this.mPcscfAddr = response.pcscf;
        int tempStatus = response.status;
        response.status = DcFailCause.NONE.getErrorCode();
        SetupResult result = updateLinkProperty(response).setupResult;
        response.status = tempStatus;
        this.mInterfaceName = response.ifname;
        log("onSetupConnectionCompleted: ifname-" + this.mInterfaceName);
        return result;
    }

    private boolean isDnsOk(String[] domainNameServers) {
        if (!NULL_IP.equals(domainNameServers[0]) || !NULL_IP.equals(domainNameServers[1]) || this.mPhone.isDnsCheckDisabled() || (this.mApnSetting.types[0].equals("mms") && isIpAddress(this.mApnSetting.mmsProxy))) {
            return true;
        }
        Object[] objArr = new Object[4];
        objArr[0] = this.mApnSetting.types[0];
        objArr[1] = "mms";
        objArr[2] = this.mApnSetting.mmsProxy;
        objArr[3] = Boolean.valueOf(isIpAddress(this.mApnSetting.mmsProxy));
        log(String.format("isDnsOk: return false apn.types[0]=%s APN_TYPE_MMS=%s isIpAddress(%s)=%s", objArr));
        return false;
    }

    private void updateTcpBufferSizes(int rilRat) {
        String sizes = null;
        if (rilRat == 19) {
            rilRat = 14;
        }
        String ratName = ServiceState.rilRadioTechnologyToString(rilRat).toLowerCase(Locale.ROOT);
        if (rilRat == 7 || rilRat == 8 || rilRat == 12) {
            ratName = "evdo";
        }
        String[] configOverride = this.mPhone.getContext().getResources().getStringArray(17236022);
        for (String split : configOverride) {
            String[] split2 = split.split(":");
            if (ratName.equals(split2[0]) && split2.length == 2) {
                sizes = split2[1];
                break;
            }
        }
        if (sizes == null) {
            switch (rilRat) {
                case 1:
                    sizes = TCP_BUFFER_SIZES_GPRS;
                    break;
                case 2:
                    sizes = TCP_BUFFER_SIZES_EDGE;
                    break;
                case 3:
                    sizes = TCP_BUFFER_SIZES_UMTS;
                    break;
                case 6:
                    sizes = TCP_BUFFER_SIZES_1XRTT;
                    break;
                case 7:
                case 8:
                case 12:
                    sizes = TCP_BUFFER_SIZES_EVDO;
                    break;
                case 9:
                    sizes = TCP_BUFFER_SIZES_HSDPA;
                    break;
                case 10:
                case 11:
                    sizes = TCP_BUFFER_SIZES_HSPA;
                    break;
                case 13:
                    sizes = TCP_BUFFER_SIZES_EHRPD;
                    break;
                case 14:
                case 19:
                    sizes = TCP_BUFFER_SIZES_LTE;
                    break;
                case 15:
                    sizes = TCP_BUFFER_SIZES_HSPAP;
                    break;
            }
        }
        this.mLinkProperties.setTcpBufferSizes(sizes);
    }

    private void setNetworkRestriction() {
        boolean z = false;
        this.mRestrictedNetworkOverride = false;
        boolean noRestrictedRequests = true;
        for (ApnContext apnContext : this.mApnContexts.keySet()) {
            noRestrictedRequests &= apnContext.hasNoRestrictedRequests(true);
        }
        if (!noRestrictedRequests && this.mApnSetting.isMetered(this.mPhone.getContext(), this.mPhone.getSubId(), this.mPhone.getServiceState().getDataRoaming())) {
            this.mRestrictedNetworkOverride = !this.mDct.isDataEnabled(true);
            if (this.mRestrictedNetworkOverride && !this.mDct.haveVsimIgnoreUserDataSetting()) {
                z = true;
            }
            this.mRestrictedNetworkOverride = z;
        }
    }

    private NetworkCapabilities makeNetworkCapabilities() {
        NetworkCapabilities result = new NetworkCapabilities();
        ApnSetting apnSetting = this.mApnSetting;
        result.addTransportType(0);
        if (!(this.mConnectionParams == null || this.mConnectionParams.mApnContext == null || this.mRat != 2)) {
            ArrayList<ApnSetting> wifiApnList = this.mConnectionParams.mApnContext.getWifiApns();
            if (wifiApnList != null) {
                for (ApnSetting tApnSetting : wifiApnList) {
                    if (!(tApnSetting == null || tApnSetting.apn.equals(UsimPBMemInfo.STRING_NOT_SET))) {
                        log("makeNetworkCapabilities: apn: " + tApnSetting.apn);
                        apnSetting = tApnSetting;
                    }
                }
            }
        }
        boolean isDataEnabled = this.mDct.getDataEnabled();
        log("makeNetworkCapabilities: check data enable:" + isDataEnabled);
        isDataEnabled = !isDataEnabled ? this.mDct.haveVsimIgnoreUserDataSetting() : true;
        log("makeNetworkCapabilities: after check data enable:" + isDataEnabled);
        if (apnSetting != null) {
            for (String type : apnSetting.types) {
                if (type.equals(CharacterSets.MIMENAME_ANY_CHARSET)) {
                    if (isDataEnabled && isDefaultDataSubPhone(this.mPhone)) {
                        result.addCapability(12);
                    }
                    if (isVsimActive()) {
                        result.addCapability(26);
                        result.removeCapability(12);
                    }
                    result.addCapability(0);
                    result.addCapability(1);
                    result.addCapability(3);
                    result.addCapability(5);
                    result.addCapability(7);
                    result.addCapability(20);
                    result.addCapability(21);
                    result.addCapability(22);
                    result.addCapability(23);
                    result.addCapability(24);
                    result.addCapability(25);
                    result.addCapability(9);
                    result.addCapability(8);
                    result.addCapability(27);
                } else if (type.equals("default")) {
                    if (isDataEnabled && isDefaultDataSubPhone(this.mPhone)) {
                        result.addCapability(12);
                    }
                    if (isVsimActive()) {
                        result.addCapability(26);
                        result.removeCapability(12);
                    }
                } else if (type.equals("mms")) {
                    result.addCapability(0);
                } else if (type.equals("supl")) {
                    result.addCapability(1);
                } else if (type.equals("dun")) {
                    ApnSetting securedDunApn = this.mDct.fetchDunApn();
                    if (securedDunApn == null || securedDunApn.equals(this.mApnSetting)) {
                        result.addCapability(2);
                    }
                } else if (type.equals("fota")) {
                    result.addCapability(3);
                } else if (type.equals(ImsSwitchController.IMS_SERVICE)) {
                    result.addCapability(4);
                } else if (type.equals("cbs")) {
                    result.addCapability(5);
                } else if (type.equals("ia")) {
                    result.addCapability(7);
                } else if (type.equals("emergency")) {
                    result.addCapability(10);
                } else if (type.equals("dm")) {
                    result.addCapability(20);
                } else if (type.equals("wap")) {
                    result.addCapability(21);
                } else if (type.equals("net")) {
                    result.addCapability(22);
                } else if (type.equals("cmmail")) {
                    result.addCapability(23);
                } else if (type.equals("tethering")) {
                    result.addCapability(24);
                } else if (type.equals("rcse")) {
                    result.addCapability(25);
                } else if (type.equals("xcap")) {
                    result.addCapability(9);
                } else if (type.equals("rcs")) {
                    result.addCapability(8);
                } else if (type.equals("bip")) {
                    result.addCapability(27);
                }
            }
            if (this.mApnSetting.isMetered(this.mPhone.getContext(), this.mPhone.getSubId(), this.mPhone.getServiceState().getDataRoaming())) {
                result.removeCapability(11);
                this.mNetworkInfo.setMetered(true);
            } else {
                result.addCapability(11);
                this.mNetworkInfo.setMetered(false);
            }
            result.maybeMarkCapabilitiesRestricted();
        }
        if (this.mRestrictedNetworkOverride) {
            result.removeCapability(13);
            result.removeCapability(2);
        }
        int up = 14;
        int down = 14;
        switch (this.mRilRat) {
            case 1:
                up = 80;
                down = 80;
                break;
            case 2:
                up = 59;
                down = 236;
                break;
            case 3:
                up = 384;
                down = 384;
                break;
            case 4:
            case 5:
                up = 14;
                down = 14;
                break;
            case 6:
                up = 100;
                down = 100;
                break;
            case 7:
                up = 153;
                down = 2457;
                break;
            case 8:
                up = 1843;
                down = 3174;
                break;
            case 9:
                up = 2048;
                down = 14336;
                break;
            case 10:
                up = 5898;
                down = 14336;
                break;
            case 11:
                up = 5898;
                down = 14336;
                break;
            case 12:
                up = 1843;
                down = 5017;
                break;
            case 13:
                up = 153;
                down = 2516;
                break;
            case 14:
                up = 51200;
                down = 102400;
                break;
            case 15:
                up = 11264;
                down = 43008;
                break;
            case 19:
                up = 51200;
                down = 102400;
                break;
        }
        result.setLinkUpstreamBandwidthKbps(up);
        result.setLinkDownstreamBandwidthKbps(down);
        result.setNetworkSpecifier(Integer.toString(this.mPhone.getSubId()));
        return result;
    }

    private static boolean isImsOrEmergencyApn(String[] apnTypes) {
        int i = 0;
        boolean isImsApn = true;
        if (apnTypes.length == 0) {
            return false;
        }
        int length = apnTypes.length;
        while (i < length) {
            String type = apnTypes[i];
            if (!ImsSwitchController.IMS_SERVICE.equals(type) && !"emergency".equals(type)) {
                isImsApn = false;
                break;
            }
            i++;
        }
        return isImsApn;
    }

    private boolean isIpAddress(String address) {
        if (address == null) {
            return false;
        }
        return Patterns.IP_ADDRESS.matcher(address).matches();
    }

    private SetupResult setLinkProperties(DataCallResponse response, LinkProperties lp) {
        String propertyPrefix = "net." + response.ifname + ".";
        String[] dnsServers = new String[2];
        dnsServers[0] = SystemProperties.get(propertyPrefix + "dns1");
        dnsServers[1] = SystemProperties.get(propertyPrefix + "dns2");
        return response.setLinkProperties(lp, isDnsOk(dnsServers));
    }

    private LinkProperties getLinkProperties() {
        if (this.mGlobalV6AddrInfo == null) {
            return this.mLinkProperties;
        }
        LinkProperties linkProperties = new LinkProperties(this.mLinkProperties);
        for (LinkAddress linkAddr : linkProperties.getLinkAddresses()) {
            if (linkAddr.getAddress() instanceof Inet6Address) {
                linkProperties.removeLinkAddress(linkAddr);
                break;
            }
        }
        linkProperties.addLinkAddress(this.mGlobalV6AddrInfo.mLinkAddr);
        return linkProperties;
    }

    private boolean initConnection(ConnectionParams cp) {
        ApnContext apnContext = cp.mApnContext;
        if (this.mApnSetting == null) {
            this.mApnSetting = apnContext.getApnSetting();
        }
        if (this.mApnSetting == null || !this.mApnSetting.canHandleType(apnContext.getApnType())) {
            log("initConnection: incompatible apnSetting in ConnectionParams cp=" + cp + " dc=" + this);
            return false;
        }
        this.mTag++;
        this.mConnectionParams = cp;
        this.mConnectionParams.mTag = this.mTag;
        if (!this.mApnContexts.containsKey(apnContext)) {
            checkIfDefaultApnReferenceCountChanged();
        }
        this.mApnContexts.put(apnContext, cp);
        log("initConnection:  RefCount=" + this.mApnContexts.size() + " mApnList=" + this.mApnContexts + " mConnectionParams=" + this.mConnectionParams);
        return true;
    }

    private boolean isNwNeedSuspended() {
        boolean bImsOrEmergencyApn = isImsOrEmergencyApn(getApnType());
        boolean bWifiCallingEnabled = this.mIsInVoiceCall ? DataConnectionHelper.getInstance().isWifiCallingEnabled() : false;
        log("isNwNeedSuspended: mIsInVoiceCall = " + this.mIsInVoiceCall + ", mIsSupportConcurrent = " + this.mIsSupportConcurrent + ", bImsOrEmergencyApn = " + bImsOrEmergencyApn + ", bWifiCallingEnabled = " + bWifiCallingEnabled);
        if (!this.mIsInVoiceCall || this.mIsSupportConcurrent || bImsOrEmergencyApn || bWifiCallingEnabled) {
            return false;
        }
        return true;
    }

    private boolean updateNetworkInfoSuspendState() {
        boolean z = true;
        DetailedState oldState = this.mNetworkInfo.getDetailedState();
        int currentDataConnectionState = this.mPhone.getServiceStateTracker().getCurrentDataConnectionState();
        boolean bNwNeedSuspended = isNwNeedSuspended();
        log("updateNetworkInfoSuspendState: oldState = " + oldState + ", currentDataConnectionState = " + currentDataConnectionState + ", bNwNeedSuspended = " + bNwNeedSuspended);
        if (this.mNetworkAgent == null) {
            Rlog.e(getName(), "Setting suspend state without a NetworkAgent");
        }
        if (currentDataConnectionState == 0 || isImsOrEmergencyApn(getApnType())) {
            if (bNwNeedSuspended) {
                this.mNetworkInfo.setDetailedState(DetailedState.SUSPENDED, null, this.mNetworkInfo.getExtraInfo());
                if (oldState == DetailedState.SUSPENDED) {
                    z = false;
                }
                return z;
            }
            this.mNetworkInfo.setDetailedState(DetailedState.CONNECTED, null, this.mNetworkInfo.getExtraInfo());
        } else if (!this.mIsInVoiceCall) {
            this.mNetworkInfo.setDetailedState(DetailedState.SUSPENDED, null, this.mNetworkInfo.getExtraInfo());
        }
        if (oldState == this.mNetworkInfo.getDetailedState()) {
            z = false;
        }
        return z;
    }

    void tearDownNow() {
        log("tearDownNow()");
        sendMessage(obtainMessage(EVENT_TEAR_DOWN_NOW));
    }

    private long getSuggestedRetryDelay(AsyncResult ar) {
        DataCallResponse response = ar.result;
        if (response.suggestedRetryTime < 0) {
            log("No suggested retry delay.");
            long delay = -2;
            DcFailCause cause = DcFailCause.fromInt(response.status);
            if (this.mDcFcMgr != null) {
                delay = this.mDcFcMgr.getSuggestedRetryDelayByOp(cause);
            }
            return delay;
        } else if (response.suggestedRetryTime != Integer.MAX_VALUE) {
            return (long) response.suggestedRetryTime;
        } else {
            log("Modem suggested not retrying.");
            return -1;
        }
    }

    protected String getWhatToString(int what) {
        return cmdToString(what);
    }

    private static String msgToString(Message msg) {
        if (msg == null) {
            return "null";
        }
        StringBuilder b = new StringBuilder();
        b.append("{what=");
        b.append(cmdToString(msg.what));
        b.append(" when=");
        TimeUtils.formatDuration(msg.getWhen() - SystemClock.uptimeMillis(), b);
        if (msg.arg1 != 0) {
            b.append(" arg1=");
            b.append(msg.arg1);
        }
        if (msg.arg2 != 0) {
            b.append(" arg2=");
            b.append(msg.arg2);
        }
        if (msg.obj != null) {
            b.append(" obj=");
            b.append(msg.obj);
        }
        b.append(" target=");
        b.append(msg.getTarget());
        b.append(" replyTo=");
        b.append(msg.replyTo);
        b.append("}");
        return b.toString();
    }

    static void slog(String s) {
        Rlog.d("DC", s);
    }

    protected void log(String s) {
        Rlog.d(getName(), s);
    }

    protected void logd(String s) {
        Rlog.d(getName(), s);
    }

    protected void logv(String s) {
        Rlog.v(getName(), s);
    }

    protected void logi(String s) {
        Rlog.i(getName(), s);
    }

    protected void logw(String s) {
        Rlog.w(getName(), s);
    }

    protected void loge(String s) {
        Rlog.e(getName(), s);
    }

    protected void loge(String s, Throwable e) {
        Rlog.e(getName(), s, e);
    }

    public String toStringSimple() {
        return getName() + ": State=" + getCurrentState().getName() + " mApnSetting=" + this.mApnSetting + " RefCount=" + this.mApnContexts.size() + " mCid=" + this.mCid + " mCreateTime=" + this.mCreateTime + " mLastastFailTime=" + this.mLastFailTime + " mLastFailCause=" + this.mLastFailCause + " mTag=" + this.mTag + " mLinkProperties=" + this.mLinkProperties + " linkCapabilities=" + makeNetworkCapabilities() + " mRestrictedNetworkOverride=" + this.mRestrictedNetworkOverride;
    }

    public String toString() {
        return "{" + toStringSimple() + " mApnContexts=" + this.mApnContexts + "}";
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
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    private void dumpToLog() {
        /*
        r4 = this;
        r3 = 0;
        r0 = new com.android.internal.telephony.dataconnection.DataConnection$3;
        r1 = new java.io.StringWriter;
        r2 = 0;
        r1.<init>(r2);
        r0.<init>(r4, r1);
        r4.dump(r3, r0, r3);
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.dataconnection.DataConnection.dumpToLog():void");
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.print("DataConnection ");
        super.dump(fd, pw, args);
        pw.println(" mApnContexts.size=" + this.mApnContexts.size());
        pw.println(" mApnContexts=" + this.mApnContexts);
        pw.flush();
        pw.println(" mDataConnectionTracker=" + this.mDct);
        pw.println(" mApnSetting=" + this.mApnSetting);
        pw.println(" mTag=" + this.mTag);
        pw.println(" mCid=" + this.mCid);
        pw.println(" mConnectionParams=" + this.mConnectionParams);
        pw.println(" mDisconnectParams=" + this.mDisconnectParams);
        pw.println(" mDcFailCause=" + this.mDcFailCause);
        pw.flush();
        pw.println(" mPhone=" + this.mPhone);
        pw.flush();
        pw.println(" mLinkProperties=" + this.mLinkProperties);
        pw.flush();
        pw.println(" mDataRegState=" + this.mDataRegState);
        pw.println(" mRilRat=" + this.mRilRat);
        pw.println(" mNetworkCapabilities=" + makeNetworkCapabilities());
        pw.println(" mCreateTime=" + TimeUtils.logTimeOfDay(this.mCreateTime));
        pw.println(" mLastFailTime=" + TimeUtils.logTimeOfDay(this.mLastFailTime));
        pw.println(" mLastFailCause=" + this.mLastFailCause);
        pw.flush();
        pw.println(" mUserData=" + this.mUserData);
        pw.println(" mInstanceNumber=" + mInstanceNumber);
        pw.println(" mAc=" + this.mAc);
        pw.flush();
    }

    boolean isApnTypeImsOrEmergency(String apnType) {
        if (TextUtils.equals(apnType, ImsSwitchController.IMS_SERVICE) || TextUtils.equals(apnType, "emergency")) {
            return true;
        }
        return false;
    }

    private int getEventByAddress(boolean bUpdated, LinkAddress linkAddr) {
        InetAddress addr = linkAddr.getAddress();
        if (bUpdated) {
            if (addr instanceof Inet6Address) {
                return EVENT_IPV6_ADDRESS_UPDATED;
            }
            loge("unknown address type, linkAddr: " + linkAddr);
            return -1;
        } else if (addr instanceof Inet6Address) {
            return EVENT_IPV6_ADDRESS_REMOVED;
        } else {
            if (addr instanceof Inet4Address) {
                return EVENT_IPV4_ADDRESS_REMOVED;
            }
            loge("unknown address type, linkAddr: " + linkAddr);
            return -1;
        }
    }

    private void sendMessageForSM(int event, String iface, LinkAddress address) {
        if (event < 0) {
            loge("sendMessageForSM: Skip notify!!!");
            return;
        }
        AddressInfo addrInfo = new AddressInfo(this, iface, address);
        log("sendMessageForSM: " + cmdToString(event) + ", addressInfo: " + addrInfo);
        sendMessage(obtainMessage(event, addrInfo));
    }

    private void onAddressRemoved() {
        if (("IPV6".equals(this.mApnSetting.protocol) || "IPV4V6".equals(this.mApnSetting.protocol)) && !isIpv4Connected()) {
            log("onAddressRemoved: IPv6 RA failed and didn't connect with IPv4");
            if (this.mApnContexts != null) {
                log("onAddressRemoved: mApnContexts size: " + this.mApnContexts.size());
                for (ConnectionParams cp : this.mApnContexts.values()) {
                    ApnContext apnContext = cp.mApnContext;
                    String apnType = apnContext.getApnType();
                    if (apnContext.getState() == DctConstants.State.CONNECTED) {
                        log("onAddressRemoved: send message EVENT_DISCONNECT_ALL");
                        sendMessage(obtainMessage(EVENT_DISCONNECT_ALL, new DisconnectParams(apnContext, PhoneInternalInterface.REASON_RA_FAILED, this.mDct.obtainMessage(270351, new Pair(apnContext, Integer.valueOf(cp.mConnectionGeneration))))));
                        return;
                    }
                }
                return;
            }
            return;
        }
        log("onAddressRemoved: no need to remove");
    }

    void checkIfDefaultApnReferenceCountChanged() {
        boolean IsDefaultExisted = false;
        int sizeOfOthers = 0;
        for (ConnectionParams cp : this.mApnContexts.values()) {
            ApnContext apnContext = cp.mApnContext;
            if (TextUtils.equals("default", apnContext.getApnType()) && DctConstants.State.CONNECTED.equals(apnContext.getState())) {
                IsDefaultExisted = true;
            } else if (DctConstants.State.CONNECTED.equals(apnContext.getState())) {
                sizeOfOthers++;
            }
        }
        if (IsDefaultExisted) {
            log("refCount = " + this.mApnContexts.size() + ", non-default refCount = " + sizeOfOthers);
            notifyDefaultApnReferenceCountChanged(sizeOfOthers + 1, 270846);
        }
    }

    private boolean isDefaultDataSubPhone(Phone phone) {
        int defaultDataPhoneId = this.mSubController.getPhoneId(this.mSubController.getDefaultDataSubId());
        int curPhoneId = phone.getPhoneId();
        if (defaultDataPhoneId == curPhoneId) {
            return true;
        }
        log("Current phone is not default phone: curPhoneId = " + curPhoneId + ", defaultDataPhoneId = " + defaultDataPhoneId);
        return false;
    }

    private void registerNetworkAlertObserver() {
        if (this.mNetworkManager != null) {
            log("registerNetworkAlertObserver X");
            try {
                this.mNetworkManager.registerObserver(this.mAlertObserver);
                log("registerNetworkAlertObserver E");
            } catch (RemoteException e) {
                loge("registerNetworkAlertObserver failed E");
            }
        }
    }

    private void unregisterNetworkAlertObserver() {
        if (this.mNetworkManager != null) {
            log("unregisterNetworkAlertObserver X");
            try {
                this.mNetworkManager.unregisterObserver(this.mAlertObserver);
                log("unregisterNetworkAlertObserver E");
            } catch (RemoteException e) {
                loge("unregisterNetworkAlertObserver failed E");
            }
            this.mInterfaceName = null;
        }
    }

    private boolean isVsimActive() {
        return this.mDct.isVsimActive(this.mPhone.getPhoneId());
    }

    public void startRetryAlarm(int what, int tag, long delay) {
        Intent intent = new Intent(this.mActionRetry);
        intent.putExtra(INTENT_RETRY_ALARM_WHAT, what);
        intent.putExtra(INTENT_RETRY_ALARM_TAG, tag);
        log("startRetryAlarm: next attempt in " + (delay / 1000) + "s" + " what=" + what + " tag=" + tag);
        this.mAlarmManager.set(2, SystemClock.elapsedRealtime() + delay, PendingIntent.getBroadcast(this.mPhone.getContext(), 0, intent, 134217728));
    }

    public void startRetryAlarmExact(int what, int tag, long delay) {
        Intent intent = new Intent(this.mActionRetry);
        intent.addFlags(268435456);
        intent.putExtra(INTENT_RETRY_ALARM_WHAT, what);
        intent.putExtra(INTENT_RETRY_ALARM_TAG, tag);
        log("startRetryAlarmExact: next attempt in " + (delay / 1000) + "s" + " what=" + what + " tag=" + tag);
        this.mAlarmManager.setExact(2, SystemClock.elapsedRealtime() + delay, PendingIntent.getBroadcast(this.mPhone.getContext(), 0, intent, 134217728));
    }

    public void resetRetryCount() {
        this.mRetryCount = 0;
        log("resetRetryCount: " + this.mRetryCount);
    }

    public void handlePcscfErrorCause(DisconnectParams dp) {
        CarrierConfigManager configMgr = (CarrierConfigManager) this.mPhone.getContext().getSystemService("carrier_config");
        int subId = this.mPhone.getSubId();
        if (configMgr == null) {
            loge("handlePcscfErrorCause() null configMgr!");
            return;
        }
        PersistableBundle b = configMgr.getConfigForSubId(subId);
        if (b == null) {
            loge("handlePcscfErrorCause() null config!");
            return;
        }
        boolean syncFailCause = b.getBoolean("ims_pdn_sync_fail_cause_to_modem_bool");
        log("handlePcscfErrorCause() syncFailCause: " + syncFailCause + " mPcscfAddr: " + this.mPcscfAddr + " with subId: " + subId);
        if (!syncFailCause || !TextUtils.equals(dp.mApnContext.getApnType(), ImsSwitchController.IMS_SERVICE)) {
            return;
        }
        if (this.mPcscfAddr == null || this.mPcscfAddr.length <= 0) {
            dp.mReason = PhoneInternalInterface.REASON_PCSCF_ADDRESS_FAILED;
            log("DcActiveState: Disconnect with empty P-CSCF address");
        }
    }
}
