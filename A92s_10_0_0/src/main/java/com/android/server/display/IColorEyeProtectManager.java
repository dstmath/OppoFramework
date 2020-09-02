package com.android.server.display;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import com.android.server.display.color.DisplayTransformManager;

public interface IColorEyeProtectManager {
    public static final String EYEPROTECT_ENABLE = "color_eyeprotect_enable";
    public static final String EYEPROTECT_INVERSE_ENABLE = "inverse_on";
    public static final int LEVEL_COLOR_MATRIX_COLOR = 400;

    boolean handleEyeProtectStateChange(Context context, int i, boolean z, Uri uri);

    boolean needResetAnimationScaleSetting(Context context, int i);

    void registerContentObserverForEyeProtect(ContentResolver contentResolver, boolean z, ContentObserver contentObserver, int i);

    void setColorMatrix(int i, float[] fArr, Context context, DisplayTransformManager displayTransformManager);
}
