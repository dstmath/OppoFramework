package com.mediatek.ims.legacy.ss;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncResult;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import android.telephony.ims.ImsCallForwardInfo;
import android.telephony.ims.ImsReasonInfo;
import android.telephony.ims.ImsSsInfo;
import android.telephony.ims.ImsUtListener;
import android.telephony.ims.stub.ImsUtImplBase;
import android.util.Log;
import com.android.internal.telephony.CallForwardInfo;
import com.android.internal.telephony.PhoneConstants;
import com.mediatek.ims.ImsService;
import com.mediatek.ims.OperatorUtils;
import com.mediatek.ims.SuppSrvConfig;
import com.mediatek.internal.telephony.MtkSuppServHelper;
import com.mediatek.simservs.xcap.XcapException;
import java.net.UnknownHostException;
import java.util.HashMap;

public class ImsUtStub extends ImsUtImplBase {
    private static final boolean DBG = true;
    private static final int DEFAULT_INVALID_PHONE_ID = -1;
    static final int HTTP_ERROR_CODE_400 = 400;
    static final int HTTP_ERROR_CODE_403 = 403;
    static final int HTTP_ERROR_CODE_404 = 404;
    static final int HTTP_ERROR_CODE_409 = 409;
    private static final int IMS_DEREG_CAUSE_BY_SS_CONFIG = 2;
    private static final String IMS_DEREG_DISABLE_PROP = "persist.vendor.radio.ss.imsdereg_off";
    private static final String IMS_DEREG_OFF = "0";
    private static final String IMS_DEREG_ON = "1";
    private static final String IMS_DEREG_PROP = "vendor.gsm.radio.ss.imsdereg";
    static final int IMS_UT_EVENT_GET_CB = 1000;
    static final int IMS_UT_EVENT_GET_CF = 1001;
    static final int IMS_UT_EVENT_GET_CLIP = 1004;
    static final int IMS_UT_EVENT_GET_CLIR = 1003;
    static final int IMS_UT_EVENT_GET_COLP = 1006;
    static final int IMS_UT_EVENT_GET_COLR = 1005;
    static final int IMS_UT_EVENT_GET_CW = 1002;
    static final int IMS_UT_EVENT_IMS_DEREG = 1014;
    static final int IMS_UT_EVENT_SET_CB = 1007;
    static final int IMS_UT_EVENT_SET_CF = 1008;
    static final int IMS_UT_EVENT_SET_CLIP = 1011;
    static final int IMS_UT_EVENT_SET_CLIR = 1010;
    static final int IMS_UT_EVENT_SET_COLP = 1013;
    static final int IMS_UT_EVENT_SET_COLR = 1012;
    static final int IMS_UT_EVENT_SET_CW = 1009;
    private static final String TAG = "ImsUtService";
    private static final Object mLock = new Object();
    private static HashMap<Integer, ImsUtStub> sImsUtStubs = new HashMap<>();
    private static int sRequestId = 0;
    private Context mContext;
    private ResultHandler mHandler;
    private ImsService mImsService = null;
    private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        /* class com.mediatek.ims.legacy.ss.ImsUtStub.AnonymousClass1 */

