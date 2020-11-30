package com.color.util;

import android.content.res.Resources;
import android.content.res.TypedArray;
import android.provider.SettingsStringUtil;

public class ColorResourcesUtil {
    public static String[][] loadStringArrays(Resources res, int array) {
        TypedArray a = res.obtainTypedArray(array);
        int length = a.length();
        String[][] arrays = new String[length][];
        for (int i = 0; i < length; i++) {
            int id = a.getResourceId(i, 0);
            if (id != 0) {
                arrays[i] = res.getStringArray(id);
            }
        }
        a.recycle();
        return arrays;
    }

    public static String dumpResource(Resources res, int id) {
        StringBuilder out = new StringBuilder();
        out.append("[");
        dumpResourceInternal(res, id, out, false);
        out.append("]");
        return out.toString();
    }

    static void dumpResourceInternal(Resources res, int id, StringBuilder out, boolean usePackageName) {
        String packageName;
        if (res != null && Resources.resourceHasPackage(id)) {
            int i = -16777216 & id;
            if (i == 16777216) {
                packageName = "android";
            } else if (i != 2130706432) {
                try {
                    packageName = res.getResourcePackageName(id);
                } catch (Resources.NotFoundException e) {
                    return;
                } catch (Exception e2) {
                    return;
                }
            } else {
                packageName = getAppPackageName(usePackageName, res, id);
            }
            String typeName = res.getResourceTypeName(id);
            String entryName = res.getResourceEntryName(id);
            out.append(packageName);
            out.append(SettingsStringUtil.DELIMITER);
            out.append(typeName);
            out.append("/");
            out.append(entryName);
        }
    }

    private static String getAppPackageName(boolean usePackageName, Resources res, int id) {
        if (usePackageName) {
            return res.getResourcePackageName(id);
        }
        return "app";
    }
}
