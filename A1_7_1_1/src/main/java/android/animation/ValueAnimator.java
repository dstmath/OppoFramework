package android.animation;

import android.animation.Animator.AnimatorListener;
import android.hardware.camera2.params.TonemapCurve;
import android.os.Looper;
import android.os.Trace;
import android.util.AndroidRuntimeException;
import android.util.Log;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import java.util.ArrayList;
import java.util.HashMap;

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
public class ValueAnimator extends Animator implements AnimationFrameCallback {
    private static final boolean DEBUG = false;
    public static final int INFINITE = -1;
    public static final int RESTART = 1;
    public static final int REVERSE = 2;
    private static final String TAG = "ValueAnimator";
    private static final TimeInterpolator sDefaultInterpolator = null;
    private static float sDurationScale;
    private boolean mAnimationEndRequested;
    private float mCurrentFraction;
    private long mDuration;
    boolean mInitialized;
    private TimeInterpolator mInterpolator;
    private long mLastFrameTime;
    private float mOverallFraction;
    private long mPauseTime;
    private int mRepeatCount;
    private int mRepeatMode;
    private boolean mResumed;
    private boolean mReversing;
    private boolean mRunning;
    float mSeekFraction;
    private long mStartDelay;
    private boolean mStartListenersCalled;
    long mStartTime;
    boolean mStartTimeCommitted;
    private boolean mStarted;
    ArrayList<AnimatorUpdateListener> mUpdateListeners;
    PropertyValuesHolder[] mValues;
    HashMap<String, PropertyValuesHolder> mValuesMap;

    public interface AnimatorUpdateListener {
        void onAnimationUpdate(ValueAnimator valueAnimator);
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.animation.ValueAnimator.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.animation.ValueAnimator.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.animation.ValueAnimator.<clinit>():void");
    }

    public static void setDurationScale(float durationScale) {
        sDurationScale = durationScale;
    }

    public static float getDurationScale() {
        return sDurationScale;
    }

    public ValueAnimator() {
        this.mSeekFraction = -1.0f;
        this.mResumed = false;
        this.mOverallFraction = TonemapCurve.LEVEL_BLACK;
        this.mCurrentFraction = TonemapCurve.LEVEL_BLACK;
        this.mLastFrameTime = 0;
        this.mRunning = false;
        this.mStarted = false;
        this.mStartListenersCalled = false;
        this.mInitialized = false;
        this.mAnimationEndRequested = false;
        this.mDuration = 300;
        this.mStartDelay = 0;
        this.mRepeatCount = 0;
        this.mRepeatMode = 1;
        this.mInterpolator = sDefaultInterpolator;
        this.mUpdateListeners = null;
    }

    public static ValueAnimator ofInt(int... values) {
        ValueAnimator anim = new ValueAnimator();
        anim.setIntValues(values);
        return anim;
    }

    public static ValueAnimator ofArgb(int... values) {
        ValueAnimator anim = new ValueAnimator();
        anim.setIntValues(values);
        anim.setEvaluator(ArgbEvaluator.getInstance());
        return anim;
    }

    public static ValueAnimator ofFloat(float... values) {
        ValueAnimator anim = new ValueAnimator();
        anim.setFloatValues(values);
        return anim;
    }

    public static ValueAnimator ofPropertyValuesHolder(PropertyValuesHolder... values) {
        ValueAnimator anim = new ValueAnimator();
        anim.setValues(values);
        return anim;
    }

    public static ValueAnimator ofObject(TypeEvaluator evaluator, Object... values) {
        ValueAnimator anim = new ValueAnimator();
        anim.setObjectValues(values);
        anim.setEvaluator(evaluator);
        return anim;
    }

    public void setIntValues(int... values) {
        if (values != null && values.length != 0) {
            if (this.mValues == null || this.mValues.length == 0) {
                PropertyValuesHolder[] propertyValuesHolderArr = new PropertyValuesHolder[1];
                propertyValuesHolderArr[0] = PropertyValuesHolder.ofInt("", values);
                setValues(propertyValuesHolderArr);
            } else {
                this.mValues[0].setIntValues(values);
            }
            this.mInitialized = false;
        }
    }

    public void setFloatValues(float... values) {
        if (values != null && values.length != 0) {
            if (this.mValues == null || this.mValues.length == 0) {
                PropertyValuesHolder[] propertyValuesHolderArr = new PropertyValuesHolder[1];
                propertyValuesHolderArr[0] = PropertyValuesHolder.ofFloat("", values);
                setValues(propertyValuesHolderArr);
            } else {
                this.mValues[0].setFloatValues(values);
            }
            this.mInitialized = false;
        }
    }

