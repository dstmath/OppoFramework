package com.android.internal.telephony;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.provider.Settings.Global;
import android.telephony.RadioAccessFamily;
import android.telephony.Rlog;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import com.android.internal.telephony.CommandException.Error;
import com.android.internal.telephony.uicc.UiccController;
import com.mediatek.internal.telephony.ITelephonyEx;
import com.mediatek.internal.telephony.ITelephonyEx.Stub;
import com.mediatek.internal.telephony.RadioCapabilitySwitchUtil;
import com.mediatek.internal.telephony.RadioManager;
import com.mediatek.internal.telephony.uicc.UsimPBMemInfo;
import com.mediatek.internal.telephony.worldphone.WorldPhoneUtil;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

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
public class ProxyController {
    private static final int EVENT_APPLY_RC_RESPONSE = 3;
    private static final int EVENT_FINISH_RC_RESPONSE = 4;
    private static final int EVENT_NOTIFICATION_RC_CHANGED = 1;
    private static final int EVENT_RADIO_AVAILABLE = 6;
    private static final int EVENT_START_RC_RESPONSE = 2;
    private static final int EVENT_TIMEOUT = 5;
    static final String LOG_TAG = "ProxyController";
    private static final String MTK_C2K_SUPPORT = "ro.boot.opt_c2k_support";
    private static final int RC_RETRY_CAUSE_AIRPLANE_MODE = 5;
    private static final int RC_RETRY_CAUSE_CAPABILITY_SWITCHING = 2;
    private static final int RC_RETRY_CAUSE_IN_CALL = 3;
    private static final int RC_RETRY_CAUSE_NONE = 0;
    private static final int RC_RETRY_CAUSE_RADIO_UNAVAILABLE = 4;
    private static final int RC_RETRY_CAUSE_WORLD_MODE_SWITCHING = 1;
    private static final int SET_RC_STATUS_APPLYING = 3;
    private static final int SET_RC_STATUS_FAIL = 5;
    private static final int SET_RC_STATUS_IDLE = 0;
    private static final int SET_RC_STATUS_STARTED = 2;
    private static final int SET_RC_STATUS_STARTING = 1;
    private static final int SET_RC_STATUS_SUCCESS = 4;
    private static final int SET_RC_TIMEOUT_WAITING_MSEC = 45000;
    private static ProxyController sProxyController;
    protected BroadcastReceiver mBroadcastReceiver;
    private CommandsInterface[] mCi;
    private Context mContext;
    private String[] mCurrentLogicalModemIds;
    private long mDoSimSwitchTime;
    private BroadcastReceiver mEccStateReceiver;
    private Handler mHandler;
    private boolean mHasRegisterEccStateReceiver;
    private boolean mHasRegisterPhoneStateReceiver;
    private boolean mHasRegisterWorldModeReceiver;
    private boolean mIsCapSwitching;
    private String[] mNewLogicalModemIds;
    private int[] mNewRadioAccessFamily;
    RadioAccessFamily[] mNextRafs;
    private int[] mOldRadioAccessFamily;
    private BroadcastReceiver mPhoneStateReceiver;
    private PhoneSubInfoController mPhoneSubInfoController;
    private PhoneSwitcher mPhoneSwitcher;
    private Phone[] mPhones;
    private int mRadioAccessFamilyStatusCounter;
    private int mRadioCapabilitySessionId;
    private int[] mSetRadioAccessFamilyStatus;
    private int mSetRafRetryCause;
    private boolean mTransactionFailed;
    private UiccController mUiccController;
    private UiccPhoneBookController mUiccPhoneBookController;
    private UiccSmsController mUiccSmsController;
    private AtomicInteger mUniqueIdGenerator;
    WakeLock mWakeLock;
    private BroadcastReceiver mWorldModeReceiver;
    private int onExceptionCount;

    /* renamed from: com.android.internal.telephony.ProxyController$2 */
    class AnonymousClass2 extends BroadcastReceiver {
        final /* synthetic */ ProxyController this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.ProxyController.2.<init>(com.android.internal.telephony.ProxyController):void, dex: 
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
        AnonymousClass2(com.android.internal.telephony.ProxyController r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.ProxyController.2.<init>(com.android.internal.telephony.ProxyController):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.ProxyController.2.<init>(com.android.internal.telephony.ProxyController):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.ProxyController.2.onReceive(android.content.Context, android.content.Intent):void, dex: 
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
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.ProxyController.2.onReceive(android.content.Context, android.content.Intent):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.ProxyController.2.onReceive(android.content.Context, android.content.Intent):void");
        }
    }

    /* renamed from: com.android.internal.telephony.ProxyController$3 */
    class AnonymousClass3 extends BroadcastReceiver {
        final /* synthetic */ ProxyController this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.ProxyController.3.<init>(com.android.internal.telephony.ProxyController):void, dex: 
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
        AnonymousClass3(com.android.internal.telephony.ProxyController r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.ProxyController.3.<init>(com.android.internal.telephony.ProxyController):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.ProxyController.3.<init>(com.android.internal.telephony.ProxyController):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.ProxyController.3.onReceive(android.content.Context, android.content.Intent):void, dex: 
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
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.ProxyController.3.onReceive(android.content.Context, android.content.Intent):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.ProxyController.3.onReceive(android.content.Context, android.content.Intent):void");
        }
    }

