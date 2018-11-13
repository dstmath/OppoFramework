package com.mediatek.runningbooster;

import android.app.ActivityManager;
import android.app.ActivityManager.RecentTaskInfo;
import android.app.AppOpsManager;
import android.app.AppOpsManager.OpEntry;
import android.app.AppOpsManager.PackageOps;
import android.app.INotificationManager;
import android.app.Notification;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageManager;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Binder;
import android.os.Build.VERSION;
import android.os.FileUtils;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.Trace;
import android.os.UserHandle;
import android.service.notification.StatusBarNotification;
import android.util.ArrayMap;
import android.util.Slog;
import android.util.SparseArray;
import com.android.server.LocalServices;
import com.android.server.LocationManagerService;
import com.android.server.am.OppoProcessManager;
import com.android.server.job.JobSchedulerShellCommand;
import com.android.server.notification.NotificationManagerService;
import com.mediatek.am.AMEventHookData.ActivityThreadResumedDone;
import com.mediatek.am.AMEventHookData.AfterActivityResumed;
import com.mediatek.am.AMEventHookData.BeforeActivitySwitch;
import com.mediatek.am.AMEventHookData.BeforeActivitySwitch.Index;
import com.mediatek.am.AMEventHookData.SystemReady;
import com.mediatek.am.AMEventHookData.SystemUserUnlock;
import com.mediatek.am.AMEventHookData.WakefulnessChanged;
import com.mediatek.anrmanager.ANRManager;
import com.mediatek.apm.frc.FocusRelationshipChainPolicy;
import com.mediatek.apm.suppression.SuppressionPolicy;
import com.mediatek.runningbooster.IRunningBoosterManager.Stub;
import com.mediatek.runningbooster.RbConfiguration.AdjValue;
import com.mediatek.runningbooster.RbConfiguration.PolicyList;
import com.mediatek.runningbooster.RbConfiguration.SuppressionPoint;
import com.mediatek.suppression.service.SuppressionInternal;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

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
public class RunningBoosterService extends Stub {
    public static final String ACTION_START_RUNNING_BOOSTER = "android.intent.action.ACTION_START_RUNNING_BOOSTER";
    private static IPackageManager B = null;
    private static boolean H = false;
    private static RunningBoosterService I = null;
    private static final boolean IS_USER_BUILD = false;
    public static final String RUNNING_BOOSTER_APP = "com.android.runningbooster";
    private static AmsEventHandler W;
    private static boolean at;
    private final boolean DEBUG;
    private final Object J;
    private final Object K;
    private final Object L;
    private String M;
    private String N;
    private BroadcastReceiver O;
    private boolean P;
    private boolean Q;
    private boolean R;
    private boolean S;
    private boolean T;
    private final String TAG;
    private boolean U;
    private String V;
    private FocusRelationshipChainPolicy X;
    private SuppressionPolicy Y;
    private SuppressionInternal Z;
    private HashMap<Integer, String> aa;
    private HashMap<String, ArrayList<SuppressionPoint>> ab;
    private HashMap<String, ArrayList<SuppressionPoint>> ac;
    private HashMap<String, ArrayList<String>> ad;
    private SparseArray<PolicyList> ae;
    private HashMap<String, ArrayList<String>> af;
    private HashMap<String, ArrayList<String>> ag;
    private HashMap<String, ArrayList<String>> ah;
    private HashMap<String, ArrayList<String>> ai;
    private ArrayList<String> aj;
    private ArrayList<String> ak;
    private HashMap<String, Boolean> al;
    private HashMap<String, Boolean> am;
    private ArrayList<String> an;
    private ArrayMap<Integer, ArrayList<Integer>> ao;
    private List<RecentTaskInfo> ap;
    private HashMap<String, ArrayList<String>> aq;
    private RbConfiguration ar;
    private SuppressionPoint as;
    private ArrayList<String> au;
    private ArrayList<String> av;
    private Context mContext;
    private int mCurrentUserId;

    public class AmsEventHandler extends Handler {
        public AmsEventHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message message) {
            switch (message.what) {
                case 1001:
                    RunningBoosterService.this.a((SuppressionPoint) message.obj);
                    return;
                case 1002:
                    RunningBoosterService.this.b((SuppressionPoint) message.obj);
                    return;
                case 1003:
                    RunningBoosterService.this.c((SuppressionPoint) message.obj);
                    return;
                case 1004:
                case 1008:
                    RunningBoosterService.this.l();
                    return;
                case 1005:
                    RunningBoosterService.this.k();
                    return;
                case ANRManager.RENAME_TRACE_FILES_MSG /*1006*/:
                    RunningBoosterService.this.h((String) message.obj);
                    return;
                case 1007:
                    RunningBoosterService.this.g();
                    return;
                default:
                    return;
            }
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.mediatek.runningbooster.RunningBoosterService.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.mediatek.runningbooster.RunningBoosterService.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.runningbooster.RunningBoosterService.<clinit>():void");
    }

    public RunningBoosterService(Context context) {
        boolean z = true;
        this.TAG = "RunningBoosterService";
        if (1 != SystemProperties.getInt("persist.runningbooster.debug", 0)) {
            z = false;
        }
        this.DEBUG = z;
        this.J = new Object();
        this.K = new Object();
        this.L = new Object();
        this.M = "/data/runningbooster/package_list.txt";
        this.N = "/system/etc/runningbooster/platform_list.txt";
        this.O = null;
        this.P = false;
        this.Q = false;
        this.R = false;
        this.S = false;
        this.T = false;
        this.U = false;
        this.X = FocusRelationshipChainPolicy.getInstance();
        this.Y = SuppressionPolicy.getInstance();
        this.Z = (SuppressionInternal) LocalServices.getService(SuppressionInternal.class);
        this.aa = new HashMap();
        this.ab = new HashMap();
        this.ac = new HashMap();
        this.ad = new HashMap();
        this.ae = new SparseArray();
        this.af = new HashMap();
        this.ag = new HashMap();
        this.ah = new HashMap();
        this.ai = new HashMap();
        this.aj = new ArrayList();
        this.ak = new ArrayList();
        this.al = new HashMap();
        this.am = new HashMap();
        this.an = new ArrayList();
        this.ao = new ArrayMap();
        this.ap = new ArrayList();
        this.aq = new HashMap();
        this.ar = new RbConfiguration();
        this.as = new SuppressionPoint("initial", -1);
        this.au = new ArrayList();
        this.av = new ArrayList();
        this.mContext = context;
        f();
        h();
        this.mCurrentUserId = ActivityManager.getCurrentUser();
        HandlerThread handlerThread = new HandlerThread("AmsEventThread");
        handlerThread.start();
        W = new AmsEventHandler(handlerThread.getLooper());
        I = this;
        this.mCurrentUserId = ActivityManager.getCurrentUser();
        Slog.d("RunningBoosterService", "mCurrentUserId = " + this.mCurrentUserId);
        j(this.M);
        if (!IS_USER_BUILD || this.DEBUG) {
            j("/data/runningbooster/allow_list_bytype.txt");
            j("/data/runningbooster/kill_list.txt");
            j("/data/runningbooster/allow_list.txt");
            j("/data/runningbooster/adj_allow_list.txt");
            j("/data/runningbooster/adj_kill_list.txt");
            j("/data/runningbooster/filter_list.txt");
            j("/data/runningbooster/recent_list.txt");
            j("/data/runningbooster/notification_list.txt");
            j("/data/runningbooster/appwidget_list.txt");
            j("/data/runningbooster/location_list.txt");
        }
    }

    public void systemRunning() {
        Slog.d("RunningBoosterService", "[systemRunning]");
        e();
    }

