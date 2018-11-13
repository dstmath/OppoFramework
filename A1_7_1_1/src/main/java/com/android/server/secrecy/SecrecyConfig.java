package com.android.server.secrecy;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.OppoUsageManager;
import android.secrecy.RC4;
import android.util.ArrayMap;
import com.android.server.oppo.IElsaManager;
import com.android.server.secrecy.policy.PolicyManager;
import com.android.server.secrecy.policy.util.LogUtil;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Map;
import java.util.Properties;

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
public class SecrecyConfig {
    private static final String COUNTDOWN_PROP = "countdown";
    private static final String ENCRYPT_ADB_PROP = "encrypt_adb";
    private static final String ENCRYPT_APP_PROP = "encrypt_app";
    private static final String ENCRYPT_LOG_PROP = "encrypt_log";
    private static final String IMEI_PROP = "imei";
    private static final String LAST_DOWNLODE_TIME = "last_download_time";
    private static final int MSG_LOAD_CONFIG = 1;
    private static final int MSG_NOTIFY_CONFIG_CHANGED = 3;
    private static final int MSG_SAVE_CONFIG = 2;
    private static final String RC4_KEY_PROP = "rc4_key";
    private static final String TAG = "SecrecyService.SecrecyConfig";
    private static SecrecyConfig sInstance;
    private static final Object sInstanceLock = null;
    private String mConfigImei;
    private Handler mHandler;
    private PolicyManager mPolicyManager;
    private byte[] mRC4Key;
    private final byte[] mRC4Sbox;

    private class MyHandler extends Handler {
        /* synthetic */ MyHandler(SecrecyConfig this$0, Looper looper, MyHandler myHandler) {
            this(looper);
        }

