package com.suntek.mway.rcs.client.aidl.plugin;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.suntek.mway.rcs.client.aidl.plugin.callback.ICloudOperationCtrl;
import com.suntek.mway.rcs.client.aidl.plugin.callback.IContactSyncListener;
import com.suntek.mway.rcs.client.aidl.plugin.callback.IProfileListener;
import com.suntek.mway.rcs.client.aidl.plugin.callback.IPublicAccountCallbackAPI;
import com.suntek.mway.rcs.client.aidl.plugin.entity.emoticon.EmojiPackageBO;
import com.suntek.mway.rcs.client.aidl.plugin.entity.emoticon.EmoticonBO;
import com.suntek.mway.rcs.client.aidl.plugin.entity.profile.Avatar;
import com.suntek.mway.rcs.client.aidl.plugin.entity.profile.Profile;
import com.suntek.mway.rcs.client.aidl.plugin.entity.richscrn.ResultInfo;
import com.suntek.mway.rcs.client.aidl.plugin.entity.richscrn.ResultUtil;
import java.util.List;

public interface IPluginApi extends IInterface {

    public static abstract class Stub extends Binder implements IPluginApi {
        private static final String DESCRIPTOR = "com.suntek.mway.rcs.client.aidl.plugin.IPluginApi";
        static final int TRANSACTION_addSubscribe = 10;
        static final int TRANSACTION_cancelIntervalSync = 24;
        static final int TRANSACTION_cancelSubscribe = 11;
        static final int TRANSACTION_clearRichScrnLocalCache = 57;
        static final int TRANSACTION_collectRichScrnObj = 60;
        static final int TRANSACTION_complainPublic = 12;
        static final int TRANSACTION_decrypt2Bytes = 50;
        static final int TRANSACTION_doSync = 22;
        static final int TRANSACTION_downloadEmoticon = 54;
        static final int TRANSACTION_downloadFileFromUrl = 34;
        static final int TRANSACTION_downloadHomeLocRules = 58;
        static final int TRANSACTION_downloadRichScrnObj = 56;
        static final int TRANSACTION_getAutoSync = 28;
        static final int TRANSACTION_getEmojiPackage = 45;
        static final int TRANSACTION_getEmojiPackageWithDetail = 46;
        static final int TRANSACTION_getEmoticon = 47;
        static final int TRANSACTION_getEnableAutoSync = 27;
        static final int TRANSACTION_getHeadPicByContact = 5;
        static final int TRANSACTION_getHeadPicByNumber = 6;
        static final int TRANSACTION_getIntervalSyncMode = 25;
        static final int TRANSACTION_getLocalContactCounts = 31;
        static final int TRANSACTION_getLocalRootPath = 38;
        static final int TRANSACTION_getMyHeadPic = 4;
        static final int TRANSACTION_getMyProfile = 3;
        static final int TRANSACTION_getOnlySyncEnableViaWifi = 30;
        static final int TRANSACTION_getPreMessage = 13;
        static final int TRANSACTION_getPublicDetail = 14;
        static final int TRANSACTION_getPublicList = 15;
        static final int TRANSACTION_getPublicMenuInfo = 16;
        static final int TRANSACTION_getRecommendPublic = 20;
        static final int TRANSACTION_getRemoteContactCounts = 32;
        static final int TRANSACTION_getRemoteFileList = 36;
        static final int TRANSACTION_getRichScrnObj = 55;
        static final int TRANSACTION_getShareFileList = 37;
        static final int TRANSACTION_getStorageRootPath = 51;
        static final int TRANSACTION_getUpdateTimeOfContactsHeadPic = 9;
        static final int TRANSACTION_getUserSubscribePublicList = 17;
        static final int TRANSACTION_isCanSend = 49;
        static final int TRANSACTION_isEmojiPackageExist = 48;
        static final int TRANSACTION_isEmojiStoreInstall = 52;
        static final int TRANSACTION_putFile = 33;
        static final int TRANSACTION_queryEmojiPackages = 41;
        static final int TRANSACTION_queryEmojiPackagesWithDetail = 42;
        static final int TRANSACTION_queryEmoticonName = 44;
        static final int TRANSACTION_queryEmoticons = 43;
        static final int TRANSACTION_refreshMyQRImg = 7;
        static final int TRANSACTION_registerCallback = 18;
        static final int TRANSACTION_richScrnChangeNetWork = 59;
        static final int TRANSACTION_setAcceptStatus = 21;
        static final int TRANSACTION_setEnableAutoSync = 26;
        static final int TRANSACTION_setMyHeadPic = 2;
        static final int TRANSACTION_setMyProfile = 1;
        static final int TRANSACTION_setOnlySyncEnableViaWifi = 29;
        static final int TRANSACTION_shareFile = 35;
        static final int TRANSACTION_shareFileAndSend = 39;
        static final int TRANSACTION_shareFileAndSendGroup = 40;
        static final int TRANSACTION_startEmojiStoreApp = 53;
        static final int TRANSACTION_startIntervalSync = 23;
        static final int TRANSACTION_startRichScreenApp = 61;
        static final int TRANSACTION_unregisterCallback = 19;
        static final int TRANSACTION_updateContactsHeadPicAtFixedRateEveryDay = 8;

