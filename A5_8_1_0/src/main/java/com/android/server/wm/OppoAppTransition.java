package com.android.server.wm;

import android.content.Context;
import android.graphics.GraphicBuffer;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Debug;
import android.os.IRemoteCallback;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.telephony.TelephonyManager;
import android.util.Slog;
import android.view.OppoScreenDragUtil;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.OppoBezierInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import com.android.server.am.ActivityManagerService;
import com.android.server.am.OppoFreeFormManagerService;
import com.android.server.am.OppoProcessManager;
import com.android.server.display.DisplayTransformManager;
import com.android.server.display.OppoBrightUtils;
import com.oppo.util.OppoAnimSynthesisNumber;
import java.util.ArrayList;

public class OppoAppTransition extends AppTransition {
    static int ALPHA_BASE_T = 210;
    static int ALPHA_DELAY_BASE_T = 70;
    static int ALPHA_DELAY_DELTA_T = 0;
    static int ALPHA_DELTA_T = 0;
    private static final int ANIM_MORE_TIME = 30;
    private static final boolean DEBUG = false;
    private static final int DEFAULT_APP_TRANSITION_DURATION = 336;
    private static final long DEFAULT_APP_TRANSITION_DURATION_ENTER = 200;
    private static final long DEFAULT_APP_TRANSITION_DURATION_EXIT = 200;
    private static final long DEFAULT_APP_TRANSITION_DURATION_FREEFORM = 0;
    static int DELTA_NUM = 4;
    static int ENTER_BASE_T = DisplayTransformManager.LEVEL_COLOR_MATRIX_COLOR;
    static int ENTER_DELTA_T = 0;
    private static final float EXITICONSCALEHEIGHT = 0.5f;
    private static final float EXITICONSCALEWIDTH = 0.5f;
    private static final float HEIGHTPERSENT = 0.75f;
    private static final float ICONSCALEHEIGHT = 0.4f;
    private static final float ICONSCALEWIDTH = 0.4f;
    private static final String INCALLUI_PACKAGE_NAME = "com.android.incallui";
    private static final int NEXT_TRANSIT_TYPE_CUSTOM = 1;
    private static final int NEXT_TRANSIT_TYPE_NONE = 0;
    private static final int NEXT_TRANSIT_TYPE_SCALE_UP = 2;
    static int SCALE_BASE_T = 280;
    static int SCALE_DELTA_T = 0;
    private static final String TAG = "OppoAppTransition";
    static int TRANS_X_BASE_T = 300;
    static int TRANS_X_DELTA_T = 100;
    static int TRANS_Y_BASE_T = 300;
    static int TRANS_Y_DELTA_T = 100;
    private static final float WIDTHPERSENT = 0.6f;
    static int delayAlpha = 70;
    static int durationAlpha = 210;
    static int durationDelayX = 0;
    static int durationEnter = DisplayTransformManager.LEVEL_COLOR_MATRIX_COLOR;
    static int durationScale = 280;
    static int durationTransX = DisplayTransformManager.LEVEL_COLOR_MATRIX_COLOR;
    static int durationTransY = DisplayTransformManager.LEVEL_COLOR_MATRIX_COLOR;
    private static final String mOppoRencentPackageName = "com.coloros.recents";
    static float scaleDst = 0.34f;
    static float scaleDstEnter = 0.86f;
    private Interpolator mAccelerateInterpolator;
    private String mApptokenName = null;
    protected AppWindowToken mCloseAtoken;
    private int mConfigShortAnimTime;
    private Context mContext;
    private Interpolator mDecelerateInterpolator;
    private float[] mEnterPosition = new float[3];
    private float[] mExitPosition = new float[3];
    private boolean mIsHeteromorphism = false;
    private boolean mIsOppoLauncherEnter = false;
    private boolean mIsOppoLauncherExit = false;
    private boolean mIsOppoRencentExit = false;
    private boolean mIsSystemUI = false;
    private int mNextAppTransitionEnterTemp;
    private int mNextAppTransitionExitTemp;
    private int mNextAppTransitionStartHeight;
    private int mNextAppTransitionStartWidth;
    private int mNextAppTransitionStartX;
    private int mNextAppTransitionStartY;
    private AppWindowToken mOpenAtoken;
    private OppoBezierInterpolator mOppoBezierInterpolatorAlpha;
    private OppoBezierInterpolator mOppoBezierInterpolatorEnter;
    private OppoBezierInterpolator mOppoBezierInterpolatorEnterAlpha;
    private final OppoBezierInterpolator mOppoBezierInterpolatorEnterAlphaNew = new OppoBezierInterpolator(0.25d, 0.10000000149011612d, 0.25d, 1.0d, true);
    private final OppoBezierInterpolator mOppoBezierInterpolatorEnterNew = new OppoBezierInterpolator(0.41999998688697815d, 0.25d, -0.11999999731779099d, 1.0d, true);
    private OppoBezierInterpolator mOppoBezierInterpolatorExit;
    private OppoBezierInterpolator mOppoBezierInterpolatorScale;
    private OppoBezierInterpolator mOppoBezierInterpolatorTansX;
    private OppoBezierInterpolator mOppoBezierInterpolatorTansY;
    private long mResumeLauncherTime = 0;
    private final ArrayList<String> mSpecialAppWindows = new ArrayList();
    private boolean mUserGestureAnim = false;
    private int mWindowHeight = 0;
    private int mWindowWidth = 0;
    int offsetX = 0;
    int offsetY = 0;

