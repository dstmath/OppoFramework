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
        }
        boolean ret = false;
        if ("VN".equals(region)) {
            if (!id.equals(TH_IME)) {
                ret = true;
            }
        } else if (!id.equals(VN_IME)) {
            ret = true;
        }
        return ret;
    }

    public static boolean isRegionValidSystemIME(String id) {
        String region = SystemProperties.get("persist.sys.oppo.region", "CN");
        boolean def = (id.equals(TH_IME) || id.equals(VN_IME)) ? true : id.equals(US_IME);
        return isRegionValidIME(region, id) ? def : false;
    }

    public static List<InputMethodInfo> getRegionFilteredInputMethodList(List<InputMethodInfo> list) {
        String region = SystemProperties.get("persist.sys.oppo.region", "CN");
        if ("CN".equals(region)) {
            return list;
        }
        if (list == null || list.size() < 1) {
            return new ArrayList(0);
        }
        ArrayList<InputMethodInfo> filteredInputMethodList = new ArrayList();
        for (int i = 0; i < list.size(); i++) {
            InputMethodInfo im = (InputMethodInfo) list.get(i);
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
