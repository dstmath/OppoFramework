package android.view;

import android.content.Context;
import android.content.res.CompatibilityInfo.Translator;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff.Mode;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.Region.Op;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.Trace;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceHolder.Callback2;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.view.ViewTreeObserver.OnScrollChangedListener;
import android.view.WindowManager.LayoutParams;
import com.android.internal.view.BaseIWindow;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;

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
public class SurfaceView extends View {
    private static final int DBG_UPDATE_CONTENT = 2;
    private static final int DBG_UPDATE_WINDOW = 1;
    private static boolean DEBUG = false;
    private static boolean DEBUG_PANIC = false;
    static final int FORCE_UPDATE_WINDOW_MSG = 4;
    static final int GET_NEW_SURFACE_MSG = 2;
    private static final boolean IS_ENG_BUILD = false;
    static final int KEEP_SCREEN_ON_MSG = 1;
    private static final String LOG_PROPERTY_NAME = "debug.surfaceview.dumpinfo";
    private static boolean LOG_UPDATE_CONTENT = false;
    private static boolean LOG_UPDATE_WINDOW = false;
    private static final int MSG_RELAYOUT_RETRY = 101;
    private static final int MSG_RELAYOUT_RETRY_DELAY = 200;
    private static final String TAG = "SurfaceView";
    static final int UPDATE_WINDOW_MSG = 3;
    final Rect mBackdropFrame;
    final ArrayList<Callback> mCallbacks;
    final Configuration mConfiguration;
    final Rect mContentInsets;
    private boolean mCustomSet;
    private final OnPreDrawListener mDrawListener;
    boolean mDrawingStopped;
    int mFormat;
    private boolean mGlobalListenersAdded;
    final Handler mHandler;
    boolean mHaveFrame;
    boolean mIsCreating;
    int mLastHeight;
    long mLastLockTime;
    int mLastSurfaceHeight;
    int mLastSurfaceWidth;
    int mLastWidth;
    final LayoutParams mLayout;
    final int[] mLocation;
    final Surface mNewSurface;
    final Rect mOutsets;
    final Rect mOverscanInsets;
    private Rect mRTLastReportedPosition;
    boolean mReportDrawNeeded;
    int mRequestedFormat;
    int mRequestedHeight;
    boolean mRequestedVisible;
    int mRequestedWidth;
    private boolean mRetryLayout;
    private volatile boolean mRtHandlingPositionUpdates;
    private final OnScrollChangedListener mScrollChangedListener;
    IWindowSession mSession;
    final Rect mStableInsets;
    final Surface mSurface;
    boolean mSurfaceCreated;
    final Rect mSurfaceFrame;
    private final SurfaceHolder mSurfaceHolder;
    final ReentrantLock mSurfaceLock;
    final Rect mTmpRect;
    private Translator mTranslator;
    boolean mUpdateWindowNeeded;
    boolean mViewVisibility;
    boolean mVisible;
    final Rect mVisibleInsets;
    final Rect mWinFrame;
    MyWindow mWindow;
    private int mWindowInsetLeft;
    private int mWindowInsetTop;
    int mWindowSpaceHeight;
    int mWindowSpaceLeft;
    int mWindowSpaceTop;
    int mWindowSpaceWidth;
    int mWindowType;
    boolean mWindowVisibility;

    private static class MyWindow extends BaseIWindow {
        int mCurHeight = -1;
        int mCurWidth = -1;
        private final WeakReference<SurfaceView> mSurfaceView;

        public MyWindow(SurfaceView surfaceView) {
            this.mSurfaceView = new WeakReference(surfaceView);
        }

        public void resized(Rect frame, Rect overscanInsets, Rect contentInsets, Rect visibleInsets, Rect stableInsets, Rect outsets, boolean reportDraw, Configuration newConfig, Rect backDropRect, boolean forceLayout, boolean alwaysConsumeNavBar) {
            SurfaceView surfaceView = (SurfaceView) this.mSurfaceView.get();
            if (surfaceView != null) {
                if (SurfaceView.DEBUG) {
                    Log.v(SurfaceView.TAG, "this = " + surfaceView + " got resized: w=" + frame.width() + " h=" + frame.height() + ", cur w=" + this.mCurWidth + " h=" + this.mCurHeight);
                }
                surfaceView.mSurfaceLock.lock();
                if (reportDraw) {
                    try {
                        surfaceView.mUpdateWindowNeeded = true;
                        surfaceView.mReportDrawNeeded = true;
                        surfaceView.mHandler.sendEmptyMessage(3);
                    } catch (Throwable th) {
                        surfaceView.mSurfaceLock.unlock();
                    }
                } else if (!(surfaceView.mWinFrame.width() == frame.width() && surfaceView.mWinFrame.height() == frame.height() && !forceLayout)) {
                    surfaceView.mUpdateWindowNeeded = true;
                    surfaceView.mHandler.sendEmptyMessage(3);
                }
                surfaceView.mSurfaceLock.unlock();
            }
        }

        public void dispatchAppVisibility(boolean visible) {
        }

        public void dispatchGetNewSurface() {
            SurfaceView surfaceView = (SurfaceView) this.mSurfaceView.get();
            if (surfaceView != null) {
                surfaceView.mHandler.sendMessage(surfaceView.mHandler.obtainMessage(2));
            }
        }

