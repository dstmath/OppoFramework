package com.android.server.operator;

import android.util.ArraySet;
import java.util.Set;

public class PermissionGroup extends Element {
    private String grant;
    private String grpName;
    private String level;
    private Set<String> packages = new ArraySet();
    private Set<String> permissions = new ArraySet();
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

    public String getGrpName() {
        return this.grpName;
    }

    public void setGrpName(String grpName2) {
        this.grpName = grpName2;
    }

    public Set<String> getPermissions() {
        return this.permissions;
    }

    public void setPermissions(Set<String> permissions2) {
        this.permissions.addAll(permissions2);
    }

    public PermissionGroup() {
        super("permissionGroup");
    }

    public String toString() {
        return "PermissionGroup{level='" + this.level + '\'' + ", grpName='" + this.grpName + '\'' + ", packages=" + this.packages + '\'' + ", grant='" + this.grant + '\'' + ", systemFixed=" + this.systemFixed + '\'' + ", permissions=" + this.permissions + '\'' + ", country=" + this.country + '}';
    }
}
