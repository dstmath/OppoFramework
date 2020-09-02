package com.oppo.atlas;

import android.content.Context;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.provider.SettingsStringUtil;
import com.oppo.atlas.IOppoMMAtlasService;

public class OppoAtlasManager {
    public static final String SERVICE_NAME = "multimediaDaemon";
    private static final String TAG = "OppoAtlasManager";
    private static IOppoMMAtlasService mOppoAtlasService;
    private static volatile OppoAtlasManager sInstance = null;
    private static boolean sServiceEnable = true;
    private boolean mBindServiceFlag = false;
    private final Context mContext;

    private OppoAtlasManager(Context context) {
        this.mContext = context;
    }

    public static OppoAtlasManager getInstance(Context context) {
        if (sInstance == null) {
            synchronized (OppoAtlasManager.class) {
                if (sInstance == null) {
                    sInstance = new OppoAtlasManager(context);
                }
            }
        }
        if (SystemProperties.getBoolean("persist.sys.multimedia.atlas.service", false)) {
            sServiceEnable = false;
        } else {
            sServiceEnable = true;
        }
        return sInstance;
    }

    private static IOppoMMAtlasService getService() {
        if (!sServiceEnable) {
            DebugLog.d(TAG, "OppoAtlasService disabled");
            return null;
        }
        IOppoMMAtlasService iOppoMMAtlasService = mOppoAtlasService;
        if (iOppoMMAtlasService != null) {
            return iOppoMMAtlasService;
        }
        mOppoAtlasService = IOppoMMAtlasService.Stub.asInterface(ServiceManager.getService(SERVICE_NAME));
        return mOppoAtlasService;
    }

    public void sendMessage(String info) {
        if (info != null) {
            String[] args = info.split(SettingsStringUtil.DELIMITER);
            if (args.length != 2) {
                DebugLog.d(TAG, "info = " + info + " format is error,check it");
                return;
            }
            try {
                setEvent(Integer.parseInt(args[0]), args[1]);
            } catch (NumberFormatException e) {
            }
        }
    }

    public void setEvent(int event, String info) {
        IOppoMMAtlasService service = getService();
        DebugLog.d(TAG, "setEventInfo info = " + info + " service = " + service);
        if (service != null) {
            try {
                service.setEvent(event, info);
            } catch (RemoteException e) {
                DebugLog.e(TAG, "Dead object in info", e);
            }
        }
    }

    public void setParameters(String keyValuePairs) {
        IOppoMMAtlasService service = getService();
        DebugLog.d(TAG, "setParameters keyValuePairs = " + keyValuePairs + " service = " + service);
        if (service != null) {
            try {
                service.setParameters(keyValuePairs);
            } catch (RemoteException e) {
                DebugLog.e(TAG, "Dead object in info", e);
            }
        }
    }

    public String getParameters(String keys) {
        IOppoMMAtlasService service = getService();
        if (service == null) {
            return null;
        }
        try {
            return service.getParameters(keys);
        } catch (RemoteException e) {
            DebugLog.e(TAG, "Dead object in info", e);
            return null;
        }
    }

    public void registerServiceCallback(IOppoAtlasServiceCallback callback) {
        IOppoMMAtlasService service = getService();
        if (service != null) {
            try {
                service.registerCallback(callback);
            } catch (RemoteException e) {
                DebugLog.e(TAG, "Dead object in info", e);
            }
        }
    }

    public void unRegisterServiceCallback(IOppoAtlasServiceCallback callback) {
        IOppoMMAtlasService service = getService();
        if (service != null) {
            try {
                service.unRegisterCallback(callback);
            } catch (RemoteException e) {
                DebugLog.e(TAG, "Dead object in info", e);
            }
        }
    }

    public void registerAudioCallback(IOppoAtlasAudioCallback callback) {
        IOppoMMAtlasService service = getService();
        if (service != null) {
            try {
                service.registerAudioCallback(callback);
            } catch (RemoteException e) {
                DebugLog.e(TAG, "Dead object in info", e);
            }
        }
    }

    public void unRegisterAudioCallback(IOppoAtlasAudioCallback callback) {
        IOppoMMAtlasService service = getService();
        if (service != null) {
            try {
                service.unRegisterAudioCallback(callback);
            } catch (RemoteException e) {
                DebugLog.e(TAG, "Dead object in info", e);
            }
        }
    }
}
