package com.android.server.om;

import android.common.OppoFeatureCache;
import android.content.om.IOverlayManager;
import android.os.Parcel;
import android.os.RemoteException;

public abstract class OppoOverlayManagerInnerService extends IOverlayManager.Stub {
    public OppoOverlayManagerInnerService() {
    }

    @Deprecated
    public OppoOverlayManagerInnerService(IColorLanguageManager colorLanguageManager) {
    }

    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        if (code != 10002) {
            return OppoOverlayManagerInnerService.super.onTransact(code, data, reply, flags);
        }
        data.enforceInterface("android.content.om.IOverlayManager");
        OppoFeatureCache.get(IColorLanguageManager.DEFAULT).setLanguageEnable(data.readString(), data.readInt());
        return true;
    }
}
