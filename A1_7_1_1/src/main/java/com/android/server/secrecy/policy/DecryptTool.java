package com.android.server.secrecy.policy;

import android.content.Context;
import android.secrecy.RC4;
import android.util.ArrayMap;
import com.android.server.secrecy.RSA;
import com.android.server.secrecy.SecrecyConfig;
import com.android.server.secrecy.policy.util.LogUtil;
import java.io.PrintWriter;
import java.io.StringReader;
import java.math.BigInteger;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.Map;
import java.util.Properties;

public class DecryptTool {
    public static final String COUNTDOWN = "countdown";
    private static final String ENCRYPT_ADB_PROP = "encrypt_adb";
    private static final String ENCRYPT_ALL_PROP = "encrypt_all";
    private static final String ENCRYPT_APP_PROP = "encrypt_app";
    private static final String ENCRYPT_LOG_PROP = "encrypt_log";
    private static final String ENCRYPT_UNLOCK_TYPE = "unlock_type";
    private static final String IMEI_PROP = "imei";
    private static final String MODULUS = "14o4ia2g027r2dcmf4mw9f1pvoifswti3i2x1fvtmxsqpjvjqj1ry3dvmlyuwn9lhxl03bgvxxo5sgj5opu1l8vov2jrdlaf3031z3drh72068omvyft11qdaszicxys2bjqi9sxl5z79mzy6dcmubwqyi7fr424okmq9wyxctpmni43ok5d2ac9v5dezpu68send5foga4xxnpckhuzcjhnb4y7ot4z0ypm9j3hb59ax8v7n4ed82p01xtmu73iajvjnxov4wfuni17mmhi9smsf3sak8q9k04no4u8f7um8h4qhqk3xww3nmohl4190hr2rwnsw5nw1qs7vh60adz8a94qkeerkvvkf2b6qznm3q5x2aa1e8u9fq4liav015vwx5xzfn64b";
    private static final long ONE_DAY_IN_MILLIS = 86400000;
    private static final String PUBLIC_EXPONENT = "1ekh";
    private static final String STAMP_PROP = "stamp";
    private static final String TAG = "SecrecyService.DecryptTool";
    public static final String UNLOCK_TYPE_ID = "id";
    public static final String UNLOCK_TYPE_INTERNAL = "internal";
    public static final String UNLOCK_TYPE_MAC = "mac";
    private final Context mContext;
    private final PolicyManager mPolicyManager;

    public DecryptTool(Context context, PolicyManager policyManager) {
        this.mPolicyManager = policyManager;
        this.mContext = context;
    }

    public boolean verifyKey(PrintWriter pw, String key_arg) {
        if (verifySignature(this.mPolicyManager.getImei(), key_arg)) {
            pw.println("OK: key imported successful!");
            return true;
        }
        pw.println("ERROR: The key to import is invalid.");
        return false;
    }

    public void config(PrintWriter pw, String config) {
        LogUtil.d(TAG, "config = " + config);
        StringReader sr = new StringReader(config.replace('.', 13));
        Properties pros = new Properties();
        try {
            pros.load(sr);
            Map<Integer, Boolean> policyStateMap = new ArrayMap();
            String prop = pros.getProperty(ENCRYPT_APP_PROP);
            LogUtil.d(TAG, "encryptAppSuggest prop = " + prop);
            if (prop != null) {
                prop = prop.trim();
            }
            if ("true".equals(prop)) {
                policyStateMap.put(Integer.valueOf(2), Boolean.valueOf(true));
            }
            prop = pros.getProperty(ENCRYPT_LOG_PROP);
            LogUtil.d(TAG, "encryptLogSuggest prop = " + prop);
            if (prop != null) {
                prop = prop.trim();
            }
            if ("true".equals(prop)) {
                policyStateMap.put(Integer.valueOf(1), Boolean.valueOf(true));
            }
            prop = pros.getProperty(ENCRYPT_ADB_PROP);
            LogUtil.d(TAG, "encryptAdbSuggest prop = " + prop);
            if (prop != null) {
                prop = prop.trim();
            }
            if ("true".equals(prop)) {
                policyStateMap.put(Integer.valueOf(4), Boolean.valueOf(true));
            }
            prop = pros.getProperty(ENCRYPT_ALL_PROP);
            LogUtil.d(TAG, "encryptAll prop = " + prop);
            if (prop != null) {
                prop = prop.trim();
            }
            if ("true".equals(prop)) {
                this.mPolicyManager.createPolicyState(policyStateMap, true);
            }
            if (policyStateMap.isEmpty()) {
                pw.println("ERROR: Config argument is illegal.");
            } else {
                this.mPolicyManager.updatePolicyState(policyStateMap, "config");
                pw.println(PowerStatusReport.OK);
            }
        } catch (Exception e) {
            pw.println("ERROR: Config argument is malformat.");
        }
    }