        private MyHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    SecrecyConfig.this.loadSecrecyConfigInternal();
                    SecrecyConfig.this.mPolicyManager.onConfigLoadFinished();
                    return;
                case 2:
                    SecrecyConfig.this.saveSecrecyConfigInternal();
                    SecrecyConfig.this.mPolicyManager.onConfigSaveFinished((Map) msg.obj);
                    return;
                default:
                    return;
            }
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.secrecy.SecrecyConfig.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.secrecy.SecrecyConfig.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.secrecy.SecrecyConfig.<clinit>():void");
    }

    public SecrecyConfig() {
        this.mRC4Sbox = new byte[256];
    }

    public static SecrecyConfig getInstance() {
        synchronized (sInstanceLock) {
            if (sInstance == null) {
                sInstance = new SecrecyConfig();
            }
        }
        return sInstance;
    }

    public void setPolicyManager(PolicyManager policyManager, Looper looper) {
        this.mPolicyManager = policyManager;
        this.mHandler = new MyHandler(this, looper, null);
    }

    public void saveSecrecyConfig(Map map) {
        if (this.mHandler != null) {
            this.mHandler.removeMessages(2);
            this.mHandler.sendMessage(this.mHandler.obtainMessage(2, map));
        }
    }

    public void loadSecrecyConfig() {
        if (this.mHandler != null) {
            this.mHandler.removeMessages(1);
            this.mHandler.sendEmptyMessage(1);
        }
    }

    public void saveRC4Key(String key_arg) {
        this.mRC4Key = RC4.decodeHex(key_arg);
    }

    public void saveImei(String imei) {
        this.mConfigImei = imei;
    }

    public boolean getSecrecyKey(byte[] key) {
        System.arraycopy(this.mRC4Sbox, 0, key, 0, this.mRC4Sbox.length);
        return true;
    }

    public String calculateChallenge(long challenge) {
        if (this.mRC4Key == null) {
            return IElsaManager.EMPTY_PACKAGE;
        }
        byte[] challengeBytes = RC4.longToBytes(challenge);
        byte[] box = new byte[256];
        System.arraycopy(this.mRC4Sbox, 0, box, 0, this.mRC4Sbox.length);
        RC4.encrypt(box, challengeBytes);
        return RC4.encodeHex(challengeBytes);
    }

    /* JADX WARNING: Removed duplicated region for block: B:53:0x01aa A:{SYNTHETIC, Splitter: B:53:0x01aa} */
    /* JADX WARNING: Removed duplicated region for block: B:58:0x01b3 A:{SYNTHETIC, Splitter: B:58:0x01b3} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void loadSecrecyConfigInternal() {
        Throwable th;
        String config = OppoUsageManager.getOppoUsageManager().loadSecrecyConfig();
        if (config != null) {
            boolean defaultPolicy = this.mPolicyManager.getDefaultPolicy();
            boolean isEncryptLog = defaultPolicy;
            boolean isEncryptApp = defaultPolicy;
            boolean isEncryptAdb = defaultPolicy;
            Map<Integer, Boolean> policyStateMap = new ArrayMap();
            Properties properties = new Properties();
            ByteArrayInputStream input = null;
            try {
                InputStream byteArrayInputStream = new ByteArrayInputStream(config.getBytes("utf-8"));
                InputStream inputStream;
                try {
                    properties.load(byteArrayInputStream);
                    String RC4_key_hex = properties.getProperty(RC4_KEY_PROP);
                    if (RC4_key_hex == null) {
                        LogUtil.d(TAG, "RC4Key is null, use defaultPolicy");
                        this.mPolicyManager.createPolicyState(policyStateMap, defaultPolicy);
                        this.mPolicyManager.updatePolicyState(policyStateMap, "load_config");
                        if (byteArrayInputStream != null) {
                            try {
                                byteArrayInputStream.close();
                            } catch (IOException e) {
                            }
                        }
                        return;
                    }
                    try {
                        this.mRC4Key = RC4.decodeHexRC4(RC4_key_hex);
                        RC4.mixSbox(this.mRC4Sbox, this.mRC4Key);
                        String imei = properties.getProperty(IMEI_PROP);
                        if (imei != null) {
                            this.mConfigImei = imei;
                        }
                        String log = properties.getProperty(ENCRYPT_LOG_PROP);
                        if (log != null) {
                            isEncryptLog = "true".equals(log);
                        }
                        String app = properties.getProperty(ENCRYPT_APP_PROP);
                        if (app != null) {
                            isEncryptApp = "true".equals(app);
                        }
                        String adb = properties.getProperty(ENCRYPT_ADB_PROP);
                        if (adb != null) {
                            isEncryptAdb = "true".equals(adb);
                        }
                        policyStateMap.put(Integer.valueOf(1), Boolean.valueOf(isEncryptLog));
                        policyStateMap.put(Integer.valueOf(2), Boolean.valueOf(isEncryptApp));
                        policyStateMap.put(Integer.valueOf(4), Boolean.valueOf(isEncryptAdb));
                        this.mPolicyManager.updatePolicyState(policyStateMap, "load_config", false);
                        String countdownProp = properties.getProperty("countdown");
                        if (countdownProp != null) {
                            try {
                                this.mPolicyManager.setCountdown(Integer.parseInt(countdownProp, 16), false);
                            } catch (NumberFormatException e2) {
                            }
                        }
                        this.mPolicyManager.setLastDownloadTimeInMills(properties.getProperty(LAST_DOWNLODE_TIME));
                        if (byteArrayInputStream != null) {
                            try {
                                byteArrayInputStream.close();
                            } catch (IOException e3) {
                            }
                        }
                        inputStream = byteArrayInputStream;
                    } catch (IllegalArgumentException e4) {
                        this.mPolicyManager.createPolicyState(policyStateMap, defaultPolicy);
                        this.mPolicyManager.updatePolicyState(policyStateMap, "load_config");
                        if (byteArrayInputStream != null) {
                            try {
                                byteArrayInputStream.close();
                            } catch (IOException e5) {
                            }
                        }
                    }
                } catch (IOException e6) {
                    inputStream = byteArrayInputStream;
                    if (input != null) {
                    }
                } catch (Throwable th2) {
                    th = th2;
                    inputStream = byteArrayInputStream;
                    if (input != null) {
                    }
                    throw th;
                }
            } catch (IOException e7) {
                if (input != null) {
                    try {
                        input.close();
                    } catch (IOException e8) {
                    }
                }
            } catch (Throwable th3) {
                th = th3;
                if (input != null) {
                    try {
                        input.close();
                    } catch (IOException e9) {
                    }
                }
                throw th;
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:36:0x00b8 A:{SYNTHETIC, Splitter: B:36:0x00b8} */
    /* JADX WARNING: Removed duplicated region for block: B:41:0x00c1 A:{SYNTHETIC, Splitter: B:41:0x00c1} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void saveSecrecyConfigInternal() {
        Throwable th;
        if (this.mRC4Key == null) {
            LogUtil.d(TAG, "RC4Key is null, skip save action");
            return;
        }
        Properties properties = new Properties();
        ByteArrayOutputStream output = null;
        try {
            ByteArrayOutputStream output2 = new ByteArrayOutputStream();
            try {
                boolean isEncryptLog = this.mPolicyManager.getPolicyState(1);
                boolean isEncryptApp = this.mPolicyManager.getPolicyState(2);
                boolean isEncryptAdb = this.mPolicyManager.getPolicyState(4);
                String currentDownloadTimeInMillis = Long.toString(this.mPolicyManager.getCurrentDownloadTimeInMills().longValue());
                if (this.mConfigImei != null) {
                    properties.setProperty(IMEI_PROP, this.mConfigImei);
                }
                properties.setProperty(RC4_KEY_PROP, RC4.encodeHex(this.mRC4Key));
                properties.setProperty(ENCRYPT_LOG_PROP, isEncryptLog ? "true" : "false");
                properties.setProperty(ENCRYPT_APP_PROP, isEncryptApp ? "true" : "false");
                properties.setProperty(ENCRYPT_ADB_PROP, isEncryptAdb ? "true" : "false");
                int countdown = this.mPolicyManager.getCountdown();
                if (countdown > 0) {
                    properties.setProperty("countdown", Integer.toHexString(countdown));
                }
                properties.setProperty(LAST_DOWNLODE_TIME, currentDownloadTimeInMillis);
                properties.store(output2, null);
                int ret = OppoUsageManager.getOppoUsageManager().saveSecrecyConfig(output2.toString("utf-8"));
                if (output2 != null) {
                    try {
                        output2.close();
                    } catch (IOException e) {
                    }
                }
                output = output2;
            } catch (IOException e2) {
                output = output2;
                if (output != null) {
                }
            } catch (Throwable th2) {
                th = th2;
                output = output2;
                if (output != null) {
                }
                throw th;
            }
        } catch (IOException e3) {
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e4) {
                }
            }
        } catch (Throwable th3) {
            th = th3;
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e5) {
                }
            }
            throw th;
        }
    }

    public byte[] generateCipherFromKey(int cipherLength) {
        if (this.mRC4Key == null) {
            return null;
        }
        if (cipherLength != 4 && cipherLength != 8 && cipherLength != 16 && cipherLength != 32) {
            return null;
        }
        byte[] cipher = new byte[cipherLength];
        int loop = this.mRC4Key.length / cipherLength;
        for (int i = 0; i < cipherLength; i++) {
            for (int j = 0; j < loop; j++) {
                if (j == 0) {
                    cipher[i] = this.mRC4Key[i * loop];
                } else {
                    cipher[i] = (byte) (this.mRC4Key[(i * loop) + j] ^ cipher[i]);
                }
            }
        }
        return cipher;
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String prefix) {
        pw.print(prefix);
        pw.println("SecrecyConfig dump");
        pw.print(prefix);
        pw.println("    mRC4Key  = " + this.mRC4Key);
        pw.print(prefix);
        pw.println("    mRC4Sbox = " + this.mRC4Sbox);
        pw.print(prefix);
        pw.println("    mImei = " + this.mConfigImei);
    }
}
