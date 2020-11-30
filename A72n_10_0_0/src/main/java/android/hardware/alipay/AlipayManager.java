package android.hardware.alipay;

import android.content.Context;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.util.Slog;

public class AlipayManager {
    public static final int AUTH_TYPE_FACE = 4;
    public static final int AUTH_TYPE_FINGERPRINT = 1;
    public static final int AUTH_TYPE_IRIS = 2;
    public static final int AUTH_TYPE_NOT_SUPPORT = 0;
    public static final int AUTH_TYPE_OPTICAL_FINGERPRINT = 17;
    public static final int OPPO_DEFAULT_FINGERPRINT_ICON_DIAMETER = 190;
    public static final int OPPO_DEFAULT_FINGERPRINT_ICON_LOCATION_X = 445;
    public static final int OPPO_DEFAULT_FINGERPRINT_ICON_LOCATION_Y = 1967;
    public static final String OPPO_DEFAULT_MODEL = "OPPO-Default";
    private static final String TAG = "AlipayManager";
    private Context mContext;
    private IAlipayService mService;
    private IBinder mToken = new Binder();

    public AlipayManager(Context context, IAlipayService service) {
        this.mContext = context;
        this.mService = service;
        if (this.mService == null) {
            Slog.v(TAG, "AlipayService was null");
        }
    }

    public byte[] alipayInvokeCommand(byte[] inbuf) {
        IAlipayService iAlipayService = this.mService;
        if (iAlipayService != null) {
            try {
                return iAlipayService.alipayInvokeCommand(inbuf);
            } catch (RemoteException e) {
                Log.v(TAG, "Remote exception in alipayInvokeCommand(): ", e);
                return null;
            }
        } else {
            Log.w(TAG, "alipayInvokeCommand(): Service not connected!");
            return null;
        }
    }

    public byte[] alipayFaceInvokeCommand(byte[] inbuf) {
        IAlipayService iAlipayService = this.mService;
        if (iAlipayService != null) {
            try {
                return iAlipayService.alipayFaceInvokeCommand(inbuf);
            } catch (RemoteException e) {
                Log.v(TAG, "Remote exception in alipayFaceInvokeCommand(): ", e);
                return null;
            }
        } else {
            Log.w(TAG, "alipayFaceInvokeCommand(): Service not connected!");
            return null;
        }
    }

    public void authenticate(String reqId, int flags, IAlipayAuthenticatorCallback callback) {
        IAlipayService iAlipayService = this.mService;
        if (iAlipayService != null) {
            try {
                iAlipayService.authenticate(this.mToken, reqId, flags, callback);
            } catch (RemoteException e) {
                Log.v(TAG, "Remote exception in authenticate(): ", e);
            }
        } else {
            Log.w(TAG, "authenticate(): Service not connected!");
        }
    }

    public void enroll(String reqId, int flags, IAlipayAuthenticatorCallback callback) {
        IAlipayService iAlipayService = this.mService;
        if (iAlipayService != null) {
            try {
                iAlipayService.enroll(this.mToken, reqId, flags, callback);
            } catch (RemoteException e) {
                Log.v(TAG, "Remote exception in enroll(): ", e);
            }
        } else {
            Log.w(TAG, "enroll(): Service not connected!");
        }
    }

    public int cancel(String reqId) {
        IAlipayService iAlipayService = this.mService;
        if (iAlipayService != null) {
            try {
                return iAlipayService.cancel(reqId);
            } catch (RemoteException e) {
                Log.v(TAG, "Remote exception in cancel(): ", e);
                return -1;
            }
        } else {
            Log.w(TAG, "cancel(): Service not connected!");
            return -1;
        }
    }

    public void upgrade(String path) {
        IAlipayService iAlipayService = this.mService;
        if (iAlipayService != null) {
            try {
                iAlipayService.upgrade(path);
            } catch (RemoteException e) {
                Log.v(TAG, "Remote exception in upgrade(): ", e);
            }
        } else {
            Log.w(TAG, "upgrade(): Service not connected!");
        }
    }

    public int getSupportBIOTypes() {
        IAlipayService iAlipayService = this.mService;
        if (iAlipayService != null) {
            try {
                return iAlipayService.getSupportBIOTypes();
            } catch (RemoteException e) {
                Log.v(TAG, "Remote exception in getSupportBIOTypes(): ", e);
                return 0;
            }
        } else {
            Log.w(TAG, "Service not connected!");
            return 0;
        }
    }

    public int getSupportIFAAVersion() {
        IAlipayService iAlipayService = this.mService;
        if (iAlipayService != null) {
            try {
                return iAlipayService.getSupportIFAAVersion();
            } catch (RemoteException e) {
                Log.v(TAG, "Remote exception in getSupportIFAAVersion(): ", e);
                return 0;
            }
        } else {
            Log.w(TAG, "Service not connected!");
            return 0;
        }
    }

    public String getDeviceModel() {
        IAlipayService iAlipayService = this.mService;
        if (iAlipayService != null) {
            try {
                return iAlipayService.getDeviceModel();
            } catch (RemoteException e) {
                Log.v(TAG, "Remote exception in getDeviceModel(): ", e);
                return OPPO_DEFAULT_MODEL;
            }
        } else {
            Log.w(TAG, "Service not connected!");
            return OPPO_DEFAULT_MODEL;
        }
    }

    public int getFingerprintIconDiameter() {
        IAlipayService iAlipayService = this.mService;
        if (iAlipayService != null) {
            try {
                return iAlipayService.getFingerprintIconDiameter();
            } catch (RemoteException e) {
                Log.v(TAG, "Remote exception in getFingerprintIconDiameter(): ", e);
                return 190;
            }
        } else {
            Log.w(TAG, "Service not connected!");
            return 190;
        }
    }

    public int getFingerprintIconExternalCircleXY(String coordinate) {
        int coord;
        if ("X".equals(coordinate)) {
            coord = OPPO_DEFAULT_FINGERPRINT_ICON_LOCATION_X;
        } else {
            coord = OPPO_DEFAULT_FINGERPRINT_ICON_LOCATION_Y;
        }
        IAlipayService iAlipayService = this.mService;
        if (iAlipayService != null) {
            try {
                return iAlipayService.getFingerprintIconExternalCircleXY(coordinate);
            } catch (RemoteException e) {
                Log.v(TAG, "Remote exception in getFingerprintIconExternalCircleXY(): ", e);
                return coord;
            }
        } else {
            Log.w(TAG, "Service not connected!");
            return coord;
        }
    }
}
