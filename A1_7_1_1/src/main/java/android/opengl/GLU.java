package android.opengl;

import android.hardware.camera2.params.TonemapCurve;
import javax.microedition.khronos.opengles.GL10;

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
public class GLU {
    private static final float[] sScratch = null;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.opengl.GLU.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.opengl.GLU.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.opengl.GLU.<clinit>():void");
    }

    public static String gluErrorString(int error) {
        switch (error) {
            case 0:
                return "no error";
            case 1280:
                return "invalid enum";
            case 1281:
                return "invalid value";
            case 1282:
                return "invalid operation";
            case 1283:
                return "stack overflow";
            case 1284:
                return "stack underflow";
            case 1285:
                return "out of memory";
            default:
                return null;
        }
    }

    public static void gluLookAt(GL10 gl, float eyeX, float eyeY, float eyeZ, float centerX, float centerY, float centerZ, float upX, float upY, float upZ) {
        float[] scratch = sScratch;
        synchronized (scratch) {
            Matrix.setLookAtM(scratch, 0, eyeX, eyeY, eyeZ, centerX, centerY, centerZ, upX, upY, upZ);
            gl.glMultMatrixf(scratch, 0);
        }
    }

    public static void gluOrtho2D(GL10 gl, float left, float right, float bottom, float top) {
        gl.glOrthof(left, right, bottom, top, -1.0f, 1.0f);
    }

    public static void gluPerspective(GL10 gl, float fovy, float aspect, float zNear, float zFar) {
        float top = zNear * ((float) Math.tan(((double) fovy) * 0.008726646259971648d));
        float bottom = -top;
        gl.glFrustumf(bottom * aspect, top * aspect, bottom, top, zNear, zFar);
    }

    public static int gluProject(float objX, float objY, float objZ, float[] model, int modelOffset, float[] project, int projectOffset, int[] view, int viewOffset, float[] win, int winOffset) {
        float[] scratch = sScratch;
        synchronized (scratch) {
            Matrix.multiplyMM(scratch, 0, project, projectOffset, model, modelOffset);
            scratch[16] = objX;
            scratch[17] = objY;
            scratch[18] = objZ;
            scratch[19] = 1.0f;
            Matrix.multiplyMV(scratch, 20, scratch, 0, scratch, 16);
            float w = scratch[23];
            if (w == TonemapCurve.LEVEL_BLACK) {
                return 0;
            }
            float rw = 1.0f / w;
            win[winOffset] = ((float) view[viewOffset]) + ((((float) view[viewOffset + 2]) * ((scratch[20] * rw) + 1.0f)) * 0.5f);
            win[winOffset + 1] = ((float) view[viewOffset + 1]) + ((((float) view[viewOffset + 3]) * ((scratch[21] * rw) + 1.0f)) * 0.5f);
            win[winOffset + 2] = ((scratch[22] * rw) + 1.0f) * 0.5f;
            return 1;
        }
    }

    public static int gluUnProject(float winX, float winY, float winZ, float[] model, int modelOffset, float[] project, int projectOffset, int[] view, int viewOffset, float[] obj, int objOffset) {
        float[] scratch = sScratch;
        synchronized (scratch) {
            Matrix.multiplyMM(scratch, 0, project, projectOffset, model, modelOffset);
            if (Matrix.invertM(scratch, 16, scratch, 0)) {
                scratch[0] = (((winX - ((float) view[viewOffset + 0])) * 2.0f) / ((float) view[viewOffset + 2])) - 1.0f;
                scratch[1] = (((winY - ((float) view[viewOffset + 1])) * 2.0f) / ((float) view[viewOffset + 3])) - 1.0f;
                scratch[2] = (2.0f * winZ) - 1.0f;
                scratch[3] = 1.0f;
                Matrix.multiplyMV(obj, objOffset, scratch, 16, scratch, 0);
                return 1;
            }
            return 0;
        }
    }
}
