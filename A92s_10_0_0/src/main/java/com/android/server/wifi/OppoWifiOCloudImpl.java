package com.android.server.wifi;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiConfiguration;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import com.android.server.wifi.WifiConfigManager;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import org.json.JSONException;
import org.json.JSONObject;

public class OppoWifiOCloudImpl {
    private static final String ACTION_DATA_CHANGE = "com.heytap.cloud.action.DATA_CHANGED";
    private static final String COLOROS_CLOUD_PACKAGE_NAME = "com.heytap.cloud";
    private static boolean DBG = true;
    private static final String EXTRA_APP = "DATA";
    private static final String EXTRA_RECOVER_FLAG = "NEED_RECOVERY";
    private static final int MSG_LOAD_FROM_STORE = 6;
    private static final int MSG_NETWORK_DELETED = 2;
    private static final int MSG_NETWORK_DISABLED = 5;
    private static final int MSG_NETWORK_ENABLED = 4;
    private static final int MSG_NETWORK_NEW_ADDED = 1;
    private static final int MSG_NETWORK_UPDATED = 3;
    private static final String PERMISSION_DATA_CHANGE = "heytap.permission.cloud.ACCESS_SYNC_SERVICE";
    private static final String SETTINGS_WIFI_HEYTAP_ENABLE = "global.wifi.heytap.enabled";
    private static final String SETTINGS_WIFI_HEYTAP_SUPPORT = "global.wifi.heytap.support";
    private static final String TAG = "OppoWifiOCloudImpl";
    /* access modifiers changed from: private */
    public WifiConfigManager mConfigManager;
    private Context mContext;
    private ArrayList<String> mDuplicateRecord = new ArrayList<>();
    /* access modifiers changed from: private */
    public InternalHandler mHandler = new InternalHandler();
    private NetworkChangedListener mListener = new NetworkChangedListener();
    /* access modifiers changed from: private */
    public ConcurrentHashMap<String, SimpleWifiConfig> mNetworks = new ConcurrentHashMap<>();

    OppoWifiOCloudImpl(Context context, WifiConfigManager wifiConfigManager) {
        this.mContext = context;
        this.mConfigManager = wifiConfigManager;
        this.mConfigManager.setOnSavedNetworkUpdateListener(this.mListener);
        Settings.Global.putInt(this.mContext.getContentResolver(), SETTINGS_WIFI_HEYTAP_SUPPORT, 1);
    }

    public class NetworkChangedListener implements WifiConfigManager.OnSavedNetworkUpdateListener {
        public NetworkChangedListener() {
        }

        @Override // com.android.server.wifi.WifiConfigManager.OnSavedNetworkUpdateListener
        public void onSavedNetworkAdded(int networkId) {
            OppoWifiOCloudImpl.this.mHandler.sendMessage(OppoWifiOCloudImpl.this.mHandler.obtainMessage(1, networkId, 0));
        }

        @Override // com.android.server.wifi.WifiConfigManager.OnSavedNetworkUpdateListener
        public void onSavedNetworkEnabled(int networkId) {
            OppoWifiOCloudImpl.this.mHandler.sendMessage(OppoWifiOCloudImpl.this.mHandler.obtainMessage(4, networkId, 0));
        }

        @Override // com.android.server.wifi.WifiConfigManager.OnSavedNetworkUpdateListener
        public void onSavedNetworkRemoved(int networkId) {
            OppoWifiOCloudImpl.this.mHandler.sendMessage(OppoWifiOCloudImpl.this.mHandler.obtainMessage(2, networkId, 0));
        }

        @Override // com.android.server.wifi.WifiConfigManager.OnSavedNetworkUpdateListener
        public void onSavedNetworkUpdated(int networkId) {
            OppoWifiOCloudImpl.this.mHandler.sendMessage(OppoWifiOCloudImpl.this.mHandler.obtainMessage(3, networkId, 0));
        }

        @Override // com.android.server.wifi.WifiConfigManager.OnSavedNetworkUpdateListener
        public void onSavedNetworkTemporarilyDisabled(int networkId, int disableReason) {
            OppoWifiOCloudImpl.this.mHandler.sendMessage(OppoWifiOCloudImpl.this.mHandler.obtainMessage(5, networkId, 0));
        }

