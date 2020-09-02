package com.mediatek.xcap.auth;

import android.os.Build;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.Log;
import com.android.internal.util.HexDump;
import com.mediatek.xcap.header.WwwAuthHeader;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

public class AkaDigestAuth {
    private static final String AKA_SPLITTER = "-";
    private static final String AKA_VERSION = "AKAv";
    private static final String ALGORITHM_NAME_MD5 = "MD5";
    private static final String ALGORITHM_NAME_MD5_SESS = "MD5-sess";
    private static final String ALGORITHM_NAME_UNSPECIFIED = "";
    private static final String HEADER_VALUE_FORMAT_WITH_RESPONSE = "Digest username=\"%s\", realm=\"%s\", nonce=\"%s\", uri=\"%s\", qop=%s, nc=%s, cnonce=\"%s\", response=\"%s\", opaque=\"%s\"";
    private static final String HEADER_VALUE_FORMAT_WITH_RESPONSE2 = "Digest username=\"%s\", realm=\"%s\", nonce=\"%s\", uri=\"%s\", qop=%s, nc=%s, cnonce=\"%s\", response=\"%s\"";
    private static final char[] HEXADECIMAL = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
    private static final String ISO_8859_1 = "ISO-8859-1";
    private static final String PROP_FORCE_DEBUG_KEY = "persist.vendor.log.tag.tel_dbg";
    private static final String QOP_AUTH_BOTH = "auth, auth-int";
    private static final String QOP_AUTH_INT_NAME = "auth-int";
    private static final String QOP_AUTH_NAME = "auth";
    private static final String QOP_UNSPECIFIED = "";
    private static final String QUOTE = "\"";
    private static final boolean SENLOG = TextUtils.equals(Build.TYPE, "user");
    private static final String SEPARATOR = ":";
    private static final String TAG = "AkaDigestAuth";
    private static final boolean TELDBG;
    private static final String US_ASCII = "US-ASCII";
    private String mAlgorithm;
    private String mAuts;
    private String mCharSet;
    private String mCnonce;
    private String mEntityBody;
    MessageDigest mMd5Helper;
    private String mMethod;
    private String mNc;
    private String mNonce;
    private String mOpaque;
    private String mPassword;
    private String mQop;
    private String mRealm;
    private String mResponse;
    private String mUri;
    private String mUsername;

    static {
        boolean z = false;
        if (SystemProperties.getInt(PROP_FORCE_DEBUG_KEY, 0) == 1) {
            z = true;
        }
        TELDBG = z;
    }

    private AkaDigestAuth(String userName, String auts, String passwd, String uri, String nc, String qop, String algorithm, String realm, String nonce, String opaque, String cnonce, String method, String content) {
        initMd5();
        this.mQop = qop;
        this.mAlgorithm = algorithm;
        this.mUsername = userName;
        this.mRealm = realm;
        this.mPassword = passwd;
        this.mAuts = auts;
        this.mNonce = nonce;
        this.mOpaque = opaque;
        this.mUri = uri;
        this.mNc = nc;
        this.mMethod = method;
        this.mCnonce = cnonce;
        this.mEntityBody = content;
        this.mResponse = "";
        this.mCharSet = ISO_8859_1;
    }

    public AkaDigestAuth(WwwAuthHeader header, String userName, String auts, String passwd, String uri, String nc, String method, String content) {
        initMd5();
        this.mQop = header.getQop();
        if (!(this.mQop.indexOf(QOP_AUTH_NAME) == -1 || this.mQop.indexOf(QOP_AUTH_INT_NAME) == -1)) {
            this.mQop = QOP_AUTH_INT_NAME;
        }
        if (header.getAlgorithm() != null) {
            this.mAlgorithm = header.getAlgorithm();
        } else {
            this.mAlgorithm = "";
        }
        this.mUsername = userName;
        this.mRealm = header.getRealm();
        this.mPassword = passwd;
        this.mAuts = auts;
        this.mNonce = header.getNonce();
        this.mOpaque = header.getOpaque();
        this.mUri = uri;
        this.mNc = nc;
        this.mMethod = method;
        this.mCnonce = createCNonce();
        this.mEntityBody = content;
        this.mResponse = "";
        this.mCharSet = ISO_8859_1;
    }

