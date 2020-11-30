package com.oppo.enterprise.mdmcoreservice.certificate;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageParser;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.util.Slog;
import android.util.jar.StrictJarFile;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.util.IdentityHashMap;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.zip.ZipEntry;
import libcore.io.IoUtils;
import okhttp3.Call;
import okhttp3.Response;
import sun.security.pkcs.PKCS7;

public class OppoCertificateVerifier {
    private static final boolean DEBUG = (!RELEASE_TYPE || PANIC_TYPE);
    private static String OPPO_CERT_PUBKEY = (RELEASE_TYPE ? "-----BEGIN CERTIFICATE-----\nMIID5zCCAs+gAwIBAgIJALBj5twRqU2UMA0GCSqGSIb3DQEBBQUAMIGJMQswCQYD\nVQQGEwJDTjESMBAGA1UECAwJR3VhbmdEb25nMREwDwYDVQQHDAhEb25nR3VhbjEN\nMAsGA1UECgwET1BQTzEQMA4GA1UECwwHQ29sb3JPUzEUMBIGA1UEAwwLQW5kcm9p\nZFRlYW0xHDAaBgkqhkiG9w0BCQEWDW9wcG9Ab3Bwby5jb20wHhcNMTkxMTIyMDYx\nMDM3WhcNNDcwNDA5MDYxMDM3WjCBiTELMAkGA1UEBhMCQ04xEjAQBgNVBAgMCUd1\nYW5nRG9uZzERMA8GA1UEBwwIRG9uZ0d1YW4xDTALBgNVBAoMBE9QUE8xEDAOBgNV\nBAsMB0NvbG9yT1MxFDASBgNVBAMMC0FuZHJvaWRUZWFtMRwwGgYJKoZIhvcNAQkB\nFg1vcHBvQG9wcG8uY29tMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA\nmI4hwHXMjoLVyBVY09AldEk53XtLb3xsAaAMxbcnRokchGGf0DNJPX552Z5Ds2AW\nk4TW3OVc+QjcmcNwMN7RJ+y+hpwb5fM3jShIpfXB3vuwLaCXXRVYEX4KsaCfFjGq\ntWAbSD7NcXkAl47dZd0uG7j8ddx4tkGguECgQYwoO6gBxqbn0l1/SK6gtsT7RT7t\ntlDXkNsf1mnFQCPl945ZpMPlbf4yYtJnc2GzWcISXxb++6x/TCtSbw4iBOZe2IOy\n9B6byYKxxZtv03aJfqhfEiXF08brLhquYWCVcXCsL8lxIQlxGPbQCuj5O5dLxUg6\nrNhvEdZ3TNSsMfOpi02/RQIDAQABo1AwTjAdBgNVHQ4EFgQU0fZ1AzM2gWBHJ64z\nRwCpmH4nXbgwHwYDVR0jBBgwFoAU0fZ1AzM2gWBHJ64zRwCpmH4nXbgwDAYDVR0T\nBAUwAwEB/zANBgkqhkiG9w0BAQUFAAOCAQEAcUPowxvOu/AnYlNkLKdPW7Dv7qeQ\n5GG6UP5RmE2yTQIPMgyHKl6R12rFs9o3/smtnKYci2V4pjm41JOopbq8CJgiWg03\n8y5vDWzhR4ozI/X+M1O4xluUhfr6yGnfA3WMUKsRVYGCR1K+2KHu065JOfjsHHe1\nNHK2rKX+3rHRpQ9sK3uJDrvMhEXMtLJv18F58xwZCsjYFC7YX+Puw521fNXw0+Zl\ntPyoYeNkgVwejwt+3UYhnivPh0WnUgnXtW99e66PyiFb36h2udq2p3WjdIOr+vfA\n9SbVza6AKWbc9wqlKNQmfi+OYbG9vo52K/rhR3AYW2ZjSCoTlYLbYu21iw==\n-----END CERTIFICATE-----\n" : "-----BEGIN CERTIFICATE-----\nMIIEcjCCA1qgAwIBAgIJAMK9Z9Mw3+6RMA0GCSqGSIb3DQEBBQUAMIGCMQswCQYD\nVQQGEwJDTjERMA8GA1UECBMIRG9uZ0d1YW4xEDAOBgNVBAcTB0NoYW5nQW4xDTAL\nBgNVBAoTBE9QUE8xDDAKBgNVBAsTA1BMRjETMBEGA1UEAxMKU21hcnRQaG9uZTEc\nMBoGCSqGSIb3DQEJARYNb3Bwb0BvcHBvLmNvbTAeFw0xMjA3MTIwMzMzMzBaFw0z\nOTExMjgwMzMzMzBaMIGCMQswCQYDVQQGEwJDTjERMA8GA1UECBMIRG9uZ0d1YW4x\nEDAOBgNVBAcTB0NoYW5nQW4xDTALBgNVBAoTBE9QUE8xDDAKBgNVBAsTA1BMRjET\nMBEGA1UEAxMKU21hcnRQaG9uZTEcMBoGCSqGSIb3DQEJARYNb3Bwb0BvcHBvLmNv\nbTCCASAwDQYJKoZIhvcNAQEBBQADggENADCCAQgCggEBANWXxQyibMFN58L4wKnP\nT1iL4ZyhJ1wSoGxlbW9auLNatEx+CGgbrx2cIR2GZL/2uYsdB/m7TPn7b9RApIaH\nYoJvYl1c32Rjc0kpPL/78kd9Bm7e7ir3fgo6y5zF3B5O0tPY2iCvI1JRcuvx6vAC\nWLPQqeHg0EhjV6TAx/oXKGhYeQTV7BB4Br1k3oVTPNLR/ZD2ZlPNyIL9gTiyDrNt\n5Ehw+NQgAgqrwl5A+QZ/ly8B4QFZ1eIlL6B1ScfpoqbfB1mVYg0EkKeVez2xMP9f\nHlnGcgT9mSuKppsl08CHC7AaP3dsZBSwqMP2OJ/iJMV0peGLsz+bXJXQHfboXidY\nyKECAQOjgeowgecwHQYDVR0OBBYEFJuD1NnZn6HJdyJVOZFseaVuXfNWMIG3BgNV\nHSMEga8wgayAFJuD1NnZn6HJdyJVOZFseaVuXfNWoYGIpIGFMIGCMQswCQYDVQQG\nEwJDTjERMA8GA1UECBMIRG9uZ0d1YW4xEDAOBgNVBAcTB0NoYW5nQW4xDTALBgNV\nBAoTBE9QUE8xDDAKBgNVBAsTA1BMRjETMBEGA1UEAxMKU21hcnRQaG9uZTEcMBoG\nCSqGSIb3DQEJARYNb3Bwb0BvcHBvLmNvbYIJAMK9Z9Mw3+6RMAwGA1UdEwQFMAMB\nAf8wDQYJKoZIhvcNAQEFBQADggEBAEX0fI4xwGYZLAChi5XPxI1WW2tELEYXIviA\nSgapoVBzArgqtJbDw2VqbOmrTAt0ZICZnKnaD59uP+vzjhFu8jrj2/bSFe5zfaZO\n7UsoXprpkeJoWU6DLfGBzV+4LFrpw1eV3k/GqXl/+Hwu19lFnA9+4bux80nk9Qyp\nemsyOAxGxhi04nHrpw8V+4Q/TFsJAVx88RekdczM5pyfX6FoqaVo06r5vPpCC/Wa\nfotx34/Va2EqAXjBglbEi0ao9yBndqW22W4826MRZzOjm4knkGF5mQs7ONB5iaBv\nPVeROpIEE8y+JEarn/nlWwpXmB03PSKqwnkCQ+e/wuQLoiBJINY=\n-----END CERTIFICATE-----\n");
    private static final boolean PANIC_TYPE = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    private static final boolean RELEASE_TYPE = SystemProperties.getBoolean("ro.build.release_type", false);
    public static long REMOTE_VERIFICATION_POLLING_INTERVAL = 28800000;
    private static String mCertificateCachedPackageListDir = "/data/user_de/0/com.oppo.enterprise.mdmcoreservice/cache/cachedpackagelist.info";
    private static String mCertificateCachedVerifyResult = "/data/user_de/0/com.oppo.enterprise.mdmcoreservice/cache/cachedverity.info";
    private static Context mContext;
    private static boolean mForce_Do_remote_verify = false;
    private static boolean sEnvEnable = SystemProperties.getBoolean("persist.sys.testenv.enable", false);
    private static OppoCertificateVerifier sInstance = null;
    private boolean mBootCompleted = false;
    private long mBootTime = -1;
    private HashMap<String, RemoteVerificationResult> mCachedVerificationResult = new HashMap<>();
    private ConnectivityManager mConnectivityManager = null;
    private ArrayList<String> mControlledPermissions = new ArrayList<>();
    private VerityHandler mHandler;
    private String mImei = "";
    private Object mLock = new Object();
    private HashMap<String, String> mOppoCertificatePackageList = new HashMap<>();
    private HashMap<String, OppoCertificateInfo> mOppoCertificates = new HashMap<>();
    private OppoHttpClient mOppoHttpClient = new OppoHttpClient();
    private PackageManager mPackageManager;
    private int mPostBootRetryCount = 0;
    private VerityBroadcastReceiver mReceiver = new VerityBroadcastReceiver();
    private boolean mSystemReady = false;
    private HandlerThread mThread;
    private Object mVLock = new Object();
    private VerificationThread mVThread;

