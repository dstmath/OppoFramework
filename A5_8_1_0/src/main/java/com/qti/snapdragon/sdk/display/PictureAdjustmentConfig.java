package com.qti.snapdragon.sdk.display;

import java.util.EnumSet;

public class PictureAdjustmentConfig {
    private int contrast;
    private int hue;
    private int intensity;
    protected boolean isDesaturation = false;
    protected boolean isGlobalPADisabled = false;
    private EnumSet<PICTURE_ADJUSTMENT_PARAMS> paValues;
    private int sat_threshold;
    private int saturation;

    public enum PICTURE_ADJUSTMENT_PARAMS {
        HUE(0),
        SATURATION(1),
        INTENSITY(2),
        CONTRAST(3),
        SATURATION_THRESHOLD(4);
        
        private int value;

        private PICTURE_ADJUSTMENT_PARAMS(int value) {
            this.value = value;
        }

        protected int getValue() {
            return this.value;
        }
    }

    public PictureAdjustmentConfig(EnumSet<PICTURE_ADJUSTMENT_PARAMS> paramFlags, int hue, int saturation, int intensity, int contrast, int sat_thresh) {
        this.paValues = paramFlags;
        this.hue = hue;
        this.saturation = saturation;
        this.intensity = intensity;
        this.contrast = contrast;
        this.sat_threshold = sat_thresh;
    }

    public EnumSet<PICTURE_ADJUSTMENT_PARAMS> getParamFlags() {
        return this.paValues;
    }

    public int getHue() {
        return this.hue;
    }

    public int getSaturation() {
        return this.saturation;
    }

    public int getIntensity() {
        return this.intensity;
    }

    public int getContrast() {
        return this.contrast;
    }

    public int getSaturationThreshold() {
        return this.sat_threshold;
    }

    public boolean isDesaturationEnabled() {
        return this.isDesaturation;
    }

    public boolean isGlobalPictureAdjustmentDisabled() {
        return this.isGlobalPADisabled;
    }
}
