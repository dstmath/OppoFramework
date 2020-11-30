package com.mediatek.op.ims.ril;

import android.content.Context;
import android.hardware.radio.V1_0.RadioResponseInfo;
import android.net.ConnectivityManager;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.IHwBinder;
import android.os.Message;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.WorkSource;
import android.telephony.ModemActivityInfo;
import android.telephony.Rlog;
import android.telephony.TelephonyHistogram;
import android.util.SparseArray;
import com.android.internal.telephony.ClientWakelockTracker;
import com.android.internal.telephony.CommandException;
import com.android.internal.telephony.metrics.TelephonyMetrics;
import com.mediatek.opcommon.telephony.MtkRILConstantsOp;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import vendor.mediatek.hardware.radio_op.V2_0.DialFrom;
import vendor.mediatek.hardware.radio_op.V2_0.IRadioOp;

public final class OpImsRIL extends OpImsBaseCommands {
    private static final int DEFAULT_ACK_WAKE_LOCK_TIMEOUT_MS = 200;
    private static final int DEFAULT_WAKE_LOCK_TIMEOUT_MS = 60000;
    static final int EVENT_ACK_WAKE_LOCK_TIMEOUT = 4;
    static final int EVENT_BLOCKING_RESPONSE_TIMEOUT = 5;
    static final int EVENT_RADIO_PROXY_DEAD = 6;
    static final int EVENT_SEND = 1;
    static final int EVENT_WAKE_LOCK_TIMEOUT = 2;
    public static final int FOR_ACK_WAKELOCK = 1;
    public static final int FOR_WAKELOCK = 0;
    static final String[] IMS_HIDL_SERVICE_NAME = {"OpImsRILd1", "OpImsRILd2", "OpImsRILd3", "OpImsRILd4"};
    public static final int INVALID_WAKELOCK = -1;
    static final int IRADIO_GET_SERVICE_DELAY_MILLIS = 4000;
    static final boolean OpImsRIL_LOGD = true;
    static final boolean OpImsRIL_LOGV = false;
    static final String OpImsRIL_LOG_TAG = "IMS_RILA_WWOP";
    static final String PROPERTY_WAKE_LOCK_TIMEOUT = "ro.ril.wake_lock_timeout";
    static final String RILJ_ACK_WAKELOCK_NAME = "OpImsRIL_ACK_WL";
    static final int RIL_HISTOGRAM_BUCKET_COUNT = 5;
    static SparseArray<TelephonyHistogram> mRilTimeHistograms = new SparseArray<>();
    final PowerManager.WakeLock mAckWakeLock;
    final int mAckWakeLockTimeout;
    volatile int mAckWlSequenceNum;
    private WorkSource mActiveWakelockWorkSource;
    private final ClientWakelockTracker mClientWakelockTracker = new ClientWakelockTracker();
    Context mContext;
    boolean mIsMobileNetworkSupported;
    Object[] mLastNITZTimeInfo;
    private TelephonyMetrics mMetrics;
    final Integer mPhoneId;
    private WorkSource mRILDefaultWorkSource;
    OpImsRadioIndication mRadioIndication;
    volatile IRadioOp mRadioProxy;
    final AtomicLong mRadioProxyCookie;
    final RadioProxyDeathRecipient mRadioProxyDeathRecipient;
    OpImsRadioResponse mRadioResponse;
    SparseArray<RILRequest> mRequestList;
    final RilHandler mRilHandler;
    AtomicBoolean mTestingEmergencyCall;
    final PowerManager.WakeLock mWakeLock;
    int mWakeLockCount;
    final int mWakeLockTimeout;
    volatile int mWlSequenceNum;

    public static List<TelephonyHistogram> getTelephonyRILTimingHistograms() {
        List<TelephonyHistogram> list;
        synchronized (mRilTimeHistograms) {
            list = new ArrayList<>(mRilTimeHistograms.size());
            for (int i = 0; i < mRilTimeHistograms.size(); i++) {
                list.add(new TelephonyHistogram(mRilTimeHistograms.valueAt(i)));
            }
        }
        return list;
    }

