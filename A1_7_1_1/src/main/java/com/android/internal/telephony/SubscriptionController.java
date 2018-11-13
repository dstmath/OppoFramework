package com.android.internal.telephony;

import android.app.AppOpsManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncResult;
import android.os.Binder;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings.Global;
import android.provider.Settings.SettingNotFoundException;
import android.provider.oppo.Telephony.SimInfo;
import android.provider.oppo.Telephony.TextBasedCbSmsColumns;
import android.telephony.ColorOSTelephonyManager;
import android.telephony.OppoTelephonyFunction;
import android.telephony.PhoneStateListener;
import android.telephony.RadioAccessFamily;
import android.telephony.Rlog;
import android.telephony.SubInfoRecord;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.text.format.Time;
import android.util.Log;
import com.android.internal.telephony.ISub.Stub;
import com.android.internal.telephony.IccCardConstants.State;
import com.android.internal.telephony.cat.CatService.AnonymousClass4;
import com.android.internal.telephony.regionlock.RegionLockConstant;
import com.android.internal.telephony.uicc.UiccCard;
import com.mediatek.internal.telephony.RadioCapabilitySwitchUtil;
import com.mediatek.internal.telephony.RadioManager;
import com.mediatek.internal.telephony.dataconnection.DataSubSelector;
import com.mediatek.internal.telephony.uicc.UsimPBMemInfo;
import com.mediatek.internal.telephony.worldphone.IWorldPhone;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

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
public class SubscriptionController extends Stub {
    static final boolean DBG = false;
    static final boolean ENGDEBUG = false;
    private static final int EVENT_WRITE_MSISDN_DONE = 1;
    private static final String INVALID_ICCID = "N/A";
    static final String LOG_TAG = "SubscriptionController";
    static final int MAX_LOCAL_LOG_LINES = 500;
    private static final int MODE_PHONE1_ONLY = 1;
    public static final String OPPO_MULTI_SIM_NETWORK_PRIMARY_SLOT = "oppo_multi_sim_network_primary_slot";
    static final String PROPERTY_RIL_DATA_ICCID = "persist.radio.data.iccid";
    private static final boolean RADIO_POWER_OFF = false;
    private static final boolean RADIO_POWER_ON = true;
    static final boolean VDBG = false;
    private static int mDefaultFallbackSubId;
    private static int mDefaultPhoneId;
    private static boolean mHasSoftSimCard;
    private static int mSoftSimSlotId;
    private static SubscriptionController sInstance;
    protected static Phone[] sPhones;
    private static Map<Integer, Integer> sSlotIdxToSubId;
    private static Intent sStickyIntent;
    private String[] PROPERTY_ICCID;
    private int[] colorArr;
    private boolean inSwitchingDssState1;
    private boolean inSwitchingDssState2;
    private List<SubscriptionInfo> mActiveList;
    private AppOpsManager mAppOps;
    protected CallManager mCM;
    protected Context mContext;
    protected Handler mHandler;
    private boolean mIsOP01;
    private boolean mIsOP09A;
    private boolean mIsReady;
    private ScLocalLog mLocalLog;
    protected final Object mLock;
    private PhoneStateListener mPhoneStateListener_1;
    private PhoneStateListener mPhoneStateListener_2;
    protected boolean mSuccess;
    protected TelephonyManager mTelephonyManager;

    /* renamed from: com.android.internal.telephony.SubscriptionController$2 */
    class AnonymousClass2 implements Comparator<SubInfoRecord> {
        final /* synthetic */ SubscriptionController this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.SubscriptionController.2.<init>(com.android.internal.telephony.SubscriptionController):void, dex: 
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
        AnonymousClass2(com.android.internal.telephony.SubscriptionController r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.SubscriptionController.2.<init>(com.android.internal.telephony.SubscriptionController):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.SubscriptionController.2.<init>(com.android.internal.telephony.SubscriptionController):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.SubscriptionController.2.compare(android.telephony.SubInfoRecord, android.telephony.SubInfoRecord):int, dex: 
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
        public int compare(android.telephony.SubInfoRecord r1, android.telephony.SubInfoRecord r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.SubscriptionController.2.compare(android.telephony.SubInfoRecord, android.telephony.SubInfoRecord):int, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.SubscriptionController.2.compare(android.telephony.SubInfoRecord, android.telephony.SubInfoRecord):int");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.SubscriptionController.2.compare(java.lang.Object, java.lang.Object):int, dex: 
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
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.SubscriptionController.2.compare(java.lang.Object, java.lang.Object):int, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.SubscriptionController.2.compare(java.lang.Object, java.lang.Object):int");
        }
    }

    /* renamed from: com.android.internal.telephony.SubscriptionController$3 */
    class AnonymousClass3 implements Comparator<SubInfoRecord> {
        final /* synthetic */ SubscriptionController this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.SubscriptionController.3.<init>(com.android.internal.telephony.SubscriptionController):void, dex: 
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
        AnonymousClass3(com.android.internal.telephony.SubscriptionController r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.SubscriptionController.3.<init>(com.android.internal.telephony.SubscriptionController):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.SubscriptionController.3.<init>(com.android.internal.telephony.SubscriptionController):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.SubscriptionController.3.compare(android.telephony.SubInfoRecord, android.telephony.SubInfoRecord):int, dex: 
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
        public int compare(android.telephony.SubInfoRecord r1, android.telephony.SubInfoRecord r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.SubscriptionController.3.compare(android.telephony.SubInfoRecord, android.telephony.SubInfoRecord):int, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.SubscriptionController.3.compare(android.telephony.SubInfoRecord, android.telephony.SubInfoRecord):int");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.SubscriptionController.3.compare(java.lang.Object, java.lang.Object):int, dex: 
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
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.SubscriptionController.3.compare(java.lang.Object, java.lang.Object):int, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.SubscriptionController.3.compare(java.lang.Object, java.lang.Object):int");
        }
    }

    static class ScLocalLog {
        private LinkedList<String> mLog;
        private int mMaxLines;
        private Time mNow;

        public ScLocalLog(int maxLines) {
            this.mLog = new LinkedList();
            this.mMaxLines = maxLines;
            this.mNow = new Time();
        }

        public synchronized void log(String msg) {
            if (this.mMaxLines > 0) {
                int pid = Process.myPid();
                int tid = Process.myTid();
                this.mNow.setToNow();
                this.mLog.add(this.mNow.format("%m-%d %H:%M:%S") + " pid=" + pid + " tid=" + tid + " " + msg);
                while (this.mLog.size() > this.mMaxLines) {
                    this.mLog.remove();
                }
            }
        }

        public synchronized void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
            Iterator<String> itr = this.mLog.listIterator(0);
            int i = 0;
            while (true) {
                int i2 = i;
                if (itr.hasNext()) {
                    i = i2 + 1;
                    pw.println(Integer.toString(i2) + ": " + ((String) itr.next()));
                    if (i % 10 == 0) {
                        pw.flush();
                    }
                }
            }
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.internal.telephony.SubscriptionController.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.internal.telephony.SubscriptionController.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.SubscriptionController.<clinit>():void");
    }

    public static SubscriptionController init(Phone phone) {
        SubscriptionController subscriptionController;
        synchronized (SubscriptionController.class) {
            if (sInstance == null) {
                sInstance = new SubscriptionController(phone);
            } else {
                Log.wtf(LOG_TAG, "init() called multiple times!  sInstance = " + sInstance);
            }
            subscriptionController = sInstance;
        }
        return subscriptionController;
    }

    public static SubscriptionController init(Context c, CommandsInterface[] ci) {
        SubscriptionController subscriptionController;
        synchronized (SubscriptionController.class) {
            if (sInstance == null) {
                sInstance = new SubscriptionController(c);
            } else {
                Log.wtf(LOG_TAG, "init() called multiple times!  sInstance = " + sInstance);
            }
            subscriptionController = sInstance;
        }
        return subscriptionController;
    }

    public static SubscriptionController getInstance() {
        if (sInstance == null) {
            Log.wtf(LOG_TAG, "getInstance null");
        }
        return sInstance;
    }

