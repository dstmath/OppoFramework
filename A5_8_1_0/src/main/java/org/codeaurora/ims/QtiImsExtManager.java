package org.codeaurora.ims;

import android.content.Context;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.telephony.SubscriptionManager;
import android.util.Log;
import com.android.ims.ImsException;
import com.android.ims.ImsManager;
import org.codeaurora.ims.internal.IQtiImsExt;
import org.codeaurora.ims.internal.IQtiImsExt.Stub;
import org.codeaurora.ims.internal.IQtiImsExtListener;

public class QtiImsExtManager {
    private static String LOG_TAG = "QtiImsExtManager";
    public static final String SERVICE_ID = "qti.ims.ext";
    private Context mContext;
    private IQtiImsExt mQtiImsExt;

    public QtiImsExtManager(Context context) {
        this.mContext = context;
    }

    public void setCallForwardUncondTimer(int phoneId, int startHour, int startMinute, int endHour, int endMinute, int action, int condition, int serviceClass, String number, IQtiImsExtListener listener) throws QtiImsException {
        obtainBinder();
        checkPhoneId(phoneId);
        checkFeatureStatus(phoneId);
        try {
            this.mQtiImsExt.setCallForwardUncondTimer(phoneId, startHour, startMinute, endHour, endMinute, action, condition, serviceClass, number, listener);
        } catch (RemoteException e) {
            throw new QtiImsException("Remote ImsService setCallForwardUncondTimer : " + e);
        }
    }

    public void getCallForwardUncondTimer(int phoneId, int reason, int serviceClass, IQtiImsExtListener listener) throws QtiImsException {
        obtainBinder();
        checkPhoneId(phoneId);
        checkFeatureStatus(phoneId);
        try {
            this.mQtiImsExt.getCallForwardUncondTimer(phoneId, reason, serviceClass, listener);
        } catch (RemoteException e) {
            throw new QtiImsException("Remote ImsService getCallForwardUncondTimer : " + e);
        }
    }

    public void getPacketCount(int phoneId, IQtiImsExtListener listener) throws QtiImsException {
        obtainBinder();
        checkPhoneId(phoneId);
        checkFeatureStatus(phoneId);
        try {
            this.mQtiImsExt.getPacketCount(phoneId, listener);
        } catch (RemoteException e) {
            throw new QtiImsException("Remote ImsService getPacketCount : " + e);
        }
    }

    public void getPacketErrorCount(int phoneId, IQtiImsExtListener listener) throws QtiImsException {
        obtainBinder();
        checkPhoneId(phoneId);
        checkFeatureStatus(phoneId);
        try {
            this.mQtiImsExt.getPacketErrorCount(phoneId, listener);
        } catch (RemoteException e) {
            throw new QtiImsException("Remote ImsService getPacketErrorCount : " + e);
        }
    }

    public void sendCallDeflectRequest(int phoneId, String deflectNumber, IQtiImsExtListener listener) throws QtiImsException {
        obtainBinder();
        checkPhoneId(phoneId);
        checkFeatureStatus(phoneId);
        try {
            this.mQtiImsExt.sendCallDeflectRequest(phoneId, deflectNumber, listener);
        } catch (RemoteException e) {
            throw new QtiImsException("Remote ImsService sendCallDeflectRequestCount : " + e);
        }
    }

    public void resumePendingCall(int phoneId, int videoState) throws QtiImsException {
        obtainBinder();
        checkPhoneId(phoneId);
        checkFeatureStatus(phoneId);
        try {
            this.mQtiImsExt.resumePendingCall(phoneId, videoState);
        } catch (RemoteException e) {
            throw new QtiImsException("Remote ImsService resumePendingCall : " + e);
        }
    }

