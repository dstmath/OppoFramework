package android.net.wifi.p2p;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
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
public class WifiP2pWfdInfo implements Parcelable {
    private static final int CONTENT_PROTECTION_SUPPORT = 256;
    private static final int COUPLED_SINK_SUPPORT_AT_SINK = 8;
    private static final int COUPLED_SINK_SUPPORT_AT_SOURCE = 4;
    public static final Creator<WifiP2pWfdInfo> CREATOR = null;
    private static final int DEVICE_TYPE = 3;
    private static final int I2C_READ_WRITE_SUPPORT = 2;
    private static final int PREFERRED_DISPLAY_SUPPORT = 4;
    public static final int PRIMARY_SINK = 1;
    public static final int SECONDARY_SINK = 2;
    private static final int SESSION_AVAILABLE = 48;
    private static final int SESSION_AVAILABLE_BIT1 = 16;
    private static final int SESSION_AVAILABLE_BIT2 = 32;
    public static final int SOURCE_OR_PRIMARY_SINK = 3;
    private static final int STANDBY_RESUME_CONTROL_SUPPORT = 8;
    private static final String TAG = "WifiP2pWfdInfo";
    private static final int UIBC_SUPPORT = 1;
    public static final int WFD_SOURCE = 0;
    public boolean mCrossmountLoaned;
    private int mCtrlPort;
    private int mDeviceInfo;
    private int mExtCapa;
    private int mMaxThroughput;
    private boolean mWfdEnabled;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.net.wifi.p2p.WifiP2pWfdInfo.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.net.wifi.p2p.WifiP2pWfdInfo.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.net.wifi.p2p.WifiP2pWfdInfo.<clinit>():void");
    }

    public WifiP2pWfdInfo(int devInfo, int ctrlPort, int maxTput) {
        this.mWfdEnabled = true;
        this.mDeviceInfo = devInfo;
        this.mCtrlPort = ctrlPort;
        this.mMaxThroughput = maxTput;
    }

    public WifiP2pWfdInfo(int devInfo, int ctrlPort, int maxTput, int extCapa) {
        this.mWfdEnabled = true;
        this.mDeviceInfo = devInfo;
        this.mCtrlPort = ctrlPort;
        this.mMaxThroughput = maxTput;
        this.mExtCapa = extCapa;
    }

    public boolean isWfdEnabled() {
        return this.mWfdEnabled;
    }

    public void setWfdEnabled(boolean enabled) {
        this.mWfdEnabled = enabled;
    }

    public int getDeviceType() {
        return this.mDeviceInfo & 3;
    }

    public boolean setDeviceType(int deviceType) {
        if (deviceType < 0 || deviceType > 3) {
            return false;
        }
        this.mDeviceInfo &= -4;
        this.mDeviceInfo |= deviceType;
        return true;
    }

    public boolean isCoupledSinkSupportedAtSource() {
        return (this.mDeviceInfo & 8) != 0;
    }

    public void setCoupledSinkSupportAtSource(boolean enabled) {
        if (enabled) {
            this.mDeviceInfo |= 8;
        } else {
            this.mDeviceInfo &= -9;
        }
    }

    public boolean isCoupledSinkSupportedAtSink() {
        return (this.mDeviceInfo & 8) != 0;
    }

    public void setCoupledSinkSupportAtSink(boolean enabled) {
        if (enabled) {
            this.mDeviceInfo |= 8;
        } else {
            this.mDeviceInfo &= -9;
        }
    }

    public boolean isSessionAvailable() {
        return (this.mDeviceInfo & 48) != 0;
    }

    public void setSessionAvailable(boolean enabled) {
        if (enabled) {
            this.mDeviceInfo |= 16;
            this.mDeviceInfo &= -33;
            return;
        }
        this.mDeviceInfo &= -49;
    }

    public void setContentProtected(boolean enabled) {
        if (enabled) {
            this.mDeviceInfo |= 256;
        } else {
            this.mDeviceInfo &= -257;
        }
    }

    public boolean isContentProtected() {
        return (this.mDeviceInfo & 256) != 0;
    }

    public int getExtendedCapability() {
        return this.mExtCapa;
    }

    public void setUibcSupported(boolean enabled) {
        if (enabled) {
            this.mExtCapa |= 1;
        } else {
            this.mExtCapa &= -2;
        }
    }

    public boolean isUibcSupported() {
        return (this.mExtCapa & 1) != 0;
    }

    public void setI2cRWSupported(boolean enabled) {
        if (enabled) {
            this.mExtCapa |= 2;
        } else {
            this.mExtCapa &= -3;
        }
    }

    public boolean isI2cRWSupported() {
        return (this.mExtCapa & 2) != 0;
    }

    public void setPreferredDisplaySupported(boolean enabled) {
        if (enabled) {
            this.mExtCapa |= 4;
        } else {
            this.mExtCapa &= -5;
        }
    }

    public boolean isPreferredDisplaySupported() {
        return (this.mExtCapa & 4) != 0;
    }

    public void setStandbyResumeCtrlSupported(boolean enabled) {
        if (enabled) {
            this.mExtCapa |= 8;
        } else {
            this.mExtCapa &= -9;
        }
    }

    public boolean isStandbyResumeCtrlSupported() {
        return (this.mExtCapa & 8) != 0;
    }

    public int getControlPort() {
        return this.mCtrlPort;
    }

    public void setControlPort(int port) {
        this.mCtrlPort = port;
    }

    public void setMaxThroughput(int maxThroughput) {
        this.mMaxThroughput = maxThroughput;
    }

    public int getMaxThroughput() {
        return this.mMaxThroughput;
    }

    public String getDeviceInfoHex() {
        Object[] objArr = new Object[4];
        objArr[0] = Integer.valueOf(6);
        objArr[1] = Integer.valueOf(this.mDeviceInfo);
        objArr[2] = Integer.valueOf(this.mCtrlPort);
        objArr[3] = Integer.valueOf(this.mMaxThroughput);
        return String.format(Locale.US, "%04x%04x%04x%04x", objArr);
    }

    public String getExtCapaHex() {
        Object[] objArr = new Object[2];
        objArr[0] = Integer.valueOf(2);
        objArr[1] = Integer.valueOf(this.mExtCapa);
        return String.format(Locale.US, "%04x%04x", objArr);
    }

    public String toString() {
        StringBuffer sbuf = new StringBuffer();
        sbuf.append("WFD enabled: ").append(this.mWfdEnabled);
        sbuf.append("\n WFD DeviceInfo: ").append(this.mDeviceInfo);
        sbuf.append("\n WFD CtrlPort: ").append(this.mCtrlPort);
        sbuf.append("\n WFD MaxThroughput: ").append(this.mMaxThroughput);
        sbuf.append("\n WFD Extended Capability: ").append(this.mExtCapa);
        sbuf.append("\n WFD info. loan to Crossmount? ").append(this.mCrossmountLoaned);
        return sbuf.toString();
    }

    public int describeContents() {
        return 0;
    }

    public WifiP2pWfdInfo(WifiP2pWfdInfo source) {
        if (source != null) {
            this.mWfdEnabled = source.mWfdEnabled;
            this.mDeviceInfo = source.mDeviceInfo;
            this.mCtrlPort = source.mCtrlPort;
            this.mMaxThroughput = source.mMaxThroughput;
            this.mExtCapa = source.mExtCapa;
            this.mCrossmountLoaned = source.mCrossmountLoaned;
        }
    }

    public void writeToParcel(Parcel dest, int flags) {
        int i;
        int i2 = 1;
        if (this.mWfdEnabled) {
            i = 1;
        } else {
            i = 0;
        }
        dest.writeInt(i);
        dest.writeInt(this.mDeviceInfo);
        dest.writeInt(this.mCtrlPort);
        dest.writeInt(this.mMaxThroughput);
        dest.writeInt(this.mExtCapa);
        if (!this.mCrossmountLoaned) {
            i2 = 0;
        }
        dest.writeInt(i2);
    }

    public void readFromParcel(Parcel in) {
        boolean z;
        boolean z2 = true;
        if (in.readInt() == 1) {
            z = true;
        } else {
            z = false;
        }
        this.mWfdEnabled = z;
        this.mDeviceInfo = in.readInt();
        this.mCtrlPort = in.readInt();
        this.mMaxThroughput = in.readInt();
        this.mExtCapa = in.readInt();
        if (in.readInt() != 1) {
            z2 = false;
        }
        this.mCrossmountLoaned = z2;
    }
}
