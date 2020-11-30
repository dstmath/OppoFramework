package com.android.server.display;

import android.content.Context;
import android.os.Handler;
import android.os.SystemProperties;
import android.os.Trace;
import android.service.dreams.DreamManagerInternal;
import android.util.FloatProperty;
import android.util.IntProperty;
import android.util.Slog;
import android.view.Choreographer;
import android.view.Display;
import com.android.server.LocalServices;
import com.android.server.biometrics.fingerprint.FingerprintService;
import com.oppo.hypnus.Hypnus;
import java.io.PrintWriter;

/* access modifiers changed from: package-private */
public final class DisplayPowerState {
    public static final FloatProperty<DisplayPowerState> COLOR_FADE_LEVEL = new FloatProperty<DisplayPowerState>("electronBeamLevel") {
        /* class com.android.server.display.DisplayPowerState.AnonymousClass1 */

        public void setValue(DisplayPowerState object, float value) {
            object.setColorFadeLevel(value);
        }

        public Float get(DisplayPowerState object) {
            return Float.valueOf(object.getColorFadeLevel());
        }
    };
    private static String COUNTER_COLOR_FADE = "ColorFadeLevel";
    public static boolean DEBUG = SystemProperties.getBoolean("dbg.dms.dps", false);
    public static final IntProperty<DisplayPowerState> SCREEN_BRIGHTNESS = new IntProperty<DisplayPowerState>("screenBrightness") {
        /* class com.android.server.display.DisplayPowerState.AnonymousClass2 */

        public void setValue(DisplayPowerState object, int value) {
            object.setScreenBrightness(value);
        }

        public Integer get(DisplayPowerState object) {
            return Integer.valueOf(object.getScreenBrightness());
        }
    };
    private static final String TAG = "DisplayPowerState";
    private static Hypnus mHyp = null;
    private final int mAodBrightness = 1;
    private final DisplayBlanker mBlanker;
    private final Choreographer mChoreographer = Choreographer.getInstance();
    private Runnable mCleanListener;
    private final ColorFade mColorFade;
    private boolean mColorFadeDrawPending;
    private final Runnable mColorFadeDrawRunnable = new Runnable() {
        /* class com.android.server.display.DisplayPowerState.AnonymousClass4 */

        public void run() {
            DisplayPowerState.this.mColorFadeDrawPending = false;
            if (DisplayPowerState.this.mColorFadePrepared) {
                DisplayPowerState.this.mColorFade.draw(DisplayPowerState.this.mColorFadeLevel);
                Trace.traceCounter(131072, DisplayPowerState.COUNTER_COLOR_FADE, Math.round(DisplayPowerState.this.mColorFadeLevel * 100.0f));
            }
            DisplayPowerState.this.mColorFadeReady = true;
            DisplayPowerState.this.invokeCleanListenerIfNeeded();
        }
    };
    private float mColorFadeLevel;
    private boolean mColorFadePrepared;
    private boolean mColorFadeReady;
    private DreamManagerInternal mDreamManager;
    private boolean mFingerprintOpticalSupport = false;
    private final Handler mHandler = new Handler(true);
    private boolean mIsAodStatus = false;
    private final PhotonicModulator mPhotonicModulator;
    private int mScreenBrightness;
    private boolean mScreenReady;
    private int mScreenState;
    private boolean mScreenUpdatePending;
    private final Runnable mScreenUpdateRunnable = new Runnable() {
        /* class com.android.server.display.DisplayPowerState.AnonymousClass3 */

        public void run() {
            int i = 0;
            DisplayPowerState.this.mScreenUpdatePending = false;
            if (DisplayPowerState.this.mScreenState != 1 && DisplayPowerState.this.mColorFadeLevel > OppoBrightUtils.MIN_LUX_LIMITI) {
                i = DisplayPowerState.this.mScreenBrightness;
            }
            int brightness = i;
            if (DisplayPowerState.this.mFingerprintOpticalSupport && DisplayPowerState.this.mDreamManager.isDreaming()) {
                brightness = DisplayPowerState.this.mScreenBrightness;
            }
            if (DisplayPowerState.this.mScreenState == 3 || DisplayPowerState.this.mScreenState == 4) {
                brightness = 1;
                if (DisplayPowerState.DEBUG) {
                    Slog.d(DisplayPowerState.TAG, "add for debug Aod brightness set");
                }
            }
            if (DisplayPowerState.this.mPhotonicModulator.setState(DisplayPowerState.this.mScreenState, brightness)) {
                if (DisplayPowerState.DEBUG) {
                    Slog.d(DisplayPowerState.TAG, "Screen ready");
                }
                DisplayPowerState.this.mScreenReady = true;
                DisplayPowerState.this.invokeCleanListenerIfNeeded();
            } else if (DisplayPowerState.DEBUG) {
                Slog.d(DisplayPowerState.TAG, "Screen not ready");
            }
        }
    };

