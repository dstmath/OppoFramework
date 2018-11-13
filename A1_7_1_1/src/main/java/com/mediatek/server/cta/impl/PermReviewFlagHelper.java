package com.mediatek.server.cta.impl;

import android.content.Context;
import android.content.pm.PackageParser.Package;
import android.os.UserHandle;
import com.mediatek.cta.CtaUtils;
import java.util.Iterator;

public class PermReviewFlagHelper {
    private static final String TAG = "PermReviewFlagHelper";
    private static PermReviewFlagHelper sInstance;
    private Context mContext;

    private PermReviewFlagHelper(Context context) {
        this.mContext = context;
    }

    public static PermReviewFlagHelper getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new PermReviewFlagHelper(context);
        }
        return sInstance;
    }

    public boolean isPermissionReviewRequired(Package packageR, int i, boolean z) {
        if (!CtaUtils.isCtaSupported()) {
            return z;
        }
        if (!z) {
            return false;
        }
        if (packageR.mSharedUserId == null) {
            return z;
        }
        if (packageR.requestedPermissions.size() == 0) {
            return false;
        }
        boolean z2;
        UserHandle of = UserHandle.of(i);
        Iterator it = packageR.requestedPermissions.iterator();
        while (it.hasNext()) {
            if ((this.mContext.getPackageManager().getPermissionFlags((String) it.next(), packageR.packageName, of) & 64) != 0) {
                z2 = true;
                break;
            }
        }
        z2 = false;
        return z2;
    }
}
