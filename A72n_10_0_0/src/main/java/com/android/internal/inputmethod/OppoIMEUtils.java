package com.android.internal.inputmethod;

import android.os.SystemProperties;
import android.util.Log;
import android.view.inputmethod.InputMethodInfo;
import java.util.ArrayList;
import java.util.List;

public class OppoIMEUtils {
    private static final boolean REGION_DEGUB = false;
    private static final String REGION_TAG = "RegionIME";
    private static final String TH_IME = "com.oppo.keyboard/.GoKeyboard";
    private static final String US_IME = "com.google.android.inputmethod.latin/com.android.inputmethod.latin.LatinIME";
    private static final String VN_IME = "kynam.ime.gotiengviet/.IME";

    public static boolean isRegionValidIME(String region, String id) {
        if ("CN".equals(region)) {
            return true;
        }
        if (id == null || id.isEmpty()) {
            Log.w(REGION_TAG, "isRegionValidIME(), id is wrong !");
            return false;
        } else if ("VN".equals(region)) {
            if (!id.equals(TH_IME)) {
                return true;
            }
            return false;
        } else if (!id.equals(VN_IME)) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isRegionValidSystemIME(String id) {
        return isRegionValidIME(SystemProperties.get("persist.sys.oppo.region", "CN"), id) && (id.equals(TH_IME) || id.equals(VN_IME) || id.equals(US_IME));
    }

    public static List<InputMethodInfo> getRegionFilteredInputMethodList(List<InputMethodInfo> list) {
        String region = SystemProperties.get("persist.sys.oppo.region", "CN");
        if ("CN".equals(region)) {
            return list;
        }
        if (list == null || list.size() < 1) {
            return new ArrayList(0);
        }
        ArrayList<InputMethodInfo> filteredInputMethodList = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            InputMethodInfo im = list.get(i);
            if (isRegionValidIME(region, im.getId())) {
                filteredInputMethodList.add(im);
            }
        }
        return filteredInputMethodList;
    }

    public static boolean isChinaRegion() {
        return "CN".equals(SystemProperties.get("persist.sys.oppo.region", "CN"));
    }
}
