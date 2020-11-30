package android.view;

import android.Manifest;
import android.animation.LayoutTransition;
import android.annotation.OppoHook;
import android.annotation.UnsupportedAppUsage;
import android.app.ActivityManager;
import android.app.ActivityThread;
import android.app.ResourcesManager;
import android.common.ColorFrameworkFactory;
import android.common.OppoFeatureCache;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Context;
import android.content.res.CompatibilityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.HardwareRenderer;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.RecordingCanvas;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.RenderNode;
import android.graphics.drawable.Drawable;
import android.hardware.display.DisplayManager;
import android.hardware.input.InputManager;
import android.media.AudioManager;
import android.media.TtmlUtils;
import android.net.TrafficStats;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.Binder;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.os.Process;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.Trace;
import android.sysprop.DisplayProperties;
import android.telecom.Logging.Session;
import android.telephony.SmsManager;
import android.util.AndroidRuntimeException;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.LongArray;
import android.util.MergedConfiguration;
import android.util.Slog;
import android.util.SparseArray;
import android.util.TimeUtils;
import android.util.TypedValue;
import android.view.ActionMode;
import android.view.Choreographer;
import android.view.DisplayCutout;
import android.view.IWindow;
import android.view.InputDevice;
import android.view.InputQueue;
import android.view.KeyCharacterMap;
import android.view.Surface;
import android.view.SurfaceControl;
import android.view.SurfaceHolder;
import android.view.ThreadedRenderer;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityNodeIdManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityNodeProvider;
import android.view.accessibility.IAccessibilityInteractionConnection;
import android.view.accessibility.IAccessibilityInteractionConnectionCallback;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.autofill.AutofillManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Scroller;
import com.android.internal.R;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.logging.nano.MetricsProto;
import com.android.internal.os.IResultReceiver;
import com.android.internal.os.SomeArgs;
import com.android.internal.policy.PhoneFallbackEventHandler;
import com.android.internal.util.Preconditions;
import com.android.internal.view.BaseSurfaceHolder;
import com.android.internal.view.RootViewSurfaceTaker;
import com.android.internal.view.SurfaceCallbackHelper;
import com.color.darkmode.IColorDarkModeManager;
import com.color.screenshot.ColorLongshotViewRoot;
import com.color.zoomwindow.ColorZoomWindowManager;
import com.mediatek.view.SurfaceExt;
import com.mediatek.view.SurfaceFactory;
import com.mediatek.view.ViewDebugManager;
import com.oppo.debug.InputLog;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CountDownLatch;

public final class ViewRootImpl implements ViewParent, View.AttachInfo.Callbacks, ThreadedRenderer.DrawCallbacks, IColorBaseViewRoot {
    public static boolean DBG = false;
    public static boolean DEBUG_CONFIGURATION = false;
    private static final boolean DEBUG_CONTENT_CAPTURE;
    public static boolean DEBUG_DIALOG = false;
    private static final boolean DEBUG_DISABLEHW = SystemProperties.getBoolean("debug.viewroot.disableHW", false);
    public static boolean DEBUG_DRAW = false;
    public static boolean DEBUG_FPS = false;
    public static boolean DEBUG_IMF = false;
    public static boolean DEBUG_INPUT_RESIZE = false;
    public static boolean DEBUG_INPUT_STAGES = false;
    public static boolean DEBUG_KEEP_SCREEN_ON = false;
    public static boolean DEBUG_LAYOUT = false;
    public static boolean DEBUG_ORIENTATION = false;
    private static boolean DEBUG_PANIC = false;
    public static boolean DEBUG_TRACKBALL = false;
    public static boolean LOCAL_LOGV = false;
    private static final int MAX_QUEUED_INPUT_EVENT_POOL_SIZE = 10;
    static final int MAX_TRACKBALL_DELAY = 250;
    private static final int MSG_CHECK_FOCUS = 13;
    private static final int MSG_CLEAR_ACCESSIBILITY_FOCUS_HOST = 21;
    private static final int MSG_CLOSE_SYSTEM_DIALOGS = 14;
    private static final int MSG_DIE = 3;
    private static final int MSG_DISPATCH_APP_VISIBILITY = 8;
    private static final int MSG_DISPATCH_DRAG_EVENT = 15;
    private static final int MSG_DISPATCH_DRAG_LOCATION_EVENT = 16;
    private static final int MSG_DISPATCH_GET_NEW_SURFACE = 9;
    private static final int MSG_DISPATCH_INPUT_EVENT = 7;
    private static final int MSG_DISPATCH_KEY_FROM_AUTOFILL = 12;
    private static final int MSG_DISPATCH_KEY_FROM_IME = 11;
    private static final int MSG_DISPATCH_SYSTEM_UI_VISIBILITY = 17;
    private static final int MSG_DISPATCH_WINDOW_SHOWN = 25;
    private static final int MSG_DRAW_FINISHED = 29;
    private static final int MSG_INSETS_CHANGED = 30;
    private static final int MSG_INSETS_CONTROL_CHANGED = 31;
    private static final int MSG_INVALIDATE = 1;
    private static final int MSG_INVALIDATE_RECT = 2;
    private static final int MSG_INVALIDATE_WORLD = 22;
    private static final int MSG_POINTER_CAPTURE_CHANGED = 28;
    private static final int MSG_PROCESS_INPUT_EVENTS = 19;
    private static final int MSG_RELAYOUT_RETRY_DELAY = 200;
    private static final int MSG_REQUEST_KEYBOARD_SHORTCUTS = 26;
    private static final int MSG_RESIZED = 4;
    private static final int MSG_RESIZED_REPORT = 5;
    private static final int MSG_SYNTHESIZE_INPUT_EVENT = 24;
    private static final int MSG_SYSTEM_GESTURE_EXCLUSION_CHANGED = 32;
    private static final int MSG_UPDATE_CONFIGURATION = 18;
    private static final int MSG_UPDATE_POINTER_ICON = 27;
    private static final int MSG_WINDOW_FOCUS_CHANGED = 6;
    private static final int MSG_WINDOW_MOVED = 23;
    private static final boolean MT_RENDERER_AVAILABLE = true;
    public static final int NEW_INSETS_MODE_FULL = 2;
    public static final int NEW_INSETS_MODE_IME = 1;
    public static final int NEW_INSETS_MODE_NONE = 0;
    public static final String PROPERTY_EMULATOR_WIN_OUTSET_BOTTOM_PX = "ro.emu.win_outset_bottom_px";
    private static final String PROPERTY_PROFILE_RENDERING = "viewroot.profile_rendering";
    private static final String TAG = "ViewRootImpl";
    private static final String USE_NEW_INSETS_PROPERTY = "persist.wm.new_insets";
    static final Interpolator mResizeInterpolator = new AccelerateDecelerateInterpolator();
    private static boolean sAlwaysAssignFocus;
    private static boolean sCompatibilityDone = false;
    private static final ArrayList<ConfigChangedCallback> sConfigCallbacks = new ArrayList<>();
    static boolean sFirstDrawComplete = false;
    static final ArrayList<Runnable> sFirstDrawHandlers = new ArrayList<>();
    public static int sNewInsetsMode = SystemProperties.getInt(USE_NEW_INSETS_PROPERTY, 0);
    @UnsupportedAppUsage
    static final ThreadLocal<HandlerActionQueue> sRunQueues = new ThreadLocal<>();
    private boolean FRAME_ONT;
    private int doFrameIndex;
    boolean isBuiltinApp;
    private boolean isOptApp;
    View mAccessibilityFocusedHost;
    AccessibilityNodeInfo mAccessibilityFocusedVirtualView;
    final AccessibilityInteractionConnectionManager mAccessibilityInteractionConnectionManager;
    AccessibilityInteractionController mAccessibilityInteractionController;
    final AccessibilityManager mAccessibilityManager;
    private ActivityConfigCallback mActivityConfigCallback;
    private boolean mActivityRelaunched;
    @UnsupportedAppUsage
    boolean mAdded;
    boolean mAddedTouchMode;
    private boolean mAppVisibilityChanged;
    boolean mAppVisible;
    boolean mApplyInsetsRequested;
    @UnsupportedAppUsage
    final View.AttachInfo mAttachInfo;
    AudioManager mAudioManager;
    final String mBasePackageName;
    public final Surface mBoundsSurface;
    private SurfaceControl mBoundsSurfaceControl;
    private int mCanvasOffsetX;
    private int mCanvasOffsetY;
    Choreographer mChoreographer;
    int mClientWindowLayoutFlags;
    IColorViewRootUtil mColorViewRootUtil;
    final ConsumeBatchedInputImmediatelyRunnable mConsumeBatchedInputImmediatelyRunnable;
    boolean mConsumeBatchedInputImmediatelyScheduled;
    boolean mConsumeBatchedInputScheduled;
    final ConsumeBatchedInputRunnable mConsumedBatchedInputRunnable;
    @UnsupportedAppUsage
    public final Context mContext;
    int mCurScrollY;
    View mCurrentDragView;
    private PointerIcon mCustomPointerIcon;
    private final int mDensity;
    private final Object mDestroyLock = new Object();
    @UnsupportedAppUsage
    Rect mDirty;
    final Rect mDispatchContentInsets;
    DisplayCutout mDispatchDisplayCutout;
    final Rect mDispatchStableInsets;
    Display mDisplay;
    private final DisplayManager.DisplayListener mDisplayListener;
    final DisplayManager mDisplayManager;
    ClipDescription mDragDescription;
    final PointF mDragPoint;
    private boolean mDragResizing;
    boolean mDrawingAllowed;
    int mDrawsNeededToReport;
    @UnsupportedAppUsage
    FallbackEventHandler mFallbackEventHandler;
    boolean mFirst;
    private boolean mFirstFrameScheduled;
    InputStage mFirstInputStage;
    InputStage mFirstPostImeInputStage;
    private boolean mForceDecorViewVisibility;
    private boolean mForceNextConfigUpdate;
    boolean mForceNextWindowRelayout;
    private int mFpsNumFrames;
    private long mFpsPrevTime;
    private long mFpsStartTime;
    public int mFrame;
    boolean mFullRedrawNeeded;
    private final GestureExclusionTracker mGestureExclusionTracker;
    private final Object mGraphicLock = new Object();
    boolean mHadWindowFocus;
    final ViewRootHandler mHandler;
    boolean mHandlingLayoutInLayoutRequest;
    int mHardwareXOffset;
    int mHardwareYOffset;
    boolean mHasHadWindowFocus;
    @UnsupportedAppUsage
    int mHeight;
    final HighContrastTextManager mHighContrastTextManager;
    public long mIdent;
    private boolean mInLayout;
    InputChannel mInputChannel;
    private final InputEventCompatProcessor mInputCompatProcessor;
    protected final InputEventConsistencyVerifier mInputEventConsistencyVerifier;
    WindowInputEventReceiver mInputEventReceiver;
    InputQueue mInputQueue;
    InputQueue.Callback mInputQueueCallback;
    private final InsetsController mInsetsController;
    final InvalidateOnAnimationRunnable mInvalidateOnAnimationRunnable;
    private boolean mInvalidateRootRequested;
    boolean mIsAmbientMode;
    public boolean mIsAnimating;
    boolean mIsCreating;
    boolean mIsDrawing;
    boolean mIsInTraversal;
    boolean mIsLuckyMoneyView;
    private boolean mIsSurfaceViewCreated;
    public boolean mIsWeixinLauncherUI;
    private final Configuration mLastConfigurationFromResources;
    final ViewTreeObserver.InternalInsetsInfo mLastGivenInsets;
    boolean mLastInCompatMode;
    boolean mLastOverscanRequested;
    private final MergedConfiguration mLastReportedMergedConfiguration;
    @UnsupportedAppUsage
    WeakReference<View> mLastScrolledFocus;
    int mLastSystemUiVisibility;
    final PointF mLastTouchPoint;
    int mLastTouchSource;
    boolean mLastWasImTarget;
    private WindowInsets mLastWindowInsets;
    boolean mLayoutRequested;
    ArrayList<View> mLayoutRequesters;
    volatile Object mLocalDragState;
    final WindowLeaked mLocation;
    boolean mLostWindowFocus;
    public IColorDarkModeManager mManager;
    private boolean mNeedsRendererSetup;
    boolean mNewSurfaceNeeded;
    private final int mNoncompatDensity;
    int mOrigWindowType;
    int mOverrideRefreshRateId;
    boolean mPausedForTransition;
    boolean mPendingAlwaysConsumeSystemBars;
    final Rect mPendingBackDropFrame;
    final Rect mPendingContentInsets;
    final DisplayCutout.ParcelableWrapper mPendingDisplayCutout;
    int mPendingInputEventCount;
    QueuedInputEvent mPendingInputEventHead;
    String mPendingInputEventQueueLengthCounterName;
    QueuedInputEvent mPendingInputEventTail;
    private final MergedConfiguration mPendingMergedConfiguration;
    final Rect mPendingOutsets;
    final Rect mPendingOverscanInsets;
    final Rect mPendingStableInsets;
    private ArrayList<LayoutTransition> mPendingTransitions;
    final Rect mPendingVisibleInsets;
    boolean mPointerCapture;
    private int mPointerIconType;
    final Region mPreviousTransparentRegion;
    boolean mProcessInputEventsScheduled;
    private boolean mProfile;
    private boolean mProfileRendering;
    private QueuedInputEvent mQueuedInputEventPool;
    private int mQueuedInputEventPoolSize;
    private boolean mRemoved;
    private Choreographer.FrameCallback mRenderProfiler;
    private boolean mRenderProfilingEnabled;
    boolean mReportNextDraw;
    private int mResizeMode;
    private boolean mRetryLayout;
    public ArrayList<String> mScreenModeView = new ArrayList<>();
    private ArrayList<String> mScreenModeViewList;
    boolean mScrollMayChange;
    int mScrollY;
    Scroller mScroller;
    SendWindowContentChangedAccessibilityEvent mSendWindowContentChangedAccessibilityEvent;
    int mSeq;
    int mSoftInputMode;
    @UnsupportedAppUsage
    boolean mStopped;
    @UnsupportedAppUsage
    public final Surface mSurface;
    private final SurfaceControl mSurfaceControl;
    private SurfaceExt mSurfaceExt = null;
    BaseSurfaceHolder mSurfaceHolder;
    SurfaceHolder.Callback2 mSurfaceHolderCallback;
    private SurfaceSession mSurfaceSession;
    InputStage mSyntheticInputStage;
    private String mTag;
    final int mTargetSdkVersion;
    private final Rect mTempBoundsRect;
    HashSet<View> mTempHashSet;
    private InsetsState mTempInsets;
    final Rect mTempRect;
    final Thread mThread;
    final Rect mTmpFrame;
    final int[] mTmpLocation;
    final TypedValue mTmpValue;
    private final SurfaceControl.Transaction mTransaction;
    CompatibilityInfo.Translator mTranslator;
    final Region mTransparentRegion;
    int mTraversalBarrier;
    final TraversalRunnable mTraversalRunnable;
    public boolean mTraversalScheduled;
    boolean mUnbufferedInputDispatch;
    private final UnhandledKeyManager mUnhandledKeyManager;
    @GuardedBy({"this"})
    boolean mUpcomingInTouchMode;
    @GuardedBy({"this"})
    boolean mUpcomingWindowFocus;
    private boolean mUseMTRenderer;
    @UnsupportedAppUsage
    View mView;
    final ViewConfiguration mViewConfiguration;
    private int mViewLayoutDirectionInitial;
    public final ColorViewRootImplHooks mViewRootHooks;
    int mViewVisibility;
    final Rect mVisRect;
    @UnsupportedAppUsage
    int mWidth;
    boolean mWillDrawSoon;
    final Rect mWinFrame;
    final W mWindow;
    public final WindowManager.LayoutParams mWindowAttributes;
    boolean mWindowAttributesChanged;
    int mWindowAttributesChangesFlag;
    @GuardedBy({"mWindowCallbacks"})
    final ArrayList<WindowCallbacks> mWindowCallbacks;
    CountDownLatch mWindowDrawCountDown;
    @GuardedBy({"this"})
    boolean mWindowFocusChanged;
    @UnsupportedAppUsage
    final IWindowSession mWindowSession;
    private final ArrayList<WindowStoppedCallback> mWindowStoppedCallbacks;

    public interface ActivityConfigCallback {
        void onConfigurationChanged(Configuration configuration, int i);
    }

    public interface ConfigChangedCallback {
        void onConfigurationChanged(Configuration configuration);
    }

    interface WindowStoppedCallback {
        void windowStopped(boolean z);
    }

    static {
        boolean z = LOCAL_LOGV;
        DEBUG_DRAW = z;
        DEBUG_LAYOUT = z;
        DEBUG_DIALOG = z;
        DEBUG_INPUT_RESIZE = z;
        DEBUG_ORIENTATION = z;
        DEBUG_TRACKBALL = z;
        DEBUG_IMF = z;
        DEBUG_CONFIGURATION = z;
        DEBUG_INPUT_STAGES = z;
        DEBUG_KEEP_SCREEN_ON = z;
        DEBUG_CONTENT_CAPTURE = z;
    }

    /* access modifiers changed from: package-private */
    public void setSurfaceViewCreated(boolean created) {
        this.mIsSurfaceViewCreated = created;
        if (created && this.mSurfaceExt == null) {
            this.mSurfaceExt = SurfaceFactory.getInstance().getSurfaceExt();
        }
        Log.d(TAG, "setSurfaceViewCreated, created:" + created);
    }

    /* access modifiers changed from: package-private */
    public static final class SystemUiVisibilityInfo {
        int globalVisibility;
        int localChanges;
        int localValue;
        int seq;

        SystemUiVisibilityInfo() {
        }
    }

    public ViewRootImpl(Context context, Display display) {
        String str;
        boolean z = false;
        this.mIsSurfaceViewCreated = false;
        this.mWindowCallbacks = new ArrayList<>();
        this.mTmpLocation = new int[2];
        this.mTmpValue = new TypedValue();
        this.mWindowAttributes = new WindowManager.LayoutParams();
        this.mAppVisible = true;
        this.mForceDecorViewVisibility = false;
        this.mOrigWindowType = -1;
        this.mStopped = false;
        this.mIsAmbientMode = false;
        this.mPausedForTransition = false;
        this.mLastInCompatMode = false;
        this.mTempBoundsRect = new Rect();
        this.mFirstFrameScheduled = false;
        this.doFrameIndex = 0;
        this.FRAME_ONT = true;
        this.isOptApp = false;
        this.mPendingInputEventQueueLengthCounterName = "pq";
        this.mUnhandledKeyManager = new UnhandledKeyManager();
        this.mWindowAttributesChanged = false;
        this.mWindowAttributesChangesFlag = 0;
        this.mSurface = new Surface();
        this.mSurfaceControl = new SurfaceControl();
        this.mBoundsSurface = new Surface();
        this.mTransaction = new SurfaceControl.Transaction();
        this.mTmpFrame = new Rect();
        this.mPendingOverscanInsets = new Rect();
        this.mPendingVisibleInsets = new Rect();
        this.mPendingStableInsets = new Rect();
        this.mPendingContentInsets = new Rect();
        this.mPendingOutsets = new Rect();
        this.mPendingBackDropFrame = new Rect();
        this.mPendingDisplayCutout = new DisplayCutout.ParcelableWrapper(DisplayCutout.NO_CUTOUT);
        this.mTempInsets = new InsetsState();
        this.mLastGivenInsets = new ViewTreeObserver.InternalInsetsInfo();
        this.mDispatchContentInsets = new Rect();
        this.mDispatchStableInsets = new Rect();
        this.mDispatchDisplayCutout = DisplayCutout.NO_CUTOUT;
        this.mLastConfigurationFromResources = new Configuration();
        this.mLastReportedMergedConfiguration = new MergedConfiguration();
        this.mPendingMergedConfiguration = new MergedConfiguration();
        this.mDragPoint = new PointF();
        this.mLastTouchPoint = new PointF();
        this.mFpsStartTime = -1;
        this.mFpsPrevTime = -1;
        this.mPointerIconType = 1;
        this.mCustomPointerIcon = null;
        this.mAccessibilityInteractionConnectionManager = new AccessibilityInteractionConnectionManager();
        this.mInLayout = false;
        this.mLayoutRequesters = new ArrayList<>();
        this.mHandlingLayoutInLayoutRequest = false;
        this.mColorViewRootUtil = null;
        this.mIsWeixinLauncherUI = false;
        this.mRetryLayout = false;
        this.mManager = null;
        this.mIsLuckyMoneyView = false;
        this.isBuiltinApp = false;
        this.mInputEventConsistencyVerifier = InputEventConsistencyVerifier.isInstrumentationEnabled() ? new InputEventConsistencyVerifier(this, 0) : null;
        this.mInsetsController = new InsetsController(this);
        this.mGestureExclusionTracker = new GestureExclusionTracker();
        this.mTag = TAG;
        this.mProfile = false;
        this.mDisplayListener = new DisplayManager.DisplayListener() {
            /* class android.view.ViewRootImpl.AnonymousClass1 */

            @Override // android.hardware.display.DisplayManager.DisplayListener
            public void onDisplayChanged(int displayId) {
                int oldDisplayState;
                int newDisplayState;
                if (ViewRootImpl.this.mView != null && ViewRootImpl.this.mDisplay.getDisplayId() == displayId && (oldDisplayState = ViewRootImpl.this.mAttachInfo.mDisplayState) != (newDisplayState = ViewRootImpl.this.mDisplay.getState())) {
                    ViewRootImpl.this.mAttachInfo.mDisplayState = newDisplayState;
                    ViewRootImpl.this.pokeDrawLockIfNeeded();
                    if (oldDisplayState != 0) {
                        int oldScreenState = toViewScreenState(oldDisplayState);
                        int newScreenState = toViewScreenState(newDisplayState);
                        if (oldScreenState != newScreenState) {
                            ViewRootImpl.this.mView.dispatchScreenStateChanged(newScreenState);
                        }
                        if (newDisplayState == 2 || (oldDisplayState == 1 && ViewRootImpl.this.mBasePackageName.equals("com.android.systemui"))) {
                            ViewRootImpl viewRootImpl = ViewRootImpl.this;
                            viewRootImpl.mFullRedrawNeeded = true;
                            viewRootImpl.scheduleTraversals();
                        }
                    }
                }
            }

            @Override // android.hardware.display.DisplayManager.DisplayListener
            public void onDisplayRemoved(int displayId) {
            }

            @Override // android.hardware.display.DisplayManager.DisplayListener
            public void onDisplayAdded(int displayId) {
            }

            private int toViewScreenState(int displayState) {
                if (displayState == 1 || displayState == 3 || displayState == 4) {
                    return 0;
                }
                return 1;
            }
        };
        this.mWindowStoppedCallbacks = new ArrayList<>();
        this.mDrawsNeededToReport = 0;
        this.mHandler = new ViewRootHandler();
        this.mTraversalRunnable = new TraversalRunnable();
        this.mConsumedBatchedInputRunnable = new ConsumeBatchedInputRunnable();
        this.mConsumeBatchedInputImmediatelyRunnable = new ConsumeBatchedInputImmediatelyRunnable();
        this.mInvalidateOnAnimationRunnable = new InvalidateOnAnimationRunnable();
        this.mFrame = 0;
        this.mScreenModeViewList = new ArrayList<>();
        this.mOverrideRefreshRateId = 0;
        this.mContext = context;
        this.mViewRootHooks = new ColorViewRootImplHooks(this, context);
        this.mWindowSession = WindowManagerGlobal.getWindowSession();
        this.mDisplay = display;
        this.mBasePackageName = context.getBasePackageName();
        this.mThread = Thread.currentThread();
        this.mLocation = new WindowLeaked(null);
        this.mLocation.fillInStackTrace();
        this.mWidth = -1;
        this.mHeight = -1;
        this.mDirty = new Rect();
        this.mTempRect = new Rect();
        this.mVisRect = new Rect();
        this.mWinFrame = new Rect();
        this.mWindow = this.mViewRootHooks.createWindowClient(this);
        DEBUG_PANIC = SystemProperties.getBoolean("persist.sys.assert.panic", false);
        DEBUG_IMF = DEBUG_PANIC;
        this.mTargetSdkVersion = context.getApplicationInfo().targetSdkVersion;
        this.mViewVisibility = 8;
        this.mTransparentRegion = new Region();
        this.mPreviousTransparentRegion = new Region();
        this.mFirst = true;
        this.mAdded = false;
        this.mAttachInfo = new View.AttachInfo(this.mWindowSession, this.mWindow, display, this, this.mHandler, this, context);
        this.mAccessibilityManager = AccessibilityManager.getInstance(context);
        this.mAccessibilityManager.addAccessibilityStateChangeListener(this.mAccessibilityInteractionConnectionManager, this.mHandler);
        this.mHighContrastTextManager = new HighContrastTextManager();
        this.mAccessibilityManager.addHighTextContrastStateChangeListener(this.mHighContrastTextManager, this.mHandler);
        this.mViewConfiguration = ViewConfiguration.get(context);
        this.mDensity = context.getResources().getDisplayMetrics().densityDpi;
        this.mNoncompatDensity = context.getResources().getDisplayMetrics().noncompatDensityDpi;
        this.mFallbackEventHandler = new PhoneFallbackEventHandler(context);
        this.mChoreographer = Choreographer.getInstance();
        this.mDisplayManager = (DisplayManager) context.getSystemService("display");
        String processorOverrideName = context.getResources().getString(R.string.config_inputEventCompatProcessorOverrideClassName);
        if (processorOverrideName.isEmpty()) {
            this.mInputCompatProcessor = new InputEventCompatProcessor(context);
        } else {
            InputEventCompatProcessor compatProcessor = null;
            try {
                compatProcessor = (InputEventCompatProcessor) Class.forName(processorOverrideName).getConstructor(Context.class).newInstance(context);
            } catch (Exception e) {
                Log.e(TAG, "Unable to create the InputEventCompatProcessor. ", e);
            } catch (Throwable th) {
                this.mInputCompatProcessor = null;
                throw th;
            }
            this.mInputCompatProcessor = compatProcessor;
        }
        if (!sCompatibilityDone) {
            sAlwaysAssignFocus = this.mTargetSdkVersion < 28;
            sCompatibilityDone = true;
        }
        loadSystemProperties();
        ViewDebugManager.getInstance().debugViewRootConstruct(this.mTag, context, this.mThread, this.mChoreographer, this.mTraversalRunnable, this);
        this.FRAME_ONT = SystemProperties.getBoolean("persist.oppo.frameopts", true);
        if (this.FRAME_ONT && (str = this.mBasePackageName) != null) {
            this.isOptApp = ActivityThread.inCptWhiteList(MetricsProto.MetricsEvent.ACTION_PERMISSION_REVOKE_RECEIVE_MMS, str);
        }
        try {
            this.mColorViewRootUtil = (IColorViewRootUtil) OppoFeatureCache.getOrCreate(IColorViewRootUtil.DEFAULT, new Object[0]);
            if (this.mColorViewRootUtil != null) {
                this.mColorViewRootUtil.initSwipState(display, this.mContext);
            }
            this.mManager = ColorFrameworkFactory.getInstance().newColorDarkModeManager();
        } catch (LinkageError e2) {
            Log.e(TAG, "mColorViewRootUtil", e2);
        }
        setDynamicalLogEnable(SystemProperties.get("debug.dynamicallog.view", "").equals(this.mBasePackageName));
        DEBUG_PANIC = SystemProperties.getBoolean("persist.sys.assert.panic", false);
        DEBUG_IMF = DEBUG_PANIC;
        this.isBuiltinApp = ((context.getApplicationInfo().flags & 1) == 0 && (context.getApplicationInfo().flags & 128) == 0) ? z : true;
    }

    public static void addFirstDrawHandler(Runnable callback) {
        synchronized (sFirstDrawHandlers) {
            if (!sFirstDrawComplete) {
                sFirstDrawHandlers.add(callback);
            }
        }
    }

    @UnsupportedAppUsage
    public static void addConfigCallback(ConfigChangedCallback callback) {
        synchronized (sConfigCallbacks) {
            sConfigCallbacks.add(callback);
        }
    }

    public void setActivityConfigCallback(ActivityConfigCallback callback) {
        this.mActivityConfigCallback = callback;
    }

    public void addWindowCallbacks(WindowCallbacks callback) {
        synchronized (this.mWindowCallbacks) {
            this.mWindowCallbacks.add(callback);
        }
    }

    public void removeWindowCallbacks(WindowCallbacks callback) {
        synchronized (this.mWindowCallbacks) {
            this.mWindowCallbacks.remove(callback);
        }
    }

    public void reportDrawFinish() {
        CountDownLatch countDownLatch = this.mWindowDrawCountDown;
        if (countDownLatch != null) {
            countDownLatch.countDown();
        }
    }

    public void profile() {
        this.mProfile = true;
    }

    static boolean isInTouchMode() {
        IWindowSession windowSession = WindowManagerGlobal.peekWindowSession();
        if (windowSession == null) {
            return false;
        }
        try {
            return windowSession.getInTouchMode();
        } catch (RemoteException e) {
            return false;
        }
    }

    public void notifyChildRebuilt() {
        if (this.mView instanceof RootViewSurfaceTaker) {
            SurfaceHolder.Callback2 callback2 = this.mSurfaceHolderCallback;
            if (callback2 != null) {
                this.mSurfaceHolder.removeCallback(callback2);
            }
            this.mSurfaceHolderCallback = ((RootViewSurfaceTaker) this.mView).willYouTakeTheSurface();
            if (this.mSurfaceHolderCallback != null) {
                this.mSurfaceHolder = new TakenSurfaceHolder();
                this.mSurfaceHolder.setFormat(0);
                this.mSurfaceHolder.addCallback(this.mSurfaceHolderCallback);
            } else {
                this.mSurfaceHolder = null;
            }
            this.mInputQueueCallback = ((RootViewSurfaceTaker) this.mView).willYouTakeTheInputQueue();
            InputQueue.Callback callback = this.mInputQueueCallback;
            if (callback != null) {
                callback.onInputQueueCreated(this.mInputQueue);
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:154:0x0495  */
    public void setView(View view, WindowManager.LayoutParams attrs, View panelParentView) {
        Throwable th;
        boolean restore;
        RemoteException e;
        RemoteException e2;
        synchronized (this) {
            try {
                if (this.mView == null) {
                    this.mView = view;
                    this.mViewRootHooks.setView(this.mView);
                    this.mAttachInfo.mDisplayState = this.mDisplay.getState();
                    this.mDisplayManager.registerDisplayListener(this.mDisplayListener, this.mHandler);
                    this.mViewLayoutDirectionInitial = this.mView.getRawLayoutDirection();
                    this.mFallbackEventHandler.setView(view);
                    try {
                        this.mWindowAttributes.copyFrom(attrs);
                        if (this.mWindowAttributes.packageName == null) {
                            this.mWindowAttributes.packageName = this.mBasePackageName;
                        }
                        WindowManager.LayoutParams attrs2 = this.mWindowAttributes;
                        try {
                            setTag();
                            if (DEBUG_KEEP_SCREEN_ON && (this.mClientWindowLayoutFlags & 128) != 0 && (attrs2.flags & 128) == 0) {
                                Slog.d(this.mTag, "setView: FLAG_KEEP_SCREEN_ON changed from true to false!");
                            }
                            this.mClientWindowLayoutFlags = attrs2.flags;
                            setAccessibilityFocus(null, null);
                            if (view instanceof RootViewSurfaceTaker) {
                                this.mSurfaceHolderCallback = ((RootViewSurfaceTaker) view).willYouTakeTheSurface();
                                if (this.mSurfaceHolderCallback != null) {
                                    this.mSurfaceHolder = new TakenSurfaceHolder();
                                    this.mSurfaceHolder.setFormat(0);
                                    this.mSurfaceHolder.addCallback(this.mSurfaceHolderCallback);
                                }
                            }
                            if (!attrs2.hasManualSurfaceInsets) {
                                attrs2.setSurfaceInsets(view, false, true);
                            }
                            CompatibilityInfo compatibilityInfo = this.mDisplay.getDisplayAdjustments().getCompatibilityInfo();
                            this.mTranslator = compatibilityInfo.getTranslator();
                            if (this.mSurfaceHolder == null) {
                                enableHardwareAcceleration(attrs2);
                                boolean useMTRenderer = this.mAttachInfo.mThreadedRenderer != null;
                                if (this.mUseMTRenderer != useMTRenderer) {
                                    endDragResizing();
                                    this.mUseMTRenderer = useMTRenderer;
                                }
                            }
                            if (this.mTranslator != null) {
                                this.mSurface.setCompatibilityTranslator(this.mTranslator);
                                attrs2.backup();
                                this.mTranslator.translateWindowLayout(attrs2);
                                restore = true;
                            } else {
                                restore = false;
                            }
                            if (DEBUG_LAYOUT || ViewDebugManager.DEBUG_LIFECYCLE) {
                                Log.d(this.mTag, "WindowLayout in setView:" + attrs2 + ",mView = " + this.mView + ",compatibilityInfo = " + compatibilityInfo + ", this = " + this);
                            }
                            if (!compatibilityInfo.supportsScreen()) {
                                attrs2.privateFlags |= 128;
                                this.mLastInCompatMode = true;
                            }
                            this.mSoftInputMode = attrs2.softInputMode;
                            this.mWindowAttributesChanged = true;
                            this.mWindowAttributesChangesFlag = -1;
                            this.mAttachInfo.mRootView = view;
                            this.mAttachInfo.mScalingRequired = this.mTranslator != null;
                            this.mAttachInfo.mApplicationScale = this.mTranslator == null ? 1.0f : this.mTranslator.applicationScale;
                            if (panelParentView != null) {
                                this.mAttachInfo.mPanelParentWindowToken = panelParentView.getApplicationWindowToken();
                            }
                            this.mAdded = true;
                            requestLayout();
                            if ((this.mWindowAttributes.inputFeatures & 2) == 0) {
                                this.mInputChannel = new InputChannel();
                            }
                            this.mForceDecorViewVisibility = (this.mWindowAttributes.privateFlags & 16384) != 0;
                            this.mViewRootHooks.markUserDefinedToast(this.mView, this.mWindowAttributes);
                            try {
                                this.mOrigWindowType = this.mWindowAttributes.type;
                                this.mAttachInfo.mRecomputeGlobalAttributes = true;
                                collectViewAttributes();
                                try {
                                } catch (RemoteException e3) {
                                    e2 = e3;
                                    try {
                                        this.mAdded = false;
                                        this.mView = null;
                                        this.mAttachInfo.mRootView = null;
                                        this.mInputChannel = null;
                                        this.mFallbackEventHandler.setView(null);
                                        unscheduleTraversals();
                                        setAccessibilityFocus(null, null);
                                        throw new RuntimeException("Adding window failed", e2);
                                    } catch (Throwable th2) {
                                        e = th2;
                                        if (restore) {
                                            attrs2.restore();
                                        }
                                        throw e;
                                    }
                                }
                                try {
                                    int res = this.mWindowSession.addToDisplay(this.mWindow, this.mSeq, this.mWindowAttributes, getHostVisibility(), this.mDisplay.getDisplayId(), this.mTmpFrame, this.mAttachInfo.mContentInsets, this.mAttachInfo.mStableInsets, this.mAttachInfo.mOutsets, this.mAttachInfo.mDisplayCutout, this.mInputChannel, this.mTempInsets);
                                    setFrame(this.mTmpFrame);
                                    if (restore) {
                                        try {
                                            attrs2.restore();
                                        } catch (Throwable th3) {
                                            th = th3;
                                            throw th;
                                        }
                                    }
                                    if (this.mTranslator != null) {
                                        this.mTranslator.translateRectInScreenToAppWindow(this.mAttachInfo.mContentInsets);
                                    }
                                    this.mPendingOverscanInsets.set(0, 0, 0, 0);
                                    this.mPendingContentInsets.set(this.mAttachInfo.mContentInsets);
                                    this.mPendingStableInsets.set(this.mAttachInfo.mStableInsets);
                                    this.mPendingDisplayCutout.set(this.mAttachInfo.mDisplayCutout);
                                    this.mPendingVisibleInsets.set(0, 0, 0, 0);
                                    this.mAttachInfo.mAlwaysConsumeSystemBars = (res & 4) != 0;
                                    this.mPendingAlwaysConsumeSystemBars = this.mAttachInfo.mAlwaysConsumeSystemBars;
                                    this.mInsetsController.onStateChanged(this.mTempInsets);
                                    if (DEBUG_LAYOUT) {
                                        Log.v(this.mTag, "Added window " + this.mWindow + ", mPendingContentInsets = " + this.mPendingContentInsets + ", mPendingStableInsets = " + this.mPendingStableInsets);
                                    }
                                    if (res < 0) {
                                        this.mAttachInfo.mRootView = null;
                                        this.mAdded = false;
                                        this.mFallbackEventHandler.setView(null);
                                        unscheduleTraversals();
                                        setAccessibilityFocus(null, null);
                                        switch (res) {
                                            case -10:
                                                throw new WindowManager.InvalidDisplayException("Unable to add window " + this.mWindow + " -- the specified window type " + this.mWindowAttributes.type + " is not valid");
                                            case -9:
                                                throw new WindowManager.InvalidDisplayException("Unable to add window " + this.mWindow + " -- the specified display can not be found");
                                            case -8:
                                                throw new WindowManager.BadTokenException("Unable to add window " + this.mWindow + " -- permission denied for window type " + this.mWindowAttributes.type);
                                            case -7:
                                                throw new WindowManager.BadTokenException("Unable to add window " + this.mWindow + " -- another window of type " + this.mWindowAttributes.type + " already exists");
                                            case -6:
                                                return;
                                            case -5:
                                                throw new WindowManager.BadTokenException("Unable to add window -- window " + this.mWindow + " has already been added");
                                            case -4:
                                                throw new WindowManager.BadTokenException("Unable to add window -- app for token " + attrs2.token + " is exiting");
                                            case -3:
                                                throw new WindowManager.BadTokenException("Unable to add window -- token " + attrs2.token + " is not for an application");
                                            case -2:
                                            case -1:
                                                throw new WindowManager.BadTokenException("Unable to add window -- token " + attrs2.token + " is not valid; is your activity running?");
                                            default:
                                                throw new RuntimeException("Unable to add window -- unknown error code " + res);
                                        }
                                    } else {
                                        if (view instanceof RootViewSurfaceTaker) {
                                            this.mInputQueueCallback = ((RootViewSurfaceTaker) view).willYouTakeTheInputQueue();
                                        }
                                        if (this.mInputChannel != null) {
                                            if (this.mInputQueueCallback != null) {
                                                this.mInputQueue = new InputQueue();
                                                this.mInputQueueCallback.onInputQueueCreated(this.mInputQueue);
                                            }
                                            this.mInputEventReceiver = new WindowInputEventReceiver(this.mInputChannel, Looper.myLooper());
                                        }
                                        view.assignParent(this);
                                        this.mAddedTouchMode = (res & 1) != 0;
                                        this.mAppVisible = (res & 2) != 0;
                                        if (this.mAccessibilityManager.isEnabled()) {
                                            this.mAccessibilityInteractionConnectionManager.ensureConnection();
                                        }
                                        if (view.getImportantForAccessibility() == 0) {
                                            view.setImportantForAccessibility(1);
                                        }
                                        CharSequence counterSuffix = attrs2.getTitle();
                                        this.mSyntheticInputStage = new SyntheticInputStage();
                                        InputStage earlyPostImeStage = new EarlyPostImeInputStage(new NativePostImeInputStage(new ViewPostImeInputStage(this.mSyntheticInputStage), "aq:native-post-ime:" + ((Object) counterSuffix)));
                                        this.mFirstInputStage = new NativePreImeInputStage(new ViewPreImeInputStage(new ImeInputStage(earlyPostImeStage, "aq:ime:" + ((Object) counterSuffix))), "aq:native-pre-ime:" + ((Object) counterSuffix));
                                        this.mFirstPostImeInputStage = earlyPostImeStage;
                                        this.mPendingInputEventQueueLengthCounterName = "aq:pending:" + ((Object) counterSuffix);
                                    }
                                } catch (RemoteException e4) {
                                    e2 = e4;
                                    this.mAdded = false;
                                    this.mView = null;
                                    this.mAttachInfo.mRootView = null;
                                    this.mInputChannel = null;
                                    this.mFallbackEventHandler.setView(null);
                                    unscheduleTraversals();
                                    setAccessibilityFocus(null, null);
                                    throw new RuntimeException("Adding window failed", e2);
                                } catch (Throwable th4) {
                                    e = th4;
                                    if (restore) {
                                    }
                                    throw e;
                                }
                            } catch (RemoteException e5) {
                                e2 = e5;
                                this.mAdded = false;
                                this.mView = null;
                                this.mAttachInfo.mRootView = null;
                                this.mInputChannel = null;
                                this.mFallbackEventHandler.setView(null);
                                unscheduleTraversals();
                                setAccessibilityFocus(null, null);
                                throw new RuntimeException("Adding window failed", e2);
                            } catch (Throwable th5) {
                                e = th5;
                                if (restore) {
                                }
                                throw e;
                            }
                        } catch (Throwable th6) {
                            th = th6;
                            throw th;
                        }
                    } catch (Throwable th7) {
                        th = th7;
                        throw th;
                    }
                }
            } catch (Throwable th8) {
                th = th8;
                throw th;
            }
        }
    }

    private void setTag() {
        String[] split = this.mWindowAttributes.getTitle().toString().split("\\.");
        if (split.length > 0) {
            this.mTag = "ViewRootImpl[" + split[split.length - 1] + "]";
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isInLocalFocusMode() {
        return (this.mWindowAttributes.flags & 268435456) != 0;
    }

    @UnsupportedAppUsage
    public int getWindowFlags() {
        return this.mWindowAttributes.flags;
    }

    public int getDisplayId() {
        return this.mDisplay.getDisplayId();
    }

    public CharSequence getTitle() {
        return this.mWindowAttributes.getTitle();
    }

    public int getWidth() {
        return this.mWidth;
    }

    public int getHeight() {
        return this.mHeight;
    }

    /* access modifiers changed from: package-private */
    public void destroyHardwareResources() {
        synchronized (this.mDestroyLock) {
            ThreadedRenderer renderer = this.mAttachInfo.mThreadedRenderer;
            if (renderer != null) {
                if (Looper.myLooper() != this.mAttachInfo.mHandler.getLooper()) {
                    this.mAttachInfo.mHandler.postAtFrontOfQueue(new Runnable() {
                        /* class android.view.$$Lambda$dj1hfDQd0iEp_uBDBPEUMMYJJwk */

                        public final void run() {
                            ViewRootImpl.this.destroyHardwareResources();
                        }
                    });
                } else {
                    renderer.destroyHardwareResources(this.mView);
                    renderer.destroy();
                }
            }
        }
    }

    @UnsupportedAppUsage
    public void detachFunctor(long functor) {
        if (this.mAttachInfo.mThreadedRenderer != null) {
            this.mAttachInfo.mThreadedRenderer.stopDrawing();
        }
    }

    @UnsupportedAppUsage
    public static void invokeFunctor(long functor, boolean waitForCompletion) {
        ThreadedRenderer.invokeFunctor(functor, waitForCompletion);
    }

    public void registerAnimatingRenderNode(RenderNode animator) {
        if (this.mAttachInfo.mThreadedRenderer != null) {
            this.mAttachInfo.mThreadedRenderer.registerAnimatingRenderNode(animator);
            return;
        }
        if (this.mAttachInfo.mPendingAnimatingRenderNodes == null) {
            this.mAttachInfo.mPendingAnimatingRenderNodes = new ArrayList();
        }
        this.mAttachInfo.mPendingAnimatingRenderNodes.add(animator);
    }

    public void registerVectorDrawableAnimator(NativeVectorDrawableAnimator animator) {
        if (this.mAttachInfo.mThreadedRenderer != null) {
            this.mAttachInfo.mThreadedRenderer.registerVectorDrawableAnimator(animator);
        }
    }

    public void registerRtFrameCallback(HardwareRenderer.FrameDrawingCallback callback) {
        if (this.mAttachInfo.mThreadedRenderer != null) {
            this.mAttachInfo.mThreadedRenderer.registerRtFrameCallback(new HardwareRenderer.FrameDrawingCallback() {
                /* class android.view.$$Lambda$ViewRootImpl$IReiNMSbDakZSGbIZuL_ifaFWn8 */

                @Override // android.graphics.HardwareRenderer.FrameDrawingCallback
                public final void onFrameDraw(long j) {
                    ViewRootImpl.lambda$registerRtFrameCallback$0(HardwareRenderer.FrameDrawingCallback.this, j);
                }
            });
        }
    }

    static /* synthetic */ void lambda$registerRtFrameCallback$0(HardwareRenderer.FrameDrawingCallback callback, long frame) {
        try {
            callback.onFrameDraw(frame);
        } catch (Exception e) {
            Log.e(TAG, "Exception while executing onFrameDraw", e);
        }
    }

    @UnsupportedAppUsage
    private void enableHardwareAcceleration(WindowManager.LayoutParams attrs) {
        View.AttachInfo attachInfo = this.mAttachInfo;
        boolean wideGamut = false;
        attachInfo.mHardwareAccelerated = false;
        attachInfo.mHardwareAccelerationRequested = false;
        if (DEBUG_DISABLEHW) {
            Log.d(TAG, "disable hardware acceleration by DEBUG!!!, this = " + this);
        } else if (this.mTranslator == null) {
            boolean hardwareAccelerated = ViewDebugManager.getInstance().debugForceHWDraw((attrs.flags & 16777216) != 0);
            boolean forceHardwareAccelerated = this.mManager.ensureHardWareWhenDarkMode(this.mContext, hardwareAccelerated);
            if ((hardwareAccelerated || forceHardwareAccelerated) && ThreadedRenderer.isAvailable()) {
                boolean fakeHwAccelerated = (attrs.privateFlags & 1) != 0;
                boolean forceHwAccelerated = (attrs.privateFlags & 2) != 0;
                if (fakeHwAccelerated) {
                    this.mAttachInfo.mHardwareAccelerationRequested = true;
                } else if (!ThreadedRenderer.sRendererDisabled || (ThreadedRenderer.sSystemRendererDisabled && forceHwAccelerated)) {
                    if (this.mAttachInfo.mThreadedRenderer != null) {
                        this.mAttachInfo.mThreadedRenderer.destroy();
                    }
                    Rect insets = attrs.surfaceInsets;
                    boolean translucent = attrs.format != -1 || (insets.left != 0 || insets.right != 0 || insets.top != 0 || insets.bottom != 0);
                    if (this.mContext.getResources().getConfiguration().isScreenWideColorGamut() && attrs.getColorMode() == 1) {
                        wideGamut = true;
                    }
                    this.mAttachInfo.mThreadedRenderer = ThreadedRenderer.create(this.mContext, translucent, attrs.getTitle().toString());
                    this.mAttachInfo.mThreadedRenderer.setWideGamut(wideGamut);
                    updateForceDarkMode();
                    if (this.mAttachInfo.mThreadedRenderer != null) {
                        View.AttachInfo attachInfo2 = this.mAttachInfo;
                        attachInfo2.mHardwareAccelerationRequested = true;
                        attachInfo2.mHardwareAccelerated = true;
                    }
                }
                if (ViewDebugManager.DEBUG_USER) {
                    Log.d(this.mTag, "hardware acceleration = " + this.mAttachInfo.mHardwareAccelerated + " , fakeHwAccelerated = " + fakeHwAccelerated + ", sRendererDisabled = " + ThreadedRenderer.sRendererDisabled + ", forceHwAccelerated = " + forceHwAccelerated + ", sSystemRendererDisabled = " + ThreadedRenderer.sSystemRendererDisabled);
                }
            }
        }
    }

    private int getNightMode() {
        return this.mContext.getResources().getConfiguration().uiMode & 48;
    }

    public void updateForceDarkMode() {
        if (this.mAttachInfo.mThreadedRenderer != null) {
            boolean z = true;
            boolean useAutoDark = getNightMode() == 32;
            this.mManager.logConfigurationNightError(this.mContext, useAutoDark);
            if (useAutoDark) {
                boolean forceDarkAllowedDefault = SystemProperties.getBoolean(ThreadedRenderer.DEBUG_FORCE_DARK, false);
                TypedArray a = this.mContext.obtainStyledAttributes(R.styleable.Theme);
                if (!a.getBoolean(279, true) || !a.getBoolean(278, forceDarkAllowedDefault)) {
                    z = false;
                }
                useAutoDark = z;
                a.recycle();
                ((IColorDarkModeManager) OppoFeatureCache.getOrCreate(IColorDarkModeManager.DEFAULT, new Object[0])).logForceDarkAllowedStatus(this.mContext, forceDarkAllowedDefault);
            }
            if (this.mAttachInfo.mThreadedRenderer.setForceDark(this.mManager.forceDarkWithoutTheme(this.mContext, useAutoDark))) {
                invalidateWorld(this.mView);
            }
        }
    }

    @UnsupportedAppUsage
    public View getView() {
        return this.mView;
    }

    /* access modifiers changed from: package-private */
    public final WindowLeaked getLocation() {
        return this.mLocation;
    }

    /* access modifiers changed from: package-private */
    public void setLayoutParams(WindowManager.LayoutParams attrs, boolean newView) {
        synchronized (this) {
            int oldInsetLeft = this.mWindowAttributes.surfaceInsets.left;
            int oldInsetTop = this.mWindowAttributes.surfaceInsets.top;
            int oldInsetRight = this.mWindowAttributes.surfaceInsets.right;
            int oldInsetBottom = this.mWindowAttributes.surfaceInsets.bottom;
            int oldSoftInputMode = this.mWindowAttributes.softInputMode;
            boolean oldHasManualSurfaceInsets = this.mWindowAttributes.hasManualSurfaceInsets;
            if (DEBUG_KEEP_SCREEN_ON && (this.mClientWindowLayoutFlags & 128) != 0 && (attrs.flags & 128) == 0) {
                Slog.d(this.mTag, "setLayoutParams: FLAG_KEEP_SCREEN_ON from true to false!");
            }
            this.mClientWindowLayoutFlags = attrs.flags;
            int compatibleWindowFlag = this.mWindowAttributes.privateFlags & 128;
            attrs.systemUiVisibility = this.mWindowAttributes.systemUiVisibility;
            attrs.subtreeSystemUiVisibility = this.mWindowAttributes.subtreeSystemUiVisibility;
            this.mWindowAttributesChangesFlag = this.mWindowAttributes.copyFrom(attrs);
            if ((this.mWindowAttributesChangesFlag & 524288) != 0) {
                this.mAttachInfo.mRecomputeGlobalAttributes = true;
            }
            if ((this.mWindowAttributesChangesFlag & 1) != 0) {
                this.mAttachInfo.mNeedsUpdateLightCenter = true;
            }
            if (this.mWindowAttributes.packageName == null) {
                this.mWindowAttributes.packageName = this.mBasePackageName;
            }
            this.mWindowAttributes.privateFlags |= compatibleWindowFlag;
            if (this.mWindowAttributes.preservePreviousSurfaceInsets) {
                this.mWindowAttributes.surfaceInsets.set(oldInsetLeft, oldInsetTop, oldInsetRight, oldInsetBottom);
                this.mWindowAttributes.hasManualSurfaceInsets = oldHasManualSurfaceInsets;
            } else if (!(this.mWindowAttributes.surfaceInsets.left == oldInsetLeft && this.mWindowAttributes.surfaceInsets.top == oldInsetTop && this.mWindowAttributes.surfaceInsets.right == oldInsetRight && this.mWindowAttributes.surfaceInsets.bottom == oldInsetBottom)) {
                this.mNeedsRendererSetup = true;
            }
            applyKeepScreenOnFlag(this.mWindowAttributes);
            if (newView) {
                this.mSoftInputMode = attrs.softInputMode;
                requestLayout();
            }
            if ((attrs.softInputMode & 240) == 0) {
                this.mWindowAttributes.softInputMode = (this.mWindowAttributes.softInputMode & TrafficStats.TAG_SYSTEM_IMPERSONATION_RANGE_END) | (oldSoftInputMode & 240);
            }
            this.mWindowAttributesChanged = true;
            scheduleTraversals();
        }
        if (DEBUG_IMF) {
            Log.d(this.mTag, "setLayoutParams: attrs = " + attrs + ", mSoftInputMode = " + this.mSoftInputMode + ", mWindowAttributes = " + this.mWindowAttributes + ", this = " + this);
        }
    }

    /* access modifiers changed from: package-private */
    public void handleAppVisibility(boolean visible) {
        if (DEBUG_LAYOUT) {
            String str = this.mTag;
            Log.d(str, "handleAppVisibility: visible=" + visible + ", mAppVisible=" + this.mAppVisible + ", this = " + this);
        }
        if (this.mAppVisible != visible) {
            this.mAppVisible = visible;
            this.mAppVisibilityChanged = true;
            scheduleTraversals();
            if (!this.mAppVisible) {
                WindowManagerGlobal.trimForeground();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void handleGetNewSurface() {
        this.mNewSurfaceNeeded = true;
        this.mFullRedrawNeeded = true;
        scheduleTraversals();
    }

    public void onMovedToDisplay(int displayId, Configuration config) {
        if (this.mDisplay.getDisplayId() != displayId) {
            updateInternalDisplay(displayId, this.mView.getResources());
            this.mAttachInfo.mDisplayState = this.mDisplay.getState();
            this.mView.dispatchMovedToDisplay(this.mDisplay, config);
        }
    }

    private void updateInternalDisplay(int displayId, Resources resources) {
        Display preferredDisplay = ResourcesManager.getInstance().getAdjustedDisplay(displayId, resources);
        if (preferredDisplay == null) {
            Slog.w(TAG, "Cannot get desired display with Id: " + displayId);
            this.mDisplay = ResourcesManager.getInstance().getAdjustedDisplay(0, resources);
        } else {
            this.mDisplay = preferredDisplay;
        }
        this.mContext.updateDisplay(this.mDisplay.getDisplayId());
    }

    /* access modifiers changed from: package-private */
    public void pokeDrawLockIfNeeded() {
        int displayState = this.mAttachInfo.mDisplayState;
        if (this.mView != null && this.mAdded && this.mTraversalScheduled && displayState == 3) {
            try {
                this.mWindowSession.pokeDrawLock(this.mWindow);
            } catch (RemoteException e) {
            }
        }
    }

    @Override // android.view.ViewParent
    public void requestFitSystemWindows() {
        checkThread();
        this.mApplyInsetsRequested = true;
        scheduleTraversals();
    }

    /* access modifiers changed from: package-private */
    public void notifyInsetsChanged() {
        if (sNewInsetsMode != 0) {
            this.mApplyInsetsRequested = true;
            if (!this.mIsInTraversal) {
                scheduleTraversals();
            }
        }
    }

    @Override // android.view.ViewParent
    public void requestLayout() {
        if (!this.mHandlingLayoutInLayoutRequest) {
            if (ViewDebugManager.DEBUG_REQUESTLAYOUT) {
                String str = this.mTag;
                Log.d(str, "requestLayout: mView = " + this.mView + ", this = " + this, new Throwable("requestLayout"));
            }
            checkThread();
            this.mLayoutRequested = true;
            scheduleTraversals();
        }
    }

    @Override // android.view.ViewParent
    public boolean isLayoutRequested() {
        return this.mLayoutRequested;
    }

    @Override // android.view.ViewParent
    public void onDescendantInvalidated(View child, View descendant) {
        if ((descendant.mPrivateFlags & 64) != 0) {
            this.mIsAnimating = true;
        }
        invalidate();
    }

    /* access modifiers changed from: package-private */
    @UnsupportedAppUsage
    public void invalidate() {
        this.mDirty.set(0, 0, this.mWidth, this.mHeight);
        if (!this.mWillDrawSoon) {
            scheduleTraversals();
        }
    }

    /* access modifiers changed from: package-private */
    public void invalidateWorld(View view) {
        view.invalidate();
        if (view instanceof ViewGroup) {
            ViewGroup parent = (ViewGroup) view;
            for (int i = 0; i < parent.getChildCount(); i++) {
                invalidateWorld(parent.getChildAt(i));
            }
        }
    }

    @Override // android.view.ViewParent
    public void invalidateChild(View child, Rect dirty) {
        invalidateChildInParent(null, dirty);
    }

    @Override // android.view.ViewParent
    public ViewParent invalidateChildInParent(int[] location, Rect dirty) {
        checkThread();
        if (DEBUG_DRAW) {
            String str = this.mTag;
            Log.v(str, "Invalidate child: " + dirty);
        }
        if (dirty == null) {
            invalidate();
            return null;
        } else if (dirty.isEmpty() && !this.mIsAnimating) {
            return null;
        } else {
            if (!(this.mCurScrollY == 0 && this.mTranslator == null)) {
                this.mTempRect.set(dirty);
                dirty = this.mTempRect;
                int i = this.mCurScrollY;
                if (i != 0) {
                    dirty.offset(0, -i);
                }
                CompatibilityInfo.Translator translator = this.mTranslator;
                if (translator != null) {
                    translator.translateRectInAppWindowToScreen(dirty);
                }
                if (this.mAttachInfo.mScalingRequired) {
                    dirty.inset(-1, -1);
                }
            }
            invalidateRectOnScreen(dirty);
            return null;
        }
    }

    private void invalidateRectOnScreen(Rect dirty) {
        Rect localDirty = this.mDirty;
        localDirty.union(dirty.left, dirty.top, dirty.right, dirty.bottom);
        float appScale = this.mAttachInfo.mApplicationScale;
        boolean intersected = localDirty.intersect(0, 0, (int) ((((float) this.mWidth) * appScale) + 0.5f), (int) ((((float) this.mHeight) * appScale) + 0.5f));
        if (!intersected) {
            localDirty.setEmpty();
        }
        if (!this.mWillDrawSoon && (intersected || this.mIsAnimating)) {
            scheduleTraversals();
        } else if (DEBUG_DRAW) {
            String str = this.mTag;
            Log.v(str, "Invalidate child: Do not scheduleTraversals, mWillDrawSoon =" + this.mWillDrawSoon + ", intersected =" + intersected + ", mIsAnimating =" + this.mIsAnimating);
        }
    }

    public void setIsAmbientMode(boolean ambient) {
        this.mIsAmbientMode = ambient;
    }

    /* access modifiers changed from: package-private */
    public void addWindowStoppedCallback(WindowStoppedCallback c) {
        this.mWindowStoppedCallbacks.add(c);
    }

    /* access modifiers changed from: package-private */
    public void removeWindowStoppedCallback(WindowStoppedCallback c) {
        this.mWindowStoppedCallbacks.remove(c);
    }

    /* access modifiers changed from: package-private */
    public void setWindowStopped(boolean stopped) {
        if (DEBUG_PANIC) {
            String str = this.mTag;
            Log.d(str, "setWindowStopped, stopped:" + stopped + " mStopped " + this.mStopped + " this " + this + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + Debug.getCallers(6));
        }
        checkThread();
        if (this.mStopped != stopped) {
            this.mStopped = stopped;
            ThreadedRenderer renderer = this.mAttachInfo.mThreadedRenderer;
            if (renderer != null) {
                if (DEBUG_DRAW) {
                    String str2 = this.mTag;
                    Log.d(str2, "WindowStopped on " + ((Object) getTitle()) + " set to " + this.mStopped);
                }
                renderer.setStopped(this.mStopped);
            }
            if (!this.mStopped) {
                this.mNewSurfaceNeeded = true;
                scheduleTraversals();
            } else if (renderer != null) {
                renderer.destroyHardwareResources(this.mView);
            }
            for (int i = 0; i < this.mWindowStoppedCallbacks.size(); i++) {
                this.mWindowStoppedCallbacks.get(i).windowStopped(stopped);
            }
            if (this.mStopped) {
                if (this.mSurfaceHolder != null && this.mSurface.isValid()) {
                    notifySurfaceDestroyed();
                }
                destroySurface();
            }
        }
    }

    public void createBoundsSurface(int zOrderLayer) {
        if (this.mSurfaceSession == null) {
            this.mSurfaceSession = new SurfaceSession();
        }
        if (this.mBoundsSurfaceControl == null || !this.mBoundsSurface.isValid()) {
            SurfaceControl.Builder builder = new SurfaceControl.Builder(this.mSurfaceSession);
            this.mBoundsSurfaceControl = builder.setName("Bounds for - " + getTitle().toString()).setParent(this.mSurfaceControl).build();
            setBoundsSurfaceCrop();
            this.mTransaction.setLayer(this.mBoundsSurfaceControl, zOrderLayer).show(this.mBoundsSurfaceControl).apply();
            this.mBoundsSurface.copyFrom(this.mBoundsSurfaceControl);
        }
    }

    private void setBoundsSurfaceCrop() {
        this.mTempBoundsRect.set(this.mWinFrame);
        this.mTempBoundsRect.offsetTo(this.mWindowAttributes.surfaceInsets.left, this.mWindowAttributes.surfaceInsets.top);
        this.mTransaction.setWindowCrop(this.mBoundsSurfaceControl, this.mTempBoundsRect);
    }

    private void updateBoundsSurface() {
        if (this.mBoundsSurfaceControl != null && this.mSurface.isValid()) {
            setBoundsSurfaceCrop();
            this.mBoundsSurfaceControl.reparent(this.mSurfaceControl);
            SurfaceControl.Transaction transaction = this.mTransaction;
            SurfaceControl surfaceControl = this.mBoundsSurfaceControl;
            Surface surface = this.mSurface;
            transaction.deferTransactionUntilSurface(surfaceControl, surface, surface.getNextFrameNumber()).apply();
        }
    }

    private void destroySurface() {
        this.mSurface.release();
        this.mSurfaceControl.release();
        this.mSurfaceSession = null;
        SurfaceControl surfaceControl = this.mBoundsSurfaceControl;
        if (surfaceControl != null) {
            surfaceControl.remove();
            this.mBoundsSurface.release();
            this.mBoundsSurfaceControl = null;
        }
    }

    public void setPausedForTransition(boolean paused) {
        this.mPausedForTransition = paused;
    }

    @Override // android.view.ViewParent
    public ViewParent getParent() {
        return null;
    }

    @Override // android.view.ViewParent
    public boolean getChildVisibleRect(View child, Rect r, Point offset) {
        if (child == this.mView) {
            return r.intersect(0, 0, this.mWidth, this.mHeight);
        }
        throw new RuntimeException("child is not mine, honest!");
    }

    @Override // android.view.ViewParent
    public void bringChildToFront(View child) {
    }

    /* access modifiers changed from: package-private */
    public int getHostVisibility() {
        View view;
        if ((this.mAppVisible || this.mForceDecorViewVisibility) && (view = this.mView) != null) {
            return view.getVisibility();
        }
        return 8;
    }

    public void requestTransitionStart(LayoutTransition transition) {
        ArrayList<LayoutTransition> arrayList = this.mPendingTransitions;
        if (arrayList == null || !arrayList.contains(transition)) {
            if (this.mPendingTransitions == null) {
                this.mPendingTransitions = new ArrayList<>();
            }
            this.mPendingTransitions.add(transition);
        }
    }

    /* access modifiers changed from: package-private */
    public void notifyRendererOfFramePending() {
        if (this.mAttachInfo.mThreadedRenderer != null) {
            this.mAttachInfo.mThreadedRenderer.notifyFramePending();
        }
    }

    /* access modifiers changed from: package-private */
    @UnsupportedAppUsage
    public void scheduleTraversals() {
        if (DEBUG_PANIC && this.isBuiltinApp) {
            checkThread();
        }
        Trace.traceBegin(8, "scheduleTraversals");
        if (this.mFirst && !this.mFirstFrameScheduled && scheduleTraversalsImmediately()) {
            this.mFirstFrameScheduled = true;
        } else if (!this.mTraversalScheduled) {
            this.mTraversalScheduled = true;
            this.mTraversalBarrier = this.mHandler.getLooper().getQueue().postSyncBarrier();
            this.mChoreographer.postCallback(3, this.mTraversalRunnable, null);
            if (!this.mUnbufferedInputDispatch) {
                scheduleConsumeBatchedInput();
            }
            notifyRendererOfFramePending();
            pokeDrawLockIfNeeded();
        }
        Trace.traceEnd(8);
    }

    private boolean scheduleTraversalsImmediately() {
        int i;
        if (!this.FRAME_ONT || !ActivityThread.sDoFrameOptEnabled || (i = this.doFrameIndex) > 1 || !this.isOptApp) {
            return false;
        }
        if (i == 1 && ActivityThread.sDoFrameOptEnabled) {
            ActivityThread.sDoFrameOptEnabled = false;
        }
        this.doFrameIndex++;
        Trace.traceBegin(8, "scheduleTraversalsImmediately");
        this.mTraversalScheduled = true;
        try {
            this.mHandler.getLooper().getQueue().removeSyncBarrier(this.mTraversalBarrier);
        } catch (IllegalStateException e) {
            Log.v(TAG, "The specified message queue synchronization  barrier token has not been posted or has already been removed");
        }
        this.mTraversalBarrier = this.mHandler.getLooper().getQueue().postSyncBarrier();
        this.mChoreographer.postCallbackImmediately(3, this.mTraversalRunnable, null, 0);
        this.mChoreographer.doFrameImmediately();
        if (!this.mUnbufferedInputDispatch) {
            scheduleConsumeBatchedInput();
        }
        notifyRendererOfFramePending();
        pokeDrawLockIfNeeded();
        Trace.traceEnd(8);
        return true;
    }

    /* access modifiers changed from: package-private */
    public void unscheduleTraversals() {
        if (this.mTraversalScheduled) {
            this.mTraversalScheduled = false;
            try {
                this.mHandler.getLooper().getQueue().removeSyncBarrier(this.mTraversalBarrier);
            } catch (IllegalStateException e) {
                Log.v(TAG, "The specified message queue synchronization  barrier token has not been posted or has already been removed");
            }
            this.mChoreographer.removeCallbacks(3, this.mTraversalRunnable, null);
        }
    }

    /* access modifiers changed from: package-private */
    public void doTraversal() {
        if (ViewDebugManager.DEBUG_LIFECYCLE || ViewDebugManager.DEBUG_ENG) {
            String str = this.mTag;
            Log.v(str, "doTraversal: mTraversalScheduled = " + this.mTraversalScheduled + " mFisrt = " + this.mFirst + ",mTraversalBarrier = " + this.mTraversalBarrier + ",this = " + this);
        }
        if (this.mTraversalScheduled) {
            this.mTraversalScheduled = false;
            try {
                this.mHandler.getLooper().getQueue().removeSyncBarrier(this.mTraversalBarrier);
            } catch (IllegalStateException e) {
                Log.v(TAG, "The specified message queue synchronization  barrier token has not been posted or has already been removed");
            }
            if (this.mProfile) {
                Debug.startMethodTracing("ViewAncestor");
            }
            if (ViewDebugManager.DEBUG_SCHEDULETRAVERSALS) {
                Trace.traceBegin(8, "doTraversal");
            }
            performTraversals();
            if (ViewDebugManager.DEBUG_SCHEDULETRAVERSALS) {
                Trace.traceEnd(8);
            }
            if (this.mProfile) {
                Debug.stopMethodTracing();
                this.mProfile = false;
            }
        }
    }

    private void applyKeepScreenOnFlag(WindowManager.LayoutParams params) {
        if (this.mAttachInfo.mKeepScreenOn) {
            params.flags |= 128;
        } else {
            params.flags = (params.flags & -129) | (this.mClientWindowLayoutFlags & 128);
        }
    }

    private boolean collectViewAttributes() {
        if (this.mAttachInfo.mRecomputeGlobalAttributes) {
            View.AttachInfo attachInfo = this.mAttachInfo;
            attachInfo.mRecomputeGlobalAttributes = false;
            boolean oldScreenOn = attachInfo.mKeepScreenOn;
            View.AttachInfo attachInfo2 = this.mAttachInfo;
            attachInfo2.mKeepScreenOn = false;
            attachInfo2.mSystemUiVisibility = 0;
            attachInfo2.mHasSystemUiListeners = false;
            this.mView.dispatchCollectViewAttributes(attachInfo2, 0);
            View.AttachInfo attachInfo3 = this.mAttachInfo;
            attachInfo3.mSystemUiVisibility = this.mManager.changeSystemUiVisibility(attachInfo3.mSystemUiVisibility);
            this.mAttachInfo.mSystemUiVisibility &= ~this.mAttachInfo.mDisabledSystemUiVisibility;
            WindowManager.LayoutParams params = this.mWindowAttributes;
            this.mAttachInfo.mSystemUiVisibility |= getImpliedSystemUiVisibility(params);
            if (!(this.mAttachInfo.mKeepScreenOn == oldScreenOn && this.mAttachInfo.mSystemUiVisibility == params.subtreeSystemUiVisibility && this.mAttachInfo.mHasSystemUiListeners == params.hasSystemUiListeners)) {
                applyKeepScreenOnFlag(params);
                params.subtreeSystemUiVisibility = this.mAttachInfo.mSystemUiVisibility;
                params.hasSystemUiListeners = this.mAttachInfo.mHasSystemUiListeners;
                this.mView.dispatchWindowSystemUiVisiblityChanged(this.mAttachInfo.mSystemUiVisibility);
                return true;
            }
        }
        return false;
    }

    private int getImpliedSystemUiVisibility(WindowManager.LayoutParams params) {
        int vis = 0;
        if ((params.flags & 67108864) != 0) {
            vis = 0 | 1280;
        }
        if ((params.flags & 134217728) != 0) {
            return vis | 768;
        }
        return vis;
    }

    /* JADX WARNING: Removed duplicated region for block: B:46:0x01b9  */
    private boolean measureHierarchy(View host, WindowManager.LayoutParams lp, Resources res, int desiredWindowWidth, int desiredWindowHeight) {
        boolean goodMeasure;
        boolean windowSizeMayChange;
        boolean windowSizeMayChange2;
        if (DEBUG_ORIENTATION || DEBUG_LAYOUT) {
            Log.v(this.mTag, "Measuring " + host + " in display " + desiredWindowWidth + "x" + desiredWindowHeight + Session.TRUNCATE_STRING);
        }
        if (lp.width == -2) {
            DisplayMetrics packageMetrics = res.getDisplayMetrics();
            res.getValue(R.dimen.config_prefDialogWidth, this.mTmpValue, true);
            int baseSize = 0;
            if (this.mTmpValue.type == 5) {
                baseSize = (int) this.mTmpValue.getDimension(packageMetrics);
            }
            if (DEBUG_DIALOG) {
                Log.v(this.mTag, "Window " + this.mView + ": baseSize=" + baseSize + ", desiredWindowWidth=" + desiredWindowWidth);
            }
            if (baseSize == 0 || desiredWindowWidth <= baseSize) {
                windowSizeMayChange = false;
                goodMeasure = false;
            } else {
                int childWidthMeasureSpec = getRootMeasureSpec(baseSize, lp.width);
                int childHeightMeasureSpec = getRootMeasureSpec(desiredWindowHeight, lp.height);
                performMeasure(childWidthMeasureSpec, childHeightMeasureSpec);
                windowSizeMayChange = false;
                if (DEBUG_DIALOG) {
                    goodMeasure = false;
                    Log.v(this.mTag, "Window " + this.mView + ": measured (" + host.getMeasuredWidth() + SmsManager.REGEX_PREFIX_DELIMITER + host.getMeasuredHeight() + ") from width spec: " + View.MeasureSpec.toString(childWidthMeasureSpec) + " and height spec: " + View.MeasureSpec.toString(childHeightMeasureSpec));
                } else {
                    goodMeasure = false;
                }
                if ((host.getMeasuredWidthAndState() & 16777216) == 0) {
                    goodMeasure = true;
                } else {
                    int baseSize2 = (baseSize + desiredWindowWidth) / 2;
                    if (DEBUG_DIALOG) {
                        Log.v(this.mTag, "Window " + this.mView + ": next baseSize=" + baseSize2);
                    }
                    performMeasure(getRootMeasureSpec(baseSize2, lp.width), childHeightMeasureSpec);
                    if (DEBUG_DIALOG) {
                        Log.v(this.mTag, "Window " + this.mView + ": measured (" + host.getMeasuredWidth() + SmsManager.REGEX_PREFIX_DELIMITER + host.getMeasuredHeight() + ")");
                    }
                    if ((host.getMeasuredWidthAndState() & 16777216) == 0) {
                        if (DEBUG_DIALOG) {
                            Log.v(this.mTag, "Good!");
                        }
                        goodMeasure = true;
                    }
                }
            }
        } else {
            windowSizeMayChange = false;
            goodMeasure = false;
        }
        if (!goodMeasure) {
            performMeasure(getRootMeasureSpec(desiredWindowWidth, lp.width), getRootMeasureSpec(desiredWindowHeight, lp.height));
            if (!(this.mWidth == host.getMeasuredWidth() && this.mHeight == host.getMeasuredHeight())) {
                windowSizeMayChange2 = true;
                if (DBG) {
                    System.out.println("======================================");
                    System.out.println("performTraversals -- after measure");
                    host.debug();
                }
                if (DEBUG_ORIENTATION || ViewDebugManager.DEBUG_LAYOUT) {
                    Log.v(this.mTag, "ViewRoot measure-: host measured size = (" + host.getMeasuredWidth() + "x" + host.getMeasuredHeight() + "), windowSizeMayChange = " + windowSizeMayChange2 + ", this = " + this);
                }
                return windowSizeMayChange2;
            }
        }
        windowSizeMayChange2 = windowSizeMayChange;
        if (DBG) {
        }
        Log.v(this.mTag, "ViewRoot measure-: host measured size = (" + host.getMeasuredWidth() + "x" + host.getMeasuredHeight() + "), windowSizeMayChange = " + windowSizeMayChange2 + ", this = " + this);
        return windowSizeMayChange2;
    }

    /* access modifiers changed from: package-private */
    public void transformMatrixToGlobal(Matrix m) {
        m.preTranslate((float) this.mAttachInfo.mWindowLeft, (float) this.mAttachInfo.mWindowTop);
    }

    /* access modifiers changed from: package-private */
    public void transformMatrixToLocal(Matrix m) {
        m.postTranslate((float) (-this.mAttachInfo.mWindowLeft), (float) (-this.mAttachInfo.mWindowTop));
    }

    /* access modifiers changed from: package-private */
    public WindowInsets getWindowInsets(boolean forceConstruct) {
        DisplayCutout displayCutout;
        if (this.mLastWindowInsets == null || forceConstruct) {
            this.mDispatchContentInsets.set(this.mAttachInfo.mContentInsets);
            this.mDispatchStableInsets.set(this.mAttachInfo.mStableInsets);
            this.mDispatchDisplayCutout = this.mAttachInfo.mDisplayCutout.get();
            Rect contentInsets = this.mDispatchContentInsets;
            Rect stableInsets = this.mDispatchStableInsets;
            DisplayCutout displayCutout2 = this.mDispatchDisplayCutout;
            if (forceConstruct || (this.mPendingContentInsets.equals(contentInsets) && this.mPendingStableInsets.equals(stableInsets) && this.mPendingDisplayCutout.get().equals(displayCutout2))) {
                displayCutout = displayCutout2;
            } else {
                contentInsets = this.mPendingContentInsets;
                stableInsets = this.mPendingStableInsets;
                displayCutout = this.mPendingDisplayCutout.get();
            }
            Rect outsets = this.mAttachInfo.mOutsets;
            if (outsets.left > 0 || outsets.top > 0 || outsets.right > 0 || outsets.bottom > 0) {
                contentInsets = new Rect(contentInsets.left + outsets.left, contentInsets.top + outsets.top, contentInsets.right + outsets.right, contentInsets.bottom + outsets.bottom);
            }
            this.mLastWindowInsets = this.mInsetsController.calculateInsets(this.mContext.getResources().getConfiguration().isScreenRound(), this.mAttachInfo.mAlwaysConsumeSystemBars, displayCutout, ensureInsetsNonNegative(contentInsets, "content"), ensureInsetsNonNegative(stableInsets, "stable"), this.mWindowAttributes.softInputMode);
        }
        return this.mLastWindowInsets;
    }

    private Rect ensureInsetsNonNegative(Rect insets, String kind) {
        if (insets.left >= 0 && insets.top >= 0 && insets.right >= 0 && insets.bottom >= 0) {
            return insets;
        }
        String str = this.mTag;
        Log.wtf(str, "Negative " + kind + "Insets: " + insets + ", mFirst=" + this.mFirst);
        return new Rect(Math.max(0, insets.left), Math.max(0, insets.top), Math.max(0, insets.right), Math.max(0, insets.bottom));
    }

    /* access modifiers changed from: package-private */
    public void dispatchApplyInsets(View host) {
        Trace.traceBegin(8, "dispatchApplyInsets");
        boolean dispatchCutout = true;
        WindowInsets insets = getWindowInsets(true);
        if (this.mWindowAttributes.layoutInDisplayCutoutMode != 1) {
            dispatchCutout = false;
        }
        if (!dispatchCutout) {
            insets = insets.consumeDisplayCutout();
        }
        host.dispatchApplyWindowInsets(insets);
        Trace.traceEnd(8);
    }

    /* access modifiers changed from: package-private */
    public InsetsController getInsetsController() {
        return this.mInsetsController;
    }

    private static boolean shouldUseDisplaySize(WindowManager.LayoutParams lp) {
        return lp.type == 2014 || lp.type == 2011 || lp.type == 2020;
    }

    private int dipToPx(int dip) {
        return (int) ((this.mContext.getResources().getDisplayMetrics().density * ((float) dip)) + 0.5f);
    }

    /* JADX DEBUG: Failed to insert an additional move for type inference into block B:343:0x0751 */
    /* JADX DEBUG: Failed to insert an additional move for type inference into block B:348:0x077d */
    /* JADX DEBUG: Failed to insert an additional move for type inference into block B:353:0x07a9 */
    /* JADX DEBUG: Failed to insert an additional move for type inference into block B:369:0x0816 */
    /* JADX DEBUG: Failed to insert an additional move for type inference into block B:373:0x083f */
    /* JADX DEBUG: Failed to insert an additional move for type inference into block B:390:0x0875 */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r2v86 */
    /* JADX WARN: Type inference failed for: r2v87 */
    /* JADX WARN: Type inference failed for: r2v88 */
    /* JADX WARN: Type inference failed for: r2v89 */
    /* JADX WARN: Type inference failed for: r2v97 */
    /* JADX WARN: Type inference failed for: r2v101 */
    /* JADX WARN: Type inference failed for: r2v102 */
    /* JADX WARN: Type inference failed for: r2v105 */
    /* JADX WARN: Type inference failed for: r1v174, types: [java.lang.StringBuilder] */
    /* JADX WARN: Type inference failed for: r2v108, types: [android.graphics.Rect, java.lang.Object] */
    /* JADX WARN: Type inference failed for: r2v109 */
    /* JADX WARN: Type inference failed for: r2v113 */
    /* JADX WARN: Type inference failed for: r1v187, types: [java.lang.StringBuilder] */
    /* JADX WARN: Type inference failed for: r2v116, types: [android.graphics.Rect, java.lang.Object] */
    /* JADX WARN: Type inference failed for: r1v190, types: [java.lang.StringBuilder] */
    /* JADX WARN: Type inference failed for: r2v120, types: [android.graphics.Rect, java.lang.Object] */
    /* JADX WARNING: Code restructure failed: missing block: B:284:0x05a4, code lost:
        r14 = relayoutWindow(r3, r13, r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:287:0x05a7, code lost:
        if (android.view.ViewRootImpl.DEBUG_LAYOUT == false) goto L_0x0651;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:289:?, code lost:
        android.util.Log.v(r48.mTag, "relayout: frame=" + r7.toShortString() + " overscan=" + r48.mPendingOverscanInsets.toShortString() + " content=" + r48.mPendingContentInsets.toShortString() + " visible=" + r48.mPendingVisibleInsets.toShortString() + " stable=" + r48.mPendingStableInsets.toShortString() + " cutout=" + r48.mPendingDisplayCutout.get().toString() + " outsets=" + r48.mPendingOutsets.toShortString() + " surface=" + r48.mSurface + " valid = " + r48.mSurface.isValid() + " surfaceGenerationId = " + r6 + " relayoutResult = " + r14);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:290:0x0645, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:291:0x0646, code lost:
        r44 = r1;
        r46 = r14;
        r14 = r6;
        r27 = r27;
        r29 = r29;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:295:0x0658, code lost:
        if (android.os.Trace.isTagEnabled(8) == false) goto L_0x066a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:297:?, code lost:
        android.os.Trace.traceEnd(8);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:298:0x065e, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:299:0x065f, code lost:
        r44 = r1;
        r46 = r14;
        r14 = r6;
        r27 = r27;
        r29 = r29;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:300:0x066a, code lost:
        r2 = r48.mPendingMergedConfiguration.equals(r48.mLastReportedMergedConfiguration);
        r2 = r2;
        r27 = r27;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:301:0x0672, code lost:
        if (r2 != false) goto L_0x06a4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:303:0x0676, code lost:
        if (android.view.ViewRootImpl.DEBUG_CONFIGURATION == false) goto L_0x0694;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:304:0x0678, code lost:
        android.util.Log.v(r48.mTag, "Visible with new config: " + r48.mPendingMergedConfiguration.getMergedConfiguration());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:305:0x0694, code lost:
        r0 = r48.mPendingMergedConfiguration;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:306:0x0698, code lost:
        if (r48.mFirst != false) goto L_0x069c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:307:0x069a, code lost:
        r1 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:308:0x069c, code lost:
        r1 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:309:0x069d, code lost:
        performConfigurationChange(r0, r1, -1);
        r2 = true;
        r27 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:311:0x06ae, code lost:
        if (r48.mPendingOverscanInsets.equals(r48.mAttachInfo.mOverscanInsets) != false) goto L_0x06b2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:312:0x06b0, code lost:
        r0 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:313:0x06b2, code lost:
        r0 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:315:0x06be, code lost:
        if (r48.mPendingContentInsets.equals(r48.mAttachInfo.mContentInsets) != false) goto L_0x06c2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:316:0x06c0, code lost:
        r0 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:317:0x06c2, code lost:
        r0 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:318:0x06c3, code lost:
        r23 = r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:319:0x06cf, code lost:
        if (r48.mPendingVisibleInsets.equals(r48.mAttachInfo.mVisibleInsets) != false) goto L_0x06d3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:320:0x06d1, code lost:
        r0 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:321:0x06d3, code lost:
        r0 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:323:0x06e0, code lost:
        if (r48.mPendingStableInsets.equals(r48.mAttachInfo.mStableInsets) != false) goto L_0x06e4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:324:0x06e2, code lost:
        r0 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:325:0x06e4, code lost:
        r0 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:327:0x06f1, code lost:
        if (r48.mPendingDisplayCutout.equals(r48.mAttachInfo.mDisplayCutout) != false) goto L_0x06f5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:328:0x06f3, code lost:
        r0 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:329:0x06f5, code lost:
        r0 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:330:0x06f6, code lost:
        r0 = !r48.mPendingOutsets.equals(r48.mAttachInfo.mOutsets);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:331:0x0708, code lost:
        if ((r14 & 32) == 0) goto L_0x070c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:332:0x070a, code lost:
        r0 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:333:0x070c, code lost:
        r0 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:334:0x070d, code lost:
        r25 = r0;
        r8 = r8 | r25;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:335:0x0717, code lost:
        if (r48.mPendingAlwaysConsumeSystemBars == r48.mAttachInfo.mAlwaysConsumeSystemBars) goto L_0x071b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:336:0x0719, code lost:
        r0 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:337:0x071b, code lost:
        r0 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:338:0x071c, code lost:
        r0 = hasColorModeChanged(r12.getColorMode());
        r2 = r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:339:0x0728, code lost:
        if (r23 == false) goto L_0x0751;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:340:0x072a, code lost:
        r48.mAttachInfo.mContentInsets.set(r48.mPendingContentInsets);
        r2 = r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:341:0x0735, code lost:
        if (android.view.ViewRootImpl.DEBUG_LAYOUT == false) goto L_0x0751;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:342:0x0737, code lost:
        r0 = r48.mTag;
        r1 = new java.lang.StringBuilder();
        r1.append("Content insets changing to: ");
        r2 = r48.mAttachInfo.mContentInsets;
        r1.append(r2);
        android.util.Log.v(r0, r1.toString());
        r2 = r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:343:0x0751, code lost:
        if (r0 == false) goto L_0x077d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:344:0x0753, code lost:
        r48.mAttachInfo.mOverscanInsets.set(r48.mPendingOverscanInsets);
        r2 = r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:345:0x075e, code lost:
        if (android.view.ViewRootImpl.DEBUG_LAYOUT == false) goto L_0x077a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:346:0x0760, code lost:
        r0 = r48.mTag;
        r1 = new java.lang.StringBuilder();
        r1.append("Overscan insets changing to: ");
        r2 = r48.mAttachInfo.mOverscanInsets;
        r1.append(r2);
        android.util.Log.v(r0, r1.toString());
        r2 = r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:347:0x077a, code lost:
        r23 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:348:0x077d, code lost:
        if (r0 == false) goto L_0x07a9;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:349:0x077f, code lost:
        r48.mAttachInfo.mStableInsets.set(r48.mPendingStableInsets);
        r2 = r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:350:0x078a, code lost:
        if (android.view.ViewRootImpl.DEBUG_LAYOUT == false) goto L_0x07a6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:351:0x078c, code lost:
        r0 = r48.mTag;
        r1 = new java.lang.StringBuilder();
        r1.append("Decor insets changing to: ");
        r2 = r48.mAttachInfo.mStableInsets;
        r1.append(r2);
        android.util.Log.v(r0, r1.toString());
        r2 = r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:352:0x07a6, code lost:
        r23 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:353:0x07a9, code lost:
        if (r0 == false) goto L_0x07d5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:354:0x07ab, code lost:
        r48.mAttachInfo.mDisplayCutout.set(r48.mPendingDisplayCutout);
        r2 = r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:355:0x07b6, code lost:
        if (android.view.ViewRootImpl.DEBUG_LAYOUT == false) goto L_0x07d2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:356:0x07b8, code lost:
        r0 = r48.mTag;
        r1 = new java.lang.StringBuilder();
        r1.append("DisplayCutout changing to: ");
        r2 = r48.mAttachInfo.mDisplayCutout;
        r1.append(r2);
        android.util.Log.v(r0, r1.toString());
        r2 = r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:357:0x07d2, code lost:
        r23 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:358:0x07d5, code lost:
        if (r0 == false) goto L_0x07e0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:359:0x07d7, code lost:
        r48.mAttachInfo.mAlwaysConsumeSystemBars = r48.mPendingAlwaysConsumeSystemBars;
        r23 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:360:0x07e0, code lost:
        if (r23 != false) goto L_0x07f8;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:362:0x07e8, code lost:
        if (r48.mLastSystemUiVisibility != r48.mAttachInfo.mSystemUiVisibility) goto L_0x07f8;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:364:0x07ec, code lost:
        if (r48.mApplyInsetsRequested != false) goto L_0x07f8;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:366:0x07f4, code lost:
        if (r48.mLastOverscanRequested != r48.mAttachInfo.mOverscanRequested) goto L_0x07f8;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:367:0x07f6, code lost:
        if (r0 == false) goto L_0x0816;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:368:0x07f8, code lost:
        r48.mLastSystemUiVisibility = r48.mAttachInfo.mSystemUiVisibility;
        r48.mLastOverscanRequested = r48.mAttachInfo.mOverscanRequested;
        r48.mAttachInfo.mOutsets.set(r48.mPendingOutsets);
        r48.mApplyInsetsRequested = false;
        dispatchApplyInsets(r10);
        r23 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:369:0x0816, code lost:
        if (r0 == false) goto L_0x083f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:370:0x0818, code lost:
        r48.mAttachInfo.mVisibleInsets.set(r48.mPendingVisibleInsets);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:371:0x0823, code lost:
        if (android.view.ViewRootImpl.DEBUG_LAYOUT == false) goto L_0x083f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:372:0x0825, code lost:
        r0 = r48.mTag;
        r1 = new java.lang.StringBuilder();
        r1.append("Visible insets changing to: ");
        r2 = r48.mAttachInfo.mVisibleInsets;
        r1.append(r2);
        android.util.Log.v(r0, r1.toString());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:373:0x083f, code lost:
        if (r0 == false) goto L_0x0858;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:375:0x0845, code lost:
        if (r48.mAttachInfo.mThreadedRenderer == null) goto L_0x0858;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:376:0x0847, code lost:
        r0 = r48.mAttachInfo.mThreadedRenderer;
        r2 = 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:377:0x0850, code lost:
        if (r12.getColorMode() != 1) goto L_0x0854;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:378:0x0852, code lost:
        r1 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:379:0x0854, code lost:
        r1 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:380:0x0855, code lost:
        r0.setWideGamut(r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:381:0x0858, code lost:
        if (r11 != false) goto L_0x08ef;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:384:0x0860, code lost:
        if (r48.mSurface.isValid() == false) goto L_0x08df;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:385:0x0862, code lost:
        r48.mFullRedrawNeeded = true;
        r48.mPreviousTransparentRegion.setEmpty();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:386:0x086e, code lost:
        if (r48.mAttachInfo.mThreadedRenderer == null) goto L_0x08db;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:387:0x0870, code lost:
        r1 = r48.mGraphicLock;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:388:0x0872, code lost:
        monitor-enter(r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:392:0x0879, code lost:
        if (android.os.Trace.isTagEnabled(8) == false) goto L_0x0885;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:394:0x087d, code lost:
        r2 = 8;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:396:?, code lost:
        android.os.Trace.traceBegin(8, "HW init");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:399:0x0891, code lost:
        r2 = r48.mAttachInfo.mThreadedRenderer.initialize(r48.mSurface);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:400:0x0892, code lost:
        if (r2 == 0) goto L_0x08a1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:403:0x0898, code lost:
        if ((r10.mPrivateFlags & 512) != 0) goto L_0x08a1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:404:0x089a, code lost:
        r48.mAttachInfo.mThreadedRenderer.allocateBuffers();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:406:0x08a7, code lost:
        if (android.os.Trace.isTagEnabled(8) == false) goto L_0x08ad;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:407:0x08a9, code lost:
        android.os.Trace.traceEnd(8);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:409:?, code lost:
        monitor-exit(r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:410:0x08ae, code lost:
        r29 = r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:411:0x08b2, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:413:0x08b4, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:414:0x08b5, code lost:
        r2 = r29;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:415:0x08b8, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:417:0x08bc, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:418:0x08bd, code lost:
        r2 = r29;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:419:0x08c2, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:421:0x08c7, code lost:
        handleOutOfResourcesException(r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:422:0x08ca, code lost:
        monitor-exit(r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:423:0x08cb, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:424:0x08cc, code lost:
        r0 = th;
        r2 = r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:425:0x08cd, code lost:
        monitor-exit(r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:427:?, code lost:
        throw r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:428:0x08cf, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:429:0x08d0, code lost:
        r2 = r2 ? 1 : 0;
        r2 = r2 ? 1 : 0;
        r29 = r2;
        r44 = r1;
        r46 = r14;
        r14 = r6;
        r27 = r27;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:430:0x08db, code lost:
        r29 = r29;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:431:0x08df, code lost:
        r29 = r29;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:432:0x08e3, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:433:0x08e4, code lost:
        r44 = r1;
        r46 = r14;
        r14 = r6;
        r27 = r27;
        r29 = r29;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:437:0x08f7, code lost:
        if (r48.mSurface.isValid() != false) goto L_0x0941;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:440:0x08fb, code lost:
        if (r48.mLastScrolledFocus == null) goto L_0x0902;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:441:0x08fd, code lost:
        r48.mLastScrolledFocus.clear();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:442:0x0902, code lost:
        r48.mCurScrollY = 0;
        r48.mScrollY = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:443:0x090b, code lost:
        if ((r48.mView instanceof com.android.internal.view.RootViewSurfaceTaker) == false) goto L_0x0916;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:444:0x090d, code lost:
        ((com.android.internal.view.RootViewSurfaceTaker) r48.mView).onRootViewScrollYChanged(r48.mCurScrollY);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:446:0x0918, code lost:
        if (r48.mScroller == null) goto L_0x091f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:447:0x091a, code lost:
        r48.mScroller.abortAnimation();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:448:0x091f, code lost:
        r29 = r29;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:449:0x0923, code lost:
        if (r48.mAttachInfo.mThreadedRenderer == null) goto L_0x096b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:450:0x0925, code lost:
        r29 = r29;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:451:0x092d, code lost:
        if (r48.mAttachInfo.mThreadedRenderer.isEnabled() == false) goto L_0x096b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:452:0x092f, code lost:
        r48.mAttachInfo.mThreadedRenderer.destroy();
        r29 = r29;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:453:0x0937, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:454:0x0938, code lost:
        r44 = r1;
        r46 = r14;
        r14 = r6;
        r27 = r27;
        r29 = r29;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:455:0x0941, code lost:
        r29 = r29;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:456:0x0947, code lost:
        if (r6 != r48.mSurface.getGenerationId()) goto L_0x094f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:457:0x0949, code lost:
        if (r25 != false) goto L_0x094f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:458:0x094b, code lost:
        if (r4 != false) goto L_0x094f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:459:0x094d, code lost:
        if (r0 == false) goto L_0x096b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:460:0x094f, code lost:
        r29 = r29;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:461:0x0951, code lost:
        if (r48.mSurfaceHolder != null) goto L_0x096b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:462:0x0953, code lost:
        r29 = r29;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:463:0x0957, code lost:
        if (r48.mAttachInfo.mThreadedRenderer == null) goto L_0x096b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:464:0x0959, code lost:
        r48.mFullRedrawNeeded = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:466:?, code lost:
        r48.mAttachInfo.mThreadedRenderer.updateSurface(r48.mSurface);
        r29 = r29;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:467:0x0966, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:468:0x0967, code lost:
        handleOutOfResourcesException(r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:469:0x096a, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:471:0x096d, code lost:
        if ((r14 & 16) == 0) goto L_0x0971;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:472:0x096f, code lost:
        r0 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:473:0x0971, code lost:
        r0 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:475:0x0974, code lost:
        if ((r14 & 8) == 0) goto L_0x0978;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:476:0x0976, code lost:
        r1 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:477:0x0978, code lost:
        r1 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:479:0x097b, code lost:
        if (r0 != false) goto L_0x0982;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:480:0x097d, code lost:
        if (r1 == false) goto L_0x0980;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:481:0x0980, code lost:
        r1 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:482:0x0982, code lost:
        r1 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:484:0x0986, code lost:
        if (r48.mDragResizing == r1) goto L_0x09f4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:485:0x0988, code lost:
        if (r1 == false) goto L_0x09e5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:486:0x098a, code lost:
        if (r0 == false) goto L_0x098e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:487:0x098c, code lost:
        r1 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:488:0x098e, code lost:
        r1 = 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:489:0x098f, code lost:
        r48.mResizeMode = r1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:490:0x099d, code lost:
        if (r48.mWinFrame.width() != r48.mPendingBackDropFrame.width()) goto L_0x09af;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:492:0x09ab, code lost:
        if (r48.mWinFrame.height() != r48.mPendingBackDropFrame.height()) goto L_0x09af;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:493:0x09ad, code lost:
        r1 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:494:0x09af, code lost:
        r1 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:495:0x09b0, code lost:
        r2 = r48.mPendingBackDropFrame;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:496:0x09b4, code lost:
        if (r1 != false) goto L_0x09b9;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:497:0x09b6, code lost:
        r40 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:498:0x09b9, code lost:
        r40 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:502:0x09c5, code lost:
        r44 = r1;
        r45 = r1;
        r46 = r14;
        r14 = r6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:504:?, code lost:
        startDragResizing(r2, r40, r48.mPendingVisibleInsets, r48.mPendingStableInsets, r48.mResizeMode);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:505:0x09da, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:506:0x09db, code lost:
        r44 = r1;
        r46 = r14;
        r14 = r6;
        r27 = r27;
        r29 = r29;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:507:0x09e5, code lost:
        r45 = r1;
        r44 = r1;
        r46 = r14;
        r14 = r6;
        endDragResizing();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:508:0x09f4, code lost:
        r45 = r1;
        r44 = r1;
        r46 = r14;
        r14 = r6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:510:0x0a01, code lost:
        if (r48.mUseMTRenderer != false) goto L_0x0a17;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:511:0x0a03, code lost:
        if (r45 == false) goto L_0x0a12;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:512:0x0a05, code lost:
        r48.mCanvasOffsetX = r48.mWinFrame.left;
        r48.mCanvasOffsetY = r48.mWinFrame.top;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:513:0x0a12, code lost:
        r48.mCanvasOffsetY = 0;
        r48.mCanvasOffsetX = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:514:0x0a17, code lost:
        r1 = r23;
        r3 = r25;
        r2 = r27;
        r29 = r29;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:515:0x0a1f, code lost:
        r0 = e;
        r27 = r27;
        r29 = r29;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:516:0x0a22, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:517:0x0a23, code lost:
        r44 = r1;
        r46 = r14;
        r14 = r6;
        r27 = r27;
        r29 = r29;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:518:0x0a2c, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:519:0x0a2d, code lost:
        r44 = r1;
        r46 = r14;
        r14 = r6;
        r27 = r27;
        r29 = r29;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:520:0x0a38, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:521:0x0a39, code lost:
        r44 = r1;
        r46 = r14;
        r14 = r6;
        r27 = r27;
        r29 = r29;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:522:0x0a43, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:523:0x0a44, code lost:
        r44 = r1;
        r14 = r6;
        r46 = r26;
        r27 = r27;
        r29 = r29;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:532:0x0a65, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:533:0x0a66, code lost:
        r46 = r26;
        r27 = r27;
        r29 = r29;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:553:0x0b02, code lost:
        if (r48.mSurface.isValid() != false) goto L_0x0b04;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:554:0x0b04, code lost:
        r48.mSurfaceHolder.mSurface = r48.mSurface;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:555:0x0b0a, code lost:
        r48.mSurfaceHolder.setSurfaceFrameSize(r48.mWidth, r48.mHeight);
        r48.mSurfaceHolder.mSurfaceLock.unlock();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:556:0x0b20, code lost:
        if (r48.mSurface.isValid() != false) goto L_0x0b22;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:557:0x0b22, code lost:
        if (r11 == false) goto L_0x0b24;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:558:0x0b24, code lost:
        r48.mSurfaceHolder.ungetCallbacks();
        r48.mIsCreating = true;
        r0 = r48.mSurfaceHolder.getCallbacks();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:559:0x0b32, code lost:
        if (r0 != null) goto L_0x0b34;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:560:0x0b34, code lost:
        r4 = r0.length;
        r5 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:561:0x0b36, code lost:
        if (r5 < r4) goto L_0x0b38;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:562:0x0b38, code lost:
        r0[r5].surfaceCreated(r48.mSurfaceHolder);
        r5 = r5 + 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:563:0x0b42, code lost:
        r8 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:564:0x0b43, code lost:
        if (r8 != false) goto L_0x0b51;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:567:0x0b4e, code lost:
        r25 = r3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:568:0x0b51, code lost:
        r0 = r48.mSurfaceHolder.getCallbacks();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:569:0x0b57, code lost:
        if (r0 != null) goto L_0x0b59;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:570:0x0b59, code lost:
        r4 = r0.length;
        r5 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:571:0x0b5b, code lost:
        if (r5 < r4) goto L_0x0b5d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:572:0x0b5d, code lost:
        r0[r5].surfaceChanged(r48.mSurfaceHolder, r12.format, r48.mWidth, r48.mHeight);
        r5 = r5 + 1;
        r0 = r0;
        r3 = r3;
        r4 = r4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:573:0x0b79, code lost:
        r25 = r3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:574:0x0b7e, code lost:
        r25 = r3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:575:0x0b82, code lost:
        r48.mIsCreating = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:576:0x0b86, code lost:
        r25 = r3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:577:0x0b88, code lost:
        if (r11 != false) goto L_0x0b8a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:578:0x0b8a, code lost:
        notifySurfaceDestroyed();
        r48.mSurfaceHolder.mSurfaceLock.lock();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:580:?, code lost:
        r48.mSurfaceHolder.mSurface = new android.view.Surface();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:582:0x0ba5, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:583:0x0ba6, code lost:
        r48.mSurfaceHolder.mSurfaceLock.unlock();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:584:0x0bad, code lost:
        throw r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:585:0x0bae, code lost:
        r25 = r3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:604:0x0bee, code lost:
        r3 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:605:0x0bf0, code lost:
        r3 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:617:0x0c24, code lost:
        android.util.Log.v(r48.mTag, "Ooops, something changed!  mWidth=" + r48.mWidth + " measuredWidth=" + r10.getMeasuredWidth() + " mHeight=" + r48.mHeight + " measuredHeight=" + r10.getMeasuredHeight() + " coveredInsetsChanged=" + r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:621:0x0c83, code lost:
        r0 = r0 + ((int) (((float) (r48.mWidth - r0)) * r12.horizontalWeight));
        r4 = android.view.View.MeasureSpec.makeMeasureSpec(r0, 1073741824);
        r15 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:624:0x0c99, code lost:
        r6 = r6 + ((int) (((float) (r48.mHeight - r6)) * r12.verticalWeight));
        r5 = android.view.View.MeasureSpec.makeMeasureSpec(r6, 1073741824);
        r15 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:627:0x0cad, code lost:
        if (android.view.ViewRootImpl.DEBUG_LAYOUT != false) goto L_0x0caf;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:628:0x0caf, code lost:
        android.util.Log.v(r48.mTag, "And hey let's measure once more: width=" + r0 + " height=" + r6);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:630:0x0cd2, code lost:
        performMeasure(r4, r5);
     */
    /* JADX WARNING: Removed duplicated region for block: B:125:0x02b9  */
    /* JADX WARNING: Removed duplicated region for block: B:128:0x02c7  */
    /* JADX WARNING: Removed duplicated region for block: B:131:0x02cf  */
    /* JADX WARNING: Removed duplicated region for block: B:138:0x02eb  */
    /* JADX WARNING: Removed duplicated region for block: B:151:0x0323  */
    /* JADX WARNING: Removed duplicated region for block: B:163:0x0346  */
    /* JADX WARNING: Removed duplicated region for block: B:167:0x0377  */
    /* JADX WARNING: Removed duplicated region for block: B:170:0x037f  */
    /* JADX WARNING: Removed duplicated region for block: B:172:0x0384 A[ADDED_TO_REGION] */
    /* JADX WARNING: Removed duplicated region for block: B:193:0x03c1  */
    /* JADX WARNING: Removed duplicated region for block: B:199:0x03d7  */
    /* JADX WARNING: Removed duplicated region for block: B:205:0x03ee  */
    /* JADX WARNING: Removed duplicated region for block: B:206:0x03f0  */
    /* JADX WARNING: Removed duplicated region for block: B:209:0x03f9  */
    /* JADX WARNING: Removed duplicated region for block: B:210:0x0452  */
    /* JADX WARNING: Removed duplicated region for block: B:213:0x0464 A[ADDED_TO_REGION] */
    /* JADX WARNING: Removed duplicated region for block: B:224:0x0499  */
    /* JADX WARNING: Removed duplicated region for block: B:231:0x04a6  */
    /* JADX WARNING: Removed duplicated region for block: B:234:0x04ac  */
    /* JADX WARNING: Removed duplicated region for block: B:235:0x04b7  */
    /* JADX WARNING: Removed duplicated region for block: B:240:0x04c8 A[SYNTHETIC, Splitter:B:240:0x04c8] */
    /* JADX WARNING: Removed duplicated region for block: B:254:0x0543  */
    /* JADX WARNING: Removed duplicated region for block: B:258:0x054a A[SYNTHETIC, Splitter:B:258:0x054a] */
    /* JADX WARNING: Removed duplicated region for block: B:31:0x0074  */
    /* JADX WARNING: Removed duplicated region for block: B:32:0x007a  */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x008d  */
    /* JADX WARNING: Removed duplicated region for block: B:40:0x00a8  */
    /* JADX WARNING: Removed duplicated region for block: B:43:0x00b2  */
    /* JADX WARNING: Removed duplicated region for block: B:51:0x0110  */
    /* JADX WARNING: Removed duplicated region for block: B:552:0x0afc  */
    /* JADX WARNING: Removed duplicated region for block: B:585:0x0bae  */
    /* JADX WARNING: Removed duplicated region for block: B:604:0x0bee  */
    /* JADX WARNING: Removed duplicated region for block: B:605:0x0bf0  */
    /* JADX WARNING: Removed duplicated region for block: B:60:0x0149  */
    /* JADX WARNING: Removed duplicated region for block: B:617:0x0c24  */
    /* JADX WARNING: Removed duplicated region for block: B:618:0x0c69  */
    /* JADX WARNING: Removed duplicated region for block: B:621:0x0c83  */
    /* JADX WARNING: Removed duplicated region for block: B:624:0x0c99  */
    /* JADX WARNING: Removed duplicated region for block: B:626:0x0cab  */
    /* JADX WARNING: Removed duplicated region for block: B:631:0x0cd6  */
    /* JADX WARNING: Removed duplicated region for block: B:635:0x0ce0  */
    /* JADX WARNING: Removed duplicated region for block: B:637:0x0ce5  */
    /* JADX WARNING: Removed duplicated region for block: B:645:0x0cf4  */
    /* JADX WARNING: Removed duplicated region for block: B:651:0x0d04  */
    /* JADX WARNING: Removed duplicated region for block: B:653:0x0d4e  */
    /* JADX WARNING: Removed duplicated region for block: B:674:0x0e0e  */
    /* JADX WARNING: Removed duplicated region for block: B:676:0x0e1a  */
    /* JADX WARNING: Removed duplicated region for block: B:694:0x0ec3  */
    /* JADX WARNING: Removed duplicated region for block: B:703:0x0ee7  */
    /* JADX WARNING: Removed duplicated region for block: B:706:0x0f07  */
    /* JADX WARNING: Removed duplicated region for block: B:729:0x0f93  */
    /* JADX WARNING: Removed duplicated region for block: B:736:0x0fa2 A[ADDED_TO_REGION] */
    /* JADX WARNING: Removed duplicated region for block: B:741:0x0faa  */
    /* JADX WARNING: Removed duplicated region for block: B:747:0x0fb5  */
    /* JADX WARNING: Removed duplicated region for block: B:748:0x0fb9 A[ADDED_TO_REGION] */
    /* JADX WARNING: Removed duplicated region for block: B:756:0x0fca  */
    /* JADX WARNING: Removed duplicated region for block: B:757:0x0fcc  */
    /* JADX WARNING: Removed duplicated region for block: B:75:0x0172  */
    /* JADX WARNING: Removed duplicated region for block: B:762:0x0fd8  */
    /* JADX WARNING: Removed duplicated region for block: B:765:0x0fea  */
    /* JADX WARNING: Removed duplicated region for block: B:778:0x1039  */
    /* JADX WARNING: Removed duplicated region for block: B:785:0x1051  */
    /* JADX WARNING: Removed duplicated region for block: B:795:0x107b  */
    /* JADX WARNING: Removed duplicated region for block: B:815:0x10c2  */
    /* JADX WARNING: Removed duplicated region for block: B:822:0x10e0  */
    /* JADX WARNING: Removed duplicated region for block: B:823:0x10e2  */
    /* JADX WARNING: Removed duplicated region for block: B:86:0x0195  */
    /* JADX WARNING: Unknown variable types count: 5 */
    private void performTraversals() {
        boolean viewUserVisibilityChanged;
        boolean surfaceChanged;
        boolean supportsScreen;
        boolean z;
        WindowManager.LayoutParams params;
        int desiredWindowWidth;
        int desiredWindowHeight;
        boolean layoutRequested;
        Rect frame;
        int i;
        int desiredWindowHeight2;
        int desiredWindowWidth2;
        boolean insetsChanged;
        WindowManager.LayoutParams params2;
        boolean insetsChanged2;
        WindowManager.LayoutParams params3;
        boolean windowSizeMayChange;
        boolean computesInternalInsets;
        int surfaceGenerationId;
        boolean isViewVisible;
        boolean updatedConfiguration;
        int relayoutResult;
        boolean insetsChanged3;
        int i2;
        boolean insetsPending;
        WindowManager.LayoutParams params4;
        boolean insetsChanged4;
        int relayoutResult2;
        boolean insetsPending2;
        boolean viewVisibilityChanged;
        boolean isViewVisible2;
        boolean layoutRequested2;
        Rect frame2;
        boolean didLayout;
        boolean triggerGlobalLayoutListener;
        boolean hasWindowFocus;
        boolean regainedFocus;
        boolean cancelDraw;
        boolean z2;
        WindowManager.LayoutParams layoutParams;
        boolean isToast;
        View view;
        Region touchableRegion;
        Rect visibleInsets;
        Rect visibleInsets2;
        boolean insetsPending3;
        BaseSurfaceHolder baseSurfaceHolder;
        boolean contentInsetsChanged;
        int relayoutResult3;
        boolean measureAgain;
        boolean hwInitialized;
        boolean updatedConfiguration2;
        RemoteException e;
        boolean hwInitialized2;
        Throwable th;
        int resizeMode;
        boolean insetsChanged5;
        int desiredWindowHeight3;
        int desiredWindowWidth3;
        View host = this.mView;
        if (host != null && this.mAdded) {
            if (DBG) {
                System.out.println("======================================");
                System.out.println("performTraversals");
                host.debug();
            }
            ((IColorBurmeseZgHooks) ColorFrameworkFactory.getInstance().getFeature(IColorBurmeseZgHooks.DEFAULT, new Object[0])).initBurmeseZgFlag(this.mContext);
            this.mIsInTraversal = true;
            this.mWillDrawSoon = true;
            boolean windowSizeMayChange2 = false;
            WindowManager.LayoutParams lp = this.mWindowAttributes;
            int viewVisibility = getHostVisibility();
            boolean viewVisibilityChanged2 = !this.mFirst && (this.mViewVisibility != viewVisibility || this.mNewSurfaceNeeded || this.mAppVisibilityChanged);
            this.mAppVisibilityChanged = false;
            if (!this.mFirst) {
                if ((this.mViewVisibility == 0) != (viewVisibility == 0)) {
                    viewUserVisibilityChanged = true;
                    WindowManager.LayoutParams params5 = null;
                    if (!this.mWindowAttributesChanged) {
                        this.mWindowAttributesChanged = false;
                        params5 = lp;
                        surfaceChanged = true;
                    } else {
                        surfaceChanged = false;
                    }
                    supportsScreen = this.mDisplay.getDisplayAdjustments().getCompatibilityInfo().supportsScreen();
                    z = this.mLastInCompatMode;
                    if (supportsScreen != z) {
                        this.mFullRedrawNeeded = true;
                        this.mLayoutRequested = true;
                        if (z) {
                            lp.privateFlags &= -129;
                            this.mLastInCompatMode = false;
                        } else {
                            lp.privateFlags |= 128;
                            this.mLastInCompatMode = true;
                        }
                        params = lp;
                    } else {
                        params = params5;
                    }
                    this.mWindowAttributesChangesFlag = 0;
                    Rect frame3 = this.mWinFrame;
                    if (!this.mFirst) {
                        this.mFullRedrawNeeded = true;
                        this.mLayoutRequested = true;
                        Configuration config = this.mContext.getResources().getConfiguration();
                        if (shouldUseDisplaySize(lp)) {
                            Point size = new Point();
                            this.mDisplay.getRealSize(size);
                            desiredWindowWidth = size.x;
                            desiredWindowHeight = size.y;
                        } else {
                            desiredWindowWidth = this.mWinFrame.width();
                            desiredWindowHeight = this.mWinFrame.height();
                        }
                        View.AttachInfo attachInfo = this.mAttachInfo;
                        attachInfo.mUse32BitDrawingCache = true;
                        attachInfo.mWindowVisibility = viewVisibility;
                        attachInfo.mRecomputeGlobalAttributes = false;
                        this.mLastConfigurationFromResources.setTo(config);
                        this.mLastSystemUiVisibility = this.mAttachInfo.mSystemUiVisibility;
                        if (this.mViewLayoutDirectionInitial == 2) {
                            host.setLayoutDirection(config.getLayoutDirection());
                        }
                        host.dispatchAttachedToWindow(this.mAttachInfo, 0);
                        this.mAttachInfo.mTreeObserver.dispatchOnWindowAttachedChange(true);
                        dispatchApplyInsets(host);
                    } else {
                        desiredWindowWidth = frame3.width();
                        desiredWindowHeight = frame3.height();
                        if (!(desiredWindowWidth == this.mWidth && desiredWindowHeight == this.mHeight)) {
                            if (DEBUG_ORIENTATION) {
                                Log.v(this.mTag, "View " + host + " resized to: " + frame3);
                            }
                            this.mFullRedrawNeeded = true;
                            this.mLayoutRequested = true;
                            windowSizeMayChange2 = true;
                        }
                    }
                    if (viewVisibilityChanged2) {
                        this.mAttachInfo.mWindowVisibility = viewVisibility;
                        host.dispatchWindowVisibilityChanged(viewVisibility);
                        if (viewUserVisibilityChanged) {
                            host.dispatchVisibilityAggregated(viewVisibility == 0);
                        }
                        if (viewVisibility != 0 || this.mNewSurfaceNeeded) {
                            endDragResizing();
                            destroyHardwareResources();
                        }
                        if (viewVisibility == 8) {
                            this.mHasHadWindowFocus = false;
                        }
                    }
                    if (this.mAttachInfo.mWindowVisibility != 0) {
                        host.clearAccessibilityFocus();
                    }
                    getRunQueue().executeActions(this.mAttachInfo.mHandler);
                    boolean insetsChanged6 = false;
                    layoutRequested = !this.mLayoutRequested && (!this.mStopped || this.mReportNextDraw);
                    if (!layoutRequested) {
                        Resources res = this.mView.getContext().getResources();
                        if (this.mFirst) {
                            View.AttachInfo attachInfo2 = this.mAttachInfo;
                            boolean z3 = this.mAddedTouchMode;
                            attachInfo2.mInTouchMode = !z3;
                            ensureTouchModeLocally(z3);
                            insetsChanged5 = false;
                            desiredWindowHeight3 = desiredWindowHeight;
                            desiredWindowWidth3 = desiredWindowWidth;
                        } else {
                            if (!this.mPendingOverscanInsets.equals(this.mAttachInfo.mOverscanInsets)) {
                                insetsChanged6 = true;
                            }
                            if (!this.mPendingContentInsets.equals(this.mAttachInfo.mContentInsets)) {
                                if (DEBUG_LAYOUT) {
                                    Log.v(this.mTag, "Content insets changing from " + this.mPendingContentInsets + " to: " + this.mAttachInfo.mContentInsets);
                                }
                                insetsChanged6 = true;
                            }
                            if (!this.mPendingStableInsets.equals(this.mAttachInfo.mStableInsets)) {
                                insetsChanged6 = true;
                            }
                            if (!this.mPendingDisplayCutout.equals(this.mAttachInfo.mDisplayCutout)) {
                                insetsChanged6 = true;
                            }
                            if (!this.mPendingVisibleInsets.equals(this.mAttachInfo.mVisibleInsets)) {
                                this.mAttachInfo.mVisibleInsets.set(this.mPendingVisibleInsets);
                                if (DEBUG_LAYOUT) {
                                    Log.v(this.mTag, "Visible insets changing to: " + this.mAttachInfo.mVisibleInsets + ", mWinFrame = " + this.mWinFrame);
                                }
                            }
                            if (!this.mPendingOutsets.equals(this.mAttachInfo.mOutsets)) {
                                insetsChanged6 = true;
                            }
                            if (this.mPendingAlwaysConsumeSystemBars != this.mAttachInfo.mAlwaysConsumeSystemBars) {
                                insetsChanged6 = true;
                            }
                            if (lp.width == -2 || lp.height == -2) {
                                windowSizeMayChange2 = true;
                                if (shouldUseDisplaySize(lp)) {
                                    Point size2 = new Point();
                                    this.mDisplay.getRealSize(size2);
                                    int desiredWindowWidth4 = size2.x;
                                    insetsChanged5 = insetsChanged6;
                                    desiredWindowHeight3 = size2.y;
                                    desiredWindowWidth3 = desiredWindowWidth4;
                                } else {
                                    Configuration config2 = res.getConfiguration();
                                    int desiredWindowWidth5 = dipToPx(config2.screenWidthDp);
                                    insetsChanged5 = insetsChanged6;
                                    desiredWindowHeight3 = dipToPx(config2.screenHeightDp);
                                    desiredWindowWidth3 = desiredWindowWidth5;
                                }
                            } else {
                                insetsChanged5 = insetsChanged6;
                                desiredWindowHeight3 = desiredWindowHeight;
                                desiredWindowWidth3 = desiredWindowWidth;
                            }
                        }
                        i = -2;
                        frame = frame3;
                        windowSizeMayChange2 |= measureHierarchy(host, lp, res, desiredWindowWidth3, desiredWindowHeight3);
                        insetsChanged = insetsChanged5;
                        desiredWindowHeight2 = desiredWindowWidth3;
                        desiredWindowWidth2 = desiredWindowHeight3;
                    } else {
                        frame = frame3;
                        i = -2;
                        insetsChanged = false;
                        desiredWindowWidth2 = desiredWindowHeight;
                        desiredWindowHeight2 = desiredWindowWidth;
                    }
                    if (collectViewAttributes()) {
                        params = lp;
                    }
                    if (this.mAttachInfo.mForceReportNewAttributes) {
                        this.mAttachInfo.mForceReportNewAttributes = false;
                        params = lp;
                    }
                    if (this.mFirst || this.mAttachInfo.mViewVisibilityChanged) {
                        View.AttachInfo attachInfo3 = this.mAttachInfo;
                        attachInfo3.mViewVisibilityChanged = false;
                        resizeMode = this.mSoftInputMode & 240;
                        if (resizeMode == 0) {
                            int N = attachInfo3.mScrollContainers.size();
                            for (int i3 = 0; i3 < N; i3++) {
                                if (this.mAttachInfo.mScrollContainers.get(i3).isShown()) {
                                    resizeMode = 16;
                                }
                            }
                            if (resizeMode == 0) {
                                resizeMode = 32;
                            }
                            if ((lp.softInputMode & 240) != resizeMode) {
                                lp.softInputMode = (lp.softInputMode & TrafficStats.TAG_SYSTEM_IMPERSONATION_RANGE_END) | resizeMode;
                                params2 = lp;
                                if (params2 != null) {
                                    if ((host.mPrivateFlags & 512) != 0 && !PixelFormat.formatHasAlpha(params2.format)) {
                                        params2.format = -3;
                                    }
                                    this.mAttachInfo.mOverscanRequested = (params2.flags & 33554432) != 0;
                                }
                                if (this.mApplyInsetsRequested) {
                                    this.mApplyInsetsRequested = false;
                                    this.mLastOverscanRequested = this.mAttachInfo.mOverscanRequested;
                                    dispatchApplyInsets(host);
                                    if (this.mLayoutRequested) {
                                        params3 = params2;
                                        insetsChanged2 = insetsChanged;
                                        windowSizeMayChange = windowSizeMayChange2 | measureHierarchy(host, lp, this.mView.getContext().getResources(), desiredWindowHeight2, desiredWindowWidth2);
                                        if (layoutRequested) {
                                            this.mLayoutRequested = false;
                                        }
                                        boolean windowShouldResize = (!layoutRequested && windowSizeMayChange && !(this.mWidth == host.getMeasuredWidth() && this.mHeight == host.getMeasuredHeight() && ((lp.width != i || frame.width() >= desiredWindowHeight2 || frame.width() == this.mWidth) && (lp.height != i || frame.height() >= desiredWindowWidth2 || frame.height() == this.mHeight)))) | (!this.mDragResizing && this.mResizeMode == 0) | this.mActivityRelaunched;
                                        computesInternalInsets = !this.mAttachInfo.mTreeObserver.hasComputeInternalInsetsListeners() || this.mAttachInfo.mHasNonEmptyGivenInternalInsets;
                                        surfaceGenerationId = this.mSurface.getGenerationId();
                                        isViewVisible = viewVisibility != 0;
                                        boolean windowRelayoutWasForced = this.mForceNextWindowRelayout;
                                        if (!DEBUG_LAYOUT) {
                                            insetsPending = false;
                                            String str = this.mTag;
                                            relayoutResult = 0;
                                            StringBuilder sb = new StringBuilder();
                                            updatedConfiguration = false;
                                            sb.append("ViewRoot adjustSize+ : mFirst = ");
                                            sb.append(this.mFirst);
                                            sb.append(", windowShouldResize = ");
                                            sb.append(windowShouldResize);
                                            sb.append(", insetsChanged = ");
                                            insetsChanged4 = insetsChanged2;
                                            sb.append(insetsChanged4);
                                            insetsChanged3 = false;
                                            sb.append(", viewVisibilityChanged = ");
                                            sb.append(viewVisibilityChanged2);
                                            sb.append(", params = ");
                                            params4 = params3;
                                            sb.append(params4);
                                            i2 = desiredWindowWidth2;
                                            sb.append(", mForceNextWindowRelayout = ");
                                            sb.append(this.mForceNextWindowRelayout);
                                            sb.append(", isViewVisible = ");
                                            sb.append(isViewVisible);
                                            Log.v(str, sb.toString());
                                        } else {
                                            insetsPending = false;
                                            relayoutResult = 0;
                                            updatedConfiguration = false;
                                            insetsChanged4 = insetsChanged2;
                                            insetsChanged3 = false;
                                            params4 = params3;
                                            i2 = desiredWindowWidth2;
                                        }
                                        if (!this.mFirst || windowShouldResize || insetsChanged4 || viewVisibilityChanged2 || params4 != null) {
                                            frame2 = frame;
                                        } else if (this.mForceNextWindowRelayout) {
                                            frame2 = frame;
                                        } else {
                                            frame2 = frame;
                                            maybeHandleWindowMove(frame2);
                                            isViewVisible2 = isViewVisible;
                                            viewVisibilityChanged = viewVisibilityChanged2;
                                            layoutRequested2 = layoutRequested;
                                            insetsPending2 = insetsPending;
                                            relayoutResult2 = relayoutResult;
                                            if (insetsChanged3) {
                                                updateBoundsSurface();
                                            }
                                            didLayout = !layoutRequested2 && (!this.mStopped || this.mReportNextDraw);
                                            triggerGlobalLayoutListener = !didLayout || this.mAttachInfo.mRecomputeGlobalAttributes;
                                            if (DEBUG_LAYOUT) {
                                                Log.v(this.mTag, "ViewRoot layout+ : " + host + ", layoutRequested = " + layoutRequested2 + ", frame = " + frame2 + ", mStopped = " + this.mStopped + ", host.getMeasuredWidth() = " + host.getMeasuredWidth() + ", host.getMeasuredHeight() = " + host.getMeasuredHeight());
                                            }
                                            if (didLayout) {
                                                performLayout(lp, this.mWidth, this.mHeight);
                                                if (this.mView instanceof ViewGroup) {
                                                    OppoRefreshRateInjector.getInstance().setRefreshRateIfNeed(this.mContext, (ViewGroup) this.mView, this);
                                                }
                                                if ((host.mPrivateFlags & 512) != 0) {
                                                    host.getLocationInWindow(this.mTmpLocation);
                                                    Region region = this.mTransparentRegion;
                                                    int[] iArr = this.mTmpLocation;
                                                    region.set(iArr[0], iArr[1], (iArr[0] + host.mRight) - host.mLeft, (this.mTmpLocation[1] + host.mBottom) - host.mTop);
                                                    host.gatherTransparentRegion(this.mTransparentRegion);
                                                    CompatibilityInfo.Translator translator = this.mTranslator;
                                                    if (translator != null) {
                                                        translator.translateRegionInWindowToScreen(this.mTransparentRegion);
                                                    }
                                                    if (!this.mTransparentRegion.equals(this.mPreviousTransparentRegion)) {
                                                        this.mPreviousTransparentRegion.set(this.mTransparentRegion);
                                                        this.mFullRedrawNeeded = true;
                                                        try {
                                                            this.mWindowSession.setTransparentRegion(this.mWindow, this.mTransparentRegion);
                                                            if (ViewDebugManager.DBG_TRANSP) {
                                                                Log.d(this.mTag, "Set transparent region to WMS, region = " + this.mTransparentRegion);
                                                            }
                                                        } catch (RemoteException e2) {
                                                            Log.e(this.mTag, "Exception in " + this + " when set transparent region.", e2);
                                                        }
                                                    }
                                                }
                                                if (DBG) {
                                                    System.out.println("======================================");
                                                    System.out.println("performTraversals -- after setFrame");
                                                    host.debug();
                                                }
                                            }
                                            if (triggerGlobalLayoutListener) {
                                                View.AttachInfo attachInfo4 = this.mAttachInfo;
                                                attachInfo4.mRecomputeGlobalAttributes = false;
                                                attachInfo4.mTreeObserver.dispatchOnGlobalLayout();
                                            }
                                            if (computesInternalInsets) {
                                                ViewTreeObserver.InternalInsetsInfo insets = this.mAttachInfo.mGivenInternalInsets;
                                                insets.reset();
                                                this.mAttachInfo.mTreeObserver.dispatchOnComputeInternalInsets(insets);
                                                this.mAttachInfo.mHasNonEmptyGivenInternalInsets = !insets.isEmpty();
                                                if (insetsPending2 || !this.mLastGivenInsets.equals(insets)) {
                                                    this.mLastGivenInsets.set(insets);
                                                    CompatibilityInfo.Translator translator2 = this.mTranslator;
                                                    if (translator2 != null) {
                                                        Rect contentInsets = translator2.getTranslatedContentInsets(insets.contentInsets);
                                                        Rect visibleInsets3 = this.mTranslator.getTranslatedVisibleInsets(insets.visibleInsets);
                                                        touchableRegion = this.mTranslator.getTranslatedTouchableArea(insets.touchableRegion);
                                                        visibleInsets = visibleInsets3;
                                                        visibleInsets2 = contentInsets;
                                                    } else {
                                                        Rect contentInsets2 = insets.contentInsets;
                                                        Rect visibleInsets4 = insets.visibleInsets;
                                                        touchableRegion = insets.touchableRegion;
                                                        visibleInsets = visibleInsets4;
                                                        visibleInsets2 = contentInsets2;
                                                    }
                                                    try {
                                                        this.mWindowSession.setInsets(this.mWindow, insets.mTouchableInsets, visibleInsets2, visibleInsets, touchableRegion);
                                                    } catch (RemoteException e3) {
                                                        Log.e(this.mTag, "RemoteException happens when setInsets, mWindow = " + this.mWindow + ", contentInsets = " + visibleInsets2 + ", visibleInsets = " + visibleInsets + ", touchableRegion = " + touchableRegion + ", this = " + this, e3);
                                                    }
                                                }
                                            }
                                            if (this.mFirst && this.mView != null) {
                                                if (!sAlwaysAssignFocus || !isInTouchMode()) {
                                                    if (DEBUG_INPUT_RESIZE) {
                                                        Log.v(this.mTag, "First: mView.hasFocus()=" + this.mView.hasFocus());
                                                    }
                                                    view = this.mView;
                                                    if (view != null) {
                                                        if (!view.hasFocus()) {
                                                            this.mView.restoreDefaultFocus();
                                                            if (DEBUG_INPUT_RESIZE) {
                                                                Log.v(this.mTag, "First: requested focused view=" + this.mView.findFocus());
                                                            }
                                                        } else if (DEBUG_INPUT_RESIZE) {
                                                            Log.v(this.mTag, "First: existing focused view=" + this.mView.findFocus());
                                                        }
                                                    }
                                                } else {
                                                    View focused = this.mView.findFocus();
                                                    if ((focused instanceof ViewGroup) && ((ViewGroup) focused).getDescendantFocusability() == 262144) {
                                                        focused.restoreDefaultFocus();
                                                    }
                                                }
                                                WindowManager.LayoutParams layoutParams2 = this.mWindowAttributes;
                                                if (!(layoutParams2 == null || layoutParams2.getTitle() == null || !this.mWindowAttributes.getTitle().toString().contains("com.tencent.mm.ui.LauncherUI"))) {
                                                    this.mIsWeixinLauncherUI = true;
                                                }
                                                WindowManager.LayoutParams layoutParams3 = this.mWindowAttributes;
                                                if (!(layoutParams3 == null || layoutParams3.getTitle() == null || !this.mWindowAttributes.getTitle().toString().contains("com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyReceiveUI"))) {
                                                    this.mIsLuckyMoneyView = true;
                                                }
                                            }
                                            boolean changedVisibility = (!viewVisibilityChanged || this.mFirst) && isViewVisible2;
                                            hasWindowFocus = !this.mAttachInfo.mHasWindowFocus && isViewVisible2;
                                            regainedFocus = !hasWindowFocus && this.mLostWindowFocus;
                                            if (!regainedFocus) {
                                                this.mLostWindowFocus = false;
                                            } else if (!hasWindowFocus && this.mHadWindowFocus) {
                                                this.mLostWindowFocus = true;
                                            }
                                            if (changedVisibility || regainedFocus) {
                                                layoutParams = this.mWindowAttributes;
                                                if (layoutParams != null) {
                                                    isToast = false;
                                                } else {
                                                    isToast = layoutParams.type == 2005;
                                                }
                                                if (!isToast) {
                                                    host.sendAccessibilityEvent(32);
                                                }
                                            }
                                            this.mWillDrawSoon = false;
                                            this.mNewSurfaceNeeded = false;
                                            this.mActivityRelaunched = false;
                                            this.mViewVisibility = viewVisibility;
                                            this.mHadWindowFocus = hasWindowFocus;
                                            if (hasWindowFocus || isInLocalFocusMode()) {
                                                cancelDraw = true;
                                            } else {
                                                boolean imTarget = WindowManager.LayoutParams.mayUseInputMethod(this.mWindowAttributes.flags);
                                                if (imTarget != this.mLastWasImTarget) {
                                                    this.mLastWasImTarget = imTarget;
                                                    InputMethodManager imm = (InputMethodManager) this.mContext.getSystemService(InputMethodManager.class);
                                                    if (imm == null || !imTarget) {
                                                        cancelDraw = true;
                                                    } else {
                                                        imm.onPreWindowFocus(this.mView, hasWindowFocus);
                                                        View view2 = this.mView;
                                                        cancelDraw = true;
                                                        imm.onPostWindowFocus(view2, view2.findFocus(), this.mWindowAttributes.softInputMode, !this.mHasHadWindowFocus, this.mWindowAttributes.flags);
                                                    }
                                                } else {
                                                    cancelDraw = true;
                                                }
                                            }
                                            if ((relayoutResult2 & 2) != 0) {
                                                reportNextDraw();
                                            }
                                            if (!this.mAttachInfo.mTreeObserver.dispatchOnPreDraw() && isViewVisible2) {
                                                cancelDraw = false;
                                            }
                                            OppoViewDebug.performTraversals(cancelDraw);
                                            if (cancelDraw) {
                                                ArrayList<LayoutTransition> arrayList = this.mPendingTransitions;
                                                if (arrayList != null && arrayList.size() > 0) {
                                                    for (int i4 = 0; i4 < this.mPendingTransitions.size(); i4++) {
                                                        this.mPendingTransitions.get(i4).startChangingAnimations();
                                                    }
                                                    this.mPendingTransitions.clear();
                                                }
                                                performDraw();
                                            } else if (!isViewVisible2) {
                                                ArrayList<LayoutTransition> arrayList2 = this.mPendingTransitions;
                                                if (arrayList2 != null && arrayList2.size() > 0) {
                                                    for (int i5 = 0; i5 < this.mPendingTransitions.size(); i5++) {
                                                        this.mPendingTransitions.get(i5).endChangingAnimations();
                                                    }
                                                    this.mPendingTransitions.clear();
                                                }
                                            } else if (!this.mFirst || !this.mIsInTraversal || !scheduleTraversalsImmediately()) {
                                                scheduleTraversals();
                                            } else {
                                                Log.v(TAG, "schedule traversals immediately");
                                            }
                                            if (!DEBUG_DRAW || ViewDebugManager.DEBUG_LIFECYCLE || ViewDebugManager.DEBUG_ENG) {
                                                ViewDebugManager instance = ViewDebugManager.getInstance();
                                                View.AttachInfo attachInfo5 = this.mAttachInfo;
                                                ThreadedRenderer threadedRenderer = attachInfo5.mThreadedRenderer;
                                                if (this.mAttachInfo.mThreadedRenderer != null) {
                                                    z2 = false;
                                                } else {
                                                    z2 = this.mAttachInfo.mThreadedRenderer.isEnabled();
                                                }
                                                instance.debugTraveralDone(attachInfo5, threadedRenderer, z2, this, isViewVisible2, cancelDraw, this.mTag);
                                            }
                                            this.mFirst = false;
                                            this.mIsInTraversal = false;
                                        }
                                        this.mForceNextWindowRelayout = false;
                                        if (!isViewVisible) {
                                            insetsPending3 = computesInternalInsets && (this.mFirst || viewVisibilityChanged2);
                                        } else {
                                            insetsPending3 = insetsPending;
                                        }
                                        baseSurfaceHolder = this.mSurfaceHolder;
                                        if (baseSurfaceHolder == null) {
                                            baseSurfaceHolder.mSurfaceLock.lock();
                                            this.mDrawingAllowed = true;
                                        }
                                        contentInsetsChanged = false;
                                        boolean hadSurface = this.mSurface.isValid();
                                        if (!DEBUG_LAYOUT) {
                                            try {
                                                String str2 = this.mTag;
                                                boolean z4 = false;
                                                try {
                                                    StringBuilder sb2 = new StringBuilder();
                                                    isViewVisible2 = isViewVisible;
                                                    try {
                                                        sb2.append("host=w:");
                                                        sb2.append(host.getMeasuredWidth());
                                                        sb2.append(", h:");
                                                        sb2.append(host.getMeasuredHeight());
                                                        sb2.append(", params=");
                                                        sb2.append(params4);
                                                        sb2.append(" surface=");
                                                        sb2.append(this.mSurface);
                                                        sb2.append(",hadSurface = ");
                                                        sb2.append(hadSurface);
                                                        Log.i(str2, sb2.toString());
                                                        hwInitialized2 = z4;
                                                    } catch (RemoteException e4) {
                                                        e = e4;
                                                        insetsPending2 = insetsPending3;
                                                        viewVisibilityChanged = viewVisibilityChanged2;
                                                        relayoutResult2 = relayoutResult;
                                                        relayoutResult3 = surfaceGenerationId;
                                                        updatedConfiguration2 = updatedConfiguration;
                                                        hwInitialized = z4;
                                                    }
                                                } catch (RemoteException e5) {
                                                    e = e5;
                                                    insetsPending2 = insetsPending3;
                                                    isViewVisible2 = isViewVisible;
                                                    viewVisibilityChanged = viewVisibilityChanged2;
                                                    relayoutResult2 = relayoutResult;
                                                    relayoutResult3 = surfaceGenerationId;
                                                    updatedConfiguration2 = updatedConfiguration;
                                                    hwInitialized = z4;
                                                    Log.e(this.mTag, "RemoteException happens in " + this, e);
                                                    boolean contentInsetsChanged2 = contentInsetsChanged;
                                                    boolean surfaceSizeChanged = insetsChanged3;
                                                    boolean updatedConfiguration3 = updatedConfiguration2;
                                                    boolean hwInitialized3 = hwInitialized;
                                                    Log.v(this.mTag, "Relayout returned: frame=" + frame2 + ", surface=" + this.mSurface);
                                                    this.mAttachInfo.mWindowLeft = frame2.left;
                                                    this.mAttachInfo.mWindowTop = frame2.top;
                                                    this.mWidth = frame2.width();
                                                    this.mHeight = frame2.height();
                                                    if (this.mSurfaceHolder == null) {
                                                    }
                                                    ThreadedRenderer threadedRenderer2 = this.mAttachInfo.mThreadedRenderer;
                                                    threadedRenderer2.setup(this.mWidth, this.mHeight, this.mAttachInfo, this.mWindowAttributes.surfaceInsets);
                                                    this.mNeedsRendererSetup = false;
                                                    if ((relayoutResult2 & 1) == 0) {
                                                    }
                                                    int childWidthMeasureSpec = getRootMeasureSpec(this.mWidth, lp.width);
                                                    int childHeightMeasureSpec = getRootMeasureSpec(this.mHeight, lp.height);
                                                    if (!DEBUG_LAYOUT) {
                                                    }
                                                    performMeasure(childWidthMeasureSpec, childHeightMeasureSpec);
                                                    int width = host.getMeasuredWidth();
                                                    int height = host.getMeasuredHeight();
                                                    measureAgain = false;
                                                    if (lp.horizontalWeight > 0.0f) {
                                                    }
                                                    if (lp.verticalWeight > 0.0f) {
                                                    }
                                                    if (!measureAgain) {
                                                    }
                                                    layoutRequested = true;
                                                    layoutRequested2 = layoutRequested;
                                                    if (insetsChanged3) {
                                                    }
                                                    if (!layoutRequested2) {
                                                    }
                                                    if (!didLayout) {
                                                    }
                                                    if (DEBUG_LAYOUT) {
                                                    }
                                                    if (didLayout) {
                                                    }
                                                    if (triggerGlobalLayoutListener) {
                                                    }
                                                    if (computesInternalInsets) {
                                                    }
                                                    if (!sAlwaysAssignFocus) {
                                                    }
                                                    if (DEBUG_INPUT_RESIZE) {
                                                    }
                                                    view = this.mView;
                                                    if (view != null) {
                                                    }
                                                    WindowManager.LayoutParams layoutParams22 = this.mWindowAttributes;
                                                    this.mIsWeixinLauncherUI = true;
                                                    WindowManager.LayoutParams layoutParams32 = this.mWindowAttributes;
                                                    this.mIsLuckyMoneyView = true;
                                                    if (!viewVisibilityChanged) {
                                                    }
                                                    if (!this.mAttachInfo.mHasWindowFocus) {
                                                    }
                                                    if (!hasWindowFocus) {
                                                    }
                                                    if (!regainedFocus) {
                                                    }
                                                    layoutParams = this.mWindowAttributes;
                                                    if (layoutParams != null) {
                                                    }
                                                    if (!isToast) {
                                                    }
                                                    this.mWillDrawSoon = false;
                                                    this.mNewSurfaceNeeded = false;
                                                    this.mActivityRelaunched = false;
                                                    this.mViewVisibility = viewVisibility;
                                                    this.mHadWindowFocus = hasWindowFocus;
                                                    if (hasWindowFocus) {
                                                    }
                                                    cancelDraw = true;
                                                    if ((relayoutResult2 & 2) != 0) {
                                                    }
                                                    cancelDraw = false;
                                                    OppoViewDebug.performTraversals(cancelDraw);
                                                    if (cancelDraw) {
                                                    }
                                                    if (!DEBUG_DRAW) {
                                                    }
                                                    ViewDebugManager instance2 = ViewDebugManager.getInstance();
                                                    View.AttachInfo attachInfo52 = this.mAttachInfo;
                                                    ThreadedRenderer threadedRenderer3 = attachInfo52.mThreadedRenderer;
                                                    if (this.mAttachInfo.mThreadedRenderer != null) {
                                                    }
                                                    instance2.debugTraveralDone(attachInfo52, threadedRenderer3, z2, this, isViewVisible2, cancelDraw, this.mTag);
                                                    this.mFirst = false;
                                                    this.mIsInTraversal = false;
                                                }
                                            } catch (RemoteException e6) {
                                                e = e6;
                                                hwInitialized = false;
                                                insetsPending2 = insetsPending3;
                                                isViewVisible2 = isViewVisible;
                                                viewVisibilityChanged = viewVisibilityChanged2;
                                                relayoutResult2 = relayoutResult;
                                                relayoutResult3 = surfaceGenerationId;
                                                updatedConfiguration2 = updatedConfiguration;
                                                Log.e(this.mTag, "RemoteException happens in " + this, e);
                                                boolean contentInsetsChanged22 = contentInsetsChanged;
                                                boolean surfaceSizeChanged2 = insetsChanged3;
                                                boolean updatedConfiguration32 = updatedConfiguration2;
                                                boolean hwInitialized32 = hwInitialized;
                                                Log.v(this.mTag, "Relayout returned: frame=" + frame2 + ", surface=" + this.mSurface);
                                                this.mAttachInfo.mWindowLeft = frame2.left;
                                                this.mAttachInfo.mWindowTop = frame2.top;
                                                this.mWidth = frame2.width();
                                                this.mHeight = frame2.height();
                                                if (this.mSurfaceHolder == null) {
                                                }
                                                ThreadedRenderer threadedRenderer22 = this.mAttachInfo.mThreadedRenderer;
                                                threadedRenderer22.setup(this.mWidth, this.mHeight, this.mAttachInfo, this.mWindowAttributes.surfaceInsets);
                                                this.mNeedsRendererSetup = false;
                                                if ((relayoutResult2 & 1) == 0) {
                                                }
                                                int childWidthMeasureSpec2 = getRootMeasureSpec(this.mWidth, lp.width);
                                                int childHeightMeasureSpec2 = getRootMeasureSpec(this.mHeight, lp.height);
                                                if (!DEBUG_LAYOUT) {
                                                }
                                                performMeasure(childWidthMeasureSpec2, childHeightMeasureSpec2);
                                                int width2 = host.getMeasuredWidth();
                                                int height2 = host.getMeasuredHeight();
                                                measureAgain = false;
                                                if (lp.horizontalWeight > 0.0f) {
                                                }
                                                if (lp.verticalWeight > 0.0f) {
                                                }
                                                if (!measureAgain) {
                                                }
                                                layoutRequested = true;
                                                layoutRequested2 = layoutRequested;
                                                if (insetsChanged3) {
                                                }
                                                if (!layoutRequested2) {
                                                }
                                                if (!didLayout) {
                                                }
                                                if (DEBUG_LAYOUT) {
                                                }
                                                if (didLayout) {
                                                }
                                                if (triggerGlobalLayoutListener) {
                                                }
                                                if (computesInternalInsets) {
                                                }
                                                if (!sAlwaysAssignFocus) {
                                                }
                                                if (DEBUG_INPUT_RESIZE) {
                                                }
                                                view = this.mView;
                                                if (view != null) {
                                                }
                                                WindowManager.LayoutParams layoutParams222 = this.mWindowAttributes;
                                                this.mIsWeixinLauncherUI = true;
                                                WindowManager.LayoutParams layoutParams322 = this.mWindowAttributes;
                                                this.mIsLuckyMoneyView = true;
                                                if (!viewVisibilityChanged) {
                                                }
                                                if (!this.mAttachInfo.mHasWindowFocus) {
                                                }
                                                if (!hasWindowFocus) {
                                                }
                                                if (!regainedFocus) {
                                                }
                                                layoutParams = this.mWindowAttributes;
                                                if (layoutParams != null) {
                                                }
                                                if (!isToast) {
                                                }
                                                this.mWillDrawSoon = false;
                                                this.mNewSurfaceNeeded = false;
                                                this.mActivityRelaunched = false;
                                                this.mViewVisibility = viewVisibility;
                                                this.mHadWindowFocus = hasWindowFocus;
                                                if (hasWindowFocus) {
                                                }
                                                cancelDraw = true;
                                                if ((relayoutResult2 & 2) != 0) {
                                                }
                                                cancelDraw = false;
                                                OppoViewDebug.performTraversals(cancelDraw);
                                                if (cancelDraw) {
                                                }
                                                if (!DEBUG_DRAW) {
                                                }
                                                ViewDebugManager instance22 = ViewDebugManager.getInstance();
                                                View.AttachInfo attachInfo522 = this.mAttachInfo;
                                                ThreadedRenderer threadedRenderer32 = attachInfo522.mThreadedRenderer;
                                                if (this.mAttachInfo.mThreadedRenderer != null) {
                                                }
                                                instance22.debugTraveralDone(attachInfo522, threadedRenderer32, z2, this, isViewVisible2, cancelDraw, this.mTag);
                                                this.mFirst = false;
                                                this.mIsInTraversal = false;
                                            }
                                        } else {
                                            hwInitialized2 = false;
                                            isViewVisible2 = isViewVisible;
                                        }
                                        synchronized (this.mGraphicLock) {
                                            try {
                                                if (this.mAttachInfo.mThreadedRenderer != null) {
                                                    try {
                                                        if (this.mAttachInfo.mThreadedRenderer.pause()) {
                                                            try {
                                                                viewVisibilityChanged = viewVisibilityChanged2;
                                                                this.mDirty.set(0, 0, this.mWidth, this.mHeight);
                                                            } catch (Throwable th2) {
                                                                th = th2;
                                                                insetsPending2 = insetsPending3;
                                                                relayoutResult3 = surfaceGenerationId;
                                                                while (true) {
                                                                    break;
                                                                }
                                                                throw th;
                                                            }
                                                        } else {
                                                            viewVisibilityChanged = viewVisibilityChanged2;
                                                        }
                                                        this.mChoreographer.mFrameInfo.addFlags(1);
                                                    } catch (Throwable th3) {
                                                        th = th3;
                                                        viewVisibilityChanged = viewVisibilityChanged2;
                                                        insetsPending2 = insetsPending3;
                                                        relayoutResult3 = surfaceGenerationId;
                                                        while (true) {
                                                            break;
                                                        }
                                                        throw th;
                                                    }
                                                } else {
                                                    viewVisibilityChanged = viewVisibilityChanged2;
                                                }
                                                try {
                                                } catch (Throwable th4) {
                                                    th = th4;
                                                    insetsPending2 = insetsPending3;
                                                    relayoutResult3 = surfaceGenerationId;
                                                    while (true) {
                                                        break;
                                                    }
                                                    throw th;
                                                }
                                            } catch (Throwable th5) {
                                                th = th5;
                                                insetsPending2 = insetsPending3;
                                                viewVisibilityChanged = viewVisibilityChanged2;
                                                relayoutResult3 = surfaceGenerationId;
                                                while (true) {
                                                    break;
                                                }
                                                throw th;
                                            }
                                        }
                                    } else {
                                        params3 = params2;
                                        insetsChanged2 = insetsChanged;
                                    }
                                } else {
                                    params3 = params2;
                                    insetsChanged2 = insetsChanged;
                                }
                                windowSizeMayChange = windowSizeMayChange2;
                                if (layoutRequested) {
                                }
                                boolean windowShouldResize2 = (!layoutRequested && windowSizeMayChange && !(this.mWidth == host.getMeasuredWidth() && this.mHeight == host.getMeasuredHeight() && ((lp.width != i || frame.width() >= desiredWindowHeight2 || frame.width() == this.mWidth) && (lp.height != i || frame.height() >= desiredWindowWidth2 || frame.height() == this.mHeight)))) | (!this.mDragResizing && this.mResizeMode == 0) | this.mActivityRelaunched;
                                if (!this.mAttachInfo.mTreeObserver.hasComputeInternalInsetsListeners()) {
                                }
                                surfaceGenerationId = this.mSurface.getGenerationId();
                                if (viewVisibility != 0) {
                                }
                                boolean windowRelayoutWasForced2 = this.mForceNextWindowRelayout;
                                if (!DEBUG_LAYOUT) {
                                }
                                if (!this.mFirst) {
                                }
                                frame2 = frame;
                                this.mForceNextWindowRelayout = false;
                                if (!isViewVisible) {
                                }
                                baseSurfaceHolder = this.mSurfaceHolder;
                                if (baseSurfaceHolder == null) {
                                }
                                contentInsetsChanged = false;
                                boolean hadSurface2 = this.mSurface.isValid();
                                if (!DEBUG_LAYOUT) {
                                }
                                synchronized (this.mGraphicLock) {
                                }
                            }
                        }
                    }
                    params2 = params;
                    if (params2 != null) {
                    }
                    if (this.mApplyInsetsRequested) {
                    }
                    windowSizeMayChange = windowSizeMayChange2;
                    if (layoutRequested) {
                    }
                    boolean windowShouldResize22 = (!layoutRequested && windowSizeMayChange && !(this.mWidth == host.getMeasuredWidth() && this.mHeight == host.getMeasuredHeight() && ((lp.width != i || frame.width() >= desiredWindowHeight2 || frame.width() == this.mWidth) && (lp.height != i || frame.height() >= desiredWindowWidth2 || frame.height() == this.mHeight)))) | (!this.mDragResizing && this.mResizeMode == 0) | this.mActivityRelaunched;
                    if (!this.mAttachInfo.mTreeObserver.hasComputeInternalInsetsListeners()) {
                    }
                    surfaceGenerationId = this.mSurface.getGenerationId();
                    if (viewVisibility != 0) {
                    }
                    boolean windowRelayoutWasForced22 = this.mForceNextWindowRelayout;
                    if (!DEBUG_LAYOUT) {
                    }
                    if (!this.mFirst) {
                    }
                    frame2 = frame;
                    this.mForceNextWindowRelayout = false;
                    if (!isViewVisible) {
                    }
                    baseSurfaceHolder = this.mSurfaceHolder;
                    if (baseSurfaceHolder == null) {
                    }
                    contentInsetsChanged = false;
                    boolean hadSurface22 = this.mSurface.isValid();
                    if (!DEBUG_LAYOUT) {
                    }
                    try {
                        synchronized (this.mGraphicLock) {
                        }
                    } catch (RemoteException e7) {
                        e = e7;
                        insetsPending2 = insetsPending3;
                        viewVisibilityChanged = viewVisibilityChanged2;
                        relayoutResult3 = surfaceGenerationId;
                        relayoutResult2 = relayoutResult;
                        updatedConfiguration2 = updatedConfiguration;
                        hwInitialized = hwInitialized2;
                        Log.e(this.mTag, "RemoteException happens in " + this, e);
                        boolean contentInsetsChanged222 = contentInsetsChanged;
                        boolean surfaceSizeChanged22 = insetsChanged3;
                        boolean updatedConfiguration322 = updatedConfiguration2;
                        boolean hwInitialized322 = hwInitialized;
                        Log.v(this.mTag, "Relayout returned: frame=" + frame2 + ", surface=" + this.mSurface);
                        this.mAttachInfo.mWindowLeft = frame2.left;
                        this.mAttachInfo.mWindowTop = frame2.top;
                        this.mWidth = frame2.width();
                        this.mHeight = frame2.height();
                        if (this.mSurfaceHolder == null) {
                        }
                        ThreadedRenderer threadedRenderer222 = this.mAttachInfo.mThreadedRenderer;
                        threadedRenderer222.setup(this.mWidth, this.mHeight, this.mAttachInfo, this.mWindowAttributes.surfaceInsets);
                        this.mNeedsRendererSetup = false;
                        if ((relayoutResult2 & 1) == 0) {
                        }
                        int childWidthMeasureSpec22 = getRootMeasureSpec(this.mWidth, lp.width);
                        int childHeightMeasureSpec22 = getRootMeasureSpec(this.mHeight, lp.height);
                        if (!DEBUG_LAYOUT) {
                        }
                        performMeasure(childWidthMeasureSpec22, childHeightMeasureSpec22);
                        int width22 = host.getMeasuredWidth();
                        int height22 = host.getMeasuredHeight();
                        measureAgain = false;
                        if (lp.horizontalWeight > 0.0f) {
                        }
                        if (lp.verticalWeight > 0.0f) {
                        }
                        if (!measureAgain) {
                        }
                        layoutRequested = true;
                        layoutRequested2 = layoutRequested;
                        if (insetsChanged3) {
                        }
                        if (!layoutRequested2) {
                        }
                        if (!didLayout) {
                        }
                        if (DEBUG_LAYOUT) {
                        }
                        if (didLayout) {
                        }
                        if (triggerGlobalLayoutListener) {
                        }
                        if (computesInternalInsets) {
                        }
                        if (!sAlwaysAssignFocus) {
                        }
                        if (DEBUG_INPUT_RESIZE) {
                        }
                        view = this.mView;
                        if (view != null) {
                        }
                        WindowManager.LayoutParams layoutParams2222 = this.mWindowAttributes;
                        this.mIsWeixinLauncherUI = true;
                        WindowManager.LayoutParams layoutParams3222 = this.mWindowAttributes;
                        this.mIsLuckyMoneyView = true;
                        if (!viewVisibilityChanged) {
                        }
                        if (!this.mAttachInfo.mHasWindowFocus) {
                        }
                        if (!hasWindowFocus) {
                        }
                        if (!regainedFocus) {
                        }
                        layoutParams = this.mWindowAttributes;
                        if (layoutParams != null) {
                        }
                        if (!isToast) {
                        }
                        this.mWillDrawSoon = false;
                        this.mNewSurfaceNeeded = false;
                        this.mActivityRelaunched = false;
                        this.mViewVisibility = viewVisibility;
                        this.mHadWindowFocus = hasWindowFocus;
                        if (hasWindowFocus) {
                        }
                        cancelDraw = true;
                        if ((relayoutResult2 & 2) != 0) {
                        }
                        cancelDraw = false;
                        OppoViewDebug.performTraversals(cancelDraw);
                        if (cancelDraw) {
                        }
                        if (!DEBUG_DRAW) {
                        }
                        ViewDebugManager instance222 = ViewDebugManager.getInstance();
                        View.AttachInfo attachInfo5222 = this.mAttachInfo;
                        ThreadedRenderer threadedRenderer322 = attachInfo5222.mThreadedRenderer;
                        if (this.mAttachInfo.mThreadedRenderer != null) {
                        }
                        instance222.debugTraveralDone(attachInfo5222, threadedRenderer322, z2, this, isViewVisible2, cancelDraw, this.mTag);
                        this.mFirst = false;
                        this.mIsInTraversal = false;
                    }
                }
            }
            viewUserVisibilityChanged = false;
            WindowManager.LayoutParams params52 = null;
            if (!this.mWindowAttributesChanged) {
            }
            supportsScreen = this.mDisplay.getDisplayAdjustments().getCompatibilityInfo().supportsScreen();
            z = this.mLastInCompatMode;
            if (supportsScreen != z) {
            }
            this.mWindowAttributesChangesFlag = 0;
            Rect frame32 = this.mWinFrame;
            if (!this.mFirst) {
            }
            if (viewVisibilityChanged2) {
            }
            if (this.mAttachInfo.mWindowVisibility != 0) {
            }
            getRunQueue().executeActions(this.mAttachInfo.mHandler);
            boolean insetsChanged62 = false;
            layoutRequested = !this.mLayoutRequested && (!this.mStopped || this.mReportNextDraw);
            if (!layoutRequested) {
            }
            if (collectViewAttributes()) {
            }
            if (this.mAttachInfo.mForceReportNewAttributes) {
            }
            View.AttachInfo attachInfo32 = this.mAttachInfo;
            attachInfo32.mViewVisibilityChanged = false;
            resizeMode = this.mSoftInputMode & 240;
            if (resizeMode == 0) {
            }
            params2 = params;
            if (params2 != null) {
            }
            if (this.mApplyInsetsRequested) {
            }
            windowSizeMayChange = windowSizeMayChange2;
            if (layoutRequested) {
            }
            boolean windowShouldResize222 = (!layoutRequested && windowSizeMayChange && !(this.mWidth == host.getMeasuredWidth() && this.mHeight == host.getMeasuredHeight() && ((lp.width != i || frame.width() >= desiredWindowHeight2 || frame.width() == this.mWidth) && (lp.height != i || frame.height() >= desiredWindowWidth2 || frame.height() == this.mHeight)))) | (!this.mDragResizing && this.mResizeMode == 0) | this.mActivityRelaunched;
            if (!this.mAttachInfo.mTreeObserver.hasComputeInternalInsetsListeners()) {
            }
            surfaceGenerationId = this.mSurface.getGenerationId();
            if (viewVisibility != 0) {
            }
            boolean windowRelayoutWasForced222 = this.mForceNextWindowRelayout;
            if (!DEBUG_LAYOUT) {
            }
            if (!this.mFirst) {
            }
            frame2 = frame;
            this.mForceNextWindowRelayout = false;
            if (!isViewVisible) {
            }
            baseSurfaceHolder = this.mSurfaceHolder;
            if (baseSurfaceHolder == null) {
            }
            contentInsetsChanged = false;
            boolean hadSurface222 = this.mSurface.isValid();
            try {
                if (!DEBUG_LAYOUT) {
                }
                synchronized (this.mGraphicLock) {
                }
            } catch (RemoteException e8) {
                e = e8;
                insetsPending2 = insetsPending3;
                hwInitialized = false;
                isViewVisible2 = isViewVisible;
                viewVisibilityChanged = viewVisibilityChanged2;
                relayoutResult3 = surfaceGenerationId;
                relayoutResult2 = relayoutResult;
                updatedConfiguration2 = updatedConfiguration;
                Log.e(this.mTag, "RemoteException happens in " + this, e);
                boolean contentInsetsChanged2222 = contentInsetsChanged;
                boolean surfaceSizeChanged222 = insetsChanged3;
                boolean updatedConfiguration3222 = updatedConfiguration2;
                boolean hwInitialized3222 = hwInitialized;
                Log.v(this.mTag, "Relayout returned: frame=" + frame2 + ", surface=" + this.mSurface);
                this.mAttachInfo.mWindowLeft = frame2.left;
                this.mAttachInfo.mWindowTop = frame2.top;
                this.mWidth = frame2.width();
                this.mHeight = frame2.height();
                if (this.mSurfaceHolder == null) {
                }
                ThreadedRenderer threadedRenderer2222 = this.mAttachInfo.mThreadedRenderer;
                threadedRenderer2222.setup(this.mWidth, this.mHeight, this.mAttachInfo, this.mWindowAttributes.surfaceInsets);
                this.mNeedsRendererSetup = false;
                if ((relayoutResult2 & 1) == 0) {
                }
                int childWidthMeasureSpec222 = getRootMeasureSpec(this.mWidth, lp.width);
                int childHeightMeasureSpec222 = getRootMeasureSpec(this.mHeight, lp.height);
                if (!DEBUG_LAYOUT) {
                }
                performMeasure(childWidthMeasureSpec222, childHeightMeasureSpec222);
                int width222 = host.getMeasuredWidth();
                int height222 = host.getMeasuredHeight();
                measureAgain = false;
                if (lp.horizontalWeight > 0.0f) {
                }
                if (lp.verticalWeight > 0.0f) {
                }
                if (!measureAgain) {
                }
                layoutRequested = true;
                layoutRequested2 = layoutRequested;
                if (insetsChanged3) {
                }
                if (!layoutRequested2) {
                }
                if (!didLayout) {
                }
                if (DEBUG_LAYOUT) {
                }
                if (didLayout) {
                }
                if (triggerGlobalLayoutListener) {
                }
                if (computesInternalInsets) {
                }
                if (!sAlwaysAssignFocus) {
                }
                if (DEBUG_INPUT_RESIZE) {
                }
                view = this.mView;
                if (view != null) {
                }
                WindowManager.LayoutParams layoutParams22222 = this.mWindowAttributes;
                this.mIsWeixinLauncherUI = true;
                WindowManager.LayoutParams layoutParams32222 = this.mWindowAttributes;
                this.mIsLuckyMoneyView = true;
                if (!viewVisibilityChanged) {
                }
                if (!this.mAttachInfo.mHasWindowFocus) {
                }
                if (!hasWindowFocus) {
                }
                if (!regainedFocus) {
                }
                layoutParams = this.mWindowAttributes;
                if (layoutParams != null) {
                }
                if (!isToast) {
                }
                this.mWillDrawSoon = false;
                this.mNewSurfaceNeeded = false;
                this.mActivityRelaunched = false;
                this.mViewVisibility = viewVisibility;
                this.mHadWindowFocus = hasWindowFocus;
                if (hasWindowFocus) {
                }
                cancelDraw = true;
                if ((relayoutResult2 & 2) != 0) {
                }
                cancelDraw = false;
                OppoViewDebug.performTraversals(cancelDraw);
                if (cancelDraw) {
                }
                if (!DEBUG_DRAW) {
                }
                ViewDebugManager instance2222 = ViewDebugManager.getInstance();
                View.AttachInfo attachInfo52222 = this.mAttachInfo;
                ThreadedRenderer threadedRenderer3222 = attachInfo52222.mThreadedRenderer;
                if (this.mAttachInfo.mThreadedRenderer != null) {
                }
                instance2222.debugTraveralDone(attachInfo52222, threadedRenderer3222, z2, this, isViewVisible2, cancelDraw, this.mTag);
                this.mFirst = false;
                this.mIsInTraversal = false;
            }
        }
    }

    private void notifySurfaceDestroyed() {
        this.mSurfaceHolder.ungetCallbacks();
        SurfaceHolder.Callback[] callbacks = this.mSurfaceHolder.getCallbacks();
        if (callbacks != null) {
            for (SurfaceHolder.Callback c : callbacks) {
                c.surfaceDestroyed(this.mSurfaceHolder);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void maybeHandleWindowMove(Rect frame) {
        boolean windowMoved = (this.mAttachInfo.mWindowLeft == frame.left && this.mAttachInfo.mWindowTop == frame.top) ? false : true;
        if (windowMoved) {
            CompatibilityInfo.Translator translator = this.mTranslator;
            if (translator != null) {
                translator.translateRectInScreenToAppWinFrame(frame);
            }
            this.mAttachInfo.mWindowLeft = frame.left;
            this.mAttachInfo.mWindowTop = frame.top;
        }
        if (windowMoved || this.mAttachInfo.mNeedsUpdateLightCenter) {
            if (this.mAttachInfo.mThreadedRenderer != null) {
                this.mAttachInfo.mThreadedRenderer.setLightCenter(this.mAttachInfo);
            }
            this.mAttachInfo.mNeedsUpdateLightCenter = false;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    /* JADX WARNING: Code restructure failed: missing block: B:10:0x0013, code lost:
        if (r1 == false) goto L_0x001b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0015, code lost:
        r12.mInsetsController.onWindowFocusGained();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x001b, code lost:
        r12.mInsetsController.onWindowFocusLost();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0022, code lost:
        if (r12.mAdded == false) goto L_0x011a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0024, code lost:
        profileRendering(r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0028, code lost:
        if (r1 == false) goto L_0x0086;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x002a, code lost:
        ensureTouchModeLocally(r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0031, code lost:
        if (r12.mAttachInfo.mThreadedRenderer == null) goto L_0x0086;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0039, code lost:
        if (r12.mSurface.isValid() == false) goto L_0x0086;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x003b, code lost:
        r12.mFullRedrawNeeded = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:?, code lost:
        r4 = r12.mWindowAttributes;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x003f, code lost:
        if (r4 == null) goto L_0x0044;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0041, code lost:
        r5 = r4.surfaceInsets;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x0044, code lost:
        r5 = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0045, code lost:
        r12.mAttachInfo.mThreadedRenderer.initializeIfNeeded(r12.mWidth, r12.mHeight, r12.mAttachInfo, r12.mSurface, r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x0057, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x0058, code lost:
        android.util.Log.e(r12.mTag, "OutOfResourcesException locking surface", r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x0067, code lost:
        if (r12.mWindowSession.outOfMemory(r12.mWindow) == false) goto L_0x0069;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x0069, code lost:
        android.util.Slog.w(r12.mTag, "No processes killed for memory; killing self");
        android.os.Process.killProcess(android.os.Process.myPid());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x0079, code lost:
        r3 = r12.mHandler;
        r3.sendMessageDelayed(r3.obtainMessage(6), 500);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x0085, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x0086, code lost:
        r12.mAttachInfo.mHasWindowFocus = r1;
        r12.mLastWasImTarget = android.view.WindowManager.LayoutParams.mayUseInputMethod(r12.mWindowAttributes.flags);
        r4 = (android.view.inputmethod.InputMethodManager) r12.mContext.getSystemService(android.view.inputmethod.InputMethodManager.class);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x009e, code lost:
        if (r4 == null) goto L_0x00af;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x00a2, code lost:
        if (r12.mLastWasImTarget == false) goto L_0x00af;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:0x00a8, code lost:
        if (isInLocalFocusMode() != false) goto L_0x00af;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:0x00aa, code lost:
        r4.onPreWindowFocus(r12.mView, r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:45:0x00b1, code lost:
        if (r12.mView == null) goto L_0x00d3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:46:0x00b3, code lost:
        r12.mAttachInfo.mKeyDispatchState.reset();
        r12.mView.dispatchWindowFocusChanged(r1);
        r12.mAttachInfo.mTreeObserver.dispatchOnWindowFocusChange(r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:47:0x00ca, code lost:
        if (r12.mAttachInfo.mTooltipHost == null) goto L_0x00d3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:48:0x00cc, code lost:
        r12.mAttachInfo.mTooltipHost.hideTooltip();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:49:0x00d3, code lost:
        if (r1 == false) goto L_0x0113;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:50:0x00d5, code lost:
        if (r4 == null) goto L_0x00f7;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:52:0x00d9, code lost:
        if (r12.mLastWasImTarget == false) goto L_0x00f7;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:54:0x00df, code lost:
        if (isInLocalFocusMode() != false) goto L_0x00f7;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:55:0x00e1, code lost:
        r6 = r12.mView;
        r4.onPostWindowFocus(r6, r6.findFocus(), r12.mWindowAttributes.softInputMode, !r12.mHasHadWindowFocus, r12.mWindowAttributes.flags);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:56:0x00f7, code lost:
        r12.mWindowAttributes.softInputMode &= android.net.TrafficStats.TAG_NETWORK_STACK_RANGE_END;
        ((android.view.WindowManager.LayoutParams) r12.mView.getLayoutParams()).softInputMode &= android.net.TrafficStats.TAG_NETWORK_STACK_RANGE_END;
        r12.mHasHadWindowFocus = true;
        fireAccessibilityFocusEventIfHasFocusedNode();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:58:0x0115, code lost:
        if (r12.mPointerCapture == false) goto L_0x011a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:59:0x0117, code lost:
        handlePointerCaptureChanged(false);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:60:0x011a, code lost:
        r12.mFirstInputStage.onWindowFocusChanged(r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:61:0x011f, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x0011, code lost:
        if (android.view.ViewRootImpl.sNewInsetsMode == 0) goto L_0x0020;
     */
    private void handleWindowFocusChanged() {
        synchronized (this) {
            if (this.mWindowFocusChanged) {
                this.mWindowFocusChanged = false;
                boolean hasWindowFocus = this.mUpcomingWindowFocus;
                boolean inTouchMode = this.mUpcomingInTouchMode;
            }
        }
    }

    private void fireAccessibilityFocusEventIfHasFocusedNode() {
        View focusedView;
        if (AccessibilityManager.getInstance(this.mContext).isEnabled() && (focusedView = this.mView.findFocus()) != null) {
            AccessibilityNodeProvider provider = focusedView.getAccessibilityNodeProvider();
            if (provider == null) {
                focusedView.sendAccessibilityEvent(8);
                return;
            }
            AccessibilityNodeInfo focusedNode = findFocusedVirtualNode(provider);
            if (focusedNode != null) {
                int virtualId = AccessibilityNodeInfo.getVirtualDescendantId(focusedNode.getSourceNodeId());
                AccessibilityEvent event = AccessibilityEvent.obtain(8);
                event.setSource(focusedView, virtualId);
                event.setPackageName(focusedNode.getPackageName());
                event.setChecked(focusedNode.isChecked());
                event.setContentDescription(focusedNode.getContentDescription());
                event.setPassword(focusedNode.isPassword());
                event.getText().add(focusedNode.getText());
                event.setEnabled(focusedNode.isEnabled());
                focusedView.getParent().requestSendAccessibilityEvent(focusedView, event);
                focusedNode.recycle();
            }
        }
    }

    private AccessibilityNodeInfo findFocusedVirtualNode(AccessibilityNodeProvider provider) {
        AccessibilityNodeInfo focusedNode = provider.findFocus(1);
        if (focusedNode != null) {
            return focusedNode;
        }
        if (!this.mContext.isAutofillCompatibilityEnabled()) {
            return null;
        }
        AccessibilityNodeInfo current = provider.createAccessibilityNodeInfo(-1);
        if (current.isFocused()) {
            return current;
        }
        Queue<AccessibilityNodeInfo> fringe = new LinkedList<>();
        fringe.offer(current);
        while (!fringe.isEmpty()) {
            AccessibilityNodeInfo current2 = fringe.poll();
            LongArray childNodeIds = current2.getChildNodeIds();
            if (childNodeIds != null && childNodeIds.size() > 0) {
                int childCount = childNodeIds.size();
                for (int i = 0; i < childCount; i++) {
                    AccessibilityNodeInfo child = provider.createAccessibilityNodeInfo(AccessibilityNodeInfo.getVirtualDescendantId(childNodeIds.get(i)));
                    if (child != null) {
                        if (child.isFocused()) {
                            return child;
                        }
                        fringe.offer(child);
                    }
                }
                current2.recycle();
            }
        }
        return null;
    }

    private void handleOutOfResourcesException(Surface.OutOfResourcesException e) {
        Log.e(this.mTag, "OutOfResourcesException initializing HW surface", e);
        try {
            if (!this.mWindowSession.outOfMemory(this.mWindow) && Process.myUid() != 1000) {
                Slog.w(this.mTag, "No processes killed for memory; killing self");
                Process.killProcess(Process.myPid());
            }
        } catch (RemoteException e2) {
        }
        this.mLayoutRequested = true;
    }

    private void performMeasure(int childWidthMeasureSpec, int childHeightMeasureSpec) {
        if (this.mView != null) {
            Trace.traceBegin(8, "measure");
            try {
                this.mView.measure(childWidthMeasureSpec, childHeightMeasureSpec);
            } finally {
                Trace.traceEnd(8);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isInLayout() {
        return this.mInLayout;
    }

    /* access modifiers changed from: package-private */
    public boolean requestLayoutDuringLayout(View view) {
        if (view.mParent == null || view.mAttachInfo == null) {
            return true;
        }
        if (!this.mLayoutRequesters.contains(view)) {
            this.mLayoutRequesters.add(view);
        }
        if (!this.mHandlingLayoutInLayoutRequest) {
            return true;
        }
        return false;
    }

    /* JADX INFO: finally extract failed */
    private void performLayout(WindowManager.LayoutParams lp, int desiredWindowWidth, int desiredWindowHeight) {
        ArrayList<View> validLayoutRequesters;
        this.mLayoutRequested = false;
        this.mScrollMayChange = true;
        this.mInLayout = true;
        View host = this.mView;
        if (host != null) {
            if (DEBUG_ORIENTATION || DEBUG_LAYOUT) {
                String str = this.mTag;
                Log.v(str, "Laying out " + host + " in " + this + " to (" + host.getMeasuredWidth() + ", " + host.getMeasuredHeight() + ")");
            }
            Trace.traceBegin(8, TtmlUtils.TAG_LAYOUT);
            try {
                host.layout(0, 0, host.getMeasuredWidth(), host.getMeasuredHeight());
                this.mInLayout = false;
                if (this.mLayoutRequesters.size() > 0 && (validLayoutRequesters = getValidLayoutRequesters(this.mLayoutRequesters, false)) != null) {
                    this.mHandlingLayoutInLayoutRequest = true;
                    int numValidRequests = validLayoutRequesters.size();
                    for (int i = 0; i < numValidRequests; i++) {
                        View view = validLayoutRequesters.get(i);
                        if (DEBUG_LAYOUT) {
                            Log.w("View", "requestLayout() improperly called by " + view + " during layout: running second layout pass");
                        }
                        view.requestLayout();
                    }
                    measureHierarchy(host, lp, this.mView.getContext().getResources(), desiredWindowWidth, desiredWindowHeight);
                    this.mInLayout = true;
                    host.layout(0, 0, host.getMeasuredWidth(), host.getMeasuredHeight());
                    this.mHandlingLayoutInLayoutRequest = false;
                    final ArrayList<View> validLayoutRequesters2 = getValidLayoutRequesters(this.mLayoutRequesters, true);
                    if (validLayoutRequesters2 != null) {
                        getRunQueue().post(new Runnable() {
                            /* class android.view.ViewRootImpl.AnonymousClass2 */

                            public void run() {
                                int numValidRequests = validLayoutRequesters2.size();
                                for (int i = 0; i < numValidRequests; i++) {
                                    View view = (View) validLayoutRequesters2.get(i);
                                    if (ViewRootImpl.DEBUG_PANIC) {
                                        Log.w("View", "requestLayout() improperly called by " + view + " during second layout pass: posting in next frame");
                                    }
                                    view.requestLayout();
                                }
                            }
                        });
                    }
                }
                Trace.traceEnd(8);
                this.mInLayout = false;
            } catch (Throwable th) {
                Trace.traceEnd(8);
                throw th;
            }
        }
    }

    private ArrayList<View> getValidLayoutRequesters(ArrayList<View> layoutRequesters, boolean secondLayoutRequests) {
        int numViewsRequestingLayout = layoutRequesters.size();
        ArrayList<View> validLayoutRequesters = null;
        for (int i = 0; i < numViewsRequestingLayout; i++) {
            View view = layoutRequesters.get(i);
            if (!(view == null || view.mAttachInfo == null || view.mParent == null || !(secondLayoutRequests || (view.mPrivateFlags & 4096) == 4096))) {
                boolean gone = false;
                View parent = view;
                while (true) {
                    if (parent == null) {
                        break;
                    } else if ((parent.mViewFlags & 12) == 8) {
                        gone = true;
                        break;
                    } else if (parent.mParent instanceof View) {
                        parent = (View) parent.mParent;
                    } else {
                        parent = null;
                    }
                }
                if (!gone) {
                    if (validLayoutRequesters == null) {
                        validLayoutRequesters = new ArrayList<>();
                    }
                    validLayoutRequesters.add(view);
                }
            }
        }
        if (!secondLayoutRequests) {
            for (int i2 = 0; i2 < numViewsRequestingLayout; i2++) {
                View view2 = layoutRequesters.get(i2);
                while (view2 != null && (view2.mPrivateFlags & 4096) != 0) {
                    view2.mPrivateFlags &= -4097;
                    if (view2.mParent instanceof View) {
                        view2 = (View) view2.mParent;
                    } else {
                        view2 = null;
                    }
                }
            }
        }
        layoutRequesters.clear();
        return validLayoutRequesters;
    }

    @Override // android.view.ViewParent
    public void requestTransparentRegion(View child) {
        checkThread();
        View view = this.mView;
        if (view == child) {
            view.mPrivateFlags |= 512;
            this.mWindowAttributesChanged = true;
            this.mWindowAttributesChangesFlag = 0;
            requestLayout();
        }
    }

    private static int getRootMeasureSpec(int windowSize, int rootDimension) {
        if (rootDimension == -2) {
            return View.MeasureSpec.makeMeasureSpec(windowSize, Integer.MIN_VALUE);
        }
        if (rootDimension != -1) {
            return View.MeasureSpec.makeMeasureSpec(rootDimension, 1073741824);
        }
        return View.MeasureSpec.makeMeasureSpec(windowSize, 1073741824);
    }

    @Override // android.view.ThreadedRenderer.DrawCallbacks
    public void onPreDraw(RecordingCanvas canvas) {
        if (!(this.mCurScrollY == 0 || this.mHardwareYOffset == 0 || !this.mAttachInfo.mThreadedRenderer.isOpaque())) {
            canvas.drawColor(-16777216);
        }
        canvas.translate((float) (-this.mHardwareXOffset), (float) (-this.mHardwareYOffset));
    }

    @Override // android.view.ThreadedRenderer.DrawCallbacks
    public void onPostDraw(RecordingCanvas canvas) {
        drawAccessibilityFocusedDrawableIfNeeded(canvas);
        if (this.mUseMTRenderer) {
            for (int i = this.mWindowCallbacks.size() - 1; i >= 0; i--) {
                this.mWindowCallbacks.get(i).onPostDraw(canvas);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void outputDisplayList(View view) {
        view.mRenderNode.output();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void profileRendering(boolean enabled) {
        if (this.mProfileRendering) {
            this.mRenderProfilingEnabled = enabled;
            Choreographer.FrameCallback frameCallback = this.mRenderProfiler;
            if (frameCallback != null) {
                this.mChoreographer.removeFrameCallback(frameCallback);
            }
            if (this.mRenderProfilingEnabled) {
                if (this.mRenderProfiler == null) {
                    this.mRenderProfiler = new Choreographer.FrameCallback() {
                        /* class android.view.ViewRootImpl.AnonymousClass3 */

                        @Override // android.view.Choreographer.FrameCallback
                        public void doFrame(long frameTimeNanos) {
                            ViewRootImpl.this.mDirty.set(0, 0, ViewRootImpl.this.mWidth, ViewRootImpl.this.mHeight);
                            ViewRootImpl.this.scheduleTraversals();
                            if (ViewRootImpl.this.mRenderProfilingEnabled) {
                                ViewRootImpl.this.mChoreographer.postFrameCallback(ViewRootImpl.this.mRenderProfiler);
                            }
                        }
                    };
                }
                this.mChoreographer.postFrameCallback(this.mRenderProfiler);
                return;
            }
            this.mRenderProfiler = null;
        }
    }

    private void trackFPS() {
        long nowTime = System.currentTimeMillis();
        if (this.mFpsStartTime < 0) {
            this.mFpsPrevTime = nowTime;
            this.mFpsStartTime = nowTime;
            this.mFpsNumFrames = 0;
            return;
        }
        this.mFpsNumFrames++;
        String thisHash = Integer.toHexString(System.identityHashCode(this));
        long totalTime = nowTime - this.mFpsStartTime;
        Log.v(this.mTag, "0x" + thisHash + "\tFrame time:\t" + (nowTime - this.mFpsPrevTime));
        this.mFpsPrevTime = nowTime;
        if (totalTime > 1000) {
            float fps = (((float) this.mFpsNumFrames) * 1000.0f) / ((float) totalTime);
            Log.v(this.mTag, "0x" + thisHash + "\tFPS:\t" + fps);
            this.mFpsStartTime = nowTime;
            this.mFpsNumFrames = 0;
        }
    }

    /* access modifiers changed from: package-private */
    public void drawPending() {
        this.mDrawsNeededToReport++;
    }

    /* access modifiers changed from: package-private */
    public void pendingDrawFinished() {
        int i = this.mDrawsNeededToReport;
        if (i != 0) {
            this.mDrawsNeededToReport = i - 1;
            if (this.mDrawsNeededToReport == 0) {
                reportDrawFinished();
                return;
            }
            return;
        }
        throw new RuntimeException("Unbalanced drawPending/pendingDrawFinished calls");
    }

    /* access modifiers changed from: private */
    public void postDrawFinished() {
        this.mHandler.sendEmptyMessage(29);
    }

    private void reportDrawFinished() {
        if (Trace.isTagEnabled(8)) {
            Trace.traceBegin(8, "finish draw");
        }
        try {
            this.mDrawsNeededToReport = 0;
            this.mWindowSession.finishDrawing(this.mWindow);
            this.mManager.refreshForceDark(this.mView);
        } catch (RemoteException e) {
            if (ViewDebugManager.DEBUG_DRAW) {
                String str = this.mTag;
                Log.d(str, "Exception when finish draw window " + this.mWindow + " in " + this, e);
            }
        }
        if (Trace.isTagEnabled(8)) {
            Trace.traceEnd(8);
        }
    }

    /* JADX INFO: finally extract failed */
    private void performDraw() {
        if ((this.mAttachInfo.mDisplayState != 1 || this.mReportNextDraw) && this.mView != null) {
            boolean fullRedrawNeeded = this.mFullRedrawNeeded || this.mReportNextDraw;
            this.mFullRedrawNeeded = false;
            this.mIsDrawing = true;
            Trace.traceBegin(8, "draw");
            boolean usingAsyncReport = false;
            if (this.mAttachInfo.mThreadedRenderer != null && this.mAttachInfo.mThreadedRenderer.isEnabled()) {
                ArrayList<Runnable> commitCallbacks = this.mAttachInfo.mTreeObserver.captureFrameCommitCallbacks();
                if (this.mReportNextDraw) {
                    usingAsyncReport = true;
                    this.mAttachInfo.mThreadedRenderer.setFrameCompleteCallback(new HardwareRenderer.FrameCompleteCallback(this.mAttachInfo.mHandler, commitCallbacks) {
                        /* class android.view.$$Lambda$ViewRootImpl$YBiqAhbCbXVPSKdbE3K4rH2gpxI */
                        private final /* synthetic */ Handler f$1;
                        private final /* synthetic */ ArrayList f$2;

                        {
                            this.f$1 = r2;
                            this.f$2 = r3;
                        }

                        @Override // android.graphics.HardwareRenderer.FrameCompleteCallback
                        public final void onFrameComplete(long j) {
                            ViewRootImpl.this.lambda$performDraw$2$ViewRootImpl(this.f$1, this.f$2, j);
                        }
                    });
                } else if (commitCallbacks != null && commitCallbacks.size() > 0) {
                    this.mAttachInfo.mThreadedRenderer.setFrameCompleteCallback(new HardwareRenderer.FrameCompleteCallback(commitCallbacks) {
                        /* class android.view.$$Lambda$ViewRootImpl$zlBUjCwDtoAWMNaHI62DIqeKFY */
                        private final /* synthetic */ ArrayList f$1;

                        {
                            this.f$1 = r2;
                        }

                        @Override // android.graphics.HardwareRenderer.FrameCompleteCallback
                        public final void onFrameComplete(long j) {
                            Handler.this.postAtFrontOfQueue(new Runnable(this.f$1) {
                                /* class android.view.$$Lambda$ViewRootImpl$dgEKMWLAJVMlaVy41safRlNQBo */
                                private final /* synthetic */ ArrayList f$0;

                                {
                                    this.f$0 = r1;
                                }

                                public final void run() {
                                    ViewRootImpl.lambda$performDraw$3(this.f$0);
                                }
                            });
                        }
                    });
                }
            }
            try {
                boolean canUseAsync = draw(fullRedrawNeeded);
                if (usingAsyncReport && !canUseAsync) {
                    if (this.mAttachInfo.mThreadedRenderer != null) {
                        this.mAttachInfo.mThreadedRenderer.setFrameCompleteCallback(null);
                    }
                    usingAsyncReport = false;
                }
                this.mIsDrawing = false;
                Trace.traceEnd(8);
                if (this.mAttachInfo.mPendingAnimatingRenderNodes != null) {
                    int count = this.mAttachInfo.mPendingAnimatingRenderNodes.size();
                    for (int i = 0; i < count; i++) {
                        this.mAttachInfo.mPendingAnimatingRenderNodes.get(i).endAllAnimators();
                    }
                    this.mAttachInfo.mPendingAnimatingRenderNodes.clear();
                }
                if (this.mReportNextDraw) {
                    this.mReportNextDraw = false;
                    CountDownLatch countDownLatch = this.mWindowDrawCountDown;
                    if (countDownLatch != null) {
                        try {
                            countDownLatch.await();
                        } catch (InterruptedException e) {
                            Log.e(this.mTag, "Window redraw count down interrupted!");
                        }
                        this.mWindowDrawCountDown = null;
                    }
                    if (this.mAttachInfo.mThreadedRenderer != null) {
                        this.mAttachInfo.mThreadedRenderer.setStopped(this.mStopped);
                    }
                    if (LOCAL_LOGV || DEBUG_DRAW) {
                        String str = this.mTag;
                        Log.v(str, "FINISHED DRAWING: " + ((Object) this.mWindowAttributes.getTitle()));
                    }
                    if (this.mSurfaceHolder != null && this.mSurface.isValid()) {
                        new SurfaceCallbackHelper(new Runnable() {
                            /* class android.view.$$Lambda$ViewRootImpl$dznxCZGM2R1fsBljsJKomLjBRoM */

                            public final void run() {
                                ViewRootImpl.this.postDrawFinished();
                            }
                        }).dispatchSurfaceRedrawNeededAsync(this.mSurfaceHolder, this.mSurfaceHolder.getCallbacks());
                    } else if (!usingAsyncReport) {
                        if (this.mAttachInfo.mThreadedRenderer != null) {
                            this.mAttachInfo.mThreadedRenderer.fence();
                        }
                        pendingDrawFinished();
                    }
                }
            } catch (Throwable th) {
                this.mIsDrawing = false;
                Trace.traceEnd(8);
                throw th;
            }
        }
    }

    public /* synthetic */ void lambda$performDraw$2$ViewRootImpl(Handler handler, ArrayList commitCallbacks, long frameNr) {
        handler.postAtFrontOfQueue(new Runnable(commitCallbacks) {
            /* class android.view.$$Lambda$ViewRootImpl$7A_3tkr_Kw4TZAeIUGVlOoTcZhg */
            private final /* synthetic */ ArrayList f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                ViewRootImpl.this.lambda$performDraw$1$ViewRootImpl(this.f$1);
            }
        });
    }

    public /* synthetic */ void lambda$performDraw$1$ViewRootImpl(ArrayList commitCallbacks) {
        pendingDrawFinished();
        if (commitCallbacks != null) {
            for (int i = 0; i < commitCallbacks.size(); i++) {
                ((Runnable) commitCallbacks.get(i)).run();
            }
        }
    }

    static /* synthetic */ void lambda$performDraw$3(ArrayList commitCallbacks) {
        for (int i = 0; i < commitCallbacks.size(); i++) {
            ((Runnable) commitCallbacks.get(i)).run();
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:101:0x0236, code lost:
        if (r26.mHardwareXOffset != r3) goto L_0x024c;
     */
    /* JADX WARNING: Removed duplicated region for block: B:106:0x0253  */
    /* JADX WARNING: Removed duplicated region for block: B:109:0x0267  */
    /* JADX WARNING: Removed duplicated region for block: B:112:0x0272 A[Catch:{ all -> 0x0299 }] */
    /* JADX WARNING: Removed duplicated region for block: B:114:0x0276 A[Catch:{ all -> 0x0299 }] */
    /* JADX WARNING: Removed duplicated region for block: B:81:0x01fc A[SYNTHETIC, Splitter:B:81:0x01fc] */
    /* JADX WARNING: Removed duplicated region for block: B:99:0x0234 A[SYNTHETIC, Splitter:B:99:0x0234] */
    private boolean draw(boolean fullRedrawNeeded) {
        int curScrollY;
        boolean fullRedrawNeeded2;
        int xOffset;
        boolean accessibilityFocusDirty;
        Surface.OutOfResourcesException e;
        WindowManager.LayoutParams params;
        int xOffset2;
        Surface.OutOfResourcesException e2;
        boolean invalidateRoot;
        boolean updated;
        int yOffset;
        Scroller scroller;
        Surface surface = this.mSurface;
        if (!surface.isValid()) {
            if (DEBUG_PANIC) {
                Log.d(this.mTag, " draw  --mSurface release,nothing draw. this=" + this);
            }
            return false;
        }
        if (DEBUG_FPS) {
            trackFPS();
        }
        if (!sFirstDrawComplete) {
            synchronized (sFirstDrawHandlers) {
                sFirstDrawComplete = true;
                int count = sFirstDrawHandlers.size();
                for (int i = 0; i < count; i++) {
                    this.mHandler.post(sFirstDrawHandlers.get(i));
                }
            }
        }
        Rect surfaceInsets = null;
        scrollToRectOrFocus(null, false);
        if (this.mAttachInfo.mViewScrollChanged) {
            View.AttachInfo attachInfo = this.mAttachInfo;
            attachInfo.mViewScrollChanged = false;
            attachInfo.mTreeObserver.dispatchOnScrollChanged();
        }
        Scroller scroller2 = this.mScroller;
        boolean animating = scroller2 != null && scroller2.computeScrollOffset();
        if (animating) {
            curScrollY = this.mScroller.getCurrY();
        } else {
            curScrollY = this.mScrollY;
        }
        if (this.mCurScrollY != curScrollY) {
            this.mCurScrollY = curScrollY;
            View view = this.mView;
            if (view instanceof RootViewSurfaceTaker) {
                ((RootViewSurfaceTaker) view).onRootViewScrollYChanged(this.mCurScrollY);
            }
            fullRedrawNeeded2 = true;
        } else {
            fullRedrawNeeded2 = fullRedrawNeeded;
        }
        float appScale = this.mAttachInfo.mApplicationScale;
        boolean scalingRequired = this.mAttachInfo.mScalingRequired;
        Rect dirty = this.mDirty;
        if (this.mSurfaceHolder != null) {
            dirty.setEmpty();
            if (animating && (scroller = this.mScroller) != null) {
                scroller.abortAnimation();
            }
            return false;
        }
        if (fullRedrawNeeded2) {
            dirty.set(0, 0, (int) ((((float) this.mWidth) * appScale) + 0.5f), (int) ((((float) this.mHeight) * appScale) + 0.5f));
        }
        if (DEBUG_ORIENTATION || DEBUG_DRAW) {
            Log.v(this.mTag, "Draw " + this.mView + "/" + ((Object) this.mWindowAttributes.getTitle()) + ": dirty={" + dirty.left + SmsManager.REGEX_PREFIX_DELIMITER + dirty.top + SmsManager.REGEX_PREFIX_DELIMITER + dirty.right + SmsManager.REGEX_PREFIX_DELIMITER + dirty.bottom + "} surface=" + surface + " surface.isValid()=" + surface.isValid() + ", appScale = " + appScale + ", width=" + this.mWidth + ", height=" + this.mHeight + ", mScrollY = " + this.mScrollY + ", mCurScrollY = " + this.mCurScrollY + ", animating = " + animating + ", mIsAnimating = " + this.mIsAnimating + ", this = " + this);
        }
        this.mAttachInfo.mTreeObserver.dispatchOnDraw();
        int xOffset3 = -this.mCanvasOffsetX;
        int yOffset2 = (-this.mCanvasOffsetY) + curScrollY;
        WindowManager.LayoutParams params2 = this.mWindowAttributes;
        if (params2 != null) {
            surfaceInsets = params2.surfaceInsets;
        }
        if (surfaceInsets != null) {
            int xOffset4 = xOffset3 - surfaceInsets.left;
            yOffset2 -= surfaceInsets.top;
            dirty.offset(surfaceInsets.left, surfaceInsets.right);
            xOffset = xOffset4;
        } else {
            xOffset = xOffset3;
        }
        Drawable drawable = this.mAttachInfo.mAccessibilityFocusDrawable;
        if (drawable != null) {
            Rect bounds = this.mAttachInfo.mTmpInvalRect;
            if (!getAccessibilityFocusedRect(bounds)) {
                bounds.setEmpty();
            }
            if (!bounds.equals(drawable.getBounds())) {
                accessibilityFocusDirty = true;
                this.mAttachInfo.mDrawingTime = this.mChoreographer.getFrameTimeNanos() / TimeUtils.NANOS_PER_MS;
                boolean useAsyncReport = false;
                if (dirty.isEmpty() || this.mIsAnimating || accessibilityFocusDirty) {
                    synchronized (this.mDestroyLock) {
                        try {
                            if (this.mAttachInfo.mThreadedRenderer != null) {
                                try {
                                    if (this.mAttachInfo.mThreadedRenderer.isEnabled()) {
                                        if (!accessibilityFocusDirty) {
                                            try {
                                                if (!this.mInvalidateRootRequested) {
                                                    invalidateRoot = false;
                                                    this.mInvalidateRootRequested = false;
                                                    this.mIsAnimating = false;
                                                    if (this.mHardwareYOffset == yOffset2) {
                                                        try {
                                                        } catch (Throwable th) {
                                                            e = th;
                                                            throw e;
                                                        }
                                                    }
                                                    this.mHardwareYOffset = yOffset2;
                                                    this.mHardwareXOffset = xOffset;
                                                    invalidateRoot = true;
                                                    if (invalidateRoot) {
                                                        this.mAttachInfo.mThreadedRenderer.invalidateRoot();
                                                    }
                                                    dirty.setEmpty();
                                                    updated = updateContentDrawBounds();
                                                    if (!this.mReportNextDraw) {
                                                        yOffset = yOffset2;
                                                        try {
                                                            this.mAttachInfo.mThreadedRenderer.setStopped(false);
                                                        } catch (Throwable th2) {
                                                            e = th2;
                                                            throw e;
                                                        }
                                                    } else {
                                                        yOffset = yOffset2;
                                                    }
                                                    if (updated) {
                                                        requestDrawWindow();
                                                    }
                                                    useAsyncReport = true;
                                                    this.mAttachInfo.mThreadedRenderer.draw(this.mView, this.mAttachInfo, this);
                                                }
                                            } catch (Throwable th3) {
                                                e = th3;
                                                throw e;
                                            }
                                        }
                                        invalidateRoot = true;
                                        try {
                                            this.mInvalidateRootRequested = false;
                                            this.mIsAnimating = false;
                                            if (this.mHardwareYOffset == yOffset2) {
                                            }
                                            this.mHardwareYOffset = yOffset2;
                                            this.mHardwareXOffset = xOffset;
                                            invalidateRoot = true;
                                            if (invalidateRoot) {
                                            }
                                            dirty.setEmpty();
                                            updated = updateContentDrawBounds();
                                            if (!this.mReportNextDraw) {
                                            }
                                            if (updated) {
                                            }
                                            useAsyncReport = true;
                                            this.mAttachInfo.mThreadedRenderer.draw(this.mView, this.mAttachInfo, this);
                                        } catch (Throwable th4) {
                                            e = th4;
                                            throw e;
                                        }
                                    }
                                } catch (Throwable th5) {
                                    e = th5;
                                    throw e;
                                }
                            }
                            try {
                                if (this.mAttachInfo.mThreadedRenderer != null) {
                                    try {
                                        if (this.mAttachInfo.mThreadedRenderer.isEnabled()) {
                                            xOffset2 = xOffset;
                                            params = params2;
                                        } else if (!this.mAttachInfo.mThreadedRenderer.isRequested()) {
                                            xOffset2 = xOffset;
                                            params = params2;
                                        } else if (this.mSurface.isValid()) {
                                            try {
                                            } catch (Surface.OutOfResourcesException e3) {
                                                e2 = e3;
                                                handleOutOfResourcesException(e2);
                                                return false;
                                            }
                                            try {
                                                try {
                                                    this.mAttachInfo.mThreadedRenderer.initializeIfNeeded(this.mWidth, this.mHeight, this.mAttachInfo, this.mSurface, surfaceInsets);
                                                    this.mFullRedrawNeeded = true;
                                                    scheduleTraversals();
                                                    return false;
                                                } catch (Surface.OutOfResourcesException e4) {
                                                    e2 = e4;
                                                    handleOutOfResourcesException(e2);
                                                    return false;
                                                } catch (Throwable th6) {
                                                    e = th6;
                                                    throw e;
                                                }
                                            } catch (Surface.OutOfResourcesException e5) {
                                                e2 = e5;
                                                handleOutOfResourcesException(e2);
                                                return false;
                                            } catch (Throwable th7) {
                                                e = th7;
                                                throw e;
                                            }
                                        } else {
                                            xOffset2 = xOffset;
                                            params = params2;
                                        }
                                    } catch (Throwable th8) {
                                        e = th8;
                                        throw e;
                                    }
                                } else {
                                    xOffset2 = xOffset;
                                    params = params2;
                                }
                                try {
                                } catch (Throwable th9) {
                                    e = th9;
                                    throw e;
                                }
                                try {
                                    if (!drawSoftware(surface, this.mAttachInfo, xOffset2, yOffset2, scalingRequired, dirty, surfaceInsets)) {
                                        if (DEBUG_DRAW) {
                                            Log.v(this.mTag, "drawSoftware return: this = " + this);
                                        }
                                        return false;
                                    }
                                } catch (Throwable th10) {
                                    e = th10;
                                    throw e;
                                }
                            } catch (Throwable th11) {
                                e = th11;
                                throw e;
                            }
                        } catch (Throwable th12) {
                            e = th12;
                            throw e;
                        }
                    }
                }
                if (animating) {
                    this.mFullRedrawNeeded = true;
                    scheduleTraversals();
                }
                return useAsyncReport;
            }
        }
        accessibilityFocusDirty = false;
        this.mAttachInfo.mDrawingTime = this.mChoreographer.getFrameTimeNanos() / TimeUtils.NANOS_PER_MS;
        boolean useAsyncReport2 = false;
        if (dirty.isEmpty()) {
        }
        synchronized (this.mDestroyLock) {
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:30:0x00c2 A[Catch:{ all -> 0x0195 }] */
    /* JADX WARNING: Removed duplicated region for block: B:33:0x010d A[Catch:{ all -> 0x0195 }] */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x0114 A[Catch:{ all -> 0x0195 }] */
    /* JADX WARNING: Removed duplicated region for block: B:36:0x0117 A[Catch:{ all -> 0x0195 }] */
    /* JADX WARNING: Removed duplicated region for block: B:39:0x0127 A[Catch:{ all -> 0x0195 }] */
    private boolean drawSoftware(Surface surface, View.AttachInfo attachInfo, int xoff, int yoff, boolean scalingRequired, Rect dirty, Rect surfaceInsets) {
        int dirtyYOffset;
        int dirtyYOffset2;
        String str;
        String str2;
        String str3;
        IllegalArgumentException e;
        String str4;
        StringBuilder sb;
        boolean z;
        if (surfaceInsets != null) {
            int dirtyXOffset = xoff + surfaceInsets.left;
            dirtyYOffset = yoff + surfaceInsets.top;
            dirtyYOffset2 = dirtyXOffset;
        } else {
            dirtyYOffset = yoff;
            dirtyYOffset2 = xoff;
        }
        try {
            dirty.offset(-dirtyYOffset2, -dirtyYOffset);
            int i = dirty.left;
            int i2 = dirty.top;
            int i3 = dirty.right;
            int left = dirty.bottom;
            if (Trace.isTagEnabled(8)) {
                str2 = ", canvas = ";
                str = "Could not unlock surface, surface = ";
                Trace.traceBegin(8, "drawSoftware lockCanvas");
            } else {
                str2 = ", canvas = ";
                str = "Could not unlock surface, surface = ";
            }
            Canvas canvas = this.mSurface.lockCanvas(dirty);
            if (Trace.isTagEnabled(8)) {
                Trace.traceEnd(8);
            }
            canvas.setDensity(this.mDensity);
            dirty.offset(dirtyYOffset2, dirtyYOffset);
            try {
                if (DEBUG_ORIENTATION || DEBUG_DRAW) {
                    Log.v(this.mTag, "Surface " + surface + " drawing to bitmap w=" + canvas.getWidth() + ", h=" + canvas.getHeight());
                }
                if (canvas.isOpaque() && yoff == 0) {
                    if (xoff == 0) {
                        z = false;
                        dirty.setEmpty();
                        this.mIsAnimating = z;
                        this.mView.mPrivateFlags |= 32;
                        if (DEBUG_DRAW) {
                            Context cxt = this.mView.getContext();
                            Log.i(this.mTag, "Drawing: package:" + cxt.getPackageName() + ", metrics=" + cxt.getResources().getDisplayMetrics() + ", compatibilityInfo=" + cxt.getResources().getCompatibilityInfo());
                        }
                        canvas.translate((float) (-xoff), (float) (-yoff));
                        if (this.mTranslator != null) {
                            this.mTranslator.translateCanvas(canvas);
                        }
                        canvas.setScreenDensity(!scalingRequired ? this.mNoncompatDensity : 0);
                        this.mView.draw(canvas);
                        drawAccessibilityFocusedDrawableIfNeeded(canvas);
                        if (DEBUG_DRAW) {
                            Log.v(this.mTag, "Drawing view end- : mView = " + this.mView + ", this = " + this);
                        }
                        surface.unlockCanvasAndPost(canvas);
                        if (LOCAL_LOGV && !DEBUG_DRAW) {
                            return true;
                        }
                        Log.v(this.mTag, "Surface " + surface + " unlockCanvasAndPost");
                        return true;
                    }
                }
                z = false;
                canvas.drawColor(0, PorterDuff.Mode.CLEAR);
                dirty.setEmpty();
                this.mIsAnimating = z;
                this.mView.mPrivateFlags |= 32;
                if (DEBUG_DRAW) {
                }
                canvas.translate((float) (-xoff), (float) (-yoff));
                if (this.mTranslator != null) {
                }
                canvas.setScreenDensity(!scalingRequired ? this.mNoncompatDensity : 0);
                this.mView.draw(canvas);
                drawAccessibilityFocusedDrawableIfNeeded(canvas);
                if (DEBUG_DRAW) {
                }
                try {
                    surface.unlockCanvasAndPost(canvas);
                    if (LOCAL_LOGV) {
                    }
                    Log.v(this.mTag, "Surface " + surface + " unlockCanvasAndPost");
                    return true;
                } catch (IllegalArgumentException e2) {
                    e = e2;
                    str4 = this.mTag;
                    sb = new StringBuilder();
                    sb.append(str);
                    sb.append(surface);
                    str3 = str2;
                    sb.append(str3);
                    sb.append(canvas);
                    sb.append(", this = ");
                    sb.append(this);
                    Log.e(str4, sb.toString(), e);
                    this.mLayoutRequested = true;
                    return false;
                }
            } catch (Throwable e3) {
                str3 = str2;
                try {
                    surface.unlockCanvasAndPost(canvas);
                    if (LOCAL_LOGV || DEBUG_DRAW) {
                        Log.v(this.mTag, "Surface " + surface + " unlockCanvasAndPost");
                    }
                    throw e3;
                } catch (IllegalArgumentException e4) {
                    e = e4;
                    str4 = this.mTag;
                    sb = new StringBuilder();
                    sb.append(str);
                    sb.append(surface);
                    sb.append(str3);
                    sb.append(canvas);
                    sb.append(", this = ");
                    sb.append(this);
                    Log.e(str4, sb.toString(), e);
                    this.mLayoutRequested = true;
                    return false;
                }
            }
        } catch (Surface.OutOfResourcesException e5) {
            handleOutOfResourcesException(e5);
            dirty.offset(dirtyYOffset2, dirtyYOffset);
            return false;
        } catch (IllegalArgumentException e6) {
            Log.e(this.mTag, "Could not lock surface", e6);
            this.mLayoutRequested = true;
            dirty.offset(dirtyYOffset2, dirtyYOffset);
            return false;
        } catch (Throwable e7) {
            dirty.offset(dirtyYOffset2, dirtyYOffset);
            throw e7;
        }
    }

    private void drawAccessibilityFocusedDrawableIfNeeded(Canvas canvas) {
        Rect bounds = this.mAttachInfo.mTmpInvalRect;
        if (getAccessibilityFocusedRect(bounds)) {
            Drawable drawable = getAccessibilityFocusedDrawable();
            if (drawable != null) {
                drawable.setBounds(bounds);
                drawable.draw(canvas);
            }
        } else if (this.mAttachInfo.mAccessibilityFocusDrawable != null) {
            this.mAttachInfo.mAccessibilityFocusDrawable.setBounds(0, 0, 0, 0);
        }
    }

    @OppoHook(level = OppoHook.OppoHookType.CHANGE_CODE, note = "XiaoKang.Feng@ROM.SDK : Modify for direct", property = OppoHook.OppoRomType.ROM)
    private boolean getAccessibilityFocusedRect(Rect bounds) {
        View host;
        AccessibilityManager manager = AccessibilityManager.getInstance(this.mView.mContext);
        if (!manager.isEnabled() || !manager.isOppoTouchExplorationEnabled() || (host = this.mAccessibilityFocusedHost) == null || host.mAttachInfo == null) {
            return false;
        }
        if (host.getAccessibilityNodeProvider() == null) {
            host.getBoundsOnScreen(bounds, true);
        } else {
            AccessibilityNodeInfo accessibilityNodeInfo = this.mAccessibilityFocusedVirtualView;
            if (accessibilityNodeInfo == null) {
                return false;
            }
            accessibilityNodeInfo.getBoundsInScreen(bounds);
        }
        View.AttachInfo attachInfo = this.mAttachInfo;
        bounds.offset(0, attachInfo.mViewRootImpl.mScrollY);
        bounds.offset(-attachInfo.mWindowLeft, -attachInfo.mWindowTop);
        if (!bounds.intersect(0, 0, attachInfo.mViewRootImpl.mWidth, attachInfo.mViewRootImpl.mHeight)) {
            bounds.setEmpty();
        }
        return !bounds.isEmpty();
    }

    private Drawable getAccessibilityFocusedDrawable() {
        if (this.mAttachInfo.mAccessibilityFocusDrawable == null) {
            TypedValue value = new TypedValue();
            if (this.mView.mContext.getTheme().resolveAttribute(R.attr.accessibilityFocusedDrawable, value, true)) {
                this.mAttachInfo.mAccessibilityFocusDrawable = this.mView.mContext.getDrawable(value.resourceId);
            }
        }
        return this.mAttachInfo.mAccessibilityFocusDrawable;
    }

    /* access modifiers changed from: package-private */
    public void updateSystemGestureExclusionRectsForView(View view) {
        this.mGestureExclusionTracker.updateRectsForView(view);
        this.mHandler.sendEmptyMessage(32);
    }

    /* access modifiers changed from: package-private */
    public void systemGestureExclusionChanged() {
        List<Rect> rectsForWindowManager = this.mGestureExclusionTracker.computeChangedRects();
        if (rectsForWindowManager != null && this.mView != null) {
            try {
                this.mWindowSession.reportSystemGestureExclusionChanged(this.mWindow, rectsForWindowManager);
                this.mAttachInfo.mTreeObserver.dispatchOnSystemGestureExclusionRectsChanged(rectsForWindowManager);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public void setRootSystemGestureExclusionRects(List<Rect> rects) {
        this.mGestureExclusionTracker.setRootSystemGestureExclusionRects(rects);
        this.mHandler.sendEmptyMessage(32);
    }

    public List<Rect> getRootSystemGestureExclusionRects() {
        return this.mGestureExclusionTracker.getRootSystemGestureExclusionRects();
    }

    public void requestInvalidateRootRenderNode() {
        this.mInvalidateRootRequested = true;
    }

    /* access modifiers changed from: package-private */
    public boolean scrollToRectOrFocus(Rect rectangle, boolean immediate) {
        int scrollY;
        Rect ci = this.mAttachInfo.mContentInsets;
        Rect vi = this.mAttachInfo.mVisibleInsets;
        int scrollY2 = 0;
        boolean handled = false;
        if (vi.left > ci.left || vi.top > ci.top || vi.right > ci.right || vi.bottom > ci.bottom) {
            scrollY2 = this.mScrollY;
            View focus = this.mView.findFocus();
            if (focus == null) {
                return false;
            }
            WeakReference<View> weakReference = this.mLastScrolledFocus;
            View lastScrolledFocus = weakReference != null ? weakReference.get() : null;
            if (focus != lastScrolledFocus) {
                rectangle = null;
            }
            if (DEBUG_INPUT_RESIZE) {
                String str = this.mTag;
                Log.v(str, "Eval scroll: focus=" + focus + " rectangle=" + rectangle + " ci=" + ci + " vi=" + vi);
            }
            if (focus != lastScrolledFocus || this.mScrollMayChange || rectangle != null) {
                this.mLastScrolledFocus = new WeakReference<>(focus);
                this.mScrollMayChange = false;
                if (DEBUG_INPUT_RESIZE) {
                    Log.v(this.mTag, "Need to scroll?");
                }
                if (focus.getGlobalVisibleRect(this.mVisRect, null)) {
                    if (DEBUG_INPUT_RESIZE) {
                        String str2 = this.mTag;
                        Log.v(str2, "Root w=" + this.mView.getWidth() + " h=" + this.mView.getHeight() + " ci=" + ci.toShortString() + " vi=" + vi.toShortString());
                    }
                    if (rectangle == null) {
                        focus.getFocusedRect(this.mTempRect);
                        if (DEBUG_INPUT_RESIZE) {
                            String str3 = this.mTag;
                            Log.v(str3, "Focus " + focus + ": focusRect=" + this.mTempRect.toShortString());
                        }
                        View view = this.mView;
                        if (view instanceof ViewGroup) {
                            ((ViewGroup) view).offsetDescendantRectToMyCoords(focus, this.mTempRect);
                        }
                        if (DEBUG_INPUT_RESIZE) {
                            String str4 = this.mTag;
                            Log.v(str4, "Focus in window: focusRect=" + this.mTempRect.toShortString() + " visRect=" + this.mVisRect.toShortString());
                        }
                    } else {
                        this.mTempRect.set(rectangle);
                        if (DEBUG_INPUT_RESIZE) {
                            String str5 = this.mTag;
                            Log.v(str5, "Request scroll to rect: " + this.mTempRect.toShortString() + " visRect=" + this.mVisRect.toShortString());
                        }
                    }
                    if (this.mTempRect.intersect(this.mVisRect)) {
                        if (DEBUG_INPUT_RESIZE) {
                            String str6 = this.mTag;
                            Log.v(str6, "Focus window visible rect: " + this.mTempRect.toShortString());
                        }
                        if (this.mTempRect.height() <= (this.mView.getHeight() - vi.top) - vi.bottom) {
                            if (this.mTempRect.top < vi.top) {
                                scrollY = this.mTempRect.top - vi.top;
                                if (DEBUG_INPUT_RESIZE) {
                                    String str7 = this.mTag;
                                    Log.v(str7, "Top covered; scrollY=" + scrollY);
                                }
                            } else if (this.mTempRect.bottom > this.mView.getHeight() - vi.bottom) {
                                scrollY = this.mTempRect.bottom - (this.mView.getHeight() - vi.bottom);
                                if (DEBUG_INPUT_RESIZE) {
                                    String str8 = this.mTag;
                                    Log.v(str8, "Bottom covered; scrollY=" + scrollY);
                                }
                            } else {
                                scrollY2 = 0;
                            }
                            scrollY2 = scrollY;
                        } else if (DEBUG_INPUT_RESIZE) {
                            String str9 = this.mTag;
                            Log.v(str9, "Too tall; leaving scrollY=" + scrollY2);
                        }
                        handled = true;
                    }
                }
            } else if (DEBUG_INPUT_RESIZE) {
                String str10 = this.mTag;
                Log.v(str10, "Keeping scroll y=" + this.mScrollY + " vi=" + vi.toShortString());
            }
        }
        if (scrollY2 != this.mScrollY) {
            if (DEBUG_INPUT_RESIZE) {
                String str11 = this.mTag;
                Log.v(str11, "Pan scroll changed: old=" + this.mScrollY + " , new=" + scrollY2);
            }
            if (!immediate) {
                if (this.mScroller == null) {
                    this.mScroller = new Scroller(this.mView.getContext());
                }
                Scroller scroller = this.mScroller;
                int i = this.mScrollY;
                scroller.startScroll(0, i, 0, scrollY2 - i);
            } else {
                Scroller scroller2 = this.mScroller;
                if (scroller2 != null) {
                    scroller2.abortAnimation();
                }
            }
            this.mScrollY = scrollY2;
        }
        return handled;
    }

    @UnsupportedAppUsage
    public View getAccessibilityFocusedHost() {
        return this.mAccessibilityFocusedHost;
    }

    @UnsupportedAppUsage
    public AccessibilityNodeInfo getAccessibilityFocusedVirtualView() {
        return this.mAccessibilityFocusedVirtualView;
    }

    /* access modifiers changed from: package-private */
    public void setAccessibilityFocus(View view, AccessibilityNodeInfo node) {
        if (this.mAccessibilityFocusedVirtualView != null) {
            AccessibilityNodeInfo focusNode = this.mAccessibilityFocusedVirtualView;
            View focusHost = this.mAccessibilityFocusedHost;
            this.mAccessibilityFocusedHost = null;
            this.mAccessibilityFocusedVirtualView = null;
            focusHost.clearAccessibilityFocusNoCallbacks(64);
            AccessibilityNodeProvider provider = focusHost.getAccessibilityNodeProvider();
            if (provider != null) {
                focusNode.getBoundsInParent(this.mTempRect);
                focusHost.invalidate(this.mTempRect);
                provider.performAction(AccessibilityNodeInfo.getVirtualDescendantId(focusNode.getSourceNodeId()), 128, null);
            }
            focusNode.recycle();
        }
        View view2 = this.mAccessibilityFocusedHost;
        if (!(view2 == null || view2 == view)) {
            view2.clearAccessibilityFocusNoCallbacks(64);
        }
        this.mAccessibilityFocusedHost = view;
        this.mAccessibilityFocusedVirtualView = node;
        if (this.mAttachInfo.mThreadedRenderer != null) {
            this.mAttachInfo.mThreadedRenderer.invalidateRoot();
        }
    }

    /* access modifiers changed from: package-private */
    public boolean hasPointerCapture() {
        return this.mPointerCapture;
    }

    /* access modifiers changed from: package-private */
    public void requestPointerCapture(boolean enabled) {
        if (this.mPointerCapture != enabled) {
            InputManager.getInstance().requestPointerCapture(this.mAttachInfo.mWindowToken, enabled);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handlePointerCaptureChanged(boolean hasCapture) {
        if (this.mPointerCapture != hasCapture) {
            this.mPointerCapture = hasCapture;
            View view = this.mView;
            if (view != null) {
                view.dispatchPointerCaptureChanged(hasCapture);
            }
        }
    }

    private boolean hasColorModeChanged(int colorMode) {
        if (this.mAttachInfo.mThreadedRenderer == null) {
            return false;
        }
        boolean isWideGamut = colorMode == 1;
        if (this.mAttachInfo.mThreadedRenderer.isWideGamut() == isWideGamut) {
            return false;
        }
        if (!isWideGamut || this.mContext.getResources().getConfiguration().isScreenWideColorGamut()) {
            return true;
        }
        return false;
    }

    @Override // android.view.ViewParent
    public void requestChildFocus(View child, View focused) {
        if (DEBUG_INPUT_RESIZE) {
            String str = this.mTag;
            Log.v(str, "Request child " + child + " focus: focus now " + focused + " in " + this);
        }
        checkThread();
        scheduleTraversals();
    }

    @Override // android.view.ViewParent
    public void clearChildFocus(View child) {
        if (DEBUG_INPUT_RESIZE) {
            Log.v(this.mTag, "Clearing child focus");
        }
        checkThread();
        scheduleTraversals();
    }

    @Override // android.view.ViewParent
    public ViewParent getParentForAccessibility() {
        return null;
    }

    @Override // android.view.ViewParent
    public void focusableViewAvailable(View v) {
        checkThread();
        View view = this.mView;
        if (view == null) {
            return;
        }
        if (view.hasFocus()) {
            View focused = this.mView.findFocus();
            if ((focused instanceof ViewGroup) && ((ViewGroup) focused).getDescendantFocusability() == 262144 && isViewDescendantOf(v, focused)) {
                v.requestFocus();
            }
        } else if (sAlwaysAssignFocus || !this.mAttachInfo.mInTouchMode) {
            v.requestFocus();
        }
    }

    @Override // android.view.ViewParent
    public void recomputeViewAttributes(View child) {
        checkThread();
        if (this.mView == child) {
            this.mAttachInfo.mRecomputeGlobalAttributes = true;
            if (!this.mWillDrawSoon) {
                scheduleTraversals();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void dispatchDetachedFromWindow() {
        InputQueue inputQueue;
        InputStage inputStage = this.mFirstInputStage;
        if (inputStage != null) {
            inputStage.onDetachedFromWindow();
        }
        View view = this.mView;
        if (!(view == null || view.mAttachInfo == null)) {
            this.mAttachInfo.mTreeObserver.dispatchOnWindowAttachedChange(false);
            this.mView.dispatchDetachedFromWindow();
            this.mViewRootHooks.dispatchDetachedFromWindow(this.mView);
        }
        this.mAccessibilityInteractionConnectionManager.ensureNoConnection();
        this.mAccessibilityManager.removeAccessibilityStateChangeListener(this.mAccessibilityInteractionConnectionManager);
        this.mAccessibilityManager.removeHighTextContrastStateChangeListener(this.mHighContrastTextManager);
        removeSendWindowContentChangedCallback();
        destroyHardwareRenderer();
        setAccessibilityFocus(null, null);
        this.mView.assignParent(null);
        this.mView = null;
        this.mAttachInfo.mRootView = null;
        destroySurface();
        InputQueue.Callback callback = this.mInputQueueCallback;
        if (!(callback == null || (inputQueue = this.mInputQueue) == null)) {
            callback.onInputQueueDestroyed(inputQueue);
            this.mInputQueue.dispose();
            this.mInputQueueCallback = null;
            this.mInputQueue = null;
        }
        WindowInputEventReceiver windowInputEventReceiver = this.mInputEventReceiver;
        if (windowInputEventReceiver != null) {
            windowInputEventReceiver.dispose();
            this.mInputEventReceiver = null;
        }
        try {
            this.mWindowSession.remove(this.mWindow);
        } catch (RemoteException e) {
            String str = this.mTag;
            Log.e(str, "RemoteException remove window " + this.mWindow + " in " + this, e);
        }
        InputChannel inputChannel = this.mInputChannel;
        if (inputChannel != null) {
            inputChannel.dispose();
            this.mInputChannel = null;
        }
        this.mDisplayManager.unregisterDisplayListener(this.mDisplayListener);
        unscheduleTraversals();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void performConfigurationChange(MergedConfiguration mergedConfiguration, boolean force, int newDisplayId) {
        if (mergedConfiguration != null) {
            Configuration globalConfig = mergedConfiguration.getGlobalConfiguration();
            Configuration overrideConfig = mergedConfiguration.getOverrideConfiguration();
            if (DEBUG_CONFIGURATION) {
                Log.v(this.mTag, "Applying new config to window " + ((Object) this.mWindowAttributes.getTitle()) + ", globalConfig: " + globalConfig + ", overrideConfig: " + overrideConfig + ", force = " + force + ", this = " + this);
            }
            CompatibilityInfo ci = this.mDisplay.getDisplayAdjustments().getCompatibilityInfo();
            if (!ci.equals(CompatibilityInfo.DEFAULT_COMPATIBILITY_INFO)) {
                globalConfig = new Configuration(globalConfig);
                ci.applyToConfiguration(this.mNoncompatDensity, globalConfig);
            }
            synchronized (sConfigCallbacks) {
                for (int i = sConfigCallbacks.size() - 1; i >= 0; i--) {
                    sConfigCallbacks.get(i).onConfigurationChanged(globalConfig);
                }
            }
            this.mLastReportedMergedConfiguration.setConfiguration(globalConfig, overrideConfig);
            this.mForceNextConfigUpdate = force;
            ActivityConfigCallback activityConfigCallback = this.mActivityConfigCallback;
            if (activityConfigCallback != null) {
                activityConfigCallback.onConfigurationChanged(overrideConfig, newDisplayId);
            } else {
                updateConfiguration(newDisplayId);
            }
            this.mForceNextConfigUpdate = false;
            return;
        }
        throw new IllegalArgumentException("No merged config provided.");
    }

    @OppoHook(level = OppoHook.OppoHookType.CHANGE_CODE, note = "Changwei.Li@ROM.SDK, 2017-04-10 : Modify for BUG959197", property = OppoHook.OppoRomType.ROM)
    public void updateConfiguration(int newDisplayId) {
        View view = this.mView;
        if (view != null) {
            Resources localResources = view.getResources();
            Configuration config = localResources.getConfiguration();
            if (newDisplayId != -1) {
                onMovedToDisplay(newDisplayId, config);
            }
            config.fontScale = this.mLastConfigurationFromResources.fontScale;
            if (this.mForceNextConfigUpdate || this.mLastConfigurationFromResources.diff(config) != 0) {
                updateInternalDisplay(this.mDisplay.getDisplayId(), localResources);
                int lastLayoutDirection = this.mLastConfigurationFromResources.getLayoutDirection();
                int currentLayoutDirection = config.getLayoutDirection();
                this.mLastConfigurationFromResources.setTo(config);
                if (lastLayoutDirection != currentLayoutDirection && this.mViewLayoutDirectionInitial == 2) {
                    this.mView.setLayoutDirection(currentLayoutDirection);
                }
                this.mView.dispatchConfigurationChanged(config);
                this.mForceNextWindowRelayout = true;
                requestLayout();
            }
            ColorFrameworkFactory.getInstance().getColorDarkModeManager().clearCache();
            updateForceDarkMode();
        }
    }

    public static boolean isViewDescendantOf(View child, View parent) {
        if (child == parent) {
            return true;
        }
        ViewParent theParent = child.getParent();
        if (!(theParent instanceof ViewGroup) || !isViewDescendantOf((View) theParent, parent)) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: private */
    public static void forceLayout(View view) {
        view.forceLayout();
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            int count = group.getChildCount();
            for (int i = 0; i < count; i++) {
                if (group.getChildAt(i) != null) {
                    forceLayout(group.getChildAt(i));
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public final class ViewRootHandler extends Handler {
        ViewRootHandler() {
        }

        @Override // android.os.Handler
        public String getMessageName(Message message) {
            switch (message.what) {
                case 1:
                    return "MSG_INVALIDATE";
                case 2:
                    return "MSG_INVALIDATE_RECT";
                case 3:
                    return "MSG_DIE";
                case 4:
                    return "MSG_RESIZED";
                case 5:
                    return "MSG_RESIZED_REPORT";
                case 6:
                    return "MSG_WINDOW_FOCUS_CHANGED";
                case 7:
                    return "MSG_DISPATCH_INPUT_EVENT";
                case 8:
                    return "MSG_DISPATCH_APP_VISIBILITY";
                case 9:
                    return "MSG_DISPATCH_GET_NEW_SURFACE";
                case 10:
                case 20:
                case 22:
                case 26:
                default:
                    return super.getMessageName(message);
                case 11:
                    return "MSG_DISPATCH_KEY_FROM_IME";
                case 12:
                    return "MSG_DISPATCH_KEY_FROM_AUTOFILL";
                case 13:
                    return "MSG_CHECK_FOCUS";
                case 14:
                    return "MSG_CLOSE_SYSTEM_DIALOGS";
                case 15:
                    return "MSG_DISPATCH_DRAG_EVENT";
                case 16:
                    return "MSG_DISPATCH_DRAG_LOCATION_EVENT";
                case 17:
                    return "MSG_DISPATCH_SYSTEM_UI_VISIBILITY";
                case 18:
                    return "MSG_UPDATE_CONFIGURATION";
                case 19:
                    return "MSG_PROCESS_INPUT_EVENTS";
                case 21:
                    return "MSG_CLEAR_ACCESSIBILITY_FOCUS_HOST";
                case 23:
                    return "MSG_WINDOW_MOVED";
                case 24:
                    return "MSG_SYNTHESIZE_INPUT_EVENT";
                case 25:
                    return "MSG_DISPATCH_WINDOW_SHOWN";
                case 27:
                    return "MSG_UPDATE_POINTER_ICON";
                case 28:
                    return "MSG_POINTER_CAPTURE_CHANGED";
                case 29:
                    return "MSG_DRAW_FINISHED";
                case 30:
                    return "MSG_INSETS_CHANGED";
                case 31:
                    return "MSG_INSETS_CONTROL_CHANGED";
                case 32:
                    return "MSG_SYSTEM_GESTURE_EXCLUSION_CHANGED";
            }
        }

        @Override // android.os.Handler
        public boolean sendMessageAtTime(Message msg, long uptimeMillis) {
            if (msg.what != 26 || msg.obj != null) {
                return super.sendMessageAtTime(msg, uptimeMillis);
            }
            throw new NullPointerException("Attempted to call MSG_REQUEST_KEYBOARD_SHORTCUTS with null receiver:");
        }

        /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            int i = -1;
            boolean hasCapture = true;
            switch (msg.what) {
                case 1:
                    ((View) msg.obj).invalidate();
                    return;
                case 2:
                    View.AttachInfo.InvalidateInfo info = (View.AttachInfo.InvalidateInfo) msg.obj;
                    info.target.invalidate(info.left, info.top, info.right, info.bottom);
                    info.recycle();
                    return;
                case 3:
                    ViewRootImpl.this.doDie();
                    return;
                case 4:
                    SomeArgs args = (SomeArgs) msg.obj;
                    if (ViewRootImpl.this.mWinFrame.equals(args.arg1) && ViewRootImpl.this.mPendingOverscanInsets.equals(args.arg5) && ViewRootImpl.this.mPendingContentInsets.equals(args.arg2) && ViewRootImpl.this.mPendingStableInsets.equals(args.arg6) && ViewRootImpl.this.mPendingDisplayCutout.get().equals(args.arg9) && ViewRootImpl.this.mPendingVisibleInsets.equals(args.arg3) && ViewRootImpl.this.mPendingOutsets.equals(args.arg7) && ViewRootImpl.this.mPendingBackDropFrame.equals(args.arg8) && args.arg4 == null && args.argi1 == 0 && ViewRootImpl.this.mDisplay.getDisplayId() == args.argi3) {
                        return;
                    }
                case 5:
                    break;
                case 6:
                    ViewRootImpl.this.mColorViewRootUtil.checkGestureConfig(ViewRootImpl.this.mContext);
                    ViewRootImpl.this.handleWindowFocusChanged();
                    return;
                case 7:
                    SomeArgs args2 = (SomeArgs) msg.obj;
                    ViewRootImpl.this.enqueueInputEvent((InputEvent) args2.arg1, (InputEventReceiver) args2.arg2, 0, true);
                    args2.recycle();
                    return;
                case 8:
                    ViewRootImpl viewRootImpl = ViewRootImpl.this;
                    if (msg.arg1 == 0) {
                        hasCapture = false;
                    }
                    viewRootImpl.handleAppVisibility(hasCapture);
                    return;
                case 9:
                    ViewRootImpl.this.handleGetNewSurface();
                    return;
                case 10:
                case 20:
                default:
                    return;
                case 11:
                    if (ViewRootImpl.LOCAL_LOGV || ViewDebugManager.DEBUG_KEY) {
                        Log.v(ViewRootImpl.this.mTag, "Dispatching key " + msg.obj + " from IME to " + ViewRootImpl.this.mView + " in " + this);
                    }
                    KeyEvent event = (KeyEvent) msg.obj;
                    if ((event.getFlags() & 8) != 0) {
                        event = KeyEvent.changeFlags(event, event.getFlags() & -9);
                    }
                    ViewRootImpl.this.enqueueInputEvent(event, null, 1, true);
                    return;
                case 12:
                    if (ViewRootImpl.LOCAL_LOGV) {
                        Log.v(ViewRootImpl.TAG, "Dispatching key " + msg.obj + " from Autofill to " + ViewRootImpl.this.mView);
                    }
                    ViewRootImpl.this.enqueueInputEvent((KeyEvent) msg.obj, null, 0, true);
                    return;
                case 13:
                    InputMethodManager imm = (InputMethodManager) ViewRootImpl.this.mContext.getSystemService(InputMethodManager.class);
                    if (imm != null) {
                        imm.checkFocus();
                        return;
                    }
                    return;
                case 14:
                    if (ViewRootImpl.this.mView != null) {
                        ViewRootImpl.this.mView.onCloseSystemDialogs((String) msg.obj);
                        return;
                    }
                    return;
                case 15:
                case 16:
                    DragEvent event2 = (DragEvent) msg.obj;
                    event2.mLocalState = ViewRootImpl.this.mLocalDragState;
                    ViewRootImpl.this.handleDragEvent(event2);
                    return;
                case 17:
                    ViewRootImpl.this.handleDispatchSystemUiVisibilityChanged((SystemUiVisibilityInfo) msg.obj);
                    return;
                case 18:
                    Configuration config = (Configuration) msg.obj;
                    if (config.isOtherSeqNewer(ViewRootImpl.this.mLastReportedMergedConfiguration.getMergedConfiguration())) {
                        config = ViewRootImpl.this.mLastReportedMergedConfiguration.getGlobalConfiguration();
                    }
                    ViewRootImpl.this.mPendingMergedConfiguration.setConfiguration(config, ViewRootImpl.this.mLastReportedMergedConfiguration.getOverrideConfiguration());
                    ((IColorBurmeseZgHooks) ColorFrameworkFactory.getInstance().getFeature(IColorBurmeseZgHooks.DEFAULT, new Object[0])).updateBurmeseZgFlag(ViewRootImpl.this.mContext);
                    ViewRootImpl viewRootImpl2 = ViewRootImpl.this;
                    viewRootImpl2.performConfigurationChange(viewRootImpl2.mPendingMergedConfiguration, false, -1);
                    return;
                case 19:
                    ViewRootImpl viewRootImpl3 = ViewRootImpl.this;
                    viewRootImpl3.mProcessInputEventsScheduled = false;
                    viewRootImpl3.doProcessInputEvents();
                    return;
                case 21:
                    ViewRootImpl.this.setAccessibilityFocus(null, null);
                    return;
                case 22:
                    if (ViewRootImpl.this.mView != null) {
                        ViewRootImpl viewRootImpl4 = ViewRootImpl.this;
                        viewRootImpl4.invalidateWorld(viewRootImpl4.mView);
                        return;
                    }
                    return;
                case 23:
                    if (ViewRootImpl.this.mAdded) {
                        int w = ViewRootImpl.this.mWinFrame.width();
                        int h = ViewRootImpl.this.mWinFrame.height();
                        int l = msg.arg1;
                        int t = msg.arg2;
                        ViewRootImpl.this.mTmpFrame.left = l;
                        ViewRootImpl.this.mTmpFrame.right = l + w;
                        ViewRootImpl.this.mTmpFrame.top = t;
                        ViewRootImpl.this.mTmpFrame.bottom = t + h;
                        ViewRootImpl viewRootImpl5 = ViewRootImpl.this;
                        viewRootImpl5.setFrame(viewRootImpl5.mTmpFrame);
                        ViewRootImpl.this.mPendingBackDropFrame.set(ViewRootImpl.this.mWinFrame);
                        ViewRootImpl viewRootImpl6 = ViewRootImpl.this;
                        viewRootImpl6.maybeHandleWindowMove(viewRootImpl6.mWinFrame);
                        return;
                    }
                    return;
                case 24:
                    ViewRootImpl.this.enqueueInputEvent((InputEvent) msg.obj, null, 32, true);
                    return;
                case 25:
                    ViewRootImpl.this.handleDispatchWindowShown();
                    return;
                case 26:
                    ViewRootImpl.this.handleRequestKeyboardShortcuts((IResultReceiver) msg.obj, msg.arg1);
                    return;
                case 27:
                    ViewRootImpl.this.resetPointerIcon((MotionEvent) msg.obj);
                    return;
                case 28:
                    if (msg.arg1 == 0) {
                        hasCapture = false;
                    }
                    ViewRootImpl.this.handlePointerCaptureChanged(hasCapture);
                    return;
                case 29:
                    ViewRootImpl.this.pendingDrawFinished();
                    return;
                case 30:
                    ViewRootImpl.this.mInsetsController.onStateChanged((InsetsState) msg.obj);
                    ViewRootImpl.this.mColorViewRootUtil.checkGestureConfig(ViewRootImpl.this.mContext);
                    return;
                case 31:
                    SomeArgs args3 = (SomeArgs) msg.obj;
                    ViewRootImpl.this.mInsetsController.onControlsChanged((InsetsSourceControl[]) args3.arg2);
                    ViewRootImpl.this.mInsetsController.onStateChanged((InsetsState) args3.arg1);
                    return;
                case 32:
                    ViewRootImpl.this.systemGestureExclusionChanged();
                    return;
            }
            if (ViewRootImpl.this.mAdded) {
                SomeArgs args4 = (SomeArgs) msg.obj;
                int displayId = args4.argi3;
                MergedConfiguration mergedConfiguration = (MergedConfiguration) args4.arg4;
                boolean displayChanged = ViewRootImpl.this.mDisplay.getDisplayId() != displayId;
                boolean configChanged = false;
                if (!ViewRootImpl.this.mLastReportedMergedConfiguration.equals(mergedConfiguration)) {
                    ViewRootImpl viewRootImpl7 = ViewRootImpl.this;
                    if (displayChanged) {
                        i = displayId;
                    }
                    viewRootImpl7.performConfigurationChange(mergedConfiguration, false, i);
                    configChanged = true;
                } else if (displayChanged) {
                    ViewRootImpl viewRootImpl8 = ViewRootImpl.this;
                    viewRootImpl8.onMovedToDisplay(displayId, viewRootImpl8.mLastConfigurationFromResources);
                }
                if (ViewRootImpl.DEBUG_LAYOUT) {
                    Log.d(ViewRootImpl.this.mTag, "Handle RESIZE: message = " + msg.what + " ,this = " + ViewRootImpl.this);
                }
                boolean framesChanged = !ViewRootImpl.this.mWinFrame.equals(args4.arg1) || !ViewRootImpl.this.mPendingOverscanInsets.equals(args4.arg5) || !ViewRootImpl.this.mPendingContentInsets.equals(args4.arg2) || !ViewRootImpl.this.mPendingStableInsets.equals(args4.arg6) || !ViewRootImpl.this.mPendingDisplayCutout.get().equals(args4.arg9) || !ViewRootImpl.this.mPendingVisibleInsets.equals(args4.arg3) || !ViewRootImpl.this.mPendingOutsets.equals(args4.arg7);
                ViewRootImpl.this.setFrame((Rect) args4.arg1);
                ViewRootImpl.this.mPendingOverscanInsets.set((Rect) args4.arg5);
                ViewRootImpl.this.mPendingContentInsets.set((Rect) args4.arg2);
                ViewRootImpl.this.mPendingStableInsets.set((Rect) args4.arg6);
                ViewRootImpl.this.mPendingDisplayCutout.set((DisplayCutout) args4.arg9);
                ViewRootImpl.this.mPendingVisibleInsets.set((Rect) args4.arg3);
                ViewRootImpl.this.mPendingOutsets.set((Rect) args4.arg7);
                ViewRootImpl.this.mPendingBackDropFrame.set((Rect) args4.arg8);
                ViewRootImpl.this.mForceNextWindowRelayout = args4.argi1 != 0;
                ViewRootImpl viewRootImpl9 = ViewRootImpl.this;
                if (args4.argi2 == 0) {
                    hasCapture = false;
                }
                viewRootImpl9.mPendingAlwaysConsumeSystemBars = hasCapture;
                args4.recycle();
                if (msg.what == 5) {
                    ViewRootImpl.this.reportNextDraw();
                }
                if (ViewRootImpl.this.mView != null && (framesChanged || configChanged)) {
                    ViewRootImpl.forceLayout(ViewRootImpl.this.mView);
                }
                ViewRootImpl.this.requestLayout();
            }
            ViewRootImpl.this.mColorViewRootUtil.initSwipState(ViewRootImpl.this.mDisplay, ViewRootImpl.this.mContext);
        }
    }

    /* access modifiers changed from: package-private */
    @UnsupportedAppUsage
    public boolean ensureTouchMode(boolean inTouchMode) {
        if (DBG) {
            Log.d("touchmode", "ensureTouchMode(" + inTouchMode + "), current touch mode is " + this.mAttachInfo.mInTouchMode);
        }
        if (this.mAttachInfo.mInTouchMode == inTouchMode) {
            return false;
        }
        try {
            this.mWindowSession.setInTouchMode(inTouchMode);
            return ensureTouchModeLocally(inTouchMode);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean ensureTouchModeLocally(boolean inTouchMode) {
        if (DBG) {
            Log.d("touchmode", "ensureTouchModeLocally(" + inTouchMode + "), current touch mode is " + this.mAttachInfo.mInTouchMode);
        }
        if (this.mAttachInfo.mInTouchMode == inTouchMode) {
            return false;
        }
        View.AttachInfo attachInfo = this.mAttachInfo;
        attachInfo.mInTouchMode = inTouchMode;
        attachInfo.mTreeObserver.dispatchOnTouchModeChanged(inTouchMode);
        return inTouchMode ? enterTouchMode() : leaveTouchMode();
    }

    private boolean enterTouchMode() {
        View focused;
        View view = this.mView;
        if (view == null || !view.hasFocus() || (focused = this.mView.findFocus()) == null || focused.isFocusableInTouchMode()) {
            return false;
        }
        ViewGroup ancestorToTakeFocus = findAncestorToTakeFocusInTouchMode(focused);
        if (ancestorToTakeFocus != null) {
            return ancestorToTakeFocus.requestFocus();
        }
        focused.clearFocusInternal(null, true, false);
        return true;
    }

    private static ViewGroup findAncestorToTakeFocusInTouchMode(View focused) {
        ViewParent parent = focused.getParent();
        while (parent instanceof ViewGroup) {
            ViewGroup vgParent = (ViewGroup) parent;
            if (vgParent.getDescendantFocusability() == 262144 && vgParent.isFocusableInTouchMode()) {
                return vgParent;
            }
            if (vgParent.isRootNamespace()) {
                return null;
            }
            parent = vgParent.getParent();
        }
        return null;
    }

    private boolean leaveTouchMode() {
        View view = this.mView;
        if (view == null) {
            return false;
        }
        if (view.hasFocus()) {
            View focusedView = this.mView.findFocus();
            if (!(focusedView instanceof ViewGroup) || ((ViewGroup) focusedView).getDescendantFocusability() != 262144) {
                return false;
            }
        }
        return this.mView.restoreDefaultFocus();
    }

    /* access modifiers changed from: package-private */
    public abstract class InputStage {
        protected static final String DEBUG_TAG = "ANR_LOG";
        protected static final int FINISH_HANDLED = 1;
        protected static final int FINISH_NOT_HANDLED = 2;
        protected static final int FORWARD = 0;
        protected static final long SPENT_TOO_MUCH_TIME = 500;
        protected long mDeliverTime = 0;
        private final InputStage mNext;

        public InputStage(InputStage next) {
            this.mNext = next;
        }

        public final void deliver(QueuedInputEvent q) {
            ViewDebugManager.getInstance().debugInputStageDeliverd(this, System.currentTimeMillis());
            this.mDeliverTime = System.currentTimeMillis();
            if ((q.mFlags & 4) != 0) {
                forward(q);
            } else if (shouldDropInputEvent(q)) {
                finish(q, false);
            } else {
                ViewDebugManager.getInstance().debugInputDispatchState(q.mEvent, toString());
                apply(q, onProcess(q));
            }
        }

        /* access modifiers changed from: protected */
        public void finish(QueuedInputEvent q, boolean handled) {
            q.mFlags |= 4;
            if (handled) {
                q.mFlags |= 8;
            }
            forward(q);
        }

        /* access modifiers changed from: protected */
        public void forward(QueuedInputEvent q) {
            onDeliverToNext(q);
        }

        /* access modifiers changed from: protected */
        public void apply(QueuedInputEvent q, int result) {
            if (result == 0) {
                forward(q);
            } else if (result == 1) {
                finish(q, true);
            } else if (result == 2) {
                finish(q, false);
            } else {
                throw new IllegalArgumentException("Invalid result: " + result);
            }
        }

        /* access modifiers changed from: protected */
        public int onProcess(QueuedInputEvent q) {
            return 0;
        }

        /* access modifiers changed from: protected */
        public void onDeliverToNext(QueuedInputEvent q) {
            if (SystemClock.uptimeMillis() - this.mDeliverTime > SPENT_TOO_MUCH_TIME) {
                Log.v(DEBUG_TAG, "Spent too much time, " + Long.toString(SystemClock.uptimeMillis() - this.mDeliverTime) + " ms in " + getClass().getSimpleName());
            }
            if (ViewRootImpl.DEBUG_INPUT_STAGES || InputLog.DEBUG) {
                String str = ViewRootImpl.this.mTag;
                Log.v(str, "Done with " + getClass().getSimpleName() + ". " + q);
            }
            InputStage inputStage = this.mNext;
            if (inputStage != null) {
                inputStage.deliver(q);
            } else {
                ViewRootImpl.this.finishInputEvent(q);
            }
        }

        /* access modifiers changed from: protected */
        public void onWindowFocusChanged(boolean hasWindowFocus) {
            InputStage inputStage = this.mNext;
            if (inputStage != null) {
                inputStage.onWindowFocusChanged(hasWindowFocus);
            }
        }

        /* access modifiers changed from: protected */
        public void onDetachedFromWindow() {
            InputStage inputStage = this.mNext;
            if (inputStage != null) {
                inputStage.onDetachedFromWindow();
            }
        }

        /* access modifiers changed from: protected */
        public boolean shouldDropInputEvent(QueuedInputEvent q) {
            if (ViewRootImpl.this.mView == null || !ViewRootImpl.this.mAdded) {
                String str = ViewRootImpl.this.mTag;
                Slog.w(str, "Dropping event due to root view being removed: " + q.mEvent);
                return true;
            } else if ((ViewRootImpl.this.mAttachInfo.mHasWindowFocus || q.mEvent.isFromSource(2) || ViewRootImpl.this.isAutofillUiShowing()) && !ViewRootImpl.this.mStopped && ((!ViewRootImpl.this.mIsAmbientMode || q.mEvent.isFromSource(1)) && (!ViewRootImpl.this.mPausedForTransition || isBack(q.mEvent)))) {
                return false;
            } else {
                if (ViewRootImpl.isTerminalInputEvent(q.mEvent)) {
                    q.mEvent.cancel();
                    if (ViewRootImpl.DEBUG_PANIC) {
                        String str2 = ViewRootImpl.this.mTag;
                        Slog.w(str2, "Cancelling event due to no window focus: " + q.mEvent + ", hasFocus:" + ViewRootImpl.this.mAttachInfo.mHasWindowFocus + ", mStopped:" + ViewRootImpl.this.mStopped + ", mPausedForTransition:" + ViewRootImpl.this.mPausedForTransition);
                    }
                    return false;
                }
                if (ViewRootImpl.DEBUG_PANIC) {
                    String str3 = ViewRootImpl.this.mTag;
                    Slog.w(str3, "Dropping event due to no window focus: " + q.mEvent + ", hasFocus:" + ViewRootImpl.this.mAttachInfo.mHasWindowFocus + ", mStopped:" + ViewRootImpl.this.mStopped + ", mPausedForTransition:" + ViewRootImpl.this.mPausedForTransition);
                }
                return true;
            }
        }

        /* access modifiers changed from: package-private */
        public void dump(String prefix, PrintWriter writer) {
            InputStage inputStage = this.mNext;
            if (inputStage != null) {
                inputStage.dump(prefix, writer);
            }
        }

        private boolean isBack(InputEvent event) {
            if (!(event instanceof KeyEvent) || ((KeyEvent) event).getKeyCode() != 4) {
                return false;
            }
            return true;
        }
    }

    abstract class AsyncInputStage extends InputStage {
        protected static final int DEFER = 3;
        private QueuedInputEvent mQueueHead;
        private int mQueueLength;
        private QueuedInputEvent mQueueTail;
        private final String mTraceCounter;

        public AsyncInputStage(InputStage next, String traceCounter) {
            super(next);
            this.mTraceCounter = traceCounter;
        }

        /* access modifiers changed from: protected */
        public void defer(QueuedInputEvent q) {
            q.mFlags |= 2;
            enqueue(q);
        }

        /* access modifiers changed from: protected */
        @Override // android.view.ViewRootImpl.InputStage
        public void forward(QueuedInputEvent q) {
            q.mFlags &= -3;
            QueuedInputEvent curr = this.mQueueHead;
            if (curr == null) {
                super.forward(q);
                return;
            }
            int deviceId = q.mEvent.getDeviceId();
            QueuedInputEvent prev = null;
            boolean blocked = false;
            while (curr != null && curr != q) {
                if (!blocked && deviceId == curr.mEvent.getDeviceId()) {
                    blocked = true;
                }
                prev = curr;
                curr = curr.mNext;
            }
            if (!blocked) {
                if (curr != null) {
                    curr = curr.mNext;
                    dequeue(q, prev);
                }
                super.forward(q);
                while (curr != null) {
                    if (deviceId != curr.mEvent.getDeviceId()) {
                        prev = curr;
                        curr = curr.mNext;
                    } else if ((curr.mFlags & 2) == 0) {
                        QueuedInputEvent next = curr.mNext;
                        dequeue(curr, prev);
                        super.forward(curr);
                        curr = next;
                    } else {
                        return;
                    }
                }
            } else if (curr == null) {
                enqueue(q);
            }
        }

        /* access modifiers changed from: protected */
        @Override // android.view.ViewRootImpl.InputStage
        public void apply(QueuedInputEvent q, int result) {
            if (result == 3) {
                defer(q);
            } else {
                super.apply(q, result);
            }
        }

        private void enqueue(QueuedInputEvent q) {
            QueuedInputEvent queuedInputEvent = this.mQueueTail;
            if (queuedInputEvent == null) {
                this.mQueueHead = q;
                this.mQueueTail = q;
            } else {
                queuedInputEvent.mNext = q;
                this.mQueueTail = q;
            }
            this.mQueueLength++;
            Trace.traceCounter(4, this.mTraceCounter, this.mQueueLength);
        }

        private void dequeue(QueuedInputEvent q, QueuedInputEvent prev) {
            if (prev == null) {
                this.mQueueHead = q.mNext;
            } else {
                prev.mNext = q.mNext;
            }
            if (this.mQueueTail == q) {
                this.mQueueTail = prev;
            }
            q.mNext = null;
            this.mQueueLength--;
            Trace.traceCounter(4, this.mTraceCounter, this.mQueueLength);
        }

        /* access modifiers changed from: package-private */
        @Override // android.view.ViewRootImpl.InputStage
        public void dump(String prefix, PrintWriter writer) {
            writer.print(prefix);
            writer.print(getClass().getName());
            writer.print(": mQueueLength=");
            writer.println(this.mQueueLength);
            super.dump(prefix, writer);
        }
    }

    final class NativePreImeInputStage extends AsyncInputStage implements InputQueue.FinishedInputEventCallback {
        public NativePreImeInputStage(InputStage next, String traceCounter) {
            super(next, traceCounter);
        }

        /* access modifiers changed from: protected */
        @Override // android.view.ViewRootImpl.InputStage
        public int onProcess(QueuedInputEvent q) {
            if (ViewRootImpl.this.mInputQueue == null || !(q.mEvent instanceof KeyEvent)) {
                return 0;
            }
            ViewRootImpl.this.mInputQueue.sendInputEvent(q.mEvent, q, true, this);
            return 3;
        }

        @Override // android.view.InputQueue.FinishedInputEventCallback
        public void onFinishedInputEvent(Object token, boolean handled) {
            QueuedInputEvent q = (QueuedInputEvent) token;
            if (handled) {
                finish(q, true);
            } else {
                forward(q);
            }
        }
    }

    final class ViewPreImeInputStage extends InputStage {
        public ViewPreImeInputStage(InputStage next) {
            super(next);
        }

        /* access modifiers changed from: protected */
        @Override // android.view.ViewRootImpl.InputStage
        public int onProcess(QueuedInputEvent q) {
            if (q.mEvent instanceof KeyEvent) {
                return processKeyEvent(q);
            }
            return 0;
        }

        private int processKeyEvent(QueuedInputEvent q) {
            if (ViewRootImpl.this.mView.dispatchKeyEventPreIme((KeyEvent) q.mEvent)) {
                return 1;
            }
            return 0;
        }
    }

    final class ImeInputStage extends AsyncInputStage implements InputMethodManager.FinishedInputEventCallback {
        public ImeInputStage(InputStage next, String traceCounter) {
            super(next, traceCounter);
        }

        /* access modifiers changed from: protected */
        @Override // android.view.ViewRootImpl.InputStage
        public int onProcess(QueuedInputEvent q) {
            InputMethodManager imm;
            if (!ViewRootImpl.this.mLastWasImTarget || ViewRootImpl.this.isInLocalFocusMode() || (imm = (InputMethodManager) ViewRootImpl.this.mContext.getSystemService(InputMethodManager.class)) == null) {
                return 0;
            }
            InputEvent event = q.mEvent;
            if (ViewRootImpl.DEBUG_IMF) {
                String str = ViewRootImpl.this.mTag;
                Log.v(str, "Sending input event to IME: " + event);
            }
            int result = imm.dispatchInputEvent(event, q, this, ViewRootImpl.this.mHandler);
            if (result == 1) {
                return 1;
            }
            if (result == 0) {
                return 0;
            }
            return 3;
        }

        @Override // android.view.inputmethod.InputMethodManager.FinishedInputEventCallback
        public void onFinishedInputEvent(Object token, boolean handled) {
            QueuedInputEvent q = (QueuedInputEvent) token;
            if (ViewRootImpl.DEBUG_IMF) {
                String str = ViewRootImpl.this.mTag;
                Log.d(str, "IME finishedEvent: handled = " + handled + ", event = " + q + ", viewAncestor = " + this);
            }
            if (handled) {
                finish(q, true);
            } else {
                forward(q);
            }
        }
    }

    final class EarlyPostImeInputStage extends InputStage {
        public EarlyPostImeInputStage(InputStage next) {
            super(next);
        }

        /* access modifiers changed from: protected */
        @Override // android.view.ViewRootImpl.InputStage
        public int onProcess(QueuedInputEvent q) {
            if (q.mEvent instanceof KeyEvent) {
                return processKeyEvent(q);
            }
            if (q.mEvent instanceof MotionEvent) {
                return processMotionEvent(q);
            }
            return 0;
        }

        private int processKeyEvent(QueuedInputEvent q) {
            KeyEvent event = (KeyEvent) q.mEvent;
            if (ViewRootImpl.this.mAttachInfo.mTooltipHost != null) {
                ViewRootImpl.this.mAttachInfo.mTooltipHost.handleTooltipKey(event);
            }
            if (ViewRootImpl.this.checkForLeavingTouchModeAndConsume(event)) {
                return 1;
            }
            ViewRootImpl.this.mFallbackEventHandler.preDispatchKeyEvent(event);
            return 0;
        }

        private int processMotionEvent(QueuedInputEvent q) {
            if (!(q == null || q.mEvent == null)) {
                boolean zoomWindowMode = ViewRootImpl.this.mLastReportedMergedConfiguration.getMergedConfiguration().windowConfiguration.getWindowingMode() == ColorZoomWindowManager.WINDOWING_MODE_ZOOM;
                if (ViewRootImpl.this.mColorViewRootUtil != null && ViewRootImpl.this.mColorViewRootUtil.swipeFromBottom((MotionEvent) q.mEvent, ViewRootImpl.this.mNoncompatDensity, ViewRootImpl.this.mDensity, ViewRootImpl.this.mDisplay) && !zoomWindowMode) {
                    return 1;
                }
            }
            MotionEvent event = (MotionEvent) q.mEvent;
            if (event.isFromSource(2)) {
                return processPointerEvent(q);
            }
            int action = event.getActionMasked();
            if ((action == 0 || action == 8) && event.isFromSource(8)) {
                ViewRootImpl.this.ensureTouchMode(false);
            }
            return 0;
        }

        private int processPointerEvent(QueuedInputEvent q) {
            AutofillManager afm;
            MotionEvent event = (MotionEvent) q.mEvent;
            if (ViewRootImpl.this.mTranslator != null) {
                ViewRootImpl.this.mTranslator.translateEventInScreenToAppWindow(event);
            }
            int action = event.getAction();
            if (action == 0 || action == 8) {
                ViewRootImpl.this.ensureTouchMode(event.isFromSource(4098));
            }
            if (action == 0 && (afm = ViewRootImpl.this.getAutofillManager()) != null) {
                afm.requestHideFillUi();
            }
            if (action == 0 && ViewRootImpl.this.mAttachInfo.mTooltipHost != null) {
                ViewRootImpl.this.mAttachInfo.mTooltipHost.hideTooltip();
            }
            if (ViewRootImpl.this.mCurScrollY != 0) {
                event.offsetLocation(0.0f, (float) ViewRootImpl.this.mCurScrollY);
            }
            if (!event.isTouchEvent()) {
                return 0;
            }
            ViewRootImpl.this.mLastTouchPoint.x = event.getRawX();
            ViewRootImpl.this.mLastTouchPoint.y = event.getRawY();
            ViewRootImpl.this.mLastTouchSource = event.getSource();
            return 0;
        }
    }

    final class NativePostImeInputStage extends AsyncInputStage implements InputQueue.FinishedInputEventCallback {
        public NativePostImeInputStage(InputStage next, String traceCounter) {
            super(next, traceCounter);
        }

        /* access modifiers changed from: protected */
        @Override // android.view.ViewRootImpl.InputStage
        public int onProcess(QueuedInputEvent q) {
            if (ViewRootImpl.this.mInputQueue == null) {
                return 0;
            }
            ViewRootImpl.this.mInputQueue.sendInputEvent(q.mEvent, q, false, this);
            return 3;
        }

        @Override // android.view.InputQueue.FinishedInputEventCallback
        public void onFinishedInputEvent(Object token, boolean handled) {
            QueuedInputEvent q = (QueuedInputEvent) token;
            if (handled) {
                finish(q, true);
            } else {
                forward(q);
            }
        }
    }

    final class ViewPostImeInputStage extends InputStage {
        private int mPointerEventNum = 0;
        private boolean startOppoScreenShot = false;

        public ViewPostImeInputStage(InputStage next) {
            super(next);
        }

        /* access modifiers changed from: protected */
        @Override // android.view.ViewRootImpl.InputStage
        public int onProcess(QueuedInputEvent q) {
            if (q.mEvent instanceof KeyEvent) {
                return processKeyEvent(q);
            }
            int source = q.mEvent.getSource();
            if ((source & 2) != 0) {
                return processPointerEvent(q);
            }
            if ((source & 4) != 0) {
                return processTrackballEvent(q);
            }
            return processGenericMotionEvent(q);
        }

        /* access modifiers changed from: protected */
        @Override // android.view.ViewRootImpl.InputStage
        public void onDeliverToNext(QueuedInputEvent q) {
            if (ViewRootImpl.this.mUnbufferedInputDispatch && (q.mEvent instanceof MotionEvent) && ((MotionEvent) q.mEvent).isTouchEvent() && ViewRootImpl.isTerminalInputEvent(q.mEvent)) {
                ViewRootImpl viewRootImpl = ViewRootImpl.this;
                viewRootImpl.mUnbufferedInputDispatch = false;
                viewRootImpl.scheduleConsumeBatchedInput();
            }
            super.onDeliverToNext(q);
        }

        private boolean performFocusNavigation(KeyEvent event) {
            int direction = 0;
            int keyCode = event.getKeyCode();
            if (keyCode != 61) {
                switch (keyCode) {
                    case 19:
                        if (event.hasNoModifiers()) {
                            direction = 33;
                            break;
                        }
                        break;
                    case 20:
                        if (event.hasNoModifiers()) {
                            direction = 130;
                            break;
                        }
                        break;
                    case 21:
                        if (event.hasNoModifiers()) {
                            direction = 17;
                            break;
                        }
                        break;
                    case 22:
                        if (event.hasNoModifiers()) {
                            direction = 66;
                            break;
                        }
                        break;
                }
            } else if (event.hasNoModifiers()) {
                direction = 2;
            } else if (event.hasModifiers(1)) {
                direction = 1;
            }
            if (direction == 0) {
                return false;
            }
            View focused = ViewRootImpl.this.mView.findFocus();
            if (focused != null) {
                View v = focused.focusSearch(direction);
                if (!(v == null || v == focused)) {
                    focused.getFocusedRect(ViewRootImpl.this.mTempRect);
                    if (ViewRootImpl.this.mView instanceof ViewGroup) {
                        ((ViewGroup) ViewRootImpl.this.mView).offsetDescendantRectToMyCoords(focused, ViewRootImpl.this.mTempRect);
                        ((ViewGroup) ViewRootImpl.this.mView).offsetRectIntoDescendantCoords(v, ViewRootImpl.this.mTempRect);
                    }
                    if (v.requestFocus(direction, ViewRootImpl.this.mTempRect)) {
                        ViewRootImpl.this.playSoundEffect(SoundEffectConstants.getContantForFocusDirection(direction));
                        return true;
                    }
                }
                if (ViewRootImpl.this.mView.dispatchUnhandledMove(focused, direction)) {
                    return true;
                }
                return false;
            } else if (ViewRootImpl.this.mView.restoreDefaultFocus()) {
                return true;
            } else {
                return false;
            }
        }

        private boolean performKeyboardGroupNavigation(int direction) {
            View cluster;
            View focused = ViewRootImpl.this.mView.findFocus();
            if (focused == null && ViewRootImpl.this.mView.restoreDefaultFocus()) {
                return true;
            }
            if (focused == null) {
                cluster = ViewRootImpl.this.keyboardNavigationClusterSearch(null, direction);
            } else {
                cluster = focused.keyboardNavigationClusterSearch(null, direction);
            }
            int realDirection = direction;
            if (direction == 2 || direction == 1) {
                realDirection = 130;
            }
            if (cluster != null && cluster.isRootNamespace()) {
                if (cluster.restoreFocusNotInCluster()) {
                    ViewRootImpl.this.playSoundEffect(SoundEffectConstants.getContantForFocusDirection(direction));
                    return true;
                }
                cluster = ViewRootImpl.this.keyboardNavigationClusterSearch(null, direction);
            }
            if (cluster == null || !cluster.restoreFocusInCluster(realDirection)) {
                return false;
            }
            ViewRootImpl.this.playSoundEffect(SoundEffectConstants.getContantForFocusDirection(direction));
            return true;
        }

        private int processKeyEvent(QueuedInputEvent q) {
            KeyEvent event = (KeyEvent) q.mEvent;
            if (InputLog.DEBUG) {
                String str = ViewRootImpl.this.mTag;
                InputLog.d(str, " processKeyEvent, " + event);
            }
            if (ViewRootImpl.this.mUnhandledKeyManager.preViewDispatch(event)) {
                if (ViewDebugManager.DEBUG_ENG) {
                    String str2 = ViewRootImpl.this.mTag;
                    Log.v(str2, "App handle dispatchUnique event = " + event + ", mView = " + ViewRootImpl.this.mView + ", this = " + this);
                }
                return 1;
            } else if (ViewRootImpl.this.mView.dispatchKeyEvent(event)) {
                if (InputLog.DEBUG) {
                    String str3 = ViewRootImpl.this.mTag;
                    InputLog.d(str3, event.getKeyCode() + " was FINISH_HANDLED by  mView ");
                }
                return 1;
            } else if (shouldDropInputEvent(q)) {
                return 2;
            } else {
                if (ViewRootImpl.this.mUnhandledKeyManager.dispatch(ViewRootImpl.this.mView, event)) {
                    return 1;
                }
                int groupNavigationDirection = 0;
                if (event.getAction() == 0 && event.getKeyCode() == 61) {
                    if (KeyEvent.metaStateHasModifiers(event.getMetaState(), 65536)) {
                        groupNavigationDirection = 2;
                    } else if (KeyEvent.metaStateHasModifiers(event.getMetaState(), 65537)) {
                        groupNavigationDirection = 1;
                    }
                }
                if (event.getAction() == 0 && !KeyEvent.metaStateHasNoModifiers(event.getMetaState()) && event.getRepeatCount() == 0 && !KeyEvent.isModifierKey(event.getKeyCode()) && groupNavigationDirection == 0) {
                    if (ViewRootImpl.this.mView.dispatchKeyShortcutEvent(event)) {
                        return 1;
                    }
                    if (shouldDropInputEvent(q)) {
                        return 2;
                    }
                }
                if (ViewRootImpl.this.mFallbackEventHandler.dispatchKeyEvent(event)) {
                    return 1;
                }
                if (shouldDropInputEvent(q)) {
                    return 2;
                }
                if (event.getAction() != 0) {
                    return 0;
                }
                if (groupNavigationDirection != 0) {
                    if (performKeyboardGroupNavigation(groupNavigationDirection)) {
                        return 1;
                    }
                    return 0;
                } else if (performFocusNavigation(event)) {
                    return 1;
                } else {
                    return 0;
                }
            }
        }

        @OppoHook(level = OppoHook.OppoHookType.CHANGE_CODE, note = "Jun.Zhang@Plf.Framework,2013.02.19:add for three pointers move shot screen; ", property = OppoHook.OppoRomType.ROM)
        private int processPointerEvent(QueuedInputEvent q) {
            MotionEvent eventWrap;
            MotionEvent event = (MotionEvent) q.mEvent;
            this.mPointerEventNum++;
            if (InputLog.DEBUG) {
                InputLog.d(ViewRootImpl.TAG, " processPointerEvent  event = " + event);
            } else if (ViewRootImpl.DEBUG_PANIC && this.mPointerEventNum > 50) {
                Log.d(ViewRootImpl.TAG, " processPointerEvent  event = " + event);
                this.mPointerEventNum = 0;
            }
            boolean tmpOppoScreenShot = OppoScreenShotUtil.checkPauseDeliverPointer();
            boolean z = this.startOppoScreenShot;
            this.startOppoScreenShot = tmpOppoScreenShot;
            if (tmpOppoScreenShot) {
                OppoScreenShotUtil.dealPausedDeliverPointer((MotionEvent) q.mEvent, ViewRootImpl.this.mView);
                return 1;
            }
            ViewRootImpl.this.mAttachInfo.mUnbufferedDispatchRequested = false;
            ViewRootImpl.this.mAttachInfo.mHandlingPointerEvent = true;
            if (ViewRootImpl.this.mView == null || (eventWrap = ViewRootImpl.this.mViewRootHooks.updatePointerEvent(event, ViewRootImpl.this.mView, ViewRootImpl.this.mLastConfigurationFromResources)) == null) {
                return 1;
            }
            boolean handled = ViewRootImpl.this.mView.dispatchPointerEvent(eventWrap);
            if (handled && ViewDebugManager.DEBUG_ENG) {
                Log.v(ViewRootImpl.this.mTag, "App handle pointer event: event = " + eventWrap + ", mView = " + ViewRootImpl.this.mView + ", this = " + this);
            }
            maybeUpdatePointerIcon(eventWrap);
            ViewRootImpl.this.maybeUpdateTooltip(eventWrap);
            ViewRootImpl.this.mAttachInfo.mHandlingPointerEvent = false;
            if (ViewRootImpl.this.mAttachInfo.mUnbufferedDispatchRequested && !ViewRootImpl.this.mUnbufferedInputDispatch) {
                ViewRootImpl viewRootImpl = ViewRootImpl.this;
                viewRootImpl.mUnbufferedInputDispatch = true;
                if (viewRootImpl.mConsumeBatchedInputScheduled) {
                    ViewRootImpl.this.scheduleConsumeBatchedInputImmediately();
                }
            }
            if (InputLog.DEBUG) {
                InputLog.d(ViewRootImpl.TAG, " dispatchPointerEvent to mView handled=" + handled);
            }
            return handled ? 1 : 0;
        }

        private void maybeUpdatePointerIcon(MotionEvent event) {
            if (event.getPointerCount() == 1 && event.isFromSource(8194)) {
                if (event.getActionMasked() == 9 || event.getActionMasked() == 10) {
                    ViewRootImpl.this.mPointerIconType = 1;
                }
                if (event.getActionMasked() != 10 && !ViewRootImpl.this.updatePointerIcon(event) && event.getActionMasked() == 7) {
                    ViewRootImpl.this.mPointerIconType = 1;
                }
            }
        }

        private int processTrackballEvent(QueuedInputEvent q) {
            MotionEvent event = (MotionEvent) q.mEvent;
            if ((!event.isFromSource(InputDevice.SOURCE_MOUSE_RELATIVE) || (ViewRootImpl.this.hasPointerCapture() && !ViewRootImpl.this.mView.dispatchCapturedPointerEvent(event))) && !ViewRootImpl.this.mView.dispatchTrackballEvent(event)) {
                return 0;
            }
            return 1;
        }

        private int processGenericMotionEvent(QueuedInputEvent q) {
            MotionEvent event = (MotionEvent) q.mEvent;
            if ((!event.isFromSource(InputDevice.SOURCE_TOUCHPAD) || !ViewRootImpl.this.hasPointerCapture() || !ViewRootImpl.this.mView.dispatchCapturedPointerEvent(event)) && !ViewRootImpl.this.mView.dispatchGenericMotionEvent(event)) {
                return 0;
            }
            return 1;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void resetPointerIcon(MotionEvent event) {
        this.mPointerIconType = 1;
        updatePointerIcon(event);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    @OppoHook(level = OppoHook.OppoHookType.CHANGE_CODE, note = "XiaoKang.Feng@ROM.SDK : Modify for direct", property = OppoHook.OppoRomType.ROM)
    private boolean updatePointerIcon(MotionEvent event) {
        float x = event.getX(0);
        float y = event.getY(0);
        View view = this.mView;
        if (view == null) {
            Slog.d(this.mTag, "updatePointerIcon called after view was removed");
            return false;
        } else if (x < 0.0f || x >= ((float) view.getWidth()) || y < 0.0f || y >= ((float) this.mView.getHeight())) {
            Slog.d(this.mTag, "updatePointerIcon called with position out of bounds");
            return false;
        } else {
            PointerIcon pointerIcon = this.mView.onResolvePointerIcon(event, 0);
            int pointerType = pointerIcon != null ? pointerIcon.getType() : 1000;
            if (this.mPointerIconType != pointerType) {
                this.mPointerIconType = pointerType;
                this.mCustomPointerIcon = null;
                if (this.mPointerIconType != -1) {
                    InputManager.getInstance().setPointerIconType(pointerType);
                    return true;
                }
            }
            if (this.mPointerIconType == -1 && !pointerIcon.equals(this.mCustomPointerIcon)) {
                this.mCustomPointerIcon = pointerIcon;
                InputManager.getInstance().setCustomPointerIcon(this.mCustomPointerIcon);
            }
            return true;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void maybeUpdateTooltip(MotionEvent event) {
        if (event.getPointerCount() == 1) {
            int action = event.getActionMasked();
            if (action == 9 || action == 7 || action == 10) {
                AccessibilityManager manager = AccessibilityManager.getInstance(this.mContext);
                if (!manager.isEnabled() || !manager.isOppoTouchExplorationEnabled()) {
                    View view = this.mView;
                    if (view == null) {
                        Slog.d(this.mTag, "maybeUpdateTooltip called after view was removed");
                    } else {
                        view.dispatchTooltipHoverEvent(event);
                    }
                }
            }
        }
    }

    final class SyntheticInputStage extends InputStage {
        private final SyntheticJoystickHandler mJoystick = new SyntheticJoystickHandler();
        private final SyntheticKeyboardHandler mKeyboard = new SyntheticKeyboardHandler();
        private final SyntheticTouchNavigationHandler mTouchNavigation = new SyntheticTouchNavigationHandler();
        private final SyntheticTrackballHandler mTrackball = new SyntheticTrackballHandler();

        public SyntheticInputStage() {
            super(null);
        }

        /* access modifiers changed from: protected */
        @Override // android.view.ViewRootImpl.InputStage
        public int onProcess(QueuedInputEvent q) {
            q.mFlags |= 16;
            if (q.mEvent instanceof MotionEvent) {
                MotionEvent event = (MotionEvent) q.mEvent;
                int source = event.getSource();
                if ((source & 4) != 0) {
                    this.mTrackball.process(event);
                    return 1;
                } else if ((source & 16) != 0) {
                    this.mJoystick.process(event);
                    return 1;
                } else if ((source & 2097152) != 2097152) {
                    return 0;
                } else {
                    this.mTouchNavigation.process(event);
                    return 1;
                }
            } else if ((q.mFlags & 32) == 0) {
                return 0;
            } else {
                this.mKeyboard.process((KeyEvent) q.mEvent);
                return 1;
            }
        }

        /* access modifiers changed from: protected */
        @Override // android.view.ViewRootImpl.InputStage
        public void onDeliverToNext(QueuedInputEvent q) {
            if ((q.mFlags & 16) == 0 && (q.mEvent instanceof MotionEvent)) {
                MotionEvent event = (MotionEvent) q.mEvent;
                int source = event.getSource();
                if ((source & 4) != 0) {
                    this.mTrackball.cancel();
                } else if ((source & 16) != 0) {
                    this.mJoystick.cancel();
                } else if ((source & 2097152) == 2097152) {
                    this.mTouchNavigation.cancel(event);
                }
            }
            super.onDeliverToNext(q);
        }

        /* access modifiers changed from: protected */
        @Override // android.view.ViewRootImpl.InputStage
        public void onWindowFocusChanged(boolean hasWindowFocus) {
            if (!hasWindowFocus) {
                this.mJoystick.cancel();
            }
        }

        /* access modifiers changed from: protected */
        @Override // android.view.ViewRootImpl.InputStage
        public void onDetachedFromWindow() {
            this.mJoystick.cancel();
        }
    }

    final class SyntheticTrackballHandler {
        private long mLastTime;
        private final TrackballAxis mX = new TrackballAxis();
        private final TrackballAxis mY = new TrackballAxis();

        SyntheticTrackballHandler() {
        }

        public void process(MotionEvent event) {
            long curTime;
            int i;
            int keycode;
            float accel;
            String str;
            long curTime2;
            String str2;
            int keycode2;
            int keycode3;
            long curTime3 = SystemClock.uptimeMillis();
            if (this.mLastTime + 250 < curTime3) {
                this.mX.reset(0);
                this.mY.reset(0);
                this.mLastTime = curTime3;
            }
            int action = event.getAction();
            int metaState = event.getMetaState();
            if (action == 0) {
                this.mX.reset(2);
                this.mY.reset(2);
                curTime = curTime3;
                i = 2;
                ViewRootImpl.this.enqueueInputEvent(new KeyEvent(curTime3, curTime3, 0, 23, 0, metaState, -1, 0, 1024, 257));
            } else if (action != 1) {
                curTime = curTime3;
                i = 2;
            } else {
                this.mX.reset(2);
                this.mY.reset(2);
                ViewRootImpl.this.enqueueInputEvent(new KeyEvent(curTime3, curTime3, 1, 23, 0, metaState, -1, 0, 1024, 257));
                curTime = curTime3;
                i = 2;
            }
            if (ViewRootImpl.DEBUG_TRACKBALL) {
                Log.v(ViewRootImpl.this.mTag, "TB X=" + this.mX.position + " step=" + this.mX.step + " dir=" + this.mX.dir + " acc=" + this.mX.acceleration + " move=" + event.getX() + " / Y=" + this.mY.position + " step=" + this.mY.step + " dir=" + this.mY.dir + " acc=" + this.mY.acceleration + " move=" + event.getY());
            }
            float xOff = this.mX.collect(event.getX(), event.getEventTime(), "X");
            float yOff = this.mY.collect(event.getY(), event.getEventTime(), "Y");
            int movement = 0;
            if (xOff > yOff) {
                movement = this.mX.generate();
                if (movement != 0) {
                    if (movement > 0) {
                        keycode3 = 22;
                    } else {
                        keycode3 = 21;
                    }
                    float accel2 = this.mX.acceleration;
                    this.mY.reset(i);
                    keycode = keycode3;
                    accel = accel2;
                } else {
                    keycode = 0;
                    accel = 1.0f;
                }
            } else if (yOff > 0.0f) {
                movement = this.mY.generate();
                if (movement != 0) {
                    if (movement > 0) {
                        keycode2 = 20;
                    } else {
                        keycode2 = 19;
                    }
                    float accel3 = this.mY.acceleration;
                    this.mX.reset(i);
                    keycode = keycode2;
                    accel = accel3;
                } else {
                    keycode = 0;
                    accel = 1.0f;
                }
            } else {
                keycode = 0;
                accel = 1.0f;
            }
            if (keycode != 0) {
                if (movement < 0) {
                    movement = -movement;
                }
                int accelMovement = (int) (((float) movement) * accel);
                if (ViewRootImpl.DEBUG_TRACKBALL) {
                    Log.v(ViewRootImpl.this.mTag, "Move: movement=" + movement + " accelMovement=" + accelMovement + " accel=" + accel);
                }
                if (accelMovement > movement) {
                    if (ViewRootImpl.DEBUG_TRACKBALL) {
                        Log.v(ViewRootImpl.this.mTag, "Delivering fake DPAD: " + keycode);
                    }
                    int movement2 = movement - 1;
                    str = "Delivering fake DPAD: ";
                    ViewRootImpl.this.enqueueInputEvent(new KeyEvent(curTime, curTime, 2, keycode, accelMovement - movement2, metaState, -1, 0, 1024, 257));
                    curTime2 = curTime;
                    movement = movement2;
                } else {
                    str = "Delivering fake DPAD: ";
                    curTime2 = curTime;
                }
                while (movement > 0) {
                    if (ViewRootImpl.DEBUG_TRACKBALL) {
                        String str3 = ViewRootImpl.this.mTag;
                        StringBuilder sb = new StringBuilder();
                        str2 = str;
                        sb.append(str2);
                        sb.append(keycode);
                        Log.v(str3, sb.toString());
                    } else {
                        str2 = str;
                    }
                    movement--;
                    curTime2 = SystemClock.uptimeMillis();
                    ViewRootImpl.this.enqueueInputEvent(new KeyEvent(curTime2, curTime2, 0, keycode, 0, metaState, -1, 0, 1024, 257));
                    ViewRootImpl.this.enqueueInputEvent(new KeyEvent(curTime2, curTime2, 1, keycode, 0, metaState, -1, 0, 1024, 257));
                    str = str2;
                }
                this.mLastTime = curTime2;
            }
        }

        public void cancel() {
            this.mLastTime = -2147483648L;
            if (ViewRootImpl.this.mView != null && ViewRootImpl.this.mAdded) {
                ViewRootImpl.this.ensureTouchMode(false);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public static final class TrackballAxis {
        static final float ACCEL_MOVE_SCALING_FACTOR = 0.025f;
        static final long FAST_MOVE_TIME = 150;
        static final float FIRST_MOVEMENT_THRESHOLD = 0.5f;
        static final float MAX_ACCELERATION = 20.0f;
        static final float SECOND_CUMULATIVE_MOVEMENT_THRESHOLD = 2.0f;
        static final float SUBSEQUENT_INCREMENTAL_MOVEMENT_THRESHOLD = 1.0f;
        float acceleration = 1.0f;
        int dir;
        long lastMoveTime = 0;
        int nonAccelMovement;
        float position;
        int step;

        TrackballAxis() {
        }

        /* access modifiers changed from: package-private */
        public void reset(int _step) {
            this.position = 0.0f;
            this.acceleration = 1.0f;
            this.lastMoveTime = 0;
            this.step = _step;
            this.dir = 0;
        }

        /* access modifiers changed from: package-private */
        public float collect(float off, long time, String axis) {
            long normTime;
            if (off > 0.0f) {
                normTime = (long) (off * 150.0f);
                if (this.dir < 0) {
                    if (ViewRootImpl.DEBUG_TRACKBALL) {
                        Log.v(ViewRootImpl.TAG, axis + " reversed to positive!");
                    }
                    this.position = 0.0f;
                    this.step = 0;
                    this.acceleration = 1.0f;
                    this.lastMoveTime = 0;
                }
                this.dir = 1;
            } else if (off < 0.0f) {
                normTime = (long) ((-off) * 150.0f);
                if (this.dir > 0) {
                    if (ViewRootImpl.DEBUG_TRACKBALL) {
                        Log.v(ViewRootImpl.TAG, axis + " reversed to negative!");
                    }
                    this.position = 0.0f;
                    this.step = 0;
                    this.acceleration = 1.0f;
                    this.lastMoveTime = 0;
                }
                this.dir = -1;
            } else {
                normTime = 0;
            }
            if (normTime > 0) {
                long delta = time - this.lastMoveTime;
                this.lastMoveTime = time;
                float acc = this.acceleration;
                if (delta < normTime) {
                    float scale = ((float) (normTime - delta)) * ACCEL_MOVE_SCALING_FACTOR;
                    if (scale > 1.0f) {
                        acc *= scale;
                    }
                    if (ViewRootImpl.DEBUG_TRACKBALL) {
                        Log.v(ViewRootImpl.TAG, axis + " accelerate: off=" + off + " normTime=" + normTime + " delta=" + delta + " scale=" + scale + " acc=" + acc);
                    }
                    float f = MAX_ACCELERATION;
                    if (acc < MAX_ACCELERATION) {
                        f = acc;
                    }
                    this.acceleration = f;
                } else {
                    float scale2 = ((float) (delta - normTime)) * ACCEL_MOVE_SCALING_FACTOR;
                    if (scale2 > 1.0f) {
                        acc /= scale2;
                    }
                    if (ViewRootImpl.DEBUG_TRACKBALL) {
                        Log.v(ViewRootImpl.TAG, axis + " deccelerate: off=" + off + " normTime=" + normTime + " delta=" + delta + " scale=" + scale2 + " acc=" + acc);
                    }
                    float f2 = 1.0f;
                    if (acc > 1.0f) {
                        f2 = acc;
                    }
                    this.acceleration = f2;
                }
            }
            this.position += off;
            return Math.abs(this.position);
        }

        /* access modifiers changed from: package-private */
        public int generate() {
            int movement = 0;
            this.nonAccelMovement = 0;
            while (true) {
                int dir2 = this.position >= 0.0f ? 1 : -1;
                int i = this.step;
                if (i != 0) {
                    if (i != 1) {
                        if (Math.abs(this.position) < 1.0f) {
                            return movement;
                        }
                        movement += dir2;
                        this.position -= ((float) dir2) * 1.0f;
                        float acc = this.acceleration * 1.1f;
                        this.acceleration = acc < MAX_ACCELERATION ? acc : this.acceleration;
                    } else if (Math.abs(this.position) < SECOND_CUMULATIVE_MOVEMENT_THRESHOLD) {
                        return movement;
                    } else {
                        movement += dir2;
                        this.nonAccelMovement += dir2;
                        this.position -= ((float) dir2) * SECOND_CUMULATIVE_MOVEMENT_THRESHOLD;
                        this.step = 2;
                    }
                } else if (Math.abs(this.position) < 0.5f) {
                    return movement;
                } else {
                    movement += dir2;
                    this.nonAccelMovement += dir2;
                    this.step = 1;
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public final class SyntheticJoystickHandler extends Handler {
        private static final int MSG_ENQUEUE_X_AXIS_KEY_REPEAT = 1;
        private static final int MSG_ENQUEUE_Y_AXIS_KEY_REPEAT = 2;
        private final SparseArray<KeyEvent> mDeviceKeyEvents = new SparseArray<>();
        private final JoystickAxesState mJoystickAxesState = new JoystickAxesState();

        public SyntheticJoystickHandler() {
            super(true);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            int i = msg.what;
            if ((i == 1 || i == 2) && ViewRootImpl.this.mAttachInfo.mHasWindowFocus) {
                KeyEvent oldEvent = (KeyEvent) msg.obj;
                KeyEvent e = KeyEvent.changeTimeRepeat(oldEvent, SystemClock.uptimeMillis(), oldEvent.getRepeatCount() + 1);
                ViewRootImpl.this.enqueueInputEvent(e);
                Message m = obtainMessage(msg.what, e);
                m.setAsynchronous(true);
                sendMessageDelayed(m, (long) ViewConfiguration.getKeyRepeatDelay());
            }
        }

        public void process(MotionEvent event) {
            int actionMasked = event.getActionMasked();
            if (actionMasked == 2) {
                update(event);
            } else if (actionMasked != 3) {
                String str = ViewRootImpl.this.mTag;
                Log.w(str, "Unexpected action: " + event.getActionMasked());
            } else {
                cancel();
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void cancel() {
            removeMessages(1);
            removeMessages(2);
            for (int i = 0; i < this.mDeviceKeyEvents.size(); i++) {
                KeyEvent keyEvent = this.mDeviceKeyEvents.valueAt(i);
                if (keyEvent != null) {
                    ViewRootImpl.this.enqueueInputEvent(KeyEvent.changeTimeRepeat(keyEvent, SystemClock.uptimeMillis(), 0));
                }
            }
            this.mDeviceKeyEvents.clear();
            this.mJoystickAxesState.resetState();
        }

        private void update(MotionEvent event) {
            int historySize = event.getHistorySize();
            for (int h = 0; h < historySize; h++) {
                long time = event.getHistoricalEventTime(h);
                this.mJoystickAxesState.updateStateForAxis(event, time, 0, event.getHistoricalAxisValue(0, 0, h));
                this.mJoystickAxesState.updateStateForAxis(event, time, 1, event.getHistoricalAxisValue(1, 0, h));
                this.mJoystickAxesState.updateStateForAxis(event, time, 15, event.getHistoricalAxisValue(15, 0, h));
                this.mJoystickAxesState.updateStateForAxis(event, time, 16, event.getHistoricalAxisValue(16, 0, h));
            }
            long time2 = event.getEventTime();
            this.mJoystickAxesState.updateStateForAxis(event, time2, 0, event.getAxisValue(0));
            this.mJoystickAxesState.updateStateForAxis(event, time2, 1, event.getAxisValue(1));
            this.mJoystickAxesState.updateStateForAxis(event, time2, 15, event.getAxisValue(15));
            this.mJoystickAxesState.updateStateForAxis(event, time2, 16, event.getAxisValue(16));
        }

        /* access modifiers changed from: package-private */
        public final class JoystickAxesState {
            private static final int STATE_DOWN_OR_RIGHT = 1;
            private static final int STATE_NEUTRAL = 0;
            private static final int STATE_UP_OR_LEFT = -1;
            final int[] mAxisStatesHat = {0, 0};
            final int[] mAxisStatesStick = {0, 0};

            JoystickAxesState() {
            }

            /* access modifiers changed from: package-private */
            public void resetState() {
                int[] iArr = this.mAxisStatesHat;
                iArr[0] = 0;
                iArr[1] = 0;
                int[] iArr2 = this.mAxisStatesStick;
                iArr2[0] = 0;
                iArr2[1] = 0;
            }

            /* access modifiers changed from: package-private */
            public void updateStateForAxis(MotionEvent event, long time, int axis, float value) {
                int repeatMessage;
                int axisStateIndex;
                int currentState;
                if (isXAxis(axis)) {
                    axisStateIndex = 0;
                    repeatMessage = 1;
                } else if (isYAxis(axis)) {
                    axisStateIndex = 1;
                    repeatMessage = 2;
                } else {
                    String str = ViewRootImpl.this.mTag;
                    Log.e(str, "Unexpected axis " + axis + " in updateStateForAxis!");
                    return;
                }
                int newState = joystickAxisValueToState(value);
                if (axis == 0 || axis == 1) {
                    currentState = this.mAxisStatesStick[axisStateIndex];
                } else {
                    currentState = this.mAxisStatesHat[axisStateIndex];
                }
                if (currentState != newState) {
                    int metaState = event.getMetaState();
                    int deviceId = event.getDeviceId();
                    int source = event.getSource();
                    if (currentState == 1 || currentState == -1) {
                        int keyCode = joystickAxisAndStateToKeycode(axis, currentState);
                        if (keyCode != 0) {
                            ViewRootImpl.this.enqueueInputEvent(new KeyEvent(time, time, 1, keyCode, 0, metaState, deviceId, 0, 1024, source));
                            deviceId = deviceId;
                            SyntheticJoystickHandler.this.mDeviceKeyEvents.put(deviceId, null);
                        }
                        SyntheticJoystickHandler.this.removeMessages(repeatMessage);
                    }
                    if (newState == 1 || newState == -1) {
                        int keyCode2 = joystickAxisAndStateToKeycode(axis, newState);
                        if (keyCode2 != 0) {
                            KeyEvent keyEvent = new KeyEvent(time, time, 0, keyCode2, 0, metaState, deviceId, 0, 1024, source);
                            ViewRootImpl.this.enqueueInputEvent(keyEvent);
                            Message m = SyntheticJoystickHandler.this.obtainMessage(repeatMessage, keyEvent);
                            m.setAsynchronous(true);
                            SyntheticJoystickHandler.this.sendMessageDelayed(m, (long) ViewConfiguration.getKeyRepeatTimeout());
                            SyntheticJoystickHandler.this.mDeviceKeyEvents.put(deviceId, new KeyEvent(time, time, 1, keyCode2, 0, metaState, deviceId, 0, 1056, source));
                        }
                    }
                    if (axis == 0 || axis == 1) {
                        this.mAxisStatesStick[axisStateIndex] = newState;
                    } else {
                        this.mAxisStatesHat[axisStateIndex] = newState;
                    }
                }
            }

            private boolean isXAxis(int axis) {
                return axis == 0 || axis == 15;
            }

            private boolean isYAxis(int axis) {
                return axis == 1 || axis == 16;
            }

            private int joystickAxisAndStateToKeycode(int axis, int state) {
                if (isXAxis(axis) && state == -1) {
                    return 21;
                }
                if (isXAxis(axis) && state == 1) {
                    return 22;
                }
                if (isYAxis(axis) && state == -1) {
                    return 19;
                }
                if (isYAxis(axis) && state == 1) {
                    return 20;
                }
                String str = ViewRootImpl.this.mTag;
                Log.e(str, "Unknown axis " + axis + " or direction " + state);
                return 0;
            }

            private int joystickAxisValueToState(float value) {
                if (value >= 0.5f) {
                    return 1;
                }
                if (value <= -0.5f) {
                    return -1;
                }
                return 0;
            }
        }
    }

    final class SyntheticTouchNavigationHandler extends Handler {
        private static final float DEFAULT_HEIGHT_MILLIMETERS = 48.0f;
        private static final float DEFAULT_WIDTH_MILLIMETERS = 48.0f;
        private static final float FLING_TICK_DECAY = 0.8f;
        private static final boolean LOCAL_DEBUG = false;
        private static final String LOCAL_TAG = "SyntheticTouchNavigationHandler";
        private static final float MAX_FLING_VELOCITY_TICKS_PER_SECOND = 20.0f;
        private static final float MIN_FLING_VELOCITY_TICKS_PER_SECOND = 6.0f;
        private static final int TICK_DISTANCE_MILLIMETERS = 12;
        private float mAccumulatedX;
        private float mAccumulatedY;
        private int mActivePointerId = -1;
        private float mConfigMaxFlingVelocity;
        private float mConfigMinFlingVelocity;
        private float mConfigTickDistance;
        private boolean mConsumedMovement;
        private int mCurrentDeviceId = -1;
        private boolean mCurrentDeviceSupported;
        private int mCurrentSource;
        private final Runnable mFlingRunnable = new Runnable() {
            /* class android.view.ViewRootImpl.SyntheticTouchNavigationHandler.AnonymousClass1 */

            public void run() {
                long time = SystemClock.uptimeMillis();
                SyntheticTouchNavigationHandler syntheticTouchNavigationHandler = SyntheticTouchNavigationHandler.this;
                syntheticTouchNavigationHandler.sendKeyDownOrRepeat(time, syntheticTouchNavigationHandler.mPendingKeyCode, SyntheticTouchNavigationHandler.this.mPendingKeyMetaState);
                SyntheticTouchNavigationHandler.access$3432(SyntheticTouchNavigationHandler.this, SyntheticTouchNavigationHandler.FLING_TICK_DECAY);
                if (!SyntheticTouchNavigationHandler.this.postFling(time)) {
                    SyntheticTouchNavigationHandler.this.mFlinging = false;
                    SyntheticTouchNavigationHandler.this.finishKeys(time);
                }
            }
        };
        private float mFlingVelocity;
        private boolean mFlinging;
        private float mLastX;
        private float mLastY;
        private int mPendingKeyCode = 0;
        private long mPendingKeyDownTime;
        private int mPendingKeyMetaState;
        private int mPendingKeyRepeatCount;
        private float mStartX;
        private float mStartY;
        private VelocityTracker mVelocityTracker;

        static /* synthetic */ float access$3432(SyntheticTouchNavigationHandler x0, float x1) {
            float f = x0.mFlingVelocity * x1;
            x0.mFlingVelocity = f;
            return f;
        }

        public SyntheticTouchNavigationHandler() {
            super(true);
        }

        /* JADX INFO: Multiple debug info for r8v1 boolean: [D('x' float), D('caughtFling' boolean)] */
        public void process(MotionEvent event) {
            long time = event.getEventTime();
            int deviceId = event.getDeviceId();
            int source = event.getSource();
            if (!(this.mCurrentDeviceId == deviceId && this.mCurrentSource == source)) {
                finishKeys(time);
                finishTracking(time);
                this.mCurrentDeviceId = deviceId;
                this.mCurrentSource = source;
                this.mCurrentDeviceSupported = false;
                InputDevice device = event.getDevice();
                if (device != null) {
                    InputDevice.MotionRange xRange = device.getMotionRange(0);
                    InputDevice.MotionRange yRange = device.getMotionRange(1);
                    if (!(xRange == null || yRange == null)) {
                        this.mCurrentDeviceSupported = true;
                        float xRes = xRange.getResolution();
                        if (xRes <= 0.0f) {
                            xRes = xRange.getRange() / 48.0f;
                        }
                        float yRes = yRange.getResolution();
                        if (yRes <= 0.0f) {
                            yRes = yRange.getRange() / 48.0f;
                        }
                        this.mConfigTickDistance = 12.0f * (xRes + yRes) * 0.5f;
                        float f = this.mConfigTickDistance;
                        this.mConfigMinFlingVelocity = MIN_FLING_VELOCITY_TICKS_PER_SECOND * f;
                        this.mConfigMaxFlingVelocity = f * MAX_FLING_VELOCITY_TICKS_PER_SECOND;
                    }
                }
            }
            if (this.mCurrentDeviceSupported) {
                int action = event.getActionMasked();
                if (action == 0) {
                    boolean caughtFling = this.mFlinging;
                    finishKeys(time);
                    finishTracking(time);
                    this.mActivePointerId = event.getPointerId(0);
                    this.mVelocityTracker = VelocityTracker.obtain();
                    this.mVelocityTracker.addMovement(event);
                    this.mStartX = event.getX();
                    this.mStartY = event.getY();
                    this.mLastX = this.mStartX;
                    this.mLastY = this.mStartY;
                    this.mAccumulatedX = 0.0f;
                    this.mAccumulatedY = 0.0f;
                    this.mConsumedMovement = caughtFling;
                } else if (action == 1 || action == 2) {
                    int i = this.mActivePointerId;
                    if (i >= 0) {
                        int index = event.findPointerIndex(i);
                        if (index < 0) {
                            finishKeys(time);
                            finishTracking(time);
                            return;
                        }
                        this.mVelocityTracker.addMovement(event);
                        float x = event.getX(index);
                        float y = event.getY(index);
                        this.mAccumulatedX += x - this.mLastX;
                        this.mAccumulatedY += y - this.mLastY;
                        this.mLastX = x;
                        this.mLastY = y;
                        consumeAccumulatedMovement(time, event.getMetaState());
                        if (action == 1) {
                            if (this.mConsumedMovement && this.mPendingKeyCode != 0) {
                                this.mVelocityTracker.computeCurrentVelocity(1000, this.mConfigMaxFlingVelocity);
                                if (!startFling(time, this.mVelocityTracker.getXVelocity(this.mActivePointerId), this.mVelocityTracker.getYVelocity(this.mActivePointerId))) {
                                    finishKeys(time);
                                }
                            }
                            finishTracking(time);
                        }
                    }
                } else if (action == 3) {
                    finishKeys(time);
                    finishTracking(time);
                }
            }
        }

        public void cancel(MotionEvent event) {
            if (this.mCurrentDeviceId == event.getDeviceId() && this.mCurrentSource == event.getSource()) {
                long time = event.getEventTime();
                finishKeys(time);
                finishTracking(time);
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void finishKeys(long time) {
            cancelFling();
            sendKeyUp(time);
        }

        private void finishTracking(long time) {
            if (this.mActivePointerId >= 0) {
                this.mActivePointerId = -1;
                this.mVelocityTracker.recycle();
                this.mVelocityTracker = null;
            }
        }

        private void consumeAccumulatedMovement(long time, int metaState) {
            float absX = Math.abs(this.mAccumulatedX);
            float absY = Math.abs(this.mAccumulatedY);
            if (absX >= absY) {
                if (absX >= this.mConfigTickDistance) {
                    this.mAccumulatedX = consumeAccumulatedMovement(time, metaState, this.mAccumulatedX, 21, 22);
                    this.mAccumulatedY = 0.0f;
                    this.mConsumedMovement = true;
                }
            } else if (absY >= this.mConfigTickDistance) {
                this.mAccumulatedY = consumeAccumulatedMovement(time, metaState, this.mAccumulatedY, 19, 20);
                this.mAccumulatedX = 0.0f;
                this.mConsumedMovement = true;
            }
        }

        private float consumeAccumulatedMovement(long time, int metaState, float accumulator, int negativeKeyCode, int positiveKeyCode) {
            while (accumulator <= (-this.mConfigTickDistance)) {
                sendKeyDownOrRepeat(time, negativeKeyCode, metaState);
                accumulator += this.mConfigTickDistance;
            }
            while (accumulator >= this.mConfigTickDistance) {
                sendKeyDownOrRepeat(time, positiveKeyCode, metaState);
                accumulator -= this.mConfigTickDistance;
            }
            return accumulator;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void sendKeyDownOrRepeat(long time, int keyCode, int metaState) {
            if (this.mPendingKeyCode != keyCode) {
                sendKeyUp(time);
                this.mPendingKeyDownTime = time;
                this.mPendingKeyCode = keyCode;
                this.mPendingKeyRepeatCount = 0;
            } else {
                this.mPendingKeyRepeatCount++;
            }
            this.mPendingKeyMetaState = metaState;
            ViewRootImpl.this.enqueueInputEvent(new KeyEvent(this.mPendingKeyDownTime, time, 0, this.mPendingKeyCode, this.mPendingKeyRepeatCount, this.mPendingKeyMetaState, this.mCurrentDeviceId, 1024, this.mCurrentSource));
        }

        private void sendKeyUp(long time) {
            int i = this.mPendingKeyCode;
            if (i != 0) {
                ViewRootImpl.this.enqueueInputEvent(new KeyEvent(this.mPendingKeyDownTime, time, 1, i, 0, this.mPendingKeyMetaState, this.mCurrentDeviceId, 0, 1024, this.mCurrentSource));
                this.mPendingKeyCode = 0;
            }
        }

        private boolean startFling(long time, float vx, float vy) {
            switch (this.mPendingKeyCode) {
                case 19:
                    if ((-vy) >= this.mConfigMinFlingVelocity && Math.abs(vx) < this.mConfigMinFlingVelocity) {
                        this.mFlingVelocity = -vy;
                        break;
                    } else {
                        return false;
                    }
                case 20:
                    if (vy >= this.mConfigMinFlingVelocity && Math.abs(vx) < this.mConfigMinFlingVelocity) {
                        this.mFlingVelocity = vy;
                        break;
                    } else {
                        return false;
                    }
                case 21:
                    if ((-vx) >= this.mConfigMinFlingVelocity && Math.abs(vy) < this.mConfigMinFlingVelocity) {
                        this.mFlingVelocity = -vx;
                        break;
                    } else {
                        return false;
                    }
                    break;
                case 22:
                    if (vx >= this.mConfigMinFlingVelocity && Math.abs(vy) < this.mConfigMinFlingVelocity) {
                        this.mFlingVelocity = vx;
                        break;
                    } else {
                        return false;
                    }
            }
            this.mFlinging = postFling(time);
            return this.mFlinging;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private boolean postFling(long time) {
            float f = this.mFlingVelocity;
            if (f < this.mConfigMinFlingVelocity) {
                return false;
            }
            postAtTime(this.mFlingRunnable, time + ((long) ((this.mConfigTickDistance / f) * 1000.0f)));
            return true;
        }

        private void cancelFling() {
            if (this.mFlinging) {
                removeCallbacks(this.mFlingRunnable);
                this.mFlinging = false;
            }
        }
    }

    final class SyntheticKeyboardHandler {
        SyntheticKeyboardHandler() {
        }

        public void process(KeyEvent event) {
            if ((event.getFlags() & 1024) == 0) {
                KeyCharacterMap.FallbackAction fallbackAction = event.getKeyCharacterMap().getFallbackAction(event.getKeyCode(), event.getMetaState());
                if (fallbackAction != null) {
                    KeyEvent fallbackEvent = KeyEvent.obtain(event.getDownTime(), event.getEventTime(), event.getAction(), fallbackAction.keyCode, event.getRepeatCount(), fallbackAction.metaState, event.getDeviceId(), event.getScanCode(), event.getFlags() | 1024, event.getSource(), null);
                    fallbackAction.recycle();
                    ViewRootImpl.this.enqueueInputEvent(fallbackEvent);
                }
            }
        }
    }

    private static boolean isNavigationKey(KeyEvent keyEvent) {
        int keyCode = keyEvent.getKeyCode();
        if (!(keyCode == 61 || keyCode == 62)) {
            if (keyCode != 66) {
                if (!(keyCode == 92 || keyCode == 93 || keyCode == 122 || keyCode == 123)) {
                    switch (keyCode) {
                        case 19:
                        case 20:
                        case 21:
                        case 22:
                        case 23:
                            break;
                        default:
                            return false;
                    }
                }
            } else if (ActivityThread.currentPackageName() == null || ActivityThread.currentPackageName().toLowerCase().contains(Context.CAMERA_SERVICE)) {
                return false;
            } else {
                return true;
            }
        }
        return true;
    }

    private static boolean isTypingKey(KeyEvent keyEvent) {
        return keyEvent.getUnicodeChar() > 0;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean checkForLeavingTouchModeAndConsume(KeyEvent event) {
        if (!this.mAttachInfo.mInTouchMode) {
            return false;
        }
        int action = event.getAction();
        if ((action != 0 && action != 2) || (event.getFlags() & 4) != 0) {
            return false;
        }
        if (isNavigationKey(event)) {
            return ensureTouchMode(false);
        }
        if (!isTypingKey(event)) {
            return false;
        }
        ensureTouchMode(false);
        return false;
    }

    /* access modifiers changed from: package-private */
    @UnsupportedAppUsage
    public void setLocalDragState(Object obj) {
        this.mLocalDragState = obj;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleDragEvent(DragEvent event) {
        if (this.mView != null && this.mAdded) {
            int what = event.mAction;
            if (what == 1) {
                this.mCurrentDragView = null;
                this.mDragDescription = event.mClipDescription;
            } else {
                if (what == 4) {
                    this.mDragDescription = null;
                }
                event.mClipDescription = this.mDragDescription;
            }
            if (what == 6) {
                if (View.sCascadedDragDrop) {
                    this.mView.dispatchDragEnterExitInPreN(event);
                }
                setDragFocus(null, event);
            } else {
                if (what == 2 || what == 3) {
                    this.mDragPoint.set(event.mX, event.mY);
                    CompatibilityInfo.Translator translator = this.mTranslator;
                    if (translator != null) {
                        translator.translatePointInScreenToAppWindow(this.mDragPoint);
                    }
                    int i = this.mCurScrollY;
                    if (i != 0) {
                        this.mDragPoint.offset(0.0f, (float) i);
                    }
                    event.mX = this.mDragPoint.x;
                    event.mY = this.mDragPoint.y;
                }
                View prevDragView = this.mCurrentDragView;
                if (what == 3 && event.mClipData != null) {
                    event.mClipData.prepareToEnterProcess();
                }
                boolean result = this.mView.dispatchDragEvent(event);
                if (what == 2 && !event.mEventHandlerWasCalled) {
                    setDragFocus(null, event);
                }
                if (prevDragView != this.mCurrentDragView) {
                    if (prevDragView != null) {
                        try {
                            this.mWindowSession.dragRecipientExited(this.mWindow);
                        } catch (RemoteException e) {
                            Slog.e(this.mTag, "Unable to note drag target change");
                        }
                    }
                    if (this.mCurrentDragView != null) {
                        this.mWindowSession.dragRecipientEntered(this.mWindow);
                    }
                }
                if (what == 3) {
                    try {
                        String str = this.mTag;
                        Log.i(str, "Reporting drop result: " + result);
                        this.mWindowSession.reportDropResult(this.mWindow, result);
                    } catch (RemoteException e2) {
                        Log.e(this.mTag, "Unable to report drop result");
                    }
                }
                if (what == 4) {
                    this.mCurrentDragView = null;
                    setLocalDragState(null);
                    View.AttachInfo attachInfo = this.mAttachInfo;
                    attachInfo.mDragToken = null;
                    if (attachInfo.mDragSurface != null) {
                        this.mAttachInfo.mDragSurface.release();
                        this.mAttachInfo.mDragSurface = null;
                    }
                }
            }
        }
        event.recycle();
    }

    public void handleDispatchSystemUiVisibilityChanged(SystemUiVisibilityInfo args) {
        if (this.mSeq != args.seq) {
            this.mSeq = args.seq;
            this.mAttachInfo.mForceReportNewAttributes = true;
            scheduleTraversals();
        }
        if (this.mView != null) {
            if (args.localChanges != 0) {
                this.mView.updateLocalSystemUiVisibility(args.localValue, args.localChanges);
            }
            int visibility = args.globalVisibility & 7;
            if (visibility != this.mAttachInfo.mGlobalSystemUiVisibility) {
                this.mAttachInfo.mGlobalSystemUiVisibility = visibility;
                this.mView.dispatchSystemUiVisibilityChanged(visibility);
            }
        }
    }

    public void onWindowTitleChanged() {
        this.mAttachInfo.mForceReportNewAttributes = true;
    }

    public void handleDispatchWindowShown() {
        this.mAttachInfo.mTreeObserver.dispatchOnWindowShown();
    }

    public void handleRequestKeyboardShortcuts(IResultReceiver receiver, int deviceId) {
        Bundle data = new Bundle();
        ArrayList<KeyboardShortcutGroup> list = new ArrayList<>();
        View view = this.mView;
        if (view != null) {
            view.requestKeyboardShortcuts(list, deviceId);
        }
        data.putParcelableArrayList(WindowManager.PARCEL_KEY_SHORTCUTS_ARRAY, list);
        try {
            receiver.send(0, data);
        } catch (RemoteException e) {
        }
    }

    @UnsupportedAppUsage
    public void getLastTouchPoint(Point outLocation) {
        outLocation.x = (int) this.mLastTouchPoint.x;
        outLocation.y = (int) this.mLastTouchPoint.y;
    }

    public int getLastTouchSource() {
        return this.mLastTouchSource;
    }

    public void setDragFocus(View newDragTarget, DragEvent event) {
        if (this.mCurrentDragView != newDragTarget && !View.sCascadedDragDrop) {
            float tx = event.mX;
            float ty = event.mY;
            int action = event.mAction;
            ClipData td = event.mClipData;
            event.mX = 0.0f;
            event.mY = 0.0f;
            event.mClipData = null;
            View view = this.mCurrentDragView;
            if (view != null) {
                event.mAction = 6;
                view.callDragEventHandler(event);
            }
            if (newDragTarget != null) {
                event.mAction = 5;
                newDragTarget.callDragEventHandler(event);
            }
            event.mAction = action;
            event.mX = tx;
            event.mY = ty;
            event.mClipData = td;
        }
        this.mCurrentDragView = newDragTarget;
    }

    private AudioManager getAudioManager() {
        View view = this.mView;
        if (view != null) {
            if (this.mAudioManager == null) {
                this.mAudioManager = (AudioManager) view.getContext().getSystemService("audio");
            }
            return this.mAudioManager;
        }
        throw new IllegalStateException("getAudioManager called when there is no mView");
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private AutofillManager getAutofillManager() {
        View view = this.mView;
        if (!(view instanceof ViewGroup)) {
            return null;
        }
        ViewGroup decorView = (ViewGroup) view;
        if (decorView.getChildCount() > 0) {
            return (AutofillManager) decorView.getChildAt(0).getContext().getSystemService(AutofillManager.class);
        }
        return null;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isAutofillUiShowing() {
        AutofillManager afm = getAutofillManager();
        if (afm == null) {
            return false;
        }
        return afm.isAutofillUiShowing();
    }

    public AccessibilityInteractionController getAccessibilityInteractionController() {
        if (this.mView != null) {
            if (this.mAccessibilityInteractionController == null) {
                this.mAccessibilityInteractionController = new AccessibilityInteractionController(this);
            }
            return this.mAccessibilityInteractionController;
        }
        throw new IllegalStateException("getAccessibilityInteractionController called when there is no mView");
    }

    private int relayoutWindow(WindowManager.LayoutParams params, int viewVisibility, boolean insetsPending) throws RemoteException {
        boolean restore;
        long frameNumber;
        float appScale = this.mAttachInfo.mApplicationScale;
        if (params == null || this.mTranslator == null) {
            restore = false;
        } else {
            params.backup();
            this.mTranslator.translateWindowLayout(params);
            restore = true;
        }
        if (params != null) {
            if (DBG) {
                Log.d(this.mTag, "WindowLayout in layoutWindow:" + params);
            }
            if (DEBUG_LAYOUT) {
                Log.d(this.mTag, ">>>>>> CALLING relayoutW+ " + this.mWindow + ", params = " + params + ",viewVisibility = " + viewVisibility + ", insetsPending = " + insetsPending + ", appScale = " + appScale + ", mWinFrame = " + this.mWinFrame + ", mSeq = " + this.mSeq + ", mPendingOverscanInsets = " + this.mPendingOverscanInsets + ", mPendingContentInsets = " + this.mPendingContentInsets + ", mPendingVisibleInsets = " + this.mPendingVisibleInsets + ", mPendingStableInsets = " + this.mPendingStableInsets + ", mPendingOutsets = " + this.mPendingOutsets + ", mPendingMergedConfiguration = " + this.mPendingMergedConfiguration + ", mSurface = " + this.mSurface + ",valid = " + this.mSurface.isValid() + ", mOrigWindowType = " + this.mOrigWindowType + ",this = " + this);
            }
            if (this.mOrigWindowType != params.type && this.mTargetSdkVersion < 14) {
                Slog.w(this.mTag, "Window type can not be changed after the window is added; ignoring change of " + this.mView);
                params.type = this.mOrigWindowType;
            }
        }
        if (this.mSurface.isValid()) {
            frameNumber = this.mSurface.getNextFrameNumber();
        } else {
            frameNumber = -1;
        }
        int relayoutResult = this.mWindowSession.relayout(this.mWindow, this.mSeq, params, (int) ((((float) this.mView.getMeasuredWidth()) * appScale) + 0.5f), (int) ((((float) this.mView.getMeasuredHeight()) * appScale) + 0.5f), viewVisibility, insetsPending ? 1 : 0, frameNumber, this.mTmpFrame, this.mPendingOverscanInsets, this.mPendingContentInsets, this.mPendingVisibleInsets, this.mPendingStableInsets, this.mPendingOutsets, this.mPendingBackDropFrame, this.mPendingDisplayCutout, this.mPendingMergedConfiguration, this.mSurfaceControl, this.mTempInsets);
        this.mManager.refreshForceDark(this.mView, this.mPendingMergedConfiguration.mColorForceDarkValue);
        if (this.mSurfaceControl.isValid()) {
            this.mSurface.copyFrom(this.mSurfaceControl);
        } else {
            destroySurface();
        }
        this.mPendingAlwaysConsumeSystemBars = (relayoutResult & 64) != 0;
        if (DEBUG_LAYOUT) {
            Log.d(this.mTag, "<<<<<< BACK FROM relayoutW- : res = " + relayoutResult + ", mWinFrame = " + this.mWinFrame + ", mPendingOverscanInsets = " + this.mPendingOverscanInsets + ", mPendingContentInsets = " + this.mPendingContentInsets + ", mPendingVisibleInsets = " + this.mPendingVisibleInsets + ", mPendingStableInsets = " + this.mPendingStableInsets + ", mPendingOutsets = " + this.mPendingOutsets + ", mPendingMergedConfiguration = " + this.mPendingMergedConfiguration + ", mSurface = " + this.mSurface + ",valid = " + this.mSurface.isValid() + ",params = " + params + ", this = " + this);
        }
        if (restore) {
            params.restore();
        }
        CompatibilityInfo.Translator translator = this.mTranslator;
        if (translator != null) {
            translator.translateRectInScreenToAppWinFrame(this.mTmpFrame);
            this.mTranslator.translateRectInScreenToAppWindow(this.mPendingOverscanInsets);
            this.mTranslator.translateRectInScreenToAppWindow(this.mPendingContentInsets);
            this.mTranslator.translateRectInScreenToAppWindow(this.mPendingVisibleInsets);
            this.mTranslator.translateRectInScreenToAppWindow(this.mPendingStableInsets);
        }
        setFrame(this.mTmpFrame);
        this.mInsetsController.onStateChanged(this.mTempInsets);
        return relayoutResult;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setFrame(Rect frame) {
        this.mWinFrame.set(frame);
        this.mInsetsController.onFrameChanged(frame);
    }

    @Override // android.view.View.AttachInfo.Callbacks
    public void playSoundEffect(int effectId) {
        checkThread();
        try {
            AudioManager audioManager = getAudioManager();
            if (effectId == 0) {
                audioManager.playSoundEffect(0);
            } else if (effectId == 1) {
                audioManager.playSoundEffect(3);
            } else if (effectId == 2) {
                audioManager.playSoundEffect(1);
            } else if (effectId == 3) {
                audioManager.playSoundEffect(4);
            } else if (effectId == 4) {
                audioManager.playSoundEffect(2);
            } else {
                throw new IllegalArgumentException("unknown effect id " + effectId + " not defined in " + SoundEffectConstants.class.getCanonicalName());
            }
        } catch (IllegalStateException e) {
            String str = this.mTag;
            Log.e(str, "FATAL EXCEPTION when attempting to play sound effect: " + e);
            e.printStackTrace();
        }
    }

    @Override // android.view.View.AttachInfo.Callbacks
    public boolean performHapticFeedback(int effectId, boolean always) {
        try {
            return this.mWindowSession.performHapticFeedback(effectId, always);
        } catch (RemoteException e) {
            String str = this.mTag;
            Log.e(str, "performHapticFeedback RemoteException happens in " + this, e);
            return false;
        }
    }

    @Override // android.view.ViewParent
    public View focusSearch(View focused, int direction) {
        checkThread();
        if (!(this.mView instanceof ViewGroup)) {
            return null;
        }
        return FocusFinder.getInstance().findNextFocus((ViewGroup) this.mView, focused, direction);
    }

    @Override // android.view.ViewParent
    public View keyboardNavigationClusterSearch(View currentCluster, int direction) {
        checkThread();
        return FocusFinder.getInstance().findNextKeyboardNavigationCluster(this.mView, currentCluster, direction);
    }

    public void debug() {
        this.mView.debug();
    }

    public void dump(String prefix, FileDescriptor fd, PrintWriter writer, String[] args) {
        String innerPrefix = prefix + "  ";
        writer.print(prefix);
        writer.println("ViewRoot:");
        writer.print(innerPrefix);
        writer.print("mAdded=");
        writer.print(this.mAdded);
        writer.print(" mRemoved=");
        writer.println(this.mRemoved);
        writer.print(innerPrefix);
        writer.print("mConsumeBatchedInputScheduled=");
        writer.println(this.mConsumeBatchedInputScheduled);
        writer.print(innerPrefix);
        writer.print("mConsumeBatchedInputImmediatelyScheduled=");
        writer.println(this.mConsumeBatchedInputImmediatelyScheduled);
        writer.print(innerPrefix);
        writer.print("mPendingInputEventCount=");
        writer.println(this.mPendingInputEventCount);
        writer.print(innerPrefix);
        writer.print("mProcessInputEventsScheduled=");
        writer.println(this.mProcessInputEventsScheduled);
        writer.print(innerPrefix);
        writer.print("mTraversalScheduled=");
        writer.print(this.mTraversalScheduled);
        writer.print(innerPrefix);
        writer.print("mIsAmbientMode=");
        writer.print(this.mIsAmbientMode);
        if (this.mTraversalScheduled) {
            writer.print(" (barrier=");
            writer.print(this.mTraversalBarrier);
            writer.println(")");
        } else {
            writer.println();
        }
        this.mFirstInputStage.dump(innerPrefix, writer);
        this.mChoreographer.dump(prefix, writer);
        this.mInsetsController.dump(prefix, writer);
        writer.print(prefix);
        writer.println("View Hierarchy:");
        dumpViewHierarchy(innerPrefix, writer, this.mView);
    }

    private void dumpViewHierarchy(String prefix, PrintWriter writer, View view) {
        ViewGroup grp;
        int N;
        writer.print(prefix);
        if (view == null) {
            writer.println("null");
            return;
        }
        writer.println(view.toString());
        if ((view instanceof ViewGroup) && (N = (grp = (ViewGroup) view).getChildCount()) > 0) {
            String prefix2 = prefix + "  ";
            for (int i = 0; i < N; i++) {
                dumpViewHierarchy(prefix2, writer, grp.getChildAt(i));
            }
        }
    }

    public void dumpGfxInfo(int[] info) {
        info[1] = 0;
        info[0] = 0;
        View view = this.mView;
        if (view != null) {
            getGfxInfo(view, info);
        }
    }

    private static void getGfxInfo(View view, int[] info) {
        RenderNode renderNode = view.mRenderNode;
        info[0] = info[0] + 1;
        if (renderNode != null) {
            info[1] = info[1] + ((int) renderNode.computeApproximateMemoryUsage());
        }
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            int count = group.getChildCount();
            for (int i = 0; i < count; i++) {
                getGfxInfo(group.getChildAt(i), info);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean die(boolean immediate) {
        if (ViewDebugManager.DEBUG_LIFECYCLE) {
            String str = this.mTag;
            Log.v(str, "die: immediate = " + immediate + ", mIsInTraversal = " + this.mIsInTraversal + ",mIsDrawing = " + this.mIsDrawing + ",this = " + this, new Throwable());
        }
        if (!immediate || this.mIsInTraversal) {
            if (!this.mIsDrawing) {
                destroyHardwareRenderer();
            } else {
                String str2 = this.mTag;
                Log.e(str2, "Attempting to destroy the window while drawing!\n  window=" + this + ", title=" + ((Object) this.mWindowAttributes.getTitle()));
            }
            this.mHandler.sendEmptyMessage(3);
            return true;
        }
        doDie();
        return false;
    }

    /* access modifiers changed from: package-private */
    public void doDie() {
        checkThread();
        if (LOCAL_LOGV) {
            Log.v(this.mTag, "DIE in " + this + " of " + this.mSurface);
        }
        if (ViewDebugManager.DEBUG_LIFECYCLE) {
            Log.v(this.mTag, "DIE in " + this + " of " + this.mSurface + ", mAdded = " + this.mAdded + ", mFirst = " + this.mFirst);
        }
        synchronized (this.mGraphicLock) {
            synchronized (this) {
                if (!this.mRemoved) {
                    boolean viewVisibilityChanged = true;
                    this.mRemoved = true;
                    if (this.mAdded) {
                        dispatchDetachedFromWindow();
                    }
                    if (this.mAdded && !this.mFirst) {
                        destroyHardwareRenderer();
                        if (this.mView != null) {
                            int viewVisibility = this.mView.getVisibility();
                            if (this.mViewVisibility == viewVisibility) {
                                viewVisibilityChanged = false;
                            }
                            if (this.mWindowAttributesChanged || viewVisibilityChanged) {
                                try {
                                    if ((relayoutWindow(this.mWindowAttributes, viewVisibility, false) & 2) != 0) {
                                        this.mWindowSession.finishDrawing(this.mWindow);
                                    }
                                } catch (RemoteException e) {
                                    Log.e(this.mTag, "RemoteException when finish draw window " + this.mWindow + " in " + this, e);
                                }
                            }
                            destroySurface();
                        }
                    }
                    this.mAdded = false;
                    WindowManagerGlobal.getInstance().doRemoveView(this);
                }
            }
        }
    }

    public void requestUpdateConfiguration(Configuration config) {
        this.mHandler.sendMessage(this.mHandler.obtainMessage(18, config));
    }

    public void loadSystemProperties() {
        this.mHandler.post(new Runnable() {
            /* class android.view.ViewRootImpl.AnonymousClass4 */

            public void run() {
                ViewRootImpl.this.mProfileRendering = SystemProperties.getBoolean(ViewRootImpl.PROPERTY_PROFILE_RENDERING, false);
                ViewRootImpl viewRootImpl = ViewRootImpl.this;
                viewRootImpl.profileRendering(viewRootImpl.mAttachInfo.mHasWindowFocus);
                if (ViewRootImpl.this.mAttachInfo.mThreadedRenderer != null && ViewRootImpl.this.mAttachInfo.mThreadedRenderer.loadSystemProperties()) {
                    ViewRootImpl.this.invalidate();
                }
                boolean layout = DisplayProperties.debug_layout().orElse(false).booleanValue();
                if (layout != ViewRootImpl.this.mAttachInfo.mDebugLayout) {
                    ViewRootImpl.this.mAttachInfo.mDebugLayout = layout;
                    if (!ViewRootImpl.this.mHandler.hasMessages(22)) {
                        ViewRootImpl.this.mHandler.sendEmptyMessageDelayed(22, 200);
                    }
                }
            }
        });
    }

    private void destroyHardwareRenderer() {
        synchronized (this.mDestroyLock) {
            ThreadedRenderer hardwareRenderer = this.mAttachInfo.mThreadedRenderer;
            if (hardwareRenderer != null) {
                if (this.mView != null) {
                    hardwareRenderer.destroyHardwareResources(this.mView);
                }
                hardwareRenderer.destroy();
                hardwareRenderer.setRequested(false);
                this.mAttachInfo.mThreadedRenderer = null;
                this.mAttachInfo.mHardwareAccelerated = false;
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    @UnsupportedAppUsage
    private void dispatchResized(Rect frame, Rect overscanInsets, Rect contentInsets, Rect visibleInsets, Rect stableInsets, Rect outsets, boolean reportDraw, MergedConfiguration mergedConfiguration, Rect backDropFrame, boolean forceLayout, boolean alwaysConsumeSystemBars, int displayId, DisplayCutout.ParcelableWrapper displayCutout) {
        if (DEBUG_LAYOUT) {
            Log.v(this.mTag, "Resizing " + this + ": frame=" + frame.toShortString() + " contentInsets=" + contentInsets.toShortString() + " visibleInsets=" + visibleInsets.toShortString() + " reportDraw=" + reportDraw + " backDropFrame=" + backDropFrame);
        }
        boolean sameProcessCall = true;
        if (this.mDragResizing && this.mUseMTRenderer) {
            boolean fullscreen = frame.equals(backDropFrame);
            synchronized (this.mWindowCallbacks) {
                for (int i = this.mWindowCallbacks.size() - 1; i >= 0; i--) {
                    this.mWindowCallbacks.get(i).onWindowSizeIsChanging(backDropFrame, fullscreen, visibleInsets, stableInsets);
                }
            }
        }
        Message msg = this.mHandler.obtainMessage(reportDraw ? 5 : 4);
        CompatibilityInfo.Translator translator = this.mTranslator;
        if (translator != null) {
            translator.translateRectInScreenToAppWindow(frame);
            this.mTranslator.translateRectInScreenToAppWindow(overscanInsets);
            this.mTranslator.translateRectInScreenToAppWindow(contentInsets);
            this.mTranslator.translateRectInScreenToAppWindow(visibleInsets);
        }
        SomeArgs args = SomeArgs.obtain();
        if (Binder.getCallingPid() != Process.myPid()) {
            sameProcessCall = false;
        }
        args.arg1 = sameProcessCall ? new Rect(frame) : frame;
        args.arg2 = sameProcessCall ? new Rect(contentInsets) : contentInsets;
        args.arg3 = sameProcessCall ? new Rect(visibleInsets) : visibleInsets;
        args.arg4 = (!sameProcessCall || mergedConfiguration == null) ? mergedConfiguration : new MergedConfiguration(mergedConfiguration);
        args.arg5 = sameProcessCall ? new Rect(overscanInsets) : overscanInsets;
        args.arg6 = sameProcessCall ? new Rect(stableInsets) : stableInsets;
        args.arg7 = sameProcessCall ? new Rect(outsets) : outsets;
        args.arg8 = sameProcessCall ? new Rect(backDropFrame) : backDropFrame;
        args.arg9 = displayCutout.get();
        args.argi1 = forceLayout ? 1 : 0;
        args.argi2 = alwaysConsumeSystemBars ? 1 : 0;
        args.argi3 = displayId;
        msg.obj = args;
        this.mHandler.sendMessage(msg);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void dispatchInsetsChanged(InsetsState insetsState) {
        this.mHandler.obtainMessage(30, insetsState).sendToTarget();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void dispatchInsetsControlChanged(InsetsState insetsState, InsetsSourceControl[] activeControls) {
        SomeArgs args = SomeArgs.obtain();
        args.arg1 = insetsState;
        args.arg2 = activeControls;
        this.mHandler.obtainMessage(31, args).sendToTarget();
    }

    public void dispatchMoved(int newX, int newY) {
        if (DEBUG_LAYOUT) {
            String str = this.mTag;
            Log.v(str, "Window moved " + this + ": newX=" + newX + " newY=" + newY);
        }
        if (this.mTranslator != null) {
            PointF point = new PointF((float) newX, (float) newY);
            this.mTranslator.translatePointInScreenToAppWindow(point);
            newX = (int) (((double) point.x) + 0.5d);
            newY = (int) (((double) point.y) + 0.5d);
        }
        this.mHandler.sendMessage(this.mHandler.obtainMessage(23, newX, newY));
    }

    /* access modifiers changed from: private */
    public static final class QueuedInputEvent {
        public static final int FLAG_DEFERRED = 2;
        public static final int FLAG_DELIVER_POST_IME = 1;
        public static final int FLAG_FINISHED = 4;
        public static final int FLAG_FINISHED_HANDLED = 8;
        public static final int FLAG_MODIFIED_FOR_COMPATIBILITY = 64;
        public static final int FLAG_RESYNTHESIZED = 16;
        public static final int FLAG_UNHANDLED = 32;
        public InputEvent mEvent;
        public int mFlags;
        public QueuedInputEvent mNext;
        public InputEventReceiver mReceiver;

        private QueuedInputEvent() {
        }

        public boolean shouldSkipIme() {
            if ((this.mFlags & 1) != 0) {
                return true;
            }
            InputEvent inputEvent = this.mEvent;
            if (!(inputEvent instanceof MotionEvent) || (!inputEvent.isFromSource(2) && !this.mEvent.isFromSource(4194304))) {
                return false;
            }
            return true;
        }

        public boolean shouldSendToSynthesizer() {
            if ((this.mFlags & 32) != 0) {
                return true;
            }
            return false;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder("QueuedInputEvent{flags=");
            if (!flagToString("UNHANDLED", 32, flagToString("RESYNTHESIZED", 16, flagToString("FINISHED_HANDLED", 8, flagToString("FINISHED", 4, flagToString("DEFERRED", 2, flagToString("DELIVER_POST_IME", 1, false, sb), sb), sb), sb), sb), sb)) {
                sb.append(WifiEnterpriseConfig.ENGINE_DISABLE);
            }
            StringBuilder sb2 = new StringBuilder();
            sb2.append(", hasNextQueuedEvent=");
            String str = "true";
            sb2.append(this.mEvent != null ? str : "false");
            sb.append(sb2.toString());
            StringBuilder sb3 = new StringBuilder();
            sb3.append(", hasInputEventReceiver=");
            if (this.mReceiver == null) {
                str = "false";
            }
            sb3.append(str);
            sb.append(sb3.toString());
            sb.append(", mEvent=" + this.mEvent + "}");
            return sb.toString();
        }

        private boolean flagToString(String name, int flag, boolean hasPrevious, StringBuilder sb) {
            if ((this.mFlags & flag) == 0) {
                return hasPrevious;
            }
            if (hasPrevious) {
                sb.append("|");
            }
            sb.append(name);
            return true;
        }
    }

    private QueuedInputEvent obtainQueuedInputEvent(InputEvent event, InputEventReceiver receiver, int flags) {
        QueuedInputEvent q = this.mQueuedInputEventPool;
        if (q != null) {
            this.mQueuedInputEventPoolSize--;
            this.mQueuedInputEventPool = q.mNext;
            q.mNext = null;
        } else {
            q = new QueuedInputEvent();
        }
        q.mEvent = event;
        q.mReceiver = receiver;
        q.mFlags = flags;
        return q;
    }

    private void recycleQueuedInputEvent(QueuedInputEvent q) {
        q.mEvent = null;
        q.mReceiver = null;
        int i = this.mQueuedInputEventPoolSize;
        if (i < 10) {
            this.mQueuedInputEventPoolSize = i + 1;
            q.mNext = this.mQueuedInputEventPool;
            this.mQueuedInputEventPool = q;
        }
    }

    /* access modifiers changed from: package-private */
    @UnsupportedAppUsage
    public void enqueueInputEvent(InputEvent event) {
        enqueueInputEvent(event, null, 0, false);
    }

    /* access modifiers changed from: package-private */
    @UnsupportedAppUsage
    public void enqueueInputEvent(InputEvent event, InputEventReceiver receiver, int flags, boolean processImmediately) {
        QueuedInputEvent q = obtainQueuedInputEvent(event, receiver, flags);
        QueuedInputEvent last = this.mPendingInputEventTail;
        if (last == null) {
            this.mPendingInputEventHead = q;
            this.mPendingInputEventTail = q;
        } else {
            last.mNext = q;
            this.mPendingInputEventTail = q;
        }
        this.mPendingInputEventCount++;
        Trace.traceCounter(4, this.mPendingInputEventQueueLengthCounterName, this.mPendingInputEventCount);
        if (InputLog.DEBUG) {
            InputLog.d(this.mTag, " enqueueInputEvent," + event + SmsManager.REGEX_PREFIX_DELIMITER + Debug.getCallers(3));
        }
        if (ViewDebugManager.DEBUG_MET_TRACE && (event instanceof MotionEvent)) {
            MotionEvent ev = (MotionEvent) event;
            Trace.traceBegin(4, "MET_enqueueInputEvent_name: " + ((Object) this.mWindowAttributes.getTitle()));
            Trace.traceEnd(4);
            Trace.traceBegin(4, "MET_enqueueInputEvent: " + ev.getEventTime() + SmsManager.REGEX_PREFIX_DELIMITER + ev.getAction() + SmsManager.REGEX_PREFIX_DELIMITER + ev.getX(0) + SmsManager.REGEX_PREFIX_DELIMITER + ev.getY(0));
            Trace.traceEnd(4);
        }
        if (ViewDebugManager.DEBUG_INPUT || ViewDebugManager.DEBUG_KEY || ViewDebugManager.DEBUG_MOTION || ViewDebugManager.DEBUG_ENG) {
            Log.v(this.mTag, "enqueueInputEvent: event = " + event + ",processImmediately = " + processImmediately + ",mProcessInputEventsScheduled = " + this.mProcessInputEventsScheduled + ", this = " + this);
        }
        if (ViewDebugManager.debugHighFrameRateTouchLowLatency && (event instanceof MotionEvent) && ((MotionEvent) event).getAction() == 0) {
            if (ViewDebugManager.DEBUG_INPUT) {
                Log.d(this.mTag, "down is coming...");
                Trace.traceBegin(8, "down is coming");
            }
            this.mChoreographer.postTouchViewMessage(true);
            if (ViewDebugManager.DEBUG_INPUT) {
                Trace.traceEnd(8);
            }
        }
        if (processImmediately) {
            doProcessInputEvents();
        } else {
            scheduleProcessInputEvents();
        }
    }

    public void postTouchPointerViewMessage(boolean postMessage) {
        if (ViewDebugManager.debugHighFrameRateTouchLowLatency) {
            this.mChoreographer.postTouchPointerViewMessage(postMessage);
        }
    }

    private void scheduleProcessInputEvents() {
        if (!this.mProcessInputEventsScheduled) {
            this.mProcessInputEventsScheduled = true;
            Message msg = this.mHandler.obtainMessage(19);
            msg.setAsynchronous(true);
            this.mHandler.sendMessage(msg);
        }
    }

    /* access modifiers changed from: package-private */
    public void doProcessInputEvents() {
        while (this.mPendingInputEventHead != null) {
            QueuedInputEvent q = this.mPendingInputEventHead;
            this.mPendingInputEventHead = q.mNext;
            if (this.mPendingInputEventHead == null) {
                this.mPendingInputEventTail = null;
            }
            q.mNext = null;
            this.mPendingInputEventCount--;
            Trace.traceCounter(4, this.mPendingInputEventQueueLengthCounterName, this.mPendingInputEventCount);
            long eventTime = q.mEvent.getEventTimeNano();
            long oldestEventTime = eventTime;
            if (q.mEvent instanceof MotionEvent) {
                MotionEvent me = (MotionEvent) q.mEvent;
                if (this.mIsSurfaceViewCreated && this.mSurfaceExt.isResolutionTuningPackage()) {
                    Matrix scale = new Matrix();
                    scale.setScale(1.0f / this.mSurfaceExt.getXScale(), 1.0f / this.mSurfaceExt.getYScale());
                    me.transform(scale);
                }
                if (me.getHistorySize() > 0) {
                    oldestEventTime = me.getHistoricalEventTimeNano(0);
                }
            }
            this.mChoreographer.mFrameInfo.updateInputEventTime(eventTime, oldestEventTime);
            if (ViewDebugManager.DEBUG_INPUT || ViewDebugManager.DEBUG_KEY || ViewDebugManager.DEBUG_MOTION) {
                Log.v(this.mTag, "doProcessInputEvents: mCurrentInputEvent = " + q + ", this = " + this);
            }
            deliverInputEvent(q);
        }
        if (this.mProcessInputEventsScheduled) {
            this.mProcessInputEventsScheduled = false;
            this.mHandler.removeMessages(19);
        }
    }

    private void deliverInputEvent(QueuedInputEvent q) {
        InputStage stage;
        Trace.asyncTraceBegin(8, "deliverInputEvent", q.mEvent.getSequenceNumber());
        InputEventConsistencyVerifier inputEventConsistencyVerifier = this.mInputEventConsistencyVerifier;
        if (inputEventConsistencyVerifier != null) {
            inputEventConsistencyVerifier.onInputEvent(q.mEvent, 0);
        }
        if (q.shouldSendToSynthesizer()) {
            stage = this.mSyntheticInputStage;
        } else {
            stage = q.shouldSkipIme() ? this.mFirstPostImeInputStage : this.mFirstInputStage;
        }
        if (q.mEvent instanceof KeyEvent) {
            this.mUnhandledKeyManager.preDispatch((KeyEvent) q.mEvent);
        }
        if (stage != null) {
            handleWindowFocusChanged();
            stage.deliver(q);
            return;
        }
        finishInputEvent(q);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void finishInputEvent(QueuedInputEvent q) {
        Trace.asyncTraceEnd(8, "deliverInputEvent", q.mEvent.getSequenceNumber());
        boolean modified = true;
        boolean handled = (q.mFlags & 8) != 0;
        ViewDebugManager.getInstance().debugInputEventFinished(this.mTag, handled, q.mEvent, this);
        if (q.mReceiver != null) {
            if ((q.mFlags & 64) == 0) {
                modified = false;
            }
            if (modified) {
                Trace.traceBegin(8, "processInputEventBeforeFinish");
                try {
                    InputEvent processedEvent = this.mInputCompatProcessor.processInputEventBeforeFinish(q.mEvent);
                    if (processedEvent != null) {
                        q.mReceiver.finishInputEvent(processedEvent, handled);
                    }
                } finally {
                    Trace.traceEnd(8);
                }
            } else {
                q.mReceiver.finishInputEvent(q.mEvent, handled);
            }
        } else {
            q.mEvent.recycleIfNeededAfterDispatch();
        }
        recycleQueuedInputEvent(q);
    }

    static boolean isTerminalInputEvent(InputEvent event) {
        if (event instanceof KeyEvent) {
            return ((KeyEvent) event).getAction() == 1;
        }
        int action = ((MotionEvent) event).getAction();
        return action == 1 || action == 3 || action == 10;
    }

    /* access modifiers changed from: package-private */
    public void scheduleConsumeBatchedInput() {
        if (!this.mConsumeBatchedInputScheduled) {
            this.mConsumeBatchedInputScheduled = true;
            this.mChoreographer.postCallback(0, this.mConsumedBatchedInputRunnable, null);
        }
    }

    /* access modifiers changed from: package-private */
    public void unscheduleConsumeBatchedInput() {
        if (this.mConsumeBatchedInputScheduled) {
            this.mConsumeBatchedInputScheduled = false;
            this.mChoreographer.removeCallbacks(0, this.mConsumedBatchedInputRunnable, null);
        }
    }

    /* access modifiers changed from: package-private */
    public void scheduleConsumeBatchedInputImmediately() {
        if (!this.mConsumeBatchedInputImmediatelyScheduled) {
            unscheduleConsumeBatchedInput();
            this.mConsumeBatchedInputImmediatelyScheduled = true;
            this.mHandler.post(this.mConsumeBatchedInputImmediatelyRunnable);
        }
    }

    /* access modifiers changed from: package-private */
    public void doConsumeBatchedInput(long frameTimeNanos) {
        if (this.mConsumeBatchedInputScheduled) {
            this.mConsumeBatchedInputScheduled = false;
            if (this.mInputEventReceiver != null) {
                if (ViewDebugManager.DEBUG_MET_TRACE) {
                    Trace.traceBegin(4, "MET_consumeBatchedInput_name: " + ((Object) this.mWindowAttributes.getTitle()));
                    Trace.traceEnd(4);
                }
                if (this.mInputEventReceiver.consumeBatchedInputEvents(frameTimeNanos) && frameTimeNanos != -1) {
                    scheduleConsumeBatchedInput();
                }
            }
            doProcessInputEvents();
        }
    }

    /* access modifiers changed from: package-private */
    public final class TraversalRunnable implements Runnable {
        TraversalRunnable() {
        }

        public void run() {
            ViewRootImpl.this.doTraversal();
        }
    }

    /* access modifiers changed from: package-private */
    public final class WindowInputEventReceiver extends InputEventReceiver {
        public WindowInputEventReceiver(InputChannel inputChannel, Looper looper) {
            super(inputChannel, looper);
        }

        /* JADX INFO: finally extract failed */
        @Override // android.view.InputEventReceiver
        public void onInputEvent(InputEvent event) {
            ViewDebugManager.getInstance().debugInputEventStart(event);
            Trace.traceBegin(8, "processInputEventForCompatibility");
            try {
                List<InputEvent> processedEvents = ViewRootImpl.this.mInputCompatProcessor.processInputEventForCompatibility(event);
                Trace.traceEnd(8);
                if (processedEvents == null) {
                    ViewRootImpl.this.enqueueInputEvent(event, this, 0, true);
                } else if (processedEvents.isEmpty()) {
                    finishInputEvent(event, true);
                } else {
                    for (int i = 0; i < processedEvents.size(); i++) {
                        ViewRootImpl.this.enqueueInputEvent(processedEvents.get(i), this, 64, true);
                    }
                }
            } catch (Throwable th) {
                Trace.traceEnd(8);
                throw th;
            }
        }

        @Override // android.view.InputEventReceiver
        public void onBatchedInputEventPending() {
            if (ViewRootImpl.this.mUnbufferedInputDispatch) {
                super.onBatchedInputEventPending();
            } else {
                ViewRootImpl.this.scheduleConsumeBatchedInput();
            }
        }

        @Override // android.view.InputEventReceiver
        public void dispose() {
            ViewRootImpl.this.unscheduleConsumeBatchedInput();
            super.dispose();
        }
    }

    /* access modifiers changed from: package-private */
    public final class ConsumeBatchedInputRunnable implements Runnable {
        ConsumeBatchedInputRunnable() {
        }

        public void run() {
            ViewRootImpl viewRootImpl = ViewRootImpl.this;
            viewRootImpl.doConsumeBatchedInput(viewRootImpl.mChoreographer.getFrameTimeNanos());
        }
    }

    /* access modifiers changed from: package-private */
    public final class ConsumeBatchedInputImmediatelyRunnable implements Runnable {
        ConsumeBatchedInputImmediatelyRunnable() {
        }

        public void run() {
            ViewRootImpl.this.doConsumeBatchedInput(-1);
        }
    }

    /* access modifiers changed from: package-private */
    public final class InvalidateOnAnimationRunnable implements Runnable {
        private boolean mPosted;
        private View.AttachInfo.InvalidateInfo[] mTempViewRects;
        private View[] mTempViews;
        private final ArrayList<View.AttachInfo.InvalidateInfo> mViewRects = new ArrayList<>();
        private final ArrayList<View> mViews = new ArrayList<>();

        InvalidateOnAnimationRunnable() {
        }

        public void addView(View view) {
            synchronized (this) {
                this.mViews.add(view);
                postIfNeededLocked();
            }
        }

        public void addViewRect(View.AttachInfo.InvalidateInfo info) {
            synchronized (this) {
                this.mViewRects.add(info);
                postIfNeededLocked();
            }
        }

        public void removeView(View view) {
            synchronized (this) {
                this.mViews.remove(view);
                int i = this.mViewRects.size();
                while (true) {
                    int i2 = i - 1;
                    if (i <= 0) {
                        break;
                    }
                    View.AttachInfo.InvalidateInfo info = this.mViewRects.get(i2);
                    if (info.target == view) {
                        this.mViewRects.remove(i2);
                        info.recycle();
                    }
                    i = i2;
                }
                if (this.mPosted && this.mViews.isEmpty() && this.mViewRects.isEmpty()) {
                    ViewRootImpl.this.mChoreographer.removeCallbacks(1, this, null);
                    this.mPosted = false;
                }
            }
        }

        public void run() {
            int viewCount;
            int viewRectCount;
            synchronized (this) {
                this.mPosted = false;
                viewCount = this.mViews.size();
                if (viewCount != 0) {
                    this.mTempViews = (View[]) this.mViews.toArray(this.mTempViews != null ? this.mTempViews : new View[viewCount]);
                    this.mViews.clear();
                }
                viewRectCount = this.mViewRects.size();
                if (viewRectCount != 0) {
                    this.mTempViewRects = (View.AttachInfo.InvalidateInfo[]) this.mViewRects.toArray(this.mTempViewRects != null ? this.mTempViewRects : new View.AttachInfo.InvalidateInfo[viewRectCount]);
                    this.mViewRects.clear();
                }
            }
            for (int i = 0; i < viewCount; i++) {
                this.mTempViews[i].invalidate();
                this.mTempViews[i] = null;
            }
            for (int i2 = 0; i2 < viewRectCount; i2++) {
                View.AttachInfo.InvalidateInfo info = this.mTempViewRects[i2];
                info.target.invalidate(info.left, info.top, info.right, info.bottom);
                info.recycle();
            }
        }

        private void postIfNeededLocked() {
            if (!this.mPosted) {
                ViewRootImpl.this.mChoreographer.postCallback(1, this, null);
                this.mPosted = true;
            }
        }
    }

    public void dispatchInvalidateDelayed(View view, long delayMilliseconds) {
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(1, view), delayMilliseconds);
    }

    public void dispatchInvalidateRectDelayed(View.AttachInfo.InvalidateInfo info, long delayMilliseconds) {
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(2, info), delayMilliseconds);
    }

    public void dispatchInvalidateOnAnimation(View view) {
        this.mInvalidateOnAnimationRunnable.addView(view);
    }

    public void dispatchInvalidateRectOnAnimation(View.AttachInfo.InvalidateInfo info) {
        this.mInvalidateOnAnimationRunnable.addViewRect(info);
    }

    @UnsupportedAppUsage
    public void cancelInvalidate(View view) {
        this.mHandler.removeMessages(1, view);
        this.mHandler.removeMessages(2, view);
        this.mInvalidateOnAnimationRunnable.removeView(view);
    }

    @UnsupportedAppUsage
    public void dispatchInputEvent(InputEvent event) {
        dispatchInputEvent(event, null);
    }

    @UnsupportedAppUsage
    public void dispatchInputEvent(InputEvent event, InputEventReceiver receiver) {
        SomeArgs args = SomeArgs.obtain();
        args.arg1 = event;
        args.arg2 = receiver;
        Message msg = this.mHandler.obtainMessage(7, args);
        msg.setAsynchronous(true);
        this.mHandler.sendMessage(msg);
    }

    public void synthesizeInputEvent(InputEvent event) {
        Message msg = this.mHandler.obtainMessage(24, event);
        msg.setAsynchronous(true);
        this.mHandler.sendMessage(msg);
    }

    @UnsupportedAppUsage
    public void dispatchKeyFromIme(KeyEvent event) {
        Message msg = this.mHandler.obtainMessage(11, event);
        msg.setAsynchronous(true);
        this.mHandler.sendMessage(msg);
    }

    public void dispatchKeyFromAutofill(KeyEvent event) {
        Message msg = this.mHandler.obtainMessage(12, event);
        msg.setAsynchronous(true);
        this.mHandler.sendMessage(msg);
    }

    @UnsupportedAppUsage
    public void dispatchUnhandledInputEvent(InputEvent event) {
        if (event instanceof MotionEvent) {
            event = MotionEvent.obtain((MotionEvent) event);
        }
        synthesizeInputEvent(event);
    }

    public void dispatchAppVisibility(boolean visible) {
        Message msg = this.mHandler.obtainMessage(8);
        msg.arg1 = visible ? 1 : 0;
        this.mHandler.sendMessage(msg);
    }

    public void dispatchGetNewSurface() {
        this.mHandler.sendMessage(this.mHandler.obtainMessage(9));
    }

    public void windowFocusChanged(boolean hasFocus, boolean inTouchMode) {
        synchronized (this) {
            this.mWindowFocusChanged = true;
            this.mUpcomingWindowFocus = hasFocus;
            this.mUpcomingInTouchMode = inTouchMode;
        }
        Message msg = Message.obtain();
        msg.what = 6;
        this.mHandler.sendMessage(msg);
    }

    public void dispatchWindowShown() {
        this.mHandler.sendEmptyMessage(25);
    }

    public void dispatchCloseSystemDialogs(String reason) {
        Message msg = Message.obtain();
        msg.what = 14;
        msg.obj = reason;
        this.mHandler.sendMessage(msg);
    }

    public void dispatchDragEvent(DragEvent event) {
        int what;
        if (event.getAction() == 2) {
            what = 16;
            this.mHandler.removeMessages(16);
        } else {
            what = 15;
        }
        this.mHandler.sendMessage(this.mHandler.obtainMessage(what, event));
    }

    public void updatePointerIcon(float x, float y) {
        this.mHandler.removeMessages(27);
        this.mHandler.sendMessage(this.mHandler.obtainMessage(27, MotionEvent.obtain(0, SystemClock.uptimeMillis(), 7, x, y, 0)));
    }

    public void dispatchSystemUiVisibilityChanged(int seq, int globalVisibility, int localValue, int localChanges) {
        SystemUiVisibilityInfo args = new SystemUiVisibilityInfo();
        args.seq = seq;
        args.globalVisibility = globalVisibility;
        args.localValue = localValue;
        args.localChanges = localChanges;
        ViewRootHandler viewRootHandler = this.mHandler;
        viewRootHandler.sendMessage(viewRootHandler.obtainMessage(17, args));
    }

    public void dispatchCheckFocus() {
        if (!this.mHandler.hasMessages(13)) {
            this.mHandler.sendEmptyMessage(13);
        }
    }

    public void dispatchRequestKeyboardShortcuts(IResultReceiver receiver, int deviceId) {
        this.mHandler.obtainMessage(26, deviceId, 0, receiver).sendToTarget();
    }

    public void dispatchPointerCaptureChanged(boolean on) {
        this.mHandler.removeMessages(28);
        Message msg = this.mHandler.obtainMessage(28);
        msg.arg1 = on ? 1 : 0;
        this.mHandler.sendMessage(msg);
    }

    private void postSendWindowContentChangedCallback(View source, int changeType) {
        if (this.mSendWindowContentChangedAccessibilityEvent == null) {
            this.mSendWindowContentChangedAccessibilityEvent = new SendWindowContentChangedAccessibilityEvent();
        }
        this.mSendWindowContentChangedAccessibilityEvent.runOrPost(source, changeType);
    }

    private void removeSendWindowContentChangedCallback() {
        SendWindowContentChangedAccessibilityEvent sendWindowContentChangedAccessibilityEvent = this.mSendWindowContentChangedAccessibilityEvent;
        if (sendWindowContentChangedAccessibilityEvent != null) {
            this.mHandler.removeCallbacks(sendWindowContentChangedAccessibilityEvent);
        }
    }

    @Override // android.view.ViewParent
    public boolean showContextMenuForChild(View originalView) {
        return false;
    }

    @Override // android.view.ViewParent
    public boolean showContextMenuForChild(View originalView, float x, float y) {
        return false;
    }

    @Override // android.view.ViewParent
    public ActionMode startActionModeForChild(View originalView, ActionMode.Callback callback) {
        return null;
    }

    @Override // android.view.ViewParent
    public ActionMode startActionModeForChild(View originalView, ActionMode.Callback callback, int type) {
        return null;
    }

    @Override // android.view.ViewParent
    public void createContextMenu(ContextMenu menu) {
    }

    @Override // android.view.ViewParent
    public void childDrawableStateChanged(View child) {
    }

    @Override // android.view.ViewParent
    public boolean requestSendAccessibilityEvent(View child, AccessibilityEvent event) {
        AccessibilityNodeProvider provider;
        SendWindowContentChangedAccessibilityEvent sendWindowContentChangedAccessibilityEvent;
        if (this.mView == null || this.mStopped || this.mPausedForTransition) {
            return false;
        }
        if (!(event.getEventType() == 2048 || (sendWindowContentChangedAccessibilityEvent = this.mSendWindowContentChangedAccessibilityEvent) == null || sendWindowContentChangedAccessibilityEvent.mSource == null)) {
            this.mSendWindowContentChangedAccessibilityEvent.removeCallbacksAndRun();
        }
        int eventType = event.getEventType();
        View source = getSourceForAccessibilityEvent(event);
        if (eventType == 2048) {
            handleWindowContentChangedEvent(event);
        } else if (eventType != 32768) {
            if (!(eventType != 65536 || source == null || source.getAccessibilityNodeProvider() == null)) {
                setAccessibilityFocus(null, null);
            }
        } else if (!(source == null || (provider = source.getAccessibilityNodeProvider()) == null)) {
            setAccessibilityFocus(source, provider.createAccessibilityNodeInfo(AccessibilityNodeInfo.getVirtualDescendantId(event.getSourceNodeId())));
        }
        this.mAccessibilityManager.sendAccessibilityEvent(event);
        return true;
    }

    private View getSourceForAccessibilityEvent(AccessibilityEvent event) {
        return AccessibilityNodeIdManager.getInstance().findView(AccessibilityNodeInfo.getAccessibilityViewId(event.getSourceNodeId()));
    }

    private void handleWindowContentChangedEvent(AccessibilityEvent event) {
        View focusedHost = this.mAccessibilityFocusedHost;
        if (focusedHost != null && this.mAccessibilityFocusedVirtualView != null) {
            AccessibilityNodeProvider provider = focusedHost.getAccessibilityNodeProvider();
            if (provider == null) {
                this.mAccessibilityFocusedHost = null;
                this.mAccessibilityFocusedVirtualView = null;
                focusedHost.clearAccessibilityFocusNoCallbacks(0);
                return;
            }
            int changes = event.getContentChangeTypes();
            if ((changes & 1) != 0 || changes == 0) {
                int changedViewId = AccessibilityNodeInfo.getAccessibilityViewId(event.getSourceNodeId());
                boolean hostInSubtree = false;
                View root = this.mAccessibilityFocusedHost;
                while (root != null && !hostInSubtree) {
                    if (changedViewId == root.getAccessibilityViewId()) {
                        hostInSubtree = true;
                    } else {
                        ViewParent parent = root.getParent();
                        if (parent instanceof View) {
                            root = (View) parent;
                        } else {
                            root = null;
                        }
                    }
                }
                if (hostInSubtree) {
                    int focusedChildId = AccessibilityNodeInfo.getVirtualDescendantId(this.mAccessibilityFocusedVirtualView.getSourceNodeId());
                    Rect oldBounds = this.mTempRect;
                    this.mAccessibilityFocusedVirtualView.getBoundsInScreen(oldBounds);
                    this.mAccessibilityFocusedVirtualView = provider.createAccessibilityNodeInfo(focusedChildId);
                    AccessibilityNodeInfo accessibilityNodeInfo = this.mAccessibilityFocusedVirtualView;
                    if (accessibilityNodeInfo == null) {
                        this.mAccessibilityFocusedHost = null;
                        focusedHost.clearAccessibilityFocusNoCallbacks(0);
                        provider.performAction(focusedChildId, AccessibilityNodeInfo.AccessibilityAction.ACTION_CLEAR_ACCESSIBILITY_FOCUS.getId(), null);
                        invalidateRectOnScreen(oldBounds);
                        return;
                    }
                    Rect newBounds = accessibilityNodeInfo.getBoundsInScreen();
                    if (!oldBounds.equals(newBounds)) {
                        oldBounds.union(newBounds);
                        invalidateRectOnScreen(oldBounds);
                    }
                }
            }
        }
    }

    @Override // android.view.ViewParent
    public void notifySubtreeAccessibilityStateChanged(View child, View source, int changeType) {
        postSendWindowContentChangedCallback((View) Preconditions.checkNotNull(source), changeType);
    }

    @Override // android.view.ViewParent
    public boolean canResolveLayoutDirection() {
        return true;
    }

    @Override // android.view.ViewParent
    public boolean isLayoutDirectionResolved() {
        return true;
    }

    @Override // android.view.ViewParent
    public int getLayoutDirection() {
        return 0;
    }

    @Override // android.view.ViewParent
    public boolean canResolveTextDirection() {
        return true;
    }

    @Override // android.view.ViewParent
    public boolean isTextDirectionResolved() {
        return true;
    }

    @Override // android.view.ViewParent
    public int getTextDirection() {
        return 1;
    }

    @Override // android.view.ViewParent
    public boolean canResolveTextAlignment() {
        return true;
    }

    @Override // android.view.ViewParent
    public boolean isTextAlignmentResolved() {
        return true;
    }

    @Override // android.view.ViewParent
    public int getTextAlignment() {
        return 1;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private View getCommonPredecessor(View first, View second) {
        if (this.mTempHashSet == null) {
            this.mTempHashSet = new HashSet<>();
        }
        HashSet<View> seen = this.mTempHashSet;
        seen.clear();
        View firstCurrent = first;
        while (firstCurrent != null) {
            seen.add(firstCurrent);
            ViewParent firstCurrentParent = firstCurrent.mParent;
            if (firstCurrentParent instanceof View) {
                firstCurrent = (View) firstCurrentParent;
            } else {
                firstCurrent = null;
            }
        }
        View secondCurrent = second;
        while (secondCurrent != null) {
            if (seen.contains(secondCurrent)) {
                seen.clear();
                return secondCurrent;
            }
            ViewParent secondCurrentParent = secondCurrent.mParent;
            if (secondCurrentParent instanceof View) {
                secondCurrent = (View) secondCurrentParent;
            } else {
                secondCurrent = null;
            }
        }
        seen.clear();
        return null;
    }

    /* access modifiers changed from: package-private */
    public void checkThread() {
        if (this.mThread != Thread.currentThread()) {
            throw new CalledFromWrongThreadException("Only the original thread that created a view hierarchy can touch its views.");
        }
    }

    @Override // android.view.ViewParent
    public void requestDisallowInterceptTouchEvent(boolean disallowIntercept) {
    }

    @Override // android.view.ViewParent
    public boolean requestChildRectangleOnScreen(View child, Rect rectangle, boolean immediate) {
        if (rectangle == null) {
            return scrollToRectOrFocus(null, immediate);
        }
        rectangle.offset(child.getLeft() - child.getScrollX(), child.getTop() - child.getScrollY());
        boolean scrolled = scrollToRectOrFocus(rectangle, immediate);
        this.mTempRect.set(rectangle);
        this.mTempRect.offset(0, -this.mCurScrollY);
        this.mTempRect.offset(this.mAttachInfo.mWindowLeft, this.mAttachInfo.mWindowTop);
        try {
            this.mWindowSession.onRectangleOnScreenRequested(this.mWindow, this.mTempRect);
        } catch (RemoteException e) {
        }
        return scrolled;
    }

    @Override // android.view.ViewParent
    public void childHasTransientStateChanged(View child, boolean hasTransientState) {
    }

    @Override // android.view.ViewParent
    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
        return false;
    }

    @Override // android.view.ViewParent
    public void onStopNestedScroll(View target) {
    }

    @Override // android.view.ViewParent
    public void onNestedScrollAccepted(View child, View target, int nestedScrollAxes) {
    }

    @Override // android.view.ViewParent
    public void onNestedScroll(View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
    }

    @Override // android.view.ViewParent
    public void onNestedPreScroll(View target, int dx, int dy, int[] consumed) {
    }

    @Override // android.view.ViewParent
    public boolean onNestedFling(View target, float velocityX, float velocityY, boolean consumed) {
        return false;
    }

    @Override // android.view.ViewParent
    public boolean onNestedPreFling(View target, float velocityX, float velocityY) {
        return false;
    }

    @Override // android.view.ViewParent
    public boolean onNestedPrePerformAccessibilityAction(View target, int action, Bundle args) {
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void reportNextDraw() {
        if (!this.mReportNextDraw) {
            drawPending();
        }
        this.mReportNextDraw = true;
    }

    public void setReportNextDraw() {
        reportNextDraw();
        invalidate();
    }

    /* access modifiers changed from: package-private */
    public void changeCanvasOpacity(boolean opaque) {
        String str = this.mTag;
        Log.d(str, "changeCanvasOpacity: opaque=" + opaque);
        boolean opaque2 = opaque & ((this.mView.mPrivateFlags & 512) == 0);
        if (this.mAttachInfo.mThreadedRenderer != null) {
            this.mAttachInfo.mThreadedRenderer.setOpaque(opaque2);
        }
    }

    public boolean dispatchUnhandledKeyEvent(KeyEvent event) {
        return this.mUnhandledKeyManager.dispatch(this.mView, event);
    }

    /* access modifiers changed from: package-private */
    public class TakenSurfaceHolder extends BaseSurfaceHolder {
        TakenSurfaceHolder() {
        }

        @Override // com.android.internal.view.BaseSurfaceHolder
        public boolean onAllowLockCanvas() {
            return ViewRootImpl.this.mDrawingAllowed;
        }

        @Override // com.android.internal.view.BaseSurfaceHolder
        public void onRelayoutContainer() {
        }

        @Override // android.view.SurfaceHolder, com.android.internal.view.BaseSurfaceHolder
        public void setFormat(int format) {
            ((RootViewSurfaceTaker) ViewRootImpl.this.mView).setSurfaceFormat(format);
        }

        @Override // android.view.SurfaceHolder, com.android.internal.view.BaseSurfaceHolder
        public void setType(int type) {
            ((RootViewSurfaceTaker) ViewRootImpl.this.mView).setSurfaceType(type);
        }

        @Override // com.android.internal.view.BaseSurfaceHolder
        public void onUpdateSurface() {
            throw new IllegalStateException("Shouldn't be here");
        }

        @Override // android.view.SurfaceHolder
        public boolean isCreating() {
            return ViewRootImpl.this.mIsCreating;
        }

        @Override // android.view.SurfaceHolder, com.android.internal.view.BaseSurfaceHolder
        public void setFixedSize(int width, int height) {
            throw new UnsupportedOperationException("Currently only support sizing from layout");
        }

        @Override // android.view.SurfaceHolder
        public void setKeepScreenOn(boolean screenOn) {
            ((RootViewSurfaceTaker) ViewRootImpl.this.mView).setSurfaceKeepScreenOn(screenOn);
        }
    }

    /* access modifiers changed from: package-private */
    public static class W extends IWindow.Stub {
        final WeakReference<ViewRootImpl> mViewAncestor;
        private final IWindowSession mWindowSession;

        W(ViewRootImpl viewAncestor) {
            this.mViewAncestor = new WeakReference<>(viewAncestor);
            this.mWindowSession = viewAncestor.mWindowSession;
        }

        @Override // android.view.IWindow
        public void resized(Rect frame, Rect overscanInsets, Rect contentInsets, Rect visibleInsets, Rect stableInsets, Rect outsets, boolean reportDraw, MergedConfiguration mergedConfiguration, Rect backDropFrame, boolean forceLayout, boolean alwaysConsumeSystemBars, int displayId, DisplayCutout.ParcelableWrapper displayCutout) {
            ViewRootImpl viewAncestor = this.mViewAncestor.get();
            if (viewAncestor != null) {
                viewAncestor.dispatchResized(frame, overscanInsets, contentInsets, visibleInsets, stableInsets, outsets, reportDraw, mergedConfiguration, backDropFrame, forceLayout, alwaysConsumeSystemBars, displayId, displayCutout);
            }
        }

        @Override // android.view.IWindow
        public void insetsChanged(InsetsState insetsState) {
            ViewRootImpl viewAncestor = this.mViewAncestor.get();
            if (viewAncestor != null) {
                viewAncestor.dispatchInsetsChanged(insetsState);
            }
        }

        @Override // android.view.IWindow
        public void insetsControlChanged(InsetsState insetsState, InsetsSourceControl[] activeControls) {
            ViewRootImpl viewAncestor = this.mViewAncestor.get();
            if (viewAncestor != null) {
                viewAncestor.dispatchInsetsControlChanged(insetsState, activeControls);
            }
        }

        @Override // android.view.IWindow
        public void moved(int newX, int newY) {
            ViewRootImpl viewAncestor = this.mViewAncestor.get();
            if (viewAncestor != null) {
                viewAncestor.dispatchMoved(newX, newY);
            }
        }

        @Override // android.view.IWindow
        public void dispatchAppVisibility(boolean visible) {
            ViewRootImpl viewAncestor = this.mViewAncestor.get();
            if (ViewDebugManager.DEBUG_LIFECYCLE) {
                Log.v(ViewRootImpl.TAG, "dispatchAppVisibility: visible = " + visible + ", viewAncestor = " + viewAncestor);
            }
            if (viewAncestor != null) {
                viewAncestor.dispatchAppVisibility(visible);
            }
        }

        @Override // android.view.IWindow
        public void dispatchGetNewSurface() {
            ViewRootImpl viewAncestor = this.mViewAncestor.get();
            if (viewAncestor != null) {
                viewAncestor.dispatchGetNewSurface();
            }
        }

        @Override // android.view.IWindow
        public void windowFocusChanged(boolean hasFocus, boolean inTouchMode) {
            ViewRootImpl viewAncestor = this.mViewAncestor.get();
            if (ViewRootImpl.DEBUG_IMF) {
                Log.v(ViewRootImpl.TAG, "W windowFocusChanged: hasFocus = " + hasFocus + ", inTouchMode = " + inTouchMode + ", viewAncestor = " + viewAncestor + ", this = " + this);
            }
            if (viewAncestor != null) {
                viewAncestor.windowFocusChanged(hasFocus, inTouchMode);
            }
        }

        private static int checkCallingPermission(String permission) {
            try {
                return ActivityManager.getService().checkPermission(permission, Binder.getCallingPid(), Binder.getCallingUid());
            } catch (RemoteException e) {
                return -1;
            }
        }

        @Override // android.view.IWindow
        public void executeCommand(String command, String parameters, ParcelFileDescriptor out) {
            View view;
            ViewRootImpl viewAncestor = this.mViewAncestor.get();
            if (viewAncestor != null && (view = viewAncestor.mView) != null) {
                if (checkCallingPermission(Manifest.permission.DUMP) == 0) {
                    OutputStream clientStream = null;
                    try {
                        clientStream = new ParcelFileDescriptor.AutoCloseOutputStream(out);
                        ViewDebug.dispatchCommand(view, command, parameters, clientStream);
                        try {
                            clientStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } catch (IOException e2) {
                        e2.printStackTrace();
                        if (clientStream != null) {
                            clientStream.close();
                        }
                    } catch (Throwable th) {
                        if (clientStream != null) {
                            try {
                                clientStream.close();
                            } catch (IOException e3) {
                                e3.printStackTrace();
                            }
                        }
                        throw th;
                    }
                } else {
                    throw new SecurityException("Insufficient permissions to invoke executeCommand() from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid());
                }
            }
        }

        @Override // android.view.IWindow
        public void closeSystemDialogs(String reason) {
            ViewRootImpl viewAncestor = this.mViewAncestor.get();
            if (ViewRootImpl.LOCAL_LOGV) {
                Log.v(ViewRootImpl.TAG, "Close system dialogs in " + viewAncestor + " for " + reason);
            }
            if (viewAncestor != null) {
                viewAncestor.dispatchCloseSystemDialogs(reason);
            }
        }

        @Override // android.view.IWindow
        public void dispatchWallpaperOffsets(float x, float y, float xStep, float yStep, boolean sync) {
            if (sync) {
                try {
                    this.mWindowSession.wallpaperOffsetsComplete(asBinder());
                } catch (RemoteException e) {
                    Log.e(ViewRootImpl.TAG, "RemoteException happens when dispatchWallpaperOffsets.", e);
                }
            }
        }

        @Override // android.view.IWindow
        public void dispatchWallpaperCommand(String action, int x, int y, int z, Bundle extras, boolean sync) {
            if (sync) {
                try {
                    this.mWindowSession.wallpaperCommandComplete(asBinder(), null);
                } catch (RemoteException e) {
                    Log.e(ViewRootImpl.TAG, "RemoteException happens when dispatchWallpaperCommand.", e);
                }
            }
        }

        @Override // android.view.IWindow
        public void dispatchDragEvent(DragEvent event) {
            ViewRootImpl viewAncestor = this.mViewAncestor.get();
            if (ViewRootImpl.LOCAL_LOGV || ViewDebugManager.DEBUG_INPUT) {
                Log.v(ViewRootImpl.TAG, "Dispatch drag event " + event + " in " + viewAncestor);
            }
            if (viewAncestor != null) {
                viewAncestor.dispatchDragEvent(event);
            }
        }

        @Override // android.view.IWindow
        public void updatePointerIcon(float x, float y) {
            ViewRootImpl viewAncestor = this.mViewAncestor.get();
            if (viewAncestor != null) {
                viewAncestor.updatePointerIcon(x, y);
            }
        }

        @Override // android.view.IWindow
        public void dispatchSystemUiVisibilityChanged(int seq, int globalVisibility, int localValue, int localChanges) {
            ViewRootImpl viewAncestor = this.mViewAncestor.get();
            if (ViewRootImpl.LOCAL_LOGV) {
                Log.v(ViewRootImpl.TAG, "dispatchSystemUiVisibilityChanged: seq = " + seq + ", globalVisibility = " + globalVisibility + ", localValue = " + localValue + ", localChanges = " + localChanges + ", viewAncestor" + viewAncestor);
            }
            if (viewAncestor != null) {
                viewAncestor.dispatchSystemUiVisibilityChanged(seq, globalVisibility, localValue, localChanges);
            }
        }

        @Override // android.view.IWindow
        public void dispatchWindowShown() {
            ViewRootImpl viewAncestor = this.mViewAncestor.get();
            if (ViewRootImpl.DEBUG_DRAW) {
                Log.v(ViewRootImpl.TAG, "doneAnimating: viewAncestor" + viewAncestor);
            }
            if (viewAncestor != null) {
                viewAncestor.dispatchWindowShown();
            }
        }

        @Override // android.view.IWindow
        public void requestAppKeyboardShortcuts(IResultReceiver receiver, int deviceId) {
            ViewRootImpl viewAncestor = this.mViewAncestor.get();
            if (viewAncestor != null) {
                viewAncestor.dispatchRequestKeyboardShortcuts(receiver, deviceId);
            }
        }

        @Override // android.view.IWindow
        public void dispatchPointerCaptureChanged(boolean hasCapture) {
            ViewRootImpl viewAncestor = this.mViewAncestor.get();
            if (viewAncestor != null) {
                viewAncestor.dispatchPointerCaptureChanged(hasCapture);
            }
        }

        @Override // android.view.IWindow
        public void dispatchFreeformChanged(boolean hasFreeform) {
            ViewRootImpl viewAncestor = this.mViewAncestor.get();
            if (viewAncestor != null) {
                viewAncestor.dispatchFreeformChanged(hasFreeform);
            }
        }
    }

    public static final class CalledFromWrongThreadException extends AndroidRuntimeException {
        @UnsupportedAppUsage
        public CalledFromWrongThreadException(String msg) {
            super(msg);
        }
    }

    static HandlerActionQueue getRunQueue() {
        HandlerActionQueue rq = sRunQueues.get();
        if (rq != null) {
            return rq;
        }
        HandlerActionQueue rq2 = new HandlerActionQueue();
        sRunQueues.set(rq2);
        return rq2;
    }

    private void startDragResizing(Rect initialBounds, boolean fullscreen, Rect systemInsets, Rect stableInsets, int resizeMode) {
        if (!this.mDragResizing) {
            this.mDragResizing = true;
            if (this.mUseMTRenderer) {
                for (int i = this.mWindowCallbacks.size() - 1; i >= 0; i--) {
                    this.mWindowCallbacks.get(i).onWindowDragResizeStart(initialBounds, fullscreen, systemInsets, stableInsets, resizeMode);
                }
            }
            this.mFullRedrawNeeded = true;
        }
    }

    private void endDragResizing() {
        if (this.mDragResizing) {
            this.mDragResizing = false;
            if (this.mUseMTRenderer) {
                for (int i = this.mWindowCallbacks.size() - 1; i >= 0; i--) {
                    this.mWindowCallbacks.get(i).onWindowDragResizeEnd();
                }
            }
            this.mFullRedrawNeeded = true;
        }
    }

    private boolean updateContentDrawBounds() {
        boolean updated = false;
        boolean z = true;
        if (this.mUseMTRenderer) {
            for (int i = this.mWindowCallbacks.size() - 1; i >= 0; i--) {
                updated |= this.mWindowCallbacks.get(i).onContentDrawn(this.mWindowAttributes.surfaceInsets.left, this.mWindowAttributes.surfaceInsets.top, this.mWidth, this.mHeight);
            }
        }
        if (!this.mDragResizing || !this.mReportNextDraw) {
            z = false;
        }
        return updated | z;
    }

    private void requestDrawWindow() {
        if (this.mUseMTRenderer) {
            this.mWindowDrawCountDown = new CountDownLatch(this.mWindowCallbacks.size());
            for (int i = this.mWindowCallbacks.size() - 1; i >= 0; i--) {
                this.mWindowCallbacks.get(i).onRequestDraw((this.mResizeMode == 1 && this.mDragResizing) || this.mReportNextDraw);
            }
        }
    }

    public void reportActivityRelaunched() {
        this.mActivityRelaunched = true;
    }

    public SurfaceControl getSurfaceControl() {
        return this.mSurfaceControl;
    }

    public void dispatchFreeformChanged(boolean hasFreeform) {
        View.AttachInfo attachInfo = this.mAttachInfo;
        if (attachInfo != null) {
            attachInfo.mTreeObserver.dispatchOnWindowAttachedChange(hasFreeform);
        }
    }

    /* access modifiers changed from: package-private */
    public final class AccessibilityInteractionConnectionManager implements AccessibilityManager.AccessibilityStateChangeListener {
        AccessibilityInteractionConnectionManager() {
        }

        @Override // android.view.accessibility.AccessibilityManager.AccessibilityStateChangeListener
        public void onAccessibilityStateChanged(boolean enabled) {
            if (enabled) {
                ensureConnection();
                try {
                    if (ViewRootImpl.this.mAttachInfo.mHasWindowFocus && ViewRootImpl.this.mView != null) {
                        ViewRootImpl.this.mView.sendAccessibilityEvent(32);
                        View focusedView = ViewRootImpl.this.mView.findFocus();
                        if (focusedView != null && focusedView != ViewRootImpl.this.mView) {
                            focusedView.sendAccessibilityEvent(8);
                        }
                    }
                } catch (NullPointerException e) {
                    Slog.w(ViewRootImpl.TAG, "onAccessibilityStateChanged catch NullPointerException " + e);
                }
            } else {
                ensureNoConnection();
                ViewRootImpl.this.mHandler.obtainMessage(21).sendToTarget();
            }
        }

        public void ensureConnection() {
            if (!(ViewRootImpl.this.mAttachInfo.mAccessibilityWindowId != -1)) {
                ViewRootImpl.this.mAttachInfo.mAccessibilityWindowId = ViewRootImpl.this.mAccessibilityManager.addAccessibilityInteractionConnection(ViewRootImpl.this.mWindow, ViewRootImpl.this.mContext.getPackageName(), new AccessibilityInteractionConnection(ViewRootImpl.this));
            }
        }

        public void ensureNoConnection() {
            if (ViewRootImpl.this.mAttachInfo.mAccessibilityWindowId != -1) {
                ViewRootImpl.this.mAttachInfo.mAccessibilityWindowId = -1;
                ViewRootImpl.this.mAccessibilityManager.removeAccessibilityInteractionConnection(ViewRootImpl.this.mWindow);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public final class HighContrastTextManager implements AccessibilityManager.HighTextContrastChangeListener {
        HighContrastTextManager() {
            ThreadedRenderer.setHighContrastText(ViewRootImpl.this.mAccessibilityManager.isHighTextContrastEnabled());
        }

        @Override // android.view.accessibility.AccessibilityManager.HighTextContrastChangeListener
        public void onHighTextContrastStateChanged(boolean enabled) {
            ThreadedRenderer.setHighContrastText(enabled);
            ViewRootImpl.this.destroyHardwareResources();
            ViewRootImpl.this.invalidate();
        }
    }

    /* access modifiers changed from: package-private */
    public static final class AccessibilityInteractionConnection extends IAccessibilityInteractionConnection.Stub {
        private final WeakReference<ViewRootImpl> mViewRootImpl;

        AccessibilityInteractionConnection(ViewRootImpl viewRootImpl) {
            this.mViewRootImpl = new WeakReference<>(viewRootImpl);
        }

        @Override // android.view.accessibility.IAccessibilityInteractionConnection
        public void findAccessibilityNodeInfoByAccessibilityId(long accessibilityNodeId, Region interactiveRegion, int interactionId, IAccessibilityInteractionConnectionCallback callback, int flags, int interrogatingPid, long interrogatingTid, MagnificationSpec spec, Bundle args) {
            ViewRootImpl viewRootImpl = this.mViewRootImpl.get();
            if (viewRootImpl == null || viewRootImpl.mView == null) {
                try {
                    callback.setFindAccessibilityNodeInfosResult(null, interactionId);
                } catch (RemoteException e) {
                }
            } else {
                viewRootImpl.getAccessibilityInteractionController().findAccessibilityNodeInfoByAccessibilityIdClientThread(accessibilityNodeId, interactiveRegion, interactionId, callback, flags, interrogatingPid, interrogatingTid, spec, args);
            }
        }

        @Override // android.view.accessibility.IAccessibilityInteractionConnection
        public void performAccessibilityAction(long accessibilityNodeId, int action, Bundle arguments, int interactionId, IAccessibilityInteractionConnectionCallback callback, int flags, int interrogatingPid, long interrogatingTid) {
            ViewRootImpl viewRootImpl = this.mViewRootImpl.get();
            if (viewRootImpl == null || viewRootImpl.mView == null) {
                try {
                    callback.setPerformAccessibilityActionResult(false, interactionId);
                } catch (RemoteException e) {
                }
            } else {
                viewRootImpl.getAccessibilityInteractionController().performAccessibilityActionClientThread(accessibilityNodeId, action, arguments, interactionId, callback, flags, interrogatingPid, interrogatingTid);
            }
        }

        @Override // android.view.accessibility.IAccessibilityInteractionConnection
        public void findAccessibilityNodeInfosByViewId(long accessibilityNodeId, String viewId, Region interactiveRegion, int interactionId, IAccessibilityInteractionConnectionCallback callback, int flags, int interrogatingPid, long interrogatingTid, MagnificationSpec spec) {
            ViewRootImpl viewRootImpl = this.mViewRootImpl.get();
            if (viewRootImpl == null || viewRootImpl.mView == null) {
                try {
                    callback.setFindAccessibilityNodeInfoResult(null, interactionId);
                } catch (RemoteException e) {
                }
            } else {
                viewRootImpl.getAccessibilityInteractionController().findAccessibilityNodeInfosByViewIdClientThread(accessibilityNodeId, viewId, interactiveRegion, interactionId, callback, flags, interrogatingPid, interrogatingTid, spec);
            }
        }

        @Override // android.view.accessibility.IAccessibilityInteractionConnection
        public void findAccessibilityNodeInfosByText(long accessibilityNodeId, String text, Region interactiveRegion, int interactionId, IAccessibilityInteractionConnectionCallback callback, int flags, int interrogatingPid, long interrogatingTid, MagnificationSpec spec) {
            ViewRootImpl viewRootImpl = this.mViewRootImpl.get();
            if (viewRootImpl == null || viewRootImpl.mView == null) {
                try {
                    callback.setFindAccessibilityNodeInfosResult(null, interactionId);
                } catch (RemoteException e) {
                }
            } else {
                viewRootImpl.getAccessibilityInteractionController().findAccessibilityNodeInfosByTextClientThread(accessibilityNodeId, text, interactiveRegion, interactionId, callback, flags, interrogatingPid, interrogatingTid, spec);
            }
        }

        @Override // android.view.accessibility.IAccessibilityInteractionConnection
        public void findFocus(long accessibilityNodeId, int focusType, Region interactiveRegion, int interactionId, IAccessibilityInteractionConnectionCallback callback, int flags, int interrogatingPid, long interrogatingTid, MagnificationSpec spec) {
            ViewRootImpl viewRootImpl = this.mViewRootImpl.get();
            if (viewRootImpl == null || viewRootImpl.mView == null) {
                try {
                    callback.setFindAccessibilityNodeInfoResult(null, interactionId);
                } catch (RemoteException e) {
                }
            } else {
                viewRootImpl.getAccessibilityInteractionController().findFocusClientThread(accessibilityNodeId, focusType, interactiveRegion, interactionId, callback, flags, interrogatingPid, interrogatingTid, spec);
            }
        }

        @Override // android.view.accessibility.IAccessibilityInteractionConnection
        public void focusSearch(long accessibilityNodeId, int direction, Region interactiveRegion, int interactionId, IAccessibilityInteractionConnectionCallback callback, int flags, int interrogatingPid, long interrogatingTid, MagnificationSpec spec) {
            ViewRootImpl viewRootImpl = this.mViewRootImpl.get();
            if (viewRootImpl == null || viewRootImpl.mView == null) {
                try {
                    callback.setFindAccessibilityNodeInfoResult(null, interactionId);
                } catch (RemoteException e) {
                }
            } else {
                viewRootImpl.getAccessibilityInteractionController().focusSearchClientThread(accessibilityNodeId, direction, interactiveRegion, interactionId, callback, flags, interrogatingPid, interrogatingTid, spec);
            }
        }

        @Override // android.view.accessibility.IAccessibilityInteractionConnection
        public void clearAccessibilityFocus() {
            ViewRootImpl viewRootImpl = this.mViewRootImpl.get();
            if (viewRootImpl != null && viewRootImpl.mView != null) {
                viewRootImpl.getAccessibilityInteractionController().clearAccessibilityFocusClientThread();
            }
        }

        @Override // android.view.accessibility.IAccessibilityInteractionConnection
        public void notifyOutsideTouch() {
            ViewRootImpl viewRootImpl = this.mViewRootImpl.get();
            if (viewRootImpl != null && viewRootImpl.mView != null) {
                viewRootImpl.getAccessibilityInteractionController().notifyOutsideTouchClientThread();
            }
        }
    }

    /* access modifiers changed from: private */
    public class SendWindowContentChangedAccessibilityEvent implements Runnable {
        private int mChangeTypes;
        public long mLastEventTimeMillis;
        public StackTraceElement[] mOrigin;
        public View mSource;

        private SendWindowContentChangedAccessibilityEvent() {
            this.mChangeTypes = 0;
        }

        public void run() {
            View source = this.mSource;
            this.mSource = null;
            if (source == null) {
                Log.e(ViewRootImpl.TAG, "Accessibility content change has no source");
                return;
            }
            if (AccessibilityManager.getInstance(ViewRootImpl.this.mContext).isEnabled()) {
                this.mLastEventTimeMillis = SystemClock.uptimeMillis();
                AccessibilityEvent event = AccessibilityEvent.obtain();
                event.setEventType(2048);
                event.setContentChangeTypes(this.mChangeTypes);
                source.sendAccessibilityEventUnchecked(event);
            } else {
                this.mLastEventTimeMillis = 0;
            }
            source.resetSubtreeAccessibilityStateChanged();
            this.mChangeTypes = 0;
        }

        public void runOrPost(View source, int changeType) {
            if (ViewRootImpl.this.mHandler.getLooper() != Looper.myLooper()) {
                Log.e(ViewRootImpl.TAG, "Accessibility content change on non-UI thread. Future Android versions will throw an exception.", new CalledFromWrongThreadException("Only the original thread that created a view hierarchy can touch its views."));
                ViewRootImpl.this.mHandler.removeCallbacks(this);
                if (this.mSource != null) {
                    run();
                }
            }
            View view = this.mSource;
            if (view != null) {
                View predecessor = ViewRootImpl.this.getCommonPredecessor(view, source);
                if (predecessor != null) {
                    predecessor = predecessor.getSelfOrParentImportantForA11y();
                }
                this.mSource = predecessor != null ? predecessor : source;
                this.mChangeTypes |= changeType;
                return;
            }
            this.mSource = source;
            this.mChangeTypes = changeType;
            long timeSinceLastMillis = SystemClock.uptimeMillis() - this.mLastEventTimeMillis;
            long minEventIntevalMillis = ViewConfiguration.getSendRecurringAccessibilityEventsInterval();
            if (timeSinceLastMillis >= minEventIntevalMillis) {
                removeCallbacksAndRun();
            } else {
                ViewRootImpl.this.mHandler.postDelayed(this, minEventIntevalMillis - timeSinceLastMillis);
            }
        }

        public void removeCallbacksAndRun() {
            ViewRootImpl.this.mHandler.removeCallbacks(this);
            run();
        }
    }

    /* access modifiers changed from: private */
    public static class UnhandledKeyManager {
        private final SparseArray<WeakReference<View>> mCapturedKeys;
        private WeakReference<View> mCurrentReceiver;
        private boolean mDispatched;

        private UnhandledKeyManager() {
            this.mDispatched = true;
            this.mCapturedKeys = new SparseArray<>();
            this.mCurrentReceiver = null;
        }

        /* access modifiers changed from: package-private */
        public boolean dispatch(View root, KeyEvent event) {
            if (this.mDispatched) {
                return false;
            }
            try {
                Trace.traceBegin(8, "UnhandledKeyEvent dispatch");
                this.mDispatched = true;
                View consumer = root.dispatchUnhandledKeyEvent(event);
                if (event.getAction() == 0) {
                    int keycode = event.getKeyCode();
                    if (consumer != null && !KeyEvent.isModifierKey(keycode)) {
                        this.mCapturedKeys.put(keycode, new WeakReference<>(consumer));
                    }
                }
                if (consumer != null) {
                    return true;
                }
                return false;
            } finally {
                Trace.traceEnd(8);
            }
        }

        /* access modifiers changed from: package-private */
        public void preDispatch(KeyEvent event) {
            int idx;
            this.mCurrentReceiver = null;
            if (event.getAction() == 1 && (idx = this.mCapturedKeys.indexOfKey(event.getKeyCode())) >= 0) {
                this.mCurrentReceiver = this.mCapturedKeys.valueAt(idx);
                this.mCapturedKeys.removeAt(idx);
            }
        }

        /* access modifiers changed from: package-private */
        public boolean preViewDispatch(KeyEvent event) {
            this.mDispatched = false;
            if (this.mCurrentReceiver == null) {
                this.mCurrentReceiver = this.mCapturedKeys.get(event.getKeyCode());
            }
            WeakReference<View> weakReference = this.mCurrentReceiver;
            if (weakReference == null) {
                return false;
            }
            View target = weakReference.get();
            if (event.getAction() == 1) {
                this.mCurrentReceiver = null;
            }
            if (target != null && target.isAttachedToWindow()) {
                target.onUnhandledKeyEvent(event);
            }
            return true;
        }
    }

    private static void setDynamicalLogEnable(boolean on) {
        DEBUG_CONFIGURATION = on;
        DEBUG_LAYOUT = on;
        DEBUG_DIALOG = on;
        DEBUG_DRAW = on;
    }

    @Override // android.view.IColorBaseViewRoot
    public ColorViewRootImplHooks getColorViewRootImplHooks() {
        return this.mViewRootHooks;
    }

    @Override // android.view.IColorBaseViewRoot
    public ColorLongshotViewRoot getLongshotViewRoot() {
        ColorViewRootImplHooks colorViewRootImplHooks = this.mViewRootHooks;
        if (colorViewRootImplHooks != null) {
            return colorViewRootImplHooks.getLongshotViewRoot();
        }
        return null;
    }

    public ArrayList<String> getScreenModeViewList() {
        return this.mScreenModeViewList;
    }

    public boolean isScreenModeViewListEmpty() {
        return this.mScreenModeViewList.isEmpty();
    }

    public boolean containInScreenModeViewList(String viewClassNameWithHash) {
        if (viewClassNameWithHash == null || viewClassNameWithHash.isEmpty()) {
            return false;
        }
        return this.mScreenModeViewList.contains(viewClassNameWithHash);
    }

    public void addViewToScreenModeViewList(String viewClassNameWithHash) {
        if (viewClassNameWithHash != null && !viewClassNameWithHash.isEmpty()) {
            this.mScreenModeViewList.add(viewClassNameWithHash);
        }
    }

    public void removeViewFromScreenModeViewList(String viewClassNameWithHash) {
        if (viewClassNameWithHash != null && !viewClassNameWithHash.isEmpty()) {
            this.mScreenModeViewList.remove(viewClassNameWithHash);
        }
    }
}
