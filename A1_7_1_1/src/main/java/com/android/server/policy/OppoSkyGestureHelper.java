package com.android.server.policy;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.UserHandle;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.android.server.lights.OppoLightsService.ButtonLight;

public class OppoSkyGestureHelper {
    private static final String ACTION_KEY_DOWN = "com.oppo.intent.action.SKYGESTURE_ACTION_KEY_DOWN";
    private static final String ACTION_KEY_UP = "com.oppo.intent.action.SKYGESTURE_ACTION_KEY_UP";
    private static final String TAG = "OppoSkyGestureHelper";
    private static final int WAIT_TIME_CPU_LOCK = 6000;
    private static final int WAIT_TIME_UNBIND_SERVICE = 10000;
    PhoneStateListener listener = new PhoneStateListener() {
        public void onCallStateChanged(int state, String incomingNumber) {
            super.onCallStateChanged(state, incomingNumber);
            switch (state) {
                case 0:
                    OppoSkyGestureHelper.this.mIsInOffHook = false;
                    return;
                case 1:
                    OppoSkyGestureHelper.this.mIsInOffHook = true;
                    return;
                case 2:
                    OppoSkyGestureHelper.this.mIsInOffHook = true;
                    return;
                default:
                    return;
            }
        }
    };
    private Context mContext;
    private WakeLock mGestureCpuLock;
    private Handler mHandler;
    private boolean mIsInOffHook = false;
    private boolean mIsVolumeLongPress = false;
    private PowerManager mPowerManager;
    ServiceConnection mSkyGestureConnection = null;
    final Object mSkyGestureLock = new Object();
    final Runnable mSkyGestureTimeout = new Runnable() {
        public void run() {
            synchronized (OppoSkyGestureHelper.this.mSkyGestureLock) {
                if (OppoSkyGestureHelper.this.mSkyGestureConnection != null) {
                    OppoSkyGestureHelper.this.mContext.unbindService(OppoSkyGestureHelper.this.mSkyGestureConnection);
                    OppoSkyGestureHelper.this.mSkyGestureConnection = null;
                }
            }
        }
    };

    OppoSkyGestureHelper(Handler handler, Context context, PowerManager power) {
        this.mHandler = handler;
        this.mContext = context;
        this.mPowerManager = power;
        this.mGestureCpuLock = this.mPowerManager.newWakeLock(1, "OppoSkyGestureHelper.mGestureCpuLock");
        ((TelephonyManager) context.getSystemService("phone")).listen(this.listener, 32);
    }

    public void dealSkyGestureDown() {
        if (!this.mIsInOffHook) {
            bindSkyGestureService();
            this.mIsVolumeLongPress = true;
            this.mContext.sendBroadcast(new Intent(ACTION_KEY_DOWN));
            this.mGestureCpuLock.acquire(ButtonLight.TIMEOUT_DEFAULT);
            Log.i(TAG, "send broadcast silence action for ACTION_KEY_DOWN.");
            this.mHandler.removeCallbacks(this.mSkyGestureTimeout);
        }
    }

    public void dealSkyGestureUp() {
        this.mContext.sendBroadcast(new Intent(ACTION_KEY_UP));
        this.mGestureCpuLock.acquire(ButtonLight.TIMEOUT_DEFAULT);
        Log.i(TAG, "send broadcast silence action for ACTION_KEY_UP.");
        this.mIsVolumeLongPress = false;
        this.mHandler.postDelayed(this.mSkyGestureTimeout, 10000);
    }

    public boolean isVolumeLongPress() {
        return this.mIsVolumeLongPress;
    }

    /* JADX WARNING: Missing block: B:12:0x0038, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void bindSkyGestureService() {
        synchronized (this.mSkyGestureLock) {
            if (this.mSkyGestureConnection != null) {
                return;
            }
            ComponentName cn = new ComponentName("com.oppo.gestureguide.custom", "com.oppo.gestureguide.custom.service.SkyGestureService");
            Intent intent = new Intent();
            intent.setComponent(cn);
            ServiceConnection conn = new ServiceConnection() {
                public void onServiceConnected(ComponentName name, IBinder service) {
                    Log.i(OppoSkyGestureHelper.TAG, "onServiceConnected");
                }

                public void onServiceDisconnected(ComponentName name) {
                    Log.i(OppoSkyGestureHelper.TAG, "onServiceDisconnected");
                }
            };
            if (this.mContext.bindServiceAsUser(intent, conn, 1, UserHandle.CURRENT)) {
                this.mSkyGestureConnection = conn;
                Log.i(TAG, "bindService ");
            } else {
                Log.i(TAG, "bindService error");
            }
        }
    }
}
