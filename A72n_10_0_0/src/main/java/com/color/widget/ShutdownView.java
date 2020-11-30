package com.color.widget;

import android.animation.ArgbEvaluator;
import android.animation.FloatEvaluator;
import android.animation.IntEvaluator;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.SystemProperties;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewRootImpl;
import android.view.WindowManagerGlobal;
import android.view.accessibility.AccessibilityManager;
import android.widget.Button;
import com.android.internal.widget.ColorViewExplorerByTouchHelper;
import com.color.util.ColorAccessibilityUtil;
import com.oppo.internal.R;
import java.lang.reflect.Method;

public class ShutdownView extends View implements ViewRootImpl.ConfigChangedCallback {
    static final int CANCEL_NODE_ID = 1;
    private static final String CONFIGURATION_MY = "my";
    static final float DEFAULT_BACKGROUND_ALPHA = 0.7f;
    static final float DEFAULT_BAR_ALPHA = 0.8f;
    public static final int DEFAULT_BAR_OFFSET = 75;
    public static final int DEFAULT_BAR_RADIUS = 46;
    static final float DEFAULT_FORCE_REBOOT_HINT_ALPHA = 0.5f;
    static final int EMERGENCY_NODE_ID = 2;
    static final int HINT_REBOOT = 0;
    static final int HINT_SHUTDOWN = 1;
    static final int MANUALLY_LOCK_NODE_ID = 3;
    static final int SHUT_DOWN_NODE_ID = 0;
    private String mAccessContent;
    RectF mAccessibilityHandlerRect;
    private ArgbEvaluator mArgbEvaluator;
    private float mBackgroundAlpha;
    private Drawable mBackgroundDrawable;
    private float mBarAlpha;
    private int mBarHeight;
    private Paint mBarMaskPaint;
    private int mBarOffset;
    private Paint mBarPaint;
    private int mBarRadius;
    RectF mBarRectF;
    private int mBarWidth;
    private int mCancelMarginRight;
    private int mCancelOffset;
    Rect mCancelRect;
    private String mCancelText;
    private int mCancelWidth;
    public float mCanvasStranslateXForHandler;
    private float mCanvasStranslateXForHandlerFloat;
    private ColorViewExplorerByTouchHelper.ColorViewTalkBalkInteraction mColorViewTalkBalkInteraction;
    private int mDefaultHandlerColor;
    private int mDefaultRebootColor;
    private int mDefaultShutdownColor;
    private float mEmergencyAlpha;
    private Paint mEmergencyBarPaint;
    RectF mEmergencyBarRectF;
    private Bitmap mEmergencyCallBitmap;
    private Drawable mEmergencyCallDrawable;
    private Drawable mEmergencyCallNormalDrawable;
    private Drawable mEmergencyCallPressedDrawable;
    private int mEmergencyMarginDrawable;
    private boolean mEmergencyPressed;
    Rect mEmergencyRect;
    private String mEmergencyText;
    private int mEmergencyTextMaxWidth;
    private TextPaint mEmergencyTextPaint;
    private FloatEvaluator mFloatEvaluator;
    private String mForceRebootHint;
    private float mForceRebootHintAlpha;
    private int mForceRebootHintMaxHeight;
    private int mForceRebootLandscapMarginBottom;
    private TextPaint mForceRebootPaint;
    private int mForceRebootPortraitMarginBottom;
    private float mForegroundAlpha;
    private Drawable mForegroundDrawable;
    private FingerprintManager mFpm;
    private float mHandlerAlpha;
    private int mHandlerColor;
    private Paint mHandlerPaint;
    RectF mHandlerRectF;
    private float mHandlerScale;
    private float mHandlerTransition;
    private int mHandlerWidth;
    private String mHint;
    private float mHintAlpha;
    private TextPaint mHintPaint;
    private float mIconScale;
    private boolean mIsMy;
    private boolean mIsPortrait;
    private boolean mIsSupportOnScreenFingerprint;
    private String mLandscapeRebootHint;
    private String mLandscapeShutdownHint;
    Rect mLockRect;
    private int mManuallLockWidth;
    private float mManuallyLockAlpha;
    RectF mManuallyLockBarRectF;
    private Paint mManuallyLockButtonPaint;
    private int mManuallyLockDrawableMargin;
    private boolean mManuallyLockEnable;
    private int mManuallyLockPressColor;
    private boolean mManuallyLockPressed;
    private Drawable mManuallyLockedDrawable;
    private Drawable mManuallyLockedNormalDrawable;
    private TextPaint mManuallyPaint;
    private String mManuallyText;
    private OperationListener mOperationListener;
    private OrientationChangeListener mOrientationChangeListener;
    private String mPortraitRebootHint;
    private String mPortraitShutdownHint;
    private float mRebootAlpha;
    private Bitmap mRebootBitmap;
    private int mRebootColor;
    private int mRebootLineWidth;
    private int mRebootOffset;
    private Paint mRebootPaint;
    private float mRebootRatio;
    private float mRebootScale;
    private String mRebootTextOnBar;
    private int mRebootTransition;
    private int mRebootWidth;
    private Resources mResources;
    private float mRoadAlpha1;
    private float mRoadAlpha2;
    private float mRoadAlpha3;
    private Paint mRoadPaint;
    private Path mRoadPath;
    private Rect mRoadRect;
    private int mRoadTransition;
    private float mScale;
    private float mShutdownAlpha;
    private Bitmap mShutdownBitmap;
    private int mShutdownColor;
    private int mShutdownLineWidth;
    private Paint mShutdownPaint;
    private float mShutdownRatio;
    private float mShutdownScale;
    private String mShutdownTextOnBar;
    private int mShutdownTransition;
    private int mTextMarginToBitmap;
    private TextPaint mTextOnBarPaint;
    private int mTextPadding;
    private TouchEventListener mTouchEventListener;
    private ColorViewExplorerByTouchHelper mTouchHelper;
    private Paint mTrianglePaint;
    private Path mTrianglePath;

    /* access modifiers changed from: package-private */
    public interface OperationListener {
        void cancel();

        void emergency();

        void manuallyLock();
    }

    interface OrientationChangeListener {
        void onOrientationChanged();
    }

    interface TouchEventListener {
        void onCancel(float f, float f2);

        boolean onDown(float f, float f2);

        void onMove(float f, float f2);

        void onPointerDown();

        void onUp(float f, float f2);
    }

    public ShutdownView(Context context) {
        this(context, null);
    }

    public ShutdownView(Context context, AttributeSet attrs) {
        this(context, attrs, 201393350);
    }

