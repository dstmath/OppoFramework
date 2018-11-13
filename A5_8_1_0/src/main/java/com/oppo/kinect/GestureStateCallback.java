package com.oppo.kinect;

import android.content.ComponentName;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import com.oppo.kinect.IRemoteServiceCallback.Stub;

public abstract class GestureStateCallback {
    public static final int MSG_NOTIFYRESULT = 0;
    public static final int MSG_SERVICEDISCONNECTED = 1;
    public IRemoteServiceCallback mCallback = new Stub() {
        Handler mHandler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 0:
                        GestureStateCallback.this.notifyResult(msg.obj);
                        return;
                    default:
                        return;
                }
            }
        };

        public void notifyResult(int[] value) throws RemoteException {
            Message.obtain(this.mHandler, 0, -1, -1, value).sendToTarget();
        }
    };

    public void notifyResult(int[] value) {
    }

    public void onServiceDisconnected(ComponentName name) {
    }
}
