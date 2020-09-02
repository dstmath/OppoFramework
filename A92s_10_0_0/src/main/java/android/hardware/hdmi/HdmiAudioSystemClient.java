package android.hardware.hdmi;

import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.util.Log;
import com.android.internal.annotations.VisibleForTesting;

public final class HdmiAudioSystemClient extends HdmiClient {
    private static final int REPORT_AUDIO_STATUS_INTERVAL_MS = 500;
    private static final String TAG = "HdmiAudioSystemClient";
    /* access modifiers changed from: private */
    public boolean mCanSendAudioStatus;
    /* access modifiers changed from: private */
    public final Handler mHandler;
    /* access modifiers changed from: private */
    public boolean mLastIsMute;
    /* access modifiers changed from: private */
    public int mLastMaxVolume;
    /* access modifiers changed from: private */
    public int mLastVolume;
    /* access modifiers changed from: private */
    public boolean mPendingReportAudioStatus;

    public interface SetSystemAudioModeCallback {
        void onComplete(int i);
    }

    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PACKAGE)
    public HdmiAudioSystemClient(IHdmiControlService service) {
        this(service, null);
    }

    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PACKAGE)
    public HdmiAudioSystemClient(IHdmiControlService service, Handler handler) {
        super(service);
        this.mCanSendAudioStatus = true;
        this.mHandler = handler == null ? new Handler(Looper.getMainLooper()) : handler;
    }

    @Override // android.hardware.hdmi.HdmiClient
    public int getDeviceType() {
        return 5;
    }

    public void sendReportAudioStatusCecCommand(boolean isMuteAdjust, int volume, int maxVolume, boolean isMute) {
        if (isMuteAdjust) {
            try {
                this.mService.reportAudioStatus(getDeviceType(), volume, maxVolume, isMute);
            } catch (RemoteException e) {
            }
        } else {
            this.mLastVolume = volume;
            this.mLastMaxVolume = maxVolume;
            this.mLastIsMute = isMute;
            if (this.mCanSendAudioStatus) {
                try {
                    this.mService.reportAudioStatus(getDeviceType(), volume, maxVolume, isMute);
                    this.mCanSendAudioStatus = false;
                    this.mHandler.postDelayed(new Runnable() {
                        /* class android.hardware.hdmi.HdmiAudioSystemClient.AnonymousClass1 */

                        public void run() {
                            if (HdmiAudioSystemClient.this.mPendingReportAudioStatus) {
                                try {
                                    HdmiAudioSystemClient.this.mService.reportAudioStatus(HdmiAudioSystemClient.this.getDeviceType(), HdmiAudioSystemClient.this.mLastVolume, HdmiAudioSystemClient.this.mLastMaxVolume, HdmiAudioSystemClient.this.mLastIsMute);
                                    HdmiAudioSystemClient.this.mHandler.postDelayed(this, 500);
                                } catch (RemoteException e) {
                                    boolean unused = HdmiAudioSystemClient.this.mCanSendAudioStatus = true;
                                } catch (Throwable th) {
                                    boolean unused2 = HdmiAudioSystemClient.this.mPendingReportAudioStatus = false;
                                    throw th;
                                }
                                boolean unused3 = HdmiAudioSystemClient.this.mPendingReportAudioStatus = false;
                                return;
                            }
                            boolean unused4 = HdmiAudioSystemClient.this.mCanSendAudioStatus = true;
                        }
                    }, 500);
                } catch (RemoteException e2) {
                }
            } else {
                this.mPendingReportAudioStatus = true;
            }
        }
    }

    public void setSystemAudioMode(boolean state, SetSystemAudioModeCallback callback) {
    }

    public void setSystemAudioModeOnForAudioOnlySource() {
        try {
            this.mService.setSystemAudioModeOnForAudioOnlySource();
        } catch (RemoteException e) {
            Log.d(TAG, "Failed to set System Audio Mode on for Audio Only source");
        }
    }
}
