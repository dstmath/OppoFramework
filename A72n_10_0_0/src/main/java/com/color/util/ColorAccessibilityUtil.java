package com.color.util;

import android.content.ComponentName;
import android.content.Context;
import android.provider.Settings;
import android.text.TextUtils;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class ColorAccessibilityUtil {
    private static final String TALKBACK_SERVICE = "com.google.android.marvin.talkback/com.google.android.marvin.talkback.TalkBackService";

    public static Set<ComponentName> getEnabledServicesFromSettings(Context context) {
        return getEnabledServicesFromSettings(context, -2);
    }

    public static Set<ComponentName> getEnabledServicesFromSettings(Context context, int userId) {
        String enabledServicesSetting = Settings.Secure.getStringForUser(context.getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES, userId);
        if (enabledServicesSetting == null) {
            return Collections.emptySet();
        }
        Set<ComponentName> enabledServices = new HashSet<>();
        TextUtils.SimpleStringSplitter colonSplitter = new TextUtils.SimpleStringSplitter(':');
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
        for (ComponentName cn2 : enabledServices) {
            if (TextUtils.equals(cn2.flattenToString(), TALKBACK_SERVICE)) {
                return true;
            }
        }
        return false;
    }
}