    public void setObjectValues(Object... values) {
        if (values != null && values.length != 0) {
            if (this.mValues == null || this.mValues.length == 0) {
                PropertyValuesHolder[] propertyValuesHolderArr = new PropertyValuesHolder[1];
                propertyValuesHolderArr[0] = PropertyValuesHolder.ofObject("", null, values);
                setValues(propertyValuesHolderArr);
            } else {
                this.mValues[0].setObjectValues(values);
            }
            this.mInitialized = false;
        }
    }

    public void setValues(PropertyValuesHolder... values) {
        this.mValues = values;
        this.mValuesMap = new HashMap(numValues);
        for (PropertyValuesHolder valuesHolder : values) {
            this.mValuesMap.put(valuesHolder.getPropertyName(), valuesHolder);
        }
        this.mInitialized = false;
    }

    public PropertyValuesHolder[] getValues() {
        return this.mValues;
    }

    void initAnimation() {
        if (!this.mInitialized) {
            for (PropertyValuesHolder init : this.mValues) {
                init.init();
            }
            this.mInitialized = true;
        }
    }

    public ValueAnimator setDuration(long duration) {
        if (duration < 0) {
            throw new IllegalArgumentException("Animators cannot have negative duration: " + duration);
        }
        this.mDuration = duration;
        return this;
    }

    private long getScaledDuration() {
        return (long) (((float) this.mDuration) * sDurationScale);
    }

    public long getDuration() {
        return this.mDuration;
    }

    public long getTotalDuration() {
        if (this.mRepeatCount == -1) {
            return -1;
        }
        return this.mStartDelay + (this.mDuration * ((long) (this.mRepeatCount + 1)));
    }

    public void setCurrentPlayTime(long playTime) {
        setCurrentFraction(this.mDuration > 0 ? ((float) playTime) / ((float) this.mDuration) : 1.0f);
    }

    public void setCurrentFraction(float fraction) {
        initAnimation();
        fraction = clampFraction(fraction);
        this.mStartTime = AnimationUtils.currentAnimationTimeMillis() - ((long) (((float) getScaledDuration()) * fraction));
        this.mStartTimeCommitted = true;
        if (!isPulsingInternal()) {
            this.mSeekFraction = fraction;
        }
        this.mOverallFraction = fraction;
        animateValue(getCurrentIterationFraction(fraction));
    }

    private int getCurrentIteration(float fraction) {
        fraction = clampFraction(fraction);
        double iteration = Math.floor((double) fraction);
        if (((double) fraction) == iteration && fraction > TonemapCurve.LEVEL_BLACK) {
            iteration -= 1.0d;
        }
        return (int) iteration;
    }

    private float getCurrentIterationFraction(float fraction) {
        fraction = clampFraction(fraction);
        int iteration = getCurrentIteration(fraction);
        float currentFraction = fraction - ((float) iteration);
        return shouldPlayBackward(iteration) ? 1.0f - currentFraction : currentFraction;
    }

    private float clampFraction(float fraction) {
        if (fraction < TonemapCurve.LEVEL_BLACK) {
            return TonemapCurve.LEVEL_BLACK;
        }
        if (this.mRepeatCount != -1) {
            return Math.min(fraction, (float) (this.mRepeatCount + 1));
        }
        return fraction;
    }

    private boolean shouldPlayBackward(int iteration) {
        boolean z = true;
        if (iteration <= 0 || this.mRepeatMode != 2 || (iteration >= this.mRepeatCount + 1 && this.mRepeatCount != -1)) {
            return this.mReversing;
        }
        if (this.mReversing) {
            if (iteration % 2 != 0) {
                z = false;
            }
            return z;
        }
        if (iteration % 2 == 0) {
            z = false;
        }
        return z;
    }

    public long getCurrentPlayTime() {
        if (!this.mInitialized || (!this.mStarted && this.mSeekFraction < TonemapCurve.LEVEL_BLACK)) {
            return 0;
        }
        if (this.mSeekFraction >= TonemapCurve.LEVEL_BLACK) {
            return (long) (((float) this.mDuration) * this.mSeekFraction);
        }
        return (long) (((float) (AnimationUtils.currentAnimationTimeMillis() - this.mStartTime)) / (sDurationScale == TonemapCurve.LEVEL_BLACK ? 1.0f : sDurationScale));
    }

