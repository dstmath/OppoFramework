package com.android.server.wm;

import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Debug;
import android.os.IRemoteCallback;
import android.util.Slog;
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
import com.android.server.display.OppoBrightUtils;
import com.oppo.util.OppoAnimSynthesisNumber;

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
public class OppoAppTransition extends AppTransition {
    private static final int ANIM_MORE_TIME = 30;
    private static final int DEFAULT_APP_TRANSITION_DURATION = 336;
    private static final long DEFAULT_APP_TRANSITION_DURATION_ENTER = 200;
    private static final long DEFAULT_APP_TRANSITION_DURATION_EXIT = 200;
    private static final float EXITICONSCALEHEIGHT = 0.5f;
    private static final float EXITICONSCALEWIDTH = 0.5f;
    private static final float HEIGHTPERSENT = 0.75f;
    private static final float ICONSCALEHEIGHT = 0.4f;
    private static final float ICONSCALEWIDTH = 0.4f;
    private static final int NEXT_TRANSIT_TYPE_CUSTOM = 1;
    private static final int NEXT_TRANSIT_TYPE_NONE = 0;
    private static final int NEXT_TRANSIT_TYPE_SCALE_UP = 2;
    private static final String TAG = "OppoAppTransition";
    private static final float WIDTHPERSENT = 0.6f;
    private static String apptokenName;
    private static boolean isOppoLauncherEnter;
    private static boolean isOppoLauncherExit;
    private static boolean isOppoRencentExit;
    private boolean isSystemUI;
    private final Interpolator mAccelerateInterpolator;
    private final int mConfigShortAnimTime;
    private final Context mContext;
    private final Interpolator mDecelerateInterpolator;
    private float[] mEnterPosition;
    private float[] mExitPosition;
    private int mNextAppTransitionEnterTemp;
    private int mNextAppTransitionExitTemp;
    private int mNextAppTransitionStartHeight;
    private int mNextAppTransitionStartWidth;
    private int mNextAppTransitionStartX;
    private int mNextAppTransitionStartY;
    private final OppoBezierInterpolator mOppoBezierInterpolatorEnter;
    private final String mOppoRencentPackageName;
    private int windowHeight;
    private int windowWidth;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.wm.OppoAppTransition.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.wm.OppoAppTransition.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.wm.OppoAppTransition.<clinit>():void");
    }

    OppoAppTransition(Context context, WindowManagerService servic) {
        super(context, servic);
        this.windowWidth = 0;
        this.windowHeight = 0;
        this.mEnterPosition = new float[3];
        this.mExitPosition = new float[3];
        this.mOppoRencentPackageName = "com.coloros.recents";
        this.isSystemUI = false;
        this.mContext = context;
        this.mConfigShortAnimTime = context.getResources().getInteger(17694720);
        this.mOppoBezierInterpolatorEnter = new OppoBezierInterpolator(0.20000000298023224d, 0.07999999821186066d, 0.20000000298023224d, 1.0d, true);
        this.mAccelerateInterpolator = new AccelerateInterpolator();
        this.mDecelerateInterpolator = new DecelerateInterpolator(1.2f);
        WindowManager wm = (WindowManager) this.mContext.getSystemService("window");
        Point windowSize = new Point();
        wm.getDefaultDisplay().getSize(windowSize);
        this.windowWidth = windowSize.x;
        this.windowHeight = windowSize.y;
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
                Slog.v(TAG, "OppoAppTransition overridePendingAppTransitionScaleUp  startX = " + startX + "  startY = " + startY + " startWidth= " + startWidth + " startHeight = " + startHeight);
            }
        }
    }

    void overridePendingAppTransition(String packageName, int enterAnim, int exitAnim, IRemoteCallback startedCallback) {
        if (!isTransitionSet() || isOppoRencentExit || this.isSystemUI) {
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
            if (this.mNextAppTransitionPackage.equals("com.coloros.recents")) {
                isOppoRencentExit = true;
            } else if (this.mNextAppTransitionPackage.equals("com.android.systemui")) {
                this.isSystemUI = true;
            }
            if (OppoAnimSynthesisNumber.isSynthesisNumber(enterAnim) && OppoAnimSynthesisNumber.isSynthesisNumber(exitAnim)) {
                this.mNextAppTransitionEnterTemp = enterAnim;
                this.mNextAppTransitionExitTemp = exitAnim;
            }
            if (WindowManagerDebugConfig.DEBUG_ANIM) {
                Slog.v(TAG, "OppoAppTransition overridePendingAppTransition packageName = " + packageName + ", isOppoRencentExit=" + isOppoRencentExit + " isSystemUI " + this.isSystemUI + " enterAnim = " + Integer.toHexString(enterAnim) + " exitAnim = " + Integer.toHexString(exitAnim));
            }
        }
    }

    void clear() {
        if (WindowManagerDebugConfig.DEBUG_ANIM) {
            Slog.v(TAG, "OppoAppTransition clear()");
        }
        this.mNextAppTransitionEnterTemp = 0;
        this.mNextAppTransitionExitTemp = 0;
        isOppoRencentExit = false;
        this.isSystemUI = false;
        super.clear();
    }

    static void setAppWindowTokenLocked(AppWindowToken closeAtoken, AppWindowToken openAtoken) {
        apptokenName = closeAtoken.toString();
        if (openAtoken.toString().contains("com.oppo.launcher/.Launcher")) {
            isOppoLauncherEnter = true;
        } else {
            isOppoLauncherEnter = false;
        }
        if (closeAtoken.toString().contains("com.oppo.launcher/.Launcher")) {
            isOppoLauncherExit = true;
        } else {
            isOppoLauncherExit = false;
        }
        if (WindowManagerDebugConfig.DEBUG_ANIM) {
            Slog.v(TAG, "OppoAppTransition setAppWindowTokenLocked isOppoLauncherEnter =" + isOppoLauncherEnter + " openAtoken = " + openAtoken);
            Slog.v(TAG, "OppoAppTransition setAppWindowTokenLocked isOppoLauncherExit =" + isOppoLauncherExit + " closeAtoken = " + closeAtoken);
        }
    }

    private void reviseCustomExitAnim(int transit) {
        if (isOppoLauncherEnter && this.mNextAppTransitionType == 0 && transit == 13 && this.mNextAppTransitionEnterTemp != 0 && this.mNextAppTransitionExitTemp != 0) {
            this.mNextAppTransitionType = 1;
            this.mNextAppTransitionEnter = this.mNextAppTransitionEnterTemp;
            this.mNextAppTransitionExit = this.mNextAppTransitionExitTemp;
            this.mNextAppTransitionEnterTemp = 0;
            this.mNextAppTransitionExitTemp = 0;
        } else if (isOppoLauncherExit && this.mNextAppTransitionType == 0 && this.mNextAppTransitionEnterTemp != 0 && this.mNextAppTransitionExitTemp != 0) {
            this.mNextAppTransitionType = 2;
        }
    }

    Animation loadAnimation(LayoutParams lp, int transit, boolean enter, int uiMode, int orientation, Rect frame, Rect displayFrame, Rect insets, Rect surfaceInsets, boolean isVoiceInteraction, boolean freeform, int taskId) {
        reviseCustomExitAnim(transit);
        Animation a;
        if (this.mNextAppTransitionType == 1) {
            a = createOppoCustomAnimLocked(this.mNextAppTransitionPackage, enter, frame.width(), frame.height());
            if (!(a == null || apptokenName == null || !apptokenName.contains("cn.kuwo.ui.lockscreen.LockScreenActivity"))) {
                a.scaleCurrentDuration(OppoBrightUtils.MIN_LUX_LIMITI);
            }
            if (!WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS && !WindowManagerDebugConfig.DEBUG_ANIM) {
                return a;
            }
            Slog.v(TAG, "applyAnimation: anim=" + a + " nextAppTransition=ANIM_CUSTOM" + " transit=" + transit + " isEntrance=" + enter + " Callers=" + Debug.getCallers(3));
            return a;
        } else if (this.mNextAppTransitionType != 2) {
            return super.loadAnimation(lp, transit, enter, uiMode, orientation, frame, displayFrame, insets, surfaceInsets, isVoiceInteraction, freeform, taskId);
        } else {
            a = createOppoScaleUpAnimationLocked(transit, enter, frame.width(), frame.height());
            if (!WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS && !WindowManagerDebugConfig.DEBUG_ANIM) {
                return a;
            }
            Slog.v(TAG, "applyAnimation: anim=" + a + " nextAppTransition=ANIM_SCALE_UP" + " transit=" + transit + " isEntrance=" + enter + " Callers=" + Debug.getCallers(3));
            return a;
        }
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
        } else {
            a = createOppoScaleUpExitAnimationLocked(transit, false, appWidth, appHeight, this.mEnterPosition[2]);
        }
        a.setFillAfter(true);
        a.initialize(appWidth, appHeight, appWidth, appHeight);
        return a;
    }

    private Animation createOppoScaleUpExitAnimationLocked(int transit, boolean enter, int appWidth, int appHeight, float time) {
        if (WindowManagerDebugConfig.DEBUG_ANIM) {
            Slog.v(TAG, "createOppoScaleUpExitAnimationLocked isOppoLauncherExit = " + isOppoLauncherExit);
        }
        Animation a;
        if (isOppoLauncherExit) {
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
                    Slog.v(TAG, "OppoAppTransition createOppoCustomAnimLocked  start center");
                }
            }
            if (WindowManagerDebugConfig.DEBUG_ANIM) {
                Slog.v(TAG, "OppoAppTransition createOppoCustomAnimLocked  strartX = " + startX + "  startWidth = " + startWidth + " startY = " + startY + "  startHight = " + startHight);
            }
            this.mExitPosition = caculatePosition((float) this.mNextAppTransitionStartWidth, (float) this.mNextAppTransitionStartHeight, (float) startX, (float) startY, this.mExitPosition);
            Animation a;
            if (enter) {
                a = new AlphaAnimation(1.0f, 1.0f);
                a.setDuration(200);
                a.setDetachWallpaper(true);
                return a;
            }
            a = prepareOPPOScaleUpExitAnimation(this.mExitPosition[0], this.mExitPosition[1]);
            a.setZAdjustment(1);
            return a;
        }
        return loadAnimationRes(this.mNextAppTransitionPackage, enter ? this.mNextAppTransitionEnter : this.mNextAppTransitionExit);
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
        Animation scale = new ScaleAnimation(1.0f, 0.5f, 1.0f, 0.5f, pivotX, pivotY);
        scale.setInterpolator(this.mDecelerateInterpolator);
        scale.setDuration(200);
        Animation alpha = new AlphaAnimation(1.0f, OppoBrightUtils.MIN_LUX_LIMITI);
        alpha.setInterpolator(this.mDecelerateInterpolator);
        alpha.setDuration(160);
        AnimationSet set = new AnimationSet(false);
        set.addAnimation(scale);
        set.addAnimation(alpha);
        return set;
    }

    public float[] caculatePosition(float w, float h, float x, float y, float[] position) {
        if (!(this.windowWidth == 0 || this.windowHeight == 0)) {
            int ph = (int) ((((((h / 2.0f) + y) / ((float) this.windowHeight)) * h) * HEIGHTPERSENT) + y);
            position[0] = (float) ((int) (((((((w / 2.0f) + x) / ((float) this.windowWidth)) * w) * 0.6f) + x) + ((0.39999998f * w) / 2.0f)));
            position[1] = (float) ph;
        }
        if (x < 0.01f && y < 0.01f) {
            position[0] = (float) (this.windowWidth / 2);
            position[1] = (float) (this.windowHeight / 2);
        }
        float a = (float) Math.sqrt(Math.pow((double) (w / 2.0f), 2.0d) + Math.pow((double) (h / 2.0f), 2.0d));
        float b = (float) Math.sqrt(Math.pow((double) ((w / 2.0f) * 3.0f), 2.0d) + Math.pow((double) ((h / 2.0f) * 5.0f), 2.0d));
        float px = (float) Math.sqrt(Math.pow((double) ((((float) (this.windowWidth / 2)) - x) - (w / 2.0f)), 2.0d) + Math.pow((double) ((((float) (this.windowHeight / 2)) - y) - (h / 2.0f)), 2.0d));
        if (b - a != OppoBrightUtils.MIN_LUX_LIMITI) {
            position[2] = ((px - a) / (b - a)) * 30.0f;
        }
        return position;
    }
}
