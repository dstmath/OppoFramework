package com.color.util;

import android.content.ComponentName;
import android.content.Context;
import android.os.UserHandle;
import android.provider.Settings.Secure;
import android.text.TextUtils;
import android.text.TextUtils.SimpleStringSplitter;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class ColorAccessibilityUtil {
    public static final char ENABLED_ACCESSIBILITY_SERVICES_SEPARATOR = ':';
    private static final String TALKBACK_PACKAGE = "com.google.android.marvin.talkback";
    static final SimpleStringSplitter sStringColonSplitter = new SimpleStringSplitter(ENABLED_ACCESSIBILITY_SERVICES_SEPARATOR);

    public static Set<ComponentName> getEnabledServicesFromSettings(Context context) {
        return getEnabledServicesFromSettings(context, UserHandle.myUserId());
    }

    public static Set<ComponentName> getEnabledServicesFromSettings(Context context, int userId) {
        String enabledServicesSetting = Secure.getStringForUser(context.getContentResolver(), Secure.ENABLED_ACCESSIBILITY_SERVICES, userId);
        if (enabledServicesSetting == null) {
            return Collections.emptySet();
        }
        Set<ComponentName> enabledServices = new HashSet();
        SimpleStringSplitter colonSplitter = sStringColonSplitter;
        colonSplitter.setString(enabledServicesSetting);
        while (colonSplitter.hasNext()) {
            ComponentName enabledService = ComponentName.unflattenFromString(colonSplitter.next());
            if (enabledService != null) {
                enabledServices.add(enabledService);
            }
        }
        return enabledServices;
    }

    public static boolean isTalkbackEnabled(Context context) {
        Set<ComponentName> enabledServices = getEnabledServicesFromSettings(context);
        if (enabledServices == null || enabledServices.isEmpty()) {
            return false;
        }
        for (ComponentName cn : enabledServices) {
            if (TextUtils.equals(cn.getPackageName(), TALKBACK_PACKAGE)) {
                return true;
            }
        }
        return false;
    }
}