    OppoAppTransition(Context context, WindowManagerService servic) {
        super(context, servic);
        this.mContext = context;
        this.mConfigShortAnimTime = context.getResources().getInteger(17694720);
        this.mOppoBezierInterpolatorEnter = new OppoBezierInterpolator(0.20000000298023224d, 0.07999999821186066d, 0.20000000298023224d, 1.0d, true);
        this.mOppoBezierInterpolatorExit = new OppoBezierInterpolator(0.3741999864578247d, 0.12520000338554382d, 0.20340000092983246d, 1.0d, true);
        this.mOppoBezierInterpolatorTansX = new OppoBezierInterpolator(0.15000000596046448d, 0.25d, 0.15000000596046448d, 1.0d, true);
        this.mOppoBezierInterpolatorTansY = new OppoBezierInterpolator(0.20000000298023224d, 0.0d, 0.75d, 1.0d, true);
        this.mOppoBezierInterpolatorScale = new OppoBezierInterpolator(0.5d, 0.23000000417232513d, 0.25999999046325684d, 0.949999988079071d, true);
        this.mOppoBezierInterpolatorAlpha = new OppoBezierInterpolator(0.6499999761581421d, 0.0d, 0.1899999976158142d, 1.0d, true);
        this.mOppoBezierInterpolatorEnterAlpha = new OppoBezierInterpolator(0.25d, 0.10000000149011612d, 0.25d, 1.0d, true);
        this.mAccelerateInterpolator = new AccelerateInterpolator();
        this.mDecelerateInterpolator = new DecelerateInterpolator(1.2f);
        WindowManager wm = (WindowManager) this.mContext.getSystemService(OppoProcessManager.RESUME_REASON_VISIBLE_WINDOW_STR);
        Point windowSize = new Point();
        wm.getDefaultDisplay().getSize(windowSize);
        this.mWindowWidth = windowSize.x;
        this.mWindowHeight = windowSize.y;
        this.mIsHeteromorphism = false;
        initSpecilaAppWindows();
    }

    void overridePendingAppTransitionScaleUp(int startX, int startY, int startWidth, int startHeight) {
        if (isTransitionSet()) {
            clear();
            this.mNextAppTransitionType = 2;
            putDefaultNextAppTransitionCoordinates(startX, startY, startX + startWidth, startY + startHeight, null);
            this.mNextAppTransitionStartX = startX;
            this.mNextAppTransitionStartY = startY;
            this.mNextAppTransitionStartWidth = startWidth;
            this.mNextAppTransitionStartHeight = startHeight;
            this.mNextAppTransitionEnterTemp = OppoAnimSynthesisNumber.getSynthesisNumber((startWidth / 4) + startX, startWidth / 2);
            this.mNextAppTransitionExitTemp = OppoAnimSynthesisNumber.getSynthesisNumber((startWidth / 4) + startY, startWidth / 2);
            postAnimationCallback();
            if (WindowManagerDebugConfig.DEBUG_ANIM) {
                Slog.v(TAG, "OppoAppTransition overridePendingAppTransitionScaleUp startX=" + startX + " startY=" + startY + " startWidth=" + startWidth + " startHeight=" + startHeight);
            }
        }
    }

    void overridePendingAppTransition(String packageName, int enterAnim, int exitAnim, IRemoteCallback startedCallback) {
        if (!isTransitionSet() || (this.mIsOppoRencentExit ^ 1) == 0 || (this.mIsSystemUI ^ 1) == 0) {
            postAnimationCallback();
            return;
        }
        clear();
        this.mNextAppTransitionType = 1;
        this.mNextAppTransitionPackage = packageName;
        this.mNextAppTransitionEnter = enterAnim;
        this.mNextAppTransitionExit = exitAnim;
        postAnimationCallback();
        this.mNextAppTransitionCallback = startedCallback;
        if (2 != this.mNextAppTransitionType) {
            if (this.mNextAppTransitionPackage.equals(mOppoRencentPackageName)) {
                this.mIsOppoRencentExit = true;
            } else if (this.mNextAppTransitionPackage.equals(OppoFreeFormManagerService.FREEFORM_CALLER_PKG)) {
                this.mIsSystemUI = true;
            }
            if (OppoAnimSynthesisNumber.isSynthesisNumber(enterAnim) && OppoAnimSynthesisNumber.isSynthesisNumber(exitAnim)) {
                if (!(!this.mService.mPolicy.isGestureAnimSupport() || this.mService.mClosingApps.isEmpty() || (checkSpecilaAppWindowToken((AppWindowToken) this.mService.mClosingApps.valueAt(0)) ^ 1) == 0)) {
                    this.mUserGestureAnim = true;
                    this.mService.mPolicy.handleOpeningSpecialApp(1, ActivityManagerService.OPPO_LAUNCHER);
                }
                this.mNextAppTransitionEnterTemp = enterAnim;
                this.mNextAppTransitionExitTemp = exitAnim;
            }
            if (WindowManagerDebugConfig.DEBUG_ANIM) {
                Slog.v(TAG, "OppoAppTransition overridePendingAppTransition packageName=" + packageName + " mIsOppoRencentExit=" + this.mIsOppoRencentExit + " mIsSystemUI=" + this.mIsSystemUI + " enterAnim=" + Integer.toHexString(enterAnim) + " exitAnim=" + Integer.toHexString(exitAnim));
            }
        }
    }

    void clear() {
        if (WindowManagerDebugConfig.DEBUG_ANIM) {
            Slog.v(TAG, "OppoAppTransition clear()");
        }
        this.mNextAppTransitionEnterTemp = 0;
        this.mNextAppTransitionExitTemp = 0;
        this.mIsOppoRencentExit = false;
        this.mIsSystemUI = false;
        this.mUserGestureAnim = false;
        super.clear();
    }

