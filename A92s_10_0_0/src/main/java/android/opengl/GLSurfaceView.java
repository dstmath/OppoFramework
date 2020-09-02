package android.opengl;

import android.annotation.UnsupportedAppUsage;
import android.content.Context;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.SystemProperties;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
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

public class GLSurfaceView extends SurfaceView implements SurfaceHolder.Callback2 {
    public static final int DEBUG_CHECK_GL_ERROR = 1;
    public static final int DEBUG_LOG_GL_CALLS = 2;
    private static final boolean LOG_ATTACH_DETACH = SystemProperties.getBoolean("debug.glsurfaceview.log", false);
    /* access modifiers changed from: private */
    public static final boolean LOG_EGL = SystemProperties.getBoolean("debug.glsurfaceview.log", false);
    /* access modifiers changed from: private */
    public static final boolean LOG_PAUSE_RESUME = SystemProperties.getBoolean("debug.glsurfaceview.log", false);
    /* access modifiers changed from: private */
    public static final boolean LOG_RENDERER = SystemProperties.getBoolean("debug.glsurfaceview.log", false);
    /* access modifiers changed from: private */
    public static final boolean LOG_RENDERER_DRAW_FRAME = SystemProperties.getBoolean("debug.glsurfaceview.log", false);
    /* access modifiers changed from: private */
    public static final boolean LOG_SURFACE = SystemProperties.getBoolean("debug.glsurfaceview.log", false);
    /* access modifiers changed from: private */
    public static final boolean LOG_THREADS = SystemProperties.getBoolean("debug.glsurfaceview.log", false);
    public static final int RENDERMODE_CONTINUOUSLY = 1;
    public static final int RENDERMODE_WHEN_DIRTY = 0;
    private static final String TAG = "GLSurfaceView";
    /* access modifiers changed from: private */
    public static final GLThreadManager sGLThreadManager = new GLThreadManager();
    /* access modifiers changed from: private */
    public int mDebugFlags;
    private boolean mDetached;
    /* access modifiers changed from: private */
    public EGLConfigChooser mEGLConfigChooser;
    /* access modifiers changed from: private */
    public int mEGLContextClientVersion;
    /* access modifiers changed from: private */
    public EGLContextFactory mEGLContextFactory;
    /* access modifiers changed from: private */
    public EGLWindowSurfaceFactory mEGLWindowSurfaceFactory;
    @UnsupportedAppUsage
    private GLThread mGLThread;
    /* access modifiers changed from: private */
    public GLWrapper mGLWrapper;
    /* access modifiers changed from: private */
    public boolean mPreserveEGLContextOnPause;
    /* access modifiers changed from: private */
    @UnsupportedAppUsage
    public Renderer mRenderer;
    private final WeakReference<GLSurfaceView> mThisWeakRef = new WeakReference<>(this);

    public interface EGLConfigChooser {
        EGLConfig chooseConfig(EGL10 egl10, EGLDisplay eGLDisplay);
    }

    public interface EGLContextFactory {
        EGLContext createContext(EGL10 egl10, EGLDisplay eGLDisplay, EGLConfig eGLConfig);

        void destroyContext(EGL10 egl10, EGLDisplay eGLDisplay, EGLContext eGLContext);
    }

    public interface EGLWindowSurfaceFactory {
        EGLSurface createWindowSurface(EGL10 egl10, EGLDisplay eGLDisplay, EGLConfig eGLConfig, Object obj);

        void destroySurface(EGL10 egl10, EGLDisplay eGLDisplay, EGLSurface eGLSurface);
    }

    public interface GLWrapper {
        GL wrap(GL gl);
    }

    public interface Renderer {
        void onDrawFrame(GL10 gl10);

        void onSurfaceChanged(GL10 gl10, int i, int i2);

        void onSurfaceCreated(GL10 gl10, EGLConfig eGLConfig);
    }

    public GLSurfaceView(Context context) {
        super(context);
        init();
    }

