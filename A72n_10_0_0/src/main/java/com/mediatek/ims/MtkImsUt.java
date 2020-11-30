package com.mediatek.ims;

import android.os.Message;
import android.os.RemoteException;
import android.telephony.Rlog;
import android.telephony.ims.ImsCallForwardInfo;
import android.telephony.ims.ImsReasonInfo;
import com.android.ims.ImsException;
import com.android.ims.ImsUt;
import com.android.ims.internal.IImsUt;
import com.mediatek.ims.internal.IMtkImsUt;
import com.mediatek.ims.internal.IMtkImsUtListener;
import java.util.Arrays;

public class MtkImsUt extends ImsUt {
    private static final String TAG = "MtkImsUt";
    private final IMtkImsUt miMtkUt;

    public MtkImsUt(IImsUt iUt, IMtkImsUt iMtkUt) {
        super(iUt);
        this.miMtkUt = iMtkUt;
        IMtkImsUt iMtkImsUt = this.miMtkUt;
        if (iMtkImsUt != null) {
            try {
                iMtkImsUt.setListener(new IMtkImsUtListenerProxy());
            } catch (RemoteException e) {
            }
        }
    }

    public void close() {
        MtkImsUt.super.close();
    }

    private class IMtkImsUtListenerProxy extends IMtkImsUtListener.Stub {
        private IMtkImsUtListenerProxy() {
        }

        public void utConfigurationCallForwardInTimeSlotQueried(IMtkImsUt iMtkUt, int id, MtkImsCallForwardInfo[] cfInfo) {
            Integer key = Integer.valueOf(id);
            synchronized (MtkImsUt.this.mLockObj) {
                MtkImsUt.this.sendSuccessReport((Message) MtkImsUt.this.mPendingCmds.get(key), cfInfo);
                MtkImsUt.this.mPendingCmds.remove(key);
            }
        }

        public void utConfigurationCallForwardQueried(IMtkImsUt iMtkUt, int id, ImsCallForwardInfo[] cfInfo) {
            Integer key = Integer.valueOf(id);
            synchronized (MtkImsUt.this.mLockObj) {
                MtkImsUt.this.sendSuccessReport((Message) MtkImsUt.this.mPendingCmds.get(key), cfInfo);
                MtkImsUt.this.mPendingCmds.remove(key);
            }
        }
    }

    public String getUtIMPUFromNetwork() throws ImsException {
        String utIMPUFromNetwork;
        log("getUtIMPUFromNetwork :: Ut = " + this.miMtkUt);
        synchronized (this.mLockObj) {
            try {
                utIMPUFromNetwork = this.miMtkUt.getUtIMPUFromNetwork();
            } catch (RemoteException e) {
                throw new ImsException("getUtIMPUFromNetwork()", e, 802);
            } catch (Throwable th) {
                throw th;
            }
        }
        return utIMPUFromNetwork;
    }

    public void setupXcapUserAgentString(String userAgent) throws ImsException {
        log("setupXcapUserAgentString :: Ut = " + this.miMtkUt);
        synchronized (this.mLockObj) {
            try {
                this.miMtkUt.setupXcapUserAgentString(userAgent);
            } catch (RemoteException e) {
                throw new ImsException("setupXcapUserAgentString()", e, 802);
            } catch (Throwable th) {
                throw th;
            }
        }
    }

    public void queryCallForwardInTimeSlot(int condition, Message result) {
        log("queryCallForwardInTimeSlot :: Ut = " + this.miMtkUt + ", condition = " + condition);
        synchronized (this.mLockObj) {
            try {
                int id = this.miMtkUt.queryCallForwardInTimeSlot(condition);
                if (id < 0) {
                    sendFailureReport(result, new ImsReasonInfo(802, 0));
                } else {
                    this.mPendingCmds.put(Integer.valueOf(id), result);
                }
            } catch (RemoteException e) {
                sendFailureReport(result, new ImsReasonInfo(802, 0));
            }
        }
    }

    public void updateCallForwardInTimeSlot(int action, int condition, String number, int timeSeconds, long[] timeSlot, Message result) {
        log("updateCallForwardInTimeSlot :: Ut = " + this.miMtkUt + ", action = " + action + ", condition = " + condition + ", number = " + number + ", timeSeconds = " + timeSeconds + ", timeSlot = " + Arrays.toString(timeSlot));
        synchronized (this.mLockObj) {
            try {
                int id = this.miMtkUt.updateCallForwardInTimeSlot(action, condition, number, timeSeconds, timeSlot);
                if (id < 0) {
                    sendFailureReport(result, new ImsReasonInfo(802, 0));
                } else {
                    this.mPendingCmds.put(Integer.valueOf(id), result);
                }
            } catch (RemoteException e) {
                sendFailureReport(result, new ImsReasonInfo(802, 0));
            }
        }
    }

    public void updateCallBarring(String password, int cbType, int action, Message result, String[] barrList, int serviceClass) {
        if (barrList != null) {
            String bList = new String();
            for (int i = 0; i < barrList.length; i++) {
                bList.concat(barrList[i] + " ");
            }
            log("updateCallBarring :: Ut=" + this.miMtkUt + ", cbType=" + cbType + ", action=" + action + ", serviceClass=" + serviceClass + ", barrList=" + bList);
        } else {
            log("updateCallBarring :: Ut=" + this.miMtkUt + ", cbType=" + cbType + ", action=" + action + ", serviceClass=" + serviceClass);
        }
        synchronized (this.mLockObj) {
            try {
                int id = this.miMtkUt.updateCallBarringForServiceClass(password, cbType, action, barrList, serviceClass);
                if (id < 0) {
                    sendFailureReport(result, new ImsReasonInfo(802, 0));
                } else {
                    this.mPendingCmds.put(Integer.valueOf(id), result);
                }
            } catch (RemoteException e) {
                sendFailureReport(result, new ImsReasonInfo(802, 0));
            }
        }
    }

    public String getXcapConflictErrorMessage() throws ImsException {
        String xcapConflictErrorMessage;
        log("getXcapConflictErrorMessage :: Ut = " + this.miMtkUt);
        synchronized (this.mLockObj) {
            try {
                xcapConflictErrorMessage = this.miMtkUt.getXcapConflictErrorMessage();
            } catch (RemoteException e) {
                throw new ImsException("getXcapConflictErrorMessage()", e, 802);
            } catch (Throwable th) {
                throw th;
            }
        }
        return xcapConflictErrorMessage;
    }

    public void queryCFForServiceClass(int condition, String number, int serviceClass, Message result) {
        log("queryCFForServiceClass :: condition=" + condition + ", number=" + Rlog.pii(TAG, number) + ", serviceClass = " + serviceClass);
        synchronized (this.mLockObj) {
            try {
                int id = this.miMtkUt.queryCFForServiceClass(condition, number, serviceClass);
                if (id < 0) {
                    sendFailureReport(result, new ImsReasonInfo(802, 0));
                } else {
                    this.mPendingCmds.put(Integer.valueOf(id), result);
                }
            } catch (RemoteException e) {
                sendFailureReport(result, new ImsReasonInfo(802, 0));
            }
        }
    }

    /* access modifiers changed from: protected */
    public void log(String s) {
        Rlog.d(TAG, s);
    }
}
