package com.suntek.mway.rcs.client.aidl.service;

import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.suntek.mway.rcs.client.aidl.service.callback.ICapabiltyListener;
import com.suntek.mway.rcs.client.aidl.service.callback.IGroupChatCallback;
import com.suntek.mway.rcs.client.aidl.service.entity.GroupChat;
import com.suntek.mway.rcs.client.aidl.service.entity.GroupChatMember;
import com.suntek.mway.rcs.client.aidl.service.entity.SimpleMessage;
import java.util.List;

public interface IServiceApi extends IInterface {

    public static abstract class Stub extends Binder implements IServiceApi {
        private static final String DESCRIPTOR = "com.suntek.mway.rcs.client.aidl.service.IServiceApi";
        static final int TRANSACTION_acceptToJoin = 87;
        static final int TRANSACTION_addBlacklist = 71;
        static final int TRANSACTION_addSsn = 64;
        static final int TRANSACTION_assignChairman = 89;
        static final int TRANSACTION_backUpFavouriteAll = 14;
        static final int TRANSACTION_backup = 15;
        static final int TRANSACTION_backupAll = 13;
        static final int TRANSACTION_burn = 17;
        static final int TRANSACTION_burnAll = 16;
        static final int TRANSACTION_cancelBackup = 18;
        static final int TRANSACTION_cancelCollect = 19;
        static final int TRANSACTION_cancelTopConversation = 20;
        static final int TRANSACTION_clearBlacklist = 73;
        static final int TRANSACTION_collect = 21;
        static final int TRANSACTION_complain = 12;
        static final int TRANSACTION_create = 86;
        static final int TRANSACTION_deleteAllGroupChat = 99;
        static final int TRANSACTION_deleteAllMessage = 34;
        static final int TRANSACTION_deleteAllSsn = 70;
        static final int TRANSACTION_deleteBlacklist = 75;
        static final int TRANSACTION_deleteGroupChat = 98;
        static final int TRANSACTION_deleteMessage = 36;
        static final int TRANSACTION_deleteMessageByThreadId = 35;
        static final int TRANSACTION_deleteSsn = 69;
        static final int TRANSACTION_deleteSsnPrefix = 66;
        static final int TRANSACTION_disableSsn = 65;
        static final int TRANSACTION_disband = 90;
        static final int TRANSACTION_download = 11;
        static final int TRANSACTION_enableSsn = 68;
        static final int TRANSACTION_forward = 22;
        static final int TRANSACTION_forwardToGroupChat = 23;
        static final int TRANSACTION_getAccount = 8;
        static final int TRANSACTION_getAllGroupChat = 78;
        static final int TRANSACTION_getAudioMaxDuration = 25;
        static final int TRANSACTION_getBlacklist = 74;
        static final int TRANSACTION_getCapability = 77;
        static final int TRANSACTION_getConfiguration = 6;
        static final int TRANSACTION_getConfigurationWithOtp = 7;
        static final int TRANSACTION_getGroupChatById = 79;
        static final int TRANSACTION_getGroupChatByThreadId = 80;
        static final int TRANSACTION_getImageMaxSize = 26;
        static final int TRANSACTION_getMaxAdhocGroupSize = 102;
        static final int TRANSACTION_getMember = 81;
        static final int TRANSACTION_getMemberAvatar = 85;
        static final int TRANSACTION_getMemberAvatarFromServer = 84;
        static final int TRANSACTION_getMembers = 82;
        static final int TRANSACTION_getMembersAllowChairman = 83;
        static final int TRANSACTION_getMyGroupChat = 100;
        static final int TRANSACTION_getRemindPolicy = 29;
        static final int TRANSACTION_getSendPolicy = 30;
        static final int TRANSACTION_getSsnList = 67;
        static final int TRANSACTION_getThreadId = 24;
        static final int TRANSACTION_getVideoMaxDuration = 27;
        static final int TRANSACTION_getVideoMaxSize = 28;
        static final int TRANSACTION_invite = 91;
        static final int TRANSACTION_isBlacklist = 72;
        static final int TRANSACTION_isOnline = 9;
        static final int TRANSACTION_kickOut = 92;
        static final int TRANSACTION_login = 1;
        static final int TRANSACTION_logout = 2;
        static final int TRANSACTION_markMessageAsReaded = 63;
        static final int TRANSACTION_openAccount = 4;
        static final int TRANSACTION_pauseDownload = 31;
        static final int TRANSACTION_quit = 93;
        static final int TRANSACTION_recoverBlockedMessage = 32;
        static final int TRANSACTION_recoverBlockedMessageByThreadId = 33;
        static final int TRANSACTION_rejectOpenAccount = 5;
        static final int TRANSACTION_rejectToJoin = 88;
        static final int TRANSACTION_rejoin = 101;
        static final int TRANSACTION_resend = 41;
        static final int TRANSACTION_restoreAll = 37;
        static final int TRANSACTION_restoreAllFavourite = 38;
        static final int TRANSACTION_sendAudio = 44;
        static final int TRANSACTION_sendAudioToGroupChat = 50;
        static final int TRANSACTION_sendAudioToPc = 56;
        static final int TRANSACTION_sendAudioToPublicAccount = 111;
        static final int TRANSACTION_sendCloud = 106;
        static final int TRANSACTION_sendCloudToGroupChat = 107;
        static final int TRANSACTION_sendCloudToPc = 108;
        static final int TRANSACTION_sendCommandToPublicAccount = 115;
        static final int TRANSACTION_sendEmoticon = 103;
        static final int TRANSACTION_sendEmoticonToGroupChat = 104;
        static final int TRANSACTION_sendEmoticonToPc = 105;
        static final int TRANSACTION_sendImage = 43;
        static final int TRANSACTION_sendImageToGroupChat = 49;
        static final int TRANSACTION_sendImageToPc = 55;
        static final int TRANSACTION_sendImageToPublicAccount = 110;
        static final int TRANSACTION_sendLocation = 46;
        static final int TRANSACTION_sendLocationToGroupChat = 52;
        static final int TRANSACTION_sendLocationToPc = 58;
        static final int TRANSACTION_sendLocationToPublicAccount = 113;
        static final int TRANSACTION_sendText = 42;
        static final int TRANSACTION_sendTextToGroupChat = 48;
        static final int TRANSACTION_sendTextToPc = 54;
        static final int TRANSACTION_sendTextToPublicAccount = 109;
        static final int TRANSACTION_sendVcard = 47;
        static final int TRANSACTION_sendVcardToGroupChat = 53;
        static final int TRANSACTION_sendVcardToPc = 59;
        static final int TRANSACTION_sendVcardToPublicAccount = 114;
        static final int TRANSACTION_sendVideo = 45;
        static final int TRANSACTION_sendVideoToGroupChat = 51;
        static final int TRANSACTION_sendVideoToPc = 57;
        static final int TRANSACTION_sendVideoToPublicAccount = 112;
        static final int TRANSACTION_setBlacklistProvider = 76;
        static final int TRANSACTION_setGroupChatRemindPolicy = 97;
        static final int TRANSACTION_setMyAlias = 94;
        static final int TRANSACTION_setRemarks = 96;
        static final int TRANSACTION_setRemindPolicy = 60;
        static final int TRANSACTION_setSendPolicy = 61;
        static final int TRANSACTION_setSubject = 95;
        static final int TRANSACTION_startComposing = 39;
        static final int TRANSACTION_startPluginCenter = 3;
        static final int TRANSACTION_startService = 10;
        static final int TRANSACTION_stopComposing = 40;
        static final int TRANSACTION_topConversation = 62;

        private static class Proxy implements IServiceApi {
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

