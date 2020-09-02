package com.mediatek.mmsdk;

import android.content.Context;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import com.mediatek.mmsdk.CameraEffect;
import com.mediatek.mmsdk.IEffectFactory;
import com.mediatek.mmsdk.IEffectHalClient;
import com.mediatek.mmsdk.IFeatureManager;
import com.mediatek.mmsdk.IMMSdkService;
import java.util.ArrayList;
import java.util.List;

public class CameraEffectManager {
    private static final String CAMERA_MM_SERVICE_BINDER_NAME = "media.mmsdk";
    private static final String TAG = "CameraEffectManager";
    private final Context mContext;
    private IEffectFactory mIEffectFactory;
    private IFeatureManager mIFeatureManager;
    private IMMSdkService mIMmsdkService;

    public CameraEffectManager(Context context) {
        this.mContext = context;
    }

    public CameraEffect openEffectHal(EffectHalVersion version, CameraEffect.StateCallback callback, Handler handler) throws CameraEffectHalException {
        if (version != null) {
            if (handler == null) {
                if (Looper.myLooper() != null) {
                    handler = new Handler();
                } else {
                    throw new IllegalArgumentException("Looper doesn't exist in the calling thread");
                }
            }
            return openEffect(version, callback, handler);
        }
        throw new IllegalArgumentException("effect version is null");
    }

    public List<EffectHalVersion> getSupportedVersion(String effectName) throws CameraEffectHalException {
        List<EffectHalVersion> version = new ArrayList<>();
        getEffectFactory();
        try {
            this.mIEffectFactory.getSupportedVersion(effectName, version);
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException during getSupportedVersion", e);
        }
        return version;
    }

    private CameraEffect openEffect(EffectHalVersion version, CameraEffect.StateCallback callback, Handler handler) throws CameraEffectHalException {
        getMmSdkService();
        getFeatureManager();
        getEffectFactory();
        IEffectHalClient effectHalClient = createEffectHalClient(version);
        try {
            int initValue = effectHalClient.init();
            CameraEffectImpl cameraEffectImpl = new CameraEffectImpl(callback, handler);
            try {
                int setListenerValue = effectHalClient.setEffectListener(cameraEffectImpl.getEffectHalListener());
                cameraEffectImpl.setRemoteCameraEffect(effectHalClient);
                Log.i(TAG, "[openEffect],version = " + version + ",initValue = " + initValue + ",setListenerValue = " + setListenerValue + ",cameraEffect = " + cameraEffectImpl);
                return cameraEffectImpl;
            } catch (RemoteException e) {
                Log.e(TAG, "RemoteException during setEffectListener", e);
                CameraEffectHalRuntimeException exception = new CameraEffectHalRuntimeException(CameraEffectHalException.EFFECT_HAL_LISTENER_ERROR);
                cameraEffectImpl.setRemoteCameraEffectFail(exception);
                throw exception.asChecked();
            }
        } catch (RemoteException e1) {
            Log.e(TAG, "RemoteException during init", e1);
            throw new CameraEffectHalException(CameraEffectHalException.EFFECT_INITIAL_ERROR);
        }
    }

    private IMMSdkService getMmSdkService() throws CameraEffectHalException {
        if (this.mIMmsdkService == null) {
            IBinder mmsdkService = ServiceManager.getService("media.mmsdk");
            if (mmsdkService != null) {
                this.mIMmsdkService = IMMSdkService.Stub.asInterface(mmsdkService);
            } else {
                throw new CameraEffectHalException(101);
            }
        }
        return this.mIMmsdkService;
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

    private IEffectHalClient createEffectHalClient(EffectHalVersion version) throws CameraEffectHalException {
        getEffectFactory();
        BinderHolder effectHalClientHolder = new BinderHolder();
        try {
            this.mIEffectFactory.createEffectHalClient(version, effectHalClientHolder);
            return IEffectHalClient.Stub.asInterface(effectHalClientHolder.getBinder());
        } catch (RemoteException e) {
            throw new CameraEffectHalException(105);
        }
    }
}
