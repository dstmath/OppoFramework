package com.oppo.internal.telephony;

import android.content.Context;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.os.RegistrantList;
import android.telephony.Rlog;
import android.telephony.TelephonyManager;
import com.android.internal.telephony.Call;
import com.android.internal.telephony.GsmCdmaCall;
import com.android.internal.telephony.OemConstant;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.imsphone.ImsPhone;
import com.android.internal.telephony.imsphone.ImsPhoneCall;

public class OppoCallStateMonitor extends Handler {
    public static final String TAG = "OppoCallStateMonitor";
    private static OppoCallStateMonitor sMe = null;
    private final int EVENT_PRECISE_CS_CALL_STATE_CHANGED = 101;
    private final int EVENT_PRECISE_IMS_CALL_STATE_CHANGED = 102;
    private GsmCdmaCall[] mBgCsCalls;
    private ImsPhoneCall[] mBgImsCalls;
    private RegistrantList mCallEndRegistrants = new RegistrantList();
    private RegistrantList mCallStartRegistrants = new RegistrantList();
    private Context mContext = null;
    private GsmCdmaCall[] mFgCsCalls;
    private ImsPhoneCall[] mFgImsCalls;
    private ImsPhone[] mImsPhones;
    private boolean mIsCallInActiveState = false;
    private boolean mIsCallInProgress = false;
    private final int mNumPhones = TelephonyManager.getDefault().getPhoneCount();
    private Phone[] mPhones;
    private GsmCdmaCall[] mRiCsCalls;
    private ImsPhoneCall[] mRiImsCalls;

    private static void enforceModifyPermission(Context context, String msg) {
        context.enforceCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE", msg);
    }

    public static OppoCallStateMonitor getInstance(Context context) {
        enforceModifyPermission(context, TAG);
        if (sMe == null) {
            sMe = new OppoCallStateMonitor(context);
        }
        return sMe;
    }

    private OppoCallStateMonitor(Context context) {
        int i = this.mNumPhones;
        this.mPhones = new Phone[i];
        this.mImsPhones = new ImsPhone[i];
        this.mFgCsCalls = new GsmCdmaCall[i];
        this.mBgCsCalls = new GsmCdmaCall[i];
        this.mRiCsCalls = new GsmCdmaCall[i];
        this.mFgImsCalls = new ImsPhoneCall[i];
        this.mBgImsCalls = new ImsPhoneCall[i];
        this.mRiImsCalls = new ImsPhoneCall[i];
    }

    public void initGsmCdma(Phone phone) {
        logd("initGsmCdma = " + phone);
        int phoneId = phone.getPhoneId();
        Phone[] phoneArr = this.mPhones;
        phoneArr[phoneId] = phone;
        if (phoneArr[phoneId] != null) {
            phoneArr[phoneId].registerForPreciseCallStateChanged(this, 101, Integer.valueOf(phoneId));
            this.mFgCsCalls[phoneId] = (GsmCdmaCall) this.mPhones[phoneId].getForegroundCall();
            this.mBgCsCalls[phoneId] = (GsmCdmaCall) this.mPhones[phoneId].getBackgroundCall();
            this.mRiCsCalls[phoneId] = (GsmCdmaCall) this.mPhones[phoneId].getRingingCall();
        }
    }

    public void initIms(ImsPhone imsPhone) {
        logd("initIms = " + imsPhone);
        int phoneId = imsPhone.getPhoneId();
        ImsPhone[] imsPhoneArr = this.mImsPhones;
        imsPhoneArr[phoneId] = imsPhone;
        if (imsPhoneArr[phoneId] != null) {
            imsPhoneArr[phoneId].registerForPreciseCallStateChanged(this, 102, Integer.valueOf(phoneId));
            this.mFgImsCalls[phoneId] = this.mImsPhones[phoneId].getForegroundCall();
            this.mBgImsCalls[phoneId] = this.mImsPhones[phoneId].getBackgroundCall();
            this.mRiImsCalls[phoneId] = this.mImsPhones[phoneId].getRingingCall();
        }
    }

    public boolean isCallActive() {
        return this.mIsCallInActiveState;
    }

    public boolean isCallInProgress() {
        return this.mIsCallInProgress;
    }

    public void registerForCallActive(Handler h, int what, Object o) {
        this.mCallStartRegistrants.addUnique(h, what, o);
    }

    public void unregisterForCallActive(Handler h) {
        this.mCallStartRegistrants.remove(h);
    }

    public void registerForCallEnd(Handler h, int what, Object o) {
        this.mCallEndRegistrants.addUnique(h, what, o);
    }

    public void unregisterForCallEnd(Handler h) {
        this.mCallEndRegistrants.remove(h);
    }

