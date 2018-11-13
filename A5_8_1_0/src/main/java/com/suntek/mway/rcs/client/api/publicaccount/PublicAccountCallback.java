package com.suntek.mway.rcs.client.api.publicaccount;

import android.os.RemoteException;
import com.suntek.mway.rcs.client.aidl.plugin.callback.IPublicAccountCallbackAPI.Stub;
import com.suntek.mway.rcs.client.aidl.plugin.entity.pubacct.MenuInfoMode;
import com.suntek.mway.rcs.client.aidl.plugin.entity.pubacct.MsgContent;
import com.suntek.mway.rcs.client.aidl.plugin.entity.pubacct.PublicAccounts;
import com.suntek.mway.rcs.client.aidl.plugin.entity.pubacct.PublicAccountsDetail;
import java.util.List;

public abstract class PublicAccountCallback extends Stub {
    public void respAddSubscribeAccount(boolean arg0, PublicAccounts arg1) throws RemoteException {
    }

    public void respCancelSubscribeAccount(boolean arg0, PublicAccounts arg1) throws RemoteException {
    }

    public void respComplainPublicAccount(boolean result, PublicAccounts arg1) throws RemoteException {
    }

    public void respGetPreMessage(boolean arg0, List<MsgContent> list) throws RemoteException {
    }

    public void respGetPublicDetail(boolean arg0, PublicAccountsDetail arg1) throws RemoteException {
    }

    public void respGetPublicList(boolean arg0, List<PublicAccounts> list) throws RemoteException {
    }

    public void respGetPublicMenuInfo(boolean result, MenuInfoMode menuInfoMode) throws RemoteException {
    }

    public void respGetUserSubscribePublicList(boolean result, List<PublicAccounts> list) throws RemoteException {
    }

    public void respGetPublicRecommend(boolean arg0, List<PublicAccounts> list) throws RemoteException {
    }

    public void respSetAcceptStatus(boolean result, String uuid) throws RemoteException {
    }
}
