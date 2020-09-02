package com.mediatek.ims.internal;

import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.telephony.Rlog;
import com.android.ims.ImsException;
import com.mediatek.ims.internal.IMtkImsService;

public class MtkImsManagerEx {
    private static final boolean DBG = true;
    public static final String MTK_IMS_SERVICE = "mtkIms";
    private static final String TAG = "MtkImsManagerEx";
    private static MtkImsManagerEx sInstance = new MtkImsManagerEx();
    private MtkImsServiceDeathRecipient mMtkDeathRecipient = new MtkImsServiceDeathRecipient();
    /* access modifiers changed from: private */
    public IMtkImsService mMtkImsService = null;

    private MtkImsManagerEx() {
        bindMtkImsService(DBG);
    }

    public static MtkImsManagerEx getInstance() {
        return sInstance;
    }

    private void checkAndThrowExceptionIfServiceUnavailable() throws ImsException {
        if (this.mMtkImsService == null) {
            bindMtkImsService(DBG);
            if (this.mMtkImsService == null) {
                throw new ImsException("MtkImsService is unavailable", 106);
            }
        }
    }

    private static String getMtkImsServiceName() {
        return "mtkIms";
    }

    private void bindMtkImsService(boolean checkService) {
        if (!checkService || ServiceManager.checkService(getMtkImsServiceName()) != null) {
            IBinder b = ServiceManager.getService(getMtkImsServiceName());
            if (b != null) {
                try {
                    b.linkToDeath(this.mMtkDeathRecipient, 0);
                } catch (RemoteException e) {
                }
            }
            this.mMtkImsService = IMtkImsService.Stub.asInterface(b);
            log("mMtkImsService = " + this.mMtkImsService);
            return;
        }
        loge("bindMtkImsService binder is null");
    }

    private static void log(String s) {
        Rlog.d(TAG, s);
    }

    private static void logw(String s) {
        Rlog.w(TAG, s);
    }

    private static void loge(String s) {
        Rlog.e(TAG, s);
    }

    private static void loge(String s, Throwable t) {
        Rlog.e(TAG, s, t);
    }

    private class MtkImsServiceDeathRecipient implements IBinder.DeathRecipient {
        private MtkImsServiceDeathRecipient() {
        }

        public void binderDied() {
            IMtkImsService unused = MtkImsManagerEx.this.mMtkImsService = null;
        }
    }

    public int getImsState(int phoneId) throws ImsException {
        checkAndThrowExceptionIfServiceUnavailable();
        try {
            int imsState = this.mMtkImsService.getImsState(phoneId);
            log("getImsState=" + imsState + " for phoneId=" + phoneId);
            return imsState;
        } catch (RemoteException e) {
            throw new ImsException("getImsState()", e, 106);
        }
    }

    public int getCurrentCallCount(int phoneId) throws ImsException {
        checkAndThrowExceptionIfServiceUnavailable();
        try {
            int callCount = this.mMtkImsService.getCurrentCallCount(phoneId);
            log("getCurrentCallCount, phoneId: " + phoneId + " callCount: " + callCount);
            return callCount;
        } catch (RemoteException e) {
            throw new ImsException("getCurrentCallCount()", e, 106);
        }
    }
}
