package com.android.internal.telephony.cat;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources.NotFoundException;
import android.database.ContentObserver;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.SystemProperties;
import android.provider.Settings.Global;
import android.provider.Settings.SettingNotFoundException;
import android.provider.Settings.System;
import android.provider.oppo.Telephony.SimInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.CommandsInterface.RadioState;
import com.android.internal.telephony.cat.AppInterface.CommandType;
import com.android.internal.telephony.cat.Duration.TimeUnit;
import com.android.internal.telephony.uicc.IccCardStatus.CardState;
import com.android.internal.telephony.uicc.IccFileHandler;
import com.android.internal.telephony.uicc.IccRecords;
import com.android.internal.telephony.uicc.IccRefreshResponse;
import com.android.internal.telephony.uicc.IccUtils;
import com.android.internal.telephony.uicc.UiccCard;
import com.android.internal.telephony.uicc.UiccCardApplication;
import com.android.internal.telephony.uicc.UiccController;
import com.mediatek.internal.telephony.uicc.UsimPBMemInfo;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

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
public class CatService extends Handler implements AppInterface {
    /* renamed from: -com-android-internal-telephony-cat-AppInterface$CommandTypeSwitchesValues */
    private static final /* synthetic */ int[] f28x72eb89a2 = null;
    /* renamed from: -com-android-internal-telephony-cat-ResultCodeSwitchesValues */
    private static final /* synthetic */ int[] f29-com-android-internal-telephony-cat-ResultCodeSwitchesValues = null;
    static final String ACTION_PREBOOT_IPO = "android.intent.action.ACTION_PREBOOT_IPO";
    static final String ACTION_SHUTDOWN_IPO = "android.intent.action.ACTION_SHUTDOWN_IPO";
    private static final boolean DBG = true;
    private static final int DEV_ID_DISPLAY = 2;
    private static final int DEV_ID_EARPIECE = 3;
    private static final int DEV_ID_KEYPAD = 1;
    private static final int DEV_ID_NETWORK = 131;
    private static final int DEV_ID_TERMINAL = 130;
    private static final int DEV_ID_UICC = 129;
    static final String DISPLAY_TEXT_DISABLE_PROPERTY = "persist.service.cat.dt.disable";
    static final int EVENT_LIST_ELEMENT_BROWSER_TERMINATION = 8;
    static final int EVENT_LIST_ELEMENT_CALL_CONNECTED = 1;
    static final int EVENT_LIST_ELEMENT_CALL_DISCONNECTED = 2;
    static final int EVENT_LIST_ELEMENT_CARD_READER_STATUS = 6;
    static final int EVENT_LIST_ELEMENT_IDLE_SCREEN_AVAILABLE = 5;
    static final int EVENT_LIST_ELEMENT_LANGUAGE_SELECTION = 7;
    static final int EVENT_LIST_ELEMENT_LOCATION_STATUS = 3;
    static final int EVENT_LIST_ELEMENT_MT_CALL = 0;
    static final int EVENT_LIST_ELEMENT_USER_ACTIVITY = 4;
    static final String IDLE_SCREEN_ENABLE_KEY = "_enable";
    static final String IDLE_SCREEN_INTENT_NAME = "android.intent.action.IDLE_SCREEN_NEEDED";
    protected static final int MSG_ID_ALPHA_NOTIFY = 9;
    public static final int MSG_ID_CACHED_DISPLAY_TEXT_TIMEOUT = 46;
    private static final int MSG_ID_CALL_CTRL = 25;
    protected static final int MSG_ID_CALL_SETUP = 4;
    public static final int MSG_ID_CONN_RETRY_TIMEOUT = 47;
    static final int MSG_ID_DB_HANDLER = 12;
    private static final int MSG_ID_DISABLE_DISPLAY_TEXT_DELAYED = 15;
    private static final int MSG_ID_EVDL_CALL = 21;
    static final int MSG_ID_EVENT_DOWNLOAD = 11;
    protected static final int MSG_ID_EVENT_NOTIFY = 3;
    protected static final int MSG_ID_ICC_CHANGED = 8;
    private static final int MSG_ID_ICC_RECORDS_LOADED = 20;
    private static final int MSG_ID_ICC_REFRESH = 30;
    private static final int MSG_ID_IVSR_DELAYED = 14;
    static final int MSG_ID_LAUNCH_DB_SETUP_MENU = 13;
    private static final int MSG_ID_MODEM_EVDL_CALL_CONN_TIMEOUT = 22;
    private static final int MSG_ID_MODEM_EVDL_CALL_DISCONN_TIMEOUT = 23;
    protected static final int MSG_ID_PROACTIVE_COMMAND = 2;
    static final int MSG_ID_REFRESH = 5;
    static final int MSG_ID_RESPONSE = 6;
    static final int MSG_ID_RIL_MSG_DECODED = 10;
    protected static final int MSG_ID_SESSION_END = 1;
    private static final int MSG_ID_SETUP_MENU_RESET = 24;
    static final int MSG_ID_SIM_READY = 7;
    static final String STK_DEFAULT = "Default Message";
    private static final int STK_EVDL_CALL_STATE_CALLCONN = 0;
    private static final int STK_EVDL_CALL_STATE_CALLDISCONN = 1;
    static final String USER_ACTIVITY_ENABLE_KEY = "state";
    static final String USER_ACTIVITY_INTENT_NAME = "android.intent.action.stk.USER_ACTIVITY.enable";
    private static final String mEsnTrackUtkMenuSelect = "com.android.internal.telephony.cat.ESN_MENU_SELECTION";
    private static IccRecords mIccRecords;
    private static boolean mIsCatServiceDisposed;
    protected static Object mLock;
    private static UiccCardApplication mUiccApplication;
    private static String[] sInstKey;
    private static CatService[] sInstance;
    private static final Object sInstanceLock = null;
    private int CACHED_DISPLAY_TIMEOUT;
    private BroadcastReceiver CatServiceReceiver;
    private final int DISABLE_DISPLAY_TEXT_DELAYED_TIME;
    private final int IVSR_DELAYED_TIME;
    private final int LTE_DC_PHONE_PROXY_ID;
    private int MODEM_EVDL_TIMEOUT;
    private boolean default_send_setupmenu_tr;
    private boolean isDisplayTextDisabled;
    private boolean isIvsrBootUp;
    private BipService mBipService;
    private CatCmdMessage mCachedDisplayTextCmd;
    private CardState mCardState;
    private final BroadcastReceiver mClearDisplayTextReceiver;
    private CommandsInterface mCmdIf;
    private CatCmdMessage mCmdMessage;
    private Context mContext;
    private CatCmdMessage mCurrentCmd;
    private LinkedList<Integer> mEvdlCallConnObjQ;
    private LinkedList<Integer> mEvdlCallDisConnObjQ;
    private int mEvdlCallObj;
    private LinkedList<EventDownloadCallInfo> mEventDownloadCallConnInfo;
    private LinkedList<EventDownloadCallInfo> mEventDownloadCallDisConnInfo;
    private byte[] mEventList;
    public boolean mGotSetUpMenu;
    private HandlerThread mHandlerThread;
    private boolean mHasCachedDTCmd;
    private boolean mIsAllCallDisConn;
    private boolean mIsProactiveCmdResponsed;
    private CatCmdMessage mMenuCmd;
    private RilMessageDecoder mMsgDecoder;
    boolean mNeedRegisterAgain;
    private int mNumEventDownloadCallConn;
    private int mNumEventDownloadCallDisConn;
    private ContentObserver mPowerOnSequenceObserver;
    private boolean mReadFromPreferenceDone;
    public boolean mSaveNewSetUpMenu;
    private boolean mSetUpMenuFromMD;
    private int mSlotId;
    private boolean mStkAppInstalled;
    private final BroadcastReceiver mStkIdleScreenAvailableReceiver;
    Handler mTimeoutHandler;
    private UiccController mUiccController;
    private int simIdfromIntent;
    private String simState;

    /* renamed from: com.android.internal.telephony.cat.CatService$1 */
    class AnonymousClass1 extends Handler {
        final /* synthetic */ CatService this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.cat.CatService.1.<init>(com.android.internal.telephony.cat.CatService):void, dex: 
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
        AnonymousClass1(com.android.internal.telephony.cat.CatService r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.cat.CatService.1.<init>(com.android.internal.telephony.cat.CatService):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.cat.CatService.1.<init>(com.android.internal.telephony.cat.CatService):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.cat.CatService.1.handleMessage(android.os.Message):void, dex: 
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
        public void handleMessage(android.os.Message r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.cat.CatService.1.handleMessage(android.os.Message):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.cat.CatService.1.handleMessage(android.os.Message):void");
        }
    }

    /* renamed from: com.android.internal.telephony.cat.CatService$2 */
    class AnonymousClass2 extends BroadcastReceiver {
        final /* synthetic */ CatService this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.cat.CatService.2.<init>(com.android.internal.telephony.cat.CatService):void, dex: 
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
        AnonymousClass2(com.android.internal.telephony.cat.CatService r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.cat.CatService.2.<init>(com.android.internal.telephony.cat.CatService):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.cat.CatService.2.<init>(com.android.internal.telephony.cat.CatService):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.cat.CatService.2.onReceive(android.content.Context, android.content.Intent):void, dex: 
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
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.cat.CatService.2.onReceive(android.content.Context, android.content.Intent):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.cat.CatService.2.onReceive(android.content.Context, android.content.Intent):void");
        }
    }

    /* renamed from: com.android.internal.telephony.cat.CatService$3 */
    class AnonymousClass3 extends BroadcastReceiver {
        final /* synthetic */ CatService this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.cat.CatService.3.<init>(com.android.internal.telephony.cat.CatService):void, dex: 
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
        AnonymousClass3(com.android.internal.telephony.cat.CatService r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.cat.CatService.3.<init>(com.android.internal.telephony.cat.CatService):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.cat.CatService.3.<init>(com.android.internal.telephony.cat.CatService):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.cat.CatService.3.onReceive(android.content.Context, android.content.Intent):void, dex: 
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
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.cat.CatService.3.onReceive(android.content.Context, android.content.Intent):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.cat.CatService.3.onReceive(android.content.Context, android.content.Intent):void");
        }
    }

    /* renamed from: com.android.internal.telephony.cat.CatService$4 */
    class AnonymousClass4 extends BroadcastReceiver {
        public static final String EXTRA_VALUE_REMOVE_SIM = "REMOVE";
        public static final String INTENT_KEY_DETECT_STATUS = "simDetectStatus";
        final /* synthetic */ CatService this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.cat.CatService.4.<init>(com.android.internal.telephony.cat.CatService):void, dex: 
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
        AnonymousClass4(com.android.internal.telephony.cat.CatService r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.cat.CatService.4.<init>(com.android.internal.telephony.cat.CatService):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.cat.CatService.4.<init>(com.android.internal.telephony.cat.CatService):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.cat.CatService.4.onReceive(android.content.Context, android.content.Intent):void, dex: 
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
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.cat.CatService.4.onReceive(android.content.Context, android.content.Intent):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.cat.CatService.4.onReceive(android.content.Context, android.content.Intent):void");
        }
    }

