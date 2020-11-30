package com.mediatek.internal.telephony.uicc;

import com.android.internal.telephony.uicc.IccRefreshResponse;

public class MtkIccRefreshResponse extends IccRefreshResponse {
    public static final int REFRESH_INIT_FILE_UPDATED = 5;
    public static final int REFRESH_INIT_FULL_FILE_UPDATED = 4;
    public static final int REFRESH_RESULT_APP_INIT = 3;
    public static final int REFRESH_SESSION_RESET = 6;
}
