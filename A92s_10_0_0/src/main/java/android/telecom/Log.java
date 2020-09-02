package android.telecom;

import android.annotation.UnsupportedAppUsage;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.SystemProperties;
import android.provider.Settings;
import android.provider.SettingsStringUtil;
import android.telecom.Logging.EventManager;
import android.telecom.Logging.Session;
import android.telecom.Logging.SessionManager;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.IndentingPrintWriter;
import java.util.IllegalFormatException;
import java.util.Locale;

public class Log {
    public static boolean DEBUG = false;
    public static boolean ERROR = isLoggable(6);
    private static final int EVENTS_TO_CACHE = 10;
    private static final int EVENTS_TO_CACHE_DEBUG = 20;
    private static final long EXTENDED_LOGGING_DURATION_MILLIS = 1800000;
    private static final boolean FORCE_LOGGING = false;
    public static boolean INFO;
    private static final int NUM_DIALABLE_DIGITS_TO_LOG = (Build.IS_USER ? 0 : 2);
    public static boolean OPPO_DEBUG = false;
    public static boolean OPPO_PANIC = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    public static boolean OPPO_PHONE_LOG_SWITCH = false;
    @VisibleForTesting
    public static String TAG = "TelecomFramework";
    private static final boolean USER_BUILD = Build.IS_USER;
    public static boolean VERBOSE;
    public static boolean WARN = isLoggable(5);
    private static EventManager sEventManager;
    private static boolean sIsUserExtendedLoggingEnabled = false;
    private static SessionManager sSessionManager;
    private static final Object sSingletonSync = new Object();
    private static long sUserExtendedLoggingStopTime = 0;

    static {
        boolean z = OPPO_PANIC;
        DEBUG = z;
        INFO = z;
        VERBOSE = z;
    }

    private Log() {
    }

    public static void oppoRefreshLogSwitch(Context context) {
        if (context != null) {
            boolean z = false;
            OPPO_PANIC = SystemProperties.getBoolean("persist.sys.assert.panic", false);
            OPPO_PHONE_LOG_SWITCH = Settings.System.getInt(context.getContentResolver(), "phone.log.switch", 0) == 1;
            android.util.Log.i(TAG, buildMessage("TelecomFramework", "OPPO_PANIC = " + OPPO_PANIC + ", OPPO_PHONE_LOG_SWITCH = " + OPPO_PHONE_LOG_SWITCH, new Object[0]));
            if (OPPO_PHONE_LOG_SWITCH || OPPO_PANIC) {
                z = true;
            }
            DEBUG = z;
            boolean z2 = DEBUG;
            VERBOSE = z2;
            INFO = z2;
            OPPO_DEBUG = OPPO_PHONE_LOG_SWITCH;
        }
    }

    public static void d(String prefix, String format, Object... args) {
        if (sIsUserExtendedLoggingEnabled) {
            maybeDisableLogging();
            android.util.Log.i(TAG, buildMessage(prefix, format, args));
        } else if (DEBUG) {
            android.util.Log.d(TAG, buildMessage(prefix, format, args));
        }
    }

    public static void d(Object objectPrefix, String format, Object... args) {
        if (sIsUserExtendedLoggingEnabled) {
            maybeDisableLogging();
            android.util.Log.i(TAG, buildMessage(getPrefixFromObject(objectPrefix), format, args));
        } else if (DEBUG) {
            android.util.Log.d(TAG, buildMessage(getPrefixFromObject(objectPrefix), format, args));
        }
    }

    @UnsupportedAppUsage
    public static void i(String prefix, String format, Object... args) {
        if (INFO) {
            android.util.Log.i(TAG, buildMessage(prefix, format, args));
        }
    }

    public static void i(Object objectPrefix, String format, Object... args) {
        if (INFO) {
            android.util.Log.i(TAG, buildMessage(getPrefixFromObject(objectPrefix), format, args));
        }
    }

    public static void v(String prefix, String format, Object... args) {
        if (sIsUserExtendedLoggingEnabled) {
            maybeDisableLogging();
            android.util.Log.i(TAG, buildMessage(prefix, format, args));
        } else if (VERBOSE) {
            android.util.Log.v(TAG, buildMessage(prefix, format, args));
        }
    }

    public static void v(Object objectPrefix, String format, Object... args) {
        if (sIsUserExtendedLoggingEnabled) {
            maybeDisableLogging();
            android.util.Log.i(TAG, buildMessage(getPrefixFromObject(objectPrefix), format, args));
        } else if (VERBOSE) {
            android.util.Log.v(TAG, buildMessage(getPrefixFromObject(objectPrefix), format, args));
        }
    }

    @UnsupportedAppUsage
    public static void w(String prefix, String format, Object... args) {
        if (WARN) {
            android.util.Log.w(TAG, buildMessage(prefix, format, args));
        }
    }

