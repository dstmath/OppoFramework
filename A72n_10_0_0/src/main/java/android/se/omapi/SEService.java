package android.se.omapi;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.se.omapi.ISecureElementListener;
import android.util.Log;
import java.util.HashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import org.simalliance.openmobileapi.service.ISmartcardService;

public final class SEService {
    public static final int IO_ERROR = 1;
    public static final int NO_SUCH_ELEMENT_ERROR = 2;
    private static final String TAG = "OMAPI.SEService";
    private ServiceConnection mConnection;
    private final Context mContext;
    private final Object mLock = new Object();
    private final HashMap<String, Reader> mReaders = new HashMap<>();
    private SEListener mSEListener = new SEListener();
    private volatile ISecureElementService mSecureElementService;
    private volatile ISmartcardService mSmartcardService;

    public interface OnConnectedListener {
        void onConnected();
    }

    /* access modifiers changed from: private */
    public class SEListener extends ISecureElementListener.Stub {
        public Executor mExecutor;
        public OnConnectedListener mListener;

        private SEListener() {
            this.mListener = null;
            this.mExecutor = null;
        }

        @Override // android.os.IInterface, android.se.omapi.ISecureElementListener.Stub
        public IBinder asBinder() {
            return this;
        }

        public void onConnected() {
            Executor executor;
            if (this.mListener != null && (executor = this.mExecutor) != null) {
                executor.execute(new Runnable() {
                    /* class android.se.omapi.SEService.SEListener.AnonymousClass1 */

                    public void run() {
                        SEListener.this.mListener.onConnected();
                    }
                });
            }
        }
    }

    public SEService(Context context, Executor executor, OnConnectedListener listener) {
        if (context == null || listener == null || executor == null) {
            throw new NullPointerException("Arguments must not be null");
        }
        this.mContext = context;
        SEListener sEListener = this.mSEListener;
        sEListener.mListener = listener;
        sEListener.mExecutor = executor;
        final Semaphore available = new Semaphore(1, true);
        this.mConnection = new ServiceConnection() {
            /* class android.se.omapi.SEService.AnonymousClass1 */

            @Override // android.content.ServiceConnection
            public synchronized void onServiceConnected(ComponentName className, IBinder service) {
                SEService.this.mSmartcardService = ISmartcardService.Stub.asInterface(service);
                if (SEService.this.mSEListener != null) {
                    try {
                        available.tryAcquire(4, TimeUnit.SECONDS);
                    } catch (Exception e) {
                        Log.e(SEService.TAG, e.toString());
                    }
                    SEService.this.mSEListener.onConnected();
                }
                Log.i(SEService.TAG, "Service onServiceConnected");
            }

            @Override // android.content.ServiceConnection
            public void onServiceDisconnected(ComponentName className) {
                SEService.this.mSmartcardService = null;
                Log.i(SEService.TAG, "Service onServiceDisconnected");
            }
        };
        try {
            available.acquire();
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
        Intent intent = new Intent(ISecureElementService.class.getName());
        intent.setClassName("com.android.se", "com.android.se.SecureElementService");
        if (this.mContext.bindService(intent, this.mConnection, 1)) {
            Log.i(TAG, "bindService successful");
        }
        available.release();
    }

    public boolean isConnected() {
        return this.mSmartcardService != null;
    }

    public Reader[] getReaders() {
        Exception e;
        if (this.mSmartcardService != null) {
            try {
                String[] readerNames = this.mSmartcardService.getReaders();
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
        return "3.3";
    }

    /* access modifiers changed from: package-private */
    public ISecureElementListener getListener() {
        return this.mSEListener;
    }

    private ISecureElementReader getReader(String name) {
        try {
            return this.mSmartcardService.getReader(name);
        } catch (RemoteException e) {
            throw new IllegalStateException(e.getMessage());
        }
    }
}
