package com.android.server.am;

import android.content.Context;

public final class ColorActivityManagerRegistryHelper extends ColorActivityManagerCommonHelper {
    public ColorActivityManagerRegistryHelper(Context context, IColorActivityManagerServiceEx amsEx) {
        super(context, amsEx);
    }

    public void registerColorCustomManager() {
        registerColorAppPhoneManager();
        registerColorFreeformManager();
    }

    private void registerColorAppPhoneManager() {
    }

    private void registerColorFreeformManager() {
    }
}
