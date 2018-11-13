package android.telephony;

import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import com.android.internal.telephony.ITelephony;
import com.android.internal.telephony.ITelephony.Stub;

public abstract class CellLocation {
    protected int mType;

    public abstract void fillInNotifierBundle(Bundle bundle);

    public abstract boolean isEmpty();

    public abstract void setStateInvalid();

    public static void requestLocationUpdate() {
        try {
            ITelephony phone = Stub.asInterface(ServiceManager.getService("phone"));
            if (phone != null) {
                phone.updateServiceLocation();
            }
        } catch (RemoteException e) {
        }
    }

    public static CellLocation newFromBundle(Bundle bundle) {
        int phoneType;
        try {
            phoneType = TelephonyManager.getDefault().getCurrentPhoneType(SubscriptionManager.getDefaultDataSubscriptionId());
            if (bundle != null && bundle.containsKey("type")) {
                int phoneTypeFromBundle = bundle.getInt("type", 0);
                if (phoneTypeFromBundle != 0) {
                    phoneType = phoneTypeFromBundle;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            phoneType = TelephonyManager.getDefault().getCurrentPhoneType();
        }
        Rlog.d("CellLocation", "phoneType=" + phoneType);
        switch (phoneType) {
            case 1:
                return new GsmCellLocation(bundle);
            case 2:
                return new CdmaCellLocation(bundle);
            default:
                return null;
        }
    }

    public static CellLocation newFromBundle(Bundle bundle, String vCardType) {
        int vPhoneType = bundle.getInt("type", 0);
        if ("CSIM".equals(vCardType) || "RUIM".equals(vCardType) || vPhoneType == 2) {
            return new CdmaCellLocation(bundle);
        }
        return new GsmCellLocation(bundle);
    }

    public static CellLocation getEmpty() {
        switch (TelephonyManager.getDefault().getCurrentPhoneType()) {
            case 1:
                return new GsmCellLocation();
            case 2:
                return new CdmaCellLocation();
            default:
                return null;
        }
    }
}
