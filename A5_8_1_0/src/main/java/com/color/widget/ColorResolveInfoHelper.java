package com.color.widget;

import android.app.OppoActivityManager;
import android.app.OppoThemeHelper;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.ComponentInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.os.FileObserver;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.storage.StorageManager;
import android.provider.Settings.Secure;
import android.text.TextUtils;
import android.util.Log;
import com.color.util.ColorResolveData;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import oppo.content.res.OppoThemeResources;
import oppo.util.OppoMultiLauncherUtil;
import oppo.util.OppoStatistics;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

public class ColorResolveInfoHelper {
    private static final String APP_EVENT_ID = "resolver_app";
    private static final String CODE = "20120";
    private static final String DEFAULT_APP_AUDIO = "audio";
    private static final String DEFAULT_APP_BROWSER = "browser";
    private static final String DEFAULT_APP_CAMERA = "camera";
    private static final String DEFAULT_APP_CONTACTS = "contacts";
    private static final String DEFAULT_APP_DIALER = "dialer";
    private static final String DEFAULT_APP_EMAIL = "email";
    private static final String DEFAULT_APP_EXCEL = "excel";
    private static final String DEFAULT_APP_GALLERY = "gallery";
    private static final String DEFAULT_APP_LAUNCHER = "launcher";
    private static final String DEFAULT_APP_MARKET = "market";
    private static final String DEFAULT_APP_MESSAGE = "message";
    private static final String DEFAULT_APP_PDF = "pdf";
    private static final String DEFAULT_APP_PPT = "ppt";
    private static final String DEFAULT_APP_TEXT = "txt";
    private static final String DEFAULT_APP_VIDEO = "video";
    private static final String DEFAULT_APP_WORD = "word";
    private static final String GALLERY_PIN_LIST = "gallery_pin_list";
    private static final String KEY_ACTION = "action";
    private static final String KEY_CATEGORIES = "categories";
    private static final String KEY_CHOOSE = "isChooser";
    private static final String KEY_COMPONENT_LABEL = "componentLabel";
    private static final String KEY_DATA = "data";
    private static final String KEY_INTENT = "intent";
    private static final String KEY_MIME_TYPE = "mimeType";
    private static final String KEY_NAME = "name";
    private static final String KEY_POSITION = "position";
    private static final String KEY_REFERRER_PACKAGE = "referrerPackage";
    private static final String KEY_SCHEME = "scheme";
    private static final String KEY_TARGET_PACKAGE = "targetPackage";
    private static final String KEY_TYPE = "type";
    private static final String PINNED_SHARED_PREFS_NAME = "chooser_pin_settings";
    private static final String RESOLVER_APP_FILE = "/data/oppo/coloros/safecenter/safe_default_app_list.xml";
    private static final String RESOLVER_APP_PATH = "/data/oppo/coloros/safecenter";
    private static final String SHOW_EVENT_ID = "resolver_show";
    private static final String TAG = "ColorResolveInfoHelper";
    private static final String TAG_BLACK_CHOOSE_ACTIVITY = "black_choose_activity";
    private static final String TAG_BLACK_CHOOSE_PACKAGE = "black_choose_package";
    private static final String TAG_BLACK_RESOLVE = "black_resolve";
    private static final String TAG_CHOOSE = "choose";
    private static final String TAG_DEFAULT = "default";
    private static final String TAG_RESOLVE = "resolve";
    private static final String TAG_TYPE = "type";
    private static int mColumnCounts = 4;
    private static int mRowCounts = 2;
    private static ColorResolveInfoHelper sResolveInfoHelper;
    private boolean DEBUG = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    private HashMap<String, List<String>> mCloudBlackChooseActivityMap = new HashMap();
    private HashMap<String, List<String>> mCloudBlackChooseMap = new HashMap();
    private HashMap<String, List<String>> mCloudBlackResolveMap = new HashMap();
    private HashMap<String, List<String>> mCloudChooserMap = new HashMap();
    private HashMap<String, List<String>> mCloudResolveMap = new HashMap();
    private Context mContext;
    private ColorResolveData mData;
    private FileObserverPolicy mFileObserver = null;
    private final Object mLock = new Object();
    private HashMap<String, List<String>> mMap1 = new HashMap();
    private HashMap<String, List<String>> mMap2 = new HashMap();
    private HashMap<String, List<String>> mMap3 = new HashMap();
    private HashMap<String, List<String>> mMap4 = new HashMap();
    private HashMap<String, List<String>> mMap5 = new HashMap();
    private SharedPreferences mPinnedSharedPrefs;
    private List<ResolveInfo> mPriorityList = new ArrayList();

