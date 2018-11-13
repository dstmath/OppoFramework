package com.suntek.mway.rcs.client.api.profile;

import android.os.RemoteException;
import com.suntek.mway.rcs.client.aidl.plugin.entity.profile.Avatar;
import com.suntek.mway.rcs.client.aidl.plugin.entity.profile.Profile;
import com.suntek.mway.rcs.client.api.PluginApi;
import com.suntek.mway.rcs.client.api.exception.ServiceDisconnectedException;

public class ProfileApi {
    private static ProfileApi instance;

    private ProfileApi() {
    }

    public static synchronized ProfileApi getInstance() {
        ProfileApi profileApi;
        synchronized (ProfileApi.class) {
            if (instance == null) {
                instance = new ProfileApi();
            }
            profileApi = instance;
        }
        return profileApi;
    }

    public void getHeadPicByContact(long contactId, ProfileListener listener) throws RemoteException, ServiceDisconnectedException {
        PluginApi.getPluginApi().getHeadPicByContact(contactId, listener);
    }

    public void getHeadPicByNumber(String number, int pixel, ProfileListener listener) throws RemoteException, ServiceDisconnectedException {
        PluginApi.getPluginApi().getHeadPicByNumber(number, pixel, listener);
    }

    public void getMyHeadPic(ProfileListener listener) throws RemoteException, ServiceDisconnectedException {
        PluginApi.getPluginApi().getMyHeadPic(listener);
    }

    public void getMyProfile(ProfileListener listener) throws RemoteException, ServiceDisconnectedException {
        PluginApi.getPluginApi().getMyProfile(listener);
    }

    public String getUpdateTimeOfContactsHeadPic() throws RemoteException, ServiceDisconnectedException {
        return PluginApi.getPluginApi().getUpdateTimeOfContactsHeadPic();
    }

    public void refreshMyQRImg(Profile profile, boolean includeEInfo, ProfileListener listener) throws RemoteException, ServiceDisconnectedException {
        PluginApi.getPluginApi().refreshMyQRImg(profile, includeEInfo, listener);
    }

    public void setMyHeadPic(Avatar avatar, ProfileListener listener) throws RemoteException, ServiceDisconnectedException {
        PluginApi.getPluginApi().setMyHeadPic(avatar, listener);
    }

    public void setMyProfile(Profile profile, ProfileListener listener) throws RemoteException, ServiceDisconnectedException {
        PluginApi.getPluginApi().setMyProfile(profile, listener);
    }

    public void updateContactsHeadPicAtFixedRateEveryDay(String hhmm, ProfileListener listener) throws RemoteException, ServiceDisconnectedException {
        PluginApi.getPluginApi().updateContactsHeadPicAtFixedRateEveryDay(hhmm, listener);
    }
}
