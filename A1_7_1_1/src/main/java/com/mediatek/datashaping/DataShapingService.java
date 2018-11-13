package com.mediatek.datashaping;

import android.content.Context;
import android.util.Slog;
import com.android.server.SystemService;

public class DataShapingService extends SystemService {
    private final String TAG = "DataShapingService";
    private DataShapingServiceImpl mImpl;

    public DataShapingService(Context context) {
        super(context);
        this.mImpl = new DataShapingServiceImpl(context);
    }

    public void onStart() {
        Slog.d("DataShapingService", "Start DataShaping Service.");
        publishBinderService("data_shaping", this.mImpl);
    }

    public void onBootPhase(int phase) {
        if (phase == 500) {
            this.mImpl.start();
        }
    }
}
