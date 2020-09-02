package com.oppo.media;

import android.app.ActivityThread;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.os.Binder;
import android.os.SystemProperties;
import android.util.Log;
import com.oppo.atlas.OppoAtlasManager;
import java.util.ArrayList;

public class OppoMediaCodecList {
    private static final String TAG = "OppoMediaCodecList";
    private static boolean isoppoh264 = false;
    private static boolean needToHandle = false;
    private static String result = null;
    private static int[] sTempCodecIndex = {-1, -1, -1, -1, -1};
    private static int tempCodecIndex = 0;

    public static void checkUseOppoh264() {
        try {
            result = OppoAtlasManager.getInstance(null).getParameters("get_listinfo_bypid=wechat-encode=" + Binder.getCallingPid());
        } catch (NullPointerException e) {
            Log.e(TAG, "OppoMultimediaService getParameters failed");
        }
        isoppoh264 = SystemProperties.getBoolean("wechat.encode.oppo.h264", true);
    }

    public static boolean checkSaveOppoh264Info(MediaCodecInfo info, int index) {
        String str;
        if (!isoppoh264 || (str = result) == null || str.indexOf(info.getName()) == -1) {
            return false;
        }
        Log.i(TAG, "Skip " + info.getName() + " result = " + result + " index is " + result.indexOf(info.getName()) + " app is " + ActivityThread.currentPackageName());
        int[] iArr = sTempCodecIndex;
        int i = tempCodecIndex;
        iArr[i] = index;
        tempCodecIndex = i + 1;
        needToHandle = true;
        return true;
    }

    public static void checkHandleOppoh264Info(ArrayList<MediaCodecInfo> regulars, ArrayList<MediaCodecInfo> all) {
        if (needToHandle) {
            int i = 0;
            while (i < sTempCodecIndex.length) {
                try {
                    if (sTempCodecIndex[i] != -1) {
                        MediaCodecInfo tempInfo = MediaCodecList.getNewCodecInfoAtOppo(sTempCodecIndex[i]);
                        all.add(tempInfo);
                        MediaCodecInfo tempInfo2 = tempInfo.makeRegular();
                        if (tempInfo2 != null) {
                            regulars.add(tempInfo2);
                            Log.i(TAG, "i = " + i + " CodecIndex is " + sTempCodecIndex[i] + " codecInfo is " + tempInfo2.getName());
                        }
                    }
                    i++;
                } catch (Exception e) {
                    Log.e(TAG, "Get codec capabilities failed", e);
                    return;
                }
            }
            needToHandle = false;
        }
    }
}
