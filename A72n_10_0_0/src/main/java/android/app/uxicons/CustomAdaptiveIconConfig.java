package android.app.uxicons;

import android.content.res.Resources;
import android.graphics.Path;

public class CustomAdaptiveIconConfig {
    public static final int COLOR_ADAPTIVE_MASK_SIZE = 150;
    private int mCustomIconFgSize;
    private int mCustomIconSize;
    private Path mCustomMask;
    private int mDefaultIconSize;
    private float mForegroundScalePercent;
    private boolean mIsAdaptiveIconDrawable;
    private boolean mIsPlatformDrawable;
    private float mScalePercent;

    public static class Builder {
        private CustomAdaptiveIconConfig mConfig;

        public Builder(Resources res) {
            this.mConfig = new CustomAdaptiveIconConfig(res);
        }

        public Builder setCustomIconSize(int customIconSize) {
            this.mConfig.mCustomIconSize = customIconSize;
            CustomAdaptiveIconConfig customAdaptiveIconConfig = this.mConfig;
            customAdaptiveIconConfig.mScalePercent = (((float) customIconSize) * 1.0f) / ((float) customAdaptiveIconConfig.getDefaultIconSize());
            return this;
        }

        public Builder setCustomIconFgSize(int customIconFgSize) {
            this.mConfig.mCustomIconFgSize = customIconFgSize;
            CustomAdaptiveIconConfig customAdaptiveIconConfig = this.mConfig;
            customAdaptiveIconConfig.mForegroundScalePercent = (((float) customIconFgSize) * 1.0f) / ((float) customAdaptiveIconConfig.getDefaultIconSize());
            return this;
        }

        public Builder setCustomMask(Path customMask) {
            this.mConfig.mCustomMask = customMask;
            return this;
        }

        public Builder setIsPlatformDrawable(boolean isPlatformDrawable) {
            this.mConfig.mIsPlatformDrawable = isPlatformDrawable;
            return this;
        }

        public Builder setIsAdaptiveIconDrawable(boolean isAdaptiveIconDrawable) {
            this.mConfig.mIsAdaptiveIconDrawable = isAdaptiveIconDrawable;
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
