package com.android.server.display;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.display.DisplayManagerInternal;
import android.hardware.display.DisplayManagerInternal.DisplayTransactionListener;
import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLSurface;
import android.opengl.GLES20;
import android.util.Slog;
import android.view.DisplayInfo;
import android.view.Surface;
import android.view.Surface.OutOfResourcesException;
import android.view.SurfaceControl;
import android.view.SurfaceSession;
import com.android.server.LocalServices;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import libcore.io.Streams;

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
final class ColorFade {
    private static final int COLOR_FADE_LAYER = 1073741825;
    private static final boolean DEBUG = false;
    private static final int DEJANK_FRAMES = 3;
    public static final int MODE_COOL_DOWN = 1;
    public static final int MODE_FADE = 2;
    public static final int MODE_WARM_UP = 0;
    private static final String TAG = "ColorFade";
    private boolean mCreatedResources;
    private int mDisplayHeight;
    private final int mDisplayId;
    private int mDisplayLayerStack;
    private final DisplayManagerInternal mDisplayManagerInternal;
    private int mDisplayWidth;
    private EGLConfig mEglConfig;
    private EGLContext mEglContext;
    private EGLDisplay mEglDisplay;
    private EGLSurface mEglSurface;
    private final int[] mGLBuffers;
    private int mGammaLoc;
    private int mMode;
    private int mOpacityLoc;
    private boolean mPrepared;
    private int mProgram;
    private final float[] mProjMatrix;
    private int mProjMatrixLoc;
    private int mSaturationLoc;
    private int mScaleLoc;
    private Surface mSurface;
    private float mSurfaceAlpha;
    private SurfaceControl mSurfaceControl;
    private NaturalSurfaceLayout mSurfaceLayout;
    private SurfaceSession mSurfaceSession;
    private boolean mSurfaceVisible;
    private final FloatBuffer mTexCoordBuffer;
    private int mTexCoordLoc;
    private final float[] mTexMatrix;
    private int mTexMatrixLoc;
    private final int[] mTexNames;
    private boolean mTexNamesGenerated;
    private int mTexUnitLoc;
    private final FloatBuffer mVertexBuffer;
    private int mVertexLoc;

    private static final class NaturalSurfaceLayout implements DisplayTransactionListener {
        private final int mDisplayId;
        private final DisplayManagerInternal mDisplayManagerInternal;
        private SurfaceControl mSurfaceControl;

        public NaturalSurfaceLayout(DisplayManagerInternal displayManagerInternal, int displayId, SurfaceControl surfaceControl) {
            this.mDisplayManagerInternal = displayManagerInternal;
            this.mDisplayId = displayId;
            this.mSurfaceControl = surfaceControl;
            this.mDisplayManagerInternal.registerDisplayTransactionListener(this);
        }

        public void dispose() {
            synchronized (this) {
                this.mSurfaceControl = null;
            }
            this.mDisplayManagerInternal.unregisterDisplayTransactionListener(this);
        }

