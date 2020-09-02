package android.app.uxicons;

import android.content.res.Resources;
import android.graphics.Path;

public class CustomAdaptiveIconConfig {
    public static final int COLOR_ADAPTIVE_MASK_SIZE = 150;
    /* access modifiers changed from: private */
    public int mCustomIconFgSize;
    /* access modifiers changed from: private */
    public int mCustomIconSize;
    /* access modifiers changed from: private */
    public Path mCustomMask;
    private int mDefaultIconSize;
    /* access modifiers changed from: private */
    public float mForegroundScalePercent;
    /* access modifiers changed from: private */
    public boolean mIsAdaptiveIconDrawable;
    /* access modifiers changed from: private */
    public boolean mIsPlatformDrawable;
    /* access modifiers changed from: private */
    public float mScalePercent;

    public static class Builder {
        private CustomAdaptiveIconConfig mConfig;

        public Builder(Resources res) {
            this.mConfig = new CustomAdaptiveIconConfig(res);
        }

        public Builder setCustomIconSize(int customIconSize) {
            int unused = this.mConfig.mCustomIconSize = customIconSize;
            CustomAdaptiveIconConfig customAdaptiveIconConfig = this.mConfig;
            float unused2 = customAdaptiveIconConfig.mScalePercent = (((float) customIconSize) * 1.0f) / ((float) customAdaptiveIconConfig.getDefaultIconSize());
            return this;
        }

        public Builder setCustomIconFgSize(int customIconFgSize) {
            int unused = this.mConfig.mCustomIconFgSize = customIconFgSize;
            CustomAdaptiveIconConfig customAdaptiveIconConfig = this.mConfig;
            float unused2 = customAdaptiveIconConfig.mForegroundScalePercent = (((float) customIconFgSize) * 1.0f) / ((float) customAdaptiveIconConfig.getDefaultIconSize());
            return this;
        }

        public Builder setCustomMask(Path customMask) {
            Path unused = this.mConfig.mCustomMask = customMask;
            return this;
        }

        public Builder setIsPlatformDrawable(boolean isPlatformDrawable) {
            boolean unused = this.mConfig.mIsPlatformDrawable = isPlatformDrawable;
            return this;
        }

        public Builder setIsAdaptiveIconDrawable(boolean isAdaptiveIconDrawable) {
            boolean unused = this.mConfig.mIsAdaptiveIconDrawable = isAdaptiveIconDrawable;
            return this;
        }

        public CustomAdaptiveIconConfig create() {
            return this.mConfig;
        }
    }

    private CustomAdaptiveIconConfig(Resources resources) {
        this.mDefaultIconSize = resources.getDimensionPixelSize(201655817);
        int i = this.mDefaultIconSize;
        this.mCustomIconSize = i;
        this.mCustomIconFgSize = i;
        this.mCustomMask = null;
        this.mScalePercent = 1.0f;
        this.mForegroundScalePercent = 1.0f;
        this.mIsPlatformDrawable = false;
        this.mIsAdaptiveIconDrawable = false;
    }

    public int getDefaultIconSize() {
        return this.mDefaultIconSize;
    }

    public Path getCustomMask() {
        return this.mCustomMask;
    }

    public int getCustomIconSize() {
        return this.mCustomIconSize;
    }

    public int getCustomIconFgSize() {
        return this.mCustomIconFgSize;
    }

    public float getScalePercent() {
        return this.mScalePercent;
    }

    public float getForegroundScalePercent() {
        return this.mForegroundScalePercent;
    }

    public boolean getIsPlatformDrawable() {
        return this.mIsPlatformDrawable;
    }

    public boolean getIsAdaptiveIconDrawable() {
        return this.mIsAdaptiveIconDrawable;
    }

    public String toString() {
        return "CustomIconConfig:DefaultIconSize = " + this.mDefaultIconSize + ";CustomIconSize = " + this.mCustomIconSize + ";CustomIconFgSize = " + this.mCustomIconFgSize + ";ScalePercent" + this.mScalePercent + ";ForegroundScalePercent = " + this.mForegroundScalePercent + ";IsPlatformDrawable = " + this.mIsPlatformDrawable + ";IsAdaptiveIconDrawable" + this.mIsAdaptiveIconDrawable;
    }
}
