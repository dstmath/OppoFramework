package org.codeaurora.ims;

import org.codeaurora.ims.internal.IQtiImsExt.Stub;
import org.codeaurora.ims.internal.IQtiImsExtListener;

public abstract class QtiImsExtBase {
    private QtiImsExtBinder mQtiImsExtBinder;

    public final class QtiImsExtBinder extends Stub {
        public void setCallForwardUncondTimer(int phoneId, int startHour, int startMinute, int endHour, int endMinute, int action, int condition, int serviceClass, String number, IQtiImsExtListener listener) {
            QtiImsExtBase.this.onSetCallForwardUncondTimer(phoneId, startHour, startMinute, endHour, endMinute, action, condition, serviceClass, number, listener);
        }

        public void getCallForwardUncondTimer(int phoneId, int reason, int serviceClass, IQtiImsExtListener listener) {
            QtiImsExtBase.this.onGetCallForwardUncondTimer(phoneId, reason, serviceClass, listener);
        }

        public void getPacketCount(int phoneId, IQtiImsExtListener listener) {
            QtiImsExtBase.this.onGetPacketCount(phoneId, listener);
        }

        public void getPacketErrorCount(int phoneId, IQtiImsExtListener listener) {
            QtiImsExtBase.this.onGetPacketErrorCount(phoneId, listener);
        }

        public void sendCallDeflectRequest(int phoneId, String deflectNumber, IQtiImsExtListener listener) {
            QtiImsExtBase.this.onSendCallDeflectRequest(phoneId, deflectNumber, listener);
        }

        public void resumePendingCall(int phoneId, int videoState) {
            QtiImsExtBase.this.onResumePendingCall(phoneId, videoState);
        }

        public void sendCallTransferRequest(int phoneId, int type, String number, IQtiImsExtListener listener) {
            QtiImsExtBase.this.onSendCallTransferRequest(phoneId, type, number, listener);
        }

        public void sendCancelModifyCall(int phoneId, IQtiImsExtListener listener) {
            QtiImsExtBase.this.onSendCancelModifyCall(phoneId, listener);
        }

        public void queryVopsStatus(int phoneId, IQtiImsExtListener listener) {
            QtiImsExtBase.this.onQueryVopsStatus(phoneId, listener);
        }

        public void querySsacStatus(int phoneId, IQtiImsExtListener listener) {
            QtiImsExtBase.this.onQuerySsacStatus(phoneId, listener);
        }

        public void registerForParticipantStatusInfo(int phoneId, IQtiImsExtListener listener) {
            QtiImsExtBase.this.onRegisterForParticipantStatusInfo(phoneId, listener);
        }

        public void updateVoltePreference(int phoneId, int preference, IQtiImsExtListener listener) {
            QtiImsExtBase.this.onUpdateVoltePreference(phoneId, preference, listener);
        }

        public void queryVoltePreference(int phoneId, IQtiImsExtListener listener) {
            QtiImsExtBase.this.onQueryVoltePreference(phoneId, listener);
        }

        public void getHandoverConfig(int phoneId, IQtiImsExtListener listener) {
            QtiImsExtBase.this.onGetHandoverConfig(phoneId, listener);
        }

        public void setHandoverConfig(int phoneId, int hoConfig, IQtiImsExtListener listener) {
            QtiImsExtBase.this.onSetHandoverConfig(phoneId, hoConfig, listener);
        }

        public int setRcsAppConfig(int phoneId, int defaultSmsApp) {
            return QtiImsExtBase.this.onSetRcsAppConfig(phoneId, defaultSmsApp);
        }

        public int getRcsAppConfig(int phoneId) {
            return QtiImsExtBase.this.onGetRcsAppConfig(phoneId);
        }

        public int setVvmAppConfig(int phoneId, int defaultVvmApp) {
            return QtiImsExtBase.this.onSetVvmAppConfig(phoneId, defaultVvmApp);
        }

        public int getVvmAppConfig(int phoneId) {
            return QtiImsExtBase.this.onGetVvmAppConfig(phoneId);
        }
    }

    public QtiImsExtBinder getBinder() {
        if (this.mQtiImsExtBinder == null) {
            this.mQtiImsExtBinder = new QtiImsExtBinder();
        }
        return this.mQtiImsExtBinder;
    }

    protected void onSetCallForwardUncondTimer(int phoneId, int startHour, int startMinute, int endHour, int endMinute, int action, int condition, int serviceClass, String number, IQtiImsExtListener listener) {
    }

    protected void onGetCallForwardUncondTimer(int phoneId, int reason, int serviceClass, IQtiImsExtListener listener) {
    }

    protected void onGetPacketCount(int phoneId, IQtiImsExtListener listener) {
    }

    protected void onGetPacketErrorCount(int phoneId, IQtiImsExtListener listener) {
    }

    protected void onSendCallDeflectRequest(int phoneId, String deflectNumber, IQtiImsExtListener listener) {
    }

    protected void onResumePendingCall(int phoneId, int videoState) {
    }

    protected void onSendCallTransferRequest(int phoneId, int type, String number, IQtiImsExtListener listener) {
    }

    protected void onSendCancelModifyCall(int phoneId, IQtiImsExtListener listener) {
    }

    protected void onQueryVopsStatus(int phoneId, IQtiImsExtListener listener) {
    }

    protected void onQuerySsacStatus(int phoneId, IQtiImsExtListener listener) {
    }

    protected void onRegisterForViceRefreshInfo(int phoneId, IQtiImsExtListener listener) {
    }

    protected void onRegisterForParticipantStatusInfo(int phoneId, IQtiImsExtListener listener) {
    }

    protected void onUpdateVoltePreference(int phoneId, int preference, IQtiImsExtListener listener) {
    }

    protected void onQueryVoltePreference(int phoneId, IQtiImsExtListener listener) {
    }

    protected void onGetHandoverConfig(int phoneId, IQtiImsExtListener listener) {
    }

    protected void onSetHandoverConfig(int phoneId, int hoConfig, IQtiImsExtListener listener) {
    }

    protected int onGetVvmAppConfig(int phoneId) {
        return 0;
    }

    protected int onSetVvmAppConfig(int phoneId, int defaultVvmApp) {
        return 0;
    }

    protected int onGetRcsAppConfig(int phoneId) {
        return 0;
    }

    protected int onSetRcsAppConfig(int phoneId, int defaultSmsApp) {
        return 0;
    }
}