    public long getStartDelay() {
        return this.mStartDelay;
    }

    public void setStartDelay(long startDelay) {
        if (startDelay < 0) {
            Log.w(TAG, "Start delay should always be non-negative");
            startDelay = 0;
        }
        this.mStartDelay = startDelay;
    }

    public static long getFrameDelay() {
        AnimationHandler.getInstance();
        return AnimationHandler.getFrameDelay();
    }

    public static void setFrameDelay(long frameDelay) {
        AnimationHandler.getInstance();
        AnimationHandler.setFrameDelay(frameDelay);
    }

    public Object getAnimatedValue() {
        if (this.mValues == null || this.mValues.length <= 0) {
            return null;
        }
        return this.mValues[0].getAnimatedValue();
    }

    public Object getAnimatedValue(String propertyName) {
        PropertyValuesHolder valuesHolder = (PropertyValuesHolder) this.mValuesMap.get(propertyName);
        if (valuesHolder != null) {
            return valuesHolder.getAnimatedValue();
        }
        return null;
    }

    public void setRepeatCount(int value) {
        this.mRepeatCount = value;
    }

    public int getRepeatCount() {
        return this.mRepeatCount;
    }

    public void setRepeatMode(int value) {
        this.mRepeatMode = value;
    }

    public int getRepeatMode() {
        return this.mRepeatMode;
    }

    public void addUpdateListener(AnimatorUpdateListener listener) {
        if (this.mUpdateListeners == null) {
            this.mUpdateListeners = new ArrayList();
        }
        this.mUpdateListeners.add(listener);
    }

    public void removeAllUpdateListeners() {
        if (this.mUpdateListeners != null) {
            this.mUpdateListeners.clear();
            this.mUpdateListeners = null;
        }
    }

    public void removeUpdateListener(AnimatorUpdateListener listener) {
        if (this.mUpdateListeners != null) {
            this.mUpdateListeners.remove(listener);
            if (this.mUpdateListeners.size() == 0) {
                this.mUpdateListeners = null;
            }
        }
    }

    public void setInterpolator(TimeInterpolator value) {
        if (value != null) {
            this.mInterpolator = value;
        } else {
            this.mInterpolator = new LinearInterpolator();
        }
    }

    public TimeInterpolator getInterpolator() {
        return this.mInterpolator;
    }

    public void setEvaluator(TypeEvaluator value) {
        if (value != null && this.mValues != null && this.mValues.length > 0) {
            this.mValues[0].setEvaluator(value);
        }
    }

    private void notifyStartListeners() {
        if (!(this.mListeners == null || this.mStartListenersCalled)) {
            ArrayList<AnimatorListener> tmpListeners = (ArrayList) this.mListeners.clone();
            int numListeners = tmpListeners.size();
            for (int i = 0; i < numListeners; i++) {
                ((AnimatorListener) tmpListeners.get(i)).onAnimationStart(this);
            }
        }
        this.mStartListenersCalled = true;
    }

    private void start(boolean playBackwards) {
        if (Looper.myLooper() == null) {
            throw new AndroidRuntimeException("Animators may only be run on Looper threads");
        }
        this.mReversing = playBackwards;
        if (!(!playBackwards || this.mSeekFraction == -1.0f || this.mSeekFraction == TonemapCurve.LEVEL_BLACK)) {
            if (this.mRepeatCount == -1) {
                this.mSeekFraction = 1.0f - ((float) (((double) this.mSeekFraction) - Math.floor((double) this.mSeekFraction)));
            } else {
                this.mSeekFraction = ((float) (this.mRepeatCount + 1)) - this.mSeekFraction;
            }
        }
        this.mStarted = true;
        this.mPaused = false;
        this.mRunning = false;
        this.mAnimationEndRequested = false;
        this.mLastFrameTime = 0;
        AnimationHandler.getInstance().addAnimationFrameCallback(this, (long) (((float) this.mStartDelay) * sDurationScale));
        if (this.mStartDelay == 0 || this.mSeekFraction >= TonemapCurve.LEVEL_BLACK) {
            startAnimation();
            if (this.mSeekFraction == -1.0f) {
                setCurrentPlayTime(0);
            } else {
                setCurrentFraction(this.mSeekFraction);
            }
        }
    }

    public void start() {
        start(false);
    }

