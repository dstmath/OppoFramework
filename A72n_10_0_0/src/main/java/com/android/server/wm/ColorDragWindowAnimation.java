package com.android.server.wm;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Slog;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Transformation;
import com.android.internal.R;
import com.android.server.AttributeCache;

public class ColorDragWindowAnimation {
    private static int PER_ASSISTANT_MODE = 2;
    private static int SINGLE_HAND_MODE = 1;
    private static final String TAG = "OppoDragWindowAnimation";
    boolean mAnimating;
    Animation mAnimation;
    Context mContext;
    boolean mHasTransformation;
    private int mMode;
    private int mScreenHeight;
    private int mScreenWidth;
    WindowManagerService mService;
    private final float[] mTmpFloats = new float[9];
    Transformation mTransformation = new Transformation();

    public ColorDragWindowAnimation(Context context, WindowManagerService service) {
        this.mContext = context;
        this.mService = service;
    }

    private void setAnimation() {
        this.mAnimating = false;
        if (!this.mAnimation.isInitialized()) {
            Animation animation = this.mAnimation;
            int i = this.mScreenWidth;
            int i2 = this.mScreenHeight;
            animation.initialize(i, i2, i, i2);
        }
        this.mTransformation.clear();
        this.mHasTransformation = true;
    }

    public boolean stepAnimation(long currentTime) {
        ColorDragWindowHelper.getInstance().setDefaultDisplay();
        if (ColorDragWindowHelper.getInstance().mDefaultDisplayContent.okToAnimate()) {
            Animation animation = this.mAnimation;
            if (animation == null) {
                return false;
            }
            if (!this.mAnimating) {
                animation.setStartTime(currentTime);
            }
            this.mAnimating = true;
            this.mTransformation.clear();
            boolean hasMoreFrames = this.mAnimation.getTransformation(currentTime, this.mTransformation);
            if (this.mMode == PER_ASSISTANT_MODE) {
                this.mTransformation.getMatrix().getValues(this.mTmpFloats);
                ColorDragWindowHelper.getInstance().mMagnificationSpec.offsetY = this.mTmpFloats[5];
                this.mService.applyMagnificationSpecLocked(ColorDragWindowHelper.getInstance().mDefaultDisplayContent.getDisplayId(), ColorDragWindowHelper.getInstance().mMagnificationSpec);
            }
            if (!hasMoreFrames) {
                resetAnimation();
            }
            this.mHasTransformation = hasMoreFrames;
        } else {
            boolean isNop = true;
            if (ColorDragWindowHelper.getInstance().mMagnificationSpec != null) {
                isNop = ColorDragWindowHelper.getInstance().inDragWindowing();
            }
            if (this.mAnimating || this.mAnimation != null || isNop) {
                if (this.mMode == PER_ASSISTANT_MODE) {
                    ColorDragWindowHelper.getInstance().mMagnificationSpec.clear();
                    this.mService.applyMagnificationSpecLocked(ColorDragWindowHelper.getInstance().mDefaultDisplayContent.getDisplayId(), ColorDragWindowHelper.getInstance().mMagnificationSpec);
                    Slog.v(TAG, "okToAnimate is false");
                }
                resetAnimation();
            }
        }
        return this.mHasTransformation;
    }

    public boolean updateAnimationAndDragState(String pkg, int resId, int mode) {
        return updateAnimationAndDragState(pkg, resId, mode, null);
    }

    public boolean updateAnimationAndDragState(String pkg, int resId, int mode, Bundle options) {
        if (mode == PER_ASSISTANT_MODE) {
            this.mAnimation = loadAnimationRes(pkg, resId, options);
        }
        if (this.mAnimation == null) {
            return false;
        }
        this.mMode = mode;
        this.mScreenHeight = this.mService.getDefaultDisplayContentLocked().getDisplayInfo().logicalHeight;
        this.mScreenWidth = this.mService.getDefaultDisplayContentLocked().getDisplayInfo().logicalWidth;
        Slog.v(TAG, "updateAnimationAndDragState mScreenHeight: " + this.mScreenHeight + " ,mScreenWidth: " + this.mScreenWidth);
        setAnimation();
        return true;
    }

    private void resetAnimation() {
        this.mAnimating = false;
        Animation animation = this.mAnimation;
        if (animation != null) {
            animation.cancel();
            this.mAnimation = null;
        }
        this.mHasTransformation = false;
        this.mTransformation.clear();
        ColorDragWindowHelper.getInstance().removeDragWindowAnimation();
    }

    /* access modifiers changed from: protected */
    public Animation loadAnimationRes(String packageName, int resId, Bundle options) {
        AttributeCache.Entry ent;
        Slog.v(TAG, "loadAnimationRes: " + resId);
        Context context = this.mContext;
        if (resId >= 0 && (ent = getCachedAnimations(packageName, resId)) != null) {
            context = ent.context;
            int redirectDensity = options != null ? options.getInt("density", -1) : -1;
            Slog.v(TAG, "loadAnimationRes, redirectDensity=" + redirectDensity);
            if (!(context == null || redirectDensity == -1)) {
                Resources resources = context.getResources();
                Configuration configuration = resources != null ? resources.getConfiguration() : null;
                if (configuration != null) {
                    configuration.densityDpi = redirectDensity;
                    resources.updateConfiguration(configuration, resources.getDisplayMetrics());
                }
            }
        }
        if (resId != 0) {
            try {
                return AnimationUtils.loadAnimation(context, resId);
            } catch (Exception e) {
                Slog.e(TAG, "dragwindow loadAnimationRes error ", e);
            }
        }
        return null;
    }

    private AttributeCache.Entry getCachedAnimations(String packageName, int resId) {
        Slog.v(TAG, "DrawWindow Loading animations: package=" + packageName + " resId=0x" + Integer.toHexString(resId));
        if (packageName == null) {
            return null;
        }
        if ((-16777216 & resId) == 16777216) {
            packageName = "android";
        }
        Slog.v(TAG, "Loading animations: picked package=" + packageName);
        return AttributeCache.instance().get(packageName, resId, R.styleable.WindowAnimation, 0);
    }
}
