package android.os;

import android.os.Parcelable.Creator;

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
public class BatteryProperties implements Parcelable {
    public static final Creator<BatteryProperties> CREATOR = null;
    public int adjustPower;
    public int batteryChargeCounter;
    public int batteryCurrentNow;
    public int batteryHealth;
    public int batteryLevel;
    public int batteryLevel_smb;
    public boolean batteryPresent;
    public boolean batteryPresent_smb;
    public int batteryStatus;
    public int batteryStatus_smb;
    public String batteryTechnology;
    public int batteryTemperature;
    public int batteryVoltage;
    public boolean chargerAcOnline;
    public boolean chargerUsbOnline;
    public boolean chargerWirelessOnline;
    public int maxChargingCurrent;
    public int maxChargingVoltage;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.os.BatteryProperties.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.os.BatteryProperties.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.os.BatteryProperties.<clinit>():void");
    }

    /* synthetic */ BatteryProperties(Parcel p, BatteryProperties batteryProperties) {
        this(p);
    }

    public void set(BatteryProperties other) {
        this.chargerAcOnline = other.chargerAcOnline;
        this.chargerUsbOnline = other.chargerUsbOnline;
        this.chargerWirelessOnline = other.chargerWirelessOnline;
        this.maxChargingCurrent = other.maxChargingCurrent;
        this.maxChargingVoltage = other.maxChargingVoltage;
        this.batteryStatus = other.batteryStatus;
        this.batteryHealth = other.batteryHealth;
        this.batteryPresent = other.batteryPresent;
        this.batteryLevel = other.batteryLevel;
        this.batteryVoltage = other.batteryVoltage;
        this.batteryTemperature = other.batteryTemperature;
        this.batteryStatus_smb = other.batteryStatus_smb;
        this.batteryPresent_smb = other.batteryPresent_smb;
        this.batteryLevel_smb = other.batteryLevel_smb;
        this.batteryCurrentNow = other.batteryCurrentNow;
        this.batteryChargeCounter = other.batteryChargeCounter;
        this.adjustPower = other.adjustPower;
        this.batteryTechnology = other.batteryTechnology;
    }

    private BatteryProperties(Parcel p) {
        boolean z;
        boolean z2 = true;
        if (p.readInt() == 1) {
            z = true;
        } else {
            z = false;
        }
        this.chargerAcOnline = z;
        if (p.readInt() == 1) {
            z = true;
        } else {
            z = false;
        }
        this.chargerUsbOnline = z;
        if (p.readInt() == 1) {
            z = true;
        } else {
            z = false;
        }
        this.chargerWirelessOnline = z;
        this.maxChargingCurrent = p.readInt();
        this.maxChargingVoltage = p.readInt();
        this.batteryStatus = p.readInt();
        this.batteryHealth = p.readInt();
        if (p.readInt() == 1) {
            z = true;
        } else {
            z = false;
        }
        this.batteryPresent = z;
        this.batteryLevel = p.readInt();
        this.batteryVoltage = p.readInt();
        this.batteryTemperature = p.readInt();
        this.batteryStatus_smb = p.readInt();
        if (p.readInt() != 1) {
            z2 = false;
        }
        this.batteryPresent_smb = z2;
        this.batteryLevel_smb = p.readInt();
        this.batteryCurrentNow = p.readInt();
        this.batteryChargeCounter = p.readInt();
        this.adjustPower = p.readInt();
        this.batteryTechnology = p.readString();
    }

    public void writeToParcel(Parcel p, int flags) {
        int i;
        int i2 = 1;
        if (this.chargerAcOnline) {
            i = 1;
        } else {
            i = 0;
        }
        p.writeInt(i);
        if (this.chargerUsbOnline) {
            i = 1;
        } else {
            i = 0;
        }
        p.writeInt(i);
        if (this.chargerWirelessOnline) {
            i = 1;
        } else {
            i = 0;
        }
        p.writeInt(i);
        p.writeInt(this.maxChargingCurrent);
        p.writeInt(this.maxChargingVoltage);
        p.writeInt(this.batteryStatus);
        p.writeInt(this.batteryHealth);
        if (this.batteryPresent) {
            i = 1;
        } else {
            i = 0;
        }
        p.writeInt(i);
        p.writeInt(this.batteryLevel);
        p.writeInt(this.batteryVoltage);
        p.writeInt(this.batteryTemperature);
        p.writeInt(this.batteryStatus_smb);
        if (!this.batteryPresent_smb) {
            i2 = 0;
        }
        p.writeInt(i2);
        p.writeInt(this.batteryLevel_smb);
        p.writeInt(this.batteryCurrentNow);
        p.writeInt(this.batteryChargeCounter);
        p.writeInt(this.adjustPower);
        p.writeString(this.batteryTechnology);
    }

    public int describeContents() {
        return 0;
    }
}
