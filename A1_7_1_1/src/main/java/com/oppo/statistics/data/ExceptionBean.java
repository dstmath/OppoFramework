package com.oppo.statistics.data;

public class ExceptionBean implements StatisticBean {
    private int mCount;
    private long mEventTime;
    private String mException;

    public long getEventTime() {
        return this.mEventTime;
    }

    public void setEventTime(long mEventTime) {
        this.mEventTime = mEventTime;
    }

    public String getException() {
        return this.mException;
    }

    public void setException(String mException) {
        this.mException = mException;
    }

    public int getCount() {
        return this.mCount;
    }

    public void setCount(int mCount) {
        this.mCount = mCount;
    }

    public int getDataType() {
        return 5;
    }

    public String toString() {
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append("exception is :");
        strBuilder.append(getException());
        strBuilder.append("\n");
        strBuilder.append("count is :");
        strBuilder.append(getCount());
        strBuilder.append("\n");
        strBuilder.append("time is :");
        strBuilder.append(getEventTime());
        strBuilder.append("\n");
        return strBuilder.toString();
    }
}
