package com.android.server.display.ai.bean;

import android.text.TextUtils;
import android.util.Slog;
import com.android.server.BatteryService;
import com.android.server.display.OppoBrightUtils;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ModelConfig {
    private static final String TAG = "ModelConfig";
    private float darkLuxThreshold;
    private int[] dayHours;
    private float defaultBrightness;
    private float deltaDownScale;
    private float deltaUpScale;
    private String device;
    private float dragExpandMultipleLeft;
    private float dragExpandMultipleRight;
    private int frameDuration;
    private float[] hbmMaxXs;
    private float[] hbmMinXs;
    private float[] hbmXs;
    private float[] hbmYs;
    private float leftScaleInDarkEnv;
    private float maxBrightnessChange;
    private float minBrightnessChange;
    private float[] minLightInDNM;
    private int[] nightHours;
    private float normalMaxBrightness;
    private float reviseX;
    private float reviseXChangePoint;
    private float reviseXMultiple;
    private float rightScaleInDarkEnv;
    private float speedMultipleInDistance;
    private HashMap<String, SplineModel> splineModelHashMap;
    private int splinePointSize;
    private float stableDownMinPercent;
    private float stableDownSelfPercent;
    private float stableRightMinLux;
    private float stableSmallChangePercent;
    private float stableSmallChangeScaleInDarkEnv;
    private float stableSmallChangeTargetPercent;
    private float stableSmallRightMinLux;
    private float stableUpMinPercent;
    private float stableUpSelfPercent;
    private float[] xs;
    private float[] ys;

    public ModelConfig() {
        this.splineModelHashMap = new HashMap<>();
        this.device = DefaultConfig.DEVICE_NAME;
        this.reviseXChangePoint = 36.0f;
        this.reviseX = 54.0f;
        this.reviseXMultiple = 10.0f;
        this.stableUpMinPercent = 0.17f;
        this.stableUpSelfPercent = 1.7f;
        this.stableDownMinPercent = 0.15f;
        this.stableDownSelfPercent = 0.33f;
        this.stableSmallChangePercent = 0.3f;
        this.stableSmallChangeTargetPercent = 0.1f;
        this.dragExpandMultipleRight = 1.3f;
        this.dragExpandMultipleLeft = 1.8f;
        this.deltaUpScale = 0.5f;
        this.deltaDownScale = 0.15f;
        this.frameDuration = 32;
        this.minBrightnessChange = 0.5f;
        this.maxBrightnessChange = 4.0f;
        this.speedMultipleInDistance = 0.1f;
        this.splinePointSize = 500;
        this.defaultBrightness = 260.0f;
        initSplines();
        this.xs = DefaultConfig.XS;
        this.ys = DefaultConfig.YS;
        this.darkLuxThreshold = 36.0f;
        this.leftScaleInDarkEnv = 1.5f;
        this.rightScaleInDarkEnv = 1.5f;
        this.stableSmallChangeScaleInDarkEnv = 1.5f;
        this.normalMaxBrightness = 1023.0f;
        this.stableRightMinLux = 10.01f;
        this.stableSmallRightMinLux = 7.01f;
        this.hbmXs = DefaultConfig.HBM_XS;
        this.hbmYs = DefaultConfig.HBM_YS;
        this.hbmMinXs = DefaultConfig.HBM_XS_MIN;
        this.hbmMaxXs = DefaultConfig.HBM_XS_MAX;
        this.dayHours = DefaultConfig.DAY_HOURS;
        this.nightHours = DefaultConfig.NIGHT_HOURS;
        this.minLightInDNM = DefaultConfig.MIN_LIGHT_IN_DNM;
    }

    private void initSplines() {
        if (this.splineModelHashMap == null) {
            this.splineModelHashMap = new HashMap<>();
        }
        SplineModel gameSpline = new SplineModel();
        gameSpline.setXs(DefaultConfig.GAME_XS);
        gameSpline.setYs(DefaultConfig.GAME_YS);
        this.splineModelHashMap.put(DefaultConfig.GAME_SPLINE, gameSpline);
    }

    public ModelConfig(ModelConfig modelConfig) {
        this();
        this.reviseXChangePoint = modelConfig.reviseXChangePoint;
        this.reviseX = modelConfig.reviseX;
        this.reviseXMultiple = modelConfig.reviseXMultiple;
        this.stableUpMinPercent = modelConfig.stableUpMinPercent;
        this.stableUpSelfPercent = modelConfig.stableUpSelfPercent;
        this.stableDownMinPercent = modelConfig.stableDownMinPercent;
        this.stableDownSelfPercent = modelConfig.stableDownSelfPercent;
        this.stableSmallChangePercent = modelConfig.stableSmallChangePercent;
        this.stableSmallChangeTargetPercent = modelConfig.stableSmallChangeTargetPercent;
        this.dragExpandMultipleRight = modelConfig.dragExpandMultipleRight;
        this.dragExpandMultipleLeft = modelConfig.dragExpandMultipleLeft;
        this.deltaUpScale = modelConfig.deltaUpScale;
        this.deltaDownScale = modelConfig.deltaDownScale;
        this.frameDuration = modelConfig.frameDuration;
        this.minBrightnessChange = modelConfig.minBrightnessChange;
        this.maxBrightnessChange = modelConfig.maxBrightnessChange;
        this.speedMultipleInDistance = modelConfig.speedMultipleInDistance;
        this.splinePointSize = modelConfig.splinePointSize;
        this.defaultBrightness = modelConfig.defaultBrightness;
        this.xs = modelConfig.xs;
        this.ys = modelConfig.ys;
        this.darkLuxThreshold = modelConfig.darkLuxThreshold;
        this.leftScaleInDarkEnv = modelConfig.leftScaleInDarkEnv;
        this.rightScaleInDarkEnv = modelConfig.rightScaleInDarkEnv;
        this.stableSmallChangeScaleInDarkEnv = modelConfig.stableSmallChangeScaleInDarkEnv;
        this.normalMaxBrightness = modelConfig.normalMaxBrightness;
        this.stableRightMinLux = modelConfig.stableRightMinLux;
        this.stableSmallRightMinLux = modelConfig.stableSmallRightMinLux;
        this.hbmXs = modelConfig.hbmXs;
        this.hbmYs = modelConfig.hbmYs;
        this.hbmMinXs = modelConfig.hbmMinXs;
        this.hbmMaxXs = modelConfig.hbmMaxXs;
        this.dayHours = modelConfig.dayHours;
        this.nightHours = modelConfig.nightHours;
        this.minLightInDNM = modelConfig.minLightInDNM;
        Map<String, SplineModel> splineModelMap = modelConfig.splineModelHashMap;
        if (splineModelMap != null && !splineModelMap.isEmpty()) {
            for (Map.Entry<String, SplineModel> splineModelEntry : splineModelMap.entrySet()) {
                String splineName = splineModelEntry.getKey();
                SplineModel tmpSplineModel = splineModelEntry.getValue();
                if (!(TextUtils.isEmpty(splineName) || tmpSplineModel == null || tmpSplineModel.getXs() == null || tmpSplineModel.getYs() == null)) {
                    SplineModel splineModel = new SplineModel();
                    int length = tmpSplineModel.getXs().length;
                    float[] xs2 = new float[length];
                    System.arraycopy(tmpSplineModel.getXs(), 0, xs2, 0, length);
                    splineModel.setXs(xs2);
                    int length2 = tmpSplineModel.getYs().length;
                    float[] ys2 = new float[length2];
                    System.arraycopy(tmpSplineModel.getYs(), 0, ys2, 0, length2);
                    splineModel.setYs(ys2);
                    if (TextUtils.equals(splineName, BatteryService.HealthServiceWrapper.INSTANCE_VENDOR)) {
                        this.xs = xs2;
                        this.ys = ys2;
                    }
                    this.splineModelHashMap.put(splineName, splineModel);
                }
            }
        }
    }

    public void setReviseXChangePoint(float reviseXChangePoint2) {
        this.reviseXChangePoint = reviseXChangePoint2;
    }

    public void setReviseX(float reviseX2) {
        this.reviseX = reviseX2;
    }

    public void setReviseXMultiple(float reviseXMultiple2) {
        this.reviseXMultiple = reviseXMultiple2;
    }

    public void setStableUpMinPercent(float stableUpMinPercent2) {
        this.stableUpMinPercent = stableUpMinPercent2;
    }

    public void setStableUpSelfPercent(float stableUpSelfPercent2) {
        this.stableUpSelfPercent = stableUpSelfPercent2;
    }

    public void setStableDownMinPercent(float stableDownMinPercent2) {
        this.stableDownMinPercent = stableDownMinPercent2;
    }

    public void setStableDownSelfPercent(float stableDownSelfPercent2) {
        this.stableDownSelfPercent = stableDownSelfPercent2;
    }

    public float getStableSmallRightMinLux() {
        return this.stableSmallRightMinLux;
    }

    public void setStableSmallRightMinLux(float stableSmallRightMinLux2) {
        this.stableSmallRightMinLux = stableSmallRightMinLux2;
    }

    public void setStableSmallChangePercent(float stableSmallChangePercent2) {
        this.stableSmallChangePercent = stableSmallChangePercent2;
    }

    public void setStableSmallChangeTargetPercent(float stableSmallChangeTargetPercent2) {
        this.stableSmallChangeTargetPercent = stableSmallChangeTargetPercent2;
    }

    public void setDragExpandMultipleRight(float dragExpandMultipleRight2) {
        this.dragExpandMultipleRight = dragExpandMultipleRight2;
    }

    public void setDragExpandMultipleLeft(float dragExpandMultipleLeft2) {
        this.dragExpandMultipleLeft = dragExpandMultipleLeft2;
    }

    public void setDeltaUpScale(float deltaUpScale2) {
        this.deltaUpScale = deltaUpScale2;
    }

    public void setDeltaDownScale(float deltaDownScale2) {
        this.deltaDownScale = deltaDownScale2;
    }

    public void setFrameDuration(int frameDuration2) {
        this.frameDuration = frameDuration2;
    }

    public void setMinBrightnessChange(float minBrightnessChange2) {
        this.minBrightnessChange = minBrightnessChange2;
    }

    public void setMaxBrightnessChange(float maxBrightnessChange2) {
        this.maxBrightnessChange = maxBrightnessChange2;
    }

    public void setSpeedMultipleInDistance(float speedMultipleInDistance2) {
        this.speedMultipleInDistance = speedMultipleInDistance2;
    }

    public void setSplinePointSize(int splinePointSize2) {
        this.splinePointSize = splinePointSize2;
    }

    public void setDefaultBrightness(float defaultBrightness2) {
        this.defaultBrightness = defaultBrightness2;
    }

    public void setXs(float[] xs2) {
        this.xs = xs2;
    }

    public void setYs(float[] ys2) {
        this.ys = ys2;
    }

    public String getDevice() {
        return this.device;
    }

    public void setDevice(String device2) {
        this.device = device2;
    }

    public float getReviseXChangePoint() {
        return this.reviseXChangePoint;
    }

    public float getReviseX() {
        return this.reviseX;
    }

    public float getReviseXMultiple() {
        return this.reviseXMultiple;
    }

    public float getStableUpMinPercent() {
        return this.stableUpMinPercent;
    }

    public float getStableUpSelfPercent() {
        return this.stableUpSelfPercent;
    }

    public float getStableDownMinPercent() {
        return this.stableDownMinPercent;
    }

    public float getStableDownSelfPercent() {
        return this.stableDownSelfPercent;
    }

    public float getStableSmallChangePercent() {
        return this.stableSmallChangePercent;
    }

    public float getStableSmallChangeTargetPercent() {
        return this.stableSmallChangeTargetPercent;
    }

    public float getDragExpandMultipleRight() {
        return this.dragExpandMultipleRight;
    }

    public float getDragExpandMultipleLeft() {
        return this.dragExpandMultipleLeft;
    }

    public float getDeltaUpScale() {
        return this.deltaUpScale;
    }

    public float getDeltaDownScale() {
        return this.deltaDownScale;
    }

    public int getFrameDuration() {
        return this.frameDuration;
    }

    public float getMinBrightnessChange() {
        return this.minBrightnessChange;
    }

    public float getMaxBrightnessChange() {
        return this.maxBrightnessChange;
    }

    public float getSpeedMultipleInDistance() {
        return this.speedMultipleInDistance;
    }

    public int getSplinePointSize() {
        return this.splinePointSize;
    }

    public float getDefaultBrightness() {
        return this.defaultBrightness;
    }

    public float[] getXs() {
        return this.xs;
    }

    public float[] getYs() {
        return this.ys;
    }

    public HashMap<String, SplineModel> getSplineModelHashMap() {
        HashMap<String, SplineModel> cloneHashMap = null;
        HashMap<String, SplineModel> hashMap = this.splineModelHashMap;
        if (hashMap != null && !hashMap.isEmpty()) {
            for (Map.Entry<String, SplineModel> entry : this.splineModelHashMap.entrySet()) {
                String name = entry.getKey();
                SplineModel splineModel = entry.getValue();
                if (!TextUtils.isEmpty(name) && splineModel != null) {
                    if (cloneHashMap == null) {
                        cloneHashMap = new HashMap<>(this.splineModelHashMap.size());
                    }
                    try {
                        cloneHashMap.put(name, splineModel.clone());
                    } catch (CloneNotSupportedException e) {
                        Slog.e(TAG, e.toString());
                    }
                }
            }
        }
        return cloneHashMap;
    }

    public void setSplineModelHashMap(HashMap<String, SplineModel> splineModelHashMap2) {
        this.splineModelHashMap = splineModelHashMap2;
    }

    public float getDarkLuxThreshold() {
        return this.darkLuxThreshold;
    }

    public void setDarkLuxThreshold(float darkLuxThreshold2) {
        this.darkLuxThreshold = darkLuxThreshold2;
    }

    public float getLeftScaleInDarkEnv() {
        return this.leftScaleInDarkEnv;
    }

    public void setLeftScaleInDarkEnv(float leftScaleInDarkEnv2) {
        this.leftScaleInDarkEnv = leftScaleInDarkEnv2;
    }

    public float getRightScaleInDarkEnv() {
        return this.rightScaleInDarkEnv;
    }

    public void setRightScaleInDarkEnv(float rightScaleInDarkEnv2) {
        this.rightScaleInDarkEnv = rightScaleInDarkEnv2;
    }

    public float getStableSmallChangeScaleInDarkEnv() {
        return this.stableSmallChangeScaleInDarkEnv;
    }

    public void setStableSmallChangeScaleInDarkEnv(float stableSmallChangeScaleInDarkEnv2) {
        this.stableSmallChangeScaleInDarkEnv = stableSmallChangeScaleInDarkEnv2;
    }

    public float getNormalMaxBrightness() {
        return this.normalMaxBrightness;
    }

    public void setNormalMaxBrightness(float normalMaxBrightness2) {
        this.normalMaxBrightness = normalMaxBrightness2;
    }

    public float getStableRightMinLux() {
        return this.stableRightMinLux;
    }

    public void setStableRightMinLux(float stableRightMinLux2) {
        this.stableRightMinLux = stableRightMinLux2;
    }

    public float[] getHbmXs() {
        return this.hbmXs;
    }

    public void setHbmXs(float[] hbmXs2) {
        this.hbmXs = hbmXs2;
    }

    public float[] getHbmYs() {
        return this.hbmYs;
    }

    public void setHbmYs(float[] hbmYs2) {
        this.hbmYs = hbmYs2;
    }

    public float[] getHbmMinXs() {
        return this.hbmMinXs;
    }

    public void setHbmMinXs(float[] hbmMinXs2) {
        this.hbmMinXs = hbmMinXs2;
    }

    public float[] getHbmMaxXs() {
        return this.hbmMaxXs;
    }

    public void setHbmMaxXs(float[] hbmMaxXs2) {
        this.hbmMaxXs = hbmMaxXs2;
    }

    public int[] getDayHours() {
        return this.dayHours;
    }

    public void setDayHours(int[] dayHours2) {
        this.dayHours = dayHours2;
    }

    public int[] getNightHours() {
        return this.nightHours;
    }

    public void setNightHours(int[] nightHours2) {
        this.nightHours = nightHours2;
    }

    public float[] getMinLightInDNM() {
        return this.minLightInDNM;
    }

    public void setMinLightInDNM(float[] minLightInDNM2) {
        this.minLightInDNM = minLightInDNM2;
    }

    private static class DefaultConfig {
        public static final float DARK_LUX_THRESHOLD = 36.0f;
        public static final int[] DAY_HOURS = {6, 17};
        public static final float DEFAULT_BRIGHTNESS = 260.0f;
        public static final float DELTA_DOWN_SCALE = 0.15f;
        public static final float DELTA_UP_SCALE = 0.5f;
        public static final String DEVICE_NAME = "samsung_tenbit";
        public static final float DRAG_EXPAND_MULTIPLE_LEFT = 1.8f;
        public static final float DRAG_EXPAND_MULTIPLE_RIGHT = 1.3f;
        public static final int FRAME_DURATION = 32;
        public static final String GAME_SPLINE = "game";
        public static final float[] GAME_XS = {OppoBrightUtils.MIN_LUX_LIMITI, 1.0f, 2.0f, 3.0f, 10.0f, 20.0f, 36.0f, 300.0f, 1000.0f, 2250.0f, 4600.0f, 5900.0f};
        public static final float[] GAME_YS = {20.0f, 30.0f, 35.0f, 50.0f, 100.0f, 150.0f, 200.0f, 280.0f, 430.0f, 600.0f, 750.0f, 1023.0f};
        public static final float[] HBM_XS = {12000.0f, 20000.0f, 30000.0f, 40000.0f, 60000.0f, 100000.0f};
        public static final float[] HBM_XS_MAX = {20100.0f, 30100.0f, 40100.0f, 60100.0f, 100100.0f, 500000.0f};
        public static final float[] HBM_XS_MIN = {8600.0f, 20000.0f, 30000.0f, 40000.0f, 60000.0f, 100000.0f};
        public static final float[] HBM_YS = {1023.0f, 1035.0f, 1080.0f, 1189.0f, 1270.0f, 2047.0f};
        public static final float LEFT_SCALE_IN_DARK_ENV = 1.5f;
        public static final boolean MAIN_SWITCH = true;
        public static final float MAX_BRIGHTNESS_CHANGE = 4.0f;
        public static final float MIN_BRIGHTNESS_CHANGE = 0.5f;
        public static final float[] MIN_LIGHT_IN_DNM = {20.0f, 30.0f};
        public static final int[] NIGHT_HOURS = {19, 6};
        public static final float NORMAL_MAX_BRIGHTNESS = 1023.0f;
        public static final float REVISE_X = 54.0f;
        public static final float REVISE_X_CHANGE_POINT = 36.0f;
        public static final float REVISE_X_MULTIPLE = 10.0f;
        public static final float RIGHT_SCALE_IN_DARK_ENV = 1.5f;
        public static final float SPEED_MULTIPLE_IN_DISTANCE = 0.1f;
        public static final int SPLINE_POINT_SIZE = 500;
        public static final float STABLE_DOWN_MIN_PERCENT = 0.15f;
        public static final float STABLE_DOWN_SELF_PERCENT = 0.33f;
        public static final float STABLE_RIGHT_MIN_LUX = 10.01f;
        public static final float STABLE_SMALL_CHANGE_PERCENT = 0.3f;
        public static final float STABLE_SMALL_CHANGE_SCALE_IN_DARK_ENV = 1.5f;
        public static final float STABLE_SMALL_CHANGE_TARGET_PERCENT = 0.1f;
        public static final float STABLE_SMALL_RIGHT_MIN_LUX = 7.01f;
        public static final float STABLE_UP_MIN_PERCENT = 0.17f;
        public static final float STABLE_UP_SELF_PERCENT = 1.7f;
        public static final float[] XS = {OppoBrightUtils.MIN_LUX_LIMITI, 1.0f, 2.0f, 3.0f, 10.0f, 20.0f, 36.0f, 100.0f, 300.0f, 1000.0f, 2250.0f, 4600.0f, 5900.0f, 8600.0f};
        public static final float[] YS = {13.0f, 25.0f, 30.0f, 40.0f, 80.0f, 113.0f, 135.0f, 190.0f, 245.0f, 390.0f, 550.0f, 700.0f, 900.0f, 1023.0f};

        private DefaultConfig() {
        }
    }

    public String toString() {
        return this.device + 10 + this.reviseXChangePoint + 10 + this.reviseX + 10 + this.reviseXMultiple + 10 + this.stableUpMinPercent + 10 + this.stableUpSelfPercent + 10 + this.stableDownMinPercent + 10 + this.stableDownSelfPercent + 10 + this.stableSmallChangePercent + 10 + this.stableSmallChangeTargetPercent + 10 + this.dragExpandMultipleRight + 10 + this.dragExpandMultipleLeft + 10 + this.deltaUpScale + 10 + this.deltaDownScale + 10 + this.frameDuration + 10 + this.minBrightnessChange + 10 + this.maxBrightnessChange + 10 + this.speedMultipleInDistance + 10 + this.splinePointSize + 10 + this.defaultBrightness + 10 + Arrays.toString(this.xs) + 10 + Arrays.toString(this.ys) + 10 + this.normalMaxBrightness + 10 + this.stableRightMinLux + 10 + this.stableSmallRightMinLux + 10 + Arrays.toString(this.hbmMinXs) + 10 + Arrays.toString(this.hbmXs) + 10 + Arrays.toString(this.hbmMaxXs) + 10 + Arrays.toString(this.hbmYs) + 10 + Arrays.toString(this.dayHours) + 10 + Arrays.toString(this.nightHours) + 10 + "MinLevel config : day=" + this.minLightInDNM[1] + " normal=" + this.minLightInDNM[0] + " night=" + this.ys[0];
    }
}