    /* renamed from: com.android.internal.telephony.ProxyController$4 */
    class AnonymousClass4 extends BroadcastReceiver {
        final /* synthetic */ ProxyController this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.ProxyController.4.<init>(com.android.internal.telephony.ProxyController):void, dex: 
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
        AnonymousClass4(com.android.internal.telephony.ProxyController r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.ProxyController.4.<init>(com.android.internal.telephony.ProxyController):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.ProxyController.4.<init>(com.android.internal.telephony.ProxyController):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.ProxyController.4.onReceive(android.content.Context, android.content.Intent):void, dex: 
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
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.ProxyController.4.onReceive(android.content.Context, android.content.Intent):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.ProxyController.4.onReceive(android.content.Context, android.content.Intent):void");
        }
    }

    /* renamed from: com.android.internal.telephony.ProxyController$5 */
    class AnonymousClass5 extends BroadcastReceiver {
        final /* synthetic */ ProxyController this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.ProxyController.5.<init>(com.android.internal.telephony.ProxyController):void, dex: 
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
        AnonymousClass5(com.android.internal.telephony.ProxyController r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.ProxyController.5.<init>(com.android.internal.telephony.ProxyController):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.ProxyController.5.<init>(com.android.internal.telephony.ProxyController):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.internal.telephony.ProxyController.5.onReceive(android.content.Context, android.content.Intent):void, dex: 
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
        public void onReceive(android.content.Context r1, android.content.Intent r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.internal.telephony.ProxyController.5.onReceive(android.content.Context, android.content.Intent):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.ProxyController.5.onReceive(android.content.Context, android.content.Intent):void");
        }
    }

    public static ProxyController getInstance(Context context, Phone[] phone, UiccController uiccController, CommandsInterface[] ci, PhoneSwitcher ps) {
        if (sProxyController == null) {
            sProxyController = new ProxyController(context, phone, uiccController, ci, ps);
        }
        return sProxyController;
    }

    public static ProxyController getInstance() {
        return sProxyController;
    }

