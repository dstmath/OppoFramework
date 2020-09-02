package com.color.favorite;

import android.os.SystemProperties;

public interface IColorFavoriteConstans {
    public static final boolean DBG;
    public static final String EXTRA_PACKAGE_NAME = "package_name";
    public static final String EXTRA_RESULT_DATA = "result_data";
    public static final String EXTRA_RESULT_ERROR = "result_error";
    public static final String EXTRA_RESULT_SAVED = "result_saved";
    public static final String EXTRA_RESULT_TITLES = "result_titles";
    public static final boolean LOG_FAVORITE = SystemProperties.getBoolean("log.favorite", false);
    public static final boolean LOG_PANIC = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    public static final String TAG_UNIFY = "AnteaterFavorite";

    static {
        boolean z = false;
        if (LOG_PANIC || LOG_FAVORITE) {
            z = true;
        }
        DBG = z;
    }
}
