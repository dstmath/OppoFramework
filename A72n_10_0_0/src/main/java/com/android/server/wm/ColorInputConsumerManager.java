package com.android.server.wm;

import android.os.IBinder;
import android.os.SystemProperties;
import android.util.ArrayMap;
import android.util.Slog;
import android.view.InputChannel;
import com.android.server.display.ai.utils.ColorAILog;
import com.android.server.wm.ColorInputConsumer;

class ColorInputConsumerManager implements ColorInputConsumer.Callback {
    private static final String TAG = "ColorInputConsumerManager";
    private static ColorInputConsumerManager sInstance;
    private boolean mDebug = SystemProperties.getBoolean(ColorAILog.OPPO_LOG_KEY, false);
    private final ArrayMap<String, ColorInputConsumer> mInputConsumers = new ArrayMap<>();

    ColorInputConsumerManager() {
    }

    public static ColorInputConsumerManager getInstance() {
        if (sInstance == null) {
            synchronized (ColorInputConsumerManager.class) {
                if (sInstance == null) {
                    sInstance = new ColorInputConsumerManager();
                }
            }
        }
        return sInstance;
    }

    public void createMonitorInputConsumer(WindowManagerService service, IBinder token, String name, InputChannel inputChannel) {
        Slog.i(TAG, "createMonitorInputConsumer name = " + name);
        synchronized (this.mInputConsumers) {
            if (!this.mInputConsumers.containsKey(name)) {
                this.mInputConsumers.put(name, new ColorInputConsumer(service, token, name, inputChannel, this));
            } else {
                throw new IllegalStateException("Existing input consumer found with name: " + name);
            }
        }
    }

    public boolean destroyMonitorInputConsumer(String name) {
        Slog.i(TAG, "destroyMonitorInputConsumer name = " + name);
        synchronized (this.mInputConsumers) {
            ColorInputConsumer consumer = this.mInputConsumers.remove(name);
            if (consumer == null) {
                return false;
            }
            consumer.dispose();
            return true;
        }
    }

    public void pilferPointers(String name) {
        if (this.mDebug) {
            Slog.i(TAG, "start pilferPointers, consumer name: " + name);
        }
        synchronized (this.mInputConsumers) {
            ColorInputConsumer consumer = this.mInputConsumers.getOrDefault(name, null);
            if (consumer != null) {
                consumer.pilferPointers();
            } else {
                Slog.e(TAG, "Uncorrect input monitor name, pilferPointers failed");
            }
        }
    }

    @Override // com.android.server.wm.ColorInputConsumer.Callback
    public void onBinderDied(String name) {
        Slog.i(TAG, "onBinderDied name = " + name + " size = " + this.mInputConsumers.size());
        destroyMonitorInputConsumer(name);
    }
}
