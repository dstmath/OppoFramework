package com.mediatek.server.cta.impl;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.PermissionInfo;
import android.text.TextUtils;
import android.util.Slog;
import com.android.server.oppo.IElsaManager;
import com.mediatek.cta.CtaUtils;
import com.mediatek.server.cta.CtaPermsController;

public class PermErrorHelper {
    private static final String PATTERN_OR = " or ";
    private static final String PATTERN_PERMISSION_DENIAL = "Permission Denial";
    private static final String PATTERN_REQUIRES = " requires ";
    private static final String PATTERN_SECURITY_EXCEPTION = "SecurityException";
    private static final String TAG = "PermErrorHelper";
    private static PermErrorHelper sInstance;
    private Context mContext;

    public static PermErrorHelper getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new PermErrorHelper(context);
        }
        return sInstance;
    }

    private PermErrorHelper(Context context) {
        this.mContext = context;
    }

    public String parsePermName(int i, String str, String str2) {
        if (CtaPermsController.DEBUG) {
            Slog.d(TAG, "parsePermName uid = " + i + ", packageName = " + str + ", exceptionMsg = " + str2);
        }
        if (!CtaUtils.isCtaSupported() || TextUtils.isEmpty(str2) || !str2.contains(PATTERN_SECURITY_EXCEPTION) || !str2.contains(PATTERN_PERMISSION_DENIAL)) {
            return null;
        }
        String substring = str2.substring(str2.indexOf(PATTERN_REQUIRES) + PATTERN_REQUIRES.length(), str2.length());
        String str3 = IElsaManager.EMPTY_PACKAGE;
        if (substring.contains(PATTERN_OR)) {
            substring = substring.substring(substring.indexOf(PATTERN_OR) + PATTERN_OR.length(), substring.length());
        }
        substring = substring.trim();
        if (CtaPermsController.DEBUG) {
            Slog.d(TAG, "initMtkPermErrorDialog() parseResult = " + substring);
        }
        try {
            PermissionInfo permissionInfo = this.mContext.getPackageManager().getPermissionInfo(substring, 0);
            if (permissionInfo.protectionLevel == 1) {
                if (CtaUtils.isPlatformPermission(permissionInfo.packageName, permissionInfo.name)) {
                    substring = permissionInfo.name;
                    return substring;
                }
            }
            if (permissionInfo.protectionLevel != 18) {
                substring = str3;
            } else if (CtaUtils.isCtaOnlyPermission(permissionInfo.name)) {
                substring = permissionInfo.name;
            } else {
                substring = str3;
            }
        } catch (NameNotFoundException e) {
            substring = IElsaManager.EMPTY_PACKAGE;
        }
        return substring;
    }
}
