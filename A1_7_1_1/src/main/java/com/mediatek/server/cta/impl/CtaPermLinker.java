package com.mediatek.server.cta.impl;

import android.content.Context;
import android.content.pm.PackageParser.Activity;
import android.content.pm.PackageParser.ActivityIntentInfo;
import android.content.pm.PackageParser.Package;
import android.text.TextUtils;
import android.util.Slog;
import com.android.server.am.OppoPermissionConstants;
import com.mediatek.cta.CtaPermissions;
import com.mediatek.server.cta.CtaPermsController;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

public class CtaPermLinker {
    private static final String TAG = "CtaPermLinker";
    private static HashSet<String> sForceAddEmailPkgs;
    private static HashSet<String> sForceAddMmsPkgs;
    private static CtaPermLinker sInstance;

    private CtaPermLinker(Context context) {
        if (sForceAddEmailPkgs == null) {
            sForceAddEmailPkgs = new HashSet(Arrays.asList(context.getResources().getStringArray(134479886)));
        }
        if (sForceAddMmsPkgs == null) {
            sForceAddMmsPkgs = new HashSet(Arrays.asList(context.getResources().getStringArray(134479887)));
        }
        if (CtaPermsController.DEBUG) {
            Iterator it = sForceAddEmailPkgs.iterator();
            while (it.hasNext()) {
                Slog.d(TAG, "sForceAddEmailPkgs pkg = " + ((String) it.next()));
            }
            it = sForceAddMmsPkgs.iterator();
            while (it.hasNext()) {
                Slog.d(TAG, "sForceAddMmsPkgs pkg = " + ((String) it.next()));
            }
        }
    }

    public static CtaPermLinker getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new CtaPermLinker(context);
        }
        return sInstance;
    }

    public void link(Package packageR) {
        long currentTimeMillis = System.currentTimeMillis();
        linkCtaPermissionsInternal(packageR);
        Slog.i(TAG, "linkCtaPermissions takes " + (System.currentTimeMillis() - currentTimeMillis) + " ms for pkg: " + (packageR == null ? "null" : packageR.packageName));
    }

    private void linkCtaPermissionsInternal(Package packageR) {
        int i = 0;
        if (packageR != null) {
            while (true) {
                int i2 = i;
                if (i2 >= packageR.requestedPermissions.size()) {
                    boolean contains = packageR.requestedPermissions.contains(OppoPermissionConstants.PERMISSION_SEND_MMS_INTERNET);
                    handleCtaSendEmailPerm(packageR, contains);
                    handleCtaSendMmsPerm(packageR, contains);
                    return;
                }
                String str = (String) packageR.requestedPermissions.get(i2);
                if (CtaPermissions.MAP.containsKey(str)) {
                    for (String str2 : (List) CtaPermissions.MAP.get(str2)) {
                        addCtaPermission(packageR, str2);
                    }
                }
                i = i2 + 1;
            }
        } else {
            Slog.w(TAG, "linkCtaPermissionsInternal pkg is null");
        }
    }

    private void handleCtaSendEmailPerm(Package packageR, boolean z) {
        if (z) {
            if (sForceAddEmailPkgs.contains(packageR.packageName)) {
                addCtaPermission(packageR, "com.mediatek.permission.CTA_SEND_EMAIL");
            }
            if (!TextUtils.isEmpty(packageR.packageName) && packageR.packageName.contains("mail")) {
                addCtaPermission(packageR, "com.mediatek.permission.CTA_SEND_EMAIL");
            }
            if (!packageR.requestedPermissions.contains("com.mediatek.permission.CTA_SEND_EMAIL") && packageR.activities != null) {
                ArrayList arrayList = packageR.activities;
                for (int i = 0; i < arrayList.size(); i++) {
                    ArrayList arrayList2 = ((Activity) arrayList.get(i)).intents;
                    if (arrayList2 != null) {
                        int size = arrayList2.size();
                        for (int i2 = 0; i2 < size; i2++) {
                            ActivityIntentInfo activityIntentInfo = (ActivityIntentInfo) arrayList2.get(i2);
                            if ((activityIntentInfo.hasAction("android.intent.action.SEND") || activityIntentInfo.hasAction("android.intent.action.SENDTO") || activityIntentInfo.hasAction("android.intent.action.SEND_MULTIPLE")) && activityIntentInfo.hasDataScheme("mailto")) {
                                addCtaPermission(packageR, "com.mediatek.permission.CTA_SEND_EMAIL");
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    private void handleCtaSendMmsPerm(Package packageR, boolean z) {
        if (z) {
            if (sForceAddMmsPkgs.contains(packageR.packageName)) {
                addCtaPermission(packageR, "com.mediatek.permission.CTA_SEND_MMS");
            }
            if (packageR.requestedPermissions.contains(OppoPermissionConstants.PERMISSION_RECEIVE_MMS)) {
                addCtaPermission(packageR, "com.mediatek.permission.CTA_SEND_MMS");
            }
            if (!packageR.requestedPermissions.contains("com.mediatek.permission.CTA_SEND_MMS") && packageR.activities != null) {
                ArrayList arrayList = packageR.activities;
                for (int i = 0; i < arrayList.size(); i++) {
                    ArrayList arrayList2 = ((Activity) arrayList.get(i)).intents;
                    if (arrayList2 != null) {
                        int size = arrayList2.size();
                        for (int i2 = 0; i2 < size; i2++) {
                            ActivityIntentInfo activityIntentInfo = (ActivityIntentInfo) arrayList2.get(i2);
                            if ((activityIntentInfo.hasAction("android.intent.action.SEND") || activityIntentInfo.hasAction("android.intent.action.SENDTO") || activityIntentInfo.hasAction("android.intent.action.SEND_MULTIPLE")) && (activityIntentInfo.hasDataScheme("mms") || activityIntentInfo.hasDataScheme("mmsto"))) {
                                addCtaPermission(packageR, "com.mediatek.permission.CTA_SEND_MMS");
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    private void addCtaPermission(Package packageR, String str) {
        if (packageR.requestedPermissions.indexOf(str) == -1) {
            packageR.requestedPermissions.add(str);
        }
    }
}
