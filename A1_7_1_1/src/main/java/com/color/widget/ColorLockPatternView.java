package com.color.widget;

import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.Shader;
import android.media.AudioManager;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.BaseSavedState;
import android.view.View.MeasureSpec;
import android.view.animation.Interpolator;
import com.android.internal.widget.ExploreByTouchHelper;
import com.color.screenshot.ColorLongshotUnsupported;
import com.oppo.widget.OppoFingerWrongIn;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
    	at jadx.core.utils.BlockUtils.isAllBlocksEmpty(BlockUtils.java:546)
    	at jadx.core.dex.visitors.ExtractFieldInit.getConstructorsList(ExtractFieldInit.java:221)
    	at jadx.core.dex.visitors.ExtractFieldInit.moveCommonFieldsInit(ExtractFieldInit.java:121)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:46)
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
public class ColorLockPatternView extends View implements ColorAnimation, ColorLongshotUnsupported {
    private static final int ASPECT_LOCK_HEIGHT = 2;
    private static final int ASPECT_LOCK_WIDTH = 1;
    private static final int ASPECT_SQUARE = 0;
    private static final boolean DEBUG = false;
    private static final float DRAG_THRESHHOLD = 0.0f;
    private static final float ENDALPHA = 0.0f;
    private static final int LONGTIME = 200;
    private static final int MILLIS_PER_CIRCLE_ANIMATING = 700;
    private static final int MORELONGTIME = 300;
    private static final boolean PROFILE_DRAWING = false;
    private static final int SHORTTIME = 100;
    private static final float STARTALPHA = 0.3f;
    static final int STATUS_BAR_HEIGHT = 25;
    private static final String TAG = "ColorLockPatternView";
    public static final int VIRTUAL_BASE_VIEW_ID = 1;
    private final Interpolator TRUELARGE;
    private long mAnimatingPeriodStart;
    private int mAspect;
    private AudioManager mAudioManager;
    private float mCenterX;
    private float mCenterY;
    private float mCirLargeWidth;
    private Paint mCirclePaint;
    private final Path mCurrentPath;
    private int mDefaultCircleColor;
    private int mDefaultColorBlue;
    private int mDefaultColorGreen;
    private int mDefaultColorRed;
    private float mDefaultDiameterFactor;
    private float mDefaultFingerRadius;
    private float mDefaultRadius;
    private float mDiameterFactor;
    private boolean mDrawingProfilingStarted;
    private boolean mEnableHapticFeedback;
    private int mEndAlpha;
    private PatternExploreByTouchHelper mExploreByTouchHelper;
    private Paint mFingerPaint;
    private float mFingerRadius;
    private float mHitFactor;
    private float mInProgressX;
    private float mInProgressY;
    private boolean mInStealthMode;
    private boolean mInputEnabled;
    private final Rect mInvalidate;
    private boolean mIsClearPattern;
    private boolean mIsDrag;
    private boolean mIsDrawLadder;
    private boolean mIsSetPassword;
    private boolean mIsShrinkAnim;
    private int mLadderEndColor;
    private int mLadderStartColor;
    private float mLargeFingerRadius;
    private float mLargeRingRadius;
    private float mLargeRingWidth;
    private float mLineEndWidth;
    private float mLineStartWidth;
    private final int mMinMumber;
    private boolean[][] mNotDraw;
    private OnPatternListener mOnPatternListener;
    private Paint mPaint;
    private int[][] mPaintAlpha;
    private Paint mPathPaint;
    private int mPathTrueColor;
    private int mPathWrongColor;
    private ArrayList<Cell> mPattern;
    private DisplayMode mPatternDisplayMode;
    private boolean[][] mPatternDrawLookup;
    private boolean mPatternInProgress;
    private Paint mRingPaint;
    private float mSetLockPathWidth;
    private double mShrinkAngle;
    private Paint mShrinkPaint;
    private Shader mShrinkShader;
    private float mSmallRingRadius;
    private float mSmallRingWidth;
    private float mSquareHeight;
    private float mSquareWidth;
    private int mStartAlpha;
    float[] mStretch1;
    float[] mStretch2;
    float[] mStretch3;
    float[] mStretch4;
    private double mStretchAngle;
    private Paint mStretchPaint;
    private Shader mStretchShader;
    private final int mStrokeAlpha;
    private final Rect mTmpInvalidateRect;
    private float[][] mTranslateY;
    private int mTrueCircleColor;
    private int mTrueColorBlue;
    private int mTrueColorGreen;
    private int mTrueColorRed;
    private int mTrueLargeTime;
    private int mTruePauseTime;
    private int mViewHeight;
    private int mViewWidth;
    private int mWrongCircleColor;
    private int mWrongColorBlue;
    private int mWrongColorGreen;
    private int mWrongColorRed;
    private OppoFingerWrongIn mWrongInterpolator;
    private int mWrongPathAlpha;
    private int mWrongTotalTime;
    private int mZoomOutPositon;

    /* renamed from: com.color.widget.ColorLockPatternView$1 */
    class AnonymousClass1 implements AnimatorUpdateListener {
        final /* synthetic */ ColorLockPatternView this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.color.widget.ColorLockPatternView.1.<init>(com.color.widget.ColorLockPatternView):void, dex: 
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
        AnonymousClass1(com.color.widget.ColorLockPatternView r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.color.widget.ColorLockPatternView.1.<init>(com.color.widget.ColorLockPatternView):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.1.<init>(com.color.widget.ColorLockPatternView):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.color.widget.ColorLockPatternView.1.onAnimationUpdate(android.animation.ValueAnimator):void, dex: 
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
        public void onAnimationUpdate(android.animation.ValueAnimator r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.color.widget.ColorLockPatternView.1.onAnimationUpdate(android.animation.ValueAnimator):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.1.onAnimationUpdate(android.animation.ValueAnimator):void");
        }
    }

    /* renamed from: com.color.widget.ColorLockPatternView$2 */
    class AnonymousClass2 implements AnimatorUpdateListener {
        final /* synthetic */ ColorLockPatternView this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.color.widget.ColorLockPatternView.2.<init>(com.color.widget.ColorLockPatternView):void, dex: 
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
        AnonymousClass2(com.color.widget.ColorLockPatternView r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.color.widget.ColorLockPatternView.2.<init>(com.color.widget.ColorLockPatternView):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.2.<init>(com.color.widget.ColorLockPatternView):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.color.widget.ColorLockPatternView.2.onAnimationUpdate(android.animation.ValueAnimator):void, dex: 
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
        public void onAnimationUpdate(android.animation.ValueAnimator r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.color.widget.ColorLockPatternView.2.onAnimationUpdate(android.animation.ValueAnimator):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.2.onAnimationUpdate(android.animation.ValueAnimator):void");
        }
    }

    /* renamed from: com.color.widget.ColorLockPatternView$3 */
    class AnonymousClass3 implements AnimatorUpdateListener {
        final /* synthetic */ ColorLockPatternView this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.color.widget.ColorLockPatternView.3.<init>(com.color.widget.ColorLockPatternView):void, dex: 
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
        AnonymousClass3(com.color.widget.ColorLockPatternView r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.color.widget.ColorLockPatternView.3.<init>(com.color.widget.ColorLockPatternView):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.3.<init>(com.color.widget.ColorLockPatternView):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.color.widget.ColorLockPatternView.3.onAnimationUpdate(android.animation.ValueAnimator):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        public void onAnimationUpdate(android.animation.ValueAnimator r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.color.widget.ColorLockPatternView.3.onAnimationUpdate(android.animation.ValueAnimator):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.3.onAnimationUpdate(android.animation.ValueAnimator):void");
        }
    }

    /* renamed from: com.color.widget.ColorLockPatternView$4 */
    class AnonymousClass4 implements AnimatorUpdateListener {
        final /* synthetic */ ColorLockPatternView this$0;
        final /* synthetic */ int val$m;
        final /* synthetic */ int val$n;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.color.widget.ColorLockPatternView.4.<init>(com.color.widget.ColorLockPatternView, int, int):void, dex: 
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
        AnonymousClass4(com.color.widget.ColorLockPatternView r1, int r2, int r3) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.color.widget.ColorLockPatternView.4.<init>(com.color.widget.ColorLockPatternView, int, int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.4.<init>(com.color.widget.ColorLockPatternView, int, int):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.color.widget.ColorLockPatternView.4.onAnimationUpdate(android.animation.ValueAnimator):void, dex: 
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
        public void onAnimationUpdate(android.animation.ValueAnimator r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.color.widget.ColorLockPatternView.4.onAnimationUpdate(android.animation.ValueAnimator):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.4.onAnimationUpdate(android.animation.ValueAnimator):void");
        }
    }

    /* renamed from: com.color.widget.ColorLockPatternView$5 */
    class AnonymousClass5 extends AnimatorListenerAdapter {
        final /* synthetic */ ColorLockPatternView this$0;
        final /* synthetic */ int val$m;
        final /* synthetic */ int val$n;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.color.widget.ColorLockPatternView.5.<init>(com.color.widget.ColorLockPatternView, int, int):void, dex: 
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
        AnonymousClass5(com.color.widget.ColorLockPatternView r1, int r2, int r3) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.color.widget.ColorLockPatternView.5.<init>(com.color.widget.ColorLockPatternView, int, int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.5.<init>(com.color.widget.ColorLockPatternView, int, int):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.color.widget.ColorLockPatternView.5.onAnimationStart(android.animation.Animator):void, dex: 
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
        public void onAnimationStart(android.animation.Animator r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.color.widget.ColorLockPatternView.5.onAnimationStart(android.animation.Animator):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.5.onAnimationStart(android.animation.Animator):void");
        }
    }

    /* renamed from: com.color.widget.ColorLockPatternView$6 */
    class AnonymousClass6 implements AnimatorUpdateListener {
        final /* synthetic */ ColorLockPatternView this$0;
        final /* synthetic */ int val$m;
        final /* synthetic */ int val$n;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.color.widget.ColorLockPatternView.6.<init>(com.color.widget.ColorLockPatternView, int, int):void, dex: 
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
        AnonymousClass6(com.color.widget.ColorLockPatternView r1, int r2, int r3) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.color.widget.ColorLockPatternView.6.<init>(com.color.widget.ColorLockPatternView, int, int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.6.<init>(com.color.widget.ColorLockPatternView, int, int):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.color.widget.ColorLockPatternView.6.onAnimationUpdate(android.animation.ValueAnimator):void, dex: 
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
        public void onAnimationUpdate(android.animation.ValueAnimator r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.color.widget.ColorLockPatternView.6.onAnimationUpdate(android.animation.ValueAnimator):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.6.onAnimationUpdate(android.animation.ValueAnimator):void");
        }
    }

    /* renamed from: com.color.widget.ColorLockPatternView$7 */
    class AnonymousClass7 implements AnimatorUpdateListener {
        final /* synthetic */ ColorLockPatternView this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.color.widget.ColorLockPatternView.7.<init>(com.color.widget.ColorLockPatternView):void, dex: 
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
        AnonymousClass7(com.color.widget.ColorLockPatternView r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.color.widget.ColorLockPatternView.7.<init>(com.color.widget.ColorLockPatternView):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.7.<init>(com.color.widget.ColorLockPatternView):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.color.widget.ColorLockPatternView.7.onAnimationUpdate(android.animation.ValueAnimator):void, dex: 
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
        public void onAnimationUpdate(android.animation.ValueAnimator r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.color.widget.ColorLockPatternView.7.onAnimationUpdate(android.animation.ValueAnimator):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.7.onAnimationUpdate(android.animation.ValueAnimator):void");
        }
    }

