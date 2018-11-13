package com.oppo.statistics.data;

import com.oppo.statistics.util.AccountUtil;

public class AppStartBean implements StatisticBean {
    private String mTime = AccountUtil.SSOID_DEFAULT;

    public AppStartBean(String time) {
        this.mTime = time;
    }

    public int getDataType() {
        return 1;
    }

    public String getTime() {
        return this.mTime;
    }

    public void setTime(String mTime) {
        this.mTime = mTime;
    }

    public String toString() {
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append("loginTime is :");
        strBuilder.append(getTime());
        strBuilder.append("\n");
        return strBuilder.toString();
    }
}
