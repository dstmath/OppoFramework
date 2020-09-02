package com.color.inner.content.pm;

import android.content.pm.ComponentInfo;
import android.content.pm.ResolveInfo;

public class ResolveInfoWrapper {
    private static final String TAG = "ResolveInfoWrapper";

    private ResolveInfoWrapper() {
    }

    public static ComponentInfo getComponentInfo(ResolveInfo resolveInfo) {
        return resolveInfo.getComponentInfo();
    }
}
