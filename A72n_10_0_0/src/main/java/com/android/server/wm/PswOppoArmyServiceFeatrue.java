package com.android.server.wm;

import android.content.Context;
import android.os.Environment;
import android.os.SystemProperties;
import android.util.Slog;
import android.util.Xml;
import com.android.internal.util.FastXmlSerializer;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlSerializer;

public class PswOppoArmyServiceFeatrue implements IPswOppoArmyServiceFeatrue {
    private static final String CUSTOMIZE_LIST_PATH = "/system/etc/oppo_customize_whitelist.xml";
    private static final boolean DBG_CUSTOMIZE = false;
    private static final boolean DBG_LIST_PARSE = false;
    private static final String FEATURE_DISALLOW_APP_RUN_STR = "oppo.customize.function.disallow_app_run";
    private static final String PROTECT_APP_LIST_PATH = "/data/system/custom_protect_app.xml";
    private static final String TAG = "PswOppoArmyServiceFeatrue";
    private static PswOppoArmyServiceFeatrue mInstance = null;
    private static final Object mLock = new Object();
    private Context mContext;
    private List<String> mCustomizeList = new ArrayList();
    private List<String> mDisallowedRuningAppList = new ArrayList();
    private final Object mDisallowedRuningAppListLock = new Object();
    private final File mNotAllowAppFilename = new File(this.mSystemDir, "not_allow_packages.xml");
    private OppoArmyService mOppoArmyService;
    private List<String> mProtectAppList = new ArrayList();
    private final File mSystemDir = new File(Environment.getDataDirectory(), "system");
    private Context mUiContext;

    private PswOppoArmyServiceFeatrue() {
    }

    public static PswOppoArmyServiceFeatrue getInstance() {
        PswOppoArmyServiceFeatrue pswOppoArmyServiceFeatrue;
        synchronized (mLock) {
            if (mInstance == null) {
                mInstance = new PswOppoArmyServiceFeatrue();
            }
            pswOppoArmyServiceFeatrue = mInstance;
        }
        return pswOppoArmyServiceFeatrue;
    }

    public void init(Context context) {
        this.mContext = context;
    }

    public void systemReady() {
        readDisallowAppListFile();
    }

    private boolean readDisallowAppListFile() {
        int type;
        String name;
        try {
            if (!this.mNotAllowAppFilename.exists()) {
                return false;
            }
            FileInputStream str = new FileInputStream(this.mNotAllowAppFilename);
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(str, null);
            do {
                type = parser.next();
                if (type == 2 && "package".equals(parser.getName()) && (name = parser.getAttributeValue(null, "name")) != null && !this.mDisallowedRuningAppList.contains(name)) {
                    this.mDisallowedRuningAppList.add(name);
                }
            } while (type != 1);
            return true;
        } catch (IOException e) {
            Slog.w(TAG, "Error reading not allow running package", e);
            return false;
        } catch (Exception e2) {
            Slog.w(TAG, "Error reading not allow running package", e2);
            return false;
        }
    }

    public boolean addDisallowedRunningApp(List<String> appPkgNamesList) {
        boolean result;
        if (!this.mContext.getPackageManager().hasSystemFeature(FEATURE_DISALLOW_APP_RUN_STR) || appPkgNamesList == null || appPkgNamesList.size() <= 0) {
            return false;
        }
        synchronized (this.mDisallowedRuningAppListLock) {
            int size = appPkgNamesList.size();
            for (int index = 0; index < size; index++) {
                String pkgName = appPkgNamesList.get(index);
                if (pkgName != null) {
                    if (pkgName.length() > 0) {
                        if (!this.mDisallowedRuningAppList.contains(pkgName)) {
                            this.mDisallowedRuningAppList.add(pkgName);
                        }
                    }
                }
            }
            result = saveDisallowAppListFile(this.mDisallowedRuningAppList);
        }
        return result;
    }

    public boolean removeDisallowedRunningApp(List<String> appPkgNamesList) {
        if (!this.mContext.getPackageManager().hasSystemFeature(FEATURE_DISALLOW_APP_RUN_STR) || appPkgNamesList == null || appPkgNamesList.size() <= 0) {
            return false;
        }
        synchronized (this.mDisallowedRuningAppListLock) {
            int size = appPkgNamesList.size();
            for (int index = 0; index < size; index++) {
                String pkgName = appPkgNamesList.get(index);
                if (pkgName != null) {
                    if (pkgName.length() > 0) {
                        if (this.mDisallowedRuningAppList.contains(pkgName)) {
                            this.mDisallowedRuningAppList.remove(pkgName);
                        }
                    }
                }
            }
            saveDisallowAppListFile(this.mDisallowedRuningAppList);
        }
        return true;
    }

    public List<String> getDisallowedRunningApp() {
        return new ArrayList(this.mDisallowedRuningAppList);
    }

    public void allowToUseSdcard(boolean allow) {
        String value = "1";
        if (allow != SystemProperties.get("persist.sys.exStorage_support", value).equals(value)) {
            if (!allow) {
                value = "0";
            }
            SystemProperties.set("persist.sys.exStorage_support", value);
        }
    }

    public boolean isRunningDisallowed(String pkgName) {
        if (pkgName == null || pkgName.length() <= 0) {
            return false;
        }
        return this.mDisallowedRuningAppList.contains(pkgName);
    }

    private boolean saveDisallowAppListFile(List<String> packages) {
        boolean z;
        IOException e;
        Exception e2;
        try {
            try {
                FileOutputStream fstr = new FileOutputStream(this.mNotAllowAppFilename);
                BufferedOutputStream str = new BufferedOutputStream(fstr);
                XmlSerializer serializer = new FastXmlSerializer();
                serializer.setOutput(str, "utf-8");
                serializer.startDocument(null, true);
                serializer.startTag(null, "packages");
                int size = packages.size();
                int index = 0;
                while (index < size) {
                    serializer.startTag(null, "package");
                    serializer.attribute(null, "name", packages.get(index));
                    serializer.endTag(null, "package");
                    index++;
                    fstr = fstr;
                }
                serializer.endTag(null, "packages");
                serializer.endDocument();
                str.flush();
                str.close();
                return true;
            } catch (IOException e3) {
                e = e3;
                z = false;
                Slog.w(TAG, "Unable to write not allow running package, current changes will be lost at reboot", e);
                return z;
            } catch (Exception e4) {
                e2 = e4;
                Slog.w(TAG, "Unable to write not allow running package, current changes will be lost at reboot", e2);
                return false;
            }
        } catch (IOException e5) {
            e = e5;
            z = false;
            Slog.w(TAG, "Unable to write not allow running package, current changes will be lost at reboot", e);
            return z;
        } catch (Exception e6) {
            e2 = e6;
            Slog.w(TAG, "Unable to write not allow running package, current changes will be lost at reboot", e2);
            return false;
        }
    }
}
