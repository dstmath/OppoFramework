package com.android.server.usage;

public class UnixCalendar {
    public static final long DAY_IN_MILLIS = 86400000;
    public static final long MONTH_IN_MILLIS = 2592000000L;
    public static final long WEEK_IN_MILLIS = 604800000;
    public static final long YEAR_IN_MILLIS = 31536000000L;
    private long mTime;

    public UnixCalendar(long time) {
        this.mTime = time;
    }

    public void addDays(int val) {
        this.mTime += ((long) val) * 86400000;
    }

    public void addWeeks(int val) {
        this.mTime += ((long) val) * WEEK_IN_MILLIS;
    }

    public void addMonths(int val) {
        this.mTime += ((long) val) * MONTH_IN_MILLIS;
    }

    public void addYears(int val) {
        this.mTime += ((long) val) * YEAR_IN_MILLIS;
    }

    public void setTimeInMillis(long time) {
        this.mTime = time;
    }

    public long getTimeInMillis() {
        return this.mTime;
    }
}
