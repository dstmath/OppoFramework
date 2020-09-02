package com.mediatek.media;

import android.media.TimedText;
import android.util.Log;
import java.lang.reflect.Field;

public class TimedTextEx {
    private static final String CLASS_NAME = "android.media.TimedText";
    private static final String FD_FIELD_NAME = "mBitMapFd";
    private static final String HEIGHT_FIELD_NAME = "mBitMapHeight";
    private static final String TAG = "TimedTextEx";
    private static final String TEXT_FIELD_NAME = "mTextByteChars";
    private static final String WIDTH_FIELD_NAME = "mBitMapWidth";
    private static Field fdField;
    private static Field heightField;
    private static Field textField;
    private static Field widthField;

    static {
        try {
            Class cls = Class.forName(CLASS_NAME);
            textField = cls.getDeclaredField(TEXT_FIELD_NAME);
            widthField = cls.getDeclaredField(WIDTH_FIELD_NAME);
            heightField = cls.getDeclaredField(HEIGHT_FIELD_NAME);
            fdField = cls.getDeclaredField(FD_FIELD_NAME);
            textField.setAccessible(true);
            widthField.setAccessible(true);
            heightField.setAccessible(true);
            fdField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            Log.e("@M_TimedTextEx", "NoSuchFieldException");
        } catch (ClassNotFoundException e2) {
            Log.e("@M_TimedTextEx", "ClassNotFoundException: android.media.TimedText");
        }
    }

    public static byte[] getTextByteChars(TimedText timedText) throws IllegalArgumentException, IllegalAccessException {
        return (byte[]) textField.get(timedText);
    }

    public static int getBitmapWidth(TimedText timedText) throws IllegalArgumentException, IllegalAccessException {
        return ((Integer) widthField.get(timedText)).intValue();
    }

    public static int getBitmapHeight(TimedText timedText) throws IllegalArgumentException, IllegalAccessException {
        return ((Integer) heightField.get(timedText)).intValue();
    }

    public static int getBitmapFd(TimedText timedText) throws IllegalArgumentException, IllegalAccessException {
        return ((Integer) fdField.get(timedText)).intValue();
    }
}
