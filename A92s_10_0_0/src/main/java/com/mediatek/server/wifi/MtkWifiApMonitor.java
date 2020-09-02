package com.mediatek.server.wifi;

import android.os.Handler;
import android.os.Message;
import android.util.ArraySet;
import android.util.Log;
import android.util.SparseArray;
import com.android.internal.annotations.VisibleForTesting;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MtkWifiApMonitor {
    public static final int AP_STA_CONNECTED_EVENT = 147498;
    public static final int AP_STA_DISCONNECTED_EVENT = 147497;
    private static final int BASE = 147456;
    public static final int SUP_CONNECTION_EVENT = 147457;
    public static final int SUP_DISCONNECTION_EVENT = 147458;
    private static final String TAG = "MtkWifiApMonitor";
    private static final Map<String, SparseArray<Set<Handler>>> sHandlerMap = new HashMap();
    private static final Map<String, Boolean> sMonitoringMap = new HashMap();

    public static synchronized void registerHandler(String iface, int what, Handler handler) {
        synchronized (MtkWifiApMonitor.class) {
            SparseArray<Set<Handler>> ifaceHandlers = sHandlerMap.get(iface);
            if (ifaceHandlers == null) {
                ifaceHandlers = new SparseArray<>();
                sHandlerMap.put(iface, ifaceHandlers);
            }
            Set<Handler> ifaceWhatHandlers = ifaceHandlers.get(what);
            if (ifaceWhatHandlers == null) {
                ifaceWhatHandlers = new ArraySet();
                ifaceHandlers.put(what, ifaceWhatHandlers);
            }
            ifaceWhatHandlers.add(handler);
        }
    }

    public static synchronized void deregisterAllHandler() {
        synchronized (MtkWifiApMonitor.class) {
            sHandlerMap.clear();
        }
    }

    private static boolean isMonitoring(String iface) {
        Boolean val = sMonitoringMap.get(iface);
        if (val == null) {
            return false;
        }
        return val.booleanValue();
    }

    @VisibleForTesting
    public static void setMonitoring(String iface, boolean enabled) {
        sMonitoringMap.put(iface, Boolean.valueOf(enabled));
    }

    private static void setMonitoringNone() {
        for (String iface : sMonitoringMap.keySet()) {
            setMonitoring(iface, false);
        }
    }

    public static synchronized void startMonitoring(String iface) {
        synchronized (MtkWifiApMonitor.class) {
            Log.d(TAG, "startMonitoring(" + iface + ")");
            setMonitoring(iface, true);
            broadcastSupplicantConnectionEvent(iface);
        }
    }

    public static synchronized void stopMonitoring(String iface) {
        synchronized (MtkWifiApMonitor.class) {
            Log.d(TAG, "stopMonitoring(" + iface + ")");
            setMonitoring(iface, true);
            broadcastSupplicantDisconnectionEvent(iface);
            setMonitoring(iface, false);
        }
    }

    public static synchronized void stopAllMonitoring() {
        synchronized (MtkWifiApMonitor.class) {
            setMonitoringNone();
        }
    }

    private static void sendMessage(String iface, int what) {
        sendMessage(iface, Message.obtain((Handler) null, what));
    }

    private static void sendMessage(String iface, int what, Object obj) {
        sendMessage(iface, Message.obtain(null, what, obj));
    }

    private static void sendMessage(String iface, int what, int arg1) {
        sendMessage(iface, Message.obtain(null, what, arg1, 0));
    }

    private static void sendMessage(String iface, int what, int arg1, int arg2) {
        sendMessage(iface, Message.obtain(null, what, arg1, arg2));
    }

    private static void sendMessage(String iface, int what, int arg1, int arg2, Object obj) {
        sendMessage(iface, Message.obtain(null, what, arg1, arg2, obj));
    }

    private static void sendMessage(String iface, Message message) {
        SparseArray<Set<Handler>> ifaceHandlers = sHandlerMap.get(iface);
        if (iface == null || ifaceHandlers == null) {
            Log.d(TAG, "Sending to all monitors because there's no matching iface");
            for (Map.Entry<String, SparseArray<Set<Handler>>> entry : sHandlerMap.entrySet()) {
                if (isMonitoring(entry.getKey())) {
                    for (Handler handler : entry.getValue().get(message.what)) {
                        if (handler != null) {
                            sendMessage(handler, Message.obtain(message));
                        }
                    }
                }
            }
        } else if (isMonitoring(iface)) {
            Set<Handler> ifaceWhatHandlers = ifaceHandlers.get(message.what);
            if (ifaceWhatHandlers != null) {
                for (Handler handler2 : ifaceWhatHandlers) {
                    if (handler2 != null) {
                        sendMessage(handler2, Message.obtain(message));
                    }
                }
            }
        } else {
            Log.d(TAG, "Dropping event because (" + iface + ") is stopped");
        }
        message.recycle();
    }

    private static void sendMessage(Handler handler, Message message) {
        message.setTarget(handler);
        message.sendToTarget();
    }

    public static void broadcastSupplicantConnectionEvent(String iface) {
        sendMessage(iface, 147457);
    }

    public static void broadcastSupplicantDisconnectionEvent(String iface) {
        sendMessage(iface, 147458);
    }

    public static void broadcastApStaConnected(String iface, String macAddress) {
        sendMessage(iface, 147498, macAddress);
    }

    public static void broadcastApStaDisconnected(String iface, String macAddress) {
        sendMessage(iface, 147497, macAddress);
    }
}
