package android.graphics.drawable;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.graphics.Canvas;
import android.graphics.CanvasProperty;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.RippleComponent.RenderNodeAnimatorSet;
import android.hardware.camera2.params.TonemapCurve;
import android.util.FloatProperty;
import android.util.MathUtils;
import android.util.Property;
import android.view.DisplayListCanvas;
import android.view.RenderNodeAnimator;

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
class RippleForeground extends RippleComponent {
    private static final int BOUNDED_OPACITY_EXIT_DURATION = 400;
    private static final int BOUNDED_ORIGIN_EXIT_DURATION = 300;
    private static final int BOUNDED_RADIUS_EXIT_DURATION = 800;
    private static final TimeInterpolator DECELERATE_INTERPOLATOR = null;
    private static final TimeInterpolator LINEAR_INTERPOLATOR = null;
    private static final float MAX_BOUNDED_RADIUS = 350.0f;
    private static final FloatProperty<RippleForeground> OPACITY = null;
    private static final int OPACITY_ENTER_DURATION_FAST = 120;
    private static final int RIPPLE_ENTER_DELAY = 80;
    private static final FloatProperty<RippleForeground> TWEEN_ORIGIN = null;
    private static final FloatProperty<RippleForeground> TWEEN_RADIUS = null;
    private static final float WAVE_OPACITY_DECAY_VELOCITY = 3.0f;
    private static final float WAVE_TOUCH_DOWN_ACCELERATION = 1024.0f;
    private static final float WAVE_TOUCH_UP_ACCELERATION = 3400.0f;
    private final AnimatorListenerAdapter mAnimationListener;
    private float mBoundedRadius;
    private float mClampedStartingX;
    private float mClampedStartingY;
    private boolean mHasFinishedExit;
    private boolean mIsBounded;
    private float mOpacity;
    private CanvasProperty<Paint> mPropPaint;
    private CanvasProperty<Float> mPropRadius;
    private CanvasProperty<Float> mPropX;
    private CanvasProperty<Float> mPropY;
    private float mStartingX;
    private float mStartingY;
    private float mTargetX;
    private float mTargetY;
    private float mTweenRadius;
    private float mTweenX;
    private float mTweenY;

    /* renamed from: android.graphics.drawable.RippleForeground$2 */
    static class AnonymousClass2 extends FloatProperty<RippleForeground> {
        AnonymousClass2(String $anonymous0) {
            super($anonymous0);
        }

        public void setValue(RippleForeground object, float value) {
            object.mTweenRadius = value;
            object.invalidateSelf();
        }

        public Float get(RippleForeground object) {
            return Float.valueOf(object.mTweenRadius);
        }
    }

    /* renamed from: android.graphics.drawable.RippleForeground$3 */
    static class AnonymousClass3 extends FloatProperty<RippleForeground> {
        AnonymousClass3(String $anonymous0) {
            super($anonymous0);
        }

        public void setValue(RippleForeground object, float value) {
            object.mTweenX = value;
            object.mTweenY = value;
            object.invalidateSelf();
        }

        public Float get(RippleForeground object) {
            return Float.valueOf(object.mTweenX);
        }
    }

    /* renamed from: android.graphics.drawable.RippleForeground$4 */
    static class AnonymousClass4 extends FloatProperty<RippleForeground> {
        AnonymousClass4(String $anonymous0) {
            super($anonymous0);
        }

        public void setValue(RippleForeground object, float value) {
            object.mOpacity = value;
            object.invalidateSelf();
        }

        public Float get(RippleForeground object) {
            return Float.valueOf(object.mOpacity);
        }
    }

    private static final class LogDecelerateInterpolator implements TimeInterpolator {
        private final float mBase;
        private final float mDrift;
        private final float mOutputScale = (1.0f / computeLog(1.0f));
        private final float mTimeScale;

        public LogDecelerateInterpolator(float base, float timeScale, float drift) {
            this.mBase = base;
            this.mDrift = drift;
            this.mTimeScale = 1.0f / timeScale;
        }

        private float computeLog(float t) {
            return (1.0f - ((float) Math.pow((double) this.mBase, (double) ((-t) * this.mTimeScale)))) + (this.mDrift * t);
        }

        public float getInterpolation(float t) {
            return computeLog(t) * this.mOutputScale;
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.graphics.drawable.RippleForeground.<clinit>():void, dex: 
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
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 9 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.graphics.drawable.RippleForeground.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.graphics.drawable.RippleForeground.<clinit>():void");
    }

