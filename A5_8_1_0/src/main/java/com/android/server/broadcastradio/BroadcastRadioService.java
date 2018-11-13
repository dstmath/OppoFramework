package com.android.server.broadcastradio;

import android.content.Context;
import android.hardware.radio.IRadioService.Stub;
import android.hardware.radio.ITuner;
import android.hardware.radio.ITunerCallback;
import android.hardware.radio.RadioManager.BandConfig;
import android.hardware.radio.RadioManager.ModuleProperties;
import android.os.ParcelableException;
import com.android.server.SystemService;
import java.util.List;

public class BroadcastRadioService extends SystemService {
    private final Object mLock = new Object();
    private List<ModuleProperties> mModules = null;
    private final long mNativeContext = nativeInit();
    private final ServiceImpl mServiceImpl = new ServiceImpl(this, null);

    private class ServiceImpl extends Stub {
        /* synthetic */ ServiceImpl(BroadcastRadioService this$0, ServiceImpl -this1) {
            this();
        }

        private ServiceImpl() {
        }

        private void enforcePolicyAccess() {
            if (BroadcastRadioService.this.getContext().checkCallingPermission("android.permission.ACCESS_BROADCAST_RADIO") != 0) {
                throw new SecurityException("ACCESS_BROADCAST_RADIO permission not granted");
            }
        }

        public List<ModuleProperties> listModules() {
            enforcePolicyAccess();
            synchronized (BroadcastRadioService.this.mLock) {
                List<ModuleProperties> -get1;
                if (BroadcastRadioService.this.mModules != null) {
                    -get1 = BroadcastRadioService.this.mModules;
                    return -get1;
                }
                BroadcastRadioService.this.mModules = BroadcastRadioService.this.nativeLoadModules(BroadcastRadioService.this.mNativeContext);
                if (BroadcastRadioService.this.mModules == null) {
                    throw new ParcelableException(new NullPointerException("couldn't load radio modules"));
                }
                -get1 = BroadcastRadioService.this.mModules;
                return -get1;
            }
        }

        public ITuner openTuner(int moduleId, BandConfig bandConfig, boolean withAudio, ITunerCallback callback) {
            enforcePolicyAccess();
            if (callback == null) {
                throw new IllegalArgumentException("Callback must not be empty");
            }
            Tuner -wrap0;
            synchronized (BroadcastRadioService.this.mLock) {
                -wrap0 = BroadcastRadioService.this.nativeOpenTuner(BroadcastRadioService.this.mNativeContext, moduleId, bandConfig, withAudio, callback);
            }
            return -wrap0;
        }
    }

    private native void nativeFinalize(long j);

    private native long nativeInit();

    private native List<ModuleProperties> nativeLoadModules(long j);

    private native Tuner nativeOpenTuner(long j, int i, BandConfig bandConfig, boolean z, ITunerCallback iTunerCallback);

    public BroadcastRadioService(Context context) {
        super(context);
    }

    protected void finalize() throws Throwable {
        nativeFinalize(this.mNativeContext);
        super.finalize();
    }

    public void onStart() {
        publishBinderService("broadcastradio", this.mServiceImpl);
    }
}
