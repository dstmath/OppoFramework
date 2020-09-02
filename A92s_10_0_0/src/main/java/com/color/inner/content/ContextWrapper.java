package com.color.inner.content;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.UserHandle;
import android.util.Log;
import android.view.Display;
import java.io.File;

public class ContextWrapper {
    public static final String STATUS_BAR_SERVICE = "statusbar";
    private static final String TAG = "ContextWrapper";

    public static void startActivityAsUser(Context context, Intent intent, UserHandle user) {
        try {
            context.startActivityAsUser(intent, user);
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
        }
    }

    public static void startActivityAsUser(Context context, Intent intent, Bundle options, UserHandle userId) {
        try {
            context.startActivityAsUser(intent, options, userId);
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
        }
    }

    public static Display getDisplay(Context context) {
        try {
            return context.getDisplay();
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
            return null;
        }
    }

    public static Context createCredentialProtectedStorageContext(Context context) {
        try {
            return context.createCredentialProtectedStorageContext();
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
            return null;
        }
    }

    public static File getSharedPreferencesPath(Context context, String name) {
        try {
            return context.getSharedPreferencesPath(name);
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
            return null;
        }
    }
}
