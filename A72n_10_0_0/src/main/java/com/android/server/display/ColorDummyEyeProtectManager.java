package com.android.server.display;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import com.android.server.display.color.DisplayTransformManager;

public class ColorDummyEyeProtectManager implements IColorEyeProtectManager {
    private static volatile ColorDummyEyeProtectManager sInstance = null;

    public static ColorDummyEyeProtectManager getInstance() {
        if (sInstance == null) {
            synchronized (ColorDummyEyeProtectManager.class) {
                if (sInstance == null) {
                    sInstance = new ColorDummyEyeProtectManager();
                }
            }
        }
        return sInstance;
    }

    @Override // com.android.server.display.IColorEyeProtectManager
    public void registerContentObserverForEyeProtect(ContentResolver contentResolver, boolean notifyForDescendants, ContentObserver observer, int userHandle) {
    }

    @Override // com.android.server.display.IColorEyeProtectManager
    public boolean needResetAnimationScaleSetting(Context context, int userId) {
        return false;
    }

    @Override // com.android.server.display.IColorEyeProtectManager
    public void setColorMatrix(int level, float[] value, Context context, DisplayTransformManager dtm) {
        dtm.setColorMatrix(level, value);
    }

    @Override // com.android.server.display.IColorEyeProtectManager
    public boolean handleEyeProtectStateChange(Context context, int userId, boolean selfChange, Uri uri) {
        return false;
    }
}
