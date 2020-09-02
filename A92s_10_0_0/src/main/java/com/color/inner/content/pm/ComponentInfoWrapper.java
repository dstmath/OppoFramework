package com.color.inner.content.pm;

import android.content.ComponentName;
import android.content.pm.ComponentInfo;

public class ComponentInfoWrapper {
    private static final String TAG = "ComponentInfoWrapper";

    private ComponentInfoWrapper() {
    }

    public static ComponentName getComponentName(ComponentInfo componentInfo) {
        return componentInfo.getComponentName();
    }
}
