package com.android.server.coloros;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioAttributes;
import android.os.Binder;
import android.os.IBinder;
import android.os.IVibratorService;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.Vibrator;
import android.util.Slog;
import com.android.server.pm.PackageManagerService;
import com.android.server.slice.SliceClientPermissions;
import com.oppo.os.ILinearmotorVibratorService;
import com.oppo.os.WaveformEffect;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class LinearmotorVibratorService extends ILinearmotorVibratorService.Stub {
    private static final boolean DEBUG = false;
    private static final String REASON = "LinearmotorVibrator";
    private static final String TAG = "LinearmotorVibratorService";
    public static final int USAGE_ALARM = 4;
    public static final int USAGE_GAME = 14;
    public static final int USAGE_NOTIFICATION = 5;
    public static final int USAGE_NOTIFICATION_RINGTONE = 6;
    public static final int USAGE_UNKNOWN = 0;
    private static LinearmotorVibratorService sInstance = null;
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        /* class com.android.server.coloros.LinearmotorVibratorService.AnonymousClass1 */

        public void onReceive(Context context, Intent intent) {
            if ("android.media.RINGER_MODE_CHANGED".equals(intent.getAction())) {
                LinearmotorVibratorService.this.configureForRingerMode(intent.getIntExtra("android.media.EXTRA_RINGER_MODE", 2));
            }
        }
    };
    private final Context mContext;
    private boolean mHasLinearMotor;
    private Vibrator mVibrator;
    private IVibratorService mVibratorService;

    public LinearmotorVibratorService(Context context) {
        this.mContext = context;
    }

    public void systemReady() {
        this.mVibrator = (Vibrator) this.mContext.getSystemService("vibrator");
        this.mHasLinearMotor = this.mContext.getPackageManager().hasSystemFeature("oppo.feature.vibrator.waveform.support");
        this.mVibratorService = IVibratorService.Stub.asInterface(ServiceManager.getService("vibrator"));
        registerBroadCastReceiver();
    }

    private void registerBroadCastReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.media.RINGER_MODE_CHANGED");
        this.mContext.registerReceiver(this.mBroadcastReceiver, filter, null, null);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void configureForRingerMode(int ringerMode) {
        NoSuchMethodException e;
        IllegalAccessException e2;
        InvocationTargetException e3;
        ClassNotFoundException e4;
        try {
            Method method = Class.forName("android.os.Vibrator").getDeclaredMethod("linearMotorVibrate", Integer.TYPE, String.class, int[].class, long[].class, Integer.TYPE, Integer.TYPE, String.class, AudioAttributes.class, IBinder.class);
            AudioAttributes attributes = new AudioAttributes.Builder().setUsage(4).build();
            int[] silentValue = {1025};
            int[] normalValue = {1026};
            long[] timing = {1};
            Binder token = new Binder();
            if (ringerMode == 1) {
                try {
                    method.invoke(this.mVibrator, 1000, PackageManagerService.PLATFORM_PACKAGE_NAME, normalValue, timing, (byte) 2, -1, REASON, attributes, token);
                } catch (NoSuchMethodException e5) {
                    e = e5;
                    Slog.e(TAG, "NoSuchMethodException " + e.toString());
                } catch (IllegalAccessException e6) {
                    e2 = e6;
                    Slog.e(TAG, "IllegalAccessException " + e2.toString());
                } catch (InvocationTargetException e7) {
                    e3 = e7;
                    Slog.e(TAG, "InvocationTargetException " + e3.getTargetException().toString());
                } catch (ClassNotFoundException e8) {
                    e4 = e8;
                    Slog.e(TAG, "ClassNotFoundException " + e4.toString());
                }
            } else {
                method.invoke(this.mVibrator, 1000, PackageManagerService.PLATFORM_PACKAGE_NAME, silentValue, timing, (byte) 2, -1, REASON, attributes, token);
            }
        } catch (NoSuchMethodException e9) {
            e = e9;
            Slog.e(TAG, "NoSuchMethodException " + e.toString());
        } catch (IllegalAccessException e10) {
            e2 = e10;
            Slog.e(TAG, "IllegalAccessException " + e2.toString());
        } catch (InvocationTargetException e11) {
            e3 = e11;
            Slog.e(TAG, "InvocationTargetException " + e3.getTargetException().toString());
        } catch (ClassNotFoundException e12) {
            e4 = e12;
            Slog.e(TAG, "ClassNotFoundException " + e4.toString());
        }
    }

    private boolean hasLinearMotor() {
        return this.mHasLinearMotor;
    }

    public void vibrate(int uid, String opPkg, WaveformEffect we, IBinder token) {
        if (this.mContext.checkCallingOrSelfPermission("android.permission.VIBRATE") != 0) {
            throw new SecurityException("Requires VIBRATE permission");
        } else if (token == null) {
            Slog.e(TAG, "token must not be null");
        } else if (this.mHasLinearMotor) {
            linearMotorVibrate(uid, opPkg, this.mVibrator, we, token);
        }
    }

    public void cancelVibrate(WaveformEffect we, IBinder token) {
        if (token == null) {
            Slog.e(TAG, "token must not be null");
            return;
        }
        IVibratorService iVibratorService = this.mVibratorService;
        if (iVibratorService != null) {
            try {
                iVibratorService.cancelVibrate(token);
            } catch (RemoteException e) {
                Slog.e(TAG, "RemoteException " + e.toString());
            }
        }
    }

    private static void linearMotorVibrate(int uid, String opPkg, Vibrator vibrator, WaveformEffect we, IBinder token) {
        AudioAttributes attributes;
        AudioAttributes attributes2;
        AudioAttributes attributes3;
        AudioAttributes attributes4;
        try {
            Method method = Class.forName("android.os.Vibrator").getDeclaredMethod("linearMotorVibrate", Integer.TYPE, String.class, int[].class, long[].class, Integer.TYPE, Integer.TYPE, String.class, AudioAttributes.class, IBinder.class);
            int waveFormNodeType = we.getWaveFormNodeType();
            if (waveFormNodeType == 2) {
                if (we.getUsageHint() == 0) {
                    attributes4 = new AudioAttributes.Builder().setUsage(4).build();
                } else {
                    attributes4 = new AudioAttributes.Builder().setUsage(we.getUsageHint()).build();
                }
                Object[] objArr = new Object[9];
                objArr[0] = Integer.valueOf(uid);
                objArr[1] = opPkg;
                objArr[2] = we.getWaveFormIndexArray();
                objArr[3] = we.getWaveFormDurationArray();
                objArr[4] = Integer.valueOf(we.getEffectStrength());
                objArr[5] = Integer.valueOf(we.getEffectLoop() ? 0 : -1);
                objArr[6] = REASON;
                objArr[7] = attributes4;
                objArr[8] = token;
                method.invoke(vibrator, objArr);
            } else if (waveFormNodeType == 1) {
                if (we.getUsageHint() == 0) {
                    attributes3 = new AudioAttributes.Builder().setUsage(0).build();
                } else {
                    attributes3 = new AudioAttributes.Builder().setUsage(we.getUsageHint()).build();
                }
                Object[] objArr2 = new Object[9];
                objArr2[0] = Integer.valueOf(uid);
                objArr2[1] = opPkg;
                objArr2[2] = we.getWaveFormIndexArray();
                objArr2[3] = we.getWaveFormDurationArray();
                objArr2[4] = Integer.valueOf(we.getEffectStrength());
                objArr2[5] = Integer.valueOf(we.getEffectLoop() ? 0 : -1);
                objArr2[6] = REASON;
                objArr2[7] = attributes3;
                objArr2[8] = token;
                method.invoke(vibrator, objArr2);
            } else if (we.getIsRingtoneCustomized() || we.getRingtoneFilePath().isEmpty()) {
                if (we.getUsageHint() == 4) {
                    attributes = new AudioAttributes.Builder().setUsage(4).build();
                } else if (we.getUsageHint() == 14) {
                    attributes = new AudioAttributes.Builder().setUsage(14).build();
                } else {
                    attributes = new AudioAttributes.Builder().setUsage(6).build();
                }
                int ringtoneWaveFormEffect = we.getRingtoneVibrateType();
                int i = -1;
                if (ringtoneWaveFormEffect != -1 && ringtoneWaveFormEffect != 67) {
                    Object[] objArr3 = new Object[9];
                    objArr3[0] = Integer.valueOf(uid);
                    objArr3[1] = opPkg;
                    objArr3[2] = we.getWaveFormIndexArray(ringtoneWaveFormEffect);
                    objArr3[3] = we.getWaveFormDurationArray(ringtoneWaveFormEffect);
                    objArr3[4] = Integer.valueOf(we.getEffectStrength());
                    if (we.getEffectLoop()) {
                        i = 0;
                    }
                    objArr3[5] = Integer.valueOf(i);
                    objArr3[6] = REASON;
                    objArr3[7] = attributes;
                    objArr3[8] = token;
                    method.invoke(vibrator, objArr3);
                }
            } else if (we.getRingtoneVibrateType() != -1 && we.getRingtoneVibrateType() != 67) {
                if (we.getRingtoneVibrateType() == 64) {
                    String ringtoneFilePath = we.getRingtoneFilePath();
                    String ringtoneFileName = getNameWithoutExt(ringtoneFilePath);
                    String ringtoneTitle = WaveformEffect.getRingtoneTitle(ringtoneFileName);
                    int ringtoneWaveFormEffect2 = WaveformEffect.getRingtoneWaveFormEffect(ringtoneTitle);
                    Slog.d(TAG, "linearMotorVibrate ringtoneFilePath=" + ringtoneFilePath + " ringtoneFileName=" + ringtoneFileName + " ringtoneTitle=" + ringtoneTitle + " ringtoneWaveFormEffect=" + ringtoneWaveFormEffect2 + " loop=" + we.getEffectLoop());
                    if (we.getUsageHint() == 4) {
                        attributes2 = new AudioAttributes.Builder().setUsage(4).build();
                    } else {
                        attributes2 = new AudioAttributes.Builder().setUsage(6).build();
                    }
                    Object[] objArr4 = new Object[9];
                    objArr4[0] = Integer.valueOf(uid);
                    objArr4[1] = opPkg;
                    objArr4[2] = we.getWaveFormIndexArray(ringtoneWaveFormEffect2);
                    objArr4[3] = we.getWaveFormDurationArray(ringtoneWaveFormEffect2);
                    objArr4[4] = Integer.valueOf(we.getEffectStrength());
                    objArr4[5] = Integer.valueOf(we.getEffectLoop() ? 0 : -1);
                    objArr4[6] = REASON;
                    objArr4[7] = attributes2;
                    objArr4[8] = token;
                    method.invoke(vibrator, objArr4);
                    return;
                }
                int ringtoneWaveFormEffect3 = we.getRingtoneVibrateType();
                AudioAttributes attributes5 = new AudioAttributes.Builder().setUsage(4).build();
                Object[] objArr5 = new Object[9];
                objArr5[0] = Integer.valueOf(uid);
                objArr5[1] = opPkg;
                objArr5[2] = we.getWaveFormIndexArray(ringtoneWaveFormEffect3);
                objArr5[3] = we.getWaveFormDurationArray(ringtoneWaveFormEffect3);
                objArr5[4] = Integer.valueOf(we.getEffectStrength());
                objArr5[5] = Integer.valueOf(we.getEffectLoop() ? 0 : -1);
                objArr5[6] = REASON;
                objArr5[7] = attributes5;
                objArr5[8] = token;
                method.invoke(vibrator, objArr5);
            }
        } catch (NoSuchMethodException e) {
            Slog.e(TAG, "NoSuchMethodException " + e.toString(), e);
        } catch (IllegalAccessException e2) {
            Slog.e(TAG, "IllegalAccessException " + e2.toString(), e2);
        } catch (InvocationTargetException e3) {
            Slog.e(TAG, "InvocationTargetException " + e3.getTargetException().toString(), e3);
        } catch (ClassNotFoundException e4) {
            Slog.e(TAG, "ClassNotFoundException " + e4.toString(), e4);
        }
    }

    private static int prefixLength(String filePath) {
        if (filePath.length() != 0 && filePath.charAt(0) == '/') {
            return 1;
        }
        return 0;
    }

    private static String getNameWithoutExt(String filePath) {
        try {
            int lastSlashIndex = filePath.lastIndexOf(SliceClientPermissions.SliceAuthority.DELIMITER);
            int prefixLength = prefixLength(filePath);
            String fileName = lastSlashIndex < prefixLength ? filePath.substring(prefixLength) : filePath.substring(lastSlashIndex + 1);
            int dotIndex = fileName.lastIndexOf(".");
            return dotIndex == -1 ? fileName : fileName.substring(0, dotIndex);
        } catch (Exception e) {
            Slog.e("TAG", "getNameWithoutExt e:" + e.toString());
            return "";
        }
    }
}
