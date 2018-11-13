package android.net;

import android.content.Context;
import android.net.NetworkInfo.State;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.util.Slog;
import java.util.Objects;

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
public class NetworkIdentity implements Comparable<NetworkIdentity> {
    @Deprecated
    public static final boolean COMBINE_SUBTYPE_ENABLED = true;
    private static final String PROP_FORCE_DEBUG_KEY = "persist.log.tag.tel_dbg";
    public static final int SUBTYPE_COMBINED = -1;
    private static final String TAG = "NetworkIdentity";
    private static final boolean VDBG = false;
    final boolean mMetered;
    final String mNetworkId;
    final boolean mRoaming;
    final int mSubType;
    final String mSubscriberId;
    final int mType;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.net.NetworkIdentity.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.net.NetworkIdentity.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.net.NetworkIdentity.<clinit>():void");
    }

    public NetworkIdentity(int type, int subType, String subscriberId, String networkId, boolean roaming, boolean metered) {
        this.mType = type;
        this.mSubType = -1;
        this.mSubscriberId = subscriberId;
        this.mNetworkId = networkId;
        this.mRoaming = roaming;
        this.mMetered = metered;
    }

    public int hashCode() {
        Object[] objArr = new Object[6];
        objArr[0] = Integer.valueOf(this.mType);
        objArr[1] = Integer.valueOf(this.mSubType);
        objArr[2] = this.mSubscriberId;
        objArr[3] = this.mNetworkId;
        objArr[4] = Boolean.valueOf(this.mRoaming);
        objArr[5] = Boolean.valueOf(this.mMetered);
        return Objects.hash(objArr);
    }

    public boolean equals(Object obj) {
        boolean z = false;
        if (!(obj instanceof NetworkIdentity)) {
            return false;
        }
        NetworkIdentity ident = (NetworkIdentity) obj;
        if (this.mType == ident.mType && this.mSubType == ident.mSubType && this.mRoaming == ident.mRoaming && Objects.equals(this.mSubscriberId, ident.mSubscriberId) && Objects.equals(this.mNetworkId, ident.mNetworkId) && this.mMetered == ident.mMetered) {
            z = true;
        }
        return z;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder("{");
        builder.append("type=").append(ConnectivityManager.getNetworkTypeName(this.mType));
        builder.append(", subType=");
        builder.append("COMBINED");
        if (this.mSubscriberId != null) {
            builder.append(", subscriberId=").append(scrubSubscriberId(this.mSubscriberId));
        }
        if (this.mNetworkId != null) {
            builder.append(", networkId=").append(this.mNetworkId);
        }
        if (this.mRoaming) {
            builder.append(", ROAMING");
        }
        builder.append(", metered=").append(this.mMetered);
        return builder.append("}").toString();
    }

    public int getType() {
        return this.mType;
    }

    public int getSubType() {
        return this.mSubType;
    }

    public String getSubscriberId() {
        return this.mSubscriberId;
    }

    public String getNetworkId() {
        return this.mNetworkId;
    }

    public boolean getRoaming() {
        return this.mRoaming;
    }

    public boolean getMetered() {
        return this.mMetered;
    }

    public static String scrubSubscriberId(String subscriberId) {
        if ("eng".equals(Build.TYPE)) {
            return subscriberId;
        }
        if (subscriberId != null) {
            return subscriberId.substring(0, Math.min(6, subscriberId.length())) + "...";
        }
        return "null";
    }

    public static String[] scrubSubscriberId(String[] subscriberId) {
        if (subscriberId == null) {
            return null;
        }
        String[] res = new String[subscriberId.length];
        for (int i = 0; i < res.length; i++) {
            res[i] = scrubSubscriberId(subscriberId[i]);
        }
        return res;
    }

    public static NetworkIdentity buildNetworkIdentity(Context context, NetworkState state) {
        int type = state.networkInfo.getType();
        int subType = state.networkInfo.getSubtype();
        String subscriberId = null;
        String networkId = null;
        boolean roaming = false;
        boolean metered = false;
        if (VDBG) {
            Slog.i(TAG, "buildNetworkIdentity:");
        }
        if (ConnectivityManager.isNetworkTypeMobile(type)) {
            if (state.subscriberId == null && state.networkInfo.getState() != State.DISCONNECTED && state.networkInfo.getState() != State.UNKNOWN && VDBG) {
                Slog.w(TAG, "Active mobile network without subscriber! ni = " + state.networkInfo);
            }
            subscriberId = state.subscriberId;
            roaming = state.networkInfo.isRoaming();
            metered = !state.networkCapabilities.hasCapability(11);
        } else if (type == 1) {
            if (state.networkId != null) {
                networkId = state.networkId;
            } else {
                WifiInfo info = ((WifiManager) context.getSystemService("wifi")).getConnectionInfo();
                networkId = info != null ? WifiInfo.removeDoubleQuotes(info.getSSID()) : null;
                if (VDBG) {
                    Slog.i(TAG, "networkId = " + networkId);
                }
            }
        }
        return new NetworkIdentity(type, subType, subscriberId, networkId, roaming, metered);
    }

    public int compareTo(NetworkIdentity another) {
        int res = Integer.compare(this.mType, another.mType);
        if (res == 0) {
            res = Integer.compare(this.mSubType, another.mSubType);
        }
        if (!(res != 0 || this.mSubscriberId == null || another.mSubscriberId == null)) {
            res = this.mSubscriberId.compareTo(another.mSubscriberId);
        }
        if (!(res != 0 || this.mNetworkId == null || another.mNetworkId == null)) {
            res = this.mNetworkId.compareTo(another.mNetworkId);
        }
        if (res == 0) {
            res = Boolean.compare(this.mRoaming, another.mRoaming);
        }
        if (res == 0) {
            return Boolean.compare(this.mMetered, another.mMetered);
        }
        return res;
    }
}