        public void windowFocusChanged(boolean hasFocus, boolean touchEnabled) {
            Log.w(SurfaceView.TAG, "Unexpected focus in surface: focus=" + hasFocus + ", touchEnabled=" + touchEnabled + ", this = " + this.mSurfaceView.get());
        }

        public void executeCommand(String command, String parameters, ParcelFileDescriptor out) {
        }

        public void enableLog(boolean enable) {
            SurfaceView surfaceView = (SurfaceView) this.mSurfaceView.get();
            if (surfaceView != null) {
                surfaceView.enableLog(enable);
            }
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.view.SurfaceView.<clinit>():void, dex: 
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
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 9 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.view.SurfaceView.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.view.SurfaceView.<clinit>():void");
    }

    public SurfaceView(Context context) {
        this(context, null);
    }

    public SurfaceView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public SurfaceView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mCallbacks = new ArrayList();
        this.mLocation = new int[2];
        this.mSurfaceLock = new ReentrantLock(true);
        this.mSurface = new Surface();
        this.mNewSurface = new Surface();
        this.mDrawingStopped = true;
        this.mLayout = new LayoutParams();
        this.mVisibleInsets = new Rect();
        this.mWinFrame = new Rect();
        this.mOverscanInsets = new Rect();
        this.mContentInsets = new Rect();
        this.mStableInsets = new Rect();
        this.mOutsets = new Rect();
        this.mBackdropFrame = new Rect();
        this.mTmpRect = new Rect();
        this.mConfiguration = new Configuration();
        this.mWindowType = 1001;
        this.mIsCreating = false;
        this.mRtHandlingPositionUpdates = false;
        this.mHandler = new Handler() {
            public void handleMessage(Message msg) {
                boolean z = true;
                switch (msg.what) {
                    case 1:
                        SurfaceView surfaceView = SurfaceView.this;
                        if (msg.arg1 == 0) {
                            z = false;
                        }
                        surfaceView.setKeepScreenOn(z);
                        return;
                    case 2:
                        SurfaceView.this.handleGetNewSurface();
                        return;
                    case 3:
                        Log.i(SurfaceView.TAG, "updateWindow -- UPDATE_WINDOW_MSG, this = " + this);
                        SurfaceView.this.updateWindow(false, false);
                        return;
                    case 4:
                        Log.i(SurfaceView.TAG, "updateWindow -- FORCE_UPDATE_WINDOW_MSG, this = " + this);
                        SurfaceView.this.updateWindow(true, false);
                        return;
                    case 101:
                        if (SurfaceView.DEBUG_PANIC || SurfaceView.DEBUG) {
                            Log.i(SurfaceView.TAG, "MSG_RELAYOUT_RETRY, set mRetryLayout to true");
                        }
                        SurfaceView.this.mRetryLayout = true;
                        SurfaceView.this.updateWindow(false, false);
                        return;
                    default:
                        return;
                }
            }
        };
        this.mScrollChangedListener = new OnScrollChangedListener() {
            public void onScrollChanged() {
                Log.i(SurfaceView.TAG, "updateWindow -- OnScrollChangedListener, this = " + this);
                SurfaceView.this.updateWindow(false, false);
            }
        };
        this.mDrawListener = new OnPreDrawListener() {
            public boolean onPreDraw() {
                boolean z;
                SurfaceView surfaceView = SurfaceView.this;
                if (SurfaceView.this.getWidth() <= 0 || SurfaceView.this.getHeight() <= 0) {
                    z = false;
                } else {
                    z = true;
                }
                surfaceView.mHaveFrame = z;
                SurfaceView.this.updateWindow(false, false);
                return true;
            }
        };
        this.mRequestedVisible = false;
        this.mWindowVisibility = false;
        this.mViewVisibility = false;
        this.mRequestedWidth = -1;
        this.mRequestedHeight = -1;
        this.mRequestedFormat = 4;
        this.mHaveFrame = false;
        this.mSurfaceCreated = false;
        this.mLastLockTime = 0;
        this.mVisible = false;
        this.mWindowSpaceLeft = -1;
        this.mWindowSpaceTop = -1;
        this.mWindowSpaceWidth = -1;
        this.mWindowSpaceHeight = -1;
        this.mFormat = -1;
        this.mSurfaceFrame = new Rect();
        this.mLastSurfaceWidth = -1;
        this.mLastSurfaceHeight = -1;
        this.mLastWidth = -1;
        this.mLastHeight = -1;
        this.mRetryLayout = false;
        this.mCustomSet = false;
        this.mRTLastReportedPosition = new Rect();
        this.mSurfaceHolder = new SurfaceHolder() {
            private static final String LOG_TAG = "SurfaceHolder";

            public boolean isCreating() {
                return SurfaceView.this.mIsCreating;
            }

            public void addCallback(Callback callback) {
                synchronized (SurfaceView.this.mCallbacks) {
                    if (!SurfaceView.this.mCallbacks.contains(callback)) {
                        SurfaceView.this.mCallbacks.add(callback);
                    }
                }
            }

            public void removeCallback(Callback callback) {
                synchronized (SurfaceView.this.mCallbacks) {
                    SurfaceView.this.mCallbacks.remove(callback);
                }
            }

            public void setFixedSize(int width, int height) {
                if (SurfaceView.this.mRequestedWidth != width || SurfaceView.this.mRequestedHeight != height) {
                    Log.i(SurfaceView.TAG, "setFixedSize w = " + width + ", h = " + height + ", this = " + SurfaceView.this);
                    SurfaceView.this.mRequestedWidth = width;
                    SurfaceView.this.mRequestedHeight = height;
                    SurfaceView.this.requestLayout();
                }
            }

            public void setSizeFromLayout() {
                if (SurfaceView.this.mRequestedWidth != -1 || SurfaceView.this.mRequestedHeight != -1) {
                    SurfaceView surfaceView = SurfaceView.this;
                    SurfaceView.this.mRequestedHeight = -1;
                    surfaceView.mRequestedWidth = -1;
                    SurfaceView.this.requestLayout();
                }
            }

            public void setFormat(int format) {
                if (format == -1) {
                    format = 4;
                }
                SurfaceView.this.mRequestedFormat = format;
                if (SurfaceView.this.mWindow != null) {
                    Log.i(SurfaceView.TAG, "updateWindow -- setFormat, this = " + SurfaceView.this);
                    SurfaceView.this.updateWindow(false, false);
                }
            }

            @Deprecated
            public void setType(int type) {
            }

            public void setKeepScreenOn(boolean screenOn) {
                int i = 1;
                Message msg = SurfaceView.this.mHandler.obtainMessage(1);
                if (!screenOn) {
                    i = 0;
                }
                msg.arg1 = i;
                SurfaceView.this.mHandler.sendMessage(msg);
            }

            public Canvas lockCanvas() {
                return internalLockCanvas(null);
            }

            public Canvas lockCanvas(Rect inOutDirty) {
                return internalLockCanvas(inOutDirty);
            }

            private final Canvas internalLockCanvas(Rect dirty) {
                SurfaceView.this.mSurfaceLock.lock();
                if (SurfaceView.IS_ENG_BUILD || SurfaceView.LOG_UPDATE_CONTENT) {
                    Log.i(SurfaceView.TAG, System.identityHashCode(this) + " " + "Locking canvas... stopped=" + SurfaceView.this.mDrawingStopped + ", win=" + SurfaceView.this.mWindow);
                }
                Canvas c = null;
                if (!(SurfaceView.this.mDrawingStopped || SurfaceView.this.mWindow == null)) {
                    try {
                        c = SurfaceView.this.mSurface.lockCanvas(dirty);
                    } catch (Exception e) {
                        Log.e(LOG_TAG, "Exception locking surface", e);
                    }
                }
                if (SurfaceView.IS_ENG_BUILD || SurfaceView.LOG_UPDATE_CONTENT) {
                    Log.i(SurfaceView.TAG, System.identityHashCode(this) + " " + "Returned canvas: " + c);
                }
                if (c != null) {
                    SurfaceView.this.mLastLockTime = SystemClock.uptimeMillis();
                    return c;
                }
                long now = SystemClock.uptimeMillis();
                long nextTime = SurfaceView.this.mLastLockTime + 100;
                if (nextTime > now) {
                    try {
                        Thread.sleep(nextTime - now);
                    } catch (InterruptedException e2) {
                    }
                    now = SystemClock.uptimeMillis();
                }
                SurfaceView.this.mLastLockTime = now;
                SurfaceView.this.mSurfaceLock.unlock();
                return null;
            }

            public void unlockCanvasAndPost(Canvas canvas) {
                if (SurfaceView.IS_ENG_BUILD || SurfaceView.LOG_UPDATE_CONTENT) {
                    Log.i(SurfaceView.TAG, System.identityHashCode(this) + " " + "UnLocking canvas... " + ", win=" + SurfaceView.this.mWindow);
                }
                SurfaceView.this.mSurface.unlockCanvasAndPost(canvas);
                SurfaceView.this.mSurfaceLock.unlock();
            }

            public Surface getSurface() {
                return SurfaceView.this.mSurface;
            }

            public Rect getSurfaceFrame() {
                return SurfaceView.this.mSurfaceFrame;
            }
        };
        setWillNotDraw(true);
        checkLogProperty();
    }

