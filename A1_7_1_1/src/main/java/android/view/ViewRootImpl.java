package android.view;

import android.animation.LayoutTransition;
import android.annotation.OppoHook;
import android.annotation.OppoHook.OppoHookType;
import android.annotation.OppoHook.OppoRomType;
import android.app.ActivityManagerNative;
import android.app.ActivityThread;
import android.app.ResourcesManager;
import android.content.ClipDescription;
import android.content.ComponentCallbacks;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.content.res.CompatibilityInfo;
import android.content.res.CompatibilityInfo.Translator;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.PorterDuff.Mode;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.drawable.AnimatedVectorDrawable.VectorDrawableAnimatorRT;
import android.graphics.drawable.Drawable;
import android.hardware.display.DisplayManager;
import android.hardware.display.DisplayManager.DisplayListener;
import android.hardware.input.InputManager;
import android.media.AudioManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.os.ParcelFileDescriptor.AutoCloseOutputStream;
import android.os.Process;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.Trace;
import android.telephony.OppoTelephonyConstant;
import android.text.format.DateFormat;
import android.util.AndroidRuntimeException;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Slog;
import android.util.TimeUtils;
import android.util.TypedValue;
import android.view.Choreographer.FrameCallback;
import android.view.IWindow.Stub;
import android.view.InputDevice.MotionRange;
import android.view.InputQueue.Callback;
import android.view.KeyCharacterMap.FallbackAction;
import android.view.Surface.OutOfResourcesException;
import android.view.SurfaceHolder.Callback2;
import android.view.View.MeasureSpec;
import android.view.ViewTreeObserver.InternalInsetsInfo;
import android.view.WindowManager.BadTokenException;
import android.view.WindowManager.InvalidDisplayException;
import android.view.WindowManager.LayoutParams;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityManager.AccessibilityStateChangeListener;
import android.view.accessibility.AccessibilityManager.HighTextContrastChangeListener;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityNodeInfo.AccessibilityAction;
import android.view.accessibility.AccessibilityNodeProvider;
import android.view.accessibility.IAccessibilityInteractionConnection;
import android.view.animation.Interpolator;
import android.view.inputmethod.InputMethodManager;
import android.view.inputmethod.InputMethodManager.FinishedInputEventCallback;
import android.widget.Scroller;
import com.android.internal.R;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.os.IResultReceiver;
import com.android.internal.os.SomeArgs;
import com.android.internal.policy.PhoneFallbackEventHandler;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.view.BaseSurfaceHolder;
import com.android.internal.view.RootViewSurfaceTaker;
import com.color.util.ColorAccidentallyTouchUtils;
import com.oppo.debug.InputLog;
import com.oppo.debug.ProcessCpuTrackerRunnable;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.concurrent.CountDownLatch;
import javax.microedition.khronos.opengles.GL10;

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
    	at jadx.core.utils.BlockUtils.isAllBlocksEmpty(BlockUtils.java:546)
    	at jadx.core.dex.visitors.ExtractFieldInit.getConstructorsList(ExtractFieldInit.java:221)
    	at jadx.core.dex.visitors.ExtractFieldInit.moveCommonFieldsInit(ExtractFieldInit.java:121)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:46)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:42)
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
/*  JADX ERROR: NullPointerException in pass: ClassModifier
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ClassModifier.removeFieldUsageFromConstructor(ClassModifier.java:100)
    	at jadx.core.dex.visitors.ClassModifier.removeSyntheticFields(ClassModifier.java:75)
    	at jadx.core.dex.visitors.ClassModifier.visit(ClassModifier.java:48)
    	at jadx.core.dex.visitors.ClassModifier.visit(ClassModifier.java:40)
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
public final class ViewRootImpl implements ViewParent, Callbacks, HardwareDrawCallbacks {
    private static final String DATE_FORMAT_STRING = "yyyyMMdd_hhmmss";
    private static boolean DBG = false;
    static final boolean DBG_APP_FAST_LAUNCH_ENHANCE = false;
    static final boolean DBG_APP_LAUNCH_ENHANCE = true;
    private static final boolean DBG_TRANSP = false;
    private static boolean DEBUG_CONFIGURATION = false;
    private static final int DEBUG_CONFIGURATION_FLAG = 256;
    private static final boolean DEBUG_DEFAULT = false;
    private static final int DEBUG_DEFAULT_FLAG = 512;
    private static boolean DEBUG_DIALOG = false;
    private static final int DEBUG_DIALOG_FLAG = 8;
    private static final boolean DEBUG_DISABLEHW = false;
    private static boolean DEBUG_DRAW = false;
    private static final int DEBUG_DRAW_FLAG = 2;
    private static final int DEBUG_ENABLE_ALL_FLAG = 1;
    private static boolean DEBUG_FPS = false;
    private static final int DEBUG_FPS_FLAG = 1024;
    private static boolean DEBUG_HWUI = false;
    private static final int DEBUG_HWUI_FLAG = 2048;
    private static boolean DEBUG_IME_ANR = false;
    private static final int DEBUG_IME_ANR_FLAG = 32768;
    private static boolean DEBUG_IMF = false;
    private static final int DEBUG_IMF_FLAG = 128;
    private static boolean DEBUG_INPUT = false;
    private static final int DEBUG_INPUT_FLAG = 4096;
    private static boolean DEBUG_INPUT_RESIZE = false;
    private static final int DEBUG_INPUT_RESIZE_FLAG = 16;
    private static boolean DEBUG_INPUT_STAGES = false;
    private static final int DEBUG_INPUT_STAGES_FLAG = 4194304;
    private static boolean DEBUG_INVALIDATE = false;
    private static final int DEBUG_INVALIDATE_FLAG = 262144;
    private static boolean DEBUG_KEEP_SCREEN_ON = false;
    private static final int DEBUG_KEEP_SCREEN_ON_FLAG = 8388608;
    private static boolean DEBUG_KEY = false;
    private static final int DEBUG_KEY_FLAG = 8192;
    private static boolean DEBUG_LAYOUT = false;
    private static final int DEBUG_LAYOUT_FLAG = 4;
    private static boolean DEBUG_LIFECYCLE = false;
    private static final int DEBUG_LIFECYCLE_FLAG = 65536;
    private static boolean DEBUG_LIGHT = false;
    private static boolean DEBUG_MEASURE_LAYOUT = false;
    private static final int DEBUG_MEASURE_LAYOUT_FLAG = 2097152;
    private static boolean DEBUG_MOTION = false;
    private static final int DEBUG_MOTION_FLAG = 16384;
    private static boolean DEBUG_ORIENTATION = false;
    private static final int DEBUG_ORIENTATION_FLAG = 32;
    private static boolean DEBUG_PANIC = false;
    private static boolean DEBUG_REQUESTLAYOUT = false;
    private static final int DEBUG_REQUESTLAYOUT_FLAG = 131072;
    private static boolean DEBUG_SCHEDULETRAVERSALS = false;
    private static final int DEBUG_SCHEDULETRAVERSALS_FLAG = 524288;
    private static boolean DEBUG_TOUCHMODE = false;
    private static final int DEBUG_TOUCHMODE_FLAG = 1048576;
    private static boolean DEBUG_TRACKBALL = false;
    private static final int DEBUG_TRACKBALL_FLAG = 64;
    private static final String DUMP_IMAGE_FORMAT = ".png";
    private static final String DUMP_IMAGE_PTAH = "/data/dump/";
    private static final float EVENT_270 = 1.5707964f;
    private static final float EVENT_90 = -1.5707964f;
    private static final float EVENT_ORI = 0.0f;
    private static final float EVENT_OTHER = -3.1415927f;
    private static final String INPUT_DISPATCH_STATE_DELIVER_EVENT = "4: Deliver input event";
    private static final String INPUT_DISPATCH_STATE_EARLY_POST_IME_STAGE = "8: Early post IME stage";
    private static final String INPUT_DISPATCH_STATE_ENQUEUE_EVENT = "2: Enqueue input event";
    private static final String INPUT_DISPATCH_STATE_FINISHED = "0: Finish handle input event";
    private static final String INPUT_DISPATCH_STATE_IME_STAGE = "7: IME stage";
    private static final String INPUT_DISPATCH_STATE_NATIVE_POST_IME_STAGE = "9: Native post IME stage";
    private static final String INPUT_DISPATCH_STATE_NATIVE_PRE_IME_STAGE = "5: Native pre IME stage";
    private static final String INPUT_DISPATCH_STATE_PROCESS_EVENT = "3 1: Process input event";
    private static final String INPUT_DISPATCH_STATE_SCHEDULE_EVENT = "3 2: Schedule process input event";
    private static final String INPUT_DISPATCH_STATE_STARTED = "1: Start event from input";
    private static final String INPUT_DISPATCH_STATE_SYNTHETC_INPUT_STAGE = "11: Synthetic input stage";
    private static final String INPUT_DISPATCH_STATE_VIEW_POST_IME_STAGE = "10: View Post IME stage";
    private static final String INPUT_DISPATCH_STATE_VIEW_PRE_IME_STAGE = "6: View pre IME stage";
    private static final int INPUT_TIMEOUT = 6000;
    private static final boolean IS_ENG_BUILD = false;
    private static final String KEYGUARD_PACKAGENAME = "com.android.keyguard";
    private static boolean LOCAL_LOGV = false;
    private static final int LOG_DISABLED = 0;
    private static final int LOG_ENABLED = 1;
    private static final String LOG_PROPERTY_NAME = "debug.viewroot.enable";
    @OppoHook(level = OppoHookType.NEW_FIELD, note = "XiaoKang.Feng@Plf.SDK, 2016-10-28 : Add for Surface detection", property = OppoRomType.ROM)
    private static final int MAX_COUNT = 10;
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
    private static final int MSG_DISPATCH_KEY_FROM_IME = 11;
    private static final int MSG_DISPATCH_SYSTEM_UI_VISIBILITY = 17;
    private static final int MSG_DISPATCH_WINDOW_SHOWN = 25;
    private static final int MSG_INIT_GLOBAL_SCALE = 10000;
    private static final int MSG_INVALIDATE = 1;
    private static final int MSG_INVALIDATE_RECT = 2;
    private static final int MSG_INVALIDATE_WORLD = 22;
    private static final int MSG_PROCESS_INPUT_EVENTS = 19;
    private static final int MSG_RELAYOUT_RETRY = 101;
    private static final int MSG_RELAYOUT_RETRY_DELAY = 200;
    private static final int MSG_REQUEST_KEYBOARD_SHORTCUTS = 26;
    private static final int MSG_RESIZED = 4;
    private static final int MSG_RESIZED_REPORT = 5;
    private static final int MSG_SYNTHESIZE_INPUT_EVENT = 24;
    private static final int MSG_UPDATE_CONFIGURATION = 18;
    private static final int MSG_UPDATE_POINTER_ICON = 27;
    private static final int MSG_WINDOW_FOCUS_CHANGED = 6;
    private static final int MSG_WINDOW_MOVED = 23;
    public static final String PROPERTY_EMULATOR_WIN_OUTSET_BOTTOM_PX = "ro.emu.win_outset_bottom_px";
    private static final String PROPERTY_PROFILE_RENDERING = "viewroot.profile_rendering";
    public static final String PROP_NAME_OPEN_ALL_FRAMES = "debug.screenoff.unlock";
    private static final String TAG = "ViewRootImpl";
    private static final boolean USE_MT_RENDERER = true;
    private static final boolean USE_RENDER_THREAD = false;
    static final Interpolator mResizeInterpolator = null;
    static final ArrayList<ComponentCallbacks> sConfigCallbacks = null;
    static boolean sFirstDrawComplete;
    static final ArrayList<Runnable> sFirstDrawHandlers = null;
    private static long sIdent;
    static final ThreadLocal<HandlerActionQueue> sRunQueues = null;
    View mAccessibilityFocusedHost;
    AccessibilityNodeInfo mAccessibilityFocusedVirtualView;
    AccessibilityInteractionConnectionManager mAccessibilityInteractionConnectionManager;
    AccessibilityInteractionController mAccessibilityInteractionController;
    final AccessibilityManager mAccessibilityManager;
    private boolean mActivityRelaunched;
    boolean mAdded;
    boolean mAddedTouchMode;
    boolean mAppVisible;
    boolean mApplyInsetsRequested;
    final AttachInfo mAttachInfo;
    AudioManager mAudioManager;
    final String mBasePackageName;
    private int mCanvasOffsetX;
    private int mCanvasOffsetY;
    View mCapturingView;
    Choreographer mChoreographer;
    int mClientWindowLayoutFlags;
    private float mCompactScale;
    final ConsumeBatchedInputImmediatelyRunnable mConsumeBatchedInputImmediatelyRunnable;
    boolean mConsumeBatchedInputImmediatelyScheduled;
    boolean mConsumeBatchedInputScheduled;
    final ConsumeBatchedInputRunnable mConsumedBatchedInputRunnable;
    final Context mContext;
    @OppoHook(level = OppoHookType.NEW_FIELD, note = "XiaoKang.Feng@Plf.SDK, 2016-10-28 : Add for Surface detection", property = OppoRomType.ROM)
    private int mCount;
    int mCurScrollY;
    View mCurrentDragView;
    private KeyEvent mCurrentKeyEvent;
    private MotionEvent mCurrentMotion;
    private PointerIcon mCustomPointerIcon;
    private final int mDensity;
    Rect mDirty;
    final Rect mDispatchContentInsets;
    final Rect mDispatchStableInsets;
    Display mDisplay;
    private DisplayInfo mDisplayInfo;
    private final DisplayListener mDisplayListener;
    final DisplayManager mDisplayManager;
    ClipDescription mDragDescription;
    final PointF mDragPoint;
    private boolean mDragResizing;
    boolean mDrawingAllowed;
    FallbackEventHandler mFallbackEventHandler;
    boolean mFirst;
    InputStage mFirstInputStage;
    InputStage mFirstPostImeInputStage;
    private boolean mForceDecorViewVisibility;
    boolean mForceNextWindowRelayout;
    private int mFpsNumFrames;
    private long mFpsPrevTime;
    private long mFpsStartTime;
    private int mFrame;
    boolean mFullRedrawNeeded;
    private boolean mFullScreen;
    private float mGlobalScale;
    private final Object mGraphicLock;
    boolean mHadWindowFocus;
    final ViewRootHandler mHandler;
    boolean mHandlingLayoutInLayoutRequest;
    int mHardwareXOffset;
    int mHardwareYOffset;
    boolean mHasHadWindowFocus;
    int mHeight;
    private int mHideNavigationbarArea;
    HighContrastTextManager mHighContrastTextManager;
    private long mIdent;
    private boolean mIgnoring;
    private boolean mInLayout;
    InputChannel mInputChannel;
    protected final InputEventConsistencyVerifier mInputEventConsistencyVerifier;
    WindowInputEventReceiver mInputEventReceiver;
    InputQueue mInputQueue;
    Callback mInputQueueCallback;
    final InvalidateOnAnimationRunnable mInvalidateOnAnimationRunnable;
    private boolean mInvalidateRootRequested;
    boolean mIsAmbientMode;
    boolean mIsAnimating;
    boolean mIsCreating;
    boolean mIsDrawing;
    boolean mIsInTraversal;
    private boolean mIsKeyguard;
    boolean mIsLuckyMoneyView;
    boolean mIsWeixinLauncherUI;
    private long mKeyEventStartTime;
    private String mKeyEventStatus;
    final Configuration mLastConfiguration;
    final InternalInsetsInfo mLastGivenInsets;
    boolean mLastInCompatMode;
    boolean mLastOverscanRequested;
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
    private long mMotionEventStartTime;
    private String mMotionEventStatus;
    private boolean mNeedsHwRendererSetup;
    boolean mNewSurfaceNeeded;
    private final int mNoncompatDensity;
    int mOrigWindowType;
    boolean mPausedForTransition;
    boolean mPendingAlwaysConsumeNavBar;
    final Rect mPendingBackDropFrame;
    final Configuration mPendingConfiguration;
    final Rect mPendingContentInsets;
    int mPendingInputEventCount;
    QueuedInputEvent mPendingInputEventHead;
    String mPendingInputEventQueueLengthCounterName;
    QueuedInputEvent mPendingInputEventTail;
    final Rect mPendingOutsets;
    final Rect mPendingOverscanInsets;
    final Rect mPendingStableInsets;
    private ArrayList<LayoutTransition> mPendingTransitions;
    final Rect mPendingVisibleInsets;
    private int mPointerIconType;
    private KeyEvent mPreviousKeyEvent;
    private long mPreviousKeyEventFinishTime;
    private MotionEvent mPreviousMotion;
    private long mPreviousMotionEventFinishTime;
    final Region mPreviousTransparentRegion;
    private ProcessCpuTrackerRunnable mProcessCpuTrackerRunnable;
    boolean mProcessInputEventsScheduled;
    private boolean mProfile;
    private boolean mProfileRendering;
    private QueuedInputEvent mQueuedInputEventPool;
    private int mQueuedInputEventPoolSize;
    private boolean mRemoved;
    private FrameCallback mRenderProfiler;
    private boolean mRenderProfilingEnabled;
    boolean mReportNextDraw;
    private int mResizeMode;
    private boolean mRetryLayout;
    private int mScreenHeight;
    private int mScreenWidth;
    boolean mScrollMayChange;
    int mScrollY;
    Scroller mScroller;
    SendWindowContentChangedAccessibilityEvent mSendWindowContentChangedAccessibilityEvent;
    int mSeq;
    private boolean mSoftInputMayChanged;
    int mSoftInputMode;
    private int mStatusBarHeight;
    boolean mStopped;
    final Surface mSurface;
    BaseSurfaceHolder mSurfaceHolder;
    Callback2 mSurfaceHolderCallback;
    InputStage mSyntheticInputStage;
    private String mTag;
    final int mTargetSdkVersion;
    HashSet<View> mTempHashSet;
    final Rect mTempRect;
    final Thread mThread;
    final int[] mTmpLocation;
    final TypedValue mTmpValue;
    Translator mTranslator;
    final Region mTransparentRegion;
    int mTraversalBarrier;
    final TraversalRunnable mTraversalRunnable;
    boolean mTraversalScheduled;
    boolean mUnbufferedInputDispatch;
    View mView;
    final ViewConfiguration mViewConfiguration;
    private int mViewLayoutDirectionInitial;
    @OppoHook(level = OppoHookType.NEW_FIELD, note = "JianHui.Yu@Plf.SDK, 2017-04-13 : Add for Longshot", property = OppoRomType.ROM)
    public final ColorViewRootImplHooks mViewRootHooks;
    int mViewVisibility;
    private boolean mViewVisibilityChanged;
    final Rect mVisRect;
    int mWidth;
    boolean mWillDrawSoon;
    final Rect mWinFrame;
    final W mWindow;
    final LayoutParams mWindowAttributes;
    boolean mWindowAttributesChanged;
    int mWindowAttributesChangesFlag;
    @GuardedBy("mWindowCallbacks")
    final ArrayList<WindowCallbacks> mWindowCallbacks;
    CountDownLatch mWindowDrawCountDown;
    final IWindowSession mWindowSession;

    static class W extends Stub {
        @OppoHook(level = OppoHookType.CHANGE_ACCESS, note = "JianHui.Yu@Plf.SDK, 2016-03-03 : [-private] Modify for Longshot", property = OppoRomType.ROM)
        final WeakReference<ViewRootImpl> mViewAncestor;
        private final IWindowSession mWindowSession;

        W(ViewRootImpl viewAncestor) {
            this.mViewAncestor = new WeakReference(viewAncestor);
            this.mWindowSession = viewAncestor.mWindowSession;
        }

        public void resized(Rect frame, Rect overscanInsets, Rect contentInsets, Rect visibleInsets, Rect stableInsets, Rect outsets, boolean reportDraw, Configuration newConfig, Rect backDropFrame, boolean forceLayout, boolean alwaysConsumeNavBar) {
            ViewRootImpl viewAncestor = (ViewRootImpl) this.mViewAncestor.get();
            if (viewAncestor != null) {
                viewAncestor.dispatchResized(frame, overscanInsets, contentInsets, visibleInsets, stableInsets, outsets, reportDraw, newConfig, backDropFrame, forceLayout, alwaysConsumeNavBar);
            }
        }

        public void moved(int newX, int newY) {
            ViewRootImpl viewAncestor = (ViewRootImpl) this.mViewAncestor.get();
            if (viewAncestor != null) {
                viewAncestor.dispatchMoved(newX, newY);
            }
        }

        public void dispatchAppVisibility(boolean visible) {
            ViewRootImpl viewAncestor = (ViewRootImpl) this.mViewAncestor.get();
            if (ViewRootImpl.DEBUG_LIFECYCLE) {
                Log.v(ViewRootImpl.TAG, "dispatchAppVisibility: visible = " + visible + ", viewAncestor = " + viewAncestor);
            }
            if (viewAncestor != null) {
                viewAncestor.dispatchAppVisibility(visible);
            }
        }

        public void dispatchGetNewSurface() {
            ViewRootImpl viewAncestor = (ViewRootImpl) this.mViewAncestor.get();
            if (viewAncestor != null) {
                viewAncestor.dispatchGetNewSurface();
            }
        }

        public void windowFocusChanged(boolean hasFocus, boolean inTouchMode) {
            ViewRootImpl viewAncestor = (ViewRootImpl) this.mViewAncestor.get();
            if (ViewRootImpl.DEBUG_IMF) {
                Log.v(ViewRootImpl.TAG, "W windowFocusChanged: hasFocus = " + hasFocus + ", inTouchMode = " + inTouchMode + ", viewAncestor = " + viewAncestor + ", this = " + this);
            }
            if (viewAncestor != null) {
                viewAncestor.windowFocusChanged(hasFocus, inTouchMode);
            }
        }

        private static int checkCallingPermission(String permission) {
            try {
                return ActivityManagerNative.getDefault().checkPermission(permission, Binder.getCallingPid(), Binder.getCallingUid());
            } catch (RemoteException e) {
                return -1;
            }
        }

        /* JADX WARNING: Removed duplicated region for block: B:42:? A:{SYNTHETIC, RETURN} */
        /* JADX WARNING: Removed duplicated region for block: B:22:0x005e A:{SYNTHETIC, Splitter: B:22:0x005e} */
        /* JADX WARNING: Removed duplicated region for block: B:28:0x006a A:{SYNTHETIC, Splitter: B:28:0x006a} */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void executeCommand(String command, String parameters, ParcelFileDescriptor out) {
            IOException e;
            Throwable th;
            ViewRootImpl viewAncestor = (ViewRootImpl) this.mViewAncestor.get();
            if (viewAncestor != null) {
                View view = viewAncestor.mView;
                if (view == null) {
                    return;
                }
                if (checkCallingPermission("android.permission.DUMP") != 0) {
                    throw new SecurityException("Insufficient permissions to invoke executeCommand() from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid());
                }
                OutputStream clientStream = null;
                try {
                    OutputStream clientStream2 = new AutoCloseOutputStream(out);
                    try {
                        ViewDebug.dispatchCommand(view, command, parameters, clientStream2);
                        if (clientStream2 != null) {
                            try {
                                clientStream2.close();
                            } catch (IOException e2) {
                                e2.printStackTrace();
                            }
                        }
                    } catch (IOException e3) {
                        e2 = e3;
                        clientStream = clientStream2;
                        try {
                            e2.printStackTrace();
                            if (clientStream == null) {
                            }
                        } catch (Throwable th2) {
                            th = th2;
                            if (clientStream != null) {
                                try {
                                    clientStream.close();
                                } catch (IOException e22) {
                                    e22.printStackTrace();
                                }
                            }
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        clientStream = clientStream2;
                        if (clientStream != null) {
                        }
                        throw th;
                    }
                } catch (IOException e4) {
                    e22 = e4;
                    e22.printStackTrace();
                    if (clientStream == null) {
                        try {
                            clientStream.close();
                        } catch (IOException e222) {
                            e222.printStackTrace();
                        }
                    }
                }
            }
        }

        public void closeSystemDialogs(String reason) {
            ViewRootImpl viewAncestor = (ViewRootImpl) this.mViewAncestor.get();
            if (ViewRootImpl.LOCAL_LOGV) {
                Log.v(ViewRootImpl.TAG, "Close system dialogs in " + viewAncestor + " for " + reason);
            }
            if (viewAncestor != null) {
                viewAncestor.dispatchCloseSystemDialogs(reason);
            }
        }

        public void dispatchWallpaperOffsets(float x, float y, float xStep, float yStep, boolean sync) {
            if (sync) {
                try {
                    this.mWindowSession.wallpaperOffsetsComplete(asBinder());
                } catch (RemoteException e) {
                    Log.e(ViewRootImpl.TAG, "RemoteException happens when dispatchWallpaperOffsets.", e);
                }
            }
        }

        public void dispatchWallpaperCommand(String action, int x, int y, int z, Bundle extras, boolean sync) {
            if (sync) {
                try {
                    this.mWindowSession.wallpaperCommandComplete(asBinder(), null);
                } catch (RemoteException e) {
                    Log.e(ViewRootImpl.TAG, "RemoteException happens when dispatchWallpaperCommand.", e);
                }
            }
        }

        public void dispatchDragEvent(DragEvent event) {
            ViewRootImpl viewAncestor = (ViewRootImpl) this.mViewAncestor.get();
            if (ViewRootImpl.LOCAL_LOGV || ViewRootImpl.DEBUG_INPUT) {
                Log.v(ViewRootImpl.TAG, "Dispatch drag event " + event + " in " + viewAncestor);
            }
            if (viewAncestor != null) {
                viewAncestor.dispatchDragEvent(event);
            }
        }

        public void updatePointerIcon(float x, float y) {
            ViewRootImpl viewAncestor = (ViewRootImpl) this.mViewAncestor.get();
            if (viewAncestor != null) {
                viewAncestor.updatePointerIcon(x, y);
            }
        }

        public void dispatchSystemUiVisibilityChanged(int seq, int globalVisibility, int localValue, int localChanges) {
            ViewRootImpl viewAncestor = (ViewRootImpl) this.mViewAncestor.get();
            if (ViewRootImpl.LOCAL_LOGV) {
                Log.v(ViewRootImpl.TAG, "dispatchSystemUiVisibilityChanged: seq = " + seq + ", globalVisibility = " + globalVisibility + ", localValue = " + localValue + ", localChanges = " + localChanges + ", viewAncestor" + viewAncestor);
            }
            if (viewAncestor != null) {
                viewAncestor.dispatchSystemUiVisibilityChanged(seq, globalVisibility, localValue, localChanges);
            }
        }

        public void dispatchWindowShown() {
            ViewRootImpl viewAncestor = (ViewRootImpl) this.mViewAncestor.get();
            if (ViewRootImpl.DEBUG_DRAW) {
                Log.v(ViewRootImpl.TAG, "doneAnimating: viewAncestor" + viewAncestor);
            }
            if (viewAncestor != null) {
                viewAncestor.dispatchWindowShown();
            }
        }

        public void requestAppKeyboardShortcuts(IResultReceiver receiver, int deviceId) {
            ViewRootImpl viewAncestor = (ViewRootImpl) this.mViewAncestor.get();
            if (viewAncestor != null) {
                viewAncestor.dispatchRequestKeyboardShortcuts(receiver, deviceId);
            }
        }

        public void enableLog(boolean enable) {
            ViewRootImpl viewAncestor = (ViewRootImpl) this.mViewAncestor.get();
            if (viewAncestor != null) {
                viewAncestor.enableLog(enable);
            }
        }

        public void dumpInputDispatchingStatus() {
            ViewRootImpl viewAncestor = (ViewRootImpl) this.mViewAncestor.get();
            if (viewAncestor != null) {
                viewAncestor.dumpInputDispatchingStatus();
            }
        }

        public boolean isUserDefinedToast() {
            ViewRootImpl viewAncestor = (ViewRootImpl) this.mViewAncestor.get();
            if (viewAncestor == null) {
                return false;
            }
            View view = viewAncestor.getView();
            if (view != null) {
                return view.mID != 201458937;
            } else {
                return false;
            }
        }

        public void enableLogLight(boolean enable) {
            ViewRootImpl viewAncestor = (ViewRootImpl) this.mViewAncestor.get();
            if (viewAncestor != null) {
                viewAncestor.enableLogLight(enable);
            }
        }

        public void updateWindowNavigationBarColor(int color) {
            ViewRootImpl viewAncestor = (ViewRootImpl) this.mViewAncestor.get();
            if (viewAncestor != null) {
                viewAncestor.updateWindowNavigationBarColor(color);
            }
        }
    }

    /* renamed from: android.view.ViewRootImpl$3 */
    class AnonymousClass3 implements FrameCallback {
        final /* synthetic */ ViewRootImpl this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.view.ViewRootImpl.3.<init>(android.view.ViewRootImpl):void, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        AnonymousClass3(android.view.ViewRootImpl r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.view.ViewRootImpl.3.<init>(android.view.ViewRootImpl):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.view.ViewRootImpl.3.<init>(android.view.ViewRootImpl):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.view.ViewRootImpl.3.doFrame(long):void, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public void doFrame(long r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.view.ViewRootImpl.3.doFrame(long):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.view.ViewRootImpl.3.doFrame(long):void");
        }
    }

    /* renamed from: android.view.ViewRootImpl$4 */
    class AnonymousClass4 implements Runnable {
        final /* synthetic */ ViewRootImpl this$0;

        AnonymousClass4(ViewRootImpl this$0) {
            this.this$0 = this$0;
        }

        public void run() {
            this.this$0.mProfileRendering = SystemProperties.getBoolean(ViewRootImpl.PROPERTY_PROFILE_RENDERING, false);
            this.this$0.profileRendering(this.this$0.mAttachInfo.mHasWindowFocus);
            if (this.this$0.mAttachInfo.mHardwareRenderer != null && this.this$0.mAttachInfo.mHardwareRenderer.loadSystemProperties()) {
                this.this$0.invalidate();
            }
            boolean layout = SystemProperties.getBoolean(View.DEBUG_LAYOUT_PROPERTY, false);
            if (layout != this.this$0.mAttachInfo.mDebugLayout) {
                this.this$0.mAttachInfo.mDebugLayout = layout;
                if (!this.this$0.mHandler.hasMessages(22)) {
                    this.this$0.mHandler.sendEmptyMessageDelayed(22, 200);
                }
            }
        }
    }

    static final class AccessibilityInteractionConnection extends IAccessibilityInteractionConnection.Stub {
        private final WeakReference<ViewRootImpl> mViewRootImpl;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: android.view.ViewRootImpl.AccessibilityInteractionConnection.<init>(android.view.ViewRootImpl):void, dex:  in method: android.view.ViewRootImpl.AccessibilityInteractionConnection.<init>(android.view.ViewRootImpl):void, dex: 
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
            Caused by: jadx.core.utils.exceptions.DecodeException: null in method: android.view.ViewRootImpl.AccessibilityInteractionConnection.<init>(android.view.ViewRootImpl):void, dex: 
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
            	... 10 more
            Caused by: java.io.EOFException
            	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
            	at com.android.dx.io.instructions.ShortArrayCodeInput.readLong(ShortArrayCodeInput.java:71)
            	at com.android.dx.io.instructions.InstructionCodec$31.decode(InstructionCodec.java:652)
            	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
            	... 11 more
            */
        AccessibilityInteractionConnection(android.view.ViewRootImpl r1) {
            /*
            // Can't load method instructions: Load method exception: null in method: android.view.ViewRootImpl.AccessibilityInteractionConnection.<init>(android.view.ViewRootImpl):void, dex:  in method: android.view.ViewRootImpl.AccessibilityInteractionConnection.<init>(android.view.ViewRootImpl):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.view.ViewRootImpl.AccessibilityInteractionConnection.<init>(android.view.ViewRootImpl):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.view.ViewRootImpl.AccessibilityInteractionConnection.findAccessibilityNodeInfoByAccessibilityId(long, android.graphics.Region, int, android.view.accessibility.IAccessibilityInteractionConnectionCallback, int, int, long, android.view.MagnificationSpec):void, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public void findAccessibilityNodeInfoByAccessibilityId(long r1, android.graphics.Region r3, int r4, android.view.accessibility.IAccessibilityInteractionConnectionCallback r5, int r6, int r7, long r8, android.view.MagnificationSpec r10) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.view.ViewRootImpl.AccessibilityInteractionConnection.findAccessibilityNodeInfoByAccessibilityId(long, android.graphics.Region, int, android.view.accessibility.IAccessibilityInteractionConnectionCallback, int, int, long, android.view.MagnificationSpec):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.view.ViewRootImpl.AccessibilityInteractionConnection.findAccessibilityNodeInfoByAccessibilityId(long, android.graphics.Region, int, android.view.accessibility.IAccessibilityInteractionConnectionCallback, int, int, long, android.view.MagnificationSpec):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.view.ViewRootImpl.AccessibilityInteractionConnection.findAccessibilityNodeInfosByText(long, java.lang.String, android.graphics.Region, int, android.view.accessibility.IAccessibilityInteractionConnectionCallback, int, int, long, android.view.MagnificationSpec):void, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public void findAccessibilityNodeInfosByText(long r1, java.lang.String r3, android.graphics.Region r4, int r5, android.view.accessibility.IAccessibilityInteractionConnectionCallback r6, int r7, int r8, long r9, android.view.MagnificationSpec r11) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.view.ViewRootImpl.AccessibilityInteractionConnection.findAccessibilityNodeInfosByText(long, java.lang.String, android.graphics.Region, int, android.view.accessibility.IAccessibilityInteractionConnectionCallback, int, int, long, android.view.MagnificationSpec):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.view.ViewRootImpl.AccessibilityInteractionConnection.findAccessibilityNodeInfosByText(long, java.lang.String, android.graphics.Region, int, android.view.accessibility.IAccessibilityInteractionConnectionCallback, int, int, long, android.view.MagnificationSpec):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.view.ViewRootImpl.AccessibilityInteractionConnection.findAccessibilityNodeInfosByViewId(long, java.lang.String, android.graphics.Region, int, android.view.accessibility.IAccessibilityInteractionConnectionCallback, int, int, long, android.view.MagnificationSpec):void, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public void findAccessibilityNodeInfosByViewId(long r1, java.lang.String r3, android.graphics.Region r4, int r5, android.view.accessibility.IAccessibilityInteractionConnectionCallback r6, int r7, int r8, long r9, android.view.MagnificationSpec r11) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.view.ViewRootImpl.AccessibilityInteractionConnection.findAccessibilityNodeInfosByViewId(long, java.lang.String, android.graphics.Region, int, android.view.accessibility.IAccessibilityInteractionConnectionCallback, int, int, long, android.view.MagnificationSpec):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.view.ViewRootImpl.AccessibilityInteractionConnection.findAccessibilityNodeInfosByViewId(long, java.lang.String, android.graphics.Region, int, android.view.accessibility.IAccessibilityInteractionConnectionCallback, int, int, long, android.view.MagnificationSpec):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.view.ViewRootImpl.AccessibilityInteractionConnection.findFocus(long, int, android.graphics.Region, int, android.view.accessibility.IAccessibilityInteractionConnectionCallback, int, int, long, android.view.MagnificationSpec):void, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public void findFocus(long r1, int r3, android.graphics.Region r4, int r5, android.view.accessibility.IAccessibilityInteractionConnectionCallback r6, int r7, int r8, long r9, android.view.MagnificationSpec r11) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.view.ViewRootImpl.AccessibilityInteractionConnection.findFocus(long, int, android.graphics.Region, int, android.view.accessibility.IAccessibilityInteractionConnectionCallback, int, int, long, android.view.MagnificationSpec):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.view.ViewRootImpl.AccessibilityInteractionConnection.findFocus(long, int, android.graphics.Region, int, android.view.accessibility.IAccessibilityInteractionConnectionCallback, int, int, long, android.view.MagnificationSpec):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.view.ViewRootImpl.AccessibilityInteractionConnection.focusSearch(long, int, android.graphics.Region, int, android.view.accessibility.IAccessibilityInteractionConnectionCallback, int, int, long, android.view.MagnificationSpec):void, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public void focusSearch(long r1, int r3, android.graphics.Region r4, int r5, android.view.accessibility.IAccessibilityInteractionConnectionCallback r6, int r7, int r8, long r9, android.view.MagnificationSpec r11) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.view.ViewRootImpl.AccessibilityInteractionConnection.focusSearch(long, int, android.graphics.Region, int, android.view.accessibility.IAccessibilityInteractionConnectionCallback, int, int, long, android.view.MagnificationSpec):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.view.ViewRootImpl.AccessibilityInteractionConnection.focusSearch(long, int, android.graphics.Region, int, android.view.accessibility.IAccessibilityInteractionConnectionCallback, int, int, long, android.view.MagnificationSpec):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.view.ViewRootImpl.AccessibilityInteractionConnection.performAccessibilityAction(long, int, android.os.Bundle, int, android.view.accessibility.IAccessibilityInteractionConnectionCallback, int, int, long):void, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public void performAccessibilityAction(long r1, int r3, android.os.Bundle r4, int r5, android.view.accessibility.IAccessibilityInteractionConnectionCallback r6, int r7, int r8, long r9) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.view.ViewRootImpl.AccessibilityInteractionConnection.performAccessibilityAction(long, int, android.os.Bundle, int, android.view.accessibility.IAccessibilityInteractionConnectionCallback, int, int, long):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.view.ViewRootImpl.AccessibilityInteractionConnection.performAccessibilityAction(long, int, android.os.Bundle, int, android.view.accessibility.IAccessibilityInteractionConnectionCallback, int, int, long):void");
        }
    }

    final class AccessibilityInteractionConnectionManager implements AccessibilityStateChangeListener {
        final /* synthetic */ ViewRootImpl this$0;

        AccessibilityInteractionConnectionManager(ViewRootImpl this$0) {
            this.this$0 = this$0;
        }

        public void onAccessibilityStateChanged(boolean enabled) {
            if (enabled) {
                ensureConnection();
                try {
                    if (this.this$0.mAttachInfo.mHasWindowFocus && this.this$0.mView != null) {
                        this.this$0.mView.sendAccessibilityEvent(32);
                        View focusedView = this.this$0.mView.findFocus();
                        if (focusedView != null && focusedView != this.this$0.mView) {
                            focusedView.sendAccessibilityEvent(8);
                            return;
                        }
                        return;
                    }
                    return;
                } catch (NullPointerException e) {
                    Slog.w(ViewRootImpl.TAG, "onAccessibilityStateChanged catch NullPointerException " + e);
                    return;
                }
            }
            ensureNoConnection();
            this.this$0.mHandler.obtainMessage(21).sendToTarget();
        }

        public void ensureConnection() {
            if (!(this.this$0.mAttachInfo.mAccessibilityWindowId != Integer.MAX_VALUE)) {
                this.this$0.mAttachInfo.mAccessibilityWindowId = this.this$0.mAccessibilityManager.addAccessibilityInteractionConnection(this.this$0.mWindow, this.this$0.mContext.getPackageName(), new AccessibilityInteractionConnection(this.this$0));
            }
        }

        public void ensureNoConnection() {
            if (this.this$0.mAttachInfo.mAccessibilityWindowId != Integer.MAX_VALUE) {
                this.this$0.mAttachInfo.mAccessibilityWindowId = Integer.MAX_VALUE;
                this.this$0.mAccessibilityManager.removeAccessibilityInteractionConnection(this.this$0.mWindow);
            }
        }
    }

    abstract class InputStage {
        protected static final String DEBUG_TAG = "ANR_LOG";
        protected static final int FINISH_HANDLED = 1;
        protected static final int FINISH_NOT_HANDLED = 2;
        protected static final int FORWARD = 0;
        protected static final long SPENT_TOO_MUCH_TIME = 500;
        protected long mDeliverTime;
        private long mDeliveredTimestamp;
        private final InputStage mNext;
        final /* synthetic */ ViewRootImpl this$0;

        public InputStage(ViewRootImpl this$0, InputStage next) {
            this.this$0 = this$0;
            this.mDeliverTime = 0;
            this.mNext = next;
        }

        public final void deliver(QueuedInputEvent q) {
            this.mDeliveredTimestamp = System.currentTimeMillis();
            this.mDeliverTime = System.currentTimeMillis();
            if ((q.mFlags & 4) != 0) {
                forward(q);
            } else if (shouldDropInputEvent(q)) {
                finish(q, false);
            } else {
                apply(q, onProcess(q));
            }
        }

        protected void finish(QueuedInputEvent q, boolean handled) {
            q.mFlags |= 4;
            if (handled) {
                q.mFlags |= 8;
            }
            forward(q);
        }

        protected void forward(QueuedInputEvent q) {
            onDeliverToNext(q);
        }

        protected void apply(QueuedInputEvent q, int result) {
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

        protected int onProcess(QueuedInputEvent q) {
            return 0;
        }

        protected void onDeliverToNext(QueuedInputEvent q) {
            if (SystemClock.uptimeMillis() - this.mDeliverTime > SPENT_TOO_MUCH_TIME) {
                Log.v(DEBUG_TAG, "Spent too much time, " + Long.toString(SystemClock.uptimeMillis() - this.mDeliverTime) + " ms in " + getClass().getSimpleName());
            }
            if (ViewRootImpl.DEBUG_INPUT_STAGES || InputLog.DEBUG) {
                Log.v(this.this$0.mTag, "Done with " + getClass().getSimpleName() + ". " + q);
            }
            if (this.mNext != null) {
                this.mNext.deliver(q);
            } else {
                this.this$0.finishInputEvent(q);
            }
        }

        protected boolean shouldDropInputEvent(QueuedInputEvent q) {
            if (this.this$0.mView == null || !this.this$0.mAdded) {
                Slog.w(this.this$0.mTag, "Dropping event due to root view being removed: " + q.mEvent);
                return true;
            } else if ((this.this$0.mAttachInfo.mHasWindowFocus || q.mEvent.isFromSource(2)) && !this.this$0.mStopped && ((!this.this$0.mIsAmbientMode || q.mEvent.isFromSource(1)) && (!this.this$0.mPausedForTransition || isBack(q.mEvent)))) {
                return false;
            } else {
                if (ViewRootImpl.isTerminalInputEvent(q.mEvent)) {
                    q.mEvent.cancel();
                    Slog.w(this.this$0.mTag, "Cancelling event due to no window focus: " + q.mEvent + ", hasFocus:" + this.this$0.mAttachInfo.mHasWindowFocus + ", mStopped:" + this.this$0.mStopped + ", mPausedForTransition:" + this.this$0.mPausedForTransition);
                    return false;
                }
                Slog.w(this.this$0.mTag, "Dropping event due to no window focus: " + q.mEvent + ", hasFocus:" + this.this$0.mAttachInfo.mHasWindowFocus + ", mStopped:" + this.this$0.mStopped + ", mPausedForTransition:" + this.this$0.mPausedForTransition);
                return true;
            }
        }

        void dump(String prefix, PrintWriter writer) {
            if (this.mNext != null) {
                this.mNext.dump(prefix, writer);
            }
        }

        private boolean isBack(InputEvent event) {
            boolean z = false;
            if (!(event instanceof KeyEvent)) {
                return false;
            }
            if (((KeyEvent) event).getKeyCode() == 4) {
                z = true;
            }
            return z;
        }

        void clear() {
            this.mDeliveredTimestamp = 0;
            if (this.mNext != null) {
                this.mNext.clear();
            }
        }

        void dumpStageInfo(SimpleDateFormat sdf) {
            Date deliveredTime = new Date(this.mDeliveredTimestamp);
            if (this.mDeliveredTimestamp != 0) {
                Log.v(this.this$0.mTag, "Input event delivered to " + this + " at " + sdf.format(deliveredTime));
            }
            if (this.mNext != null) {
                this.mNext.dumpStageInfo(sdf);
            }
        }
    }

    abstract class AsyncInputStage extends InputStage {
        protected static final int DEFER = 3;
        private QueuedInputEvent mQueueHead;
        private int mQueueLength;
        private QueuedInputEvent mQueueTail;
        private final String mTraceCounter;
        final /* synthetic */ ViewRootImpl this$0;

        public AsyncInputStage(ViewRootImpl this$0, InputStage next, String traceCounter) {
            this.this$0 = this$0;
            super(this$0, next);
            this.mTraceCounter = traceCounter;
        }

        protected void defer(QueuedInputEvent q) {
            q.mFlags |= 2;
            enqueue(q);
        }

        protected void forward(QueuedInputEvent q) {
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
            if (blocked) {
                if (curr == null) {
                    enqueue(q);
                }
                return;
            }
            if (curr != null) {
                curr = curr.mNext;
                dequeue(q, prev);
            }
            super.forward(q);
            while (curr != null) {
                if (deviceId != curr.mEvent.getDeviceId()) {
                    prev = curr;
                    curr = curr.mNext;
                } else if ((curr.mFlags & 2) != 0) {
                    break;
                } else {
                    QueuedInputEvent next = curr.mNext;
                    dequeue(curr, prev);
                    super.forward(curr);
                    curr = next;
                }
            }
        }

        protected void apply(QueuedInputEvent q, int result) {
            if (result == 3) {
                defer(q);
            } else {
                super.apply(q, result);
            }
        }

        private void enqueue(QueuedInputEvent q) {
            if (this.mQueueTail == null) {
                this.mQueueHead = q;
                this.mQueueTail = q;
            } else {
                this.mQueueTail.mNext = q;
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

        void dump(String prefix, PrintWriter writer) {
            writer.print(prefix);
            writer.print(getClass().getName());
            writer.print(": mQueueLength=");
            writer.println(this.mQueueLength);
            super.dump(prefix, writer);
        }
    }

    public static final class CalledFromWrongThreadException extends AndroidRuntimeException {
        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.view.ViewRootImpl.CalledFromWrongThreadException.<init>(java.lang.String):void, dex: 
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
        public CalledFromWrongThreadException(java.lang.String r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.view.ViewRootImpl.CalledFromWrongThreadException.<init>(java.lang.String):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.view.ViewRootImpl.CalledFromWrongThreadException.<init>(java.lang.String):void");
        }
    }

    @OppoHook(level = OppoHookType.CHANGE_ACCESS, note = "JianHui.Yu@Plf.SDK, 2016-04-14 : [-final] Modify for Longshot", property = OppoRomType.ROM)
    class WindowInputEventReceiver extends InputEventReceiver {
        final /* synthetic */ ViewRootImpl this$0;

        public WindowInputEventReceiver(ViewRootImpl this$0, InputChannel inputChannel, Looper looper) {
            this.this$0 = this$0;
            super(inputChannel, looper);
        }

        public void onInputEvent(InputEvent event) {
            if (event instanceof KeyEvent) {
                this.this$0.mCurrentKeyEvent = (KeyEvent) event;
                this.this$0.mKeyEventStartTime = System.currentTimeMillis();
                this.this$0.mKeyEventStatus = ViewRootImpl.INPUT_DISPATCH_STATE_STARTED;
            } else {
                this.this$0.mCurrentMotion = (MotionEvent) event;
                this.this$0.mMotionEventStartTime = System.currentTimeMillis();
                this.this$0.mMotionEventStatus = ViewRootImpl.INPUT_DISPATCH_STATE_STARTED;
            }
            this.this$0.enqueueInputEvent(event, this, 0, true);
        }

        public void onBatchedInputEventPending() {
            if (ViewRootImpl.DEBUG_INPUT || ViewRootImpl.DEBUG_KEY || ViewRootImpl.DEBUG_MOTION) {
                Log.v(this.this$0.mTag, "onBatchedInputEventPending: this = " + this);
            }
            if (this.this$0.mUnbufferedInputDispatch) {
                super.onBatchedInputEventPending();
            } else {
                this.this$0.scheduleConsumeBatchedInput();
            }
        }

        public void dispose() {
            this.this$0.unscheduleConsumeBatchedInput();
            super.dispose();
        }
    }

    @OppoHook(level = OppoHookType.NEW_CLASS, note = "JianHui.Yu@Plf.SDK, 2017-04-13 : Add for Longshot", property = OppoRomType.ROM)
    class ColorWindowInputEventReceiver extends WindowInputEventReceiver {
        private final ViewRootImpl mViewAncestor;
        final /* synthetic */ ViewRootImpl this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: android.view.ViewRootImpl.ColorWindowInputEventReceiver.<init>(android.view.ViewRootImpl, android.view.InputChannel, android.os.Looper, android.view.ViewRootImpl):void, dex:  in method: android.view.ViewRootImpl.ColorWindowInputEventReceiver.<init>(android.view.ViewRootImpl, android.view.InputChannel, android.os.Looper, android.view.ViewRootImpl):void, dex: 
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
            Caused by: jadx.core.utils.exceptions.DecodeException: null in method: android.view.ViewRootImpl.ColorWindowInputEventReceiver.<init>(android.view.ViewRootImpl, android.view.InputChannel, android.os.Looper, android.view.ViewRootImpl):void, dex: 
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
            	... 10 more
            Caused by: java.io.EOFException
            	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
            	at com.android.dx.io.instructions.InstructionCodec.decodeRegisterList(InstructionCodec.java:920)
            	at com.android.dx.io.instructions.InstructionCodec.access$900(InstructionCodec.java:31)
            	at com.android.dx.io.instructions.InstructionCodec$25.decode(InstructionCodec.java:572)
            	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
            	... 11 more
            */
        public ColorWindowInputEventReceiver(android.view.ViewRootImpl r1, android.view.InputChannel r2, android.os.Looper r3, android.view.ViewRootImpl r4) {
            /*
            // Can't load method instructions: Load method exception: null in method: android.view.ViewRootImpl.ColorWindowInputEventReceiver.<init>(android.view.ViewRootImpl, android.view.InputChannel, android.os.Looper, android.view.ViewRootImpl):void, dex:  in method: android.view.ViewRootImpl.ColorWindowInputEventReceiver.<init>(android.view.ViewRootImpl, android.view.InputChannel, android.os.Looper, android.view.ViewRootImpl):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.view.ViewRootImpl.ColorWindowInputEventReceiver.<init>(android.view.ViewRootImpl, android.view.InputChannel, android.os.Looper, android.view.ViewRootImpl):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.view.ViewRootImpl.ColorWindowInputEventReceiver.onInputEvent(android.view.InputEvent):void, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public void onInputEvent(android.view.InputEvent r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.view.ViewRootImpl.ColorWindowInputEventReceiver.onInputEvent(android.view.InputEvent):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.view.ViewRootImpl.ColorWindowInputEventReceiver.onInputEvent(android.view.InputEvent):void");
        }
    }

    final class ConsumeBatchedInputImmediatelyRunnable implements Runnable {
        final /* synthetic */ ViewRootImpl this$0;

        ConsumeBatchedInputImmediatelyRunnable(ViewRootImpl this$0) {
            this.this$0 = this$0;
        }

        public void run() {
            this.this$0.doConsumeBatchedInput(-1);
        }
    }

    final class ConsumeBatchedInputRunnable implements Runnable {
        final /* synthetic */ ViewRootImpl this$0;

        ConsumeBatchedInputRunnable(ViewRootImpl this$0) {
            this.this$0 = this$0;
        }

        public void run() {
            this.this$0.doConsumeBatchedInput(this.this$0.mChoreographer.getFrameTimeNanos());
        }
    }

    final class EarlyPostImeInputStage extends InputStage {
        final /* synthetic */ ViewRootImpl this$0;

        public EarlyPostImeInputStage(ViewRootImpl this$0, InputStage next) {
            this.this$0 = this$0;
            super(this$0, next);
        }

        protected int onProcess(QueuedInputEvent q) {
            if (q.mEvent instanceof KeyEvent) {
                this.this$0.mKeyEventStatus = ViewRootImpl.INPUT_DISPATCH_STATE_EARLY_POST_IME_STAGE;
                return processKeyEvent(q);
            }
            this.this$0.mMotionEventStatus = ViewRootImpl.INPUT_DISPATCH_STATE_EARLY_POST_IME_STAGE;
            if (OppoScreenDragUtil.isOffsetState() && q != null && q.mEvent != null && (q.mEvent instanceof MotionEvent)) {
                if (this.this$0.needScale()) {
                    OppoScreenDragUtil.setEventLocationScale((MotionEvent) q.mEvent, this.this$0.mCompactScale);
                } else {
                    OppoScreenDragUtil.setEventLocation((MotionEvent) q.mEvent);
                }
            }
            if (q != null && q.mEvent != null && (q.mEvent instanceof MotionEvent) && this.this$0.swipeFromBottom((MotionEvent) q.mEvent)) {
                if (InputLog.DEBUG) {
                    Slog.v(ViewRootImpl.TAG, "swipe input: " + this.this$0.mIgnoring);
                }
                return 1;
            } else if ((q.mEvent.getSource() & 2) != 0) {
                return processPointerEvent(q);
            } else {
                return 0;
            }
        }

        private int processKeyEvent(QueuedInputEvent q) {
            KeyEvent event = q.mEvent;
            if (this.this$0.checkForLeavingTouchModeAndConsume(event)) {
                return 1;
            }
            this.this$0.mFallbackEventHandler.preDispatchKeyEvent(event);
            return 0;
        }

        private int processPointerEvent(QueuedInputEvent q) {
            MotionEvent event = q.mEvent;
            if (this.this$0.mTranslator != null) {
                this.this$0.mTranslator.translateEventInScreenToAppWindow(event);
            }
            int action = event.getAction();
            if (action == 0 || action == 8) {
                this.this$0.ensureTouchMode(true);
            }
            if (this.this$0.mCurScrollY != 0) {
                event.offsetLocation(0.0f, (float) this.this$0.mCurScrollY);
            }
            if (event.isTouchEvent()) {
                this.this$0.mLastTouchPoint.x = event.getRawX();
                this.this$0.mLastTouchPoint.y = event.getRawY();
                this.this$0.mLastTouchSource = event.getSource();
            }
            return 0;
        }
    }

    final class HighContrastTextManager implements HighTextContrastChangeListener {
        final /* synthetic */ ViewRootImpl this$0;

        HighContrastTextManager(ViewRootImpl this$0) {
            this.this$0 = this$0;
            this$0.mAttachInfo.mHighContrastText = this$0.mAccessibilityManager.isHighTextContrastEnabled();
        }

        public void onHighTextContrastStateChanged(boolean enabled) {
            this.this$0.mAttachInfo.mHighContrastText = enabled;
            this.this$0.destroyHardwareResources();
            this.this$0.invalidate();
        }
    }

    final class ImeInputStage extends AsyncInputStage implements FinishedInputEventCallback {
        final /* synthetic */ ViewRootImpl this$0;

        public ImeInputStage(ViewRootImpl this$0, InputStage next, String traceCounter) {
            this.this$0 = this$0;
            super(this$0, next, traceCounter);
        }

        protected int onProcess(QueuedInputEvent q) {
            if (this.this$0.mLastWasImTarget && !this.this$0.isInLocalFocusMode()) {
                InputMethodManager imm = InputMethodManager.peekInstance();
                if (imm != null) {
                    this.this$0.mKeyEventStatus = ViewRootImpl.INPUT_DISPATCH_STATE_IME_STAGE;
                    InputEvent event = q.mEvent;
                    if (ViewRootImpl.DEBUG_IMF || ViewRootImpl.DEBUG_INPUT || ViewRootImpl.DEBUG_KEY) {
                        Log.v(this.this$0.mTag, "Sending input event to IME: " + event + ", this = " + this);
                    }
                    int result = imm.dispatchInputEvent(event, q, this, this.this$0.mHandler);
                    if (result == 1) {
                        return 1;
                    }
                    if (result == 0) {
                        return 0;
                    }
                    return 3;
                }
            }
            return 0;
        }

        public void onFinishedInputEvent(Object token, boolean handled) {
            QueuedInputEvent q = (QueuedInputEvent) token;
            if (ViewRootImpl.DEBUG_IMF || ViewRootImpl.DEBUG_INPUT || ViewRootImpl.DEBUG_KEY) {
                Log.d(this.this$0.mTag, "IME finishedEvent: handled = " + handled + ", event = " + q + ", viewAncestor = " + this);
            }
            if (handled) {
                finish(q, true);
            } else {
                forward(q);
            }
        }
    }

    final class InvalidateOnAnimationRunnable implements Runnable {
        private boolean mPosted;
        private InvalidateInfo[] mTempViewRects;
        private View[] mTempViews;
        private final ArrayList<InvalidateInfo> mViewRects;
        private final ArrayList<View> mViews;
        final /* synthetic */ ViewRootImpl this$0;

        InvalidateOnAnimationRunnable(ViewRootImpl this$0) {
            this.this$0 = this$0;
            this.mViews = new ArrayList();
            this.mViewRects = new ArrayList();
        }

        public void addView(View view) {
            synchronized (this) {
                this.mViews.add(view);
                postIfNeededLocked();
            }
        }

        public void addViewRect(InvalidateInfo info) {
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
                    InvalidateInfo info = (InvalidateInfo) this.mViewRects.get(i2);
                    if (info.target == view) {
                        this.mViewRects.remove(i2);
                        info.recycle();
                    }
                    i = i2;
                }
                if (this.mPosted && this.mViews.isEmpty() && this.mViewRects.isEmpty()) {
                    this.this$0.mChoreographer.removeCallbacks(1, this, null);
                    this.mPosted = false;
                }
            }
        }

        public void run() {
            int viewCount;
            int viewRectCount;
            int i;
            synchronized (this) {
                this.mPosted = false;
                viewCount = this.mViews.size();
                if (viewCount != 0) {
                    this.mTempViews = (View[]) this.mViews.toArray(this.mTempViews != null ? this.mTempViews : new View[viewCount]);
                    this.mViews.clear();
                }
                viewRectCount = this.mViewRects.size();
                if (viewRectCount != 0) {
                    this.mTempViewRects = (InvalidateInfo[]) this.mViewRects.toArray(this.mTempViewRects != null ? this.mTempViewRects : new InvalidateInfo[viewRectCount]);
                    this.mViewRects.clear();
                }
            }
            for (i = 0; i < viewCount; i++) {
                this.mTempViews[i].invalidate();
                this.mTempViews[i] = null;
            }
            for (i = 0; i < viewRectCount; i++) {
                InvalidateInfo info = this.mTempViewRects[i];
                info.target.invalidate(info.left, info.top, info.right, info.bottom);
                info.recycle();
            }
        }

        private void postIfNeededLocked() {
            if (!this.mPosted) {
                this.this$0.mChoreographer.postCallback(1, this, null);
                this.mPosted = true;
            }
        }
    }

    final class NativePostImeInputStage extends AsyncInputStage implements InputQueue.FinishedInputEventCallback {
        final /* synthetic */ ViewRootImpl this$0;

        public NativePostImeInputStage(ViewRootImpl this$0, InputStage next, String traceCounter) {
            this.this$0 = this$0;
            super(this$0, next, traceCounter);
        }

        protected int onProcess(QueuedInputEvent q) {
            if (this.this$0.mInputQueue == null) {
                return 0;
            }
            if (q.mEvent instanceof KeyEvent) {
                this.this$0.mKeyEventStatus = ViewRootImpl.INPUT_DISPATCH_STATE_NATIVE_POST_IME_STAGE;
            } else {
                this.this$0.mMotionEventStatus = ViewRootImpl.INPUT_DISPATCH_STATE_NATIVE_POST_IME_STAGE;
            }
            this.this$0.mInputQueue.sendInputEvent(q.mEvent, q, false, this);
            return 3;
        }

        public void onFinishedInputEvent(Object token, boolean handled) {
            QueuedInputEvent q = (QueuedInputEvent) token;
            if (handled) {
                finish(q, true);
            } else {
                forward(q);
            }
        }
    }

    final class NativePreImeInputStage extends AsyncInputStage implements InputQueue.FinishedInputEventCallback {
        final /* synthetic */ ViewRootImpl this$0;

        public NativePreImeInputStage(ViewRootImpl this$0, InputStage next, String traceCounter) {
            this.this$0 = this$0;
            super(this$0, next, traceCounter);
        }

        protected int onProcess(QueuedInputEvent q) {
            if (this.this$0.mInputQueue == null || !(q.mEvent instanceof KeyEvent)) {
                return 0;
            }
            this.this$0.mKeyEventStatus = ViewRootImpl.INPUT_DISPATCH_STATE_NATIVE_PRE_IME_STAGE;
            this.this$0.mInputQueue.sendInputEvent(q.mEvent, q, true, this);
            return 3;
        }

        public void onFinishedInputEvent(Object token, boolean handled) {
            QueuedInputEvent q = (QueuedInputEvent) token;
            if (handled) {
                finish(q, true);
            } else {
                forward(q);
            }
        }
    }

    private static final class QueuedInputEvent {
        public static final int FLAG_DEFERRED = 2;
        public static final int FLAG_DELIVER_POST_IME = 1;
        public static final int FLAG_FINISHED = 4;
        public static final int FLAG_FINISHED_HANDLED = 8;
        public static final int FLAG_RESYNTHESIZED = 16;
        public static final int FLAG_UNHANDLED = 32;
        public InputEvent mEvent;
        public int mFlags;
        public QueuedInputEvent mNext;
        public InputEventReceiver mReceiver;

        /* synthetic */ QueuedInputEvent(QueuedInputEvent queuedInputEvent) {
            this();
        }

        private QueuedInputEvent() {
        }

        public boolean shouldSkipIme() {
            boolean z = false;
            if ((this.mFlags & 1) != 0) {
                return true;
            }
            if (this.mEvent instanceof MotionEvent) {
                z = this.mEvent.isFromSource(2);
            }
            return z;
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
                sb.append("0");
            }
            sb.append(", hasNextQueuedEvent=").append(this.mEvent != null ? "true" : "false");
            sb.append(", hasInputEventReceiver=").append(this.mReceiver != null ? "true" : "false");
            sb.append(", mEvent=").append(this.mEvent).append("}");
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

    private class SendWindowContentChangedAccessibilityEvent implements Runnable {
        private int mChangeTypes;
        public long mLastEventTimeMillis;
        public View mSource;
        final /* synthetic */ ViewRootImpl this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: android.view.ViewRootImpl.SendWindowContentChangedAccessibilityEvent.<init>(android.view.ViewRootImpl):void, dex:  in method: android.view.ViewRootImpl.SendWindowContentChangedAccessibilityEvent.<init>(android.view.ViewRootImpl):void, dex: 
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
            Caused by: jadx.core.utils.exceptions.DecodeException: null in method: android.view.ViewRootImpl.SendWindowContentChangedAccessibilityEvent.<init>(android.view.ViewRootImpl):void, dex: 
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
            	... 10 more
            Caused by: java.io.EOFException
            	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
            	at com.android.dx.io.instructions.ShortArrayCodeInput.readLong(ShortArrayCodeInput.java:71)
            	at com.android.dx.io.instructions.InstructionCodec$31.decode(InstructionCodec.java:652)
            	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
            	... 11 more
            */
        private SendWindowContentChangedAccessibilityEvent(android.view.ViewRootImpl r1) {
            /*
            // Can't load method instructions: Load method exception: null in method: android.view.ViewRootImpl.SendWindowContentChangedAccessibilityEvent.<init>(android.view.ViewRootImpl):void, dex:  in method: android.view.ViewRootImpl.SendWindowContentChangedAccessibilityEvent.<init>(android.view.ViewRootImpl):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.view.ViewRootImpl.SendWindowContentChangedAccessibilityEvent.<init>(android.view.ViewRootImpl):void");
        }

        /* synthetic */ SendWindowContentChangedAccessibilityEvent(ViewRootImpl this$0, SendWindowContentChangedAccessibilityEvent sendWindowContentChangedAccessibilityEvent) {
            this(this$0);
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.view.ViewRootImpl.SendWindowContentChangedAccessibilityEvent.run():void, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public void run() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.view.ViewRootImpl.SendWindowContentChangedAccessibilityEvent.run():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.view.ViewRootImpl.SendWindowContentChangedAccessibilityEvent.run():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.view.ViewRootImpl.SendWindowContentChangedAccessibilityEvent.runOrPost(android.view.View, int):void, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public void runOrPost(android.view.View r1, int r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.view.ViewRootImpl.SendWindowContentChangedAccessibilityEvent.runOrPost(android.view.View, int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.view.ViewRootImpl.SendWindowContentChangedAccessibilityEvent.runOrPost(android.view.View, int):void");
        }
    }

    final class SyntheticInputStage extends InputStage {
        private final SyntheticJoystickHandler mJoystick;
        private final SyntheticKeyboardHandler mKeyboard;
        private final SyntheticTouchNavigationHandler mTouchNavigation;
        private final SyntheticTrackballHandler mTrackball;
        final /* synthetic */ ViewRootImpl this$0;

        public SyntheticInputStage(ViewRootImpl this$0) {
            this.this$0 = this$0;
            super(this$0, null);
            this.mTrackball = new SyntheticTrackballHandler(this.this$0);
            this.mJoystick = new SyntheticJoystickHandler(this.this$0);
            this.mTouchNavigation = new SyntheticTouchNavigationHandler(this.this$0);
            this.mKeyboard = new SyntheticKeyboardHandler(this.this$0);
        }

        protected int onProcess(QueuedInputEvent q) {
            q.mFlags |= 16;
            if (q.mEvent instanceof MotionEvent) {
                this.this$0.mMotionEventStatus = ViewRootImpl.INPUT_DISPATCH_STATE_SYNTHETC_INPUT_STAGE;
                MotionEvent event = q.mEvent;
                int source = event.getSource();
                if ((source & 4) != 0) {
                    this.mTrackball.process(event);
                    return 1;
                } else if ((source & 16) != 0) {
                    this.mJoystick.process(event);
                    return 1;
                } else if ((source & 2097152) == 2097152) {
                    this.mTouchNavigation.process(event);
                    return 1;
                }
            } else if ((q.mFlags & 32) != 0) {
                this.mKeyboard.process((KeyEvent) q.mEvent);
                return 1;
            }
            return 0;
        }

        protected void onDeliverToNext(QueuedInputEvent q) {
            if ((q.mFlags & 16) == 0 && (q.mEvent instanceof MotionEvent)) {
                MotionEvent event = q.mEvent;
                int source = event.getSource();
                if ((source & 4) != 0) {
                    this.mTrackball.cancel(event);
                } else if ((source & 16) != 0) {
                    this.mJoystick.cancel(event);
                } else if ((source & 2097152) == 2097152) {
                    this.mTouchNavigation.cancel(event);
                }
            }
            super.onDeliverToNext(q);
        }
    }

    final class SyntheticJoystickHandler extends Handler {
        private static final int MSG_ENQUEUE_X_AXIS_KEY_REPEAT = 1;
        private static final int MSG_ENQUEUE_Y_AXIS_KEY_REPEAT = 2;
        private static final String TAG = "SyntheticJoystickHandler";
        private int mLastXDirection;
        private int mLastXKeyCode;
        private int mLastYDirection;
        private int mLastYKeyCode;
        final /* synthetic */ ViewRootImpl this$0;

        public SyntheticJoystickHandler(ViewRootImpl this$0) {
            this.this$0 = this$0;
            super(true);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                case 2:
                    KeyEvent oldEvent = msg.obj;
                    KeyEvent e = KeyEvent.changeTimeRepeat(oldEvent, SystemClock.uptimeMillis(), oldEvent.getRepeatCount() + 1);
                    if (this.this$0.mAttachInfo.mHasWindowFocus) {
                        this.this$0.enqueueInputEvent(e);
                        Message m = obtainMessage(msg.what, e);
                        m.setAsynchronous(true);
                        sendMessageDelayed(m, (long) ViewConfiguration.getKeyRepeatDelay());
                        return;
                    }
                    return;
                default:
                    return;
            }
        }

        public void process(MotionEvent event) {
            switch (event.getActionMasked()) {
                case 2:
                    update(event, true);
                    return;
                case 3:
                    cancel(event);
                    return;
                default:
                    Log.w(this.this$0.mTag, "Unexpected action: " + event.getActionMasked());
                    return;
            }
        }

        private void cancel(MotionEvent event) {
            removeMessages(1);
            removeMessages(2);
            update(event, false);
        }

        private void update(MotionEvent event, boolean synthesizeNewKeys) {
            KeyEvent e;
            Message m;
            long time = event.getEventTime();
            int metaState = event.getMetaState();
            int deviceId = event.getDeviceId();
            int source = event.getSource();
            int xDirection = joystickAxisValueToDirection(event.getAxisValue(15));
            if (xDirection == 0) {
                xDirection = joystickAxisValueToDirection(event.getX());
            }
            int yDirection = joystickAxisValueToDirection(event.getAxisValue(16));
            if (yDirection == 0) {
                yDirection = joystickAxisValueToDirection(event.getY());
            }
            if (xDirection != this.mLastXDirection) {
                if (this.mLastXKeyCode != 0) {
                    removeMessages(1);
                    this.this$0.enqueueInputEvent(new KeyEvent(time, time, 1, this.mLastXKeyCode, 0, metaState, deviceId, 0, 1024, source));
                    this.mLastXKeyCode = 0;
                }
                this.mLastXDirection = xDirection;
                if (xDirection != 0 && synthesizeNewKeys) {
                    this.mLastXKeyCode = xDirection > 0 ? 22 : 21;
                    e = new KeyEvent(time, time, 0, this.mLastXKeyCode, 0, metaState, deviceId, 0, 1024, source);
                    this.this$0.enqueueInputEvent(e);
                    m = obtainMessage(1, e);
                    m.setAsynchronous(true);
                    sendMessageDelayed(m, (long) ViewConfiguration.getKeyRepeatTimeout());
                }
            }
            if (yDirection != this.mLastYDirection) {
                if (this.mLastYKeyCode != 0) {
                    removeMessages(2);
                    this.this$0.enqueueInputEvent(new KeyEvent(time, time, 1, this.mLastYKeyCode, 0, metaState, deviceId, 0, 1024, source));
                    this.mLastYKeyCode = 0;
                }
                this.mLastYDirection = yDirection;
                if (yDirection != 0 && synthesizeNewKeys) {
                    this.mLastYKeyCode = yDirection > 0 ? 20 : 19;
                    e = new KeyEvent(time, time, 0, this.mLastYKeyCode, 0, metaState, deviceId, 0, 1024, source);
                    this.this$0.enqueueInputEvent(e);
                    m = obtainMessage(2, e);
                    m.setAsynchronous(true);
                    sendMessageDelayed(m, (long) ViewConfiguration.getKeyRepeatTimeout());
                }
            }
        }

        private int joystickAxisValueToDirection(float value) {
            if (value >= 0.5f) {
                return 1;
            }
            if (value <= -0.5f) {
                return -1;
            }
            return 0;
        }
    }

    final class SyntheticKeyboardHandler {
        final /* synthetic */ ViewRootImpl this$0;

        SyntheticKeyboardHandler(ViewRootImpl this$0) {
            this.this$0 = this$0;
        }

        public void process(KeyEvent event) {
            if ((event.getFlags() & 1024) == 0) {
                FallbackAction fallbackAction = event.getKeyCharacterMap().getFallbackAction(event.getKeyCode(), event.getMetaState());
                if (fallbackAction != null) {
                    InputEvent fallbackEvent = KeyEvent.obtain(event.getDownTime(), event.getEventTime(), event.getAction(), fallbackAction.keyCode, event.getRepeatCount(), fallbackAction.metaState, event.getDeviceId(), event.getScanCode(), event.getFlags() | 1024, event.getSource(), null);
                    fallbackAction.recycle();
                    this.this$0.enqueueInputEvent(fallbackEvent);
                }
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
        private int mActivePointerId;
        private float mConfigMaxFlingVelocity;
        private float mConfigMinFlingVelocity;
        private float mConfigTickDistance;
        private boolean mConsumedMovement;
        private int mCurrentDeviceId;
        private boolean mCurrentDeviceSupported;
        private int mCurrentSource;
        private final Runnable mFlingRunnable;
        private float mFlingVelocity;
        private boolean mFlinging;
        private float mLastX;
        private float mLastY;
        private int mPendingKeyCode;
        private long mPendingKeyDownTime;
        private int mPendingKeyMetaState;
        private int mPendingKeyRepeatCount;
        private float mStartX;
        private float mStartY;
        private VelocityTracker mVelocityTracker;
        final /* synthetic */ ViewRootImpl this$0;

        /* renamed from: android.view.ViewRootImpl$SyntheticTouchNavigationHandler$1 */
        class AnonymousClass1 implements Runnable {
            final /* synthetic */ SyntheticTouchNavigationHandler this$1;

            AnonymousClass1(SyntheticTouchNavigationHandler this$1) {
                this.this$1 = this$1;
            }

            public void run() {
                long time = SystemClock.uptimeMillis();
                this.this$1.sendKeyDownOrRepeat(time, this.this$1.mPendingKeyCode, this.this$1.mPendingKeyMetaState);
                SyntheticTouchNavigationHandler syntheticTouchNavigationHandler = this.this$1;
                syntheticTouchNavigationHandler.mFlingVelocity = syntheticTouchNavigationHandler.mFlingVelocity * SyntheticTouchNavigationHandler.FLING_TICK_DECAY;
                if (!this.this$1.postFling(time)) {
                    this.this$1.mFlinging = false;
                    this.this$1.finishKeys(time);
                }
            }
        }

        public SyntheticTouchNavigationHandler(ViewRootImpl this$0) {
            this.this$0 = this$0;
            super(true);
            this.mCurrentDeviceId = -1;
            this.mActivePointerId = -1;
            this.mPendingKeyCode = 0;
            this.mFlingRunnable = new AnonymousClass1(this);
        }

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
                    MotionRange xRange = device.getMotionRange(0);
                    MotionRange yRange = device.getMotionRange(1);
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
                        this.mConfigTickDistance = 12.0f * ((xRes + yRes) * 0.5f);
                        this.mConfigMinFlingVelocity = this.mConfigTickDistance * MIN_FLING_VELOCITY_TICKS_PER_SECOND;
                        this.mConfigMaxFlingVelocity = this.mConfigTickDistance * MAX_FLING_VELOCITY_TICKS_PER_SECOND;
                    }
                }
            }
            if (this.mCurrentDeviceSupported) {
                int action = event.getActionMasked();
                switch (action) {
                    case 0:
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
                        break;
                    case 1:
                    case 2:
                        if (this.mActivePointerId >= 0) {
                            int index = event.findPointerIndex(this.mActivePointerId);
                            if (index >= 0) {
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
                                    break;
                                }
                            }
                            finishKeys(time);
                            finishTracking(time);
                            break;
                        }
                        break;
                    case 3:
                        finishKeys(time);
                        finishTracking(time);
                        break;
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
            this.this$0.enqueueInputEvent(new KeyEvent(this.mPendingKeyDownTime, time, 0, this.mPendingKeyCode, this.mPendingKeyRepeatCount, this.mPendingKeyMetaState, this.mCurrentDeviceId, 1024, this.mCurrentSource));
        }

        private void sendKeyUp(long time) {
            if (this.mPendingKeyCode != 0) {
                this.this$0.enqueueInputEvent(new KeyEvent(this.mPendingKeyDownTime, time, 1, this.mPendingKeyCode, 0, this.mPendingKeyMetaState, this.mCurrentDeviceId, 0, 1024, this.mCurrentSource));
                this.mPendingKeyCode = 0;
            }
        }

        private boolean startFling(long time, float vx, float vy) {
            switch (this.mPendingKeyCode) {
                case 19:
                    if ((-vy) >= this.mConfigMinFlingVelocity && Math.abs(vx) < this.mConfigMinFlingVelocity) {
                        this.mFlingVelocity = -vy;
                        break;
                    }
                    return false;
                case 20:
                    if (vy >= this.mConfigMinFlingVelocity && Math.abs(vx) < this.mConfigMinFlingVelocity) {
                        this.mFlingVelocity = vy;
                        break;
                    }
                    return false;
                    break;
                case 21:
                    if ((-vx) >= this.mConfigMinFlingVelocity && Math.abs(vy) < this.mConfigMinFlingVelocity) {
                        this.mFlingVelocity = -vx;
                        break;
                    }
                    return false;
                case 22:
                    if (vx >= this.mConfigMinFlingVelocity && Math.abs(vy) < this.mConfigMinFlingVelocity) {
                        this.mFlingVelocity = vx;
                        break;
                    }
                    return false;
            }
            this.mFlinging = postFling(time);
            return this.mFlinging;
        }

        private boolean postFling(long time) {
            if (this.mFlingVelocity < this.mConfigMinFlingVelocity) {
                return false;
            }
            postAtTime(this.mFlingRunnable, time + ((long) ((this.mConfigTickDistance / this.mFlingVelocity) * 1000.0f)));
            return true;
        }

        private void cancelFling() {
            if (this.mFlinging) {
                removeCallbacks(this.mFlingRunnable);
                this.mFlinging = false;
            }
        }
    }

    final class SyntheticTrackballHandler {
        private long mLastTime;
        private final TrackballAxis mX;
        private final TrackballAxis mY;
        final /* synthetic */ ViewRootImpl this$0;

        SyntheticTrackballHandler(ViewRootImpl this$0) {
            this.this$0 = this$0;
            this.mX = new TrackballAxis();
            this.mY = new TrackballAxis();
        }

        public void process(MotionEvent event) {
            long curTime = SystemClock.uptimeMillis();
            if (this.mLastTime + 250 < curTime) {
                this.mX.reset(0);
                this.mY.reset(0);
                this.mLastTime = curTime;
            }
            int action = event.getAction();
            int metaState = event.getMetaState();
            switch (action) {
                case 0:
                    this.mX.reset(2);
                    this.mY.reset(2);
                    this.this$0.enqueueInputEvent(new KeyEvent(curTime, curTime, 0, 23, 0, metaState, -1, 0, 1024, 257));
                    break;
                case 1:
                    this.mX.reset(2);
                    this.mY.reset(2);
                    this.this$0.enqueueInputEvent(new KeyEvent(curTime, curTime, 1, 23, 0, metaState, -1, 0, 1024, 257));
                    break;
            }
            if (ViewRootImpl.DEBUG_TRACKBALL) {
                Log.v(this.this$0.mTag, "TB X=" + this.mX.position + " step=" + this.mX.step + " dir=" + this.mX.dir + " acc=" + this.mX.acceleration + " move=" + event.getX() + " / Y=" + this.mY.position + " step=" + this.mY.step + " dir=" + this.mY.dir + " acc=" + this.mY.acceleration + " move=" + event.getY());
            }
            float xOff = this.mX.collect(event.getX(), event.getEventTime(), "X");
            float yOff = this.mY.collect(event.getY(), event.getEventTime(), "Y");
            int keycode = 0;
            int movement = 0;
            float accel = 1.0f;
            if (xOff > yOff) {
                movement = this.mX.generate();
                if (movement != 0) {
                    if (movement > 0) {
                        keycode = 22;
                    } else {
                        keycode = 21;
                    }
                    accel = this.mX.acceleration;
                    this.mY.reset(2);
                }
            } else if (yOff > 0.0f) {
                movement = this.mY.generate();
                if (movement != 0) {
                    if (movement > 0) {
                        keycode = 20;
                    } else {
                        keycode = 19;
                    }
                    accel = this.mY.acceleration;
                    this.mX.reset(2);
                }
            }
            if (keycode != 0) {
                if (movement < 0) {
                    movement = -movement;
                }
                int accelMovement = (int) (((float) movement) * accel);
                if (ViewRootImpl.DEBUG_TRACKBALL) {
                    Log.v(this.this$0.mTag, "Move: movement=" + movement + " accelMovement=" + accelMovement + " accel=" + accel);
                }
                if (accelMovement > movement) {
                    if (ViewRootImpl.DEBUG_TRACKBALL) {
                        Log.v(this.this$0.mTag, "Delivering fake DPAD: " + keycode);
                    }
                    movement--;
                    int repeatCount = accelMovement - movement;
                    this.this$0.enqueueInputEvent(new KeyEvent(curTime, curTime, 2, keycode, repeatCount, metaState, -1, 0, 1024, 257));
                }
                while (movement > 0) {
                    if (ViewRootImpl.DEBUG_TRACKBALL) {
                        Log.v(this.this$0.mTag, "Delivering fake DPAD: " + keycode);
                    }
                    movement--;
                    curTime = SystemClock.uptimeMillis();
                    this.this$0.enqueueInputEvent(new KeyEvent(curTime, curTime, 0, keycode, 0, metaState, -1, 0, 1024, 257));
                    this.this$0.enqueueInputEvent(new KeyEvent(curTime, curTime, 1, keycode, 0, metaState, -1, 0, 1024, 257));
                }
                this.mLastTime = curTime;
            }
        }

        public void cancel(MotionEvent event) {
            this.mLastTime = -2147483648L;
            if (this.this$0.mView != null && this.this$0.mAdded) {
                this.this$0.ensureTouchMode(false);
            }
        }
    }

    static final class SystemUiVisibilityInfo {
        int globalVisibility;
        int localChanges;
        int localValue;
        int seq;

        SystemUiVisibilityInfo() {
        }
    }

    class TakenSurfaceHolder extends BaseSurfaceHolder {
        final /* synthetic */ ViewRootImpl this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.view.ViewRootImpl.TakenSurfaceHolder.<init>(android.view.ViewRootImpl):void, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        TakenSurfaceHolder(android.view.ViewRootImpl r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.view.ViewRootImpl.TakenSurfaceHolder.<init>(android.view.ViewRootImpl):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.view.ViewRootImpl.TakenSurfaceHolder.<init>(android.view.ViewRootImpl):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.view.ViewRootImpl.TakenSurfaceHolder.isCreating():boolean, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public boolean isCreating() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.view.ViewRootImpl.TakenSurfaceHolder.isCreating():boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.view.ViewRootImpl.TakenSurfaceHolder.isCreating():boolean");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.view.ViewRootImpl.TakenSurfaceHolder.onAllowLockCanvas():boolean, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public boolean onAllowLockCanvas() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.view.ViewRootImpl.TakenSurfaceHolder.onAllowLockCanvas():boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.view.ViewRootImpl.TakenSurfaceHolder.onAllowLockCanvas():boolean");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.view.ViewRootImpl.TakenSurfaceHolder.onRelayoutContainer():void, dex: 
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
        public void onRelayoutContainer() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.view.ViewRootImpl.TakenSurfaceHolder.onRelayoutContainer():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.view.ViewRootImpl.TakenSurfaceHolder.onRelayoutContainer():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.view.ViewRootImpl.TakenSurfaceHolder.setFormat(int):void, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public void setFormat(int r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.view.ViewRootImpl.TakenSurfaceHolder.setFormat(int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.view.ViewRootImpl.TakenSurfaceHolder.setFormat(int):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.view.ViewRootImpl.TakenSurfaceHolder.setKeepScreenOn(boolean):void, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public void setKeepScreenOn(boolean r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.view.ViewRootImpl.TakenSurfaceHolder.setKeepScreenOn(boolean):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.view.ViewRootImpl.TakenSurfaceHolder.setKeepScreenOn(boolean):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.view.ViewRootImpl.TakenSurfaceHolder.setType(int):void, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public void setType(int r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.view.ViewRootImpl.TakenSurfaceHolder.setType(int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.view.ViewRootImpl.TakenSurfaceHolder.setType(int):void");
        }

        public void onUpdateSurface() {
            throw new IllegalStateException("Shouldn't be here");
        }

        public void setFixedSize(int width, int height) {
            throw new UnsupportedOperationException("Currently only support sizing from layout");
        }
    }

    static final class TrackballAxis {
        static final float ACCEL_MOVE_SCALING_FACTOR = 0.025f;
        static final long FAST_MOVE_TIME = 150;
        static final float FIRST_MOVEMENT_THRESHOLD = 0.5f;
        static final float MAX_ACCELERATION = 20.0f;
        static final float SECOND_CUMULATIVE_MOVEMENT_THRESHOLD = 2.0f;
        static final float SUBSEQUENT_INCREMENTAL_MOVEMENT_THRESHOLD = 1.0f;
        float acceleration;
        int dir;
        long lastMoveTime;
        int nonAccelMovement;
        float position;
        int step;

        TrackballAxis() {
            this.acceleration = 1.0f;
            this.lastMoveTime = 0;
        }

        void reset(int _step) {
            this.position = 0.0f;
            this.acceleration = 1.0f;
            this.lastMoveTime = 0;
            this.step = _step;
            this.dir = 0;
        }

        float collect(float off, long time, String axis) {
            long normTime;
            if (off > 0.0f) {
                normTime = (long) (150.0f * off);
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
                float scale;
                if (delta < normTime) {
                    scale = ((float) (normTime - delta)) * ACCEL_MOVE_SCALING_FACTOR;
                    if (scale > 1.0f) {
                        acc *= scale;
                    }
                    if (ViewRootImpl.DEBUG_TRACKBALL) {
                        Log.v(ViewRootImpl.TAG, axis + " accelerate: off=" + off + " normTime=" + normTime + " delta=" + delta + " scale=" + scale + " acc=" + acc);
                    }
                    if (acc >= MAX_ACCELERATION) {
                        acc = MAX_ACCELERATION;
                    }
                    this.acceleration = acc;
                } else {
                    scale = ((float) (delta - normTime)) * ACCEL_MOVE_SCALING_FACTOR;
                    if (scale > 1.0f) {
                        acc /= scale;
                    }
                    if (ViewRootImpl.DEBUG_TRACKBALL) {
                        Log.v(ViewRootImpl.TAG, axis + " deccelerate: off=" + off + " normTime=" + normTime + " delta=" + delta + " scale=" + scale + " acc=" + acc);
                    }
                    if (acc <= 1.0f) {
                        acc = 1.0f;
                    }
                    this.acceleration = acc;
                }
            }
            this.position += off;
            return Math.abs(this.position);
        }

        int generate() {
            int movement = 0;
            this.nonAccelMovement = 0;
            while (true) {
                int dir = this.position >= 0.0f ? 1 : -1;
                switch (this.step) {
                    case 0:
                        if (Math.abs(this.position) >= FIRST_MOVEMENT_THRESHOLD) {
                            movement += dir;
                            this.nonAccelMovement += dir;
                            this.step = 1;
                            break;
                        }
                        return movement;
                    case 1:
                        if (Math.abs(this.position) >= SECOND_CUMULATIVE_MOVEMENT_THRESHOLD) {
                            movement += dir;
                            this.nonAccelMovement += dir;
                            this.position -= ((float) dir) * SECOND_CUMULATIVE_MOVEMENT_THRESHOLD;
                            this.step = 2;
                            break;
                        }
                        return movement;
                    default:
                        if (Math.abs(this.position) >= 1.0f) {
                            movement += dir;
                            this.position -= ((float) dir) * 1.0f;
                            float acc = this.acceleration * 1.1f;
                            if (acc >= MAX_ACCELERATION) {
                                acc = this.acceleration;
                            }
                            this.acceleration = acc;
                            break;
                        }
                        return movement;
                }
            }
        }
    }

    final class TraversalRunnable implements Runnable {
        final /* synthetic */ ViewRootImpl this$0;

        TraversalRunnable(ViewRootImpl this$0) {
            this.this$0 = this$0;
        }

        public void run() {
            this.this$0.doTraversal();
        }
    }

    final class ViewPostImeInputStage extends InputStage {
        private int mPointerEventNum;
        private boolean startOppoScreenShot;
        final /* synthetic */ ViewRootImpl this$0;

        public ViewPostImeInputStage(ViewRootImpl this$0, InputStage next) {
            this.this$0 = this$0;
            super(this$0, next);
            this.startOppoScreenShot = false;
            this.mPointerEventNum = 0;
        }

        protected int onProcess(QueuedInputEvent q) {
            if (q.mEvent instanceof KeyEvent) {
                this.this$0.mKeyEventStatus = ViewRootImpl.INPUT_DISPATCH_STATE_VIEW_POST_IME_STAGE;
                return processKeyEvent(q);
            }
            this.this$0.mMotionEventStatus = ViewRootImpl.INPUT_DISPATCH_STATE_VIEW_POST_IME_STAGE;
            int source = q.mEvent.getSource();
            if ((source & 2) != 0) {
                return processPointerEvent(q);
            }
            if ((source & 4) != 0) {
                return processTrackballEvent(q);
            }
            return processGenericMotionEvent(q);
        }

        protected void onDeliverToNext(QueuedInputEvent q) {
            if (this.this$0.mUnbufferedInputDispatch && (q.mEvent instanceof MotionEvent) && ((MotionEvent) q.mEvent).isTouchEvent() && ViewRootImpl.isTerminalInputEvent(q.mEvent)) {
                this.this$0.mUnbufferedInputDispatch = false;
                this.this$0.scheduleConsumeBatchedInput();
            }
            super.onDeliverToNext(q);
        }

        private int processKeyEvent(QueuedInputEvent q) {
            KeyEvent event = q.mEvent;
            if (ViewRootImpl.DEBUG_KEY || ViewRootImpl.DEBUG_INPUT) {
                Log.v(this.this$0.mTag, "deliverKeyEventPostIme: event = " + event + ", mView = " + this.this$0.mView + ", mAdded = " + this.this$0.mAdded + ", this = " + this);
            }
            if (InputLog.DEBUG) {
                InputLog.d(this.this$0.mTag, " processKeyEvent, " + event);
            }
            if (this.this$0.mView.dispatchKeyEvent(event)) {
                if (ViewRootImpl.DEBUG_DEFAULT) {
                    Log.v(this.this$0.mTag, "App handle key event: event = " + event + ", mView = " + this.this$0.mView + ", this = " + this);
                }
                if (InputLog.DEBUG) {
                    InputLog.d(this.this$0.mTag, event.getKeyCode() + " was FINISH_HANDLED by  mView ");
                }
                return 1;
            } else if (shouldDropInputEvent(q)) {
                return 2;
            } else {
                if (event.getAction() == 0 && event.isCtrlPressed() && event.getRepeatCount() == 0 && !KeyEvent.isModifierKey(event.getKeyCode())) {
                    if (this.this$0.mView.dispatchKeyShortcutEvent(event)) {
                        return 1;
                    }
                    if (shouldDropInputEvent(q)) {
                        return 2;
                    }
                }
                if (this.this$0.mFallbackEventHandler.dispatchKeyEvent(event)) {
                    return 1;
                }
                if (shouldDropInputEvent(q)) {
                    return 2;
                }
                if (event.getAction() == 0) {
                    int direction = 0;
                    switch (event.getKeyCode()) {
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
                        case 61:
                            if (!event.hasNoModifiers()) {
                                if (event.hasModifiers(1)) {
                                    direction = 1;
                                    break;
                                }
                            }
                            direction = 2;
                            break;
                            break;
                    }
                    if (direction != 0) {
                        View focused = this.this$0.mView.findFocus();
                        View v;
                        if (focused != null) {
                            v = focused.focusSearch(direction);
                            if (!(v == null || v == focused)) {
                                focused.getFocusedRect(this.this$0.mTempRect);
                                if (this.this$0.mView instanceof ViewGroup) {
                                    ((ViewGroup) this.this$0.mView).offsetDescendantRectToMyCoords(focused, this.this$0.mTempRect);
                                    ((ViewGroup) this.this$0.mView).offsetRectIntoDescendantCoords(v, this.this$0.mTempRect);
                                }
                                if (v.requestFocus(direction, this.this$0.mTempRect)) {
                                    this.this$0.playSoundEffect(SoundEffectConstants.getContantForFocusDirection(direction));
                                    return 1;
                                }
                            }
                            if (this.this$0.mView.dispatchUnhandledMove(focused, direction)) {
                                return 1;
                            }
                        }
                        v = this.this$0.focusSearch(null, direction);
                        return (v == null || !v.requestFocus(direction)) ? 0 : 1;
                    }
                }
            }
        }

        @OppoHook(level = OppoHookType.CHANGE_CODE, note = "Jun.Zhang@Plf.Framework,2013.02.19:add for three pointers move shot screen", property = OppoRomType.ROM)
        private int processPointerEvent(QueuedInputEvent q) {
            int i = 0;
            MotionEvent event = q.mEvent;
            this.mPointerEventNum++;
            if (InputLog.DEBUG) {
                InputLog.d(ViewRootImpl.TAG, " processPointerEvent  event = " + event);
            } else if (ViewRootImpl.DEBUG_PANIC && this.mPointerEventNum > 50) {
                Log.d(ViewRootImpl.TAG, " processPointerEvent  event = " + event);
                this.mPointerEventNum = 0;
            }
            boolean tmpOppoScreenShot = OppoScreenShotUtil.checkPauseDeliverPointer();
            if (!this.startOppoScreenShot && tmpOppoScreenShot) {
                this.this$0.mHandler.removeCallbacks(this.this$0.mProcessCpuTrackerRunnable);
                this.this$0.mHandler.postDelayed(this.this$0.mProcessCpuTrackerRunnable, 2000);
            }
            this.startOppoScreenShot = tmpOppoScreenShot;
            if (tmpOppoScreenShot) {
                OppoScreenShotUtil.dealPausedDeliverPointer((MotionEvent) q.mEvent, this.this$0.mView);
                return 1;
            }
            this.this$0.mAttachInfo.mUnbufferedDispatchRequested = false;
            View eventTarget = (!event.isFromSource(InputDevice.SOURCE_MOUSE) || this.this$0.mCapturingView == null) ? this.this$0.mView : this.this$0.mCapturingView;
            this.this$0.mAttachInfo.mHandlingPointerEvent = true;
            if (InputLog.DEBUG) {
                InputLog.d(ViewRootImpl.TAG, " dispatchPointerEvent to mView");
            }
            MotionEvent eventWrap = ColorAccidentallyTouchUtils.getInstance().updatePointerEvent(event, this.this$0.mView, this.this$0.mLastConfiguration);
            if (eventWrap == null) {
                return 1;
            }
            event = eventWrap;
            boolean handled = eventTarget.dispatchPointerEvent(eventWrap);
            if (ViewRootImpl.DEBUG_INPUT) {
                Log.v(this.this$0.mTag, "App handle pointer event: event = " + eventWrap + ", eventTarget = " + eventTarget + ", mCapturingView = " + this.this$0.mCapturingView + ", mView = " + this.this$0.mView + ", this = " + this);
            }
            maybeUpdatePointerIcon(eventWrap);
            this.this$0.mAttachInfo.mHandlingPointerEvent = false;
            if (this.this$0.mAttachInfo.mUnbufferedDispatchRequested && !this.this$0.mUnbufferedInputDispatch) {
                this.this$0.mUnbufferedInputDispatch = true;
                if (this.this$0.mConsumeBatchedInputScheduled) {
                    this.this$0.scheduleConsumeBatchedInputImmediately();
                }
            }
            if (handled) {
                i = 1;
            }
            return i;
        }

        private void maybeUpdatePointerIcon(MotionEvent event) {
            if (event.getPointerCount() == 1 && event.isFromSource(InputDevice.SOURCE_MOUSE)) {
                if (event.getActionMasked() == 9 || event.getActionMasked() == 10) {
                    this.this$0.mPointerIconType = 1;
                }
                if (event.getActionMasked() != 10 && !this.this$0.updatePointerIcon(event) && event.getActionMasked() == 7) {
                    this.this$0.mPointerIconType = 1;
                }
            }
        }

        private int processTrackballEvent(QueuedInputEvent q) {
            if (this.this$0.mView.dispatchTrackballEvent(q.mEvent)) {
                return 1;
            }
            return 0;
        }

        private int processGenericMotionEvent(QueuedInputEvent q) {
            if (this.this$0.mView.dispatchGenericMotionEvent(q.mEvent)) {
                return 1;
            }
            return 0;
        }
    }

    final class ViewPreImeInputStage extends InputStage {
        final /* synthetic */ ViewRootImpl this$0;

        public ViewPreImeInputStage(ViewRootImpl this$0, InputStage next) {
            this.this$0 = this$0;
            super(this$0, next);
        }

        protected int onProcess(QueuedInputEvent q) {
            if (!(q.mEvent instanceof KeyEvent)) {
                return 0;
            }
            this.this$0.mKeyEventStatus = ViewRootImpl.INPUT_DISPATCH_STATE_VIEW_PRE_IME_STAGE;
            return processKeyEvent(q);
        }

        private int processKeyEvent(QueuedInputEvent q) {
            if (this.this$0.mView.dispatchKeyEventPreIme(q.mEvent)) {
                return 1;
            }
            return 0;
        }
    }

    final class ViewRootHandler extends Handler {
        final /* synthetic */ ViewRootImpl this$0;

        ViewRootHandler(ViewRootImpl this$0) {
            this.this$0 = this$0;
        }

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
                case 11:
                    return "MSG_DISPATCH_KEY_FROM_IME";
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
                case 101:
                    return "MSG_RELAYOUT_RETRY";
                default:
                    return super.getMessageName(message);
            }
        }

        public boolean sendMessageAtTime(Message msg, long uptimeMillis) {
            if (msg.what != 26 || msg.obj != null) {
                return super.sendMessageAtTime(msg, uptimeMillis);
            }
            throw new NullPointerException("Attempted to call MSG_REQUEST_KEYBOARD_SHORTCUTS with null receiver:");
        }

        /* JADX WARNING: Missing block: B:29:0x00cb, code:
            if (r14.argi1 == 0) goto L_0x0007;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void handleMessage(Message msg) {
            SomeArgs args;
            Configuration config;
            InputMethodManager imm;
            switch (msg.what) {
                case 1:
                    ((View) msg.obj).invalidate();
                    break;
                case 2:
                    InvalidateInfo info = msg.obj;
                    info.target.invalidate(info.left, info.top, info.right, info.bottom);
                    info.recycle();
                    break;
                case 3:
                    this.this$0.doDie();
                    break;
                case 4:
                    args = msg.obj;
                    if (this.this$0.mWinFrame.equals(args.arg1)) {
                        if (this.this$0.mPendingOverscanInsets.equals(args.arg5)) {
                            if (this.this$0.mPendingContentInsets.equals(args.arg2)) {
                                if (this.this$0.mPendingStableInsets.equals(args.arg6)) {
                                    if (this.this$0.mPendingVisibleInsets.equals(args.arg3)) {
                                        if (this.this$0.mPendingOutsets.equals(args.arg7)) {
                                            if (this.this$0.mPendingBackDropFrame.equals(args.arg8)) {
                                                if (args.arg4 == null) {
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                case 5:
                    if (this.this$0.mAdded) {
                        args = (SomeArgs) msg.obj;
                        config = args.arg4;
                        if (config != null) {
                            this.this$0.updateConfiguration(config, false);
                        }
                        if (ViewRootImpl.DEBUG_LAYOUT) {
                            Log.d(this.this$0.mTag, "Handle RESIZE: message = " + msg.what + " ,this = " + this.this$0);
                        }
                        boolean framesChanged = (this.this$0.mWinFrame.equals(args.arg1) && this.this$0.mPendingOverscanInsets.equals(args.arg5) && this.this$0.mPendingContentInsets.equals(args.arg2) && this.this$0.mPendingStableInsets.equals(args.arg6) && this.this$0.mPendingVisibleInsets.equals(args.arg3)) ? !this.this$0.mPendingOutsets.equals(args.arg7) : true;
                        this.this$0.mWinFrame.set((Rect) args.arg1);
                        this.this$0.mPendingOverscanInsets.set((Rect) args.arg5);
                        this.this$0.mPendingContentInsets.set((Rect) args.arg2);
                        this.this$0.mPendingStableInsets.set((Rect) args.arg6);
                        this.this$0.mPendingVisibleInsets.set((Rect) args.arg3);
                        this.this$0.mPendingOutsets.set((Rect) args.arg7);
                        this.this$0.mPendingBackDropFrame.set((Rect) args.arg8);
                        this.this$0.mForceNextWindowRelayout = args.argi1 != 0;
                        this.this$0.mPendingAlwaysConsumeNavBar = args.argi2 != 0;
                        args.recycle();
                        if (msg.what == 5) {
                            this.this$0.mReportNextDraw = true;
                        }
                        if (this.this$0.mView != null && framesChanged) {
                            ViewRootImpl.forceLayout(this.this$0.mView);
                        }
                        this.this$0.requestLayout();
                        break;
                    }
                    break;
                case 6:
                    if (this.this$0.mAdded) {
                        boolean hasWindowFocus = msg.arg1 != 0;
                        this.this$0.mAttachInfo.mHasWindowFocus = hasWindowFocus;
                        if (ViewRootImpl.DEBUG_PANIC) {
                            Log.d(ViewRootImpl.TAG, "MSG_WINDOW_FOCUS_CHANGED, hasWindowFocus:" + this.this$0.mAttachInfo.mHasWindowFocus);
                        }
                        this.this$0.profileRendering(hasWindowFocus);
                        if (hasWindowFocus) {
                            this.this$0.ensureTouchModeLocally(msg.arg2 != 0);
                            if (this.this$0.mAttachInfo.mHardwareRenderer != null && this.this$0.mSurface.isValid()) {
                                this.this$0.mFullRedrawNeeded = true;
                                try {
                                    LayoutParams lp = this.this$0.mWindowAttributes;
                                    this.this$0.mAttachInfo.mHardwareRenderer.initializeIfNeeded(this.this$0.mWidth, this.this$0.mHeight, this.this$0.mAttachInfo, this.this$0.mSurface, lp != null ? lp.surfaceInsets : null);
                                } catch (Throwable e) {
                                    Log.e(this.this$0.mTag, "OutOfResourcesException locking surface", e);
                                    try {
                                        if (!this.this$0.mWindowSession.outOfMemory(this.this$0.mWindow)) {
                                            Slog.w(this.this$0.mTag, "No processes killed for memory; killing self");
                                            Process.killProcess(Process.myPid());
                                        }
                                    } catch (RemoteException e2) {
                                        Log.e(this.this$0.mTag, "RemoteException when call outOfMemory.", e);
                                    }
                                    sendMessageDelayed(obtainMessage(msg.what, msg.arg1, msg.arg2), 500);
                                    return;
                                }
                            }
                        }
                        this.this$0.mLastWasImTarget = LayoutParams.mayUseInputMethod(this.this$0.mWindowAttributes.flags);
                        imm = InputMethodManager.peekInstance();
                        if (!(imm == null || !this.this$0.mLastWasImTarget || this.this$0.isInLocalFocusMode())) {
                            imm.onPreWindowFocus(this.this$0.mView, hasWindowFocus);
                        }
                        if (this.this$0.mView != null) {
                            this.this$0.mAttachInfo.mKeyDispatchState.reset();
                            this.this$0.mView.dispatchWindowFocusChanged(hasWindowFocus);
                            this.this$0.mAttachInfo.mTreeObserver.dispatchOnWindowFocusChange(hasWindowFocus);
                        }
                        if (ViewRootImpl.DEBUG_IMF) {
                            Log.v(this.this$0.mTag, "Handle MSG_WINDOW_FOCUS_CHANGED: hasWindowFocus = " + hasWindowFocus + ", mLastWasImTarget = " + this.this$0.mLastWasImTarget + ", softInputMode = #" + Integer.toHexString(this.this$0.mWindowAttributes.softInputMode) + ", window flags = #" + Integer.toHexString(this.this$0.mWindowAttributes.flags) + ", mView = " + this.this$0.mView + ", this = " + this);
                        }
                        if (hasWindowFocus) {
                            if (!(imm == null || !this.this$0.mLastWasImTarget || this.this$0.isInLocalFocusMode())) {
                                imm.onPostWindowFocus(this.this$0.mView, this.this$0.mView.findFocus(), this.this$0.mWindowAttributes.softInputMode, !this.this$0.mHasHadWindowFocus, this.this$0.mWindowAttributes.flags);
                            }
                            LayoutParams layoutParams = this.this$0.mWindowAttributes;
                            layoutParams.softInputMode &= -257;
                            layoutParams = (LayoutParams) this.this$0.mView.getLayoutParams();
                            layoutParams.softInputMode &= -257;
                            this.this$0.mHasHadWindowFocus = true;
                            break;
                        }
                    }
                    break;
                case 7:
                    args = (SomeArgs) msg.obj;
                    this.this$0.enqueueInputEvent(args.arg1, args.arg2, 0, true);
                    args.recycle();
                    break;
                case 8:
                    this.this$0.handleAppVisibility(msg.arg1 != 0);
                    break;
                case 9:
                    this.this$0.handleGetNewSurface();
                    break;
                case 11:
                    if (ViewRootImpl.LOCAL_LOGV || ViewRootImpl.DEBUG_KEY) {
                        Log.v(this.this$0.mTag, "Dispatching key " + msg.obj + " from IME to " + this.this$0.mView + " in " + this);
                    }
                    InputEvent event = msg.obj;
                    if ((event.getFlags() & 8) != 0) {
                        event = KeyEvent.changeFlags(event, event.getFlags() & -9);
                    }
                    this.this$0.enqueueInputEvent(event, null, 1, true);
                    break;
                case 13:
                    imm = InputMethodManager.peekInstance();
                    if (imm != null) {
                        imm.checkFocus();
                        break;
                    }
                    break;
                case 14:
                    if (this.this$0.mView != null) {
                        this.this$0.mView.onCloseSystemDialogs((String) msg.obj);
                        break;
                    }
                    break;
                case 15:
                case 16:
                    DragEvent event2 = msg.obj;
                    event2.mLocalState = this.this$0.mLocalDragState;
                    this.this$0.handleDragEvent(event2);
                    break;
                case 17:
                    this.this$0.handleDispatchSystemUiVisibilityChanged((SystemUiVisibilityInfo) msg.obj);
                    break;
                case 18:
                    config = (Configuration) msg.obj;
                    if (config.isOtherSeqNewer(this.this$0.mLastConfiguration)) {
                        config = this.this$0.mLastConfiguration;
                    }
                    this.this$0.updateConfiguration(config, false);
                    break;
                case 19:
                    this.this$0.mProcessInputEventsScheduled = false;
                    this.this$0.doProcessInputEvents();
                    break;
                case 21:
                    this.this$0.setAccessibilityFocus(null, null);
                    break;
                case 22:
                    if (this.this$0.mView != null) {
                        this.this$0.invalidateWorld(this.this$0.mView);
                        break;
                    }
                    break;
                case 23:
                    if (this.this$0.mAdded) {
                        boolean suppress;
                        int w = this.this$0.mWinFrame.width();
                        int h = this.this$0.mWinFrame.height();
                        int l = msg.arg1;
                        int t = msg.arg2;
                        this.this$0.mWinFrame.left = l;
                        this.this$0.mWinFrame.right = l + w;
                        this.this$0.mWinFrame.top = t;
                        this.this$0.mWinFrame.bottom = t + h;
                        this.this$0.mPendingBackDropFrame.set(this.this$0.mWinFrame);
                        boolean isDockedDivider = this.this$0.mWindowAttributes.type == 2034;
                        if (this.this$0.mDragResizing && this.this$0.mResizeMode == 1) {
                            suppress = true;
                        } else {
                            suppress = isDockedDivider;
                        }
                        if (!suppress) {
                            if (this.this$0.mView != null) {
                                ViewRootImpl.forceLayout(this.this$0.mView);
                            }
                            this.this$0.requestLayout();
                            break;
                        }
                        this.this$0.maybeHandleWindowMove(this.this$0.mWinFrame);
                        break;
                    }
                    break;
                case 24:
                    this.this$0.enqueueInputEvent((InputEvent) msg.obj, null, 32, true);
                    break;
                case 25:
                    this.this$0.handleDispatchWindowShown();
                    break;
                case 26:
                    this.this$0.handleRequestKeyboardShortcuts(msg.obj, msg.arg1);
                    break;
                case 27:
                    this.this$0.resetPointerIcon(msg.obj);
                    break;
                case 101:
                    if (ViewRootImpl.DEBUG_LAYOUT) {
                        Log.i(ViewRootImpl.TAG, "MSG_RELAYOUT_RETRY, set mRetryLayout to true");
                    }
                    this.this$0.mRetryLayout = true;
                    break;
                case 10000:
                    this.this$0.initGlobalScale();
                    break;
            }
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.view.ViewRootImpl.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.view.ViewRootImpl.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.view.ViewRootImpl.<clinit>():void");
    }

    @OppoHook(level = OppoHookType.CHANGE_CODE, note = "JianHui.Yu@Plf.SDK, 2016-03-03 : Modify for Longshot", property = OppoRomType.ROM)
    public ViewRootImpl(Context context, Display display) {
        this.mGraphicLock = new Object();
        this.mWindowCallbacks = new ArrayList();
        this.mTmpLocation = new int[2];
        this.mTmpValue = new TypedValue();
        this.mWindowAttributes = new LayoutParams();
        this.mAppVisible = true;
        this.mForceDecorViewVisibility = false;
        this.mOrigWindowType = -1;
        this.mStopped = false;
        this.mIsAmbientMode = false;
        this.mPausedForTransition = false;
        this.mLastInCompatMode = false;
        this.mCompactScale = 1.0f;
        this.mPendingInputEventQueueLengthCounterName = "pq";
        this.mWindowAttributesChanged = false;
        this.mWindowAttributesChangesFlag = 0;
        this.mSurface = new Surface();
        this.mPendingOverscanInsets = new Rect();
        this.mPendingVisibleInsets = new Rect();
        this.mPendingStableInsets = new Rect();
        this.mPendingContentInsets = new Rect();
        this.mPendingOutsets = new Rect();
        this.mPendingBackDropFrame = new Rect();
        this.mLastGivenInsets = new InternalInsetsInfo();
        this.mDispatchContentInsets = new Rect();
        this.mDispatchStableInsets = new Rect();
        this.mLastConfiguration = new Configuration();
        this.mPendingConfiguration = new Configuration();
        this.mDragPoint = new PointF();
        this.mLastTouchPoint = new PointF();
        this.mFpsStartTime = -1;
        this.mFpsPrevTime = -1;
        this.mPointerIconType = 1;
        this.mCustomPointerIcon = null;
        this.mInLayout = false;
        this.mLayoutRequesters = new ArrayList();
        this.mHandlingLayoutInLayoutRequest = false;
        this.mFullScreen = true;
        this.mDisplayInfo = new DisplayInfo();
        this.mGlobalScale = -1.0f;
        this.mIsLuckyMoneyView = false;
        this.mIsWeixinLauncherUI = false;
        this.mRetryLayout = false;
        this.mInputEventConsistencyVerifier = InputEventConsistencyVerifier.isInstrumentationEnabled() ? new InputEventConsistencyVerifier(this, 0) : null;
        this.mTag = TAG;
        this.mProfile = false;
        this.mDisplayListener = new DisplayListener() {
            public void onDisplayChanged(int displayId) {
                if (ViewRootImpl.this.mView != null && ViewRootImpl.this.mDisplay.getDisplayId() == displayId) {
                    int oldDisplayState = ViewRootImpl.this.mAttachInfo.mDisplayState;
                    int newDisplayState = ViewRootImpl.this.mDisplay.getState();
                    if (oldDisplayState != newDisplayState) {
                        ViewRootImpl.this.mAttachInfo.mDisplayState = newDisplayState;
                        ViewRootImpl.this.pokeDrawLockIfNeeded();
                        if (oldDisplayState != 0) {
                            int oldScreenState = toViewScreenState(oldDisplayState);
                            int newScreenState = toViewScreenState(newDisplayState);
                            if (oldScreenState != newScreenState) {
                                ViewRootImpl.this.mView.dispatchScreenStateChanged(newScreenState);
                            }
                            if (oldDisplayState == 1) {
                                ViewRootImpl.this.mFullRedrawNeeded = true;
                                ViewRootImpl.this.scheduleTraversals();
                            }
                        }
                    }
                }
            }

            public void onDisplayRemoved(int displayId) {
            }

            public void onDisplayAdded(int displayId) {
            }

            private int toViewScreenState(int displayState) {
                if (displayState == 1) {
                    return 0;
                }
                return 1;
            }
        };
        this.mHandler = new ViewRootHandler(this);
        this.mProcessCpuTrackerRunnable = new ProcessCpuTrackerRunnable();
        this.mTraversalRunnable = new TraversalRunnable(this);
        this.mConsumedBatchedInputRunnable = new ConsumeBatchedInputRunnable(this);
        this.mConsumeBatchedInputImmediatelyRunnable = new ConsumeBatchedInputImmediatelyRunnable(this);
        this.mInvalidateOnAnimationRunnable = new InvalidateOnAnimationRunnable(this);
        this.mKeyEventStatus = INPUT_DISPATCH_STATE_FINISHED;
        this.mMotionEventStatus = INPUT_DISPATCH_STATE_FINISHED;
        this.mFrame = 0;
        this.mCount = 0;
        this.mContext = context;
        this.mViewRootHooks = new ColorViewRootImplHooks(this, context);
        this.mWindowSession = WindowManagerGlobal.getWindowSession();
        this.mDisplay = display;
        this.mBasePackageName = context.getBasePackageName();
        this.mIsKeyguard = KEYGUARD_PACKAGENAME.equals(this.mBasePackageName);
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
        this.mTargetSdkVersion = context.getApplicationInfo().targetSdkVersion;
        this.mViewVisibility = 8;
        this.mTransparentRegion = new Region();
        this.mPreviousTransparentRegion = new Region();
        this.mFirst = true;
        this.mAdded = false;
        this.mAttachInfo = new AttachInfo(this.mWindowSession, this.mWindow, display, this, this.mHandler, this);
        this.mAccessibilityManager = AccessibilityManager.getInstance(context);
        this.mAccessibilityInteractionConnectionManager = new AccessibilityInteractionConnectionManager(this);
        this.mAccessibilityManager.addAccessibilityStateChangeListener(this.mAccessibilityInteractionConnectionManager);
        this.mHighContrastTextManager = new HighContrastTextManager(this);
        this.mAccessibilityManager.addHighTextContrastStateChangeListener(this.mHighContrastTextManager);
        this.mViewConfiguration = ViewConfiguration.get(context);
        this.mDensity = context.getResources().getDisplayMetrics().densityDpi;
        this.mNoncompatDensity = context.getResources().getDisplayMetrics().noncompatDensityDpi;
        this.mFallbackEventHandler = new PhoneFallbackEventHandler(context);
        this.mChoreographer = Choreographer.getInstance();
        this.mDisplayManager = (DisplayManager) context.getSystemService("display");
        loadSystemProperties();
        DEBUG_PANIC = SystemProperties.getBoolean("persist.sys.assert.panic", false);
        DEBUG_IMF = DEBUG_PANIC;
        String open = SystemProperties.get("sys.view.root.log");
        if (open == null || !open.equals("true")) {
            enableLogLight(false);
        } else {
            enableLogLight(true);
        }
        long j = sIdent;
        sIdent = 1 + j;
        this.mIdent = j;
        checkViewRootImplLogProperty();
        if (LOCAL_LOGV) {
            enableLog(true);
        }
        if (DEBUG_LIFECYCLE) {
            Log.v(this.mTag, "ViewRootImpl construct: context = " + context + ", mThread = " + this.mThread + ", mChoreographer = " + this.mChoreographer + ", mTraversalRunnable = " + this.mTraversalRunnable + ", this = " + this);
        }
        this.mScreenHeight = OppoScreenDragUtil.getHeight();
        this.mScreenWidth = OppoScreenDragUtil.getWidth();
        this.mDisplay.getDisplayInfo(this.mDisplayInfo);
        int height = this.mDisplayInfo.logicalHeight;
        int width = this.mDisplayInfo.logicalWidth;
        int i;
        if (width > height) {
            if (this.mScreenHeight > width) {
                i = this.mScreenHeight;
            } else {
                i = width;
            }
            this.mScreenHeight = i;
            if (this.mScreenWidth > height) {
                i = this.mScreenWidth;
            } else {
                i = height;
            }
            this.mScreenWidth = i;
        } else {
            if (this.mScreenHeight > height) {
                i = this.mScreenHeight;
            } else {
                i = height;
            }
            this.mScreenHeight = i;
            if (this.mScreenWidth > width) {
                i = this.mScreenWidth;
            } else {
                i = width;
            }
            this.mScreenWidth = i;
        }
        if (height <= width) {
            height = width;
        }
        this.mHideNavigationbarArea = (context.getResources().getDimensionPixelSize(R.dimen.navigation_bar_height) + height) - 21;
        this.mStatusBarHeight = context.getResources().getDimensionPixelSize(R.dimen.status_bar_height);
        if (context.getResources().getDisplayMetrics().noncompatScaledDensity != context.getResources().getDisplayMetrics().scaledDensity) {
            TypedValue outValue = new TypedValue();
            context.getResources().getValue(R.dimen.status_bar_height, outValue, true);
            if (outValue != null && outValue.type == 5) {
                this.mStatusBarHeight = ((int) TypedValue.complexToFloat(outValue.data)) * ((int) context.getResources().getDisplayMetrics().noncompatScaledDensity);
            }
        }
        sendInitGlobalScale();
        DisplayMetrics displatMetrics = new DisplayMetrics();
        this.mDisplayInfo.getAppMetrics(displatMetrics);
        this.mCompactScale = CompatibilityInfo.computeCompatibleScaling(displatMetrics, null);
    }

    public static void addFirstDrawHandler(Runnable callback) {
        synchronized (sFirstDrawHandlers) {
            if (!sFirstDrawComplete) {
                sFirstDrawHandlers.add(callback);
            }
        }
    }

    public static void addConfigCallback(ComponentCallbacks callback) {
        synchronized (sConfigCallbacks) {
            sConfigCallbacks.add(callback);
        }
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
        if (this.mWindowDrawCountDown != null) {
            this.mWindowDrawCountDown.countDown();
        }
    }

    public void profile() {
        this.mProfile = true;
    }

    static boolean isInTouchMode() {
        IWindowSession windowSession = WindowManagerGlobal.peekWindowSession();
        if (windowSession != null) {
            try {
                return windowSession.getInTouchMode();
            } catch (RemoteException e) {
            }
        }
        return false;
    }

    public void notifyChildRebuilt() {
        if (this.mView instanceof RootViewSurfaceTaker) {
            this.mSurfaceHolderCallback = ((RootViewSurfaceTaker) this.mView).willYouTakeTheSurface();
            if (this.mSurfaceHolderCallback != null) {
                this.mSurfaceHolder = new TakenSurfaceHolder(this);
                this.mSurfaceHolder.setFormat(0);
            } else {
                this.mSurfaceHolder = null;
            }
            this.mInputQueueCallback = ((RootViewSurfaceTaker) this.mView).willYouTakeTheInputQueue();
            if (this.mInputQueueCallback != null) {
                this.mInputQueueCallback.onInputQueueCreated(this.mInputQueue);
            }
        }
    }

    @OppoHook(level = OppoHookType.CHANGE_CODE, note = "JianHui.Yu@Plf.SDK, 2016-04-14 : Modify for Longshot", property = OppoRomType.ROM)
    public void setView(View view, LayoutParams attrs, View panelParentView) {
        synchronized (this) {
            if (this.mView == null) {
                float f;
                int res;
                this.mView = view;
                this.mAttachInfo.mDisplayState = this.mDisplay.getState();
                this.mDisplayManager.registerDisplayListener(this.mDisplayListener, this.mHandler);
                this.mViewLayoutDirectionInitial = this.mView.getRawLayoutDirection();
                this.mFallbackEventHandler.setView(view);
                this.mWindowAttributes.copyFrom(attrs);
                if (this.mWindowAttributes.packageName == null) {
                    this.mWindowAttributes.packageName = this.mBasePackageName;
                }
                attrs = this.mWindowAttributes;
                setTag();
                if (DEBUG_KEEP_SCREEN_ON && (this.mClientWindowLayoutFlags & 128) != 0 && (attrs.flags & 128) == 0) {
                    Slog.d(this.mTag, "setView: FLAG_KEEP_SCREEN_ON changed from true to false!");
                }
                this.mClientWindowLayoutFlags = attrs.flags;
                setAccessibilityFocus(null, null);
                if (view instanceof RootViewSurfaceTaker) {
                    this.mSurfaceHolderCallback = ((RootViewSurfaceTaker) view).willYouTakeTheSurface();
                    if (this.mSurfaceHolderCallback != null) {
                        this.mSurfaceHolder = new TakenSurfaceHolder(this);
                        this.mSurfaceHolder.setFormat(0);
                    }
                }
                if (!attrs.hasManualSurfaceInsets) {
                    attrs.setSurfaceInsets(view, false, true);
                }
                CompatibilityInfo compatibilityInfo = this.mDisplay.getDisplayAdjustments().getCompatibilityInfo();
                this.mTranslator = compatibilityInfo.getTranslator();
                if (this.mSurfaceHolder == null) {
                    enableHardwareAcceleration(attrs);
                }
                boolean restore = false;
                if (this.mTranslator != null) {
                    this.mSurface.setCompatibilityTranslator(this.mTranslator);
                    restore = true;
                    attrs.backup();
                    this.mTranslator.translateWindowLayout(attrs);
                }
                if (DEBUG_LAYOUT || DEBUG_LIFECYCLE) {
                    Log.d(this.mTag, "WindowLayout in setView:" + attrs + ",mView = " + this.mView + ",compatibilityInfo = " + compatibilityInfo + ", this = " + this);
                }
                if (!compatibilityInfo.supportsScreen()) {
                    attrs.privateFlags |= 128;
                    this.mLastInCompatMode = true;
                }
                this.mSoftInputMode = attrs.softInputMode;
                this.mWindowAttributesChanged = true;
                this.mWindowAttributesChangesFlag = -1;
                this.mAttachInfo.mRootView = view;
                this.mAttachInfo.mScalingRequired = this.mTranslator != null;
                AttachInfo attachInfo = this.mAttachInfo;
                if (this.mTranslator == null) {
                    f = 1.0f;
                } else {
                    f = this.mTranslator.applicationScale;
                }
                attachInfo.mApplicationScale = f;
                if (panelParentView != null) {
                    this.mAttachInfo.mPanelParentWindowToken = panelParentView.getApplicationWindowToken();
                }
                this.mAdded = true;
                requestLayout();
                if ((this.mWindowAttributes.inputFeatures & 2) == 0) {
                    this.mInputChannel = new InputChannel();
                }
                this.mForceDecorViewVisibility = (this.mWindowAttributes.privateFlags & 16384) != 0;
                try {
                    this.mOrigWindowType = this.mWindowAttributes.type;
                    this.mAttachInfo.mRecomputeGlobalAttributes = true;
                    collectViewAttributes();
                    res = this.mWindowSession.addToDisplay(this.mWindow, this.mSeq, this.mWindowAttributes, getHostVisibility(), this.mDisplay.getDisplayId(), this.mAttachInfo.mContentInsets, this.mAttachInfo.mStableInsets, this.mAttachInfo.mOutsets, this.mInputChannel);
                    if (restore) {
                        attrs.restore();
                    }
                    if (this.mTranslator != null) {
                        this.mTranslator.translateRectInScreenToAppWindow(this.mAttachInfo.mContentInsets);
                    }
                    this.mPendingOverscanInsets.set(0, 0, 0, 0);
                    this.mPendingContentInsets.set(this.mAttachInfo.mContentInsets);
                    this.mPendingStableInsets.set(this.mAttachInfo.mStableInsets);
                    this.mPendingVisibleInsets.set(0, 0, 0, 0);
                    this.mAttachInfo.mAlwaysConsumeNavBar = (res & 4) != 0;
                    this.mPendingAlwaysConsumeNavBar = this.mAttachInfo.mAlwaysConsumeNavBar;
                    if (DEBUG_LAYOUT) {
                        Log.v(this.mTag, "Added window " + this.mWindow + ", mPendingContentInsets = " + this.mPendingContentInsets + ", mPendingStableInsets = " + this.mPendingStableInsets + ", this = " + this);
                    }
                } catch (RemoteException e) {
                    this.mAdded = false;
                    this.mView = null;
                    this.mAttachInfo.mRootView = null;
                    this.mInputChannel = null;
                    this.mFallbackEventHandler.setView(null);
                    unscheduleTraversals();
                    setAccessibilityFocus(null, null);
                    throw new RuntimeException("Adding window failed", e);
                } catch (Throwable th) {
                    if (restore) {
                        attrs.restore();
                    }
                }
                if (res < 0) {
                    this.mAttachInfo.mRootView = null;
                    this.mAdded = false;
                    this.mFallbackEventHandler.setView(null);
                    unscheduleTraversals();
                    setAccessibilityFocus(null, null);
                    switch (res) {
                        case -10:
                            throw new InvalidDisplayException("Unable to add window " + this.mWindow + " -- the specified window type " + this.mWindowAttributes.type + " is not valid");
                        case -9:
                            throw new InvalidDisplayException("Unable to add window " + this.mWindow + " -- the specified display can not be found");
                        case -8:
                            throw new BadTokenException("Unable to add window " + this.mWindow + " -- permission denied for window type " + this.mWindowAttributes.type);
                        case -7:
                            throw new BadTokenException("Unable to add window " + this.mWindow + " -- another window of type " + this.mWindowAttributes.type + " already exists");
                        case -6:
                            return;
                        case -5:
                            throw new BadTokenException("Unable to add window -- window " + this.mWindow + " has already been added");
                        case -4:
                            throw new BadTokenException("Unable to add window -- app for token " + attrs.token + " is exiting");
                        case -3:
                            throw new BadTokenException("Unable to add window -- token " + attrs.token + " is not for an application");
                        case -2:
                        case -1:
                            throw new BadTokenException("Unable to add window -- token " + attrs.token + " is not valid; is your activity running?");
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
                        this.mInputEventReceiver = new ColorWindowInputEventReceiver(this, this.mInputChannel, Looper.myLooper(), this);
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
                    CharSequence counterSuffix = attrs.getTitle();
                    this.mSyntheticInputStage = new SyntheticInputStage(this);
                    InputStage earlyPostImeInputStage = new EarlyPostImeInputStage(this, new NativePostImeInputStage(this, new ViewPostImeInputStage(this, this.mSyntheticInputStage), "aq:native-post-ime:" + counterSuffix));
                    this.mFirstInputStage = new NativePreImeInputStage(this, new ViewPreImeInputStage(this, new ImeInputStage(this, earlyPostImeInputStage, "aq:ime:" + counterSuffix)), "aq:native-pre-ime:" + counterSuffix);
                    this.mFirstPostImeInputStage = earlyPostImeInputStage;
                    this.mPendingInputEventQueueLengthCounterName = "aq:pending:" + counterSuffix;
                }
            }
            this.mViewRootHooks.getLongshotViewRoot().createAnalyzer();
        }
    }

    private void setTag() {
        String[] split = this.mWindowAttributes.getTitle().toString().split("\\.");
        if (split.length > 0) {
            this.mTag = "ViewRootImpl[" + split[split.length - 1] + "]";
        }
    }

    private boolean isInLocalFocusMode() {
        return (this.mWindowAttributes.flags & 268435456) != 0;
    }

    public int getWindowFlags() {
        return this.mWindowAttributes.flags;
    }

    public int getDisplayId() {
        return this.mDisplay.getDisplayId();
    }

    public CharSequence getTitle() {
        return this.mWindowAttributes.getTitle();
    }

    void destroyHardwareResources() {
        if (this.mAttachInfo.mHardwareRenderer != null) {
            this.mAttachInfo.mHardwareRenderer.destroyHardwareResources(this.mView);
            this.mAttachInfo.mHardwareRenderer.destroy();
        }
    }

    public void detachFunctor(long functor) {
        if (this.mAttachInfo.mHardwareRenderer != null) {
            this.mAttachInfo.mHardwareRenderer.stopDrawing();
        }
    }

    public static void invokeFunctor(long functor, boolean waitForCompletion) {
        ThreadedRenderer.invokeFunctor(functor, waitForCompletion);
    }

    public void registerAnimatingRenderNode(RenderNode animator) {
        if (this.mAttachInfo.mHardwareRenderer != null) {
            this.mAttachInfo.mHardwareRenderer.registerAnimatingRenderNode(animator);
            return;
        }
        if (this.mAttachInfo.mPendingAnimatingRenderNodes == null) {
            this.mAttachInfo.mPendingAnimatingRenderNodes = new ArrayList();
        }
        this.mAttachInfo.mPendingAnimatingRenderNodes.add(animator);
    }

    public void registerVectorDrawableAnimator(VectorDrawableAnimatorRT animator) {
        if (this.mAttachInfo.mHardwareRenderer != null) {
            this.mAttachInfo.mHardwareRenderer.registerVectorDrawableAnimator(animator);
        }
    }

    private void enableHardwareAcceleration(LayoutParams attrs) {
        boolean hardwareAccelerated = false;
        this.mAttachInfo.mHardwareAccelerated = false;
        this.mAttachInfo.mHardwareAccelerationRequested = false;
        if (DEBUG_DISABLEHW) {
            Log.d(this.mTag, "disable hardware acceleration by DEBUG!!!, this = " + this);
        } else if (this.mTranslator == null) {
            if ((attrs.flags & 16777216) != 0) {
                hardwareAccelerated = true;
            }
            if (!hardwareAccelerated) {
                Log.d(this.mTag, "disable hardware acceleration by APP!!!, this = " + this);
            }
            if (hardwareAccelerated) {
                if (ThreadedRenderer.isAvailable()) {
                    boolean fakeHwAccelerated = (attrs.privateFlags & 1) != 0;
                    boolean forceHwAccelerated = (attrs.privateFlags & 2) != 0;
                    if (fakeHwAccelerated) {
                        this.mAttachInfo.mHardwareAccelerationRequested = true;
                    } else if (!ThreadedRenderer.sRendererDisabled || (ThreadedRenderer.sSystemRendererDisabled && forceHwAccelerated)) {
                        if (this.mAttachInfo.mHardwareRenderer != null) {
                            this.mAttachInfo.mHardwareRenderer.destroy();
                        }
                        Rect insets = attrs.surfaceInsets;
                        boolean hasSurfaceInsets = (insets.left == 0 && insets.right == 0 && insets.top == 0) ? insets.bottom != 0 : true;
                        this.mAttachInfo.mHardwareRenderer = ThreadedRenderer.create(this.mContext, attrs.format == -1 ? hasSurfaceInsets : true);
                        if (this.mAttachInfo.mHardwareRenderer != null) {
                            this.mAttachInfo.mHardwareRenderer.setName(attrs.getTitle().toString());
                            AttachInfo attachInfo = this.mAttachInfo;
                            this.mAttachInfo.mHardwareAccelerationRequested = true;
                            attachInfo.mHardwareAccelerated = true;
                            Log.d(this.mTag, "hardware acceleration is enabled, this = " + this);
                        }
                    }
                    if (this.mAttachInfo.mHardwareRenderer == null) {
                        Log.d(this.mTag, "hardware acceleration is disabled, fakeHwAccelerated = " + fakeHwAccelerated + ", HardwareRenderer.sRendererDisabled = " + ThreadedRenderer.sRendererDisabled + ", forceHwAccelerated = " + forceHwAccelerated + ", HardwareRenderer.sSystemRendererDisabled = " + ThreadedRenderer.sSystemRendererDisabled + ", this = " + this);
                    }
                } else {
                    Log.d(this.mTag, "system doesn't support hardware acceleration!!!, this = " + this);
                }
            }
        }
    }

    public View getView() {
        return this.mView;
    }

    final WindowLeaked getLocation() {
        return this.mLocation;
    }

    /* JADX WARNING: Missing block: B:45:0x0122, code:
            if (r10.mWindowAttributes.surfaceInsets.bottom == r2) goto L_0x0092;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    void setLayoutParams(LayoutParams attrs, boolean newView) {
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
            LayoutParams layoutParams = this.mWindowAttributes;
            layoutParams.privateFlags |= compatibleWindowFlag;
            if (this.mWindowAttributes.preservePreviousSurfaceInsets) {
                this.mWindowAttributes.surfaceInsets.set(oldInsetLeft, oldInsetTop, oldInsetRight, oldInsetBottom);
                this.mWindowAttributes.hasManualSurfaceInsets = oldHasManualSurfaceInsets;
            } else {
                if (this.mWindowAttributes.surfaceInsets.left == oldInsetLeft && this.mWindowAttributes.surfaceInsets.top == oldInsetTop) {
                    if (this.mWindowAttributes.surfaceInsets.right == oldInsetRight) {
                    }
                }
                this.mNeedsHwRendererSetup = true;
            }
            applyKeepScreenOnFlag(this.mWindowAttributes);
            if (newView) {
                this.mSoftInputMode = attrs.softInputMode;
                requestLayout();
            }
            if ((attrs.softInputMode & 240) == 0) {
                this.mWindowAttributes.softInputMode = (this.mWindowAttributes.softInputMode & -241) | (oldSoftInputMode & 240);
            }
            this.mWindowAttributesChanged = true;
            scheduleTraversals();
        }
        if (DEBUG_IMF) {
            Log.d(this.mTag, "setLayoutParams: attrs = " + attrs + ", mSoftInputMode = " + this.mSoftInputMode + ", mWindowAttributes = " + this.mWindowAttributes + ", this = " + this);
        }
    }

    void updateNavigationBarParams(View view, LayoutParams attrs) {
        synchronized (this) {
            if (view == this.mView) {
                if (this.mWindowAttributes.navigationBarColor != attrs.navigationBarColor) {
                    this.mWindowAttributes.navigationBarColor = attrs.navigationBarColor;
                    this.mWindowAttributesChanged = true;
                }
                if (this.mWindowAttributes.navigationBarVisibility != attrs.navigationBarVisibility) {
                    this.mWindowAttributes.navigationBarVisibility = attrs.navigationBarVisibility;
                    this.mWindowAttributesChanged = true;
                }
                if (this.mWindowAttributesChanged) {
                    scheduleTraversals();
                }
            }
        }
    }

    void handleAppVisibility(boolean visible) {
        if (DEBUG_DRAW || DEBUG_LAYOUT) {
            Log.d(this.mTag, "handleAppVisibility: visible=" + visible + ", mAppVisible=" + this.mAppVisible + ", this = " + this);
        }
        if (this.mAppVisible != visible) {
            this.mAppVisible = visible;
            scheduleTraversals();
            if (!this.mAppVisible) {
                WindowManagerGlobal.trimForeground();
            }
            this.mViewVisibilityChanged = true;
        }
    }

    void handleGetNewSurface() {
        this.mNewSurfaceNeeded = true;
        this.mFullRedrawNeeded = true;
        scheduleTraversals();
    }

    void pokeDrawLockIfNeeded() {
        int displayState = this.mAttachInfo.mDisplayState;
        if (this.mView == null || !this.mAdded || !this.mTraversalScheduled) {
            return;
        }
        if (displayState == 3 || displayState == 4) {
            try {
                this.mWindowSession.pokeDrawLock(this.mWindow);
            } catch (RemoteException e) {
            }
        }
    }

    public void requestFitSystemWindows() {
        checkThread();
        this.mApplyInsetsRequested = true;
        scheduleTraversals();
    }

    public void requestLayout() {
        if (!this.mHandlingLayoutInLayoutRequest) {
            if (DEBUG_LAYOUT) {
                Log.d(this.mTag, "requestLayout: mView = " + this.mView + ", this = " + this);
            }
            if (DEBUG_REQUESTLAYOUT) {
                Log.d(this.mTag, "requestLayout: mView = " + this.mView + ", this = " + this, new Throwable("requestLayout"));
            }
            checkThread();
            this.mLayoutRequested = true;
            scheduleTraversals();
        }
    }

    public boolean isLayoutRequested() {
        return this.mLayoutRequested;
    }

    void invalidate() {
        this.mDirty.set(0, 0, this.mWidth, this.mHeight);
        if (!this.mWillDrawSoon) {
            scheduleTraversals();
        }
    }

    void invalidateWorld(View view) {
        view.invalidate();
        if (view instanceof ViewGroup) {
            ViewGroup parent = (ViewGroup) view;
            for (int i = 0; i < parent.getChildCount(); i++) {
                invalidateWorld(parent.getChildAt(i));
            }
        }
    }

    public void invalidateChild(View child, Rect dirty) {
        invalidateChildInParent(null, dirty);
    }

    public ViewParent invalidateChildInParent(int[] location, Rect dirty) {
        checkThread();
        if (DEBUG_DRAW) {
            Log.v(this.mTag, "Invalidate child: " + dirty + ",this = " + this);
        }
        if (DEBUG_INVALIDATE) {
            Log.v(this.mTag, "Invalidate child: " + dirty + ",this = " + this, new Throwable("invalidateChildInParent"));
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
                if (this.mCurScrollY != 0) {
                    dirty.offset(0, -this.mCurScrollY);
                }
                if (this.mTranslator != null) {
                    this.mTranslator.translateRectInAppWindowToScreen(dirty);
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
        if (!(localDirty.isEmpty() || localDirty.contains(dirty))) {
            this.mAttachInfo.mSetIgnoreDirtyState = true;
            this.mAttachInfo.mIgnoreDirtyState = true;
        }
        localDirty.union(dirty.left, dirty.top, dirty.right, dirty.bottom);
        float appScale = this.mAttachInfo.mApplicationScale;
        boolean intersected = localDirty.intersect(0, 0, (int) ((((float) this.mWidth) * appScale) + 0.5f), (int) ((((float) this.mHeight) * appScale) + 0.5f));
        if (!intersected) {
            localDirty.setEmpty();
        }
        if (!this.mWillDrawSoon && (intersected || this.mIsAnimating)) {
            scheduleTraversals();
        } else if (DEBUG_DRAW) {
            Log.v(this.mTag, "Invalidate child: Do not scheduleTraversals, mWillDrawSoon =" + this.mWillDrawSoon + ", intersected =" + intersected + ", mIsAnimating =" + this.mIsAnimating);
        }
    }

    public void setIsAmbientMode(boolean ambient) {
        this.mIsAmbientMode = ambient;
    }

    void setWindowStopped(boolean stopped) {
        if (DEBUG_LAYOUT) {
            Log.v(this.mTag, "setWindowStopped: stopped = " + stopped + ", this = " + this);
        }
        if (this.mStopped != stopped) {
            this.mStopped = stopped;
            ThreadedRenderer renderer = this.mAttachInfo.mHardwareRenderer;
            if (renderer != null) {
                if (DEBUG_DRAW) {
                    Log.d(this.mTag, "WindowStopped on " + getTitle() + " set to " + this.mStopped);
                }
                renderer.setStopped(this.mStopped);
            }
            if (!this.mStopped) {
                scheduleTraversals();
            } else if (renderer != null) {
                renderer.destroyHardwareResources(this.mView);
            }
        }
    }

    public void setPausedForTransition(boolean paused) {
        if (DEBUG_PANIC) {
            Log.d(TAG, "setPausedForTransition, paused:" + paused + ", from:" + Debug.getCallers(5));
        }
        this.mPausedForTransition = paused;
    }

    public ViewParent getParent() {
        return null;
    }

    public boolean getChildVisibleRect(View child, Rect r, Point offset) {
        if (child != this.mView) {
            throw new RuntimeException("child is not mine, honest!");
        }
        if (DEBUG_DRAW) {
            Log.v(this.mTag, "getChildVisibleRect: child = " + child + ",r = " + r + ", this = " + this);
        }
        return r.intersect(0, 0, this.mWidth, this.mHeight);
    }

    public void bringChildToFront(View child) {
    }

    int getHostVisibility() {
        if ((this.mAppVisible || this.mForceDecorViewVisibility) && this.mView != null) {
            return this.mView.getVisibility();
        }
        return 8;
    }

    public void requestTransitionStart(LayoutTransition transition) {
        if (this.mPendingTransitions == null || !this.mPendingTransitions.contains(transition)) {
            if (this.mPendingTransitions == null) {
                this.mPendingTransitions = new ArrayList();
            }
            this.mPendingTransitions.add(transition);
        }
    }

    void notifyRendererOfFramePending() {
        if (this.mAttachInfo.mHardwareRenderer != null) {
            this.mAttachInfo.mHardwareRenderer.notifyFramePending();
        }
    }

    void scheduleTraversals() {
        if (!this.mTraversalScheduled) {
            boolean scheduleByHandler = false;
            if (this.mCurrentMotion != null && this.mCurrentMotion.getAction() == 1 && isTargetPackage()) {
                scheduleByHandler = true;
                Trace.traceBegin(8, "scheduleTraversals by message");
                this.mHandler.post(this.mTraversalRunnable);
                Trace.traceEnd(8);
            }
            this.mTraversalScheduled = true;
            this.mTraversalBarrier = this.mHandler.getLooper().getQueue().postSyncBarrier();
            if (DEBUG_LIFECYCLE) {
                Log.v(this.mTag, "scheduleTraversals: mTraversalBarrier = " + this.mTraversalBarrier + ",this = " + this);
            }
            if (DEBUG_SCHEDULETRAVERSALS) {
                Log.v(this.mTag, "scheduleTraversals: mTraversalBarrier = " + this.mTraversalBarrier + ",this = " + this, new Throwable("scheduleTraversals"));
            }
            if (!scheduleByHandler) {
                this.mChoreographer.postCallback(2, this.mTraversalRunnable, null);
            }
            if (!this.mUnbufferedInputDispatch) {
                scheduleConsumeBatchedInput();
            }
            notifyRendererOfFramePending();
            pokeDrawLockIfNeeded();
        }
    }

    void unscheduleTraversals() {
        if (DEBUG_LIFECYCLE) {
            Log.v(this.mTag, "unscheduleTraversals: mTraversalScheduled = " + this.mTraversalScheduled + ",mTraversalBarrier = " + this.mTraversalBarrier + ",this = " + this);
        }
        if (this.mTraversalScheduled) {
            this.mTraversalScheduled = false;
            this.mHandler.getLooper().getQueue().removeSyncBarrier(this.mTraversalBarrier);
            this.mChoreographer.removeCallbacks(2, this.mTraversalRunnable, null);
        }
    }

    void doTraversal() {
        if (DEBUG_LIFECYCLE) {
            Log.v(this.mTag, "doTraversal: mTraversalScheduled = " + this.mTraversalScheduled + ",mTraversalBarrier = " + this.mTraversalBarrier + ",this = " + this);
        }
        if (this.mTraversalScheduled) {
            this.mTraversalScheduled = false;
            this.mHandler.getLooper().getQueue().removeSyncBarrier(this.mTraversalBarrier);
            if (this.mProfile) {
                Debug.startMethodTracing("ViewAncestor");
            }
            performTraversals();
            if (this.mProfile) {
                Debug.stopMethodTracing();
                this.mProfile = false;
            }
        }
    }

    private void applyKeepScreenOnFlag(LayoutParams params) {
        if (this.mAttachInfo.mKeepScreenOn) {
            params.flags |= 128;
        } else {
            params.flags = (params.flags & -129) | (this.mClientWindowLayoutFlags & 128);
        }
    }

    private boolean collectViewAttributes() {
        if (this.mAttachInfo.mRecomputeGlobalAttributes) {
            this.mAttachInfo.mRecomputeGlobalAttributes = false;
            boolean oldScreenOn = this.mAttachInfo.mKeepScreenOn;
            this.mAttachInfo.mKeepScreenOn = false;
            this.mAttachInfo.mSystemUiVisibility = 0;
            this.mAttachInfo.mHasSystemUiListeners = false;
            this.mView.dispatchCollectViewAttributes(this.mAttachInfo, 0);
            AttachInfo attachInfo = this.mAttachInfo;
            attachInfo.mSystemUiVisibility &= ~this.mAttachInfo.mDisabledSystemUiVisibility;
            LayoutParams params = this.mWindowAttributes;
            attachInfo = this.mAttachInfo;
            attachInfo.mSystemUiVisibility |= getImpliedSystemUiVisibility(params);
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

    private int getImpliedSystemUiVisibility(LayoutParams params) {
        int vis = 0;
        if ((params.flags & 67108864) != 0) {
            vis = GL10.GL_INVALID_ENUM;
        }
        if ((params.flags & 134217728) != 0) {
            return vis | 768;
        }
        return vis;
    }

    private boolean measureHierarchy(View host, LayoutParams lp, Resources res, int desiredWindowWidth, int desiredWindowHeight) {
        boolean windowSizeMayChange = false;
        if (DEBUG_ORIENTATION || DEBUG_LAYOUT || DEBUG_MEASURE_LAYOUT) {
            Log.v(this.mTag, "ViewRoot measure+ : " + host + " in display " + desiredWindowWidth + "x" + desiredWindowHeight + ", lp = " + lp + ", this = " + this);
        }
        boolean goodMeasure = false;
        if (lp.width == -2) {
            DisplayMetrics packageMetrics = res.getDisplayMetrics();
            res.getValue(R.dimen.config_prefDialogWidth, this.mTmpValue, true);
            int baseSize = 0;
            if (this.mTmpValue.type == 5) {
                baseSize = (int) this.mTmpValue.getDimension(packageMetrics);
            }
            if (DEBUG_DIALOG) {
                Log.v(this.mTag, "Window " + this.mView + ": baseSize=" + baseSize + ", desiredWindowWidth=" + desiredWindowWidth + ", this = " + this);
            }
            if (baseSize != 0 && desiredWindowWidth > baseSize) {
                int childWidthMeasureSpec = getRootMeasureSpec(baseSize, lp.width);
                int childHeightMeasureSpec = getRootMeasureSpec(desiredWindowHeight, lp.height);
                performMeasure(childWidthMeasureSpec, childHeightMeasureSpec);
                if (DEBUG_DIALOG) {
                    Log.v(this.mTag, "Window " + this.mView + ": measured (" + host.getMeasuredWidth() + "," + host.getMeasuredHeight() + ") from width spec: " + MeasureSpec.toString(childWidthMeasureSpec) + " and height spec: " + MeasureSpec.toString(childHeightMeasureSpec) + ", this = " + this);
                }
                if ((host.getMeasuredWidthAndState() & 16777216) == 0) {
                    goodMeasure = true;
                } else {
                    baseSize = (baseSize + desiredWindowWidth) / 2;
                    if (DEBUG_DIALOG) {
                        Log.v(this.mTag, "Window " + this.mView + ": next baseSize=" + baseSize + ", this = " + this);
                    }
                    performMeasure(getRootMeasureSpec(baseSize, lp.width), childHeightMeasureSpec);
                    if (DEBUG_DIALOG) {
                        Log.v(this.mTag, "Window " + this.mView + ": measured (" + host.getMeasuredWidth() + "," + host.getMeasuredHeight() + ")" + ", this = " + this);
                    }
                    if ((host.getMeasuredWidthAndState() & 16777216) == 0) {
                        if (DEBUG_DIALOG) {
                            Log.v(this.mTag, "Good!");
                        }
                        goodMeasure = true;
                    }
                }
            }
        }
        if (!goodMeasure) {
            performMeasure(getRootMeasureSpec(desiredWindowWidth, lp.width), getRootMeasureSpec(desiredWindowHeight, lp.height));
            if (!(this.mWidth == host.getMeasuredWidth() && this.mHeight == host.getMeasuredHeight())) {
                windowSizeMayChange = true;
            }
        }
        if (DBG) {
            System.out.println("======================================");
            System.out.println("performTraversals -- after measure");
            host.debug();
        }
        if (DEBUG_ORIENTATION || DEBUG_LAYOUT || DEBUG_MEASURE_LAYOUT) {
            Log.v(this.mTag, "ViewRoot measure-: host measured size = (" + host.getMeasuredWidth() + "x" + host.getMeasuredHeight() + "), windowSizeMayChange = " + windowSizeMayChange + ", this = " + this);
        }
        return windowSizeMayChange;
    }

    void transformMatrixToGlobal(Matrix m) {
        m.preTranslate((float) this.mAttachInfo.mWindowLeft, (float) this.mAttachInfo.mWindowTop);
    }

    void transformMatrixToLocal(Matrix m) {
        m.postTranslate((float) (-this.mAttachInfo.mWindowLeft), (float) (-this.mAttachInfo.mWindowTop));
    }

    WindowInsets getWindowInsets(boolean forceConstruct) {
        if (this.mLastWindowInsets == null || forceConstruct) {
            this.mDispatchContentInsets.set(this.mAttachInfo.mContentInsets);
            this.mDispatchStableInsets.set(this.mAttachInfo.mStableInsets);
            Rect contentInsets = this.mDispatchContentInsets;
            Rect stableInsets = this.mDispatchStableInsets;
            if (!(forceConstruct || (this.mPendingContentInsets.equals(contentInsets) && this.mPendingStableInsets.equals(stableInsets)))) {
                contentInsets = this.mPendingContentInsets;
                stableInsets = this.mPendingStableInsets;
            }
            Rect outsets = this.mAttachInfo.mOutsets;
            if (outsets.left > 0 || outsets.top > 0 || outsets.right > 0 || outsets.bottom > 0) {
                contentInsets = new Rect(contentInsets.left + outsets.left, contentInsets.top + outsets.top, contentInsets.right + outsets.right, contentInsets.bottom + outsets.bottom);
            }
            this.mLastWindowInsets = new WindowInsets(contentInsets, null, stableInsets, this.mContext.getResources().getConfiguration().isScreenRound(), this.mAttachInfo.mAlwaysConsumeNavBar);
        }
        return this.mLastWindowInsets;
    }

    void dispatchApplyInsets(View host) {
        host.dispatchApplyWindowInsets(getWindowInsets(true));
    }

    private static boolean shouldUseDisplaySize(LayoutParams lp) {
        if (lp.type == 2014 || lp.type == 2011 || lp.type == 2020) {
            return true;
        }
        return false;
    }

    private int dipToPx(int dip) {
        return (int) ((this.mContext.getResources().getDisplayMetrics().density * ((float) dip)) + 0.5f);
    }

    /* JADX WARNING: Missing block: B:533:0x123f, code:
            if (r62 == false) goto L_0x09eb;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void performTraversals() {
        View host = this.mView;
        if (DEBUG_LIFECYCLE || DEBUG_DEFAULT) {
            Log.v(this.mTag, "ViewRoot performTraversals+ : mFirst = " + this.mFirst + ", this = " + this);
        }
        if (DBG) {
            System.out.println("======================================");
            System.out.println("performTraversals");
            host.debug();
        }
        if (host != null && this.mAdded) {
            boolean viewVisibilityChanged;
            boolean viewUserVisibilityChanged;
            Point size;
            int desiredWindowWidth;
            int desiredWindowHeight;
            Configuration config;
            int i;
            boolean computesInternalInsets;
            boolean triggerGlobalLayoutListener;
            this.mIsInTraversal = true;
            this.mWillDrawSoon = true;
            int windowSizeMayChange = false;
            boolean newSurface = false;
            boolean surfaceChanged = false;
            LayoutParams lp = this.mWindowAttributes;
            int viewVisibility = getHostVisibility();
            if (this.mFirst) {
                viewVisibilityChanged = false;
            } else if (this.mViewVisibility != viewVisibility || this.mNewSurfaceNeeded) {
                viewVisibilityChanged = true;
            } else {
                viewVisibilityChanged = this.mViewVisibilityChanged;
            }
            this.mViewVisibilityChanged = false;
            if (this.mFirst) {
                viewUserVisibilityChanged = false;
            } else {
                viewUserVisibilityChanged = (this.mViewVisibility == 0 ? 1 : null) != (viewVisibility == 0 ? 1 : null);
            }
            LayoutParams params = null;
            if (this.mWindowAttributesChanged) {
                this.mWindowAttributesChanged = false;
                surfaceChanged = true;
                params = lp;
            }
            if (this.mDisplay.getDisplayAdjustments().getCompatibilityInfo().supportsScreen() == this.mLastInCompatMode) {
                params = lp;
                this.mFullRedrawNeeded = true;
                this.mLayoutRequested = true;
                if (this.mLastInCompatMode) {
                    lp.privateFlags &= -129;
                    this.mLastInCompatMode = false;
                } else {
                    lp.privateFlags |= 128;
                    this.mLastInCompatMode = true;
                }
            }
            this.mWindowAttributesChangesFlag = 0;
            Rect frame = this.mWinFrame;
            if (this.mFirst) {
                this.mFullRedrawNeeded = true;
                this.mLayoutRequested = true;
                if (shouldUseDisplaySize(lp)) {
                    size = new Point();
                    this.mDisplay.getRealSize(size);
                    desiredWindowWidth = size.x;
                    desiredWindowHeight = size.y;
                } else {
                    config = this.mContext.getResources().getConfiguration();
                    desiredWindowWidth = dipToPx(config.screenWidthDp);
                    desiredWindowHeight = dipToPx(config.screenHeightDp);
                }
                this.mAttachInfo.mUse32BitDrawingCache = true;
                this.mAttachInfo.mHasWindowFocus = false;
                this.mAttachInfo.mWindowVisibility = viewVisibility;
                this.mAttachInfo.mRecomputeGlobalAttributes = false;
                this.mLastConfiguration.setTo(host.getResources().getConfiguration());
                this.mLastSystemUiVisibility = this.mAttachInfo.mSystemUiVisibility;
                if (this.mViewLayoutDirectionInitial == 2) {
                    host.setLayoutDirection(this.mLastConfiguration.getLayoutDirection());
                }
                host.dispatchAttachedToWindow(this.mAttachInfo, 0);
                this.mAttachInfo.mTreeObserver.dispatchOnWindowAttachedChange(true);
                dispatchApplyInsets(host);
            } else {
                desiredWindowWidth = frame.width();
                desiredWindowHeight = frame.height();
                if (!(desiredWindowWidth == this.mWidth && desiredWindowHeight == this.mHeight)) {
                    if (DEBUG_ORIENTATION || DEBUG_LAYOUT) {
                        Log.v(this.mTag, "View " + host + " resized to: " + frame + ", this = " + this);
                    }
                    this.mFullRedrawNeeded = true;
                    this.mLayoutRequested = true;
                    windowSizeMayChange = true;
                }
            }
            if (viewVisibilityChanged) {
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
            boolean insetsChanged = false;
            boolean layoutRequested = this.mLayoutRequested ? this.mStopped ? this.mReportNextDraw : true : false;
            if (layoutRequested) {
                boolean windowSizeMayChange2;
                Resources res = this.mView.getContext().getResources();
                if (this.mFirst) {
                    this.mAttachInfo.mInTouchMode = !this.mAddedTouchMode;
                    ensureTouchModeLocally(this.mAddedTouchMode);
                } else {
                    if (!this.mPendingOverscanInsets.equals(this.mAttachInfo.mOverscanInsets)) {
                        insetsChanged = true;
                    }
                    if (!this.mPendingContentInsets.equals(this.mAttachInfo.mContentInsets)) {
                        if (DEBUG_LAYOUT) {
                            Log.v(this.mTag, "Content insets changing from " + this.mPendingContentInsets + " to: " + this.mAttachInfo.mContentInsets + ", this = " + this);
                        }
                        insetsChanged = true;
                    }
                    if (!this.mPendingStableInsets.equals(this.mAttachInfo.mStableInsets)) {
                        insetsChanged = true;
                    }
                    if (!this.mPendingVisibleInsets.equals(this.mAttachInfo.mVisibleInsets)) {
                        this.mAttachInfo.mVisibleInsets.set(this.mPendingVisibleInsets);
                        if (DEBUG_LAYOUT) {
                            Log.v(this.mTag, "Visible insets changing to: " + this.mAttachInfo.mVisibleInsets + ", mWinFrame = " + this.mWinFrame + ", this = " + this);
                        }
                    }
                    if (!this.mPendingOutsets.equals(this.mAttachInfo.mOutsets)) {
                        insetsChanged = true;
                    }
                    if (this.mPendingAlwaysConsumeNavBar != this.mAttachInfo.mAlwaysConsumeNavBar) {
                        insetsChanged = true;
                    }
                    if (lp.width == -2 || lp.height == -2) {
                        windowSizeMayChange2 = true;
                        if (shouldUseDisplaySize(lp)) {
                            size = new Point();
                            this.mDisplay.getRealSize(size);
                            desiredWindowWidth = size.x;
                            desiredWindowHeight = size.y;
                        } else {
                            config = res.getConfiguration();
                            desiredWindowWidth = dipToPx(config.screenWidthDp);
                            desiredWindowHeight = dipToPx(config.screenHeightDp);
                        }
                    }
                }
                windowSizeMayChange = windowSizeMayChange2 | measureHierarchy(host, lp, res, desiredWindowWidth, desiredWindowHeight);
            }
            if (collectViewAttributes()) {
                params = lp;
            }
            if (this.mAttachInfo.mForceReportNewAttributes) {
                this.mAttachInfo.mForceReportNewAttributes = false;
                params = lp;
            }
            if (this.mFirst || this.mAttachInfo.mViewVisibilityChanged) {
                this.mAttachInfo.mViewVisibilityChanged = false;
                int resizeMode = this.mSoftInputMode & 240;
                if (DEBUG_INPUT_RESIZE) {
                    Log.v(this.mTag, "Handle input resize mode start+ : lp.softInputMode = #" + Integer.toHexString(lp.softInputMode) + ", resizeMode = #" + Integer.toHexString(resizeMode) + ", this = " + this);
                }
                if (resizeMode == 0) {
                    int N = this.mAttachInfo.mScrollContainers.size();
                    for (i = 0; i < N; i++) {
                        if (((View) this.mAttachInfo.mScrollContainers.get(i)).isShown()) {
                            resizeMode = 16;
                        }
                    }
                    if (resizeMode == 0) {
                        resizeMode = 32;
                    }
                    if ((lp.softInputMode & 240) != resizeMode) {
                        lp.softInputMode = (lp.softInputMode & -241) | resizeMode;
                        params = lp;
                    }
                }
                if (DEBUG_INPUT_RESIZE) {
                    Log.v(this.mTag, "Handle input resize mode end- : lp.softInputMode = #" + Integer.toHexString(lp.softInputMode) + ", resizeMode = #" + Integer.toHexString(resizeMode) + ", this = " + this);
                }
            }
            if (params != null) {
                if (!((host.mPrivateFlags & 512) == 0 || PixelFormat.formatHasAlpha(params.format))) {
                    params.format = -3;
                }
                this.mAttachInfo.mOverscanRequested = (params.flags & 33554432) != 0;
            }
            if (this.mApplyInsetsRequested) {
                this.mApplyInsetsRequested = false;
                this.mLastOverscanRequested = this.mAttachInfo.mOverscanRequested;
                dispatchApplyInsets(host);
                if (this.mLayoutRequested) {
                    windowSizeMayChange |= measureHierarchy(host, lp, this.mView.getContext().getResources(), desiredWindowWidth, desiredWindowHeight);
                }
            }
            if (layoutRequested) {
                this.mLayoutRequested = false;
            }
            if (this.mSoftInputMayChanged) {
                insetsChanged = true;
                this.mSoftInputMayChanged = false;
            }
            boolean windowShouldResize = (!layoutRequested || windowSizeMayChange == 0) ? false : (this.mWidth == host.getMeasuredWidth() && this.mHeight == host.getMeasuredHeight() && (lp.width != -2 || frame.width() >= desiredWindowWidth || frame.width() == this.mWidth)) ? (lp.height != -2 || frame.height() >= desiredWindowHeight) ? false : frame.height() != this.mHeight : true;
            int i2 = (this.mDragResizing && this.mResizeMode == 0) ? 1 : 0;
            windowShouldResize = (windowShouldResize | i2) | this.mActivityRelaunched;
            if (this.mAttachInfo.mTreeObserver.hasComputeInternalInsetsListeners()) {
                computesInternalInsets = true;
            } else {
                computesInternalInsets = this.mAttachInfo.mHasNonEmptyGivenInternalInsets;
            }
            boolean insetsPending = false;
            int relayoutResult = 0;
            boolean updatedConfiguration = false;
            int surfaceGenerationId = this.mSurface.getGenerationId();
            boolean isViewVisible = viewVisibility == 0;
            if (DEBUG_LAYOUT) {
                Log.v(this.mTag, "ViewRoot adjustSize+ : mFirst = " + this.mFirst + ", windowShouldResize = " + windowShouldResize + ", insetsChanged = " + insetsChanged + ", viewVisibilityChanged = " + viewVisibilityChanged + ", params = " + params + ", mForceNextWindowRelayout = " + this.mForceNextWindowRelayout + ", isViewVisible = " + isViewVisible + ", this = " + this);
            }
            if (this.mFirst || windowShouldResize || insetsChanged || viewVisibilityChanged || params != null || this.mForceNextWindowRelayout || this.mRetryLayout) {
                this.mForceNextWindowRelayout = false;
                this.mHandler.removeMessages(101);
                this.mRetryLayout = false;
                if (isViewVisible) {
                    insetsPending = computesInternalInsets ? !this.mFirst ? viewVisibilityChanged : true : false;
                }
                if (this.mSurfaceHolder != null) {
                    this.mSurfaceHolder.mSurfaceLock.lock();
                    this.mDrawingAllowed = true;
                }
                boolean z = false;
                boolean contentInsetsChanged = false;
                boolean hadSurface = this.mSurface.isValid();
                try {
                    if (DEBUG_LAYOUT) {
                        Log.i(this.mTag, "host=w:" + host.getMeasuredWidth() + ", h:" + host.getMeasuredHeight() + ", params=" + params + " surface=" + this.mSurface + ",hadSurface = " + hadSurface + ", this = " + this);
                    }
                    Trace.traceBegin(8, "relayoutWindow");
                    if (this.mAttachInfo.mHardwareRenderer != null) {
                        if (this.mAttachInfo.mHardwareRenderer.pauseSurface(this.mSurface)) {
                            this.mDirty.set(0, 0, this.mWidth, this.mHeight);
                        }
                        this.mChoreographer.mFrameInfo.addFlags(1);
                    }
                    relayoutResult = relayoutWindow(params, viewVisibility, insetsPending);
                    if ((8388608 & relayoutResult) != 0) {
                        if (DEBUG_LAYOUT) {
                            Log.i(TAG, "relayoutResult RELAYOUT_NEED_RETRY, send MSG_RELAYOUT_RETRY message");
                        }
                        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(101), 200);
                    }
                    if (DEBUG_LAYOUT) {
                        Log.v(this.mTag, "relayout: frame=" + frame.toShortString() + " overscan=" + this.mPendingOverscanInsets.toShortString() + " content=" + this.mPendingContentInsets.toShortString() + " visible=" + this.mPendingVisibleInsets.toShortString() + " visible=" + this.mPendingStableInsets.toShortString() + " outsets=" + this.mPendingOutsets.toShortString() + " surface=" + this.mSurface + " valid = " + this.mSurface.isValid() + " surfaceGenerationId = " + surfaceGenerationId + " relayoutResult = " + relayoutResult + " this = " + this);
                    }
                    Trace.traceEnd(8);
                    if (this.mPendingConfiguration.seq != 0) {
                        if (DEBUG_CONFIGURATION) {
                            Log.v(this.mTag, "Visible with new config: " + this.mPendingConfiguration + ", this = " + this);
                        }
                        updateConfiguration(new Configuration(this.mPendingConfiguration), !this.mFirst);
                        this.mPendingConfiguration.seq = 0;
                        updatedConfiguration = true;
                    }
                    boolean overscanInsetsChanged = !this.mPendingOverscanInsets.equals(this.mAttachInfo.mOverscanInsets);
                    contentInsetsChanged = !this.mPendingContentInsets.equals(this.mAttachInfo.mContentInsets);
                    boolean visibleInsetsChanged = !this.mPendingVisibleInsets.equals(this.mAttachInfo.mVisibleInsets);
                    boolean stableInsetsChanged = !this.mPendingStableInsets.equals(this.mAttachInfo.mStableInsets);
                    boolean outsetsChanged = !this.mPendingOutsets.equals(this.mAttachInfo.mOutsets);
                    boolean surfaceSizeChanged = (relayoutResult & 32) != 0;
                    boolean alwaysConsumeNavBarChanged = this.mPendingAlwaysConsumeNavBar != this.mAttachInfo.mAlwaysConsumeNavBar;
                    if (contentInsetsChanged) {
                        this.mAttachInfo.mContentInsets.set(this.mPendingContentInsets);
                        if (DEBUG_LAYOUT) {
                            Log.v(this.mTag, "Content insets changing to: " + this.mAttachInfo.mContentInsets + ", this = " + this);
                        }
                    }
                    if (overscanInsetsChanged) {
                        this.mAttachInfo.mOverscanInsets.set(this.mPendingOverscanInsets);
                        if (DEBUG_LAYOUT) {
                            Log.v(this.mTag, "Overscan insets changing to: " + this.mAttachInfo.mOverscanInsets);
                        }
                        contentInsetsChanged = true;
                    }
                    if (stableInsetsChanged) {
                        this.mAttachInfo.mStableInsets.set(this.mPendingStableInsets);
                        if (DEBUG_LAYOUT) {
                            Log.v(this.mTag, "Decor insets changing to: " + this.mAttachInfo.mStableInsets);
                        }
                        contentInsetsChanged = true;
                    }
                    if (alwaysConsumeNavBarChanged) {
                        this.mAttachInfo.mAlwaysConsumeNavBar = this.mPendingAlwaysConsumeNavBar;
                        contentInsetsChanged = true;
                    }
                    if (!contentInsetsChanged && this.mLastSystemUiVisibility == this.mAttachInfo.mSystemUiVisibility) {
                        if (!this.mApplyInsetsRequested) {
                            if (this.mLastOverscanRequested == this.mAttachInfo.mOverscanRequested) {
                            }
                        }
                    }
                    this.mLastSystemUiVisibility = this.mAttachInfo.mSystemUiVisibility;
                    this.mLastOverscanRequested = this.mAttachInfo.mOverscanRequested;
                    this.mAttachInfo.mOutsets.set(this.mPendingOutsets);
                    this.mApplyInsetsRequested = false;
                    dispatchApplyInsets(host);
                    if (visibleInsetsChanged) {
                        this.mAttachInfo.mVisibleInsets.set(this.mPendingVisibleInsets);
                        if (DEBUG_LAYOUT) {
                            Log.v(this.mTag, "Visible insets changing to: " + this.mAttachInfo.mVisibleInsets + ", this = " + this);
                        }
                    }
                    if (hadSurface) {
                        if (!this.mSurface.isValid()) {
                            if (this.mLastScrolledFocus != null) {
                                this.mLastScrolledFocus.clear();
                            }
                            if (DEBUG_DRAW) {
                                Log.v(this.mTag, "Surface invalid: " + this.mSurface + ",surfaceGenerationId = " + surfaceGenerationId + ", mAttachInfo.mHardwareRenderer = " + this.mAttachInfo.mHardwareRenderer + ",this = " + this);
                            }
                            this.mCurScrollY = 0;
                            this.mScrollY = 0;
                            if (this.mView instanceof RootViewSurfaceTaker) {
                                ((RootViewSurfaceTaker) this.mView).onRootViewScrollYChanged(this.mCurScrollY);
                            }
                            if (this.mScroller != null) {
                                this.mScroller.abortAnimation();
                            }
                            if (this.mAttachInfo.mHardwareRenderer != null && this.mAttachInfo.mHardwareRenderer.isEnabled()) {
                                this.mAttachInfo.mHardwareRenderer.destroy();
                            }
                        } else if ((surfaceGenerationId != this.mSurface.getGenerationId() || surfaceSizeChanged) && this.mSurfaceHolder == null && this.mAttachInfo.mHardwareRenderer != null) {
                            this.mFullRedrawNeeded = true;
                            try {
                                this.mAttachInfo.mHardwareRenderer.updateSurface(this.mSurface);
                            } catch (OutOfResourcesException e) {
                                handleOutOfResourcesException(e);
                                return;
                            }
                        }
                    } else if (this.mSurface.isValid()) {
                        if (DEBUG_DRAW) {
                            Log.v(this.mTag, "Create new surface: " + this.mSurface + ",surfaceGenerationId = " + surfaceGenerationId + ", mSurface.getGenerationId() = " + this.mSurface.getGenerationId() + ",mPreviousTransparentRegion = " + this.mPreviousTransparentRegion + ",this = " + this);
                        }
                        if (lp.type != 3) {
                            newSurface = true;
                        }
                        this.mFullRedrawNeeded = true;
                        this.mPreviousTransparentRegion.setEmpty();
                        if (this.mAttachInfo.mHardwareRenderer != null) {
                            synchronized (this.mGraphicLock) {
                                try {
                                    Trace.traceBegin(8, "HW init");
                                    z = this.mAttachInfo.mHardwareRenderer.initialize(this.mSurface);
                                    if (z && (host.mPrivateFlags & 512) == 0) {
                                        this.mSurface.allocateBuffers();
                                    }
                                    Trace.traceEnd(8);
                                } catch (OutOfResourcesException e2) {
                                    handleOutOfResourcesException(e2);
                                    return;
                                }
                            }
                        }
                    }
                    boolean freeformResizing = (relayoutResult & 16) != 0;
                    boolean dragResizing = !freeformResizing ? (relayoutResult & 8) != 0 : true;
                    if (this.mDragResizing != dragResizing) {
                        if (dragResizing) {
                            if (freeformResizing) {
                                i2 = 0;
                            } else {
                                i2 = 1;
                            }
                            this.mResizeMode = i2;
                            startDragResizing(this.mPendingBackDropFrame, this.mWinFrame.equals(this.mPendingBackDropFrame), this.mPendingVisibleInsets, this.mPendingStableInsets, this.mResizeMode);
                        } else {
                            endDragResizing();
                        }
                    }
                } catch (Throwable e3) {
                    Log.e(this.mTag, "RemoteException happens in " + this, e3);
                }
                if (DEBUG_ORIENTATION || DEBUG_LAYOUT) {
                    Log.v(this.mTag, "Relayout returned: frame=" + frame + ", surface=" + this.mSurface + ", this = " + this);
                }
                this.mAttachInfo.mWindowLeft = frame.left;
                this.mAttachInfo.mWindowTop = frame.top;
                if (!(this.mWidth == frame.width() && this.mHeight == frame.height())) {
                    this.mWidth = frame.width();
                    this.mHeight = frame.height();
                }
                if (this.mSurfaceHolder != null) {
                    if (this.mSurface.isValid()) {
                        this.mSurfaceHolder.mSurface = this.mSurface;
                    }
                    this.mSurfaceHolder.setSurfaceFrameSize(this.mWidth, this.mHeight);
                    this.mSurfaceHolder.mSurfaceLock.unlock();
                    SurfaceHolder.Callback[] callbacks;
                    if (this.mSurface.isValid()) {
                        if (!hadSurface) {
                            this.mSurfaceHolder.ungetCallbacks();
                            this.mIsCreating = true;
                            this.mSurfaceHolderCallback.surfaceCreated(this.mSurfaceHolder);
                            callbacks = this.mSurfaceHolder.getCallbacks();
                            if (callbacks != null) {
                                for (SurfaceHolder.Callback c : callbacks) {
                                    c.surfaceCreated(this.mSurfaceHolder);
                                }
                            }
                            surfaceChanged = true;
                        }
                        if (surfaceChanged || surfaceGenerationId != this.mSurface.getGenerationId()) {
                            this.mSurfaceHolderCallback.surfaceChanged(this.mSurfaceHolder, lp.format, this.mWidth, this.mHeight);
                            callbacks = this.mSurfaceHolder.getCallbacks();
                            if (callbacks != null) {
                                for (SurfaceHolder.Callback c2 : callbacks) {
                                    c2.surfaceChanged(this.mSurfaceHolder, lp.format, this.mWidth, this.mHeight);
                                }
                            }
                        }
                        this.mIsCreating = false;
                    } else if (hadSurface) {
                        this.mSurfaceHolder.ungetCallbacks();
                        callbacks = this.mSurfaceHolder.getCallbacks();
                        this.mSurfaceHolderCallback.surfaceDestroyed(this.mSurfaceHolder);
                        if (callbacks != null) {
                            for (SurfaceHolder.Callback c22 : callbacks) {
                                c22.surfaceDestroyed(this.mSurfaceHolder);
                            }
                        }
                        this.mSurfaceHolder.mSurfaceLock.lock();
                        try {
                            this.mSurfaceHolder.mSurface = new Surface();
                        } finally {
                            this.mSurfaceHolder.mSurfaceLock.unlock();
                        }
                    }
                }
                ThreadedRenderer hardwareRenderer = this.mAttachInfo.mHardwareRenderer;
                if (hardwareRenderer != null && hardwareRenderer.isEnabled() && (z || this.mWidth != hardwareRenderer.getWidth() || this.mHeight != hardwareRenderer.getHeight() || this.mNeedsHwRendererSetup)) {
                    hardwareRenderer.setup(this.mWidth, this.mHeight, this.mAttachInfo, this.mWindowAttributes.surfaceInsets);
                    this.mNeedsHwRendererSetup = false;
                }
                if (!this.mStopped || this.mReportNextDraw) {
                    if (ensureTouchModeLocally((relayoutResult & 1) != 0) || this.mWidth != host.getMeasuredWidth() || this.mHeight != host.getMeasuredHeight() || contentInsetsChanged || updatedConfiguration) {
                        int childWidthMeasureSpec = getRootMeasureSpec(this.mWidth, lp.width);
                        int childHeightMeasureSpec = getRootMeasureSpec(this.mHeight, lp.height);
                        if (DEBUG_LAYOUT) {
                            Log.v(this.mTag, "Ooops, something changed!  mWidth=" + this.mWidth + " measuredWidth=" + host.getMeasuredWidth() + " mHeight=" + this.mHeight + " measuredHeight=" + host.getMeasuredHeight() + " coveredInsetsChanged=" + contentInsetsChanged + " this = " + this);
                        }
                        performMeasure(childWidthMeasureSpec, childHeightMeasureSpec);
                        int width = host.getMeasuredWidth();
                        int height = host.getMeasuredHeight();
                        boolean measureAgain = false;
                        if (lp.horizontalWeight > 0.0f) {
                            width += (int) (((float) (this.mWidth - width)) * lp.horizontalWeight);
                            childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(width, 1073741824);
                            measureAgain = true;
                        }
                        if (lp.verticalWeight > 0.0f) {
                            height += (int) (((float) (this.mHeight - height)) * lp.verticalWeight);
                            childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(height, 1073741824);
                            measureAgain = true;
                        }
                        if (measureAgain) {
                            if (DEBUG_LAYOUT) {
                                Log.v(this.mTag, "And hey let's measure once more: width=" + width + " height=" + height + " this = " + this);
                            }
                            performMeasure(childWidthMeasureSpec, childHeightMeasureSpec);
                        }
                        layoutRequested = true;
                    }
                }
            } else {
                maybeHandleWindowMove(frame);
            }
            if (DEBUG_LAYOUT) {
                Log.v(this.mTag, "ViewRoot adjustSize- , frame = " + frame + ", host.getMeasuredWidth() = " + host.getMeasuredWidth() + ", host.getMeasuredHeight() = " + host.getMeasuredHeight() + ", this = " + this);
            }
            boolean didLayout = layoutRequested ? this.mStopped ? this.mReportNextDraw : true : false;
            if (didLayout) {
                triggerGlobalLayoutListener = true;
            } else {
                triggerGlobalLayoutListener = this.mAttachInfo.mRecomputeGlobalAttributes;
            }
            if (DEBUG_LAYOUT || DEBUG_MEASURE_LAYOUT) {
                Log.v(this.mTag, "ViewRoot layout+ : " + host + ", layoutRequested = " + layoutRequested + ", mStopped = " + this.mStopped + ", this = " + this);
            }
            if (didLayout) {
                performLayout(lp, this.mWidth, this.mHeight);
                if ((host.mPrivateFlags & 512) != 0) {
                    host.getLocationInWindow(this.mTmpLocation);
                    if (host instanceof ViewGroup) {
                        if (getChildCount((ViewGroup) host) < 10) {
                            try {
                                if (this.mBasePackageName != null && this.mBasePackageName.length() < 90) {
                                    this.mWindowSession.setLastSurfaceAppName(this.mBasePackageName);
                                }
                            } catch (RemoteException e4) {
                            }
                        }
                    }
                    this.mTransparentRegion.set(this.mTmpLocation[0], this.mTmpLocation[1], (this.mTmpLocation[0] + host.mRight) - host.mLeft, (this.mTmpLocation[1] + host.mBottom) - host.mTop);
                    if (DEBUG_DRAW || DBG_TRANSP) {
                        Log.v(this.mTag, "ViewRoot gatherTransparentRegion+ : mTransparentRegion = " + this.mTransparentRegion + ", mTmpLocation = " + this.mTmpLocation + ",host = " + host + ", this = " + this);
                    }
                    host.gatherTransparentRegion(this.mTransparentRegion);
                    if (this.mTranslator != null) {
                        this.mTranslator.translateRegionInWindowToScreen(this.mTransparentRegion);
                    }
                    if (DEBUG_DRAW || DBG_TRANSP) {
                        Log.v(this.mTag, "ViewRoot gatherTransparentRegion- : mTransparentRegion = " + this.mTransparentRegion + ",mWindow = " + this.mWindow + ", mPreviousTransparentRegion = " + this.mPreviousTransparentRegion + ", this = " + this);
                    }
                    if (!this.mTransparentRegion.equals(this.mPreviousTransparentRegion)) {
                        this.mPreviousTransparentRegion.set(this.mTransparentRegion);
                        this.mFullRedrawNeeded = true;
                        try {
                            this.mWindowSession.setTransparentRegion(this.mWindow, this.mTransparentRegion);
                        } catch (Throwable e32) {
                            Log.e(this.mTag, "Exception in " + this + " when set transparent region.", e32);
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
                this.mAttachInfo.mRecomputeGlobalAttributes = false;
                this.mAttachInfo.mTreeObserver.dispatchOnGlobalLayout();
            }
            if (DEBUG_LAYOUT || DEBUG_MEASURE_LAYOUT) {
                View content = null;
                if (host instanceof ViewGroup) {
                    content = ((ViewGroup) host).getChildAt(0);
                }
                String logString = "ViewRoot layout- : host.mLeft=" + host.mLeft + ", host.mRight=" + host.mRight + ", host.mTop=" + host.mTop + ", host.mBottom=" + host.mBottom + ", computesInternalInsets = " + computesInternalInsets + ",content = " + content + ", this = " + this;
                if (content != null) {
                    logString = logString + ",content padding = (" + content.mPaddingLeft + "," + content.mPaddingTop + "," + content.mPaddingRight + "," + content.mPaddingBottom + ")";
                }
                Log.v(this.mTag, logString);
            }
            if (computesInternalInsets) {
                InternalInsetsInfo insets = this.mAttachInfo.mGivenInternalInsets;
                insets.reset();
                this.mAttachInfo.mTreeObserver.dispatchOnComputeInternalInsets(insets);
                this.mAttachInfo.mHasNonEmptyGivenInternalInsets = !insets.isEmpty();
                if (insetsPending || !this.mLastGivenInsets.equals(insets)) {
                    Rect contentInsets;
                    Rect visibleInsets;
                    Region touchableRegion;
                    this.mLastGivenInsets.set(insets);
                    if (this.mTranslator != null) {
                        contentInsets = this.mTranslator.getTranslatedContentInsets(insets.contentInsets);
                        visibleInsets = this.mTranslator.getTranslatedVisibleInsets(insets.visibleInsets);
                        touchableRegion = this.mTranslator.getTranslatedTouchableArea(insets.touchableRegion);
                    } else {
                        contentInsets = insets.contentInsets;
                        visibleInsets = insets.visibleInsets;
                        touchableRegion = insets.touchableRegion;
                    }
                    try {
                        this.mWindowSession.setInsets(this.mWindow, insets.mTouchableInsets, contentInsets, visibleInsets, touchableRegion);
                    } catch (Throwable e322) {
                        Log.e(this.mTag, "RemoteException happens when setInsets, mWindow = " + this.mWindow + ", contentInsets = " + contentInsets + ", visibleInsets = " + visibleInsets + ", touchableRegion = " + touchableRegion + ", this = " + this, e322);
                    }
                }
            }
            if (this.mFirst) {
                if (DEBUG_INPUT_RESIZE) {
                    Log.v(this.mTag, "First: mView.hasFocus()=" + (this.mView != null ? Boolean.valueOf(this.mView.hasFocus()) : "null") + ", relayoutResult = " + relayoutResult + ", this = " + this);
                }
                if (this.mView != null) {
                    if (!this.mView.hasFocus()) {
                        this.mView.requestFocus(2);
                        if (DEBUG_INPUT_RESIZE) {
                            Log.v(this.mTag, "First: requested focused view=" + this.mView.findFocus());
                        }
                    } else if (DEBUG_INPUT_RESIZE) {
                        Log.v(this.mTag, "First: existing focused view=" + this.mView.findFocus());
                    }
                }
                if (!(this.mWindowAttributes == null || this.mWindowAttributes.getTitle() == null || !this.mWindowAttributes.getTitle().toString().contains("com.tencent.mm.ui.LauncherUI"))) {
                    this.mIsWeixinLauncherUI = true;
                }
                if (!(this.mWindowAttributes == null || this.mWindowAttributes.getTitle() == null || !this.mWindowAttributes.getTitle().toString().contains("com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyReceiveUI"))) {
                    this.mIsLuckyMoneyView = true;
                }
            }
            boolean changedVisibility = (viewVisibilityChanged || this.mFirst) ? isViewVisible : false;
            boolean hasWindowFocus = this.mAttachInfo.mHasWindowFocus ? isViewVisible : false;
            boolean regainedFocus = hasWindowFocus ? this.mLostWindowFocus : false;
            if (regainedFocus) {
                this.mLostWindowFocus = false;
            } else if (!hasWindowFocus && this.mHadWindowFocus) {
                this.mLostWindowFocus = true;
            }
            if (changedVisibility || regainedFocus) {
                boolean isToast = this.mWindowAttributes == null ? false : this.mWindowAttributes.type == 2005;
                if (!isToast) {
                    host.sendAccessibilityEvent(32);
                }
            }
            this.mFirst = false;
            this.mWillDrawSoon = false;
            this.mNewSurfaceNeeded = false;
            this.mActivityRelaunched = false;
            this.mViewVisibility = viewVisibility;
            this.mHadWindowFocus = hasWindowFocus;
            if (hasWindowFocus && !isInLocalFocusMode()) {
                boolean imTarget = LayoutParams.mayUseInputMethod(this.mWindowAttributes.flags);
                if (imTarget != this.mLastWasImTarget) {
                    this.mLastWasImTarget = imTarget;
                    InputMethodManager imm = InputMethodManager.peekInstance();
                    if (imm != null && imTarget) {
                        imm.onPreWindowFocus(this.mView, hasWindowFocus);
                        imm.onPostWindowFocus(this.mView, this.mView.findFocus(), this.mWindowAttributes.softInputMode, !this.mHasHadWindowFocus, this.mWindowAttributes.flags);
                    }
                }
            }
            if ((relayoutResult & 2) != 0) {
                this.mReportNextDraw = true;
            }
            boolean cancelDraw = this.mAttachInfo.mTreeObserver.dispatchOnPreDraw() || !isViewVisible;
            if (DEBUG_DRAW) {
                Log.v(this.mTag, "ViewRoot draw+ , cancelDraw=" + cancelDraw + ", isViewVisible = " + isViewVisible + ", newSurface = " + newSurface + ", mFullRedrawNeeded = " + this.mFullRedrawNeeded + ", mReportNextDraw = " + this.mReportNextDraw + ", this = " + this);
            }
            if (!cancelDraw && !newSurface) {
                if (this.mPendingTransitions != null && this.mPendingTransitions.size() > 0) {
                    for (i = 0; i < this.mPendingTransitions.size(); i++) {
                        ((LayoutTransition) this.mPendingTransitions.get(i)).startChangingAnimations();
                    }
                    this.mPendingTransitions.clear();
                }
                performDraw();
            } else if (isViewVisible) {
                scheduleTraversals();
            } else if (this.mPendingTransitions != null && this.mPendingTransitions.size() > 0) {
                for (i = 0; i < this.mPendingTransitions.size(); i++) {
                    ((LayoutTransition) this.mPendingTransitions.get(i)).endChangingAnimations();
                }
                this.mPendingTransitions.clear();
            }
            if (DEBUG_DRAW || DEBUG_LIFECYCLE) {
                long frameCount = -999;
                String rendererAddr = "0x0";
                if (!(this.mAttachInfo == null || this.mAttachInfo.mHardwareRenderer == null || !this.mAttachInfo.mHardwareRenderer.isEnabled())) {
                    frameCount = (long) this.mFrame;
                }
                Log.v(this.mTag, "ViewRoot performTraversals and draw- : this = " + this + " frame#" + frameCount + " (cancelDraw = " + cancelDraw + ")");
                this.mFrame++;
            }
            this.mIsInTraversal = false;
        }
    }

    private void maybeHandleWindowMove(Rect frame) {
        boolean windowMoved = true;
        if (this.mAttachInfo.mWindowLeft == frame.left && this.mAttachInfo.mWindowTop == frame.top) {
            windowMoved = false;
        }
        if (windowMoved) {
            if (this.mTranslator != null) {
                this.mTranslator.translateRectInScreenToAppWinFrame(frame);
            }
            this.mAttachInfo.mWindowLeft = frame.left;
            this.mAttachInfo.mWindowTop = frame.top;
        }
        if (windowMoved || this.mAttachInfo.mNeedsUpdateLightCenter) {
            if (this.mAttachInfo.mHardwareRenderer != null) {
                this.mAttachInfo.mHardwareRenderer.setLightCenter(this.mAttachInfo);
            }
            this.mAttachInfo.mNeedsUpdateLightCenter = false;
        }
    }

    private void handleOutOfResourcesException(OutOfResourcesException e) {
        Log.e(this.mTag, "OutOfResourcesException initializing HW surface", e);
        try {
            if (!(this.mWindowSession.outOfMemory(this.mWindow) || Process.myUid() == 1000)) {
                Slog.w(this.mTag, "No processes killed for memory; killing self");
                Process.killProcess(Process.myPid());
            }
        } catch (RemoteException e2) {
        }
        this.mLayoutRequested = true;
    }

    private void performMeasure(int childWidthMeasureSpec, int childHeightMeasureSpec) {
        Trace.traceBegin(8, "measure");
        try {
            this.mView.measure(childWidthMeasureSpec, childHeightMeasureSpec);
        } finally {
            Trace.traceEnd(8);
        }
    }

    boolean isInLayout() {
        return this.mInLayout;
    }

    boolean requestLayoutDuringLayout(View view) {
        if (view.mParent == null || view.mAttachInfo == null) {
            return true;
        }
        if (!this.mLayoutRequesters.contains(view)) {
            this.mLayoutRequesters.add(view);
        }
        if (this.mHandlingLayoutInLayoutRequest) {
            return false;
        }
        return true;
    }

    private void performLayout(LayoutParams lp, int desiredWindowWidth, int desiredWindowHeight) {
        this.mLayoutRequested = false;
        this.mScrollMayChange = true;
        this.mInLayout = true;
        View host = this.mView;
        if (DEBUG_ORIENTATION || DEBUG_LAYOUT) {
            Log.v(this.mTag, "Laying out " + host + " in " + this + " to (" + host.getMeasuredWidth() + ", " + host.getMeasuredHeight() + ")");
        }
        Trace.traceBegin(8, "layout");
        try {
            host.layout(0, 0, host.getMeasuredWidth(), host.getMeasuredHeight());
            this.mInLayout = false;
            if (this.mLayoutRequesters.size() > 0) {
                ArrayList<View> validLayoutRequesters = getValidLayoutRequesters(this.mLayoutRequesters, false);
                if (validLayoutRequesters != null) {
                    this.mHandlingLayoutInLayoutRequest = true;
                    int numValidRequests = validLayoutRequesters.size();
                    for (int i = 0; i < numValidRequests; i++) {
                        View view = (View) validLayoutRequesters.get(i);
                        if (DEBUG_PANIC) {
                            Log.w("View", "requestLayout() improperly called by " + view + " during layout: running second layout pass");
                        }
                        view.requestLayout();
                    }
                    measureHierarchy(host, lp, this.mView.getContext().getResources(), desiredWindowWidth, desiredWindowHeight);
                    this.mInLayout = true;
                    host.layout(0, 0, host.getMeasuredWidth(), host.getMeasuredHeight());
                    this.mHandlingLayoutInLayoutRequest = false;
                    validLayoutRequesters = getValidLayoutRequesters(this.mLayoutRequesters, true);
                    if (validLayoutRequesters != null) {
                        ArrayList<View> finalRequesters = validLayoutRequesters;
                        getRunQueue().post(new Runnable() {
                            public void run() {
                                int numValidRequests = validLayoutRequesters.size();
                                for (int i = 0; i < numValidRequests; i++) {
                                    View view = (View) validLayoutRequesters.get(i);
                                    if (ViewRootImpl.DEBUG_PANIC) {
                                        Log.w("View", "requestLayout() improperly called by " + view + " during second layout pass: posting in next frame");
                                    }
                                    view.requestLayout();
                                }
                            }
                        });
                    }
                }
            }
            Trace.traceEnd(8);
            this.mInLayout = false;
        } catch (Throwable th) {
            Trace.traceEnd(8);
        }
    }

    private ArrayList<View> getValidLayoutRequesters(ArrayList<View> layoutRequesters, boolean secondLayoutRequests) {
        int i;
        View view;
        int numViewsRequestingLayout = layoutRequesters.size();
        ArrayList<View> validLayoutRequesters = null;
        for (i = 0; i < numViewsRequestingLayout; i++) {
            view = (View) layoutRequesters.get(i);
            if (!(view == null || view.mAttachInfo == null || view.mParent == null || (!secondLayoutRequests && (view.mPrivateFlags & 4096) != 4096))) {
                boolean gone = false;
                View parent = view;
                while (parent != null) {
                    if ((parent.mViewFlags & 12) == 8) {
                        gone = true;
                        break;
                    } else if (parent.mParent instanceof View) {
                        parent = parent.mParent;
                    } else {
                        parent = null;
                    }
                }
                if (!gone) {
                    if (validLayoutRequesters == null) {
                        validLayoutRequesters = new ArrayList();
                    }
                    validLayoutRequesters.add(view);
                }
            }
        }
        if (!secondLayoutRequests) {
            for (i = 0; i < numViewsRequestingLayout; i++) {
                view = (View) layoutRequesters.get(i);
                while (view != null && (view.mPrivateFlags & 4096) != 0) {
                    view.mPrivateFlags &= -4097;
                    if (view.mParent instanceof View) {
                        view = (View) view.mParent;
                    } else {
                        view = null;
                    }
                }
            }
        }
        layoutRequesters.clear();
        return validLayoutRequesters;
    }

    public void requestTransparentRegion(View child) {
        checkThread();
        if (this.mView == child) {
            View view = this.mView;
            view.mPrivateFlags |= 512;
            this.mWindowAttributesChanged = true;
            this.mWindowAttributesChangesFlag = 0;
            requestLayout();
        }
    }

    private static int getRootMeasureSpec(int windowSize, int rootDimension) {
        switch (rootDimension) {
            case -2:
                return MeasureSpec.makeMeasureSpec(windowSize, Integer.MIN_VALUE);
            case -1:
                return MeasureSpec.makeMeasureSpec(windowSize, 1073741824);
            default:
                return MeasureSpec.makeMeasureSpec(rootDimension, 1073741824);
        }
    }

    public void onHardwarePreDraw(DisplayListCanvas canvas) {
        canvas.translate((float) (-this.mHardwareXOffset), (float) (-this.mHardwareYOffset));
    }

    public void onHardwarePostDraw(DisplayListCanvas canvas) {
        if (DEBUG_DRAW) {
            Log.v(this.mTag, "onHardwarePostDraw: this = " + this);
        }
        drawAccessibilityFocusedDrawableIfNeeded(canvas);
        for (int i = this.mWindowCallbacks.size() - 1; i >= 0; i--) {
            ((WindowCallbacks) this.mWindowCallbacks.get(i)).onPostDraw(canvas);
        }
    }

    void outputDisplayList(View view) {
        view.mRenderNode.output();
        if (this.mAttachInfo.mHardwareRenderer != null) {
            this.mAttachInfo.mHardwareRenderer.serializeDisplayListTree();
        }
    }

    private void profileRendering(boolean enabled) {
        if (this.mProfileRendering) {
            this.mRenderProfilingEnabled = enabled;
            if (this.mRenderProfiler != null) {
                this.mChoreographer.removeFrameCallback(this.mRenderProfiler);
            }
            if (this.mRenderProfilingEnabled) {
                if (this.mRenderProfiler == null) {
                    this.mRenderProfiler = new AnonymousClass3(this);
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
            Log.v(this.mTag, "0x" + thisHash + "\tFPS:\t" + ((((float) this.mFpsNumFrames) * 1000.0f) / ((float) totalTime)));
            this.mFpsStartTime = nowTime;
            this.mFpsNumFrames = 0;
        }
    }

    boolean shouldOpenAllframeDraw() {
        return SystemProperties.getBoolean(PROP_NAME_OPEN_ALL_FRAMES, false);
    }

    private void performDraw() {
        if (this.mAttachInfo.mDisplayState == 1 && !this.mReportNextDraw) {
            if (this.mIsKeyguard && shouldOpenAllframeDraw()) {
                Log.d(TAG, " draw all frames ");
            } else {
                return;
            }
        }
        boolean fullRedrawNeeded = this.mFullRedrawNeeded;
        this.mFullRedrawNeeded = false;
        this.mIsDrawing = true;
        Trace.traceBegin(4104, "draw");
        try {
            if (!(DBG_APP_FAST_LAUNCH_ENHANCE && this.mWindowAttributes.type == 3 && this.mWindowAttributes.getTitle().equals("FastStarting"))) {
                draw(fullRedrawNeeded);
            }
            this.mIsDrawing = false;
            Trace.traceEnd(4104);
            if (this.mAttachInfo.mPendingAnimatingRenderNodes != null) {
                int count = this.mAttachInfo.mPendingAnimatingRenderNodes.size();
                for (int i = 0; i < count; i++) {
                    ((RenderNode) this.mAttachInfo.mPendingAnimatingRenderNodes.get(i)).endAllAnimators();
                }
                this.mAttachInfo.mPendingAnimatingRenderNodes.clear();
            }
            if (this.mReportNextDraw) {
                this.mReportNextDraw = false;
                if (this.mWindowDrawCountDown != null) {
                    try {
                        this.mWindowDrawCountDown.await();
                    } catch (InterruptedException e) {
                        Log.e(this.mTag, "Window redraw count down interruped!");
                    }
                    this.mWindowDrawCountDown = null;
                }
                if (this.mAttachInfo.mHardwareRenderer != null) {
                    this.mAttachInfo.mHardwareRenderer.fence();
                }
                if (this.mAttachInfo.mHardwareRenderer != null) {
                    this.mAttachInfo.mHardwareRenderer.setStopped(this.mStopped);
                }
                if (DEBUG_DRAW || DEBUG_LIGHT || DEBUG_PANIC) {
                    Log.v(this.mTag, "FINISHED DRAWING: " + this.mWindowAttributes.getTitle() + ", this = " + this);
                }
                if (this.mSurfaceHolder != null && this.mSurface.isValid()) {
                    this.mSurfaceHolderCallback.surfaceRedrawNeeded(this.mSurfaceHolder);
                    SurfaceHolder.Callback[] callbacks = this.mSurfaceHolder.getCallbacks();
                    if (callbacks != null) {
                        for (SurfaceHolder.Callback c : callbacks) {
                            if (c instanceof Callback2) {
                                ((Callback2) c).surfaceRedrawNeeded(this.mSurfaceHolder);
                            }
                        }
                    }
                }
                Trace.traceBegin(8, "finish draw");
                try {
                    this.mWindowSession.finishDrawing(this.mWindow);
                } catch (RemoteException e2) {
                    Log.e(this.mTag, "Exception when finish draw window " + this.mWindow + " in " + this, e2);
                }
                Trace.traceEnd(8);
            }
        } catch (Throwable th) {
            this.mIsDrawing = false;
            Trace.traceEnd(4104);
        }
    }

    private void draw(boolean fullRedrawNeeded) {
        Surface surface = this.mSurface;
        if (surface.isValid()) {
            int curScrollY;
            if (DEBUG_FPS) {
                trackFPS();
            }
            if (!sFirstDrawComplete) {
                synchronized (sFirstDrawHandlers) {
                    sFirstDrawComplete = true;
                    int count = sFirstDrawHandlers.size();
                    for (int i = 0; i < count; i++) {
                        this.mHandler.post((Runnable) sFirstDrawHandlers.get(i));
                    }
                }
            }
            scrollToRectOrFocus(null, false);
            if (this.mAttachInfo.mViewScrollChanged) {
                this.mAttachInfo.mViewScrollChanged = false;
                this.mAttachInfo.mTreeObserver.dispatchOnScrollChanged();
            }
            boolean animating = this.mScroller != null ? this.mScroller.computeScrollOffset() : false;
            if (animating) {
                curScrollY = this.mScroller.getCurrY();
            } else {
                curScrollY = this.mScrollY;
            }
            if (this.mCurScrollY != curScrollY) {
                this.mCurScrollY = curScrollY;
                fullRedrawNeeded = true;
                if (this.mView instanceof RootViewSurfaceTaker) {
                    ((RootViewSurfaceTaker) this.mView).onRootViewScrollYChanged(this.mCurScrollY);
                }
            }
            float appScale = this.mAttachInfo.mApplicationScale;
            boolean scalingRequired = this.mAttachInfo.mScalingRequired;
            Rect dirty = this.mDirty;
            if (this.mSurfaceHolder != null) {
                dirty.setEmpty();
                if (animating && this.mScroller != null) {
                    this.mScroller.abortAnimation();
                }
                return;
            }
            if (fullRedrawNeeded) {
                this.mAttachInfo.mIgnoreDirtyState = true;
                dirty.set(0, 0, (int) ((((float) this.mWidth) * appScale) + 0.5f), (int) ((((float) this.mHeight) * appScale) + 0.5f));
            }
            if (DEBUG_ORIENTATION || DEBUG_DRAW) {
                Log.v(this.mTag, "Draw " + this.mView + "/" + this.mWindowAttributes.getTitle() + ": dirty={" + dirty.left + "," + dirty.top + "," + dirty.right + "," + dirty.bottom + "} surface=" + surface + " surface.isValid()=" + surface.isValid() + ", appScale = " + appScale + ", width=" + this.mWidth + ", height=" + this.mHeight + ", mScrollY = " + this.mScrollY + ", mCurScrollY = " + this.mCurScrollY + ", animating = " + animating + ", mIsAnimating = " + this.mIsAnimating + ", this = " + this);
            }
            this.mAttachInfo.mTreeObserver.dispatchOnDraw();
            int xOffset = -this.mCanvasOffsetX;
            int yOffset = (-this.mCanvasOffsetY) + curScrollY;
            LayoutParams params = this.mWindowAttributes;
            Rect surfaceInsets = params != null ? params.surfaceInsets : null;
            if (surfaceInsets != null) {
                xOffset -= surfaceInsets.left;
                yOffset -= surfaceInsets.top;
                dirty.offset(surfaceInsets.left, surfaceInsets.right);
            }
            boolean accessibilityFocusDirty = false;
            Drawable drawable = this.mAttachInfo.mAccessibilityFocusDrawable;
            if (drawable != null) {
                Rect bounds = this.mAttachInfo.mTmpInvalRect;
                if (!getAccessibilityFocusedRect(bounds)) {
                    bounds.setEmpty();
                }
                if (!bounds.equals(drawable.getBounds())) {
                    accessibilityFocusDirty = true;
                }
            }
            try {
                this.mAttachInfo.mDrawingTime = this.mChoreographer.getFrameTimeNanos() / TimeUtils.NANOS_PER_MS;
            } catch (IllegalStateException e) {
                this.mAttachInfo.mDrawingTime = System.nanoTime() / TimeUtils.NANOS_PER_MS;
            }
            if (!dirty.isEmpty() || this.mIsAnimating || accessibilityFocusDirty) {
                if (this.mAttachInfo.mHardwareRenderer != null && this.mAttachInfo.mHardwareRenderer.isEnabled()) {
                    boolean invalidateRoot = !accessibilityFocusDirty ? this.mInvalidateRootRequested : true;
                    this.mInvalidateRootRequested = false;
                    this.mIsAnimating = false;
                    if (!(this.mHardwareYOffset == yOffset && this.mHardwareXOffset == xOffset)) {
                        this.mHardwareYOffset = yOffset;
                        this.mHardwareXOffset = xOffset;
                        invalidateRoot = true;
                    }
                    if (invalidateRoot) {
                        this.mAttachInfo.mHardwareRenderer.invalidateRoot();
                    }
                    dirty.setEmpty();
                    boolean updated = updateContentDrawBounds();
                    if (this.mReportNextDraw) {
                        this.mAttachInfo.mHardwareRenderer.setStopped(false);
                    }
                    if (updated) {
                        requestDrawWindow();
                    }
                    synchronized (this.mGraphicLock) {
                        if (this.mAttachInfo.mHardwareRenderer == null || !this.mAttachInfo.mHardwareRenderer.isEnabled()) {
                            Log.w(TAG, "HardwareRenderer have been destroy");
                        } else {
                            this.mAttachInfo.mHardwareRenderer.draw(this.mView, this.mAttachInfo, this);
                        }
                    }
                } else if (this.mAttachInfo.mHardwareRenderer == null || this.mAttachInfo.mHardwareRenderer.isEnabled() || !this.mAttachInfo.mHardwareRenderer.isRequested()) {
                    if (!drawSoftware(surface, this.mAttachInfo, xOffset, yOffset, scalingRequired, dirty)) {
                        if (DEBUG_DRAW) {
                            Log.v(this.mTag, "drawSoftware return: this = " + this);
                        }
                        return;
                    }
                } else {
                    try {
                        this.mAttachInfo.mHardwareRenderer.initializeIfNeeded(this.mWidth, this.mHeight, this.mAttachInfo, this.mSurface, surfaceInsets);
                        this.mFullRedrawNeeded = true;
                        scheduleTraversals();
                        return;
                    } catch (OutOfResourcesException e2) {
                        handleOutOfResourcesException(e2);
                        return;
                    }
                }
            }
            if (animating) {
                this.mFullRedrawNeeded = true;
                scheduleTraversals();
            }
        }
    }

    /* JADX WARNING: Missing block: B:46:0x018d, code:
            if (r2 == r20.bottom) goto L_0x0036;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean drawSoftware(Surface surface, AttachInfo attachInfo, int xoff, int yoff, boolean scalingRequired, Rect dirty) {
        try {
            int left = dirty.left;
            int top = dirty.top;
            int right = dirty.right;
            int bottom = dirty.bottom;
            Trace.traceBegin(8, "drawSoftware lockCanvas");
            Canvas canvas = this.mSurface.lockCanvas(dirty);
            Trace.traceEnd(8);
            if (left == dirty.left && top == dirty.top) {
                if (right == dirty.right) {
                }
            }
            attachInfo.mIgnoreDirtyState = true;
            canvas.setDensity(this.mDensity);
            try {
                if (DEBUG_ORIENTATION || DEBUG_DRAW) {
                    Log.v(this.mTag, "Surface " + surface + " drawing to bitmap w=" + canvas.getWidth() + ", h=" + canvas.getHeight() + ", this = " + this);
                }
                if (!(canvas.isOpaque() && yoff == 0 && xoff == 0)) {
                    canvas.drawColor(0, Mode.CLEAR);
                }
                dirty.setEmpty();
                this.mIsAnimating = false;
                View view = this.mView;
                view.mPrivateFlags |= 32;
                if (DEBUG_DRAW) {
                    Context cxt = this.mView.getContext();
                    Log.i(this.mTag, "Drawing view start+ : package:" + cxt.getPackageName() + ", metrics=" + cxt.getResources().getDisplayMetrics() + ", compatibilityInfo=" + cxt.getResources().getCompatibilityInfo() + ", this = " + this);
                }
                canvas.translate((float) (-xoff), (float) (-yoff));
                if (this.mTranslator != null) {
                    this.mTranslator.translateCanvas(canvas);
                }
                canvas.setScreenDensity(scalingRequired ? this.mNoncompatDensity : 0);
                attachInfo.mSetIgnoreDirtyState = false;
                this.mView.draw(canvas);
                drawAccessibilityFocusedDrawableIfNeeded(canvas);
                if (!attachInfo.mSetIgnoreDirtyState) {
                    attachInfo.mIgnoreDirtyState = false;
                }
                if (DEBUG_DRAW) {
                    Log.v(this.mTag, "Drawing view end- : mView = " + this.mView + ", this = " + this);
                }
                try {
                    surface.unlockCanvasAndPost(canvas);
                    if (DEBUG_DRAW) {
                        Log.v(this.mTag, "Surface " + surface + " unlockCanvasAndPost, this = " + this);
                    }
                    return true;
                } catch (IllegalArgumentException e) {
                    Log.e(this.mTag, "Could not unlock surface, surface = " + surface + ", canvas = " + canvas + ", this = " + this, e);
                    this.mLayoutRequested = true;
                    return false;
                }
            } catch (Throwable th) {
                try {
                    surface.unlockCanvasAndPost(canvas);
                    if (DEBUG_DRAW) {
                        Log.v(this.mTag, "Surface " + surface + " unlockCanvasAndPost, this = " + this);
                    }
                } catch (IllegalArgumentException e2) {
                    Log.e(this.mTag, "Could not unlock surface, surface = " + surface + ", canvas = " + canvas + ", this = " + this, e2);
                    this.mLayoutRequested = true;
                    return false;
                }
            }
        } catch (OutOfResourcesException e3) {
            handleOutOfResourcesException(e3);
            return false;
        } catch (IllegalArgumentException e22) {
            Log.e(this.mTag, "Could not lock surface", e22);
            this.mLayoutRequested = true;
            return false;
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

    private boolean getAccessibilityFocusedRect(Rect bounds) {
        boolean z = false;
        AccessibilityManager manager = AccessibilityManager.getInstance(this.mView.mContext);
        if (!manager.isEnabled() || !manager.isTouchExplorationEnabled()) {
            return false;
        }
        View host = this.mAccessibilityFocusedHost;
        if (host == null || host.mAttachInfo == null) {
            return false;
        }
        if (host.getAccessibilityNodeProvider() == null) {
            host.getBoundsOnScreen(bounds, true);
        } else if (this.mAccessibilityFocusedVirtualView == null) {
            return false;
        } else {
            this.mAccessibilityFocusedVirtualView.getBoundsInScreen(bounds);
        }
        AttachInfo attachInfo = this.mAttachInfo;
        bounds.offset(0, attachInfo.mViewRootImpl.mScrollY);
        bounds.offset(-attachInfo.mWindowLeft, -attachInfo.mWindowTop);
        if (!bounds.intersect(0, 0, attachInfo.mViewRootImpl.mWidth, attachInfo.mViewRootImpl.mHeight)) {
            bounds.setEmpty();
        }
        if (!bounds.isEmpty()) {
            z = true;
        }
        return z;
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

    public void requestInvalidateRootRenderNode() {
        this.mInvalidateRootRequested = true;
    }

    boolean scrollToRectOrFocus(Rect rectangle, boolean immediate) {
        Rect ci = this.mAttachInfo.mContentInsets;
        Rect vi = this.mAttachInfo.mVisibleInsets;
        int scrollY = 0;
        boolean handled = false;
        if (vi.left > ci.left || vi.top > ci.top || vi.right > ci.right || vi.bottom > ci.bottom) {
            scrollY = this.mScrollY;
            View focus = this.mView.findFocus();
            if (focus == null) {
                return false;
            }
            View lastScrolledFocus;
            if (this.mLastScrolledFocus != null) {
                lastScrolledFocus = (View) this.mLastScrolledFocus.get();
            } else {
                lastScrolledFocus = null;
            }
            if (focus != lastScrolledFocus) {
                rectangle = null;
            }
            if (DEBUG_INPUT_RESIZE) {
                Log.v(this.mTag, "Eval scroll: focus=" + focus + " rectangle=" + rectangle + " ci=" + ci + " vi=" + vi + " mScrollMayChange = " + this.mScrollMayChange + " mLastScrolledFocus = " + this.mLastScrolledFocus + " this = " + this);
            }
            if (focus != lastScrolledFocus || this.mScrollMayChange || rectangle != null) {
                this.mLastScrolledFocus = new WeakReference(focus);
                this.mScrollMayChange = false;
                if (DEBUG_INPUT_RESIZE) {
                    Log.v(this.mTag, "Need to scroll? mVisRect = " + this.mVisRect + " this = " + this);
                }
                if (focus.getGlobalVisibleRect(this.mVisRect, null)) {
                    if (DEBUG_INPUT_RESIZE) {
                        Log.v(this.mTag, "Root w=" + this.mView.getWidth() + " h=" + this.mView.getHeight() + " ci=" + ci.toShortString() + " vi=" + vi.toShortString() + " this = " + this);
                    }
                    if (rectangle == null) {
                        focus.getFocusedRect(this.mTempRect);
                        if (DEBUG_INPUT_RESIZE) {
                            Log.v(this.mTag, "Focus " + focus + ": focusRect=" + this.mTempRect.toShortString() + " this = " + this);
                        }
                        if (this.mView instanceof ViewGroup) {
                            ((ViewGroup) this.mView).offsetDescendantRectToMyCoords(focus, this.mTempRect);
                        }
                        if (DEBUG_INPUT_RESIZE) {
                            Log.v(this.mTag, "Focus in window: focusRect=" + this.mTempRect.toShortString() + " visRect=" + this.mVisRect.toShortString() + " this = " + this);
                        }
                    } else {
                        this.mTempRect.set(rectangle);
                        if (DEBUG_INPUT_RESIZE) {
                            Log.v(this.mTag, "Request scroll to rect: " + this.mTempRect.toShortString() + " visRect=" + this.mVisRect.toShortString() + " this = " + this);
                        }
                    }
                    if (this.mTempRect.intersect(this.mVisRect)) {
                        if (DEBUG_INPUT_RESIZE) {
                            Log.v(this.mTag, "Focus window visible rect: " + this.mTempRect.toShortString() + " this = " + this);
                        }
                        if (this.mTempRect.height() > (this.mView.getHeight() - vi.top) - vi.bottom) {
                            if (DEBUG_INPUT_RESIZE) {
                                Log.v(this.mTag, "Too tall; leaving scrollY=" + scrollY + ", this = " + this);
                            }
                        } else if (this.mTempRect.top < vi.top) {
                            scrollY = this.mTempRect.top - vi.top;
                            if (DEBUG_INPUT_RESIZE) {
                                Log.v(this.mTag, "Top covered; scrollY=" + scrollY + ", this = " + this);
                            }
                        } else if (this.mTempRect.bottom > this.mView.getHeight() - vi.bottom) {
                            scrollY = this.mTempRect.bottom - (this.mView.getHeight() - vi.bottom);
                            if (DEBUG_INPUT_RESIZE) {
                                Log.v(this.mTag, "Bottom covered; scrollY=" + scrollY + ", this = " + this);
                            }
                        } else {
                            scrollY = 0;
                        }
                        handled = true;
                    }
                }
            } else if (DEBUG_INPUT_RESIZE) {
                Log.v(this.mTag, "Keeping scroll y=" + this.mScrollY + " vi=" + vi.toShortString() + " this = " + this);
            }
        }
        if (scrollY != this.mScrollY) {
            if (DEBUG_INPUT_RESIZE) {
                Log.v(this.mTag, "Pan scroll changed: old=" + this.mScrollY + " , new=" + scrollY + ", this = " + this);
            }
            if (!immediate) {
                if (this.mScroller == null) {
                    this.mScroller = new Scroller(this.mView.getContext());
                }
                this.mScroller.startScroll(0, this.mScrollY, 0, scrollY - this.mScrollY);
            } else if (this.mScroller != null) {
                this.mScroller.abortAnimation();
            }
            this.mScrollY = scrollY;
        }
        return handled;
    }

    public View getAccessibilityFocusedHost() {
        return this.mAccessibilityFocusedHost;
    }

    public AccessibilityNodeInfo getAccessibilityFocusedVirtualView() {
        return this.mAccessibilityFocusedVirtualView;
    }

    void setAccessibilityFocus(View view, AccessibilityNodeInfo node) {
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
        if (!(this.mAccessibilityFocusedHost == null || this.mAccessibilityFocusedHost == view)) {
            this.mAccessibilityFocusedHost.clearAccessibilityFocusNoCallbacks(64);
        }
        this.mAccessibilityFocusedHost = view;
        this.mAccessibilityFocusedVirtualView = node;
        if (this.mAttachInfo.mHardwareRenderer != null) {
            this.mAttachInfo.mHardwareRenderer.invalidateRoot();
        }
    }

    public void requestChildFocus(View child, View focused) {
        if (DEBUG_INPUT_RESIZE) {
            Log.v(this.mTag, "Request child " + child + " focus: focus now " + focused + " in " + this);
        }
        checkThread();
        scheduleTraversals();
    }

    public void clearChildFocus(View child) {
        if (DEBUG_INPUT_RESIZE) {
            Log.v(this.mTag, "Clearing child focus");
        }
        checkThread();
        scheduleTraversals();
    }

    public ViewParent getParentForAccessibility() {
        return null;
    }

    public void focusableViewAvailable(View v) {
        checkThread();
        if (this.mView == null) {
            return;
        }
        if (this.mView.hasFocus()) {
            View focused = this.mView.findFocus();
            if ((focused instanceof ViewGroup) && ((ViewGroup) focused).getDescendantFocusability() == 262144 && isViewDescendantOf(v, focused)) {
                v.requestFocus();
                return;
            }
            return;
        }
        v.requestFocus();
    }

    public void recomputeViewAttributes(View child) {
        checkThread();
        if (this.mView == child) {
            this.mAttachInfo.mRecomputeGlobalAttributes = true;
            if (!this.mWillDrawSoon) {
                scheduleTraversals();
            }
        }
    }

    @OppoHook(level = OppoHookType.CHANGE_CODE, note = "JianHui.Yu@Plf.SDK, 2017-04-13 : Modify for Longshot", property = OppoRomType.ROM)
    void dispatchDetachedFromWindow() {
        this.mViewRootHooks.getLongshotViewRoot().releaseAnalyzer();
        if (!(this.mView == null || this.mView.mAttachInfo == null)) {
            this.mAttachInfo.mTreeObserver.dispatchOnWindowAttachedChange(false);
            this.mView.dispatchDetachedFromWindow();
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
        this.mSurface.release();
        if (!(this.mInputQueueCallback == null || this.mInputQueue == null)) {
            this.mInputQueueCallback.onInputQueueDestroyed(this.mInputQueue);
            this.mInputQueue.dispose();
            this.mInputQueueCallback = null;
            this.mInputQueue = null;
        }
        if (this.mInputEventReceiver != null) {
            this.mInputEventReceiver.dispose();
            this.mInputEventReceiver = null;
        }
        try {
            this.mWindowSession.remove(this.mWindow);
        } catch (RemoteException e) {
            Log.e(this.mTag, "RemoteException remove window " + this.mWindow + " in " + this, e);
        }
        if (this.mInputChannel != null) {
            this.mInputChannel.dispose();
            this.mInputChannel = null;
        }
        this.mDisplayManager.unregisterDisplayListener(this.mDisplayListener);
        unscheduleTraversals();
    }

    void updateConfiguration(Configuration config, boolean force) {
        if (DEBUG_CONFIGURATION) {
            Log.v(this.mTag, "Applying new config to window " + this.mWindowAttributes.getTitle() + ": " + config + ", force = " + force + ", this = " + this);
        }
        CompatibilityInfo ci = this.mDisplay.getDisplayAdjustments().getCompatibilityInfo();
        if (!ci.equals(CompatibilityInfo.DEFAULT_COMPATIBILITY_INFO)) {
            Configuration config2 = new Configuration(config);
            ci.applyToConfiguration(this.mNoncompatDensity, config2);
            config = config2;
        }
        synchronized (sConfigCallbacks) {
            for (int i = sConfigCallbacks.size() - 1; i >= 0; i--) {
                ((ComponentCallbacks) sConfigCallbacks.get(i)).onConfigurationChanged(config);
            }
        }
        if (this.mView != null) {
            Resources localResources = this.mView.getResources();
            config = localResources.getConfiguration();
            if (force || this.mLastConfiguration.diff(config) != 0) {
                this.mDisplay = ResourcesManager.getInstance().getAdjustedDisplay(this.mDisplay.getDisplayId(), localResources.getDisplayAdjustments());
                int lastLayoutDirection = this.mLastConfiguration.getLayoutDirection();
                int currentLayoutDirection = config.getLayoutDirection();
                this.mLastConfiguration.setTo(config);
                if (lastLayoutDirection != currentLayoutDirection && this.mViewLayoutDirectionInitial == 2) {
                    this.mView.setLayoutDirection(currentLayoutDirection);
                }
                this.mView.dispatchConfigurationChanged(config);
            }
        }
    }

    public static boolean isViewDescendantOf(View child, View parent) {
        if (child == parent) {
            return true;
        }
        ViewParent theParent = child.getParent();
        return theParent instanceof ViewGroup ? isViewDescendantOf((View) theParent, parent) : false;
    }

    private static void forceLayout(View view) {
        view.forceLayout();
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            int count = group.getChildCount();
            for (int i = 0; i < count; i++) {
                forceLayout(group.getChildAt(i));
            }
        }
    }

    boolean ensureTouchMode(boolean inTouchMode) {
        if (DBG) {
            Log.d("touchmode", "ensureTouchMode(" + inTouchMode + "), current " + "touch mode is " + this.mAttachInfo.mInTouchMode + ", this = " + this);
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
        if (this.mAttachInfo.mInTouchMode == inTouchMode) {
            return false;
        }
        if (DEBUG_TOUCHMODE) {
            Log.d("touchmode", "ensureTouchModeLocally(" + inTouchMode + "), current touch mode is" + ", " + this.mAttachInfo.mInTouchMode + ", this = " + this, new Throwable("ensureTouchModeLocally"));
        }
        this.mAttachInfo.mInTouchMode = inTouchMode;
        this.mAttachInfo.mTreeObserver.dispatchOnTouchModeChanged(inTouchMode);
        return inTouchMode ? enterTouchMode() : leaveTouchMode();
    }

    private boolean enterTouchMode() {
        if (this.mView != null && this.mView.hasFocus()) {
            View focused = this.mView.findFocus();
            if (!(focused == null || focused.isFocusableInTouchMode())) {
                ViewGroup ancestorToTakeFocus = findAncestorToTakeFocusInTouchMode(focused);
                if (ancestorToTakeFocus != null) {
                    return ancestorToTakeFocus.requestFocus();
                }
                focused.clearFocusInternal(null, true, false);
                return true;
            }
        }
        return false;
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
        if (this.mView != null) {
            if (this.mView.hasFocus()) {
                View focusedView = this.mView.findFocus();
                if (!((focusedView instanceof ViewGroup) && ((ViewGroup) focusedView).getDescendantFocusability() == 262144)) {
                    return false;
                }
            }
            View focused = focusSearch(null, 130);
            if (focused != null) {
                return focused.requestFocus(130);
            }
        }
        return false;
    }

    private void resetPointerIcon(MotionEvent event) {
        this.mPointerIconType = 1;
        updatePointerIcon(event);
    }

    private boolean updatePointerIcon(MotionEvent event) {
        float x = event.getX(0);
        float y = event.getY(0);
        if (this.mView == null) {
            Slog.d(this.mTag, "updatePointerIcon called after view was removed");
            return false;
        } else if (x < 0.0f || x >= ((float) this.mView.getWidth()) || y < 0.0f || y >= ((float) this.mView.getHeight())) {
            Slog.d(this.mTag, "updatePointerIcon called with position out of bounds");
            return false;
        } else {
            PointerIcon pointerIcon = this.mView.onResolvePointerIcon(event, 0);
            int pointerType = pointerIcon != null ? pointerIcon.getType() : 1000;
            if (this.mPointerIconType != pointerType) {
                this.mPointerIconType = pointerType;
                if (this.mPointerIconType != -1) {
                    this.mCustomPointerIcon = null;
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

    private static boolean isNavigationKey(KeyEvent keyEvent) {
        switch (keyEvent.getKeyCode()) {
            case 19:
            case 20:
            case 21:
            case 22:
            case 23:
            case 61:
            case 62:
            case 92:
            case 93:
            case 122:
            case 123:
                return true;
            case 66:
                if (!(ActivityThread.currentPackageName() == null || ActivityThread.currentPackageName().toLowerCase().contains("camera"))) {
                    return true;
                }
        }
        return false;
    }

    private static boolean isTypingKey(KeyEvent keyEvent) {
        return keyEvent.getUnicodeChar() > 0;
    }

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

    void setLocalDragState(Object obj) {
        this.mLocalDragState = obj;
    }

    private void handleDragEvent(DragEvent event) {
        if (DEBUG_INPUT) {
            Log.v(this.mTag, "handleDragEvent: event = " + event + ", mView = " + this.mView + ", mAdded = " + this.mAdded + ", this = " + this);
        }
        if (this.mView != null && this.mAdded) {
            int what = event.mAction;
            if (what == 1) {
                this.mCurrentDragView = null;
                this.mDragDescription = event.mClipDescription;
            } else {
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
                    if (this.mTranslator != null) {
                        this.mTranslator.translatePointInScreenToAppWindow(this.mDragPoint);
                    }
                    if (this.mCurScrollY != 0) {
                        this.mDragPoint.offset(0.0f, (float) this.mCurScrollY);
                    }
                    event.mX = this.mDragPoint.x;
                    event.mY = this.mDragPoint.y;
                }
                View prevDragView = this.mCurrentDragView;
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
                    this.mDragDescription = null;
                    try {
                        Log.i(this.mTag, "Reporting drop result: " + result);
                        this.mWindowSession.reportDropResult(this.mWindow, result);
                    } catch (RemoteException e2) {
                        Log.e(this.mTag, "Unable to report drop result");
                    }
                }
                if (what == 4) {
                    this.mCurrentDragView = null;
                    setLocalDragState(null);
                    this.mAttachInfo.mDragToken = null;
                    if (this.mAttachInfo.mDragSurface != null) {
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

    public void handleDispatchWindowShown() {
        this.mAttachInfo.mTreeObserver.dispatchOnWindowShown();
    }

    public void handleRequestKeyboardShortcuts(IResultReceiver receiver, int deviceId) {
        Bundle data = new Bundle();
        ArrayList<KeyboardShortcutGroup> list = new ArrayList();
        if (this.mView != null) {
            this.mView.requestKeyboardShortcuts(list, deviceId);
        }
        data.putParcelableArrayList(WindowManager.PARCEL_KEY_SHORTCUTS_ARRAY, list);
        try {
            receiver.send(0, data);
        } catch (RemoteException e) {
        }
    }

    public void getLastTouchPoint(Point outLocation) {
        outLocation.x = (int) this.mLastTouchPoint.x;
        outLocation.y = (int) this.mLastTouchPoint.y;
    }

    public int getLastTouchSource() {
        return this.mLastTouchSource;
    }

    public void setDragFocus(View newDragTarget, DragEvent event) {
        if (!(this.mCurrentDragView == newDragTarget || View.sCascadedDragDrop)) {
            float tx = event.mX;
            float ty = event.mY;
            int action = event.mAction;
            event.mX = 0.0f;
            event.mY = 0.0f;
            if (this.mCurrentDragView != null) {
                event.mAction = 6;
                this.mCurrentDragView.callDragEventHandler(event);
            }
            if (newDragTarget != null) {
                event.mAction = 5;
                newDragTarget.callDragEventHandler(event);
            }
            event.mAction = action;
            event.mX = tx;
            event.mY = ty;
        }
        this.mCurrentDragView = newDragTarget;
    }

    private AudioManager getAudioManager() {
        if (this.mView == null) {
            throw new IllegalStateException("getAudioManager called when there is no mView");
        }
        if (this.mAudioManager == null) {
            this.mAudioManager = (AudioManager) this.mView.getContext().getSystemService("audio");
        }
        return this.mAudioManager;
    }

    public AccessibilityInteractionController getAccessibilityInteractionController() {
        if (this.mView == null) {
            throw new IllegalStateException("getAccessibilityInteractionController called when there is no mView");
        }
        if (this.mAccessibilityInteractionController == null) {
            this.mAccessibilityInteractionController = new AccessibilityInteractionController(this);
        }
        return this.mAccessibilityInteractionController;
    }

    private int relayoutWindow(LayoutParams params, int viewVisibility, boolean insetsPending) throws RemoteException {
        float appScale = this.mAttachInfo.mApplicationScale;
        boolean restore = false;
        if (!(params == null || this.mTranslator == null)) {
            restore = true;
            params.backup();
            this.mTranslator.translateWindowLayout(params);
        }
        if (params != null && DBG) {
            Log.d(this.mTag, "WindowLayout in layoutWindow:" + params);
        }
        this.mPendingConfiguration.seq = 0;
        if (DEBUG_LAYOUT || DEBUG_LIGHT || DEBUG_PANIC) {
            Log.d(this.mTag, ">>>>>> CALLING relayoutW+ " + this.mWindow + ", params = " + params + ",viewVisibility = " + viewVisibility + ", insetsPending = " + insetsPending + ", appScale = " + appScale + ", mWinFrame = " + this.mWinFrame + ", mSeq = " + this.mSeq + ", mPendingOverscanInsets = " + this.mPendingOverscanInsets + ", mPendingContentInsets = " + this.mPendingContentInsets + ", mPendingVisibleInsets = " + this.mPendingVisibleInsets + ", mPendingStableInsets = " + this.mPendingStableInsets + ", mPendingOutsets = " + this.mPendingOutsets + ", mPendingConfiguration = " + this.mPendingConfiguration + ", mSurface = " + this.mSurface + ",valid = " + this.mSurface.isValid() + ", mOrigWindowType = " + this.mOrigWindowType + ",this = " + this);
        }
        if (!(params == null || this.mOrigWindowType == params.type || this.mTargetSdkVersion >= 14)) {
            Slog.w(this.mTag, "Window type can not be changed after the window is added; ignoring change of " + this.mView);
            params.type = this.mOrigWindowType;
        }
        int relayoutResult = this.mWindowSession.relayout(this.mWindow, this.mSeq, params, (int) ((((float) this.mView.getMeasuredWidth()) * appScale) + 0.5f), (int) ((((float) this.mView.getMeasuredHeight()) * appScale) + 0.5f), viewVisibility, insetsPending ? 1 : 0, this.mWinFrame, this.mPendingOverscanInsets, this.mPendingContentInsets, this.mPendingVisibleInsets, this.mPendingStableInsets, this.mPendingOutsets, this.mPendingBackDropFrame, this.mPendingConfiguration, this.mSurface);
        this.mPendingAlwaysConsumeNavBar = (relayoutResult & 64) != 0;
        if (DEBUG_LAYOUT || DEBUG_LIGHT || DEBUG_PANIC) {
            Log.d(this.mTag, "<<<<<< BACK FROM relayoutW- : res = " + relayoutResult + ", mWinFrame = " + this.mWinFrame + ", mPendingOverscanInsets = " + this.mPendingOverscanInsets + ", mPendingContentInsets = " + this.mPendingContentInsets + ", mPendingVisibleInsets = " + this.mPendingVisibleInsets + ", mPendingStableInsets = " + this.mPendingStableInsets + ", mPendingOutsets = " + this.mPendingOutsets + ", mPendingConfiguration = " + this.mPendingConfiguration + ", mSurface = " + this.mSurface + ",valid = " + this.mSurface.isValid() + ",params = " + params + ", this = " + this);
        }
        if (restore) {
            params.restore();
        }
        if (this.mTranslator != null) {
            this.mTranslator.translateRectInScreenToAppWinFrame(this.mWinFrame);
            this.mTranslator.translateRectInScreenToAppWindow(this.mPendingOverscanInsets);
            this.mTranslator.translateRectInScreenToAppWindow(this.mPendingContentInsets);
            this.mTranslator.translateRectInScreenToAppWindow(this.mPendingVisibleInsets);
            this.mTranslator.translateRectInScreenToAppWindow(this.mPendingStableInsets);
        }
        return relayoutResult;
    }

    public void playSoundEffect(int effectId) {
        checkThread();
        try {
            AudioManager audioManager = getAudioManager();
            switch (effectId) {
                case 0:
                    audioManager.playSoundEffect(0);
                    return;
                case 1:
                    audioManager.playSoundEffect(3);
                    return;
                case 2:
                    audioManager.playSoundEffect(1);
                    return;
                case 3:
                    audioManager.playSoundEffect(4);
                    return;
                case 4:
                    audioManager.playSoundEffect(2);
                    return;
                default:
                    throw new IllegalArgumentException("unknown effect id " + effectId + " not defined in " + SoundEffectConstants.class.getCanonicalName());
            }
        } catch (IllegalStateException e) {
            Log.e(this.mTag, "FATAL EXCEPTION when attempting to play sound effect: " + e);
            e.printStackTrace();
        }
        Log.e(this.mTag, "FATAL EXCEPTION when attempting to play sound effect: " + e);
        e.printStackTrace();
    }

    public boolean performHapticFeedback(int effectId, boolean always) {
        try {
            return this.mWindowSession.performHapticFeedback(this.mWindow, effectId, always);
        } catch (RemoteException e) {
            Log.e(this.mTag, "performHapticFeedback RemoteException happens in " + this, e);
            return false;
        }
    }

    public View focusSearch(View focused, int direction) {
        checkThread();
        if (this.mView instanceof ViewGroup) {
            return FocusFinder.getInstance().findNextFocus((ViewGroup) this.mView, focused, direction);
        }
        return null;
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
        writer.print(prefix);
        writer.println("View Hierarchy:");
        dumpViewHierarchy(innerPrefix, writer, this.mView);
    }

    private void dumpViewHierarchy(String prefix, PrintWriter writer, View view) {
        writer.print(prefix);
        if (view == null) {
            writer.println("null");
            return;
        }
        writer.println(view.toString());
        if (view instanceof ViewGroup) {
            ViewGroup grp = (ViewGroup) view;
            int N = grp.getChildCount();
            if (N > 0) {
                prefix = prefix + "  ";
                for (int i = 0; i < N; i++) {
                    dumpViewHierarchy(prefix, writer, grp.getChildAt(i));
                }
            }
        }
    }

    public void dumpGfxInfo(int[] info) {
        info[1] = 0;
        info[0] = 0;
        if (this.mView != null) {
            String str;
            getGfxInfo(this.mView, info);
            String name = this.mView.getClass().getSimpleName() + " @" + Integer.toHexString(this.mView.hashCode());
            boolean hwdraw = false;
            if (this.mAttachInfo != null) {
                hwdraw = this.mAttachInfo.mHardwareRenderer != null;
            }
            String str2 = "OpenGLRenderer";
            StringBuilder append = new StringBuilder().append(name).append(" is drawn by ");
            if (hwdraw) {
                str = "HWUI";
            } else {
                str = "Skia";
            }
            Log.d(str2, append.append(str).toString());
        }
    }

    private static void getGfxInfo(View view, int[] info) {
        RenderNode renderNode = view.mRenderNode;
        info[0] = info[0] + 1;
        if (renderNode != null) {
            info[1] = info[1] + renderNode.getDebugSize();
        }
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            int count = group.getChildCount();
            for (int i = 0; i < count; i++) {
                getGfxInfo(group.getChildAt(i), info);
            }
        }
    }

    boolean die(boolean immediate) {
        if (DEBUG_LIFECYCLE) {
            Log.v(this.mTag, "die: immediate = " + immediate + ", mIsInTraversal = " + this.mIsInTraversal + ",mIsDrawing = " + this.mIsDrawing + ",this = " + this);
        }
        if (!immediate || this.mIsInTraversal) {
            if (this.mIsDrawing) {
                Log.e(this.mTag, "Attempting to destroy the window while drawing!\n  window=" + this + ", title=" + this.mWindowAttributes.getTitle());
            } else {
                destroyHardwareRenderer();
            }
            this.mHandler.sendEmptyMessage(3);
            return true;
        }
        doDie();
        return false;
    }

    void doDie() {
        Trace.traceBegin(8, "DoDie");
        checkThread();
        if (LOCAL_LOGV) {
            Log.v(this.mTag, "DIE in " + this + " of " + this.mSurface);
        }
        if (DEBUG_LIFECYCLE) {
            Log.v(this.mTag, "DIE in " + this + " of " + this.mSurface + ", mAdded = " + this.mAdded + ", mFirst = " + this.mFirst);
        }
        synchronized (this.mGraphicLock) {
            synchronized (this) {
                if (this.mRemoved) {
                    return;
                }
                this.mRemoved = true;
                if (this.mAdded) {
                    dispatchDetachedFromWindow();
                }
                if (this.mAdded && !this.mFirst) {
                    Trace.traceBegin(8, "destroyHardwareRenderer");
                    destroyHardwareRenderer();
                    Trace.traceEnd(8);
                    if (this.mView != null) {
                        int viewVisibility = this.mView.getVisibility();
                        boolean viewVisibilityChanged = this.mViewVisibility != viewVisibility;
                        if (this.mWindowAttributesChanged || viewVisibilityChanged) {
                            try {
                                if ((relayoutWindow(this.mWindowAttributes, viewVisibility, false) & 2) != 0) {
                                    this.mWindowSession.finishDrawing(this.mWindow);
                                }
                            } catch (RemoteException e) {
                                Log.e(this.mTag, "RemoteException when finish draw window " + this.mWindow + " in " + this, e);
                            }
                        }
                        this.mSurface.release();
                    }
                }
                this.mAdded = false;
                WindowManagerGlobal.getInstance().doRemoveView(this);
                Trace.traceEnd(8);
                return;
            }
        }
    }

    public void requestUpdateConfiguration(Configuration config) {
        this.mHandler.sendMessage(this.mHandler.obtainMessage(18, config));
    }

    public void loadSystemProperties() {
        this.mHandler.post(new AnonymousClass4(this));
    }

    private void destroyHardwareRenderer() {
        ThreadedRenderer hardwareRenderer = this.mAttachInfo.mHardwareRenderer;
        if (DEBUG_HWUI && isTargetPackage()) {
            Log.v(this.mTag, "destroyHardwareRenderer: hardwareRenderer = " + hardwareRenderer + ", mView = " + this.mView + ", this = " + this);
        }
        if (hardwareRenderer != null) {
            if (this.mView != null) {
                hardwareRenderer.destroyHardwareResources(this.mView);
            }
            hardwareRenderer.destroy();
            hardwareRenderer.setRequested(false);
            this.mAttachInfo.mHardwareRenderer = null;
            this.mAttachInfo.mHardwareAccelerated = false;
        }
    }

    public void dispatchResized(Rect frame, Rect overscanInsets, Rect contentInsets, Rect visibleInsets, Rect stableInsets, Rect outsets, boolean reportDraw, Configuration newConfig, Rect backDropFrame, boolean forceLayout, boolean alwaysConsumeNavBar) {
        if (DEBUG_LAYOUT) {
            Log.v(this.mTag, "Resizing " + this + ": frame=" + frame.toShortString() + " contentInsets=" + contentInsets.toShortString() + " visibleInsets=" + visibleInsets.toShortString() + " reportDraw=" + reportDraw + " backDropFrame=" + backDropFrame + ", this = " + this);
        }
        if (this.mDragResizing) {
            boolean fullscreen = frame.equals(backDropFrame);
            synchronized (this.mWindowCallbacks) {
                for (int i = this.mWindowCallbacks.size() - 1; i >= 0; i--) {
                    ((WindowCallbacks) this.mWindowCallbacks.get(i)).onWindowSizeIsChanging(backDropFrame, fullscreen, visibleInsets, stableInsets);
                }
            }
        }
        Message msg = this.mHandler.obtainMessage(reportDraw ? 5 : 4);
        if (this.mTranslator != null) {
            this.mTranslator.translateRectInScreenToAppWindow(frame);
            this.mTranslator.translateRectInScreenToAppWindow(overscanInsets);
            this.mTranslator.translateRectInScreenToAppWindow(contentInsets);
            this.mTranslator.translateRectInScreenToAppWindow(visibleInsets);
        }
        SomeArgs args = SomeArgs.obtain();
        boolean sameProcessCall = Binder.getCallingPid() == Process.myPid();
        if (sameProcessCall) {
            frame = new Rect(frame);
        }
        args.arg1 = frame;
        if (sameProcessCall) {
            contentInsets = new Rect(contentInsets);
        }
        args.arg2 = contentInsets;
        if (sameProcessCall) {
            visibleInsets = new Rect(visibleInsets);
        }
        args.arg3 = visibleInsets;
        if (sameProcessCall && newConfig != null) {
            newConfig = new Configuration(newConfig);
        }
        args.arg4 = newConfig;
        if (sameProcessCall) {
            overscanInsets = new Rect(overscanInsets);
        }
        args.arg5 = overscanInsets;
        if (sameProcessCall) {
            stableInsets = new Rect(stableInsets);
        }
        args.arg6 = stableInsets;
        if (sameProcessCall) {
            outsets = new Rect(outsets);
        }
        args.arg7 = outsets;
        if (sameProcessCall) {
            backDropFrame = new Rect(backDropFrame);
        }
        args.arg8 = backDropFrame;
        args.argi1 = forceLayout ? 1 : 0;
        args.argi2 = alwaysConsumeNavBar ? 1 : 0;
        msg.obj = args;
        this.mHandler.sendMessage(msg);
    }

    public void dispatchMoved(int newX, int newY) {
        if (DEBUG_LAYOUT) {
            Log.v(this.mTag, "Window moved " + this + ": newX=" + newX + " newY=" + newY);
        }
        if (this.mTranslator != null) {
            PointF point = new PointF((float) newX, (float) newY);
            this.mTranslator.translatePointInScreenToAppWindow(point);
            newX = (int) (((double) point.x) + 0.5d);
            newY = (int) (((double) point.y) + 0.5d);
        }
        this.mHandler.sendMessage(this.mHandler.obtainMessage(23, newX, newY));
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
        if (this.mQueuedInputEventPoolSize < 10) {
            this.mQueuedInputEventPoolSize++;
            q.mNext = this.mQueuedInputEventPool;
            this.mQueuedInputEventPool = q;
        }
    }

    void enqueueInputEvent(InputEvent event) {
        enqueueInputEvent(event, null, 0, false);
    }

    void enqueueInputEvent(InputEvent event, InputEventReceiver receiver, int flags, boolean processImmediately) {
        adjustInputEventForCompatibility(event);
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
        if (event instanceof KeyEvent) {
            this.mKeyEventStatus = INPUT_DISPATCH_STATE_ENQUEUE_EVENT;
        } else {
            this.mMotionEventStatus = INPUT_DISPATCH_STATE_ENQUEUE_EVENT;
        }
        if (DEBUG_MOTION && (event instanceof MotionEvent)) {
            MotionEvent ev = (MotionEvent) event;
            Trace.traceBegin(4, "MET_enqueueInputEvent_name: " + this.mWindowAttributes.getTitle());
            Trace.traceEnd(4);
            Trace.traceBegin(4, "MET_enqueueInputEvent: " + ev.getEventTime() + "," + ev.getAction() + "," + ev.getX(0) + "," + ev.getY(0));
            Trace.traceEnd(4);
        }
        if (DEBUG_INPUT || DEBUG_KEY || DEBUG_MOTION || DEBUG_DEFAULT) {
            Log.v(this.mTag, "enqueueInputEvent: event = " + event + ",processImmediately = " + processImmediately + ",mProcessInputEventsScheduled = " + this.mProcessInputEventsScheduled + ", this = " + this);
        }
        if (processImmediately) {
            doProcessInputEvents();
        } else {
            scheduleProcessInputEvents();
        }
    }

    private void scheduleProcessInputEvents() {
        if (DEBUG_INPUT || DEBUG_KEY || DEBUG_MOTION) {
            Log.v(this.mTag, "scheduleProcessInputEvents: mPendingInputEventHead = " + (this.mPendingInputEventHead != null ? this.mPendingInputEventHead.mEvent : PhoneConstants.MVNO_TYPE_NONE) + ",mProcessInputEventsScheduled = " + this.mProcessInputEventsScheduled + ", this = " + this);
        }
        if (!this.mProcessInputEventsScheduled) {
            this.mKeyEventStatus = INPUT_DISPATCH_STATE_SCHEDULE_EVENT;
            this.mProcessInputEventsScheduled = true;
            Message msg = this.mHandler.obtainMessage(19);
            msg.setAsynchronous(true);
            this.mHandler.sendMessage(msg);
        }
    }

    void doProcessInputEvents() {
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
                MotionEvent me = q.mEvent;
                if (me.getHistorySize() > 0) {
                    oldestEventTime = me.getHistoricalEventTimeNano(0);
                }
            }
            this.mChoreographer.mFrameInfo.updateInputEventTime(eventTime, oldestEventTime);
            if (q.mEvent instanceof KeyEvent) {
                this.mKeyEventStatus = INPUT_DISPATCH_STATE_PROCESS_EVENT;
            } else {
                this.mMotionEventStatus = INPUT_DISPATCH_STATE_PROCESS_EVENT;
            }
            if (DEBUG_INPUT || DEBUG_KEY || DEBUG_MOTION) {
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
        Trace.asyncTraceBegin(8, "deliverInputEvent", q.mEvent.getSequenceNumber());
        if (this.mInputEventConsistencyVerifier != null) {
            this.mInputEventConsistencyVerifier.onInputEvent(q.mEvent, 0);
        }
        InputStage stage = q.shouldSendToSynthesizer() ? this.mSyntheticInputStage : q.shouldSkipIme() ? this.mFirstPostImeInputStage : this.mFirstInputStage;
        if (stage != null) {
            stage.deliver(q);
        } else {
            finishInputEvent(q);
        }
    }

    private void finishInputEvent(QueuedInputEvent q) {
        String stage;
        long inputElapseTime;
        Trace.asyncTraceEnd(8, "deliverInputEvent", q.mEvent.getSequenceNumber());
        boolean handled = (q.mFlags & 8) != 0;
        long currentTime = System.currentTimeMillis();
        if (q.mEvent instanceof KeyEvent) {
            this.mPreviousKeyEvent = this.mCurrentKeyEvent;
            this.mPreviousKeyEventFinishTime = System.currentTimeMillis();
            this.mCurrentKeyEvent = null;
            stage = this.mKeyEventStatus;
            this.mKeyEventStatus = INPUT_DISPATCH_STATE_FINISHED;
            inputElapseTime = currentTime - this.mKeyEventStartTime;
        } else {
            this.mPreviousMotion = this.mCurrentMotion;
            this.mPreviousMotionEventFinishTime = System.currentTimeMillis();
            this.mCurrentMotion = null;
            stage = this.mMotionEventStatus;
            this.mMotionEventStatus = INPUT_DISPATCH_STATE_FINISHED;
            inputElapseTime = currentTime - this.mMotionEventStartTime;
        }
        if ((DEBUG_DEFAULT || DEBUG_INPUT_STAGES) && inputElapseTime >= OppoTelephonyConstant.OPPO_START_PHONE_FROM_BOOT_TIME) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            Log.v(this.mTag, "[ANR Warning]Input routeing takes more than 6000ms since " + simpleDateFormat.format(new Date(currentTime - inputElapseTime)) + ", this = " + this);
            this.mFirstInputStage.dumpStageInfo(simpleDateFormat);
        }
        this.mFirstInputStage.clear();
        if (DEBUG_DEFAULT || DEBUG_INPUT || DEBUG_KEY || DEBUG_MOTION) {
            Log.v(this.mTag, "finishInputEvent: handled = " + handled + ",event = " + q.mEvent + ", stage = " + stage + ", this = " + this);
        }
        if (q.mReceiver != null) {
            q.mReceiver.finishInputEvent(q.mEvent, handled);
        } else {
            q.mEvent.recycleIfNeededAfterDispatch();
        }
        recycleQueuedInputEvent(q);
    }

    private void adjustInputEventForCompatibility(InputEvent e) {
        if (this.mTargetSdkVersion < 23 && (e instanceof MotionEvent)) {
            MotionEvent motion = (MotionEvent) e;
            int buttonState = motion.getButtonState();
            int compatButtonState = (buttonState & 96) >> 4;
            if (compatButtonState != 0) {
                motion.setButtonState(buttonState | compatButtonState);
            }
        }
    }

    static boolean isTerminalInputEvent(InputEvent event) {
        boolean z = true;
        if (event instanceof KeyEvent) {
            if (((KeyEvent) event).getAction() != 1) {
                z = false;
            }
            return z;
        }
        int action = ((MotionEvent) event).getAction();
        if (!(action == 1 || action == 3 || action == 10)) {
            z = false;
        }
        return z;
    }

    void scheduleConsumeBatchedInput() {
        if (DEBUG_INPUT || DEBUG_KEY || DEBUG_MOTION) {
            Log.v(this.mTag, "scheduleConsumeBatchedInput: mConsumeBatchedInputScheduled = " + this.mConsumeBatchedInputScheduled + ",mPendingInputEventHead = " + this.mPendingInputEventHead + ", this = " + this);
        }
        if (!this.mConsumeBatchedInputScheduled) {
            this.mConsumeBatchedInputScheduled = true;
            this.mChoreographer.postCallback(0, this.mConsumedBatchedInputRunnable, null);
        }
    }

    void unscheduleConsumeBatchedInput() {
        if (DEBUG_INPUT || DEBUG_KEY || DEBUG_MOTION) {
            Log.v(this.mTag, "unscheduleConsumeBatchedInput: mConsumeBatchedInputScheduled = " + this.mConsumeBatchedInputScheduled + ", this = " + this);
        }
        if (this.mConsumeBatchedInputScheduled) {
            this.mConsumeBatchedInputScheduled = false;
            this.mChoreographer.removeCallbacks(0, this.mConsumedBatchedInputRunnable, null);
        }
    }

    void scheduleConsumeBatchedInputImmediately() {
        if (!this.mConsumeBatchedInputImmediatelyScheduled) {
            unscheduleConsumeBatchedInput();
            this.mConsumeBatchedInputImmediatelyScheduled = true;
            this.mHandler.post(this.mConsumeBatchedInputImmediatelyRunnable);
        }
    }

    void doConsumeBatchedInput(long frameTimeNanos) {
        if (this.mPendingInputEventHead != null && (DEBUG_INPUT || DEBUG_KEY || DEBUG_MOTION)) {
            Log.v(this.mTag, "doConsumeBatchedInput: frameTimeNanos = " + frameTimeNanos + ",mConsumeBatchedInputScheduled = " + this.mConsumeBatchedInputScheduled + ",mFirstPendingInputEvent = " + this.mPendingInputEventHead.mEvent + ", this = " + this);
        }
        if (this.mConsumeBatchedInputScheduled) {
            this.mConsumeBatchedInputScheduled = false;
            if (this.mInputEventReceiver != null) {
                if (DEBUG_MOTION) {
                    Trace.traceBegin(4, "MET_consumeBatchedInput_name: " + this.mWindowAttributes.getTitle());
                    Trace.traceEnd(4);
                }
                if (this.mInputEventReceiver.consumeBatchedInputEvents(frameTimeNanos) && frameTimeNanos != -1) {
                    scheduleConsumeBatchedInput();
                }
            }
            doProcessInputEvents();
        }
    }

    public void dispatchInvalidateDelayed(View view, long delayMilliseconds) {
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(1, view), delayMilliseconds);
    }

    public void dispatchInvalidateRectDelayed(InvalidateInfo info, long delayMilliseconds) {
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(2, info), delayMilliseconds);
    }

    public void dispatchInvalidateOnAnimation(View view) {
        this.mInvalidateOnAnimationRunnable.addView(view);
    }

    public void dispatchInvalidateRectOnAnimation(InvalidateInfo info) {
        this.mInvalidateOnAnimationRunnable.addViewRect(info);
    }

    public void cancelInvalidate(View view) {
        this.mHandler.removeMessages(1, view);
        this.mHandler.removeMessages(2, view);
        this.mInvalidateOnAnimationRunnable.removeView(view);
    }

    public void dispatchInputEvent(InputEvent event) {
        dispatchInputEvent(event, null);
    }

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

    public void dispatchKeyFromIme(KeyEvent event) {
        Message msg = this.mHandler.obtainMessage(11, event);
        msg.setAsynchronous(true);
        this.mHandler.sendMessage(msg);
    }

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
        int i;
        int i2 = 1;
        Message msg = Message.obtain();
        msg.what = 6;
        if (hasFocus) {
            i = 1;
        } else {
            i = 0;
        }
        msg.arg1 = i;
        if (!inTouchMode) {
            i2 = 0;
        }
        msg.arg2 = i2;
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
        this.mHandler.sendMessage(this.mHandler.obtainMessage(17, args));
    }

    public void dispatchCheckFocus() {
        if (!this.mHandler.hasMessages(13)) {
            this.mHandler.sendEmptyMessage(13);
        }
    }

    public void dispatchRequestKeyboardShortcuts(IResultReceiver receiver, int deviceId) {
        this.mHandler.obtainMessage(26, deviceId, 0, receiver).sendToTarget();
    }

    private void postSendWindowContentChangedCallback(View source, int changeType) {
        if (this.mSendWindowContentChangedAccessibilityEvent == null) {
            this.mSendWindowContentChangedAccessibilityEvent = new SendWindowContentChangedAccessibilityEvent(this, null);
        }
        this.mSendWindowContentChangedAccessibilityEvent.runOrPost(source, changeType);
    }

    private void removeSendWindowContentChangedCallback() {
        if (this.mSendWindowContentChangedAccessibilityEvent != null) {
            this.mHandler.removeCallbacks(this.mSendWindowContentChangedAccessibilityEvent);
        }
    }

    public boolean showContextMenuForChild(View originalView) {
        return false;
    }

    public boolean showContextMenuForChild(View originalView, float x, float y) {
        return false;
    }

    public ActionMode startActionModeForChild(View originalView, ActionMode.Callback callback) {
        return null;
    }

    public ActionMode startActionModeForChild(View originalView, ActionMode.Callback callback, int type) {
        return null;
    }

    public void createContextMenu(ContextMenu menu) {
    }

    public void childDrawableStateChanged(View child) {
    }

    public boolean requestSendAccessibilityEvent(View child, AccessibilityEvent event) {
        if (this.mView == null || this.mStopped || this.mPausedForTransition) {
            return false;
        }
        View source;
        switch (event.getEventType()) {
            case 2048:
                handleWindowContentChangedEvent(event);
                break;
            case 32768:
                long sourceNodeId = event.getSourceNodeId();
                source = this.mView.findViewByAccessibilityId(AccessibilityNodeInfo.getAccessibilityViewId(sourceNodeId));
                if (source != null) {
                    AccessibilityNodeProvider provider = source.getAccessibilityNodeProvider();
                    if (provider != null) {
                        AccessibilityNodeInfo node;
                        int virtualNodeId = AccessibilityNodeInfo.getVirtualDescendantId(sourceNodeId);
                        if (virtualNodeId == Integer.MAX_VALUE) {
                            node = provider.createAccessibilityNodeInfo(-1);
                        } else {
                            node = provider.createAccessibilityNodeInfo(virtualNodeId);
                        }
                        setAccessibilityFocus(source, node);
                        break;
                    }
                }
                break;
            case 65536:
                source = this.mView.findViewByAccessibilityId(AccessibilityNodeInfo.getAccessibilityViewId(event.getSourceNodeId()));
                if (!(source == null || source.getAccessibilityNodeProvider() == null)) {
                    setAccessibilityFocus(null, null);
                    break;
                }
        }
        this.mAccessibilityManager.sendAccessibilityEvent(event);
        return true;
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
                    if (focusedChildId == Integer.MAX_VALUE) {
                        focusedChildId = -1;
                    }
                    Rect oldBounds = this.mTempRect;
                    this.mAccessibilityFocusedVirtualView.getBoundsInScreen(oldBounds);
                    this.mAccessibilityFocusedVirtualView = provider.createAccessibilityNodeInfo(focusedChildId);
                    if (this.mAccessibilityFocusedVirtualView == null) {
                        this.mAccessibilityFocusedHost = null;
                        focusedHost.clearAccessibilityFocusNoCallbacks(0);
                        provider.performAction(focusedChildId, AccessibilityAction.ACTION_CLEAR_ACCESSIBILITY_FOCUS.getId(), null);
                        invalidateRectOnScreen(oldBounds);
                    } else {
                        Rect newBounds = this.mAccessibilityFocusedVirtualView.getBoundsInScreen();
                        if (!oldBounds.equals(newBounds)) {
                            oldBounds.union(newBounds);
                            invalidateRectOnScreen(oldBounds);
                        }
                    }
                }
            }
        }
    }

    public void notifySubtreeAccessibilityStateChanged(View child, View source, int changeType) {
        postSendWindowContentChangedCallback(source, changeType);
    }

    public boolean canResolveLayoutDirection() {
        return true;
    }

    public boolean isLayoutDirectionResolved() {
        return true;
    }

    public int getLayoutDirection() {
        return 0;
    }

    public boolean canResolveTextDirection() {
        return true;
    }

    public boolean isTextDirectionResolved() {
        return true;
    }

    public int getTextDirection() {
        return 1;
    }

    public boolean canResolveTextAlignment() {
        return true;
    }

    public boolean isTextAlignmentResolved() {
        return true;
    }

    public int getTextAlignment() {
        return 1;
    }

    private View getCommonPredecessor(View first, View second) {
        if (this.mTempHashSet == null) {
            this.mTempHashSet = new HashSet();
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

    void checkThread() {
        if (this.mThread != Thread.currentThread()) {
            throw new CalledFromWrongThreadException("Only the original thread that created a view hierarchy can touch its views.");
        }
    }

    public void requestDisallowInterceptTouchEvent(boolean disallowIntercept) {
    }

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

    public void childHasTransientStateChanged(View child, boolean hasTransientState) {
    }

    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
        return false;
    }

    public void onStopNestedScroll(View target) {
    }

    public void onNestedScrollAccepted(View child, View target, int nestedScrollAxes) {
    }

    public void onNestedScroll(View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
    }

    public void onNestedPreScroll(View target, int dx, int dy, int[] consumed) {
    }

    public boolean onNestedFling(View target, float velocityX, float velocityY, boolean consumed) {
        return false;
    }

    public boolean onNestedPreFling(View target, float velocityX, float velocityY) {
        return false;
    }

    public boolean onNestedPrePerformAccessibilityAction(View target, int action, Bundle args) {
        return false;
    }

    public void setReportNextDraw() {
        this.mReportNextDraw = true;
        invalidate();
    }

    void changeCanvasOpacity(boolean opaque) {
        Log.d(this.mTag, "changeCanvasOpacity: opaque=" + opaque);
        if (this.mAttachInfo.mHardwareRenderer != null) {
            this.mAttachInfo.mHardwareRenderer.setOpaque(opaque);
        }
    }

    static HandlerActionQueue getRunQueue() {
        HandlerActionQueue rq = (HandlerActionQueue) sRunQueues.get();
        if (rq != null) {
            return rq;
        }
        rq = new HandlerActionQueue();
        sRunQueues.set(rq);
        return rq;
    }

    private void startDragResizing(Rect initialBounds, boolean fullscreen, Rect systemInsets, Rect stableInsets, int resizeMode) {
        if (!this.mDragResizing) {
            this.mDragResizing = true;
            for (int i = this.mWindowCallbacks.size() - 1; i >= 0; i--) {
                ((WindowCallbacks) this.mWindowCallbacks.get(i)).onWindowDragResizeStart(initialBounds, fullscreen, systemInsets, stableInsets, resizeMode);
            }
            this.mFullRedrawNeeded = true;
        }
    }

    private void endDragResizing() {
        if (this.mDragResizing) {
            this.mDragResizing = false;
            for (int i = this.mWindowCallbacks.size() - 1; i >= 0; i--) {
                ((WindowCallbacks) this.mWindowCallbacks.get(i)).onWindowDragResizeEnd();
            }
            this.mFullRedrawNeeded = true;
        }
    }

    private boolean updateContentDrawBounds() {
        int updated = 0;
        for (int i = this.mWindowCallbacks.size() - 1; i >= 0; i--) {
            updated |= ((WindowCallbacks) this.mWindowCallbacks.get(i)).onContentDrawn(this.mWindowAttributes.surfaceInsets.left, this.mWindowAttributes.surfaceInsets.top, this.mWidth, this.mHeight);
        }
        return (this.mDragResizing ? this.mReportNextDraw : 0) | updated;
    }

    private void requestDrawWindow() {
        if (this.mReportNextDraw) {
            this.mWindowDrawCountDown = new CountDownLatch(this.mWindowCallbacks.size());
        }
        for (int i = this.mWindowCallbacks.size() - 1; i >= 0; i--) {
            ((WindowCallbacks) this.mWindowCallbacks.get(i)).onRequestDraw(this.mReportNextDraw);
        }
    }

    public void reportActivityRelaunched() {
        this.mActivityRelaunched = true;
    }

    public void enableLogLight(boolean enable) {
        Log.v(this.mTag, "enableLogLight: enable = " + enable);
        DEBUG_LIGHT = enable;
    }

    public void updateWindowNavigationBarColor(int color) {
        if (this.mView != null) {
            this.mView.setWindowNavigationBarColor(color);
        }
    }

    public void requestLayoutBySoftInputChanged() {
        this.mSoftInputMayChanged = true;
    }

    public void enableLog(boolean enable) {
        Log.v(this.mTag, "enableLog: enable = " + enable);
        LOCAL_LOGV = enable;
        DEBUG_DRAW = enable;
        DEBUG_LAYOUT = enable;
        DEBUG_DIALOG = enable;
        DEBUG_INPUT_RESIZE = enable;
        DEBUG_ORIENTATION = enable;
        DEBUG_TRACKBALL = enable;
        DEBUG_IMF = enable;
        DEBUG_CONFIGURATION = enable;
        DEBUG_FPS = enable;
        DEBUG_INPUT = enable;
        DEBUG_IME_ANR = enable;
        DEBUG_LIFECYCLE = enable;
        DEBUG_REQUESTLAYOUT = enable;
        DEBUG_INVALIDATE = enable;
        DEBUG_SCHEDULETRAVERSALS = enable;
    }

    public String toString() {
        return "ViewRoot{" + Integer.toHexString(System.identityHashCode(this)) + " " + this.mWindowAttributes.getTitle() + ",ident = " + this.mIdent + "}";
    }

    private static void checkViewRootImplLogProperty() {
        boolean z = true;
        String propString = SystemProperties.get(LOG_PROPERTY_NAME);
        if (propString != null && propString.length() > 0) {
            boolean z2;
            int logFilter = 0;
            try {
                logFilter = Integer.parseInt(propString, 16);
            } catch (NumberFormatException e) {
                Log.w(TAG, "Invalid format of propery string: " + propString);
            }
            Log.d(TAG, "checkViewRootImplLogProperty: propString = " + propString + ",logFilter = #" + Integer.toHexString(logFilter));
            if ((logFilter & 1) == 1) {
                z2 = true;
            } else {
                z2 = false;
            }
            LOCAL_LOGV = z2;
            if ((logFilter & 2) == 2) {
                z2 = true;
            } else {
                z2 = false;
            }
            DEBUG_DRAW = z2;
            if ((logFilter & 4) == 4) {
                z2 = true;
            } else {
                z2 = false;
            }
            DEBUG_LAYOUT = z2;
            if ((logFilter & 8) == 8) {
                z2 = true;
            } else {
                z2 = false;
            }
            DEBUG_DIALOG = z2;
            if ((logFilter & 16) == 16) {
                z2 = true;
            } else {
                z2 = false;
            }
            DEBUG_INPUT_RESIZE = z2;
            if ((logFilter & 32) == 32) {
                z2 = true;
            } else {
                z2 = false;
            }
            DEBUG_ORIENTATION = z2;
            if ((logFilter & 64) == 64) {
                z2 = true;
            } else {
                z2 = false;
            }
            DEBUG_TRACKBALL = z2;
            if ((logFilter & 128) == 128) {
                z2 = true;
            } else {
                z2 = false;
            }
            DEBUG_IMF = z2;
            if ((logFilter & 256) == 256) {
                z2 = true;
            } else {
                z2 = false;
            }
            DEBUG_CONFIGURATION = z2;
            if ((logFilter & 512) == 512) {
                z2 = true;
            } else {
                z2 = false;
            }
            DBG = z2;
            if ((logFilter & 1024) == 1024) {
                z2 = true;
            } else {
                z2 = false;
            }
            DEBUG_FPS = z2;
            if ((4194304 & logFilter) == 4194304) {
                z2 = true;
            } else {
                z2 = false;
            }
            DEBUG_INPUT_STAGES = z2;
            if ((8388608 & logFilter) == 8388608) {
                z2 = true;
            } else {
                z2 = false;
            }
            DEBUG_KEEP_SCREEN_ON = z2;
            if ((logFilter & 2048) == 2048) {
                z2 = true;
            } else {
                z2 = false;
            }
            DEBUG_HWUI = z2;
            if ((logFilter & 4096) == 4096) {
                z2 = true;
            } else {
                z2 = false;
            }
            DEBUG_INPUT = z2;
            if (DEBUG_INPUT || (logFilter & 8192) == 8192) {
                z2 = true;
            } else {
                z2 = false;
            }
            DEBUG_KEY = z2;
            if (DEBUG_INPUT || (logFilter & 16384) == 16384) {
                z2 = true;
            } else {
                z2 = false;
            }
            DEBUG_MOTION = z2;
            if ((logFilter & 32768) == 32768) {
                z2 = true;
            } else {
                z2 = false;
            }
            DEBUG_IME_ANR = z2;
            if ((logFilter & 65536) == 65536) {
                z2 = true;
            } else {
                z2 = false;
            }
            DEBUG_LIFECYCLE = z2;
            if ((131072 & logFilter) == 131072) {
                z2 = true;
            } else {
                z2 = false;
            }
            DEBUG_REQUESTLAYOUT = z2;
            if ((262144 & logFilter) == 262144) {
                z2 = true;
            } else {
                z2 = false;
            }
            DEBUG_INVALIDATE = z2;
            if ((524288 & logFilter) == 524288) {
                z2 = true;
            } else {
                z2 = false;
            }
            DEBUG_SCHEDULETRAVERSALS = z2;
            if ((1048576 & logFilter) == 1048576) {
                z2 = true;
            } else {
                z2 = false;
            }
            DEBUG_TOUCHMODE = z2;
            if ((2097152 & logFilter) != 2097152) {
                z = false;
            }
            DEBUG_MEASURE_LAYOUT = z;
        }
    }

    private static boolean checkAppLaunchTimeProperty() {
        if (1 == SystemProperties.getInt("persist.applaunchtime.enable", 0)) {
            return true;
        }
        return false;
    }

    /* JADX WARNING: Missing block: B:2:0x0004, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static void dumpViewAsBitmap(View view, String dumpPkgName) {
        if (!(view == null || dumpPkgName == null || !view.getContext().getPackageName().equals(dumpPkgName))) {
            Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Config.ARGB_8888);
            view.dispatchDraw(new Canvas(bitmap));
            try {
                String fileName = DUMP_IMAGE_PTAH + DateFormat.format(DATE_FORMAT_STRING, System.currentTimeMillis()).toString() + DUMP_IMAGE_FORMAT;
                Log.d(TAG, "dumpViewInfo: fileName = " + fileName);
                File file = new File(fileName);
                Log.d(TAG, "dumpViewInfo: file = " + file);
                FileOutputStream fos = new FileOutputStream(file);
                if (fos != null) {
                    bitmap.compress(CompressFormat.PNG, 90, fos);
                    fos.flush();
                    fos.close();
                    bitmap.recycle();
                    Log.d(TAG, "Bitmap dump successfully.");
                } else {
                    Log.d(TAG, "Bitmap dump failed.");
                }
            } catch (IOException ioe) {
                Log.d(TAG, "Bitmap dump failed with exception " + ioe);
            }
        }
    }

    private boolean isTargetPackage() {
        if (this.mView == null) {
            return false;
        }
        String pkgName = this.mView.getContext().getPackageName();
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.addCategory("android.intent.category.HOME");
        ResolveInfo resolveInfo = this.mContext.getPackageManager().resolveActivity(intent, 65536);
        if (resolveInfo == null || resolveInfo.activityInfo == null || resolveInfo.activityInfo.packageName == null) {
            return false;
        }
        return resolveInfo.activityInfo.packageName.equals(pkgName);
    }

    public void dumpInputDispatchingStatus() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        Date date = new Date(this.mKeyEventStartTime);
        Date date2 = new Date(this.mPreviousKeyEventFinishTime);
        long dispatchTime = System.currentTimeMillis() - this.mKeyEventStartTime;
        if (this.mCurrentKeyEvent == null) {
            Log.i(this.mTag, "ANR Key Analyze: No Key event currently.");
            Log.i(this.mTag, "ANR Key Analyze: Previeous Event " + this.mPreviousKeyEvent + ",finish at " + simpleDateFormat.format(date2));
        } else {
            Log.i(this.mTag, "Dispatch " + this.mCurrentKeyEvent + " status is " + this.mKeyEventStatus + ",start" + " at " + simpleDateFormat.format(date) + ", spent " + dispatchTime + "ms.");
        }
        if (this.mCurrentMotion == null) {
            date2.setTime(this.mPreviousMotionEventFinishTime);
            Log.i(this.mTag, "ANR Motion Analyze: No motion event currently.");
            Log.i(this.mTag, "ANR Motion Analyze: Previeous Event " + this.mPreviousMotion + ",finish at " + simpleDateFormat.format(date2));
        } else {
            date.setTime(this.mMotionEventStartTime);
            Log.i(this.mTag, "Dispatch " + this.mCurrentMotion + " status is " + this.mMotionEventStatus + ",start" + " at " + simpleDateFormat.format(date) + ", spent " + (System.currentTimeMillis() - this.mMotionEventStartTime) + "ms.");
        }
        this.mFirstInputStage.dumpStageInfo(simpleDateFormat);
        this.mFirstInputStage.clear();
    }

    @OppoHook(level = OppoHookType.NEW_METHOD, note = "XiaoKang.Feng@Plf.SDK, 2016-10-28 : Add for Surface detection", property = OppoRomType.ROM)
    private int getChildCount(ViewGroup parent) {
        this.mCount = 0;
        if (parent != null) {
            updateChildCount(parent);
        }
        return this.mCount;
    }

    @OppoHook(level = OppoHookType.NEW_METHOD, note = "XiaoKang.Feng@Plf.SDK, 2016-10-28 : Add for Surface detection", property = OppoRomType.ROM)
    private void updateChildCount(ViewGroup parent) {
        int count = parent.getChildCount();
        for (int i = 0; i < count; i++) {
            View child = parent.getChildAt(i);
            if (child != null) {
                this.mCount++;
                if (this.mCount <= 10) {
                    if (child instanceof ViewGroup) {
                        ViewGroup group = (ViewGroup) child;
                        if (group.getChildCount() >= 1) {
                            updateChildCount(group);
                        }
                    }
                } else {
                    return;
                }
            }
        }
    }

    public boolean swipeFromBottom(MotionEvent event) {
        switch (event.getAction()) {
            case 0:
                int downY = (int) event.getRawY();
                int downX = (int) event.getRawX();
                float ori = event.getOrientation();
                if (this.mGlobalScale != 1.0f && this.mGlobalScale != -1.0f) {
                    downY = (int) ((this.mGlobalScale * ((float) downY)) + 0.5f);
                    downX = (int) ((this.mGlobalScale * ((float) downX)) + 0.5f);
                } else if (isCompatibleWindow()) {
                    try {
                        WindowManagerGlobal.getInstance();
                        this.mGlobalScale = WindowManagerGlobal.getWindowManagerService().getWindowGlobalScale(this.mWindow);
                    } catch (RemoteException e) {
                        Slog.w(TAG, "getSystemCompatibleScaling " + e);
                    }
                }
                if (ori == 0.0f || ori == EVENT_OTHER) {
                    if (downY < this.mScreenHeight - this.mStatusBarHeight) {
                        this.mIgnoring = false;
                    } else {
                        this.mIgnoring = true;
                        Slog.d(TAG, "swipeFromBottom mIgnoring " + this.mIgnoring + " downY " + downY + " mScreenHeight " + this.mScreenHeight + " mStatusBarHeight " + this.mStatusBarHeight + " globalScale " + this.mGlobalScale);
                    }
                } else if (ori == EVENT_90) {
                    if (downX < this.mScreenHeight - this.mStatusBarHeight) {
                        this.mIgnoring = false;
                    } else {
                        this.mIgnoring = true;
                        Slog.d(TAG, "ViewRoomtImpl swipeRight 90 mIgnoring " + this.mIgnoring + " downX " + downX + " mScreenWidth " + this.mScreenWidth + " mStatusBarHeight " + this.mStatusBarHeight + " globalScale " + this.mGlobalScale);
                    }
                } else if (ori != EVENT_270) {
                    this.mIgnoring = false;
                } else if (downX > this.mStatusBarHeight) {
                    this.mIgnoring = false;
                } else {
                    this.mIgnoring = true;
                    Slog.d(TAG, "ViewRoomtImpl swipeLeft 270 mIgnoring " + this.mIgnoring + " downX " + downX + " mScreenWidth " + this.mScreenWidth + " mStatusBarHeight " + this.mStatusBarHeight + " globalScale " + this.mGlobalScale);
                }
                if (event.getDownTime() != event.getEventTime()) {
                    this.mIgnoring = false;
                    Slog.d(TAG, "swipeFromBottom mIgnoring === " + this.mIgnoring + " downY " + downY + " mStatusBarHeight " + this.mStatusBarHeight);
                    break;
                }
                break;
            case 1:
                if (this.mIgnoring) {
                    this.mIgnoring = false;
                    return true;
                }
                break;
        }
        return this.mIgnoring;
    }

    private void initGlobalScale() {
        if (isCompatibleWindow()) {
            try {
                WindowManagerGlobal.getInstance();
                this.mGlobalScale = WindowManagerGlobal.getWindowManagerService().getWindowGlobalScale(this.mWindow);
            } catch (RemoteException e) {
                Slog.w(TAG, "getSystemCompatibleScaling " + e);
            }
        }
    }

    private boolean isCompatibleWindow() {
        CompatibilityInfo compatibilityInfo = null;
        if (!(this.mDisplay == null || this.mDisplay.getDisplayAdjustments() == null)) {
            compatibilityInfo = this.mDisplay.getDisplayAdjustments().getCompatibilityInfo();
        }
        if (compatibilityInfo == null || !compatibilityInfo.isScalingRequired() || compatibilityInfo.supportsScreen()) {
            return false;
        }
        return true;
    }

    private void sendInitGlobalScale() {
        this.mHandler.sendMessage(this.mHandler.obtainMessage(10000));
    }

    private boolean needScale() {
        CompatibilityInfo compatibilityInfo = null;
        if (!(this.mDisplay == null || this.mDisplay.getDisplayAdjustments() == null)) {
            compatibilityInfo = this.mDisplay.getDisplayAdjustments().getCompatibilityInfo();
        }
        if (compatibilityInfo == null || !compatibilityInfo.isScalingRequired() || compatibilityInfo.supportsScreen() || this.mNoncompatDensity == this.mDensity) {
            return false;
        }
        return true;
    }
}
