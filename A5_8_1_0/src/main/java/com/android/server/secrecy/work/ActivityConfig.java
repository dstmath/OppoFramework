package com.android.server.secrecy.work;

import android.content.pm.ActivityInfo;
import android.util.Xml;
import com.android.internal.util.XmlUtils;
import com.android.server.am.OppoAppStartupManager;
import com.android.server.secrecy.SecrecyService;
import com.android.server.secrecy.policy.util.LogUtil;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import libcore.io.IoUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class ActivityConfig {
    public static boolean DEBUG = SecrecyService.DEBUG;
    private static final Object mLock = new Object();
    private static ActivityConfig sInstance;
    private final String ACTIVITY_CONFIG_FILE = "system/etc/activity_config.xml";
    private final String ATTR_CLASSNAME = "className";
    private final String ATTR_PACKAGE = "packageName";
    private final String START_TAG = "activityconfig";
    private final String TAG = "SecrecyService.ActivityConfig";
    private final Object TAG_ACTIVITY = OppoAppStartupManager.TYPE_ACTIVITY;
    private final ArrayList<ActivityItem> mActivityItems = new ArrayList();

    private class ActivityItem {
        private final String mClassName;
        private final String mPackageName;

        public ActivityItem(String packageName, String className) {
            this.mPackageName = packageName;
            this.mClassName = className;
        }

        public String getPackageName() {
            return this.mPackageName;
        }

        public String getClassName() {
            return this.mClassName;
        }
    }

    public static ActivityConfig getInstance() {
        ActivityConfig activityConfig;
        synchronized (ActivityConfig.class) {
            if (sInstance == null) {
                sInstance = new ActivityConfig();
            }
            activityConfig = sInstance;
        }
        return activityConfig;
    }

    public void readConfigs() {
        File configFile = new File("system/etc/activity_config.xml");
        if (configFile != null && (configFile.exists() ^ 1) == 0 && (configFile.canRead() ^ 1) == 0) {
            readConfigFromXml(configFile);
        } else {
            LogUtil.e("SecrecyService.ActivityConfig", " error hanppend when readConfigs from system/etc/activity_config.xml, configFile = " + configFile);
        }
    }

    private void readConfigFromXml(File configFile) {
        try {
            FileReader permReader = new FileReader(configFile);
            try {
                int type;
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(permReader);
                while (true) {
                    type = parser.next();
                    if (type != 2) {
                        if (type == 1) {
                            break;
                        }
                    }
                    break;
                }
                if (type != 2) {
                    throw new XmlPullParserException("No start tag found");
                } else if (parser.getName().equals("activityconfig")) {
                    loop1:
                    while (true) {
                        XmlUtils.nextElement(parser);
                        if (parser.getEventType() == 1) {
                            break loop1;
                        }
                        if (this.TAG_ACTIVITY.equals(parser.getName())) {
                            this.mActivityItems.add(new ActivityItem(parser.getAttributeValue(null, "packageName"), parser.getAttributeValue(null, "className")));
                            XmlUtils.skipCurrentTag(parser);
                        } else {
                            XmlUtils.skipCurrentTag(parser);
                        }
                    }
                } else {
                    throw new XmlPullParserException("Unexpected start tag in : found " + parser.getName() + ", expected 'permissions' or 'config'");
                }
            } catch (XmlPullParserException e) {
                e.printStackTrace();
            } catch (IOException e2) {
                e2.printStackTrace();
            } finally {
                IoUtils.closeQuietly(permReader);
            }
        } catch (FileNotFoundException e3) {
            e3.printStackTrace();
        }
    }

    public boolean isInActivityConfig(String packageName, String className) {
        if (packageName == null || className == null) {
            return false;
        }
        synchronized (mLock) {
            for (int index = 0; index < this.mActivityItems.size(); index++) {
                ActivityItem activityItem = (ActivityItem) this.mActivityItems.get(index);
                if (packageName.equals(activityItem.getPackageName()) && className.equals(activityItem.getClassName())) {
                    return true;
                }
            }
            return false;
        }
    }

    public boolean isInActivityConfig(ActivityInfo info) {
        if (info == null) {
            return false;
        }
        return isInActivityConfig(info.packageName, info.name);
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String prefix) {
        synchronized (mLock) {
            pw.println("mActivityItems.size = " + this.mActivityItems.size());
            for (ActivityItem activityItem : this.mActivityItems) {
                pw.println(prefix + activityItem.mPackageName + ", " + activityItem.mClassName);
            }
        }
    }

    public void parseActivityInfo(XmlPullParser parser) {
        String packageName = parser.getAttributeValue(null, "packageName");
        String className = parser.getAttributeValue(null, "className");
        ActivityItem activiytItem = new ActivityItem(packageName, className);
        synchronized (mLock) {
            this.mActivityItems.add(activiytItem);
        }
        LogUtil.d("SecrecyService.ActivityConfig", "parseActivityInfo, packageName = " + packageName + ", className = " + className);
    }

    public void clearActivityInfo() {
        synchronized (mLock) {
            this.mActivityItems.clear();
            LogUtil.d("SecrecyService.ActivityConfig", "clearActivityInfo");
        }
    }
}
