package com.android.internal.telephony;

import android.content.Context;
import android.os.Bundle;
import android.os.SystemProperties;

public final class OppoRlog {
    private OppoRlog() {
    }

    public static final class Rlog {
        public static final boolean DBG = isDebugSwitchOpen();

        private Rlog() {
        }

        public static boolean isLoggable(String tag, int level) {
            return false;
        }

        public static void v(String tag, String msg) {
            if (DBG && tag != null && msg != null) {
                android.telephony.Rlog.v(tag, msg);
            }
        }

        public static void d(String tag, String msg) {
            if (DBG && tag != null && msg != null) {
                android.telephony.Rlog.d(tag, msg);
            }
        }

        public static void i(String tag, String msg) {
            if (DBG && tag != null && msg != null) {
                android.telephony.Rlog.i(tag, msg);
            }
        }

        public static void w(String tag, String msg) {
            if (DBG && tag != null && msg != null) {
                android.telephony.Rlog.w(tag, msg);
            }
        }

        public static void e(String tag, String msg) {
            if (tag != null && msg != null) {
                android.telephony.Rlog.e(tag, msg);
            }
        }

        public static int e(String tag, String msg, Throwable tr) {
            if (tag == null || msg == null || tr == null) {
                return 0;
            }
            return android.telephony.Rlog.e(tag, msg, tr);
        }

        public static String pii(String tag, Object pii) {
            return "[unknown]";
        }

        public static String pii(boolean enablePiiLogging, Object pii) {
            return "[unknown]";
        }

        public static boolean isDebugSwitchOpen() {
            try {
                return "true".equalsIgnoreCase(SystemProperties.get("persist.sys.assert.panic", "false"));
            } catch (Exception ex) {
                ex.printStackTrace();
                return true;
            }
        }
    }

    public static final class Log {
        public static final int ASSERT = 7;
        public static final boolean DBG = isDebugSwitchOpen();
        public static final int DEBUG = 3;
        public static final int ERROR = 6;
        public static final int INFO = 4;
        public static final int VERBOSE = 2;
        public static final int WARN = 5;

        private Log() {
        }

        public static boolean isLoggable(String tag, int level) {
            return false;
        }

        public static void v(String tag, String msg) {
            if (DBG && tag != null && msg != null) {
                android.util.Log.v(tag, msg);
            }
        }

        public static void d(String tag, String msg) {
            if (DBG && tag != null && msg != null) {
                android.util.Log.d(tag, msg);
            }
        }

        public static void i(String tag, String msg) {
            if (DBG && tag != null && msg != null) {
                android.util.Log.i(tag, msg);
            }
        }

        public static int i(String tag, String msg, Throwable tr) {
            if (!DBG || tag == null || msg == null || tr == null) {
                return 0;
            }
            return android.util.Log.i(tag, msg, tr);
        }

        public static void w(String tag, String msg) {
            if (DBG && tag != null && msg != null) {
                android.util.Log.w(tag, msg);
            }
        }

        public static int w(String tag, String msg, Throwable tr) {
            if (!DBG || tag == null || msg == null || tr == null) {
                return 0;
            }
            return android.util.Log.w(tag, msg, tr);
        }

        public static void e(String tag, String msg) {
            if (tag != null && msg != null) {
                android.util.Log.e(tag, msg);
            }
        }

        public static int e(String tag, String msg, Throwable tr) {
            if (tag == null || msg == null || tr == null) {
                return 0;
            }
            return android.util.Log.e(tag, msg, tr);
        }

        public static boolean isDebugSwitchOpen() {
            try {
                return "true".equalsIgnoreCase(SystemProperties.get("persist.sys.assert.panic", "false"));
            } catch (Exception ex) {
                ex.printStackTrace();
                return true;
            }
        }
    }

    public static final class Build {
        public static final boolean IS_DEBUGGABLE = false;

        private Build() {
        }
    }

    public static final class BlockChecker {
        private static final String TAG = "BlockChecker";

        private BlockChecker() {
        }

        public static boolean isBlocked(Context context, String phoneNumber) {
            return isBlocked(context, phoneNumber, null);
        }

        public static boolean isBlocked(Context context, String phoneNumber, Bundle extras) {
            try {
                IOppoSmsManager manager = (IOppoSmsManager) OppoTelephonyFactory.getInstance().getFeature(IOppoSmsManager.DEFAULT, new Object[0]);
                if (manager == null || !manager.oemIsMtSmsBlock(context, phoneNumber)) {
                    return BlockChecker.isBlocked(context, phoneNumber, extras);
                }
                Rlog.d(TAG, "nomral block");
                return true;
            } catch (Exception ex) {
                ex.printStackTrace();
                return false;
            }
        }
    }
}
