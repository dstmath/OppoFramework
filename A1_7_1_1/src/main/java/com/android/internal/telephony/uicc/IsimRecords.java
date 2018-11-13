package com.android.internal.telephony.uicc;

import android.os.Message;

public interface IsimRecords {
    byte[] getEfPsismsc();

    String getIsimChallengeResponse(String str);

    String getIsimDomain();

    String getIsimGbabp();

    String getIsimImpi();

    String[] getIsimImpu();

    String getIsimIst();

    String[] getIsimPcscf();

    void setIsimGbabp(String str, Message message);
}
