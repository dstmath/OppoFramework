package com.android.server.operator;

import android.util.ArraySet;
import java.util.Set;

public class Permission extends Element {
    private String grant;
    private String level;
    private Set<String> packages = new ArraySet();
    private String permName;
    private boolean systemFixed;

    public String getLevel() {
        return this.level;
    }

    public void setLevel(String level2) {
        this.level = level2;
    }

    public Set<String> getPackages() {
        return this.packages;
    }

    public void setPackages(Set<String> packages2) {
        this.packages.addAll(packages2);
    }

    public String getGrant() {
        return this.grant;
    }

    public void setGrant(String grant2) {
        this.grant = grant2;
    }

    public boolean isSystemFixed() {
        return this.systemFixed;
    }

    public void setSystemFixed(boolean systemFixed2) {
        this.systemFixed = systemFixed2;
    }

    public Permission() {
        super("permission");
    }

    public String getPermission() {
        return this.permName;
    }

    public String getPermName() {
        return this.permName;
    }

    public void setPermName(String permName2) {
        this.permName = permName2;
    }

    public String toString() {
        return "Permission{level='" + this.level + '\'' + ", permName='" + this.permName + '\'' + ", packages=" + this.packages + '\'' + ", grant='" + this.grant + '\'' + ", systemFixed=" + this.systemFixed + '\'' + ", country=" + this.country + '\'' + '}';
    }
}
