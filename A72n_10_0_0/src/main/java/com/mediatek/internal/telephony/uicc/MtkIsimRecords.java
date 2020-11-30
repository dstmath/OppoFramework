package com.mediatek.internal.telephony.uicc;

import android.os.Message;
import com.android.internal.telephony.uicc.IsimRecords;

public interface MtkIsimRecords extends IsimRecords {
    byte[] getEfPsismsc();

    String getIsimGbabp();

    void setIsimGbabp(String str, Message message);
}
