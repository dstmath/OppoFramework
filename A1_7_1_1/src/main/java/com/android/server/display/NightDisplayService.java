package com.android.server.display;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.app.AlarmManager;
import android.app.AlarmManager.OnAlarmListener;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.provider.Settings.Secure;
import android.service.vr.IVrManager;
import android.service.vr.IVrStateCallbacks;
import android.service.vr.IVrStateCallbacks.Stub;
import android.util.MathUtils;
import android.util.Slog;
import android.view.animation.AnimationUtils;
import com.android.internal.app.NightDisplayController;
import com.android.internal.app.NightDisplayController.Callback;
import com.android.internal.app.NightDisplayController.LocalTime;
import com.android.server.SystemService;
import com.android.server.twilight.TwilightListener;
import com.android.server.twilight.TwilightManager;
import com.android.server.twilight.TwilightState;
import com.android.server.vr.VrManagerService;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicBoolean;

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
public final class NightDisplayService extends SystemService implements Callback {
    private static final ColorMatrixEvaluator COLOR_MATRIX_EVALUATOR = null;
    private static final boolean DEBUG = false;
    private static final float[] MATRIX_IDENTITY = null;
    private static final float[] MATRIX_NIGHT = null;
    private static final String TAG = "NightDisplayService";
    private AutoMode mAutoMode;
    private boolean mBootCompleted;
    private ValueAnimator mColorMatrixAnimator;
    private NightDisplayController mController;
    private int mCurrentUser;
    private final Handler mHandler;
    private final AtomicBoolean mIgnoreAllColorMatrixChanges;
    private Boolean mIsActivated;
    private ContentObserver mUserSetupObserver;
    private final IVrStateCallbacks mVrStateCallbacks;

    private abstract class AutoMode implements Callback {
        /* synthetic */ AutoMode(NightDisplayService this$0, AutoMode autoMode) {
            this();
        }

        public abstract void onStart();

        public abstract void onStop();

        private AutoMode() {
        }
    }

    private static class ColorMatrixEvaluator implements TypeEvaluator<float[]> {
        private final float[] mResultMatrix;

        /* synthetic */ ColorMatrixEvaluator(ColorMatrixEvaluator colorMatrixEvaluator) {
            this();
        }

        private ColorMatrixEvaluator() {
            this.mResultMatrix = new float[16];
        }

        public float[] evaluate(float fraction, float[] startValue, float[] endValue) {
            for (int i = 0; i < this.mResultMatrix.length; i++) {
                this.mResultMatrix[i] = MathUtils.lerp(startValue[i], endValue[i], fraction);
            }
            return this.mResultMatrix;
        }
    }

