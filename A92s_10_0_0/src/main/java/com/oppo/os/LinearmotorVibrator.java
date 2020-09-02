package com.oppo.os;

import android.content.Context;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.os.RemoteException;
import android.util.Slog;

public class LinearmotorVibrator {
    public static final String FEATURE_WAVEFORM_VIBRATOR = "oppo.feature.vibrator.waveform.support";
    public static final String LINEARMOTORVIBRATOR_SERVICE = "linearmotor";
    private static final int MSG_LINEARMOTOR_VIBRATOR_BEGIN = 10000;
    private static final int MSG_LINEARMOTOR_VIBRATOR_VIBRATE = 10001;
    public static final String TAG = "LinearmotorVibrator";
    private Context mContext;
    private VibratorHandler mHandler = null;
    private HandlerThread mHandlerThread = null;
    /* access modifiers changed from: private */
    public final Object mLock = new Object();
    /* access modifiers changed from: private */
    public final String mPackageName;
    /* access modifiers changed from: private */
    public final ILinearmotorVibratorService mService;
    /* access modifiers changed from: private */
    public final Binder mToken = new Binder();
    /* access modifiers changed from: private */
    public boolean mVibrating;

    private final class VibratorHandler extends Handler {
        public VibratorHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            if (msg.arg1 == 10001) {
                synchronized (LinearmotorVibrator.this.mLock) {
                    if (!LinearmotorVibrator.this.mVibrating) {
                        boolean unused = LinearmotorVibrator.this.mVibrating = true;
                        try {
                            LinearmotorVibrator.this.mService.vibrate(Process.myUid(), LinearmotorVibrator.this.mPackageName, (WaveformEffect) msg.obj, LinearmotorVibrator.this.mToken);
                        } catch (RemoteException e) {
                            Slog.w(LinearmotorVibrator.TAG, "Remote exception in LinearmotorVibrator: ", e);
                        }
                        boolean unused2 = LinearmotorVibrator.this.mVibrating = false;
                    }
                }
            }
        }
    }

    public LinearmotorVibrator(Context context, ILinearmotorVibratorService service) {
        this.mContext = context;
        this.mService = service;
        this.mPackageName = context.getOpPackageName();
        if (this.mService == null) {
            Slog.v(TAG, "ILinearmotorVibratorService was null");
        }
        this.mVibrating = false;
    }

    public void vibrate(WaveformEffect we) {
        Slog.d(TAG, "vibrate WaveformEffect:" + we.toString());
        if (this.mService == null) {
            return;
        }
        if (we.getAsynchronous()) {
            if (this.mHandlerThread == null) {
                this.mHandlerThread = new HandlerThread("LinearmotorVibrator-Thread");
                this.mHandlerThread.start();
            }
            if (this.mHandler == null) {
                this.mHandler = new VibratorHandler(this.mHandlerThread.getLooper());
            }
            if (!this.mVibrating) {
                Message msg = this.mHandler.obtainMessage();
                msg.arg1 = 10001;
                msg.obj = we;
                this.mHandler.sendMessage(msg);
                Slog.d(TAG, "vibrate WaveformEffect async.");
                return;
            }
            return;
        }
        try {
            Slog.d(TAG, "call service vibrate");
            this.mService.vibrate(Process.myUid(), this.mPackageName, we, this.mToken);
        } catch (RemoteException e) {
            Slog.w(TAG, "Remote exception in LinearmotorVibrator: ", e);
        }
    }

    public void cancelVibrate(WaveformEffect we) {
        if (this.mService != null) {
            try {
                Slog.d(TAG, "call linearmotor vibrator service cancelVibrate");
                this.mService.cancelVibrate(we, this.mToken);
                if (this.mHandlerThread != null) {
                    this.mHandlerThread.quitSafely();
                    this.mHandlerThread = null;
                }
                if (this.mHandler != null) {
                    this.mHandler = null;
                }
            } catch (RemoteException e) {
                Slog.w(TAG, "Remote exception in LinearmotorVibrator: ", e);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        try {
            if (this.mHandlerThread != null) {
                this.mHandlerThread.quitSafely();
                this.mHandlerThread = null;
            }
            if (this.mHandler != null) {
                this.mHandler = null;
            }
        } finally {
            super.finalize();
        }
    }
}
