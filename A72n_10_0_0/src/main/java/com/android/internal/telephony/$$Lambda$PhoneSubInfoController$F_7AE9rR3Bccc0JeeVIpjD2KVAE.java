package com.android.internal.telephony;

import com.android.internal.telephony.PhoneSubInfoController;

/* renamed from: com.android.internal.telephony.-$$Lambda$PhoneSubInfoController$F_7AE9rR3Bccc0JeeVIpjD2KVAE  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$PhoneSubInfoController$F_7AE9rR3Bccc0JeeVIpjD2KVAE implements PhoneSubInfoController.CallPhoneMethodHelper {
    public static final /* synthetic */ $$Lambda$PhoneSubInfoController$F_7AE9rR3Bccc0JeeVIpjD2KVAE INSTANCE = new $$Lambda$PhoneSubInfoController$F_7AE9rR3Bccc0JeeVIpjD2KVAE();

    private /* synthetic */ $$Lambda$PhoneSubInfoController$F_7AE9rR3Bccc0JeeVIpjD2KVAE() {
    }

    @Override // com.android.internal.telephony.PhoneSubInfoController.CallPhoneMethodHelper
    public final Object callMethod(Phone phone) {
        return phone.getMsisdn();
    }
}
