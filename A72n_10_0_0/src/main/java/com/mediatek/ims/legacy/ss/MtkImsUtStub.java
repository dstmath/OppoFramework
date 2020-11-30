package com.mediatek.ims.legacy.ss;

import android.content.Context;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.telephony.ims.ImsCallForwardInfo;
import android.telephony.ims.ImsReasonInfo;
import android.util.Log;
import com.android.internal.telephony.CallForwardInfo;
import com.mediatek.ims.ImsService;
import com.mediatek.ims.MtkImsCallForwardInfo;
import com.mediatek.ims.SuppSrvConfig;
import com.mediatek.ims.feature.MtkImsUtImplBase;
import com.mediatek.ims.feature.MtkImsUtListener;
import com.mediatek.ims.internal.ImsXuiManager;
import com.mediatek.internal.telephony.MtkCallForwardInfo;
import com.mediatek.simservs.xcap.XcapException;
import java.net.UnknownHostException;
import java.util.HashMap;

public class MtkImsUtStub extends MtkImsUtImplBase {
    private static final boolean DBG = true;
    static final int IMS_UT_EVENT_GET_CF_TIME_SLOT = 1200;
    static final int IMS_UT_EVENT_GET_CF_WITH_CLASS = 1204;
    static final int IMS_UT_EVENT_SETUP_XCAP_USER_AGENT_STRING = 1203;
    static final int IMS_UT_EVENT_SET_CB_WITH_PWD = 1202;
    static final int IMS_UT_EVENT_SET_CF_TIME_SLOT = 1201;
    private static final String TAG = "MtkImsUtService";
    private static final Object mLock = new Object();
    private static HashMap<Integer, MtkImsUtStub> sMtkImsUtStubs = new HashMap<>();
    private Context mContext;
    private ResultHandler mHandler;
    private ImsService mImsService = null;
    private ImsUtStub mImsUtStub = null;
    private MtkImsUtListener mListener = null;
    private MMTelSSTransport mMMTelSSTSL;
    private int mPhoneId = 0;

    public MtkImsUtStub(Context context, int phoneId, ImsService imsService) {
        this.mContext = context;
        this.mImsUtStub = ImsUtStub.getInstance(context, phoneId, imsService);
        this.mMMTelSSTSL = MMTelSSTransport.getInstance();
        this.mMMTelSSTSL.registerUtService(this.mContext);
        HandlerThread thread = new HandlerThread("MtkImsUtStubResult");
        thread.start();
        this.mHandler = new ResultHandler(thread.getLooper());
        this.mImsService = imsService;
        this.mPhoneId = phoneId;
    }

    public static MtkImsUtStub getInstance(Context context, int phoneId, ImsService service) {
        synchronized (sMtkImsUtStubs) {
            if (sMtkImsUtStubs.containsKey(Integer.valueOf(phoneId))) {
                return sMtkImsUtStubs.get(Integer.valueOf(phoneId));
            }
            sMtkImsUtStubs.put(Integer.valueOf(phoneId), new MtkImsUtStub(context, phoneId, service));
            return sMtkImsUtStubs.get(Integer.valueOf(phoneId));
        }
    }

    public static MtkImsUtStub getInstance(int phoneId) {
        synchronized (sMtkImsUtStubs) {
            if (!sMtkImsUtStubs.containsKey(Integer.valueOf(phoneId))) {
                return null;
            }
            return sMtkImsUtStubs.get(Integer.valueOf(phoneId));
        }
    }

    public void setListener(MtkImsUtListener listener) {
        this.mListener = listener;
    }

    public String getUtIMPUFromNetwork() {
        Log.d(TAG, "getUtIMPUFromNetwork(): phoneId = " + this.mPhoneId);
        return ImsXuiManager.getInstance().getXui(this.mPhoneId);
    }

    public void setupXcapUserAgentString(String userAgent) {
        Log.w(TAG, "Not support this API setupXcapUserAgentString() in current platform");
    }

