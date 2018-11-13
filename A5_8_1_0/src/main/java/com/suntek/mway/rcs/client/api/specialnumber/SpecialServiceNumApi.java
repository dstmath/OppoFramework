package com.suntek.mway.rcs.client.api.specialnumber;

import android.os.RemoteException;
import com.suntek.mway.rcs.client.api.ServiceApi;
import com.suntek.mway.rcs.client.api.exception.ServiceDisconnectedException;
import java.util.List;

public class SpecialServiceNumApi {
    private static SpecialServiceNumApi instance;

    private SpecialServiceNumApi() {
    }

    public static synchronized SpecialServiceNumApi getInstance() {
        SpecialServiceNumApi specialServiceNumApi;
        synchronized (SpecialServiceNumApi.class) {
            if (instance == null) {
                instance = new SpecialServiceNumApi();
            }
            specialServiceNumApi = instance;
        }
        return specialServiceNumApi;
    }

    public boolean addSsn(String number) throws RemoteException, ServiceDisconnectedException {
        return ServiceApi.getServiceApi().addSsn(number);
    }

    public boolean disableSsn() throws RemoteException, ServiceDisconnectedException {
        return ServiceApi.getServiceApi().disableSsn();
    }

    public String deleteSsnPrefix(String number) throws RemoteException, ServiceDisconnectedException {
        return ServiceApi.getServiceApi().deleteSsnPrefix(number);
    }

    public List<String> getSsnList() throws RemoteException, ServiceDisconnectedException {
        return ServiceApi.getServiceApi().getSsnList();
    }

    public boolean enableSsn() throws RemoteException, ServiceDisconnectedException {
        return ServiceApi.getServiceApi().enableSsn();
    }

    public boolean deleteSsn(String number) throws RemoteException, ServiceDisconnectedException {
        return ServiceApi.getServiceApi().deleteSsn(number);
    }

    public boolean deleteAllSsn() throws RemoteException, ServiceDisconnectedException {
        return ServiceApi.getServiceApi().deleteAllSsn();
    }
}