    public static void w(Object objectPrefix, String format, Object... args) {
        if (WARN) {
            android.util.Log.w(TAG, buildMessage(getPrefixFromObject(objectPrefix), format, args));
        }
    }

    public static void e(String prefix, Throwable tr, String format, Object... args) {
        if (ERROR) {
            android.util.Log.e(TAG, buildMessage(prefix, format, args), tr);
        }
    }

    public static void e(Object objectPrefix, Throwable tr, String format, Object... args) {
        if (ERROR) {
            android.util.Log.e(TAG, buildMessage(getPrefixFromObject(objectPrefix), format, args), tr);
        }
    }

    public static void wtf(String prefix, Throwable tr, String format, Object... args) {
        android.util.Log.wtf(TAG, buildMessage(prefix, format, args), tr);
    }

    public static void wtf(Object objectPrefix, Throwable tr, String format, Object... args) {
        android.util.Log.wtf(TAG, buildMessage(getPrefixFromObject(objectPrefix), format, args), tr);
    }

    public static void wtf(String prefix, String format, Object... args) {
        String msg = buildMessage(prefix, format, args);
        android.util.Log.wtf(TAG, msg, new IllegalStateException(msg));
    }

    public static void wtf(Object objectPrefix, String format, Object... args) {
        String msg = buildMessage(getPrefixFromObject(objectPrefix), format, args);
        android.util.Log.wtf(TAG, msg, new IllegalStateException(msg));
    }

    public static void setSessionContext(Context context) {
        getSessionManager().setContext(context);
    }

    public static void startSession(String shortMethodName) {
        getSessionManager().startSession(shortMethodName, null);
    }

    public static void startSession(Session.Info info, String shortMethodName) {
        getSessionManager().startSession(info, shortMethodName, null);
    }

    public static void startSession(String shortMethodName, String callerIdentification) {
        getSessionManager().startSession(shortMethodName, callerIdentification);
    }

    public static void startSession(Session.Info info, String shortMethodName, String callerIdentification) {
        getSessionManager().startSession(info, shortMethodName, callerIdentification);
    }

    public static Session createSubsession() {
        return getSessionManager().createSubsession();
    }

    public static Session.Info getExternalSession() {
        return getSessionManager().getExternalSession();
    }

    public static void cancelSubsession(Session subsession) {
        getSessionManager().cancelSubsession(subsession);
    }

    public static void continueSession(Session subsession, String shortMethodName) {
        getSessionManager().continueSession(subsession, shortMethodName);
    }

    public static void endSession() {
        getSessionManager().endSession();
    }

    public static void registerSessionListener(SessionManager.ISessionListener l) {
        getSessionManager().registerSessionListener(l);
    }

    public static String getSessionId() {
        synchronized (sSingletonSync) {
            if (sSessionManager == null) {
                return "";
            }
            String sessionId = getSessionManager().getSessionId();
            return sessionId;
        }
    }

    public static void addEvent(EventManager.Loggable recordEntry, String event) {
        getEventManager().event(recordEntry, event, null);
    }

    public static void addEvent(EventManager.Loggable recordEntry, String event, Object data) {
        getEventManager().event(recordEntry, event, data);
    }

    public static void addEvent(EventManager.Loggable recordEntry, String event, String format, Object... args) {
        getEventManager().event(recordEntry, event, format, args);
    }

    public static void registerEventListener(EventManager.EventListener e) {
        getEventManager().registerEventListener(e);
    }

    public static void addRequestResponsePair(EventManager.TimedEventPair p) {
        getEventManager().addRequestResponsePair(p);
    }

    public static void dumpEvents(IndentingPrintWriter pw) {
        synchronized (sSingletonSync) {
            if (sEventManager != null) {
                getEventManager().dumpEvents(pw);
            } else {
                pw.println("No Historical Events Logged.");
            }
        }
    }

    public static void dumpEventsTimeline(IndentingPrintWriter pw) {
        synchronized (sSingletonSync) {
            if (sEventManager != null) {
                getEventManager().dumpEventsTimeline(pw);
            } else {
                pw.println("No Historical Events Logged.");
            }
        }
    }

    public static void setIsExtendedLoggingEnabled(boolean isExtendedLoggingEnabled) {
        if (sIsUserExtendedLoggingEnabled != isExtendedLoggingEnabled) {
            EventManager eventManager = sEventManager;
            if (eventManager != null) {
                eventManager.changeEventCacheSize(isExtendedLoggingEnabled ? 20 : 10);
            }
            sIsUserExtendedLoggingEnabled = isExtendedLoggingEnabled;
            if (sIsUserExtendedLoggingEnabled) {
                sUserExtendedLoggingStopTime = System.currentTimeMillis() + 1800000;
            } else {
                sUserExtendedLoggingStopTime = 0;
            }
        }
    }

