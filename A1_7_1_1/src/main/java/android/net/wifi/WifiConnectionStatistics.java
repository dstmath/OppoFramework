package android.net.wifi;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.text.TextUtils;
import java.util.HashMap;

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
public class WifiConnectionStatistics implements Parcelable {
    public static final Creator<WifiConnectionStatistics> CREATOR = null;
    private static final String TAG = "WifiConnnectionStatistics";
    public int num24GhzConnected;
    public int num5GhzConnected;
    public int numAutoJoinAttempt;
    public int numAutoRoamAttempt;
    public int numWifiManagerJoinAttempt;
    public HashMap<String, WifiNetworkConnectionStatistics> untrustedNetworkHistory;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.net.wifi.WifiConnectionStatistics.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.net.wifi.WifiConnectionStatistics.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.net.wifi.WifiConnectionStatistics.<clinit>():void");
    }

    public WifiConnectionStatistics() {
        this.untrustedNetworkHistory = new HashMap();
    }

    public void incrementOrAddUntrusted(String SSID, int connection, int usage) {
        if (!TextUtils.isEmpty(SSID)) {
            WifiNetworkConnectionStatistics stats;
            if (this.untrustedNetworkHistory.containsKey(SSID)) {
                stats = (WifiNetworkConnectionStatistics) this.untrustedNetworkHistory.get(SSID);
                if (stats != null) {
                    stats.numConnection += connection;
                    stats.numUsage += usage;
                }
            } else {
                stats = new WifiNetworkConnectionStatistics(connection, usage);
            }
            if (stats != null) {
                this.untrustedNetworkHistory.put(SSID, stats);
            }
        }
    }

    public String toString() {
        StringBuilder sbuf = new StringBuilder();
        sbuf.append("Connected on: 2.4Ghz=").append(this.num24GhzConnected);
        sbuf.append(" 5Ghz=").append(this.num5GhzConnected).append("\n");
        sbuf.append(" join=").append(this.numWifiManagerJoinAttempt);
        sbuf.append("\\").append(this.numAutoJoinAttempt).append("\n");
        sbuf.append(" roam=").append(this.numAutoRoamAttempt).append("\n");
        for (String Key : this.untrustedNetworkHistory.keySet()) {
            WifiNetworkConnectionStatistics stats = (WifiNetworkConnectionStatistics) this.untrustedNetworkHistory.get(Key);
            if (stats != null) {
                sbuf.append(Key).append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER).append(stats.toString()).append("\n");
            }
        }
        return sbuf.toString();
    }

    public WifiConnectionStatistics(WifiConnectionStatistics source) {
        this.untrustedNetworkHistory = new HashMap();
        if (source != null) {
            this.untrustedNetworkHistory.putAll(source.untrustedNetworkHistory);
        }
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.num24GhzConnected);
        dest.writeInt(this.num5GhzConnected);
        dest.writeInt(this.numAutoJoinAttempt);
        dest.writeInt(this.numAutoRoamAttempt);
        dest.writeInt(this.numWifiManagerJoinAttempt);
        dest.writeInt(this.untrustedNetworkHistory.size());
        for (String Key : this.untrustedNetworkHistory.keySet()) {
            WifiNetworkConnectionStatistics num = (WifiNetworkConnectionStatistics) this.untrustedNetworkHistory.get(Key);
            dest.writeString(Key);
            dest.writeInt(num.numConnection);
            dest.writeInt(num.numUsage);
        }
    }
}
