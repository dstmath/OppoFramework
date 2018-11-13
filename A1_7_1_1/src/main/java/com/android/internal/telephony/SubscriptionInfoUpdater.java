package com.android.internal.telephony;

import android.app.ActivityManagerNative;
import android.app.IUserSwitchObserver.Stub;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.IPackageManager;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserManager;
import android.preference.PreferenceManager;
import android.provider.Settings.Global;
import android.provider.oppo.CallLog;
import android.provider.oppo.Telephony.SimInfo;
import android.provider.oppo.Telephony.TextBasedCbSmsColumns;
import android.telephony.CarrierConfigManager;
import android.telephony.Rlog;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import com.android.internal.telephony.CommandException.Error;
import com.android.internal.telephony.uicc.IccCardProxy;
import com.android.internal.telephony.uicc.IccCardProxy.SimStateListener;
import com.android.internal.telephony.uicc.IccConstants;
import com.android.internal.telephony.uicc.IccFileHandler;
import com.android.internal.telephony.uicc.IccRecords;
import com.android.internal.telephony.uicc.IccUtils;
import com.android.internal.telephony.uicc.SpnOverride;
import com.android.internal.telephony.uicc.UiccController;
import com.mediatek.internal.telephony.DefaultSmsSimSettings;
import com.mediatek.internal.telephony.DefaultVoiceCallSubSettings;
import com.mediatek.internal.telephony.dataconnection.DataSubSelector;
import com.mediatek.internal.telephony.uicc.UsimPBMemInfo;
import com.mediatek.internal.telephony.worldphone.IWorldPhone;
import com.oppo.hypnus.HypnusManager;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicReferenceArray;

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
public class SubscriptionInfoUpdater extends Handler {
    private static final String ACTION_BOOT_STATE = "android.intent.action.BOOT_COMPLETED";
    private static final String ACTION_SUBINFO_STATE_CHANGE = "android.intent.action.SUBINFO_STATE_CHANGE";
    public static final int CARD_TYPE_CM = 2;
    public static final int CARD_TYPE_CT = 1;
    public static final int CARD_TYPE_CU = 3;
    public static final int CARD_TYPE_OTHER = 4;
    public static final int CARD_TYPE_TEST = 9;
    public static final int CARD_TYPE_UNKNOWN = -1;
    private static final String COMMON_SLOT_PROPERTY = "ro.mtk_sim_hot_swap_common_slot";
    public static final String CURR_SUBID = "curr_subid";
    private static final int EVENT_GET_NETWORK_SELECTION_MODE_DONE = 2;
    private static final int EVENT_RADIO_AVAILABLE = 101;
    private static final int EVENT_RADIO_UNAVAILABLE = 102;
    private static final int EVENT_SIM_ABSENT = 4;
    private static final int EVENT_SIM_IO_ERROR = 6;
    private static final int EVENT_SIM_LOADED = 3;
    private static final int EVENT_SIM_LOCKED = 5;
    private static final int EVENT_SIM_LOCKED_QUERY_ICCID_DONE = 1;
    private static final int EVENT_SIM_NO_CHANGED = 103;
    private static final int EVENT_SIM_PLUG_OUT = 105;
    private static final int EVENT_SIM_READY = 100;
    public static final int EVENT_SIM_READ_DELAY = 30;
    private static final int EVENT_SIM_RESTRICTED = 8;
    private static final int EVENT_SIM_UNKNOWN = 7;
    private static final int EVENT_TRAY_PLUG_IN = 104;
    private static final String ICCID_STRING_FOR_NO_SIM = "N/A";
    private static final String INTENT_KEY_SIM_STATE = "simstate";
    private static final String INTENT_KEY_SLOT_ID = "slotid";
    private static final String INTENT_KEY_SUB_ID = "subid";
    private static final String INTENT_VALUE_SIM_CARD_TYPE = "CARDTYPE";
    private static final String INTENT_VALUE_SIM_PLUG_IN = "PLUGIN";
    private static final String INTENT_VALUE_SIM_PLUG_OUT = "PLUGOUT";
    private static final String LOG_TAG = "SubscriptionInfoUpdater";
    private static final boolean MTK_FLIGHTMODE_POWEROFF_MD_SUPPORT = false;
    private static final int PROJECT_SIM_NUM = 0;
    private static final String[] PROPERTY_ICCID_SIM = null;
    public static final int SIM_CHANGED = -1;
    public static final int SIM_NEW = -2;
    public static final int SIM_NOT_CHANGE = 0;
    public static final int SIM_NOT_INSERT = -99;
    public static final int SIM_REPOSITION = -3;
    public static final int STATUS_NO_SIM_INSERTED = 0;
    public static final int STATUS_SIM1_INSERTED = 1;
    public static final int STATUS_SIM2_INSERTED = 2;
    public static final int STATUS_SIM3_INSERTED = 4;
    public static final int STATUS_SIM4_INSERTED = 8;
    private static Context mContext = null;
    private static HypnusManager mHM = null;
    private static String[] mIccId = null;
    private static int[] mInsertSimState = null;
    private static OemDeviceLock mOemLock = null;
    private static Phone[] mPhone = null;
    private static IccFileHandler[] sFh = null;
    private static boolean sHasInSertLockSim = false;
    private static int[] sIsUpdateAvailable = null;
    private static final int sReadICCID_retry_time = 1000;
    protected static SubscriptionInfoUpdater sSubInfoUpdater;
    private boolean QueryingIccid;
    private boolean isbootup;
    private boolean mBTrayPlugin;
    private CarrierServiceBindHelper mCarrierServiceBindHelper;
    private CommandsInterface[] mCis;
    private boolean mCommonSlotResetDone;
    private int mCurrentlyActiveUserId;
    protected AtomicReferenceArray<IccRecords> mIccRecords;
    protected final Object mLock;
    protected final Object mLockRebc;
    private IPackageManager mPackageManager;
    private int mReadIccIdCount;
    private Runnable mReadIccIdPropertyRunnable;
    private final SimStateListener mSSL;
    private SubscriptionManager mSubscriptionManager;
    private UserManager mUserManager;
    private boolean needSubUpdate;
    private Map<Integer, Intent> rebroadcastIntentsOnUnlock;
    private final BroadcastReceiver sReceiver;

    /* renamed from: com.android.internal.telephony.SubscriptionInfoUpdater$3 */
    class AnonymousClass3 extends SimStateListener {
        final /* synthetic */ SubscriptionInfoUpdater this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.SubscriptionInfoUpdater.3.<init>(com.android.internal.telephony.SubscriptionInfoUpdater):void, dex: 
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
        AnonymousClass3(com.android.internal.telephony.SubscriptionInfoUpdater r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.SubscriptionInfoUpdater.3.<init>(com.android.internal.telephony.SubscriptionInfoUpdater):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.SubscriptionInfoUpdater.3.<init>(com.android.internal.telephony.SubscriptionInfoUpdater):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.internal.telephony.SubscriptionInfoUpdater.3.onSimStateChange(int, android.content.Intent):void, dex: 
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
        public void onSimStateChange(int r1, android.content.Intent r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.internal.telephony.SubscriptionInfoUpdater.3.onSimStateChange(int, android.content.Intent):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.SubscriptionInfoUpdater.3.onSimStateChange(int, android.content.Intent):void");
        }
    }

    /* renamed from: com.android.internal.telephony.SubscriptionInfoUpdater$4 */
    class AnonymousClass4 extends Thread {
        final /* synthetic */ SubscriptionInfoUpdater this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.SubscriptionInfoUpdater.4.<init>(com.android.internal.telephony.SubscriptionInfoUpdater):void, dex: 
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
        AnonymousClass4(com.android.internal.telephony.SubscriptionInfoUpdater r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.SubscriptionInfoUpdater.4.<init>(com.android.internal.telephony.SubscriptionInfoUpdater):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.SubscriptionInfoUpdater.4.<init>(com.android.internal.telephony.SubscriptionInfoUpdater):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.internal.telephony.SubscriptionInfoUpdater.4.run():void, dex: 
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
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.internal.telephony.SubscriptionInfoUpdater.4.run():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.SubscriptionInfoUpdater.4.run():void");
        }
    }

