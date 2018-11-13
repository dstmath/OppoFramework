package com.qualcomm.wfd;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import com.qualcomm.wfd.service.ISessionManagerService;
import com.qualcomm.wfd.service.ISessionManagerService.Stub;

/* compiled from: ExtendedRemoteDisplay */
class ServiceUtil {
    private static final String TAG = "ExtendedRemoteDisplay.ServiceUtil";
    private static Handler eventHandler = null;
    protected static ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.d(ServiceUtil.TAG, "Connection object created");
            ServiceUtil.mServiceAlreadyBound = true;
            ServiceUtil.uniqueInstance = Stub.asInterface(service);
            synchronized (ServiceUtil.class) {
                ServiceUtil.class.notifyAll();
            }
            ServiceUtil.eventHandler.sendMessage(ServiceUtil.eventHandler.obtainMessage(ERDConstants.SERVICE_BOUND_CALLBACK.value()));
        }

        public void onServiceDisconnected(ComponentName className) {
            Log.d(ServiceUtil.TAG, "Remote service disconnected");
            ServiceUtil.mServiceAlreadyBound = false;
            Log.d(ServiceUtil.TAG, "Post TEARDOWN");
            ServiceUtil.eventHandler.sendMessage(ServiceUtil.eventHandler.obtainMessage(ERDConstants.TEARDOWN_CALLBACK.value()));
            Log.d(ServiceUtil.TAG, "Post INVALID");
            ServiceUtil.eventHandler.sendMessage(ServiceUtil.eventHandler.obtainMessage(ERDConstants.INVALID_STATE_CALLBACK.value()));
        }
    };
    private static boolean mServiceAlreadyBound = false;
    private static ISessionManagerService uniqueInstance = null;

    /* compiled from: ExtendedRemoteDisplay */
    public static class ServiceFailedToBindException extends Exception {
        public static final long serialVersionUID = 1;

        /* synthetic */ ServiceFailedToBindException(String inString, ServiceFailedToBindException -this1) {
            this(inString);
        }

        private ServiceFailedToBindException(String inString) {
            super(inString);
        }
    }

    ServiceUtil() {
    }

    protected static boolean getmServiceAlreadyBound() {
        return mServiceAlreadyBound;
    }

    public static void bindService(Context context, Handler inEventHandler) throws ServiceFailedToBindException {
        if (!mServiceAlreadyBound || uniqueInstance == null) {
            Log.d(TAG, "Binding to WFD Service");
            Intent serviceIntent = new Intent("com.qualcomm.wfd.service.WfdService");
            serviceIntent.setPackage("com.qualcomm.wfd.service");
            eventHandler = inEventHandler;
            if (!context.bindService(serviceIntent, mConnection, 1)) {
                Log.e(TAG, "Failed to connect to Provider service");
                throw new ServiceFailedToBindException("Failed to connect to Provider service", null);
            }
        }
    }

    public static void unbindService(Context context) {
        if (mServiceAlreadyBound) {
            try {
                context.unbindService(mConnection);
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "IllegalArgumentException: " + e);
            }
            mServiceAlreadyBound = false;
            uniqueInstance = null;
        }
    }

    public static synchronized ISessionManagerService getInstance() {
        ISessionManagerService iSessionManagerService;
        synchronized (ServiceUtil.class) {
            while (uniqueInstance == null) {
                Log.d(TAG, "Waiting for service to bind ...");
                try {
                    ServiceUtil.class.wait();
                } catch (InterruptedException e) {
                    Log.e(TAG, "InterruptedException: " + e);
                }
            }
            iSessionManagerService = uniqueInstance;
        }
        return iSessionManagerService;
    }
}
