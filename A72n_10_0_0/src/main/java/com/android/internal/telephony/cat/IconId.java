package com.android.internal.telephony.cat;

import android.annotation.UnsupportedAppUsage;

public class IconId extends ValueObject {
    @UnsupportedAppUsage
    public int recordNumber;
    public boolean selfExplanatory;

    /* access modifiers changed from: package-private */
    @Override // com.android.internal.telephony.cat.ValueObject
    public ComprehensionTlvTag getTag() {
        return ComprehensionTlvTag.ICON_ID;
    }
}