    public static class CerResponse {
        public Integer code;
        public CerResult data;
        public String msg;
    }

    public static class CerResult {
        public Long fromTime;
        public Long serverTime;
        public Integer status;
        public Long toTime;
    }

    public static class TimerResponse {
        public Integer code;
        public Long data;
        public String msg;
    }

    public static OppoCertificateVerifier getInstance(Context context) {
        if (sInstance != null) {
            return sInstance;
        }
        sInstance = new OppoCertificateVerifier(context);
        return sInstance;
    }

    private void controlledPermissionsInit() {
        this.mControlledPermissions.add("com.oppo.permission.sec.MDM_PHONE_MANAGER");
        this.mControlledPermissions.add("com.oppo.permission.sec.MDM_CRITICAL");
        restoreCertificatePermissionsFromFile("/system/etc/cerp.cfg", this.mControlledPermissions);
        restoreCertificatePermissionsFromFile("/custom/etc/cerp.cfg", this.mControlledPermissions);
        if (DEBUG) {
            restoreCertificatePermissionsFromFile("/data/etc/cerp.cfg", this.mControlledPermissions);
        }
    }

    public OppoCertificateVerifier(Context context) {
        Log.d("OppoCertificateVerifier", "OppoCertificateVerifier init start...");
        long fixValue = (long) (Math.random() * 1.44E7d);
        if (DEBUG) {
            Date now = new Date();
            now.setTime(now.getTime() + REMOTE_VERIFICATION_POLLING_INTERVAL + fixValue);
            Log.d("OppoCertificateVerifier", "random setting polling inverval with fix value---> @" + now);
        }
        REMOTE_VERIFICATION_POLLING_INTERVAL += fixValue;
        mContext = context;
        this.mConnectivityManager = (ConnectivityManager) mContext.getSystemService("connectivity");
        this.mPackageManager = mContext.getPackageManager();
        this.mImei = getImei();
        this.mThread = new HandlerThread("OppoCertificateVerifier");
        this.mThread.start();
        if (this.mThread.getLooper() != null) {
            this.mHandler = new VerityHandler(this.mThread.getLooper());
        }
        controlledPermissionsInit();
        registerBroadcastForCertificate();
        this.mVThread = new VerificationThread("OppoCertificateVerifierV");
        this.mVThread.start();
        getPackageListFromCachedFile();
    }

