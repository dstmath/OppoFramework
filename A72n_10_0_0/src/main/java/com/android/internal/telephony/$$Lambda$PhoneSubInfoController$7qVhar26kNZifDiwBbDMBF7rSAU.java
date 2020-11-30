package com.android.internal.telephony;

import android.content.Context;
import com.android.internal.telephony.PhoneSubInfoController;

/* renamed from: com.android.internal.telephony.-$$Lambda$PhoneSubInfoController$7qVhar26kNZifDiwBbDMBF7rSAU  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$PhoneSubInfoController$7qVhar26kNZifDiwBbDMBF7rSAU implements PhoneSubInfoController.PermissionCheckHelper {
    public static final /* synthetic */ $$Lambda$PhoneSubInfoController$7qVhar26kNZifDiwBbDMBF7rSAU INSTANCE = new $$Lambda$PhoneSubInfoController$7qVhar26kNZifDiwBbDMBF7rSAU();

    private /* synthetic */ $$Lambda$PhoneSubInfoController$7qVhar26kNZifDiwBbDMBF7rSAU() {
    }

    @Override // com.android.internal.telephony.PhoneSubInfoController.PermissionCheckHelper
    public final boolean checkPermission(Context context, int i, String str, String str2) {
        return TelephonyPermissions.checkCallingOrSelfReadDeviceIdentifiers(context, i, str, str2);
    }
}