    private class FileObserverPolicy extends FileObserver {
        private String focusPath;

        public FileObserverPolicy(String path) {
            super(path, 8);
            this.focusPath = path;
        }

        public void onEvent(int event, String path) {
            if (event == 8 && this.focusPath.equals(ColorResolveInfoHelper.RESOLVER_APP_FILE)) {
                synchronized (ColorResolveInfoHelper.this.mLock) {
                    Log.d(ColorResolveInfoHelper.TAG, "readConfigFile onEvent ");
                    ColorResolveInfoHelper.this.readConfigFile();
                }
            }
        }
    }

    public static ColorResolveInfoHelper getInstance(Context context) {
        if (sResolveInfoHelper == null) {
            sResolveInfoHelper = new ColorResolveInfoHelper(context);
        }
        return sResolveInfoHelper;
    }

    public void init() {
        initDir();
        initFileObserver();
        if (this.mData == null) {
            this.mData = new ColorResolveData();
        }
        this.mMap1 = this.mData.getBlackResolveMap();
        this.mMap2 = this.mData.getResolveMap();
        this.mMap3 = this.mData.getChooseMap();
        this.mMap4 = this.mData.getBlackChoosePackageMap();
        this.mMap5 = this.mData.getBlackChooseActivityMap();
        synchronized (this.mLock) {
            Log.d(TAG, "readConfigFile init ");
            readConfigFile();
        }
    }

    public ColorResolveData getData() {
        if (this.mData == null) {
            this.mData = new ColorResolveData();
        }
        return this.mData;
    }

    public List<String> getCloudBlackList(Intent intent) {
        String type = getIntentType(intent);
        List<String> list;
        if (isChooserAction(intent)) {
            list = (List) this.mMap4.get(type);
            Log.d(TAG, "getCloudBlackList = " + this.mMap4);
            return list;
        }
        list = (List) this.mMap1.get(type);
        Log.d(TAG, "getCloudBlackList = " + this.mMap1);
        return list;
    }

