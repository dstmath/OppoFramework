package com.suntek.mway.rcs.client.api.richscreen;

import android.os.RemoteException;
import com.suntek.mway.rcs.client.aidl.plugin.entity.richscrn.ResultInfo;
import com.suntek.mway.rcs.client.aidl.plugin.entity.richscrn.ResultUtil;
import com.suntek.mway.rcs.client.api.PluginApi;
import com.suntek.mway.rcs.client.api.exception.ServiceDisconnectedException;
import java.util.List;

public class RichScreenApi {
    private static RichScreenApi instance;

    private RichScreenApi() {
    }

    public static synchronized RichScreenApi getInstance() {
        RichScreenApi richScreenApi;
        synchronized (RichScreenApi.class) {
            if (instance == null) {
                instance = new RichScreenApi();
            }
            richScreenApi = instance;
        }
        return richScreenApi;
    }

    public ResultInfo clearRichScrnLocalCache(String phoneEvent) throws RemoteException, ServiceDisconnectedException {
        return PluginApi.getPluginApi().clearRichScrnLocalCache(phoneEvent);
    }

    public ResultInfo collectRichScrnObj(String sourceType, String cId) throws RemoteException, ServiceDisconnectedException {
        return PluginApi.getPluginApi().collectRichScrnObj(sourceType, cId);
    }

    public ResultInfo downloadHomeLocRules(String phoneEvent) throws RemoteException, ServiceDisconnectedException {
        return PluginApi.getPluginApi().downloadHomeLocRules(phoneEvent);
    }

    public ResultInfo downloadRichScrnObj(String missdn, String phoneEvent) throws RemoteException, ServiceDisconnectedException {
        return PluginApi.getPluginApi().downloadRichScrnObj(missdn, phoneEvent);
    }

    public ResultUtil getRichScrnObj(String missdn, String phoneEvent) throws RemoteException, ServiceDisconnectedException {
        return PluginApi.getPluginApi().getRichScrnObj(missdn, phoneEvent);
    }

    public ResultInfo richScrnChangeNetWork() throws RemoteException, ServiceDisconnectedException {
        return PluginApi.getPluginApi().richScrnChangeNetWork();
    }

    public void startRichScreenApp(List<String> mobileList) throws RemoteException, ServiceDisconnectedException {
        PluginApi.getPluginApi().startRichScreenApp(mobileList);
    }
}
