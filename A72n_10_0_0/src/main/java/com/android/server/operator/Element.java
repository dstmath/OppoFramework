package com.android.server.operator;

public class Element {
    String country;
    String name;

    public Element(String name2) {
        this.name = name2;
    }

    public String getCountry() {
        return this.country;
    }

    public void setCountry(String country2) {
        this.country = country2;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name2) {
        this.name = name2;
    }
}