    public int queryCallForwardInTimeSlot(int condition) {
        int requestId;
        synchronized (mLock) {
            ImsUtStub imsUtStub = this.mImsUtStub;
            requestId = ImsUtStub.getAndIncreaseRequestId();
        }
        Log.d(TAG, "queryCallForwardInTimeSlot(): requestId = " + requestId);
        Message msg = this.mHandler.obtainMessage(IMS_UT_EVENT_GET_CF_TIME_SLOT, requestId, 0, null);
        SuppSrvConfig.getInstance(this.mContext).update(this.mPhoneId);
        this.mMMTelSSTSL.queryCallForwardInTimeSlotStatus(this.mImsUtStub.getCFReasonFromCondition(condition), 1, msg, this.mPhoneId);
        return requestId;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:12:0x005e, code lost:
        r0 = th;
     */
    public int updateCallForwardInTimeSlot(int action, int condition, String number, int timeSeconds, long[] timeSlot) {
        int requestId;
        synchronized (mLock) {
            ImsUtStub imsUtStub = this.mImsUtStub;
            requestId = ImsUtStub.getAndIncreaseRequestId();
        }
        Log.d(TAG, "updateCallForwardInTimeSlot(): requestId = " + requestId);
        Message msg = this.mHandler.obtainMessage(IMS_UT_EVENT_SET_CF_TIME_SLOT, requestId, 0, null);
        SuppSrvConfig.getInstance(this.mContext).update(this.mPhoneId);
        this.mMMTelSSTSL.setCallForwardInTimeSlot(this.mImsUtStub.getCFActionFromAction(action), this.mImsUtStub.getCFReasonFromCondition(condition), 1, number, timeSeconds, timeSlot, msg, this.mPhoneId);
        return requestId;
        while (true) {
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0057, code lost:
        r0 = th;
     */
    public int updateCallBarringForServiceClass(String password, int cbType, int enable, String[] barrList, int serviceClass) {
        int requestId;
        synchronized (mLock) {
            ImsUtStub imsUtStub = this.mImsUtStub;
            requestId = ImsUtStub.getAndIncreaseRequestId();
        }
        Log.d(TAG, "updateCallBarringForServiceClass(): requestId = " + requestId);
        boolean bEnable = enable == 1;
        String facility = getFacilityFromCBType(cbType);
        Message msg = this.mHandler.obtainMessage(IMS_UT_EVENT_SET_CB_WITH_PWD, requestId, 0, null);
        SuppSrvConfig.getInstance(this.mContext).update(this.mPhoneId);
        this.mMMTelSSTSL.setFacilityLock(facility, bEnable, null, serviceClass, msg, this.mPhoneId);
        return requestId;
        while (true) {
        }
    }

    public int queryCFForServiceClass(int condition, String number, int serviceClass) {
        int requestId;
        synchronized (mLock) {
            ImsUtStub imsUtStub = this.mImsUtStub;
            requestId = ImsUtStub.getAndIncreaseRequestId();
        }
        Log.d(TAG, "queryCFForServiceClass(): requestId = " + requestId);
        SuppSrvConfig.getInstance(this.mContext).update(this.mPhoneId);
        this.mMMTelSSTSL.queryCallForwardStatus(this.mImsUtStub.getCFReasonFromCondition(condition), serviceClass, number, this.mHandler.obtainMessage(IMS_UT_EVENT_GET_CF_WITH_CLASS, requestId, 0, null), this.mPhoneId);
        return requestId;
    }

    public boolean isSupportCFT() {
        return false;
    }

    public String getXcapConflictErrorMessage() {
        return "";
    }

    private class ResultHandler extends Handler {
        public ResultHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            Log.d(MtkImsUtStub.TAG, "handleMessage(): event = " + msg.what + ", requestId = " + msg.arg1 + ", mListener=" + MtkImsUtStub.this.mListener);
            SuppSrvConfig.getInstance(MtkImsUtStub.this.mContext);
            switch (msg.what) {
                case MtkImsUtStub.IMS_UT_EVENT_GET_CF_TIME_SLOT /* 1200 */:
                    if (MtkImsUtStub.this.mListener != null) {
                        AsyncResult ar = (AsyncResult) msg.obj;
                        if (ar.exception == null) {
                            MtkCallForwardInfo[] cfInfo = (MtkCallForwardInfo[]) ar.result;
                            MtkImsCallForwardInfo[] imsCfInfo = null;
                            if (cfInfo != null) {
                                imsCfInfo = new MtkImsCallForwardInfo[cfInfo.length];
                                for (int i = 0; i < cfInfo.length; i++) {
                                    MtkImsCallForwardInfo info = new MtkImsCallForwardInfo();
                                    info.mCondition = MtkImsUtStub.this.mImsUtStub.getConditionFromCFReason(cfInfo[i].reason);
                                    info.mStatus = cfInfo[i].status;
                                    info.mServiceClass = cfInfo[i].serviceClass;
                                    info.mToA = cfInfo[i].toa;
                                    info.mNumber = cfInfo[i].number;
                                    info.mTimeSeconds = cfInfo[i].timeSeconds;
                                    info.mTimeSlot = cfInfo[i].timeSlot;
                                    imsCfInfo[i] = info;
                                }
                            }
                            MtkImsUtStub.this.mListener.onUtConfigurationCallForwardInTimeSlotQueried(msg.arg1, imsCfInfo);
                            return;
                        } else if (ar.exception instanceof XcapException) {
                            ImsUtStub imsUtStub = MtkImsUtStub.this.mImsUtStub;
                            ImsUtStub unused = MtkImsUtStub.this.mImsUtStub;
                            imsUtStub.notifyUtConfigurationQueryFailed(msg, ImsUtStub.xcapExceptionToImsReasonInfo((XcapException) ar.exception, MtkImsUtStub.this.mPhoneId));
                            return;
                        } else if (ar.exception instanceof UnknownHostException) {
                            Log.d(MtkImsUtStub.TAG, "IMS_UT_EVENT_GET_CF_TIME_SLOT: UnknownHostException.");
                            MtkImsUtStub.this.mImsUtStub.notifyUtConfigurationQueryFailed(msg, new ImsReasonInfo(61447, 0));
                            return;
                        } else {
                            MtkImsUtStub.this.mImsUtStub.notifyUtConfigurationQueryFailed(msg, new ImsReasonInfo(804, 0));
                            return;
                        }
                    } else {
                        return;
                    }
                case MtkImsUtStub.IMS_UT_EVENT_SET_CF_TIME_SLOT /* 1201 */:
                    if (MtkImsUtStub.this.mListener != null) {
                        AsyncResult ar2 = (AsyncResult) msg.obj;
                        if (ar2.exception == null) {
                            Log.d(MtkImsUtStub.TAG, "utConfigurationUpdated(): event = " + msg.what);
                            MtkImsUtStub.this.mImsUtStub.notifyUtConfigurationUpdated(msg);
                            return;
                        } else if (ar2.exception instanceof XcapException) {
                            ImsUtStub imsUtStub2 = MtkImsUtStub.this.mImsUtStub;
                            ImsUtStub unused2 = MtkImsUtStub.this.mImsUtStub;
                            imsUtStub2.notifyUtConfigurationUpdateFailed(msg, ImsUtStub.xcapExceptionToImsReasonInfo((XcapException) ar2.exception, MtkImsUtStub.this.mPhoneId));
                            return;
                        } else if (ar2.exception instanceof UnknownHostException) {
                            Log.d(MtkImsUtStub.TAG, "UnknownHostException. event = " + msg.what);
                            MtkImsUtStub.this.mImsUtStub.notifyUtConfigurationUpdateFailed(msg, new ImsReasonInfo(61447, 0));
                            return;
                        } else {
                            MtkImsUtStub.this.mImsUtStub.notifyUtConfigurationUpdateFailed(msg, new ImsReasonInfo(804, 0));
                            return;
                        }
                    } else {
                        return;
                    }
                case MtkImsUtStub.IMS_UT_EVENT_SET_CB_WITH_PWD /* 1202 */:
                    if (MtkImsUtStub.this.mListener != null) {
                        AsyncResult ar3 = (AsyncResult) msg.obj;
                        if (ar3.exception == null) {
                            Log.d(MtkImsUtStub.TAG, "utConfigurationUpdated(): event = " + msg.what);
                            MtkImsUtStub.this.mImsUtStub.notifyUtConfigurationUpdated(msg);
                            return;
                        } else if (ar3.exception instanceof XcapException) {
                            ImsUtStub imsUtStub3 = MtkImsUtStub.this.mImsUtStub;
                            ImsUtStub unused3 = MtkImsUtStub.this.mImsUtStub;
                            imsUtStub3.notifyUtConfigurationUpdateFailed(msg, ImsUtStub.xcapExceptionToImsReasonInfo((XcapException) ar3.exception, MtkImsUtStub.this.mPhoneId));
                            return;
                        } else if (ar3.exception instanceof UnknownHostException) {
                            Log.d(MtkImsUtStub.TAG, "UnknownHostException. event = " + msg.what);
                            MtkImsUtStub.this.mImsUtStub.notifyUtConfigurationUpdateFailed(msg, new ImsReasonInfo(61447, 0));
                            return;
                        } else {
                            MtkImsUtStub.this.mImsUtStub.notifyUtConfigurationUpdateFailed(msg, new ImsReasonInfo(804, 0));
                            return;
                        }
                    } else {
                        return;
                    }
                case MtkImsUtStub.IMS_UT_EVENT_SETUP_XCAP_USER_AGENT_STRING /* 1203 */:
                default:
                    Log.d(MtkImsUtStub.TAG, "Unknown Event: " + msg.what);
                    return;
                case MtkImsUtStub.IMS_UT_EVENT_GET_CF_WITH_CLASS /* 1204 */:
                    if (MtkImsUtStub.this.mListener != null) {
                        AsyncResult ar4 = (AsyncResult) msg.obj;
                        if (ar4.exception == null) {
                            CallForwardInfo[] cfInfo2 = (CallForwardInfo[]) ar4.result;
                            ImsCallForwardInfo[] imsCfInfo2 = null;
                            if (cfInfo2 != null) {
                                imsCfInfo2 = new ImsCallForwardInfo[cfInfo2.length];
                                for (int i2 = 0; i2 < cfInfo2.length; i2++) {
                                    Log.d(MtkImsUtStub.TAG, "IMS_UT_EVENT_GET_CF_WITH_CLASS: cfInfo[" + i2 + "] = " + cfInfo2[i2]);
                                    imsCfInfo2[i2] = MtkImsUtStub.this.mImsUtStub.getImsCallForwardInfo(cfInfo2[i2]);
                                }
                            }
                            MtkImsUtStub.this.mImsUtStub.notifyUtConfigurationCallForwardQueried(msg, imsCfInfo2);
                            return;
                        } else if (ar4.exception instanceof XcapException) {
                            ImsUtStub imsUtStub4 = MtkImsUtStub.this.mImsUtStub;
                            ImsUtStub unused4 = MtkImsUtStub.this.mImsUtStub;
                            imsUtStub4.notifyUtConfigurationQueryFailed(msg, ImsUtStub.xcapExceptionToImsReasonInfo((XcapException) ar4.exception, MtkImsUtStub.this.mPhoneId));
                            return;
                        } else if (ar4.exception instanceof UnknownHostException) {
                            Log.d(MtkImsUtStub.TAG, "IMS_UT_EVENT_GET_CF_WITH_CLASS: UnknownHostException.");
                            MtkImsUtStub.this.mImsUtStub.notifyUtConfigurationQueryFailed(msg, new ImsReasonInfo(61447, 0));
                            return;
                        } else {
                            MtkImsUtStub.this.mImsUtStub.notifyUtConfigurationQueryFailed(msg, new ImsReasonInfo(804, 0));
                            return;
                        }
                    } else {
                        return;
                    }
            }
        }
    }

    private String getFacilityFromCBType(int cbType) {
        switch (cbType) {
            case 1:
                return "AI";
            case 2:
                return "AO";
            case XcapException.NO_HTTP_RESPONSE_EXCEPTION /* 3 */:
                return "OI";
            case XcapException.HTTP_RECOVERABL_EEXCEPTION /* 4 */:
                return "OX";
            case XcapException.MALFORMED_CHALLENGE_EXCEPTION /* 5 */:
                return "IR";
            case XcapException.AUTH_CHALLENGE_EXCEPTION /* 6 */:
                return "ACR";
            case XcapException.CREDENTIALS_NOT_AVAILABLE_EXCEPTION /* 7 */:
                return "AB";
            case XcapException.INVALID_CREDENTIALS_EXCEPTION /* 8 */:
                return "AG";
            case XcapException.AUTHENTICATION_EXCEPTION /* 9 */:
                return "AC";
            case XcapException.MALFORMED_COOKIE_EXCEPTION /* 10 */:
                return "BS_MT";
            default:
                return null;
        }
    }
}