    public void cancel() {
        if (Looper.myLooper() == null) {
            throw new AndroidRuntimeException("Animators may only be run on Looper threads");
        } else if (!this.mAnimationEndRequested) {
            if ((this.mStarted || this.mRunning) && this.mListeners != null) {
                if (!this.mRunning) {
                    notifyStartListeners();
                }
                for (AnimatorListener listener : (ArrayList) this.mListeners.clone()) {
                    listener.onAnimationCancel(this);
                }
            }
            endAnimation();
        }
    }

    public void end() {
        if (Looper.myLooper() == null) {
            throw new AndroidRuntimeException("Animators may only be run on Looper threads");
        }
        if (!this.mRunning) {
            startAnimation();
            this.mStarted = true;
        } else if (!this.mInitialized) {
            initAnimation();
        }
        animateValue(shouldPlayBackward(this.mRepeatCount) ? TonemapCurve.LEVEL_BLACK : 1.0f);
        endAnimation();
    }

    public void resume() {
        if (Looper.myLooper() == null) {
            throw new AndroidRuntimeException("Animators may only be resumed from the same thread that the animator was started on");
        }
        if (this.mPaused && !this.mResumed) {
            this.mResumed = true;
            if (this.mPauseTime > 0) {
                AnimationHandler.getInstance().addAnimationFrameCallback(this, 0);
            }
        }
        super.resume();
    }

    public void pause() {
        boolean previouslyPaused = this.mPaused;
        super.pause();
        if (!previouslyPaused && this.mPaused) {
            this.mPauseTime = -1;
            this.mResumed = false;
        }
    }

    public boolean isRunning() {
        return this.mRunning;
    }

    public boolean isStarted() {
        return this.mStarted;
    }

    public void reverse() {
        boolean z = false;
        if (isPulsingInternal()) {
            long currentTime = AnimationUtils.currentAnimationTimeMillis();
            this.mStartTime = currentTime - (getScaledDuration() - (currentTime - this.mStartTime));
            this.mStartTimeCommitted = true;
            if (!this.mReversing) {
                z = true;
            }
            this.mReversing = z;
        } else if (this.mStarted) {
            if (!this.mReversing) {
                z = true;
            }
            this.mReversing = z;
            end();
        } else {
            start(true);
        }
    }

    public boolean canReverse() {
        return true;
    }

    private void endAnimation() {
        if (!this.mAnimationEndRequested) {
            AnimationHandler.getInstance().removeCallback(this);
            this.mAnimationEndRequested = true;
            this.mPaused = false;
            if ((this.mStarted || this.mRunning) && this.mListeners != null) {
                if (!this.mRunning) {
                    notifyStartListeners();
                }
                ArrayList<AnimatorListener> tmpListeners = (ArrayList) this.mListeners.clone();
                int numListeners = tmpListeners.size();
                for (int i = 0; i < numListeners; i++) {
                    ((AnimatorListener) tmpListeners.get(i)).onAnimationEnd(this);
                }
            }
            this.mRunning = false;
            this.mStarted = false;
            this.mStartListenersCalled = false;
            this.mReversing = false;
            this.mLastFrameTime = 0;
            if (Trace.isTagEnabled(8)) {
                Trace.asyncTraceEnd(8, getNameForTrace(), System.identityHashCode(this));
            }
        }
    }

    private void startAnimation() {
        if (Trace.isTagEnabled(8)) {
            Trace.asyncTraceBegin(8, getNameForTrace(), System.identityHashCode(this));
        }
        this.mAnimationEndRequested = false;
        initAnimation();
        this.mRunning = true;
        if (this.mSeekFraction >= TonemapCurve.LEVEL_BLACK) {
            this.mOverallFraction = this.mSeekFraction;
        } else {
            this.mOverallFraction = TonemapCurve.LEVEL_BLACK;
        }
        if (this.mListeners != null) {
            notifyStartListeners();
        }
    }

    private boolean isPulsingInternal() {
        return this.mLastFrameTime > 0;
    }

    String getNameForTrace() {
        return "animator";
    }

    public void commitAnimationFrame(long frameTime) {
        if (!this.mStartTimeCommitted) {
            this.mStartTimeCommitted = true;
            long adjustment = frameTime - this.mLastFrameTime;
            if (adjustment > 0) {
                this.mStartTime += adjustment;
            }
        }
    }

