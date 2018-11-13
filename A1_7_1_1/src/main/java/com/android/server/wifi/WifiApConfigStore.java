package com.android.server.wifi;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.os.SystemProperties;
import android.util.Log;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

/*  JADX ERROR: NullPointerException in pass: ReSugarCode
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
    	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ExtractFieldInit.checkStaticFieldsInit(ExtractFieldInit.java:58)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class WifiApConfigStore {
    private static final int AP_CONFIG_FILE_VERSION = 2;
    private static final String DEFAULT_AP_CONFIG_FILE = null;
    private static final String TAG = "WifiApConfigStore";
    private ArrayList<Integer> mAllowed2GChannel;
    private final String mApConfigFile;
    private final BackupManagerProxy mBackupManagerProxy;
    private final Context mContext;
    private WifiConfiguration mWifiApConfig;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.server.wifi.WifiApConfigStore.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.server.wifi.WifiApConfigStore.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.wifi.WifiApConfigStore.<clinit>():void");
    }

    WifiApConfigStore(Context context, BackupManagerProxy backupManagerProxy) {
        this(context, backupManagerProxy, DEFAULT_AP_CONFIG_FILE);
    }

    WifiApConfigStore(Context context, BackupManagerProxy backupManagerProxy, String apConfigFile) {
        this.mWifiApConfig = null;
        this.mAllowed2GChannel = null;
        this.mContext = context;
        this.mBackupManagerProxy = backupManagerProxy;
        this.mApConfigFile = apConfigFile;
        String ap2GChannelListStr = this.mContext.getResources().getString(17039418);
        Log.d(TAG, "2G band allowed channels are:" + ap2GChannelListStr);
        if (ap2GChannelListStr != null) {
            this.mAllowed2GChannel = new ArrayList();
            for (String tmp : ap2GChannelListStr.split(",")) {
                this.mAllowed2GChannel.add(Integer.valueOf(Integer.parseInt(tmp)));
            }
        }
        this.mWifiApConfig = loadApConfiguration(this.mApConfigFile);
        if (this.mWifiApConfig == null) {
            Log.d(TAG, "Fallback to use default AP configuration");
            this.mWifiApConfig = getDefaultApConfiguration();
            writeApConfiguration(this.mApConfigFile, this.mWifiApConfig);
        }
    }

    public synchronized WifiConfiguration getApConfiguration() {
        return this.mWifiApConfig;
    }

    public synchronized void setApConfiguration(WifiConfiguration config) {
        if (config == null) {
            this.mWifiApConfig = getDefaultApConfiguration();
        } else {
            this.mWifiApConfig = config;
        }
        writeApConfiguration(this.mApConfigFile, this.mWifiApConfig);
        this.mBackupManagerProxy.notifyDataChanged();
    }

    public ArrayList<Integer> getAllowed2GChannel() {
        return this.mAllowed2GChannel;
    }

    /* JADX WARNING: Removed duplicated region for block: B:41:0x00d8 A:{SYNTHETIC, Splitter: B:41:0x00d8} */
    /* JADX WARNING: Removed duplicated region for block: B:41:0x00d8 A:{SYNTHETIC, Splitter: B:41:0x00d8} */
    /* JADX WARNING: Removed duplicated region for block: B:47:0x00fb A:{SYNTHETIC, Splitter: B:47:0x00fb} */
    /* JADX WARNING: Removed duplicated region for block: B:47:0x00fb A:{SYNTHETIC, Splitter: B:47:0x00fb} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static WifiConfiguration loadApConfiguration(String filename) {
        IOException e;
        WifiConfiguration wifiConfiguration;
        Throwable th;
        DataInputStream in = null;
        try {
            DataInputStream in2;
            WifiConfiguration config = new WifiConfiguration();
            try {
                in2 = new DataInputStream(new BufferedInputStream(new FileInputStream(filename)));
            } catch (IOException e2) {
                e = e2;
                wifiConfiguration = config;
                try {
                    Log.e(TAG, "Error reading hotspot configuration " + e);
                    wifiConfiguration = null;
                    if (in != null) {
                    }
                    return wifiConfiguration;
                } catch (Throwable th2) {
                    th = th2;
                    if (in != null) {
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e3) {
                        Log.e(TAG, "Error closing hotspot configuration during read" + e3);
                    }
                }
                throw th;
            }
            try {
                int version = in2.readInt();
                if (version == 1 || version == 2) {
                    config.SSID = in2.readUTF();
                    if (version >= 2) {
                        config.apBand = in2.readInt();
                        config.apChannel = in2.readInt();
                    }
                    int authType = in2.readInt();
                    config.allowedKeyManagement.set(authType);
                    if (authType != 0) {
                        config.preSharedKey = in2.readUTF();
                        if (!(config.preSharedKey == null || config.preSharedKey.length() <= 0 || authType == 1 || authType == 4)) {
                            config.allowedKeyManagement.clear();
                            config.allowedKeyManagement.set(4);
                            Log.d(TAG, "Wrong key mgmt set default to WPA2_PSK!");
                        }
                    }
                    if (in2 != null) {
                        try {
                            in2.close();
                        } catch (IOException e32) {
                            Log.e(TAG, "Error closing hotspot configuration during read" + e32);
                        }
                    }
                    wifiConfiguration = config;
                    return wifiConfiguration;
                }
                Log.e(TAG, "Bad version on hotspot configuration file");
                if (in2 != null) {
                    try {
                        in2.close();
                    } catch (IOException e322) {
                        Log.e(TAG, "Error closing hotspot configuration during read" + e322);
                    }
                }
                return null;
            } catch (IOException e4) {
                e322 = e4;
                in = in2;
                wifiConfiguration = config;
                Log.e(TAG, "Error reading hotspot configuration " + e322);
                wifiConfiguration = null;
                if (in != null) {
                }
                return wifiConfiguration;
            } catch (Throwable th4) {
                th = th4;
                in = in2;
                wifiConfiguration = config;
                if (in != null) {
                }
                throw th;
            }
        } catch (IOException e5) {
            e322 = e5;
            Log.e(TAG, "Error reading hotspot configuration " + e322);
            wifiConfiguration = null;
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e3222) {
                    Log.e(TAG, "Error closing hotspot configuration during read" + e3222);
                }
            }
            return wifiConfiguration;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:30:0x006b A:{SYNTHETIC, Splitter: B:30:0x006b} */
    /* JADX WARNING: Removed duplicated region for block: B:41:0x007e A:{Catch:{ IOException -> 0x0071 }} */
    /* JADX WARNING: Removed duplicated region for block: B:33:0x0070 A:{SYNTHETIC, Splitter: B:33:0x0070} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static void writeApConfiguration(String filename, WifiConfiguration config) {
        IOException e;
        Throwable th;
        Throwable th2 = null;
        DataOutputStream out = null;
        try {
            DataOutputStream out2 = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(filename)));
            try {
                out2.writeInt(2);
                if (config.SSID != null) {
                    out2.writeUTF(config.SSID);
                }
                out2.writeInt(config.apBand);
                out2.writeInt(config.apChannel);
                int authType = config.getAuthType();
                out2.writeInt(authType);
                if (!(authType == 0 || config.preSharedKey == null)) {
                    out2.writeUTF(config.preSharedKey);
                }
                if (out2 != null) {
                    try {
                        out2.close();
                    } catch (Throwable th3) {
                        th2 = th3;
                    }
                }
                if (th2 != null) {
                    try {
                        throw th2;
                    } catch (IOException e2) {
                        e = e2;
                        out = out2;
                    }
                }
            } catch (Throwable th4) {
                th = th4;
                out = out2;
                if (out != null) {
                    try {
                        out.close();
                    } catch (Throwable th5) {
                        if (th2 == null) {
                            th2 = th5;
                        } else if (th2 != th5) {
                            th2.addSuppressed(th5);
                        }
                    }
                }
                if (th2 == null) {
                    try {
                        throw th2;
                    } catch (IOException e3) {
                        e = e3;
                        Log.e(TAG, "Error writing hotspot configuration" + e);
                        return;
                    }
                }
                throw th;
            }
        } catch (Throwable th6) {
            th = th6;
            if (out != null) {
            }
            if (th2 == null) {
            }
        }
    }

    private WifiConfiguration getDefaultApConfiguration() {
        WifiConfiguration config = new WifiConfiguration();
        config.SSID = SystemProperties.get("ro.oppo.market.name", "OPPO");
        if (config.SSID.equals("OPPO")) {
            config.SSID = SystemProperties.get("ro.product.model", "OPPO");
        }
        config.allowedKeyManagement.set(4);
        config.preSharedKey = "12345678";
        return config;
    }
}
