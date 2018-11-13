package com.android.server.usage;

import android.app.usage.ConfigurationStats;
import android.app.usage.TimeSparseArray;
import android.app.usage.UsageEvents;
import android.app.usage.UsageEvents.Event;
import android.app.usage.UsageStats;
import android.content.Context;
import android.content.res.Configuration;
import android.os.SystemClock;
import android.text.format.DateUtils;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.Slog;
import com.android.internal.util.IndentingPrintWriter;
import com.android.server.audio.AudioService;
import com.android.server.usage.UsageStatsDatabase.CheckinAction;
import com.android.server.voiceinteraction.DatabaseHelper.SoundModelContract;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
class UserUsageStatsService {
    private static final boolean DEBUG = false;
    private static final long[] INTERVAL_LENGTH = null;
    private static final String TAG = "UsageStatsService";
    private static final StatCombiner<ConfigurationStats> sConfigStatsCombiner = null;
    private static final SimpleDateFormat sDateFormat = null;
    private static final int sDateFormatFlags = 131093;
    private static final StatCombiner<UsageStats> sUsageStatsCombiner = null;
    private final Context mContext;
    private final IntervalStats[] mCurrentStats;
    private final UnixCalendar mDailyExpiryDate;
    private final UsageStatsDatabase mDatabase;
    private final StatsUpdatedListener mListener;
    private final String mLogPrefix;
    private boolean mStatsChanged;
    private final int mUserId;

    interface StatsUpdatedListener {
        void onNewUpdate(int i);

        void onStatsReloaded();

        void onStatsUpdated();
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.usage.UserUsageStatsService.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.usage.UserUsageStatsService.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.usage.UserUsageStatsService.<clinit>():void");
    }

    UserUsageStatsService(Context context, int userId, File usageStatsDir, StatsUpdatedListener listener) {
        this.mStatsChanged = false;
        this.mContext = context;
        this.mDailyExpiryDate = new UnixCalendar(0);
        this.mDatabase = new UsageStatsDatabase(usageStatsDir);
        this.mCurrentStats = new IntervalStats[4];
        this.mListener = listener;
        this.mLogPrefix = "User[" + Integer.toString(userId) + "] ";
        this.mUserId = userId;
    }

    void init(long currentTimeMillis) {
        int i;
        this.mDatabase.init(currentTimeMillis);
        int nullCount = 0;
        for (i = 0; i < this.mCurrentStats.length; i++) {
            this.mCurrentStats[i] = this.mDatabase.getLatestUsageStats(i);
            if (this.mCurrentStats[i] == null) {
                nullCount++;
            }
        }
        if (nullCount > 0) {
            if (nullCount != this.mCurrentStats.length) {
                Slog.w(TAG, this.mLogPrefix + "Some stats have no latest available");
            }
            loadActiveStats(currentTimeMillis);
        } else {
            updateRolloverDeadline();
        }
        for (IntervalStats stat : this.mCurrentStats) {
            int pkgCount = stat.packageStats.size();
            for (i = 0; i < pkgCount; i++) {
                UsageStats pkgStats = (UsageStats) stat.packageStats.valueAt(i);
                if (pkgStats.mLastEvent == 1 || pkgStats.mLastEvent == 4) {
                    stat.update(pkgStats.mPackageName, stat.lastTimeSaved, 3);
                    notifyStatsChanged();
                }
            }
            stat.updateConfigurationStats(null, stat.lastTimeSaved);
        }
        if (this.mDatabase.isNewUpdate()) {
            notifyNewUpdate();
        }
    }

    void onTimeChanged(long oldTime, long newTime) {
        persistActiveStats();
        this.mDatabase.onTimeChanged(newTime - oldTime);
        loadActiveStats(newTime);
    }

