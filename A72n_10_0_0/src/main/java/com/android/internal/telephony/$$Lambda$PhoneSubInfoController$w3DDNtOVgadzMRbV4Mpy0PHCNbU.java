package com.android.internal.telephony;

import android.content.Context;
import com.android.internal.telephony.PhoneSubInfoController;

/* renamed from: com.android.internal.telephony.-$$Lambda$PhoneSubInfoController$w3DDNtOVgadzMRbV4Mpy0PHCNbU  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$PhoneSubInfoController$w3DDNtOVgadzMRbV4Mpy0PHCNbU implements PhoneSubInfoController.PermissionCheckHelper {
    public static final /* synthetic */ $$Lambda$PhoneSubInfoController$w3DDNtOVgadzMRbV4Mpy0PHCNbU INSTANCE = new $$Lambda$PhoneSubInfoController$w3DDNtOVgadzMRbV4Mpy0PHCNbU();

    private /* synthetic */ $$Lambda$PhoneSubInfoController$w3DDNtOVgadzMRbV4Mpy0PHCNbU() {
    }

    @Override // com.android.internal.telephony.PhoneSubInfoController.PermissionCheckHelper
    public final boolean checkPermission(Context context, int i, String str, String str2) {
        return TelephonyPermissions.checkCallingOrSelfReadPhoneNumber(context, i, str, str2);
    }
}
