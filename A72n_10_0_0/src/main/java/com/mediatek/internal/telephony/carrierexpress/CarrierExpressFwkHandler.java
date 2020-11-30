package com.mediatek.internal.telephony.carrierexpress;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.telephony.Rlog;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneFactory;
import com.android.internal.util.Preconditions;
import com.mediatek.internal.telephony.MtkRIL;

public class CarrierExpressFwkHandler extends Handler {
    private static final String ACTION_CXP_SET_VENDOR_PROP = "com.mediatek.common.carrierexpress.cxp_set_vendor_prop";
    private static final String LOG_TAG = "CarrierExpress";
    private static CarrierExpressFwkHandler sInstance = null;
    private final BroadcastReceiver mCarrierExpressReceiver = new BroadcastReceiver() {
        /* class com.mediatek.internal.telephony.carrierexpress.CarrierExpressFwkHandler.AnonymousClass1 */

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Rlog.d(CarrierExpressFwkHandler.LOG_TAG, "BroadcastReceiver(), action= " + action);
            if (CarrierExpressFwkHandler.this.mPhone != null) {
                CarrierExpressFwkHandler carrierExpressFwkHandler = CarrierExpressFwkHandler.this;
                carrierExpressFwkHandler.mCi = carrierExpressFwkHandler.mPhone.mCi;
                if (CarrierExpressFwkHandler.this.mCi == null) {
                    Rlog.e(CarrierExpressFwkHandler.LOG_TAG, "MtkRIL is null");
                } else if ("com.mediatek.common.carrierexpress.cxp_reset_modem".equals(action)) {
                    CarrierExpressFwkHandler.this.startResetModem();
                } else if (CarrierExpressFwkHandler.ACTION_CXP_SET_VENDOR_PROP.equals(action)) {
                    CarrierExpressFwkHandler.this.mCi.setVendorSetting(0, intent.getStringExtra("OPTR"), null);
                    CarrierExpressFwkHandler.this.mCi.setVendorSetting(1, intent.getStringExtra("SPEC"), null);
                    CarrierExpressFwkHandler.this.mCi.setVendorSetting(2, intent.getStringExtra("SEG"), null);
                    CarrierExpressFwkHandler.this.mCi.setVendorSetting(3, intent.getStringExtra("SBP"), null);
                    CarrierExpressFwkHandler.this.mCi.setVendorSetting(4, intent.getStringExtra("SUBID"), null);
                }
            } else {
                Rlog.e(CarrierExpressFwkHandler.LOG_TAG, "phone is null, cannot reset modem");
            }
        }
    };
    private MtkRIL mCi;
    private Context mContext;
    private Phone mPhone = null;

    public CarrierExpressFwkHandler() {
        try {
            this.mPhone = PhoneFactory.getDefaultPhone();
        } catch (IllegalStateException e) {
            Rlog.e(LOG_TAG, "failed to get default phone from PhoneFactory: " + e.toString());
        }
        Preconditions.checkNotNull(this.mPhone, "default phone is null");
        this.mContext = this.mPhone.getContext();
        Preconditions.checkNotNull(this.mContext, "missing Context");
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.mediatek.common.carrierexpress.cxp_reset_modem");
        filter.addAction(ACTION_CXP_SET_VENDOR_PROP);
        this.mContext.registerReceiver(this.mCarrierExpressReceiver, filter);
    }

    public static void init() {
        synchronized (CarrierExpressFwkHandler.class) {
            if (sInstance == null) {
                sInstance = new CarrierExpressFwkHandler();
            } else {
                Rlog.d(LOG_TAG, "init() called multiple times!  sInstance = " + sInstance);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void startResetModem() {
        MtkRIL mtkRIL = this.mCi;
        if (mtkRIL != null) {
            mtkRIL.restartRILD(null);
            Rlog.d(LOG_TAG, "Reset modem");
            return;
        }
        Rlog.e(LOG_TAG, "MtkRIL is null, cannot reset modem");
    }

    /* access modifiers changed from: package-private */
    public void dispose() {
        this.mContext.unregisterReceiver(this.mCarrierExpressReceiver);
    }
}
