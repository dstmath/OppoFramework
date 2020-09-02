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

    @Override // com.oppo.statistics.data.StatisticBean
    public int getDataType() {
        return 4;
    }

    public String toString() {
        return "type is :" + getDataType() + "\n" + "body is :" + getBody() + "\n";
    }
}
