package com.oppo.statistics.util;

import android.content.Context;
import com.oppo.statistics.storage.PreferenceHandler;

public class AccountUtil {
    public static final String SSOID_DEFAULT = "0";

    public static String getSsoId(Context context) {
        String ssoid = PreferenceHandler.getSsoID(context);
        if (ssoid.equals(SSOID_DEFAULT)) {
            LogUtil.e("NearMeStatistics", "ssoid not set.");
        }
        return ssoid;
    }
}
