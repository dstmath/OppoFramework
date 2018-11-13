package com.suntek.mway.rcs.client.api.message;

import android.os.RemoteException;
import android.text.TextUtils;
import com.suntek.mway.rcs.client.aidl.plugin.entity.cloudfile.CloudFileMessage;
import com.suntek.mway.rcs.client.aidl.plugin.entity.pubacct.PublicMessage;
import com.suntek.mway.rcs.client.aidl.service.entity.SimpleMessage;
import com.suntek.mway.rcs.client.api.ServiceApi;
import com.suntek.mway.rcs.client.api.exception.FileDurationException;
import com.suntek.mway.rcs.client.api.exception.FileNotExistsException;
import com.suntek.mway.rcs.client.api.exception.FileSuffixException;
import com.suntek.mway.rcs.client.api.exception.FileTooLargeException;
import com.suntek.mway.rcs.client.api.exception.ServiceDisconnectedException;
import com.suntek.mway.rcs.client.api.log.LogHelper;
import com.suntek.mway.rcs.client.api.parse.CloudFileMessageParser;
import com.suntek.mway.rcs.client.api.parse.PublicMediaMessageParser;
import com.suntek.mway.rcs.client.api.parse.PublicTextMessageParser;
import com.suntek.mway.rcs.client.api.parse.PublicTopicMessageParser;
import com.suntek.mway.rcs.client.api.util.VerificationUtil;
import com.suntek.mway.rcs.client.api.util.XmlUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MessageApi {
    private static MessageApi instance;

    private MessageApi() {
    }

    public static synchronized MessageApi getInstance() {
        MessageApi messageApi;
        synchronized (MessageApi.class) {
            if (instance == null) {
                instance = new MessageApi();
            }
            messageApi = instance;
        }
        return messageApi;
    }

    public void download(long id) throws RemoteException, ServiceDisconnectedException {
        ServiceApi.getServiceApi().download(id);
    }

    public void complain(long id) throws RemoteException, ServiceDisconnectedException {
        ServiceApi.getServiceApi().complain(id);
    }

    public void backupAll() throws RemoteException, ServiceDisconnectedException {
        ServiceApi.getServiceApi().backupAll();
    }

    public void backUpFavouriteAll() throws RemoteException, ServiceDisconnectedException {
        ServiceApi.getServiceApi().backUpFavouriteAll();
    }

    public void backup(List<SimpleMessage> simpleMessageList) throws RemoteException, ServiceDisconnectedException {
        ServiceApi.getServiceApi().backup(simpleMessageList);
    }

    public void burnAll() throws RemoteException, ServiceDisconnectedException {
        ServiceApi.getServiceApi().burnAll();
    }

    public void burn(long id) throws RemoteException, ServiceDisconnectedException {
        ServiceApi.getServiceApi().burn(id, 0);
    }

    public void burn(long id, int delaySeconds) throws RemoteException, ServiceDisconnectedException {
        ServiceApi.getServiceApi().burn(id, delaySeconds);
    }

    public void cancelBackup() throws RemoteException, ServiceDisconnectedException {
        ServiceApi.getServiceApi().cancelBackup();
    }

    public void cancelCollect(List<SimpleMessage> simpleMessageList) throws RemoteException, ServiceDisconnectedException {
        ServiceApi.getServiceApi().cancelCollect(simpleMessageList);
    }

    public void cancelTopConversation(long threadId) throws RemoteException, ServiceDisconnectedException {
        ServiceApi.getServiceApi().cancelTopConversation(threadId);
    }

    public void collect(List<SimpleMessage> simpleMessageList) throws RemoteException, ServiceDisconnectedException {
        ServiceApi.getServiceApi().collect(simpleMessageList);
    }

    public long forward(long id, long threadId, String number, int barCycle) throws RemoteException, ServiceDisconnectedException {
        LogHelper.i(String.format(Locale.getDefault(), "enter method forward. [id,threadId,number,barCycle]=%d,%d,%s,%d", new Object[]{Long.valueOf(id), Long.valueOf(threadId), number, Integer.valueOf(barCycle)}));
        if (!VerificationUtil.isNumber(number)) {
            LogHelper.i("number field value error");
            return 0;
        } else if (barCycle < -1) {
            LogHelper.i("barCycle field must be greater than -1");
            return 0;
        } else {
            number = VerificationUtil.formatNumber(number);
            List<String> numberList = new ArrayList();
            numberList.add(number);
            return ServiceApi.getServiceApi().forward(id, threadId, numberList, barCycle);
        }
    }

    public long forward(long id, long threadId, List<String> numberList, int barCycle) throws RemoteException, ServiceDisconnectedException {
        LogHelper.i(String.format(Locale.getDefault(), "enter method forward. [id,threadId,numberList,barCycle]=%d,%d,%s,%d", new Object[]{Long.valueOf(id), Long.valueOf(threadId), VerificationUtil.getNumberListString(numberList), Integer.valueOf(barCycle)}));
        if (!VerificationUtil.isAllNumber(numberList)) {
            LogHelper.i("number field value error");
            return 0;
        } else if (barCycle < -1) {
            LogHelper.i("barCycle field must be greater than -1");
            return 0;
        } else {
            return ServiceApi.getServiceApi().forward(id, threadId, VerificationUtil.formatNumbers(numberList), barCycle);
        }
    }

    public long forwardToGroupChat(long id, long threadId, long groupId) throws RemoteException, ServiceDisconnectedException {
        LogHelper.i(String.format(Locale.getDefault(), "enter method forwardToGroupChat. [id,threadId,groupId]=%d,%d,%d", new Object[]{Long.valueOf(id), Long.valueOf(threadId), Long.valueOf(groupId)}));
        return ServiceApi.getServiceApi().forwardToGroupChat(id, threadId, groupId);
    }

    public long getThreadId(String number) throws RemoteException, ServiceDisconnectedException {
        LogHelper.i(String.format(Locale.getDefault(), "enter method getThreadId. [number]=%s", new Object[]{number}));
        if (VerificationUtil.isNumber(number)) {
            number = VerificationUtil.formatNumber(number);
            List<String> numberList = new ArrayList();
            numberList.add(number);
            return ServiceApi.getServiceApi().getThreadId(numberList);
        }
        LogHelper.i("number field value error");
        return 0;
    }

    public long getThreadId(List<String> numberList) throws RemoteException, ServiceDisconnectedException {
        LogHelper.i(String.format(Locale.getDefault(), "enter method getThreadId. [numberList]=%s", new Object[]{VerificationUtil.getNumberListString(numberList)}));
        if (VerificationUtil.isAllNumber(numberList)) {
            return ServiceApi.getServiceApi().getThreadId(VerificationUtil.formatNumbers(numberList));
        }
        LogHelper.i("number field value error");
        return 0;
    }

    public long getThreadIdForPublicAccount(String publicAccountId) throws RemoteException, ServiceDisconnectedException {
        LogHelper.i(String.format(Locale.getDefault(), "enter method getThreadId. [publicAccountId]=%s", new Object[]{publicAccountId}));
        publicAccountId = VerificationUtil.getNumberFromUri(publicAccountId);
        if (VerificationUtil.isNumber(publicAccountId)) {
            List<String> numberList = new ArrayList();
            numberList.add(publicAccountId);
            return ServiceApi.getServiceApi().getThreadId(numberList);
        }
        LogHelper.i("number field value error");
        return 0;
    }

    public int getAudioMaxDuration() throws RemoteException, ServiceDisconnectedException {
        return 180;
    }

    public long getImageMaxSize() throws RemoteException, ServiceDisconnectedException {
        return 10240;
    }

    public int getVideoMaxDuration() throws RemoteException, ServiceDisconnectedException {
        return 90;
    }

    public long getVideoMaxSize() throws RemoteException, ServiceDisconnectedException {
        return 512000;
    }

    public int getRemindPolicy() throws RemoteException, ServiceDisconnectedException {
        return ServiceApi.getServiceApi().getRemindPolicy();
    }

    public int getSendPolicy() throws RemoteException, ServiceDisconnectedException {
        return ServiceApi.getServiceApi().getSendPolicy();
    }

    public void pauseDownload(long id) throws RemoteException, ServiceDisconnectedException {
        ServiceApi.getServiceApi().pauseDownload(id);
    }

    public int recoverBlockedMessage(long blockedMessageId) throws RemoteException, ServiceDisconnectedException {
        return ServiceApi.getServiceApi().recoverBlockedMessage(blockedMessageId);
    }

    public int recoverBlockedMessageByThreadId(long threadId) throws RemoteException, ServiceDisconnectedException {
        return ServiceApi.getServiceApi().recoverBlockedMessageByThreadId(threadId);
    }

    public int deleteAllMessage() throws RemoteException, ServiceDisconnectedException {
        return ServiceApi.getServiceApi().deleteAllMessage();
    }

    public int deleteMessageByThreadId(long threadId) throws RemoteException, ServiceDisconnectedException {
        return ServiceApi.getServiceApi().deleteMessageByThreadId(threadId);
    }

    public int deleteMessage(long id) throws RemoteException, ServiceDisconnectedException {
        return ServiceApi.getServiceApi().deleteMessage(id);
    }

    public void restoreAll() throws RemoteException, ServiceDisconnectedException {
        ServiceApi.getServiceApi().restoreAll();
    }

    public void restoreAllFavourite() throws RemoteException, ServiceDisconnectedException {
        ServiceApi.getServiceApi().restoreAllFavourite();
    }

    public void startComposing(long threadId, String number, String contentType, int seconds) throws RemoteException, ServiceDisconnectedException {
        ServiceApi.getServiceApi().startComposing(threadId, number, contentType, seconds);
    }

    public void stopComposing(long threadId, String number, String contentType, long lastActive) throws RemoteException, ServiceDisconnectedException {
        ServiceApi.getServiceApi().stopComposing(threadId, number, contentType, lastActive);
    }

    public void resend(long id) throws RemoteException, ServiceDisconnectedException {
        ServiceApi.getServiceApi().resend(id);
    }

    public long sendText(String number, long threadId, String text, int barCycle) throws RemoteException, ServiceDisconnectedException {
        LogHelper.i(String.format(Locale.getDefault(), "enter method sendText. [number,threadId,text,barCycle]=%s,%d,%s,%d", new Object[]{number, Long.valueOf(threadId), text, Integer.valueOf(barCycle)}));
        if ("".equals(text.trim())) {
            LogHelper.i("text value is null/Space");
            return 0;
        } else if (!VerificationUtil.isNumber(number)) {
            LogHelper.i("number field value error");
            return 0;
        } else if (barCycle < -1) {
            LogHelper.i("barCycle field must be greater than -1");
            return 0;
        } else {
            number = VerificationUtil.formatNumber(number);
            List<String> numberList = new ArrayList();
            numberList.add(number);
            return ServiceApi.getServiceApi().sendText(numberList, threadId, text, barCycle);
        }
    }

    public long sendText(List<String> numberList, long threadId, String text, int barCycle) throws RemoteException, ServiceDisconnectedException {
        LogHelper.i(String.format(Locale.getDefault(), "enter method sendText. [numberList,threadId,text,barCycle]=%s,%d,%s,%d", new Object[]{VerificationUtil.getNumberListString(numberList), Long.valueOf(threadId), text, Integer.valueOf(barCycle)}));
        if ("".equals(text.trim())) {
            LogHelper.i("text value is null/Space");
            return 0;
        } else if (!VerificationUtil.isAllNumber(numberList)) {
            LogHelper.i("number field value error");
            return 0;
        } else if (barCycle < -1) {
            LogHelper.i("barCycle field must be greater than -1");
            return 0;
        } else {
            return ServiceApi.getServiceApi().sendText(VerificationUtil.formatNumbers(numberList), threadId, text, barCycle);
        }
    }

    public long sendImage(String number, long threadId, String filepath, int quality, boolean isRecord, int barCycle) throws RemoteException, ServiceDisconnectedException, FileSuffixException, FileNotExistsException, FileTooLargeException {
        return sendImage(number, threadId, filepath, quality, isRecord, barCycle, null);
    }

    public long sendImage(String number, long threadId, String filepath, int quality, boolean isRecord, int barCycle, String thumbnailPath) throws RemoteException, ServiceDisconnectedException, FileSuffixException, FileNotExistsException, FileTooLargeException {
        LogHelper.i(String.format(Locale.getDefault(), "enter method sendImage. [number,threadId,filepath,quality,isRecord,barCycle,thumbnailPath]=%s,%d,%s,%d,%b,%d,%s", new Object[]{number, Long.valueOf(threadId), filepath, Integer.valueOf(quality), Boolean.valueOf(isRecord), Integer.valueOf(barCycle), thumbnailPath}));
        if (!VerificationUtil.isNumber(number)) {
            LogHelper.i("number field value error");
            return 0;
        } else if (barCycle < -1) {
            LogHelper.i("barCycle field must be greater than -1");
            return 0;
        } else if (quality < 0 || quality > 100) {
            LogHelper.i("quality field value must be between 0 to 100");
            return 0;
        } else {
            VerificationUtil.isImageFile(filepath);
            VerificationUtil.isFileExists(filepath);
            if (thumbnailPath != null) {
                VerificationUtil.isImageFile(thumbnailPath);
                VerificationUtil.isFileExists(thumbnailPath);
            }
            if (quality == 100) {
                VerificationUtil.isFileSizeToLarge(filepath, getImageMaxSize());
            }
            number = VerificationUtil.formatNumber(number);
            List<String> numberList = new ArrayList();
            numberList.add(number);
            return ServiceApi.getServiceApi().sendImage(numberList, threadId, filepath, quality, isRecord, barCycle, thumbnailPath);
        }
    }

    public long sendImage(List<String> numberList, long threadId, String filepath, int quality, boolean isRecord, int barCycle) throws RemoteException, ServiceDisconnectedException, FileSuffixException, FileNotExistsException, FileTooLargeException {
        return sendImage((List) numberList, threadId, filepath, quality, isRecord, barCycle, null);
    }

    public long sendImage(List<String> numberList, long threadId, String filepath, int quality, boolean isRecord, int barCycle, String thumbnailPath) throws RemoteException, ServiceDisconnectedException, FileSuffixException, FileNotExistsException, FileTooLargeException {
        LogHelper.i(String.format(Locale.getDefault(), "enter method sendImage. [numberList,threadId,filepath,quality,isRecord,barCycle,thumbnailPath]=%s,%d,%s,%d,%b,%d,%s", new Object[]{VerificationUtil.getNumberListString(numberList), Long.valueOf(threadId), filepath, Integer.valueOf(quality), Boolean.valueOf(isRecord), Integer.valueOf(barCycle), thumbnailPath}));
        if (!VerificationUtil.isAllNumber(numberList)) {
            LogHelper.i("number field value error");
            return 0;
        } else if (barCycle < -1) {
            LogHelper.i("barCycle field must be greater than -1");
            return 0;
        } else if (quality < 0 || quality > 100) {
            LogHelper.i("quality field value must be between 0 to 100");
            return 0;
        } else {
            VerificationUtil.isImageFile(filepath);
            VerificationUtil.isFileExists(filepath);
            if (thumbnailPath != null) {
                VerificationUtil.isImageFile(thumbnailPath);
                VerificationUtil.isFileExists(thumbnailPath);
            }
            if (quality == 100) {
                VerificationUtil.isFileSizeToLarge(filepath, getImageMaxSize());
            }
            return ServiceApi.getServiceApi().sendImage(VerificationUtil.formatNumbers(numberList), threadId, filepath, quality, isRecord, barCycle, thumbnailPath);
        }
    }

    public long sendAudio(String number, long threadId, String filepath, int duration, boolean isRecord, int barCycle) throws RemoteException, ServiceDisconnectedException, FileSuffixException, FileNotExistsException, FileTooLargeException, FileDurationException {
        LogHelper.i(String.format(Locale.getDefault(), "enter method sendAudio. [number,threadId,filepath,duration,isRecord,barCycle]=%s,%d,%s,%d,%b,%d", new Object[]{number, Long.valueOf(threadId), filepath, Integer.valueOf(duration), Boolean.valueOf(isRecord), Integer.valueOf(barCycle)}));
        if (!VerificationUtil.isNumber(number)) {
            LogHelper.i("number field value error");
            return 0;
        } else if (barCycle < -1) {
            LogHelper.i("barCycle field must be greater than -1");
            return 0;
        } else {
            VerificationUtil.isAudioFile(filepath);
            VerificationUtil.isFileExists(filepath);
            if (isRecord) {
                VerificationUtil.isAudioDurationToLong(ServiceApi.getInstance().getContext(), filepath, (long) getAudioMaxDuration(), duration);
            }
            VerificationUtil.isFileSizeToLarge(filepath, getVideoMaxSize());
            number = VerificationUtil.formatNumber(number);
            List<String> numberList = new ArrayList();
            numberList.add(number);
            return ServiceApi.getServiceApi().sendAudio(numberList, threadId, filepath, duration, isRecord, barCycle);
        }
    }

    public long sendAudio(List<String> numberList, long threadId, String filepath, int duration, boolean isRecord, int barCycle) throws RemoteException, ServiceDisconnectedException, FileSuffixException, FileNotExistsException, FileTooLargeException, FileDurationException {
        LogHelper.i(String.format(Locale.getDefault(), "enter method sendAudio. [numberList,threadId,filepath,duration,isRecord,barCycle]=%s,%d,%s,%d,%b,%d", new Object[]{VerificationUtil.getNumberListString(numberList), Long.valueOf(threadId), filepath, Integer.valueOf(duration), Boolean.valueOf(isRecord), Integer.valueOf(barCycle)}));
        if (!VerificationUtil.isAllNumber(numberList)) {
            LogHelper.i("number field value error");
            return 0;
        } else if (barCycle < -1) {
            LogHelper.i("barCycle field must be greater than -1");
            return 0;
        } else {
            VerificationUtil.isAudioFile(filepath);
            VerificationUtil.isFileExists(filepath);
            if (isRecord) {
                VerificationUtil.isAudioDurationToLong(ServiceApi.getInstance().getContext(), filepath, (long) getAudioMaxDuration(), duration);
            }
            VerificationUtil.isFileSizeToLarge(filepath, getVideoMaxSize());
            return ServiceApi.getServiceApi().sendAudio(VerificationUtil.formatNumbers(numberList), threadId, filepath, duration, isRecord, barCycle);
        }
    }

    public long sendVideo(String number, long threadId, String filepath, int duration, boolean isRecord, int barCycle) throws RemoteException, ServiceDisconnectedException, FileSuffixException, FileNotExistsException, FileTooLargeException, FileDurationException {
        return sendVideo(number, threadId, filepath, duration, isRecord, barCycle, null);
    }

    public long sendVideo(String number, long threadId, String filepath, int duration, boolean isRecord, int barCycle, String thumbnailPath) throws RemoteException, ServiceDisconnectedException, FileSuffixException, FileNotExistsException, FileTooLargeException, FileDurationException {
        LogHelper.i(String.format(Locale.getDefault(), "enter method sendVideo. [number,threadId,filepath,duration,isRecord,barCycle,thumbnailPath]=%s,%d,%s,%d,%b,%d,%s", new Object[]{number, Long.valueOf(threadId), filepath, Integer.valueOf(duration), Boolean.valueOf(isRecord), Integer.valueOf(barCycle), thumbnailPath}));
        if (!VerificationUtil.isNumber(number)) {
            LogHelper.i("number field value error");
            return 0;
        } else if (barCycle < -1) {
            LogHelper.i("barCycle field must be greater than -1");
            return 0;
        } else {
            VerificationUtil.isVideoFile(filepath);
            VerificationUtil.isFileExists(filepath);
            if (thumbnailPath != null) {
                VerificationUtil.isImageFile(thumbnailPath);
                VerificationUtil.isFileExists(thumbnailPath);
            }
            if (isRecord) {
                VerificationUtil.isVideoDurationToLong(ServiceApi.getInstance().getContext(), filepath, (long) getVideoMaxDuration(), duration);
            }
            VerificationUtil.isFileSizeToLarge(filepath, getVideoMaxSize());
            number = VerificationUtil.formatNumber(number);
            List<String> numberList = new ArrayList();
            numberList.add(number);
            return ServiceApi.getServiceApi().sendVideo(numberList, threadId, filepath, duration, isRecord, barCycle, thumbnailPath);
        }
    }

    public long sendVideo(List<String> numberList, long threadId, String filepath, int duration, boolean isRecord, int barCycle) throws RemoteException, ServiceDisconnectedException, FileSuffixException, FileNotExistsException, FileTooLargeException, FileDurationException {
        return sendVideo((List) numberList, threadId, filepath, duration, isRecord, barCycle, null);
    }

    public long sendVideo(List<String> numberList, long threadId, String filepath, int duration, boolean isRecord, int barCycle, String thumbnailPath) throws RemoteException, ServiceDisconnectedException, FileSuffixException, FileNotExistsException, FileTooLargeException, FileDurationException {
        LogHelper.i(String.format(Locale.getDefault(), "enter method sendVideo. [numberList,threadId,filepath,duration,isRecord,barCycle,thumbnailPath]=%s,%d,%s,%d,%b,%d,%s", new Object[]{VerificationUtil.getNumberListString(numberList), Long.valueOf(threadId), filepath, Integer.valueOf(duration), Boolean.valueOf(isRecord), Integer.valueOf(barCycle), thumbnailPath}));
        if (!VerificationUtil.isAllNumber(numberList)) {
            LogHelper.i("number field value error");
            return 0;
        } else if (barCycle < -1) {
            LogHelper.i("barCycle field must be greater than -1");
            return 0;
        } else {
            VerificationUtil.isVideoFile(filepath);
            VerificationUtil.isFileExists(filepath);
            if (thumbnailPath != null) {
                VerificationUtil.isImageFile(thumbnailPath);
                VerificationUtil.isFileExists(thumbnailPath);
            }
            if (isRecord) {
                VerificationUtil.isVideoDurationToLong(ServiceApi.getInstance().getContext(), filepath, (long) getVideoMaxDuration(), duration);
            }
            VerificationUtil.isFileSizeToLarge(filepath, getVideoMaxSize());
            return ServiceApi.getServiceApi().sendVideo(VerificationUtil.formatNumbers(numberList), threadId, filepath, duration, isRecord, barCycle, thumbnailPath);
        }
    }

    public long sendLocation(String number, long threadId, double lat, double lng, String label, int barCycle) throws RemoteException, ServiceDisconnectedException {
        LogHelper.i(String.format(Locale.getDefault(), "enter method sendLocation. [number,threadId,lat,lng,text,barCycle]=%s,%d,%f,%f,%s,%d", new Object[]{number, Long.valueOf(threadId), Double.valueOf(lat), Double.valueOf(lng), label, Integer.valueOf(barCycle)}));
        if (!VerificationUtil.isNumber(number)) {
            LogHelper.i("number field value error");
            return 0;
        } else if (barCycle < -1) {
            LogHelper.i("barCycle field must be greater than -1");
            return 0;
        } else {
            number = VerificationUtil.formatNumber(number);
            List<String> numberList = new ArrayList();
            numberList.add(number);
            return ServiceApi.getServiceApi().sendLocation(numberList, threadId, lat, lng, label, barCycle);
        }
    }

    public long sendLocation(List<String> numberList, long threadId, double lat, double lng, String label, int barCycle) throws RemoteException, ServiceDisconnectedException {
        LogHelper.i(String.format(Locale.getDefault(), "enter method sendLocation. [numberList,threadId,lat,lng,text,barCycle]=%s,%d,%f,%f,%s,%d", new Object[]{VerificationUtil.getNumberListString(numberList), Long.valueOf(threadId), Double.valueOf(lat), Double.valueOf(lng), label, Integer.valueOf(barCycle)}));
        if (!VerificationUtil.isAllNumber(numberList)) {
            LogHelper.i("number field value error");
            return 0;
        } else if (barCycle < -1) {
            LogHelper.i("barCycle field must be greater than -1");
            return 0;
        } else {
            return ServiceApi.getServiceApi().sendLocation(VerificationUtil.formatNumbers(numberList), threadId, lat, lng, label, barCycle);
        }
    }

    public long sendVcard(String number, long threadId, String filepath, int barCycle) throws RemoteException, ServiceDisconnectedException, FileSuffixException, FileNotExistsException {
        LogHelper.i(String.format(Locale.getDefault(), "enter method sendVcard. [number,threadId,filepath,barCycle]=%s,%d,%s,%d", new Object[]{number, Long.valueOf(threadId), filepath, Integer.valueOf(barCycle)}));
        if (!VerificationUtil.isNumber(number)) {
            LogHelper.i("number field value error");
            return 0;
        } else if (barCycle < -1) {
            LogHelper.i("barCycle field must be greater than -1");
            return 0;
        } else {
            VerificationUtil.isVcardFile(filepath);
            VerificationUtil.isFileExists(filepath);
            number = VerificationUtil.formatNumber(number);
            List<String> numberList = new ArrayList();
            numberList.add(number);
            return ServiceApi.getServiceApi().sendVcard(numberList, threadId, filepath, barCycle);
        }
    }

    public long sendVcard(List<String> numberList, long threadId, String filepath, int barCycle) throws RemoteException, ServiceDisconnectedException, FileSuffixException, FileNotExistsException {
        LogHelper.i(String.format(Locale.getDefault(), "enter method sendVcard. [numberList,threadId,filepath,barCycle]=%s,%d,%s,%d", new Object[]{VerificationUtil.getNumberListString(numberList), Long.valueOf(threadId), filepath, Integer.valueOf(barCycle)}));
        if (!VerificationUtil.isAllNumber(numberList)) {
            LogHelper.i("number field value error");
            return 0;
        } else if (barCycle < -1) {
            LogHelper.i("barCycle field must be greater than -1");
            return 0;
        } else {
            VerificationUtil.isVcardFile(filepath);
            VerificationUtil.isFileExists(filepath);
            return ServiceApi.getServiceApi().sendVcard(VerificationUtil.formatNumbers(numberList), threadId, filepath, barCycle);
        }
    }

    public long sendTextToGroupChat(long groupId, long threadId, String text) throws RemoteException, ServiceDisconnectedException {
        LogHelper.i(String.format(Locale.getDefault(), "enter method sendTextToGroupChat. [groupId,threadId,text]=%d,%d,%s", new Object[]{Long.valueOf(groupId), Long.valueOf(threadId), text}));
        if (!"".equals(text.trim())) {
            return ServiceApi.getServiceApi().sendTextToGroupChat(groupId, threadId, text);
        }
        LogHelper.i("text value is null/Space");
        return 0;
    }

    public long sendImageToGroupChat(long groupId, long threadId, String filepath, int quality, boolean isRecord) throws RemoteException, ServiceDisconnectedException, FileSuffixException, FileNotExistsException, FileTooLargeException {
        return sendImageToGroupChat(groupId, threadId, filepath, quality, isRecord, null);
    }

    public long sendImageToGroupChat(long groupId, long threadId, String filepath, int quality, boolean isRecord, String thumbnailPath) throws RemoteException, ServiceDisconnectedException, FileSuffixException, FileNotExistsException, FileTooLargeException {
        LogHelper.i(String.format(Locale.getDefault(), "enter method sendImageToGroupChat. [groupId,threadId,filepath,quality,isRecord,thumbnailPath]=%d,%d,%s,%d,%b,%s", new Object[]{Long.valueOf(groupId), Long.valueOf(threadId), filepath, Integer.valueOf(quality), Boolean.valueOf(isRecord), thumbnailPath}));
        if (quality < 0 || quality > 100) {
            LogHelper.i("quality field value must be between 0 to 100");
            return 0;
        }
        VerificationUtil.isImageFile(filepath);
        VerificationUtil.isFileExists(filepath);
        if (thumbnailPath != null) {
            VerificationUtil.isImageFile(thumbnailPath);
            VerificationUtil.isFileExists(thumbnailPath);
        }
        if (quality == 100) {
            VerificationUtil.isFileSizeToLarge(filepath, getImageMaxSize());
        }
        return ServiceApi.getServiceApi().sendImageToGroupChat(groupId, threadId, filepath, quality, isRecord, thumbnailPath);
    }

    public long sendAudioToGroupChat(long groupId, long threadId, String filepath, int duration, boolean isRecord) throws RemoteException, ServiceDisconnectedException, FileSuffixException, FileNotExistsException, FileTooLargeException, FileDurationException {
        LogHelper.i(String.format(Locale.getDefault(), "enter method sendAudioToGroupChat. [groupId,threadId,filepath,duration,isRecord]=%d,%d,%s,%d,%b", new Object[]{Long.valueOf(groupId), Long.valueOf(threadId), filepath, Integer.valueOf(duration), Boolean.valueOf(isRecord)}));
        VerificationUtil.isAudioFile(filepath);
        VerificationUtil.isFileExists(filepath);
        if (isRecord) {
            VerificationUtil.isAudioDurationToLong(ServiceApi.getInstance().getContext(), filepath, (long) getAudioMaxDuration(), duration);
        }
        VerificationUtil.isFileSizeToLarge(filepath, getVideoMaxSize());
        return ServiceApi.getServiceApi().sendAudioToGroupChat(groupId, threadId, filepath, duration, isRecord);
    }

    public long sendVideoToGroupChat(long groupId, long threadId, String filepath, int duration, boolean isRecord) throws RemoteException, ServiceDisconnectedException, FileSuffixException, FileNotExistsException, FileTooLargeException, FileDurationException {
        return sendVideoToGroupChat(groupId, threadId, filepath, duration, isRecord, null);
    }

    public long sendVideoToGroupChat(long groupId, long threadId, String filepath, int duration, boolean isRecord, String thumbnailPath) throws RemoteException, ServiceDisconnectedException, FileSuffixException, FileNotExistsException, FileTooLargeException, FileDurationException {
        LogHelper.i(String.format(Locale.getDefault(), "enter method sendVideoToGroupChat. [groupId,threadId,filepath,duration,isRecord,thumbnailPath]=%d,%d,%s,%d,%b,%s", new Object[]{Long.valueOf(groupId), Long.valueOf(threadId), filepath, Integer.valueOf(duration), Boolean.valueOf(isRecord), thumbnailPath}));
        VerificationUtil.isVideoFile(filepath);
        VerificationUtil.isFileExists(filepath);
        if (thumbnailPath != null) {
            VerificationUtil.isImageFile(thumbnailPath);
            VerificationUtil.isFileExists(thumbnailPath);
        }
        if (isRecord) {
            VerificationUtil.isVideoDurationToLong(ServiceApi.getInstance().getContext(), filepath, (long) getVideoMaxDuration(), duration);
        }
        VerificationUtil.isFileSizeToLarge(filepath, getVideoMaxSize());
        return ServiceApi.getServiceApi().sendVideoToGroupChat(groupId, threadId, filepath, duration, isRecord, thumbnailPath);
    }

    public long sendLocationToGroupChat(long groupId, long threadId, double lat, double lng, String label) throws RemoteException, ServiceDisconnectedException {
        LogHelper.i(String.format(Locale.getDefault(), "enter method sendLocationToGroupChat. [groupId,threadId,lat,lng,text,barCycle]=%d,%d,%f,%f,%s", new Object[]{Long.valueOf(groupId), Long.valueOf(threadId), Double.valueOf(lat), Double.valueOf(lng), label}));
        return ServiceApi.getServiceApi().sendLocationToGroupChat(groupId, threadId, lat, lng, label);
    }

    public long sendVcardToGroupChat(long groupId, long threadId, String filepath) throws RemoteException, ServiceDisconnectedException, FileSuffixException, FileNotExistsException {
        LogHelper.i(String.format(Locale.getDefault(), "enter method sendVcardToGroupChat. [groupId,threadId,filepath]=%d,%d,%s", new Object[]{Long.valueOf(groupId), Long.valueOf(threadId), filepath}));
        VerificationUtil.isVcardFile(filepath);
        VerificationUtil.isFileExists(filepath);
        return ServiceApi.getServiceApi().sendVcardToGroupChat(groupId, threadId, filepath);
    }

    public long sendTextToPc(long threadId, String text, int barCycle) throws RemoteException, ServiceDisconnectedException {
        LogHelper.i(String.format(Locale.getDefault(), "enter method sendTextToPc. [threadId,text,barCycle]=%d,%s,%d", new Object[]{Long.valueOf(threadId), text, Integer.valueOf(barCycle)}));
        if ("".equals(text.trim())) {
            LogHelper.i("text value is null/Space");
            return 0;
        } else if (barCycle >= -1) {
            return ServiceApi.getServiceApi().sendTextToPc(threadId, text, barCycle);
        } else {
            LogHelper.i("barCycle field must be greater than -1");
            return 0;
        }
    }

    public long sendImageToPc(long threadId, String filepath, int quality, boolean isRecord, int barCycle) throws RemoteException, ServiceDisconnectedException, FileSuffixException, FileNotExistsException, FileTooLargeException {
        return sendImageToPc(threadId, filepath, quality, isRecord, barCycle, null);
    }

    public long sendImageToPc(long threadId, String filepath, int quality, boolean isRecord, int barCycle, String thumbnailPath) throws RemoteException, ServiceDisconnectedException, FileSuffixException, FileNotExistsException, FileTooLargeException {
        LogHelper.i(String.format(Locale.getDefault(), "enter method sendImageToPc. [threadId,filepath,quality,isRecord,barCycle,thumbnailPath]=%d,%s,%d,%b,%d,%s", new Object[]{Long.valueOf(threadId), filepath, Integer.valueOf(quality), Boolean.valueOf(isRecord), Integer.valueOf(barCycle), thumbnailPath}));
        if (barCycle < -1) {
            LogHelper.i("barCycle field must be greater than -1");
            return 0;
        } else if (quality < 0 || quality > 100) {
            LogHelper.i("quality field value must be between 0 to 100");
            return 0;
        } else {
            VerificationUtil.isImageFile(filepath);
            VerificationUtil.isFileExists(filepath);
            if (thumbnailPath != null) {
                VerificationUtil.isImageFile(thumbnailPath);
                VerificationUtil.isFileExists(thumbnailPath);
            }
            if (quality == 100) {
                VerificationUtil.isFileSizeToLarge(filepath, getImageMaxSize());
            }
            return ServiceApi.getServiceApi().sendImageToPc(threadId, filepath, quality, isRecord, barCycle, thumbnailPath);
        }
    }

    public long sendAudioToPc(long threadId, String filepath, int duration, boolean isRecord, int barCycle) throws RemoteException, ServiceDisconnectedException, FileSuffixException, FileNotExistsException, FileTooLargeException, FileDurationException {
        LogHelper.i(String.format(Locale.getDefault(), "enter method sendAudioToPc. [threadId,filepath,duration,isRecord,barCycle]=%d,%s,%d,%b,%d", new Object[]{Long.valueOf(threadId), filepath, Integer.valueOf(duration), Boolean.valueOf(isRecord), Integer.valueOf(barCycle)}));
        if (barCycle < -1) {
            LogHelper.i("barCycle field must be greater than -1");
            return 0;
        }
        VerificationUtil.isAudioFile(filepath);
        VerificationUtil.isFileExists(filepath);
        if (isRecord) {
            VerificationUtil.isAudioDurationToLong(ServiceApi.getInstance().getContext(), filepath, (long) getAudioMaxDuration(), duration);
        }
        VerificationUtil.isFileSizeToLarge(filepath, getVideoMaxSize());
        return ServiceApi.getServiceApi().sendAudioToPc(threadId, filepath, duration, isRecord, barCycle);
    }

    public long sendVideoToPc(long threadId, String filepath, int duration, boolean isRecord, int barCycle) throws RemoteException, ServiceDisconnectedException, FileSuffixException, FileNotExistsException, FileTooLargeException, FileDurationException {
        return sendVideoToPc(threadId, filepath, duration, isRecord, barCycle, null);
    }

    public long sendVideoToPc(long threadId, String filepath, int duration, boolean isRecord, int barCycle, String thumbnailPath) throws RemoteException, ServiceDisconnectedException, FileSuffixException, FileNotExistsException, FileTooLargeException, FileDurationException {
        LogHelper.i(String.format(Locale.getDefault(), "enter method sendVideoToPc. [threadId,filepath,duration,isRecord,barCycle,thumbnailPath]=%d,%s,%d,%b,%d,%s", new Object[]{Long.valueOf(threadId), filepath, Integer.valueOf(duration), Boolean.valueOf(isRecord), Integer.valueOf(barCycle), thumbnailPath}));
        if (barCycle < -1) {
            LogHelper.i("barCycle field must be greater than -1");
            return 0;
        }
        VerificationUtil.isVideoFile(filepath);
        VerificationUtil.isFileExists(filepath);
        if (thumbnailPath != null) {
            VerificationUtil.isImageFile(thumbnailPath);
            VerificationUtil.isFileExists(thumbnailPath);
        }
        if (isRecord) {
            VerificationUtil.isVideoDurationToLong(ServiceApi.getInstance().getContext(), filepath, (long) getVideoMaxDuration(), duration);
        }
        VerificationUtil.isFileSizeToLarge(filepath, getVideoMaxSize());
        return ServiceApi.getServiceApi().sendVideoToPc(threadId, filepath, duration, isRecord, barCycle, thumbnailPath);
    }

    public long sendLocationToPc(long threadId, double lat, double lng, String label, int barCycle) throws RemoteException, ServiceDisconnectedException {
        LogHelper.i(String.format(Locale.getDefault(), "enter method sendLocationToPc. [threadId,lat,lng,text,barCycle]=%d,%f,%f,%s,%d", new Object[]{Long.valueOf(threadId), Double.valueOf(lat), Double.valueOf(lng), label, Integer.valueOf(barCycle)}));
        if (barCycle >= -1) {
            return ServiceApi.getServiceApi().sendLocationToPc(threadId, lat, lng, label, barCycle);
        }
        LogHelper.i("barCycle field must be greater than -1");
        return 0;
    }

    public long sendVcardToPc(long threadId, String filepath, int barCycle) throws RemoteException, ServiceDisconnectedException, FileSuffixException, FileNotExistsException {
        LogHelper.i(String.format(Locale.getDefault(), "enter method sendVcardToPc. [threadId,filepath,barCycle]=%d,%s,%d", new Object[]{Long.valueOf(threadId), filepath, Integer.valueOf(barCycle)}));
        if (barCycle < -1) {
            LogHelper.i("barCycle field must be greater than -1");
            return 0;
        }
        VerificationUtil.isVcardFile(filepath);
        VerificationUtil.isFileExists(filepath);
        return ServiceApi.getServiceApi().sendVcardToPc(threadId, filepath, barCycle);
    }

    public void setRemindPolicy(int policy) throws RemoteException, ServiceDisconnectedException {
        ServiceApi.getServiceApi().setRemindPolicy(policy);
    }

    public void setSendPolicy(int policy) throws RemoteException, ServiceDisconnectedException {
        ServiceApi.getServiceApi().setSendPolicy(policy);
    }

    public int topConversation(long threadId) throws RemoteException, ServiceDisconnectedException {
        return ServiceApi.getServiceApi().topConversation(threadId);
    }

    public int markMessageAsReaded(long id) throws RemoteException, ServiceDisconnectedException {
        return ServiceApi.getServiceApi().markMessageAsReaded(id);
    }

    public long sendEmoticon(String number, long threadId, String emoticonId, String emoticonName, int barCycle) throws RemoteException, ServiceDisconnectedException {
        LogHelper.i(String.format(Locale.getDefault(), "enter method sendEmoticon. [number,threadId,emoticonId,emoticonName,barCycle]=%s,%d,%s,%s,%d", new Object[]{number, Long.valueOf(threadId), emoticonId, emoticonName, Integer.valueOf(barCycle)}));
        if (!VerificationUtil.isNumber(number)) {
            LogHelper.i("number field value error");
            return 0;
        } else if (barCycle < -1) {
            LogHelper.i("barCycle field must be greater than -1");
            return 0;
        } else if (TextUtils.isEmpty(emoticonId) || TextUtils.isEmpty(emoticonName)) {
            LogHelper.i("emoticonId or emoticonName is empty");
            return 0;
        } else {
            List<String> numberList = new ArrayList();
            numberList.add(number);
            return ServiceApi.getServiceApi().sendEmoticon(numberList, threadId, emoticonId, emoticonName, barCycle);
        }
    }

    public long sendEmoticon(List<String> numberList, long threadId, String emoticonId, String emoticonName, int barCycle) throws RemoteException, ServiceDisconnectedException {
        LogHelper.i(String.format(Locale.getDefault(), "enter method sendEmoticon. [numberList,threadId,emoticonId,emoticonName,barCycle]=%s,%d,%s,%s,%d", new Object[]{VerificationUtil.getNumberListString(numberList), Long.valueOf(threadId), emoticonId, emoticonName, Integer.valueOf(barCycle)}));
        if (!VerificationUtil.isAllNumber(numberList)) {
            LogHelper.i("number field value error");
            return 0;
        } else if (barCycle < -1) {
            LogHelper.i("barCycle field must be greater than -1");
            return 0;
        } else if (!TextUtils.isEmpty(emoticonId) && !TextUtils.isEmpty(emoticonName)) {
            return ServiceApi.getServiceApi().sendEmoticon(numberList, threadId, emoticonId, emoticonName, barCycle);
        } else {
            LogHelper.i("emoticonId or emoticonName is empty");
            return 0;
        }
    }

    public long sendEmoticonToGroupChat(long groupId, long threadId, String emoticonId, String emoticonName) throws RemoteException, ServiceDisconnectedException {
        LogHelper.i(String.format(Locale.getDefault(), "enter method sendEmoticonToGroupChat. [groupId,threadId,emoticonId,emoticonName]=%d,%d,%s,%s", new Object[]{Long.valueOf(groupId), Long.valueOf(threadId), emoticonId, emoticonName}));
        if (!TextUtils.isEmpty(emoticonId) && !TextUtils.isEmpty(emoticonName)) {
            return ServiceApi.getServiceApi().sendEmoticonToGroupChat(groupId, threadId, emoticonId, emoticonName);
        }
        LogHelper.i("emoticonId or emoticonName is empty");
        return 0;
    }

    public long sendEmoticonToPc(long threadId, String emoticonId, String emoticonName, int barCycle) throws RemoteException, ServiceDisconnectedException {
        LogHelper.i(String.format(Locale.getDefault(), "enter method sendEmoticonToPc. [threadId,emoticonId,emoticonName,barCycle]=%d,%s,%s,%d", new Object[]{Long.valueOf(threadId), emoticonId, emoticonName, Integer.valueOf(barCycle)}));
        if (barCycle < -1) {
            LogHelper.i("barCycle field must be greater than -1");
            return 0;
        } else if (!TextUtils.isEmpty(emoticonId) && !TextUtils.isEmpty(emoticonName)) {
            return ServiceApi.getServiceApi().sendEmoticonToPc(threadId, emoticonId, emoticonName, barCycle);
        } else {
            LogHelper.i("emoticonId or emoticonName is empty");
            return 0;
        }
    }

    public long sendCloud(String number, long threadId, String fileName, long fileSize, String shareUrl, String smsContent, int barCycle) throws RemoteException, ServiceDisconnectedException {
        LogHelper.i(String.format(Locale.getDefault(), "enter method sendCloud. [number,threadId,fileName,fileSize,shareUrl,smsContent,barCycle]=%s,%d,%s,%d,%s,%s,%d", new Object[]{number, Long.valueOf(threadId), fileName, Long.valueOf(fileSize), shareUrl, smsContent, Integer.valueOf(barCycle)}));
        if (!VerificationUtil.isNumber(number)) {
            LogHelper.i("number field value error");
            return 0;
        } else if (barCycle < -1) {
            LogHelper.i("barCycle field must be greater than -1");
            return 0;
        } else if (TextUtils.isEmpty(fileName) || TextUtils.isEmpty(shareUrl) || TextUtils.isEmpty(smsContent)) {
            LogHelper.i("fileName or shareUrl or smsContent is empty");
            return 0;
        } else {
            List<String> numberList = new ArrayList();
            numberList.add(number);
            return ServiceApi.getServiceApi().sendCloud(numberList, threadId, fileName, fileSize, shareUrl, smsContent, barCycle);
        }
    }

    public long sendCloud(List<String> numberList, long threadId, String fileName, long fileSize, String shareUrl, String smsContent, int barCycle) throws RemoteException, ServiceDisconnectedException {
        LogHelper.i(String.format(Locale.getDefault(), "enter method sendCloud. [numberList,threadId,fileName,fileSize,shareUrl,smsContent,barCycle]=%s,%d,%s,%d,%s,%s,%d", new Object[]{VerificationUtil.getNumberListString(numberList), Long.valueOf(threadId), fileName, Long.valueOf(fileSize), shareUrl, smsContent, Integer.valueOf(barCycle)}));
        if (!VerificationUtil.isAllNumber(numberList)) {
            LogHelper.i("number field value error");
            return 0;
        } else if (barCycle < -1) {
            LogHelper.i("barCycle field must be greater than -1");
            return 0;
        } else if (!TextUtils.isEmpty(fileName) && !TextUtils.isEmpty(shareUrl) && !TextUtils.isEmpty(smsContent)) {
            return ServiceApi.getServiceApi().sendCloud(numberList, threadId, fileName, fileSize, shareUrl, smsContent, barCycle);
        } else {
            LogHelper.i("fileName or shareUrl or smsContent is empty");
            return 0;
        }
    }

    public long sendCloudToGroupChat(long groupId, long threadId, String fileName, long fileSize, String shareUrl) throws RemoteException, ServiceDisconnectedException {
        LogHelper.i(String.format(Locale.getDefault(), "enter method sendCloudToGroupChat. [groupId,threadId,fileName,fileSize,shareUrl]=%d,%d,%s,%d,%s", new Object[]{Long.valueOf(groupId), Long.valueOf(threadId), fileName, Long.valueOf(fileSize), shareUrl}));
        if (!TextUtils.isEmpty(fileName) && !TextUtils.isEmpty(shareUrl)) {
            return ServiceApi.getServiceApi().sendCloudToGroupChat(groupId, threadId, fileName, fileSize, shareUrl);
        }
        LogHelper.i("fileName or shareUrl is empty");
        return 0;
    }

    public long sendCloudToPc(long threadId, String fileName, long fileSize, String shareUrl, String smsContent, int barCycle) throws RemoteException, ServiceDisconnectedException {
        LogHelper.i(String.format(Locale.getDefault(), "enter method sendCloudToPc. [threadId,fileName,fileSize,shareUrl,smsContent,barCycle]=%d,%s,%d,%s,%s,%d", new Object[]{Long.valueOf(threadId), fileName, Long.valueOf(fileSize), shareUrl, smsContent, Integer.valueOf(barCycle)}));
        if (barCycle < -1) {
            LogHelper.i("barCycle field must be greater than -1");
            return 0;
        } else if (!TextUtils.isEmpty(fileName) && !TextUtils.isEmpty(shareUrl) && !TextUtils.isEmpty(smsContent)) {
            return ServiceApi.getServiceApi().sendCloudToPc(threadId, fileName, fileSize, shareUrl, smsContent, barCycle);
        } else {
            LogHelper.i("fileName or shareUrl or smsContent is empty");
            return 0;
        }
    }

    public long sendTextToPublicAccount(String publicAccountId, long threadId, String text) throws RemoteException, ServiceDisconnectedException {
        LogHelper.i(String.format(Locale.getDefault(), "enter method sendTextToPublicAccount. [publicAccountId,threadId,text]=%s,%d,%s", new Object[]{publicAccountId, Long.valueOf(threadId), text}));
        if (!"".equals(text.trim())) {
            return ServiceApi.getServiceApi().sendTextToPublicAccount(publicAccountId, threadId, text);
        }
        LogHelper.i("text value is null/Space");
        return 0;
    }

    public long sendImageToPublicAccount(String publicAccountId, long threadId, String filepath, int quality, boolean isRecord) throws RemoteException, ServiceDisconnectedException, FileSuffixException, FileNotExistsException, FileTooLargeException {
        return sendImageToPublicAccount(publicAccountId, threadId, filepath, quality, isRecord, null);
    }

    public long sendImageToPublicAccount(String publicAccountId, long threadId, String filepath, int quality, boolean isRecord, String thumbnailPath) throws RemoteException, ServiceDisconnectedException, FileSuffixException, FileNotExistsException, FileTooLargeException {
        LogHelper.i(String.format(Locale.getDefault(), "enter method sendImageToPublicAccount. [publicAccountId,threadId,filepath,quality,isRecord,thumbnailPath]=%s,%d,%s,%d,%b,%s", new Object[]{publicAccountId, Long.valueOf(threadId), filepath, Integer.valueOf(quality), Boolean.valueOf(isRecord), thumbnailPath}));
        if (quality < 0 || quality > 100) {
            LogHelper.i("quality field value must be between 0 to 100");
            return 0;
        }
        VerificationUtil.isImageFile(filepath);
        VerificationUtil.isFileExists(filepath);
        if (thumbnailPath != null) {
            VerificationUtil.isImageFile(thumbnailPath);
            VerificationUtil.isFileExists(thumbnailPath);
        }
        if (quality == 100) {
            VerificationUtil.isFileSizeToLarge(filepath, getImageMaxSize());
        }
        return ServiceApi.getServiceApi().sendImageToPublicAccount(publicAccountId, threadId, filepath, quality, isRecord, thumbnailPath);
    }

    public long sendAudioToPublicAccount(String publicAccountId, long threadId, String filepath, int duration, boolean isRecord) throws RemoteException, ServiceDisconnectedException, FileSuffixException, FileNotExistsException, FileTooLargeException, FileDurationException {
        LogHelper.i(String.format(Locale.getDefault(), "enter method sendAudioToPublicAccount. [publicAccountId,threadId,filepath,duration,isRecord]=%s,%d,%s,%d,%b", new Object[]{publicAccountId, Long.valueOf(threadId), filepath, Integer.valueOf(duration), Boolean.valueOf(isRecord)}));
        VerificationUtil.isAudioFile(filepath);
        VerificationUtil.isFileExists(filepath);
        if (isRecord) {
            VerificationUtil.isAudioDurationToLong(ServiceApi.getInstance().getContext(), filepath, (long) getAudioMaxDuration(), duration);
        }
        VerificationUtil.isFileSizeToLarge(filepath, getVideoMaxSize());
        return ServiceApi.getServiceApi().sendAudioToPublicAccount(publicAccountId, threadId, filepath, duration, isRecord);
    }

    public long sendVideoToPublicAccount(String publicAccountId, long threadId, String filepath, int duration, boolean isRecord) throws RemoteException, ServiceDisconnectedException, FileSuffixException, FileNotExistsException, FileTooLargeException, FileDurationException {
        return sendVideoToPublicAccount(publicAccountId, threadId, filepath, duration, isRecord, null);
    }

    public long sendVideoToPublicAccount(String publicAccountId, long threadId, String filepath, int duration, boolean isRecord, String thumbnailPath) throws RemoteException, ServiceDisconnectedException, FileSuffixException, FileNotExistsException, FileTooLargeException, FileDurationException {
        LogHelper.i(String.format(Locale.getDefault(), "enter method sendVideoToPublicAccount. [publicAccountId,threadId,filepath,duration,isRecord,thumbnailPath]=%s,%d,%s,%d,%b,%s", new Object[]{publicAccountId, Long.valueOf(threadId), filepath, Integer.valueOf(duration), Boolean.valueOf(isRecord), thumbnailPath}));
        VerificationUtil.isVideoFile(filepath);
        VerificationUtil.isFileExists(filepath);
        if (thumbnailPath != null) {
            VerificationUtil.isImageFile(thumbnailPath);
            VerificationUtil.isFileExists(thumbnailPath);
        }
        if (isRecord) {
            VerificationUtil.isVideoDurationToLong(ServiceApi.getInstance().getContext(), filepath, (long) getVideoMaxDuration(), duration);
        }
        VerificationUtil.isFileSizeToLarge(filepath, getVideoMaxSize());
        return ServiceApi.getServiceApi().sendVideoToPublicAccount(publicAccountId, threadId, filepath, duration, isRecord, thumbnailPath);
    }

    public long sendLocationToPublicAccount(String publicAccountId, long threadId, double lat, double lng, String label) throws RemoteException, ServiceDisconnectedException {
        LogHelper.i(String.format(Locale.getDefault(), "enter method sendLocationToPublicAccount. [publicAccountId,threadId,lat,lng,text]=%s,%d,%f,%f,%s", new Object[]{publicAccountId, Long.valueOf(threadId), Double.valueOf(lat), Double.valueOf(lng), label}));
        return ServiceApi.getServiceApi().sendLocationToPublicAccount(publicAccountId, threadId, lat, lng, label);
    }

    public long sendVcardToPublicAccount(String publicAccountId, long threadId, String filepath) throws RemoteException, ServiceDisconnectedException, FileSuffixException, FileNotExistsException {
        LogHelper.i(String.format(Locale.getDefault(), "enter method sendVcardToPublicAccount. [publicAccountId,threadId,filepath,barCycle]=%s,%d,%s", new Object[]{publicAccountId, Long.valueOf(threadId), filepath}));
        VerificationUtil.isVcardFile(filepath);
        VerificationUtil.isFileExists(filepath);
        return ServiceApi.getServiceApi().sendVcardToPublicAccount(publicAccountId, threadId, filepath);
    }

    public long sendCommandToPublicAccount(String publicAccountId, long threadId, String text) throws RemoteException, ServiceDisconnectedException {
        LogHelper.i(String.format(Locale.getDefault(), "enter method sendCommandToPublicAccount. [publicAccountId,text]=%s,%s", new Object[]{publicAccountId, text}));
        if (!"".equals(text.trim())) {
            return ServiceApi.getServiceApi().sendCommandToPublicAccount(publicAccountId, threadId, text);
        }
        LogHelper.i("text value is null/Space");
        return 0;
    }

    public static PublicMessage parsePublicMessage(int msgType, String content) {
        switch (msgType) {
            case 0:
            case 4:
            case 5:
                PublicTextMessageParser textHandler = new PublicTextMessageParser();
                XmlUtil.parse(content, textHandler);
                return textHandler.getMessage();
            case 1:
            case 2:
            case 3:
                PublicMediaMessageParser Mediahandler = new PublicMediaMessageParser();
                XmlUtil.parse(content, Mediahandler);
                return Mediahandler.getMessage();
            case 6:
                PublicTopicMessageParser Topichandler = new PublicTopicMessageParser();
                XmlUtil.parse(content, Topichandler);
                return Topichandler.getMessage();
            default:
                return null;
        }
    }

    public static CloudFileMessage parseCloudFileMessage(String content) {
        CloudFileMessageParser handler = new CloudFileMessageParser();
        XmlUtil.parse(content, handler);
        return handler.getMessage();
    }
}
