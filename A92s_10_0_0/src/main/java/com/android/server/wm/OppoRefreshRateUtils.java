package com.android.server.wm;

import android.text.TextUtils;
import com.android.server.wm.OppoRefreshRateConstants;

class OppoRefreshRateUtils {
    OppoRefreshRateUtils() {
    }

    static int getDefaultRefreshRateId(int settingMode) {
        if (settingMode == 1 || settingMode == 3) {
            return 1;
        }
        return 2;
    }

    static float getRefreshRateById(int id) {
        if (id == 1) {
            return 90.0f;
        }
        if (id != 3) {
            return 60.0f;
        }
        return 120.0f;
    }

    static int compareRefreshRateId(int a, int b) {
        return Float.compare(getRefreshRateById(a), getRefreshRateById(b));
    }

    static int minRefreshRateId(int a, int b) {
        return compareRefreshRateId(a, b) < 0 ? a : b;
    }

    static OppoRefreshRateConstants.PreferredRefreshRateData parsePreferredRefreshRate(String refreshRateStr) {
        OppoRefreshRateConstants.PreferredRefreshRateData tmp = new OppoRefreshRateConstants.PreferredRefreshRateData();
        if (!TextUtils.isEmpty(refreshRateStr)) {
            try {
                String[] strs = refreshRateStr.split("-");
                for (int i = 0; i < strs.length; i++) {
                    tmp.putPreferredRefreshRateId(convertXmlIndex2SettingMode(i), Integer.parseInt(strs[i]));
                }
            } catch (Exception e) {
            }
        }
        return tmp;
    }

    private static int convertXmlIndex2SettingMode(int xmlIndex) {
        if (xmlIndex == 0) {
            return 0;
        }
        if (xmlIndex == 1) {
            return 3;
        }
        if (xmlIndex == 2) {
            return 1;
        }
        if (xmlIndex != 3) {
            return xmlIndex;
        }
        return 2;
    }
}
