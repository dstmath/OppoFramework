package com.android.server.wm;

import android.graphics.Rect;
import android.os.Debug;
import android.util.ArrayMap;
import android.util.Slog;
import android.util.TypedValue;
import com.android.server.display.OppoBrightUtils;
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
class DimLayerController {
    private static final float DEFAULT_DIM_AMOUNT_DEAD_WINDOW = 0.5f;
    private static final int DEFAULT_DIM_DURATION = 200;
    private static final String TAG = null;
    private static final String TAG_LOCAL = "DimLayerController";
    private DisplayContent mDisplayContent;
    private DimLayer mSharedFullScreenDimLayer;
    private ArrayMap<DimLayerUser, DimLayerState> mState;
    private Rect mTmpBounds;

    private static class DimLayerState {
        WindowStateAnimator animator;
        boolean continueDimming;
        boolean dimAbove;
        DimLayer dimLayer;

        /* synthetic */ DimLayerState(DimLayerState dimLayerState) {
            this();
        }

        private DimLayerState() {
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.wm.DimLayerController.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.wm.DimLayerController.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.wm.DimLayerController.<clinit>():void");
    }

    DimLayerController(DisplayContent displayContent) {
        this.mState = new ArrayMap();
        this.mTmpBounds = new Rect();
        this.mDisplayContent = displayContent;
    }

    void updateDimLayer(DimLayerUser dimLayerUser) {
        DimLayer newDimLayer;
        DimLayerState state = getOrCreateDimLayerState(dimLayerUser);
        boolean previousFullscreen = state.dimLayer != null ? state.dimLayer == this.mSharedFullScreenDimLayer : false;
        int displayId = this.mDisplayContent.getDisplayId();
        if (!dimLayerUser.dimFullscreen()) {
            if (state.dimLayer == null || previousFullscreen) {
                newDimLayer = new DimLayer(this.mDisplayContent.mService, dimLayerUser, displayId, getDimLayerTag(dimLayerUser));
            } else {
                newDimLayer = state.dimLayer;
            }
            dimLayerUser.getDimBounds(this.mTmpBounds);
            newDimLayer.setBounds(this.mTmpBounds);
        } else if (!previousFullscreen || this.mSharedFullScreenDimLayer == null) {
            newDimLayer = this.mSharedFullScreenDimLayer;
            if (newDimLayer == null) {
                if (state.dimLayer != null) {
                    newDimLayer = state.dimLayer;
                } else {
                    newDimLayer = new DimLayer(this.mDisplayContent.mService, dimLayerUser, displayId, getDimLayerTag(dimLayerUser));
                }
                dimLayerUser.getDimBounds(this.mTmpBounds);
                newDimLayer.setBounds(this.mTmpBounds);
                this.mSharedFullScreenDimLayer = newDimLayer;
            } else if (state.dimLayer != null) {
                state.dimLayer.destroySurface();
            }
        } else {
            this.mSharedFullScreenDimLayer.setBoundsForFullscreen();
            return;
        }
        state.dimLayer = newDimLayer;
        if (WindowManagerDebugConfig.DEBUG_DIM_LAYER) {
            Slog.v(TAG, "updateDimLayer, dimLayerUser= " + dimLayerUser.toShortString() + " state.dimLayer " + state.dimLayer + " " + Debug.getCallers(8));
        }
    }

    private static String getDimLayerTag(DimLayerUser dimLayerUser) {
        return "DimLayerController/" + dimLayerUser.toShortString();
    }

    private DimLayerState getOrCreateDimLayerState(DimLayerUser dimLayerUser) {
        if (WindowManagerDebugConfig.DEBUG_DIM_LAYER) {
            Slog.v(TAG, "getOrCreateDimLayerState, dimLayerUser=" + dimLayerUser.toShortString());
        }
        DimLayerState state = (DimLayerState) this.mState.get(dimLayerUser);
        if (state != null) {
            return state;
        }
        state = new DimLayerState();
        this.mState.put(dimLayerUser, state);
        return state;
    }

    private void setContinueDimming(DimLayerUser dimLayerUser) {
        DimLayerState state = (DimLayerState) this.mState.get(dimLayerUser);
        if (state == null) {
            if (WindowManagerDebugConfig.DEBUG_DIM_LAYER) {
                Slog.w(TAG, "setContinueDimming, no state for: " + dimLayerUser.toShortString());
            }
            return;
        }
        state.continueDimming = true;
    }

    boolean isDimming() {
        for (int i = this.mState.size() - 1; i >= 0; i--) {
            DimLayerState state = (DimLayerState) this.mState.valueAt(i);
            if (state.dimLayer != null && state.dimLayer.isDimming()) {
                return true;
            }
        }
        return false;
    }

    void resetDimming() {
        for (int i = this.mState.size() - 1; i >= 0; i--) {
            ((DimLayerState) this.mState.valueAt(i)).continueDimming = false;
        }
    }

    private boolean getContinueDimming(DimLayerUser dimLayerUser) {
        DimLayerState state = (DimLayerState) this.mState.get(dimLayerUser);
        return state != null ? state.continueDimming : false;
    }

