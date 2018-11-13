package android.net.wifi;

import android.net.NetworkInfo.DetailedState;
import android.net.NetworkUtils;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.text.TextUtils;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.EnumMap;
import java.util.Locale;

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
public class WifiInfo implements Parcelable {
    public static final Creator<WifiInfo> CREATOR = null;
    public static final String DEFAULT_MAC_ADDRESS = "02:00:00:00:00:00";
    public static final String FREQUENCY_UNITS = "MHz";
    public static final int INVALID_RSSI = -127;
    public static final String LINK_SPEED_UNITS = "Mbps";
    public static final int MAX_RSSI = 200;
    public static final int MIN_RSSI = -126;
    private static final String TAG = "WifiInfo";
    private static final EnumMap<SupplicantState, DetailedState> stateMap = null;
    public int badRssiCount;
    public int linkStuckCount;
    public int lowRssiCount;
    private String mBSSID;
    private boolean mEphemeral;
    private int mFrequency;
    private InetAddress mIpAddress;
    private int mLinkSpeed;
    private String mMacAddress;
    private boolean mMeteredHint;
    private int mNetworkId;
    private int mRssi;
    public int mSmoothRssi;
    private SupplicantState mSupplicantState;
    private WifiSsid mWifiSsid;
    private final Object mWifiSsidLock;
    public long rxSuccess;
    public double rxSuccessRate;
    public int score;
    public long txBad;
    public double txBadRate;
    public long txRetries;
    public double txRetriesRate;
    public long txSuccess;
    public double txSuccessRate;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.net.wifi.WifiInfo.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.net.wifi.WifiInfo.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.net.wifi.WifiInfo.<clinit>():void");
    }

    public void updatePacketRates(WifiLinkLayerStats stats) {
        if (stats != null) {
            long txgood = ((stats.txmpdu_be + stats.txmpdu_bk) + stats.txmpdu_vi) + stats.txmpdu_vo;
            long txretries = ((stats.retries_be + stats.retries_bk) + stats.retries_vi) + stats.retries_vo;
            long rxgood = ((stats.rxmpdu_be + stats.rxmpdu_bk) + stats.rxmpdu_vi) + stats.rxmpdu_vo;
            long txbad = ((stats.lostmpdu_be + stats.lostmpdu_bk) + stats.lostmpdu_vi) + stats.lostmpdu_vo;
            if (this.txBad > txbad || this.txSuccess > txgood || this.rxSuccess > rxgood || this.txRetries > txretries) {
                this.txBadRate = 0.0d;
                this.txSuccessRate = 0.0d;
                this.rxSuccessRate = 0.0d;
                this.txRetriesRate = 0.0d;
            } else {
                this.txBadRate = (this.txBadRate * 0.5d) + (((double) (txbad - this.txBad)) * 0.5d);
                this.txSuccessRate = (this.txSuccessRate * 0.5d) + (((double) (txgood - this.txSuccess)) * 0.5d);
                this.rxSuccessRate = (this.rxSuccessRate * 0.5d) + (((double) (rxgood - this.rxSuccess)) * 0.5d);
                this.txRetriesRate = (this.txRetriesRate * 0.5d) + (((double) (txretries - this.txRetries)) * 0.5d);
            }
            this.txBad = txbad;
            this.txSuccess = txgood;
            this.rxSuccess = rxgood;
            this.txRetries = txretries;
            return;
        }
        this.txBad = 0;
        this.txSuccess = 0;
        this.rxSuccess = 0;
        this.txRetries = 0;
        this.txBadRate = 0.0d;
        this.txSuccessRate = 0.0d;
        this.rxSuccessRate = 0.0d;
        this.txRetriesRate = 0.0d;
    }

    public void updatePacketRates(long txPackets, long rxPackets) {
        this.txBad = 0;
        this.txRetries = 0;
        this.txBadRate = 0.0d;
        this.txRetriesRate = 0.0d;
        if (this.txSuccess > txPackets || this.rxSuccess > rxPackets) {
            this.txBadRate = 0.0d;
            this.txRetriesRate = 0.0d;
        } else {
            this.txSuccessRate = (this.txSuccessRate * 0.5d) + (((double) (txPackets - this.txSuccess)) * 0.5d);
            this.rxSuccessRate = (this.rxSuccessRate * 0.5d) + (((double) (rxPackets - this.rxSuccess)) * 0.5d);
        }
        this.txSuccess = txPackets;
        this.rxSuccess = rxPackets;
    }

    public WifiInfo() {
        this.mMacAddress = "02:00:00:00:00:00";
        this.mWifiSsidLock = new Object();
        this.mSmoothRssi = 0;
        this.mWifiSsid = null;
        this.mBSSID = null;
        this.mNetworkId = -1;
        this.mSupplicantState = SupplicantState.UNINITIALIZED;
        this.mRssi = INVALID_RSSI;
        this.mLinkSpeed = -1;
        this.mFrequency = -1;
    }

    public void reset() {
        setInetAddress(null);
        setBSSID(null);
        setSSID(null);
        setNetworkId(-1);
        setRssi(INVALID_RSSI);
        setLinkSpeed(-1);
        setFrequency(-1);
        setMeteredHint(false);
        setEphemeral(false);
        this.txBad = 0;
        this.txSuccess = 0;
        this.rxSuccess = 0;
        this.txRetries = 0;
        this.txBadRate = 0.0d;
        this.txSuccessRate = 0.0d;
        this.rxSuccessRate = 0.0d;
        this.txRetriesRate = 0.0d;
        this.lowRssiCount = 0;
        this.badRssiCount = 0;
        this.linkStuckCount = 0;
        this.score = 0;
    }

    public WifiInfo(WifiInfo source) {
        this.mMacAddress = "02:00:00:00:00:00";
        this.mWifiSsidLock = new Object();
        this.mSmoothRssi = 0;
        if (source != null) {
            this.mSupplicantState = source.mSupplicantState;
            this.mBSSID = source.mBSSID;
            this.mWifiSsid = source.mWifiSsid;
            this.mNetworkId = source.mNetworkId;
            this.mRssi = source.mRssi;
            this.mLinkSpeed = source.mLinkSpeed;
            this.mFrequency = source.mFrequency;
            this.mIpAddress = source.mIpAddress;
            this.mMacAddress = source.mMacAddress;
            this.mMeteredHint = source.mMeteredHint;
            this.mEphemeral = source.mEphemeral;
            this.txBad = source.txBad;
            this.txRetries = source.txRetries;
            this.txSuccess = source.txSuccess;
            this.rxSuccess = source.rxSuccess;
            this.txBadRate = source.txBadRate;
            this.txRetriesRate = source.txRetriesRate;
            this.txSuccessRate = source.txSuccessRate;
            this.rxSuccessRate = source.rxSuccessRate;
            this.score = source.score;
            this.badRssiCount = source.badRssiCount;
            this.lowRssiCount = source.lowRssiCount;
            this.linkStuckCount = source.linkStuckCount;
        }
    }

    public void setSSID(WifiSsid wifiSsid) {
        synchronized (this.mWifiSsidLock) {
            this.mWifiSsid = wifiSsid;
        }
    }

    public String getSSID() {
        if (this.mWifiSsid == null) {
            return WifiSsid.NONE;
        }
        String unicode = this.mWifiSsid.toString();
        if (!TextUtils.isEmpty(unicode)) {
            return "\"" + unicode + "\"";
        }
        String hex = this.mWifiSsid.getHexString();
        if (hex == null) {
            hex = WifiSsid.NONE;
        }
        return hex;
    }

    public WifiSsid getWifiSsid() {
        return this.mWifiSsid;
    }

    public void setBSSID(String BSSID) {
        this.mBSSID = BSSID;
    }

    public String getBSSID() {
        return this.mBSSID;
    }

    public int getRssi() {
        return this.mRssi;
    }

    public void setRssi(int rssi) {
        if (rssi < INVALID_RSSI) {
            rssi = INVALID_RSSI;
        }
        if (rssi > 200) {
            rssi = 200;
        }
        this.mRssi = rssi;
    }

    public int getRssi(boolean useOriginalRssi) {
        if (useOriginalRssi) {
            return getRssi();
        }
        return this.mSmoothRssi;
    }

    public int getLinkSpeed() {
        return this.mLinkSpeed;
    }

    public void setLinkSpeed(int linkSpeed) {
        this.mLinkSpeed = linkSpeed;
    }

    public int getFrequency() {
        return this.mFrequency;
    }

    public void setFrequency(int frequency) {
        this.mFrequency = frequency;
    }

    public boolean is24GHz() {
        return ScanResult.is24GHz(this.mFrequency);
    }

    public boolean is5GHz() {
        return ScanResult.is5GHz(this.mFrequency);
    }

    public void setMacAddress(String macAddress) {
        this.mMacAddress = macAddress;
    }

    public String getMacAddress() {
        return this.mMacAddress;
    }

    public boolean hasRealMacAddress() {
        return (this.mMacAddress == null || "02:00:00:00:00:00".equals(this.mMacAddress)) ? false : true;
    }

    public void setMeteredHint(boolean meteredHint) {
        this.mMeteredHint = meteredHint;
    }

    public boolean getMeteredHint() {
        return this.mMeteredHint;
    }

    public void setEphemeral(boolean ephemeral) {
        this.mEphemeral = ephemeral;
    }

    public boolean isEphemeral() {
        return this.mEphemeral;
    }

    public void setNetworkId(int id) {
        this.mNetworkId = id;
    }

    public int getNetworkId() {
        return this.mNetworkId;
    }

    public SupplicantState getSupplicantState() {
        return this.mSupplicantState;
    }

    public void setSupplicantState(SupplicantState state) {
        this.mSupplicantState = state;
    }

    public void setInetAddress(InetAddress address) {
        this.mIpAddress = address;
    }

    public int getIpAddress() {
        if (this.mIpAddress instanceof Inet4Address) {
            return NetworkUtils.inetAddressToInt((Inet4Address) this.mIpAddress);
        }
        return 0;
    }

    public boolean getHiddenSSID() {
        if (this.mWifiSsid == null) {
            return false;
        }
        return this.mWifiSsid.isHidden();
    }

    public static DetailedState getDetailedStateOf(SupplicantState suppState) {
        return (DetailedState) stateMap.get(suppState);
    }

    void setSupplicantState(String stateName) {
        this.mSupplicantState = valueOf(stateName);
    }

    static SupplicantState valueOf(String stateName) {
        if ("4WAY_HANDSHAKE".equalsIgnoreCase(stateName)) {
            return SupplicantState.FOUR_WAY_HANDSHAKE;
        }
        try {
            return SupplicantState.valueOf(stateName.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return SupplicantState.INVALID;
        }
    }

    public static String removeDoubleQuotes(String string) {
        if (string == null) {
            return null;
        }
        int length = string.length();
        if (length > 1 && string.charAt(0) == '\"' && string.charAt(length - 1) == '\"') {
            return string.substring(1, length - 1);
        }
        return string;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        String none = "<none>";
        StringBuffer append = sb.append("SSID: ").append(this.mWifiSsid == null ? WifiSsid.NONE : this.mWifiSsid).append(", BSSID: ").append(this.mBSSID == null ? none : this.mBSSID).append(", MAC: ").append(this.mMacAddress == null ? none : this.mMacAddress).append(", Supplicant state: ");
        if (this.mSupplicantState != null) {
            none = this.mSupplicantState;
        }
        append.append(none).append(", RSSI: ").append(this.mRssi).append(", Link speed: ").append(this.mLinkSpeed).append(LINK_SPEED_UNITS).append(", Frequency: ").append(this.mFrequency).append(FREQUENCY_UNITS).append(", Net ID: ").append(this.mNetworkId).append(", Metered hint: ").append(this.mMeteredHint).append(", score: ").append(Integer.toString(this.score));
        return sb.toString();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        int i;
        int i2 = 1;
        dest.writeInt(this.mNetworkId);
        dest.writeInt(this.mRssi);
        dest.writeInt(this.mLinkSpeed);
        dest.writeInt(this.mFrequency);
        InetAddress ia = this.mIpAddress;
        byte[] bia = null;
        if (ia != null) {
            bia = ia.getAddress();
        }
        if (bia != null) {
            dest.writeByte((byte) 1);
            dest.writeByteArray(bia);
        } else {
            dest.writeByte((byte) 0);
        }
        synchronized (this.mWifiSsidLock) {
            if (this.mWifiSsid != null) {
                dest.writeInt(1);
                this.mWifiSsid.writeToParcel(dest, flags);
            } else {
                dest.writeInt(0);
            }
        }
        dest.writeString(this.mBSSID);
        dest.writeString(this.mMacAddress);
        if (this.mMeteredHint) {
            i = 1;
        } else {
            i = 0;
        }
        dest.writeInt(i);
        if (!this.mEphemeral) {
            i2 = 0;
        }
        dest.writeInt(i2);
        dest.writeInt(this.score);
        dest.writeDouble(this.txSuccessRate);
        dest.writeDouble(this.txRetriesRate);
        dest.writeDouble(this.txBadRate);
        dest.writeDouble(this.rxSuccessRate);
        dest.writeInt(this.badRssiCount);
        dest.writeInt(this.lowRssiCount);
        this.mSupplicantState.writeToParcel(dest, flags);
    }
}
