package com.android.server.location;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.LocAppsOp;
import android.os.Handler;
import android.text.TextUtils;
import android.util.AtomicFile;
import android.util.Log;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.content.PackageMonitor;
import com.android.internal.util.FastXmlSerializer;
import com.android.server.connectivity.networkrecovery.dnsresolve.StringUtils;
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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

public class OppoLocationBlacklist {
    public static final int ALLOW = 1;
    private static final String APP_INSTALL_FILE_DIR = "data/system/sys_gps_lbs_app_install.xml";
    private static final String ATTR_NAME = "name";
    private static final String COLUMN_NAME_1 = "version";
    private static final String COLUMN_NAME_2 = "xml";
    private static final String DATA_FILE_DIR = "data/system/sys_gps_lbs_config.xml";
    /* access modifiers changed from: private */
    public static boolean DEBUG = false;
    public static final int FG_ONLY = 2;
    private static final int GPS_PERMISSION_LIST_DEF_SIZE = 32;
    private static final String KEY_GPS_BG_RUNNING_APP = "config_gpsBackgroudRunningApp";
    private static final String KEY_GPS_FASTREPORT_APP = "config_gpsfastReportApp";
    private static final String KEY_GPS_FG_RUNNING_APP = "config_gpsForegroudRunningApp";
    private static final String KEY_GPS_FORBIDDEN_RUNNING_APP = "config_gpsForbiddenRunningApp";
    private static final String OPPO_LBS_CONFIG_UPDATE_ACTION = "com.android.location.oppo.lbsconfig.update.success";
    private static final String PERMISSION_GPS = "android.permission.ACCESS_FINE_LOCATION";
    public static final int PROHIBIT = 3;
    public static final int PROHIBIT_FR = 4;
    private static final String SYS_FILE_DIR = "system/etc/sys_gps_lbs_config.xml";
    private static final String TAG = "OppoLocationBlacklist";
    private static final String TAG_ITEM = "item";
    private static final String TAG_STRING_ARRAY = "string-array";
    private static final String TAG_VERSION = "version";
    public static final int UNSET = 0;
    private static final long WRITE_DELAY = 10000;
    private final Context mContext;
    /* access modifiers changed from: private */
    @GuardedBy({"mLock"})
    public ArrayList<String> mFRBlackList;
    private AtomicFile mFile;
    private Handler mHandler;
    /* access modifiers changed from: private */
    public final Object mLock = new Object();
    private int mOpLevel = 3;
    /* access modifiers changed from: private */
    @GuardedBy({"mLock"})
    public HashMap<String, Integer> mOpList = null;
    private final PackageMonitor mPackageMonitor = new PackageMonitor() {
        /* class com.android.server.location.OppoLocationBlacklist.AnonymousClass3 */

        public void onPackageAdded(String name, int uid) {
            if (OppoLocationBlacklist.DEBUG) {
                Log.d(OppoLocationBlacklist.TAG, "add: " + name);
            }
            synchronized (OppoLocationBlacklist.this.mLock) {
                if (OppoLocationBlacklist.this.mRomOpList == null) {
                    int unused = OppoLocationBlacklist.this.updateDataFromRus();
                }
                if (OppoLocationBlacklist.this.mRomOpList != null && OppoLocationBlacklist.this.mRomOpList.size() > 0) {
                    PackageInfo info = null;
                    try {
                        info = OppoLocationBlacklist.this.mPm.getPackageInfo(name, 4096);
                    } catch (PackageManager.NameNotFoundException e) {
                    }
                    if (info != null && OppoLocationBlacklist.this.isGpsApp(info)) {
                        Integer keyOfRom = (Integer) OppoLocationBlacklist.this.mRomOpList.get(name);
                        int opOfRom = (keyOfRom != null ? keyOfRom.intValue() : 1) << 3;
                        Integer key = (Integer) OppoLocationBlacklist.this.mOpList.get(name);
                        OppoLocationBlacklist.this.mOpList.put(name, Integer.valueOf(key != null ? key.intValue() | opOfRom : opOfRom));
                        if (OppoLocationBlacklist.this.mRomFRBlackList != null) {
                            Iterator it = OppoLocationBlacklist.this.mRomFRBlackList.iterator();
                            while (true) {
                                if (it.hasNext()) {
                                    if (name.startsWith((String) it.next())) {
                                        OppoLocationBlacklist.this.mFRBlackList.add(name);
                                        break;
                                    }
                                } else {
                                    break;
                                }
                            }
                        }
                        boolean unused2 = OppoLocationBlacklist.this.mWriteScheduled = true;
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
                    boolean unused = OppoLocationBlacklist.this.mWriteScheduled = true;
                }
                if (OppoLocationBlacklist.this.mFRBlackList.remove(name)) {
                    boolean unused2 = OppoLocationBlacklist.this.mWriteScheduled = true;
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
                    int unused = OppoLocationBlacklist.this.updateDataFromRus();
                }
                if (OppoLocationBlacklist.this.mRomOpList != null && OppoLocationBlacklist.this.mRomOpList.size() > 0) {
                    PackageInfo info = null;
                    try {
                        info = OppoLocationBlacklist.this.mPm.getPackageInfo(name, 4096);
                    } catch (PackageManager.NameNotFoundException e) {
                    }
                    if (info == null || !OppoLocationBlacklist.this.isGpsApp(info)) {
                        if (OppoLocationBlacklist.this.mOpList.remove(name) != null) {
                            boolean unused2 = OppoLocationBlacklist.this.mWriteScheduled = true;
                        }
                        if (OppoLocationBlacklist.this.mFRBlackList.remove(name)) {
                            boolean unused3 = OppoLocationBlacklist.this.mWriteScheduled = true;
                        }
                    } else {
                        Integer keyOfRom = (Integer) OppoLocationBlacklist.this.mRomOpList.get(name);
                        int opOfRom = (keyOfRom != null ? keyOfRom.intValue() : 1) << 3;
                        Integer key = (Integer) OppoLocationBlacklist.this.mOpList.get(name);
                        OppoLocationBlacklist.this.mOpList.put(name, Integer.valueOf(key != null ? key.intValue() | opOfRom : opOfRom));
                        if (!OppoLocationBlacklist.this.mFRBlackList.contains(name) && OppoLocationBlacklist.this.mRomFRBlackList != null) {
                            Iterator it = OppoLocationBlacklist.this.mRomFRBlackList.iterator();
                            while (true) {
                                if (it.hasNext()) {
                                    if (name.startsWith((String) it.next())) {
                                        OppoLocationBlacklist.this.mFRBlackList.add(name);
                                        break;
                                    }
                                } else {
                                    break;
                                }
                            }
                        }
                        boolean unused4 = OppoLocationBlacklist.this.mWriteScheduled = true;
                    }
                }
                if (OppoLocationBlacklist.this.mWriteScheduled) {
                    OppoLocationBlacklist.this.scheduleWriteLocked();
                }
            }
        }
    };
    /* access modifiers changed from: private */
    public PackageManager mPm;
    /* access modifiers changed from: private */
    @GuardedBy({"mLock"})
    public ArrayList<String> mRomFRBlackList;
    /* access modifiers changed from: private */
    @GuardedBy({"mLock"})
    public HashMap<String, Integer> mRomOpList = null;
    private BroadcastReceiver mRomUpdateReceiver = new BroadcastReceiver() {
        /* class com.android.server.location.OppoLocationBlacklist.AnonymousClass2 */

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            OppoLocationBlacklist oppoLocationBlacklist = OppoLocationBlacklist.this;
            oppoLocationBlacklist.printLogD("get action: " + action);
            if (OppoLocationBlacklist.OPPO_LBS_CONFIG_UPDATE_ACTION.equals(action)) {
                OppoLocationBlacklist.this.printLogD("Will update LBS Config!!");
                OppoLocationBlacklist.this.readAppOps(false);
            } else if ("android.intent.action.BOOT_COMPLETED".equals(action)) {
                OppoLocationBlacklist.this.readAppOps(true);
            }
        }
    };
    private OppoLbsRomUpdateUtil mRusUtil = null;
    private String mStringArrayAttName = null;
    private int mVersion = -1;
    private final Runnable mWriteRunner = new Runnable() {
        /* class com.android.server.location.OppoLocationBlacklist.AnonymousClass1 */

        public void run() {
            synchronized (OppoLocationBlacklist.this.mLock) {
                boolean unused = OppoLocationBlacklist.this.mWriteScheduled = false;
                OppoLocationBlacklist.this.writeToInstallFile();
            }
        }
    };
    /* access modifiers changed from: private */
    public boolean mWriteScheduled = false;

