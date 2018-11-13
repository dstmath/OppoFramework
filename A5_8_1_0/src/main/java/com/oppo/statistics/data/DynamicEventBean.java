package com.oppo.statistics.data;

public class DynamicEventBean implements StatisticBean {
    private String mBody = "";
    private int mUploadMode = 0;

    public DynamicEventBean(int uploadMode, String body) {
        this.mUploadMode = uploadMode;
        this.mBody = body;
    }

    public int getUploadMode() {
        return this.mUploadMode;
    }

    public void setUploadMode(int uploadMode) {
        this.mUploadMode = uploadMode;
    }

    public String getBody() {
        return this.mBody;
    }

    public void setBody(String logBody) {
        this.mBody = logBody;
    }

    public int getDataType() {
        return 10;
    }

    public String toString() {
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append("uploadMode is :");
        strBuilder.append(this.mUploadMode);
        strBuilder.append("\n");
        strBuilder.append("body is :");
        strBuilder.append(getBody());
        strBuilder.append("\n");
        return strBuilder.toString();
    }
}
