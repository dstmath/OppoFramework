package com.android.server.pm;

import java.util.Objects;

/* access modifiers changed from: package-private */
/* compiled from: PackageManagerXmlParse */
public class PackagePathlist {
    private String packageName;
    private String pathName;

    PackagePathlist(String packageNm, String pathNm) {
        this.packageName = packageNm;
        this.pathName = pathNm;
    }

    public String getPackage() {
        return this.packageName;
    }

    public String getPathName() {
        return this.pathName;
    }

    public int hashCode() {
        String str = this.packageName;
        if (str == null || str.equalsIgnoreCase("")) {
            return this.pathName.hashCode() & Integer.MAX_VALUE;
        }
        return this.packageName.hashCode() & Integer.MAX_VALUE;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        PackagePathlist packagePathlist = (PackagePathlist) obj;
        if (!Objects.equals(this.packageName, packagePathlist.packageName) || !Objects.equals(this.pathName, packagePathlist.pathName)) {
            return false;
        }
        return true;
    }
}