    public DisplayPowerState(DisplayBlanker blanker, ColorFade colorFade, Context context) {
        this.mBlanker = blanker;
        this.mColorFade = colorFade;
        this.mPhotonicModulator = new PhotonicModulator();
        this.mPhotonicModulator.start();
        this.mScreenState = 2;
        this.mScreenBrightness = OppoBrightUtils.getInstance().getBootupBrightness();
        scheduleScreenUpdate();
        this.mColorFadePrepared = false;
        this.mColorFadeLevel = 1.0f;
        this.mColorFadeReady = true;
        this.mDreamManager = (DreamManagerInternal) LocalServices.getService(DreamManagerInternal.class);
        if (context != null && context.getPackageManager().hasSystemFeature(FingerprintService.OPTICAL_FINGERPRINT_FEATURE)) {
            this.mFingerprintOpticalSupport = true;
        }
    }

    public void setScreenState(int state) {
        if (this.mScreenState != state) {
            if (DEBUG) {
                Slog.d(TAG, "setScreenState: state=" + state);
            }
            this.mScreenState = state;
            this.mScreenReady = false;
            scheduleScreenUpdate();
            if (mHyp == null) {
                mHyp = Hypnus.getHypnus();
            }
            Hypnus hypnus = mHyp;
            if (hypnus != null) {
                hypnus.HypnusSetDisplayState(state);
            }
        }
    }

    public int getScreenState() {
        return this.mScreenState;
    }

    public void setScreenBrightness(int brightness) {
        if (this.mScreenBrightness != brightness) {
            if (DEBUG) {
                Slog.d(TAG, "setScreenBrightness: brightness=" + brightness);
            }
            this.mScreenBrightness = brightness;
            if (this.mScreenState != 1) {
                this.mScreenReady = false;
                scheduleScreenUpdate();
            }
        }
    }

    public int getScreenBrightness() {
        return this.mScreenBrightness;
    }

    public boolean prepareColorFade(Context context, int mode) {
        ColorFade colorFade = this.mColorFade;
        if (colorFade == null || !colorFade.prepare(context, mode)) {
            this.mColorFadePrepared = false;
            this.mColorFadeReady = true;
            return false;
        }
        this.mColorFadePrepared = true;
        this.mColorFadeReady = false;
        scheduleColorFadeDraw();
        return true;
    }

    public void dismissColorFade() {
        Trace.traceCounter(131072, COUNTER_COLOR_FADE, 100);
        ColorFade colorFade = this.mColorFade;
        if (colorFade != null) {
            colorFade.dismiss();
        }
        this.mColorFadePrepared = false;
        this.mColorFadeReady = true;
    }

    public void dismissColorFadeResources() {
        ColorFade colorFade = this.mColorFade;
        if (colorFade != null) {
            colorFade.dismissResources();
        }
    }

    public void setColorFadeLevel(float level) {
        if (this.mColorFadeLevel != level) {
            if (DEBUG) {
                Slog.d(TAG, "setColorFadeLevel: level=" + level);
            }
            this.mColorFadeLevel = level;
            if (this.mScreenState != 1) {
                this.mScreenReady = false;
                scheduleScreenUpdate();
            }
            if (this.mColorFadePrepared) {
                this.mColorFadeReady = false;
                scheduleColorFadeDraw();
            }
        }
    }

    public float getColorFadeLevel() {
        return this.mColorFadeLevel;
    }

    public void setAodStatus(boolean isAod) {
        if (this.mIsAodStatus != isAod) {
            if (DEBUG) {
                Slog.d(TAG, "setAodStatus: isAod=" + isAod);
            }
            this.mIsAodStatus = isAod;
            if (this.mIsAodStatus) {
                this.mScreenReady = false;
                scheduleScreenUpdate();
            }
        }
    }

    public boolean getAodStatus() {
        return this.mIsAodStatus;
    }

    public boolean waitUntilClean(Runnable listener) {
        if (!this.mScreenReady || !this.mColorFadeReady) {
            this.mCleanListener = listener;
            return false;
        }
        this.mCleanListener = null;
        return true;
    }

    public void dump(PrintWriter pw) {
        pw.println();
        pw.println("Display Power State:");
        pw.println("  mScreenState=" + Display.stateToString(this.mScreenState));
        pw.println("  mScreenBrightness=" + this.mScreenBrightness);
        pw.println("  mScreenReady=" + this.mScreenReady);
        pw.println("  mScreenUpdatePending=" + this.mScreenUpdatePending);
        pw.println("  mColorFadePrepared=" + this.mColorFadePrepared);
        pw.println("  mColorFadeLevel=" + this.mColorFadeLevel);
        pw.println("  mColorFadeReady=" + this.mColorFadeReady);
        pw.println("  mColorFadeDrawPending=" + this.mColorFadeDrawPending);
        this.mPhotonicModulator.dump(pw);
        ColorFade colorFade = this.mColorFade;
        if (colorFade != null) {
            colorFade.dump(pw);
        }
    }

