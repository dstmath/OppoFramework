package com.qualcomm.qti.telephonyservice;

import android.content.Context;
import android.hidl.manager.V1_0.IServiceManager;
import android.hidl.manager.V1_0.IServiceNotification;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioPortUpdateListener;
import android.media.AudioPatch;
import android.media.AudioPort;
import android.media.AudioSystem;
import android.os.IHwBinder.DeathRecipient;
import android.os.RemoteException;
import android.util.Log;
import java.util.concurrent.atomic.AtomicLong;
import vendor.qti.hardware.radio.am.V1_0.AudioError;
import vendor.qti.hardware.radio.am.V1_0.IQcRilAudio;
import vendor.qti.hardware.radio.am.V1_0.IQcRilAudioCallback;
import vendor.qti.hardware.radio.am.V1_0.IQcRilAudioCallback.Stub;

public class QcRilAudio {
    static final String TAG = "QcRilAudio";
    private AudioManager mAudioManager = null;
    private Context mContext = null;
    private final String mInstanceName;
    private boolean mIsDisposed = false;
    private AudioPortUpdateListener mListener = null;
    private final IQcRilAudioCallback mQcRilAudioCallback = new QcRilAudioCallback(this, null);
    private IQcRilAudio mRilAudio;
    private final AtomicLong mRilAudioCookie = new AtomicLong(0);
    private final AudioProxyDeathRecipient mRilAudioDeathRecipient = new AudioProxyDeathRecipient(this, null);
    private final ServiceNotification mServiceNotification = new ServiceNotification(this, null);

    private class AudioPortUpdateListener implements OnAudioPortUpdateListener {
        /* synthetic */ AudioPortUpdateListener(QcRilAudio this$0, AudioPortUpdateListener -this1) {
            this();
        }

        private AudioPortUpdateListener() {
        }

        public void onAudioPortListUpdate(AudioPort[] portList) {
        }

        public void onAudioPatchListUpdate(AudioPatch[] patchList) {
        }

        public void onServiceDied() {
            if (QcRilAudio.this.isDisposed()) {
                Log.d(QcRilAudio.TAG, "AudioPortUpdateListener onServiceDied: Ignoring.");
                QcRilAudio.this.dump("AudioPortUpdateListener onServiceDied");
                return;
            }
            int error = AudioSystem.checkAudioFlinger();
            Log.d(QcRilAudio.TAG, "AudioSystem error: " + error);
            switch (error) {
                case 0:
                case AudioError.AUDIO_STATUS_SERVER_DIED /*100*/:
                    QcRilAudio.this.setError(error);
                    break;
                default:
                    Log.d(QcRilAudio.TAG, "AudioSystem error not supported");
                    break;
            }
        }
    }

    private class AudioProxyDeathRecipient implements DeathRecipient {
        /* synthetic */ AudioProxyDeathRecipient(QcRilAudio this$0, AudioProxyDeathRecipient -this1) {
            this();
        }

        private AudioProxyDeathRecipient() {
        }

        public void serviceDied(long cookie) {
            if (QcRilAudio.this.isDisposed()) {
                Log.d(QcRilAudio.TAG, "serviceDied: Ignoring.");
                QcRilAudio.this.dump("serviceDied");
                return;
            }
            long current = QcRilAudio.this.mRilAudioCookie.get();
            if (cookie != current) {
                Log.v(QcRilAudio.TAG, "serviceDied: Ignoring. provided=" + cookie + " expected=" + current);
                return;
            }
            Log.e(QcRilAudio.TAG, "IQcRilAudio service died");
            QcRilAudio.this.resetService();
        }
    }

    private class QcRilAudioCallback extends Stub {
        /* synthetic */ QcRilAudioCallback(QcRilAudio this$0, QcRilAudioCallback -this1) {
            this();
        }

        private QcRilAudioCallback() {
        }

