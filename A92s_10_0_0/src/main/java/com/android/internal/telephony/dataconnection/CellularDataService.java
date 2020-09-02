package com.android.internal.telephony.dataconnection;

import android.net.LinkProperties;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import android.telephony.Rlog;
import android.telephony.SubscriptionManager;
import android.telephony.data.DataCallResponse;
import android.telephony.data.DataProfile;
import android.telephony.data.DataService;
import android.telephony.data.DataServiceCallback;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneFactory;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CellularDataService extends DataService {
    private static final int DATA_CALL_LIST_CHANGED = 6;
    /* access modifiers changed from: private */
    public static final boolean DBG = SystemProperties.getBoolean("ro.sys.engineering.pre", false);
    private static final int DEACTIVATE_DATA_ALL_COMPLETE = 2;
    private static final int REQUEST_DATA_CALL_LIST_COMPLETE = 5;
    private static final int SETUP_DATA_CALL_COMPLETE = 1;
    private static final int SET_DATA_PROFILE_COMPLETE = 4;
    private static final int SET_INITIAL_ATTACH_APN_COMPLETE = 3;
    private static final String TAG = CellularDataService.class.getSimpleName();

    private class CellularDataServiceProvider extends DataService.DataServiceProvider {
        /* access modifiers changed from: private */
        public final Map<Message, DataServiceCallback> mCallbackMap;
        private final Handler mHandler;
        private final HandlerThread mHandlerThread;
        private final Looper mLooper;
        private final Phone mPhone;

        private CellularDataServiceProvider(int slotId) {
            super(CellularDataService.this, slotId);
            this.mCallbackMap = new HashMap();
            this.mPhone = PhoneFactory.getPhone(getSlotIndex());
            this.mHandlerThread = new HandlerThread(CellularDataService.class.getSimpleName());
            this.mHandlerThread.start();
            this.mLooper = this.mHandlerThread.getLooper();
            this.mHandler = new Handler(this.mLooper, CellularDataService.this) {
                /* class com.android.internal.telephony.dataconnection.CellularDataService.CellularDataServiceProvider.AnonymousClass1 */

                public void handleMessage(Message message) {
                    List list;
                    if (CellularDataService.DBG) {
                        CellularDataService.this.log("handleMessage " + message);
                    }
                    DataServiceCallback callback = (DataServiceCallback) CellularDataServiceProvider.this.mCallbackMap.remove(message);
                    AsyncResult ar = (AsyncResult) message.obj;
                    int i = 4;
                    switch (message.what) {
                        case 1:
                            DataCallResponse response = (DataCallResponse) ar.result;
                            if (ar.exception == null) {
                                i = 0;
                            }
                            callback.onSetupDataCallComplete(i, response);
                            return;
                        case 2:
                            if (ar.exception == null) {
                                i = 0;
                            }
                            callback.onDeactivateDataCallComplete(i);
                            return;
                        case 3:
                            if (ar.exception == null) {
                                i = 0;
                            }
                            callback.onSetInitialAttachApnComplete(i);
                            return;
                        case 4:
                            if (ar.exception == null) {
                                i = 0;
                            }
                            callback.onSetDataProfileComplete(i);
                            return;
                        case 5:
                            if (ar.exception == null) {
                                i = 0;
                            }
                            if (ar.exception != null) {
                                list = null;
                            } else {
                                list = (List) ar.result;
                            }
                            callback.onRequestDataCallListComplete(i, list);
                            return;
                        case 6:
                            CellularDataServiceProvider.this.notifyDataCallListChanged((List) ar.result);
                            return;
                        default:
                            CellularDataService.this.loge("Unexpected event: " + message.what);
                            return;
                    }
                }
            };
            if (CellularDataService.DBG) {
                CellularDataService.this.log("Register for data call list changed.");
            }
            this.mPhone.mCi.registerForDataCallListChanged(this.mHandler, 6, null);
        }

        public void setupDataCall(int accessNetworkType, DataProfile dataProfile, boolean isRoaming, boolean allowRoaming, int reason, LinkProperties linkProperties, DataServiceCallback callback) {
            if (CellularDataService.DBG) {
                CellularDataService cellularDataService = CellularDataService.this;
                cellularDataService.log("setupDataCall " + getSlotIndex());
            }
            Message message = null;
            if (callback != null) {
                message = Message.obtain(this.mHandler, 1);
                this.mCallbackMap.put(message, callback);
                if (CellularDataService.DBG) {
                    CellularDataService cellularDataService2 = CellularDataService.this;
                    cellularDataService2.log("setupDataCall " + message);
                }
            }
            this.mPhone.mCi.setupDataCall(accessNetworkType, dataProfile, isRoaming, allowRoaming, reason, linkProperties, message);
        }

        public void deactivateDataCall(int cid, int reason, DataServiceCallback callback) {
            if (CellularDataService.DBG) {
                CellularDataService cellularDataService = CellularDataService.this;
                cellularDataService.log("deactivateDataCall " + getSlotIndex());
            }
            Message message = null;
            if (callback != null) {
                message = Message.obtain(this.mHandler, 2);
                this.mCallbackMap.put(message, callback);
                if (CellularDataService.DBG) {
                    CellularDataService cellularDataService2 = CellularDataService.this;
                    cellularDataService2.log("deactivateDataCall " + message);
                }
            }
            this.mPhone.mCi.deactivateDataCall(cid, reason, message);
        }

        public void setInitialAttachApn(DataProfile dataProfile, boolean isRoaming, DataServiceCallback callback) {
            if (CellularDataService.DBG) {
                CellularDataService cellularDataService = CellularDataService.this;
                cellularDataService.log("setInitialAttachApn " + getSlotIndex());
            }
            Message message = null;
            if (callback != null) {
                message = Message.obtain(this.mHandler, 3);
                this.mCallbackMap.put(message, callback);
            }
            this.mPhone.mCi.setInitialAttachApn(dataProfile, isRoaming, message);
        }

        public void setDataProfile(List<DataProfile> dps, boolean isRoaming, DataServiceCallback callback) {
            if (CellularDataService.DBG) {
                CellularDataService cellularDataService = CellularDataService.this;
                cellularDataService.log("setDataProfile " + getSlotIndex());
            }
            Message message = null;
            if (callback != null) {
                message = Message.obtain(this.mHandler, 4);
                this.mCallbackMap.put(message, callback);
            }
            this.mPhone.mCi.setDataProfile((DataProfile[]) dps.toArray(new DataProfile[dps.size()]), isRoaming, message);
        }

        public void requestDataCallList(DataServiceCallback callback) {
            if (CellularDataService.DBG) {
                CellularDataService cellularDataService = CellularDataService.this;
                cellularDataService.log("requestDataCallList " + getSlotIndex());
            }
            Message message = null;
            if (callback != null) {
                message = Message.obtain(this.mHandler, 5);
                this.mCallbackMap.put(message, callback);
            }
            this.mPhone.mCi.getDataCallList(message);
        }

        public void close() {
            this.mPhone.mCi.unregisterForDataCallListChanged(this.mHandler);
            this.mHandlerThread.quit();
        }
    }

    public DataService.DataServiceProvider onCreateDataServiceProvider(int slotIndex) {
        log("Cellular data service created for slot " + slotIndex);
        if (SubscriptionManager.isValidSlotIndex(slotIndex)) {
            return new CellularDataServiceProvider(slotIndex);
        }
        loge("Tried to cellular data service with invalid slotId " + slotIndex);
        return null;
    }

    /* access modifiers changed from: private */
    public void log(String s) {
        Rlog.d(TAG, s);
    }

    /* access modifiers changed from: private */
    public void loge(String s) {
        Rlog.e(TAG, s);
    }
}