    private void scheduleScreenUpdate() {
        if (!this.mScreenUpdatePending) {
            this.mScreenUpdatePending = true;
            postScreenUpdateThreadSafe();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void postScreenUpdateThreadSafe() {
        this.mHandler.removeCallbacks(this.mScreenUpdateRunnable);
        this.mHandler.post(this.mScreenUpdateRunnable);
    }

    private void scheduleColorFadeDraw() {
        if (!this.mColorFadeDrawPending) {
            this.mColorFadeDrawPending = true;
            this.mChoreographer.postCallback(3, this.mColorFadeDrawRunnable, null);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void invokeCleanListenerIfNeeded() {
        Runnable listener = this.mCleanListener;
        if (listener != null && this.mScreenReady && this.mColorFadeReady) {
            this.mCleanListener = null;
            listener.run();
        }
    }

    /* access modifiers changed from: private */
    public final class PhotonicModulator extends Thread {
        private static final int INITIAL_BACKLIGHT = -1;
        private static final int INITIAL_SCREEN_STATE = 1;
        private int mActualBacklight = -1;
        private int mActualState = 1;
        private boolean mBacklightChangeInProgress;
        private final Object mLock = new Object();
        private int mPendingBacklight = -1;
        private int mPendingState = 1;
        private boolean mStateChangeInProgress;

        public PhotonicModulator() {
            super("PhotonicModulator");
        }

        /* JADX WARNING: Removed duplicated region for block: B:24:0x004f  */
        /* JADX WARNING: Removed duplicated region for block: B:31:0x005b  */
        /* JADX WARNING: Removed duplicated region for block: B:38:0x0067  */
        public boolean setState(int state, int backlight) {
            boolean z;
            boolean changeInProgress;
            boolean z2;
            boolean z3;
            synchronized (this.mLock) {
                z = true;
                boolean stateChanged = state != this.mPendingState;
                boolean backlightChanged = backlight != this.mPendingBacklight;
                if (stateChanged || backlightChanged) {
                    if (DisplayPowerState.DEBUG) {
                        Slog.d(DisplayPowerState.TAG, "Requesting new screen state: state=" + Display.stateToString(state) + ", backlight=" + backlight);
                    }
                    this.mPendingState = state;
                    this.mPendingBacklight = backlight;
                    if (!this.mStateChangeInProgress) {
                        if (!this.mBacklightChangeInProgress) {
                            changeInProgress = false;
                            if (!stateChanged) {
                                if (!this.mStateChangeInProgress) {
                                    z2 = false;
                                    this.mStateChangeInProgress = z2;
                                    if (!backlightChanged) {
                                        if (!this.mBacklightChangeInProgress) {
                                            z3 = false;
                                            this.mBacklightChangeInProgress = z3;
                                            if (!changeInProgress) {
                                                this.mLock.notifyAll();
                                            }
                                        }
                                    }
                                    z3 = true;
                                    this.mBacklightChangeInProgress = z3;
                                    if (!changeInProgress) {
                                    }
                                }
                            }
                            z2 = true;
                            this.mStateChangeInProgress = z2;
                            if (!backlightChanged) {
                            }
                            z3 = true;
                            this.mBacklightChangeInProgress = z3;
                            if (!changeInProgress) {
                            }
                        }
                    }
                    changeInProgress = true;
                    if (!stateChanged) {
                    }
                    z2 = true;
                    this.mStateChangeInProgress = z2;
                    if (!backlightChanged) {
                    }
                    z3 = true;
                    this.mBacklightChangeInProgress = z3;
                    if (!changeInProgress) {
                    }
                }
                if (this.mStateChangeInProgress) {
                    z = false;
                }
            }
            return z;
        }

        public void dump(PrintWriter pw) {
            synchronized (this.mLock) {
                pw.println();
                pw.println("Photonic Modulator State:");
                pw.println("  mPendingState=" + Display.stateToString(this.mPendingState));
                pw.println("  mPendingBacklight=" + this.mPendingBacklight);
                pw.println("  mActualState=" + Display.stateToString(this.mActualState));
                pw.println("  mActualBacklight=" + this.mActualBacklight);
                pw.println("  mStateChangeInProgress=" + this.mStateChangeInProgress);
                pw.println("  mBacklightChangeInProgress=" + this.mBacklightChangeInProgress);
            }
        }

        public void run() {
            while (true) {
                synchronized (this.mLock) {
                    int state = this.mPendingState;
                    boolean backlightChanged = true;
                    boolean stateChanged = state != this.mActualState;
                    int backlight = this.mPendingBacklight;
                    if (backlight == this.mActualBacklight) {
                        backlightChanged = false;
                    }
                    if (!stateChanged) {
                        DisplayPowerState.this.postScreenUpdateThreadSafe();
                        this.mStateChangeInProgress = false;
                    }
                    if (!backlightChanged) {
                        this.mBacklightChangeInProgress = false;
                    }
                    if (stateChanged || backlightChanged) {
                        this.mActualState = state;
                        this.mActualBacklight = backlight;
                        if (DisplayPowerState.DEBUG) {
                            Slog.d(DisplayPowerState.TAG, "Updating screen state: state=" + Display.stateToString(state) + ", backlight=" + backlight);
                        }
                        DisplayPowerState.this.mBlanker.requestDisplayState(state, backlight);
                    } else {
                        try {
                            this.mLock.wait();
                        } catch (InterruptedException e) {
                        }
                    }
                }
            }
        }
    }
}
