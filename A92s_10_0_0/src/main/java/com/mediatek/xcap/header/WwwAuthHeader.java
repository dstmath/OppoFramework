package com.mediatek.xcap.header;

import android.util.Log;

public class WwwAuthHeader {
    private static final String ALGO = "algorithm";
    private static final String DIGEST_SCHEME = "Digest";
    private static final String NONCE = "nonce";
    private static final String OPAQUE = "opaque";
    private static final String QOP = "qop";
    private static final String QOP_AUTH = "auth";
    private static final String QOP_AUTH_INT = "auth-int";
    private static final String REALM = "realm";
    private static final String TAG = "WwwAuthenticateHeader";
    private String mAlgorithm;
    private String mNonce;
    private String mOpaque;
    private String mQop;
    private String mRealm;
    private String mSchemeName;

    protected WwwAuthHeader(String schemeName, String realm, String nonce, String algorithm, String qop, String opaque) {
        this.mSchemeName = schemeName;
        this.mRealm = realm;
        this.mNonce = nonce;
        this.mAlgorithm = algorithm;
        this.mQop = qop;
        this.mOpaque = opaque;
    }

    public String getRealm() {
        return this.mRealm;
    }

    public void setRealm(String realm) {
        this.mRealm = realm;
    }

    public String getNonce() {
        return this.mNonce;
    }

    public void setNonce(String nonce) {
        this.mNonce = nonce;
    }

    public String getAlgorithm() {
        return this.mAlgorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.mAlgorithm = algorithm;
    }

    public String getQop() {
        return this.mQop;
    }

    public void setQop(String qop) {
        this.mQop = qop;
    }

    public String getOpaque() {
        return this.mOpaque;
    }

    public void setOpaque(String opaque) {
        this.mOpaque = opaque;
    }

    public String getSchemeName() {
        return this.mSchemeName;
    }

    public void setSchemeName(String schemeName) {
        this.mSchemeName = schemeName;
    }

    public String toString() {
        return "WwwAuthHeader [realm=" + this.mRealm + ", schema name=" + this.mSchemeName + ", nonce=" + this.mNonce + ", algorithm=" + this.mAlgorithm + ", qop=" + this.mQop + ", opaque=" + this.mOpaque + "]";
    }

    public static WwwAuthHeader parse(String headerValue) {
        String kv;
        WwwAuthHeader obj = null;
        int pos = 0;
        for (String value = headerValue; pos < value.length(); value = value) {
            int pos2 = HeaderParser.skipUntil(value, pos, " ");
            String scheme = value.substring(pos, pos2).trim();
            int pos3 = HeaderParser.skipWhitespace(value, pos2);
            String rest = value.substring(pos3);
            pos = pos3 + rest.length();
            String[] fields = rest.split(",");
            String nonce = null;
            String algo = null;
            String qop = null;
            String opaque = null;
            int i = 0;
            String realm = null;
            for (String field : fields) {
                System.out.println("field[" + i + "]: " + field);
                String[] keyValue = field.trim().split("=");
                if (keyValue.length < 2) {
                    System.out.println("No support:" + field);
                } else {
                    String key = keyValue[0];
                    if (keyValue.length > 2) {
                        kv = field.trim().substring(key.length() + 1);
                    } else {
                        kv = keyValue[1];
                    }
                    if (kv.indexOf("\"") >= 0) {
                        kv = HeaderParser.getQuoteString(kv, key, 0);
                    }
                    if (REALM.equals(key)) {
                        realm = kv;
                    } else if (!"uri".equals(key)) {
                        if (ALGO.equals(key)) {
                            algo = kv;
                        } else if (!"domain".equals(key)) {
                            if (NONCE.equals(key)) {
                                nonce = kv;
                            } else if (!"stale".equals(key)) {
                                if (QOP.equals(key)) {
                                    qop = kv;
                                } else if (OPAQUE.equals(key)) {
                                    opaque = kv;
                                }
                            }
                        }
                    }
                }
                i++;
            }
            obj = new WwwAuthHeader(scheme, realm, nonce, algo, qop, opaque);
        }
        Log.d(TAG, "Dump:" + obj);
        return obj;
    }
}
