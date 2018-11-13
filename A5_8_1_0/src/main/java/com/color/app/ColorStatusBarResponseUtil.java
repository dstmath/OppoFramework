package com.color.app;

import android.app.Activity;
import android.app.IColorClickTopCallback.Stub;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import java.lang.ref.WeakReference;

public class ColorStatusBarResponseUtil {
    public static Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    };
    private final String TAG = "ColorStatusBarResponseUtil";
    WeakReference<StatusBarClickListener> mClickReference;
    private ColorClickTopCallback mColorClickTopCallback = new ColorClickTopCallback(this, null);
    private IBinder mRemote = null;
    private StatusBarClickListener statusBarClickListener;

    public interface StatusBarClickListener {
        void onStatusBarClicked();
    }

    private class ColorClickTopCallback extends Stub {
        /* synthetic */ ColorClickTopCallback(ColorStatusBarResponseUtil this$0, ColorClickTopCallback -this1) {
            this();
        }

        private ColorClickTopCallback() {
        }

        public void onClickTopCallback() {
            ColorStatusBarResponseUtil.mHandler.post(new Runnable() {
                public void run() {
                    if (ColorStatusBarResponseUtil.this.mClickReference.get() != null) {
                        ((StatusBarClickListener) ColorStatusBarResponseUtil.this.mClickReference.get()).onStatusBarClicked();
                        Log.i("ColorStatusBarResponseUtil", "onStatusBarClicked is called at time :" + System.currentTimeMillis());
                    }
                }
            });
        }
    }

    public ColorStatusBarResponseUtil(Activity activity) {
    }

    public void onResume() {
        registerClickTopCallback();
    }

    public void onPause() {
        unregisterClickTopCallback();
    }

    public void registerClickTopCallback() {
        IBinder iBinder = null;
        if (this.mRemote == null) {
            this.mRemote = ServiceManager.getService("statusbar");
        }
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken("com.android.internal.statusbar.IStatusBarService");
        if (this.mColorClickTopCallback != null) {
            iBinder = this.mColorClickTopCallback;
        }
        data.writeStrongBinder(iBinder);
        try {
            this.mRemote.transact(20003, data, reply, 0);
        } catch (RemoteException ex) {
            Log.e("ColorStatusBarResponseUtil", "register", ex);
        }
        data.recycle();
        reply.recycle();
    }

    public void unregisterClickTopCallback() {
        IBinder iBinder = null;
        if (this.mRemote == null) {
            this.mRemote = ServiceManager.getService("statusbar");
        }
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken("com.android.internal.statusbar.IStatusBarService");
        if (this.mColorClickTopCallback != null) {
            iBinder = this.mColorClickTopCallback;
        }
        data.writeStrongBinder(iBinder);
        try {
            this.mRemote.transact(20005, data, reply, 0);
        } catch (RemoteException ex) {
            Log.e("ColorStatusBarResponseUtil", "unregisterClickTopCallback", ex);
        }
        data.recycle();
        reply.recycle();
    }

    public void setStatusBarClickListener(StatusBarClickListener listener) {
        this.mClickReference = new WeakReference(listener);
    }
}
