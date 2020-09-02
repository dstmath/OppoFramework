package com.android.server.display;

import android.common.OppoFeatureCache;
import android.content.Context;
import android.hardware.display.IDisplayManager;
import android.os.Bundle;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.Slog;
import com.android.server.ColorServiceFactory;
import com.android.server.SystemService;

public abstract class OppoBaseDisplayManagerService extends SystemService {
    private static final String TAG = "OppoBaseDisplayManagerService";
    public IColorDisplayManagerServiceEx mDMSEx;
    /* access modifiers changed from: private */
    public DisplayPowerController mDisplayPowerController;

    public OppoBaseDisplayManagerService(Context context) {
        super(context);
        this.mDMSEx = null;
        this.mDMSEx = ColorServiceFactory.getInstance().getFeature(IColorDisplayManagerServiceEx.DEFAULT, new Object[]{getContext(), this});
    }

    public boolean setStateChanged(int msgId, Bundle extraData) throws RemoteException {
        return OppoFeatureCache.getOrCreate(IOppoBrightness.DEFAULT, new Object[0]).getAIBrightness().setStateChanged(msgId, extraData);
    }

    public abstract class ColorDisplayBaseBinderService extends OppoBaseBinderService {
        /* access modifiers changed from: package-private */
        public abstract void printAutoLuxInterval();

        public ColorDisplayBaseBinderService() {
            super();
        }

        @Override // com.android.server.display.OppoBaseDisplayManagerService.OppoBaseBinderService
        public /* bridge */ /* synthetic */ boolean onTransact(int i, Parcel parcel, Parcel parcel2, int i2) throws RemoteException {
            return super.onTransact(i, parcel, parcel2, i2);
        }
    }

    /* access modifiers changed from: protected */
    public void onOppoStart() {
        Slog.i(TAG, "onOppoStart");
        publishLocalService(OppoDisplayManagerInternal.class, new OppoLocalService());
    }

    /* access modifiers changed from: protected */
    public void initPowerManagement(DisplayPowerController dpc) {
        Slog.i(TAG, "initPowerManagement");
        this.mDisplayPowerController = dpc;
    }

    final class OppoLocalService extends OppoDisplayManagerInternal {
        OppoLocalService() {
        }

        @Override // com.android.server.display.OppoDisplayManagerInternal
        public boolean isBlockScreenOnByBiometrics() {
            if (OppoBaseDisplayManagerService.this.mDisplayPowerController == null || OppoMirrorDisplayPowerController.isBlockScreenOnByBiometrics == null) {
                return false;
            }
            return ((Boolean) OppoMirrorDisplayPowerController.isBlockScreenOnByBiometrics.call(OppoBaseDisplayManagerService.this.mDisplayPowerController, new Object[0])).booleanValue();
        }
    }

    protected abstract class OppoBaseBinderService extends IDisplayManager.Stub {
        protected OppoBaseBinderService() {
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (OppoBaseDisplayManagerService.super.onTransact(code, data, reply, flags)) {
                return true;
            }
            if (OppoBaseDisplayManagerService.this.mDMSEx == null || !OppoBaseDisplayManagerService.this.mDMSEx.onTransact(code, data, reply, flags)) {
                return false;
            }
            return true;
        }
    }
}
