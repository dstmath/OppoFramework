package com.color.inner.app;

import android.util.Log;
import java.lang.reflect.Field;
import java.util.LinkedList;

public class QueuedWorkWrapper {
    private static final String TAG = "QueuedWorkWrapper";
    public static final LinkedList<Runnable> sFinishers = getsFinishers();

    private static LinkedList<Runnable> getsFinishers() {
        try {
            Field field = Class.forName("android.app.QueuedWork").getDeclaredField("sFinishers");
            field.setAccessible(true);
            return (LinkedList) field.get(null);
        } catch (Throwable throwable) {
            Log.e(TAG, throwable.getMessage());
            return null;
        }
    }
}
