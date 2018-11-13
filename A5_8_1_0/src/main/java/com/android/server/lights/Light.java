package com.android.server.lights;

public abstract class Light {
    public static final int BRIGHTNESS_MODE_LOW_PERSISTENCE = 2;
    public static final int BRIGHTNESS_MODE_SENSOR = 1;
    public static final int BRIGHTNESS_MODE_USER = 0;
    public static final int LIGHT_FLASH_HARDWARE = 2;
    public static final int LIGHT_FLASH_NONE = 0;
    public static final int LIGHT_FLASH_TIMED = 1;

    public abstract void pulse();

    public abstract void pulse(int i, int i2);

    public abstract void setBrightness(int i);

    public abstract void setBrightness(int i, int i2);

    public abstract void setColor(int i);

    public abstract void setFlashing(int i, int i2, int i3, int i4);

    public abstract void setVrMode(boolean z);

    public abstract void turnOff();
}