    public SurfaceHolder getHolder() {
        return this.mSurfaceHolder;
    }

    protected void onAttachedToWindow() {
        boolean z;
        super.onAttachedToWindow();
        if (this.mAttachInfo != null) {
            AttachInfo attachInfo = this.mAttachInfo;
            attachInfo.mSurfaceViewCount++;
        }
        this.mParent.requestTransparentRegion(this);
        this.mSession = getWindowSession();
        this.mLayout.token = getWindowToken();
        if (this.mCustomSet) {
            Log.i(TAG, "Cunstom has set");
        } else {
            this.mLayout.setTitle("SurfaceView - " + getViewRootImpl().getTitle());
        }
        this.mLayout.packageName = this.mContext.getOpPackageName();
        if (getVisibility() == 0) {
            z = true;
        } else {
            z = false;
        }
        this.mViewVisibility = z;
        if (!this.mGlobalListenersAdded) {
            ViewTreeObserver observer = getViewTreeObserver();
            observer.addOnScrollChangedListener(this.mScrollChangedListener);
            observer.addOnPreDrawListener(this.mDrawListener);
            this.mGlobalListenersAdded = true;
        }
        DEBUG_PANIC = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    }

    public void setTitle(CharSequence title) {
        this.mLayout.setTitle("SurfaceView - " + title);
        this.mCustomSet = true;
        Log.i(TAG, "setTitle mLayout.getTitle() " + this.mLayout.getTitle() + ", this = " + this);
    }

