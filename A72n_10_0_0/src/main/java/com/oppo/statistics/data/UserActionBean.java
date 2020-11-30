package com.oppo.statistics.data;

import com.oppo.statistics.util.AccountUtil;

public class UserActionBean implements StatisticBean {
    private int mAmount = 0;
    private int mCode = 0;
    private String mDate = AccountUtil.SSOID_DEFAULT;

    public UserActionBean(int actionId, String actionDate, int actionAmount) {
        this.mCode = actionId;
        this.mDate = actionDate;
        this.mAmount = actionAmount;
    }

    public int getActionCode() {
        return this.mCode;
    }

    public void setActionCode(int actionId) {
        this.mCode = actionId;
    }

    public String getActionDate() {
        return this.mDate;
    }

    public void setActionDate(String actionDate) {
        this.mDate = actionDate;
    }

    public int getActionAmount() {
        return this.mAmount;
    }

    public void setActionAmount(int actionAmount) {
        this.mAmount = actionAmount;
    }

    @Override // com.oppo.statistics.data.StatisticBean
    public int getDataType() {
        return 2;
    }

    public String toString() {
        return "action code is: " + getActionCode() + "\naction amount is: " + getActionAmount() + "\naction date is: " + getActionDate() + "\n";
    }
}
