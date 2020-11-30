package com.android.server.oppo;

import android.app.IOppoArmyManager;
import android.content.Context;
import android.os.Environment;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.util.Slog;
import android.util.Xml;
import com.android.internal.util.FastXmlSerializer;
import com.android.server.pm.Settings;
import com.android.server.theia.NoFocusWindow;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlSerializer;

public class OppoArmyService extends IOppoArmyManager.Stub {
    private static final boolean DBG_CUSTOMIZE = false;
    private static final boolean DBG_LIST_PARSE = false;
    private static final String FEATURE_DISALLOW_APP_RUN_STR = "oppo.customize.function.disallow_app_run";
    private static final String SERVICE_NAME = "oppo_army";
    private static final String TAG = "OppoArmyService";
    private Context mContext;
    private List<String> mDisallowedRuningAppList = new ArrayList();
    private final Object mDisallowedRuningAppListLock = new Object();
    private final File mNotAllowAppFilename = new File(this.mSystemDir, "not_allow_packages.xml");
    private final File mSystemDir = new File(Environment.getDataDirectory(), "system");

    public void publish(Context context) {
        this.mContext = context;
        ServiceManager.addService(SERVICE_NAME, asBinder());
    }

    public void systemReady() {
        readDisallowAppListFile();
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
        String value = NoFocusWindow.HUNG_CONFIG_ENABLE;
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
                    serializer.startTag(null, Settings.ATTR_PACKAGE);
                    serializer.attribute(null, Settings.ATTR_NAME, packages.get(index));
                    serializer.endTag(null, Settings.ATTR_PACKAGE);
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
                if (type == 2 && Settings.ATTR_PACKAGE.equals(parser.getName()) && (name = parser.getAttributeValue(null, Settings.ATTR_NAME)) != null && !this.mDisallowedRuningAppList.contains(name)) {
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
}
