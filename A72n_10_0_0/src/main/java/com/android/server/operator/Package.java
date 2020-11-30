package com.android.server.operator;

import com.android.server.pm.Settings;

public class Package extends Element {
    private String path;
    private String pkgName;
    private String reboot;
    private String removable;

    public String toString() {
        return "Package{pkgName='" + this.pkgName + "', path='" + this.path + "', removable='" + this.removable + "', reboot='" + this.reboot + "', country=" + this.country + "'}";
    }

    public String getPkgName() {
        return this.pkgName;
    }

    public void setPkgName(String pkgName2) {
        this.pkgName = pkgName2;
    }

    public String getPath() {
        return this.path;
    }

    public void setPath(String path2) {
        this.path = path2;
    }

    public String getRemovable() {
        return this.removable;
    }

    public void setRemovable(String removable2) {
        this.removable = removable2;
    }

    public String getReboot() {
        return this.reboot;
    }

    public void setReboot(String reboot2) {
        this.reboot = reboot2;
    }

    public Package() {
        super(Settings.ATTR_PACKAGE);
    }
}
