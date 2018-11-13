package com.mediatek.apm.suppression;

import android.os.Build;
import android.os.SystemProperties;
import android.util.ArrayMap;
import android.util.Slog;
import com.android.server.LocationManagerService;
import com.mediatek.apm.frc.FocusRelationshipChainPolicy;
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
public class SuppressionPolicy {
    public static final int SUPPRESSION_POLICY_ALL = 0;
    public static final int SUPPRESSION_POLICY_EXCLUDE_EXTRA_ALLOW_LIST = 2;
    public static final int SUPPRESSION_POLICY_EXCLUDE_FOCUS_RELATIONSHIP_CHAIN = 1;
    public static final int SUPPRESSION_POLICY_EXCLUDE_SYSTEM_CALLER = 4;
    private static String TAG;
    private static SuppressionPolicy s;
    private boolean n;
    private boolean o;
    private ArrayMap<String, a> t;
    private FocusRelationshipChainPolicy u;

    class a {
        int v;
        int w;
        String x;
        List<String> y;

        a() {
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.mediatek.apm.suppression.SuppressionPolicy.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.mediatek.apm.suppression.SuppressionPolicy.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.apm.suppression.SuppressionPolicy.<clinit>():void");
    }

    private SuppressionPolicy() {
        this.n = false;
        this.o = "user".equals(Build.TYPE);
        new com.mediatek.common.jpe.a().a();
        if (this.t == null) {
            this.t = new ArrayMap();
        }
        this.u = FocusRelationshipChainPolicy.getInstance();
        this.n = SystemProperties.get("persist.sys.apm.debug_mode").equals(LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON);
    }

    public static SuppressionPolicy getInstance() {
        if (s == null) {
            s = new SuppressionPolicy();
        }
        return s;
    }

    public void startSuppression(String str, int i, int i2, String str2, List<String> list) {
        synchronized (this) {
            if (str == null) {
                Slog.w(TAG, "The tag is null");
            } else if (e(str)) {
                Slog.w(TAG, "The tag:" + str + " exist");
            } else {
                a aVar = new a();
                aVar.v = i;
                aVar.w = i2;
                aVar.x = str2;
                aVar.y = list;
                this.t.put(str, aVar);
                Slog.v(TAG, "startSuppression(" + str + ", " + i + ", " + str2 + ")");
            }
        }
    }

    public void stopSuppression(String str) {
        synchronized (this) {
            if (str == null) {
                Slog.w(TAG, "The tag is null");
            } else if (e(str)) {
                this.t.remove(str);
                Slog.v(TAG, "stopSuppression(" + str + ")");
            } else {
                Slog.w(TAG, "The tag:" + str + " does not exist");
            }
        }
    }

    public void updateExtraAllowList(String str, List<String> list) {
        synchronized (this) {
            if (str == null) {
                Slog.w(TAG, "The tag is null");
            } else if (e(str)) {
                ((a) this.t.get(str)).y = list;
            } else {
                Slog.w(TAG, "The tag:" + str + " does not exist");
            }
        }
    }

    /* JADX WARNING: Missing block: B:18:0x002b, code:
            return true;
     */
    /* JADX WARNING: Missing block: B:32:0x006b, code:
            return false;
     */
    /* JADX WARNING: Missing block: B:46:0x00a9, code:
            return false;
     */
    /* JADX WARNING: Missing block: B:56:0x00e0, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean isPackageInSuppression(String str, String str2, int i) {
        synchronized (this) {
            if (str == null) {
                Slog.w(TAG, "The tag is null");
                return false;
            } else if (e(str)) {
                boolean z;
                if (i != 9999) {
                    z = false;
                } else {
                    z = true;
                }
                a aVar = (a) this.t.get(str);
                int i2 = aVar.v;
                if ((i2 & 1) == 0 || !this.u.isPackageInFrc(aVar.x, str2)) {
                    if ((i2 & 2) != 0) {
                        if (aVar.y.contains(str2)) {
                            if ((!this.o || this.n) && !z) {
                                Slog.v(TAG, "Caller " + str2 + " is not suppressed by " + 2);
                            }
                        }
                    }
                    if ((i2 & 4) != 0 && i == 1000) {
                        if ((!this.o || this.n) && !z) {
                            Slog.v(TAG, "Caller " + str2 + " is not suppressed by " + 4);
                        }
                    } else if (this.n && !z) {
                        Slog.v(TAG, "Caller " + str2 + "(" + i + ") is suppressed");
                    }
                } else if ((!this.o || this.n) && !z) {
                    Slog.v(TAG, "Caller " + str2 + " is not suppressed by " + 1);
                }
            } else {
                Slog.w(TAG, "The tag:" + str + " does not exist");
                return false;
            }
        }
    }

    public ArrayList<String> getSuppressionList() {
        synchronized (this) {
            if (this.t.size() != 0) {
                ArrayList<String> arrayList = new ArrayList(this.t.keySet());
                return arrayList;
            }
            return null;
        }
    }

    int d(String str) {
        synchronized (this) {
            if (str == null) {
                Slog.w(TAG, "The tag is null");
                return -1;
            } else if (e(str)) {
                int i = ((a) this.t.get(str)).w;
                return i;
            } else {
                Slog.w(TAG, "The tag:" + str + " does not exist");
                return -1;
            }
        }
    }

    private boolean e(String str) {
        return this.t.containsKey(str);
    }
}
