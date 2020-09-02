package com.android.server.wifi.hotspot2;

import android.net.wifi.ScanResult;
import android.util.Log;
import com.android.server.wifi.hotspot2.anqp.ANQPElement;
import com.android.server.wifi.hotspot2.anqp.Constants;
import com.android.server.wifi.hotspot2.anqp.RawByteElement;
import com.android.server.wifi.util.InformationElementUtil;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class NetworkDetail {
    private static final boolean DBG = false;
    private static final String TAG = "NetworkDetail:";
    private final Map<Constants.ANQPElementType, ANQPElement> mANQPElements;
    private final int mAnqpDomainID;
    private final int mAnqpOICount;
    private final Ant mAnt;
    private final long mBSSID;
    private final int mCapacity;
    private final int mCenterfreq0;
    private final int mCenterfreq1;
    private final int mChannelUtilization;
    private final int mChannelWidth;
    private int mDtimInterval = -1;
    private final InformationElementUtil.ExtendedCapabilities mExtendedCapabilities;
    private final long mHESSID;
    private final HSRelease mHSRelease;
    private final boolean mInternet;
    private final boolean mIsHiddenSsid;
    private final int mMaxRate;
    private final int mPrimaryFreq;
    private final long[] mRoamingConsortiums;
    private final String mSSID;
    private final int mStationCount;
    private final int mWifiMode;

    public enum Ant {
        Private,
        PrivateWithGuest,
        ChargeablePublic,
        FreePublic,
        Personal,
        EmergencyOnly,
        Resvd6,
        Resvd7,
        Resvd8,
        Resvd9,
        Resvd10,
        Resvd11,
        Resvd12,
        Resvd13,
        TestOrExperimental,
        Wildcard
    }

    public enum HSRelease {
        R1,
        R2,
        Unknown
    }

    /* JADX INFO: Multiple debug info for r4v7 int: [D('isHiddenSsid' boolean), D('decoder' java.nio.charset.CharsetDecoder)] */
    /* JADX WARNING: Removed duplicated region for block: B:101:0x0255  */
    /* JADX WARNING: Removed duplicated region for block: B:102:0x0263  */
    /* JADX WARNING: Removed duplicated region for block: B:57:0x0113  */
    /* JADX WARNING: Removed duplicated region for block: B:59:0x0119  */
    /* JADX WARNING: Removed duplicated region for block: B:82:0x016b  */
    /* JADX WARNING: Removed duplicated region for block: B:85:0x01af  */
    /* JADX WARNING: Removed duplicated region for block: B:86:0x01c2  */
    /* JADX WARNING: Removed duplicated region for block: B:89:0x01d9  */
    /* JADX WARNING: Removed duplicated region for block: B:92:0x01e6  */
    /* JADX WARNING: Removed duplicated region for block: B:93:0x0201  */
    /* JADX WARNING: Removed duplicated region for block: B:96:0x020b  */
    public NetworkDetail(String bssid, ScanResult.InformationElement[] infoElements, List<String> list, int freq) {
        boolean isHiddenSsid;
        byte[] ssidOctets;
        ArrayList<Integer> iesFound;
        boolean isHiddenSsid2;
        String ssid;
        int maxRateB;
        String ssid2;
        byte[] ssidOctets2;
        ScanResult.InformationElement ie;
        ScanResult.InformationElement[] informationElementArr = infoElements;
        if (informationElementArr != null) {
            this.mBSSID = Utils.parseMac(bssid);
            boolean isHiddenSsid3 = false;
            InformationElementUtil.BssLoad bssLoad = new InformationElementUtil.BssLoad();
            InformationElementUtil.Interworking interworking = new InformationElementUtil.Interworking();
            InformationElementUtil.RoamingConsortium roamingConsortium = new InformationElementUtil.RoamingConsortium();
            InformationElementUtil.Vsa vsa = new InformationElementUtil.Vsa();
            InformationElementUtil.HtOperation htOperation = new InformationElementUtil.HtOperation();
            InformationElementUtil.VhtOperation vhtOperation = new InformationElementUtil.VhtOperation();
            InformationElementUtil.ExtendedCapabilities extendedCapabilities = new InformationElementUtil.ExtendedCapabilities();
            InformationElementUtil.TrafficIndicationMap trafficIndicationMap = new InformationElementUtil.TrafficIndicationMap();
            InformationElementUtil.SupportedRates supportedRates = new InformationElementUtil.SupportedRates();
            InformationElementUtil.SupportedRates extendedSupportedRates = new InformationElementUtil.SupportedRates();
            RuntimeException exception = null;
            ArrayList<Integer> iesFound2 = new ArrayList<>();
            try {
                int length = informationElementArr.length;
                ssidOctets2 = null;
                int i = 0;
                while (i < length) {
                    try {
                        ie = informationElementArr[i];
                        iesFound = iesFound2;
                    } catch (ArrayIndexOutOfBoundsException | IllegalArgumentException | BufferUnderflowException e) {
                        e = e;
                        iesFound = iesFound2;
                        isHiddenSsid = isHiddenSsid3;
                        Log.d(Utils.hs2LogTag(getClass()), "Caught " + e);
                        if (ssidOctets2 == null) {
                        }
                    }
                    try {
                        iesFound.add(Integer.valueOf(ie.id));
                        int i2 = ie.id;
                        if (i2 != 0) {
                            isHiddenSsid = isHiddenSsid3;
                            if (i2 == 1) {
                                supportedRates.from(ie);
                            } else if (i2 == 5) {
                                trafficIndicationMap.from(ie);
                            } else if (i2 == 11) {
                                bssLoad.from(ie);
                            } else if (i2 == 50) {
                                extendedSupportedRates.from(ie);
                            } else if (i2 == 61) {
                                htOperation.from(ie);
                            } else if (i2 == 107) {
                                interworking.from(ie);
                            } else if (i2 == 111) {
                                roamingConsortium.from(ie);
                            } else if (i2 == 127) {
                                extendedCapabilities.from(ie);
                            } else if (i2 == 192) {
                                vhtOperation.from(ie);
                            } else if (i2 == 221) {
                                try {
                                    vsa.from(ie);
                                } catch (ArrayIndexOutOfBoundsException | IllegalArgumentException | BufferUnderflowException e2) {
                                    e = e2;
                                    Log.d(Utils.hs2LogTag(getClass()), "Caught " + e);
                                    if (ssidOctets2 == null) {
                                    }
                                }
                            }
                        } else {
                            isHiddenSsid = isHiddenSsid3;
                            ssidOctets2 = ie.bytes;
                        }
                        i++;
                        isHiddenSsid3 = isHiddenSsid;
                        length = length;
                        iesFound2 = iesFound;
                        informationElementArr = infoElements;
                    } catch (ArrayIndexOutOfBoundsException | IllegalArgumentException | BufferUnderflowException e3) {
                        e = e3;
                        isHiddenSsid = isHiddenSsid3;
                        Log.d(Utils.hs2LogTag(getClass()), "Caught " + e);
                        if (ssidOctets2 == null) {
                            exception = e;
                            ssidOctets = ssidOctets2;
                            if (ssidOctets == null) {
                            }
                            this.mSSID = ssid;
                            this.mHESSID = interworking.hessid;
                            this.mIsHiddenSsid = isHiddenSsid2;
                            this.mStationCount = bssLoad.stationCount;
                            this.mChannelUtilization = bssLoad.channelUtilization;
                            this.mCapacity = bssLoad.capacity;
                            this.mAnt = interworking.ant;
                            this.mInternet = interworking.internet;
                            this.mHSRelease = vsa.hsRelease;
                            this.mAnqpDomainID = vsa.anqpDomainID;
                            this.mAnqpOICount = roamingConsortium.anqpOICount;
                            this.mRoamingConsortiums = roamingConsortium.getRoamingConsortiums();
                            this.mExtendedCapabilities = extendedCapabilities;
                            this.mANQPElements = null;
                            this.mPrimaryFreq = freq;
                            if (!vhtOperation.isValid()) {
                            }
                            if (trafficIndicationMap.isValid()) {
                            }
                            if (!extendedSupportedRates.isValid()) {
                            }
                            if (!supportedRates.isValid()) {
                            }
                        } else {
                            throw new IllegalArgumentException("Malformed IE string (no SSID)", e);
                        }
                    }
                }
                iesFound = iesFound2;
                isHiddenSsid = isHiddenSsid3;
                ssidOctets = ssidOctets2;
            } catch (ArrayIndexOutOfBoundsException | IllegalArgumentException | BufferUnderflowException e4) {
                e = e4;
                iesFound = iesFound2;
                isHiddenSsid = false;
                ssidOctets2 = null;
                Log.d(Utils.hs2LogTag(getClass()), "Caught " + e);
                if (ssidOctets2 == null) {
                }
            }
            if (ssidOctets == null) {
                try {
                    ssid2 = StandardCharsets.UTF_8.newDecoder().decode(ByteBuffer.wrap(ssidOctets)).toString();
                } catch (CharacterCodingException e5) {
                    ssid2 = null;
                }
                if (ssid2 == null) {
                    if (extendedCapabilities.isStrictUtf8()) {
                        if (exception != null) {
                            throw new IllegalArgumentException("Failed to decode SSID in dubious IE string");
                        }
                    }
                    ssid2 = new String(ssidOctets, StandardCharsets.ISO_8859_1);
                }
                int length2 = ssidOctets.length;
                int i3 = 0;
                while (true) {
                    if (i3 >= length2) {
                        isHiddenSsid2 = true;
                        ssid = ssid2;
                        break;
                    } else if (ssidOctets[i3] != 0) {
                        isHiddenSsid2 = false;
                        ssid = ssid2;
                        break;
                    } else {
                        i3++;
                    }
                }
            } else {
                isHiddenSsid2 = isHiddenSsid;
                ssid = null;
            }
            this.mSSID = ssid;
            this.mHESSID = interworking.hessid;
            this.mIsHiddenSsid = isHiddenSsid2;
            this.mStationCount = bssLoad.stationCount;
            this.mChannelUtilization = bssLoad.channelUtilization;
            this.mCapacity = bssLoad.capacity;
            this.mAnt = interworking.ant;
            this.mInternet = interworking.internet;
            this.mHSRelease = vsa.hsRelease;
            this.mAnqpDomainID = vsa.anqpDomainID;
            this.mAnqpOICount = roamingConsortium.anqpOICount;
            this.mRoamingConsortiums = roamingConsortium.getRoamingConsortiums();
            this.mExtendedCapabilities = extendedCapabilities;
            this.mANQPElements = null;
            this.mPrimaryFreq = freq;
            if (!vhtOperation.isValid()) {
                this.mChannelWidth = vhtOperation.getChannelWidth();
                this.mCenterfreq0 = vhtOperation.getCenterFreq0();
                this.mCenterfreq1 = vhtOperation.getCenterFreq1();
            } else {
                this.mChannelWidth = htOperation.getChannelWidth();
                this.mCenterfreq0 = htOperation.getCenterFreq0(this.mPrimaryFreq);
                this.mCenterfreq1 = 0;
            }
            if (trafficIndicationMap.isValid()) {
                this.mDtimInterval = trafficIndicationMap.mDtimPeriod;
            }
            if (!extendedSupportedRates.isValid()) {
                maxRateB = extendedSupportedRates.mRates.get(extendedSupportedRates.mRates.size() - 1).intValue();
            } else {
                maxRateB = 0;
            }
            if (!supportedRates.isValid()) {
                int maxRateA = supportedRates.mRates.get(supportedRates.mRates.size() - 1).intValue();
                this.mMaxRate = maxRateA > maxRateB ? maxRateA : maxRateB;
                this.mWifiMode = InformationElementUtil.WifiMode.determineMode(this.mPrimaryFreq, this.mMaxRate, vhtOperation.isValid(), iesFound.contains(61), iesFound.contains(42));
                return;
            }
            this.mWifiMode = 0;
            this.mMaxRate = 0;
            return;
        }
        throw new IllegalArgumentException("Null information elements");
    }

    private static ByteBuffer getAndAdvancePayload(ByteBuffer data, int plLength) {
        ByteBuffer payload = data.duplicate().order(data.order());
        payload.limit(payload.position() + plLength);
        data.position(data.position() + plLength);
        return payload;
    }

    private NetworkDetail(NetworkDetail base, Map<Constants.ANQPElementType, ANQPElement> anqpElements) {
        this.mSSID = base.mSSID;
        this.mIsHiddenSsid = base.mIsHiddenSsid;
        this.mBSSID = base.mBSSID;
        this.mHESSID = base.mHESSID;
        this.mStationCount = base.mStationCount;
        this.mChannelUtilization = base.mChannelUtilization;
        this.mCapacity = base.mCapacity;
        this.mAnt = base.mAnt;
        this.mInternet = base.mInternet;
        this.mHSRelease = base.mHSRelease;
        this.mAnqpDomainID = base.mAnqpDomainID;
        this.mAnqpOICount = base.mAnqpOICount;
        this.mRoamingConsortiums = base.mRoamingConsortiums;
        this.mExtendedCapabilities = new InformationElementUtil.ExtendedCapabilities(base.mExtendedCapabilities);
        this.mANQPElements = anqpElements;
        this.mChannelWidth = base.mChannelWidth;
        this.mPrimaryFreq = base.mPrimaryFreq;
        this.mCenterfreq0 = base.mCenterfreq0;
        this.mCenterfreq1 = base.mCenterfreq1;
        this.mDtimInterval = base.mDtimInterval;
        this.mWifiMode = base.mWifiMode;
        this.mMaxRate = base.mMaxRate;
    }

    public NetworkDetail complete(Map<Constants.ANQPElementType, ANQPElement> anqpElements) {
        return new NetworkDetail(this, anqpElements);
    }

    public boolean queriable(List<Constants.ANQPElementType> queryElements) {
        return this.mAnt != null && (Constants.hasBaseANQPElements(queryElements) || (Constants.hasR2Elements(queryElements) && this.mHSRelease == HSRelease.R2));
    }

    public boolean has80211uInfo() {
        return (this.mAnt == null && this.mRoamingConsortiums == null && this.mHSRelease == null) ? false : true;
    }

    public boolean hasInterworking() {
        return this.mAnt != null;
    }

    public String getSSID() {
        return this.mSSID;
    }

    public String getTrimmedSSID() {
        if (this.mSSID == null) {
            return "";
        }
        for (int n = 0; n < this.mSSID.length(); n++) {
            if (this.mSSID.charAt(n) != 0) {
                return this.mSSID;
            }
        }
        return "";
    }

    public long getHESSID() {
        return this.mHESSID;
    }

    public long getBSSID() {
        return this.mBSSID;
    }

    public int getStationCount() {
        return this.mStationCount;
    }

    public int getChannelUtilization() {
        return this.mChannelUtilization;
    }

    public int getCapacity() {
        return this.mCapacity;
    }

    public boolean isInterworking() {
        return this.mAnt != null;
    }

    public Ant getAnt() {
        return this.mAnt;
    }

    public boolean isInternet() {
        return this.mInternet;
    }

    public HSRelease getHSRelease() {
        return this.mHSRelease;
    }

    public int getAnqpDomainID() {
        return this.mAnqpDomainID;
    }

    public byte[] getOsuProviders() {
        ANQPElement osuProviders;
        Map<Constants.ANQPElementType, ANQPElement> map = this.mANQPElements;
        if (map == null || (osuProviders = map.get(Constants.ANQPElementType.HSOSUProviders)) == null) {
            return null;
        }
        return ((RawByteElement) osuProviders).getPayload();
    }

    public int getAnqpOICount() {
        return this.mAnqpOICount;
    }

    public long[] getRoamingConsortiums() {
        return this.mRoamingConsortiums;
    }

    public Map<Constants.ANQPElementType, ANQPElement> getANQPElements() {
        return this.mANQPElements;
    }

    public int getChannelWidth() {
        return this.mChannelWidth;
    }

    public int getCenterfreq0() {
        return this.mCenterfreq0;
    }

    public int getCenterfreq1() {
        return this.mCenterfreq1;
    }

    public int getWifiMode() {
        return this.mWifiMode;
    }

    public int getDtimInterval() {
        return this.mDtimInterval;
    }

    public boolean is80211McResponderSupport() {
        return this.mExtendedCapabilities.is80211McRTTResponder();
    }

    public boolean isSSID_UTF8() {
        return this.mExtendedCapabilities.isStrictUtf8();
    }

    public boolean equals(Object thatObject) {
        if (this == thatObject) {
            return true;
        }
        if (thatObject == null || getClass() != thatObject.getClass()) {
            return false;
        }
        NetworkDetail that = (NetworkDetail) thatObject;
        if (!getSSID().equals(that.getSSID()) || getBSSID() != that.getBSSID()) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        long j = this.mBSSID;
        return (((this.mSSID.hashCode() * 31) + ((int) (j >>> 32))) * 31) + ((int) j);
    }

    public String toString() {
        return String.format("NetworkInfo{SSID='%s', HESSID=%x, BSSID=%x, StationCount=%d, ChannelUtilization=%d, Capacity=%d, Ant=%s, Internet=%s, HSRelease=%s, AnqpDomainID=%d, AnqpOICount=%d, RoamingConsortiums=%s}", this.mSSID, Long.valueOf(this.mHESSID), Long.valueOf(this.mBSSID), Integer.valueOf(this.mStationCount), Integer.valueOf(this.mChannelUtilization), Integer.valueOf(this.mCapacity), this.mAnt, Boolean.valueOf(this.mInternet), this.mHSRelease, Integer.valueOf(this.mAnqpDomainID), Integer.valueOf(this.mAnqpOICount), Utils.roamingConsortiumsToString(this.mRoamingConsortiums));
    }

    public String toKeyString() {
        if (this.mHESSID != 0) {
            return String.format("'%s':%012x (%012x)", this.mSSID, Long.valueOf(this.mBSSID), Long.valueOf(this.mHESSID));
        }
        return String.format("'%s':%012x", this.mSSID, Long.valueOf(this.mBSSID));
    }

    public String getBSSIDString() {
        return toMACString(this.mBSSID);
    }

    public boolean isBeaconFrame() {
        return this.mDtimInterval > 0;
    }

    public boolean isHiddenBeaconFrame() {
        return isBeaconFrame() && this.mIsHiddenSsid;
    }

    public static String toMACString(long mac) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (int n = 5; n >= 0; n--) {
            if (first) {
                first = false;
            } else {
                sb.append(':');
            }
            sb.append(String.format("%02x", Long.valueOf((mac >>> (n * 8)) & 255)));
        }
        return sb.toString();
    }
}
