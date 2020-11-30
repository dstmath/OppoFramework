package com.color.inner.libcore.io;

import android.util.Log;
import java.io.FileDescriptor;
import java.net.Socket;
import libcore.io.IoUtils;

public class IoUtilsWrapper {
    private static final String TAG = "IoUtilsWrapper";

    private IoUtilsWrapper() {
    }

    public static void closeQuietly(AutoCloseable closeable) {
        try {
            IoUtils.closeQuietly(closeable);
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
        }
    }

    public static void closeQuietly(FileDescriptor fd) {
        try {
            IoUtils.closeQuietly(fd);
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
        }
    }

    public static void closeQuietly(Socket socket) {
        try {
            IoUtils.closeQuietly(socket);
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
        }
    }
}
