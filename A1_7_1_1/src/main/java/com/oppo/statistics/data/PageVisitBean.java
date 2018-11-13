package com.oppo.statistics.data;

public class PageVisitBean implements StatisticBean {
    private String mActivities;
    private long mDuration;
    private String mTime;

    public int getDataType() {
        return 3;
    }

    public String getTime() {
        return this.mTime;
    }

    public void setTime(String mTime) {
        this.mTime = mTime;
    }

    public long getDuration() {
        return this.mDuration;
    }

    public void setDuration(long mDuration) {
        this.mDuration = mDuration;
    }

    public String getActivities() {
        return this.mActivities;
    }

    public void setActivities(String mActivities) {
        this.mActivities = mActivities;
    }

    public String toString() {
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append("time is :");
        strBuilder.append(getTime());
        strBuilder.append("\n");
        strBuilder.append("duration is :");
        strBuilder.append(getDuration());
        strBuilder.append("\n");
        strBuilder.append("activities is :");
        strBuilder.append(getActivities());
        strBuilder.append("\n");
        return strBuilder.toString();
    }
}