    private ProxyController(Context context, Phone[] phone, UiccController uiccController, CommandsInterface[] ci, PhoneSwitcher phoneSwitcher) {
        this.mTransactionFailed = false;
        this.mUniqueIdGenerator = new AtomicInteger(new Random().nextInt());
        this.mHasRegisterWorldModeReceiver = false;
        this.mHasRegisterPhoneStateReceiver = false;
        this.mHasRegisterEccStateReceiver = false;
        this.mNextRafs = null;
        this.onExceptionCount = 0;
        this.mDoSimSwitchTime = 0;
        this.mHandler = new Handler() {
            public void handleMessage(Message msg) {
                ProxyController.this.logd("handleMessage msg.what=" + msg.what);
                switch (msg.what) {
                    case 1:
                        ProxyController.this.onNotificationRadioCapabilityChanged(msg);
                        return;
                    case 2:
                        ProxyController.this.onStartRadioCapabilityResponse(msg);
                        return;
                    case 3:
                        ProxyController.this.onApplyRadioCapabilityResponse(msg);
                        return;
                    case 4:
                        ProxyController.this.onFinishRadioCapabilityResponse(msg);
                        return;
                    case 5:
                        ProxyController.this.onTimeoutRadioCapability(msg);
                        return;
                    case 6:
                        ProxyController.this.onRetryWhenRadioAvailable(msg);
                        return;
                    default:
                        return;
                }
            }
        };
        this.mBroadcastReceiver = new AnonymousClass2(this);
        this.mWorldModeReceiver = new AnonymousClass3(this);
        this.mPhoneStateReceiver = new AnonymousClass4(this);
        this.mEccStateReceiver = new AnonymousClass5(this);
        logd("Constructor - Enter");
        this.mContext = context;
        this.mPhones = phone;
        this.mUiccController = uiccController;
        this.mCi = ci;
        this.mPhoneSwitcher = phoneSwitcher;
        this.mUiccPhoneBookController = new UiccPhoneBookController(this.mPhones);
        this.mPhoneSubInfoController = new PhoneSubInfoController(this.mContext, this.mPhones);
        this.mUiccSmsController = new UiccSmsController(this.mPhones);
        this.mSetRadioAccessFamilyStatus = new int[this.mPhones.length];
        this.mNewRadioAccessFamily = new int[this.mPhones.length];
        this.mOldRadioAccessFamily = new int[this.mPhones.length];
        this.mCurrentLogicalModemIds = new String[this.mPhones.length];
        this.mNewLogicalModemIds = new String[this.mPhones.length];
        this.mWakeLock = ((PowerManager) context.getSystemService("power")).newWakeLock(1, LOG_TAG);
        this.mWakeLock.setReferenceCounted(false);
        clearTransaction();
        for (Phone registerForRadioCapabilityChanged : this.mPhones) {
            registerForRadioCapabilityChanged.registerForRadioCapabilityChanged(this.mHandler, 1, null);
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.AIRPLANE_MODE");
        context.registerReceiver(this.mBroadcastReceiver, filter);
        logd("Constructor - Exit");
    }

    public void updateDataConnectionTracker(int sub) {
        this.mPhones[sub].updateDataConnectionTracker();
    }

    public void enableDataConnectivity(int sub) {
        this.mPhones[sub].setInternalDataEnabled(true, null);
    }

    public void disableDataConnectivity(int sub, Message dataCleanedUpMsg) {
        this.mPhones[sub].setInternalDataEnabled(false, dataCleanedUpMsg);
    }

    public void updateCurrentCarrierInProvider(int sub) {
        this.mPhones[sub].updateCurrentCarrierInProvider();
    }

    public void registerForAllDataDisconnected(int subId, Handler h, int what, Object obj) {
        int phoneId = SubscriptionController.getInstance().getPhoneId(subId);
        if (phoneId >= 0 && phoneId < TelephonyManager.getDefault().getPhoneCount()) {
            this.mPhones[phoneId].registerForAllDataDisconnected(h, what, obj);
        }
    }

    public void unregisterForAllDataDisconnected(int subId, Handler h) {
        int phoneId = SubscriptionController.getInstance().getPhoneId(subId);
        if (phoneId >= 0 && phoneId < TelephonyManager.getDefault().getPhoneCount()) {
            this.mPhones[phoneId].unregisterForAllDataDisconnected(h);
        }
    }

    public boolean isDataDisconnected(int subId) {
        int phoneId = SubscriptionController.getInstance().getPhoneId(subId);
        if (phoneId < 0 || phoneId >= TelephonyManager.getDefault().getPhoneCount()) {
            return true;
        }
        return this.mPhones[phoneId].mDcTracker.isDisconnected();
    }

    public int getRadioAccessFamily(int phoneId) {
        if (phoneId >= this.mPhones.length) {
            return 1;
        }
        return this.mPhones[phoneId].getRadioAccessFamily();
    }

    /* JADX WARNING: Missing block: B:136:0x03a5, code:
            r13 = true;
            r6 = 0;
     */
    /* JADX WARNING: Missing block: B:138:0x03ac, code:
            if (r6 >= r19.mPhones.length) goto L_0x03c9;
     */
    /* JADX WARNING: Missing block: B:140:0x03c0, code:
            if (r19.mPhones[r6].getRadioAccessFamily() == r20[r6].getRadioAccessFamily()) goto L_0x03c3;
     */
    /* JADX WARNING: Missing block: B:141:0x03c2, code:
            r13 = false;
     */
    /* JADX WARNING: Missing block: B:142:0x03c3, code:
            r6 = r6 + 1;
     */
    /* JADX WARNING: Missing block: B:146:0x03c9, code:
            if (r13 == false) goto L_0x03da;
     */
    /* JADX WARNING: Missing block: B:147:0x03cb, code:
            logd("setRadioCapability: Already in requested configuration, nothing to do.");
            r19.mIsCapSwitching = false;
     */
    /* JADX WARNING: Missing block: B:148:0x03d9, code:
            return true;
     */
    /* JADX WARNING: Missing block: B:150:0x03de, code:
            if (com.mediatek.internal.telephony.worldphone.WorldPhoneUtil.isWorldModeSupport() != false) goto L_0x03ed;
     */
    /* JADX WARNING: Missing block: B:152:0x03e4, code:
            if (com.mediatek.internal.telephony.worldphone.WorldPhoneUtil.isWorldPhoneSupport() == false) goto L_0x03ed;
     */
    /* JADX WARNING: Missing block: B:153:0x03e6, code:
            com.android.internal.telephony.PhoneFactory.getWorldPhone().notifyRadioCapabilityChange(r12);
     */
    /* JADX WARNING: Missing block: B:154:0x03ed, code:
            r19.mWakeLock.acquire();
     */
    /* JADX WARNING: Missing block: B:155:0x03f8, code:
            return doSetRadioCapabilities(r20);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean setRadioCapability(RadioAccessFamily[] rafs) {
        if (rafs.length != this.mPhones.length) {
            throw new RuntimeException("Length of input rafs must equal to total phone count");
        } else if (SystemProperties.getBoolean("ro.mtk_disable_cap_switch", false)) {
            completeRadioCapabilityTransaction();
            logd("skip switching because mtk_disable_cap_switch is true");
            return true;
        } else {
            this.mNextRafs = rafs;
            if (WorldPhoneUtil.isWorldPhoneSwitching()) {
                logd("world mode switching");
                if (!this.mHasRegisterWorldModeReceiver) {
                    registerWorldModeReceiver();
                }
                this.mSetRafRetryCause = 1;
                return true;
            }
            if (this.mSetRafRetryCause == 1 && this.mHasRegisterWorldModeReceiver) {
                unRegisterWorldModeReceiver();
                this.mSetRafRetryCause = 0;
            }
            if (SystemProperties.getInt("gsm.gcf.testmode", 0) == 2) {
                completeRadioCapabilityTransaction();
                logd("skip switching because FTA mode");
                return true;
            } else if (SystemProperties.getInt("persist.radio.simswitch.emmode", 1) == 0) {
                completeRadioCapabilityTransaction();
                logd("skip switching because EM disable mode");
                return true;
            } else if (TelephonyManager.getDefault().getCallState() != 0) {
                logd("setCapability in calling, fail to set RAT for phones");
                if (!this.mHasRegisterPhoneStateReceiver) {
                    registerPhoneStateReceiver();
                }
                this.mSetRafRetryCause = 3;
                this.mNextRafs = rafs;
                return false;
            } else if (isEccInProgress()) {
                logd("setCapability in ECC, fail to set RAT for phones");
                if (!this.mHasRegisterEccStateReceiver) {
                    registerEccStateReceiver();
                }
                this.mSetRafRetryCause = 3;
                return false;
            } else {
                if (this.mSetRafRetryCause == 3) {
                    if (this.mHasRegisterPhoneStateReceiver) {
                        unRegisterPhoneStateReceiver();
                        this.mSetRafRetryCause = 0;
                    }
                    if (this.mHasRegisterEccStateReceiver) {
                        unRegisterEccStateReceiver();
                        this.mSetRafRetryCause = 0;
                    }
                }
                if (Global.getInt(this.mContext.getContentResolver(), "airplane_mode_on", 0) > 0) {
                    logd("airplane mode is on, fail to set RAT for phones");
                    this.mSetRafRetryCause = 5;
                    this.mNextRafs = rafs;
                    return false;
                } else if (this.mIsCapSwitching) {
                    logd("keep it and return,because capability swithing");
                    this.mSetRafRetryCause = 2;
                    this.mNextRafs = rafs;
                    return true;
                } else {
                    if (this.mSetRafRetryCause == 2) {
                        logd("setCapability, mIsCapSwitching is not switching, can switch");
                        this.mSetRafRetryCause = 0;
                    }
                    this.mIsCapSwitching = true;
                    int i = 0;
                    while (i < this.mPhones.length) {
                        if (this.mPhones[i].isRadioAvailable()) {
                            if (this.mSetRafRetryCause == 4) {
                                this.mCi[i].unregisterForAvailable(this.mHandler);
                                if (i == this.mPhones.length - 1) {
                                    this.mSetRafRetryCause = 0;
                                }
                            }
                            i++;
                        } else {
                            this.mSetRafRetryCause = 4;
                            this.mCi[i].registerForAvailable(this.mHandler, 6, null);
                            logd("setCapability fail,Phone" + i + " is not available");
                            this.mNextRafs = rafs;
                            this.mIsCapSwitching = false;
                            return false;
                        }
                    }
                    logd("setCapability,All Phones is available");
                    int switchStatus = Integer.valueOf(SystemProperties.get("persist.radio.simswitch", "1")).intValue();
                    boolean bIsboth3G = false;
                    int newMajorPhoneId = 0;
                    for (i = 0; i < rafs.length; i++) {
                        boolean bIsMajorPhone = false;
                        if ((rafs[i].getRadioAccessFamily() & 2) > 0) {
                            bIsMajorPhone = true;
                        }
                        if (bIsMajorPhone) {
                            newMajorPhoneId = rafs[i].getPhoneId();
                            if (newMajorPhoneId == switchStatus - 1) {
                                logd("no change, skip setRadioCapability");
                                this.mSetRafRetryCause = 0;
                                this.mNextRafs = null;
                                this.mIsCapSwitching = false;
                                completeRadioCapabilityTransaction();
                                return true;
                            } else if (bIsboth3G) {
                                logd("set more than one 3G phone, fail");
                                this.mIsCapSwitching = false;
                                throw new RuntimeException("input parameter is incorrect");
                            } else {
                                bIsboth3G = true;
                            }
                        }
                    }
                    if (bIsboth3G) {
                        if (SystemProperties.getInt("ro.mtk_external_sim_support", 0) == 1) {
                            i = 0;
                            while (i < this.mPhones.length) {
                                TelephonyManager.getDefault();
                                String isVsimEnabled = TelephonyManager.getTelephonyProperty(i, "gsm.external.sim.enabled", "0");
                                TelephonyManager.getDefault();
                                String isVsimInserted = TelephonyManager.getTelephonyProperty(i, "gsm.external.sim.inserted", "0");
                                int defaultPhoneId = SubscriptionManager.getPhoneId(SubscriptionManager.getDefaultDataSubscriptionId());
                                if (!"1".equals(isVsimEnabled) || (!("0".equals(isVsimInserted) || UsimPBMemInfo.STRING_NOT_SET.equals(isVsimInserted)) || newMajorPhoneId == defaultPhoneId)) {
                                    i++;
                                } else {
                                    this.mIsCapSwitching = false;
                                    throw new RuntimeException("vsim not ready, can't switch to another sim!");
                                }
                            }
                            int mainPhoneId = RadioCapabilitySwitchUtil.getMainCapabilityPhoneId();
                            TelephonyManager.getDefault();
                            String isVsimEnabledOnMain = TelephonyManager.getTelephonyProperty(mainPhoneId, "gsm.external.sim.enabled", "0");
                            TelephonyManager.getDefault();
                            String mainPhoneIdSimType = TelephonyManager.getTelephonyProperty(mainPhoneId, "gsm.external.sim.inserted", "0");
                            if (isVsimEnabledOnMain.equals("1") && mainPhoneIdSimType.equals("2")) {
                                this.mIsCapSwitching = false;
                                throw new RuntimeException("vsim enabled, can't switch to another sim!");
                            }
                        }
                        switch (RadioCapabilitySwitchUtil.isNeedSwitchInOpPackage(this.mPhones, rafs)) {
                            case 0:
                                logd("do setRadioCapability");
                                break;
                            case 1:
                                logd("no change in op check, skip setRadioCapability");
                                break;
                            case 2:
                                logd("Sim status/info is not ready, skip setRadioCapability");
                                this.mIsCapSwitching = false;
                                return true;
                            default:
                                logd("should not be here...!!");
                                this.mIsCapSwitching = false;
                                return true;
                        }
                        synchronized (this.mSetRadioAccessFamilyStatus) {
                            for (i = 0; i < this.mPhones.length; i++) {
                                if (this.mSetRadioAccessFamilyStatus[i] != 0) {
                                    loge("setRadioCapability: Phone[" + i + "] is not idle. Rejecting request.");
                                    this.mIsCapSwitching = false;
                                    return false;
                                }
                            }
                        }
                    } else {
                        this.mIsCapSwitching = false;
                        throw new RuntimeException("input parameter is incorrect - no 3g phone");
                    }
                }
            }
        }
    }

    private boolean doSetRadioCapabilities(RadioAccessFamily[] rafs) {
        this.mRadioCapabilitySessionId = this.mUniqueIdGenerator.getAndIncrement();
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(5, this.mRadioCapabilitySessionId, 0), 45000);
        this.mDoSimSwitchTime = System.currentTimeMillis() / 1000;
        SystemProperties.set("ril.time.stamp", Long.toString(this.mDoSimSwitchTime));
        SystemProperties.set("ril.switch.session.id", Integer.toString(this.mRadioCapabilitySessionId));
        logd("setRadioCapability: timestamp =" + this.mDoSimSwitchTime);
        this.mIsCapSwitching = true;
        synchronized (this.mSetRadioAccessFamilyStatus) {
            logd("setRadioCapability: new request session id=" + this.mRadioCapabilitySessionId);
            resetRadioAccessFamilyStatusCounter();
            this.onExceptionCount = 0;
            for (int i = 0; i < rafs.length; i++) {
                int phoneId = rafs[i].getPhoneId();
                this.mSetRadioAccessFamilyStatus[phoneId] = 1;
                this.mOldRadioAccessFamily[phoneId] = this.mPhones[phoneId].getRadioAccessFamily();
                int requestedRaf = rafs[i].getRadioAccessFamily();
                this.mNewRadioAccessFamily[phoneId] = requestedRaf;
                this.mCurrentLogicalModemIds[phoneId] = this.mPhones[phoneId].getModemUuId();
                this.mNewLogicalModemIds[phoneId] = getLogicalModemIdFromRaf(requestedRaf);
                logd("setRadioCapability: phoneId=" + phoneId + " status=STARTING" + "mOldRadioAccessFamily[" + phoneId + "]=" + this.mOldRadioAccessFamily[phoneId] + "mNewRadioAccessFamily[" + phoneId + "]=" + this.mNewRadioAccessFamily[phoneId]);
                sendRadioCapabilityRequest(phoneId, this.mRadioCapabilitySessionId, 1, this.mOldRadioAccessFamily[phoneId], this.mCurrentLogicalModemIds[phoneId], 0, 2);
            }
        }
        return true;
    }

    /* JADX WARNING: Missing block: B:41:0x012c, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void onStartRadioCapabilityResponse(Message msg) {
        synchronized (this.mSetRadioAccessFamilyStatus) {
            AsyncResult ar = msg.obj;
            int i;
            if (ar.exception != null) {
                if (this.onExceptionCount == 0) {
                    Error err = null;
                    this.onExceptionCount = 1;
                    if (ar.exception instanceof CommandException) {
                        err = ((CommandException) ar.exception).getCommandError();
                    }
                    if (err == Error.RADIO_NOT_AVAILABLE) {
                        this.mSetRafRetryCause = 4;
                        for (i = 0; i < this.mPhones.length; i++) {
                            this.mCi[i].registerForAvailable(this.mHandler, 6, null);
                        }
                        loge("onStartRadioCapabilityResponse: Retry later due to modem off");
                    }
                }
                logd("onStartRadioCapabilityResponse got exception=" + ar.exception);
                this.mRadioCapabilitySessionId = this.mUniqueIdGenerator.getAndIncrement();
                this.mContext.sendBroadcast(new Intent("android.intent.action.ACTION_SET_RADIO_CAPABILITY_FAILED"));
                clearTransaction();
                return;
            }
            RadioCapability rc = ((AsyncResult) msg.obj).result;
            if (rc == null || rc.getSession() != this.mRadioCapabilitySessionId) {
                logd("onStartRadioCapabilityResponse: Ignore session=" + this.mRadioCapabilitySessionId + " rc=" + rc);
                return;
            }
            this.mRadioAccessFamilyStatusCounter--;
            int id = rc.getPhoneId();
            if (((AsyncResult) msg.obj).exception != null) {
                logd("onStartRadioCapabilityResponse: Error response session=" + rc.getSession());
                logd("onStartRadioCapabilityResponse: phoneId=" + id + " status=FAIL");
                this.mSetRadioAccessFamilyStatus[id] = 5;
                this.mTransactionFailed = true;
            } else {
                logd("onStartRadioCapabilityResponse: phoneId=" + id + " status=STARTED");
                this.mSetRadioAccessFamilyStatus[id] = 2;
            }
            if (this.mRadioAccessFamilyStatusCounter == 0) {
                boolean z;
                StringBuilder append = new StringBuilder().append("onStartRadioCapabilityResponse: success=");
                if (this.mTransactionFailed) {
                    z = false;
                } else {
                    z = true;
                }
                logd(append.append(z).toString());
                if (this.mTransactionFailed) {
                    issueFinish(this.mRadioCapabilitySessionId);
                } else {
                    resetRadioAccessFamilyStatusCounter();
                    for (i = 0; i < this.mPhones.length; i++) {
                        sendRadioCapabilityRequest(i, this.mRadioCapabilitySessionId, 2, this.mNewRadioAccessFamily[i], this.mNewLogicalModemIds[i], 0, 3);
                        logd("onStartRadioCapabilityResponse: phoneId=" + i + " status=APPLYING");
                        this.mSetRadioAccessFamilyStatus[i] = 3;
                    }
                }
            }
        }
    }

    private void onApplyRadioCapabilityResponse(Message msg) {
        RadioCapability rc = ((AsyncResult) msg.obj).result;
        AsyncResult ar = msg.obj;
        Error err = null;
        if (rc == null || rc.getSession() != this.mRadioCapabilitySessionId) {
            if (rc == null && ar.exception != null && this.onExceptionCount == 0) {
                this.onExceptionCount = 1;
                if (ar.exception instanceof CommandException) {
                    err = ((CommandException) ar.exception).getCommandError();
                }
                if (err == Error.RADIO_NOT_AVAILABLE) {
                    this.mSetRafRetryCause = 4;
                    for (int i = 0; i < this.mPhones.length; i++) {
                        this.mCi[i].registerForAvailable(this.mHandler, 6, null);
                    }
                    loge("onApplyRadioCapabilityResponse: Retry later due to RADIO_NOT_AVAILABLE");
                } else {
                    loge("onApplyRadioCapabilityResponse: exception=" + ar.exception);
                }
                this.mRadioCapabilitySessionId = this.mUniqueIdGenerator.getAndIncrement();
                this.mContext.sendBroadcast(new Intent("android.intent.action.ACTION_SET_RADIO_CAPABILITY_FAILED"));
                clearTransaction();
            }
            logd("onApplyRadioCapabilityResponse: Ignore session=" + this.mRadioCapabilitySessionId + " rc=" + rc);
            return;
        }
        logd("onApplyRadioCapabilityResponse: rc=" + rc);
        if (((AsyncResult) msg.obj).exception != null) {
            synchronized (this.mSetRadioAccessFamilyStatus) {
                logd("onApplyRadioCapabilityResponse: Error response session=" + rc.getSession());
                int id = rc.getPhoneId();
                if (ar.exception instanceof CommandException) {
                    err = ((CommandException) ar.exception).getCommandError();
                }
                if (err == Error.RADIO_NOT_AVAILABLE) {
                    this.mSetRafRetryCause = 4;
                    this.mCi[id].registerForAvailable(this.mHandler, 6, null);
                    loge("onApplyRadioCapabilityResponse: Retry later due to modem off");
                } else {
                    loge("onApplyRadioCapabilityResponse: exception=" + ar.exception);
                }
                logd("onApplyRadioCapabilityResponse: phoneId=" + id + " status=FAIL");
                this.mSetRadioAccessFamilyStatus[id] = 5;
                this.mTransactionFailed = true;
            }
        } else {
            logd("onApplyRadioCapabilityResponse: Valid start expecting notification rc=" + rc);
        }
    }

    /* JADX WARNING: Missing block: B:28:0x00ef, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void onNotificationRadioCapabilityChanged(Message msg) {
        RadioCapability rc = ((AsyncResult) msg.obj).result;
        if (rc == null || rc.getSession() != this.mRadioCapabilitySessionId) {
            logd("onNotificationRadioCapabilityChanged: Ignore session=" + this.mRadioCapabilitySessionId + " rc=" + rc);
        } else if (this.mIsCapSwitching) {
            synchronized (this.mSetRadioAccessFamilyStatus) {
                logd("onNotificationRadioCapabilityChanged: rc=" + rc);
                if (rc.getSession() != this.mRadioCapabilitySessionId) {
                    logd("onNotificationRadioCapabilityChanged: Ignore session=" + this.mRadioCapabilitySessionId + " rc=" + rc);
                    return;
                }
                int id = rc.getPhoneId();
                if (((AsyncResult) msg.obj).exception != null || rc.getStatus() == 2) {
                    logd("onNotificationRadioCapabilityChanged: phoneId=" + id + " status=FAIL");
                    this.mSetRadioAccessFamilyStatus[id] = 5;
                    this.mTransactionFailed = true;
                } else {
                    logd("onNotificationRadioCapabilityChanged: phoneId=" + id + " status=SUCCESS");
                    this.mSetRadioAccessFamilyStatus[id] = 4;
                    this.mPhoneSwitcher.resendDataAllowed(id);
                    this.mPhones[id].radioCapabilityUpdated(rc);
                }
                this.mRadioAccessFamilyStatusCounter--;
                if (this.mRadioAccessFamilyStatusCounter == 0) {
                    logd("onNotificationRadioCapabilityChanged: APPLY URC success=" + this.mTransactionFailed);
                    issueFinish(this.mRadioCapabilitySessionId);
                }
            }
        } else {
            logd("radio change is not triggered by sim switch, notification should be ignore");
            clearTransaction();
        }
    }

    void onFinishRadioCapabilityResponse(Message msg) {
        RadioCapability rc = ((AsyncResult) msg.obj).result;
        if (rc != null && rc.getSession() == this.mRadioCapabilitySessionId) {
            synchronized (this.mSetRadioAccessFamilyStatus) {
                logd(" onFinishRadioCapabilityResponse mRadioAccessFamilyStatusCounter=" + this.mRadioAccessFamilyStatusCounter);
                this.mRadioAccessFamilyStatusCounter--;
                if (this.mRadioAccessFamilyStatusCounter == 0) {
                    completeRadioCapabilityTransaction();
                }
            }
        } else if (rc != null || ((AsyncResult) msg.obj).exception == null) {
            logd("onFinishRadioCapabilityResponse: Ignore session=" + this.mRadioCapabilitySessionId + " rc=" + rc);
        } else {
            synchronized (this.mSetRadioAccessFamilyStatus) {
                logd("onFinishRadioCapabilityResponse C2K mRadioAccessFamilyStatusCounter=" + this.mRadioAccessFamilyStatusCounter);
                this.mRadioAccessFamilyStatusCounter--;
                if (this.mRadioAccessFamilyStatusCounter == 0) {
                    completeRadioCapabilityTransaction();
                }
            }
        }
    }

    private void onTimeoutRadioCapability(Message msg) {
        if (msg.arg1 != this.mRadioCapabilitySessionId) {
            logd("RadioCapability timeout: Ignore msg.arg1=" + msg.arg1 + "!= mRadioCapabilitySessionId=" + this.mRadioCapabilitySessionId);
            return;
        }
        synchronized (this.mSetRadioAccessFamilyStatus) {
            for (int i = 0; i < this.mPhones.length; i++) {
                logd("RadioCapability timeout: mSetRadioAccessFamilyStatus[" + i + "]=" + this.mSetRadioAccessFamilyStatus[i]);
            }
            int uniqueDifferentId = this.mUniqueIdGenerator.getAndIncrement();
            this.mTransactionFailed = true;
            issueFinish(uniqueDifferentId);
        }
    }

    private void issueFinish(int sessionId) {
        synchronized (this.mSetRadioAccessFamilyStatus) {
            resetRadioAccessFamilyStatusCounter();
            for (int i = 0; i < this.mPhones.length; i++) {
                int i2;
                logd("issueFinish: phoneId=" + i + " sessionId=" + sessionId + " mTransactionFailed=" + this.mTransactionFailed);
                int i3 = this.mOldRadioAccessFamily[i];
                String str = this.mCurrentLogicalModemIds[i];
                if (this.mTransactionFailed) {
                    i2 = 2;
                } else {
                    i2 = 1;
                }
                sendRadioCapabilityRequest(i, sessionId, 4, i3, str, i2, 4);
                if (this.mTransactionFailed) {
                    logd("issueFinish: phoneId: " + i + " status: FAIL");
                    this.mSetRadioAccessFamilyStatus[i] = 5;
                }
            }
        }
    }

    private void completeRadioCapabilityTransaction() {
        Intent intent;
        logd("onFinishRadioCapabilityResponse: success=" + (!this.mTransactionFailed));
        if (this.mTransactionFailed) {
            intent = new Intent("android.intent.action.ACTION_SET_RADIO_CAPABILITY_FAILED");
            this.mTransactionFailed = false;
            if (retryToSetRadioCapabilityIfTimeout()) {
                this.mSetRafRetryCause = 2;
            } else {
                clearTransaction();
            }
        } else {
            ArrayList<RadioAccessFamily> phoneRAFList = new ArrayList();
            for (int i = 0; i < this.mPhones.length; i++) {
                int raf = this.mPhones[i].getRadioAccessFamily();
                logd("radioAccessFamily[" + i + "]=" + raf);
                phoneRAFList.add(new RadioAccessFamily(i, raf));
            }
            intent = new Intent("android.intent.action.ACTION_SET_RADIO_CAPABILITY_DONE");
            intent.putParcelableArrayListExtra("rafs", phoneRAFList);
            this.mRadioCapabilitySessionId = this.mUniqueIdGenerator.getAndIncrement();
            clearTransaction();
        }
        RadioCapabilitySwitchUtil.updateIccid(this.mPhones);
        this.mContext.sendBroadcast(intent, "android.permission.READ_PHONE_STATE");
        if (this.mNextRafs != null && this.mSetRafRetryCause == 2) {
            logd("has next capability switch request,trigger it");
            try {
                if (setRadioCapability(this.mNextRafs)) {
                    this.mSetRafRetryCause = 0;
                    this.mNextRafs = null;
                    return;
                }
                sendCapabilityFailBroadcast();
            } catch (RuntimeException e) {
                sendCapabilityFailBroadcast();
            }
        }
    }

    private void clearTransaction() {
        logd("clearTransaction mIsCapSwitching =" + this.mIsCapSwitching);
        if (this.mIsCapSwitching) {
            this.mHandler.removeMessages(5);
        }
        this.mIsCapSwitching = false;
        synchronized (this.mSetRadioAccessFamilyStatus) {
            for (int i = 0; i < this.mPhones.length; i++) {
                this.mSetRadioAccessFamilyStatus[i] = 0;
                this.mOldRadioAccessFamily[i] = 0;
                this.mNewRadioAccessFamily[i] = 0;
                this.mTransactionFailed = false;
            }
            logd("clearTransaction: All phones status=IDLE");
            if (this.mWakeLock.isHeld()) {
                this.mWakeLock.release();
            }
        }
    }

    private void resetRadioAccessFamilyStatusCounter() {
        this.mRadioAccessFamilyStatusCounter = this.mPhones.length;
    }

    private void sendRadioCapabilityRequest(int phoneId, int sessionId, int rcPhase, int radioFamily, String logicalModemId, int status, int eventId) {
        if (logicalModemId == null || logicalModemId.equals(UsimPBMemInfo.STRING_NOT_SET)) {
            logicalModemId = "modem_sys3";
        }
        this.mPhones[phoneId].setRadioCapability(new RadioCapability(phoneId, sessionId, rcPhase, radioFamily, logicalModemId, status), this.mHandler.obtainMessage(eventId));
    }

    public int getMaxRafSupported() {
        int[] numRafSupported = new int[this.mPhones.length];
        int maxRaf = 1;
        for (int len = 0; len < this.mPhones.length; len++) {
            if ((this.mPhones[len].getRadioAccessFamily() & 2) == 2) {
                maxRaf = this.mPhones[len].getRadioAccessFamily();
            }
        }
        logd("getMaxRafSupported: maxRafBit=" + 0 + " maxRaf=" + maxRaf + " flag=" + (maxRaf & 2));
        if (maxRaf == 1) {
            return maxRaf | 2;
        }
        return maxRaf;
    }

    public int getMinRafSupported() {
        int[] numRafSupported = new int[this.mPhones.length];
        int minRaf = 1;
        for (int len = 0; len < this.mPhones.length; len++) {
            if ((this.mPhones[len].getRadioAccessFamily() & 2) == 0) {
                minRaf = this.mPhones[len].getRadioAccessFamily();
            }
        }
        logd("getMinRafSupported: minRafBit=" + 0 + " minRaf=" + minRaf + " flag=" + (minRaf & 2));
        return minRaf;
    }

    private String getLogicalModemIdFromRaf(int raf) {
        for (int phoneId = 0; phoneId < this.mPhones.length; phoneId++) {
            if (this.mPhones[phoneId].getRadioAccessFamily() == raf) {
                return this.mPhones[phoneId].getModemUuId();
            }
        }
        return null;
    }

    private void logd(String string) {
        Rlog.d(LOG_TAG, string);
    }

    private void loge(String string) {
        Rlog.e(LOG_TAG, string);
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        try {
            this.mPhoneSwitcher.dump(fd, pw, args);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isCapabilitySwitching() {
        return this.mIsCapSwitching;
    }

    private boolean retryToSetRadioCapabilityIfTimeout() {
        int iRet = SystemProperties.getInt("ril.switch.result", 0);
        SystemProperties.set("ril.switch.result", "0");
        logd("retryToSetRadioCapabilityIfTimeout ret = " + iRet);
        return iRet == 1;
    }

    private void onRetryWhenRadioAvailable(Message msg) {
        logd("onRetryWhenRadioAvailable,mSetRafRetryCause:" + this.mSetRafRetryCause);
        for (int i = 0; i < this.mPhones.length; i++) {
            if (RadioManager.isModemPowerOff(i)) {
                logd("onRetryWhenRadioAvailable, Phone" + i + " modem off");
                return;
            }
        }
        if (this.mNextRafs != null && this.mSetRafRetryCause == 4) {
            try {
                setRadioCapability(this.mNextRafs);
            } catch (RuntimeException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendCapabilityFailBroadcast() {
        if (this.mContext != null) {
            this.mContext.sendBroadcast(new Intent("android.intent.action.ACTION_SET_RADIO_CAPABILITY_FAILED"));
        }
    }

    private void registerWorldModeReceiver() {
        if (this.mContext == null) {
            logd("registerWorldModeReceiver, context is null => return");
            return;
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.ACTION_WORLD_MODE_CHANGED");
        this.mContext.registerReceiver(this.mWorldModeReceiver, filter);
        this.mHasRegisterWorldModeReceiver = true;
    }

    private void unRegisterWorldModeReceiver() {
        if (this.mContext == null) {
            logd("unRegisterWorldModeReceiver, context is null => return");
            return;
        }
        this.mContext.unregisterReceiver(this.mWorldModeReceiver);
        this.mHasRegisterWorldModeReceiver = false;
    }

    private void registerPhoneStateReceiver() {
        if (this.mContext == null) {
            logd("registerPhoneStateReceiver, context is null => return");
            return;
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.PHONE_STATE");
        this.mContext.registerReceiver(this.mPhoneStateReceiver, filter);
        this.mHasRegisterPhoneStateReceiver = true;
    }

    private void unRegisterPhoneStateReceiver() {
        if (this.mContext == null) {
            logd("unRegisterPhoneStateReceiver, context is null => return");
            return;
        }
        this.mContext.unregisterReceiver(this.mPhoneStateReceiver);
        this.mHasRegisterPhoneStateReceiver = false;
    }

    private void registerEccStateReceiver() {
        if (this.mContext == null) {
            logd("registerEccStateReceiver, context is null => return");
            return;
        }
        IntentFilter filter = new IntentFilter("android.intent.action.ECC_IN_PROGRESS");
        filter.addAction("android.intent.action.EMERGENCY_CALLBACK_MODE_CHANGED");
        this.mContext.registerReceiver(this.mEccStateReceiver, filter);
        this.mHasRegisterEccStateReceiver = true;
    }

    private void unRegisterEccStateReceiver() {
        if (this.mContext == null) {
            logd("unRegisterEccStateReceiver, context is null => return");
            return;
        }
        this.mContext.unregisterReceiver(this.mEccStateReceiver);
        this.mHasRegisterEccStateReceiver = false;
    }

    private boolean isEccInProgress() {
        String value = SystemProperties.get("ril.cdma.inecmmode", UsimPBMemInfo.STRING_NOT_SET);
        boolean inEcm = value.contains("true");
        boolean isInEcc = false;
        ITelephonyEx telEx = Stub.asInterface(ServiceManager.getService("phoneEx"));
        if (telEx != null) {
            try {
                isInEcc = telEx.isEccInProgress();
            } catch (RemoteException e) {
                loge("Exception of isEccInProgress");
            }
        }
        logd("isEccInProgress, value:" + value + ", inEcm:" + inEcm + ", isInEcc:" + isInEcc);
        if (inEcm) {
            return true;
        }
        return isInEcc;
    }
}