    public void sendCallTransferRequest(int phoneId, int type, String number, IQtiImsExtListener listener) throws QtiImsException {
        obtainBinder();
        checkPhoneId(phoneId);
        checkFeatureStatus(phoneId);
        try {
            this.mQtiImsExt.sendCallTransferRequest(phoneId, type, number, listener);
        } catch (RemoteException e) {
            throw new QtiImsException("Remote ImsService sendCallTransferRequest : " + e);
        }
    }

    public void sendCancelModifyCall(int phoneId, IQtiImsExtListener listener) throws QtiImsException {
        obtainBinder();
        checkPhoneId(phoneId);
        checkFeatureStatus(phoneId);
        try {
            this.mQtiImsExt.sendCancelModifyCall(phoneId, listener);
        } catch (RemoteException e) {
            throw new QtiImsException("Remote ImsService sendCancelModifyCall : " + e);
        }
    }

    public void queryVopsStatus(int phoneId, IQtiImsExtListener listener) throws QtiImsException {
        obtainBinder();
        checkPhoneId(phoneId);
        checkFeatureStatus(phoneId);
        try {
            this.mQtiImsExt.queryVopsStatus(phoneId, listener);
        } catch (RemoteException e) {
            throw new QtiImsException("Remote ImsService queryVopsStatus : " + e);
        }
    }

    public void querySsacStatus(int phoneId, IQtiImsExtListener listener) throws QtiImsException {
        obtainBinder();
        checkPhoneId(phoneId);
        checkFeatureStatus(phoneId);
        try {
            this.mQtiImsExt.querySsacStatus(phoneId, listener);
        } catch (RemoteException e) {
            throw new QtiImsException("Remote ImsService querySsacStatus : " + e);
        }
    }

    public void registerForParticipantStatusInfo(int phoneId, IQtiImsExtListener listener) throws QtiImsException {
        obtainBinder();
        checkPhoneId(phoneId);
        checkFeatureStatus(phoneId);
        try {
            this.mQtiImsExt.registerForParticipantStatusInfo(phoneId, listener);
        } catch (RemoteException e) {
            throw new QtiImsException("Remote ImsService registerForParticipantStatusInfo : " + e);
        }
    }

    public void updateVoltePreference(int phoneId, int preference, IQtiImsExtListener listener) throws QtiImsException {
        obtainBinder();
        checkPhoneId(phoneId);
        checkFeatureStatus(phoneId);
        try {
            this.mQtiImsExt.updateVoltePreference(phoneId, preference, listener);
        } catch (RemoteException e) {
            throw new QtiImsException("Remote ImsService updateVoltePreference : " + e);
        }
    }

    public void queryVoltePreference(int phoneId, IQtiImsExtListener listener) throws QtiImsException {
        obtainBinder();
        checkPhoneId(phoneId);
        checkFeatureStatus(phoneId);
        try {
            this.mQtiImsExt.queryVoltePreference(phoneId, listener);
        } catch (RemoteException e) {
            throw new QtiImsException("Remote ImsService queryVoltePreference : " + e);
        }
    }

    public void getHandoverConfig(int phoneId, IQtiImsExtListener listener) throws QtiImsException {
        obtainBinder();
        checkPhoneId(phoneId);
        checkFeatureStatus(phoneId);
        try {
            this.mQtiImsExt.getHandoverConfig(phoneId, listener);
        } catch (RemoteException e) {
            throw new QtiImsException("Remote ImsService getHandoverConfig : " + e);
        }
    }

    public void setHandoverConfig(int phoneId, int hoConfig, IQtiImsExtListener listener) throws QtiImsException {
        obtainBinder();
        checkPhoneId(phoneId);
        checkFeatureStatus(phoneId);
        try {
            this.mQtiImsExt.setHandoverConfig(phoneId, hoConfig, listener);
        } catch (RemoteException e) {
            throw new QtiImsException("Remote ImsService setHandoverConfig : " + e);
        }
    }

