package com.mediatek.internal.telephony;

import android.content.Context;
import android.os.Bundle;
import android.telephony.Rlog;
import com.android.internal.telephony.Connection;

public class MtkGsmCdmaCallTrackerExt implements IMtkGsmCdmaCallTrackerExt {
    static final String TAG = "GsmCdmaCallTkrExt";
    protected Context mContext;

    public MtkGsmCdmaCallTrackerExt() {
    }

    public MtkGsmCdmaCallTrackerExt(Context context) {
        this.mContext = context;
    }

    public void log(String text) {
        Rlog.d(TAG, text);
    }

    @Override // com.mediatek.internal.telephony.IMtkGsmCdmaCallTrackerExt
    public String convertDialString(Bundle intentExtras, String destination) {
        return null;
    }

    @Override // com.mediatek.internal.telephony.IMtkGsmCdmaCallTrackerExt
    public String convertAddress(String formatNumber) {
        return null;
    }

    protected static boolean equalsHandlesNulls(Object a, Object b) {
        if (a == null) {
            return b == null;
        }
        return a.equals(b);
    }

    protected static boolean equalsBaseDialString(String a, String b) {
        if (a == null) {
            if (b == null) {
                return true;
            }
        } else if (b != null && a.startsWith(b)) {
            return true;
        }
        return false;
    }

    @Override // com.mediatek.internal.telephony.IMtkGsmCdmaCallTrackerExt
    public boolean isAddressChanged(boolean converted, String dcNumber, String address, String convertedNumber) {
        if (equalsBaseDialString(address, dcNumber)) {
            return false;
        }
        if (!converted || !equalsBaseDialString(convertedNumber, dcNumber)) {
            return true;
        }
        return false;
    }

    @Override // com.mediatek.internal.telephony.IMtkGsmCdmaCallTrackerExt
    public boolean isAddressChanged(boolean converted, String dcAddress, String address) {
        if (!equalsHandlesNulls(address, dcAddress)) {
            return true;
        }
        return false;
    }

    @Override // com.mediatek.internal.telephony.IMtkGsmCdmaCallTrackerExt
    public Bundle getAddressExtras(String formatNumber) {
        return null;
    }

    @Override // com.mediatek.internal.telephony.IMtkGsmCdmaCallTrackerExt
    public boolean areConnectionsInSameLine(Connection[] connections) {
        return true;
    }
}
