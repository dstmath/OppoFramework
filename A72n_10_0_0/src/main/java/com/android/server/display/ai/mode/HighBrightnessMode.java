package com.android.server.display.ai.mode;

import com.android.server.display.ai.bean.BrightnessPoint;
import com.android.server.display.ai.bean.ModelConfig;
import com.android.server.display.ai.utils.ColorAILog;

public class HighBrightnessMode {
    private static final int DEFAULT_LEVEL = -1;
    public static final BrightnessPoint NO_NEED_CHANGE_POINT = new BrightnessPoint();
    private static final String TAG = "HighBrightnessMode";
    private final float[] mHbmMaxXs;
    private final float[] mHbmMinXs;
    private final float[] mHbmXs;
    private final float[] mHbmYs;
    private final float mNormalMaxBrightness;

    public HighBrightnessMode(ModelConfig modelConfig) {
        this.mNormalMaxBrightness = modelConfig.getNormalMaxBrightness();
        this.mHbmXs = modelConfig.getHbmXs();
        this.mHbmYs = modelConfig.getHbmYs();
        this.mHbmMinXs = modelConfig.getHbmMinXs();
        this.mHbmMaxXs = modelConfig.getHbmMaxXs();
    }

    public BrightnessPoint getNextPoint(float currentBrightness, float lux, boolean isReset) {
        if (lux < this.mHbmMinXs[0]) {
            return null;
        }
        if (!isReset || currentBrightness <= this.mNormalMaxBrightness) {
            int hbmLevel = -1;
            int length = this.mHbmYs.length;
            for (int i = 0; i < length; i++) {
                if (((int) currentBrightness) >= ((int) this.mHbmYs[i])) {
                    hbmLevel = i;
                }
            }
            if (hbmLevel != -1) {
                ColorAILog.d(TAG, "getNextBrightness, hbmLevel:" + hbmLevel + ", currentBrightness:" + currentBrightness + ", min:" + this.mHbmMinXs[hbmLevel] + ", max:" + this.mHbmMaxXs[hbmLevel] + ", lux:" + lux);
            } else {
                ColorAILog.d(TAG, "getNextBrightness, hbmLevel:" + hbmLevel + ", currentBrightness:" + currentBrightness + ", lux:" + lux);
            }
            if (lux <= this.mHbmXs[0]) {
                ColorAILog.i(TAG, "getNextBrightness, return null, check normal mode.");
                return null;
            }
            float y = 0.0f;
            if (hbmLevel == -1 || lux < this.mHbmMinXs[hbmLevel] || lux > this.mHbmMaxXs[hbmLevel]) {
                int length2 = this.mHbmXs.length;
                for (int i2 = 0; i2 < length2; i2++) {
                    if (lux > this.mHbmXs[i2]) {
                        y = this.mHbmYs[i2];
                    }
                }
                if (y != 0.0f) {
                    return new BrightnessPoint(lux, y);
                }
            }
            ColorAILog.d(TAG, "getNextBrightness, NO_NEED_CHANGE_POINT:" + lux + ", " + currentBrightness);
            return NO_NEED_CHANGE_POINT;
        }
        ColorAILog.i(TAG, "getNextBrightness, isReset:" + lux + ", " + currentBrightness + " to " + this.mNormalMaxBrightness);
        return new BrightnessPoint(lux, this.mNormalMaxBrightness);
    }
}
