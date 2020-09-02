package com.android.server.power;

import android.os.SystemClock;
import android.util.ArrayMap;
import com.android.server.power.PowerManagerService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class OppoPowerMonitor {
    private static final String CPUBLOCKER = "PowerManagerService.WakeLocks";
    private static final int MAX_APP_WAKELOCK_SIZE_LIMIT = 1000;
    private Map<String, Long> mAppWakeupMap = new ArrayMap();
    private long mFrameworksBlockedTime = 0;
    private long mLastBlockedTime = -1;
    private long mLastScreenOffTime = 0;
    private int mWakefulness;

    private boolean isInteractive() {
        int i = this.mWakefulness;
        return i == 1 || i == 2;
    }

    public void screenOff() {
        this.mLastScreenOffTime = SystemClock.uptimeMillis();
        this.mLastBlockedTime = SystemClock.uptimeMillis();
        this.mFrameworksBlockedTime = 0;
    }

    public void screenOn() {
        if (this.mLastBlockedTime != -1) {
            this.mFrameworksBlockedTime += SystemClock.uptimeMillis() - this.mLastBlockedTime;
            this.mLastBlockedTime = -1;
        }
    }

    public void acquireSuspendBlocker(String name) {
        if (name.equals(CPUBLOCKER) && !isInteractive() && this.mLastBlockedTime == -1) {
            this.mLastBlockedTime = SystemClock.uptimeMillis();
        }
    }

    public void releaseSuspendBlocker(String name) {
        if (name.equals(CPUBLOCKER) && !isInteractive()) {
            long releaseTime = SystemClock.uptimeMillis();
            long j = this.mLastScreenOffTime;
            long j2 = this.mLastBlockedTime;
            if (j > j2) {
                this.mFrameworksBlockedTime += releaseTime - j;
            } else {
                this.mFrameworksBlockedTime += releaseTime - j2;
            }
            this.mLastBlockedTime = -1;
        }
    }

    public void acquireWakeLock(String packageName, String tag, int level) {
    }

    public void releaseWakeLock(String packageName, String tag, long totalTime) {
        String key = packageName + ":" + tag;
        long screenOffTime = SystemClock.uptimeMillis() - this.mLastScreenOffTime;
        long wakeLockTime = screenOffTime <= totalTime ? screenOffTime : totalTime;
        synchronized (this.mAppWakeupMap) {
            if (this.mAppWakeupMap.containsKey(key)) {
                this.mAppWakeupMap.put(key, Long.valueOf(this.mAppWakeupMap.get(key).longValue() + wakeLockTime));
            } else if (this.mAppWakeupMap.size() < 1000) {
                this.mAppWakeupMap.put(key, Long.valueOf(wakeLockTime));
            }
        }
    }

    public long getFrameworksBlockedTime() {
        return this.mFrameworksBlockedTime;
    }

    public Map getTopAppBlocked(int n) {
        if (n < 1) {
            return null;
        }
        Map<String, Long> appWakeupResult = new ArrayMap<>();
        synchronized (this.mAppWakeupMap) {
            List<Map.Entry<String, Long>> appWakeupList = new ArrayList<>(new HashMap(this.mAppWakeupMap).entrySet());
            Collections.sort(appWakeupList, new Comparator<Map.Entry<String, Long>>() {
                /* class com.android.server.power.OppoPowerMonitor.AnonymousClass1 */

                public int compare(Map.Entry<String, Long> o1, Map.Entry<String, Long> o2) {
                    long v2 = o2.getValue().longValue();
                    long v1 = o1.getValue().longValue();
                    if (v2 > v1) {
                        return 1;
                    }
                    if (v1 > v2) {
                        return -1;
                    }
                    return 0;
                }
            });
            int limit = n < appWakeupList.size() ? n : appWakeupList.size();
            for (int i = 0; i < limit; i++) {
                Map.Entry<String, Long> m = appWakeupList.get(i);
                appWakeupResult.put(m.getKey().toString(), m.getValue());
            }
        }
        return appWakeupResult;
    }

    public void clear() {
        this.mLastScreenOffTime = 0;
        this.mFrameworksBlockedTime = 0;
        this.mLastBlockedTime = -1;
        synchronized (this.mAppWakeupMap) {
            this.mAppWakeupMap.clear();
        }
    }

    public void onWakeFullnessChanged(int wakefulness) {
        this.mWakefulness = wakefulness;
    }

    public boolean isCPULock(PowerManagerService.WakeLock wakeLock) {
        if ((wakeLock.mFlags & 1) == 0 && (wakeLock.mFlags & 128) == 0) {
            return false;
        }
        return true;
    }
}
