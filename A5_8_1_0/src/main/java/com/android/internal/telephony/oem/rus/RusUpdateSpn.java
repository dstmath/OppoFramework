package com.android.internal.telephony.oem.rus;

import com.android.internal.util.XmlUtils;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import org.xmlpull.v1.XmlPullParser;

public final class RusUpdateSpn extends RusBase {
    private static final String TAG = "RusUpdateSpn";
    private static final String citylan_name = "spn_citylan.xml";
    private static final String country_lan = "<CountryLan";
    private static final String country_lan_end = "/CountryLan>";
    private static final int countrylan_count = 12;
    private static final String index_end = "/lan_";
    private static final String index_start = "<lan_";
    private static final String mSubPath = "/data/data/com.android.phone/";
    private static final String spnname_postfix = ".xml";
    private static final String spnname_substr = "spn_";
    private String mSpnContent = null;

    public RusUpdateSpn() {
        setPath(mSubPath);
    }

    public void execute() {
        valiateAndLoadSpn();
    }

    private void valiateAndLoadSpn() {
        printLog(TAG, "ValiateAndLoadSpn");
        ArrayList<String> lan = new ArrayList();
        try {
            this.mSpnContent = getContent();
            if (this.mSpnContent != null) {
                lan = getCityLan(this.mSpnContent);
                if (lan != null) {
                    for (int i = 0; i < lan.size(); i++) {
                        printLog(TAG, "The current enum spn name=" + ((String) lan.get(i)));
                        printLog(TAG, "The current enum spn ordinal=" + i);
                        createLanXmlFile((String) lan.get(i));
                    }
                } else {
                    return;
                }
            }
            printLog(TAG, "the romupdate database data is null");
        } catch (Exception e) {
            printLog(TAG, "Exception while parsing " + e);
        }
        return;
    }

    private ArrayList<String> getCityLan(String providerstirng) {
        String cityLanPath = this.mPath + citylan_name;
        int citylanStartIndex = providerstirng.indexOf(country_lan);
        int citylanEndIndex = providerstirng.indexOf(country_lan_end);
        printLog(TAG, "citylanStartIndex=" + citylanStartIndex + "citylanEndIndex=" + citylanEndIndex);
        String cityLanContent = providerstirng.substring(citylanStartIndex, citylanEndIndex + 12);
        printLog(TAG, "cityLanContent is = " + cityLanContent);
        File citylanFile = this.mFileUtils.saveToFile(cityLanContent, cityLanPath);
        if (!citylanFile.exists()) {
            return null;
        }
        ArrayList arrayList = new ArrayList();
        return getXmlParserResult(citylanFile);
    }

    /* JADX WARNING: Removed duplicated region for block: B:29:0x00d7 A:{SYNTHETIC, Splitter: B:29:0x00d7} */
    /* JADX WARNING: Removed duplicated region for block: B:43:0x0103 A:{SYNTHETIC, Splitter: B:43:0x0103} */
    /* JADX WARNING: Removed duplicated region for block: B:49:0x010e A:{SYNTHETIC, Splitter: B:49:0x010e} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private ArrayList<String> getXmlParserResult(File file) {
        FileNotFoundException e;
        Throwable th;
        Exception e2;
        FileReader confreader = null;
        ArrayList<String> allLanList = new ArrayList();
        try {
            FileReader confreader2 = new FileReader(file);
            try {
                XmlPullParser parser = getXmlParser(confreader2, "CountryLan");
                if (parser != null) {
                    XmlUtils.nextElement(parser);
                    while (parser.getEventType() != 1) {
                        if ("city_lan".equals(parser.getName())) {
                            int lanCount = Integer.parseInt(parser.getAttributeValue(null, "lanCount"));
                            printLog(TAG, "The count of language is =" + lanCount);
                            for (int i = 0; i < lanCount; i++) {
                                String lan = parser.getAttributeValue(null, "lan_" + String.valueOf(i));
                                printLog(TAG, " The City_lan" + String.valueOf(i) + "is=" + lan);
                                allLanList.add(lan);
                            }
                            XmlUtils.nextElement(parser);
                        } else {
                            printLog(TAG, "this first tag is not match");
                            if (confreader2 != null) {
                                try {
                                    confreader2.close();
                                } catch (IOException e3) {
                                }
                            }
                            return null;
                        }
                    }
                }
                if (confreader2 != null) {
                    try {
                        confreader2.close();
                    } catch (IOException e4) {
                    }
                }
            } catch (FileNotFoundException e5) {
                e = e5;
                confreader = confreader2;
                try {
                    printLog(TAG, "FileNotFoundException " + e);
                    if (confreader != null) {
                    }
                    return allLanList;
                } catch (Throwable th2) {
                    th = th2;
                    if (confreader != null) {
                    }
                    throw th;
                }
            } catch (Exception e6) {
                e2 = e6;
                confreader = confreader2;
                printLog(TAG, "Exception while parsing apns xml file" + e2);
                if (confreader != null) {
                }
                return allLanList;
            } catch (Throwable th3) {
                th = th3;
                confreader = confreader2;
                if (confreader != null) {
                    try {
                        confreader.close();
                    } catch (IOException e7) {
                    }
                }
                throw th;
            }
        } catch (FileNotFoundException e8) {
            e = e8;
            printLog(TAG, "FileNotFoundException " + e);
            if (confreader != null) {
                try {
                    confreader.close();
                } catch (IOException e9) {
                }
            }
            return allLanList;
        } catch (Exception e10) {
            e2 = e10;
            printLog(TAG, "Exception while parsing apns xml file" + e2);
            if (confreader != null) {
                try {
                    confreader.close();
                } catch (IOException e11) {
                }
            }
            return allLanList;
        }
        return allLanList;
    }

    private void createLanXmlFile(String name) {
        String spnPath = this.mPath + spnname_substr + name + spnname_postfix;
        printLog(TAG, "romupdate spn_en path=" + spnPath);
        int subLength = name.length();
        int spnStartIndex = this.mSpnContent.indexOf(index_start + name);
        int spnEndIndex = this.mSpnContent.indexOf(index_end + name);
        printLog(TAG, "spn_index start=" + spnStartIndex + " spn_indext end" + spnEndIndex);
        String spnContent = this.mSpnContent.substring(spnStartIndex, ((spnEndIndex + 4) + subLength) + 2);
        printLog(TAG, "SPN XML STRING IS:" + spnContent);
        this.mFileUtils.saveSpnToFile(spnContent, spnPath);
    }
}
