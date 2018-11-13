package android.bluetooth;

import android.bluetooth.BluetoothProfile.ServiceListener;
import android.bluetooth.IBluetoothDun.Stub;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Process;
import android.os.RemoteException;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;

public final class BluetoothDun implements BluetoothProfile {
    public static final String ACTION_CONNECTION_STATE_CHANGED = "codeaurora.bluetooth.dun.profile.action.CONNECTION_STATE_CHANGED";
    private static final boolean DBG = false;
    private static final String TAG = "BluetoothDun";
    private static final boolean VDBG = false;
    private BluetoothAdapter mAdapter;
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            BluetoothDun.this.mDunService = Stub.asInterface(service);
            if (BluetoothDun.this.mServiceListener != null) {
                BluetoothDun.this.mServiceListener.onServiceConnected(20, BluetoothDun.this);
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            BluetoothDun.this.mDunService = null;
            if (BluetoothDun.this.mServiceListener != null) {
                BluetoothDun.this.mServiceListener.onServiceDisconnected(20);
            }
        }
    };
    private Context mContext;
    private IBluetoothDun mDunService;
    private ServiceListener mServiceListener;
    private IBluetoothStateChangeCallback mStateChangeCallback = new IBluetoothStateChangeCallback.Stub() {
        public void onBluetoothStateChange(boolean on) {
            Log.d(BluetoothDun.TAG, "onBluetoothStateChange on: " + on);
            if (on) {
                try {
                    if (BluetoothDun.this.mDunService == null) {
                        Log.d(BluetoothDun.TAG, "onBluetoothStateChange call bindService");
                        BluetoothDun.this.doBind();
                        return;
                    }
                    return;
                } catch (IllegalStateException e) {
                    Log.e(BluetoothDun.TAG, "onBluetoothStateChange: could not bind to DUN service: ", e);
                    return;
                } catch (SecurityException e2) {
                    Log.e(BluetoothDun.TAG, "onBluetoothStateChange: could not bind to DUN service: ", e2);
                    return;
                }
            }
            synchronized (BluetoothDun.this.mConnection) {
                if (BluetoothDun.this.mDunService != null) {
                    try {
                        BluetoothDun.this.mDunService = null;
                        BluetoothDun.this.mContext.unbindService(BluetoothDun.this.mConnection);
                    } catch (Exception re) {
                        Log.e(BluetoothDun.TAG, "", re);
                    }
                }
            }
            return;
        }
    };

    BluetoothDun(Context context, ServiceListener l) {
        this.mContext = context;
        this.mServiceListener = l;
        this.mAdapter = BluetoothAdapter.getDefaultAdapter();
        try {
            this.mAdapter.getBluetoothManager().registerStateChangeCallback(this.mStateChangeCallback);
        } catch (RemoteException re) {
            Log.w(TAG, "Unable to register BluetoothStateChangeCallback", re);
        }
        Log.d(TAG, "BluetoothDun() call bindService");
        doBind();
    }

    boolean doBind() {
        Intent intent = new Intent(IBluetoothDun.class.getName());
        ComponentName comp = intent.resolveSystemService(this.mContext.getPackageManager(), 0);
        intent.setComponent(comp);
        if (comp != null && (this.mContext.bindServiceAsUser(intent, this.mConnection, 0, Process.myUserHandle()) ^ 1) == 0) {
            return true;
        }
        Log.e(TAG, "Could not bind to Bluetooth Dun Service with " + intent);
        return false;
    }

    void close() {
        this.mServiceListener = null;
        IBluetoothManager mgr = this.mAdapter.getBluetoothManager();
        if (mgr != null) {
            try {
                mgr.unregisterStateChangeCallback(this.mStateChangeCallback);
            } catch (RemoteException re) {
                Log.w(TAG, "Unable to unregister BluetoothStateChangeCallback", re);
            }
        }
        synchronized (this.mConnection) {
            if (this.mDunService != null) {
                try {
                    this.mDunService = null;
                    this.mContext.unbindService(this.mConnection);
                } catch (Exception re2) {
                    Log.e(TAG, "", re2);
                }
            }
        }
        return;
    }

    protected void finalize() {
        close();
    }

    public boolean disconnect(BluetoothDevice device) {
        if (this.mDunService != null && isEnabled() && isValidDevice(device)) {
            try {
                return this.mDunService.disconnect(device);
            } catch (RemoteException e) {
                Log.e(TAG, "Stack:" + Log.getStackTraceString(new Throwable()));
                return false;
            }
        }
        if (this.mDunService == null) {
            Log.w(TAG, "Proxy not attached to service");
        }
        return false;
    }

    public List<BluetoothDevice> getConnectedDevices() {
        if (this.mDunService == null || !isEnabled()) {
            if (this.mDunService == null) {
                Log.w(TAG, "Proxy not attached to service");
            }
            return new ArrayList();
        }
        try {
            return this.mDunService.getConnectedDevices();
        } catch (RemoteException e) {
            Log.e(TAG, "Stack:" + Log.getStackTraceString(new Throwable()));
            return new ArrayList();
        }
    }

    public List<BluetoothDevice> getDevicesMatchingConnectionStates(int[] states) {
        if (this.mDunService == null || !isEnabled()) {
            if (this.mDunService == null) {
                Log.w(TAG, "Proxy not attached to service");
            }
            return new ArrayList();
        }
        try {
            return this.mDunService.getDevicesMatchingConnectionStates(states);
        } catch (RemoteException e) {
            Log.e(TAG, "Stack:" + Log.getStackTraceString(new Throwable()));
            return new ArrayList();
        }
    }

    public int getConnectionState(BluetoothDevice device) {
        if (this.mDunService != null && isEnabled() && isValidDevice(device)) {
            try {
                return this.mDunService.getConnectionState(device);
            } catch (RemoteException e) {
                Log.e(TAG, "Stack:" + Log.getStackTraceString(new Throwable()));
                return 0;
            }
        }
        if (this.mDunService == null) {
            Log.w(TAG, "Proxy not attached to service");
        }
        return 0;
    }

    private boolean isEnabled() {
        if (this.mAdapter.getState() == 12) {
            return true;
        }
        return false;
    }

    private boolean isValidDevice(BluetoothDevice device) {
        if (device != null && BluetoothAdapter.checkBluetoothAddress(device.getAddress())) {
            return true;
        }
        return false;
    }

    private static void log(String msg) {
        Log.d(TAG, msg);
    }
}
