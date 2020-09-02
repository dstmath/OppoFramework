package com.android.internal.telephony.cat;

public class ItemsIconId extends ValueObject {
    public int[] recordNumbers;
    public boolean selfExplanatory;

    /* access modifiers changed from: package-private */
    @Override // com.android.internal.telephony.cat.ValueObject
    public ComprehensionTlvTag getTag() {
        return ComprehensionTlvTag.ITEM_ICON_ID_LIST;
    }
}
