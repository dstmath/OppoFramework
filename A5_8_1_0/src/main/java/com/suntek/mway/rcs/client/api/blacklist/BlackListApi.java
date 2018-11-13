package com.suntek.mway.rcs.client.api.blacklist;

import android.os.RemoteException;
import com.suntek.mway.rcs.client.api.ServiceApi;
import com.suntek.mway.rcs.client.api.exception.ServiceDisconnectedException;
import java.util.List;

public class BlackListApi {
    private static BlackListApi instance;

    private BlackListApi() {
    }

    public static synchronized BlackListApi getInstance() {
        BlackListApi blackListApi;
        synchronized (BlackListApi.class) {
            if (instance == null) {
                instance = new BlackListApi();
            }
            blackListApi = instance;
        }
        return blackListApi;
    }

    public boolean addBlacklist(String number, String name) throws RemoteException, ServiceDisconnectedException {
        return ServiceApi.getServiceApi().addBlacklist(number, name);
    }

    public boolean isBlacklist(String number) throws RemoteException, ServiceDisconnectedException {
        return ServiceApi.getServiceApi().isBlacklist(number);
    }

    public void clearBlacklist() throws RemoteException, ServiceDisconnectedException {
        ServiceApi.getServiceApi().clearBlacklist();
    }

    public List<String> getBlacklist() throws RemoteException, ServiceDisconnectedException {
        return ServiceApi.getServiceApi().getBlacklist();
    }

    public boolean deleteBlacklist(String number) throws RemoteException, ServiceDisconnectedException {
        return ServiceApi.getServiceApi().deleteBlacklist(number);
    }
}
