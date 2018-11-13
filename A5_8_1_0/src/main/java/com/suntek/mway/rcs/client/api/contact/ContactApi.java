package com.suntek.mway.rcs.client.api.contact;

import android.os.RemoteException;
import com.suntek.mway.rcs.client.aidl.plugin.callback.IContactSyncListener;
import com.suntek.mway.rcs.client.aidl.plugin.entity.contact.IntervalAction;
import com.suntek.mway.rcs.client.aidl.plugin.entity.contact.SyncAction;
import com.suntek.mway.rcs.client.api.PluginApi;
import com.suntek.mway.rcs.client.api.exception.ServiceDisconnectedException;

public class ContactApi {
    private static ContactApi instance;

    private ContactApi() {
    }

    public static synchronized ContactApi getInstance() {
        ContactApi contactApi;
        synchronized (ContactApi.class) {
            if (instance == null) {
                instance = new ContactApi();
            }
            contactApi = instance;
        }
        return contactApi;
    }

    public void cancelIntervalSync() throws RemoteException, ServiceDisconnectedException {
        PluginApi.getPluginApi().cancelIntervalSync();
    }

    public void doSync(SyncAction syncAction, IContactSyncListener listener) throws RemoteException, ServiceDisconnectedException {
        PluginApi.getPluginApi().doSync(syncAction.ordinal(), listener);
    }

    public int getAutoSync() throws RemoteException, ServiceDisconnectedException {
        return PluginApi.getPluginApi().getAutoSync();
    }

    public boolean getEnableAutoSync() throws RemoteException, ServiceDisconnectedException {
        return PluginApi.getPluginApi().getEnableAutoSync();
    }

    public int getIntervalSyncMode() throws RemoteException, ServiceDisconnectedException {
        return PluginApi.getPluginApi().getIntervalSyncMode();
    }

    public int getLocalContactCounts() throws RemoteException, ServiceDisconnectedException {
        return PluginApi.getPluginApi().getLocalContactCounts();
    }

    public boolean getOnlySyncEnableViaWifi() throws RemoteException, ServiceDisconnectedException {
        return PluginApi.getPluginApi().getOnlySyncEnableViaWifi();
    }

    public int getRemoteContactCounts() throws RemoteException, ServiceDisconnectedException {
        return PluginApi.getPluginApi().getRemoteContactCounts();
    }

    public void setEnableAutoSync(boolean status, SyncAction syncAction) throws RemoteException, ServiceDisconnectedException {
        PluginApi.getPluginApi().setEnableAutoSync(status, syncAction.ordinal());
    }

    public void setOnlySyncEnableViaWifi(boolean status) throws RemoteException, ServiceDisconnectedException {
        PluginApi.getPluginApi().setOnlySyncEnableViaWifi(status);
    }

    public void startIntervalSync(SyncAction syncAction, IntervalAction intervalAction, long time) throws RemoteException, ServiceDisconnectedException {
        PluginApi.getPluginApi().startIntervalSync(syncAction.ordinal(), intervalAction.ordinal(), time);
    }
}