    void setAppWindowTokenLocked(AppWindowToken closeAtoken, AppWindowToken openAtoken) {
        this.mApptokenName = closeAtoken.toString();
        this.mOpenAtoken = openAtoken;
        this.mCloseAtoken = closeAtoken;
        if (openAtoken.toString().contains("com.oppo.launcher/.Launcher")) {
            this.mResumeLauncherTime = openAtoken.mResumeLauncherTime;
            this.mIsOppoLauncherEnter = true;
        } else {
            this.mIsOppoLauncherEnter = false;
        }
        if (closeAtoken.toString().contains("com.oppo.launcher/.Launcher")) {
            this.mIsOppoLauncherExit = true;
        } else {
            this.mIsOppoLauncherExit = false;
        }
        if (WindowManagerDebugConfig.DEBUG_ANIM) {
            Slog.v(TAG, "setAppWindowTokenLocked mIsOppoLauncherEnter=" + this.mIsOppoLauncherEnter + " openAtoken=" + openAtoken);
            Slog.v(TAG, "setAppWindowTokenLocked mIsOppoLauncherExit=" + this.mIsOppoLauncherExit + " closeAtoken=" + closeAtoken);
        }
    }

    private void reviseCustomExitAnim(int transit) {
        if (this.mIsOppoLauncherEnter && this.mNextAppTransitionType == 0 && transit == 13 && this.mNextAppTransitionEnterTemp != 0 && this.mNextAppTransitionExitTemp != 0) {
            this.mNextAppTransitionType = 1;
            this.mNextAppTransitionEnter = this.mNextAppTransitionEnterTemp;
            this.mNextAppTransitionExit = this.mNextAppTransitionExitTemp;
            this.mNextAppTransitionEnterTemp = 0;
            this.mNextAppTransitionExitTemp = 0;
        } else if (this.mIsOppoLauncherExit && this.mNextAppTransitionType == 0 && this.mNextAppTransitionEnterTemp != 0 && this.mNextAppTransitionExitTemp != 0) {
            this.mNextAppTransitionType = 2;
        }
    }

