package com.mediatek.internal.telephony;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.telephony.CarrierConfigManager;
import android.telephony.Rlog;
import com.android.internal.telephony.BlockChecker;
import com.android.internal.telephony.CallerInfo;
import com.android.internal.telephony.CallerInfoAsyncQuery;

public class MtkIncomingCallChecker {
    private static final String PROP_LOG_TAG = "MTCallChecker";
    private static final int TOKEN_MT_CHECKER = 256;
    AsyncBlockCheckTask blockChecker = null;
    OnCheckCompleteListener mCallback = null;
    String mName;
    Object obj = null;

    public interface OnCheckCompleteListener {
        void onCheckComplete(boolean z, Object obj);
    }

    public MtkIncomingCallChecker(String name, Object obj2) {
        this.mName = name;
        this.obj = obj2;
    }

    public boolean startIncomingCallNumberCheck(Context context, int subId, final String number, OnCheckCompleteListener callback) {
        if (context == null) {
            proprietaryLogE("cannot do checkIncomingCallNumber (context=null, subId=" + subId + ", number=" + number + "), call will enter");
            return false;
        } else if (callback == null) {
            proprietaryLogE("checkIncomingCallNumber callback null, call will enter");
            return false;
        } else if (number == null || number.isEmpty()) {
            proprietaryLog("checkIncomingCallNumber skipped (number=" + number + ")");
            return false;
        } else {
            this.mCallback = callback;
            if (this.blockChecker != null) {
                proprietaryLog("block checker not null (" + this.blockChecker.toString() + "). will create a new one.");
            }
            this.blockChecker = new AsyncBlockCheckTask(context);
            if (!isMtkEnhancedCallBlockingEnabled(context, subId)) {
                return false;
            }
            MtkCallerInfoAsyncQuery.startQuery(256, context, number, new CallerInfoAsyncQuery.OnQueryCompleteListener() {
                /* class com.mediatek.internal.telephony.MtkIncomingCallChecker.AnonymousClass1 */

                public void onQueryComplete(int token, Object cookie, CallerInfo info) {
                    MtkIncomingCallChecker.this.blockChecker.execute(number, String.valueOf(1), String.valueOf(info == null ? false : info.contactExists));
                }
            }, "ContactQuery", subId);
            return true;
        }
    }

    /* access modifiers changed from: package-private */
    public void onBlockCheckComplete(Boolean isBlocked) {
        proprietaryLog("query result, isBlocked=" + isBlocked);
        OnCheckCompleteListener onCheckCompleteListener = this.mCallback;
        if (onCheckCompleteListener != null) {
            onCheckCompleteListener.onCheckComplete(isBlocked.booleanValue(), this.obj);
        }
    }

    static boolean isMtkEnhancedCallBlockingEnabled(Context context, int subId) {
        if (context == null) {
            proprietaryLog("isMtkEnhancedCallBlockingEnabled fail, return false (context null)");
            return false;
        }
        PersistableBundle carrierConfig = ((CarrierConfigManager) context.getSystemService("carrier_config")).getConfigForSubId(subId);
        if (carrierConfig == null) {
            carrierConfig = CarrierConfigManager.getDefaultConfig();
        }
        return carrierConfig.getBoolean("mtk_support_enhanced_call_blocking_bool");
    }

    static void proprietaryLog(String s) {
        Rlog.d(PROP_LOG_TAG, s);
    }

    static void proprietaryLogE(String s) {
        Rlog.e(PROP_LOG_TAG, s);
    }

    static void proprietaryLogI(String s) {
        Rlog.i(PROP_LOG_TAG, s);
    }

    /* access modifiers changed from: package-private */
    public class AsyncBlockCheckTask extends AsyncTask<String, Void, Boolean> {
        private Context mContext;

        public AsyncBlockCheckTask(Context context) {
            this.mContext = context;
        }

        /* access modifiers changed from: protected */
        public Boolean doInBackground(String... params) {
            String number = "";
            Bundle extras = new Bundle();
            if (params.length > 0) {
                number = params[0];
            }
            if (params.length > 1) {
                extras.putInt("extra_call_presentation", Integer.valueOf(params[1]).intValue());
            }
            if (params.length > 2) {
                extras.putBoolean("extra_contact_exist", Boolean.valueOf(params[2]).booleanValue());
            }
            return Boolean.valueOf(BlockChecker.isBlocked(this.mContext, number, extras));
        }

        /* access modifiers changed from: protected */
        public void onPostExecute(Boolean isBlocked) {
            MtkIncomingCallChecker.this.onBlockCheckComplete(Boolean.valueOf(isBlocked.booleanValue()));
        }
    }
}
