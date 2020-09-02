package com.mediatek.internal.telephony;

import mediatek.telephony.ISignalStrengthExt;

public class MtkOpTelephonyCustomizationFactoryBase {
    public ISignalStrengthExt makeSignalStrengthExt() {
        return new SignalStrengthExt();
    }
}
