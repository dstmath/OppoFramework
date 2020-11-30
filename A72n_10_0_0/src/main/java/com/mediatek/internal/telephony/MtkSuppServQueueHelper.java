package com.mediatek.internal.telephony;

import android.content.Context;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.telephony.Rlog;
import com.android.internal.telephony.Phone;
import java.util.Vector;

public class MtkSuppServQueueHelper {
    private static final boolean DBG = true;
    private static final int EVENT_SS_RESPONSE = 2;
    private static final int EVENT_SS_SEND = 1;
    public static final String LOG_TAG = "SuppServQueueHelper";
    private static MtkSuppServQueueHelper instance = null;
    private static Object pausedSync = new Object();
    private Context mContext;
    private SuppServQueueHelperHandler mHandler;
    private Phone[] mPhones;

    private MtkSuppServQueueHelper(Context context, Phone[] phones) {
        this.mContext = context;
        this.mPhones = phones;
    }

    public static MtkSuppServQueueHelper makeSuppServQueueHelper(Context context, Phone[] phones) {
        if (context == null || phones == null) {
            return null;
        }
        MtkSuppServQueueHelper mtkSuppServQueueHelper = instance;
        if (mtkSuppServQueueHelper == null) {
            Rlog.d(LOG_TAG, "Create MtkSuppServQueueHelper singleton instance, phones.length = " + phones.length);
            instance = new MtkSuppServQueueHelper(context, phones);
        } else {
            mtkSuppServQueueHelper.mContext = context;
            mtkSuppServQueueHelper.mPhones = phones;
        }
        return instance;
    }

    public void init(Looper looper) {
        Rlog.d(LOG_TAG, "Initialize SuppServQueueHelper!");
        this.mHandler = new SuppServQueueHelperHandler(looper);
    }

    public void dispose() {
        Rlog.d(LOG_TAG, "dispose.");
    }

    /* access modifiers changed from: package-private */
    public class SuppServQueueHelperHandler extends Handler implements Runnable {
        private boolean paused = false;
        private Vector<Message> requestBuffer = new Vector<>();

        public SuppServQueueHelperHandler(Looper looper) {
            super(looper);
        }

        public void run() {
        }

        public void handleMessage(Message msg) {
            Rlog.d(MtkSuppServQueueHelper.LOG_TAG, "handleMessage(), msg.what = " + msg.what + " , paused = " + this.paused);
            int i = msg.what;
            if (i == 1) {
                synchronized (MtkSuppServQueueHelper.pausedSync) {
                    if (this.paused) {
                        Rlog.d(MtkSuppServQueueHelper.LOG_TAG, "A SS request ongoing, add it into the queue");
                        Message msgCopy = new Message();
                        msgCopy.copyFrom(msg);
                        this.requestBuffer.add(msgCopy);
                    } else {
                        processRequest(msg.obj, msg.arg1);
                        this.paused = true;
                    }
                }
            } else if (i != 2) {
                Rlog.d(MtkSuppServQueueHelper.LOG_TAG, "handleMessage(), msg.what must be SEND or RESPONSE");
            } else {
                synchronized (MtkSuppServQueueHelper.pausedSync) {
                    processResponse(msg.obj);
                    this.paused = false;
                    if (this.requestBuffer.size() > 0) {
                        this.requestBuffer.removeElementAt(0);
                        sendMessage(this.requestBuffer.elementAt(0));
                    }
                }
            }
        }

        private void processRequest(Object obj, int phoneId) {
            MtkSuppSrvRequest ss = (MtkSuppSrvRequest) obj;
            boolean enable = false;
            ss.mParcel.setDataPosition(0);
            Message respCallback = MtkSuppServQueueHelper.this.mHandler.obtainMessage(2, ss);
            Rlog.d(MtkSuppServQueueHelper.LOG_TAG, "processRequest(), ss.mRequestCode = " + ss.mRequestCode + ", ss.mResultCallback = " + ss.mResultCallback + ", phoneId = " + phoneId);
            int i = ss.mRequestCode;
            if (i == 3) {
                MtkSuppServQueueHelper.this.mPhones[phoneId].setOutgoingCallerIdDisplayInternal(ss.mParcel.readInt(), respCallback);
            } else if (i != 4) {
                boolean lockState = true;
                if (i != 18) {
                    switch (i) {
                        case 9:
                            String facility = ss.mParcel.readString();
                            if (ss.mParcel.readInt() != 1) {
                                lockState = false;
                            }
                            MtkSuppServQueueHelper.this.mPhones[phoneId].setCallBarringInternal(facility, lockState, ss.mParcel.readString(), respCallback, ss.mParcel.readInt());
                            return;
                        case 10:
                            MtkSuppServQueueHelper.this.mPhones[phoneId].getCallBarringInternal(ss.mParcel.readString(), ss.mParcel.readString(), respCallback, ss.mParcel.readInt());
                            return;
                        case 11:
                            MtkSuppServQueueHelper.this.mPhones[phoneId].setCallForwardingOptionInternal(ss.mParcel.readInt(), ss.mParcel.readInt(), ss.mParcel.readString(), ss.mParcel.readInt(), ss.mParcel.readInt(), respCallback);
                            return;
                        case 12:
                            MtkSuppServQueueHelper.this.mPhones[phoneId].getCallForwardingOptionInternal(ss.mParcel.readInt(), ss.mParcel.readInt(), respCallback);
                            return;
                        case 13:
                            if (ss.mParcel.readInt() == 1) {
                                enable = true;
                            }
                            MtkSuppServQueueHelper.this.mPhones[phoneId].setCallWaitingInternal(enable, respCallback);
                            return;
                        case 14:
                            MtkSuppServQueueHelper.this.mPhones[phoneId].getCallWaitingInternal(respCallback);
                            return;
                        default:
                            Rlog.d(MtkSuppServQueueHelper.LOG_TAG, "processRequest(), no match mRequestCode");
                            return;
                    }
                } else {
                    int reason = ss.mParcel.readInt();
                    int withTimeSlot = ss.mParcel.readInt();
                    MtkSuppServHelper ssHelper = MtkSuppServManager.getSuppServHelper(phoneId);
                    if (ssHelper != null) {
                        if (withTimeSlot == 1) {
                            enable = true;
                        }
                        ssHelper.queryCallForwardingOption(reason, enable, respCallback);
                    }
                }
            } else {
                MtkSuppServQueueHelper.this.mPhones[phoneId].getOutgoingCallerIdDisplayInternal(respCallback);
            }
        }

