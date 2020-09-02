package com.android.server.wm;

import android.app.IOppoArmyManager;
import android.common.OppoFeatureCache;
import android.content.Context;
import android.os.Environment;
import android.os.ServiceManager;
import java.io.File;
import java.util.List;

public class OppoArmyService extends IOppoArmyManager.Stub {
    private static final boolean DBG_CUSTOMIZE = false;
    private static final String SERVICE_NAME = "oppo_army";
    private static final String TAG = "OppoArmyService";
    private final File mNotAllowAppFilename = new File(this.mSystemDir, "not_allow_packages.xml");
    IPswOppoArmyServiceFeatrue mOppoArmyServiceFeatrue;
    private final File mSystemDir = new File(Environment.getDataDirectory(), "system");

    public void publish(Context context) {
        ServiceManager.addService(SERVICE_NAME, asBinder());
        this.mOppoArmyServiceFeatrue = OppoFeatureCache.getOrCreate(IPswOppoArmyServiceFeatrue.DEFAULT, new Object[0]);
        this.mOppoArmyServiceFeatrue.init(context);
    }

    public void systemReady() {
        this.mOppoArmyServiceFeatrue.systemReady();
    }

    public boolean addDisallowedRunningApp(List<String> appPkgNamesList) {
        return this.mOppoArmyServiceFeatrue.addDisallowedRunningApp(appPkgNamesList);
    }

    public boolean removeDisallowedRunningApp(List<String> appPkgNamesList) {
        return this.mOppoArmyServiceFeatrue.removeDisallowedRunningApp(appPkgNamesList);
    }

    public List<String> getDisallowedRunningApp() {
        return this.mOppoArmyServiceFeatrue.getDisallowedRunningApp();
    }

    public void allowToUseSdcard(boolean allow) {
        this.mOppoArmyServiceFeatrue.allowToUseSdcard(allow);
    }

    public boolean isRunningDisallowed(String pkgName) {
        return this.mOppoArmyServiceFeatrue.isRunningDisallowed(pkgName);
    }
}