    private class CustomAutoMode extends AutoMode implements OnAlarmListener {
        private final AlarmManager mAlarmManager;
        private LocalTime mEndTime;
        private Calendar mLastActivatedTime;
        private LocalTime mStartTime;
        private final BroadcastReceiver mTimeChangedReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                CustomAutoMode.this.updateActivated();
            }
        };

        public CustomAutoMode() {
            super(NightDisplayService.this, null);
            this.mAlarmManager = (AlarmManager) NightDisplayService.this.getContext().getSystemService("alarm");
        }

        private void updateActivated() {
            Calendar now = Calendar.getInstance();
            boolean activated = now.before(this.mEndTime.getDateTimeAfter(this.mStartTime.getDateTimeBefore(now)));
            boolean setActivated = NightDisplayService.this.mIsActivated == null || this.mLastActivatedTime == null;
            if (!(setActivated || NightDisplayService.this.mIsActivated.booleanValue() == activated)) {
                TimeZone currentTimeZone = now.getTimeZone();
                if (!currentTimeZone.equals(this.mLastActivatedTime.getTimeZone())) {
                    int year = this.mLastActivatedTime.get(1);
                    int dayOfYear = this.mLastActivatedTime.get(6);
                    int hourOfDay = this.mLastActivatedTime.get(11);
                    int minute = this.mLastActivatedTime.get(12);
                    this.mLastActivatedTime.setTimeZone(currentTimeZone);
                    this.mLastActivatedTime.set(1, year);
                    this.mLastActivatedTime.set(6, dayOfYear);
                    this.mLastActivatedTime.set(11, hourOfDay);
                    this.mLastActivatedTime.set(12, minute);
                }
                setActivated = NightDisplayService.this.mIsActivated.booleanValue() ? !now.before(this.mStartTime.getDateTimeBefore(this.mLastActivatedTime)) ? now.after(this.mEndTime.getDateTimeAfter(this.mLastActivatedTime)) : true : !now.before(this.mEndTime.getDateTimeBefore(this.mLastActivatedTime)) ? now.after(this.mStartTime.getDateTimeAfter(this.mLastActivatedTime)) : true;
            }
            if (setActivated) {
                NightDisplayService.this.mController.setActivated(activated);
            }
            updateNextAlarm(NightDisplayService.this.mIsActivated, now);
        }

        private void updateNextAlarm(Boolean activated, Calendar now) {
            if (activated != null) {
                Calendar next;
                if (activated.booleanValue()) {
                    next = this.mEndTime.getDateTimeAfter(now);
                } else {
                    next = this.mStartTime.getDateTimeAfter(now);
                }
                this.mAlarmManager.setExact(1, next.getTimeInMillis(), NightDisplayService.TAG, this, null);
            }
        }

        public void onStart() {
            IntentFilter intentFilter = new IntentFilter("android.intent.action.TIME_SET");
            intentFilter.addAction("android.intent.action.TIMEZONE_CHANGED");
            NightDisplayService.this.getContext().registerReceiver(this.mTimeChangedReceiver, intentFilter);
            this.mStartTime = NightDisplayService.this.mController.getCustomStartTime();
            this.mEndTime = NightDisplayService.this.mController.getCustomEndTime();
            updateActivated();
        }

        public void onStop() {
            NightDisplayService.this.getContext().unregisterReceiver(this.mTimeChangedReceiver);
            this.mAlarmManager.cancel(this);
            this.mLastActivatedTime = null;
        }

        public void onActivated(boolean activated) {
            Calendar now = Calendar.getInstance();
            if (NightDisplayService.this.mIsActivated != null) {
                this.mLastActivatedTime = now;
            }
            updateNextAlarm(Boolean.valueOf(activated), now);
        }

        public void onCustomStartTimeChanged(LocalTime startTime) {
            this.mStartTime = startTime;
            this.mLastActivatedTime = null;
            updateActivated();
        }

        public void onCustomEndTimeChanged(LocalTime endTime) {
            this.mEndTime = endTime;
            this.mLastActivatedTime = null;
            updateActivated();
        }

        public void onAlarm() {
            Slog.d(NightDisplayService.TAG, "onAlarm");
            updateActivated();
        }
    }

    private class TwilightAutoMode extends AutoMode implements TwilightListener {
        private Calendar mLastActivatedTime;
        private final TwilightManager mTwilightManager;

        public TwilightAutoMode() {
            super(NightDisplayService.this, null);
            this.mTwilightManager = (TwilightManager) NightDisplayService.this.-wrap1(TwilightManager.class);
        }

        private void updateActivated(TwilightState state) {
            boolean setActivated = true;
            boolean isNight = state != null ? state.isNight() : false;
            if (NightDisplayService.this.mIsActivated != null && NightDisplayService.this.mIsActivated.booleanValue() == isNight) {
                setActivated = false;
            }
            if (!(!setActivated || state == null || this.mLastActivatedTime == null)) {
                Calendar sunrise = state.sunrise();
                Calendar sunset = state.sunset();
                if (sunrise.before(sunset)) {
                    if (this.mLastActivatedTime.before(sunrise)) {
                        setActivated = true;
                    } else {
                        setActivated = this.mLastActivatedTime.after(sunset);
                    }
                } else if (this.mLastActivatedTime.before(sunset)) {
                    setActivated = true;
                } else {
                    setActivated = this.mLastActivatedTime.after(sunrise);
                }
            }
            if (setActivated) {
                NightDisplayService.this.mController.setActivated(isNight);
            }
        }

        public void onStart() {
            this.mTwilightManager.registerListener(this, NightDisplayService.this.mHandler);
            updateActivated(this.mTwilightManager.getLastTwilightState());
        }

        public void onStop() {
            this.mTwilightManager.unregisterListener(this);
            this.mLastActivatedTime = null;
        }

        public void onActivated(boolean activated) {
            if (NightDisplayService.this.mIsActivated != null) {
                this.mLastActivatedTime = Calendar.getInstance();
            }
        }

        public void onTwilightStateChanged(TwilightState state) {
            Object obj = null;
            String str = NightDisplayService.TAG;
            StringBuilder append = new StringBuilder().append("onTwilightStateChanged: isNight=");
            if (state != null) {
                obj = Boolean.valueOf(state.isNight());
            }
            Slog.d(str, append.append(obj).toString());
            updateActivated(state);
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.display.NightDisplayService.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.display.NightDisplayService.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.display.NightDisplayService.<clinit>():void");
    }

    public NightDisplayService(Context context) {
        super(context);
        this.mIgnoreAllColorMatrixChanges = new AtomicBoolean();
        this.mVrStateCallbacks = new Stub() {
            public void onVrStateChanged(final boolean enabled) {
                NightDisplayService.this.mIgnoreAllColorMatrixChanges.set(enabled);
                NightDisplayService.this.mHandler.post(new Runnable() {
                    public void run() {
                        if (NightDisplayService.this.mColorMatrixAnimator != null) {
                            NightDisplayService.this.mColorMatrixAnimator.cancel();
                        }
                        DisplayTransformManager dtm = (DisplayTransformManager) NightDisplayService.this.-wrap1(DisplayTransformManager.class);
                        if (enabled) {
                            dtm.setColorMatrix(100, NightDisplayService.MATRIX_IDENTITY);
                        } else if (NightDisplayService.this.mController.isActivated()) {
                            dtm.setColorMatrix(100, NightDisplayService.MATRIX_NIGHT);
                        }
                    }
                });
            }
        };
        this.mCurrentUser = -10000;
        this.mHandler = new Handler(Looper.getMainLooper());
    }

    public void onStart() {
    }

    public void onBootPhase(int phase) {
        if (phase == 500) {
            IVrManager vrManager = (IVrManager) getBinderService(VrManagerService.VR_MANAGER_BINDER_SERVICE);
            if (vrManager != null) {
                try {
                    vrManager.registerListener(this.mVrStateCallbacks);
                } catch (RemoteException e) {
                    Slog.e(TAG, "Failed to register VR mode state listener: " + e);
                }
            }
        } else if (phase == 1000) {
            this.mBootCompleted = true;
            if (this.mCurrentUser != -10000 && this.mUserSetupObserver == null) {
                setUp();
            }
        }
    }

    public void onStartUser(int userHandle) {
        super.onStartUser(userHandle);
        if (this.mCurrentUser == -10000) {
            onUserChanged(userHandle);
        }
    }

    public void onSwitchUser(int userHandle) {
        super.onSwitchUser(userHandle);
        onUserChanged(userHandle);
    }

    public void onStopUser(int userHandle) {
        super.onStopUser(userHandle);
        if (this.mCurrentUser == userHandle) {
            onUserChanged(-10000);
        }
    }

    private void onUserChanged(int userHandle) {
        final ContentResolver cr = getContext().getContentResolver();
        if (this.mCurrentUser != -10000) {
            if (this.mUserSetupObserver != null) {
                cr.unregisterContentObserver(this.mUserSetupObserver);
                this.mUserSetupObserver = null;
            } else if (this.mBootCompleted) {
                tearDown();
            }
        }
        this.mCurrentUser = userHandle;
        if (this.mCurrentUser == -10000) {
            return;
        }
        if (!isUserSetupCompleted(cr, this.mCurrentUser)) {
            this.mUserSetupObserver = new ContentObserver(this.mHandler) {
                public void onChange(boolean selfChange, Uri uri) {
                    if (NightDisplayService.isUserSetupCompleted(cr, NightDisplayService.this.mCurrentUser)) {
                        cr.unregisterContentObserver(this);
                        NightDisplayService.this.mUserSetupObserver = null;
                        if (NightDisplayService.this.mBootCompleted) {
                            NightDisplayService.this.setUp();
                        }
                    }
                }
            };
            cr.registerContentObserver(Secure.getUriFor("user_setup_complete"), false, this.mUserSetupObserver, this.mCurrentUser);
        } else if (this.mBootCompleted) {
            setUp();
        }
    }

    private static boolean isUserSetupCompleted(ContentResolver cr, int userHandle) {
        return Secure.getIntForUser(cr, "user_setup_complete", 0, userHandle) == 1;
    }

    private void setUp() {
        Slog.d(TAG, "setUp: currentUser=" + this.mCurrentUser);
        this.mController = new NightDisplayController(getContext(), this.mCurrentUser);
        this.mController.setListener(this);
        onAutoModeChanged(this.mController.getAutoMode());
        if (this.mIsActivated == null) {
            onActivated(this.mController.isActivated());
        }
    }

    private void tearDown() {
        Slog.d(TAG, "tearDown: currentUser=" + this.mCurrentUser);
        if (this.mController != null) {
            this.mController.setListener(null);
            this.mController = null;
        }
        if (this.mAutoMode != null) {
            this.mAutoMode.onStop();
            this.mAutoMode = null;
        }
        if (this.mColorMatrixAnimator != null) {
            this.mColorMatrixAnimator.end();
            this.mColorMatrixAnimator = null;
        }
        this.mIsActivated = null;
    }

    public void onActivated(boolean activated) {
        if (this.mIsActivated == null || this.mIsActivated.booleanValue() != activated) {
            Slog.i(TAG, activated ? "Turning on night display" : "Turning off night display");
            if (this.mAutoMode != null) {
                this.mAutoMode.onActivated(activated);
            }
            this.mIsActivated = Boolean.valueOf(activated);
            if (this.mColorMatrixAnimator != null) {
                this.mColorMatrixAnimator.cancel();
            }
            if (!this.mIgnoreAllColorMatrixChanges.get()) {
                float[] fArr;
                final DisplayTransformManager dtm = (DisplayTransformManager) -wrap1(DisplayTransformManager.class);
                float[] from = dtm.getColorMatrix(100);
                final float[] to = this.mIsActivated.booleanValue() ? MATRIX_NIGHT : null;
                TypeEvaluator typeEvaluator = COLOR_MATRIX_EVALUATOR;
                Object[] objArr = new Object[2];
                if (from == null) {
                    from = MATRIX_IDENTITY;
                }
                objArr[0] = from;
                if (to == null) {
                    fArr = MATRIX_IDENTITY;
                } else {
                    fArr = to;
                }
                objArr[1] = fArr;
                this.mColorMatrixAnimator = ValueAnimator.ofObject(typeEvaluator, objArr);
                this.mColorMatrixAnimator.setDuration((long) getContext().getResources().getInteger(17694722));
                this.mColorMatrixAnimator.setInterpolator(AnimationUtils.loadInterpolator(getContext(), 17563661));
                this.mColorMatrixAnimator.addUpdateListener(new AnimatorUpdateListener() {
                    public void onAnimationUpdate(ValueAnimator animator) {
                        dtm.setColorMatrix(100, (float[]) animator.getAnimatedValue());
                    }
                });
                this.mColorMatrixAnimator.addListener(new AnimatorListenerAdapter() {
                    private boolean mIsCancelled;

                    public void onAnimationCancel(Animator animator) {
                        this.mIsCancelled = true;
                    }

                    public void onAnimationEnd(Animator animator) {
                        if (!this.mIsCancelled) {
                            dtm.setColorMatrix(100, to);
                        }
                        NightDisplayService.this.mColorMatrixAnimator = null;
                    }
                });
                this.mColorMatrixAnimator.start();
            }
        }
    }

    public void onAutoModeChanged(int autoMode) {
        Slog.d(TAG, "onAutoModeChanged: autoMode=" + autoMode);
        if (this.mAutoMode != null) {
            this.mAutoMode.onStop();
            this.mAutoMode = null;
        }
        if (autoMode == 1) {
            this.mAutoMode = new CustomAutoMode();
        } else if (autoMode == 2) {
            this.mAutoMode = new TwilightAutoMode();
        }
        if (this.mAutoMode != null) {
            this.mAutoMode.onStart();
        }
    }

    public void onCustomStartTimeChanged(LocalTime startTime) {
        Slog.d(TAG, "onCustomStartTimeChanged: startTime=" + startTime);
        if (this.mAutoMode != null) {
            this.mAutoMode.onCustomStartTimeChanged(startTime);
        }
    }

    public void onCustomEndTimeChanged(LocalTime endTime) {
        Slog.d(TAG, "onCustomEndTimeChanged: endTime=" + endTime);
        if (this.mAutoMode != null) {
            this.mAutoMode.onCustomEndTimeChanged(endTime);
        }
    }
}
