package com.color.inner.os;

import android.os.FileUtils;
import android.util.Log;
import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;

public class FileUtilsWrapper {
    private static final String TAG = "FileUtilsWrapper";

    public static void copyFileOrThrow(File srcFile, File destFile) {
        try {
            FileUtils.copyFileOrThrow(srcFile, destFile);
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
        }
    }

    public static boolean copyFile(File srcFile, File destFile) {
        try {
            return FileUtils.copyFile(srcFile, destFile);
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
            return false;
        }
    }

    public static int setPermissions(File path, int mode, int uid, int gid) {
        try {
            return FileUtils.setPermissions(path, mode, uid, gid);
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
            return -1;
        }
    }

    public static int setPermissions(String path, int mode, int uid, int gid) {
        try {
            return FileUtils.setPermissions(path, mode, uid, gid);
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
            return -1;
        }
    }

    public static int setPermissions(FileDescriptor fd, int mode, int uid, int gid) {
        try {
            return FileUtils.setPermissions(fd, mode, uid, gid);
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
            return -1;
        }
    }

    public static String readTextFile(File file, int max, String ellipsis) throws IOException {
        return FileUtils.readTextFile(file, max, ellipsis);
    }
}
