package com.android.server.secrecy.work;

import android.content.pm.ActivityInfo;
import android.util.Xml;
import com.android.internal.util.XmlUtils;
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

/*  JADX ERROR: NullPointerException in pass: ReSugarCode
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
    	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
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
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class ActivityConfig {
    public static boolean DEBUG;
    private static final Object mLock = null;
    private static ActivityConfig sInstance;
    private final String ACTIVITY_CONFIG_FILE;
    private final String ATTR_CLASSNAME;
    private final String ATTR_PACKAGE;
    private final String START_TAG;
    private final String TAG;
    private final Object TAG_ACTIVITY;
    private final ArrayList<ActivityItem> mActivityItems;

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

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.secrecy.work.ActivityConfig.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 9 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.secrecy.work.ActivityConfig.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.secrecy.work.ActivityConfig.<clinit>():void");
    }

    public ActivityConfig() {
        this.TAG = "SecrecyService.ActivityConfig";
        this.ACTIVITY_CONFIG_FILE = "system/etc/activity_config.xml";
        this.START_TAG = "activityconfig";
        this.TAG_ACTIVITY = "activity";
        this.ATTR_PACKAGE = "packageName";
        this.ATTR_CLASSNAME = "className";
        this.mActivityItems = new ArrayList();
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
        if (configFile != null && configFile.exists() && configFile.canRead()) {
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
