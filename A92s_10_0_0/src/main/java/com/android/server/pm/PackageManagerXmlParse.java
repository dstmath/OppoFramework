package com.android.server.pm;

import android.os.SystemProperties;
import android.util.Slog;
import com.android.internal.util.ArrayUtils;
import com.android.server.oppo.TemperatureProvider;
import com.android.server.slice.SliceClientPermissions;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

class PackageManagerXmlParse {
    public static String DATAFLAG = "white";
    public static String SYSTEMFLAG = "black";
    static final String TAG = "PackageManagerXmlParse";
    static Map<ThirdPartPackage, Boolean> lastMatchResult = new HashMap();
    private static boolean mIsSupportRSA3 = false;
    static Map<String, ArrayList<String>> mPackagePathMaping = new HashMap();
    static PhoneInfo phoneObject;
    public final int MAXSIZE = 1000;
    public ArrayList<PackagePathlist> mAddList = new ArrayList<>();
    public ArrayList<PackagePathlist> mDeleteList = new ArrayList<>();
    ArrayList<ThirdPartPackage> mSuitProjectThirdPartPackages = new ArrayList<>();
    ArrayList<ThirdPartPackage> mTotalThirdPartPackages = new ArrayList<>();
    private String packageMappingFile = "package_path.txt";

    PackageManagerXmlParse(String xmlFilePath, String xmlFlag, String mProject, String mLightOS, String mCountry, String mEuexOperator, String mOperator) {
        mIsSupportRSA3 = SystemProperties.get("ro.oppo.rsa3.support", TemperatureProvider.SWITCH_OFF).equalsIgnoreCase(TemperatureProvider.SWITCH_ON);
        phoneObject = new PhoneInfo(mProject, mLightOS, mCountry, mEuexOperator, mOperator);
        phoneObject.printPhoneInfo();
        initPackagePathMappingList(xmlFilePath);
        parseXml(xmlFilePath, xmlFlag);
    }

