package com.oppo.statistics.data;

public class PageVisitBean implements StatisticBean {
    private String mActivities;
    private long mDuration;
    private String mTime;

    @Override // com.oppo.statistics.data.StatisticBean
    public int getDataType() {
        return 3;
    }

    public String getTime() {
        return this.mTime;
    }

    public void setTime(String mTime2) {
        this.mTime = mTime2;
    }

    public long getDuration() {
        return this.mDuration;
    }

    public void setDuration(long mDuration2) {
        this.mDuration = mDuration2;
    }

    public String getActivities() {
        return this.mActivities;
    }

    public void setActivities(String mActivities2) {
        this.mActivities = mActivities2;
    }

    public String toString() {
        return "time is :" + getTime() + "\nduration is :" + getDuration() + "\nactivities is :" + getActivities() + "\n";
    }
}
