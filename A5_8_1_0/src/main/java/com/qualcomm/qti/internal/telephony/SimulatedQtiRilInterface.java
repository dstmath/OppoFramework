package com.qualcomm.qti.internal.telephony;

import android.content.Context;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.os.Registrant;
import android.os.RegistrantList;
import android.telephony.Rlog;
import android.util.Log;
import com.qualcomm.qcrilhook.QcRilHook;

public class SimulatedQtiRilInterface implements BaseRilInterface {
    private static final String LOG_TAG = "SimulatedQtiRilInterface";
    private static boolean mIsServiceReady = false;
    private static SimulatedQtiRilInterface sInstance = null;
    private QcRilHook mQcRilHook;
    private RegistrantList mServiceReadyRegistrantList = new RegistrantList();

    public static synchronized SimulatedQtiRilInterface getInstance(Context context) {
        SimulatedQtiRilInterface simulatedQtiRilInterface;
        synchronized (SimulatedQtiRilInterface.class) {
            if (sInstance == null) {
                sInstance = new SimulatedQtiRilInterface(context);
            } else {
                Log.wtf(LOG_TAG, "instance = " + sInstance);
            }
            simulatedQtiRilInterface = sInstance;
        }
        return simulatedQtiRilInterface;
    }

    private SimulatedQtiRilInterface(Context context) {
        logd(" in constructor ");
    }

    public void getOmhCallProfile(int modemApnType, Message callbackMsg, int phoneId) {
        logd("getOmhCallProfile, modemApnType: " + modemApnType + ", phoneId: " + phoneId);
        Message msg = Message.obtain(callbackMsg);
        msg.obj = new AsyncResult(callbackMsg.obj, null, null);
        msg.sendToTarget();
    }

    public void sendPhoneStatus(int isReady, int phoneId) {
        logd("sendPhoneStatus, isReady: " + isReady + ", phoneId: " + phoneId);
    }

    public boolean isServiceReady() {
        return true;
    }

    public void registerForServiceReadyEvent(Handler h, int what, Object obj) {
        Registrant r = new Registrant(h, what, obj);
        this.mServiceReadyRegistrantList.add(r);
        if (isServiceReady()) {
            r.notifyRegistrant(new AsyncResult(null, Boolean.valueOf(mIsServiceReady), null));
        }
    }

    public void unRegisterForServiceReadyEvent(Handler h) {
        this.mServiceReadyRegistrantList.remove(h);
    }

    public boolean setLocalCallHold(int phoneId, boolean enable) {
        return true;
    }

    private void logd(String string) {
        Rlog.d(LOG_TAG, string);
    }
}
