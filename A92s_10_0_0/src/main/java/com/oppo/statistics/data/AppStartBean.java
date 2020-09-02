package com.oppo.statistics.data;

import com.oppo.statistics.util.AccountUtil;

public class AppStartBean implements StatisticBean {
    private String mTime = AccountUtil.SSOID_DEFAULT;

    public AppStartBean(String time) {
        this.mTime = time;
    }

    @Override // com.oppo.statistics.data.StatisticBean
    public int getDataType() {
        return 1;
    }

    public String getTime() {
        return this.mTime;
    }

    public void setTime(String mTime2) {
        this.mTime = mTime2;
    }

    public String toString() {
        return "loginTime is :" + getTime() + "\n";
    }
}
