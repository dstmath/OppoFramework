package com.color.inner.sun.nio.ch;

import android.util.Log;
import java.lang.reflect.Method;
import java.nio.MappedByteBuffer;

public class FileChannelImplWrapper {
    private static final String TAG = "FileChannelImplWrapper";

    private FileChannelImplWrapper() {
    }

    public static void unmap(MappedByteBuffer buffer) {
        if (buffer == null) {
            Log.w(TAG, "[unmap] buffer is null");
            return;
        }
        try {
            Method method = Class.forName("sun.nio.ch.FileChannelImpl").getDeclaredMethod("unmap", MappedByteBuffer.class);
            method.setAccessible(true);
            method.invoke(null, buffer);
        } catch (Exception e) {
            Log.e(TAG, "[unmap] Exception:", e);
        }
    }
}
