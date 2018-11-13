package com.mediatek.location;

import android.net.ConnectivityManager;
import android.net.ConnectivityManager.NetworkCallback;
import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.net.LocalSocketAddress.Namespace;
import android.net.Network;
import android.net.NetworkRequest;
import android.net.NetworkRequest.Builder;
import android.util.Log;
import com.android.server.display.OppoBrightUtils;
import java.io.BufferedOutputStream;
import java.io.IOException;

class C2kAgpsInterface {
    private static final int EVENT_AGPS_FLIGHT_MODE = 5001;
    private static final int EVENT_AGPS_NETWORK_STATE = 5000;
    private static final int EVENT_AGPS_SET_NETWORK_ID = 4;
    private static final int NETWORK_AVAILABLE = 1;
    private static final int NETWORK_LOST = 0;
    private static final String SOCKET_ADDRESS = "c2kagpsd";
    private static final String TAG = "C2kAgpsInterface";
    private LocalSocket mClient;
    private ConnectivityManager mConnectivityManager;
    private NetworkCallback mNetworkCallback = null;
    private final NetworkRequest mNetworkRequest = new Builder().addCapability(1).build();
    private BufferedOutputStream mOut;

    C2kAgpsInterface(ConnectivityManager manager) {
        this.mConnectivityManager = manager;
    }

    void doMtkSuplConnectionCallback(int state, Network network) {
        switch (state) {
            case 0:
            case 1:
                Log.d(TAG, "[agps] SUPL_CONN: onLost: network=" + network);
                return;
            case 2:
                Log.d(TAG, "[agps] SUPL_CONN: onAvailable: network=" + network);
                setNetworkId(network.netId);
                return;
            default:
                Log.d(TAG, "[agps] SUPL_CONN: unknown state =" + state);
                return;
        }
    }

    void requestNetwork() {
        this.mNetworkCallback = new NetworkCallback() {
            public void onAvailable(Network network) {
                super.onAvailable(network);
                Log.d(C2kAgpsInterface.TAG, "[agps] WARNING: onAvailable: network=" + network);
                C2kAgpsInterface.this.setNetworkId(network.netId);
                C2kAgpsInterface.this.setNetworkState(1);
            }

            public void onLost(Network network) {
                super.onLost(network);
                Log.d(C2kAgpsInterface.TAG, "[agps] WARNING: onLost: network=" + network);
                C2kAgpsInterface.this.setNetworkState(0);
                C2kAgpsInterface.this.releaseNetwork();
            }
        };
        Log.d(TAG, "[agps] WARNING: requestNetwork");
        this.mConnectivityManager.requestNetwork(this.mNetworkRequest, this.mNetworkCallback);
    }

    void releaseNetwork() {
        Log.d(TAG, "[agps] WARNING: releaseNetwork");
        if (this.mNetworkCallback != null) {
            this.mConnectivityManager.unregisterNetworkCallback(this.mNetworkCallback);
            this.mNetworkCallback = null;
        }
    }

    void setFlightMode(boolean enabled) {
        Log.d(TAG, "[agps] WARNING: setFlightMode=" + enabled);
        int data = enabled ? 1 : 0;
        try {
            connect();
            putInt(this.mOut, EVENT_AGPS_FLIGHT_MODE);
            putInt(this.mOut, data);
            this.mOut.flush();
        } catch (IOException e) {
            Log.e(TAG, "Exception " + e);
        } finally {
            close();
        }
    }

    private void setNetworkId(int netId) {
        try {
            connect();
            putInt(this.mOut, 4);
            putInt(this.mOut, netId);
            this.mOut.flush();
        } catch (IOException e) {
            Log.e(TAG, "Exception " + e);
        } finally {
            close();
        }
    }

    private void setNetworkState(int state) {
        try {
            connect();
            putInt(this.mOut, EVENT_AGPS_NETWORK_STATE);
            putInt(this.mOut, state);
            this.mOut.flush();
        } catch (IOException e) {
            Log.e(TAG, "Exception " + e);
        } finally {
            close();
        }
    }

    private void connect() throws IOException {
        if (this.mClient != null) {
            this.mClient.close();
        }
        this.mClient = new LocalSocket();
        this.mClient.connect(new LocalSocketAddress(SOCKET_ADDRESS, Namespace.ABSTRACT));
        this.mClient.setSoTimeout(OppoBrightUtils.HIGH_BRIGHTNESS_LUX_STEP);
        this.mOut = new BufferedOutputStream(this.mClient.getOutputStream());
    }

    private void close() {
        try {
            if (this.mClient != null) {
                this.mClient.close();
                this.mClient = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void putByte(BufferedOutputStream out, byte data) throws IOException {
        out.write(data);
    }

    private static void putShort(BufferedOutputStream out, short data) throws IOException {
        putByte(out, (byte) (data & 255));
        putByte(out, (byte) ((data >> 8) & 255));
    }

    private static void putInt(BufferedOutputStream out, int data) throws IOException {
        putShort(out, (short) (data & 65535));
        putShort(out, (short) ((data >> 16) & 65535));
    }
}
