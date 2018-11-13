package com.wapi.wapicertstore;

import android.security.KeyStore;
import android.util.Log;

public class WapiCertStore {
    private static final String TAG = "WapiCertStore";
    public static final String WAPI = "WAPI_";
    public static final String WAPI_CA_CERTIFICATE = "WAPI_CA_";
    public static final String WAPI_USER_CERTIFICATE = "WAPI_USER_";
    public static final String WAPI_USER_CERTIFICATE_INFO = "WAPI_USRCERTINFO_";
    public static final String WAPI_USER_PRIVATE_KEY = "WAPI_KEY_";
    private final KeyStore mKeyStore = KeyStore.getInstance();

    public static native int checkUserCaCertNative(byte[] bArr, byte[] bArr2, byte[] bArr3);

    public static native String getCertInfoNative(byte[] bArr);

    public static native int isP12CertNative(byte[] bArr);

    public static native UserCertParam parseP12CertNative(byte[] bArr, String str);

    public static native int testJniNative(String str);

    public boolean installCert(byte[] userCert, byte[] priKey, byte[] caCert, String certAlias) {
        if (!this.mKeyStore.put(WAPI_USER_CERTIFICATE + certAlias, userCert, 1010, 0)) {
            Log.e(TAG, "Failed to install " + certAlias + " user cert " + 1010);
            return false;
        } else if (!this.mKeyStore.put(WAPI_USER_PRIVATE_KEY + certAlias, priKey, 1010, 0)) {
            Log.e(TAG, "Failed to install " + certAlias + " user private key " + 1010);
            return false;
        } else if (this.mKeyStore.put(WAPI_CA_CERTIFICATE + certAlias, caCert, 1010, 0)) {
            Log.d(TAG, "Install cert " + certAlias + " success");
            return true;
        } else {
            Log.e(TAG, "Failed to install " + certAlias + " ca cert " + 1010);
            return false;
        }
    }

    public byte[] getUserCert(String certAlias) {
        if (certAlias == null) {
            throw new NullPointerException("certAlias == null");
        } else if (this.mKeyStore.isUnlocked()) {
            return this.mKeyStore.get(WAPI_USER_CERTIFICATE + certAlias);
        } else {
            throw new IllegalStateException("keystore is " + this.mKeyStore.state().toString());
        }
    }

    public boolean deleteCert(String certAlias) {
        if (!this.mKeyStore.delete(WAPI_USER_CERTIFICATE + certAlias, 1010)) {
            Log.e(TAG, "Failed to delete " + certAlias + " user cert " + 1010);
            return false;
        } else if (!this.mKeyStore.delete(WAPI_CA_CERTIFICATE + certAlias, 1010)) {
            Log.e(TAG, "Failed to delete " + certAlias + " ca cert " + 1010);
            return false;
        } else if (this.mKeyStore.delete(WAPI_USER_PRIVATE_KEY + certAlias, 1010)) {
            Log.d(TAG, "Delete cert " + certAlias + " success");
            return true;
        } else {
            Log.e(TAG, "Failed to delete " + certAlias + " user private key " + 1010);
            return false;
        }
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
        System.loadLibrary("wapi_cert");
    }
}
