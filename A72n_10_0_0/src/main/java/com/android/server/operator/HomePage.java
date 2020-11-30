package com.android.server.operator;

public class HomePage extends Element {
    private String label;
    private String spn;
    private String url;

    public HomePage() {
        super("homepage");
    }

    public String toString() {
        return "HomePage{label='" + this.label + "', url='" + this.url + "', country='" + this.country + "', spn='" + this.spn + "'}";
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

    public String getSpn() {
        return this.spn;
    }

    public void setSpn(String spn2) {
        this.spn = spn2;
    }
}
