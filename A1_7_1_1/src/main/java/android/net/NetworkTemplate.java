package android.net;

import android.net.wifi.WifiInfo;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.telephony.TelephonyManager;
import android.util.BackupUtils;
import android.util.BackupUtils.BadVersionException;
import com.android.internal.util.ArrayUtils;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;
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
public class NetworkTemplate implements Parcelable {
    private static final int BACKUP_VERSION = 1;
    public static final Creator<NetworkTemplate> CREATOR = null;
    public static final int MATCH_BLUETOOTH = 8;
    public static final int MATCH_ETHERNET = 5;
    @Deprecated
    public static final int MATCH_MOBILE_3G_LOWER = 2;
    @Deprecated
    public static final int MATCH_MOBILE_4G = 3;
    public static final int MATCH_MOBILE_ALL = 1;
    public static final int MATCH_MOBILE_WILDCARD = 6;
    public static final int MATCH_PROXY = 9;
    public static final int MATCH_WIFI = 4;
    public static final int MATCH_WIFI_WILDCARD = 7;
    private static boolean sForceAllNetworkTypes;
    private final int mMatchRule;
    private final String[] mMatchSubscriberIds;
    private final String mNetworkId;
    private final String mSubscriberId;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.net.NetworkTemplate.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.net.NetworkTemplate.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.net.NetworkTemplate.<clinit>():void");
    }

    /* synthetic */ NetworkTemplate(Parcel in, NetworkTemplate networkTemplate) {
        this(in);
    }

    public static void forceAllNetworkTypes() {
        sForceAllNetworkTypes = true;
    }

    public static NetworkTemplate buildTemplateMobileAll(String subscriberId) {
        return new NetworkTemplate(1, subscriberId, null);
    }

    @Deprecated
    public static NetworkTemplate buildTemplateMobile3gLower(String subscriberId) {
        return new NetworkTemplate(2, subscriberId, null);
    }

    @Deprecated
    public static NetworkTemplate buildTemplateMobile4g(String subscriberId) {
        return new NetworkTemplate(3, subscriberId, null);
    }

    public static NetworkTemplate buildTemplateMobileWildcard() {
        return new NetworkTemplate(6, null, null);
    }

    public static NetworkTemplate buildTemplateWifiWildcard() {
        return new NetworkTemplate(7, null, null);
    }

    @Deprecated
    public static NetworkTemplate buildTemplateWifi() {
        return buildTemplateWifiWildcard();
    }

    public static NetworkTemplate buildTemplateWifi(String networkId) {
        return new NetworkTemplate(4, null, networkId);
    }

    public static NetworkTemplate buildTemplateEthernet() {
        return new NetworkTemplate(5, null, null);
    }

    public static NetworkTemplate buildTemplateBluetooth() {
        return new NetworkTemplate(8, null, null);
    }

    public static NetworkTemplate buildTemplateProxy() {
        return new NetworkTemplate(9, null, null);
    }

    public NetworkTemplate(int matchRule, String subscriberId, String networkId) {
        String[] strArr = new String[1];
        strArr[0] = subscriberId;
        this(matchRule, subscriberId, strArr, networkId);
    }

    public NetworkTemplate(int matchRule, String subscriberId, String[] matchSubscriberIds, String networkId) {
        this.mMatchRule = matchRule;
        this.mSubscriberId = subscriberId;
        this.mMatchSubscriberIds = matchSubscriberIds;
        this.mNetworkId = networkId;
    }

    private NetworkTemplate(Parcel in) {
        this.mMatchRule = in.readInt();
        this.mSubscriberId = in.readString();
        this.mMatchSubscriberIds = in.createStringArray();
        this.mNetworkId = in.readString();
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mMatchRule);
        dest.writeString(this.mSubscriberId);
        dest.writeStringArray(this.mMatchSubscriberIds);
        dest.writeString(this.mNetworkId);
    }

    public int describeContents() {
        return 0;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder("nt");
        builder.append(":").append("").append(this.mMatchRule);
        if (this.mSubscriberId != null) {
            builder.append(",").append(NetworkIdentity.scrubSubscriberId(this.mSubscriberId));
        }
        if (this.mMatchSubscriberIds != null) {
            builder.append(",").append(Arrays.toString(NetworkIdentity.scrubSubscriberId(this.mMatchSubscriberIds)));
        }
        if (this.mNetworkId != null) {
            builder.append(",").append(this.mNetworkId);
        }
        return builder.toString();
    }

    public int hashCode() {
        Object[] objArr = new Object[3];
        objArr[0] = Integer.valueOf(this.mMatchRule);
        objArr[1] = this.mSubscriberId;
        objArr[2] = this.mNetworkId;
        return Objects.hash(objArr);
    }

    public boolean equals(Object obj) {
        boolean z = false;
        if (!(obj instanceof NetworkTemplate)) {
            return false;
        }
        NetworkTemplate other = (NetworkTemplate) obj;
        if (this.mMatchRule == other.mMatchRule && Objects.equals(this.mSubscriberId, other.mSubscriberId)) {
            z = Objects.equals(this.mNetworkId, other.mNetworkId);
        }
        return z;
    }

    public boolean isMatchRuleMobile() {
        switch (this.mMatchRule) {
            case 1:
            case 2:
            case 3:
            case 6:
                return true;
            default:
                return false;
        }
    }

    public boolean isPersistable() {
        switch (this.mMatchRule) {
            case 6:
            case 7:
                return false;
            default:
                return true;
        }
    }

    public int getMatchRule() {
        return this.mMatchRule;
    }

    public String getSubscriberId() {
        return this.mSubscriberId;
    }

    public String getNetworkId() {
        return this.mNetworkId;
    }

    public boolean matches(NetworkIdentity ident) {
        switch (this.mMatchRule) {
            case 1:
                return matchesMobile(ident);
            case 2:
                return matchesMobile3gLower(ident);
            case 3:
                return matchesMobile4g(ident);
            case 4:
                return matchesWifi(ident);
            case 5:
                return matchesEthernet(ident);
            case 6:
                return matchesMobileWildcard(ident);
            case 7:
                return matchesWifiWildcard(ident);
            case 8:
                return matchesBluetooth(ident);
            case 9:
                return matchesProxy(ident);
            default:
                throw new IllegalArgumentException("unknown network template");
        }
    }

    private boolean matchesMobile(NetworkIdentity ident) {
        boolean z = false;
        if (ident.mType == 6) {
            return true;
        }
        if ((sForceAllNetworkTypes || (ident.mType == 0 && ident.mMetered)) && !ArrayUtils.isEmpty(this.mMatchSubscriberIds)) {
            z = ArrayUtils.contains(this.mMatchSubscriberIds, ident.mSubscriberId);
        }
        return z;
    }

    @Deprecated
    private boolean matchesMobile3gLower(NetworkIdentity ident) {
        ensureSubtypeAvailable();
        if (ident.mType != 6 && matchesMobile(ident)) {
            switch (TelephonyManager.getNetworkClass(ident.mSubType)) {
                case 0:
                case 1:
                case 2:
                    return true;
            }
        }
        return false;
    }

    @Deprecated
    private boolean matchesMobile4g(NetworkIdentity ident) {
        ensureSubtypeAvailable();
        if (ident.mType == 6) {
            return true;
        }
        if (matchesMobile(ident)) {
            switch (TelephonyManager.getNetworkClass(ident.mSubType)) {
                case 3:
                    return true;
            }
        }
        return false;
    }

    private boolean matchesWifi(NetworkIdentity ident) {
        switch (ident.mType) {
            case 1:
                return Objects.equals(WifiInfo.removeDoubleQuotes(this.mNetworkId), WifiInfo.removeDoubleQuotes(ident.mNetworkId));
            default:
                return false;
        }
    }

    private boolean matchesEthernet(NetworkIdentity ident) {
        if (ident.mType == 9) {
            return true;
        }
        return false;
    }

    private boolean matchesMobileWildcard(NetworkIdentity ident) {
        boolean z = true;
        if (ident.mType == 6) {
            return true;
        }
        if (!sForceAllNetworkTypes) {
            z = ident.mType == 0 ? ident.mMetered : false;
        }
        return z;
    }

    private boolean matchesWifiWildcard(NetworkIdentity ident) {
        switch (ident.mType) {
            case 1:
            case 13:
                return true;
            default:
                return false;
        }
    }

    private boolean matchesBluetooth(NetworkIdentity ident) {
        if (ident.mType == 7) {
            return true;
        }
        return false;
    }

    private boolean matchesProxy(NetworkIdentity ident) {
        return ident.mType == 16;
    }

    private static String getMatchRuleName(int matchRule) {
        switch (matchRule) {
            case 1:
                return "MOBILE_ALL";
            case 2:
                return "MOBILE_3G_LOWER";
            case 3:
                return "MOBILE_4G";
            case 4:
                return "WIFI";
            case 5:
                return "ETHERNET";
            case 6:
                return "MOBILE_WILDCARD";
            case 7:
                return "WIFI_WILDCARD";
            case 8:
                return "BLUETOOTH";
            case 9:
                return "PROXY";
            default:
                return "UNKNOWN";
        }
    }

    private static void ensureSubtypeAvailable() {
        throw new IllegalArgumentException("Unable to enforce 3G_LOWER template on combined data.");
    }

    public static NetworkTemplate normalize(NetworkTemplate template, String[] merged) {
        if (template.isMatchRuleMobile() && ArrayUtils.contains(merged, template.mSubscriberId)) {
            return new NetworkTemplate(template.mMatchRule, merged[0], merged, template.mNetworkId);
        }
        return template;
    }

    public byte[] getBytesForBackup() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(baos);
        out.writeInt(1);
        out.writeInt(this.mMatchRule);
        BackupUtils.writeString(out, this.mSubscriberId);
        BackupUtils.writeString(out, this.mNetworkId);
        return baos.toByteArray();
    }

    public static NetworkTemplate getNetworkTemplateFromBackup(DataInputStream in) throws IOException, BadVersionException {
        int version = in.readInt();
        if (version >= 1 && version <= 1) {
            return new NetworkTemplate(in.readInt(), BackupUtils.readString(in), BackupUtils.readString(in));
        }
        throw new BadVersionException("Unknown Backup Serialization Version");
    }
}
