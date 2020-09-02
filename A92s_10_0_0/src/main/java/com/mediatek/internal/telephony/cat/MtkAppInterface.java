package com.mediatek.internal.telephony.cat;

import com.android.internal.telephony.cat.AppInterface;
import com.android.internal.telephony.uicc.IccRecords;

public interface MtkAppInterface extends AppInterface {
    public static final String MTK_CAT_CMD_ACTION = "com.mediatek.internal.stk.command";

    IccRecords getIccRecords();

    void onDBHandler(int i);

    void onEventDownload(MtkCatResponseMessage mtkCatResponseMessage);

    void onLaunchCachedSetupMenu();
}
