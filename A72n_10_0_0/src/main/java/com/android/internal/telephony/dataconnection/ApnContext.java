package com.android.internal.telephony.dataconnection;

import android.app.PendingIntent;
import android.net.NetworkCapabilities;
import android.net.NetworkConfig;
import android.net.NetworkRequest;
import android.os.Bundle;
import android.os.Message;
import android.os.SystemProperties;
import android.telephony.Rlog;
import android.telephony.data.ApnSetting;
import android.text.TextUtils;
import android.util.LocalLog;
import android.util.SparseIntArray;
import com.android.internal.telephony.DctConstants;
import com.android.internal.telephony.OppoModemLogManager;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneInternalInterface;
import com.android.internal.telephony.RetryManager;
import com.android.internal.telephony.TelephonyComponentFactory;
import com.android.internal.util.IndentingPrintWriter;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class ApnContext {
    protected static final boolean DBG = false;
    private static final String SLOG_TAG = "ApnContext";
    private static Method sMethodGetApnTypeFromNetworkRequestEx;
    private static Method sMethodGetApnTypeFromNetworkTypeEx;
    public final String LOG_TAG;
    private ApnSetting mApnSetting;
    protected final String mApnType;
    private boolean mConcurrentVoiceAndDataAllowed;
    private final AtomicInteger mConnectionGeneration = new AtomicInteger(0);
    private DataConnection mDataConnection;
    AtomicBoolean mDataEnabled;
    private final DcTracker mDcTracker;
    AtomicBoolean mDependencyMet;
    private final LocalLog mLocalLog = new LocalLog(150);
    private final ArrayList<NetworkRequest> mNetworkRequests = new ArrayList<>();
    private final Phone mPhone;
    String mReason;
    PendingIntent mReconnectAlarmIntent;
    private int mRefCount = 0;
    private final Object mRefCountLock = new Object();
    private final SparseIntArray mRetriesLeftPerErrorCode = new SparseIntArray();
    private final RetryManager mRetryManager;
    private DctConstants.State mState;
    private final LocalLog mStateLocalLog = new LocalLog(50);
    public final int priority;

    static {
        if (SystemProperties.get("ro.vendor.mtk_telephony_add_on_policy", OppoModemLogManager.DEFAULT_MODEMDUMP_POSTBACK).equals(OppoModemLogManager.DEFAULT_MODEMDUMP_POSTBACK)) {
            Class<?> clz = null;
            try {
                clz = Class.forName("com.mediatek.internal.telephony.dataconnection.MtkApnContext");
            } catch (Exception e) {
                Rlog.d(SLOG_TAG, e.toString());
            }
            if (clz != null) {
                try {
                    sMethodGetApnTypeFromNetworkTypeEx = clz.getDeclaredMethod("getApnTypeFromNetworkTypeEx", Integer.TYPE);
                    sMethodGetApnTypeFromNetworkTypeEx.setAccessible(true);
                } catch (Exception e2) {
                    Rlog.d(SLOG_TAG, e2.toString());
                }
                try {
                    sMethodGetApnTypeFromNetworkRequestEx = clz.getDeclaredMethod("getApnTypeFromNetworkRequestEx", NetworkCapabilities.class, Integer.TYPE, Boolean.TYPE);
                    sMethodGetApnTypeFromNetworkRequestEx.setAccessible(true);
                } catch (Exception e3) {
                    Rlog.d(SLOG_TAG, e3.toString());
                }
            }
        }
    }

    public ApnContext(Phone phone, String apnType, String logTag, NetworkConfig config, DcTracker tracker) {
        this.mPhone = phone;
        this.mApnType = apnType;
        this.mState = DctConstants.State.IDLE;
        setReason(PhoneInternalInterface.REASON_DATA_ENABLED);
        this.mDataEnabled = new AtomicBoolean(false);
        this.mDependencyMet = new AtomicBoolean(config.dependencyMet);
        this.priority = config.priority;
        this.LOG_TAG = logTag;
        this.mDcTracker = tracker;
        this.mRetryManager = TelephonyComponentFactory.getInstance().makeRetryManager(phone, apnType);
    }

    public String getApnType() {
        return this.mApnType;
    }

    public int getApnTypeBitmask() {
        return ApnSetting.getApnTypesBitmaskFromString(this.mApnType);
    }

    public synchronized DataConnection getDataConnection() {
        return this.mDataConnection;
    }

    public synchronized void setDataConnection(DataConnection dc) {
        log("setDataConnectionAc: old=" + this.mDataConnection + ",new=" + dc + " this=" + this);
        this.mDataConnection = dc;
    }

    public synchronized void releaseDataConnection(String reason) {
        if (this.mDataConnection != null) {
            this.mDataConnection.tearDown(this, reason, null);
            this.mDataConnection = null;
        }
        setState(DctConstants.State.IDLE);
    }

    public synchronized PendingIntent getReconnectIntent() {
        return this.mReconnectAlarmIntent;
    }

    public synchronized void setReconnectIntent(PendingIntent intent) {
        this.mReconnectAlarmIntent = intent;
    }

    public synchronized ApnSetting getApnSetting() {
        log("getApnSetting: apnSetting=" + this.mApnSetting);
        return this.mApnSetting;
    }

    public synchronized void setApnSetting(ApnSetting apnSetting) {
        log("setApnSetting: apnSetting=" + apnSetting);
        this.mApnSetting = apnSetting;
    }

    public synchronized void setWaitingApns(ArrayList<ApnSetting> waitingApns) {
        this.mRetryManager.setWaitingApns(waitingApns);
    }

    public ApnSetting getNextApnSetting() {
        return this.mRetryManager.getNextApnSetting();
    }

    public void setModemSuggestedDelay(long delay) {
        this.mRetryManager.setModemSuggestedDelay(delay);
    }

    public long getDelayForNextApn(boolean failFastEnabled) {
        return this.mRetryManager.getDelayForNextApn(failFastEnabled || isFastRetryReason());
    }

    public void markApnPermanentFailed(ApnSetting apn) {
        this.mRetryManager.markApnPermanentFailed(apn);
    }

    public ArrayList<ApnSetting> getWaitingApns() {
        return this.mRetryManager.getWaitingApns();
    }

    public synchronized void setConcurrentVoiceAndDataAllowed(boolean allowed) {
        this.mConcurrentVoiceAndDataAllowed = allowed;
    }

    public synchronized boolean isConcurrentVoiceAndDataAllowed() {
        return this.mConcurrentVoiceAndDataAllowed;
    }

    public synchronized void setState(DctConstants.State s) {
        log("setState: " + s + ", previous state:" + this.mState);
        if (this.mState != s) {
            LocalLog localLog = this.mStateLocalLog;
            localLog.log("State changed from " + this.mState + " to " + s);
            this.mState = s;
        }
        if (this.mState == DctConstants.State.FAILED && this.mRetryManager.getWaitingApns() != null) {
            this.mRetryManager.getWaitingApns().clear();
        }
    }

    public synchronized DctConstants.State getState() {
        return this.mState;
    }

    public boolean isDisconnected() {
        DctConstants.State currentState = getState();
        return currentState == DctConstants.State.IDLE || currentState == DctConstants.State.FAILED;
    }

    public synchronized void setReason(String reason) {
        log("set reason as " + reason + ",current state " + this.mState);
        this.mReason = reason;
    }

    public synchronized String getReason() {
        return this.mReason;
    }

    public boolean isReady() {
        return this.mDataEnabled.get() && this.mDependencyMet.get();
    }

    public boolean isConnectable() {
        return isReady() && (this.mState == DctConstants.State.IDLE || this.mState == DctConstants.State.RETRYING || this.mState == DctConstants.State.FAILED);
    }

    private boolean isFastRetryReason() {
        return PhoneInternalInterface.REASON_NW_TYPE_CHANGED.equals(this.mReason) || PhoneInternalInterface.REASON_APN_CHANGED.equals(this.mReason);
    }

    public boolean isConnectedOrConnecting() {
        return isReady() && (this.mState == DctConstants.State.CONNECTED || this.mState == DctConstants.State.CONNECTING || this.mState == DctConstants.State.RETRYING);
    }

    public void setEnabled(boolean enabled) {
        log("set enabled as " + enabled + ", current state is " + this.mDataEnabled.get());
        this.mDataEnabled.set(enabled);
    }

    public boolean isEnabled() {
        return this.mDataEnabled.get();
    }

    public boolean isDependencyMet() {
        return this.mDependencyMet.get();
    }

    public boolean isProvisioningApn() {
        ApnSetting apnSetting;
        String provisioningApn = this.mPhone.getContext().getResources().getString(17040448);
        if (TextUtils.isEmpty(provisioningApn) || (apnSetting = this.mApnSetting) == null || apnSetting.getApnName() == null) {
            return false;
        }
        return this.mApnSetting.getApnName().equals(provisioningApn);
    }

    public void requestLog(String str) {
        synchronized (this.mLocalLog) {
            this.mLocalLog.log(str);
        }
    }

    public void requestNetwork(NetworkRequest networkRequest, int type, Message onCompleteMsg) {
        synchronized (this.mRefCountLock) {
            this.mNetworkRequests.add(networkRequest);
            logl("requestNetwork for " + networkRequest + ", type=" + DcTracker.requestTypeToString(type));
            this.mDcTracker.enableApn(ApnSetting.getApnTypesBitmaskFromString(this.mApnType), type, onCompleteMsg);
            if (this.mDataConnection != null) {
                this.mDataConnection.reevaluateDataConnectionProperties();
            }
        }
    }

    public void releaseNetwork(NetworkRequest networkRequest, int type) {
        synchronized (this.mRefCountLock) {
            if (!this.mNetworkRequests.contains(networkRequest)) {
                logl("releaseNetwork can't find this request (" + networkRequest + ")");
            } else {
                this.mNetworkRequests.remove(networkRequest);
                if (this.mDataConnection != null) {
                    this.mDataConnection.reevaluateDataConnectionProperties();
                }
                logl("releaseNetwork left with " + this.mNetworkRequests.size() + " requests.");
                if (this.mNetworkRequests.size() == 0 || type == 2 || type == 3) {
                    this.mDcTracker.disableApn(ApnSetting.getApnTypesBitmaskFromString(this.mApnType), type);
                }
            }
        }
    }

    public boolean hasRestrictedRequests(boolean excludeDun) {
        synchronized (this.mRefCountLock) {
            Iterator<NetworkRequest> it = this.mNetworkRequests.iterator();
            while (it.hasNext()) {
                NetworkRequest nr = it.next();
                if (!excludeDun || !nr.networkCapabilities.hasCapability(2)) {
                    if (!nr.networkCapabilities.hasCapability(13)) {
                        return true;
                    }
                }
            }
            return false;
        }
    }

    public void resetErrorCodeRetries() {
        logl("ApnContext.resetErrorCodeRetries");
        String[] config = this.mPhone.getContext().getResources().getStringArray(17236003);
        synchronized (this.mRetriesLeftPerErrorCode) {
            this.mRetriesLeftPerErrorCode.clear();
            for (String c : config) {
                String[] errorValue = c.split(",");
                if (errorValue == null || errorValue.length != 2) {
                    log("Exception parsing config_retries_per_error_code: " + c);
                } else {
                    try {
                        int errorCode = Integer.parseInt(errorValue[0]);
                        int count = Integer.parseInt(errorValue[1]);
                        if (count > 0 && errorCode > 0) {
                            this.mRetriesLeftPerErrorCode.put(errorCode, count);
                        }
                    } catch (NumberFormatException e) {
                        log("Exception parsing config_retries_per_error_code: " + e);
                    } catch (Exception e2) {
                        logl(e2.toString());
                    }
                }
            }
        }
    }

    public boolean restartOnError(int errorCode) {
        int retriesLeft;
        boolean result = false;
        synchronized (this.mRetriesLeftPerErrorCode) {
            retriesLeft = this.mRetriesLeftPerErrorCode.get(errorCode);
            if (retriesLeft != 0) {
                if (retriesLeft != 1) {
                    this.mRetriesLeftPerErrorCode.put(errorCode, retriesLeft - 1);
                    result = false;
                } else {
                    resetErrorCodeRetries();
                    result = true;
                }
            }
        }
        logl("ApnContext.restartOnError(" + errorCode + ") found " + retriesLeft + " and returned " + result);
        return result;
    }

    public int incAndGetConnectionGeneration() {
        return this.mConnectionGeneration.incrementAndGet();
    }

    public int getConnectionGeneration() {
        return this.mConnectionGeneration.get();
    }

    /* access modifiers changed from: package-private */
    public long getRetryAfterDisconnectDelay() {
        return this.mRetryManager.getRetryAfterDisconnectDelay();
    }

    public static int getApnTypeFromNetworkType(int networkType) {
        if (networkType == 0) {
            return 17;
        }
        if (networkType == 2) {
            return 2;
        }
        if (networkType == 3) {
            return 4;
        }
        if (networkType == 4) {
            return 8;
        }
        if (networkType == 14) {
            return 256;
        }
        if (networkType == 15) {
            return 512;
        }
        switch (networkType) {
            case 10:
                return 32;
            case 11:
                return 64;
            case 12:
                return 128;
            default:
                try {
                    if (sMethodGetApnTypeFromNetworkTypeEx != null) {
                        return ((Integer) sMethodGetApnTypeFromNetworkTypeEx.invoke(null, Integer.valueOf(networkType))).intValue();
                    }
                } catch (Exception e) {
                    Rlog.d(SLOG_TAG, e.toString());
                }
                return 0;
        }
    }

    public static int getApnTypeFromNetworkRequest(NetworkRequest nr) {
        NetworkCapabilities nc = nr.networkCapabilities;
        if (nc.getTransportTypes().length > 0 && !nc.hasTransport(0)) {
            return 0;
        }
        int apnType = 0;
        boolean error = false;
        if (nc.hasCapability(12)) {
            apnType = 17;
        }
        if (nc.hasCapability(0)) {
            if (apnType != 0) {
                error = true;
            }
            apnType = 2;
        }
        if (nc.hasCapability(1)) {
            if (apnType != 0) {
                error = true;
            }
            apnType = 4;
        }
        if (nc.hasCapability(2)) {
            if (apnType != 0) {
                error = true;
            }
            apnType = 8;
        }
        if (nc.hasCapability(3)) {
            if (apnType != 0) {
                error = true;
            }
            apnType = 32;
        }
        if (nc.hasCapability(4)) {
            if (apnType != 0) {
                error = true;
            }
            apnType = 64;
        }
        if (nc.hasCapability(5)) {
            if (apnType != 0) {
                error = true;
            }
            apnType = 128;
        }
        if (nc.hasCapability(7)) {
            if (apnType != 0) {
                error = true;
            }
            apnType = 256;
        }
        if (nc.hasCapability(10)) {
            if (apnType != 0) {
                error = true;
            }
            apnType = 512;
        }
        if (nc.hasCapability(23)) {
            if (apnType != 0) {
                error = true;
            }
            apnType = 1024;
        }
        try {
            if (sMethodGetApnTypeFromNetworkRequestEx != null) {
                Bundle b = (Bundle) sMethodGetApnTypeFromNetworkRequestEx.invoke(null, nc, Integer.valueOf(apnType), Boolean.valueOf(error));
                apnType = b.getInt("apnType");
                error = b.getBoolean("error");
            }
        } catch (Exception e) {
            Rlog.d(SLOG_TAG, e.toString());
        }
        if (error) {
            Rlog.d(SLOG_TAG, "Multiple apn types specified in request - result is unspecified!");
        }
        if (apnType == 0) {
            Rlog.d(SLOG_TAG, "Unsupported NetworkRequest in Telephony: nr=" + nr);
        }
        return apnType;
    }

    public List<NetworkRequest> getNetworkRequests() {
        ArrayList arrayList;
        synchronized (this.mRefCountLock) {
            arrayList = new ArrayList(this.mNetworkRequests);
        }
        return arrayList;
    }

    public synchronized String toString() {
        return "{mApnType=" + this.mApnType + " mState=" + getState() + " mWaitingApns={" + this.mRetryManager.getWaitingApns() + "} mApnSetting={" + this.mApnSetting + "} mReason=" + this.mReason + " mDataEnabled=" + this.mDataEnabled + " mDependencyMet=" + this.mDependencyMet + "}";
    }

    /* access modifiers changed from: protected */
    public void log(String s) {
    }

    private void logl(String s) {
        log(s);
        this.mLocalLog.log(s);
    }

    public void dump(FileDescriptor fd, PrintWriter printWriter, String[] args) {
        IndentingPrintWriter pw = new IndentingPrintWriter(printWriter, "  ");
        synchronized (this.mRefCountLock) {
            pw.println(toString());
            if (this.mNetworkRequests.size() > 0) {
                pw.println("NetworkRequests:");
                pw.increaseIndent();
                Iterator<NetworkRequest> it = this.mNetworkRequests.iterator();
                while (it.hasNext()) {
                    pw.println(it.next());
                }
                pw.decreaseIndent();
            }
            pw.increaseIndent();
            pw.println("-----");
            pw.println("Local log:");
            this.mLocalLog.dump(fd, pw, args);
            pw.println("-----");
            pw.decreaseIndent();
            pw.println("Historical APN state:");
            pw.increaseIndent();
            this.mStateLocalLog.dump(fd, pw, args);
            pw.decreaseIndent();
            pw.println(this.mRetryManager);
            pw.println("--------------------------");
        }
    }
}