        @Override // com.android.server.wifi.WifiConfigManager.OnSavedNetworkUpdateListener
        public void onSavedNetworkPermanentlyDisabled(int networkId, int disableReason) {
            OppoWifiOCloudImpl.this.mHandler.sendMessage(OppoWifiOCloudImpl.this.mHandler.obtainMessage(5, networkId, 0));
        }
    }

    /* access modifiers changed from: protected */
    public void setDirtyFlag(String globalId, boolean value) {
        synchronized (this.mNetworks) {
            for (SimpleWifiConfig swc : this.mNetworks.values()) {
                if (swc.mGlobalId.equals(globalId)) {
                    if (DBG) {
                        Log.d(TAG, "setting dirty flag to " + value + " for " + swc.mConfigKey);
                    }
                    swc.mDirty = value;
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void setDirtyFlag(String configKey, boolean dirty, boolean override) {
        synchronized (this.mNetworks) {
            SimpleWifiConfig swc = this.mNetworks.get(configKey);
            if (swc == null) {
                Log.e(TAG, "not network found for cfgKey: " + configKey);
            } else if (dirty) {
                swc.mGlobalId = "NULL";
                swc.mAction = SimpleWifiConfig.ADD;
                swc.mDirty = true;
            } else {
                swc.mDirty = dirty;
            }
        }
    }

    /* access modifiers changed from: protected */
    public boolean hasOCloudDirtyData() {
        boolean result = false;
        synchronized (this.mNetworks) {
            Iterator<SimpleWifiConfig> it = this.mNetworks.values().iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                } else if (it.next().mDirty) {
                    result = true;
                    break;
                }
            }
        }
        return result;
    }

    /* access modifiers changed from: protected */
    public void notifyLoadFromStore() {
        InternalHandler internalHandler = this.mHandler;
        internalHandler.sendMessage(internalHandler.obtainMessage(6));
    }

    /* access modifiers changed from: protected */
    public void backupWhileLoadFromStore() {
        if (DBG) {
            Log.d(TAG, "enter backupWhileLoadFromStore");
        }
        List<WifiConfiguration> allNetworks = this.mConfigManager.getConfiguredNetworksWithPasswords();
        if (allNetworks == null) {
            Log.e(TAG, "failed to get networks.");
            return;
        }
        for (WifiConfiguration config : allNetworks) {
            this.mNetworks.put(config.configKey(), new SimpleWifiConfig(config.configKey(), config.preSharedKey, config.networkId, config.globalId));
        }
    }

    /* access modifiers changed from: private */
    public class InternalHandler extends Handler {
        private InternalHandler() {
        }

        public void handleMessage(Message msg) {
            boolean needSync = true;
            int networkId = msg.arg1;
            switch (msg.what) {
                case 1:
                    Log.d(OppoWifiOCloudImpl.TAG, "new network added");
                    WifiConfiguration newWifiConfig = OppoWifiOCloudImpl.this.mConfigManager.getConfiguredNetworkWithPassword(networkId);
                    if (newWifiConfig == null) {
                        Log.e(OppoWifiOCloudImpl.TAG, "newWifiConfig is null");
                        needSync = false;
                        break;
                    } else {
                        SimpleWifiConfig swc = null;
                        if (OppoWifiOCloudImpl.this.mNetworks != null) {
                            SimpleWifiConfig swc2 = (SimpleWifiConfig) OppoWifiOCloudImpl.this.mNetworks.get(newWifiConfig.configKey());
                            if (swc2 != null) {
                                Log.e(OppoWifiOCloudImpl.TAG, "network already in sync list");
                                if (!"NULL".equals(newWifiConfig.globalId)) {
                                    swc2.mGlobalId = newWifiConfig.globalId;
                                    swc2.mDirty = false;
                                }
                                if (swc2.mNetworkId != networkId) {
                                    swc2.mNetworkId = networkId;
                                    break;
                                }
                            } else {
                                swc = new SimpleWifiConfig(newWifiConfig.configKey(), newWifiConfig.preSharedKey, networkId, newWifiConfig.globalId);
                                OppoWifiOCloudImpl.this.mNetworks.put(newWifiConfig.configKey(), swc);
                            }
                        }
                        if (!swc.isDirty()) {
                            needSync = false;
                            break;
                        }
                    }
                    break;
                case 2:
                    Log.d(OppoWifiOCloudImpl.TAG, "network deleted");
                    SimpleWifiConfig simpleConfig = OppoWifiOCloudImpl.this.getInternalDeletedNetwork(networkId);
                    if (simpleConfig != null) {
                        if ("NULL".equals(simpleConfig.mGlobalId)) {
                            synchronized (OppoWifiOCloudImpl.this.mNetworks) {
                                OppoWifiOCloudImpl.this.mNetworks.remove(simpleConfig.mConfigKey);
                            }
                            needSync = false;
                            Log.d(OppoWifiOCloudImpl.TAG, "found a network but gid is null");
                            break;
                        } else {
                            simpleConfig.mAction = SimpleWifiConfig.DELETE;
                            simpleConfig.mDirty = true;
                            break;
                        }
                    } else {
                        Log.e(OppoWifiOCloudImpl.TAG, "cannot find network...");
                        needSync = false;
                        break;
                    }
                case 3:
                    Log.d(OppoWifiOCloudImpl.TAG, "network updated");
                    WifiConfiguration wifiConfig = OppoWifiOCloudImpl.this.mConfigManager.getConfiguredNetworkWithPassword(networkId);
                    if (wifiConfig == null) {
                        Log.e(OppoWifiOCloudImpl.TAG, "wifiConfig is null");
                        needSync = false;
                        break;
                    } else {
                        SimpleWifiConfig simpleConfig2 = (SimpleWifiConfig) OppoWifiOCloudImpl.this.mNetworks.get(wifiConfig.configKey());
                        if (simpleConfig2 == null) {
                            Log.e(OppoWifiOCloudImpl.TAG, "network is not in mNetworks: " + wifiConfig.configKey());
                            needSync = false;
                            break;
                        } else {
                            if (!simpleConfig2.mGlobalId.equals(wifiConfig.globalId)) {
                                simpleConfig2.mGlobalId = wifiConfig.globalId;
                            }
                            if (simpleConfig2.mNetworkId != networkId) {
                                simpleConfig2.mNetworkId = networkId;
                            }
                            if (simpleConfig2.mPassword != null && wifiConfig.preSharedKey != null) {
                                if (!simpleConfig2.mPassword.equals(wifiConfig.preSharedKey)) {
                                    simpleConfig2.mPassword = wifiConfig.preSharedKey;
                                    simpleConfig2.mDirty = true;
                                    simpleConfig2.mAction = SimpleWifiConfig.UPDATE;
                                    break;
                                } else {
                                    simpleConfig2.mDirty = false;
                                    simpleConfig2.mAction = "null";
                                    needSync = false;
                                    break;
                                }
                            } else {
                                needSync = false;
                                break;
                            }
                        }
                    }
                    break;
                case 4:
                case 5:
                    needSync = false;
                    break;
                case 6:
                    OppoWifiOCloudImpl.this.backupWhileLoadFromStore();
                    if (!OppoWifiOCloudImpl.this.hasOCloudDirtyData()) {
                        needSync = false;
                        break;
                    }
                    break;
                default:
                    needSync = false;
                    Log.d(OppoWifiOCloudImpl.TAG, "ignore unknown msg " + msg.what);
                    break;
            }
            if (needSync && OppoWifiOCloudImpl.this.isWifiOCloudSwitchOn()) {
                Log.d(OppoWifiOCloudImpl.TAG, "need to sync");
                OppoWifiOCloudImpl.this.notifyCloudService();
            }
        }
    }

    /* access modifiers changed from: private */
    public boolean isWifiOCloudSwitchOn() {
        if (Settings.Global.getInt(this.mContext.getContentResolver(), SETTINGS_WIFI_HEYTAP_ENABLE, 0) != -1) {
            return true;
        }
        if (DBG) {
            Log.d(TAG, "wifi ocloud sync is diabled");
        }
        return false;
    }

    /* access modifiers changed from: private */
    public SimpleWifiConfig getInternalDeletedNetwork(int networkId) {
        for (SimpleWifiConfig config : this.mNetworks.values()) {
            if (config.mNetworkId == networkId) {
                return config;
            }
        }
        return null;
    }

    /* access modifiers changed from: protected */
    public void updateGlobalId(WifiConfiguration internalConfig, String globalId) {
        if (internalConfig == null) {
            Log.e(TAG, "internal config is invalid!");
            return;
        }
        internalConfig.globalId = globalId;
        SimpleWifiConfig internalSimpleConfig = this.mNetworks.get(internalConfig.configKey());
        internalSimpleConfig.mGlobalId = globalId;
        internalSimpleConfig.mDirty = false;
        internalSimpleConfig.mAction = "null";
    }

    /* access modifiers changed from: protected */
    public List<String> getWifiOCloudData(boolean dirtyOnly) {
        if (DBG) {
            Log.d(TAG, "getting wifi data with dirtyonly " + dirtyOnly);
        }
        List<String> result = new ArrayList<>();
        synchronized (this.mNetworks) {
            Iterator it = this.mNetworks.keySet().iterator();
            while (it.hasNext()) {
                String key = it.next();
                SimpleWifiConfig simpleConfig = this.mNetworks.get(key);
                if (!dirtyOnly || simpleConfig.isDirty()) {
                    String config = simpleConfig.toJSONString();
                    if (config != null) {
                        if (!SimpleWifiConfig.ADD.equals(simpleConfig.mAction)) {
                            if ("NULL".equals(simpleConfig.mGlobalId)) {
                                Log.e(TAG, "not a add action but gid is null:" + simpleConfig.mConfigKey + " action: " + simpleConfig.mAction);
                                it.remove();
                            }
                        } else if (!"NULL".equals(simpleConfig.mGlobalId)) {
                            Log.e(TAG, "a add action but gid is not null:" + simpleConfig.mConfigKey + " action: " + simpleConfig.mAction + " gid: " + simpleConfig.mGlobalId);
                            it.remove();
                        }
                        if (DBG) {
                            Log.d(TAG, "adding config: " + key);
                        }
                        result.add(config);
                    }
                }
            }
        }
        Iterator<String> it2 = this.mDuplicateRecord.iterator();
        while (it2.hasNext()) {
            result.add(it2.next());
        }
        return result;
    }

    /* access modifiers changed from: protected */
    public void removeServerEndDuplicateNetwork(String configKey, String password, int networkId, String globalId) {
        if (DBG) {
            Log.d(TAG, "removing ocloud server dup record: " + globalId);
        }
        SimpleWifiConfig swc = new SimpleWifiConfig(configKey, password, networkId, globalId);
        swc.mAction = SimpleWifiConfig.DELETE;
        String dC = swc.toJSONString();
        if (dC != null) {
            this.mDuplicateRecord.add(dC);
        }
        SimpleWifiConfig swc2 = this.mNetworks.get(configKey);
        if (swc2 != null && swc2.mGlobalId.equals(globalId)) {
            synchronized (this.mNetworks) {
                this.mNetworks.remove(swc2.mConfigKey);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void removeNetworkByGlobalId(String cfgKey, String globalId, boolean hardDelete) {
        ConcurrentHashMap<String, SimpleWifiConfig> concurrentHashMap = this.mNetworks;
        if (concurrentHashMap != null) {
            SimpleWifiConfig deleteConfig = null;
            synchronized (concurrentHashMap) {
                Iterator<SimpleWifiConfig> it = this.mNetworks.values().iterator();
                while (true) {
                    if (!it.hasNext()) {
                        break;
                    }
                    SimpleWifiConfig config = it.next();
                    if (config.mGlobalId.equals(globalId)) {
                        deleteConfig = config;
                        break;
                    }
                }
            }
            if (deleteConfig != null) {
                if (DBG) {
                    Log.d(TAG, "delete config: " + deleteConfig.mConfigKey);
                }
                if (this.mNetworks.remove(deleteConfig.mConfigKey) == null) {
                    Log.d(TAG, "mNetworks remove failed!");
                } else {
                    Log.d(TAG, "mNetworks remove success!");
                }
            }
            int index = -1;
            Iterator<String> it2 = this.mDuplicateRecord.iterator();
            while (true) {
                if (!it2.hasNext()) {
                    break;
                }
                String str = it2.next();
                if (str.contains(globalId)) {
                    index = this.mDuplicateRecord.indexOf(str);
                    break;
                }
            }
            if (index != -1) {
                Log.d(TAG, "dup record found! index:  " + index);
                this.mDuplicateRecord.remove(index);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void clear() {
        this.mNetworks.clear();
        this.mDuplicateRecord.clear();
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: ClspMth{android.content.Intent.putExtra(java.lang.String, boolean):android.content.Intent}
     arg types: [java.lang.String, int]
     candidates:
      ClspMth{android.content.Intent.putExtra(java.lang.String, int):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, java.lang.String[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, int[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, double):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, char):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, boolean[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, byte):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, android.os.Bundle):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, float):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, java.lang.CharSequence[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, java.lang.CharSequence):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, long[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, long):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, short):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, android.os.Parcelable[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, java.io.Serializable):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, double[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, android.os.Parcelable):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, float[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, byte[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, java.lang.String):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, short[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, char[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, boolean):android.content.Intent} */
    /* access modifiers changed from: private */
    public void notifyCloudService() {
        Log.d(TAG, "Notify ocloud service");
        Intent intent = new Intent(ACTION_DATA_CHANGE);
        intent.putExtra(EXTRA_APP, "wifi");
        intent.putExtra(EXTRA_RECOVER_FLAG, false);
        intent.setPackage(COLOROS_CLOUD_PACKAGE_NAME);
        try {
            this.mContext.sendBroadcast(intent, PERMISSION_DATA_CHANGE);
        } catch (Exception e) {
            Log.e(TAG, "sendSyncDataChangeMsg failed. error = " + e.toString());
        }
    }

    /* access modifiers changed from: protected */
    public void enableVerboseLogging(boolean enable) {
        DBG = enable;
    }

    /* access modifiers changed from: private */
    public class SimpleWifiConfig {
        public static final String ACTION = "action";
        public static final String ADD = "add";
        public static final String CONFIG_KEY = "configKey";
        public static final String DELETE = "delete";
        public static final String GLOBAL_ID = "globalId";
        public static final String ITEM_ID = "itemId";
        public static final String PASSWORD = "password";
        public static final String UPDATE = "update";
        public String mAction;
        public String mConfigKey;
        public boolean mDirty;
        public String mGlobalId;
        public int mNetworkId;
        public String mPassword;

        SimpleWifiConfig(String configKey, String password, int networkId, String globalId) {
            this.mConfigKey = configKey;
            this.mPassword = password;
            this.mNetworkId = networkId;
            this.mGlobalId = globalId;
            if ("NULL".equals(this.mGlobalId)) {
                this.mDirty = true;
                this.mAction = ADD;
                return;
            }
            this.mDirty = false;
            this.mAction = "null";
        }

        SimpleWifiConfig(SimpleWifiConfig source) {
            if (source != null) {
                this.mConfigKey = source.mConfigKey;
                this.mPassword = source.mPassword;
                this.mNetworkId = source.mNetworkId;
                this.mGlobalId = source.mGlobalId;
                this.mDirty = source.mDirty;
                this.mAction = source.mAction;
            }
        }

        public boolean isDirty() {
            return this.mDirty;
        }

        public String toJSONString() {
            JSONObject jObj = new JSONObject();
            try {
                jObj.put("configKey", this.mConfigKey);
                jObj.put(PASSWORD, this.mPassword);
                jObj.put(ITEM_ID, this.mNetworkId);
                jObj.put(ACTION, this.mAction);
                jObj.put(GLOBAL_ID, this.mGlobalId);
                return jObj.toString();
            } catch (JSONException jE) {
                Log.e(OppoWifiOCloudImpl.TAG, jE.toString());
                return null;
            }
        }
    }
}
