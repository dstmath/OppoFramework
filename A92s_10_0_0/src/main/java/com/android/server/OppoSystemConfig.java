package com.android.server;

import android.content.pm.FeatureInfo;
import android.content.res.OppoThemeResources;
import android.os.Environment;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;
import android.util.Slog;
import android.util.Xml;
import com.android.internal.util.XmlUtils;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import libcore.io.IoUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class OppoSystemConfig extends SystemConfig {
    private static final boolean DEBUG = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    private static final boolean DEBUG_LOAD_FEATURE = SystemProperties.getBoolean("persist.debug.loadfeature", false);
    private static final String FEATURE_EXP_SELLMODE = "oppo.specialversion.exp.sellmode";
    static final int OPPO_ALLOW_ALL = -1;
    private static final String PATH_SELLMODE_FLAG = "/data/format_unclear/screensavers/sale_mode.fea";
    private static final String PERMISSION_XML_CTTEST = "/system/etc/permissions/com.oppo.rom.allnetcttest.xml";
    private static final String PERMISSION_XML_CUTEST = "/system/etc/permissions/com.oppo.rom.allnetcutest.xml";
    private static final String PERMISSION_XML_ROM = "/system/etc/permissions/com.oppo.rom.xml";
    private static final String PROPERTY_DEF_VALUE_ROM_FEATURE = "allnet";
    private static final String PROPERTY_DEF_VALUE_ROM_TEST_FEATURE = "allnetcmccdeeptest";
    private static final String PROPERTY_NAME_OPPO_OPERATOR = "ro.oppo.operator";
    private static final String PROPERTY_NAME_OPPO_OPERATOR_BY_SIM = "persist.sys.oppo_opta";
    private static final String PROPERTY_NAME_OPPO_REGION = "persist.sys.oppo.region";
    private static final String PROPERTY_NAME_OPPO_REGION_BY_SIM = "persist.sys.oppo_optb";
    private static final String PROPERTY_NAME_ROM_FEATURE = "ro.rom.featrue";
    private static final String PROPERTY_NAME_ROM_TEST_FEATURE = "ro.rom.test.featrue";
    private static final String TAG = "SystemConfig";
    private Method addFeatureMethod = null;
    private Method readPermissionsFromXmlMethod = null;
    private Method removeFeatureMethod = null;

    OppoSystemConfig() {
        initFiledAndMethod();
        readOperatorGroupFeature(Environment.buildPath(Environment.getRootDirectory(), "etc", "oppoOperatorGroupFeatures"), -1);
        readOppoOperatorFeature(Environment.buildPath(Environment.getRootDirectory(), "etc", "oppoOperatorFeatures"), -1);
        readOppoRegionMarkFeature(Environment.buildPath(Environment.getRootDirectory(), "etc", "oppoRegionMarkFeatures"), -1);
        readOppoEuexCountryFeature(Environment.buildPath(Environment.getRootDirectory(), "etc", "oppoEuexCountryFeatures"), -1);
        addDynamicFeatureNotInXml();
        removeUnavailableFeature();
        removeUnavailableEEAFeature();
    }

    @Override // com.android.server.OppoBaseSystemConfig
    public ArrayMap<String, FeatureInfo> loadOppoAvailableFeatures(String name) {
        File featureDir = Environment.buildPath(Environment.getRootDirectory(), "etc", "oppoRegionFeatures");
        if (!featureDir.exists() || !featureDir.isDirectory()) {
            Slog.w(TAG, "No directory " + featureDir + ", skipping");
            return null;
        } else if (!featureDir.canRead()) {
            Slog.w(TAG, "Directory " + featureDir + " cannot be read");
            return null;
        } else {
            File loadFile = null;
            File[] listFiles = featureDir.listFiles();
            int length = listFiles.length;
            int i = 0;
            while (true) {
                if (i >= length) {
                    break;
                }
                File f = listFiles[i];
                if (f.getPath().contains(name)) {
                    loadFile = f;
                    break;
                }
                i++;
            }
            if (loadFile != null) {
                return readOppoFeature(loadFile);
            }
            Slog.w(TAG, "path not exist : " + name);
            return null;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:14:0x0044 A[Catch:{ XmlPullParserException -> 0x00bb, IOException -> 0x00b5, all -> 0x00b3 }, LOOP:1: B:14:0x0044->B:42:0x0044, LOOP_START] */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x00ab A[Catch:{ XmlPullParserException -> 0x00bb, IOException -> 0x00b5, all -> 0x00b3 }] */
    private ArrayMap<String, FeatureInfo> readOppoFeature(File file) {
        int type;
        if (DEBUG) {
            Slog.d(TAG, "readOppoFeature " + file.getPath());
        }
        ArrayMap<String, FeatureInfo> featureInfos = new ArrayMap<>();
        try {
            FileReader permReader = new FileReader(file);
            try {
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(permReader);
                while (true) {
                    type = parser.next();
                    if (type == 2 || type == 1) {
                        if (type != 2) {
                            while (true) {
                                XmlUtils.nextElement(parser);
                                if (parser.getEventType() == 1) {
                                    break;
                                } else if ("feature".equals(parser.getName())) {
                                    String fname = parser.getAttributeValue(null, "name");
                                    if (fname == null) {
                                        Slog.w(TAG, "<feature> without name in " + file + " at " + parser.getPositionDescription());
                                    } else {
                                        Slog.i(TAG, "Got feature " + fname);
                                        FeatureInfo fi = new FeatureInfo();
                                        fi.name = fname;
                                        featureInfos.put(fname, fi);
                                    }
                                    XmlUtils.skipCurrentTag(parser);
                                } else {
                                    XmlUtils.skipCurrentTag(parser);
                                }
                            }
                            IoUtils.closeQuietly(permReader);
                            return featureInfos;
                        }
                        throw new XmlPullParserException("No start tag found");
                    }
                }
                if (type != 2) {
                }
            } catch (XmlPullParserException e) {
                Slog.w(TAG, "Got exception parsing permissions.", e);
            } catch (IOException e2) {
                Slog.w(TAG, "Got exception parsing permissions.", e2);
            } catch (Throwable th) {
                IoUtils.closeQuietly(permReader);
                throw th;
            }
        } catch (FileNotFoundException e3) {
            Slog.w(TAG, "Couldn't find or open permissions file " + file);
            return null;
        }
    }

    private void readOppoOperatorFeature(File operatorDir, int permissionFlag) {
        String featureName;
        if (!operatorDir.exists() || !operatorDir.isDirectory()) {
            Slog.w(TAG, "No directory " + operatorDir + ", skipping");
        } else if (!operatorDir.canRead()) {
            Slog.w(TAG, "Directory " + operatorDir + " cannot be read");
        } else {
            String region = SystemProperties.get(PROPERTY_NAME_OPPO_REGION);
            String operator = SystemProperties.get(PROPERTY_NAME_OPPO_OPERATOR);
            String operatorBySim = SystemProperties.get(PROPERTY_NAME_OPPO_OPERATOR_BY_SIM);
            String regionBySim = SystemProperties.get(PROPERTY_NAME_OPPO_REGION_BY_SIM);
            boolean scanXmlBySim = !TextUtils.isEmpty(operatorBySim) && !TextUtils.isEmpty(regionBySim);
            if (!scanXmlBySim && (TextUtils.isEmpty(region) || TextUtils.isEmpty(operator))) {
                Slog.w(TAG, "region or operator is empty.");
                return;
            }
            File operatorFile = null;
            if (scanXmlBySim) {
                if (TextUtils.isEmpty(operator)) {
                    operator = "OPPO";
                }
                featureName = regionBySim + "." + operator + "." + operatorBySim;
            } else {
                featureName = region + "." + operator;
            }
            Slog.w(TAG, "featureName " + featureName + " ok!");
            File[] operatorFiles = operatorDir.listFiles();
            if (operatorFiles != null && operatorFiles.length > 0) {
                int length = operatorFiles.length;
                int i = 0;
                while (true) {
                    if (i >= length) {
                        break;
                    }
                    File f = operatorFiles[i];
                    if (f.getPath().contains(featureName)) {
                        operatorFile = f;
                        break;
                    }
                    i++;
                }
            }
            if (operatorFile != null) {
                Slog.w(TAG, "operatorFile " + operatorFile + " ok!");
                try {
                    this.readPermissionsFromXmlMethod.invoke(this, operatorFile, Integer.valueOf(permissionFlag));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e2) {
                    e2.printStackTrace();
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Removed duplicated region for block: B:27:0x0087  */
    public void readOppoRegionMarkFeature(File regionMarkDir, int permissionFlag) {
        File regionMarkFile;
        File regionMarkFile2;
        if (!regionMarkDir.exists() || !regionMarkDir.isDirectory()) {
            if (permissionFlag == -1) {
                Slog.w(TAG, "No directory " + regionMarkDir + ", skipping");
            }
        } else if (!regionMarkDir.canRead()) {
            Slog.w(TAG, "Directory " + regionMarkDir + " cannot be read");
        } else {
            File regionMarkFile3 = null;
            String regionMark = SystemProperties.get("ro.oppo.regionmark");
            String RSAProtocal = SystemProperties.get("ro.oppo.rsa3.support");
            ArrayList<String> Tier2Country = new ArrayList<String>() {
                /* class com.android.server.OppoSystemConfig.AnonymousClass1 */

                {
                    add("ID");
                    add("MY");
                    add("PH");
                    add("TH");
                    add("VN");
                }
            };
            if (RSAProtocal != null && RSAProtocal.length() > 0 && RSAProtocal.equals("true")) {
                if (regionMark == null || regionMark == "" || !Tier2Country.contains(regionMark)) {
                    File[] listFiles = regionMarkDir.listFiles();
                    int length = listFiles.length;
                    int i = 0;
                    while (true) {
                        if (i >= length) {
                            break;
                        }
                        File f = listFiles[i];
                        if (f.getPath().contains("com.oppo.regionmark.RSA3PROTOCAL")) {
                            regionMarkFile2 = f;
                            break;
                        }
                        i++;
                    }
                    if (regionMarkFile2 != null) {
                        Slog.w(TAG, "RSA3.0 regionMarkFile is" + regionMarkFile2);
                        try {
                            this.readPermissionsFromXmlMethod.invoke(this, regionMarkFile2, Integer.valueOf(permissionFlag));
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        } catch (InvocationTargetException e2) {
                            e2.printStackTrace();
                        }
                    }
                    regionMarkFile3 = regionMarkFile2;
                }
                regionMarkFile2 = null;
                if (regionMarkFile2 != null) {
                }
                regionMarkFile3 = regionMarkFile2;
            }
            if (regionMark == null || regionMark.length() <= 0) {
                Slog.w(TAG, "regionMark is null !");
                return;
            }
            String featureName = "com.oppo.regionmark." + regionMark;
            Slog.w(TAG, "regionmark featureName is " + featureName);
            File[] listFiles2 = regionMarkDir.listFiles();
            int length2 = listFiles2.length;
            int i2 = 0;
            while (true) {
                if (i2 >= length2) {
                    regionMarkFile = regionMarkFile3;
                    break;
                }
                File f2 = listFiles2[i2];
                if (f2.getPath().contains(featureName)) {
                    regionMarkFile = f2;
                    break;
                }
                i2++;
            }
            if (regionMarkFile != null) {
                Slog.w(TAG, "regionMarkFile is" + regionMarkFile);
                try {
                    this.readPermissionsFromXmlMethod.invoke(this, regionMarkFile, Integer.valueOf(permissionFlag));
                } catch (IllegalAccessException e3) {
                    e3.printStackTrace();
                } catch (InvocationTargetException e4) {
                    e4.printStackTrace();
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void readOppoEuexCountryFeature(File euexCountryDir, int permissionFlag) {
        File euexCountryFile;
        if (!euexCountryDir.exists() || !euexCountryDir.isDirectory()) {
            if (permissionFlag == -1) {
                Slog.w(TAG, "No directory " + euexCountryDir + ", skipping");
            }
        } else if (!euexCountryDir.canRead()) {
            Slog.w(TAG, "Directory " + euexCountryDir + " cannot be read");
        } else {
            String euexCountry = SystemProperties.get("ro.oppo.euex.country");
            if (euexCountry == null || euexCountry.length() <= 0) {
                Slog.w(TAG, "regionMark is null !");
                return;
            }
            String featureName = "com.oppo.euex.country." + euexCountry;
            Slog.w(TAG, "euexCountry featureName is " + featureName);
            File[] listFiles = euexCountryDir.listFiles();
            int length = listFiles.length;
            int i = 0;
            while (true) {
                if (i >= length) {
                    euexCountryFile = null;
                    break;
                }
                File f = listFiles[i];
                if (f.getPath().contains(featureName)) {
                    euexCountryFile = f;
                    break;
                }
                i++;
            }
            if (euexCountryFile != null) {
                Slog.w(TAG, "euexCountryFile is" + euexCountryFile);
                try {
                    this.readPermissionsFromXmlMethod.invoke(this, euexCountryFile, Integer.valueOf(permissionFlag));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e2) {
                    e2.printStackTrace();
                }
            }
            String firstTestDate = SystemProperties.get("ro.oppo.first.test.date");
            if (firstTestDate != null && firstTestDate.length() > 0 && Long.parseLong(firstTestDate) >= 20200215) {
                String featureName2 = "com.oppo.euex.country.TMADA." + euexCountry;
                File[] listFiles2 = euexCountryDir.listFiles();
                int length2 = listFiles2.length;
                int i2 = 0;
                while (true) {
                    if (i2 >= length2) {
                        break;
                    }
                    File f2 = listFiles2[i2];
                    if (f2.getPath().contains(featureName2)) {
                        euexCountryFile = f2;
                        break;
                    }
                    i2++;
                }
                if (euexCountryFile != null) {
                    Slog.w(TAG, "euexCountryFile is" + euexCountryFile);
                    try {
                        this.readPermissionsFromXmlMethod.invoke(this, euexCountryFile, Integer.valueOf(permissionFlag));
                    } catch (IllegalAccessException e3) {
                        e3.printStackTrace();
                    } catch (InvocationTargetException e4) {
                        e4.printStackTrace();
                    }
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void readOperatorGroupFeature(File operateDir, int permissionFlag) {
        if (!operateDir.exists() || !operateDir.isDirectory()) {
            if (permissionFlag == -1) {
                Slog.w(TAG, "No directory " + operateDir + ", skipping");
            }
        } else if (!operateDir.canRead()) {
            Slog.w(TAG, "Directory " + operateDir + " cannot be read");
        } else {
            File operateFile = null;
            String operateName = SystemProperties.get(PROPERTY_NAME_OPPO_OPERATOR);
            if (operateName == null || operateName.length() <= 0) {
                Slog.w(TAG, "operateName is null !");
                return;
            }
            String featureName = "com.oppo.operator.group." + operateName + ".xml";
            Slog.w(TAG, "featureName " + featureName + " ok!");
            File[] listFiles = operateDir.listFiles();
            int length = listFiles.length;
            int i = 0;
            while (true) {
                if (i >= length) {
                    break;
                }
                File f = listFiles[i];
                if (f.getPath().contains(featureName)) {
                    operateFile = f;
                    break;
                }
                i++;
            }
            if (operateFile != null) {
                Slog.w(TAG, "operateFile " + operateFile + " ok!");
                try {
                    this.readPermissionsFromXmlMethod.invoke(this, operateFile, Integer.valueOf(permissionFlag));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e2) {
                    e2.printStackTrace();
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.OppoBaseSystemConfig
    public boolean filterOppoFeatureFile(File file) {
        return filterRomFeatureFile(file) || filterCommonSoftFeatureFile(file);
    }

    private boolean filterRomFeatureFile(File file) {
        if (DEBUG_LOAD_FEATURE) {
            Slog.d(TAG, "filterRomFeatureFile " + file.getPath());
        }
        String romFeature = SystemProperties.get(PROPERTY_NAME_ROM_FEATURE, PROPERTY_DEF_VALUE_ROM_FEATURE);
        String romTestFeature = SystemProperties.get(PROPERTY_NAME_ROM_TEST_FEATURE, PROPERTY_DEF_VALUE_ROM_TEST_FEATURE);
        if (PERMISSION_XML_CTTEST.equals(file.getPath()) || PERMISSION_XML_CUTEST.equals(file.getPath()) || !file.getPath().startsWith("/system/etc/permissions/com.oppo.rom.")) {
            return false;
        }
        String path = file.getPath();
        if (path.endsWith(romFeature + ".xml")) {
            return false;
        }
        String path2 = file.getPath();
        if (path2.endsWith(romTestFeature + ".xml") || PERMISSION_XML_ROM.equals(file.getPath())) {
            return false;
        }
        Slog.i(TAG, "scan feature file : " + file.getPath() + ",ignore!!!");
        return true;
    }

    private boolean filterCommonSoftFeatureFile(File file) {
        if (!file.getPath().startsWith("/system/etc/permissions/com.oppo.features.allnet.common.")) {
            return false;
        }
        String path = file.getPath();
        if (path.endsWith(SystemProperties.get("ro.commonsoft.product", OppoThemeResources.OPPO_PACKAGE) + ".xml")) {
            return false;
        }
        Slog.i(TAG, "scan feature file : " + file.getPath() + ",ignore!!!");
        return true;
    }

    /* access modifiers changed from: package-private */
    public void addDynamicFeatureNotInXml() {
        if (!this.mAvailableFeatures.containsKey(FEATURE_EXP_SELLMODE) && new File(PATH_SELLMODE_FLAG).exists()) {
            try {
                this.addFeatureMethod.invoke(this, FEATURE_EXP_SELLMODE, 0);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e2) {
                e2.printStackTrace();
            }
        }
    }

    private void removeUnavailableFeature() {
        if (SystemProperties.getBoolean("persist.sys.assert.panic.multi.user.entrance", false)) {
            try {
                this.removeFeatureMethod.invoke(this, "oppo.multiuser.entry.unsupport");
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e2) {
                e2.printStackTrace();
            }
        }
    }

    private void removeUnavailableEEAFeature() {
        String oppoEuexCountry = SystemProperties.get("ro.oppo.euex.country");
        String oppoNoneEEACountry = SystemProperties.get("ro.vendor.oppo.non.eea");
        if ("TR".equals(oppoEuexCountry) || "CH".equals(oppoEuexCountry) || "UA".equals(oppoEuexCountry) || "NONEEA".equals(oppoNoneEEACountry)) {
            try {
                this.removeFeatureMethod.invoke(this, "com.google.android.feature.EEA_DEVICE");
                this.removeFeatureMethod.invoke(this, "com.google.android.paid.search");
                this.removeFeatureMethod.invoke(this, "com.google.android.paid.chrome");
                this.removeFeatureMethod.invoke(this, "com.google.android.feature.EEA_V2_DEVICE");
                Slog.w(TAG, "remove unsupport EEA feature ");
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e2) {
                e2.printStackTrace();
            }
        }
    }

    private void initFiledAndMethod() {
        try {
            this.readPermissionsFromXmlMethod = SystemConfig.class.getDeclaredMethod("readPermissionsFromXml", File.class, Integer.TYPE);
            this.readPermissionsFromXmlMethod.setAccessible(true);
            this.addFeatureMethod = SystemConfig.class.getDeclaredMethod("addFeature", String.class, Integer.TYPE);
            this.addFeatureMethod.setAccessible(true);
            this.removeFeatureMethod = SystemConfig.class.getDeclaredMethod("removeFeature", String.class);
            this.removeFeatureMethod.setAccessible(true);
            Log.i(TAG, "addFeatureMethod = " + this.addFeatureMethod + " removeFeatureMethod = " + this.removeFeatureMethod + " readPermissionsFromXmlMethod = " + this.readPermissionsFromXmlMethod);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            Log.i(TAG, "NoSucheMethod");
        }
    }
}
