package com.android.server.wm;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.os.Trace;
import android.util.Slog;
import android.view.Display;
import android.view.DisplayInfo;
import android.view.Surface;
import android.view.Surface.OutOfResourcesException;
import android.view.SurfaceControl;
import android.view.SurfaceSession;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Transformation;
import com.android.server.display.OppoBrightUtils;
import com.android.server.job.controllers.JobStatus;
import java.io.PrintWriter;

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
class ScreenRotationAnimation {
    static final boolean DEBUG_STATE = false;
    static final boolean DEBUG_TRANSFORMS = false;
    static final int SCREEN_FREEZE_LAYER_BASE = 2010000;
    static final int SCREEN_FREEZE_LAYER_CUSTOM = 2010003;
    static final int SCREEN_FREEZE_LAYER_ENTER = 2010000;
    static final int SCREEN_FREEZE_LAYER_EXIT = 2010002;
    static final int SCREEN_FREEZE_LAYER_SCREENSHOT = 2010001;
    static final String TAG = null;
    static final boolean TWO_PHASE_ANIMATION = false;
    static final boolean USE_CUSTOM_BLACK_FRAME = false;
    boolean mAnimRunning;
    final Context mContext;
    int mCurRotation;
    Rect mCurrentDisplayRect;
    BlackFrame mCustomBlackFrame;
    final DisplayContent mDisplayContent;
    final Transformation mEnterTransformation;
    BlackFrame mEnteringBlackFrame;
    final Matrix mExitFrameFinalMatrix;
    final Transformation mExitTransformation;
    BlackFrame mExitingBlackFrame;
    boolean mFinishAnimReady;
    long mFinishAnimStartTime;
    Animation mFinishEnterAnimation;
    final Transformation mFinishEnterTransformation;
    Animation mFinishExitAnimation;
    final Transformation mFinishExitTransformation;
    Animation mFinishFrameAnimation;
    final Transformation mFinishFrameTransformation;
    boolean mForceDefaultOrientation;
    final Matrix mFrameInitialMatrix;
    final Transformation mFrameTransformation;
    long mHalfwayPoint;
    int mHeight;
    Animation mLastRotateEnterAnimation;
    final Transformation mLastRotateEnterTransformation;
    Animation mLastRotateExitAnimation;
    final Transformation mLastRotateExitTransformation;
    Animation mLastRotateFrameAnimation;
    final Transformation mLastRotateFrameTransformation;
    private boolean mMoreFinishEnter;
    private boolean mMoreFinishExit;
    private boolean mMoreFinishFrame;
    private boolean mMoreRotateEnter;
    private boolean mMoreRotateExit;
    private boolean mMoreRotateFrame;
    private boolean mMoreStartEnter;
    private boolean mMoreStartExit;
    private boolean mMoreStartFrame;
    Rect mOriginalDisplayRect;
    int mOriginalHeight;
    int mOriginalRotation;
    int mOriginalWidth;
    Animation mRotateEnterAnimation;
    final Transformation mRotateEnterTransformation;
    Animation mRotateExitAnimation;
    final Transformation mRotateExitTransformation;
    Animation mRotateFrameAnimation;
    final Transformation mRotateFrameTransformation;
    final Matrix mSnapshotFinalMatrix;
    final Matrix mSnapshotInitialMatrix;
    Animation mStartEnterAnimation;
    final Transformation mStartEnterTransformation;
    Animation mStartExitAnimation;
    final Transformation mStartExitTransformation;
    Animation mStartFrameAnimation;
    final Transformation mStartFrameTransformation;
    boolean mStarted;
    SurfaceControl mSurfaceControl;
    final float[] mTmpFloats;
    final Matrix mTmpMatrix;
    int mWidth;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.wm.ScreenRotationAnimation.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.wm.ScreenRotationAnimation.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.wm.ScreenRotationAnimation.<clinit>():void");
    }

    public void printTo(String prefix, PrintWriter pw) {
        pw.print(prefix);
        pw.print("mSurface=");
        pw.print(this.mSurfaceControl);
        pw.print(" mWidth=");
        pw.print(this.mWidth);
        pw.print(" mHeight=");
        pw.println(this.mHeight);
        pw.print(prefix);
        pw.print("mExitingBlackFrame=");
        pw.println(this.mExitingBlackFrame);
        if (this.mExitingBlackFrame != null) {
            this.mExitingBlackFrame.printTo(prefix + "  ", pw);
        }
        pw.print(prefix);
        pw.print("mEnteringBlackFrame=");
        pw.println(this.mEnteringBlackFrame);
        if (this.mEnteringBlackFrame != null) {
            this.mEnteringBlackFrame.printTo(prefix + "  ", pw);
        }
        pw.print(prefix);
        pw.print("mCurRotation=");
        pw.print(this.mCurRotation);
        pw.print(" mOriginalRotation=");
        pw.println(this.mOriginalRotation);
        pw.print(prefix);
        pw.print("mOriginalWidth=");
        pw.print(this.mOriginalWidth);
        pw.print(" mOriginalHeight=");
        pw.println(this.mOriginalHeight);
        pw.print(prefix);
        pw.print("mStarted=");
        pw.print(this.mStarted);
        pw.print(" mAnimRunning=");
        pw.print(this.mAnimRunning);
        pw.print(" mFinishAnimReady=");
        pw.print(this.mFinishAnimReady);
        pw.print(" mFinishAnimStartTime=");
        pw.println(this.mFinishAnimStartTime);
        pw.print(prefix);
        pw.print("mStartExitAnimation=");
        pw.print(this.mStartExitAnimation);
        pw.print(" ");
        this.mStartExitTransformation.printShortString(pw);
        pw.println();
        pw.print(prefix);
        pw.print("mStartEnterAnimation=");
        pw.print(this.mStartEnterAnimation);
        pw.print(" ");
        this.mStartEnterTransformation.printShortString(pw);
        pw.println();
        pw.print(prefix);
        pw.print("mStartFrameAnimation=");
        pw.print(this.mStartFrameAnimation);
        pw.print(" ");
        this.mStartFrameTransformation.printShortString(pw);
        pw.println();
        pw.print(prefix);
        pw.print("mFinishExitAnimation=");
        pw.print(this.mFinishExitAnimation);
        pw.print(" ");
        this.mFinishExitTransformation.printShortString(pw);
        pw.println();
        pw.print(prefix);
        pw.print("mFinishEnterAnimation=");
        pw.print(this.mFinishEnterAnimation);
        pw.print(" ");
        this.mFinishEnterTransformation.printShortString(pw);
        pw.println();
        pw.print(prefix);
        pw.print("mFinishFrameAnimation=");
        pw.print(this.mFinishFrameAnimation);
        pw.print(" ");
        this.mFinishFrameTransformation.printShortString(pw);
        pw.println();
        pw.print(prefix);
        pw.print("mRotateExitAnimation=");
        pw.print(this.mRotateExitAnimation);
        pw.print(" ");
        this.mRotateExitTransformation.printShortString(pw);
        pw.println();
        pw.print(prefix);
        pw.print("mRotateEnterAnimation=");
        pw.print(this.mRotateEnterAnimation);
        pw.print(" ");
        this.mRotateEnterTransformation.printShortString(pw);
        pw.println();
        pw.print(prefix);
        pw.print("mRotateFrameAnimation=");
        pw.print(this.mRotateFrameAnimation);
        pw.print(" ");
        this.mRotateFrameTransformation.printShortString(pw);
        pw.println();
        pw.print(prefix);
        pw.print("mExitTransformation=");
        this.mExitTransformation.printShortString(pw);
        pw.println();
        pw.print(prefix);
        pw.print("mEnterTransformation=");
        this.mEnterTransformation.printShortString(pw);
        pw.println();
        pw.print(prefix);
        pw.print("mFrameTransformation=");
        this.mEnterTransformation.printShortString(pw);
        pw.println();
        pw.print(prefix);
        pw.print("mFrameInitialMatrix=");
        this.mFrameInitialMatrix.printShortString(pw);
        pw.println();
        pw.print(prefix);
        pw.print("mSnapshotInitialMatrix=");
        this.mSnapshotInitialMatrix.printShortString(pw);
        pw.print(" mSnapshotFinalMatrix=");
        this.mSnapshotFinalMatrix.printShortString(pw);
        pw.println();
        pw.print(prefix);
        pw.print("mExitFrameFinalMatrix=");
        this.mExitFrameFinalMatrix.printShortString(pw);
        pw.println();
        pw.print(prefix);
        pw.print("mForceDefaultOrientation=");
        pw.print(this.mForceDefaultOrientation);
        if (this.mForceDefaultOrientation) {
            pw.print(" mOriginalDisplayRect=");
            pw.print(this.mOriginalDisplayRect.toShortString());
            pw.print(" mCurrentDisplayRect=");
            pw.println(this.mCurrentDisplayRect.toShortString());
        }
    }

    public ScreenRotationAnimation(Context context, DisplayContent displayContent, SurfaceSession session, boolean inTransaction, boolean forceDefaultOrientation, boolean isSecure) {
        int originalWidth;
        int originalHeight;
        this.mOriginalDisplayRect = new Rect();
        this.mCurrentDisplayRect = new Rect();
        this.mStartExitTransformation = new Transformation();
        this.mStartEnterTransformation = new Transformation();
        this.mStartFrameTransformation = new Transformation();
        this.mFinishExitTransformation = new Transformation();
        this.mFinishEnterTransformation = new Transformation();
        this.mFinishFrameTransformation = new Transformation();
        this.mRotateExitTransformation = new Transformation();
        this.mRotateEnterTransformation = new Transformation();
        this.mRotateFrameTransformation = new Transformation();
        this.mLastRotateExitTransformation = new Transformation();
        this.mLastRotateEnterTransformation = new Transformation();
        this.mLastRotateFrameTransformation = new Transformation();
        this.mExitTransformation = new Transformation();
        this.mEnterTransformation = new Transformation();
        this.mFrameTransformation = new Transformation();
        this.mFrameInitialMatrix = new Matrix();
        this.mSnapshotInitialMatrix = new Matrix();
        this.mSnapshotFinalMatrix = new Matrix();
        this.mExitFrameFinalMatrix = new Matrix();
        this.mTmpMatrix = new Matrix();
        this.mTmpFloats = new float[9];
        this.mContext = context;
        this.mDisplayContent = displayContent;
        displayContent.getLogicalDisplayRect(this.mOriginalDisplayRect);
        Display display = displayContent.getDisplay();
        int originalRotation = display.getRotation();
        DisplayInfo displayInfo = displayContent.getDisplayInfo();
        if (forceDefaultOrientation) {
            this.mForceDefaultOrientation = true;
            originalWidth = displayContent.mBaseDisplayWidth;
            originalHeight = displayContent.mBaseDisplayHeight;
        } else {
            originalWidth = displayInfo.logicalWidth;
            originalHeight = displayInfo.logicalHeight;
        }
        if (originalRotation == 1 || originalRotation == 3) {
            this.mWidth = originalHeight;
            this.mHeight = originalWidth;
        } else {
            this.mWidth = originalWidth;
            this.mHeight = originalHeight;
        }
        this.mOriginalRotation = originalRotation;
        this.mOriginalWidth = originalWidth;
        this.mOriginalHeight = originalHeight;
        Trace.traceBegin(32, "ScreenRotationAnimation:Create");
        Slog.i(WindowManagerService.TAG, "  FREEZE " + this.mSurfaceControl + ": CREATE, w = " + this.mWidth + ", h = " + this.mHeight + ", r=" + originalRotation);
        Trace.traceEnd(32);
        this.mDisplayContent.mService.setRotationBoost(true);
        if (!inTransaction) {
            if (WindowManagerDebugConfig.SHOW_LIGHT_TRANSACTIONS) {
                Slog.i("WindowManager", ">>> OPEN TRANSACTION ScreenRotationAnimation");
            }
            SurfaceControl.openTransaction();
        }
        int flags = 4;
        if (isSecure) {
            flags = 132;
        }
        try {
            if (WindowManagerDebugConfig.DEBUG_SURFACE_TRACE) {
                this.mSurfaceControl = new SurfaceTrace(session, "ScreenshotSurface", this.mWidth, this.mHeight, -1, flags);
                Slog.w(TAG, "ScreenRotationAnimation ctor: displayOffset=" + this.mOriginalDisplayRect.toShortString());
            } else {
                this.mSurfaceControl = new SurfaceControl(session, "ScreenshotSurface", this.mWidth, this.mHeight, -1, flags);
            }
            Surface sur = new Surface();
            sur.copyFrom(this.mSurfaceControl);
            SurfaceControl.screenshot(SurfaceControl.getBuiltInDisplay(0), sur);
            this.mSurfaceControl.setLayerStack(display.getLayerStack());
            this.mSurfaceControl.setLayer(SCREEN_FREEZE_LAYER_SCREENSHOT);
            this.mSurfaceControl.setAlpha(OppoBrightUtils.MIN_LUX_LIMITI);
            this.mSurfaceControl.show();
            sur.destroy();
        } catch (OutOfResourcesException e) {
            Slog.w(TAG, "Unable to allocate freeze surface", e);
        } catch (Throwable th) {
            if (!inTransaction) {
                SurfaceControl.closeTransaction();
                if (WindowManagerDebugConfig.SHOW_LIGHT_TRANSACTIONS) {
                    Slog.i("WindowManager", "<<< CLOSE TRANSACTION ScreenRotationAnimation");
                }
            }
        }
        if (WindowManagerDebugConfig.SHOW_TRANSACTIONS || WindowManagerDebugConfig.SHOW_SURFACE_ALLOC) {
            Slog.i("WindowManager", "  FREEZE " + this.mSurfaceControl + ": CREATE");
        }
        setRotationInTransaction(originalRotation);
        if (!inTransaction) {
            SurfaceControl.closeTransaction();
            if (WindowManagerDebugConfig.SHOW_LIGHT_TRANSACTIONS) {
                Slog.i("WindowManager", "<<< CLOSE TRANSACTION ScreenRotationAnimation");
            }
        }
    }

    boolean hasScreenshot() {
        return this.mSurfaceControl != null;
    }

    private void setSnapshotTransformInTransaction(Matrix matrix, float alpha) {
        if (this.mSurfaceControl != null) {
            matrix.getValues(this.mTmpFloats);
            float x = this.mTmpFloats[2];
            float y = this.mTmpFloats[5];
            if (this.mForceDefaultOrientation) {
                this.mDisplayContent.getLogicalDisplayRect(this.mCurrentDisplayRect);
                x -= (float) this.mCurrentDisplayRect.left;
                y -= (float) this.mCurrentDisplayRect.top;
            }
            this.mSurfaceControl.setPosition(x, y);
            this.mSurfaceControl.setMatrix(this.mTmpFloats[0], this.mTmpFloats[3], this.mTmpFloats[1], this.mTmpFloats[4]);
            this.mSurfaceControl.setAlpha(alpha);
            Trace.traceBegin(32, "ScreenRotationAnimation:SetMatrix dsdx=" + this.mTmpFloats[0] + " dtdx=" + this.mTmpFloats[3] + " dsdy=" + this.mTmpFloats[1] + " dtdy=" + this.mTmpFloats[4]);
            Trace.traceEnd(32);
        }
    }

    public static void createRotationMatrix(int rotation, int width, int height, Matrix outMatrix) {
        switch (rotation) {
            case 0:
                outMatrix.reset();
                return;
            case 1:
                outMatrix.setRotate(90.0f, OppoBrightUtils.MIN_LUX_LIMITI, OppoBrightUtils.MIN_LUX_LIMITI);
                outMatrix.postTranslate((float) height, OppoBrightUtils.MIN_LUX_LIMITI);
                return;
            case 2:
                outMatrix.setRotate(180.0f, OppoBrightUtils.MIN_LUX_LIMITI, OppoBrightUtils.MIN_LUX_LIMITI);
                outMatrix.postTranslate((float) width, (float) height);
                return;
            case 3:
                outMatrix.setRotate(270.0f, OppoBrightUtils.MIN_LUX_LIMITI, OppoBrightUtils.MIN_LUX_LIMITI);
                outMatrix.postTranslate(OppoBrightUtils.MIN_LUX_LIMITI, (float) width);
                return;
            default:
                return;
        }
    }

    private void setRotationInTransaction(int rotation) {
        this.mCurRotation = rotation;
        createRotationMatrix(DisplayContent.deltaRotation(rotation, 0), this.mWidth, this.mHeight, this.mSnapshotInitialMatrix);
        setSnapshotTransformInTransaction(this.mSnapshotInitialMatrix, 1.0f);
    }

    public boolean setRotationInTransaction(int rotation, SurfaceSession session, long maxAnimationDuration, float animationScale, int finalWidth, int finalHeight) {
        setRotationInTransaction(rotation);
        return false;
    }

    private boolean startAnimation(SurfaceSession session, long maxAnimationDuration, float animationScale, int finalWidth, int finalHeight, boolean dismissing, int exitAnim, int enterAnim) {
        if (this.mSurfaceControl == null) {
            return false;
        }
        if (this.mStarted) {
            return true;
        }
        boolean customAnim;
        this.mStarted = true;
        int delta = DisplayContent.deltaRotation(this.mCurRotation, this.mOriginalRotation);
        if (exitAnim == 0 || enterAnim == 0) {
            customAnim = false;
            switch (delta) {
                case 0:
                    this.mRotateExitAnimation = AnimationUtils.loadAnimation(this.mContext, 17432688);
                    this.mRotateEnterAnimation = AnimationUtils.loadAnimation(this.mContext, 17432687);
                    break;
                case 1:
                    this.mRotateExitAnimation = AnimationUtils.loadAnimation(this.mContext, 17432700);
                    this.mRotateEnterAnimation = AnimationUtils.loadAnimation(this.mContext, 17432699);
                    break;
                case 2:
                    this.mRotateExitAnimation = AnimationUtils.loadAnimation(this.mContext, 17432691);
                    this.mRotateEnterAnimation = AnimationUtils.loadAnimation(this.mContext, 17432690);
                    break;
                case 3:
                    this.mRotateExitAnimation = AnimationUtils.loadAnimation(this.mContext, 17432697);
                    this.mRotateEnterAnimation = AnimationUtils.loadAnimation(this.mContext, 17432696);
                    break;
            }
        }
        customAnim = true;
        this.mRotateExitAnimation = AnimationUtils.loadAnimation(this.mContext, exitAnim);
        this.mRotateEnterAnimation = AnimationUtils.loadAnimation(this.mContext, enterAnim);
        this.mRotateEnterAnimation.initialize(finalWidth, finalHeight, this.mOriginalWidth, this.mOriginalHeight);
        this.mRotateExitAnimation.initialize(finalWidth, finalHeight, this.mOriginalWidth, this.mOriginalHeight);
        this.mAnimRunning = false;
        this.mFinishAnimReady = false;
        this.mFinishAnimStartTime = -1;
        this.mRotateExitAnimation.restrictDuration(maxAnimationDuration);
        this.mRotateExitAnimation.scaleCurrentDuration(animationScale);
        this.mRotateEnterAnimation.restrictDuration(maxAnimationDuration);
        this.mRotateEnterAnimation.scaleCurrentDuration(animationScale);
        int layerStack = this.mDisplayContent.getDisplay().getLayerStack();
        if (!customAnim && this.mExitingBlackFrame == null) {
            if (WindowManagerDebugConfig.SHOW_LIGHT_TRANSACTIONS) {
                Slog.i("WindowManager", ">>> OPEN TRANSACTION ScreenRotationAnimation.startAnimation");
            }
            SurfaceControl.openTransaction();
            try {
                Rect outer;
                Rect inner;
                createRotationMatrix(delta, this.mOriginalWidth, this.mOriginalHeight, this.mFrameInitialMatrix);
                if (this.mForceDefaultOrientation) {
                    outer = this.mCurrentDisplayRect;
                    inner = this.mOriginalDisplayRect;
                } else {
                    outer = new Rect((-this.mOriginalWidth) * 1, (-this.mOriginalHeight) * 1, this.mOriginalWidth * 2, this.mOriginalHeight * 2);
                    inner = new Rect(0, 0, this.mOriginalWidth, this.mOriginalHeight);
                }
                this.mExitingBlackFrame = new BlackFrame(session, outer, inner, SCREEN_FREEZE_LAYER_EXIT, layerStack, this.mForceDefaultOrientation);
                this.mExitingBlackFrame.setMatrix(this.mFrameInitialMatrix);
                SurfaceControl.closeTransaction();
                if (WindowManagerDebugConfig.SHOW_LIGHT_TRANSACTIONS) {
                    Slog.i("WindowManager", "<<< CLOSE TRANSACTION ScreenRotationAnimation.startAnimation");
                }
            } catch (OutOfResourcesException e) {
                Slog.w(TAG, "Unable to allocate black surface", e);
                SurfaceControl.closeTransaction();
                if (WindowManagerDebugConfig.SHOW_LIGHT_TRANSACTIONS) {
                    Slog.i("WindowManager", "<<< CLOSE TRANSACTION ScreenRotationAnimation.startAnimation");
                }
            } catch (Throwable th) {
                SurfaceControl.closeTransaction();
                if (WindowManagerDebugConfig.SHOW_LIGHT_TRANSACTIONS) {
                    Slog.i("WindowManager", "<<< CLOSE TRANSACTION ScreenRotationAnimation.startAnimation");
                }
            }
        }
        if (customAnim && this.mEnteringBlackFrame == null) {
            if (WindowManagerDebugConfig.SHOW_LIGHT_TRANSACTIONS) {
                Slog.i("WindowManager", ">>> OPEN TRANSACTION ScreenRotationAnimation.startAnimation");
            }
            SurfaceControl.openTransaction();
            try {
                this.mEnteringBlackFrame = new BlackFrame(session, new Rect((-finalWidth) * 1, (-finalHeight) * 1, finalWidth * 2, finalHeight * 2), new Rect(0, 0, finalWidth, finalHeight), 2010000, layerStack, false);
                SurfaceControl.closeTransaction();
                if (WindowManagerDebugConfig.SHOW_LIGHT_TRANSACTIONS) {
                    Slog.i("WindowManager", "<<< CLOSE TRANSACTION ScreenRotationAnimation.startAnimation");
                }
            } catch (OutOfResourcesException e2) {
                Slog.w(TAG, "Unable to allocate black surface", e2);
                SurfaceControl.closeTransaction();
                if (WindowManagerDebugConfig.SHOW_LIGHT_TRANSACTIONS) {
                    Slog.i("WindowManager", "<<< CLOSE TRANSACTION ScreenRotationAnimation.startAnimation");
                }
            } catch (Throwable th2) {
                SurfaceControl.closeTransaction();
                if (WindowManagerDebugConfig.SHOW_LIGHT_TRANSACTIONS) {
                    Slog.i("WindowManager", "<<< CLOSE TRANSACTION ScreenRotationAnimation.startAnimation");
                }
            }
        }
        return true;
    }

    public boolean dismiss(SurfaceSession session, long maxAnimationDuration, float animationScale, int finalWidth, int finalHeight, int exitAnim, int enterAnim) {
        Trace.traceBegin(32, "ScreenRotationAnimation:Begin");
        Slog.v(WindowManagerService.TAG, "Dismiss!");
        Trace.traceEnd(32);
        if (this.mSurfaceControl == null) {
            return false;
        }
        if (!this.mStarted) {
            startAnimation(session, maxAnimationDuration, animationScale, finalWidth, finalHeight, true, exitAnim, enterAnim);
        }
        if (!this.mStarted) {
            return false;
        }
        this.mFinishAnimReady = true;
        return true;
    }

    public void kill() {
        if (this.mSurfaceControl != null) {
            if (WindowManagerDebugConfig.SHOW_TRANSACTIONS || WindowManagerDebugConfig.SHOW_SURFACE_ALLOC) {
                Slog.i("WindowManager", "  FREEZE " + this.mSurfaceControl + ": DESTROY");
            }
            Trace.traceBegin(32, "ScreenRotationAnimation:End");
            Slog.i(WindowManagerService.TAG, "  FREEZE " + this.mSurfaceControl + ": DESTROY");
            Trace.traceEnd(32);
            this.mSurfaceControl.destroy();
            this.mSurfaceControl = null;
        }
        if (this.mCustomBlackFrame != null) {
            this.mCustomBlackFrame.kill();
            this.mCustomBlackFrame = null;
        }
        if (this.mExitingBlackFrame != null) {
            this.mExitingBlackFrame.kill();
            this.mExitingBlackFrame = null;
        }
        if (this.mEnteringBlackFrame != null) {
            this.mEnteringBlackFrame.kill();
            this.mEnteringBlackFrame = null;
        }
        if (this.mRotateExitAnimation != null) {
            this.mRotateExitAnimation.cancel();
            this.mRotateExitAnimation = null;
        }
        if (this.mRotateEnterAnimation != null) {
            this.mRotateEnterAnimation.cancel();
            this.mRotateEnterAnimation = null;
        }
        this.mDisplayContent.mService.setRotationBoost(false);
    }

    public boolean isAnimating() {
        return hasAnimations();
    }

    public boolean isRotating() {
        return this.mCurRotation != this.mOriginalRotation;
    }

    private boolean hasAnimations() {
        return (this.mRotateEnterAnimation == null && this.mRotateExitAnimation == null) ? false : true;
    }

    private boolean stepAnimation(long now) {
        if (now > this.mHalfwayPoint) {
            this.mHalfwayPoint = JobStatus.NO_LATEST_RUNTIME;
        }
        if (this.mFinishAnimReady && this.mFinishAnimStartTime < 0) {
            this.mFinishAnimStartTime = now;
        }
        if (this.mFinishAnimReady) {
            long finishNow = now - this.mFinishAnimStartTime;
        }
        this.mMoreRotateExit = false;
        if (this.mRotateExitAnimation != null) {
            this.mMoreRotateExit = this.mRotateExitAnimation.getTransformation(now, this.mRotateExitTransformation);
        }
        this.mMoreRotateEnter = false;
        if (this.mRotateEnterAnimation != null) {
            this.mMoreRotateEnter = this.mRotateEnterAnimation.getTransformation(now, this.mRotateEnterTransformation);
        }
        if (!(this.mMoreRotateExit || this.mRotateExitAnimation == null)) {
            this.mRotateExitAnimation.cancel();
            this.mRotateExitAnimation = null;
            this.mRotateExitTransformation.clear();
        }
        if (!(this.mMoreRotateEnter || this.mRotateEnterAnimation == null)) {
            this.mRotateEnterAnimation.cancel();
            this.mRotateEnterAnimation = null;
            this.mRotateEnterTransformation.clear();
        }
        this.mExitTransformation.set(this.mRotateExitTransformation);
        this.mEnterTransformation.set(this.mRotateEnterTransformation);
        boolean more = (this.mMoreRotateEnter || this.mMoreRotateExit) ? true : !this.mFinishAnimReady;
        this.mSnapshotFinalMatrix.setConcat(this.mExitTransformation.getMatrix(), this.mSnapshotInitialMatrix);
        return more;
    }

    void updateSurfacesInTransaction() {
        if (this.mStarted) {
            if (!(this.mSurfaceControl == null || this.mMoreStartExit || this.mMoreFinishExit || this.mMoreRotateExit)) {
                this.mSurfaceControl.hide();
            }
            if (this.mCustomBlackFrame != null) {
                if (this.mMoreStartFrame || this.mMoreFinishFrame || this.mMoreRotateFrame) {
                    this.mCustomBlackFrame.setMatrix(this.mFrameTransformation.getMatrix());
                } else {
                    this.mCustomBlackFrame.hide();
                }
            }
            if (this.mExitingBlackFrame != null) {
                if (this.mMoreStartExit || this.mMoreFinishExit || this.mMoreRotateExit) {
                    this.mExitFrameFinalMatrix.setConcat(this.mExitTransformation.getMatrix(), this.mFrameInitialMatrix);
                    this.mExitingBlackFrame.setMatrix(this.mExitFrameFinalMatrix);
                    if (this.mForceDefaultOrientation) {
                        this.mExitingBlackFrame.setAlpha(this.mExitTransformation.getAlpha());
                    }
                } else {
                    this.mExitingBlackFrame.hide();
                }
            }
            if (this.mEnteringBlackFrame != null) {
                if (this.mMoreStartEnter || this.mMoreFinishEnter || this.mMoreRotateEnter) {
                    this.mEnteringBlackFrame.setMatrix(this.mEnterTransformation.getMatrix());
                } else {
                    this.mEnteringBlackFrame.hide();
                }
            }
            setSnapshotTransformInTransaction(this.mSnapshotFinalMatrix, this.mExitTransformation.getAlpha());
        }
    }

    public boolean stepAnimationLocked(long now) {
        if (hasAnimations()) {
            if (!this.mAnimRunning) {
                if (this.mRotateEnterAnimation != null) {
                    this.mRotateEnterAnimation.setStartTime(now);
                }
                if (this.mRotateExitAnimation != null) {
                    this.mRotateExitAnimation.setStartTime(now);
                }
                this.mAnimRunning = true;
                this.mHalfwayPoint = (this.mRotateEnterAnimation.getDuration() / 2) + now;
            }
            return stepAnimation(now);
        }
        this.mFinishAnimReady = false;
        return false;
    }

    public Transformation getEnterTransformation() {
        return this.mEnterTransformation;
    }
}