    private IQtiImsExt obtainBinder() throws QtiImsException {
        if (this.mQtiImsExt != null) {
            return this.mQtiImsExt;
        }
        IBinder b = ServiceManager.getService(SERVICE_ID);
        this.mQtiImsExt = Stub.asInterface(b);
        if (this.mQtiImsExt == null) {
            throw new QtiImsException("ImsService is not running");
        }
        try {
            b.linkToDeath(new -$Lambda$wtkba8Bd1_tkkSOmbS18RK4QCQY(this), 0);
        } catch (RemoteException e) {
            Log.e(LOG_TAG, "Unable to listen for QtiImsExt service death");
        }
        return this.mQtiImsExt;
    }

    private void handleQtiImsExtServiceDeath() {
        this.mQtiImsExt = null;
        Log.i(LOG_TAG, "qtiImsExtDeathListener QtiImsExt binder died");
    }

    private void checkPhoneId(int phoneId) throws QtiImsException {
        if (!SubscriptionManager.isValidPhoneId(phoneId)) {
            Log.e(LOG_TAG, "phoneId " + phoneId + " is not valid");
            throw new QtiImsException("invalid phoneId");
        }
    }

    private void checkFeatureStatus(int phoneId) throws QtiImsException {
        if (this.mContext == null) {
            throw new QtiImsException("Context is null");
        }
        try {
            if (ImsManager.getInstance(this.mContext, phoneId).getImsServiceStatus() != 2) {
                Log.e(LOG_TAG, "Feature status for phoneId " + phoneId + " is not ready");
                throw new QtiImsException("Feature state is NOT_READY");
            }
        } catch (ImsException e) {
            Log.e(LOG_TAG, "Got ImsException for phoneId " + phoneId);
            throw new QtiImsException("Feature state is NOT_READY");
        }
    }

    public int setRcsAppConfig(int phoneId, int defaultSmsApp) throws QtiImsException {
        obtainBinder();
        checkPhoneId(phoneId);
        checkFeatureStatus(phoneId);
        try {
            return this.mQtiImsExt.setRcsAppConfig(phoneId, defaultSmsApp);
        } catch (RemoteException e) {
            throw new QtiImsException("Remote ImsService setRcsAppConfig : " + e);
        }
    }

    public int getRcsAppConfig(int phoneId) throws QtiImsException {
        obtainBinder();
        checkPhoneId(phoneId);
        checkFeatureStatus(phoneId);
        try {
            return this.mQtiImsExt.getRcsAppConfig(phoneId);
        } catch (RemoteException e) {
            throw new QtiImsException("Remote ImsService getRcsAppConfig : " + e);
        }
    }

    public boolean isConnected(int phoneId, int serviceType, int callType) throws QtiImsException {
        try {
            return ImsManager.getInstance(this.mContext, phoneId).isConnected(serviceType, callType);
        } catch (ImsException e) {
            throw new QtiImsException("Exception in Ims isConnected : " + e);
        }
    }

    public boolean isOpened(int phoneId) throws QtiImsException {
        try {
            return ImsManager.getInstance(this.mContext, phoneId).isOpened();
        } catch (ImsException e) {
            throw new QtiImsException("Exception in Ims isOpened : " + e);
        }
    }

    public int setVvmAppConfig(int phoneId, int defaultVvmApp) throws QtiImsException {
        obtainBinder();
        checkPhoneId(phoneId);
        checkFeatureStatus(phoneId);
        try {
            return this.mQtiImsExt.setVvmAppConfig(phoneId, defaultVvmApp);
        } catch (RemoteException e) {
            throw new QtiImsException("Remote ImsService setVvmAppConfig : " + e);
        }
    }

    public int getVvmAppConfig(int phoneId) throws QtiImsException {
        obtainBinder();
        checkPhoneId(phoneId);
        checkFeatureStatus(phoneId);
        try {
            return this.mQtiImsExt.getVvmAppConfig(phoneId);
        } catch (RemoteException e) {
            throw new QtiImsException("Remote ImsService getVvmAppConfig : " + e);
        }
    }
}