    protected void onWindowVisibilityChanged(int visibility) {
        boolean z;
        super.onWindowVisibilityChanged(visibility);
        if (visibility == 0) {
            z = true;
        } else {
            z = false;
        }
        this.mWindowVisibility = z;
        if (this.mWindowVisibility) {
            z = this.mViewVisibility;
        } else {
            z = false;
        }
        this.mRequestedVisible = z;
        Log.i(TAG, "updateWindow -- onWindowVisibilityChanged, visibility = " + visibility + ", this = " + this);
        updateWindow(false, false);
    }

    public void setVisibility(int visibility) {
        boolean z;
        super.setVisibility(visibility);
        if (visibility == 0) {
            z = true;
        } else {
            z = false;
        }
        this.mViewVisibility = z;
        boolean newRequestedVisible = this.mWindowVisibility ? this.mViewVisibility : false;
        if (newRequestedVisible != this.mRequestedVisible) {
            requestLayout();
        }
        this.mRequestedVisible = newRequestedVisible;
        Log.i(TAG, "updateWindow -- setVisibility, visibility = " + visibility + ", this = " + this);
        updateWindow(false, false);
    }

    protected void onDetachedFromWindow() {
        if (this.mGlobalListenersAdded) {
            ViewTreeObserver observer = getViewTreeObserver();
            observer.removeOnScrollChangedListener(this.mScrollChangedListener);
            observer.removeOnPreDrawListener(this.mDrawListener);
            this.mGlobalListenersAdded = false;
        }
        this.mRequestedVisible = false;
        Log.i(TAG, "updateWindow -- onDetachedFromWindow, this = " + this);
        updateWindow(false, false);
        this.mHaveFrame = false;
        if (this.mWindow != null) {
            try {
                this.mSession.remove(this.mWindow);
            } catch (RemoteException e) {
            }
            this.mWindow = null;
        }
        this.mSession = null;
        this.mLayout.token = null;
        if (this.mAttachInfo != null) {
            AttachInfo attachInfo = this.mAttachInfo;
            attachInfo.mSurfaceViewCount--;
        }
        super.onDetachedFromWindow();
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width;
        int height;
        if (this.mRequestedWidth >= 0) {
            width = View.resolveSizeAndState(this.mRequestedWidth, widthMeasureSpec, 0);
        } else {
            width = View.getDefaultSize(0, widthMeasureSpec);
        }
        if (this.mRequestedHeight >= 0) {
            height = View.resolveSizeAndState(this.mRequestedHeight, heightMeasureSpec, 0);
        } else {
            height = View.getDefaultSize(0, heightMeasureSpec);
        }
        setMeasuredDimension(width, height);
    }

    protected boolean setFrame(int left, int top, int right, int bottom) {
        boolean result = super.setFrame(left, top, right, bottom);
        Log.i(TAG, "updateWindow -- setFrame, this = " + this);
        updateWindow(false, false);
        return result;
    }

    public boolean gatherTransparentRegion(Region region) {
        if (this.mWindowType == 1000) {
            return super.gatherTransparentRegion(region);
        }
        boolean opaque = true;
        if ((this.mPrivateFlags & 128) == 0) {
            opaque = super.gatherTransparentRegion(region);
        } else if (region != null) {
            int w = getWidth();
            int h = getHeight();
            if (w > 0 && h > 0) {
                getLocationInWindow(this.mLocation);
                int l = this.mLocation[0];
                int t = this.mLocation[1];
                region.op(l, t, l + w, t + h, Op.UNION);
            }
        }
        if (PixelFormat.formatHasAlpha(this.mRequestedFormat)) {
            opaque = false;
        }
        return opaque;
    }

    public void draw(Canvas canvas) {
        if (this.mWindowType != 1000 && (this.mPrivateFlags & 128) == 0) {
            canvas.drawColor(0, Mode.CLEAR);
            Log.i(TAG, "Punch a hole(draw), this = " + this);
        }
        super.draw(canvas);
    }

    protected void dispatchDraw(Canvas canvas) {
        if (this.mWindowType != 1000 && (this.mPrivateFlags & 128) == 128) {
            canvas.drawColor(0, Mode.CLEAR);
            Log.i(TAG, "Punch a hole(dispatchDraw), this = " + this);
        }
        super.dispatchDraw(canvas);
    }

    public void setZOrderMediaOverlay(boolean isMediaOverlay) {
        int i;
        if (isMediaOverlay) {
            i = 1004;
        } else {
            i = 1001;
        }
        this.mWindowType = i;
    }

    public void setZOrderOnTop(boolean onTop) {
        LayoutParams layoutParams;
        if (onTop) {
            this.mWindowType = 1000;
            layoutParams = this.mLayout;
            layoutParams.flags |= 131072;
            return;
        }
        this.mWindowType = 1001;
        layoutParams = this.mLayout;
        layoutParams.flags &= -131073;
    }

