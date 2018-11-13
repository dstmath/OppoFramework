package com.mediatek.apm.frc;

import android.os.Build;
import android.os.SystemProperties;
import android.util.ArrayMap;
import android.util.Slog;
import com.android.server.LocationManagerService;
import com.mediatek.am.AMEventHookData.BeforeActivitySwitch;
import com.mediatek.am.AMEventHookData.BeforeActivitySwitch.Index;
import com.mediatek.am.AMEventHookData.ReadyToGetProvider;
import com.mediatek.am.AMEventHookData.ReadyToStartDynamicReceiver;
import com.mediatek.am.AMEventHookData.ReadyToStartService;
import com.mediatek.am.AMEventHookData.ReadyToStartStaticReceiver;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;

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
public class FocusRelationshipChainPolicy {
    public static final int FRC_ADD_POLICY_EMPTY = 0;
    public static final int FRC_ADD_POLICY_FROM_EXTRA_ALLOW_LIST = 1;
    public static final int FRC_ADD_POLICY_FROM_FRC = 8;
    public static final int FRC_ADD_POLICY_FROM_OTHERS = 4;
    public static final int FRC_ADD_POLICY_FROM_SYSTEM_CALLER = 2;
    private static FocusRelationshipChainPolicy c;
    private boolean a;
    private boolean b;
    private ArrayMap<String, a> d;

    class a {
        int e;
        List<String> f;
        HashSet<String> g;

        a() {
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.mediatek.apm.frc.FocusRelationshipChainPolicy.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.mediatek.apm.frc.FocusRelationshipChainPolicy.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.apm.frc.FocusRelationshipChainPolicy.<clinit>():void");
    }

    private FocusRelationshipChainPolicy() {
        this.a = false;
        this.b = "user".equals(Build.TYPE);
        new com.mediatek.common.jpe.a().a();
        this.d = new ArrayMap();
        this.a = SystemProperties.get("persist.sys.apm.debug_mode").equals(LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON);
    }

    public static FocusRelationshipChainPolicy getInstance() {
        if (c == null) {
            c = new FocusRelationshipChainPolicy();
        }
        return c;
    }

