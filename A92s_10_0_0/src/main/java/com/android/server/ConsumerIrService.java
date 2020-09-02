package com.android.server;

import android.content.Context;
import android.hardware.IConsumerIrService;
import android.os.PowerManager;
import android.util.Slog;

public class ConsumerIrService extends IConsumerIrService.Stub {
    private static final int MAX_XMIT_TIME = 2000000;
    private static final String TAG = "ConsumerIrService";
    private final Context mContext;
    private final Object mHalLock = new Object();
    private final boolean mHasNativeHal;
    private final PowerManager.WakeLock mWakeLock;

    private static native int[] halGetCarrierFrequencies();

    private static native boolean halOpen();

    private static native int halTransmit(int i, int[] iArr);

    ConsumerIrService(Context context) {
        this.mContext = context;
        this.mWakeLock = ((PowerManager) context.getSystemService("power")).newWakeLock(1, TAG);
        this.mWakeLock.setReferenceCounted(true);
        this.mHasNativeHal = halOpen();
        if (this.mContext.getPackageManager().hasSystemFeature("android.hardware.consumerir")) {
            if (!this.mHasNativeHal) {
                throw new RuntimeException("FEATURE_CONSUMER_IR present, but no IR HAL loaded!");
            }
        } else if (this.mHasNativeHal) {
            throw new RuntimeException("IR HAL present, but FEATURE_CONSUMER_IR is not set!");
        }
    }

    public boolean hasIrEmitter() {
        return this.mHasNativeHal;
    }

    private void throwIfNoIrEmitter() {
        if (!this.mHasNativeHal) {
            throw new UnsupportedOperationException("IR emitter not available");
        }
    }

    public void transmit(String packageName, int carrierFrequency, int[] pattern) {
        if (this.mContext.checkCallingOrSelfPermission("android.permission.TRANSMIT_IR") == 0) {
            long totalXmitTime = 0;
            int length = pattern.length;
            int i = 0;
            while (i < length) {
                int slice = pattern[i];
                if (slice > 0) {
                    totalXmitTime += (long) slice;
                    i++;
                } else {
                    throw new IllegalArgumentException("Non-positive IR slice");
                }
            }
            if (totalXmitTime <= 2000000) {
                throwIfNoIrEmitter();
                synchronized (this.mHalLock) {
                    int err = halTransmit(carrierFrequency, pattern);
                    if (err < 0) {
                        Slog.e(TAG, "Error transmitting: " + err);
                    }
                }
                return;
            }
            throw new IllegalArgumentException("IR pattern too long");
        }
        throw new SecurityException("Requires TRANSMIT_IR permission");
    }

    public int[] getCarrierFrequencies() {
        int[] halGetCarrierFrequencies;
        if (this.mContext.checkCallingOrSelfPermission("android.permission.TRANSMIT_IR") == 0) {
            throwIfNoIrEmitter();
            synchronized (this.mHalLock) {
                halGetCarrierFrequencies = halGetCarrierFrequencies();
            }
            return halGetCarrierFrequencies;
        }
        throw new SecurityException("Requires TRANSMIT_IR permission");
    }
}
