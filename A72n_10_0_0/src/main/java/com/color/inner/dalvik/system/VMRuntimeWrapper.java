package com.color.inner.dalvik.system;

import dalvik.system.VMRuntime;

public class VMRuntimeWrapper {
    private VMRuntime mVMRuntime = VMRuntime.getRuntime();

    private VMRuntimeWrapper() {
    }

    public static VMRuntimeWrapper getRuntime() {
        return new VMRuntimeWrapper();
    }

    public boolean is64Bit() {
        return this.mVMRuntime.is64Bit();
    }

    public static String getCurrentInstructionSet() {
        return VMRuntime.getCurrentInstructionSet();
    }
}