    /* JADX WARNING: Missing block: B:8:0x0023, code:
            return;
     */
    /* JADX WARNING: Missing block: B:12:0x0029, code:
            return;
     */
    /* JADX WARNING: Missing block: B:21:0x003c, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void startFrc(String str, int i, List<String> list) {
        synchronized (this) {
            if (str != null) {
                if (!a(str)) {
                    a aVar = new a();
                    aVar.e = i;
                    aVar.f = list;
                    aVar.g = new HashSet();
                    this.d.put(str, aVar);
                    if (this.a) {
                        Slog.v("FocusRelationshipChain", "startFrc(" + str + ", " + i + ")");
                    }
                } else if (this.a) {
                    Slog.w("FocusRelationshipChain", "The tag:" + str + " exist");
                }
            } else if (this.a) {
                Slog.w("FocusRelationshipChain", "The tag is null");
            }
        }
    }

    /* JADX WARNING: Missing block: B:8:0x0013, code:
            return;
     */
    /* JADX WARNING: Missing block: B:12:0x0019, code:
            return;
     */
    /* JADX WARNING: Missing block: B:21:0x002c, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void stopFrc(String str) {
        synchronized (this) {
            if (str != null) {
                if (a(str)) {
                    this.d.remove(str);
                    if (this.a) {
                        Slog.v("FocusRelationshipChain", "stopFrc(" + str + ")");
                    }
                } else if (this.a) {
                    Slog.w("FocusRelationshipChain", "The tag:" + str + " does not exist");
                }
            } else if (this.a) {
                Slog.w("FocusRelationshipChain", "The tag is null");
            }
        }
    }

    /* JADX WARNING: Missing block: B:12:0x001f, code:
            return false;
     */
    /* JADX WARNING: Missing block: B:21:0x0032, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean isPackageInFrc(String str, String str2) {
        synchronized (this) {
            if (str != null) {
                if (a(str)) {
                    boolean contains = ((a) this.d.get(str)).g.contains(str2);
                    return contains;
                } else if (this.a) {
                    Slog.w("FocusRelationshipChain", "The tag:" + str + " does not exist");
                }
            } else if (this.a) {
                Slog.w("FocusRelationshipChain", "The tag is null");
            }
        }
    }

    /* JADX WARNING: Missing block: B:12:0x0020, code:
            return null;
     */
    /* JADX WARNING: Missing block: B:21:0x0033, code:
            return null;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public ArrayList<String> getFrcPackageList(String str) {
        synchronized (this) {
            if (str != null) {
                if (a(str)) {
                    ArrayList<String> arrayList = new ArrayList(((a) this.d.get(str)).g);
                    return arrayList;
                } else if (this.a) {
                    Slog.w("FocusRelationshipChain", "The tag:" + str + " does not exist");
                }
            } else if (this.a) {
                Slog.w("FocusRelationshipChain", "The tag is null");
            }
        }
    }

    /* JADX WARNING: Missing block: B:11:0x001a, code:
            return;
     */
    /* JADX WARNING: Missing block: B:20:0x002d, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void updateFrcExtraAllowList(String str, List<String> list) {
        synchronized (this) {
            if (str != null) {
                if (a(str)) {
                    ((a) this.d.get(str)).f = list;
                } else if (this.a) {
                    Slog.w("FocusRelationshipChain", "The tag:" + str + " does not exist");
                }
            } else if (this.a) {
                Slog.w("FocusRelationshipChain", "The tag is null");
            }
        }
    }

    /* JADX WARNING: Missing block: B:12:0x001b, code:
            return 0;
     */
    /* JADX WARNING: Missing block: B:21:0x002e, code:
            return 0;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int getUserAddPolicy(String str) {
        synchronized (this) {
            if (str != null) {
                if (a(str)) {
                    int i = ((a) this.d.get(str)).e;
                    return i;
                } else if (this.a) {
                    Slog.w("FocusRelationshipChain", "The tag:" + str + " does not exist");
                }
            } else if (this.a) {
                Slog.w("FocusRelationshipChain", "The tag is null");
            }
        }
    }

    public void onStartActivity(BeforeActivitySwitch beforeActivitySwitch) {
        String string = beforeActivitySwitch.getString(Index.nextResumedPackageName);
        ArrayList arrayList = (ArrayList) beforeActivitySwitch.get(Index.nextTaskPackageList);
        if (!beforeActivitySwitch.getBoolean(Index.isNeedToPauseActivityFirst)) {
            a(string, arrayList);
        }
    }

    public void onStartService(ReadyToStartService readyToStartService) {
        a(readyToStartService.getString(ReadyToStartService.Index.packageName), readyToStartService.getString(ReadyToStartService.Index.callerPackageName), readyToStartService.getInt(ReadyToStartService.Index.callerUid), (ArrayList) readyToStartService.get(ReadyToStartService.Index.bindCallerPkgList), (ArrayList) readyToStartService.get(ReadyToStartService.Index.bindCallerUidList), (ArrayList) readyToStartService.get(ReadyToStartService.Index.delayedCallerPkgList), (ArrayList) readyToStartService.get(ReadyToStartService.Index.delayedCallerUidList), readyToStartService.getString(ReadyToStartService.Index.reason));
    }

    public void onStartDynamicReceiver(ReadyToStartDynamicReceiver readyToStartDynamicReceiver) {
        a(readyToStartDynamicReceiver.getString(ReadyToStartDynamicReceiver.Index.packageName), readyToStartDynamicReceiver.getString(ReadyToStartDynamicReceiver.Index.callerPackageName), readyToStartDynamicReceiver.getInt(ReadyToStartDynamicReceiver.Index.callerUid));
    }

    public void onStartStaticReceiver(ReadyToStartStaticReceiver readyToStartStaticReceiver) {
        a(readyToStartStaticReceiver.getString(ReadyToStartStaticReceiver.Index.packageName), readyToStartStaticReceiver.getString(ReadyToStartStaticReceiver.Index.callerPackageName), readyToStartStaticReceiver.getInt(ReadyToStartStaticReceiver.Index.callerUid));
    }

    public void onStartProvider(ReadyToGetProvider readyToGetProvider) {
        a(readyToGetProvider.getString(ReadyToGetProvider.Index.packageName), (ArrayList) readyToGetProvider.get(ReadyToGetProvider.Index.callerPackageNameList), readyToGetProvider.getInt(ReadyToGetProvider.Index.callerUid));
    }

    private void a(String str, ArrayList<String> arrayList) {
        synchronized (this) {
            for (Entry entry : this.d.entrySet()) {
                a aVar = (a) entry.getValue();
                aVar.g.add(str);
                if (this.a) {
                    Slog.v("FocusRelationshipChain", "add " + str + " to " + ((String) entry.getKey()) + " by activity");
                }
                if (arrayList != null) {
                    for (int i = 0; i < arrayList.size(); i++) {
                        String str2 = (String) arrayList.get(i);
                        aVar.g.add(str2);
                        if (this.a) {
                            Slog.v("FocusRelationshipChain", "add " + str2 + " to " + ((String) entry.getKey()) + " by task of activity");
                        }
                    }
                    continue;
                }
            }
        }
    }

    private void a(String str, String str2, int i, ArrayList<String> arrayList, ArrayList<Integer> arrayList2, ArrayList<String> arrayList3, ArrayList<Integer> arrayList4, String str3) {
        synchronized (this) {
            for (Entry entry : this.d.entrySet()) {
                a aVar = (a) entry.getValue();
                if (!aVar.g.contains(str)) {
                    if (!str3.equalsIgnoreCase("bind service")) {
                        if (!str3.equalsIgnoreCase("start service")) {
                            int i2;
                            int i3;
                            Object obj;
                            int i4;
                            String str4;
                            if (str3.equalsIgnoreCase("restart service")) {
                                i2 = 0;
                                while (true) {
                                    i3 = i2;
                                    if (i3 >= arrayList.size()) {
                                        obj = null;
                                        break;
                                    } else if (a(aVar, (String) arrayList.get(i3), ((Integer) arrayList2.get(i3)).intValue())) {
                                        aVar.g.add(str);
                                        if (this.a) {
                                            Slog.v("FocusRelationshipChain", "add " + str + " to " + ((String) entry.getKey()) + " by restart service");
                                        }
                                        obj = 1;
                                    } else {
                                        i2 = i3 + 1;
                                    }
                                }
                                if (this.a && obj == null) {
                                    Slog.v("FocusRelationshipChain", str + " is not added by restart service");
                                    i4 = 0;
                                    while (true) {
                                        i2 = i4;
                                        if (i2 >= arrayList.size()) {
                                            break;
                                        }
                                        str4 = (String) arrayList.get(i2);
                                        Slog.v("FocusRelationshipChain", "caller is " + str4 + ", " + ((Integer) arrayList2.get(i2)).intValue());
                                        i4 = i2 + 1;
                                    }
                                }
                            } else if (str3.equalsIgnoreCase("delayed service")) {
                                i2 = 0;
                                while (true) {
                                    i3 = i2;
                                    if (i3 >= arrayList3.size()) {
                                        obj = null;
                                        break;
                                    } else if (a(aVar, (String) arrayList3.get(i3), ((Integer) arrayList4.get(i3)).intValue())) {
                                        aVar.g.add(str);
                                        if (this.a) {
                                            Slog.v("FocusRelationshipChain", "add " + str + " to " + ((String) entry.getKey()) + " by delay start service");
                                        }
                                        obj = 1;
                                    } else {
                                        i2 = i3 + 1;
                                    }
                                }
                                if (this.a && r0 == null) {
                                    Slog.v("FocusRelationshipChain", str + " is not added by delay start service");
                                    i4 = 0;
                                    while (true) {
                                        i2 = i4;
                                        if (i2 >= arrayList3.size()) {
                                            break;
                                        }
                                        str4 = (String) arrayList3.get(i2);
                                        Slog.v("FocusRelationshipChain", "caller is " + str4 + ", " + ((Integer) arrayList4.get(i2)).intValue());
                                        i4 = i2 + 1;
                                    }
                                }
                            }
                        }
                    }
                    if (a(aVar, str2, i)) {
                        aVar.g.add(str);
                        if (this.a) {
                            Slog.v("FocusRelationshipChain", "add " + str + " to " + ((String) entry.getKey()) + " by start/bind service");
                        }
                    } else if (this.a) {
                        Slog.v("FocusRelationshipChain", str + " is not added by start/bind service,  caller is " + str2 + ", " + i);
                    }
                }
            }
        }
    }

    private void a(String str, String str2, int i) {
        synchronized (this) {
            for (Entry entry : this.d.entrySet()) {
                a aVar = (a) entry.getValue();
                if (!aVar.g.contains(str)) {
                    if (a(aVar, str2, i)) {
                        aVar.g.add(str);
                        if (this.a) {
                            Slog.v("FocusRelationshipChain", "add " + str + " to " + ((String) entry.getKey()) + " by receiver");
                        }
                    } else if (this.a) {
                        Slog.v("FocusRelationshipChain", str + " is not added by receiver,  caller is " + str2 + ", " + i);
                    }
                }
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:28:0x007b A:{LOOP_END, LOOP:2: B:26:0x0075->B:28:0x007b} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void a(String str, ArrayList<String> arrayList, int i) {
        synchronized (this) {
            for (Entry entry : this.d.entrySet()) {
                a aVar = (a) entry.getValue();
                if (!aVar.g.contains(str)) {
                    if (arrayList != null) {
                        Object obj;
                        int i2;
                        int i3 = 0;
                        while (i3 < arrayList.size()) {
                            if (a(aVar, (String) arrayList.get(i3), i)) {
                                aVar.g.add(str);
                                if (this.a) {
                                    Slog.v("FocusRelationshipChain", "add " + str + " to " + ((String) entry.getKey()) + " by provider");
                                }
                                obj = 1;
                                if (this.a && obj == null) {
                                    Slog.v("FocusRelationshipChain", str + " is not added by provider");
                                    for (i2 = 0; i2 < arrayList.size(); i2++) {
                                        Slog.v("FocusRelationshipChain", "caller is " + ((String) arrayList.get(i2)) + ", " + i);
                                    }
                                }
                            } else {
                                i3++;
                            }
                        }
                        obj = null;
                        Slog.v("FocusRelationshipChain", str + " is not added by provider");
                        while (i2 < arrayList.size()) {
                        }
                    } else if (this.a) {
                        Slog.v("FocusRelationshipChain", str + " is not added by provider, caller is null");
                    }
                }
            }
        }
    }

    private boolean a(a aVar, String str, int i) {
        if (aVar.g.contains(str)) {
            if (!this.b || this.a) {
                Slog.v("FocusRelationshipChain", "AddedByPolicy from 8");
            }
            return true;
        } else if ((aVar.e & 1) != 0 && aVar.f != null && aVar.f.contains(str)) {
            if (!this.b || this.a) {
                Slog.v("FocusRelationshipChain", "AddedByPolicy from 1");
            }
            return true;
        } else if ((aVar.e & 2) != 0 && i == 1000) {
            if (!this.b || this.a) {
                Slog.v("FocusRelationshipChain", "AddedByPolicy from 2");
            }
            return true;
        } else if ((aVar.e & 4) == 0) {
            return false;
        } else {
            if (!this.b || this.a) {
                Slog.v("FocusRelationshipChain", "AddedByPolicy from 4");
            }
            return true;
        }
    }

    private boolean a(String str) {
        return this.d.containsKey(str);
    }
}