    void startDimmingIfNeeded(DimLayerUser dimLayerUser, WindowStateAnimator newWinAnimator, boolean aboveApp) {
        DimLayerState state = getOrCreateDimLayerState(dimLayerUser);
        state.dimAbove = aboveApp;
        if (WindowManagerDebugConfig.DEBUG_DIM_LAYER) {
            Slog.v(TAG, "startDimmingIfNeeded, dimLayerUser=" + dimLayerUser.toShortString() + " newWinAnimator=" + newWinAnimator + " state.animator=" + state.animator);
        }
        if (!newWinAnimator.getShown()) {
            return;
        }
        if (state.animator == null || !state.animator.getShown() || state.animator.mAnimLayer <= newWinAnimator.mAnimLayer) {
            state.animator = newWinAnimator;
            if (state.animator.mWin.mAppToken != null || dimLayerUser.dimFullscreen()) {
                dimLayerUser.getDimBounds(this.mTmpBounds);
            } else {
                this.mDisplayContent.getLogicalDisplayRect(this.mTmpBounds);
            }
            if (state.dimLayer != null) {
                state.dimLayer.setBounds(this.mTmpBounds);
            } else {
                Slog.e(TAG, "state dimLayer is null,not set bounds " + Debug.getCallers(8));
            }
        }
    }

    void stopDimmingIfNeeded() {
        if (WindowManagerDebugConfig.DEBUG_DIM_LAYER) {
            Slog.v(TAG, "stopDimmingIfNeeded, mState.size()=" + this.mState.size());
        }
        for (int i = this.mState.size() - 1; i >= 0; i--) {
            stopDimmingIfNeeded((DimLayerUser) this.mState.keyAt(i));
        }
    }

    private void stopDimmingIfNeeded(DimLayerUser dimLayerUser) {
        DimLayerState state = (DimLayerState) this.mState.get(dimLayerUser);
        if (state.dimLayer == null) {
            Slog.e(TAG, "state dimLayer is null,return " + Debug.getCallers(8));
            return;
        }
        if (WindowManagerDebugConfig.DEBUG_DIM_LAYER) {
            Object valueOf;
            String str = TAG;
            StringBuilder append = new StringBuilder().append("stopDimmingIfNeeded, dimLayerUser=").append(dimLayerUser.toShortString()).append(" state.continueDimming=").append(state.continueDimming).append(" state.dimLayer.isDimming=").append(state.dimLayer.isDimming()).append(" mWillReplaceWindow=");
            if (state.animator != null) {
                valueOf = Boolean.valueOf(state.animator.mWin.mWillReplaceWindow);
            } else {
                valueOf = "null";
            }
            Slog.v(str, append.append(valueOf).toString());
        }
        if ((state.animator == null || !state.animator.mWin.mWillReplaceWindow) && !state.continueDimming && state.dimLayer.isDimming()) {
            state.animator = null;
            dimLayerUser.getDimBounds(this.mTmpBounds);
            state.dimLayer.setBounds(this.mTmpBounds);
        }
    }

    boolean animateDimLayers() {
        int fullScreen = -1;
        int fullScreenAndDimming = -1;
        boolean result = false;
        int windowLayer = -1;
        for (int i = this.mState.size() - 1; i >= 0; i--) {
            DimLayerUser user = (DimLayerUser) this.mState.keyAt(i);
            DimLayerState state = (DimLayerState) this.mState.valueAt(i);
            if (user.dimFullscreen() && state.dimLayer == this.mSharedFullScreenDimLayer) {
                fullScreen = i;
                if (((DimLayerState) this.mState.valueAt(i)).continueDimming && state.animator != null && state.animator.mWin != null && state.animator.mWin.mHasSurface && state.animator.mWin.mLayer > windowLayer) {
                    windowLayer = state.animator.mWin.mLayer;
                    fullScreenAndDimming = i;
                }
            } else {
                result |= animateDimLayers(user);
            }
        }
        if (fullScreenAndDimming != -1) {
            return result | animateDimLayers((DimLayerUser) this.mState.keyAt(fullScreenAndDimming));
        }
        if (fullScreen != -1) {
            return result | animateDimLayers((DimLayerUser) this.mState.keyAt(fullScreen));
        }
        return result;
    }