        public int setParameters(String keyValuePairs) {
            return AudioSystem.setParameters(keyValuePairs);
        }

        public String getParameters(String key) {
            return AudioSystem.getParameters(key);
        }
    }

    private class ServiceNotification extends IServiceNotification.Stub {
        /* synthetic */ ServiceNotification(QcRilAudio this$0, ServiceNotification -this1) {
            this();
        }

        private ServiceNotification() {
        }

        public void onRegistration(String fqName, String name, boolean preexisting) {
            Log.d(QcRilAudio.TAG, "onRegistration: fqName=" + fqName + " name=" + name);
            if (!QcRilAudio.this.mInstanceName.equals(name) || QcRilAudio.this.isDisposed()) {
                Log.d(QcRilAudio.TAG, "onRegistration: Ignoring.");
                QcRilAudio.this.dump("onRegistration");
                return;
            }
            QcRilAudio.this.initHal();
            QcRilAudio.this.setError(AudioSystem.checkAudioFlinger());
        }
    }

    public QcRilAudio(int slotId, Context context) {
        this.mContext = context;
        this.mInstanceName = "slot" + slotId;
        try {
            boolean ret = IServiceManager.getService().registerForNotifications(IQcRilAudio.kInterfaceName, this.mInstanceName, this.mServiceNotification);
            if (!ret) {
                Log.e(TAG, "Unable to register service start notification: ret = " + ret);
            }
        } catch (RemoteException e) {
            Log.e(TAG, "Unable to register service start notification");
        }
        registerAudioPortListener();
    }

    public synchronized void dispose() {
        if (!isDisposed()) {
            this.mIsDisposed = true;
            try {
                if (this.mRilAudio != null) {
                    this.mRilAudio.unlinkToDeath(this.mRilAudioDeathRecipient);
                    this.mRilAudio = null;
                }
            } catch (RemoteException e) {
                Log.d(TAG, "dispose: Exception=" + e);
            }
            unregisterAudioPortListener();
            return;
        }
        return;
    }

    public boolean isDisposed() {
        return this.mIsDisposed;
    }

    private synchronized void initHal() {
        Log.d(TAG, "initHal");
        try {
            this.mRilAudio = IQcRilAudio.getService(this.mInstanceName);
            if (this.mRilAudio == null) {
                Log.e(TAG, "initHal: mRilAudio == null");
                return;
            } else {
                this.mRilAudio.linkToDeath(this.mRilAudioDeathRecipient, this.mRilAudioCookie.incrementAndGet());
                this.mRilAudio.setCallback(this.mQcRilAudioCallback);
            }
        } catch (Exception e) {
            Log.e(TAG, "initHal: Exception: " + e);
        }
    }

    private synchronized void setError(int errorcode) {
        if (this.mRilAudio == null) {
            Log.w(TAG, "setError - mRilAudio is null, returning.");
            return;
        }
        try {
            this.mRilAudio.setError(errorcode);
        } catch (Exception e) {
            Log.e(TAG, "setError request to IQcRilAudio. Exception: " + e);
        }
    }

    private synchronized void registerAudioPortListener() {
        this.mAudioManager = (AudioManager) this.mContext.getSystemService("audio");
        if (this.mAudioManager != null) {
            this.mListener = new AudioPortUpdateListener(this, null);
            this.mAudioManager.registerAudioPortUpdateListener(this.mListener);
        }
    }

    private synchronized void unregisterAudioPortListener() {
        if (!(this.mListener == null || this.mAudioManager == null)) {
            this.mAudioManager.unregisterAudioPortUpdateListener(this.mListener);
            this.mListener = null;
            this.mAudioManager = null;
        }
    }

    private synchronized void resetService() {
        this.mRilAudio = null;
    }

    private void dump(String fn) {
        Log.d(TAG, fn + ": InstanceName=" + this.mInstanceName);
        Log.d(TAG, fn + ": isDisposed=" + isDisposed());
    }
}
