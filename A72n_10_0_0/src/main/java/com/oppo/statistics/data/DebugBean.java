package com.oppo.statistics.data;

public class DebugBean implements StatisticBean {
    private boolean mFlag = false;

    public DebugBean(boolean flag) {
        this.mFlag = flag;
    }

    public boolean getFlag() {
        return this.mFlag;
    }

    public void setFlag(boolean flag) {
        this.mFlag = flag;
    }

    @Override // com.oppo.statistics.data.StatisticBean
    public int getDataType() {
        return 12;
    }

    public String toString() {
        return "type is :" + getDataType() + "\nflag is :" + getFlag() + "\n";
    }
}
