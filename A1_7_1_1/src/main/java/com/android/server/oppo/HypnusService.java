package com.android.server.oppo;

import android.content.Context;
import android.util.Log;
import com.android.internal.app.IHypnusService.Stub;
import com.oppo.hypnus.Hypnus;

public class HypnusService extends Stub {
    private static final String TAG = "HypnusService";
    private Context mContext;
    private Hypnus mHyp = new Hypnus();

    public HypnusService(Context context) {
        this.mContext = context;
    }

    public void hypnusSetNotification(int msg_src, int msg_type, long msg_time, int pid, int v0, int v1) {
        if (this.mHyp != null) {
            this.mHyp.hypnusSetNotification(msg_src, msg_type, msg_time, pid, v0, v1);
        } else {
            Log.e(TAG, "mHyp is not initialized!");
        }
    }

    public void hypnusSetScene(int pid, String processName) {
        if (this.mHyp != null) {
            this.mHyp.hypnusSetScene(pid, processName);
        } else {
            Log.e(TAG, "mHyp is not initialized!");
        }
    }

    public void hypnusSetAction(int action, int timeout) {
        if (this.mHyp != null) {
            this.mHyp.hypnusSetAction(action, timeout);
        } else {
            Log.e(TAG, "mHyp is not initialized!");
        }
    }

    public void hypnusSetBurst(int tid, int type, int timeout) {
        if (this.mHyp != null) {
            this.mHyp.hypnusSetBurst(tid, type, timeout);
        } else {
            Log.e(TAG, "mHyp is not initialized!");
        }
    }

    public boolean isHypnusOK() {
        if (this.mHyp != null) {
            return this.mHyp.isHypnusOK();
        }
        Log.e(TAG, "mHyp is not initialized!");
        return false;
    }
}