    void reportEvent(Event event) {
        int i = 0;
        if (event.mTimeStamp >= this.mDailyExpiryDate.getTimeInMillis()) {
            rolloverStats(event.mTimeStamp);
        }
        IntervalStats currentDailyStats = this.mCurrentStats[0];
        Configuration newFullConfig = event.mConfiguration;
        if (event.mEventType == 5 && currentDailyStats.activeConfiguration != null) {
            event.mConfiguration = Configuration.generateDelta(currentDailyStats.activeConfiguration, newFullConfig);
        }
        if (currentDailyStats.events == null) {
            currentDailyStats.events = new TimeSparseArray();
        }
        if (event.mEventType != 6) {
            currentDailyStats.events.put(event.mTimeStamp, event);
        }
        IntervalStats[] intervalStatsArr = this.mCurrentStats;
        int length = intervalStatsArr.length;
        while (i < length) {
            IntervalStats stats = intervalStatsArr[i];
            if (event.mEventType == 5) {
                stats.updateConfigurationStats(newFullConfig, event.mTimeStamp);
            } else {
                stats.update(event.mPackage, event.mTimeStamp, event.mEventType);
            }
            i++;
        }
        notifyStatsChanged();
    }

    private <T> List<T> queryStats(int intervalType, long beginTime, long endTime, StatCombiner<T> combiner) {
        if (intervalType == 4) {
            intervalType = this.mDatabase.findBestFitBucket(beginTime, endTime);
            if (intervalType < 0) {
                intervalType = 0;
            }
        }
        if (intervalType < 0 || intervalType >= this.mCurrentStats.length) {
            return null;
        }
        IntervalStats currentStats = this.mCurrentStats[intervalType];
        if (beginTime >= currentStats.endTime) {
            return null;
        }
        List<T> results = this.mDatabase.queryUsageStats(intervalType, beginTime, Math.min(currentStats.beginTime, endTime), combiner);
        if (beginTime < currentStats.endTime && endTime > currentStats.beginTime) {
            if (results == null) {
                results = new ArrayList();
            }
            combiner.combine(currentStats, true, results);
        }
        return results;
    }

    List<UsageStats> queryUsageStats(int bucketType, long beginTime, long endTime) {
        return queryStats(bucketType, beginTime, endTime, sUsageStatsCombiner);
    }

    List<ConfigurationStats> queryConfigurationStats(int bucketType, long beginTime, long endTime) {
        return queryStats(bucketType, beginTime, endTime, sConfigStatsCombiner);
    }

    UsageEvents queryEvents(long beginTime, long endTime) {
        final ArraySet<String> names = new ArraySet();
        final long j = beginTime;
        final long j2 = endTime;
        List<Event> results = queryStats(0, beginTime, endTime, new StatCombiner<Event>() {
            public void combine(IntervalStats stats, boolean mutable, List<Event> accumulatedResult) {
                if (stats.events != null) {
                    int startIndex = stats.events.closestIndexOnOrAfter(j);
                    if (startIndex >= 0) {
                        int size = stats.events.size();
                        int i = startIndex;
                        while (i < size && stats.events.keyAt(i) < j2) {
                            Event event = (Event) stats.events.valueAt(i);
                            names.add(event.mPackage);
                            if (event.mClass != null) {
                                names.add(event.mClass);
                            }
                            accumulatedResult.add(event);
                            i++;
                        }
                    }
                }
            }
        });
        if (results == null || results.isEmpty()) {
            return null;
        }
        String[] table = (String[]) names.toArray(new String[names.size()]);
        Arrays.sort(table);
        return new UsageEvents(results, table);
    }

    void persistActiveStats() {
        if (this.mStatsChanged) {
            Slog.i(TAG, this.mLogPrefix + "Flushing usage stats to disk");
            int i = 0;
            while (i < this.mCurrentStats.length) {
                try {
                    this.mDatabase.putUsageStats(i, this.mCurrentStats[i]);
                    i++;
                } catch (IOException e) {
                    Slog.e(TAG, this.mLogPrefix + "Failed to persist active stats", e);
                    return;
                }
            }
            this.mStatsChanged = false;
        }
    }

