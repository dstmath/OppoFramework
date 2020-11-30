package com.wapi.wapicertstore;

import android.security.KeyStore;
import android.util.Log;

public class WapiCertStore {
    private static final String TAG = "WapiCertStore";
    public static final String WAPI = "WAPI_";
    public static final String WAPI_CA_CERTIFICATE = "WAPI_CACERT_";
    public static final String WAPI_USER_CERTIFICATE = "WAPI_USRCERT_";
    public static final String WAPI_USER_CERTIFICATE_INFO = "WAPI_USRCERTINFO_";
    public static final String WAPI_USER_PRIVATE_KEY = "WAPI_USRPKEY_";
    private final KeyStore mKeyStore = KeyStore.getInstance();

    public static native int checkUserCaCertNative(byte[] bArr, byte[] bArr2, byte[] bArr3);

    public static native String getCertInfoNative(byte[] bArr);

    public static native int isP12CertNative(byte[] bArr);

    public static native UserCertParam parseP12CertNative(byte[] bArr, String str);

    public static native int testJniNative(String str);

    public boolean installCert(byte[] userCert, byte[] priKey, byte[] caCert, String certAlias) {
        int flags = 1010 == 1010 ? 0 : 1;
        KeyStore keyStore = this.mKeyStore;
        if (!keyStore.put(WAPI_USER_CERTIFICATE + certAlias, userCert, 1010, flags)) {
            Log.e(TAG, "Failed to install " + certAlias + " user cert 1010");
            return false;
        }
        KeyStore keyStore2 = this.mKeyStore;
        if (!keyStore2.put(WAPI_USER_PRIVATE_KEY + certAlias, priKey, 1010, flags)) {
            Log.e(TAG, "Failed to install " + certAlias + " user private key 1010");
            return false;
        }
        KeyStore keyStore3 = this.mKeyStore;
        if (!keyStore3.put(WAPI_CA_CERTIFICATE + certAlias, caCert, 1010, flags)) {
            Log.e(TAG, "Failed to install " + certAlias + " ca cert 1010");
            return false;
        }
        Log.d(TAG, "Install cert " + certAlias + " success");
        return true;
    }

    public byte[] getUserCert(String certAlias) {
        if (certAlias == null) {
            throw new NullPointerException("certAlias == null");
        } else if (this.mKeyStore.isUnlocked()) {
            KeyStore keyStore = this.mKeyStore;
            return keyStore.get(WAPI_USER_CERTIFICATE + certAlias);
        } else {
            throw new IllegalStateException("keystore is " + this.mKeyStore.state().toString());
        }
    }

    public boolean deleteCert(String certAlias) {
        KeyStore keyStore = this.mKeyStore;
        if (!keyStore.delete(WAPI_USER_CERTIFICATE + certAlias, 1010)) {
            Log.e(TAG, "Failed to delete " + certAlias + " user cert 1010");
            return false;
        }
        KeyStore keyStore2 = this.mKeyStore;
        if (!keyStore2.delete(WAPI_CA_CERTIFICATE + certAlias, 1010)) {
            Log.e(TAG, "Failed to delete " + certAlias + " ca cert 1010");
            return false;
        }
        KeyStore keyStore3 = this.mKeyStore;
        if (!keyStore3.delete(WAPI_USER_PRIVATE_KEY + certAlias, 1010)) {
            Log.e(TAG, "Failed to delete " + certAlias + " user private key 1010");
            return false;
        }
        Log.d(TAG, "Delete cert " + certAlias + " success");
        return true;
    }

    public String[] getCertAliasList() {
        return this.mKeyStore.list(WAPI_USER_CERTIFICATE, 1010);
    }

    public int testJni(String str) {
        return testJniNative(str);
    }

    public int isP12Cert(byte[] userCert) {
        return isP12CertNative(userCert);
    }

    public UserCertParam parseP12Cert(byte[] userCert, String password) {
        return parseP12CertNative(userCert, password);
    }

    public boolean checkUserCaCert(byte[] userCert, byte[] priKey, byte[] caCert) {
        if (checkUserCaCertNative(userCert, priKey, caCert) == 0) {
            return true;
        }
        return false;
    }

    public String getCertInfo(byte[] userCert) {
        return getCertInfoNative(userCert);
    }

    static {
        System.loadLibrary("wapi_cert_jni");
    }
}