        public void onReceive(Context context, Intent intent) {
            Log.d(ImsUtStub.TAG, "Intent action:" + intent.getAction());
            if (intent.getAction().equals("android.intent.action.SUBSCRIPTION_PHONE_STATE")) {
                ImsUtStub.this.onReceivePhoneStateChange(intent.getIntExtra("slot", ImsUtStub.DEFAULT_INVALID_PHONE_ID), intent.getIntExtra("phoneType", 0), Enum.valueOf(PhoneConstants.State.class, intent.getStringExtra("state")));
            }
        }
    };
    private boolean mIsInVoLteCall = false;
    private boolean mIsNeedImsDereg = false;
    private ImsUtListener mListener = null;
    private MMTelSSTransport mMMTelSSTSL;
    private int mPhoneId = 0;

    private ImsUtStub(Context context, int phoneId, ImsService imsService) {
        this.mContext = context;
        this.mMMTelSSTSL = MMTelSSTransport.getInstance();
        this.mMMTelSSTSL.registerUtService(this.mContext);
        HandlerThread thread = new HandlerThread("ImsUtStubResult");
        thread.start();
        this.mHandler = new ResultHandler(thread.getLooper());
        this.mContext.registerReceiver(this.mIntentReceiver, new IntentFilter("android.intent.action.SUBSCRIPTION_PHONE_STATE"));
        this.mImsService = imsService;
        this.mPhoneId = phoneId;
    }

    public static ImsUtStub getInstance(Context context, int phoneId, ImsService service) {
        synchronized (sImsUtStubs) {
            if (sImsUtStubs.containsKey(Integer.valueOf(phoneId))) {
                return sImsUtStubs.get(Integer.valueOf(phoneId));
            }
            sImsUtStubs.put(Integer.valueOf(phoneId), new ImsUtStub(context, phoneId, service));
            return sImsUtStubs.get(Integer.valueOf(phoneId));
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onReceivePhoneStateChange(int phoneId, int phoneType, PhoneConstants.State phoneState) {
        Log.d(TAG, "onReceivePhoneStateChange phoneId:" + phoneId + " phoneType: " + phoneType + " phoneState: " + phoneState + " mIsInVoLteCall: " + this.mIsInVoLteCall);
        if (phoneId == this.mPhoneId) {
            if (this.mIsInVoLteCall) {
                if (phoneState == PhoneConstants.State.IDLE) {
                    this.mIsInVoLteCall = false;
                    if (this.mIsNeedImsDereg) {
                        ResultHandler resultHandler = this.mHandler;
                        resultHandler.sendMessage(resultHandler.obtainMessage(IMS_UT_EVENT_IMS_DEREG, null));
                        this.mIsNeedImsDereg = false;
                    }
                }
            } else if (phoneState != PhoneConstants.State.IDLE && phoneType == 5) {
                this.mIsInVoLteCall = true;
            }
        }
    }

    /* access modifiers changed from: private */
    public class ResultHandler extends Handler {
        public ResultHandler(Looper looper) {
            super(looper);
        }

        /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
        public void handleMessage(Message msg) {
            Log.d(ImsUtStub.TAG, "handleMessage(): event = " + msg.what + ", requestId = " + msg.arg1 + ", mListener=" + ImsUtStub.this.mListener);
            SuppSrvConfig ssConfig = SuppSrvConfig.getInstance(ImsUtStub.this.mContext);
            switch (msg.what) {
                case ImsUtStub.IMS_UT_EVENT_GET_CB /* 1000 */:
                    if (ImsUtStub.this.mListener != null) {
                        AsyncResult ar = (AsyncResult) msg.obj;
                        if (ar.exception == null) {
                            int[] result = (int[]) ar.result;
                            ImsSsInfo[] info = {new ImsSsInfo()};
                            info[0].mStatus = result[0];
                            Log.d(ImsUtStub.TAG, "IMS_UT_EVENT_GET_CB: status = " + result[0]);
                            ImsUtStub.this.mListener.onUtConfigurationCallBarringQueried(msg.arg1, info);
                            return;
                        } else if (ar.exception instanceof UnknownHostException) {
                            Log.d(ImsUtStub.TAG, "IMS_UT_EVENT_GET_CB: UnknownHostException.");
                            ImsUtStub.this.mListener.onUtConfigurationQueryFailed(msg.arg1, new ImsReasonInfo(61447, 0));
                            return;
                        } else if (ar.exception instanceof XcapException) {
                            ImsUtStub.this.mListener.onUtConfigurationQueryFailed(msg.arg1, ImsUtStub.xcapExceptionToImsReasonInfo((XcapException) ar.exception, ImsUtStub.this.mPhoneId));
                            return;
                        } else {
                            ImsUtStub.this.mListener.onUtConfigurationQueryFailed(msg.arg1, new ImsReasonInfo(804, 0));
                            return;
                        }
                    } else {
                        return;
                    }
                case ImsUtStub.IMS_UT_EVENT_GET_CF /* 1001 */:
                    if (ImsUtStub.this.mListener != null) {
                        AsyncResult ar2 = (AsyncResult) msg.obj;
                        if (ar2.exception == null) {
                            CallForwardInfo[] cfInfo = (CallForwardInfo[]) ar2.result;
                            ImsCallForwardInfo[] imsCfInfo = null;
                            if (cfInfo != null) {
                                imsCfInfo = new ImsCallForwardInfo[cfInfo.length];
                                int i = 0;
                                while (i < cfInfo.length) {
                                    imsCfInfo[i] = ImsUtStub.this.getImsCallForwardInfo(cfInfo[i]);
                                    StringBuilder sb = new StringBuilder();
                                    sb.append("IMS_UT_EVENT_SET_CF: cfInfo[");
                                    sb.append(i);
                                    sb.append("] = , Condition: ");
                                    sb.append(imsCfInfo[i].getCondition());
                                    sb.append(", Status: ");
                                    sb.append(imsCfInfo[i].getStatus() == 0 ? "disabled" : "enabled");
                                    sb.append(", ToA: ");
                                    sb.append(imsCfInfo[i].getToA());
                                    sb.append(", Service Class: ");
                                    sb.append(imsCfInfo[i].getServiceClass());
                                    sb.append(", Number=");
                                    sb.append(MtkSuppServHelper.encryptString(imsCfInfo[i].getNumber()));
                                    sb.append(", Time (seconds): ");
                                    sb.append(imsCfInfo[i].getTimeSeconds());
                                    Log.d(ImsUtStub.TAG, sb.toString());
                                    i++;
                                    ar2 = ar2;
                                }
                            }
                            if (ssConfig.isNeedIMSDereg()) {
                                boolean enable = ImsUtStub.IMS_DEREG_ON.equals(SystemProperties.get(ImsUtStub.IMS_DEREG_PROP, ImsUtStub.IMS_DEREG_OFF));
                                SystemProperties.set(ImsUtStub.IMS_DEREG_PROP, ImsUtStub.IMS_DEREG_OFF);
                                boolean disableIMSDereg = ImsUtStub.IMS_DEREG_ON.equals(SystemProperties.get(ImsUtStub.IMS_DEREG_DISABLE_PROP, "-1"));
                                if (!enable || disableIMSDereg) {
                                    Log.d(ImsUtStub.TAG, "Skip IMS dereg.");
                                } else if (ImsUtStub.this.mIsInVoLteCall) {
                                    Log.d(ImsUtStub.TAG, "During call and later do IMS dereg");
                                    ImsUtStub.this.mIsNeedImsDereg = true;
                                } else {
                                    Log.d(ImsUtStub.TAG, "IMS dereg.");
                                    ImsUtStub.this.mImsService.deregisterImsWithCause(ImsUtStub.this.mPhoneId, 2);
                                }
                            }
                            ImsUtStub.this.mListener.onUtConfigurationCallForwardQueried(msg.arg1, imsCfInfo);
                            return;
                        } else if (ar2.exception instanceof XcapException) {
                            ImsUtStub.this.mListener.onUtConfigurationQueryFailed(msg.arg1, ImsUtStub.xcapExceptionToImsReasonInfo((XcapException) ar2.exception, ImsUtStub.this.mPhoneId));
                            return;
                        } else if (ar2.exception instanceof UnknownHostException) {
                            Log.d(ImsUtStub.TAG, "IMS_UT_EVENT_GET_CF: UnknownHostException.");
                            ImsUtStub.this.mListener.onUtConfigurationQueryFailed(msg.arg1, new ImsReasonInfo(61447, 0));
                            return;
                        } else {
                            ImsUtStub.this.mListener.onUtConfigurationQueryFailed(msg.arg1, new ImsReasonInfo(804, 0));
                            return;
                        }
                    } else {
                        return;
                    }
                case ImsUtStub.IMS_UT_EVENT_GET_CW /* 1002 */:
                    if (ImsUtStub.this.mListener != null) {
                        AsyncResult ar3 = (AsyncResult) msg.obj;
                        if (ar3.exception == null) {
                            int[] result2 = (int[]) ar3.result;
                            ImsSsInfo[] info2 = {new ImsSsInfo()};
                            info2[0].mStatus = result2[0];
                            Log.d(ImsUtStub.TAG, "IMS_UT_EVENT_GET_CW: status = " + result2[0]);
                            ImsUtStub.this.mListener.onUtConfigurationCallWaitingQueried(msg.arg1, info2);
                        } else if (ar3.exception instanceof UnknownHostException) {
                            Log.d(ImsUtStub.TAG, "IMS_UT_EVENT_GET_CW: UnknownHostException.");
                            ImsUtStub.this.mListener.onUtConfigurationQueryFailed(msg.arg1, new ImsReasonInfo(61447, 0));
                        } else if (ar3.exception instanceof XcapException) {
                            ImsUtStub.this.mListener.onUtConfigurationQueryFailed(msg.arg1, ImsUtStub.xcapExceptionToImsReasonInfo((XcapException) ar3.exception, ImsUtStub.this.mPhoneId));
                        } else {
                            ImsUtStub.this.mListener.onUtConfigurationQueryFailed(msg.arg1, new ImsReasonInfo(804, 0));
                        }
                        return;
                    }
                    return;
                case ImsUtStub.IMS_UT_EVENT_GET_CLIR /* 1003 */:
                    if (ImsUtStub.this.mListener != null) {
                        AsyncResult ar4 = (AsyncResult) msg.obj;
                        if (ar4.exception == null) {
                            Bundle info3 = new Bundle();
                            info3.putIntArray("queryClir", (int[]) ar4.result);
                            ImsUtStub.this.mListener.onUtConfigurationQueried(msg.arg1, info3);
                        } else if (ar4.exception instanceof UnknownHostException) {
                            Log.d(ImsUtStub.TAG, "IMS_UT_EVENT_GET_CLIR: UnknownHostException.");
                            ImsUtStub.this.mListener.onUtConfigurationQueryFailed(msg.arg1, new ImsReasonInfo(61447, 0));
                        } else if (ar4.exception instanceof XcapException) {
                            ImsUtStub.this.mListener.onUtConfigurationQueryFailed(msg.arg1, ImsUtStub.xcapExceptionToImsReasonInfo((XcapException) ar4.exception, ImsUtStub.this.mPhoneId));
                        } else {
                            ImsUtStub.this.mListener.onUtConfigurationQueryFailed(msg.arg1, new ImsReasonInfo(804, 0));
                        }
                        return;
                    }
                    return;
                case ImsUtStub.IMS_UT_EVENT_GET_CLIP /* 1004 */:
                case ImsUtStub.IMS_UT_EVENT_GET_COLR /* 1005 */:
                case ImsUtStub.IMS_UT_EVENT_GET_COLP /* 1006 */:
                    if (ImsUtStub.this.mListener != null) {
                        AsyncResult ar5 = (AsyncResult) msg.obj;
                        if (ar5.exception == null) {
                            ImsSsInfo ssInfo = new ImsSsInfo();
                            ssInfo.mStatus = ((int[]) ar5.result)[0];
                            Bundle info4 = new Bundle();
                            info4.putParcelable("imsSsInfo", ssInfo);
                            ImsUtStub.this.mListener.onUtConfigurationQueried(msg.arg1, info4);
                        } else if (ar5.exception instanceof XcapException) {
                            ImsUtStub.this.mListener.onUtConfigurationQueryFailed(msg.arg1, ImsUtStub.xcapExceptionToImsReasonInfo((XcapException) ar5.exception, ImsUtStub.this.mPhoneId));
                        } else if (ar5.exception instanceof UnknownHostException) {
                            Log.d(ImsUtStub.TAG, "UnknownHostException. event = " + msg.what);
                            ImsUtStub.this.mListener.onUtConfigurationQueryFailed(msg.arg1, new ImsReasonInfo(61447, 0));
                        } else {
                            ImsUtStub.this.mListener.onUtConfigurationQueryFailed(msg.arg1, new ImsReasonInfo(804, 0));
                        }
                        return;
                    }
                    return;
                case ImsUtStub.IMS_UT_EVENT_SET_CB /* 1007 */:
                case ImsUtStub.IMS_UT_EVENT_SET_CF /* 1008 */:
                    if (ImsUtStub.this.mListener != null) {
                        AsyncResult ar6 = (AsyncResult) msg.obj;
                        if (ar6.exception != null || ar6.result == null) {
                            if (ar6.exception == null && ssConfig.isNeedIMSDereg()) {
                                boolean enable2 = ImsUtStub.IMS_DEREG_ON.equals(SystemProperties.get(ImsUtStub.IMS_DEREG_PROP, ImsUtStub.IMS_DEREG_OFF));
                                SystemProperties.set(ImsUtStub.IMS_DEREG_PROP, ImsUtStub.IMS_DEREG_OFF);
                                boolean disableIMSDereg2 = ImsUtStub.IMS_DEREG_ON.equals(SystemProperties.get(ImsUtStub.IMS_DEREG_DISABLE_PROP, "-1"));
                                if (enable2 && !disableIMSDereg2) {
                                    if (ImsUtStub.this.mIsInVoLteCall) {
                                        Log.d(ImsUtStub.TAG, "During call and later do IMS dereg");
                                        ImsUtStub.this.mIsNeedImsDereg = true;
                                        break;
                                    } else {
                                        Log.d(ImsUtStub.TAG, "IMS dereg.");
                                        ImsUtStub.this.mImsService.deregisterImsWithCause(ImsUtStub.this.mPhoneId, 2);
                                        break;
                                    }
                                } else {
                                    Log.d(ImsUtStub.TAG, "Skip IMS dereg.");
                                    break;
                                }
                            }
                        } else if (ar6.result instanceof CallForwardInfo[]) {
                            CallForwardInfo[] cfInfo2 = (CallForwardInfo[]) ar6.result;
                            ImsCallForwardInfo[] imsCfInfo2 = null;
                            if (!(cfInfo2 == null || cfInfo2.length == 0)) {
                                imsCfInfo2 = new ImsCallForwardInfo[cfInfo2.length];
                                for (int i2 = 0; i2 < cfInfo2.length; i2++) {
                                    Log.d(ImsUtStub.TAG, "IMS_UT_EVENT_SET_CF: cfInfo[" + i2 + "] = " + cfInfo2[i2]);
                                    imsCfInfo2[i2] = ImsUtStub.this.getImsCallForwardInfo(cfInfo2[i2]);
                                }
                            }
                            ImsUtStub.this.mListener.onUtConfigurationCallForwardQueried(msg.arg1, imsCfInfo2);
                            return;
                        }
                    }
                    break;
                case ImsUtStub.IMS_UT_EVENT_SET_CW /* 1009 */:
                case ImsUtStub.IMS_UT_EVENT_SET_CLIR /* 1010 */:
                case ImsUtStub.IMS_UT_EVENT_SET_CLIP /* 1011 */:
                case ImsUtStub.IMS_UT_EVENT_SET_COLR /* 1012 */:
                case ImsUtStub.IMS_UT_EVENT_SET_COLP /* 1013 */:
                    break;
                case ImsUtStub.IMS_UT_EVENT_IMS_DEREG /* 1014 */:
                    ImsUtStub.this.mImsService.deregisterImsWithCause(ImsUtStub.this.mPhoneId, 2);
                    return;
                default:
                    Log.d(ImsUtStub.TAG, "Unknown Event: " + msg.what);
                    return;
            }
            if (((ImsUtStub) ImsUtStub.this).mListener != null) {
                AsyncResult ar7 = (AsyncResult) msg.obj;
                if (ar7.exception == null) {
                    Log.d(ImsUtStub.TAG, "utConfigurationUpdated(): event = " + msg.what);
                    ImsUtStub.this.mListener.onUtConfigurationUpdated(msg.arg1);
                } else if (ar7.exception instanceof XcapException) {
                    ImsUtStub.this.mListener.onUtConfigurationUpdateFailed(msg.arg1, ImsUtStub.xcapExceptionToImsReasonInfo((XcapException) ar7.exception, ImsUtStub.this.mPhoneId));
                } else if (ar7.exception instanceof UnknownHostException) {
                    Log.d(ImsUtStub.TAG, "UnknownHostException. event = " + msg.what);
                    ImsUtStub.this.mListener.onUtConfigurationUpdateFailed(msg.arg1, new ImsReasonInfo(61447, 0));
                } else {
                    ImsUtStub.this.mListener.onUtConfigurationUpdateFailed(msg.arg1, new ImsReasonInfo(804, 0));
                }
            }
        }
    }

    public void close() {
        this.mContext.unregisterReceiver(this.mIntentReceiver);
    }

    /* access modifiers changed from: protected */
    public String getFacilityFromCBType(int cbType) {
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

    /* access modifiers changed from: protected */
    public int getCFActionFromAction(int cfAction) {
        if (cfAction == 0) {
            return 0;
        }
        if (cfAction == 1) {
            return 1;
        }
        if (cfAction == 3) {
            return 3;
        }
        if (cfAction != 4) {
            return 0;
        }
        return 4;
    }

    /* access modifiers changed from: protected */
    public int getCFReasonFromCondition(int condition) {
        switch (condition) {
            case 0:
                return 0;
            case 1:
                return 1;
            case 2:
                return 2;
            case XcapException.NO_HTTP_RESPONSE_EXCEPTION /* 3 */:
                return 3;
            case XcapException.HTTP_RECOVERABL_EEXCEPTION /* 4 */:
                return 4;
            case XcapException.MALFORMED_CHALLENGE_EXCEPTION /* 5 */:
                return 5;
            case XcapException.AUTH_CHALLENGE_EXCEPTION /* 6 */:
                return 6;
            default:
                return 3;
        }
    }

    /* access modifiers changed from: protected */
    public int getConditionFromCFReason(int reason) {
        switch (reason) {
            case 0:
                return 0;
            case 1:
                return 1;
            case 2:
                return 2;
            case XcapException.NO_HTTP_RESPONSE_EXCEPTION /* 3 */:
                return 3;
            case XcapException.HTTP_RECOVERABL_EEXCEPTION /* 4 */:
                return 4;
            case XcapException.MALFORMED_CHALLENGE_EXCEPTION /* 5 */:
                return 5;
            case XcapException.AUTH_CHALLENGE_EXCEPTION /* 6 */:
                return 6;
            default:
                return DEFAULT_INVALID_PHONE_ID;
        }
    }

    /* access modifiers changed from: protected */
    public ImsCallForwardInfo getImsCallForwardInfo(CallForwardInfo info) {
        ImsCallForwardInfo imsCfInfo = new ImsCallForwardInfo();
        imsCfInfo.mCondition = getConditionFromCFReason(info.reason);
        imsCfInfo.mStatus = info.status;
        imsCfInfo.mServiceClass = info.serviceClass;
        imsCfInfo.mToA = info.toa;
        imsCfInfo.mNumber = info.number;
        imsCfInfo.mTimeSeconds = info.timeSeconds;
        return imsCfInfo;
    }

    public int queryCallBarring(int cbType) {
        int requestId;
        int serviceClass;
        synchronized (mLock) {
            requestId = sRequestId;
            serviceClass = 1;
            sRequestId++;
        }
        Log.d(TAG, "queryCallBarring(): requestId = " + requestId);
        String facility = getFacilityFromCBType(cbType);
        Message msg = this.mHandler.obtainMessage(IMS_UT_EVENT_GET_CB, requestId, 0, null);
        SuppSrvConfig.getInstance(this.mContext).update(this.mPhoneId);
        if (MMTelSSUtils.getServiceClass() != DEFAULT_INVALID_PHONE_ID) {
            serviceClass = MMTelSSUtils.getServiceClass();
        }
        this.mMMTelSSTSL.queryFacilityLock(facility, null, serviceClass, msg, this.mPhoneId);
        MMTelSSUtils.resetServcieClass();
        return requestId;
    }

    public int queryCallBarringForServiceClass(int cbType, int serviceClass) {
        int requestId;
        synchronized (mLock) {
            requestId = sRequestId;
            sRequestId++;
        }
        Log.d(TAG, "queryCallBarringForServiceClass(): requestId = " + requestId);
        String facility = getFacilityFromCBType(cbType);
        Message msg = this.mHandler.obtainMessage(IMS_UT_EVENT_GET_CB, requestId, 0, null);
        SuppSrvConfig.getInstance(this.mContext).update(this.mPhoneId);
        this.mMMTelSSTSL.queryFacilityLock(facility, null, serviceClass, msg, this.mPhoneId);
        return requestId;
    }

    public int queryCallForward(int condition, String number) {
        int requestId;
        int serviceClass;
        synchronized (mLock) {
            requestId = sRequestId;
            sRequestId++;
        }
        Log.d(TAG, "queryCallForward(): requestId = " + requestId);
        Message msg = this.mHandler.obtainMessage(IMS_UT_EVENT_GET_CF, requestId, 0, null);
        SuppSrvConfig.getInstance(this.mContext).update(this.mPhoneId);
        if (MMTelSSUtils.getServiceClass() != DEFAULT_INVALID_PHONE_ID) {
            serviceClass = MMTelSSUtils.getServiceClass();
        } else {
            serviceClass = 0;
        }
        this.mMMTelSSTSL.queryCallForwardStatus(getCFReasonFromCondition(condition), serviceClass, number, msg, this.mPhoneId);
        MMTelSSUtils.resetServcieClass();
        return requestId;
    }

    public int queryCallWaiting() {
        int requestId;
        synchronized (mLock) {
            requestId = sRequestId;
            sRequestId++;
        }
        Log.d(TAG, "queryCallWaiting(): requestId = " + requestId);
        Message msg = this.mHandler.obtainMessage(IMS_UT_EVENT_GET_CW, requestId, 0, null);
        SuppSrvConfig.getInstance(this.mContext).update(this.mPhoneId);
        this.mMMTelSSTSL.queryCallWaiting(1, msg, this.mPhoneId);
        return requestId;
    }

    public int queryCLIR() {
        int requestId;
        synchronized (mLock) {
            requestId = sRequestId;
            sRequestId++;
        }
        Log.d(TAG, "queryCLIR(): requestId = " + requestId);
        Message msg = this.mHandler.obtainMessage(IMS_UT_EVENT_GET_CLIR, requestId, 0, null);
        SuppSrvConfig.getInstance(this.mContext).update(this.mPhoneId);
        this.mMMTelSSTSL.getCLIR(msg, this.mPhoneId);
        return requestId;
    }

    public int queryCLIP() {
        int requestId;
        synchronized (mLock) {
            requestId = sRequestId;
            sRequestId++;
        }
        Log.d(TAG, "queryCLIP(): requestId = " + requestId);
        Message msg = this.mHandler.obtainMessage(IMS_UT_EVENT_GET_CLIP, requestId, 0, null);
        SuppSrvConfig.getInstance(this.mContext).update(this.mPhoneId);
        this.mMMTelSSTSL.queryCLIP(msg, this.mPhoneId);
        return requestId;
    }

    public int queryCOLR() {
        int requestId;
        synchronized (mLock) {
            requestId = sRequestId;
            sRequestId++;
        }
        Log.d(TAG, "queryCOLR(): requestId = " + requestId);
        Message msg = this.mHandler.obtainMessage(IMS_UT_EVENT_GET_COLR, requestId, 0, null);
        SuppSrvConfig.getInstance(this.mContext).update(this.mPhoneId);
        this.mMMTelSSTSL.getCOLR(msg, this.mPhoneId);
        return requestId;
    }

    public int queryCOLP() {
        int requestId;
        synchronized (mLock) {
            requestId = sRequestId;
            sRequestId++;
        }
        Log.d(TAG, "queryCOLP(): requestId = " + requestId);
        Message msg = this.mHandler.obtainMessage(IMS_UT_EVENT_GET_COLP, requestId, 0, null);
        SuppSrvConfig.getInstance(this.mContext).update(this.mPhoneId);
        this.mMMTelSSTSL.getCOLP(msg, this.mPhoneId);
        return requestId;
    }

    public int transact(Bundle ssInfo) {
        int requestId;
        synchronized (mLock) {
            requestId = sRequestId;
            sRequestId++;
        }
        return requestId;
    }

    public int updateCallBarring(int cbType, int enable, String[] barrList) {
        int requestId;
        int serviceClass;
        synchronized (mLock) {
            requestId = sRequestId;
            sRequestId++;
        }
        Log.d(TAG, "updateCallBarring(): requestId = " + requestId);
        boolean bEnable = enable == 1;
        String facility = getFacilityFromCBType(cbType);
        Message msg = this.mHandler.obtainMessage(IMS_UT_EVENT_SET_CB, requestId, 0, null);
        SuppSrvConfig.getInstance(this.mContext).update(this.mPhoneId);
        if (MMTelSSUtils.getServiceClass() != DEFAULT_INVALID_PHONE_ID) {
            serviceClass = MMTelSSUtils.getServiceClass();
        } else {
            serviceClass = 1;
        }
        this.mMMTelSSTSL.setFacilityLock(facility, bEnable, null, serviceClass, msg, this.mPhoneId);
        MMTelSSUtils.resetServcieClass();
        return requestId;
    }

    public int updateCallBarringForServiceClass(int cbType, int enable, String[] barrList, int serviceClass) {
        int requestId;
        synchronized (mLock) {
            requestId = sRequestId;
            sRequestId++;
        }
        Log.d(TAG, "updateCallBarringForServiceClass(): requestId = " + requestId);
        boolean bEnable = enable == 1;
        String facility = getFacilityFromCBType(cbType);
        Message msg = this.mHandler.obtainMessage(IMS_UT_EVENT_SET_CB, requestId, 0, null);
        SuppSrvConfig.getInstance(this.mContext).update(this.mPhoneId);
        this.mMMTelSSTSL.setFacilityLock(facility, bEnable, null, serviceClass, msg, this.mPhoneId);
        return requestId;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0056, code lost:
        r0 = th;
     */
    public int updateCallForward(int action, int condition, String number, int serviceClass, int timeSeconds) {
        int requestId;
        synchronized (mLock) {
            requestId = sRequestId;
            sRequestId++;
        }
        Log.d(TAG, "updateCallForward(): requestId = " + requestId);
        Message msg = this.mHandler.obtainMessage(IMS_UT_EVENT_SET_CF, requestId, 0, null);
        SuppSrvConfig.getInstance(this.mContext).update(this.mPhoneId);
        this.mMMTelSSTSL.setCallForward(getCFActionFromAction(action), getCFReasonFromCondition(condition), serviceClass, number, timeSeconds, msg, this.mPhoneId);
        return requestId;
        while (true) {
        }
    }

    public int updateCallWaiting(boolean enable, int serviceClass) {
        int requestId;
        synchronized (mLock) {
            requestId = sRequestId;
            sRequestId++;
        }
        Log.d(TAG, "updateCallWaiting(): requestId = " + requestId);
        Message msg = this.mHandler.obtainMessage(IMS_UT_EVENT_SET_CW, requestId, 0, null);
        SuppSrvConfig.getInstance(this.mContext).update(this.mPhoneId);
        this.mMMTelSSTSL.setCallWaiting(enable, serviceClass, msg, this.mPhoneId);
        return requestId;
    }

    public int updateCLIR(int clirMode) {
        int requestId;
        synchronized (mLock) {
            requestId = sRequestId;
            sRequestId++;
        }
        Log.d(TAG, "updateCLIR(): requestId = " + requestId);
        Message msg = this.mHandler.obtainMessage(IMS_UT_EVENT_SET_CLIR, requestId, 0, null);
        SuppSrvConfig.getInstance(this.mContext).update(this.mPhoneId);
        this.mMMTelSSTSL.setCLIR(clirMode, msg, this.mPhoneId);
        return requestId;
    }

    public int updateCLIP(boolean enable) {
        int requestId;
        synchronized (mLock) {
            requestId = sRequestId;
            sRequestId++;
        }
        Log.d(TAG, "updateCLIP(): requestId = " + requestId);
        Message msg = this.mHandler.obtainMessage(IMS_UT_EVENT_SET_CLIP, requestId, 0, null);
        SuppSrvConfig.getInstance(this.mContext).update(this.mPhoneId);
        this.mMMTelSSTSL.setCLIP(enable ? 1 : 0, msg, this.mPhoneId);
        return requestId;
    }

    public int updateCOLR(int presentation) {
        int requestId;
        synchronized (mLock) {
            requestId = sRequestId;
            sRequestId++;
        }
        Log.d(TAG, "updateCOLR(): requestId = " + requestId);
        Message msg = this.mHandler.obtainMessage(IMS_UT_EVENT_SET_COLR, requestId, 0, null);
        SuppSrvConfig.getInstance(this.mContext).update(this.mPhoneId);
        this.mMMTelSSTSL.setCOLR(presentation, msg, this.mPhoneId);
        return requestId;
    }

    public int updateCOLP(boolean enable) {
        int requestId;
        synchronized (mLock) {
            requestId = sRequestId;
            sRequestId++;
        }
        Log.d(TAG, "updateCOLP(): requestId = " + requestId);
        Message msg = this.mHandler.obtainMessage(IMS_UT_EVENT_SET_COLP, requestId, 0, null);
        SuppSrvConfig.getInstance(this.mContext).update(this.mPhoneId);
        this.mMMTelSSTSL.setCOLP(enable ? 1 : 0, msg, this.mPhoneId);
        return requestId;
    }

    public void setListener(ImsUtListener listener) {
        this.mListener = listener;
    }

    static ImsReasonInfo xcapExceptionToImsReasonInfo(XcapException xcapEx, int phoneId) {
        if (xcapEx != null) {
            Log.d(TAG, "xcapExceptionToImsReasonInfo(): XcapException: code = " + xcapEx.getExceptionCodeCode() + ", http error = " + xcapEx.getHttpErrorCode() + ", isConnectionError = " + xcapEx.isConnectionError() + ", phoneId = " + phoneId);
        }
        if (OperatorUtils.isMatched(OperatorUtils.OPID.OP02, phoneId) && xcapEx.getHttpErrorCode() == HTTP_ERROR_CODE_400) {
            Log.d(TAG, "xcapExceptionToImsReasonInfo - translate 400 error cause to 403");
            return new ImsReasonInfo(61446, 0);
        } else if (xcapEx != null && xcapEx.getHttpErrorCode() == HTTP_ERROR_CODE_403) {
            return new ImsReasonInfo(61446, 0);
        } else {
            if (xcapEx != null && xcapEx.getHttpErrorCode() == HTTP_ERROR_CODE_404) {
                return new ImsReasonInfo(61448, 0);
            }
            if (xcapEx == null || xcapEx.getHttpErrorCode() != HTTP_ERROR_CODE_409) {
                return new ImsReasonInfo(804, 0);
            }
            return new ImsReasonInfo(61449, 0);
        }
    }

    public static int getAndIncreaseRequestId() {
        int requestId;
        synchronized (mLock) {
            requestId = sRequestId;
            sRequestId++;
        }
        return requestId;
    }

    public void notifyUtConfigurationUpdated(Message msg) {
        this.mListener.onUtConfigurationUpdated(msg.arg1);
    }

    public void notifyUtConfigurationUpdateFailed(Message msg, ImsReasonInfo error) {
        this.mListener.onUtConfigurationUpdateFailed(msg.arg1, error);
    }

    public void notifyUtConfigurationQueried(Message msg, Bundle ssInfo) {
        this.mListener.onUtConfigurationQueried(msg.arg1, ssInfo);
    }

    public void notifyUtConfigurationQueryFailed(Message msg, ImsReasonInfo error) {
        this.mListener.onUtConfigurationQueryFailed(msg.arg1, error);
    }

    public void notifyUtConfigurationCallForwardQueried(Message msg, ImsCallForwardInfo[] cfInfo) {
        this.mListener.onUtConfigurationCallForwardQueried(msg.arg1, cfInfo);
    }
}
