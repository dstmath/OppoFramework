package com.android.server.notification;

import android.app.ActivityManager;
import android.common.OppoFeatureCache;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.AtomicFile;
import android.util.Log;
import android.util.Xml;
import com.android.internal.util.FastXmlSerializer;
import com.android.server.am.IColorMultiAppManager;
import com.android.server.display.ai.utils.BrightnessConstants;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import libcore.io.IoUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class OpenID {
    private static final String ATTR_NAME = "name";
    private static final String ATTR_SIGN = "sign";
    private static final String ATTR_UID = "uid";
    private static final String ATTR_VALUE = "value";
    private static final String ATTR_VERSION = "version";
    private static final String COLOR_DUID_SIGN = "color_openid_duid_sign";
    private static final int DB_VERSION = 1;
    private static final boolean DEBUG_INTERNAL = false;
    private static final String HEYTAP_ID_BROADCAST_ACTION = "heytap.intent.action.HEYTAPID";
    private static final String[] HeytapID_BROADCAST_RECEIVER_LIST = {"com.heytap.habit.analysis"};
    public static final String MD5 = "MD5";
    private static final String MUTIL_APP_UID = "999";
    private static final String REASON_TYPE_DEFAULT = "0";
    private static final String REASON_TYPE_INIT = "1";
    private static final String REASON_TYPE_REGENERATE = "2";
    public static final String SHA1 = "SHA1";
    public static final String SHA256 = "SHA256";
    private static final String SPLITER = "|";
    private static final String SYSTEM = "system";
    private static final String TAG = "OpenID";
    private static final String TAG_APID = "apid";
    private static final String TAG_AUID = "auid";
    private static final String TAG_DUID = "duid";
    private static final String TAG_GUID = "guid";
    private static final String TAG_OPENID_CONFIG = "openid_config";
    private static final String TAG_OUID = "ouid";
    public static final String TYPE_APID = "APID";
    public static final String TYPE_AUID = "AUID";
    public static final String TYPE_DUID = "DUID";
    public static final String TYPE_GUID = "GUID";
    public static final String TYPE_OUID = "OUID";
    private Map<String, String> mAPIDMap;
    private List<String> mApidList = new ArrayList();
    private List<String> mColorDuidList = new ArrayList();
    private AtomicFile mConfigFile;
    private Context mContext;
    private String mCurrentGUID;
    private String mCurrentUserId = REASON_TYPE_DEFAULT;
    private Map<String, Duid> mDUIDMap;
    private String mGUID;
    private List<String> mGuidList = new ArrayList();
    private Handler mHandler;
    private boolean mHasLoadFinish = DEBUG_INTERNAL;
    private Map<String, String> mOUIDMap;
    private Map<String, Object> mOuidToggleMap;
    private final BroadcastReceiver mPackageIntentReceiver = new BroadcastReceiver() {
        /* class com.android.server.notification.OpenID.AnonymousClass1 */

        /* JADX WARNING: Code restructure failed: missing block: B:6:0x0024, code lost:
            r8 = r7.getSchemeSpecificPart();
         */
        public void onReceive(Context context, final Intent intent) {
            final String pkgName;
            final String action = intent.getAction();
            Log.d(OpenID.TAG, "action=" + action);
            if (action != null && (uri = intent.getData()) != null && pkgName != null) {
                final boolean isreplace = intent.getExtras().getBoolean("android.intent.extra.REPLACING");
                Log.d(OpenID.TAG, "pkgName:" + pkgName + ",isreplace=" + isreplace);
                OpenID.this.mHandler.post(new Runnable() {
                    /* class com.android.server.notification.OpenID.AnonymousClass1.AnonymousClass1 */

                    public void run() {
                        int uid;
                        try {
                            if ("android.intent.action.PACKAGE_REMOVED".equals(action)) {
                                if (!isreplace) {
                                    OpenID.this.clearUUID(pkgName, OpenID.DEBUG_INTERNAL);
                                }
                            } else if (!"android.intent.action.PACKAGE_ADDED".equals(action)) {
                                if (!"oppo.intent.action.MULTI_APP_PACKAGE_ADDED".equals(action)) {
                                    if ("oppo.intent.action.MULTI_APP_PACKAGE_REMOVED".equals(action)) {
                                        if (!isreplace) {
                                            OpenID.this.clearUUID(pkgName, true);
                                        }
                                    } else if ("android.intent.action.PACKAGE_DATA_CLEARED".equals(action)) {
                                        Bundle extra = intent.getExtras();
                                        if (extra != null && (uid = extra.getInt("android.intent.extra.UID", -1)) != -1) {
                                            OpenID.this.clearUUID(pkgName, OpenID.this.isMultiAppUid(pkgName, uid));
                                        } else {
                                            return;
                                        }
                                    }
                                }
                            }
                        } catch (Exception e) {
                        }
                        OpenID.this.saveConfigFile();
                    }
                });
            }
        }
    };
    private String mSignType = MD5;

    public OpenID(Context context, Handler handler) {
        this.mHandler = handler;
        this.mGUID = "";
        this.mCurrentGUID = "";
        this.mDUIDMap = new HashMap();
        this.mAPIDMap = new HashMap();
        this.mContext = context;
        this.mOUIDMap = new HashMap();
        this.mOuidToggleMap = new HashMap();
    }

    public void init(String type) {
        this.mConfigFile = new AtomicFile(new File(new File(Environment.getDataDirectory(), SYSTEM), "openid_config.xml"));
        this.mSignType = type;
        loadConfigFile();
        loadAllOpenid();
        registerPackageReceiver(this.mContext);
    }

    public void setColorDuidList(List<String> list) {
        synchronized (this.mColorDuidList) {
            this.mColorDuidList.clear();
            this.mColorDuidList.addAll(list);
        }
    }

    public void setGuidList(List<String> list) {
        synchronized (this.mGuidList) {
            this.mGuidList.clear();
            this.mGuidList.addAll(list);
        }
    }

    public void setApidList(List<String> list) {
        synchronized (this.mApidList) {
            this.mApidList.clear();
            this.mApidList.addAll(list);
        }
    }

    public void setOuidToggle(boolean toggle, int userId) {
        String uid = String.valueOf(userId);
        if (!this.mOuidToggleMap.containsKey(uid) || ((Boolean) this.mOuidToggleMap.get(uid)).booleanValue() != toggle) {
            synchronized (this.mOUIDMap) {
                if (!toggle) {
                    if (this.mOUIDMap.containsKey(uid)) {
                        this.mOUIDMap.put(uid, OpenIDUtils.generateOUID());
                        sendBroadcastForOUID(REASON_TYPE_REGENERATE);
                        saveConfigFile();
                    }
                }
            }
            if (this.mOuidToggleMap.containsKey(uid)) {
                this.mOuidToggleMap.remove(uid);
            }
            this.mOuidToggleMap.put(uid, Boolean.valueOf(toggle));
        }
    }

    public boolean checkGetGUID(String pkg, int uid) {
        synchronized (this.mGuidList) {
            if (TextUtils.isEmpty(pkg) || !this.mGuidList.contains(pkg)) {
                return DEBUG_INTERNAL;
            }
            return true;
        }
    }

    public boolean checkGetAPID(String pkg, int uid) {
        synchronized (this.mApidList) {
            if (TextUtils.isEmpty(pkg) || !this.mApidList.contains(pkg)) {
                return DEBUG_INTERNAL;
            }
            return true;
        }
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    public String getOpenid(String pkg, int uid, String type) {
        char c = 65535;
        try {
            switch (type.hashCode()) {
                case 2015626:
                    if (type.equals(TYPE_APID)) {
                        c = 4;
                        break;
                    }
                    break;
                case 2020431:
                    if (type.equals(TYPE_AUID)) {
                        c = 3;
                        break;
                    }
                    break;
                case 2109804:
                    if (type.equals(TYPE_DUID)) {
                        c = 2;
                        break;
                    }
                    break;
                case 2199177:
                    if (type.equals(TYPE_GUID)) {
                        c = 0;
                        break;
                    }
                    break;
                case 2437505:
                    if (type.equals(TYPE_OUID)) {
                        c = 1;
                        break;
                    }
                    break;
            }
            if (c == 0) {
                return getGUID();
            }
            if (c == 1) {
                this.mCurrentUserId = getCurrentUserId();
                return getOUID();
            } else if (c != 2) {
                if (c == 3) {
                    return getAUID(pkg, uid);
                }
                if (c != 4) {
                    return "";
                }
                return getAPID(pkg, uid);
            } else if (TextUtils.isEmpty(pkg)) {
                return "";
            } else {
                return getDUID(pkg, uid);
            }
        } catch (Exception e) {
            Log.d(TAG, "getOpenid--error-pkg:" + pkg + ",uid:" + uid + ",type" + type + ":" + e.getMessage());
            return "";
        }
    }

    private String getCurrentUserId() {
        try {
            return String.valueOf(ActivityManager.getCurrentUser());
        } catch (Exception e) {
            Log.d(TAG, "getCurrentUserId--error:" + e.getMessage());
            return String.valueOf(0);
        }
    }

    private void registerPackageReceiver(Context context) {
        IntentFilter pkgFilter = new IntentFilter();
        pkgFilter.addAction("android.intent.action.PACKAGE_ADDED");
        pkgFilter.addAction("android.intent.action.PACKAGE_REMOVED");
        pkgFilter.addAction("android.intent.action.PACKAGE_DATA_CLEARED");
        pkgFilter.addDataScheme(BrightnessConstants.AppSplineXml.TAG_PACKAGE);
        context.registerReceiverAsUser(this.mPackageIntentReceiver, UserHandle.ALL, pkgFilter, null, null);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void clearUUID(String pkg, boolean isMultiApp) {
        if (!TextUtils.isEmpty(pkg)) {
            synchronized (this.mDUIDMap) {
                Iterator<Map.Entry<String, Duid>> mapIterator = this.mDUIDMap.entrySet().iterator();
                while (mapIterator.hasNext()) {
                    String sign = mapIterator.next().getKey();
                    Duid duid = this.mDUIDMap.get(sign);
                    if (duid != null && !TextUtils.isEmpty(duid.getValue())) {
                        Iterator<Auid> iterator = duid.getAuidList().iterator();
                        while (iterator.hasNext()) {
                            Auid auid = iterator.next();
                            if (isMultiApp) {
                                int uid = Integer.parseInt(auid.getUid());
                                if (TextUtils.equals(pkg, auid.getPackageName()) && isMultiAppUid(pkg, uid)) {
                                    iterator.remove();
                                }
                            } else if (TextUtils.equals(pkg, auid.getPackageName())) {
                                iterator.remove();
                            }
                        }
                        if (!TextUtils.equals(COLOR_DUID_SIGN, sign) && !hasSameSignApp(sign)) {
                            mapIterator.remove();
                        }
                    }
                }
            }
            saveConfigFile();
        }
    }

    private void loadAllOpenid() {
        initHeyTapID();
    }

    private boolean hasSameSignApp(String sign) {
        for (ApplicationInfo applicationInfo : this.mContext.getPackageManager().getInstalledApplications(8192)) {
            if (sign.equals(getSingInfo(this.mContext, applicationInfo.packageName, this.mSignType))) {
                return true;
            }
        }
        return DEBUG_INTERNAL;
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    public void clearOpenid(String pkg, int uid, String type) {
        char c;
        switch (type.hashCode()) {
            case 2109804:
                if (type.equals(TYPE_DUID)) {
                    c = 2;
                    break;
                }
                c = 65535;
                break;
            case 2199177:
                if (type.equals(TYPE_GUID)) {
                    c = 0;
                    break;
                }
                c = 65535;
                break;
            case 2437505:
                if (type.equals(TYPE_OUID)) {
                    c = 1;
                    break;
                }
                c = 65535;
                break;
            case 3005519:
                if (type.equals(TAG_AUID)) {
                    c = 3;
                    break;
                }
                c = 65535;
                break;
            default:
                c = 65535;
                break;
        }
        if (c == 0) {
            synchronized (this.mAPIDMap) {
                this.mGUID = "";
            }
        } else if (c == 1) {
            this.mCurrentUserId = getCurrentUserId();
            synchronized (this.mOUIDMap) {
                if (this.mOUIDMap.containsKey(this.mCurrentUserId)) {
                    this.mOUIDMap.remove(this.mCurrentUserId);
                }
            }
        } else if (c == 2) {
            String sign = getSingInfo(this.mContext, pkg, this.mSignType);
            synchronized (this.mDUIDMap) {
                if (this.mDUIDMap.containsKey(sign)) {
                    this.mDUIDMap.remove(sign);
                }
            }
        } else if (c == 3) {
            clearUUID(pkg, isMultiAppUid(pkg, uid));
        }
        saveConfigFile();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private String getGUID() {
        if (TextUtils.isEmpty(this.mGUID) || !TextUtils.equals(this.mGUID, this.mCurrentGUID)) {
            synchronized (this.mAPIDMap) {
                if (TextUtils.isEmpty(this.mCurrentGUID)) {
                    this.mCurrentGUID = OpenIDUtils.generateGUID();
                    if (TextUtils.isEmpty(this.mCurrentGUID)) {
                        return this.mGUID;
                    }
                }
                String reasonType = REASON_TYPE_DEFAULT;
                if (TextUtils.isEmpty(this.mGUID)) {
                    reasonType = REASON_TYPE_INIT;
                } else if (!TextUtils.equals(this.mGUID, this.mCurrentGUID)) {
                    reasonType = REASON_TYPE_REGENERATE;
                }
                if (!TextUtils.equals(REASON_TYPE_DEFAULT, reasonType)) {
                    this.mGUID = this.mCurrentGUID;
                    sendBroadcastForGUID(reasonType);
                    this.mAPIDMap.clear();
                    saveConfigFile();
                }
            }
        }
        return this.mGUID;
    }

    private String getOUID() {
        synchronized (this.mOUIDMap) {
            if (!this.mOUIDMap.containsKey(this.mCurrentUserId) || TextUtils.isEmpty(this.mOUIDMap.get(this.mCurrentUserId))) {
                String ouid = OpenIDUtils.generateOUID();
                if (!TextUtils.isEmpty(ouid)) {
                    this.mOUIDMap.put(this.mCurrentUserId, ouid);
                    sendBroadcastForOUID(REASON_TYPE_INIT);
                    saveConfigFile();
                }
                return ouid;
            }
            return this.mOUIDMap.get(this.mCurrentUserId);
        }
    }

    private void initHeyTapID() {
        final Intent intent = new Intent(HEYTAP_ID_BROADCAST_ACTION);
        boolean reason = DEBUG_INTERNAL;
        synchronized (this.mAPIDMap) {
            if (TextUtils.isEmpty(this.mCurrentGUID)) {
                this.mCurrentGUID = OpenIDUtils.generateGUID();
            }
            if ((TextUtils.isEmpty(this.mGUID) || !TextUtils.equals(this.mGUID, this.mCurrentGUID)) && !TextUtils.isEmpty(this.mCurrentGUID)) {
                if (TextUtils.isEmpty(this.mGUID)) {
                    intent.putExtra(TYPE_GUID, REASON_TYPE_INIT);
                } else {
                    intent.putExtra(TYPE_GUID, REASON_TYPE_REGENERATE);
                }
                reason = true;
                this.mGUID = this.mCurrentGUID;
                this.mAPIDMap.clear();
            }
        }
        if (TextUtils.isEmpty(this.mGUID)) {
            this.mHandler.postDelayed(new Runnable() {
                /* class com.android.server.notification.OpenID.AnonymousClass2 */

                public void run() {
                    OpenID.this.getGUID();
                }
            }, 500);
            Log.e(TAG, "guid is null-----delay 500ms to init");
        }
        synchronized (this.mOUIDMap) {
            if ((!this.mOUIDMap.containsKey(this.mCurrentUserId) || TextUtils.isEmpty(this.mOUIDMap.get(this.mCurrentUserId))) && !TextUtils.isEmpty(this.mCurrentGUID)) {
                intent.putExtra(TYPE_OUID, REASON_TYPE_INIT);
                reason = true;
                this.mOUIDMap.put(this.mCurrentUserId, OpenIDUtils.generateOUID());
                saveConfigFile();
            }
        }
        if (reason && this.mContext != null) {
            new Handler().postDelayed(new Runnable() {
                /* class com.android.server.notification.OpenID.AnonymousClass3 */

                public void run() {
                    for (String pkg : OpenID.HeytapID_BROADCAST_RECEIVER_LIST) {
                        intent.setPackage(pkg);
                        if (OpenID.this.mContext != null) {
                            OpenID.this.mContext.sendBroadcast(intent);
                        }
                    }
                }
            }, 10000);
        }
    }

    private void sendBroadcastForGUID(String reason) {
        if (this.mContext != null) {
            Intent intent = new Intent(HEYTAP_ID_BROADCAST_ACTION);
            intent.putExtra(TYPE_GUID, reason);
            for (String pkg : HeytapID_BROADCAST_RECEIVER_LIST) {
                intent.setPackage(pkg);
                this.mContext.sendBroadcast(intent);
            }
        }
    }

    private void sendBroadcastForOUID(String reason) {
        if (this.mContext != null) {
            Intent intent = new Intent(HEYTAP_ID_BROADCAST_ACTION);
            intent.putExtra(TYPE_OUID, reason);
            for (String pkg : HeytapID_BROADCAST_RECEIVER_LIST) {
                intent.setPackage(pkg);
                this.mContext.sendBroadcast(intent);
            }
        }
    }

    private void setDUID(String sign, String duid) {
        if (!TextUtils.isEmpty(sign)) {
            synchronized (this.mDUIDMap) {
                this.mDUIDMap.put(sign, new Duid(sign, duid));
            }
            saveConfigFile();
        }
    }

    private String getDUID(String pkg, int uid) {
        String sign = getSingInfo(this.mContext, pkg, this.mSignType, uid);
        if (TextUtils.isEmpty(sign)) {
            return "";
        }
        synchronized (this.mDUIDMap) {
            if (!this.mDUIDMap.containsKey(sign) || TextUtils.isEmpty(this.mDUIDMap.get(sign).getValue())) {
                String duid = OpenIDUtils.generateAUID(pkg);
                setDUID(sign, duid);
                return duid;
            }
            return this.mDUIDMap.get(sign).getValue();
        }
    }

    private void setAUID(String pkg, int uid, String value, String sign) {
        String sUid = String.valueOf(uid);
        if (!(TextUtils.isEmpty(pkg) || TextUtils.isEmpty(sUid) || value == null || TextUtils.isEmpty(sign))) {
            synchronized (this.mDUIDMap) {
                Duid duid = this.mDUIDMap.get(sign);
                if (duid != null && !TextUtils.isEmpty(duid.getValue())) {
                    List<Auid> auidList = duid.getAuidList();
                    for (Auid auid : auidList) {
                        if (pkg.equals(auid.getPackageName()) && sUid.equals(auid.getUid())) {
                            auid.setValue(value);
                            return;
                        }
                    }
                    auidList.add(new Auid(pkg, sUid, value));
                }
                saveConfigFile();
            }
        }
    }

    private String getAUID(String pkg, int uid) {
        if (TextUtils.isEmpty(pkg)) {
            return "";
        }
        String sign = getSingInfo(this.mContext, pkg, this.mSignType, uid);
        if (TextUtils.isEmpty(sign) || TextUtils.isEmpty(getDUID(pkg, uid))) {
            return "";
        }
        synchronized (this.mDUIDMap) {
            for (Auid auid : this.mDUIDMap.get(sign).getAuidList()) {
                if (TextUtils.equals(auid.getPackageName(), pkg) && TextUtils.equals(auid.getUid(), String.valueOf(uid)) && !TextUtils.isEmpty(auid.getValue())) {
                    return auid.getValue();
                }
            }
            String auid2 = OpenIDUtils.generateAUID(pkg);
            setAUID(pkg, uid, auid2, sign);
            return auid2;
        }
    }

    private String getAPID(String pkg, int uid) {
        if (TextUtils.isEmpty(pkg)) {
            return "";
        }
        String key = getMultiAppPkgUidKey(pkg, uid);
        if (TextUtils.isEmpty(key)) {
            return "";
        }
        synchronized (this.mAPIDMap) {
            String apid = this.mAPIDMap.get(key);
            if (TextUtils.isEmpty(apid)) {
                String guid = getGUID();
                if (TextUtils.isEmpty(guid)) {
                    return "";
                }
                apid = OpenIDUtils.generateAPID(key, guid);
                setAPID(key, apid);
            }
            return apid;
        }
    }

    private void setAPID(String apidKey, String value) {
        if (!TextUtils.isEmpty(apidKey) && value != null) {
            synchronized (this.mAPIDMap) {
                this.mAPIDMap.put(apidKey, value);
            }
            saveConfigFile();
        }
    }

    private String getMultiAppPkgUidKey(String pkg, int uid) {
        if (!isMultiAppUid(pkg, uid)) {
            return pkg;
        }
        return MUTIL_APP_UID + pkg;
    }

    private void loadConfigFile() {
        synchronized (this.mConfigFile) {
            InputStream infile = null;
            try {
                infile = this.mConfigFile.openRead();
                loadConfigXml(infile);
                IoUtils.closeQuietly(infile);
            } catch (FileNotFoundException e) {
                Log.wtf(TAG, "Unable to parse openid config-FileNotFoundException", e);
                IoUtils.closeQuietly(infile);
            } catch (IOException e2) {
                Log.wtf(TAG, "Unable to parse openid config-IOException", e2);
                IoUtils.closeQuietly(infile);
            } catch (NumberFormatException e3) {
                Log.wtf(TAG, "Unable to parse openid config-NumberFormatException", e3);
                IoUtils.closeQuietly(infile);
            } catch (XmlPullParserException e4) {
                Log.wtf(TAG, "Unable to parse openid config-XmlPullParserException", e4);
                IoUtils.closeQuietly(infile);
            } catch (Exception e5) {
                Log.wtf(TAG, "Unable to parse openid config-Exception", e5);
                IoUtils.closeQuietly(infile);
            } catch (Throwable th) {
                IoUtils.closeQuietly(infile);
                this.mHasLoadFinish = true;
                throw th;
            }
            this.mHasLoadFinish = true;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void saveConfigFile() {
        if (this.mHasLoadFinish) {
            this.mHandler.removeMessages(1);
            this.mHandler.sendEmptyMessage(1);
        }
    }

    private void loadConfigXml(InputStream stream) throws XmlPullParserException, NumberFormatException, IOException {
        XmlPullParser parser = Xml.newPullParser();
        parser.setInput(stream, StandardCharsets.UTF_8.name());
        for (int eventType = parser.getEventType(); eventType != 1; eventType = parser.next()) {
            if (eventType == 2 && TAG_GUID.equals(parser.getName())) {
                this.mGUID = parser.getAttributeValue(null, ATTR_VALUE);
            } else if (eventType == 2 && TAG_OUID.equals(parser.getName())) {
                String userId = parser.getAttributeValue(null, "name");
                String ouid = parser.getAttributeValue(null, ATTR_VALUE);
                synchronized (this.mOUIDMap) {
                    this.mOUIDMap.put(userId, ouid);
                }
            } else if (eventType == 2 && TAG_DUID.equals(parser.getName())) {
                String sign = parser.getAttributeValue(null, ATTR_SIGN);
                String value = parser.getAttributeValue(null, ATTR_VALUE);
                synchronized (this.mDUIDMap) {
                    this.mDUIDMap.put(sign, new Duid(sign, value));
                }
            } else if (eventType == 2 && TAG_AUID.equals(parser.getName())) {
                String pkg = parser.getAttributeValue(null, "name");
                String uid = parser.getAttributeValue(null, "uid");
                String value2 = parser.getAttributeValue(null, ATTR_VALUE);
                String sign2 = getSingInfo(this.mContext, pkg, this.mSignType);
                synchronized (this.mDUIDMap) {
                    Duid duid = this.mDUIDMap.get(sign2);
                    if (duid != null && !TextUtils.isEmpty(duid.getValue())) {
                        duid.getAuidList().add(new Auid(pkg, uid, value2));
                    }
                }
            } else if (eventType == 2 && TAG_APID.equals(parser.getName())) {
                String key = parser.getAttributeValue(null, "name");
                String value3 = parser.getAttributeValue(null, ATTR_VALUE);
                synchronized (this.mAPIDMap) {
                    this.mAPIDMap.put(key, value3);
                }
            }
        }
    }

    public void handleSaveConfigFile() {
        synchronized (this.mConfigFile) {
            try {
                FileOutputStream stream = this.mConfigFile.startWrite();
                try {
                    writeConfigXml(stream);
                    this.mConfigFile.finishWrite(stream);
                } catch (IOException e) {
                    Log.w(TAG, "Failed to save config file, restoring backup", e);
                    this.mConfigFile.failWrite(stream);
                } catch (Exception e2) {
                    Log.w(TAG, "Failed to save config file,", e2);
                } catch (Throwable th) {
                    throw th;
                }
            } catch (IOException e3) {
                Log.w(TAG, "Failed to save config file", e3);
            }
        }
    }

    private void writeConfigXml(OutputStream stream) throws IOException {
        XmlSerializer out = new FastXmlSerializer();
        out.setOutput(stream, StandardCharsets.UTF_8.name());
        out.startDocument(null, true);
        out.startTag(null, TAG_OPENID_CONFIG);
        out.attribute(null, "version", Integer.toString(1));
        out.startTag(null, TAG_GUID);
        out.attribute(null, "name", SYSTEM);
        out.attribute(null, ATTR_VALUE, this.mGUID);
        out.endTag(null, TAG_GUID);
        synchronized (this.mOUIDMap) {
            for (String key : this.mOUIDMap.keySet()) {
                String value = this.mOUIDMap.get(key);
                if (!TextUtils.isEmpty(key)) {
                    out.startTag(null, TAG_OUID);
                    out.attribute(null, "name", key);
                    out.attribute(null, ATTR_VALUE, value);
                    out.endTag(null, TAG_OUID);
                }
            }
        }
        synchronized (this.mDUIDMap) {
            for (String key2 : this.mDUIDMap.keySet()) {
                Duid duid = this.mDUIDMap.get(key2);
                if (duid != null && !TextUtils.isEmpty(duid.getValue())) {
                    out.startTag(null, TAG_DUID);
                    out.attribute(null, ATTR_SIGN, duid.getSign());
                    out.attribute(null, ATTR_VALUE, duid.getValue());
                    List<Auid> auidList = duid.getAuidList();
                    if (auidList != null && !auidList.isEmpty()) {
                        for (Auid auid : auidList) {
                            out.startTag(null, TAG_AUID);
                            out.attribute(null, "name", auid.getPackageName());
                            out.attribute(null, "uid", auid.getUid());
                            out.attribute(null, ATTR_VALUE, auid.getValue());
                            out.endTag(null, TAG_AUID);
                        }
                    }
                    out.endTag(null, TAG_DUID);
                }
            }
        }
        synchronized (this.mAPIDMap) {
            for (String key3 : this.mAPIDMap.keySet()) {
                String value2 = this.mAPIDMap.get(key3);
                if (!TextUtils.isEmpty(key3)) {
                    out.startTag(null, TAG_APID);
                    out.attribute(null, "name", key3);
                    out.attribute(null, ATTR_VALUE, value2);
                    out.endTag(null, TAG_APID);
                }
            }
        }
        out.endTag(null, TAG_OPENID_CONFIG);
        out.endDocument();
    }

    /* JADX WARNING: Code restructure failed: missing block: B:12:0x001c, code lost:
        r0 = getSignatures(r7, r8);
        r1 = new java.lang.StringBuilder();
        r2 = r0.length;
        r3 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0027, code lost:
        if (r3 >= r2) goto L_0x0044;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0029, code lost:
        r4 = r0[r3];
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0033, code lost:
        if (android.text.TextUtils.isEmpty(r1.toString()) != false) goto L_0x003a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0035, code lost:
        r1.append(com.android.server.notification.OpenID.SPLITER);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x003a, code lost:
        r1.append(getSignatureString(r4, r9));
        r3 = r3 + 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0048, code lost:
        return r1.toString();
     */
    private String getSingInfo(Context context, String packageName, String type) {
        if (TextUtils.isEmpty(packageName)) {
            return "";
        }
        synchronized (this.mColorDuidList) {
            if (!this.mColorDuidList.contains(packageName)) {
                if (isSystemPkg(context, packageName)) {
                }
            }
            return COLOR_DUID_SIGN;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:12:0x001c, code lost:
        r0 = android.os.UserHandle.getUserId(r11);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0020, code lost:
        if (r0 <= 0) goto L_0x0028;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0026, code lost:
        if (isMultiAppUid(r9, r11) == false) goto L_0x0029;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0028, code lost:
        r0 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0029, code lost:
        r1 = getSignatures(r8, r9, r0);
        r2 = new java.lang.StringBuilder();
        r3 = r1.length;
        r4 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0034, code lost:
        if (r4 >= r3) goto L_0x0051;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0036, code lost:
        r5 = r1[r4];
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0040, code lost:
        if (android.text.TextUtils.isEmpty(r2.toString()) != false) goto L_0x0047;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0042, code lost:
        r2.append(com.android.server.notification.OpenID.SPLITER);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0047, code lost:
        r2.append(getSignatureString(r5, r10));
        r4 = r4 + 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0055, code lost:
        return r2.toString();
     */
    private String getSingInfo(Context context, String packageName, String type, int uid) {
        if (TextUtils.isEmpty(packageName)) {
            return "";
        }
        synchronized (this.mColorDuidList) {
            if (!this.mColorDuidList.contains(packageName)) {
                if (isSystemPkg(context, packageName)) {
                }
            }
            return COLOR_DUID_SIGN;
        }
    }

    private boolean isSystemPkg(Context context, String pkg) {
        try {
            ApplicationInfo ai = context.getPackageManager().getApplicationInfo(pkg, 0);
            if (ai == null || (ai.flags & 1) == 0) {
                return DEBUG_INTERNAL;
            }
            return true;
        } catch (PackageManager.NameNotFoundException nfe) {
            Log.d(TAG, "isSystemPkg--pkg:" + pkg + ",nfe:" + nfe.toString());
        }
    }

    private Signature[] getSignatures(Context context, String packageName) {
        try {
            return context.getPackageManager().getPackageInfo(packageName, 64).signatures;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    private Signature[] getSignatures(Context context, String packageName, int userId) {
        try {
            return context.getPackageManager().getPackageInfoAsUser(packageName, 64, userId).signatures;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String getSignatureString(Signature sig, String type) {
        byte[] hexBytes = sig.toByteArray();
        try {
            MessageDigest digest = MessageDigest.getInstance(type);
            if (digest == null) {
                return null;
            }
            byte[] digestBytes = digest.digest(hexBytes);
            StringBuilder sb = new StringBuilder();
            for (byte digestByte : digestBytes) {
                sb.append(Integer.toHexString((digestByte & 255) | 256).substring(1, 3));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isMultiAppUid(String pkg, int uid) {
        return OppoFeatureCache.get(IColorMultiAppManager.DEFAULT).isMultiApp(UserHandle.getUserId(uid), pkg);
    }

    public boolean dumpOpenidInfo(PrintWriter pw, String[] args) {
        if (args.length != 1 || !"openid".equals(args[0])) {
            return DEBUG_INTERNAL;
        }
        synchronized (this.mDUIDMap) {
            try {
                OpenIDUtils.generateGUID();
                pw.println("mGuidList:" + this.mGuidList);
                pw.println("mColorDuidList:" + this.mColorDuidList);
                pw.println("mDUIDMap:" + this.mDUIDMap);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    /* access modifiers changed from: private */
    public class Duid {
        private List<Auid> mAuidList;
        private String mSign;
        private String mValue;

        public Duid(String sign, String value) {
            this.mSign = sign;
            this.mValue = value;
        }

        public String getSign() {
            return this.mSign;
        }

        public void setSign(String mSign2) {
            this.mSign = mSign2;
        }

        public String getValue() {
            return this.mValue;
        }

        public void setValue(String mValue2) {
            this.mValue = mValue2;
        }

        public List<Auid> getAuidList() {
            if (this.mAuidList == null) {
                this.mAuidList = new ArrayList();
            }
            return this.mAuidList;
        }

        public void setAuidList(List<Auid> mAuidList2) {
            this.mAuidList = mAuidList2;
        }

        public String toString() {
            return "Duid--sign:" + this.mSign + ",value:" + this.mValue + ",auidList:" + this.mAuidList;
        }
    }

    /* access modifiers changed from: private */
    public class Auid {
        private String mPackageName;
        private String mUid;
        private String mValue;

        public Auid(String pkg, String uid, String value) {
            this.mPackageName = pkg;
            this.mUid = uid;
            this.mValue = value;
        }

        public String getPackageName() {
            return this.mPackageName;
        }

        public void setPackageName(String mPackageName2) {
            this.mPackageName = mPackageName2;
        }

        public String getUid() {
            return this.mUid;
        }

        public void setUid(String mUid2) {
            this.mUid = mUid2;
        }

        public String getValue() {
            return this.mValue;
        }

        public void setValue(String mValue2) {
            this.mValue = mValue2;
        }

        public String toString() {
            return "Auid--pkg:" + this.mPackageName + ",uid:" + this.mUid + ",value:" + this.mValue;
        }
    }
}
