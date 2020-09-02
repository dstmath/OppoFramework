package com.color.settings;

import android.content.Context;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ColorSettings extends ColorBaseSettings {
    private static final String TAG = "ColorSettings";
    public static final int TYPE_COLOR = 0;
    public static final int TYPE_PSW = 1;

    public static InputStream readConfig(Context context, String customPath, int type) throws IOException {
        return readConfigAsUser(context, customPath, -2, type);
    }

    public static OutputStream writeConfig(Context context, String customPath, int type) throws IOException {
        return writeConfigAsUser(context, customPath, -2, type);
    }

    public static String readConfigString(Context context, String customPath, int type) throws IOException {
        return readConfigStringAsUser(context, customPath, -2, type);
    }

    public static int writeConfigString(Context context, String customPath, int type, String str) throws IOException {
        return writeConfigStringAsUser(context, customPath, -2, type, str);
    }

    public static void registerChangeListener(Context context, String customPath, int type, ColorSettingsChangeListener listener) {
        registerChangeListenerAsUser(context, customPath, -2, type, listener);
    }

    public static void unRegisterChangeListener(Context context, ColorSettingsChangeListener listener) {
        context.getContentResolver().unregisterContentObserver(listener);
    }
}