    public RippleForeground(RippleDrawable owner, Rect bounds, float startingX, float startingY, boolean isBounded, boolean forceSoftware) {
        super(owner, bounds, forceSoftware);
        this.mTargetX = TonemapCurve.LEVEL_BLACK;
        this.mTargetY = TonemapCurve.LEVEL_BLACK;
        this.mBoundedRadius = TonemapCurve.LEVEL_BLACK;
        this.mOpacity = 1.0f;
        this.mTweenRadius = TonemapCurve.LEVEL_BLACK;
        this.mTweenX = TonemapCurve.LEVEL_BLACK;
        this.mTweenY = TonemapCurve.LEVEL_BLACK;
        this.mAnimationListener = new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animator) {
                RippleForeground.this.mHasFinishedExit = true;
            }
        };
        this.mIsBounded = isBounded;
        this.mStartingX = startingX;
        this.mStartingY = startingY;
        if (isBounded) {
            this.mBoundedRadius = ((float) ((Math.random() * 350.0d) * 0.1d)) + 315.0f;
        } else {
            this.mBoundedRadius = TonemapCurve.LEVEL_BLACK;
        }
    }

    protected void onTargetRadiusChanged(float targetRadius) {
        clampStartingPosition();
    }

    protected boolean drawSoftware(Canvas c, Paint p) {
        int origAlpha = p.getAlpha();
        int alpha = (int) ((((float) origAlpha) * this.mOpacity) + 0.5f);
        float radius = getCurrentRadius();
        if (alpha <= 0 || radius <= TonemapCurve.LEVEL_BLACK) {
            return false;
        }
        float x = getCurrentX();
        float y = getCurrentY();
        p.setAlpha(alpha);
        c.drawCircle(x, y, radius, p);
        p.setAlpha(origAlpha);
        return true;
    }

    protected boolean drawHardware(DisplayListCanvas c) {
        c.drawCircle(this.mPropX, this.mPropY, this.mPropRadius, this.mPropPaint);
        return true;
    }

    public void getBounds(Rect bounds) {
        int outerX = (int) this.mTargetX;
        int outerY = (int) this.mTargetY;
        int r = ((int) this.mTargetRadius) + 1;
        bounds.set(outerX - r, outerY - r, outerX + r, outerY + r);
    }

    public void move(float x, float y) {
        this.mStartingX = x;
        this.mStartingY = y;
        clampStartingPosition();
    }

    public boolean hasFinishedExit() {
        return this.mHasFinishedExit;
    }

    protected Animator createSoftwareEnter(boolean fast) {
        if (this.mIsBounded) {
            return null;
        }
        int duration = (int) ((Math.sqrt((double) ((this.mTargetRadius / WAVE_TOUCH_DOWN_ACCELERATION) * this.mDensityScale)) * 1000.0d) + 0.5d);
        Property property = TWEEN_RADIUS;
        float[] fArr = new float[1];
        fArr[0] = 1.0f;
        ObjectAnimator tweenRadius = ObjectAnimator.ofFloat((Object) this, property, fArr);
        tweenRadius.setAutoCancel(true);
        tweenRadius.setDuration((long) duration);
        tweenRadius.setInterpolator(LINEAR_INTERPOLATOR);
        tweenRadius.setStartDelay(80);
        property = TWEEN_ORIGIN;
        fArr = new float[1];
        fArr[0] = 1.0f;
        ObjectAnimator tweenOrigin = ObjectAnimator.ofFloat((Object) this, property, fArr);
        tweenOrigin.setAutoCancel(true);
        tweenOrigin.setDuration((long) duration);
        tweenOrigin.setInterpolator(LINEAR_INTERPOLATOR);
        tweenOrigin.setStartDelay(80);
        property = OPACITY;
        fArr = new float[1];
        fArr[0] = 1.0f;
        ObjectAnimator opacity = ObjectAnimator.ofFloat((Object) this, property, fArr);
        opacity.setAutoCancel(true);
        opacity.setDuration(120);
        opacity.setInterpolator(LINEAR_INTERPOLATOR);
        AnimatorSet set = new AnimatorSet();
        set.play(tweenOrigin).with(tweenRadius).with(opacity);
        return set;
    }

    private float getCurrentX() {
        return MathUtils.lerp(this.mClampedStartingX - this.mBounds.exactCenterX(), this.mTargetX, this.mTweenX);
    }

    private float getCurrentY() {
        return MathUtils.lerp(this.mClampedStartingY - this.mBounds.exactCenterY(), this.mTargetY, this.mTweenY);
    }

    private int getRadiusExitDuration() {
        return (int) ((Math.sqrt((double) (((this.mTargetRadius - getCurrentRadius()) / 4424.0f) * this.mDensityScale)) * 1000.0d) + 0.5d);
    }

    private float getCurrentRadius() {
        return MathUtils.lerp(TonemapCurve.LEVEL_BLACK, this.mTargetRadius, this.mTweenRadius);
    }

    private int getOpacityExitDuration() {
        return (int) (((this.mOpacity * 1000.0f) / WAVE_OPACITY_DECAY_VELOCITY) + 0.5f);
    }

    private void computeBoundedTargetValues() {
        this.mTargetX = (this.mClampedStartingX - this.mBounds.exactCenterX()) * 0.7f;
        this.mTargetY = (this.mClampedStartingY - this.mBounds.exactCenterY()) * 0.7f;
        this.mTargetRadius = this.mBoundedRadius;
    }

    protected Animator createSoftwareExit() {
        int radiusDuration;
        int originDuration;
        int opacityDuration;
        if (this.mIsBounded) {
            computeBoundedTargetValues();
            radiusDuration = 800;
            originDuration = 300;
            opacityDuration = 400;
        } else {
            radiusDuration = getRadiusExitDuration();
            originDuration = radiusDuration;
            opacityDuration = getOpacityExitDuration();
        }
        Property property = TWEEN_RADIUS;
        float[] fArr = new float[1];
        fArr[0] = 1.0f;
        ObjectAnimator tweenRadius = ObjectAnimator.ofFloat((Object) this, property, fArr);
        tweenRadius.setAutoCancel(true);
        tweenRadius.setDuration((long) radiusDuration);
        tweenRadius.setInterpolator(DECELERATE_INTERPOLATOR);
        property = TWEEN_ORIGIN;
        fArr = new float[1];
        fArr[0] = 1.0f;
        ObjectAnimator tweenOrigin = ObjectAnimator.ofFloat((Object) this, property, fArr);
        tweenOrigin.setAutoCancel(true);
        tweenOrigin.setDuration((long) originDuration);
        tweenOrigin.setInterpolator(DECELERATE_INTERPOLATOR);
        property = OPACITY;
        fArr = new float[1];
        fArr[0] = TonemapCurve.LEVEL_BLACK;
        ObjectAnimator opacity = ObjectAnimator.ofFloat((Object) this, property, fArr);
        opacity.setAutoCancel(true);
        opacity.setDuration((long) opacityDuration);
        opacity.setInterpolator(LINEAR_INTERPOLATOR);
        AnimatorSet set = new AnimatorSet();
        set.play(tweenOrigin).with(tweenRadius).with(opacity);
        set.addListener(this.mAnimationListener);
        return set;
    }

    protected RenderNodeAnimatorSet createHardwareExit(Paint p) {
        int radiusDuration;
        int originDuration;
        int opacityDuration;
        if (this.mIsBounded) {
            computeBoundedTargetValues();
            radiusDuration = 800;
            originDuration = 300;
            opacityDuration = 400;
        } else {
            radiusDuration = getRadiusExitDuration();
            originDuration = radiusDuration;
            opacityDuration = getOpacityExitDuration();
        }
        float startX = getCurrentX();
        float startY = getCurrentY();
        float startRadius = getCurrentRadius();
        p.setAlpha((int) ((((float) p.getAlpha()) * this.mOpacity) + 0.5f));
        this.mPropPaint = CanvasProperty.createPaint(p);
        this.mPropRadius = CanvasProperty.createFloat(startRadius);
        this.mPropX = CanvasProperty.createFloat(startX);
        this.mPropY = CanvasProperty.createFloat(startY);
        RenderNodeAnimator radius = new RenderNodeAnimator(this.mPropRadius, this.mTargetRadius);
        radius.setDuration((long) radiusDuration);
        radius.setInterpolator(DECELERATE_INTERPOLATOR);
        RenderNodeAnimator x = new RenderNodeAnimator(this.mPropX, this.mTargetX);
        x.setDuration((long) originDuration);
        x.setInterpolator(DECELERATE_INTERPOLATOR);
        RenderNodeAnimator y = new RenderNodeAnimator(this.mPropY, this.mTargetY);
        y.setDuration((long) originDuration);
        y.setInterpolator(DECELERATE_INTERPOLATOR);
        RenderNodeAnimator opacity = new RenderNodeAnimator(this.mPropPaint, 1, TonemapCurve.LEVEL_BLACK);
        opacity.setDuration((long) opacityDuration);
        opacity.setInterpolator(LINEAR_INTERPOLATOR);
        opacity.addListener(this.mAnimationListener);
        RenderNodeAnimatorSet set = new RenderNodeAnimatorSet();
        set.add(radius);
        set.add(opacity);
        set.add(x);
        set.add(y);
        return set;
    }

    protected void jumpValuesToExit() {
        this.mOpacity = TonemapCurve.LEVEL_BLACK;
        this.mTweenX = 1.0f;
        this.mTweenY = 1.0f;
        this.mTweenRadius = 1.0f;
    }

    private void clampStartingPosition() {
        float cX = this.mBounds.exactCenterX();
        float cY = this.mBounds.exactCenterY();
        float dX = this.mStartingX - cX;
        float dY = this.mStartingY - cY;
        float r = this.mTargetRadius;
        if ((dX * dX) + (dY * dY) > r * r) {
            double angle = Math.atan2((double) dY, (double) dX);
            this.mClampedStartingX = ((float) (Math.cos(angle) * ((double) r))) + cX;
            this.mClampedStartingY = ((float) (Math.sin(angle) * ((double) r))) + cY;
            return;
        }
        this.mClampedStartingX = this.mStartingX;
        this.mClampedStartingY = this.mStartingY;
    }
}
