package com.qualcomm.qti.internal.telephony;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.os.Handler;
import com.android.internal.telephony.SmsUsageMonitor;
import com.android.internal.telephony.SmsUsageMonitor.SmsAuthorizationCallback;
import com.qualcomm.qti.internal.telephony.QtiSmsSecurityService.SmsSecurityServiceCallback;

public class QtiSmsUsageMonitor extends SmsUsageMonitor {
    private final QtiSmsSecurityService mSmsSecurityService;

    private static final class CallBackAdapter implements SmsSecurityServiceCallback {
        private final SmsAuthorizationCallback mTarget;

        public CallBackAdapter(SmsAuthorizationCallback callback) {
            this.mTarget = callback;
        }

        public void onAuthorizationResult(boolean authorized) {
            this.mTarget.onAuthorizationResult(authorized);
        }
    }

    public QtiSmsUsageMonitor(Context context, QtiSmsSecurityService service) {
        super(context);
        this.mSmsSecurityService = service;
    }

    public void authorizeOutgoingSms(PackageInfo packageInfo, String destinationAddress, String message, SmsAuthorizationCallback callback, Handler callbackHandler) {
        this.mSmsSecurityService.requestAuthorization(packageInfo, destinationAddress, message, new CallBackAdapter(callback), callbackHandler);
    }
}
