package com.android.server.usb;

import android.common.IOppoCommonFeature;
import android.common.OppoFeatureList;
import android.util.Log;

public interface IOppoUsbDeviceFeature extends IOppoCommonFeature {
    public static final IOppoUsbDeviceFeature DEFAULT = new IOppoUsbDeviceFeature() {
        /* class com.android.server.usb.IOppoUsbDeviceFeature.AnonymousClass1 */
    };
    public static final String NAME = "IOppoUsbDeviceFeature";

    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IOppoUsbDeviceFeature;
    }

    default IOppoCommonFeature getDefault() {
        return DEFAULT;
    }

    default void setUsbAccessoryStartFlag() {
        Log.d(NAME, "default setUsbAccessoryStartFlag");
    }

    default void initUsbAccessoryStartFlag() {
        Log.d(NAME, "default initUsbAccessoryStartFlag");
    }

    default boolean getUsbAccessoryStartFlag(long functions) {
        Log.d(NAME, "default getUsbAccessoryStartFlag");
        return false;
    }

    default void initUsbPlugFlag() {
        Log.d(NAME, "default initUsbPlugFlag");
    }

    default void setUsbPlugFlag(boolean connected) {
        Log.d(NAME, "default setUsbPlugFlag");
    }

    default boolean usbFunctionsShouldForceStart() {
        Log.d(NAME, "default usbFunctionsShouldForceStart");
        return false;
    }

    default boolean usbFunctionsShuoldUseDefault(String functions) {
        Log.d(NAME, "default usbFunctionsShuoldUseDefault");
        return false;
    }

    default void printFinishBootInfo(OppoUsbDeviceFinishBootInfo bootInfo) {
        Log.d(NAME, "default printFinishBootInfo");
    }

    default long getChargingFunctions() {
        Log.d(NAME, "default getChargingFunctions");
        return 0;
    }

    default boolean isTelecomRequirement(String functions) {
        Log.d(NAME, "default isTelecomRequirement");
        return false;
    }

    default void printBootModeForDebug(String bootMode) {
        Log.d(NAME, "default printBootModeForDebug");
    }

    default boolean isNormalBoot() {
        Log.d(NAME, "default isNormalBoot");
        return false;
    }

    default boolean ignoreCTARequirement(String functions) {
        Log.d(NAME, "default IgnoreCTARequirement");
        return true;
    }

    default boolean usbFunctionsNotForceRestart(String functions) {
        Log.d(NAME, "default usbFunctionsNotForceRestart");
        return false;
    }

    default String resetFunctionsForCTA(String functions) {
        Log.d(NAME, "default resetFunctionsForCTA");
        return null;
    }

    default void printFunctionsForDebug(OppoUsbDeviceFunctionInfo functionInfo) {
        Log.d(NAME, "default printFunctionsForDebug");
    }

    default void usbConfigRecord(String functions) {
        Log.d(NAME, "default usbConfigRecord");
    }
}
