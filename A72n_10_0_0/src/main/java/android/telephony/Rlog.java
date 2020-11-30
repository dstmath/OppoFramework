package android.telephony;

import android.annotation.UnsupportedAppUsage;
import android.os.Build;
import android.os.SystemProperties;
import android.security.keystore.KeyProperties;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class Rlog {
    public static boolean DATA_VDBG = SystemProperties.getBoolean("persist.sys.data.log_switch", false);
    public static final String OPERATOR = SystemProperties.get("ro.oppo.operator", "OPPO");
    public static final boolean SWITCH_LOG = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    private static final boolean USER_BUILD = Build.IS_USER;
    public static final boolean VDF_NON_DEBUG;
    public static final boolean VDF_OPERATOR = "VODAFONE_EEA".equals(OPERATOR);

    static {
        boolean z = false;
        if (!SWITCH_LOG && VDF_OPERATOR) {
            z = true;
        }
        VDF_NON_DEBUG = z;
    }

    private Rlog() {
    }

    @UnsupportedAppUsage
    public static int v(String tag, String msg) {
        if (VDF_NON_DEBUG) {
            return 0;
        }
        return Log.println_native(1, 2, tag, msg);
    }

    public static int v(String tag, String msg, Throwable tr) {
        if (VDF_NON_DEBUG) {
            return 0;
        }
        return Log.println_native(1, 2, tag, msg + '\n' + Log.getStackTraceString(tr));
    }

    @UnsupportedAppUsage
    public static int d(String tag, String msg) {
        if (VDF_NON_DEBUG) {
            return 0;
        }
        return Log.println_native(1, 3, tag, msg);
    }

    @UnsupportedAppUsage
    public static int d(String tag, String msg, Throwable tr) {
        if (VDF_NON_DEBUG) {
            return 0;
        }
        return Log.println_native(1, 3, tag, msg + '\n' + Log.getStackTraceString(tr));
    }

    @UnsupportedAppUsage
    public static int i(String tag, String msg) {
        if (VDF_NON_DEBUG) {
            return 0;
        }
        return Log.println_native(1, 4, tag, msg);
    }

    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    public static int i(String tag, String msg, Throwable tr) {
        if (VDF_NON_DEBUG) {
            return 0;
        }
        return Log.println_native(1, 4, tag, msg + '\n' + Log.getStackTraceString(tr));
    }

    @UnsupportedAppUsage
    public static int w(String tag, String msg) {
        if (VDF_NON_DEBUG) {
            return 0;
        }
        return Log.println_native(1, 5, tag, msg);
    }

    @UnsupportedAppUsage
    public static int w(String tag, String msg, Throwable tr) {
        if (VDF_NON_DEBUG) {
            return 0;
        }
        return Log.println_native(1, 5, tag, msg + '\n' + Log.getStackTraceString(tr));
    }

    public static int w(String tag, Throwable tr) {
        if (VDF_NON_DEBUG) {
            return 0;
        }
        return Log.println_native(1, 5, tag, Log.getStackTraceString(tr));
    }

    @UnsupportedAppUsage
    public static int e(String tag, String msg) {
        if (VDF_NON_DEBUG) {
            return 0;
        }
        return Log.println_native(1, 6, tag, msg);
    }

    @UnsupportedAppUsage
    public static int e(String tag, String msg, Throwable tr) {
        if (VDF_NON_DEBUG) {
            return 0;
        }
        return Log.println_native(1, 6, tag, msg + '\n' + Log.getStackTraceString(tr));
    }

    public static int println(int priority, String tag, String msg) {
        if (VDF_NON_DEBUG) {
            return 0;
        }
        return Log.println_native(1, priority, tag, msg);
    }

    public static boolean isLoggable(String tag, int level) {
        if (VDF_NON_DEBUG) {
            return false;
        }
        return Log.isLoggable(tag, level);
    }

    public static String pii(String tag, Object pii) {
        String val = String.valueOf(pii);
        if (pii == null || TextUtils.isEmpty(val) || isLoggable(tag, 2)) {
            return val;
        }
        return "[" + secureHash(val.getBytes()) + "]";
    }

    public static String pii(boolean enablePiiLogging, Object pii) {
        String val = String.valueOf(pii);
        if (pii == null || TextUtils.isEmpty(val) || enablePiiLogging) {
            return val;
        }
        return "[" + secureHash(val.getBytes()) + "]";
    }

    private static String secureHash(byte[] input) {
        if (USER_BUILD) {
            return "****";
        }
        try {
            return Base64.encodeToString(MessageDigest.getInstance(KeyProperties.DIGEST_SHA1).digest(input), 11);
        } catch (NoSuchAlgorithmException e) {
            return "####";
        }
    }
}