        private void processResponse(Object obj) {
            AsyncResult ar = (AsyncResult) obj;
            MtkSuppSrvRequest ss = (MtkSuppSrvRequest) ar.userObj;
            Message resp = ss.mResultCallback;
            Rlog.d(MtkSuppServQueueHelper.LOG_TAG, "processResponse, resp = " + resp + " , ar.result = " + ar.result + " , ar.exception = " + ar.exception);
            if (resp != null) {
                AsyncResult.forMessage(resp, ar.result, ar.exception);
                resp.sendToTarget();
            }
            ss.setResultCallback(null);
            ss.mParcel.recycle();
        }
    }

    private void addRequest(MtkSuppSrvRequest ss, int phoneId) {
        SuppServQueueHelperHandler suppServQueueHelperHandler = this.mHandler;
        if (suppServQueueHelperHandler != null) {
            suppServQueueHelperHandler.obtainMessage(1, phoneId, 0, ss).sendToTarget();
        }
    }

    public void getCallForwardingOptionForServiceClass(int cfReason, int serviceClass, Message response, int phoneId) {
        MtkSuppSrvRequest ss = MtkSuppSrvRequest.obtain(12, response);
        ss.mParcel.writeInt(cfReason);
        ss.mParcel.writeInt(serviceClass);
        addRequest(ss, phoneId);
    }

    public void setCallForwardingOptionForServiceClass(int action, int cfReason, String number, int timeSeconds, int serviceClass, Message response, int phoneId) {
        MtkSuppSrvRequest ss = MtkSuppSrvRequest.obtain(11, response);
        ss.mParcel.writeInt(action);
        ss.mParcel.writeInt(cfReason);
        ss.mParcel.writeString(number);
        ss.mParcel.writeInt(timeSeconds);
        ss.mParcel.writeInt(serviceClass);
        addRequest(ss, phoneId);
    }

    public void getCallWaiting(Message response, int phoneId) {
        addRequest(MtkSuppSrvRequest.obtain(14, response), phoneId);
    }

    public void setCallWaiting(boolean enable, Message response, int phoneId) {
        MtkSuppSrvRequest ss = MtkSuppSrvRequest.obtain(13, response);
        ss.mParcel.writeInt(enable ? 1 : 0);
        addRequest(ss, phoneId);
    }

    public void getCallBarring(String facility, String password, int serviceClass, Message response, int phoneId) {
        MtkSuppSrvRequest ss = MtkSuppSrvRequest.obtain(10, response);
        ss.mParcel.writeString(facility);
        ss.mParcel.writeString(password);
        ss.mParcel.writeInt(serviceClass);
        addRequest(ss, phoneId);
    }

    public void setCallBarring(String facility, boolean lockState, String password, int serviceClass, Message response, int phoneId) {
        MtkSuppSrvRequest ss = MtkSuppSrvRequest.obtain(9, response);
        ss.mParcel.writeString(facility);
        ss.mParcel.writeInt(lockState ? 1 : 0);
        ss.mParcel.writeString(password);
        ss.mParcel.writeInt(serviceClass);
        addRequest(ss, phoneId);
    }

    public void getOutgoingCallerIdDisplay(Message response, int phoneId) {
        addRequest(MtkSuppSrvRequest.obtain(4, response), phoneId);
    }

    public void setOutgoingCallerIdDisplay(int clirMode, Message response, int phoneId) {
        MtkSuppSrvRequest ss = MtkSuppSrvRequest.obtain(3, response);
        ss.mParcel.writeInt(clirMode);
        addRequest(ss, phoneId);
    }

    public void getCallForwardingOption(int reason, int withTimeSlot, Message response, int phoneId) {
        MtkSuppSrvRequest ss = MtkSuppSrvRequest.obtain(18, response);
        ss.mParcel.writeInt(reason);
        ss.mParcel.writeInt(withTimeSlot);
        addRequest(ss, phoneId);
    }
}
