package com.mediatek.internal.telephony;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.telephony.LteVopsSupportInfo;
import android.telephony.NetworkRegistrationInfo;
import android.telephony.NetworkService;
import android.telephony.NetworkServiceCallback;
import android.telephony.Rlog;
import android.telephony.SubscriptionManager;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneFactory;
import com.mediatek.wfo.IMwiService;
import com.mediatek.wfo.IWifiOffloadService;
import com.mediatek.wfo.MwisConstants;
import com.mediatek.wfo.WifiOffloadManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;

public class IWlanNetworkService extends NetworkService {
    public static final int MWI_SERVICE_READY = 0;
    private static final String TAG = "IWlanNetworkService";
    private IWifiOffloadServiceDeathRecipient mDeathRecipient = new IWifiOffloadServiceDeathRecipient();
    private final ConcurrentHashMap<Integer, IWlanNetworkServiceProvider> mIWlanNetSrvProviderMap = new ConcurrentHashMap<>();
    private IWifiOffloadListenerProxy mProxy = null;
    private IWifiOffloadService sWifiOffloadService = null;

    public IWlanNetworkService() {
        bindAndRegisterWifiOffloadService();
        log("IWlanNetworkService init.");
    }

    /* access modifiers changed from: private */
    public class IWlanNetworkServiceProvider extends NetworkService.NetworkServiceProvider {
        public static final int GET_IWLAN_REGISTRATION_STATE_DONE = 2;
        public static final int IWLAN_REGISTRATION_STATE_CHANGED = 1;
        private final ConcurrentHashMap<Message, NetworkServiceCallback> mCallbackMap = new ConcurrentHashMap<>();
        private final Handler mHandler;
        private final HandlerThread mHandlerThread;
        private final Object mLock = new Object();
        private final Looper mLooper;
        private final Phone mPhone;
        private int mWfcState = 0;

        IWlanNetworkServiceProvider(int slotId) {
            super(IWlanNetworkService.this, slotId);
            IWlanNetworkService.this.log("IWlanNetworkServiceProvider construct.");
            this.mPhone = PhoneFactory.getPhone(getSlotIndex());
            this.mHandlerThread = new HandlerThread(IWlanNetworkServiceProvider.class.getSimpleName());
            this.mHandlerThread.start();
            this.mLooper = this.mHandlerThread.getLooper();
            this.mHandler = new Handler(this.mLooper, IWlanNetworkService.this) {
                /* class com.mediatek.internal.telephony.IWlanNetworkService.IWlanNetworkServiceProvider.AnonymousClass1 */

                /* JADX INFO: Multiple debug info for r1v1 int: [D('state' int), D('netState' android.telephony.NetworkRegistrationInfo)] */
                public void handleMessage(Message message) {
                    NetworkServiceCallback callback = (NetworkServiceCallback) IWlanNetworkServiceProvider.this.mCallbackMap.remove(message);
                    int i = message.what;
                    if (i == 1) {
                        int state = message.arg1;
                        IWlanNetworkService iWlanNetworkService = IWlanNetworkService.this;
                        iWlanNetworkService.log("IWLAN_REGISTRATION_STATE_CHANGED, slotid: " + IWlanNetworkServiceProvider.this.getSlotIndex() + ", state: " + state);
                        IWlanNetworkServiceProvider.this.mWfcState = state;
                        IWlanNetworkServiceProvider.this.notifyNetworkRegistrationInfoChanged();
                    } else if (i == 2) {
                        IWlanNetworkServiceProvider iWlanNetworkServiceProvider = IWlanNetworkServiceProvider.this;
                        NetworkRegistrationInfo netState = iWlanNetworkServiceProvider.createRegistrationState(iWlanNetworkServiceProvider.mWfcState);
                        try {
                            IWlanNetworkService iWlanNetworkService2 = IWlanNetworkService.this;
                            iWlanNetworkService2.log("Calling callback.onGetNetworkRegistrationStateComplete.resultCode = 0, netState = " + netState + ", slotid: " + IWlanNetworkServiceProvider.this.getSlotIndex());
                            callback.onRequestNetworkRegistrationInfoComplete(0, netState);
                        } catch (Exception e) {
                            IWlanNetworkService iWlanNetworkService3 = IWlanNetworkService.this;
                            iWlanNetworkService3.loge("Exception: " + e);
                        }
                    }
                }
            };
        }

        public Handler getHandler() {
            return this.mHandler;
        }

        public void requestNetworkRegistrationInfo(int domain, NetworkServiceCallback callback) {
            IWlanNetworkService iWlanNetworkService = IWlanNetworkService.this;
            iWlanNetworkService.log("getNetworkRegistrationState for domain " + domain + ", slotid: " + getSlotIndex());
            if (domain == 2) {
                Message message = Message.obtain(this.mHandler, 2);
                this.mCallbackMap.put(message, callback);
                message.sendToTarget();
                return;
            }
            IWlanNetworkService iWlanNetworkService2 = IWlanNetworkService.this;
            iWlanNetworkService2.loge("getNetworkRegistrationState invalid domain " + domain + ", slotid: " + getSlotIndex());
            callback.onRequestNetworkRegistrationInfoComplete(2, (NetworkRegistrationInfo) null);
        }

