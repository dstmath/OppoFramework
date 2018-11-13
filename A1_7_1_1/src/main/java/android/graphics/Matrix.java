package android.graphics;

import java.io.PrintWriter;

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
public class Matrix {
    public static final Matrix IDENTITY_MATRIX = null;
    public static final int MPERSP_0 = 6;
    public static final int MPERSP_1 = 7;
    public static final int MPERSP_2 = 8;
    public static final int MSCALE_X = 0;
    public static final int MSCALE_Y = 4;
    public static final int MSKEW_X = 1;
    public static final int MSKEW_Y = 3;
    public static final int MTRANS_X = 2;
    public static final int MTRANS_Y = 5;
    public long native_instance;

    /*  JADX ERROR: NullPointerException in pass: EnumVisitor
        java.lang.NullPointerException
        	at jadx.core.dex.visitors.EnumVisitor.visit(EnumVisitor.java:102)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
        	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$0(DepthTraversal.java:13)
        	at java.util.ArrayList.forEach(ArrayList.java:1251)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:13)
        	at jadx.core.ProcessClass.process(ProcessClass.java:32)
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    public enum ScaleToFit {
        ;
        
        final int nativeInt;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.graphics.Matrix.ScaleToFit.<clinit>():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 10 more
            */
        static {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.graphics.Matrix.ScaleToFit.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.graphics.Matrix.ScaleToFit.<clinit>():void");
        }

        private ScaleToFit(int nativeInt) {
            this.nativeInt = nativeInt;
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.graphics.Matrix.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.graphics.Matrix.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.graphics.Matrix.<clinit>():void");
    }

    private static native void finalizer(long j);

    private static native long native_create(long j);

    private static native boolean native_equals(long j, long j2);

    private static native void native_getValues(long j, float[] fArr);

    private static native boolean native_invert(long j, long j2);

    private static native boolean native_isAffine(long j);

    private static native boolean native_isIdentity(long j);

    private static native void native_mapPoints(long j, float[] fArr, int i, float[] fArr2, int i2, int i3, boolean z);

    private static native float native_mapRadius(long j, float f);

    private static native boolean native_mapRect(long j, RectF rectF, RectF rectF2);

    private static native void native_postConcat(long j, long j2);

    private static native void native_postRotate(long j, float f);

    private static native void native_postRotate(long j, float f, float f2, float f3);

    private static native void native_postScale(long j, float f, float f2);

    private static native void native_postScale(long j, float f, float f2, float f3, float f4);

    private static native void native_postSkew(long j, float f, float f2);

    private static native void native_postSkew(long j, float f, float f2, float f3, float f4);

    private static native void native_postTranslate(long j, float f, float f2);

    private static native void native_preConcat(long j, long j2);

    private static native void native_preRotate(long j, float f);

    private static native void native_preRotate(long j, float f, float f2, float f3);

    private static native void native_preScale(long j, float f, float f2);

    private static native void native_preScale(long j, float f, float f2, float f3, float f4);

    private static native void native_preSkew(long j, float f, float f2);

    private static native void native_preSkew(long j, float f, float f2, float f3, float f4);

    private static native void native_preTranslate(long j, float f, float f2);

    private static native boolean native_rectStaysRect(long j);

    private static native void native_reset(long j);

    private static native void native_set(long j, long j2);

    private static native void native_setConcat(long j, long j2, long j3);

    private static native boolean native_setPolyToPoly(long j, float[] fArr, int i, float[] fArr2, int i2, int i3);

    private static native boolean native_setRectToRect(long j, RectF rectF, RectF rectF2, int i);

    private static native void native_setRotate(long j, float f);

    private static native void native_setRotate(long j, float f, float f2, float f3);

    private static native void native_setScale(long j, float f, float f2);

    private static native void native_setScale(long j, float f, float f2, float f3, float f4);

    private static native void native_setSinCos(long j, float f, float f2);

    private static native void native_setSinCos(long j, float f, float f2, float f3, float f4);

    private static native void native_setSkew(long j, float f, float f2);

    private static native void native_setSkew(long j, float f, float f2, float f3, float f4);

    private static native void native_setTranslate(long j, float f, float f2);

    private static native void native_setValues(long j, float[] fArr);

    public Matrix() {
        this.native_instance = native_create(0);
    }

    public Matrix(Matrix src) {
        this.native_instance = native_create(src != null ? src.native_instance : 0);
    }

    public boolean isIdentity() {
        return native_isIdentity(this.native_instance);
    }

