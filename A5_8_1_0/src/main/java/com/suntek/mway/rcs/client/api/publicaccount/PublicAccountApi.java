package com.suntek.mway.rcs.client.api.publicaccount;

import android.os.RemoteException;
import com.suntek.mway.rcs.client.aidl.plugin.callback.IPublicAccountCallbackAPI;
import com.suntek.mway.rcs.client.api.PluginApi;
import com.suntek.mway.rcs.client.api.exception.ServiceDisconnectedException;

public class PublicAccountApi {
    private static PublicAccountApi instance;

    private PublicAccountApi() {
    }

    public static synchronized PublicAccountApi getInstance() {
        PublicAccountApi publicAccountApi;
        synchronized (PublicAccountApi.class) {
            if (instance == null) {
                instance = new PublicAccountApi();
            }
            publicAccountApi = instance;
        }
        return publicAccountApi;
    }

    public void addSubscribe(String uuid, PublicAccountCallback callback) throws RemoteException, ServiceDisconnectedException {
        PluginApi.getPluginApi().registerCallback(callback);
        PluginApi.getPluginApi().addSubscribe(uuid);
    }

    public void cancelSubscribe(String uuid, PublicAccountCallback callback) throws RemoteException, ServiceDisconnectedException {
        PluginApi.getPluginApi().registerCallback(callback);
        PluginApi.getPluginApi().cancelSubscribe(uuid);
    }

    public void complainPublic(String uuid, String reason, String description, int type, String data, PublicAccountCallback callback) throws RemoteException, ServiceDisconnectedException {
        PluginApi.getPluginApi().registerCallback(callback);
        PluginApi.getPluginApi().complainPublic(uuid, reason, description, type, data);
    }

    public void getPreMessage(String uuid, String timestamp, int order, int pageSize, int pageNum, PublicAccountCallback callback) throws RemoteException, ServiceDisconnectedException {
        PluginApi.getPluginApi().registerCallback(callback);
        PluginApi.getPluginApi().getPreMessage(uuid, timestamp, order, pageSize, pageNum);
    }

    public void getPublicDetail(String uuid, PublicAccountCallback callback) throws RemoteException, ServiceDisconnectedException {
        PluginApi.getPluginApi().registerCallback(callback);
        PluginApi.getPluginApi().getPublicDetail(uuid);
    }

    public void getPublicList(String keywords, int pageSize, int pageNum, int order, PublicAccountCallback callback) throws RemoteException, ServiceDisconnectedException {
        PluginApi.getPluginApi().registerCallback(callback);
        PluginApi.getPluginApi().getPublicList(keywords, pageSize, pageNum, order);
    }

    public void getPublicMenuInfo(String uuid, PublicAccountCallback callback) throws RemoteException, ServiceDisconnectedException {
        PluginApi.getPluginApi().registerCallback(callback);
        PluginApi.getPluginApi().getPublicMenuInfo(uuid);
    }

    public void getRecommendPublic(int type, int pageSize, int pageNum, PublicAccountCallback callback) throws RemoteException, ServiceDisconnectedException {
        PluginApi.getPluginApi().registerCallback(callback);
        PluginApi.getPluginApi().getRecommendPublic(type, pageSize, pageNum);
    }

    public void getUserSubscribePublicList(PublicAccountCallback callback) throws RemoteException, ServiceDisconnectedException {
        PluginApi.getPluginApi().registerCallback(callback);
        PluginApi.getPluginApi().getUserSubscribePublicList();
    }

    public void setAcceptStatus(String uuid, int acceptStatus, PublicAccountCallback callback) throws RemoteException, ServiceDisconnectedException {
        PluginApi.getPluginApi().registerCallback(callback);
        PluginApi.getPluginApi().setAcceptStatus(uuid, acceptStatus);
    }

    public void unregisterCallback(IPublicAccountCallbackAPI callback) throws RemoteException, ServiceDisconnectedException {
        PluginApi.getPluginApi().unregisterCallback(callback);
    }
}
