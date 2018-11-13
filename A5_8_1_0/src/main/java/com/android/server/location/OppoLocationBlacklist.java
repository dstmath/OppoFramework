package com.android.server.location;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.location.LocAppsOp;
import android.net.Uri;
import android.os.Handler;
import android.text.TextUtils;
import android.util.AtomicFile;
import android.util.Log;
import android.util.Xml;
import com.android.internal.content.PackageMonitor;
import com.android.internal.util.FastXmlSerializer;
import com.android.server.am.OppoCrashClearManager;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class OppoLocationBlacklist {
    public static final String ACTION_ROM_UPDATE_CONFIG = "oppo.intent.action.ROM_UPDATE_CONFIG_SUCCESS";
    public static final int ALLOW = 1;
    private static final String APP_INSTALL_FILE_DIR = "data/system/sys_gps_lbs_app_install.xml";
    private static final String COLUMN_NAME_1 = "version";
    private static final String COLUMN_NAME_2 = "xml";
    private static final Uri CONTENT_URI_WHITE_LIST = Uri.parse("content://com.nearme.romupdate.provider.db/update_list");
    private static final String DATA_FILE_DIR = "data/system/sys_gps_lbs_config.xml";
    private static boolean DEBUG = false;
    public static final int FG_ONLY = 2;
    private static final int GPS_PERMISSION_LIST_DEF_SIZE = 32;
    private static final String PERMISSION_GPS = "android.permission.ACCESS_FINE_LOCATION";
    public static final int PROHIBIT = 3;
    public static final int PROHIBIT_FR = 4;
    public static final String ROM_UPDATE_CONFIG_LIST = "ROM_UPDATE_CONFIG_LIST";
    private static final String SYS_FILE_DIR = "system/etc/sys_gps_lbs_config.xml";
    public static final String SYS_GPS_LBS_CFG = "sys_gps_lbs_config";
    private static final String TAG = "OLB";
    public static final int UNSET = 0;
    private static final long WRITE_DELAY = 10000;
    private final Context mContext;
    private ArrayList<String> mFRBlackList;
    private AtomicFile mFile;
    private Handler mHandler;
    private final Object mLock = new Object();
    private int mOpLevel = 3;
    private HashMap<String, Integer> mOpList;
    private final PackageMonitor mPackageMonitor = new PackageMonitor() {
        public void onPackageAdded(String name, int uid) {
            if (OppoLocationBlacklist.DEBUG) {
                Log.d(OppoLocationBlacklist.TAG, "add: " + name);
            }
            synchronized (OppoLocationBlacklist.this.mLock) {
                if (OppoLocationBlacklist.this.mRomOpList == null) {
                    OppoLocationBlacklist.this.loadDefaultCfg();
                }
                if (OppoLocationBlacklist.this.mRomOpList != null && OppoLocationBlacklist.this.mRomOpList.size() > 0) {
                    PackageInfo info = null;
                    try {
                        info = OppoLocationBlacklist.this.mPm.getPackageInfo(name, 4096);
                    } catch (NameNotFoundException e) {
                    }
                    if (info != null) {
                        if (OppoLocationBlacklist.this.isGpsApp(info)) {
                            Integer keyOfRom = (Integer) OppoLocationBlacklist.this.mRomOpList.get(name);
                            int opOfRom = (keyOfRom != null ? keyOfRom.intValue() : 2) << 3;
                            Integer key = (Integer) OppoLocationBlacklist.this.mOpList.get(name);
                            OppoLocationBlacklist.this.mOpList.put(name, Integer.valueOf(key != null ? key.intValue() | opOfRom : opOfRom));
                            for (String black : OppoLocationBlacklist.this.mRomFRBlackList) {
                                if (name.startsWith(black)) {
                                    OppoLocationBlacklist.this.mFRBlackList.add(name);
                                    break;
                                }
                            }
                            OppoLocationBlacklist.this.mWriteScheduled = true;
                        }
                    }
                }
                if (OppoLocationBlacklist.this.mWriteScheduled) {
                    OppoLocationBlacklist.this.scheduleWriteLocked();
                }
            }
        }

        public void onPackageRemoved(String name, int uid) {
            if (OppoLocationBlacklist.DEBUG) {
                Log.d(OppoLocationBlacklist.TAG, "remove: " + name);
            }
            synchronized (OppoLocationBlacklist.this.mLock) {
                if (OppoLocationBlacklist.this.mOpList.remove(name) != null) {
                    OppoLocationBlacklist.this.mWriteScheduled = true;
                }
                if (OppoLocationBlacklist.this.mFRBlackList.remove(name)) {
                    OppoLocationBlacklist.this.mWriteScheduled = true;
                }
                if (OppoLocationBlacklist.this.mWriteScheduled) {
                    OppoLocationBlacklist.this.scheduleWriteLocked();
                }
            }
        }

        public void onPackageUpdateFinished(String name, int uid) {
            if (OppoLocationBlacklist.DEBUG) {
                Log.d(OppoLocationBlacklist.TAG, "update: " + name);
            }
            synchronized (OppoLocationBlacklist.this.mLock) {
                if (OppoLocationBlacklist.this.mRomOpList == null) {
                    OppoLocationBlacklist.this.loadDefaultCfg();
                }
                if (OppoLocationBlacklist.this.mRomOpList != null && OppoLocationBlacklist.this.mRomOpList.size() > 0) {
                    PackageInfo info = null;
                    try {
                        info = OppoLocationBlacklist.this.mPm.getPackageInfo(name, 4096);
                    } catch (NameNotFoundException e) {
                    }
                    if (info != null) {
                        if (OppoLocationBlacklist.this.isGpsApp(info)) {
                            Integer keyOfRom = (Integer) OppoLocationBlacklist.this.mRomOpList.get(name);
                            int opOfRom = (keyOfRom != null ? keyOfRom.intValue() : 2) << 3;
                            Integer key = (Integer) OppoLocationBlacklist.this.mOpList.get(name);
                            OppoLocationBlacklist.this.mOpList.put(name, Integer.valueOf(key != null ? key.intValue() | opOfRom : opOfRom));
                            if (!OppoLocationBlacklist.this.mFRBlackList.contains(name)) {
                                for (String black : OppoLocationBlacklist.this.mRomFRBlackList) {
                                    if (name.startsWith(black)) {
                                        OppoLocationBlacklist.this.mFRBlackList.add(name);
                                        break;
                                    }
                                }
                            }
                            OppoLocationBlacklist.this.mWriteScheduled = true;
                        }
                    }
                    if (OppoLocationBlacklist.this.mOpList.remove(name) != null) {
                        OppoLocationBlacklist.this.mWriteScheduled = true;
                    }
                    if (OppoLocationBlacklist.this.mFRBlackList.remove(name)) {
                        OppoLocationBlacklist.this.mWriteScheduled = true;
                    }
                }
                if (OppoLocationBlacklist.this.mWriteScheduled) {
                    OppoLocationBlacklist.this.scheduleWriteLocked();
                }
            }
        }
    };
    private PackageManager mPm;
    private ArrayList<String> mRomFRBlackList;
    private HashMap<String, Integer> mRomOpList;
    private BroadcastReceiver mRomUpdateReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (OppoLocationBlacklist.DEBUG) {
                Log.d(OppoLocationBlacklist.TAG, "action: " + action);
            }
            if ("oppo.intent.action.ROM_UPDATE_CONFIG_SUCCESS".equals(action)) {
                List<String> list = intent.getStringArrayListExtra("ROM_UPDATE_CONFIG_LIST");
                if (list != null && !list.isEmpty() && (list.contains(OppoLocationBlacklist.SYS_GPS_LBS_CFG) ^ 1) == 0) {
                    OppoLocationBlacklist.this.updateFromProvider();
                    OppoLocationBlacklist.this.readAppOps(false);
                }
            } else if ("android.intent.action.BOOT_COMPLETED".equals(action)) {
                OppoLocationBlacklist.this.readAppOps(true);
            }
        }
    };
    private int mVersion = -1;
    private final Runnable mWriteRunner = new Runnable() {
        public void run() {
            synchronized (OppoLocationBlacklist.this.mLock) {
                OppoLocationBlacklist.this.mWriteScheduled = false;
                OppoLocationBlacklist.this.writeToInstallFile();
            }
        }
    };
    private boolean mWriteScheduled = false;

    public OppoLocationBlacklist(Context context, Handler handler) {
        this.mContext = context;
        this.mHandler = handler;
        synchronized (this.mLock) {
            this.mOpList = new HashMap(32);
            this.mFRBlackList = new ArrayList();
            this.mRomOpList = new HashMap();
            this.mRomFRBlackList = new ArrayList();
        }
        this.mFile = new AtomicFile(new File(APP_INSTALL_FILE_DIR));
        this.mPm = this.mContext.getPackageManager();
    }

    public void init() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("oppo.intent.action.ROM_UPDATE_CONFIG_SUCCESS");
        intentFilter.addAction("android.intent.action.BOOT_COMPLETED");
        this.mContext.registerReceiver(this.mRomUpdateReceiver, intentFilter, null, this.mHandler);
        this.mPackageMonitor.register(this.mContext, this.mHandler.getLooper(), true);
    }

    public void enableLog(int verbose) {
        boolean z = false;
        if (verbose > 0) {
            z = true;
        }
        DEBUG = z;
        Log.e(TAG, "debug:" + DEBUG);
    }

    public int getOpLevel() {
        return this.mOpLevel;
    }

    /* JADX WARNING: Removed duplicated region for block: B:33:0x0049 A:{SYNTHETIC, Splitter: B:33:0x0049} */
    /* JADX WARNING: Removed duplicated region for block: B:41:0x005a A:{SYNTHETIC, Splitter: B:41:0x005a} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public String readFromFile(File path) {
        IOException e;
        FileNotFoundException e2;
        Throwable th;
        if (path == null) {
            return "";
        }
        InputStream is = null;
        try {
            InputStream is2 = new FileInputStream(path);
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(is2));
                StringBuffer buffer = new StringBuffer();
                String str = "";
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

    /* JADX WARNING: Removed duplicated region for block: B:28:0x0039 A:{SYNTHETIC, Splitter: B:28:0x0039} */
    /* JADX WARNING: Removed duplicated region for block: B:19:0x002b A:{SYNTHETIC, Splitter: B:19:0x002b} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean saveToFile(String content, String filePath) {
        Exception e;
        Throwable th;
        FileOutputStream outStream = null;
        try {
            FileOutputStream outStream2 = new FileOutputStream(new File(filePath));
            if (outStream2 != null) {
                try {
                    outStream2.write(content.getBytes());
                    outStream2.close();
                    outStream = null;
                } catch (Exception e2) {
                    e = e2;
                    outStream = outStream2;
                    try {
                        e.printStackTrace();
                        if (outStream != null) {
                            try {
                                outStream.close();
                            } catch (Exception e1) {
                                e1.printStackTrace();
                            }
                        }
                        return false;
                    } catch (Throwable th2) {
                        th = th2;
                        if (outStream != null) {
                            try {
                                outStream.close();
                            } catch (Exception e12) {
                                e12.printStackTrace();
                            }
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    outStream = outStream2;
                    if (outStream != null) {
                    }
                    throw th;
                }
            }
            outStream = outStream2;
            if (outStream != null) {
                try {
                    outStream.close();
                } catch (Exception e122) {
                    e122.printStackTrace();
                }
            }
            return true;
        } catch (Exception e3) {
            e = e3;
            e.printStackTrace();
            if (outStream != null) {
            }
            return false;
        }
    }

    private void loadDefaultCfg() {
        File file = new File(DATA_FILE_DIR);
        String contentOfData = null;
        if (file.exists()) {
            contentOfData = readFromFile(file);
        }
        String contentOfSys = null;
        file = new File(SYS_FILE_DIR);
        if (file.exists()) {
            contentOfSys = readFromFile(file);
        }
        int versionOfData = getVersion(contentOfData);
        int versionOfSys = getVersion(contentOfSys);
        if (versionOfData == -1 || versionOfData < versionOfSys) {
            contentOfData = contentOfSys;
            versionOfData = versionOfSys;
        }
        this.mVersion = versionOfData;
        if (!parseContentFromXML(contentOfData, false)) {
            this.mVersion = versionOfSys;
            parseContentFromXML(contentOfSys, false);
        }
    }

    private boolean isGpsApp(PackageInfo info) {
        if (info.requestedPermissions == null) {
            return false;
        }
        for (String permission : info.requestedPermissions) {
            if ("android.permission.ACCESS_FINE_LOCATION".equals(permission)) {
                return true;
            }
        }
        return false;
    }

    private void readAppOps(boolean boot) {
        int version = -1;
        if (boot) {
            File file = new File(APP_INSTALL_FILE_DIR);
            if (file.exists()) {
                String content = readFromFile(file);
                version = getVersion(content);
                if (version != -1) {
                    parseContentFromXML(content, true);
                }
            }
        }
        loadDefaultCfg();
        synchronized (this.mLock) {
            if (version < this.mVersion && this.mRomOpList.size() > 0) {
                List apps;
                try {
                    apps = this.mPm.getInstalledPackages(4096);
                } catch (Exception e) {
                    this.mVersion = 20171111;
                    apps = null;
                }
                if (apps != null) {
                    int N = apps.size();
                    if (DEBUG) {
                        Log.d(TAG, "size:" + N);
                    }
                    this.mFRBlackList.clear();
                    int i = 0;
                    while (i < N) {
                        String name = ((PackageInfo) apps.get(i)).packageName;
                        ApplicationInfo info = ((PackageInfo) apps.get(i)).applicationInfo;
                        if (info != null && (info.flags & 1) == 0 && isGpsApp((PackageInfo) apps.get(i))) {
                            Integer keyOfRom = (Integer) this.mRomOpList.get(name);
                            int opOfRom = (keyOfRom != null ? keyOfRom.intValue() : 2) << 3;
                            Integer key = (Integer) this.mOpList.get(name);
                            int op = key != null ? (key.intValue() & 7) | opOfRom : opOfRom;
                            this.mOpList.put(name, Integer.valueOf(op));
                            if (DEBUG) {
                                Log.d(TAG, "put-pkg:" + name + ",op:" + op);
                            }
                        } else if (DEBUG) {
                            Log.d(TAG, "not-pkg:" + name);
                        }
                        for (String black : this.mRomFRBlackList) {
                            if (name.startsWith(black)) {
                                this.mFRBlackList.add(name);
                                break;
                            }
                        }
                        i++;
                    }
                    this.mWriteScheduled = true;
                }
            }
            if (this.mWriteScheduled) {
                scheduleWriteLocked();
            }
        }
    }

    private int getVersion(String content) {
        int version = -1;
        if (content == null || content.isEmpty()) {
            return -1;
        }
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(new StringReader(content));
            while (true) {
                int type = parser.next();
                if (type != 1) {
                    String tag = parser.getName();
                    switch (type) {
                        case 2:
                            if (!"version".equals(tag)) {
                                break;
                            }
                            try {
                                String versionStr = parser.nextText();
                                if (DEBUG) {
                                    Log.d(TAG, "version:" + versionStr);
                                }
                                if (versionStr != null) {
                                    version = Integer.parseInt(versionStr.trim());
                                }
                            } catch (NumberFormatException e) {
                            }
                            return version;
                        default:
                            break;
                    }
                }
            }
        } catch (XmlPullParserException e2) {
        } catch (IOException e3) {
        }
        return -1;
    }

    private boolean parseContentFromXML(String content, boolean isInstallFile) {
        if (content == null || content.isEmpty()) {
            return false;
        }
        ArrayList<String> frPkgs = new ArrayList();
        HashMap<String, Integer> opList = new HashMap(Vr2dDisplay.DEFAULT_VIRTUAL_DISPLAY_DPI);
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(new StringReader(content));
            boolean insidePermissionList = false;
            while (true) {
                int type = parser.next();
                if (type != 1) {
                    String tag = parser.getName();
                    switch (type) {
                        case 2:
                            if (!"permission_list".equals(tag)) {
                                if (!"pkg".equals(tag)) {
                                    if (!"op_level".equals(tag)) {
                                        break;
                                    }
                                    String opLevelStr = parser.nextText();
                                    if (!TextUtils.isEmpty(opLevelStr)) {
                                        try {
                                            this.mOpLevel = Integer.parseInt(opLevelStr);
                                            break;
                                        } catch (NumberFormatException e) {
                                            break;
                                        }
                                    }
                                    break;
                                } else if (!insidePermissionList) {
                                    break;
                                } else {
                                    String pkgName = parser.getAttributeValue(null, OppoCrashClearManager.CRASH_COUNT);
                                    String opStr = parser.getAttributeValue(null, "op");
                                    if (DEBUG) {
                                        Log.d(TAG, "pkg:" + pkgName + ", op:" + opStr);
                                    }
                                    if (!(TextUtils.isEmpty(pkgName) || TextUtils.isEmpty(opStr))) {
                                        try {
                                            int op = Integer.parseInt(opStr) & 255;
                                            switch (op) {
                                                case 4:
                                                    frPkgs.add(pkgName);
                                                    break;
                                                default:
                                                    opList.put(pkgName, Integer.valueOf(op));
                                                    break;
                                            }
                                        } catch (NumberFormatException e2) {
                                            break;
                                        }
                                    }
                                }
                            }
                            insidePermissionList = true;
                            break;
                        case 3:
                            if (!"permission_list".equals(tag)) {
                                break;
                            }
                            insidePermissionList = false;
                            break;
                        default:
                            break;
                    }
                }
                synchronized (this.mLock) {
                    if (isInstallFile) {
                        this.mOpList = opList;
                        this.mFRBlackList = frPkgs;
                    } else {
                        this.mRomOpList = opList;
                        this.mRomFRBlackList = frPkgs;
                    }
                }
                return true;
            }
        } catch (XmlPullParserException e3) {
            Log.w(TAG, "problem reading blacklist", e3);
            return false;
        } catch (IOException e4) {
            return false;
        }
    }

    private void writeToInstallFile() {
        try {
            FileOutputStream stream = this.mFile.startWrite();
            try {
                XmlSerializer out = new FastXmlSerializer();
                out.setOutput(stream, StandardCharsets.UTF_8.name());
                out.startDocument(null, Boolean.valueOf(true));
                if (DEBUG) {
                    Log.d(TAG, "write version:" + this.mVersion);
                }
                out.startTag(null, "version");
                out.text(Integer.toString(this.mVersion));
                out.endTag(null, "version");
                out.startTag(null, "op_level");
                out.text(Integer.toString(this.mOpLevel));
                out.endTag(null, "op_level");
                out.startTag(null, "permission_list");
                synchronized (this.mLock) {
                    for (Entry<String, Integer> entry : this.mOpList.entrySet()) {
                        out.startTag(null, "pkg");
                        out.attribute(null, OppoCrashClearManager.CRASH_COUNT, (String) entry.getKey());
                        out.attribute(null, "op", ((Integer) entry.getValue()).toString());
                        out.endTag(null, "pkg");
                    }
                }
                for (String black : this.mFRBlackList) {
                    out.startTag(null, "pkg");
                    out.attribute(null, OppoCrashClearManager.CRASH_COUNT, black);
                    out.attribute(null, "op", Integer.toString(4));
                    out.endTag(null, "pkg");
                }
                out.endTag(null, "permission_list");
                out.endDocument();
                this.mFile.finishWrite(stream);
                if (DEBUG) {
                    Log.d(TAG, "end");
                }
            } catch (IOException e) {
                Log.w(TAG, "Failed to write list, restoring backup.", e);
                this.mFile.failWrite(stream);
            }
        } catch (IOException e2) {
            if (DEBUG) {
                Log.w(TAG, "Failed to write cache: " + e2);
            }
        }
    }

    private void scheduleWriteLocked() {
        this.mHandler.removeCallbacks(this.mWriteRunner);
        this.mHandler.postDelayed(this.mWriteRunner, 10000);
    }

    /* JADX WARNING: Missing block: B:17:0x0025, code:
            return r4;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int getAppOp(String packageName) {
        synchronized (this.mLock) {
            if (this.mRomOpList == null || this.mRomOpList.size() <= 0) {
                return 1;
            }
            Integer key = (Integer) this.mOpList.get(packageName);
            if (key != null) {
                int op = key.intValue();
                int setOp = op & 7;
                if (setOp == 0) {
                    setOp = op >> 3;
                }
            } else {
                Integer keyOfRom = (Integer) this.mRomOpList.get(packageName);
                int opOfRom = keyOfRom != null ? keyOfRom.intValue() : 2;
                this.mOpList.put(packageName, Integer.valueOf(opOfRom << 3));
                return opOfRom;
            }
        }
    }

    public boolean inFRBlacklist(String packageName) {
        synchronized (this.mLock) {
            for (String black : this.mFRBlackList) {
                if (packageName.startsWith(black)) {
                    if (DEBUG) {
                        Log.d(TAG, "block frpkg: " + packageName);
                    }
                    return true;
                }
            }
            return false;
        }
    }

    public void getLocAppsOp(int flag, LocAppsOp locAppsOp) {
        if (DEBUG) {
            Log.d(TAG, "getLocAppsOp flag is : " + flag);
        }
        Object obj;
        switch (flag) {
            case 1:
                obj = this.mLock;
                synchronized (obj) {
                    if (this.mRomOpList != null) {
                        locAppsOp.setOpLevel(this.mOpLevel);
                        locAppsOp.setAppsOp(this.mRomOpList);
                        if (DEBUG) {
                            if (this.mRomOpList == null) {
                                Log.d(TAG, "mRomOpList's null!!");
                                break;
                            } else {
                                Log.d(TAG, "mRomOpList's size : " + this.mRomOpList.size());
                                break;
                            }
                        }
                    }
                    locAppsOp.setOpLevel(-1);
                    break;
                }
                break;
            case 2:
                obj = this.mLock;
                synchronized (obj) {
                    locAppsOp.setOpLevel(this.mOpLevel);
                    locAppsOp.setAppsOp(this.mOpList);
                    if (DEBUG) {
                        if (this.mOpList == null) {
                            Log.d(TAG, "mOpList's null!!");
                            break;
                        } else {
                            Log.d(TAG, "mOpList's size : " + this.mOpList.size());
                            break;
                        }
                    }
                }
                break;
            case 3:
                locAppsOp.setOpLevel(this.mOpLevel);
                return;
            default:
                return;
        }
    }

    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setLocAppsOp(int cmd, LocAppsOp locAppsOp) {
        synchronized (this.mLock) {
            String key;
            switch (cmd) {
                case 0:
                    HashMap<String, Integer> removeList = locAppsOp.getAppsOp();
                    if (removeList != null) {
                        for (Entry<String, Integer> entry : removeList.entrySet()) {
                            key = (String) entry.getKey();
                            if (DEBUG) {
                                Log.d(TAG, "r-pkg:" + key);
                            }
                            if (!(key == null || this.mOpList.remove(key) == null)) {
                                this.mWriteScheduled = true;
                            }
                        }
                    }
                case 1:
                    HashMap<String, Integer> updateList = locAppsOp.getAppsOp();
                    if (updateList != null) {
                        for (Entry<String, Integer> entry2 : updateList.entrySet()) {
                            key = (String) entry2.getKey();
                            Integer val = (Integer) entry2.getValue();
                            if (DEBUG) {
                                Log.d(TAG, "u-pkg:" + key + ", op:" + val);
                            }
                            if (!(key == null || val == null)) {
                                this.mOpList.put(key, val);
                            }
                        }
                        this.mWriteScheduled = true;
                    }
                default:
                    if (this.mWriteScheduled) {
                        scheduleWriteLocked();
                    }
            }
        }
    }

    public void updateFromProvider() {
        try {
            String tmp = getDataFromProvider();
            if (DEBUG) {
                Log.d(TAG, "rom xml: " + tmp);
            }
            if (tmp != null) {
                saveToFile(tmp, DATA_FILE_DIR);
            } else {
                Log.e(TAG, "Rom xml is null!!!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getDataFromProvider() {
        Cursor cursor = null;
        String returnStr = null;
        String[] projection = new String[]{"version", COLUMN_NAME_2};
        try {
            if (this.mContext == null) {
                return null;
            }
            cursor = this.mContext.getContentResolver().query(CONTENT_URI_WHITE_LIST, projection, "filtername=\"sys_gps_lbs_config\"", null, null);
            if (cursor != null && cursor.getCount() > 0) {
                int versioncolumnIndex = cursor.getColumnIndex("version");
                int xmlcolumnIndex = cursor.getColumnIndex(COLUMN_NAME_2);
                cursor.moveToNext();
                int configVersion = cursor.getInt(versioncolumnIndex);
                returnStr = cursor.getString(xmlcolumnIndex);
                if (DEBUG) {
                    Log.d(TAG, "White List updated, version = " + configVersion);
                }
            }
            if (cursor != null) {
                cursor.close();
            }
            return returnStr;
        } catch (Exception e) {
            Log.w(TAG, "We can not get white list data from provider, because of " + e);
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public void shutdown() {
        synchronized (this.mLock) {
            if (this.mWriteScheduled) {
                this.mHandler.removeCallbacks(this.mWriteRunner);
                this.mWriteScheduled = false;
                writeToInstallFile();
            }
        }
    }

    public void dump(PrintWriter pw) {
        synchronized (this.mLock) {
            if (!(this.mOpList == null || this.mRomOpList == null)) {
                if (!(this.mFRBlackList == null || this.mRomFRBlackList == null)) {
                    pw.println("opList:");
                    for (Entry<String, Integer> entry : this.mOpList.entrySet()) {
                        pw.println("    " + ((String) entry.getKey()) + ":" + entry.getValue());
                    }
                    pw.println("RomOpList:");
                    for (Entry<String, Integer> entry2 : this.mRomOpList.entrySet()) {
                        pw.println("    " + ((String) entry2.getKey()) + ":" + entry2.getValue());
                    }
                    pw.println("FRBlacklist:");
                    for (String black : this.mFRBlackList) {
                        pw.println("    " + black);
                    }
                    pw.println("RomFRBlacklist:");
                    for (String black2 : this.mRomFRBlackList) {
                        pw.println("    " + black2);
                    }
                    return;
                }
            }
            pw.println("list not init");
        }
    }
}
