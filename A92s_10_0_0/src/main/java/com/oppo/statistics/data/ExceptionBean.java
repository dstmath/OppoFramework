package com.oppo.statistics.data;

public class ExceptionBean implements StatisticBean {
    private int mCount;
    private long mEventTime;
    private String mException;

    public long getEventTime() {
        return this.mEventTime;
    }

    public void setEventTime(long mEventTime2) {
        this.mEventTime = mEventTime2;
    }

    public String getException() {
        return this.mException;
    }

    public void setException(String mException2) {
        this.mException = mException2;
    }

    public int getCount() {
        return this.mCount;
    }

    public void setCount(int mCount2) {
        this.mCount = mCount2;
    }

    @Override // com.oppo.statistics.data.StatisticBean
    public int getDataType() {
        return 5;
    }

    public String toString() {
        return "exception is :" + getException() + "\n" + "count is :" + getCount() + "\n" + "time is :" + getEventTime() + "\n";
    }
}
