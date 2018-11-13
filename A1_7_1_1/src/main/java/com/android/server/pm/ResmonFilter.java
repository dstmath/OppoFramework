package com.android.server.pm;

import android.content.pm.PackageParser.Package;
import android.os.SystemProperties;
import java.util.ArrayList;
import java.util.HashMap;

final class ResmonFilter {
    ResmonFilter() {
    }

    void filt(Settings settings, HashMap<String, Package> packages) {
        if ("eng".equals(SystemProperties.get("ro.build.type"))) {
            ResmonWhitelistPackage rsp = new ResmonWhitelistPackage();
            rsp.readList();
            if (!rsp.mPackages.isEmpty() && rsp.mPackages.size() > 0) {
                PackageSetting ps;
                ArrayList<Integer> excludeUidList = new ArrayList();
                for (int i = 0; i < rsp.mPackages.size(); i++) {
                    ps = (PackageSetting) settings.mPackages.get(rsp.mPackages.get(i));
                    if (!(ps == null || ps.pkg == null || excludeUidList.contains(Integer.valueOf(ps.pkg.applicationInfo.uid)))) {
                        excludeUidList.add(Integer.valueOf(ps.pkg.applicationInfo.uid));
                    }
                }
                ArrayList<Integer> uidList = new ArrayList();
                for (Package pkg : packages.values()) {
                    ps = (PackageSetting) settings.mPackages.get(pkg.packageName);
                    if (ps == null || (ps.pkgFlags & 1) != 0) {
                        int curUid = pkg.applicationInfo.uid;
                        if (!(excludeUidList.contains(Integer.valueOf(curUid)) || uidList.contains(Integer.valueOf(curUid)))) {
                            uidList.add(Integer.valueOf(curUid));
                        }
                    }
                }
                new ResmonUidList().updateList(uidList);
            }
        }
    }
}