    public GLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        try {
            if (this.mGLThread != null) {
                this.mGLThread.requestExitAndWait();
            }
        } finally {
            super.finalize();
        }
    }

    private void init() {
        getHolder().addCallback(this);
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
        if (this.mEGLConfigChooser == null) {
            this.mEGLConfigChooser = new SimpleEGLConfigChooser(true);
        }
        if (this.mEGLContextFactory == null) {
            this.mEGLContextFactory = new DefaultContextFactory();
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
    }

    public void setEGLWindowSurfaceFactory(EGLWindowSurfaceFactory factory) {
        checkRenderThreadState();
        this.mEGLWindowSurfaceFactory = factory;
    }

    public void setEGLConfigChooser(EGLConfigChooser configChooser) {
        checkRenderThreadState();
        this.mEGLConfigChooser = configChooser;
    }

    public void setEGLConfigChooser(boolean needDepth) {
        setEGLConfigChooser(new SimpleEGLConfigChooser(needDepth));
    }

    public void setEGLConfigChooser(int redSize, int greenSize, int blueSize, int alphaSize, int depthSize, int stencilSize) {
        setEGLConfigChooser(new ComponentSizeChooser(redSize, greenSize, blueSize, alphaSize, depthSize, stencilSize));
    }

    public void setEGLContextClientVersion(int version) {
        checkRenderThreadState();
        this.mEGLContextClientVersion = version;
    }

    public void setRenderMode(int renderMode) {
        this.mGLThread.setRenderMode(renderMode);
    }

    public int getRenderMode() {
        return this.mGLThread.getRenderMode();
    }

    public void requestRender() {
        this.mGLThread.requestRender();
    }

    @Override // android.view.SurfaceHolder.Callback
    public void surfaceCreated(SurfaceHolder holder) {
        this.mGLThread.surfaceCreated();
    }

    @Override // android.view.SurfaceHolder.Callback
    public void surfaceDestroyed(SurfaceHolder holder) {
        this.mGLThread.surfaceDestroyed();
    }

    @Override // android.view.SurfaceHolder.Callback
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        this.mGLThread.onWindowResize(w, h);
    }

    @Override // android.view.SurfaceHolder.Callback2
    public void surfaceRedrawNeededAsync(SurfaceHolder holder, Runnable finishDrawing) {
        GLThread gLThread = this.mGLThread;
        if (gLThread != null) {
            gLThread.requestRenderAndNotify(finishDrawing);
        } else if (finishDrawing != null) {
            finishDrawing.run();
        }
    }

    @Override // android.view.SurfaceHolder.Callback2
    @Deprecated
    public void surfaceRedrawNeeded(SurfaceHolder holder) {
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

    /* access modifiers changed from: protected */
    @Override // android.view.SurfaceView, android.view.View
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (LOG_ATTACH_DETACH) {
            Log.d(TAG, "onAttachedToWindow reattach =" + this.mDetached);
        }
        if (this.mDetached && this.mRenderer != null) {
            int renderMode = 1;
            GLThread gLThread = this.mGLThread;
            if (gLThread != null) {
                renderMode = gLThread.getRenderMode();
            }
            this.mGLThread = new GLThread(this.mThisWeakRef);
            if (renderMode != 1) {
                this.mGLThread.setRenderMode(renderMode);
            }
            this.mGLThread.start();
        }
        this.mDetached = false;
    }

    /* access modifiers changed from: protected */
    @Override // android.view.SurfaceView, android.view.View
    public void onDetachedFromWindow() {
        if (LOG_ATTACH_DETACH) {
            Log.d(TAG, "onDetachedFromWindow");
        }
        GLThread gLThread = this.mGLThread;
        if (gLThread != null) {
            gLThread.requestExitAndWait();
        }
        this.mDetached = true;
        super.onDetachedFromWindow();
    }

    private class DefaultContextFactory implements EGLContextFactory {
        private int EGL_CONTEXT_CLIENT_VERSION;

        private DefaultContextFactory() {
            this.EGL_CONTEXT_CLIENT_VERSION = 12440;
        }

        @Override // android.opengl.GLSurfaceView.EGLContextFactory
        public EGLContext createContext(EGL10 egl, EGLDisplay display, EGLConfig config) {
            return egl.eglCreateContext(display, config, EGL10.EGL_NO_CONTEXT, GLSurfaceView.this.mEGLContextClientVersion != 0 ? new int[]{this.EGL_CONTEXT_CLIENT_VERSION, GLSurfaceView.this.mEGLContextClientVersion, 12344} : null);
        }

        @Override // android.opengl.GLSurfaceView.EGLContextFactory
        public void destroyContext(EGL10 egl, EGLDisplay display, EGLContext context) {
            if (!egl.eglDestroyContext(display, context)) {
                Log.e("DefaultContextFactory", "display:" + display + " context: " + context);
                if (GLSurfaceView.LOG_THREADS) {
                    Log.i("DefaultContextFactory", "tid=" + Thread.currentThread().getId());
                }
                EglHelper.throwEglException("eglDestroyContex", egl.eglGetError());
            }
        }
    }

    private static class DefaultWindowSurfaceFactory implements EGLWindowSurfaceFactory {
        private DefaultWindowSurfaceFactory() {
        }

        @Override // android.opengl.GLSurfaceView.EGLWindowSurfaceFactory
        public EGLSurface createWindowSurface(EGL10 egl, EGLDisplay display, EGLConfig config, Object nativeWindow) {
            try {
                return egl.eglCreateWindowSurface(display, config, nativeWindow, null);
            } catch (IllegalArgumentException e) {
                Log.e(GLSurfaceView.TAG, "eglCreateWindowSurface", e);
                return null;
            }
        }

        @Override // android.opengl.GLSurfaceView.EGLWindowSurfaceFactory
        public void destroySurface(EGL10 egl, EGLDisplay display, EGLSurface surface) {
            egl.eglDestroySurface(display, surface);
        }
    }

    private abstract class BaseConfigChooser implements EGLConfigChooser {
        protected int[] mConfigSpec;

        /* access modifiers changed from: package-private */
        public abstract EGLConfig chooseConfig(EGL10 egl10, EGLDisplay eGLDisplay, EGLConfig[] eGLConfigArr);

        public BaseConfigChooser(int[] configSpec) {
            this.mConfigSpec = filterConfigSpec(configSpec);
        }

        @Override // android.opengl.GLSurfaceView.EGLConfigChooser
        public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display) {
            int[] num_config = new int[1];
            if (egl.eglChooseConfig(display, this.mConfigSpec, null, 0, num_config)) {
                int numConfigs = num_config[0];
                if (numConfigs > 0) {
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
                throw new IllegalArgumentException("No configs match configSpec");
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
            newConfigSpec[len - 1] = 12352;
            if (GLSurfaceView.this.mEGLContextClientVersion == 2) {
                newConfigSpec[len] = 4;
            } else {
                newConfigSpec[len] = 64;
            }
            newConfigSpec[len + 1] = 12344;
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
            super(new int[]{12324, redSize, 12323, greenSize, 12322, blueSize, 12321, alphaSize, 12325, depthSize, 12326, stencilSize, 12344});
            this.mRedSize = redSize;
            this.mGreenSize = greenSize;
            this.mBlueSize = blueSize;
            this.mAlphaSize = alphaSize;
            this.mDepthSize = depthSize;
            this.mStencilSize = stencilSize;
        }

        @Override // android.opengl.GLSurfaceView.BaseConfigChooser
        public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display, EGLConfig[] configs) {
            for (EGLConfig config : configs) {
                int d = findConfigAttrib(egl, display, config, 12325, 0);
                int s = findConfigAttrib(egl, display, config, 12326, 0);
                if (d >= this.mDepthSize && s >= this.mStencilSize) {
                    int r = findConfigAttrib(egl, display, config, 12324, 0);
                    int g = findConfigAttrib(egl, display, config, 12323, 0);
                    int b = findConfigAttrib(egl, display, config, 12322, 0);
                    int a = findConfigAttrib(egl, display, config, 12321, 0);
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

    private class SimpleEGLConfigChooser extends ComponentSizeChooser {
        /* JADX INFO: super call moved to the top of the method (can break code semantics) */
        public SimpleEGLConfigChooser(boolean withDepthBuffer) {
            super(8, 8, 8, 0, withDepthBuffer ? 16 : 0, 0);
        }
    }

    private static class EglHelper {
        EGL10 mEgl;
        EGLConfig mEglConfig;
        @UnsupportedAppUsage
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
            if (this.mEglDisplay != EGL10.EGL_NO_DISPLAY) {
                if (this.mEgl.eglInitialize(this.mEglDisplay, new int[2])) {
                    GLSurfaceView view = this.mGLSurfaceViewWeakRef.get();
                    if (view == null) {
                        this.mEglConfig = null;
                        this.mEglContext = null;
                    } else {
                        this.mEglConfig = view.mEGLConfigChooser.chooseConfig(this.mEgl, this.mEglDisplay);
                        this.mEglContext = view.mEGLContextFactory.createContext(this.mEgl, this.mEglDisplay, this.mEglConfig);
                    }
                    EGLContext eGLContext = this.mEglContext;
                    if (eGLContext == null || eGLContext == EGL10.EGL_NO_CONTEXT) {
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
            throw new RuntimeException("eglGetDisplay failed");
        }

        public boolean createSurface() {
            if (GLSurfaceView.LOG_EGL) {
                Log.w("EglHelper", "createSurface()  tid=" + Thread.currentThread().getId());
            }
            if (this.mEgl == null) {
                throw new RuntimeException("egl not initialized");
            } else if (this.mEglDisplay == null) {
                throw new RuntimeException("eglDisplay not initialized");
            } else if (this.mEglConfig != null) {
                destroySurfaceImp();
                GLSurfaceView view = this.mGLSurfaceViewWeakRef.get();
                if (view != null) {
                    this.mEglSurface = view.mEGLWindowSurfaceFactory.createWindowSurface(this.mEgl, this.mEglDisplay, this.mEglConfig, view.getHolder());
                } else {
                    this.mEglSurface = null;
                }
                EGLSurface eGLSurface = this.mEglSurface;
                if (eGLSurface == null || eGLSurface == EGL10.EGL_NO_SURFACE) {
                    if (this.mEgl.eglGetError() == 12299) {
                        Log.e("EglHelper", "createWindowSurface returned EGL_BAD_NATIVE_WINDOW.");
                    }
                    return false;
                }
                EGL10 egl10 = this.mEgl;
                EGLDisplay eGLDisplay = this.mEglDisplay;
                EGLSurface eGLSurface2 = this.mEglSurface;
                if (egl10.eglMakeCurrent(eGLDisplay, eGLSurface2, eGLSurface2, this.mEglContext)) {
                    return true;
                }
                logEglErrorAsWarning("EGLHelper", "eglMakeCurrent", this.mEgl.eglGetError());
                return false;
            } else {
                throw new RuntimeException("mEglConfig not initialized");
            }
        }

        /* access modifiers changed from: package-private */
        public GL createGL() {
            GL gl = this.mEglContext.getGL();
            GLSurfaceView view = this.mGLSurfaceViewWeakRef.get();
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
            Writer log = null;
            if ((view.mDebugFlags & 1) != 0) {
                configFlags = 0 | 1;
            }
            if ((view.mDebugFlags & 2) != 0) {
                log = new LogWriter();
            }
            return GLDebugHelper.wrap(gl, configFlags, log);
        }

        public int swap() {
            if (!this.mEgl.eglSwapBuffers(this.mEglDisplay, this.mEglSurface)) {
                return this.mEgl.eglGetError();
            }
            return 12288;
        }

        public void destroySurface() {
            if (GLSurfaceView.LOG_EGL) {
                Log.w("EglHelper", "destroySurface()  tid=" + Thread.currentThread().getId());
            }
            destroySurfaceImp();
        }

        private void destroySurfaceImp() {
            EGLSurface eGLSurface = this.mEglSurface;
            if (eGLSurface != null && eGLSurface != EGL10.EGL_NO_SURFACE) {
                this.mEgl.eglMakeCurrent(this.mEglDisplay, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_CONTEXT);
                GLSurfaceView view = this.mGLSurfaceViewWeakRef.get();
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
                GLSurfaceView view = this.mGLSurfaceViewWeakRef.get();
                if (view != null) {
                    view.mEGLContextFactory.destroyContext(this.mEgl, this.mEglDisplay, this.mEglContext);
                }
                this.mEglContext = null;
            }
            EGLDisplay eGLDisplay = this.mEglDisplay;
            if (eGLDisplay != null) {
                this.mEgl.eglTerminate(eGLDisplay);
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
        @UnsupportedAppUsage
        private EglHelper mEglHelper;
        private ArrayList<Runnable> mEventQueue = new ArrayList<>();
        /* access modifiers changed from: private */
        public boolean mExited;
        private Runnable mFinishDrawingRunnable = null;
        private boolean mFinishedCreatingEglSurface;
        private WeakReference<GLSurfaceView> mGLSurfaceViewWeakRef;
        private boolean mHasSurface;
        private boolean mHaveEglContext;
        private boolean mHaveEglSurface;
        private int mHeight = 0;
        boolean mIsNeedDraw = true;
        private boolean mPaused;
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
        }

        public void run() {
            setName("GLThread " + getId());
            if (GLSurfaceView.LOG_THREADS) {
                Log.i("GLThread", "starting tid=" + getId());
            }
            try {
                guardedRun();
            } catch (InterruptedException e) {
            } catch (Throwable th) {
                GLSurfaceView.sGLThreadManager.threadExiting(this);
                throw th;
            }
            GLSurfaceView.sGLThreadManager.threadExiting(this);
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

        /* JADX WARN: Failed to insert an additional move for type inference into block B:56:0x0125 */
        /* JADX DEBUG: Additional 3 move instruction added to help type inference */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r14v9, resolved type: boolean} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r14v11, resolved type: boolean} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r14v13, resolved type: boolean} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r14v14, resolved type: boolean} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r14v16, resolved type: boolean} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r14v17, resolved type: boolean} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r14v19, resolved type: boolean} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r14v20, resolved type: java.lang.StringBuilder} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r14v21, resolved type: boolean} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r14v22, resolved type: boolean} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r14v23, resolved type: boolean} */
        /* JADX WARN: Multi-variable type inference failed */
        /* JADX WARNING: Code restructure failed: missing block: B:146:0x02c0, code lost:
            if (r13 == null) goto L_0x02cd;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:147:0x02c2, code lost:
            r13.run();
            r13 = null;
            r0 = r2;
            r3 = r20;
            r2 = 0;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:148:0x02cd, code lost:
            if (r4 == false) goto L_0x0316;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:150:0x02d3, code lost:
            if (android.opengl.GLSurfaceView.access$1200() == false) goto L_0x02dc;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:151:0x02d5, code lost:
            android.util.Log.w("GLThread", "egl createSurface");
         */
        /* JADX WARNING: Code restructure failed: missing block: B:153:0x02e2, code lost:
            if (r24.mEglHelper.createSurface() == false) goto L_0x02fa;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:154:0x02e4, code lost:
            r3 = android.opengl.GLSurfaceView.access$1000();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:155:0x02e8, code lost:
            monitor-enter(r3);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:158:?, code lost:
            r24.mFinishedCreatingEglSurface = true;
            android.opengl.GLSurfaceView.access$1000().notifyAll();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:159:0x02f3, code lost:
            monitor-exit(r3);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:160:0x02f4, code lost:
            r4 = false;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:164:0x02fa, code lost:
            r3 = android.opengl.GLSurfaceView.access$1000();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:165:0x02fe, code lost:
            monitor-enter(r3);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:168:?, code lost:
            r24.mFinishedCreatingEglSurface = true;
            r24.mSurfaceIsBad = true;
            android.opengl.GLSurfaceView.access$1000().notifyAll();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:169:0x030b, code lost:
            monitor-exit(r3);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:170:0x030c, code lost:
            r0 = r2;
            r3 = r20;
            r2 = 0;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:174:0x0316, code lost:
            if (r5 == false) goto L_0x0324;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:175:0x0318, code lost:
            r5 = false;
            r3 = (javax.microedition.khronos.opengles.GL10) r24.mEglHelper.createGL();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:176:0x0324, code lost:
            r3 = r20;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:178:0x0329, code lost:
            if (r2 == false) goto L_0x036f;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:180:0x032f, code lost:
            if (android.opengl.GLSurfaceView.access$1400() == false) goto L_0x0339;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:181:0x0331, code lost:
            android.util.Log.w("GLThread", "onSurfaceCreated");
         */
        /* JADX WARNING: Code restructure failed: missing block: B:182:0x0339, code lost:
            r0 = r24.mGLSurfaceViewWeakRef.get();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:183:0x0342, code lost:
            if (r0 == null) goto L_0x036a;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:184:0x0344, code lost:
            r17 = r6;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:186:?, code lost:
            android.os.Trace.traceBegin(8, "onSurfaceCreated");
            android.opengl.GLSurfaceView.access$1500(r0).onSurfaceCreated(r3, r24.mEglHelper.mEglConfig);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:188:0x035f, code lost:
            r0 = move-exception;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:189:0x0364, code lost:
            android.os.Trace.traceEnd(8);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:190:0x0369, code lost:
            throw r0;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:191:0x036a, code lost:
            r17 = r6;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:192:0x036c, code lost:
            r2 = false;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:193:0x036f, code lost:
            r17 = r6;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:194:0x0371, code lost:
            if (r7 == false) goto L_0x03c4;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:196:0x0377, code lost:
            if (android.opengl.GLSurfaceView.access$1400() == false) goto L_0x039d;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:197:0x0379, code lost:
            android.util.Log.w("GLThread", "onSurfaceChanged(" + r11 + ", " + r12 + ")");
         */
        /* JADX WARNING: Code restructure failed: missing block: B:198:0x039d, code lost:
            r0 = r24.mGLSurfaceViewWeakRef.get();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:199:0x03a6, code lost:
            if (r0 == null) goto L_0x03c2;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:202:?, code lost:
            android.os.Trace.traceBegin(8, "onSurfaceChanged");
            android.opengl.GLSurfaceView.access$1500(r0).onSurfaceChanged(r3, r11, r12);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:203:0x03b7, code lost:
            android.os.Trace.traceEnd(8);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:204:0x03bb, code lost:
            r0 = move-exception;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:205:0x03bc, code lost:
            android.os.Trace.traceEnd(8);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:206:0x03c1, code lost:
            throw r0;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:207:0x03c2, code lost:
            r7 = false;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:209:0x03c8, code lost:
            if (android.opengl.GLSurfaceView.access$1600() == false) goto L_0x03e5;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:210:0x03ca, code lost:
            android.util.Log.w("GLThread", "onDrawFrame tid=" + getId());
         */
        /* JADX WARNING: Code restructure failed: missing block: B:212:0x03e8, code lost:
            if (r24.mWidth != 1) goto L_0x03f0;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:214:0x03ec, code lost:
            if (r24.mHeight != 1) goto L_0x03f0;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:215:0x03ee, code lost:
            r0 = true;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:216:0x03f0, code lost:
            r0 = false;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:217:0x03f1, code lost:
            r0 = r24.mGLSurfaceViewWeakRef.get();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:218:0x03fb, code lost:
            if (r0 == false) goto L_0x0445;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:220:0x03ff, code lost:
            if (r24.mIsNeedDraw == false) goto L_0x0445;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:221:0x0401, code lost:
            r18 = false;
            r18 = false;
            r18 = false;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:224:0x0422, code lost:
            r21 = r2;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:227:0x042a, code lost:
            if (android.net.wifi.WifiEnterpriseConfig.ENGINE_ENABLE.equals(com.oppo.atlas.OppoAtlasManager.getInstance(null).getParameters("get_listinfo_bypid=surfaceview-black-list=" + android.os.Binder.getCallingPid())) == false) goto L_0x0432;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:230:?, code lost:
            r24.mIsNeedDraw = false;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:231:0x0430, code lost:
            r0 = e;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:233:0x0434, code lost:
            r0 = e;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:235:0x0437, code lost:
            r0 = e;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:236:0x0438, code lost:
            r21 = r2;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:237:0x043b, code lost:
            r22 = r0;
            android.util.Log.e(android.opengl.GLSurfaceView.TAG, "OppoMultimediaService getParameters failed");
         */
        /* JADX WARNING: Code restructure failed: missing block: B:238:0x0445, code lost:
            r21 = r2;
            r18 = false;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:239:0x0449, code lost:
            if (r0 == null) goto L_0x0471;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:240:0x044b, code lost:
            r2 = r7;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:242:?, code lost:
            android.os.Trace.traceBegin(8, "onDrawFrame");
            android.opengl.GLSurfaceView.access$1500(r0).onDrawFrame(r3);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:243:0x045b, code lost:
            if (r15 != null) goto L_0x045d;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:244:0x045d, code lost:
            r15.run();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:245:0x0460, code lost:
            r15 = null;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:247:0x0467, code lost:
            r0 = move-exception;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:248:0x046b, code lost:
            android.os.Trace.traceEnd(8);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:249:0x0470, code lost:
            throw r0;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:250:0x0471, code lost:
            r2 = r7;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:252:0x0476, code lost:
            if (r24.mIsNeedDraw == false) goto L_0x047f;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:253:0x0478, code lost:
            r0 = r24.mEglHelper.swap();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:254:0x047f, code lost:
            r0 = 12288;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:256:0x0481, code lost:
            if (r0 != 12288) goto L_0x0483;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:258:0x0485, code lost:
            if (r0 != 12302) goto L_0x0487;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:259:0x0487, code lost:
            android.opengl.GLSurfaceView.EglHelper.logEglErrorAsWarning("GLThread", "eglSwapBuffers", r0);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:260:0x0492, code lost:
            monitor-enter(android.opengl.GLSurfaceView.access$1000());
         */
        /* JADX WARNING: Code restructure failed: missing block: B:263:?, code lost:
            r24.mSurfaceIsBad = true;
            android.opengl.GLSurfaceView.access$1000().notifyAll();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:270:0x04a6, code lost:
            if (android.opengl.GLSurfaceView.access$1200() != false) goto L_0x04a8;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:271:0x04a8, code lost:
            android.util.Log.i("GLThread", "egl context lost tid=" + getId());
         */
        /* JADX WARNING: Code restructure failed: missing block: B:272:0x04c2, code lost:
            r6 = 1;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:273:0x04c6, code lost:
            r6 = r17;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:274:0x04c8, code lost:
            if (r8 != false) goto L_0x04ca;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:275:0x04ca, code lost:
            r9 = true;
            r8 = false;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:276:0x04ce, code lost:
            r7 = r2;
            r4 = r4;
            r5 = r5;
            r0 = r21;
            r2 = 0;
         */
        /* JADX WARNING: Removed duplicated region for block: B:101:0x021b A[Catch:{ all -> 0x016b, all -> 0x05b0 }] */
        /* JADX WARNING: Removed duplicated region for block: B:104:0x0227 A[Catch:{ all -> 0x016b, all -> 0x05b0 }] */
        /* JADX WARNING: Removed duplicated region for block: B:240:0x044b  */
        /* JADX WARNING: Removed duplicated region for block: B:250:0x0471  */
        /* JADX WARNING: Removed duplicated region for block: B:253:0x0478  */
        /* JADX WARNING: Removed duplicated region for block: B:254:0x047f  */
        /* JADX WARNING: Removed duplicated region for block: B:257:0x0483  */
        /* JADX WARNING: Removed duplicated region for block: B:275:0x04ca  */
        /* JADX WARNING: Removed duplicated region for block: B:282:0x04ec  */
        /* JADX WARNING: Removed duplicated region for block: B:288:0x0509  */
        /* JADX WARNING: Removed duplicated region for block: B:289:0x0594  */
        /* JADX WARNING: Removed duplicated region for block: B:81:0x0184 A[Catch:{ all -> 0x016b, all -> 0x05b0 }] */
        /* JADX WARNING: Removed duplicated region for block: B:84:0x01a3 A[Catch:{ all -> 0x016b, all -> 0x05b0 }] */
        /* JADX WARNING: Removed duplicated region for block: B:92:0x01c1 A[Catch:{ all -> 0x016b, all -> 0x05b0 }] */
        /* JADX WARNING: Removed duplicated region for block: B:95:0x01e8 A[Catch:{ all -> 0x016b, all -> 0x05b0 }] */
        private void guardedRun() throws InterruptedException {
            NullPointerException nullPointerException;
            GL10 gl;
            boolean createEglContext;
            boolean createEglContext2;
            boolean pausing;
            boolean createGlInterface;
            boolean z;
            boolean createEglContext3;
            boolean createEglContext4;
            boolean createEglContext5;
            this.mEglHelper = new EglHelper(this.mGLSurfaceViewWeakRef);
            int i = 0;
            this.mHaveEglContext = false;
            this.mHaveEglSurface = false;
            this.mWantRenderNotification = false;
            boolean createEglSurface = false;
            boolean createGlInterface2 = false;
            NullPointerException nullPointerException2 = null;
            boolean sizeChanged = false;
            boolean wantRenderNotification = false;
            boolean doRenderNotification = false;
            boolean askedToReleaseEglContext = false;
            int w = 0;
            int h = 0;
            Runnable event = null;
            Runnable finishDrawingRunnable = null;
            GL10 gl2 = null;
            boolean createEglContext6 = false;
            while (true) {
                try {
                    synchronized (GLSurfaceView.sGLThreadManager) {
                        boolean createEglSurface2 = createEglSurface;
                        boolean createEglSurface3 = createEglContext6;
                        while (!this.mShouldExit) {
                            try {
                                if (!this.mEventQueue.isEmpty()) {
                                    event = this.mEventQueue.remove(i);
                                    gl = gl2;
                                    createEglContext = createEglSurface3;
                                    createEglSurface = createEglSurface2;
                                } else {
                                    boolean z2 = this.mPaused;
                                    if (z2 != this.mRequestPaused) {
                                        try {
                                            boolean pausing2 = this.mRequestPaused;
                                            this.mPaused = this.mRequestPaused;
                                            GLSurfaceView.sGLThreadManager.notifyAll();
                                            if (GLSurfaceView.LOG_PAUSE_RESUME) {
                                                StringBuilder sb = new StringBuilder();
                                                pausing = pausing2;
                                                sb.append("mPaused is now ");
                                                sb.append(this.mPaused);
                                                sb.append(" tid=");
                                                gl = gl2;
                                                createEglContext2 = createEglSurface3;
                                                try {
                                                    sb.append(getId());
                                                    Log.i("GLThread", sb.toString());
                                                    createGlInterface = sb;
                                                } catch (Throwable th) {
                                                    th = th;
                                                }
                                            } else {
                                                pausing = pausing2;
                                                gl = gl2;
                                                createEglContext2 = createEglSurface3;
                                                createGlInterface = z2;
                                            }
                                        } catch (Throwable th2) {
                                            th = th2;
                                            while (true) {
                                                try {
                                                    break;
                                                } catch (Throwable th3) {
                                                    th = th3;
                                                }
                                            }
                                            throw th;
                                        }
                                    } else {
                                        gl = gl2;
                                        createEglContext2 = createEglSurface3;
                                        pausing = false;
                                        createGlInterface = z2;
                                    }
                                    try {
                                        if (this.mShouldReleaseEglContext) {
                                            if (GLSurfaceView.LOG_SURFACE) {
                                                Log.i("GLThread", "releasing EGL context because asked to tid=" + getId());
                                            }
                                            stopEglSurfaceLocked();
                                            stopEglContextLocked();
                                            this.mShouldReleaseEglContext = false;
                                            askedToReleaseEglContext = true;
                                        }
                                        if (nullPointerException2 != null) {
                                            stopEglSurfaceLocked();
                                            stopEglContextLocked();
                                            nullPointerException2 = null;
                                        }
                                        if (pausing && this.mHaveEglSurface) {
                                            if (GLSurfaceView.LOG_SURFACE) {
                                                Log.i("GLThread", "releasing EGL surface because paused tid=" + getId());
                                            }
                                            stopEglSurfaceLocked();
                                        }
                                        if (pausing) {
                                            try {
                                                if (this.mHaveEglContext) {
                                                    GLSurfaceView view = this.mGLSurfaceViewWeakRef.get();
                                                    if (!(view == null ? false : view.mPreserveEGLContextOnPause)) {
                                                        stopEglContextLocked();
                                                        if (GLSurfaceView.LOG_SURFACE) {
                                                            StringBuilder sb2 = new StringBuilder();
                                                            sb2.append("releasing EGL context because paused tid=");
                                                            createGlInterface = createGlInterface2;
                                                            nullPointerException = nullPointerException2;
                                                            sb2.append(getId());
                                                            Log.i("GLThread", sb2.toString());
                                                        } else {
                                                            createGlInterface = createGlInterface2;
                                                            nullPointerException = nullPointerException2;
                                                        }
                                                    } else {
                                                        createGlInterface = createGlInterface2;
                                                        nullPointerException = nullPointerException2;
                                                    }
                                                    if (!this.mHasSurface && !this.mWaitingForSurface) {
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
                                                    if (this.mFinishDrawingRunnable != null) {
                                                        finishDrawingRunnable = this.mFinishDrawingRunnable;
                                                        this.mFinishDrawingRunnable = null;
                                                    }
                                                    if (!readyToDraw()) {
                                                        if (this.mHaveEglContext) {
                                                            createEglContext3 = createEglContext2;
                                                        } else if (askedToReleaseEglContext) {
                                                            askedToReleaseEglContext = false;
                                                            createEglContext3 = createEglContext2;
                                                        } else {
                                                            try {
                                                                this.mEglHelper.start();
                                                                this.mHaveEglContext = true;
                                                                createEglContext3 = true;
                                                                try {
                                                                    GLSurfaceView.sGLThreadManager.notifyAll();
                                                                } catch (Throwable th4) {
                                                                    th = th4;
                                                                }
                                                            } catch (RuntimeException t) {
                                                                GLSurfaceView.sGLThreadManager.releaseEglContextLocked(this);
                                                                throw t;
                                                            }
                                                        }
                                                        try {
                                                            if (!this.mHaveEglContext || this.mHaveEglSurface) {
                                                                createGlInterface2 = createGlInterface;
                                                            } else {
                                                                this.mHaveEglSurface = true;
                                                                createEglSurface2 = true;
                                                                createGlInterface2 = true;
                                                                sizeChanged = true;
                                                            }
                                                        } catch (Throwable th5) {
                                                            th = th5;
                                                            while (true) {
                                                                break;
                                                            }
                                                            throw th;
                                                        }
                                                        try {
                                                            if (this.mHaveEglSurface) {
                                                                if (this.mSizeChanged) {
                                                                    sizeChanged = true;
                                                                    w = this.mWidth;
                                                                    h = this.mHeight;
                                                                    this.mWantRenderNotification = true;
                                                                    if (GLSurfaceView.LOG_SURFACE) {
                                                                        StringBuilder sb3 = new StringBuilder();
                                                                        sb3.append("noticing that we want render notification tid=");
                                                                        createEglContext5 = createEglContext3;
                                                                        try {
                                                                            sb3.append(getId());
                                                                            Log.i("GLThread", sb3.toString());
                                                                        } catch (Throwable th6) {
                                                                            th = th6;
                                                                            while (true) {
                                                                                break;
                                                                            }
                                                                            throw th;
                                                                        }
                                                                    } else {
                                                                        createEglContext5 = createEglContext3;
                                                                    }
                                                                    createEglSurface2 = true;
                                                                    this.mSizeChanged = false;
                                                                } else {
                                                                    createEglContext5 = createEglContext3;
                                                                }
                                                                this.mRequestRender = false;
                                                                GLSurfaceView.sGLThreadManager.notifyAll();
                                                                if (this.mWantRenderNotification) {
                                                                    wantRenderNotification = true;
                                                                    createEglContext = createEglContext5;
                                                                    createEglSurface = createEglSurface2;
                                                                    nullPointerException2 = nullPointerException;
                                                                } else {
                                                                    createEglContext = createEglContext5;
                                                                    createEglSurface = createEglSurface2;
                                                                    nullPointerException2 = nullPointerException;
                                                                }
                                                            } else {
                                                                z = false;
                                                            }
                                                        } catch (Throwable th7) {
                                                            th = th7;
                                                            while (true) {
                                                                break;
                                                            }
                                                            throw th;
                                                        }
                                                    } else {
                                                        z = false;
                                                        z = false;
                                                        if (finishDrawingRunnable != null) {
                                                            Log.w(GLSurfaceView.TAG, "Warning, !readyToDraw() but waiting for draw finished! Early reporting draw finished.");
                                                            finishDrawingRunnable.run();
                                                            finishDrawingRunnable = null;
                                                            createGlInterface2 = createGlInterface;
                                                            createEglContext3 = createEglContext2;
                                                        } else {
                                                            createGlInterface2 = createGlInterface;
                                                            createEglContext3 = createEglContext2;
                                                        }
                                                    }
                                                    if (!GLSurfaceView.LOG_THREADS) {
                                                        StringBuilder sb4 = new StringBuilder();
                                                        sb4.append("waiting tid=");
                                                        createEglContext4 = createEglContext3;
                                                        sb4.append(getId());
                                                        sb4.append(" mHaveEglContext: ");
                                                        sb4.append(this.mHaveEglContext);
                                                        sb4.append(" mHaveEglSurface: ");
                                                        sb4.append(this.mHaveEglSurface);
                                                        sb4.append(" mFinishedCreatingEglSurface: ");
                                                        sb4.append(this.mFinishedCreatingEglSurface);
                                                        sb4.append(" mPaused: ");
                                                        sb4.append(this.mPaused);
                                                        sb4.append(" mHasSurface: ");
                                                        sb4.append(this.mHasSurface);
                                                        sb4.append(" mSurfaceIsBad: ");
                                                        sb4.append(this.mSurfaceIsBad);
                                                        sb4.append(" mWaitingForSurface: ");
                                                        sb4.append(this.mWaitingForSurface);
                                                        sb4.append(" mWidth: ");
                                                        sb4.append(this.mWidth);
                                                        sb4.append(" mHeight: ");
                                                        sb4.append(this.mHeight);
                                                        sb4.append(" mRequestRender: ");
                                                        sb4.append(this.mRequestRender);
                                                        sb4.append(" mRenderMode: ");
                                                        sb4.append(this.mRenderMode);
                                                        Log.i("GLThread", sb4.toString());
                                                    } else {
                                                        createEglContext4 = createEglContext3;
                                                    }
                                                    GLSurfaceView.sGLThreadManager.wait();
                                                    createEglSurface3 = createEglContext4;
                                                    gl2 = gl;
                                                    nullPointerException2 = nullPointerException;
                                                    i = 0;
                                                }
                                            } catch (Throwable th8) {
                                                th = th8;
                                                while (true) {
                                                    break;
                                                }
                                                throw th;
                                            }
                                        }
                                        createGlInterface = createGlInterface2;
                                        nullPointerException = nullPointerException2;
                                        if (GLSurfaceView.LOG_SURFACE) {
                                        }
                                        if (this.mHaveEglSurface) {
                                        }
                                        this.mWaitingForSurface = true;
                                        this.mSurfaceIsBad = false;
                                        GLSurfaceView.sGLThreadManager.notifyAll();
                                        if (GLSurfaceView.LOG_SURFACE) {
                                        }
                                        this.mWaitingForSurface = false;
                                        GLSurfaceView.sGLThreadManager.notifyAll();
                                        if (doRenderNotification) {
                                        }
                                        if (this.mFinishDrawingRunnable != null) {
                                        }
                                        if (!readyToDraw()) {
                                        }
                                        if (!GLSurfaceView.LOG_THREADS) {
                                        }
                                        GLSurfaceView.sGLThreadManager.wait();
                                        createEglSurface3 = createEglContext4;
                                        gl2 = gl;
                                        nullPointerException2 = nullPointerException;
                                        i = 0;
                                    } catch (Throwable th9) {
                                        th = th9;
                                        while (true) {
                                            break;
                                        }
                                        throw th;
                                    }
                                }
                                try {
                                } catch (Throwable th10) {
                                    th = th10;
                                    while (true) {
                                        break;
                                    }
                                    throw th;
                                }
                            } catch (Throwable th11) {
                                th = th11;
                                while (true) {
                                    break;
                                }
                                throw th;
                            }
                        }
                        try {
                            synchronized (GLSurfaceView.sGLThreadManager) {
                                stopEglSurfaceLocked();
                                stopEglContextLocked();
                            }
                            return;
                        } catch (Throwable th12) {
                            th = th12;
                            while (true) {
                                break;
                            }
                            throw th;
                        }
                    }
                } catch (Throwable th13) {
                    synchronized (GLSurfaceView.sGLThreadManager) {
                        stopEglSurfaceLocked();
                        stopEglContextLocked();
                        throw th13;
                    }
                }
            }
        }

        public boolean ableToDraw() {
            return this.mHaveEglContext && this.mHaveEglSurface && readyToDraw();
        }

        private boolean readyToDraw() {
            return !this.mPaused && this.mHasSurface && !this.mSurfaceIsBad && this.mWidth > 0 && this.mHeight > 0 && (this.mRequestRender || this.mRenderMode == 1);
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
                this.mRequestRender = true;
                GLSurfaceView.sGLThreadManager.notifyAll();
            }
        }

        public void requestRenderAndNotify(Runnable finishDrawing) {
            synchronized (GLSurfaceView.sGLThreadManager) {
                if (Thread.currentThread() != this) {
                    this.mWantRenderNotification = true;
                    this.mRequestRender = true;
                    this.mRenderComplete = false;
                    this.mFinishDrawingRunnable = finishDrawing;
                    GLSurfaceView.sGLThreadManager.notifyAll();
                }
            }
        }

        public void surfaceCreated() {
            synchronized (GLSurfaceView.sGLThreadManager) {
                if (GLSurfaceView.LOG_THREADS) {
                    Log.i("GLThread", "surfaceCreated tid=" + getId());
                }
                this.mHasSurface = true;
                this.mFinishedCreatingEglSurface = false;
                GLSurfaceView.sGLThreadManager.notifyAll();
                while (this.mWaitingForSurface && !this.mFinishedCreatingEglSurface && !this.mExited) {
                    try {
                        GLSurfaceView.sGLThreadManager.wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }

        public void surfaceDestroyed() {
            synchronized (GLSurfaceView.sGLThreadManager) {
                if (GLSurfaceView.LOG_THREADS) {
                    Log.i("GLThread", "surfaceDestroyed tid=" + getId());
                }
                this.mHasSurface = false;
                GLSurfaceView.sGLThreadManager.notifyAll();
                while (!this.mWaitingForSurface && !this.mExited) {
                    try {
                        GLSurfaceView.sGLThreadManager.wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }

        public void onPause() {
            synchronized (GLSurfaceView.sGLThreadManager) {
                if (GLSurfaceView.LOG_PAUSE_RESUME) {
                    Log.i("GLThread", "onPause tid=" + getId());
                }
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
            }
        }

        public void onResume() {
            synchronized (GLSurfaceView.sGLThreadManager) {
                if (GLSurfaceView.LOG_PAUSE_RESUME) {
                    Log.i("GLThread", "onResume tid=" + getId());
                }
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
            }
        }

        /* JADX WARNING: Code restructure failed: missing block: B:24:0x0065, code lost:
            return;
         */
        public void onWindowResize(int w, int h) {
            synchronized (GLSurfaceView.sGLThreadManager) {
                this.mWidth = w;
                this.mHeight = h;
                this.mSizeChanged = true;
                this.mRequestRender = true;
                this.mRenderComplete = false;
                if (Thread.currentThread() != this) {
                    GLSurfaceView.sGLThreadManager.notifyAll();
                    while (!this.mExited && !this.mPaused && !this.mRenderComplete && ableToDraw()) {
                        if (GLSurfaceView.LOG_SURFACE) {
                            Log.i("Main thread", "onWindowResize waiting for render complete from tid=" + getId());
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

        public void requestExitAndWait() {
            synchronized (GLSurfaceView.sGLThreadManager) {
                this.mShouldExit = true;
                GLSurfaceView.sGLThreadManager.notifyAll();
                while (!this.mExited) {
                    try {
                        GLSurfaceView.sGLThreadManager.wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }

        public void requestReleaseEglContextLocked() {
            this.mShouldReleaseEglContext = true;
            GLSurfaceView.sGLThreadManager.notifyAll();
        }

        public void queueEvent(Runnable r) {
            if (r != null) {
                synchronized (GLSurfaceView.sGLThreadManager) {
                    this.mEventQueue.add(r);
                    GLSurfaceView.sGLThreadManager.notifyAll();
                }
                return;
            }
            throw new IllegalArgumentException("r must not be null");
        }
    }

    static class LogWriter extends Writer {
        private StringBuilder mBuilder = new StringBuilder();

        LogWriter() {
        }

        @Override // java.io.Closeable, java.io.Writer, java.lang.AutoCloseable
        public void close() {
            flushBuilder();
        }

        @Override // java.io.Writer, java.io.Flushable
        public void flush() {
            flushBuilder();
        }

        @Override // java.io.Writer
        public void write(char[] buf, int offset, int count) {
            for (int i = 0; i < count; i++) {
                char c = buf[offset + i];
                if (c == 10) {
                    flushBuilder();
                } else {
                    this.mBuilder.append(c);
                }
            }
        }

        private void flushBuilder() {
            if (this.mBuilder.length() > 0) {
                Log.v(GLSurfaceView.TAG, this.mBuilder.toString());
                StringBuilder sb = this.mBuilder;
                sb.delete(0, sb.length());
            }
        }
    }

    private void checkRenderThreadState() {
        if (this.mGLThread != null) {
            throw new IllegalStateException("setRenderer has already been called for this instance.");
        }
    }

    /* access modifiers changed from: private */
    public static class GLThreadManager {
        private static String TAG = "GLThreadManager";

        private GLThreadManager() {
        }

        public synchronized void threadExiting(GLThread thread) {
            if (GLSurfaceView.LOG_THREADS) {
                Log.i("GLThread", "exiting tid=" + thread.getId());
            }
            boolean unused = thread.mExited = true;
            notifyAll();
        }

        public void releaseEglContextLocked(GLThread thread) {
            notifyAll();
        }
    }
}