    /* renamed from: com.android.internal.telephony.cat.CatService$5 */
    class AnonymousClass5 extends ContentObserver {
        final /* synthetic */ CatService this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.cat.CatService.5.<init>(com.android.internal.telephony.cat.CatService, android.os.Handler):void, dex: 
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
        AnonymousClass5(com.android.internal.telephony.cat.CatService r1, android.os.Handler r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.cat.CatService.5.<init>(com.android.internal.telephony.cat.CatService, android.os.Handler):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.cat.CatService.5.<init>(com.android.internal.telephony.cat.CatService, android.os.Handler):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.cat.CatService.5.onChange(boolean):void, dex: 
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
        public void onChange(boolean r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.cat.CatService.5.onChange(boolean):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.cat.CatService.5.onChange(boolean):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.cat.CatService.5.onChange(boolean, android.net.Uri):void, dex: 
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
        public void onChange(boolean r1, android.net.Uri r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.cat.CatService.5.onChange(boolean, android.net.Uri):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.cat.CatService.5.onChange(boolean, android.net.Uri):void");
        }
    }

    /* renamed from: -getcom-android-internal-telephony-cat-AppInterface$CommandTypeSwitchesValues */
    private static /* synthetic */ int[] m116xe796fd46() {
        if (f28x72eb89a2 != null) {
            return f28x72eb89a2;
        }
        int[] iArr = new int[CommandType.values().length];
        try {
            iArr[CommandType.ACTIVATE.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[CommandType.CALLCTRL_RSP_MSG.ordinal()] = 41;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[CommandType.CLOSE_CHANNEL.ordinal()] = 2;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[CommandType.DECLARE_SERVICE.ordinal()] = 42;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[CommandType.DISPLAY_MULTIMEDIA_MESSAGE.ordinal()] = 43;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[CommandType.DISPLAY_TEXT.ordinal()] = 3;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[CommandType.GET_CHANNEL_STATUS.ordinal()] = 44;
        } catch (NoSuchFieldError e7) {
        }
        try {
            iArr[CommandType.GET_FRAME_STATUS.ordinal()] = 45;
        } catch (NoSuchFieldError e8) {
        }
        try {
            iArr[CommandType.GET_INKEY.ordinal()] = 4;
        } catch (NoSuchFieldError e9) {
        }
        try {
            iArr[CommandType.GET_INPUT.ordinal()] = 5;
        } catch (NoSuchFieldError e10) {
        }
        try {
            iArr[CommandType.GET_READER_STATUS.ordinal()] = 46;
        } catch (NoSuchFieldError e11) {
        }
        try {
            iArr[CommandType.GET_SERVICE_INFORMATION.ordinal()] = 47;
        } catch (NoSuchFieldError e12) {
        }
        try {
            iArr[CommandType.LANGUAGE_NOTIFICATION.ordinal()] = 48;
        } catch (NoSuchFieldError e13) {
        }
        try {
            iArr[CommandType.LAUNCH_BROWSER.ordinal()] = 6;
        } catch (NoSuchFieldError e14) {
        }
        try {
            iArr[CommandType.MORE_TIME.ordinal()] = 49;
        } catch (NoSuchFieldError e15) {
        }
        try {
            iArr[CommandType.OPEN_CHANNEL.ordinal()] = 7;
        } catch (NoSuchFieldError e16) {
        }
        try {
            iArr[CommandType.PERFORM_CARD_APDU.ordinal()] = 50;
        } catch (NoSuchFieldError e17) {
        }
        try {
            iArr[CommandType.PLAY_TONE.ordinal()] = 8;
        } catch (NoSuchFieldError e18) {
        }
        try {
            iArr[CommandType.POLLING_OFF.ordinal()] = 51;
        } catch (NoSuchFieldError e19) {
        }
        try {
            iArr[CommandType.POLL_INTERVAL.ordinal()] = 52;
        } catch (NoSuchFieldError e20) {
        }
        try {
            iArr[CommandType.POWER_OFF_CARD.ordinal()] = 53;
        } catch (NoSuchFieldError e21) {
        }
        try {
            iArr[CommandType.POWER_ON_CARD.ordinal()] = 54;
        } catch (NoSuchFieldError e22) {
        }
        try {
            iArr[CommandType.PROVIDE_LOCAL_INFORMATION.ordinal()] = 9;
        } catch (NoSuchFieldError e23) {
        }
        try {
            iArr[CommandType.RECEIVE_DATA.ordinal()] = 10;
        } catch (NoSuchFieldError e24) {
        }
        try {
            iArr[CommandType.REFRESH.ordinal()] = 11;
        } catch (NoSuchFieldError e25) {
        }
        try {
            iArr[CommandType.RETRIEVE_MULTIMEDIA_MESSAGE.ordinal()] = 55;
        } catch (NoSuchFieldError e26) {
        }
        try {
            iArr[CommandType.RUN_AT_COMMAND.ordinal()] = 56;
        } catch (NoSuchFieldError e27) {
        }
        try {
            iArr[CommandType.SELECT_ITEM.ordinal()] = 12;
        } catch (NoSuchFieldError e28) {
        }
        try {
            iArr[CommandType.SEND_DATA.ordinal()] = 13;
        } catch (NoSuchFieldError e29) {
        }
        try {
            iArr[CommandType.SEND_DTMF.ordinal()] = 14;
        } catch (NoSuchFieldError e30) {
        }
        try {
            iArr[CommandType.SEND_SMS.ordinal()] = 15;
        } catch (NoSuchFieldError e31) {
        }
        try {
            iArr[CommandType.SEND_SS.ordinal()] = 16;
        } catch (NoSuchFieldError e32) {
        }
        try {
            iArr[CommandType.SEND_USSD.ordinal()] = 17;
        } catch (NoSuchFieldError e33) {
        }
        try {
            iArr[CommandType.SERVICE_SEARCH.ordinal()] = 57;
        } catch (NoSuchFieldError e34) {
        }
        try {
            iArr[CommandType.SET_FRAME.ordinal()] = 58;
        } catch (NoSuchFieldError e35) {
        }
        try {
            iArr[CommandType.SET_UP_CALL.ordinal()] = 18;
        } catch (NoSuchFieldError e36) {
        }
        try {
            iArr[CommandType.SET_UP_EVENT_LIST.ordinal()] = 19;
        } catch (NoSuchFieldError e37) {
        }
        try {
            iArr[CommandType.SET_UP_IDLE_MODE_TEXT.ordinal()] = 20;
        } catch (NoSuchFieldError e38) {
        }
        try {
            iArr[CommandType.SET_UP_MENU.ordinal()] = 21;
        } catch (NoSuchFieldError e39) {
        }
        try {
            iArr[CommandType.SUBMIT_MULTIMEDIA_MESSAGE.ordinal()] = 59;
        } catch (NoSuchFieldError e40) {
        }
        try {
            iArr[CommandType.TIMER_MANAGEMENT.ordinal()] = 60;
        } catch (NoSuchFieldError e41) {
        }
        f28x72eb89a2 = iArr;
        return iArr;
    }

    /* renamed from: -getcom-android-internal-telephony-cat-ResultCodeSwitchesValues */
    private static /* synthetic */ int[] m117-getcom-android-internal-telephony-cat-ResultCodeSwitchesValues() {
        if (f29-com-android-internal-telephony-cat-ResultCodeSwitchesValues != null) {
            return f29-com-android-internal-telephony-cat-ResultCodeSwitchesValues;
        }
        int[] iArr = new int[ResultCode.values().length];
        try {
            iArr[ResultCode.ACCESS_TECH_UNABLE_TO_PROCESS.ordinal()] = 41;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[ResultCode.BACKWARD_MOVE_BY_USER.ordinal()] = 1;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[ResultCode.BEYOND_TERMINAL_CAPABILITY.ordinal()] = 42;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[ResultCode.BIP_ERROR.ordinal()] = 43;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[ResultCode.CMD_DATA_NOT_UNDERSTOOD.ordinal()] = 2;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[ResultCode.CMD_NUM_NOT_KNOWN.ordinal()] = 44;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[ResultCode.CMD_TYPE_NOT_UNDERSTOOD.ordinal()] = 45;
        } catch (NoSuchFieldError e7) {
        }
        try {
            iArr[ResultCode.CONTRADICTION_WITH_TIMER.ordinal()] = 46;
        } catch (NoSuchFieldError e8) {
        }
        try {
            iArr[ResultCode.FRAMES_ERROR.ordinal()] = 47;
        } catch (NoSuchFieldError e9) {
        }
        try {
            iArr[ResultCode.HELP_INFO_REQUIRED.ordinal()] = 3;
        } catch (NoSuchFieldError e10) {
        }
        try {
            iArr[ResultCode.LAUNCH_BROWSER_ERROR.ordinal()] = 4;
        } catch (NoSuchFieldError e11) {
        }
        try {
            iArr[ResultCode.MMS_ERROR.ordinal()] = 48;
        } catch (NoSuchFieldError e12) {
        }
        try {
            iArr[ResultCode.MMS_TEMPORARY.ordinal()] = 49;
        } catch (NoSuchFieldError e13) {
        }
        try {
            iArr[ResultCode.MULTI_CARDS_CMD_ERROR.ordinal()] = 50;
        } catch (NoSuchFieldError e14) {
        }
        try {
            iArr[ResultCode.NAA_CALL_CONTROL_TEMPORARY.ordinal()] = 51;
        } catch (NoSuchFieldError e15) {
        }
        try {
            iArr[ResultCode.NETWORK_CRNTLY_UNABLE_TO_PROCESS.ordinal()] = 5;
        } catch (NoSuchFieldError e16) {
        }
        try {
            iArr[ResultCode.NO_RESPONSE_FROM_USER.ordinal()] = 6;
        } catch (NoSuchFieldError e17) {
        }
        try {
            iArr[ResultCode.OK.ordinal()] = 7;
        } catch (NoSuchFieldError e18) {
        }
        try {
            iArr[ResultCode.PRFRMD_ICON_NOT_DISPLAYED.ordinal()] = 8;
        } catch (NoSuchFieldError e19) {
        }
        try {
            iArr[ResultCode.PRFRMD_LIMITED_SERVICE.ordinal()] = 9;
        } catch (NoSuchFieldError e20) {
        }
        try {
            iArr[ResultCode.PRFRMD_MODIFIED_BY_NAA.ordinal()] = 10;
        } catch (NoSuchFieldError e21) {
        }
        try {
            iArr[ResultCode.PRFRMD_NAA_NOT_ACTIVE.ordinal()] = 11;
        } catch (NoSuchFieldError e22) {
        }
        try {
            iArr[ResultCode.PRFRMD_TONE_NOT_PLAYED.ordinal()] = 12;
        } catch (NoSuchFieldError e23) {
        }
        try {
            iArr[ResultCode.PRFRMD_WITH_ADDITIONAL_EFS_READ.ordinal()] = 13;
        } catch (NoSuchFieldError e24) {
        }
        try {
            iArr[ResultCode.PRFRMD_WITH_MISSING_INFO.ordinal()] = 14;
        } catch (NoSuchFieldError e25) {
        }
        try {
            iArr[ResultCode.PRFRMD_WITH_MODIFICATION.ordinal()] = 15;
        } catch (NoSuchFieldError e26) {
        }
        try {
            iArr[ResultCode.PRFRMD_WITH_PARTIAL_COMPREHENSION.ordinal()] = 16;
        } catch (NoSuchFieldError e27) {
        }
        try {
            iArr[ResultCode.REQUIRED_VALUES_MISSING.ordinal()] = 52;
        } catch (NoSuchFieldError e28) {
        }
        try {
            iArr[ResultCode.SMS_RP_ERROR.ordinal()] = 53;
        } catch (NoSuchFieldError e29) {
        }
        try {
            iArr[ResultCode.SS_RETURN_ERROR.ordinal()] = 54;
        } catch (NoSuchFieldError e30) {
        }
        try {
            iArr[ResultCode.TERMINAL_CRNTLY_UNABLE_TO_PROCESS.ordinal()] = 17;
        } catch (NoSuchFieldError e31) {
        }
        try {
            iArr[ResultCode.UICC_SESSION_TERM_BY_USER.ordinal()] = 18;
        } catch (NoSuchFieldError e32) {
        }
        try {
            iArr[ResultCode.USER_CLEAR_DOWN_CALL.ordinal()] = 55;
        } catch (NoSuchFieldError e33) {
        }
        try {
            iArr[ResultCode.USER_NOT_ACCEPT.ordinal()] = 19;
        } catch (NoSuchFieldError e34) {
        }
        try {
            iArr[ResultCode.USIM_CALL_CONTROL_PERMANENT.ordinal()] = 56;
        } catch (NoSuchFieldError e35) {
        }
        try {
            iArr[ResultCode.USSD_RETURN_ERROR.ordinal()] = 57;
        } catch (NoSuchFieldError e36) {
        }
        try {
            iArr[ResultCode.USSD_SS_SESSION_TERM_BY_USER.ordinal()] = 58;
        } catch (NoSuchFieldError e37) {
        }
        f29-com-android-internal-telephony-cat-ResultCodeSwitchesValues = iArr;
        return iArr;
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.internal.telephony.cat.CatService.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.internal.telephony.cat.CatService.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.cat.CatService.<clinit>():void");
    }

    void cancelTimeOut(int msg) {
        CatLog.d((Object) this, "cancelTimeOut, sim_id: " + this.mSlotId + ", msg id: " + msg);
        this.mTimeoutHandler.removeMessages(msg);
    }

    void startTimeOut(int msg, long delay) {
        CatLog.d((Object) this, "startTimeOut, sim_id: " + this.mSlotId + ", msg id: " + msg);
        cancelTimeOut(msg);
        this.mTimeoutHandler.sendMessageDelayed(this.mTimeoutHandler.obtainMessage(msg), delay);
    }

    private void clearCachedDisplayText(int sim_id) {
        if (!SystemProperties.get("ro.mtk_bsp_package").equals("1")) {
            CatLog.d("CatService", "clearCachedDisplayText, sim_id: " + sim_id + ", mSlotId: " + this.mSlotId + ", mCachedDisplayTextCmd: " + (this.mCachedDisplayTextCmd != null ? 1 : 0));
            if (sim_id != this.mSlotId) {
                return;
            }
            if (this.mCachedDisplayTextCmd != null) {
                CatResponseMessage resMsg = new CatResponseMessage(this.mCachedDisplayTextCmd);
                resMsg.setResultCode(ResultCode.UICC_SESSION_TERM_BY_USER);
                handleCmdResponse(resMsg);
                this.mCachedDisplayTextCmd = null;
                unregisterPowerOnSequenceObserver();
            } else if (this.mHasCachedDTCmd) {
                unregisterPowerOnSequenceObserver();
                resetPowerOnSequenceFlag();
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
    private CatService(com.android.internal.telephony.CommandsInterface r8, com.android.internal.telephony.uicc.UiccCardApplication r9, com.android.internal.telephony.uicc.IccRecords r10, android.content.Context r11, com.android.internal.telephony.uicc.IccFileHandler r12, com.android.internal.telephony.uicc.UiccCard r13, int r14) {
        /*
        r7 = this;
        r7.<init>();
        r4 = 0;
        r7.mCurrentCmd = r4;
        r4 = 0;
        r7.mMenuCmd = r4;
        r4 = 0;
        r7.mMsgDecoder = r4;
        r4 = 0;
        r7.mStkAppInstalled = r4;
        r4 = com.android.internal.telephony.uicc.IccCardStatus.CardState.CARDSTATE_ABSENT;
        r7.mCardState = r4;
        r4 = 0;
        r7.mBipService = r4;
        r4 = 1;
        r7.default_send_setupmenu_tr = r4;
        r4 = 0;
        r7.mGotSetUpMenu = r4;
        r4 = 0;
        r7.mSaveNewSetUpMenu = r4;
        r4 = 0;
        r7.mSetUpMenuFromMD = r4;
        r4 = 0;
        r7.mReadFromPreferenceDone = r4;
        r4 = 2000; // 0x7d0 float:2.803E-42 double:9.88E-321;
        r7.MODEM_EVDL_TIMEOUT = r4;
        r4 = new java.util.LinkedList;
        r4.<init>();
        r7.mEvdlCallConnObjQ = r4;
        r4 = new java.util.LinkedList;
        r4.<init>();
        r7.mEvdlCallDisConnObjQ = r4;
        r4 = 0;
        r7.mEvdlCallObj = r4;
        r4 = 0;
        r7.simState = r4;
        r4 = 0;
        r7.simIdfromIntent = r4;
        r4 = 0;
        r7.mCachedDisplayTextCmd = r4;
        r4 = 0;
        r7.mHasCachedDTCmd = r4;
        r4 = 0;
        r7.isIvsrBootUp = r4;
        r4 = 60000; // 0xea60 float:8.4078E-41 double:2.9644E-319;
        r7.IVSR_DELAYED_TIME = r4;
        r4 = 0;
        r7.isDisplayTextDisabled = r4;
        r4 = 30000; // 0x7530 float:4.2039E-41 double:1.4822E-319;
        r7.DISABLE_DISPLAY_TEXT_DELAYED_TIME = r4;
        r4 = 0;
        r7.mNeedRegisterAgain = r4;
        r4 = new java.util.LinkedList;
        r4.<init>();
        r7.mEventDownloadCallDisConnInfo = r4;
        r4 = new java.util.LinkedList;
        r4.<init>();
        r7.mEventDownloadCallConnInfo = r4;
        r4 = 0;
        r7.mNumEventDownloadCallDisConn = r4;
        r4 = 0;
        r7.mNumEventDownloadCallConn = r4;
        r4 = 0;
        r7.mIsAllCallDisConn = r4;
        r4 = 0;
        r7.mIsProactiveCmdResponsed = r4;
        r4 = 120000; // 0x1d4c0 float:1.68156E-40 double:5.9288E-319;
        r7.CACHED_DISPLAY_TIMEOUT = r4;
        r4 = 0;
        r7.LTE_DC_PHONE_PROXY_ID = r4;
        r4 = new com.android.internal.telephony.cat.CatService$1;
        r4.<init>(r7);
        r7.mTimeoutHandler = r4;
        r4 = new com.android.internal.telephony.cat.CatService$2;
        r4.<init>(r7);
        r7.mStkIdleScreenAvailableReceiver = r4;
        r4 = new com.android.internal.telephony.cat.CatService$3;
        r4.<init>(r7);
        r7.mClearDisplayTextReceiver = r4;
        r4 = new com.android.internal.telephony.cat.CatService$4;
        r4.<init>(r7);
        r7.CatServiceReceiver = r4;
        r4 = new com.android.internal.telephony.cat.CatService$5;
        r4.<init>(r7, r7);
        r7.mPowerOnSequenceObserver = r4;
        r4 = 0;
        r7.mCmdMessage = r4;
        if (r8 == 0) goto L_0x00a4;
    L_0x00a2:
        if (r9 != 0) goto L_0x00ad;
    L_0x00a4:
        r4 = new java.lang.NullPointerException;
        r5 = "Service: Input parameters must not be null";
        r4.<init>(r5);
        throw r4;
    L_0x00ad:
        if (r10 == 0) goto L_0x00a4;
    L_0x00af:
        if (r11 == 0) goto L_0x00a4;
    L_0x00b1:
        if (r12 == 0) goto L_0x00a4;
    L_0x00b3:
        if (r13 == 0) goto L_0x00a4;
    L_0x00b5:
        r7.mCmdIf = r8;
        r7.mContext = r11;
        r7.mSlotId = r14;
        r4 = new android.os.HandlerThread;
        r5 = new java.lang.StringBuilder;
        r5.<init>();
        r6 = "Cat Telephony service";
        r5 = r5.append(r6);
        r5 = r5.append(r14);
        r5 = r5.toString();
        r4.<init>(r5);
        r7.mHandlerThread = r4;
        r4 = r7.mHandlerThread;
        r4.start();
        r4 = new java.lang.StringBuilder;
        r4.<init>();
        r5 = "slotId ";
        r4 = r4.append(r5);
        r4 = r4.append(r14);
        r4 = r4.toString();
        com.android.internal.telephony.cat.CatLog.d(r7, r4);
        r4 = com.android.internal.telephony.cat.RilMessageDecoder.getInstance(r7, r12, r14);
        r7.mMsgDecoder = r4;
        r4 = r7.mMsgDecoder;
        if (r4 != 0) goto L_0x0103;
    L_0x00fc:
        r4 = "Null RilMessageDecoder instance";
        com.android.internal.telephony.cat.CatLog.d(r7, r4);
        return;
    L_0x0103:
        r4 = r7.mMsgDecoder;
        r4.start();
        r4 = r7.mContext;
        r5 = r7.mSlotId;
        r6 = r7.mCmdIf;
        r4 = com.android.internal.telephony.cat.BipService.getInstance(r4, r7, r5, r6, r12);
        r7.mBipService = r4;
        r4 = r7.mCmdIf;
        r5 = 1;
        r6 = 0;
        r4.setOnCatSessionEnd(r7, r5, r6);
        r4 = r7.mCmdIf;
        r5 = 2;
        r6 = 0;
        r4.setOnCatProactiveCmd(r7, r5, r6);
        r4 = r7.mCmdIf;
        r5 = 3;
        r6 = 0;
        r4.setOnCatEvent(r7, r5, r6);
        r4 = r7.mCmdIf;
        r5 = 4;
        r6 = 0;
        r4.setOnCatCallSetUp(r7, r5, r6);
        r4 = "ro.mtk_bsp_package";
        r4 = android.os.SystemProperties.get(r4);
        r5 = "1";
        r4 = r4.equals(r5);
        if (r4 != 0) goto L_0x0148;
    L_0x0140:
        r4 = r7.mCmdIf;
        r5 = 21;
        r6 = 0;
        r4.setOnStkEvdlCall(r7, r5, r6);
    L_0x0148:
        r4 = r7.mCmdIf;
        r5 = 24;
        r6 = 0;
        r4.setOnStkSetupMenuReset(r7, r5, r6);
        r4 = r7.mCmdIf;
        r5 = 30;
        r6 = 0;
        r4.registerForIccRefresh(r7, r5, r6);
        r4 = r7.mCmdIf;
        r5 = 9;
        r6 = 0;
        r4.setOnCatCcAlphaNotify(r7, r5, r6);
        mIccRecords = r10;
        mUiccApplication = r9;
        r4 = mUiccApplication;
        r5 = 7;
        r6 = 0;
        r4.registerForReady(r7, r5, r6);
        r4 = mIccRecords;
        r5 = 20;
        r6 = 0;
        r4.registerForRecordsLoaded(r7, r5, r6);
        r4 = new java.lang.StringBuilder;
        r4.<init>();
        r5 = "registerForRecordsLoaded slotid=";
        r4 = r4.append(r5);
        r5 = r7.mSlotId;
        r4 = r4.append(r5);
        r5 = " instance:";
        r4 = r4.append(r5);
        r4 = r4.append(r7);
        r4 = r4.toString();
        com.android.internal.telephony.cat.CatLog.d(r7, r4);
        r1 = new android.content.IntentFilter;
        r4 = "android.intent.action.ACTION_SHUTDOWN_IPO";
        r1.<init>(r4);
        r4 = "mediatek.intent.action.IVSR_NOTIFY";
        r1.addAction(r4);
        r4 = "com.android.phone.ACTION_SIM_RECOVERY_DONE";
        r1.addAction(r4);
        r4 = "android.intent.action.ACTION_MD_TYPE_CHANGE";
        r1.addAction(r4);
        r3 = new android.content.IntentFilter;
        r4 = "android.intent.action.SIM_STATE_CHANGED";
        r3.<init>(r4);
        r4 = "android.intent.action.RADIO_TECHNOLOGY";
        r3.addAction(r4);
        r4 = r7.mContext;
        r5 = r7.CatServiceReceiver;
        r4.registerReceiver(r5, r1);
        r4 = r7.mContext;
        r5 = r7.CatServiceReceiver;
        r4.registerReceiver(r5, r3);
        r2 = new android.content.IntentFilter;
        r4 = "android.intent.action.stk.IDLE_SCREEN_AVAILABLE";
        r2.<init>(r4);
        r4 = r7.mContext;
        r5 = r7.mStkIdleScreenAvailableReceiver;
        r4.registerReceiver(r5, r2);
        r4 = "CatService: is running";
        com.android.internal.telephony.cat.CatLog.d(r7, r4);
        r4 = com.android.internal.telephony.uicc.UiccController.getInstance();
        r7.mUiccController = r4;
        r4 = r7.mUiccController;
        r5 = 8;
        r6 = 0;
        r4.registerForIccChanged(r7, r5, r6);
        r4 = r7.isStkAppInstalled();
        r7.mStkAppInstalled = r4;
        r4 = "ro.mtk_bsp_package";
        r4 = android.os.SystemProperties.get(r4);
        r5 = "1";
        r4 = r4.equals(r5);
        if (r4 != 0) goto L_0x021c;
    L_0x0206:
        r4 = r7.mHasCachedDTCmd;
        if (r4 == 0) goto L_0x021c;
    L_0x020a:
        r7.registerPowerOnSequenceObserver();
        r0 = new android.content.IntentFilter;
        r4 = "android.intent.action.stk.clear_display_text";
        r0.<init>(r4);
        r4 = r7.mContext;
        r5 = r7.mClearDisplayTextReceiver;
        r4.registerReceiver(r5, r0);
    L_0x021c:
        r4 = new java.lang.StringBuilder;
        r4.<init>();
        r5 = "Running CAT service on Slotid: ";
        r4 = r4.append(r5);
        r5 = r7.mSlotId;
        r4 = r4.append(r5);
        r5 = ". STK app installed:";
        r4 = r4.append(r5);
        r5 = r7.mStkAppInstalled;
        r4 = r4.append(r5);
        r4 = r4.toString();
        com.android.internal.telephony.cat.CatLog.d(r7, r4);
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.cat.CatService.<init>(com.android.internal.telephony.CommandsInterface, com.android.internal.telephony.uicc.UiccCardApplication, com.android.internal.telephony.uicc.IccRecords, android.content.Context, com.android.internal.telephony.uicc.IccFileHandler, com.android.internal.telephony.uicc.UiccCard, int):void");
    }

    public static CatService getInstance(CommandsInterface ci, Context context, UiccCard ic, int slotId) {
        UiccCardApplication ca = null;
        IccFileHandler fh = null;
        IccRecords ir = null;
        if (ic != null) {
            int phoneType = 1;
            int[] subId = SubscriptionManager.getSubId(slotId);
            if (subId != null) {
                phoneType = TelephonyManager.getDefault().getCurrentPhoneType(subId[0]);
                CatLog.d("CatService", "getInstance phoneType : " + phoneType + "slotid: " + slotId + "subId[0]:" + subId[0]);
            }
            if (phoneType == 2) {
                ca = ic.getApplication(2);
            } else {
                ca = ic.getApplicationIndex(0);
            }
            if (ca != null) {
                fh = ca.getIccFileHandler();
                ir = ca.getIccRecords();
            }
        }
        CatLog.d("CatService", "call getInstance 1");
        synchronized (sInstanceLock) {
            if (sInstance == null) {
                int simCount = TelephonyManager.getDefault().getSimCount();
                sInstance = new CatService[simCount];
                for (int i = 0; i < simCount; i++) {
                    sInstance[i] = null;
                }
            }
            if (sInstance[slotId] != null) {
                if (ir != null) {
                    if (mIccRecords != ir) {
                        CatLog.d("CatService", "Reinitialize the Service with SIMRecords");
                        mIccRecords = ir;
                        CatLog.d("CatService", "read data from sInstSim1");
                        String cmd = readCmdFromPreference(sInstance[slotId], context, sInstKey[slotId]);
                        if (mIccRecords != null) {
                            mIccRecords.unregisterForRecordsLoaded(sInstance[slotId]);
                        }
                        mIccRecords = ir;
                        mUiccApplication = ca;
                        mIccRecords.registerForRecordsLoaded(sInstance[slotId], 20, null);
                        handleProactiveCmdFromDB(sInstance[slotId], cmd);
                        CatLog.d("CatService", "sr changed reinitialize and return current sInstance");
                    }
                }
                CatLog.d("CatService", "Return current sInstance");
            } else if (ci == null || ca == null || ir == null || context == null || fh == null || ic == null) {
                CatLog.d("CatService", "null parameters, return directly");
                return null;
            } else {
                sInstance[slotId] = new CatService(ci, ca, ir, context, fh, ic, slotId);
                CatLog.d(sInstance[slotId], "create instance " + slotId);
            }
            sInstance[slotId].registerSATcb();
            CatService catService = sInstance[slotId];
            return catService;
        }
    }

    private void sendTerminalResponseByCurrentCmd(CatCmdMessage catCmd) {
        if (catCmd == null) {
            CatLog.e((Object) this, "catCmd is null.");
            return;
        }
        CommandType cmdType = CommandType.fromInt(catCmd.mCmdDet.typeOfCommand);
        CatLog.d((Object) this, "Send TR for cmd: " + cmdType);
        switch (m116xe796fd46()[cmdType.ordinal()]) {
            case 18:
                this.mCmdIf.handleCallSetupRequestFromSim(false, ResultCode.OK.value(), null);
                break;
            case 20:
            case 21:
                sendTerminalResponse(catCmd.mCmdDet, ResultCode.OK, false, 0, null);
                break;
            default:
                sendTerminalResponse(catCmd.mCmdDet, ResultCode.UICC_SESSION_TERM_BY_USER, false, 0, null);
                break;
        }
    }

    public void dispose() {
        synchronized (sInstanceLock) {
            CatLog.d((Object) this, "Disposing CatService object : " + this.mSlotId);
            mIccRecords.unregisterForRecordsLoaded(this);
            this.mContext.unregisterReceiver(this.CatServiceReceiver);
            this.mContext.unregisterReceiver(this.mStkIdleScreenAvailableReceiver);
            if (!(this.mIsProactiveCmdResponsed || this.mCurrentCmd == null)) {
                CatLog.d((Object) this, "Send TR for the last pending commands.");
                sendTerminalResponseByCurrentCmd(this.mCurrentCmd);
            }
            broadcastCardStateAndIccRefreshResp(CardState.CARDSTATE_ABSENT, null);
            this.mCmdIf.unSetOnCatSessionEnd(this);
            this.mCmdIf.unSetOnCatProactiveCmd(this);
            this.mCmdIf.unSetOnCatEvent(this);
            this.mCmdIf.unSetOnCatCallSetUp(this);
            this.mCmdIf.unSetOnCatCcAlphaNotify(this);
            if (!SystemProperties.get("ro.mtk_bsp_package").equals("1")) {
                this.mCmdIf.unSetOnStkEvdlCall(this);
            }
            this.mCmdIf.unSetOnStkSetupMenuReset(this);
            this.mNeedRegisterAgain = true;
            this.mCmdIf.unregisterForIccRefresh(this);
            if (this.mUiccController != null) {
                this.mUiccController.unregisterForIccChanged(this);
                this.mUiccController = null;
            }
            if (mUiccApplication != null) {
                mUiccApplication.unregisterForReady(this);
            }
            this.mMsgDecoder.dispose();
            this.mMsgDecoder = null;
            this.mHandlerThread.quit();
            this.mHandlerThread = null;
            removeCallbacksAndMessages(null);
            if (this.mBipService != null) {
                this.mBipService.dispose();
            }
            handleDBHandler(this.mSlotId);
            if (sInstance != null) {
                if (SubscriptionManager.isValidSlotId(this.mSlotId)) {
                    sInstance[this.mSlotId] = null;
                } else {
                    CatLog.d((Object) this, "error: invaild slot id: " + this.mSlotId);
                }
            }
        }
    }

    protected void finalize() {
        CatLog.d((Object) this, "Service finalized");
    }

    private void handleRilMsg(RilMessage rilMsg) {
        if (rilMsg != null) {
            CommandParams cmdParams;
            switch (rilMsg.mId) {
                case 1:
                    handleSessionEnd();
                    break;
                case 2:
                    if (rilMsg.mId == 2) {
                        this.mIsProactiveCmdResponsed = false;
                    }
                    try {
                        cmdParams = (CommandParams) rilMsg.mData;
                        if (cmdParams != null) {
                            if (rilMsg.mResCode != ResultCode.OK) {
                                if (rilMsg.mResCode != ResultCode.PRFRMD_ICON_NOT_DISPLAYED) {
                                    CatLog.d("CAT", "SS-handleMessage: invalid proactive command: " + cmdParams.mCmdDet.typeOfCommand);
                                    sendTerminalResponse(cmdParams.mCmdDet, rilMsg.mResCode, false, 0, null);
                                    break;
                                }
                                this.mSetUpMenuFromMD = rilMsg.mSetUpMenuFromMD;
                                handleCommand(cmdParams, true);
                                break;
                            }
                            this.mSetUpMenuFromMD = rilMsg.mSetUpMenuFromMD;
                            handleCommand(cmdParams, true);
                            break;
                        }
                    } catch (ClassCastException e) {
                        CatLog.d((Object) this, "Fail to parse proactive command");
                        if (this.mCurrentCmd != null) {
                            sendTerminalResponse(this.mCurrentCmd.mCmdDet, ResultCode.CMD_DATA_NOT_UNDERSTOOD, false, 0, null);
                            break;
                        }
                    }
                    break;
                case 3:
                    cmdParams = rilMsg.mData;
                    if (cmdParams != null) {
                        if (rilMsg.mResCode != ResultCode.OK) {
                            CatLog.d((Object) this, "event notify error code: " + rilMsg.mResCode);
                            if (rilMsg.mResCode == ResultCode.PRFRMD_ICON_NOT_DISPLAYED && (cmdParams.mCmdDet.typeOfCommand == 17 || cmdParams.mCmdDet.typeOfCommand == 18 || cmdParams.mCmdDet.typeOfCommand == 19 || cmdParams.mCmdDet.typeOfCommand == 20)) {
                                CatLog.d((Object) this, "notify user text message even though get icon fail");
                                handleCommand(cmdParams, false);
                            }
                            if (cmdParams.mCmdDet.typeOfCommand == 64) {
                                CatLog.d((Object) this, "Open Channel with ResultCode");
                                handleCommand(cmdParams, false);
                                break;
                            }
                        }
                        handleCommand(cmdParams, false);
                        break;
                    }
                    break;
                case 5:
                    cmdParams = rilMsg.mData;
                    if (cmdParams != null) {
                        handleCommand(cmdParams, false);
                        break;
                    }
                    break;
            }
        }
    }

    private boolean isSupportedSetupEventCommand(CatCmdMessage cmdMsg) {
        boolean flag = true;
        for (int eventVal : cmdMsg.getSetEventList().eventList) {
            CatLog.d((Object) this, "Event: " + eventVal);
            switch (eventVal) {
                case 5:
                case 7:
                    break;
                default:
                    flag = false;
                    break;
            }
        }
        return flag;
    }

    private void handleCommand(CommandParams cmdParams, boolean isProactiveCmd) {
        CatLog.d((Object) this, cmdParams.getCommandType().name());
        if (isProactiveCmd && this.mUiccController != null) {
            this.mUiccController.addCardLog("ProactiveCommand mSlotId=" + this.mSlotId + " cmdParams=" + cmdParams);
        }
        CatCmdMessage catCmdMessage = new CatCmdMessage(cmdParams);
        boolean isAlarmState;
        int flightMode;
        boolean isFlightMode;
        ResultCode resultCode;
        switch (m116xe796fd46()[cmdParams.getCommandType().ordinal()]) {
            case 1:
                if (1 == ((ActivateParams) cmdParams).mTarget) {
                    CatLog.d((Object) this, "Activate UICC-CLF interface mSlotId: " + this.mSlotId);
                    boolean result = false;
                    try {
                        Class nfcAdapter = Class.forName("android.nfc.NfcAdapter");
                        Class infcAdapterGsmaExtras = Class.forName("android.nfc.INfcAdapterGsmaExtras");
                        int sim1 = nfcAdapter.getField("SIM_1").getInt(null);
                        int sim2 = nfcAdapter.getField("SIM_2").getInt(null);
                        int sim3 = nfcAdapter.getField("SIM_3").getInt(null);
                        Class[] clsArr = new Class[1];
                        clsArr[0] = Context.class;
                        Method getDefaultAdapter = nfcAdapter.getDeclaredMethod("getDefaultAdapter", clsArr);
                        Object[] objArr = new Object[1];
                        objArr[0] = this.mContext;
                        Object adapter = getDefaultAdapter.invoke(null, objArr);
                        if (adapter == null) {
                            CatLog.d((Object) this, "Cannot get NFC Default Adapter !!!");
                            sendTerminalResponse(cmdParams.mCmdDet, ResultCode.TERMINAL_CRNTLY_UNABLE_TO_PROCESS, true, 0, null);
                            return;
                        }
                        Object gsmaExtras = nfcAdapter.getDeclaredMethod("getNfcAdapterGsmaExtrasInterface", new Class[0]).invoke(adapter, new Object[0]);
                        if (gsmaExtras == null) {
                            CatLog.d((Object) this, "NfcAdapterGsmaExtras service is null !!!");
                            sendTerminalResponse(cmdParams.mCmdDet, ResultCode.TERMINAL_CRNTLY_UNABLE_TO_PROCESS, true, 0, null);
                            return;
                        }
                        clsArr = new Class[1];
                        clsArr[0] = Integer.TYPE;
                        Method setNfcSwpActive = infcAdapterGsmaExtras.getDeclaredMethod("setNfcSwpActive", clsArr);
                        if (this.mSlotId == 0) {
                            objArr = new Object[1];
                            objArr[0] = Integer.valueOf(sim1);
                            result = ((Boolean) setNfcSwpActive.invoke(gsmaExtras, objArr)).booleanValue();
                        } else if (1 == this.mSlotId) {
                            objArr = new Object[1];
                            objArr[0] = Integer.valueOf(sim2);
                            result = ((Boolean) setNfcSwpActive.invoke(gsmaExtras, objArr)).booleanValue();
                        } else if (2 == this.mSlotId) {
                            objArr = new Object[1];
                            objArr[0] = Integer.valueOf(sim3);
                            result = ((Boolean) setNfcSwpActive.invoke(gsmaExtras, objArr)).booleanValue();
                        }
                        CatLog.d((Object) this, "setNfcSwpActive result: " + result);
                        if (result) {
                            sendTerminalResponse(cmdParams.mCmdDet, ResultCode.OK, false, 0, null);
                        } else {
                            sendTerminalResponse(cmdParams.mCmdDet, ResultCode.TERMINAL_CRNTLY_UNABLE_TO_PROCESS, true, 0, null);
                        }
                    } catch (ClassNotFoundException ex) {
                        CatLog.d((Object) this, "Activate UICC-CLF failed !!! " + ex);
                        sendTerminalResponse(cmdParams.mCmdDet, ResultCode.TERMINAL_CRNTLY_UNABLE_TO_PROCESS, true, 0, null);
                    } catch (NoSuchFieldException ex2) {
                        CatLog.d((Object) this, "Activate UICC-CLF failed !!! " + ex2);
                        sendTerminalResponse(cmdParams.mCmdDet, ResultCode.TERMINAL_CRNTLY_UNABLE_TO_PROCESS, true, 0, null);
                    } catch (NoSuchMethodException ex3) {
                        CatLog.d((Object) this, "Activate UICC-CLF failed !!! " + ex3);
                        sendTerminalResponse(cmdParams.mCmdDet, ResultCode.TERMINAL_CRNTLY_UNABLE_TO_PROCESS, true, 0, null);
                    } catch (IllegalAccessException ex4) {
                        CatLog.d((Object) this, "Activate UICC-CLF failed !!! " + ex4);
                        sendTerminalResponse(cmdParams.mCmdDet, ResultCode.TERMINAL_CRNTLY_UNABLE_TO_PROCESS, true, 0, null);
                    } catch (InvocationTargetException ex5) {
                        CatLog.d((Object) this, "Activate UICC-CLF failed !!! " + ex5);
                        sendTerminalResponse(cmdParams.mCmdDet, ResultCode.TERMINAL_CRNTLY_UNABLE_TO_PROCESS, true, 0, null);
                    }
                } else {
                    CatLog.d((Object) this, "Unsupport target or interface !!!");
                    sendTerminalResponse(cmdParams.mCmdDet, ResultCode.BEYOND_TERMINAL_CAPABILITY, false, 0, null);
                }
                return;
            case 2:
            case 7:
            case 10:
            case 13:
                BIPClientParams cmd = (BIPClientParams) cmdParams;
                boolean noAlphaUsrCnf;
                try {
                    noAlphaUsrCnf = this.mContext.getResources().getBoolean(17956996);
                } catch (NotFoundException e) {
                    noAlphaUsrCnf = false;
                }
                if (cmd.mTextMsg.text != null || (!cmd.mHasAlphaId && !noAlphaUsrCnf)) {
                    if (!this.mStkAppInstalled) {
                        CatLog.d((Object) this, "No STK application found.");
                        if (isProactiveCmd) {
                            sendTerminalResponse(cmdParams.mCmdDet, ResultCode.BEYOND_TERMINAL_CAPABILITY, false, 0, null);
                            return;
                        }
                    }
                    if (isProactiveCmd && (cmdParams.getCommandType() == CommandType.CLOSE_CHANNEL || cmdParams.getCommandType() == CommandType.RECEIVE_DATA || cmdParams.getCommandType() == CommandType.SEND_DATA)) {
                        sendTerminalResponse(cmdParams.mCmdDet, ResultCode.OK, false, 0, null);
                        break;
                    }
                }
                CatLog.d((Object) this, "cmd " + cmdParams.getCommandType() + " with null alpha id");
                if (isProactiveCmd) {
                    sendTerminalResponse(cmdParams.mCmdDet, ResultCode.OK, false, 0, null);
                } else if (cmdParams.getCommandType() == CommandType.OPEN_CHANNEL) {
                    this.mCmdIf.handleCallSetupRequestFromSim(true, ResultCode.OK.value(), null);
                }
                return;
                break;
            case 3:
                if (!SystemProperties.get("ro.mtk_bsp_package").equals("1") && this.mHasCachedDTCmd) {
                    CatLog.d((Object) this, "[CacheDT cache DISPLAY_TEXT");
                    int seqValue = System.getInt(this.mContext.getContentResolver(), "dialog_sequence_settings", 0);
                    CatLog.d((Object) this, "seqValue in CatService, " + seqValue);
                    if (seqValue != 2) {
                        this.mCachedDisplayTextCmd = catCmdMessage;
                        if (seqValue == 0) {
                            System.putInt(this.mContext.getContentResolver(), "dialog_sequence_settings", 2);
                        }
                        CatLog.d((Object) this, "[CacheDT set current cmd as DISPLAY_TEXT");
                        this.mCurrentCmd = catCmdMessage;
                        startTimeOut(46, (long) this.CACHED_DISPLAY_TIMEOUT);
                        return;
                    }
                }
                isAlarmState = isAlarmBoot();
                try {
                    flightMode = Global.getInt(this.mContext.getContentResolver(), "airplane_mode_on");
                } catch (SettingNotFoundException e2) {
                    CatLog.d((Object) this, "fail to get property from Settings");
                    flightMode = 0;
                }
                isFlightMode = flightMode != 0;
                CatLog.d((Object) this, "isAlarmState = " + isAlarmState + ", isFlightMode = " + isFlightMode + ", flightMode = " + flightMode);
                if (isAlarmState && isFlightMode) {
                    sendTerminalResponse(cmdParams.mCmdDet, ResultCode.OK, false, 0, null);
                    return;
                }
                if (catCmdMessage.geTextMessage().text != null) {
                    if ((catCmdMessage.geTextMessage().text.equals("Error in application") || catCmdMessage.geTextMessage().text.equals("invalid input") || catCmdMessage.geTextMessage().text.equals("DF A8'H Default Error")) && mIccRecords != null && (mIccRecords.isIndiaAirtelPlmn() || mIccRecords.getOperatorNumeric() == null)) {
                        CatLog.d((Object) this, "Ignore airtel sim card popup info, send TR directly");
                        sendTerminalResponse(cmdParams.mCmdDet, ResultCode.OK, false, 0, null);
                        return;
                    } else if (catCmdMessage.geTextMessage().text.equals("Out of variable memory") && mIccRecords != null && (mIccRecords.isTataDocomoPlmn() || mIccRecords.getOperatorNumeric() == null)) {
                        CatLog.d((Object) this, "Ignore tata docomo sim card popup info, send TR directly");
                        sendTerminalResponse(cmdParams.mCmdDet, ResultCode.OK, false, 0, null);
                        return;
                    }
                }
                if (this.isIvsrBootUp) {
                    CatLog.d((Object) this, "[IVSR send TR directly");
                    sendTerminalResponse(cmdParams.mCmdDet, ResultCode.BACKWARD_MOVE_BY_USER, false, 0, null);
                    return;
                } else if (this.isDisplayTextDisabled) {
                    CatLog.d((Object) this, "[Sim Recovery send TR directly");
                    sendTerminalResponse(cmdParams.mCmdDet, ResultCode.BACKWARD_MOVE_BY_USER, false, 0, null);
                    return;
                } else if (SystemProperties.get(DISPLAY_TEXT_DISABLE_PROPERTY).equals("1")) {
                    CatLog.d((Object) this, "Filter DISPLAY_TEXT command.");
                    sendTerminalResponse(cmdParams.mCmdDet, ResultCode.BACKWARD_MOVE_BY_USER, false, 0, null);
                    return;
                }
                break;
            case 4:
            case 5:
                boolean z;
                if (this.simState == null || this.simState.length() == 0 || "READY".equals(this.simState) || "IMSI".equals(this.simState)) {
                    z = true;
                } else {
                    z = "LOADED".equals(this.simState);
                }
                if (!z) {
                    sendTerminalResponse(cmdParams.mCmdDet, ResultCode.TERMINAL_CRNTLY_UNABLE_TO_PROCESS, false, 0, null);
                    return;
                }
                break;
            case 6:
                if (((LaunchBrowserParams) cmdParams).mConfirmMsg.text != null && ((LaunchBrowserParams) cmdParams).mConfirmMsg.text.equals(STK_DEFAULT)) {
                    ((LaunchBrowserParams) cmdParams).mConfirmMsg.text = this.mContext.getText(17040635).toString();
                    break;
                }
            case 8:
                this.mIsProactiveCmdResponsed = true;
                break;
            case 9:
                if (cmdParams.mCmdDet.commandQualifier == 3) {
                    Calendar cal = Calendar.getInstance();
                    byte[] datetime = new byte[7];
                    int temp = cal.get(1) - 2000;
                    datetime[0] = (byte) (((temp % 10) << 4) | (temp / 10));
                    temp = cal.get(2) + 1;
                    datetime[1] = (byte) (((temp % 10) << 4) | (temp / 10));
                    temp = cal.get(5);
                    datetime[2] = (byte) (((temp % 10) << 4) | (temp / 10));
                    temp = cal.get(11);
                    datetime[3] = (byte) (((temp % 10) << 4) | (temp / 10));
                    temp = cal.get(12);
                    datetime[4] = (byte) (((temp % 10) << 4) | (temp / 10));
                    temp = cal.get(13);
                    datetime[5] = (byte) (((temp % 10) << 4) | (temp / 10));
                    temp = cal.get(15) / 900000;
                    datetime[6] = (byte) (((temp % 10) << 4) | (temp / 10));
                    sendTerminalResponse(cmdParams.mCmdDet, ResultCode.OK, false, 0, new ProvideLocalInformationResponseData(datetime[0], datetime[1], datetime[2], datetime[3], datetime[4], datetime[5], datetime[6]));
                    return;
                } else if (cmdParams.mCmdDet.commandQualifier == 4) {
                    byte[] lang = new byte[2];
                    Locale locale = Locale.getDefault();
                    lang[0] = (byte) locale.getLanguage().charAt(0);
                    lang[1] = (byte) locale.getLanguage().charAt(1);
                    sendTerminalResponse(cmdParams.mCmdDet, ResultCode.OK, false, 0, new ProvideLocalInformationResponseData(lang));
                    return;
                } else if (cmdParams.mCmdDet.commandQualifier == 10) {
                    sendTerminalResponse(cmdParams.mCmdDet, ResultCode.OK, false, 0, new ProvideLocalInformationResponseData(getBatteryState(this.mContext)));
                    return;
                } else {
                    return;
                }
            case 11:
                this.mIsProactiveCmdResponsed = true;
                cmdParams.mCmdDet.typeOfCommand = CommandType.SET_UP_IDLE_MODE_TEXT.value();
                if (cmdParams.mCmdDet.commandQualifier != 4) {
                    CatLog.d((Object) this, "Do not to remove event list because SIM Refresh type not 4");
                    break;
                }
                CatLog.d((Object) this, "remove event list because of SIM Refresh type 4");
                this.mEventList = null;
                break;
            case 12:
                isAlarmState = isAlarmBoot();
                try {
                    flightMode = Global.getInt(this.mContext.getContentResolver(), "airplane_mode_on");
                } catch (SettingNotFoundException e3) {
                    CatLog.d((Object) this, "fail to get property from Settings");
                    flightMode = 0;
                }
                isFlightMode = flightMode != 0;
                CatLog.d((Object) this, "isAlarmState = " + isAlarmState + ", isFlightMode = " + isFlightMode + ", flightMode = " + flightMode);
                if (isAlarmState && isFlightMode) {
                    sendTerminalResponse(cmdParams.mCmdDet, ResultCode.UICC_SESSION_TERM_BY_USER, false, 0, null);
                    return;
                }
            case 14:
            case 15:
            case 16:
            case 17:
                this.mIsProactiveCmdResponsed = true;
                if (((DisplayTextParams) cmdParams).mTextMsg.text != null && ((DisplayTextParams) cmdParams).mTextMsg.text.equals(STK_DEFAULT)) {
                    ((DisplayTextParams) cmdParams).mTextMsg.text = this.mContext.getText(17040634).toString();
                    break;
                }
            case 18:
                if (((CallSetupParams) cmdParams).mConfirmMsg.text != null && ((CallSetupParams) cmdParams).mConfirmMsg.text.equals(STK_DEFAULT)) {
                    ((CallSetupParams) cmdParams).mConfirmMsg.text = this.mContext.getText(17040636).toString();
                    break;
                }
            case 19:
                this.mBipService.setSetupEventList(catCmdMessage);
                this.mIsProactiveCmdResponsed = true;
                this.mEventList = ((SetupEventListParams) cmdParams).eventList;
                return;
            case 20:
                if (cmdParams.mLoadIconFailed) {
                    resultCode = ResultCode.PRFRMD_ICON_NOT_DISPLAYED;
                } else {
                    resultCode = ResultCode.OK;
                }
                sendTerminalResponse(cmdParams.mCmdDet, resultCode, false, 0, null);
                break;
            case 21:
                if (removeMenu(catCmdMessage.getMenu())) {
                    this.mMenuCmd = null;
                } else {
                    this.mMenuCmd = catCmdMessage;
                }
                CatLog.d("CAT", "mSetUpMenuFromMD: " + this.mSetUpMenuFromMD);
                if (catCmdMessage.getMenu() != null) {
                    catCmdMessage.getMenu().setSetUpMenuFlag(this.mSetUpMenuFromMD ? 1 : 0);
                }
                if (!this.mSetUpMenuFromMD) {
                    this.mIsProactiveCmdResponsed = true;
                    break;
                }
                this.mSetUpMenuFromMD = false;
                if (cmdParams.mLoadIconFailed) {
                    resultCode = ResultCode.PRFRMD_ICON_NOT_DISPLAYED;
                } else {
                    resultCode = ResultCode.OK;
                }
                sendTerminalResponse(cmdParams.mCmdDet, resultCode, false, 0, null);
                break;
            default:
                CatLog.d((Object) this, "Unsupported command");
                return;
        }
        this.mCurrentCmd = catCmdMessage;
        broadcastCatCmdIntent(catCmdMessage);
    }

    private void broadcastCatCmdIntent(CatCmdMessage cmdMsg) {
        Intent intent = new Intent(AppInterface.CAT_CMD_ACTION);
        intent.addFlags(67108864);
        intent.addFlags(268435456);
        intent.putExtra("STK CMD", cmdMsg);
        intent.putExtra("SLOT_ID", this.mSlotId);
        CatLog.d((Object) this, "Sending CmdMsg: " + cmdMsg + " on slotid:" + this.mSlotId);
        this.mContext.sendBroadcast(intent, AppInterface.STK_PERMISSION);
    }

    private void handleSessionEnd() {
        CatLog.d((Object) this, "SESSION END on " + this.mSlotId);
        this.mCurrentCmd = this.mMenuCmd;
        Intent intent = new Intent(AppInterface.CAT_SESSION_END_ACTION);
        intent.putExtra("SLOT_ID", this.mSlotId);
        this.mContext.sendBroadcast(intent, AppInterface.STK_PERMISSION);
    }

    private void sendTerminalResponse(CommandDetails cmdDet, ResultCode resultCode, boolean includeAdditionalInfo, int additionalInfo, ResponseData resp) {
        int length = 2;
        if (cmdDet == null) {
            CatLog.e((Object) this, "SS-sendTR: cmdDet is null");
            return;
        }
        CatLog.d((Object) this, "SS-sendTR: command type is " + cmdDet.typeOfCommand);
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        Input cmdInput = null;
        if (this.mCurrentCmd != null) {
            cmdInput = this.mCurrentCmd.geInput();
        }
        this.mIsProactiveCmdResponsed = true;
        int tag = ComprehensionTlvTag.COMMAND_DETAILS.value();
        if (cmdDet.compRequired) {
            tag |= 128;
        }
        buf.write(tag);
        buf.write(3);
        buf.write(cmdDet.commandNumber);
        buf.write(cmdDet.typeOfCommand);
        buf.write(cmdDet.commandQualifier);
        buf.write(ComprehensionTlvTag.DEVICE_IDENTITIES.value() | 128);
        buf.write(2);
        buf.write(130);
        buf.write(129);
        tag = ComprehensionTlvTag.RESULT.value();
        if (cmdDet.compRequired) {
            tag |= 128;
        }
        buf.write(tag);
        if (!includeAdditionalInfo) {
            length = 1;
        }
        buf.write(length);
        buf.write(resultCode.value());
        if (includeAdditionalInfo) {
            buf.write(additionalInfo);
        }
        if (resp != null) {
            CatLog.d((Object) this, "SS-sendTR: write response data into TR");
            resp.format(buf);
        } else {
            encodeOptionalTags(cmdDet, resultCode, cmdInput, buf);
        }
        String hexString = IccUtils.bytesToHexString(buf.toByteArray());
        CatLog.d((Object) this, "TERMINAL RESPONSE: " + hexString);
        this.mCmdIf.sendTerminalResponse(hexString, null);
    }

    private void encodeOptionalTags(CommandDetails cmdDet, ResultCode resultCode, Input cmdInput, ByteArrayOutputStream buf) {
        CommandType cmdType = CommandType.fromInt(cmdDet.typeOfCommand);
        if (cmdType != null) {
            switch (m116xe796fd46()[cmdType.ordinal()]) {
                case 4:
                    if (resultCode.value() == ResultCode.NO_RESPONSE_FROM_USER.value() && cmdInput != null && cmdInput.duration != null) {
                        getInKeyResponse(buf, cmdInput);
                        return;
                    }
                    return;
                case 9:
                    if (cmdDet.commandQualifier == 4 && resultCode.value() == ResultCode.OK.value()) {
                        getPliResponse(buf);
                        return;
                    }
                    return;
                default:
                    CatLog.d((Object) this, "encodeOptionalTags() Unsupported Cmd details=" + cmdDet);
                    return;
            }
        }
        CatLog.d((Object) this, "encodeOptionalTags() bad Cmd details=" + cmdDet);
    }

    private void getInKeyResponse(ByteArrayOutputStream buf, Input cmdInput) {
        buf.write(ComprehensionTlvTag.DURATION.value());
        buf.write(2);
        TimeUnit timeUnit = cmdInput.duration.timeUnit;
        buf.write(TimeUnit.SECOND.value());
        buf.write(cmdInput.duration.timeInterval);
    }

    private void getPliResponse(ByteArrayOutputStream buf) {
        String lang = Locale.getDefault().getLanguage();
        if (lang != null) {
            buf.write(ComprehensionTlvTag.LANGUAGE.value());
            ResponseData.writeLength(buf, lang.length());
            buf.write(lang.getBytes(), 0, lang.length());
        }
    }

    private void sendMenuSelection(int menuId, boolean helpRequired) {
        CatLog.d("CatService", "sendMenuSelection SET_UP_MENU");
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        buf.write(211);
        buf.write(0);
        buf.write(ComprehensionTlvTag.DEVICE_IDENTITIES.value() | 128);
        buf.write(2);
        buf.write(1);
        buf.write(129);
        buf.write(ComprehensionTlvTag.ITEM_ID.value() | 128);
        buf.write(1);
        buf.write(menuId);
        if (helpRequired) {
            buf.write(ComprehensionTlvTag.HELP_REQUEST.value());
            buf.write(0);
        }
        byte[] rawData = buf.toByteArray();
        rawData[1] = (byte) (rawData.length - 2);
        String hexString = IccUtils.bytesToHexString(rawData);
        CatLog.d("CatService", "sendMenuSelection before");
        this.mCmdIf.sendEnvelope(hexString, null);
        CatLog.d("CatService", "sendMenuSelection after");
        cancelTimeOut(15);
        CatLog.d((Object) this, "[Reset Disable Display Text flag because MENU_SELECTION");
        this.isDisplayTextDisabled = false;
        if (SystemProperties.get("persist.sys.esn_track_switch").equals("1")) {
            this.mContext.sendBroadcast(new Intent(mEsnTrackUtkMenuSelect).putExtra(SimInfo.SLOT, this.mSlotId));
        }
    }

    private void writeCallDisConnED(ByteArrayOutputStream buffer) {
        if (!SystemProperties.get("ro.mtk_bsp_package").equals("1")) {
            EventDownloadCallInfo evdlcallInfo = (EventDownloadCallInfo) this.mEventDownloadCallDisConnInfo.removeFirst();
            if (evdlcallInfo != null) {
                CatLog.d((Object) this, "SS-eventDownload: event is CALL_DISCONNECTED.[" + evdlcallInfo.mIsFarEnd + "," + evdlcallInfo.mTi + "," + evdlcallInfo.mCauseLen + "," + evdlcallInfo.mCause + "]");
                buffer.write(1 == evdlcallInfo.mIsFarEnd ? 131 : 130);
                buffer.write(129);
                buffer.write(ComprehensionTlvTag.TRANSACTION_ID.value());
                buffer.write(1);
                buffer.write(evdlcallInfo.mTi);
                if (evdlcallInfo.mCauseLen == 0) {
                    buffer.write(ComprehensionTlvTag.CAUSE.value() | 128);
                    buffer.write(0);
                    return;
                } else if (255 != evdlcallInfo.mCauseLen) {
                    buffer.write(ComprehensionTlvTag.CAUSE.value() | 128);
                    buffer.write(evdlcallInfo.mCauseLen);
                    for (int i = evdlcallInfo.mCauseLen - 1; i >= 0; i--) {
                        CatLog.d((Object) this, "SS-eventDownload:cause:" + Integer.toHexString((evdlcallInfo.mCause >> (i * 8)) & 255));
                        buffer.write((evdlcallInfo.mCause >> (i * 8)) & 255);
                    }
                    return;
                } else {
                    CatLog.d((Object) this, "SS-eventDownload:no cause value");
                    return;
                }
            }
            CatLog.d((Object) this, "SS-eventDownload:X null evdlcallInfo");
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:67:0x02d0  */
    /* JADX WARNING: Removed duplicated region for block: B:18:0x00e1  */
    /* JADX WARNING: Removed duplicated region for block: B:41:0x015a  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void eventDownload(int event, int sourceId, int destinationId, byte[] additionalInfo, boolean oneShot) {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        if (this.mEventList == null || this.mEventList.length == 0) {
            CatLog.d((Object) this, "SS-eventDownload: event list null");
            return;
        }
        byte[] rawData;
        CatLog.d((Object) this, "SS-eventDownload: event list length:" + this.mEventList.length);
        int index = 0;
        while (index < this.mEventList.length) {
            CatLog.d((Object) this, "SS-eventDownload: event [" + this.mEventList[index] + "]");
            if (this.mEventList[index] == event) {
                int i;
                Intent intent;
                if (event == 5) {
                    CatLog.d((Object) this, "SS-eventDownload: event is IDLE_SCREEN_AVAILABLE");
                    CatLog.d((Object) this, "SS-eventDownload: sent intent with idle = false");
                    intent = new Intent(IDLE_SCREEN_INTENT_NAME);
                    intent.putExtra(IDLE_SCREEN_ENABLE_KEY, false);
                    this.mContext.sendBroadcast(intent);
                } else if (event == 4) {
                    CatLog.d((Object) this, "SS-eventDownload: event is USER_ACTIVITY");
                    intent = new Intent(USER_ACTIVITY_INTENT_NAME);
                    intent.putExtra(USER_ACTIVITY_ENABLE_KEY, false);
                    this.mContext.sendBroadcast(intent);
                } else if (event == 1) {
                    CatLog.d((Object) this, "SS-eventDownload: event is CALL_CONNECTED");
                } else if (event == 2) {
                    CatLog.d((Object) this, "SS-eventDownload: event is CALL_DISCONNECTED");
                }
                if (oneShot) {
                    this.mEventList[index] = (byte) 0;
                }
                buf.write(BerTlv.BER_EVENT_DOWNLOAD_TAG);
                buf.write(0);
                buf.write(ComprehensionTlvTag.EVENT_LIST.value() | 128);
                buf.write(1);
                buf.write(event);
                buf.write(ComprehensionTlvTag.DEVICE_IDENTITIES.value() | 128);
                buf.write(2);
                Message msg1;
                if (!SystemProperties.get("ro.mtk_bsp_package").equals("1")) {
                    buf.write(sourceId);
                    buf.write(destinationId);
                } else if (event == 2) {
                    if (this.mEventDownloadCallDisConnInfo.size() <= 0) {
                        CatLog.d((Object) this, "SS-eventDownload: Wait 2s for modem CALL_DISCONNECTED");
                        msg1 = obtainMessage(23);
                        if (this.mEvdlCallObj > 65535) {
                            this.mEvdlCallObj = 0;
                        }
                        i = this.mEvdlCallObj;
                        this.mEvdlCallObj = i + 1;
                        msg1.obj = new Integer(i);
                        this.mEvdlCallDisConnObjQ.add((Integer) msg1.obj);
                        this.mTimeoutHandler.sendMessageDelayed(msg1, (long) this.MODEM_EVDL_TIMEOUT);
                        this.mNumEventDownloadCallDisConn++;
                        CatLog.d((Object) this, "SS-eventDownload: mNumEventDownloadCallDisConn ++.[" + this.mNumEventDownloadCallDisConn + "]");
                        return;
                    } else if (this.mIsAllCallDisConn) {
                        while (this.mEventDownloadCallDisConnInfo.size() > 0) {
                            writeCallDisConnED(buf);
                        }
                    } else {
                        writeCallDisConnED(buf);
                    }
                } else if (event != 1) {
                    buf.write(sourceId);
                    buf.write(destinationId);
                } else if (this.mEventDownloadCallConnInfo.size() > 0) {
                    EventDownloadCallInfo evdlcallInfo = (EventDownloadCallInfo) this.mEventDownloadCallConnInfo.removeFirst();
                    if (evdlcallInfo != null) {
                        CatLog.d((Object) this, "SS-eventDownload: event is CALL_CONNECTED.[" + evdlcallInfo.mIsMTCall + "," + evdlcallInfo.mTi + "]");
                        buf.write(1 == evdlcallInfo.mIsMTCall ? 130 : 131);
                        buf.write(129);
                        buf.write(ComprehensionTlvTag.TRANSACTION_ID.value());
                        buf.write(1);
                        buf.write(evdlcallInfo.mTi);
                    } else {
                        CatLog.d((Object) this, "SS-eventDownload:O null evdlcallInfo");
                    }
                } else {
                    msg1 = obtainMessage(22);
                    if (this.mEvdlCallObj > 65535) {
                        this.mEvdlCallObj = 0;
                    }
                    i = this.mEvdlCallObj;
                    this.mEvdlCallObj = i + 1;
                    msg1.obj = new Integer(i);
                    this.mEvdlCallConnObjQ.add((Integer) msg1.obj);
                    this.mTimeoutHandler.sendMessageDelayed(msg1, (long) this.MODEM_EVDL_TIMEOUT);
                    this.mNumEventDownloadCallConn++;
                    CatLog.d((Object) this, "SS-eventDownload: mNumEventDownloadCallConn ++.[" + this.mNumEventDownloadCallConn + "]");
                    return;
                }
                if (additionalInfo != null) {
                    for (byte b : additionalInfo) {
                        buf.write(b);
                    }
                }
                rawData = buf.toByteArray();
                rawData[1] = (byte) (rawData.length - 2);
                this.mCmdIf.sendEnvelope(IccUtils.bytesToHexString(rawData), null);
            }
            index++;
            if (index == this.mEventList.length) {
                return;
            }
        }
        buf.write(BerTlv.BER_EVENT_DOWNLOAD_TAG);
        buf.write(0);
        buf.write(ComprehensionTlvTag.EVENT_LIST.value() | 128);
        buf.write(1);
        buf.write(event);
        buf.write(ComprehensionTlvTag.DEVICE_IDENTITIES.value() | 128);
        buf.write(2);
        if (!SystemProperties.get("ro.mtk_bsp_package").equals("1")) {
        }
        if (additionalInfo != null) {
        }
        rawData = buf.toByteArray();
        rawData[1] = (byte) (rawData.length - 2);
        this.mCmdIf.sendEnvelope(IccUtils.bytesToHexString(rawData), null);
    }

    private void registerSATcb() {
        CatLog.d("CatService", "registerSATcb, mNeedRegisterAgain: " + this.mNeedRegisterAgain);
        if (this.mNeedRegisterAgain) {
            this.mCmdIf.setOnCatSessionEnd(this, 1, null);
            this.mCmdIf.setOnCatEvent(this, 3, null);
            this.mCmdIf.setOnCatCallSetUp(this, 4, null);
            if (!SystemProperties.get("ro.mtk_bsp_package").equals("1")) {
                this.mCmdIf.setOnStkEvdlCall(this, 21, null);
            }
            this.mCmdIf.setOnStkSetupMenuReset(this, 24, null);
            this.mCmdIf.setOnCatCcAlphaNotify(this, 9, null);
            this.mNeedRegisterAgain = false;
        }
    }

    public static CatService getInstance(CommandsInterface ci, Context context, UiccCard ic) {
        CatLog.d("CatService", "call getInstance 2");
        int sim_id = 0;
        if (ic != null) {
            sim_id = ic.getPhoneId();
            CatLog.d("CatService", "get SIM id from UiccCard. sim id: " + sim_id);
        }
        return getInstance(ci, context, ic, sim_id);
    }

    public static AppInterface getInstance() {
        CatLog.d("CatService", "call getInstance 4");
        return getInstance(null, null, null, 0);
    }

    public static AppInterface getInstance(int slotId) {
        CatLog.d("CatService", "call getInstance 3");
        return getInstance(null, null, null, slotId);
    }

    private static void handleProactiveCmdFromDB(CatService inst, String data) {
        if (SystemProperties.get("ro.mtk_bsp_package").equals("1")) {
            CatLog.d("CatService", "BSP package does not support db cache.");
        } else if (data == null) {
            CatLog.d("CatService", "handleProactiveCmdFromDB: cmd = null");
        } else {
            inst.default_send_setupmenu_tr = false;
            CatLog.d("CatService", " handleProactiveCmdFromDB: cmd = " + data + " from: " + inst);
            inst.mMsgDecoder.sendStartDecodingMessageParams(new RilMessage(2, data));
            CatLog.d("CatService", "handleProactiveCmdFromDB: over");
        }
    }

    private boolean isSetUpMenuCmd(String cmd) {
        boolean validCmd = false;
        if (cmd == null) {
            return false;
        }
        try {
            if (cmd.charAt(2) == '8' && cmd.charAt(3) == '1') {
                if (cmd.charAt(12) == '2' && cmd.charAt(13) == '5') {
                    validCmd = true;
                }
            } else if (cmd.charAt(10) == '2' && cmd.charAt(11) == '5') {
                validCmd = true;
            }
            return validCmd;
        } catch (IndexOutOfBoundsException e) {
            CatLog.d((Object) this, "IndexOutOfBoundsException isSetUpMenuCmd: " + cmd);
            e.printStackTrace();
            return false;
        }
    }

    public static boolean getSaveNewSetUpMenuFlag(int sim_id) {
        if (sInstance == null || sInstance[sim_id] == null) {
            return false;
        }
        boolean result = sInstance[sim_id].mSaveNewSetUpMenu;
        CatLog.d("CatService", sim_id + " , mSaveNewSetUpMenu: " + result);
        return result;
    }

    public void handleMessage(Message msg) {
        CatLog.d((Object) this, "handleMessage[" + msg.what + "]");
        AsyncResult ar;
        switch (msg.what) {
            case 1:
            case 2:
            case 3:
            case 5:
                CatLog.d((Object) this, "ril message arrived, slotid:" + this.mSlotId);
                String data = null;
                boolean flag = false;
                if (msg.obj != null) {
                    ar = msg.obj;
                    if (this.mMsgDecoder != null) {
                        if (!(ar == null || ar.result == null)) {
                            try {
                                data = (String) ar.result;
                                if (SystemProperties.get("ro.mtk_bsp_package").equals("1")) {
                                    CatLog.d((Object) this, "BSP package always set SET_UP_MENU from MD.");
                                    flag = true;
                                } else if (isSetUpMenuCmd(data) && this == sInstance[this.mSlotId]) {
                                    saveCmdToPreference(this.mContext, sInstKey[this.mSlotId], data);
                                    this.mSaveNewSetUpMenu = true;
                                    flag = true;
                                }
                            } catch (ClassCastException e) {
                                break;
                            }
                        }
                    }
                    CatLog.e((Object) this, "mMsgDecoder == null, return.");
                    return;
                }
                RilMessage rilMessage = new RilMessage(msg.what, data);
                rilMessage.setSetUpMenuFromMD(flag);
                this.mMsgDecoder.sendStartDecodingMessageParams(rilMessage);
                break;
            case 4:
                this.mMsgDecoder.sendStartDecodingMessageParams(new RilMessage(msg.what, null));
                break;
            case 6:
                handleCmdResponse((CatResponseMessage) msg.obj);
                break;
            case 7:
                CatLog.d((Object) this, "SIM Ready");
                if (!SystemProperties.get("ro.mtk_bsp_package").equals("1")) {
                    this.mCmdIf.setStkEvdlCallByAP(0, null);
                    break;
                } else {
                    this.mCmdIf.setStkEvdlCallByAP(1, null);
                    break;
                }
            case 8:
                CatLog.w((Object) this, "MSG_ID_ICC_CHANGED");
                updateIccAvailability();
                break;
            case 9:
                CatLog.d((Object) this, "RIL event Call Ctrl.");
                if (msg.obj != null) {
                    ar = (AsyncResult) msg.obj;
                    if (!(ar == null || ar.result == null)) {
                        String[] callCtrlInfo = ar.result;
                        try {
                            CatLog.d((Object) this, "callCtrlInfo.length: " + callCtrlInfo.length + "," + callCtrlInfo[0] + "," + callCtrlInfo[1] + "," + callCtrlInfo[2]);
                            if (callCtrlInfo[1] != null && callCtrlInfo[1].length() > 0) {
                                byte[] rawData = IccUtils.hexStringToBytes(callCtrlInfo[1]);
                                try {
                                    String alphaId = IccUtils.adnStringFieldToString(rawData, 0, rawData.length);
                                    CatLog.d((Object) this, "CC Alpha msg: " + alphaId + ", sim id: " + this.mSlotId);
                                    TextMessage textMessage = new TextMessage();
                                    CommandDetails cmdDet = new CommandDetails();
                                    cmdDet.typeOfCommand = CommandType.CALLCTRL_RSP_MSG.value();
                                    textMessage.text = alphaId;
                                    broadcastCatCmdIntent(new CatCmdMessage(new CallCtrlBySimParams(cmdDet, textMessage, Integer.parseInt(callCtrlInfo[0]), callCtrlInfo[2])));
                                    break;
                                } catch (IndexOutOfBoundsException e2) {
                                    CatLog.d((Object) this, "IndexOutOfBoundsException adnStringFieldToString");
                                    break;
                                }
                            }
                            CatLog.d((Object) this, "Null CC alpha id.");
                            break;
                        } catch (RuntimeException e3) {
                            CatLog.d((Object) this, "CC message drop");
                            break;
                        }
                    }
                }
                break;
            case 10:
                handleRilMsg((RilMessage) msg.obj);
                break;
            case 11:
                handleEventDownload((CatResponseMessage) msg.obj);
                break;
            case 12:
                handleDBHandler(msg.arg1);
                break;
            case 13:
                CatLog.d((Object) this, "MSG_ID_LAUNCH_DB_SETUP_MENU");
                String strCmd = readCmdFromPreference(sInstance[this.mSlotId], this.mContext, sInstKey[this.mSlotId]);
                if (!(sInstance[this.mSlotId] == null || strCmd == null)) {
                    handleProactiveCmdFromDB(sInstance[this.mSlotId], strCmd);
                    break;
                }
            case 14:
                CatLog.d((Object) this, "[IVSR cancel IVSR flag");
                this.isIvsrBootUp = false;
                break;
            case 20:
                break;
            case 21:
                if (!SystemProperties.get("ro.mtk_bsp_package").equals("1")) {
                    CatLog.d((Object) this, "RIL event download for call.");
                    if (msg.obj != null) {
                        ar = (AsyncResult) msg.obj;
                        if (!(ar == null || ar.result == null)) {
                            int[] evdlCalldata = (int[]) ar.result;
                            EventDownloadCallInfo eventDownloadCallInfo = new EventDownloadCallInfo(evdlCalldata[0], evdlCalldata[1], evdlCalldata[2], evdlCalldata[3], evdlCalldata[4], evdlCalldata[5]);
                            if (255 > eventDownloadCallInfo.mCauseLen) {
                                eventDownloadCallInfo.mCauseLen >>= 1;
                            } else {
                                eventDownloadCallInfo.mCauseLen = 255;
                            }
                            if (evdlCalldata[0] == 0) {
                                this.mEventDownloadCallConnInfo.add(eventDownloadCallInfo);
                                if (this.mNumEventDownloadCallConn > 0) {
                                    this.mNumEventDownloadCallConn--;
                                    removeMessages(22, this.mEvdlCallConnObjQ.removeFirst());
                                    CatLog.d((Object) this, "mNumEventDownloadCallConn --.[" + this.mNumEventDownloadCallConn + "]");
                                    eventDownload(1, 0, 0, null, false);
                                }
                            } else {
                                this.mEventDownloadCallDisConnInfo.add(eventDownloadCallInfo);
                                if (this.mNumEventDownloadCallDisConn > 0) {
                                    this.mNumEventDownloadCallDisConn--;
                                    removeMessages(23, this.mEvdlCallDisConnObjQ.removeFirst());
                                    CatLog.d((Object) this, "mNumEventDownloadCallDisConn --.[" + this.mNumEventDownloadCallDisConn + "]");
                                    eventDownload(2, 0, 0, null, false);
                                }
                            }
                            CatLog.d((Object) this, "Evdl data:" + evdlCalldata[0] + "," + evdlCalldata[1] + "," + evdlCalldata[2] + "," + evdlCalldata[3] + "," + evdlCalldata[4]);
                            break;
                        }
                    }
                }
                break;
            case 24:
                CatLog.d((Object) this, "SETUP_MENU_RESET : Setup menu reset.");
                ar = (AsyncResult) msg.obj;
                if (ar != null && ar.exception == null) {
                    this.mSaveNewSetUpMenu = false;
                    break;
                }
                CatLog.d((Object) this, "SETUP_MENU_RESET : AsyncResult null.");
                break;
                break;
            case 30:
                if (msg.obj == null) {
                    CatLog.d((Object) this, "IccRefresh Message is null");
                    break;
                }
                ar = (AsyncResult) msg.obj;
                if (ar != null && ar.result != null) {
                    broadcastCardStateAndIccRefreshResp(CardState.CARDSTATE_PRESENT, (IccRefreshResponse) ar.result);
                    break;
                }
                CatLog.d((Object) this, "Icc REFRESH with exception: " + ar.exception);
                break;
                break;
            default:
                throw new AssertionError("Unrecognized CAT command: " + msg.what);
        }
    }

    private void broadcastCardStateAndIccRefreshResp(CardState cardState, IccRefreshResponse iccRefreshState) {
        Intent intent = new Intent(AppInterface.CAT_ICC_STATUS_CHANGE);
        intent.addFlags(268435456);
        boolean cardPresent = cardState == CardState.CARDSTATE_PRESENT;
        if (iccRefreshState != null) {
            intent.putExtra(AppInterface.REFRESH_RESULT, iccRefreshState.refreshResult);
            CatLog.d((Object) this, "Sending IccResult with Result: " + iccRefreshState.refreshResult);
        }
        intent.putExtra(AppInterface.CARD_STATUS, cardPresent);
        CatLog.d((Object) this, "Sending Card Status: " + cardState + " " + "cardPresent: " + cardPresent);
        intent.putExtra("SLOT_ID", this.mSlotId);
        this.mContext.sendBroadcast(intent, AppInterface.STK_PERMISSION);
    }

    private void broadcastAlphaMessage(String alphaString) {
        CatLog.d((Object) this, "Broadcasting CAT Alpha message from card: " + alphaString);
        Intent intent = new Intent(AppInterface.CAT_ALPHA_NOTIFY_ACTION);
        intent.addFlags(268435456);
        intent.putExtra(AppInterface.ALPHA_STRING, alphaString);
        intent.putExtra("SLOT_ID", this.mSlotId);
        this.mContext.sendBroadcast(intent, AppInterface.STK_PERMISSION);
    }

    public synchronized void onCmdResponse(CatResponseMessage resMsg) {
        if (resMsg != null) {
            obtainMessage(6, resMsg).sendToTarget();
        }
    }

    public synchronized void onEventDownload(CatResponseMessage resMsg) {
        if (resMsg != null) {
            obtainMessage(11, resMsg).sendToTarget();
        }
    }

    public synchronized void onDBHandler(int sim_id) {
        obtainMessage(12, sim_id, 0).sendToTarget();
    }

    public synchronized void onLaunchCachedSetupMenu() {
        obtainMessage(13, this.mSlotId, 0).sendToTarget();
    }

    private boolean validateResponse(CatResponseMessage resMsg) {
        if (resMsg.mCmdDet.typeOfCommand == CommandType.SET_UP_EVENT_LIST.value() || resMsg.mCmdDet.typeOfCommand == CommandType.SET_UP_MENU.value()) {
            CatLog.d((Object) this, "CmdType: " + resMsg.mCmdDet.typeOfCommand);
            return true;
        } else if (this.mCurrentCmd == null) {
            return false;
        } else {
            boolean validResponse = resMsg.mCmdDet.compareTo(this.mCurrentCmd.mCmdDet);
            CatLog.d((Object) this, "isResponse for last valid cmd: " + validResponse);
            return validResponse;
        }
    }

    private boolean removeMenu(Menu menu) {
        try {
            return menu.items.size() == 1 && menu.items.get(0) == null;
        } catch (NullPointerException e) {
            CatLog.d((Object) this, "Unable to get Menu's items size");
            return true;
        }
    }

    private void handleEventDownload(CatResponseMessage resMsg) {
        eventDownload(resMsg.mEvent, resMsg.mSourceId, resMsg.mDestinationId, resMsg.mAdditionalInfo, resMsg.mOneShot);
    }

    private void handleDBHandler(int sim_id) {
        CatLog.d((Object) this, "handleDBHandler, sim_id: " + sim_id);
        saveCmdToPreference(this.mContext, sInstKey[sim_id], null);
    }

    private void handleCmdResponse(CatResponseMessage resMsg) {
        if (validateResponse(resMsg)) {
            ResponseData resp = null;
            boolean helpRequired = false;
            CommandDetails cmdDet = resMsg.getCmdDetails();
            CommandType type = CommandType.fromInt(cmdDet.typeOfCommand);
            switch (m117-getcom-android-internal-telephony-cat-ResultCodeSwitchesValues()[resMsg.mResCode.ordinal()]) {
                case 1:
                case 2:
                case 6:
                case 18:
                    switch (m116xe796fd46()[type.ordinal()]) {
                        case 3:
                            if (!SystemProperties.get("ro.mtk_bsp_package").equals("1") && this.mHasCachedDTCmd) {
                                resetPowerOnSequenceFlag();
                                break;
                            }
                        case 7:
                            this.mCmdIf.handleCallSetupRequestFromSim(false, ResultCode.BACKWARD_MOVE_BY_USER.value(), null);
                            this.mCurrentCmd = null;
                            return;
                        case 18:
                            CatLog.d((Object) this, "SS-handleCmdResponse: [BACKWARD_MOVE_BY_USER] userConfirm[" + resMsg.mUsersConfirm + "] resultCode[" + resMsg.mResCode.value() + "]");
                            this.mCmdIf.handleCallSetupRequestFromSim(false, ResultCode.BACKWARD_MOVE_BY_USER.value(), null);
                            this.mCurrentCmd = null;
                            return;
                    }
                    resp = null;
                    break;
                case 3:
                    helpRequired = true;
                    break;
                case 4:
                    if (cmdDet.typeOfCommand == CommandType.LAUNCH_BROWSER.value()) {
                        CatLog.d((Object) this, "send TR for LAUNCH_BROWSER_ERROR");
                        sendTerminalResponse(cmdDet, resMsg.mResCode, true, 2, null);
                        return;
                    }
                    break;
                case 5:
                    switch (m116xe796fd46()[type.ordinal()]) {
                        case 3:
                            if (!SystemProperties.get("ro.mtk_bsp_package").equals("1") && this.mHasCachedDTCmd) {
                                resetPowerOnSequenceFlag();
                            }
                            if (!(resMsg.mAdditionalInfo == null || resMsg.mAdditionalInfo.length <= 0 || resMsg.mAdditionalInfo[0] == (byte) 0)) {
                                sendTerminalResponse(cmdDet, resMsg.mResCode, true, resMsg.mAdditionalInfo[0], null);
                                this.mCurrentCmd = null;
                                return;
                            }
                        case 18:
                            this.mCmdIf.handleCallSetupRequestFromSim(resMsg.mUsersConfirm, resMsg.mResCode.value(), null);
                            this.mCurrentCmd = null;
                            return;
                    }
                    break;
                case 7:
                case 8:
                case 9:
                case 10:
                case 11:
                case 12:
                case 13:
                case 14:
                case 15:
                case 16:
                case 17:
                    break;
                case 19:
                    switch (m116xe796fd46()[CommandType.fromInt(cmdDet.typeOfCommand).ordinal()]) {
                        case 7:
                            CatLog.d("[BIP]", "SS-handleCmdResponse: User don't accept open channel");
                            this.mCmdIf.handleCallSetupRequestFromSim(false, ResultCode.USER_NOT_ACCEPT.value(), null);
                            this.mCurrentCmd = null;
                            return;
                    }
                    break;
                default:
                    return;
            }
            switch (m116xe796fd46()[type.ordinal()]) {
                case 3:
                    if (!SystemProperties.get("ro.mtk_bsp_package").equals("1") && this.mHasCachedDTCmd) {
                        resetPowerOnSequenceFlag();
                    }
                    byte[] additionalInfo = new byte[1];
                    if (resMsg.mResCode != ResultCode.TERMINAL_CRNTLY_UNABLE_TO_PROCESS) {
                        resMsg.mIncludeAdditionalInfo = false;
                        additionalInfo[0] = (byte) 0;
                        break;
                    }
                    additionalInfo[0] = (byte) 1;
                    resMsg.setAdditionalInfo(additionalInfo);
                    break;
                case 4:
                case 5:
                    Input input = this.mCurrentCmd.geInput();
                    if (!input.yesNo) {
                        if (!helpRequired) {
                            resp = new GetInkeyInputResponseData(resMsg.mUsersInput, input.ucs2, input.packed);
                            break;
                        }
                    }
                    resp = new GetInkeyInputResponseData(resMsg.mUsersYesNoSelection);
                    break;
                    break;
                case 7:
                case 18:
                    this.mCmdIf.handleCallSetupRequestFromSim(resMsg.mUsersConfirm, resMsg.mResCode.value(), null);
                    this.mCurrentCmd = null;
                    return;
                case 12:
                    CatLog.d("CatService", "SELECT_ITEM");
                    resp = new SelectItemResponseData(resMsg.mUsersMenuSelection);
                    break;
                case 21:
                    CatLog.d("CatService", "SET_UP_MENU");
                    sendMenuSelection(resMsg.mUsersMenuSelection, resMsg.mResCode == ResultCode.HELP_INFO_REQUIRED);
                    return;
            }
            ResultCode resultCode = resMsg.mResCode;
            boolean z = resMsg.mIncludeAdditionalInfo;
            int i = (!resMsg.mIncludeAdditionalInfo || resMsg.mAdditionalInfo == null || resMsg.mAdditionalInfo.length <= 0) ? 0 : resMsg.mAdditionalInfo[0];
            sendTerminalResponse(cmdDet, resultCode, z, i, resp);
            this.mCurrentCmd = null;
        }
    }

    public Context getContext() {
        return this.mContext;
    }

    private boolean isStkAppInstalled() {
        List<ResolveInfo> broadcastReceivers = this.mContext.getPackageManager().queryBroadcastReceivers(new Intent(AppInterface.CAT_CMD_ACTION), 128);
        if ((broadcastReceivers == null ? 0 : broadcastReceivers.size()) > 0) {
            return true;
        }
        return false;
    }

    public void update(CommandsInterface ci, Context context, UiccCard ic) {
        UiccCardApplication ca = null;
        IccRecords ir = null;
        if (ic != null) {
            int phoneType = 1;
            int[] subId = SubscriptionManager.getSubId(this.mSlotId);
            if (subId != null) {
                phoneType = TelephonyManager.getDefault().getCurrentPhoneType(subId[0]);
                CatLog.d("CatService", "update phoneType : " + phoneType + ", mSlotId: " + this.mSlotId + ", subId[0]:" + subId[0]);
            }
            if (phoneType == 2) {
                ca = ic.getApplication(2);
            } else {
                ca = ic.getApplicationIndex(0);
            }
            if (ca != null) {
                ir = ca.getIccRecords();
            }
        }
        synchronized (sInstanceLock) {
            if (ir != null) {
                if (mIccRecords != ir) {
                    if (mIccRecords != null) {
                        mIccRecords.unregisterForRecordsLoaded(this);
                    }
                    if (mUiccApplication != null) {
                        CatLog.d((Object) this, "unregisterForReady slotid: " + this.mSlotId + "instance : " + this);
                        mUiccApplication.unregisterForReady(this);
                    }
                    CatLog.d((Object) this, "Reinitialize the Service with SIMRecords and UiccCardApplication");
                    mIccRecords = ir;
                    mUiccApplication = ca;
                    mIccRecords.registerForRecordsLoaded(this, 20, null);
                    CatLog.d((Object) this, "registerForRecordsLoaded slotid=" + this.mSlotId + " instance:" + this);
                }
            }
        }
    }

    void updateIccAvailability() {
        if (this.mUiccController == null) {
            CatLog.d((Object) this, "updateIccAvailability, mUiccController is null");
            return;
        }
        CardState newState = CardState.CARDSTATE_ABSENT;
        UiccCard newCard = this.mUiccController.getUiccCard(this.mSlotId);
        if (newCard != null) {
            newState = newCard.getCardState();
        }
        CardState oldState = this.mCardState;
        this.mCardState = newState;
        CatLog.d((Object) this, "Slot id: " + this.mSlotId + " New Card State = " + newState + " " + "Old Card State = " + oldState);
        if (oldState == CardState.CARDSTATE_PRESENT && newState != CardState.CARDSTATE_PRESENT) {
            broadcastCardStateAndIccRefreshResp(newState, null);
        } else if (oldState != CardState.CARDSTATE_PRESENT && newState == CardState.CARDSTATE_PRESENT) {
            if (this.mCmdIf.getRadioState() == RadioState.RADIO_UNAVAILABLE) {
                CatLog.w((Object) this, "updateIccAvailability(): Radio unavailable");
                this.mCardState = oldState;
            } else {
                CatLog.d((Object) this, "SIM present. Reporting STK service running now...");
                this.mCmdIf.reportStkServiceIsRunning(null);
            }
        }
    }

    private boolean isAlarmBoot() {
        String bootReason = SystemProperties.get("sys.boot.reason");
        return bootReason != null ? bootReason.equals("1") : false;
    }

    private boolean isDeviceProvisioned(Context context) {
        boolean z = false;
        try {
            if (Global.getInt(context.getContentResolver(), "device_provisioned") != 0) {
                z = true;
            }
            return z;
        } catch (SettingNotFoundException e) {
            return false;
        }
    }

    private boolean checkSetupWizardInstalled() {
        String packageName = "com.google.android.setupwizard";
        String activityName = "com.google.android.setupwizard.SetupWizardActivity";
        PackageManager pm = this.mContext.getPackageManager();
        if (pm == null) {
            CatLog.d((Object) this, "fail to get PM");
            return false;
        }
        boolean isPkgInstalled = true;
        try {
            pm.getInstallerPackageName("com.google.android.setupwizard");
        } catch (IllegalArgumentException e) {
            CatLog.d((Object) this, "fail to get SetupWizard package");
            isPkgInstalled = false;
        }
        if (isPkgInstalled) {
            int pkgEnabledState = pm.getComponentEnabledSetting(new ComponentName("com.google.android.setupwizard", "com.google.android.setupwizard.SetupWizardActivity"));
            if (pkgEnabledState == 1 || pkgEnabledState == 0) {
                CatLog.d((Object) this, "should not show DISPLAY_TEXT immediately");
                return true;
            }
            CatLog.d((Object) this, "Setup Wizard Activity is not activate");
        }
        CatLog.d((Object) this, "isPkgInstalled = false");
        return false;
    }

    private void registerPowerOnSequenceObserver() {
        if (!SystemProperties.get("ro.mtk_bsp_package").equals("1")) {
            CatLog.d((Object) this, "call registerPowerOnSequenceObserver");
            this.mContext.getContentResolver().registerContentObserver(System.getUriFor("dialog_sequence_settings"), false, this.mPowerOnSequenceObserver);
            this.mHasCachedDTCmd = true;
        }
    }

    private void unregisterPowerOnSequenceObserver() {
        if (!SystemProperties.get("ro.mtk_bsp_package").equals("1")) {
            CatLog.d((Object) this, "call unregisterPowerOnSequenceObserver");
            this.mContext.getContentResolver().unregisterContentObserver(this.mPowerOnSequenceObserver);
            cancelTimeOut(46);
        }
    }

    private void resetPowerOnSequenceFlag() {
        if (!SystemProperties.get("ro.mtk_bsp_package").equals("1")) {
            int seqValue = System.getInt(this.mContext.getContentResolver(), "dialog_sequence_settings", 0);
            CatLog.d((Object) this, "call resetPowerOnSequenceFlag, seqValue: " + seqValue);
            if (seqValue == 2) {
                System.putInt(this.mContext.getContentResolver(), "dialog_sequence_settings", 0);
            }
            this.mHasCachedDTCmd = false;
        }
    }

    public CatCmdMessage getCmdMessage() {
        StringBuilder append = new StringBuilder().append("getCmdMessage, command type: ");
        int i = (this.mCmdMessage == null || this.mCmdMessage.mCmdDet == null) ? -1 : this.mCmdMessage.mCmdDet.typeOfCommand;
        CatLog.d((Object) this, append.append(i).toString());
        return this.mCmdMessage;
    }

    public IccRecords getIccRecords() {
        IccRecords iccRecords;
        synchronized (sInstanceLock) {
            iccRecords = mIccRecords;
        }
        return iccRecords;
    }

    private static void saveCmdToPreference(Context context, String key, String cmd) {
        synchronized (mLock) {
            CatLog.d("CatService", "saveCmdToPreference, key: " + key + ", cmd: " + cmd);
            Editor editor = context.getSharedPreferences("set_up_menu", 0).edit();
            editor.putString(key, cmd);
            editor.apply();
        }
    }

    private static String readCmdFromPreference(CatService inst, Context context, String key) {
        String cmd = String.valueOf(UsimPBMemInfo.STRING_NOT_SET);
        if (inst == null) {
            CatLog.d("CatService", "readCmdFromPreference with null instance");
            return null;
        }
        synchronized (mLock) {
            if (inst.mReadFromPreferenceDone) {
                CatLog.d("CatService", "readCmdFromPreference, do not read again");
            } else {
                cmd = context.getSharedPreferences("set_up_menu", 0).getString(key, UsimPBMemInfo.STRING_NOT_SET);
                inst.mReadFromPreferenceDone = true;
                CatLog.d("CatService", "readCmdFromPreference, key: " + key + ", cmd: " + cmd);
            }
        }
        if (cmd.length() == 0) {
            cmd = null;
        }
        return cmd;
    }

    public void setAllCallDisConn(boolean isDisConn) {
        if (!SystemProperties.get("ro.mtk_bsp_package").equals("1")) {
            this.mIsAllCallDisConn = isDisConn;
        }
    }

    public boolean isCallDisConnReceived() {
        boolean z = false;
        if (SystemProperties.get("ro.mtk_bsp_package").equals("1")) {
            return false;
        }
        if (this.mEventDownloadCallDisConnInfo.size() > 0) {
            z = true;
        }
        return z;
    }

    public static int getBatteryState(Context context) {
        int batteryState = 255;
        Intent batteryStatus = context.registerReceiver(null, new IntentFilter("android.intent.action.BATTERY_CHANGED"));
        if (batteryStatus != null) {
            int level = batteryStatus.getIntExtra("level", -1);
            int scale = batteryStatus.getIntExtra("scale", -1);
            int status = batteryStatus.getIntExtra("status", -1);
            boolean isCharging = status != 2 ? status == 5 : true;
            float batteryPct = ((float) level) / ((float) scale);
            CatLog.d("CatService", " batteryPct == " + batteryPct + "isCharging:" + isCharging);
            if (isCharging) {
                batteryState = 255;
            } else if (((double) batteryPct) <= 0.05d) {
                batteryState = 0;
            } else if (((double) batteryPct) > 0.05d && ((double) batteryPct) <= 0.15d) {
                batteryState = 1;
            } else if (((double) batteryPct) > 0.15d && ((double) batteryPct) <= 0.6d) {
                batteryState = 2;
            } else if (((double) batteryPct) > 0.6d && batteryPct < 1.0f) {
                batteryState = 3;
            } else if (batteryPct == 1.0f) {
                batteryState = 4;
            }
        }
        CatLog.d("CatService", "getBatteryState() batteryState = " + batteryState);
        return batteryState;
    }
}
