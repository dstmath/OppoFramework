package com.android.internal.telephony;

import android.content.Context;
import com.android.internal.telephony.PhoneSubInfoController;

/* renamed from: com.android.internal.telephony.-$$Lambda$PhoneSubInfoController$po8_4IMWtKcwFfhE86w8JrxFyiQ  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$PhoneSubInfoController$po8_4IMWtKcwFfhE86w8JrxFyiQ implements PhoneSubInfoController.PermissionCheckHelper {
    public static final /* synthetic */ $$Lambda$PhoneSubInfoController$po8_4IMWtKcwFfhE86w8JrxFyiQ INSTANCE = new $$Lambda$PhoneSubInfoController$po8_4IMWtKcwFfhE86w8JrxFyiQ();

    private /* synthetic */ $$Lambda$PhoneSubInfoController$po8_4IMWtKcwFfhE86w8JrxFyiQ() {
    }

    @Override // com.android.internal.telephony.PhoneSubInfoController.PermissionCheckHelper
    public final boolean checkPermission(Context context, int i, String str, String str2) {
        return TelephonyPermissions.checkCallingOrSelfReadPhoneState(context, i, str, str2);
    }
}
