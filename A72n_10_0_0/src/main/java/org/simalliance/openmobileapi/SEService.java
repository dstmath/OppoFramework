package org.simalliance.openmobileapi;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.se.omapi.ISecureElementListener;
import android.se.omapi.ISecureElementReader;
import android.se.omapi.ISecureElementService;
import android.util.Log;
import java.util.HashMap;
import org.simalliance.openmobileapi.service.ISmartcardService;
import org.simalliance.openmobileapi.service.ISmartcardServiceCallback;

public final class SEService {
    public static final int IO_ERROR = 1;
    public static final int NFC_IN_USE = 3;
    public static final int NO_SUCH_ELEMENT_ERROR = 2;
    private static final String TAG = "OMAPI.SEService";
    private final ISmartcardServiceCallback mCallback = new ISmartcardServiceCallback.Stub() {
        /* class org.simalliance.openmobileapi.SEService.AnonymousClass1 */
    };
    private CallBack mCallerCallback;
    private ServiceConnection mConnection;
    private final Context mContext;
    private final Object mLock = new Object();
    private final HashMap<String, Reader> mReaders = new HashMap<>();
    private SEListener mSEListener = new SEListener();
    private volatile ISmartcardService mSecureElementService;

    public interface CallBack {
        void serviceConnected(SEService sEService);
    }

    /* access modifiers changed from: private */
    public class SEListener extends ISecureElementListener.Stub {
        public CallBack mListener;

        private SEListener() {
            this.mListener = null;
        }

        /* JADX DEBUG: Multi-variable search result rejected for r0v0, resolved type: org.simalliance.openmobileapi.SEService$SEListener */
        /* JADX WARN: Multi-variable type inference failed */
        public IBinder asBinder() {
            return this;
        }

        public void onConnected() {
            CallBack callBack = this.mListener;
            if (callBack != null) {
                callBack.serviceConnected(SEService.this);
            }
        }
    }

    public SEService(Context context, CallBack listener) {
        if (context != null) {
            this.mContext = context;
            this.mSEListener.mListener = listener;
            this.mConnection = new ServiceConnection() {
                /* class org.simalliance.openmobileapi.SEService.AnonymousClass2 */

                public synchronized void onServiceConnected(ComponentName className, IBinder service) {
                    SEService.this.mSecureElementService = ISmartcardService.Stub.asInterface(service);
                    if (SEService.this.mSEListener != null) {
                        SEService.this.mSEListener.onConnected();
                    }
                    Log.i(SEService.TAG, "Service onServiceConnected");
                }

                public void onServiceDisconnected(ComponentName className) {
                    SEService.this.mSecureElementService = null;
                    Log.i(SEService.TAG, "Service onServiceDisconnected");
                }
            };
            Intent intent = new Intent(ISecureElementService.class.getName());
            intent.setClassName("com.android.se", "com.android.se.SecureElementService");
            if (this.mContext.bindService(intent, this.mConnection, 1)) {
                Log.i(TAG, "bindService successful");
                return;
            }
            return;
        }
        throw new NullPointerException("context must not be null");
    }

    public boolean isConnected() {
        return this.mSecureElementService != null;
    }

    public Reader[] getReaders() {
        Exception e;
        if (this.mSecureElementService != null) {
            try {
                String[] readerNames = this.mSecureElementService.getReaders();
                Reader[] readers = new Reader[readerNames.length];
                int i = 0;
                for (String readerName : readerNames) {
                    if (this.mReaders.get(readerName) == null) {
                        try {
                            this.mReaders.put(readerName, new Reader(this, readerName, getReader(readerName)));
                            int i2 = i + 1;
                            try {
                                readers[i] = this.mReaders.get(readerName);
                                i = i2;
                            } catch (Exception e2) {
                                e = e2;
                                i = i2;
                                Log.e(TAG, "Error adding Reader: " + readerName, e);
                            }
                        } catch (Exception e3) {
                            e = e3;
                            Log.e(TAG, "Error adding Reader: " + readerName, e);
                        }
                    } else {
                        readers[i] = this.mReaders.get(readerName);
                        i++;
                    }
                }
                return readers;
            } catch (RemoteException e4) {
                throw new RuntimeException(e4);
            }
        } else {
            throw new IllegalStateException("service not connected to system");
        }
    }

    public void shutdown() {
        synchronized (this.mLock) {
            if (this.mSecureElementService != null) {
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
            this.mSecureElementService = null;
        }
    }

    public String getVersion() {
        return "3.3";
    }

    /* access modifiers changed from: package-private */
    public ISecureElementListener getListener() {
        return this.mSEListener;
    }

    private ISecureElementReader getReader(String name) {
        try {
            return this.mSecureElementService.getReader(name);
        } catch (RemoteException e) {
            throw new IllegalStateException(e.getMessage());
        }
    }

    /* access modifiers changed from: package-private */
    public ISmartcardServiceCallback getCallback() {
        return this.mCallback;
    }
}
