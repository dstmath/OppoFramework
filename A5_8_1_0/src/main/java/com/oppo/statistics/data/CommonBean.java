package com.oppo.statistics.data;

public class CommonBean implements StatisticBean {
    private String eventID = "";
    private String logMap = "";
    private String logTag = "";
    private boolean uploadNow = false;

    public CommonBean(String logTag, String eventID, String logMap) {
        this.logTag = logTag;
        this.eventID = eventID;
        this.logMap = logMap;
    }

    public CommonBean(String logTag, String eventID, String logMap, boolean uploadNow) {
        this.logTag = logTag;
        this.logMap = logMap;
        this.eventID = eventID;
        this.uploadNow = uploadNow;
    }

    public String getEventID() {
        return this.eventID;
    }

    public void setEventID(String eventID) {
        this.eventID = eventID;
    }

    public String getLogTag() {
        return this.logTag;
    }

    public void setLogTag(String logTag) {
        this.logTag = logTag;
    }

    public String getLogMap() {
        return this.logMap;
    }

    public void setLogMap(String logMap) {
        this.logMap = logMap;
    }

    public boolean getUploadNow() {
        return this.uploadNow;
    }

    public void setUploadNow(boolean uploadNow) {
        this.uploadNow = uploadNow;
    }

    public int getDataType() {
        return 9;
    }

    public String toString() {
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append(" type is :");
        strBuilder.append(getDataType());
        strBuilder.append(",");
        strBuilder.append(" uploadNow is :");
        strBuilder.append(getUploadNow());
        strBuilder.append(",");
        strBuilder.append(" tag is :");
        strBuilder.append(getLogTag());
        strBuilder.append(",");
        strBuilder.append(" eventID is :");
        strBuilder.append(getEventID());
        strBuilder.append(",");
        strBuilder.append(" map is :");
        strBuilder.append(getLogMap());
        return strBuilder.toString();
    }
}
