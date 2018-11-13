package com.android.server.face.test.sensetime.util;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.os.Environment;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FileUtil {
    public static final String DIR = (Environment.getExternalStorageDirectory().getAbsolutePath() + "/sensetime/faceunlock/");
    public static final String HACKER_MODEL = "M_Liveness_Antispoofing_General_6.0.6_14700201_24320401_half.model";
    public static final String TAG = "FaceService.FileUtil";
    public static final String VERIFY_MODEL = "M_Verify_MimicRes_Common_3.4.0_10000101_50001201_half.model";
    private static Object sMutex = new Object();
    private static FileUtil sSingleInstance;

    public static FileUtil geFileUtil() {
        synchronized (sMutex) {
            if (sSingleInstance == null) {
                sSingleInstance = new FileUtil();
            }
        }
        return sSingleInstance;
    }

    public boolean saveBitmap(Bitmap bitmap, String imagePath) {
        File file = new File(imagePath);
        File parentFile = file.getParentFile();
        if (!parentFile.exists()) {
            parentFile.mkdirs();
        }
        try {
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e2) {
            e2.printStackTrace();
            return false;
        }
    }

    public boolean saveBytes(byte[] bytes, String filePath) {
        File file = new File(filePath);
        File parentFile = file.getParentFile();
        if (!parentFile.exists()) {
            parentFile.mkdirs();
        }
        try {
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(bytes);
            fos.flush();
            fos.close();
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e2) {
            e2.printStackTrace();
            return false;
        }
    }

    public String getFormatTime(long milliseconds) {
        return new SimpleDateFormat("yyyy.MM.dd-HH.mm.ss").format(new Date(milliseconds));
    }
}
