package com.suntek.mway.rcs.client.api;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import com.suntek.mway.rcs.client.aidl.constant.Main;
import com.suntek.mway.rcs.client.aidl.service.IServiceApi;
import com.suntek.mway.rcs.client.aidl.service.IServiceApi.Stub;
import com.suntek.mway.rcs.client.api.exception.ServiceDisconnectedException;
import com.suntek.mway.rcs.client.api.log.LogHelper;
import com.suntek.mway.rcs.client.api.util.VerificationUtil;

public class ServiceApi {
    private static ServiceApi instance;
    private static IServiceApi serviceApi;
    protected final int MAX_RECONECTION_TIMES = 5;
    private String SERVICE_BIND_AIDL = Main.SERVICE_BIND_AIDL;
    protected Context context = null;
    private boolean isBinded = false;
    protected boolean isNormallyClosed = false;
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceDisconnected(ComponentName name) {
            if (ServiceApi.this.isNormallyClosed || ServiceApi.this.reconnectionTimes > 5) {
                LogHelper.d("service api disconnect service");
                ServiceApi.serviceApi = null;
                ServiceApi.this.reconnectionTimes = 1;
                ServiceApi.this.notifyServiceDisconnected();
                return;
            }
            LogHelper.d("illegal call serviceApi api disconnect service :" + ServiceApi.this.reconnectionTimes);
            ServiceApi.this.init(ServiceApi.this.context, ServiceApi.this.rcsListener);
            if (!ServiceApi.this.isBinded()) {
                ServiceApi.serviceApi = null;
                ServiceApi.this.notifyServiceDisconnected();
            }
            ServiceApi serviceApi = ServiceApi.this;
            serviceApi.reconnectionTimes++;
        }

        public void onServiceConnected(ComponentName name, IBinder service) {
            ServiceApi.serviceApi = Stub.asInterface(service);
            ServiceApi.this.reconnectionTimes = 1;
            ServiceApi.this.notifyServiceConnected();
            LogHelper.d("IServiceApi have success connect");
        }
    };
    protected ServiceListener rcsListener = null;
    protected int reconnectionTimes = 1;

    private ServiceApi() {
    }

    public static synchronized ServiceApi getInstance() {
        ServiceApi serviceApi;
        synchronized (ServiceApi.class) {
            if (instance == null) {
                instance = new ServiceApi();
            }
            serviceApi = instance;
        }
        return serviceApi;
    }

    public Context getContext() {
        return this.context;
    }

    public static IServiceApi getServiceApi() throws ServiceDisconnectedException {
        VerificationUtil.ApiIsNull(serviceApi);
        return serviceApi;
    }

    public boolean isBinded() {
        return this.isBinded;
    }

    public void init(Context context, ServiceListener listener) {
        this.rcsListener = listener;
        this.context = context;
        Intent intent = new Intent();
        intent.setClassName(Main.PACKAGE_NAME, this.SERVICE_BIND_AIDL);
        this.isBinded = context.bindService(intent, this.mConnection, 1);
        LogHelper.d("bind " + this.SERVICE_BIND_AIDL + "--> result:" + this.isBinded);
    }

    public void destory(Context context) {
        try {
            if (this.isBinded) {
                LogHelper.d("destory()--> to destroy service : " + this.SERVICE_BIND_AIDL);
                this.isNormallyClosed = true;
                context.unbindService(this.mConnection);
            } else {
                LogHelper.i("destory()--> service(" + this.SERVICE_BIND_AIDL + ") already unbinded, do not need to destroy.");
            }
        } catch (Exception e) {
            LogHelper.e("unbind " + this.SERVICE_BIND_AIDL + "--> result:" + e.getMessage(), e);
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
