package android.os;

import android.util.Log;
import vendor.oppo.hardware.lmvibrator.V1_0.ILinearMotorVibrator;

public class LinearMotorVibratorManager {
    private static final String TAG = "LinearMotorVibratorManager";
    private static ILinearMotorVibrator sLinearMotorVibrateService = null;

    private static ILinearMotorVibrator getLinearMotorVibrateService() {
        if (sLinearMotorVibrateService == null) {
            try {
                sLinearMotorVibrateService = ILinearMotorVibrator.getService();
            } catch (Exception e) {
                Log.e(TAG, "Failed to get linear motor vibrator interface", e);
            }
        }
        return sLinearMotorVibrateService;
    }

    public static void turnOffLinearMotorVibrator() {
        try {
            ILinearMotorVibrator service = getLinearMotorVibrateService();
            if (service != null) {
                service.linearmotorVibratorOff();
            }
        } catch (RemoteException e) {
            Log.e(TAG, "turnOffLinearMotorVibrator failed.", e);
        }
    }

    public static void turnOnLinearmotorVibrator(int waveformId, short amplitude, boolean isRtpMode) {
        try {
            ILinearMotorVibrator service = getLinearMotorVibrateService();
            if (service != null) {
                service.linearmotorVibratorOn(waveformId, amplitude, isRtpMode);
            }
        } catch (RemoteException e) {
            Log.e(TAG, "turnOnLinearmotorVibrator failed.", e);
        }
    }
}
