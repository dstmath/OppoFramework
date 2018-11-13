package android.hardware.location;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import java.util.Arrays;

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
public class ContextHubInfo {
    public static final Creator<ContextHubInfo> CREATOR = null;
    private int mId;
    private int mMaxPacketLengthBytes;
    private MemoryRegion[] mMemoryRegions;
    private String mName;
    private float mPeakMips;
    private float mPeakPowerDrawMw;
    private int mPlatformVersion;
    private float mSleepPowerDrawMw;
    private int mStaticSwVersion;
    private float mStoppedPowerDrawMw;
    private int[] mSupportedSensors;
    private String mToolchain;
    private int mToolchainVersion;
    private String mVendor;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.hardware.location.ContextHubInfo.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.hardware.location.ContextHubInfo.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.hardware.location.ContextHubInfo.<clinit>():void");
    }

    /* synthetic */ ContextHubInfo(Parcel in, ContextHubInfo contextHubInfo) {
        this(in);
    }

    public int getMaxPacketLengthBytes() {
        return this.mMaxPacketLengthBytes;
    }

    public void setMaxPacketLenBytes(int bytes) {
        this.mMaxPacketLengthBytes = bytes;
    }

    public int getId() {
        return this.mId;
    }

    public void setId(int id) {
        this.mId = id;
    }

    public String getName() {
        return this.mName;
    }

    public void setName(String name) {
        this.mName = name;
    }

    public String getVendor() {
        return this.mVendor;
    }

    public void setVendor(String vendor) {
        this.mVendor = vendor;
    }

    public String getToolchain() {
        return this.mToolchain;
    }

    public void setToolchain(String toolchain) {
        this.mToolchain = toolchain;
    }

    public int getPlatformVersion() {
        return this.mPlatformVersion;
    }

    public void setPlatformVersion(int platformVersion) {
        this.mPlatformVersion = platformVersion;
    }

    public int getStaticSwVersion() {
        return this.mStaticSwVersion;
    }

    public void setStaticSwVersion(int staticSwVersion) {
        this.mStaticSwVersion = staticSwVersion;
    }

    public int getToolchainVersion() {
        return this.mToolchainVersion;
    }

    public void setToolchainVersion(int toolchainVersion) {
        this.mToolchainVersion = toolchainVersion;
    }

    public float getPeakMips() {
        return this.mPeakMips;
    }

    public void setPeakMips(float peakMips) {
        this.mPeakMips = peakMips;
    }

    public float getStoppedPowerDrawMw() {
        return this.mStoppedPowerDrawMw;
    }

    public void setStoppedPowerDrawMw(float stoppedPowerDrawMw) {
        this.mStoppedPowerDrawMw = stoppedPowerDrawMw;
    }

    public float getSleepPowerDrawMw() {
        return this.mSleepPowerDrawMw;
    }

    public void setSleepPowerDrawMw(float sleepPowerDrawMw) {
        this.mSleepPowerDrawMw = sleepPowerDrawMw;
    }

    public float getPeakPowerDrawMw() {
        return this.mPeakPowerDrawMw;
    }

    public void setPeakPowerDrawMw(float peakPowerDrawMw) {
        this.mPeakPowerDrawMw = peakPowerDrawMw;
    }

    public int[] getSupportedSensors() {
        return Arrays.copyOf(this.mSupportedSensors, this.mSupportedSensors.length);
    }

    public MemoryRegion[] getMemoryRegions() {
        return (MemoryRegion[]) Arrays.copyOf(this.mMemoryRegions, this.mMemoryRegions.length);
    }

    public void setSupportedSensors(int[] supportedSensors) {
        this.mSupportedSensors = Arrays.copyOf(supportedSensors, supportedSensors.length);
    }

    public void setMemoryRegions(MemoryRegion[] memoryRegions) {
        this.mMemoryRegions = (MemoryRegion[]) Arrays.copyOf(memoryRegions, memoryRegions.length);
    }

    public String toString() {
        return ((((((((((("" + "Id : " + this.mId) + ", Name : " + this.mName) + "\n\tVendor : " + this.mVendor) + ", ToolChain : " + this.mToolchain) + "\n\tPlatformVersion : " + this.mPlatformVersion) + ", StaticSwVersion : " + this.mStaticSwVersion) + "\n\tPeakMips : " + this.mPeakMips) + ", StoppedPowerDraw : " + this.mStoppedPowerDrawMw + " mW") + ", PeakPowerDraw : " + this.mPeakPowerDrawMw + " mW") + ", MaxPacketLength : " + this.mMaxPacketLengthBytes + " Bytes") + "\n\tSupported sensors : " + Arrays.toString(this.mSupportedSensors)) + "\n\tMemory Regions : " + Arrays.toString(this.mMemoryRegions);
    }

    private ContextHubInfo(Parcel in) {
        this.mId = in.readInt();
        this.mName = in.readString();
        this.mVendor = in.readString();
        this.mToolchain = in.readString();
        this.mPlatformVersion = in.readInt();
        this.mToolchainVersion = in.readInt();
        this.mStaticSwVersion = in.readInt();
        this.mPeakMips = in.readFloat();
        this.mStoppedPowerDrawMw = in.readFloat();
        this.mSleepPowerDrawMw = in.readFloat();
        this.mPeakPowerDrawMw = in.readFloat();
        this.mMaxPacketLengthBytes = in.readInt();
        this.mSupportedSensors = new int[in.readInt()];
        in.readIntArray(this.mSupportedSensors);
        this.mMemoryRegions = (MemoryRegion[]) in.createTypedArray(MemoryRegion.CREATOR);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(this.mId);
        out.writeString(this.mName);
        out.writeString(this.mVendor);
        out.writeString(this.mToolchain);
        out.writeInt(this.mPlatformVersion);
        out.writeInt(this.mToolchainVersion);
        out.writeInt(this.mStaticSwVersion);
        out.writeFloat(this.mPeakMips);
        out.writeFloat(this.mStoppedPowerDrawMw);
        out.writeFloat(this.mSleepPowerDrawMw);
        out.writeFloat(this.mPeakPowerDrawMw);
        out.writeInt(this.mMaxPacketLengthBytes);
        out.writeInt(this.mSupportedSensors.length);
        out.writeIntArray(this.mSupportedSensors);
        out.writeTypedArray(this.mMemoryRegions, flags);
    }
}
