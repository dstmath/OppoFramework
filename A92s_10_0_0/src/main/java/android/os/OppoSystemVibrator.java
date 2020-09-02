package android.os;

import android.app.KeyguardManager;
import android.app.OppoActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.os.VibrationEffect;
import android.util.Log;
import com.android.internal.telephony.PhoneConstants;
import com.oppo.atlas.OppoAtlasManagerDefine;

public class OppoSystemVibrator {
    private static final String TAG = "Vibrator";
    private boolean mCameraAntiShake = false;
    private Context mContext = null;
    private KeyguardManager mKeyguardManager;
    private boolean mLogEnable = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    private OppoActivityManager mOppoActivityManager = null;

    public OppoSystemVibrator(Context context) {
        this.mCameraAntiShake = context.getPackageManager().hasSystemFeature("oppo.camera.antishake.support");
        this.mContext = context;
        this.mOppoActivityManager = new OppoActivityManager();
    }

    private boolean isVirtualKeyVibrate(int uid, VibrationEffect.Waveform effect) {
        long[] mTimings = effect.getTimings();
        if (UserHandle.getAppId(uid) != 1000 || mTimings.length != 2 || mTimings[0] != 0 || mTimings[1] != 35) {
            return false;
        }
        if (this.mLogEnable) {
            Log.d(TAG, "vibrate from user " + uid + ", effect = " + effect);
        }
        return true;
    }

    public boolean doVibrate(int uid, String opPkg, VibrationEffect effect) {
        boolean shouldVib = false;
        if (effect instanceof VibrationEffect.Waveform) {
            VibrationEffect.Waveform waveform = (VibrationEffect.Waveform) effect;
            if (waveform.getRepeatIndex() >= 0 || isVirtualKeyVibrate(uid, waveform)) {
                shouldVib = true;
            }
        }
        if ((effect instanceof VibrationEffect.Prebaked) && ((VibrationEffect.Prebaked) effect).getId() == 0) {
            shouldVib = true;
        }
        String cameraPkgName = SystemProperties.get("oppo.camera.packname", PhoneConstants.APN_TYPE_DEFAULT);
        if (this.mCameraAntiShake && !shouldVib && cameraPkgName.equals(OppoAtlasManagerDefine.CAMERA_PACKAGE_NAME)) {
            ComponentName componentName = null;
            try {
                componentName = this.mOppoActivityManager.getTopActivityComponentName();
            } catch (RemoteException e) {
                Log.e(TAG, "get top activity failed.", e);
            }
            String topAppPkgName = componentName != null ? componentName.toString() : null;
            boolean isCameraAppFocus = topAppPkgName != null && topAppPkgName.contains(cameraPkgName);
            if (this.mKeyguardManager == null) {
                this.mKeyguardManager = (KeyguardManager) this.mContext.getSystemService(Context.KEYGUARD_SERVICE);
            }
            KeyguardManager keyguardManager = this.mKeyguardManager;
            boolean isKeyguradFocus = keyguardManager != null && keyguardManager.inKeyguardRestrictedInputMode();
            if (isCameraAppFocus && !isKeyguradFocus) {
                Log.d(TAG, "vibrate return because isCameraAppFocus, cameraPkgName = " + cameraPkgName + "; isKeyguradFocus = " + isKeyguradFocus);
                return true;
            }
        }
        if (this.mLogEnable) {
            long duration = effect != null ? effect.getDuration() : 0;
            Log.i(TAG, "SystemVibrator vibrate is uid= " + uid + ",opPkg =" + opPkg + ",duration=" + duration + ",effect=" + effect + ",Binder.getCallingPid()=" + Binder.getCallingPid());
            if (uid > 10000) {
                Log.d(TAG, "vibrate here dumpStack:  Callers=" + Debug.getCallers(4));
            }
        }
        return false;
    }
}