    boolean animateBasedOnTime(long currentTime) {
        boolean done = false;
        if (this.mRunning) {
            long scaledDuration = getScaledDuration();
            float fraction = scaledDuration > 0 ? ((float) (currentTime - this.mStartTime)) / ((float) scaledDuration) : 1.0f;
            boolean newIteration = ((int) fraction) > ((int) this.mOverallFraction);
            boolean lastIterationFinished = fraction >= ((float) (this.mRepeatCount + 1)) ? this.mRepeatCount != -1 : false;
            if (scaledDuration == 0) {
                done = true;
            } else if (!newIteration || lastIterationFinished) {
                if (lastIterationFinished) {
                    done = true;
                }
            } else if (this.mListeners != null) {
                int numListeners = this.mListeners.size();
                for (int i = 0; i < numListeners; i++) {
                    ((AnimatorListener) this.mListeners.get(i)).onAnimationRepeat(this);
                }
            }
            this.mOverallFraction = clampFraction(fraction);
            animateValue(getCurrentIterationFraction(this.mOverallFraction));
        }
        return done;
    }

    public final void doAnimationFrame(long frameTime) {
        AnimationHandler handler = AnimationHandler.getInstance();
        if (this.mLastFrameTime == 0) {
            handler.addOneShotCommitCallback(this);
            if (this.mStartDelay > 0) {
                startAnimation();
            }
            if (this.mSeekFraction < TonemapCurve.LEVEL_BLACK) {
                this.mStartTime = frameTime;
            } else {
                this.mStartTime = frameTime - ((long) (((float) getScaledDuration()) * this.mSeekFraction));
                this.mSeekFraction = -1.0f;
            }
            this.mStartTimeCommitted = false;
        }
        this.mLastFrameTime = frameTime;
        if (this.mPaused) {
            this.mPauseTime = frameTime;
            handler.removeCallback(this);
            return;
        }
        if (this.mResumed) {
            this.mResumed = false;
            if (this.mPauseTime > 0) {
                this.mStartTime += frameTime - this.mPauseTime;
                this.mStartTimeCommitted = false;
            }
            handler.addOneShotCommitCallback(this);
        }
        if (animateBasedOnTime(Math.max(frameTime, this.mStartTime))) {
            endAnimation();
        }
    }

    public float getAnimatedFraction() {
        return this.mCurrentFraction;
    }

    void animateValue(float fraction) {
        int i;
        fraction = this.mInterpolator.getInterpolation(fraction);
        this.mCurrentFraction = fraction;
        for (PropertyValuesHolder calculateValue : this.mValues) {
            calculateValue.calculateValue(fraction);
        }
        if (this.mUpdateListeners != null) {
            int numListeners = this.mUpdateListeners.size();
            for (i = 0; i < numListeners; i++) {
                ((AnimatorUpdateListener) this.mUpdateListeners.get(i)).onAnimationUpdate(this);
            }
        }
    }

    public ValueAnimator clone() {
        ValueAnimator anim = (ValueAnimator) super.clone();
        if (this.mUpdateListeners != null) {
            anim.mUpdateListeners = new ArrayList(this.mUpdateListeners);
        }
        anim.mSeekFraction = -1.0f;
        anim.mReversing = false;
        anim.mInitialized = false;
        anim.mStarted = false;
        anim.mRunning = false;
        anim.mPaused = false;
        anim.mResumed = false;
        anim.mStartListenersCalled = false;
        anim.mStartTime = 0;
        anim.mStartTimeCommitted = false;
        anim.mAnimationEndRequested = false;
        anim.mPauseTime = 0;
        anim.mLastFrameTime = 0;
        anim.mOverallFraction = TonemapCurve.LEVEL_BLACK;
        anim.mCurrentFraction = TonemapCurve.LEVEL_BLACK;
        PropertyValuesHolder[] oldValues = this.mValues;
        if (oldValues != null) {
            int numValues = oldValues.length;
            anim.mValues = new PropertyValuesHolder[numValues];
            anim.mValuesMap = new HashMap(numValues);
            for (int i = 0; i < numValues; i++) {
                PropertyValuesHolder newValuesHolder = oldValues[i].clone();
                anim.mValues[i] = newValuesHolder;
                anim.mValuesMap.put(newValuesHolder.getPropertyName(), newValuesHolder);
            }
        }
        return anim;
    }

    public static int getCurrentAnimationsCount() {
        return AnimationHandler.getAnimationCount();
    }

    public String toString() {
        String returnVal = "ValueAnimator@" + Integer.toHexString(hashCode());
        if (this.mValues != null) {
            for (PropertyValuesHolder propertyValuesHolder : this.mValues) {
                returnVal = returnVal + "\n    " + propertyValuesHolder.toString();
            }
        }
        return returnVal;
    }

    public void setAllowRunningAsynchronously(boolean mayRunAsync) {
    }
}