    public void config(PrintWriter pw, String config, String signature) {
        String imei = this.mPolicyManager.getImei();
        LogUtil.d(TAG, "config = " + config + ", sinature = " + signature);
        LogUtil.d(TAG, "imei = " + imei);
        StringReader stringReader = new StringReader(config.replace('.', 13));
        Properties pros = new Properties();
        try {
            pros.load(stringReader);
            String proImei = pros.getProperty(IMEI_PROP);
            Map<Integer, Boolean> policyStateMap = new ArrayMap();
            if ("000000000000000".equals(proImei) && this.mPolicyManager.getTestSwitch()) {
                this.mPolicyManager.createPolicyState(policyStateMap, false);
                this.mPolicyManager.updatePolicyState(policyStateMap, "config_no_verfify");
            } else if ("111111111111111".equals(proImei) && this.mPolicyManager.getTestSwitch()) {
                this.mPolicyManager.createPolicyState(policyStateMap, true);
                this.mPolicyManager.updatePolicyState(policyStateMap, "config_no_verfify");
            } else if (proImei == null || !proImei.trim().equals(imei)) {
                pw.println("ERROR: IMEI doesn't match.");
                LogUtil.d(TAG, "ERROR: IMEI doesn't match.");
            } else {
                String proStamp = pros.getProperty(STAMP_PROP);
                if (proStamp != null) {
                    try {
                        long stamp = Long.parseLong(proStamp, 16);
                        long now = System.currentTimeMillis();
                        if (stamp > 86400000 + now || stamp < now - 86400000) {
                            pw.println("ERROR: stamp is expired.");
                            return;
                        } else if (!SecrecyConfig.getInstance().calculateChallenge(stamp).equals(signature)) {
                            pw.println("ERROR: Signature doesn't match.");
                            return;
                        }
                    } catch (NumberFormatException e) {
                        pw.println("ERROR: stamp is invalid.");
                        return;
                    }
                } else if (!verifySignature(config, signature)) {
                    pw.println("ERROR: Signature doesn't match.");
                    return;
                }
                String prop = pros.getProperty(ENCRYPT_APP_PROP);
                LogUtil.d(TAG, "encryptAppSuggest prop = " + prop);
                if (prop != null) {
                    prop = prop.trim();
                }
                if ("true".equals(prop)) {
                    policyStateMap.put(Integer.valueOf(2), Boolean.valueOf(true));
                } else if ("false".equals(prop)) {
                    policyStateMap.put(Integer.valueOf(2), Boolean.valueOf(false));
                }
                prop = pros.getProperty(ENCRYPT_LOG_PROP);
                LogUtil.d(TAG, "encryptLogSuggest prop = " + prop);
                if (prop != null) {
                    prop = prop.trim();
                }
                if ("true".equals(prop)) {
                    policyStateMap.put(Integer.valueOf(1), Boolean.valueOf(true));
                } else if ("false".equals(prop)) {
                    policyStateMap.put(Integer.valueOf(1), Boolean.valueOf(false));
                }
                prop = pros.getProperty(ENCRYPT_ADB_PROP);
                LogUtil.d(TAG, "encryptAdbSuggest prop = " + prop);
                if (prop != null) {
                    prop = prop.trim();
                }
                if ("true".equals(prop)) {
                    policyStateMap.put(Integer.valueOf(4), Boolean.valueOf(true));
                } else if ("false".equals(prop)) {
                    policyStateMap.put(Integer.valueOf(4), Boolean.valueOf(false));
                }
                prop = pros.getProperty(ENCRYPT_ALL_PROP);
                LogUtil.d(TAG, "encryptAll prop = " + prop);
                if (prop != null) {
                    prop = prop.trim();
                }
                if ("true".equals(prop)) {
                    this.mPolicyManager.createPolicyState(policyStateMap, true);
                } else if ("false".equals(prop)) {
                    this.mPolicyManager.createPolicyState(policyStateMap, false);
                }
                String unlockType = pros.getProperty(ENCRYPT_UNLOCK_TYPE);
                LogUtil.d(TAG, "encryptUnlockType prop = " + unlockType);
                if (unlockType != null) {
                    prop = unlockType.trim();
                }
                prop = pros.getProperty(COUNTDOWN);
                LogUtil.d(TAG, "countdown prop = " + prop);
                if (prop != null) {
                    prop = prop.trim();
                }
                int countdown = -1;
                if ("def".equals(prop)) {
                    countdown = 1440;
                } else {
                    try {
                        countdown = Integer.parseInt(prop);
                    } catch (NumberFormatException e2) {
                    }
                }
                if (countdown >= 0 || !policyStateMap.isEmpty()) {
                    if (countdown >= 0) {
                        this.mPolicyManager.setCountdown(countdown, true);
                    }
                    if (!policyStateMap.isEmpty()) {
                        this.mPolicyManager.updatePolicyState(policyStateMap, unlockType);
                    }
                    pw.println(PowerStatusReport.OK);
                } else {
                    pw.println("ERROR: Config argument is illegal.");
                }
            }
        } catch (Exception e3) {
            pw.println("ERROR: Config argument is malformat.");
        }
    }

    private boolean verifySignature(String content, String signatureStr) {
        PublicKey publicKey = RSA.initRsaPublicKey(new BigInteger(MODULUS, 36), new BigInteger(PUBLIC_EXPONENT, 36));
        if (publicKey == null) {
            return false;
        }
        try {
            byte[] deciphered = RSA.decrypt(publicKey, RC4.decodeHex(signatureStr));
            byte[] sha = RSA.shaDigest(content);
            if (deciphered == null || sha == null) {
                LogUtil.e(TAG, "ERROR: Verify signature failed.");
                return false;
            }
            byte[] trim;
            if (deciphered.length > sha.length) {
                trim = new byte[sha.length];
                System.arraycopy(deciphered, deciphered.length - sha.length, trim, 0, sha.length);
            } else {
                trim = deciphered;
            }
            return Arrays.equals(sha, trim);
        } catch (IllegalArgumentException e) {
            LogUtil.e(TAG, "ERROR: Signature is malformat.");
            return false;
        }
    }
}