        public void close() {
            IWlanNetworkService.this.log("close.");
            this.mCallbackMap.clear();
            this.mHandlerThread.quit();
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private NetworkRegistrationInfo createRegistrationState(int state) {
            int accessNetworkTechnology;
            int regState;
            IWlanNetworkService.this.log("createRegistrationState.");
            if (state == 1) {
                regState = 1;
                accessNetworkTechnology = 18;
            } else {
                regState = 0;
                accessNetworkTechnology = 0;
            }
            return new NetworkRegistrationInfo(2, 2, regState, accessNetworkTechnology, 0, false, IWlanNetworkService.this.getAvailableServices(regState, 2, false), null, 0, false, false, false, new LteVopsSupportInfo(1, 1), false);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private ArrayList<Integer> getAvailableServices(int regState, int domain, boolean emergencyOnly) {
        log("getAvailableServices.");
        if (emergencyOnly) {
            return new ArrayList<>(Arrays.asList(5));
        } else if ((regState != 5 && regState != 1) || domain != 2) {
            return null;
        } else {
            return new ArrayList<>(Arrays.asList(2));
        }
    }

    public NetworkService.NetworkServiceProvider onCreateNetworkServiceProvider(int slotIndex) {
        log("IWlan network service created for slot " + slotIndex);
        if (!SubscriptionManager.isValidSlotIndex(slotIndex)) {
            loge("Tried to Iwlan network service with invalid slotId " + slotIndex);
            return null;
        }
        this.mIWlanNetSrvProviderMap.remove(Integer.valueOf(slotIndex));
        this.mIWlanNetSrvProviderMap.put(Integer.valueOf(slotIndex), new IWlanNetworkServiceProvider(slotIndex));
        return this.mIWlanNetSrvProviderMap.get(Integer.valueOf(slotIndex));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void log(String s) {
        Rlog.d(TAG, s);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void loge(String s) {
        Rlog.e(TAG, s);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private IWifiOffloadListenerProxy createWifiOffloadListenerProxy() {
        if (this.mProxy == null) {
            log("create WifiOffloadListenerProxy");
            this.mProxy = new IWifiOffloadListenerProxy();
        }
        return this.mProxy;
    }

    /* access modifiers changed from: private */
    public class IWifiOffloadListenerProxy extends WifiOffloadManager.Listener {
        private IWifiOffloadListenerProxy() {
        }

        @Override // com.mediatek.wfo.IWifiOffloadListener, com.mediatek.wfo.WifiOffloadManager.Listener
        public void onWfcStateChanged(int simId, int state) {
            IWlanNetworkService iWlanNetworkService = IWlanNetworkService.this;
            iWlanNetworkService.log("onWfcStateChanged simIdx=" + simId + ", state=" + state);
            IWlanNetworkService.this.notifyWfcStateChanged(simId, state);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void notifyWfcStateChanged(int simId, int state) {
        if (!this.mIWlanNetSrvProviderMap.containsKey(Integer.valueOf(simId))) {
            log("IWlanNetworkServiceProvider id " + simId + " did not exist.");
            return;
        }
        log("notifyWfcStateChanged: " + state);
        this.mIWlanNetSrvProviderMap.get(Integer.valueOf(simId)).getHandler().obtainMessage(1, state, 0).sendToTarget();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void checkAndBindWifiOffloadService() {
        IBinder b = ServiceManager.getService(MwisConstants.MWI_SERVICE);
        if (b != null) {
            try {
                b.linkToDeath(this.mDeathRecipient, 0);
                this.sWifiOffloadService = IMwiService.Stub.asInterface(b).getWfcHandlerInterface();
            } catch (RemoteException e) {
                loge("can't get MwiService:" + e);
            }
        } else {
            log("No MwiService exist");
        }
        log("checkAndBindWifiOffloadService: sWifiOffloadService = " + this.sWifiOffloadService);
    }

    /* access modifiers changed from: private */
    public class IWifiOffloadServiceDeathRecipient implements IBinder.DeathRecipient {
        private IWifiOffloadServiceDeathRecipient() {
        }

        public void binderDied() {
            IWlanNetworkService.this.sWifiOffloadService = null;
            IWlanNetworkService.this.bindAndRegisterWifiOffloadService();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void bindAndRegisterWifiOffloadService() {
        new Thread(new Runnable() {
            /* class com.mediatek.internal.telephony.IWlanNetworkService.AnonymousClass1 */

            public void run() {
                while (IWlanNetworkService.this.sWifiOffloadService == null) {
                    IWlanNetworkService.this.checkAndBindWifiOffloadService();
                    if (IWlanNetworkService.this.sWifiOffloadService != null) {
                        try {
                            IWlanNetworkService.this.sWifiOffloadService.registerForHandoverEvent(IWlanNetworkService.this.createWifiOffloadListenerProxy());
                        } catch (RemoteException e) {
                            IWlanNetworkService.this.loge("can't register handover event");
                        }
                    } else if (SystemProperties.getInt("persist.vendor.mtk_wfc_support", 0) == 0) {
                        IWlanNetworkService.this.loge("can't get WifiOffloadService");
                        return;
                    }
                    if (IWlanNetworkService.this.sWifiOffloadService == null) {
                        IWlanNetworkService.this.loge("can't get WifiOffloadService, retry after 1s.");
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e2) {
                        }
                    } else {
                        return;
                    }
                }
            }
        }).start();
    }
}
