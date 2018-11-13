package com.qualcomm.qti.internal.telephony;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.IBinder.DeathRecipient;
import android.os.RemoteException;
import android.telephony.Rlog;
import com.android.internal.telephony.ISmsSecurityAgent;
import com.android.internal.telephony.ISmsSecurityService.Stub;
import com.android.internal.telephony.SmsAuthorizationRequest;
import java.util.concurrent.ConcurrentHashMap;

public class QtiSmsSecurityService extends Stub {
    private static final String LOG_TAG = QtiSmsSecurityService.class.getSimpleName();
    public static final String SERVICE_NAME = "sms-sec";
    private volatile SecurityAgentRecord mAgentRecord;
    private final Context mContext;
    private final ConcurrentHashMap<IBinder, PendingRequestRecord> mPendingRequests = new ConcurrentHashMap();
    private final long mTimeoutMs;

    private static final class PendingRequestRecord {
        private final SmsSecurityServiceCallback mCallback;
        private final Handler mHandler;
        private final Runnable mTimeoutCallback;

        public PendingRequestRecord(final QtiSmsSecurityService service, final SmsAuthorizationRequest request, SmsSecurityServiceCallback callback, Handler callbackHandler) {
            this.mCallback = callback;
            this.mHandler = callbackHandler;
            this.mTimeoutCallback = new Runnable() {
                public void run() {
                    service.onRequestTimeout(request);
                }
            };
        }

        public void invokeCallback(final boolean authorized) {
            cancelTimeout();
            this.mHandler.post(new Runnable() {
                public void run() {
                    PendingRequestRecord.this.mCallback.onAuthorizationResult(authorized);
                }
            });
        }

        public void invokeTimeout() {
            this.mCallback.onAuthorizationResult(true);
        }

        public void scheduleTimeout(long delayMillis) {
            this.mHandler.postDelayed(this.mTimeoutCallback, delayMillis);
        }

        public void cancelTimeout() {
            this.mHandler.removeCallbacks(this.mTimeoutCallback);
        }
    }

    private static final class SecurityAgentRecord implements DeathRecipient {
        private final ISmsSecurityAgent mAgent;
        private final QtiSmsSecurityService mService;

        public SecurityAgentRecord(ISmsSecurityAgent agent, QtiSmsSecurityService monitor) throws RemoteException {
            this.mAgent = agent;
            this.mService = monitor;
            this.mAgent.asBinder().linkToDeath(this, 0);
        }

        public void binderDied() {
            this.mService.doUnregisterSafe(this.mAgent);
        }
    }

    public interface SmsSecurityServiceCallback {
        void onAuthorizationResult(boolean z);
    }

    public QtiSmsSecurityService(Context context) {
        this.mTimeoutMs = (long) context.getResources().getInteger(17694856);
        this.mContext = context;
    }

    public boolean register(ISmsSecurityAgent agent) throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission("com.qti.permission.AUTHORIZE_OUTGOING_SMS", LOG_TAG);
        boolean registered = false;
        synchronized (this) {
            if (!(this.mAgentRecord == null || (this.mAgentRecord.mAgent.asBinder().equals(agent.asBinder()) ^ 1) == 0)) {
                unregister(this.mAgentRecord.mAgent);
                this.mAgentRecord = null;
            }
            if (this.mAgentRecord == null) {
                this.mAgentRecord = new SecurityAgentRecord(agent, this);
                registered = true;
            }
        }
        return registered;
    }

    public boolean unregister(ISmsSecurityAgent agent) throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission("com.qti.permission.AUTHORIZE_OUTGOING_SMS", LOG_TAG);
        return doUnregisterSafe(agent);
    }

    public boolean sendResponse(SmsAuthorizationRequest request, boolean authorized) throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission("com.qti.permission.AUTHORIZE_OUTGOING_SMS", LOG_TAG);
        PendingRequestRecord record = (PendingRequestRecord) this.mPendingRequests.remove(request.getToken());
        if (record != null) {
            record.invokeCallback(authorized);
        }
        return record != null;
    }

    public void requestAuthorization(PackageInfo packageInfo, String destinationAddress, String message, SmsSecurityServiceCallback callback, Handler callbackHandler) {
        boolean requested = false;
        IBinder token = new Binder();
        SmsAuthorizationRequest request = new SmsAuthorizationRequest(this, token, packageInfo.packageName, destinationAddress, message);
        PendingRequestRecord requestRecord = new PendingRequestRecord(this, request, callback, callbackHandler);
        this.mPendingRequests.put(token, requestRecord);
        SecurityAgentRecord record = this.mAgentRecord;
        if (record != null) {
            requestRecord.scheduleTimeout(this.mTimeoutMs);
            try {
                record.mAgent.onAuthorize(request);
                requested = true;
            } catch (RemoteException e) {
                Rlog.e(LOG_TAG, "Unable to request SMS authentication.", e);
                requestRecord.cancelTimeout();
            }
        }
        if (!requested) {
            PendingRequestRecord failedRequest = (PendingRequestRecord) this.mPendingRequests.remove(request.getToken());
            if (failedRequest != null) {
                failedRequest.invokeCallback(true);
            }
        }
    }

    protected void onRequestTimeout(SmsAuthorizationRequest request) {
        PendingRequestRecord record = (PendingRequestRecord) this.mPendingRequests.remove(request.getToken());
        if (record != null) {
            record.invokeTimeout();
        }
    }

    private boolean doUnregisterSafe(ISmsSecurityAgent agent) {
        boolean unregistered = false;
        synchronized (this) {
            if (this.mAgentRecord != null && this.mAgentRecord.mAgent.asBinder().equals(agent.asBinder())) {
                this.mAgentRecord.mAgent.asBinder().unlinkToDeath(this.mAgentRecord, 0);
                this.mAgentRecord = null;
                unregistered = true;
            }
        }
        return unregistered;
    }
}
