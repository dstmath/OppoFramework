package com.oppo.debug;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.OppoAssertTip;
import android.util.Log;
import com.color.widget.ColorRecyclerView.ItemAnimator;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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
    private static final String ASSERT_ENABLE_PROP = "persist.sys.assert.enable";
    private static final String ASSERT_PANIC_PROP = "persist.sys.assert.panic";
    private static final String ASSERT_STATE = "persist.sys.assert.state";
    private static final String DESTDIR = "/data/oppo_log/anr_binder_info/binder_info_";
    private static final int IS_EMPTY = 1;
    private static final int IS_GZIPPED = 4;
    private static final int IS_TEXT = 2;
    private static final int MAX_CONTEXT_LENGTH = 20;
    private static final String SOURCEDIR = "/sys/kernel/debug/binder/state";
    private static final String TAG = "java.lang.ASSERT";
    private static OppoAssertTip mFunctionProxy = null;
    private static final Runtime rt = Runtime.getRuntime();

    private static native void displayErrorInfo_native(String str);

    private static native String getProcessName_native();

    private static native String getSystemProperties_native(String str, String str2);

    private static native void panic_native();

    private static native void setSystemProperties_native(String str, String str2);

    protected ASSERT() {
    }

    public static boolean epitaph(File temp, String tag, int flags, Context c) {
        IOException e;
        String process = null;
        String packageName = null;
        String PID = "NONE";
        mFunctionProxy = OppoAssertTip.getInstance();
        if (temp == null) {
            return false;
        }
        try {
            InputStream is = new FileInputStream(temp);
            if ((flags & 4) != 0) {
                try {
                    is = new GZIPInputStream(is);
                } catch (IOException e2) {
                    e = e2;
                    e.printStackTrace();
                    return false;
                }
            }
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            InputStream is2;
            try {
                int maxSize;
                if ("true".equals(System.getProperty("persist.sys.thridpart.debug", "false"))) {
                    maxSize = ItemAnimator.FLAG_APPEARED_IN_PRE_LAYOUT;
                } else {
                    maxSize = 1024;
                }
                StringBuilder stringBuilder = new StringBuilder(maxSize);
                int count = 0;
                while (count < maxSize) {
                    String line = br.readLine();
                    if (line != null && !line.startsWith("-----", 0)) {
                        if (line.startsWith("Process: ", 0)) {
                            process = line.substring(line.indexOf(":") + 1).trim();
                        }
                        if (line.startsWith("PID: ", 0)) {
                            PID = line.substring(line.indexOf(":") + 1).trim();
                        }
                        if (line.startsWith("Package: ", 0)) {
                            String start = line.substring(line.indexOf(":") + 2);
                            int end = start.indexOf(" ");
                            if (end > 0) {
                                packageName = start.substring(0, end).trim();
                            } else {
                                Log.v(TAG, "pacakge line = " + line);
                            }
                        }
                        stringBuilder.append(line);
                        stringBuilder.append("\r\n");
                        count += line.length();
                    }
                }
                br.close();
                is.close();
                if (count == 0) {
                    return false;
                }
                if (process == null) {
                    process = "NONE";
                }
                is2 = new FileInputStream(temp);
                if ((flags & 4) != 0) {
                    try {
                        is2 = new GZIPInputStream(is2);
                    } catch (IOException e3) {
                        e = e3;
                        e.printStackTrace();
                        return false;
                    }
                }
                String withoutColonProcessName = process.replace(':', '_');
                process = withoutColonProcessName;
                Log.d(TAG, "after replace ':' with '_' ,the ProcessName is " + withoutColonProcessName);
                copyAssert(is2, withoutColonProcessName + "-" + PID);
                is2.close();
                String content = stringBuilder.toString();
                if (getSystemProperties_native(ASSERT_STATE, "true").equals("false")) {
                    stringBuilder.append("assert state is close");
                } else {
                    String appName = getAppName(c, packageName);
                    if (appName == null || appName.isEmpty()) {
                        Log.v(TAG, "can not get the app name");
                    } else {
                        content = appName + "\n" + content;
                    }
                    int result = mFunctionProxy.requestShowAssertMessage(content);
                }
                return true;
            } catch (IOException e4) {
                e = e4;
                is2 = is;
                e.printStackTrace();
                return false;
            } catch (Throwable th) {
                br.close();
                is.close();
            }
        } catch (IOException e5) {
            e = e5;
            e.printStackTrace();
            return false;
        }
    }

    public static void CopyTombstone(String filePath) {
        Log.v(TAG, "in copyTombstone");
        if (getSystemProperties_native(ASSERT_PANIC_PROP, "false").equals("true")) {
            setSystemProperties_native("sys.tombstone.file", filePath);
            setSystemProperties_native("ctl.start", "tranfer_tomb");
        }
    }

    public static boolean copyAssert(InputStream inputStream, String DestFileString) {
        if (!getSystemProperties_native(ASSERT_PANIC_PROP, "false").equals("true")) {
            return true;
        }
        File destFile = new File(getSystemProperties_native("sys.oppo.logkit.assertlog", "") + "/" + (DestFileString + "-" + new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date()) + ".txt"));
        Log.d(TAG, "copyAssert destFile=" + destFile);
        OutputStream out;
        try {
            if (destFile.exists()) {
                destFile.delete();
            }
            out = new FileOutputStream(destFile);
            byte[] buffer = new byte[ItemAnimator.FLAG_APPEARED_IN_PRE_LAYOUT];
            while (true) {
                int bytesRead = inputStream.read(buffer);
                if (bytesRead >= 0) {
                    out.write(buffer, 0, bytesRead);
                    out.flush();
                } else {
                    out.close();
                    return true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } catch (Throwable th) {
            out.close();
        }
    }

    public static void copyAnr(String filePath, String name) {
        Log.v(TAG, "copyAnr filePath = " + filePath);
        if (getSystemProperties_native(ASSERT_PANIC_PROP, "false").equals("true")) {
            setSystemProperties_native("sys.anr.srcfile", filePath);
            setSystemProperties_native("sys.anr.destfile", name);
            setSystemProperties_native("ctl.start", "tranfer_anr");
        }
    }

    private static String getAppName(Context c, String packageName) {
        if (packageName == null) {
            Log.v(TAG, "can not get the pacakge");
            return null;
        }
        PackageManager pm = c.getPackageManager();
        try {
            return pm.getApplicationInfo(packageName, 0).loadLabel(pm).toString() + " ";
        } catch (Exception e) {
            Log.v(TAG, "getAppName e = " + e);
            return null;
        }
    }

    private static String getTimeStamp() {
        return new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date());
    }

    public static void copyBinderInfo() {
        new Thread("ANR_ZIPWorker") {
            public void run() {
                try {
                    ASSERT.doZip(ASSERT.SOURCEDIR, ASSERT.DESTDIR + ASSERT.getTimeStamp() + ".zip");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    /* JADX WARNING: Removed duplicated region for block: B:27:0x004d A:{SYNTHETIC, Splitter: B:27:0x004d} */
    /* JADX WARNING: Removed duplicated region for block: B:33:0x0059 A:{SYNTHETIC, Splitter: B:33:0x0059} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static void doZip(String src, String dest) throws IOException {
        IOException ex;
        Throwable th;
        ZipOutputStream out = null;
        try {
            File outFile = new File(dest);
            File fileOrDirectory = new File(src);
            if (fileOrDirectory.exists()) {
                ZipOutputStream out2 = new ZipOutputStream(new FileOutputStream(outFile));
                try {
                    if (fileOrDirectory.isFile()) {
                        zipFileOrDirectory(out2, fileOrDirectory, "");
                    } else {
                        File[] entries = fileOrDirectory.listFiles();
                        for (File zipFileOrDirectory : entries) {
                            zipFileOrDirectory(out2, zipFileOrDirectory, "");
                        }
                    }
                    if (out2 != null) {
                        try {
                            out2.close();
                        } catch (IOException ex2) {
                            ex2.printStackTrace();
                        }
                    }
                    out = out2;
                } catch (IOException e) {
                    ex2 = e;
                    out = out2;
                    try {
                        ex2.printStackTrace();
                        if (out != null) {
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        if (out != null) {
                            try {
                                out.close();
                            } catch (IOException ex22) {
                                ex22.printStackTrace();
                            }
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    out = out2;
                    if (out != null) {
                    }
                    throw th;
                }
            }
        } catch (IOException e2) {
            ex22 = e2;
            ex22.printStackTrace();
            if (out != null) {
                try {
                    out.close();
                } catch (IOException ex222) {
                    ex222.printStackTrace();
                }
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:36:0x0087 A:{SYNTHETIC, Splitter: B:36:0x0087} */
    /* JADX WARNING: Removed duplicated region for block: B:46:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:16:0x0040 A:{SYNTHETIC, Splitter: B:16:0x0040} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static void zipFileOrDirectory(ZipOutputStream out, File fileOrDirectory, String curPath) throws IOException {
        IOException ex;
        Throwable th;
        FileInputStream in = null;
        try {
            if (fileOrDirectory.isDirectory()) {
                File[] entries = fileOrDirectory.listFiles();
                for (File zipFileOrDirectory : entries) {
                    zipFileOrDirectory(out, zipFileOrDirectory, curPath + fileOrDirectory.getName() + "/");
                }
            } else {
                byte[] buffer = new byte[ItemAnimator.FLAG_APPEARED_IN_PRE_LAYOUT];
                FileInputStream in2 = new FileInputStream(fileOrDirectory);
                try {
                    out.putNextEntry(new ZipEntry(curPath + fileOrDirectory.getName()));
                    while (true) {
                        int bytes_read = in2.read(buffer);
                        if (bytes_read == -1) {
                            break;
                        }
                        out.write(buffer, 0, bytes_read);
                    }
                    out.closeEntry();
                    in = in2;
                } catch (IOException e) {
                    ex = e;
                    in = in2;
                    try {
                        ex.printStackTrace();
                        if (in == null) {
                            try {
                                in.close();
                                return;
                            } catch (IOException ex2) {
                                ex2.printStackTrace();
                                return;
                            }
                        }
                        return;
                    } catch (Throwable th2) {
                        th = th2;
                        if (in != null) {
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    in = in2;
                    if (in != null) {
                        try {
                            in.close();
                        } catch (IOException ex22) {
                            ex22.printStackTrace();
                        }
                    }
                    throw th;
                }
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ex222) {
                    ex222.printStackTrace();
                }
            }
        } catch (IOException e2) {
            ex222 = e2;
            ex222.printStackTrace();
            if (in == null) {
            }
        }
    }
}
