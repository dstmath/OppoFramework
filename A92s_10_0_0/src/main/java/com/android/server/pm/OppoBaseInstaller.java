package com.android.server.pm;

import android.content.Context;
import com.android.server.SystemService;

public abstract class OppoBaseInstaller extends SystemService {
    public OppoBaseInstaller(Context context) {
        super(context);
    }

    public boolean updateAppProfile(String pkg, int userId, int appId, String profileName, String codePath, String dexMetadataPath) {
        return false;
    }

    public boolean dumpAppClassAndMethod(int uid, String packageName, String profilePath, String codePath, String outputMethodPath) {
        return false;
    }

    public boolean createAppProfile(int uid, String packageName, String hotMethodPath, String codePath, String outputProfilePath) {
        return false;
    }
}
