package com.oppo.statistics.data;

public class StaticEventBean implements StatisticBean {
    private String mBody = "";
    private int mUploadMode = 0;

    public StaticEventBean(int uploadMode, String body) {
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

    public void setBody(String keyguardLog) {
        this.mBody = keyguardLog;
    }

    @Override // com.oppo.statistics.data.StatisticBean
    public int getDataType() {
        return 11;
    }

    public String toString() {
        return "uploadMode is :" + this.mUploadMode + "\nbody is :" + getBody() + "\n";
    }
}