    /* renamed from: com.android.internal.telephony.SubscriptionInfoUpdater$5 */
    class AnonymousClass5 extends Stub {
        final /* synthetic */ SubscriptionInfoUpdater this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.SubscriptionInfoUpdater.5.<init>(com.android.internal.telephony.SubscriptionInfoUpdater):void, dex: 
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
        AnonymousClass5(com.android.internal.telephony.SubscriptionInfoUpdater r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.SubscriptionInfoUpdater.5.<init>(com.android.internal.telephony.SubscriptionInfoUpdater):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.SubscriptionInfoUpdater.5.<init>(com.android.internal.telephony.SubscriptionInfoUpdater):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.internal.telephony.SubscriptionInfoUpdater.5.onForegroundProfileSwitch(int):void, dex: 
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
        public void onForegroundProfileSwitch(int r1) throws android.os.RemoteException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.internal.telephony.SubscriptionInfoUpdater.5.onForegroundProfileSwitch(int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.SubscriptionInfoUpdater.5.onForegroundProfileSwitch(int):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.internal.telephony.SubscriptionInfoUpdater.5.onUserSwitchComplete(int):void, dex: 
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
        public void onUserSwitchComplete(int r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.internal.telephony.SubscriptionInfoUpdater.5.onUserSwitchComplete(int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.SubscriptionInfoUpdater.5.onUserSwitchComplete(int):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.internal.telephony.SubscriptionInfoUpdater.5.onUserSwitching(int, android.os.IRemoteCallback):void, dex: 
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
        public void onUserSwitching(int r1, android.os.IRemoteCallback r2) throws android.os.RemoteException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.internal.telephony.SubscriptionInfoUpdater.5.onUserSwitching(int, android.os.IRemoteCallback):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.SubscriptionInfoUpdater.5.onUserSwitching(int, android.os.IRemoteCallback):void");
        }
    }

    /* renamed from: com.android.internal.telephony.SubscriptionInfoUpdater$6 */
    class AnonymousClass6 extends Thread {
        final /* synthetic */ SubscriptionInfoUpdater this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.SubscriptionInfoUpdater.6.<init>(com.android.internal.telephony.SubscriptionInfoUpdater):void, dex: 
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
        AnonymousClass6(com.android.internal.telephony.SubscriptionInfoUpdater r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.SubscriptionInfoUpdater.6.<init>(com.android.internal.telephony.SubscriptionInfoUpdater):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.SubscriptionInfoUpdater.6.<init>(com.android.internal.telephony.SubscriptionInfoUpdater):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.internal.telephony.SubscriptionInfoUpdater.6.run():void, dex: 
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
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.internal.telephony.SubscriptionInfoUpdater.6.run():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.SubscriptionInfoUpdater.6.run():void");
        }
    }

    private static class QueryIccIdUserObj {
        public String reason;
        public int slotId;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.SubscriptionInfoUpdater.QueryIccIdUserObj.<init>(java.lang.String, int):void, dex: 
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
        QueryIccIdUserObj(java.lang.String r1, int r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.SubscriptionInfoUpdater.QueryIccIdUserObj.<init>(java.lang.String, int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.SubscriptionInfoUpdater.QueryIccIdUserObj.<init>(java.lang.String, int):void");
        }
    }

    private class SubscriptionUpdatorThread extends Thread {
        public static final int SIM_ABSENT = 0;
        public static final int SIM_LOADED = 1;
        public static final int SIM_LOCKED = 2;
        public static final int SIM_NO_CHANGED = 4;
        public static final int SIM_READY = 3;
        private int mEventId;
        private QueryIccIdUserObj mUserObj;
        final /* synthetic */ SubscriptionInfoUpdater this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.SubscriptionInfoUpdater.SubscriptionUpdatorThread.<init>(com.android.internal.telephony.SubscriptionInfoUpdater, com.android.internal.telephony.SubscriptionInfoUpdater$QueryIccIdUserObj, int):void, dex: 
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
        SubscriptionUpdatorThread(com.android.internal.telephony.SubscriptionInfoUpdater r1, com.android.internal.telephony.SubscriptionInfoUpdater.QueryIccIdUserObj r2, int r3) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.SubscriptionInfoUpdater.SubscriptionUpdatorThread.<init>(com.android.internal.telephony.SubscriptionInfoUpdater, com.android.internal.telephony.SubscriptionInfoUpdater$QueryIccIdUserObj, int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.SubscriptionInfoUpdater.SubscriptionUpdatorThread.<init>(com.android.internal.telephony.SubscriptionInfoUpdater, com.android.internal.telephony.SubscriptionInfoUpdater$QueryIccIdUserObj, int):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: com.android.internal.telephony.SubscriptionInfoUpdater.SubscriptionUpdatorThread.run():void, dex: 
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
        public void run() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: com.android.internal.telephony.SubscriptionInfoUpdater.SubscriptionUpdatorThread.run():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.SubscriptionInfoUpdater.SubscriptionUpdatorThread.run():void");
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.SubscriptionInfoUpdater.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.SubscriptionInfoUpdater.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.SubscriptionInfoUpdater.<clinit>():void");
    }

    public SubscriptionInfoUpdater(Context context, Phone[] phone, CommandsInterface[] ci) {
        int i;
        super(IccPhoneBookInterfaceManager.mHandlerThread.getLooper(), null, true);
        this.mCis = null;
        this.mSubscriptionManager = null;
        this.rebroadcastIntentsOnUnlock = new HashMap();
        this.mIccRecords = new AtomicReferenceArray(PROJECT_SIM_NUM);
        this.mReadIccIdCount = 0;
        this.mLock = new Object();
        this.mLockRebc = new Object();
        this.mCommonSlotResetDone = false;
        this.needSubUpdate = false;
        this.isbootup = false;
        this.QueryingIccid = false;
        this.mBTrayPlugin = false;
        this.sReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                SubscriptionInfoUpdater.this.logd("[Receiver]+");
                String action = intent.getAction();
                SubscriptionInfoUpdater.this.logd("Action: " + action);
                if (action.equals("android.intent.action.USER_UNLOCKED")) {
                    synchronized (SubscriptionInfoUpdater.this.mLockRebc) {
                        Iterator iterator = SubscriptionInfoUpdater.this.rebroadcastIntentsOnUnlock.entrySet().iterator();
                        while (iterator.hasNext()) {
                            Entry pair = (Entry) iterator.next();
                            Intent i = (Intent) pair.getValue();
                            iterator.remove();
                            SubscriptionInfoUpdater.this.logd("Broadcasting intent ACTION_SIM_STATE_CHANGED for mCardIndex: " + pair.getKey());
                            ActivityManagerNative.broadcastStickyIntent(i, "android.permission.READ_PHONE_STATE", -1);
                        }
                    }
                    SubscriptionInfoUpdater.this.logd("[Receiver]-");
                } else if (action.equals("android.intent.action.SIM_STATE_CHANGED") || action.equals(IccCardProxy.ACTION_INTERNAL_SIM_STATE_CHANGED) || action.equals(IWorldPhone.ACTION_SHUTDOWN_IPO) || action.equals("com.mediatek.phone.ACTION_COMMON_SLOT_NO_CHANGED") || action.equals("android.intent.action.LOCALE_CHANGED")) {
                    int slotId = intent.getIntExtra("phone", -1);
                    SubscriptionInfoUpdater.this.logd("slotId: " + slotId);
                    if (slotId != -1 || (!action.equals("android.intent.action.SIM_STATE_CHANGED") && !action.equals(IccCardProxy.ACTION_INTERNAL_SIM_STATE_CHANGED))) {
                        String simStatus = intent.getStringExtra("ss");
                        SubscriptionInfoUpdater.this.logd("simStatus: " + simStatus);
                        if (action.equals("android.intent.action.SIM_STATE_CHANGED")) {
                            if ("ABSENT".equals(simStatus)) {
                                SubscriptionInfoUpdater.this.sendMessage(SubscriptionInfoUpdater.this.obtainMessage(4, slotId, -1));
                            } else if ("UNKNOWN".equals(simStatus)) {
                                SubscriptionInfoUpdater.this.sendMessage(SubscriptionInfoUpdater.this.obtainMessage(7, slotId, -1));
                            } else if ("CARD_IO_ERROR".equals(simStatus)) {
                                SubscriptionInfoUpdater.this.sendMessage(SubscriptionInfoUpdater.this.obtainMessage(6, slotId, -1));
                            } else if ("READY".equals(simStatus)) {
                                SubscriptionInfoUpdater.this.sendMessage(SubscriptionInfoUpdater.this.obtainMessage(100, slotId, -1));
                            } else if ("CARD_RESTRICTED".equals(simStatus)) {
                                SubscriptionInfoUpdater.this.sendMessage(SubscriptionInfoUpdater.this.obtainMessage(8, slotId, -1));
                            } else {
                                SubscriptionInfoUpdater.this.logd("Ignoring simStatus: " + simStatus);
                            }
                        } else if (action.equals(IccCardProxy.ACTION_INTERNAL_SIM_STATE_CHANGED)) {
                            if ("LOCKED".equals(simStatus)) {
                                SubscriptionInfoUpdater.this.sendMessage(SubscriptionInfoUpdater.this.obtainMessage(5, slotId, -1, intent.getStringExtra(DataSubSelector.EXTRA_MOBILE_DATA_ENABLE_REASON)));
                            } else if ("LOADED".equals(simStatus)) {
                                SubscriptionInfoUpdater.this.sendMessage(SubscriptionInfoUpdater.this.obtainMessage(3, slotId, -1));
                                SubscriptionInfoUpdater.this.mReadIccIdCount = 10;
                            } else {
                                SubscriptionInfoUpdater.this.logd("Ignoring simStatus: " + simStatus);
                            }
                        } else if (action.equals(IWorldPhone.ACTION_SHUTDOWN_IPO)) {
                            for (int i2 = 0; i2 < SubscriptionInfoUpdater.PROJECT_SIM_NUM; i2++) {
                                SubscriptionInfoUpdater.this.clearIccId(i2);
                            }
                            SubscriptionInfoUpdater.this.mSubscriptionManager.clearSubscriptionInfo();
                            SubscriptionController.getInstance().removeStickyIntent();
                        } else if (action.equals("android.intent.action.LOCALE_CHANGED")) {
                            for (int subId : SubscriptionInfoUpdater.this.mSubscriptionManager.getActiveSubscriptionIdList()) {
                                SubscriptionInfoUpdater.this.updateSubName(subId);
                            }
                        } else if (action.equals("com.mediatek.phone.ACTION_COMMON_SLOT_NO_CHANGED")) {
                            slotId = intent.getIntExtra("phone", -1);
                            SubscriptionInfoUpdater.this.logd("[Common Slot] NO_CHANTED, slotId: " + slotId);
                            SubscriptionInfoUpdater.this.sendMessage(SubscriptionInfoUpdater.this.obtainMessage(103, slotId, -1));
                        }
                        SubscriptionInfoUpdater.this.logd("[Receiver]-");
                    }
                }
            }
        };
        this.mReadIccIdPropertyRunnable = new Runnable() {
            public void run() {
                SubscriptionInfoUpdater subscriptionInfoUpdater = SubscriptionInfoUpdater.this;
                subscriptionInfoUpdater.mReadIccIdCount = subscriptionInfoUpdater.mReadIccIdCount + 1;
                if (SubscriptionInfoUpdater.this.mReadIccIdCount > 10) {
                    return;
                }
                if (SubscriptionInfoUpdater.this.checkAllIccIdReady()) {
                    SubscriptionInfoUpdater.this.updateSubscriptionInfoIfNeed();
                } else {
                    SubscriptionInfoUpdater.this.postDelayed(SubscriptionInfoUpdater.this.mReadIccIdPropertyRunnable, 1000);
                }
            }
        };
        this.mSSL = new AnonymousClass3(this);
        logd("Constructor invoked");
        mContext = context;
        mPhone = phone;
        this.mSubscriptionManager = SubscriptionManager.from(mContext);
        this.mPackageManager = IPackageManager.Stub.asInterface(ServiceManager.getService("package"));
        this.mUserManager = (UserManager) mContext.getSystemService("user");
        if (OemConstant.EXP_VERSION) {
            mOemLock = OemDeviceLock.getInstance(mContext);
        }
        sSubInfoUpdater = this;
        this.mCis = ci;
        for (i = 0; i < PROJECT_SIM_NUM; i++) {
            sIsUpdateAvailable[i] = 0;
            mIccId[i] = SystemProperties.get(PROPERTY_ICCID_SIM[i], UsimPBMemInfo.STRING_NOT_SET);
            if (mIccId[i].length() == 3) {
                logd("No SIM insert :" + i);
            }
            logd("mIccId[" + i + "]:" + SubscriptionInfo.givePrintableIccid(mIccId[i]));
        }
        if (isAllIccIdQueryDone()) {
            new AnonymousClass4(this).start();
        }
        IntentFilter intentFilter = new IntentFilter("android.intent.action.SIM_STATE_CHANGED");
        intentFilter.addAction(IccCardProxy.ACTION_INTERNAL_SIM_STATE_CHANGED);
        intentFilter.addAction(IWorldPhone.ACTION_SHUTDOWN_IPO);
        if ("OP09".equals(SystemProperties.get("persist.operator.optr")) && ("SEGDEFAULT".equals(SystemProperties.get("persist.operator.seg")) || "SEGC".equals(SystemProperties.get("persist.operator.seg")))) {
            intentFilter.addAction("android.intent.action.LOCALE_CHANGED");
        }
        for (i = 0; i < this.mCis.length; i++) {
            Integer index = new Integer(i);
            this.mCis[i].registerForNotAvailable(this, 102, index);
            this.mCis[i].registerForAvailable(this, 101, index);
            if (SystemProperties.get(COMMON_SLOT_PROPERTY).equals("1")) {
                this.mCis[i].registerForTrayPlugIn(this, 104, index);
                this.mCis[i].registerForSimPlugOut(this, 105, index);
            }
        }
        intentFilter.addAction("android.intent.action.USER_UNLOCKED");
        mContext.registerReceiver(this.sReceiver, intentFilter);
        this.mCarrierServiceBindHelper = new CarrierServiceBindHelper(mContext);
        initializeCarrierApps();
        IccCardProxy.registerSimStateListener(this.mSSL);
        ExpOperatorSwitchUtils.init(mContext);
    }

    private void initializeCarrierApps() {
        this.mCurrentlyActiveUserId = 0;
        try {
            ActivityManagerNative.getDefault().registerUserSwitchObserver(new AnonymousClass5(this), LOG_TAG);
            this.mCurrentlyActiveUserId = ActivityManagerNative.getDefault().getCurrentUser().id;
        } catch (RemoteException e) {
            logd("Couldn't get current user ID; guessing it's 0: " + e.getMessage());
        }
        CarrierAppUtils.disableCarrierAppsUntilPrivileged(mContext.getOpPackageName(), this.mPackageManager, TelephonyManager.getDefault(), mContext.getContentResolver(), this.mCurrentlyActiveUserId);
    }

    private boolean isAllIccIdQueryDone() {
        int i = 0;
        while (i < PROJECT_SIM_NUM) {
            if (mIccId[i] == null || mIccId[i].equals(UsimPBMemInfo.STRING_NOT_SET)) {
                logd("Wait for SIM" + (i + 1) + " IccId");
                return false;
            }
            i++;
        }
        logd("All IccIds query complete");
        return true;
    }

    public void setDisplayNameForNewSub(String newSubName, int subId, int newNameSource) {
        SubscriptionInfo subInfo = this.mSubscriptionManager.getActiveSubscriptionInfo(subId);
        if (subInfo != null) {
            int oldNameSource = subInfo.getNameSource();
            CharSequence oldSubName = subInfo.getDisplayName();
            logd("[setDisplayNameForNewSub] subId = " + subInfo.getSubscriptionId() + ", oldSimName = " + oldSubName + ", oldNameSource = " + oldNameSource + ", newSubName = " + newSubName + ", newNameSource = " + newNameSource);
            if (oldSubName == null || ((oldNameSource == 0 && newSubName != null) || !(oldNameSource != 1 || newSubName == null || newSubName.equals(oldSubName)))) {
                this.mSubscriptionManager.setDisplayName(newSubName, subInfo.getSubscriptionId(), (long) newNameSource);
                return;
            }
            return;
        }
        logd("SUB" + (subId + 1) + " SubInfo not created yet");
    }

    public void handleMessage(Message msg) {
        AsyncResult ar;
        Integer index;
        switch (msg.what) {
            case 1:
                ar = msg.obj;
                QueryIccIdUserObj uObj = ar.userObj;
                int slotId = uObj.slotId;
                logd("handleMessage : <EVENT_SIM_LOCKED_QUERY_ICCID_DONE> SIM" + (slotId + 1));
                if (ar.exception != null) {
                    if ((ar.exception instanceof CommandException) && ((CommandException) ar.exception).getCommandError() == Error.RADIO_NOT_AVAILABLE) {
                        mIccId[slotId] = UsimPBMemInfo.STRING_NOT_SET;
                    } else {
                        mIccId[slotId] = ICCID_STRING_FOR_NO_SIM;
                    }
                    logd("Query IccId fail: " + ar.exception);
                } else if (ar.result != null) {
                    byte[] data = ar.result;
                    mIccId[slotId] = IccUtils.bcdToString(data, 0, data.length);
                } else {
                    logd("Null ar");
                    mIccId[slotId] = ICCID_STRING_FOR_NO_SIM;
                }
                logd("sIccId[" + slotId + "] = " + mIccId[slotId]);
                ((TelephonyManager) mContext.getSystemService("phone")).setSimOperatorNameForPhone(slotId, UsimPBMemInfo.STRING_NOT_SET);
                if (isAllIccIdQueryDone()) {
                    updateSubscriptionInfoByIccId();
                }
                broadcastSimStateChanged(slotId, "LOCKED", uObj.reason);
                if (!ICCID_STRING_FOR_NO_SIM.equals(mIccId[slotId])) {
                    updateCarrierServices(slotId, "LOCKED");
                }
                new SubscriptionUpdatorThread(this, new QueryIccIdUserObj(uObj.reason, slotId), 2).start();
                return;
            case 2:
                ar = (AsyncResult) msg.obj;
                Integer slotId2 = ar.userObj;
                if (ar.exception != null || ar.result == null) {
                    logd("EVENT_GET_NETWORK_SELECTION_MODE_DONE: error getting network mode.");
                    return;
                } else if (ar.result[0] == 1) {
                    mPhone[slotId2.intValue()].setNetworkSelectionModeAutomatic(null);
                    return;
                } else {
                    return;
                }
            case 3:
                new SubscriptionUpdatorThread(this, new QueryIccIdUserObj(null, msg.arg1), 1).start();
                return;
            case 4:
                new SubscriptionUpdatorThread(this, new QueryIccIdUserObj(null, msg.arg1), 0).start();
                return;
            case 5:
                handleSimLocked(msg.arg1, (String) msg.obj);
                return;
            case 6:
                updateCarrierServices(msg.arg1, "CARD_IO_ERROR");
                return;
            case 7:
                updateCarrierServices(msg.arg1, "UNKNOWN");
                return;
            case 8:
                updateCarrierServices(msg.arg1, "CARD_RESTRICTED");
                return;
            case 30:
                String operatorVersion = SystemProperties.get("ro.oppo.operator", "OPPO");
                if (operatorVersion.equals("SGOP") || operatorVersion.equals("NZOP")) {
                    setOperatorConf(operatorVersion);
                    return;
                }
                return;
            case 100:
                new SubscriptionUpdatorThread(this, new QueryIccIdUserObj(null, msg.arg1), 3).start();
                return;
            case 101:
                index = getCiIndex(msg);
                logd("handleMessage : <EVENT_RADIO_AVAILABLE> SIM" + (index.intValue() + 1));
                sIsUpdateAvailable[index.intValue()] = 1;
                if (checkIsAvailable()) {
                    this.mReadIccIdCount = 0;
                    if (checkAllIccIdReady()) {
                        updateSubscriptionInfoIfNeed();
                        return;
                    }
                    postDelayed(this.mReadIccIdPropertyRunnable, 1000);
                    return;
                }
                return;
            case 102:
                index = getCiIndex(msg);
                logd("handleMessage : <EVENT_RADIO_UNAVAILABLE> SIM" + (index.intValue() + 1));
                sIsUpdateAvailable[index.intValue()] = 0;
                if (SystemProperties.get(COMMON_SLOT_PROPERTY).equals("1")) {
                    logd("[Common slot] reset mCommonSlotResetDone in EVENT_RADIO_UNAVAILABLE");
                    this.mCommonSlotResetDone = false;
                    return;
                }
                return;
            case 103:
                new SubscriptionUpdatorThread(this, new QueryIccIdUserObj(null, msg.arg1), 4).start();
                return;
            case 104:
                logd("[Common Slot] handle EVENT_TRAY_PLUG_IN " + this.mCommonSlotResetDone);
                if (!this.mCommonSlotResetDone) {
                    this.mCommonSlotResetDone = true;
                    for (int i = 0; i < PROJECT_SIM_NUM; i++) {
                        TelephonyManager.getDefault();
                        String vsimEnabled = TelephonyManager.getTelephonyProperty(i, "gsm.external.sim.enabled", "0");
                        if (vsimEnabled.length() == 0) {
                            vsimEnabled = "0";
                        }
                        logd("vsimEnabled[" + i + "]: (" + vsimEnabled + ")");
                        try {
                            if ("0".equals(vsimEnabled)) {
                                logd("[Common Slot] reset mIccId[" + i + "] to empty.");
                                mIccId[i] = UsimPBMemInfo.STRING_NOT_SET;
                            }
                        } catch (NumberFormatException e) {
                            logd("[Common Slot] NumberFormatException, reset mIccId[" + i + "] to empty.");
                            mIccId[i] = UsimPBMemInfo.STRING_NOT_SET;
                        }
                    }
                }
                this.mBTrayPlugin = true;
                return;
            case 105:
                logd("[Common Slot] handle EVENT_SIM_PLUG_OUT " + this.mCommonSlotResetDone);
                this.mCommonSlotResetDone = false;
                return;
            default:
                logd("Unknown msg:" + msg.what);
                return;
        }
    }

    private void handleSimLocked(int slotId, String reason) {
        IccFileHandler fileHandler = null;
        synchronized (this.mLock) {
            if (mIccId[slotId] != null && mIccId[slotId].equals(ICCID_STRING_FOR_NO_SIM)) {
                logd("SIM" + (slotId + 1) + " hot plug in");
                mIccId[slotId] = null;
            }
            if (mPhone[slotId].getIccCard() != null) {
                fileHandler = mPhone[slotId].getIccCard().getIccFileHandler();
            }
            if (fileHandler != null) {
                String iccId = mIccId[slotId];
                if (iccId == null || iccId.equals(UsimPBMemInfo.STRING_NOT_SET)) {
                    mIccId[slotId] = SystemProperties.get(PROPERTY_ICCID_SIM[slotId], UsimPBMemInfo.STRING_NOT_SET);
                    if (mIccId[slotId] == null || mIccId[slotId].equals(UsimPBMemInfo.STRING_NOT_SET)) {
                        logd("Querying IccId");
                        fileHandler.loadEFTransparent(IccConstants.EF_ICCID, obtainMessage(1, new QueryIccIdUserObj(reason, slotId)));
                    } else {
                        logd("Use Icc ID system property for performance enhancement");
                        new SubscriptionUpdatorThread(this, new QueryIccIdUserObj(reason, slotId), 2).start();
                    }
                } else {
                    logd("NOT Querying IccId its already set sIccid[" + slotId + "]=" + SubscriptionInfo.givePrintableIccid(iccId));
                    String tempIccid = SystemProperties.get(PROPERTY_ICCID_SIM[slotId], UsimPBMemInfo.STRING_NOT_SET);
                    logd("tempIccid:" + SubscriptionInfo.givePrintableIccid(tempIccid) + ", mIccId[slotId]:" + SubscriptionInfo.givePrintableIccid(mIccId[slotId]));
                    if (!(!MTK_FLIGHTMODE_POWEROFF_MD_SUPPORT || checkAllIccIdReady() || tempIccid.equals(mIccId[slotId]))) {
                        logd("All iccids are not ready and iccid changed");
                        mIccId[slotId] = null;
                        this.mSubscriptionManager.clearSubscriptionInfo();
                    }
                    updateCarrierServices(slotId, "LOCKED");
                    broadcastSimStateChanged(slotId, "LOCKED", reason);
                }
            } else {
                logd("sFh[" + slotId + "] is null, ignore");
            }
        }
    }

    private void handleSimLoaded(int slotId) {
        logd("handleSimStateLoadedInternal: slotId: " + slotId);
        boolean needUpdate = false;
        IccRecords records = mPhone[slotId].getIccCard().getIccRecords();
        if (records == null) {
            logd("onRecieve: IccRecords null");
        } else if (records.getIccId() == null) {
            logd("onRecieve: IccID null");
        } else {
            String iccId = SystemProperties.get(PROPERTY_ICCID_SIM[slotId], UsimPBMemInfo.STRING_NOT_SET);
            if (!iccId.equals(mIccId[slotId])) {
                logd("NeedUpdate");
                needUpdate = true;
                mIccId[slotId] = iccId;
            }
            if (isAllIccIdQueryDone() && needUpdate) {
                updateSubscriptionInfoByIccId();
            }
            int subId = Integer.MAX_VALUE;
            int[] subIds = SubscriptionController.getInstance().getSubId(slotId);
            if (subIds != null) {
                subId = subIds[0];
            }
            if (SubscriptionManager.isValidSubscriptionId(subId)) {
                String iccid;
                TelephonyManager tm = TelephonyManager.from(mContext);
                String operator = tm.getSimOperatorNumericForPhone(slotId);
                if (TextUtils.isEmpty(operator)) {
                    logd("EVENT_RECORDS_LOADED Operator name is null");
                } else {
                    if (subId == SubscriptionController.getInstance().getDefaultSubId()) {
                        MccTable.updateMccMncConfiguration(mContext, operator, false);
                    }
                    SubscriptionController.getInstance().setMccMnc(operator, subId);
                }
                String msisdn = tm.getLine1Number(subId);
                ContentResolver contentResolver = mContext.getContentResolver();
                if (msisdn != null) {
                    SubscriptionController.getInstance().setDisplayNumber(msisdn, subId, false);
                }
                SubscriptionInfo subInfo = this.mSubscriptionManager.getActiveSubscriptionInfo(subId);
                String simCarrierName = tm.getSimOperatorName(subId);
                ContentValues name = new ContentValues(1);
                if (!(subInfo == null || subInfo.getNameSource() == 2)) {
                    String nameToSet;
                    if (OemConstant.EXP_VERSION) {
                        nameToSet = SubscriptionController.getInstance().getExportSimDefaultName(slotId);
                    } else {
                        String imsi = TelephonyManager.getDefault().getSubscriberId(subId);
                        iccid = (mIccId == null || slotId >= mIccId.length || slotId < 0) ? UsimPBMemInfo.STRING_NOT_SET : mIccId[slotId];
                        int cardType = getCardType(imsi, iccid);
                        logd("[handleSimLoaded] cardType = " + cardType);
                        if (cardType == 1) {
                            nameToSet = SubscriptionController.getCarrierName(mContext, UsimPBMemInfo.STRING_NOT_SET, UsimPBMemInfo.STRING_NOT_SET, iccid, slotId);
                        } else {
                            nameToSet = SubscriptionController.getCarrierName(mContext, simCarrierName, imsi, iccid, slotId);
                        }
                    }
                    this.mSubscriptionManager.setDisplayName(nameToSet, subId);
                    logd("[handleSimLoaded] subId = " + subId + ", sim name = " + nameToSet);
                }
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
                if (sp.getInt(CURR_SUBID + slotId, -1) != subId) {
                    int networkType = Global.getInt(mPhone[slotId].getContext().getContentResolver(), "preferred_network_mode" + subId, RILConstants.PREFERRED_NETWORK_MODE);
                    logd("Possibly a new IMSI. Set sub(" + subId + ") networkType to " + networkType);
                    Rlog.d(LOG_TAG, "check persist.radio.lte.chip : " + SystemProperties.get("persist.radio.lte.chip"));
                    if (SystemProperties.get("persist.radio.lte.chip").equals("2") && (networkType == 8 || networkType == 9 || networkType == 10 || networkType == 11 || networkType == 12 || networkType == 15 || networkType == 17 || networkType == 19 || networkType == 20 || networkType == 22 || networkType == 30 || networkType == 31)) {
                        if (SystemProperties.get("ro.boot.opt_c2k_support").equals("1")) {
                            networkType = 7;
                        } else {
                            networkType = 0;
                        }
                        logd("Chip limit access 4G,modify PREFERRED_NETWORK_MODE init value to " + networkType + ",subId = " + subId);
                    }
                    Global.putInt(mPhone[slotId].getContext().getContentResolver(), "preferred_network_mode" + subId, networkType);
                    mPhone[slotId].getNetworkSelectionMode(obtainMessage(2, new Integer(slotId)));
                    Editor editor = sp.edit();
                    editor.putInt(CURR_SUBID + slotId, subId);
                    editor.apply();
                }
                String[] imsi2 = mPhone[slotId].getLteCdmaImsi(slotId);
                iccid = mPhone[slotId].getIccSerialNumber();
                if (UsimPBMemInfo.STRING_NOT_SET != imsi2[0]) {
                    setCardTypeInColor(subId, imsi2[0], iccid);
                } else {
                    setCardTypeInColor(subId, imsi2[1], iccid);
                }
                broadcastSubInfoUpdateIntent(CallLog.UNKNOWN_NUMBER, Integer.toString(subId), INTENT_VALUE_SIM_CARD_TYPE);
                if (OemConstant.EXP_VERSION) {
                    OemDeviceLock oemDeviceLock = mOemLock;
                    if (OemDeviceLock.IS_OP_LOCK && OemDeviceLock.getDeviceLockStatus()) {
                        if (allSimActived()) {
                            updateOperatorDeviceLock(true, true);
                        } else {
                            updateOperatorDeviceLock(false, true);
                        }
                    }
                }
            } else {
                logd("Invalid subId, could not update ContentResolver, send sim loaded.");
                SubscriptionController subCtrl = SubscriptionController.getInstance();
                if (subCtrl == null) {
                    logd("subCtrl is null");
                } else if (!subCtrl.isReady()) {
                    if (sSubInfoUpdater != null) {
                        SystemClock.sleep(100);
                        sSubInfoUpdater.sendMessage(obtainMessage(3, slotId, -1));
                    } else {
                        logd("sSubInfoUpdater is null");
                    }
                }
            }
            CarrierAppUtils.disableCarrierAppsUntilPrivileged(mContext.getOpPackageName(), this.mPackageManager, TelephonyManager.getDefault(), mContext.getContentResolver(), this.mCurrentlyActiveUserId);
            broadcastSimStateChanged(slotId, "LOADED", null);
            updateCarrierServices(slotId, "LOADED");
        }
    }

    private void updateCarrierServices(int slotId, String simState) {
        ((CarrierConfigManager) mContext.getSystemService("carrier_config")).updateConfigForPhoneId(slotId, simState);
        this.mCarrierServiceBindHelper.updateForPhoneId(slotId, simState);
    }

    private void handleSimAbsent(int slotId) {
        if (!(mIccId[slotId] == null || mIccId[slotId].equals(ICCID_STRING_FOR_NO_SIM))) {
            logd("SIM" + (slotId + 1) + " hot plug out");
        }
        TelephonyManager.setTelephonyProperty(slotId, "persist.radio.nitz_oper_code", UsimPBMemInfo.STRING_NOT_SET);
        TelephonyManager.setTelephonyProperty(slotId, "persist.radio.nitz_oper_lname", UsimPBMemInfo.STRING_NOT_SET);
        TelephonyManager.setTelephonyProperty(slotId, "persist.radio.nitz_oper_sname", UsimPBMemInfo.STRING_NOT_SET);
        if (mIccId[slotId] == null || !mIccId[slotId].equals(ICCID_STRING_FOR_NO_SIM)) {
            if (!SystemProperties.get(COMMON_SLOT_PROPERTY).equals("1")) {
                mIccId[slotId] = ICCID_STRING_FOR_NO_SIM;
                if (isAllIccIdQueryDone()) {
                    updateSubscriptionInfoByIccId();
                }
            } else if (checkAllIccIdReady()) {
                updateSubscriptionInfoIfNeed();
            }
            updateCarrierServices(slotId, "ABSENT");
            return;
        }
        logd("SIM" + (slotId + 1) + " absent - card state no changed.");
        updateCarrierServices(slotId, "ABSENT");
    }

    /* JADX WARNING: Removed duplicated region for block: B:141:0x053e  */
    /* JADX WARNING: Removed duplicated region for block: B:181:0x064a  */
    /* JADX WARNING: Removed duplicated region for block: B:183:0x0663  */
    /* JADX WARNING: Removed duplicated region for block: B:132:0x050f  */
    /* JADX WARNING: Removed duplicated region for block: B:144:0x055c  */
    /* JADX WARNING: Removed duplicated region for block: B:159:0x05e2  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private synchronized void updateSubscriptionInfoByIccId() {
        synchronized (this.mLock) {
            logd("updateSubscriptionInfoByIccId:+ Start");
            if (isAllIccIdQueryDone()) {
                int i;
                Intent intent;
                String operatroVersion;
                if (mHM == null) {
                    mHM = new HypnusManager();
                }
                if (mHM != null) {
                    mHM.hypnusSetAction(12, 30000);
                    logd("--David--hypnusSetAction()");
                }
                this.mCommonSlotResetDone = false;
                boolean skipCapabilitySwitch = false;
                for (i = 0; i < PROJECT_SIM_NUM; i++) {
                    mInsertSimState[i] = 0;
                    int simState = TelephonyManager.from(mContext).getSimState(i);
                    if (simState == 2 || simState == 3 || simState == 4 || simState == 6) {
                        logd("skipCapabilitySwitch = " + skipCapabilitySwitch);
                        skipCapabilitySwitch = true;
                    }
                }
                int insertedSimCount = PROJECT_SIM_NUM;
                for (i = 0; i < PROJECT_SIM_NUM; i++) {
                    if (ICCID_STRING_FOR_NO_SIM.equals(mIccId[i])) {
                        insertedSimCount--;
                        mInsertSimState[i] = -99;
                    }
                }
                logd("insertedSimCount = " + insertedSimCount);
                for (i = 0; i < PROJECT_SIM_NUM; i++) {
                    if (mInsertSimState[i] != -99) {
                        int index = 2;
                        for (int j = i + 1; j < PROJECT_SIM_NUM; j++) {
                            if (mInsertSimState[j] == 0) {
                                if (mIccId[i].equals(mIccId[j])) {
                                    mInsertSimState[i] = 1;
                                    mInsertSimState[j] = index;
                                    index++;
                                }
                            }
                        }
                    }
                }
                ContentResolver contentResolver = mContext.getContentResolver();
                String[] oldIccId = new String[PROJECT_SIM_NUM];
                for (i = 0; i < PROJECT_SIM_NUM; i++) {
                    oldIccId[i] = null;
                    List<SubscriptionInfo> oldSubInfo = SubscriptionController.getInstance().getSubInfoUsingSlotIdWithCheck(i, false, mContext.getOpPackageName());
                    if (oldSubInfo != null) {
                        oldIccId[i] = ((SubscriptionInfo) oldSubInfo.get(0)).getIccId();
                        logd("updateSubscriptionInfoByIccId: oldSubId = " + ((SubscriptionInfo) oldSubInfo.get(0)).getSubscriptionId());
                        if (mInsertSimState[i] == 0) {
                            if (!mIccId[i].equals(oldIccId[i])) {
                                mInsertSimState[i] = -1;
                            }
                        }
                        if (mInsertSimState[i] != 0) {
                            SubscriptionController.getInstance().clearSubInfoUsingPhoneId(i);
                            logd("updateSubscriptionInfoByIccId: clearSubInfoUsingPhoneId phoneId = " + i);
                            ContentValues value = new ContentValues(1);
                            value.put(TextBasedCbSmsColumns.SIM_ID, Integer.valueOf(-1));
                            contentResolver.update(SubscriptionManager.CONTENT_URI, value, "_id=" + Integer.toString(((SubscriptionInfo) oldSubInfo.get(0)).getSubscriptionId()), null);
                        }
                    } else {
                        if (mInsertSimState[i] == 0) {
                            mInsertSimState[i] = -1;
                        }
                        SubscriptionController.getInstance().clearSubInfoUsingPhoneId(i);
                        logd("updateSubscriptionInfoByIccId: clearSubInfoUsingPhoneId phoneId = " + i);
                        oldIccId[i] = ICCID_STRING_FOR_NO_SIM;
                        logd("updateSubscriptionInfoByIccId: No SIM in slot " + i + " last time");
                    }
                }
                for (i = 0; i < PROJECT_SIM_NUM; i++) {
                    logd("updateSubscriptionInfoByIccId: oldIccId[" + i + "] = " + SubscriptionInfo.givePrintableIccid(oldIccId[i]) + ", sIccId[" + i + "] = " + SubscriptionInfo.givePrintableIccid(mIccId[i]));
                }
                int nNewCardCount = 0;
                int nNewSimStatus = 0;
                for (i = 0; i < PROJECT_SIM_NUM; i++) {
                    if (mInsertSimState[i] == -99) {
                        logd("updateSubscriptionInfoByIccId: No SIM inserted in slot " + i + " this time");
                    } else {
                        if (mInsertSimState[i] > 0) {
                            this.mSubscriptionManager.addSubscriptionInfoRecord(mIccId[i] + Integer.toString(mInsertSimState[i]), i);
                            logd("SUB" + (i + 1) + " has invalid IccId");
                        } else {
                            this.mSubscriptionManager.addSubscriptionInfoRecord(mIccId[i], i);
                        }
                        if (isNewSim(mIccId[i], oldIccId)) {
                            nNewCardCount++;
                            switch (i) {
                                case 0:
                                    nNewSimStatus |= 1;
                                    break;
                                case 1:
                                    nNewSimStatus |= 2;
                                    break;
                                case 2:
                                    nNewSimStatus |= 4;
                                    break;
                            }
                            mInsertSimState[i] = -2;
                        }
                    }
                }
                for (i = 0; i < PROJECT_SIM_NUM; i++) {
                    if (mInsertSimState[i] == -1) {
                        mInsertSimState[i] = -3;
                    }
                    logd("updateSubscriptionInfoByIccId: sInsertSimState[" + i + "] = " + mInsertSimState[i]);
                }
                List<SubscriptionInfo> subInfos = this.mSubscriptionManager.getActiveSubscriptionInfoList();
                int nSubCount = subInfos == null ? 0 : subInfos.size();
                logd("updateSubscriptionInfoByIccId: nSubCount = " + nSubCount);
                for (i = 0; i < nSubCount; i++) {
                    SubscriptionInfo temp = (SubscriptionInfo) subInfos.get(i);
                    String msisdn = TelephonyManager.from(mContext).getLine1Number(temp.getSubscriptionId());
                    if (msisdn != null) {
                        SubscriptionController.getInstance().setDisplayNumber(msisdn, temp.getSubscriptionId(), false);
                    }
                }
                setAllDefaultSub(subInfos);
                boolean hasSimRemoved = false;
                for (i = 0; i < PROJECT_SIM_NUM; i++) {
                    if (mIccId[i] != null) {
                        if (mIccId[i].equals(ICCID_STRING_FOR_NO_SIM)) {
                            if (!oldIccId[i].equals(ICCID_STRING_FOR_NO_SIM)) {
                                hasSimRemoved = true;
                                intent = null;
                                if (nNewCardCount == 0) {
                                    logd("New SIM detected");
                                    intent = setUpdatedData(1, nSubCount, nNewSimStatus);
                                } else if (hasSimRemoved) {
                                    i = 0;
                                    while (i < PROJECT_SIM_NUM) {
                                        if (mInsertSimState[i] == -3) {
                                            logd("No new SIM detected and SIM repositioned");
                                            intent = setUpdatedData(3, nSubCount, nNewSimStatus);
                                            if (i == PROJECT_SIM_NUM) {
                                                logd("No new SIM detected and SIM removed");
                                                intent = setUpdatedData(2, nSubCount, nNewSimStatus);
                                            }
                                        } else {
                                            i++;
                                        }
                                    }
                                    if (i == PROJECT_SIM_NUM) {
                                    }
                                } else {
                                    i = 0;
                                    while (i < PROJECT_SIM_NUM) {
                                        if (mInsertSimState[i] == -3) {
                                            logd("No new SIM detected and SIM repositioned");
                                            intent = setUpdatedData(3, nSubCount, nNewSimStatus);
                                            if (i == PROJECT_SIM_NUM) {
                                                logd("[updateSimInfoByIccId] All SIM inserted into the same slot");
                                                intent = setUpdatedData(4, nSubCount, nNewSimStatus);
                                            }
                                        } else {
                                            i++;
                                        }
                                    }
                                    if (i == PROJECT_SIM_NUM) {
                                    }
                                }
                                if (PROJECT_SIM_NUM > 1) {
                                    logd("updateSubscriptionInfoByIccId  mBTrayPlugin:" + this.mBTrayPlugin);
                                    if (this.mBTrayPlugin) {
                                        skipCapabilitySwitch = true;
                                    }
                                    int dataSub = calculateDataSubId();
                                    if (skipCapabilitySwitch && UiccController.isHotSwapSimReboot()) {
                                        this.mSubscriptionManager.setDefaultDataSubIdWithoutCapabilitySwitch(dataSub);
                                    } else {
                                        this.mSubscriptionManager.setDefaultDataSubId(dataSub);
                                    }
                                }
                                SubscriptionController.getInstance().notifySubscriptionInfoChanged(intent);
                                operatroVersion = SystemProperties.get("ro.oppo.operator", "OPPO");
                                if (operatroVersion.equals("SGOP") || operatroVersion.equals("NZOP")) {
                                    setOperatorConf(operatroVersion);
                                }
                                if (OemConstant.EXP_VERSION) {
                                    OemDeviceLock oemDeviceLock = mOemLock;
                                    if (OemDeviceLock.IS_OP_LOCK && OemDeviceLock.getDeviceLockStatus()) {
                                        oemDeviceLock = mOemLock;
                                        if (OemDeviceLock.isSimBindingCompleted()) {
                                            updateDeviceLockUI();
                                            sHasInSertLockSim = hasInsertBindingSimCard();
                                            updateOperatorDeviceLockStatus(true, false);
                                        }
                                    }
                                }
                                logd("updateSubscriptionInfoByIccId:- SsubscriptionInfo update complete");
                                return;
                            }
                        } else {
                            continue;
                        }
                    }
                }
                intent = null;
                if (nNewCardCount == 0) {
                }
                if (PROJECT_SIM_NUM > 1) {
                }
                SubscriptionController.getInstance().notifySubscriptionInfoChanged(intent);
                operatroVersion = SystemProperties.get("ro.oppo.operator", "OPPO");
                setOperatorConf(operatroVersion);
                if (OemConstant.EXP_VERSION) {
                }
                logd("updateSubscriptionInfoByIccId:- SsubscriptionInfo update complete");
                return;
            }
        }
    }

    private Intent setUpdatedData(int detectedType, int subCount, int newSimStatus) {
        Intent intent = new Intent("android.intent.action.ACTION_SUBINFO_RECORD_UPDATED");
        logd("[setUpdatedData]+ ");
        if (detectedType == 1) {
            intent.putExtra(com.android.internal.telephony.cat.CatService.AnonymousClass4.INTENT_KEY_DETECT_STATUS, 1);
            intent.putExtra("simCount", subCount);
            intent.putExtra("newSIMSlot", newSimStatus);
        } else if (detectedType == 3) {
            intent.putExtra(com.android.internal.telephony.cat.CatService.AnonymousClass4.INTENT_KEY_DETECT_STATUS, 3);
            intent.putExtra("simCount", subCount);
        } else if (detectedType == 2) {
            intent.putExtra(com.android.internal.telephony.cat.CatService.AnonymousClass4.INTENT_KEY_DETECT_STATUS, 2);
            intent.putExtra("simCount", subCount);
        } else if (detectedType == 4) {
            intent.putExtra(com.android.internal.telephony.cat.CatService.AnonymousClass4.INTENT_KEY_DETECT_STATUS, 4);
        }
        logd("[setUpdatedData]- [" + detectedType + ", " + subCount + ", " + newSimStatus + "]");
        return intent;
    }

    private boolean isNewSim(String iccId, String[] oldIccId) {
        boolean newSim = true;
        int i = 0;
        while (i < PROJECT_SIM_NUM) {
            if (iccId != null && oldIccId[i] != null && oldIccId[i].indexOf(iccId) == 0) {
                newSim = false;
                break;
            }
            i++;
        }
        logd("newSim = " + newSim);
        return newSim;
    }

    private void broadcastSimStateChanged(int slotId, String state, String reason) {
        Intent i = new Intent("android.intent.action.SIM_STATE_CHANGED");
        i.addFlags(67108864);
        i.putExtra("phoneName", "Phone");
        i.putExtra("ss", state);
        i.putExtra(DataSubSelector.EXTRA_MOBILE_DATA_ENABLE_REASON, reason);
        SubscriptionManager.putPhoneIdAndSubIdExtra(i, slotId);
        logd("Broadcasting intent ACTION_SIM_STATE_CHANGED " + state + " reason " + reason + " for mCardIndex: " + slotId);
        ActivityManagerNative.broadcastStickyIntent(i, "android.permission.READ_PHONE_STATE", -1);
        synchronized (this.mLockRebc) {
            this.rebroadcastIntentsOnUnlock.put(Integer.valueOf(slotId), i);
        }
    }

    public void dispose() {
        logd("[dispose]");
        mContext.unregisterReceiver(this.sReceiver);
        IccCardProxy.unRegisterSimStateListener(this.mSSL);
    }

    private void logd(String message) {
        Rlog.d(LOG_TAG, message);
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("SubscriptionInfoUpdater:");
        this.mCarrierServiceBindHelper.dump(fd, pw, args);
    }

    private void setAllDefaultSub(List<SubscriptionInfo> subInfos) {
        logd("[setAllDefaultSub]+ ");
        DefaultSmsSimSettings.setSmsTalkDefaultSim(subInfos, mContext);
        logd("[setSmsTalkDefaultSim]- ");
        DefaultVoiceCallSubSettings.setVoiceCallDefaultSub(subInfos);
        logd("[setVoiceCallDefaultSub]- ");
    }

    private void clearIccId(int slotId) {
        synchronized (this.mLock) {
            logd("[clearIccId], slotId = " + slotId);
            sFh[slotId] = null;
            mIccId[slotId] = null;
        }
    }

    private boolean checkAllIccIdReady() {
        String iccId = UsimPBMemInfo.STRING_NOT_SET;
        logd("checkAllIccIdReady +, retry_count = " + this.mReadIccIdCount);
        for (int i = 0; i < PROJECT_SIM_NUM; i++) {
            iccId = SystemProperties.get(PROPERTY_ICCID_SIM[i], UsimPBMemInfo.STRING_NOT_SET);
            if (iccId.length() == 3) {
                logd("No SIM insert :" + i);
            }
            if (iccId == null || iccId.equals(UsimPBMemInfo.STRING_NOT_SET)) {
                return false;
            }
            logd("iccId[" + i + "] = " + SubscriptionInfo.givePrintableIccid(iccId));
        }
        return true;
    }

    private void updateSubscriptionInfoIfNeed() {
        logd("[updateSubscriptionInfoIfNeed]+");
        boolean needUpdate = false;
        int i = 0;
        while (i < PROJECT_SIM_NUM) {
            if (mIccId[i] == null || !mIccId[i].equals(SystemProperties.get(PROPERTY_ICCID_SIM[i], UsimPBMemInfo.STRING_NOT_SET))) {
                logd("[updateSubscriptionInfoIfNeed] icc id change, slot[" + i + "]");
                mIccId[i] = SystemProperties.get(PROPERTY_ICCID_SIM[i], UsimPBMemInfo.STRING_NOT_SET);
                needUpdate = true;
            }
            i++;
        }
        if (isAllIccIdQueryDone() && needUpdate) {
            new AnonymousClass6(this).start();
        }
        logd("[updateSubscriptionInfoIfNeed]- return: " + needUpdate);
    }

    private Integer getCiIndex(Message msg) {
        Integer index = new Integer(0);
        if (msg == null) {
            return index;
        }
        if (msg.obj != null && (msg.obj instanceof Integer)) {
            return msg.obj;
        }
        if (msg.obj == null || !(msg.obj instanceof AsyncResult)) {
            return index;
        }
        AsyncResult ar = msg.obj;
        if (ar.userObj == null || !(ar.userObj instanceof Integer)) {
            return index;
        }
        return ar.userObj;
    }

    private boolean checkIsAvailable() {
        boolean result = true;
        for (int i = 0; i < PROJECT_SIM_NUM; i++) {
            if (sIsUpdateAvailable[i] <= 0) {
                logd("sIsUpdateAvailable[" + i + "] = " + sIsUpdateAvailable[i]);
                result = false;
                break;
            }
        }
        logd("checkIsAvailable result = " + result);
        return result;
    }

    private void updateSubName(int subId) {
        SubscriptionInfo subInfo = this.mSubscriptionManager.getSubscriptionInfo(subId);
        if (subInfo != null && subInfo.getNameSource() != 2) {
            SpnOverride spnOverride = SpnOverride.getInstance();
            String carrierName = TelephonyManager.getDefault().getSimOperator(subId);
            int slotId = SubscriptionManager.getSlotId(subId);
            logd("updateSubName, carrierName = " + carrierName + ", subId = " + subId);
            if (SubscriptionManager.isValidSlotId(slotId)) {
                String nameToSet;
                if (spnOverride.containsCarrierEx(carrierName)) {
                    nameToSet = spnOverride.lookupOperatorName(subId, carrierName, true, mContext);
                    logd("SPN found, name = " + nameToSet);
                } else {
                    nameToSet = "CARD " + Integer.toString(slotId + 1);
                    logd("SPN not found, set name to " + nameToSet);
                }
                this.mSubscriptionManager.setDisplayName(nameToSet, subId);
            }
        }
    }

    public void broadcastSubInfoUpdateIntent(String slotid, String subid, String simstate) {
        Intent intent = new Intent(ACTION_SUBINFO_STATE_CHANGE);
        intent.putExtra(INTENT_KEY_SLOT_ID, slotid);
        intent.putExtra(INTENT_KEY_SUB_ID, subid);
        intent.putExtra(INTENT_KEY_SIM_STATE, simstate);
        logd("Broadcasting intent ACTION_SUBINFO_STATE_CHANGE slotid:" + slotid + " simstate:" + simstate + " subid:" + subid);
        mContext.sendBroadcast(intent);
    }

    private int getCardType(String imsi, String iccid) {
        int result = -1;
        logd("getCardType imsi : " + imsi + " iccid : " + iccid);
        if (imsi != null && imsi.length() > 5) {
            String mccmnc = imsi.substring(0, 5);
            if (mccmnc.equals("00101") || SystemProperties.getInt("persist.sys.oppo.ctlab", 0) == 1) {
                result = 9;
            } else if (mccmnc.equals("46003") || mccmnc.equals("46011") || mccmnc.equals("45502")) {
                result = 1;
            } else if (mccmnc.equals("46001") || mccmnc.equals("46009")) {
                result = 3;
            } else if (mccmnc.equals("46000") || mccmnc.equals("46002") || mccmnc.equals("46004") || mccmnc.equals("46007") || mccmnc.equals("46008")) {
                result = 2;
            }
            if (result != -1) {
                logd("getCardType by imsi result = " + result);
                return result;
            }
        }
        if (iccid != null && iccid.length() > 6 && result == -1) {
            String operator = iccid.substring(0, 6);
            if (operator.equals("898603") || operator.equals("898611")) {
                result = 1;
            } else if (operator.equals("898600") || operator.equals("898602") || operator.equals("898607")) {
                result = 2;
            } else if (operator.equals("898601") || operator.equals("898609")) {
                result = 3;
            } else {
                result = 4;
            }
        }
        logd("getCardType by iccid result = " + result);
        return result;
    }

    private int setCardTypeInColor(int subId, String imsi, String iccid) {
        logd("setCardTypeInColor + subId:" + subId + " imsi:" + imsi + " iccid:" + iccid);
        ContentValues value = new ContentValues(1);
        if (mContext == null || mContext.getContentResolver() == null || !SubscriptionManager.isValidSubscriptionId(subId)) {
            logd("setCardTypeInColor param error");
            return -1;
        }
        value.put(SimInfo.COLOR, Integer.valueOf(getCardType(imsi, iccid)));
        return mContext.getContentResolver().update(SubscriptionManager.CONTENT_URI, value, "_id=" + Integer.toString(subId), null);
    }

    private void setOperatorConf(String version) {
        if (ExpOperatorSwitchUtils.isFirstInsertSim()) {
            boolean isSpecOperator = false;
            boolean isInsertSim = false;
            for (int slot = 0; slot < TelephonyManager.getDefault().getPhoneCount(); slot++) {
                if (!mIccId[slot].equals(ICCID_STRING_FOR_NO_SIM)) {
                    IccRecords records = mPhone[slot].getIccCard().getIccRecords();
                    if (records != null) {
                        String operator = records.getOperatorNumeric();
                        if (operator != null) {
                            ExpOperatorSwitchUtils.setFirstInsertSimFlag(1);
                            isInsertSim = true;
                            Rlog.d(LOG_TAG, "setOperatorConf, slot = " + slot + " operator=" + operator);
                            if (ExpOperatorSwitchUtils.oppoIsSpecOperator(operator, slot, version)) {
                                isSpecOperator = true;
                                break;
                            }
                        }
                    }
                    sendMessageDelayed(obtainMessage(30), 3000);
                    return;
                }
            }
            String hotStatus = SystemProperties.get("com.oppo.sim_plug_in", "false");
            if (!isSpecOperator && isInsertSim && hotStatus.equals("true")) {
                ExpOperatorSwitchUtils.oppoBroadCastDelayHotswap();
            }
            return;
        }
        Rlog.d(LOG_TAG, "setOperatorConf, not first insert simcard!!");
    }

    public int calculateDataSubId() {
        SubscriptionManager subscriptionManager = this.mSubscriptionManager;
        int oldDatasubId = SubscriptionManager.getDefaultDataSubscriptionId();
        int calDataSubId = -1;
        int primarySlot = Global.getInt(mContext.getContentResolver(), SubscriptionController.OPPO_MULTI_SIM_NETWORK_PRIMARY_SLOT, 0);
        logd("calculateDataSubId oldDatasubId:" + oldDatasubId + ", primarySlot:" + primarySlot);
        SubscriptionController subController = SubscriptionController.getInstance();
        if (subController == null) {
            return oldDatasubId;
        }
        List<SubscriptionInfo> subList = subController.getActiveSubscriptionInfoList(mContext.getOpPackageName());
        if (subList != null) {
            int activeSimRef = 0;
            for (SubscriptionInfo si : subList) {
                if (1 == subController.getSubState(si.getSubscriptionId())) {
                    activeSimRef++;
                    if (si.getSimSlotIndex() == primarySlot) {
                        calDataSubId = si.getSubscriptionId();
                        logd("calculateDataSubId get subId:" + calDataSubId + " from primarySlot:" + primarySlot);
                        break;
                    } else if (-1 == calDataSubId) {
                        calDataSubId = si.getSubscriptionId();
                    }
                }
            }
            if (activeSimRef == 0 && 1 == subList.size()) {
                for (SubscriptionInfo si2 : subList) {
                    if (si2.getSimSlotIndex() == primarySlot) {
                        calDataSubId = si2.getSubscriptionId();
                        logd("calculateDataSubId activeSimRef == 0, calDataSubId:" + calDataSubId);
                        break;
                    }
                }
            }
        }
        if (calDataSubId == -1) {
            calDataSubId = oldDatasubId;
        }
        logd("calculateDataSubId return calDataSubId:" + calDataSubId);
        return calDataSubId;
    }

    private void updateDeviceLockUI() {
        boolean[] isSimInsert = new boolean[2];
        int slot = 0;
        while (slot < TelephonyManager.getDefault().getPhoneCount()) {
            if (mIccId[slot] == null || mIccId[slot].equals(ICCID_STRING_FOR_NO_SIM)) {
                isSimInsert[slot] = false;
            } else {
                isSimInsert[slot] = true;
            }
            slot++;
        }
        if (!isSimInsert[0] && !isSimInsert[0]) {
            OemDeviceLock oemDeviceLock = mOemLock;
            OemDeviceLock.setDeviceLockedForPhone(0, true);
            oemDeviceLock = mOemLock;
            OemDeviceLock.setDeviceLockedForPhone(1, true);
            oemDeviceLock = mOemLock;
            OemDeviceLock.setSimLoadedForPhone(true);
            oemDeviceLock = mOemLock;
            OemDeviceLock.setSimInsertForPhone(isSimInsert);
            oemDeviceLock = mOemLock;
            OemDeviceLock.notifyDeviceLocked(false);
        }
    }

    private boolean hasInsertBindingSimCard() {
        boolean[] lock = new boolean[4];
        int slot = 0;
        while (slot < TelephonyManager.getDefault().getPhoneCount()) {
            if (!(mIccId[slot] == null || mIccId[slot].equals(ICCID_STRING_FOR_NO_SIM))) {
                OemDeviceLock oemDeviceLock;
                if (mIccId[slot].length() >= 15) {
                    String simOperator = mIccId[slot].substring(0, 6);
                    oemDeviceLock = mOemLock;
                    lock = OemDeviceLock.isNeedAllowedOperator(simOperator, mIccId[slot], slot, false);
                }
                if (lock[0] && lock[1] && lock[2]) {
                    Rlog.d(LOG_TAG, "has insert locked sim card");
                    oemDeviceLock = mOemLock;
                    OemDeviceLock.setDeviceLockedForPhone(slot, false);
                    oemDeviceLock = mOemLock;
                    OemDeviceLock.setDeviceLockedSlot(slot);
                    return true;
                }
            }
            slot++;
        }
        return false;
    }

    private void updateOperatorDeviceLock(boolean simAllActived, boolean checkGid1OrSpn) {
        boolean[] isSimInsert = new boolean[2];
        OemDeviceLock oemDeviceLock = mOemLock;
        if (!OemDeviceLock.isSimBindingCompleted()) {
            firstBindingDeviceLock(checkGid1OrSpn);
        }
        updateOperatorDeviceLockStatus(simAllActived, checkGid1OrSpn);
    }

    private void firstBindingDeviceLock(boolean checkGid1OrSpn) {
        boolean[] lock = new boolean[4];
        String simOperator = null;
        int slot = 0;
        while (slot < TelephonyManager.getDefault().getPhoneCount()) {
            if (!(mIccId[slot] == null || mIccId[slot].equals(ICCID_STRING_FOR_NO_SIM))) {
                OemDeviceLock oemDeviceLock;
                if (mIccId[slot].length() >= 15) {
                    simOperator = mIccId[slot].substring(0, 6);
                    oemDeviceLock = mOemLock;
                    lock = OemDeviceLock.initOperatorDeviceLock(simOperator, mIccId[slot], slot, true);
                }
                boolean z = (lock[0] && lock[1]) ? lock[2] : false;
                Rlog.d(LOG_TAG, "firstBindingDeviceLock,success" + z + ",slotId = " + slot + ",simOperator = " + simOperator + ",mIccId[" + slot + "] = " + mIccId[slot]);
                if (z) {
                    oemDeviceLock = mOemLock;
                    OemDeviceLock.setDeviceLockedForPhone(slot, false);
                    oemDeviceLock = mOemLock;
                    OemDeviceLock.setDeviceLockedSlot(slot);
                    if (lock[3]) {
                        Rlog.d(LOG_TAG, "has binding operator success");
                        return;
                    }
                } else if (!(lock[0] || !lock[1] || lock[3])) {
                    Rlog.d(LOG_TAG, "firstBindingDeviceLock,first init locked fail");
                }
            }
            slot++;
        }
    }

    private void updateOperatorDeviceLockStatus(boolean allSimActived, boolean checkGid1OrSpn) {
        OemDeviceLock oemDeviceLock;
        boolean[] lock = new boolean[4];
        boolean[] isSimInsert = new boolean[2];
        boolean insertLockedSim = false;
        int currentSlot = -1;
        int slot = 0;
        while (slot < TelephonyManager.getDefault().getPhoneCount()) {
            if (!(mIccId[slot] == null || mIccId[slot].equals(ICCID_STRING_FOR_NO_SIM))) {
                String simOperator = null;
                if (mIccId[slot].length() >= 15) {
                    simOperator = mIccId[slot].substring(0, 6);
                    oemDeviceLock = mOemLock;
                    lock = OemDeviceLock.isNeedAllowedOperator(simOperator, mIccId[slot], slot, checkGid1OrSpn);
                }
                isSimInsert[slot] = true;
                Rlog.d(LOG_TAG, "updateOperatorDeviceLock,slot = " + slot + ",lock[0] = " + lock[0] + ",lock[1]  = " + lock[1] + ",lock[2] = " + lock[2] + ",simOperator = " + simOperator);
                if (lock[0] && lock[1] && lock[2]) {
                    Rlog.d(LOG_TAG, "has insert locked sim card");
                    insertLockedSim = true;
                    currentSlot = slot;
                    oemDeviceLock = mOemLock;
                    OemDeviceLock.setDeviceLockedForPhone(slot, false);
                    oemDeviceLock = mOemLock;
                    OemDeviceLock.setDeviceLockedSlot(slot);
                }
            }
            slot++;
        }
        if (!(allSimActived || insertLockedSim)) {
            insertLockedSim = sHasInSertLockSim;
            oemDeviceLock = mOemLock;
            currentSlot = OemDeviceLock.getDeviceLockedSlot();
        }
        int anotherSlot = currentSlot == 0 ? 1 : 0;
        if (!insertLockedSim || currentSlot == -1) {
            oemDeviceLock = mOemLock;
            if (OemDeviceLock.isNeedAllowedOperator(UsimPBMemInfo.STRING_NOT_SET, UsimPBMemInfo.STRING_NOT_SET, 0, false)[0]) {
                Rlog.d(LOG_TAG, "updateOperatorDeviceLock, has init locked,but not insert simcard");
            } else {
                Rlog.d(LOG_TAG, "updateOperatorDeviceLock has not init locked");
            }
            oemDeviceLock = mOemLock;
            OemDeviceLock.setDeviceLockedForPhone(0, true);
            oemDeviceLock = mOemLock;
            OemDeviceLock.setDeviceLockedForPhone(1, true);
        } else if (mIccId[anotherSlot] == null || mIccId[anotherSlot].equals(ICCID_STRING_FOR_NO_SIM)) {
            isSimInsert[anotherSlot] = false;
            oemDeviceLock = mOemLock;
            OemDeviceLock.setDeviceLockedForPhone(anotherSlot, true);
        } else {
            isSimInsert[anotherSlot] = true;
            if (mIccId[anotherSlot].length() >= 15) {
                oemDeviceLock = mOemLock;
                if (OemDeviceLock.isAllowSimCheck(anotherSlot, mIccId[anotherSlot].substring(0, 6), true)) {
                    oemDeviceLock = mOemLock;
                    OemDeviceLock.setDeviceLockedForPhone(anotherSlot, false);
                } else {
                    oemDeviceLock = mOemLock;
                    OemDeviceLock.setDeviceLockedForPhone(anotherSlot, true);
                }
            } else {
                oemDeviceLock = mOemLock;
                OemDeviceLock.setDeviceLockedForPhone(anotherSlot, true);
            }
        }
        oemDeviceLock = mOemLock;
        OemDeviceLock.setSimLoadedForPhone(true);
        oemDeviceLock = mOemLock;
        OemDeviceLock.setSimInsertForPhone(isSimInsert);
        oemDeviceLock = mOemLock;
        OemDeviceLock.notifyDeviceLocked(false);
        oemDeviceLock = mOemLock;
        OemDeviceLock.notifyUpdateDataCapacity(mPhone, isSimInsert);
        oemDeviceLock = mOemLock;
        OemDeviceLock.updateServiceState(isSimInsert);
    }

    private boolean allSimActived() {
        if (currentInsertSimCount() > SubscriptionController.getInstance().getActiveSubInfoCount(getClass().getPackage().getName())) {
            return false;
        }
        return true;
    }

    private int currentInsertSimCount() {
        for (int slot = 0; slot < TelephonyManager.getDefault().getPhoneCount(); slot++) {
            if (mPhone[slot].getIccCard().hasIccCard()) {
                int i = 0 + 1;
            }
        }
        return 0;
    }
}