    static ArrayList<String> readByLineFromMappingFile(File file) {
        ArrayList<String> list = new ArrayList<>();
        if (!file.exists()) {
            return list;
        }
        BufferedReader in = null;
        try {
            BufferedReader in2 = new BufferedReader(new FileReader(file));
            while (true) {
                String line = in2.readLine();
                if (line != null) {
                    list.add(line);
                } else {
                    try {
                        break;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            in2.close();
        } catch (FileNotFoundException e2) {
            e2.printStackTrace();
            if (in != null) {
                in.close();
            }
        } catch (IOException e3) {
            e3.printStackTrace();
            if (in != null) {
                in.close();
            }
        } catch (Throwable th) {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e4) {
                    e4.printStackTrace();
                }
            }
            throw th;
        }
        return list;
    }

    /* access modifiers changed from: package-private */
    public void getSuitPackagePathArrayList() {
        for (int index = 0; index < this.mTotalThirdPartPackages.size(); index++) {
            if (this.mSuitProjectThirdPartPackages.contains(this.mTotalThirdPartPackages.get(index))) {
                String packageNm = this.mTotalThirdPartPackages.get(index).getXmlPacakgeName();
                String truePath = this.mTotalThirdPartPackages.get(index).getXmlPackagePath();
                if (mPackagePathMaping.containsKey(packageNm)) {
                    for (int i = 0; i < mPackagePathMaping.get(packageNm).size(); i++) {
                        if (mPackagePathMaping.get(packageNm).get(i).contains(truePath)) {
                            truePath = mPackagePathMaping.get(packageNm).get(i);
                        }
                    }
                }
                PackagePathlist packagePathAddList = new PackagePathlist(packageNm, truePath);
                if (!this.mAddList.contains(packagePathAddList)) {
                    this.mAddList.add(packagePathAddList);
                }
            } else {
                String packageNmDel = this.mTotalThirdPartPackages.get(index).getXmlPacakgeName();
                String truePathDel = this.mTotalThirdPartPackages.get(index).getXmlPackagePath();
                if (mPackagePathMaping.containsKey(packageNmDel)) {
                    for (int i2 = 0; i2 < mPackagePathMaping.get(packageNmDel).size(); i2++) {
                        if (mPackagePathMaping.get(packageNmDel).get(i2).contains(truePathDel)) {
                            truePathDel = mPackagePathMaping.get(packageNmDel).get(i2);
                        }
                    }
                }
                PackagePathlist packagePathDelList = new PackagePathlist(packageNmDel, truePathDel);
                if (!this.mDeleteList.contains(packagePathDelList)) {
                    this.mDeleteList.add(packagePathDelList);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void initPackagePathMappingList(String mappingFilePath) {
        ArrayList<String> list;
        try {
            File data_app_mapping_file = new File(mappingFilePath, this.packageMappingFile);
            if (!data_app_mapping_file.exists() || !data_app_mapping_file.exists() || (list = readByLineFromMappingFile(data_app_mapping_file)) == null) {
                return;
            }
            if (list.size() >= 1) {
                for (int i = 0; i < list.size(); i++) {
                    String[] listArray = list.get(i).split(":", 2);
                    if (listArray != null && listArray.length == 2) {
                        String packageName = listArray[0];
                        String packagePath = listArray[1];
                        ArrayList<String> pathList = new ArrayList<>();
                        pathList.add(packagePath);
                        if (mPackagePathMaping.containsKey(packageName)) {
                            mPackagePathMaping.get(packageName).add(packagePath);
                        } else {
                            mPackagePathMaping.put(packageName, pathList);
                        }
                    }
                }
            }
        } catch (Exception e) {
        }
    }

    public static String setStringValue(String value, String defaultVaule) {
        return value == null ? defaultVaule.trim() : value.trim();
    }

    /* access modifiers changed from: package-private */
    public void parseXml(String xmlFilePath, String dirTag) {
        File[] files = new File(xmlFilePath).listFiles();
        if (ArrayUtils.isEmpty(files)) {
            Slog.d(TAG, "No files in app dir " + xmlFilePath);
            return;
        }
        for (File xmlFile : files) {
            if (mIsSupportRSA3) {
                if (xmlFile.getName().contains("RSA3_0")) {
                    Slog.e(TAG, "is RSA3.0 project reading xml file :" + xmlFile.getName());
                    parseXmlByLine(xmlFile, dirTag);
                    getSuitPackagePathArrayList();
                } else {
                    Slog.e(TAG, "is RSA3.0 project " + xmlFile.getName() + " is not need");
                }
            } else if (!xmlFile.getName().contains("RSA3_0")) {
                Slog.e(TAG, " reading xml file :" + xmlFile.getName());
                parseXmlByLine(xmlFile, dirTag);
                getSuitPackagePathArrayList();
            } else {
                Slog.e(TAG, "no RSA3.0 project " + xmlFile.getName() + " is not need");
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x0024, code lost:
        r27 = null;
        r2 = r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x0023, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Removed duplicated region for block: B:103:0x0340 A[SYNTHETIC, Splitter:B:103:0x0340] */
    /* JADX WARNING: Removed duplicated region for block: B:113:0x0353  */
    /* JADX WARNING: Removed duplicated region for block: B:120:0x0369 A[SYNTHETIC, Splitter:B:120:0x0369] */
    /* JADX WARNING: Removed duplicated region for block: B:26:0x007d A[Catch:{ all -> 0x0345 }] */
    /* JADX WARNING: Removed duplicated region for block: B:67:0x01ed A[SYNTHETIC, Splitter:B:67:0x01ed] */
    /* JADX WARNING: Removed duplicated region for block: B:83:0x026d A[Catch:{ IOException | XmlPullParserException -> 0x0335 }] */
    /* JADX WARNING: Removed duplicated region for block: B:9:0x0023 A[ExcHandler: IOException | XmlPullParserException (r0v9 'e' java.lang.Exception A[CUSTOM_DECLARE]), Splitter:B:19:0x003f] */
    public void parseXmlByLine(File xmlConfigFile, String fileTag) {
        InputStream input;
        Throwable th;
        Exception e;
        InputStream input2;
        int i;
        XmlPullParser parser;
        int i2;
        XmlPullParser parser2;
        XmlPullParserFactory factory;
        String eventname;
        String pathName;
        String countryList;
        boolean z;
        String reverseMatch = "";
        this.mSuitProjectThirdPartPackages.clear();
        this.mTotalThirdPartPackages.clear();
        try {
            try {
                input2 = new FileInputStream(xmlConfigFile);
            } catch (FileNotFoundException e2) {
                e = e2;
                try {
                    e.printStackTrace();
                    input2 = null;
                    Slog.d(TAG, "start parse xml,xml name : " + xmlConfigFile.getName() + " xml flag: " + fileTag);
                    XmlPullParserFactory factory2 = XmlPullParserFactory.newInstance();
                    i = 1;
                    factory2.setNamespaceAware(true);
                    parser = factory2.newPullParser();
                    parser.setInput(input2, "UTF-8");
                    while (parser.getEventType() != i) {
                    }
                    input = input2;
                    if (input != null) {
                    }
                } catch (IOException | XmlPullParserException e3) {
                } catch (Throwable th2) {
                    input = null;
                    th = th2;
                    if (input != null) {
                    }
                    throw th;
                }
            }
            try {
                Slog.d(TAG, "start parse xml,xml name : " + xmlConfigFile.getName() + " xml flag: " + fileTag);
                XmlPullParserFactory factory22 = XmlPullParserFactory.newInstance();
                i = 1;
                factory22.setNamespaceAware(true);
                parser = factory22.newPullParser();
                parser.setInput(input2, "UTF-8");
                while (parser.getEventType() != i) {
                    if (parser.getEventType() != 2) {
                        parser2 = parser;
                        i2 = i;
                        factory = factory22;
                        input = input2;
                        eventname = reverseMatch;
                    } else if (parser.getName().equalsIgnoreCase(Settings.ATTR_PACKAGE)) {
                        String packageName = setStringValue(parser.getAttributeValue(null, Settings.ATTR_NAME), reverseMatch);
                        String pathName2 = setStringValue(parser.getAttributeValue(null, "path"), reverseMatch);
                        String reverseMatch2 = setStringValue(parser.getAttributeValue(null, "negative"), TemperatureProvider.SWITCH_OFF);
                        String ifLightOsConfig = setStringValue(parser.getAttributeValue(null, "lightOS"), TemperatureProvider.SWITCH_ON);
                        String projectList = setStringValue(parser.getAttributeValue(null, "project"), reverseMatch);
                        String countryList2 = setStringValue(parser.getAttributeValue(null, "country"), reverseMatch);
                        String euexcountryList = setStringValue(parser.getAttributeValue(null, "euexcountry"), reverseMatch);
                        factory = factory22;
                        String operatorList = setStringValue(parser.getAttributeValue(null, "operator"), reverseMatch);
                        if (projectList == reverseMatch || countryList2 == reverseMatch || operatorList == reverseMatch) {
                            Slog.d(TAG, "Whitelist xml may config wrong in this line");
                            parser.next();
                            reverseMatch = reverseMatch;
                            factory22 = factory;
                            parser = parser;
                            i = 1;
                            input2 = input2;
                        } else if (packageName == reverseMatch && pathName2 == reverseMatch) {
                            try {
                                Slog.d(TAG, "Whitelist xml may config wrong in this line, no package value and path value");
                                parser.next();
                                factory22 = factory;
                                i = 1;
                            } catch (Throwable th3) {
                                th = th3;
                                input = input2;
                                if (input != null) {
                                }
                                throw th;
                            }
                        } else {
                            try {
                                if (fileTag.contains(DATAFLAG)) {
                                    try {
                                        countryList = countryList2;
                                        if (pathName2.equals(reverseMatch)) {
                                            pathName = "data/app/" + packageName;
                                        } else if (!pathName2.contains("data/app")) {
                                            pathName = "data/app/" + pathName2;
                                        }
                                        eventname = reverseMatch;
                                        parser2 = parser;
                                        i2 = 1;
                                        input = input2;
                                        ThirdPartPackage thirdPartPackage = new ThirdPartPackage(packageName, reverseMatch2, ifLightOsConfig, pathName, projectList, countryList, euexcountryList, operatorList, fileTag);
                                        this.mTotalThirdPartPackages.add(thirdPartPackage);
                                        boolean z2 = false;
                                        if (fileTag == DATAFLAG) {
                                            try {
                                                boolean suitResult = thirdPartPackage.ifSuit(phoneObject);
                                                if (lastMatchResult.containsKey(thirdPartPackage)) {
                                                    if (!suitResult) {
                                                        if (!lastMatchResult.get(thirdPartPackage).booleanValue()) {
                                                            z = false;
                                                            suitResult = z;
                                                        }
                                                    }
                                                    z = true;
                                                    suitResult = z;
                                                }
                                                if (suitResult) {
                                                    Slog.d(TAG, fileTag + " xml:  configuration mode: " + reverseMatch2 + " add: " + thirdPartPackage.getXmlPacakgeNameOrPathName());
                                                    this.mSuitProjectThirdPartPackages.add(thirdPartPackage);
                                                } else {
                                                    Slog.d(TAG, fileTag + " xml:  configuration mode: " + reverseMatch2 + " remove: " + thirdPartPackage.getXmlPacakgeNameOrPathName());
                                                    this.mSuitProjectThirdPartPackages.remove(thirdPartPackage);
                                                }
                                                lastMatchResult.put(thirdPartPackage, Boolean.valueOf(suitResult));
                                            } catch (IOException | XmlPullParserException e4) {
                                                e = e4;
                                                try {
                                                    e.printStackTrace();
                                                    if (input != null) {
                                                        input.close();
                                                        return;
                                                    }
                                                } catch (Throwable th4) {
                                                    th = th4;
                                                    if (input != null) {
                                                    }
                                                    throw th;
                                                }
                                            }
                                        }
                                        if (fileTag == SYSTEMFLAG) {
                                            boolean suitResult2 = thirdPartPackage.ifSuit(phoneObject);
                                            if (lastMatchResult.containsKey(thirdPartPackage)) {
                                                if (suitResult2 || lastMatchResult.get(thirdPartPackage).booleanValue()) {
                                                    z2 = true;
                                                }
                                                suitResult2 = z2;
                                            }
                                            if (suitResult2) {
                                                Slog.d(TAG, fileTag + " xml:  configuration mode: " + reverseMatch2 + " disable: " + thirdPartPackage.getXmlPacakgeNameOrPathName());
                                                this.mSuitProjectThirdPartPackages.add(thirdPartPackage);
                                            } else {
                                                Slog.d(TAG, fileTag + " xml:  configuration mode: " + reverseMatch2 + " enable: " + thirdPartPackage.getXmlPacakgeNameOrPathName());
                                                this.mSuitProjectThirdPartPackages.remove(thirdPartPackage);
                                            }
                                            lastMatchResult.put(thirdPartPackage, Boolean.valueOf(suitResult2));
                                        }
                                    } catch (IOException | XmlPullParserException e5) {
                                        e = e5;
                                        input = input2;
                                        e.printStackTrace();
                                        if (input != null) {
                                        }
                                    }
                                } else {
                                    countryList = countryList2;
                                    if (fileTag.contains(SYSTEMFLAG)) {
                                        if (pathName2.equals(reverseMatch)) {
                                            pathName = "system/app/" + packageName;
                                        } else if (!pathName2.contains(SliceClientPermissions.SliceAuthority.DELIMITER)) {
                                            pathName = "system/app/" + pathName2;
                                        }
                                        eventname = reverseMatch;
                                        parser2 = parser;
                                        i2 = 1;
                                        input = input2;
                                        ThirdPartPackage thirdPartPackage2 = new ThirdPartPackage(packageName, reverseMatch2, ifLightOsConfig, pathName, projectList, countryList, euexcountryList, operatorList, fileTag);
                                        this.mTotalThirdPartPackages.add(thirdPartPackage2);
                                        boolean z22 = false;
                                        if (fileTag == DATAFLAG) {
                                        }
                                        if (fileTag == SYSTEMFLAG) {
                                        }
                                    }
                                }
                                pathName = pathName2;
                                eventname = reverseMatch;
                                parser2 = parser;
                                i2 = 1;
                                input = input2;
                                ThirdPartPackage thirdPartPackage22 = new ThirdPartPackage(packageName, reverseMatch2, ifLightOsConfig, pathName, projectList, countryList, euexcountryList, operatorList, fileTag);
                                this.mTotalThirdPartPackages.add(thirdPartPackage22);
                                boolean z222 = false;
                                if (fileTag == DATAFLAG) {
                                }
                                if (fileTag == SYSTEMFLAG) {
                                }
                            } catch (IOException | XmlPullParserException e6) {
                                input = input2;
                                e = e6;
                                e.printStackTrace();
                                if (input != null) {
                                }
                            }
                        }
                    } else {
                        parser2 = parser;
                        i2 = i;
                        factory = factory22;
                        input = input2;
                        eventname = reverseMatch;
                    }
                    parser2.next();
                    reverseMatch = eventname;
                    factory22 = factory;
                    parser = parser2;
                    i = i2;
                    input2 = input;
                }
                input = input2;
                if (input != null) {
                    try {
                        input.close();
                    } catch (IOException e7) {
                        e7.printStackTrace();
                    }
                }
            } catch (Throwable th5) {
                input = input2;
                th = th5;
                if (input != null) {
                    try {
                        input.close();
                    } catch (IOException e8) {
                        e8.printStackTrace();
                    }
                }
                throw th;
            }
        } catch (FileNotFoundException e9) {
            e = e9;
            e.printStackTrace();
            input2 = null;
            Slog.d(TAG, "start parse xml,xml name : " + xmlConfigFile.getName() + " xml flag: " + fileTag);
            XmlPullParserFactory factory222 = XmlPullParserFactory.newInstance();
            i = 1;
            factory222.setNamespaceAware(true);
            parser = factory222.newPullParser();
            parser.setInput(input2, "UTF-8");
            while (parser.getEventType() != i) {
            }
            input = input2;
            if (input != null) {
            }
        } catch (IOException | XmlPullParserException e10) {
            input = null;
            e = e10;
            e.printStackTrace();
            if (input != null) {
            }
        } catch (Throwable th6) {
            input = null;
            th = th6;
            if (input != null) {
            }
            throw th;
        }
    }
}
