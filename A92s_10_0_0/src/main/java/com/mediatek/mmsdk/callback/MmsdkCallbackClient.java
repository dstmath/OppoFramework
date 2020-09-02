package com.mediatek.mmsdk.callback;

import android.content.Context;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import android.view.Surface;
import com.mediatek.mmsdk.BaseParameters;
import com.mediatek.mmsdk.BinderHolder;
import com.mediatek.mmsdk.CameraEffectHalException;
import com.mediatek.mmsdk.CameraEffectHalRuntimeException;
import com.mediatek.mmsdk.EffectHalVersion;
import com.mediatek.mmsdk.IEffectFactory;
import com.mediatek.mmsdk.IFeatureManager;
import com.mediatek.mmsdk.IMMSdkService;
import com.mediatek.mmsdk.callback.ICallbackClient;
import java.util.List;

public class MmsdkCallbackClient {
    public static final int CAMERA_MSG_COMPRESSED_IMAGE = 256;
    private static final String TAG = "MmsdkCallbackClient";
    private ICallbackClient mICallbackClient;
    private IEffectFactory mIEffectFactory;
    private IFeatureManager mIFeatureManager;
    private IMMSdkService mIMmsdkService;

    public MmsdkCallbackClient(Context context) {
    }

    public boolean isCallbackClientSupported() {
        try {
            return getEffectFactory() != null && isCallbackSupported();
        } catch (CameraEffectHalException e) {
            Log.e(TAG, "Current not support Effect HAl", e);
            return false;
        }
    }

    public void start() throws CameraEffectHalException {
        init();
        try {
            this.mICallbackClient.start();
        } catch (RemoteException e1) {
            Log.e(TAG, "RemoteException during start", e1);
            throw new CameraEffectHalException(CameraEffectHalException.EFFECT_INITIAL_ERROR);
        }
    }

    public void stop() throws CameraEffectHalException {
        try {
            this.mICallbackClient.stop();
        } catch (RemoteException e1) {
            Log.e(TAG, "RemoteException during stop", e1);
            throw new CameraEffectHalException(CameraEffectHalException.EFFECT_HAL_ERROR);
        }
    }

    public void setOutputSurfaces(List<Surface> outputs, List<BaseParameters> parameters) throws CameraEffectHalException {
        try {
            this.mICallbackClient.setOutputSurfaces(outputs, parameters);
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException during set Listener", e);
            throw new CameraEffectHalRuntimeException(CameraEffectHalException.EFFECT_HAL_LISTENER_ERROR).asChecked();
        }
    }

    private void init() throws CameraEffectHalException {
        getMmSdkService();
        getFeatureManager();
        getEffectFactory();
        this.mICallbackClient = createCallbackClient(new EffectHalVersion());
    }

    private IMMSdkService getMmSdkService() throws CameraEffectHalException {
        if (this.mIMmsdkService == null) {
            IBinder mmsdkService = ServiceManager.getService(BaseParameters.CAMERA_MM_SERVICE_BINDER_NAME);
            if (mmsdkService != null) {
                this.mIMmsdkService = IMMSdkService.Stub.asInterface(mmsdkService);
            } else {
                throw new CameraEffectHalException(101);
            }
        }
        return this.mIMmsdkService;
    }

    private boolean isCallbackSupported() throws CameraEffectHalException {
        getMmSdkService();
        try {
            boolean isSupport = true;
            if (this.mIMmsdkService.existCallbackClient() != 1) {
                isSupport = false;
            }
            return isSupport;
        } catch (RemoteException e) {
            throw new CameraEffectHalException(101);
        }
    }

    private IFeatureManager getFeatureManager() throws CameraEffectHalException {
        getMmSdkService();
        if (this.mIFeatureManager == null) {
            BinderHolder featureManagerHolder = new BinderHolder();
            try {
                this.mIMmsdkService.connectFeatureManager(featureManagerHolder);
                this.mIFeatureManager = IFeatureManager.Stub.asInterface(featureManagerHolder.getBinder());
            } catch (RemoteException e) {
                throw new CameraEffectHalException(CameraEffectHalException.EFFECT_HAL_FEATUREMANAGER_ERROR);
            }
        }
        return this.mIFeatureManager;
    }

    private IEffectFactory getEffectFactory() throws CameraEffectHalException {
        getFeatureManager();
        if (this.mIEffectFactory == null) {
            BinderHolder effectFactoryHolder = new BinderHolder();
            try {
                this.mIFeatureManager.getEffectFactory(effectFactoryHolder);
                this.mIEffectFactory = IEffectFactory.Stub.asInterface(effectFactoryHolder.getBinder());
            } catch (RemoteException e) {
                throw new CameraEffectHalException(CameraEffectHalException.EFFECT_HAL_FACTORY_ERROR);
            }
        }
        return this.mIEffectFactory;
    }

    private ICallbackClient createCallbackClient(EffectHalVersion version) throws CameraEffectHalException {
        getEffectFactory();
        BinderHolder callbackClientHolder = new BinderHolder();
        try {
            this.mIEffectFactory.createCallbackClient(version, callbackClientHolder);
            return ICallbackClient.Stub.asInterface(callbackClientHolder.getBinder());
        } catch (RemoteException e) {
            throw new CameraEffectHalException(105);
        }
    }
}