    public void applyUserConfig(String str, RbConfiguration rbConfiguration) {
        RbConfiguration a;
        boolean z;
        if (!H) {
            a.a(this.mContext).f("applyUserConfig");
        }
        Slog.d("RunningBoosterService", "[applyUserConfig] Enter");
        a(rbConfiguration);
        int callingUid = Binder.getCallingUid();
        RbConfiguration rbConfiguration2 = new RbConfiguration();
        PolicyList policyList = (PolicyList) this.ae.get(callingUid);
        if (this.aa.get(Integer.valueOf(callingUid)) == null) {
            a(callingUid, str);
            this.aa.put(Integer.valueOf(callingUid), str);
        }
        RbConfiguration rbConfiguration3 = new RbConfiguration();
        if (policyList != null) {
            a = a(rbConfiguration, policyList.mConfig, callingUid);
            Slog.d("RunningBoosterService", "[applyUserConfig] new UserPolicy");
        } else {
            a = a(rbConfiguration, rbConfiguration3, callingUid);
            Slog.d("RunningBoosterService", "[applyUserConfig] null == oldUserPolicy");
        }
        policyList = new PolicyList(callingUid, this.mCurrentUserId, str, false, a);
        a(a);
        this.ae.put(callingUid, policyList);
        if (1 == a.suppressPoint.size() && ((SuppressionPoint) a.suppressPoint.get(0)).equal(RbConfiguration.DEAULT_STARTPOINT)) {
            z = true;
        } else {
            z = false;
        }
        if (!z) {
            Iterator it = a.suppressPoint.iterator();
            while (it.hasNext()) {
                ((SuppressionPoint) it.next()).mConfig = a;
            }
            if (true != a.enableRunningBooster) {
                Slog.d("RunningBoosterService", "[applyUserConfig] enableRunningBooster is disable, pkg = " + str);
                if (this.ab.size() > 0) {
                    i(str);
                    this.ab.remove(str);
                    this.ac.remove(str);
                }
            } else {
                Slog.d("RunningBoosterService", "[applyUserConfig] enableRunningBooster is enable, pkg = " + str);
                this.ab.put(str, a.suppressPoint);
            }
            ArrayList arrayList = new ArrayList();
            Iterator it2 = a.suppressPoint.iterator();
            while (it2.hasNext()) {
                SuppressionPoint suppressionPoint = (SuppressionPoint) it2.next();
                Slog.d("RunningBoosterService", "[applyUserConfig] SupressPoint tag = " + suppressionPoint.mSuppressTag);
                SuppressionPoint suppressionPoint2 = new SuppressionPoint();
                if (suppressionPoint.mAppState != 0) {
                    suppressionPoint2.mAppState = 0;
                } else {
                    suppressionPoint2.mAppState = 1;
                }
                suppressionPoint2.mPackageName = suppressionPoint.mPackageName;
                suppressionPoint2.mSuppressTag = suppressionPoint.mSuppressTag;
                arrayList.add(suppressionPoint2);
                Slog.d("RunningBoosterService", "[applyUserConfig] unSupressPoint tag = " + suppressionPoint2.mSuppressTag);
            }
            Slog.d("RunningBoosterService", "[applyUserConfig] mCurrentUnSuppressPointList pkg = " + str);
            this.ac.put(str, arrayList);
        }
    }

    public String getAPIVersion() {
        if (!H) {
            a.a(this.mContext).f("getAPIVersion");
        }
        Slog.d("RunningBoosterService", "getAPIVersion");
        return "1.0";
    }

