package com.mediatek.android.mms.pdu;

import com.google.android.mms.pdu.PduPart;

public class MtkPduPart extends PduPart {
    public static final String CONTENT_DISPOSITION = "Content-Disposition";
    public static final String CONTENT_ID = "Content-ID";
    public static final String CONTENT_LOCATION = "Content-Location";
    public static final String CONTENT_TYPE = "Content-Type";
    public static final String PARA_NAME = "Name";
    public static final int P_DATE = 146;
    public static final int P_TRANSFER_ENCODING = 167;
    public static final int P_X_WAP_CONTENT_URI = 176;
    private boolean mNeedUpdate = true;
}