    /* access modifiers changed from: package-private */
    public class RilHandler extends Handler {
        RilHandler() {
        }

        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i == 2) {
                synchronized (OpImsRIL.this.mRequestList) {
                    if (msg.arg1 == OpImsRIL.this.mWlSequenceNum && OpImsRIL.this.clearWakeLock(0)) {
                        int count = OpImsRIL.this.mRequestList.size();
                        Rlog.d(OpImsRIL.OpImsRIL_LOG_TAG, "WAKE_LOCK_TIMEOUT  mRequestList=" + count);
                        for (int i2 = 0; i2 < count; i2++) {
                            RILRequest rr = OpImsRIL.this.mRequestList.valueAt(i2);
                            Rlog.d(OpImsRIL.OpImsRIL_LOG_TAG, i2 + ": [" + rr.mSerial + "] " + OpImsRIL.requestToString(rr.mRequest));
                        }
                    }
                }
            } else if (i != 4) {
                if (i == 5) {
                    RILRequest rr2 = OpImsRIL.this.findAndRemoveRequestFromList(msg.arg1);
                    if (rr2 != null) {
                        if (rr2.mResult != null) {
                            AsyncResult.forMessage(rr2.mResult, OpImsRIL.getResponseForTimedOutRILRequest(rr2), (Throwable) null);
                            rr2.mResult.sendToTarget();
                            OpImsRIL.this.mMetrics.writeOnRilTimeoutResponse(OpImsRIL.this.mPhoneId.intValue(), rr2.mSerial, rr2.mRequest);
                        }
                        OpImsRIL.this.decrementWakeLock(rr2);
                        rr2.release();
                    }
                } else if (i == 6) {
                    OpImsRIL opImsRIL = OpImsRIL.this;
                    opImsRIL.riljLog("handleMessage: EVENT_RADIO_PROXY_DEAD cookie = " + msg.obj + " mRadioProxyCookie = " + OpImsRIL.this.mRadioProxyCookie.get());
                    if (((Long) msg.obj).longValue() == OpImsRIL.this.mRadioProxyCookie.get()) {
                        OpImsRIL.this.resetProxyAndRequestList();
                        OpImsRIL.this.getRadioProxy(null);
                    }
                }
            } else if (msg.arg1 == OpImsRIL.this.mAckWlSequenceNum) {
                OpImsRIL.this.clearWakeLock(1);
            }
        }
    }

    /* access modifiers changed from: private */
    public static Object getResponseForTimedOutRILRequest(RILRequest rr) {
        if (rr != null && rr.mRequest == 135) {
            return new ModemActivityInfo(0, 0, 0, new int[5], 0, 0);
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public final class RadioProxyDeathRecipient implements IHwBinder.DeathRecipient {
        RadioProxyDeathRecipient() {
        }

        public void serviceDied(long cookie) {
            OpImsRIL.this.riljLog("serviceDied");
            OpImsRIL.this.mRilHandler.sendMessageDelayed(OpImsRIL.this.mRilHandler.obtainMessage(6, Long.valueOf(cookie)), 4000);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void resetProxyAndRequestList() {
        this.mRadioProxy = null;
        this.mRadioProxyCookie.incrementAndGet();
        RILRequest.resetSerial();
        clearRequestList(1, OpImsRIL_LOGV);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private IRadioOp getRadioProxy(Message result) {
        if (!this.mIsMobileNetworkSupported) {
            return null;
        }
        if (this.mRadioProxy != null) {
            return this.mRadioProxy;
        }
        try {
            this.mRadioProxy = IRadioOp.getService(IMS_HIDL_SERVICE_NAME[this.mPhoneId == null ? 0 : this.mPhoneId.intValue()]);
            if (this.mRadioProxy != null) {
                this.mRadioProxy.linkToDeath(this.mRadioProxyDeathRecipient, this.mRadioProxyCookie.incrementAndGet());
                this.mRadioProxy.setResponseFunctionsIms(this.mRadioResponse, this.mRadioIndication);
            } else {
                riljLoge("getRadioProxy: mRadioProxy == null");
            }
        } catch (RemoteException | RuntimeException e) {
            this.mRadioProxy = null;
            riljLoge("RadioProxy getService/setResponseFunctionsIms: " + e);
        }
        if (this.mRadioProxy == null) {
            if (result != null) {
                AsyncResult.forMessage(result, (Object) null, CommandException.fromRilErrno(1));
                result.sendToTarget();
            }
            RilHandler rilHandler = this.mRilHandler;
            rilHandler.sendMessageDelayed(rilHandler.obtainMessage(6, Long.valueOf(this.mRadioProxyCookie.incrementAndGet())), 4000);
        }
        return this.mRadioProxy;
    }

    public OpImsRIL(Context context, int instanceId) {
        super(context, instanceId);
        boolean z = OpImsRIL_LOGV;
        this.mWlSequenceNum = 0;
        this.mAckWlSequenceNum = 0;
        this.mRequestList = new SparseArray<>();
        this.mTestingEmergencyCall = new AtomicBoolean(OpImsRIL_LOGV);
        this.mMetrics = TelephonyMetrics.getInstance();
        this.mRadioProxy = null;
        this.mRadioProxyCookie = new AtomicLong(0);
        this.mContext = context;
        this.mPhoneId = Integer.valueOf(instanceId);
        this.mIsMobileNetworkSupported = ((ConnectivityManager) context.getSystemService("connectivity")).isNetworkSupported(0);
        this.mRadioResponse = new OpImsRadioResponse(this, instanceId);
        this.mRadioIndication = new OpImsRadioIndication(this, instanceId);
        this.mRilHandler = new RilHandler();
        this.mRadioProxyDeathRecipient = new RadioProxyDeathRecipient();
        PowerManager pm = (PowerManager) context.getSystemService("power");
        this.mWakeLock = pm.newWakeLock(1, OpImsRIL_LOG_TAG);
        this.mWakeLock.setReferenceCounted(OpImsRIL_LOGV);
        this.mAckWakeLock = pm.newWakeLock(1, RILJ_ACK_WAKELOCK_NAME);
        this.mAckWakeLock.setReferenceCounted(OpImsRIL_LOGV);
        this.mWakeLockTimeout = SystemProperties.getInt(PROPERTY_WAKE_LOCK_TIMEOUT, (int) DEFAULT_WAKE_LOCK_TIMEOUT_MS);
        this.mAckWakeLockTimeout = SystemProperties.getInt(PROPERTY_WAKE_LOCK_TIMEOUT, (int) DEFAULT_ACK_WAKE_LOCK_TIMEOUT_MS);
        this.mWakeLockCount = 0;
        this.mRILDefaultWorkSource = new WorkSource(context.getApplicationInfo().uid, context.getPackageName());
        IRadioOp proxy = getRadioProxy(null);
        StringBuilder sb = new StringBuilder();
        sb.append("Ims-RIL-WWOP: proxy = ");
        sb.append(proxy == null ? true : z);
        riljLog(sb.toString());
    }

    private void addRequest(RILRequest rr) {
        acquireWakeLock(rr, 0);
        synchronized (this.mRequestList) {
            rr.mStartTimeMs = SystemClock.elapsedRealtime();
            this.mRequestList.append(rr.mSerial, rr);
        }
    }

    private RILRequest obtainRequest(int request, Message result, WorkSource workSource) {
        RILRequest rr = RILRequest.obtain(request, result, workSource);
        addRequest(rr);
        return rr;
    }

    private void handleRadioProxyExceptionForRR(RILRequest rr, String caller, Exception e) {
        riljLoge(caller + ": " + e);
        resetProxyAndRequestList();
        RilHandler rilHandler = this.mRilHandler;
        rilHandler.sendMessageDelayed(rilHandler.obtainMessage(6, Long.valueOf(this.mRadioProxyCookie.incrementAndGet())), 4000);
    }

    /* access modifiers changed from: protected */
    public String convertNullToEmptyString(String string) {
        return string != null ? string : "";
    }

    public void dialFrom(String address, String fromAddress, int clirMode, boolean isVideoCall, Message response) {
        IRadioOp radioProxy = getRadioProxy(response);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(MtkRILConstantsOp.RIL_REQUEST_DIAL_FROM, response, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + ">  " + requestToString(rr.mRequest) + " address = " + address + ", from = " + fromAddress);
            try {
                DialFrom dialInfo = new DialFrom();
                dialInfo.address = convertNullToEmptyString(address);
                dialInfo.clir = clirMode;
                dialInfo.fromAddress = convertNullToEmptyString(fromAddress);
                dialInfo.isVideoCall = isVideoCall;
                radioProxy.dialFrom(rr.mSerial, dialInfo);
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "dialFrom", e);
            }
        }
    }

    public void sendUssiFrom(String from, int action, String ussi, Message response) {
        IRadioOp radioProxy = getRadioProxy(response);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(MtkRILConstantsOp.RIL_REQUEST_SEND_USSI_FROM, response, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + ">  " + requestToString(rr.mRequest) + " from=" + from + ", action=" + action + ", ussi=" + ussi);
            try {
                radioProxy.sendUssiFrom(rr.mSerial, from, action, ussi);
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "sendUssiFrom", e);
            }
        }
    }

    public void cancelUssiFrom(String from, Message response) {
        IRadioOp radioProxy = getRadioProxy(response);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(MtkRILConstantsOp.RIL_REQUEST_CANCEL_USSI_FROM, response, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + ">  " + requestToString(rr.mRequest) + " from=" + from);
            try {
                radioProxy.cancelUssiFrom(rr.mSerial, from);
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "cancelUssiFrom", e);
            }
        }
    }

    public void deviceSwitch(String number, String deviceId, Message response) {
        IRadioOp radioProxy = getRadioProxy(response);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(MtkRILConstantsOp.RIL_REQUEST_DEVICE_SWITCH, response, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + ">  " + requestToString(rr.mRequest) + " number= " + number + " deviceId= " + deviceId);
            try {
                radioProxy.deviceSwitch(rr.mSerial, number, deviceId);
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "deviceSwitch", e);
            }
        }
    }

    public void cancelDeviceSwitch(Message response) {
        IRadioOp radioProxy = getRadioProxy(response);
        if (radioProxy != null) {
            RILRequest rr = obtainRequest(MtkRILConstantsOp.RIL_REQUEST_CANCEL_DEVICE_SWITCH, response, this.mRILDefaultWorkSource);
            riljLog(rr.serialString() + ">  " + requestToString(rr.mRequest));
            try {
                radioProxy.cancelDeviceSwitch(rr.mSerial);
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "cancelDeviceSwitch", e);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void processIndication(int indicationType) {
        if (indicationType == 1) {
            sendAck();
            riljLog("Unsol response received; Sending ack to ril.cpp");
        }
    }

    /* access modifiers changed from: package-private */
    public void processRequestAck(int serial) {
        RILRequest rr;
        synchronized (this.mRequestList) {
            rr = this.mRequestList.get(serial);
        }
        if (rr == null) {
            Rlog.w(OpImsRIL_LOG_TAG, "processRequestAck: Unexpected solicited ack response! serial: " + serial);
            return;
        }
        decrementWakeLock(rr);
        riljLog(rr.serialString() + " Ack < " + requestToString(rr.mRequest));
    }

    /* access modifiers changed from: package-private */
    public RILRequest processResponse(RadioResponseInfo responseInfo) {
        RILRequest rr;
        int serial = responseInfo.serial;
        int error = responseInfo.error;
        int type = responseInfo.type;
        if (type == 1) {
            synchronized (this.mRequestList) {
                rr = this.mRequestList.get(serial);
            }
            if (rr == null) {
                Rlog.w(OpImsRIL_LOG_TAG, "Unexpected solicited ack response! sn: " + serial);
            } else {
                decrementWakeLock(rr);
                riljLog(rr.serialString() + " Ack < " + requestToString(rr.mRequest));
            }
            return rr;
        }
        RILRequest rr2 = findAndRemoveRequestFromList(serial);
        if (rr2 == null) {
            Rlog.e(OpImsRIL_LOG_TAG, "processResponse: Unexpected response! serial: " + serial + " error: " + error);
            return null;
        }
        addToRilHistogram(rr2);
        if (type == 2) {
            sendAck();
            riljLog("Response received for " + rr2.serialString() + " " + requestToString(rr2.mRequest) + " Sending ack to ril.cpp");
        }
        int i = rr2.mRequest;
        return rr2;
    }

    /* access modifiers changed from: package-private */
    public void processResponseDone(RILRequest rr, RadioResponseInfo responseInfo, Object ret) {
        if (responseInfo.error == 0) {
            riljLog(rr.serialString() + "< " + requestToString(rr.mRequest) + " " + retToString(rr.mRequest, ret));
        } else {
            riljLog(rr.serialString() + "< " + requestToString(rr.mRequest) + " error " + responseInfo.error);
            rr.onError(responseInfo.error, ret);
        }
        this.mMetrics.writeOnRilSolicitedResponse(this.mPhoneId.intValue(), rr.mSerial, responseInfo.error, rr.mRequest, ret);
        if (responseInfo.type == 0) {
            decrementWakeLock(rr);
        }
        rr.release();
    }

    private void sendAck() {
        RILRequest rr = RILRequest.obtain(800, null, this.mRILDefaultWorkSource);
        acquireWakeLock(rr, 1);
        IRadioOp radioProxy = getRadioProxy(null);
        if (radioProxy != null) {
            try {
                radioProxy.responseAcknowledgement();
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRR(rr, "sendAck", e);
                riljLoge("sendAck: " + e);
            }
        } else {
            Rlog.e(OpImsRIL_LOG_TAG, "Error trying to send ack, radioProxy = null");
        }
        rr.release();
    }

    private WorkSource getDeafultWorkSourceIfInvalid(WorkSource workSource) {
        if (workSource == null) {
            return this.mRILDefaultWorkSource;
        }
        return workSource;
    }

    private String getWorkSourceClientId(WorkSource workSource) {
        if (workSource == null) {
            return null;
        }
        return String.valueOf(workSource.get(0)) + ":" + workSource.getName(0);
    }

    private void acquireWakeLock(RILRequest rr, int wakeLockType) {
        synchronized (rr) {
            if (rr.mWakeLockType != -1) {
                Rlog.d(OpImsRIL_LOG_TAG, "Failed to aquire wakelock for " + rr.serialString());
                return;
            }
            if (wakeLockType == 0) {
                synchronized (this.mWakeLock) {
                    this.mWakeLock.acquire();
                    this.mWakeLockCount++;
                    this.mWlSequenceNum++;
                    if (!this.mClientWakelockTracker.isClientActive(getWorkSourceClientId(rr.mWorkSource))) {
                        if (this.mActiveWakelockWorkSource != null) {
                            this.mActiveWakelockWorkSource.add(rr.mWorkSource);
                        } else {
                            this.mActiveWakelockWorkSource = rr.mWorkSource;
                        }
                        this.mWakeLock.setWorkSource(this.mActiveWakelockWorkSource);
                    }
                    this.mClientWakelockTracker.startTracking(rr.mClientId, rr.mRequest, rr.mSerial, this.mWakeLockCount);
                    Message msg = this.mRilHandler.obtainMessage(2);
                    msg.arg1 = this.mWlSequenceNum;
                    this.mRilHandler.sendMessageDelayed(msg, (long) this.mWakeLockTimeout);
                }
            } else if (wakeLockType != 1) {
                Rlog.w(OpImsRIL_LOG_TAG, "Acquiring Invalid Wakelock type " + wakeLockType);
                return;
            } else {
                synchronized (this.mAckWakeLock) {
                    this.mAckWakeLock.acquire();
                    this.mAckWlSequenceNum++;
                    Message msg2 = this.mRilHandler.obtainMessage(4);
                    msg2.arg1 = this.mAckWlSequenceNum;
                    this.mRilHandler.sendMessageDelayed(msg2, (long) this.mAckWakeLockTimeout);
                }
            }
            rr.mWakeLockType = wakeLockType;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void decrementWakeLock(RILRequest rr) {
        synchronized (rr) {
            int i = rr.mWakeLockType;
            if (i != -1) {
                if (i == 0) {
                    synchronized (this.mWakeLock) {
                        this.mClientWakelockTracker.stopTracking(rr.mClientId, rr.mRequest, rr.mSerial, this.mWakeLockCount > 1 ? this.mWakeLockCount - 1 : 0);
                        if (!this.mClientWakelockTracker.isClientActive(getWorkSourceClientId(rr.mWorkSource)) && this.mActiveWakelockWorkSource != null) {
                            this.mActiveWakelockWorkSource.remove(rr.mWorkSource);
                            if (this.mActiveWakelockWorkSource.size() == 0) {
                                this.mActiveWakelockWorkSource = null;
                            }
                            this.mWakeLock.setWorkSource(this.mActiveWakelockWorkSource);
                        }
                        if (this.mWakeLockCount > 1) {
                            this.mWakeLockCount--;
                        } else {
                            this.mWakeLockCount = 0;
                            this.mWakeLock.release();
                        }
                    }
                } else if (i != 1) {
                    Rlog.w(OpImsRIL_LOG_TAG, "Decrementing Invalid Wakelock type " + rr.mWakeLockType);
                }
            }
            rr.mWakeLockType = -1;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean clearWakeLock(int wakeLockType) {
        if (wakeLockType == 0) {
            synchronized (this.mWakeLock) {
                if (this.mWakeLockCount == 0 && !this.mWakeLock.isHeld()) {
                    return OpImsRIL_LOGV;
                }
                Rlog.d(OpImsRIL_LOG_TAG, "NOTE: mWakeLockCount is " + this.mWakeLockCount + "at time of clearing");
                this.mWakeLockCount = 0;
                this.mWakeLock.release();
                this.mClientWakelockTracker.stopTrackingAll();
                this.mActiveWakelockWorkSource = null;
                return OpImsRIL_LOGD;
            }
        }
        synchronized (this.mAckWakeLock) {
            if (!this.mAckWakeLock.isHeld()) {
                return OpImsRIL_LOGV;
            }
            this.mAckWakeLock.release();
            return OpImsRIL_LOGD;
        }
    }

    private void clearRequestList(int error, boolean loggable) {
        synchronized (this.mRequestList) {
            int count = this.mRequestList.size();
            if (loggable) {
                Rlog.d(OpImsRIL_LOG_TAG, "clearRequestList  mWakeLockCount=" + this.mWakeLockCount + " mRequestList=" + count);
            }
            for (int i = 0; i < count; i++) {
                RILRequest rr = this.mRequestList.valueAt(i);
                if (loggable) {
                    Rlog.d(OpImsRIL_LOG_TAG, i + ": [" + rr.mSerial + "] " + requestToString(rr.mRequest));
                }
                rr.onError(error, null);
                decrementWakeLock(rr);
                rr.release();
            }
            this.mRequestList.clear();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private RILRequest findAndRemoveRequestFromList(int serial) {
        RILRequest rr;
        synchronized (this.mRequestList) {
            rr = this.mRequestList.get(serial);
            if (rr != null) {
                this.mRequestList.remove(serial);
            }
        }
        return rr;
    }

    private void addToRilHistogram(RILRequest rr) {
        int totalTime = (int) (SystemClock.elapsedRealtime() - rr.mStartTimeMs);
        synchronized (mRilTimeHistograms) {
            TelephonyHistogram entry = mRilTimeHistograms.get(rr.mRequest);
            if (entry == null) {
                entry = new TelephonyHistogram(1, rr.mRequest, 5);
                mRilTimeHistograms.put(rr.mRequest, entry);
            }
            entry.addTimeTaken(totalTime);
        }
    }

    static String responseToString(int request) {
        return "<unknown response>";
    }

    static String requestToString(int request) {
        switch (request) {
            case MtkRILConstantsOp.RIL_REQUEST_DIAL_FROM /* 11003 */:
                return "RIL_REQUEST_DIAL_FROM";
            case MtkRILConstantsOp.RIL_REQUEST_SEND_USSI_FROM /* 11004 */:
                return "RIL_REQUEST_SEND_USSI_FROM";
            case MtkRILConstantsOp.RIL_REQUEST_CANCEL_USSI_FROM /* 11005 */:
                return "RIL_REQUEST_CANCEL_USSI_FROM";
            case MtkRILConstantsOp.RIL_REQUEST_SET_EMERGENCY_CALL_CONFIG /* 11006 */:
                return "RIL_REQUEST_SET_EMERGENCY_CALL_CONFIG";
            default:
                return "<unknown request>";
        }
    }

    /* JADX INFO: Multiple debug info for r0v5 java.lang.String: [D('strings' java.lang.String[]), D('s' java.lang.String)] */
    /* JADX INFO: Multiple debug info for r0v8 java.lang.String: [D('intArray' int[]), D('s' java.lang.String)] */
    static String retToString(int req, Object ret) {
        if (ret == null) {
            return "";
        }
        if (ret instanceof int[]) {
            int[] intArray = (int[]) ret;
            int length = intArray.length;
            StringBuilder sb = new StringBuilder("{");
            if (length > 0) {
                sb.append(intArray[0]);
                for (int i = 0 + 1; i < length; i++) {
                    sb.append(", ");
                    sb.append(intArray[i]);
                }
            }
            sb.append("}");
            return sb.toString();
        } else if (!(ret instanceof String[])) {
            return ret.toString();
        } else {
            String[] strings = (String[]) ret;
            int length2 = strings.length;
            StringBuilder sb2 = new StringBuilder("{");
            if (length2 > 0) {
                sb2.append(strings[0]);
                for (int i2 = 0 + 1; i2 < length2; i2++) {
                    sb2.append(", ");
                    sb2.append(strings[i2]);
                }
            }
            sb2.append("}");
            return sb2.toString();
        }
    }

    /* access modifiers changed from: package-private */
    public void riljLog(String msg) {
        String str;
        StringBuilder sb = new StringBuilder();
        sb.append(msg);
        if (this.mPhoneId != null) {
            str = " [SUB" + this.mPhoneId + "]";
        } else {
            str = "";
        }
        sb.append(str);
        Rlog.d(OpImsRIL_LOG_TAG, sb.toString());
    }

    /* access modifiers changed from: package-private */
    public void riljLoge(String msg) {
        String str;
        StringBuilder sb = new StringBuilder();
        sb.append(msg);
        if (this.mPhoneId != null) {
            str = " [SUB" + this.mPhoneId + "]";
        } else {
            str = "";
        }
        sb.append(str);
        Rlog.e(OpImsRIL_LOG_TAG, sb.toString());
    }

    /* access modifiers changed from: package-private */
    public void riljLoge(String msg, Exception e) {
        String str;
        StringBuilder sb = new StringBuilder();
        sb.append(msg);
        if (this.mPhoneId != null) {
            str = " [SUB" + this.mPhoneId + "]";
        } else {
            str = "";
        }
        sb.append(str);
        Rlog.e(OpImsRIL_LOG_TAG, sb.toString(), e);
    }

    /* access modifiers changed from: package-private */
    public void riljLogv(String msg) {
        String str;
        StringBuilder sb = new StringBuilder();
        sb.append(msg);
        if (this.mPhoneId != null) {
            str = " [SUB" + this.mPhoneId + "]";
        } else {
            str = "";
        }
        sb.append(str);
        Rlog.v(OpImsRIL_LOG_TAG, sb.toString());
    }

    /* access modifiers changed from: package-private */
    public void unsljLog(int response) {
        riljLog("[UNSL]< " + responseToString(response));
    }

    /* access modifiers changed from: package-private */
    public void unsljLogMore(int response, String more) {
        riljLog("[UNSL]< " + responseToString(response) + " " + more);
    }

    /* access modifiers changed from: package-private */
    public void unsljLogRet(int response, Object ret) {
        riljLog("[UNSL]< " + responseToString(response) + " " + retToString(response, ret));
    }

    /* access modifiers changed from: package-private */
    public void unsljLogvRet(int response, Object ret) {
        riljLogv("[UNSL]< " + responseToString(response) + " " + retToString(response, ret));
    }
}