    /* JADX WARNING: Removed duplicated region for block: B:52:0x0100  */
    /* JADX WARNING: Removed duplicated region for block: B:33:0x00a7 A:{SYNTHETIC, Splitter: B:33:0x00a7} */
    /* JADX WARNING: Removed duplicated region for block: B:15:0x0030 A:{SYNTHETIC, Splitter: B:15:0x0030} */
    /* JADX WARNING: Removed duplicated region for block: B:52:0x0100  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public List<String> getPlatformWhiteList() {
        Object obj;
        Object obj2;
        BufferedReader bufferedReader;
        String readLine;
        Throwable e;
        Throwable th;
        if (!H) {
            a.a(this.mContext).f("getPlatformWhiteList");
        }
        int callingUid = Binder.getCallingUid();
        try {
            if (B == null) {
                B = a();
            }
        } catch (RuntimeException e2) {
            Slog.e("RunningBoosterService", "[getPlatformWhiteList]can't get PMS " + e2);
        }
        try {
            String[] packagesForUid = B.getPackagesForUid(callingUid);
            if (packagesForUid == null) {
                obj = null;
            } else {
                obj = packagesForUid[0];
                try {
                    Slog.d("RunningBoosterService", "[getPlatformWhiteList] callerPkgName = " + obj);
                } catch (RemoteException e3) {
                    RemoteException remoteException = e3;
                    obj2 = obj;
                    obj = remoteException;
                }
            }
        } catch (RemoteException e4) {
            obj = e4;
            obj2 = null;
        }
        try {
            bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(this.N), "utf-8"));
            while (true) {
                try {
                    readLine = bufferedReader.readLine();
                    if (readLine != null) {
                        try {
                            bufferedReader.close();
                            break;
                        } catch (Throwable e5) {
                            Slog.e("RunningBoosterService", "file closed fail ", e5);
                        }
                    } else {
                        this.aj.add(readLine);
                        Slog.d("RunningBoosterService", "[getPlatformWhiteList] package name = " + readLine);
                    }
                } catch (Exception e6) {
                    e5 = e6;
                }
            }
        } catch (Throwable e7) {
            Throwable th2 = e7;
            bufferedReader = null;
            e5 = th2;
            try {
                Slog.e("RunningBoosterService", "getPlatformWhiteList fail ", e5);
                try {
                    bufferedReader.close();
                } catch (Throwable e52) {
                    Slog.e("RunningBoosterService", "file closed fail ", e52);
                }
                if (obj != null) {
                }
                return this.aj;
            } catch (Throwable th3) {
                th = th3;
                try {
                    bufferedReader.close();
                } catch (Throwable e522) {
                    Slog.e("RunningBoosterService", "file closed fail ", e522);
                }
                throw th;
            }
        } catch (Throwable th4) {
            th = th4;
            bufferedReader = null;
            bufferedReader.close();
            throw th;
        }
        if (obj != null) {
            this.aj.add(obj);
        }
        return this.aj;
        Slog.e("RunningBoosterService", "[getPlatformWhiteList] get callerPkgName fail " + obj);
        obj = obj2;
        bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(this.N), "utf-8"));
        while (true) {
            readLine = bufferedReader.readLine();
            if (readLine != null) {
            }
        }
        if (obj != null) {
        }
        return this.aj;
    }

    private void e() {
        Intent intent = new Intent(ACTION_START_RUNNING_BOOSTER);
        if (LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON.equals(SystemProperties.get("persist.runningbooster.app.on"))) {
            intent.putExtra("APP_DEFAULT_ON", true);
        } else {
            intent.putExtra("APP_DEFAULT_ON", false);
        }
        this.mContext.sendBroadcastAsUser(intent, UserHandle.CURRENT);
    }

    private void f() {
        Slog.d("RunningBoosterService", "[registerReceiver]");
        if (this.O == null) {
            this.O = new BroadcastReceiver() {
                public void onReceive(Context context, Intent intent) {
                    String action = intent.getAction();
                    Slog.d("RunningBoosterService", "received broadcast, action is: " + action);
                    int intExtra;
                    String schemeSpecificPart;
                    if ("android.intent.action.PACKAGE_ADDED" == action || "android.intent.action.PACKAGE_REPLACED" == action || "android.intent.action.PACKAGE_DATA_CLEARED" == action) {
                        intExtra = intent.getIntExtra("android.intent.extra.UID", -1);
                        schemeSpecificPart = intent.getData().getSchemeSpecificPart();
                        Slog.d("RunningBoosterService", "[PACKAGE_ADDED|PACKAGE_REPLACED|DATA_CLEARED] uid = " + intExtra);
                        if (RunningBoosterService.this.aa.containsValue(schemeSpecificPart)) {
                            Slog.d("RunningBoosterService", "[ADDED|REPLACED|DATA_CLEARED] mSupportAppPackage = " + schemeSpecificPart);
                            RunningBoosterService.this.e();
                        }
                    } else if ("android.intent.action.PACKAGE_CHANGED" == action) {
                        action = intent.getData().getSchemeSpecificPart();
                        Slog.d("RunningBoosterService", "ACTION_PACKAGE_CHANGED packageName = " + action);
                        if (RunningBoosterService.this.aa.containsValue(action)) {
                            intExtra = RunningBoosterService.this.mContext.getPackageManager().getApplicationEnabledSetting(action);
                            Slog.d("RunningBoosterService", "ACTION_PACKAGE_CHANGED state = " + intExtra);
                            if (intExtra == 1 || intExtra == 0) {
                                Slog.d("RunningBoosterService", "[ACTION_PACKAGE_CHANGED] APP is enable");
                                RunningBoosterService.this.e();
                                return;
                            }
                            Slog.d("RunningBoosterService", "[ACTION_PACKAGE_CHANGED] APP is disable");
                            RunningBoosterService.W.sendMessage(RunningBoosterService.W.obtainMessage(1008));
                        }
                    } else if ("android.intent.action.PACKAGE_RESTARTED" == action) {
                        int intExtra2 = intent.getIntExtra("android.intent.extra.UID", -1);
                        String schemeSpecificPart2 = intent.getData().getSchemeSpecificPart();
                        if (!RunningBoosterService.IS_USER_BUILD || RunningBoosterService.this.DEBUG) {
                            Slog.d("RunningBoosterService", "[ACTION_PACKAGE_RESTARTED] uid = " + intExtra2);
                        }
                        if (RunningBoosterService.this.aa.get(Integer.valueOf(intExtra2)) != null) {
                            Slog.d("RunningBoosterService", "[ACTION_PACKAGE_RESTARTED] mSupportAppPackage = " + ((String) RunningBoosterService.this.aa.get(Integer.valueOf(intExtra2))));
                            PolicyList policyList = (PolicyList) RunningBoosterService.this.ae.get(intExtra2);
                            if (policyList != null) {
                                policyList.mForceStopState = true;
                                RunningBoosterService.this.ae.put(intExtra2, policyList);
                                RunningBoosterService.this.a(intExtra2, schemeSpecificPart2, true);
                            }
                        }
                    } else if ("android.intent.action.USER_SWITCHED" == action) {
                        intExtra = RunningBoosterService.this.mCurrentUserId;
                        RunningBoosterService.this.mCurrentUserId = ActivityManager.getCurrentUser();
                        Slog.d("RunningBoosterService", "[ACTION_USER_SWITCHED] oldUserId = " + intExtra + " mCurrentUserId = " + RunningBoosterService.this.mCurrentUserId);
                        if (intExtra != RunningBoosterService.this.mCurrentUserId) {
                            RunningBoosterService.W.sendMessage(RunningBoosterService.W.obtainMessage(1004));
                        }
                        RunningBoosterService.this.e();
                    } else if ("android.intent.action.PACKAGE_REMOVED" == action) {
                        intExtra = intent.getIntExtra("android.intent.extra.UID", -1);
                        schemeSpecificPart = intent.getData().getSchemeSpecificPart();
                        Slog.d("RunningBoosterService", "[ACTION_PACKAGE_REMOVED] uid = " + intExtra + "pkg = " + schemeSpecificPart);
                        if (RunningBoosterService.this.aa.get(Integer.valueOf(intExtra)) != null) {
                            RunningBoosterService.this.a(intExtra, schemeSpecificPart, false);
                        }
                    } else if ("android.intent.action.ACTION_PREBOOT_IPO".equals(action)) {
                        Slog.d("RunningBoosterService", "IPO reboot");
                        RunningBoosterService.this.e();
                    } else if ("android.intent.action.BOOT_COMPLETED" == action) {
                        Slog.d("RunningBoosterService", "reboot complete");
                        RunningBoosterService.this.e();
                    }
                }
            };
        }
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.PACKAGE_ADDED");
        intentFilter.addAction("android.intent.action.PACKAGE_REPLACED");
        intentFilter.addAction("android.intent.action.PACKAGE_RESTARTED");
        intentFilter.addAction("android.intent.action.PACKAGE_REMOVED");
        intentFilter.addAction("android.intent.action.PACKAGE_DATA_CLEARED");
        intentFilter.addAction("android.intent.action.PACKAGE_CHANGED");
        intentFilter.addDataScheme("package");
        this.mContext.registerReceiverAsUser(this.O, UserHandle.ALL, intentFilter, null, null);
        intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.USER_SWITCHED");
        intentFilter.addAction("android.intent.action.ACTION_PREBOOT_IPO");
        intentFilter.addAction("android.intent.action.BOOT_COMPLETED");
        this.mContext.registerReceiverAsUser(this.O, UserHandle.ALL, intentFilter, null, null);
    }

    public void onBeforeActivitySwitch(BeforeActivitySwitch beforeActivitySwitch) {
        if (this.ab.size() != 0) {
            SuppressionPoint suppressionPoint;
            SuppressionPoint suppressionPoint2;
            if (this.DEBUG) {
                Slog.d("RunningBoosterService", "=> onBeforeActivitySwitch");
            }
            String string = beforeActivitySwitch.getString(Index.lastResumedActivityName);
            String string2 = beforeActivitySwitch.getString(Index.nextResumedActivityName);
            String string3 = beforeActivitySwitch.getString(Index.lastResumedPackageName);
            String string4 = beforeActivitySwitch.getString(Index.nextResumedPackageName);
            boolean z = beforeActivitySwitch.getBoolean(Index.isNeedToPauseActivityFirst);
            int i = beforeActivitySwitch.getInt(Index.lastResumedActivityType);
            int i2 = beforeActivitySwitch.getInt(Index.nextResumedActivityType);
            if (string3 == null) {
                suppressionPoint = RbConfiguration.DEAULT_STARTPOINT;
                Slog.d("RunningBoosterService", "[onBeforeActivitySwitch]lastResumed pkg is null");
                suppressionPoint2 = suppressionPoint;
            } else if (string3.equals(string4)) {
                Slog.d("RunningBoosterService", "[onBeforeActivitySwitch]lastResumed pkg is the same as nextResumed pkg");
                return;
            } else {
                suppressionPoint2 = a(i, string3, 0);
            }
            SuppressionPoint a = a(i2, string4, 1);
            if (suppressionPoint2.mPackageName.equals(a.mPackageName)) {
                Slog.d("RunningBoosterService", "[onBeforeActivitySwitch] Suppress package is the same, pkg = " + suppressionPoint2.mPackageName);
                return;
            }
            Iterator it;
            Iterator it2;
            if (!IS_USER_BUILD || this.DEBUG) {
                Slog.d("RunningBoosterService", "[onBeforeActivitySwitch] mScreenStateChange = " + this.R + " mScreenOffState = " + this.Q);
            }
            if (true == this.R && !this.Q) {
                this.R = false;
                h(string4);
                Slog.d("RunningBoosterService", "[onBeforeActivitySwitch] mPausePkgNameAfterScreenOff = " + this.V + " nextResumedPackageName = " + string4);
            }
            this.V = string4;
            if (this.DEBUG) {
                Slog.d("RunningBoosterService", "[onBeforeActivitySwitch] lastResumedActivityName = " + string);
                Slog.d("RunningBoosterService", "[onBeforeActivitySwitch] nextResumedActivityName = " + string2);
                Slog.d("RunningBoosterService", "[onBeforeActivitySwitch] lastResumedPackageName = " + string3);
                Slog.d("RunningBoosterService", "[onBeforeActivitySwitch] nextResumedPackageName = " + string4);
                Slog.d("RunningBoosterService", "[onBeforeActivitySwitch] isNeedToPauseActivityFirst = " + z);
            }
            if (this.aa.containsValue(string4)) {
                int i3;
                for (Entry entry : this.aa.entrySet()) {
                    string = (String) entry.getValue();
                    if (string.equals(string4)) {
                        i = ((Integer) entry.getKey()).intValue();
                        Slog.d("RunningBoosterService", "[onBeforeActivitySwitch] mSupportAppPackage = " + string);
                        i3 = i;
                        break;
                    }
                }
                i3 = 0;
                PolicyList policyList = (PolicyList) this.ae.get(i3);
                if (policyList != null && true == policyList.mForceStopState) {
                    policyList.mForceStopState = false;
                    this.ae.put(i3, policyList);
                    e();
                }
            }
            if (this.DEBUG) {
                Slog.d("RunningBoosterService", "[onBeforeActivitySwitch] updateOrSuppress Point1 = " + suppressionPoint2.mAppState + " pkgName = " + suppressionPoint2.mPackageName);
                Slog.d("RunningBoosterService", "[onBeforeActivitySwitch] updateOrSuppress Point2 = " + a.mAppState + " pkgName = " + a.mPackageName);
            }
            ArrayList arrayList = new ArrayList();
            arrayList = new ArrayList();
            for (Entry entry2 : this.ab.entrySet()) {
                it = ((ArrayList) entry2.getValue()).iterator();
                while (it.hasNext()) {
                    suppressionPoint = (SuppressionPoint) it.next();
                    if (suppressionPoint2.equal(suppressionPoint) || a.equal(suppressionPoint)) {
                        if (true == z || !(z || at)) {
                            ActivityManager activityManager = (ActivityManager) this.mContext.getSystemService("activity");
                            synchronized (this.K) {
                                if (this.ao != null) {
                                    this.ao.clear();
                                }
                                if (this.DEBUG) {
                                    Slog.d("RunningBoosterService", "[onBeforeActivitySwitch] => getProcessesWithAdj");
                                }
                                this.ao = activityManager.getProcessesWithAdj();
                                if (this.DEBUG) {
                                    Slog.d("RunningBoosterService", "[onBeforeActivitySwitch] <= getProcessesWithAdj");
                                }
                            }
                            synchronized (this.J) {
                                try {
                                    this.ap.clear();
                                    if (this.DEBUG) {
                                        Slog.d("RunningBoosterService", "[onBeforeActivitySwitch] => getRecentTasks");
                                    }
                                    this.ap = activityManager.getRecentTasksForUser(suppressionPoint.mConfig.keepRecentTaskNumner, 63, this.mCurrentUserId);
                                    if (this.DEBUG) {
                                        Slog.d("RunningBoosterService", "[onBeforeActivitySwitch] <= getRecentTasks");
                                    }
                                } catch (Throwable e) {
                                    Slog.e("RunningBoosterService", "getRecentTasks fail ", e);
                                    if (this.DEBUG) {
                                        Slog.d("RunningBoosterService", "[onBeforeActivitySwitch] <= getRecentTasks");
                                    }
                                } catch (Throwable th) {
                                    if (this.DEBUG) {
                                        Slog.d("RunningBoosterService", "[onBeforeActivitySwitch] <= getRecentTasks");
                                    }
                                }
                            }
                        }
                        if (this.DEBUG) {
                            Slog.d("RunningBoosterService", "[onBeforeActivitySwitch] updateOrSuppress point=" + suppressionPoint.mAppState + "pkgName = " + suppressionPoint.mPackageName);
                        }
                        if (!z) {
                            synchronized (this.L) {
                                this.aq.put(suppressionPoint.mSuppressTag, (ArrayList) beforeActivitySwitch.get(Index.nextTaskPackageList));
                            }
                        }
                        a(z, suppressionPoint);
                    }
                }
            }
            for (Entry entry22 : this.ac.entrySet()) {
                it2 = ((ArrayList) entry22.getValue()).iterator();
                while (it2.hasNext()) {
                    suppressionPoint = (SuppressionPoint) it2.next();
                    if (suppressionPoint2.equal(suppressionPoint) || a.equal(suppressionPoint)) {
                        if (this.DEBUG) {
                            Slog.d("RunningBoosterService", "[onBeforeActivitySwitch] stopSuppressByTag tag = " + suppressionPoint.mSuppressTag);
                        }
                        Slog.d("RunningBoosterService", "[onBeforeActivitySwitch] stopSuppressByTag");
                        W.sendMessage(W.obtainMessage(1003, suppressionPoint));
                    }
                }
            }
            at = z;
            if (this.DEBUG) {
                Slog.d("RunningBoosterService", "<= onBeforeActivitySwitch");
            }
            return;
        }
        Slog.d("RunningBoosterService", "[onBeforeActivitySwitch] mCurrentSuppressPointList is null");
    }

    public void onWakefulnessChanged(WakefulnessChanged wakefulnessChanged) {
        int i = wakefulnessChanged.getInt(WakefulnessChanged.Index.wakefulness);
        boolean z = this.Q;
        Slog.d("RunningBoosterService", "[onWakefulnessChanged] wakefulness = " + i);
        if (1 == i) {
            this.Q = false;
        }
        if (i == 0) {
            this.Q = true;
            W.sendMessage(W.obtainMessage(1005));
        }
        if (this.Q != z) {
            this.R = true;
        }
    }

    public void onAfterActivityResumed(AfterActivityResumed afterActivityResumed) {
        if (!IS_USER_BUILD || this.DEBUG) {
            Slog.d("RunningBoosterService", "[onAfterActivityResumed] mScreenStateChange = " + this.R + " mScreenOffState = " + this.Q);
        }
        if (this.ab.size() != 0) {
            if (true == this.R && !this.Q) {
                Slog.d("RunningBoosterService", "[onAfterActivityResumed] mPausePkgNameAfterScreenOff = " + this.V);
                String string = afterActivityResumed.getString(AfterActivityResumed.Index.packageName);
                Slog.d("RunningBoosterService", "[onAfterActivityResumed] packageName = " + string + "activityType = " + afterActivityResumed.getInt(AfterActivityResumed.Index.activityType));
                Message obtainMessage = W.obtainMessage(ANRManager.RENAME_TRACE_FILES_MSG, string);
                int i = SystemProperties.getInt("duraspeed.event.delaytime", 500);
                Slog.d("RunningBoosterService", "[onAfterActivityResumed] delaytime = " + i);
                W.removeMessages(ANRManager.RENAME_TRACE_FILES_MSG);
                W.sendMessageDelayed(obtainMessage, (long) i);
                this.R = false;
            }
            return;
        }
        Slog.d("RunningBoosterService", "[onAfterActivityResumed] mCurrentSuppressPointList is null");
    }

    public void onActivityThreadResumedDone(ActivityThreadResumedDone activityThreadResumedDone) {
        Slog.d("RunningBoosterService", "[onActivityThreadResumedDone]");
        Message obtainMessage = W.obtainMessage(1007);
        W.removeMessages(1007);
        W.sendMessageDelayed(obtainMessage, 500);
    }

    public void onSystemUserUnlock(SystemUserUnlock systemUserUnlock) {
        Slog.d("RunningBoosterService", "[onSystemUserUnlock]");
        e();
    }

    private void g() {
        Trace.traceBegin(64, "handleActivityResumeDone");
        if (this.am.size() > 0) {
            for (Entry entry : this.am.entrySet()) {
                if (((Boolean) entry.getValue()).booleanValue()) {
                    String str = (String) entry.getKey();
                    Slog.d("RunningBoosterService", "[onActivityThreadResumedDone] Second kill phase tag = " + str);
                    if (((ArrayList) this.ai.get(str)).size() > 0 && this.Z != null) {
                        this.Z.suppressPackages((List) this.ai.get(str), 1064374545, str);
                    }
                    this.am.put(str, Boolean.valueOf(false));
                }
            }
        }
        Trace.traceEnd(64);
    }

    private RbConfiguration a(RbConfiguration rbConfiguration, RbConfiguration rbConfiguration2, int i) {
        Iterator it;
        if (!IS_USER_BUILD || this.DEBUG) {
            Slog.d("RunningBoosterService", "[updateUserConfiguration] SDK version = " + VERSION.SDK_INT);
        }
        SystemProperties.set("mtk.duraspeed.on", String.valueOf(rbConfiguration.enableRunningBooster));
        if (rbConfiguration.adj >= AdjValue.PerceptibleAppAdj.getAdjValue() && rbConfiguration.adj <= AdjValue.PreviousAppAdj.getAdjValue()) {
            if (VERSION.SDK_INT <= 23) {
                rbConfiguration.adj /= 100;
            }
            rbConfiguration2.adj = rbConfiguration.adj;
        } else {
            Slog.d("RunningBoosterService", "[updateUserConfiguration] adj values is wrong, so use default adj");
            if (VERSION.SDK_INT > 23) {
                rbConfiguration2.adj = AdjValue.PerceptibleAppAdj.getAdjValue();
            } else {
                rbConfiguration2.adj = AdjValue.PerceptibleAppAdj.getAdjValue() / 100;
            }
        }
        if (JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE != rbConfiguration.keepRecentTaskNumner && rbConfiguration.keepRecentTaskNumner < 10) {
            rbConfiguration2.keepRecentTaskNumner = rbConfiguration.keepRecentTaskNumner;
        }
        if (JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE != rbConfiguration.keepNotificationAPPNumber) {
            rbConfiguration2.keepNotificationAPPNumber = rbConfiguration.keepNotificationAPPNumber;
        }
        if (rbConfiguration2.checkLocationServiceApp != rbConfiguration.checkLocationServiceApp) {
            rbConfiguration2.checkLocationServiceApp = rbConfiguration.checkLocationServiceApp;
        }
        if (rbConfiguration2.enableLauncherWidget != rbConfiguration.enableLauncherWidget) {
            rbConfiguration2.enableLauncherWidget = rbConfiguration.enableLauncherWidget;
        }
        if (rbConfiguration2.enableRunningBooster != rbConfiguration.enableRunningBooster) {
            rbConfiguration2.enableRunningBooster = rbConfiguration.enableRunningBooster;
        }
        Slog.d("RunningBoosterService", "[updateUserConfiguration] newConfig.adj = " + rbConfiguration2.adj + "; keepRecentTaskNumner= " + rbConfiguration2.keepRecentTaskNumner + "; keepNotificationAPPNumber= " + rbConfiguration2.keepNotificationAPPNumber + "; checkLocationServiceApp= " + rbConfiguration2.checkLocationServiceApp + "; enableLauncherWidget= " + rbConfiguration2.enableLauncherWidget + "; enableRunningBooster= " + rbConfiguration2.enableRunningBooster);
        rbConfiguration2.whiteList = a(rbConfiguration.whiteList, rbConfiguration2.whiteList);
        if (!IS_USER_BUILD || this.DEBUG) {
            it = rbConfiguration2.whiteList.iterator();
            while (it.hasNext()) {
                Slog.d("RunningBoosterService", "[updateUserConfiguration] whiteList= " + ((String) it.next()));
            }
        }
        rbConfiguration2.blackList = a(rbConfiguration.blackList, rbConfiguration2.blackList);
        rbConfiguration2.suppressPoint = updateSuppressionPoint(i, rbConfiguration.suppressPoint, rbConfiguration2.suppressPoint);
        if (!IS_USER_BUILD || this.DEBUG) {
            Slog.d("RunningBoosterService", "[updateUserConfiguration] suppressPoint size = " + rbConfiguration2.suppressPoint.size());
            it = rbConfiguration2.suppressPoint.iterator();
            while (it.hasNext()) {
                Slog.d("RunningBoosterService", "[updateUserConfiguration] mSuppressTag = " + ((SuppressionPoint) it.next()).mSuppressTag);
            }
        }
        return rbConfiguration2;
    }

    private ArrayList<String> a(ArrayList<String> arrayList, ArrayList<String> arrayList2) {
        if (1 == arrayList.size() && arrayList.contains("initial")) {
            return arrayList2;
        }
        arrayList.remove("initial");
        return arrayList;
    }

    public ArrayList<SuppressionPoint> updateSuppressionPoint(int i, ArrayList<SuppressionPoint> arrayList, ArrayList<SuppressionPoint> arrayList2) {
        Slog.d("RunningBoosterService", "[updateSuppressionPoint] enter");
        if (1 == arrayList.size() && ((SuppressionPoint) arrayList.get(0)).equal(RbConfiguration.DEAULT_STARTPOINT)) {
            return arrayList2;
        }
        arrayList.remove(RbConfiguration.DEAULT_STARTPOINT);
        Iterator it = arrayList.iterator();
        int i2 = 0;
        while (it.hasNext()) {
            SuppressionPoint suppressionPoint = (SuppressionPoint) it.next();
            suppressionPoint.mSuppressTag = Integer.toString(i) + LocationManagerService.OPPO_FAKE_LOCATION_SPLIT + Integer.toString(i2);
            i2++;
            this.al.put(suppressionPoint.mSuppressTag, Boolean.valueOf(false));
            Slog.d("RunningBoosterService", "[updateSuppressionPoint] point.mSuppressTag = " + suppressionPoint.mSuppressTag);
        }
        return arrayList;
    }

    private void a(int i, String str) {
        File file = new File(this.M);
        try {
            File parentFile = file.getParentFile();
            if (parentFile != null) {
                if (!parentFile.exists()) {
                    parentFile.mkdirs();
                }
                FileUtils.setPermissions(parentFile.getPath(), 509, -1, -1);
            }
            if (!file.exists()) {
                file.createNewFile();
            }
            FileUtils.setPermissions(file.getPath(), 438, -1, -1);
            try {
                BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(this.M, true));
                bufferedWriter.write(String.valueOf(i));
                bufferedWriter.newLine();
                bufferedWriter.write(str);
                bufferedWriter.newLine();
                bufferedWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (Throwable e2) {
            Slog.w("RunningBoosterService", "Unable to prepare stack trace file ", e2);
        }
    }

    private void h() {
        Throwable e;
        BufferedReader bufferedReader;
        try {
            bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(this.M), "utf-8"));
            while (true) {
                try {
                    String readLine = bufferedReader.readLine();
                    if (readLine == null) {
                        try {
                            bufferedReader.close();
                            return;
                        } catch (Throwable e2) {
                            Slog.e("RunningBoosterService", "file closed fail ", e2);
                            return;
                        }
                    }
                    int intValue = Integer.valueOf(readLine).intValue();
                    if (this.aa.get(Integer.valueOf(intValue)) == null) {
                        this.aa.put(Integer.valueOf(intValue), bufferedReader.readLine());
                    }
                } catch (Exception e3) {
                    e2 = e3;
                }
            }
        } catch (Exception e4) {
            e2 = e4;
            bufferedReader = null;
            try {
                Slog.e("RunningBoosterService", "getPackageList fail ", e2);
                try {
                    bufferedReader.close();
                } catch (Throwable e22) {
                    Slog.e("RunningBoosterService", "file closed fail ", e22);
                }
            } catch (Throwable th) {
                e22 = th;
                try {
                    bufferedReader.close();
                } catch (Throwable e5) {
                    Slog.e("RunningBoosterService", "file closed fail ", e5);
                }
                throw e22;
            }
        } catch (Throwable th2) {
            e22 = th2;
            bufferedReader = null;
            bufferedReader.close();
            throw e22;
        }
    }

    private void a(int i, String str, boolean z) {
        if (this.ab.size() != 0) {
            ArrayList arrayList = (ArrayList) this.ab.get(str);
            if (arrayList != null) {
                Slog.d("RunningBoosterService", "[clearPolicyAndSuppressList] pkgname = " + str);
                Iterator it = arrayList.iterator();
                while (it.hasNext()) {
                    c((SuppressionPoint) it.next());
                }
            }
        }
        i(str);
        this.ab.remove(str);
        this.ac.remove(str);
        if (!z) {
            this.ae.remove(i);
            this.aa.remove(Integer.valueOf(i));
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:80:0x0279  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void a(SuppressionPoint suppressionPoint) {
        Throwable e;
        IOException e2;
        ArrayList arrayList;
        Iterator it;
        Trace.traceBegin(64, "updateAllowList");
        Slog.d("RunningBoosterService", "[updateAllowList] enter");
        RbConfiguration rbConfiguration = suppressionPoint.mConfig;
        ArrayList arrayList2 = new ArrayList();
        Object arrayList3 = new ArrayList();
        ArrayList arrayList4 = new ArrayList();
        arrayList3.addAll(rbConfiguration.blackList);
        arrayList4.addAll(rbConfiguration.whiteList);
        if (this.DEBUG) {
            Slog.d("RunningBoosterService", "[updateAllowList] => setPackageListByAdj");
        }
        b(rbConfiguration.adj);
        if (this.DEBUG) {
            Slog.d("RunningBoosterService", "[updateAllowList] <= setPackageListByAdj");
        }
        arrayList2.addAll(this.au);
        if (this.DEBUG) {
            Slog.d("RunningBoosterService", "[updateAllowList] => getPackageListByRecentTask");
        }
        arrayList2.addAll(c(rbConfiguration.keepRecentTaskNumner));
        if (this.DEBUG) {
            Slog.d("RunningBoosterService", "[updateAllowList] <= getPackageListByRecentTask");
        }
        if (this.DEBUG) {
            Slog.d("RunningBoosterService", "[updateAllowList] => getPackageListByNotification");
        }
        arrayList2.addAll(d(rbConfiguration.keepNotificationAPPNumber));
        if (this.DEBUG) {
            Slog.d("RunningBoosterService", "[updateAllowList] <= getPackageListByNotification");
        }
        Slog.d("RunningBoosterService", "[updateAllowList] mAllowPkgList.remove mSuppressTag = " + suppressionPoint.mSuppressTag);
        this.af.remove(suppressionPoint.mSuppressTag);
        this.ag.remove(suppressionPoint.mSuppressTag);
        this.ah.remove(suppressionPoint.mSuppressTag);
        this.ai.remove(suppressionPoint.mSuppressTag);
        if (rbConfiguration.enableLauncherWidget) {
            if (this.DEBUG) {
                Slog.d("RunningBoosterService", "[updateAllowList] => getPackageListByAPPWidget");
            }
            arrayList2.addAll(i());
            if (this.DEBUG) {
                Slog.d("RunningBoosterService", "[updateAllowList] <= getPackageListByAPPWidget");
            }
        }
        if (rbConfiguration.checkLocationServiceApp) {
            if (this.DEBUG) {
                Slog.d("RunningBoosterService", "[updateAllowList] => getPackageListByLocation");
            }
            arrayList2.addAll(j());
            if (this.DEBUG) {
                Slog.d("RunningBoosterService", "[updateAllowList] <= getPackageListByLocation");
            }
        }
        if (!arrayList2.contains(this.V)) {
            arrayList2.add(this.V);
            Slog.d("RunningBoosterService", "[updateAllowList] mPausePkgNameAfterScreenOff = " + this.V);
        }
        if (!IS_USER_BUILD || this.DEBUG) {
            b("/data/runningbooster/adj_allow_list.txt", this.au);
            b("/data/runningbooster/adj_kill_list.txt", this.av);
            b("/data/runningbooster/recent_list.txt", c(rbConfiguration.keepRecentTaskNumner));
            b("/data/runningbooster/notification_list.txt", d(rbConfiguration.keepNotificationAPPNumber));
            b("/data/runningbooster/appwidget_list.txt", i());
            b("/data/runningbooster/location_list.txt", j());
        }
        if (!IS_USER_BUILD || this.DEBUG) {
            List<ApplicationInfo> installedApplications = this.mContext.getPackageManager().getInstalledApplications(41472);
            if (this.DEBUG) {
                BufferedWriter bufferedWriter;
                try {
                    bufferedWriter = new BufferedWriter(new FileWriter("/data/runningbooster/install_list.txt", false));
                    try {
                        Slog.d("RunningBoosterService", "write install_list.txt");
                        for (ApplicationInfo applicationInfo : installedApplications) {
                            bufferedWriter.write(applicationInfo.packageName);
                            bufferedWriter.newLine();
                        }
                        try {
                            bufferedWriter.close();
                        } catch (Throwable e3) {
                            Slog.e("RunningBoosterService", "file closed fail ", e3);
                        }
                    } catch (IOException e4) {
                        e2 = e4;
                    }
                } catch (IOException e5) {
                    e2 = e5;
                    bufferedWriter = null;
                    try {
                        Slog.e("RunningBoosterService", "write install_list.txt fail");
                        e2.printStackTrace();
                        try {
                            bufferedWriter.close();
                        } catch (Throwable e32) {
                            Slog.e("RunningBoosterService", "file closed fail ", e32);
                        }
                        arrayList = new ArrayList();
                        arrayList.addAll(arrayList3);
                        it = arrayList.iterator();
                        while (it.hasNext()) {
                        }
                        b("/data/runningbooster/allow_list_bytype.txt", arrayList2);
                        this.af.put(suppressionPoint.mSuppressTag, arrayList4);
                        this.ag.put(suppressionPoint.mSuppressTag, arrayList3);
                        this.al.put(suppressionPoint.mSuppressTag, Boolean.valueOf(true));
                        Slog.d("RunningBoosterService", "[updateAllowList] exit mSuppressTag = " + suppressionPoint.mSuppressTag);
                        Trace.traceEnd(64);
                    } catch (Throwable th) {
                        e32 = th;
                        try {
                            bufferedWriter.close();
                        } catch (Throwable e6) {
                            Slog.e("RunningBoosterService", "file closed fail ", e6);
                        }
                        throw e32;
                    }
                } catch (Throwable th2) {
                    e32 = th2;
                    bufferedWriter = null;
                    bufferedWriter.close();
                    throw e32;
                }
            }
        }
        arrayList = new ArrayList();
        arrayList.addAll(arrayList3);
        it = arrayList.iterator();
        while (it.hasNext()) {
            String str = (String) it.next();
            if (arrayList2.contains(str)) {
                arrayList4.add(str);
                arrayList3.remove(str);
            }
        }
        b("/data/runningbooster/allow_list_bytype.txt", arrayList2);
        this.af.put(suppressionPoint.mSuppressTag, arrayList4);
        this.ag.put(suppressionPoint.mSuppressTag, arrayList3);
        this.al.put(suppressionPoint.mSuppressTag, Boolean.valueOf(true));
        Slog.d("RunningBoosterService", "[updateAllowList] exit mSuppressTag = " + suppressionPoint.mSuppressTag);
        Trace.traceEnd(64);
    }

    private void b(SuppressionPoint suppressionPoint) {
        Trace.traceBegin(64, "startSuppress");
        if (true != ((Boolean) this.al.get(suppressionPoint.mSuppressTag)).booleanValue()) {
            Slog.d("RunningBoosterService", "[startSuppress] Need to update allow list mSuppressTag = " + suppressionPoint.mSuppressTag);
            a(suppressionPoint);
        } else {
            Slog.d("RunningBoosterService", "[startSuppress] start to suppress tag = " + suppressionPoint.mSuppressTag + " mIsUpdateAllowList = " + this.al.get(suppressionPoint.mSuppressTag));
            this.al.put(suppressionPoint.mSuppressTag, Boolean.valueOf(false));
        }
        synchronized (this.L) {
            g(suppressionPoint.mSuppressTag);
        }
        this.X.startFrc(suppressionPoint.mSuppressTag, 3, (List) this.af.get(suppressionPoint.mSuppressTag));
        this.Y.startSuppression(suppressionPoint.mSuppressTag, 7, 1064374545, suppressionPoint.mSuppressTag, (List) this.af.get(suppressionPoint.mSuppressTag));
        Slog.d("RunningBoosterService", "[startSuppress] mKillPkgList size = " + ((ArrayList) this.ah.get(suppressionPoint.mSuppressTag)).size());
        if (this.Z != null) {
            this.Z.suppressPackages((List) this.ah.get(suppressionPoint.mSuppressTag), 1064374545, suppressionPoint.mSuppressTag);
        }
        this.am.put(suppressionPoint.mSuppressTag, Boolean.valueOf(true));
        if (!this.ak.contains(suppressionPoint.mSuppressTag)) {
            this.ak.add(suppressionPoint.mSuppressTag);
        }
        Slog.d("RunningBoosterService", "[startSuppress] Suppress exit");
        Trace.traceEnd(64);
    }

    private void g(String str) {
        String str2;
        Slog.d("RunningBoosterService", "[updateTwoPhaseKillList] suppressTag = " + str);
        ArrayList arrayList = (ArrayList) this.ag.get(str);
        ArrayList arrayList2 = (ArrayList) this.af.get(str);
        ArrayList arrayList3 = (ArrayList) this.aq.get(str);
        if (arrayList3 != null) {
            Iterator it = arrayList3.iterator();
            while (it.hasNext()) {
                str2 = (String) it.next();
                if (!IS_USER_BUILD || this.DEBUG) {
                    Slog.d("RunningBoosterService", "[updateTwoPhaseKillList] pkgName = " + str2);
                }
                if (!arrayList2.contains(str2)) {
                    arrayList2.add(str2);
                    arrayList.remove(str2);
                }
            }
        }
        ArrayList arrayList4 = new ArrayList();
        ArrayList arrayList5 = new ArrayList();
        Iterator it2 = arrayList.iterator();
        while (it2.hasNext()) {
            str2 = (String) it2.next();
            if (this.av.contains(str2)) {
                if (!IS_USER_BUILD || this.DEBUG) {
                    Slog.d("RunningBoosterService", "[updateTwoPhaseKillList] tmpFirstKillPkgList pkg = " + str2);
                }
                arrayList4.add(str2);
            } else {
                if (!IS_USER_BUILD || this.DEBUG) {
                    Slog.d("RunningBoosterService", "[updateTwoPhaseKillList] tmpSecondKillPkgList pjg = " + str2);
                }
                arrayList5.add(str2);
            }
        }
        this.af.put(str, arrayList2);
        this.ag.put(str, arrayList);
        this.ah.put(str, arrayList4);
        this.ai.put(str, arrayList5);
        b("/data/runningbooster/allow_list.txt", arrayList2);
        b("/data/runningbooster/kill_list.txt", arrayList);
    }

    private void c(SuppressionPoint suppressionPoint) {
        Slog.d("RunningBoosterService", "[stopSuppressByTag] point.mSuppressTag = " + suppressionPoint.mSuppressTag);
        this.X.stopFrc(suppressionPoint.mSuppressTag);
        this.Y.stopSuppression(suppressionPoint.mSuppressTag);
        if (this.Z != null) {
            this.Z.unsuppressPackages(suppressionPoint.mSuppressTag);
        }
        this.ak.remove(suppressionPoint.mSuppressTag);
        this.am.put(suppressionPoint.mSuppressTag, Boolean.valueOf(false));
    }

    private void b(int i) {
        ActivityManager activityManager = (ActivityManager) this.mContext.getSystemService("activity");
        this.au.clear();
        this.av.clear();
        synchronized (this.K) {
            for (Entry entry : this.ao.entrySet()) {
                int intValue = ((Integer) entry.getKey()).intValue();
                Iterator it = ((ArrayList) entry.getValue()).iterator();
                while (it.hasNext()) {
                    String[] packageListFromPid = activityManager.getPackageListFromPid(((Integer) it.next()).intValue());
                    if (packageListFromPid != null) {
                        if (intValue > i) {
                            for (Object add : packageListFromPid) {
                                this.av.add(add);
                            }
                        } else {
                            for (Object add2 : packageListFromPid) {
                                this.au.add(add2);
                            }
                        }
                    }
                }
            }
        }
    }

    /* JADX WARNING: Missing block: B:9:0x0029, code:
            return r2;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private ArrayList<String> c(int i) {
        int i2 = 0;
        ActivityManager activityManager = (ActivityManager) this.mContext.getSystemService("activity");
        ArrayList<String> arrayList = new ArrayList();
        synchronized (this.J) {
            if (this.ap.size() != 0) {
                Iterator it = this.ap.iterator();
                while (it.hasNext() && i2 < i) {
                    RecentTaskInfo recentTaskInfo = (RecentTaskInfo) it.next();
                    if (recentTaskInfo.realActivity != null) {
                        arrayList.add(recentTaskInfo.realActivity.getPackageName());
                        Slog.d("RunningBoosterService", "recentPackage realActivity : " + recentTaskInfo.realActivity.getPackageName());
                        if (!(recentTaskInfo.topActivity == null || recentTaskInfo.realActivity.getPackageName().equals(recentTaskInfo.topActivity.getPackageName()))) {
                            arrayList.add(recentTaskInfo.topActivity.getPackageName());
                            Slog.d("RunningBoosterService", "recentPackage topActivity : " + recentTaskInfo.topActivity.getPackageName());
                        }
                    }
                    i2++;
                }
            } else {
                return arrayList;
            }
        }
    }

    private ArrayList<String> d(int i) {
        StatusBarNotification[] statusBarNotificationArr = null;
        INotificationManager asInterface = INotificationManager.Stub.asInterface(ServiceManager.getService(NotificationManagerService.NOTIFICATON_TITLE_NAME));
        ArrayList<String> arrayList = new ArrayList();
        try {
            statusBarNotificationArr = asInterface.getActiveNotifications(this.mContext.getPackageName());
        } catch (Throwable e) {
            Slog.e("RunningBoosterService", "getPackageListByNotification fail ", e);
        }
        if (statusBarNotificationArr == null) {
            return arrayList;
        }
        if (i > statusBarNotificationArr.length || i == -1) {
            i = statusBarNotificationArr.length;
        }
        for (int i2 = 0; i2 < i; i2++) {
            if (!arrayList.contains(statusBarNotificationArr[i2].getPackageName())) {
                Notification notification = statusBarNotificationArr[i2].getNotification();
                if (!(notification == null || notification.getSmallIcon() == null || (notification.flags & 268435456) != 0)) {
                    arrayList.add(statusBarNotificationArr[i2].getPackageName());
                    Slog.d("RunningBoosterService", "notificationPackagelist : " + statusBarNotificationArr[i2].getPackageName());
                }
            }
        }
        return arrayList;
    }

    private ArrayList<String> i() {
        AppWidgetManager appWidgetManager = (AppWidgetManager) this.mContext.getSystemService(OppoProcessManager.RESUME_REASON_APPWIDGET_CHANGE_STR);
        ArrayList<String> arrayList = new ArrayList();
        PackageManager packageManager = this.mContext.getPackageManager();
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.addCategory("android.intent.category.HOME");
        ResolveInfo resolveActivity = packageManager.resolveActivity(intent, DumpState.DUMP_INSTALLS);
        if (resolveActivity == null) {
            Slog.d("RunningBoosterService", "No Launcher");
            return arrayList;
        }
        String str = resolveActivity.activityInfo.packageName;
        Slog.d("RunningBoosterService", "Current launcherName : " + str);
        List<ComponentName> appWidgetOfHost = appWidgetManager.getAppWidgetOfHost(str, this.mCurrentUserId);
        if (appWidgetOfHost == null) {
            return arrayList;
        }
        for (ComponentName componentName : appWidgetOfHost) {
            arrayList.add(componentName.getPackageName());
            Slog.d("RunningBoosterService", "widgetPackagelist : " + componentName.getPackageName());
        }
        return arrayList;
    }

    private ArrayList<String> j() {
        AppOpsManager appOpsManager = (AppOpsManager) this.mContext.getSystemService("appops");
        int[] iArr = new int[1];
        iArr[0] = 42;
        List packagesForOps = appOpsManager.getPackagesForOps(iArr);
        ArrayList<String> arrayList = new ArrayList();
        if (packagesForOps != null) {
            int size = packagesForOps.size();
            for (int i = 0; i < size; i++) {
                PackageOps packageOps = (PackageOps) packagesForOps.get(i);
                List ops = packageOps.getOps();
                if (ops != null) {
                    int size2 = ops.size();
                    for (int i2 = 0; i2 < size2; i2++) {
                        OpEntry opEntry = (OpEntry) ops.get(i2);
                        if (opEntry.getOp() == 42 && opEntry.isRunning()) {
                            arrayList.add(packageOps.getPackageName());
                            Slog.d("RunningBoosterService", "locationPackagelist : " + packageOps.getPackageName());
                            break;
                        }
                    }
                }
            }
        }
        return arrayList;
    }

    private void a(boolean z, SuppressionPoint suppressionPoint) {
        if (true != z) {
            synchronized (this.af) {
                Slog.d("RunningBoosterService", "[updateOrSuppress] start suppress");
                W.sendMessage(W.obtainMessage(1002, suppressionPoint));
            }
            return;
        }
        synchronized (this.af) {
            Slog.d("RunningBoosterService", "[updateOrSuppress] update allow list");
            W.sendMessage(W.obtainMessage(1001, suppressionPoint));
        }
    }

    public static RunningBoosterService getInstance(SystemReady systemReady) {
        if (I == null) {
            I = new RunningBoosterService((Context) systemReady.get(SystemReady.Index.context));
        }
        return I;
    }

    private SuppressionPoint a(int i, String str, int i2) {
        switch (i) {
            case 1:
            case 2:
                str = "launcher";
                break;
        }
        return new SuppressionPoint(str, i2);
    }

    private void k() {
        Slog.d("RunningBoosterService", "[handleScreenOffEvent] enter");
        W.removeMessages(1007);
        W.removeMessages(ANRManager.RENAME_TRACE_FILES_MSG);
        if (this.ak.size() != 0) {
            this.ad.clear();
            ArrayList arrayList = new ArrayList();
            arrayList.addAll(this.ak);
            Iterator it = arrayList.iterator();
            while (it.hasNext()) {
                String str = (String) it.next();
                Slog.d("RunningBoosterService", "[handleScreenOffEvent] unsuppress tag = " + str);
                ArrayList frcPackageList = this.X.getFrcPackageList(str);
                if (frcPackageList != null) {
                    this.ad.put(str, frcPackageList);
                }
                this.Y.stopSuppression(str);
                if (this.Z != null) {
                    this.Z.unsuppressPackages(str);
                }
                this.ak.remove(str);
            }
            return;
        }
        Slog.d("RunningBoosterService", "[handleScreenOffEvent] mSuppressTagList is null");
    }

    private void h(String str) {
        Trace.traceBegin(64, "handleScreenOnEvent");
        if (this.ad.size() != 0) {
            W.removeMessages(1007);
            Slog.d("RunningBoosterService", "[handleScreenOnEvent] resumePkgName = " + str);
            ArrayList arrayList = new ArrayList();
            Object arrayList2 = new ArrayList();
            arrayList = new ArrayList();
            for (Entry entry : this.ad.entrySet()) {
                String str2 = (String) entry.getKey();
                arrayList = (ArrayList) entry.getValue();
                if (this.DEBUG && arrayList != null) {
                    Iterator it = arrayList.iterator();
                    while (it.hasNext()) {
                        Slog.d("RunningBoosterService", "[handleScreenOnEvent] tmpFrcList pkg = " + ((String) it.next()));
                    }
                }
                arrayList2.addAll((Collection) this.af.get(str2));
                arrayList2.addAll(arrayList);
                Slog.d("RunningBoosterService", "[handleScreenOnEvent] suppress tag = " + str2);
                if (!this.V.equals(str)) {
                    arrayList2.add(str);
                }
                this.af.put(str2, arrayList2);
                ArrayList arrayList3 = (ArrayList) this.ag.get(str2);
                Iterator it2 = arrayList2.iterator();
                while (it2.hasNext()) {
                    String str3 = (String) it2.next();
                    if (arrayList3.contains(str3)) {
                        arrayList3.remove(str3);
                        Slog.d("RunningBoosterService", "[handleScreenOnEvent] tmpKillList remove pkg = " + str3);
                    }
                }
                this.ag.put(str2, arrayList3);
                this.X.updateFrcExtraAllowList(str2, arrayList2);
                this.Y.startSuppression(str2, 7, 1064374545, str2, arrayList2);
                Slog.d("RunningBoosterService", "[handleScreenOnEvent] kill process start");
                if (this.Z != null) {
                    this.Z.suppressPackages(arrayList3, 1064374545, str2);
                }
                if (!this.ak.contains(str2)) {
                    this.ak.add(str2);
                }
            }
            Trace.traceEnd(64);
            return;
        }
        Slog.d("RunningBoosterService", "[handleScreenOnEvent] mCurrentFrcList is null");
    }

    private void l() {
        Slog.d("RunningBoosterService", "[stopAndClearSuppressionTask]");
        if (this.ak.size() != 0) {
            Iterator it = this.ak.iterator();
            while (it.hasNext()) {
                String str = (String) it.next();
                Slog.d("RunningBoosterService", "[stopAndClearSuppressionTask] unsuppress tag = " + str);
                this.X.stopFrc(str);
                this.Y.stopSuppression(str);
                if (this.Z != null) {
                    this.Z.unsuppressPackages(str);
                }
            }
        }
        this.ab.clear();
        this.ac.clear();
        this.ak.clear();
        this.ad.clear();
        this.af.clear();
        this.ag.clear();
        this.ah.clear();
        this.ai.clear();
        this.am.clear();
        this.al.clear();
    }

    private void i(String str) {
        Slog.d("RunningBoosterService", "[clearSuppressionData]");
        if (this.ab.size() != 0) {
            Iterator it = ((ArrayList) this.ab.get(str)).iterator();
            while (it.hasNext()) {
                SuppressionPoint suppressionPoint = (SuppressionPoint) it.next();
                Slog.d("RunningBoosterService", "[clearSuppressionData] remove tag = " + suppressionPoint.mSuppressTag);
                this.ak.remove(suppressionPoint.mSuppressTag);
                this.ad.remove(suppressionPoint.mSuppressTag);
            }
        }
    }

    private void j(String str) {
        Slog.i("RunningBoosterService", "prepareStackTraceFile: " + str);
        if (str != null && str.length() != 0) {
            File file = new File(str);
            try {
                File parentFile = file.getParentFile();
                if (parentFile != null) {
                    if (!parentFile.exists()) {
                        parentFile.mkdirs();
                    }
                    FileUtils.setPermissions(parentFile.getPath(), 509, -1, -1);
                }
                if (!file.exists()) {
                    file.createNewFile();
                }
                FileUtils.setPermissions(file.getPath(), 438, -1, -1);
            } catch (Throwable e) {
                Slog.e("RunningBoosterService", "Unable to prepare stack trace file: " + str, e);
            }
        }
    }

    private void a(RbConfiguration rbConfiguration) {
        Iterator it;
        if (!IS_USER_BUILD || this.DEBUG) {
            Slog.d("RunningBoosterService", "PerceptibleAppAdj = " + AdjValue.PerceptibleAppAdj.getAdjValue());
            Slog.d("RunningBoosterService", "adj=" + rbConfiguration.adj);
            Slog.d("RunningBoosterService", "keepRecentTaskNumner = " + rbConfiguration.keepRecentTaskNumner);
            Slog.d("RunningBoosterService", "keepNotificationAPPNumber = " + rbConfiguration.keepNotificationAPPNumber);
            Slog.d("RunningBoosterService", "checkLocationServiceApp = " + rbConfiguration.checkLocationServiceApp);
            Slog.d("RunningBoosterService", "enableLauncherWidget = " + rbConfiguration.enableLauncherWidget);
            Slog.d("RunningBoosterService", "enableRunningBooster = " + rbConfiguration.enableRunningBooster);
        }
        if (this.DEBUG) {
            it = rbConfiguration.whiteList.iterator();
            while (it.hasNext()) {
                Slog.d("RunningBoosterService", "whiteList = " + ((String) it.next()));
            }
            it = rbConfiguration.blackList.iterator();
            while (it.hasNext()) {
                Slog.d("RunningBoosterService", "blackList = " + ((String) it.next()));
            }
        }
        if (!IS_USER_BUILD || this.DEBUG) {
            it = rbConfiguration.suppressPoint.iterator();
            while (it.hasNext()) {
                SuppressionPoint suppressionPoint = (SuppressionPoint) it.next();
                Slog.d("RunningBoosterService", "point.mPackageName = " + suppressionPoint.mPackageName + " point.mAppState = " + suppressionPoint.mAppState + " point.mSuppressTag = " + suppressionPoint.mSuppressTag);
            }
        }
    }

    private void b(String str, ArrayList<String> arrayList) {
        Throwable e;
        IOException e2;
        if ((!IS_USER_BUILD || this.DEBUG) && arrayList != null) {
            BufferedWriter bufferedWriter;
            try {
                bufferedWriter = new BufferedWriter(new FileWriter(str, false));
                try {
                    Slog.d("RunningBoosterService", "saveDataToFile");
                    Iterator it = arrayList.iterator();
                    while (it.hasNext()) {
                        String str2 = (String) it.next();
                        if (str2 != null) {
                            bufferedWriter.write(str2);
                            bufferedWriter.newLine();
                        }
                    }
                    try {
                        bufferedWriter.close();
                    } catch (Throwable e3) {
                        Slog.e("RunningBoosterService", "file closed fail ", e3);
                    }
                } catch (IOException e4) {
                    e2 = e4;
                    try {
                        Slog.e("RunningBoosterService", "saveDataToFile fail");
                        e2.printStackTrace();
                        try {
                            bufferedWriter.close();
                        } catch (Throwable e32) {
                            Slog.e("RunningBoosterService", "file closed fail ", e32);
                        }
                    } catch (Throwable th) {
                        e32 = th;
                        try {
                            bufferedWriter.close();
                        } catch (Throwable e5) {
                            Slog.e("RunningBoosterService", "file closed fail ", e5);
                        }
                        throw e32;
                    }
                }
            } catch (IOException e6) {
                e2 = e6;
                bufferedWriter = null;
                Slog.e("RunningBoosterService", "saveDataToFile fail");
                e2.printStackTrace();
                bufferedWriter.close();
            } catch (Throwable th2) {
                e32 = th2;
                bufferedWriter = null;
                bufferedWriter.close();
                throw e32;
            }
        }
    }

    private static IPackageManager a() {
        IPackageManager asInterface = IPackageManager.Stub.asInterface(ServiceManager.getService("package"));
        if (asInterface != null) {
            return asInterface;
        }
        throw new RuntimeException("null package manager service");
    }
}
