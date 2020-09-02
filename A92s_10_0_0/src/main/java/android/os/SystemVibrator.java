package android.os;

import android.annotation.UnsupportedAppUsage;
import android.content.Context;
import android.media.AudioAttributes;
import android.os.IVibratorService;
import android.util.Log;

public class SystemVibrator extends Vibrator {
    private static final String TAG = "Vibrator";
    private OppoSystemVibrator mOppoSystemVibrator;
    private final IVibratorService mService = IVibratorService.Stub.asInterface(ServiceManager.getService(Context.VIBRATOR_SERVICE));
    private final Binder mToken = new Binder();

    @UnsupportedAppUsage
    public SystemVibrator() {
    }

    @UnsupportedAppUsage
    public SystemVibrator(Context context) {
        super(context);
        this.mOppoSystemVibrator = new OppoSystemVibrator(context);
    }

    @Override // android.os.Vibrator
    public boolean hasVibrator() {
        IVibratorService iVibratorService = this.mService;
        if (iVibratorService == null) {
            Log.w(TAG, "Failed to vibrate; no vibrator service.");
            return false;
        }
        try {
            return iVibratorService.hasVibrator();
        } catch (RemoteException e) {
            return false;
        }
    }

    @Override // android.os.Vibrator
    public boolean hasAmplitudeControl() {
        IVibratorService iVibratorService = this.mService;
        if (iVibratorService == null) {
            Log.w(TAG, "Failed to check amplitude control; no vibrator service.");
            return false;
        }
        try {
            return iVibratorService.hasAmplitudeControl();
        } catch (RemoteException e) {
            return false;
        }
    }

    @Override // android.os.Vibrator
    public void vibrate(int uid, String opPkg, VibrationEffect effect, String reason, AudioAttributes attributes) {
        vibrate(uid, opPkg, effect, reason, attributes, this.mToken);
    }

    private void vibrate(int uid, String opPkg, VibrationEffect effect, String reason, AudioAttributes attributes, IBinder token) {
        if (this.mService == null) {
            Log.w(TAG, "Failed to vibrate; no vibrator service.");
        } else if (!this.mOppoSystemVibrator.doVibrate(uid, opPkg, effect)) {
            try {
                this.mService.vibrate(uid, opPkg, effect, usageForAttributes(attributes), reason, this.mToken);
            } catch (RemoteException e) {
                Log.w(TAG, "Failed to vibrate.", e);
            }
        }
    }

    private static int usageForAttributes(AudioAttributes attributes) {
        if (attributes != null) {
            return attributes.getUsage();
        }
        return 0;
    }

    @Override // android.os.Vibrator
    public void cancel() {
        IVibratorService iVibratorService = this.mService;
        if (iVibratorService != null) {
            try {
                iVibratorService.cancelVibrate(this.mToken);
            } catch (RemoteException e) {
                Log.w(TAG, "Failed to cancel vibration.", e);
            }
        }
    }

    /* JADX DEBUG: Additional 2 move instruction added to help type inference */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v3, resolved type: android.os.OppoNativeWaveformVibrationEffect} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v7, resolved type: android.os.OppoNativeOneShotVibrationEffect} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v8, resolved type: android.os.OppoNativeWaveformVibrationEffect} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v9, resolved type: android.os.OppoNativeWaveformVibrationEffect} */
    /* JADX WARN: Multi-variable type inference failed */
    @Override // android.os.Vibrator
    public void linearMotorVibrate(int uid, String opPkg, int[] waveformIds, long[] timings, int strength, int repeat, String reason, AudioAttributes attributes, IBinder token) {
        VibrationEffect effect;
        try {
            if (waveformIds.length == 1 && timings.length == 1 && repeat == -1) {
                OppoNativeOneShotVibrationEffect oneShot = new OppoNativeOneShotVibrationEffect(waveformIds[0], timings[0]);
                oneShot.setEffectStrength(strength);
                effect = oneShot;
            } else {
                OppoNativeWaveformVibrationEffect waveform = new OppoNativeWaveformVibrationEffect(timings, waveformIds, repeat);
                waveform.setEffectStrength(strength);
                effect = waveform;
            }
            vibrate(uid, opPkg, effect, reason, attributes, token);
        } catch (IllegalArgumentException iae) {
            Log.e(TAG, "Failed to linearMotorVibrate", iae);
        } catch (Exception e) {
            Log.e(TAG, "linearMotorVibrate failed", e);
        }
    }
}
