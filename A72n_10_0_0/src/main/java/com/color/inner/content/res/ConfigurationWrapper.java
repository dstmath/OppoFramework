package com.color.inner.content.res;

import android.content.res.ColorBaseConfiguration;
import android.content.res.Configuration;
import android.util.Log;
import com.color.util.ColorTypeCastingHelper;

public class ConfigurationWrapper {
    private static final String TAG = "ConfigurationWrapper";

    public static int getFlipFont(Configuration configuration) {
        try {
            ColorBaseConfiguration baseConfiguration = typeCasting(configuration);
            if (baseConfiguration != null) {
                return baseConfiguration.mOppoExtraConfiguration.mFlipFont;
            }
            return -1;
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
            return -1;
        }
    }

    public static int getThemeChanged(Configuration configuration) {
        try {
            ColorBaseConfiguration baseConfiguration = typeCasting(configuration);
            if (baseConfiguration != null) {
                return baseConfiguration.mOppoExtraConfiguration.mThemeChanged;
            }
            return -1;
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
            return -1;
        }
    }

    public static void setThemeChanged(Configuration configuration, int val) {
        try {
            ColorBaseConfiguration baseConfiguration = typeCasting(configuration);
            if (baseConfiguration != null) {
                baseConfiguration.mOppoExtraConfiguration.mThemeChanged = val;
            }
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
        }
    }

    public static long getThemeChangedFlags(Configuration configuration) {
        try {
            ColorBaseConfiguration baseConfiguration = typeCasting(configuration);
            if (baseConfiguration != null) {
                return baseConfiguration.mOppoExtraConfiguration.mThemeChangedFlags;
            }
            return -1;
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
            return -1;
        }
    }

    public static void setThemeChangedFlags(Configuration configuration, long val) {
        try {
            ColorBaseConfiguration baseConfiguration = typeCasting(configuration);
            if (baseConfiguration != null) {
                baseConfiguration.mOppoExtraConfiguration.mThemeChangedFlags = val;
            }
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
        }
    }

    public static int getAccessibleChanged(Configuration configuration) {
        try {
            ColorBaseConfiguration baseConfiguration = typeCasting(configuration);
            if (baseConfiguration != null) {
                return baseConfiguration.mOppoExtraConfiguration.mAccessibleChanged;
            }
            return 0;
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
            return 0;
        }
    }

    public static void setAccessibleChanged(Configuration configuration, int val) {
        try {
            ColorBaseConfiguration baseConfiguration = typeCasting(configuration);
            if (baseConfiguration != null) {
                baseConfiguration.mOppoExtraConfiguration.mAccessibleChanged = val;
            }
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
        }
    }

    public static long getUxIconConfig(Configuration configuration) {
        try {
            ColorBaseConfiguration baseConfiguration = typeCasting(configuration);
            if (baseConfiguration != null) {
                return baseConfiguration.mOppoExtraConfiguration.mUxIconConfig;
            }
            return -1;
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
            return -1;
        }
    }

    public static void setUxIconConfig(Configuration configuration, long val) {
        try {
            ColorBaseConfiguration baseConfiguration = typeCasting(configuration);
            if (baseConfiguration != null) {
                baseConfiguration.mOppoExtraConfiguration.mUxIconConfig = val;
            }
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
        }
    }

    private static ColorBaseConfiguration typeCasting(Configuration configuration) {
        return (ColorBaseConfiguration) ColorTypeCastingHelper.typeCasting(ColorBaseConfiguration.class, configuration);
    }
}
