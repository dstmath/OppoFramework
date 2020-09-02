package com.android.internal.telephony;

import android.content.Context;
import com.android.internal.telephony.PhoneSubInfoController;

/* renamed from: com.android.internal.telephony.-$$Lambda$PhoneSubInfoController$Tindg8C4HeRTS8KeokNuT5s_py0  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$PhoneSubInfoController$Tindg8C4HeRTS8KeokNuT5s_py0 implements PhoneSubInfoController.PermissionCheckHelper {
    public static final /* synthetic */ $$Lambda$PhoneSubInfoController$Tindg8C4HeRTS8KeokNuT5s_py0 INSTANCE = new $$Lambda$PhoneSubInfoController$Tindg8C4HeRTS8KeokNuT5s_py0();

    private /* synthetic */ $$Lambda$PhoneSubInfoController$Tindg8C4HeRTS8KeokNuT5s_py0() {
    }

    @Override // com.android.internal.telephony.PhoneSubInfoController.PermissionCheckHelper
    public final boolean checkPermission(Context context, int i, String str, String str2) {
        return TelephonyPermissions.checkCallingOrSelfReadSubscriberIdentifiers(context, i, str, str2);
    }
}