    /* renamed from: com.color.widget.ColorLockPatternView$8 */
    class AnonymousClass8 extends AnimatorListenerAdapter {
        final /* synthetic */ ColorLockPatternView this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.color.widget.ColorLockPatternView.8.<init>(com.color.widget.ColorLockPatternView):void, dex: 
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
        AnonymousClass8(com.color.widget.ColorLockPatternView r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.color.widget.ColorLockPatternView.8.<init>(com.color.widget.ColorLockPatternView):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.8.<init>(com.color.widget.ColorLockPatternView):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus registerCount: 0 in method: com.color.widget.ColorLockPatternView.8.onAnimationEnd(android.animation.Animator):void, dex:  in method: com.color.widget.ColorLockPatternView.8.onAnimationEnd(android.animation.Animator):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: jadx.core.utils.exceptions.DecodeException: bogus registerCount: 0 in method: com.color.widget.ColorLockPatternView.8.onAnimationEnd(android.animation.Animator):void, dex: 
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
            	... 6 more
            Caused by: com.android.dex.DexException: bogus registerCount: 0
            	at com.android.dx.io.instructions.InstructionCodec$32.decode(InstructionCodec.java:693)
            	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
            	... 7 more
            */
        public void onAnimationEnd(android.animation.Animator r1) {
            /*
            // Can't load method instructions: Load method exception: bogus registerCount: 0 in method: com.color.widget.ColorLockPatternView.8.onAnimationEnd(android.animation.Animator):void, dex:  in method: com.color.widget.ColorLockPatternView.8.onAnimationEnd(android.animation.Animator):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.8.onAnimationEnd(android.animation.Animator):void");
        }
    }

    private class AnimUpdateListener implements AnimatorUpdateListener {
        private final Cell mCell;
        final /* synthetic */ ColorLockPatternView this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.color.widget.ColorLockPatternView.AnimUpdateListener.<init>(com.color.widget.ColorLockPatternView, com.color.widget.ColorLockPatternView$Cell):void, dex: 
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
        public AnimUpdateListener(com.color.widget.ColorLockPatternView r1, com.color.widget.ColorLockPatternView.Cell r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.color.widget.ColorLockPatternView.AnimUpdateListener.<init>(com.color.widget.ColorLockPatternView, com.color.widget.ColorLockPatternView$Cell):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.AnimUpdateListener.<init>(com.color.widget.ColorLockPatternView, com.color.widget.ColorLockPatternView$Cell):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: com.color.widget.ColorLockPatternView.AnimUpdateListener.onAnimationUpdate(android.animation.ValueAnimator):void, dex:  in method: com.color.widget.ColorLockPatternView.AnimUpdateListener.onAnimationUpdate(android.animation.ValueAnimator):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: jadx.core.utils.exceptions.DecodeException: null in method: com.color.widget.ColorLockPatternView.AnimUpdateListener.onAnimationUpdate(android.animation.ValueAnimator):void, dex: 
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
            	... 6 more
            Caused by: java.io.EOFException
            	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
            	at com.android.dx.io.instructions.InstructionCodec$14.decode(InstructionCodec.java:307)
            	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
            	... 7 more
            */
        public void onAnimationUpdate(android.animation.ValueAnimator r1) {
            /*
            // Can't load method instructions: Load method exception: null in method: com.color.widget.ColorLockPatternView.AnimUpdateListener.onAnimationUpdate(android.animation.ValueAnimator):void, dex:  in method: com.color.widget.ColorLockPatternView.AnimUpdateListener.onAnimationUpdate(android.animation.ValueAnimator):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.AnimUpdateListener.onAnimationUpdate(android.animation.ValueAnimator):void");
        }
    }

    public static class Cell extends com.android.internal.widget.LockPatternView.Cell {
        private static final Cell[][] sCells = null;
        float coordinateX;
        boolean isfillInGapCell;
        float ringRadius;
        float ringWidth;
        float[] shrink1;
        float[] shrink2;
        float[] shrink3;
        float[] shrink4;
        ValueAnimator shrinkAnim;
        ShrinkAnimListener shrinkAnimListener;
        int trueColorBlue;
        int trueColorGreen;
        int trueColorRed;
        int wrongColorBlue;
        int wrongColorGreen;
        int wrongColorRed;
        float wrongRadius;
        ValueAnimator zoomInAnim;
        ZoomInAnimListener zoomInAnimListener;
        boolean zoomInEnd;
        boolean zoomInStart;
        boolean zoomOut;
        ValueAnimator zoomOutAnim;
        ZoomOutAnimListener zoomOutAnimListener;
        ValueAnimator zoomOutWrongAnim;

        /* renamed from: com.color.widget.ColorLockPatternView$Cell$1 */
        class AnonymousClass1 implements AnimatorUpdateListener {
            final /* synthetic */ Cell this$1;
            final /* synthetic */ ColorAnimation val$anim;
            final /* synthetic */ Cell val$cell;

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.color.widget.ColorLockPatternView.Cell.1.<init>(com.color.widget.ColorLockPatternView$Cell, com.color.widget.ColorAnimation, com.color.widget.ColorLockPatternView$Cell):void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 7 more
                */
            AnonymousClass1(com.color.widget.ColorLockPatternView.Cell r1, com.color.widget.ColorAnimation r2, com.color.widget.ColorLockPatternView.Cell r3) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.color.widget.ColorLockPatternView.Cell.1.<init>(com.color.widget.ColorLockPatternView$Cell, com.color.widget.ColorAnimation, com.color.widget.ColorLockPatternView$Cell):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.Cell.1.<init>(com.color.widget.ColorLockPatternView$Cell, com.color.widget.ColorAnimation, com.color.widget.ColorLockPatternView$Cell):void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.color.widget.ColorLockPatternView.Cell.1.onAnimationUpdate(android.animation.ValueAnimator):void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 7 more
                */
            public void onAnimationUpdate(android.animation.ValueAnimator r1) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.color.widget.ColorLockPatternView.Cell.1.onAnimationUpdate(android.animation.ValueAnimator):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.Cell.1.onAnimationUpdate(android.animation.ValueAnimator):void");
            }
        }

        private class ShrinkAnimListener extends AnimatorListenerAdapter {
            final /* synthetic */ Cell this$1;

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.color.widget.ColorLockPatternView.Cell.ShrinkAnimListener.<init>(com.color.widget.ColorLockPatternView$Cell):void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 7 more
                */
            private ShrinkAnimListener(com.color.widget.ColorLockPatternView.Cell r1) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.color.widget.ColorLockPatternView.Cell.ShrinkAnimListener.<init>(com.color.widget.ColorLockPatternView$Cell):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.Cell.ShrinkAnimListener.<init>(com.color.widget.ColorLockPatternView$Cell):void");
            }

            /* synthetic */ ShrinkAnimListener(Cell this$1, ShrinkAnimListener shrinkAnimListener) {
                this(this$1);
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.color.widget.ColorLockPatternView.Cell.ShrinkAnimListener.onAnimationCancel(android.animation.Animator):void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 7 more
                */
            public void onAnimationCancel(android.animation.Animator r1) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.color.widget.ColorLockPatternView.Cell.ShrinkAnimListener.onAnimationCancel(android.animation.Animator):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.Cell.ShrinkAnimListener.onAnimationCancel(android.animation.Animator):void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.color.widget.ColorLockPatternView.Cell.ShrinkAnimListener.onAnimationStart(android.animation.Animator):void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 7 more
                */
            public void onAnimationStart(android.animation.Animator r1) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.color.widget.ColorLockPatternView.Cell.ShrinkAnimListener.onAnimationStart(android.animation.Animator):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.Cell.ShrinkAnimListener.onAnimationStart(android.animation.Animator):void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.color.widget.ColorLockPatternView.Cell.ShrinkAnimListener.updateParms(float, float, android.animation.ValueAnimator$AnimatorUpdateListener):void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 7 more
                */
            public void updateParms(float r1, float r2, android.animation.ValueAnimator.AnimatorUpdateListener r3) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.color.widget.ColorLockPatternView.Cell.ShrinkAnimListener.updateParms(float, float, android.animation.ValueAnimator$AnimatorUpdateListener):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.Cell.ShrinkAnimListener.updateParms(float, float, android.animation.ValueAnimator$AnimatorUpdateListener):void");
            }
        }

        private class ZoomInAnimListener extends AnimatorListenerAdapter {
            ColorAnimation anim;
            Cell cell;
            final /* synthetic */ Cell this$1;
            AnimatorUpdateListener updateListener;

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.color.widget.ColorLockPatternView.Cell.ZoomInAnimListener.<init>(com.color.widget.ColorLockPatternView$Cell, com.color.widget.ColorLockPatternView$Cell, com.color.widget.ColorAnimation):void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 7 more
                */
            public ZoomInAnimListener(com.color.widget.ColorLockPatternView.Cell r1, com.color.widget.ColorLockPatternView.Cell r2, com.color.widget.ColorAnimation r3) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.color.widget.ColorLockPatternView.Cell.ZoomInAnimListener.<init>(com.color.widget.ColorLockPatternView$Cell, com.color.widget.ColorLockPatternView$Cell, com.color.widget.ColorAnimation):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.Cell.ZoomInAnimListener.<init>(com.color.widget.ColorLockPatternView$Cell, com.color.widget.ColorLockPatternView$Cell, com.color.widget.ColorAnimation):void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.color.widget.ColorLockPatternView.Cell.ZoomInAnimListener.onAnimationEnd(android.animation.Animator):void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 7 more
                */
            public void onAnimationEnd(android.animation.Animator r1) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.color.widget.ColorLockPatternView.Cell.ZoomInAnimListener.onAnimationEnd(android.animation.Animator):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.Cell.ZoomInAnimListener.onAnimationEnd(android.animation.Animator):void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.color.widget.ColorLockPatternView.Cell.ZoomInAnimListener.onAnimationStart(android.animation.Animator):void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 7 more
                */
            public void onAnimationStart(android.animation.Animator r1) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.color.widget.ColorLockPatternView.Cell.ZoomInAnimListener.onAnimationStart(android.animation.Animator):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.Cell.ZoomInAnimListener.onAnimationStart(android.animation.Animator):void");
            }
        }

        private class ZoomOutAnimListener extends AnimatorListenerAdapter {
            ColorAnimation anim;
            Cell cell;
            float endValue;
            float startValue;
            final /* synthetic */ Cell this$1;
            AnimatorUpdateListener updateListener;
            Cell zoomInCell;

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.color.widget.ColorLockPatternView.Cell.ZoomOutAnimListener.<init>(com.color.widget.ColorLockPatternView$Cell, com.color.widget.ColorLockPatternView$Cell, com.color.widget.ColorAnimation):void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 7 more
                */
            public ZoomOutAnimListener(com.color.widget.ColorLockPatternView.Cell r1, com.color.widget.ColorLockPatternView.Cell r2, com.color.widget.ColorAnimation r3) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.color.widget.ColorLockPatternView.Cell.ZoomOutAnimListener.<init>(com.color.widget.ColorLockPatternView$Cell, com.color.widget.ColorLockPatternView$Cell, com.color.widget.ColorAnimation):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.Cell.ZoomOutAnimListener.<init>(com.color.widget.ColorLockPatternView$Cell, com.color.widget.ColorLockPatternView$Cell, com.color.widget.ColorAnimation):void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.color.widget.ColorLockPatternView.Cell.ZoomOutAnimListener.onAnimationCancel(android.animation.Animator):void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 7 more
                */
            public void onAnimationCancel(android.animation.Animator r1) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.color.widget.ColorLockPatternView.Cell.ZoomOutAnimListener.onAnimationCancel(android.animation.Animator):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.Cell.ZoomOutAnimListener.onAnimationCancel(android.animation.Animator):void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.color.widget.ColorLockPatternView.Cell.ZoomOutAnimListener.onAnimationEnd(android.animation.Animator):void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 7 more
                */
            public void onAnimationEnd(android.animation.Animator r1) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.color.widget.ColorLockPatternView.Cell.ZoomOutAnimListener.onAnimationEnd(android.animation.Animator):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.Cell.ZoomOutAnimListener.onAnimationEnd(android.animation.Animator):void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.color.widget.ColorLockPatternView.Cell.ZoomOutAnimListener.onAnimationStart(android.animation.Animator):void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 7 more
                */
            public void onAnimationStart(android.animation.Animator r1) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.color.widget.ColorLockPatternView.Cell.ZoomOutAnimListener.onAnimationStart(android.animation.Animator):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.Cell.ZoomOutAnimListener.onAnimationStart(android.animation.Animator):void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e6 in method: com.color.widget.ColorLockPatternView.Cell.ZoomOutAnimListener.updateZoomParams(float, float, android.animation.ValueAnimator$AnimatorUpdateListener, com.color.widget.ColorLockPatternView$Cell):void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.ProcessClass.process(ProcessClass.java:29)
                	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
                	at jadx.api.JavaClass.decompile(JavaClass.java:62)
                	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
                Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e6
                	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
                	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
                	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
                	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
                	... 7 more
                */
            public void updateZoomParams(float r1, float r2, android.animation.ValueAnimator.AnimatorUpdateListener r3, com.color.widget.ColorLockPatternView.Cell r4) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e6 in method: com.color.widget.ColorLockPatternView.Cell.ZoomOutAnimListener.updateZoomParams(float, float, android.animation.ValueAnimator$AnimatorUpdateListener, com.color.widget.ColorLockPatternView$Cell):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.Cell.ZoomOutAnimListener.updateZoomParams(float, float, android.animation.ValueAnimator$AnimatorUpdateListener, com.color.widget.ColorLockPatternView$Cell):void");
            }
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.color.widget.ColorLockPatternView.Cell.-wrap0(com.color.widget.ColorLockPatternView$Cell, com.color.widget.ColorAnimation, com.color.widget.ColorLockPatternView$Cell):void, dex: 
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
        /* renamed from: -wrap0 */
        static /* synthetic */ void m162-wrap0(com.color.widget.ColorLockPatternView.Cell r1, com.color.widget.ColorAnimation r2, com.color.widget.ColorLockPatternView.Cell r3) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.color.widget.ColorLockPatternView.Cell.-wrap0(com.color.widget.ColorLockPatternView$Cell, com.color.widget.ColorAnimation, com.color.widget.ColorLockPatternView$Cell):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.Cell.-wrap0(com.color.widget.ColorLockPatternView$Cell, com.color.widget.ColorAnimation, com.color.widget.ColorLockPatternView$Cell):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.color.widget.ColorLockPatternView.Cell.-wrap1(com.color.widget.ColorLockPatternView$Cell, float, float, android.animation.ValueAnimator$AnimatorUpdateListener, com.color.widget.ColorLockPatternView$Cell, com.color.widget.ColorAnimation):void, dex: 
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
        /* renamed from: -wrap1 */
        static /* synthetic */ void m163-wrap1(com.color.widget.ColorLockPatternView.Cell r1, float r2, float r3, android.animation.ValueAnimator.AnimatorUpdateListener r4, com.color.widget.ColorLockPatternView.Cell r5, com.color.widget.ColorAnimation r6) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.color.widget.ColorLockPatternView.Cell.-wrap1(com.color.widget.ColorLockPatternView$Cell, float, float, android.animation.ValueAnimator$AnimatorUpdateListener, com.color.widget.ColorLockPatternView$Cell, com.color.widget.ColorAnimation):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.Cell.-wrap1(com.color.widget.ColorLockPatternView$Cell, float, float, android.animation.ValueAnimator$AnimatorUpdateListener, com.color.widget.ColorLockPatternView$Cell, com.color.widget.ColorAnimation):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.color.widget.ColorLockPatternView.Cell.<clinit>():void, dex: 
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
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.color.widget.ColorLockPatternView.Cell.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.Cell.<clinit>():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.color.widget.ColorLockPatternView.Cell.<init>(int, int):void, dex: 
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
        public Cell(int r1, int r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.color.widget.ColorLockPatternView.Cell.<init>(int, int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.Cell.<init>(int, int):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.color.widget.ColorLockPatternView.Cell.checkRange(int, int):void, dex: 
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
        private static void checkRange(int r1, int r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.color.widget.ColorLockPatternView.Cell.checkRange(int, int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.Cell.checkRange(int, int):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.color.widget.ColorLockPatternView.Cell.startChangeColor(com.color.widget.ColorAnimation, com.color.widget.ColorLockPatternView$Cell):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        private void startChangeColor(com.color.widget.ColorAnimation r1, com.color.widget.ColorLockPatternView.Cell r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.color.widget.ColorLockPatternView.Cell.startChangeColor(com.color.widget.ColorAnimation, com.color.widget.ColorLockPatternView$Cell):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.Cell.startChangeColor(com.color.widget.ColorAnimation, com.color.widget.ColorLockPatternView$Cell):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.color.widget.ColorLockPatternView.Cell.startShrinkAnim(float, float, android.animation.ValueAnimator$AnimatorUpdateListener):void, dex: 
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
        private void startShrinkAnim(float r1, float r2, android.animation.ValueAnimator.AnimatorUpdateListener r3) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.color.widget.ColorLockPatternView.Cell.startShrinkAnim(float, float, android.animation.ValueAnimator$AnimatorUpdateListener):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.Cell.startShrinkAnim(float, float, android.animation.ValueAnimator$AnimatorUpdateListener):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.color.widget.ColorLockPatternView.Cell.startZoomInInternal(float, float, android.animation.ValueAnimator$AnimatorUpdateListener, com.color.widget.ColorLockPatternView$Cell, com.color.widget.ColorAnimation):void, dex: 
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
        private void startZoomInInternal(float r1, float r2, android.animation.ValueAnimator.AnimatorUpdateListener r3, com.color.widget.ColorLockPatternView.Cell r4, com.color.widget.ColorAnimation r5) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.color.widget.ColorLockPatternView.Cell.startZoomInInternal(float, float, android.animation.ValueAnimator$AnimatorUpdateListener, com.color.widget.ColorLockPatternView$Cell, com.color.widget.ColorAnimation):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.Cell.startZoomInInternal(float, float, android.animation.ValueAnimator$AnimatorUpdateListener, com.color.widget.ColorLockPatternView$Cell, com.color.widget.ColorAnimation):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.color.widget.ColorLockPatternView.Cell.setShrinkAnim(float, float, android.animation.ValueAnimator$AnimatorUpdateListener):void, dex: 
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
        public void setShrinkAnim(float r1, float r2, android.animation.ValueAnimator.AnimatorUpdateListener r3) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.color.widget.ColorLockPatternView.Cell.setShrinkAnim(float, float, android.animation.ValueAnimator$AnimatorUpdateListener):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.Cell.setShrinkAnim(float, float, android.animation.ValueAnimator$AnimatorUpdateListener):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.color.widget.ColorLockPatternView.Cell.startZoomIn(float, float, android.animation.ValueAnimator$AnimatorUpdateListener, com.color.widget.ColorLockPatternView$Cell, com.color.widget.ColorAnimation):void, dex: 
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
        public void startZoomIn(float r1, float r2, android.animation.ValueAnimator.AnimatorUpdateListener r3, com.color.widget.ColorLockPatternView.Cell r4, com.color.widget.ColorAnimation r5) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.color.widget.ColorLockPatternView.Cell.startZoomIn(float, float, android.animation.ValueAnimator$AnimatorUpdateListener, com.color.widget.ColorLockPatternView$Cell, com.color.widget.ColorAnimation):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.Cell.startZoomIn(float, float, android.animation.ValueAnimator$AnimatorUpdateListener, com.color.widget.ColorLockPatternView$Cell, com.color.widget.ColorAnimation):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.color.widget.ColorLockPatternView.Cell.startZoomOut(float, float, android.animation.ValueAnimator$AnimatorUpdateListener, com.color.widget.ColorLockPatternView$Cell, com.color.widget.ColorAnimation):void, dex: 
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
        public void startZoomOut(float r1, float r2, android.animation.ValueAnimator.AnimatorUpdateListener r3, com.color.widget.ColorLockPatternView.Cell r4, com.color.widget.ColorAnimation r5) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.color.widget.ColorLockPatternView.Cell.startZoomOut(float, float, android.animation.ValueAnimator$AnimatorUpdateListener, com.color.widget.ColorLockPatternView$Cell, com.color.widget.ColorAnimation):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.Cell.startZoomOut(float, float, android.animation.ValueAnimator$AnimatorUpdateListener, com.color.widget.ColorLockPatternView$Cell, com.color.widget.ColorAnimation):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.color.widget.ColorLockPatternView.Cell.startZoomOutWrong(float, float, android.animation.ValueAnimator$AnimatorUpdateListener):void, dex: 
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
        public void startZoomOutWrong(float r1, float r2, android.animation.ValueAnimator.AnimatorUpdateListener r3) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.color.widget.ColorLockPatternView.Cell.startZoomOutWrong(float, float, android.animation.ValueAnimator$AnimatorUpdateListener):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.Cell.startZoomOutWrong(float, float, android.animation.ValueAnimator$AnimatorUpdateListener):void");
        }

        private static Cell[][] createCells() {
            Cell[][] res = (Cell[][]) Array.newInstance(Cell.class, new int[]{3, 3});
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    res[i][j] = new Cell(i, j);
                }
            }
            return res;
        }

        public static Cell of(int row, int column) {
            checkRange(row, column);
            return sCells[row][column];
        }
    }

    /*  JADX ERROR: NullPointerException in pass: EnumVisitor
        java.lang.NullPointerException
        	at jadx.core.dex.visitors.EnumVisitor.visit(EnumVisitor.java:102)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
        	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$0(DepthTraversal.java:13)
        	at java.util.ArrayList.forEach(ArrayList.java:1251)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:13)
        	at jadx.core.ProcessClass.process(ProcessClass.java:32)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    public enum DisplayMode {
        ;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.color.widget.ColorLockPatternView.DisplayMode.<clinit>():void, dex: 
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
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.color.widget.ColorLockPatternView.DisplayMode.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.DisplayMode.<clinit>():void");
        }
    }

    public interface OnPatternListener {
        void onPatternCellAdded(List<Cell> list);

        void onPatternCleared();

        void onPatternDetected(List<Cell> list);

        void onPatternStart();
    }

    private final class PatternExploreByTouchHelper extends ExploreByTouchHelper {
        private HashMap<Integer, VirtualViewContainer> mItems;
        private Rect mTempRect;
        final /* synthetic */ ColorLockPatternView this$0;

        class VirtualViewContainer {
            CharSequence description;
            final /* synthetic */ PatternExploreByTouchHelper this$1;

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.color.widget.ColorLockPatternView.PatternExploreByTouchHelper.VirtualViewContainer.<init>(com.color.widget.ColorLockPatternView$PatternExploreByTouchHelper, java.lang.CharSequence):void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 7 more
                */
            public VirtualViewContainer(com.color.widget.ColorLockPatternView.PatternExploreByTouchHelper r1, java.lang.CharSequence r2) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.color.widget.ColorLockPatternView.PatternExploreByTouchHelper.VirtualViewContainer.<init>(com.color.widget.ColorLockPatternView$PatternExploreByTouchHelper, java.lang.CharSequence):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.PatternExploreByTouchHelper.VirtualViewContainer.<init>(com.color.widget.ColorLockPatternView$PatternExploreByTouchHelper, java.lang.CharSequence):void");
            }
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.color.widget.ColorLockPatternView.PatternExploreByTouchHelper.<init>(com.color.widget.ColorLockPatternView, android.view.View):void, dex: 
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
        public PatternExploreByTouchHelper(com.color.widget.ColorLockPatternView r1, android.view.View r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.color.widget.ColorLockPatternView.PatternExploreByTouchHelper.<init>(com.color.widget.ColorLockPatternView, android.view.View):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.PatternExploreByTouchHelper.<init>(com.color.widget.ColorLockPatternView, android.view.View):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: protoIndex doesn't fit in a short: 51685 in method: com.color.widget.ColorLockPatternView.PatternExploreByTouchHelper.getBoundsForVirtualView(int):android.graphics.Rect, dex:  in method: com.color.widget.ColorLockPatternView.PatternExploreByTouchHelper.getBoundsForVirtualView(int):android.graphics.Rect, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: jadx.core.utils.exceptions.DecodeException: protoIndex doesn't fit in a short: 51685 in method: com.color.widget.ColorLockPatternView.PatternExploreByTouchHelper.getBoundsForVirtualView(int):android.graphics.Rect, dex: 
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
            	... 6 more
            Caused by: java.lang.IllegalArgumentException: protoIndex doesn't fit in a short: 51685
            	at com.android.dx.io.instructions.InvokePolymorphicRangeDecodedInstruction.<init>(InvokePolymorphicRangeDecodedInstruction.java:38)
            	at com.android.dx.io.instructions.InstructionCodec$33.decode(InstructionCodec.java:730)
            	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
            	... 7 more
            */
        private android.graphics.Rect getBoundsForVirtualView(int r1) {
            /*
            // Can't load method instructions: Load method exception: protoIndex doesn't fit in a short: 51685 in method: com.color.widget.ColorLockPatternView.PatternExploreByTouchHelper.getBoundsForVirtualView(int):android.graphics.Rect, dex:  in method: com.color.widget.ColorLockPatternView.PatternExploreByTouchHelper.getBoundsForVirtualView(int):android.graphics.Rect, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.PatternExploreByTouchHelper.getBoundsForVirtualView(int):android.graphics.Rect");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.color.widget.ColorLockPatternView.PatternExploreByTouchHelper.getTextForVirtualView(int):java.lang.CharSequence, dex: 
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
        private java.lang.CharSequence getTextForVirtualView(int r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.color.widget.ColorLockPatternView.PatternExploreByTouchHelper.getTextForVirtualView(int):java.lang.CharSequence, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.PatternExploreByTouchHelper.getTextForVirtualView(int):java.lang.CharSequence");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.color.widget.ColorLockPatternView.PatternExploreByTouchHelper.getVirtualViewIdForHit(float, float):int, dex: 
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
        private int getVirtualViewIdForHit(float r1, float r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.color.widget.ColorLockPatternView.PatternExploreByTouchHelper.getVirtualViewIdForHit(float, float):int, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.PatternExploreByTouchHelper.getVirtualViewIdForHit(float, float):int");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.color.widget.ColorLockPatternView.PatternExploreByTouchHelper.isClickable(int):boolean, dex: 
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
        private boolean isClickable(int r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.color.widget.ColorLockPatternView.PatternExploreByTouchHelper.isClickable(int):boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.PatternExploreByTouchHelper.isClickable(int):boolean");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.color.widget.ColorLockPatternView.PatternExploreByTouchHelper.shouldSpeakPassword():boolean, dex: 
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
        private boolean shouldSpeakPassword() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.color.widget.ColorLockPatternView.PatternExploreByTouchHelper.shouldSpeakPassword():boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.PatternExploreByTouchHelper.shouldSpeakPassword():boolean");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.color.widget.ColorLockPatternView.PatternExploreByTouchHelper.getVisibleVirtualViews(android.util.IntArray):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        protected void getVisibleVirtualViews(android.util.IntArray r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.color.widget.ColorLockPatternView.PatternExploreByTouchHelper.getVisibleVirtualViews(android.util.IntArray):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.PatternExploreByTouchHelper.getVisibleVirtualViews(android.util.IntArray):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.color.widget.ColorLockPatternView.PatternExploreByTouchHelper.onItemClicked(int):boolean, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        boolean onItemClicked(int r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.color.widget.ColorLockPatternView.PatternExploreByTouchHelper.onItemClicked(int):boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.PatternExploreByTouchHelper.onItemClicked(int):boolean");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.color.widget.ColorLockPatternView.PatternExploreByTouchHelper.onPerformActionForVirtualView(int, int, android.os.Bundle):boolean, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        protected boolean onPerformActionForVirtualView(int r1, int r2, android.os.Bundle r3) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.color.widget.ColorLockPatternView.PatternExploreByTouchHelper.onPerformActionForVirtualView(int, int, android.os.Bundle):boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.PatternExploreByTouchHelper.onPerformActionForVirtualView(int, int, android.os.Bundle):boolean");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.color.widget.ColorLockPatternView.PatternExploreByTouchHelper.onPopulateAccessibilityEvent(android.view.View, android.view.accessibility.AccessibilityEvent):void, dex: 
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
        public void onPopulateAccessibilityEvent(android.view.View r1, android.view.accessibility.AccessibilityEvent r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.color.widget.ColorLockPatternView.PatternExploreByTouchHelper.onPopulateAccessibilityEvent(android.view.View, android.view.accessibility.AccessibilityEvent):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.PatternExploreByTouchHelper.onPopulateAccessibilityEvent(android.view.View, android.view.accessibility.AccessibilityEvent):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.color.widget.ColorLockPatternView.PatternExploreByTouchHelper.onPopulateEventForVirtualView(int, android.view.accessibility.AccessibilityEvent):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        protected void onPopulateEventForVirtualView(int r1, android.view.accessibility.AccessibilityEvent r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.color.widget.ColorLockPatternView.PatternExploreByTouchHelper.onPopulateEventForVirtualView(int, android.view.accessibility.AccessibilityEvent):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.PatternExploreByTouchHelper.onPopulateEventForVirtualView(int, android.view.accessibility.AccessibilityEvent):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.color.widget.ColorLockPatternView.PatternExploreByTouchHelper.onPopulateNodeForVirtualView(int, android.view.accessibility.AccessibilityNodeInfo):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        protected void onPopulateNodeForVirtualView(int r1, android.view.accessibility.AccessibilityNodeInfo r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.color.widget.ColorLockPatternView.PatternExploreByTouchHelper.onPopulateNodeForVirtualView(int, android.view.accessibility.AccessibilityNodeInfo):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.PatternExploreByTouchHelper.onPopulateNodeForVirtualView(int, android.view.accessibility.AccessibilityNodeInfo):void");
        }

        protected int getVirtualViewAt(float x, float y) {
            return getVirtualViewIdForHit(x, y);
        }
    }

    private static class SavedState extends BaseSavedState {
        public static final Creator<SavedState> CREATOR = null;
        private final int mDisplayMode;
        private final boolean mInStealthMode;
        private final boolean mInputEnabled;
        private final String mSerializedPattern;
        private final boolean mTactileFeedbackEnabled;

        /* renamed from: com.color.widget.ColorLockPatternView$SavedState$1 */
        static class AnonymousClass1 implements Creator<SavedState> {
            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.color.widget.ColorLockPatternView.SavedState.1.<init>():void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 7 more
                */
            AnonymousClass1() {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.color.widget.ColorLockPatternView.SavedState.1.<init>():void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.SavedState.1.<init>():void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.color.widget.ColorLockPatternView.SavedState.1.createFromParcel(android.os.Parcel):java.lang.Object, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 7 more
                */
            public /* bridge */ /* synthetic */ java.lang.Object createFromParcel(android.os.Parcel r1) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.color.widget.ColorLockPatternView.SavedState.1.createFromParcel(android.os.Parcel):java.lang.Object, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.SavedState.1.createFromParcel(android.os.Parcel):java.lang.Object");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.color.widget.ColorLockPatternView.SavedState.1.newArray(int):java.lang.Object[], dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 7 more
                */
            public /* bridge */ /* synthetic */ java.lang.Object[] newArray(int r1) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.color.widget.ColorLockPatternView.SavedState.1.newArray(int):java.lang.Object[], dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.SavedState.1.newArray(int):java.lang.Object[]");
            }

            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in, null);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.color.widget.ColorLockPatternView.SavedState.<clinit>():void, dex: 
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
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.color.widget.ColorLockPatternView.SavedState.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.SavedState.<clinit>():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.color.widget.ColorLockPatternView.SavedState.<init>(android.os.Parcel):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        private SavedState(android.os.Parcel r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.color.widget.ColorLockPatternView.SavedState.<init>(android.os.Parcel):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.SavedState.<init>(android.os.Parcel):void");
        }

        /* synthetic */ SavedState(Parcel in, SavedState savedState) {
            this(in);
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: com.color.widget.ColorLockPatternView.SavedState.<init>(android.os.Parcelable, java.lang.String, int, boolean, boolean, boolean):void, dex:  in method: com.color.widget.ColorLockPatternView.SavedState.<init>(android.os.Parcelable, java.lang.String, int, boolean, boolean, boolean):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: jadx.core.utils.exceptions.DecodeException: null in method: com.color.widget.ColorLockPatternView.SavedState.<init>(android.os.Parcelable, java.lang.String, int, boolean, boolean, boolean):void, dex: 
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
            	... 6 more
            Caused by: java.io.EOFException
            	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
            	at com.android.dx.io.instructions.ShortArrayCodeInput.readLong(ShortArrayCodeInput.java:73)
            	at com.android.dx.io.instructions.InstructionCodec$31.decode(InstructionCodec.java:652)
            	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
            	... 7 more
            */
        private SavedState(android.os.Parcelable r1, java.lang.String r2, int r3, boolean r4, boolean r5, boolean r6) {
            /*
            // Can't load method instructions: Load method exception: null in method: com.color.widget.ColorLockPatternView.SavedState.<init>(android.os.Parcelable, java.lang.String, int, boolean, boolean, boolean):void, dex:  in method: com.color.widget.ColorLockPatternView.SavedState.<init>(android.os.Parcelable, java.lang.String, int, boolean, boolean, boolean):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.SavedState.<init>(android.os.Parcelable, java.lang.String, int, boolean, boolean, boolean):void");
        }

        /* synthetic */ SavedState(Parcelable superState, String serializedPattern, int displayMode, boolean inputEnabled, boolean inStealthMode, boolean tactileFeedbackEnabled, SavedState savedState) {
            this(superState, serializedPattern, displayMode, inputEnabled, inStealthMode, tactileFeedbackEnabled);
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: com.color.widget.ColorLockPatternView.SavedState.getDisplayMode():int, dex:  in method: com.color.widget.ColorLockPatternView.SavedState.getDisplayMode():int, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: jadx.core.utils.exceptions.DecodeException: null in method: com.color.widget.ColorLockPatternView.SavedState.getDisplayMode():int, dex: 
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
        public int getDisplayMode() {
            /*
            // Can't load method instructions: Load method exception: null in method: com.color.widget.ColorLockPatternView.SavedState.getDisplayMode():int, dex:  in method: com.color.widget.ColorLockPatternView.SavedState.getDisplayMode():int, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.SavedState.getDisplayMode():int");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.color.widget.ColorLockPatternView.SavedState.getSerializedPattern():java.lang.String, dex: 
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
        public java.lang.String getSerializedPattern() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.color.widget.ColorLockPatternView.SavedState.getSerializedPattern():java.lang.String, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.SavedState.getSerializedPattern():java.lang.String");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: com.color.widget.ColorLockPatternView.SavedState.isInStealthMode():boolean, dex:  in method: com.color.widget.ColorLockPatternView.SavedState.isInStealthMode():boolean, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: jadx.core.utils.exceptions.DecodeException: null in method: com.color.widget.ColorLockPatternView.SavedState.isInStealthMode():boolean, dex: 
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
            	... 6 more
            Caused by: java.io.EOFException
            	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
            	at com.android.dx.io.instructions.ShortArrayCodeInput.readLong(ShortArrayCodeInput.java:71)
            	at com.android.dx.io.instructions.InstructionCodec$31.decode(InstructionCodec.java:652)
            	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
            	... 7 more
            */
        public boolean isInStealthMode() {
            /*
            // Can't load method instructions: Load method exception: null in method: com.color.widget.ColorLockPatternView.SavedState.isInStealthMode():boolean, dex:  in method: com.color.widget.ColorLockPatternView.SavedState.isInStealthMode():boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.SavedState.isInStealthMode():boolean");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00ef in method: com.color.widget.ColorLockPatternView.SavedState.isInputEnabled():boolean, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00ef
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        public boolean isInputEnabled() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00ef in method: com.color.widget.ColorLockPatternView.SavedState.isInputEnabled():boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.SavedState.isInputEnabled():boolean");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00ef in method: com.color.widget.ColorLockPatternView.SavedState.isTactileFeedbackEnabled():boolean, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00ef
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        public boolean isTactileFeedbackEnabled() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00ef in method: com.color.widget.ColorLockPatternView.SavedState.isTactileFeedbackEnabled():boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.SavedState.isTactileFeedbackEnabled():boolean");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.color.widget.ColorLockPatternView.SavedState.writeToParcel(android.os.Parcel, int):void, dex: 
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
        public void writeToParcel(android.os.Parcel r1, int r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.color.widget.ColorLockPatternView.SavedState.writeToParcel(android.os.Parcel, int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.SavedState.writeToParcel(android.os.Parcel, int):void");
        }
    }

    private class ShrinkdateListener implements AnimatorUpdateListener {
        private final Cell mCell;
        final /* synthetic */ ColorLockPatternView this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.color.widget.ColorLockPatternView.ShrinkdateListener.<init>(com.color.widget.ColorLockPatternView, com.color.widget.ColorLockPatternView$Cell):void, dex: 
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
        public ShrinkdateListener(com.color.widget.ColorLockPatternView r1, com.color.widget.ColorLockPatternView.Cell r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.color.widget.ColorLockPatternView.ShrinkdateListener.<init>(com.color.widget.ColorLockPatternView, com.color.widget.ColorLockPatternView$Cell):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.ShrinkdateListener.<init>(com.color.widget.ColorLockPatternView, com.color.widget.ColorLockPatternView$Cell):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.color.widget.ColorLockPatternView.ShrinkdateListener.onAnimationUpdate(android.animation.ValueAnimator):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        public void onAnimationUpdate(android.animation.ValueAnimator r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.color.widget.ColorLockPatternView.ShrinkdateListener.onAnimationUpdate(android.animation.ValueAnimator):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.ShrinkdateListener.onAnimationUpdate(android.animation.ValueAnimator):void");
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.color.widget.ColorLockPatternView.-get1(com.color.widget.ColorLockPatternView):android.media.AudioManager, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
        	... 5 more
        */
    /* renamed from: -get1 */
    static /* synthetic */ android.media.AudioManager m120-get1(com.color.widget.ColorLockPatternView r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.color.widget.ColorLockPatternView.-get1(com.color.widget.ColorLockPatternView):android.media.AudioManager, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.-get1(com.color.widget.ColorLockPatternView):android.media.AudioManager");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: com.color.widget.ColorLockPatternView.-get10(com.color.widget.ColorLockPatternView):float, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e3
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    /* renamed from: -get10 */
    static /* synthetic */ float m121-get10(com.color.widget.ColorLockPatternView r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: com.color.widget.ColorLockPatternView.-get10(com.color.widget.ColorLockPatternView):float, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.-get10(com.color.widget.ColorLockPatternView):float");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: com.color.widget.ColorLockPatternView.-get11(com.color.widget.ColorLockPatternView):float, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e3
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    /* renamed from: -get11 */
    static /* synthetic */ float m122-get11(com.color.widget.ColorLockPatternView r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: com.color.widget.ColorLockPatternView.-get11(com.color.widget.ColorLockPatternView):float, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.-get11(com.color.widget.ColorLockPatternView):float");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00ef in method: com.color.widget.ColorLockPatternView.-get12(com.color.widget.ColorLockPatternView):boolean, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00ef
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    /* renamed from: -get12 */
    static /* synthetic */ boolean m123-get12(com.color.widget.ColorLockPatternView r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00ef in method: com.color.widget.ColorLockPatternView.-get12(com.color.widget.ColorLockPatternView):boolean, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.-get12(com.color.widget.ColorLockPatternView):boolean");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00ef in method: com.color.widget.ColorLockPatternView.-get13(com.color.widget.ColorLockPatternView):boolean, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00ef
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    /* renamed from: -get13 */
    static /* synthetic */ boolean m124-get13(com.color.widget.ColorLockPatternView r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00ef in method: com.color.widget.ColorLockPatternView.-get13(com.color.widget.ColorLockPatternView):boolean, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.-get13(com.color.widget.ColorLockPatternView):boolean");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: com.color.widget.ColorLockPatternView.-get14(com.color.widget.ColorLockPatternView):float, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e3
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    /* renamed from: -get14 */
    static /* synthetic */ float m125-get14(com.color.widget.ColorLockPatternView r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: com.color.widget.ColorLockPatternView.-get14(com.color.widget.ColorLockPatternView):float, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.-get14(com.color.widget.ColorLockPatternView):float");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: com.color.widget.ColorLockPatternView.-get15(com.color.widget.ColorLockPatternView):float, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e3
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    /* renamed from: -get15 */
    static /* synthetic */ float m126-get15(com.color.widget.ColorLockPatternView r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: com.color.widget.ColorLockPatternView.-get15(com.color.widget.ColorLockPatternView):float, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.-get15(com.color.widget.ColorLockPatternView):float");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.color.widget.ColorLockPatternView.-get16(com.color.widget.ColorLockPatternView):boolean[][], dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
        	... 5 more
        */
    /* renamed from: -get16 */
    static /* synthetic */ boolean[][] m127-get16(com.color.widget.ColorLockPatternView r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.color.widget.ColorLockPatternView.-get16(com.color.widget.ColorLockPatternView):boolean[][], dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.-get16(com.color.widget.ColorLockPatternView):boolean[][]");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.color.widget.ColorLockPatternView.-get17(com.color.widget.ColorLockPatternView):int[][], dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
        	... 5 more
        */
    /* renamed from: -get17 */
    static /* synthetic */ int[][] m128-get17(com.color.widget.ColorLockPatternView r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.color.widget.ColorLockPatternView.-get17(com.color.widget.ColorLockPatternView):int[][], dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.-get17(com.color.widget.ColorLockPatternView):int[][]");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.color.widget.ColorLockPatternView.-get18(com.color.widget.ColorLockPatternView):java.util.ArrayList, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
        	... 5 more
        */
    /* renamed from: -get18 */
    static /* synthetic */ java.util.ArrayList m129-get18(com.color.widget.ColorLockPatternView r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.color.widget.ColorLockPatternView.-get18(com.color.widget.ColorLockPatternView):java.util.ArrayList, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.-get18(com.color.widget.ColorLockPatternView):java.util.ArrayList");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.color.widget.ColorLockPatternView.-get19(com.color.widget.ColorLockPatternView):com.color.widget.ColorLockPatternView$DisplayMode, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
        	... 5 more
        */
    /* renamed from: -get19 */
    static /* synthetic */ com.color.widget.ColorLockPatternView.DisplayMode m130-get19(com.color.widget.ColorLockPatternView r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.color.widget.ColorLockPatternView.-get19(com.color.widget.ColorLockPatternView):com.color.widget.ColorLockPatternView$DisplayMode, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.-get19(com.color.widget.ColorLockPatternView):com.color.widget.ColorLockPatternView$DisplayMode");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: com.color.widget.ColorLockPatternView.-get2(com.color.widget.ColorLockPatternView):float, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e3
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    /* renamed from: -get2 */
    static /* synthetic */ float m131-get2(com.color.widget.ColorLockPatternView r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: com.color.widget.ColorLockPatternView.-get2(com.color.widget.ColorLockPatternView):float, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.-get2(com.color.widget.ColorLockPatternView):float");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.color.widget.ColorLockPatternView.-get20(com.color.widget.ColorLockPatternView):boolean[][], dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
        	... 5 more
        */
    /* renamed from: -get20 */
    static /* synthetic */ boolean[][] m132-get20(com.color.widget.ColorLockPatternView r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.color.widget.ColorLockPatternView.-get20(com.color.widget.ColorLockPatternView):boolean[][], dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.-get20(com.color.widget.ColorLockPatternView):boolean[][]");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00ef in method: com.color.widget.ColorLockPatternView.-get21(com.color.widget.ColorLockPatternView):boolean, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00ef
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    /* renamed from: -get21 */
    static /* synthetic */ boolean m133-get21(com.color.widget.ColorLockPatternView r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00ef in method: com.color.widget.ColorLockPatternView.-get21(com.color.widget.ColorLockPatternView):boolean, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.-get21(com.color.widget.ColorLockPatternView):boolean");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: com.color.widget.ColorLockPatternView.-get22(com.color.widget.ColorLockPatternView):float, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e3
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    /* renamed from: -get22 */
    static /* synthetic */ float m134-get22(com.color.widget.ColorLockPatternView r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: com.color.widget.ColorLockPatternView.-get22(com.color.widget.ColorLockPatternView):float, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.-get22(com.color.widget.ColorLockPatternView):float");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: com.color.widget.ColorLockPatternView.-get23(com.color.widget.ColorLockPatternView):float, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e3
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    /* renamed from: -get23 */
    static /* synthetic */ float m135-get23(com.color.widget.ColorLockPatternView r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: com.color.widget.ColorLockPatternView.-get23(com.color.widget.ColorLockPatternView):float, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.-get23(com.color.widget.ColorLockPatternView):float");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: com.color.widget.ColorLockPatternView.-get24(com.color.widget.ColorLockPatternView):float, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e3
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    /* renamed from: -get24 */
    static /* synthetic */ float m136-get24(com.color.widget.ColorLockPatternView r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: com.color.widget.ColorLockPatternView.-get24(com.color.widget.ColorLockPatternView):float, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.-get24(com.color.widget.ColorLockPatternView):float");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: com.color.widget.ColorLockPatternView.-get25(com.color.widget.ColorLockPatternView):float, dex:  in method: com.color.widget.ColorLockPatternView.-get25(com.color.widget.ColorLockPatternView):float, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: jadx.core.utils.exceptions.DecodeException: null in method: com.color.widget.ColorLockPatternView.-get25(com.color.widget.ColorLockPatternView):float, dex: 
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
        	... 5 more
        Caused by: java.io.EOFException
        	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
        	at com.android.dx.io.instructions.InstructionCodec.decodeRegisterList(InstructionCodec.java:920)
        	at com.android.dx.io.instructions.InstructionCodec.access$900(InstructionCodec.java:31)
        	at com.android.dx.io.instructions.InstructionCodec$25.decode(InstructionCodec.java:572)
        	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
        	... 6 more
        */
    /* renamed from: -get25 */
    static /* synthetic */ float m137-get25(com.color.widget.ColorLockPatternView r1) {
        /*
        // Can't load method instructions: Load method exception: null in method: com.color.widget.ColorLockPatternView.-get25(com.color.widget.ColorLockPatternView):float, dex:  in method: com.color.widget.ColorLockPatternView.-get25(com.color.widget.ColorLockPatternView):float, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.-get25(com.color.widget.ColorLockPatternView):float");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: com.color.widget.ColorLockPatternView.-get26(com.color.widget.ColorLockPatternView):int, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e3
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    /* renamed from: -get26 */
    static /* synthetic */ int m138-get26(com.color.widget.ColorLockPatternView r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: com.color.widget.ColorLockPatternView.-get26(com.color.widget.ColorLockPatternView):int, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.-get26(com.color.widget.ColorLockPatternView):int");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.color.widget.ColorLockPatternView.-get27(com.color.widget.ColorLockPatternView):float[][], dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
        	... 5 more
        */
    /* renamed from: -get27 */
    static /* synthetic */ float[][] m139-get27(com.color.widget.ColorLockPatternView r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.color.widget.ColorLockPatternView.-get27(com.color.widget.ColorLockPatternView):float[][], dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.-get27(com.color.widget.ColorLockPatternView):float[][]");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: com.color.widget.ColorLockPatternView.-get28(com.color.widget.ColorLockPatternView):int, dex:  in method: com.color.widget.ColorLockPatternView.-get28(com.color.widget.ColorLockPatternView):int, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: jadx.core.utils.exceptions.DecodeException: null in method: com.color.widget.ColorLockPatternView.-get28(com.color.widget.ColorLockPatternView):int, dex: 
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
        	... 5 more
        Caused by: java.io.EOFException
        	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
        	at com.android.dx.io.instructions.InstructionCodec.decodeRegisterRange(InstructionCodec.java:985)
        	at com.android.dx.io.instructions.InstructionCodec.access$1100(InstructionCodec.java:31)
        	at com.android.dx.io.instructions.InstructionCodec$28.decode(InstructionCodec.java:611)
        	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
        	... 6 more
        */
    /* renamed from: -get28 */
    static /* synthetic */ int m140-get28(com.color.widget.ColorLockPatternView r1) {
        /*
        // Can't load method instructions: Load method exception: null in method: com.color.widget.ColorLockPatternView.-get28(com.color.widget.ColorLockPatternView):int, dex:  in method: com.color.widget.ColorLockPatternView.-get28(com.color.widget.ColorLockPatternView):int, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.-get28(com.color.widget.ColorLockPatternView):int");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: com.color.widget.ColorLockPatternView.-get29(com.color.widget.ColorLockPatternView):int, dex:  in method: com.color.widget.ColorLockPatternView.-get29(com.color.widget.ColorLockPatternView):int, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: jadx.core.utils.exceptions.DecodeException: null in method: com.color.widget.ColorLockPatternView.-get29(com.color.widget.ColorLockPatternView):int, dex: 
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
        	... 5 more
        Caused by: java.io.EOFException
        	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
        	at com.android.dx.io.instructions.InstructionCodec.decodeRegisterRange(InstructionCodec.java:985)
        	at com.android.dx.io.instructions.InstructionCodec.access$1100(InstructionCodec.java:31)
        	at com.android.dx.io.instructions.InstructionCodec$28.decode(InstructionCodec.java:611)
        	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
        	... 6 more
        */
    /* renamed from: -get29 */
    static /* synthetic */ int m141-get29(com.color.widget.ColorLockPatternView r1) {
        /*
        // Can't load method instructions: Load method exception: null in method: com.color.widget.ColorLockPatternView.-get29(com.color.widget.ColorLockPatternView):int, dex:  in method: com.color.widget.ColorLockPatternView.-get29(com.color.widget.ColorLockPatternView):int, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.-get29(com.color.widget.ColorLockPatternView):int");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: com.color.widget.ColorLockPatternView.-get3(com.color.widget.ColorLockPatternView):android.content.Context, dex:  in method: com.color.widget.ColorLockPatternView.-get3(com.color.widget.ColorLockPatternView):android.content.Context, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: jadx.core.utils.exceptions.DecodeException: null in method: com.color.widget.ColorLockPatternView.-get3(com.color.widget.ColorLockPatternView):android.content.Context, dex: 
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
        	... 5 more
        Caused by: java.io.EOFException
        	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
        	at com.android.dx.io.instructions.ShortArrayCodeInput.readInt(ShortArrayCodeInput.java:62)
        	at com.android.dx.io.instructions.InstructionCodec$23.decode(InstructionCodec.java:514)
        	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
        	... 6 more
        */
    /* renamed from: -get3 */
    static /* synthetic */ android.content.Context m142-get3(com.color.widget.ColorLockPatternView r1) {
        /*
        // Can't load method instructions: Load method exception: null in method: com.color.widget.ColorLockPatternView.-get3(com.color.widget.ColorLockPatternView):android.content.Context, dex:  in method: com.color.widget.ColorLockPatternView.-get3(com.color.widget.ColorLockPatternView):android.content.Context, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.-get3(com.color.widget.ColorLockPatternView):android.content.Context");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: com.color.widget.ColorLockPatternView.-get30(com.color.widget.ColorLockPatternView):int, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e3
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    /* renamed from: -get30 */
    static /* synthetic */ int m143-get30(com.color.widget.ColorLockPatternView r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: com.color.widget.ColorLockPatternView.-get30(com.color.widget.ColorLockPatternView):int, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.-get30(com.color.widget.ColorLockPatternView):int");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: com.color.widget.ColorLockPatternView.-get31(com.color.widget.ColorLockPatternView):int, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e3
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    /* renamed from: -get31 */
    static /* synthetic */ int m144-get31(com.color.widget.ColorLockPatternView r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: com.color.widget.ColorLockPatternView.-get31(com.color.widget.ColorLockPatternView):int, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.-get31(com.color.widget.ColorLockPatternView):int");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: com.color.widget.ColorLockPatternView.-get32(com.color.widget.ColorLockPatternView):int, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e3
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    /* renamed from: -get32 */
    static /* synthetic */ int m145-get32(com.color.widget.ColorLockPatternView r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: com.color.widget.ColorLockPatternView.-get32(com.color.widget.ColorLockPatternView):int, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.-get32(com.color.widget.ColorLockPatternView):int");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: com.color.widget.ColorLockPatternView.-get33(com.color.widget.ColorLockPatternView):int, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e3
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    /* renamed from: -get33 */
    static /* synthetic */ int m146-get33(com.color.widget.ColorLockPatternView r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: com.color.widget.ColorLockPatternView.-get33(com.color.widget.ColorLockPatternView):int, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.-get33(com.color.widget.ColorLockPatternView):int");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: com.color.widget.ColorLockPatternView.-get4(com.color.widget.ColorLockPatternView):int, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e3
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    /* renamed from: -get4 */
    static /* synthetic */ int m147-get4(com.color.widget.ColorLockPatternView r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: com.color.widget.ColorLockPatternView.-get4(com.color.widget.ColorLockPatternView):int, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.-get4(com.color.widget.ColorLockPatternView):int");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: com.color.widget.ColorLockPatternView.-get5(com.color.widget.ColorLockPatternView):int, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e3
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    /* renamed from: -get5 */
    static /* synthetic */ int m148-get5(com.color.widget.ColorLockPatternView r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: com.color.widget.ColorLockPatternView.-get5(com.color.widget.ColorLockPatternView):int, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.-get5(com.color.widget.ColorLockPatternView):int");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: com.color.widget.ColorLockPatternView.-get6(com.color.widget.ColorLockPatternView):int, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e3
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    /* renamed from: -get6 */
    static /* synthetic */ int m149-get6(com.color.widget.ColorLockPatternView r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: com.color.widget.ColorLockPatternView.-get6(com.color.widget.ColorLockPatternView):int, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.-get6(com.color.widget.ColorLockPatternView):int");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: com.color.widget.ColorLockPatternView.-get7(com.color.widget.ColorLockPatternView):float, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e3
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    /* renamed from: -get7 */
    static /* synthetic */ float m150-get7(com.color.widget.ColorLockPatternView r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: com.color.widget.ColorLockPatternView.-get7(com.color.widget.ColorLockPatternView):float, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.-get7(com.color.widget.ColorLockPatternView):float");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: com.color.widget.ColorLockPatternView.-get8(com.color.widget.ColorLockPatternView):int, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e3
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    /* renamed from: -get8 */
    static /* synthetic */ int m151-get8(com.color.widget.ColorLockPatternView r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: com.color.widget.ColorLockPatternView.-get8(com.color.widget.ColorLockPatternView):int, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.-get8(com.color.widget.ColorLockPatternView):int");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.color.widget.ColorLockPatternView.-get9(com.color.widget.ColorLockPatternView):android.graphics.Paint, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
        	... 5 more
        */
    /* renamed from: -get9 */
    static /* synthetic */ android.graphics.Paint m152-get9(com.color.widget.ColorLockPatternView r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.color.widget.ColorLockPatternView.-get9(com.color.widget.ColorLockPatternView):android.graphics.Paint, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.-get9(com.color.widget.ColorLockPatternView):android.graphics.Paint");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e6 in method: com.color.widget.ColorLockPatternView.-set0(com.color.widget.ColorLockPatternView, float):float, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e6
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    /* renamed from: -set0 */
    static /* synthetic */ float m153-set0(com.color.widget.ColorLockPatternView r1, float r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e6 in method: com.color.widget.ColorLockPatternView.-set0(com.color.widget.ColorLockPatternView, float):float, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.-set0(com.color.widget.ColorLockPatternView, float):float");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00eb in method: com.color.widget.ColorLockPatternView.-set1(com.color.widget.ColorLockPatternView, boolean):boolean, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00eb
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    /* renamed from: -set1 */
    static /* synthetic */ boolean m154-set1(com.color.widget.ColorLockPatternView r1, boolean r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00eb in method: com.color.widget.ColorLockPatternView.-set1(com.color.widget.ColorLockPatternView, boolean):boolean, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.-set1(com.color.widget.ColorLockPatternView, boolean):boolean");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: com.color.widget.ColorLockPatternView.-set2(com.color.widget.ColorLockPatternView, boolean):boolean, dex:  in method: com.color.widget.ColorLockPatternView.-set2(com.color.widget.ColorLockPatternView, boolean):boolean, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: jadx.core.utils.exceptions.DecodeException: null in method: com.color.widget.ColorLockPatternView.-set2(com.color.widget.ColorLockPatternView, boolean):boolean, dex: 
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
        	... 5 more
        Caused by: java.io.EOFException
        	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
        	at com.android.dx.io.instructions.InstructionCodec$21.decode(InstructionCodec.java:471)
        	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
        	... 6 more
        */
    /* renamed from: -set2 */
    static /* synthetic */ boolean m155-set2(com.color.widget.ColorLockPatternView r1, boolean r2) {
        /*
        // Can't load method instructions: Load method exception: null in method: com.color.widget.ColorLockPatternView.-set2(com.color.widget.ColorLockPatternView, boolean):boolean, dex:  in method: com.color.widget.ColorLockPatternView.-set2(com.color.widget.ColorLockPatternView, boolean):boolean, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.-set2(com.color.widget.ColorLockPatternView, boolean):boolean");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00eb in method: com.color.widget.ColorLockPatternView.-set3(com.color.widget.ColorLockPatternView, boolean):boolean, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00eb
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    /* renamed from: -set3 */
    static /* synthetic */ boolean m156-set3(com.color.widget.ColorLockPatternView r1, boolean r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00eb in method: com.color.widget.ColorLockPatternView.-set3(com.color.widget.ColorLockPatternView, boolean):boolean, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.-set3(com.color.widget.ColorLockPatternView, boolean):boolean");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.color.widget.ColorLockPatternView.-set4(com.color.widget.ColorLockPatternView, com.color.widget.ColorLockPatternView$DisplayMode):com.color.widget.ColorLockPatternView$DisplayMode, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
        	... 5 more
        */
    /* renamed from: -set4 */
    static /* synthetic */ com.color.widget.ColorLockPatternView.DisplayMode m157-set4(com.color.widget.ColorLockPatternView r1, com.color.widget.ColorLockPatternView.DisplayMode r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.color.widget.ColorLockPatternView.-set4(com.color.widget.ColorLockPatternView, com.color.widget.ColorLockPatternView$DisplayMode):com.color.widget.ColorLockPatternView$DisplayMode, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.-set4(com.color.widget.ColorLockPatternView, com.color.widget.ColorLockPatternView$DisplayMode):com.color.widget.ColorLockPatternView$DisplayMode");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e7 in method: com.color.widget.ColorLockPatternView.-set5(com.color.widget.ColorLockPatternView, double):double, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e7
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    /* renamed from: -set5 */
    static /* synthetic */ double m158-set5(com.color.widget.ColorLockPatternView r1, double r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e7 in method: com.color.widget.ColorLockPatternView.-set5(com.color.widget.ColorLockPatternView, double):double, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.-set5(com.color.widget.ColorLockPatternView, double):double");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e6 in method: com.color.widget.ColorLockPatternView.-set6(com.color.widget.ColorLockPatternView, int):int, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e6
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    /* renamed from: -set6 */
    static /* synthetic */ int m159-set6(com.color.widget.ColorLockPatternView r1, int r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e6 in method: com.color.widget.ColorLockPatternView.-set6(com.color.widget.ColorLockPatternView, int):int, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.-set6(com.color.widget.ColorLockPatternView, int):int");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e6 in method: com.color.widget.ColorLockPatternView.-set7(com.color.widget.ColorLockPatternView, int):int, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e6
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    /* renamed from: -set7 */
    static /* synthetic */ int m160-set7(com.color.widget.ColorLockPatternView r1, int r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e6 in method: com.color.widget.ColorLockPatternView.-set7(com.color.widget.ColorLockPatternView, int):int, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.-set7(com.color.widget.ColorLockPatternView, int):int");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.color.widget.ColorLockPatternView.-wrap4(com.color.widget.ColorLockPatternView):void, dex: 
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
    /* renamed from: -wrap4 */
    static /* synthetic */ void m161-wrap4(com.color.widget.ColorLockPatternView r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.color.widget.ColorLockPatternView.-wrap4(com.color.widget.ColorLockPatternView):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.-wrap4(com.color.widget.ColorLockPatternView):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.color.widget.ColorLockPatternView.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.color.widget.ColorLockPatternView.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.<clinit>():void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus element_width: 1e12 in method: com.color.widget.ColorLockPatternView.<init>(android.content.Context, android.util.AttributeSet, int):void, dex:  in method: com.color.widget.ColorLockPatternView.<init>(android.content.Context, android.util.AttributeSet, int):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: jadx.core.utils.exceptions.DecodeException: bogus element_width: 1e12 in method: com.color.widget.ColorLockPatternView.<init>(android.content.Context, android.util.AttributeSet, int):void, dex: 
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
        	... 5 more
        Caused by: com.android.dex.DexException: bogus element_width: 1e12
        	at com.android.dx.io.instructions.InstructionCodec$36.decode(InstructionCodec.java:871)
        	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
        	... 6 more
        */
    public ColorLockPatternView(android.content.Context r1, android.util.AttributeSet r2, int r3) {
        /*
        // Can't load method instructions: Load method exception: bogus element_width: 1e12 in method: com.color.widget.ColorLockPatternView.<init>(android.content.Context, android.util.AttributeSet, int):void, dex:  in method: com.color.widget.ColorLockPatternView.<init>(android.content.Context, android.util.AttributeSet, int):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.<init>(android.content.Context, android.util.AttributeSet, int):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.color.widget.ColorLockPatternView.addCellToPattern(com.color.widget.ColorLockPatternView$Cell):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
        	... 5 more
        */
    private void addCellToPattern(com.color.widget.ColorLockPatternView.Cell r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.color.widget.ColorLockPatternView.addCellToPattern(com.color.widget.ColorLockPatternView$Cell):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.addCellToPattern(com.color.widget.ColorLockPatternView$Cell):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.color.widget.ColorLockPatternView.addFillInGapCellX(float, float, float, float, com.color.widget.ColorLockPatternView$Cell):float, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
        	... 5 more
        */
    private float addFillInGapCellX(float r1, float r2, float r3, float r4, com.color.widget.ColorLockPatternView.Cell r5) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.color.widget.ColorLockPatternView.addFillInGapCellX(float, float, float, float, com.color.widget.ColorLockPatternView$Cell):float, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.addFillInGapCellX(float, float, float, float, com.color.widget.ColorLockPatternView$Cell):float");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.color.widget.ColorLockPatternView.calculateEnd(com.color.widget.ColorLockPatternView$Cell, float, float, float, float, java.util.ArrayList, float):java.util.ArrayList<java.lang.Float>, dex: 
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
    private java.util.ArrayList<java.lang.Float> calculateEnd(com.color.widget.ColorLockPatternView.Cell r1, float r2, float r3, float r4, float r5, java.util.ArrayList<java.lang.Float> r6, float r7) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.color.widget.ColorLockPatternView.calculateEnd(com.color.widget.ColorLockPatternView$Cell, float, float, float, float, java.util.ArrayList, float):java.util.ArrayList<java.lang.Float>, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.calculateEnd(com.color.widget.ColorLockPatternView$Cell, float, float, float, float, java.util.ArrayList, float):java.util.ArrayList<java.lang.Float>");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: com.color.widget.ColorLockPatternView.calculateProgressPoints(float, float, float, float, float, com.color.widget.ColorLockPatternView$Cell, boolean):void, dex:  in method: com.color.widget.ColorLockPatternView.calculateProgressPoints(float, float, float, float, float, com.color.widget.ColorLockPatternView$Cell, boolean):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: jadx.core.utils.exceptions.DecodeException: null in method: com.color.widget.ColorLockPatternView.calculateProgressPoints(float, float, float, float, float, com.color.widget.ColorLockPatternView$Cell, boolean):void, dex: 
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
        	... 5 more
        Caused by: java.io.EOFException
        	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
        	at com.android.dx.io.instructions.ShortArrayCodeInput.readInt(ShortArrayCodeInput.java:62)
        	at com.android.dx.io.instructions.InstructionCodec$35.decode(InstructionCodec.java:790)
        	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
        	... 6 more
        */
    private void calculateProgressPoints(float r1, float r2, float r3, float r4, float r5, com.color.widget.ColorLockPatternView.Cell r6, boolean r7) {
        /*
        // Can't load method instructions: Load method exception: null in method: com.color.widget.ColorLockPatternView.calculateProgressPoints(float, float, float, float, float, com.color.widget.ColorLockPatternView$Cell, boolean):void, dex:  in method: com.color.widget.ColorLockPatternView.calculateProgressPoints(float, float, float, float, float, com.color.widget.ColorLockPatternView$Cell, boolean):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.calculateProgressPoints(float, float, float, float, float, com.color.widget.ColorLockPatternView$Cell, boolean):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.color.widget.ColorLockPatternView.calculateStart(com.color.widget.ColorLockPatternView$Cell, float, float, float, float, java.util.ArrayList, float):java.util.ArrayList<java.lang.Float>, dex: 
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
    private java.util.ArrayList<java.lang.Float> calculateStart(com.color.widget.ColorLockPatternView.Cell r1, float r2, float r3, float r4, float r5, java.util.ArrayList<java.lang.Float> r6, float r7) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.color.widget.ColorLockPatternView.calculateStart(com.color.widget.ColorLockPatternView$Cell, float, float, float, float, java.util.ArrayList, float):java.util.ArrayList<java.lang.Float>, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.calculateStart(com.color.widget.ColorLockPatternView$Cell, float, float, float, float, java.util.ArrayList, float):java.util.ArrayList<java.lang.Float>");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: com.color.widget.ColorLockPatternView.calculateStartPoints(float, float, float, float, float, float, com.color.widget.ColorLockPatternView$Cell, boolean):void, dex:  in method: com.color.widget.ColorLockPatternView.calculateStartPoints(float, float, float, float, float, float, com.color.widget.ColorLockPatternView$Cell, boolean):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: jadx.core.utils.exceptions.DecodeException: null in method: com.color.widget.ColorLockPatternView.calculateStartPoints(float, float, float, float, float, float, com.color.widget.ColorLockPatternView$Cell, boolean):void, dex: 
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
        	... 5 more
        Caused by: java.io.EOFException
        	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
        	at com.android.dx.io.instructions.ShortArrayCodeInput.readInt(ShortArrayCodeInput.java:61)
        	at com.android.dx.io.instructions.InstructionCodec$35.decode(InstructionCodec.java:790)
        	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
        	... 6 more
        */
    private void calculateStartPoints(float r1, float r2, float r3, float r4, float r5, float r6, com.color.widget.ColorLockPatternView.Cell r7, boolean r8) {
        /*
        // Can't load method instructions: Load method exception: null in method: com.color.widget.ColorLockPatternView.calculateStartPoints(float, float, float, float, float, float, com.color.widget.ColorLockPatternView$Cell, boolean):void, dex:  in method: com.color.widget.ColorLockPatternView.calculateStartPoints(float, float, float, float, float, float, com.color.widget.ColorLockPatternView$Cell, boolean):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.calculateStartPoints(float, float, float, float, float, float, com.color.widget.ColorLockPatternView$Cell, boolean):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.color.widget.ColorLockPatternView.changePathPaintColor():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
        	... 5 more
        */
    private void changePathPaintColor() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.color.widget.ColorLockPatternView.changePathPaintColor():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.changePathPaintColor():void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.color.widget.ColorLockPatternView.checkForNewHit(float, float):com.color.widget.ColorLockPatternView$Cell, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
        	... 5 more
        */
    private com.color.widget.ColorLockPatternView.Cell checkForNewHit(float r1, float r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.color.widget.ColorLockPatternView.checkForNewHit(float, float):com.color.widget.ColorLockPatternView$Cell, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.checkForNewHit(float, float):com.color.widget.ColorLockPatternView$Cell");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.color.widget.ColorLockPatternView.clearPatternDrawLookup():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
        	... 5 more
        */
    private void clearPatternDrawLookup() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.color.widget.ColorLockPatternView.clearPatternDrawLookup():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.clearPatternDrawLookup():void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.color.widget.ColorLockPatternView.detectAndAddHit(float, float):com.color.widget.ColorLockPatternView$Cell, dex: 
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
    private com.color.widget.ColorLockPatternView.Cell detectAndAddHit(float r1, float r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.color.widget.ColorLockPatternView.detectAndAddHit(float, float):com.color.widget.ColorLockPatternView$Cell, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.detectAndAddHit(float, float):com.color.widget.ColorLockPatternView$Cell");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: com.color.widget.ColorLockPatternView.drawCircle(android.graphics.Canvas):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e3
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    private void drawCircle(android.graphics.Canvas r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: com.color.widget.ColorLockPatternView.drawCircle(android.graphics.Canvas):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.drawCircle(android.graphics.Canvas):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus element_width: 0538 in method: com.color.widget.ColorLockPatternView.drawCircle(android.graphics.Canvas, int, int, int, int, boolean):void, dex:  in method: com.color.widget.ColorLockPatternView.drawCircle(android.graphics.Canvas, int, int, int, int, boolean):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: jadx.core.utils.exceptions.DecodeException: bogus element_width: 0538 in method: com.color.widget.ColorLockPatternView.drawCircle(android.graphics.Canvas, int, int, int, int, boolean):void, dex: 
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
        	... 5 more
        Caused by: com.android.dex.DexException: bogus element_width: 0538
        	at com.android.dx.io.instructions.InstructionCodec$36.decode(InstructionCodec.java:871)
        	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
        	... 6 more
        */
    private void drawCircle(android.graphics.Canvas r1, int r2, int r3, int r4, int r5, boolean r6) {
        /*
        // Can't load method instructions: Load method exception: bogus element_width: 0538 in method: com.color.widget.ColorLockPatternView.drawCircle(android.graphics.Canvas, int, int, int, int, boolean):void, dex:  in method: com.color.widget.ColorLockPatternView.drawCircle(android.graphics.Canvas, int, int, int, int, boolean):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.drawCircle(android.graphics.Canvas, int, int, int, int, boolean):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.color.widget.ColorLockPatternView.drawCircularRing(android.graphics.Canvas, int, int):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
        	... 5 more
        */
    private void drawCircularRing(android.graphics.Canvas r1, int r2, int r3) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.color.widget.ColorLockPatternView.drawCircularRing(android.graphics.Canvas, int, int):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.drawCircularRing(android.graphics.Canvas, int, int):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: com.color.widget.ColorLockPatternView.drawFingerCircle(android.graphics.Canvas):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e3
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    private void drawFingerCircle(android.graphics.Canvas r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: com.color.widget.ColorLockPatternView.drawFingerCircle(android.graphics.Canvas):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.drawFingerCircle(android.graphics.Canvas):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: com.color.widget.ColorLockPatternView.drawLadder(com.color.widget.ColorLockPatternView$Cell, java.util.ArrayList, float, float, android.graphics.Path, int, boolean, android.graphics.Canvas, java.util.ArrayList, int):void, dex:  in method: com.color.widget.ColorLockPatternView.drawLadder(com.color.widget.ColorLockPatternView$Cell, java.util.ArrayList, float, float, android.graphics.Path, int, boolean, android.graphics.Canvas, java.util.ArrayList, int):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: jadx.core.utils.exceptions.DecodeException: null in method: com.color.widget.ColorLockPatternView.drawLadder(com.color.widget.ColorLockPatternView$Cell, java.util.ArrayList, float, float, android.graphics.Path, int, boolean, android.graphics.Canvas, java.util.ArrayList, int):void, dex: 
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
        	... 5 more
        Caused by: java.io.EOFException
        	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
        	at com.android.dx.io.instructions.ShortArrayCodeInput.readInt(ShortArrayCodeInput.java:62)
        	at com.android.dx.io.instructions.InstructionCodec$35.decode(InstructionCodec.java:790)
        	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
        	... 6 more
        */
    private void drawLadder(com.color.widget.ColorLockPatternView.Cell r1, java.util.ArrayList<java.lang.Float> r2, float r3, float r4, android.graphics.Path r5, int r6, boolean r7, android.graphics.Canvas r8, java.util.ArrayList<com.color.widget.ColorLockPatternView.Cell> r9, int r10) {
        /*
        // Can't load method instructions: Load method exception: null in method: com.color.widget.ColorLockPatternView.drawLadder(com.color.widget.ColorLockPatternView$Cell, java.util.ArrayList, float, float, android.graphics.Path, int, boolean, android.graphics.Canvas, java.util.ArrayList, int):void, dex:  in method: com.color.widget.ColorLockPatternView.drawLadder(com.color.widget.ColorLockPatternView$Cell, java.util.ArrayList, float, float, android.graphics.Path, int, boolean, android.graphics.Canvas, java.util.ArrayList, int):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.drawLadder(com.color.widget.ColorLockPatternView$Cell, java.util.ArrayList, float, float, android.graphics.Path, int, boolean, android.graphics.Canvas, java.util.ArrayList, int):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: com.color.widget.ColorLockPatternView.drawLinePath(com.color.widget.ColorLockPatternView$Cell, java.util.ArrayList, java.util.ArrayList, float, float, android.graphics.Path, int, boolean, android.graphics.Canvas, java.util.ArrayList, int):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e3
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    private void drawLinePath(com.color.widget.ColorLockPatternView.Cell r1, java.util.ArrayList<java.lang.Float> r2, java.util.ArrayList<java.lang.Float> r3, float r4, float r5, android.graphics.Path r6, int r7, boolean r8, android.graphics.Canvas r9, java.util.ArrayList<com.color.widget.ColorLockPatternView.Cell> r10, int r11) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: com.color.widget.ColorLockPatternView.drawLinePath(com.color.widget.ColorLockPatternView$Cell, java.util.ArrayList, java.util.ArrayList, float, float, android.graphics.Path, int, boolean, android.graphics.Canvas, java.util.ArrayList, int):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.drawLinePath(com.color.widget.ColorLockPatternView$Cell, java.util.ArrayList, java.util.ArrayList, float, float, android.graphics.Path, int, boolean, android.graphics.Canvas, java.util.ArrayList, int):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.color.widget.ColorLockPatternView.drawWrongCircle(android.graphics.Canvas, int, int):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
        	... 5 more
        */
    private void drawWrongCircle(android.graphics.Canvas r1, int r2, int r3) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.color.widget.ColorLockPatternView.drawWrongCircle(android.graphics.Canvas, int, int):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.drawWrongCircle(android.graphics.Canvas, int, int):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: com.color.widget.ColorLockPatternView.getCenterXForColumn(int):float, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e3
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    private float getCenterXForColumn(int r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: com.color.widget.ColorLockPatternView.getCenterXForColumn(int):float, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.getCenterXForColumn(int):float");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: com.color.widget.ColorLockPatternView.getCenterYForRow(int):float, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e3
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    private float getCenterYForRow(int r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: com.color.widget.ColorLockPatternView.getCenterYForRow(int):float, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.getCenterYForRow(int):float");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: com.color.widget.ColorLockPatternView.getColumnHit(float):int, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e3
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    private int getColumnHit(float r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: com.color.widget.ColorLockPatternView.getColumnHit(float):int, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.getColumnHit(float):int");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.color.widget.ColorLockPatternView.getDimensionOrFraction(android.content.res.TypedArray, int, int, int):int, dex: 
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
    static int getDimensionOrFraction(android.content.res.TypedArray r1, int r2, int r3, int r4) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.color.widget.ColorLockPatternView.getDimensionOrFraction(android.content.res.TypedArray, int, int, int):int, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.getDimensionOrFraction(android.content.res.TypedArray, int, int, int):int");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: com.color.widget.ColorLockPatternView.getRowHit(float):int, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e3
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    private int getRowHit(float r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: com.color.widget.ColorLockPatternView.getRowHit(float):int, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.getRowHit(float):int");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: com.color.widget.ColorLockPatternView.getShrinkShader(float, float, com.color.widget.ColorLockPatternView$Cell):void, dex:  in method: com.color.widget.ColorLockPatternView.getShrinkShader(float, float, com.color.widget.ColorLockPatternView$Cell):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: jadx.core.utils.exceptions.DecodeException: null in method: com.color.widget.ColorLockPatternView.getShrinkShader(float, float, com.color.widget.ColorLockPatternView$Cell):void, dex: 
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
        	... 5 more
        Caused by: java.io.EOFException
        	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
        	at com.android.dx.io.instructions.InstructionCodec.decodeRegisterList(InstructionCodec.java:920)
        	at com.android.dx.io.instructions.InstructionCodec.access$900(InstructionCodec.java:31)
        	at com.android.dx.io.instructions.InstructionCodec$25.decode(InstructionCodec.java:572)
        	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
        	... 6 more
        */
    private void getShrinkShader(float r1, float r2, com.color.widget.ColorLockPatternView.Cell r3) {
        /*
        // Can't load method instructions: Load method exception: null in method: com.color.widget.ColorLockPatternView.getShrinkShader(float, float, com.color.widget.ColorLockPatternView$Cell):void, dex:  in method: com.color.widget.ColorLockPatternView.getShrinkShader(float, float, com.color.widget.ColorLockPatternView$Cell):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.getShrinkShader(float, float, com.color.widget.ColorLockPatternView$Cell):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.color.widget.ColorLockPatternView.handleActionDown(android.view.MotionEvent):void, dex: 
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
    private void handleActionDown(android.view.MotionEvent r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.color.widget.ColorLockPatternView.handleActionDown(android.view.MotionEvent):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.handleActionDown(android.view.MotionEvent):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00ea in method: com.color.widget.ColorLockPatternView.handleActionMove(android.view.MotionEvent):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00ea
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    private void handleActionMove(android.view.MotionEvent r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00ea in method: com.color.widget.ColorLockPatternView.handleActionMove(android.view.MotionEvent):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.handleActionMove(android.view.MotionEvent):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.color.widget.ColorLockPatternView.handleActionUp():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
        	... 5 more
        */
    private void handleActionUp() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.color.widget.ColorLockPatternView.handleActionUp():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.handleActionUp():void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: com.color.widget.ColorLockPatternView.initAlphaColor():void, dex:  in method: com.color.widget.ColorLockPatternView.initAlphaColor():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: jadx.core.utils.exceptions.DecodeException: null in method: com.color.widget.ColorLockPatternView.initAlphaColor():void, dex: 
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
        	... 5 more
        Caused by: java.io.EOFException
        	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
        	at com.android.dx.io.instructions.InstructionCodec.decodeRegisterRange(InstructionCodec.java:985)
        	at com.android.dx.io.instructions.InstructionCodec.access$1100(InstructionCodec.java:31)
        	at com.android.dx.io.instructions.InstructionCodec$28.decode(InstructionCodec.java:611)
        	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
        	... 6 more
        */
    private void initAlphaColor() {
        /*
        // Can't load method instructions: Load method exception: null in method: com.color.widget.ColorLockPatternView.initAlphaColor():void, dex:  in method: com.color.widget.ColorLockPatternView.initAlphaColor():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.initAlphaColor():void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.color.widget.ColorLockPatternView.initPaint():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
        	... 5 more
        */
    private void initPaint() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.color.widget.ColorLockPatternView.initPaint():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.initPaint():void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.color.widget.ColorLockPatternView.isOrientationPortrait(android.content.Context):boolean, dex: 
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
    private boolean isOrientationPortrait(android.content.Context r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.color.widget.ColorLockPatternView.isOrientationPortrait(android.content.Context):boolean, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.isOrientationPortrait(android.content.Context):boolean");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.color.widget.ColorLockPatternView.notifyCellAdded():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
        	... 5 more
        */
    private void notifyCellAdded() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.color.widget.ColorLockPatternView.notifyCellAdded():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.notifyCellAdded():void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.color.widget.ColorLockPatternView.notifyPatternCleared():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
        	... 5 more
        */
    private void notifyPatternCleared() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.color.widget.ColorLockPatternView.notifyPatternCleared():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.notifyPatternCleared():void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.color.widget.ColorLockPatternView.notifyPatternDetected():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
        	... 5 more
        */
    private void notifyPatternDetected() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.color.widget.ColorLockPatternView.notifyPatternDetected():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.notifyPatternDetected():void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.color.widget.ColorLockPatternView.notifyPatternStarted():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
        	... 5 more
        */
    private void notifyPatternStarted() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.color.widget.ColorLockPatternView.notifyPatternStarted():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.notifyPatternStarted():void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.color.widget.ColorLockPatternView.resetPattern():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
        	... 5 more
        */
    private void resetPattern() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.color.widget.ColorLockPatternView.resetPattern():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.resetPattern():void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.color.widget.ColorLockPatternView.sendAccessEvent(int):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
        	... 5 more
        */
    private void sendAccessEvent(int r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.color.widget.ColorLockPatternView.sendAccessEvent(int):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.sendAccessEvent(int):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00eb in method: com.color.widget.ColorLockPatternView.setPatternInProgress(boolean):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00eb
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    private void setPatternInProgress(boolean r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00eb in method: com.color.widget.ColorLockPatternView.setPatternInProgress(boolean):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.setPatternInProgress(boolean):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.color.widget.ColorLockPatternView.startWrongPathAnim(float, float):void, dex: 
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
    private void startWrongPathAnim(float r1, float r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.color.widget.ColorLockPatternView.startWrongPathAnim(float, float):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.startWrongPathAnim(float, float):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.color.widget.ColorLockPatternView.clearPattern():void, dex: 
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
    public void clearPattern() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.color.widget.ColorLockPatternView.clearPattern():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.clearPattern():void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.color.widget.ColorLockPatternView.clearPattern(boolean):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
        	... 5 more
        */
    public void clearPattern(boolean r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.color.widget.ColorLockPatternView.clearPattern(boolean):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.clearPattern(boolean):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00eb in method: com.color.widget.ColorLockPatternView.disableInput():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00eb
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public void disableInput() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00eb in method: com.color.widget.ColorLockPatternView.disableInput():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.disableInput():void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.color.widget.ColorLockPatternView.dispatchHoverEvent(android.view.MotionEvent):boolean, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
        	... 5 more
        */
    protected boolean dispatchHoverEvent(android.view.MotionEvent r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.color.widget.ColorLockPatternView.dispatchHoverEvent(android.view.MotionEvent):boolean, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.dispatchHoverEvent(android.view.MotionEvent):boolean");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00eb in method: com.color.widget.ColorLockPatternView.enableInput():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00eb
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public void enableInput() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00eb in method: com.color.widget.ColorLockPatternView.enableInput():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.enableInput():void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.color.widget.ColorLockPatternView.getFailAnimator():android.animation.Animator, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
        	... 5 more
        */
    public android.animation.Animator getFailAnimator() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.color.widget.ColorLockPatternView.getFailAnimator():android.animation.Animator, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.getFailAnimator():android.animation.Animator");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.color.widget.ColorLockPatternView.getSuccessAnimator():android.animation.Animator, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
        	... 5 more
        */
    public android.animation.Animator getSuccessAnimator() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.color.widget.ColorLockPatternView.getSuccessAnimator():android.animation.Animator, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.getSuccessAnimator():android.animation.Animator");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: com.color.widget.ColorLockPatternView.isInStealthMode():boolean, dex:  in method: com.color.widget.ColorLockPatternView.isInStealthMode():boolean, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: jadx.core.utils.exceptions.DecodeException: null in method: com.color.widget.ColorLockPatternView.isInStealthMode():boolean, dex: 
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
        	... 5 more
        Caused by: java.io.EOFException
        	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
        	at com.android.dx.io.instructions.ShortArrayCodeInput.readInt(ShortArrayCodeInput.java:61)
        	at com.android.dx.io.instructions.InstructionCodec$36.decode(InstructionCodec.java:827)
        	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
        	... 6 more
        */
    public boolean isInStealthMode() {
        /*
        // Can't load method instructions: Load method exception: null in method: com.color.widget.ColorLockPatternView.isInStealthMode():boolean, dex:  in method: com.color.widget.ColorLockPatternView.isInStealthMode():boolean, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.isInStealthMode():boolean");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00ef in method: com.color.widget.ColorLockPatternView.isSetLockPassword():boolean, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00ef
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public boolean isSetLockPassword() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00ef in method: com.color.widget.ColorLockPatternView.isSetLockPassword():boolean, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.isSetLockPassword():boolean");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00ef in method: com.color.widget.ColorLockPatternView.isTactileFeedbackEnabled():boolean, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00ef
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public boolean isTactileFeedbackEnabled() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00ef in method: com.color.widget.ColorLockPatternView.isTactileFeedbackEnabled():boolean, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.isTactileFeedbackEnabled():boolean");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus element_width: 0238 in method: com.color.widget.ColorLockPatternView.onDraw(android.graphics.Canvas):void, dex:  in method: com.color.widget.ColorLockPatternView.onDraw(android.graphics.Canvas):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: jadx.core.utils.exceptions.DecodeException: bogus element_width: 0238 in method: com.color.widget.ColorLockPatternView.onDraw(android.graphics.Canvas):void, dex: 
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
        	... 5 more
        Caused by: com.android.dex.DexException: bogus element_width: 0238
        	at com.android.dx.io.instructions.InstructionCodec$36.decode(InstructionCodec.java:871)
        	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
        	... 6 more
        */
    protected void onDraw(android.graphics.Canvas r1) {
        /*
        // Can't load method instructions: Load method exception: bogus element_width: 0238 in method: com.color.widget.ColorLockPatternView.onDraw(android.graphics.Canvas):void, dex:  in method: com.color.widget.ColorLockPatternView.onDraw(android.graphics.Canvas):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.onDraw(android.graphics.Canvas):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.color.widget.ColorLockPatternView.onHoverEvent(android.view.MotionEvent):boolean, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
        	... 5 more
        */
    public boolean onHoverEvent(android.view.MotionEvent r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.color.widget.ColorLockPatternView.onHoverEvent(android.view.MotionEvent):boolean, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.onHoverEvent(android.view.MotionEvent):boolean");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.color.widget.ColorLockPatternView.onMeasure(int, int):void, dex: 
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
    protected void onMeasure(int r1, int r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.color.widget.ColorLockPatternView.onMeasure(int, int):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.onMeasure(int, int):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus element_width: 10e9 in method: com.color.widget.ColorLockPatternView.onRestoreInstanceState(android.os.Parcelable):void, dex:  in method: com.color.widget.ColorLockPatternView.onRestoreInstanceState(android.os.Parcelable):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: jadx.core.utils.exceptions.DecodeException: bogus element_width: 10e9 in method: com.color.widget.ColorLockPatternView.onRestoreInstanceState(android.os.Parcelable):void, dex: 
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
        	... 5 more
        Caused by: com.android.dex.DexException: bogus element_width: 10e9
        	at com.android.dx.io.instructions.InstructionCodec$36.decode(InstructionCodec.java:871)
        	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
        	... 6 more
        */
    protected void onRestoreInstanceState(android.os.Parcelable r1) {
        /*
        // Can't load method instructions: Load method exception: bogus element_width: 10e9 in method: com.color.widget.ColorLockPatternView.onRestoreInstanceState(android.os.Parcelable):void, dex:  in method: com.color.widget.ColorLockPatternView.onRestoreInstanceState(android.os.Parcelable):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.onRestoreInstanceState(android.os.Parcelable):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus element_width: 86ef in method: com.color.widget.ColorLockPatternView.onSaveInstanceState():android.os.Parcelable, dex:  in method: com.color.widget.ColorLockPatternView.onSaveInstanceState():android.os.Parcelable, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: jadx.core.utils.exceptions.DecodeException: bogus element_width: 86ef in method: com.color.widget.ColorLockPatternView.onSaveInstanceState():android.os.Parcelable, dex: 
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
        	... 5 more
        Caused by: com.android.dex.DexException: bogus element_width: 86ef
        	at com.android.dx.io.instructions.InstructionCodec$36.decode(InstructionCodec.java:871)
        	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
        	... 6 more
        */
    protected android.os.Parcelable onSaveInstanceState() {
        /*
        // Can't load method instructions: Load method exception: bogus element_width: 86ef in method: com.color.widget.ColorLockPatternView.onSaveInstanceState():android.os.Parcelable, dex:  in method: com.color.widget.ColorLockPatternView.onSaveInstanceState():android.os.Parcelable, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.onSaveInstanceState():android.os.Parcelable");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: com.color.widget.ColorLockPatternView.onSizeChanged(int, int, int, int):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e3
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    protected void onSizeChanged(int r1, int r2, int r3, int r4) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: com.color.widget.ColorLockPatternView.onSizeChanged(int, int, int, int):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.onSizeChanged(int, int, int, int):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00ef in method: com.color.widget.ColorLockPatternView.onTouchEvent(android.view.MotionEvent):boolean, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00ef
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public boolean onTouchEvent(android.view.MotionEvent r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00ef in method: com.color.widget.ColorLockPatternView.onTouchEvent(android.view.MotionEvent):boolean, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.onTouchEvent(android.view.MotionEvent):boolean");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.color.widget.ColorLockPatternView.setAlphaAnimation(android.animation.ValueAnimator, int, int):void, dex: 
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
    public void setAlphaAnimation(android.animation.ValueAnimator r1, int r2, int r3) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.color.widget.ColorLockPatternView.setAlphaAnimation(android.animation.ValueAnimator, int, int):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.setAlphaAnimation(android.animation.ValueAnimator, int, int):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.color.widget.ColorLockPatternView.setBitmapNotDraw():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
        	... 5 more
        */
    public void setBitmapNotDraw() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.color.widget.ColorLockPatternView.setBitmapNotDraw():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.setBitmapNotDraw():void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.color.widget.ColorLockPatternView.setDisplayMode(com.color.widget.ColorLockPatternView$DisplayMode):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
        	... 5 more
        */
    public void setDisplayMode(com.color.widget.ColorLockPatternView.DisplayMode r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.color.widget.ColorLockPatternView.setDisplayMode(com.color.widget.ColorLockPatternView$DisplayMode):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.setDisplayMode(com.color.widget.ColorLockPatternView$DisplayMode):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: com.color.widget.ColorLockPatternView.setInStealthMode(boolean):void, dex:  in method: com.color.widget.ColorLockPatternView.setInStealthMode(boolean):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: jadx.core.utils.exceptions.DecodeException: null in method: com.color.widget.ColorLockPatternView.setInStealthMode(boolean):void, dex: 
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
        	... 5 more
        Caused by: java.io.EOFException
        	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
        	at com.android.dx.io.instructions.ShortArrayCodeInput.readInt(ShortArrayCodeInput.java:61)
        	at com.android.dx.io.instructions.InstructionCodec$36.decode(InstructionCodec.java:827)
        	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
        	... 6 more
        */
    public void setInStealthMode(boolean r1) {
        /*
        // Can't load method instructions: Load method exception: null in method: com.color.widget.ColorLockPatternView.setInStealthMode(boolean):void, dex:  in method: com.color.widget.ColorLockPatternView.setInStealthMode(boolean):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.setInStealthMode(boolean):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00eb in method: com.color.widget.ColorLockPatternView.setLockPassword(boolean):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00eb
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public void setLockPassword(boolean r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00eb in method: com.color.widget.ColorLockPatternView.setLockPassword(boolean):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.setLockPassword(boolean):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.color.widget.ColorLockPatternView.setOnPatternListener(com.color.widget.ColorLockPatternView$OnPatternListener):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
        	... 5 more
        */
    public void setOnPatternListener(com.color.widget.ColorLockPatternView.OnPatternListener r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.color.widget.ColorLockPatternView.setOnPatternListener(com.color.widget.ColorLockPatternView$OnPatternListener):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.setOnPatternListener(com.color.widget.ColorLockPatternView$OnPatternListener):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.color.widget.ColorLockPatternView.setPattern(com.color.widget.ColorLockPatternView$DisplayMode, java.util.List):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
        	... 5 more
        */
    public void setPattern(com.color.widget.ColorLockPatternView.DisplayMode r1, java.util.List<com.color.widget.ColorLockPatternView.Cell> r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.color.widget.ColorLockPatternView.setPattern(com.color.widget.ColorLockPatternView$DisplayMode, java.util.List):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.setPattern(com.color.widget.ColorLockPatternView$DisplayMode, java.util.List):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.color.widget.ColorLockPatternView.setSuccessFinger():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
        	... 5 more
        */
    public void setSuccessFinger() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.color.widget.ColorLockPatternView.setSuccessFinger():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.setSuccessFinger():void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00eb in method: com.color.widget.ColorLockPatternView.setTactileFeedbackEnabled(boolean):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00eb
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public void setTactileFeedbackEnabled(boolean r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00eb in method: com.color.widget.ColorLockPatternView.setTactileFeedbackEnabled(boolean):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.setTactileFeedbackEnabled(boolean):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.color.widget.ColorLockPatternView.setTranslateAnimation(android.animation.ValueAnimator, int, int):void, dex: 
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
    public void setTranslateAnimation(android.animation.ValueAnimator r1, int r2, int r3) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.color.widget.ColorLockPatternView.setTranslateAnimation(android.animation.ValueAnimator, int, int):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.setTranslateAnimation(android.animation.ValueAnimator, int, int):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: com.color.widget.ColorLockPatternView.update(float, com.color.widget.ColorLockPatternView$Cell):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e3
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public void update(float r1, com.color.widget.ColorLockPatternView.Cell r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: com.color.widget.ColorLockPatternView.update(float, com.color.widget.ColorLockPatternView$Cell):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorLockPatternView.update(float, com.color.widget.ColorLockPatternView$Cell):void");
    }

    public ColorLockPatternView(Context context) {
        this(context, null);
    }

    public ColorLockPatternView(Context context, AttributeSet attrs) {
        this(context, attrs, 201393223);
    }

    private int resolveMeasured(int measureSpec, int desired) {
        int specSize = MeasureSpec.getSize(measureSpec);
        switch (MeasureSpec.getMode(measureSpec)) {
            case Integer.MIN_VALUE:
                return Math.max(specSize, desired);
            case 0:
                return desired;
            default:
                return specSize;
        }
    }

    public boolean isLongshotUnsupported() {
        return true;
    }
}
