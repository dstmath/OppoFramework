package com.android.server.operator;

public class BookMark extends Element {
    private String folder;
    private String label;
    private String operator;
    private String spn;
    private String url;

    public BookMark() {
        super("bookmark");
    }

    public String toString() {
        return "BookMark{label='" + this.label + "', url='" + this.url + "', operator='" + this.operator + "', folder='" + this.folder + "', country='" + this.country + "', spn='" + this.spn + "'}";
    }

    public String getLabel() {
        return this.label;
    }

    public void setLabel(String label2) {
        this.label = label2;
    }

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url2) {
        this.url = url2;
    }

    public String getOperator() {
        return this.operator;
    }

    public void setOperator(String operator2) {
        this.operator = operator2;
    }

    public String getFolder() {
        return this.folder;
    }

    public void setFolder(String folder2) {
        this.folder = folder2;
    }

    public String getSpn() {
        return this.spn;
    }

    public void setSpn(String spn2) {
        this.spn = spn2;
    }
}
