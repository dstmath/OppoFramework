package com.oppo.statistics.data;

import com.oppo.statistics.util.AccountUtil;

public class SpecialAppStartBean implements StatisticBean {
    private int mAppId = 0;
    private String mSsoid = AccountUtil.SSOID_DEFAULT;
    private String mTime = AccountUtil.SSOID_DEFAULT;

    public SpecialAppStartBean(String ssoid, String time, int appId) {
        this.mSsoid = ssoid;
        this.mTime = time;
        this.mAppId = appId;
    }

    public String getSsoid() {
        return this.mSsoid;
    }

    public void setSsoid(String mSsoid) {
        this.mSsoid = mSsoid;
    }

    public String getTime() {
        return this.mTime;
    }

    public void setTime(String mTime) {
        this.mTime = mTime;
    }

    public int getAppId() {
        return this.mAppId;
    }

    public void setAppId(int mAppId) {
        this.mAppId = mAppId;
    }

    public int getDataType() {
        return 7;
    }
}
