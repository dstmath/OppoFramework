package com.mediatek.internal.telephony;

import android.os.Message;
import android.os.Parcel;

public class MtkSuppSrvRequest {
    public static final int SUPP_SRV_REQ_CFU_QUERY = 18;
    public static final int SUPP_SRV_REQ_GET_CB = 10;
    public static final int SUPP_SRV_REQ_GET_CF = 12;
    public static final int SUPP_SRV_REQ_GET_CF_IN_TIME_SLOT = 16;
    public static final int SUPP_SRV_REQ_GET_CLIP = 2;
    public static final int SUPP_SRV_REQ_GET_CLIR = 4;
    public static final int SUPP_SRV_REQ_GET_COLP = 6;
    public static final int SUPP_SRV_REQ_GET_COLR = 8;
    public static final int SUPP_SRV_REQ_GET_CW = 14;
    public static final int SUPP_SRV_REQ_MMI_CODE = 15;
    public static final int SUPP_SRV_REQ_SET_CB = 9;
    public static final int SUPP_SRV_REQ_SET_CF = 11;
    public static final int SUPP_SRV_REQ_SET_CF_IN_TIME_SLOT = 17;
    public static final int SUPP_SRV_REQ_SET_CLIP = 1;
    public static final int SUPP_SRV_REQ_SET_CLIR = 3;
    public static final int SUPP_SRV_REQ_SET_COLP = 5;
    public static final int SUPP_SRV_REQ_SET_COLR = 7;
    public static final int SUPP_SRV_REQ_SET_CW = 13;
    public Parcel mParcel;
    int mRequestCode;
    Message mResultCallback;

    public static MtkSuppSrvRequest obtain(int request, Message resultCallback) {
        MtkSuppSrvRequest ss = new MtkSuppSrvRequest();
        ss.mRequestCode = request;
        ss.mResultCallback = resultCallback;
        ss.mParcel = Parcel.obtain();
        return ss;
    }

    private MtkSuppSrvRequest() {
    }

    public Message getResultCallback() {
        return this.mResultCallback;
    }

    public void setResultCallback(Message resultCallback) {
        this.mResultCallback = resultCallback;
    }

    public int getRequestCode() {
        return this.mRequestCode;
    }
}
