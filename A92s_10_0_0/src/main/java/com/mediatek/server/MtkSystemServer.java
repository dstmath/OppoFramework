package com.mediatek.server;

import android.content.Context;
import android.util.Slog;
import android.util.TimingsTraceLog;
import com.android.server.NetworkManagementService;
import com.android.server.SystemServiceManager;
import com.android.server.net.NetworkPolicyManagerService;
import com.android.server.net.NetworkStatsService;
import dalvik.system.PathClassLoader;

public class MtkSystemServer {
    public static PathClassLoader sClassLoader;
    private static MtkSystemServer sInstance;

    public static MtkSystemServer getInstance() {
        if (sInstance == null) {
            try {
                sClassLoader = new PathClassLoader("/system/framework/mediatek-services.jar", MtkSystemServer.class.getClassLoader());
                sInstance = (MtkSystemServer) Class.forName("com.mediatek.server.MtkSystemServerImpl", false, sClassLoader).getConstructor(new Class[0]).newInstance(new Object[0]);
            } catch (Exception e) {
                Slog.e("MtkSystemServer", "getInstance: " + e.toString());
                sInstance = new MtkSystemServer();
            }
        }
        return sInstance;
    }

    public void setPrameters(TimingsTraceLog btt, SystemServiceManager ssm, Context context) {
    }

    public void startMtkBootstrapServices() {
    }

    public void startMtkCoreServices() {
    }

    public boolean startMtkAlarmManagerService() {
        return false;
    }

    public void startMtkOtherServices() {
    }

    public boolean startMtkStorageManagerService() {
        return false;
    }

    public Object getMtkConnectivityService(NetworkManagementService networkManagement, NetworkStatsService networkStats, NetworkPolicyManagerService networkPolicy) {
        return null;
    }

    public void addBootEvent(String bootevent) {
    }
}
