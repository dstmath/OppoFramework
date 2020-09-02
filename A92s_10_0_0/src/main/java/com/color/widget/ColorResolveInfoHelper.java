package com.color.widget;

import android.app.OppoActivityManager;
import android.app.slice.SliceItem;
import android.common.OppoFeatureCache;
import android.content.BroadcastReceiver;
import android.content.ClipDescription;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.ComponentInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.IColorThemeManager;
import android.content.res.OppoThemeResources;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.storage.StorageManager;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.provider.Settings;
import android.telecom.PhoneAccount;
import android.text.TextUtils;
import android.util.Log;
import com.android.internal.R;
import com.android.internal.telephony.PhoneConstants;
import com.color.multiapp.ColorMultiAppManager;
import com.color.settings.ColorSettings;
import com.color.settings.ColorSettingsChangeListener;
import com.color.util.ColorResolveData;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    private static final String EXPORT_FEATURE = "oppo.version.exp";
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
    private static final String RESOLVER_APP_FILE = "safecenter/safe_default_app_list.xml";
    private static final String SHOW_EVENT_ID = "resolver_show";
    private static final String TAG = "ColorResolveInfoHelper";
    private static final String TAG_BLACK_CHOOSE_ACTIVITY = "black_choose_activity";
    private static final String TAG_BLACK_CHOOSE_PACKAGE = "black_choose_package";
    private static final String TAG_BLACK_RESOLVE = "black_resolve";
    private static final String TAG_CHOOSE = "choose";
    private static final String TAG_DEFAULT = "default";
    private static final String TAG_RESOLVE = "resolve";
    private static final String TAG_TYPE = "type";
    private static final int mColumnCounts = 4;
    private static int mRowCounts = 2;
    private static ColorResolveInfoHelper sResolveInfoHelper;
    private boolean DEBUG = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    private HashMap<String, List<String>> mCloudBlackChooseActivityMap = new HashMap<>();
    private HashMap<String, List<String>> mCloudBlackChooseMap = new HashMap<>();
    private HashMap<String, List<String>> mCloudBlackResolveMap = new HashMap<>();
    private HashMap<String, List<String>> mCloudChooserMap = new HashMap<>();
    private HashMap<String, List<String>> mCloudResolveMap = new HashMap<>();
    /* access modifiers changed from: private */
    public ColorSettingsChangeListener mConfigChangeListener = new ColorSettingsChangeListener(null) {
        /* class com.color.widget.ColorResolveInfoHelper.AnonymousClass3 */

        @Override // com.color.settings.ColorSettingsChangeListener
        public void onSettingsChange(boolean selfChange, String customPath, int userId) {
            Log.d(ColorResolveInfoHelper.TAG, "onConfigChange path=" + customPath + " selfChange=" + selfChange + " userId:" + userId);
            ColorResolveInfoHelper.this.readConfigFileBackground(userId);
        }
    };
    /* access modifiers changed from: private */
    public Context mContext;
    private ColorResolveData mData;
    private final Object mLock = new Object();
    private HashMap<String, List<String>> mMap1 = new HashMap<>();
    private HashMap<String, List<String>> mMap2 = new HashMap<>();
    private HashMap<String, List<String>> mMap3 = new HashMap<>();
    private HashMap<String, List<String>> mMap4 = new HashMap<>();
    private HashMap<String, List<String>> mMap5 = new HashMap<>();
    private SharedPreferences mPinnedSharedPrefs;
    private List<ResolveInfo> mPriorityList = new ArrayList();

    public static ColorResolveInfoHelper getInstance(Context context) {
        if (sResolveInfoHelper == null) {
            sResolveInfoHelper = new ColorResolveInfoHelper(context.getApplicationContext());
        }
        return sResolveInfoHelper;
    }

    public void init() {
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
            readConfigFileBackground(0);
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
        if (isChooserAction(intent)) {
            List<String> list = this.mMap4.get(type);
            Log.d(TAG, "getCloudBlackList = " + this.mMap4);
            return list;
        }
        List<String> list2 = this.mMap1.get(type);
        Log.d(TAG, "getCloudBlackList = " + this.mMap1);
        return list2;
    }

    /* access modifiers changed from: private */
    public void readConfigFileBackground(final int userId) {
        new Thread(new Runnable() {
            /* class com.color.widget.ColorResolveInfoHelper.AnonymousClass1 */

            public void run() {
                ColorResolveInfoHelper.this.readConfigFile(userId);
            }
        }, TAG).start();
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:115:0x0293, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:117:?, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:119:0x0299, code lost:
        if ((r1 instanceof java.io.Closeable) != false) goto L_0x029b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:120:0x029b, code lost:
        ((java.io.Closeable) r1).close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:121:0x02a2, code lost:
        r2.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:122:0x02a6, code lost:
        if (r3 != null) goto L_0x02a8;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:123:0x02a8, code lost:
        r3.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:126:0x02b1, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:127:0x02b2, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:128:0x02b7, code lost:
        if ((r1 instanceof java.io.Closeable) != false) goto L_0x02b9;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:129:0x02b9, code lost:
        ((java.io.Closeable) r1).close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:130:0x02c0, code lost:
        if (r2 != null) goto L_0x02c2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:131:0x02c2, code lost:
        r2.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:132:0x02c5, code lost:
        if (r3 != null) goto L_0x02c7;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:133:0x02c7, code lost:
        r3.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:134:0x02cb, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:135:0x02d0, code lost:
        if ((r1 instanceof java.io.Closeable) != false) goto L_0x02d2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:136:0x02d2, code lost:
        ((java.io.Closeable) r1).close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:137:0x02d9, code lost:
        if (r2 != null) goto L_0x02db;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:138:0x02db, code lost:
        r2.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:139:0x02de, code lost:
        if (r3 != null) goto L_0x02e0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:140:0x02e0, code lost:
        r3.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:141:0x02e4, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:142:0x02e9, code lost:
        if ((r1 instanceof java.io.Closeable) != false) goto L_0x02eb;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:143:0x02eb, code lost:
        ((java.io.Closeable) r1).close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:144:0x02f2, code lost:
        if (r2 != null) goto L_0x02f4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:145:0x02f4, code lost:
        r2.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:146:0x02f7, code lost:
        if (r3 != null) goto L_0x02f9;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:147:0x02f9, code lost:
        r3.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:150:0x0300, code lost:
        if ((r1 instanceof java.io.Closeable) != false) goto L_0x0302;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:151:0x0302, code lost:
        ((java.io.Closeable) r1).close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:152:0x0309, code lost:
        if (r2 != null) goto L_0x030b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:153:0x030b, code lost:
        r2.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:154:0x030e, code lost:
        if (r3 != null) goto L_0x0310;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:155:0x0310, code lost:
        r3.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:156:0x0314, code lost:
        r4 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:157:0x0315, code lost:
        r4.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:158:0x0318, code lost:
        throw r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:161:0x031b, code lost:
        if ((0 instanceof java.io.Closeable) != false) goto L_0x031d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:162:0x031d, code lost:
        null.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:163:0x0324, code lost:
        if (r2 != null) goto L_0x0326;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:164:0x0326, code lost:
        r2.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:165:0x0329, code lost:
        if (r3 != null) goto L_0x032b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:166:0x032b, code lost:
        r3.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:167:0x032f, code lost:
        r7 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:168:0x0330, code lost:
        r7.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:175:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:177:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:178:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:179:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:180:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:181:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:182:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:183:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:185:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:186:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:187:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:4:0x0021, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:5:0x0024, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:6:0x0027, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Removed duplicated region for block: B:126:0x02b1 A[ExcHandler: IOException (r0v1 'e' java.io.IOException A[CUSTOM_DECLARE]), PHI: r1 r2 r3 
      PHI: (r1v1 'parser' org.xmlpull.v1.XmlPullParser) = (r1v2 'parser' org.xmlpull.v1.XmlPullParser), (r1v2 'parser' org.xmlpull.v1.XmlPullParser), (r1v0 'parser' org.xmlpull.v1.XmlPullParser), (r1v0 'parser' org.xmlpull.v1.XmlPullParser) binds: [B:27:0x0090, B:28:?, B:12:0x0034, B:13:?] A[DONT_GENERATE, DONT_INLINE]
      PHI: (r2v1 'br' java.io.BufferedReader) = (r2v2 'br' java.io.BufferedReader), (r2v2 'br' java.io.BufferedReader), (r2v0 'br' java.io.BufferedReader), (r2v0 'br' java.io.BufferedReader) binds: [B:27:0x0090, B:28:?, B:12:0x0034, B:13:?] A[DONT_GENERATE, DONT_INLINE]
      PHI: (r3v2 'is' java.io.InputStream) = (r3v3 'is' java.io.InputStream), (r3v3 'is' java.io.InputStream), (r3v1 'is' java.io.InputStream), (r3v1 'is' java.io.InputStream) binds: [B:27:0x0090, B:28:?, B:12:0x0034, B:13:?] A[DONT_GENERATE, DONT_INLINE], Splitter:B:12:0x0034] */
    /* JADX WARNING: Removed duplicated region for block: B:5:0x0024 A[ExcHandler: UnsupportedEncodingException (r0v57 'e' java.io.UnsupportedEncodingException A[CUSTOM_DECLARE]), PHI: r1 r2 r3 
      PHI: (r1v4 'parser' org.xmlpull.v1.XmlPullParser) = (r1v0 'parser' org.xmlpull.v1.XmlPullParser), (r1v2 'parser' org.xmlpull.v1.XmlPullParser), (r1v2 'parser' org.xmlpull.v1.XmlPullParser), (r1v0 'parser' org.xmlpull.v1.XmlPullParser), (r1v0 'parser' org.xmlpull.v1.XmlPullParser), (r1v0 'parser' org.xmlpull.v1.XmlPullParser) binds: [B:1:0x0019, B:27:0x0090, B:28:?, B:12:0x0034, B:13:?, B:2:?] A[DONT_GENERATE, DONT_INLINE]
      PHI: (r2v4 'br' java.io.BufferedReader) = (r2v0 'br' java.io.BufferedReader), (r2v2 'br' java.io.BufferedReader), (r2v2 'br' java.io.BufferedReader), (r2v0 'br' java.io.BufferedReader), (r2v0 'br' java.io.BufferedReader), (r2v0 'br' java.io.BufferedReader) binds: [B:1:0x0019, B:27:0x0090, B:28:?, B:12:0x0034, B:13:?, B:2:?] A[DONT_GENERATE, DONT_INLINE]
      PHI: (r3v7 'is' java.io.InputStream) = (r3v0 'is' java.io.InputStream), (r3v3 'is' java.io.InputStream), (r3v3 'is' java.io.InputStream), (r3v1 'is' java.io.InputStream), (r3v1 'is' java.io.InputStream), (r3v0 'is' java.io.InputStream) binds: [B:1:0x0019, B:27:0x0090, B:28:?, B:12:0x0034, B:13:?, B:2:?] A[DONT_GENERATE, DONT_INLINE], Splitter:B:1:0x0019] */
    /* JADX WARNING: Removed duplicated region for block: B:6:0x0027 A[ExcHandler: XmlPullParserException (r0v53 'e' org.xmlpull.v1.XmlPullParserException A[CUSTOM_DECLARE]), PHI: r1 r2 r3 
      PHI: (r1v3 'parser' org.xmlpull.v1.XmlPullParser) = (r1v0 'parser' org.xmlpull.v1.XmlPullParser), (r1v2 'parser' org.xmlpull.v1.XmlPullParser), (r1v2 'parser' org.xmlpull.v1.XmlPullParser), (r1v0 'parser' org.xmlpull.v1.XmlPullParser), (r1v0 'parser' org.xmlpull.v1.XmlPullParser), (r1v0 'parser' org.xmlpull.v1.XmlPullParser) binds: [B:1:0x0019, B:27:0x0090, B:28:?, B:12:0x0034, B:13:?, B:2:?] A[DONT_GENERATE, DONT_INLINE]
      PHI: (r2v3 'br' java.io.BufferedReader) = (r2v0 'br' java.io.BufferedReader), (r2v2 'br' java.io.BufferedReader), (r2v2 'br' java.io.BufferedReader), (r2v0 'br' java.io.BufferedReader), (r2v0 'br' java.io.BufferedReader), (r2v0 'br' java.io.BufferedReader) binds: [B:1:0x0019, B:27:0x0090, B:28:?, B:12:0x0034, B:13:?, B:2:?] A[DONT_GENERATE, DONT_INLINE]
      PHI: (r3v6 'is' java.io.InputStream) = (r3v0 'is' java.io.InputStream), (r3v3 'is' java.io.InputStream), (r3v3 'is' java.io.InputStream), (r3v1 'is' java.io.InputStream), (r3v1 'is' java.io.InputStream), (r3v0 'is' java.io.InputStream) binds: [B:1:0x0019, B:27:0x0090, B:28:?, B:12:0x0034, B:13:?, B:2:?] A[DONT_GENERATE, DONT_INLINE], Splitter:B:1:0x0019] */
    /* JADX WARNING: Removed duplicated region for block: B:7:0x002a A[ExcHandler: Exception (e java.lang.Exception), PHI: r3 
      PHI: (r3v5 'is' java.io.InputStream) = (r3v0 'is' java.io.InputStream), (r3v1 'is' java.io.InputStream), (r3v1 'is' java.io.InputStream), (r3v0 'is' java.io.InputStream) binds: [B:1:0x0019, B:12:0x0034, B:13:?, B:2:?] A[DONT_GENERATE, DONT_INLINE], Splitter:B:1:0x0019] */
    public void readConfigFile(int userId) {
        int event;
        XmlPullParser parser = null;
        BufferedReader br = null;
        InputStream is = null;
        this.mMap1.clear();
        this.mMap2.clear();
        this.mMap3.clear();
        String curAppType = null;
        try {
            is = ColorSettings.readConfigAsUser(this.mContext, RESOLVER_APP_FILE, userId, 0);
            if (is == null && userId != 0) {
                Log.e(TAG, "userId = " + userId + " inputString is empty");
                is = ColorSettings.readConfig(this.mContext, RESOLVER_APP_FILE, 0);
            }
            if (is == null) {
                try {
                    if (0 instanceof Closeable) {
                        null.close();
                    } else if (br != null) {
                        br.close();
                    }
                    if (is != null) {
                        is.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                parser = XmlPullParserFactory.newInstance().newPullParser();
                br = new BufferedReader(new InputStreamReader(is));
                parser.setInput(br);
                Log.d(TAG, "read cloud file");
                do {
                    event = parser.next();
                    String tag = parser.getName();
                    if (event == 2) {
                        if ("default".equals(tag)) {
                            int attrCount = parser.getAttributeCount();
                            if (attrCount > 0) {
                                int i = 0;
                                while (true) {
                                    if (i >= attrCount) {
                                        break;
                                    }
                                    String attrName = parser.getAttributeName(i);
                                    String attrValue = parser.getAttributeValue(i);
                                    if ("type".equals(attrName)) {
                                        curAppType = attrValue;
                                        break;
                                    }
                                    i++;
                                }
                            }
                        } else if (TAG_BLACK_RESOLVE.equals(tag)) {
                            if (curAppType != null) {
                                parser.next();
                                String blackResolve = parser.getText().trim();
                                if (!TextUtils.isEmpty(blackResolve)) {
                                    List<String> blackList = this.mMap1.get(curAppType);
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
                                    List<String> blackList2 = this.mMap4.get(curAppType);
                                    if (blackList2 == null) {
                                        blackList2 = new ArrayList();
                                    }
                                    if (!blackList2.contains(blackChoose)) {
                                        blackList2.add(blackChoose);
                                        Log.d(TAG, "curAppType=" + curAppType + ", blackChoose=" + blackChoose);
                                    }
                                    this.mMap4.put(curAppType, blackList2);
                                }
                            }
                        } else if (TAG_BLACK_CHOOSE_ACTIVITY.equals(tag)) {
                            if (curAppType != null) {
                                parser.next();
                                String blackChooseActivity = parser.getText().trim();
                                if (!TextUtils.isEmpty(blackChooseActivity)) {
                                    List<String> blackList3 = this.mMap5.get(curAppType);
                                    if (blackList3 == null) {
                                        blackList3 = new ArrayList();
                                    }
                                    if (!blackList3.contains(blackChooseActivity)) {
                                        blackList3.add(blackChooseActivity);
                                        Log.d(TAG, "curAppType=" + curAppType + ", blackChooseActivity=" + blackChooseActivity);
                                    }
                                    this.mMap5.put(curAppType, blackList3);
                                }
                            }
                        } else if (TAG_RESOLVE.equals(tag)) {
                            if (curAppType != null) {
                                parser.next();
                                String resolveApp = parser.getText().trim();
                                if (!TextUtils.isEmpty(resolveApp)) {
                                    List<String> resolveList = this.mMap2.get(curAppType);
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
                                List<String> chooserList = this.mMap3.get(curAppType);
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
                    } else if (event == 3 && "default".equals(tag)) {
                        curAppType = null;
                    }
                } while (event != 1);
                try {
                    if (parser instanceof Closeable) {
                        ((Closeable) parser).close();
                    } else {
                        br.close();
                    }
                    if (is != null) {
                        is.close();
                    }
                } catch (IOException e2) {
                    e2.printStackTrace();
                }
            }
        } catch (Exception e3) {
        } catch (XmlPullParserException e4) {
        } catch (UnsupportedEncodingException e5) {
        } catch (IOException e6) {
        }
    }

    private void initFileObserver() {
        ColorSettings.registerChangeListener(this.mContext, RESOLVER_APP_FILE, 0, this.mConfigChangeListener);
        this.mContext.registerReceiverAsUser(new BroadcastReceiver() {
            /* class com.color.widget.ColorResolveInfoHelper.AnonymousClass2 */

            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                int userId = intent.getIntExtra(Intent.EXTRA_USER_HANDLE, 0);
                ColorSettings.unRegisterChangeListener(ColorResolveInfoHelper.this.mContext, ColorResolveInfoHelper.this.mConfigChangeListener);
                if (userId == 0) {
                    ColorSettings.registerChangeListener(ColorResolveInfoHelper.this.mContext, ColorResolveInfoHelper.RESOLVER_APP_FILE, 0, ColorResolveInfoHelper.this.mConfigChangeListener);
                } else {
                    ColorSettings.registerChangeListenerAsUser(ColorResolveInfoHelper.this.mContext, ColorResolveInfoHelper.RESOLVER_APP_FILE, userId, 0, ColorResolveInfoHelper.this.mConfigChangeListener);
                }
                Log.d(ColorResolveInfoHelper.TAG, "action user switched:" + userId);
                ColorResolveInfoHelper.this.readConfigFileBackground(userId);
            }
        }, UserHandle.ALL, new IntentFilter(Intent.ACTION_USER_SWITCHED), null, null);
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

    public int getResolveTopSize(Intent intent) {
        String type = getIntentType(intent);
        if (this.mPriorityList == null) {
            return 0;
        }
        if (!"email".equals(type) || !isExport()) {
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

    private List<String> getPriorityListFromXml(boolean isChoose, String type) {
        if (isChoose) {
            return getChooserListWithType(type);
        }
        return getResolveListWithType(type);
    }

    public void resort(List<ResolveInfo> list, Intent intent) {
        initData();
        String type = getIntentType(intent);
        List<ResolveInfo> list2 = filterBlackApps(intent, type, list);
        boolean isChoose = isChooserAction(intent);
        startToResort(list2, type, getPriorityListFromXml(isChoose, type), isChoose);
    }

    private List<ResolveInfo> filterBlackApps(Intent intent, String type, List<ResolveInfo> result) {
        if (result == null || result.isEmpty()) {
            return result;
        }
        if (isChooserAction(intent)) {
            List<String> blackChooseList = getBlackChooseListWithType(type);
            if (blackChooseList != null && !blackChooseList.isEmpty()) {
                Iterator<ResolveInfo> it = result.iterator();
                while (it.hasNext()) {
                    ActivityInfo ai = it.next().activityInfo;
                    if (ai != null && isInDataSet(blackChooseList, ai.packageName)) {
                        Log.d(TAG, "remove black choose package : " + ai.packageName);
                        it.remove();
                    }
                }
            }
            List<String> blackChooseListActivity = getBlackChooseActivityListWithType(type);
            if (blackChooseListActivity != null && !blackChooseListActivity.isEmpty()) {
                Iterator<ResolveInfo> it2 = result.iterator();
                while (it2.hasNext()) {
                    String componentName = it2.next().getComponentInfo().getComponentName().flattenToShortString();
                    if (isInDataSet(blackChooseListActivity, componentName)) {
                        Log.d(TAG, "remove black choose componentName : " + componentName);
                        it2.remove();
                    }
                }
            }
        } else {
            List<String> blackResolveList = getBlackResolveListWithType(type);
            if (blackResolveList != null && !blackResolveList.isEmpty()) {
                Iterator<ResolveInfo> it3 = result.iterator();
                while (it3.hasNext()) {
                    ActivityInfo ai2 = it3.next().activityInfo;
                    if (ai2 != null && isInDataSet(blackResolveList, ai2.packageName)) {
                        Log.d(TAG, "remove black resolve package : " + ai2.packageName);
                        it3.remove();
                    }
                }
            }
        }
        return result;
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: oppo.util.OppoStatistics.onCommon(android.content.Context, java.lang.String, java.lang.String, java.util.Map<java.lang.String, java.lang.String>, boolean):void
     arg types: [android.content.Context, java.lang.String, java.lang.String, java.util.HashMap<java.lang.String, java.lang.String>, int]
     candidates:
      oppo.util.OppoStatistics.onCommon(android.content.Context, java.lang.String, java.lang.String, java.util.List<java.util.Map<java.lang.String, java.lang.String>>, boolean):void
      oppo.util.OppoStatistics.onCommon(android.content.Context, java.lang.String, java.lang.String, java.util.Map<java.lang.String, java.lang.String>, boolean):void */
    private void startToResort(List<ResolveInfo> list, String type, List<String> priority, boolean isChoose) {
        Log.d(TAG, "start to resort : " + list);
        if (!isChoose) {
            HashMap<String, String> map = new HashMap<>();
            map.put("type", type);
            OppoStatistics.onCommon(this.mContext, CODE, SHOW_EVENT_ID, (Map<String, String>) map, false);
            Log.d(TAG, "statistics data [resolver_show]: " + map);
        }
        if (this.mPinnedSharedPrefs == null) {
            this.mPinnedSharedPrefs = getPinnedSharedPrefs(this.mContext);
        }
        List<ResolveInfo> pinnedList = new ArrayList<>();
        List<ResolveInfo> otherList = new ArrayList<>();
        this.mPriorityList.clear();
        Set<String> pinPrefList = new HashSet<>();
        if (isChoose && type != null) {
            if (DEFAULT_APP_GALLERY.equals(type)) {
                String galleryPinList = Settings.Secure.getString(this.mContext.getContentResolver(), GALLERY_PIN_LIST);
                Log.d(TAG, "galleryPinList = " + galleryPinList);
                if (!TextUtils.isEmpty(galleryPinList)) {
                    pinPrefList = new HashSet<>(Arrays.asList(galleryPinList.split(";")));
                }
            } else {
                pinPrefList = this.mPinnedSharedPrefs.getStringSet(type, null);
            }
            Log.d(TAG, "mPinnedSharedPrefs pinPrefList : " + pinPrefList + ", type : " + type);
        }
        for (int i = 0; i < list.size(); i++) {
            String componentName = list.get(i).getComponentInfo().getComponentName().flattenToShortString();
            String packageName = list.get(i).activityInfo.packageName;
            boolean isPinned = false;
            isPinned = false;
            isPinned = false;
            if (isChoose && pinPrefList != null && pinPrefList.size() > 0) {
                isPinned = pinPrefList.contains(componentName);
            }
            if (isPinned) {
                pinnedList.add(list.get(i));
                Log.d(TAG, "pinnedList add : " + list.get(i));
            } else if (priority == null || (!priority.contains(componentName) && !priority.contains(packageName))) {
                otherList.add(list.get(i));
            } else {
                this.mPriorityList.add(list.get(i));
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

    public int getExpandSizeWithoutMoreIcon(List<ResolveInfo> list, Intent intent) {
        initData();
        String type = getIntentType(intent);
        List<ResolveInfo> list2 = filterBlackApps(intent, type, list);
        List<String> priority = getPriorityListFromXml(isChooserAction(intent), type);
        int result = 0;
        for (ResolveInfo info : list2) {
            String componentName = info.getComponentInfo().getComponentName().flattenToShortString();
            String packageName = info.activityInfo.packageName;
            if (priority != null && (priority.contains(componentName) || priority.contains(packageName))) {
                result++;
            }
        }
        return result;
    }

    private void sortPriorityList(List<String> list) {
        List<ResolveInfo> resultList = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            for (int j = 0; j < this.mPriorityList.size(); j++) {
                String packageName = this.mPriorityList.get(j).activityInfo.packageName;
                if (list.get(i).equals(this.mPriorityList.get(j).getComponentInfo().getComponentName().flattenToShortString()) || list.get(i).equals(packageName)) {
                    resultList.add(this.mPriorityList.get(j));
                }
            }
        }
        Log.d(TAG, "sort priorityList : " + resultList);
        this.mPriorityList.clear();
        this.mPriorityList.addAll(resultList);
    }

    private List<String> getBlackResolveListWithType(String type) {
        if (type != null) {
            return this.mCloudBlackResolveMap.get(type);
        }
        return null;
    }

    private List<String> getBlackChooseListWithType(String type) {
        if (type != null) {
            return this.mCloudBlackChooseMap.get(type);
        }
        return null;
    }

    private List<String> getBlackChooseActivityListWithType(String type) {
        if (type != null) {
            return this.mCloudBlackChooseActivityMap.get(type);
        }
        return null;
    }

    private List<String> getResolveListWithType(String type) {
        if (type != null) {
            return this.mCloudResolveMap.get(type);
        }
        return null;
    }

    private List<String> getChooserListWithType(String type) {
        if (type != null) {
            return this.mCloudChooserMap.get(type);
        }
        return null;
    }

    public String getIntentType(Intent intent) {
        String action = intent.getAction();
        Set<String> categories = intent.getCategories();
        String scheme = intent.getScheme();
        String type = intent.getType();
        Uri data = intent.getData();
        String host = data != null ? data.getHost() : null;
        String defaultAppType = "others";
        if (TextUtils.equals(action, Intent.ACTION_MAIN) && isInDataSet(categories, Intent.CATEGORY_HOME)) {
            defaultAppType = DEFAULT_APP_LAUNCHER;
        } else if (TextUtils.equals(scheme, "sms") || TextUtils.equals(scheme, PhoneConstants.APN_TYPE_MMS) || TextUtils.equals(scheme, "smsto") || TextUtils.equals(scheme, "mmsto")) {
            defaultAppType = "message";
        } else if (TextUtils.equals(action, Intent.ACTION_DIAL) || TextUtils.equals(scheme, PhoneAccount.SCHEME_TEL) || TextUtils.equals(type, CallLog.Calls.CONTENT_TYPE)) {
            defaultAppType = DEFAULT_APP_DIALER;
        } else if (TextUtils.equals(type, ContactsContract.Contacts.CONTENT_TYPE) || TextUtils.equals(type, ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE)) {
            defaultAppType = "contacts";
        } else if (TextUtils.equals(scheme, IntentFilter.SCHEME_HTTP) || TextUtils.equals(scheme, IntentFilter.SCHEME_HTTPS)) {
            defaultAppType = DEFAULT_APP_BROWSER;
        } else if (TextUtils.equals(action, MediaStore.ACTION_IMAGE_CAPTURE) || TextUtils.equals(action, MediaStore.ACTION_VIDEO_CAPTURE) || TextUtils.equals(action, MediaStore.INTENT_ACTION_VIDEO_CAMERA) || TextUtils.equals(action, MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA) || TextUtils.equals(action, "com.oppo.action.CAMERA")) {
            defaultAppType = "camera";
        } else if (!TextUtils.isEmpty(type) && type.startsWith(SliceItem.FORMAT_IMAGE)) {
            defaultAppType = DEFAULT_APP_GALLERY;
        } else if (!TextUtils.isEmpty(type) && type.startsWith("audio")) {
            defaultAppType = "audio";
        } else if (!TextUtils.isEmpty(type) && type.startsWith("video")) {
            defaultAppType = "video";
        } else if (TextUtils.equals(scheme, "mailto")) {
            defaultAppType = "email";
        } else if (TextUtils.equals(type, ClipDescription.MIMETYPE_TEXT_PLAIN)) {
            defaultAppType = DEFAULT_APP_TEXT;
        } else if (TextUtils.equals(type, "application/pdf")) {
            defaultAppType = DEFAULT_APP_PDF;
        } else if (TextUtils.equals(type, "application/msword") || TextUtils.equals(type, "application/ms-word") || TextUtils.equals(type, "application/vnd.ms-word") || TextUtils.equals(type, "application/vnd.openxmlformats-officedocument.wordprocessingml.document") || TextUtils.equals(type, "application/vnd.openxmlformats-officedocument.wordprocessingml.template") || TextUtils.equals(type, "application/vnd.ms-word.document.macroenabled.12") || TextUtils.equals(type, "application/vnd.ms-word.template.macroenabled.12")) {
            defaultAppType = "word";
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

    private <T> boolean isInDataSet(Collection<T> dataSet, T e) {
        if (dataSet == null || dataSet.isEmpty() || e == null) {
            return false;
        }
        return dataSet.contains(e);
    }

    private <T> boolean equalsDataSet(Collection<T> a, Collection<T> b) {
        if (a == null && b == null) {
            return true;
        }
        if (a == null || b == null) {
            return false;
        }
        if (a.isEmpty() && b.isEmpty()) {
            return true;
        }
        if (!a.containsAll(b) || !b.containsAll(a)) {
            return false;
        }
        return true;
    }

    public boolean isChooserAction(Intent intent) {
        String action;
        if (intent == null || (action = intent.getAction()) == null || (!action.equals(Intent.ACTION_SEND) && !action.equals(Intent.ACTION_SEND_MULTIPLE))) {
            return false;
        }
        return true;
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: oppo.util.OppoStatistics.onCommon(android.content.Context, java.lang.String, java.lang.String, java.util.Map<java.lang.String, java.lang.String>, boolean):void
     arg types: [android.content.Context, java.lang.String, java.lang.String, java.util.HashMap<java.lang.String, java.lang.String>, int]
     candidates:
      oppo.util.OppoStatistics.onCommon(android.content.Context, java.lang.String, java.lang.String, java.util.List<java.util.Map<java.lang.String, java.lang.String>>, boolean):void
      oppo.util.OppoStatistics.onCommon(android.content.Context, java.lang.String, java.lang.String, java.util.Map<java.lang.String, java.lang.String>, boolean):void */
    public void statisticsData(ResolveInfo ri, Intent intent, int which, String referrerPackage) {
        HashMap<String, String> map = new HashMap<>();
        String componentName = ri.getComponentInfo().getComponentName().flattenToShortString();
        CharSequence componentLabel = ri.loadLabel(this.mContext.getPackageManager());
        String type = getIntentType(intent);
        boolean isChooser = isChooserAction(intent);
        map.put("intent", intent + "");
        map.put("action", intent.getAction() + "");
        map.put(KEY_CATEGORIES, intent.getCategories() + "");
        map.put("data", intent.getData() + "");
        map.put(KEY_MIME_TYPE, intent.getType() + "");
        map.put(KEY_SCHEME, intent.getScheme() + "");
        map.put("type", type);
        map.put(KEY_CHOOSE, isChooser + "");
        map.put(KEY_REFERRER_PACKAGE, referrerPackage);
        map.put(KEY_TARGET_PACKAGE, ri.activityInfo.packageName);
        map.put("name", componentName);
        map.put(KEY_COMPONENT_LABEL, ((Object) componentLabel) + "");
        map.put("position", which + "");
        OppoStatistics.onCommon(this.mContext, CODE, APP_EVENT_ID, (Map<String, String>) map, false);
        Log.d(TAG, "statistics data [resolver_app] :" + map);
    }

    public ColorItem getAppInfo(ResolveInfo info, PackageManager mPm, boolean isMultiApp) {
        String name;
        if (info == null) {
            return null;
        }
        ColorItem appInfo = new ColorItem();
        ComponentInfo ci = null;
        if (isMultiApp) {
            ci = getComponentInfo(info);
            appInfo.setText(info.loadLabel(mPm).toString() + ((Object) mPm.getText(OppoThemeResources.OPPO_PACKAGE, 201590120, null)));
        } else {
            appInfo.setText(info.loadLabel(mPm).toString());
        }
        if (!(ci == null || (name = ColorMultiAppManager.getInstance().getAliasMultiApp(ci.packageName)) == null)) {
            appInfo.setText(name);
        }
        appInfo.setIcon(oppoLoadIconForResolveInfo(info, mPm));
        return appInfo;
    }

    public List<ColorItem> getDefaultAppInfo(int count) {
        List<ColorItem> appInfo = new ArrayList<>();
        mRowCounts = (int) Math.min(Math.ceil(((double) count) / 4.0d), 2.0d);
        for (int i = 0; i < mRowCounts; i++) {
            int j = 0;
            while (j < count - (i * 4) && j < 4) {
                ColorItem item = new ColorItem();
                item.setIcon(this.mContext.getDrawable(R.drawable.resolver_icon_placeholder));
                item.setText("");
                appInfo.add(item);
                j++;
            }
        }
        return appInfo;
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
        Drawable dr2;
        if (!(ri.resolvePackageName == null || ri.icon == 0)) {
            if (ri.activityInfo.packageName == null || ri.resolvePackageName.contains(ri.activityInfo.packageName)) {
                dr2 = ((IColorThemeManager) OppoFeatureCache.getOrCreate(IColorThemeManager.DEFAULT, new Object[0])).loadResolveIcon(ri, mPm, ri.activityInfo.packageName, ri.icon, ri.activityInfo.applicationInfo, true);
            } else {
                dr2 = ((IColorThemeManager) OppoFeatureCache.getOrCreate(IColorThemeManager.DEFAULT, new Object[0])).loadResolveIcon(ri, mPm, ri.resolvePackageName, ri.icon, null, true);
            }
            if (dr2 != null) {
                return dr2;
            }
        }
        int iconRes = ri.getIconResource();
        if (iconRes == 0 || (dr = ((IColorThemeManager) OppoFeatureCache.getOrCreate(IColorThemeManager.DEFAULT, new Object[0])).loadResolveIcon(ri, mPm, ri.activityInfo.packageName, iconRes, ri.activityInfo.applicationInfo, true)) == null) {
            return ri.loadIcon(mPm);
        }
        return dr;
    }

    public void adjustPosition(List<ResolveInfo> resolveInfoList, List<String> priorPackage) {
        List<ResolveInfo> prior = new ArrayList<>();
        List<ResolveInfo> priorSort = new ArrayList<>();
        List<ResolveInfo> rest = new ArrayList<>();
        for (int i = 0; i < resolveInfoList.size(); i++) {
            if (priorPackage.contains(resolveInfoList.get(i).activityInfo.packageName)) {
                prior.add(resolveInfoList.get(i));
            } else {
                rest.add(resolveInfoList.get(i));
            }
        }
        for (int j = 0; j < priorPackage.size(); j++) {
            for (int k = 0; k < prior.size(); k++) {
                if (priorPackage.get(j).equals(prior.get(k).activityInfo.packageName)) {
                    priorSort.add(prior.get(k));
                }
            }
        }
        resolveInfoList.clear();
        resolveInfoList.addAll(priorSort);
        resolveInfoList.addAll(rest);
    }

    public boolean isMarketRecommendType(String type) {
        if ("email".equals(type) || "video".equals(type) || DEFAULT_APP_TEXT.equals(type) || DEFAULT_APP_PDF.equals(type) || "word".equals(type) || DEFAULT_APP_EXCEL.equals(type) || DEFAULT_APP_PPT.equals(type)) {
            return true;
        }
        return false;
    }

    private boolean isExport() {
        return this.mContext.getPackageManager().hasSystemFeature(EXPORT_FEATURE);
    }
}
