package com.suntek.mway.rcs.client.api.util;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.text.TextUtils;
import com.suntek.mway.rcs.client.aidl.constant.Constants.MessageConstants;
import com.suntek.mway.rcs.client.api.exception.FileDurationException;
import com.suntek.mway.rcs.client.api.exception.FileNotExistsException;
import com.suntek.mway.rcs.client.api.exception.FileSuffixException;
import com.suntek.mway.rcs.client.api.exception.FileTooLargeException;
import com.suntek.mway.rcs.client.api.exception.ServiceDisconnectedException;
import com.suntek.mway.rcs.client.api.log.LogHelper;
import com.suntek.rcs.ui.common.mms.RcsContactsUtils;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class VerificationUtil {
    private static final String SIP_PREFIX = "sip:";
    private static final String TEL_PREFIX = "tel:";

    public static void ApiIsNull(Object api) throws ServiceDisconnectedException {
        if (api == null) {
            throw new ServiceDisconnectedException("Service unavailable, myApi is null");
        }
    }

    public static boolean isNumber(String number) {
        if (number == null) {
            return false;
        }
        return formatWith86(number).matches("[+86]\\d+");
    }

    public static boolean isAllNumber(List<String> numbers) {
        if (numbers == null || numbers.size() == 0) {
            return false;
        }
        boolean sign = true;
        for (String number : numbers) {
            if (!isNumber(number)) {
                sign = false;
            }
        }
        return sign;
    }

    public static String formatNumber(String number) {
        return formatWith86(getNumberFromUri(number)).replaceAll(" ", "").replaceAll("-", "");
    }

    public static List<String> formatNumbers(List<String> numbers) {
        List<String> re = new ArrayList();
        if (numbers != null && numbers.size() > 0) {
            for (String number : numbers) {
                re.add(formatNumber(number));
            }
            Collections.sort(re);
        }
        return re;
    }

    public static String formatWithout86(String mobile) {
        String formatStr = mobile;
        if (mobile != null) {
            formatStr = mobile.replaceAll(" ", "").replaceAll("-", "");
            if (formatStr.startsWith(RcsContactsUtils.PHONE_PRE_CODE)) {
                formatStr = formatStr.substring(3);
            }
            if (formatStr.startsWith("86")) {
                formatStr = formatStr.substring(2);
            }
            if (formatStr.startsWith(" 86")) {
                formatStr = formatStr.substring(3);
            }
        }
        if (formatStr == null) {
            formatStr = "";
        }
        return formatStr.trim();
    }

    public static String formatWith86(String number) {
        return RcsContactsUtils.PHONE_PRE_CODE + formatWithout86(number);
    }

    public static String getNumberFromUri(String uriStr) {
        if (uriStr == null) {
            return "";
        }
        int index = uriStr.lastIndexOf("<");
        if (index != -1) {
            int index2 = uriStr.indexOf(">");
            if (index2 != -1) {
                uriStr = uriStr.substring(index + 1, index2);
            } else {
                uriStr = uriStr.substring(index + 1);
            }
        }
        if (uriStr.endsWith(">")) {
            uriStr = uriStr.substring(0, uriStr.length() - 1);
        }
        if (uriStr.startsWith(TEL_PREFIX)) {
            uriStr = uriStr.substring(4);
        } else if (uriStr.startsWith(SIP_PREFIX)) {
            uriStr = uriStr.substring(4, uriStr.indexOf("@"));
        }
        int pos = uriStr.indexOf("@");
        if (pos != -1) {
            uriStr = uriStr.substring(0, pos);
        }
        return uriStr;
    }

    public static String getNumberListString(List<String> numberList) {
        if (numberList == null || numberList.size() == 0) {
            return "";
        }
        StringBuffer sbBuffer = new StringBuffer();
        for (String number : numberList) {
            sbBuffer.append(number).append(",");
        }
        sbBuffer.deleteCharAt(sbBuffer.length() - 1);
        return sbBuffer.toString();
    }

    public static void isFileExists(String filePath) throws FileNotExistsException {
        if (TextUtils.isEmpty(filePath) || !new File(filePath).exists()) {
            throw new FileNotExistsException();
        }
    }

    public static void isImageFile(String fileName) throws FileSuffixException {
        boolean flag;
        int suffixIndex = fileName.lastIndexOf(".");
        if (suffixIndex != -1) {
            flag = isImageSuffix(fileName.substring(suffixIndex + 1));
        } else {
            flag = false;
        }
        if (!flag) {
            throw new FileSuffixException("File extension is incorrect, the correct extension is 'JPG,JPEG,PNG,GIF,BMP'");
        }
    }

    public static void isAudioFile(String fileName) throws FileSuffixException {
        boolean flag;
        if (TextUtils.isEmpty(fileName)) {
        }
        int suffixIndex = fileName.lastIndexOf(".");
        if (suffixIndex != -1) {
            flag = isAudioSuffix(fileName.substring(suffixIndex + 1));
        } else {
            flag = false;
        }
        if (!flag) {
            throw new FileSuffixException("File extension is incorrect, the correct extension is 'MP3,M4A,AAC,AMR,3GP'");
        }
    }

    public static void isVideoFile(String fileName) throws FileSuffixException {
        boolean flag;
        if (TextUtils.isEmpty(fileName)) {
        }
        int suffixIndex = fileName.lastIndexOf(".");
        if (suffixIndex != -1) {
            flag = isVideoSuffix(fileName.substring(suffixIndex + 1));
        } else {
            flag = false;
        }
        if (!flag) {
            throw new FileSuffixException("File extension is incorrect, the correct extension is '3GP,MP4'");
        }
    }

    public static void isCloudFile(String filename) throws FileSuffixException {
        if (!isCloudFileAllowedFile(filename)) {
            throw new FileSuffixException("File extension is incorrect, the incorrect extension is 'EXE,BAT,APK,SH,IPA,DEB,PXL,XAP'");
        }
    }

    public static void isFileSizeToLarge(String filename, long maxSize) throws FileTooLargeException {
        File file = new File(filename);
        if (file.exists() && file.isFile() && file.length() > maxSize * 1024) {
            throw new FileTooLargeException("File too large " + (file.length() / 1024) + " KB. Max size of file to be transfer is " + maxSize + " KB.");
        }
    }

    public static void isAudioDurationToLong(Context context, String filename, long maxDuration, int recordTime) throws FileDurationException {
        File file = new File(filename);
        if (file.exists() && file.isFile()) {
            int duration = getAmrFileDuration(context, file);
            if (((long) duration) >= (maxDuration + 1) * 1000 || ((long) recordTime) >= (maxDuration + 1) * 1000) {
                LogHelper.i("throw FileDurationException, duration=" + duration);
                throw new FileDurationException("File duration too long " + duration + " s. Max duration is " + maxDuration + " s.");
            }
        }
    }

    public static void isVideoDurationToLong(Context context, String filename, long maxDuration, int recordTime) throws FileDurationException {
        File file = new File(filename);
        if (file.exists() && file.isFile()) {
            int duration = getVideoFileDuration(context, file);
            if (((long) duration) >= (maxDuration + 1) * 1000 || ((long) recordTime) >= (maxDuration + 1) * 1000) {
                LogHelper.i("throw FileDurationException, duration=" + duration);
                throw new FileDurationException("File duration too long " + duration + " s. Max duration is " + maxDuration + " s.");
            }
        }
    }

    public static void isVcardFile(String fileName) throws FileSuffixException {
        boolean flag;
        if (TextUtils.isEmpty(fileName)) {
        }
        int suffixIndex = fileName.lastIndexOf(".");
        if (suffixIndex != -1) {
            flag = isVcardSuffix(fileName.substring(suffixIndex + 1));
        } else {
            flag = false;
        }
        if (!flag) {
            throw new FileSuffixException("File extension is incorrect, the correct extension is 'VCF'");
        }
    }

    public static boolean isCloudFileAllowedFile(String fileName) {
        if (TextUtils.isEmpty(fileName)) {
            return false;
        }
        int suffixIndex = fileName.lastIndexOf(".");
        if (suffixIndex != -1) {
            return isCloudFileExcludeSuffix(fileName.substring(suffixIndex + 1)) ^ 1;
        }
        return false;
    }

    public static boolean isImageSuffix(String suffix) {
        boolean z = false;
        if (TextUtils.isEmpty(suffix)) {
            return false;
        }
        if (MessageConstants.CONST_IMAGE_SUFFIX.indexOf(suffix.toUpperCase(Locale.getDefault())) != -1) {
            z = true;
        }
        return z;
    }

    public static boolean isAudioSuffix(String suffix) {
        boolean z = false;
        if (TextUtils.isEmpty(suffix)) {
            return false;
        }
        if (MessageConstants.CONST_AUDIO_SUFFIX.indexOf(suffix.toUpperCase(Locale.getDefault())) != -1) {
            z = true;
        }
        return z;
    }

    public static boolean isVideoSuffix(String suffix) {
        boolean z = false;
        if (TextUtils.isEmpty(suffix)) {
            return false;
        }
        if (MessageConstants.CONST_VIDEO_SUFFIX.indexOf(suffix.toUpperCase(Locale.getDefault())) != -1) {
            z = true;
        }
        return z;
    }

    public static boolean isVcardSuffix(String suffix) {
        boolean z = false;
        if (TextUtils.isEmpty(suffix)) {
            return false;
        }
        if (MessageConstants.CONST_VCARD_SUFFIX.indexOf(suffix.toUpperCase(Locale.getDefault())) != -1) {
            z = true;
        }
        return z;
    }

    public static boolean isCloudFileExcludeSuffix(String suffix) {
        boolean z = false;
        if (TextUtils.isEmpty(suffix)) {
            return false;
        }
        if (MessageConstants.CONST_CLOUD_FILE_EXCLUDE_SUFFIX.indexOf(suffix.toUpperCase(Locale.getDefault())) != -1) {
            z = true;
        }
        return z;
    }

    public static final int getAmrFileDuration(Context context, File file) {
        MediaPlayer mp = MediaPlayer.create(context, Uri.fromFile(file));
        int duration = mp.getDuration();
        mp.release();
        return duration;
    }

    public static final int getVideoFileDuration(Context context, File file) {
        try {
            MediaPlayer mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(file.getAbsolutePath());
            mediaPlayer.prepare();
            mediaPlayer.getDuration();
            int duration = mediaPlayer.getDuration();
            mediaPlayer.release();
            return duration;
        } catch (Exception e) {
            return -1;
        }
    }
}