    private void rolloverStats(long currentTimeMillis) {
        int i;
        long startTime = SystemClock.elapsedRealtime();
        Slog.i(TAG, this.mLogPrefix + "Rolling over usage stats");
        Configuration previousConfig = this.mCurrentStats[0].activeConfiguration;
        ArraySet<String> continuePreviousDay = new ArraySet();
        for (IntervalStats stat : this.mCurrentStats) {
            int pkgCount = stat.packageStats.size();
            for (i = 0; i < pkgCount; i++) {
                UsageStats pkgStats = (UsageStats) stat.packageStats.valueAt(i);
                if (pkgStats.mLastEvent == 1 || pkgStats.mLastEvent == 4) {
                    continuePreviousDay.add(pkgStats.mPackageName);
                    stat.update(pkgStats.mPackageName, this.mDailyExpiryDate.getTimeInMillis() - 1, 3);
                    notifyStatsChanged();
                }
            }
            stat.updateConfigurationStats(null, this.mDailyExpiryDate.getTimeInMillis() - 1);
        }
        persistActiveStats();
        this.mDatabase.prune(currentTimeMillis);
        loadActiveStats(currentTimeMillis);
        int continueCount = continuePreviousDay.size();
        for (i = 0; i < continueCount; i++) {
            String name = (String) continuePreviousDay.valueAt(i);
            long beginTime = this.mCurrentStats[0].beginTime;
            for (IntervalStats stat2 : this.mCurrentStats) {
                stat2.update(name, beginTime, 4);
                stat2.updateConfigurationStats(previousConfig, beginTime);
                notifyStatsChanged();
            }
        }
        persistActiveStats();
        Slog.i(TAG, this.mLogPrefix + "Rolling over usage stats complete. Took " + (SystemClock.elapsedRealtime() - startTime) + " milliseconds");
    }

    private void notifyStatsChanged() {
        if (!this.mStatsChanged) {
            this.mStatsChanged = true;
            this.mListener.onStatsUpdated();
        }
    }

    private void notifyNewUpdate() {
        this.mListener.onNewUpdate(this.mUserId);
    }

    private void loadActiveStats(long currentTimeMillis) {
        int intervalType = 0;
        while (intervalType < this.mCurrentStats.length) {
            IntervalStats stats = this.mDatabase.getLatestUsageStats(intervalType);
            if (stats == null || currentTimeMillis - 500 < stats.endTime || currentTimeMillis >= stats.beginTime + INTERVAL_LENGTH[intervalType]) {
                this.mCurrentStats[intervalType] = new IntervalStats();
                this.mCurrentStats[intervalType].beginTime = currentTimeMillis;
                this.mCurrentStats[intervalType].endTime = 1 + currentTimeMillis;
            } else {
                this.mCurrentStats[intervalType] = stats;
            }
            intervalType++;
        }
        this.mStatsChanged = false;
        updateRolloverDeadline();
        this.mListener.onStatsReloaded();
    }

    private void updateRolloverDeadline() {
        this.mDailyExpiryDate.setTimeInMillis(this.mCurrentStats[0].beginTime);
        this.mDailyExpiryDate.addDays(1);
        Slog.i(TAG, this.mLogPrefix + "Rollover scheduled @ " + sDateFormat.format(Long.valueOf(this.mDailyExpiryDate.getTimeInMillis())) + "(" + this.mDailyExpiryDate.getTimeInMillis() + ")");
    }

    void checkin(final IndentingPrintWriter pw) {
        this.mDatabase.checkinDailyFiles(new CheckinAction() {
            public boolean checkin(IntervalStats stats) {
                UserUsageStatsService.this.printIntervalStats(pw, stats, false);
                return true;
            }
        });
    }

    void dump(IndentingPrintWriter pw) {
        for (int interval = 0; interval < this.mCurrentStats.length; interval++) {
            pw.print("In-memory ");
            pw.print(intervalToString(interval));
            pw.println(" stats");
            printIntervalStats(pw, this.mCurrentStats[interval], true);
        }
    }

    private String formatDateTime(long dateTime, boolean pretty) {
        if (pretty) {
            return "\"" + DateUtils.formatDateTime(this.mContext, dateTime, sDateFormatFlags) + "\"";
        }
        return Long.toString(dateTime);
    }

    private String formatElapsedTime(long elapsedTime, boolean pretty) {
        if (pretty) {
            return "\"" + DateUtils.formatElapsedTime(elapsedTime / 1000) + "\"";
        }
        return Long.toString(elapsedTime);
    }

