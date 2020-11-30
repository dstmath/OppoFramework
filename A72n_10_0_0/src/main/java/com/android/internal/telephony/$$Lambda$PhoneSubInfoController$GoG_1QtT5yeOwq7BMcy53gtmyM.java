package com.android.internal.telephony;

import com.android.internal.telephony.PhoneSubInfoController;

/* renamed from: com.android.internal.telephony.-$$Lambda$PhoneSubInfoController$GoG_1Q-tT5yeOwq7BMcy53gtmyM  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$PhoneSubInfoController$GoG_1QtT5yeOwq7BMcy53gtmyM implements PhoneSubInfoController.CallPhoneMethodHelper {
    public static final /* synthetic */ $$Lambda$PhoneSubInfoController$GoG_1QtT5yeOwq7BMcy53gtmyM INSTANCE = new $$Lambda$PhoneSubInfoController$GoG_1QtT5yeOwq7BMcy53gtmyM();

    private /* synthetic */ $$Lambda$PhoneSubInfoController$GoG_1QtT5yeOwq7BMcy53gtmyM() {
    }

    @Override // com.android.internal.telephony.PhoneSubInfoController.CallPhoneMethodHelper
    public final Object callMethod(Phone phone) {
        return phone.getIccSerialNumber();
    }
}