        private static class Proxy implements IPluginApi {
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            public IBinder asBinder() {
                return this.mRemote;
            }

            public String getInterfaceDescriptor() {
                return Stub.DESCRIPTOR;
            }

            public void setMyProfile(Profile profile, IProfileListener listener) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (profile != null) {
                        _data.writeInt(1);
                        profile.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (listener != null) {
                        iBinder = listener.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setMyHeadPic(Avatar avatar, IProfileListener listener) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (avatar != null) {
                        _data.writeInt(1);
                        avatar.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (listener != null) {
                        iBinder = listener.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void getMyProfile(IProfileListener listener) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (listener != null) {
                        iBinder = listener.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void getMyHeadPic(IProfileListener listener) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (listener != null) {
                        iBinder = listener.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void getHeadPicByContact(long contactId, IProfileListener listener) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(contactId);
                    if (listener != null) {
                        iBinder = listener.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void getHeadPicByNumber(String number, int pixel, IProfileListener listener) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(number);
                    _data.writeInt(pixel);
                    if (listener != null) {
                        iBinder = listener.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void refreshMyQRImg(Profile profile, boolean includeEInfo, IProfileListener listener) throws RemoteException {
                int i = 1;
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (profile != null) {
                        _data.writeInt(1);
                        profile.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!includeEInfo) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    if (listener != null) {
                        iBinder = listener.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(7, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void updateContactsHeadPicAtFixedRateEveryDay(String hhmm, IProfileListener listener) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(hhmm);
                    if (listener != null) {
                        iBinder = listener.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(8, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String getUpdateTimeOfContactsHeadPic() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(9, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void addSubscribe(String uuid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(uuid);
                    this.mRemote.transact(10, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void cancelSubscribe(String uuid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(uuid);
                    this.mRemote.transact(11, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void complainPublic(String uuid, String reason, String description, int type, String data) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(uuid);
                    _data.writeString(reason);
                    _data.writeString(description);
                    _data.writeInt(type);
                    _data.writeString(data);
                    this.mRemote.transact(12, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void getPreMessage(String uuid, String timestamp, int order, int pageSize, int pageNum) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(uuid);
                    _data.writeString(timestamp);
                    _data.writeInt(order);
                    _data.writeInt(pageSize);
                    _data.writeInt(pageNum);
                    this.mRemote.transact(13, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void getPublicDetail(String uuid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(uuid);
                    this.mRemote.transact(14, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void getPublicList(String keywords, int pageSize, int pageNum, int order) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(keywords);
                    _data.writeInt(pageSize);
                    _data.writeInt(pageNum);
                    _data.writeInt(order);
                    this.mRemote.transact(15, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void getPublicMenuInfo(String uuid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(uuid);
                    this.mRemote.transact(16, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void getUserSubscribePublicList() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getUserSubscribePublicList, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void registerCallback(IPublicAccountCallbackAPI callback) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (callback != null) {
                        iBinder = callback.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_registerCallback, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void unregisterCallback(IPublicAccountCallbackAPI callback) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (callback != null) {
                        iBinder = callback.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_unregisterCallback, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void getRecommendPublic(int type, int pageSize, int pageNum) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(type);
                    _data.writeInt(pageSize);
                    _data.writeInt(pageNum);
                    this.mRemote.transact(Stub.TRANSACTION_getRecommendPublic, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setAcceptStatus(String uuid, int acceptStatus) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(uuid);
                    _data.writeInt(acceptStatus);
                    this.mRemote.transact(Stub.TRANSACTION_setAcceptStatus, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void doSync(int syncAction, IContactSyncListener listener) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(syncAction);
                    if (listener != null) {
                        iBinder = listener.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_doSync, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void startIntervalSync(int syncAction, int intervalAction, long time) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(syncAction);
                    _data.writeInt(intervalAction);
                    _data.writeLong(time);
                    this.mRemote.transact(Stub.TRANSACTION_startIntervalSync, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void cancelIntervalSync() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_cancelIntervalSync, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getIntervalSyncMode() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getIntervalSyncMode, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setEnableAutoSync(boolean status, int syncAction) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (status) {
                        i = 1;
                    }
                    _data.writeInt(i);
                    _data.writeInt(syncAction);
                    this.mRemote.transact(Stub.TRANSACTION_setEnableAutoSync, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean getEnableAutoSync() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getEnableAutoSync, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getAutoSync() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getAutoSync, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setOnlySyncEnableViaWifi(boolean status) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (status) {
                        i = 1;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_setOnlySyncEnableViaWifi, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean getOnlySyncEnableViaWifi() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getOnlySyncEnableViaWifi, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getLocalContactCounts() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getLocalContactCounts, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getRemoteContactCounts() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(32, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ICloudOperationCtrl putFile(String localPath, String remotePath, int transOper) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(localPath);
                    _data.writeString(remotePath);
                    _data.writeInt(transOper);
                    this.mRemote.transact(Stub.TRANSACTION_putFile, _data, _reply, 0);
                    _reply.readException();
                    ICloudOperationCtrl _result = com.suntek.mway.rcs.client.aidl.plugin.callback.ICloudOperationCtrl.Stub.asInterface(_reply.readStrongBinder());
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ICloudOperationCtrl downloadFileFromUrl(String remoteUrl, String fileName, int transOper, long chatMessageId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(remoteUrl);
                    _data.writeString(fileName);
                    _data.writeInt(transOper);
                    _data.writeLong(chatMessageId);
                    this.mRemote.transact(Stub.TRANSACTION_downloadFileFromUrl, _data, _reply, 0);
                    _reply.readException();
                    ICloudOperationCtrl _result = com.suntek.mway.rcs.client.aidl.plugin.callback.ICloudOperationCtrl.Stub.asInterface(_reply.readStrongBinder());
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void shareFile(String fileId, String shareDesc) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(fileId);
                    _data.writeString(shareDesc);
                    this.mRemote.transact(Stub.TRANSACTION_shareFile, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void getRemoteFileList(String remotePath, int beginIndex, int endIndex, int fileNodeOrder) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(remotePath);
                    _data.writeInt(beginIndex);
                    _data.writeInt(endIndex);
                    _data.writeInt(fileNodeOrder);
                    this.mRemote.transact(Stub.TRANSACTION_getRemoteFileList, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void getShareFileList(int beginIndex, int endIndex) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(beginIndex);
                    _data.writeInt(endIndex);
                    this.mRemote.transact(Stub.TRANSACTION_getShareFileList, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String getLocalRootPath() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getLocalRootPath, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void shareFileAndSend(String fileId, String shareDesc, List<String> contacts, long threadId, String smsContentTemp, int barCycle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(fileId);
                    _data.writeString(shareDesc);
                    _data.writeStringList(contacts);
                    _data.writeLong(threadId);
                    _data.writeString(smsContentTemp);
                    _data.writeInt(barCycle);
                    this.mRemote.transact(Stub.TRANSACTION_shareFileAndSend, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void shareFileAndSendGroup(String fileId, String shareDesc, long threadId, long groupId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(fileId);
                    _data.writeString(shareDesc);
                    _data.writeLong(threadId);
                    _data.writeLong(groupId);
                    this.mRemote.transact(Stub.TRANSACTION_shareFileAndSendGroup, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<EmojiPackageBO> queryEmojiPackages() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_queryEmojiPackages, _data, _reply, 0);
                    _reply.readException();
                    List<EmojiPackageBO> _result = _reply.createTypedArrayList(EmojiPackageBO.CREATOR);
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<EmojiPackageBO> queryEmojiPackagesWithDetail() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_queryEmojiPackagesWithDetail, _data, _reply, 0);
                    _reply.readException();
                    List<EmojiPackageBO> _result = _reply.createTypedArrayList(EmojiPackageBO.CREATOR);
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<EmoticonBO> queryEmoticons(String packageId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageId);
                    this.mRemote.transact(Stub.TRANSACTION_queryEmoticons, _data, _reply, 0);
                    _reply.readException();
                    List<EmoticonBO> _result = _reply.createTypedArrayList(EmoticonBO.CREATOR);
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<EmoticonBO> queryEmoticonName(String emoticonName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(emoticonName);
                    this.mRemote.transact(Stub.TRANSACTION_queryEmoticonName, _data, _reply, 0);
                    _reply.readException();
                    List<EmoticonBO> _result = _reply.createTypedArrayList(EmoticonBO.CREATOR);
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public EmojiPackageBO getEmojiPackage(String packageId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    EmojiPackageBO _result;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageId);
                    this.mRemote.transact(Stub.TRANSACTION_getEmojiPackage, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (EmojiPackageBO) EmojiPackageBO.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public EmojiPackageBO getEmojiPackageWithDetail(String packageId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    EmojiPackageBO _result;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageId);
                    this.mRemote.transact(Stub.TRANSACTION_getEmojiPackageWithDetail, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (EmojiPackageBO) EmojiPackageBO.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public EmoticonBO getEmoticon(String emoticonId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    EmoticonBO _result;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(emoticonId);
                    this.mRemote.transact(Stub.TRANSACTION_getEmoticon, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (EmoticonBO) EmoticonBO.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isEmojiPackageExist(String packageId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageId);
                    this.mRemote.transact(Stub.TRANSACTION_isEmojiPackageExist, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isCanSend(String emoticonId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(emoticonId);
                    this.mRemote.transact(Stub.TRANSACTION_isCanSend, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public byte[] decrypt2Bytes(String emoticonId, int emoFileType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(emoticonId);
                    _data.writeInt(emoFileType);
                    this.mRemote.transact(Stub.TRANSACTION_decrypt2Bytes, _data, _reply, 0);
                    _reply.readException();
                    byte[] _result = _reply.createByteArray();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String getStorageRootPath() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getStorageRootPath, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isEmojiStoreInstall() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_isEmojiStoreInstall, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void startEmojiStoreApp() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_startEmojiStoreApp, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void downloadEmoticon(String emoticonId, long messageRowId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(emoticonId);
                    _data.writeLong(messageRowId);
                    this.mRemote.transact(Stub.TRANSACTION_downloadEmoticon, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ResultUtil getRichScrnObj(String missdn, String phoneEvent) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    ResultUtil _result;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(missdn);
                    _data.writeString(phoneEvent);
                    this.mRemote.transact(Stub.TRANSACTION_getRichScrnObj, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (ResultUtil) ResultUtil.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ResultInfo downloadRichScrnObj(String missdn, String phoneEvent) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    ResultInfo _result;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(missdn);
                    _data.writeString(phoneEvent);
                    this.mRemote.transact(Stub.TRANSACTION_downloadRichScrnObj, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (ResultInfo) ResultInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ResultInfo clearRichScrnLocalCache(String phoneEvent) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    ResultInfo _result;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(phoneEvent);
                    this.mRemote.transact(Stub.TRANSACTION_clearRichScrnLocalCache, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (ResultInfo) ResultInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ResultInfo downloadHomeLocRules(String phoneEvent) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    ResultInfo _result;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(phoneEvent);
                    this.mRemote.transact(Stub.TRANSACTION_downloadHomeLocRules, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (ResultInfo) ResultInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ResultInfo richScrnChangeNetWork() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    ResultInfo _result;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_richScrnChangeNetWork, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (ResultInfo) ResultInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ResultInfo collectRichScrnObj(String sourceType, String cId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    ResultInfo _result;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(sourceType);
                    _data.writeString(cId);
                    this.mRemote.transact(60, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (ResultInfo) ResultInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void startRichScreenApp(List<String> mobileList) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringList(mobileList);
                    this.mRemote.transact(Stub.TRANSACTION_startRichScreenApp, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IPluginApi asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IPluginApi)) {
                return new Proxy(obj);
            }
            return (IPluginApi) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            Profile _arg0;
            String _result;
            int _result2;
            boolean _result3;
            ICloudOperationCtrl _result4;
            List<EmojiPackageBO> _result5;
            List<EmoticonBO> _result6;
            EmojiPackageBO _result7;
            ResultInfo _result8;
            switch (code) {
                case 1:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg0 = (Profile) Profile.CREATOR.createFromParcel(data);
                    } else {
                        _arg0 = null;
                    }
                    setMyProfile(_arg0, com.suntek.mway.rcs.client.aidl.plugin.callback.IProfileListener.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case 2:
                    Avatar _arg02;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg02 = (Avatar) Avatar.CREATOR.createFromParcel(data);
                    } else {
                        _arg02 = null;
                    }
                    setMyHeadPic(_arg02, com.suntek.mway.rcs.client.aidl.plugin.callback.IProfileListener.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case 3:
                    data.enforceInterface(DESCRIPTOR);
                    getMyProfile(com.suntek.mway.rcs.client.aidl.plugin.callback.IProfileListener.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case 4:
                    data.enforceInterface(DESCRIPTOR);
                    getMyHeadPic(com.suntek.mway.rcs.client.aidl.plugin.callback.IProfileListener.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case 5:
                    data.enforceInterface(DESCRIPTOR);
                    getHeadPicByContact(data.readLong(), com.suntek.mway.rcs.client.aidl.plugin.callback.IProfileListener.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case 6:
                    data.enforceInterface(DESCRIPTOR);
                    getHeadPicByNumber(data.readString(), data.readInt(), com.suntek.mway.rcs.client.aidl.plugin.callback.IProfileListener.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case 7:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg0 = (Profile) Profile.CREATOR.createFromParcel(data);
                    } else {
                        _arg0 = null;
                    }
                    refreshMyQRImg(_arg0, data.readInt() != 0, com.suntek.mway.rcs.client.aidl.plugin.callback.IProfileListener.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case 8:
                    data.enforceInterface(DESCRIPTOR);
                    updateContactsHeadPicAtFixedRateEveryDay(data.readString(), com.suntek.mway.rcs.client.aidl.plugin.callback.IProfileListener.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case 9:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getUpdateTimeOfContactsHeadPic();
                    reply.writeNoException();
                    reply.writeString(_result);
                    return true;
                case 10:
                    data.enforceInterface(DESCRIPTOR);
                    addSubscribe(data.readString());
                    reply.writeNoException();
                    return true;
                case 11:
                    data.enforceInterface(DESCRIPTOR);
                    cancelSubscribe(data.readString());
                    reply.writeNoException();
                    return true;
                case 12:
                    data.enforceInterface(DESCRIPTOR);
                    complainPublic(data.readString(), data.readString(), data.readString(), data.readInt(), data.readString());
                    reply.writeNoException();
                    return true;
                case 13:
                    data.enforceInterface(DESCRIPTOR);
                    getPreMessage(data.readString(), data.readString(), data.readInt(), data.readInt(), data.readInt());
                    reply.writeNoException();
                    return true;
                case 14:
                    data.enforceInterface(DESCRIPTOR);
                    getPublicDetail(data.readString());
                    reply.writeNoException();
                    return true;
                case 15:
                    data.enforceInterface(DESCRIPTOR);
                    getPublicList(data.readString(), data.readInt(), data.readInt(), data.readInt());
                    reply.writeNoException();
                    return true;
                case 16:
                    data.enforceInterface(DESCRIPTOR);
                    getPublicMenuInfo(data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getUserSubscribePublicList /*17*/:
                    data.enforceInterface(DESCRIPTOR);
                    getUserSubscribePublicList();
                    reply.writeNoException();
                    return true;
                case TRANSACTION_registerCallback /*18*/:
                    data.enforceInterface(DESCRIPTOR);
                    registerCallback(com.suntek.mway.rcs.client.aidl.plugin.callback.IPublicAccountCallbackAPI.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case TRANSACTION_unregisterCallback /*19*/:
                    data.enforceInterface(DESCRIPTOR);
                    unregisterCallback(com.suntek.mway.rcs.client.aidl.plugin.callback.IPublicAccountCallbackAPI.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getRecommendPublic /*20*/:
                    data.enforceInterface(DESCRIPTOR);
                    getRecommendPublic(data.readInt(), data.readInt(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_setAcceptStatus /*21*/:
                    data.enforceInterface(DESCRIPTOR);
                    setAcceptStatus(data.readString(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_doSync /*22*/:
                    data.enforceInterface(DESCRIPTOR);
                    doSync(data.readInt(), com.suntek.mway.rcs.client.aidl.plugin.callback.IContactSyncListener.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case TRANSACTION_startIntervalSync /*23*/:
                    data.enforceInterface(DESCRIPTOR);
                    startIntervalSync(data.readInt(), data.readInt(), data.readLong());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_cancelIntervalSync /*24*/:
                    data.enforceInterface(DESCRIPTOR);
                    cancelIntervalSync();
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getIntervalSyncMode /*25*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getIntervalSyncMode();
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_setEnableAutoSync /*26*/:
                    data.enforceInterface(DESCRIPTOR);
                    setEnableAutoSync(data.readInt() != 0, data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getEnableAutoSync /*27*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = getEnableAutoSync();
                    reply.writeNoException();
                    reply.writeInt(_result3 ? 1 : 0);
                    return true;
                case TRANSACTION_getAutoSync /*28*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getAutoSync();
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_setOnlySyncEnableViaWifi /*29*/:
                    data.enforceInterface(DESCRIPTOR);
                    setOnlySyncEnableViaWifi(data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getOnlySyncEnableViaWifi /*30*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = getOnlySyncEnableViaWifi();
                    reply.writeNoException();
                    reply.writeInt(_result3 ? 1 : 0);
                    return true;
                case TRANSACTION_getLocalContactCounts /*31*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getLocalContactCounts();
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case 32:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getRemoteContactCounts();
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_putFile /*33*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result4 = putFile(data.readString(), data.readString(), data.readInt());
                    reply.writeNoException();
                    reply.writeStrongBinder(_result4 != null ? _result4.asBinder() : null);
                    return true;
                case TRANSACTION_downloadFileFromUrl /*34*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result4 = downloadFileFromUrl(data.readString(), data.readString(), data.readInt(), data.readLong());
                    reply.writeNoException();
                    reply.writeStrongBinder(_result4 != null ? _result4.asBinder() : null);
                    return true;
                case TRANSACTION_shareFile /*35*/:
                    data.enforceInterface(DESCRIPTOR);
                    shareFile(data.readString(), data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getRemoteFileList /*36*/:
                    data.enforceInterface(DESCRIPTOR);
                    getRemoteFileList(data.readString(), data.readInt(), data.readInt(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getShareFileList /*37*/:
                    data.enforceInterface(DESCRIPTOR);
                    getShareFileList(data.readInt(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getLocalRootPath /*38*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getLocalRootPath();
                    reply.writeNoException();
                    reply.writeString(_result);
                    return true;
                case TRANSACTION_shareFileAndSend /*39*/:
                    data.enforceInterface(DESCRIPTOR);
                    shareFileAndSend(data.readString(), data.readString(), data.createStringArrayList(), data.readLong(), data.readString(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_shareFileAndSendGroup /*40*/:
                    data.enforceInterface(DESCRIPTOR);
                    shareFileAndSendGroup(data.readString(), data.readString(), data.readLong(), data.readLong());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_queryEmojiPackages /*41*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result5 = queryEmojiPackages();
                    reply.writeNoException();
                    reply.writeTypedList(_result5);
                    return true;
                case TRANSACTION_queryEmojiPackagesWithDetail /*42*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result5 = queryEmojiPackagesWithDetail();
                    reply.writeNoException();
                    reply.writeTypedList(_result5);
                    return true;
                case TRANSACTION_queryEmoticons /*43*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result6 = queryEmoticons(data.readString());
                    reply.writeNoException();
                    reply.writeTypedList(_result6);
                    return true;
                case TRANSACTION_queryEmoticonName /*44*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result6 = queryEmoticonName(data.readString());
                    reply.writeNoException();
                    reply.writeTypedList(_result6);
                    return true;
                case TRANSACTION_getEmojiPackage /*45*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result7 = getEmojiPackage(data.readString());
                    reply.writeNoException();
                    if (_result7 != null) {
                        reply.writeInt(1);
                        _result7.writeToParcel(reply, 1);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_getEmojiPackageWithDetail /*46*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result7 = getEmojiPackageWithDetail(data.readString());
                    reply.writeNoException();
                    if (_result7 != null) {
                        reply.writeInt(1);
                        _result7.writeToParcel(reply, 1);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_getEmoticon /*47*/:
                    data.enforceInterface(DESCRIPTOR);
                    EmoticonBO _result9 = getEmoticon(data.readString());
                    reply.writeNoException();
                    if (_result9 != null) {
                        reply.writeInt(1);
                        _result9.writeToParcel(reply, 1);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_isEmojiPackageExist /*48*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = isEmojiPackageExist(data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result3 ? 1 : 0);
                    return true;
                case TRANSACTION_isCanSend /*49*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = isCanSend(data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result3 ? 1 : 0);
                    return true;
                case TRANSACTION_decrypt2Bytes /*50*/:
                    data.enforceInterface(DESCRIPTOR);
                    byte[] _result10 = decrypt2Bytes(data.readString(), data.readInt());
                    reply.writeNoException();
                    reply.writeByteArray(_result10);
                    return true;
                case TRANSACTION_getStorageRootPath /*51*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getStorageRootPath();
                    reply.writeNoException();
                    reply.writeString(_result);
                    return true;
                case TRANSACTION_isEmojiStoreInstall /*52*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = isEmojiStoreInstall();
                    reply.writeNoException();
                    reply.writeInt(_result3 ? 1 : 0);
                    return true;
                case TRANSACTION_startEmojiStoreApp /*53*/:
                    data.enforceInterface(DESCRIPTOR);
                    startEmojiStoreApp();
                    reply.writeNoException();
                    return true;
                case TRANSACTION_downloadEmoticon /*54*/:
                    data.enforceInterface(DESCRIPTOR);
                    downloadEmoticon(data.readString(), data.readLong());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getRichScrnObj /*55*/:
                    data.enforceInterface(DESCRIPTOR);
                    ResultUtil _result11 = getRichScrnObj(data.readString(), data.readString());
                    reply.writeNoException();
                    if (_result11 != null) {
                        reply.writeInt(1);
                        _result11.writeToParcel(reply, 1);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_downloadRichScrnObj /*56*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result8 = downloadRichScrnObj(data.readString(), data.readString());
                    reply.writeNoException();
                    if (_result8 != null) {
                        reply.writeInt(1);
                        _result8.writeToParcel(reply, 1);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_clearRichScrnLocalCache /*57*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result8 = clearRichScrnLocalCache(data.readString());
                    reply.writeNoException();
                    if (_result8 != null) {
                        reply.writeInt(1);
                        _result8.writeToParcel(reply, 1);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_downloadHomeLocRules /*58*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result8 = downloadHomeLocRules(data.readString());
                    reply.writeNoException();
                    if (_result8 != null) {
                        reply.writeInt(1);
                        _result8.writeToParcel(reply, 1);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_richScrnChangeNetWork /*59*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result8 = richScrnChangeNetWork();
                    reply.writeNoException();
                    if (_result8 != null) {
                        reply.writeInt(1);
                        _result8.writeToParcel(reply, 1);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case 60:
                    data.enforceInterface(DESCRIPTOR);
                    _result8 = collectRichScrnObj(data.readString(), data.readString());
                    reply.writeNoException();
                    if (_result8 != null) {
                        reply.writeInt(1);
                        _result8.writeToParcel(reply, 1);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_startRichScreenApp /*61*/:
                    data.enforceInterface(DESCRIPTOR);
                    startRichScreenApp(data.createStringArrayList());
                    reply.writeNoException();
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void addSubscribe(String str) throws RemoteException;

    void cancelIntervalSync() throws RemoteException;

    void cancelSubscribe(String str) throws RemoteException;

    ResultInfo clearRichScrnLocalCache(String str) throws RemoteException;

    ResultInfo collectRichScrnObj(String str, String str2) throws RemoteException;

    void complainPublic(String str, String str2, String str3, int i, String str4) throws RemoteException;

    byte[] decrypt2Bytes(String str, int i) throws RemoteException;

    void doSync(int i, IContactSyncListener iContactSyncListener) throws RemoteException;

    void downloadEmoticon(String str, long j) throws RemoteException;

    ICloudOperationCtrl downloadFileFromUrl(String str, String str2, int i, long j) throws RemoteException;

    ResultInfo downloadHomeLocRules(String str) throws RemoteException;

    ResultInfo downloadRichScrnObj(String str, String str2) throws RemoteException;

    int getAutoSync() throws RemoteException;

    EmojiPackageBO getEmojiPackage(String str) throws RemoteException;

    EmojiPackageBO getEmojiPackageWithDetail(String str) throws RemoteException;

    EmoticonBO getEmoticon(String str) throws RemoteException;

    boolean getEnableAutoSync() throws RemoteException;

    void getHeadPicByContact(long j, IProfileListener iProfileListener) throws RemoteException;

    void getHeadPicByNumber(String str, int i, IProfileListener iProfileListener) throws RemoteException;

    int getIntervalSyncMode() throws RemoteException;

    int getLocalContactCounts() throws RemoteException;

    String getLocalRootPath() throws RemoteException;

    void getMyHeadPic(IProfileListener iProfileListener) throws RemoteException;

    void getMyProfile(IProfileListener iProfileListener) throws RemoteException;

    boolean getOnlySyncEnableViaWifi() throws RemoteException;

    void getPreMessage(String str, String str2, int i, int i2, int i3) throws RemoteException;

    void getPublicDetail(String str) throws RemoteException;

    void getPublicList(String str, int i, int i2, int i3) throws RemoteException;

    void getPublicMenuInfo(String str) throws RemoteException;

    void getRecommendPublic(int i, int i2, int i3) throws RemoteException;

    int getRemoteContactCounts() throws RemoteException;

    void getRemoteFileList(String str, int i, int i2, int i3) throws RemoteException;

    ResultUtil getRichScrnObj(String str, String str2) throws RemoteException;

    void getShareFileList(int i, int i2) throws RemoteException;

    String getStorageRootPath() throws RemoteException;

    String getUpdateTimeOfContactsHeadPic() throws RemoteException;

    void getUserSubscribePublicList() throws RemoteException;

    boolean isCanSend(String str) throws RemoteException;

    boolean isEmojiPackageExist(String str) throws RemoteException;

    boolean isEmojiStoreInstall() throws RemoteException;

    ICloudOperationCtrl putFile(String str, String str2, int i) throws RemoteException;

    List<EmojiPackageBO> queryEmojiPackages() throws RemoteException;

    List<EmojiPackageBO> queryEmojiPackagesWithDetail() throws RemoteException;

    List<EmoticonBO> queryEmoticonName(String str) throws RemoteException;

    List<EmoticonBO> queryEmoticons(String str) throws RemoteException;

    void refreshMyQRImg(Profile profile, boolean z, IProfileListener iProfileListener) throws RemoteException;

    void registerCallback(IPublicAccountCallbackAPI iPublicAccountCallbackAPI) throws RemoteException;

    ResultInfo richScrnChangeNetWork() throws RemoteException;

    void setAcceptStatus(String str, int i) throws RemoteException;

    void setEnableAutoSync(boolean z, int i) throws RemoteException;

    void setMyHeadPic(Avatar avatar, IProfileListener iProfileListener) throws RemoteException;

    void setMyProfile(Profile profile, IProfileListener iProfileListener) throws RemoteException;

    void setOnlySyncEnableViaWifi(boolean z) throws RemoteException;

    void shareFile(String str, String str2) throws RemoteException;

    void shareFileAndSend(String str, String str2, List<String> list, long j, String str3, int i) throws RemoteException;

    void shareFileAndSendGroup(String str, String str2, long j, long j2) throws RemoteException;

    void startEmojiStoreApp() throws RemoteException;

    void startIntervalSync(int i, int i2, long j) throws RemoteException;

    void startRichScreenApp(List<String> list) throws RemoteException;

    void unregisterCallback(IPublicAccountCallbackAPI iPublicAccountCallbackAPI) throws RemoteException;

    void updateContactsHeadPicAtFixedRateEveryDay(String str, IProfileListener iProfileListener) throws RemoteException;
}
