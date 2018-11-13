package com.android.server.wm;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.Binder;
import android.os.Debug;
import android.os.IBinder;
import android.os.IRemoteCallback;
import android.os.RemoteException;
import android.util.ArraySet;
import android.util.Slog;
import android.util.SparseArray;
import android.view.AppTransitionAnimationSpec;
import android.view.IAppTransitionAnimationSpecsFuture;
import android.view.WindowManager.LayoutParams;
import android.view.WindowManagerInternal.AppTransitionListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.ClipRectAnimation;
import android.view.animation.Interpolator;
import android.view.animation.PathInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import com.android.internal.R;
import com.android.internal.util.DumpUtils.Dump;
import com.android.server.AttributeCache;
import com.android.server.AttributeCache.Entry;
import com.android.server.display.OppoBrightUtils;
import com.android.server.oppo.IElsaManager;
import com.android.server.usb.UsbAudioDevice;
import com.android.server.wm.animation.ClipRectLRAnimation;
import com.android.server.wm.animation.ClipRectTBAnimation;
import com.android.server.wm.animation.CurvedTranslateAnimation;
import com.oppo.util.OppoAnimSynthesisNumber;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
public class AppTransition implements Dump {
    private static final int APP_STATE_IDLE = 0;
    private static final int APP_STATE_READY = 1;
    private static final int APP_STATE_RUNNING = 2;
    private static final int APP_STATE_TIMEOUT = 3;
    private static final long APP_TRANSITION_ANIMATION_SPECS_FUTURE_TIMEOUT_MS = 1500;
    private static final long APP_TRANSITION_TIMEOUT_MS = 5000;
    private static final int CLIP_REVEAL_TRANSLATION_Y_DP = 8;
    static final int DEFAULT_APP_TRANSITION_DURATION = 336;
    private static final int MAX_CLIP_REVEAL_TRANSITION_DURATION = 420;
    private static final int NEXT_TRANSIT_TYPE_CLIP_REVEAL = 8;
    private static final int NEXT_TRANSIT_TYPE_CUSTOM = 1;
    private static final int NEXT_TRANSIT_TYPE_CUSTOM_IN_PLACE = 7;
    private static final int NEXT_TRANSIT_TYPE_NONE = 0;
    private static final int NEXT_TRANSIT_TYPE_SCALE_UP = 2;
    private static final int NEXT_TRANSIT_TYPE_THUMBNAIL_ASPECT_SCALE_DOWN = 6;
    private static final int NEXT_TRANSIT_TYPE_THUMBNAIL_ASPECT_SCALE_UP = 5;
    private static final int NEXT_TRANSIT_TYPE_THUMBNAIL_SCALE_DOWN = 4;
    private static final int NEXT_TRANSIT_TYPE_THUMBNAIL_SCALE_UP = 3;
    private static final float RECENTS_THUMBNAIL_FADEIN_FRACTION = 0.5f;
    private static final float RECENTS_THUMBNAIL_FADEOUT_FRACTION = 0.5f;
    private static final String TAG = null;
    private static final int THUMBNAIL_APP_TRANSITION_DURATION = 336;
    private static final Interpolator THUMBNAIL_DOCK_INTERPOLATOR = null;
    private static final int THUMBNAIL_TRANSITION_ENTER_SCALE_DOWN = 2;
    private static final int THUMBNAIL_TRANSITION_ENTER_SCALE_UP = 0;
    private static final int THUMBNAIL_TRANSITION_EXIT_SCALE_DOWN = 3;
    private static final int THUMBNAIL_TRANSITION_EXIT_SCALE_UP = 1;
    static final Interpolator TOUCH_RESPONSE_INTERPOLATOR = null;
    public static final int TRANSIT_ACTIVITY_CLOSE = 7;
    public static final int TRANSIT_ACTIVITY_OPEN = 6;
    public static final int TRANSIT_ACTIVITY_RELAUNCH = 18;
    public static final int TRANSIT_DOCK_TASK_FROM_RECENTS = 19;
    public static final int TRANSIT_NONE = 0;
    public static final int TRANSIT_TASK_CLOSE = 9;
    public static final int TRANSIT_TASK_IN_PLACE = 17;
    public static final int TRANSIT_TASK_OPEN = 8;
    public static final int TRANSIT_TASK_OPEN_BEHIND = 16;
    public static final int TRANSIT_TASK_TO_BACK = 11;
    public static final int TRANSIT_TASK_TO_FRONT = 10;
    public static final int TRANSIT_UNSET = -1;
    public static final int TRANSIT_WALLPAPER_CLOSE = 12;
    public static final int TRANSIT_WALLPAPER_INTRA_CLOSE = 15;
    public static final int TRANSIT_WALLPAPER_INTRA_OPEN = 14;
    public static final int TRANSIT_WALLPAPER_OPEN = 13;
    private IRemoteCallback mAnimationFinishedCallback;
    private int mAppTransitionState;
    private final Interpolator mClipHorizontalInterpolator;
    private final int mClipRevealTranslationY;
    private final int mConfigShortAnimTime;
    private final Context mContext;
    private int mCurrentUserId;
    private final Interpolator mDecelerateInterpolator;
    private final ExecutorService mDefaultExecutor;
    private AppTransitionAnimationSpec mDefaultNextAppTransitionAnimationSpec;
    private final Interpolator mFastOutLinearInInterpolator;
    private final Interpolator mFastOutSlowInInterpolator;
    private int mLastClipRevealMaxTranslation;
    private long mLastClipRevealTransitionDuration;
    private String mLastClosingApp;
    private boolean mLastHadClipReveal;
    private String mLastOpeningApp;
    private int mLastUsedAppTransition;
    private final Interpolator mLinearOutSlowInInterpolator;
    private final ArrayList<AppTransitionListener> mListeners;
    private int mNextAppTransition;
    private final SparseArray<AppTransitionAnimationSpec> mNextAppTransitionAnimationsSpecs;
    private IAppTransitionAnimationSpecsFuture mNextAppTransitionAnimationsSpecsFuture;
    private boolean mNextAppTransitionAnimationsSpecsPending;
    protected IRemoteCallback mNextAppTransitionCallback;
    protected int mNextAppTransitionEnter;
    protected int mNextAppTransitionExit;
    private IRemoteCallback mNextAppTransitionFutureCallback;
    private int mNextAppTransitionInPlace;
    private Rect mNextAppTransitionInsets;
    protected String mNextAppTransitionPackage;
    private boolean mNextAppTransitionScaleUp;
    protected int mNextAppTransitionType;
    private boolean mProlongedAnimationsEnded;
    private final WindowManagerService mService;
    private final Interpolator mThumbnailFadeInInterpolator;
    private final Interpolator mThumbnailFadeOutInterpolator;
    private Rect mTmpFromClipRect;
    private final Rect mTmpRect;
    private Rect mTmpToClipRect;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.wm.AppTransition.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.wm.AppTransition.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.wm.AppTransition.<clinit>():void");
    }

    AppTransition(Context context, WindowManagerService service) {
        this.mNextAppTransition = -1;
        this.mLastUsedAppTransition = -1;
        this.mNextAppTransitionType = 0;
        this.mNextAppTransitionAnimationsSpecs = new SparseArray();
        this.mNextAppTransitionInsets = new Rect();
        this.mTmpFromClipRect = new Rect();
        this.mTmpToClipRect = new Rect();
        this.mTmpRect = new Rect();
        this.mAppTransitionState = 0;
        this.mClipHorizontalInterpolator = new PathInterpolator(OppoBrightUtils.MIN_LUX_LIMITI, OppoBrightUtils.MIN_LUX_LIMITI, 0.4f, 1.0f);
        this.mCurrentUserId = 0;
        this.mLastClipRevealTransitionDuration = 336;
        this.mListeners = new ArrayList();
        this.mDefaultExecutor = Executors.newSingleThreadExecutor();
        this.mContext = context;
        this.mService = service;
        this.mLinearOutSlowInInterpolator = AnimationUtils.loadInterpolator(context, 17563662);
        this.mFastOutLinearInInterpolator = AnimationUtils.loadInterpolator(context, 17563663);
        this.mFastOutSlowInInterpolator = AnimationUtils.loadInterpolator(context, 17563661);
        this.mConfigShortAnimTime = context.getResources().getInteger(17694720);
        this.mDecelerateInterpolator = AnimationUtils.loadInterpolator(context, 17563651);
        this.mThumbnailFadeInInterpolator = new Interpolator() {
            public float getInterpolation(float input) {
                if (input < 0.5f) {
                    return OppoBrightUtils.MIN_LUX_LIMITI;
                }
                return AppTransition.this.mFastOutLinearInInterpolator.getInterpolation((input - 0.5f) / 0.5f);
            }
        };
        this.mThumbnailFadeOutInterpolator = new Interpolator() {
            public float getInterpolation(float input) {
                if (input >= 0.5f) {
                    return 1.0f;
                }
                return AppTransition.this.mLinearOutSlowInInterpolator.getInterpolation(input / 0.5f);
            }
        };
        this.mClipRevealTranslationY = (int) (this.mContext.getResources().getDisplayMetrics().density * 8.0f);
    }

    boolean isTransitionSet() {
        return this.mNextAppTransition != -1;
    }

    boolean isTransitionEqual(int transit) {
        return this.mNextAppTransition == transit;
    }

    int getAppTransition() {
        return this.mNextAppTransition;
    }

    private void setAppTransition(int transit) {
        this.mNextAppTransition = transit;
        setLastAppTransition(-1, null, null);
    }

    void setLastAppTransition(int transit, AppWindowToken openingApp, AppWindowToken closingApp) {
        this.mLastUsedAppTransition = transit;
        this.mLastOpeningApp = IElsaManager.EMPTY_PACKAGE + openingApp;
        this.mLastClosingApp = IElsaManager.EMPTY_PACKAGE + closingApp;
    }

    boolean isReady() {
        if (this.mAppTransitionState == 1 || this.mAppTransitionState == 3) {
            return true;
        }
        return false;
    }

    void setReady() {
        this.mAppTransitionState = 1;
        fetchAppTransitionSpecsFromFuture();
    }

    boolean isRunning() {
        return this.mAppTransitionState == 2;
    }

    void setIdle() {
        this.mAppTransitionState = 0;
    }

    boolean isTimeout() {
        return this.mAppTransitionState == 3;
    }

    void setTimeout() {
        this.mAppTransitionState = 3;
    }

    Bitmap getAppTransitionThumbnailHeader(int taskId) {
        AppTransitionAnimationSpec spec = (AppTransitionAnimationSpec) this.mNextAppTransitionAnimationsSpecs.get(taskId);
        if (spec == null) {
            spec = this.mDefaultNextAppTransitionAnimationSpec;
        }
        if (spec != null) {
            return spec.bitmap;
        }
        return null;
    }

    boolean isNextThumbnailTransitionAspectScaled() {
        if (this.mNextAppTransitionType == 5 || this.mNextAppTransitionType == 6) {
            return true;
        }
        return false;
    }

    boolean isNextThumbnailTransitionScaleUp() {
        return this.mNextAppTransitionScaleUp;
    }

    boolean isNextAppTransitionThumbnailUp() {
        if (this.mNextAppTransitionType == 3 || this.mNextAppTransitionType == 5) {
            return true;
        }
        return false;
    }

    boolean isNextAppTransitionThumbnailDown() {
        if (this.mNextAppTransitionType == 4 || this.mNextAppTransitionType == 6) {
            return true;
        }
        return false;
    }

    boolean isFetchingAppTransitionsSpecs() {
        return this.mNextAppTransitionAnimationsSpecsPending;
    }

    private boolean prepare() {
        if (isRunning()) {
            return false;
        }
        this.mAppTransitionState = 0;
        notifyAppTransitionPendingLocked();
        this.mLastHadClipReveal = false;
        this.mLastClipRevealMaxTranslation = 0;
        this.mLastClipRevealTransitionDuration = 336;
        return true;
    }

    void goodToGo(AppWindowAnimator topOpeningAppAnimator, AppWindowAnimator topClosingAppAnimator, ArraySet<AppWindowToken> openingApps, ArraySet<AppWindowToken> arraySet) {
        IBinder iBinder;
        IBinder iBinder2;
        Animation animation;
        Animation animation2 = null;
        int appTransition = this.mNextAppTransition;
        this.mNextAppTransition = -1;
        this.mAppTransitionState = 2;
        if (topOpeningAppAnimator != null) {
            iBinder = topOpeningAppAnimator.mAppToken.token;
        } else {
            iBinder = null;
        }
        if (topClosingAppAnimator != null) {
            iBinder2 = topClosingAppAnimator.mAppToken.token;
        } else {
            iBinder2 = null;
        }
        if (topOpeningAppAnimator != null) {
            animation = topOpeningAppAnimator.animation;
        } else {
            animation = null;
        }
        if (topClosingAppAnimator != null) {
            animation2 = topClosingAppAnimator.animation;
        }
        notifyAppTransitionStartingLocked(iBinder, iBinder2, animation, animation2);
        this.mService.getDefaultDisplayContentLocked().getDockedDividerController().notifyAppTransitionStarting(openingApps, appTransition);
        if (this.mNextAppTransition == 19 && !this.mProlongedAnimationsEnded) {
            for (int i = openingApps.size() - 1; i >= 0; i--) {
                ((AppWindowToken) openingApps.valueAt(i)).mAppAnimator.startProlongAnimation(2);
            }
        }
    }

    void notifyProlongedAnimationsEnded() {
        this.mProlongedAnimationsEnded = true;
    }

    void clear() {
        this.mNextAppTransitionType = 0;
        this.mNextAppTransitionPackage = null;
        this.mNextAppTransitionAnimationsSpecs.clear();
        this.mNextAppTransitionAnimationsSpecsFuture = null;
        this.mDefaultNextAppTransitionAnimationSpec = null;
        this.mAnimationFinishedCallback = null;
        this.mProlongedAnimationsEnded = false;
    }

    void freeze() {
        setAppTransition(-1);
        clear();
        setReady();
        notifyAppTransitionCancelledLocked();
    }

    void registerListenerLocked(AppTransitionListener listener) {
        this.mListeners.add(listener);
    }

    public void notifyAppTransitionFinishedLocked(IBinder token) {
        for (int i = 0; i < this.mListeners.size(); i++) {
            ((AppTransitionListener) this.mListeners.get(i)).onAppTransitionFinishedLocked(token);
        }
    }

    private void notifyAppTransitionPendingLocked() {
        for (int i = 0; i < this.mListeners.size(); i++) {
            ((AppTransitionListener) this.mListeners.get(i)).onAppTransitionPendingLocked();
        }
    }

    private void notifyAppTransitionCancelledLocked() {
        for (int i = 0; i < this.mListeners.size(); i++) {
            ((AppTransitionListener) this.mListeners.get(i)).onAppTransitionCancelledLocked();
        }
    }

    private void notifyAppTransitionStartingLocked(IBinder openToken, IBinder closeToken, Animation openAnimation, Animation closeAnimation) {
        for (int i = 0; i < this.mListeners.size(); i++) {
            ((AppTransitionListener) this.mListeners.get(i)).onAppTransitionStartingLocked(openToken, closeToken, openAnimation, closeAnimation);
        }
    }

    private Entry getCachedAnimations(LayoutParams lp) {
        if (WindowManagerDebugConfig.DEBUG_ANIM) {
            String str;
            String str2 = TAG;
            StringBuilder append = new StringBuilder().append("Loading animations: layout params pkg=");
            if (lp != null) {
                str = lp.packageName;
            } else {
                str = null;
            }
            append = append.append(str).append(" resId=0x");
            if (lp != null) {
                str = Integer.toHexString(lp.windowAnimations);
            } else {
                str = null;
            }
            Slog.v(str2, append.append(str).toString());
        }
        if (lp == null || lp.windowAnimations == 0) {
            return null;
        }
        String packageName = lp.packageName != null ? lp.packageName : "android";
        int resId = lp.windowAnimations;
        if ((UsbAudioDevice.kAudioDeviceMetaMask & resId) == 16777216) {
            packageName = "android";
        }
        if (WindowManagerDebugConfig.DEBUG_ANIM) {
            Slog.v(TAG, "Loading animations: picked package=" + packageName);
        }
        return AttributeCache.instance().get(packageName, resId, R.styleable.WindowAnimation, this.mCurrentUserId);
    }

    private Entry getCachedAnimations(String packageName, int resId) {
        if (WindowManagerDebugConfig.DEBUG_ANIM) {
            Slog.v(TAG, "Loading animations: package=" + packageName + " resId=0x" + Integer.toHexString(resId));
        }
        if (packageName == null) {
            return null;
        }
        if ((UsbAudioDevice.kAudioDeviceMetaMask & resId) == 16777216) {
            packageName = "android";
        }
        if (WindowManagerDebugConfig.DEBUG_ANIM) {
            Slog.v(TAG, "Loading animations: picked package=" + packageName);
        }
        return AttributeCache.instance().get(packageName, resId, R.styleable.WindowAnimation, this.mCurrentUserId);
    }

    Animation loadAnimationAttr(LayoutParams lp, int animAttr) {
        int anim = 0;
        Context context = this.mContext;
        if (animAttr >= 0) {
            Entry ent = getCachedAnimations(lp);
            if (ent != null) {
                context = ent.context;
                anim = ent.array.getResourceId(animAttr, 0);
            }
        }
        if (anim != 0) {
            return AnimationUtils.loadAnimation(context, anim);
        }
        return null;
    }

    Animation loadAnimationRes(LayoutParams lp, int resId) {
        Context context = this.mContext;
        if (resId < 0 || OppoAnimSynthesisNumber.isSynthesisNumber(resId)) {
            return null;
        }
        Entry ent = getCachedAnimations(lp);
        if (ent != null) {
            context = ent.context;
        }
        return AnimationUtils.loadAnimation(context, resId);
    }

    protected Animation loadAnimationRes(String packageName, int resId) {
        int anim = 0;
        Context context = this.mContext;
        if (resId >= 0 && !OppoAnimSynthesisNumber.isSynthesisNumber(resId)) {
            Entry ent = getCachedAnimations(packageName, resId);
            if (ent != null) {
                context = ent.context;
                anim = resId;
            }
        }
        if (anim != 0) {
            return AnimationUtils.loadAnimation(context, anim);
        }
        return null;
    }

    private static float computePivot(int startPos, float finalScale) {
        float denom = finalScale - 1.0f;
        if (Math.abs(denom) < 1.0E-4f) {
            return (float) startPos;
        }
        return ((float) (-startPos)) / denom;
    }

    private Animation createScaleUpAnimationLocked(int transit, boolean enter, Rect containingFrame) {
        Animation a;
        long duration;
        getDefaultNextAppTransitionStartRect(this.mTmpRect);
        int appWidth = containingFrame.width();
        int appHeight = containingFrame.height();
        if (enter) {
            float scaleW = ((float) this.mTmpRect.width()) / ((float) appWidth);
            float scaleH = ((float) this.mTmpRect.height()) / ((float) appHeight);
            Animation scale = new ScaleAnimation(scaleW, 1.0f, scaleH, 1.0f, computePivot(this.mTmpRect.left, scaleW), computePivot(this.mTmpRect.top, scaleH));
            scale.setInterpolator(this.mDecelerateInterpolator);
            Animation alpha = new AlphaAnimation(OppoBrightUtils.MIN_LUX_LIMITI, 1.0f);
            alpha.setInterpolator(this.mThumbnailFadeOutInterpolator);
            Animation set = new AnimationSet(false);
            set.addAnimation(scale);
            set.addAnimation(alpha);
            set.setDetachWallpaper(true);
            a = set;
        } else if (transit == 14 || transit == 15) {
            a = new AlphaAnimation(1.0f, OppoBrightUtils.MIN_LUX_LIMITI);
            a.setDetachWallpaper(true);
        } else {
            a = new AlphaAnimation(1.0f, 1.0f);
        }
        switch (transit) {
            case 6:
            case 7:
                duration = (long) this.mConfigShortAnimTime;
                break;
            default:
                duration = 336;
                break;
        }
        a.setDuration(duration);
        a.setFillAfter(true);
        a.setInterpolator(this.mDecelerateInterpolator);
        a.initialize(appWidth, appHeight, appWidth, appHeight);
        return a;
    }

    private void getDefaultNextAppTransitionStartRect(Rect rect) {
        if (this.mDefaultNextAppTransitionAnimationSpec == null || this.mDefaultNextAppTransitionAnimationSpec.rect == null) {
            Slog.e(TAG, "Starting rect for app requested, but none available", new Throwable());
            rect.setEmpty();
            return;
        }
        rect.set(this.mDefaultNextAppTransitionAnimationSpec.rect);
    }

    void getNextAppTransitionStartRect(int taskId, Rect rect) {
        AppTransitionAnimationSpec spec = (AppTransitionAnimationSpec) this.mNextAppTransitionAnimationsSpecs.get(taskId);
        if (spec == null) {
            spec = this.mDefaultNextAppTransitionAnimationSpec;
        }
        if (spec == null || spec.rect == null) {
            Slog.wtf(TAG, "Starting rect for task: " + taskId + " requested, but not available", new Throwable());
            rect.setEmpty();
            return;
        }
        rect.set(spec.rect);
    }

    protected void putDefaultNextAppTransitionCoordinates(int left, int top, int width, int height, Bitmap bitmap) {
        this.mDefaultNextAppTransitionAnimationSpec = new AppTransitionAnimationSpec(-1, bitmap, new Rect(left, top, left + width, top + height));
    }

    long getLastClipRevealTransitionDuration() {
        return this.mLastClipRevealTransitionDuration;
    }

    int getLastClipRevealMaxTranslation() {
        return this.mLastClipRevealMaxTranslation;
    }

    boolean hadClipRevealAnimation() {
        return this.mLastHadClipReveal;
    }

    private long calculateClipRevealTransitionDuration(boolean cutOff, float translationX, float translationY, Rect displayFrame) {
        if (cutOff) {
            return (long) ((84.0f * Math.max(Math.abs(translationX) / ((float) displayFrame.width()), Math.abs(translationY) / ((float) displayFrame.height()))) + 336.0f);
        }
        return 336;
    }

    private Animation createClipRevealAnimationLocked(int transit, boolean enter, Rect appFrame, Rect displayFrame) {
        Animation anim;
        long duration;
        if (enter) {
            Interpolator interpolator;
            int appWidth = appFrame.width();
            int appHeight = appFrame.height();
            getDefaultNextAppTransitionStartRect(this.mTmpRect);
            float t = OppoBrightUtils.MIN_LUX_LIMITI;
            if (appHeight > 0) {
                t = ((float) this.mTmpRect.top) / ((float) displayFrame.height());
            }
            int translationY = this.mClipRevealTranslationY + ((int) ((((float) displayFrame.height()) / 7.0f) * t));
            int translationX = 0;
            int translationYCorrection = translationY;
            int centerX = this.mTmpRect.centerX();
            int centerY = this.mTmpRect.centerY();
            int halfWidth = this.mTmpRect.width() / 2;
            int halfHeight = this.mTmpRect.height() / 2;
            int clipStartX = (centerX - halfWidth) - appFrame.left;
            int clipStartY = (centerY - halfHeight) - appFrame.top;
            boolean cutOff = false;
            if (appFrame.top > centerY - halfHeight) {
                translationY = (centerY - halfHeight) - appFrame.top;
                translationYCorrection = 0;
                clipStartY = 0;
                cutOff = true;
            }
            if (appFrame.left > centerX - halfWidth) {
                translationX = (centerX - halfWidth) - appFrame.left;
                clipStartX = 0;
                cutOff = true;
            }
            if (appFrame.right < centerX + halfWidth) {
                translationX = (centerX + halfWidth) - appFrame.right;
                clipStartX = appWidth - this.mTmpRect.width();
                cutOff = true;
            }
            duration = calculateClipRevealTransitionDuration(cutOff, (float) translationX, (float) translationY, displayFrame);
            Animation clipRectLRAnimation = new ClipRectLRAnimation(clipStartX, this.mTmpRect.width() + clipStartX, 0, appWidth);
            clipRectLRAnimation.setInterpolator(this.mClipHorizontalInterpolator);
            clipRectLRAnimation.setDuration((long) (((float) duration) / 2.5f));
            clipRectLRAnimation = new TranslateAnimation((float) translationX, OppoBrightUtils.MIN_LUX_LIMITI, (float) translationY, OppoBrightUtils.MIN_LUX_LIMITI);
            if (cutOff) {
                interpolator = TOUCH_RESPONSE_INTERPOLATOR;
            } else {
                interpolator = this.mLinearOutSlowInInterpolator;
            }
            clipRectLRAnimation.setInterpolator(interpolator);
            clipRectLRAnimation.setDuration(duration);
            Animation clipAnimTB = new ClipRectTBAnimation(clipStartY, this.mTmpRect.height() + clipStartY, 0, appHeight, translationYCorrection, 0, this.mLinearOutSlowInInterpolator);
            clipAnimTB.setInterpolator(TOUCH_RESPONSE_INTERPOLATOR);
            clipAnimTB.setDuration(duration);
            long alphaDuration = duration / 4;
            AlphaAnimation alpha = new AlphaAnimation(0.5f, 1.0f);
            alpha.setDuration(alphaDuration);
            alpha.setInterpolator(this.mLinearOutSlowInInterpolator);
            clipRectLRAnimation = new AnimationSet(false);
            clipRectLRAnimation.addAnimation(clipRectLRAnimation);
            clipRectLRAnimation.addAnimation(clipAnimTB);
            clipRectLRAnimation.addAnimation(clipRectLRAnimation);
            clipRectLRAnimation.addAnimation(alpha);
            clipRectLRAnimation.setZAdjustment(1);
            clipRectLRAnimation.initialize(appWidth, appHeight, appWidth, appHeight);
            anim = clipRectLRAnimation;
            this.mLastHadClipReveal = true;
            this.mLastClipRevealTransitionDuration = duration;
            this.mLastClipRevealMaxTranslation = cutOff ? Math.max(Math.abs(translationY), Math.abs(translationX)) : 0;
        } else {
            switch (transit) {
                case 6:
                case 7:
                    duration = (long) this.mConfigShortAnimTime;
                    break;
                default:
                    duration = 336;
                    break;
            }
            if (transit == 14 || transit == 15) {
                anim = new AlphaAnimation(1.0f, OppoBrightUtils.MIN_LUX_LIMITI);
                anim.setDetachWallpaper(true);
            } else {
                anim = new AlphaAnimation(1.0f, 1.0f);
            }
            anim.setInterpolator(this.mDecelerateInterpolator);
            anim.setDuration(duration);
            anim.setFillAfter(true);
        }
        return anim;
    }

    Animation prepareThumbnailAnimationWithDuration(Animation a, int appWidth, int appHeight, long duration, Interpolator interpolator) {
        if (duration > 0) {
            a.setDuration(duration);
        }
        a.setFillAfter(true);
        if (interpolator != null) {
            a.setInterpolator(interpolator);
        }
        a.initialize(appWidth, appHeight, appWidth, appHeight);
        return a;
    }

    Animation prepareThumbnailAnimation(Animation a, int appWidth, int appHeight, int transit) {
        int duration;
        switch (transit) {
            case 6:
            case 7:
                duration = this.mConfigShortAnimTime;
                break;
            default:
                duration = 336;
                break;
        }
        return prepareThumbnailAnimationWithDuration(a, appWidth, appHeight, (long) duration, this.mDecelerateInterpolator);
    }

    int getThumbnailTransitionState(boolean enter) {
        if (enter) {
            if (this.mNextAppTransitionScaleUp) {
                return 0;
            }
            return 2;
        } else if (this.mNextAppTransitionScaleUp) {
            return 1;
        } else {
            return 3;
        }
    }

    Animation createThumbnailAspectScaleAnimationLocked(Rect appRect, Rect contentInsets, Bitmap thumbnailHeader, int taskId, int uiMode, int orientation) {
        float fromX;
        float fromY;
        float toX;
        float toY;
        float pivotX;
        float pivotY;
        Animation a;
        int thumbWidthI = thumbnailHeader.getWidth();
        float thumbWidth = (float) (thumbWidthI > 0 ? thumbWidthI : 1);
        int thumbHeightI = thumbnailHeader.getHeight();
        int appWidth = appRect.width();
        float scaleW = ((float) appWidth) / thumbWidth;
        getNextAppTransitionStartRect(taskId, this.mTmpRect);
        if (isTvUiMode(uiMode) || orientation == 1) {
            fromX = (float) this.mTmpRect.left;
            fromY = (float) this.mTmpRect.top;
            toX = (((float) (this.mTmpRect.width() / 2)) * (scaleW - 1.0f)) + ((float) appRect.left);
            toY = (((float) (appRect.height() / 2)) * (1.0f - (1.0f / scaleW))) + ((float) appRect.top);
            pivotX = (float) (this.mTmpRect.width() / 2);
            pivotY = ((float) (appRect.height() / 2)) / scaleW;
        } else {
            pivotX = OppoBrightUtils.MIN_LUX_LIMITI;
            pivotY = OppoBrightUtils.MIN_LUX_LIMITI;
            fromX = (float) this.mTmpRect.left;
            fromY = (float) this.mTmpRect.top;
            toX = (float) appRect.left;
            toY = (float) appRect.top;
        }
        long duration = getAspectScaleDuration();
        Interpolator interpolator = getAspectScaleInterpolator();
        Animation alphaAnimation;
        Animation translate;
        if (this.mNextAppTransitionScaleUp) {
            long j;
            Animation scale = new ScaleAnimation(1.0f, scaleW, 1.0f, scaleW, pivotX, pivotY);
            scale.setInterpolator(interpolator);
            scale.setDuration(duration);
            alphaAnimation = new AlphaAnimation(1.0f, OppoBrightUtils.MIN_LUX_LIMITI);
            alphaAnimation.setInterpolator(this.mNextAppTransition == 19 ? THUMBNAIL_DOCK_INTERPOLATOR : this.mThumbnailFadeOutInterpolator);
            if (this.mNextAppTransition == 19) {
                j = duration / 2;
            } else {
                j = duration;
            }
            alphaAnimation.setDuration(j);
            translate = createCurvedMotion(fromX, toX, fromY, toY);
            translate.setInterpolator(interpolator);
            translate.setDuration(duration);
            this.mTmpFromClipRect.set(0, 0, thumbWidthI, thumbHeightI);
            this.mTmpToClipRect.set(appRect);
            this.mTmpToClipRect.offsetTo(0, 0);
            this.mTmpToClipRect.right = (int) (((float) this.mTmpToClipRect.right) / scaleW);
            this.mTmpToClipRect.bottom = (int) (((float) this.mTmpToClipRect.bottom) / scaleW);
            if (contentInsets != null) {
                this.mTmpToClipRect.inset((int) (((float) (-contentInsets.left)) * scaleW), (int) (((float) (-contentInsets.top)) * scaleW), (int) (((float) (-contentInsets.right)) * scaleW), (int) (((float) (-contentInsets.bottom)) * scaleW));
            }
            alphaAnimation = new ClipRectAnimation(this.mTmpFromClipRect, this.mTmpToClipRect);
            alphaAnimation.setInterpolator(interpolator);
            alphaAnimation.setDuration(duration);
            alphaAnimation = new AnimationSet(false);
            alphaAnimation.addAnimation(scale);
            alphaAnimation.addAnimation(alphaAnimation);
            alphaAnimation.addAnimation(translate);
            alphaAnimation.addAnimation(alphaAnimation);
            a = alphaAnimation;
        } else {
            Animation scaleAnimation = new ScaleAnimation(scaleW, 1.0f, scaleW, 1.0f, pivotX, pivotY);
            scaleAnimation.setInterpolator(interpolator);
            scaleAnimation.setDuration(duration);
            alphaAnimation = new AlphaAnimation(OppoBrightUtils.MIN_LUX_LIMITI, 1.0f);
            alphaAnimation.setInterpolator(this.mThumbnailFadeInInterpolator);
            alphaAnimation.setDuration(duration);
            translate = createCurvedMotion(toX, fromX, toY, fromY);
            translate.setInterpolator(interpolator);
            translate.setDuration(duration);
            alphaAnimation = new AnimationSet(false);
            alphaAnimation.addAnimation(scaleAnimation);
            alphaAnimation.addAnimation(alphaAnimation);
            alphaAnimation.addAnimation(translate);
            a = alphaAnimation;
        }
        return prepareThumbnailAnimationWithDuration(a, appWidth, appRect.height(), 0, null);
    }

    private Animation createCurvedMotion(float fromX, float toX, float fromY, float toY) {
        if (Math.abs(toX - fromX) < 1.0f || this.mNextAppTransition != 19) {
            return new TranslateAnimation(fromX, toX, fromY, toY);
        }
        return new CurvedTranslateAnimation(createCurvedPath(fromX, toX, fromY, toY));
    }

    private Path createCurvedPath(float fromX, float toX, float fromY, float toY) {
        Path path = new Path();
        path.moveTo(fromX, fromY);
        if (fromY > toY) {
            path.cubicTo(fromX, fromY, toX, (0.9f * fromY) + (0.1f * toY), toX, toY);
        } else {
            path.cubicTo(fromX, fromY, fromX, (0.1f * fromY) + (0.9f * toY), toX, toY);
        }
        return path;
    }

    private long getAspectScaleDuration() {
        if (this.mNextAppTransition == 19) {
            return 453;
        }
        return 336;
    }

    private Interpolator getAspectScaleInterpolator() {
        if (this.mNextAppTransition == 19) {
            return this.mFastOutSlowInInterpolator;
        }
        return TOUCH_RESPONSE_INTERPOLATOR;
    }

    Animation createAspectScaledThumbnailEnterExitAnimationLocked(int thumbTransitState, int uiMode, int orientation, int transit, Rect containingFrame, Rect contentInsets, Rect surfaceInsets, boolean freeform, int taskId) {
        Animation a;
        int appWidth = containingFrame.width();
        int appHeight = containingFrame.height();
        getDefaultNextAppTransitionStartRect(this.mTmpRect);
        int thumbWidthI = this.mTmpRect.width();
        float thumbWidth = (float) (thumbWidthI > 0 ? thumbWidthI : 1);
        int thumbHeightI = this.mTmpRect.height();
        float thumbHeight = (float) (thumbHeightI > 0 ? thumbHeightI : 1);
        int thumbStartX = (this.mTmpRect.left - containingFrame.left) - contentInsets.left;
        int thumbStartY = this.mTmpRect.top - containingFrame.top;
        switch (thumbTransitState) {
            case 0:
            case 3:
                boolean scaleUp = thumbTransitState == 0;
                if (!freeform || !scaleUp) {
                    if (!freeform) {
                        Animation animationSet = new AnimationSet(true);
                        this.mTmpFromClipRect.set(containingFrame);
                        this.mTmpToClipRect.set(containingFrame);
                        this.mTmpFromClipRect.offsetTo(0, 0);
                        this.mTmpToClipRect.offsetTo(0, 0);
                        this.mTmpFromClipRect.inset(contentInsets);
                        this.mNextAppTransitionInsets.set(contentInsets);
                        Animation clipAnim;
                        Animation translateAnim;
                        if (isTvUiMode(uiMode) || orientation == 1) {
                            float f;
                            float scale = thumbWidth / ((float) ((appWidth - contentInsets.left) - contentInsets.right));
                            this.mTmpFromClipRect.bottom = this.mTmpFromClipRect.top + ((int) (thumbHeight / scale));
                            this.mNextAppTransitionInsets.set(contentInsets);
                            float f2 = scaleUp ? scale : 1.0f;
                            float f3 = scaleUp ? 1.0f : scale;
                            float f4 = scaleUp ? scale : 1.0f;
                            if (scaleUp) {
                                f = 1.0f;
                            } else {
                                f = scale;
                            }
                            Animation scaleAnim = new ScaleAnimation(f2, f3, f4, f, ((float) containingFrame.width()) / 2.0f, (((float) containingFrame.height()) / 2.0f) + ((float) contentInsets.top));
                            float startX = ((float) (this.mTmpRect.left - containingFrame.left)) - ((((float) containingFrame.width()) / 2.0f) - ((((float) containingFrame.width()) / 2.0f) * scale));
                            float startY = ((float) (this.mTmpRect.top - containingFrame.top)) - ((((float) containingFrame.height()) / 2.0f) - ((((float) containingFrame.height()) / 2.0f) * scale));
                            if (scaleUp) {
                                clipAnim = new ClipRectAnimation(this.mTmpFromClipRect, this.mTmpToClipRect);
                            } else {
                                clipAnim = new ClipRectAnimation(this.mTmpToClipRect, this.mTmpFromClipRect);
                            }
                            if (scaleUp) {
                                translateAnim = createCurvedMotion(startX, OppoBrightUtils.MIN_LUX_LIMITI, startY - ((float) contentInsets.top), OppoBrightUtils.MIN_LUX_LIMITI);
                            } else {
                                translateAnim = createCurvedMotion(OppoBrightUtils.MIN_LUX_LIMITI, startX, OppoBrightUtils.MIN_LUX_LIMITI, startY - ((float) contentInsets.top));
                            }
                            animationSet.addAnimation(clipAnim);
                            animationSet.addAnimation(scaleAnim);
                            animationSet.addAnimation(translateAnim);
                        } else {
                            this.mTmpFromClipRect.bottom = this.mTmpFromClipRect.top + thumbHeightI;
                            this.mTmpFromClipRect.right = this.mTmpFromClipRect.left + thumbWidthI;
                            if (scaleUp) {
                                clipAnim = new ClipRectAnimation(this.mTmpFromClipRect, this.mTmpToClipRect);
                            } else {
                                clipAnim = new ClipRectAnimation(this.mTmpToClipRect, this.mTmpFromClipRect);
                            }
                            if (scaleUp) {
                                translateAnim = createCurvedMotion((float) thumbStartX, OppoBrightUtils.MIN_LUX_LIMITI, (float) (thumbStartY - contentInsets.top), OppoBrightUtils.MIN_LUX_LIMITI);
                            } else {
                                translateAnim = createCurvedMotion(OppoBrightUtils.MIN_LUX_LIMITI, (float) thumbStartX, OppoBrightUtils.MIN_LUX_LIMITI, (float) (thumbStartY - contentInsets.top));
                            }
                            animationSet.addAnimation(clipAnim);
                            animationSet.addAnimation(translateAnim);
                        }
                        a = animationSet;
                        animationSet.setZAdjustment(1);
                        break;
                    }
                    a = createAspectScaledThumbnailExitFreeformAnimationLocked(containingFrame, surfaceInsets, taskId);
                    break;
                }
                a = createAspectScaledThumbnailEnterFreeformAnimationLocked(containingFrame, surfaceInsets, taskId);
                break;
                break;
            case 1:
                if (transit != 14) {
                    a = new AlphaAnimation(1.0f, 1.0f);
                    break;
                }
                a = new AlphaAnimation(1.0f, OppoBrightUtils.MIN_LUX_LIMITI);
                break;
            case 2:
                if (transit != 14) {
                    a = new AlphaAnimation(1.0f, 1.0f);
                    break;
                }
                a = new AlphaAnimation(OppoBrightUtils.MIN_LUX_LIMITI, 1.0f);
                break;
            default:
                throw new RuntimeException("Invalid thumbnail transition state");
        }
        return prepareThumbnailAnimationWithDuration(a, appWidth, appHeight, getAspectScaleDuration(), getAspectScaleInterpolator());
    }

    private Animation createAspectScaledThumbnailEnterFreeformAnimationLocked(Rect frame, Rect surfaceInsets, int taskId) {
        getNextAppTransitionStartRect(taskId, this.mTmpRect);
        return createAspectScaledThumbnailFreeformAnimationLocked(this.mTmpRect, frame, surfaceInsets, true);
    }

    private Animation createAspectScaledThumbnailExitFreeformAnimationLocked(Rect frame, Rect surfaceInsets, int taskId) {
        getNextAppTransitionStartRect(taskId, this.mTmpRect);
        return createAspectScaledThumbnailFreeformAnimationLocked(frame, this.mTmpRect, surfaceInsets, false);
    }

    private AnimationSet createAspectScaledThumbnailFreeformAnimationLocked(Rect sourceFrame, Rect destFrame, Rect surfaceInsets, boolean enter) {
        ScaleAnimation scale;
        float sourceWidth = (float) sourceFrame.width();
        float sourceHeight = (float) sourceFrame.height();
        float destWidth = (float) destFrame.width();
        float destHeight = (float) destFrame.height();
        float scaleH = enter ? sourceWidth / destWidth : destWidth / sourceWidth;
        float scaleV = enter ? sourceHeight / destHeight : destHeight / sourceHeight;
        AnimationSet animationSet = new AnimationSet(true);
        int surfaceInsetsH = surfaceInsets == null ? 0 : surfaceInsets.left + surfaceInsets.right;
        int surfaceInsetsV = surfaceInsets == null ? 0 : surfaceInsets.top + surfaceInsets.bottom;
        if (!enter) {
            destWidth = sourceWidth;
        }
        float scaleHCenter = (((float) surfaceInsetsH) + destWidth) / 2.0f;
        if (!enter) {
            destHeight = sourceHeight;
        }
        float scaleVCenter = (((float) surfaceInsetsV) + destHeight) / 2.0f;
        if (enter) {
            scale = new ScaleAnimation(scaleH, 1.0f, scaleV, 1.0f, scaleHCenter, scaleVCenter);
        } else {
            ScaleAnimation scaleAnimation = new ScaleAnimation(1.0f, scaleH, 1.0f, scaleV, scaleHCenter, scaleVCenter);
        }
        int sourceHCenter = sourceFrame.left + (sourceFrame.width() / 2);
        int sourceVCenter = sourceFrame.top + (sourceFrame.height() / 2);
        int destHCenter = destFrame.left + (destFrame.width() / 2);
        int destVCenter = destFrame.top + (destFrame.height() / 2);
        int fromX = enter ? sourceHCenter - destHCenter : destHCenter - sourceHCenter;
        int fromY = enter ? sourceVCenter - destVCenter : destVCenter - sourceVCenter;
        Animation translateAnimation;
        if (enter) {
            translateAnimation = new TranslateAnimation((float) fromX, OppoBrightUtils.MIN_LUX_LIMITI, (float) fromY, OppoBrightUtils.MIN_LUX_LIMITI);
        } else {
            translateAnimation = new TranslateAnimation(OppoBrightUtils.MIN_LUX_LIMITI, (float) fromX, OppoBrightUtils.MIN_LUX_LIMITI, (float) fromY);
        }
        animationSet.addAnimation(scale);
        animationSet.addAnimation(translation);
        IRemoteCallback callback = this.mAnimationFinishedCallback;
        if (callback != null) {
            final IRemoteCallback iRemoteCallback = callback;
            animationSet.setAnimationListener(new AnimationListener() {
                public void onAnimationStart(Animation animation) {
                }

                public void onAnimationEnd(Animation animation) {
                    AppTransition.this.mService.mH.obtainMessage(26, iRemoteCallback).sendToTarget();
                }

                public void onAnimationRepeat(Animation animation) {
                }
            });
        }
        return animationSet;
    }

    Animation createThumbnailScaleAnimationLocked(int appWidth, int appHeight, int transit, Bitmap thumbnailHeader) {
        Animation a;
        getDefaultNextAppTransitionStartRect(this.mTmpRect);
        int thumbWidthI = thumbnailHeader.getWidth();
        if (thumbWidthI <= 0) {
            thumbWidthI = 1;
        }
        float thumbWidth = (float) thumbWidthI;
        int thumbHeightI = thumbnailHeader.getHeight();
        if (thumbHeightI <= 0) {
            thumbHeightI = 1;
        }
        float thumbHeight = (float) thumbHeightI;
        float scaleW;
        float scaleH;
        if (this.mNextAppTransitionScaleUp) {
            scaleW = ((float) appWidth) / thumbWidth;
            scaleH = ((float) appHeight) / thumbHeight;
            Animation scale = new ScaleAnimation(1.0f, scaleW, 1.0f, scaleH, computePivot(this.mTmpRect.left, 1.0f / scaleW), computePivot(this.mTmpRect.top, 1.0f / scaleH));
            scale.setInterpolator(this.mDecelerateInterpolator);
            Animation alpha = new AlphaAnimation(1.0f, OppoBrightUtils.MIN_LUX_LIMITI);
            alpha.setInterpolator(this.mThumbnailFadeOutInterpolator);
            Animation set = new AnimationSet(false);
            set.addAnimation(scale);
            set.addAnimation(alpha);
            a = set;
        } else {
            scaleW = ((float) appWidth) / thumbWidth;
            scaleH = ((float) appHeight) / thumbHeight;
            a = new ScaleAnimation(scaleW, 1.0f, scaleH, 1.0f, computePivot(this.mTmpRect.left, 1.0f / scaleW), computePivot(this.mTmpRect.top, 1.0f / scaleH));
        }
        return prepareThumbnailAnimation(a, appWidth, appHeight, transit);
    }

    Animation createThumbnailEnterExitAnimationLocked(int thumbTransitState, Rect containingFrame, int transit, int taskId) {
        int thumbWidthI;
        int thumbHeightI;
        Animation a;
        int appWidth = containingFrame.width();
        int appHeight = containingFrame.height();
        Bitmap thumbnailHeader = getAppTransitionThumbnailHeader(taskId);
        getDefaultNextAppTransitionStartRect(this.mTmpRect);
        if (thumbnailHeader != null) {
            thumbWidthI = thumbnailHeader.getWidth();
        } else {
            thumbWidthI = appWidth;
        }
        if (thumbWidthI <= 0) {
            thumbWidthI = 1;
        }
        float thumbWidth = (float) thumbWidthI;
        if (thumbnailHeader != null) {
            thumbHeightI = thumbnailHeader.getHeight();
        } else {
            thumbHeightI = appHeight;
        }
        if (thumbHeightI <= 0) {
            thumbHeightI = 1;
        }
        float thumbHeight = (float) thumbHeightI;
        float scaleW;
        float scaleH;
        switch (thumbTransitState) {
            case 0:
                scaleW = thumbWidth / ((float) appWidth);
                scaleH = thumbHeight / ((float) appHeight);
                a = new ScaleAnimation(scaleW, 1.0f, scaleH, 1.0f, computePivot(this.mTmpRect.left, scaleW), computePivot(this.mTmpRect.top, scaleH));
                break;
            case 1:
                if (transit != 14) {
                    a = new AlphaAnimation(1.0f, 1.0f);
                    break;
                }
                a = new AlphaAnimation(1.0f, OppoBrightUtils.MIN_LUX_LIMITI);
                break;
            case 2:
                a = new AlphaAnimation(1.0f, 1.0f);
                break;
            case 3:
                scaleW = thumbWidth / ((float) appWidth);
                scaleH = thumbHeight / ((float) appHeight);
                Animation scale = new ScaleAnimation(1.0f, scaleW, 1.0f, scaleH, computePivot(this.mTmpRect.left, scaleW), computePivot(this.mTmpRect.top, scaleH));
                Animation alpha = new AlphaAnimation(1.0f, OppoBrightUtils.MIN_LUX_LIMITI);
                Animation animationSet = new AnimationSet(true);
                animationSet.addAnimation(scale);
                animationSet.addAnimation(alpha);
                animationSet.setZAdjustment(1);
                a = animationSet;
                break;
            default:
                throw new RuntimeException("Invalid thumbnail transition state");
        }
        return prepareThumbnailAnimation(a, appWidth, appHeight, transit);
    }

    private Animation createRelaunchAnimation(Rect containingFrame, Rect contentInsets) {
        getDefaultNextAppTransitionStartRect(this.mTmpFromClipRect);
        int left = this.mTmpFromClipRect.left;
        int top = this.mTmpFromClipRect.top;
        this.mTmpFromClipRect.offset(-left, -top);
        this.mTmpToClipRect.set(0, 0, containingFrame.width(), containingFrame.height());
        AnimationSet set = new AnimationSet(true);
        float fromWidth = (float) this.mTmpFromClipRect.width();
        float toWidth = (float) this.mTmpToClipRect.width();
        float fromHeight = (float) this.mTmpFromClipRect.height();
        float toHeight = (float) ((this.mTmpToClipRect.height() - contentInsets.top) - contentInsets.bottom);
        int translateAdjustment = 0;
        if (fromWidth > toWidth || fromHeight > toHeight) {
            set.addAnimation(new ScaleAnimation(fromWidth / toWidth, 1.0f, fromHeight / toHeight, 1.0f));
            translateAdjustment = (int) ((((float) contentInsets.top) * fromHeight) / toHeight);
        } else {
            set.addAnimation(new ClipRectAnimation(this.mTmpFromClipRect, this.mTmpToClipRect));
        }
        set.addAnimation(new TranslateAnimation((float) (left - containingFrame.left), OppoBrightUtils.MIN_LUX_LIMITI, (float) ((top - containingFrame.top) - translateAdjustment), OppoBrightUtils.MIN_LUX_LIMITI));
        set.setDuration(336);
        set.setZAdjustment(1);
        return set;
    }

    boolean canSkipFirstFrame() {
        if (this.mNextAppTransitionType == 1 || this.mNextAppTransitionType == 7) {
            return false;
        }
        return this.mNextAppTransitionType != 8;
    }

    Animation loadAnimation(LayoutParams lp, int transit, boolean enter, int uiMode, int orientation, Rect frame, Rect displayFrame, Rect insets, Rect surfaceInsets, boolean isVoiceInteraction, boolean freeform, int taskId) {
        Animation a;
        int i;
        if (isVoiceInteraction && (transit == 6 || transit == 8 || transit == 10)) {
            if (enter) {
                i = 17432737;
            } else {
                i = 17432738;
            }
            a = loadAnimationRes(lp, i);
            if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS || WindowManagerDebugConfig.DEBUG_ANIM) {
                Slog.v(TAG, "applyAnimation voice: anim=" + a + " transit=" + appTransitionToString(transit) + " isEntrance=" + enter + " Callers=" + Debug.getCallers(3));
            }
        } else if (isVoiceInteraction && (transit == 7 || transit == 9 || transit == 11)) {
            if (enter) {
                i = 17432735;
            } else {
                i = 17432736;
            }
            a = loadAnimationRes(lp, i);
            if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS || WindowManagerDebugConfig.DEBUG_ANIM) {
                Slog.v(TAG, "applyAnimation voice: anim=" + a + " transit=" + appTransitionToString(transit) + " isEntrance=" + enter + " Callers=" + Debug.getCallers(3));
            }
        } else if (transit == 18) {
            a = createRelaunchAnimation(frame, insets);
            if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS || WindowManagerDebugConfig.DEBUG_ANIM) {
                Slog.v(TAG, "applyAnimation: anim=" + a + " nextAppTransition=" + this.mNextAppTransition + " transit=" + appTransitionToString(transit) + " Callers=" + Debug.getCallers(3));
            }
        } else if (this.mNextAppTransitionType == 1) {
            a = loadAnimationRes(this.mNextAppTransitionPackage, enter ? this.mNextAppTransitionEnter : this.mNextAppTransitionExit);
            if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS || WindowManagerDebugConfig.DEBUG_ANIM) {
                Slog.v(TAG, "applyAnimation: anim=" + a + " nextAppTransition=ANIM_CUSTOM" + " transit=" + appTransitionToString(transit) + " isEntrance=" + enter + " Callers=" + Debug.getCallers(3));
            }
        } else if (this.mNextAppTransitionType == 7) {
            a = loadAnimationRes(this.mNextAppTransitionPackage, this.mNextAppTransitionInPlace);
            if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS || WindowManagerDebugConfig.DEBUG_ANIM) {
                Slog.v(TAG, "applyAnimation: anim=" + a + " nextAppTransition=ANIM_CUSTOM_IN_PLACE" + " transit=" + appTransitionToString(transit) + " Callers=" + Debug.getCallers(3));
            }
        } else if (this.mNextAppTransitionType == 8) {
            a = createClipRevealAnimationLocked(transit, enter, frame, displayFrame);
            if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS || WindowManagerDebugConfig.DEBUG_ANIM) {
                Slog.v(TAG, "applyAnimation: anim=" + a + " nextAppTransition=ANIM_CLIP_REVEAL" + " transit=" + appTransitionToString(transit) + " Callers=" + Debug.getCallers(3));
            }
        } else if (this.mNextAppTransitionType == 2) {
            a = createScaleUpAnimationLocked(transit, enter, frame);
            if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS || WindowManagerDebugConfig.DEBUG_ANIM) {
                Slog.v(TAG, "applyAnimation: anim=" + a + " nextAppTransition=ANIM_SCALE_UP" + " transit=" + appTransitionToString(transit) + " isEntrance=" + enter + " Callers=" + Debug.getCallers(3));
            }
        } else if (this.mNextAppTransitionType == 3 || this.mNextAppTransitionType == 4) {
            this.mNextAppTransitionScaleUp = this.mNextAppTransitionType == 3;
            a = createThumbnailEnterExitAnimationLocked(getThumbnailTransitionState(enter), frame, transit, taskId);
            if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS || WindowManagerDebugConfig.DEBUG_ANIM) {
                Slog.v(TAG, "applyAnimation: anim=" + a + " nextAppTransition=" + (this.mNextAppTransitionScaleUp ? "ANIM_THUMBNAIL_SCALE_UP" : "ANIM_THUMBNAIL_SCALE_DOWN") + " transit=" + appTransitionToString(transit) + " isEntrance=" + enter + " Callers=" + Debug.getCallers(3));
            }
        } else if (this.mNextAppTransitionType == 5 || this.mNextAppTransitionType == 6) {
            this.mNextAppTransitionScaleUp = this.mNextAppTransitionType == 5;
            a = createAspectScaledThumbnailEnterExitAnimationLocked(getThumbnailTransitionState(enter), uiMode, orientation, transit, frame, insets, surfaceInsets, freeform, taskId);
            if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS || WindowManagerDebugConfig.DEBUG_ANIM) {
                Slog.v(TAG, "applyAnimation: anim=" + a + " nextAppTransition=" + (this.mNextAppTransitionScaleUp ? "ANIM_THUMBNAIL_ASPECT_SCALE_UP" : "ANIM_THUMBNAIL_ASPECT_SCALE_DOWN") + " transit=" + appTransitionToString(transit) + " isEntrance=" + enter + " Callers=" + Debug.getCallers(3));
            }
        } else {
            int animAttr = 0;
            switch (transit) {
                case 6:
                    if (!enter) {
                        animAttr = 5;
                        break;
                    }
                    animAttr = 4;
                    break;
                case 7:
                    if (!enter) {
                        animAttr = 7;
                        break;
                    }
                    animAttr = 6;
                    break;
                case 8:
                case 19:
                    if (!enter) {
                        animAttr = 9;
                        break;
                    }
                    animAttr = 8;
                    break;
                case 9:
                    if (!enter) {
                        animAttr = 11;
                        break;
                    }
                    animAttr = 10;
                    break;
                case 10:
                    if (!enter) {
                        animAttr = 13;
                        break;
                    }
                    animAttr = 12;
                    break;
                case 11:
                    if (!enter) {
                        animAttr = 15;
                        break;
                    }
                    animAttr = 14;
                    break;
                case 12:
                    if (!enter) {
                        animAttr = 19;
                        break;
                    }
                    animAttr = 18;
                    break;
                case 13:
                    if (!enter) {
                        animAttr = 17;
                        break;
                    }
                    animAttr = 16;
                    break;
                case 14:
                    if (!enter) {
                        animAttr = 21;
                        break;
                    }
                    animAttr = 20;
                    break;
                case 15:
                    if (!enter) {
                        animAttr = 23;
                        break;
                    }
                    animAttr = 22;
                    break;
                case 16:
                    if (!enter) {
                        animAttr = 24;
                        break;
                    }
                    animAttr = 25;
                    break;
            }
            a = animAttr != 0 ? loadAnimationAttr(lp, animAttr) : null;
            if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS || WindowManagerDebugConfig.DEBUG_ANIM) {
                Slog.v(TAG, "applyAnimation: anim=" + a + " animAttr=0x" + Integer.toHexString(animAttr) + " transit=" + appTransitionToString(transit) + " isEntrance=" + enter + " Callers=" + Debug.getCallers(3));
            }
        }
        return a;
    }

    int getAppStackClipMode() {
        if (this.mNextAppTransition == 18 || this.mNextAppTransition == 19 || this.mNextAppTransitionType == 8) {
            return 2;
        }
        return 0;
    }

    void postAnimationCallback() {
        if (this.mNextAppTransitionCallback != null) {
            this.mService.mH.sendMessage(this.mService.mH.obtainMessage(26, this.mNextAppTransitionCallback));
            this.mNextAppTransitionCallback = null;
        }
    }

    void overridePendingAppTransition(String packageName, int enterAnim, int exitAnim, IRemoteCallback startedCallback) {
        if (isTransitionSet()) {
            clear();
            this.mNextAppTransitionType = 1;
            this.mNextAppTransitionPackage = packageName;
            this.mNextAppTransitionEnter = enterAnim;
            this.mNextAppTransitionExit = exitAnim;
            postAnimationCallback();
            this.mNextAppTransitionCallback = startedCallback;
            return;
        }
        postAnimationCallback();
    }

    void overridePendingAppTransitionScaleUp(int startX, int startY, int startWidth, int startHeight) {
        if (isTransitionSet()) {
            clear();
            this.mNextAppTransitionType = 2;
            putDefaultNextAppTransitionCoordinates(startX, startY, startWidth, startHeight, null);
            postAnimationCallback();
        }
    }

    void overridePendingAppTransitionClipReveal(int startX, int startY, int startWidth, int startHeight) {
        if (isTransitionSet()) {
            clear();
            this.mNextAppTransitionType = 8;
            putDefaultNextAppTransitionCoordinates(startX, startY, startWidth, startHeight, null);
            postAnimationCallback();
        }
    }

    void overridePendingAppTransitionThumb(Bitmap srcThumb, int startX, int startY, IRemoteCallback startedCallback, boolean scaleUp) {
        if (isTransitionSet()) {
            int i;
            clear();
            if (scaleUp) {
                i = 3;
            } else {
                i = 4;
            }
            this.mNextAppTransitionType = i;
            this.mNextAppTransitionScaleUp = scaleUp;
            putDefaultNextAppTransitionCoordinates(startX, startY, 0, 0, srcThumb);
            postAnimationCallback();
            this.mNextAppTransitionCallback = startedCallback;
            return;
        }
        postAnimationCallback();
    }

    void overridePendingAppTransitionAspectScaledThumb(Bitmap srcThumb, int startX, int startY, int targetWidth, int targetHeight, IRemoteCallback startedCallback, boolean scaleUp) {
        if (isTransitionSet()) {
            int i;
            clear();
            if (scaleUp) {
                i = 5;
            } else {
                i = 6;
            }
            this.mNextAppTransitionType = i;
            this.mNextAppTransitionScaleUp = scaleUp;
            putDefaultNextAppTransitionCoordinates(startX, startY, targetWidth, targetHeight, srcThumb);
            postAnimationCallback();
            this.mNextAppTransitionCallback = startedCallback;
            return;
        }
        postAnimationCallback();
    }

    public void overridePendingAppTransitionMultiThumb(AppTransitionAnimationSpec[] specs, IRemoteCallback onAnimationStartedCallback, IRemoteCallback onAnimationFinishedCallback, boolean scaleUp) {
        if (isTransitionSet()) {
            int i;
            clear();
            if (scaleUp) {
                i = 5;
            } else {
                i = 6;
            }
            this.mNextAppTransitionType = i;
            this.mNextAppTransitionScaleUp = scaleUp;
            if (specs != null) {
                for (int i2 = 0; i2 < specs.length; i2++) {
                    AppTransitionAnimationSpec spec = specs[i2];
                    if (spec != null) {
                        this.mNextAppTransitionAnimationsSpecs.put(spec.taskId, spec);
                        if (i2 == 0) {
                            Rect rect = spec.rect;
                            putDefaultNextAppTransitionCoordinates(rect.left, rect.top, rect.width(), rect.height(), spec.bitmap);
                        }
                    }
                }
            }
            postAnimationCallback();
            this.mNextAppTransitionCallback = onAnimationStartedCallback;
            this.mAnimationFinishedCallback = onAnimationFinishedCallback;
            return;
        }
        postAnimationCallback();
    }

    void overridePendingAppTransitionMultiThumbFuture(IAppTransitionAnimationSpecsFuture specsFuture, IRemoteCallback callback, boolean scaleUp) {
        Slog.v(TAG, "overridePendingAppTransitionMultiThumbFuture pid:" + Binder.getCallingPid());
        if (isTransitionSet()) {
            int i;
            clear();
            if (scaleUp) {
                i = 5;
            } else {
                i = 6;
            }
            this.mNextAppTransitionType = i;
            this.mNextAppTransitionAnimationsSpecsFuture = specsFuture;
            this.mNextAppTransitionScaleUp = scaleUp;
            this.mNextAppTransitionFutureCallback = callback;
        }
    }

    void overrideInPlaceAppTransition(String packageName, int anim) {
        if (isTransitionSet()) {
            clear();
            this.mNextAppTransitionType = 7;
            this.mNextAppTransitionPackage = packageName;
            this.mNextAppTransitionInPlace = anim;
            return;
        }
        postAnimationCallback();
    }

    private void fetchAppTransitionSpecsFromFuture() {
        if (this.mNextAppTransitionAnimationsSpecsFuture != null) {
            this.mNextAppTransitionAnimationsSpecsPending = true;
            final IAppTransitionAnimationSpecsFuture future = this.mNextAppTransitionAnimationsSpecsFuture;
            this.mNextAppTransitionAnimationsSpecsFuture = null;
            this.mService.mH.removeMessages(1002);
            this.mService.mH.sendEmptyMessageDelayed(1002, APP_TRANSITION_ANIMATION_SPECS_FUTURE_TIMEOUT_MS);
            this.mDefaultExecutor.execute(new Runnable() {
                public void run() {
                    AppTransitionAnimationSpec[] specs = null;
                    try {
                        specs = future.get();
                    } catch (RemoteException e) {
                        Slog.w(AppTransition.TAG, "Failed to fetch app transition specs: " + e);
                    }
                    synchronized (AppTransition.this.mService.mWindowMap) {
                        AppTransition.this.mNextAppTransitionAnimationsSpecsPending = false;
                        AppTransition.this.overridePendingAppTransitionMultiThumb(specs, AppTransition.this.mNextAppTransitionFutureCallback, null, AppTransition.this.mNextAppTransitionScaleUp);
                        AppTransition.this.mNextAppTransitionFutureCallback = null;
                        if (specs != null) {
                            AppTransition.this.mService.prolongAnimationsFromSpecs(specs, AppTransition.this.mNextAppTransitionScaleUp);
                        }
                    }
                    AppTransition.this.mService.requestTraversal();
                    AppTransition.this.mService.mH.removeMessages(1002);
                }
            });
        }
    }

    void handleAppTransitionSpecsFromFutureTimeout() {
        Slog.w(TAG, "handleAppTransitionSpecsFromFutureTimeout");
        synchronized (this.mService.mWindowMap) {
            this.mNextAppTransitionAnimationsSpecsPending = false;
            overridePendingAppTransitionMultiThumb(null, this.mNextAppTransitionFutureCallback, null, this.mNextAppTransitionScaleUp);
            this.mNextAppTransitionFutureCallback = null;
        }
        this.mService.requestTraversal();
    }

    public String toString() {
        return "mNextAppTransition=" + appTransitionToString(this.mNextAppTransition);
    }

    public static String appTransitionToString(int transition) {
        switch (transition) {
            case -1:
                return "TRANSIT_UNSET";
            case 0:
                return "TRANSIT_NONE";
            case 6:
                return "TRANSIT_ACTIVITY_OPEN";
            case 7:
                return "TRANSIT_ACTIVITY_CLOSE";
            case 8:
                return "TRANSIT_TASK_OPEN";
            case 9:
                return "TRANSIT_TASK_CLOSE";
            case 10:
                return "TRANSIT_TASK_TO_FRONT";
            case 11:
                return "TRANSIT_TASK_TO_BACK";
            case 12:
                return "TRANSIT_WALLPAPER_CLOSE";
            case 13:
                return "TRANSIT_WALLPAPER_OPEN";
            case 14:
                return "TRANSIT_WALLPAPER_INTRA_OPEN";
            case 15:
                return "TRANSIT_WALLPAPER_INTRA_CLOSE";
            case 16:
                return "TRANSIT_TASK_OPEN_BEHIND";
            case 18:
                return "TRANSIT_ACTIVITY_RELAUNCH";
            case 19:
                return "TRANSIT_DOCK_TASK_FROM_RECENTS";
            default:
                return "<UNKNOWN>";
        }
    }

    private String appStateToString() {
        switch (this.mAppTransitionState) {
            case 0:
                return "APP_STATE_IDLE";
            case 1:
                return "APP_STATE_READY";
            case 2:
                return "APP_STATE_RUNNING";
            case 3:
                return "APP_STATE_TIMEOUT";
            default:
                return "unknown state=" + this.mAppTransitionState;
        }
    }

    private String transitTypeToString() {
        switch (this.mNextAppTransitionType) {
            case 0:
                return "NEXT_TRANSIT_TYPE_NONE";
            case 1:
                return "NEXT_TRANSIT_TYPE_CUSTOM";
            case 2:
                return "NEXT_TRANSIT_TYPE_SCALE_UP";
            case 3:
                return "NEXT_TRANSIT_TYPE_THUMBNAIL_SCALE_UP";
            case 4:
                return "NEXT_TRANSIT_TYPE_THUMBNAIL_SCALE_DOWN";
            case 5:
                return "NEXT_TRANSIT_TYPE_THUMBNAIL_ASPECT_SCALE_UP";
            case 6:
                return "NEXT_TRANSIT_TYPE_THUMBNAIL_ASPECT_SCALE_DOWN";
            case 7:
                return "NEXT_TRANSIT_TYPE_CUSTOM_IN_PLACE";
            default:
                return "unknown type=" + this.mNextAppTransitionType;
        }
    }

    public void dump(PrintWriter pw, String prefix) {
        pw.print(prefix);
        pw.println(this);
        pw.print(prefix);
        pw.print("mAppTransitionState=");
        pw.println(appStateToString());
        if (this.mNextAppTransitionType != 0) {
            pw.print(prefix);
            pw.print("mNextAppTransitionType=");
            pw.println(transitTypeToString());
        }
        switch (this.mNextAppTransitionType) {
            case 1:
                pw.print(prefix);
                pw.print("mNextAppTransitionPackage=");
                pw.println(this.mNextAppTransitionPackage);
                pw.print(prefix);
                pw.print("mNextAppTransitionEnter=0x");
                pw.print(Integer.toHexString(this.mNextAppTransitionEnter));
                pw.print(" mNextAppTransitionExit=0x");
                pw.println(Integer.toHexString(this.mNextAppTransitionExit));
                break;
            case 2:
                getDefaultNextAppTransitionStartRect(this.mTmpRect);
                pw.print(prefix);
                pw.print("mNextAppTransitionStartX=");
                pw.print(this.mTmpRect.left);
                pw.print(" mNextAppTransitionStartY=");
                pw.println(this.mTmpRect.top);
                pw.print(prefix);
                pw.print("mNextAppTransitionStartWidth=");
                pw.print(this.mTmpRect.width());
                pw.print(" mNextAppTransitionStartHeight=");
                pw.println(this.mTmpRect.height());
                break;
            case 3:
            case 4:
            case 5:
            case 6:
                pw.print(prefix);
                pw.print("mDefaultNextAppTransitionAnimationSpec=");
                pw.println(this.mDefaultNextAppTransitionAnimationSpec);
                pw.print(prefix);
                pw.print("mNextAppTransitionAnimationsSpecs=");
                pw.println(this.mNextAppTransitionAnimationsSpecs);
                pw.print(prefix);
                pw.print("mNextAppTransitionScaleUp=");
                pw.println(this.mNextAppTransitionScaleUp);
                break;
            case 7:
                pw.print(prefix);
                pw.print("mNextAppTransitionPackage=");
                pw.println(this.mNextAppTransitionPackage);
                pw.print(prefix);
                pw.print("mNextAppTransitionInPlace=0x");
                pw.print(Integer.toHexString(this.mNextAppTransitionInPlace));
                break;
        }
        if (this.mNextAppTransitionCallback != null) {
            pw.print(prefix);
            pw.print("mNextAppTransitionCallback=");
            pw.println(this.mNextAppTransitionCallback);
        }
        if (this.mLastUsedAppTransition != 0) {
            pw.print(prefix);
            pw.print("mLastUsedAppTransition=");
            pw.println(appTransitionToString(this.mLastUsedAppTransition));
            pw.print(prefix);
            pw.print("mLastOpeningApp=");
            pw.println(this.mLastOpeningApp);
            pw.print(prefix);
            pw.print("mLastClosingApp=");
            pw.println(this.mLastClosingApp);
        }
    }

    public void setCurrentUser(int newUserId) {
        this.mCurrentUserId = newUserId;
    }

    boolean prepareAppTransitionLocked(int transit, boolean alwaysKeepCurrent) {
        if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS || WindowManagerService.DEBUG_WMS) {
            Slog.v(TAG, "Prepare app transition: transit=" + appTransitionToString(transit) + " " + this + " alwaysKeepCurrent=" + alwaysKeepCurrent + " Callers=" + Debug.getCallers(3));
        }
        if (!isTransitionSet() || this.mNextAppTransition == 0) {
            setAppTransition(transit);
        } else if (!alwaysKeepCurrent) {
            if (transit == 8 && isTransitionEqual(9)) {
                setAppTransition(transit);
            } else if (transit == 6 && isTransitionEqual(7)) {
                setAppTransition(transit);
            }
        }
        boolean prepared = prepare();
        if (isTransitionSet()) {
            this.mService.mH.removeMessages(13);
            this.mService.mH.sendEmptyMessageDelayed(13, 5000);
        }
        return prepared;
    }

    private boolean isTvUiMode(int uiMode) {
        return (uiMode & 4) > 0;
    }
}
