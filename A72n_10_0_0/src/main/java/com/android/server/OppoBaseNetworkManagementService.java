package com.android.server;

import android.content.Context;
import android.os.INetworkManagementService;

public abstract class OppoBaseNetworkManagementService extends INetworkManagementService.Stub {
    private static final String TAG = "OppoBaseNetworkManagementService";
    INetworkManagementServiceInner mInner;
    OppoLocalService mOppoLocalService;

    public OppoBaseNetworkManagementService() {
        this.mOppoLocalService = null;
        this.mInner = null;
    }

    public OppoBaseNetworkManagementService(Context context) {
        this.mOppoLocalService = null;
        this.mInner = null;
        this.mOppoLocalService = new OppoLocalService();
    }

    public void onOppoInit(INetworkManagementServiceInner inner) {
        LocalServices.addService(OppoNetworkManagementInternal.class, this.mOppoLocalService);
        this.mInner = inner;
    }

    final class OppoLocalService extends OppoNetworkManagementInternal {
        OppoLocalService() {
        }

        @Override // com.android.server.OppoNetworkManagementInternal
        public void closeSocketsForHans(int chain, String chainName) {
            if (OppoBaseNetworkManagementService.this.mInner != null) {
                OppoBaseNetworkManagementService.this.mInner.closeSocketsForHans(chain, chainName);
            }
        }
    }
}
