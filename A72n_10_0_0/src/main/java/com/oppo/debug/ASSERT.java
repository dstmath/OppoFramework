package com.oppo.debug;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.OppoAssertTip;
import android.os.SystemProperties;
import android.provider.SettingsStringUtil;
import android.security.keystore.KeyProperties;
import android.util.Log;
import com.android.internal.content.NativeLibraryHelper;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ASSERT {
    private static final String ASSERT_STATE = "persist.sys.assert.state";
    private static final String DESTDIR = "/data/oppo_log/anr_binder_info/binder_info_";
    private static final int IS_GZIPPED = 4;
    private static final String SOURCEDIR = "/sys/kernel/debug/binder/state";
    private static final String TAG = "java.lang.ASSERT";
    private static OppoAssertTip mFunctionProxy = null;
    private static final Runtime rt = Runtime.getRuntime();

    protected ASSERT() {
    }

    /* JADX INFO: Multiple debug info for r20v11 'line'  java.lang.String: [D('line' java.lang.String), D('PID' java.lang.String)] */
    /* JADX WARNING: Removed duplicated region for block: B:145:0x02b9 A[SYNTHETIC, Splitter:B:145:0x02b9] */
    /* JADX WARNING: Removed duplicated region for block: B:168:0x02fe A[SYNTHETIC, Splitter:B:168:0x02fe] */
    /* JADX WARNING: Removed duplicated region for block: B:173:0x030c A[SYNTHETIC, Splitter:B:173:0x030c] */
    /* JADX WARNING: Removed duplicated region for block: B:180:0x0326 A[SYNTHETIC, Splitter:B:180:0x0326] */
    /* JADX WARNING: Removed duplicated region for block: B:185:0x0334 A[SYNTHETIC, Splitter:B:185:0x0334] */
    /* JADX WARNING: Removed duplicated region for block: B:197:? A[RETURN, SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:200:? A[RETURN, SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:87:0x01bf A[SYNTHETIC, Splitter:B:87:0x01bf] */
    /* JADX WARNING: Removed duplicated region for block: B:95:0x01d7 A[SYNTHETIC, Splitter:B:95:0x01d7] */
    public static boolean epitaph(File temp, String tag, int flags, Context c) {
        Exception exc;
        IOException e;
        String process;
        String packageName;
        String PID;
        Exception e2;
        int maxSize;
        Exception e3;
        String packageName2;
        if (temp == null) {
            return false;
        }
        InputStream isTempForTrim = null;
        BufferedReader brTempForTrim = null;
        String process2 = null;
        String packageName3 = null;
        String PID2 = KeyProperties.DIGEST_NONE;
        int maxSize2 = 0;
        try {
            if ("true".equals(SystemProperties.get("persist.sys.thridpart.debug", "false"))) {
                maxSize2 = 4096;
            } else {
                maxSize2 = 1024;
            }
            try {
                StringBuilder sb = new StringBuilder(maxSize2);
                isTempForTrim = new FileInputStream(temp);
                if ((flags & 4) != 0) {
                    isTempForTrim = new GZIPInputStream(isTempForTrim);
                }
                brTempForTrim = new BufferedReader(new InputStreamReader(isTempForTrim));
                int count = 0;
                while (true) {
                    process = process2;
                    if (count >= maxSize2) {
                        packageName = packageName3;
                        PID = PID2;
                        break;
                    }
                    try {
                        String line = brTempForTrim.readLine();
                        if (line == null) {
                            packageName = packageName3;
                            PID = PID2;
                            break;
                        }
                        packageName = packageName3;
                        PID = PID2;
                        try {
                            if (line.startsWith("-----", 0)) {
                                break;
                            }
                            if (line.startsWith("Process: ", 0)) {
                                try {
                                    process = line.substring(line.indexOf(SettingsStringUtil.DELIMITER) + 1).trim();
                                } catch (Exception e4) {
                                    e3 = e4;
                                    maxSize = maxSize2;
                                    PID2 = PID;
                                    packageName3 = packageName;
                                    try {
                                        Log.e(TAG, "epitaph failed.", e3);
                                        if (count != 0) {
                                        }
                                    } catch (IOException e5) {
                                        e = e5;
                                        process2 = process;
                                        maxSize2 = maxSize;
                                    } catch (Throwable e6) {
                                        exc = e6;
                                        if (isTempForTrim != null) {
                                        }
                                        if (brTempForTrim != null) {
                                        }
                                        throw exc;
                                    }
                                } catch (IOException e7) {
                                    e = e7;
                                    process2 = process;
                                    PID2 = PID;
                                    packageName3 = packageName;
                                    try {
                                        e.printStackTrace();
                                        if (isTempForTrim != null) {
                                            try {
                                                isTempForTrim.close();
                                            } catch (Exception e8) {
                                                Log.e(TAG, "finally close is failed.");
                                            }
                                        }
                                        if (brTempForTrim == null) {
                                            return false;
                                        }
                                        try {
                                            brTempForTrim.close();
                                            return false;
                                        } catch (Exception e9) {
                                            Log.e(TAG, "finally close br failed.");
                                            return false;
                                        }
                                    } catch (Throwable th) {
                                        exc = th;
                                    }
                                } catch (Throwable th2) {
                                    exc = th2;
                                    if (isTempForTrim != null) {
                                        try {
                                            isTempForTrim.close();
                                        } catch (Exception e10) {
                                            Log.e(TAG, "finally close is failed.");
                                        }
                                    }
                                    if (brTempForTrim != null) {
                                        try {
                                            brTempForTrim.close();
                                        } catch (Exception e11) {
                                            Log.e(TAG, "finally close br failed.");
                                        }
                                    }
                                    throw exc;
                                }
                            }
                            maxSize = maxSize2;
                            try {
                                if (line.startsWith("PID: ", 0)) {
                                    PID = line.substring(line.indexOf(SettingsStringUtil.DELIMITER) + 1).trim();
                                }
                                if (line.startsWith("Package: ", 0)) {
                                    String start = line.substring(line.indexOf(SettingsStringUtil.DELIMITER) + 2);
                                    int end = start.indexOf(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
                                    if (end > 0) {
                                        packageName2 = start.substring(0, end).trim();
                                        sb.append(line);
                                        sb.append("\r\n");
                                        count += line.length();
                                        packageName3 = packageName2;
                                        process2 = process;
                                        PID2 = PID;
                                        maxSize2 = maxSize;
                                    } else {
                                        Log.v(TAG, "pacakge line = " + line);
                                    }
                                }
                                packageName2 = packageName;
                            } catch (Exception e12) {
                                e3 = e12;
                                PID2 = PID;
                                packageName3 = packageName;
                                Log.e(TAG, "epitaph failed.", e3);
                                if (count != 0) {
                                }
                            } catch (IOException e13) {
                                e = e13;
                                process2 = process;
                                PID2 = PID;
                                packageName3 = packageName;
                                maxSize2 = maxSize;
                                e.printStackTrace();
                                if (isTempForTrim != null) {
                                }
                                if (brTempForTrim == null) {
                                }
                            } catch (Throwable th3) {
                                exc = th3;
                                if (isTempForTrim != null) {
                                }
                                if (brTempForTrim != null) {
                                }
                                throw exc;
                            }
                            try {
                                sb.append(line);
                                sb.append("\r\n");
                                count += line.length();
                                packageName3 = packageName2;
                                process2 = process;
                                PID2 = PID;
                                maxSize2 = maxSize;
                            } catch (Exception e14) {
                                e3 = e14;
                                packageName3 = packageName2;
                                PID2 = PID;
                                Log.e(TAG, "epitaph failed.", e3);
                                if (count != 0) {
                                }
                            } catch (IOException e15) {
                                e = e15;
                                packageName3 = packageName2;
                                process2 = process;
                                PID2 = PID;
                                maxSize2 = maxSize;
                                e.printStackTrace();
                                if (isTempForTrim != null) {
                                }
                                if (brTempForTrim == null) {
                                }
                            } catch (Throwable th4) {
                                exc = th4;
                                if (isTempForTrim != null) {
                                }
                                if (brTempForTrim != null) {
                                }
                                throw exc;
                            }
                        } catch (Exception e16) {
                            e3 = e16;
                            maxSize = maxSize2;
                            PID2 = PID;
                            packageName3 = packageName;
                            Log.e(TAG, "epitaph failed.", e3);
                            if (count != 0) {
                            }
                        } catch (IOException e17) {
                            e = e17;
                            process2 = process;
                            PID2 = PID;
                            packageName3 = packageName;
                            e.printStackTrace();
                            if (isTempForTrim != null) {
                            }
                            if (brTempForTrim == null) {
                            }
                        } catch (Throwable th5) {
                            exc = th5;
                            if (isTempForTrim != null) {
                            }
                            if (brTempForTrim != null) {
                            }
                            throw exc;
                        }
                    } catch (Exception e18) {
                        e3 = e18;
                        maxSize = maxSize2;
                        Log.e(TAG, "epitaph failed.", e3);
                        if (count != 0) {
                        }
                    } catch (IOException e19) {
                        e = e19;
                        process2 = process;
                        e.printStackTrace();
                        if (isTempForTrim != null) {
                        }
                        if (brTempForTrim == null) {
                        }
                    } catch (Throwable th6) {
                        exc = th6;
                        if (isTempForTrim != null) {
                        }
                        if (brTempForTrim != null) {
                        }
                        throw exc;
                    }
                }
                PID2 = PID;
                packageName3 = packageName;
                if (count != 0) {
                    try {
                        isTempForTrim.close();
                    } catch (Exception e20) {
                        Log.e(TAG, "finally close is failed.");
                    }
                    try {
                        brTempForTrim.close();
                        return false;
                    } catch (Exception e21) {
                        Log.e(TAG, "finally close br failed.");
                        return false;
                    }
                } else {
                    try {
                        isTempForTrim.close();
                    } catch (Exception e22) {
                        Log.e(TAG, "finally close is failed.");
                    }
                    try {
                        brTempForTrim.close();
                    } catch (Exception e23) {
                        Log.e(TAG, "finally close br failed.");
                    }
                    String process3 = process == null ? KeyProperties.DIGEST_NONE : process;
                    mFunctionProxy = OppoAssertTip.getInstance();
                    InputStream isForCopyAssert = null;
                    int showResult = -1;
                    try {
                        InputStream isForCopyAssert2 = new FileInputStream(temp);
                        if ((flags & 4) != 0) {
                            isForCopyAssert2 = new GZIPInputStream(isForCopyAssert2);
                        }
                        String withoutColonProcessName = process3.replace(':', '_');
                        Log.d(TAG, "after replace ':' with '_' ,the ProcessName is " + withoutColonProcessName);
                        copyAssert(isForCopyAssert2, withoutColonProcessName + NativeLibraryHelper.CLEAR_ABI_OVERRIDE + PID2);
                        isForCopyAssert2.close();
                        isForCopyAssert = null;
                        String content = sb.toString();
                        if (SystemProperties.get(ASSERT_STATE, "true").equals("false")) {
                            sb.append("assert state is close");
                        } else {
                            try {
                                String appName = getAppName(c, packageName3);
                                if (appName != null) {
                                    if (!appName.isEmpty()) {
                                        content = appName + "\n" + content;
                                        showResult = mFunctionProxy.requestShowAssertMessage(content);
                                    }
                                }
                                Log.v(TAG, "can not get the app name");
                                showResult = mFunctionProxy.requestShowAssertMessage(content);
                            } catch (Exception e24) {
                                e2 = e24;
                                try {
                                    Log.e(TAG, "epitaph failed.", e2);
                                    if (isForCopyAssert != null) {
                                    }
                                } catch (Throwable th7) {
                                    e = th7;
                                }
                            }
                        }
                        if (0 != 0) {
                            try {
                                isForCopyAssert.close();
                            } catch (Exception e25) {
                                Log.e(TAG, "finally close isForCopyAssert failed.");
                            }
                        }
                        if (-1 != showResult) {
                            return true;
                        }
                        return false;
                    } catch (Exception e26) {
                        e2 = e26;
                        Log.e(TAG, "epitaph failed.", e2);
                        if (isForCopyAssert != null) {
                            return false;
                        }
                        try {
                            isForCopyAssert.close();
                            return false;
                        } catch (Exception e27) {
                            Log.e(TAG, "finally close isForCopyAssert failed.");
                            return false;
                        }
                    } catch (Throwable th8) {
                        e = th8;
                        if (isForCopyAssert != null) {
                            try {
                                isForCopyAssert.close();
                            } catch (Exception e28) {
                                Log.e(TAG, "finally close isForCopyAssert failed.");
                            }
                        }
                        throw e;
                    }
                }
            } catch (IOException e29) {
                e = e29;
                e.printStackTrace();
                if (isTempForTrim != null) {
                }
                if (brTempForTrim == null) {
                }
            } catch (Throwable th9) {
                exc = th9;
                if (isTempForTrim != null) {
                }
                if (brTempForTrim != null) {
                }
                throw exc;
            }
        } catch (IOException e30) {
            e = e30;
            e.printStackTrace();
            if (isTempForTrim != null) {
            }
            if (brTempForTrim == null) {
            }
        } catch (Throwable th10) {
            exc = th10;
            if (isTempForTrim != null) {
            }
            if (brTempForTrim != null) {
            }
            throw exc;
        }
    }

    public static void CopyTombstone(String filePath) {
        Log.v(TAG, "in copyTombstone");
        if (SystemProperties.get("persist.sys.assert.panic", "false").equals("true") || SystemProperties.get("persist.sys.assert.panic.camera", "false").equals("true")) {
            SystemProperties.set("sys.tombstone.file", filePath);
            SystemProperties.set("ctl.start", "tranfer_tomb");
        }
    }

    public static boolean copyAssert(InputStream inputStream, String DestFileString) {
        if (!SystemProperties.get("persist.sys.assert.panic", "false").equals("true") && !SystemProperties.get("persist.sys.assert.panic.camera", "false").equals("true")) {
            return true;
        }
        String DestPath = SystemProperties.get("sys.oppo.logkit.assertlog", "");
        File destFile = new File(DestPath + "/" + (DestFileString + NativeLibraryHelper.CLEAR_ABI_OVERRIDE + new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date()) + ".txt"));
        StringBuilder sb = new StringBuilder();
        sb.append("copyAssert destFile=");
        sb.append(destFile);
        Log.d(TAG, sb.toString());
        try {
            if (destFile.exists()) {
                destFile.delete();
            }
            OutputStream out = new FileOutputStream(destFile);
            try {
                byte[] buffer = new byte[4096];
                while (true) {
                    int bytesRead = inputStream.read(buffer);
                    if (bytesRead < 0) {
                        return true;
                    }
                    out.write(buffer, 0, bytesRead);
                    out.flush();
                }
            } finally {
                out.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void copyAnr(String filePath, String name) {
        Log.v(TAG, "copyAnr filePath = " + filePath);
        if (SystemProperties.get("persist.sys.assert.panic", "false").equals("true") || SystemProperties.get("persist.sys.assert.panic.camera", "false").equals("true")) {
            SystemProperties.set("sys.anr.srcfile", filePath);
            SystemProperties.set("sys.anr.destfile", name);
            SystemProperties.set("ctl.start", "tranfer_anr");
            copyBinderInfo();
        }
    }

    private static String getAppName(Context c, String packageName) {
        if (packageName == null) {
            Log.v(TAG, "can not get the pacakge");
            return null;
        }
        PackageManager pm = c.getPackageManager();
        try {
            ApplicationInfo ai = pm.getApplicationInfo(packageName, 0);
            return ai.loadLabel(pm).toString() + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER;
        } catch (Exception e) {
            Log.v(TAG, "getAppName e = " + e);
            return null;
        }
    }

    private static String getTimeStamp() {
        return new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date());
    }

    public static void copyBinderInfo() {
        SystemProperties.set("ctl.start", "copybinderinfo");
    }

    private static void binderStateRead() {
        try {
            Log.i(TAG, "Collecting Binder Transaction Status Information");
            BufferedReader in = new BufferedReader(new FileReader(SOURCEDIR));
            FileWriter out = new FileWriter(DESTDIR + getTimeStamp() + ".txt");
            while (true) {
                String line = in.readLine();
                if (line != null) {
                    out.write(line);
                    out.write(10);
                } else {
                    in.close();
                    out.close();
                    return;
                }
            }
        } catch (IOException e) {
            Log.w(TAG, "Failed to collect binder state file", e);
        }
    }

    private static void doZip(String src, String dest) throws IOException {
        File[] entries;
        ZipOutputStream out = null;
        try {
            File outFile = new File(dest);
            File fileOrDirectory = new File(src);
            if (fileOrDirectory.exists()) {
                ZipOutputStream out2 = new ZipOutputStream(new FileOutputStream(outFile));
                if (fileOrDirectory.isFile()) {
                    zipFileOrDirectory(out2, fileOrDirectory, "");
                } else {
                    for (File file : fileOrDirectory.listFiles()) {
                        zipFileOrDirectory(out2, file, "");
                    }
                }
                try {
                    out2.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            } else if (0 != 0) {
                try {
                    out.close();
                } catch (IOException ex2) {
                    ex2.printStackTrace();
                }
            }
        } catch (Exception ex3) {
            ex3.printStackTrace();
            if (0 != 0) {
                out.close();
            }
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    out.close();
                } catch (IOException ex4) {
                    ex4.printStackTrace();
                }
            }
            throw th;
        }
    }

    private static void zipFileOrDirectory(ZipOutputStream out, File fileOrDirectory, String curPath) throws IOException {
        FileInputStream in = null;
        try {
            if (!fileOrDirectory.isDirectory()) {
                byte[] buffer = new byte[4096];
                in = new FileInputStream(fileOrDirectory);
                out.putNextEntry(new ZipEntry(curPath + fileOrDirectory.getName()));
                while (true) {
                    int bytes_read = in.read(buffer);
                    if (bytes_read == -1) {
                        break;
                    }
                    out.write(buffer, 0, bytes_read);
                }
                out.closeEntry();
            } else {
                File[] entries = fileOrDirectory.listFiles();
                for (File file : entries) {
                    zipFileOrDirectory(out, file, curPath + fileOrDirectory.getName() + "/");
                }
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        } catch (IOException ex2) {
            ex2.printStackTrace();
            if (0 != 0) {
                in.close();
            }
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    in.close();
                } catch (IOException ex3) {
                    ex3.printStackTrace();
                }
            }
            throw th;
        }
    }
}
