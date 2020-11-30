package com.oppo.theme;

import android.content.pm.ApplicationInfo;
import android.os.SystemProperties;
import android.util.Log;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class OppoAppIconInfo {
    public static final String ALL_APPS = "allApps.xml";
    private static boolean DEBUG = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    private static final String TAG = "OppoAppIconInfo";
    private static ArrayList<String> mAllIconNames = new ArrayList<>();
    private static ArrayList<String> mAllPackageNames = new ArrayList<>();
    private static String mCurrentTag = null;
    private static Map<String, String> mDiffPackages = new HashMap();
    private static boolean sParsered = false;

    static {
        mDiffPackages.put("ic_launcher_stk.png", "com.android.stk");
        mDiffPackages.put("ic_launcher_wallet.png", "com.finshell.wallet");
        mDiffPackages.put("ic_launcher_bbs.png", "com.coloros.bbs");
        mDiffPackages.put("ic_launcher_calender.png", "com.coloros.calendar");
        mDiffPackages.put("ic_launcher_encryptiont.png", "com.coloros.encryptiont");
        mDiffPackages.put("ic_launcher_findmyphone.png", "com.coloros.findmyphone");
        mDiffPackages.put("ic_launcher_gamespace.png", "com.coloros.gamespaceui");
        mDiffPackages.put("ic_launcher_shortcuts.png", "com.coloros.shortcuts");
        mDiffPackages.put("ic_launcher_videoeditor.png", "com.coloros.videoeditor");
        mDiffPackages.put("ic_launcher_browser.png", "com.heytap.browser");
        mDiffPackages.put("ic_launcher_book_store.png", "com.heytap.book");
        mDiffPackages.put("ic_launcher_health.png", "com.heytap.health");
        mDiffPackages.put("ic_launcher_nearme_market.png", "com.heytap.market");
        mDiffPackages.put("ic_launcher_nearme_reader.png", "com.heytap.reader");
        mDiffPackages.put("ic_launcher_ohome.png", "com.heytap.smarthome");
        mDiffPackages.put("ic_launcher_speechassist.png", "com.heytap.speechassist");
        mDiffPackages.put("ic_launcher_themespace.png", "com.heytap.themestore");
        mDiffPackages.put("ic_launcher_nearme_usercenter.png", "com.heytap.usercenter");
        mDiffPackages.put("ic_launcher_yoli.png", "com.heytap.yoli");
        mDiffPackages.put("ic_launcher_store.png", "com.oppo.store");
        mDiffPackages.put("ic_launcher_calcultor.png", "com.coloros.calculator");
        mDiffPackages.put("ic_launcher_nearme_note.png", "com.coloros.note");
        mDiffPackages.put("ic_launcher_compass.png", "com.coloros.compass2");
        mDiffPackages.put("ic_launcher_weather.png", "com.coloros.weather2");
    }

    /* access modifiers changed from: package-private */
    public static class IconXmlHandler extends DefaultHandler {
        IconXmlHandler() {
        }

        @Override // org.xml.sax.helpers.DefaultHandler, org.xml.sax.ContentHandler
        public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException {
            String unused = OppoAppIconInfo.mCurrentTag = localName;
            if (OppoAppIconInfo.mCurrentTag.equalsIgnoreCase("icon")) {
                String iconName = attributes.getValue("name");
                if (iconName != null) {
                    OppoAppIconInfo.mAllIconNames.add(iconName);
                } else {
                    OppoAppIconInfo.mAllIconNames.add("no_icon_name");
                }
                String packageName = attributes.getValue("package");
                if (packageName != null) {
                    OppoAppIconInfo.mAllPackageNames.add(packageName);
                } else {
                    OppoAppIconInfo.mAllIconNames.add("no_package_name");
                }
            }
        }

        @Override // org.xml.sax.helpers.DefaultHandler, org.xml.sax.ContentHandler
        public void characters(char[] ch, int start, int length) throws SAXException {
        }

        @Override // org.xml.sax.helpers.DefaultHandler, org.xml.sax.ContentHandler
        public void endElement(String uri, String localName, String name) throws SAXException {
            String unused = OppoAppIconInfo.mCurrentTag = null;
        }
    }

    public static void parseXml(InputStream inStream) throws Exception {
        SAXParserFactory.newInstance().newSAXParser().parse(inStream, new IconXmlHandler());
        inStream.close();
    }

    public static boolean parseIconXml() {
        return false;
    }

    public static boolean parseIconXmlForUser(int userId) {
        InputStream input;
        if (sParsered && DEBUG) {
            Log.d(TAG, "parseIconXml sParsered true");
        }
        ZipFile param = null;
        InputStream input2 = null;
        mAllIconNames.clear();
        mAllPackageNames.clear();
        if (!OppoThirdPartUtil.mIsDefaultTheme) {
            boolean checkDiff = false;
            try {
                String thirdIconPath = OppoThirdPartUtil.getThemePathForUser(userId) + "icons";
                if (new File(thirdIconPath).exists()) {
                    param = new ZipFile(thirdIconPath);
                    ZipEntry entry = param.getEntry(ALL_APPS);
                    if (entry == null) {
                        input = new FileInputStream("/system/media/theme/default/allApps.xml");
                    } else {
                        input = param.getInputStream(entry);
                        checkDiff = true;
                    }
                } else {
                    input = new FileInputStream("/system/media/theme/default/allApps.xml");
                }
                parseXml(input);
                input.close();
                if (param != null) {
                    param.close();
                }
                if (checkDiff) {
                    checkDiffPackages();
                }
                try {
                    input.close();
                    if (param != null) {
                        param.close();
                    }
                    sParsered = true;
                } catch (Exception e) {
                    Log.e(TAG, "parseIconXml input error");
                }
                return true;
            } catch (Exception ex) {
                Log.e(TAG, "parseIconXml error");
                ex.printStackTrace();
                if (0 != 0) {
                    try {
                        input2.close();
                    } catch (Exception e2) {
                        Log.e(TAG, "parseIconXml input error");
                        return false;
                    }
                }
                if (0 != 0) {
                    param.close();
                }
                sParsered = true;
                return false;
            } catch (Throwable th) {
                if (0 != 0) {
                    try {
                        input2.close();
                    } catch (Exception e3) {
                        Log.e(TAG, "parseIconXml input error");
                        throw th;
                    }
                }
                if (0 != 0) {
                    param.close();
                }
                sParsered = true;
                throw th;
            }
        } else {
            try {
                input2 = new FileInputStream("/system/media/theme/default/allApps.xml");
                parseXml(input2);
                input2.close();
                try {
                    input2.close();
                    sParsered = true;
                } catch (Exception e4) {
                    Log.e(TAG, "parseIconXml input error");
                }
                return true;
            } catch (Exception ex2) {
                Log.e(TAG, "parseIconXml error");
                ex2.printStackTrace();
                if (input2 != null) {
                    try {
                        input2.close();
                    } catch (Exception e5) {
                        Log.e(TAG, "parseIconXml input error");
                        return false;
                    }
                }
                sParsered = true;
                return false;
            } catch (Throwable th2) {
                if (input2 != null) {
                    try {
                        input2.close();
                    } catch (Exception e6) {
                        Log.e(TAG, "parseIconXml input error");
                        throw th2;
                    }
                }
                sParsered = true;
                throw th2;
            }
        }
    }

    public static boolean isThirdPart(ApplicationInfo ai) {
        if (mAllPackageNames.contains(ai.packageName)) {
            return false;
        }
        return true;
    }

    public static boolean isThirdPartbyIconName(String iconName) {
        if (mAllIconNames.contains(iconName)) {
            return false;
        }
        return true;
    }

    public static int getAppsNumbers() {
        return mAllPackageNames.size();
    }

    public static int indexOfPackageName(String name) {
        return mAllPackageNames.indexOf(name);
    }

    public static String getPackageName(int index) {
        return mAllPackageNames.get(index);
    }

    public static int indexOfIconName(String name) {
        return mAllIconNames.indexOf(name);
    }

    public static String getIconName(int index) {
        return mAllIconNames.get(index);
    }

    public static void reset() {
        sParsered = false;
    }

    private static void checkDiffPackages() {
        for (Map.Entry<String, String> entry : mDiffPackages.entrySet()) {
            if (entry != null) {
                String key = entry.getKey();
                String value = entry.getValue();
                int iconIndex = mAllIconNames.indexOf(key);
                int packIndex = mAllPackageNames.indexOf(value);
                if (iconIndex < 0 || packIndex < 0) {
                    mAllIconNames.add(key);
                    mAllPackageNames.add(value);
                }
            }
        }
    }
}
