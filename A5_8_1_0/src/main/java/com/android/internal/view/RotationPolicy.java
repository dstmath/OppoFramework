package com.android.internal.view;

import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.graphics.Point;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.RemoteException;
import android.os.UserHandle;
import android.provider.Settings.System;
import android.util.Log;
import android.view.IWindowManager;
import android.view.WindowManagerGlobal;
import com.android.internal.R;

public final class RotationPolicy {
    private static final int CURRENT_ROTATION = -1;
    private static final int NATURAL_ROTATION = 0;
    private static final String TAG = "RotationPolicy";

    public static abstract class RotationPolicyListener {
        final ContentObserver mObserver = new ContentObserver(new Handler()) {
            public void onChange(boolean selfChange, Uri uri) {
                RotationPolicyListener.this.onChange();
            }
        };

        public abstract void onChange();
    }

    private RotationPolicy() {
    }

    public static boolean isRotationSupported(Context context) {
        PackageManager pm = context.getPackageManager();
        if (pm.hasSystemFeature("android.hardware.sensor.accelerometer") && pm.hasSystemFeature("android.hardware.screen.portrait") && pm.hasSystemFeature("android.hardware.screen.landscape")) {
            return context.getResources().getBoolean(R.bool.config_supportAutoRotation);
        }
        return false;
    }

    public static int getRotationLockOrientation(Context context) {
        if (!areAllRotationsAllowed(context)) {
            Point size = new Point();
            try {
                WindowManagerGlobal.getWindowManagerService().getInitialDisplaySize(0, size);
                return size.x < size.y ? 1 : 2;
            } catch (RemoteException e) {
                Log.w(TAG, "Unable to get the display size");
            }
        }
        return 0;
    }

    public static boolean isRotationLockToggleVisible(Context context) {
        if (isRotationSupported(context) && System.getIntForUser(context.getContentResolver(), System.HIDE_ROTATION_LOCK_TOGGLE_FOR_ACCESSIBILITY, 0, -2) == 0) {
            return true;
        }
        return false;
    }

    public static boolean isRotationLocked(Context context) {
        return System.getIntForUser(context.getContentResolver(), System.ACCELEROMETER_ROTATION, 0, -2) == 0;
    }

    public static void setRotationLock(Context context, boolean enabled) {
        System.putIntForUser(context.getContentResolver(), System.HIDE_ROTATION_LOCK_TOGGLE_FOR_ACCESSIBILITY, 0, -2);
        setRotationLock(enabled, areAllRotationsAllowed(context) ? -1 : 0);
    }

    public static void setRotationLockForAccessibility(Context context, boolean enabled) {
        int i;
        ContentResolver contentResolver = context.getContentResolver();
        String str = System.HIDE_ROTATION_LOCK_TOGGLE_FOR_ACCESSIBILITY;
        if (enabled) {
            i = 1;
        } else {
            i = 0;
        }
        System.putIntForUser(contentResolver, str, i, -2);
        setRotationLock(enabled, 0);
    }

    private static boolean areAllRotationsAllowed(Context context) {
        return context.getResources().getBoolean(R.bool.config_allowAllRotations);
    }

    private static void setRotationLock(final boolean enabled, final int rotation) {
        AsyncTask.execute(new Runnable() {
            public void run() {
                try {
                    IWindowManager wm = WindowManagerGlobal.getWindowManagerService();
                    if (enabled) {
                        wm.freezeRotation(rotation);
                    } else {
                        wm.thawRotation();
                    }
                } catch (RemoteException e) {
                    Log.w(RotationPolicy.TAG, "Unable to save auto-rotate setting");
                }
            }
        });
    }

    public static void registerRotationPolicyListener(Context context, RotationPolicyListener listener) {
        registerRotationPolicyListener(context, listener, UserHandle.getCallingUserId());
    }

    public static void registerRotationPolicyListener(Context context, RotationPolicyListener listener, int userHandle) {
        context.getContentResolver().registerContentObserver(System.getUriFor(System.ACCELEROMETER_ROTATION), false, listener.mObserver, userHandle);
        context.getContentResolver().registerContentObserver(System.getUriFor(System.HIDE_ROTATION_LOCK_TOGGLE_FOR_ACCESSIBILITY), false, listener.mObserver, userHandle);
    }

    public static void unregisterRotationPolicyListener(Context context, RotationPolicyListener listener) {
        context.getContentResolver().unregisterContentObserver(listener.mObserver);
    }
}