    private static EventManager getEventManager() {
        if (sEventManager == null) {
            synchronized (sSingletonSync) {
                if (sEventManager == null) {
                    sEventManager = new EventManager($$Lambda$qa4s1Fm2YuohEunaJUJcmJXDXG0.INSTANCE);
                    EventManager eventManager = sEventManager;
                    return eventManager;
                }
            }
        }
        return sEventManager;
    }

    @VisibleForTesting
    public static SessionManager getSessionManager() {
        if (sSessionManager == null) {
            synchronized (sSingletonSync) {
                if (sSessionManager == null) {
                    sSessionManager = new SessionManager();
                    SessionManager sessionManager = sSessionManager;
                    return sessionManager;
                }
            }
        }
        return sSessionManager;
    }

    public static void setTag(String tag) {
        TAG = tag;
        WARN = isLoggable(5);
        ERROR = isLoggable(6);
    }

    private static void maybeDisableLogging() {
        if (sIsUserExtendedLoggingEnabled && sUserExtendedLoggingStopTime < System.currentTimeMillis()) {
            sUserExtendedLoggingStopTime = 0;
            sIsUserExtendedLoggingEnabled = false;
        }
    }

    public static boolean isLoggable(int level) {
        return android.util.Log.isLoggable(TAG, level);
    }

    public static String piiHandle(Object pii) {
        if (pii == null || VERBOSE) {
            return String.valueOf(pii);
        }
        StringBuilder sb = new StringBuilder();
        if (pii instanceof Uri) {
            Uri uri = (Uri) pii;
            String scheme = uri.getScheme();
            if (!TextUtils.isEmpty(scheme)) {
                sb.append(scheme);
                sb.append(SettingsStringUtil.DELIMITER);
            }
            String textToObfuscate = uri.getSchemeSpecificPart();
            if (PhoneAccount.SCHEME_TEL.equals(scheme)) {
                obfuscatePhoneNumber(sb, textToObfuscate);
            } else if ("sip".equals(scheme)) {
                for (int i = 0; i < textToObfuscate.length(); i++) {
                    char c = textToObfuscate.charAt(i);
                    if (!(c == '@' || c == '.')) {
                        c = '*';
                    }
                    sb.append(c);
                }
            } else {
                sb.append(pii(pii));
            }
        } else if (pii instanceof String) {
            obfuscatePhoneNumber(sb, (String) pii);
        }
        return sb.toString();
    }

    private static void obfuscatePhoneNumber(StringBuilder sb, String phoneNumber) {
        int numDigitsToObfuscate = getDialableCount(phoneNumber) - NUM_DIALABLE_DIGITS_TO_LOG;
        for (int i = 0; i < phoneNumber.length(); i++) {
            char c = phoneNumber.charAt(i);
            boolean isDialable = PhoneNumberUtils.isDialable(c);
            if (isDialable) {
                numDigitsToObfuscate--;
            }
            sb.append((!isDialable || numDigitsToObfuscate < 0) ? Character.valueOf(c) : "*");
        }
    }

    private static int getDialableCount(String toCount) {
        int numDialable = 0;
        for (char c : toCount.toCharArray()) {
            if (PhoneNumberUtils.isDialable(c)) {
                numDialable++;
            }
        }
        return numDialable;
    }

    public static String pii(Object pii) {
        if (pii == null || VERBOSE) {
            return String.valueOf(pii);
        }
        return "***";
    }

    private static String getPrefixFromObject(Object obj) {
        return obj == null ? "<null>" : obj.getClass().getSimpleName();
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: android.telecom.Log.e(java.lang.String, java.lang.Throwable, java.lang.String, java.lang.Object[]):void
     arg types: [java.lang.String, java.util.IllegalFormatException, java.lang.String, java.lang.Object[]]
     candidates:
      android.telecom.Log.e(java.lang.Object, java.lang.Throwable, java.lang.String, java.lang.Object[]):void
      android.telecom.Log.e(java.lang.String, java.lang.Throwable, java.lang.String, java.lang.Object[]):void */
    private static String buildMessage(String prefix, String format, Object... args) {
        String sessionPostfix;
        String msg;
        if (TextUtils.isEmpty(null)) {
            sessionPostfix = "";
        } else {
            sessionPostfix = ": " + ((String) null);
        }
        if (args != null) {
            try {
                if (args.length != 0) {
                    msg = String.format(Locale.US, format, args);
                    return String.format(Locale.US, "%s: %s%s", prefix, msg, sessionPostfix);
                }
            } catch (IllegalFormatException ife) {
                e(TAG, (Throwable) ife, "Log: IllegalFormatException: formatString='%s' numArgs=%d", format, Integer.valueOf(args.length));
                msg = format + " (An error occurred while formatting the message.)";
            }
        }
        msg = format;
        return String.format(Locale.US, "%s: %s%s", prefix, msg, sessionPostfix);
    }
}
