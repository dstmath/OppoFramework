package com.android.server.display;

import java.util.List;

public interface IPswDisplayPowerControllerReduceBrightnessInner {
    public static final IPswDisplayPowerControllerReduceBrightnessInner DEFAULT = new IPswDisplayPowerControllerReduceBrightnessInner() {
        /* class com.android.server.display.IPswDisplayPowerControllerReduceBrightnessInner.AnonymousClass1 */
    };

    default void sendUpdatePowerStateInner() {
    }

    default void init() {
    }

    default void registerByNewImpl() {
    }

    default List<String> GetReduceBrightnessPackage() {
        return null;
    }
}
