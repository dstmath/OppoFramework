package com.mediatek.appworkingset;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Binder;
import android.os.Build.VERSION;
import android.os.Debug;
import android.os.Process;
import android.os.SystemProperties;
import android.os.Trace;
import android.os.UserHandle;
import android.util.ArrayMap;
import android.util.Log;
import android.util.SparseArray;
import com.android.internal.app.ProcessMap;
import com.android.internal.util.MemInfoReader;
import com.android.server.ColorOSDeviceIdleHelper;
import com.android.server.LocationManagerService;
import com.mediatek.am.AMEventHookData.BeforeActivitySwitch;
import com.mediatek.am.AMEventHookData.SystemReady;
import com.mediatek.am.AMEventHookData.SystemReady.Index;
import com.mediatek.am.IAWSProcessRecord;
import com.mediatek.am.IAWSStoreRecord;
import com.mediatek.apm.frc.FocusRelationshipChainPolicy;
import com.mediatek.apm.suppression.SuppressionPolicy;
import com.mediatek.aws.CustomProtectProcess;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

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
public final class AWSManager {
    static final int APPLICATION_ACTIVITY_TYPE = 0;
    static final boolean CONFIG_ACCURATE = true;
    static final boolean DEBUG = false;
    static final boolean DEBUG_DB = false;
    static final boolean DEBUG_KILL = false;
    static final boolean DEBUG_PRIORITY = false;
    static final boolean DEBUG_RECORD = false;
    static final boolean DEBUG_SUPPRESS = false;
    static final boolean DEBUG_SWITCH = false;
    static final int HOME_ACTIVITY_TYPE = 1;
    static final int MAX_SUPPRESSION_TIME = 5000;
    private static final int MEM_BACK_TO_HOME = 2;
    private static final int MEM_IN_COMING_CALL = 3;
    private static final int MEM_LAUNCHED_PROCESS = 1;
    private static final int MEM_SAMPLE_PROCESS = 0;
    static final int MIN_DB_COMMIT_TIME = 180000;
    static final int MPO_SUPPRESS_ACTION = 545;
    static final int MaxPkgPriorityNode = 100;
    private static int PERCEPTIBLE_APP_ADJ = 0;
    public static final int PROCESS_STATE_BOUND_FOREGROUND_SERVICE = 3;
    public static final int PROCESS_STATE_TOP = 2;
    static final int RECENTS_ACTIVITY_TYPE = 2;
    private static final String TAG = "AWSManager";
    static final int defaultAppNeeded = 30720;
    static boolean mIsReady;
    static final String[] nativeRecordProcess = null;
    private static AWSManager sInstance;
    private AWSDBHelper db;
    private Timer dbTimer;
    String foregroundApp;
    boolean frcFlag;
    ActivityManager mAm;
    Context mContext;
    private FocusRelationshipChainPolicy mFrcPolicy;
    private String mLaunchingPkgName;
    ArrayMap<String, Integer> mNativePids;
    ArrayMap<String, Integer> mNativeUids;
    int mNumPkgPriorityNode;
    protected PkgPriorityNode mPDirty;
    protected PkgPriorityNode mPHead;
    protected PkgPriorityNode mPTail;
    final ArrayMap<String, PkgPriorityNode> mPackagesProcessMap;
    final ProcessMap<ProcessRecordStore> mProcessNames;
    private SuppressionPolicy mSuppressPolicy;
    private Timer suppressTimer;
    private TimerTask suppressTimerTask;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.mediatek.appworkingset.AWSManager.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.mediatek.appworkingset.AWSManager.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.appworkingset.AWSManager.<clinit>():void");
    }

    public AWSManager(Context context) {
        this.mNativePids = new ArrayMap();
        this.mNativeUids = new ArrayMap();
        this.mProcessNames = new ProcessMap();
        this.mPackagesProcessMap = new ArrayMap();
        this.mPHead = new PkgPriorityNode();
        this.mPTail = new PkgPriorityNode();
        this.foregroundApp = null;
        this.frcFlag = false;
        this.mFrcPolicy = FocusRelationshipChainPolicy.getInstance();
        this.mSuppressPolicy = SuppressionPolicy.getInstance();
        this.suppressTimer = null;
        this.suppressTimerTask = null;
        if (context != null) {
            mIsReady = false;
            this.mNumPkgPriorityNode = 0;
            this.mPDirty = null;
            this.mContext = context;
            this.mAm = (ActivityManager) this.mContext.getSystemService("activity");
            if (VERSION.SDK_INT > 23) {
                Log.v(TAG, "Version > 23, perceptible adj = " + PERCEPTIBLE_APP_ADJ);
            }
            this.dbTimer = null;
            this.db = new AWSDBHelper(context, this);
            if (this.db != null) {
                registerForPackageRemoval(context);
                registerForScreenOnOff(context);
                new Thread(new Runnable() {
                    public void run() {
                        synchronized (AWSManager.this.db) {
                            AWSManager.this.db.readDB();
                        }
                        AWSManager.mIsReady = true;
                        Log.v(AWSManager.TAG, "Initialized done");
                    }
                }).start();
                return;
            }
            Log.e(TAG, "Initializing with something's wrong:" + context + "," + this.db);
            return;
        }
        Log.e(TAG, "Initializing with context's wrong:" + context);
    }

    private void registerForPackageRemoval(Context context) {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.PACKAGE_REMOVED");
        if (context != null) {
            context.registerReceiverAsUser(new BroadcastReceiver() {
                /* Code decompiled incorrectly, please refer to instructions dump. */
                public void onReceive(Context context, Intent intent) {
                    int i;
                    String action = intent.getAction();
                    switch (action.hashCode()) {
                        case 525384130:
                            if (action.equals("android.intent.action.PACKAGE_REMOVED")) {
                                boolean i2 = false;
                                break;
                            }
                        default:
                            i2 = -1;
                            break;
                    }
                    switch (i2) {
                        case 0:
                            if (!intent.getBooleanExtra("android.intent.extra.REPLACING", false)) {
                                i2 = intent.getIntExtra("android.intent.extra.UID", -1);
                                Uri data = intent.getData();
                                if (i2 != -1 && data != null) {
                                    action = data.getSchemeSpecificPart();
                                    Log.v(AWSManager.TAG, "Pkg removal:" + action);
                                    AWSManager.this.clearProcList(action);
                                    AWSManager.this.clearLaunchProcList(action);
                                    break;
                                }
                                return;
                            }
                            return;
                            break;
                    }
                }
            }, UserHandle.ALL, intentFilter, null, null);
            return;
        }
        Log.e(TAG, "context = null");
    }

    private void registerForScreenOnOff(Context context) {
        if (context != null) {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("android.intent.action.SCREEN_OFF");
            intentFilter.addAction("android.intent.action.SCREEN_ON");
            context.registerReceiverAsUser(new BroadcastReceiver() {
                public void onReceive(Context context, Intent intent) {
                    String action = intent.getAction();
                    Object obj = -1;
                    switch (action.hashCode()) {
                        case -2128145023:
                            if (action.equals("android.intent.action.SCREEN_OFF")) {
                                obj = null;
                                break;
                            }
                            break;
                        case -1454123155:
                            if (action.equals("android.intent.action.SCREEN_ON")) {
                                obj = 1;
                                break;
                            }
                            break;
                    }
                    switch (obj) {
                        case null:
                            if (AWSManager.mIsReady) {
                                if (AWSManager.this.dbTimer == null) {
                                    AWSManager.this.dbTimer = new Timer();
                                }
                                AWSManager.this.dbTimer.schedule(new TimerTask() {
                                    public void run() {
                                        new Thread(new Runnable() {
                                            public void run() {
                                                Log.v(AWSManager.TAG, "Updating DB");
                                                synchronized (AWSManager.this.db) {
                                                    AWSManager.this.db.updateDB();
                                                }
                                                if (AWSManager.this.dbTimer != null) {
                                                    AWSManager.this.dbTimer.cancel();
                                                    AWSManager.this.dbTimer = null;
                                                }
                                            }
                                        }).start();
                                    }
                                }, ColorOSDeviceIdleHelper.ALARM_WINDOW_LENGTH);
                                return;
                            }
                            return;
                        case 1:
                            if (AWSManager.this.dbTimer != null) {
                                AWSManager.this.dbTimer.cancel();
                                AWSManager.this.dbTimer = null;
                                return;
                            }
                            return;
                        default:
                            return;
                    }
                }
            }, UserHandle.ALL, intentFilter, null, null);
            return;
        }
        Log.e(TAG, "context = null");
    }

    public static AWSManager getInstance(SystemReady systemReady) {
        if (SystemProperties.get("ro.mtk_aws_support").equals(LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON)) {
            if (sInstance == null) {
                sInstance = new AWSManager((Context) systemReady.get(Index.context));
            }
            return sInstance;
        }
        Log.d(TAG, "AWSManager not enabled");
        return null;
    }

    public static AWSManager getInstance() {
        if (!SystemProperties.get("ro.mtk_aws_support").equals(LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON)) {
            Log.e(TAG, "AWSManager not enabled");
            return null;
        } else if (sInstance != null) {
            return sInstance;
        } else {
            Log.v(TAG, "AWSManager get null instance, system not ready?");
            return null;
        }
    }

    public int onBeforeActivitySwitch(BeforeActivitySwitch beforeActivitySwitch) {
        if (!IsSystemAndReady("onBeforeActivitySwitch")) {
            return 0;
        }
        boolean z = beforeActivitySwitch.getBoolean(BeforeActivitySwitch.Index.isNeedToPauseActivityFirst);
        String string = beforeActivitySwitch.getString(BeforeActivitySwitch.Index.lastResumedPackageName);
        String string2 = beforeActivitySwitch.getString(BeforeActivitySwitch.Index.nextResumedPackageName);
        int i = beforeActivitySwitch.getInt(BeforeActivitySwitch.Index.lastResumedActivityType);
        int i2 = beforeActivitySwitch.getInt(BeforeActivitySwitch.Index.nextResumedActivityType);
        if (!z) {
            Trace.traceBegin(64, "MPO_Suppress2");
            if (this.mFrcPolicy == null || this.mSuppressPolicy == null) {
                Log.e(TAG, "[onBeforeActivitySwitch] getInstance = null");
            } else if (i != 0 && i2 == 0) {
                if (!this.frcFlag) {
                    this.mFrcPolicy.startFrc("MPO", 2, null);
                    this.frcFlag = true;
                }
                if (this.foregroundApp == null) {
                    this.mSuppressPolicy.startSuppression("MPO", 5, MPO_SUPPRESS_ACTION, "MPO", null);
                    this.foregroundApp = string2;
                    if (this.suppressTimer == null) {
                        this.suppressTimer = new Timer();
                        this.suppressTimerTask = new TimerTask() {
                            public void run() {
                                if (AWSManager.this.mSuppressPolicy == null) {
                                    SuppressionPolicy.getInstance();
                                }
                                if (AWSManager.this.mSuppressPolicy == null) {
                                    Log.e(AWSManager.TAG, "[onBeforeActivitySwitch] mSuppressPolicy = null");
                                } else {
                                    AWSManager.this.mSuppressPolicy.stopSuppression("MPO");
                                }
                            }
                        };
                        this.suppressTimer.schedule(this.suppressTimerTask, 5000);
                    }
                }
            } else if (i == 0 && i2 != 0) {
                if (this.frcFlag) {
                    this.mFrcPolicy.stopFrc("MPO");
                    this.frcFlag = false;
                    if (this.foregroundApp != null) {
                        this.mSuppressPolicy.stopSuppression("MPO");
                        this.foregroundApp = null;
                        if (this.suppressTimer != null) {
                            this.suppressTimer.cancel();
                            this.suppressTimer = null;
                        }
                    }
                }
            } else if (this.frcFlag && this.foregroundApp != null) {
                this.mSuppressPolicy.stopSuppression("MPO");
                this.foregroundApp = null;
                if (this.suppressTimer != null) {
                    this.suppressTimer.cancel();
                    this.suppressTimer = null;
                }
            }
            Trace.traceEnd(64);
            Trace.traceBegin(64, "AWS_switch");
            final AWSLaunchRecord aWSLaunchRecord = new AWSLaunchRecord(string, string2, i, i2, beforeActivitySwitch.getInt(BeforeActivitySwitch.Index.waitProcessPid), (ArrayList) beforeActivitySwitch.get(BeforeActivitySwitch.Index.runningProcRecords));
            String prevPkgName = aWSLaunchRecord.getPrevPkgName();
            String nextPkgName = aWSLaunchRecord.getNextPkgName();
            if (!(prevPkgName == null || nextPkgName == null || nextPkgName.equalsIgnoreCase(prevPkgName))) {
                boolean contains = nextPkgName.contains("com.android.packageinstaller");
                if (aWSLaunchRecord.isLaunchingToRecentApp() || contains) {
                    Trace.traceEnd(64);
                    return 0;
                }
                this.mLaunchingPkgName = nextPkgName;
                try {
                    new Thread(new Runnable() {
                        public void run() {
                            AWSManager.this.ensureWorkingSet(aWSLaunchRecord);
                        }
                    }).start();
                } catch (Throwable e) {
                    Log.e(TAG, "Exception thrown during ensureWorkingSet:", e);
                }
            }
            Trace.traceEnd(64);
            return 0;
        }
        Trace.traceBegin(64, "MPO_Suppress1");
        if (this.mFrcPolicy == null || this.mSuppressPolicy == null) {
            Log.e(TAG, "[onBeforeActivitySwitch] getInstance = null");
        } else if (!(i == 0 || i2 != 0 || this.frcFlag)) {
            this.mFrcPolicy.startFrc("MPO", 2, null);
            this.frcFlag = true;
        }
        Trace.traceEnd(64);
        return 0;
    }

    public int recordST(int i, int i2, String str) {
        if (IsSystemAndReady("recordST") && str != null && Arrays.asList(nativeRecordProcess).contains(str)) {
            this.mNativePids.put(str, Integer.valueOf(i));
            this.mNativeUids.put(str, Integer.valueOf(i2));
        }
        return 0;
    }

    public int storeRecord(IAWSStoreRecord iAWSStoreRecord) {
        if (!IsSystemAndReady("storeRecord")) {
            return 0;
        }
        if (iAWSStoreRecord != null) {
            switch ((int) (iAWSStoreRecord.getExtraVal() * -1)) {
                case 1:
                case 2:
                case 3:
                    recordLaunchMemory(iAWSStoreRecord);
                    return 0;
                default:
                    recordProcMemory(iAWSStoreRecord);
                    return 0;
            }
        }
        Log.e(TAG, "storeRecord, record is null");
        return -1;
    }

    private int recordProcMemory(IAWSStoreRecord iAWSStoreRecord) {
        updateProcessNames(iAWSStoreRecord.getRecord()).updateSampledMem(iAWSStoreRecord.getExtraVal());
        return 0;
    }

    private int recordLaunchMemory(IAWSStoreRecord iAWSStoreRecord) {
        String topPkgName = iAWSStoreRecord.getTopPkgName();
        if (topPkgName == null) {
            Log.e(TAG, "When recoring launch Mem, Top Pkg null");
            return -1;
        } else if (!topPkgName.equals(this.mLaunchingPkgName)) {
            return 0;
        } else {
            clearLaunchProcList(topPkgName);
            ArrayList arrayList = new ArrayList();
            Iterator it = new AWSStoreRecord(iAWSStoreRecord).getRecords().iterator();
            while (it.hasNext()) {
                IAWSProcessRecord iAWSProcessRecord = (IAWSProcessRecord) it.next();
                ArrayMap arrayMap = new ArrayMap(iAWSProcessRecord.getpkgList());
                if (arrayMap.size() != 0) {
                    for (int i = 0; i < arrayMap.size(); i++) {
                        ProcessRecordStore updateProcessNames = updateProcessNames(iAWSProcessRecord);
                        String str = (String) arrayMap.keyAt(i);
                        if (topPkgName.equals(str) || isDepenPkg(topPkgName, str)) {
                            long pss = getPss(iAWSProcessRecord.getPid());
                            updateProcessNames.updateSampledMem(pss);
                            updateLaunchProcList(topPkgName, updateProcessNames);
                            updateProcessNames.updateLaunchMem(topPkgName, pss);
                            ArrayList depNaitveProcs = getDepNaitveProcs(iAWSProcessRecord);
                            if (depNaitveProcs != null) {
                                Iterator it2 = depNaitveProcs.iterator();
                                while (it2.hasNext()) {
                                    str = (String) it2.next();
                                    if (!arrayList.contains(str)) {
                                        arrayList.add(str);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            it = arrayList.iterator();
            while (it.hasNext()) {
                String str2 = (String) it.next();
                Integer num = (Integer) this.mNativePids.get(str2);
                Integer num2 = (Integer) this.mNativeUids.get(str2);
                if (!(num == null || num2 == null)) {
                    long pss2 = getPss(num.intValue());
                    ProcessRecordStore processRecordStore = (ProcessRecordStore) this.mProcessNames.get(str2, num2.intValue());
                    if (processRecordStore == null) {
                        processRecordStore = new ProcessRecordStore(str2, num2.intValue(), num.intValue());
                        updateProcessNames(processRecordStore);
                    }
                    processRecordStore.updateSampledMem(pss2);
                    updateLaunchProcList(topPkgName, processRecordStore);
                    processRecordStore.updateLaunchMem(topPkgName, pss2);
                }
            }
            return 0;
        }
    }

    private boolean isDepenPkg(String str, String str2) {
        return false;
    }

    private ArrayList<String> getDepNaitveProcs(IAWSProcessRecord iAWSProcessRecord) {
        ArrayList<String> arrayList = new ArrayList();
        boolean contains = iAWSProcessRecord.getProcName().contains("com.android.camera");
        boolean contains2 = iAWSProcessRecord.getProcName().contains("com.mediatek.camera");
        if (!contains && !contains2) {
            return null;
        }
        for (Object add : nativeRecordProcess) {
            arrayList.add(add);
        }
        return arrayList;
    }

    protected ProcessRecordStore updateProcessNames(IAWSProcessRecord iAWSProcessRecord) {
        ProcessRecordStore processRecordStore;
        synchronized (this.mProcessNames) {
            processRecordStore = (ProcessRecordStore) this.mProcessNames.get(iAWSProcessRecord.getProcName(), iAWSProcessRecord.getUid());
            if (processRecordStore != null) {
                processRecordStore.update(iAWSProcessRecord);
            } else {
                processRecordStore = new ProcessRecordStore(iAWSProcessRecord);
                this.mProcessNames.put(iAWSProcessRecord.getProcName(), iAWSProcessRecord.getUid(), processRecordStore);
            }
        }
        return processRecordStore;
    }

    protected void updateLaunchProcList(String str, ProcessRecordStore processRecordStore) {
        synchronized (this.mPackagesProcessMap) {
            PkgPriorityNode pkgPriorityNode = (PkgPriorityNode) this.mPackagesProcessMap.get(str);
            if (pkgPriorityNode != null) {
                updatePkgPriorityNode(pkgPriorityNode);
            } else {
                pkgPriorityNode = new PkgPriorityNode(str, processRecordStore);
                this.mPackagesProcessMap.put(str, pkgPriorityNode);
                addPkgPriorityNodeToHead(pkgPriorityNode);
            }
            ArrayList arrayList = pkgPriorityNode.procList;
            if (arrayList == null) {
                Log.e(TAG, "Adding PRS to non existing prsList at pkg" + str);
            } else if (!arrayList.contains(processRecordStore)) {
                arrayList.add(processRecordStore);
            }
        }
    }

    private int ensureWorkingSet(AWSLaunchRecord aWSLaunchRecord) {
        int i = 1;
        aWSLaunchRecord.getNextPkgName();
        long diffFromHistory = diffFromHistory(aWSLaunchRecord);
        MemInfoReader memInfoReader = new MemInfoReader();
        memInfoReader.readMemInfo();
        long[] rawInfo = memInfoReader.getRawInfo();
        long j = diffFromHistory - ((rawInfo[1] + rawInfo[3]) - rawInfo[9]);
        if ((j <= 0 ? 1 : 0) != 0) {
            return 0;
        }
        diffFromHistory = rawInfo[0] / 2;
        if (j > diffFromHistory) {
            i = 0;
        }
        if (i != 0) {
            diffFromHistory = j;
        }
        int reserveMemory = reserveMemory(aWSLaunchRecord, diffFromHistory);
        if (reserveMemory == 0) {
        }
        return reserveMemory;
    }

    private long diffFromHistory(AWSLaunchRecord aWSLaunchRecord) {
        PkgPriorityNode pkgPriorityNode;
        String nextPkgName = aWSLaunchRecord.getNextPkgName();
        ArrayList runningProcessesRecords = aWSLaunchRecord.getRunningProcessesRecords();
        synchronized (this.mPackagesProcessMap) {
            pkgPriorityNode = (PkgPriorityNode) this.mPackagesProcessMap.get(nextPkgName);
        }
        if (pkgPriorityNode == null) {
            return 30720;
        }
        long j;
        synchronized (this.mPackagesProcessMap) {
            Iterator it = pkgPriorityNode.procList.iterator();
            j = 0;
            while (it.hasNext()) {
                ProcessRecordStore processRecordStore = (ProcessRecordStore) it.next();
                long j2 = 0;
                long launchMem = processRecordStore.getLaunchMem(nextPkgName);
                Iterator it2 = runningProcessesRecords.iterator();
                while (it2.hasNext()) {
                    IAWSProcessRecord iAWSProcessRecord = (IAWSProcessRecord) it2.next();
                    if (processRecordStore.getProcName().equals(iAWSProcessRecord.getProcName())) {
                        processRecordStore.getSampledMem();
                        j2 = getPss(iAWSProcessRecord.getPid());
                        processRecordStore.updateSampledMem(j2);
                    }
                }
                if (Arrays.asList(nativeRecordProcess).contains(processRecordStore.getProcName())) {
                    j2 = processRecordStore.getSampledMem();
                    Integer num = (Integer) this.mNativePids.get(processRecordStore.getProcName());
                    if (num != null) {
                        j2 = getPss(num.intValue());
                        processRecordStore.updateSampledMem(j2);
                    }
                }
                j = (launchMem - j2) + j;
            }
        }
        return j;
    }

    private int reserveMemory(AWSLaunchRecord aWSLaunchRecord, long j) {
        ProcessRecordStore processRecordStore;
        ArrayList killingCandidates = getKillingCandidates(aWSLaunchRecord);
        ArrayList arrayList = new ArrayList();
        SparseArray sparseArray = new SparseArray();
        Iterator it = killingCandidates.iterator();
        int i = -1;
        ArrayList arrayList2 = null;
        while (it.hasNext()) {
            processRecordStore = (ProcessRecordStore) it.next();
            int adj = processRecordStore.getAdj();
            if (adj != i) {
                arrayList2 = new ArrayList();
                if (sparseArray.get(adj) != null) {
                    Log.e(TAG, "Adj " + adj + " added before");
                    return 0;
                }
                sparseArray.put(adj, arrayList2);
                i = adj;
            }
            if (arrayList2 == null) {
                Log.e(TAG, "Adj" + i + "add fail");
                return 0;
            }
            arrayList2.add(processRecordStore);
        }
        long j2 = 0;
        int i2 = 0;
        int size = sparseArray.size() - 1;
        while (size >= 0) {
            long sampledMem;
            int i3;
            ArrayList arrayList3 = (ArrayList) sparseArray.get(sparseArray.keyAt(size));
            if (arrayList3.size() != 1) {
                Collections.sort(arrayList3, new Comparator<ProcessRecordStore>() {
                    public int compare(ProcessRecordStore processRecordStore, ProcessRecordStore processRecordStore2) {
                        long j;
                        long sampledMem;
                        long j2;
                        ProcessRecordStore processRecordStore3 = (ProcessRecordStore) AWSManager.this.mProcessNames.get(processRecordStore.getProcName(), processRecordStore.getUid());
                        ProcessRecordStore processRecordStore4 = (ProcessRecordStore) AWSManager.this.mProcessNames.get(processRecordStore2.getProcName(), processRecordStore2.getUid());
                        if (processRecordStore3 == null) {
                            j = 0;
                        } else {
                            sampledMem = processRecordStore3.getSampledMem();
                            if (sampledMem == 0) {
                                sampledMem = AWSManager.this.getPss(processRecordStore3.getPid());
                                processRecordStore3.updateSampledMem(sampledMem);
                                j = sampledMem;
                            } else {
                                j = sampledMem;
                            }
                        }
                        if (processRecordStore4 == null) {
                            j2 = 0;
                        } else {
                            sampledMem = processRecordStore4.getSampledMem();
                            if (sampledMem == 0) {
                                sampledMem = AWSManager.this.getPss(processRecordStore4.getPid());
                                processRecordStore4.updateSampledMem(sampledMem);
                                j2 = sampledMem;
                            } else {
                                j2 = sampledMem;
                            }
                        }
                        if ((j2 - j <= 0 ? 1 : 0) == 0) {
                            return 1;
                        }
                        return -1;
                    }
                });
            } else {
                ProcessRecordStore processRecordStore2 = (ProcessRecordStore) arrayList3.get(0);
                if (processRecordStore2 == null) {
                    Log.e(TAG, "ArrayList size= 1 but element does not exsit");
                } else {
                    sampledMem = processRecordStore2.getSampledMem();
                    if (sampledMem == 0) {
                        sampledMem = getPss(processRecordStore2.getPid());
                    }
                    processRecordStore2.updateSampledMem(sampledMem);
                }
            }
            Iterator it2 = arrayList3.iterator();
            sampledMem = j2;
            while (it2.hasNext()) {
                processRecordStore = (ProcessRecordStore) it2.next();
                if ((sampledMem <= j ? 1 : null) == null) {
                    i3 = 1;
                    break;
                }
                arrayList.add(processRecordStore);
                sampledMem = processRecordStore.getSampledMem() + sampledMem;
            }
            i3 = i2;
            if (i3 == 1) {
                j2 = sampledMem;
                break;
            }
            size--;
            i2 = i3;
            j2 = sampledMem;
        }
        Iterator it3 = arrayList.iterator();
        while (it3.hasNext()) {
            processRecordStore = (ProcessRecordStore) it3.next();
            if (matchAdj(processRecordStore.getAdj(), processRecordStore.getPid())) {
                kill(processRecordStore.getPid(), processRecordStore.getProcName(), processRecordStore.getAdj());
            }
        }
        return (((j - j2) > 0 ? 1 : ((j - j2) == 0 ? 0 : -1)) <= 0 ? 1 : null) == null ? -1 : 0;
    }

    private boolean matchAdj(int i, int i2) {
        if (this.mAm == null) {
            if (this.mContext != null) {
                this.mAm = (ActivityManager) this.mContext.getSystemService("activity");
                if (this.mAm == null) {
                    Log.e(TAG, "AM is null, nothing to be kill");
                    return false;
                }
            }
            Log.e(TAG, "Context is null, nothing to be kill");
            return false;
        }
        ArrayMap processesWithAdj = this.mAm.getProcessesWithAdj();
        if (processesWithAdj != null) {
            ArrayList arrayList = (ArrayList) processesWithAdj.get(Integer.valueOf(i));
            if (arrayList != null) {
                Iterator it = arrayList.iterator();
                while (it.hasNext()) {
                    if (i2 == ((Integer) it.next()).intValue()) {
                        return true;
                    }
                }
                return false;
            }
            Log.e(TAG, "pidList is null, nothing to be kill");
            return false;
        }
        Log.e(TAG, "processMap is null, nothing to be kill");
        return false;
    }

    private ArrayList<ProcessRecordStore> getKillingCandidates(AWSLaunchRecord aWSLaunchRecord) {
        String nextPkgName = aWSLaunchRecord.getNextPkgName();
        ArrayList runningProcessesRecords = aWSLaunchRecord.getRunningProcessesRecords();
        Object arrayList = new ArrayList();
        Iterator it = runningProcessesRecords.iterator();
        while (it.hasNext()) {
            IAWSProcessRecord iAWSProcessRecord = (IAWSProcessRecord) it.next();
            if (iAWSProcessRecord.getAdj() > PERCEPTIBLE_APP_ADJ) {
                ArrayMap arrayMap = new ArrayMap(iAWSProcessRecord.getpkgList());
                if (arrayMap.size() != 0) {
                    for (int i = 0; i < arrayMap.size(); i++) {
                        if (!((String) arrayMap.keyAt(i)).equals(nextPkgName)) {
                        }
                    }
                    if (!(collectDepedencyList(aWSLaunchRecord).contains(Integer.valueOf(iAWSProcessRecord.getPid())) || isWhitelistToKeep(iAWSProcessRecord.getProcName()) || Arrays.asList(CustomProtectProcess.PROTECT_PROCESS_LIST).contains(iAWSProcessRecord.getProcName()))) {
                        arrayList.add(updateProcessNames(iAWSProcessRecord));
                    }
                }
            }
        }
        Collections.sort(arrayList, new Comparator<ProcessRecordStore>() {
            public int compare(ProcessRecordStore processRecordStore, ProcessRecordStore processRecordStore2) {
                if (processRecordStore.getAdj() < processRecordStore2.getAdj()) {
                    return 1;
                }
                if (processRecordStore.getAdj() <= processRecordStore2.getAdj()) {
                    return 0;
                }
                return -1;
            }
        });
        return arrayList;
    }

    private ArrayList<Integer> collectDepedencyList(AWSLaunchRecord aWSLaunchRecord) {
        ArrayList<Integer> arrayList = new ArrayList();
        int waitProcessPID = aWSLaunchRecord.getWaitProcessPID();
        if (waitProcessPID != -1) {
            arrayList.add(Integer.valueOf(waitProcessPID));
        }
        return arrayList;
    }

    private boolean isWhitelistToKeep(String str) {
        return str.matches("android.process.media") || str.matches("android.process.acore");
    }

    private void kill(int i, String str, int i2) {
        String str2 = TAG;
        Object[] objArr = new Object[3];
        objArr[0] = Integer.valueOf(i);
        objArr[1] = str;
        objArr[2] = Integer.valueOf(i2);
        Log.v(str2, String.format(" Killing process:pid: %d,(%s),adj=%d", objArr));
        try {
            Process.killProcessQuiet(i);
        } catch (Throwable e) {
            Log.e(TAG, "Exception thrown during kill:", e);
        }
    }

    private void clearProcList(String str) {
        synchronized (this.mProcessNames) {
            int size = this.mProcessNames.getMap().size();
            for (int i = 0; i < size; i++) {
                SparseArray sparseArray = (SparseArray) this.mProcessNames.getMap().valueAt(i);
                int size2 = sparseArray.size();
                for (int i2 = 0; i2 < size2; i2++) {
                    ProcessRecordStore processRecordStore = (ProcessRecordStore) sparseArray.valueAt(i2);
                    if (processRecordStore.getPkgName().equals(str)) {
                        this.mProcessNames.remove(processRecordStore.getPkgName(), processRecordStore.getUid());
                    }
                }
            }
        }
    }

    private int clearLaunchProcList(String str) {
        synchronized (this.mPackagesProcessMap) {
            PkgPriorityNode pkgPriorityNode = (PkgPriorityNode) this.mPackagesProcessMap.get(str);
            if (pkgPriorityNode != null) {
                removePkgPriorityNode(pkgPriorityNode);
                this.mPackagesProcessMap.remove(str);
            }
        }
        return 0;
    }

    private PkgPriorityNode removePkgPriorityNode(PkgPriorityNode pkgPriorityNode) {
        if (this.mPHead.next == pkgPriorityNode && this.mPTail.prev == pkgPriorityNode) {
            this.mPHead.next = null;
            this.mPTail.prev = null;
            this.mPDirty = null;
        } else {
            pkgPriorityNode.next.prev = pkgPriorityNode.prev;
            pkgPriorityNode.prev.next = pkgPriorityNode.next;
            if (this.mPDirty == pkgPriorityNode) {
                this.mPDirty = pkgPriorityNode.prev;
            }
        }
        pkgPriorityNode.next = null;
        pkgPriorityNode.prev = null;
        this.mNumPkgPriorityNode--;
        return pkgPriorityNode;
    }

    private int addPkgPriorityNodeToHead(PkgPriorityNode pkgPriorityNode) {
        if (this.mPHead.next != null) {
            this.mPHead.next.prev = pkgPriorityNode;
            pkgPriorityNode.next = this.mPHead.next;
            this.mPHead.next = pkgPriorityNode;
            pkgPriorityNode.prev = this.mPHead;
        } else {
            this.mPHead.next = pkgPriorityNode;
            this.mPTail.prev = pkgPriorityNode;
            this.mPDirty = pkgPriorityNode;
            pkgPriorityNode.next = this.mPTail;
            pkgPriorityNode.prev = this.mPHead;
        }
        this.mNumPkgPriorityNode++;
        if (this.mNumPkgPriorityNode > 100) {
            clearLaunchProcList(this.mPTail.prev.pkgName);
        }
        return 0;
    }

    private int updatePkgPriorityNode(PkgPriorityNode pkgPriorityNode) {
        if (this.mPHead.next != pkgPriorityNode) {
            addPkgPriorityNodeToHead(removePkgPriorityNode(pkgPriorityNode));
        }
        return 0;
    }

    private void dumpAllPriority() {
        Log.v(TAG, "[PkgPriority]dumpAllPriority------");
        PkgPriorityNode pkgPriorityNode = this.mPHead.next;
        int i = 0;
        if (pkgPriorityNode != null) {
            do {
                Log.v(TAG, "[PkgPriority]: index" + i + ":" + pkgPriorityNode.pkgName);
                i++;
                pkgPriorityNode = pkgPriorityNode.next;
            } while (pkgPriorityNode.next != this.mPTail.next);
            Log.v(TAG, "[PkgPriority]: Iterate to Tail-----");
            return;
        }
        Log.v(TAG, "[PkgPriority]: currently empty now");
    }

    private long getPss(int i) {
        long[] jArr = new long[2];
        return jArr[1] + Debug.getPss(i, jArr, null);
    }

    private boolean IsSystemAndReady(String str) {
        if (!mIsReady) {
            Log.v(TAG, "not ready");
            return false;
        }
        int callingUid = Binder.getCallingUid();
        if (callingUid == 0 || callingUid == 1000) {
            return true;
        }
        throw new SecurityException(str + " called from non-system process");
    }
}
