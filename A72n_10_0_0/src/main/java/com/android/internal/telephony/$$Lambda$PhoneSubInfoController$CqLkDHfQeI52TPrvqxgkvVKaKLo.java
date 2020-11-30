package com.android.internal.telephony;

import com.android.internal.telephony.PhoneSubInfoController;

/* renamed from: com.android.internal.telephony.-$$Lambda$PhoneSubInfoController$CqLkDHfQeI52TPrvqxgkvVKaKLo  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$PhoneSubInfoController$CqLkDHfQeI52TPrvqxgkvVKaKLo implements PhoneSubInfoController.CallPhoneMethodHelper {
    public static final /* synthetic */ $$Lambda$PhoneSubInfoController$CqLkDHfQeI52TPrvqxgkvVKaKLo INSTANCE = new $$Lambda$PhoneSubInfoController$CqLkDHfQeI52TPrvqxgkvVKaKLo();

    private /* synthetic */ $$Lambda$PhoneSubInfoController$CqLkDHfQeI52TPrvqxgkvVKaKLo() {
    }

    @Override // com.android.internal.telephony.PhoneSubInfoController.CallPhoneMethodHelper
    public final Object callMethod(Phone phone) {
        return phone.getIccSerialNumber();
    }
}