    public boolean isCallActive(int phoneId) {
        GsmCdmaCall[] gsmCdmaCallArr = this.mFgCsCalls;
        if (gsmCdmaCallArr[phoneId] == null || gsmCdmaCallArr[phoneId].getState() != Call.State.ACTIVE) {
            GsmCdmaCall[] gsmCdmaCallArr2 = this.mBgCsCalls;
            if (gsmCdmaCallArr2[phoneId] == null || gsmCdmaCallArr2[phoneId].getState() != Call.State.ACTIVE) {
                GsmCdmaCall[] gsmCdmaCallArr3 = this.mRiCsCalls;
                if (gsmCdmaCallArr3[phoneId] == null || gsmCdmaCallArr3[phoneId].getState() != Call.State.ACTIVE) {
                    ImsPhoneCall[] imsPhoneCallArr = this.mFgImsCalls;
                    if (imsPhoneCallArr[phoneId] == null || imsPhoneCallArr[phoneId].getState() != Call.State.ACTIVE) {
                        ImsPhoneCall[] imsPhoneCallArr2 = this.mBgImsCalls;
                        if (imsPhoneCallArr2[phoneId] == null || imsPhoneCallArr2[phoneId].getState() != Call.State.ACTIVE) {
                            ImsPhoneCall[] imsPhoneCallArr3 = this.mRiImsCalls;
                            if (imsPhoneCallArr3[phoneId] == null || imsPhoneCallArr3[phoneId].getState() != Call.State.ACTIVE) {
                                return false;
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    public boolean isCallIdle(int phoneId) {
        GsmCdmaCall[] gsmCdmaCallArr = this.mFgCsCalls;
        if (gsmCdmaCallArr[phoneId] == null || gsmCdmaCallArr[phoneId].isIdle()) {
            GsmCdmaCall[] gsmCdmaCallArr2 = this.mBgCsCalls;
            if (gsmCdmaCallArr2[phoneId] == null || gsmCdmaCallArr2[phoneId].isIdle()) {
                GsmCdmaCall[] gsmCdmaCallArr3 = this.mRiCsCalls;
                if (gsmCdmaCallArr3[phoneId] == null || gsmCdmaCallArr3[phoneId].isIdle()) {
                    ImsPhoneCall[] imsPhoneCallArr = this.mFgImsCalls;
                    if (imsPhoneCallArr[phoneId] == null || imsPhoneCallArr[phoneId].isIdle()) {
                        ImsPhoneCall[] imsPhoneCallArr2 = this.mBgImsCalls;
                        if (imsPhoneCallArr2[phoneId] == null || imsPhoneCallArr2[phoneId].isIdle()) {
                            ImsPhoneCall[] imsPhoneCallArr3 = this.mRiImsCalls;
                            if (imsPhoneCallArr3[phoneId] == null || imsPhoneCallArr3[phoneId].isIdle()) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    public boolean isCurrPhoneInCall(int phoneId) {
        logd("jianwei.wang - isCurrPhoneInCall" + phoneId);
        GsmCdmaCall[] gsmCdmaCallArr = this.mFgCsCalls;
        if (gsmCdmaCallArr[phoneId] == null || !gsmCdmaCallArr[phoneId].getState().isAlive()) {
            GsmCdmaCall[] gsmCdmaCallArr2 = this.mBgCsCalls;
            if (gsmCdmaCallArr2[phoneId] == null || !gsmCdmaCallArr2[phoneId].getState().isAlive()) {
                GsmCdmaCall[] gsmCdmaCallArr3 = this.mRiCsCalls;
                if (gsmCdmaCallArr3[phoneId] == null || !gsmCdmaCallArr3[phoneId].getState().isAlive()) {
                    ImsPhoneCall[] imsPhoneCallArr = this.mFgImsCalls;
                    if (imsPhoneCallArr[phoneId] == null || !imsPhoneCallArr[phoneId].getState().isAlive()) {
                        ImsPhoneCall[] imsPhoneCallArr2 = this.mBgImsCalls;
                        if (imsPhoneCallArr2[phoneId] == null || !imsPhoneCallArr2[phoneId].getState().isAlive()) {
                            ImsPhoneCall[] imsPhoneCallArr3 = this.mRiImsCalls;
                            if (imsPhoneCallArr3[phoneId] == null || !imsPhoneCallArr3[phoneId].getState().isAlive()) {
                                return false;
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    public boolean isOtherPhoneInCall(int phoneId) {
        int oPhoneId = 0;
        if (this.mNumPhones == 1) {
            logd("For SSSS project there is no other phone");
            return false;
        }
        if (phoneId == 0) {
            oPhoneId = 1;
        }
        logd("oPhoneId = " + oPhoneId);
        return isCurrPhoneInCall(oPhoneId);
    }

    public void handleMessage(Message msg) {
        int phoneId = ((Integer) ((AsyncResult) msg.obj).userObj).intValue();
        int i = msg.what;
        if (i != 101 && i != 102) {
            return;
        }
        if (!this.mIsCallInActiveState && isCallActive(phoneId)) {
            logd("processCallStateChanged: call active on phone " + phoneId);
            this.mIsCallInActiveState = true;
            this.mCallStartRegistrants.notifyRegistrants();
        } else if (isCallIdle(phoneId)) {
            logd("processCallStateChanged: call disconnected on phone " + phoneId);
            this.mIsCallInActiveState = false;
            this.mIsCallInProgress = false;
            this.mCallEndRegistrants.notifyRegistrants();
        } else if (!this.mIsCallInProgress) {
            logd("processCallStateChanged: call started on phone " + phoneId);
            this.mIsCallInProgress = true;
        }
    }

    /* access modifiers changed from: package-private */
    public void logd(String s) {
        if (OemConstant.SWITCH_LOG) {
            Rlog.d(TAG, s);
        }
    }

    /* access modifiers changed from: package-private */
    public void loge(String s) {
        Rlog.e(TAG, s);
    }
}