    Animation loadAnimation(LayoutParams lp, int transit, boolean enter, int uiMode, int orientation, Rect frame, Rect displayFrame, Rect insets, Rect surfaceInsets, Rect stableInsets, boolean isVoiceInteraction, boolean freeform, int taskId) {
        reviseCustomExitAnim(transit);
        if (freeform) {
            return createOppoFreeFormAnimationLocked(transit, enter, frame.width(), frame.height());
        }
        Animation a;
        if (this.mNextAppTransitionType == 1) {
            a = createOppoCustomAnimLocked(this.mNextAppTransitionPackage, enter, frame.width(), frame.height());
            if (!(a == null || this.mApptokenName == null || !this.mApptokenName.contains("cn.kuwo.ui.lockscreen.LockScreenActivity"))) {
                a.scaleCurrentDuration(OppoBrightUtils.MIN_LUX_LIMITI);
            }
            if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS || WindowManagerDebugConfig.DEBUG_ANIM) {
                Slog.v(TAG, "applyAnimation: anim=" + a + " nextAppTransition=ANIM_CUSTOM" + " transit=" + transit + " isEntrance=" + enter + " Callers=" + Debug.getCallers(3));
            }
        } else if (this.mNextAppTransitionType == 2) {
            a = createOppoScaleUpAnimationLocked(transit, enter, frame.width(), frame.height());
            if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS || WindowManagerDebugConfig.DEBUG_ANIM) {
                Slog.v(TAG, "applyAnimation: anim=" + a + " nextAppTransition=ANIM_SCALE_UP" + " transit=" + transit + " isEntrance=" + enter + " Callers=" + Debug.getCallers(3));
            }
        } else {
            if (this.mNextAppTransitionType != 13 || (enter ^ 1) == 0) {
                a = super.loadAnimation(lp, transit, enter, uiMode, orientation, frame, displayFrame, insets, surfaceInsets, stableInsets, isVoiceInteraction, freeform, taskId);
            } else {
                a = createOppoCustomAnimLocked(this.mNextAppTransitionPackage, enter, frame.width(), frame.height());
            }
            if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS || WindowManagerDebugConfig.DEBUG_ANIM) {
                Slog.v(TAG, "applyAnimation: anim=" + a + " nextAppTransition=" + this.mNextAppTransitionType + " transit=" + transit + " isEntrance=" + enter + " Callers=" + Debug.getCallers(3));
            }
        }
        return a;
    }

    private Animation createOppoScaleUpAnimationLocked(int transit, boolean enter, int appWidth, int appHeight) {
        Animation a;
        if (enter) {
            this.mEnterPosition = caculatePosition((float) this.mNextAppTransitionStartWidth, (float) this.mNextAppTransitionStartHeight, (float) this.mNextAppTransitionStartX, (float) this.mNextAppTransitionStartY, this.mEnterPosition);
            a = prepareOPPOScaleUpEnterAnimation(this.mEnterPosition[0], this.mEnterPosition[1], this.mEnterPosition[2]);
            a.setDetachWallpaper(true);
            if (!(this.mNextAppTransitionEnterTemp == 0 || this.mNextAppTransitionExitTemp == 0)) {
                a.setAnimationListener(new AnimationListener() {
                    public void onAnimationStart(Animation animation) {
                    }

                    public void onAnimationRepeat(Animation animation) {
                    }

                    public void onAnimationEnd(Animation animation) {
                        OppoAppTransition.this.mNextAppTransitionEnterTemp = 0;
                        OppoAppTransition.this.mNextAppTransitionExitTemp = 0;
                    }
                });
            }
        } else if (this.mIsHeteromorphism) {
            a = createOppoScaleUpExitAnimationLocked(transit, false, appWidth, appHeight, this.mEnterPosition[2]);
        } else {
            Animation scale = new ScaleAnimation(1.0f, 1.0f, 1.0f, 1.0f, 1, 0.5f, 1, 0.5f);
            scale.setInterpolator(this.mOppoBezierInterpolatorEnterAlpha);
            scale.setDuration(((long) this.mEnterPosition[2]) + 200);
            scale.setDetachWallpaper(true);
            a = scale;
        }
        a.setFillAfter(true);
        a.initialize(appWidth, appHeight, appWidth, appHeight);
        return a;
    }

    private Animation createOppoScaleUpExitAnimationLocked(int transit, boolean enter, int appWidth, int appHeight, float time) {
        if (WindowManagerDebugConfig.DEBUG_ANIM) {
            Slog.v(TAG, "createOppoScaleUpExitAnimationLocked mIsOppoLauncherExit = " + this.mIsOppoLauncherExit);
        }
        Animation a;
        if (this.mIsOppoLauncherExit) {
            a = new AlphaAnimation(1.0f, 1.0f);
            a.setDuration((long) (200.0f + time));
            a.setDetachWallpaper(true);
            return a;
        }
        long duration;
        if (transit == 14 || transit == 15) {
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
        a.setInterpolator(this.mDecelerateInterpolator);
        return a;
    }

    private Animation createOppoCustomAnimLocked(String packageName, boolean enter, int appWidth, int appHeight) {
        Animation a;
        if (OppoAnimSynthesisNumber.isSynthesisNumber(this.mNextAppTransitionEnter) && OppoAnimSynthesisNumber.isSynthesisNumber(this.mNextAppTransitionExit)) {
            int startX;
            int startY;
            int startWidth = OppoAnimSynthesisNumber.getLowerDigit(this.mNextAppTransitionEnter);
            int startHight = OppoAnimSynthesisNumber.getLowerDigit(this.mNextAppTransitionExit);
            if (!(startWidth == 0 && startHight == 0) && OppoAnimSynthesisNumber.getHighDigit(this.mNextAppTransitionEnter) < appWidth && OppoAnimSynthesisNumber.getHighDigit(this.mNextAppTransitionExit) < appHeight) {
                startX = OppoAnimSynthesisNumber.getHighDigit(this.mNextAppTransitionEnter);
                startY = OppoAnimSynthesisNumber.getHighDigit(this.mNextAppTransitionExit);
            } else {
                startX = 0;
                startY = 0;
                if (WindowManagerDebugConfig.DEBUG_ANIM) {
                    Slog.v(TAG, "createOppoCustomAnimLocked start center");
                }
            }
            if (WindowManagerDebugConfig.DEBUG_ANIM) {
                Slog.v(TAG, "createOppoCustomAnimLocked strartX=" + startX + " startWidth=" + startWidth + " startY=" + startY + " startHight=" + startHight + " appWidth=" + appWidth + " appHeight=" + appHeight);
            }
            this.mExitPosition = caculatePosition((float) this.mNextAppTransitionStartWidth, (float) this.mNextAppTransitionStartHeight, (float) startX, (float) startY, this.mExitPosition);
            caculateAniAttrs(startX, startY, appWidth, appHeight);
            if (enter) {
                long resumeTime = 0;
                boolean isNoScale = false;
                if (this.mService.mPolicy.isGestureAnimSupport() && this.mService.mPolicy.getNavigationBarStatus() == 2) {
                    resumeTime = SystemClock.uptimeMillis() - this.mResumeLauncherTime;
                    if (resumeTime >= ((long) durationEnter)) {
                        isNoScale = true;
                    } else {
                        resumeTime = resumeTime >= ((long) (durationEnter + -110)) ? (long) (durationEnter - 120) : resumeTime - 20;
                    }
                }
                if (this.mIsHeteromorphism || (isNoScale ^ 1) == 0) {
                    a = new AlphaAnimation(1.0f, 1.0f);
                    a.setDuration(200);
                    a.setDetachWallpaper(true);
                } else {
                    Animation scale = new ScaleAnimation(scaleDstEnter, 1.0f, scaleDstEnter, 1.0f, 1, 0.5f, 1, 0.5f);
                    scale.setInterpolator(this.mOppoBezierInterpolatorEnterAlpha);
                    scale.setDuration(((long) durationEnter) - resumeTime);
                    scale.setDetachWallpaper(true);
                    a = scale;
                }
            } else {
                if (this.mService.mPolicy.isGestureAnimSupport()) {
                    if ((checkSpecilaAppWindowToken(this.mCloseAtoken) ^ 1) != 0) {
                        a = new AlphaAnimation(1.0f, 1.0f);
                        a.setZAdjustment(1);
                        a.setDuration(50);
                        return a;
                    }
                }
                a = prepareOPPOScaleUpExitAnimation(this.mExitPosition[0], this.mExitPosition[1]);
                a.setZAdjustment(1);
            }
        } else {
            a = loadAnimationRes(this.mNextAppTransitionPackage, enter ? this.mNextAppTransitionEnter : this.mNextAppTransitionExit);
        }
        return a;
    }

    public Animation prepareOPPOScaleUpEnterAnimation(float pivotX, float pivotY, float time) {
        Animation scale = new ScaleAnimation(0.4f, 1.0f, 0.4f, 1.0f, pivotX, pivotY);
        scale.setInterpolator(this.mOppoBezierInterpolatorEnter);
        scale.setDuration((long) (200.0f + time));
        Animation alpha = new AlphaAnimation(OppoBrightUtils.MIN_LUX_LIMITI, 1.0f);
        alpha.setInterpolator(this.mAccelerateInterpolator);
        alpha.setDuration((long) (((200.0f + time) * 2.0f) / 5.0f));
        AnimationSet set = new AnimationSet(false);
        set.addAnimation(scale);
        set.addAnimation(alpha);
        return set;
    }

    public Animation prepareOPPOScaleUpExitAnimation(float pivotX, float pivotY) {
        Animation scale = new ScaleAnimation(1.0f, scaleDst, 1.0f, scaleDst, 1, 0.5f, 1, 0.5f);
        scale.setInterpolator(this.mOppoBezierInterpolatorScale);
        scale.setDuration((long) durationScale);
        Animation alpha = new AlphaAnimation(1.0f, OppoBrightUtils.MIN_LUX_LIMITI);
        alpha.setInterpolator(this.mOppoBezierInterpolatorAlpha);
        alpha.setDuration((long) durationAlpha);
        alpha.setStartOffset((long) delayAlpha);
        AnimationSet set = new AnimationSet(false);
        set.addAnimation(scale);
        set.addAnimation(alpha);
        return set;
    }

    public Animation prepareOPPOScaleUpExitAnimation(int startX, int startY, int startWidth, int startHight, int appWidth, int appHeight) {
        int transX = startX - ((appWidth - startWidth) / 2);
        int transY = startY - ((appHeight - startHight) / 2);
        float scaleX = ((float) startWidth) / ((float) appWidth);
        float scaleY = ((float) startHight) / ((float) appHeight);
        float scaleBase = SystemProperties.getInt("debug.animation.startoffset", 0) >= 1 ? 0.9f : 1.0f;
        if (WindowManagerDebugConfig.DEBUG_ANIM) {
            Slog.v(TAG, "prepareOPPOScaleUpExitAnimation startX=" + startX + " startY=" + startY + " appWidth=" + appWidth + " appHeight=" + appHeight + " scaleBase=" + scaleBase + ", scaleDst=" + scaleDst);
        }
        Animation scale = new ScaleAnimation(1.0f, scaleDst, 1.0f, scaleDst, 1, 0.5f, 1, 0.5f);
        scale.setInterpolator(this.mOppoBezierInterpolatorScale);
        scale.setDuration((long) durationScale);
        Animation translateAnimation = new TranslateAnimation(OppoBrightUtils.MIN_LUX_LIMITI, (float) transX, OppoBrightUtils.MIN_LUX_LIMITI, OppoBrightUtils.MIN_LUX_LIMITI);
        translateAnimation.setInterpolator(this.mOppoBezierInterpolatorTansX);
        translateAnimation.setDuration((long) durationTransX);
        translateAnimation.setStartOffset((long) durationDelayX);
        translateAnimation = new TranslateAnimation(OppoBrightUtils.MIN_LUX_LIMITI, OppoBrightUtils.MIN_LUX_LIMITI, OppoBrightUtils.MIN_LUX_LIMITI, (float) transY);
        translateAnimation.setInterpolator(this.mOppoBezierInterpolatorTansY);
        translateAnimation.setDuration((long) durationTransY);
        Animation alpha = new AlphaAnimation(1.0f, OppoBrightUtils.MIN_LUX_LIMITI);
        alpha.setInterpolator(this.mOppoBezierInterpolatorAlpha);
        alpha.setDuration((long) durationAlpha);
        alpha.setStartOffset((long) delayAlpha);
        AnimationSet set = new AnimationSet(false);
        set.addAnimation(scale);
        set.addAnimation(translateAnimation);
        set.addAnimation(translateAnimation);
        set.addAnimation(alpha);
        return set;
    }

    public void caculateAniAttrs(int startX, int startY, int appWidth, int appHeight) {
        int magnification = SystemProperties.getInt("debug.animation.mag", 1);
        durationDelayX = SystemProperties.getInt("debug.animation.delayx", 0);
        if (SystemProperties.getInt("debug.animation.enable", 0) >= 1) {
            parseAnmation(1, SystemProperties.get("debug.animation.transx", ""));
            parseAnmation(2, SystemProperties.get("debug.animation.transy", ""));
            parseAnmation(3, SystemProperties.get("debug.animation.scale", ""));
            parseAnmation(4, SystemProperties.get("debug.animation.alpha", ""));
            parseAnmation(5, SystemProperties.get("debug.animation.enteralpha", ""));
        }
        int deltaX = 0;
        int deltaY = 0;
        if (appWidth >= 1080 && (startX <= 36 || startX >= 792)) {
            deltaX = 1;
        }
        if (appHeight >= 2016) {
            if (startY <= 78 || startY >= 1758) {
                deltaY = 3;
            } else if (startY <= 351 || startY >= 1443) {
                deltaY = 2;
            } else if (startY <= 624 || startY >= 1170) {
                deltaY = 1;
            }
        }
        durationTransX = (TRANS_X_BASE_T + ((TRANS_X_DELTA_T / DELTA_NUM) * (deltaX + deltaY))) * magnification;
        durationTransY = (TRANS_Y_BASE_T + ((TRANS_Y_DELTA_T / DELTA_NUM) * (deltaX + deltaY))) * magnification;
        durationScale = (SCALE_BASE_T + ((SCALE_DELTA_T / DELTA_NUM) * (deltaX + deltaY))) * magnification;
        durationAlpha = (ALPHA_BASE_T + ((ALPHA_DELTA_T / DELTA_NUM) * (deltaX + deltaY))) * magnification;
        delayAlpha = (ALPHA_DELAY_BASE_T + ((ALPHA_DELAY_DELTA_T / DELTA_NUM) * (deltaX + deltaY))) * magnification;
        durationEnter = (ENTER_BASE_T + ((ENTER_DELTA_T / DELTA_NUM) * (deltaX + deltaY))) * magnification;
        if (WindowManagerDebugConfig.DEBUG_ANIM) {
            Slog.v(TAG, "caculateAniAttrs mIsHeteromorphism=" + this.mIsHeteromorphism);
            Slog.v(TAG, "caculateAniAttrs deltaX=" + deltaX + " deltaY=" + deltaY);
            Slog.v(TAG, "caculateAniAttrs durationTransX=" + durationTransX + " durationTransY=" + durationTransY);
            Slog.v(TAG, "caculateAniAttrs durationScale=" + durationScale + " durationAlpha=" + durationAlpha + " delayAlpha=" + delayAlpha + " durationEnter=" + durationEnter);
        }
    }

    public void parseAnmation(int aniType, String str) {
        if (str == null || str.length() <= 0) {
            Slog.v(TAG, "parseAnmation str null aniType=" + aniType);
            return;
        }
        String[] params = str.split(",");
        if (params.length < 6) {
            Slog.v(TAG, "parseAnmation params error length = " + params.length + " aniType=" + aniType);
            return;
        }
        float f1;
        float f2;
        float f3;
        float f4;
        int duration;
        int deltaT;
        switch (aniType) {
            case 1:
                f1 = parseFloat(params[0]);
                f2 = parseFloat(params[1]);
                f3 = parseFloat(params[2]);
                f4 = parseFloat(params[3]);
                duration = Integer.parseInt(params[4]);
                deltaT = Integer.parseInt(params[5]);
                Slog.v(TAG, "caculateAniAttrs 1 f1=" + f1 + " f2=" + f2 + " f3=" + f3 + " f4=" + f4 + " duration=" + duration + " deltaT=" + deltaT);
                this.mOppoBezierInterpolatorTansX = new OppoBezierInterpolator((double) f1, (double) f2, (double) f3, (double) f4, true);
                TRANS_X_BASE_T = duration;
                TRANS_X_DELTA_T = deltaT;
                break;
            case 2:
                f1 = parseFloat(params[0]);
                f2 = parseFloat(params[1]);
                f3 = parseFloat(params[2]);
                f4 = parseFloat(params[3]);
                duration = Integer.parseInt(params[4]);
                deltaT = Integer.parseInt(params[5]);
                Slog.v(TAG, "caculateAniAttrs 2 f1=" + f1 + " f2=" + f2 + " f3=" + f3 + " f4=" + f4 + " duration=" + duration + " deltaT=" + deltaT);
                this.mOppoBezierInterpolatorTansY = new OppoBezierInterpolator((double) f1, (double) f2, (double) f3, (double) f4, true);
                TRANS_Y_BASE_T = duration;
                TRANS_Y_DELTA_T = deltaT;
                break;
            case 3:
                f1 = parseFloat(params[0]);
                f2 = parseFloat(params[1]);
                f3 = parseFloat(params[2]);
                f4 = parseFloat(params[3]);
                duration = Integer.parseInt(params[4]);
                deltaT = Integer.parseInt(params[5]);
                if (params.length > 6) {
                    scaleDst = parseFloat(params[6]);
                }
                Slog.v(TAG, "caculateAniAttrs 3 f1=" + f1 + " f2=" + f2 + " f3=" + f3 + " f4=" + f4 + " duration=" + duration + " deltaT=" + deltaT + " scaleDst=" + scaleDst);
                this.mOppoBezierInterpolatorScale = new OppoBezierInterpolator((double) f1, (double) f2, (double) f3, (double) f4, true);
                SCALE_BASE_T = duration;
                SCALE_DELTA_T = deltaT;
                break;
            case 4:
                if (params.length >= 8) {
                    f1 = parseFloat(params[0]);
                    f2 = parseFloat(params[1]);
                    f3 = parseFloat(params[2]);
                    f4 = parseFloat(params[3]);
                    duration = Integer.parseInt(params[4]);
                    deltaT = Integer.parseInt(params[5]);
                    int delayT = Integer.parseInt(params[6]);
                    int delayDeltaT = Integer.parseInt(params[7]);
                    Slog.v(TAG, "caculateAniAttrs 4 f1=" + f1 + " f2=" + f2 + " f3=" + f3 + " f4=" + f4 + " duration=" + duration + " deltaT=" + deltaT + " delayT=" + delayT + " delayDeltaT=" + delayDeltaT);
                    this.mOppoBezierInterpolatorAlpha = new OppoBezierInterpolator((double) f1, (double) f2, (double) f3, (double) f4, true);
                    ALPHA_BASE_T = duration;
                    ALPHA_DELTA_T = deltaT;
                    ALPHA_DELAY_BASE_T = delayT;
                    ALPHA_DELAY_DELTA_T = delayDeltaT;
                    break;
                }
                return;
            case 5:
                f1 = parseFloat(params[0]);
                f2 = parseFloat(params[1]);
                f3 = parseFloat(params[2]);
                f4 = parseFloat(params[3]);
                duration = Integer.parseInt(params[4]);
                deltaT = Integer.parseInt(params[5]);
                if (params.length > 6) {
                    scaleDstEnter = parseFloat(params[6]);
                }
                Slog.v(TAG, "caculateAniAttrs 5 f1=" + f1 + " f2=" + f2 + " f3=" + f3 + " f4=" + f4 + " duration=" + duration + " deltaT=" + deltaT + " scaleDstEnter=" + scaleDstEnter);
                this.mOppoBezierInterpolatorEnterAlpha = new OppoBezierInterpolator((double) f1, (double) f2, (double) f3, (double) f4, true);
                ENTER_BASE_T = duration;
                ENTER_DELTA_T = deltaT;
                break;
        }
    }

    public float parseFloat(String str) {
        try {
            return Float.parseFloat(str);
        } catch (Exception e) {
            return -1.0f;
        }
    }

    public float[] caculatePosition(float w, float h, float x, float y, float[] position) {
        if (!(this.mWindowWidth == 0 || this.mWindowHeight == 0)) {
            int ph = (int) ((((((h / 2.0f) + y) / ((float) this.mWindowHeight)) * h) * HEIGHTPERSENT) + y);
            position[0] = (float) ((int) (((((((w / 2.0f) + x) / ((float) this.mWindowWidth)) * w) * WIDTHPERSENT) + x) + ((0.39999998f * w) / 2.0f)));
            position[1] = (float) ph;
        }
        if (x < 0.01f && y < 0.01f) {
            position[0] = (float) (this.mWindowWidth / 2);
            position[1] = (float) (this.mWindowHeight / 2);
        }
        float a = (float) Math.sqrt(Math.pow((double) (w / 2.0f), 2.0d) + Math.pow((double) (h / 2.0f), 2.0d));
        float b = (float) Math.sqrt(Math.pow((double) ((w / 2.0f) * 3.0f), 2.0d) + Math.pow((double) ((h / 2.0f) * 5.0f), 2.0d));
        float px = (float) Math.sqrt(Math.pow((double) ((((float) (this.mWindowWidth / 2)) - x) - (w / 2.0f)), 2.0d) + Math.pow((double) ((((float) (this.mWindowHeight / 2)) - y) - (h / 2.0f)), 2.0d));
        if (b - a != OppoBrightUtils.MIN_LUX_LIMITI) {
            position[2] = ((px - a) / (b - a)) * 30.0f;
        }
        return position;
    }

    private Animation createOppoFreeFormAnimationLocked(int transit, boolean enter, int appWidth, int appHeight) {
        Animation alphaAnimation;
        if (WindowManagerDebugConfig.DEBUG_ANIM) {
            Slog.v(TAG, "createOppoFreeFormAnimationLocked transit:" + transit + ", enter:" + enter + ", appWidth:" + appWidth + ", appHeight:" + appHeight);
        }
        AnimationSet set = new AnimationSet(false);
        Animation scaleAnimation = null;
        if (transit == 7 || transit == 9 || transit == 11 || transit == 13) {
            if (enter) {
                alphaAnimation = new AlphaAnimation(1.0f, 1.0f);
                alphaAnimation.setInterpolator(this.mAccelerateInterpolator);
                alphaAnimation.setDuration(0);
            } else {
                scaleAnimation = new ScaleAnimation(1.0f, 0.5f, 1.0f, 0.5f, 1, 0.5f, 1, 0.5f);
                scaleAnimation.setInterpolator(this.mOppoBezierInterpolatorEnter);
                scaleAnimation.setDuration(0);
                alphaAnimation = new AlphaAnimation(1.0f, OppoBrightUtils.MIN_LUX_LIMITI);
                alphaAnimation.setInterpolator(this.mAccelerateInterpolator);
                alphaAnimation.setDuration(0);
            }
        } else if (enter) {
            scaleAnimation = new ScaleAnimation(0.5f, 1.0f, 0.5f, 1.0f, 1, 0.5f, 1, 0.5f);
            scaleAnimation.setInterpolator(this.mOppoBezierInterpolatorEnter);
            scaleAnimation.setDuration(0);
            alphaAnimation = new AlphaAnimation(OppoBrightUtils.MIN_LUX_LIMITI, 1.0f);
            alphaAnimation.setInterpolator(this.mAccelerateInterpolator);
            alphaAnimation.setDuration(0);
        } else {
            alphaAnimation = new AlphaAnimation(1.0f, 1.0f);
            alphaAnimation.setInterpolator(this.mAccelerateInterpolator);
            alphaAnimation.setDuration(0);
        }
        if (scaleAnimation != null) {
            set.addAnimation(scaleAnimation);
        }
        if (alphaAnimation != null) {
            set.addAnimation(alphaAnimation);
        }
        return set;
    }

    Animation createLauncherThumbnailAspectScaleAnimationLocked(Rect appRect, Rect contentInsets, GraphicBuffer thumbnailHeader, int taskId, int uiMode, int orientation) {
        float scaleW;
        float scaleH;
        float fromX;
        float fromY;
        float toX;
        float toY;
        Animation a;
        int thumbWidthI = thumbnailHeader.getWidth();
        if (thumbWidthI <= 0) {
            thumbWidthI = 1;
        }
        float thumbWidth = (float) (thumbWidthI + 12);
        int thumbHeightI = thumbnailHeader.getHeight();
        if (thumbHeightI <= 0) {
            thumbHeightI = 1;
        }
        float thumbHeight = (float) (thumbHeightI + 12);
        int appWidth = appRect.width();
        int appHeight = appRect.height();
        getNextAppTransitionStartRect(taskId, this.mTmpRect);
        if (OppoScreenDragUtil.isOffsetState()) {
            float dragScale = OppoScreenDragUtil.getScale();
            float offsetX = (float) OppoScreenDragUtil.getOffsetX();
            float offsetY = (1.0f - dragScale) * ((float) appHeight);
            scaleW = (((float) appWidth) / thumbWidth) * dragScale;
            scaleH = (((float) appHeight) / thumbHeight) * dragScale;
            fromX = (((float) this.mTmpRect.left) * dragScale) + offsetX;
            fromY = (((float) this.mTmpRect.top) * dragScale) + offsetY;
            toX = (((float) appRect.left) * dragScale) + offsetX;
            toY = (((float) appRect.top) * dragScale) + offsetY;
        } else {
            scaleW = ((float) appWidth) / thumbWidth;
            scaleH = ((float) appHeight) / thumbHeight;
            fromX = (float) this.mTmpRect.left;
            fromY = (float) this.mTmpRect.top;
            toX = (float) appRect.left;
            toY = (float) appRect.top;
        }
        Animation alphaAnimation;
        Animation translate;
        if (this.mNextAppTransitionScaleUp) {
            Animation scale = new ScaleAnimation(1.0f, scaleW, 1.0f, scaleH, OppoBrightUtils.MIN_LUX_LIMITI, OppoBrightUtils.MIN_LUX_LIMITI);
            scale.setInterpolator(this.mOppoBezierInterpolatorEnterNew);
            scale.setDuration(340);
            alphaAnimation = new AlphaAnimation(1.0f, OppoBrightUtils.MIN_LUX_LIMITI);
            alphaAnimation.setInterpolator(this.mOppoBezierInterpolatorEnterAlphaNew);
            alphaAnimation.setDuration(100);
            translate = createCurvedMotion(fromX, toX, fromY, toY);
            translate.setInterpolator(this.mOppoBezierInterpolatorEnterNew);
            translate.setDuration(340);
            alphaAnimation = new AnimationSet(false);
            alphaAnimation.addAnimation(scale);
            alphaAnimation.addAnimation(alphaAnimation);
            alphaAnimation.addAnimation(translate);
            a = alphaAnimation;
        } else {
            Animation scaleAnimation = new ScaleAnimation(scaleW, 1.0f, scaleH, 1.0f, OppoBrightUtils.MIN_LUX_LIMITI, OppoBrightUtils.MIN_LUX_LIMITI);
            scaleAnimation.setInterpolator(this.mOppoBezierInterpolatorEnterNew);
            scaleAnimation.setDuration(340);
            alphaAnimation = new AlphaAnimation(OppoBrightUtils.MIN_LUX_LIMITI, 1.0f);
            alphaAnimation.setInterpolator(this.mOppoBezierInterpolatorEnterAlphaNew);
            alphaAnimation.setDuration(150);
            translate = createCurvedMotion(toX, fromX, toY, fromY);
            translate.setInterpolator(this.mOppoBezierInterpolatorEnterNew);
            translate.setDuration(340);
            alphaAnimation = new AnimationSet(false);
            alphaAnimation.addAnimation(scaleAnimation);
            alphaAnimation.addAnimation(alphaAnimation);
            alphaAnimation.addAnimation(translate);
            a = alphaAnimation;
        }
        a.setZAdjustment(1);
        return prepareThumbnailAnimationWithDuration(a, appWidth, appHeight, 0, null);
    }

    Animation loadAnimationAttr(LayoutParams lp, int animAttr) {
        if (!this.mService.mPolicy.isGestureAnimSupport() || this.mOpenAtoken == null || !this.mOpenAtoken.toString().contains("com.oppo.launcher/.Launcher") || !this.mUserGestureAnim) {
            return super.loadAnimationAttr(lp, animAttr);
        }
        if (WindowManagerDebugConfig.DEBUG_ANIM) {
            Slog.w(TAG, "OppoAppTransition  loadAnimationAttr *** " + this.mOpenAtoken + "\n" + this.mCloseAtoken);
        }
        return null;
    }

    Animation createLauncherThumbnailEnterExitAnimationLocked(int thumbTransitState, Rect containingFrame, int transit, int taskId) {
        if (this.mCloseAtoken == null || !this.mCloseAtoken.toString().contains("com.oppo.launcher/.Launcher")) {
            return null;
        }
        Animation a;
        if (this.mOpenAtoken != null) {
            WindowState topWindow = this.mOpenAtoken.getTopWindow();
            if (topWindow != null && topWindow.toString().contains("Splash Screen") && ((topWindow.getAttrs().format == -2 || topWindow.getAttrs().format == -3) && thumbTransitState == 1)) {
                return null;
            }
        }
        int appWidth = containingFrame.width();
        int appHeight = containingFrame.height();
        GraphicBuffer thumbnailHeader = getAppTransitionThumbnailHeader(taskId);
        getDefaultNextAppTransitionStartRect(this.mTmpRect);
        int thumbWidthI = thumbnailHeader != null ? thumbnailHeader.getWidth() : appWidth;
        float thumbWidth = (float) (thumbWidthI > 0 ? thumbWidthI / 2 : 1);
        int thumbHeightI = thumbnailHeader != null ? thumbnailHeader.getHeight() : appHeight;
        float thumbHeight = (float) (thumbHeightI > 0 ? thumbHeightI / 2 : 1);
        switch (thumbTransitState) {
            case 0:
                float scaleW = thumbWidth / ((float) appWidth);
                float scaleH = thumbHeight / ((float) appHeight);
                a = new ScaleAnimation(scaleW, 1.0f, scaleH, 1.0f, AppTransition.computePivot(((int) (thumbWidth > 1.0f ? thumbWidth / 2.0f : OppoBrightUtils.MIN_LUX_LIMITI)) + this.mTmpRect.left, scaleW), AppTransition.computePivot(((int) (thumbHeight > 1.0f ? thumbHeight / 2.0f : OppoBrightUtils.MIN_LUX_LIMITI)) + this.mTmpRect.top, scaleH));
                break;
            case 1:
                if (transit != 14) {
                    Animation scaleAnimation = new ScaleAnimation(1.0f, 0.9f, 1.0f, 0.9f, (float) (appWidth / 2), (float) (appHeight / 2));
                    break;
                }
                a = new AlphaAnimation(1.0f, OppoBrightUtils.MIN_LUX_LIMITI);
                break;
            default:
                return null;
        }
        return prepareThumbnailAnimationWithDuration(a, appWidth, appHeight, 340, this.mOppoBezierInterpolatorEnterNew);
    }

    private void initSpecilaAppWindows() {
        this.mSpecialAppWindows.add("com.coloros.safecenter/.privacy.view.password");
        this.mSpecialAppWindows.add("com.android.settings/.FallbackHome");
        this.mSpecialAppWindows.add("com.coloros.recents/.SplitChooserActivity");
    }

    private boolean checkSpecilaAppWindowToken(AppWindowToken appWindowToken) {
        if (this.mService.getDockedStackSide() != -1) {
            return true;
        }
        if (appWindowToken != null) {
            if (appWindowToken.toString().contains(INCALLUI_PACKAGE_NAME)) {
                TelephonyManager telephonyManager = (TelephonyManager) this.mContext.getSystemService("phone");
                boolean isIdle = true;
                if (telephonyManager != null) {
                    isIdle = telephonyManager.isIdle();
                }
                return isIdle;
            } else {
                for (String item : this.mSpecialAppWindows) {
                    if (appWindowToken.toString().contains(item)) {
                        return true;
                    }
                }
                if (appWindowToken.containsShowWhenLockedWindow()) {
                    return true;
                }
            }
        }
        return false;
    }
}
