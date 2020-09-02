package com.android.server.display;

import com.oppo.reflect.RefClass;
import com.oppo.reflect.RefMethod;

public class OppoMirrorDisplayPowerController {
    public static Class<?> TYPE = RefClass.load(OppoMirrorDisplayPowerController.class, DisplayPowerController.class);
    public static RefMethod<Boolean> isBlockScreenOnByBiometrics;
}
