package com.oppo.statistics.data;

public class CommonBean implements StatisticBean {
    private String mAppId = "";
    private String mEventID = "";
    private String mLogMap = "";
    private String mLogTag = "";
    private boolean mUploadNow = false;

    public CommonBean(String logTag, String eventID, String logMap) {
        this.mLogTag = logTag;
        this.mEventID = eventID;
        this.mLogMap = logMap;
    }

    public CommonBean(String logTag, String eventID, String logMap, boolean uploadNow) {
        this.mLogTag = logTag;
        this.mLogMap = logMap;
        this.mEventID = eventID;
        this.mUploadNow = uploadNow;
    }

    public String getEventID() {
        return this.mEventID;
    }

    public void setEventID(String eventID) {
        this.mEventID = eventID;
    }

    public String getLogTag() {
        return this.mLogTag;
    }

    public void setLogTag(String logTag) {
        this.mLogTag = logTag;
    }

    public String getLogMap() {
        return this.mLogMap;
    }

    public void setLogMap(String logMap) {
        this.mLogMap = logMap;
    }

    public boolean getUploadNow() {
        return this.mUploadNow;
    }

    public void setUploadNow(boolean uploadNow) {
        this.mUploadNow = uploadNow;
    }

    @Override // com.oppo.statistics.data.StatisticBean
    public int getDataType() {
        return 9;
    }

    public String getAppId() {
        return this.mAppId;
    }

    public void setAppId(String appId) {
        this.mAppId = appId;
    }

    public String toString() {
        return " type is :" + getDataType() + "," + " uploadNow is :" + getUploadNow() + "," + " tag is :" + getLogTag() + "," + " eventID is :" + getEventID() + "," + " map is :" + getLogMap();
    }
}
