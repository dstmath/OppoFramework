package com.suntek.mway.rcs.client.api.emoticon;

import android.os.RemoteException;
import com.suntek.mway.rcs.client.aidl.plugin.entity.emoticon.EmojiPackageBO;
import com.suntek.mway.rcs.client.aidl.plugin.entity.emoticon.EmoticonBO;
import com.suntek.mway.rcs.client.api.PluginApi;
import com.suntek.mway.rcs.client.api.exception.ServiceDisconnectedException;
import java.util.List;

public class EmoticonApi {
    private static EmoticonApi instance;

    private EmoticonApi() {
    }

    public static synchronized EmoticonApi getInstance() {
        EmoticonApi emoticonApi;
        synchronized (EmoticonApi.class) {
            if (instance == null) {
                instance = new EmoticonApi();
            }
            emoticonApi = instance;
        }
        return emoticonApi;
    }

    public byte[] decrypt2Bytes(String emoticonId, int emoFileType) throws RemoteException, ServiceDisconnectedException {
        return PluginApi.getPluginApi().decrypt2Bytes(emoticonId, emoFileType);
    }

    public EmojiPackageBO getEmojiPackage(String packageId) throws RemoteException, ServiceDisconnectedException {
        return PluginApi.getPluginApi().getEmojiPackage(packageId);
    }

    public EmojiPackageBO getEmojiPackageWithDetail(String packageId) throws RemoteException, ServiceDisconnectedException {
        return PluginApi.getPluginApi().getEmojiPackageWithDetail(packageId);
    }

    public EmoticonBO getEmoticon(String emoticonId) throws RemoteException, ServiceDisconnectedException {
        return PluginApi.getPluginApi().getEmoticon(emoticonId);
    }

    public String getStorageRootPath() throws RemoteException, ServiceDisconnectedException {
        return PluginApi.getPluginApi().getStorageRootPath();
    }

    public boolean isCanSend(String emoticonId) throws RemoteException, ServiceDisconnectedException {
        return PluginApi.getPluginApi().isCanSend(emoticonId);
    }

    public boolean isEmojiPackageExist(String packageId) throws RemoteException, ServiceDisconnectedException {
        return PluginApi.getPluginApi().isEmojiPackageExist(packageId);
    }

    public boolean isEmojiStoreInstall() throws RemoteException, ServiceDisconnectedException {
        return PluginApi.getPluginApi().isEmojiStoreInstall();
    }

    public List<EmojiPackageBO> queryEmojiPackages() throws RemoteException, ServiceDisconnectedException {
        return PluginApi.getPluginApi().queryEmojiPackages();
    }

    public List<EmojiPackageBO> queryEmojiPackagesWithDetail() throws RemoteException, ServiceDisconnectedException {
        return PluginApi.getPluginApi().queryEmojiPackagesWithDetail();
    }

    public List<EmoticonBO> queryEmoticonName(String emoticonName) throws RemoteException, ServiceDisconnectedException {
        return PluginApi.getPluginApi().queryEmoticonName(emoticonName);
    }

    public List<EmoticonBO> queryEmoticons(String packageId) throws RemoteException, ServiceDisconnectedException {
        return PluginApi.getPluginApi().queryEmoticons(packageId);
    }

    public void startEmojiStoreApp() throws RemoteException, ServiceDisconnectedException {
        PluginApi.getPluginApi().startEmojiStoreApp();
    }

    public void downloadEmoticon(String emoticonId, long messageRowId) throws RemoteException, ServiceDisconnectedException {
        PluginApi.getPluginApi().downloadEmoticon(emoticonId, messageRowId);
    }
}
