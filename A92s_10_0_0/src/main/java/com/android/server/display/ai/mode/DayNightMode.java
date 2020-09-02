package com.android.server.display.ai.mode;

import com.android.server.display.ai.bean.BrightnessPoint;
import com.android.server.display.ai.bean.ModelConfig;
import com.android.server.display.ai.utils.BrightnessConstants;
import com.android.server.display.ai.utils.ColorAILog;
import java.util.Calendar;
import java.util.List;

public class DayNightMode {
    private static final int DAY = 1;
    private static final int DEFAULT = -1;
    private static final int END_HOUR = 1;
    private static final String[] MODE_ARRAY = {"NORMAL", "DAY", "NIGHT"};
    private static final int NIGHT = 2;
    private static final int NORMAL = 0;
    private static final int START_HOUR = 0;
    private static final String TAG = "DayNightMode";
    private final int[] mDayHours;
    private int mLastMode = -1;
    private final float[] mMinBrightnessArray;
    private final int[] mNightHours;

    public DayNightMode(ModelConfig modelConfig) {
        this.mDayHours = modelConfig.getDayHours();
        this.mNightHours = modelConfig.getNightHours();
        this.mMinBrightnessArray = modelConfig.getMinLightInDNM();
    }

    public void updateDayNightMode(List<BrightnessPoint> points) {
        if (points != null && !points.isEmpty()) {
            int mode = getCurrentMode();
            if (this.mLastMode == -1) {
                ColorAILog.d(TAG, "updateDayNightMode switch from default  to " + MODE_ARRAY[mode]);
                updateCenterPointsByMode(mode, points);
            }
            if (mode != this.mLastMode) {
                ColorAILog.d(TAG, "updateDayNightMode switch from " + MODE_ARRAY[this.mLastMode] + " to " + MODE_ARRAY[mode]);
                updateCenterPointsByMode(mode, points);
            }
        }
    }

    private void updateCenterPointsByMode(int mode, List<BrightnessPoint> points) {
        if (mode == 0 || mode == 1) {
            float[] fArr = this.mMinBrightnessArray;
            if (fArr != null && fArr.length > mode) {
                float minBrightness = fArr[mode];
                for (BrightnessPoint point : points) {
                    if (point.ySrc >= minBrightness) {
                        break;
                    }
                    if (BrightnessConstants.sDebugDetailsLux) {
                        ColorAILog.d(TAG, "updateDayNightMode before, point:" + point);
                    }
                    point.y = minBrightness;
                    if (BrightnessConstants.sDebugDetailsLux) {
                        ColorAILog.d(TAG, "updateDayNightMode after, point:" + point);
                    }
                }
            }
        } else if (mode == 2) {
            for (BrightnessPoint point2 : points) {
                if (point2.y <= point2.ySrc) {
                    break;
                }
                if (BrightnessConstants.sDebugDetailsLux) {
                    ColorAILog.d(TAG, "updateDayNightMode before, point:" + point2);
                }
                point2.y = point2.ySrc;
                if (BrightnessConstants.sDebugDetailsLux) {
                    ColorAILog.d(TAG, "updateDayNightMode after, point:" + point2);
                }
            }
        }
        this.mLastMode = mode;
    }

    private int getCurrentMode() {
        int hour = Calendar.getInstance().get(11);
        int[] iArr = this.mDayHours;
        if (hour >= iArr[0] && hour < iArr[1]) {
            return 1;
        }
        if (hour >= this.mNightHours[0] || hour < this.mDayHours[1]) {
            return 2;
        }
        return 0;
    }
}