        /* JADX WARNING: Missing block: B:10:0x0015, code:
            return;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void onDisplayTransaction() {
            synchronized (this) {
                if (this.mSurfaceControl != null) {
                    DisplayInfo displayInfo = this.mDisplayManagerInternal.getDisplayInfo(this.mDisplayId);
                    switch (displayInfo.rotation) {
                        case 0:
                            this.mSurfaceControl.setPosition(OppoBrightUtils.MIN_LUX_LIMITI, OppoBrightUtils.MIN_LUX_LIMITI);
                            this.mSurfaceControl.setMatrix(1.0f, OppoBrightUtils.MIN_LUX_LIMITI, OppoBrightUtils.MIN_LUX_LIMITI, 1.0f);
                            break;
                        case 1:
                            this.mSurfaceControl.setPosition(OppoBrightUtils.MIN_LUX_LIMITI, (float) displayInfo.logicalHeight);
                            this.mSurfaceControl.setMatrix(OppoBrightUtils.MIN_LUX_LIMITI, -1.0f, 1.0f, OppoBrightUtils.MIN_LUX_LIMITI);
                            break;
                        case 2:
                            this.mSurfaceControl.setPosition((float) displayInfo.logicalWidth, (float) displayInfo.logicalHeight);
                            this.mSurfaceControl.setMatrix(-1.0f, OppoBrightUtils.MIN_LUX_LIMITI, OppoBrightUtils.MIN_LUX_LIMITI, -1.0f);
                            break;
                        case 3:
                            this.mSurfaceControl.setPosition((float) displayInfo.logicalWidth, OppoBrightUtils.MIN_LUX_LIMITI);
                            this.mSurfaceControl.setMatrix(OppoBrightUtils.MIN_LUX_LIMITI, 1.0f, -1.0f, OppoBrightUtils.MIN_LUX_LIMITI);
                            break;
                    }
                }
            }
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.server.display.ColorFade.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.server.display.ColorFade.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.display.ColorFade.<clinit>():void");
    }

    public ColorFade(int displayId) {
        this.mTexNames = new int[1];
        this.mTexMatrix = new float[16];
        this.mProjMatrix = new float[16];
        this.mGLBuffers = new int[2];
        this.mVertexBuffer = createNativeFloatBuffer(8);
        this.mTexCoordBuffer = createNativeFloatBuffer(8);
        this.mDisplayId = displayId;
        this.mDisplayManagerInternal = (DisplayManagerInternal) LocalServices.getService(DisplayManagerInternal.class);
    }

    public boolean prepare(Context context, int mode) {
        boolean captureScreenshotTextureAndSetViewport;
        if (DEBUG) {
            Slog.d(TAG, "prepare: mode=" + mode);
        }
        this.mMode = mode;
        DisplayInfo displayInfo = this.mDisplayManagerInternal.getDisplayInfo(this.mDisplayId);
        this.mDisplayLayerStack = displayInfo.layerStack;
        this.mDisplayWidth = displayInfo.getNaturalWidth();
        this.mDisplayHeight = displayInfo.getNaturalHeight();
        if (createSurface() && createEglContext() && createEglSurface()) {
            captureScreenshotTextureAndSetViewport = captureScreenshotTextureAndSetViewport();
        } else {
            captureScreenshotTextureAndSetViewport = false;
        }
        if (!captureScreenshotTextureAndSetViewport) {
            dismiss();
            return false;
        } else if (!attachEglContext()) {
            return false;
        } else {
            try {
                if (initGLShaders(context) && initGLBuffers() && !checkGlErrors("prepare")) {
                    detachEglContext();
                    this.mCreatedResources = true;
                    this.mPrepared = true;
                    if (mode == 1) {
                        for (int i = 0; i < 3; i++) {
                            draw(1.0f);
                        }
                    }
                    return true;
                }
                detachEglContext();
                dismiss();
                detachEglContext();
                return false;
            } catch (Throwable th) {
                detachEglContext();
            }
        }
    }

    private String readFile(Context context, int resourceId) {
        try {
            return new String(Streams.readFully(new InputStreamReader(context.getResources().openRawResource(resourceId))));
        } catch (IOException e) {
            Slog.e(TAG, "Unrecognized shader " + Integer.toString(resourceId));
            throw new RuntimeException(e);
        }
    }

    private int loadShader(Context context, int resourceId, int type) {
        String source = readFile(context, resourceId);
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, source);
        GLES20.glCompileShader(shader);
        int[] compiled = new int[1];
        GLES20.glGetShaderiv(shader, 35713, compiled, 0);
        if (compiled[0] != 0) {
            return shader;
        }
        Slog.e(TAG, "Could not compile shader " + shader + ", " + type + ":");
        Slog.e(TAG, GLES20.glGetShaderSource(shader));
        Slog.e(TAG, GLES20.glGetShaderInfoLog(shader));
        GLES20.glDeleteShader(shader);
        return 0;
    }

    private boolean initGLShaders(Context context) {
        int vshader = loadShader(context, 17825796, 35633);
        int fshader = loadShader(context, 17825795, 35632);
        GLES20.glReleaseShaderCompiler();
        if (vshader == 0 || fshader == 0) {
            return false;
        }
        this.mProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(this.mProgram, vshader);
        GLES20.glAttachShader(this.mProgram, fshader);
        GLES20.glDeleteShader(vshader);
        GLES20.glDeleteShader(fshader);
        GLES20.glLinkProgram(this.mProgram);
        this.mVertexLoc = GLES20.glGetAttribLocation(this.mProgram, "position");
        this.mTexCoordLoc = GLES20.glGetAttribLocation(this.mProgram, "uv");
        this.mProjMatrixLoc = GLES20.glGetUniformLocation(this.mProgram, "proj_matrix");
        this.mTexMatrixLoc = GLES20.glGetUniformLocation(this.mProgram, "tex_matrix");
        this.mOpacityLoc = GLES20.glGetUniformLocation(this.mProgram, "opacity");
        this.mGammaLoc = GLES20.glGetUniformLocation(this.mProgram, "gamma");
        this.mSaturationLoc = GLES20.glGetUniformLocation(this.mProgram, "saturation");
        this.mScaleLoc = GLES20.glGetUniformLocation(this.mProgram, "scale");
        this.mTexUnitLoc = GLES20.glGetUniformLocation(this.mProgram, "texUnit");
        GLES20.glUseProgram(this.mProgram);
        GLES20.glUniform1i(this.mTexUnitLoc, 0);
        GLES20.glUseProgram(0);
        return true;
    }

    private void destroyGLShaders() {
        GLES20.glDeleteProgram(this.mProgram);
        checkGlErrors("glDeleteProgram");
    }

    private boolean initGLBuffers() {
        setQuad(this.mVertexBuffer, OppoBrightUtils.MIN_LUX_LIMITI, OppoBrightUtils.MIN_LUX_LIMITI, (float) this.mDisplayWidth, (float) this.mDisplayHeight);
        GLES20.glBindTexture(36197, this.mTexNames[0]);
        GLES20.glTexParameteri(36197, 10240, 9728);
        GLES20.glTexParameteri(36197, 10241, 9728);
        GLES20.glTexParameteri(36197, 10242, 33071);
        GLES20.glTexParameteri(36197, 10243, 33071);
        GLES20.glBindTexture(36197, 0);
        GLES20.glGenBuffers(2, this.mGLBuffers, 0);
        GLES20.glBindBuffer(34962, this.mGLBuffers[0]);
        GLES20.glBufferData(34962, this.mVertexBuffer.capacity() * 4, this.mVertexBuffer, 35044);
        GLES20.glBindBuffer(34962, this.mGLBuffers[1]);
        GLES20.glBufferData(34962, this.mTexCoordBuffer.capacity() * 4, this.mTexCoordBuffer, 35044);
        GLES20.glBindBuffer(34962, 0);
        return true;
    }

    private void destroyGLBuffers() {
        GLES20.glDeleteBuffers(2, this.mGLBuffers, 0);
        checkGlErrors("glDeleteBuffers");
    }

    private static void setQuad(FloatBuffer vtx, float x, float y, float w, float h) {
        if (DEBUG) {
            Slog.d(TAG, "setQuad: x=" + x + ", y=" + y + ", w=" + w + ", h=" + h);
        }
        vtx.put(0, x);
        vtx.put(1, y);
        vtx.put(2, x);
        vtx.put(3, y + h);
        vtx.put(4, x + w);
        vtx.put(5, y + h);
        vtx.put(6, x + w);
        vtx.put(7, y);
    }

    public void dismissResources() {
        if (DEBUG) {
            Slog.d(TAG, "dismissResources");
        }
        if (this.mCreatedResources) {
            attachEglContext();
            try {
                destroyScreenshotTexture();
                destroyGLShaders();
                destroyGLBuffers();
                destroyEglSurface();
                GLES20.glFlush();
                this.mCreatedResources = false;
            } finally {
                detachEglContext();
            }
        }
    }

    public void dismiss() {
        if (DEBUG) {
            Slog.d(TAG, "dismiss");
        }
        if (this.mPrepared) {
            dismissResources();
            destroySurface();
            this.mPrepared = false;
        }
    }

    public boolean draw(float level) {
        if (DEBUG) {
            Slog.d(TAG, "drawFrame: level=" + level);
        }
        if (!this.mPrepared) {
            return false;
        }
        if (this.mMode == 2) {
            return showSurface(1.0f - level);
        }
        if (!attachEglContext()) {
            return false;
        }
        try {
            GLES20.glClearColor(OppoBrightUtils.MIN_LUX_LIMITI, OppoBrightUtils.MIN_LUX_LIMITI, OppoBrightUtils.MIN_LUX_LIMITI, 1.0f);
            GLES20.glClear(16384);
            double one_minus_level = (double) (1.0f - level);
            double cos = Math.cos(3.141592653589793d * one_minus_level);
            drawFaded(((float) (-Math.pow(one_minus_level, 2.0d))) + 1.0f, 1.0f / ((float) (((((0.5d * ((double) (cos < 0.0d ? -1 : 1))) * Math.pow(cos, 2.0d)) + 0.5d) * 0.9d) + 0.1d)), (float) Math.pow((double) level, 4.0d), (float) ((((-Math.pow(one_minus_level, 2.0d)) + 1.0d) * 0.1d) + 0.9d));
            if (checkGlErrors("drawFrame")) {
                return false;
            }
            EGL14.eglSwapBuffers(this.mEglDisplay, this.mEglSurface);
            detachEglContext();
            return showSurface(1.0f);
        } finally {
            detachEglContext();
        }
    }

    private void drawFaded(float opacity, float gamma, float saturation, float scale) {
        if (DEBUG) {
            Slog.d(TAG, "drawFaded: opacity=" + opacity + ", gamma=" + gamma + ", saturation=" + saturation + ", scale=" + scale);
        }
        GLES20.glUseProgram(this.mProgram);
        GLES20.glUniformMatrix4fv(this.mProjMatrixLoc, 1, false, this.mProjMatrix, 0);
        GLES20.glUniformMatrix4fv(this.mTexMatrixLoc, 1, false, this.mTexMatrix, 0);
        GLES20.glUniform1f(this.mOpacityLoc, opacity);
        GLES20.glUniform1f(this.mGammaLoc, gamma);
        GLES20.glUniform1f(this.mSaturationLoc, saturation);
        GLES20.glUniform1f(this.mScaleLoc, scale);
        GLES20.glActiveTexture(33984);
        GLES20.glBindTexture(36197, this.mTexNames[0]);
        GLES20.glBindBuffer(34962, this.mGLBuffers[0]);
        GLES20.glEnableVertexAttribArray(this.mVertexLoc);
        GLES20.glVertexAttribPointer(this.mVertexLoc, 2, 5126, false, 0, 0);
        GLES20.glBindBuffer(34962, this.mGLBuffers[1]);
        GLES20.glEnableVertexAttribArray(this.mTexCoordLoc);
        GLES20.glVertexAttribPointer(this.mTexCoordLoc, 2, 5126, false, 0, 0);
        GLES20.glDrawArrays(6, 0, 4);
        GLES20.glBindTexture(36197, 0);
        GLES20.glBindBuffer(34962, 0);
    }

    private void ortho(float left, float right, float bottom, float top, float znear, float zfar) {
        this.mProjMatrix[0] = 2.0f / (right - left);
        this.mProjMatrix[1] = OppoBrightUtils.MIN_LUX_LIMITI;
        this.mProjMatrix[2] = OppoBrightUtils.MIN_LUX_LIMITI;
        this.mProjMatrix[3] = OppoBrightUtils.MIN_LUX_LIMITI;
        this.mProjMatrix[4] = OppoBrightUtils.MIN_LUX_LIMITI;
        this.mProjMatrix[5] = 2.0f / (top - bottom);
        this.mProjMatrix[6] = OppoBrightUtils.MIN_LUX_LIMITI;
        this.mProjMatrix[7] = OppoBrightUtils.MIN_LUX_LIMITI;
        this.mProjMatrix[8] = OppoBrightUtils.MIN_LUX_LIMITI;
        this.mProjMatrix[9] = OppoBrightUtils.MIN_LUX_LIMITI;
        this.mProjMatrix[10] = -2.0f / (zfar - znear);
        this.mProjMatrix[11] = OppoBrightUtils.MIN_LUX_LIMITI;
        this.mProjMatrix[12] = (-(right + left)) / (right - left);
        this.mProjMatrix[13] = (-(top + bottom)) / (top - bottom);
        this.mProjMatrix[14] = (-(zfar + znear)) / (zfar - znear);
        this.mProjMatrix[15] = 1.0f;
    }

    private boolean captureScreenshotTextureAndSetViewport() {
        if (!attachEglContext()) {
            return false;
        }
        SurfaceTexture st;
        Surface s;
        try {
            if (!this.mTexNamesGenerated) {
                GLES20.glGenTextures(1, this.mTexNames, 0);
                if (checkGlErrors("glGenTextures")) {
                    detachEglContext();
                    return false;
                }
                this.mTexNamesGenerated = true;
            }
            st = new SurfaceTexture(this.mTexNames[0]);
            s = new Surface(st);
            SurfaceControl.screenshot(SurfaceControl.getBuiltInDisplay(0), s);
            st.updateTexImage();
            st.getTransformMatrix(this.mTexMatrix);
            s.release();
            st.release();
            this.mTexCoordBuffer.put(0, OppoBrightUtils.MIN_LUX_LIMITI);
            this.mTexCoordBuffer.put(1, OppoBrightUtils.MIN_LUX_LIMITI);
            this.mTexCoordBuffer.put(2, OppoBrightUtils.MIN_LUX_LIMITI);
            this.mTexCoordBuffer.put(3, 1.0f);
            this.mTexCoordBuffer.put(4, 1.0f);
            this.mTexCoordBuffer.put(5, 1.0f);
            this.mTexCoordBuffer.put(6, 1.0f);
            this.mTexCoordBuffer.put(7, OppoBrightUtils.MIN_LUX_LIMITI);
            GLES20.glViewport(0, 0, this.mDisplayWidth, this.mDisplayHeight);
            ortho(OppoBrightUtils.MIN_LUX_LIMITI, (float) this.mDisplayWidth, OppoBrightUtils.MIN_LUX_LIMITI, (float) this.mDisplayHeight, -1.0f, 1.0f);
            detachEglContext();
            return true;
        } catch (Throwable th) {
            detachEglContext();
        }
    }

    private void destroyScreenshotTexture() {
        if (this.mTexNamesGenerated) {
            this.mTexNamesGenerated = false;
            GLES20.glDeleteTextures(1, this.mTexNames, 0);
            checkGlErrors("glDeleteTextures");
        }
    }

    private boolean createEglContext() {
        if (this.mEglDisplay == null) {
            this.mEglDisplay = EGL14.eglGetDisplay(0);
            if (this.mEglDisplay == EGL14.EGL_NO_DISPLAY) {
                logEglError("eglGetDisplay");
                return false;
            }
            int[] version = new int[2];
            if (!EGL14.eglInitialize(this.mEglDisplay, version, 0, version, 1)) {
                this.mEglDisplay = null;
                logEglError("eglInitialize");
                return false;
            }
        }
        if (this.mEglConfig == null) {
            EGLConfig[] eglConfigs = new EGLConfig[1];
            if (EGL14.eglChooseConfig(this.mEglDisplay, new int[]{12352, 4, 12324, 8, 12323, 8, 12322, 8, 12321, 8, 12344}, 0, eglConfigs, 0, eglConfigs.length, new int[1], 0)) {
                this.mEglConfig = eglConfigs[0];
            } else {
                logEglError("eglChooseConfig");
                return false;
            }
        }
        if (this.mEglContext == null) {
            this.mEglContext = EGL14.eglCreateContext(this.mEglDisplay, this.mEglConfig, EGL14.EGL_NO_CONTEXT, new int[]{12440, 2, 12344}, 0);
            if (this.mEglContext == null) {
                logEglError("eglCreateContext");
                return false;
            }
        }
        return true;
    }

    private boolean createSurface() {
        if (this.mSurfaceSession == null) {
            this.mSurfaceSession = new SurfaceSession();
        }
        SurfaceControl.openTransaction();
        try {
            if (this.mSurfaceControl == null) {
                int flags;
                if (this.mMode == 2) {
                    flags = 131076;
                } else {
                    flags = 1028;
                }
                this.mSurfaceControl = new SurfaceControl(this.mSurfaceSession, TAG, this.mDisplayWidth, this.mDisplayHeight, -1, flags);
                this.mSurfaceControl.setLayerStack(this.mDisplayLayerStack);
                this.mSurfaceControl.setSize(this.mDisplayWidth, this.mDisplayHeight);
                this.mSurface = new Surface();
                this.mSurface.copyFrom(this.mSurfaceControl);
                this.mSurfaceLayout = new NaturalSurfaceLayout(this.mDisplayManagerInternal, this.mDisplayId, this.mSurfaceControl);
                this.mSurfaceLayout.onDisplayTransaction();
            }
            SurfaceControl.closeTransaction();
            return true;
        } catch (OutOfResourcesException ex) {
            Slog.e(TAG, "Unable to create surface.", ex);
            SurfaceControl.closeTransaction();
            return false;
        } catch (Throwable th) {
            SurfaceControl.closeTransaction();
            throw th;
        }
    }

    private boolean createEglSurface() {
        if (this.mEglSurface == null) {
            int[] eglSurfaceAttribList = new int[1];
            eglSurfaceAttribList[0] = 12344;
            this.mEglSurface = EGL14.eglCreateWindowSurface(this.mEglDisplay, this.mEglConfig, this.mSurface, eglSurfaceAttribList, 0);
            if (this.mEglSurface == null) {
                logEglError("eglCreateWindowSurface");
                return false;
            }
        }
        return true;
    }

    private void destroyEglSurface() {
        if (this.mEglSurface != null) {
            if (!EGL14.eglDestroySurface(this.mEglDisplay, this.mEglSurface)) {
                logEglError("eglDestroySurface");
            }
            this.mEglSurface = null;
        }
    }

    private void destroySurface() {
        if (this.mSurfaceControl != null) {
            this.mSurfaceLayout.dispose();
            this.mSurfaceLayout = null;
            SurfaceControl.openTransaction();
            try {
                this.mSurfaceControl.destroy();
                this.mSurface.release();
                this.mSurfaceControl = null;
                this.mSurfaceVisible = false;
                this.mSurfaceAlpha = OppoBrightUtils.MIN_LUX_LIMITI;
            } finally {
                SurfaceControl.closeTransaction();
            }
        }
    }

    private boolean showSurface(float alpha) {
        if (!(this.mSurfaceVisible && this.mSurfaceAlpha == alpha)) {
            SurfaceControl.openTransaction();
            try {
                this.mSurfaceControl.setLayer(COLOR_FADE_LAYER);
                this.mSurfaceControl.setAlpha(alpha);
                this.mSurfaceControl.show();
                this.mSurfaceVisible = true;
                this.mSurfaceAlpha = alpha;
            } finally {
                SurfaceControl.closeTransaction();
            }
        }
        return true;
    }

    private boolean attachEglContext() {
        if (this.mEglSurface == null) {
            return false;
        }
        if (EGL14.eglMakeCurrent(this.mEglDisplay, this.mEglSurface, this.mEglSurface, this.mEglContext)) {
            return true;
        }
        logEglError("eglMakeCurrent");
        return false;
    }

    private void detachEglContext() {
        if (this.mEglDisplay != null) {
            EGL14.eglMakeCurrent(this.mEglDisplay, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT);
        }
    }

    private static FloatBuffer createNativeFloatBuffer(int size) {
        ByteBuffer bb = ByteBuffer.allocateDirect(size * 4);
        bb.order(ByteOrder.nativeOrder());
        return bb.asFloatBuffer();
    }

    private static void logEglError(String func) {
        Slog.e(TAG, func + " failed: error " + EGL14.eglGetError(), new Throwable());
    }

    private static boolean checkGlErrors(String func) {
        return checkGlErrors(func, true);
    }

    private static boolean checkGlErrors(String func, boolean log) {
        boolean hadError = false;
        while (true) {
            int error = GLES20.glGetError();
            if (error == 0) {
                return hadError;
            }
            if (log) {
                Slog.e(TAG, func + " failed: error " + error, new Throwable());
            }
            hadError = true;
        }
    }

    public void dump(PrintWriter pw) {
        pw.println();
        pw.println("Color Fade State:");
        pw.println("  mPrepared=" + this.mPrepared);
        pw.println("  mMode=" + this.mMode);
        pw.println("  mDisplayLayerStack=" + this.mDisplayLayerStack);
        pw.println("  mDisplayWidth=" + this.mDisplayWidth);
        pw.println("  mDisplayHeight=" + this.mDisplayHeight);
        pw.println("  mSurfaceVisible=" + this.mSurfaceVisible);
        pw.println("  mSurfaceAlpha=" + this.mSurfaceAlpha);
    }
}
