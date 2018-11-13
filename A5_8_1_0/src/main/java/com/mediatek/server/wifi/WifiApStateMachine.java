package com.mediatek.server.wifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WpsInfo;
import android.os.Message;
import android.util.Log;
import com.android.internal.util.AsyncChannel;
import com.android.server.wifi.SoftApManager;
import com.android.server.wifi.WifiStateMachine;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import mediatek.net.wifi.HotspotClient;

public class WifiApStateMachine {
    private static final String ACTION_LOAD_FROM_STORE = "android.intent.action.OPPO_ACTION_LOAD_FROM_STORE";
    static final int BASE = 131072;
    private static final int FAILURE = -1;
    public static final int M_CMD_ALLOW_DEVICE = 131378;
    public static final int M_CMD_BLOCK_CLIENT = 131372;
    public static final int M_CMD_DISALLOW_DEVICE = 131379;
    public static final int M_CMD_GET_ALLOWED_DEVICES = 131380;
    public static final int M_CMD_GET_BLOCKED_CLIENTS_LIST = 131381;
    public static final int M_CMD_GET_CLIENTS_LIST = 131374;
    public static final int M_CMD_IS_ALL_DEVICES_ALLOWED = 131376;
    public static final int M_CMD_SET_ALL_DEVICES_ALLOWED = 131377;
    public static final int M_CMD_START_AP_WPS = 131375;
    public static final int M_CMD_UNBLOCK_CLIENT = 131373;
    private static final int SUCCESS = 1;
    private static final String TAG = "WifiApStateMachine";
    private static WifiStateMachine mWifiStateMachine;
    private final Context mContext;

