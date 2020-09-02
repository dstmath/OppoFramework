package com.mediatek.server.anr;

import android.util.EventLog;

public class EventLogTags {
    public static final int AM_ANR = 30008;

    private EventLogTags() {
    }

    public static void writeAmAnr(int user, int pid, String packageName, int flags, String reason) {
        EventLog.writeEvent((int) AM_ANR, Integer.valueOf(user), Integer.valueOf(pid), packageName, Integer.valueOf(flags), reason);
    }
}
