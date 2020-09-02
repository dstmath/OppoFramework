package com.android.server.display;

import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pGroup;
import android.os.UserHandle;
import android.util.Slog;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import oppo.util.OppoStatistics;

public class OppoWifiDisplayUsageHelper {
    private static final String TAG = OppoWifiDisplayUsageHelper.class.getSimpleName();
    private final String WFD_CONNECT_FAIL_BOARDCAST = "oppo.intent.state.wfdconnectfail";
    private final String WFD_CONNECT_FAIL_EXTRA = "wfd_connect_fail_extra";
    HashMap<String, Integer> callTimeMap = new HashMap<>();
    String mCallerPkgName;
    NetworkInfo.DetailedState mLastState = NetworkInfo.DetailedState.DISCONNECTED;

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: oppo.util.OppoStatistics.onCommon(android.content.Context, java.lang.String, java.lang.String, java.util.Map<java.lang.String, java.lang.String>, boolean):void
     arg types: [android.content.Context, java.lang.String, java.lang.String, java.util.HashMap<java.lang.String, java.lang.String>, int]
     candidates:
      oppo.util.OppoStatistics.onCommon(android.content.Context, java.lang.String, java.lang.String, java.util.List<java.util.Map<java.lang.String, java.lang.String>>, boolean):void
      oppo.util.OppoStatistics.onCommon(android.content.Context, java.lang.String, java.lang.String, java.util.Map<java.lang.String, java.lang.String>, boolean):void */
    public void wfdConnecteSuceess(WifiP2pGroup group, WifiP2pDevice p2pdevice, Context context) {
        HashMap<String, String> map = new HashMap<>();
        if (group != null) {
            map.put("GO", Boolean.toString(group.isGroupOwner()));
            map.put("freq", String.valueOf(group.getFrequency()));
            map.put("GO-name", group.getNetworkName());
        }
        if (!(p2pdevice == null || p2pdevice.deviceName == null)) {
            map.put("peer-name", p2pdevice.deviceName);
        }
        OppoStatistics.onCommon(context, "wifi_fool_proof", "Wfd_Connected", (Map<String, String>) map, false);
        String str = TAG;
        Slog.d(str, "wfdConnecteSuceess:" + map.toString());
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: oppo.util.OppoStatistics.onCommon(android.content.Context, java.lang.String, java.lang.String, java.util.Map<java.lang.String, java.lang.String>, boolean):void
     arg types: [android.content.Context, java.lang.String, java.lang.String, java.util.HashMap<java.lang.String, java.lang.String>, int]
     candidates:
      oppo.util.OppoStatistics.onCommon(android.content.Context, java.lang.String, java.lang.String, java.util.List<java.util.Map<java.lang.String, java.lang.String>>, boolean):void
      oppo.util.OppoStatistics.onCommon(android.content.Context, java.lang.String, java.lang.String, java.util.Map<java.lang.String, java.lang.String>, boolean):void */
    public void wfdConnectedFailed(String reason, WifiP2pDevice p2pdevice, WifiP2pGroup connectedDeviceGroupInfo, Context context) {
        HashMap<String, String> map = new HashMap<>();
        if (connectedDeviceGroupInfo != null) {
            map.put("GO", Boolean.toString(connectedDeviceGroupInfo.isGroupOwner()));
            map.put("freq", String.valueOf(connectedDeviceGroupInfo.getFrequency()));
            map.put("GO-name", connectedDeviceGroupInfo.getNetworkName());
        }
        if (!(p2pdevice == null || p2pdevice.deviceName == null)) {
            map.put("peer-name", p2pdevice.deviceName);
        }
        OppoStatistics.onCommon(context, "wifi_fool_proof", "Wfd_Fail_" + reason, (Map<String, String>) map, false);
        String str = TAG;
        Slog.d(str, "wfdConnectedFailed:" + map.toString());
        Intent intent = new Intent("oppo.intent.state.wfdconnectfail");
        intent.addFlags(67108864);
        intent.putExtra("wfd_connect_fail_extra", reason);
        context.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
    }

    public void p2pConnectState(NetworkInfo info, WifiP2pDevice connectingDevice, Boolean wfdEnabled, WifiP2pGroup connectedDeviceGroupInfo, Context context) {
        if (wfdEnabled.booleanValue()) {
            NetworkInfo.DetailedState state = info.getDetailedState();
            if ((state == NetworkInfo.DetailedState.DISCONNECTED && this.mLastState == NetworkInfo.DetailedState.DISCONNECTED) || state == NetworkInfo.DetailedState.FAILED) {
                wfdConnectedFailed("P2P_Fail_Connect", connectingDevice, connectedDeviceGroupInfo, context);
            }
            String str = TAG;
            Slog.d(str, "State=" + state + " mLastState=" + this.mLastState + " wfdEnabled=" + wfdEnabled);
        }
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: oppo.util.OppoStatistics.onCommon(android.content.Context, java.lang.String, java.lang.String, java.util.Map<java.lang.String, java.lang.String>, boolean):void
     arg types: [android.content.Context, java.lang.String, java.lang.String, java.util.HashMap<java.lang.String, java.lang.String>, int]
     candidates:
      oppo.util.OppoStatistics.onCommon(android.content.Context, java.lang.String, java.lang.String, java.util.List<java.util.Map<java.lang.String, java.lang.String>>, boolean):void
      oppo.util.OppoStatistics.onCommon(android.content.Context, java.lang.String, java.lang.String, java.util.Map<java.lang.String, java.lang.String>, boolean):void */
    public void reportWfdConnectionTime(Long connectionTime, WifiP2pDevice connectedDevice, Context context) {
        HashMap<String, String> map = new HashMap<>();
        if (!(connectedDevice == null || connectedDevice.deviceName == null)) {
            map.put("peer-name", connectedDevice.deviceName);
        }
        map.put("conntion_time", String.valueOf(connectionTime.longValue() / 6000));
        OppoStatistics.onCommon(context, "wifi_fool_proof", "Wfd_connection_time", (Map<String, String>) map, false);
        String str = TAG;
        Slog.d(str, "wfd Connection information:" + map.toString());
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: oppo.util.OppoStatistics.onCommon(android.content.Context, java.lang.String, java.lang.String, java.util.Map<java.lang.String, java.lang.String>, boolean):void
     arg types: [android.content.Context, java.lang.String, java.lang.String, java.util.HashMap<java.lang.String, java.lang.String>, int]
     candidates:
      oppo.util.OppoStatistics.onCommon(android.content.Context, java.lang.String, java.lang.String, java.util.List<java.util.Map<java.lang.String, java.lang.String>>, boolean):void
      oppo.util.OppoStatistics.onCommon(android.content.Context, java.lang.String, java.lang.String, java.util.Map<java.lang.String, java.lang.String>, boolean):void */
    public void reportP2pConnectionCallerName(String pkgName, String calledTime, Context context) {
        int callTimes;
        HashMap<String, String> map = new HashMap<>();
        if (pkgName != null) {
            this.mCallerPkgName = pkgName;
            map.put("caller-name", pkgName);
            if (this.callTimeMap.containsKey(pkgName)) {
                int callTimes2 = this.callTimeMap.get(pkgName).intValue() + 1;
                this.callTimeMap.put(pkgName, Integer.valueOf(callTimes2));
                callTimes = callTimes2;
            } else {
                callTimes = 1;
                this.callTimeMap.put(pkgName, 1);
            }
            map.put("called-times", String.valueOf(callTimes));
            map.put("current-time", String.valueOf(calledTime));
            OppoStatistics.onCommon(context, "wifi_fool_proof", "p2p_caller_name", (Map<String, String>) map, false);
            String str = TAG;
            Slog.d(str, "p2p Connection is setting up by: " + pkgName + " for " + String.valueOf(callTimes) + " times at " + getFormatTime());
            return;
        }
        Slog.e(TAG, "ingore the null caller pkgname which should not happen");
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: oppo.util.OppoStatistics.onCommon(android.content.Context, java.lang.String, java.lang.String, java.util.Map<java.lang.String, java.lang.String>, boolean):void
     arg types: [android.content.Context, java.lang.String, java.lang.String, java.util.HashMap<java.lang.String, java.lang.String>, int]
     candidates:
      oppo.util.OppoStatistics.onCommon(android.content.Context, java.lang.String, java.lang.String, java.util.List<java.util.Map<java.lang.String, java.lang.String>>, boolean):void
      oppo.util.OppoStatistics.onCommon(android.content.Context, java.lang.String, java.lang.String, java.util.Map<java.lang.String, java.lang.String>, boolean):void */
    public void reportP2pConnectionTime(Long connectionTime, Context context) {
        HashMap<String, String> map = new HashMap<>();
        String str = this.mCallerPkgName;
        if (str != null) {
            map.put("peer-name", str);
            this.mCallerPkgName = null;
        }
        map.put("conntion_time", String.valueOf(connectionTime.longValue() / 6000));
        OppoStatistics.onCommon(context, "wifi_fool_proof", "P2p_connection_time", (Map<String, String>) map, false);
        String str2 = TAG;
        Slog.d(str2, "p2p Connection with:" + map.toString());
    }

    public String getFormatTime() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }
}
