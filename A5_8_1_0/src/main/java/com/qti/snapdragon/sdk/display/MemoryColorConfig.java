package com.qti.snapdragon.sdk.display;

public class MemoryColorConfig {
    private int hue;
    private int intensity;
    protected boolean isEnabled = true;
    private MEMORY_COLOR_TYPE memColorType;
    private int saturation;

    public enum MEMORY_COLOR_PARAMS {
        HUE(0),
        SATURATION(1),
        INTENSITY(2);
        
        private int value;

        private MEMORY_COLOR_PARAMS(int value) {
            this.value = value;
        }

        protected int getValue() {
            return this.value;
        }
    }

    public enum MEMORY_COLOR_TYPE {
        SKIN(0),
        SKY(1),
        FOLIAGE(2);
        
        private int value;

        private MEMORY_COLOR_TYPE(int value) {
            this.value = value;
        }

        protected int getValue() {
            return this.value;
        }
    }

    public MemoryColorConfig(MEMORY_COLOR_TYPE type, int hue, int saturation, int intensity) {
        this.memColorType = type;
        this.hue = hue;
        this.saturation = saturation;
        this.intensity = intensity;
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

    public MEMORY_COLOR_TYPE getMemoryColorType() {
        return this.memColorType;
    }

    public boolean isMemoryColorEnabled() {
        return this.isEnabled;
    }
}
