package android.net.wifi;

import android.net.NetworkInfo.DetailedState;
import android.net.NetworkUtils;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.text.TextUtils;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.EnumMap;
import java.util.Locale;

public class WifiInfo implements Parcelable {
    public static final Creator<WifiInfo> CREATOR = new Creator<WifiInfo>() {
        public WifiInfo createFromParcel(Parcel in) {
            boolean z = true;
            WifiInfo info = new WifiInfo();
            info.setNetworkId(in.readInt());
            info.setRssi(in.readInt());
            info.setLinkSpeed(in.readInt());
            info.setFrequency(in.readInt());
            if (in.readByte() == (byte) 1) {
                try {
                    info.setInetAddress(InetAddress.getByAddress(in.createByteArray()));
                } catch (UnknownHostException e) {
                }
            }
            if (in.readInt() == 1) {
                info.mWifiSsid = (WifiSsid) WifiSsid.CREATOR.createFromParcel(in);
            }
            info.mBSSID = in.readString();
            info.mMacAddress = in.readString();
            info.mMeteredHint = in.readInt() != 0;
            if (in.readInt() == 0) {
                z = false;
            }
            info.mEphemeral = z;
            info.score = in.readInt();
            info.txSuccessRate = in.readDouble();
            info.txRetriesRate = in.readDouble();
            info.txBadRate = in.readDouble();
            info.rxSuccessRate = in.readDouble();
            info.badRssiCount = in.readInt();
            info.lowRssiCount = in.readInt();
            info.mSupplicantState = (SupplicantState) SupplicantState.CREATOR.createFromParcel(in);
            return info;
        }

        public WifiInfo[] newArray(int size) {
            return new WifiInfo[size];
        }
    };
    public static final String DEFAULT_MAC_ADDRESS = "02:00:00:00:00:00";
    private static final long FILTER_TIME_CONSTANT = 3000;
    public static final String FREQUENCY_UNITS = "MHz";
    public static final int INVALID_RSSI = -127;
    public static final String LINK_SPEED_UNITS = "Mbps";
    public static final int MAX_RSSI = 200;
    public static final int MIN_RSSI = -126;
    private static final long OUTPUT_SCALE_FACTOR = 5;
    private static final long RESET_TIME_STAMP = Long.MIN_VALUE;
    private static final String TAG = "WifiInfo";
    private static final EnumMap<SupplicantState, DetailedState> stateMap = new EnumMap(SupplicantState.class);
    public int badRssiCount;
    public int linkStuckCount;
    public int lowRssiCount;
    private String mBSSID;
    private boolean mEphemeral;
    private int mFrequency;
    private InetAddress mIpAddress;
    private long mLastPacketCountUpdateTimeStamp;
    private int mLinkSpeed;
    private String mMacAddress;
    private boolean mMeteredHint;
    private int mNetworkId;
    private int mRssi;
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

    static {
        stateMap.put(SupplicantState.DISCONNECTED, DetailedState.DISCONNECTED);
        stateMap.put(SupplicantState.INTERFACE_DISABLED, DetailedState.DISCONNECTED);
        stateMap.put(SupplicantState.INACTIVE, DetailedState.IDLE);
        stateMap.put(SupplicantState.SCANNING, DetailedState.SCANNING);
        stateMap.put(SupplicantState.AUTHENTICATING, DetailedState.CONNECTING);
        stateMap.put(SupplicantState.ASSOCIATING, DetailedState.CONNECTING);
        stateMap.put(SupplicantState.ASSOCIATED, DetailedState.CONNECTING);
        stateMap.put(SupplicantState.FOUR_WAY_HANDSHAKE, DetailedState.AUTHENTICATING);
        stateMap.put(SupplicantState.GROUP_HANDSHAKE, DetailedState.AUTHENTICATING);
        stateMap.put(SupplicantState.COMPLETED, DetailedState.OBTAINING_IPADDR);
        stateMap.put(SupplicantState.DORMANT, DetailedState.DISCONNECTED);
        stateMap.put(SupplicantState.UNINITIALIZED, DetailedState.IDLE);
        stateMap.put(SupplicantState.INVALID, DetailedState.FAILED);
    }

