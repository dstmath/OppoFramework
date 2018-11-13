package android.hardware.input;

import android.hardware.camera2.params.TonemapCurve;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

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
public class TouchCalibration implements Parcelable {
    public static final Creator<TouchCalibration> CREATOR = null;
    public static final TouchCalibration IDENTITY = null;
    private final float mXOffset;
    private final float mXScale;
    private final float mXYMix;
    private final float mYOffset;
    private final float mYScale;
    private final float mYXMix;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.hardware.input.TouchCalibration.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.hardware.input.TouchCalibration.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.hardware.input.TouchCalibration.<clinit>():void");
    }

    public TouchCalibration() {
        this(1.0f, TonemapCurve.LEVEL_BLACK, TonemapCurve.LEVEL_BLACK, TonemapCurve.LEVEL_BLACK, 1.0f, TonemapCurve.LEVEL_BLACK);
    }

    public TouchCalibration(float xScale, float xyMix, float xOffset, float yxMix, float yScale, float yOffset) {
        this.mXScale = xScale;
        this.mXYMix = xyMix;
        this.mXOffset = xOffset;
        this.mYXMix = yxMix;
        this.mYScale = yScale;
        this.mYOffset = yOffset;
    }

    public TouchCalibration(Parcel in) {
        this.mXScale = in.readFloat();
        this.mXYMix = in.readFloat();
        this.mXOffset = in.readFloat();
        this.mYXMix = in.readFloat();
        this.mYScale = in.readFloat();
        this.mYOffset = in.readFloat();
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeFloat(this.mXScale);
        dest.writeFloat(this.mXYMix);
        dest.writeFloat(this.mXOffset);
        dest.writeFloat(this.mYXMix);
        dest.writeFloat(this.mYScale);
        dest.writeFloat(this.mYOffset);
    }

    public int describeContents() {
        return 0;
    }

    public float[] getAffineTransform() {
        float[] fArr = new float[6];
        fArr[0] = this.mXScale;
        fArr[1] = this.mXYMix;
        fArr[2] = this.mXOffset;
        fArr[3] = this.mYXMix;
        fArr[4] = this.mYScale;
        fArr[5] = this.mYOffset;
        return fArr;
    }

    public boolean equals(Object obj) {
        boolean z = true;
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof TouchCalibration)) {
            return false;
        }
        TouchCalibration cal = (TouchCalibration) obj;
        if (cal.mXScale != this.mXScale || cal.mXYMix != this.mXYMix || cal.mXOffset != this.mXOffset || cal.mYXMix != this.mYXMix || cal.mYScale != this.mYScale) {
            z = false;
        } else if (cal.mYOffset != this.mYOffset) {
            z = false;
        }
        return z;
    }

    public int hashCode() {
        return ((((Float.floatToIntBits(this.mXScale) ^ Float.floatToIntBits(this.mXYMix)) ^ Float.floatToIntBits(this.mXOffset)) ^ Float.floatToIntBits(this.mYXMix)) ^ Float.floatToIntBits(this.mYScale)) ^ Float.floatToIntBits(this.mYOffset);
    }

    public String toString() {
        Object[] objArr = new Object[6];
        objArr[0] = Float.valueOf(this.mXScale);
        objArr[1] = Float.valueOf(this.mXYMix);
        objArr[2] = Float.valueOf(this.mXOffset);
        objArr[3] = Float.valueOf(this.mYXMix);
        objArr[4] = Float.valueOf(this.mYScale);
        objArr[5] = Float.valueOf(this.mYOffset);
        return String.format("[%f, %f, %f, %f, %f, %f]", objArr);
    }
}