    public boolean isAffine() {
        return native_isAffine(this.native_instance);
    }

    public boolean rectStaysRect() {
        return native_rectStaysRect(this.native_instance);
    }

    public void set(Matrix src) {
        if (src == null) {
            reset();
        } else {
            native_set(this.native_instance, src.native_instance);
        }
    }

    public boolean equals(Object obj) {
        if (obj instanceof Matrix) {
            return native_equals(this.native_instance, ((Matrix) obj).native_instance);
        }
        return false;
    }

    public int hashCode() {
        return 44;
    }

    public void reset() {
        native_reset(this.native_instance);
    }

    public void setTranslate(float dx, float dy) {
        native_setTranslate(this.native_instance, dx, dy);
    }

    public void setScale(float sx, float sy, float px, float py) {
        native_setScale(this.native_instance, sx, sy, px, py);
    }

    public void setScale(float sx, float sy) {
        native_setScale(this.native_instance, sx, sy);
    }

    public void setRotate(float degrees, float px, float py) {
        native_setRotate(this.native_instance, degrees, px, py);
    }

    public void setRotate(float degrees) {
        native_setRotate(this.native_instance, degrees);
    }

    public void setSinCos(float sinValue, float cosValue, float px, float py) {
        native_setSinCos(this.native_instance, sinValue, cosValue, px, py);
    }

    public void setSinCos(float sinValue, float cosValue) {
        native_setSinCos(this.native_instance, sinValue, cosValue);
    }

    public void setSkew(float kx, float ky, float px, float py) {
        native_setSkew(this.native_instance, kx, ky, px, py);
    }

    public void setSkew(float kx, float ky) {
        native_setSkew(this.native_instance, kx, ky);
    }

    public boolean setConcat(Matrix a, Matrix b) {
        native_setConcat(this.native_instance, a.native_instance, b.native_instance);
        return true;
    }

    public boolean preTranslate(float dx, float dy) {
        native_preTranslate(this.native_instance, dx, dy);
        return true;
    }

    public boolean preScale(float sx, float sy, float px, float py) {
        native_preScale(this.native_instance, sx, sy, px, py);
        return true;
    }

    public boolean preScale(float sx, float sy) {
        native_preScale(this.native_instance, sx, sy);
        return true;
    }

    public boolean preRotate(float degrees, float px, float py) {
        native_preRotate(this.native_instance, degrees, px, py);
        return true;
    }

    public boolean preRotate(float degrees) {
        native_preRotate(this.native_instance, degrees);
        return true;
    }

    public boolean preSkew(float kx, float ky, float px, float py) {
        native_preSkew(this.native_instance, kx, ky, px, py);
        return true;
    }

    public boolean preSkew(float kx, float ky) {
        native_preSkew(this.native_instance, kx, ky);
        return true;
    }

    public boolean preConcat(Matrix other) {
        native_preConcat(this.native_instance, other.native_instance);
        return true;
    }

    public boolean postTranslate(float dx, float dy) {
        native_postTranslate(this.native_instance, dx, dy);
        return true;
    }

    public boolean postScale(float sx, float sy, float px, float py) {
        native_postScale(this.native_instance, sx, sy, px, py);
        return true;
    }

    public boolean postScale(float sx, float sy) {
        native_postScale(this.native_instance, sx, sy);
        return true;
    }

    public boolean postRotate(float degrees, float px, float py) {
        native_postRotate(this.native_instance, degrees, px, py);
        return true;
    }

    public boolean postRotate(float degrees) {
        native_postRotate(this.native_instance, degrees);
        return true;
    }

    public boolean postSkew(float kx, float ky, float px, float py) {
        native_postSkew(this.native_instance, kx, ky, px, py);
        return true;
    }

    public boolean postSkew(float kx, float ky) {
        native_postSkew(this.native_instance, kx, ky);
        return true;
    }

    public boolean postConcat(Matrix other) {
        native_postConcat(this.native_instance, other.native_instance);
        return true;
    }

    public boolean setRectToRect(RectF src, RectF dst, ScaleToFit stf) {
        if (dst != null && src != null) {
            return native_setRectToRect(this.native_instance, src, dst, stf.nativeInt);
        }
        throw new NullPointerException();
    }

    private static void checkPointArrays(float[] src, int srcIndex, float[] dst, int dstIndex, int pointCount) {
        int srcStop = srcIndex + (pointCount << 1);
        int dstStop = dstIndex + (pointCount << 1);
        if (((((pointCount | srcIndex) | dstIndex) | srcStop) | dstStop) < 0 || srcStop > src.length || dstStop > dst.length) {
            throw new ArrayIndexOutOfBoundsException();
        }
    }

