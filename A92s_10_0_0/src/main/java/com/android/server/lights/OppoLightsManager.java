package com.android.server.lights;

public abstract class OppoLightsManager extends LightsManager {
    public static final int LIGHT_FLASH_HARDWARE = 2;
    public static final int LIGHT_FLASH_NONE = 0;
    public static final int LIGHT_FLASH_TIMED = 1;

    @Override // com.android.server.lights.LightsManager
    public abstract boolean getLightState(int i);

    @Override // com.android.server.lights.LightsManager
    public abstract void setKeyguardWindowAlpha(float f);
}
