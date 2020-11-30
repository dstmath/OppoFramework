package com.oppo.omedia;

import android.hardware.camera2.utils.SurfaceUtils;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.telephony.SmsManager;
import android.util.Size;
import android.util.Slog;
import android.view.Surface;
import java.util.List;

public class OMediaProxy {
    private static final String DESCRIPTOR = "com.oppo.omedia.IOMediaService";
    private static final int NORMAL_OPERATING_MODE = 0;
    private static final int OMEDIA_OFF = 0;
    private static final String TAG = "OMediaProxy";
    private static final int TRANSACTION_CLOSE_SESSION = 6;
    private static final int TRANSACTION_GET_OPERATING_MODE = 5;
    private static OMediaProxy sMediaProxyService = null;
    private static int sOmediaSysEnabledProperty = SystemProperties.getInt("persist.sys.omedia.enable", 0);
    private IBinder.DeathRecipient mDeathRecipient = new IBinder.DeathRecipient() {
        /* class com.oppo.omedia.OMediaProxy.AnonymousClass1 */

        @Override // android.os.IBinder.DeathRecipient
        public void binderDied() {
            Slog.e(OMediaProxy.TAG, "omedia service binder die.");
            if (OMediaProxy.this.mRemote != null) {
                OMediaProxy.this.mRemote.unlinkToDeath(OMediaProxy.this.mDeathRecipient, 0);
                OMediaProxy.this.mRemote = null;
            }
        }
    };
    private IBinder mRemote;

    private OMediaProxy() {
        connectService();
    }

    private boolean connectService() {
        if (sOmediaSysEnabledProperty == 0) {
            return false;
        }
        this.mRemote = ServiceManager.checkService("omedia");
        IBinder iBinder = this.mRemote;
        if (iBinder == null) {
            return false;
        }
        try {
            iBinder.linkToDeath(this.mDeathRecipient, 0);
            return true;
        } catch (RemoteException e) {
            return false;
        }
    }

    public static synchronized OMediaProxy getInstance() {
        OMediaProxy oMediaProxy;
        synchronized (OMediaProxy.class) {
            if (sMediaProxyService == null) {
                sMediaProxyService = new OMediaProxy();
            }
            oMediaProxy = sMediaProxyService;
        }
        return oMediaProxy;
    }

    public int getOperatingMode(List<Surface> surfaces, String camId) {
        if (sOmediaSysEnabledProperty == 0) {
            return 0;
        }
        if (this.mRemote == null && !connectService()) {
            return 0;
        }
        try {
            int tmpmode = getOperatingModeRemote(getStreamInfoFromSurface(surfaces, camId));
            if (tmpmode <= 0) {
                return 0;
            }
            Slog.d(TAG, "omedia mode is " + tmpmode);
            return tmpmode;
        } catch (Exception e) {
            Slog.e(TAG, "catch a omedia 'get operating mode' Exception");
            return 0;
        }
    }

    private int getOperatingModeRemote(String param) throws RemoteException {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        try {
            _data.writeInterfaceToken(DESCRIPTOR);
            _data.writeString(param);
            this.mRemote.transact(5, _data, _reply, 0);
            _reply.readException();
            return _reply.readInt();
        } finally {
            _reply.recycle();
            _data.recycle();
        }
    }

    public static String getStreamInfoFromSurface(List<Surface> surfaces, String camId) {
        String type;
        if (surfaces.size() == 0) {
            return "{}";
        }
        try {
            String strStreamCnt = Integer.toString(surfaces.size());
            String streamSize = "";
            for (Surface surface : surfaces) {
                Size size = SurfaceUtils.getSurfaceSize(surface);
                if (streamSize != null && !streamSize.isEmpty()) {
                    streamSize = streamSize + SmsManager.REGEX_PREFIX_DELIMITER;
                }
                int format = SurfaceUtils.getSurfaceFormat(surface);
                if (format != 256) {
                    switch (format) {
                        case 33:
                            break;
                        case 34:
                            type = "PreviewSurface";
                            break;
                        case 35:
                            type = "PreviewYuv";
                            break;
                        default:
                            type = "UnKown" + format;
                            break;
                    }
                    if (streamSize != null && !streamSize.isEmpty() && streamSize.contains(type)) {
                        type = type + surfaces.indexOf(surface);
                    }
                    streamSize = streamSize + "\"" + type + "\":\"" + size.getWidth() + "x" + size.getHeight() + "\"";
                }
                type = "Jpeg";
                type = type + surfaces.indexOf(surface);
                streamSize = streamSize + "\"" + type + "\":\"" + size.getWidth() + "x" + size.getHeight() + "\"";
            }
            return "{\"CamId\":" + camId + ",\"StreamCount\":" + strStreamCnt + SmsManager.REGEX_PREFIX_DELIMITER + streamSize + "}";
        } catch (Exception e) {
            e.printStackTrace();
            return "{}";
        }
    }

    public boolean sendCameraDeviceClose(String param) {
        boolean z = false;
        if (sOmediaSysEnabledProperty == 0 || (this.mRemote == null && !connectService())) {
            return false;
        }
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        boolean ret = false;
        try {
            _data.writeInterfaceToken(DESCRIPTOR);
            _data.writeString(param);
            this.mRemote.transact(6, _data, _reply, 0);
            _reply.readException();
            if (_reply.readInt() > 0) {
                z = true;
            }
            ret = z;
        } catch (Exception e) {
            Slog.e(TAG, "catch a omedia 'send close time' Exception");
        } catch (Throwable th) {
            _data.recycle();
            _reply.recycle();
            throw th;
        }
        _data.recycle();
        _reply.recycle();
        return ret;
    }
}
