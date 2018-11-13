package com.sensetime.faceapi;

import android.graphics.Bitmap;
import android.graphics.Rect;
import com.android.server.face.sensetime.faceapi.model.CvPixelFormat;
import com.sensetime.faceapi.model.FaceInfo;

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
public class FaceLibrary {
    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.sensetime.faceapi.FaceLibrary.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.sensetime.faceapi.FaceLibrary.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sensetime.faceapi.FaceLibrary.<clinit>():void");
    }

    public static native float averageBrightness(byte[] bArr, CvPixelFormat cvPixelFormat, int i, int i2, int i3, int i4, int i5, int i6);

    public static native void cropNv21Data(byte[] bArr, int i, int i2, int i3, int i4, int i5, int i6, byte[] bArr2);

    public static native void cvFaceCheckOcular(long j, byte[] bArr, int i, int i2, int i3, int i4, int i5, Rect rect, float[] fArr, int[] iArr);

    public static native float cvFaceCompareFeature(long j, byte[] bArr, byte[] bArr2, int[] iArr);

    public static native long cvFaceCreateDetector(String str, int i);

    public static native long cvFaceCreateHackness(String str);

    public static native long cvFaceCreateOcular(String str);

    public static native long cvFaceCreateSelect(String str, int i);

    public static native long cvFaceCreateTracker(String str, String str2, int i);

    public static native long cvFaceCreateVerify(String str);

    public static native long cvFaceDeserialize(byte[] bArr);

    public static native void cvFaceDestroyDetector(long j);

    public static native void cvFaceDestroyHackness(long j);

    public static native void cvFaceDestroyOcular(long j);

    public static native void cvFaceDestroySelect(long j);

    public static native void cvFaceDestroyTracker(long j);

    public static native void cvFaceDestroyVerify(long j);

    public static native FaceInfo[] cvFaceDetectBytes(long j, byte[] bArr, int i, int i2, int i3, int i4, int i5, int[] iArr);

    public static native FaceInfo[] cvFaceDetectInts(long j, int[] iArr, int i, int i2, int i3, int i4, int i5, int[] iArr2);

    public static native byte[] cvFaceGetFeatureBytes(long j, byte[] bArr, int i, int i2, int i3, int i4, FaceInfo faceInfo, int[] iArr);

    public static native byte[] cvFaceGetFeatureInts(long j, int[] iArr, int i, int i2, int i3, int i4, FaceInfo faceInfo, int[] iArr2);

    public static native int cvFaceGetVerifyLength(long j);

    public static native int cvFaceGetVerifyVersion(long j);

    public static native float cvFaceHackness(long j, byte[] bArr, int i, int i2, int i3, int i4, int i5, FaceInfo faceInfo, int[] iArr);

    public static native void cvFaceResetSelect(long j);

    public static native void cvFaceResetTracker(long j);

    public static native float cvFaceSelectFrame(long j, byte[] bArr, int i, int i2, int i3, int i4, FaceInfo faceInfo, int[] iArr);

    public static native void cvFaceShowInsideModel();

    public static native FaceInfo[] cvFaceTrackBytes(long j, byte[] bArr, int i, int i2, int i3, int i4, int i5, int[] iArr);

    public static native FaceInfo[] cvFaceTrackInts(long j, int[] iArr, int i, int i2, int i3, int i4, int i5, int[] iArr2);

    public static native int cvFaceTrackSetDetectFaceCntLimit(long j, int i);

    public static native int cvFaceTrackSetDetectInterval(long j, int i);

    public static native void getBGRFromBitmap(Bitmap bitmap, byte[] bArr);

    public static native float getDetectThreshold(long j, int[] iArr);

    public static native int getVerifyVersion(long j);

    public static native int initLiscence(byte[] bArr);

    public static native int initLiscenceStr(String str);

    public static native void setDebug(boolean z);

    public static native void setDetectThreshold(long j, float f, int[] iArr);
}
