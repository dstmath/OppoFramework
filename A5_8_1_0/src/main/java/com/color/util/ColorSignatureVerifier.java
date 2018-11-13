package com.color.util;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.os.Binder;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.Log;
import com.google.android.collect.Sets;
import java.io.ByteArrayInputStream;
import java.security.MessageDigest;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Set;

public class ColorSignatureVerifier {
    private static final boolean DEBUG = SystemProperties.getBoolean("persist.sys.assert.panic", DEBUG);
    private static final String TAG = "SignatureVerifier";
    private static ColorSignatureUpdater mColorSignatureUpdater;
    private static Set<String> sWhiteList = Sets.newHashSet();

    public static void initUpdater(ColorSignatureUpdater updater) {
        mColorSignatureUpdater = updater;
    }

    public static boolean verificaionPass(Context context) {
        String packageName = getPackageForUid(context, Binder.getCallingUid());
        if (DEBUG) {
            Log.d(TAG, "verificaionPass packageName:" + packageName);
        }
        if (TextUtils.isEmpty(packageName)) {
            return DEBUG;
        }
        if (sWhiteList.contains(packageName)) {
            Log.d(TAG, "verificaionPass sWhiteList contain " + packageName);
            return true;
        } else if (!isSystemApp(context, packageName)) {
            return verifySignature(context, packageName);
        } else {
            sWhiteList.add(packageName);
            Log.d(TAG, "verificaionPass isSystemApp sWhiteList add " + packageName);
            return true;
        }
    }

    private static boolean isSystemApp(Context context, String packageName) {
        try {
            ApplicationInfo info = context.getPackageManager().getApplicationInfo(packageName, 0);
            if (!((info.flags & 1) == 0 && (info.flags & 128) == 0)) {
                Log.d(TAG, "isSystemApp true");
                return true;
            }
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return DEBUG;
    }

    private static boolean verifySignature(Context context, String packageName) {
        String signature = getMD5Signature(context, packageName);
        if (mColorSignatureUpdater == null) {
            Log.e(TAG, "should initUpdater first");
            return DEBUG;
        } else if (!mColorSignatureUpdater.getSignatures().contains(signature)) {
            return DEBUG;
        } else {
            Log.d(TAG, "verifySignature contains signature:" + signature);
            sWhiteList.add(packageName);
            return true;
        }
    }

    public static String getMD5Signature(Context context, String packageName) {
        Signature[] signatures = getSignatures(context, packageName);
        if (signatures == null) {
            Log.w(TAG, "sigutures is null");
            return null;
        }
        try {
            String md5HexString = null;
            try {
                try {
                    byte[] publicKey = MessageDigest.getInstance("MD5").digest(((X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(new ByteArrayInputStream(signatures[0].toByteArray()))).getEncoded());
                    StringBuilder sb = new StringBuilder();
                    for (byte digestByte : publicKey) {
                        sb.append(Integer.toHexString((digestByte & 255) | 256).substring(1, 3));
                    }
                    md5HexString = sb.toString();
                    Log.d(TAG, "getMD5Signature -- md5HexString = " + md5HexString);
                } catch (Exception e) {
                    Log.w(TAG, "getMD5Signature -- 3 error: " + e);
                }
                return md5HexString;
            } catch (CertificateException e2) {
                Log.w(TAG, "getMD5Signature -- 2 error: " + e2);
                return null;
            }
        } catch (CertificateException e22) {
            Log.w(TAG, "getMD5Signature -- 1 error: " + e22);
            return null;
        }
    }

    private static Signature[] getSignatures(Context context, String packageName) {
        if (context == null || packageName == null) {
            Log.w(TAG, "getSignatures packageName is null");
            return null;
        }
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(packageName, 64);
            if (packageInfo != null) {
                return packageInfo.signatures;
            }
            Log.w(TAG, "getSignatures packageInfo is null");
            return null;
        } catch (NameNotFoundException e) {
            Log.w(TAG, "getSignatures error: " + e);
            return null;
        }
    }

    private static String getPackageForUid(Context context, int uid) {
        PackageManager pm = context.getPackageManager();
        if (pm == null) {
            return null;
        }
        String[] packages = pm.getPackagesForUid(uid);
        if (packages == null || packages.length == 0) {
            return null;
        }
        return packages[0];
    }
}
