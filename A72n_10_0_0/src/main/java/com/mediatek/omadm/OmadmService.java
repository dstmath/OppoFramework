package com.mediatek.omadm;

import android.content.Context;
import android.util.Slog;
import com.android.server.SystemService;

public class OmadmService extends SystemService {
    private final String TAG = "OmadmService";
    private OmadmServiceImpl mOmadmServiceImpl;

    public OmadmService(Context context) {
        super(context);
        this.mOmadmServiceImpl = new OmadmServiceImpl(context);
    }

    public void onStart() {
        Slog.d("OmadmService", "Start OmadmService.");
    }

    public void onBootPhase(int phase) {
        if (phase == 500) {
            Slog.d("OmadmService", "onBootPhase OmadmService.");
        }
    }
}
