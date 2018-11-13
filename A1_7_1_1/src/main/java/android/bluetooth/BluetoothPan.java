package android.bluetooth;

import android.bluetooth.BluetoothProfile.ServiceListener;
import android.bluetooth.IBluetoothPan.Stub;
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

public final class BluetoothPan implements BluetoothProfile {
    public static final String ACTION_CONNECTION_STATE_CHANGED = "android.bluetooth.pan.profile.action.CONNECTION_STATE_CHANGED";
    private static final boolean DBG = true;
    public static final String EXTRA_LOCAL_ROLE = "android.bluetooth.pan.extra.LOCAL_ROLE";
    public static final int LOCAL_NAP_ROLE = 1;
    public static final int LOCAL_PANU_ROLE = 2;
    public static final int PAN_CONNECT_FAILED_ALREADY_CONNECTED = 1001;
    public static final int PAN_CONNECT_FAILED_ATTEMPT_FAILED = 1002;
    public static final int PAN_DISCONNECT_FAILED_NOT_CONNECTED = 1000;
    public static final int PAN_OPERATION_GENERIC_FAILURE = 1003;
    public static final int PAN_OPERATION_SUCCESS = 1004;
    public static final int PAN_ROLE_NONE = 0;
    public static final int REMOTE_NAP_ROLE = 1;
    public static final int REMOTE_PANU_ROLE = 2;
    private static final String TAG = "BluetoothPan";
    private static final boolean VDBG = true;
    private BluetoothAdapter mAdapter;
    private final ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            synchronized (BluetoothPan.this.mConnection) {
                Log.d(BluetoothPan.TAG, "BluetoothPAN Proxy object connected");
                BluetoothPan.this.mPanService = Stub.asInterface(service);
                if (BluetoothPan.this.mServiceListener != null) {
                    BluetoothPan.this.mServiceListener.onServiceConnected(5, BluetoothPan.this);
                }
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            synchronized (BluetoothPan.this.mConnection) {
                Log.d(BluetoothPan.TAG, "BluetoothPAN Proxy object disconnected");
                BluetoothPan.this.mPanService = null;
                if (BluetoothPan.this.mServiceListener != null) {
                    BluetoothPan.this.mServiceListener.onServiceDisconnected(5);
                }
            }
        }
    };
    private Context mContext;
    private IBluetoothPan mPanService;
    private ServiceListener mServiceListener;
    private final IBluetoothStateChangeCallback mStateChangeCallback = new IBluetoothStateChangeCallback.Stub() {
        public void onBluetoothStateChange(boolean on) {
            Log.d(BluetoothPan.TAG, "onBluetoothStateChange on: " + on);
            ServiceConnection -get0;
            if (on) {
                -get0 = BluetoothPan.this.mConnection;
                synchronized (-get0) {
                    try {
                        if (BluetoothPan.this.mPanService == null && BluetoothPan.this.mContext != null) {
                            Log.d(BluetoothPan.TAG, "onBluetoothStateChange calling doBind()");
                            BluetoothPan.this.doBind();
                        }
                    } catch (IllegalStateException e) {
                        Log.e(BluetoothPan.TAG, "onBluetoothStateChange: could not bind to PAN service: ", e);
                    } catch (SecurityException e2) {
                        Log.e(BluetoothPan.TAG, "onBluetoothStateChange: could not bind to PAN service: ", e2);
                    }
                }
            } else {
                Log.d(BluetoothPan.TAG, "Unbinding service...");
                -get0 = BluetoothPan.this.mConnection;
                synchronized (-get0) {
                    try {
                        BluetoothPan.this.mPanService = null;
                        if (BluetoothPan.this.mContext == null) {
                            Log.d(BluetoothPan.TAG, "Context is null");
                        } else {
                            BluetoothPan.this.mContext.unbindService(BluetoothPan.this.mConnection);
                        }
                    } catch (Exception re) {
                        Log.e(BluetoothPan.TAG, "", re);
                    }
                }
            }
            return;
        }
    };

    BluetoothPan(Context context, ServiceListener l) {
        this.mContext = context;
        this.mServiceListener = l;
        this.mAdapter = BluetoothAdapter.getDefaultAdapter();
        try {
            Log.d(TAG, "Register mBluetoothStateChangeCallback = " + this.mStateChangeCallback);
            this.mAdapter.getBluetoothManager().registerStateChangeCallback(this.mStateChangeCallback);
        } catch (RemoteException re) {
            Log.w(TAG, "Unable to register BluetoothStateChangeCallback", re);
        }
        Log.d(TAG, "BluetoothPan() call bindService");
        doBind();
    }

    boolean doBind() {
        if (this.mContext == null) {
            Log.e(TAG, "Context is null");
            return false;
        }
        Intent intent = new Intent(IBluetoothPan.class.getName());
        ComponentName comp = intent.resolveSystemService(this.mContext.getPackageManager(), 0);
        intent.setComponent(comp);
        if (comp != null && this.mContext.bindServiceAsUser(intent, this.mConnection, 0, Process.myUserHandle())) {
            return true;
        }
        Log.e(TAG, "Could not bind to Bluetooth Pan Service with " + intent);
        return false;
    }

    void close() {
        log("close()");
        IBluetoothManager mgr = this.mAdapter.getBluetoothManager();
        if (mgr != null) {
            try {
                Log.d(TAG, "Unregister mBluetoothStateChangeCallback = " + this.mStateChangeCallback);
                mgr.unregisterStateChangeCallback(this.mStateChangeCallback);
            } catch (RemoteException re) {
                Log.w(TAG, "Unable to unregister BluetoothStateChangeCallback", re);
            }
        }
        synchronized (this.mConnection) {
            if (this.mPanService != null) {
                try {
                    this.mPanService = null;
                    if (this.mContext == null) {
                        Log.d(TAG, "Context is null");
                    } else {
                        this.mContext.unbindService(this.mConnection);
                    }
                } catch (Exception re2) {
                    Log.e(TAG, "", re2);
                }
            }
            this.mContext = null;
            this.mServiceListener = null;
        }
        return;
    }

    protected void finalize() {
        close();
    }

    public boolean connect(BluetoothDevice device) {
        log("connect(" + device + ")");
        if (this.mPanService != null && isEnabled() && isValidDevice(device)) {
            try {
                return this.mPanService.connect(device);
            } catch (RemoteException e) {
                Log.e(TAG, "Stack:" + Log.getStackTraceString(new Throwable()));
                return false;
            }
        }
        if (this.mPanService == null) {
            Log.w(TAG, "Proxy not attached to service");
        }
        return false;
    }

    public boolean disconnect(BluetoothDevice device) {
        log("disconnect(" + device + ")");
        if (this.mPanService != null && isEnabled() && isValidDevice(device)) {
            try {
                return this.mPanService.disconnect(device);
            } catch (RemoteException e) {
                Log.e(TAG, "Stack:" + Log.getStackTraceString(new Throwable()));
                return false;
            }
        }
        if (this.mPanService == null) {
            Log.w(TAG, "Proxy not attached to service");
        }
        return false;
    }

    public List<BluetoothDevice> getConnectedDevices() {
        log("getConnectedDevices()");
        if (!isEnabled() || this.mPanService == null) {
            if (this.mPanService == null) {
                Log.w(TAG, "Proxy not attached to service");
            }
            return new ArrayList();
        }
        try {
            return this.mPanService.getConnectedDevices();
        } catch (RemoteException e) {
            Log.e(TAG, "Stack:" + Log.getStackTraceString(new Throwable()));
            return new ArrayList();
        }
    }

    public List<BluetoothDevice> getDevicesMatchingConnectionStates(int[] states) {
        log("getDevicesMatchingStates()");
        if (this.mPanService == null || !isEnabled()) {
            if (this.mPanService == null) {
                Log.w(TAG, "Proxy not attached to service");
            }
            return new ArrayList();
        }
        try {
            return this.mPanService.getDevicesMatchingConnectionStates(states);
        } catch (RemoteException e) {
            Log.e(TAG, "Stack:" + Log.getStackTraceString(new Throwable()));
            return new ArrayList();
        }
    }

    public int getConnectionState(BluetoothDevice device) {
        log("getState(" + device + ")");
        if (this.mPanService != null && isEnabled() && isValidDevice(device)) {
            try {
                return this.mPanService.getConnectionState(device);
            } catch (RemoteException e) {
                Log.e(TAG, "Stack:" + Log.getStackTraceString(new Throwable()));
                return 0;
            }
        }
        if (this.mPanService == null) {
            Log.w(TAG, "Proxy not attached to service");
        }
        return 0;
    }

    public void setBluetoothTethering(boolean value) {
        log("setBluetoothTethering(" + value + ")");
        if (this.mPanService != null && isEnabled()) {
            try {
                this.mPanService.setBluetoothTethering(value);
            } catch (RemoteException e) {
                Log.e(TAG, "Stack:" + Log.getStackTraceString(new Throwable()));
            }
        }
    }

    public boolean isTetheringOn() {
        log("isTetheringOn()");
        if (this.mPanService != null && isEnabled()) {
            try {
                return this.mPanService.isTetheringOn();
            } catch (RemoteException e) {
                Log.e(TAG, "Stack:" + Log.getStackTraceString(new Throwable()));
            }
        }
        return false;
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