    public void setSecure(boolean isSecure) {
        LayoutParams layoutParams;
        if (isSecure) {
            layoutParams = this.mLayout;
            layoutParams.flags |= 8192;
            return;
        }
        layoutParams = this.mLayout;
        layoutParams.flags &= -8193;
    }

    public void setWindowType(int type) {
        this.mWindowType = type;
    }

    protected void updateWindow(boolean force, boolean redrawNeeded) {
        if (this.mHaveFrame) {
            boolean sizeChanged;
            if (LOG_UPDATE_WINDOW) {
                Trace.traceBegin(8, "SurfaceView updateWindow start");
            }
            ViewRootImpl viewRoot = getViewRootImpl();
            if (viewRoot != null) {
                this.mTranslator = viewRoot.mTranslator;
            }
            if (this.mTranslator != null) {
                this.mSurface.setCompatibilityTranslator(this.mTranslator);
            }
            int myWidth = this.mRequestedWidth;
            if (myWidth <= 0) {
                myWidth = getWidth();
            }
            int myHeight = this.mRequestedHeight;
            if (myHeight <= 0) {
                myHeight = getHeight();
            }
            boolean forceSizeChanged = (this.mLastWidth == getWidth() && this.mLastHeight == getHeight()) ? false : true;
            boolean creating = this.mWindow == null;
            boolean formatChanged = this.mFormat != this.mRequestedFormat;
            if (this.mWindowSpaceWidth == myWidth && this.mWindowSpaceHeight == myHeight) {
                sizeChanged = forceSizeChanged;
            } else {
                sizeChanged = true;
            }
            boolean visibleChanged = this.mVisible != this.mRequestedVisible;
            boolean layoutSizeChanged = getWidth() == this.mLayout.width ? getHeight() != this.mLayout.height : true;
            int width;
            if (force || creating || formatChanged || sizeChanged || visibleChanged || this.mUpdateWindowNeeded || this.mReportDrawNeeded || redrawNeeded || this.mRetryLayout) {
                this.mHandler.removeMessages(101);
                this.mRetryLayout = false;
                getLocationInWindow(this.mLocation);
                if (LOG_UPDATE_WINDOW) {
                    Trace.traceBegin(8, "SurfaceView property changed");
                }
                if (DEBUG) {
                    boolean z;
                    String str = TAG;
                    StringBuilder append = new StringBuilder().append(System.identityHashCode(this)).append(" ").append("Changes: creating=").append(creating).append(" format=").append(formatChanged).append(" size=").append(sizeChanged).append(" visible=").append(visibleChanged).append(" left=").append(this.mWindowSpaceLeft != this.mLocation[0]).append(" top=");
                    if (this.mWindowSpaceTop != this.mLocation[1]) {
                        z = true;
                    } else {
                        z = false;
                    }
                    Log.i(str, append.append(z).append(" mUpdateWindowNeeded=").append(this.mUpdateWindowNeeded).append(" mReportDrawNeeded=").append(this.mReportDrawNeeded).append(" redrawNeeded=").append(redrawNeeded).append(" forceSizeChanged=").append(forceSizeChanged).append(" mVisible=").append(this.mVisible).append(" mRequestedVisible=").append(this.mRequestedVisible).append(", this = ").append(this).toString());
                }
                try {
                    boolean visible = this.mRequestedVisible;
                    this.mVisible = visible;
                    this.mWindowSpaceLeft = this.mLocation[0];
                    this.mWindowSpaceTop = this.mLocation[1];
                    this.mWindowSpaceWidth = myWidth;
                    this.mWindowSpaceHeight = myHeight;
                    this.mLastWidth = getWidth();
                    this.mLastHeight = getHeight();
                    this.mFormat = this.mRequestedFormat;
                    this.mLayout.x = this.mWindowSpaceLeft;
                    this.mLayout.y = this.mWindowSpaceTop;
                    this.mLayout.width = getWidth();
                    this.mLayout.height = getHeight();
                    if (this.mTranslator != null) {
                        this.mTranslator.translateLayoutParamsInAppWindowToScreen(this.mLayout);
                    }
                    this.mLayout.format = this.mRequestedFormat;
                    LayoutParams layoutParams = this.mLayout;
                    layoutParams.flags |= 16920;
                    if (creating || force || sizeChanged) {
                        layoutParams = this.mLayout;
                        layoutParams.privateFlags &= -8193;
                    } else {
                        layoutParams = this.mLayout;
                        layoutParams.privateFlags |= 8192;
                    }
                    if (!getContext().getResources().getCompatibilityInfo().supportsScreen()) {
                        layoutParams = this.mLayout;
                        layoutParams.privateFlags |= 128;
                    }
                    layoutParams = this.mLayout;
                    layoutParams.privateFlags |= 65600;
                    if (this.mWindow == null) {
                        Display display = getDisplay();
                        this.mWindow = new MyWindow(this);
                        this.mLayout.type = this.mWindowType;
                        this.mLayout.gravity = 8388659;
                        if (LOG_UPDATE_WINDOW) {
                            Trace.traceBegin(8, "SurfaceView addToDisplayWithoutInputChannel");
                        }
                        this.mSession.addToDisplayWithoutInputChannel(this.mWindow, this.mWindow.mSeq, this.mLayout, this.mVisible ? 0 : 8, display.getDisplayId(), this.mContentInsets, this.mStableInsets);
                        if (LOG_UPDATE_WINDOW) {
                            Trace.traceEnd(8);
                        }
                    }
                    this.mSurfaceLock.lock();
                    this.mUpdateWindowNeeded = false;
                    int reportDrawNeeded = this.mReportDrawNeeded;
                    this.mReportDrawNeeded = false;
                    this.mDrawingStopped = !visible;
                    if (DEBUG) {
                        Log.i(TAG, System.identityHashCode(this) + " " + "Cur surface: " + this.mSurface + ", this = " + this);
                    }
                    if (LOG_UPDATE_WINDOW) {
                        Trace.traceBegin(8, "SurfaceView relayout");
                    }
                    int relayoutResult = this.mSession.relayout(this.mWindow, this.mWindow.mSeq, this.mLayout, this.mWindowSpaceWidth, this.mWindowSpaceHeight, visible ? 0 : 8, 2, this.mWinFrame, this.mOverscanInsets, this.mContentInsets, this.mVisibleInsets, this.mStableInsets, this.mOutsets, this.mBackdropFrame, this.mConfiguration, this.mNewSurface);
                    if ((8388608 & relayoutResult) != 0) {
                        if (DEBUG_PANIC || DEBUG) {
                            Log.i(TAG, "relayoutResult RELAYOUT_NEED_RETRY, send MSG_RELAYOUT_RETRY message");
                        }
                        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(101), 200);
                    }
                    if (LOG_UPDATE_WINDOW) {
                        Trace.traceEnd(8);
                    }
                    if ((relayoutResult & 2) != 0) {
                        reportDrawNeeded = 1;
                    }
                    if (DEBUG) {
                        Log.i(TAG, System.identityHashCode(this) + " " + "New surface: " + this.mNewSurface + ", vis=" + visible + ", frame=" + this.mWinFrame + ", this = " + this);
                    }
                    this.mSurfaceFrame.left = 0;
                    this.mSurfaceFrame.top = 0;
                    if (this.mTranslator == null) {
                        this.mSurfaceFrame.right = this.mWinFrame.width();
                        this.mSurfaceFrame.bottom = this.mWinFrame.height();
                    } else {
                        float appInvertedScale = this.mTranslator.applicationInvertedScale;
                        this.mSurfaceFrame.right = (int) ((((float) this.mWinFrame.width()) * appInvertedScale) + 0.5f);
                        this.mSurfaceFrame.bottom = (int) ((((float) this.mWinFrame.height()) * appInvertedScale) + 0.5f);
                    }
                    int surfaceWidth = this.mSurfaceFrame.right;
                    int surfaceHeight = this.mSurfaceFrame.bottom;
                    boolean realSizeChanged = this.mLastSurfaceWidth == surfaceWidth ? this.mLastSurfaceHeight != surfaceHeight : true;
                    this.mLastSurfaceWidth = surfaceWidth;
                    this.mLastSurfaceHeight = surfaceHeight;
                    this.mSurfaceLock.unlock();
                    if (LOG_UPDATE_WINDOW) {
                        Trace.traceBegin(8, "SurfaceView callback block");
                    }
                    redrawNeeded |= creating | reportDrawNeeded;
                    Callback[] callbacks = null;
                    boolean surfaceChanged = (relayoutResult & 4) != 0;
                    if (this.mSurfaceCreated && (surfaceChanged || (!visible && visibleChanged))) {
                        this.mSurfaceCreated = false;
                        if (this.mSurface.isValid()) {
                            if (DEBUG) {
                                Log.i(TAG, System.identityHashCode(this) + " " + "visibleChanged -- surfaceDestroyed, this = " + this);
                            }
                            callbacks = getSurfaceCallbacks();
                            for (Callback c : callbacks) {
                                if (DEBUG) {
                                    Log.i(TAG, "surfaceDestroyed callback +, this = " + this);
                                }
                                c.surfaceDestroyed(this.mSurfaceHolder);
                                if (DEBUG) {
                                    Log.i(TAG, "surfaceDestroyed callback -, this = " + this);
                                }
                            }
                            if (this.mSurface.isValid()) {
                                this.mSurface.forceScopedDisconnect();
                            }
                        }
                    }
                    this.mSurface.transferFrom(this.mNewSurface);
                    if (visible && this.mSurface.isValid()) {
                        if (!this.mSurfaceCreated && (surfaceChanged || visibleChanged)) {
                            this.mSurfaceCreated = true;
                            this.mIsCreating = true;
                            if (DEBUG) {
                                Log.i(TAG, System.identityHashCode(this) + " " + "visibleChanged -- surfaceCreated, this = " + this);
                            }
                            if (callbacks == null) {
                                callbacks = getSurfaceCallbacks();
                            }
                            for (Callback c2 : callbacks) {
                                if (DEBUG) {
                                    Log.i(TAG, "surfaceCreated callback +, this = " + this);
                                }
                                c2.surfaceCreated(this.mSurfaceHolder);
                                if (DEBUG) {
                                    Log.i(TAG, "surfaceCreated callback -, this = " + this);
                                }
                            }
                        }
                        if (creating || formatChanged || sizeChanged || visibleChanged || realSizeChanged) {
                            if (DEBUG) {
                                Log.i(TAG, System.identityHashCode(this) + " " + "surfaceChanged -- format=" + this.mFormat + " w=" + myWidth + " h=" + myHeight + ", this = " + this);
                            }
                            if (callbacks == null) {
                                callbacks = getSurfaceCallbacks();
                            }
                            for (Callback c22 : callbacks) {
                                if (DEBUG) {
                                    Log.i(TAG, "surfaceChanged callback +, this = " + this);
                                }
                                c22.surfaceChanged(this.mSurfaceHolder, this.mFormat, myWidth, myHeight);
                                if (DEBUG) {
                                    Log.i(TAG, "surfaceChanged callback -, this = " + this);
                                }
                            }
                        }
                        if (redrawNeeded) {
                            if (DEBUG) {
                                Log.i(TAG, System.identityHashCode(this) + " " + "surfaceRedrawNeeded, this = " + this);
                            }
                            if (callbacks == null) {
                                callbacks = getSurfaceCallbacks();
                            }
                            for (Callback c222 : callbacks) {
                                if (c222 instanceof Callback2) {
                                    ((Callback2) c222).surfaceRedrawNeeded(this.mSurfaceHolder);
                                }
                            }
                        }
                    }
                    if (LOG_UPDATE_WINDOW) {
                        Trace.traceEnd(8);
                    }
                    this.mIsCreating = false;
                    if (redrawNeeded) {
                        if (DEBUG) {
                            Log.i(TAG, System.identityHashCode(this) + " " + "finishedDrawing, this = " + this);
                        }
                        if (LOG_UPDATE_WINDOW) {
                            Trace.traceBegin(8, "SurfaceView finishDrawing");
                        }
                        this.mSession.finishDrawing(this.mWindow);
                        if (LOG_UPDATE_WINDOW) {
                            Trace.traceEnd(8);
                        }
                    }
                    if (LOG_UPDATE_WINDOW) {
                        Trace.traceBegin(8, "SurfaceView performDeferredDestroy");
                    }
                    this.mSession.performDeferredDestroy(this.mWindow);
                    if (LOG_UPDATE_WINDOW) {
                        Trace.traceEnd(8);
                    }
                } catch (Throwable ex) {
                    Log.e(TAG, "Exception from relayout", ex);
                } catch (Throwable th) {
                    this.mSurfaceLock.unlock();
                }
                if (DEBUG) {
                    Log.v(TAG, "Layout: x=" + this.mLayout.x + " y=" + this.mLayout.y + " w=" + this.mLayout.width + " h=" + this.mLayout.height + ", frame=" + this.mSurfaceFrame + ", this = " + this);
                }
                if (LOG_UPDATE_WINDOW) {
                    Trace.traceEnd(8);
                }
            } else {
                getLocationInWindow(this.mLocation);
                boolean positionChanged = this.mWindowSpaceLeft == this.mLocation[0] ? this.mWindowSpaceTop != this.mLocation[1] : true;
                if (positionChanged || layoutSizeChanged) {
                    this.mWindowSpaceLeft = this.mLocation[0];
                    this.mWindowSpaceTop = this.mLocation[1];
                    int[] iArr = this.mLocation;
                    width = getWidth();
                    this.mLayout.width = width;
                    iArr[0] = width;
                    iArr = this.mLocation;
                    width = getHeight();
                    this.mLayout.height = width;
                    iArr[1] = width;
                    transformFromViewToWindowSpace(this.mLocation);
                    this.mTmpRect.set(this.mWindowSpaceLeft, this.mWindowSpaceTop, this.mLocation[0], this.mLocation[1]);
                    if (this.mTranslator != null) {
                        this.mTranslator.translateRectInAppWindowToScreen(this.mTmpRect);
                    }
                    if (!(isHardwareAccelerated() && this.mRtHandlingPositionUpdates)) {
                        try {
                            if (DEBUG) {
                                String str2 = TAG;
                                Object[] objArr = new Object[5];
                                objArr[0] = Integer.valueOf(System.identityHashCode(this));
                                objArr[1] = Integer.valueOf(this.mTmpRect.left);
                                objArr[2] = Integer.valueOf(this.mTmpRect.top);
                                objArr[3] = Integer.valueOf(this.mTmpRect.right);
                                objArr[4] = Integer.valueOf(this.mTmpRect.bottom);
                                Log.d(str2, String.format("%d updateWindowPosition UI, postion = [%d, %d, %d, %d]", objArr));
                            }
                            this.mSession.repositionChild(this.mWindow, this.mTmpRect.left, this.mTmpRect.top, this.mTmpRect.right, this.mTmpRect.bottom, -1, this.mTmpRect);
                        } catch (Throwable ex2) {
                            Log.e(TAG, "Exception from relayout", ex2);
                        }
                    }
                }
            }
            if (LOG_UPDATE_WINDOW) {
                Trace.traceEnd(8);
            }
        }
    }

    public final void updateWindowPosition_renderWorker(long frameNumber, int left, int top, int right, int bottom) {
        IWindowSession session = this.mSession;
        MyWindow window = this.mWindow;
        if (session != null && window != null) {
            this.mRtHandlingPositionUpdates = true;
            if (this.mRTLastReportedPosition.left != left || this.mRTLastReportedPosition.top != top || this.mRTLastReportedPosition.right != right || this.mRTLastReportedPosition.bottom != bottom) {
                try {
                    if (DEBUG) {
                        String str = TAG;
                        Object[] objArr = new Object[6];
                        objArr[0] = Integer.valueOf(System.identityHashCode(this));
                        objArr[1] = Long.valueOf(frameNumber);
                        objArr[2] = Integer.valueOf(left);
                        objArr[3] = Integer.valueOf(top);
                        objArr[4] = Integer.valueOf(right);
                        objArr[5] = Integer.valueOf(bottom);
                        Log.d(str, String.format("%d updateWindowPosition RenderWorker, frameNr = %d, postion = [%d, %d, %d, %d]", objArr));
                    }
                    session.repositionChild(window, left, top, right, bottom, frameNumber, this.mRTLastReportedPosition);
                    this.mRTLastReportedPosition.set(left, top, right, bottom);
                } catch (RemoteException ex) {
                    Log.e(TAG, "Exception from repositionChild", ex);
                } catch (NullPointerException ex2) {
                    Log.e(TAG, "NullPointerException from relayout", ex2);
                }
            }
        }
    }

    /* JADX WARNING: Missing block: B:6:0x002e, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public final void windowPositionLost_uiRtSync(long frameNumber) {
        String str;
        Object[] objArr;
        if (DEBUG) {
            str = TAG;
            objArr = new Object[2];
            objArr[0] = Integer.valueOf(System.identityHashCode(this));
            objArr[1] = Long.valueOf(frameNumber);
            Log.d(str, String.format("%d windowPositionLost, frameNr = %d", objArr));
        }
        IWindowSession session = this.mSession;
        MyWindow window = this.mWindow;
        if (!(session == null || window == null || !this.mRtHandlingPositionUpdates)) {
            this.mRtHandlingPositionUpdates = false;
            this.mTmpRect.set(this.mLayout.x, this.mLayout.y, this.mLayout.x + this.mLayout.width, this.mLayout.y + this.mLayout.height);
            if (!(this.mTmpRect.isEmpty() || this.mTmpRect.equals(this.mRTLastReportedPosition))) {
                try {
                    if (DEBUG) {
                        str = TAG;
                        objArr = new Object[5];
                        objArr[0] = Integer.valueOf(System.identityHashCode(this));
                        objArr[1] = Integer.valueOf(this.mTmpRect.left);
                        objArr[2] = Integer.valueOf(this.mTmpRect.top);
                        objArr[3] = Integer.valueOf(this.mTmpRect.right);
                        objArr[4] = Integer.valueOf(this.mTmpRect.bottom);
                        Log.d(str, String.format("%d updateWindowPosition, postion = [%d, %d, %d, %d]", objArr));
                    }
                    session.repositionChild(window, this.mTmpRect.left, this.mTmpRect.top, this.mTmpRect.right, this.mTmpRect.bottom, frameNumber, this.mWinFrame);
                } catch (RemoteException ex) {
                    Log.e(TAG, "Exception from relayout", ex);
                } catch (NullPointerException ex2) {
                    Log.e(TAG, "NullPointerException from relayout", ex2);
                }
            }
            this.mRTLastReportedPosition.setEmpty();
        }
    }

    private Callback[] getSurfaceCallbacks() {
        Callback[] callbacks;
        synchronized (this.mCallbacks) {
            callbacks = new Callback[this.mCallbacks.size()];
            this.mCallbacks.toArray(callbacks);
        }
        return callbacks;
    }

    void handleGetNewSurface() {
        Log.i(TAG, "updateWindow -- handleGetNewSurface, this = " + this);
        updateWindow(false, false);
    }

    public boolean isFixedSize() {
        return (this.mRequestedWidth == -1 && this.mRequestedHeight == -1) ? false : true;
    }

    protected void enableLog(boolean enable) {
        Log.d(TAG, "enableLog enable, this = " + this);
        DEBUG = enable;
    }

    private static void checkLogProperty() {
        boolean z = true;
        String dumpString = SystemProperties.get(LOG_PROPERTY_NAME);
        if (dumpString != null) {
            if (dumpString.length() <= 0 || dumpString.length() > 1) {
                Log.d(TAG, "checkSurfaceViewlLogProperty get invalid command");
                return;
            }
            boolean z2;
            int logFilter = 0;
            try {
                logFilter = Integer.parseInt(dumpString, 16);
            } catch (NumberFormatException e) {
                Log.w(TAG, "Invalid format of propery string: " + dumpString);
            }
            if ((logFilter & 1) == 1) {
                z2 = true;
            } else {
                z2 = false;
            }
            LOG_UPDATE_WINDOW = z2;
            if ((logFilter & 2) != 2) {
                z = false;
            }
            LOG_UPDATE_CONTENT = z;
            Log.d(TAG, "checkSurfaceViewlLogProperty debug filter: ,UPDATE_WINDOW= " + LOG_UPDATE_WINDOW + ",UPDATE_CONTENT= " + LOG_UPDATE_CONTENT);
        }
    }

    public int getWindowFlag() {
        return this.mLayout.flags;
    }

    public void setWindowFlag(int requestFlags) {
        this.mLayout.flags = requestFlags;
    }

    public float getWindowAlpha() {
        return this.mLayout.alpha;
    }

    public void setWindowAlpha(float alpha) {
        this.mLayout.alpha = alpha;
        this.mHandler.sendMessage(this.mHandler.obtainMessage(4));
    }
}
