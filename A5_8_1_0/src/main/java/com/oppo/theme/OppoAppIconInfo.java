package com.oppo.theme;

import android.content.pm.ApplicationInfo;
import android.util.Log;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class OppoAppIconInfo {
    public static final String ALL_APPS = "allApps.xml";
    private static ArrayList<String> mAllIconNames = new ArrayList();
    private static ArrayList<String> mAllPackageNames = new ArrayList();
    private static String mCurrentTag = null;

    static class IconXmlHandler extends DefaultHandler {
        IconXmlHandler() {
        }

        public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException {
            OppoAppIconInfo.mCurrentTag = localName;
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

        public void characters(char[] ch, int start, int length) throws SAXException {
        }

        public void endElement(String uri, String localName, String name) throws SAXException {
            OppoAppIconInfo.mCurrentTag = null;
        }
    }

    public static void parseXml(InputStream inStream) throws Exception {
        SAXParserFactory.newInstance().newSAXParser().parse(inStream, new IconXmlHandler());
        inStream.close();
    }

    /* JADX WARNING: Removed duplicated region for block: B:53:0x00bb A:{SYNTHETIC, Splitter: B:53:0x00bb} */
    /* JADX WARNING: Removed duplicated region for block: B:32:0x0079 A:{SYNTHETIC, Splitter: B:32:0x0079} */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x007e A:{Catch:{ Exception -> 0x0082 }} */
    /* JADX WARNING: Removed duplicated region for block: B:60:0x00cd A:{SYNTHETIC, Splitter: B:60:0x00cd} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static boolean parseIconXml() {
        Exception ex;
        Throwable th;
        ZipFile param = null;
        InputStream input = null;
        mAllIconNames.clear();
        mAllPackageNames.clear();
        OppoThirdPartUtil.setDefaultTheme();
        if (OppoThirdPartUtil.mIsDefaultTheme) {
            try {
                InputStream input2 = new FileInputStream("/system/media/theme/default/allApps.xml");
                try {
                    parseXml(input2);
                    input2.close();
                    if (input2 != null) {
                        try {
                            input2.close();
                        } catch (Exception e) {
                            Log.e("parseIconXml", "input error");
                        }
                    }
                    return true;
                } catch (Exception e2) {
                    ex = e2;
                    input = input2;
                    try {
                        Log.e("parseIconXml", "parseIconXml error");
                        ex.printStackTrace();
                        if (input != null) {
                        }
                        return false;
                    } catch (Throwable th2) {
                        th = th2;
                        if (input != null) {
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    input = input2;
                    if (input != null) {
                        try {
                            input.close();
                        } catch (Exception e3) {
                            Log.e("parseIconXml", "input error");
                        }
                    }
                    throw th;
                }
            } catch (Exception e4) {
                ex = e4;
                Log.e("parseIconXml", "parseIconXml error");
                ex.printStackTrace();
                if (input != null) {
                    try {
                        input.close();
                    } catch (Exception e5) {
                        Log.e("parseIconXml", "input error");
                    }
                }
                return false;
            }
        }
        try {
            ZipFile param2 = new ZipFile("/data/theme/icons");
            try {
                ZipEntry entry = param2.getEntry(ALL_APPS);
                if (entry == null) {
                    input = new FileInputStream("/system/media/theme/default/allApps.xml");
                } else {
                    input = param2.getInputStream(entry);
                }
                parseXml(input);
                input.close();
                param2.close();
                if (input != null) {
                    try {
                        input.close();
                    } catch (Exception e6) {
                        Log.e("parseIconXml", "input param error");
                    }
                }
                if (param2 != null) {
                    param2.close();
                }
                return true;
            } catch (Exception e7) {
                ex = e7;
                param = param2;
            } catch (Throwable th4) {
                th = th4;
                param = param2;
                if (input != null) {
                }
                if (param != null) {
                }
                throw th;
            }
        } catch (Exception e8) {
            ex = e8;
            try {
                Log.e("parseIconXml", "parseIconXml error");
                ex.printStackTrace();
                if (input != null) {
                    try {
                        input.close();
                    } catch (Exception e9) {
                        Log.e("parseIconXml", "input param error");
                        return false;
                    }
                }
                if (param != null) {
                    param.close();
                }
                return false;
            } catch (Throwable th5) {
                th = th5;
                if (input != null) {
                    try {
                        input.close();
                    } catch (Exception e10) {
                        Log.e("parseIconXml", "input param error");
                        throw th;
                    }
                }
                if (param != null) {
                    param.close();
                }
                throw th;
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
        return (String) mAllPackageNames.get(index);
    }

    public static int indexOfIconName(String name) {
        return mAllIconNames.indexOf(name);
    }

    public static String getIconName(int index) {
        return (String) mAllIconNames.get(index);
    }
}