    void printIntervalStats(IndentingPrintWriter pw, IntervalStats stats, boolean prettyDates) {
        int i;
        if (prettyDates) {
            pw.printPair("timeRange", "\"" + DateUtils.formatDateRange(this.mContext, stats.beginTime, stats.endTime, sDateFormatFlags) + "\"");
        } else {
            pw.printPair("beginTime", Long.valueOf(stats.beginTime));
            pw.printPair("endTime", Long.valueOf(stats.endTime));
        }
        pw.println();
        pw.increaseIndent();
        pw.println("packages");
        pw.increaseIndent();
        ArrayMap<String, UsageStats> pkgStats = stats.packageStats;
        int pkgCount = pkgStats.size();
        for (i = 0; i < pkgCount; i++) {
            UsageStats usageStats = (UsageStats) pkgStats.valueAt(i);
            pw.printPair("package", usageStats.mPackageName);
            pw.printPair("totalTime", formatElapsedTime(usageStats.mTotalTimeInForeground, prettyDates));
            pw.printPair("lastTime", formatDateTime(usageStats.mLastTimeUsed, prettyDates));
            pw.println();
        }
        pw.decreaseIndent();
        pw.println("configurations");
        pw.increaseIndent();
        ArrayMap<Configuration, ConfigurationStats> configStats = stats.configurations;
        int configCount = configStats.size();
        for (i = 0; i < configCount; i++) {
            ConfigurationStats config = (ConfigurationStats) configStats.valueAt(i);
            pw.printPair("config", Configuration.resourceQualifierString(config.mConfiguration));
            pw.printPair("totalTime", formatElapsedTime(config.mTotalTimeActive, prettyDates));
            pw.printPair("lastTime", formatDateTime(config.mLastTimeActive, prettyDates));
            pw.printPair("count", Integer.valueOf(config.mActivationCount));
            pw.println();
        }
        pw.decreaseIndent();
        pw.println("events");
        pw.increaseIndent();
        TimeSparseArray<Event> events = stats.events;
        int eventCount = events != null ? events.size() : 0;
        for (i = 0; i < eventCount; i++) {
            Event event = (Event) events.valueAt(i);
            pw.printPair("time", formatDateTime(event.mTimeStamp, prettyDates));
            pw.printPair(SoundModelContract.KEY_TYPE, eventToString(event.mEventType));
            pw.printPair("package", event.mPackage);
            if (event.mClass != null) {
                pw.printPair(AudioService.CONNECT_INTENT_KEY_DEVICE_CLASS, event.mClass);
            }
            if (event.mConfiguration != null) {
                pw.printPair("config", Configuration.resourceQualifierString(event.mConfiguration));
            }
            if (event.mShortcutId != null) {
                pw.printPair("shortcutId", event.mShortcutId);
            }
            pw.println();
        }
        pw.decreaseIndent();
        pw.decreaseIndent();
    }

    private static String intervalToString(int interval) {
        switch (interval) {
            case 0:
                return "daily";
            case 1:
                return "weekly";
            case 2:
                return "monthly";
            case 3:
                return "yearly";
            default:
                return "?";
        }
    }

    private static String eventToString(int eventType) {
        switch (eventType) {
            case 0:
                return "NONE";
            case 1:
                return "MOVE_TO_FOREGROUND";
            case 2:
                return "MOVE_TO_BACKGROUND";
            case 3:
                return "END_OF_DAY";
            case 4:
                return "CONTINUE_PREVIOUS_DAY";
            case 5:
                return "CONFIGURATION_CHANGE";
            case 6:
                return "SYSTEM_INTERACTION";
            case 7:
                return "USER_INTERACTION";
            case 8:
                return "SHORTCUT_INVOCATION";
            default:
                return "UNKNOWN";
        }
    }

    byte[] getBackupPayload(String key) {
        return this.mDatabase.getBackupPayload(key);
    }

    void applyRestoredPayload(String key, byte[] payload) {
        this.mDatabase.applyRestoredPayload(key, payload);
    }
}