    public WifiApStateMachine(WifiStateMachine wifiStateMachine, Context context) {
        mWifiStateMachine = wifiStateMachine;
        this.mContext = context;
        OppoSoftApManager.loadDeniedDevice();
        this.mContext.registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                Log.d(WifiApStateMachine.TAG, "onReceive: LFS");
                OppoSoftApManager.loadDeniedDevice();
            }
        }, new IntentFilter(ACTION_LOAD_FROM_STORE));
    }

    public static boolean processDefaultStateMessage(Message message, Context context) {
        try {
            Method replyToMessage1 = mWifiStateMachine.getClass().getDeclaredMethod("replyToMessage", new Class[]{Message.class, Integer.TYPE});
            replyToMessage1.setAccessible(true);
            Method replyToMessage2 = mWifiStateMachine.getClass().getDeclaredMethod("replyToMessage", new Class[]{Message.class, Integer.TYPE, Integer.TYPE});
            replyToMessage2.setAccessible(true);
            Method replyToMessage3 = mWifiStateMachine.getClass().getDeclaredMethod("replyToMessage", new Class[]{Message.class, Integer.TYPE, Object.class});
            replyToMessage3.setAccessible(true);
            List<HotspotClient> clients;
            switch (message.what) {
                case 131372:
                case 131375:
                    replyToMessage2.invoke(mWifiStateMachine, new Object[]{message, Integer.valueOf(message.what), Integer.valueOf(-1)});
                    return true;
                case 131373:
                    Log.e(TAG, "M_CMD_UNBLOCK_CLIENT!");
                    Message newUnblockMsg = mWifiStateMachine.obtainMessage();
                    newUnblockMsg.copyFrom(message);
                    OppoSoftApManager.syncUnblockClient(newUnblockMsg);
                    replyToMessage1.invoke(mWifiStateMachine, new Object[]{message, Integer.valueOf(message.what)});
                    return true;
                case 131374:
                    clients = new ArrayList();
                    replyToMessage3.invoke(mWifiStateMachine, new Object[]{message, Integer.valueOf(message.what), clients});
                    return true;
                case 131376:
                    boolean resultValue = OppoSoftApManager.isAllDevicesAllowed(context);
                    WifiStateMachine wifiStateMachine = mWifiStateMachine;
                    Object[] objArr = new Object[3];
                    objArr[0] = message;
                    objArr[1] = Integer.valueOf(message.what);
                    objArr[2] = Integer.valueOf(resultValue ? 1 : 0);
                    replyToMessage2.invoke(wifiStateMachine, objArr);
                    return true;
                case 131377:
                    OppoSoftApManager.writeAllDevicesAllowed(context, message.arg1 == 1);
                    replyToMessage2.invoke(mWifiStateMachine, new Object[]{message, Integer.valueOf(message.what), Integer.valueOf(1)});
                    return true;
                case 131378:
                    OppoSoftApManager.addDeviceToAllowedList((HotspotClient) message.obj);
                    replyToMessage1.invoke(mWifiStateMachine, new Object[]{message, Integer.valueOf(message.what)});
                    return true;
                case 131379:
                    OppoSoftApManager.removeDeviceFromAllowedList((String) message.obj);
                    replyToMessage1.invoke(mWifiStateMachine, new Object[]{message, Integer.valueOf(message.what)});
                    return true;
                case 131380:
                    List<HotspotClient> clientList = OppoSoftApManager.getAllowedDevices();
                    replyToMessage3.invoke(mWifiStateMachine, new Object[]{message, Integer.valueOf(message.what), clientList});
                    return true;
                case 131381:
                    clients = OppoSoftApManager.getBlockedHotspotClientsList();
                    replyToMessage3.invoke(mWifiStateMachine, new Object[]{message, Integer.valueOf(message.what), clients});
                    return true;
                default:
                    return false;
            }
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean processSoftApStateMessage(Message message, Context context, SoftApManager softApManager) {
        try {
            OppoSoftApManager mOppoSoftApManager = (OppoSoftApManager) softApManager;
            Method replyToMessage1 = mWifiStateMachine.getClass().getDeclaredMethod("replyToMessage", new Class[]{Message.class, Integer.TYPE});
            replyToMessage1.setAccessible(true);
            mWifiStateMachine.getClass().getDeclaredMethod("replyToMessage", new Class[]{Message.class, Integer.TYPE, Integer.TYPE}).setAccessible(true);
            Method replyToMessage3 = mWifiStateMachine.getClass().getDeclaredMethod("replyToMessage", new Class[]{Message.class, Integer.TYPE, Object.class});
            replyToMessage3.setAccessible(true);
            List<HotspotClient> clientList;
            switch (message.what) {
                case 131372:
                    Message newBlockMsg = mWifiStateMachine.obtainMessage();
                    newBlockMsg.copyFrom(message);
                    mOppoSoftApManager.syncBlockClient(newBlockMsg);
                    return true;
                case 131373:
                    Message newUnblockMsg = mWifiStateMachine.obtainMessage();
                    newUnblockMsg.copyFrom(message);
                    OppoSoftApManager.syncUnblockClient(newUnblockMsg);
                    return true;
                case 131374:
                    clientList = mOppoSoftApManager.getHotspotClientsList();
                    replyToMessage3.invoke(mWifiStateMachine, new Object[]{message, Integer.valueOf(message.what), clientList});
                    return true;
                case 131375:
                    Message newWpsMsg = mWifiStateMachine.obtainMessage();
                    newWpsMsg.copyFrom(message);
                    mOppoSoftApManager.startApWpsCommand(newWpsMsg);
                    return true;
                case 131377:
                    boolean enabled = message.arg1 == 1;
                    boolean allowAllConnectedDevices = message.arg2 == 1;
                    OppoSoftApManager.writeAllDevicesAllowed(context, enabled);
                    mOppoSoftApManager.syncSetAllDevicesAllowed(enabled, allowAllConnectedDevices);
                    replyToMessage3.invoke(mWifiStateMachine, new Object[]{message, Integer.valueOf(message.what), Boolean.valueOf(true)});
                    return true;
                case 131378:
                    HotspotClient device = message.obj;
                    OppoSoftApManager.addDeviceToAllowedList(device);
                    mOppoSoftApManager.syncAllowDevice(device.deviceAddress);
                    replyToMessage1.invoke(mWifiStateMachine, new Object[]{message, Integer.valueOf(message.what)});
                    return true;
                case 131379:
                    String address = message.obj;
                    OppoSoftApManager.removeDeviceFromAllowedList(address);
                    mOppoSoftApManager.syncDisallowDevice(address);
                    replyToMessage1.invoke(mWifiStateMachine, new Object[]{message, Integer.valueOf(message.what)});
                    return true;
                case 131381:
                    clientList = OppoSoftApManager.getBlockedHotspotClientsList();
                    replyToMessage3.invoke(mWifiStateMachine, new Object[]{message, Integer.valueOf(message.what), clientList});
                    return true;
                default:
                    return false;
            }
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static String smToString(int what) {
        switch (what) {
            case 131372:
                return "M_CMD_BLOCK_CLIENT";
            case 131373:
                return "M_CMD_UNBLOCK_CLIENT";
            case 131374:
                return "M_CMD_GET_CLIENTS_LIST";
            case 131375:
                return "M_CMD_START_AP_WPS";
            case 131376:
                return "M_CMD_IS_ALL_DEVICES_ALLOWED";
            case 131377:
                return "M_CMD_SET_ALL_DEVICES_ALLOWED";
            case 131378:
                return "M_CMD_ALLOW_DEVICE";
            case 131379:
                return "M_CMD_DISALLOW_DEVICE";
            case 131380:
                return "M_CMD_GET_ALLOWED_DEVICES";
            case 131381:
                return "M_CMD_GET_BLOCKED_CLIENTS_LIST";
            default:
                return null;
        }
    }

    public void startApWpsCommand(WpsInfo config) {
        mWifiStateMachine.sendMessage(mWifiStateMachine.obtainMessage(131375, config));
    }

    public List<HotspotClient> syncGetHotspotClientsList(AsyncChannel channel) {
        Message resultMsg = channel.sendMessageSynchronously(131374);
        List<HotspotClient> result = resultMsg.obj;
        resultMsg.recycle();
        return result;
    }

    public List<HotspotClient> syncGetBlockedHotspotClientsList(AsyncChannel channel) {
        Message resultMsg = channel.sendMessageSynchronously(131381);
        List<HotspotClient> result = resultMsg.obj;
        resultMsg.recycle();
        return result;
    }

    public boolean syncBlockClient(AsyncChannel channel, HotspotClient client) {
        if (client == null || client.deviceAddress == null) {
            Log.e(TAG, "Client is null!");
            return false;
        }
        Message resultMsg = channel.sendMessageSynchronously(131372, client);
        boolean result = resultMsg.arg1 != -1;
        resultMsg.recycle();
        return result;
    }

    public boolean syncUnblockClient(AsyncChannel channel, HotspotClient client) {
        if (client == null || client.deviceAddress == null) {
            Log.e(TAG, "Client is null!");
            return false;
        }
        Message resultMsg = channel.sendMessageSynchronously(131373, client);
        boolean result = resultMsg.arg1 != -1;
        resultMsg.recycle();
        return result;
    }

    public boolean syncIsAllDevicesAllowed(AsyncChannel channel) {
        Message resultMsg = channel.sendMessageSynchronously(131376);
        boolean result = resultMsg.arg1 == 1;
        resultMsg.recycle();
        return result;
    }

    public boolean syncSetAllDevicesAllowed(AsyncChannel channel, boolean enabled, boolean allowAllConnectedDevices) {
        int i;
        int i2 = 1;
        if (enabled) {
            i = 1;
        } else {
            i = 0;
        }
        if (!allowAllConnectedDevices) {
            i2 = 0;
        }
        Message resultMsg = channel.sendMessageSynchronously(131377, i, i2);
        boolean result = resultMsg.arg1 != -1;
        resultMsg.recycle();
        return result;
    }

    public boolean syncAllowDevice(AsyncChannel channel, String deviceAddress, String name) {
        if (deviceAddress == null) {
            Log.e(TAG, "deviceAddress is null!");
            return false;
        }
        Message resultMsg = channel.sendMessageSynchronously(131378, new HotspotClient(deviceAddress, false, name));
        boolean result = resultMsg.arg1 != -1;
        resultMsg.recycle();
        return result;
    }

    public boolean syncDisallowDevice(AsyncChannel channel, String deviceAddress) {
        if (deviceAddress == null) {
            Log.e(TAG, "deviceAddress is null!");
            return false;
        }
        Message resultMsg = channel.sendMessageSynchronously(131379, deviceAddress);
        boolean result = resultMsg.arg1 != -1;
        resultMsg.recycle();
        return result;
    }

    public List<HotspotClient> syncGetAllowedDevices(AsyncChannel channel) {
        Message resultMsg = channel.sendMessageSynchronously(131380);
        List<HotspotClient> result = resultMsg.obj;
        resultMsg.recycle();
        return result;
    }
}
