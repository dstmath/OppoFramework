package com.android.server.am;

import android.app.AppGlobals;
import android.os.FileObserver;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.util.Log;
import android.util.Xml;
import com.android.server.display.ai.utils.BrightnessConstants;
import com.android.server.display.ai.utils.ColorAILog;
import com.android.server.pm.PackageManagerService;
import com.android.server.wm.startingwindow.ColorStartingWindowContants;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class ColorMultiAppManagerUtil {
    private static final Object ALIASMAPLOCK = new Object();
    private static final Object ALLOWEDAPPLOCK = new Object();
    private static final Object CREATEDMULTIAPPLOCK = new Object();
    /* access modifiers changed from: private */
    public static boolean DEBUG_MULTI_APP = SystemProperties.getBoolean(ColorAILog.OPPO_LOG_KEY, false);
    private static final String OPPO_ALLOWED_APP_FILE = "/data/oppo/coloros/multiapp/oppo_allowed_app.xml";
    private static final List<String> OPPO_INIT_ALLOWED_APP = Arrays.asList(ColorStartingWindowContants.WECHAT_PACKAGE_NAME, "com.tencent.mobileqq", "com.sina.weibo", "com.taobao.taobao", "jp.naver.line.android", "com.facebook.orca", "com.whatsapp", "com.bbm", "com.viber.voip", "com.imo.android.imoim", "com.instagram.android", "org.telegram.messenger", "com.skype.raider", "com.zing.zalo", "com.facebook.katana", "com.bsb.hike");
    private static final List<String> OPPO_MULTIAPP_FILTER_COMPONET = Arrays.asList("wxapi.WXEntryActivity", "QQCallbackUI", "plugin.accountsync.ui.ContactsSyncUI", "ui.activities.OpenInBbmActivity.Chat", "ui.activities.OpenInBbmActivity.VideoChat", "ui.activities.OpenInBbmActivity.VoiceChat", "com.taobao.login4android.activity.AlipaySSOResultActivity", "apshare.ShareEntryActivity", "com.taobao.share.view.WeiboShareActivity");
    private static final List<String> OPPO_MULTIAPP_GMS_PACKAGE = Arrays.asList("com.google.android.webview", "com.google.android.permissioncontroller", "com.android.permissioncontroller", "com.coloros.securitypermission");
    private static final List<String> OPPO_MULTIAPP_INSTALL_PACKAGE = Arrays.asList("com.oppo.launcher", "com.google.android.gms", "com.google.android.permissioncontroller", "com.android.permissioncontroller", "com.coloros.securitypermission", "android");
    private static final String OPPO_MULTI_APP_ALIAS_FILE = "/data/oppo/coloros/multiapp/oppo_multi_app_alias.xml";
    private static final String OPPO_MULTI_APP_CREATED_FILE = "/data/oppo/coloros/multiapp/oppo_multi_app.xml";
    private static final List<String> REALME_INIT_ALLOWED_APP = Arrays.asList("net.one97.paytm", "com.google.android.apps.walletnfcrel", "com.phonepe.app", "in.amazon.mShop.android.shopping", "com.flipkart.android", "com.twitter.android", "com.freecharge.android", "com.application.zomato", "in.swiggy.android", "com.snapchat.android");
    /* access modifiers changed from: private */
    public static final String TAG = ColorMultiAppManagerUtil.class.getName();
    private static ColorMultiAppManagerUtil sInstance = new ColorMultiAppManagerUtil();
    private FileObserverUtil mAliasFileObserver;
    private HashMap<String, String> mAliasMap = new HashMap<>();
    private List<String> mAllowedAppList = new ArrayList();
    private FileObserverUtil mAllowedFileObserver;
    private List<String> mCreatedMultiAppList = new ArrayList();
    private List<String> mInitialAllowedAppList = new ArrayList();
    private HashMap<String, Boolean> mMultiAppInstallState = new HashMap<>();
    private FileObserverUtil mObserver;

    protected static ColorMultiAppManagerUtil getInstance() {
        return sInstance;
    }

    public void init(PackageManagerService pms) {
        if (isRealmeSurpport(pms)) {
            this.mInitialAllowedAppList.addAll(REALME_INIT_ALLOWED_APP);
        }
        this.mInitialAllowedAppList.addAll(OPPO_INIT_ALLOWED_APP);
        initFile(OPPO_ALLOWED_APP_FILE);
        initFile(OPPO_MULTI_APP_CREATED_FILE);
        initFile(OPPO_MULTI_APP_ALIAS_FILE);
        readAllowedListFromFile(OPPO_ALLOWED_APP_FILE);
        readCreatedListFromFile(OPPO_MULTI_APP_CREATED_FILE);
        readAliasFromFile(OPPO_MULTI_APP_ALIAS_FILE);
        initObserver(OPPO_ALLOWED_APP_FILE);
        initObserver(OPPO_MULTI_APP_CREATED_FILE);
        initObserver(OPPO_MULTI_APP_ALIAS_FILE);
    }

    public boolean isInFilter(String tempClass) {
        if (tempClass == null) {
            return false;
        }
        for (String name : OPPO_MULTIAPP_FILTER_COMPONET) {
            if (tempClass.endsWith(name)) {
                return true;
            }
        }
        return false;
    }

    public boolean isMultiUserInstallApp(String pkgName) {
        if (pkgName == null) {
            return false;
        }
        for (String name : OPPO_MULTIAPP_INSTALL_PACKAGE) {
            if (name.equals(pkgName)) {
                return true;
            }
        }
        return false;
    }

    public boolean isGms(String pkgName) {
        if (pkgName == null) {
            return false;
        }
        for (String name : OPPO_MULTIAPP_GMS_PACKAGE) {
            if (name.equals(pkgName)) {
                return true;
            }
        }
        return false;
    }

    public boolean isMultiAllowedApp(String pkgName) {
        if (pkgName == null) {
            return false;
        }
        if ((!this.mAllowedAppList.isEmpty() || !this.mInitialAllowedAppList.contains(pkgName)) && !this.mAllowedAppList.contains(pkgName)) {
            return false;
        }
        return true;
    }

    public List<String> getAllowedMultiApp() {
        List<String> list;
        synchronized (ALLOWEDAPPLOCK) {
            list = this.mAllowedAppList;
        }
        return list;
    }

    public List<String> getCreatedMultiApp() {
        List<String> list;
        synchronized (CREATEDMULTIAPPLOCK) {
            list = this.mCreatedMultiAppList;
        }
        return list;
    }

    public String getAliasByPackage(String pkgName) {
        String alias;
        synchronized (ALIASMAPLOCK) {
            alias = this.mAliasMap.get(pkgName);
        }
        return alias;
    }

    public boolean isMultiApp(String pkgName) {
        boolean contains;
        if (pkgName == null) {
            return false;
        }
        synchronized (CREATEDMULTIAPPLOCK) {
            contains = this.mCreatedMultiAppList.contains(pkgName);
        }
        return contains;
    }

    public boolean isMainApp(int userId, String pkgName) {
        boolean contains;
        if (userId == 999 || pkgName == null) {
            return false;
        }
        synchronized (CREATEDMULTIAPPLOCK) {
            contains = this.mCreatedMultiAppList.contains(pkgName);
        }
        return contains;
    }

    public boolean isMultiApp(int userId, String pkgName) {
        boolean contains;
        if (userId != 999 || pkgName == null) {
            return false;
        }
        synchronized (CREATEDMULTIAPPLOCK) {
            contains = this.mCreatedMultiAppList.contains(pkgName);
        }
        return contains;
    }

    public void addToCreatedMultiApp(String pkgName) {
        if (pkgName != null) {
            synchronized (CREATEDMULTIAPPLOCK) {
                if (!this.mCreatedMultiAppList.contains(pkgName)) {
                    this.mCreatedMultiAppList.add(pkgName);
                    writeListToFile(this.mCreatedMultiAppList, OPPO_MULTI_APP_CREATED_FILE);
                }
            }
        }
    }

    public void removeFromCreatedMultiApp(String pkgName) {
        if (pkgName != null) {
            synchronized (CREATEDMULTIAPPLOCK) {
                if (this.mCreatedMultiAppList.contains(pkgName)) {
                    this.mCreatedMultiAppList.remove(pkgName);
                    this.mAliasMap.remove(pkgName);
                    writeListToFile(this.mCreatedMultiAppList, OPPO_MULTI_APP_CREATED_FILE);
                }
            }
        }
    }

    private void writeListToFile(List<String> list, String filePath) {
        if (list != null && filePath != null) {
            FileOutputStream stream = null;
            try {
                FileOutputStream stream2 = new FileOutputStream(new File(filePath));
                XmlSerializer out = Xml.newSerializer();
                out.setOutput(stream2, "UTF-8");
                out.startDocument(null, true);
                out.startTag(null, "gs");
                int size = list.size();
                if (filePath.equals(OPPO_MULTI_APP_CREATED_FILE)) {
                    for (int i = 0; i < size; i++) {
                        String pkg = list.get(i);
                        if (pkg != null) {
                            out.startTag(null, ColorAppCrashClearManager.CRASH_CLEAR_NAME);
                            out.attribute(null, "att", pkg);
                            out.endTag(null, ColorAppCrashClearManager.CRASH_CLEAR_NAME);
                        }
                    }
                } else if (filePath.equals(OPPO_ALLOWED_APP_FILE)) {
                    for (int i2 = 0; i2 < size; i2++) {
                        String pkg2 = list.get(i2);
                        if (pkg2 != null) {
                            out.startTag(null, "allowedapp");
                            out.text(pkg2);
                            out.endTag(null, "allowedapp");
                        }
                    }
                }
                out.endTag(null, "gs");
                out.endDocument();
                try {
                    stream2.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (IOException ie) {
                ie.printStackTrace();
                if (stream != null) {
                    stream.close();
                }
            } catch (Throwable th) {
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException e2) {
                        e2.printStackTrace();
                    }
                }
                throw th;
            }
        }
    }

    private ColorMultiAppManagerUtil() {
    }

    private void initFile(String fileName) {
        if (DEBUG_MULTI_APP) {
            String str = TAG;
            Log.d(str, "initFile() " + fileName);
        }
        File file = new File(fileName);
        try {
            if (file.getParentFile().exists() || file.getParentFile().mkdir()) {
                if (!file.exists()) {
                    if (!file.createNewFile()) {
                        if (DEBUG_MULTI_APP) {
                            String str2 = TAG;
                            Log.e(str2, "init file failed: file-" + fileName);
                            return;
                        }
                        return;
                    } else if (fileName.equals(OPPO_ALLOWED_APP_FILE)) {
                        writeListToFile(this.mInitialAllowedAppList, OPPO_ALLOWED_APP_FILE);
                    }
                }
                changeModFile(fileName);
            } else if (DEBUG_MULTI_APP) {
                String str3 = TAG;
                Log.d(str3, "initFile() " + fileName + " mkdir() failed");
            }
        } catch (IOException e) {
        }
    }

    private void changeModFile(String fileName) {
        try {
            Runtime.getRuntime().exec("chmod 700 /data/oppo/coloros/multiapp");
            Runtime runtime = Runtime.getRuntime();
            runtime.exec("chmod 600 " + fileName);
        } catch (IOException e) {
            String str = TAG;
            Log.w(str, " " + e);
        }
    }

    private void readAllowedListFromFile(String filePath) {
        int type;
        String pkgName;
        if (DEBUG_MULTI_APP) {
            String str = TAG;
            Log.d(str, " read list from file: " + filePath);
        }
        if (filePath != null) {
            List<String> tempList = new ArrayList<>();
            FileInputStream stream = null;
            try {
                FileInputStream stream2 = new FileInputStream(new File(filePath));
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(stream2, "UTF-8");
                do {
                    type = parser.next();
                    if (type == 2 && parser.getName().equals("allowedapp") && (pkgName = parser.nextText()) != null) {
                        tempList.add(pkgName);
                    }
                } while (type != 1);
                try {
                    stream2.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (IOException | XmlPullParserException e2) {
                e2.printStackTrace();
                if (stream != null) {
                    stream.close();
                }
            } catch (Throwable th) {
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException e3) {
                        e3.printStackTrace();
                    }
                }
                throw th;
            }
            if (filePath.equals(OPPO_ALLOWED_APP_FILE)) {
                if (!this.mAllowedAppList.isEmpty()) {
                    this.mAllowedAppList.clear();
                }
                if (!tempList.isEmpty()) {
                    this.mAllowedAppList.addAll(tempList);
                }
            }
        }
    }

    private void readCreatedListFromFile(String filePath) {
        int type;
        String packageName;
        if (DEBUG_MULTI_APP) {
            String str = TAG;
            Log.d(str, " read list from file: " + filePath);
        }
        if (filePath != null) {
            List<String> tempList = new ArrayList<>();
            FileInputStream stream = null;
            try {
                FileInputStream stream2 = new FileInputStream(new File(filePath));
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(stream2, "UTF-8");
                do {
                    type = parser.next();
                    if (type == 2 && parser.getName().equals(ColorAppCrashClearManager.CRASH_CLEAR_NAME) && (packageName = parser.getAttributeValue(null, "att")) != null) {
                        tempList.add(packageName);
                    }
                } while (type != 1);
                try {
                    stream2.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (IOException | XmlPullParserException e2) {
                e2.printStackTrace();
                if (stream != null) {
                    stream.close();
                }
            } catch (Throwable th) {
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException e3) {
                        e3.printStackTrace();
                    }
                }
                throw th;
            }
            if (filePath.equals(OPPO_MULTI_APP_CREATED_FILE)) {
                if (!this.mCreatedMultiAppList.isEmpty()) {
                    this.mCreatedMultiAppList.clear();
                }
                this.mCreatedMultiAppList.addAll(tempList);
            }
        }
    }

    private void readAliasFromFile(String filePath) {
        int type;
        if (DEBUG_MULTI_APP) {
            String str = TAG;
            Log.d(str, " read list from file: " + filePath);
        }
        if (filePath != null) {
            this.mAliasMap.clear();
            FileInputStream stream = null;
            try {
                FileInputStream stream2 = new FileInputStream(new File(filePath));
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(stream2, "UTF-8");
                do {
                    type = parser.next();
                    if (type == 2 && parser.getName().equals(ColorAppCrashClearManager.CRASH_CLEAR_NAME)) {
                        String pkgName = parser.getAttributeValue(null, "pkg");
                        String name = parser.getAttributeValue(null, BrightnessConstants.AppSplineXml.TAG_NAME);
                        if (!(pkgName == null || !getAllowedMultiApp().contains(pkgName) || name == null)) {
                            this.mAliasMap.put(pkgName, name);
                        }
                    }
                } while (type != 1);
                try {
                    stream2.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (IOException | XmlPullParserException e2) {
                e2.printStackTrace();
                if (stream != null) {
                    stream.close();
                }
            } catch (Throwable th) {
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException e3) {
                        e3.printStackTrace();
                    }
                }
                throw th;
            }
        }
    }

    private void initObserver(String fileName) {
        if (fileName != null && fileName.equals(OPPO_MULTI_APP_CREATED_FILE) && this.mObserver == null) {
            this.mObserver = new FileObserverUtil(fileName);
            this.mObserver.startWatching();
        }
        if (fileName != null && fileName.equals(OPPO_ALLOWED_APP_FILE) && this.mAllowedFileObserver == null) {
            this.mAllowedFileObserver = new FileObserverUtil(fileName);
            this.mAllowedFileObserver.startWatching();
        }
        if (fileName != null && fileName.equals(OPPO_MULTI_APP_ALIAS_FILE) && this.mAliasFileObserver == null) {
            this.mAliasFileObserver = new FileObserverUtil(fileName);
            this.mAliasFileObserver.startWatching();
        }
    }

    private class FileObserverUtil extends FileObserver {
        private String mFileName;

        public FileObserverUtil(String path) {
            super(path, 8);
            this.mFileName = path;
        }

        public void onEvent(int event, String path) {
            int e = event & 4095;
            if (e == 8) {
                if (ColorMultiAppManagerUtil.DEBUG_MULTI_APP) {
                    String access$100 = ColorMultiAppManagerUtil.TAG;
                    Log.v(access$100, "file is changing: " + this.mFileName + " event= " + e);
                }
                ColorMultiAppManagerUtil.this.reactOnFileChanged(this.mFileName);
            }
        }
    }

    /* access modifiers changed from: private */
    public void reactOnFileChanged(String fileName) {
        if (fileName != null && fileName.equals(OPPO_ALLOWED_APP_FILE)) {
            synchronized (ALLOWEDAPPLOCK) {
                readAllowedListFromFile(fileName);
            }
        } else if (fileName != null && fileName.equals(OPPO_MULTI_APP_CREATED_FILE)) {
            synchronized (CREATEDMULTIAPPLOCK) {
                readCreatedListFromFile(fileName);
            }
        } else if (fileName != null && fileName.equals(OPPO_MULTI_APP_ALIAS_FILE)) {
            synchronized (ALIASMAPLOCK) {
                readAliasFromFile(fileName);
            }
        }
    }

    public void updateMultiUserInstallAppState(String name, boolean mainInstalled, boolean multiInstalled) {
        String str = TAG;
        Log.i(str, "updateMultiUserInstallAppState: name: " + name + " mainInstalled: " + mainInstalled + " multiInstalled: " + multiInstalled);
        if (!isMultiUserInstallApp(name)) {
            return;
        }
        if (!mainInstalled || multiInstalled) {
            this.mMultiAppInstallState.put(name, false);
        } else {
            this.mMultiAppInstallState.put(name, true);
        }
    }

    public void checkMultiUserInstallApp() {
        try {
            for (String name : OPPO_MULTIAPP_INSTALL_PACKAGE) {
                if (this.mMultiAppInstallState.containsKey(name) && this.mMultiAppInstallState.get(name).booleanValue()) {
                    AppGlobals.getPackageManager().installExistingPackageAsUser(name, (int) ColorMultiAppManagerService.USER_ID, 4194304, 0, (List) null);
                }
            }
        } catch (RemoteException e) {
            String str = TAG;
            Log.e(str, "checkMultiUserInstallApp exception " + e);
        }
    }

    private boolean isRealmeSurpport(PackageManagerService pms) {
        boolean values = "realme".equalsIgnoreCase(SystemProperties.get("ro.electronic.label"));
        if (!values) {
            values = "realme".equalsIgnoreCase(SystemProperties.get("ro.product.brand.sub"));
        }
        return values && pms.hasSystemFeature("oppo.version.exp", 0);
    }
}
