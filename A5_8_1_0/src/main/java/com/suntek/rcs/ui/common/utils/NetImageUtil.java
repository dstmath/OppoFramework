package com.suntek.rcs.ui.common.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.os.Environment;
import android.text.TextUtils;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class NetImageUtil {
    public static String getImgDownloadPath(Context context) {
        return Environment.getExternalStorageState() + "/Android/data/" + context.getPackageName() + "/img/";
    }

    public static String getImgNameByUrl(String url) {
        if (TextUtils.isEmpty(url)) {
            return null;
        }
        return url.substring(url.lastIndexOf("/"));
    }

    public static void saveBitmap(Context context, String url, Bitmap bitmap) {
        String folderPath = getImgDownloadPath(context);
        File folder = new File(folderPath);
        if (!folder.exists()) {
            folder.mkdir();
        }
        try {
            File file = new File(folderPath + getImgNameByUrl(url));
            file.createNewFile();
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e2) {
            e2.printStackTrace();
        }
    }
}
