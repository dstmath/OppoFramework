package android.graphics;

import android.hardware.camera2.params.TonemapCurve;
import android.os.SystemClock;

public class Interpolator {
    private int mFrameCount;
    private int mValueCount;
    private long native_instance;

    /*  JADX ERROR: NullPointerException in pass: ReSugarCode
        java.lang.NullPointerException
        	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
        	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
        	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$0(DepthTraversal.java:13)
        	at java.util.ArrayList.forEach(ArrayList.java:1251)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:13)
        	at jadx.core.ProcessClass.process(ProcessClass.java:32)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    /*  JADX ERROR: NullPointerException in pass: EnumVisitor
        java.lang.NullPointerException
        	at jadx.core.dex.visitors.EnumVisitor.visit(EnumVisitor.java:102)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
        	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$0(DepthTraversal.java:13)
        	at java.util.ArrayList.forEach(ArrayList.java:1251)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:13)
        	at jadx.core.ProcessClass.process(ProcessClass.java:32)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    public enum Result {
        ;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.graphics.Interpolator.Result.<clinit>():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        static {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.graphics.Interpolator.Result.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.graphics.Interpolator.Result.<clinit>():void");
        }
    }

    private static native long nativeConstructor(int i, int i2);

    private static native void nativeDestructor(long j);

    private static native void nativeReset(long j, int i, int i2);

    private static native void nativeSetKeyFrame(long j, int i, int i2, float[] fArr, float[] fArr2);

    private static native void nativeSetRepeatMirror(long j, float f, boolean z);

    private static native int nativeTimeToValues(long j, int i, float[] fArr);

    public Interpolator(int valueCount) {
        this.mValueCount = valueCount;
        this.mFrameCount = 2;
        this.native_instance = nativeConstructor(valueCount, 2);
    }

    public Interpolator(int valueCount, int frameCount) {
        this.mValueCount = valueCount;
        this.mFrameCount = frameCount;
        this.native_instance = nativeConstructor(valueCount, frameCount);
    }

    public void reset(int valueCount) {
        reset(valueCount, 2);
    }

    public void reset(int valueCount, int frameCount) {
        this.mValueCount = valueCount;
        this.mFrameCount = frameCount;
        nativeReset(this.native_instance, valueCount, frameCount);
    }

    public final int getKeyFrameCount() {
        return this.mFrameCount;
    }

    public final int getValueCount() {
        return this.mValueCount;
    }

    public void setKeyFrame(int index, int msec, float[] values) {
        setKeyFrame(index, msec, values, null);
    }

    public void setKeyFrame(int index, int msec, float[] values, float[] blend) {
        if (index < 0 || index >= this.mFrameCount) {
            throw new IndexOutOfBoundsException();
        } else if (values.length < this.mValueCount) {
            throw new ArrayStoreException();
        } else if (blend == null || blend.length >= 4) {
            nativeSetKeyFrame(this.native_instance, index, msec, values, blend);
        } else {
            throw new ArrayStoreException();
        }
    }

    public void setRepeatMirror(float repeatCount, boolean mirror) {
        if (repeatCount >= TonemapCurve.LEVEL_BLACK) {
            nativeSetRepeatMirror(this.native_instance, repeatCount, mirror);
        }
    }

    public Result timeToValues(float[] values) {
        return timeToValues((int) SystemClock.uptimeMillis(), values);
    }

    public Result timeToValues(int msec, float[] values) {
        if (values == null || values.length >= this.mValueCount) {
            switch (nativeTimeToValues(this.native_instance, msec, values)) {
                case 0:
                    return Result.NORMAL;
                case 1:
                    return Result.FREEZE_START;
                default:
                    return Result.FREEZE_END;
            }
        }
        throw new ArrayStoreException();
    }

    protected void finalize() throws Throwable {
        nativeDestructor(this.native_instance);
        this.native_instance = 0;
    }
}
