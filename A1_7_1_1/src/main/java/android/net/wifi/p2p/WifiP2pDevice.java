package android.net.wifi.p2p;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.util.Log;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
public class WifiP2pDevice implements Parcelable {
    public static final int AVAILABLE = 3;
    public static final int CONNECTED = 0;
    public static final Creator<WifiP2pDevice> CREATOR = null;
    private static final int DEVICE_CAPAB_CLIENT_DISCOVERABILITY = 2;
    private static final int DEVICE_CAPAB_CONCURRENT_OPER = 4;
    private static final int DEVICE_CAPAB_DEVICE_LIMIT = 16;
    private static final int DEVICE_CAPAB_INFRA_MANAGED = 8;
    private static final int DEVICE_CAPAB_INVITATION_PROCEDURE = 32;
    private static final int DEVICE_CAPAB_SERVICE_DISCOVERY = 1;
    public static final int FAILED = 2;
    private static final int GROUP_CAPAB_CROSS_CONN = 16;
    private static final int GROUP_CAPAB_GROUP_FORMATION = 64;
    private static final int GROUP_CAPAB_GROUP_LIMIT = 4;
    private static final int GROUP_CAPAB_GROUP_OWNER = 1;
    private static final int GROUP_CAPAB_INTRA_BSS_DIST = 8;
    private static final int GROUP_CAPAB_PERSISTENT_GROUP = 2;
    private static final int GROUP_CAPAB_PERSISTENT_RECONN = 32;
    public static final int INVITED = 1;
    private static final String TAG = "WifiP2pDevice";
    public static final int UNAVAILABLE = 4;
    private static final int WPS_CONFIG_DISPLAY = 8;
    private static final int WPS_CONFIG_KEYPAD = 256;
    private static final int WPS_CONFIG_PUSHBUTTON = 128;
    private static final Pattern detailedDevicePattern = null;
    private static final Pattern threeTokenPattern = null;
    private static final Pattern twoTokenPattern = null;
    public String deviceAddress;
    public int deviceCapability;
    public String deviceIP;
    public String deviceName;
    public int groupCapability;
    public String interfaceAddress;
    public String primaryDeviceType;
    public String secondaryDeviceType;
    public int status;
    public WifiP2pWfdInfo wfdInfo;
    public int wpsConfigMethodsSupported;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.net.wifi.p2p.WifiP2pDevice.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.net.wifi.p2p.WifiP2pDevice.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.net.wifi.p2p.WifiP2pDevice.<clinit>():void");
    }

    public WifiP2pDevice() {
        this.deviceName = "";
        this.deviceAddress = "";
        this.interfaceAddress = "00:00:00:00:00:00";
        this.status = 4;
    }

    public WifiP2pDevice(String string) throws IllegalArgumentException {
        this.deviceName = "";
        this.deviceAddress = "";
        this.interfaceAddress = "00:00:00:00:00:00";
        this.status = 4;
        String[] tokens = string.split("[ \n]");
        if (tokens.length < 1) {
            throw new IllegalArgumentException("Malformed supplicant event");
        }
        Matcher match;
        switch (tokens.length) {
            case 1:
                this.deviceAddress = string;
                return;
            case 2:
                match = twoTokenPattern.matcher(string);
                if (match.find()) {
                    this.deviceAddress = match.group(2);
                    return;
                }
                throw new IllegalArgumentException("Malformed supplicant event");
            case 3:
                match = threeTokenPattern.matcher(string);
                if (match.find()) {
                    this.interfaceAddress = match.group(1);
                    this.deviceAddress = match.group(2);
                    return;
                }
                throw new IllegalArgumentException("Malformed supplicant event");
            default:
                match = detailedDevicePattern.matcher(string.replaceAll("\n", "_"));
                if (match.find()) {
                    this.interfaceAddress = match.group(1);
                    this.deviceAddress = match.group(3);
                    this.primaryDeviceType = match.group(4);
                    this.deviceName = match.group(5);
                    this.wpsConfigMethodsSupported = parseHex(match.group(6));
                    this.deviceCapability = parseHex(match.group(7));
                    this.groupCapability = parseHex(match.group(8));
                    if (match.group(9) != null) {
                        String str = match.group(10);
                        this.wfdInfo = new WifiP2pWfdInfo(parseHex(str.substring(0, 4)), parseHex(str.substring(4, 8)), parseHex(str.substring(8, 12)));
                    }
                    if (tokens[0].startsWith("P2P-DEVICE-FOUND")) {
                        this.status = 3;
                    }
                    this.deviceIP = null;
                    return;
                }
                throw new IllegalArgumentException("Malformed supplicant event");
        }
    }

    public boolean wpsPbcSupported() {
        return (this.wpsConfigMethodsSupported & 128) != 0;
    }

    public boolean wpsKeypadSupported() {
        return (this.wpsConfigMethodsSupported & 256) != 0;
    }

    public boolean wpsDisplaySupported() {
        return (this.wpsConfigMethodsSupported & 8) != 0;
    }

    public boolean isServiceDiscoveryCapable() {
        return (this.deviceCapability & 1) != 0;
    }

    public boolean isInvitationCapable() {
        return (this.deviceCapability & 32) != 0;
    }

    public boolean isDeviceLimit() {
        return (this.deviceCapability & 16) != 0;
    }

    public boolean isGroupOwner() {
        return (this.groupCapability & 1) != 0;
    }

    public boolean isGroupLimit() {
        return (this.groupCapability & 4) != 0;
    }

    public void update(WifiP2pDevice device) {
        updateSupplicantDetails(device);
        this.status = device.status;
    }

    public void updateSupplicantDetails(WifiP2pDevice device) {
        if (device == null) {
            throw new IllegalArgumentException("device is null");
        } else if (device.deviceAddress == null) {
            throw new IllegalArgumentException("deviceAddress is null");
        } else if (this.deviceAddress.equals(device.deviceAddress)) {
            this.deviceName = device.deviceName;
            this.interfaceAddress = device.interfaceAddress;
            this.primaryDeviceType = device.primaryDeviceType;
            this.secondaryDeviceType = device.secondaryDeviceType;
            this.wpsConfigMethodsSupported = device.wpsConfigMethodsSupported;
            this.deviceCapability = device.deviceCapability;
            this.groupCapability = device.groupCapability;
            this.wfdInfo = device.wfdInfo;
            this.deviceIP = device.deviceIP;
        } else {
            throw new IllegalArgumentException("deviceAddress does not match");
        }
    }

    public boolean equals(Object obj) {
        boolean z = true;
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof WifiP2pDevice)) {
            return false;
        }
        WifiP2pDevice other = (WifiP2pDevice) obj;
        if (other != null && other.deviceAddress != null) {
            return other.deviceAddress.equals(this.deviceAddress);
        }
        if (this.deviceAddress != null) {
            z = false;
        }
        return z;
    }

    public String toString() {
        StringBuffer sbuf = new StringBuffer();
        sbuf.append("Device: ").append(this.deviceName);
        sbuf.append("\n deviceAddress: ").append(this.deviceAddress);
        sbuf.append("\n interfaceAddress: ").append(this.interfaceAddress);
        sbuf.append("\n primary type: ").append(this.primaryDeviceType);
        sbuf.append("\n secondary type: ").append(this.secondaryDeviceType);
        sbuf.append("\n wps: ").append(this.wpsConfigMethodsSupported);
        sbuf.append("\n grpcapab: ").append(this.groupCapability);
        sbuf.append("\n devcapab: ").append(this.deviceCapability);
        sbuf.append("\n status: ").append(this.status);
        sbuf.append("\n wfdInfo: ").append(this.wfdInfo);
        sbuf.append("\n deviceIP: ").append(this.deviceIP);
        return sbuf.toString();
    }

    public int describeContents() {
        return 0;
    }

    public WifiP2pDevice(WifiP2pDevice source) {
        this.deviceName = "";
        this.deviceAddress = "";
        this.interfaceAddress = "00:00:00:00:00:00";
        this.status = 4;
        if (source != null) {
            this.deviceName = source.deviceName;
            this.deviceAddress = source.deviceAddress;
            this.interfaceAddress = source.interfaceAddress;
            this.primaryDeviceType = source.primaryDeviceType;
            this.secondaryDeviceType = source.secondaryDeviceType;
            this.wpsConfigMethodsSupported = source.wpsConfigMethodsSupported;
            this.deviceCapability = source.deviceCapability;
            this.groupCapability = source.groupCapability;
            this.status = source.status;
            this.wfdInfo = new WifiP2pWfdInfo(source.wfdInfo);
            this.deviceIP = source.deviceIP;
        }
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.deviceName);
        dest.writeString(this.deviceAddress);
        dest.writeString(this.interfaceAddress);
        dest.writeString(this.primaryDeviceType);
        dest.writeString(this.secondaryDeviceType);
        dest.writeInt(this.wpsConfigMethodsSupported);
        dest.writeInt(this.deviceCapability);
        dest.writeInt(this.groupCapability);
        dest.writeInt(this.status);
        if (this.wfdInfo != null) {
            dest.writeInt(1);
            this.wfdInfo.writeToParcel(dest, flags);
        } else {
            dest.writeInt(0);
        }
        dest.writeString(this.deviceIP);
    }

    public static int parseHex(String hexString) {
        int num = 0;
        if (hexString.startsWith("0x") || hexString.startsWith("0X")) {
            hexString = hexString.substring(2);
        }
        try {
            return Integer.parseInt(hexString, 16);
        } catch (NumberFormatException e) {
            Log.e(TAG, "Failed to parse hex string " + hexString);
            return num;
        }
    }
}
