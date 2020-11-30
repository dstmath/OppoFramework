package com.mediatek.internal.telephony;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings;
import android.telephony.Rlog;
import android.telephony.SubscriptionManager;
import com.android.internal.content.PackageMonitor;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.SmsApplication;
import com.android.internal.telephony.SmsStorageMonitor;
import com.mediatek.internal.telephony.datasub.DataSubConstants;

public class MtkSmsStorageMonitor extends SmsStorageMonitor {
    private static final int EVENT_ME_FULL = 100;
    private static final String TAG = "MtkSmsStorageMonitor";
    private ContentObserver mContentObserver;
    private final BroadcastReceiver mMtkResultReceiver = new BroadcastReceiver() {
        /* class com.mediatek.internal.telephony.MtkSmsStorageMonitor.AnonymousClass3 */

        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED") && MtkSmsStorageMonitor.this.mPendingIccFullNotify) {
                MtkSmsStorageMonitor.this.handleIccFull();
            }
        }
    };
    private final PackageMonitor mPackageMonitor = new PackageMonitor() {
        /* class com.mediatek.internal.telephony.MtkSmsStorageMonitor.AnonymousClass1 */

        public void onPackageModified(String packageName) {
            if (MtkSmsStorageMonitor.this.mPendingIccFullNotify) {
                MtkSmsStorageMonitor.this.handleIccFull();
            }
        }
    };
    private boolean mPendingIccFullNotify = false;

    /* JADX DEBUG: Multi-variable search result rejected for r7v0, resolved type: com.mediatek.internal.telephony.MtkSmsStorageMonitor */
    /* JADX WARN: Multi-variable type inference failed */
    public MtkSmsStorageMonitor(Phone phone) {
        super(phone);
        if (this.mCi != null && (this.mCi instanceof MtkRIL)) {
            this.mCi.setOnMeSmsFull(this, 100, null);
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.BOOT_COMPLETED");
        this.mContext.registerReceiver(this.mMtkResultReceiver, filter);
        Uri defaultSmsAppUri = Settings.Secure.getUriFor("sms_default_application");
        this.mContentObserver = new ContentObserver(new Handler(Looper.getMainLooper())) {
            /* class com.mediatek.internal.telephony.MtkSmsStorageMonitor.AnonymousClass2 */

            @Override // android.database.ContentObserver
            public void onChange(boolean selfChange, Uri uri, int userId) {
                if (MtkSmsStorageMonitor.this.mPendingIccFullNotify) {
                    MtkSmsStorageMonitor.this.handleIccFull();
                }
            }
        };
        this.mContext.getContentResolver().registerContentObserver(defaultSmsAppUri, false, this.mContentObserver, -1);
        this.mPackageMonitor.register(this.mContext, this.mContext.getMainLooper(), UserHandle.ALL, false);
    }

    /* JADX DEBUG: Multi-variable search result rejected for r2v0, resolved type: com.mediatek.internal.telephony.MtkSmsStorageMonitor */
    /* JADX WARN: Multi-variable type inference failed */
    public void dispose() {
        Rlog.d(TAG, "disposed...");
        if (this.mCi != null && (this.mCi instanceof MtkRIL)) {
            this.mCi.unSetOnMeSmsFull(this);
        }
        this.mContext.unregisterReceiver(this.mMtkResultReceiver);
        this.mContext.getContentResolver().unregisterContentObserver(this.mContentObserver);
        this.mPackageMonitor.unregister();
        MtkSmsStorageMonitor.super.dispose();
    }

    public void handleIccFull() {
        if (SmsApplication.getDefaultSimFullApplication(this.mContext, false) == null) {
            this.mPendingIccFullNotify = true;
            Rlog.d(TAG, "ComponentName is NULL");
        } else if (!((UserManager) this.mContext.getSystemService(DataSubConstants.REASON_MOBILE_DATA_ENABLE_USER)).isUserUnlocked()) {
            this.mPendingIccFullNotify = true;
            Rlog.d(TAG, "too early, wait for boot complete to send broadcast");
        } else {
            this.mPendingIccFullNotify = false;
            Rlog.d(TAG, "handleIccFull");
            MtkSmsStorageMonitor.super.handleIccFull();
        }
    }

    public void handleMessage(Message msg) {
        int i = msg.what;
        if (i == 3) {
            Rlog.v(TAG, "Sending pending memory status report : mStorageAvailable = " + this.mStorageAvailable);
            this.mCi.reportSmsMemoryStatus(this.mStorageAvailable, obtainMessage(2));
        } else if (i != 100) {
            MtkSmsStorageMonitor.super.handleMessage(msg);
        } else {
            handleMeFull();
        }
    }

    private void handleMeFull() {
        Intent intent = new Intent("android.provider.Telephony.SMS_REJECTED");
        intent.putExtra("result", 3);
        SubscriptionManager.putPhoneIdAndSubIdExtra(intent, this.mPhone.getPhoneId());
        this.mWakeLock.acquire(5000);
        this.mContext.sendBroadcast(intent, "android.permission.RECEIVE_SMS");
    }
}
