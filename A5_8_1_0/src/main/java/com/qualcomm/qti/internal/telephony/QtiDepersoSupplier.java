package com.qualcomm.qti.internal.telephony;

import android.content.Context;
import android.util.Log;
import org.codeaurora.internal.IDepersoResCallback;

public class QtiDepersoSupplier {
    private static final String LOG_TAG = "QtiDepersoSupplier";
    private static QtiDepersoSupplier sInstance;
    private Context mContext;
    private QtiRilInterface mQtiRilInterface;

    public static QtiDepersoSupplier make(Context context) {
        if (sInstance == null) {
            sInstance = new QtiDepersoSupplier(context);
        } else {
            Log.wtf(LOG_TAG, "QtiDepersoSupplier.make() should be called once");
        }
        return sInstance;
    }

    public static QtiDepersoSupplier getInstance() {
        if (sInstance == null) {
            Log.e(LOG_TAG, "QtiDepersoSupplier.getInstance called before make");
        }
        return sInstance;
    }

    private QtiDepersoSupplier(Context context) {
        this.mContext = context;
        this.mQtiRilInterface = QtiRilInterface.getInstance(context);
    }

    public void supplyIccDepersonalization(String netpin, String type, IDepersoResCallback callback, int phoneId) {
        if (this.mQtiRilInterface.isServiceReady()) {
            Log.d(LOG_TAG, "supplyIccDepersonalization");
            this.mQtiRilInterface.supplyIccDepersonalization(netpin, type, callback, phoneId);
            return;
        }
        Log.d(LOG_TAG, "Oem hook service is not ready yet ");
    }
}
