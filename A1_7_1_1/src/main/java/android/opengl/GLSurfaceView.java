package android.opengl;

import android.content.Context;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.SystemProperties;
import android.os.Trace;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback2;
import android.view.SurfaceView;
import com.mediatek.perfservice.IPerfServiceWrapper;
import com.mediatek.perfservice.PerfServiceWrapper;
import java.io.Writer;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;
import javax.microedition.khronos.opengles.GL;
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
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:42)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
/*  JADX ERROR: NullPointerException in pass: ClassModifier
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ClassModifier.removeFieldUsageFromConstructor(ClassModifier.java:100)
    	at jadx.core.dex.visitors.ClassModifier.removeSyntheticFields(ClassModifier.java:75)
    	at jadx.core.dex.visitors.ClassModifier.visit(ClassModifier.java:48)
    	at jadx.core.dex.visitors.ClassModifier.visit(ClassModifier.java:40)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class GLSurfaceView extends SurfaceView implements Callback2 {
    private static final int DBG_ATTACH_DETACH = 1;
    private static final int DBG_EGL = 64;
    private static final int DBG_PAUSE_RESUME = 4;
    private static final int DBG_RENDERER = 16;
    private static final int DBG_RENDERER_DRAW_FRAME = 32;
    private static final int DBG_SURFACE = 8;
    private static final int DBG_THREADS = 2;
    public static final int DEBUG_CHECK_GL_ERROR = 1;
    public static final int DEBUG_LOG_GL_CALLS = 2;
    private static boolean LOG_ATTACH_DETACH = false;
    private static boolean LOG_EGL = false;
    private static boolean LOG_PAUSE_RESUME = false;
    private static final String LOG_PROPERTY_NAME = "debug.glsurfaceview.dumpinfo";
    private static boolean LOG_RENDERER = false;
    private static boolean LOG_RENDERER_DRAW_FRAME = false;
    private static boolean LOG_SURFACE = false;
    private static boolean LOG_THREADS = false;
    public static final int RENDERMODE_CONTINUOUSLY = 1;
    public static final int RENDERMODE_WHEN_DIRTY = 0;
    private static final String TAG = "GLSurfaceView";
    private static final GLThreadManager sGLThreadManager = null;
    private int mDebugFlags;
    private boolean mDetached;
    private EGLConfigChooser mEGLConfigChooser;
    private int mEGLContextClientVersion;
    private EGLContextFactory mEGLContextFactory;
    private EGLWindowSurfaceFactory mEGLWindowSurfaceFactory;
    private GLThread mGLThread;
    private GLWrapper mGLWrapper;
    private boolean mPreserveEGLContextOnPause;
    private Renderer mRenderer;
    private final WeakReference<GLSurfaceView> mThisWeakRef;

    public interface EGLConfigChooser {
        EGLConfig chooseConfig(EGL10 egl10, EGLDisplay eGLDisplay);
    }

    private abstract class BaseConfigChooser implements EGLConfigChooser {
        protected int[] mConfigSpec;

        abstract EGLConfig chooseConfig(EGL10 egl10, EGLDisplay eGLDisplay, EGLConfig[] eGLConfigArr);

        public BaseConfigChooser(int[] configSpec) {
            this.mConfigSpec = filterConfigSpec(configSpec);
        }

        public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display) {
            int[] num_config = new int[1];
            if (egl.eglChooseConfig(display, this.mConfigSpec, null, 0, num_config)) {
                int numConfigs = num_config[0];
                if (numConfigs <= 0) {
                    throw new IllegalArgumentException("No configs match configSpec");
                }
                EGLConfig[] configs = new EGLConfig[numConfigs];
                if (egl.eglChooseConfig(display, this.mConfigSpec, configs, numConfigs, num_config)) {
                    EGLConfig config = chooseConfig(egl, display, configs);
                    if (config != null) {
                        return config;
                    }
                    throw new IllegalArgumentException("No config chosen");
                }
                throw new IllegalArgumentException("eglChooseConfig#2 failed");
            }
            throw new IllegalArgumentException("eglChooseConfig failed");
        }

        private int[] filterConfigSpec(int[] configSpec) {
            if (GLSurfaceView.this.mEGLContextClientVersion != 2 && GLSurfaceView.this.mEGLContextClientVersion != 3) {
                return configSpec;
            }
            int len = configSpec.length;
            int[] newConfigSpec = new int[(len + 2)];
            System.arraycopy(configSpec, 0, newConfigSpec, 0, len - 1);
            newConfigSpec[len - 1] = EGL14.EGL_RENDERABLE_TYPE;
            if (GLSurfaceView.this.mEGLContextClientVersion == 2) {
                newConfigSpec[len] = 4;
            } else {
                newConfigSpec[len] = 64;
            }
            newConfigSpec[len + 1] = EGL14.EGL_NONE;
            return newConfigSpec;
        }
    }

    private class ComponentSizeChooser extends BaseConfigChooser {
        protected int mAlphaSize;
        protected int mBlueSize;
        protected int mDepthSize;
        protected int mGreenSize;
        protected int mRedSize;
        protected int mStencilSize;
        private int[] mValue = new int[1];

        public ComponentSizeChooser(int redSize, int greenSize, int blueSize, int alphaSize, int depthSize, int stencilSize) {
            int[] iArr = new int[13];
            iArr[0] = EGL14.EGL_RED_SIZE;
            iArr[1] = redSize;
            iArr[2] = EGL14.EGL_GREEN_SIZE;
            iArr[3] = greenSize;
            iArr[4] = EGL14.EGL_BLUE_SIZE;
            iArr[5] = blueSize;
            iArr[6] = EGL14.EGL_ALPHA_SIZE;
            iArr[7] = alphaSize;
            iArr[8] = EGL14.EGL_DEPTH_SIZE;
            iArr[9] = depthSize;
            iArr[10] = EGL14.EGL_STENCIL_SIZE;
            iArr[11] = stencilSize;
            iArr[12] = EGL14.EGL_NONE;
            super(iArr);
            this.mRedSize = redSize;
            this.mGreenSize = greenSize;
            this.mBlueSize = blueSize;
            this.mAlphaSize = alphaSize;
            this.mDepthSize = depthSize;
            this.mStencilSize = stencilSize;
        }

        public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display, EGLConfig[] configs) {
            for (EGLConfig config : configs) {
                int d = findConfigAttrib(egl, display, config, EGL14.EGL_DEPTH_SIZE, 0);
                int s = findConfigAttrib(egl, display, config, EGL14.EGL_STENCIL_SIZE, 0);
                if (d >= this.mDepthSize && s >= this.mStencilSize) {
                    int r = findConfigAttrib(egl, display, config, EGL14.EGL_RED_SIZE, 0);
                    int g = findConfigAttrib(egl, display, config, EGL14.EGL_GREEN_SIZE, 0);
                    int b = findConfigAttrib(egl, display, config, EGL14.EGL_BLUE_SIZE, 0);
                    int a = findConfigAttrib(egl, display, config, EGL14.EGL_ALPHA_SIZE, 0);
                    if (r == this.mRedSize && g == this.mGreenSize && b == this.mBlueSize && a == this.mAlphaSize) {
                        return config;
                    }
                }
            }
            return null;
        }

        private int findConfigAttrib(EGL10 egl, EGLDisplay display, EGLConfig config, int attribute, int defaultValue) {
            if (egl.eglGetConfigAttrib(display, config, attribute, this.mValue)) {
                return this.mValue[0];
            }
            return defaultValue;
        }
    }

    public interface EGLContextFactory {
        EGLContext createContext(EGL10 egl10, EGLDisplay eGLDisplay, EGLConfig eGLConfig);

        void destroyContext(EGL10 egl10, EGLDisplay eGLDisplay, EGLContext eGLContext);
    }

    private class DefaultContextFactory implements EGLContextFactory {
        private int EGL_CONTEXT_CLIENT_VERSION;

        /* synthetic */ DefaultContextFactory(GLSurfaceView this$0, DefaultContextFactory defaultContextFactory) {
            this();
        }

        private DefaultContextFactory() {
            this.EGL_CONTEXT_CLIENT_VERSION = 12440;
        }

        public EGLContext createContext(EGL10 egl, EGLDisplay display, EGLConfig config) {
            int[] attrib_list = new int[3];
            attrib_list[0] = this.EGL_CONTEXT_CLIENT_VERSION;
            attrib_list[1] = GLSurfaceView.this.mEGLContextClientVersion;
            attrib_list[2] = EGL14.EGL_NONE;
            Log.i("DefaultContextFactory", "createContext = " + Thread.currentThread().getId() + ", this = " + GLSurfaceView.this);
            EGLContext eGLContext = EGL10.EGL_NO_CONTEXT;
            if (GLSurfaceView.this.mEGLContextClientVersion == 0) {
                attrib_list = null;
            }
            return egl.eglCreateContext(display, config, eGLContext, attrib_list);
        }

        public void destroyContext(EGL10 egl, EGLDisplay display, EGLContext context) {
            Log.i("DefaultContextFactory", "eglDestroyContext = " + Thread.currentThread().getId() + ", this = " + GLSurfaceView.this);
            if (!egl.eglDestroyContext(display, context)) {
                Log.e("DefaultContextFactory", "display:" + display + " context: " + context);
                if (GLSurfaceView.LOG_THREADS) {
                    Log.i("DefaultContextFactory", "tid=" + Thread.currentThread().getId() + ", this = " + GLSurfaceView.this);
                }
                EglHelper.throwEglException("eglDestroyContex", egl.eglGetError());
            }
        }
    }

    public interface EGLWindowSurfaceFactory {
        EGLSurface createWindowSurface(EGL10 egl10, EGLDisplay eGLDisplay, EGLConfig eGLConfig, Object obj);

        void destroySurface(EGL10 egl10, EGLDisplay eGLDisplay, EGLSurface eGLSurface);
    }

    private static class DefaultWindowSurfaceFactory implements EGLWindowSurfaceFactory {
        /* synthetic */ DefaultWindowSurfaceFactory(DefaultWindowSurfaceFactory defaultWindowSurfaceFactory) {
            this();
        }

        private DefaultWindowSurfaceFactory() {
        }

        public EGLSurface createWindowSurface(EGL10 egl, EGLDisplay display, EGLConfig config, Object nativeWindow) {
            EGLSurface result = null;
            try {
                return egl.eglCreateWindowSurface(display, config, nativeWindow, null);
            } catch (IllegalArgumentException e) {
                Log.e("DefaultWindowSurfaceFactory", "eglCreateWindowSurface", e);
                return result;
            }
        }

        public void destroySurface(EGL10 egl, EGLDisplay display, EGLSurface surface) {
            egl.eglDestroySurface(display, surface);
        }
    }

    private static class EglHelper {
        EGL10 mEgl;
        EGLConfig mEglConfig;
        EGLContext mEglContext;
        EGLDisplay mEglDisplay;
        EGLSurface mEglSurface;
        private WeakReference<GLSurfaceView> mGLSurfaceViewWeakRef;

        public EglHelper(WeakReference<GLSurfaceView> glSurfaceViewWeakRef) {
            this.mGLSurfaceViewWeakRef = glSurfaceViewWeakRef;
        }

        public void start() {
            if (GLSurfaceView.LOG_EGL) {
                Log.w("EglHelper", "start() tid=" + Thread.currentThread().getId());
            }
            this.mEgl = (EGL10) EGLContext.getEGL();
            this.mEglDisplay = this.mEgl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);
            if (this.mEglDisplay == EGL10.EGL_NO_DISPLAY) {
                throw new RuntimeException("eglGetDisplay failed");
            }
            int[] version = new int[2];
            Log.i("EglHelper", "eglInitialize = " + Thread.currentThread().getId() + ", this = " + this.mGLSurfaceViewWeakRef.get());
            if (this.mEgl.eglInitialize(this.mEglDisplay, version)) {
                GLSurfaceView view = (GLSurfaceView) this.mGLSurfaceViewWeakRef.get();
                if (view == null) {
                    this.mEglConfig = null;
                    this.mEglContext = null;
                } else {
                    Log.i("EglHelper", "chooseConfig = " + Thread.currentThread().getId() + ", this = " + view);
                    this.mEglConfig = view.mEGLConfigChooser.chooseConfig(this.mEgl, this.mEglDisplay);
                    this.mEglContext = view.mEGLContextFactory.createContext(this.mEgl, this.mEglDisplay, this.mEglConfig);
                }
                if (this.mEglContext == null || this.mEglContext == EGL10.EGL_NO_CONTEXT) {
                    this.mEglContext = null;
                    throwEglException("createContext");
                }
                if (GLSurfaceView.LOG_EGL) {
                    Log.w("EglHelper", "createContext " + this.mEglContext + " tid=" + Thread.currentThread().getId());
                }
                this.mEglSurface = null;
                return;
            }
            throw new RuntimeException("eglInitialize failed");
        }

        public boolean createSurface() {
            if (GLSurfaceView.LOG_EGL) {
                Log.w("EglHelper", "createSurface()  tid=" + Thread.currentThread().getId());
            }
            if (this.mEgl == null) {
                throw new RuntimeException("egl not initialized");
            } else if (this.mEglDisplay == null) {
                throw new RuntimeException("eglDisplay not initialized");
            } else if (this.mEglConfig == null) {
                throw new RuntimeException("mEglConfig not initialized");
            } else {
                destroySurfaceImp();
                GLSurfaceView view = (GLSurfaceView) this.mGLSurfaceViewWeakRef.get();
                if (view != null) {
                    this.mEglSurface = view.mEGLWindowSurfaceFactory.createWindowSurface(this.mEgl, this.mEglDisplay, this.mEglConfig, view.getHolder());
                } else {
                    this.mEglSurface = null;
                }
                if (this.mEglSurface == null || this.mEglSurface == EGL10.EGL_NO_SURFACE) {
                    if (this.mEgl.eglGetError() == 12299) {
                        Log.e("EglHelper", "createWindowSurface returned EGL_BAD_NATIVE_WINDOW.");
                    }
                    return false;
                } else if (this.mEgl.eglMakeCurrent(this.mEglDisplay, this.mEglSurface, this.mEglSurface, this.mEglContext)) {
                    return true;
                } else {
                    logEglErrorAsWarning("EGLHelper", "eglMakeCurrent", this.mEgl.eglGetError());
                    return false;
                }
            }
        }

        GL createGL() {
            GL gl = this.mEglContext.getGL();
            GLSurfaceView view = (GLSurfaceView) this.mGLSurfaceViewWeakRef.get();
            if (view == null) {
                return gl;
            }
            if (view.mGLWrapper != null) {
                gl = view.mGLWrapper.wrap(gl);
            }
            if ((view.mDebugFlags & 3) == 0) {
                return gl;
            }
            int configFlags = 0;
            Writer writer = null;
            if ((view.mDebugFlags & 1) != 0) {
                configFlags = 1;
            }
            if ((view.mDebugFlags & 2) != 0) {
                writer = new LogWriter();
            }
            return GLDebugHelper.wrap(gl, configFlags, writer);
        }

        public int swap() {
            if (this.mEgl.eglSwapBuffers(this.mEglDisplay, this.mEglSurface)) {
                return 12288;
            }
            return this.mEgl.eglGetError();
        }

        public void destroySurface() {
            if (GLSurfaceView.LOG_EGL) {
                Log.w("EglHelper", "destroySurface()  tid=" + Thread.currentThread().getId());
            }
            destroySurfaceImp();
        }

        private void destroySurfaceImp() {
            if (this.mEglSurface != null && this.mEglSurface != EGL10.EGL_NO_SURFACE) {
                this.mEgl.eglMakeCurrent(this.mEglDisplay, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_CONTEXT);
                GLSurfaceView view = (GLSurfaceView) this.mGLSurfaceViewWeakRef.get();
                if (view != null) {
                    view.mEGLWindowSurfaceFactory.destroySurface(this.mEgl, this.mEglDisplay, this.mEglSurface);
                }
                this.mEglSurface = null;
            }
        }

        public void finish() {
            if (GLSurfaceView.LOG_EGL) {
                Log.w("EglHelper", "finish() tid=" + Thread.currentThread().getId());
            }
            if (this.mEglContext != null) {
                GLSurfaceView view = (GLSurfaceView) this.mGLSurfaceViewWeakRef.get();
                if (view != null) {
                    view.mEGLContextFactory.destroyContext(this.mEgl, this.mEglDisplay, this.mEglContext);
                }
                this.mEglContext = null;
            }
            if (this.mEglDisplay != null) {
                this.mEgl.eglTerminate(this.mEglDisplay);
                this.mEglDisplay = null;
            }
        }

        private void throwEglException(String function) {
            throwEglException(function, this.mEgl.eglGetError());
        }

        public static void throwEglException(String function, int error) {
            String message = formatEglError(function, error);
            if (GLSurfaceView.LOG_THREADS) {
                Log.e("EglHelper", "throwEglException tid=" + Thread.currentThread().getId() + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + message);
            }
            throw new RuntimeException(message);
        }

        public static void logEglErrorAsWarning(String tag, String function, int error) {
            Log.w(tag, formatEglError(function, error));
        }

        public static String formatEglError(String function, int error) {
            return function + " failed: " + EGLLogWrapper.getErrorString(error);
        }
    }

    static class GLThread extends Thread {
        private EglHelper mEglHelper;
        private ArrayList<Runnable> mEventQueue = new ArrayList();
        private boolean mExited;
        private boolean mFinishedCreatingEglSurface;
        private WeakReference<GLSurfaceView> mGLSurfaceViewWeakRef;
        private boolean mHasSurface;
        private boolean mHaveEglContext;
        private boolean mHaveEglSurface;
        private int mHeight = 0;
        private boolean mPaused;
        private IPerfServiceWrapper mPerfServiceWrapper;
        private boolean mRenderComplete;
        private int mRenderMode = 1;
        private boolean mRequestPaused;
        private boolean mRequestRender = true;
        private boolean mShouldExit;
        private boolean mShouldReleaseEglContext;
        private boolean mSizeChanged = true;
        private boolean mSurfaceIsBad;
        private boolean mWaitingForSurface;
        private boolean mWantRenderNotification = false;
        private int mWidth = 0;

        GLThread(WeakReference<GLSurfaceView> glSurfaceViewWeakRef) {
            this.mGLSurfaceViewWeakRef = glSurfaceViewWeakRef;
            this.mPerfServiceWrapper = new PerfServiceWrapper(null);
        }

        public void run() {
            setName("GLThread " + getId());
            if (GLSurfaceView.LOG_THREADS) {
                Log.i("GLThread", "starting tid=" + getId());
            }
            Trace.traceBegin(2, "GLSurfaceView-run");
            Trace.traceEnd(2);
            try {
                guardedRun();
            } catch (InterruptedException e) {
            } finally {
                GLSurfaceView.sGLThreadManager.threadExiting(this);
            }
        }

        private void stopEglSurfaceLocked() {
            if (this.mHaveEglSurface) {
                this.mHaveEglSurface = false;
                this.mEglHelper.destroySurface();
            }
        }

        private void stopEglContextLocked() {
            if (this.mHaveEglContext) {
                this.mEglHelper.finish();
                this.mHaveEglContext = false;
                GLSurfaceView.sGLThreadManager.releaseEglContextLocked(this);
            }
        }

        /* JADX WARNING: Missing block: B:24:0x0077, code:
            if (r9 == null) goto L_0x0451;
     */
        /* JADX WARNING: Missing block: B:25:0x0079, code:
            r9.run();
     */
        /* JADX WARNING: Missing block: B:26:0x007c, code:
            r9 = null;
     */
        /* JADX WARNING: Missing block: B:130:0x0451, code:
            if (r6 == false) goto L_0x0484;
     */
        /* JADX WARNING: Missing block: B:133:0x0457, code:
            if (android.opengl.GLSurfaceView.-get4() == false) goto L_0x0462;
     */
        /* JADX WARNING: Missing block: B:134:0x0459, code:
            android.util.Log.w("GLThread", "egl createSurface");
     */
        /* JADX WARNING: Missing block: B:136:0x046c, code:
            if (r26.mEglHelper.createSurface() == false) goto L_0x0648;
     */
        /* JADX WARNING: Missing block: B:137:0x046e, code:
            r22 = android.opengl.GLSurfaceView.-get14();
     */
        /* JADX WARNING: Missing block: B:138:0x0472, code:
            monitor-enter(r22);
     */
        /* JADX WARNING: Missing block: B:141:?, code:
            r26.mFinishedCreatingEglSurface = true;
            android.opengl.GLSurfaceView.-get14().notifyAll();
     */
        /* JADX WARNING: Missing block: B:143:?, code:
            monitor-exit(r22);
     */
        /* JADX WARNING: Missing block: B:144:0x0483, code:
            r6 = false;
     */
        /* JADX WARNING: Missing block: B:145:0x0484, code:
            if (r7 == false) goto L_0x0496;
     */
        /* JADX WARNING: Missing block: B:146:0x0486, code:
            r10 = (javax.microedition.khronos.opengles.GL10) r26.mEglHelper.createGL();
            r7 = false;
     */
        /* JADX WARNING: Missing block: B:147:0x0496, code:
            if (r5 == false) goto L_0x04ed;
     */
        /* JADX WARNING: Missing block: B:149:0x049c, code:
            if (android.opengl.GLSurfaceView.-get2() == false) goto L_0x04a7;
     */
        /* JADX WARNING: Missing block: B:150:0x049e, code:
            android.util.Log.w("GLThread", "onSurfaceCreated start");
     */
        /* JADX WARNING: Missing block: B:151:0x04a7, code:
            r18 = (android.opengl.GLSurfaceView) r26.mGLSurfaceViewWeakRef.get();
     */
        /* JADX WARNING: Missing block: B:152:0x04b3, code:
            if (r18 == null) goto L_0x04dd;
     */
        /* JADX WARNING: Missing block: B:154:?, code:
            android.os.Trace.traceBegin(8, "onSurfaceCreated");
            android.opengl.GLSurfaceView.-get13(r18).onSurfaceCreated(r10, r26.mEglHelper.mEglConfig);
     */
        /* JADX WARNING: Missing block: B:157:?, code:
            android.os.Trace.traceEnd(8);
     */
        /* JADX WARNING: Missing block: B:158:0x04dd, code:
            r5 = false;
     */
        /* JADX WARNING: Missing block: B:159:0x04e2, code:
            if (android.opengl.GLSurfaceView.-get2() == false) goto L_0x04ed;
     */
        /* JADX WARNING: Missing block: B:160:0x04e4, code:
            android.util.Log.w("GLThread", "onSurfaceCreated end");
     */
        /* JADX WARNING: Missing block: B:161:0x04ed, code:
            if (r15 == false) goto L_0x058a;
     */
        /* JADX WARNING: Missing block: B:163:0x04f3, code:
            if (android.opengl.GLSurfaceView.-get2() == false) goto L_0x0527;
     */
        /* JADX WARNING: Missing block: B:164:0x04f5, code:
            android.util.Log.w("GLThread", "onSurfaceChanged(" + r19 + ", " + r11 + ") start");
     */
        /* JADX WARNING: Missing block: B:165:0x0527, code:
            r18 = (android.opengl.GLSurfaceView) r26.mGLSurfaceViewWeakRef.get();
     */
        /* JADX WARNING: Missing block: B:166:0x0533, code:
            if (r18 == null) goto L_0x0551;
     */
        /* JADX WARNING: Missing block: B:168:?, code:
            android.os.Trace.traceBegin(8, "onSurfaceChanged");
            android.opengl.GLSurfaceView.-get13(r18).onSurfaceChanged(r10, r19, r11);
     */
        /* JADX WARNING: Missing block: B:171:?, code:
            android.os.Trace.traceEnd(8);
     */
        /* JADX WARNING: Missing block: B:172:0x0551, code:
            r15 = false;
     */
        /* JADX WARNING: Missing block: B:173:0x0556, code:
            if (android.opengl.GLSurfaceView.-get2() == false) goto L_0x058a;
     */
        /* JADX WARNING: Missing block: B:174:0x0558, code:
            android.util.Log.w("GLThread", "onSurfaceChanged(" + r19 + ", " + r11 + ") end");
     */
        /* JADX WARNING: Missing block: B:176:0x058e, code:
            if (android.opengl.GLSurfaceView.-get3() == false) goto L_0x05b2;
     */
        /* JADX WARNING: Missing block: B:177:0x0590, code:
            android.util.Log.w("GLThread", "onDrawFrame Start tid=" + getId());
     */
        /* JADX WARNING: Missing block: B:178:0x05b2, code:
            r18 = (android.opengl.GLSurfaceView) r26.mGLSurfaceViewWeakRef.get();
     */
        /* JADX WARNING: Missing block: B:179:0x05be, code:
            if (r18 == null) goto L_0x05e5;
     */
        /* JADX WARNING: Missing block: B:181:?, code:
            android.os.Trace.traceBegin(8, "onDrawFrame");
            r26.mPerfServiceWrapper.notifyFrameUpdate(0);
            android.opengl.GLSurfaceView.-get13(r18).onDrawFrame(r10);
     */
        /* JADX WARNING: Missing block: B:184:?, code:
            android.os.Trace.traceEnd(8);
     */
        /* JADX WARNING: Missing block: B:186:0x05e9, code:
            if (android.opengl.GLSurfaceView.-get3() == false) goto L_0x060d;
     */
        /* JADX WARNING: Missing block: B:187:0x05eb, code:
            android.util.Log.w("GLThread", "onDrawFrame End tid=" + getId());
     */
        /* JADX WARNING: Missing block: B:188:0x060d, code:
            r16 = r26.mEglHelper.swap();
     */
        /* JADX WARNING: Missing block: B:189:0x0617, code:
            switch(r16) {
                case 12288: goto L_0x063e;
                case android.opengl.EGL14.EGL_CONTEXT_LOST :int: goto L_0x067f;
                default: goto L_0x061a;
            };
     */
        /* JADX WARNING: Missing block: B:190:0x061a, code:
            android.opengl.GLSurfaceView.EglHelper.logEglErrorAsWarning("GLThread", "eglSwapBuffers", r16);
            r22 = android.opengl.GLSurfaceView.-get14();
     */
        /* JADX WARNING: Missing block: B:191:0x062d, code:
            monitor-enter(r22);
     */
        /* JADX WARNING: Missing block: B:194:?, code:
            r26.mSurfaceIsBad = true;
            android.opengl.GLSurfaceView.-get14().notifyAll();
     */
        /* JADX WARNING: Missing block: B:196:?, code:
            monitor-exit(r22);
     */
        /* JADX WARNING: Missing block: B:197:0x063e, code:
            if (r20 == false) goto L_0x0037;
     */
        /* JADX WARNING: Missing block: B:198:0x0640, code:
            r8 = true;
            r20 = false;
     */
        /* JADX WARNING: Missing block: B:202:0x0648, code:
            r22 = android.opengl.GLSurfaceView.-get14();
     */
        /* JADX WARNING: Missing block: B:203:0x064c, code:
            monitor-enter(r22);
     */
        /* JADX WARNING: Missing block: B:206:?, code:
            r26.mFinishedCreatingEglSurface = true;
            r26.mSurfaceIsBad = true;
            android.opengl.GLSurfaceView.-get14().notifyAll();
     */
        /* JADX WARNING: Missing block: B:208:?, code:
            monitor-exit(r22);
     */
        /* JADX WARNING: Missing block: B:223:0x0683, code:
            if (android.opengl.GLSurfaceView.-get4() == false) goto L_0x06a7;
     */
        /* JADX WARNING: Missing block: B:224:0x0685, code:
            android.util.Log.i("GLThread", "egl context lost tid=" + getId());
     */
        /* JADX WARNING: Missing block: B:225:0x06a7, code:
            r12 = true;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private void guardedRun() throws InterruptedException {
            this.mEglHelper = new EglHelper(this.mGLSurfaceViewWeakRef);
            this.mHaveEglContext = false;
            this.mHaveEglSurface = false;
            this.mWantRenderNotification = false;
            GL10 gl = null;
            boolean createEglContext = false;
            boolean createEglSurface = false;
            boolean createGlInterface = false;
            boolean lostEglContext = false;
            boolean sizeChanged = false;
            boolean wantRenderNotification = false;
            boolean doRenderNotification = false;
            boolean askedToReleaseEglContext = false;
            int w = 0;
            int h = 0;
            Runnable event = null;
            while (true) {
                synchronized (GLSurfaceView.sGLThreadManager) {
                    while (!this.mShouldExit) {
                        if (this.mEventQueue.isEmpty()) {
                            boolean pausing = false;
                            if (this.mPaused != this.mRequestPaused) {
                                pausing = this.mRequestPaused;
                                this.mPaused = this.mRequestPaused;
                                GLSurfaceView.sGLThreadManager.notifyAll();
                                if (GLSurfaceView.LOG_PAUSE_RESUME) {
                                    Log.i("GLThread", "mPaused is now " + this.mPaused + " tid=" + getId());
                                }
                            }
                            if (this.mShouldReleaseEglContext) {
                                if (GLSurfaceView.LOG_SURFACE) {
                                    Log.i("GLThread", "releasing EGL context because asked to tid=" + getId());
                                }
                                stopEglSurfaceLocked();
                                stopEglContextLocked();
                                this.mShouldReleaseEglContext = false;
                                askedToReleaseEglContext = true;
                            }
                            if (lostEglContext) {
                                stopEglSurfaceLocked();
                                stopEglContextLocked();
                                lostEglContext = false;
                            }
                            if (pausing && this.mHaveEglSurface) {
                                if (GLSurfaceView.LOG_SURFACE) {
                                    Log.i("GLThread", "releasing EGL surface because paused tid=" + getId());
                                }
                                stopEglSurfaceLocked();
                            }
                            if (pausing && this.mHaveEglContext) {
                                GLSurfaceView view = (GLSurfaceView) this.mGLSurfaceViewWeakRef.get();
                                if (!(view == null ? false : view.mPreserveEGLContextOnPause)) {
                                    stopEglContextLocked();
                                    if (GLSurfaceView.LOG_SURFACE) {
                                        Log.i("GLThread", "releasing EGL context because paused tid=" + getId());
                                    }
                                }
                            }
                            if (!(this.mHasSurface || this.mWaitingForSurface)) {
                                if (GLSurfaceView.LOG_SURFACE) {
                                    Log.i("GLThread", "noticed surfaceView surface lost tid=" + getId());
                                }
                                if (this.mHaveEglSurface) {
                                    stopEglSurfaceLocked();
                                }
                                this.mWaitingForSurface = true;
                                this.mSurfaceIsBad = false;
                                GLSurfaceView.sGLThreadManager.notifyAll();
                            }
                            if (this.mHasSurface && this.mWaitingForSurface) {
                                if (GLSurfaceView.LOG_SURFACE) {
                                    Log.i("GLThread", "noticed surfaceView surface acquired tid=" + getId());
                                }
                                this.mWaitingForSurface = false;
                                GLSurfaceView.sGLThreadManager.notifyAll();
                            }
                            if (doRenderNotification) {
                                if (GLSurfaceView.LOG_SURFACE) {
                                    Log.i("GLThread", "sending render notification tid=" + getId());
                                }
                                this.mWantRenderNotification = false;
                                doRenderNotification = false;
                                this.mRenderComplete = true;
                                GLSurfaceView.sGLThreadManager.notifyAll();
                            }
                            if (readyToDraw()) {
                                if (!this.mHaveEglContext) {
                                    if (askedToReleaseEglContext) {
                                        askedToReleaseEglContext = false;
                                    } else {
                                        this.mEglHelper.start();
                                        this.mHaveEglContext = true;
                                        createEglContext = true;
                                        GLSurfaceView.sGLThreadManager.notifyAll();
                                    }
                                }
                                if (this.mHaveEglContext && !this.mHaveEglSurface) {
                                    this.mHaveEglSurface = true;
                                    createEglSurface = true;
                                    createGlInterface = true;
                                    sizeChanged = true;
                                }
                                if (this.mHaveEglSurface) {
                                    if (this.mSizeChanged) {
                                        sizeChanged = true;
                                        w = this.mWidth;
                                        h = this.mHeight;
                                        this.mWantRenderNotification = true;
                                        if (GLSurfaceView.LOG_SURFACE) {
                                            Log.i("GLThread", "noticing that we want render notification tid=" + getId());
                                        }
                                        createEglSurface = true;
                                        this.mSizeChanged = false;
                                    }
                                    this.mRequestRender = false;
                                    GLSurfaceView.sGLThreadManager.notifyAll();
                                    if (this.mWantRenderNotification) {
                                        wantRenderNotification = true;
                                    }
                                }
                            }
                            if (GLSurfaceView.LOG_THREADS) {
                                Log.i("GLThread", "waiting tid=" + getId() + " mHaveEglContext: " + this.mHaveEglContext + " mHaveEglSurface: " + this.mHaveEglSurface + " mFinishedCreatingEglSurface: " + this.mFinishedCreatingEglSurface + " mPaused: " + this.mPaused + " mHasSurface: " + this.mHasSurface + " mSurfaceIsBad: " + this.mSurfaceIsBad + " mWaitingForSurface: " + this.mWaitingForSurface + " mWidth: " + this.mWidth + " mHeight: " + this.mHeight + " mRequestRender: " + this.mRequestRender + " mRenderMode: " + this.mRenderMode);
                            }
                            GLSurfaceView.sGLThreadManager.wait();
                        } else {
                            event = (Runnable) this.mEventQueue.remove(0);
                        }
                        try {
                        } catch (RuntimeException t) {
                            GLSurfaceView.sGLThreadManager.releaseEglContextLocked(this);
                            throw t;
                        } catch (Throwable th) {
                            synchronized (GLSurfaceView.sGLThreadManager) {
                                stopEglSurfaceLocked();
                                stopEglContextLocked();
                            }
                        }
                    }
                    synchronized (GLSurfaceView.sGLThreadManager) {
                        stopEglSurfaceLocked();
                        stopEglContextLocked();
                    }
                    return;
                }
            }
        }

        public boolean ableToDraw() {
            return (this.mHaveEglContext && this.mHaveEglSurface) ? readyToDraw() : false;
        }

        private boolean readyToDraw() {
            if (this.mPaused || !this.mHasSurface || this.mSurfaceIsBad || this.mWidth <= 0 || this.mHeight <= 0) {
                return false;
            }
            if (this.mRequestRender || this.mRenderMode == 1) {
                return true;
            }
            return false;
        }

        public void setRenderMode(int renderMode) {
            if (renderMode < 0 || renderMode > 1) {
                throw new IllegalArgumentException("renderMode");
            }
            synchronized (GLSurfaceView.sGLThreadManager) {
                this.mRenderMode = renderMode;
                GLSurfaceView.sGLThreadManager.notifyAll();
            }
        }

        public int getRenderMode() {
            int i;
            synchronized (GLSurfaceView.sGLThreadManager) {
                i = this.mRenderMode;
            }
            return i;
        }

        public void requestRender() {
            synchronized (GLSurfaceView.sGLThreadManager) {
                if (GLSurfaceView.LOG_THREADS) {
                    Log.i("GLThread", "requestRender start tid=" + getId());
                }
                Trace.traceBegin(2, "requestRender");
                this.mRequestRender = true;
                GLSurfaceView.sGLThreadManager.notifyAll();
                Trace.traceEnd(2);
                if (GLSurfaceView.LOG_THREADS) {
                    Log.i("GLThread", "requestRender end tid=" + getId());
                }
            }
        }

        /* JADX WARNING: Missing block: B:15:0x0026, code:
            return;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void requestRenderAndWait() {
            synchronized (GLSurfaceView.sGLThreadManager) {
                if (Thread.currentThread() != this) {
                    this.mWantRenderNotification = true;
                    this.mRequestRender = true;
                    this.mRenderComplete = false;
                    GLSurfaceView.sGLThreadManager.notifyAll();
                    while (!this.mExited && !this.mPaused) {
                        if (this.mRenderComplete || !ableToDraw()) {
                            break;
                        }
                        try {
                            GLSurfaceView.sGLThreadManager.wait();
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }
                }
            }
        }

        public void surfaceCreated() {
            synchronized (GLSurfaceView.sGLThreadManager) {
                if (GLSurfaceView.LOG_THREADS) {
                    Log.i("GLThread", "surfaceCreated start tid=" + getId());
                }
                Trace.traceBegin(2, "surfaceCreated");
                this.mHasSurface = true;
                this.mFinishedCreatingEglSurface = false;
                GLSurfaceView.sGLThreadManager.notifyAll();
                while (this.mWaitingForSurface && !this.mFinishedCreatingEglSurface) {
                    if (this.mExited) {
                        break;
                    }
                    try {
                        GLSurfaceView.sGLThreadManager.wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
                Trace.traceEnd(2);
                if (GLSurfaceView.LOG_THREADS) {
                    Log.i("GLThread", "surfaceCreated end tid=" + getId());
                }
            }
        }

        public void surfaceDestroyed() {
            synchronized (GLSurfaceView.sGLThreadManager) {
                if (GLSurfaceView.LOG_THREADS) {
                    Log.i("GLThread", "surfaceDestroyed start tid=" + getId());
                }
                Trace.traceBegin(2, "surfaceDestroyed");
                this.mHasSurface = false;
                GLSurfaceView.sGLThreadManager.notifyAll();
                while (!this.mWaitingForSurface && !this.mExited) {
                    try {
                        GLSurfaceView.sGLThreadManager.wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
                Trace.traceEnd(2);
                if (GLSurfaceView.LOG_THREADS) {
                    Log.i("GLThread", "surfaceDestroyed end tid=" + getId());
                }
            }
        }

        public void onPause() {
            synchronized (GLSurfaceView.sGLThreadManager) {
                if (GLSurfaceView.LOG_PAUSE_RESUME) {
                    Log.i("GLThread", "onPause start tid=" + getId());
                }
                Trace.traceBegin(2, "onPause");
                this.mRequestPaused = true;
                GLSurfaceView.sGLThreadManager.notifyAll();
                while (!this.mExited && !this.mPaused) {
                    if (GLSurfaceView.LOG_PAUSE_RESUME) {
                        Log.i("Main thread", "onPause waiting for mPaused.");
                    }
                    try {
                        GLSurfaceView.sGLThreadManager.wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
                Trace.traceEnd(2);
                if (GLSurfaceView.LOG_PAUSE_RESUME) {
                    Log.i("GLThread", "onPause end tid=" + getId());
                }
            }
        }

        public void onResume() {
            synchronized (GLSurfaceView.sGLThreadManager) {
                if (GLSurfaceView.LOG_PAUSE_RESUME) {
                    Log.i("GLThread", "onResume start tid=" + getId());
                }
                Trace.traceBegin(2, "onResume");
                this.mRequestPaused = false;
                this.mRequestRender = true;
                this.mRenderComplete = false;
                GLSurfaceView.sGLThreadManager.notifyAll();
                while (!this.mExited && this.mPaused && !this.mRenderComplete) {
                    if (GLSurfaceView.LOG_PAUSE_RESUME) {
                        Log.i("Main thread", "onResume waiting for !mPaused.");
                    }
                    try {
                        GLSurfaceView.sGLThreadManager.wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
                Trace.traceEnd(2);
                if (GLSurfaceView.LOG_PAUSE_RESUME) {
                    Log.i("GLThread", "onResume end tid=" + getId());
                }
            }
        }

        /* JADX WARNING: Missing block: B:20:0x007f, code:
            return;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void onWindowResize(int w, int h) {
            synchronized (GLSurfaceView.sGLThreadManager) {
                if (GLSurfaceView.LOG_THREADS) {
                    Log.i("GLThread", "onWindowResize start tid=" + getId());
                }
                Trace.traceBegin(2, "onWindowResize");
                this.mWidth = w;
                this.mHeight = h;
                this.mSizeChanged = true;
                this.mRequestRender = true;
                this.mRenderComplete = false;
                if (Thread.currentThread() == this) {
                    return;
                }
                GLSurfaceView.sGLThreadManager.notifyAll();
                while (!this.mExited && !this.mPaused) {
                    if (this.mRenderComplete || !ableToDraw()) {
                        break;
                    }
                    if (GLSurfaceView.LOG_SURFACE) {
                        Log.i("Main thread", "onWindowResize waiting for render complete from tid=" + getId());
                    }
                    try {
                        GLSurfaceView.sGLThreadManager.wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
                Trace.traceEnd(2);
                if (GLSurfaceView.LOG_THREADS) {
                    Log.i("GLThread", "onWindowResize end tid=" + getId());
                }
            }
        }

        public void requestExitAndWait() {
            synchronized (GLSurfaceView.sGLThreadManager) {
                if (GLSurfaceView.LOG_THREADS) {
                    Log.i("GLThread", "requestExitAndWait start tid=" + getId());
                }
                Trace.traceBegin(2, "requestExotAndWait");
                this.mShouldExit = true;
                GLSurfaceView.sGLThreadManager.notifyAll();
                while (!this.mExited) {
                    try {
                        GLSurfaceView.sGLThreadManager.wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
                Trace.traceEnd(2);
                if (GLSurfaceView.LOG_THREADS) {
                    Log.i("GLThread", "requestExitAndWait end tid=" + getId());
                }
            }
        }

        public void requestReleaseEglContextLocked() {
            if (GLSurfaceView.LOG_THREADS) {
                Log.i("GLThread", "requestReleaseEglContextLocked start tid=" + getId());
            }
            Trace.traceBegin(2, "requestReleaseEglContextLocked");
            this.mShouldReleaseEglContext = true;
            GLSurfaceView.sGLThreadManager.notifyAll();
            Trace.traceEnd(2);
            if (GLSurfaceView.LOG_THREADS) {
                Log.i("GLThread", "requestReleaseEglContextLocked end tid=" + getId());
            }
        }

        public void queueEvent(Runnable r) {
            if (r == null) {
                throw new IllegalArgumentException("r must not be null");
            }
            if (GLSurfaceView.LOG_THREADS) {
                Log.i("GLThread", "queueEvent start tid=" + getId() + " runnable=" + r);
            }
            Trace.traceBegin(2, "queueEvent");
            synchronized (GLSurfaceView.sGLThreadManager) {
                this.mEventQueue.add(r);
                GLSurfaceView.sGLThreadManager.notifyAll();
            }
            Trace.traceEnd(2);
            if (GLSurfaceView.LOG_THREADS) {
                Log.i("GLThread", "queueEvent end tid=" + getId());
            }
        }
    }

    private static class GLThreadManager {
        private static String TAG;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.opengl.GLSurfaceView.GLThreadManager.<clinit>():void, dex: 
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
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.opengl.GLSurfaceView.GLThreadManager.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.opengl.GLSurfaceView.GLThreadManager.<clinit>():void");
        }

        /* synthetic */ GLThreadManager(GLThreadManager gLThreadManager) {
            this();
        }

        private GLThreadManager() {
        }

        public synchronized void threadExiting(GLThread thread) {
            if (GLSurfaceView.LOG_THREADS) {
                Log.i("GLThread", "exiting tid=" + thread.getId());
            }
            thread.mExited = true;
            notifyAll();
        }

        public void releaseEglContextLocked(GLThread thread) {
            notifyAll();
        }
    }

    public interface GLWrapper {
        GL wrap(GL gl);
    }

    static class LogWriter extends Writer {
        private StringBuilder mBuilder;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: android.opengl.GLSurfaceView.LogWriter.<init>():void, dex:  in method: android.opengl.GLSurfaceView.LogWriter.<init>():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: jadx.core.utils.exceptions.DecodeException: null in method: android.opengl.GLSurfaceView.LogWriter.<init>():void, dex: 
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
            	... 6 more
            Caused by: java.io.EOFException
            	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
            	at com.android.dx.io.instructions.ShortArrayCodeInput.readInt(ShortArrayCodeInput.java:62)
            	at com.android.dx.io.instructions.InstructionCodec$22.decode(InstructionCodec.java:490)
            	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
            	... 7 more
            */
        LogWriter() {
            /*
            // Can't load method instructions: Load method exception: null in method: android.opengl.GLSurfaceView.LogWriter.<init>():void, dex:  in method: android.opengl.GLSurfaceView.LogWriter.<init>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.opengl.GLSurfaceView.LogWriter.<init>():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.opengl.GLSurfaceView.LogWriter.flushBuilder():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        private void flushBuilder() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.opengl.GLSurfaceView.LogWriter.flushBuilder():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.opengl.GLSurfaceView.LogWriter.flushBuilder():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.opengl.GLSurfaceView.LogWriter.close():void, dex: 
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
        public void close() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.opengl.GLSurfaceView.LogWriter.close():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.opengl.GLSurfaceView.LogWriter.close():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.opengl.GLSurfaceView.LogWriter.flush():void, dex: 
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
        public void flush() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.opengl.GLSurfaceView.LogWriter.flush():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.opengl.GLSurfaceView.LogWriter.flush():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.opengl.GLSurfaceView.LogWriter.write(char[], int, int):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        public void write(char[] r1, int r2, int r3) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.opengl.GLSurfaceView.LogWriter.write(char[], int, int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.opengl.GLSurfaceView.LogWriter.write(char[], int, int):void");
        }
    }

    public interface Renderer {
        void onDrawFrame(GL10 gl10);

        void onSurfaceChanged(GL10 gl10, int i, int i2);

        void onSurfaceCreated(GL10 gl10, EGLConfig eGLConfig);
    }

    private class SimpleEGLConfigChooser extends ComponentSizeChooser {
        final /* synthetic */ GLSurfaceView this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.opengl.GLSurfaceView.SimpleEGLConfigChooser.<init>(android.opengl.GLSurfaceView, boolean):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        public SimpleEGLConfigChooser(android.opengl.GLSurfaceView r1, boolean r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.opengl.GLSurfaceView.SimpleEGLConfigChooser.<init>(android.opengl.GLSurfaceView, boolean):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.opengl.GLSurfaceView.SimpleEGLConfigChooser.<init>(android.opengl.GLSurfaceView, boolean):void");
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.opengl.GLSurfaceView.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.opengl.GLSurfaceView.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.opengl.GLSurfaceView.<clinit>():void");
    }

    public GLSurfaceView(Context context) {
        super(context);
        this.mThisWeakRef = new WeakReference(this);
        init();
    }

    public GLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mThisWeakRef = new WeakReference(this);
        init();
    }

    protected void finalize() throws Throwable {
        try {
            if (this.mGLThread != null) {
                this.mGLThread.requestExitAndWait();
            }
            super.finalize();
        } catch (Throwable th) {
            super.finalize();
        }
    }

    private void init() {
        getHolder().addCallback(this);
        checkLogProperty();
    }

    public void setGLWrapper(GLWrapper glWrapper) {
        this.mGLWrapper = glWrapper;
    }

    public void setDebugFlags(int debugFlags) {
        this.mDebugFlags = debugFlags;
    }

    public int getDebugFlags() {
        return this.mDebugFlags;
    }

    public void setPreserveEGLContextOnPause(boolean preserveOnPause) {
        this.mPreserveEGLContextOnPause = preserveOnPause;
    }

    public boolean getPreserveEGLContextOnPause() {
        return this.mPreserveEGLContextOnPause;
    }

    public void setRenderer(Renderer renderer) {
        checkRenderThreadState();
        Log.i(TAG, "setRenderer(), this = " + this);
        if (this.mEGLConfigChooser == null) {
            this.mEGLConfigChooser = new SimpleEGLConfigChooser(this, true);
        }
        if (this.mEGLContextFactory == null) {
            this.mEGLContextFactory = new DefaultContextFactory(this, null);
        }
        if (this.mEGLWindowSurfaceFactory == null) {
            this.mEGLWindowSurfaceFactory = new DefaultWindowSurfaceFactory();
        }
        this.mRenderer = renderer;
        this.mGLThread = new GLThread(this.mThisWeakRef);
        this.mGLThread.start();
    }

    public void setEGLContextFactory(EGLContextFactory factory) {
        checkRenderThreadState();
        this.mEGLContextFactory = factory;
        Log.i(TAG, "setEGLContextFactory(), factory = " + factory + ", this = " + this);
    }

    public void setEGLWindowSurfaceFactory(EGLWindowSurfaceFactory factory) {
        checkRenderThreadState();
        this.mEGLWindowSurfaceFactory = factory;
        Log.i(TAG, "setEGLWindowSurfaceFactory(), factory = " + factory + ", this = " + this);
    }

    public void setEGLConfigChooser(EGLConfigChooser configChooser) {
        checkRenderThreadState();
        this.mEGLConfigChooser = configChooser;
    }

    public void setEGLConfigChooser(boolean needDepth) {
        setEGLConfigChooser(new SimpleEGLConfigChooser(this, needDepth));
    }

    public void setEGLConfigChooser(int redSize, int greenSize, int blueSize, int alphaSize, int depthSize, int stencilSize) {
        setEGLConfigChooser(new ComponentSizeChooser(redSize, greenSize, blueSize, alphaSize, depthSize, stencilSize));
    }

    public void setEGLContextClientVersion(int version) {
        checkRenderThreadState();
        this.mEGLContextClientVersion = version;
    }

    public void setRenderMode(int renderMode) {
        Log.i(TAG, "setRenderMode = " + renderMode + ", this = " + this);
        this.mGLThread.setRenderMode(renderMode);
    }

    public int getRenderMode() {
        return this.mGLThread.getRenderMode();
    }

    public void requestRender() {
        this.mGLThread.requestRender();
    }

    public void surfaceCreated(SurfaceHolder holder) {
        this.mGLThread.surfaceCreated();
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        this.mGLThread.surfaceDestroyed();
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        this.mGLThread.onWindowResize(w, h);
    }

    public void surfaceRedrawNeeded(SurfaceHolder holder) {
        if (this.mGLThread != null) {
            this.mGLThread.requestRenderAndWait();
        }
    }

    public void onPause() {
        this.mGLThread.onPause();
    }

    public void onResume() {
        this.mGLThread.onResume();
    }

    public void queueEvent(Runnable r) {
        this.mGLThread.queueEvent(r);
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (LOG_ATTACH_DETACH) {
            Log.d(TAG, "onAttachedToWindow reattach =" + this.mDetached + ", this = " + this);
        }
        if (this.mDetached && this.mRenderer != null) {
            int renderMode = 1;
            if (this.mGLThread != null) {
                renderMode = this.mGLThread.getRenderMode();
            }
            this.mGLThread = new GLThread(this.mThisWeakRef);
            if (renderMode != 1) {
                this.mGLThread.setRenderMode(renderMode);
            }
            this.mGLThread.start();
        }
        this.mDetached = false;
    }

    protected void onDetachedFromWindow() {
        if (LOG_ATTACH_DETACH) {
            Log.d(TAG, "onDetachedFromWindow, this = " + this);
        }
        if (this.mGLThread != null) {
            this.mGLThread.requestExitAndWait();
        }
        this.mDetached = true;
        super.onDetachedFromWindow();
    }

    private void checkRenderThreadState() {
        if (this.mGLThread != null) {
            throw new IllegalStateException("setRenderer has already been called for this instance.");
        }
    }

    private static void checkLogProperty() {
        boolean z = true;
        String dumpString = SystemProperties.get(LOG_PROPERTY_NAME);
        if (dumpString != null) {
            if (dumpString.length() <= 0 || dumpString.length() > 7) {
                Log.d(TAG, "checkGLSurfaceViewlLogProperty get invalid command");
                return;
            }
            boolean z2;
            int logFilter = 0;
            try {
                logFilter = Integer.parseInt(dumpString, 2);
            } catch (NumberFormatException e) {
                Log.w(TAG, "Invalid format of propery string: " + dumpString);
            }
            if ((logFilter & 1) == 1) {
                z2 = true;
            } else {
                z2 = false;
            }
            LOG_ATTACH_DETACH = z2;
            if ((logFilter & 2) == 2) {
                z2 = true;
            } else {
                z2 = false;
            }
            LOG_THREADS = z2;
            if ((logFilter & 4) == 4) {
                z2 = true;
            } else {
                z2 = false;
            }
            LOG_PAUSE_RESUME = z2;
            if ((logFilter & 8) == 8) {
                z2 = true;
            } else {
                z2 = false;
            }
            LOG_SURFACE = z2;
            if ((logFilter & 16) == 16) {
                z2 = true;
            } else {
                z2 = false;
            }
            LOG_RENDERER = z2;
            if ((logFilter & 32) == 32) {
                z2 = true;
            } else {
                z2 = false;
            }
            LOG_RENDERER_DRAW_FRAME = z2;
            if ((logFilter & 64) != 64) {
                z = false;
            }
            LOG_EGL = z;
            Log.d(TAG, "checkGLSurfaceViewlLogProperty debug filter: ATTACH_DETACH=" + LOG_ATTACH_DETACH + ", THREADS=" + LOG_THREADS + ", PAUSE_RESUME=" + LOG_PAUSE_RESUME + ", SURFACE=" + LOG_SURFACE + ", RENDERER=" + LOG_RENDERER + ", RENDERER_DRAW_FRAME=" + LOG_RENDERER_DRAW_FRAME + ", EGL=" + LOG_EGL);
        }
    }
}
