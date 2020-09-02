package com.color.settings;

import android.net.Uri;
import android.os.UserHandle;
import java.io.File;

public final class ColorSettingsConfig {
    private static final String CONFIG_PATH = "/data/oppo";
    public static final String PARAMS_TYPE = "ParamsType";
    public static final String PARAMS_USER_ID = "ParamsUserId";
    private static final String TAG = "CSConfig";
    protected static final int TYPE_COLOR = 0;
    protected static final int TYPE_PSW = 1;

    private static String transferTypeToStr(int type) {
        if (type == 0) {
            return "coloros";
        }
        if (type == 1) {
            return "psw";
        }
        throw new IllegalArgumentException("Error type=" + type);
    }

    public static String getFilePath(int type, int userId, String customPath) {
        String typeStr = transferTypeToStr(type);
        if (userId == 0) {
            return CONFIG_PATH + File.separator + typeStr + File.separator + customPath;
        } else if (userId == -2) {
            int userId2 = UserHandle.myUserId();
            if (userId2 == 0) {
                return CONFIG_PATH + File.separator + typeStr + File.separator + customPath;
            }
            return CONFIG_PATH + File.separator + typeStr + File.separator + userId2 + File.separator + customPath;
        } else if (userId >= 0) {
            return CONFIG_PATH + File.separator + typeStr + File.separator + userId + File.separator + customPath;
        } else {
            throw new IllegalArgumentException("Error userId=" + userId);
        }
    }

    public static Uri getUri(String base, String path, int userId, int type) {
        Uri.Builder builder = Uri.parse(base).buildUpon();
        builder.appendQueryParameter(PARAMS_TYPE, String.valueOf(type));
        if (userId == -2) {
            userId = UserHandle.myUserId();
        }
        builder.appendQueryParameter(PARAMS_USER_ID, String.valueOf(userId));
        builder.encodedPath(path);
        return builder.build();
    }
}
