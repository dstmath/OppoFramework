package com.android.server.connectivity.gatewayconflict;

import android.content.Context;
import android.net.wifi.WifiRomUpdateHelper;
import android.util.Log;
import com.android.server.connectivity.NetworkAgentInfo;
import com.android.server.connectivity.gatewayconflict.OppoArpPeer;

public class OppoGatewayState {
    private static final String TAG = "OppoGatewayState";
    private Context mContext;
    private OppoGatewayDetector mGatewayDetector = null;
    private GatewayDetectorState mGatewayDetectorState = GatewayDetectorState.INIT;
    private boolean mIsWifiNetwork = false;
    private NetworkAgentInfo mNai;
    private WifiRomUpdateHelper mWifiRomUpdateHelper;

    /* access modifiers changed from: private */
    public enum GatewayDetectorState {
        INIT,
        SUSPEND,
        PROBE,
        REEVALUATE,
        DONE
    }

    public OppoGatewayState(Context context, NetworkAgentInfo nai) {
        this.mContext = context;
        this.mNai = nai;
        this.mIsWifiNetwork = isWifiNetwork(nai);
        this.mWifiRomUpdateHelper = WifiRomUpdateHelper.getInstance(this.mContext);
    }

    public void stopGatewayDetector() {
        this.mGatewayDetectorState = GatewayDetectorState.INIT;
        OppoGatewayDetector oppoGatewayDetector = this.mGatewayDetector;
        if (oppoGatewayDetector != null) {
            oppoGatewayDetector.close();
            this.mGatewayDetector = null;
        }
    }

    private boolean isWifiNetwork(NetworkAgentInfo nai) {
        if (nai == null || nai.networkInfo == null || nai.networkInfo.getType() != 1) {
            return false;
        }
        return true;
    }

    public boolean needWaitGatewayDetector() {
        return GatewayDetectorState.SUSPEND == this.mGatewayDetectorState;
    }

    public boolean needWaitReevaluateNetwork() {
        return GatewayDetectorState.REEVALUATE == this.mGatewayDetectorState;
    }

    public void startGatewayDetector(OppoArpPeer.ArpPeerChangeCallback callback) {
        if (this.mIsWifiNetwork) {
            WifiRomUpdateHelper wifiRomUpdateHelper = this.mWifiRomUpdateHelper;
            if (wifiRomUpdateHelper != null && !wifiRomUpdateHelper.getBooleanValue("NETWORK_HANDLE_GATEWAY_CONFLICT", false)) {
                log("romupdate disable GatewayDetector!");
            } else if (GatewayDetectorState.INIT != this.mGatewayDetectorState) {
                log("GatewayDetectorState isn't init state. cur state is" + this.mGatewayDetectorState);
            } else {
                if (this.mGatewayDetector == null) {
                    this.mGatewayDetector = new OppoGatewayDetector(this.mContext, this.mNai, callback);
                }
                if (this.mGatewayDetector.probeGateway()) {
                    byte[] currentMac = this.mGatewayDetector.fetchGatewayMacFromRoute();
                    if (currentMac != null) {
                        startGatewayProbe();
                    } else if (currentMac == null && this.mGatewayDetectorState == GatewayDetectorState.INIT) {
                        this.mGatewayDetectorState = GatewayDetectorState.SUSPEND;
                    }
                }
            }
        }
    }

    public void setDuplicateGatewayStatics() {
        OppoGatewayDetector oppoGatewayDetector = this.mGatewayDetector;
        if (oppoGatewayDetector != null && oppoGatewayDetector.hasDupGateway()) {
            this.mGatewayDetector.setDuplicateGatewayStatics();
        }
    }

    public boolean needReevaluateNetwork() {
        OppoGatewayDetector oppoGatewayDetector;
        return GatewayDetectorState.PROBE == this.mGatewayDetectorState && (oppoGatewayDetector = this.mGatewayDetector) != null && oppoGatewayDetector.hasDupGateway() && this.mGatewayDetector.hasLeftAvaibleGateway();
    }

    public void reevaluateNetwork() {
        log("reevaluateNetwork  mGatewayDetectorState=" + this.mGatewayDetectorState);
        this.mGatewayDetectorState = GatewayDetectorState.REEVALUATE;
        this.mGatewayDetector.prepareNextAvailbeGateway();
    }

    public void startGatewayProbe() {
        log("startGatewayProbe  mGatewayDetectorState=" + this.mGatewayDetectorState);
        if (this.mGatewayDetectorState == GatewayDetectorState.INIT || this.mGatewayDetectorState == GatewayDetectorState.SUSPEND) {
            this.mGatewayDetectorState = GatewayDetectorState.PROBE;
            this.mGatewayDetector.prepareNextAvailbeGateway();
        }
    }

    public void restoreLastGatewayState() {
        OppoGatewayDetector oppoGatewayDetector;
        if (GatewayDetectorState.REEVALUATE == this.mGatewayDetectorState && (oppoGatewayDetector = this.mGatewayDetector) != null && oppoGatewayDetector.hasDupGateway()) {
            this.mGatewayDetectorState = GatewayDetectorState.DONE;
            this.mGatewayDetector.restoreLastGatewayState();
        }
    }

    public void setGatewayStateDone() {
        this.mGatewayDetectorState = GatewayDetectorState.DONE;
    }

    public boolean isGatewayStateDone() {
        return this.mGatewayDetectorState == GatewayDetectorState.DONE;
    }

    private static void log(String s) {
        Log.d(TAG, s);
    }

    private static void loge(String s) {
        Log.e(TAG, s);
    }
}
