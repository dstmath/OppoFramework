package com.mediatek.internal.telephony.uicc;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.telephony.Rlog;
import com.android.internal.telephony.Phone;

public class IccFileAdapter {
    private static final String TAG = "IccFileAdapter";
    private static IccFileAdapter sInstance;
    private Context mContext;
    private Phone mPhone;
    private int mPhoneId;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        /* class com.mediatek.internal.telephony.uicc.IccFileAdapter.AnonymousClass1 */

        public void onReceive(Context context, Intent intent) {
        }
    };

    public IccFileAdapter(Context c, Phone phone) {
        log("IccFileAdapter Creating!");
        this.mContext = c;
        this.mPhone = phone;
        this.mPhoneId = this.mPhone.getPhoneId();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.SIM_STATE_CHANGED");
        filter.addAction("android.intent.action.RADIO_TECHNOLOGY");
        this.mContext.registerReceiver(this.mReceiver, filter);
    }

    /* access modifiers changed from: protected */
    public void log(String msg) {
        Rlog.d(TAG, msg + " (phoneId " + this.mPhoneId + ")");
    }

    /* access modifiers changed from: protected */
    public void loge(String msg) {
        Rlog.e(TAG, msg + " (phoneId " + this.mPhoneId + ")");
    }
}
