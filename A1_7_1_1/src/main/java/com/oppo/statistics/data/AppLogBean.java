package com.oppo.statistics.data;

public class AppLogBean implements StatisticBean {
    private String mBody = "";
    private String mType = "";

    public AppLogBean(String type, String body) {
        this.mType = type;
        this.mBody = body;
    }

    public String getType() {
        return this.mType;
    }

    public void setType(String type) {
        this.mType = type;
    }

    public String getBody() {
        return this.mBody;
    }

    public void setAppLog(String appLog) {
        this.mBody = appLog;
    }

    public int getDataType() {
        return 4;
    }

    public String toString() {
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append("type is :");
        strBuilder.append(getDataType());
        strBuilder.append("\n");
        strBuilder.append("body is :");
        strBuilder.append(getBody());
        strBuilder.append("\n");
        return strBuilder.toString();
    }
}
