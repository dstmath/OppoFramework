package org.simalliance.openmobileapi;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import java.io.IOException;
import java.util.HashMap;
import org.simalliance.openmobileapi.service.ISmartcardService;
import org.simalliance.openmobileapi.service.ISmartcardServiceCallback;
import org.simalliance.openmobileapi.service.ISmartcardServiceCallback.Stub;
import org.simalliance.openmobileapi.service.ISmartcardServiceReader;
import org.simalliance.openmobileapi.service.SmartcardError;

public class SEService {
    private static final String SERVICE_TAG = "SmartcardService - SEService";
    private final ISmartcardServiceCallback mCallback = new Stub() {
    };
    private CallBack mCallerCallback;
    private ServiceConnection mConnection;
    private final Context mContext;
    private final Object mLock = new Object();
    private final HashMap<String, Reader> mReaders = new HashMap();
    private volatile ISmartcardService mSmartcardService;

    public interface CallBack {
        void serviceConnected(SEService sEService);
    }

    public SEService(Context context, CallBack listener) {
        if (context == null) {
            throw new NullPointerException("context must not be null");
        }
        this.mContext = context;
        this.mCallerCallback = listener;
        this.mConnection = new ServiceConnection() {
            public synchronized void onServiceConnected(ComponentName className, IBinder service) {
                SEService.this.mSmartcardService = ISmartcardService.Stub.asInterface(service);
                if (SEService.this.mCallerCallback != null) {
                    SEService.this.mCallerCallback.serviceConnected(SEService.this);
                }
                Log.i(SEService.SERVICE_TAG, "Service onServiceConnected");
            }

            public void onServiceDisconnected(ComponentName className) {
                SEService.this.mSmartcardService = null;
                Log.i(SEService.SERVICE_TAG, "Service onServiceDisconnected");
            }
        };
        Intent intent = new Intent(ISmartcardService.class.getName());
        intent.setClassName("org.simalliance.openmobileapi.service", "org.simalliance.openmobileapi.service.SmartcardService");
        if (this.mContext.bindService(intent, this.mConnection, 1)) {
            Log.i(SERVICE_TAG, "bindService successful");
        }
    }

    public boolean isConnected() {
        return this.mSmartcardService != null;
    }

    public Reader[] getReaders() {
        if (this.mSmartcardService == null) {
            throw new IllegalStateException("service not connected to system");
        }
        try {
            String[] readerNames = this.mSmartcardService.getReaders();
            Reader[] readers = new Reader[readerNames.length];
            int length = readerNames.length;
            int i = 0;
            int i2 = 0;
            while (i < length) {
                int i3;
                String readerName = readerNames[i];
                if (this.mReaders.get(readerName) == null) {
                    this.mReaders.put(readerName, new Reader(this, readerName));
                    i3 = i2 + 1;
                    readers[i2] = (Reader) this.mReaders.get(readerName);
                } else {
                    i3 = i2 + 1;
                    readers[i2] = (Reader) this.mReaders.get(readerName);
                }
                i++;
                i2 = i3;
            }
            return readers;
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    public void shutdown() {
        synchronized (this.mLock) {
            if (this.mSmartcardService != null) {
                for (Reader reader : this.mReaders.values()) {
                    try {
                        reader.closeSessions();
                    } catch (Exception e) {
                    }
                }
            }
            try {
                this.mContext.unbindService(this.mConnection);
            } catch (IllegalArgumentException e2) {
            }
            this.mSmartcardService = null;
        }
    }

    public String getVersion() {
        return "3.0";
    }

    public ISmartcardServiceReader getReader(String name) throws IOException {
        try {
            SmartcardError error = new SmartcardError();
            ISmartcardServiceReader reader = this.mSmartcardService.getReader(name, error);
            if (error.isSet()) {
                error.throwException();
            }
            return reader;
        } catch (RemoteException e) {
            throw new IllegalStateException(e.getMessage());
        }
    }

    ISmartcardServiceCallback getCallback() {
        return this.mCallback;
    }
}