    private boolean animateDimLayers(DimLayerUser dimLayerUser) {
        DimLayerState state = (DimLayerState) this.mState.get(dimLayerUser);
        if (WindowManagerDebugConfig.DEBUG_DIM_LAYER) {
            Slog.v(TAG, "animateDimLayers, dimLayerUser=" + dimLayerUser.toShortString() + " state.animator=" + state.animator + " state.continueDimming=" + state.continueDimming);
        }
        if (state.dimLayer == null) {
            Slog.e(TAG, "state dimLayer is null,return false " + Debug.getCallers(8));
            return false;
        }
        int dimLayer;
        float dimAmount;
        if (state.animator == null) {
            dimLayer = state.dimLayer.getLayer();
            dimAmount = OppoBrightUtils.MIN_LUX_LIMITI;
        } else if (state.dimAbove) {
            dimLayer = state.animator.mAnimLayer + 1;
            dimAmount = 0.5f;
        } else {
            dimLayer = state.animator.mAnimLayer - 1;
            dimAmount = state.animator.mWin.mAttrs.dimAmount;
        }
        float targetAlpha = state.dimLayer.getTargetAlpha();
        if (targetAlpha != dimAmount) {
            if (state.animator == null) {
                state.dimLayer.hide(200);
            } else {
                long duration;
                if (!state.animator.mAnimating || state.animator.mAnimation == null) {
                    duration = 200;
                } else {
                    duration = state.animator.mAnimation.computeDurationHint();
                }
                if (targetAlpha > dimAmount) {
                    duration = getDimLayerFadeDuration(duration);
                }
                state.dimLayer.show(dimLayer, dimAmount, duration);
                if (targetAlpha == OppoBrightUtils.MIN_LUX_LIMITI) {
                    DisplayContent displayContent = this.mDisplayContent;
                    displayContent.pendingLayoutChanges |= 1;
                    this.mDisplayContent.layoutNeeded = true;
                }
            }
        } else if (state.dimLayer.getLayer() != dimLayer) {
            state.dimLayer.setLayer(dimLayer);
        }
        if (state.dimLayer.isAnimating()) {
            if (this.mDisplayContent.mService.okToDisplay()) {
                return state.dimLayer.stepAnimation();
            }
            state.dimLayer.show();
        }
        return false;
    }

    boolean isDimming(DimLayerUser dimLayerUser, WindowStateAnimator winAnimator) {
        boolean z = false;
        DimLayerState state = (DimLayerState) this.mState.get(dimLayerUser);
        if (state == null || state.dimLayer != null) {
            if (state != null && state.animator == winAnimator) {
                z = state.dimLayer.isDimming();
            }
            return z;
        }
        Slog.e(TAG, "state dimLayer is null in isDimming " + Debug.getCallers(8));
        return false;
    }

    private long getDimLayerFadeDuration(long duration) {
        TypedValue tv = new TypedValue();
        this.mDisplayContent.mService.mContext.getResources().getValue(18022400, tv, true);
        if (tv.type == 6) {
            return (long) tv.getFraction((float) duration, (float) duration);
        }
        if (tv.type < 16 || tv.type > 31) {
            return duration;
        }
        return (long) tv.data;
    }

    void close() {
        for (int i = this.mState.size() - 1; i >= 0; i--) {
            DimLayerState state = (DimLayerState) this.mState.valueAt(i);
            if (state.dimLayer != null) {
                state.dimLayer.destroySurface();
            } else {
                Slog.e(TAG, "state dimLayer is null,can not destroy " + Debug.getCallers(8));
            }
        }
        this.mState.clear();
        this.mSharedFullScreenDimLayer = null;
    }

    void removeDimLayerUser(DimLayerUser dimLayerUser) {
        DimLayerState state = (DimLayerState) this.mState.get(dimLayerUser);
        if (state != null) {
            if (!(state.dimLayer == null || state.dimLayer == this.mSharedFullScreenDimLayer)) {
                state.dimLayer.destroySurface();
            }
            if (state.dimLayer == null) {
                Slog.e(TAG, "state dimLayer is null,can not destroy " + Debug.getCallers(8));
            }
            this.mState.remove(dimLayerUser);
        }
    }

    void applyDimBehind(DimLayerUser dimLayerUser, WindowStateAnimator animator) {
        applyDim(dimLayerUser, animator, false);
    }

    void applyDimAbove(DimLayerUser dimLayerUser, WindowStateAnimator animator) {
        applyDim(dimLayerUser, animator, true);
    }

    void applyDim(DimLayerUser dimLayerUser, WindowStateAnimator animator, boolean aboveApp) {
        if (dimLayerUser == null) {
            Slog.e(TAG, "Trying to apply dim layer for: " + this + ", but no dim layer user found.");
            return;
        }
        if (!getContinueDimming(dimLayerUser)) {
            setContinueDimming(dimLayerUser);
            if (!isDimming(dimLayerUser, animator)) {
                if (WindowManagerDebugConfig.DEBUG_DIM_LAYER) {
                    Slog.v(TAG, "Win " + this + " start dimming.");
                }
                startDimmingIfNeeded(dimLayerUser, animator, aboveApp);
            }
        }
    }

    void dump(String prefix, PrintWriter pw) {
        pw.println(prefix + TAG_LOCAL);
        String doubleSpace = "  ";
        String prefixPlusDoubleSpace = prefix + "  ";
        int n = this.mState.size();
        for (int i = 0; i < n; i++) {
            pw.println(prefixPlusDoubleSpace + ((DimLayerUser) this.mState.keyAt(i)).toShortString());
            DimLayerState state = (DimLayerState) this.mState.valueAt(i);
            pw.println(prefixPlusDoubleSpace + "  " + "dimLayer=" + (state.dimLayer == this.mSharedFullScreenDimLayer ? "shared" : state.dimLayer) + ", animator=" + state.animator + ", continueDimming=" + state.continueDimming);
            if (state.dimLayer != null) {
                state.dimLayer.printTo(prefixPlusDoubleSpace + "  ", pw);
            }
        }
    }
}
