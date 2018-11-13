package com.suntek.mway.rcs.client.api.capability;

import android.os.RemoteException;
import com.suntek.mway.rcs.client.api.ServiceApi;
import com.suntek.mway.rcs.client.api.exception.ServiceDisconnectedException;

public class CapabilityApi {
    private static CapabilityApi instance;

    private CapabilityApi() {
    }

    public static synchronized CapabilityApi getInstance() {
        CapabilityApi capabilityApi;
        synchronized (CapabilityApi.class) {
            if (instance == null) {
                instance = new CapabilityApi();
            }
            capabilityApi = instance;
        }
        return capabilityApi;
    }

    public void getCapability(String number, boolean fromServer, CapabiltyListener listener) throws RemoteException, ServiceDisconnectedException {
        ServiceApi.getServiceApi().getCapability(number, fromServer, listener);
    }
}
