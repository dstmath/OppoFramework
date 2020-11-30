package com.android.server.operator;

public class Account extends Element {
    private String label;
    private String logo;
    private String spn;

    public Account() {
        super("account");
    }

    public String toString() {
        return "Account{label='" + this.label + "', logo='" + this.logo + "', country='" + this.country + "', spn='" + this.spn + "'}";
    }

    public String getLabel() {
        return this.label;
    }

    public void setLabel(String label2) {
        this.label = label2;
    }

    public String getLogo() {
        return this.logo;
    }

    public void setLogo(String logo2) {
        this.logo = logo2;
    }

    public String getSpn() {
        return this.spn;
    }

    public void setSpn(String spn2) {
        this.spn = spn2;
    }
}
