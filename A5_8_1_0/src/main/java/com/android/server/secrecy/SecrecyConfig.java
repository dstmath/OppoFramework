package com.android.server.secrecy;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.OppoUsageManager;
import android.os.SystemProperties;
import android.secrecy.RC4;
import android.util.ArrayMap;
import android.util.Base64;
import com.android.server.secrecy.policy.PolicyManager;
import com.android.server.secrecy.policy.util.LogUtil;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Properties;

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
    private static final String PROP_STR_ITRM = "ro.sys.reserve.integrate";
    private static final String RC4_KEY_PROP = "rc4_key";
    private static final String SECRECY_CONFIG_FILE = "/opporeserve/system/config/secrecy.cfg";
    private static final String TAG = "SecrecyService.SecrecyConfig";
    private static SecrecyConfig sInstance;
    private static final Object sInstanceLock = new Object();
    private String mConfigImei;
    private Handler mHandler;
    private PolicyManager mPolicyManager;
    private byte[] mRC4Key;
    private final byte[] mRC4Sbox = new byte[256];

    private class MyHandler extends Handler {
        /* synthetic */ MyHandler(SecrecyConfig this$0, Looper looper, MyHandler -this2) {
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
            return "";
        }
        byte[] challengeBytes = RC4.longToBytes(challenge);
        byte[] box = new byte[256];
        System.arraycopy(this.mRC4Sbox, 0, box, 0, this.mRC4Sbox.length);
        RC4.encrypt(box, challengeBytes);
        return RC4.encodeHex(challengeBytes);
    }

    private void loadSecrecyConfigInternal() {
        boolean supportIntergrateReserve = SystemProperties.getBoolean("ro.sys.reserve.integrate", false);
        String config = "";
        if (!supportIntergrateReserve) {
            config = OppoUsageManager.getOppoUsageManager().loadSecrecyConfig();
            if (config == null) {
                return;
            }
        }
        boolean defaultPolicy = this.mPolicyManager.getDefaultPolicy();
        boolean isEncryptLog = defaultPolicy;
        boolean isEncryptApp = defaultPolicy;
        boolean isEncryptAdb = defaultPolicy;
        Map<Integer, Boolean> policyStateMap = new ArrayMap();
        Properties properties = new Properties();
        InputStream input = null;
        if (supportIntergrateReserve) {
            try {
                input = new FileInputStream(SECRECY_CONFIG_FILE);
            } catch (IOException e) {
                if (input != null) {
                    try {
                        input.close();
                    } catch (IOException e2) {
                    }
                }
            } catch (Throwable th) {
                if (input != null) {
                    try {
                        input.close();
                    } catch (IOException e3) {
                    }
                }
            }
        } else {
            input = new ByteArrayInputStream(config.getBytes("utf-8"));
        }
        properties.load(input);
        String RC4_key_hex = properties.getProperty(RC4_KEY_PROP);
        if (RC4_key_hex == null) {
            LogUtil.d(TAG, "RC4Key is null, use defaultPolicy");
            this.mPolicyManager.createPolicyState(policyStateMap, defaultPolicy);
            this.mPolicyManager.updatePolicyState(policyStateMap, "load_config");
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e4) {
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
                } catch (NumberFormatException e5) {
                }
            }
            this.mPolicyManager.setLastDownloadTimeInMills(properties.getProperty(LAST_DOWNLODE_TIME));
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e6) {
                }
            }
        } catch (IllegalArgumentException e7) {
            this.mPolicyManager.createPolicyState(policyStateMap, defaultPolicy);
            this.mPolicyManager.updatePolicyState(policyStateMap, "load_config");
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e8) {
                }
            }
        }
    }

    private void saveSecrecyConfigInternal() {
        if (this.mRC4Key == null) {
            LogUtil.d(TAG, "RC4Key is null, skip save action");
            return;
        }
        Properties properties = new Properties();
        OutputStream outputStream = null;
        try {
            boolean supportIntergrateReserve = SystemProperties.getBoolean("ro.sys.reserve.integrate", false);
            if (supportIntergrateReserve) {
                outputStream = new FileOutputStream(SECRECY_CONFIG_FILE);
            } else {
                outputStream = new ByteArrayOutputStream();
            }
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
            properties.store(outputStream, null);
            if (!supportIntergrateReserve) {
                int saveSecrecyConfig = OppoUsageManager.getOppoUsageManager().saveSecrecyConfig(((ByteArrayOutputStream) outputStream).toString("utf-8"));
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                }
            }
        } catch (IOException e2) {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e3) {
                }
            }
        } catch (Throwable th) {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e4) {
                }
            }
        }
    }

    public String generateTokenFromKey() {
        if (this.mConfigImei == null) {
            return null;
        }
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update(this.mConfigImei.getBytes());
            return Base64.encodeToString(messageDigest.digest(), 9);
        } catch (NoSuchAlgorithmException e) {
            return null;
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
