package com.mediatek.apm.suppression;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.SystemProperties;
import android.util.Slog;
import com.android.server.LocationManagerService;
import com.android.server.am.OppoProcessManager;
import com.mediatek.am.AMEventHookAction;
import com.mediatek.am.AMEventHookData.BeforeSendBroadcast;
import com.mediatek.am.AMEventHookData.PackageStoppedStatusChanged;
import com.mediatek.am.AMEventHookData.ReadyToStartComponent;
import com.mediatek.am.AMEventHookData.ReadyToStartComponent.Index;
import com.mediatek.am.AMEventHookResult;
import java.util.ArrayList;
import java.util.List;

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
    	at jadx.core.dex.visitors.ExtractFieldInit.checkStaticFieldsInit(ExtractFieldInit.java:58)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class SuppressionAction {
    private static SuppressionAction i;
    private static SuppressionPolicy j;
    private static Context mContext;
    private List<a> k;
    private List<b> l;
    private List<String> m;
    private boolean n;
    private boolean o;

    class a {
        int p;
        String packageName;

        a() {
        }
    }

    class b {
        List<String> r;
        String tag;

        b() {
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.mediatek.apm.suppression.SuppressionAction.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
        	... 5 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.mediatek.apm.suppression.SuppressionAction.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.apm.suppression.SuppressionAction.<clinit>():void");
    }

    private SuppressionAction(Context context) {
        this.k = null;
        this.l = null;
        this.m = null;
        this.n = false;
        this.o = "user".equals(Build.TYPE);
        mContext = context;
        new com.mediatek.common.jpe.a().a();
        if (j == null) {
            j = SuppressionPolicy.getInstance();
        }
        this.k = new ArrayList();
        this.l = new ArrayList();
        this.n = SystemProperties.get("persist.sys.apm.debug_mode").equals(LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON);
    }

    public static SuppressionAction getInstance(Context context) {
        if (i == null) {
            i = new SuppressionAction(context);
        }
        return i;
    }

    public void onReadyToStartComponent(ReadyToStartComponent readyToStartComponent) {
        String string = readyToStartComponent.getString(Index.packageName);
        int i = readyToStartComponent.getInt(Index.uid);
        List list = (List) readyToStartComponent.get(Index.callerList);
        List list2 = (List) readyToStartComponent.get(Index.callerUidList);
        List list3 = (List) readyToStartComponent.get(Index.delayedCallerList);
        List list4 = (List) readyToStartComponent.get(Index.delayedCallerUidList);
        List list5 = (List) readyToStartComponent.get(Index.clientList);
        List list6 = (List) readyToStartComponent.get(Index.clientUidList);
        String string2 = readyToStartComponent.getString(Index.suppressReason);
        readyToStartComponent.getString(Index.suppressAction);
        String str = "allowed";
        if (this.n) {
            Slog.d("SuppressionAction", "onReadyToStartComponent: begin! package = " + string);
        }
        ArrayList suppressionList = j.getSuppressionList();
        if (string2 != null) {
            if (suppressionList != null) {
                int i2 = 0;
                while (true) {
                    int i3 = i2;
                    if (i3 >= suppressionList.size()) {
                        break;
                    }
                    List arrayList = new ArrayList();
                    List arrayList2 = new ArrayList();
                    String str2 = (String) suppressionList.get(i3);
                    int d = j.d(str2);
                    String str3;
                    Object[] objArr;
                    if (string2.equals("bind service") || string2.equals("delayed service") || string2.equals("restart service") || string2.equals("start service")) {
                        if ((d & 32) != 0 || (d & 64) != 0) {
                            arrayList.add(string);
                            arrayList2.add(new Integer(i));
                            if (string2.equals("bind service") || string2.equals("start service")) {
                                if (!(list == null || list2 == null)) {
                                    arrayList.addAll(list);
                                    arrayList2.addAll(list2);
                                }
                            } else if (string2.equals("restart service")) {
                                if (!(list5 == null || list6 == null)) {
                                    arrayList.addAll(list5);
                                    arrayList2.addAll(list6);
                                }
                            } else if (!(!string2.equals("delayed service") || list3 == null || list4 == null)) {
                                arrayList.addAll(list3);
                                arrayList2.addAll(list4);
                            }
                            if (!a(str2, arrayList, arrayList2)) {
                                String str4 = (d & 32) == 0 ? (d & 64) == 0 ? str : "skipped" : "delayed";
                                if (this.n) {
                                    Slog.d("SuppressionAction", "onReadyToStartComponent:suppressClient = " + str2 + " suppressPolicy = " + d + " suppressReason = " + string2 + " suppressAction = " + str4);
                                }
                                Object[] objArr2 = new Object[10];
                                objArr2[0] = string;
                                objArr2[1] = Integer.valueOf(i);
                                objArr2[2] = list;
                                objArr2[3] = list2;
                                objArr2[4] = list3;
                                objArr2[5] = list4;
                                objArr2[6] = list5;
                                objArr2[7] = list6;
                                objArr2[8] = string2;
                                objArr2[9] = str4;
                                readyToStartComponent.set(objArr2);
                                return;
                            }
                        }
                    } else if (string2.equals(OppoProcessManager.RESUME_REASON_BROADCAST_STR)) {
                        if ((d & 16384) != 0) {
                            arrayList.add(string);
                            arrayList2.add(new Integer(i));
                            if (!a(str2, arrayList, arrayList2)) {
                                str3 = "skipped";
                                if (this.n) {
                                    Slog.d("SuppressionAction", "onReadyToStartComponent:suppressClient = " + str2 + " suppressPolicy = " + d + " suppressReason = " + string2 + " suppressAction = " + str3);
                                }
                                objArr = new Object[10];
                                objArr[0] = string;
                                objArr[1] = Integer.valueOf(i);
                                objArr[2] = null;
                                objArr[3] = null;
                                objArr[4] = null;
                                objArr[5] = null;
                                objArr[6] = null;
                                objArr[7] = null;
                                objArr[8] = OppoProcessManager.RESUME_REASON_BROADCAST_STR;
                                objArr[9] = str3;
                                readyToStartComponent.set(objArr);
                                return;
                            }
                        } else {
                            continue;
                        }
                    } else if (string2.equals("broadcast_p")) {
                        if ((32768 & d) != 0) {
                            arrayList.add(string);
                            arrayList2.add(new Integer(i));
                            if (!a(str2, arrayList, arrayList2)) {
                                str3 = "skipped";
                                if (this.n) {
                                    Slog.d("SuppressionAction", "onReadyToStartComponent:suppressClient = " + str2 + " suppressPolicy = " + d + " suppressReason = " + string2 + " suppressAction = " + str3);
                                }
                                objArr = new Object[10];
                                objArr[0] = string;
                                objArr[1] = Integer.valueOf(i);
                                objArr[2] = null;
                                objArr[3] = null;
                                objArr[4] = null;
                                objArr[5] = null;
                                objArr[6] = null;
                                objArr[7] = null;
                                objArr[8] = "broadcast_p";
                                objArr[9] = str3;
                                readyToStartComponent.set(objArr);
                                return;
                            }
                        } else {
                            continue;
                        }
                    } else if (string2.equals(OppoProcessManager.RESUME_REASON_PROVIDER_STR)) {
                        if ((d & 512) != 0) {
                            arrayList.add(string);
                            arrayList2.add(new Integer(i));
                            if (!(list == null || list2 == null)) {
                                arrayList.addAll(list);
                                arrayList2.addAll(list2);
                            }
                            if (!a(str2, arrayList, arrayList2)) {
                                String str5 = "delayed";
                                if (this.n) {
                                    Slog.d("SuppressionAction", "onReadyToStartComponent:suppressClient = " + str2 + " suppressPolicy = " + d + " suppressReason = " + string2 + " suppressAction = " + str5);
                                }
                                Object[] objArr3 = new Object[10];
                                objArr3[0] = string;
                                objArr3[1] = Integer.valueOf(i);
                                objArr3[2] = list;
                                objArr3[3] = list2;
                                objArr3[4] = null;
                                objArr3[5] = null;
                                objArr3[6] = null;
                                objArr3[7] = null;
                                objArr3[8] = OppoProcessManager.RESUME_REASON_PROVIDER_STR;
                                objArr3[9] = str5;
                                readyToStartComponent.set(objArr3);
                                return;
                            }
                        } else {
                            continue;
                        }
                    } else if (this.n) {
                        Slog.d("SuppressionAction", "onReadyToStartComponent: not support " + string2);
                    }
                    i2 = i3 + 1;
                }
            }
            if (this.n) {
                Slog.d("SuppressionAction", "onReadyToStartComponent: end! suppressAction = " + str);
            }
            return;
        }
        if (this.n) {
            Slog.d("SuppressionAction", "onReadyToStartComponent: suppressReason = null!");
        }
    }

    private boolean a(String str, List<String> list, List<Integer> list2) {
        for (int i = 0; i < list.size(); i++) {
            if (!j.isPackageInSuppression(str, (String) list.get(i), ((Integer) list2.get(i)).intValue())) {
                return true;
            }
        }
        return false;
    }

    public int getSuppressPackagePolicy(int i) {
        int i2 = 0;
        if ((DumpState.DUMP_DEXOPT & i) != 0) {
            i2 = 1;
        }
        if ((DumpState.DUMP_COMPILER_STATS & i) != 0) {
            i2 |= 2;
        }
        if ((4194304 & i) != 0) {
            i2 |= 4;
        }
        if ((8388608 & i) != 0) {
            i2 |= 8388616;
        }
        if ((16777216 & i) != 0) {
            i2 |= 16;
        }
        if ((33554432 & i) != 0) {
            i2 |= 32;
        }
        if ((67108864 & i) != 0) {
            i2 |= 64;
        }
        if ((134217728 & i) != 0) {
            i2 |= 128;
        }
        if ((268435456 & i) != 0) {
            i2 |= 256;
        }
        if ((536870912 & i) != 0) {
            i2 |= 512;
        }
        if (this.n) {
            Slog.d("SuppressionAction", "getSuppressPackagePolicy: suppressPolicy = " + i + " suppressPackagePolicy = " + i2);
        }
        return i2;
    }

    public int getUnsuppressPackagePolicy(int i) {
        int i2 = 0;
        if (i == 0) {
            i2 = 1;
        }
        if (this.n) {
            Slog.d("SuppressionAction", "getUnsuppressPackagePolicy: unsuppressPolicy = " + i + " unsuppressPackagePolicy = " + i2);
        }
        return i2;
    }

    /* JADX WARNING: Missing block: B:10:0x004e, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void onPackageStoppedStatusChanged(PackageStoppedStatusChanged packageStoppedStatusChanged) {
        int i = 0;
        synchronized (this) {
            String string = packageStoppedStatusChanged.getString(PackageStoppedStatusChanged.Index.packageName);
            int i2 = packageStoppedStatusChanged.getInt(PackageStoppedStatusChanged.Index.suppressAction);
            String string2 = packageStoppedStatusChanged.getString(PackageStoppedStatusChanged.Index.tag);
            if (i2 == 1) {
                i = b(string2);
                b bVar;
                if (i >= 0) {
                    bVar = (b) this.l.get(i);
                    if (a(string, bVar.r)) {
                        return;
                    }
                    bVar.r.add(string);
                } else {
                    bVar = new b();
                    bVar.tag = string2;
                    bVar.r = new ArrayList();
                    bVar.r.add(string);
                    this.l.add(bVar);
                }
                i = c(string);
                a aVar;
                if (i >= 0) {
                    aVar = (a) this.k.get(i);
                    aVar.p++;
                } else {
                    aVar = new a();
                    aVar.packageName = string;
                    aVar.p = 1;
                    this.k.add(aVar);
                }
            } else if (i2 == 0) {
                while (true) {
                    int i3 = i;
                    if (i3 >= this.l.size()) {
                        break;
                    }
                    ((b) this.l.get(i3)).r.remove(string);
                    i = i3 + 1;
                }
                i = c(string);
                if (i >= 0) {
                    ((a) this.k.get(i)).p = 0;
                }
            }
            if (!this.o || this.n) {
                Slog.d("SuppressionAction", "onPackageStoppedStatusChanged: packageName = " + string + " suppressAction = " + i2 + " tag = " + string2);
            }
        }
    }

    /* JADX WARNING: Missing block: B:9:0x0013, code:
            return null;
     */
    /* JADX WARNING: Missing block: B:16:0x002f, code:
            return r3;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public List<String> getUnsuppressPackageList(String str) {
        int i = 0;
        synchronized (this) {
            List<String> arrayList = new ArrayList();
            int b = b(str);
            if (b >= 0) {
                b bVar = (b) this.l.get(b);
                while (true) {
                    int i2 = i;
                    if (i2 >= bVar.r.size()) {
                        this.l.remove(b);
                        if (this.n) {
                            Slog.d("SuppressionAction", "getUnsuppressPackageList: tag = " + str + " unSuppressPackageList.size() = " + arrayList.size());
                        }
                    } else {
                        int c = c((String) bVar.r.get(i2));
                        if (c >= 0) {
                            if (((a) this.k.get(c)).p <= 1) {
                                ((a) this.k.get(c)).p = 0;
                                arrayList.add(bVar.r.get(i2));
                            } else {
                                a aVar = (a) this.k.get(c);
                                aVar.p--;
                            }
                        }
                        i = i2 + 1;
                    }
                }
            } else if (this.n) {
                Slog.d("SuppressionAction", "getUnsuppressPackageList: tag = " + str + " not found.");
            }
        }
    }

    private int b(String str) {
        int i = 0;
        if (str == null) {
            return -1;
        }
        while (true) {
            int i2 = i;
            if (i2 >= this.l.size()) {
                return -1;
            }
            if (str.equals(((b) this.l.get(i2)).tag)) {
                return i2;
            }
            i = i2 + 1;
        }
    }

    private int c(String str) {
        int i = 0;
        if (str == null) {
            return -1;
        }
        while (true) {
            int i2 = i;
            if (i2 >= this.k.size()) {
                return -1;
            }
            if (str.equals(((a) this.k.get(i2)).packageName)) {
                return i2;
            }
            i = i2 + 1;
        }
    }

    private boolean a(String str, List<String> list) {
        if (str == null) {
            return false;
        }
        for (int i = 0; i < list.size(); i++) {
            if (str.equals(list.get(i))) {
                return true;
            }
        }
        return false;
    }

    public AMEventHookResult onBeforeSendBroadcast(BeforeSendBroadcast beforeSendBroadcast, AMEventHookResult aMEventHookResult) {
        Object obj = null;
        ArrayList suppressionList = j.getSuppressionList();
        if (suppressionList != null) {
            int i = 0;
            Object obj2 = null;
            while (i < suppressionList.size()) {
                Object obj3;
                String str = (String) suppressionList.get(i);
                if ((j.d(str) & DumpState.DUMP_COMPILER_STATS) == 0) {
                    obj3 = obj2;
                } else {
                    List list = (List) beforeSendBroadcast.get(BeforeSendBroadcast.Index.filterStaticList);
                    if (this.m == null) {
                        this.m = new ArrayList();
                        List<ApplicationInfo> installedApplications = mContext.getPackageManager().getInstalledApplications(8704);
                        List queryIntentActivities = mContext.getPackageManager().queryIntentActivities(new Intent("android.intent.action.MAIN", null).addCategory("android.intent.category.LAUNCHER"), 512);
                        for (ApplicationInfo applicationInfo : installedApplications) {
                            String str2 = applicationInfo.packageName;
                            if ((applicationInfo.flags & 1) != 0 && b(str2, queryIntentActivities)) {
                                this.m.add(str2);
                            }
                        }
                    }
                    Object obj4 = obj2;
                    for (int i2 = 0; i2 < this.m.size(); i2++) {
                        String str3 = (String) this.m.get(i2);
                        if (j.isPackageInSuppression(str, str3, 9999)) {
                            list.add(str3);
                            obj4 = 1;
                        }
                    }
                    obj3 = obj4;
                }
                i++;
                obj2 = obj3;
            }
            obj = obj2;
        }
        if (obj != null) {
            aMEventHookResult.addAction(AMEventHookAction.AM_FilterStaticReceiver);
        }
        return aMEventHookResult;
    }

    private boolean b(String str, List<ResolveInfo> list) {
        for (int i = 0; i < list.size(); i++) {
            if (str.equals(((ResolveInfo) list.get(i)).activityInfo.packageName)) {
                return true;
            }
        }
        return false;
    }
}
