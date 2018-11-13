package com.android.internal.telephony.cat;

/* compiled from: CommandDetails */
class ActivateDescriptor extends ValueObject {
    public int target;

    ActivateDescriptor() {
    }

    ComprehensionTlvTag getTag() {
        return ComprehensionTlvTag.ACTIVATE_DESCRIPTOR;
    }
}
