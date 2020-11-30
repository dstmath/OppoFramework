package com.mediatek.internal.telephony;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.os.ServiceManager;
import android.telephony.Rlog;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.telephony.data.ApnSetting;
import android.text.TextUtils;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.RetryManager;
import com.android.internal.telephony.TelephonyDevController;
import com.mediatek.ims.internal.IMtkImsService;
import com.mediatek.internal.telephony.dataconnection.DcFailCauseManager;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public final class MtkRetryManager extends RetryManager {
    public static final String LOG_TAG = "MtkRetryManager";
    private static IMtkImsService mMtkImsService = null;
    private boolean mBcastRegistered = false;
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        /* class com.mediatek.internal.telephony.MtkRetryManager.AnonymousClass1 */
        private final AtomicInteger mPreviousSubId = new AtomicInteger(-1);

        public void onReceive(Context context, Intent intent) {
            Rlog.d(MtkRetryManager.LOG_TAG, "mBroadcastReceiver: action " + intent.getAction() + ", mSameApnRetryCount:" + MtkRetryManager.this.mSameApnRetryCount + ", mModemSuggestedDelay:" + MtkRetryManager.this.mModemSuggestedDelay + ", mCurrentApnIndex:" + MtkRetryManager.this.mCurrentApnIndex);
            if (intent.getAction().equals("android.telephony.action.CARRIER_CONFIG_CHANGED")) {
                int subId = intent.getIntExtra("subscription", -1);
                if (SubscriptionManager.isValidSubscriptionId(subId) && subId == MtkRetryManager.this.mPhone.getSubId() && this.mPreviousSubId.getAndSet(subId) != subId) {
                    int sameApnRetryCountTemp = MtkRetryManager.this.mSameApnRetryCount;
                    long modemSuggestedDelayTemp = MtkRetryManager.this.mModemSuggestedDelay;
                    int currentApnIndexTemp = MtkRetryManager.this.mCurrentApnIndex;
                    MtkRetryManager.this.configureRetry();
                    MtkRetryManager.this.mSameApnRetryCount = sameApnRetryCountTemp;
                    MtkRetryManager.this.mModemSuggestedDelay = modemSuggestedDelayTemp;
                    MtkRetryManager.this.mCurrentApnIndex = currentApnIndexTemp;
                }
            }
        }
    };
    private Context mContext;
    private DcFailCauseManager mDcFcMgr = DcFailCauseManager.getInstance(this.mPhone);
    private int mPhoneNum = TelephonyManager.getDefault().getPhoneCount();
    private TelephonyDevController mTelDevController = TelephonyDevController.getInstance();

    static {
        MAX_SAME_APN_RETRY = 100;
    }

    public MtkRetryManager(Phone phone, String apnType) {
        super(phone, apnType);
        if (mMtkImsService == null) {
            checkAndBindImsService();
        }
    }

    /* access modifiers changed from: protected */
    public void finalize() {
        Rlog.d(LOG_TAG, "RetryManager finalized");
        this.mContext.unregisterReceiver(this.mBroadcastReceiver);
    }

    /* access modifiers changed from: protected */
    public void configureRetry() {
        MtkRetryManager.super.configureRetry();
        if (this.mWaitingApns == null || this.mWaitingApns.size() == 0) {
            Rlog.e(LOG_TAG, "configureRetry: mWaitingApns is null or empty");
        } else {
            int index = this.mCurrentApnIndex;
            if (index < 0 || index >= this.mWaitingApns.size()) {
                index = 0;
            }
            Rlog.d(LOG_TAG, "configureRetry: mCurrentApnIndex: " + this.mCurrentApnIndex + ", reset MD data count for apn: " + ((ApnSetting) this.mWaitingApns.get(index)).getApnName());
            this.mPhone.mCi.resetMdDataRetryCount(((ApnSetting) this.mWaitingApns.get(index)).getApnName(), null);
        }
        TelephonyDevController telephonyDevController = this.mTelDevController;
        if (telephonyDevController != null && telephonyDevController.getModem(0) != null && this.mTelDevController.getModem(0).hasMdAutoSetupImsCapability()) {
            if (TextUtils.equals("ims", this.mApnType) || TextUtils.equals("emergency", this.mApnType)) {
                int i = this.mPhoneNum;
                int[] iArr = new int[i];
                int[] iArr2 = new int[i];
                try {
                    int[] getImsState = mMtkImsService.getImsNetworkState(4);
                    int[] getEImsState = mMtkImsService.getImsNetworkState(10);
                    if ((TextUtils.equals("ims", this.mApnType) && getImsState[this.mPhone.getPhoneId()] == NetworkInfo.State.DISCONNECTED.ordinal()) || (TextUtils.equals("emergency", this.mApnType) && getEImsState[this.mPhone.getPhoneId()] == NetworkInfo.State.DISCONNECTED.ordinal())) {
                        Rlog.d(LOG_TAG, "configureRetry: IMS/EIMS and disconnected, no retry by mobile.");
                        configure("max_retries=0, -1, -1, -1");
                    }
                } catch (Exception e) {
                    Rlog.d(LOG_TAG, "getImsNetworkState failed.");
                }
            }
        }
    }

    public long getDelayForNextApn(boolean failFastEnabled) {
        long delay;
        DcFailCauseManager dcFailCauseManager;
        if (this.mWaitingApns == null || this.mWaitingApns.size() == 0) {
            log("Waiting APN list is null or empty.");
            return -1;
        } else if (this.mModemSuggestedDelay == -1) {
            log("Modem suggested not retrying.");
            return -1;
        } else if (this.mModemSuggestedDelay == -2 || this.mSameApnRetryCount >= MAX_SAME_APN_RETRY) {
            int index = this.mCurrentApnIndex;
            do {
                index++;
                if (index >= this.mWaitingApns.size()) {
                    index = 0;
                }
                if (!((ApnSetting) this.mWaitingApns.get(index)).getPermanentFailed()) {
                    if (index > this.mCurrentApnIndex) {
                        delay = this.mInterApnDelay;
                    } else if (this.mRetryForever || this.mRetryCount + 1 <= this.mMaxRetryCount) {
                        delay = (long) getRetryTimer();
                        this.mRetryCount++;
                    } else {
                        log("Reached maximum retry count " + this.mMaxRetryCount + ".");
                        return -1;
                    }
                    if (!failFastEnabled || delay <= this.mFailFastInterApnDelay) {
                        return delay;
                    }
                    return this.mFailFastInterApnDelay;
                }
            } while (index != this.mCurrentApnIndex);
            log("All APNs have permanently failed.");
            return -1;
        } else if (this.mModemSuggestedDelay == ((long) DcFailCauseManager.retryConfigForCC33.retryTime.getValue()) && (dcFailCauseManager = this.mDcFcMgr) != null && dcFailCauseManager.isNetworkOperatorForCC33() && this.mSameApnRetryCount >= DcFailCauseManager.retryConfigForCC33.maxRetryCount.getValue()) {
            return -1;
        } else {
            log("Modem suggested retry in " + this.mModemSuggestedDelay + " ms.");
            return this.mModemSuggestedDelay;
        }
    }

    public void setWaitingApns(ArrayList<ApnSetting> waitingApns) {
        MtkRetryManager.super.setWaitingApns(waitingApns);
        this.mContext = this.mPhone.getContext();
        if (!this.mBcastRegistered) {
            this.mContext.registerReceiver(this.mBroadcastReceiver, new IntentFilter("android.telephony.action.CARRIER_CONFIG_CHANGED"));
            this.mBcastRegistered = true;
        }
    }

    private void checkAndBindImsService() {
        IBinder b = ServiceManager.getService("mtkIms");
        if (b != null) {
            mMtkImsService = IMtkImsService.Stub.asInterface(b);
            if (mMtkImsService != null) {
                Rlog.d(LOG_TAG, "checkAndBindImsService: mMtkImsService = " + mMtkImsService);
            }
        }
    }
}
