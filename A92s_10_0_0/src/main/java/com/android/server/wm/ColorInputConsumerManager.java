package com.android.server.wm;

import android.os.IBinder;
import android.util.ArrayMap;
import android.util.Slog;
import android.view.InputChannel;
import com.android.server.wm.ColorInputConsumer;

class ColorInputConsumerManager implements ColorInputConsumer.Callback {
    private static final String TAG = "ColorInputConsumerManager";
    private static ColorInputConsumerManager sInstance;
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
        if (!this.mInputConsumers.containsKey(name)) {
            this.mInputConsumers.put(name, new ColorInputConsumer(service, token, name, inputChannel, this));
            return;
        }
        throw new IllegalStateException("Existing input consumer found with name: " + name);
    }

    public boolean destroyMonitorInputConsumer(String name) {
        Slog.i(TAG, "destroyMonitorInputConsumer name = " + name);
        ColorInputConsumer consumer = this.mInputConsumers.remove(name);
        if (consumer == null) {
            return false;
        }
        consumer.disposeChannelsLw();
        return true;
    }

    @Override // com.android.server.wm.ColorInputConsumer.Callback
    public void onBinderDied(String name) {
        Slog.i(TAG, "onBinderDied name = " + name + " size = " + this.mInputConsumers.size());
        destroyMonitorInputConsumer(name);
    }
}
