package com.mediatek.mtklogger.c2klogger;

import android.os.Environment;
import android.util.Log;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;

public class FileUtil {
    public static String getCbpDir() {
        File[] filesSdCard = new File("/mnt/").listFiles(new FilenameFilter() {
            /* class com.mediatek.mtklogger.c2klogger.FileUtil.AnonymousClass1 */

            public boolean accept(File dir, String name) {
                if (name.toLowerCase().indexOf("sdcard") >= 0) {
                    return true;
                }
                return false;
            }
        });
        if (filesSdCard == null) {
            return null;
        }
        for (File file : filesSdCard) {
            Log.v("via_ets", file.getPath());
            String pathCbp = file.getPath() + "/cbp";
            if (new File(pathCbp).exists()) {
                return pathCbp;
            }
        }
        return null;
    }

    public static boolean sdCardExist() {
        if (Environment.getExternalStorageState().equals("mounted")) {
            Log.d("via_ets", "sdCardExist true");
            return true;
        }
        Log.d("via_ets", "sdCardExist false");
        return false;
    }

    public static String getExtOfPath(String path) {
        int i;
        if (path == null || path.length() <= 0 || (i = path.lastIndexOf(46)) <= -1 || i >= path.length()) {
            return "";
        }
        return path.substring(0, i);
    }

    public static String getDirOfPath(String path) {
        int i;
        if (path == null || path.length() <= 0 || (i = path.lastIndexOf(47)) <= -1 || i >= path.length()) {
            return "";
        }
        return path.substring(0, i + 1);
    }

    public static boolean getAllFileAndPath(String pathDir, ArrayList<String> fileNames, ArrayList<String> filePaths) {
        fileNames.clear();
        filePaths.clear();
        File[] files = new File(pathDir).listFiles();
        if (files == null) {
            return false;
        }
        for (File file : files) {
            fileNames.add(file.getName());
            filePaths.add(file.getPath());
        }
        return true;
    }

    public static boolean waitFileOccur(String pathDev, int timeoutS) {
        for (int i = 0; i < timeoutS * 2; i++) {
            if (new File(pathDev).exists()) {
                return true;
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
}