    private void initMd5() {
        try {
            this.mMd5Helper = MessageDigest.getInstance(ALGORITHM_NAME_MD5);
            this.mMd5Helper.reset();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    public String createAuthorHeaderValue() {
        StringBuilder headerValue;
        String str = this.mOpaque;
        if (str != null) {
            headerValue = new StringBuilder(String.format(HEADER_VALUE_FORMAT_WITH_RESPONSE, this.mUsername, this.mRealm, this.mNonce, this.mUri, this.mQop, this.mNc, this.mCnonce, this.mResponse, str, this.mAlgorithm));
        } else {
            headerValue = new StringBuilder(String.format(HEADER_VALUE_FORMAT_WITH_RESPONSE2, this.mUsername, this.mRealm, this.mNonce, this.mUri, this.mQop, this.mNc, this.mCnonce, this.mResponse, this.mAlgorithm));
        }
        String str2 = this.mAlgorithm;
        if (str2 != null && str2.length() > 0) {
            headerValue.append(", algorithm=" + this.mAlgorithm);
        }
        String str3 = this.mAuts;
        if (str3 != null && str3.length() > 0) {
            headerValue.append(", auts=\"" + this.mAuts + QUOTE);
        }
        return headerValue.toString();
    }

    private String createCNonce() {
        String dataStr = "" + new Date().getTime();
        Log.i(TAG, "dataStr:" + dataStr);
        try {
            return md5(dataStr.getBytes(US_ASCII));
        } catch (UnsupportedEncodingException ue) {
            Log.e(TAG, "ue:" + ue);
            return "";
        }
    }

    private String encode(byte[] binaryData) {
        if (binaryData.length != 16) {
            return null;
        }
        char[] buffer = new char[32];
        for (int i = 0; i < 16; i++) {
            char[] cArr = HEXADECIMAL;
            buffer[i * 2] = cArr[(binaryData[i] & 240) >> 4];
            buffer[(i * 2) + 1] = cArr[binaryData[i] & 15];
        }
        return new String(buffer);
    }

    public String calculateRequestDigest() {
        String res = null;
        Log.i(TAG, "mQop:" + this.mQop);
        if (this.mQop.equals(QOP_AUTH_NAME) || this.mQop.equals(QOP_AUTH_INT_NAME)) {
            String rawRes = calculateHA1(this.mAlgorithm, this.mUsername, this.mRealm, this.mPassword, this.mNonce, this.mCnonce) + SEPARATOR + this.mNonce + SEPARATOR + this.mNc + SEPARATOR + this.mCnonce + SEPARATOR + this.mQop + SEPARATOR + calculateHA2(this.mQop, this.mMethod, this.mUri, this.mEntityBody);
            Log.i(TAG, "rawRes:" + rawRes);
            res = md5(getBytes(rawRes));
            Log.i(TAG, "response:" + res);
        } else if (this.mQop.equals("")) {
            String rawRes2 = calculateHA1(this.mAlgorithm, this.mUsername, this.mRealm, this.mPassword, this.mNonce, this.mCnonce) + SEPARATOR + this.mNonce + SEPARATOR + calculateHA2(this.mQop, this.mMethod, this.mUri, this.mEntityBody);
            Log.i(TAG, "rawRes:" + rawRes2);
            res = md5(getBytes(rawRes2));
            Log.i(TAG, "response:" + res);
        } else {
            Log.e(TAG, "Unsupported qop value, qop=" + this.mQop);
        }
        this.mResponse = res;
        return res;
    }

    private String calculateHA1(String algorithm, String username, String realm, String password, String nonce, String cnonce) {
        String a1Res;
        if (!SENLOG || TELDBG) {
            Log.i(TAG, "run calculateHA1:" + algorithm + "/" + username + "/" + realm + "/" + password + "/" + nonce + "/" + cnonce);
        } else {
            Log.i(TAG, "run calculateHA1:" + algorithm + "/[hidden]/" + realm + "/" + password + "/" + nonce + "/" + cnonce);
        }
        if (algorithm.endsWith("") || algorithm.endsWith(ALGORITHM_NAME_MD5)) {
            Log.i(TAG, "A = username: relam : password");
            String tmpStr = username + SEPARATOR + realm + SEPARATOR;
            a1Res = HexDump.toHexString(getBytes(tmpStr)) + password;
            Log.i(TAG, "a1Res:" + a1Res);
        } else {
            String tempStr = username + SEPARATOR + realm + SEPARATOR + password;
            a1Res = md5(getBytes(tempStr)) + SEPARATOR + nonce + SEPARATOR + cnonce;
        }
        Log.i(TAG, "a1:" + a1Res);
        String ha1Result = md5(HexDump.hexStringToByteArray(a1Res));
        Log.i(TAG, "ha1Result:" + ha1Result);
        return ha1Result;
    }

    private String calculateHA2(String qop, String method, String uri, String entityBody) {
        String a2Res = null;
        StringBuilder sb = new StringBuilder();
        sb.append("run calculateHA2:");
        sb.append(qop);
        sb.append("/");
        sb.append(method);
        sb.append("/");
        sb.append(!SENLOG ? uri : "[hidden]");
        sb.append("/");
        sb.append(entityBody);
        Log.i(TAG, sb.toString());
        if (this.mQop.equals("") || this.mQop.equals(QOP_AUTH_NAME)) {
            a2Res = method + SEPARATOR + uri;
        } else if (this.mQop.equals(QOP_AUTH_INT_NAME)) {
            a2Res = method + SEPARATOR + uri + SEPARATOR + md5(getBytes(entityBody));
            Log.i(TAG, "a2Res:" + a2Res);
        }
        Log.i(TAG, "a2:" + a2Res);
        String ha2Result = md5(getBytes(a2Res));
        Log.i(TAG, "ha2Result:" + ha2Result);
        return ha2Result;
    }

    private String md5(byte[] input) {
        String hRes = encode(this.mMd5Helper.digest(input));
        Log.i(TAG, "encode:" + hRes);
        return hRes;
    }

    private byte[] getBytes(String data) {
        try {
            Log.i(TAG, "getBytes:" + data + SEPARATOR + this.mCharSet);
            return data.getBytes(this.mCharSet);
        } catch (UnsupportedEncodingException ue) {
            ue.printStackTrace();
            return null;
        }
    }
}
