package com.color.util;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.provider.BrowserContract;
import java.util.ArrayList;
import java.util.List;

public class ColorSupportUtils {
    private static final String KEY_SUPPORT_VERSION_CODE = "cx.xx.a.cx";
    private static final String TAG = "ColorSupportUtils";
    private static volatile ColorSupportUtils sColorSupportUtils = null;
    private static List<String> sSupportAppList = new ArrayList();

    static {
        sSupportAppList.clear();
        sSupportAppList.add("demo.coloros.support.v3");
        sSupportAppList.add("com.android.calculator2");
        sSupportAppList.add(BrowserContract.AUTHORITY);
        sSupportAppList.add("com.coloros.backuprestore");
        sSupportAppList.add("com.coloros.compass");
        sSupportAppList.add("com.nearme.note");
        sSupportAppList.add("com.coloros.findphone.client");
        sSupportAppList.add("com.coloros.weather");
        sSupportAppList.add("com.nearme.themespace");
        sSupportAppList.add("com.oppo.usercenter");
        sSupportAppList.add("com.redteamobile.roaming");
        sSupportAppList.add("com.coloros.safecenter");
        sSupportAppList.add("com.oppo.quicksearchbox");
        sSupportAppList.add("com.oppo.community");
        sSupportAppList.add("com.coloros.bbs");
        sSupportAppList.add("com.oppo.market");
        sSupportAppList.add("com.nearme.gamecenter");
        sSupportAppList.add("com.coloros.healthcheck");
        sSupportAppList.add("com.oppo.reader");
        sSupportAppList.add("com.oppo.book");
        sSupportAppList.add("com.oppo.owallet");
    }

    private ColorSupportUtils() {
    }

    public static ColorSupportUtils getInstance() {
        if (sColorSupportUtils == null) {
            synchronized (ColorSupportUtils.class) {
                if (sColorSupportUtils == null) {
                    sColorSupportUtils = new ColorSupportUtils();
                }
            }
        }
        return sColorSupportUtils;
    }

    public Drawable replaceIconIfNeeded(PackageManager packageManager, Drawable icon, String packageName) {
        boolean replaceIcon = false;
        if (!(packageName == null || packageManager == null || sSupportAppList == null || !sSupportAppList.contains(packageName))) {
            try {
                ApplicationInfo appInfo = packageManager.getApplicationInfo(packageName, 128);
                if (appInfo.metaData == null || (appInfo.metaData.containsKey(KEY_SUPPORT_VERSION_CODE) ^ 1) != 0) {
                    replaceIcon = true;
                }
            } catch (NameNotFoundException e) {
            }
        }
        if (replaceIcon) {
            return getWarningIcon();
        }
        return icon;
    }

    private Drawable getWarningIcon() {
        return Resources.getSystem().getDrawable(201852212);
    }
}
