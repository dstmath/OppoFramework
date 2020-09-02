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

    public void setSsoid(String mSsoid2) {
        this.mSsoid = mSsoid2;
    }

    public String getTime() {
        return this.mTime;
    }

    public void setTime(String mTime2) {
        this.mTime = mTime2;
    }

    public int getAppId() {
        return this.mAppId;
    }

    public void setAppId(int mAppId2) {
        this.mAppId = mAppId2;
    }

    @Override // com.oppo.statistics.data.StatisticBean
    public int getDataType() {
        return 7;
    }
}