    public boolean checkPermission(String pkgName, String perm) {
        synchronized (this.mLock) {
            if (skipCheckPermission(pkgName)) {
                Log.d("OppoCertificateVerifier", "core app debug is on, always return true");
                return true;
            }
            ControlledPermissions p = ControlledPermissions.createFromString(perm);
            if (p != null) {
                return checkPermission(pkgName, p.apiList, p.perm);
            }
            Log.d("OppoCertificateVerifier", "Check perm:" + perm + " for package " + pkgName + "failed.It can not find the perm in system!");
            return false;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:7:0x001a  */
    private boolean isVerifyServerSwitch(String pkgName, ArrayList<String> apiList, String perm) {
        if (pkgName.equals("com.oppotest.testmdm") && perm.equals("com.oppo.permission.sec.MDM_CRITICAL")) {
            Iterator<String> it = apiList.iterator();
            while (it.hasNext()) {
                String api = it.next();
                if (api.equals("2111") || api.equals("2112")) {
                    if (!DEBUG) {
                        return true;
                    }
                    Log.w("OppoCertificateVerifier", "isVerifyServerSwitch true for " + pkgName + " calling api" + api + " use permission" + perm);
                    return true;
                }
                while (it.hasNext()) {
                }
            }
        }
        if (!DEBUG) {
            return false;
        }
        Log.w("OppoCertificateVerifier", "isVerifyServerSwitch false");
        return false;
    }

    private boolean checkPermission(String pkgName, ArrayList<String> apiList, String perm) {
        synchronized (this.mLock) {
            OppoCertificateInfo info = this.mOppoCertificates.get(pkgName);
            if (info == null) {
                Log.d("OppoCertificateVerifier", "The certificated system not contain package:" + pkgName);
                return false;
            } else if (isVerifyServerSwitch(pkgName, apiList, perm)) {
                return true;
            } else {
                Date now = getServerTimeStampNoSync();
                if (apiList != null && apiList.size() > 0) {
                    Iterator<String> it = apiList.iterator();
                    while (it.hasNext()) {
                        String api = it.next();
                        if (!info.canCallAPI(api, perm, now)) {
                            Log.w("OppoCertificateVerifier", "checkPermission failed for " + pkgName + " calling api" + api + " use permission" + perm);
                            return false;
                        }
                    }
                    return true;
                } else if (info.havePermission(perm, true, now)) {
                    return true;
                } else {
                    Log.w("OppoCertificateVerifier", "checkPermission failed for " + pkgName + " use permission" + perm);
                    return false;
                }
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0022, code lost:
        if (com.oppo.enterprise.mdmcoreservice.certificate.OppoCertificateVerifier.DEBUG == false) goto L_?;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0024, code lost:
        android.util.Log.d("OppoCertificateVerifier", "isPackageContainsOppoCertificates return false");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:?, code lost:
        return false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:?, code lost:
        return false;
     */
    public boolean isPackageContainsOppoCertificates(String pkgName) {
        synchronized (this.mLock) {
            if ((this.mOppoCertificates != null && this.mOppoCertificates.containsKey(pkgName)) || skipCheckPermission(pkgName)) {
                Log.d("OppoCertificateVerifier", "mOppoCertificates isPackageContainsOppoCertificates return true");
                return true;
            }
        }
    }

    public boolean isCertificateControlledPermission(String perm) {
        synchronized (this.mLock) {
            ControlledPermissions p = ControlledPermissions.createFromString(perm);
            if (p == null) {
                return false;
            }
            return this.mControlledPermissions.contains(p.perm);
        }
    }

    private void updateOppoCertificateInfo(OppoCertificateInfo info, String codePath) {
        synchronized (this.mLock) {
            if (this.mCachedVerificationResult.containsKey(info.packageName)) {
                this.mCachedVerificationResult.remove(info.packageName);
                storeCachedVerificationResultToFile();
                Log.d("OppoCertificateVerifier", "remving cached result for " + info.packageName + " due to pakageUpdated");
            }
            this.mOppoCertificates.put(info.packageName, info);
            if (!isAlreadyInCertificatePackageList(info.packageName)) {
                this.mOppoCertificatePackageList.put(info.packageName, codePath);
                storeCachedCertificatePackageToFile();
            }
            if (info.isTypeOnline()) {
                scheduleRemoteVerificationIfNetworkAvaliable();
            }
        }
    }

    public void removeOppoCertificateInfoForPackage(String packageName) {
        synchronized (this.mLock) {
            if (this.mOppoCertificates.get(packageName) != null) {
                if (this.mCachedVerificationResult.containsKey(packageName)) {
                    this.mCachedVerificationResult.remove(packageName);
                    Log.d("OppoCertificateVerifier", "remving cached result for " + packageName + " due to packageRemoved");
                    storeCachedVerificationResultToFile();
                }
                this.mOppoCertificates.remove(packageName);
                this.mOppoCertificatePackageList.remove(packageName);
                storeCachedCertificatePackageToFile();
            }
        }
    }

    public int checkOppoCertificateIfNeeded(PackageInfo packageInfo) {
        try {
            return checkOppoCertificateIfNeededInternal(packageInfo.applicationInfo.sourceDir, packageInfo.packageName);
        } catch (Exception e) {
            Log.w("OppoCertificateVerifier", "exception while checking MDM:");
            e.printStackTrace();
            return -1;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:83:0x0208  */
    /* JADX WARNING: Removed duplicated region for block: B:90:0x0234  */
    private int checkOppoCertificateIfNeededInternal(String codePath, String packageName) {
        String[] apiList;
        OppoCertificateInfo info;
        String origHash;
        CertificateResult res = new CertificateResult();
        int ret = getOppoCertificateInfo(codePath, "entcert", res);
        if (ret == 1) {
            return -1;
        }
        if (ret == -1) {
            Log.d("OppoCertificateVerifier", "can't verify pkg " + packageName + " due to cert signature not match.");
            return 0;
        } else if (res.bytes == null) {
            Log.d("OppoCertificateVerifier", "can't verify pkg " + packageName + " due to no entcert exist ");
            return 0;
        } else {
            String cert = new String(res.bytes);
            if (!validCertificate(cert)) {
                Log.d("OppoCertificateVerifier", "invalid certificate, some properties not exist.");
                return 0;
            }
            if (DEBUG) {
                Log.d("OppoCertificateVerifier", "loading certificates: " + cert);
            }
            Properties props = new Properties();
            try {
                props.load(new ByteArrayInputStream(cert.getBytes()));
            } catch (IOException e) {
            }
            String pkgName = props.getProperty("PackageName");
            if (DEBUG) {
                Log.d("OppoCertificateVerifier", "pkgName: " + pkgName + ";packageName:" + packageName);
            }
            if (!packageName.equals(pkgName)) {
                if (DEBUG) {
                    Log.d("OppoCertificateVerifier", "invalid certificate, invalid packageName");
                }
                return 0;
            }
            int index = cert.indexOf("Signature");
            if (index == -1) {
                if (DEBUG) {
                    Log.d("OppoCertificateVerifier", "invalid certificate, invalid content");
                }
                return 0;
            }
            String content = cert.substring(0, index);
            String signature = props.getProperty("Signature");
            if (!checkSignature(content, signature, OPPO_CERT_PUBKEY)) {
                Log.w("OppoCertificateVerifier", "invalid certificate, invalid signature");
                return 0;
            }
            String apkHash = props.getProperty("ApkHash");
            String deviceIDs = props.getProperty("DeviceIds");
            String permissions = props.getProperty("Permissions");
            String apis = props.getProperty("Apis");
            String licenceCode = props.getProperty("LicenceCode");
            String validFrom = props.getProperty("ValidFrom");
            String validTo = props.getProperty("ValidTo");
            String type = props.getProperty("Type");
            if (apkHash != null) {
                if ("*".equals(apkHash.trim()) && "*".equals(deviceIDs.trim())) {
                    Log.w("OppoCertificateVerifier", "invalid certificate, both apkhash & deviceid are *");
                    return 0;
                }
            }
            ManifestDigest digest = getManifestDigest_V2(codePath);
            if (digest == null) {
                Log.w("OppoCertificateVerifier", "invalid certificate, Failed to get META-INF/MANIFEST.MF");
                return 0;
            } else if (apkHash == null || "*".equals(apkHash.trim()) || (origHash = digest.getHash()) == null || origHash.equals(apkHash)) {
                boolean imeiChecked = false;
                String[] devices = deviceIDs.split(";");
                if (!TextUtils.isEmpty(this.mImei)) {
                    if (!validateDeviceIDs(devices)) {
                        Log.w("OppoCertificateVerifier", "invalid certificate, IMEI isn't in the device list");
                        return 0;
                    }
                    imeiChecked = true;
                }
                if (permissions.equals("")) {
                    Log.w("OppoCertificateVerifier", "invalid certificate, no permission found");
                    return 0;
                }
                String[] perms = permissions.split(";");
                if (apis != null) {
                    if (!apis.equals("")) {
                        apiList = apis.split(";");
                        if (type == null) {
                            type = "online";
                        }
                        info = new OppoCertificateInfo(pkgName, licenceCode, perms, apiList, devices, validFrom, validTo, apkHash, signature, type);
                        if (imeiChecked && !info.isTypeOnline()) {
                            if (DEBUG) {
                                Log.d("OppoCertificateVerifier", "mark offline certificate for " + pkgName + " as verity sucess");
                            }
                            info.setVerityStatus(1);
                        }
                        updateOppoCertificateInfo(info, codePath);
                        Log.d("OppoCertificateVerifier", "sucessfully add certificate for " + pkgName);
                        return 1;
                    }
                }
                apiList = null;
                if (type == null) {
                }
                info = new OppoCertificateInfo(pkgName, licenceCode, perms, apiList, devices, validFrom, validTo, apkHash, signature, type);
                if (DEBUG) {
                }
                info.setVerityStatus(1);
                updateOppoCertificateInfo(info, codePath);
                Log.d("OppoCertificateVerifier", "sucessfully add certificate for " + pkgName);
                return 1;
            } else {
                Log.w("OppoCertificateVerifier", "invalid certificate, Package has mismatched apk hash: expected " + apkHash + ", got " + origHash);
                return 0;
            }
        }
    }

    private String getImei() {
        String imei = "";
        try {
            TelephonyManager telephonyManager = (TelephonyManager) mContext.getSystemService("phone");
            if (telephonyManager != null) {
                imei = telephonyManager.getImei(0);
                if (TextUtils.isEmpty(imei)) {
                    Log.d("OppoCertificateVerifier", "can not get imei from telephonyManager!");
                }
            } else {
                Log.d("OppoCertificateVerifier", "telePhony is not ready!");
            }
        } catch (Exception e) {
        }
        if (TextUtils.isEmpty(imei)) {
            String temp = SystemProperties.get("oppo.device.imeicache", "");
            if (!"".equals(temp)) {
                String[] imeis = temp.split(",");
                if (imeis.length >= 1) {
                    imei = imeis[0];
                }
            }
            if (TextUtils.isEmpty(imei)) {
                Log.d("OppoCertificateVerifier", "can not get imei from property!");
            }
        }
        return imei;
    }

    private boolean validateDeviceIDs(List<String> deviceIDs) {
        String[] array = new String[deviceIDs.size()];
        for (int i = 0; i < deviceIDs.size(); i++) {
            array[i] = deviceIDs.get(i);
        }
        return validateDeviceIDs(array);
    }

    private boolean validateDeviceIDs(String[] deviceIDs) {
        synchronized (this.mLock) {
            if (deviceIDs == null) {
                try {
                    return false;
                } catch (Throwable th) {
                    throw th;
                }
            } else if (deviceIDs.length == 1 && "*".equals(deviceIDs[0].trim())) {
                return true;
            } else {
                if ("".equals(this.mImei)) {
                    Log.w("OppoCertificateVerifier", "can't get IMEI");
                    return false;
                }
                for (String deviceid : deviceIDs) {
                    if (this.mImei.equals(deviceid)) {
                        return true;
                    }
                }
                return false;
            }
        }
    }

    private void restoreCertificatePermissionsFromFile(String filePath, ArrayList<String> list) {
        File file = new File(filePath);
        try {
            if (file.exists()) {
                BufferedReader reader = new BufferedReader(new FileReader(file));
                while (true) {
                    String line = reader.readLine();
                    if (line != null) {
                        String per = line.trim();
                        if (!"".equals(per)) {
                            synchronized (this.mLock) {
                                if (!list.contains(per)) {
                                    list.add(per);
                                    if (DEBUG) {
                                        Log.d("OppoCertificateVerifier", "add permission: " + line + " to certificate-controlled");
                                    }
                                }
                            }
                        }
                    } else {
                        reader.close();
                        return;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void restoreCachedCertificatePackageFromFile() {
        File cacheFile = new File(mCertificateCachedPackageListDir);
        try {
            if (cacheFile.exists()) {
                FileInputStream ins = new FileInputStream(mCertificateCachedPackageListDir);
                Properties props = new Properties();
                try {
                    props.loadFromXML(ins);
                } catch (IOException e) {
                    Log.w("OppoCertificateVerifier", "loadFromXML" + mCertificateCachedPackageListDir + "error" + e);
                    ins.close();
                } catch (Throwable th) {
                    ins.close();
                    throw th;
                }
                ins.close();
                for (Object o : props.keySet()) {
                    String temp = props.getProperty((String) o);
                    synchronized (this.mLock) {
                        Log.d("OppoCertificateVerifier", " restore pakcagename:" + ((String) o) + ";codepath:" + temp.trim());
                        this.mOppoCertificatePackageList.put((String) o, temp.trim());
                    }
                }
            }
        } catch (Exception e2) {
            e2.printStackTrace();
            try {
                cacheFile.delete();
            } catch (Exception e3) {
            }
        }
    }

    private void storeCachedCertificatePackageToFile() {
        HashMap<String, String> temp;
        try {
            FileOutputStream out = new FileOutputStream(mCertificateCachedPackageListDir);
            Properties props = new Properties();
            synchronized (this.mLock) {
                temp = (HashMap) this.mOppoCertificatePackageList.clone();
            }
            if (temp != null) {
                for (String key : temp.keySet()) {
                    if (DEBUG) {
                        Log.d("OppoCertificateVerifier", " store pakcagename:" + key + ";codepath:" + temp.get(key));
                    }
                    props.setProperty(key, temp.get(key));
                }
            }
            props.storeToXML(out, "OppoCertificateVerifier");
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void restoreCachedVerificationResultFromFile() {
        File cacheFile = new File(mCertificateCachedVerifyResult);
        try {
            if (cacheFile.exists()) {
                FileInputStream ins = new FileInputStream(mCertificateCachedVerifyResult);
                Properties props = new Properties();
                try {
                    props.loadFromXML(ins);
                } catch (IOException e) {
                    Log.w("OppoCertificateVerifier", "loadFromXML" + mCertificateCachedVerifyResult + "error" + e);
                    ins.close();
                } catch (Throwable th) {
                    ins.close();
                    throw th;
                }
                ins.close();
                for (Object o : props.keySet()) {
                    RemoteVerificationResult r = RemoteVerificationResult.fromString(props.getProperty((String) o).trim());
                    if (r != null) {
                        synchronized (this.mLock) {
                            this.mCachedVerificationResult.put((String) o, r);
                        }
                    }
                }
            }
        } catch (Exception e2) {
            e2.printStackTrace();
            try {
                cacheFile.delete();
            } catch (Exception e3) {
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void storeCachedVerificationResultToFile() {
        HashMap<String, RemoteVerificationResult> temp;
        new File(mCertificateCachedVerifyResult);
        try {
            FileOutputStream out = new FileOutputStream(mCertificateCachedVerifyResult);
            Properties props = new Properties();
            synchronized (this.mLock) {
                temp = (HashMap) this.mCachedVerificationResult.clone();
            }
            if (temp != null) {
                for (String key : temp.keySet()) {
                    props.setProperty(key, temp.get(key).toString());
                }
            }
            props.storeToXML(out, "OppoCertificateVerifier");
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean validCertificate(String cert) {
        return cert.contains("PackageName:") && cert.contains("LicenceCode:") && cert.contains("Permissions:") && cert.contains("DeviceIds:") && cert.contains("ValidFrom:") && cert.contains("ValidTo:") && cert.contains("ApkHash:") && cert.contains("Signature:");
    }

    private boolean checkSignature(String content, String signature, String pub) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update(content.getBytes());
            byte[] outputDigestVerify = messageDigest.digest();
            PublicKey key = ((X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(new ByteArrayInputStream(pub.getBytes()))).getPublicKey();
            byte[] decode = Base64.decode(signature, 0);
            Signature sig = Signature.getInstance("SHA256withRSA");
            sig.initVerify(key);
            sig.update(outputDigestVerify);
            return sig.verify(decode);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static boolean verifyPublicKey(X509Certificate cert, String pubkey) {
        try {
            boolean match = Base64.encodeToString(((X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(new ByteArrayInputStream(pubkey.getBytes()))).getPublicKey().getEncoded(), 0).trim().equals(Base64.encodeToString(cert.getPublicKey().getEncoded(), 0).trim());
            if (DEBUG) {
                Log.d("OppoCertificateVerifier", "verifyPublicKey --->" + match);
            }
            return match;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:38:0x00ba A[Catch:{ IOException | RuntimeException -> 0x00f2 }] */
    /* JADX WARNING: Removed duplicated region for block: B:40:0x00bf A[Catch:{ IOException | RuntimeException -> 0x00f2 }] */
    /* JADX WARNING: Removed duplicated region for block: B:43:0x00c8 A[Catch:{ IOException | RuntimeException -> 0x00f2 }] */
    /* JADX WARNING: Removed duplicated region for block: B:45:0x00cd A[Catch:{ IOException | RuntimeException -> 0x00f2 }] */
    /* JADX WARNING: Removed duplicated region for block: B:52:0x00db A[Catch:{ IOException | RuntimeException -> 0x00f2 }] */
    /* JADX WARNING: Removed duplicated region for block: B:54:0x00e0 A[Catch:{ IOException | RuntimeException -> 0x00f2 }] */
    /* JADX WARNING: Removed duplicated region for block: B:57:0x00e9 A[Catch:{ IOException | RuntimeException -> 0x00f2 }] */
    /* JADX WARNING: Removed duplicated region for block: B:59:0x00ee A[Catch:{ IOException | RuntimeException -> 0x00f2 }] */
    /* JADX WARNING: Removed duplicated region for block: B:68:0x0101  */
    private static int getOppoCertificateInfo(String codePath, String contentInfoName, CertificateResult res) {
        Exception e;
        Throwable th;
        ZipEntry rsa;
        String tempFileName = "/data/user_de/0/com.oppo.enterprise.mdmcoreservice/cache/ent.temp." + SystemClock.elapsedRealtime();
        int ret = 1;
        try {
            try {
                StrictJarFile jarFile = new StrictJarFile(codePath, false, false);
                StrictJarFile childJar = null;
                FileOutputStream fileOutputStream = null;
                InputStream rsaIns = null;
                File tempFile = new File(tempFileName);
                if (tempFile.exists()) {
                    tempFile.delete();
                }
                try {
                    ZipEntry je = jarFile.findEntry("META-INF/OPPOENT.CER");
                    if (je != null) {
                        if (DEBUG) {
                            Log.d("OppoCertificateVerifier", "jarFile is not null, tempFileName is :" + tempFileName);
                        }
                        ret = -1;
                        byte[] bytes = inputStream2ByteArray(jarFile.getInputStream(je));
                        fileOutputStream = new FileOutputStream(tempFileName);
                        fileOutputStream.write(bytes);
                        fileOutputStream.flush();
                        childJar = new StrictJarFile(tempFileName, true, false);
                        try {
                            ZipEntry childJe = childJar.findEntry(contentInfoName);
                            if (!(childJe == null || (rsa = childJar.findEntry("META-INF/PLATFORM.RSA")) == null)) {
                                rsaIns = childJar.getInputStream(rsa);
                                X509Certificate[] pkgCerts = new PKCS7(rsaIns).getCertificates();
                                if (pkgCerts != null && verifyPublicKey(pkgCerts[0], OPPO_CERT_PUBKEY)) {
                                    try {
                                        res.bytes = inputStream2ByteArray(childJar.getInputStream(childJe));
                                        ret = 0;
                                        jarFile.close();
                                        if (rsaIns != null) {
                                            rsaIns.close();
                                        }
                                        if (childJar != null) {
                                            childJar.close();
                                        }
                                        if (tempFile.exists()) {
                                            tempFile.delete();
                                        }
                                        if (fileOutputStream != null) {
                                            fileOutputStream.close();
                                        }
                                        return ret;
                                    } catch (Throwable th2) {
                                        th = th2;
                                        jarFile.close();
                                        if (rsaIns != null) {
                                        }
                                        if (childJar != null) {
                                        }
                                        if (tempFile.exists()) {
                                        }
                                        if (fileOutputStream != null) {
                                        }
                                        throw th;
                                    }
                                }
                            }
                        } catch (Throwable th3) {
                            th = th3;
                            jarFile.close();
                            if (rsaIns != null) {
                            }
                            if (childJar != null) {
                            }
                            if (tempFile.exists()) {
                            }
                            if (fileOutputStream != null) {
                            }
                            throw th;
                        }
                    }
                    try {
                        jarFile.close();
                        if (rsaIns != null) {
                        }
                        if (childJar != null) {
                        }
                        if (tempFile.exists()) {
                        }
                        if (fileOutputStream != null) {
                        }
                    } catch (IOException | RuntimeException e2) {
                        e = e2;
                        if (DEBUG) {
                        }
                        return ret;
                    }
                } catch (Throwable th4) {
                    th = th4;
                    jarFile.close();
                    if (rsaIns != null) {
                        rsaIns.close();
                    }
                    if (childJar != null) {
                        childJar.close();
                    }
                    if (tempFile.exists()) {
                        tempFile.delete();
                    }
                    if (fileOutputStream != null) {
                        fileOutputStream.close();
                    }
                    throw th;
                }
            } catch (IOException | RuntimeException e3) {
                e = e3;
                if (DEBUG) {
                    e.printStackTrace();
                }
                return ret;
            }
        } catch (IOException | RuntimeException e4) {
            e = e4;
            if (DEBUG) {
            }
            return ret;
        }
        return ret;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handlePostBootVertification() {
        Log.d("OppoCertificateVerifier", "handlePostBootVertification...");
        synchronized (this.mLock) {
            if (!this.mBootCompleted) {
                if (TextUtils.isEmpty(this.mImei)) {
                    Log.d("OppoCertificateVerifier", "mImei is empty, try to get it");
                    this.mImei = getImei();
                }
                if (TextUtils.isEmpty(this.mImei)) {
                    Log.d("OppoCertificateVerifier", "can't get deviceid, try again later....");
                    if (this.mPostBootRetryCount < 1000) {
                        this.mHandler.sendEmptyMessageDelayed(2, 2000);
                    } else {
                        Log.w("OppoCertificateVerifier", "can't verify current device.....");
                    }
                    return;
                }
                restoreCachedVerificationResultFromFile();
                ArrayList<String> invalidList = new ArrayList<>();
                for (String key : this.mOppoCertificates.keySet()) {
                    OppoCertificateInfo info = this.mOppoCertificates.get(key);
                    if (!validateDeviceIDs(info.getSupportDeviceIDs())) {
                        Log.w("OppoCertificateVerifier", "certificate" + info + " does not suppprt current Device");
                        invalidList.add(info.packageName);
                    } else {
                        if (DEBUG) {
                            Log.d("OppoCertificateVerifier", "certificate " + info + "IMEI valide sucess.");
                        }
                        if (!info.isTypeOnline()) {
                            info.setVerityStatus(1);
                        } else {
                            RemoteVerificationResult r = this.mCachedVerificationResult.get(info.packageName);
                            if (r != null) {
                                info.setVerityStatus(r.result);
                                info.setLastVerityTime(r.timeStamp);
                                if (DEBUG) {
                                    Log.d("OppoCertificateVerifier", "restore verificationresult for " + info.packageName + " status :" + OppoCertificateInfo.verityStatusToString(r.result) + "@ " + r.timeStamp);
                                }
                                if (r.result == 1 && r.haveValidFromTo()) {
                                    if (DEBUG) {
                                        Log.d("OppoCertificateVerifier", "updating cerficate validdate :" + info.packageName + "from " + r.validFrom + " to " + r.validTo);
                                    }
                                    info.updateFromToDate(r.validFrom, r.validTo);
                                }
                            }
                        }
                    }
                }
                Iterator<String> it = invalidList.iterator();
                while (it.hasNext()) {
                    String packageName = it.next();
                    this.mOppoCertificates.remove(packageName);
                    this.mOppoCertificatePackageList.remove(packageName);
                    storeCachedCertificatePackageToFile();
                }
                this.mBootCompleted = true;
                scheduleRemoteVerificationIfNetworkAvaliable();
                scheduleRemoteVerification(REMOTE_VERIFICATION_POLLING_INTERVAL, true, true, REMOTE_VERIFICATION_POLLING_INTERVAL);
            }
        }
    }

    private void scheduleRemoteVerification(boolean resetTryCount) {
        scheduleRemoteVerification(0, resetTryCount, false, 0);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void scheduleRemoteVerification(long delay, boolean resetTryCount) {
        scheduleRemoteVerification(delay, resetTryCount, false, 0);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void scheduleRemoteVerification(long delay, boolean resetTryCount, boolean polling, long pollingInterval) {
        if (this.mBootCompleted) {
            Message msg = Message.obtain();
            msg.what = 1;
            VerificationParam param = new VerificationParam();
            param.resetTryCount = resetTryCount;
            param.polling = polling;
            param.pollingInterval = pollingInterval;
            msg.obj = param;
            this.mHandler.sendMessageDelayed(msg, delay);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void schedulePackageChanged(int type, String packageName) {
        Message msg = Message.obtain();
        msg.what = type;
        msg.obj = packageName;
        this.mHandler.sendMessage(msg);
    }

    private void wakeVerificationThread() {
        if (DEBUG) {
            Log.d("OppoCertificateVerifier", "wakingUp VThread");
        }
        synchronized (this.mVLock) {
            this.mVLock.notify();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void wakeVerificationThread(boolean restRetry) {
        if (restRetry) {
            this.mVThread.resetTryCount();
        }
        wakeVerificationThread();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private RemoteVerificationResult doRemoteVerification(OppoCertificateInfo info) {
        CerResponse res = callRemote(this.mImei, info.licenceCode, null);
        if (res == null) {
            Log.e("OppoCertificateVerifier", "can't get res...");
        } else if (res.code.intValue() == 10200) {
            return RemoteVerificationResult.fromCerResult(res.data);
        } else {
            if (res.code.intValue() == 10400) {
                Log.e("OppoCertificateVerifier", "error :" + res.msg);
            } else {
                Log.e("OppoCertificateVerifier", "unknow error...");
            }
        }
        return new RemoteVerificationResult();
    }

    public void setTestServerState(boolean enable) {
        if (DEBUG) {
            Log.d("OppoCertificateVerifier", "setTestServerState: " + enable);
        }
        Date now = new Date(new Date().getTime() - 28800000);
        OppoCertificateInfo org2 = this.mOppoCertificates.get("com.oppotest.testmdm");
        if (org2 != null) {
            org2.setVerityStatus(5);
            org2.setLastVerityTime(now);
        }
        sEnvEnable = enable;
        mForce_Do_remote_verify = true;
        scheduleRemoteVerificationIfNetworkAvaliable();
    }

    public boolean getTestServerState() {
        return sEnvEnable;
    }

    private CerResponse callRemote(String imei, String licenseCode, String openid) {
        Log.e("OppoCertificateVerifier", "callRemote START");
        OppoCertificateVerifyOkHttpHelper mHelper = OppoCertificateVerifyOkHttpHelper.getInstance(mContext);
        mHelper.initParam(imei, licenseCode, openid);
        Call call = mHelper.getSponse(sEnvEnable, 1);
        if (call == null) {
            Log.d("OppoCertificateVerifier", "call is null");
            return null;
        }
        Response response = call.execute();
        if (response == null) {
            Log.d("OppoCertificateVerifier", "response is null");
            return null;
        }
        String jsonStr = response.body().string();
        if (TextUtils.isEmpty(jsonStr)) {
            return null;
        }
        try {
            CerResponse mCerResponse = (CerResponse) JSONObject.parseObject(jsonStr, CerResponse.class);
            if (DEBUG) {
                Slog.d("OppoCertificateVerifier", "OppoCertificateVerifyOkHttpHelper mCerResponse.code: " + mCerResponse.code + ";mCerResponse.msg: " + mCerResponse.msg + ";fromTime: " + mCerResponse.data.fromTime + "toTime:" + mCerResponse.data.toTime);
            }
            if (!DEBUG || mCerResponse.data.fromTime == null || mCerResponse.data.toTime == null) {
                return mCerResponse;
            }
            Slog.d("OppoCertificateVerifier", "fromTime:" + UTCToString(mCerResponse.data.fromTime.longValue()) + ";toTime:" + UTCToString(mCerResponse.data.toTime.longValue()));
            return mCerResponse;
        } catch (JSONException | NullPointerException e) {
            try {
                e.printStackTrace();
                return null;
            } catch (IOException | NullPointerException e2) {
                e2.printStackTrace();
            } catch (Throwable th) {
            }
        }
        return null;
    }

    private void registerBroadcastForCertificate() {
        Log.d("OppoCertificateVerifier", "registerBroadcastForCertificate...");
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        filter.setPriority(1000);
        mContext.registerReceiver(this.mReceiver, filter, null, this.mHandler);
        IntentFilter filterpackagechange = new IntentFilter();
        filterpackagechange.addAction("android.intent.action.PACKAGE_ADDED");
        filterpackagechange.addAction("android.intent.action.PACKAGE_REMOVED");
        filterpackagechange.addDataScheme("package");
        filterpackagechange.setPriority(1000);
        mContext.registerReceiver(this.mReceiver, filterpackagechange, null, this.mHandler);
    }

    /* access modifiers changed from: private */
    public class VerityHandler extends Handler {
        public VerityHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            try {
                int i = msg.what;
                if (i != 6) {
                    switch (i) {
                        case 0:
                            return;
                        case 1:
                            if (msg.obj != null) {
                                VerificationParam param = (VerificationParam) msg.obj;
                                Log.d("OppoCertificateVerifier", "MSG_TRIGGER_REMOTE_VERIFICATION param.resetTryCount: " + param.resetTryCount);
                                OppoCertificateVerifier.this.wakeVerificationThread(param.resetTryCount);
                                if (param.polling) {
                                    OppoCertificateVerifier.this.scheduleRemoteVerification(param.pollingInterval, param.resetTryCount, true, param.pollingInterval);
                                    return;
                                }
                                return;
                            }
                            return;
                        case 2:
                            OppoCertificateVerifier.this.handlePostBootVertification();
                            return;
                        case 3:
                            if (msg.obj != null) {
                                String packageName = (String) msg.obj;
                                try {
                                    if (OppoCertificateVerifier.this.isPackageManagerAvailable()) {
                                        PackageInfo packageinfo = OppoCertificateVerifier.this.mPackageManager.getPackageInfo(packageName, 0);
                                        Log.d("OppoCertificateVerifier", "Add packages name is :" + packageinfo.packageName + ";dir:" + packageinfo.applicationInfo.sourceDir);
                                        OppoCertificateVerifier.this.checkOppoCertificateIfNeeded(packageinfo);
                                        return;
                                    }
                                    Log.d("OppoCertificateVerifier", "mPackageManager is null return");
                                    return;
                                } catch (PackageManager.NameNotFoundException e) {
                                    Log.d("OppoCertificateVerifier", "Failed to find package with package name: " + packageName);
                                    return;
                                }
                            } else {
                                return;
                            }
                        case 4:
                            if (msg.obj != null) {
                                String packageName2 = (String) msg.obj;
                                Log.d("OppoCertificateVerifier", "Remove package name:" + packageName2);
                                OppoCertificateVerifier.this.removeOppoCertificateInfoForPackage(packageName2);
                                return;
                            }
                            return;
                        default:
                            return;
                    }
                } else {
                    Log.d("OppoCertificateVerifier", "MSG_GETPACKAGE_RETRY");
                    OppoCertificateVerifier.this.getPackageListFromCachedFile();
                }
            } catch (Exception e2) {
                Log.w("OppoCertificateVerifier", "exception while handlemsg:");
                e2.printStackTrace();
            }
        }
    }

    private Date getServerTimeStampNoSync() {
        Date now = new Date();
        if (this.mBootTime != -1) {
            long relativeTime = SystemClock.elapsedRealtime() + this.mBootTime;
            now.setTime(relativeTime);
            if (DEBUG) {
                Log.d("OppoCertificateVerifier", "setting relative time to" + now + " due to " + relativeTime);
            }
        }
        return now;
    }

    private long getServerTimeStampFromOppoHttp(int serverId) {
        if (!this.mOppoHttpClient.requestTime(mContext, serverId, 5000)) {
            return 0;
        }
        long unixTime = this.mOppoHttpClient.getHttpTime();
        if (DEBUG) {
            Log.d("OppoCertificateVerifier", "sucessfully get time from oppoHttpClient...:" + unixTime);
        }
        return unixTime;
    }

    private long getServerTimeStampFromServer() {
        OppoCertificateVerifyOkHttpHelper mHelper = OppoCertificateVerifyOkHttpHelper.getInstance(mContext);
        mHelper.initParam(this.mImei, null, null);
        Call call = mHelper.getSponse(sEnvEnable, 0);
        if (call == null) {
            Log.d("OppoCertificateVerifier", "getServerTimeStampFromServer call is null");
            return 0;
        }
        try {
            Response response = call.execute();
            if (response == null) {
                Log.d("OppoCertificateVerifier", "getServerTimeStampFromServer response is null");
                return 0;
            }
            String jsonStr = response.body().string();
            if (!TextUtils.isEmpty(jsonStr)) {
                TimerResponse res = (TimerResponse) JSONObject.parseObject(jsonStr, TimerResponse.class);
                if (DEBUG) {
                    Slog.d("OppoCertificateVerifier", "OppoCertificateVerifyOkHttpHelper res.code: " + res.code + ";res.msg: " + res.msg + ";res.data:" + UTCToString(res.data.longValue()) + ";res.data:" + res.data);
                }
                if (res != null) {
                    if (res.code.intValue() == 10200) {
                        if (DEBUG) {
                            Log.d("OppoCertificateVerifier", "getServerTimeStampFromGrom result :" + res.data + "res.code " + res.code + " msg =" + res.msg);
                        }
                        if (res.data != null) {
                            return res.data.longValue();
                        }
                    } else if (res.code.intValue() == 10400) {
                        if (DEBUG) {
                            Log.e("OppoCertificateVerifier", "getServerTimeStampFromGrom error :" + res.msg);
                        }
                    } else if (DEBUG) {
                        Log.e("OppoCertificateVerifier", "getServerTimeStampFromGrom unknow error...");
                    }
                } else if (DEBUG) {
                    Log.e("OppoCertificateVerifier", "getServerTimeStampFromGrom can't get res...");
                }
                return 0;
            }
            if (DEBUG) {
                Slog.d("OppoCertificateVerifier", "OppoCertificateVerifyOkHttpHelper TimerResponse is null");
            }
            return 0;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private long tryToGetServerTime() {
        long time = getServerTimeStampFromOppoHttp(0);
        if (time == 0) {
            time = getServerTimeStampFromOppoHttp(1);
        }
        if (time == 0) {
            return getServerTimeStampFromServer();
        }
        return time;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private Date getServerTimeStamp() {
        Date now = new Date();
        Log.d("OppoCertificateVerifier", "getServerTimeStamp mBootTime ->" + this.mBootTime);
        if (this.mBootTime == -1) {
            long serverTime = tryToGetServerTime();
            Log.d("OppoCertificateVerifier", "getServerTimeStamp serverTime ->" + serverTime);
            if (serverTime != 0) {
                now.setTime(serverTime);
                this.mBootTime = serverTime - SystemClock.elapsedRealtime();
                Date temp = new Date();
                temp.setTime(this.mBootTime);
                if (DEBUG) {
                    Log.d("OppoCertificateVerifier", "mBootTime ->" + this.mBootTime + "@" + temp);
                }
            }
        } else {
            long relativeTime = SystemClock.elapsedRealtime() + this.mBootTime;
            now.setTime(relativeTime);
            if (DEBUG) {
                Log.d("OppoCertificateVerifier", "setting relative time to" + now + " due to " + relativeTime);
            }
        }
        return now;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isNetworkAvailable() {
        if (!this.mBootCompleted) {
            return false;
        }
        if (this.mConnectivityManager == null) {
            this.mConnectivityManager = (ConnectivityManager) mContext.getSystemService("connectivity");
        }
        if (this.mConnectivityManager == null || this.mConnectivityManager == null || this.mConnectivityManager.getActiveNetworkInfo() == null || !this.mConnectivityManager.getActiveNetworkInfo().isConnected()) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void scheduleRemoteVerificationIfNetworkAvaliable() {
        if (isNetworkAvailable() && this.mBootCompleted) {
            scheduleRemoteVerification(true);
        }
    }

    /* access modifiers changed from: private */
    public class VerificationThread extends Thread {
        private int tryCount = 0;

        public VerificationThread(String name) {
            super(name);
            try {
                Process.setThreadPriority(10);
            } catch (Exception e) {
            }
        }

        public void resetTryCount() {
            this.tryCount = 0;
        }

        public void run() {
            while (true) {
                try {
                    synchronized (OppoCertificateVerifier.this.mVLock) {
                        try {
                            OppoCertificateVerifier.this.mVLock.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    if (OppoCertificateVerifier.this.mBootCompleted) {
                        if (OppoCertificateVerifier.DEBUG) {
                            Log.d("OppoCertificateVerifier", "VerificationThread waking up.. retry count " + this.tryCount);
                        }
                        if (OppoCertificateVerifier.this.isNetworkAvailable()) {
                            Date now = OppoCertificateVerifier.this.getServerTimeStamp();
                            synchronized (OppoCertificateVerifier.this.mLock) {
                                boolean found = false;
                                Iterator it = OppoCertificateVerifier.this.mOppoCertificates.keySet().iterator();
                                while (true) {
                                    if (!it.hasNext()) {
                                        break;
                                    }
                                    if (((OppoCertificateInfo) OppoCertificateVerifier.this.mOppoCertificates.get((String) it.next())).isTypeOnline()) {
                                        found = true;
                                        break;
                                    }
                                }
                                if (found) {
                                    Log.d("OppoCertificateVerifier", "VerificationThread: online certificates found.:");
                                    ArrayList<OppoCertificateInfo> toDoList = new ArrayList<>();
                                    synchronized (OppoCertificateVerifier.this.mLock) {
                                        for (String key : OppoCertificateVerifier.this.mOppoCertificates.keySet()) {
                                            OppoCertificateInfo info = (OppoCertificateInfo) OppoCertificateVerifier.this.mOppoCertificates.get(key);
                                            if (!info.isRemoteVerifyNeeded(now)) {
                                                if (!OppoCertificateVerifier.mForce_Do_remote_verify) {
                                                    Log.d("OppoCertificateVerifier", "Do not need  RemoteVerify now for " + info.packageName);
                                                }
                                            }
                                            Log.d("OppoCertificateVerifier", "add certificate for " + info.packageName + " to vertification list..");
                                            toDoList.add(info);
                                        }
                                        boolean unused = OppoCertificateVerifier.mForce_Do_remote_verify = false;
                                    }
                                    if (toDoList.size() == 0) {
                                        Log.d("OppoCertificateVerifier", "VerificationThread: no certificates need verify..");
                                    } else {
                                        boolean shouldRetry = false;
                                        Iterator<OppoCertificateInfo> it2 = toDoList.iterator();
                                        while (it2.hasNext()) {
                                            OppoCertificateInfo info2 = it2.next();
                                            Log.d("OppoCertificateVerifier", " call doRemoteVerification");
                                            RemoteVerificationResult res = OppoCertificateVerifier.this.doRemoteVerification(info2);
                                            synchronized (OppoCertificateVerifier.this.mLock) {
                                                OppoCertificateInfo org2 = (OppoCertificateInfo) OppoCertificateVerifier.this.mOppoCertificates.get(info2.packageName);
                                                if (!org2.compare(info2)) {
                                                    Log.d("OppoCertificateVerifier", "certificate changed while verfication :" + info2.packageName);
                                                } else if (res.result != 5) {
                                                    org2.setVerityStatus(res.result);
                                                    org2.setLastVerityTime(res.timeStamp);
                                                    if (res.result == 1) {
                                                        org2.updateFromToDate(res.validFrom, res.validTo);
                                                    }
                                                    OppoCertificateVerifier.this.mCachedVerificationResult.put(org2.packageName, res);
                                                    Log.d("OppoCertificateVerifier", "sucessfully get verfied result certificate for " + info2.packageName + " status :" + OppoCertificateInfo.verityStatusToString(res.result) + "@" + res.timeStamp);
                                                    OppoCertificateVerifier.this.storeCachedVerificationResultToFile();
                                                } else {
                                                    shouldRetry = true;
                                                    Log.d("OppoCertificateVerifier", "failed to conect to server... try again later for " + info2.packageName);
                                                }
                                            }
                                        }
                                        if (shouldRetry) {
                                            int i = this.tryCount + 1;
                                            this.tryCount = i;
                                            if (i < 10) {
                                                Log.d("OppoCertificateVerifier", "try again");
                                                OppoCertificateVerifier.this.scheduleRemoteVerification(((long) this.tryCount) * 2000 * 2, false);
                                            }
                                        }
                                        if (!shouldRetry) {
                                            this.tryCount = 0;
                                        }
                                    }
                                } else if (OppoCertificateVerifier.DEBUG) {
                                    Log.d("OppoCertificateVerifier", "VerificationThread: no online certificates found..");
                                }
                            }
                        } else if (OppoCertificateVerifier.DEBUG) {
                            Log.d("OppoCertificateVerifier", "network unavailable...");
                        }
                    } else if (OppoCertificateVerifier.DEBUG) {
                        Log.d("OppoCertificateVerifier", "Booting in progress");
                    }
                } catch (Exception e2) {
                    e2.printStackTrace();
                    return;
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public class VerityBroadcastReceiver extends BroadcastReceiver {
        private VerityBroadcastReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            try {
                if ("android.net.conn.CONNECTIVITY_CHANGE".equals(action)) {
                    if (OppoCertificateVerifier.DEBUG) {
                        Log.d("OppoCertificateVerifier", "network changed...");
                    }
                    OppoCertificateVerifier.this.scheduleRemoteVerificationIfNetworkAvaliable();
                } else if ("android.intent.action.PACKAGE_ADDED".equals(action)) {
                    OppoCertificateVerifier.this.schedulePackageChanged(3, intent.getData().getSchemeSpecificPart());
                } else if ("android.intent.action.PACKAGE_REMOVED".equals(action)) {
                    OppoCertificateVerifier.this.schedulePackageChanged(4, intent.getData().getSchemeSpecificPart());
                } else if ("android.intent.action.USER_UNLOCKED".equals(action)) {
                    OppoCertificateVerifier.this.schedulePackageChanged(5, null);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:38:0x00dd, code lost:
        if (0 != 0) goto L_0x00c1;
     */
    private ManifestDigest getManifestDigest_V2(String codePath) {
        ManifestDigest digest = null;
        String tempFileName = "/data/user_de/0/com.oppo.enterprise.mdmcoreservice/cache/mf.temp." + SystemClock.elapsedRealtime();
        StrictJarFile jarFile = new StrictJarFile(codePath, false, false);
        try {
            ZipEntry je = jarFile.findEntry("META-INF/MANIFEST.MF");
            if (je != null) {
                InputStream rsaIns = null;
                File tempFile = new File(tempFileName);
                if (tempFile.exists()) {
                    tempFile.delete();
                }
                BufferedWriter bufferfw = new BufferedWriter(new FileWriter(tempFile));
                BufferedReader manifestreader = new BufferedReader(new InputStreamReader(jarFile.getInputStream(je)));
                while (true) {
                    try {
                        String line = manifestreader.readLine();
                        if (line == null) {
                            break;
                        } else if (line.contains("META-INF/OPPOENT.CER")) {
                            Log.d("OppoCertificateVerifier", "find oppo certificate file in manifest file");
                            while (true) {
                                String line2 = manifestreader.readLine();
                                if (line2 != null) {
                                    if (line2.contains("Name:")) {
                                        bufferfw.write(line2 + "\r");
                                        bufferfw.newLine();
                                        break;
                                    }
                                } else {
                                    break;
                                }
                            }
                        } else {
                            bufferfw.write(line + "\r");
                            bufferfw.newLine();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        bufferfw.close();
                        if (manifestreader != null) {
                            manifestreader.close();
                        }
                        if (tempFile.exists()) {
                            tempFile.delete();
                        }
                    } catch (Throwable th) {
                        bufferfw.close();
                        if (manifestreader != null) {
                            manifestreader.close();
                        }
                        if (tempFile.exists()) {
                            tempFile.delete();
                        }
                        if (0 != 0) {
                            rsaIns.close();
                        }
                        throw th;
                    }
                }
                bufferfw.flush();
                rsaIns = new FileInputStream(tempFile);
                digest = ManifestDigest.fromInputStream(rsaIns);
                bufferfw.close();
                if (manifestreader != null) {
                    manifestreader.close();
                }
                if (tempFile.exists()) {
                    tempFile.delete();
                }
                rsaIns.close();
            }
            jarFile.close();
        } catch (IOException | RuntimeException e2) {
            e2.printStackTrace();
        } catch (Throwable th2) {
            jarFile.close();
            throw th2;
        }
        if (DEBUG && digest != null) {
            Log.d("OppoCertificateVerifier", "getManifestDigest_V2.hash:" + digest.getHash());
        }
        return digest;
    }

    private static byte[] inputStream2ByteArray(InputStream is) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] data = new byte[256];
        while (true) {
            int nRead = is.read(data, 0, data.length);
            if (nRead != -1) {
                buffer.write(data, 0, nRead);
            } else {
                buffer.flush();
                return buffer.toByteArray();
            }
        }
    }

    public static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (byte b : src) {
            String hv = Integer.toHexString(b & 255);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }

    /* access modifiers changed from: private */
    public static class ManifestDigest {
        private final byte[] mDigest;

        ManifestDigest(byte[] digest) {
            this.mDigest = digest;
        }

        static ManifestDigest fromInputStream(InputStream fileIs) {
            if (fileIs == null) {
                return null;
            }
            try {
                MessageDigest md = MessageDigest.getInstance("SHA-256");
                DigestInputStream dis = new DigestInputStream(new BufferedInputStream(fileIs), md);
                try {
                    byte[] readBuffer = new byte[IdentityHashMap.DEFAULT_SIZE];
                    do {
                    } while (dis.read(readBuffer, 0, readBuffer.length) != -1);
                    IoUtils.closeQuietly(dis);
                    return new ManifestDigest(md.digest());
                } catch (IOException e) {
                    Log.w("ManifestDigest", "Could not read manifest");
                    IoUtils.closeQuietly(dis);
                    return null;
                } catch (Throwable th) {
                    IoUtils.closeQuietly(dis);
                    throw th;
                }
            } catch (NoSuchAlgorithmException e2) {
                throw new RuntimeException("SHA-256 must be available", e2);
            }
        }

        public boolean equals(Object o) {
            if (!(o instanceof ManifestDigest)) {
                return false;
            }
            ManifestDigest other = (ManifestDigest) o;
            if (this == other || Arrays.equals(this.mDigest, other.mDigest)) {
                return true;
            }
            return false;
        }

        public int hashCode() {
            return Arrays.hashCode(this.mDigest);
        }

        public String getHash() {
            return OppoCertificateVerifier.bytesToHexString(this.mDigest);
        }
    }

    /* access modifiers changed from: private */
    public static class CertificateResult {
        public byte[] bytes;

        private CertificateResult() {
            this.bytes = null;
        }
    }

    /* access modifiers changed from: private */
    public static class VerificationParam {
        public boolean polling;
        public long pollingInterval;
        public boolean resetTryCount;

        private VerificationParam() {
        }
    }

    /* access modifiers changed from: private */
    public static class ControlledPermissions {
        ArrayList<String> apiList = null;
        public String perm;

        private ControlledPermissions(String perm2, ArrayList<String> list) {
            this.perm = perm2;
            this.apiList = list;
        }

        public static ControlledPermissions createFromString(String src) {
            if (src == null) {
                return null;
            }
            String[] temp = src.split(",");
            String perm2 = temp[0].trim();
            ArrayList<String> apiList2 = null;
            if (temp.length > 1) {
                apiList2 = new ArrayList<>();
                for (int i = 1; i < temp.length; i++) {
                    apiList2.add(temp[i].trim());
                }
            }
            return new ControlledPermissions(perm2, apiList2);
        }
    }

    public static String UTCToString(long time) {
        try {
            Date timeStamp = new Date();
            timeStamp.setTime(time);
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(timeStamp);
        } catch (Exception e) {
            e.printStackTrace();
            return "-1";
        }
    }

    public static class RemoteVerificationResult {
        int result = 5;
        Date timeStamp;
        String validFrom = "-1";
        String validTo = "-1";

        public RemoteVerificationResult() {
        }

        public RemoteVerificationResult(int result2, Date timeStamp2, String validFrom2, String validTo2) {
            this.result = result2;
            this.timeStamp = timeStamp2;
            this.validFrom = validFrom2;
            this.validTo = validTo2;
        }

        public boolean haveValidFromTo() {
            return !TextUtils.isEmpty(this.validFrom) && !TextUtils.isEmpty(this.validTo) && !"-1".equals(this.validFrom) && !"-1".equals(this.validTo);
        }

        public static RemoteVerificationResult fromCerResult(CerResult cer) {
            try {
                Date now = new Date();
                now.setTime(cer.serverTime.longValue());
                RemoteVerificationResult r = new RemoteVerificationResult();
                r.result = cer.status.intValue();
                r.timeStamp = now;
                if (OppoCertificateVerifier.DEBUG) {
                    Log.d("OppoCertificateVerifier", "CreatefromCerResult:" + OppoCertificateInfo.verityStatusToString(cer.status.intValue()) + "|" + now);
                }
                if (cer.status.intValue() == 1) {
                    String validFrom2 = OppoCertificateVerifier.UTCToString(cer.fromTime.longValue());
                    String validTo2 = OppoCertificateVerifier.UTCToString(cer.toTime.longValue());
                    r.validFrom = validFrom2;
                    r.validTo = validTo2;
                }
                return r;
            } catch (Exception e) {
                e.printStackTrace();
                return new RemoteVerificationResult();
            }
        }

        public static RemoteVerificationResult fromString(String src) {
            if (src == null) {
                return null;
            }
            try {
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String[] temp = src.split(";");
                if (temp == null || temp.length != 4) {
                    return null;
                }
                return new RemoteVerificationResult(Integer.valueOf(temp[0].trim()).intValue(), df.parse(temp[1].trim()), temp[2].trim(), temp[3].trim());
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        public String toString() {
            String time = "";
            if (this.timeStamp != null) {
                time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(this.timeStamp);
            }
            return "" + this.result + ";" + time + ";" + this.validFrom + ";" + this.validTo;
        }
    }

    public int oppoCheckPermission(String permName, String packageName) {
        if (!isCertificateControlledPermission(permName) || !checkPermission(packageName, permName)) {
            return -1;
        }
        return 0;
    }

    public boolean isAlreadyInCertificatePackageList(String pkgName) {
        synchronized (this.mLock) {
            if (this.mOppoCertificatePackageList == null || !this.mOppoCertificatePackageList.containsKey(pkgName)) {
                return false;
            }
            if (DEBUG) {
                Log.d("OppoCertificateVerifier", "isAlreadyInCertificatePackageList for " + pkgName);
            }
            return true;
        }
    }

    private boolean noNeedFindCertificate(String pkgName) {
        return pkgName.contains("com.qualcomm.") || pkgName.contains("com.coloros.") || pkgName.contains("com.google.") || pkgName.contains("com.oppo.") || pkgName.contains("com.qti.");
    }

    public void getPackagesList() {
        if (!isPackageManagerAvailable()) {
            Log.d("OppoCertificateVerifier", "getPackagesList mPackageManager is null, return");
            this.mHandler.sendEmptyMessageDelayed(6, 2000);
            return;
        }
        List<PackageInfo> packages = this.mPackageManager.getInstalledPackages(0);
        int count = packages.size();
        if (DEBUG) {
            Log.d("OppoCertificateVerifier", "getPackagesList packages count is :" + count);
        }
        synchronized (this.mLock) {
            for (int p = 0; p < count; p++) {
                try {
                    PackageInfo info = packages.get(p);
                    if (!isAlreadyInCertificatePackageList(info.packageName) && !noNeedFindCertificate(info.packageName)) {
                        checkOppoCertificateIfNeeded(info);
                    }
                } catch (Throwable th) {
                    throw th;
                }
            }
        }
        if (DEBUG) {
            Log.d("OppoCertificateVerifier", "getPackagesList end");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isPackageManagerAvailable() {
        if (this.mPackageManager != null) {
            return true;
        }
        this.mPackageManager = mContext.getPackageManager();
        if (this.mPackageManager != null) {
            return true;
        }
        return false;
    }

    public void getPackageCertificateFromPmsList(String packageName) {
        PackageInfo info = null;
        if (!isPackageManagerAvailable()) {
            Log.d("OppoCertificateVerifier", "getPackageCertificateFromPmsList mPackageManager is null, return");
            this.mHandler.sendEmptyMessageDelayed(6, 2000);
            return;
        }
        List<PackageInfo> packages = this.mPackageManager.getInstalledPackages(0);
        int count = packages.size();
        if (DEBUG) {
            Log.d("OppoCertificateVerifier", "getPackageCertificateFromPmsList packages count is :" + count);
        }
        for (int p = 0; p < count; p++) {
            info = packages.get(p);
            if (info.packageName.equals(packageName)) {
                break;
            }
        }
        if (info != null) {
            checkOppoCertificateIfNeeded(info);
        }
        if (DEBUG) {
            Log.d("OppoCertificateVerifier", "getPackageCertificateFromPmsList end");
        }
    }

    public void getPackageListFromCachedFile() {
        int result;
        restoreCachedCertificatePackageFromFile();
        synchronized (this.mLock) {
            if (!this.mOppoCertificatePackageList.isEmpty()) {
                if (DEBUG) {
                    Log.d("OppoCertificateVerifier", "mOppoCertificatePackageList is not empty,size:" + this.mOppoCertificatePackageList.size());
                }
                HashMap<String, String> temp = (HashMap) this.mOppoCertificatePackageList.clone();
                for (String key : temp.keySet()) {
                    String codepath = temp.get(key);
                    if (DEBUG) {
                        Log.d("OppoCertificateVerifier", "package:" + key + ";codapath:" + codepath);
                    }
                    File appFile = new File(codepath);
                    if (!appFile.exists() || !PackageParser.isApkFile(appFile)) {
                        result = 0;
                    } else {
                        result = checkOppoCertificateIfNeededInternal(codepath, key);
                    }
                    if (result != 1) {
                        this.mOppoCertificatePackageList.remove(key);
                        storeCachedCertificatePackageToFile();
                        if (DEBUG) {
                            Log.d("OppoCertificateVerifier", "the package: " + key + " codepath maybe changed, try again");
                        }
                        getPackageCertificateFromPmsList(key);
                    }
                }
            }
        }
        getPackagesList();
        this.mHandler.sendEmptyMessage(2);
    }

    /* access modifiers changed from: package-private */
    public boolean skipCheckPermission(String pkgName) {
        if (!DEBUG || !pkgName.equals("com.oppotest.testmdm")) {
            return false;
        }
        Log.d("OppoCertificateVerifier", "COREAPP SKIP CHECKPERMISSION :" + SystemProperties.getBoolean("persist.sys.oplus.coreapp.debug", false));
        return SystemProperties.getBoolean("persist.sys.oplus.coreapp.debug", false);
    }
}