    private void initDir() {
        File dir = new File(RESOLVER_APP_PATH);
        File file = new File(RESOLVER_APP_FILE);
        try {
            if (!dir.exists()) {
                dir.mkdirs();
            }
            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        changeModFile(RESOLVER_APP_FILE);
    }

    private void changeModFile(String fileName) {
        try {
            Runtime.getRuntime().exec("chmod 766 " + fileName);
        } catch (IOException e) {
            Log.e(TAG, "changeModFile : " + e);
        }
    }

    private void readConfigFile() {
        IOException e;
        XmlPullParserException e2;
        UnsupportedEncodingException e3;
        Exception e4;
        Throwable th;
        XmlPullParser parser = null;
        BufferedReader bufferedReader = null;
        this.mMap1.clear();
        this.mMap2.clear();
        this.mMap3.clear();
        String curAppType = null;
        try {
            File file = new File(RESOLVER_APP_FILE);
            if (!file.exists() || file.length() <= 0) {
                try {
                    if (null instanceof Closeable) {
                        ((Closeable) null).close();
                    }
                } catch (IOException e5) {
                    e5.printStackTrace();
                }
                return;
            }
            parser = XmlPullParserFactory.newInstance().newPullParser();
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
            try {
                parser.setInput(br);
                Log.d(TAG, "read cloud file");
                int event;
                do {
                    event = parser.next();
                    String tag = parser.getName();
                    if (event == 2) {
                        List<String> blackList;
                        if (TAG_DEFAULT.equals(tag)) {
                            int attrCount = parser.getAttributeCount();
                            if (attrCount > 0) {
                                for (int i = 0; i < attrCount; i++) {
                                    String attrName = parser.getAttributeName(i);
                                    String attrValue = parser.getAttributeValue(i);
                                    if ("type".equals(attrName)) {
                                        curAppType = attrValue;
                                        break;
                                    }
                                }
                            }
                        } else if (TAG_BLACK_RESOLVE.equals(tag)) {
                            if (curAppType != null) {
                                parser.next();
                                String blackResolve = parser.getText().trim();
                                if (!TextUtils.isEmpty(blackResolve)) {
                                    blackList = (List) this.mMap1.get(curAppType);
                                    if (blackList == null) {
                                        blackList = new ArrayList();
                                    }
                                    if (!blackList.contains(blackResolve)) {
                                        blackList.add(blackResolve);
                                        Log.d(TAG, "curAppType=" + curAppType + ", blackResolve=" + blackResolve);
                                    }
                                    this.mMap1.put(curAppType, blackList);
                                }
                            }
                        } else if (TAG_BLACK_CHOOSE_PACKAGE.equals(tag)) {
                            if (curAppType != null) {
                                parser.next();
                                String blackChoose = parser.getText().trim();
                                if (!TextUtils.isEmpty(blackChoose)) {
                                    blackList = (List) this.mMap4.get(curAppType);
                                    if (blackList == null) {
                                        blackList = new ArrayList();
                                    }
                                    if (!blackList.contains(blackChoose)) {
                                        blackList.add(blackChoose);
                                        Log.d(TAG, "curAppType=" + curAppType + ", blackChoose=" + blackChoose);
                                    }
                                    this.mMap4.put(curAppType, blackList);
                                }
                            }
                        } else if (TAG_BLACK_CHOOSE_ACTIVITY.equals(tag)) {
                            if (curAppType != null) {
                                parser.next();
                                String blackChooseActivity = parser.getText().trim();
                                if (!TextUtils.isEmpty(blackChooseActivity)) {
                                    blackList = (List) this.mMap5.get(curAppType);
                                    if (blackList == null) {
                                        blackList = new ArrayList();
                                    }
                                    if (!blackList.contains(blackChooseActivity)) {
                                        blackList.add(blackChooseActivity);
                                        Log.d(TAG, "curAppType=" + curAppType + ", blackChooseActivity=" + blackChooseActivity);
                                    }
                                    this.mMap5.put(curAppType, blackList);
                                }
                            }
                        } else if (TAG_RESOLVE.equals(tag)) {
                            if (curAppType != null) {
                                parser.next();
                                String resolveApp = parser.getText().trim();
                                if (!TextUtils.isEmpty(resolveApp)) {
                                    List<String> resolveList = (List) this.mMap2.get(curAppType);
                                    if (resolveList == null) {
                                        resolveList = new ArrayList();
                                    }
                                    if (!resolveList.contains(resolveApp)) {
                                        resolveList.add(resolveApp);
                                        Log.d(TAG, "curAppType=" + curAppType + ", resolveApp=" + resolveApp);
                                    }
                                    this.mMap2.put(curAppType, resolveList);
                                }
                            }
                        } else if (TAG_CHOOSE.equals(tag) && curAppType != null) {
                            parser.next();
                            String chooserApp = parser.getText().trim();
                            if (!TextUtils.isEmpty(chooserApp)) {
                                List<String> chooserList = (List) this.mMap3.get(curAppType);
                                if (chooserList == null) {
                                    chooserList = new ArrayList();
                                }
                                if (!chooserList.contains(chooserApp)) {
                                    chooserList.add(chooserApp);
                                    Log.d(TAG, "curAppType=" + curAppType + ", chooseApp=" + chooserApp);
                                }
                                this.mMap3.put(curAppType, chooserList);
                            }
                        }
                    } else if (event == 3) {
                        if (TAG_DEFAULT.equals(tag)) {
                            curAppType = null;
                        }
                    }
                } while (event != 1);
                try {
                    if (parser instanceof Closeable) {
                        ((Closeable) parser).close();
                    } else if (br != null) {
                        br.close();
                    }
                } catch (IOException e52) {
                    e52.printStackTrace();
                }
                bufferedReader = br;
            } catch (XmlPullParserException e6) {
                e2 = e6;
                bufferedReader = br;
            } catch (UnsupportedEncodingException e7) {
                e3 = e7;
                bufferedReader = br;
            } catch (IOException e8) {
                e52 = e8;
                bufferedReader = br;
            } catch (Exception e9) {
                e4 = e9;
                bufferedReader = br;
            } catch (Throwable th2) {
                th = th2;
                bufferedReader = br;
            }
        } catch (XmlPullParserException e10) {
            e2 = e10;
            try {
                e2.printStackTrace();
                try {
                    if (parser instanceof Closeable) {
                        ((Closeable) parser).close();
                    } else if (bufferedReader != null) {
                        bufferedReader.close();
                    }
                } catch (IOException e522) {
                    e522.printStackTrace();
                }
            } catch (Throwable th3) {
                th = th3;
                try {
                    if (parser instanceof Closeable) {
                        ((Closeable) parser).close();
                    } else if (bufferedReader != null) {
                        bufferedReader.close();
                    }
                } catch (IOException e5222) {
                    e5222.printStackTrace();
                }
                throw th;
            }
        } catch (UnsupportedEncodingException e11) {
            e3 = e11;
            e3.printStackTrace();
            try {
                if (parser instanceof Closeable) {
                    ((Closeable) parser).close();
                } else if (bufferedReader != null) {
                    bufferedReader.close();
                }
            } catch (IOException e52222) {
                e52222.printStackTrace();
            }
        } catch (IOException e12) {
            e52222 = e12;
            e52222.printStackTrace();
            try {
                if (parser instanceof Closeable) {
                    ((Closeable) parser).close();
                } else if (bufferedReader != null) {
                    bufferedReader.close();
                }
            } catch (IOException e522222) {
                e522222.printStackTrace();
            }
        } catch (Exception e13) {
            e4 = e13;
            e4.printStackTrace();
            try {
                if (parser instanceof Closeable) {
                    ((Closeable) parser).close();
                } else if (bufferedReader != null) {
                    bufferedReader.close();
                }
            } catch (IOException e5222222) {
                e5222222.printStackTrace();
            }
        }
    }

    private void initFileObserver() {
        this.mFileObserver = new FileObserverPolicy(RESOLVER_APP_FILE);
        this.mFileObserver.startWatching();
    }

    public void initData() {
        try {
            ColorResolveData data = new OppoActivityManager().getResolveData();
            this.mCloudBlackResolveMap = data.getBlackResolveMap();
            this.mCloudBlackChooseMap = data.getBlackChoosePackageMap();
            this.mCloudBlackChooseActivityMap = data.getBlackChooseActivityMap();
            this.mCloudResolveMap = data.getResolveMap();
            this.mCloudChooserMap = data.getChooseMap();
            Log.d(TAG, "mCloudBlackResolveMap = " + this.mCloudBlackResolveMap);
            Log.d(TAG, "mCloudBlackChooseMap = " + this.mCloudBlackChooseMap);
            Log.d(TAG, "mCloudBlackChooseActivityMap = " + this.mCloudBlackChooseActivityMap);
            Log.d(TAG, "mCloudResolveMap = " + this.mCloudResolveMap);
            Log.d(TAG, "mCloudChooserMap = " + this.mCloudChooserMap);
        } catch (RemoteException e) {
            Log.e(TAG, "init data RemoteException , " + e);
            e.printStackTrace();
        } catch (Exception e2) {
            Log.e(TAG, "init data Exception , " + e2);
            e2.printStackTrace();
        }
    }

    public int getResolveTopSize() {
        if (this.mPriorityList != null) {
            return this.mPriorityList.size();
        }
        return 0;
    }

    private ColorResolveInfoHelper(Context context) {
        this.mContext = context;
    }

    private SharedPreferences getPinnedSharedPrefs(Context context) {
        return context.getSharedPreferences(new File(new File(Environment.getDataUserCePackageDirectory(StorageManager.UUID_PRIVATE_INTERNAL, context.getUserId(), context.getPackageName()), "shared_prefs"), "chooser_pin_settings.xml"), 0);
    }

    public void resort(List<ResolveInfo> list, Intent intent) {
        List<String> priority;
        initData();
        String type = getIntentType(intent);
        list = filterBlackApps(intent, type, list);
        boolean isChoose = isChooserAction(intent);
        if (isChoose) {
            priority = getChooserListWithType(type);
        } else {
            priority = getResolveListWithType(type);
        }
        startToResort(list, type, priority, isChoose);
    }

    private List<ResolveInfo> filterBlackApps(Intent intent, String type, List<ResolveInfo> result) {
        if (result == null || result.isEmpty()) {
            return result;
        }
        Iterator<ResolveInfo> it;
        ActivityInfo ai;
        if (isChooserAction(intent)) {
            List<String> blackChooseList = getBlackChooseListWithType(type);
            if (!(blackChooseList == null || (blackChooseList.isEmpty() ^ 1) == 0)) {
                it = result.iterator();
                while (it.hasNext()) {
                    ai = ((ResolveInfo) it.next()).activityInfo;
                    if (ai != null && isInDataSet(blackChooseList, ai.packageName)) {
                        Log.d(TAG, "remove black choose package : " + ai.packageName);
                        it.remove();
                    }
                }
            }
            List<String> blackChooseListActivity = getBlackChooseActivityListWithType(type);
            if (!(blackChooseListActivity == null || (blackChooseListActivity.isEmpty() ^ 1) == 0)) {
                it = result.iterator();
                while (it.hasNext()) {
                    String componentName = ((ResolveInfo) it.next()).getComponentInfo().getComponentName().flattenToShortString();
                    if (isInDataSet(blackChooseListActivity, componentName)) {
                        Log.d(TAG, "remove black choose componentName : " + componentName);
                        it.remove();
                    }
                }
            }
        } else {
            List<String> blackResolveList = getBlackResolveListWithType(type);
            if (!(blackResolveList == null || (blackResolveList.isEmpty() ^ 1) == 0)) {
                it = result.iterator();
                while (it.hasNext()) {
                    ai = ((ResolveInfo) it.next()).activityInfo;
                    if (ai != null && isInDataSet(blackResolveList, ai.packageName)) {
                        Log.d(TAG, "remove black resolve package : " + ai.packageName);
                        it.remove();
                    }
                }
            }
        }
        return result;
    }

    private void startToResort(List<ResolveInfo> list, String type, List<String> priority, boolean isChoose) {
        Log.d(TAG, "start to resort : " + list);
        if (!isChoose) {
            Map map = new HashMap();
            map.put("type", type);
            OppoStatistics.onCommon(this.mContext, CODE, SHOW_EVENT_ID, map, false);
            Log.d(TAG, "statistics data [resolver_show]: " + map);
        }
        if (this.mPinnedSharedPrefs == null) {
            this.mPinnedSharedPrefs = getPinnedSharedPrefs(this.mContext);
        }
        List<ResolveInfo> pinnedList = new ArrayList();
        List<ResolveInfo> otherList = new ArrayList();
        this.mPriorityList.clear();
        Set<String> pinPrefList = new HashSet();
        if (isChoose && type != null) {
            if (DEFAULT_APP_GALLERY.equals(type)) {
                String galleryPinList = Secure.getString(this.mContext.getContentResolver(), GALLERY_PIN_LIST);
                Log.d(TAG, "galleryPinList = " + galleryPinList);
                if (!TextUtils.isEmpty(galleryPinList)) {
                    pinPrefList = new HashSet(Arrays.asList(galleryPinList.split(";")));
                }
            } else {
                pinPrefList = this.mPinnedSharedPrefs.getStringSet(type, null);
            }
            Log.d(TAG, "mPinnedSharedPrefs pinPrefList : " + pinPrefList + ", type : " + type);
        }
        for (int i = 0; i < list.size(); i++) {
            String componentName = ((ResolveInfo) list.get(i)).getComponentInfo().getComponentName().flattenToShortString();
            String packageName = ((ResolveInfo) list.get(i)).activityInfo.packageName;
            boolean isPinned = false;
            if (isChoose && pinPrefList != null && pinPrefList.size() > 0) {
                isPinned = pinPrefList.contains(componentName);
            }
            if (isPinned) {
                pinnedList.add((ResolveInfo) list.get(i));
                Log.d(TAG, "pinnedList add : " + list.get(i));
            } else if (priority == null || !(priority.contains(componentName) || priority.contains(packageName))) {
                otherList.add((ResolveInfo) list.get(i));
            } else {
                this.mPriorityList.add((ResolveInfo) list.get(i));
                Log.d(TAG, "priorityList add : " + list.get(i));
            }
        }
        if (!this.mPriorityList.isEmpty()) {
            sortPriorityList(priority);
        }
        list.clear();
        list.addAll(pinnedList);
        list.addAll(this.mPriorityList);
        list.addAll(otherList);
        Log.d(TAG, "finish to resort : " + list);
    }

    private void sortPriorityList(List<String> list) {
        List<ResolveInfo> resultList = new ArrayList();
        int i = 0;
        while (i < list.size()) {
            for (int j = 0; j < this.mPriorityList.size(); j++) {
                String packageName = ((ResolveInfo) this.mPriorityList.get(j)).activityInfo.packageName;
                if (((String) list.get(i)).equals(((ResolveInfo) this.mPriorityList.get(j)).getComponentInfo().getComponentName().flattenToShortString()) || ((String) list.get(i)).equals(packageName)) {
                    resultList.add((ResolveInfo) this.mPriorityList.get(j));
                }
            }
            i++;
        }
        Log.d(TAG, "sort priorityList : " + resultList);
        this.mPriorityList.clear();
        this.mPriorityList.addAll(resultList);
    }

    private List<String> getBlackResolveListWithType(String type) {
        if (type != null) {
            return (List) this.mCloudBlackResolveMap.get(type);
        }
        return null;
    }

    private List<String> getBlackChooseListWithType(String type) {
        if (type != null) {
            return (List) this.mCloudBlackChooseMap.get(type);
        }
        return null;
    }

    private List<String> getBlackChooseActivityListWithType(String type) {
        if (type != null) {
            return (List) this.mCloudBlackChooseActivityMap.get(type);
        }
        return null;
    }

    private List<String> getResolveListWithType(String type) {
        if (type != null) {
            return (List) this.mCloudResolveMap.get(type);
        }
        return null;
    }

    private List<String> getChooserListWithType(String type) {
        if (type != null) {
            return (List) this.mCloudChooserMap.get(type);
        }
        return null;
    }

    public String getIntentType(Intent intent) {
        String action = intent.getAction();
        Set<String> categories = intent.getCategories();
        String scheme = intent.getScheme();
        String type = intent.getType();
        Uri data = intent.getData();
        CharSequence host = data != null ? data.getHost() : null;
        String defaultAppType = "others";
        if (TextUtils.equals(action, "android.intent.action.MAIN") && isInDataSet(categories, "android.intent.category.HOME")) {
            defaultAppType = DEFAULT_APP_LAUNCHER;
        } else if (TextUtils.equals(scheme, "sms") || TextUtils.equals(scheme, "mms") || TextUtils.equals(scheme, "smsto") || TextUtils.equals(scheme, "mmsto")) {
            defaultAppType = DEFAULT_APP_MESSAGE;
        } else if (TextUtils.equals(action, "android.intent.action.DIAL") || TextUtils.equals(scheme, "tel") || TextUtils.equals(type, "vnd.android.cursor.dir/calls")) {
            defaultAppType = DEFAULT_APP_DIALER;
        } else if (TextUtils.equals(type, "vnd.android.cursor.dir/contact") || TextUtils.equals(type, "vnd.android.cursor.dir/phone_v2")) {
            defaultAppType = DEFAULT_APP_CONTACTS;
        } else if (TextUtils.equals(scheme, "http") || TextUtils.equals(scheme, "https")) {
            defaultAppType = DEFAULT_APP_BROWSER;
        } else if (TextUtils.equals(action, "android.media.action.IMAGE_CAPTURE") || TextUtils.equals(action, "android.media.action.VIDEO_CAPTURE") || TextUtils.equals(action, "android.media.action.VIDEO_CAMERA") || TextUtils.equals(action, "android.media.action.STILL_IMAGE_CAMERA") || TextUtils.equals(action, "com.oppo.action.CAMERA")) {
            defaultAppType = DEFAULT_APP_CAMERA;
        } else if (!TextUtils.isEmpty(type) && type.startsWith("image")) {
            defaultAppType = DEFAULT_APP_GALLERY;
        } else if (!TextUtils.isEmpty(type) && type.startsWith(DEFAULT_APP_AUDIO)) {
            defaultAppType = DEFAULT_APP_AUDIO;
        } else if (!TextUtils.isEmpty(type) && type.startsWith(DEFAULT_APP_VIDEO)) {
            defaultAppType = DEFAULT_APP_VIDEO;
        } else if (TextUtils.equals(scheme, "mailto")) {
            defaultAppType = DEFAULT_APP_EMAIL;
        } else if (TextUtils.equals(type, "text/plain")) {
            defaultAppType = DEFAULT_APP_TEXT;
        } else if (TextUtils.equals(type, "application/pdf")) {
            defaultAppType = DEFAULT_APP_PDF;
        } else if (TextUtils.equals(type, "application/msword") || TextUtils.equals(type, "application/ms-word") || TextUtils.equals(type, "application/vnd.ms-word") || TextUtils.equals(type, "application/vnd.openxmlformats-officedocument.wordprocessingml.document") || TextUtils.equals(type, "application/vnd.openxmlformats-officedocument.wordprocessingml.template") || TextUtils.equals(type, "application/vnd.ms-word.document.macroenabled.12") || TextUtils.equals(type, "application/vnd.ms-word.template.macroenabled.12")) {
            defaultAppType = DEFAULT_APP_WORD;
        } else if (TextUtils.equals(type, "application/msexcel") || TextUtils.equals(type, "application/ms-excel") || TextUtils.equals(type, "application/vnd.ms-excel") || TextUtils.equals(type, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet") || TextUtils.equals(type, "application/vnd.openxmlformats-officedocument.spreadsheetml.template") || TextUtils.equals(type, "application/vnd.ms-excel.sheet.macroenabled.12") || TextUtils.equals(type, "application/vnd.ms-excel.template.macroenabled.12") || TextUtils.equals(type, "application/vnd.ms-excel.addin.macroenabled.12") || TextUtils.equals(type, "application/vnd.ms-excel.sheet.binary.macroenabled.12")) {
            defaultAppType = DEFAULT_APP_EXCEL;
        } else if (TextUtils.equals(type, "application/mspowerpoint") || TextUtils.equals(type, "application/ms-powerpoint") || TextUtils.equals(type, "application/vnd.ms-powerpoint") || TextUtils.equals(type, "application/vnd.openxmlformats-officedocument.presentationml.presentation") || TextUtils.equals(type, "application/vnd.openxmlformats-officedocument.presentationml.template") || TextUtils.equals(type, "application/vnd.openxmlformats-officedocument.presentationml.slideshow") || TextUtils.equals(type, "application/vnd.ms-powerpoint.presentation.macroenabled.12") || TextUtils.equals(type, "application/vnd.ms-powerpoint.template.macroenabled.12") || TextUtils.equals(type, "application/vnd.ms-powerpoint.slideshow.macroenabled.12") || TextUtils.equals(type, "application/vnd.ms-powerpoint.addin.macroenabled.12") || TextUtils.equals(type, "application/vnd.ms-powerpoint.slide.macroenabled.12")) {
            defaultAppType = DEFAULT_APP_PPT;
        } else if (TextUtils.equals(scheme, DEFAULT_APP_MARKET) && TextUtils.equals(host, "details")) {
            defaultAppType = DEFAULT_APP_MARKET;
        }
        Log.d(TAG, "The MIME data type of this intent is " + defaultAppType);
        return defaultAppType;
    }

    /* JADX WARNING: Missing block: B:4:0x0009, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private <T> boolean isInDataSet(Collection<T> dataSet, T e) {
        if (dataSet == null || dataSet.isEmpty() || e == null) {
            return false;
        }
        return dataSet.contains(e);
    }

    private <T> boolean equalsDataSet(Collection<T> a, Collection<T> b) {
        boolean z = false;
        if (a == null && b == null) {
            return true;
        }
        if (a == null || b == null) {
            return false;
        }
        if (a.isEmpty() && b.isEmpty()) {
            return true;
        }
        if (a.containsAll(b)) {
            z = b.containsAll(a);
        }
        return z;
    }

    public boolean isChooserAction(Intent intent) {
        if (intent == null) {
            return false;
        }
        String action = intent.getAction();
        if (action == null || (!action.equals("android.intent.action.SEND") && !action.equals("android.intent.action.SEND_MULTIPLE"))) {
            return false;
        }
        return true;
    }

    public void statisticsData(ResolveInfo ri, Intent intent, int which, String referrerPackage) {
        Map map = new HashMap();
        String componentName = ri.getComponentInfo().getComponentName().flattenToShortString();
        CharSequence componentLabel = ri.loadLabel(this.mContext.getPackageManager());
        String type = getIntentType(intent);
        boolean isChooser = isChooserAction(intent);
        map.put(KEY_INTENT, intent + "");
        map.put(KEY_ACTION, intent.getAction() + "");
        map.put(KEY_CATEGORIES, intent.getCategories() + "");
        map.put(KEY_DATA, intent.getData() + "");
        map.put(KEY_MIME_TYPE, intent.getType() + "");
        map.put(KEY_SCHEME, intent.getScheme() + "");
        map.put("type", type);
        map.put(KEY_CHOOSE, isChooser + "");
        map.put(KEY_REFERRER_PACKAGE, referrerPackage);
        map.put(KEY_TARGET_PACKAGE, ri.activityInfo.packageName);
        map.put(KEY_NAME, componentName);
        map.put(KEY_COMPONENT_LABEL, componentLabel + "");
        map.put(KEY_POSITION, which + "");
        OppoStatistics.onCommon(this.mContext, CODE, APP_EVENT_ID, map, false);
        Log.d(TAG, "statistics data [resolver_app] :" + map);
    }

    private ResolveInfo[][] ListToArray(List<ResolveInfo> resolveInfoList) {
        mRowCounts = (int) Math.min(Math.ceil(((double) resolveInfoList.size()) / ((double) mColumnCounts)), 2.0d);
        ResolveInfo[][] array = (ResolveInfo[][]) Array.newInstance(ResolveInfo.class, new int[]{mRowCounts, mColumnCounts});
        int start = 0;
        int end = mColumnCounts + 0;
        int i = 0;
        while (start < resolveInfoList.size() && i < mRowCounts) {
            List<ResolveInfo> l = resolveInfoList.subList(start, end < resolveInfoList.size() ? end : resolveInfoList.size());
            System.arraycopy(l.toArray(), 0, array[i], 0, l.size());
            start = end;
            end = start + mColumnCounts;
            i++;
        }
        return array;
    }

    public ColorItem[][] getAppInfo(List<ResolveInfo> resolveInfoList, PackageManager mPm) {
        ResolveInfo[][] resolveInfoArray = ListToArray(resolveInfoList);
        ColorItem[][] mAppInfo = (ColorItem[][]) Array.newInstance(ColorItem.class, new int[]{mRowCounts, mColumnCounts});
        Integer[][] mAppIcons = (Integer[][]) Array.newInstance(Integer.class, new int[]{mRowCounts, mColumnCounts});
        String[][] mAppNames = (String[][]) Array.newInstance(String.class, new int[]{mRowCounts, mColumnCounts});
        for (int i = 0; i < resolveInfoArray.length; i++) {
            for (int j = 0; j < resolveInfoArray[i].length; j++) {
                if (resolveInfoArray[i][j] != null) {
                    mAppInfo[i][j] = new ColorItem();
                    ComponentInfo ci = null;
                    if (resolveInfoArray[i][j].isMultiApp) {
                        ci = getComponentInfo(resolveInfoArray[i][j]);
                        mAppInfo[i][j].setText(resolveInfoArray[i][j].loadLabel(mPm).toString() + mPm.getText(OppoThemeResources.OPPO_PACKAGE, 201590120, null));
                    } else {
                        mAppInfo[i][j].setText(resolveInfoArray[i][j].loadLabel(mPm).toString());
                    }
                    if (ci != null) {
                        String name = OppoMultiLauncherUtil.getInstance().getAliasByPackage(ci.packageName);
                        if (name != null) {
                            mAppInfo[i][j].setText(name);
                        }
                    }
                    mAppInfo[i][j].setIcon(oppoLoadIconForResolveInfo(resolveInfoArray[i][j], mPm));
                }
            }
        }
        return mAppInfo;
    }

    private ComponentInfo getComponentInfo(ResolveInfo info) {
        if (info == null) {
            return null;
        }
        if (info.activityInfo != null) {
            return info.activityInfo;
        }
        if (info.serviceInfo != null) {
            return info.serviceInfo;
        }
        if (info.providerInfo != null) {
            return info.providerInfo;
        }
        return null;
    }

    private Drawable oppoLoadIconForResolveInfo(ResolveInfo ri, PackageManager mPm) {
        Drawable dr;
        if (!(ri.resolvePackageName == null || ri.icon == 0)) {
            if (ri.activityInfo.packageName == null || (ri.resolvePackageName.contains(ri.activityInfo.packageName) ^ 1) == 0) {
                dr = OppoThemeHelper.getDrawable(mPm, ri.activityInfo.packageName, ri.icon, ri.activityInfo.applicationInfo, null);
            } else {
                dr = OppoThemeHelper.getDrawable(mPm, ri.resolvePackageName, ri.icon, null, null);
            }
            if (dr != null) {
                return dr;
            }
        }
        int iconRes = ri.getIconResource();
        if (iconRes != 0) {
            dr = OppoThemeHelper.getDrawable(mPm, ri.activityInfo.packageName, iconRes, ri.activityInfo.applicationInfo, null);
            if (dr != null) {
                return dr;
            }
        }
        return ri.loadIcon(mPm);
    }

    public void adjustPosition(List<ResolveInfo> resolveInfoList, List<String> priorPackage) {
        List<ResolveInfo> prior = new ArrayList();
        List<ResolveInfo> priorSort = new ArrayList();
        List<ResolveInfo> rest = new ArrayList();
        for (int i = 0; i < resolveInfoList.size(); i++) {
            if (priorPackage.contains(((ResolveInfo) resolveInfoList.get(i)).activityInfo.packageName)) {
                prior.add((ResolveInfo) resolveInfoList.get(i));
            } else {
                rest.add((ResolveInfo) resolveInfoList.get(i));
            }
        }
        for (int j = 0; j < priorPackage.size(); j++) {
            for (int k = 0; k < prior.size(); k++) {
                if (((String) priorPackage.get(j)).equals(((ResolveInfo) prior.get(k)).activityInfo.packageName)) {
                    priorSort.add((ResolveInfo) prior.get(k));
                }
            }
        }
        resolveInfoList.clear();
        resolveInfoList.addAll(priorSort);
        resolveInfoList.addAll(rest);
    }

    public boolean isMarketRecommendType(String type) {
        if (DEFAULT_APP_EMAIL.equals(type) || DEFAULT_APP_VIDEO.equals(type) || DEFAULT_APP_TEXT.equals(type) || DEFAULT_APP_PDF.equals(type) || DEFAULT_APP_WORD.equals(type) || DEFAULT_APP_EXCEL.equals(type) || DEFAULT_APP_PPT.equals(type)) {
            return true;
        }
        return false;
    }
}
