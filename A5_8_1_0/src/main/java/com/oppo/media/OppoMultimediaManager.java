package com.oppo.media;

import android.content.Context;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.util.Log;
import com.oppo.media.IOppoMultimediaService.Stub;

public class OppoMultimediaManager {
    public static final int ERROR = -1;
    public static final String SERVICE_NAME = "multimediaDaemon";
    public static final int SUCCESS = 0;
    private static final String TAG = "OppoMultimediaManager";
    private static OppoMultimediaManager sInstance;
    private static IOppoMultimediaService sMultimediaService;
    private static boolean sServiceEnable = true;
    private final Context mContext;

    private OppoMultimediaManager(Context context) {
        this.mContext = context;
    }

    public static OppoMultimediaManager getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new OppoMultimediaManager(context);
        }
        if (SystemProperties.getBoolean("persist.sys.multimedia.service", false)) {
            sServiceEnable = false;
        } else {
            sServiceEnable = true;
        }
        return sInstance;
    }

    private static IOppoMultimediaService getService() {
        if (!sServiceEnable) {
            DebugLog.d(TAG, "MultimediaService disabled");
            return null;
        } else if (sMultimediaService != null) {
            return sMultimediaService;
        } else {
            sMultimediaService = Stub.asInterface(ServiceManager.getService(SERVICE_NAME));
            DebugLog.d(TAG, "sMultimediaService = " + sMultimediaService);
            return sMultimediaService;
        }
    }

    public void sendMessage(String info) {
        if (info != null) {
            String[] args = info.split(":");
            if (args.length != 2) {
                Log.d(TAG, "info = " + info + " format is error,check it");
            } else {
                try {
                    setEventInfo(Integer.parseInt(args[0]), args[1]);
                } catch (NumberFormatException e) {
                }
            }
        }
    }

    public void setEventInfo(int event, String info) {
        IOppoMultimediaService service = getService();
        DebugLog.d(TAG, "setEventInfo info = " + info + " service = " + service);
        if (service != null) {
            try {
                service.setEventInfo(event, info);
            } catch (RemoteException e) {
                Log.e(TAG, "Dead object in info", e);
            }
        }
    }

    public void setParameters(String keyValuePairs) {
        IOppoMultimediaService service = getService();
        DebugLog.d(TAG, "setParameters keyValuePairs = " + keyValuePairs + " service = " + service);
        if (service != null) {
            try {
                service.setParameters(keyValuePairs);
            } catch (RemoteException e) {
                Log.e(TAG, "Dead object in info", e);
            }
        }
    }

    public String getParameters(String keys) {
        IOppoMultimediaService service = getService();
        if (service == null) {
            return null;
        }
        try {
            return service.getParameters(keys);
        } catch (RemoteException e) {
            Log.e(TAG, "Dead object in info", e);
            return null;
        }
    }
}
