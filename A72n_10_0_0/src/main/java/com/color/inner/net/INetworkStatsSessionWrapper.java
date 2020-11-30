package com.color.inner.net;

import android.net.INetworkStatsService;
import android.net.INetworkStatsSession;
import android.net.NetworkStats;
import android.net.NetworkTemplate;
import android.os.ServiceManager;
import android.util.Log;
import java.lang.reflect.Method;

public class INetworkStatsSessionWrapper {
    private static final String TAG = "INetworkStatsSessionWrapper";
    private INetworkStatsSession mINetworkStatsSession = null;

    INetworkStatsSessionWrapper() {
        try {
            this.mINetworkStatsSession = INetworkStatsService.Stub.asInterface(ServiceManager.getService("netstats")).openSession();
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
        }
    }

    public NetworkStatsWrapper getDeviceSummaryForNetwork(NetworkTemplateWrapper networkTemplateWrapper, long start, long end) {
        try {
            return new NetworkStatsWrapper(this.mINetworkStatsSession.getDeviceSummaryForNetwork(networkTemplateWrapper.mNetworkTemplate, start, end));
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
            return null;
        }
    }

    public NetworkStatsWrapper getIncrementForNetwork(NetworkTemplateWrapper networkTemplateWrapper) {
        try {
            return new NetworkStatsWrapper((NetworkStats) callMethodByReflect(this.mINetworkStatsSession, "getIncrementForNetwork", new Class[]{NetworkTemplate.class}, new Object[]{networkTemplateWrapper.mNetworkTemplate}));
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
            return null;
        }
    }

    public void close() {
        try {
            this.mINetworkStatsSession.close();
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
        }
    }

    private static Object callMethodByReflect(Object object, String methodName, Class<?>[] paramTypes, Object[] args) {
        try {
            Method method = object.getClass().getDeclaredMethod(methodName, paramTypes);
            method.setAccessible(true);
            return method.invoke(object, args);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            return null;
        }
    }
}
