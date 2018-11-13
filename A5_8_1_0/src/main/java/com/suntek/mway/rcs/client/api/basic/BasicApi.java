package com.suntek.mway.rcs.client.api.basic;

import android.os.RemoteException;
import com.suntek.mway.rcs.client.api.ServiceApi;
import com.suntek.mway.rcs.client.api.exception.ServiceDisconnectedException;

public class BasicApi {
    private static BasicApi instance;

    private BasicApi() {
    }

    public static synchronized BasicApi getInstance() {
        BasicApi basicApi;
        synchronized (BasicApi.class) {
            if (instance == null) {
                instance = new BasicApi();
            }
            basicApi = instance;
        }
        return basicApi;
    }

    public void login(String account) throws RemoteException, ServiceDisconnectedException {
        ServiceApi.getServiceApi().login(account);
    }

    public void logout() throws RemoteException, ServiceDisconnectedException {
        ServiceApi.getServiceApi().logout();
    }

    public void startPluginCenter() throws RemoteException, ServiceDisconnectedException {
        ServiceApi.getServiceApi().startPluginCenter();
    }

    public void openAccount() throws RemoteException, ServiceDisconnectedException {
        ServiceApi.getServiceApi().openAccount();
    }

    public void rejectOpenAccount() throws RemoteException, ServiceDisconnectedException {
        ServiceApi.getServiceApi().rejectOpenAccount();
    }

    public void getConfiguration() throws RemoteException, ServiceDisconnectedException {
        ServiceApi.getServiceApi().getConfiguration();
    }

    public void getConfigurationWithOtp(String otpCode) throws RemoteException, ServiceDisconnectedException {
        ServiceApi.getServiceApi().getConfigurationWithOtp(otpCode);
    }

    public String getAccount() throws RemoteException, ServiceDisconnectedException {
        return ServiceApi.getServiceApi().getAccount();
    }

    public boolean isOnline() throws RemoteException, ServiceDisconnectedException {
        return ServiceApi.getServiceApi().isOnline();
    }

    public void startService() throws RemoteException, ServiceDisconnectedException {
        ServiceApi.getServiceApi().startService();
    }
}