    public ShutdownView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mHandlerScale = 1.0f;
        this.mBackgroundAlpha = 0.0f;
        this.mForegroundAlpha = 0.0f;
        this.mIconScale = 1.0f;
        this.mScale = 1.0f;
        this.mRebootScale = 1.0f;
        this.mRebootRatio = 1.0f;
        this.mShutdownScale = 1.0f;
        this.mShutdownRatio = 1.0f;
        this.mRoadAlpha1 = 0.1f;
        this.mRoadAlpha2 = 0.1f;
        this.mRoadAlpha3 = 0.1f;
        this.mHandlerAlpha = 1.0f;
        this.mShutdownAlpha = 1.0f;
        this.mRebootAlpha = 0.0f;
        this.mHintAlpha = 1.0f;
        this.mEmergencyAlpha = 0.0f;
        this.mHint = "";
        this.mBarPaint = new Paint();
        this.mRebootPaint = new Paint();
        this.mShutdownPaint = new Paint();
        this.mHandlerPaint = new Paint();
        this.mRoadPaint = new Paint();
        this.mHintPaint = new TextPaint();
        this.mEmergencyTextPaint = new TextPaint();
        this.mTrianglePaint = new Paint();
        this.mRoadPath = new Path();
        this.mTrianglePath = new Path();
        this.mBarRectF = new RectF();
        this.mCancelRect = new Rect();
        this.mEmergencyRect = new Rect();
        this.mHandlerRectF = new RectF();
        this.mAccessibilityHandlerRect = new RectF();
        this.mRoadRect = new Rect();
        this.mArgbEvaluator = new ArgbEvaluator();
        this.mFloatEvaluator = new FloatEvaluator();
        this.mForceRebootHintAlpha = 0.0f;
        this.mForceRebootPaint = new TextPaint();
        this.mCanvasStranslateXForHandler = 0.0f;
        this.mEmergencyBarRectF = new RectF();
        this.mBarMaskPaint = new Paint();
        this.mEmergencyBarPaint = new Paint();
        this.mTextOnBarPaint = new TextPaint();
        this.mBarRadius = 46;
        this.mEmergencyPressed = false;
        this.mCanvasStranslateXForHandlerFloat = 0.0f;
        this.mLockRect = new Rect();
        this.mManuallyPaint = new TextPaint();
        this.mManuallyLockButtonPaint = new Paint();
        this.mManuallyLockBarRectF = new RectF();
        this.mManuallyLockAlpha = 0.0f;
        this.mColorViewTalkBalkInteraction = new ColorViewExplorerByTouchHelper.ColorViewTalkBalkInteraction() {
            /* class com.color.widget.ShutdownView.AnonymousClass1 */

            @Override // com.android.internal.widget.ColorViewExplorerByTouchHelper.ColorViewTalkBalkInteraction
            public void getItemBounds(int position, Rect rect) {
                if (position == 0) {
                    if (ShutdownView.this.isPortrait()) {
                        rect.set((int) (ShutdownView.this.mHandlerRectF.left + ShutdownView.this.mCanvasStranslateXForHandler), (int) ShutdownView.this.mHandlerRectF.top, (int) (ShutdownView.this.mHandlerRectF.right + ShutdownView.this.mCanvasStranslateXForHandler), (int) ShutdownView.this.mHandlerRectF.bottom);
                    } else {
                        rect.set((int) ShutdownView.this.mHandlerRectF.left, (int) (ShutdownView.this.mHandlerRectF.top + ShutdownView.this.mCanvasStranslateXForHandler), (int) ShutdownView.this.mHandlerRectF.right, (int) (ShutdownView.this.mHandlerRectF.bottom + ShutdownView.this.mCanvasStranslateXForHandler));
                    }
                } else if (position == 1) {
                    rect.set(ShutdownView.this.mCancelRect);
                } else if (position == 2) {
                    if (ShutdownView.this.isPortrait()) {
                        rect.set((int) (ShutdownView.this.mEmergencyBarRectF.left + ShutdownView.this.mCanvasStranslateXForHandler), (int) ShutdownView.this.mEmergencyBarRectF.top, (int) (ShutdownView.this.mEmergencyBarRectF.right + ShutdownView.this.mCanvasStranslateXForHandler), (int) ShutdownView.this.mEmergencyBarRectF.bottom);
                    } else {
                        rect.set((int) ShutdownView.this.mEmergencyBarRectF.left, (int) (ShutdownView.this.mEmergencyBarRectF.top + ShutdownView.this.mCanvasStranslateXForHandler), (int) ShutdownView.this.mEmergencyBarRectF.right, (int) (ShutdownView.this.mEmergencyBarRectF.bottom + ShutdownView.this.mCanvasStranslateXForHandler));
                    }
                } else if (ShutdownView.this.mManuallyLockEnable && position == 3) {
                    if (ShutdownView.this.isPortrait()) {
                        rect.set((int) (ShutdownView.this.mManuallyLockBarRectF.left + ShutdownView.this.mCanvasStranslateXForHandler), (int) ShutdownView.this.mManuallyLockBarRectF.top, (int) (ShutdownView.this.mManuallyLockBarRectF.right + ShutdownView.this.mCanvasStranslateXForHandler), (int) ShutdownView.this.mManuallyLockBarRectF.bottom);
                    } else {
                        rect.set((int) ShutdownView.this.mManuallyLockBarRectF.left, (int) (ShutdownView.this.mManuallyLockBarRectF.top + ShutdownView.this.mCanvasStranslateXForHandler), (int) ShutdownView.this.mManuallyLockBarRectF.right, (int) (ShutdownView.this.mManuallyLockBarRectF.bottom + ShutdownView.this.mCanvasStranslateXForHandler));
                    }
                }
            }

            @Override // com.android.internal.widget.ColorViewExplorerByTouchHelper.ColorViewTalkBalkInteraction
            public void performAction(int virtualViewId, int actionType, boolean resolvePara) {
                if (virtualViewId == 1) {
                    if (ShutdownView.this.mOperationListener != null) {
                        ShutdownView.this.mOperationListener.cancel();
                    }
                } else if (virtualViewId == 2) {
                    if (ShutdownView.this.mOperationListener != null) {
                        ShutdownView.this.mOperationListener.emergency();
                    }
                    ShutdownView.this.mTouchHelper.sendEventForVirtualView(2, 1);
                }
                if (ShutdownView.this.mManuallyLockEnable && virtualViewId == 3 && ShutdownView.this.mOperationListener != null) {
                    ShutdownView.this.mOperationListener.manuallyLock();
                }
            }

            @Override // com.android.internal.widget.ColorViewExplorerByTouchHelper.ColorViewTalkBalkInteraction
            public int getCurrentPosition() {
                return -2;
            }

            @Override // com.android.internal.widget.ColorViewExplorerByTouchHelper.ColorViewTalkBalkInteraction
            public int getItemCounts() {
                if (ShutdownView.this.mManuallyLockEnable) {
                    return 4;
                }
                return 3;
            }

            @Override // com.android.internal.widget.ColorViewExplorerByTouchHelper.ColorViewTalkBalkInteraction
            public int getVirtualViewAt(float x, float y) {
                if (ShutdownView.this.isTouchHandler(x, y)) {
                    return 0;
                }
                if (ShutdownView.this.isTouchEmergency(x, y)) {
                    return 2;
                }
                if (ShutdownView.this.mManuallyLockEnable && ShutdownView.this.isTouchManuallyLock(x, y)) {
                    return 3;
                }
                if (ShutdownView.this.mCancelRect.contains((int) x, (int) y)) {
                    return 1;
                }
                return -1;
            }

            @Override // com.android.internal.widget.ColorViewExplorerByTouchHelper.ColorViewTalkBalkInteraction
            public CharSequence getItemDescription(int virtualViewId) {
                if (virtualViewId == 0) {
                    return ShutdownView.this.mAccessContent;
                }
                if (virtualViewId == 1) {
                    return ShutdownView.this.mCancelText;
                }
                if (virtualViewId == 2) {
                    return ShutdownView.this.mEmergencyText;
                }
                if (!ShutdownView.this.mManuallyLockEnable || virtualViewId != 3) {
                    return getClass().getSimpleName();
                }
                return ShutdownView.this.mManuallyText;
            }

            @Override // com.android.internal.widget.ColorViewExplorerByTouchHelper.ColorViewTalkBalkInteraction
            public CharSequence getClassName() {
                return Button.class.getName();
            }

            @Override // com.android.internal.widget.ColorViewExplorerByTouchHelper.ColorViewTalkBalkInteraction
            public int getDisablePosition() {
                return -2;
            }
        };
        this.mFpm = (FingerprintManager) context.getSystemService(Context.FINGERPRINT_SERVICE);
        this.mIsSupportOnScreenFingerprint = context.getPackageManager().hasSystemFeature("oppo.hardware.fingerprint.optical.support");
        this.mIsPortrait = getResources().getConfiguration().orientation != 1 ? false : true;
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ShutdownView, defStyle, 0);
        this.mEmergencyCallNormalDrawable = typedArray.getDrawable(2);
        this.mEmergencyCallPressedDrawable = typedArray.getDrawable(3);
        this.mManuallyLockedNormalDrawable = typedArray.getDrawable(9);
        this.mManuallyLockedDrawable = this.mManuallyLockedNormalDrawable;
        this.mManuallyLockedDrawable.setAlpha(0);
        this.mManuallyText = typedArray.getString(8);
        this.mEmergencyCallDrawable = this.mEmergencyCallNormalDrawable;
        this.mEmergencyCallDrawable.setAlpha(0);
        this.mCancelText = typedArray.getString(4);
        this.mEmergencyText = typedArray.getString(5);
        this.mPortraitShutdownHint = typedArray.getString(6);
        this.mPortraitRebootHint = typedArray.getString(7);
        this.mLandscapeRebootHint = getResources().getString(201590224);
        this.mLandscapeShutdownHint = getResources().getString(201590225);
        this.mForceRebootHint = getResources().getString(201590223);
        typedArray.recycle();
        init();
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (this.mFpm != null && this.mIsSupportOnScreenFingerprint && Build.VERSION.SDK_INT >= 29) {
            try {
                Method showFingerprintIcon = this.mFpm.getClass().getDeclaredMethod("showFingerprintIcon", new Class[0]);
                showFingerprintIcon.setAccessible(true);
                showFingerprintIcon.invoke(this.mFpm, new Object[0]);
            } catch (Exception e) {
                Log.w(getClass().getSimpleName(), "Failed reflect to invoke showFingerprintIcon for mFpm!");
            }
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (this.mFpm != null && this.mIsSupportOnScreenFingerprint && Build.VERSION.SDK_INT >= 29) {
            try {
                Method hideFingerprintIcon = this.mFpm.getClass().getDeclaredMethod("hideFingerprintIcon", new Class[0]);
                hideFingerprintIcon.setAccessible(true);
                hideFingerprintIcon.invoke(this.mFpm, new Object[0]);
            } catch (Exception e) {
                Log.w(getClass().getSimpleName(), "Failed reflect to invoke hideFingerprintIcon for mFpm!");
            }
        }
    }

    private void init() {
        this.mCanvasStranslateXForHandler = (float) (this.mIsPortrait ? getWidth() : -getHeight());
        this.mIsMy = getResources().getConfiguration().getLocales().get(0).getLanguage().startsWith(CONFIGURATION_MY);
        initAccessibility();
        this.mBackgroundDrawable = new ColorDrawable(getResources().getColor(201720951));
        this.mForegroundDrawable = new ColorDrawable(getResources().getColor(201720951));
        this.mResources = getResources();
        initResources();
        initPaint();
        initCancelAndAccessibilityHandlerRect();
    }

    private void initAccessibility() {
        this.mTouchHelper = new ColorViewExplorerByTouchHelper(this);
        this.mTouchHelper.setColorViewTalkBalkInteraction(this.mColorViewTalkBalkInteraction);
        setAccessibilityDelegate(this.mTouchHelper);
        setImportantForAccessibility(1);
        this.mAccessContent = getResources().getString(201590175);
    }

    private void initResources() {
        this.mTextPadding = this.mResources.getDimensionPixelSize(201655576);
        this.mBarWidth = this.mResources.getDimensionPixelSize(201655623);
        this.mBarHeight = this.mResources.getDimensionPixelSize(201655624);
        this.mRebootWidth = this.mResources.getDimensionPixelSize(201655632);
        this.mRebootOffset = this.mResources.getDimensionPixelSize(201655635);
        this.mHandlerWidth = this.mResources.getDimensionPixelSize(201655637);
        this.mCancelWidth = this.mResources.getDimensionPixelSize(201655650);
        this.mCancelMarginRight = this.mResources.getDimensionPixelSize(201655653);
        this.mEmergencyMarginDrawable = this.mResources.getDimensionPixelSize(201655652);
        this.mShutdownLineWidth = this.mResources.getDimensionPixelSize(201655636);
        this.mManuallyLockDrawableMargin = this.mResources.getDimensionPixelSize(201655824);
        this.mManuallLockWidth = this.mResources.getDimensionPixelSize(201655825);
        this.mManuallyLockPressColor = this.mResources.getColor(201721004);
        this.mRebootLineWidth = this.mResources.getDimensionPixelSize(201655633);
        this.mForceRebootPortraitMarginBottom = this.mResources.getDimensionPixelSize(201655771);
        this.mForceRebootLandscapMarginBottom = this.mResources.getDimensionPixelSize(201655776);
        this.mForceRebootHintMaxHeight = this.mResources.getDimensionPixelSize(201655772);
        this.mBarOffset = this.mResources.getDimensionPixelSize(201655773);
        this.mCancelOffset = this.mResources.getDimensionPixelSize(201655774);
        this.mEmergencyTextMaxWidth = this.mResources.getDimensionPixelSize(201655777);
        this.mShutdownColor = this.mResources.getColor(201720946);
        this.mRebootColor = this.mResources.getColor(201720945);
        this.mHandlerColor = this.mResources.getColor(201720947);
        this.mDefaultShutdownColor = this.mResources.getColor(201720946);
        this.mDefaultRebootColor = this.mResources.getColor(201720945);
        this.mDefaultHandlerColor = this.mResources.getColor(201720947);
        this.mTextMarginToBitmap = this.mResources.getDimensionPixelSize(201655828);
        this.mShutdownBitmap = BitmapFactory.decodeResource(getResources(), 201852328);
        this.mRebootBitmap = BitmapFactory.decodeResource(getResources(), 201852329);
        this.mRebootTextOnBar = getResources().getString(201590226);
        this.mShutdownTextOnBar = getResources().getString(201590227);
    }

    private Context createDefaultDensityContext(Context context) {
        int defaultDensity = getDefaultDisplayDensity(0);
        if (defaultDensity == -1) {
            return context;
        }
        Configuration config = context.getResources().getConfiguration();
        config.densityDpi = defaultDensity;
        return context.createConfigurationContext(config);
    }

    private int getDefaultDisplayDensity(int displayId) {
        try {
            return WindowManagerGlobal.getWindowManagerService().getInitialDisplayDensity(displayId);
        } catch (Exception e) {
            return -1;
        }
    }

    private void initPaint() {
        this.mBarAlpha = DEFAULT_BAR_ALPHA;
        this.mBarPaint.setColor(this.mResources.getColor(201720944));
        this.mBarPaint.setAntiAlias(true);
        this.mBarPaint.setAlpha((int) (this.mBarAlpha * 255.0f));
        this.mBarPaint.setStyle(Paint.Style.FILL);
        this.mBarMaskPaint.setAntiAlias(true);
        this.mBarMaskPaint.setStyle(Paint.Style.FILL);
        this.mEmergencyBarPaint.setAntiAlias(true);
        this.mEmergencyBarPaint.setStyle(Paint.Style.FILL);
        this.mRebootPaint.setAntiAlias(true);
        this.mRebootPaint.setStyle(Paint.Style.STROKE);
        this.mRebootPaint.setStrokeWidth((float) this.mRebootLineWidth);
        this.mRebootPaint.setStrokeJoin(Paint.Join.ROUND);
        this.mRebootPaint.setStrokeCap(Paint.Cap.ROUND);
        this.mTrianglePaint.setAntiAlias(true);
        this.mTrianglePaint.setStyle(Paint.Style.FILL_AND_STROKE);
        this.mTrianglePaint.setStrokeWidth(6.0f);
        this.mTrianglePaint.setStrokeJoin(Paint.Join.ROUND);
        this.mTrianglePaint.setStrokeCap(Paint.Cap.ROUND);
        this.mShutdownPaint.setAntiAlias(true);
        this.mShutdownPaint.setStyle(Paint.Style.STROKE);
        this.mShutdownPaint.setStrokeJoin(Paint.Join.ROUND);
        this.mShutdownPaint.setStrokeCap(Paint.Cap.ROUND);
        this.mShutdownPaint.setStrokeWidth((float) this.mShutdownLineWidth);
        this.mHandlerPaint.setAntiAlias(true);
        this.mHandlerPaint.setStyle(Paint.Style.FILL);
        this.mRoadPaint.setColor(this.mResources.getColor(201720948));
        this.mRoadPaint.setStyle(Paint.Style.FILL);
        this.mRoadPaint.setStrokeCap(Paint.Cap.ROUND);
        this.mRoadPaint.setStrokeJoin(Paint.Join.ROUND);
        this.mRoadPaint.setStrokeWidth((float) this.mResources.getDimensionPixelSize(201655638));
        this.mRoadPaint.setAntiAlias(true);
        int hintTextSize = this.mResources.getDimensionPixelSize(201655648);
        this.mHintPaint.setColor(this.mResources.getColor(201720949));
        this.mHintPaint.setAntiAlias(true);
        this.mHintPaint.setTextSize((float) hintTextSize);
        int textOnBarTextSize = this.mResources.getDimensionPixelSize(201655812);
        this.mTextOnBarPaint.setColor(this.mResources.getColor(201720949));
        this.mTextOnBarPaint.setAntiAlias(true);
        this.mTextOnBarPaint.setTextSize((float) textOnBarTextSize);
        int textSize = this.mResources.getDimensionPixelSize(201655654);
        this.mEmergencyTextPaint.setAntiAlias(true);
        this.mEmergencyTextPaint.setAlpha(0);
        this.mEmergencyTextPaint.setColor(this.mResources.getColor(201720950));
        this.mEmergencyTextPaint.setTextSize((float) textSize);
        this.mForceRebootPaint.setTextSize((float) this.mResources.getDimensionPixelSize(201655770));
        this.mForceRebootPaint.setAntiAlias(true);
        this.mForceRebootPaint.setColor(this.mResources.getColor(201720993));
        int manuallyLockTextSize = getResources().getDimensionPixelSize(201655826);
        this.mManuallyPaint.setAntiAlias(true);
        this.mManuallyPaint.setColor(getResources().getColor(201721003));
        this.mManuallyPaint.setAlpha(0);
        this.mManuallyPaint.setTextSize((float) manuallyLockTextSize);
        this.mManuallyLockButtonPaint.setAntiAlias(true);
        this.mManuallyLockButtonPaint.setStyle(Paint.Style.FILL);
    }

    private void initCancelAndAccessibilityHandlerRect() {
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;
        Rect rect = this.mCancelRect;
        rect.left = 0;
        rect.right = width;
        rect.top = 0;
        rect.bottom = height;
        RectF rectF = this.mAccessibilityHandlerRect;
        rectF.left = 0.0f;
        rectF.right = (float) width;
        rectF.top = 0.0f;
        rectF.bottom = (float) height;
    }

    @Override // android.view.View, android.view.ViewRootImpl.ConfigChangedCallback
    public void onConfigurationChanged(Configuration globalConfig) {
        boolean newOrientation = true;
        if (getResources().getConfiguration().orientation != 1) {
            newOrientation = false;
        }
        if (newOrientation != this.mIsPortrait) {
            this.mIsPortrait = newOrientation;
            OrientationChangeListener orientationChangeListener = this.mOrientationChangeListener;
            if (orientationChangeListener != null) {
                orientationChangeListener.onOrientationChanged();
            }
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onDraw(Canvas canvas) {
        this.mBackgroundDrawable.setAlpha((int) (this.mBackgroundAlpha * 255.0f));
        this.mBarOffset = (int) ((((float) (getHeight() > getWidth() ? getHeight() : getWidth())) * 0.25f) - ((float) (this.mBarHeight / 2)));
        setBackground(this.mBackgroundDrawable);
        canvas.save();
        float f = this.mScale;
        canvas.scale(f, f, (float) (getWidth() / 2), (float) (getHeight() / 2));
        canvas.save();
        if (this.mIsPortrait) {
            canvas.translate(getCanvasTranslateValue(), 0.0f);
        } else {
            canvas.translate(0.0f, getCanvasTranslateValue());
        }
        int layerId = canvas.saveLayer(0.0f, 0.0f, (float) canvas.getWidth(), (float) canvas.getHeight(), null, 31);
        drawBar(canvas);
        drawMaskOnBar(canvas);
        if (isIndiaRegion()) {
            drawEmergencyBar(canvas);
            drawEmergencyButton(canvas);
        }
        canvas.restoreToCount(layerId);
        drawReboot(canvas);
        drawRebootText(canvas);
        drawShutDown(canvas);
        drawShutdownText(canvas);
        drawHandler(canvas);
        drawRoad(canvas);
        drawManuallyLockButtonBar(canvas);
        drawManuallyLockButton(canvas);
        canvas.restore();
        drawForceRebootHint(canvas);
        canvas.restore();
        this.mForegroundDrawable.setAlpha((int) (this.mForegroundAlpha * 255.0f));
        this.mForegroundDrawable.setBounds(0, 0, getWidth(), getHeight());
        this.mForegroundDrawable.draw(canvas);
    }

    private float getCanvasTranslateValue() {
        if (this.mIsPortrait) {
            float startOffset = (float) getWidth();
            this.mCanvasStranslateXForHandler = startOffset - (this.mCanvasStranslateXForHandlerFloat * (startOffset - (((((float) getWidth()) / 2.0f) - (((float) this.mBarWidth) / 2.0f)) - 75.0f)));
        } else {
            float startOffset2 = (float) (-getHeight());
            this.mCanvasStranslateXForHandler = (this.mCanvasStranslateXForHandlerFloat * ((((((float) (-getHeight())) / 2.0f) + (((float) this.mBarWidth) / 2.0f)) + 75.0f) - startOffset2)) + startOffset2;
        }
        return this.mCanvasStranslateXForHandler;
    }

    private void drawBar(Canvas canvas) {
        int barWidth = this.mIsPortrait ? this.mBarWidth : this.mBarHeight;
        int barHeight = this.mIsPortrait ? this.mBarHeight : this.mBarWidth;
        int i = 0;
        this.mBarRectF.left = (float) (((getWidth() / 2) - (barWidth / 2)) - (this.mIsPortrait ? 0 : this.mBarOffset));
        RectF rectF = this.mBarRectF;
        rectF.right = rectF.left + ((float) barWidth);
        RectF rectF2 = this.mBarRectF;
        int height = (getHeight() / 2) - (barHeight / 2);
        if (this.mIsPortrait) {
            i = this.mBarOffset;
        }
        rectF2.top = (float) (height - i);
        RectF rectF3 = this.mBarRectF;
        rectF3.bottom = rectF3.top + ((float) barHeight);
        this.mBarPaint.setAlpha((int) (this.mBarAlpha * 255.0f));
        RectF rectF4 = this.mBarRectF;
        int i2 = this.mBarRadius;
        canvas.drawRoundRect(rectF4, (float) i2, (float) i2, this.mBarPaint);
    }

    private void drawMaskOnBar(Canvas canvas) {
        float f;
        int boundaryInner;
        int boundaryInner2;
        int boundaryOutter;
        this.mBarMaskPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        int barColor = this.mBarPaint.getColor();
        if (this.mIsPortrait) {
            int boundaryOutter2 = (int) (((float) getHeight()) * 0.11f);
            int boundaryInner3 = (int) (((float) getHeight()) * 0.15f);
            if (this.mHandlerTransition < 0.0f) {
                boundaryInner2 = boundaryInner3;
                boundaryOutter = boundaryOutter2;
                f = 0.0f;
                this.mBarMaskPaint.setShader(new LinearGradient((float) (getWidth() / 2), (float) ((-boundaryOutter2) - ((int) (this.mHandlerTransition * 2.0f))), (float) (getWidth() / 2), (float) (boundaryInner3 - ((int) (this.mHandlerTransition * 3.4f))), -13971071, barColor, Shader.TileMode.CLAMP));
                RectF maskRectF = new RectF();
                maskRectF.left = 0.0f;
                maskRectF.right = (float) getWidth();
                float f2 = this.mHandlerTransition;
                maskRectF.top = (float) ((-boundaryOutter) - ((int) (f2 * 2.0f)));
                maskRectF.bottom = (float) (boundaryInner2 - ((int) (f2 * 3.4f)));
                canvas.drawRect(maskRectF, this.mBarMaskPaint);
            } else {
                boundaryInner2 = boundaryInner3;
                boundaryOutter = boundaryOutter2;
                f = 0.0f;
            }
            if (this.mHandlerTransition > f) {
                this.mBarMaskPaint.setShader(new LinearGradient((float) (getWidth() / 2), (float) (((getHeight() + boundaryOutter) - ((int) (this.mHandlerTransition * 2.0f))) - (this.mBarOffset * 2)), (float) (getWidth() / 2), (float) (((getHeight() - boundaryInner2) - ((int) (this.mHandlerTransition * 3.4f))) - (this.mBarOffset * 2)), -1428409, barColor, Shader.TileMode.CLAMP));
                RectF maskRectF2 = new RectF();
                maskRectF2.left = f;
                maskRectF2.right = (float) getWidth();
                maskRectF2.top = (float) (((getHeight() - boundaryInner2) - ((int) (this.mHandlerTransition * 3.4f))) - (this.mBarOffset * 2));
                maskRectF2.bottom = (float) (((getHeight() + boundaryOutter) - ((int) (this.mHandlerTransition * 2.0f))) - (this.mBarOffset * 2));
                canvas.drawRect(maskRectF2, this.mBarMaskPaint);
            }
        } else {
            f = 0.0f;
        }
        if (!this.mIsPortrait) {
            int boundaryOutter3 = (int) (((float) getWidth()) * 0.11f);
            int boundaryInner4 = (int) (((float) getWidth()) * 0.15f);
            float f3 = this.mHandlerTransition;
            if (f3 < f) {
                boundaryInner = boundaryInner4;
                this.mBarMaskPaint.setShader(new LinearGradient((float) ((-boundaryOutter3) - ((int) (f3 * 2.0f))), (float) (getHeight() / 2), (float) (boundaryInner4 - ((int) (this.mHandlerTransition * 3.0f))), (float) (getHeight() / 2), -13971071, barColor, Shader.TileMode.CLAMP));
                RectF maskRectF3 = new RectF();
                float f4 = this.mHandlerTransition;
                maskRectF3.left = (float) ((-boundaryOutter3) - ((int) (f4 * 2.0f)));
                maskRectF3.right = (float) (boundaryInner - ((int) (f4 * 3.0f)));
                maskRectF3.top = f;
                maskRectF3.bottom = (float) getHeight();
                canvas.drawRect(maskRectF3, this.mBarMaskPaint);
            } else {
                boundaryInner = boundaryInner4;
            }
            if (this.mHandlerTransition > f) {
                this.mBarMaskPaint.setShader(new LinearGradient((float) (((getWidth() + boundaryOutter3) - ((int) (this.mHandlerTransition * 2.0f))) - (this.mBarOffset * 2)), (float) (getHeight() / 2), (float) (((getWidth() - boundaryInner) - ((int) (this.mHandlerTransition * 3.4f))) - (this.mBarOffset * 2)), (float) (getHeight() / 2), -1428409, barColor, Shader.TileMode.CLAMP));
                RectF maskRectF22 = new RectF();
                maskRectF22.left = (float) (((getWidth() - boundaryInner) - ((int) (this.mHandlerTransition * 3.4f))) - (this.mBarOffset * 2));
                maskRectF22.right = (float) (((getWidth() + boundaryOutter3) - ((int) (this.mHandlerTransition * 2.0f))) - (this.mBarOffset * 2));
                maskRectF22.top = f;
                maskRectF22.bottom = (float) getHeight();
                canvas.drawRect(maskRectF22, this.mBarMaskPaint);
            }
        }
    }

    private void drawReboot(Canvas canvas) {
        this.mTrianglePath.reset();
        this.mRebootPaint.setColor(this.mRebootColor);
        this.mRebootPaint.setAlpha((int) (this.mRebootAlpha * 255.0f));
        this.mTrianglePaint.setColor(this.mRebootColor);
        this.mTrianglePaint.setAlpha((int) (this.mRebootAlpha * 255.0f));
        canvas.save();
        float f = this.mIconScale;
        canvas.scale(f, f, (float) (getWidth() / 2), (float) (getHeight() / 2));
        if (this.mIsPortrait) {
            int i = this.mRebootWidth;
            int left = (getWidth() / 2) - (i / 2);
            int top = (((getHeight() / 2) - (this.mBarHeight / 2)) + this.mRebootOffset) - this.mBarOffset;
            int bottom = this.mRebootWidth + top;
            int pivotY = ((top + bottom) / 2) + this.mRebootTransition;
            float f2 = this.mRebootScale;
            canvas.scale(f2, f2, (float) (getWidth() / 2), (float) pivotY);
            new Rect(left, top, this.mRebootBitmap.getWidth(), this.mRebootBitmap.getHeight());
            canvas.drawBitmap(this.mRebootBitmap, (Rect) null, new Rect(left, top, i + left, bottom), this.mRebootPaint);
        } else {
            int left2 = ((int) this.mBarRectF.left) + 93;
            int right = this.mRebootWidth + left2;
            int top2 = ((int) this.mBarRectF.top) + 45;
            int pivotX = ((left2 + right) / 2) + this.mRebootTransition;
            float f3 = this.mRebootScale;
            canvas.scale(f3, f3, (float) pivotX, (float) (getHeight() / 2));
            new Rect(left2, top2, this.mRebootBitmap.getWidth(), this.mRebootBitmap.getHeight());
            canvas.drawBitmap(this.mRebootBitmap, (Rect) null, new Rect(left2, top2, right, this.mRebootWidth + top2), this.mRebootPaint);
        }
        canvas.restore();
    }

    private void drawRebootText(Canvas canvas) {
        this.mTextOnBarPaint.setAlpha(((int) (this.mEmergencyAlpha * 255.0f)) / 2);
        StaticLayout layout = new StaticLayout(this.mRebootTextOnBar, this.mTextOnBarPaint, this.mBarWidth, Layout.Alignment.ALIGN_CENTER, 1.0f, 0.0f, true);
        if (this.mIsPortrait) {
            int left = (int) this.mBarRectF.left;
            int i = this.mRebootWidth + left;
            int top = (((getHeight() / 2) - (this.mBarHeight / 2)) + this.mRebootOffset) - this.mBarOffset;
            canvas.save();
            canvas.translate((float) left, (float) (this.mTextMarginToBitmap + this.mRebootWidth + top));
            layout.draw(canvas);
            canvas.restore();
        }
        if (!this.mIsPortrait) {
            int left2 = ((((int) this.mBarRectF.left) + 93) + (this.mRebootWidth / 2)) - (layout.getWidth() / 2);
            int top2 = ((int) this.mBarRectF.top) + 45 + this.mRebootWidth + this.mTextMarginToBitmap;
            canvas.save();
            canvas.translate((float) left2, (float) top2);
            layout.draw(canvas);
            canvas.restore();
        }
    }

    private void drawPortraitTriangle(Canvas canvas, int left, int top, int pivotX, int pivotY) {
        canvas.save();
        canvas.rotate((this.mRebootRatio * -90.0f) + 45.0f, (float) pivotX, (float) pivotY);
        this.mTrianglePath.reset();
        this.mTrianglePath.moveTo((float) ((this.mRebootWidth / 2) + left), ((float) top) + (((float) this.mRebootLineWidth) * 1.75f));
        this.mTrianglePath.lineTo(((float) ((this.mRebootWidth / 2) + left)) - (((float) this.mRebootLineWidth) * 2.3f), (float) top);
        this.mTrianglePath.lineTo((float) ((this.mRebootWidth / 2) + left), ((float) top) - (((float) this.mRebootLineWidth) * 1.75f));
        this.mTrianglePath.close();
        canvas.drawPath(this.mTrianglePath, this.mTrianglePaint);
        canvas.restore();
    }

    private void drawLandscapTriangle(Canvas canvas, int left, int top, int pivotX, int pivotY) {
        canvas.save();
        canvas.rotate((this.mRebootRatio * -90.0f) + 135.0f, (float) pivotX, (float) pivotY);
        this.mTrianglePath.reset();
        this.mTrianglePath.moveTo(((float) left) - (((float) this.mRebootLineWidth) * 1.75f), (float) ((this.mRebootWidth / 2) + top));
        this.mTrianglePath.lineTo((float) left, ((float) ((this.mRebootWidth / 2) + top)) + (((float) this.mRebootLineWidth) * 2.3f));
        this.mTrianglePath.lineTo(((float) left) + (((float) this.mRebootLineWidth) * 1.75f), (float) ((this.mRebootWidth / 2) + top));
        this.mTrianglePath.close();
        canvas.drawPath(this.mTrianglePath, this.mTrianglePaint);
        canvas.restore();
    }

    private void drawShutDown(Canvas canvas) {
        this.mShutdownPaint.setColor(this.mShutdownColor);
        this.mShutdownPaint.setAlpha((int) (this.mShutdownAlpha * 255.0f));
        canvas.save();
        float f = this.mIconScale;
        canvas.scale(f, f, this.mHandlerRectF.centerX(), this.mHandlerRectF.centerY());
        if (this.mIsPortrait) {
            int i = this.mRebootWidth;
            int left = (getWidth() / 2) - (i / 2);
            int right = i + left;
            int bottom = ((((getHeight() / 2) + (this.mBarHeight / 2)) - this.mRebootOffset) + this.mShutdownTransition) - this.mBarOffset;
            int top = bottom - this.mRebootWidth;
            float f2 = this.mShutdownScale;
            canvas.scale(f2, f2, (float) ((left + right) / 2), (float) ((bottom + top) / 2));
            new Rect(left, top, this.mShutdownBitmap.getWidth(), this.mShutdownBitmap.getHeight());
            canvas.drawBitmap(this.mShutdownBitmap, (Rect) null, new Rect(left, top, right, bottom), this.mShutdownPaint);
        } else {
            int right2 = (((int) this.mBarRectF.right) - 69) + this.mShutdownTransition;
            int left2 = right2 - this.mRebootWidth;
            int top2 = ((int) this.mBarRectF.top) + 45;
            int bottom2 = this.mRebootWidth + top2;
            float f3 = this.mShutdownScale;
            canvas.scale(f3, f3, (float) ((left2 + right2) / 2), (float) ((bottom2 + top2) / 2));
            new Rect(left2, top2, this.mShutdownBitmap.getWidth(), this.mShutdownBitmap.getHeight());
            canvas.drawBitmap(this.mShutdownBitmap, (Rect) null, new Rect(left2, top2, right2, bottom2), this.mShutdownPaint);
        }
        canvas.restore();
    }

    private void drawShutdownText(Canvas canvas) {
        this.mTextOnBarPaint.setAlpha(((int) (this.mEmergencyAlpha * 255.0f)) / 2);
        StaticLayout layout = new StaticLayout(this.mShutdownTextOnBar, this.mTextOnBarPaint, this.mBarWidth, Layout.Alignment.ALIGN_CENTER, 1.0f, 0.0f, true);
        if (this.mIsPortrait) {
            int left = (int) this.mBarRectF.left;
            int i = this.mRebootWidth + left;
            int height = layout.getLineBottom(layout.getLineCount() - 1) - layout.getLineTop(0);
            canvas.save();
            canvas.translate((float) left, (float) ((((((((getHeight() / 2) + (this.mBarHeight / 2)) - this.mRebootOffset) + this.mShutdownTransition) - this.mBarOffset) - this.mRebootWidth) - height) - this.mTextMarginToBitmap));
            layout.draw(canvas);
            canvas.restore();
        }
        if (!this.mIsPortrait) {
            int top = ((int) this.mBarRectF.top) + 45 + this.mRebootWidth + this.mTextMarginToBitmap;
            canvas.save();
            canvas.translate((float) ((((((int) this.mBarRectF.right) - 69) + this.mShutdownTransition) - (this.mRebootWidth / 2)) - (layout.getWidth() / 2)), (float) top);
            layout.draw(canvas);
            canvas.restore();
        }
    }

    private void drawShutdownLine(Canvas canvas, int left, int right, int bottom, int top) {
        int startX = (left + right) / 2;
        int i = this.mRebootWidth;
        int startY = ((bottom + top) / 2) - (i / 2);
        canvas.drawLine((float) startX, (float) startY, (float) startX, (float) (((int) (((float) (i / 2)) * this.mShutdownRatio)) + startY), this.mShutdownPaint);
    }

    private void drawHandler(Canvas canvas) {
        float centerY;
        float centerX;
        this.mHandlerPaint.setColor(this.mHandlerColor);
        this.mHandlerPaint.setAlpha((int) (this.mHandlerAlpha * 255.0f));
        if (this.mIsPortrait) {
            centerX = (float) (getWidth() / 2);
            centerY = (((float) (getHeight() / 2)) + this.mHandlerTransition) - ((float) this.mBarOffset);
        } else {
            centerX = (((float) (getWidth() / 2)) + this.mHandlerTransition) - ((float) this.mBarOffset);
            centerY = (float) (getHeight() / 2);
        }
        RectF rectF = this.mHandlerRectF;
        int i = this.mHandlerWidth;
        rectF.top = centerY - ((float) (i / 2));
        rectF.bottom = ((float) (i / 2)) + centerY;
        rectF.left = centerX - ((float) (i / 2));
        rectF.right = ((float) (i / 2)) + centerX;
        canvas.save();
        float f = this.mHandlerScale;
        canvas.scale(f, f, this.mHandlerRectF.centerX(), this.mHandlerRectF.centerY());
        canvas.drawArc(this.mHandlerRectF, 0.0f, 360.0f, true, this.mHandlerPaint);
        canvas.restore();
    }

    private void drawRoad(Canvas canvas) {
        int roadWidth = this.mResources.getDimensionPixelSize(201655639);
        int offset = this.mResources.getDimensionPixelSize(201655640);
        int phase = this.mResources.getDimensionPixelSize(201655641);
        canvas.save();
        float f = this.mIconScale;
        canvas.scale(f, f, (float) (getWidth() / 2), (float) (getHeight() / 2));
        for (int i = 0; i < 3; i++) {
            this.mRoadPaint.setAlpha((int) (255.0f * getRoadAlpha(i)));
            if (this.mIsPortrait) {
                int startX = (getWidth() / 2) - (roadWidth / 2);
                int startY = (((getHeight() / 2) - offset) - (i * phase)) - this.mBarOffset;
                int middleX = startX + (roadWidth / 2);
                int middleY = startY - (roadWidth / 2);
                int stopX = middleX + (roadWidth / 2);
                Rect rect = this.mRoadRect;
                rect.left = startX;
                rect.right = stopX;
                rect.top = middleY;
                rect.bottom = (getHeight() / 2) + offset + (i * phase);
                drawRoadLine(canvas, startX, startY, middleX, middleY, stopX, middleY + (roadWidth / 2));
            } else {
                int startX2 = (((getWidth() / 2) - offset) - (i * phase)) - this.mBarOffset;
                int startY2 = (getHeight() / 2) + (roadWidth / 2);
                int middleX2 = startX2 - (roadWidth / 2);
                int middleY2 = startY2 - (roadWidth / 2);
                int stopY = middleY2 - (roadWidth / 2);
                Rect rect2 = this.mRoadRect;
                rect2.left = middleX2;
                rect2.right = (getWidth() / 2) + offset + (i * phase) + (roadWidth / 2);
                Rect rect3 = this.mRoadRect;
                rect3.top = stopY;
                rect3.bottom = startY2;
                drawRoadLine(canvas, startX2, startY2, middleX2, middleY2, middleX2 + (roadWidth / 2), stopY);
            }
        }
        canvas.restore();
    }

    private float getRoadAlpha(int i) {
        if (i == 0) {
            return this.mRoadAlpha1;
        }
        if (i == 1) {
            return this.mRoadAlpha2;
        }
        if (i != 2) {
            return this.mRoadAlpha1;
        }
        return this.mRoadAlpha3;
    }

    private void drawRoadLine(Canvas canvas, int startX, int startY, int middleX, int middleY, int stopX, int stopY) {
        canvas.save();
        this.mRoadPath.reset();
        this.mRoadPath.moveTo((float) startX, (float) startY);
        this.mRoadPath.lineTo((float) middleX, (float) middleY);
        this.mRoadPath.lineTo((float) stopX, (float) stopY);
        canvas.drawCircle((float) middleX, (float) middleY, 5.0f, this.mRoadPaint);
        canvas.rotate(180.0f, this.mHandlerRectF.centerX(), this.mHandlerRectF.centerY());
        canvas.drawCircle((float) middleX, (float) middleY, 5.0f, this.mRoadPaint);
        canvas.restore();
    }

    private void drawHint(Canvas canvas) {
        this.mHintPaint.setAlpha((int) (this.mHintAlpha * 255.0f));
        StaticLayout layout = new StaticLayout(this.mHint, this.mHintPaint, this.mIsPortrait ? getWidth() - (this.mTextPadding * 2) : this.mBarHeight, Layout.Alignment.ALIGN_CENTER, 1.0f, 0.0f, true);
        canvas.save();
        canvas.translate((float) ((getWidth() / 2) - (layout.getWidth() / 2)), (this.mBarRectF.top / 2.0f) - ((float) (layout.getHeight() / 2)));
        layout.draw(canvas);
        canvas.restore();
    }

    private void drawEmergencyBar(Canvas canvas) {
        this.mEmergencyBarPaint.setAlpha((int) (this.mBarAlpha * 255.0f));
        int barHeight = this.mIsPortrait ? this.mBarHeight : this.mBarWidth;
        int emergencyBarHeight = this.mIsPortrait ? this.mIsPortrait ? this.mBarWidth : this.mBarHeight : barHeight;
        if (this.mIsPortrait) {
            this.mEmergencyBarRectF.left = (float) ((getWidth() / 2) - (emergencyBarHeight / 2));
            RectF rectF = this.mEmergencyBarRectF;
            rectF.right = rectF.left + ((float) emergencyBarHeight);
            this.mEmergencyBarRectF.top = this.mBarRectF.bottom + ((float) 48);
            RectF rectF2 = this.mEmergencyBarRectF;
            rectF2.bottom = rectF2.top + ((float) emergencyBarHeight);
        }
        if (!this.mIsPortrait) {
            this.mEmergencyBarRectF.left = this.mBarRectF.right + ((float) 48);
            RectF rectF3 = this.mEmergencyBarRectF;
            rectF3.right = rectF3.left + ((float) emergencyBarHeight);
            this.mEmergencyBarRectF.top = (float) ((getHeight() / 2) - (barHeight / 2));
            RectF rectF4 = this.mEmergencyBarRectF;
            rectF4.bottom = rectF4.top + ((float) emergencyBarHeight);
        }
        if (this.mEmergencyPressed) {
            this.mEmergencyBarPaint.setColor(-14560895);
        } else {
            this.mEmergencyBarPaint.setColor(this.mBarPaint.getColor());
        }
        canvas.drawRoundRect(this.mEmergencyBarRectF, (float) 46, (float) 46, this.mEmergencyBarPaint);
    }

    private void drawEmergencyButton(Canvas canvas) {
        int alpha = (int) (this.mEmergencyAlpha * 255.0f);
        this.mEmergencyCallDrawable.setAlpha(alpha);
        this.mEmergencyTextPaint.setAlpha(alpha / 2);
        int measureText = (int) this.mEmergencyTextPaint.measureText(this.mEmergencyText);
        Paint.FontMetricsInt fontMetricsInt = this.mEmergencyTextPaint.getFontMetricsInt();
        int i = fontMetricsInt.bottom - fontMetricsInt.top;
        if (this.mIsPortrait) {
            StaticLayout layout = new StaticLayout(this.mEmergencyText, this.mEmergencyTextPaint, this.mBarWidth, Layout.Alignment.ALIGN_CENTER, 1.0f, 0.0f, true);
            int startX = this.mEmergencyRect.centerX() - (layout.getWidth() / 2);
            int startY = this.mEmergencyRect.bottom + this.mEmergencyMarginDrawable;
            canvas.save();
            canvas.translate((float) startX, (float) startY);
            layout.draw(canvas);
            canvas.restore();
            this.mEmergencyRect.left = (getWidth() / 2) - (this.mCancelWidth / 2);
            Rect rect = this.mEmergencyRect;
            rect.right = rect.left + this.mCancelWidth;
            this.mEmergencyRect.top = (int) (this.mEmergencyBarRectF.top + 45.0f);
            Rect rect2 = this.mEmergencyRect;
            rect2.bottom = rect2.top + this.mCancelWidth;
        } else {
            int width = (int) (((float) getWidth()) - this.mBarRectF.right);
            this.mEmergencyRect.left = (int) (this.mEmergencyBarRectF.centerX() - ((float) (this.mCancelWidth / 2)));
            Rect rect3 = this.mEmergencyRect;
            rect3.right = rect3.left + this.mCancelWidth;
            this.mEmergencyRect.top = (int) (this.mEmergencyBarRectF.top + 45.0f);
            Rect rect4 = this.mEmergencyRect;
            rect4.bottom = rect4.top + this.mCancelWidth;
            StaticLayout layout2 = new StaticLayout(this.mEmergencyText, this.mEmergencyTextPaint, this.mBarWidth, Layout.Alignment.ALIGN_CENTER, 1.0f, 0.0f, true);
            int startX2 = this.mEmergencyRect.centerX() - (layout2.getWidth() / 2);
            int startY2 = this.mEmergencyRect.bottom + this.mEmergencyMarginDrawable;
            canvas.save();
            canvas.translate((float) startX2, (float) startY2);
            layout2.draw(canvas);
            canvas.restore();
        }
        this.mEmergencyCallDrawable.setBounds(this.mEmergencyRect);
        this.mEmergencyCallDrawable.draw(canvas);
    }

    private boolean isIndiaRegion() {
        return "IN".equalsIgnoreCase(SystemProperties.get("persist.sys.oppo.region", "CN"));
    }

    private int cancelMarginBottom(int textHeight) {
        if (this.mIsPortrait) {
            return ((((((getHeight() / 2) - (this.mBarHeight / 2)) - this.mCancelWidth) - this.mEmergencyMarginDrawable) - textHeight) / 3) + this.mCancelOffset;
        }
        return (((((getWidth() / 2) - (this.mBarHeight / 2)) - this.mCancelWidth) - this.mEmergencyMarginDrawable) - textHeight) / 3;
    }

    private void drawForceRebootHint(Canvas canvas) {
        this.mForceRebootPaint.setAlpha((int) (this.mForceRebootHintAlpha * 255.0f));
        StaticLayout staticLayout = new StaticLayout(this.mForceRebootHint, this.mForceRebootPaint, getWidth(), Layout.Alignment.ALIGN_CENTER, this.mIsMy ? 1.2f : 1.0f, 0.0f, true);
        canvas.save();
        if (this.mIsPortrait) {
            canvas.translate((float) ((getWidth() / 2) - (staticLayout.getWidth() / 2)), (float) ((getHeight() - this.mForceRebootPortraitMarginBottom) - ((this.mForceRebootHintMaxHeight + staticLayout.getHeight()) / 2)));
        } else {
            canvas.translate((float) ((getWidth() / 2) - (staticLayout.getWidth() / 2)), (float) ((getHeight() - this.mForceRebootLandscapMarginBottom) - staticLayout.getHeight()));
        }
        staticLayout.draw(canvas);
        canvas.restore();
    }

    /* access modifiers changed from: package-private */
    public void setTouchEventListener(TouchEventListener touchEventListener) {
        this.mTouchEventListener = touchEventListener;
    }

    /* access modifiers changed from: package-private */
    public void setOrientationChangeListener(OrientationChangeListener orientationChangeListener) {
        this.mOrientationChangeListener = orientationChangeListener;
    }

    @Override // android.view.View
    public boolean onTouchEvent(MotionEvent event) {
        TouchEventListener touchEventListener;
        float x = event.getX();
        float y = event.getY();
        int actionMasked = event.getActionMasked();
        if (actionMasked == 0) {
            TouchEventListener touchEventListener2 = this.mTouchEventListener;
            if (touchEventListener2 != null) {
                return touchEventListener2.onDown(x, y);
            }
        } else if (actionMasked == 1) {
            TouchEventListener touchEventListener3 = this.mTouchEventListener;
            if (touchEventListener3 != null) {
                touchEventListener3.onUp(x, y);
            }
        } else if (actionMasked == 2) {
            TouchEventListener touchEventListener4 = this.mTouchEventListener;
            if (touchEventListener4 != null) {
                touchEventListener4.onMove(x, y);
            }
        } else if (actionMasked == 3) {
            TouchEventListener touchEventListener5 = this.mTouchEventListener;
            if (touchEventListener5 != null) {
                touchEventListener5.onCancel(x, y);
            }
        } else if (actionMasked == 5 && (touchEventListener = this.mTouchEventListener) != null) {
            touchEventListener.onPointerDown();
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public void setHandlerScale(float handlerScale) {
        this.mHandlerScale = handlerScale;
        invalidate();
    }

    /* access modifiers changed from: package-private */
    public void setHandlerTransition(float handlerTransition) {
        this.mHandlerTransition = handlerTransition;
        float threshold = (float) getShutdownTransitionThreshold();
        float absTransition = Math.abs(handlerTransition);
        this.mHintAlpha = absTransition / threshold;
        if (this.mHintAlpha > 1.0f) {
            this.mHintAlpha = 1.0f;
        }
        float transitionFraction = absTransition / threshold;
        if (transitionFraction > 1.0f) {
            transitionFraction = 1.0f;
        }
        if (handlerTransition != 0.0f) {
            this.mBackgroundAlpha = this.mFloatEvaluator.evaluate(transitionFraction, (Number) Float.valueOf((float) DEFAULT_BACKGROUND_ALPHA), (Number) 1).floatValue();
            if (handlerTransition < 0.0f) {
                this.mRebootScale = this.mFloatEvaluator.evaluate(transitionFraction, (Number) 1, (Number) 1).floatValue();
                this.mRebootRatio = this.mFloatEvaluator.evaluate(transitionFraction, (Number) Float.valueOf(0.67f), (Number) 1).floatValue();
            } else {
                this.mShutdownScale = this.mFloatEvaluator.evaluate(transitionFraction, (Number) 1, (Number) 1).floatValue();
                this.mShutdownRatio = this.mFloatEvaluator.evaluate(transitionFraction, (Number) Float.valueOf(0.75f), (Number) 1).floatValue();
            }
            IntEvaluator intEvaluator = new IntEvaluator();
            Paint paint = this.mBarPaint;
            paint.setColor(((intEvaluator.evaluate(transitionFraction, (Integer) 23, (Integer) 0).intValue() << 16) - 16777216) + (intEvaluator.evaluate(transitionFraction, (Integer) 23, (Integer) 0).intValue() << 8) + intEvaluator.evaluate(transitionFraction, (Integer) 23, (Integer) 0).intValue());
            this.mEmergencyAlpha = this.mFloatEvaluator.evaluate(transitionFraction, (Number) 1, (Number) 0).floatValue();
            this.mRebootAlpha = this.mFloatEvaluator.evaluate(transitionFraction, (Number) 1, (Number) 0).floatValue();
            this.mShutdownAlpha = this.mFloatEvaluator.evaluate(transitionFraction, (Number) 1, (Number) 0).floatValue();
            this.mBarRadius = intEvaluator.evaluate(transitionFraction, (Integer) 46, Integer.valueOf(this.mBarWidth / 2)).intValue();
            this.mForceRebootHintAlpha = this.mFloatEvaluator.evaluate(transitionFraction, (Number) 0, (Number) Float.valueOf(0.5f)).floatValue();
            this.mManuallyLockAlpha = this.mFloatEvaluator.evaluate(transitionFraction, (Number) 1, (Number) 0).floatValue();
        }
        float handlerOffset = absTransition - (threshold / 2.0f);
        if (handlerOffset > 0.0f) {
            float f = (handlerOffset / threshold) * 2.0f;
            invalidate();
            return;
        }
        this.mHandlerColor = this.mDefaultHandlerColor;
        this.mRebootColor = this.mDefaultRebootColor;
        this.mShutdownColor = this.mDefaultShutdownColor;
        invalidate();
    }

    /* access modifiers changed from: package-private */
    public float getHandlerTransition() {
        return this.mHandlerTransition;
    }

    /* access modifiers changed from: package-private */
    public void setBarAlpha(float barAlpha) {
        this.mBarAlpha = barAlpha;
        invalidate();
    }

    /* access modifiers changed from: package-private */
    public void setRebootTransition(int rebootTransition) {
        this.mRebootTransition = rebootTransition;
        invalidate();
    }

    /* access modifiers changed from: package-private */
    public void setShutdownTransition(int shutdownTransition) {
        this.mShutdownTransition = shutdownTransition;
        invalidate();
    }

    /* access modifiers changed from: package-private */
    public void setRoadTransition(int roadTransition) {
        this.mRoadTransition = roadTransition;
        invalidate();
    }

    /* access modifiers changed from: package-private */
    public void setBackgroundAlpha(float backgroundAlpha) {
        this.mBackgroundAlpha = backgroundAlpha;
        invalidate();
    }

    /* access modifiers changed from: package-private */
    public void setForegroundAlpha(float foregroundAlpha) {
        this.mForegroundAlpha = foregroundAlpha;
        invalidate();
    }

    /* access modifiers changed from: package-private */
    public void setIconScale(float iconScale) {
        this.mIconScale = iconScale;
        invalidate();
    }

    /* access modifiers changed from: package-private */
    public void setEmergencyAlpha(float emergencyAlpha) {
        this.mEmergencyAlpha = emergencyAlpha;
        invalidate();
    }

    /* access modifiers changed from: package-private */
    public void setHandlerAlpha(float handlerAlpha) {
        this.mHandlerAlpha = handlerAlpha;
        invalidate();
    }

    /* access modifiers changed from: package-private */
    public void setEmergencyPressed(boolean emergencyPressed) {
        this.mEmergencyCallDrawable = emergencyPressed ? this.mEmergencyCallPressedDrawable : this.mEmergencyCallNormalDrawable;
        this.mEmergencyPressed = emergencyPressed;
        invalidate();
    }

    /* access modifiers changed from: package-private */
    public void setHint(int hintType) {
        if (hintType == 0) {
            this.mHint = this.mIsPortrait ? this.mPortraitRebootHint : this.mLandscapeRebootHint;
        } else if (hintType == 1) {
            this.mHint = this.mIsPortrait ? this.mPortraitShutdownHint : this.mLandscapeShutdownHint;
        }
    }

    /* access modifiers changed from: package-private */
    public void setIconAlpha(float iconAlpha) {
        this.mShutdownAlpha = iconAlpha;
        this.mRebootAlpha = iconAlpha;
        invalidate();
    }

    /* access modifiers changed from: package-private */
    public void setScale(float scale) {
        this.mScale = scale;
        invalidate();
    }

    /* access modifiers changed from: package-private */
    public void setRebootScale(float rebootScale) {
        this.mRebootScale = rebootScale;
        invalidate();
    }

    /* access modifiers changed from: package-private */
    public float getRebootScale() {
        return this.mRebootScale;
    }

    /* access modifiers changed from: package-private */
    public void setRebootRatio(float rebootRatio) {
        this.mRebootRatio = rebootRatio;
        invalidate();
    }

    /* access modifiers changed from: package-private */
    public float getRebootRatio() {
        return this.mRebootRatio;
    }

    /* access modifiers changed from: package-private */
    public void setShutdownScale(float shutdownScale) {
        this.mShutdownScale = shutdownScale;
        invalidate();
    }

    /* access modifiers changed from: package-private */
    public float getShutdownScale() {
        return this.mShutdownScale;
    }

    /* access modifiers changed from: package-private */
    public void setShutdownRatio(float shutdownRatio) {
        this.mShutdownRatio = shutdownRatio;
        invalidate();
    }

    /* access modifiers changed from: package-private */
    public float getShutdownRatio() {
        return this.mShutdownRatio;
    }

    /* access modifiers changed from: package-private */
    public void setRoadAlpha1(float roadAlpha) {
        this.mRoadAlpha1 = roadAlpha;
        invalidate(this.mRoadRect);
    }

    /* access modifiers changed from: package-private */
    public float getRoadAlpha1() {
        return this.mRoadAlpha1;
    }

    /* access modifiers changed from: package-private */
    public void setRoadAlpha2(float roadAlpha) {
        this.mRoadAlpha2 = roadAlpha;
        invalidate(this.mRoadRect);
    }

    /* access modifiers changed from: package-private */
    public float getRoadAlpha2() {
        return this.mRoadAlpha2;
    }

    /* access modifiers changed from: package-private */
    public void setRoadAlpha3(float roadAlpha) {
        this.mRoadAlpha3 = roadAlpha;
        invalidate(this.mRoadRect);
    }

    /* access modifiers changed from: package-private */
    public float getRoadAlpha3() {
        return this.mRoadAlpha3;
    }

    /* access modifiers changed from: package-private */
    public void setCanvasStranslateXForHandler(float canvasStranslateXForHandler) {
        this.mCanvasStranslateXForHandlerFloat = canvasStranslateXForHandler;
        invalidate();
    }

    /* access modifiers changed from: package-private */
    public float getCanvasStranslateXForHandler() {
        return this.mCanvasStranslateXForHandler;
    }

    /* access modifiers changed from: package-private */
    public void setBarWidth(int width) {
        this.mBarWidth = width;
        invalidate();
    }

    /* access modifiers changed from: package-private */
    public int getBarWidth() {
        return this.mBarWidth;
    }

    /* access modifiers changed from: package-private */
    public void setForceRebootHintAlpha(float forceRebootHintAlpha) {
        this.mForceRebootHintAlpha = forceRebootHintAlpha;
        invalidate();
    }

    /* access modifiers changed from: package-private */
    public float getForceRebootHintAlpha() {
        return this.mForceRebootHintAlpha;
    }

    /* access modifiers changed from: package-private */
    public int getShutdownTransitionThreshold() {
        return ((this.mBarHeight / 2) - (this.mRebootWidth / 2)) - this.mRebootOffset;
    }

    /* access modifiers changed from: package-private */
    public int getRebootTransitionThreshold() {
        return getShutdownTransitionThreshold() * -1;
    }

    /* access modifiers changed from: package-private */
    public boolean isPortrait() {
        return this.mIsPortrait;
    }

    /* access modifiers changed from: package-private */
    public void sendHandlerAccessibilityFocused() {
        this.mTouchHelper.sendEventForVirtualView(0, 1);
        this.mTouchHelper.setFocusedVirtualView(0);
    }

    /* access modifiers changed from: package-private */
    public boolean isAccessibilityEnabled() {
        return ColorAccessibilityUtil.isTalkbackEnabled(getContext()) && AccessibilityManager.getInstance(getContext()).isTouchExplorationEnabled();
    }

    @Override // android.view.View
    public void clearAccessibilityFocus() {
        ColorViewExplorerByTouchHelper colorViewExplorerByTouchHelper = this.mTouchHelper;
        if (colorViewExplorerByTouchHelper != null) {
            colorViewExplorerByTouchHelper.clearFocusedVirtualView();
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public boolean dispatchHoverEvent(MotionEvent event) {
        ColorViewExplorerByTouchHelper colorViewExplorerByTouchHelper = this.mTouchHelper;
        if (colorViewExplorerByTouchHelper == null || !colorViewExplorerByTouchHelper.dispatchHoverEvent(event)) {
            return super.dispatchHoverEvent(event);
        }
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isTouchHandler(float x, float y) {
        if (isPortrait()) {
            return this.mHandlerRectF.contains(x - this.mCanvasStranslateXForHandler, y);
        }
        return this.mHandlerRectF.contains(x, y - this.mCanvasStranslateXForHandler);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isTouchEmergency(float x, float y) {
        if (isPortrait()) {
            return this.mEmergencyBarRectF.contains((float) ((int) (x - this.mCanvasStranslateXForHandler)), (float) ((int) y));
        }
        return this.mEmergencyBarRectF.contains((float) ((int) x), (float) ((int) (y - this.mCanvasStranslateXForHandler)));
    }

    /* access modifiers changed from: package-private */
    public void setOperationListener(OperationListener operationListener) {
        this.mOperationListener = operationListener;
    }

    private void drawManuallyLockButtonBar(Canvas canvas) {
        if (this.mManuallyLockEnable) {
            this.mManuallyLockButtonPaint.setAlpha((int) (this.mBarAlpha * 255.0f));
            int barHeight = this.mIsPortrait ? this.mBarHeight : this.mBarWidth;
            int manuallyLockBarHeight = this.mIsPortrait ? this.mIsPortrait ? this.mBarWidth : this.mBarHeight : barHeight;
            if (this.mIsPortrait) {
                this.mManuallyLockBarRectF.left = (float) ((getWidth() / 2) - (manuallyLockBarHeight / 2));
                RectF rectF = this.mManuallyLockBarRectF;
                rectF.right = rectF.left + ((float) manuallyLockBarHeight);
                this.mManuallyLockBarRectF.bottom = this.mBarRectF.top - ((float) 48);
                RectF rectF2 = this.mManuallyLockBarRectF;
                rectF2.top = rectF2.bottom - ((float) manuallyLockBarHeight);
            }
            if (!this.mIsPortrait) {
                this.mManuallyLockBarRectF.right = this.mBarRectF.left - ((float) 48);
                RectF rectF3 = this.mManuallyLockBarRectF;
                rectF3.left = rectF3.right - ((float) manuallyLockBarHeight);
                this.mManuallyLockBarRectF.top = (float) ((getHeight() / 2) - (barHeight / 2));
                RectF rectF4 = this.mManuallyLockBarRectF;
                rectF4.bottom = rectF4.top + ((float) manuallyLockBarHeight);
            }
            if (this.mManuallyLockPressed) {
                this.mManuallyLockButtonPaint.setColor(this.mManuallyLockPressColor);
            } else {
                this.mManuallyLockButtonPaint.setColor(this.mBarPaint.getColor());
            }
            canvas.drawRoundRect(this.mManuallyLockBarRectF, (float) 46, (float) 46, this.mManuallyLockButtonPaint);
        }
    }

    private void drawManuallyLockButton(Canvas canvas) {
        if (this.mManuallyLockEnable) {
            int alpha = (int) (this.mManuallyLockAlpha * 255.0f);
            this.mManuallyLockedDrawable.setAlpha(alpha);
            this.mManuallyPaint.setAlpha(alpha / 2);
            int measureText = (int) this.mManuallyPaint.measureText(this.mManuallyText);
            Paint.FontMetricsInt fontMetricsInt = this.mManuallyPaint.getFontMetricsInt();
            int i = fontMetricsInt.bottom - fontMetricsInt.top;
            if (this.mIsPortrait) {
                this.mLockRect.left = (getWidth() / 2) - (this.mCancelWidth / 2);
                Rect rect = this.mLockRect;
                rect.right = rect.left + this.mCancelWidth;
                this.mLockRect.top = (int) (this.mManuallyLockBarRectF.top + 45.0f);
                Rect rect2 = this.mLockRect;
                rect2.bottom = rect2.top + this.mCancelWidth;
                StaticLayout layout = new StaticLayout(this.mManuallyText, this.mManuallyPaint, this.mBarWidth, Layout.Alignment.ALIGN_CENTER, 1.0f, 0.0f, true);
                int startX = this.mLockRect.centerX() - (layout.getWidth() / 2);
                int startY = this.mLockRect.bottom + this.mManuallyLockDrawableMargin;
                canvas.save();
                canvas.translate((float) startX, (float) startY);
                layout.draw(canvas);
                canvas.restore();
            } else {
                int width = (int) (((float) getWidth()) - this.mBarRectF.right);
                this.mLockRect.left = (int) (this.mManuallyLockBarRectF.centerX() - ((float) (this.mCancelWidth / 2)));
                Rect rect3 = this.mLockRect;
                rect3.right = rect3.left + this.mCancelWidth;
                this.mLockRect.top = (int) (this.mManuallyLockBarRectF.top + 45.0f);
                Rect rect4 = this.mLockRect;
                rect4.bottom = rect4.top + this.mCancelWidth;
                StaticLayout layout2 = new StaticLayout(this.mManuallyText, this.mManuallyPaint, this.mBarWidth, Layout.Alignment.ALIGN_CENTER, 1.0f, 0.0f, true);
                int startX2 = this.mLockRect.centerX() - (layout2.getWidth() / 2);
                int startY2 = this.mLockRect.bottom + this.mManuallyLockDrawableMargin;
                canvas.save();
                canvas.translate((float) startX2, (float) startY2);
                layout2.draw(canvas);
                canvas.restore();
            }
            this.mManuallyLockedDrawable.setBounds(this.mLockRect);
            this.mManuallyLockedDrawable.draw(canvas);
        }
    }

    /* access modifiers changed from: package-private */
    public void setManuallyLockPressed(boolean manuallyPressed) {
        this.mManuallyLockPressed = manuallyPressed;
        invalidate();
    }

    /* access modifiers changed from: package-private */
    public void setManuallyAlpha(float manuallyAlpha) {
        int alpha = (int) (255.0f * manuallyAlpha);
        this.mManuallyLockedDrawable.setAlpha(alpha);
        this.mManuallyPaint.setAlpha(alpha / 2);
        this.mManuallyLockAlpha = manuallyAlpha;
    }

    /* access modifiers changed from: package-private */
    public void setManuallyLockEnable(boolean enable) {
        this.mManuallyLockEnable = enable;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isTouchManuallyLock(float x, float y) {
        if (isPortrait()) {
            return this.mManuallyLockBarRectF.contains((float) ((int) (x - this.mCanvasStranslateXForHandler)), (float) ((int) y));
        }
        return this.mManuallyLockBarRectF.contains((float) ((int) x), (float) ((int) (y - this.mCanvasStranslateXForHandler)));
    }
}