    public void updatePacketRates(WifiLinkLayerStats stats, long timeStamp) {
        if (stats != null) {
            long txgood = ((stats.txmpdu_be + stats.txmpdu_bk) + stats.txmpdu_vi) + stats.txmpdu_vo;
            long txretries = ((stats.retries_be + stats.retries_bk) + stats.retries_vi) + stats.retries_vo;
            long rxgood = ((stats.rxmpdu_be + stats.rxmpdu_bk) + stats.rxmpdu_vi) + stats.rxmpdu_vo;
            long txbad = ((stats.lostmpdu_be + stats.lostmpdu_bk) + stats.lostmpdu_vi) + stats.lostmpdu_vo;
            if (this.mLastPacketCountUpdateTimeStamp == Long.MIN_VALUE || this.mLastPacketCountUpdateTimeStamp >= timeStamp || this.txBad > txbad || this.txSuccess > txgood || this.rxSuccess > rxgood || this.txRetries > txretries) {
                this.txBadRate = 0.0d;
                this.txSuccessRate = 0.0d;
                this.rxSuccessRate = 0.0d;
                this.txRetriesRate = 0.0d;
            } else {
                long timeDelta = timeStamp - this.mLastPacketCountUpdateTimeStamp;
                double lastSampleWeight = Math.exp((((double) timeDelta) * -1.0d) / 3000.0d);
                double currentSampleWeight = 1.0d - lastSampleWeight;
                this.txBadRate = (this.txBadRate * lastSampleWeight) + (((double) ((((txbad - this.txBad) * 5) * 1000) / timeDelta)) * currentSampleWeight);
                this.txSuccessRate = (this.txSuccessRate * lastSampleWeight) + (((double) ((((txgood - this.txSuccess) * 5) * 1000) / timeDelta)) * currentSampleWeight);
                this.rxSuccessRate = (this.rxSuccessRate * lastSampleWeight) + (((double) ((((rxgood - this.rxSuccess) * 5) * 1000) / timeDelta)) * currentSampleWeight);
                this.txRetriesRate = (this.txRetriesRate * lastSampleWeight) + (((double) ((((txretries - this.txRetries) * 5) * 1000) / timeDelta)) * currentSampleWeight);
            }
            this.txBad = txbad;
            this.txSuccess = txgood;
            this.rxSuccess = rxgood;
            this.txRetries = txretries;
            this.mLastPacketCountUpdateTimeStamp = timeStamp;
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
        this.mLastPacketCountUpdateTimeStamp = Long.MIN_VALUE;
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
        this.mWifiSsid = null;
        this.mBSSID = null;
        this.mNetworkId = -1;
        this.mSupplicantState = SupplicantState.UNINITIALIZED;
        this.mRssi = -127;
        this.mLinkSpeed = -1;
        this.mFrequency = -1;
        this.mLastPacketCountUpdateTimeStamp = Long.MIN_VALUE;
    }

    public void reset() {
        setInetAddress(null);
        setBSSID(null);
        setSSID(null);
        setNetworkId(-1);
        setRssi(-127);
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
        this.mLastPacketCountUpdateTimeStamp = Long.MIN_VALUE;
    }

    public WifiInfo(WifiInfo source) {
        this.mMacAddress = "02:00:00:00:00:00";
        this.mWifiSsidLock = new Object();
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
            this.mLastPacketCountUpdateTimeStamp = source.mLastPacketCountUpdateTimeStamp;
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
        if (rssi < -127) {
            rssi = -127;
        }
        if (rssi > 200) {
            rssi = 200;
        }
        this.mRssi = rssi;
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

    public double getTxSuccessRatePps() {
        return this.txSuccessRate / 5.0d;
    }

    public double getRxSuccessRatePps() {
        return this.rxSuccessRate / 5.0d;
    }

    public void setMacAddress(String macAddress) {
        this.mMacAddress = macAddress;
    }

    public String getMacAddress() {
        return this.mMacAddress;
    }

    public boolean hasRealMacAddress() {
        return this.mMacAddress != null ? "02:00:00:00:00:00".equals(this.mMacAddress) ^ 1 : false;
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
        int i = 1;
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
        dest.writeInt(this.mMeteredHint ? 1 : 0);
        if (!this.mEphemeral) {
            i = 0;
        }
        dest.writeInt(i);
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
