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

    public int getDataType() {
        return 12;
    }

    public String toString() {
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append("type is :");
        strBuilder.append(getDataType());
        strBuilder.append("\n");
        strBuilder.append("flag is :");
        strBuilder.append(getFlag());
        strBuilder.append("\n");
        return strBuilder.toString();
    }
}