    public boolean setPolyToPoly(float[] src, int srcIndex, float[] dst, int dstIndex, int pointCount) {
        if (pointCount > 4) {
            throw new IllegalArgumentException();
        }
        checkPointArrays(src, srcIndex, dst, dstIndex, pointCount);
        return native_setPolyToPoly(this.native_instance, src, srcIndex, dst, dstIndex, pointCount);
    }

    public boolean invert(Matrix inverse) {
        return native_invert(this.native_instance, inverse.native_instance);
    }

    public void mapPoints(float[] dst, int dstIndex, float[] src, int srcIndex, int pointCount) {
        checkPointArrays(src, srcIndex, dst, dstIndex, pointCount);
        native_mapPoints(this.native_instance, dst, dstIndex, src, srcIndex, pointCount, true);
    }

    public void mapVectors(float[] dst, int dstIndex, float[] src, int srcIndex, int vectorCount) {
        checkPointArrays(src, srcIndex, dst, dstIndex, vectorCount);
        native_mapPoints(this.native_instance, dst, dstIndex, src, srcIndex, vectorCount, false);
    }

    public void mapPoints(float[] dst, float[] src) {
        if (dst.length != src.length) {
            throw new ArrayIndexOutOfBoundsException();
        }
        mapPoints(dst, 0, src, 0, dst.length >> 1);
    }

    public void mapVectors(float[] dst, float[] src) {
        if (dst.length != src.length) {
            throw new ArrayIndexOutOfBoundsException();
        }
        mapVectors(dst, 0, src, 0, dst.length >> 1);
    }

    public void mapPoints(float[] pts) {
        mapPoints(pts, 0, pts, 0, pts.length >> 1);
    }

    public void mapVectors(float[] vecs) {
        mapVectors(vecs, 0, vecs, 0, vecs.length >> 1);
    }

    public boolean mapRect(RectF dst, RectF src) {
        if (dst != null && src != null) {
            return native_mapRect(this.native_instance, dst, src);
        }
        throw new NullPointerException();
    }

    public boolean mapRect(RectF rect) {
        return mapRect(rect, rect);
    }

    public float mapRadius(float radius) {
        return native_mapRadius(this.native_instance, radius);
    }

    public void getValues(float[] values) {
        if (values.length < 9) {
            throw new ArrayIndexOutOfBoundsException();
        }
        native_getValues(this.native_instance, values);
    }

    public void setValues(float[] values) {
        if (values.length < 9) {
            throw new ArrayIndexOutOfBoundsException();
        }
        native_setValues(this.native_instance, values);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(64);
        sb.append("Matrix{");
        toShortString(sb);
        sb.append('}');
        return sb.toString();
    }

    public String toShortString() {
        StringBuilder sb = new StringBuilder(64);
        toShortString(sb);
        return sb.toString();
    }

    public void toShortString(StringBuilder sb) {
        float[] values = new float[9];
        getValues(values);
        sb.append('[');
        sb.append(values[0]);
        sb.append(", ");
        sb.append(values[1]);
        sb.append(", ");
        sb.append(values[2]);
        sb.append("][");
        sb.append(values[3]);
        sb.append(", ");
        sb.append(values[4]);
        sb.append(", ");
        sb.append(values[5]);
        sb.append("][");
        sb.append(values[6]);
        sb.append(", ");
        sb.append(values[7]);
        sb.append(", ");
        sb.append(values[8]);
        sb.append(']');
    }

    public void printShortString(PrintWriter pw) {
        float[] values = new float[9];
        getValues(values);
        pw.print('[');
        pw.print(values[0]);
        pw.print(", ");
        pw.print(values[1]);
        pw.print(", ");
        pw.print(values[2]);
        pw.print("][");
        pw.print(values[3]);
        pw.print(", ");
        pw.print(values[4]);
        pw.print(", ");
        pw.print(values[5]);
        pw.print("][");
        pw.print(values[6]);
        pw.print(", ");
        pw.print(values[7]);
        pw.print(", ");
        pw.print(values[8]);
        pw.print(']');
    }

    protected void finalize() throws Throwable {
        try {
            finalizer(this.native_instance);
            this.native_instance = 0;
        } finally {
            super.finalize();
        }
    }

    final long ni() {
        return this.native_instance;
    }
}
