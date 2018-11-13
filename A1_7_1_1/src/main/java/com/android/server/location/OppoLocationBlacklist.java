package com.android.server.location;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.FileObserver;
import android.os.Handler;
import android.text.TextUtils;
import android.util.AtomicFile;
import android.util.Log;
import android.util.Xml;
import com.android.internal.content.PackageMonitor;
import com.android.internal.util.FastXmlSerializer;
import com.android.server.am.OppoCrashClearManager;
import com.android.server.oppo.IElsaManager;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class OppoLocationBlacklist {
    private static final String APP_INSTALL_FILE_DIR = "data/system/sys_gps_lbs_app_install.xml";
    private static final boolean D = false;
    private static final String DATA_FILE_DIR = "data/system/sys_gps_lbs_config.xml";
    private static final String SYS_FILE_DIR = "system/etc/sys_gps_lbs_config.xml";
    private static final String TAG = "OppoLocationBlacklist";
    private ArrayList<String> mBGBlackList = new ArrayList();
    private ArrayList<String> mBlackList = new ArrayList();
    private final Context mContext;
    private ArrayList<String> mFRBlackList = new ArrayList();
    private ArrayList<String> mFRRomBlackList = new ArrayList();
    private AtomicFile mFile;
    private Handler mHandler;
    private boolean mIsCache = false;
    private PermissionFileListener mListener;
    private final Object mLock = new Object();
    private final PackageMonitor mPackageMonitor = new PackageMonitor() {
        public void onPackageAdded(String packageName, int uid) {
            synchronized (OppoLocationBlacklist.this.mLock) {
                if (OppoLocationBlacklist.this.mRomBlackList.contains(packageName)) {
                    OppoLocationBlacklist.this.mBlackList.add(packageName);
                    OppoLocationBlacklist.this.mFile.delete();
                    OppoLocationBlacklist.this.mWriteScheduled = true;
                } else if (OppoLocationBlacklist.this.mFRRomBlackList.contains(packageName)) {
                    OppoLocationBlacklist.this.mFRBlackList.add(packageName);
                    OppoLocationBlacklist.this.mFile.delete();
                    OppoLocationBlacklist.this.mWriteScheduled = true;
                }
            }
        }

        public void onPackageRemoved(String packageName, int uid) {
            synchronized (OppoLocationBlacklist.this.mLock) {
                if (OppoLocationBlacklist.this.mBlackList.remove(packageName) || OppoLocationBlacklist.this.mFRBlackList.remove(packageName)) {
                    OppoLocationBlacklist.this.mFile.delete();
                    OppoLocationBlacklist.this.mWriteScheduled = true;
                }
            }
        }
    };
    private ArrayList<String> mRomBlackList = new ArrayList();
    private BroadcastReceiver mRomUpdateReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction()) && !OppoLocationBlacklist.this.mIsCache) {
                OppoLocationBlacklist.this.filterInstallApp();
            }
        }
    };
    private boolean mWriteScheduled = false;

    public class PermissionFileListener extends FileObserver {
        public PermissionFileListener(String path) {
            super(path, 4095);
        }

        public void onEvent(int event, String path) {
            switch (event) {
                case 8:
                    File file = new File(OppoLocationBlacklist.DATA_FILE_DIR);
                    if (file.exists()) {
                        OppoLocationBlacklist.this.parseContentFromXML(OppoLocationBlacklist.this.readFromFile(file), false);
                        if (!OppoLocationBlacklist.this.mRomBlackList.containsAll(OppoLocationBlacklist.this.mBlackList) || !OppoLocationBlacklist.this.mFRRomBlackList.containsAll(OppoLocationBlacklist.this.mFRBlackList)) {
                            OppoLocationBlacklist.this.filterInstallApp();
                            return;
                        }
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    }

    public OppoLocationBlacklist(Context context, Handler handler) {
        this.mContext = context;
        this.mHandler = handler;
        this.mFile = new AtomicFile(new File(APP_INSTALL_FILE_DIR));
    }

    public void init() {
        File installFile = new File(APP_INSTALL_FILE_DIR);
        if (installFile.exists()) {
            this.mIsCache = parseContentFromXML(readFromFile(installFile), true);
        }
        File file = new File(DATA_FILE_DIR);
        if (file.exists()) {
            parseContentFromXML(readFromFile(file), false);
        } else {
            permissionListInit(file);
        }
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.BOOT_COMPLETED");
        this.mContext.registerReceiver(this.mRomUpdateReceiver, intentFilter, null, this.mHandler);
        this.mPackageMonitor.register(this.mContext, this.mHandler.getLooper(), true);
        this.mListener = new PermissionFileListener(DATA_FILE_DIR);
        this.mListener.startWatching();
    }

    private void permissionListInit(File file) {
        try {
            file.createNewFile();
        } catch (IOException e) {
        }
        startPermissionListUpdate();
    }

    private void startPermissionListUpdate() {
        try {
            if (this.mContext != null) {
                this.mContext.startService(new Intent("com.oppo.settings.action.gps_config_update").setPackage("com.android.settings"));
            }
        } catch (Exception e) {
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:33:0x0049 A:{SYNTHETIC, Splitter: B:33:0x0049} */
    /* JADX WARNING: Removed duplicated region for block: B:41:0x005a A:{SYNTHETIC, Splitter: B:41:0x005a} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public String readFromFile(File path) {
        IOException e;
        FileNotFoundException e2;
        Throwable th;
        if (path == null) {
            return IElsaManager.EMPTY_PACKAGE;
        }
        InputStream is = null;
        try {
            InputStream is2 = new FileInputStream(path);
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(is2));
                StringBuffer buffer = new StringBuffer();
                String str = IElsaManager.EMPTY_PACKAGE;
                while (true) {
                    str = in.readLine();
                    if (str == null) {
                        break;
                    }
                    buffer.append(str);
                }
                String stringBuffer = buffer.toString();
                if (is2 != null) {
                    try {
                        is2.close();
                    } catch (IOException e3) {
                        e3.printStackTrace();
                    }
                }
                return stringBuffer;
            } catch (FileNotFoundException e4) {
                e2 = e4;
                is = is2;
            } catch (IOException e5) {
                e3 = e5;
                is = is2;
                e3.printStackTrace();
                if (is != null) {
                }
                return null;
            } catch (Throwable th2) {
                th = th2;
                is = is2;
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e32) {
                        e32.printStackTrace();
                    }
                }
                throw th;
            }
        } catch (FileNotFoundException e6) {
            e2 = e6;
            try {
                e2.printStackTrace();
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e322) {
                        e322.printStackTrace();
                    }
                }
                return null;
            } catch (Throwable th3) {
                th = th3;
                if (is != null) {
                }
                throw th;
            }
        } catch (IOException e7) {
            e322 = e7;
            e322.printStackTrace();
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e3222) {
                    e3222.printStackTrace();
                }
            }
            return null;
        }
    }

    private boolean saveToFile(String content, String filePath) {
        try {
            FileOutputStream outStream = new FileOutputStream(new File(filePath));
            outStream.write(content.getBytes());
            outStream.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean parseContentFromXML(String content, boolean isInstallFile) {
        if (content == null || content.isEmpty()) {
            return false;
        }
        ArrayList<String> pkgs = new ArrayList();
        ArrayList<String> frPkgs = new ArrayList();
        ArrayList<String> bgPkgs = new ArrayList();
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(new StringReader(content));
            boolean insideBlacklist = false;
            boolean insideFRBlacklist = false;
            boolean insidePermissionList = false;
            while (true) {
                int type = parser.next();
                if (type != 1) {
                    String tag = parser.getName();
                    switch (type) {
                        case 2:
                            if (!"blacklist".equals(tag)) {
                                if (!"frblacklist".equals(tag)) {
                                    if (!"permission_list".equals(tag)) {
                                        if (!"pkg".equals(tag)) {
                                            break;
                                        }
                                        String pkgName = parser.getAttributeValue(null, OppoCrashClearManager.CRASH_COUNT);
                                        if (!insideBlacklist) {
                                            if (!insideFRBlacklist) {
                                                if (!insidePermissionList) {
                                                    break;
                                                }
                                                parsePermissionListData(bgPkgs, parser);
                                                break;
                                            }
                                            frPkgs.add(pkgName);
                                            break;
                                        }
                                        pkgs.add(pkgName);
                                        break;
                                    }
                                    insidePermissionList = true;
                                    break;
                                }
                                insideFRBlacklist = true;
                                break;
                            }
                            insideBlacklist = true;
                            break;
                        case 3:
                            if (!"blacklist".equals(tag)) {
                                if (!"frblacklist".equals(tag)) {
                                    if (!"permission_list".equals(tag)) {
                                        break;
                                    }
                                    insidePermissionList = false;
                                    break;
                                }
                                insideFRBlacklist = false;
                                break;
                            }
                            insideBlacklist = false;
                            break;
                        default:
                            break;
                    }
                }
                synchronized (this.mLock) {
                    if (isInstallFile) {
                        this.mBlackList = pkgs;
                        this.mFRBlackList = frPkgs;
                    } else {
                        this.mBGBlackList = bgPkgs;
                        this.mRomBlackList = pkgs;
                        this.mFRRomBlackList = frPkgs;
                    }
                }
                return true;
            }
        } catch (XmlPullParserException e) {
            Log.w(TAG, "problem reading blacklist", e);
            return false;
        } catch (IOException e2) {
            return false;
        }
    }

    private void parsePermissionListData(ArrayList<String> list, XmlPullParser parser) {
        String opStr = parser.getAttributeValue(null, "op");
        if (list != null && !TextUtils.isEmpty(opStr)) {
            try {
                Integer op = Integer.valueOf(opStr);
                if (op != null && (op.intValue() & 4) == 4) {
                    list.add(parser.getAttributeValue(null, OppoCrashClearManager.CRASH_COUNT));
                }
            } catch (NumberFormatException e) {
            }
        }
    }

    private void writeToInstallFile() {
        try {
            FileOutputStream stream = this.mFile.startWrite();
            synchronized (this.mLock) {
                try {
                    XmlSerializer out = new FastXmlSerializer();
                    out.setOutput(stream, StandardCharsets.UTF_8.name());
                    out.startDocument(null, Boolean.valueOf(true));
                    out.startTag(null, "blacklist");
                    for (String black : this.mBlackList) {
                        out.startTag(null, "pkg");
                        out.attribute(null, OppoCrashClearManager.CRASH_COUNT, black);
                        out.endTag(null, "pkg");
                    }
                    out.endTag(null, "blacklist");
                    out.startTag(null, "frblacklist");
                    for (String black2 : this.mFRBlackList) {
                        out.startTag(null, "pkg");
                        out.attribute(null, OppoCrashClearManager.CRASH_COUNT, black2);
                        out.endTag(null, "pkg");
                    }
                    out.endTag(null, "frblacklist");
                    out.endDocument();
                    this.mFile.finishWrite(stream);
                } catch (IOException e) {
                    this.mFile.failWrite(stream);
                }
            }
            return;
        } catch (IOException e2) {
        }
    }

    private void filterInstallApp() {
        PackageManager pm = this.mContext.getPackageManager();
        ArrayList<String> list = new ArrayList();
        for (String black : this.mRomBlackList) {
            try {
                if (pm.getPackageUid(black, DumpState.DUMP_PREFERRED_XML) != -1) {
                    list.add(black);
                }
            } catch (NameNotFoundException e) {
            } catch (Exception e2) {
            }
        }
        ArrayList<String> frList = new ArrayList();
        for (String black2 : this.mFRRomBlackList) {
            try {
                if (pm.getPackageUid(black2, DumpState.DUMP_PREFERRED_XML) != -1) {
                    frList.add(black2);
                }
            } catch (NameNotFoundException e3) {
            } catch (Exception e4) {
            }
        }
        synchronized (this.mLock) {
            this.mBlackList = list;
            this.mFRBlackList = frList;
            this.mFile.delete();
            this.mWriteScheduled = true;
        }
    }

    public boolean inBlacklist(String packageName) {
        synchronized (this.mLock) {
            for (String black : this.mBlackList) {
                if (packageName.startsWith(black)) {
                    return true;
                }
            }
            return false;
        }
    }

    public boolean inFRBlacklist(String packageName) {
        synchronized (this.mLock) {
            for (String black : this.mFRBlackList) {
                if (packageName.startsWith(black)) {
                    return true;
                }
            }
            return false;
        }
    }

    public boolean inBGBlacklist(String packageName) {
        synchronized (this.mLock) {
            for (String black : this.mBGBlackList) {
                if (packageName.startsWith(black)) {
                    return true;
                }
            }
            return false;
        }
    }

    public void shutdown() {
        if (this.mListener != null) {
            this.mListener.stopWatching();
        }
        if (this.mWriteScheduled) {
            writeToInstallFile();
        }
    }

    public void dump(PrintWriter pw) {
        synchronized (this.mLock) {
            pw.println("blacklist:");
            for (String black : this.mBlackList) {
                pw.println("    " + black);
            }
            pw.println("FRBlacklist:");
            for (String black2 : this.mFRBlackList) {
                pw.println("    " + black2);
            }
            pw.println("RomBlacklist:");
            for (String black22 : this.mRomBlackList) {
                pw.println("    " + black22);
            }
            pw.println("FRRomBlacklist:");
            for (String black222 : this.mFRRomBlackList) {
                pw.println("    " + black222);
            }
            pw.println("BGBlacklist:");
            for (String black2222 : this.mBGBlackList) {
                pw.println("    " + black2222);
            }
        }
    }
}
