package com.oppo.media;

public class OppoDaemonListInfo {
    private String mAttribute;
    private String mListInfo;
    private String mModule;
    private String mName;

    public void setModule(String value) {
        this.mModule = value;
    }

    public void setName(String value) {
        this.mName = value;
    }

    public void setAttribute(String value) {
        this.mAttribute = value;
    }

    public String getModule() {
        return this.mModule;
    }

    public String getName() {
        return this.mName;
    }

    public String getAttribute() {
        return this.mAttribute;
    }

    public String getListInfo() {
        StringBuilder str = new StringBuilder();
        str.append(this.mModule);
        str.append(",");
        str.append(this.mName);
        str.append(",");
        str.append(this.mAttribute);
        this.mListInfo = str.toString();
        return this.mListInfo;
    }
}
