package android.os;

import android.content.Context;
import android.media.AudioAttributes;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.IVibratorService.Stub;
import android.util.Log;

public class SystemVibrator extends Vibrator {
    private static final String TAG = "Vibrator";
    private boolean mLogEnable = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    private final IVibratorService mService = Stub.asInterface(ServiceManager.getService(Context.VIBRATOR_SERVICE));
    private final Binder mToken = new Binder();

    public SystemVibrator(Context context) {
        super(context);
    }

    public boolean hasVibrator() {
        if (this.mService == null) {
            Log.w(TAG, "Failed to vibrate; no vibrator service.");
            return false;
        }
        try {
            return this.mService.hasVibrator();
        } catch (RemoteException e) {
            return false;
        }
    }

    public void vibrate(int uid, String opPkg, long milliseconds, AudioAttributes attributes) {
        if (this.mService == null) {
            Log.w(TAG, "Failed to vibrate; no vibrator service.");
            return;
        }
        if (this.mLogEnable) {
            Log.i(TAG, "SystemVibrator vibrate is uid= " + uid + ",opPkg =" + opPkg + ",milliseconds=" + milliseconds + ",Binder.getCallingPid()=" + Binder.getCallingPid());
            if (uid > 10000) {
                Log.d(TAG, "vibrate here dumpStack:  Callers=" + Debug.getCallers(4));
            }
        }
        try {
            this.mService.vibrate(uid, opPkg, milliseconds, usageForAttributes(attributes), this.mToken);
        } catch (RemoteException e) {
            Log.w(TAG, "Failed to vibrate.", e);
        }
    }

    public void vibrate(int uid, String opPkg, long[] pattern, int repeat, AudioAttributes attributes) {
        if (this.mService == null) {
            Log.w(TAG, "Failed to vibrate; no vibrator service.");
            return;
        }
        if (this.mLogEnable) {
            String s_pattern = "";
            int i = 0;
            while (pattern != null && i < pattern.length) {
                s_pattern = s_pattern + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + pattern[i];
                i++;
            }
            Log.i(TAG, "SystemVibrator vibratePattern isUid= " + uid + ",opPkg=" + opPkg + ",repeat=" + repeat + ",pattern=" + s_pattern + ",Binder.getCallingPid()=" + Binder.getCallingPid());
            if (uid > 10000) {
                Log.d(TAG, "vibrate here dumpStack:  Callers=" + Debug.getCallers(4));
            }
        }
        if (repeat < pattern.length) {
            try {
                this.mService.vibratePattern(uid, opPkg, pattern, repeat, usageForAttributes(attributes), this.mToken);
            } catch (RemoteException e) {
                Log.w(TAG, "Failed to vibrate.", e);
            }
            return;
        }
        throw new ArrayIndexOutOfBoundsException();
    }

    private static int usageForAttributes(AudioAttributes attributes) {
        return attributes != null ? attributes.getUsage() : 0;
    }

    public void cancel() {
        if (this.mService != null) {
            try {
                this.mService.cancelVibrate(this.mToken);
            } catch (RemoteException e) {
                Log.w(TAG, "Failed to cancel vibration.", e);
            }
        }
    }
}