            public void login(String account) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(account);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void logout() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void startPluginCenter() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void openAccount() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void rejectOpenAccount() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void getConfiguration() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void getConfigurationWithOtp(String otpCode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(otpCode);
                    this.mRemote.transact(7, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String getAccount() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(8, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isOnline() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(9, _data, _reply, 0);
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

            public void startService() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(10, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void download(long id) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(id);
                    this.mRemote.transact(11, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void complain(long id) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(id);
                    this.mRemote.transact(12, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void backupAll() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(13, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void backUpFavouriteAll() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(14, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void backup(List<SimpleMessage> simpleMessageList) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeTypedList(simpleMessageList);
                    this.mRemote.transact(15, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void burnAll() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(16, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void burn(long id, int delaySeconds) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(id);
                    _data.writeInt(delaySeconds);
                    this.mRemote.transact(Stub.TRANSACTION_burn, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void cancelBackup() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_cancelBackup, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void cancelCollect(List<SimpleMessage> simpleMessageList) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeTypedList(simpleMessageList);
                    this.mRemote.transact(Stub.TRANSACTION_cancelCollect, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void cancelTopConversation(long threadId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(threadId);
                    this.mRemote.transact(Stub.TRANSACTION_cancelTopConversation, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void collect(List<SimpleMessage> simpleMessageList) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeTypedList(simpleMessageList);
                    this.mRemote.transact(Stub.TRANSACTION_collect, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public long forward(long id, long threadId, List<String> numberList, int barCycle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(id);
                    _data.writeLong(threadId);
                    _data.writeStringList(numberList);
                    _data.writeInt(barCycle);
                    this.mRemote.transact(Stub.TRANSACTION_forward, _data, _reply, 0);
                    _reply.readException();
                    long _result = _reply.readLong();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public long forwardToGroupChat(long id, long threadId, long groupId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(id);
                    _data.writeLong(threadId);
                    _data.writeLong(groupId);
                    this.mRemote.transact(Stub.TRANSACTION_forwardToGroupChat, _data, _reply, 0);
                    _reply.readException();
                    long _result = _reply.readLong();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public long getThreadId(List<String> numberList) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringList(numberList);
                    this.mRemote.transact(Stub.TRANSACTION_getThreadId, _data, _reply, 0);
                    _reply.readException();
                    long _result = _reply.readLong();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getAudioMaxDuration() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getAudioMaxDuration, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public long getImageMaxSize() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getImageMaxSize, _data, _reply, 0);
                    _reply.readException();
                    long _result = _reply.readLong();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getVideoMaxDuration() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getVideoMaxDuration, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public long getVideoMaxSize() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getVideoMaxSize, _data, _reply, 0);
                    _reply.readException();
                    long _result = _reply.readLong();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getRemindPolicy() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getRemindPolicy, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getSendPolicy() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getSendPolicy, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void pauseDownload(long id) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(id);
                    this.mRemote.transact(Stub.TRANSACTION_pauseDownload, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int recoverBlockedMessage(long blockedMessageId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(blockedMessageId);
                    this.mRemote.transact(32, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int recoverBlockedMessageByThreadId(long threadId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(threadId);
                    this.mRemote.transact(Stub.TRANSACTION_recoverBlockedMessageByThreadId, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int deleteAllMessage() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_deleteAllMessage, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int deleteMessageByThreadId(long threadId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(threadId);
                    this.mRemote.transact(Stub.TRANSACTION_deleteMessageByThreadId, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int deleteMessage(long id) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(id);
                    this.mRemote.transact(Stub.TRANSACTION_deleteMessage, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void restoreAll() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_restoreAll, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void restoreAllFavourite() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_restoreAllFavourite, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void startComposing(long threadId, String number, String contentType, int seconds) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(threadId);
                    _data.writeString(number);
                    _data.writeString(contentType);
                    _data.writeInt(seconds);
                    this.mRemote.transact(Stub.TRANSACTION_startComposing, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void stopComposing(long threadId, String number, String contentType, long lastActive) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(threadId);
                    _data.writeString(number);
                    _data.writeString(contentType);
                    _data.writeLong(lastActive);
                    this.mRemote.transact(Stub.TRANSACTION_stopComposing, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void resend(long id) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(id);
                    this.mRemote.transact(Stub.TRANSACTION_resend, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public long sendText(List<String> numberList, long threadId, String text, int barCycle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringList(numberList);
                    _data.writeLong(threadId);
                    _data.writeString(text);
                    _data.writeInt(barCycle);
                    this.mRemote.transact(Stub.TRANSACTION_sendText, _data, _reply, 0);
                    _reply.readException();
                    long _result = _reply.readLong();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public long sendImage(List<String> numberList, long threadId, String filepath, int quality, boolean isRecord, int barCycle, String thumbnailPath) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringList(numberList);
                    _data.writeLong(threadId);
                    _data.writeString(filepath);
                    _data.writeInt(quality);
                    if (isRecord) {
                        i = 1;
                    }
                    _data.writeInt(i);
                    _data.writeInt(barCycle);
                    _data.writeString(thumbnailPath);
                    this.mRemote.transact(Stub.TRANSACTION_sendImage, _data, _reply, 0);
                    _reply.readException();
                    long _result = _reply.readLong();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public long sendAudio(List<String> numberList, long threadId, String filepath, int duration, boolean isRecord, int barCycle) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringList(numberList);
                    _data.writeLong(threadId);
                    _data.writeString(filepath);
                    _data.writeInt(duration);
                    if (isRecord) {
                        i = 1;
                    }
                    _data.writeInt(i);
                    _data.writeInt(barCycle);
                    this.mRemote.transact(Stub.TRANSACTION_sendAudio, _data, _reply, 0);
                    _reply.readException();
                    long _result = _reply.readLong();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public long sendVideo(List<String> numberList, long threadId, String filepath, int duration, boolean isRecord, int barCycle, String thumbnailPath) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringList(numberList);
                    _data.writeLong(threadId);
                    _data.writeString(filepath);
                    _data.writeInt(duration);
                    if (isRecord) {
                        i = 1;
                    }
                    _data.writeInt(i);
                    _data.writeInt(barCycle);
                    _data.writeString(thumbnailPath);
                    this.mRemote.transact(Stub.TRANSACTION_sendVideo, _data, _reply, 0);
                    _reply.readException();
                    long _result = _reply.readLong();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public long sendLocation(List<String> numberList, long threadId, double lat, double lng, String label, int barCycle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringList(numberList);
                    _data.writeLong(threadId);
                    _data.writeDouble(lat);
                    _data.writeDouble(lng);
                    _data.writeString(label);
                    _data.writeInt(barCycle);
                    this.mRemote.transact(Stub.TRANSACTION_sendLocation, _data, _reply, 0);
                    _reply.readException();
                    long _result = _reply.readLong();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public long sendVcard(List<String> numberList, long threadId, String filepath, int barCycle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringList(numberList);
                    _data.writeLong(threadId);
                    _data.writeString(filepath);
                    _data.writeInt(barCycle);
                    this.mRemote.transact(Stub.TRANSACTION_sendVcard, _data, _reply, 0);
                    _reply.readException();
                    long _result = _reply.readLong();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public long sendTextToGroupChat(long groupId, long threadId, String text) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(groupId);
                    _data.writeLong(threadId);
                    _data.writeString(text);
                    this.mRemote.transact(Stub.TRANSACTION_sendTextToGroupChat, _data, _reply, 0);
                    _reply.readException();
                    long _result = _reply.readLong();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public long sendImageToGroupChat(long groupId, long threadId, String filepath, int quality, boolean isRecord, String thumbnailPath) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(groupId);
                    _data.writeLong(threadId);
                    _data.writeString(filepath);
                    _data.writeInt(quality);
                    if (isRecord) {
                        i = 1;
                    }
                    _data.writeInt(i);
                    _data.writeString(thumbnailPath);
                    this.mRemote.transact(Stub.TRANSACTION_sendImageToGroupChat, _data, _reply, 0);
                    _reply.readException();
                    long _result = _reply.readLong();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public long sendAudioToGroupChat(long groupId, long threadId, String filepath, int duration, boolean isRecord) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(groupId);
                    _data.writeLong(threadId);
                    _data.writeString(filepath);
                    _data.writeInt(duration);
                    if (isRecord) {
                        i = 1;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_sendAudioToGroupChat, _data, _reply, 0);
                    _reply.readException();
                    long _result = _reply.readLong();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public long sendVideoToGroupChat(long groupId, long threadId, String filepath, int duration, boolean isRecord, String thumbnailPath) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(groupId);
                    _data.writeLong(threadId);
                    _data.writeString(filepath);
                    _data.writeInt(duration);
                    if (isRecord) {
                        i = 1;
                    }
                    _data.writeInt(i);
                    _data.writeString(thumbnailPath);
                    this.mRemote.transact(Stub.TRANSACTION_sendVideoToGroupChat, _data, _reply, 0);
                    _reply.readException();
                    long _result = _reply.readLong();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public long sendLocationToGroupChat(long groupId, long threadId, double lat, double lng, String label) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(groupId);
                    _data.writeLong(threadId);
                    _data.writeDouble(lat);
                    _data.writeDouble(lng);
                    _data.writeString(label);
                    this.mRemote.transact(Stub.TRANSACTION_sendLocationToGroupChat, _data, _reply, 0);
                    _reply.readException();
                    long _result = _reply.readLong();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public long sendVcardToGroupChat(long groupId, long threadId, String filepath) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(groupId);
                    _data.writeLong(threadId);
                    _data.writeString(filepath);
                    this.mRemote.transact(Stub.TRANSACTION_sendVcardToGroupChat, _data, _reply, 0);
                    _reply.readException();
                    long _result = _reply.readLong();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public long sendTextToPc(long threadId, String text, int barCycle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(threadId);
                    _data.writeString(text);
                    _data.writeInt(barCycle);
                    this.mRemote.transact(Stub.TRANSACTION_sendTextToPc, _data, _reply, 0);
                    _reply.readException();
                    long _result = _reply.readLong();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public long sendImageToPc(long threadId, String filepath, int quality, boolean isRecord, int barCycle, String thumbnailPath) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(threadId);
                    _data.writeString(filepath);
                    _data.writeInt(quality);
                    if (isRecord) {
                        i = 1;
                    }
                    _data.writeInt(i);
                    _data.writeInt(barCycle);
                    _data.writeString(thumbnailPath);
                    this.mRemote.transact(Stub.TRANSACTION_sendImageToPc, _data, _reply, 0);
                    _reply.readException();
                    long _result = _reply.readLong();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public long sendAudioToPc(long threadId, String filepath, int duration, boolean isRecord, int barCycle) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(threadId);
                    _data.writeString(filepath);
                    _data.writeInt(duration);
                    if (isRecord) {
                        i = 1;
                    }
                    _data.writeInt(i);
                    _data.writeInt(barCycle);
                    this.mRemote.transact(Stub.TRANSACTION_sendAudioToPc, _data, _reply, 0);
                    _reply.readException();
                    long _result = _reply.readLong();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public long sendVideoToPc(long threadId, String filepath, int duration, boolean isRecord, int barCycle, String thumbnailPath) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(threadId);
                    _data.writeString(filepath);
                    _data.writeInt(duration);
                    if (isRecord) {
                        i = 1;
                    }
                    _data.writeInt(i);
                    _data.writeInt(barCycle);
                    _data.writeString(thumbnailPath);
                    this.mRemote.transact(Stub.TRANSACTION_sendVideoToPc, _data, _reply, 0);
                    _reply.readException();
                    long _result = _reply.readLong();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public long sendLocationToPc(long threadId, double lat, double lng, String label, int barCycle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(threadId);
                    _data.writeDouble(lat);
                    _data.writeDouble(lng);
                    _data.writeString(label);
                    _data.writeInt(barCycle);
                    this.mRemote.transact(Stub.TRANSACTION_sendLocationToPc, _data, _reply, 0);
                    _reply.readException();
                    long _result = _reply.readLong();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public long sendVcardToPc(long threadId, String filepath, int barCycle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(threadId);
                    _data.writeString(filepath);
                    _data.writeInt(barCycle);
                    this.mRemote.transact(Stub.TRANSACTION_sendVcardToPc, _data, _reply, 0);
                    _reply.readException();
                    long _result = _reply.readLong();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setRemindPolicy(int policy) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(policy);
                    this.mRemote.transact(60, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setSendPolicy(int policy) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(policy);
                    this.mRemote.transact(Stub.TRANSACTION_setSendPolicy, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int topConversation(long threadId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(threadId);
                    this.mRemote.transact(Stub.TRANSACTION_topConversation, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int markMessageAsReaded(long id) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(id);
                    this.mRemote.transact(Stub.TRANSACTION_markMessageAsReaded, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean addSsn(String number) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(number);
                    this.mRemote.transact(64, _data, _reply, 0);
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

            public boolean disableSsn() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_disableSsn, _data, _reply, 0);
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

            public String deleteSsnPrefix(String number) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(number);
                    this.mRemote.transact(Stub.TRANSACTION_deleteSsnPrefix, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<String> getSsnList() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getSsnList, _data, _reply, 0);
                    _reply.readException();
                    List<String> _result = _reply.createStringArrayList();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean enableSsn() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_enableSsn, _data, _reply, 0);
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

            public boolean deleteSsn(String number) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(number);
                    this.mRemote.transact(Stub.TRANSACTION_deleteSsn, _data, _reply, 0);
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

            public boolean deleteAllSsn() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_deleteAllSsn, _data, _reply, 0);
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

            public boolean addBlacklist(String number, String name) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(number);
                    _data.writeString(name);
                    this.mRemote.transact(Stub.TRANSACTION_addBlacklist, _data, _reply, 0);
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

            public boolean isBlacklist(String number) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(number);
                    this.mRemote.transact(Stub.TRANSACTION_isBlacklist, _data, _reply, 0);
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

            public void clearBlacklist() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_clearBlacklist, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<String> getBlacklist() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getBlacklist, _data, _reply, 0);
                    _reply.readException();
                    List<String> _result = _reply.createStringArrayList();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean deleteBlacklist(String number) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(number);
                    this.mRemote.transact(Stub.TRANSACTION_deleteBlacklist, _data, _reply, 0);
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

            public void setBlacklistProvider(Uri uri) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (uri != null) {
                        _data.writeInt(1);
                        uri.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_setBlacklistProvider, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void getCapability(String number, boolean fromServer, ICapabiltyListener listener) throws RemoteException {
                IBinder iBinder = null;
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(number);
                    if (fromServer) {
                        i = 1;
                    }
                    _data.writeInt(i);
                    if (listener != null) {
                        iBinder = listener.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_getCapability, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<GroupChat> getAllGroupChat() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getAllGroupChat, _data, _reply, 0);
                    _reply.readException();
                    List<GroupChat> _result = _reply.createTypedArrayList(GroupChat.CREATOR);
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public GroupChat getGroupChatById(long groupChatId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    GroupChat _result;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(groupChatId);
                    this.mRemote.transact(Stub.TRANSACTION_getGroupChatById, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (GroupChat) GroupChat.CREATOR.createFromParcel(_reply);
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

            public GroupChat getGroupChatByThreadId(long threadId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    GroupChat _result;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(threadId);
                    this.mRemote.transact(Stub.TRANSACTION_getGroupChatByThreadId, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (GroupChat) GroupChat.CREATOR.createFromParcel(_reply);
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

            public GroupChatMember getMember(long groupChatId, String number) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    GroupChatMember _result;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(groupChatId);
                    _data.writeString(number);
                    this.mRemote.transact(Stub.TRANSACTION_getMember, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (GroupChatMember) GroupChatMember.CREATOR.createFromParcel(_reply);
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

            public List<GroupChatMember> getMembers(long groupChatId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(groupChatId);
                    this.mRemote.transact(Stub.TRANSACTION_getMembers, _data, _reply, 0);
                    _reply.readException();
                    List<GroupChatMember> _result = _reply.createTypedArrayList(GroupChatMember.CREATOR);
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<GroupChatMember> getMembersAllowChairman(long groupChatId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(groupChatId);
                    this.mRemote.transact(Stub.TRANSACTION_getMembersAllowChairman, _data, _reply, 0);
                    _reply.readException();
                    List<GroupChatMember> _result = _reply.createTypedArrayList(GroupChatMember.CREATOR);
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void getMemberAvatarFromServer(long groupChatId, String number, int pixel, IGroupChatCallback callback) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(groupChatId);
                    _data.writeString(number);
                    _data.writeInt(pixel);
                    if (callback != null) {
                        iBinder = callback.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_getMemberAvatarFromServer, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void getMemberAvatar(long groupChatId, String number, int pixel, IGroupChatCallback callback) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(groupChatId);
                    _data.writeString(number);
                    _data.writeInt(pixel);
                    if (callback != null) {
                        iBinder = callback.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_getMemberAvatar, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public long create(String subject, List<String> users) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(subject);
                    _data.writeStringList(users);
                    this.mRemote.transact(Stub.TRANSACTION_create, _data, _reply, 0);
                    _reply.readException();
                    long _result = _reply.readLong();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int acceptToJoin(long groupChatId, String inviteNumber) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(groupChatId);
                    _data.writeString(inviteNumber);
                    this.mRemote.transact(Stub.TRANSACTION_acceptToJoin, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int rejectToJoin(long groupChatId, String inviteNumber) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(groupChatId);
                    _data.writeString(inviteNumber);
                    this.mRemote.transact(Stub.TRANSACTION_rejectToJoin, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int assignChairman(long groupChatId, String number) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(groupChatId);
                    _data.writeString(number);
                    this.mRemote.transact(Stub.TRANSACTION_assignChairman, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int disband(long groupChatId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(groupChatId);
                    this.mRemote.transact(Stub.TRANSACTION_disband, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int invite(long groupChatId, List<String> numberList) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(groupChatId);
                    _data.writeStringList(numberList);
                    this.mRemote.transact(Stub.TRANSACTION_invite, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int kickOut(long groupChatId, String number) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(groupChatId);
                    _data.writeString(number);
                    this.mRemote.transact(Stub.TRANSACTION_kickOut, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int quit(long groupChatId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(groupChatId);
                    this.mRemote.transact(Stub.TRANSACTION_quit, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int setMyAlias(long groupChatId, String alias) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(groupChatId);
                    _data.writeString(alias);
                    this.mRemote.transact(Stub.TRANSACTION_setMyAlias, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int setSubject(long groupChatId, String subject) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(groupChatId);
                    _data.writeString(subject);
                    this.mRemote.transact(Stub.TRANSACTION_setSubject, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int setRemarks(long groupChatId, String remarks) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(groupChatId);
                    _data.writeString(remarks);
                    this.mRemote.transact(Stub.TRANSACTION_setRemarks, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setGroupChatRemindPolicy(long groupChatId, int policy) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(groupChatId);
                    _data.writeInt(policy);
                    this.mRemote.transact(Stub.TRANSACTION_setGroupChatRemindPolicy, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int deleteGroupChat(long[] threadIds) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLongArray(threadIds);
                    this.mRemote.transact(Stub.TRANSACTION_deleteGroupChat, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int deleteAllGroupChat() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_deleteAllGroupChat, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getMyGroupChat() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(100, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int rejoin(long groupChatId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(groupChatId);
                    this.mRemote.transact(Stub.TRANSACTION_rejoin, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getMaxAdhocGroupSize() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getMaxAdhocGroupSize, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public long sendEmoticon(List<String> numberList, long threadId, String emoticonId, String emoticonName, int barCycle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringList(numberList);
                    _data.writeLong(threadId);
                    _data.writeString(emoticonId);
                    _data.writeString(emoticonName);
                    _data.writeInt(barCycle);
                    this.mRemote.transact(Stub.TRANSACTION_sendEmoticon, _data, _reply, 0);
                    _reply.readException();
                    long _result = _reply.readLong();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public long sendEmoticonToGroupChat(long groupId, long threadId, String emoticonId, String emoticonName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(groupId);
                    _data.writeLong(threadId);
                    _data.writeString(emoticonId);
                    _data.writeString(emoticonName);
                    this.mRemote.transact(Stub.TRANSACTION_sendEmoticonToGroupChat, _data, _reply, 0);
                    _reply.readException();
                    long _result = _reply.readLong();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public long sendEmoticonToPc(long threadId, String emoticonId, String emoticonName, int barCycle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(threadId);
                    _data.writeString(emoticonId);
                    _data.writeString(emoticonName);
                    _data.writeInt(barCycle);
                    this.mRemote.transact(Stub.TRANSACTION_sendEmoticonToPc, _data, _reply, 0);
                    _reply.readException();
                    long _result = _reply.readLong();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public long sendCloud(List<String> numberList, long threadId, String fileName, long fileSize, String shareUrl, String smsContent, int barCycle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringList(numberList);
                    _data.writeLong(threadId);
                    _data.writeString(fileName);
                    _data.writeLong(fileSize);
                    _data.writeString(shareUrl);
                    _data.writeString(smsContent);
                    _data.writeInt(barCycle);
                    this.mRemote.transact(Stub.TRANSACTION_sendCloud, _data, _reply, 0);
                    _reply.readException();
                    long _result = _reply.readLong();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public long sendCloudToGroupChat(long groupId, long threadId, String fileName, long fileSize, String shareUrl) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(groupId);
                    _data.writeLong(threadId);
                    _data.writeString(fileName);
                    _data.writeLong(fileSize);
                    _data.writeString(shareUrl);
                    this.mRemote.transact(Stub.TRANSACTION_sendCloudToGroupChat, _data, _reply, 0);
                    _reply.readException();
                    long _result = _reply.readLong();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public long sendCloudToPc(long threadId, String fileName, long fileSize, String shareUrl, String smsContent, int barCycle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(threadId);
                    _data.writeString(fileName);
                    _data.writeLong(fileSize);
                    _data.writeString(shareUrl);
                    _data.writeString(smsContent);
                    _data.writeInt(barCycle);
                    this.mRemote.transact(Stub.TRANSACTION_sendCloudToPc, _data, _reply, 0);
                    _reply.readException();
                    long _result = _reply.readLong();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public long sendTextToPublicAccount(String publicAccountId, long threadId, String text) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(publicAccountId);
                    _data.writeLong(threadId);
                    _data.writeString(text);
                    this.mRemote.transact(Stub.TRANSACTION_sendTextToPublicAccount, _data, _reply, 0);
                    _reply.readException();
                    long _result = _reply.readLong();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public long sendImageToPublicAccount(String publicAccountId, long threadId, String filepath, int quality, boolean isRecord, String thumbnailPath) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(publicAccountId);
                    _data.writeLong(threadId);
                    _data.writeString(filepath);
                    _data.writeInt(quality);
                    if (isRecord) {
                        i = 1;
                    }
                    _data.writeInt(i);
                    _data.writeString(thumbnailPath);
                    this.mRemote.transact(Stub.TRANSACTION_sendImageToPublicAccount, _data, _reply, 0);
                    _reply.readException();
                    long _result = _reply.readLong();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public long sendAudioToPublicAccount(String publicAccountId, long threadId, String filepath, int duration, boolean isRecord) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(publicAccountId);
                    _data.writeLong(threadId);
                    _data.writeString(filepath);
                    _data.writeInt(duration);
                    if (isRecord) {
                        i = 1;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_sendAudioToPublicAccount, _data, _reply, 0);
                    _reply.readException();
                    long _result = _reply.readLong();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public long sendVideoToPublicAccount(String publicAccountId, long threadId, String filepath, int duration, boolean isRecord, String thumbnailPath) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(publicAccountId);
                    _data.writeLong(threadId);
                    _data.writeString(filepath);
                    _data.writeInt(duration);
                    if (isRecord) {
                        i = 1;
                    }
                    _data.writeInt(i);
                    _data.writeString(thumbnailPath);
                    this.mRemote.transact(Stub.TRANSACTION_sendVideoToPublicAccount, _data, _reply, 0);
                    _reply.readException();
                    long _result = _reply.readLong();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public long sendLocationToPublicAccount(String publicAccountId, long threadId, double lat, double lng, String label) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(publicAccountId);
                    _data.writeLong(threadId);
                    _data.writeDouble(lat);
                    _data.writeDouble(lng);
                    _data.writeString(label);
                    this.mRemote.transact(Stub.TRANSACTION_sendLocationToPublicAccount, _data, _reply, 0);
                    _reply.readException();
                    long _result = _reply.readLong();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public long sendVcardToPublicAccount(String publicAccountId, long threadId, String filepath) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(publicAccountId);
                    _data.writeLong(threadId);
                    _data.writeString(filepath);
                    this.mRemote.transact(Stub.TRANSACTION_sendVcardToPublicAccount, _data, _reply, 0);
                    _reply.readException();
                    long _result = _reply.readLong();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public long sendCommandToPublicAccount(String publicAccountId, long threadId, String text) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(publicAccountId);
                    _data.writeLong(threadId);
                    _data.writeString(text);
                    this.mRemote.transact(Stub.TRANSACTION_sendCommandToPublicAccount, _data, _reply, 0);
                    _reply.readException();
                    long _result = _reply.readLong();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IServiceApi asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IServiceApi)) {
                return new Proxy(obj);
            }
            return (IServiceApi) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            String _result;
            boolean _result2;
            long _result3;
            int _result4;
            List<String> _result5;
            GroupChat _result6;
            List<GroupChatMember> _result7;
            switch (code) {
                case 1:
                    data.enforceInterface(DESCRIPTOR);
                    login(data.readString());
                    reply.writeNoException();
                    return true;
                case 2:
                    data.enforceInterface(DESCRIPTOR);
                    logout();
                    reply.writeNoException();
                    return true;
                case 3:
                    data.enforceInterface(DESCRIPTOR);
                    startPluginCenter();
                    reply.writeNoException();
                    return true;
                case 4:
                    data.enforceInterface(DESCRIPTOR);
                    openAccount();
                    reply.writeNoException();
                    return true;
                case 5:
                    data.enforceInterface(DESCRIPTOR);
                    rejectOpenAccount();
                    reply.writeNoException();
                    return true;
                case 6:
                    data.enforceInterface(DESCRIPTOR);
                    getConfiguration();
                    reply.writeNoException();
                    return true;
                case 7:
                    data.enforceInterface(DESCRIPTOR);
                    getConfigurationWithOtp(data.readString());
                    reply.writeNoException();
                    return true;
                case 8:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getAccount();
                    reply.writeNoException();
                    reply.writeString(_result);
                    return true;
                case 9:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = isOnline();
                    reply.writeNoException();
                    reply.writeInt(_result2 ? 1 : 0);
                    return true;
                case 10:
                    data.enforceInterface(DESCRIPTOR);
                    startService();
                    reply.writeNoException();
                    return true;
                case 11:
                    data.enforceInterface(DESCRIPTOR);
                    download(data.readLong());
                    reply.writeNoException();
                    return true;
                case 12:
                    data.enforceInterface(DESCRIPTOR);
                    complain(data.readLong());
                    reply.writeNoException();
                    return true;
                case 13:
                    data.enforceInterface(DESCRIPTOR);
                    backupAll();
                    reply.writeNoException();
                    return true;
                case 14:
                    data.enforceInterface(DESCRIPTOR);
                    backUpFavouriteAll();
                    reply.writeNoException();
                    return true;
                case 15:
                    data.enforceInterface(DESCRIPTOR);
                    backup(data.createTypedArrayList(SimpleMessage.CREATOR));
                    reply.writeNoException();
                    return true;
                case 16:
                    data.enforceInterface(DESCRIPTOR);
                    burnAll();
                    reply.writeNoException();
                    return true;
                case TRANSACTION_burn /*17*/:
                    data.enforceInterface(DESCRIPTOR);
                    burn(data.readLong(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_cancelBackup /*18*/:
                    data.enforceInterface(DESCRIPTOR);
                    cancelBackup();
                    reply.writeNoException();
                    return true;
                case TRANSACTION_cancelCollect /*19*/:
                    data.enforceInterface(DESCRIPTOR);
                    cancelCollect(data.createTypedArrayList(SimpleMessage.CREATOR));
                    reply.writeNoException();
                    return true;
                case TRANSACTION_cancelTopConversation /*20*/:
                    data.enforceInterface(DESCRIPTOR);
                    cancelTopConversation(data.readLong());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_collect /*21*/:
                    data.enforceInterface(DESCRIPTOR);
                    collect(data.createTypedArrayList(SimpleMessage.CREATOR));
                    reply.writeNoException();
                    return true;
                case TRANSACTION_forward /*22*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = forward(data.readLong(), data.readLong(), data.createStringArrayList(), data.readInt());
                    reply.writeNoException();
                    reply.writeLong(_result3);
                    return true;
                case TRANSACTION_forwardToGroupChat /*23*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = forwardToGroupChat(data.readLong(), data.readLong(), data.readLong());
                    reply.writeNoException();
                    reply.writeLong(_result3);
                    return true;
                case TRANSACTION_getThreadId /*24*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = getThreadId(data.createStringArrayList());
                    reply.writeNoException();
                    reply.writeLong(_result3);
                    return true;
                case TRANSACTION_getAudioMaxDuration /*25*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result4 = getAudioMaxDuration();
                    reply.writeNoException();
                    reply.writeInt(_result4);
                    return true;
                case TRANSACTION_getImageMaxSize /*26*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = getImageMaxSize();
                    reply.writeNoException();
                    reply.writeLong(_result3);
                    return true;
                case TRANSACTION_getVideoMaxDuration /*27*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result4 = getVideoMaxDuration();
                    reply.writeNoException();
                    reply.writeInt(_result4);
                    return true;
                case TRANSACTION_getVideoMaxSize /*28*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = getVideoMaxSize();
                    reply.writeNoException();
                    reply.writeLong(_result3);
                    return true;
                case TRANSACTION_getRemindPolicy /*29*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result4 = getRemindPolicy();
                    reply.writeNoException();
                    reply.writeInt(_result4);
                    return true;
                case TRANSACTION_getSendPolicy /*30*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result4 = getSendPolicy();
                    reply.writeNoException();
                    reply.writeInt(_result4);
                    return true;
                case TRANSACTION_pauseDownload /*31*/:
                    data.enforceInterface(DESCRIPTOR);
                    pauseDownload(data.readLong());
                    reply.writeNoException();
                    return true;
                case 32:
                    data.enforceInterface(DESCRIPTOR);
                    _result4 = recoverBlockedMessage(data.readLong());
                    reply.writeNoException();
                    reply.writeInt(_result4);
                    return true;
                case TRANSACTION_recoverBlockedMessageByThreadId /*33*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result4 = recoverBlockedMessageByThreadId(data.readLong());
                    reply.writeNoException();
                    reply.writeInt(_result4);
                    return true;
                case TRANSACTION_deleteAllMessage /*34*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result4 = deleteAllMessage();
                    reply.writeNoException();
                    reply.writeInt(_result4);
                    return true;
                case TRANSACTION_deleteMessageByThreadId /*35*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result4 = deleteMessageByThreadId(data.readLong());
                    reply.writeNoException();
                    reply.writeInt(_result4);
                    return true;
                case TRANSACTION_deleteMessage /*36*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result4 = deleteMessage(data.readLong());
                    reply.writeNoException();
                    reply.writeInt(_result4);
                    return true;
                case TRANSACTION_restoreAll /*37*/:
                    data.enforceInterface(DESCRIPTOR);
                    restoreAll();
                    reply.writeNoException();
                    return true;
                case TRANSACTION_restoreAllFavourite /*38*/:
                    data.enforceInterface(DESCRIPTOR);
                    restoreAllFavourite();
                    reply.writeNoException();
                    return true;
                case TRANSACTION_startComposing /*39*/:
                    data.enforceInterface(DESCRIPTOR);
                    startComposing(data.readLong(), data.readString(), data.readString(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_stopComposing /*40*/:
                    data.enforceInterface(DESCRIPTOR);
                    stopComposing(data.readLong(), data.readString(), data.readString(), data.readLong());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_resend /*41*/:
                    data.enforceInterface(DESCRIPTOR);
                    resend(data.readLong());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_sendText /*42*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = sendText(data.createStringArrayList(), data.readLong(), data.readString(), data.readInt());
                    reply.writeNoException();
                    reply.writeLong(_result3);
                    return true;
                case TRANSACTION_sendImage /*43*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = sendImage(data.createStringArrayList(), data.readLong(), data.readString(), data.readInt(), data.readInt() != 0, data.readInt(), data.readString());
                    reply.writeNoException();
                    reply.writeLong(_result3);
                    return true;
                case TRANSACTION_sendAudio /*44*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = sendAudio(data.createStringArrayList(), data.readLong(), data.readString(), data.readInt(), data.readInt() != 0, data.readInt());
                    reply.writeNoException();
                    reply.writeLong(_result3);
                    return true;
                case TRANSACTION_sendVideo /*45*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = sendVideo(data.createStringArrayList(), data.readLong(), data.readString(), data.readInt(), data.readInt() != 0, data.readInt(), data.readString());
                    reply.writeNoException();
                    reply.writeLong(_result3);
                    return true;
                case TRANSACTION_sendLocation /*46*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = sendLocation(data.createStringArrayList(), data.readLong(), data.readDouble(), data.readDouble(), data.readString(), data.readInt());
                    reply.writeNoException();
                    reply.writeLong(_result3);
                    return true;
                case TRANSACTION_sendVcard /*47*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = sendVcard(data.createStringArrayList(), data.readLong(), data.readString(), data.readInt());
                    reply.writeNoException();
                    reply.writeLong(_result3);
                    return true;
                case TRANSACTION_sendTextToGroupChat /*48*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = sendTextToGroupChat(data.readLong(), data.readLong(), data.readString());
                    reply.writeNoException();
                    reply.writeLong(_result3);
                    return true;
                case TRANSACTION_sendImageToGroupChat /*49*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = sendImageToGroupChat(data.readLong(), data.readLong(), data.readString(), data.readInt(), data.readInt() != 0, data.readString());
                    reply.writeNoException();
                    reply.writeLong(_result3);
                    return true;
                case TRANSACTION_sendAudioToGroupChat /*50*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = sendAudioToGroupChat(data.readLong(), data.readLong(), data.readString(), data.readInt(), data.readInt() != 0);
                    reply.writeNoException();
                    reply.writeLong(_result3);
                    return true;
                case TRANSACTION_sendVideoToGroupChat /*51*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = sendVideoToGroupChat(data.readLong(), data.readLong(), data.readString(), data.readInt(), data.readInt() != 0, data.readString());
                    reply.writeNoException();
                    reply.writeLong(_result3);
                    return true;
                case TRANSACTION_sendLocationToGroupChat /*52*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = sendLocationToGroupChat(data.readLong(), data.readLong(), data.readDouble(), data.readDouble(), data.readString());
                    reply.writeNoException();
                    reply.writeLong(_result3);
                    return true;
                case TRANSACTION_sendVcardToGroupChat /*53*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = sendVcardToGroupChat(data.readLong(), data.readLong(), data.readString());
                    reply.writeNoException();
                    reply.writeLong(_result3);
                    return true;
                case TRANSACTION_sendTextToPc /*54*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = sendTextToPc(data.readLong(), data.readString(), data.readInt());
                    reply.writeNoException();
                    reply.writeLong(_result3);
                    return true;
                case TRANSACTION_sendImageToPc /*55*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = sendImageToPc(data.readLong(), data.readString(), data.readInt(), data.readInt() != 0, data.readInt(), data.readString());
                    reply.writeNoException();
                    reply.writeLong(_result3);
                    return true;
                case TRANSACTION_sendAudioToPc /*56*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = sendAudioToPc(data.readLong(), data.readString(), data.readInt(), data.readInt() != 0, data.readInt());
                    reply.writeNoException();
                    reply.writeLong(_result3);
                    return true;
                case TRANSACTION_sendVideoToPc /*57*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = sendVideoToPc(data.readLong(), data.readString(), data.readInt(), data.readInt() != 0, data.readInt(), data.readString());
                    reply.writeNoException();
                    reply.writeLong(_result3);
                    return true;
                case TRANSACTION_sendLocationToPc /*58*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = sendLocationToPc(data.readLong(), data.readDouble(), data.readDouble(), data.readString(), data.readInt());
                    reply.writeNoException();
                    reply.writeLong(_result3);
                    return true;
                case TRANSACTION_sendVcardToPc /*59*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = sendVcardToPc(data.readLong(), data.readString(), data.readInt());
                    reply.writeNoException();
                    reply.writeLong(_result3);
                    return true;
                case 60:
                    data.enforceInterface(DESCRIPTOR);
                    setRemindPolicy(data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_setSendPolicy /*61*/:
                    data.enforceInterface(DESCRIPTOR);
                    setSendPolicy(data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_topConversation /*62*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result4 = topConversation(data.readLong());
                    reply.writeNoException();
                    reply.writeInt(_result4);
                    return true;
                case TRANSACTION_markMessageAsReaded /*63*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result4 = markMessageAsReaded(data.readLong());
                    reply.writeNoException();
                    reply.writeInt(_result4);
                    return true;
                case 64:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = addSsn(data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result2 ? 1 : 0);
                    return true;
                case TRANSACTION_disableSsn /*65*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = disableSsn();
                    reply.writeNoException();
                    reply.writeInt(_result2 ? 1 : 0);
                    return true;
                case TRANSACTION_deleteSsnPrefix /*66*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = deleteSsnPrefix(data.readString());
                    reply.writeNoException();
                    reply.writeString(_result);
                    return true;
                case TRANSACTION_getSsnList /*67*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result5 = getSsnList();
                    reply.writeNoException();
                    reply.writeStringList(_result5);
                    return true;
                case TRANSACTION_enableSsn /*68*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = enableSsn();
                    reply.writeNoException();
                    reply.writeInt(_result2 ? 1 : 0);
                    return true;
                case TRANSACTION_deleteSsn /*69*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = deleteSsn(data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result2 ? 1 : 0);
                    return true;
                case TRANSACTION_deleteAllSsn /*70*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = deleteAllSsn();
                    reply.writeNoException();
                    reply.writeInt(_result2 ? 1 : 0);
                    return true;
                case TRANSACTION_addBlacklist /*71*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = addBlacklist(data.readString(), data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result2 ? 1 : 0);
                    return true;
                case TRANSACTION_isBlacklist /*72*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = isBlacklist(data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result2 ? 1 : 0);
                    return true;
                case TRANSACTION_clearBlacklist /*73*/:
                    data.enforceInterface(DESCRIPTOR);
                    clearBlacklist();
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getBlacklist /*74*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result5 = getBlacklist();
                    reply.writeNoException();
                    reply.writeStringList(_result5);
                    return true;
                case TRANSACTION_deleteBlacklist /*75*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = deleteBlacklist(data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result2 ? 1 : 0);
                    return true;
                case TRANSACTION_setBlacklistProvider /*76*/:
                    Uri _arg0;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg0 = (Uri) Uri.CREATOR.createFromParcel(data);
                    } else {
                        _arg0 = null;
                    }
                    setBlacklistProvider(_arg0);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getCapability /*77*/:
                    data.enforceInterface(DESCRIPTOR);
                    getCapability(data.readString(), data.readInt() != 0, com.suntek.mway.rcs.client.aidl.service.callback.ICapabiltyListener.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getAllGroupChat /*78*/:
                    data.enforceInterface(DESCRIPTOR);
                    List<GroupChat> _result8 = getAllGroupChat();
                    reply.writeNoException();
                    reply.writeTypedList(_result8);
                    return true;
                case TRANSACTION_getGroupChatById /*79*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result6 = getGroupChatById(data.readLong());
                    reply.writeNoException();
                    if (_result6 != null) {
                        reply.writeInt(1);
                        _result6.writeToParcel(reply, 1);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_getGroupChatByThreadId /*80*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result6 = getGroupChatByThreadId(data.readLong());
                    reply.writeNoException();
                    if (_result6 != null) {
                        reply.writeInt(1);
                        _result6.writeToParcel(reply, 1);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_getMember /*81*/:
                    data.enforceInterface(DESCRIPTOR);
                    GroupChatMember _result9 = getMember(data.readLong(), data.readString());
                    reply.writeNoException();
                    if (_result9 != null) {
                        reply.writeInt(1);
                        _result9.writeToParcel(reply, 1);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_getMembers /*82*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result7 = getMembers(data.readLong());
                    reply.writeNoException();
                    reply.writeTypedList(_result7);
                    return true;
                case TRANSACTION_getMembersAllowChairman /*83*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result7 = getMembersAllowChairman(data.readLong());
                    reply.writeNoException();
                    reply.writeTypedList(_result7);
                    return true;
                case TRANSACTION_getMemberAvatarFromServer /*84*/:
                    data.enforceInterface(DESCRIPTOR);
                    getMemberAvatarFromServer(data.readLong(), data.readString(), data.readInt(), com.suntek.mway.rcs.client.aidl.service.callback.IGroupChatCallback.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getMemberAvatar /*85*/:
                    data.enforceInterface(DESCRIPTOR);
                    getMemberAvatar(data.readLong(), data.readString(), data.readInt(), com.suntek.mway.rcs.client.aidl.service.callback.IGroupChatCallback.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case TRANSACTION_create /*86*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = create(data.readString(), data.createStringArrayList());
                    reply.writeNoException();
                    reply.writeLong(_result3);
                    return true;
                case TRANSACTION_acceptToJoin /*87*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result4 = acceptToJoin(data.readLong(), data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result4);
                    return true;
                case TRANSACTION_rejectToJoin /*88*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result4 = rejectToJoin(data.readLong(), data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result4);
                    return true;
                case TRANSACTION_assignChairman /*89*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result4 = assignChairman(data.readLong(), data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result4);
                    return true;
                case TRANSACTION_disband /*90*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result4 = disband(data.readLong());
                    reply.writeNoException();
                    reply.writeInt(_result4);
                    return true;
                case TRANSACTION_invite /*91*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result4 = invite(data.readLong(), data.createStringArrayList());
                    reply.writeNoException();
                    reply.writeInt(_result4);
                    return true;
                case TRANSACTION_kickOut /*92*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result4 = kickOut(data.readLong(), data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result4);
                    return true;
                case TRANSACTION_quit /*93*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result4 = quit(data.readLong());
                    reply.writeNoException();
                    reply.writeInt(_result4);
                    return true;
                case TRANSACTION_setMyAlias /*94*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result4 = setMyAlias(data.readLong(), data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result4);
                    return true;
                case TRANSACTION_setSubject /*95*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result4 = setSubject(data.readLong(), data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result4);
                    return true;
                case TRANSACTION_setRemarks /*96*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result4 = setRemarks(data.readLong(), data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result4);
                    return true;
                case TRANSACTION_setGroupChatRemindPolicy /*97*/:
                    data.enforceInterface(DESCRIPTOR);
                    setGroupChatRemindPolicy(data.readLong(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_deleteGroupChat /*98*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result4 = deleteGroupChat(data.createLongArray());
                    reply.writeNoException();
                    reply.writeInt(_result4);
                    return true;
                case TRANSACTION_deleteAllGroupChat /*99*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result4 = deleteAllGroupChat();
                    reply.writeNoException();
                    reply.writeInt(_result4);
                    return true;
                case 100:
                    data.enforceInterface(DESCRIPTOR);
                    _result4 = getMyGroupChat();
                    reply.writeNoException();
                    reply.writeInt(_result4);
                    return true;
                case TRANSACTION_rejoin /*101*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result4 = rejoin(data.readLong());
                    reply.writeNoException();
                    reply.writeInt(_result4);
                    return true;
                case TRANSACTION_getMaxAdhocGroupSize /*102*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result4 = getMaxAdhocGroupSize();
                    reply.writeNoException();
                    reply.writeInt(_result4);
                    return true;
                case TRANSACTION_sendEmoticon /*103*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = sendEmoticon(data.createStringArrayList(), data.readLong(), data.readString(), data.readString(), data.readInt());
                    reply.writeNoException();
                    reply.writeLong(_result3);
                    return true;
                case TRANSACTION_sendEmoticonToGroupChat /*104*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = sendEmoticonToGroupChat(data.readLong(), data.readLong(), data.readString(), data.readString());
                    reply.writeNoException();
                    reply.writeLong(_result3);
                    return true;
                case TRANSACTION_sendEmoticonToPc /*105*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = sendEmoticonToPc(data.readLong(), data.readString(), data.readString(), data.readInt());
                    reply.writeNoException();
                    reply.writeLong(_result3);
                    return true;
                case TRANSACTION_sendCloud /*106*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = sendCloud(data.createStringArrayList(), data.readLong(), data.readString(), data.readLong(), data.readString(), data.readString(), data.readInt());
                    reply.writeNoException();
                    reply.writeLong(_result3);
                    return true;
                case TRANSACTION_sendCloudToGroupChat /*107*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = sendCloudToGroupChat(data.readLong(), data.readLong(), data.readString(), data.readLong(), data.readString());
                    reply.writeNoException();
                    reply.writeLong(_result3);
                    return true;
                case TRANSACTION_sendCloudToPc /*108*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = sendCloudToPc(data.readLong(), data.readString(), data.readLong(), data.readString(), data.readString(), data.readInt());
                    reply.writeNoException();
                    reply.writeLong(_result3);
                    return true;
                case TRANSACTION_sendTextToPublicAccount /*109*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = sendTextToPublicAccount(data.readString(), data.readLong(), data.readString());
                    reply.writeNoException();
                    reply.writeLong(_result3);
                    return true;
                case TRANSACTION_sendImageToPublicAccount /*110*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = sendImageToPublicAccount(data.readString(), data.readLong(), data.readString(), data.readInt(), data.readInt() != 0, data.readString());
                    reply.writeNoException();
                    reply.writeLong(_result3);
                    return true;
                case TRANSACTION_sendAudioToPublicAccount /*111*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = sendAudioToPublicAccount(data.readString(), data.readLong(), data.readString(), data.readInt(), data.readInt() != 0);
                    reply.writeNoException();
                    reply.writeLong(_result3);
                    return true;
                case TRANSACTION_sendVideoToPublicAccount /*112*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = sendVideoToPublicAccount(data.readString(), data.readLong(), data.readString(), data.readInt(), data.readInt() != 0, data.readString());
                    reply.writeNoException();
                    reply.writeLong(_result3);
                    return true;
                case TRANSACTION_sendLocationToPublicAccount /*113*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = sendLocationToPublicAccount(data.readString(), data.readLong(), data.readDouble(), data.readDouble(), data.readString());
                    reply.writeNoException();
                    reply.writeLong(_result3);
                    return true;
                case TRANSACTION_sendVcardToPublicAccount /*114*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = sendVcardToPublicAccount(data.readString(), data.readLong(), data.readString());
                    reply.writeNoException();
                    reply.writeLong(_result3);
                    return true;
                case TRANSACTION_sendCommandToPublicAccount /*115*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = sendCommandToPublicAccount(data.readString(), data.readLong(), data.readString());
                    reply.writeNoException();
                    reply.writeLong(_result3);
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    int acceptToJoin(long j, String str) throws RemoteException;

    boolean addBlacklist(String str, String str2) throws RemoteException;

    boolean addSsn(String str) throws RemoteException;

    int assignChairman(long j, String str) throws RemoteException;

    void backUpFavouriteAll() throws RemoteException;

    void backup(List<SimpleMessage> list) throws RemoteException;

    void backupAll() throws RemoteException;

    void burn(long j, int i) throws RemoteException;

    void burnAll() throws RemoteException;

    void cancelBackup() throws RemoteException;

    void cancelCollect(List<SimpleMessage> list) throws RemoteException;

    void cancelTopConversation(long j) throws RemoteException;

    void clearBlacklist() throws RemoteException;

    void collect(List<SimpleMessage> list) throws RemoteException;

    void complain(long j) throws RemoteException;

    long create(String str, List<String> list) throws RemoteException;

    int deleteAllGroupChat() throws RemoteException;

    int deleteAllMessage() throws RemoteException;

    boolean deleteAllSsn() throws RemoteException;

    boolean deleteBlacklist(String str) throws RemoteException;

    int deleteGroupChat(long[] jArr) throws RemoteException;

    int deleteMessage(long j) throws RemoteException;

    int deleteMessageByThreadId(long j) throws RemoteException;

    boolean deleteSsn(String str) throws RemoteException;

    String deleteSsnPrefix(String str) throws RemoteException;

    boolean disableSsn() throws RemoteException;

    int disband(long j) throws RemoteException;

    void download(long j) throws RemoteException;

    boolean enableSsn() throws RemoteException;

    long forward(long j, long j2, List<String> list, int i) throws RemoteException;

    long forwardToGroupChat(long j, long j2, long j3) throws RemoteException;

    String getAccount() throws RemoteException;

    List<GroupChat> getAllGroupChat() throws RemoteException;

    int getAudioMaxDuration() throws RemoteException;

    List<String> getBlacklist() throws RemoteException;

    void getCapability(String str, boolean z, ICapabiltyListener iCapabiltyListener) throws RemoteException;

    void getConfiguration() throws RemoteException;

    void getConfigurationWithOtp(String str) throws RemoteException;

    GroupChat getGroupChatById(long j) throws RemoteException;

    GroupChat getGroupChatByThreadId(long j) throws RemoteException;

    long getImageMaxSize() throws RemoteException;

    int getMaxAdhocGroupSize() throws RemoteException;

    GroupChatMember getMember(long j, String str) throws RemoteException;

    void getMemberAvatar(long j, String str, int i, IGroupChatCallback iGroupChatCallback) throws RemoteException;

    void getMemberAvatarFromServer(long j, String str, int i, IGroupChatCallback iGroupChatCallback) throws RemoteException;

    List<GroupChatMember> getMembers(long j) throws RemoteException;

    List<GroupChatMember> getMembersAllowChairman(long j) throws RemoteException;

    int getMyGroupChat() throws RemoteException;

    int getRemindPolicy() throws RemoteException;

    int getSendPolicy() throws RemoteException;

    List<String> getSsnList() throws RemoteException;

    long getThreadId(List<String> list) throws RemoteException;

    int getVideoMaxDuration() throws RemoteException;

    long getVideoMaxSize() throws RemoteException;

    int invite(long j, List<String> list) throws RemoteException;

    boolean isBlacklist(String str) throws RemoteException;

    boolean isOnline() throws RemoteException;

    int kickOut(long j, String str) throws RemoteException;

    void login(String str) throws RemoteException;

    void logout() throws RemoteException;

    int markMessageAsReaded(long j) throws RemoteException;

    void openAccount() throws RemoteException;

    void pauseDownload(long j) throws RemoteException;

    int quit(long j) throws RemoteException;

    int recoverBlockedMessage(long j) throws RemoteException;

    int recoverBlockedMessageByThreadId(long j) throws RemoteException;

    void rejectOpenAccount() throws RemoteException;

    int rejectToJoin(long j, String str) throws RemoteException;

    int rejoin(long j) throws RemoteException;

    void resend(long j) throws RemoteException;

    void restoreAll() throws RemoteException;

    void restoreAllFavourite() throws RemoteException;

    long sendAudio(List<String> list, long j, String str, int i, boolean z, int i2) throws RemoteException;

    long sendAudioToGroupChat(long j, long j2, String str, int i, boolean z) throws RemoteException;

    long sendAudioToPc(long j, String str, int i, boolean z, int i2) throws RemoteException;

    long sendAudioToPublicAccount(String str, long j, String str2, int i, boolean z) throws RemoteException;

    long sendCloud(List<String> list, long j, String str, long j2, String str2, String str3, int i) throws RemoteException;

    long sendCloudToGroupChat(long j, long j2, String str, long j3, String str2) throws RemoteException;

    long sendCloudToPc(long j, String str, long j2, String str2, String str3, int i) throws RemoteException;

    long sendCommandToPublicAccount(String str, long j, String str2) throws RemoteException;

    long sendEmoticon(List<String> list, long j, String str, String str2, int i) throws RemoteException;

    long sendEmoticonToGroupChat(long j, long j2, String str, String str2) throws RemoteException;

    long sendEmoticonToPc(long j, String str, String str2, int i) throws RemoteException;

    long sendImage(List<String> list, long j, String str, int i, boolean z, int i2, String str2) throws RemoteException;

    long sendImageToGroupChat(long j, long j2, String str, int i, boolean z, String str2) throws RemoteException;

    long sendImageToPc(long j, String str, int i, boolean z, int i2, String str2) throws RemoteException;

    long sendImageToPublicAccount(String str, long j, String str2, int i, boolean z, String str3) throws RemoteException;

    long sendLocation(List<String> list, long j, double d, double d2, String str, int i) throws RemoteException;

    long sendLocationToGroupChat(long j, long j2, double d, double d2, String str) throws RemoteException;

    long sendLocationToPc(long j, double d, double d2, String str, int i) throws RemoteException;

    long sendLocationToPublicAccount(String str, long j, double d, double d2, String str2) throws RemoteException;

    long sendText(List<String> list, long j, String str, int i) throws RemoteException;

    long sendTextToGroupChat(long j, long j2, String str) throws RemoteException;

    long sendTextToPc(long j, String str, int i) throws RemoteException;

    long sendTextToPublicAccount(String str, long j, String str2) throws RemoteException;

    long sendVcard(List<String> list, long j, String str, int i) throws RemoteException;

    long sendVcardToGroupChat(long j, long j2, String str) throws RemoteException;

    long sendVcardToPc(long j, String str, int i) throws RemoteException;

    long sendVcardToPublicAccount(String str, long j, String str2) throws RemoteException;

    long sendVideo(List<String> list, long j, String str, int i, boolean z, int i2, String str2) throws RemoteException;

    long sendVideoToGroupChat(long j, long j2, String str, int i, boolean z, String str2) throws RemoteException;

    long sendVideoToPc(long j, String str, int i, boolean z, int i2, String str2) throws RemoteException;

    long sendVideoToPublicAccount(String str, long j, String str2, int i, boolean z, String str3) throws RemoteException;

    void setBlacklistProvider(Uri uri) throws RemoteException;

    void setGroupChatRemindPolicy(long j, int i) throws RemoteException;

    int setMyAlias(long j, String str) throws RemoteException;

    int setRemarks(long j, String str) throws RemoteException;

    void setRemindPolicy(int i) throws RemoteException;

    void setSendPolicy(int i) throws RemoteException;

    int setSubject(long j, String str) throws RemoteException;

    void startComposing(long j, String str, String str2, int i) throws RemoteException;

    void startPluginCenter() throws RemoteException;

    void startService() throws RemoteException;

    void stopComposing(long j, String str, String str2, long j2) throws RemoteException;

    int topConversation(long j) throws RemoteException;
}
