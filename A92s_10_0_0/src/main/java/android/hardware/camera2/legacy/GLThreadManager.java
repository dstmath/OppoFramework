package android.hardware.camera2.legacy;

import android.graphics.SurfaceTexture;
import android.hardware.camera2.legacy.RequestThreadManager;
import android.os.ConditionVariable;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.Pair;
import android.util.Size;
import android.view.Surface;
import com.android.internal.util.Preconditions;
import java.util.Collection;

public class GLThreadManager {
    /* access modifiers changed from: private */
    public static final boolean DEBUG = ParameterUtils.DEBUG;
    private static final int GL_THREAD_TIMEOUT = 2000;
    private static final int MSG_ALLOW_FRAMES = 5;
    private static final int MSG_CLEANUP = 3;
    private static final int MSG_DROP_FRAMES = 4;
    private static final int MSG_NEW_CONFIGURATION = 1;
    private static final int MSG_NEW_FRAME = 2;
    private static final int STATE_CONFIGURING = 2;
    /* access modifiers changed from: private */
    public final String TAG;
    /* access modifiers changed from: private */
    public CaptureCollector mCaptureCollector;
    /* access modifiers changed from: private */
    public final CameraDeviceState mDeviceState;
    private final Handler.Callback mGLHandlerCb = new Handler.Callback() {
        /* class android.hardware.camera2.legacy.GLThreadManager.AnonymousClass1 */
        private boolean mCleanup = false;
        private boolean mConfigured = false;
        private boolean mDroppingFrames = false;

        @Override // android.os.Handler.Callback
        public boolean handleMessage(Message msg) {
            ConfigureHolder configure;
            if (this.mCleanup) {
                return true;
            }
            try {
                int i = msg.what;
                if (i != -1) {
                    if (i == 1) {
                        ConfigureHolder configure2 = (ConfigureHolder) msg.obj;
                        GLThreadManager.this.mTextureRenderer.cleanupEGLContext();
                        GLThreadManager.this.mTextureRenderer.configureSurfaces(configure2.surfaces);
                        CaptureCollector unused = GLThreadManager.this.mCaptureCollector = (CaptureCollector) Preconditions.checkNotNull(configure2.collector);
                        configure2.condition.open();
                        this.mConfigured = true;
                    } else if (i != 2) {
                        if (i == 3) {
                            GLThreadManager.this.mTextureRenderer.cleanupEGLContext();
                            this.mCleanup = true;
                            this.mConfigured = false;
                        } else if (i == 4) {
                            this.mDroppingFrames = true;
                        } else if (i != 5) {
                            String access$200 = GLThreadManager.this.TAG;
                            Log.e(access$200, "Unhandled message " + msg.what + " on GLThread.");
                        } else {
                            this.mDroppingFrames = false;
                        }
                    } else if (this.mDroppingFrames) {
                        Log.w(GLThreadManager.this.TAG, "Ignoring frame.");
                    } else {
                        if (GLThreadManager.DEBUG) {
                            GLThreadManager.this.mPrevCounter.countAndLog();
                        }
                        if (!this.mConfigured) {
                            Log.e(GLThreadManager.this.TAG, "Dropping frame, EGL context not configured!");
                        }
                        if (GLThreadManager.this.mDeviceState.getCurrentState() == 2) {
                            Log.e(GLThreadManager.this.TAG, "Current device state is configring!");
                        } else {
                            GLThreadManager.this.mTextureRenderer.drawIntoSurfaces(GLThreadManager.this.mCaptureCollector);
                        }
                    }
                }
            } catch (Exception e) {
                Log.e(GLThreadManager.this.TAG, "Received exception on GL render thread: ", e);
                GLThreadManager.this.mDeviceState.setError(1);
                if (1 == msg.what && (configure = (ConfigureHolder) msg.obj) != null) {
                    configure.condition.open();
                }
            }
            return true;
        }
    };
    private final RequestHandlerThread mGLHandlerThread;
    /* access modifiers changed from: private */
    public final RequestThreadManager.FpsCounter mPrevCounter = new RequestThreadManager.FpsCounter("GL Preview Producer");
    /* access modifiers changed from: private */
    public final SurfaceTextureRenderer mTextureRenderer;

    private static class ConfigureHolder {
        public final CaptureCollector collector;
        public final ConditionVariable condition;
        public final Collection<Pair<Surface, Size>> surfaces;

        public ConfigureHolder(ConditionVariable condition2, Collection<Pair<Surface, Size>> surfaces2, CaptureCollector collector2) {
            this.condition = condition2;
            this.surfaces = surfaces2;
            this.collector = collector2;
        }
    }

    public GLThreadManager(int cameraId, int facing, CameraDeviceState state) {
        this.mTextureRenderer = new SurfaceTextureRenderer(facing);
        this.TAG = String.format("CameraDeviceGLThread-%d", Integer.valueOf(cameraId));
        this.mGLHandlerThread = new RequestHandlerThread(this.TAG, this.mGLHandlerCb);
        this.mDeviceState = state;
    }

    public void start() {
        this.mGLHandlerThread.start();
    }

    public void waitUntilStarted() {
        this.mGLHandlerThread.waitUntilStarted();
    }

    public void quit() {
        Handler handler = this.mGLHandlerThread.getHandler();
        handler.sendMessageAtFrontOfQueue(handler.obtainMessage(3));
        this.mGLHandlerThread.quitSafely();
        try {
            this.mGLHandlerThread.join(2000);
        } catch (InterruptedException e) {
            Log.e(this.TAG, String.format("Thread %s (%d) interrupted while quitting.", this.mGLHandlerThread.getName(), Long.valueOf(this.mGLHandlerThread.getId())));
        }
    }

    public void queueNewFrame() {
        Handler handler = this.mGLHandlerThread.getHandler();
        if (!handler.hasMessages(2)) {
            handler.sendMessage(handler.obtainMessage(2));
        } else {
            Log.e(this.TAG, "GLThread dropping frame.  Not consuming frames quickly enough!");
        }
    }

    public void setConfigurationAndWait(Collection<Pair<Surface, Size>> surfaces, CaptureCollector collector) {
        Preconditions.checkNotNull(collector, "collector must not be null");
        Handler handler = this.mGLHandlerThread.getHandler();
        ConditionVariable condition = new ConditionVariable(false);
        handler.sendMessage(handler.obtainMessage(1, 0, 0, new ConfigureHolder(condition, surfaces, collector)));
        condition.block();
    }

    public SurfaceTexture getCurrentSurfaceTexture() {
        return this.mTextureRenderer.getSurfaceTexture();
    }

    public void ignoreNewFrames() {
        this.mGLHandlerThread.getHandler().sendEmptyMessage(4);
    }

    public void waitUntilIdle() {
        this.mGLHandlerThread.waitUntilIdle();
    }

    public void allowNewFrames() {
        this.mGLHandlerThread.getHandler().sendEmptyMessage(5);
    }
}
