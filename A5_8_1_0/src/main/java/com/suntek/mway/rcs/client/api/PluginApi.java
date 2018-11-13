package com.suntek.mway.rcs.client.api;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import com.suntek.mway.rcs.client.aidl.constant.Main;
import com.suntek.mway.rcs.client.aidl.plugin.IPluginApi;
import com.suntek.mway.rcs.client.aidl.plugin.IPluginApi.Stub;
import com.suntek.mway.rcs.client.api.exception.ServiceDisconnectedException;
import com.suntek.mway.rcs.client.api.log.LogHelper;
import com.suntek.mway.rcs.client.api.util.VerificationUtil;

public class PluginApi {
    private static PluginApi instance;
    private static IPluginApi pluginApi;
    protected final int MAX_RECONECTION_TIMES = 5;
    private String PLUGIN_BIND_AIDL = Main.PLUGIN_BIND_AIDL;
    protected Context context = null;
    private boolean isBinded = false;
    protected boolean isNormallyClosed = false;
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceDisconnected(ComponentName name) {
            if (PluginApi.this.isNormallyClosed || PluginApi.this.reconnectionTimes > 5) {
                LogHelper.d("plugin api disconnect service");
                PluginApi.pluginApi = null;
                PluginApi.this.reconnectionTimes = 1;
                PluginApi.this.notifyServiceDisconnected();
                return;
            }
            LogHelper.d("illegal call serviceApi api disconnect service :" + PluginApi.this.reconnectionTimes);
            PluginApi.this.init(PluginApi.this.context, PluginApi.this.rcsListener);
            if (!PluginApi.this.isBinded()) {
                PluginApi.pluginApi = null;
                PluginApi.this.notifyServiceDisconnected();
            }
            PluginApi pluginApi = PluginApi.this;
            pluginApi.reconnectionTimes++;
        }

        public void onServiceConnected(ComponentName name, IBinder service) {
            PluginApi.pluginApi = Stub.asInterface(service);
            PluginApi.this.reconnectionTimes = 1;
            PluginApi.this.notifyServiceConnected();
            LogHelper.d("IServiceApi have success connect");
        }
    };
    protected ServiceListener rcsListener = null;
    protected int reconnectionTimes = 1;

    private PluginApi() {
    }

    public static synchronized PluginApi getInstance() {
        PluginApi pluginApi;
        synchronized (PluginApi.class) {
            if (instance == null) {
                instance = new PluginApi();
            }
            pluginApi = instance;
        }
        return pluginApi;
    }

    public static IPluginApi getPluginApi() throws ServiceDisconnectedException {
        VerificationUtil.ApiIsNull(pluginApi);
        return pluginApi;
    }

    public boolean isBinded() {
        return this.isBinded;
    }

    public void init(Context context, ServiceListener listener) {
        this.rcsListener = listener;
        this.context = context;
        Intent intent = new Intent();
        intent.setClassName(Main.PACKAGE_NAME, this.PLUGIN_BIND_AIDL);
        this.isBinded = context.bindService(intent, this.mConnection, 1);
        LogHelper.d("bind " + this.PLUGIN_BIND_AIDL + "--> result:" + this.isBinded);
    }

    public void destory(Context context) {
        try {
            if (this.isBinded) {
                LogHelper.d("destory()--> to destroy service : " + this.PLUGIN_BIND_AIDL);
                this.isNormallyClosed = true;
                context.unbindService(this.mConnection);
            } else {
                LogHelper.i("destory()--> service(" + this.PLUGIN_BIND_AIDL + ") already unbinded, do not need to destroy.");
            }
        } catch (Exception e) {
            LogHelper.e("unbind " + this.PLUGIN_BIND_AIDL + "--> result:" + e.getMessage(), e);
        } catch (Throwable th) {
            this.isBinded = false;
        }
        this.isBinded = false;
    }

    protected void notifyServiceConnected() {
        if (this.rcsListener != null) {
            try {
                this.rcsListener.onServiceConnected();
            } catch (RemoteException e) {
                LogHelper.e(e.getMessage(), e);
            }
        }
    }

    protected void notifyServiceDisconnected() {
        if (this.rcsListener != null) {
            try {
                this.rcsListener.onServiceDisconnected();
            } catch (RemoteException e) {
                LogHelper.e(e.getMessage(), e);
            }
        }
    }
}
