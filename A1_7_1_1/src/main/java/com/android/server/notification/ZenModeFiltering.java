package com.android.server.notification;

import android.content.ComponentName;
import android.content.Context;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Settings.Secure;
import android.service.notification.ZenModeConfig;
import android.telecom.TelecomManager;
import android.util.ArrayMap;
import android.util.Slog;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Objects;

/*  JADX ERROR: NullPointerException in pass: ReSugarCode
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
    	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ExtractFieldInit.checkStaticFieldsInit(ExtractFieldInit.java:58)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class ZenModeFiltering {
    private static final boolean DEBUG = false;
    static final RepeatCallers REPEAT_CALLERS = null;
    private static final String TAG = "ZenModeHelper";
    private final Context mContext;
    private ComponentName mDefaultPhoneApp;

    private static class RepeatCallers {
        private final ArrayMap<String, Long> mCalls;
        private int mThresholdMinutes;

        /* synthetic */ RepeatCallers(RepeatCallers repeatCallers) {
            this();
        }

        private RepeatCallers() {
            this.mCalls = new ArrayMap();
        }

        private synchronized void recordCall(Context context, Bundle extras) {
        }

        /* JADX WARNING: Missing block: B:7:0x000c, code:
            return false;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private synchronized boolean isRepeat(Context context, Bundle extras) {
            setThresholdMinutes(context);
            if (this.mThresholdMinutes > 0 && extras != null) {
                String peopleString = peopleString(extras);
                if (peopleString == null) {
                    return false;
                }
                long now = System.currentTimeMillis();
                cleanUp(this.mCalls, now);
                boolean isRepeat = this.mCalls.containsKey(peopleString);
                Slog.d(ZenModeFiltering.TAG, "isRepeat_" + isRepeat + ", mCalls = " + this.mCalls + ", peopleString = " + peopleString);
                this.mCalls.put(peopleString, Long.valueOf(now));
                return isRepeat;
            }
        }

        private synchronized void cleanUp(ArrayMap<String, Long> calls, long now) {
            for (int i = calls.size() - 1; i >= 0; i--) {
                long time = ((Long) this.mCalls.valueAt(i)).longValue();
                if (time > now || now - time > ((long) ((this.mThresholdMinutes * 1000) * 60))) {
                    calls.removeAt(i);
                }
            }
        }

        private void setThresholdMinutes(Context context) {
            if (this.mThresholdMinutes <= 0) {
                this.mThresholdMinutes = context.getResources().getInteger(17694872);
            }
        }

        private static String peopleString(Bundle extras) {
            String str = null;
            String[] extraPeople = ValidateNotificationPeople.getExtraPeople(extras);
            if (extraPeople == null || extraPeople.length == 0) {
                return null;
            }
            StringBuilder sb = new StringBuilder();
            for (String extraPerson : extraPeople) {
                String extraPerson2;
                if (extraPerson2 != null) {
                    extraPerson2 = extraPerson2.trim();
                    if (!extraPerson2.isEmpty()) {
                        if (sb.length() > 0) {
                            sb.append('|');
                        }
                        sb.append(extraPerson2);
                    }
                }
            }
            if (sb.length() != 0) {
                str = sb.toString();
            }
            return str;
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.notification.ZenModeFiltering.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.notification.ZenModeFiltering.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.notification.ZenModeFiltering.<clinit>():void");
    }

    public ZenModeFiltering(Context context) {
        this.mContext = context;
    }

    public void dump(PrintWriter pw, String prefix) {
        pw.print(prefix);
        pw.print("mDefaultPhoneApp=");
        pw.println(this.mDefaultPhoneApp);
        pw.print(prefix);
        pw.print("RepeatCallers.mThresholdMinutes=");
        pw.println(REPEAT_CALLERS.mThresholdMinutes);
        synchronized (REPEAT_CALLERS) {
            if (!REPEAT_CALLERS.mCalls.isEmpty()) {
                pw.print(prefix);
                pw.println("RepeatCallers.mCalls=");
                for (int i = 0; i < REPEAT_CALLERS.mCalls.size(); i++) {
                    pw.print(prefix);
                    pw.print("  ");
                    pw.print((String) REPEAT_CALLERS.mCalls.keyAt(i));
                    pw.print(" at ");
                    pw.println(ts(((Long) REPEAT_CALLERS.mCalls.valueAt(i)).longValue()));
                }
            }
        }
    }

    private static String ts(long time) {
        return new Date(time) + " (" + time + ")";
    }

    public static boolean matchesCallFilter(Context context, int zen, ZenModeConfig config, UserHandle userHandle, Bundle extras, ValidateNotificationPeople validator, int contactsTimeoutMs, float timeoutAffinity) {
        if (DEBUG) {
            Slog.d(TAG, "matchesCallFilter_zen = " + zen + ", config.allowRepeatCallers = " + config.allowRepeatCallers + ", config.allowCalls = " + config.allowCalls + ", validator = " + validator);
        }
        if (zen == 2 || zen == 3) {
            return false;
        }
        if (zen == 1) {
            if (config.allowRepeatCallers && REPEAT_CALLERS.isRepeat(context, extras)) {
                if (DEBUG) {
                    Slog.d(TAG, "matchesCallFilter_config.allowRepeatCallers and isRepeat is true");
                }
                return true;
            } else if (!config.allowCalls) {
                return false;
            } else {
                if (validator != null) {
                    return audienceMatches(config.allowCallsFrom, validator.getContactAffinity(userHandle, extras, contactsTimeoutMs, timeoutAffinity));
                }
            }
        }
        return true;
    }

    private static Bundle extras(NotificationRecord record) {
        if (record == null || record.sbn == null || record.sbn.getNotification() == null) {
            return null;
        }
        return record.sbn.getNotification().extras;
    }

    protected void recordCall(NotificationRecord record) {
        REPEAT_CALLERS.recordCall(this.mContext, extras(record));
    }

    public boolean shouldIntercept(int zen, ZenModeConfig config, NotificationRecord record) {
        if (isSystem(record)) {
            return false;
        }
        switch (zen) {
            case 1:
                if (isAlarm(record)) {
                    return false;
                }
                if (record.getPackagePriority() == 2) {
                    ZenLog.traceNotIntercepted(record, "priorityApp");
                    return false;
                } else if (isCall(record)) {
                    if (config.allowRepeatCallers && REPEAT_CALLERS.isRepeat(this.mContext, extras(record))) {
                        ZenLog.traceNotIntercepted(record, "repeatCaller");
                        return false;
                    } else if (config.allowCalls) {
                        return shouldInterceptAudience(config.allowCallsFrom, record);
                    } else {
                        ZenLog.traceIntercepted(record, "!allowCalls");
                        return true;
                    }
                } else if (isMessage(record)) {
                    if (config.allowMessages) {
                        return shouldInterceptAudience(config.allowMessagesFrom, record);
                    }
                    ZenLog.traceIntercepted(record, "!allowMessages");
                    return true;
                } else if (config.allowReminders) {
                    return false;
                } else {
                    ZenLog.traceIntercepted(record, "!priority");
                    return true;
                }
            case 2:
                ZenLog.traceIntercepted(record, "none");
                return true;
            case 3:
                if (isAlarm(record)) {
                    return false;
                }
                ZenLog.traceIntercepted(record, "alarmsOnly");
                return true;
            default:
                return false;
        }
    }

    private static boolean shouldInterceptAudience(int source, NotificationRecord record) {
        if (audienceMatches(source, record.getContactAffinity())) {
            return false;
        }
        ZenLog.traceIntercepted(record, "!audienceMatches");
        return true;
    }

    private static boolean isSystem(NotificationRecord record) {
        return record.isCategory("sys");
    }

    private static boolean isAlarm(NotificationRecord record) {
        if (record.isCategory("alarm") || record.isAudioStream(4)) {
            return true;
        }
        return record.isAudioAttributesUsage(4);
    }

    private static boolean isEvent(NotificationRecord record) {
        return record.isCategory("event");
    }

    private static boolean isReminder(NotificationRecord record) {
        return record.isCategory("reminder");
    }

    public boolean isCall(NotificationRecord record) {
        if (record == null) {
            return false;
        }
        if (isDefaultPhoneApp(record.sbn.getPackageName())) {
            return true;
        }
        return record.isCategory("call");
    }

    private boolean isDefaultPhoneApp(String pkg) {
        ComponentName componentName = null;
        if (this.mDefaultPhoneApp == null) {
            TelecomManager telecomm = (TelecomManager) this.mContext.getSystemService("telecom");
            if (telecomm != null) {
                componentName = telecomm.getDefaultPhoneApp();
            }
            this.mDefaultPhoneApp = componentName;
            if (DEBUG) {
                Slog.d(TAG, "Default phone app: " + this.mDefaultPhoneApp);
            }
        }
        if (pkg == null || this.mDefaultPhoneApp == null) {
            return false;
        }
        return pkg.equals(this.mDefaultPhoneApp.getPackageName());
    }

    private boolean isDefaultMessagingApp(NotificationRecord record) {
        int userId = record.getUserId();
        if (userId == -10000 || userId == -1) {
            return false;
        }
        return Objects.equals(Secure.getStringForUser(this.mContext.getContentResolver(), "sms_default_application", userId), record.sbn.getPackageName());
    }

    private boolean isMessage(NotificationRecord record) {
        return !record.isCategory("msg") ? isDefaultMessagingApp(record) : true;
    }

    private static boolean audienceMatches(int source, float contactAffinity) {
        boolean z = true;
        if (DEBUG) {
            Slog.d(TAG, "audienceMatches_source = " + source + ", contactAffinity = " + contactAffinity);
        }
        switch (source) {
            case 0:
                return true;
            case 1:
                if (contactAffinity < 0.5f) {
                    z = false;
                }
                return z;
            case 2:
                if (contactAffinity < 1.0f) {
                    z = false;
                }
                return z;
            default:
                Slog.w(TAG, "Encountered unknown source: " + source);
                return true;
        }
    }
}
