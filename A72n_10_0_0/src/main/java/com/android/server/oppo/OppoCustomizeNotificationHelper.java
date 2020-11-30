package com.android.server.oppo;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.Log;

public class OppoCustomizeNotificationHelper {
    private static final boolean DEBUG = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    private static final String TAG = OppoCustomizeNotificationHelper.class.getSimpleName();
    private static volatile OppoCustomizeNotificationHelper sNotificationHelper;
    private Context mContext;
    private boolean mCustomizeVersion;

    public interface Constants {
        public static final String FEATURE_BUSINESS_CUSTOM = "oppo.business.custom";
        public static final String PROP_INTERCEPT_ALL_NOTIFICATIONS = "persist.sys.customize.notification_disable";
        public static final String PROP_INTERCEPT_NON_SYSTEM_NOTIFICATIONS = "persist.sys.customize.non_system_notifications_disable";
    }

    public static OppoCustomizeNotificationHelper getInstance() {
        if (sNotificationHelper == null) {
            synchronized (OppoCustomizeNotificationHelper.class) {
                if (sNotificationHelper == null) {
                    sNotificationHelper = new OppoCustomizeNotificationHelper();
                }
            }
        }
        return sNotificationHelper;
    }

    private OppoCustomizeNotificationHelper() {
    }

    public void init(Context context) {
        this.mContext = context;
        this.mCustomizeVersion = context.getPackageManager().hasSystemFeature(Constants.FEATURE_BUSINESS_CUSTOM);
    }

    public boolean shouldInterceptNotifications(String pkgName) {
        if (!this.mCustomizeVersion) {
            return false;
        }
        if (shouldInterceptAllNotifications()) {
            Util.logDebug("Intercept all notifications by prop: persist.sys.customize.notification_disable");
            return true;
        } else if (!shouldInterceptNonSystemNotifications(pkgName)) {
            return false;
        } else {
            Util.logDebug("Intercept non system notifications by prop: persist.sys.customize.non_system_notifications_disable");
            return true;
        }
    }

    public boolean shouldInterceptAllNotifications() {
        if (!this.mCustomizeVersion || !SystemProperties.getBoolean(Constants.PROP_INTERCEPT_ALL_NOTIFICATIONS, false)) {
            return false;
        }
        return true;
    }

    public boolean shouldInterceptNonSystemNotifications() {
        if (shouldInterceptAllNotifications() || (this.mCustomizeVersion && SystemProperties.getBoolean(Constants.PROP_INTERCEPT_NON_SYSTEM_NOTIFICATIONS, false))) {
            return true;
        }
        return false;
    }

    private boolean shouldInterceptNonSystemNotifications(String pkgName) {
        return shouldInterceptNonSystemNotifications() && !isSystemApp(pkgName);
    }

    private boolean isSystemApp(String pkgName) {
        PackageManager packageManager;
        if (TextUtils.isEmpty(pkgName) || (packageManager = this.mContext.getPackageManager()) == null) {
            return false;
        }
        try {
            ApplicationInfo appInfo = packageManager.getApplicationInfo(pkgName, 0);
            if ((appInfo.flags & 1) == 0 && (appInfo.flags & 128) == 0) {
                return false;
            }
            return true;
        } catch (Exception e) {
        }
    }

    private static final class Util {
        private Util() {
        }

        /* access modifiers changed from: private */
        public static void logDebug(String msg) {
            if (OppoCustomizeNotificationHelper.DEBUG) {
                Log.d(OppoCustomizeNotificationHelper.TAG, msg);
            }
        }
    }
}
