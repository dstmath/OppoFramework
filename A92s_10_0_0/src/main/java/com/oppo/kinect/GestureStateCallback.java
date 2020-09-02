package com.oppo.kinect;

import android.content.ComponentName;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import com.oppo.kinect.IRemoteServiceCallback;

public abstract class GestureStateCallback {
    public static final int MSG_NOTIFYRESULT = 0;
    public static final int MSG_SERVICEDISCONNECTED = 1;
    public IRemoteServiceCallback mCallback = new IRemoteServiceCallback.Stub() {
        /* class com.oppo.kinect.GestureStateCallback.AnonymousClass1 */
        Handler mHandler = new Handler() {
            /* class com.oppo.kinect.GestureStateCallback.AnonymousClass1.AnonymousClass1 */

            @Override // android.os.Handler
            public void handleMessage(Message msg) {
                if (msg.what == 0) {
                    GestureStateCallback.this.notifyResult((int[]) msg.obj);
                }
            }
        };

        @Override // com.oppo.kinect.IRemoteServiceCallback
        public void notifyResult(int[] value) throws RemoteException {
            Message.obtain(this.mHandler, 0, -1, -1, value).sendToTarget();
        }
    };

    public void notifyResult(int[] value) {
    }

    public void onServiceDisconnected(ComponentName name) {
    }
}