    public OppoLocationBlacklist(Context context, Handler handler) {
        this.mContext = context;
        this.mHandler = handler;
        synchronized (this.mLock) {
            this.mOpList = new HashMap<>((int) GPS_PERMISSION_LIST_DEF_SIZE);
            this.mFRBlackList = new ArrayList<>();
            this.mRomOpList = new HashMap<>();
            this.mRomFRBlackList = new ArrayList<>();
        }
        this.mFile = new AtomicFile(new File(APP_INSTALL_FILE_DIR));
        this.mPm = this.mContext.getPackageManager();
        this.mRusUtil = OppoLbsRomUpdateUtil.getInstall(context);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(OPPO_LBS_CONFIG_UPDATE_ACTION);
        intentFilter.addAction("android.intent.action.BOOT_COMPLETED");
        this.mContext.registerReceiver(this.mRomUpdateReceiver, intentFilter, "oppo.permission.OPPO_COMPONENT_SAFE", this.mHandler);
        this.mPackageMonitor.register(this.mContext, this.mHandler.getLooper(), true);
    }

    public static void setDebug(boolean isDebug) {
        DEBUG = isDebug;
    }

    public int getOpLevel() {
        return this.mOpLevel;
    }

    public String readFromFile(File path) {
        if (path == null) {
            return StringUtils.EMPTY;
        }
        InputStream is = null;
        try {
            InputStream is2 = new FileInputStream(path);
            BufferedReader in = new BufferedReader(new InputStreamReader(is2));
            StringBuffer buffer = new StringBuffer();
            while (true) {
                String line = in.readLine();
                if (line == null) {
                    break;
                }
                buffer.append(line);
            }
            String stringBuffer = buffer.toString();
            try {
                is2.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return stringBuffer;
        } catch (FileNotFoundException e2) {
            e2.printStackTrace();
            if (is == null) {
                return null;
            }
            is.close();
            return null;
        } catch (IOException e3) {
            e3.printStackTrace();
            if (is == null) {
                return null;
            }
            try {
                is.close();
                return null;
            } catch (IOException e4) {
                e4.printStackTrace();
                return null;
            }
        } catch (Throwable th) {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e5) {
                    e5.printStackTrace();
                }
            }
            throw th;
        }
    }

    private boolean saveToFile(String content, String filePath) {
        FileOutputStream outStream = null;
        try {
            outStream = new FileOutputStream(new File(filePath));
            outStream.write(content.getBytes());
            outStream.close();
            FileOutputStream outStream2 = null;
            if (outStream2 != null) {
                try {
                    outStream2.close();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            if (outStream == null) {
                return false;
            }
            try {
                outStream.close();
                return false;
            } catch (Exception e12) {
                e12.printStackTrace();
                return false;
            }
        } catch (Throwable th) {
            if (outStream != null) {
                try {
                    outStream.close();
                } catch (Exception e13) {
                    e13.printStackTrace();
                }
            }
            throw th;
        }
    }

    /* access modifiers changed from: private */
    public boolean isGpsApp(PackageInfo info) {
        if (info.requestedPermissions == null) {
            return false;
        }
        for (String permission : info.requestedPermissions) {
            if (PERMISSION_GPS.equals(permission)) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: private */
    public int updateDataFromRus() {
        int version;
        ArrayList<String> tempList = this.mRusUtil.getStringArray(KEY_GPS_BG_RUNNING_APP);
        printLogD("Bg running App : " + tempList.toString());
        synchronized (this.mLock) {
            Iterator<String> it = tempList.iterator();
            while (it.hasNext()) {
                this.mRomOpList.put(it.next(), new Integer(1));
            }
            ArrayList<String> tempList2 = this.mRusUtil.getStringArray(KEY_GPS_FG_RUNNING_APP);
            if (tempList2 != null) {
                printLogD("Fg running App : " + tempList2.toString());
                Iterator<String> it2 = tempList2.iterator();
                while (it2.hasNext()) {
                    this.mRomOpList.put(it2.next(), new Integer(2));
                }
            }
            ArrayList<String> tempList3 = this.mRusUtil.getStringArray(KEY_GPS_FORBIDDEN_RUNNING_APP);
            if (tempList3 != null) {
                printLogD("forbidden running App : " + tempList3.toString());
                Iterator<String> it3 = tempList3.iterator();
                while (it3.hasNext()) {
                    this.mRomOpList.put(it3.next(), new Integer(3));
                }
            }
            this.mRomFRBlackList = this.mRusUtil.getStringArray(KEY_GPS_FASTREPORT_APP);
            if (this.mRomFRBlackList != null) {
                printLogD("mRomFRBlackList  App : " + this.mRomFRBlackList.toString());
            }
            printLogD("mVersion = " + this.mRusUtil.getVersion());
            this.mVersion = this.mRusUtil.getVersion();
            version = this.mRusUtil.getVersion();
        }
        return version;
    }

    private boolean updateDataFromInstallFile() {
        File file = new File(APP_INSTALL_FILE_DIR);
        if (file.exists()) {
            return parseContentFromXML(readFromFile(file));
        }
        return false;
    }

    /* access modifiers changed from: private */
    public void readAppOps(boolean boot) {
        List<PackageInfo> allApps;
        updateDataFromRus();
        boolean updateOpList = true;
        if (boot && updateDataFromInstallFile()) {
            updateOpList = false;
        }
        synchronized (this.mLock) {
            if (updateOpList) {
                if (this.mRomOpList.size() > 0) {
                    try {
                        allApps = this.mPm.getInstalledPackages(4096);
                    } catch (Exception e) {
                        this.mVersion = 20190625;
                        allApps = null;
                    }
                    if (allApps != null) {
                        int appsSize = allApps.size();
                        if (DEBUG) {
                            Log.d(TAG, "apps size:" + appsSize);
                        }
                        this.mFRBlackList.clear();
                        int index = 0;
                        while (true) {
                            int opOfRom = 1;
                            if (index >= appsSize) {
                                break;
                            }
                            String name = allApps.get(index).packageName;
                            ApplicationInfo info = allApps.get(index).applicationInfo;
                            if (isGpsApp(allApps.get(index)) && info != null && (info.flags & 1) == 0) {
                                Integer keyOfRom = this.mRomOpList.get(name);
                                if (keyOfRom != null) {
                                    opOfRom = keyOfRom.intValue();
                                }
                                int opOfRom2 = opOfRom << 3;
                                Integer key = this.mOpList.get(name);
                                int op = key != null ? (key.intValue() & 7) | opOfRom2 : opOfRom2;
                                this.mOpList.put(name, Integer.valueOf(op));
                                if (DEBUG) {
                                    Log.d(TAG, "put-pkg:" + name + ",op:" + op);
                                }
                            } else if (DEBUG) {
                                Log.d(TAG, "not-pkg:" + name);
                            }
                            if (this.mRomFRBlackList != null) {
                                Iterator<String> it = this.mRomFRBlackList.iterator();
                                while (true) {
                                    if (it.hasNext()) {
                                        if (name.startsWith(it.next())) {
                                            this.mFRBlackList.add(name);
                                            break;
                                        }
                                    } else {
                                        break;
                                    }
                                }
                            }
                            index++;
                        }
                        this.mWriteScheduled = true;
                    }
                }
            }
            if (this.mWriteScheduled) {
                scheduleWriteLocked();
            }
        }
    }

    private boolean parseContentFromXML(String content) {
        String str;
        if (content == null) {
            Log.e(TAG, "parse content is null");
            return false;
        }
        int version = 0;
        try {
            XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
            if (!TextUtils.isEmpty(content)) {
                parser.setInput(new StringReader(content));
            }
            parser.nextTag();
            int eventType = parser.getEventType();
            while (true) {
                int opOfRom = 1;
                if (eventType != 1) {
                    String tagName = parser.getName();
                    if (2 == eventType) {
                        if (tagName.equals("version")) {
                            try {
                                version = Integer.valueOf(parser.nextText()).intValue();
                            } catch (NumberFormatException e) {
                                Log.e(TAG, "Get An Error When Parsing Version From XML File!");
                            }
                            if (this.mVersion > version) {
                                Log.e(TAG, "Version is old, Don't need update anything from the xml file!");
                                return false;
                            }
                            this.mVersion = version;
                        } else if (tagName.equals(TAG_STRING_ARRAY)) {
                            this.mStringArrayAttName = parser.getAttributeValue(null, ATTR_NAME);
                        } else if (tagName.equals(TAG_ITEM)) {
                            synchronized (this.mLock) {
                                String pkgName = parser.nextText();
                                Integer keyOfRom = this.mRomOpList.get(pkgName);
                                if (keyOfRom != null) {
                                    opOfRom = keyOfRom.intValue();
                                }
                                int opOfRom2 = opOfRom << 3;
                                if (this.mStringArrayAttName.equals(KEY_GPS_BG_RUNNING_APP)) {
                                    this.mOpList.put(pkgName, new Integer(opOfRom2 | 1));
                                } else if (this.mStringArrayAttName.equals(KEY_GPS_FG_RUNNING_APP)) {
                                    this.mOpList.put(pkgName, new Integer(opOfRom2 | 2));
                                } else if (this.mStringArrayAttName.equals(KEY_GPS_FORBIDDEN_RUNNING_APP)) {
                                    this.mOpList.put(pkgName, new Integer(opOfRom2 | 3));
                                } else if (this.mStringArrayAttName.equals(KEY_GPS_FASTREPORT_APP)) {
                                    this.mFRBlackList.add(pkgName);
                                }
                            }
                        } else {
                            continue;
                        }
                    } else if (3 == eventType && tagName.equals(TAG_STRING_ARRAY) && this.mStringArrayAttName != null) {
                        this.mStringArrayAttName = null;
                    }
                    eventType = parser.next();
                } else {
                    printLogD("Parse gnss content done!");
                    return true;
                }
            }
        } catch (XmlPullParserException e2) {
            Log.e(TAG, "Got XmlPullParser exception parsing!");
            return false;
        } catch (IOException e3) {
            Log.e(TAG, "Got IO exception parsing!!");
            return false;
        } finally {
            printLogD("Parse gnss content done!");
        }
    }

    /* access modifiers changed from: private */
    public void writeToInstallFile() {
        try {
            FileOutputStream stream = this.mFile.startWrite();
            try {
                XmlSerializer out = new FastXmlSerializer();
                out.setOutput(stream, StandardCharsets.UTF_8.name());
                out.startDocument(null, true);
                if (DEBUG) {
                    Log.d(TAG, "write version:" + this.mVersion);
                }
                out.startTag(null, "version");
                out.text(Integer.toString(this.mVersion));
                out.endTag(null, "version");
                out.startTag(null, "op_level");
                out.text(Integer.toString(this.mOpLevel));
                out.endTag(null, "op_level");
                synchronized (this.mLock) {
                    out.startTag(null, TAG_STRING_ARRAY);
                    out.attribute(null, ATTR_NAME, KEY_GPS_BG_RUNNING_APP);
                    for (Map.Entry<String, Integer> entry : this.mOpList.entrySet()) {
                        if ((entry.getValue().intValue() & 1) != 0) {
                            out.startTag(null, TAG_ITEM);
                            out.text(entry.getKey());
                            out.endTag(null, TAG_ITEM);
                        }
                    }
                    out.endTag(null, TAG_STRING_ARRAY);
                    out.startTag(null, TAG_STRING_ARRAY);
                    out.attribute(null, ATTR_NAME, KEY_GPS_FG_RUNNING_APP);
                    for (Map.Entry<String, Integer> entry2 : this.mOpList.entrySet()) {
                        if ((entry2.getValue().intValue() & 2) != 0) {
                            out.startTag(null, TAG_ITEM);
                            out.text(entry2.getKey());
                            out.endTag(null, TAG_ITEM);
                        }
                    }
                    out.endTag(null, TAG_STRING_ARRAY);
                    out.startTag(null, TAG_STRING_ARRAY);
                    out.attribute(null, ATTR_NAME, KEY_GPS_FORBIDDEN_RUNNING_APP);
                    for (Map.Entry<String, Integer> entry3 : this.mOpList.entrySet()) {
                        if ((entry3.getValue().intValue() & 3) != 0) {
                            out.startTag(null, TAG_ITEM);
                            out.text(entry3.getKey());
                            out.endTag(null, TAG_ITEM);
                        }
                    }
                    out.endTag(null, TAG_STRING_ARRAY);
                }
                out.startTag(null, TAG_STRING_ARRAY);
                out.attribute(null, ATTR_NAME, KEY_GPS_FASTREPORT_APP);
                Iterator<String> it = this.mFRBlackList.iterator();
                while (it.hasNext()) {
                    out.startTag(null, TAG_ITEM);
                    out.text(it.next());
                    out.endTag(null, TAG_ITEM);
                }
                out.endTag(null, KEY_GPS_FASTREPORT_APP);
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

    /* access modifiers changed from: private */
    public void scheduleWriteLocked() {
        this.mHandler.removeCallbacks(this.mWriteRunner);
        this.mHandler.postDelayed(this.mWriteRunner, WRITE_DELAY);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0028, code lost:
        return r4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0045, code lost:
        return 1;
     */
    public int getAppOp(String packageName) {
        synchronized (this.mLock) {
            int opOfRom = 1;
            if (this.mRomOpList != null) {
                if (this.mRomOpList.size() > 0) {
                    Integer key = this.mOpList.get(packageName);
                    if (key != null) {
                        int op = key.intValue();
                        int setOp = op & 7;
                        int i = setOp != 0 ? setOp : op >> 3;
                    } else {
                        Integer keyOfRom = this.mRomOpList.get(packageName);
                        if (keyOfRom != null) {
                            opOfRom = keyOfRom.intValue();
                        }
                        this.mOpList.put(packageName, Integer.valueOf(opOfRom << 3));
                        return opOfRom;
                    }
                }
            }
        }
    }

    public boolean inFRBlacklist(String packageName) {
        synchronized (this.mLock) {
            Iterator<String> it = this.mFRBlackList.iterator();
            while (it.hasNext()) {
                if (packageName.startsWith(it.next())) {
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
        if (flag == 1) {
            synchronized (this.mLock) {
                if (this.mRomOpList == null) {
                    locAppsOp.setOpLevel(-1);
                } else {
                    locAppsOp.setOpLevel(this.mOpLevel);
                    locAppsOp.setAppsOp(this.mRomOpList);
                    if (DEBUG) {
                        if (this.mRomOpList != null) {
                            Log.d(TAG, "mRomOpList's size : " + this.mRomOpList.size());
                        } else {
                            Log.d(TAG, "mRomOpList's null!!");
                        }
                    }
                }
            }
        } else if (flag == 2) {
            synchronized (this.mLock) {
                locAppsOp.setOpLevel(this.mOpLevel);
                locAppsOp.setAppsOp(this.mOpList);
                if (DEBUG) {
                    if (this.mOpList != null) {
                        Log.d(TAG, "mOpList's size : " + this.mOpList.size());
                    } else {
                        Log.d(TAG, "mOpList's null!!");
                    }
                }
            }
        } else if (flag == 3) {
            locAppsOp.setOpLevel(this.mOpLevel);
        }
    }

    public void setLocAppsOp(int cmd, LocAppsOp locAppsOp) {
        synchronized (this.mLock) {
            if (cmd == 0) {
                HashMap<String, Integer> removeList = locAppsOp.getAppsOp();
                if (removeList != null) {
                    for (Map.Entry<String, Integer> entry : removeList.entrySet()) {
                        String key = entry.getKey();
                        if (DEBUG) {
                            Log.d(TAG, "r-pkg:" + key);
                        }
                        if (!(key == null || this.mOpList.remove(key) == null)) {
                            this.mWriteScheduled = true;
                        }
                    }
                }
            } else if (cmd == 1) {
                HashMap<String, Integer> updateList = locAppsOp.getAppsOp();
                if (updateList != null) {
                    for (Map.Entry<String, Integer> entry2 : updateList.entrySet()) {
                        String key2 = entry2.getKey();
                        Integer val = entry2.getValue();
                        if (DEBUG) {
                            Log.d(TAG, "u-pkg:" + key2 + ", op:" + val);
                        }
                        if (!(key2 == null || val == null)) {
                            this.mOpList.put(key2, val);
                        }
                    }
                    this.mWriteScheduled = true;
                }
            }
            if (this.mWriteScheduled) {
                scheduleWriteLocked();
            }
        }
    }

    /* access modifiers changed from: private */
    public void printLogD(String message) {
        if (DEBUG) {
            Log.d(TAG, message);
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
            if (!(this.mOpList == null || this.mRomOpList == null || this.mFRBlackList == null)) {
                if (this.mRomFRBlackList != null) {
                    pw.println("opList:");
                    for (Map.Entry<String, Integer> entry : this.mOpList.entrySet()) {
                        pw.println("    " + entry.getKey() + ":" + entry.getValue());
                    }
                    pw.println("RomOpList:");
                    for (Map.Entry<String, Integer> entry2 : this.mRomOpList.entrySet()) {
                        pw.println("    " + entry2.getKey() + ":" + entry2.getValue());
                    }
                    pw.println("FRBlacklist:");
                    Iterator<String> it = this.mFRBlackList.iterator();
                    while (it.hasNext()) {
                        pw.println("    " + it.next());
                    }
                    pw.println("RomFRBlacklist:");
                    Iterator<String> it2 = this.mRomFRBlackList.iterator();
                    while (it2.hasNext()) {
                        pw.println("    " + it2.next());
                    }
                    return;
                }
            }
            pw.println("list not init");
        }
    }
}