    protected SubscriptionController(Context c) {
        this.mLocalLog = new ScLocalLog(MAX_LOCAL_LOG_LINES);
        this.mLock = new Object();
        this.mIsOP01 = false;
        this.mIsOP09A = false;
        this.mIsReady = false;
        this.mHandler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        AsyncResult ar = msg.obj;
                        synchronized (SubscriptionController.this.mLock) {
                            SubscriptionController.this.mSuccess = ar.exception == null;
                            if (SubscriptionController.DBG) {
                                SubscriptionController.this.logd("EVENT_WRITE_MSISDN_DONE, mSuccess = " + SubscriptionController.this.mSuccess);
                            }
                            SubscriptionController.this.mLock.notifyAll();
                        }
                        return;
                    default:
                        return;
                }
            }
        };
        String[] strArr = new String[4];
        strArr[0] = "ril.iccid.sim1";
        strArr[1] = "ril.iccid.sim2";
        strArr[2] = "ril.iccid.sim3";
        strArr[3] = "ril.iccid.sim4";
        this.PROPERTY_ICCID = strArr;
        this.inSwitchingDssState1 = false;
        this.inSwitchingDssState2 = false;
        init(c);
    }

    protected void init(Context c) {
        this.mContext = c;
        this.mCM = CallManager.getInstance();
        this.mTelephonyManager = TelephonyManager.from(this.mContext);
        this.mAppOps = (AppOpsManager) this.mContext.getSystemService("appops");
        String operator = SystemProperties.get("persist.operator.optr", IWorldPhone.NO_OP);
        if (operator.equals("OP01")) {
            this.mIsOP01 = true;
            this.colorArr = this.mContext.getResources().getIntArray(17235979);
        } else if (operator.equals("OP09") && "SEGDEFAULT".equals(SystemProperties.get("persist.operator.seg", UsimPBMemInfo.STRING_NOT_SET))) {
            this.mIsOP09A = true;
        }
        if (ServiceManager.getService("isub") == null) {
            ServiceManager.addService("isub", this);
        }
        if (DBG) {
            logdl("[SubscriptionController] init by Context");
        }
        if (this.mActiveList != null) {
            this.mActiveList.clear();
        }
    }

    private boolean isSubInfoReady() {
        return sSlotIdxToSubId.size() > 0;
    }

    private SubscriptionController(Phone phone) {
        this.mLocalLog = new ScLocalLog(MAX_LOCAL_LOG_LINES);
        this.mLock = new Object();
        this.mIsOP01 = false;
        this.mIsOP09A = false;
        this.mIsReady = false;
        this.mHandler = /* anonymous class already generated */;
        String[] strArr = new String[4];
        strArr[0] = "ril.iccid.sim1";
        strArr[1] = "ril.iccid.sim2";
        strArr[2] = "ril.iccid.sim3";
        strArr[3] = "ril.iccid.sim4";
        this.PROPERTY_ICCID = strArr;
        this.inSwitchingDssState1 = false;
        this.inSwitchingDssState2 = false;
        this.mContext = phone.getContext();
        this.mCM = CallManager.getInstance();
        this.mAppOps = (AppOpsManager) this.mContext.getSystemService(AppOpsManager.class);
        if (ServiceManager.getService("isub") == null) {
            ServiceManager.addService("isub", this);
        }
        if (DBG) {
            logdl("[SubscriptionController] init by Phone");
        }
    }

    private boolean canReadPhoneState(String callingPackage, String message) {
        boolean z = true;
        try {
            this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PRIVILEGED_PHONE_STATE", message);
            return true;
        } catch (SecurityException e) {
            this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PHONE_STATE", message);
            if (this.mAppOps.noteOp(51, Binder.getCallingUid(), callingPackage) != 0) {
                z = false;
            }
            return z;
        }
    }

    private void enforceModifyPhoneState(String message) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE", message);
    }

    private void broadcastSimInfoContentChanged(Intent intentExt) {
        Intent intent = new Intent("android.intent.action.ACTION_SUBINFO_CONTENT_CHANGE");
        intent.addFlags(536870912);
        this.mContext.sendBroadcast(intent);
        intent = new Intent("android.intent.action.ACTION_SUBINFO_RECORD_UPDATED");
        intent.addFlags(536870912);
        if (intentExt == null) {
            intent.putExtra(AnonymousClass4.INTENT_KEY_DETECT_STATUS, 4);
        }
        synchronized (this.mLock) {
            if (intentExt != null) {
                intent = intentExt;
            }
            sStickyIntent = intent;
            int detectedType = sStickyIntent.getIntExtra(AnonymousClass4.INTENT_KEY_DETECT_STATUS, 0);
            if (ENGDEBUG) {
                logd("broadcast intent ACTION_SUBINFO_RECORD_UPDATED with detectType:" + detectedType);
            }
            this.mContext.sendStickyBroadcast(sStickyIntent);
        }
    }

    public void notifySubscriptionInfoChanged() {
        try {
            ITelephonyRegistry.Stub.asInterface(ServiceManager.getService("telephony.registry")).notifySubscriptionInfoChanged();
        } catch (RemoteException e) {
        }
        broadcastSimInfoContentChanged(null);
    }

    private SubscriptionInfo getSubInfoRecord(Cursor cursor) {
        int id = cursor.getInt(cursor.getColumnIndexOrThrow("_id"));
        String iccId = cursor.getString(cursor.getColumnIndexOrThrow(SimInfo.ICC_ID));
        int simSlotIndex = cursor.getInt(cursor.getColumnIndexOrThrow(TextBasedCbSmsColumns.SIM_ID));
        String displayName = cursor.getString(cursor.getColumnIndexOrThrow(SimInfo.DISPLAY_NAME));
        String carrierName = cursor.getString(cursor.getColumnIndexOrThrow("carrier_name"));
        int nameSource = cursor.getInt(cursor.getColumnIndexOrThrow(SimInfo.NAME_SOURCE));
        int iconTint = cursor.getInt(cursor.getColumnIndexOrThrow(SimInfo.COLOR));
        String number = cursor.getString(cursor.getColumnIndexOrThrow("number"));
        int dataRoaming = cursor.getInt(cursor.getColumnIndexOrThrow(SimInfo.DATA_ROAMING));
        Bitmap iconBitmap = BitmapFactory.decodeResource(this.mContext.getResources(), 17302585);
        int mcc = cursor.getInt(cursor.getColumnIndexOrThrow("mcc"));
        int mnc = cursor.getInt(cursor.getColumnIndexOrThrow("mnc"));
        String countryIso = getSubscriptionCountryIso(id);
        int mStatus = cursor.getInt(cursor.getColumnIndexOrThrow("sub_state"));
        if (VDBG) {
            logd("[getSubInfoRecord] id:" + id + " iccid:" + SubscriptionInfo.givePrintableIccid(iccId) + " simSlotIndex:" + simSlotIndex + " displayName:" + displayName + " nameSource:" + nameSource + " iconTint:" + iconTint + " dataRoaming:" + dataRoaming + " mcc:" + mcc + " mnc:" + mnc + " countIso:" + countryIso);
        }
        String line1Number = this.mTelephonyManager.getLine1Number(id);
        if (!(TextUtils.isEmpty(line1Number) || line1Number.equals(number))) {
            number = line1Number;
        }
        return new SubscriptionInfo(id, iccId, simSlotIndex, displayName, carrierName, nameSource, iconTint, number, dataRoaming, iconBitmap, mcc, mnc, countryIso, mStatus, -1);
    }

    private String getSubscriptionCountryIso(int subId) {
        int phoneId = getPhoneId(subId);
        if (phoneId < 0) {
            return UsimPBMemInfo.STRING_NOT_SET;
        }
        return this.mTelephonyManager.getSimCountryIsoForPhone(phoneId);
    }

    /* JADX WARNING: Removed duplicated region for block: B:24:0x00a3  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private List<SubscriptionInfo> getSubInfo(String selection, Object queryKey) {
        Throwable th;
        if (VDBG) {
            logd("selection:" + selection + " " + queryKey);
        }
        String[] strArr = null;
        if (queryKey != null) {
            strArr = new String[1];
            strArr[0] = queryKey.toString();
        }
        List<SubscriptionInfo> subList = null;
        Cursor cursor = this.mContext.getContentResolver().query(SubscriptionManager.CONTENT_URI, null, selection, strArr, "sim_id ASC");
        if (cursor != null) {
            ArrayList<SubscriptionInfo> subList2;
            loop0:
            while (true) {
                while (true) {
                    try {
                        ArrayList<SubscriptionInfo> subList3;
                        subList2 = subList3;
                        if (!cursor.moveToNext()) {
                            break loop0;
                        }
                        SubscriptionInfo subInfo = getSubInfoRecord(cursor);
                        if (subInfo == null) {
                            subList3 = subList2;
                            break;
                        }
                        if (subList2 == null) {
                            subList3 = new ArrayList();
                        } else {
                            subList3 = subList2;
                        }
                        try {
                            int slotID = subInfo.getSimSlotIndex();
                            logd("getSubInfo slotID is:" + slotID);
                            if (!OemConstant.isUiccSlotForbid(slotID)) {
                                subList3.add(subInfo);
                                break;
                            }
                            logd("getSubInfo the " + slotID + " is forbid");
                        } catch (Throwable th2) {
                            th = th2;
                            if (cursor != null) {
                            }
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        if (cursor != null) {
                            cursor.close();
                        }
                        throw th;
                    }
                }
            }
            Object subList4 = subList2;
        } else if (DBG) {
            logd("Query fail");
        }
        if (cursor != null) {
            cursor.close();
        }
        return subList4;
    }

    private int getUnusedColor(String callingPackage) {
        List<SubscriptionInfo> availableSubInfos = getActiveSubscriptionInfoList(callingPackage);
        this.colorArr = this.mContext.getResources().getIntArray(17235979);
        int colorIdx = 0;
        if (availableSubInfos != null) {
            int i = 0;
            while (i < this.colorArr.length) {
                int j = 0;
                while (j < availableSubInfos.size() && this.colorArr[i] != ((SubscriptionInfo) availableSubInfos.get(j)).getIconTint()) {
                    j++;
                }
                if (j == availableSubInfos.size()) {
                    return this.colorArr[i];
                }
                i++;
            }
            colorIdx = availableSubInfos.size() % this.colorArr.length;
        }
        return this.colorArr[colorIdx];
    }

    public SubscriptionInfo getActiveSubscriptionInfo(int subId, String callingPackage) {
        if (!canReadPhoneState(callingPackage, "getActiveSubscriptionInfo")) {
            return null;
        }
        long identity = Binder.clearCallingIdentity();
        try {
            List<SubscriptionInfo> subList = getActiveSubscriptionInfoList(this.mContext.getOpPackageName());
            if (subList != null) {
                for (SubscriptionInfo si : subList) {
                    if (si.getSubscriptionId() == subId) {
                        if (DBG) {
                            logd("[getActiveSubscriptionInfo]+ subId=" + subId + " subInfo=" + si);
                        }
                        Binder.restoreCallingIdentity(identity);
                        return si;
                    }
                }
            }
            if (DBG) {
                logd("[getActiveSubInfoForSubscriber]- subId=" + subId + " subList=" + subList + " subInfo=null");
            }
            Binder.restoreCallingIdentity(identity);
            return null;
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(identity);
        }
    }

    public SubscriptionInfo getActiveSubscriptionInfoForIccId(String iccId, String callingPackage) {
        if (!canReadPhoneState(callingPackage, "getActiveSubscriptionInfoForIccId")) {
            return null;
        }
        long identity = Binder.clearCallingIdentity();
        try {
            List<SubscriptionInfo> subList = getActiveSubscriptionInfoList(this.mContext.getOpPackageName());
            if (subList != null) {
                for (SubscriptionInfo si : subList) {
                    if (si.getIccId() == iccId) {
                        if (DBG) {
                            logd("[getActiveSubInfoUsingIccId]+ iccId=" + iccId + " subInfo=" + si);
                        }
                        Binder.restoreCallingIdentity(identity);
                        return si;
                    }
                }
            }
            if (DBG) {
                logd("[getActiveSubInfoUsingIccId]+ iccId=" + iccId + " subList=" + subList + " subInfo=null");
            }
            Binder.restoreCallingIdentity(identity);
            return null;
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(identity);
        }
    }

    public SubscriptionInfo getActiveSubscriptionInfoForSimSlotIndex(int slotIdx, String callingPackage) {
        if (!canReadPhoneState(callingPackage, "getActiveSubscriptionInfoForSimSlotIndex")) {
            return null;
        }
        long identity = Binder.clearCallingIdentity();
        try {
            List<SubscriptionInfo> subList = getActiveSubscriptionInfoList(this.mContext.getOpPackageName());
            if (subList != null) {
                for (SubscriptionInfo si : subList) {
                    if (si.getSimSlotIndex() == slotIdx) {
                        if (DBG) {
                            logd("[getActiveSubscriptionInfoForSimSlotIndex]+ slotIdx=" + slotIdx + " subId=" + si);
                        }
                        Binder.restoreCallingIdentity(identity);
                        return si;
                    }
                }
                if (DBG) {
                    logd("[getActiveSubscriptionInfoForSimSlotIndex]+ slotIdx=" + slotIdx + " subId=null");
                }
            } else if (DBG) {
                logd("[getActiveSubscriptionInfoForSimSlotIndex]+ subList=null");
            }
            Binder.restoreCallingIdentity(identity);
            return null;
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(identity);
        }
    }

    public List<SubscriptionInfo> getAllSubInfoList(String callingPackage) {
        if (DBG) {
            logd("[getAllSubInfoList]+");
        }
        if (!canReadPhoneState(callingPackage, "getAllSubInfoList")) {
            return null;
        }
        long identity = Binder.clearCallingIdentity();
        try {
            List<SubscriptionInfo> subList = getSubInfo(null, null);
            if (subList != null) {
                if (DBG) {
                    logd("[getAllSubInfoList]- " + subList.size() + " infos return");
                }
            } else if (DBG) {
                logd("[getAllSubInfoList]- no info return");
            }
            Binder.restoreCallingIdentity(identity);
            return subList;
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(identity);
        }
    }

    public List<SubscriptionInfo> getActiveSubscriptionInfoList(String callingPackage) {
        if (!canReadPhoneState(callingPackage, "getActiveSubscriptionInfoList")) {
            return null;
        }
        long identity = Binder.clearCallingIdentity();
        try {
            if (isSubInfoReady()) {
                List<SubscriptionInfo> subList = getSubInfo("sim_id>=0", null);
                if (subList != null) {
                    if (VDBG) {
                        logdl("[getActiveSubInfoList]- " + subList.size() + " infos return");
                    }
                } else if (DBG) {
                    logdl("[getActiveSubInfoList]- no info return");
                }
                Binder.restoreCallingIdentity(identity);
                return subList;
            }
            if (DBG) {
                logdl("[getActiveSubInfoList] Sub Controller not ready");
            }
            Binder.restoreCallingIdentity(identity);
            return null;
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(identity);
        }
    }

    public int getActiveSubInfoCount(String callingPackage) {
        if (DBG) {
            logd("[getActiveSubInfoCount]+");
        }
        if (!canReadPhoneState(callingPackage, "getActiveSubInfoCount")) {
            return 0;
        }
        long identity = Binder.clearCallingIdentity();
        try {
            List<SubscriptionInfo> records = getActiveSubscriptionInfoList(this.mContext.getOpPackageName());
            if (records == null) {
                if (DBG) {
                    logd("[getActiveSubInfoCount] records null");
                }
                Binder.restoreCallingIdentity(identity);
                return 0;
            }
            int mActCount = 0;
            for (SubscriptionInfo subInfo : records) {
                if (getSubState(subInfo.getSubscriptionId()) == 1) {
                    mActCount++;
                }
            }
            logd("[getActiveSubInfoCount]- count: " + mActCount);
            Binder.restoreCallingIdentity(identity);
            return mActCount;
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(identity);
        }
    }

    public int getAllSubInfoCount(String callingPackage) {
        if (DBG) {
            logd("[getAllSubInfoCount]+");
        }
        if (!canReadPhoneState(callingPackage, "getAllSubInfoCount")) {
            return 0;
        }
        long identity = Binder.clearCallingIdentity();
        Cursor cursor;
        try {
            cursor = this.mContext.getContentResolver().query(SubscriptionManager.CONTENT_URI, null, null, null, null);
            if (cursor != null) {
                int count = cursor.getCount();
                if (DBG) {
                    logd("[getAllSubInfoCount]- " + count + " SUB(s) in DB");
                }
                if (cursor != null) {
                    cursor.close();
                }
                Binder.restoreCallingIdentity(identity);
                return count;
            }
            if (cursor != null) {
                cursor.close();
            }
            if (DBG) {
                logd("[getAllSubInfoCount]- no SUB in DB");
            }
            Binder.restoreCallingIdentity(identity);
            return 0;
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(identity);
        }
    }

    public int getActiveSubInfoCountMax() {
        return this.mTelephonyManager.getSimCount();
    }

    /*  JADX ERROR: JadxRuntimeException in pass: RegionMakerVisitor
        jadx.core.utils.exceptions.JadxRuntimeException: Exception block dominator not found, method:com.android.internal.telephony.SubscriptionController.addSubInfoRecord(java.lang.String, int):int, dom blocks: [B:5:0x003e, B:15:0x007e, B:67:0x0245]
        	at jadx.core.dex.visitors.regions.ProcessTryCatchRegions.searchTryCatchDominators(ProcessTryCatchRegions.java:89)
        	at jadx.core.dex.visitors.regions.ProcessTryCatchRegions.process(ProcessTryCatchRegions.java:45)
        	at jadx.core.dex.visitors.regions.RegionMakerVisitor.postProcessRegions(RegionMakerVisitor.java:63)
        	at jadx.core.dex.visitors.regions.RegionMakerVisitor.visit(RegionMakerVisitor.java:58)
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
    /* JADX WARNING: Removed duplicated region for block: B:33:0x011d A:{SYNTHETIC, Splitter: B:33:0x011d} */
    /* JADX WARNING: Removed duplicated region for block: B:36:0x0122 A:{Catch:{ all -> 0x0450, all -> 0x041e, all -> 0x0425 }} */
    /* JADX WARNING: Removed duplicated region for block: B:64:0x0206 A:{Catch:{ all -> 0x0450, all -> 0x041e, all -> 0x0425 }} */
    /* JADX WARNING: Removed duplicated region for block: B:67:0x0245 A:{SYNTHETIC, Splitter: B:67:0x0245} */
    /* JADX WARNING: Removed duplicated region for block: B:90:0x0353 A:{SYNTHETIC, Splitter: B:90:0x0353} */
    /* JADX WARNING: Removed duplicated region for block: B:101:0x0375 A:{Catch:{ all -> 0x0450, all -> 0x041e, all -> 0x0425 }} */
    public int addSubInfoRecord(java.lang.String r36, int r37) {
        /*
        r35 = this;
        r5 = DBG;
        if (r5 == 0) goto L_0x002e;
    L_0x0004:
        r5 = new java.lang.StringBuilder;
        r5.<init>();
        r6 = "[addSubInfoRecord]+ iccId:";
        r5 = r5.append(r6);
        r6 = android.telephony.SubscriptionInfo.givePrintableIccid(r36);
        r5 = r5.append(r6);
        r6 = " slotId:";
        r5 = r5.append(r6);
        r0 = r37;
        r5 = r5.append(r0);
        r5 = r5.toString();
        r0 = r35;
        r0.logdl(r5);
    L_0x002e:
        r5 = "addSubInfoRecord";
        r0 = r35;
        r0.enforceModifyPhoneState(r5);
        r22 = 0;
        r14 = android.os.Binder.clearCallingIdentity();
        if (r36 != 0) goto L_0x004f;
    L_0x003e:
        r5 = DBG;	 Catch:{ all -> 0x0425 }
        if (r5 == 0) goto L_0x004a;	 Catch:{ all -> 0x0425 }
    L_0x0042:
        r5 = "[addSubInfoRecord]- null iccId";	 Catch:{ all -> 0x0425 }
        r0 = r35;	 Catch:{ all -> 0x0425 }
        r0.logdl(r5);	 Catch:{ all -> 0x0425 }
    L_0x004a:
        r5 = -1;
        android.os.Binder.restoreCallingIdentity(r14);
        return r5;
    L_0x004f:
        r36 = r36.toUpperCase();	 Catch:{ all -> 0x0425 }
        r0 = r35;	 Catch:{ all -> 0x0425 }
        r5 = r0.mContext;	 Catch:{ all -> 0x0425 }
        r4 = r5.getContentResolver();	 Catch:{ all -> 0x0425 }
        r5 = android.telephony.SubscriptionManager.CONTENT_URI;	 Catch:{ all -> 0x0425 }
        r7 = "icc_id=?";	 Catch:{ all -> 0x0425 }
        r6 = 1;	 Catch:{ all -> 0x0425 }
        r8 = new java.lang.String[r6];	 Catch:{ all -> 0x0425 }
        r6 = 0;	 Catch:{ all -> 0x0425 }
        r8[r6] = r36;	 Catch:{ all -> 0x0425 }
        r6 = 0;	 Catch:{ all -> 0x0425 }
        r9 = 0;	 Catch:{ all -> 0x0425 }
        r11 = r4.query(r5, r6, r7, r8, r9);	 Catch:{ all -> 0x0425 }
        r0 = r35;	 Catch:{ all -> 0x0425 }
        r5 = r0.mContext;	 Catch:{ all -> 0x0425 }
        r5 = r5.getOpPackageName();	 Catch:{ all -> 0x0425 }
        r0 = r35;	 Catch:{ all -> 0x0425 }
        r10 = r0.getUnusedColor(r5);	 Catch:{ all -> 0x0425 }
        r26 = 0;
        if (r11 == 0) goto L_0x0395;
    L_0x007e:
        r5 = r11.moveToFirst();	 Catch:{ all -> 0x041e }
        if (r5 == 0) goto L_0x0395;	 Catch:{ all -> 0x041e }
    L_0x0084:
        r5 = "_id";	 Catch:{ all -> 0x041e }
        r5 = r11.getColumnIndexOrThrow(r5);	 Catch:{ all -> 0x041e }
        r29 = r11.getInt(r5);	 Catch:{ all -> 0x041e }
        r5 = "sim_id";	 Catch:{ all -> 0x041e }
        r5 = r11.getColumnIndexOrThrow(r5);	 Catch:{ all -> 0x041e }
        r23 = r11.getInt(r5);	 Catch:{ all -> 0x041e }
        r5 = "name_source";	 Catch:{ all -> 0x041e }
        r5 = r11.getColumnIndexOrThrow(r5);	 Catch:{ all -> 0x041e }
        r20 = r11.getInt(r5);	 Catch:{ all -> 0x041e }
        r5 = "display_name";	 Catch:{ all -> 0x041e }
        r5 = r11.getColumnIndexOrThrow(r5);	 Catch:{ all -> 0x041e }
        r13 = r11.getString(r5);	 Catch:{ all -> 0x041e }
        r33 = new android.content.ContentValues;	 Catch:{ all -> 0x041e }
        r33.<init>();	 Catch:{ all -> 0x041e }
        r0 = r37;	 Catch:{ all -> 0x041e }
        r1 = r23;	 Catch:{ all -> 0x041e }
        if (r0 == r1) goto L_0x00df;	 Catch:{ all -> 0x041e }
    L_0x00bb:
        r5 = "sim_id";	 Catch:{ all -> 0x041e }
        r6 = java.lang.Integer.valueOf(r37);	 Catch:{ all -> 0x041e }
        r0 = r33;	 Catch:{ all -> 0x041e }
        r0.put(r5, r6);	 Catch:{ all -> 0x041e }
        r0 = r35;	 Catch:{ all -> 0x041e }
        r5 = r0.mIsOP01;	 Catch:{ all -> 0x041e }
        if (r5 == 0) goto L_0x00df;	 Catch:{ all -> 0x041e }
    L_0x00cd:
        r5 = "color";	 Catch:{ all -> 0x041e }
        r0 = r35;	 Catch:{ all -> 0x041e }
        r6 = r0.colorArr;	 Catch:{ all -> 0x041e }
        r6 = r6[r37];	 Catch:{ all -> 0x041e }
        r6 = java.lang.Integer.valueOf(r6);	 Catch:{ all -> 0x041e }
        r0 = r33;	 Catch:{ all -> 0x041e }
        r0.put(r5, r6);	 Catch:{ all -> 0x041e }
    L_0x00df:
        r5 = 2;	 Catch:{ all -> 0x041e }
        r0 = r20;	 Catch:{ all -> 0x041e }
        if (r0 == r5) goto L_0x00e6;	 Catch:{ all -> 0x041e }
    L_0x00e4:
        r26 = 1;	 Catch:{ all -> 0x041e }
    L_0x00e6:
        r5 = r33.size();	 Catch:{ all -> 0x041e }
        if (r5 <= 0) goto L_0x010f;	 Catch:{ all -> 0x041e }
    L_0x00ec:
        r5 = android.telephony.SubscriptionManager.CONTENT_URI;	 Catch:{ all -> 0x041e }
        r6 = new java.lang.StringBuilder;	 Catch:{ all -> 0x041e }
        r6.<init>();	 Catch:{ all -> 0x041e }
        r7 = "_id=";	 Catch:{ all -> 0x041e }
        r6 = r6.append(r7);	 Catch:{ all -> 0x041e }
        r0 = r29;	 Catch:{ all -> 0x041e }
        r8 = (long) r0;	 Catch:{ all -> 0x041e }
        r7 = java.lang.Long.toString(r8);	 Catch:{ all -> 0x041e }
        r6 = r6.append(r7);	 Catch:{ all -> 0x041e }
        r6 = r6.toString();	 Catch:{ all -> 0x041e }
        r7 = 0;	 Catch:{ all -> 0x041e }
        r0 = r33;	 Catch:{ all -> 0x041e }
        r4.update(r5, r0, r6, r7);	 Catch:{ all -> 0x041e }
    L_0x010f:
        r5 = DBG;	 Catch:{ all -> 0x041e }
        if (r5 == 0) goto L_0x011b;	 Catch:{ all -> 0x041e }
    L_0x0113:
        r5 = "[addSubInfoRecord] Record already exists";	 Catch:{ all -> 0x041e }
        r0 = r35;	 Catch:{ all -> 0x041e }
        r0.logdl(r5);	 Catch:{ all -> 0x041e }
    L_0x011b:
        if (r11 == 0) goto L_0x0120;
    L_0x011d:
        r11.close();	 Catch:{ all -> 0x0425 }
    L_0x0120:
        if (r36 == 0) goto L_0x0202;	 Catch:{ all -> 0x0425 }
    L_0x0122:
        r5 = DBG;	 Catch:{ all -> 0x0425 }
        if (r5 == 0) goto L_0x014e;	 Catch:{ all -> 0x0425 }
    L_0x0126:
        r5 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0425 }
        r5.<init>();	 Catch:{ all -> 0x0425 }
        r6 = "[addSubInfoRecord] before- mHasSoftSimCard = ";	 Catch:{ all -> 0x0425 }
        r5 = r5.append(r6);	 Catch:{ all -> 0x0425 }
        r6 = mHasSoftSimCard;	 Catch:{ all -> 0x0425 }
        r5 = r5.append(r6);	 Catch:{ all -> 0x0425 }
        r6 = " mSoftSimSlotId = ";	 Catch:{ all -> 0x0425 }
        r5 = r5.append(r6);	 Catch:{ all -> 0x0425 }
        r6 = mSoftSimSlotId;	 Catch:{ all -> 0x0425 }
        r5 = r5.append(r6);	 Catch:{ all -> 0x0425 }
        r5 = r5.toString();	 Catch:{ all -> 0x0425 }
        r0 = r35;	 Catch:{ all -> 0x0425 }
        r0.logdl(r5);	 Catch:{ all -> 0x0425 }
    L_0x014e:
        r0 = r35;	 Catch:{ all -> 0x0425 }
        r5 = r0.mContext;	 Catch:{ all -> 0x0425 }
        r0 = r35;	 Catch:{ all -> 0x0425 }
        r28 = r0.getSoftSimIccid(r5);	 Catch:{ all -> 0x0425 }
        r34 = "FFFF";	 Catch:{ all -> 0x0425 }
        r19 = "FF";	 Catch:{ all -> 0x0425 }
        r5 = r28.length();	 Catch:{ all -> 0x0425 }
        r6 = 20;	 Catch:{ all -> 0x0425 }
        if (r5 != r6) goto L_0x0199;	 Catch:{ all -> 0x0425 }
    L_0x0166:
        r5 = "F";	 Catch:{ all -> 0x0425 }
        r0 = r28;	 Catch:{ all -> 0x0425 }
        r5 = r0.endsWith(r5);	 Catch:{ all -> 0x0425 }
        if (r5 == 0) goto L_0x0199;	 Catch:{ all -> 0x0425 }
    L_0x0171:
        r5 = 0;	 Catch:{ all -> 0x0425 }
        r6 = 19;	 Catch:{ all -> 0x0425 }
        r0 = r28;	 Catch:{ all -> 0x0425 }
        r34 = r0.substring(r5, r6);	 Catch:{ all -> 0x0425 }
        r5 = DBG;	 Catch:{ all -> 0x0425 }
        if (r5 == 0) goto L_0x0199;	 Catch:{ all -> 0x0425 }
    L_0x017e:
        r5 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0425 }
        r5.<init>();	 Catch:{ all -> 0x0425 }
        r6 = "[addSubInfoRecord] vsimIccid = ";	 Catch:{ all -> 0x0425 }
        r5 = r5.append(r6);	 Catch:{ all -> 0x0425 }
        r0 = r34;	 Catch:{ all -> 0x0425 }
        r5 = r5.append(r0);	 Catch:{ all -> 0x0425 }
        r5 = r5.toString();	 Catch:{ all -> 0x0425 }
        r0 = r35;	 Catch:{ all -> 0x0425 }
        r0.logdl(r5);	 Catch:{ all -> 0x0425 }
    L_0x0199:
        r5 = r36.length();	 Catch:{ all -> 0x0425 }
        r6 = 20;	 Catch:{ all -> 0x0425 }
        if (r5 != r6) goto L_0x01df;	 Catch:{ all -> 0x0425 }
    L_0x01a1:
        r5 = "F";	 Catch:{ all -> 0x0425 }
        r0 = r36;	 Catch:{ all -> 0x0425 }
        r5 = r0.endsWith(r5);	 Catch:{ all -> 0x0425 }
        if (r5 != 0) goto L_0x01b7;	 Catch:{ all -> 0x0425 }
    L_0x01ac:
        r5 = "f";	 Catch:{ all -> 0x0425 }
        r0 = r36;	 Catch:{ all -> 0x0425 }
        r5 = r0.endsWith(r5);	 Catch:{ all -> 0x0425 }
        if (r5 == 0) goto L_0x01df;	 Catch:{ all -> 0x0425 }
    L_0x01b7:
        r5 = 0;	 Catch:{ all -> 0x0425 }
        r6 = 19;	 Catch:{ all -> 0x0425 }
        r0 = r36;	 Catch:{ all -> 0x0425 }
        r19 = r0.substring(r5, r6);	 Catch:{ all -> 0x0425 }
        r5 = DBG;	 Catch:{ all -> 0x0425 }
        if (r5 == 0) goto L_0x01df;	 Catch:{ all -> 0x0425 }
    L_0x01c4:
        r5 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0425 }
        r5.<init>();	 Catch:{ all -> 0x0425 }
        r6 = "[addSubInfoRecord] mdIccid = ";	 Catch:{ all -> 0x0425 }
        r5 = r5.append(r6);	 Catch:{ all -> 0x0425 }
        r0 = r19;	 Catch:{ all -> 0x0425 }
        r5 = r5.append(r0);	 Catch:{ all -> 0x0425 }
        r5 = r5.toString();	 Catch:{ all -> 0x0425 }
        r0 = r35;	 Catch:{ all -> 0x0425 }
        r0.logdl(r5);	 Catch:{ all -> 0x0425 }
    L_0x01df:
        r0 = r36;	 Catch:{ all -> 0x0425 }
        r1 = r28;	 Catch:{ all -> 0x0425 }
        r5 = r0.equalsIgnoreCase(r1);	 Catch:{ all -> 0x0425 }
        if (r5 != 0) goto L_0x01fd;	 Catch:{ all -> 0x0425 }
    L_0x01e9:
        r0 = r36;	 Catch:{ all -> 0x0425 }
        r1 = r34;	 Catch:{ all -> 0x0425 }
        r5 = r0.equalsIgnoreCase(r1);	 Catch:{ all -> 0x0425 }
        if (r5 != 0) goto L_0x01fd;	 Catch:{ all -> 0x0425 }
    L_0x01f3:
        r0 = r19;	 Catch:{ all -> 0x0425 }
        r1 = r28;	 Catch:{ all -> 0x0425 }
        r5 = r0.equalsIgnoreCase(r1);	 Catch:{ all -> 0x0425 }
        if (r5 == 0) goto L_0x042a;	 Catch:{ all -> 0x0425 }
    L_0x01fd:
        r5 = 1;	 Catch:{ all -> 0x0425 }
        mHasSoftSimCard = r5;	 Catch:{ all -> 0x0425 }
        mSoftSimSlotId = r37;	 Catch:{ all -> 0x0425 }
    L_0x0202:
        r5 = DBG;	 Catch:{ all -> 0x0425 }
        if (r5 == 0) goto L_0x022e;	 Catch:{ all -> 0x0425 }
    L_0x0206:
        r5 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0425 }
        r5.<init>();	 Catch:{ all -> 0x0425 }
        r6 = "[addSubInfoRecord] after- mHasSoftSimCard = ";	 Catch:{ all -> 0x0425 }
        r5 = r5.append(r6);	 Catch:{ all -> 0x0425 }
        r6 = mHasSoftSimCard;	 Catch:{ all -> 0x0425 }
        r5 = r5.append(r6);	 Catch:{ all -> 0x0425 }
        r6 = " mSoftSimSlotId = ";	 Catch:{ all -> 0x0425 }
        r5 = r5.append(r6);	 Catch:{ all -> 0x0425 }
        r6 = mSoftSimSlotId;	 Catch:{ all -> 0x0425 }
        r5 = r5.append(r6);	 Catch:{ all -> 0x0425 }
        r5 = r5.toString();	 Catch:{ all -> 0x0425 }
        r0 = r35;	 Catch:{ all -> 0x0425 }
        r0.logdl(r5);	 Catch:{ all -> 0x0425 }
    L_0x022e:
        r5 = android.telephony.SubscriptionManager.CONTENT_URI;	 Catch:{ all -> 0x0425 }
        r7 = "sim_id=?";	 Catch:{ all -> 0x0425 }
        r6 = 1;	 Catch:{ all -> 0x0425 }
        r8 = new java.lang.String[r6];	 Catch:{ all -> 0x0425 }
        r6 = java.lang.String.valueOf(r37);	 Catch:{ all -> 0x0425 }
        r9 = 0;	 Catch:{ all -> 0x0425 }
        r8[r9] = r6;	 Catch:{ all -> 0x0425 }
        r6 = 0;	 Catch:{ all -> 0x0425 }
        r9 = 0;	 Catch:{ all -> 0x0425 }
        r11 = r4.query(r5, r6, r7, r8, r9);	 Catch:{ all -> 0x0425 }
        if (r11 == 0) goto L_0x0351;
    L_0x0245:
        r5 = r11.moveToFirst();	 Catch:{ all -> 0x0450 }
        if (r5 == 0) goto L_0x0351;	 Catch:{ all -> 0x0450 }
    L_0x024b:
        r5 = "_id";	 Catch:{ all -> 0x0450 }
        r5 = r11.getColumnIndexOrThrow(r5);	 Catch:{ all -> 0x0450 }
        r29 = r11.getInt(r5);	 Catch:{ all -> 0x0450 }
        r5 = sSlotIdxToSubId;	 Catch:{ all -> 0x0450 }
        r6 = java.lang.Integer.valueOf(r37);	 Catch:{ all -> 0x0450 }
        r7 = java.lang.Integer.valueOf(r29);	 Catch:{ all -> 0x0450 }
        r5.put(r6, r7);	 Catch:{ all -> 0x0450 }
        r30 = r35.getActiveSubInfoCountMax();	 Catch:{ all -> 0x0450 }
        r12 = r35.getDefaultSubId();	 Catch:{ all -> 0x0450 }
        r5 = DBG;	 Catch:{ all -> 0x0450 }
        if (r5 == 0) goto L_0x02cd;	 Catch:{ all -> 0x0450 }
    L_0x026f:
        r5 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0450 }
        r5.<init>();	 Catch:{ all -> 0x0450 }
        r6 = "[addSubInfoRecord] sSlotIdxToSubId.size=";	 Catch:{ all -> 0x0450 }
        r5 = r5.append(r6);	 Catch:{ all -> 0x0450 }
        r6 = sSlotIdxToSubId;	 Catch:{ all -> 0x0450 }
        r6 = r6.size();	 Catch:{ all -> 0x0450 }
        r5 = r5.append(r6);	 Catch:{ all -> 0x0450 }
        r6 = " slotId=";	 Catch:{ all -> 0x0450 }
        r5 = r5.append(r6);	 Catch:{ all -> 0x0450 }
        r0 = r37;	 Catch:{ all -> 0x0450 }
        r5 = r5.append(r0);	 Catch:{ all -> 0x0450 }
        r6 = " subId=";	 Catch:{ all -> 0x0450 }
        r5 = r5.append(r6);	 Catch:{ all -> 0x0450 }
        r0 = r29;	 Catch:{ all -> 0x0450 }
        r5 = r5.append(r0);	 Catch:{ all -> 0x0450 }
        r6 = " mDefaultFallbackSubId=";	 Catch:{ all -> 0x0450 }
        r5 = r5.append(r6);	 Catch:{ all -> 0x0450 }
        r6 = mDefaultFallbackSubId;	 Catch:{ all -> 0x0450 }
        r5 = r5.append(r6);	 Catch:{ all -> 0x0450 }
        r6 = " defaultSubId=";	 Catch:{ all -> 0x0450 }
        r5 = r5.append(r6);	 Catch:{ all -> 0x0450 }
        r5 = r5.append(r12);	 Catch:{ all -> 0x0450 }
        r6 = " simCount=";	 Catch:{ all -> 0x0450 }
        r5 = r5.append(r6);	 Catch:{ all -> 0x0450 }
        r0 = r30;	 Catch:{ all -> 0x0450 }
        r5 = r5.append(r0);	 Catch:{ all -> 0x0450 }
        r5 = r5.toString();	 Catch:{ all -> 0x0450 }
        r0 = r35;	 Catch:{ all -> 0x0450 }
        r0.logdl(r5);	 Catch:{ all -> 0x0450 }
    L_0x02cd:
        r5 = android.telephony.SubscriptionManager.isValidSubscriptionId(r12);	 Catch:{ all -> 0x0450 }
        if (r5 == 0) goto L_0x02d8;	 Catch:{ all -> 0x0450 }
    L_0x02d3:
        r5 = 1;	 Catch:{ all -> 0x0450 }
        r0 = r30;	 Catch:{ all -> 0x0450 }
        if (r0 != r5) goto L_0x043c;	 Catch:{ all -> 0x0450 }
    L_0x02d8:
        r0 = r35;	 Catch:{ all -> 0x0450 }
        r1 = r29;	 Catch:{ all -> 0x0450 }
        r0.setDefaultFallbackSubId(r1);	 Catch:{ all -> 0x0450 }
    L_0x02df:
        r5 = 1;	 Catch:{ all -> 0x0450 }
        r0 = r30;	 Catch:{ all -> 0x0450 }
        if (r0 != r5) goto L_0x0318;	 Catch:{ all -> 0x0450 }
    L_0x02e4:
        r5 = DBG;	 Catch:{ all -> 0x0450 }
        if (r5 == 0) goto L_0x0303;	 Catch:{ all -> 0x0450 }
    L_0x02e8:
        r5 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0450 }
        r5.<init>();	 Catch:{ all -> 0x0450 }
        r6 = "[addSubInfoRecord] one sim set defaults to subId=";	 Catch:{ all -> 0x0450 }
        r5 = r5.append(r6);	 Catch:{ all -> 0x0450 }
        r0 = r29;	 Catch:{ all -> 0x0450 }
        r5 = r5.append(r0);	 Catch:{ all -> 0x0450 }
        r5 = r5.toString();	 Catch:{ all -> 0x0450 }
        r0 = r35;	 Catch:{ all -> 0x0450 }
        r0.logdl(r5);	 Catch:{ all -> 0x0450 }
    L_0x0303:
        r0 = r35;	 Catch:{ all -> 0x0450 }
        r1 = r29;	 Catch:{ all -> 0x0450 }
        r0.setDefaultDataSubId(r1);	 Catch:{ all -> 0x0450 }
        r0 = r35;	 Catch:{ all -> 0x0450 }
        r1 = r29;	 Catch:{ all -> 0x0450 }
        r0.setDefaultSmsSubId(r1);	 Catch:{ all -> 0x0450 }
        r0 = r35;	 Catch:{ all -> 0x0450 }
        r1 = r29;	 Catch:{ all -> 0x0450 }
        r0.setDefaultVoiceSubId(r1);	 Catch:{ all -> 0x0450 }
    L_0x0318:
        r5 = DBG;	 Catch:{ all -> 0x0450 }
        if (r5 == 0) goto L_0x034b;	 Catch:{ all -> 0x0450 }
    L_0x031c:
        r5 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0450 }
        r5.<init>();	 Catch:{ all -> 0x0450 }
        r6 = "[addSubInfoRecord] hashmap(";	 Catch:{ all -> 0x0450 }
        r5 = r5.append(r6);	 Catch:{ all -> 0x0450 }
        r0 = r37;	 Catch:{ all -> 0x0450 }
        r5 = r5.append(r0);	 Catch:{ all -> 0x0450 }
        r6 = ",";	 Catch:{ all -> 0x0450 }
        r5 = r5.append(r6);	 Catch:{ all -> 0x0450 }
        r0 = r29;	 Catch:{ all -> 0x0450 }
        r5 = r5.append(r0);	 Catch:{ all -> 0x0450 }
        r6 = ")";	 Catch:{ all -> 0x0450 }
        r5 = r5.append(r6);	 Catch:{ all -> 0x0450 }
        r5 = r5.toString();	 Catch:{ all -> 0x0450 }
        r0 = r35;	 Catch:{ all -> 0x0450 }
        r0.logdl(r5);	 Catch:{ all -> 0x0450 }
    L_0x034b:
        r5 = r11.moveToNext();	 Catch:{ all -> 0x0450 }
        if (r5 != 0) goto L_0x024b;
    L_0x0351:
        if (r11 == 0) goto L_0x0362;
    L_0x0353:
        r5 = r11.moveToFirst();	 Catch:{ all -> 0x0425 }
        if (r5 == 0) goto L_0x035f;	 Catch:{ all -> 0x0425 }
    L_0x0359:
        r0 = r35;	 Catch:{ all -> 0x0425 }
        r22 = r0.getSubInfoRecord(r11);	 Catch:{ all -> 0x0425 }
    L_0x035f:
        r11.close();	 Catch:{ all -> 0x0425 }
    L_0x0362:
        r0 = r35;	 Catch:{ all -> 0x0425 }
        r1 = r37;	 Catch:{ all -> 0x0425 }
        r31 = r0.getSubId(r1);	 Catch:{ all -> 0x0425 }
        if (r31 == 0) goto L_0x0371;	 Catch:{ all -> 0x0425 }
    L_0x036c:
        r0 = r31;	 Catch:{ all -> 0x0425 }
        r5 = r0.length;	 Catch:{ all -> 0x0425 }
        if (r5 != 0) goto L_0x0463;	 Catch:{ all -> 0x0425 }
    L_0x0371:
        r5 = DBG;	 Catch:{ all -> 0x0425 }
        if (r5 == 0) goto L_0x0390;	 Catch:{ all -> 0x0425 }
    L_0x0375:
        r5 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0425 }
        r5.<init>();	 Catch:{ all -> 0x0425 }
        r6 = "[addSubInfoRecord]- getSubId failed subIds == null || length == 0 subIds=";	 Catch:{ all -> 0x0425 }
        r5 = r5.append(r6);	 Catch:{ all -> 0x0425 }
        r0 = r31;	 Catch:{ all -> 0x0425 }
        r5 = r5.append(r0);	 Catch:{ all -> 0x0425 }
        r5 = r5.toString();	 Catch:{ all -> 0x0425 }
        r0 = r35;	 Catch:{ all -> 0x0425 }
        r0.logdl(r5);	 Catch:{ all -> 0x0425 }
    L_0x0390:
        r5 = -1;
        android.os.Binder.restoreCallingIdentity(r14);
        return r5;
    L_0x0395:
        r26 = 1;
        r33 = new android.content.ContentValues;	 Catch:{ all -> 0x041e }
        r33.<init>();	 Catch:{ all -> 0x041e }
        r5 = "icc_id";	 Catch:{ all -> 0x041e }
        r0 = r33;	 Catch:{ all -> 0x041e }
        r1 = r36;	 Catch:{ all -> 0x041e }
        r0.put(r5, r1);	 Catch:{ all -> 0x041e }
        r5 = "color";	 Catch:{ all -> 0x041e }
        r6 = java.lang.Integer.valueOf(r10);	 Catch:{ all -> 0x041e }
        r0 = r33;	 Catch:{ all -> 0x041e }
        r0.put(r5, r6);	 Catch:{ all -> 0x041e }
        r5 = "sim_id";	 Catch:{ all -> 0x041e }
        r6 = java.lang.Integer.valueOf(r37);	 Catch:{ all -> 0x041e }
        r0 = r33;	 Catch:{ all -> 0x041e }
        r0.put(r5, r6);	 Catch:{ all -> 0x041e }
        r5 = "carrier_name";	 Catch:{ all -> 0x041e }
        r6 = "";	 Catch:{ all -> 0x041e }
        r0 = r33;	 Catch:{ all -> 0x041e }
        r0.put(r5, r6);	 Catch:{ all -> 0x041e }
        r5 = "sub_state";	 Catch:{ all -> 0x041e }
        r6 = 1;	 Catch:{ all -> 0x041e }
        r6 = java.lang.Integer.valueOf(r6);	 Catch:{ all -> 0x041e }
        r0 = r33;	 Catch:{ all -> 0x041e }
        r0.put(r5, r6);	 Catch:{ all -> 0x041e }
        r5 = DBG;	 Catch:{ all -> 0x041e }
        if (r5 == 0) goto L_0x03f5;	 Catch:{ all -> 0x041e }
    L_0x03da:
        r5 = new java.lang.StringBuilder;	 Catch:{ all -> 0x041e }
        r5.<init>();	 Catch:{ all -> 0x041e }
        r6 = "[addSubInfoRecord] init to activate state for slot:";	 Catch:{ all -> 0x041e }
        r5 = r5.append(r6);	 Catch:{ all -> 0x041e }
        r0 = r37;	 Catch:{ all -> 0x041e }
        r5 = r5.append(r0);	 Catch:{ all -> 0x041e }
        r5 = r5.toString();	 Catch:{ all -> 0x041e }
        r0 = r35;	 Catch:{ all -> 0x041e }
        r0.logdl(r5);	 Catch:{ all -> 0x041e }
    L_0x03f5:
        r5 = android.telephony.SubscriptionManager.CONTENT_URI;	 Catch:{ all -> 0x041e }
        r0 = r33;	 Catch:{ all -> 0x041e }
        r32 = r4.insert(r5, r0);	 Catch:{ all -> 0x041e }
        r5 = DBG;	 Catch:{ all -> 0x041e }
        if (r5 == 0) goto L_0x011b;	 Catch:{ all -> 0x041e }
    L_0x0401:
        r5 = new java.lang.StringBuilder;	 Catch:{ all -> 0x041e }
        r5.<init>();	 Catch:{ all -> 0x041e }
        r6 = "[addSubInfoRecord] New record created: ";	 Catch:{ all -> 0x041e }
        r5 = r5.append(r6);	 Catch:{ all -> 0x041e }
        r0 = r32;	 Catch:{ all -> 0x041e }
        r5 = r5.append(r0);	 Catch:{ all -> 0x041e }
        r5 = r5.toString();	 Catch:{ all -> 0x041e }
        r0 = r35;	 Catch:{ all -> 0x041e }
        r0.logdl(r5);	 Catch:{ all -> 0x041e }
        goto L_0x011b;
    L_0x041e:
        r5 = move-exception;
        if (r11 == 0) goto L_0x0424;
    L_0x0421:
        r11.close();	 Catch:{ all -> 0x0425 }
    L_0x0424:
        throw r5;	 Catch:{ all -> 0x0425 }
    L_0x0425:
        r5 = move-exception;
        android.os.Binder.restoreCallingIdentity(r14);
        throw r5;
    L_0x042a:
        r5 = mHasSoftSimCard;	 Catch:{ all -> 0x0425 }
        if (r5 == 0) goto L_0x0202;	 Catch:{ all -> 0x0425 }
    L_0x042e:
        r5 = mSoftSimSlotId;	 Catch:{ all -> 0x0425 }
        r0 = r37;	 Catch:{ all -> 0x0425 }
        if (r5 != r0) goto L_0x0202;	 Catch:{ all -> 0x0425 }
    L_0x0434:
        r5 = 0;	 Catch:{ all -> 0x0425 }
        mHasSoftSimCard = r5;	 Catch:{ all -> 0x0425 }
        r5 = -1;	 Catch:{ all -> 0x0425 }
        mSoftSimSlotId = r5;	 Catch:{ all -> 0x0425 }
        goto L_0x0202;
    L_0x043c:
        r0 = r35;	 Catch:{ all -> 0x0450 }
        r5 = r0.isActiveSubId(r12);	 Catch:{ all -> 0x0450 }
        if (r5 == 0) goto L_0x02d8;	 Catch:{ all -> 0x0450 }
    L_0x0444:
        r5 = mDefaultFallbackSubId;	 Catch:{ all -> 0x0450 }
        r0 = r35;	 Catch:{ all -> 0x0450 }
        r5 = r0.isActiveSubId(r5);	 Catch:{ all -> 0x0450 }
        if (r5 == 0) goto L_0x02d8;
    L_0x044e:
        goto L_0x02df;
    L_0x0450:
        r5 = move-exception;
        if (r11 == 0) goto L_0x0462;
    L_0x0453:
        r6 = r11.moveToFirst();	 Catch:{ all -> 0x0425 }
        if (r6 == 0) goto L_0x045f;	 Catch:{ all -> 0x0425 }
    L_0x0459:
        r0 = r35;	 Catch:{ all -> 0x0425 }
        r22 = r0.getSubInfoRecord(r11);	 Catch:{ all -> 0x0425 }
    L_0x045f:
        r11.close();	 Catch:{ all -> 0x0425 }
    L_0x0462:
        throw r5;	 Catch:{ all -> 0x0425 }
    L_0x0463:
        if (r26 == 0) goto L_0x050d;	 Catch:{ all -> 0x0425 }
    L_0x0465:
        r0 = r35;	 Catch:{ all -> 0x0425 }
        r5 = r0.mTelephonyManager;	 Catch:{ all -> 0x0425 }
        r6 = 0;	 Catch:{ all -> 0x0425 }
        r6 = r31[r6];	 Catch:{ all -> 0x0425 }
        r27 = r5.getSimOperatorName(r6);	 Catch:{ all -> 0x0425 }
        r5 = com.android.internal.telephony.OemConstant.EXP_VERSION;	 Catch:{ all -> 0x0425 }
        if (r5 == 0) goto L_0x0592;	 Catch:{ all -> 0x0425 }
    L_0x0474:
        r0 = r35;	 Catch:{ all -> 0x0425 }
        r1 = r37;	 Catch:{ all -> 0x0425 }
        r21 = r0.getExportSimDefaultName(r1);	 Catch:{ all -> 0x0425 }
    L_0x047c:
        r5 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0425 }
        r5.<init>();	 Catch:{ all -> 0x0425 }
        r6 = "[addSubInfoRecord] nameToSet = ";	 Catch:{ all -> 0x0425 }
        r5 = r5.append(r6);	 Catch:{ all -> 0x0425 }
        r0 = r21;	 Catch:{ all -> 0x0425 }
        r5 = r5.append(r0);	 Catch:{ all -> 0x0425 }
        r5 = r5.toString();	 Catch:{ all -> 0x0425 }
        r0 = r35;	 Catch:{ all -> 0x0425 }
        r0.logdl(r5);	 Catch:{ all -> 0x0425 }
        r5 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0425 }
        r5.<init>();	 Catch:{ all -> 0x0425 }
        r6 = "[addSubInfoRecord] simCarrierName = ";	 Catch:{ all -> 0x0425 }
        r5 = r5.append(r6);	 Catch:{ all -> 0x0425 }
        r0 = r27;	 Catch:{ all -> 0x0425 }
        r5 = r5.append(r0);	 Catch:{ all -> 0x0425 }
        r5 = r5.toString();	 Catch:{ all -> 0x0425 }
        r0 = r35;	 Catch:{ all -> 0x0425 }
        r0.logdl(r5);	 Catch:{ all -> 0x0425 }
        r33 = new android.content.ContentValues;	 Catch:{ all -> 0x0425 }
        r33.<init>();	 Catch:{ all -> 0x0425 }
        r5 = "display_name";	 Catch:{ all -> 0x0425 }
        r0 = r33;	 Catch:{ all -> 0x0425 }
        r1 = r21;	 Catch:{ all -> 0x0425 }
        r0.put(r5, r1);	 Catch:{ all -> 0x0425 }
        r5 = android.telephony.SubscriptionManager.CONTENT_URI;	 Catch:{ all -> 0x0425 }
        r6 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0425 }
        r6.<init>();	 Catch:{ all -> 0x0425 }
        r7 = "_id=";	 Catch:{ all -> 0x0425 }
        r6 = r6.append(r7);	 Catch:{ all -> 0x0425 }
        r7 = 0;	 Catch:{ all -> 0x0425 }
        r7 = r31[r7];	 Catch:{ all -> 0x0425 }
        r8 = (long) r7;	 Catch:{ all -> 0x0425 }
        r7 = java.lang.Long.toString(r8);	 Catch:{ all -> 0x0425 }
        r6 = r6.append(r7);	 Catch:{ all -> 0x0425 }
        r6 = r6.toString();	 Catch:{ all -> 0x0425 }
        r7 = 0;	 Catch:{ all -> 0x0425 }
        r0 = r33;	 Catch:{ all -> 0x0425 }
        r4.update(r5, r0, r6, r7);	 Catch:{ all -> 0x0425 }
        if (r22 == 0) goto L_0x04ee;	 Catch:{ all -> 0x0425 }
    L_0x04e7:
        r0 = r22;	 Catch:{ all -> 0x0425 }
        r1 = r21;	 Catch:{ all -> 0x0425 }
        r0.setDisplayName(r1);	 Catch:{ all -> 0x0425 }
    L_0x04ee:
        r5 = DBG;	 Catch:{ all -> 0x0425 }
        if (r5 == 0) goto L_0x050d;	 Catch:{ all -> 0x0425 }
    L_0x04f2:
        r5 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0425 }
        r5.<init>();	 Catch:{ all -> 0x0425 }
        r6 = "[addSubInfoRecord] sim name = ";	 Catch:{ all -> 0x0425 }
        r5 = r5.append(r6);	 Catch:{ all -> 0x0425 }
        r0 = r21;	 Catch:{ all -> 0x0425 }
        r5 = r5.append(r0);	 Catch:{ all -> 0x0425 }
        r5 = r5.toString();	 Catch:{ all -> 0x0425 }
        r0 = r35;	 Catch:{ all -> 0x0425 }
        r0.logdl(r5);	 Catch:{ all -> 0x0425 }
    L_0x050d:
        r5 = sPhones;	 Catch:{ all -> 0x0425 }
        r5 = r5[r37];	 Catch:{ all -> 0x0425 }
        r5.updateDataConnectionTracker();	 Catch:{ all -> 0x0425 }
        r5 = DBG;	 Catch:{ all -> 0x0425 }
        if (r5 == 0) goto L_0x0537;	 Catch:{ all -> 0x0425 }
    L_0x0518:
        r5 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0425 }
        r5.<init>();	 Catch:{ all -> 0x0425 }
        r6 = "[addSubInfoRecord]- info size=";	 Catch:{ all -> 0x0425 }
        r5 = r5.append(r6);	 Catch:{ all -> 0x0425 }
        r6 = sSlotIdxToSubId;	 Catch:{ all -> 0x0425 }
        r6 = r6.size();	 Catch:{ all -> 0x0425 }
        r5 = r5.append(r6);	 Catch:{ all -> 0x0425 }
        r5 = r5.toString();	 Catch:{ all -> 0x0425 }
        r0 = r35;	 Catch:{ all -> 0x0425 }
        r0.logdl(r5);	 Catch:{ all -> 0x0425 }
    L_0x0537:
        android.os.Binder.restoreCallingIdentity(r14);
        r0 = r35;
        r5 = r0.mActiveList;
        if (r5 != 0) goto L_0x0549;
    L_0x0540:
        r5 = new java.util.concurrent.CopyOnWriteArrayList;
        r5.<init>();
        r0 = r35;
        r0.mActiveList = r5;
    L_0x0549:
        if (r22 == 0) goto L_0x0602;
    L_0x054b:
        r18 = 1;
        r0 = r35;
        r5 = r0.mActiveList;
        r25 = r5.iterator();
    L_0x0555:
        r5 = r25.hasNext();
        if (r5 == 0) goto L_0x056d;
    L_0x055b:
        r24 = r25.next();
        r24 = (android.telephony.SubscriptionInfo) r24;
        r5 = r22.getSimSlotIndex();
        r6 = r24.getSimSlotIndex();
        if (r5 != r6) goto L_0x0555;
    L_0x056b:
        r18 = 0;
    L_0x056d:
        if (r18 == 0) goto L_0x0602;
    L_0x056f:
        r17 = 0;
        r0 = r35;
        r5 = r0.mActiveList;
        r25 = r5.iterator();
    L_0x0579:
        r5 = r25.hasNext();
        if (r5 == 0) goto L_0x05dc;
    L_0x057f:
        r24 = r25.next();
        r24 = (android.telephony.SubscriptionInfo) r24;
        r5 = r22.getSimSlotIndex();
        r6 = r24.getSimSlotIndex();
        if (r5 <= r6) goto L_0x0579;
    L_0x058f:
        r17 = r17 + 1;
        goto L_0x0579;
    L_0x0592:
        r5 = android.telephony.TelephonyManager.getDefault();	 Catch:{ all -> 0x0425 }
        r6 = 0;	 Catch:{ all -> 0x0425 }
        r6 = r31[r6];	 Catch:{ all -> 0x0425 }
        r16 = r5.getSubscriberId(r6);	 Catch:{ all -> 0x0425 }
        r0 = r35;	 Catch:{ all -> 0x0425 }
        r5 = r0.mContext;	 Catch:{ all -> 0x0425 }
        r0 = r27;	 Catch:{ all -> 0x0425 }
        r1 = r16;	 Catch:{ all -> 0x0425 }
        r2 = r36;	 Catch:{ all -> 0x0425 }
        r3 = r37;	 Catch:{ all -> 0x0425 }
        r21 = getCarrierName(r5, r0, r1, r2, r3);	 Catch:{ all -> 0x0425 }
        r5 = DBG;	 Catch:{ all -> 0x0425 }
        if (r5 == 0) goto L_0x047c;	 Catch:{ all -> 0x0425 }
    L_0x05b1:
        r5 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0425 }
        r5.<init>();	 Catch:{ all -> 0x0425 }
        r6 = "[addSubInfoRecord] imsi = ";	 Catch:{ all -> 0x0425 }
        r5 = r5.append(r6);	 Catch:{ all -> 0x0425 }
        r0 = r16;	 Catch:{ all -> 0x0425 }
        r5 = r5.append(r0);	 Catch:{ all -> 0x0425 }
        r6 = "of ";	 Catch:{ all -> 0x0425 }
        r5 = r5.append(r6);	 Catch:{ all -> 0x0425 }
        r6 = 0;	 Catch:{ all -> 0x0425 }
        r6 = r31[r6];	 Catch:{ all -> 0x0425 }
        r5 = r5.append(r6);	 Catch:{ all -> 0x0425 }
        r5 = r5.toString();	 Catch:{ all -> 0x0425 }
        r0 = r35;	 Catch:{ all -> 0x0425 }
        r0.logdl(r5);	 Catch:{ all -> 0x0425 }
        goto L_0x047c;
    L_0x05dc:
        r0 = r35;
        r5 = r0.mActiveList;
        r0 = r17;
        r1 = r22;
        r5.add(r0, r1);
        r5 = new java.lang.StringBuilder;
        r5.<init>();
        r6 = "[addSubInfoRecord] insertAt=";
        r5 = r5.append(r6);
        r0 = r17;
        r5 = r5.append(r0);
        r5 = r5.toString();
        r0 = r35;
        r0.logd(r5);
    L_0x0602:
        r5 = 0;
        return r5;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.SubscriptionController.addSubInfoRecord(java.lang.String, int):int");
    }

    public boolean setPlmnSpn(int slotId, boolean showPlmn, String plmn, boolean showSpn, String spn) {
        synchronized (this.mLock) {
            int[] subIds = getSubId(slotId);
            if (!(this.mContext.getPackageManager().resolveContentProvider(SubscriptionManager.CONTENT_URI.getAuthority(), 0) == null || subIds == null)) {
                if (SubscriptionManager.isValidSubscriptionId(subIds[0]) && isReady()) {
                    String carrierText = UsimPBMemInfo.STRING_NOT_SET;
                    if (showPlmn) {
                        carrierText = plmn;
                        if (showSpn && !Objects.equals(spn, plmn)) {
                            carrierText = plmn + this.mContext.getString(17040695).toString() + spn;
                        }
                    } else if (showSpn) {
                        carrierText = spn;
                    }
                    for (int carrierText2 : subIds) {
                        setCarrierText(carrierText, carrierText2);
                    }
                    return true;
                }
            }
            if (DBG) {
                logd("[setPlmnSpn] No valid subscription to store info");
            }
            notifySubscriptionInfoChanged();
            return false;
        }
    }

    public int setCarrierText(String text, int subId) {
        if (DBG) {
            logd("[setCarrierText]+ text:" + text + " subId:" + subId);
        }
        enforceModifyPhoneState("setCarrierText");
        Cursor cursor = null;
        try {
            ContentResolver contentResolver = this.mContext.getContentResolver();
            Uri uri = SubscriptionManager.CONTENT_URI;
            String[] strArr = new String[1];
            strArr[0] = "carrier_name";
            cursor = contentResolver.query(uri, strArr, "_id=" + Integer.toString(subId), null, null);
            if (cursor != null) {
                if (cursor.moveToNext()) {
                    String carrierName = cursor.getString(0);
                    if (text != null && text.equals(carrierName)) {
                        if (DBG) {
                            logd("leon setCarrierText block for the same");
                        }
                        if (this.mActiveList != null) {
                            for (SubscriptionInfo record : this.mActiveList) {
                                if (record.getSubscriptionId() == subId) {
                                    record.setCarrierName(text);
                                }
                            }
                        }
                        if (cursor != null) {
                            cursor.close();
                        }
                        return 0;
                    }
                }
            } else if (DBG) {
                logd("leon setCarrierText Query fail");
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (Exception e) {
            logd("leon setCarrierText Query error:" + e.getMessage());
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
            throw th;
        }
        long identity = Binder.clearCallingIdentity();
        try {
            ContentValues contentValues = new ContentValues(1);
            contentValues.put("carrier_name", text);
            int result = this.mContext.getContentResolver().update(SubscriptionManager.CONTENT_URI, contentValues, "_id=" + Long.toString((long) subId), null);
            if (ENGDEBUG && DBG) {
                logd("[setCarrierText]- update result :" + result);
            }
            if (this.mActiveList != null && result > 0) {
                for (SubscriptionInfo record2 : this.mActiveList) {
                    if (record2.getSubscriptionId() == subId) {
                        record2.setCarrierName(text);
                    }
                }
            }
            notifySubscriptionInfoChanged();
            Binder.restoreCallingIdentity(identity);
            return result;
        } catch (Throwable th2) {
            Binder.restoreCallingIdentity(identity);
            throw th2;
        }
    }

    public int setIconTint(int tint, int subId) {
        if (DBG) {
            logd("[setIconTint]+ tint:" + tint + " subId:" + subId);
        }
        enforceModifyPhoneState("setIconTint");
        long identity = Binder.clearCallingIdentity();
        try {
            validateSubId(subId);
            ContentValues value = new ContentValues(1);
            value.put(SimInfo.COLOR, Integer.valueOf(tint));
            if (DBG) {
                logd("[setIconTint]- tint:" + tint + " set");
            }
            int result = this.mContext.getContentResolver().update(SubscriptionManager.CONTENT_URI, value, "_id=" + Long.toString((long) subId), null);
            if (DBG) {
                logd("[setIconTint]- update result :" + result);
            }
            if (this.mActiveList != null && result > 0) {
                for (SubscriptionInfo record : this.mActiveList) {
                    if (record.getSubscriptionId() == subId) {
                        record.setIconTint(tint);
                    }
                }
            }
            notifySubscriptionInfoChanged();
            return result;
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    public int setDisplayName(String displayName, int subId) {
        return setDisplayNameUsingSrc(displayName, subId, -1);
    }

    public int setDisplayNameUsingSrc(String displayName, int subId, long nameSource) {
        if (DBG) {
            logd("[setDisplayName]+  displayName:" + displayName + " subId:" + subId + " nameSource:" + nameSource);
        }
        enforceModifyPhoneState("setDisplayNameUsingSrc");
        long identity = Binder.clearCallingIdentity();
        try {
            String nameToSet;
            validateSubId(subId);
            if (displayName == null) {
                nameToSet = this.mContext.getString(17039374);
            } else {
                nameToSet = displayName;
            }
            ContentValues value = new ContentValues(1);
            value.put(SimInfo.DISPLAY_NAME, nameToSet);
            if (nameSource >= 0) {
                if (DBG) {
                    logd("Set nameSource=" + nameSource);
                }
                value.put(SimInfo.NAME_SOURCE, Long.valueOf(nameSource));
            }
            if (DBG) {
                logd("[setDisplayName]- mDisplayName:" + nameToSet + " set");
            }
            int result = this.mContext.getContentResolver().update(SubscriptionManager.CONTENT_URI, value, "_id=" + Long.toString((long) subId), null);
            if (DBG) {
                logd("[setDisplayName]- update result :" + result);
            }
            if (this.mActiveList != null && result > 0) {
                for (SubscriptionInfo record : this.mActiveList) {
                    if (record.getSubscriptionId() == subId) {
                        record.setDisplayName(nameToSet);
                        if (nameSource >= 0) {
                            record.setNameSource((int) nameSource);
                        }
                    }
                }
            }
            notifySubscriptionInfoChanged();
            return result;
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    public int setDisplayNumber(String number, int subId) {
        if (DBG) {
            logd("[setDisplayNumber]+ subId:" + subId);
        }
        return setDisplayNumber(number, subId, true);
    }

    public int setDataRoaming(int roaming, int subId) {
        if (DBG) {
            logd("[setDataRoaming]+ roaming:" + roaming + " subId:" + subId);
        }
        enforceModifyPhoneState("setDataRoaming");
        long identity = Binder.clearCallingIdentity();
        try {
            validateSubId(subId);
            if (roaming < 0) {
                if (DBG) {
                    logd("[setDataRoaming]- fail");
                }
                Binder.restoreCallingIdentity(identity);
                return -1;
            }
            ContentValues value = new ContentValues(1);
            value.put(SimInfo.DATA_ROAMING, Integer.valueOf(roaming));
            if (DBG) {
                logd("[setDataRoaming]- roaming:" + roaming + " set");
            }
            int result = this.mContext.getContentResolver().update(SubscriptionManager.CONTENT_URI, value, "_id=" + Long.toString((long) subId), null);
            if (DBG) {
                logd("[setDataRoaming]- update result :" + result);
            }
            if (this.mActiveList != null && result > 0) {
                for (SubscriptionInfo record : this.mActiveList) {
                    if (record.getSubscriptionId() == subId) {
                        record.setDataRoaming(roaming);
                    }
                }
            }
            notifySubscriptionInfoChanged();
            Binder.restoreCallingIdentity(identity);
            return result;
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(identity);
        }
    }

    public int setMccMnc(String mccMnc, int subId) {
        int mcc = 0;
        int mnc = 0;
        try {
            mcc = Integer.parseInt(mccMnc.substring(0, 3));
            mnc = Integer.parseInt(mccMnc.substring(3));
        } catch (NumberFormatException e) {
            loge("[setMccMnc] - couldn't parse mcc/mnc: " + mccMnc);
        }
        if (DBG) {
            logd("[setMccMnc]+ mcc/mnc:" + mcc + "/" + mnc + " subId:" + subId);
        }
        ContentValues value = new ContentValues(2);
        value.put("mcc", Integer.valueOf(mcc));
        value.put("mnc", Integer.valueOf(mnc));
        int result = this.mContext.getContentResolver().update(SubscriptionManager.CONTENT_URI, value, "_id=" + Long.toString((long) subId), null);
        if (ENGDEBUG && DBG) {
            logd("[setMccMnc]- update result :" + result);
        }
        if (this.mActiveList != null && result > 0) {
            for (SubscriptionInfo record : this.mActiveList) {
                if (record.getSubscriptionId() == subId) {
                    record.setMcc(mcc);
                    record.setMnc(mnc);
                }
            }
        }
        notifySubscriptionInfoChanged();
        return result;
    }

    public int getSlotId(int subId) {
        if (VDBG) {
            printStackTrace("[getSlotId] subId=" + subId);
        }
        if (subId == Integer.MAX_VALUE) {
            logd("[getSlotId]+ subId == SubscriptionManager.DEFAULT_SUBSCRIPTION_ID");
            subId = getDefaultSubId();
        }
        if (!SubscriptionManager.isValidSubscriptionId(subId)) {
            if (DBG) {
                logd("[getSlotId]- subId invalid");
            }
            return -1;
        } else if (sSlotIdxToSubId.size() == 0) {
            if (DBG) {
                logd("[getSlotId]- size == 0, return SIM_NOT_INSERTED instead, subId =" + subId);
            }
            return -1;
        } else {
            for (Entry<Integer, Integer> entry : sSlotIdxToSubId.entrySet()) {
                int sim = ((Integer) entry.getKey()).intValue();
                if (subId == ((Integer) entry.getValue()).intValue()) {
                    if (VDBG) {
                        logv("[getSlotId]- return =" + sim + ", subId = " + subId);
                    }
                    return sim;
                }
            }
            if (DBG) {
                logd("[getSlotId]- return INVALID_SIM_SLOT_INDEX, subId = " + subId);
            }
            return -1;
        }
    }

    @Deprecated
    public int[] getSubId(int slotIdx) {
        if (VDBG) {
            printStackTrace("[getSubId]+ slotIdx=" + slotIdx);
        }
        if (slotIdx == Integer.MAX_VALUE) {
            slotIdx = getSlotId(getDefaultSubId());
            if (VDBG) {
                logd("[getSubId] map default slotIdx=" + slotIdx);
            }
        }
        if (!SubscriptionManager.isValidSlotId(slotIdx)) {
            if (DBG) {
                logd("[getSubId]- invalid slotIdx=" + slotIdx);
            }
            return null;
        } else if (sSlotIdxToSubId.size() == 0) {
            if (VDBG) {
                logd("[getSubId]- sSlotIdxToSubId.size == 0, return DummySubIds slotIdx=" + slotIdx);
            }
            return getDummySubIds(slotIdx);
        } else {
            ArrayList<Integer> subIds = new ArrayList();
            for (Entry<Integer, Integer> entry : sSlotIdxToSubId.entrySet()) {
                int slot = ((Integer) entry.getKey()).intValue();
                int sub = ((Integer) entry.getValue()).intValue();
                if (slotIdx == slot) {
                    subIds.add(Integer.valueOf(sub));
                }
            }
            int numSubIds = subIds.size();
            if (numSubIds > 0) {
                int[] subIdArr = new int[numSubIds];
                for (int i = 0; i < numSubIds; i++) {
                    subIdArr[i] = ((Integer) subIds.get(i)).intValue();
                }
                if (VDBG) {
                    logd("[getSubId]- subIdArr=" + subIdArr);
                }
                return subIdArr;
            }
            if (ENGDEBUG && DBG) {
                logd("[getSubId]- numSubIds == 0, return DummySubIds slotIdx=" + slotIdx);
            }
            return getDummySubIds(slotIdx);
        }
    }

    public int getPhoneId(int subId) {
        if (VDBG) {
            printStackTrace("[getPhoneId] subId=" + subId);
        }
        if (subId == Integer.MAX_VALUE) {
            subId = getDefaultSubId();
            if (DBG) {
                logdl("[getPhoneId] asked for default subId=" + subId);
            }
        }
        int phoneId;
        if (!SubscriptionManager.isValidSubscriptionId(subId)) {
            if (subId > -2 - getActiveSubInfoCountMax()) {
                phoneId = -2 - subId;
            } else {
                phoneId = -1;
            }
            if (VDBG) {
                logdl("[getPhoneId]- invalid subId = " + subId + " return = " + phoneId);
            }
            return phoneId;
        } else if (sSlotIdxToSubId.size() == 0) {
            phoneId = mDefaultPhoneId;
            if (DBG) {
                logd("[getPhoneId]- no sims, returning default phoneId=" + phoneId + ", subId" + subId);
            }
            if (mHasSoftSimCard) {
                phoneId = Integer.MAX_VALUE;
            }
            return phoneId;
        } else {
            for (Entry<Integer, Integer> entry : sSlotIdxToSubId.entrySet()) {
                int sim = ((Integer) entry.getKey()).intValue();
                if (subId == ((Integer) entry.getValue()).intValue()) {
                    if (VDBG) {
                        logdl("[getPhoneId]- found subId=" + subId + " phoneId=" + sim);
                    }
                    return sim;
                }
            }
            phoneId = mDefaultPhoneId;
            if (DBG) {
                logdl("[getPhoneId]- subId=" + subId + " not found return default phoneId=" + phoneId);
            }
            return phoneId;
        }
    }

    private int[] getDummySubIds(int slotIdx) {
        int numSubs = getActiveSubInfoCountMax();
        if (numSubs <= 0) {
            return null;
        }
        int[] dummyValues = new int[numSubs];
        for (int i = 0; i < numSubs; i++) {
            dummyValues[i] = -2 - slotIdx;
        }
        if (VDBG) {
            logd("getDummySubIds: slotIdx=" + slotIdx + " return " + numSubs + " DummySubIds with each subId=" + dummyValues[0]);
        }
        return dummyValues;
    }

    public int clearSubInfo() {
        enforceModifyPhoneState("clearSubInfo");
        long identity = Binder.clearCallingIdentity();
        try {
            int size = sSlotIdxToSubId.size();
            if (size == 0) {
                if (DBG) {
                    logdl("[clearSubInfo]- no simInfo size=" + size);
                }
                Binder.restoreCallingIdentity(identity);
                return 0;
            }
            setReadyState(false);
            sSlotIdxToSubId.clear();
            if (mHasSoftSimCard && TextUtils.isEmpty(getSoftSimIccid(this.mContext))) {
                if (DBG) {
                    logdl("[clearSubInfo]- has not softsimcard");
                }
                mHasSoftSimCard = false;
                mSoftSimSlotId = -1;
            }
            this.mActiveList.clear();
            if (DBG) {
                logdl("[clearSubInfo]- clear size=" + size);
            }
            Binder.restoreCallingIdentity(identity);
            return size;
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(identity);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:38:0x00c8 A:{Catch:{ all -> 0x00db }} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int clearSubInfoUsingPhoneId(int phoneId) {
        enforceModifyPhoneState("clearSubInfoUsingPhoneId");
        long identity = Binder.clearCallingIdentity();
        try {
            if (SubscriptionManager.isValidPhoneId(phoneId)) {
                setReadyState(false);
                int size = sSlotIdxToSubId.size();
                if (size == 0) {
                    if (ENGDEBUG && DBG) {
                        logdl("[clearSubInfoUsingPhoneId]- no simInfo size=" + size);
                    }
                    Binder.restoreCallingIdentity(identity);
                    return 0;
                }
                sSlotIdxToSubId.remove(Integer.valueOf(phoneId));
                for (int i = this.mActiveList.size() - 1; i >= 0; i--) {
                    if (((SubscriptionInfo) this.mActiveList.get(i)).getSimSlotIndex() == phoneId) {
                        this.mActiveList.remove(i);
                        if (ENGDEBUG && DBG) {
                            logdl("[clearSubInfoUsingPhoneId]- clear phoneId =" + phoneId + " i = " + i);
                        }
                        if (mHasSoftSimCard && TextUtils.isEmpty(getSoftSimIccid(this.mContext))) {
                            if (DBG) {
                                logdl("[clearSubInfoUsingPhoneId]- has not softsimcard");
                            }
                            mHasSoftSimCard = false;
                            mSoftSimSlotId = -1;
                        }
                        Binder.restoreCallingIdentity(identity);
                        return 0;
                    }
                }
                if (DBG) {
                }
                mHasSoftSimCard = false;
                mSoftSimSlotId = -1;
                Binder.restoreCallingIdentity(identity);
                return 0;
            }
            if (ENGDEBUG && DBG) {
                logd("[clearSubInfoUsingPhoneId]- invalid phoneId=" + phoneId);
            }
            Binder.restoreCallingIdentity(identity);
            return -1;
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(identity);
        }
    }

    private void logvl(String msg) {
        logv(msg);
        this.mLocalLog.log(msg);
    }

    private void logv(String msg) {
        Rlog.v(LOG_TAG, msg);
    }

    private void logdl(String msg) {
        logd(msg);
        this.mLocalLog.log(msg);
    }

    private static void slogd(String msg) {
        Rlog.d(LOG_TAG, msg);
    }

    private void logd(String msg) {
        Rlog.d(LOG_TAG, msg);
    }

    private void logel(String msg) {
        loge(msg);
        this.mLocalLog.log(msg);
    }

    private void loge(String msg) {
        Rlog.e(LOG_TAG, msg);
    }

    public int getDefaultSubId() {
        int subId;
        try {
            int subIdRet = getDefaultDataSubId();
            if (isActiveSubId(subIdRet)) {
                return subIdRet;
            }
        } catch (Exception e) {
            logdl("getDefaultSubId--error");
        }
        if (this.mContext.getResources().getBoolean(17956954)) {
            subId = getDefaultVoiceSubId();
            if (VDBG) {
                logdl("[getDefaultSubId] isVoiceCapable subId=" + subId);
            }
        } else {
            subId = getDefaultDataSubId();
            if (VDBG) {
                logdl("[getDefaultSubId] NOT VoiceCapable subId=" + subId);
            }
        }
        if (!isActiveSubId(subId)) {
            subId = mDefaultFallbackSubId;
            if (VDBG) {
                logdl("[getDefaultSubId] NOT active use fall back subId=" + subId);
            }
        }
        if (VDBG) {
            logv("[getDefaultSubId]- value = " + subId);
        }
        return subId;
    }

    public void setDefaultSmsSubId(int subId) {
        enforceModifyPhoneState("setDefaultSmsSubId");
        if (subId == Integer.MAX_VALUE) {
            throw new RuntimeException("setDefaultSmsSubId called with DEFAULT_SUB_ID");
        } else if (isSoftSimCardSubId(subId)) {
            logdl("[setDefaultSmsSubId]- this subid refrence to the Soft sim, so return ");
        } else {
            if (DBG) {
                logdl("[setDefaultSmsSubId]- SoftSimCard disable!!");
            }
            if (DBG) {
                logdl("[setDefaultSmsSubId] subId=" + subId);
            }
            Global.putInt(this.mContext.getContentResolver(), "multi_sim_sms", subId);
            broadcastDefaultSmsSubIdChanged(subId);
        }
    }

    private void broadcastDefaultSmsSubIdChanged(int subId) {
        if (DBG) {
            logdl("[broadcastDefaultSmsSubIdChanged] subId=" + subId);
        }
        Intent intent = new Intent("android.intent.action.ACTION_DEFAULT_SMS_SUBSCRIPTION_CHANGED");
        intent.addFlags(536870912);
        intent.putExtra("subscription", subId);
        this.mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
    }

    public int getDefaultSmsSubId() {
        int subId;
        try {
            subId = Global.getInt(this.mContext.getContentResolver(), "multi_sim_sms", -1);
            if (isActiveSubId(subId)) {
                return subId;
            }
            int subIdRet = getDefaultDataSubId();
            if (VDBG) {
                Rlog.d("sms", "[getDefaultSmsSubId] subId=" + subIdRet);
            }
            return subIdRet;
        } catch (Exception e) {
            e.printStackTrace();
            subId = Global.getInt(this.mContext.getContentResolver(), "multi_sim_sms", -1);
            if (VDBG) {
                logd("[getDefaultSmsSubId] subId=" + subId);
            }
            return subId;
        }
    }

    public void setDefaultVoiceSubId(int subId) {
        enforceModifyPhoneState("setDefaultVoiceSubId");
        if (subId == Integer.MAX_VALUE) {
            throw new RuntimeException("setDefaultVoiceSubId called with DEFAULT_SUB_ID");
        } else if (isSoftSimCardSubId(subId)) {
            logdl("[setDefaultVoiceSubId]- do not set the voice to virtual sim!!");
        } else {
            if (DBG) {
                logdl("[setDefaultVoiceSubId]- soft sim disable!!");
            }
            if (DBG) {
                logdl("[setDefaultVoiceSubId] subId=" + subId);
            }
            Global.putInt(this.mContext.getContentResolver(), "multi_sim_voice_call", subId);
            broadcastDefaultVoiceSubIdChanged(subId);
        }
    }

    public boolean isSoftSimCardSubId(int subId) {
        boolean result = false;
        if (isHasSoftSimCard()) {
            int softSimSlotId = getSoftSimCardSlotId();
            if (softSimSlotId > -1) {
                int[] subIds = getSubId(softSimSlotId);
                if (subIds != null && subIds.length > 0) {
                    logdl("[isSoftSimCardSubId]- Soft sim slot id: " + softSimSlotId + "Soft sim subId: " + subIds[0]);
                    if (subIds[0] == subId) {
                        result = true;
                    }
                } else if (DBG) {
                    logdl("[isSoftSimCardSubId]- getSubId failed subIds == null || length == 0 subIds=" + subIds);
                }
            } else if (DBG) {
                logdl("[isSoftSimCardSubId]- SoftSimCard enable,but slotId is wrong!!,softSimSlotId:" + softSimSlotId);
            }
        } else if (DBG) {
            logdl("[isSoftSimCardSubId]- soft sim disable!!");
        }
        logdl("[isSoftSimCardSubId]-result:" + result);
        return result;
    }

    private void broadcastDefaultVoiceSubIdChanged(int subId) {
        if (DBG) {
            logdl("[broadcastDefaultVoiceSubIdChanged] subId=" + subId);
        }
        Intent intent = new Intent("android.intent.action.ACTION_DEFAULT_VOICE_SUBSCRIPTION_CHANGED");
        intent.addFlags(536870912);
        intent.putExtra("subscription", subId);
        this.mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
    }

    public int getDefaultVoiceSubId() {
        int subId = Global.getInt(this.mContext.getContentResolver(), "multi_sim_voice_call", -1);
        if (VDBG) {
            logd("[getDefaultVoiceSubId] subId=" + subId);
        }
        return subId;
    }

    public int getDefaultDataSubId() {
        int subId = Global.getInt(this.mContext.getContentResolver(), "multi_sim_data_call", -1);
        if (VDBG) {
            logd("[getDefaultDataSubId] subId= " + subId);
        }
        return subId;
    }

    public void setDefaultDataSubId(int subId) {
        setDefaultDataSubIdWithResult(subId);
    }

    private boolean isNeedCapabilitySwitch(int phoneId) {
        int mPhoneNum = sPhones.length;
        int[] simOpInfo = new int[mPhoneNum];
        int[] simType = new int[mPhoneNum];
        int insertedState = 0;
        int insertedSimCount = 0;
        int op01SimCount = 0;
        int op02SimCount = 0;
        String[] currIccId = new String[mPhoneNum];
        String opSpec = SystemProperties.get("persist.operator.optr", IWorldPhone.NO_OP);
        String opOM = IWorldPhone.NO_OP;
        String noSIM = INVALID_ICCID;
        if (opOM.equals(opSpec) && RadioCapabilitySwitchUtil.isPS2SupportLTE()) {
            int i = 0;
            while (i < mPhoneNum) {
                currIccId[i] = SystemProperties.get(this.PROPERTY_ICCID[i]);
                if (currIccId[i] == null || UsimPBMemInfo.STRING_NOT_SET.equals(currIccId[i])) {
                    logd("error: iccid not found, wait for next sub ready");
                    return false;
                }
                if (!noSIM.equals(currIccId[i])) {
                    insertedSimCount++;
                    insertedState |= 1 << i;
                }
                i++;
            }
            logd("setCapabilityIfNeeded : Inserted SIM count: " + insertedSimCount + ", insertedStatus: " + insertedState);
            if (insertedSimCount == 0) {
                return false;
            }
            if (!RadioCapabilitySwitchUtil.getSimInfo(simOpInfo, simType, insertedState)) {
                return false;
            }
            for (i = 0; i < mPhoneNum; i++) {
                if (2 == simOpInfo[i]) {
                    op01SimCount++;
                } else if (3 == simOpInfo[i]) {
                    op02SimCount++;
                    int op02PhoneId = i;
                }
            }
            if ((op02SimCount == 1 && insertedSimCount == 1) || (op02SimCount == 2 && insertedSimCount == 2)) {
                return false;
            }
        }
        return true;
    }

    public boolean setDefaultDataSubIdWithResult(int subId) {
        enforceModifyPhoneState("setDefaultDataSubId");
        if (subId == Integer.MAX_VALUE) {
            throw new RuntimeException("setDefaultDataSubId called with DEFAULT_SUB_ID");
        }
        ProxyController proxyController = ProxyController.getInstance();
        int len = sPhones.length;
        logdl("[setDefaultDataSubId] num phones=" + len + ", subId=" + subId);
        try {
            boolean equals;
            if (SystemProperties.get("persist.operator.optr", UsimPBMemInfo.STRING_NOT_SET).equals("OP18")) {
                equals = SystemProperties.get("ro.mtk_multiple_ims_support", UsimPBMemInfo.STRING_NOT_SET).equals("2");
            } else {
                equals = false;
            }
            if (!equals && (SubscriptionManager.isValidSubscriptionId(subId) || SystemProperties.getInt("ro.mtk_external_sim_support", 0) == 1)) {
                RadioAccessFamily[] rafs = new RadioAccessFamily[len];
                int targetPhoneId = 0;
                boolean atLeastOneMatch = false;
                for (int phoneId = 0; phoneId < len; phoneId++) {
                    int raf;
                    int id = sPhones[phoneId].getSubId();
                    if (id == subId) {
                        raf = proxyController.getMaxRafSupported();
                        atLeastOneMatch = true;
                        targetPhoneId = phoneId;
                    } else {
                        raf = proxyController.getMinRafSupported();
                    }
                    logdl("[setDefaultDataSubId] phoneId=" + phoneId + " subId=" + id + " RAF=" + raf);
                    rafs[phoneId] = new RadioAccessFamily(phoneId, raf);
                }
                if (atLeastOneMatch) {
                    if (isNeedCapabilitySwitch(targetPhoneId)) {
                        proxyController.setRadioCapability(rafs);
                    } else if (DBG) {
                        logdl("[setDefaultDataSubId] no need to capability switch on L+L.");
                    }
                } else if (DBG) {
                    logdl("[setDefaultDataSubId] no valid subId's found - not updating.");
                }
            }
            updateAllDataConnectionTrackers();
            int oldDdsSubId = getDefaultDataSubId();
            if (DBG) {
                logdl("oldDdsSubId = " + oldDdsSubId + " , to set current dds subId = " + subId);
            }
            if (oldDdsSubId != subId) {
                setSwitchingDssState(0, true);
                setSwitchingDssState(1, true);
            }
            Global.putInt(this.mContext.getContentResolver(), "multi_sim_data_call", subId);
            broadcastDefaultDataSubIdChanged(subId);
            return true;
        } catch (RuntimeException e) {
            logd("[setDefaultDataSubId] setRadioCapability: Runtime Exception");
            e.printStackTrace();
            return false;
        }
    }

    private void updateAllDataConnectionTrackers() {
        if (DBG) {
            logdl("[updateAllDataConnectionTrackers] sPhones.length=" + len);
        }
        for (Phone updateDataConnectionTracker : sPhones) {
            updateDataConnectionTracker.updateDataConnectionTracker();
        }
    }

    private void broadcastDefaultDataSubIdChanged(int subId) {
        if (DBG) {
            logdl("[broadcastDefaultDataSubIdChanged] subId=" + subId);
        }
        Intent intent = new Intent("android.intent.action.ACTION_DEFAULT_DATA_SUBSCRIPTION_CHANGED");
        intent.addFlags(536870912);
        intent.putExtra("subscription", subId);
        this.mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
    }

    public void setDefaultFallbackSubId(int subId) {
        if (subId == Integer.MAX_VALUE) {
            throw new RuntimeException("setDefaultSubId called with DEFAULT_SUB_ID");
        }
        if (DBG) {
            logdl("[setDefaultFallbackSubId] subId=" + subId);
        }
        if (SubscriptionManager.isValidSubscriptionId(subId)) {
            int phoneId = getPhoneId(subId);
            if (phoneId >= 0 && (phoneId < this.mTelephonyManager.getPhoneCount() || this.mTelephonyManager.getSimCount() == 1)) {
                if (DBG) {
                    logdl("[setDefaultFallbackSubId] set mDefaultFallbackSubId=" + subId);
                }
                mDefaultFallbackSubId = subId;
                MccTable.updateMccMncConfiguration(this.mContext, this.mTelephonyManager.getSimOperatorNumericForPhone(phoneId), false);
                Intent intent = new Intent("android.intent.action.ACTION_DEFAULT_SUBSCRIPTION_CHANGED");
                intent.addFlags(536870912);
                SubscriptionManager.putPhoneIdAndSubIdExtra(intent, phoneId, subId);
                if (DBG) {
                    logdl("[setDefaultFallbackSubId] broadcast default subId changed phoneId=" + phoneId + " subId=" + subId);
                }
                this.mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
            } else if (DBG) {
                logdl("[setDefaultFallbackSubId] not set invalid phoneId=" + phoneId + " subId=" + subId);
            }
        }
    }

    public void clearDefaultsForInactiveSubIds() {
        enforceModifyPhoneState("clearDefaultsForInactiveSubIds");
        long identity = Binder.clearCallingIdentity();
        try {
            boolean z;
            List<SubscriptionInfo> records = getActiveSubscriptionInfoList(this.mContext.getOpPackageName());
            if (DBG) {
                logdl("[clearDefaultsForInactiveSubIds] records: " + records);
            }
            if (this.mIsOP09A || "OP02".equals(SystemProperties.get("persist.operator.optr"))) {
                z = true;
            } else {
                z = "OP01".equals(SystemProperties.get("persist.operator.optr"));
            }
            if (z) {
                logd("clearDefaultsForInactiveSubIds, don't set default data for customization!");
            } else if (shouldDefaultBeCleared(records, getDefaultDataSubId())) {
                if (DBG) {
                    logd("[clearDefaultsForInactiveSubIds] clearing default data sub id");
                }
                setDefaultDataSubId(-1);
            }
            if (shouldDefaultBeCleared(records, getDefaultSmsSubId())) {
                if (DBG) {
                    logdl("[clearDefaultsForInactiveSubIds] clearing default sms sub id");
                }
                setDefaultSmsSubId(-1);
            }
            if (shouldDefaultBeCleared(records, getDefaultVoiceSubId())) {
                if (DBG) {
                    logdl("[clearDefaultsForInactiveSubIds] clearing default voice sub id");
                }
                setDefaultVoiceSubId(-1);
            }
            Binder.restoreCallingIdentity(identity);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(identity);
        }
    }

    private boolean shouldDefaultBeCleared(List<SubscriptionInfo> records, int subId) {
        if (DBG) {
            logdl("[shouldDefaultBeCleared: subId] " + subId);
        }
        if (records == null) {
            if (DBG) {
                logdl("[shouldDefaultBeCleared] return true no records subId=" + subId);
            }
            return true;
        } else if (SubscriptionManager.isValidSubscriptionId(subId)) {
            for (SubscriptionInfo record : records) {
                int id = record.getSubscriptionId();
                if (DBG) {
                    logdl("[shouldDefaultBeCleared] Record.id: " + id);
                    continue;
                }
                if (id == subId) {
                    logdl("[shouldDefaultBeCleared] return false subId is active, subId=" + subId);
                    return false;
                }
            }
            if (DBG) {
                logdl("[shouldDefaultBeCleared] return true not active subId=" + subId);
            }
            return true;
        } else {
            if (DBG) {
                logdl("[shouldDefaultBeCleared] return false only one subId, subId=" + subId);
            }
            return false;
        }
    }

    public int getSubIdUsingPhoneId(int phoneId) {
        int[] subIds = getSubId(phoneId);
        if (subIds == null || subIds.length == 0) {
            return -1;
        }
        return subIds[0];
    }

    public int[] getSubIdUsingSlotId(int slotId) {
        return getSubId(slotId);
    }

    public List<SubscriptionInfo> getSubInfoUsingSlotIdWithCheck(int slotId, boolean needCheck, String callingPackage) {
        Throwable th;
        if (DBG) {
            logd("[getSubInfoUsingSlotIdWithCheck]+ slotId:" + slotId);
        }
        if (!canReadPhoneState(callingPackage, "getSubInfoUsingSlotIdWithCheck")) {
            return null;
        }
        long identity = Binder.clearCallingIdentity();
        if (slotId == Integer.MAX_VALUE) {
            try {
                slotId = getSlotId(getDefaultSubId());
            } catch (Throwable th2) {
                Binder.restoreCallingIdentity(identity);
            }
        }
        if (SubscriptionManager.isValidSlotId(slotId)) {
            if (needCheck) {
                if (!isSubInfoReady()) {
                    if (DBG) {
                        logd("[getSubInfoUsingSlotIdWithCheck]- not ready");
                    }
                    Binder.restoreCallingIdentity(identity);
                    return null;
                }
            }
            String[] strArr = new String[1];
            strArr[0] = String.valueOf(slotId);
            Cursor cursor = this.mContext.getContentResolver().query(SubscriptionManager.CONTENT_URI, null, "sim_id=?", strArr, null);
            List<SubscriptionInfo> subList = null;
            if (cursor != null) {
                while (true) {
                    ArrayList<SubscriptionInfo> subList2;
                    ArrayList<SubscriptionInfo> subList3 = subList2;
                    try {
                        if (!cursor.moveToNext()) {
                            subList = subList3;
                            break;
                        }
                        SubscriptionInfo subInfo = getSubInfoRecord(cursor);
                        if (subInfo != null) {
                            if (subList3 == null) {
                                subList2 = new ArrayList();
                            } else {
                                subList2 = subList3;
                            }
                            try {
                                subList2.add(subInfo);
                            } catch (Throwable th3) {
                                th = th3;
                            }
                        } else {
                            subList2 = subList3;
                        }
                    } catch (Throwable th4) {
                        th = th4;
                        subList2 = subList3;
                        if (cursor != null) {
                            cursor.close();
                        }
                        throw th;
                    }
                }
            }
            if (cursor != null) {
                cursor.close();
            }
            if (DBG) {
                logd("[getSubInfoUsingSlotId]- null info return");
            }
            Binder.restoreCallingIdentity(identity);
            return subList;
        }
        if (DBG) {
            logd("[getSubInfoUsingSlotIdWithCheck]- invalid slotId");
        }
        Binder.restoreCallingIdentity(identity);
        return null;
    }

    private void validateSubId(int subId) {
        if (DBG) {
            logd("validateSubId subId: " + subId);
        }
        if (!SubscriptionManager.isValidSubscriptionId(subId)) {
            throw new RuntimeException("Invalid sub id passed as parameter");
        } else if (subId == Integer.MAX_VALUE) {
            throw new RuntimeException("Default sub id passed as parameter");
        }
    }

    public void updatePhonesAvailability(Phone[] phones) {
        sPhones = phones;
    }

    public int[] getActiveSubIdList() {
        Set<Entry<Integer, Integer>> simInfoSet = sSlotIdxToSubId.entrySet();
        int size = simInfoSet.size();
        int[] subIdArr = new int[size];
        if (size == 0) {
            if (VDBG) {
                logdl("[getActiveSubIdList]- sSlotIdxToSubId.size == 0");
            }
            return subIdArr;
        }
        int i = 0;
        try {
            for (Entry<Integer, Integer> entry : simInfoSet) {
                subIdArr[i] = ((Integer) entry.getValue()).intValue();
                i++;
            }
        } catch (Exception e) {
        }
        if (VDBG) {
            logdl("[getActiveSubIdList] simInfoSet=" + simInfoSet + " subIdArr.length=" + subIdArr.length);
        }
        return subIdArr;
    }

    public boolean isActiveSubId(int subId) {
        boolean retVal;
        if (SubscriptionManager.isValidSubscriptionId(subId)) {
            retVal = sSlotIdxToSubId.containsValue(Integer.valueOf(subId));
        } else {
            retVal = false;
        }
        if (VDBG) {
            logdl("[isActiveSubId]- " + retVal);
        }
        return retVal;
    }

    public int getSimStateForSlotIdx(int slotIdx) {
        State simState;
        String err;
        if (slotIdx < 0) {
            simState = State.UNKNOWN;
            err = "invalid slotIdx";
        } else {
            Phone phone = PhoneFactory.getPhone(slotIdx);
            if (phone == null) {
                simState = State.UNKNOWN;
                err = "phone == null";
            } else {
                IccCard icc = phone.getIccCard();
                if (icc == null) {
                    simState = State.UNKNOWN;
                    err = "icc == null";
                } else {
                    simState = icc.getState();
                    err = UsimPBMemInfo.STRING_NOT_SET;
                }
            }
        }
        if (VDBG) {
            logd("getSimStateForSlotIdx: " + err + " simState=" + simState + " ordinal=" + simState.ordinal() + " slotIdx=" + slotIdx);
        }
        return simState.ordinal();
    }

    public void setSubscriptionProperty(int subId, String propKey, String propValue) {
        enforceModifyPhoneState("setSubscriptionProperty");
        long token = Binder.clearCallingIdentity();
        ContentResolver resolver = this.mContext.getContentResolver();
        ContentValues value = new ContentValues();
        if (propKey.equals("enable_cmas_extreme_threat_alerts") || propKey.equals("enable_cmas_severe_threat_alerts") || propKey.equals("enable_cmas_amber_alerts") || propKey.equals("enable_emergency_alerts") || propKey.equals("alert_sound_duration") || propKey.equals("alert_reminder_interval") || propKey.equals("enable_alert_vibrate") || propKey.equals("enable_alert_speech") || propKey.equals("enable_etws_test_alerts") || propKey.equals("enable_channel_50_alerts") || propKey.equals("enable_cmas_test_alerts") || propKey.equals("show_cmas_opt_out_dialog")) {
            value.put(propKey, Integer.valueOf(Integer.parseInt(propValue)));
        } else if (DBG) {
            logd("Invalid column name");
        }
        resolver.update(SubscriptionManager.CONTENT_URI, value, "_id=" + Integer.toString(subId), null);
        Binder.restoreCallingIdentity(token);
    }

    public String getSubscriptionProperty(int subId, String propKey, String callingPackage) {
        if (!canReadPhoneState(callingPackage, "getSubInfoUsingSlotIdWithCheck")) {
            return null;
        }
        String resultValue = null;
        ContentResolver resolver = this.mContext.getContentResolver();
        Uri uri = SubscriptionManager.CONTENT_URI;
        String[] strArr = new String[1];
        strArr[0] = propKey;
        String str = InboundSmsHandler.SELECT_BY_ID;
        String[] strArr2 = new String[1];
        strArr2[0] = subId + UsimPBMemInfo.STRING_NOT_SET;
        Cursor cursor = resolver.query(uri, strArr, str, strArr2, null);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    if (!propKey.equals("enable_cmas_extreme_threat_alerts")) {
                        if (!(propKey.equals("enable_cmas_severe_threat_alerts") || propKey.equals("enable_cmas_amber_alerts") || propKey.equals("enable_emergency_alerts") || propKey.equals("alert_sound_duration") || propKey.equals("alert_reminder_interval") || propKey.equals("enable_alert_vibrate") || propKey.equals("enable_alert_speech") || propKey.equals("enable_etws_test_alerts") || propKey.equals("enable_channel_50_alerts") || propKey.equals("enable_cmas_test_alerts") || propKey.equals("show_cmas_opt_out_dialog"))) {
                            if (DBG) {
                                logd("Invalid column name");
                            }
                        }
                    }
                    resultValue = cursor.getInt(0) + UsimPBMemInfo.STRING_NOT_SET;
                } else if (DBG) {
                    logd("Valid row not present in db");
                }
            } catch (Throwable th) {
                if (cursor != null) {
                    cursor.close();
                }
            }
        } else if (DBG) {
            logd("Query failed");
        }
        if (cursor != null) {
            cursor.close();
        }
        if (DBG) {
            logd("getSubscriptionProperty Query value = " + resultValue);
        }
        return resultValue;
    }

    private static void printStackTrace(String msg) {
        RuntimeException re = new RuntimeException();
        slogd("StackTrace - " + msg);
        boolean first = true;
        for (StackTraceElement ste : re.getStackTrace()) {
            if (first) {
                first = false;
            } else {
                slogd(ste.toString());
            }
        }
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.DUMP", "Requires DUMP");
        long token = Binder.clearCallingIdentity();
        try {
            pw.println("SubscriptionController:");
            pw.println(" defaultSubId=" + getDefaultSubId());
            pw.println(" defaultDataSubId=" + getDefaultDataSubId());
            pw.println(" defaultVoiceSubId=" + getDefaultVoiceSubId());
            pw.println(" defaultSmsSubId=" + getDefaultSmsSubId());
            pw.println(" defaultDataPhoneId=" + SubscriptionManager.from(this.mContext).getDefaultDataPhoneId());
            pw.println(" defaultVoicePhoneId=" + SubscriptionManager.getDefaultVoicePhoneId());
            pw.println(" defaultSmsPhoneId=" + SubscriptionManager.from(this.mContext).getDefaultSmsPhoneId());
            pw.flush();
            for (Entry<Integer, Integer> entry : sSlotIdxToSubId.entrySet()) {
                pw.println(" sSlotIdxToSubId[" + entry.getKey() + "]: subId=" + entry.getValue());
            }
            pw.flush();
            pw.println("++++++++++++++++++++++++++++++++");
            List<SubscriptionInfo> sirl = getActiveSubscriptionInfoList(this.mContext.getOpPackageName());
            if (sirl != null) {
                pw.println(" ActiveSubInfoList:");
                for (SubscriptionInfo entry2 : sirl) {
                    pw.println("  " + entry2.toString());
                }
            } else {
                pw.println(" ActiveSubInfoList: is null");
            }
            pw.flush();
            pw.println("++++++++++++++++++++++++++++++++");
            sirl = getAllSubInfoList(this.mContext.getOpPackageName());
            if (sirl != null) {
                pw.println(" AllSubInfoList:");
                for (SubscriptionInfo entry22 : sirl) {
                    pw.println("  " + entry22.toString());
                }
            } else {
                pw.println(" AllSubInfoList: is null");
            }
            pw.flush();
            pw.println("++++++++++++++++++++++++++++++++");
            this.mLocalLog.dump(fd, pw, args);
            pw.flush();
            pw.println("++++++++++++++++++++++++++++++++");
            pw.flush();
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    public SubscriptionInfo getSubscriptionInfo(int subId) {
        if (!canReadPhoneState(this.mContext.getOpPackageName(), "getSubscriptionInfo")) {
            return null;
        }
        if (SubscriptionManager.isValidSubscriptionId(subId)) {
            ContentResolver contentResolver = this.mContext.getContentResolver();
            Uri uri = SubscriptionManager.CONTENT_URI;
            String str = InboundSmsHandler.SELECT_BY_ID;
            String[] strArr = new String[1];
            strArr[0] = Long.toString((long) subId);
            Cursor cursor = contentResolver.query(uri, null, str, strArr, null);
            if (cursor != null) {
                try {
                    if (cursor.moveToFirst()) {
                        SubscriptionInfo si = getSubInfoRecord(cursor);
                        if (si != null) {
                            if (DBG) {
                                logd("[getSubscriptionInfo]+ subId=" + subId + ", subInfo=" + si);
                            }
                            if (cursor != null) {
                                cursor.close();
                            }
                            return si;
                        }
                    }
                } catch (Throwable th) {
                    if (cursor != null) {
                        cursor.close();
                    }
                }
            } else {
                logd("[getSubscriptionInfo]- Query fail");
            }
            if (cursor != null) {
                cursor.close();
            }
            if (DBG) {
                logd("[getSubscriptionInfo]- subId=" + subId + ",subInfo=null");
            }
            return null;
        }
        logd("[getSubscriptionInfo]- invalid subId, subId =" + subId);
        return null;
    }

    public SubscriptionInfo getSubscriptionInfoForIccId(String iccId) {
        if (!canReadPhoneState(this.mContext.getOpPackageName(), "getSubscriptionInfo")) {
            return null;
        }
        if (iccId == null) {
            logd("[getSubscriptionInfoForIccId]- null iccid");
            return null;
        }
        String[] strArr = new String[1];
        strArr[0] = iccId;
        Cursor cursor = this.mContext.getContentResolver().query(SubscriptionManager.CONTENT_URI, null, "icc_id=?", strArr, null);
        if (cursor != null) {
            SubscriptionInfo si;
            do {
                try {
                    if (cursor.moveToNext()) {
                        si = getSubInfoRecord(cursor);
                    }
                } catch (Throwable th) {
                    if (cursor != null) {
                        cursor.close();
                    }
                    throw th;
                }
            } while (si == null);
            if (DBG) {
                logd("[getSubscriptionInfoForIccId]+ iccId=" + iccId + ", subInfo=" + si);
            }
            if (cursor != null) {
                cursor.close();
            }
            return si;
        }
        logd("[getSubscriptionInfoForIccId]- Query fail");
        if (cursor != null) {
            cursor.close();
        }
        if (DBG) {
            logd("[getSubscriptionInfoForIccId]- iccId=" + iccId + ",subInfo=null");
        }
        return null;
    }

    public void setDefaultDataSubIdWithoutCapabilitySwitch(int subId) {
        if (subId == Integer.MAX_VALUE) {
            throw new RuntimeException("setDefaultDataSubId called with DEFAULT_SUB_ID");
        }
        if (ENGDEBUG && DBG) {
            logd("[setDefaultDataSubId] subId=" + subId);
        }
        updateAllDataConnectionTrackers();
        Global.putInt(this.mContext.getContentResolver(), "multi_sim_data_call", subId);
        broadcastDefaultDataSubIdChanged(subId);
    }

    public void notifySubscriptionInfoChanged(Intent intent) {
        try {
            ITelephonyRegistry.Stub.asInterface(ServiceManager.getService("telephony.registry")).notifySubscriptionInfoChanged();
        } catch (RemoteException e) {
        }
        setReadyState(true);
        broadcastSimInfoContentChanged(intent);
    }

    public void removeStickyIntent() {
        synchronized (this.mLock) {
            if (sStickyIntent != null) {
                if (DBG) {
                    logd("removeStickyIntent");
                }
                this.mContext.removeStickyBroadcast(sStickyIntent);
                sStickyIntent = null;
            }
        }
    }

    public boolean isReady() {
        return this.mIsReady;
    }

    public void setReadyState(boolean isReady) {
        this.mIsReady = isReady;
    }

    public List<SubInfoRecord> getActiveSubInfoList(String callingPackage) {
        if (DBG) {
            logdl("[getActiveSubInfoList]+");
        }
        if (!canReadPhoneState(callingPackage, "getActiveSubInfoList")) {
            return null;
        }
        long identity = Binder.clearCallingIdentity();
        try {
            if (isSubInfoReady()) {
                List<SubInfoRecord> subList = colorgetSubInfo("sim_id>=0", null);
                if (subList != null) {
                    Collections.sort(subList, new AnonymousClass2(this));
                    if (DBG) {
                        logdl("[getActiveSubInfoList]- " + subList.size() + " infos return");
                    }
                } else if (DBG) {
                    logdl("[getActiveSubInfoList]- no info return");
                }
                Binder.restoreCallingIdentity(identity);
                return subList;
            }
            if (DBG) {
                logdl("[getActiveSubInfoList] Sub Controller not ready");
            }
            Binder.restoreCallingIdentity(identity);
            return null;
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(identity);
        }
    }

    private List<SubInfoRecord> colorgetSubInfo(String selection, Object queryKey) {
        Throwable th;
        logd("selection:" + selection + " " + queryKey);
        String[] strArr = null;
        if (queryKey != null) {
            strArr = new String[1];
            strArr[0] = queryKey.toString();
        }
        List<SubInfoRecord> subList = null;
        Cursor cursor = this.mContext.getContentResolver().query(SubscriptionManager.CONTENT_URI, null, selection, strArr, "sim_id ASC");
        if (cursor != null) {
            while (true) {
                ArrayList<SubInfoRecord> subList2;
                ArrayList<SubInfoRecord> subList3 = subList2;
                try {
                    if (!cursor.moveToNext()) {
                        Object subList4 = subList3;
                        break;
                    }
                    SubInfoRecord subInfo = colorgetSubInfoRecord(cursor);
                    if (subInfo != null) {
                        if (subList3 == null) {
                            subList2 = new ArrayList();
                        } else {
                            subList2 = subList3;
                        }
                        try {
                            subList2.add(subInfo);
                        } catch (Throwable th2) {
                            th = th2;
                        }
                    } else {
                        subList2 = subList3;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    if (cursor != null) {
                        cursor.close();
                    }
                    throw th;
                }
            }
        }
        logd("Query fail");
        if (cursor != null) {
            cursor.close();
        }
        return subList4;
    }

    private SubInfoRecord colorgetSubInfoRecord(Cursor cursor) {
        int id = cursor.getInt(cursor.getColumnIndexOrThrow("_id"));
        String iccId = cursor.getString(cursor.getColumnIndexOrThrow(SimInfo.ICC_ID));
        int simSlotIndex = cursor.getInt(cursor.getColumnIndexOrThrow(TextBasedCbSmsColumns.SIM_ID));
        String displayName = cursor.getString(cursor.getColumnIndexOrThrow(SimInfo.DISPLAY_NAME));
        String carrierName = cursor.getString(cursor.getColumnIndexOrThrow("carrier_name"));
        int nameSource = cursor.getInt(cursor.getColumnIndexOrThrow(SimInfo.NAME_SOURCE));
        int iconTint = cursor.getInt(cursor.getColumnIndexOrThrow(SimInfo.COLOR));
        String number = cursor.getString(cursor.getColumnIndexOrThrow("number"));
        int dataRoaming = cursor.getInt(cursor.getColumnIndexOrThrow(SimInfo.DATA_ROAMING));
        Bitmap iconBitmap = BitmapFactory.decodeResource(this.mContext.getResources(), 17302585);
        int mcc = cursor.getInt(cursor.getColumnIndexOrThrow("mcc"));
        int mnc = cursor.getInt(cursor.getColumnIndexOrThrow("mnc"));
        String countryIso = getSubscriptionCountryIso(id);
        int mStatus = cursor.getInt(cursor.getColumnIndexOrThrow("sub_state"));
        if (DBG) {
            logd("[colorgetSubInfoRecord] id:" + id + " iccid:" + iccId + " simSlotIndex:" + simSlotIndex + " displayName:" + displayName + " nameSource:" + nameSource + " iconTint:" + iconTint + " dataRoaming:" + dataRoaming + " mcc:" + mcc + " mnc:" + mnc + " countIso:" + countryIso + " mStatus:" + mStatus);
        }
        String line1Number = this.mTelephonyManager.getLine1Number(id);
        if (!(TextUtils.isEmpty(line1Number) || line1Number.equals(number))) {
            logd("Line1Number is different: " + line1Number);
            number = line1Number;
        }
        return new SubInfoRecord(id, iccId, simSlotIndex, displayName, carrierName, nameSource, iconTint, number, dataRoaming, iconBitmap, mcc, mnc, countryIso, mStatus, -1);
    }

    private void enforceSubscriptionPermission() {
        if (this.mContext != null) {
            this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PHONE_STATE", "Requires READ_PHONE_STATE");
        }
    }

    public List<SubInfoRecord> colorgetAllSubInfoList() {
        logd("[getAllSubInfoList]+");
        enforceSubscriptionPermission();
        List<SubInfoRecord> subList = colorgetSubInfo(null, null);
        if (subList != null) {
            logd("[getAllSubInfoList]- " + subList.size() + " infos return");
        } else {
            logd("[getAllSubInfoList]- no info return");
        }
        return subList;
    }

    public List<SubInfoRecord> getActiveSubInfoList() {
        enforceSubscriptionPermission();
        if (DBG) {
            logdl("[getActiveSubInfoList]+");
        }
        if (isSubInfoReady()) {
            List<SubInfoRecord> subList = colorgetSubInfo("sim_id>=0", null);
            if (subList != null) {
                Collections.sort(subList, new AnonymousClass3(this));
                if (DBG) {
                    logdl("[getActiveSubInfoList]- " + subList.size() + " infos return");
                }
            } else if (DBG) {
                logdl("[getActiveSubInfoList]- no info return");
            }
            return subList;
        }
        if (DBG) {
            logdl("[getActiveSubInfoList] Sub Controller not ready");
        }
        return null;
    }

    public SubInfoRecord colorgetActiveSubscriptionInfo(int subId) {
        enforceSubscriptionPermission();
        if (SubscriptionManager.isValidSubscriptionId(subId) && isSubInfoReady()) {
            List<SubInfoRecord> subList = getActiveSubInfoList();
            if (subList != null) {
                for (SubInfoRecord si : subList) {
                    if (si.getSubscriptionId() == subId) {
                        if (DBG) {
                            logd("[getActiveSubInfoForSubscriber]+ subId=" + subId + " subInfo=" + si);
                        }
                        return si;
                    }
                }
            }
            if (DBG) {
                logd("[getActiveSubInfoForSubscriber]- subId=" + subId + " subList=" + subList + " subInfo=null");
            }
            return null;
        }
        logd("[getSubInfoUsingSubIdx]- invalid subId or not ready = " + subId);
        return null;
    }

    public void stortUserConfig(int value) {
        int mValue = value;
        String name = "oppo_multi_sim_user_config_dataconnect";
        if (value > 0) {
            mValue = SubscriptionManager.getSlotId(value);
        } else {
            mValue = -5;
        }
        Global.putInt(this.mContext.getContentResolver(), name, mValue);
    }

    public void temporarySwitchDataSubId(int subId) {
        setDefaultDataSubId(subId);
        stortUserConfig(subId);
    }

    public SubInfoRecord colorgetActiveSubscriptionInfoForIccId(String iccId) {
        enforceSubscriptionPermission();
        List<SubInfoRecord> subList = getActiveSubInfoList();
        if (subList != null) {
            for (SubInfoRecord si : subList) {
                if (si.getIccId() == iccId) {
                    if (DBG) {
                        logd("[colorgetActiveSubscriptionInfoForIccId]+ iccId=" + iccId + " subInfo=" + si);
                    }
                    return si;
                }
            }
        }
        if (DBG) {
            logd("[colorgetActiveSubscriptionInfoForIccId]+ iccId=" + iccId + " subList=" + subList + " subInfo=null");
        }
        return null;
    }

    public List<SubInfoRecord> colorgetSubInfoUsingSlotIdWithCheck(int slotId, boolean needCheck, String callingPackage) {
        Throwable th;
        if (!canReadPhoneState(callingPackage, "colorgetSubInfoUsingSlotIdWithCheck")) {
            return null;
        }
        if (slotId == Integer.MAX_VALUE) {
            slotId = getSlotId(getDefaultSubId());
        }
        if (!SubscriptionManager.isValidSlotId(slotId)) {
            if (DBG) {
                logd("[getSubInfoUsingSlotIdWithCheck]- invalid slotId");
            }
            return null;
        } else if (!needCheck || isSubInfoReady()) {
            long identity = Binder.clearCallingIdentity();
            try {
                String[] strArr = new String[1];
                strArr[0] = String.valueOf(slotId);
                Cursor cursor = this.mContext.getContentResolver().query(SubscriptionManager.CONTENT_URI, null, "sim_id=?", strArr, null);
                List<SubInfoRecord> subList = null;
                if (cursor != null) {
                    while (true) {
                        ArrayList<SubInfoRecord> subList2;
                        ArrayList<SubInfoRecord> subList3 = subList2;
                        try {
                            if (!cursor.moveToNext()) {
                                subList = subList3;
                                break;
                            }
                            SubInfoRecord subInfo = colorgetSubInfoRecord(cursor);
                            if (subInfo != null) {
                                if (subList3 == null) {
                                    subList2 = new ArrayList();
                                } else {
                                    subList2 = subList3;
                                }
                                try {
                                    subList2.add(subInfo);
                                } catch (Throwable th2) {
                                    th = th2;
                                }
                            } else {
                                subList2 = subList3;
                            }
                        } catch (Throwable th3) {
                            th = th3;
                            subList2 = subList3;
                            if (cursor != null) {
                                cursor.close();
                            }
                            throw th;
                        }
                    }
                }
                if (cursor != null) {
                    cursor.close();
                }
                Binder.restoreCallingIdentity(identity);
                return subList;
            } catch (Throwable th4) {
                Binder.restoreCallingIdentity(identity);
            }
        } else {
            if (DBG) {
                logd("[getSubInfoUsingSlotIdWithCheck]- not ready");
            }
            return null;
        }
    }

    public List<SubInfoRecord> getSubInfoUsingSlotId(int slotId, String callingPackage) {
        return colorgetSubInfoUsingSlotIdWithCheck(slotId, true, callingPackage);
    }

    public SubInfoRecord getSubInfoForSubscriber(int subId, String callingPackage) {
        if (subId == Integer.MAX_VALUE) {
            subId = getDefaultSubId();
        }
        if (SubscriptionManager.isValidSubscriptionId(subId) && isSubInfoReady()) {
            long identity = Binder.clearCallingIdentity();
            Cursor cursor;
            try {
                ContentResolver contentResolver = this.mContext.getContentResolver();
                Uri uri = SubscriptionManager.CONTENT_URI;
                String str = InboundSmsHandler.SELECT_BY_ID;
                String[] strArr = new String[1];
                strArr[0] = Long.toString((long) subId);
                cursor = contentResolver.query(uri, null, str, strArr, null);
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        SubInfoRecord colorgetSubInfoRecord = colorgetSubInfoRecord(cursor);
                        if (cursor != null) {
                            cursor.close();
                        }
                        Binder.restoreCallingIdentity(identity);
                        return colorgetSubInfoRecord;
                    }
                }
                if (cursor != null) {
                    cursor.close();
                }
                if (DBG) {
                    logd("[getSubInfoForSubscriber]- null info return");
                }
                Binder.restoreCallingIdentity(identity);
                return null;
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(identity);
            }
        } else {
            if (DBG) {
                logd("[getSubInfoForSubscriberx]- invalid subId or not ready, subId = " + subId);
            }
            return null;
        }
    }

    public void setDefaultApplication(String packageName) {
        SmsApplication.setDefaultApplication(packageName, this.mContext);
    }

    private int getSubStatus(String iccid) {
        return RadioManager.getInstance().getSubStatus(iccid);
    }

    public boolean isCTCCard(int slotId) {
        boolean vRet = false;
        Phone phone = PhoneFactory.getPhone(slotId);
        if (phone != null) {
            String[] vCdmaImsi = phone.getLteCdmaImsi(slotId);
            if (!(vCdmaImsi[0] == null || vCdmaImsi[1] == null)) {
                vRet = !innerCTCCard(vCdmaImsi[0], slotId) ? innerCTCCard(vCdmaImsi[1], slotId) : true;
                if (DBG) {
                    Log.d(LOG_TAG, "isCTCCard-->return->" + vRet);
                }
            }
        } else if (DBG) {
            Log.d(LOG_TAG, "isCTCCard-->Failed");
        }
        return vRet;
    }

    private boolean innerCTCCard(String vimsi, int slotId) {
        String iccid;
        String mccmnc = "00101";
        if (DBG) {
            Log.d(LOG_TAG, "isCTCCard vimsi-->" + vimsi);
        }
        int[] subIdA = getSubId(slotId);
        if (subIdA != null) {
            iccid = TelephonyManager.getDefault().getSimSerialNumber(subIdA[0]);
            if (DBG) {
                Log.d(LOG_TAG, "isCTCCard iccidAAAAAA-->" + iccid);
            }
        }
        if (vimsi == null || vimsi.length() <= 5) {
            int[] subId = getSubId(slotId);
            if (subId == null) {
                return false;
            }
            iccid = TelephonyManager.getDefault().getSimSerialNumber(subId[0]);
            if (DBG) {
                Log.d(LOG_TAG, "isCTCCard iccid-->" + iccid);
            }
            if (iccid == null) {
                return false;
            }
            if (iccid.length() < 6) {
                return false;
            }
            String vOperatorStr = iccid.substring(0, 6);
            if (vOperatorStr.equals("898603") || vOperatorStr.equals("898611")) {
                return true;
            }
            return false;
        }
        mccmnc = vimsi.substring(0, 5);
        if (mccmnc.equals("46003") || mccmnc.equals("46011") || mccmnc.equals("45502")) {
            return true;
        }
        return false;
    }

    public boolean checkUsimIs4g(int slotId) {
        boolean vRet = false;
        Phone phone = PhoneFactory.getPhone(slotId);
        if (phone != null) {
            vRet = phone.OppoCheckUsimIs4G();
            if (DBG) {
                logd("checkUsimIs4g--->" + vRet);
            }
        }
        return vRet;
    }

    private String getSoftSimIccid(Context context) {
        String icc_id = UsimPBMemInfo.STRING_NOT_SET;
        String[] columns = new String[2];
        columns[0] = SimInfo.SLOT;
        columns[1] = "iccid";
        Cursor cursor = context.getContentResolver().query(Uri.parse("content://com.redteamobile.provider"), columns, null, null, null);
        if (cursor == null) {
            logdl("getSoftSimIccid: cursor is empty");
            return icc_id;
        }
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    while (true) {
                        String slot = cursor.getString(cursor.getColumnIndex(SimInfo.SLOT));
                        String iccid = cursor.getString(cursor.getColumnIndex("iccid"));
                        logdl("getSoftSimIccid: slot = " + slot + " ,iccid = " + iccid);
                        if (TextUtils.isEmpty(iccid)) {
                            if (!cursor.moveToNext()) {
                                break;
                            }
                        } else {
                            icc_id = iccid;
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                if (cursor != null) {
                    cursor.close();
                }
            } catch (Throwable th) {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        return icc_id;
    }

    public boolean isHasSoftSimCard() {
        return mHasSoftSimCard;
    }

    public int getSoftSimCardSlotId() {
        return mSoftSimSlotId;
    }

    public boolean isSMSPromptEnabled() {
        int value = 0;
        try {
            value = Global.getInt(this.mContext.getContentResolver(), "multi_sim_sms_prompt");
        } catch (SettingNotFoundException e) {
            loge("Settings Exception Reading Dual Sim SMS Prompt Values");
        }
        boolean prompt = value != 0;
        if (VDBG) {
            logd("SMS Prompt option:" + prompt);
        }
        return prompt;
    }

    public void setSMSPromptEnabled(boolean enabled) {
        Global.putInt(this.mContext.getContentResolver(), "multi_sim_sms_prompt", !enabled ? 0 : 1);
        logd("setSMSPromptOption to " + enabled);
    }

    public void setRadioPower(int subId, int slot, boolean isChecked) {
        if (!SubscriptionManager.isValidSlotId(slot)) {
            logd("setRadioPower, slot id is invalid");
        } else if (!OemConstant.isUiccSlotForbid(slot)) {
            int currentSimMode;
            boolean isRadioOn;
            int priviousSimMode = Global.getInt(this.mContext.getContentResolver(), "msim_mode_setting", -1);
            logd("setRadioPower,sim mode before changed:" + priviousSimMode + ", slot:" + slot);
            int modeSlot = 1 << slot;
            if ((priviousSimMode & modeSlot) > 0) {
                currentSimMode = priviousSimMode & (~modeSlot);
                isRadioOn = false;
            } else {
                currentSimMode = priviousSimMode | modeSlot;
                isRadioOn = true;
            }
            logd("SimMode change to:" + currentSimMode + ", isRadioOn=" + isRadioOn);
            this.mTelephonyManager.setRadioForSubscriber(subId, isChecked);
        }
    }

    public void activateSubId(int subId) {
        if (getSubState(subId) == 1) {
            logd("activateSubId: subscription already active, subId = " + subId);
        } else if (OemConstant.EXP_VERSION && RegionLockConstant.IS_REGION_LOCK && RegionLockConstant.getRegionLockStatus() && SystemProperties.get(RegionLockConstant.NOTIFY_NETLOCK_FLAG, "0").equals("1")) {
            logd("activateSubId: subscription already occurs region-lock do not allow activeSubId");
        } else {
            setRadioPower(subId, getSlotId(subId), true);
        }
    }

    public void deactivateSubId(int subId) {
        if (getSubState(subId) == 0) {
            logd("activateSubId: subscription already deactivated, subId = " + subId);
        } else {
            setRadioPower(subId, getSlotId(subId), false);
        }
    }

    private void broadcastSubStateChanged(int subId, int state) {
        int slot = SubscriptionManager.getSlotId(subId);
        logd("broadcastSubStateChanged slot:" + slot + " state:" + state + " simState:" + TelephonyManager.getDefault().getSimState(slot));
        if (slot < 0 || slot >= sPhones.length) {
            logd("broadcastSubStateChanged slot is unavailable slot:" + slot);
        } else {
            boolean isIccRecordLoaded = sPhones[slot].getIccRecordsLoaded();
            logd("broadcastSubStateChanged slot:" + slot + " , isIccRecordLoaded:" + isIccRecordLoaded);
            if (!isIccRecordLoaded) {
                logd("broadcastSubStateChanged slot:" + slot + " return ,due to record not loaded !isIccRecordLoaded:" + isIccRecordLoaded);
                return;
            }
        }
        Intent intent = new Intent("android.intent.action.SIM_STATE_CHANGED");
        intent.putExtra("phoneName", "Phone");
        if (state == 1) {
            intent.putExtra("ss", "LOADED");
            intent.putExtra(DataSubSelector.EXTRA_MOBILE_DATA_ENABLE_REASON, UsimPBMemInfo.STRING_NOT_SET);
            intent.putExtra("typeForSubStateChange", "true");
            SubscriptionManager.putPhoneIdAndSubIdExtra(intent, slot);
            logd("Broadcasting intent ACTION_SIM_STATE_CHANGED  for mSlotId : " + slot);
            this.mContext.sendBroadcast(intent, "android.permission.READ_PHONE_STATE");
        }
    }

    public int setSubState(int subId, int subStatus) {
        logd("setSubState, subStatus: " + subStatus + " subId: " + subId);
        ContentValues value = new ContentValues(1);
        value.put("sub_state", Integer.valueOf(subStatus));
        int result = this.mContext.getContentResolver().update(SubscriptionManager.CONTENT_URI, value, "_id=" + Long.toString((long) subId), null);
        if (this.mActiveList != null && this.mActiveList.size() > 0) {
            for (SubscriptionInfo record : this.mActiveList) {
                if (record.getSubscriptionId() == subId) {
                    record.mStatus = subStatus;
                }
            }
        }
        broadcastSimInfoContentChanged(subId, "sub_state", subStatus, INVALID_ICCID);
        broadcastSubStateChanged(subId, subStatus);
        return result;
    }

    public int getSubState(int subId) {
        logd("getSubState()");
        SubInfoRecord subInfo = getSubInfoForSubscriber(subId, this.mContext.getOpPackageName());
        if (subInfo == null || subInfo.slotId < 0) {
            return 0;
        }
        return subInfo.mStatus;
    }

    public boolean isVoicePromptEnabled() {
        int value = 0;
        try {
            value = Global.getInt(this.mContext.getContentResolver(), "multi_sim_voice_prompt");
        } catch (SettingNotFoundException e) {
            loge("Settings Exception Reading Dual Sim Voice Prompt Values");
        }
        boolean prompt = value != 0;
        if (VDBG) {
            logd("Voice Prompt option:" + prompt);
        }
        return prompt;
    }

    public void setVoicePromptEnabled(boolean enabled) {
        Global.putInt(this.mContext.getContentResolver(), "multi_sim_voice_prompt", !enabled ? 0 : 1);
        logd("setVoicePromptOption to " + enabled);
    }

    public int getOnDemandDataSubId() {
        return 0;
    }

    public boolean isUsimWithCsim(int slotId) {
        String[] cardType;
        UiccCard card = PhoneFactory.getPhone(slotId).getUiccCard();
        if (card != null) {
            cardType = card.getFullIccCardType();
        } else {
            cardType = new String[1];
            cardType[0] = UsimPBMemInfo.STRING_NOT_SET;
        }
        HashSet<String> fullUiccType = new HashSet(Arrays.asList(cardType));
        if (fullUiccType.contains("USIM")) {
            return fullUiccType.contains("CSIM");
        }
        return false;
    }

    public boolean colorIsImsRegistered(int slotId) {
        boolean vRet = false;
        Phone phone = PhoneFactory.getPhone(slotId);
        if (phone != null) {
            vRet = phone.isImsRegistered();
        }
        if (DBG) {
            logd("colorIsImsRegistered--->" + vRet);
        }
        return vRet;
    }

    public int setDisplayNumber(String number, int subId, boolean writeToSim) {
        if (DBG) {
            logd("[setDisplayNumber]+ number:" + number + " subId:" + subId + ", writeToSim:" + writeToSim);
        }
        enforceModifyPhoneState("setDisplayNumber");
        validateSubId(subId);
        int result = 0;
        int phoneId = getPhoneId(subId);
        if (number == null || phoneId < 0 || phoneId >= TelephonyManager.getDefault().getPhoneCount()) {
            logd("[setDispalyNumber]- fail");
            return -1;
        }
        ContentValues value = new ContentValues(1);
        value.put("number", number);
        if (DBG) {
            logd("[setDisplayNumber]- number:" + number + " set");
        }
        if (writeToSim) {
            Phone phone = sPhones[phoneId];
            String alphaTag = TelephonyManager.from(this.mContext).getLine1AlphaTag(subId);
            synchronized (this.mLock) {
                this.mSuccess = false;
                this.mSuccess = phone.setLine1Number(alphaTag, number, this.mHandler.obtainMessage(1));
                loge("[setDisplayNumber]setLine1Number result is :" + this.mSuccess);
                if (this.mSuccess) {
                    try {
                        this.mLock.wait(3000);
                    } catch (InterruptedException e) {
                        loge("interrupted while trying to write MSISDN");
                    }
                } else {
                    loge("[setDisplayNumber]setLine1Number fail due to iccrecord is null");
                }
            }
        }
        if (this.mSuccess || !writeToSim) {
            result = this.mContext.getContentResolver().update(SubscriptionManager.CONTENT_URI, value, "_id=" + Long.toString((long) subId), null);
            if (DBG) {
                logd("[setDisplayNumber]- update result :" + result);
            }
            if (this.mActiveList != null && result > 0) {
                for (SubscriptionInfo record : this.mActiveList) {
                    if (record.getSubscriptionId() == subId) {
                        record.setNumber(number);
                    }
                }
            }
            notifySubscriptionInfoChanged();
        }
        return result;
    }

    private void broadcastSimInfoContentChanged(int subId, String columnName, int intContent, String stringContent) {
        Intent intent = new Intent("android.intent.action.ACTION_SUBINFO_CONTENT_CHANGE");
        intent.putExtra("_id", subId);
        intent.putExtra("columnName", columnName);
        intent.putExtra("intContent", intContent);
        intent.putExtra("stringContent", stringContent);
        if (intContent != -100) {
            logd("[broadcastSimInfoContentChanged] subId" + subId + " changed, " + columnName + " -> " + intContent);
        } else {
            logd("[broadcastSimInfoContentChanged] subId" + subId + " changed, " + columnName + " -> " + stringContent);
        }
        this.mContext.sendBroadcast(intent);
    }

    public static String getCarrierName(Context context, String name, String imsi, String iccid, int slotid) {
        String operatorNumic = UsimPBMemInfo.STRING_NOT_SET;
        String plmn = UsimPBMemInfo.STRING_NOT_SET;
        if (!TextUtils.isEmpty(name)) {
            plmn = name;
        } else if (!TextUtils.isEmpty(imsi) && imsi.length() >= 5) {
            operatorNumic = imsi.substring(0, 5);
        } else if (TextUtils.isEmpty(iccid)) {
            operatorNumic = UsimPBMemInfo.STRING_NOT_SET;
        } else if (iccid.startsWith("898600") || iccid.startsWith("986800")) {
            operatorNumic = "46000";
        } else if (iccid.startsWith("898601") || iccid.startsWith("986810")) {
            operatorNumic = "46001";
        } else if (iccid.startsWith("898602")) {
            operatorNumic = "46002";
        } else if (iccid.startsWith("898603") || iccid.startsWith("986830") || iccid.startsWith("898606") || iccid.startsWith("898611")) {
            operatorNumic = "46003";
        } else if (iccid.startsWith("898520")) {
            operatorNumic = "45407";
        }
        if (!TextUtils.isEmpty(operatorNumic)) {
            plmn = getOemOperator(context, operatorNumic);
        }
        if (TextUtils.isEmpty(plmn)) {
            plmn = "SIM";
        }
        String mSimConfig = SystemProperties.get("persist.radio.multisim.config", UsimPBMemInfo.STRING_NOT_SET);
        if (mHasSoftSimCard && mSoftSimSlotId == slotid) {
            plmn = context.getString(17041014);
        } else if (mSimConfig.equals("dsds") || mSimConfig.equals("dsda")) {
            plmn = plmn + Integer.toString(slotid + 1);
        }
        Rlog.d(LOG_TAG, "[getCarrierName]- name:" + name + " iccid:" + iccid + " plmn:" + plmn);
        return plmn;
    }

    public static String getOemOperator(Context context, String plmn) {
        if (TextUtils.isEmpty(plmn)) {
            return UsimPBMemInfo.STRING_NOT_SET;
        }
        try {
            return OppoTelephonyFunction.oppoGetPlmnOverride(context, plmn, null);
        } catch (Exception e) {
            Rlog.d(LOG_TAG, "leon getCarrierName no res," + plmn);
            return UsimPBMemInfo.STRING_NOT_SET;
        }
    }

    public String getExportSimDefaultName(int slotId) {
        String simName = "SIM1";
        if (ColorOSTelephonyManager.getDefault(this.mContext).isOppoSingleSimCard()) {
            return "SIM";
        }
        if (slotId == 0) {
            return "SIM1";
        }
        if (slotId == 1) {
            return "SIM2";
        }
        return simName;
    }

    public boolean getSwitchingDssState(int phoneId) {
        if (phoneId == 0) {
            return this.inSwitchingDssState1;
        }
        return this.inSwitchingDssState2;
    }

    public void setSwitchingDssState(int phoneId, boolean state) {
        if (phoneId == 0) {
            this.inSwitchingDssState1 = state;
        } else {
            this.inSwitchingDssState2 = state;
        }
    }

    public String getOperatorNumericForData(int phoneId) {
        if (sPhones[phoneId] == null || sPhones[phoneId].mDcTracker == null) {
            return UsimPBMemInfo.STRING_NOT_SET;
        }
        return sPhones[phoneId].mDcTracker.getOperatorNumeric();
    }
}
