package com.mediatek.server.am.AutoBootControl;

import android.content.Context;
import android.content.Intent;
import android.content.pm.IPackageManager;
import android.content.pm.PackageInfo;
import android.content.pm.ParceledListSlice;
import android.content.pm.ResolveInfo;
import android.content.pm.UserInfo;
import android.os.Environment;
import android.os.FileObserver;
import android.os.IUserManager;
import android.os.RemoteException;
import android.os.UserHandle;
import android.util.AtomicFile;
import android.util.Log;
import android.util.Xml;
import com.android.internal.util.XmlUtils;
import com.android.server.am.OppoCrashClearManager;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/*  JADX ERROR: NullPointerException in pass: ReSugarCode
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
    	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ExtractFieldInit.checkStaticFieldsInit(ExtractFieldInit.java:58)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
class ReceiverRecordHelper {
    private static final String CLIENT_PKGNAME = "com.mediatek.security";
    public static final boolean DEFAULT_STATUS = true;
    private static final String FILE_EXTENSION = ".xml";
    private static final String FILE_NAME = "bootreceiver";
    public static boolean SUPPORT_SYSTEM_APP = false;
    private static final String TAG = "ReceiverRecordHelper";
    private Map<Integer, Map<String, ReceiverRecord>> mBootReceiverList;
    private BootReceiverPolicy mBootReceiverPolicy;
    private Context mContext;
    private AtomicFile mFile;
    private FileChangeListener mFileChangeListener;
    private Map<Integer, List<String>> mPendingSettings;
    private IPackageManager mPm;
    private boolean mReady;
    private IUserManager mUm;

    private class FileChangeListener extends FileObserver {
        public FileChangeListener(String path) {
            super(path);
        }

        public void onEvent(int event, String path) {
            if (path != null && path.equals("bootreceiver.xml")) {
                switch (event) {
                    case 2:
                    case 8:
                    case 256:
                    case 512:
                        Log.d(ReceiverRecordHelper.TAG, "FileChangeListener.onEvent(), event = " + event + ", reload the file.");
                        ReceiverRecordHelper.this.loadDataFromFileToCache();
                        return;
                    default:
                        return;
                }
            }
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.mediatek.server.am.AutoBootControl.ReceiverRecordHelper.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.mediatek.server.am.AutoBootControl.ReceiverRecordHelper.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.server.am.AutoBootControl.ReceiverRecordHelper.<clinit>():void");
    }

    public ReceiverRecordHelper(Context context, IUserManager um, IPackageManager pm) {
        this.mContext = null;
        this.mFile = null;
        this.mBootReceiverList = new HashMap();
        this.mPendingSettings = new HashMap();
        this.mUm = null;
        this.mPm = null;
        this.mReady = false;
        this.mBootReceiverPolicy = null;
        this.mFileChangeListener = null;
        File clientDataDir = new File(Environment.getDataDirectory(), "data/com.mediatek.security");
        this.mFile = new AtomicFile(new File(clientDataDir, "bootreceiver.xml"));
        this.mContext = context;
        this.mUm = um;
        this.mPm = pm;
        this.mBootReceiverPolicy = BootReceiverPolicy.getInstance(this.mContext);
        Log.d(TAG, "storeDir = " + clientDataDir.getPath());
        this.mFileChangeListener = new FileChangeListener(clientDataDir.getPath());
    }

    public void initReceiverList() {
        enforceBasicBootPolicy();
        List<UserInfo> userList = getUserList();
        synchronized (this.mBootReceiverList) {
            for (int i = 0; i < userList.size(); i++) {
                initReceiverCache(((UserInfo) userList.get(i)).id, false);
            }
        }
        loadDataFromFileToCache();
        this.mReady = true;
        if (this.mFileChangeListener != null) {
            this.mFileChangeListener.startWatching();
        }
    }

    public void initReceiverCache(int userId) {
        initReceiverCache(userId, false);
    }

    private void initReceiverCache(int userId, boolean readFile) {
        Log.d(TAG, "initReceiverCache() at User(" + userId + ")");
        List<String> list = getPackageListReceivingSpecifiedIntent(userId);
        Map<String, ReceiverRecord> receiverList = new HashMap();
        synchronized (this.mBootReceiverList) {
            this.mBootReceiverList.put(Integer.valueOf(userId), receiverList);
        }
        for (int i = 0; i < list.size(); i++) {
            String packageName = (String) list.get(i);
            receiverList.put(packageName, new ReceiverRecord(packageName, true));
            Log.d(TAG, "initReceiverCache() packageName: " + packageName);
        }
        if (readFile) {
            loadDataFromFileToCache();
        }
    }

    public List<String> getPackageListReceivingSpecifiedIntent(int userId) {
        List<String> bootReceivers = new ArrayList();
        List<String> policy = this.mBootReceiverPolicy.getBootPolicy();
        for (int i = 0; i < policy.size(); i++) {
            Intent intent = new Intent((String) policy.get(i));
            try {
                Log.d(TAG, "getPackageListReceivingSpecifiedIntent() find activities receiving intent = " + intent.getAction());
                ParceledListSlice<ResolveInfo> parceledList = this.mPm.queryIntentReceivers(intent, null, 268436480, userId);
                if (parceledList != null) {
                    List<ResolveInfo> receivers = parceledList.getList();
                    if (receivers != null) {
                        for (int j = 0; j < receivers.size(); j++) {
                            ResolveInfo info = (ResolveInfo) receivers.get(j);
                            String packageName = info.activityInfo != null ? info.activityInfo.packageName : null;
                            if (!((!SUPPORT_SYSTEM_APP && isSystemApp(userId, packageName)) || packageName == null || bootReceivers.contains(packageName))) {
                                Log.d(TAG, "getPackageListReceivingSpecifiedIntent() add " + packageName + " in the list");
                                bootReceivers.add(packageName);
                            }
                        }
                    }
                }
            } catch (RemoteException e) {
            }
        }
        return bootReceivers;
    }

    private void enforceBasicBootPolicy() {
        List<String> policy = this.mBootReceiverPolicy.getBootPolicy();
        boolean valid = true;
        if (!policy.contains("android.intent.action.BOOT_COMPLETED")) {
            valid = false;
        } else if (!policy.contains("android.intent.action.ACTION_BOOT_IPO")) {
            valid = false;
        }
        if (!valid) {
            throw new RuntimeException("Should NOT remove basic boot policy!");
        }
    }

    public boolean getReceiverDataEnabled(int userId, String packageName) {
        if (this.mReady) {
            Map<String, ReceiverRecord> receiverList = getBootReceiverListByUser(userId);
            if (receiverList != null && receiverList.containsKey(packageName)) {
                ReceiverRecord data = (ReceiverRecord) receiverList.get(packageName);
                if (data != null) {
                    return data.enabled;
                }
            }
        }
        Log.e(TAG, "getReceiverDataEnabled() not ready!");
        return true;
    }

    private boolean setReceiverRecord(int userId, String packageName, boolean enable) {
        Map<String, ReceiverRecord> receiverList = getBootReceiverListByUser(userId);
        if (receiverList == null || !receiverList.containsKey(packageName)) {
            return false;
        }
        ((ReceiverRecord) receiverList.get(packageName)).enabled = enable;
        return true;
    }

    private void addReceiverRecord(int userId, String packageName, boolean enabled) {
        Log.d(TAG, "addReceiverRecord() with " + packageName + " at User(" + userId + ") enabled: " + enabled);
        Map<String, ReceiverRecord> receiverList = getBootReceiverListByUser(userId);
        if (receiverList == null) {
            receiverList = new HashMap();
            synchronized (this.mBootReceiverList) {
                this.mBootReceiverList.put(Integer.valueOf(userId), receiverList);
            }
        }
        receiverList.put(packageName, new ReceiverRecord(packageName, enabled));
    }

    /* JADX WARNING: Missing block: B:13:0x0048, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean isPendingSetting(int userId, String packageName) {
        synchronized (this.mPendingSettings) {
            List<String> settings = (List) this.mPendingSettings.get(Integer.valueOf(userId));
            if (settings == null || !settings.contains(packageName)) {
            } else {
                Log.d(TAG, "Found a pending setting for pkg: " + packageName + " at User(" + userId + ")");
                return true;
            }
        }
    }

    private void removePendingSetting(int userId, String packageName) {
        synchronized (this.mPendingSettings) {
            List<String> settings = (List) this.mPendingSettings.get(Integer.valueOf(userId));
            if (settings != null && settings.contains(packageName)) {
                settings.remove(packageName);
            }
        }
    }

    public void loadDataFromFileToCache() {
        Throwable th;
        Log.d(TAG, "loadDataFromFileToCache()");
        synchronized (this.mFile) {
            try {
                FileInputStream stream = this.mFile.openRead();
                try {
                    int type;
                    XmlPullParser parser = Xml.newPullParser();
                    parser.setInput(stream, null);
                    do {
                        type = parser.next();
                        if (type == 2) {
                            break;
                        }
                    } while (type != 1);
                    if (type != 2) {
                        throw new IllegalStateException("no start tag found");
                    }
                    int outerDepth = parser.getDepth();
                    while (true) {
                        type = parser.next();
                        if (type != 1 && (type != 3 || parser.getDepth() > outerDepth)) {
                            if (!(type == 3 || type == 4)) {
                                if (parser.getName().equals("pkg")) {
                                    String pkgName = parser.getAttributeValue(null, OppoCrashClearManager.CRASH_COUNT);
                                    int userId = Integer.parseInt(parser.getAttributeValue(null, "u"));
                                    boolean enabled = Boolean.parseBoolean(parser.getAttributeValue(null, "e"));
                                    Log.d(TAG, "Read package name: " + pkgName + " enabled: " + enabled + " at User(" + userId + ")");
                                    if (setReceiverRecord(userId, pkgName, enabled)) {
                                        continue;
                                    } else {
                                        Log.w(TAG, "Found a pending settings for package: " + pkgName);
                                        synchronized (this.mPendingSettings) {
                                            try {
                                                List<String> list;
                                                if (!this.mPendingSettings.containsKey(Integer.valueOf(userId))) {
                                                    List<String> pendingSettings = new ArrayList();
                                                    try {
                                                        this.mPendingSettings.put(Integer.valueOf(userId), pendingSettings);
                                                        list = pendingSettings;
                                                    } catch (Throwable th2) {
                                                        th = th2;
                                                        list = pendingSettings;
                                                    }
                                                }
                                                list = (List) this.mPendingSettings.get(Integer.valueOf(userId));
                                                if (!list.contains(pkgName)) {
                                                    list.add(pkgName);
                                                }
                                            } catch (Throwable th3) {
                                                th = th3;
                                                throw th;
                                            }
                                        }
                                    }
                                }
                                Log.w(TAG, "Unknown element under <boot-receiver>: " + parser.getName());
                                XmlUtils.skipCurrentTag(parser);
                            }
                        }
                    }
                    if (!true) {
                        synchronized (this.mBootReceiverList) {
                            this.mBootReceiverList.clear();
                        }
                    }
                    stream.close();
                } catch (IOException e) {
                    throw new RuntimeException("Fail to read receiver list");
                } catch (IOException e2) {
                    throw new RuntimeException("Fail to read receiver list");
                } catch (IOException e3) {
                    throw new RuntimeException("Fail to read receiver list");
                } catch (IOException e4) {
                    throw new RuntimeException("Fail to read receiver list");
                } catch (IOException e5) {
                    throw new RuntimeException("Fail to read receiver list");
                } catch (IOException e6) {
                    throw new RuntimeException("Fail to read receiver list");
                } catch (IOException e7) {
                    throw new RuntimeException("Fail to read receiver list");
                } catch (IOException e8) {
                    throw new RuntimeException("Fail to read receiver list");
                } catch (IllegalStateException e9) {
                    Log.w(TAG, "Failed parsing " + e9);
                    if (null == null) {
                        synchronized (this.mBootReceiverList) {
                            this.mBootReceiverList.clear();
                        }
                    }
                    stream.close();
                } catch (NullPointerException e10) {
                    Log.w(TAG, "Failed parsing " + e10);
                    if (null == null) {
                        synchronized (this.mBootReceiverList) {
                            this.mBootReceiverList.clear();
                        }
                    }
                    stream.close();
                } catch (NumberFormatException e11) {
                    Log.w(TAG, "Failed parsing " + e11);
                    if (null == null) {
                        synchronized (this.mBootReceiverList) {
                            this.mBootReceiverList.clear();
                        }
                    }
                    stream.close();
                } catch (XmlPullParserException e12) {
                    Log.w(TAG, "Failed parsing " + e12);
                    if (null == null) {
                        synchronized (this.mBootReceiverList) {
                            this.mBootReceiverList.clear();
                        }
                    }
                    stream.close();
                } catch (IOException e13) {
                    Log.w(TAG, "Failed parsing " + e13);
                    if (null == null) {
                        synchronized (this.mBootReceiverList) {
                            this.mBootReceiverList.clear();
                        }
                    }
                    stream.close();
                } catch (IndexOutOfBoundsException e14) {
                    Log.w(TAG, "Failed parsing " + e14);
                    if (null == null) {
                        synchronized (this.mBootReceiverList) {
                            this.mBootReceiverList.clear();
                        }
                    }
                    stream.close();
                } catch (Throwable th4) {
                    if (null == null) {
                        synchronized (this.mBootReceiverList) {
                            this.mBootReceiverList.clear();
                        }
                    }
                    stream.close();
                }
            } catch (FileNotFoundException e15) {
                Log.i(TAG, "No existing " + this.mFile.getBaseFile() + "; starting empty");
                resetAllReceiverRecords();
            }
        }
    }

    private List<UserInfo> getUserList() {
        List<UserInfo> list = null;
        try {
            return this.mUm.getUsers(false);
        } catch (RemoteException e) {
            Log.e(TAG, "getUserList() failed!", e);
            return list;
        }
    }

    private PackageInfo getPackageInfoByUser(int userId, String packageName) {
        PackageInfo packageInfo = null;
        try {
            return this.mPm.getPackageInfo(packageName, 4096, userId);
        } catch (RemoteException e) {
            Log.e(TAG, "getPackageInfoByUser() failed! with userId: " + userId, e);
            return packageInfo;
        }
    }

    private boolean isSystemApp(int userId, String packageName) {
        boolean result = true;
        PackageInfo pkgInfo = getPackageInfoByUser(userId, packageName);
        if (pkgInfo == null || pkgInfo.applicationInfo == null) {
            Log.d(TAG, "isSystemApp() return false with null packageName");
            return false;
        }
        int appId = UserHandle.getAppId(pkgInfo.applicationInfo.uid);
        if ((pkgInfo.applicationInfo.flags & 1) == 0 && appId != 1000) {
            result = false;
        }
        return result;
    }

    private Map<String, ReceiverRecord> getBootReceiverListByUser(int userId) {
        synchronized (this.mBootReceiverList) {
            if (this.mBootReceiverList.containsKey(Integer.valueOf(userId))) {
                Map<String, ReceiverRecord> map = (Map) this.mBootReceiverList.get(Integer.valueOf(userId));
                return map;
            }
            return null;
        }
    }

    public void updateReceiverCache() {
        Log.d(TAG, "updateReceiverCache()");
        List<UserInfo> userList = getUserList();
        if (userList != null) {
            for (UserInfo user : userList) {
                updateReceiverCache(user.id);
            }
        }
    }

    void updateReceiverCache(int userId) {
        Log.d(TAG, "updateReceiverCache() at User(" + userId + ")");
        List<ReceiverRecord> oldList = getReceiverList(userId);
        List<String> updateList = getPackageListReceivingSpecifiedIntent(userId);
        for (int i = 0; i < updateList.size(); i++) {
            boolean enabled = true;
            boolean found = false;
            String packageName = (String) updateList.get(i);
            for (int j = 0; j < oldList.size(); j++) {
                ReceiverRecord record = (ReceiverRecord) oldList.get(j);
                if (record != null && packageName.equals(record.packageName)) {
                    enabled = record.enabled;
                    found = true;
                    break;
                }
            }
            if (found) {
                setReceiverRecord(userId, packageName, enabled);
            } else if (isPendingSetting(userId, packageName)) {
                addReceiverRecord(userId, packageName, false);
                removePendingSetting(userId, packageName);
            } else {
                addReceiverRecord(userId, packageName, true);
            }
        }
    }

    private List<ReceiverRecord> getReceiverList(int userId) {
        List<ReceiverRecord> res = new ArrayList();
        Map<String, ReceiverRecord> data = getBootReceiverListByUser(userId);
        if (data != null) {
            for (String pkgName : data.keySet()) {
                res.add(new ReceiverRecord((ReceiverRecord) data.get(pkgName)));
            }
        }
        return res;
    }

    void resetAllReceiverRecords() {
        Log.d(TAG, "resetAllReceiverRecords()");
        List<UserInfo> userList = getUserList();
        if (userList != null) {
            for (UserInfo user : userList) {
                List<ReceiverRecord> curList = getReceiverList(user.id);
                for (int index = 0; index < curList.size(); index++) {
                    ReceiverRecord record = (ReceiverRecord) curList.get(index);
                    if (!(record == null || record.enabled)) {
                        Log.d(TAG, "resetAllReceiverRecords() - found pkg = " + record.packageName + " to be reset");
                        record.enabled = true;
                    }
                }
            }
        }
    }
}
