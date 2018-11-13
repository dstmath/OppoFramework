package com.android.server;

import android.os.FileObserver;
import android.util.Log;
import android.util.Xml;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class OppoGuardElfConfigUtil {
    public static final String ABNORMAL_START_COUNT = "AbnormalStartCount";
    private static final int ABNORMAL_START_COUNT_DEFA_VALUE = 50;
    private static final boolean APP_CPU_KILL_SWITCH = true;
    public static final String BROADCAST_FINISH_TIME = "BroadcastFinishTime";
    private static final long BROADCAST_FINISH_TIME_VALUE = 6000;
    public static final String BROADCAST_SEND_COUNT = "BroadcastSendCount";
    private static final long BROADCAST_SEND_COUNT_VALUE = 60;
    private static final long CHECK_BROADCAST_SERVICE_TIME_OUT = 1800000;
    public static final String CHECK_BRORDCAST_SERVICE_TIME = "CheckBroadcastServiceTime";
    public static final String CHECK_START_TIME_INTERVAL = "CheckStartTimeInterval";
    private static final long CHECK_START_TIME_INTERVAL_DEFA_VALUE = 300000;
    public static final String CHECK_TOP_BROADCAST_TIME = "CheckTopBroadcastTime";
    private static final long CHECK_TOP_BROADCAST_TIME_OUT = 600000;
    public static final String CLOSE_FLAG = "CloseFlag";
    public static final String COLLECT_START_COUNT = "CollectStartCount";
    private static final int COLLECT_START_COUNT_DEFA_VALUE = 30;
    private static final int DFT_THRESHOLD_BATT_IDLE_DELAY = 300;
    private static final int DFT_THRESHOLD_BATT_IDLE_LOW_LEVEL = 30;
    private static final int DFT_THRESHOLD_BATT_IDLE_NORMAL_LEVEL = 70;
    private static final int DFT_THRESHOLD_JOB_MIN_INTERVAL = 3600;
    private static final long INTERVAL_APP_CPU_KILL = 60000;
    private static final long INTERVAL_TOTAL_CPU_SAMPLE = 5000;
    private static final String OPPO_GUARD_ELF_CONFIG_PATH = "/data/oppo/coloros/oppoguardelf/oppoguardelf_config.xml";
    private static final String OPPO_GUARD_ELF_PATH = "/data/oppo/coloros/oppoguardelf";
    public static final String SERVICE_FINISH_TIME = "ServiceFinishTime";
    private static final long SERVICE_FINISH_TIME_VALUE = 10000;
    public static String TAG = "OppoGuardElfConfigUtil";
    private static final String TAG_APP_CPU_KILL_SWITCH = "AppCpuKillSwitch";
    private static final String TAG_INTERVAL_APP_CPU_KILL = "IntervalAppCpuKill";
    private static final String TAG_INTERVAL_TOTAL_CPU_SAMPLE = "IntervalTotalCpuSample";
    private static final String TAG_THRESHOLD_BATT_IDLE_DELAY = "ThreshBattIdleDelay";
    private static final String TAG_THRESHOLD_BATT_IDLE_LOW_LEVEL = "ThreshBattIdleLowLevel";
    private static final String TAG_THRESHOLD_BATT_IDLE_NORMAL_LEVEL = "ThreshBattIdleNormalLevel";
    private static final String TAG_THRESHOLD_JOB_MIN_INTERVAL = "ThreshJobMinInterval";
    private static final String TAG_THRESH_COUNT_CONTINUOUS_HEAVY = "ThreshCountContinuousHeavy";
    private static final String TAG_THRESH_COUNT_CONTINUOUS_MIDDLE = "ThreshCountContinuousMiddle";
    private static final String TAG_THRESH_COUNT_CONTINUOUS_SLIGHT = "ThreshCountContinuousSlight";
    private static final String TAG_THRESH_TOTAL_CPU_HEAVY = "ThreshTotalCpuHeavy";
    private static final String TAG_THRESH_TOTAL_CPU_MIDDLE = "ThreshTotalCpuMiddle";
    private static final String TAG_THRESH_TOTAL_CPU_SLIGHT = "ThreshTotalCpuSlight";
    private static final String TAG_TIME_FORE_APP_STABLE = "ForeAppStableTime";
    private static final String TAG_TOTAL_CPU_MONITOR_SWITCH = "TotalCpuMonitorSwitch";
    public static final String THRESHOLD_INTERVAL_PER_WAKEUP = "ThresholdIntervalPerWakeup";
    private static final long THRESHOLD_INTERVAL_PER_WAKEUP_DEFA_VALUE = 300;
    public static final String THRESHOLD_SERIOUS_INTERVAL_PER_ALARM = "ThresholdSeriousIntervalPerAlarm";
    private static final long THRESHOLD_SERIOUS_INTERVAL_PER_ALARM_DEFA_VALUE = 180;
    public static final String THRESHOLD_WAKELOCK_TIMEOUT = "ThresholdWakeLockTimeout";
    private static final long THRESHOLD_WAKELOCK_TIMEOUT_DEFA_VALUE = 300;
    public static final String THRESHOLD_WARNING_INTERVAL_PER_WAKEUP = "ThresholdWarningIntervalPerWakeup";
    private static final long THRESHOLD_WARNING_INTERVAL_PER_WAKEUP_DEFA_VALUE = 360;
    public static final String THRESHOLD_WORST_INTERVAL_PER_WAKEUP = "ThresholdWorstIntervalPerWakeup";
    private static final long THRESHOLD_WORST_INTERVAL_PER_WAKEUP_DEFA_VALUE = 60;
    private static final int THRESH_COUNT_CONTINUOUS_HEAVY = 2;
    private static final int THRESH_COUNT_CONTINUOUS_MIDDLE = 3;
    private static final int THRESH_COUNT_CONTINUOUS_SLIGHT = 4;
    private static final int THRESH_TOTAL_CPU_HEAVY = 60;
    private static final int THRESH_TOTAL_CPU_MIDDLE = 40;
    private static final int THRESH_TOTAL_CPU_SLIGHT = 20;
    private static final long TIME_FORE_APP_STABLE = 10000;
    public static final String TOP_BROADCAST_NUMBER = "TopNumber";
    public static final int TOP_NUMBER = 3;
    private static final boolean TOTAL_CPU_MONITOR_SWITCH = true;
    private static boolean mAppCpuKillSwitch = true;
    private static long mIntervalAppCpuKill = 60000;
    private static long mIntervalTotalCpuSample = 5000;
    private static OppoGuardElfConfigUtil mOppoGuardElfConfigUtil = null;
    private static int mThreshCountContinuousHeavy = 2;
    private static int mThreshCountContinuousMiddle = 3;
    private static int mThreshCountContinuousSlight = 4;
    private static int mThreshTotalCpuHeavy = 60;
    private static int mThreshTotalCpuMiddle = 40;
    private static int mThreshTotalCpuSlight = 20;
    private static long mTimeForeAppStable = 10000;
    private static boolean mTotalCpuMonitorSwitch = true;
    public int mAbnormalStartCount = 50;
    public long mBroadcastFinishTime = 6000;
    public long mBroadcastSendCount = 60;
    public long mCheckBroadcastServiceTime = 1800000;
    public long mCheckStartTimeInterval = 300000;
    public long mCheckTopBroadcastTime = 600000;
    public boolean mCloseFlag = true;
    public int mCollectStartCount = 30;
    private FileObserverPolicy mGuardElfConfigFileObserver = null;
    public long mServiceFinishTime = 10000;
    private int mThreshBattIdleDelay = 300;
    private int mThreshBattIdleLowLevel = 30;
    private int mThreshBattIdleNormalLevel = 70;
    private long mThreshJobMinInterval = 3600;
    public long mThresholdIntervalPerWakeup = 300;
    public long mThresholdSeriousIntervalPerAlarm = THRESHOLD_SERIOUS_INTERVAL_PER_ALARM_DEFA_VALUE;
    public long mThresholdWakeLockTimeout = 300;
    public long mThresholdWarningIntervalPerWakeup = THRESHOLD_WARNING_INTERVAL_PER_WAKEUP_DEFA_VALUE;
    public long mThresholdWorstIntervalPerWakeup = 60;
    public int mTopBroadcastNumber = 3;

    private class FileObserverPolicy extends FileObserver {
        private String focusPath;

        public FileObserverPolicy(String path) {
            super(path, 8);
            this.focusPath = path;
        }

        public void onEvent(int event, String path) {
            if (event == 8 && this.focusPath.equals(OppoGuardElfConfigUtil.OPPO_GUARD_ELF_CONFIG_PATH)) {
                Log.i(OppoGuardElfConfigUtil.TAG, "onEvent: focusPath = OPPO_GUARD_ELF_CONFIG_PATH");
                OppoGuardElfConfigUtil.this.readConfigFile();
            }
        }
    }

    public OppoGuardElfConfigUtil() {
        initDir();
        initFileObserver();
        readConfigFile();
    }

    public static final OppoGuardElfConfigUtil getInstance() {
        if (mOppoGuardElfConfigUtil == null) {
            mOppoGuardElfConfigUtil = new OppoGuardElfConfigUtil();
        }
        return mOppoGuardElfConfigUtil;
    }

    private void initDir() {
        Log.i(TAG, "initDir start");
        File oppoGuardElfFilePath = new File(OPPO_GUARD_ELF_PATH);
        File oppoGuardElfConfigPath = new File(OPPO_GUARD_ELF_CONFIG_PATH);
        try {
            if (!oppoGuardElfFilePath.exists()) {
                oppoGuardElfFilePath.mkdirs();
            }
            if (!oppoGuardElfConfigPath.exists()) {
                oppoGuardElfConfigPath.createNewFile();
            }
        } catch (IOException e) {
            Log.e(TAG, "init OppoGuardElfConfigUtil Dir failed!!!");
        }
    }

    private void initFileObserver() {
        this.mGuardElfConfigFileObserver = new FileObserverPolicy(OPPO_GUARD_ELF_CONFIG_PATH);
        this.mGuardElfConfigFileObserver.startWatching();
    }

    public int getAbnormalStartCount() {
        return this.mAbnormalStartCount;
    }

    public void setAbnormalStartCount(int abnormalStartCount) {
        this.mAbnormalStartCount = abnormalStartCount;
    }

    public int getCollectStartCount() {
        return this.mCollectStartCount;
    }

    public void setCollectStartCount(int collectStartCount) {
        this.mCollectStartCount = collectStartCount;
    }

    public long getCheckStartTimeInterval() {
        return this.mCheckStartTimeInterval;
    }

    public void setCheckStartTimeInterval(long checkStartTimeInterval) {
        this.mCheckStartTimeInterval = checkStartTimeInterval;
    }

    public long getThresholdIntervalPerWakeup() {
        return this.mThresholdIntervalPerWakeup;
    }

    public void setThresholdIntervalPerWakeup(long thresholdIntervalPerWakeup) {
        this.mThresholdIntervalPerWakeup = thresholdIntervalPerWakeup;
    }

    public long getThresholdWarningIntervalPerWakeup() {
        return this.mThresholdWarningIntervalPerWakeup;
    }

    public void setThresholdWarningIntervalPerWakeup(long thresholdWarningIntervalPerWakeup) {
        this.mThresholdWarningIntervalPerWakeup = thresholdWarningIntervalPerWakeup;
    }

    public long getThresholdWorstIntervalPerWakeup() {
        return this.mThresholdWorstIntervalPerWakeup;
    }

    public long getThresholdSeriousIntervalPerAlarm() {
        return this.mThresholdSeriousIntervalPerAlarm;
    }

    public long getThresholdWakeLockTimeout() {
        return this.mThresholdWakeLockTimeout;
    }

    public long getBroadcastFinishTime() {
        return this.mBroadcastFinishTime;
    }

    public long getServiceFinishTime() {
        return this.mServiceFinishTime;
    }

    public long getCheckBroadcastServiceTime() {
        return this.mCheckBroadcastServiceTime;
    }

    public long getCheckTopBroadcastTime() {
        return this.mCheckTopBroadcastTime;
    }

    public int getTopBroadcastNumber() {
        return this.mTopBroadcastNumber;
    }

    public boolean getCloseFlag() {
        return this.mCloseFlag;
    }

    public long updateBroadcastAndServiceTime(String timeCountStr, long defaultCount) {
        try {
            return Long.parseLong(timeCountStr);
        } catch (NumberFormatException e) {
            Log.e(TAG, "updateBroadcastAndServiceTime NumberFormatException: ", e);
            return defaultCount;
        }
    }

    public boolean updateCloseFlag(String flagStr) {
        boolean closeFlag = true;
        try {
            return Boolean.parseBoolean(flagStr);
        } catch (NumberFormatException e) {
            Log.e(TAG, "updateCloseFlag NumberFormatException: ", e);
            return closeFlag;
        }
    }

    public long getBroadcastSendCount() {
        return this.mBroadcastSendCount;
    }

    /* JADX WARNING: Removed duplicated region for block: B:25:0x0065 A:{SYNTHETIC, Splitter: B:25:0x0065} */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x008e A:{SYNTHETIC, Splitter: B:37:0x008e} */
    /* JADX WARNING: Removed duplicated region for block: B:48:0x00ba A:{SYNTHETIC, Splitter: B:48:0x00ba} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void readConfigFile() {
        IOException e;
        XmlPullParserException e2;
        Throwable th;
        FileReader fileReader = null;
        try {
            FileReader xmlReader;
            XmlPullParser parser = Xml.newPullParser();
            File xmlFile = new File(OPPO_GUARD_ELF_CONFIG_PATH);
            try {
                xmlReader = new FileReader(xmlFile);
            } catch (FileNotFoundException e3) {
                Log.w(TAG, "Couldn't find or open alarm_filter_packages file " + xmlFile);
                return;
            }
            try {
                parser.setInput(xmlReader);
                for (int eventType = parser.getEventType(); eventType != 1; eventType = parser.next()) {
                    switch (eventType) {
                        case 2:
                            if (!parser.getName().equals(ABNORMAL_START_COUNT)) {
                                if (!parser.getName().equals(COLLECT_START_COUNT)) {
                                    if (!parser.getName().equals(CHECK_START_TIME_INTERVAL)) {
                                        if (!parser.getName().equals(THRESHOLD_INTERVAL_PER_WAKEUP)) {
                                            if (!parser.getName().equals(THRESHOLD_WARNING_INTERVAL_PER_WAKEUP)) {
                                                if (!parser.getName().equals(THRESHOLD_WORST_INTERVAL_PER_WAKEUP)) {
                                                    if (!parser.getName().equals(THRESHOLD_SERIOUS_INTERVAL_PER_ALARM)) {
                                                        if (!parser.getName().equals(BROADCAST_FINISH_TIME)) {
                                                            if (!parser.getName().equals(SERVICE_FINISH_TIME)) {
                                                                if (!parser.getName().equals(CHECK_BRORDCAST_SERVICE_TIME)) {
                                                                    if (!parser.getName().equals(CHECK_TOP_BROADCAST_TIME)) {
                                                                        if (!parser.getName().equals(TOP_BROADCAST_NUMBER)) {
                                                                            if (!parser.getName().equals(CLOSE_FLAG)) {
                                                                                if (!parser.getName().equals(BROADCAST_SEND_COUNT)) {
                                                                                    if (!parser.getName().equals(THRESHOLD_WAKELOCK_TIMEOUT)) {
                                                                                        if (!parser.getName().equals(TAG_THRESHOLD_BATT_IDLE_LOW_LEVEL)) {
                                                                                            if (!parser.getName().equals(TAG_THRESHOLD_BATT_IDLE_NORMAL_LEVEL)) {
                                                                                                if (!parser.getName().equals(TAG_THRESHOLD_BATT_IDLE_DELAY)) {
                                                                                                    if (!parser.getName().equals(TAG_THRESHOLD_JOB_MIN_INTERVAL)) {
                                                                                                        if (!TAG_TOTAL_CPU_MONITOR_SWITCH.equals(parser.getName())) {
                                                                                                            if (!TAG_APP_CPU_KILL_SWITCH.equals(parser.getName())) {
                                                                                                                if (!TAG_THRESH_TOTAL_CPU_SLIGHT.equals(parser.getName())) {
                                                                                                                    if (!TAG_THRESH_TOTAL_CPU_MIDDLE.equals(parser.getName())) {
                                                                                                                        if (!TAG_THRESH_TOTAL_CPU_HEAVY.equals(parser.getName())) {
                                                                                                                            if (!TAG_THRESH_COUNT_CONTINUOUS_SLIGHT.equals(parser.getName())) {
                                                                                                                                if (!TAG_THRESH_COUNT_CONTINUOUS_MIDDLE.equals(parser.getName())) {
                                                                                                                                    if (!TAG_THRESH_COUNT_CONTINUOUS_HEAVY.equals(parser.getName())) {
                                                                                                                                        if (!TAG_INTERVAL_TOTAL_CPU_SAMPLE.equals(parser.getName())) {
                                                                                                                                            if (!TAG_INTERVAL_APP_CPU_KILL.equals(parser.getName())) {
                                                                                                                                                if (TAG_TIME_FORE_APP_STABLE.equals(parser.getName())) {
                                                                                                                                                    eventType = parser.next();
                                                                                                                                                    updateTimeForeAppStable(parser.getText());
                                                                                                                                                    break;
                                                                                                                                                }
                                                                                                                                            }
                                                                                                                                            eventType = parser.next();
                                                                                                                                            updateIntervalAppCpuKill(parser.getText());
                                                                                                                                            break;
                                                                                                                                        }
                                                                                                                                        eventType = parser.next();
                                                                                                                                        updateIntervalTotalCpuSample(parser.getText());
                                                                                                                                        break;
                                                                                                                                    }
                                                                                                                                    eventType = parser.next();
                                                                                                                                    updateThreshCountContinuousHeavy(parser.getText());
                                                                                                                                    break;
                                                                                                                                }
                                                                                                                                eventType = parser.next();
                                                                                                                                updateThreshCountContinuousMiddle(parser.getText());
                                                                                                                                break;
                                                                                                                            }
                                                                                                                            eventType = parser.next();
                                                                                                                            updateThreshCountContinuousSlight(parser.getText());
                                                                                                                            break;
                                                                                                                        }
                                                                                                                        eventType = parser.next();
                                                                                                                        updateThreshTotalCpuHeavy(parser.getText());
                                                                                                                        break;
                                                                                                                    }
                                                                                                                    eventType = parser.next();
                                                                                                                    updateThreshTotalCpuMiddle(parser.getText());
                                                                                                                    break;
                                                                                                                }
                                                                                                                eventType = parser.next();
                                                                                                                updateThreshTotalCpuSlight(parser.getText());
                                                                                                                break;
                                                                                                            }
                                                                                                            eventType = parser.next();
                                                                                                            updateAppCpuKillSwitch(parser.getText());
                                                                                                            break;
                                                                                                        }
                                                                                                        eventType = parser.next();
                                                                                                        updateTotalCpuMonitorSwitch(parser.getText());
                                                                                                        break;
                                                                                                    }
                                                                                                    eventType = parser.next();
                                                                                                    updateThreshJobMinInterval(parser.getText());
                                                                                                    break;
                                                                                                }
                                                                                                eventType = parser.next();
                                                                                                updateThreshBattIdleDelay(parser.getText());
                                                                                                break;
                                                                                            }
                                                                                            eventType = parser.next();
                                                                                            updateThreshBattIdleNormalLevel(parser.getText());
                                                                                            break;
                                                                                        }
                                                                                        eventType = parser.next();
                                                                                        updateThreshBattIdleLowLevel(parser.getText());
                                                                                        break;
                                                                                    }
                                                                                    eventType = parser.next();
                                                                                    updateThresholdWakeLockTimeout(parser.getText());
                                                                                    break;
                                                                                }
                                                                                eventType = parser.next();
                                                                                this.mBroadcastSendCount = updateBroadcastAndServiceTime(parser.getText(), 60);
                                                                                break;
                                                                            }
                                                                            eventType = parser.next();
                                                                            this.mCloseFlag = updateCloseFlag(parser.getText());
                                                                            break;
                                                                        }
                                                                        eventType = parser.next();
                                                                        this.mTopBroadcastNumber = (int) updateBroadcastAndServiceTime(parser.getText(), 3);
                                                                        break;
                                                                    }
                                                                    eventType = parser.next();
                                                                    this.mCheckTopBroadcastTime = updateBroadcastAndServiceTime(parser.getText(), 600000);
                                                                    break;
                                                                }
                                                                eventType = parser.next();
                                                                this.mCheckBroadcastServiceTime = updateBroadcastAndServiceTime(parser.getText(), 1800000);
                                                                break;
                                                            }
                                                            eventType = parser.next();
                                                            this.mServiceFinishTime = updateBroadcastAndServiceTime(parser.getText(), 10000);
                                                            break;
                                                        }
                                                        eventType = parser.next();
                                                        this.mBroadcastFinishTime = updateBroadcastAndServiceTime(parser.getText(), 6000);
                                                        break;
                                                    }
                                                    eventType = parser.next();
                                                    updateThresholdSeriousIntervalPerAlarm(parser.getText());
                                                    break;
                                                }
                                                eventType = parser.next();
                                                updateThresholdWorstIntervalPerWakeup(parser.getText());
                                                break;
                                            }
                                            eventType = parser.next();
                                            updateThresholdWarningIntervalPerWakeup(parser.getText());
                                            break;
                                        }
                                        eventType = parser.next();
                                        updateThresholdIntervalPerWakeup(parser.getText());
                                        break;
                                    }
                                    eventType = parser.next();
                                    updateCheckStartTimeInterval(parser.getText());
                                    break;
                                }
                                eventType = parser.next();
                                updateCollectStartCount(parser.getText());
                                break;
                            }
                            eventType = parser.next();
                            updateAbnormalStartCount(parser.getText());
                            break;
                            break;
                    }
                }
                if (xmlReader != null) {
                    try {
                        xmlReader.close();
                    } catch (IOException e4) {
                        Log.w(TAG, "Got execption close permReader.", e4);
                    }
                }
            } catch (XmlPullParserException e5) {
                e2 = e5;
                fileReader = xmlReader;
                try {
                    Log.w(TAG, "Got execption parsing permissions.", e2);
                    if (fileReader != null) {
                    }
                } catch (Throwable th2) {
                    th = th2;
                    if (fileReader != null) {
                        try {
                            fileReader.close();
                        } catch (IOException e42) {
                            Log.w(TAG, "Got execption close permReader.", e42);
                        }
                    }
                    throw th;
                }
            } catch (IOException e6) {
                e42 = e6;
                fileReader = xmlReader;
                Log.w(TAG, "Got execption parsing permissions.", e42);
                if (fileReader != null) {
                }
            } catch (Throwable th3) {
                th = th3;
                fileReader = xmlReader;
                if (fileReader != null) {
                }
                throw th;
            }
        } catch (XmlPullParserException e7) {
            e2 = e7;
            Log.w(TAG, "Got execption parsing permissions.", e2);
            if (fileReader != null) {
                try {
                    fileReader.close();
                } catch (IOException e422) {
                    Log.w(TAG, "Got execption close permReader.", e422);
                }
            }
        } catch (IOException e8) {
            e422 = e8;
            Log.w(TAG, "Got execption parsing permissions.", e422);
            if (fileReader != null) {
                try {
                    fileReader.close();
                } catch (IOException e4222) {
                    Log.w(TAG, "Got execption close permReader.", e4222);
                }
            }
        }
    }

    public void updateAbnormalStartCount(String abnormalStartCount) {
        Log.w(TAG, "updateAbnormalStartCount abnormalStartCount == " + abnormalStartCount);
        try {
            this.mAbnormalStartCount = Integer.parseInt(abnormalStartCount);
        } catch (NumberFormatException e) {
            this.mAbnormalStartCount = 50;
            Log.e(TAG, "updateAbnormalStartCount NumberFormatException: ", e);
        }
    }

    public void updateCollectStartCount(String collectStartCount) {
        Log.w(TAG, "updateCollectStartCount collectStartCount == " + collectStartCount);
        try {
            this.mCollectStartCount = Integer.parseInt(collectStartCount);
        } catch (NumberFormatException e) {
            this.mCollectStartCount = 30;
            Log.e(TAG, "updateCollectStartCount NumberFormatException: ", e);
        }
    }

    public void updateCheckStartTimeInterval(String checkStartTimeInterval) {
        Log.w(TAG, "updateCheckStartTimeInterval checkStartTimeInterval == " + checkStartTimeInterval);
        try {
            this.mCheckStartTimeInterval = Long.parseLong(checkStartTimeInterval);
        } catch (NumberFormatException e) {
            this.mCheckStartTimeInterval = 300000;
            Log.e(TAG, "updateCheckStartTimeInterval NumberFormatException: ", e);
        }
    }

    public void updateThresholdIntervalPerWakeup(String thresholdIntervalPerWakeup) {
        Log.w(TAG, "updateThresholdIntervalPerWakeup thresholdIntervalPerWakeup == " + thresholdIntervalPerWakeup);
        try {
            this.mThresholdIntervalPerWakeup = Long.parseLong(thresholdIntervalPerWakeup);
        } catch (NumberFormatException e) {
            this.mThresholdIntervalPerWakeup = 300;
            Log.e(TAG, "updateThresholdIntervalPerWakeup NumberFormatException: ", e);
        }
    }

    public void updateThresholdWarningIntervalPerWakeup(String thresholdWarningIntervalPerWakeup) {
        Log.w(TAG, "updateThresholdWarningIntervalPerWakeup thresholdWarningIntervalPerWakeup == " + thresholdWarningIntervalPerWakeup);
        try {
            this.mThresholdWarningIntervalPerWakeup = Long.parseLong(thresholdWarningIntervalPerWakeup);
        } catch (NumberFormatException e) {
            this.mThresholdWarningIntervalPerWakeup = THRESHOLD_WARNING_INTERVAL_PER_WAKEUP_DEFA_VALUE;
            Log.e(TAG, "updateThresholdWarningIntervalPerWakeup NumberFormatException: ", e);
        }
    }

    public void updateThresholdWorstIntervalPerWakeup(String thresholdWorstIntervalPerWakeup) {
        Log.w(TAG, "updateThresholdWorstIntervalPerWakeup thresholdWorstIntervalPerWakeup == " + thresholdWorstIntervalPerWakeup);
        try {
            this.mThresholdWorstIntervalPerWakeup = Long.parseLong(thresholdWorstIntervalPerWakeup);
        } catch (NumberFormatException e) {
            this.mThresholdWorstIntervalPerWakeup = 60;
            Log.e(TAG, "updateThresholdWorstIntervalPerWakeup NumberFormatException: ", e);
        }
    }

    public void updateThresholdSeriousIntervalPerAlarm(String thresholdSeriousIntervalPerAlarm) {
        Log.w(TAG, "updateThresholdSeriousIntervalPerAlarm thresholdSeriousIntervalPerAlarm == " + thresholdSeriousIntervalPerAlarm);
        try {
            this.mThresholdSeriousIntervalPerAlarm = Long.parseLong(thresholdSeriousIntervalPerAlarm);
        } catch (NumberFormatException e) {
            this.mThresholdSeriousIntervalPerAlarm = THRESHOLD_SERIOUS_INTERVAL_PER_ALARM_DEFA_VALUE;
            Log.e(TAG, "updateThresholdSeriousIntervalPerAlarm NumberFormatException: ", e);
        }
    }

    public void updateThresholdWakeLockTimeout(String thresholdWakeLockTimeout) {
        Log.w(TAG, "updateThresholdWakeLockTimeout thresholdWakeLockTimeout == " + thresholdWakeLockTimeout);
        try {
            this.mThresholdWakeLockTimeout = Long.parseLong(thresholdWakeLockTimeout);
        } catch (NumberFormatException e) {
            this.mThresholdWakeLockTimeout = 300;
            Log.e(TAG, "updateThresholdWakeLockTimeout NumberFormatException: ", e);
        }
    }

    private void updateThreshBattIdleLowLevel(String value) {
        if (value != null) {
            try {
                this.mThreshBattIdleLowLevel = Integer.parseInt(value);
            } catch (NumberFormatException e) {
                this.mThreshBattIdleLowLevel = 30;
                Log.e(TAG, "failed parsing value " + e);
            }
        }
    }

    public int getThreshBattIdleLowLevel() {
        return this.mThreshBattIdleLowLevel;
    }

    private void updateThreshBattIdleNormalLevel(String value) {
        if (value != null) {
            try {
                this.mThreshBattIdleNormalLevel = Integer.parseInt(value);
            } catch (NumberFormatException e) {
                this.mThreshBattIdleNormalLevel = 70;
                Log.e(TAG, "failed parsing value " + e);
            }
        }
    }

    public int getThreshBattIdleNormalLevel() {
        return this.mThreshBattIdleNormalLevel;
    }

    private void updateThreshBattIdleDelay(String value) {
        if (value != null) {
            try {
                this.mThreshBattIdleDelay = Integer.parseInt(value);
            } catch (NumberFormatException e) {
                this.mThreshBattIdleDelay = 300;
                Log.e(TAG, "failed parsing value " + e);
            }
        }
    }

    public int getThreshBattIdleDelay() {
        return this.mThreshBattIdleDelay;
    }

    private void updateThreshJobMinInterval(String value) {
        if (value != null) {
            try {
                this.mThreshJobMinInterval = Long.parseLong(value);
            } catch (NumberFormatException e) {
                this.mThreshJobMinInterval = 3600;
                Log.e(TAG, "failed parsing value " + e);
            }
        }
    }

    public long getThreshJobMinInterval() {
        return this.mThreshJobMinInterval;
    }

    private void updateTotalCpuMonitorSwitch(String value) {
        if (value != null) {
            try {
                mTotalCpuMonitorSwitch = Boolean.parseBoolean(value);
            } catch (NumberFormatException e) {
                mTotalCpuMonitorSwitch = true;
                Log.i(TAG, "failed parsing value " + e);
            }
        }
    }

    private void updateAppCpuKillSwitch(String value) {
        if (value != null) {
            try {
                mAppCpuKillSwitch = Boolean.parseBoolean(value);
            } catch (NumberFormatException e) {
                mAppCpuKillSwitch = true;
                Log.i(TAG, "failed parsing value " + e);
            }
        }
    }

    private void updateThreshTotalCpuSlight(String value) {
        if (value != null) {
            try {
                mThreshTotalCpuSlight = Integer.parseInt(value);
            } catch (NumberFormatException e) {
                mThreshTotalCpuSlight = 20;
                Log.i(TAG, "failed parsing value " + e);
            }
        }
    }

    private void updateThreshTotalCpuMiddle(String value) {
        if (value != null) {
            try {
                mThreshTotalCpuMiddle = Integer.parseInt(value);
            } catch (NumberFormatException e) {
                mThreshTotalCpuMiddle = 40;
                Log.i(TAG, "failed parsing value " + e);
            }
        }
    }

    private void updateThreshTotalCpuHeavy(String value) {
        if (value != null) {
            try {
                mThreshTotalCpuHeavy = Integer.parseInt(value);
            } catch (NumberFormatException e) {
                mThreshTotalCpuHeavy = 60;
                Log.i(TAG, "failed parsing value " + e);
            }
        }
    }

    private void updateThreshCountContinuousSlight(String value) {
        if (value != null) {
            try {
                mThreshCountContinuousSlight = Integer.parseInt(value);
            } catch (NumberFormatException e) {
                mThreshCountContinuousSlight = 4;
                Log.i(TAG, "failed parsing value " + e);
            }
        }
    }

    private void updateThreshCountContinuousMiddle(String value) {
        if (value != null) {
            try {
                mThreshCountContinuousMiddle = Integer.parseInt(value);
            } catch (NumberFormatException e) {
                mThreshCountContinuousMiddle = 3;
                Log.i(TAG, "failed parsing value " + e);
            }
        }
    }

    private void updateThreshCountContinuousHeavy(String value) {
        if (value != null) {
            try {
                mThreshCountContinuousHeavy = Integer.parseInt(value);
            } catch (NumberFormatException e) {
                mThreshCountContinuousHeavy = 2;
                Log.i(TAG, "failed parsing value " + e);
            }
        }
    }

    private void updateIntervalTotalCpuSample(String value) {
        if (value != null) {
            try {
                mIntervalTotalCpuSample = Long.parseLong(value);
            } catch (NumberFormatException e) {
                mIntervalTotalCpuSample = 5000;
                Log.i(TAG, "failed parsing value " + e);
            }
        }
    }

    private void updateIntervalAppCpuKill(String value) {
        if (value != null) {
            try {
                mIntervalAppCpuKill = Long.parseLong(value);
            } catch (NumberFormatException e) {
                mIntervalAppCpuKill = 60000;
                Log.i(TAG, "failed parsing value " + e);
            }
        }
    }

    private void updateTimeForeAppStable(String value) {
        if (value != null) {
            try {
                mTimeForeAppStable = Long.parseLong(value);
            } catch (NumberFormatException e) {
                mTimeForeAppStable = 10000;
                Log.i(TAG, "failed parsing value " + e);
            }
        }
    }

    public boolean getTotalCpuMonitorSwitch() {
        return mTotalCpuMonitorSwitch;
    }

    public boolean getAppCpuKillSwitch() {
        return mAppCpuKillSwitch;
    }

    public int getThreshTotalCpuSlight() {
        return mThreshTotalCpuSlight;
    }

    public int getThreshTotalCpuMiddle() {
        return mThreshTotalCpuMiddle;
    }

    public int getThreshTotalCpuHeavy() {
        return mThreshTotalCpuHeavy;
    }

    public int getThreshCountContinuousSlight() {
        return mThreshCountContinuousSlight;
    }

    public int getThreshCountContinuousMiddle() {
        return mThreshCountContinuousMiddle;
    }

    public int getThreshCountContinuousHeavy() {
        return mThreshCountContinuousHeavy;
    }

    public long getIntervalTotalCpuSample() {
        return mIntervalTotalCpuSample;
    }

    public long getIntervalAppCpuKill() {
        return mIntervalAppCpuKill;
    }

    public long getTimeForeAppStable() {
        return mTimeForeAppStable;
    }
}
