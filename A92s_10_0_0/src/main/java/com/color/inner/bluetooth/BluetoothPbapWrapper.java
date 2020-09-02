package com.color.inner.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothPbap;
import android.content.Context;

public class BluetoothPbapWrapper {
    private static final String TAG = "BluetoothPbapWrapper";
    private Context mContext = null;
    /* access modifiers changed from: private */
    public BluetoothPbap mService;
    /* access modifiers changed from: private */
    public ServiceListener mServiceListener = null;

    public interface ServiceListener {
        void onServiceConnected(BluetoothPbapWrapper bluetoothPbapWrapper);

        void onServiceDisconnected();
    }

    public BluetoothPbapWrapper(Context mContext2, ServiceListener mServiceListener2) {
        this.mContext = mContext2;
        this.mServiceListener = mServiceListener2;
        this.mService = new BluetoothPbap(mContext2, new PbapServiceListener());
    }

    public void close() {
        this.mService.close();
    }

    private final class PbapServiceListener implements BluetoothPbap.ServiceListener {
        private PbapServiceListener() {
        }

        public void onServiceConnected(BluetoothPbap proxy) {
            BluetoothPbap unused = BluetoothPbapWrapper.this.mService = proxy;
            if (BluetoothPbapWrapper.this.mServiceListener != null) {
                BluetoothPbapWrapper.this.mServiceListener.onServiceConnected(BluetoothPbapWrapper.this);
            }
        }

        public void onServiceDisconnected() {
            if (BluetoothPbapWrapper.this.mServiceListener != null) {
                BluetoothPbapWrapper.this.mServiceListener.onServiceDisconnected();
            }
        }
    }

    public boolean disconnect(BluetoothDevice device) {
        BluetoothPbap bluetoothPbap = this.mService;
        if (bluetoothPbap != null) {
            return bluetoothPbap.disconnect(device);
        }
        return false;
    }

    public boolean isConnected(BluetoothDevice device) {
        BluetoothPbap bluetoothPbap = this.mService;
        if (bluetoothPbap != null) {
            return bluetoothPbap.isConnected(device);
        }
        return false;
    }
}
