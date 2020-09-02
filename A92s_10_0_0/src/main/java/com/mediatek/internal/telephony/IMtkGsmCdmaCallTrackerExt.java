package com.mediatek.internal.telephony;

import android.os.Bundle;
import com.android.internal.telephony.Connection;

public interface IMtkGsmCdmaCallTrackerExt {
    boolean areConnectionsInSameLine(Connection[] connectionArr);

    String convertAddress(String str);

    String convertDialString(Bundle bundle, String str);

    Bundle getAddressExtras(String str);

    boolean isAddressChanged(boolean z, String str, String str2);

    boolean isAddressChanged(boolean z, String str, String str2, String str3);
}
