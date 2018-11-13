package com.suntek.mway.rcs.client.api.cloudfile;

import android.os.RemoteException;
import android.text.TextUtils;
import com.suntek.mway.rcs.client.aidl.plugin.callback.ICloudOperationCtrl;
import com.suntek.mway.rcs.client.aidl.plugin.entity.cloudfile.FileNode.Order;
import com.suntek.mway.rcs.client.aidl.plugin.entity.cloudfile.TransNode.TransOper;
import com.suntek.mway.rcs.client.api.PluginApi;
import com.suntek.mway.rcs.client.api.exception.ServiceDisconnectedException;
import com.suntek.mway.rcs.client.api.log.LogHelper;
import com.suntek.mway.rcs.client.api.util.VerificationUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CloudFileApi {
    private static CloudFileApi instance;

    private CloudFileApi() {
    }

    public static synchronized CloudFileApi getInstance() {
        CloudFileApi cloudFileApi;
        synchronized (CloudFileApi.class) {
            if (instance == null) {
                instance = new CloudFileApi();
            }
            cloudFileApi = instance;
        }
        return cloudFileApi;
    }

    public ICloudOperationCtrl downloadFileFromUrl(String remoteUrl, String fileName, TransOper transOper, long chatMessageId) throws RemoteException, ServiceDisconnectedException {
        return PluginApi.getPluginApi().downloadFileFromUrl(remoteUrl, fileName, transOper.ordinal(), chatMessageId);
    }

    public String getLocalRootPath() throws RemoteException, ServiceDisconnectedException {
        return PluginApi.getPluginApi().getLocalRootPath();
    }

    public void getRemoteFileList(String remotePath, int beginIndex, int endIndex, Order fileOrder) throws RemoteException, ServiceDisconnectedException {
        PluginApi.getPluginApi().getRemoteFileList(remotePath, beginIndex, endIndex, fileOrder.ordinal());
    }

    public void getShareFileList(int beginIndex, int endIndex) throws RemoteException, ServiceDisconnectedException {
        PluginApi.getPluginApi().getShareFileList(beginIndex, endIndex);
    }

    public ICloudOperationCtrl putFile(String localPath, String remotePath, TransOper transOper) throws RemoteException, ServiceDisconnectedException {
        return PluginApi.getPluginApi().putFile(localPath, remotePath, transOper.ordinal());
    }

    public void shareFile(String fileId, String shareDesc) throws RemoteException, ServiceDisconnectedException {
        PluginApi.getPluginApi().shareFile(fileId, shareDesc);
    }

    public void shareFileAndSend(String fileId, String shareDesc, String number, long threadId, String smsContentTemp, int barCycle) throws RemoteException, ServiceDisconnectedException {
        LogHelper.i(String.format(Locale.getDefault(), "enter method shareFileAndSend. [fileId,shareDesc,number,threadId,smsContentTemp,barCycle]=%s,%s,%s,%d,%s,%d", new Object[]{fileId, shareDesc, number, Long.valueOf(threadId), smsContentTemp, Integer.valueOf(barCycle)}));
        if (!VerificationUtil.isNumber(number)) {
            LogHelper.i("number field value error");
        }
        if (barCycle < -1) {
            LogHelper.i("barCycle field must be greater than -1");
        }
        if (TextUtils.isEmpty(fileId)) {
            LogHelper.i("fileId is empty");
        }
        number = VerificationUtil.formatNumber(number);
        List<String> numberList = new ArrayList();
        numberList.add(number);
        PluginApi.getPluginApi().shareFileAndSend(fileId, shareDesc, numberList, threadId, smsContentTemp, barCycle);
    }

    public void shareFileAndSend(String fileId, String shareDesc, List<String> numberList, long threadId, String smsContentTemp, int barCycle) throws RemoteException, ServiceDisconnectedException {
        LogHelper.i(String.format(Locale.getDefault(), "enter method shareFileAndSend. [fileId,shareDesc,numberList,threadId,smsContentTemp,barCycle]=%s,%s,%s,%d,%s,%d", new Object[]{fileId, shareDesc, VerificationUtil.getNumberListString(numberList), Long.valueOf(threadId), smsContentTemp, Integer.valueOf(barCycle)}));
        if (!VerificationUtil.isAllNumber(numberList)) {
            LogHelper.i("number field value error");
        }
        if (barCycle < -1) {
            LogHelper.i("barCycle field must be greater than -1");
        }
        if (TextUtils.isEmpty(fileId)) {
            LogHelper.i("fileId is empty");
        }
        PluginApi.getPluginApi().shareFileAndSend(fileId, shareDesc, VerificationUtil.formatNumbers(numberList), threadId, smsContentTemp, barCycle);
    }

    public void shareFileAndSendGroup(String fileId, String shareDesc, long threadId, long groupId) throws RemoteException, ServiceDisconnectedException {
        LogHelper.i(String.format(Locale.getDefault(), "enter method shareFileAndSendGroup. [fileId,shareDesc,threadId,groupId]=%s,%s,%d,%d", new Object[]{fileId, shareDesc, Long.valueOf(threadId), Long.valueOf(groupId)}));
        if (TextUtils.isEmpty(fileId)) {
            LogHelper.i("fileId is empty");
        }
        PluginApi.getPluginApi().shareFileAndSendGroup(fileId, shareDesc, threadId, groupId);
    }
}
