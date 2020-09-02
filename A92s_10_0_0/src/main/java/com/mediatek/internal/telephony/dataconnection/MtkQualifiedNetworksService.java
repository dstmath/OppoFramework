package com.mediatek.internal.telephony.dataconnection;

import android.os.AsyncResult;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.telephony.Rlog;
import android.telephony.SubscriptionManager;
import android.telephony.data.QualifiedNetworksService;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneFactory;
import java.util.ArrayList;
import java.util.List;

public class MtkQualifiedNetworksService extends QualifiedNetworksService {
    private static final String TAG = MtkQualifiedNetworksService.class.getSimpleName();
    private static final int UPDATE_QUALIFIED_NETWORKS = 1;

    public class MtkNetworkAvailabilityProvider extends QualifiedNetworksService.NetworkAvailabilityProvider {
        private static final boolean DBG = true;
        private static final int MD_NW_TYPE_CELLULAR = 1;
        private static final int MD_NW_TYPE_IWLAN = 2;
        private static final int MD_NW_TYPE_UNKNOWN = 0;
        private final Handler mHandler;
        private final HandlerThread mHandlerThread = new HandlerThread(MtkQualifiedNetworksService.class.getSimpleName());
        private final Looper mLooper;
        private final Phone mPhone = PhoneFactory.getPhone(getSlotIndex());

        public MtkNetworkAvailabilityProvider(int slotIndex) {
            super(MtkQualifiedNetworksService.this, slotIndex);
            this.mHandlerThread.start();
            this.mLooper = this.mHandlerThread.getLooper();
            this.mHandler = new Handler(this.mLooper, MtkQualifiedNetworksService.this) {
                /* class com.mediatek.internal.telephony.dataconnection.MtkQualifiedNetworksService.MtkNetworkAvailabilityProvider.AnonymousClass1 */

                public void handleMessage(Message message) {
                    AsyncResult ar = (AsyncResult) message.obj;
                    if (message.what != 1) {
                        MtkQualifiedNetworksService mtkQualifiedNetworksService = MtkQualifiedNetworksService.this;
                        mtkQualifiedNetworksService.loge("Unexpected event: " + message.what);
                        return;
                    }
                    int[] availabilityUpdate = (int[]) ar.result;
                    int length = availabilityUpdate.length;
                    List<Integer> qualifiedNetworkTypes = new ArrayList<>();
                    int mode = availabilityUpdate[0];
                    int apnTypes = availabilityUpdate[1];
                    MtkQualifiedNetworksService mtkQualifiedNetworksService2 = MtkQualifiedNetworksService.this;
                    mtkQualifiedNetworksService2.log("UPDATE_QUALIFIED_NETWORKS mode=" + mode + " apnTypes=" + apnTypes);
                    for (int i = 2; i < length; i++) {
                        qualifiedNetworkTypes.add(Integer.valueOf(MtkNetworkAvailabilityProvider.this.converNetworkType(availabilityUpdate[i])));
                        MtkQualifiedNetworksService mtkQualifiedNetworksService3 = MtkQualifiedNetworksService.this;
                        mtkQualifiedNetworksService3.log("availabilityUpdate[" + i + "]=" + MtkNetworkAvailabilityProvider.this.converNetworkType(availabilityUpdate[i]));
                    }
                    MtkNetworkAvailabilityProvider.this.updateQualifiedNetworkTypes(apnTypes, qualifiedNetworkTypes);
                }
            };
            MtkQualifiedNetworksService.this.log("Register for qyalified networks changed.");
            this.mPhone.mCi.registerForQualifiedNetworkTypesChanged(this.mHandler, 1, null);
        }

        public void close() {
            this.mPhone.mCi.unregisterForQualifiedNetworkTypesChanged(this.mHandler);
            this.mHandlerThread.quit();
        }

        public int converNetworkType(int mdReportType) {
            if (mdReportType == 1) {
                return 3;
            }
            if (mdReportType != 2) {
                return 0;
            }
            return 5;
        }
    }

    public QualifiedNetworksService.NetworkAvailabilityProvider onCreateNetworkAvailabilityProvider(int slotIndex) {
        log("MtkQNS create MtkNetworkAvailabilityProvider for slot " + slotIndex);
        if (SubscriptionManager.isValidSlotIndex(slotIndex)) {
            return new MtkNetworkAvailabilityProvider(slotIndex);
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
