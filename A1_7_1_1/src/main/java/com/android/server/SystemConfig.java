package com.android.server;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.pm.FeatureInfo;
import android.os.Environment;
import android.os.Process;
import android.os.SystemProperties;
import android.os.storage.StorageManager;
import android.service.quicksettings.TileService;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.Slog;
import android.util.SparseArray;
import android.util.Xml;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.XmlUtils;
import com.oppo.media.OppoDaemonListHelper;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import libcore.io.IoUtils;
import oppo.content.res.OppoThemeResources;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class SystemConfig {
    private static final int ALLOW_ALL = -1;
    private static final int ALLOW_APP_CONFIGS = 8;
    private static final int ALLOW_FEATURES = 1;
    private static final int ALLOW_LIBS = 2;
    private static final int ALLOW_PERMISSIONS = 4;
    private static final String PERMISSION_XML_CTTEST = "/system/etc/permissions/com.oppo.rom.allnetcttest.xml";
    private static final String PERMISSION_XML_CUTEST = "/system/etc/permissions/com.oppo.rom.allnetcutest.xml";
    private static final String PERMISSION_XML_ROM = "/system/etc/permissions/com.oppo.rom.xml";
    private static final String SPECIAL_SENSOR = "lis2ds12";
    private static final String SPECIAL_SENSOR_PATH = "/proc/devinfo/Sensor_accel";
    private static final String SPECIAL_SENSOR_PERMISSION_XML = "/system/etc/permissions/com.oppo.special.sensor.xml";
    static final String TAG = "SystemConfig";
    static SystemConfig sInstance;
    final ArraySet<String> mAllowInDataUsageSave = new ArraySet();
    final ArraySet<String> mAllowInPowerSave = new ArraySet();
    final ArraySet<String> mAllowInPowerSaveExceptIdle = new ArraySet();
    final ArrayMap<String, FeatureInfo> mAvailableFeatures = new ArrayMap();
    final ArraySet<ComponentName> mBackupTransportWhitelist = new ArraySet();
    final ArraySet<ComponentName> mDefaultVrComponents = new ArraySet();
    final ArrayMap<String, List<String>> mDisabledUntilUsedPreinstalledCarrierAssociatedApps = new ArrayMap();
    int[] mGlobalGids;
    final ArraySet<String> mLinkedApps = new ArraySet();
    final ArrayMap<String, PermissionEntry> mPermissions = new ArrayMap();
    final ArrayMap<String, String> mSharedLibraries = new ArrayMap();
    final SparseArray<ArraySet<String>> mSystemPermissions = new SparseArray();
    final ArraySet<String> mSystemUserBlacklistedApps = new ArraySet();
    final ArraySet<String> mSystemUserWhitelistedApps = new ArraySet();
    final ArraySet<String> mUnavailableFeatures = new ArraySet();

    public static final class PermissionEntry {
        public int[] gids;
        public final String name;
        public boolean perUser;

        PermissionEntry(String name, boolean perUser) {
            this.name = name;
            this.perUser = perUser;
        }
    }

    public static SystemConfig getInstance() {
        SystemConfig systemConfig;
        synchronized (SystemConfig.class) {
            if (sInstance == null) {
                sInstance = new SystemConfig();
            }
            systemConfig = sInstance;
        }
        return systemConfig;
    }

    public int[] getGlobalGids() {
        return this.mGlobalGids;
    }

    public SparseArray<ArraySet<String>> getSystemPermissions() {
        return this.mSystemPermissions;
    }

    public ArrayMap<String, String> getSharedLibraries() {
        return this.mSharedLibraries;
    }

    public ArrayMap<String, FeatureInfo> getAvailableFeatures() {
        return this.mAvailableFeatures;
    }

    public ArrayMap<String, PermissionEntry> getPermissions() {
        return this.mPermissions;
    }

    public ArraySet<String> getAllowInPowerSaveExceptIdle() {
        return this.mAllowInPowerSaveExceptIdle;
    }

    public ArraySet<String> getAllowInPowerSave() {
        return this.mAllowInPowerSave;
    }

    public ArraySet<String> getAllowInDataUsageSave() {
        return this.mAllowInDataUsageSave;
    }

    public ArraySet<String> getLinkedApps() {
        return this.mLinkedApps;
    }

    public ArraySet<String> getSystemUserWhitelistedApps() {
        return this.mSystemUserWhitelistedApps;
    }

    public ArraySet<String> getSystemUserBlacklistedApps() {
        return this.mSystemUserBlacklistedApps;
    }

    public ArraySet<ComponentName> getDefaultVrComponents() {
        return this.mDefaultVrComponents;
    }

    public ArraySet<ComponentName> getBackupTransportWhitelist() {
        return this.mBackupTransportWhitelist;
    }

    public ArrayMap<String, List<String>> getDisabledUntilUsedPreinstalledCarrierAssociatedApps() {
        return this.mDisabledUntilUsedPreinstalledCarrierAssociatedApps;
    }

    SystemConfig() {
        readPermissions(Environment.buildPath(Environment.getRootDirectory(), new String[]{"etc", "sysconfig"}), -1);
        readPermissions(Environment.buildPath(Environment.getRootDirectory(), new String[]{"etc", "permissions"}), -1);
        readPermissions(Environment.buildPath(Environment.getOdmDirectory(), new String[]{"etc", "sysconfig"}), 11);
        readPermissions(Environment.buildPath(Environment.getOdmDirectory(), new String[]{"etc", "permissions"}), 11);
        readPermissions(Environment.buildPath(Environment.getOemDirectory(), new String[]{"etc", "sysconfig"}), 1);
        readPermissions(Environment.buildPath(Environment.getOemDirectory(), new String[]{"etc", "permissions"}), 1);
        readOppoOperatorFeature(Environment.buildPath(Environment.getRootDirectory(), new String[]{"etc", "oppoOperatorFeatures"}), -1);
        readPermissions(Environment.buildPath(Environment.getVendorDirectory(), new String[]{"etc", "sysconfig"}), 2);
        readPermissions(Environment.buildPath(Environment.getVendorDirectory(), new String[]{"etc", "permissions"}), 2);
    }

    public ArrayMap<String, FeatureInfo> loadOppoAvailableFeatures(String name) {
        int i = 0;
        File featureDir = Environment.buildPath(Environment.getRootDirectory(), new String[]{"etc", "oppoRegionFeatures"});
        if (!featureDir.exists() || !featureDir.isDirectory()) {
            Slog.w(TAG, "No directory " + featureDir + ", skipping");
            return null;
        } else if (featureDir.canRead()) {
            File loadFile = null;
            File[] listFiles = featureDir.listFiles();
            int length = listFiles.length;
            while (i < length) {
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
        } else {
            Slog.w(TAG, "Directory " + featureDir + " cannot be read");
            return null;
        }
    }

    private ArrayMap<String, FeatureInfo> readOppoFeature(File file) {
        ArrayMap<String, FeatureInfo> mOppoAvailableFeatures = new ArrayMap();
        try {
            FileReader permReader = new FileReader(file);
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
                }
                loop1:
                while (true) {
                    XmlUtils.nextElement(parser);
                    if (parser.getEventType() == 1) {
                        break loop1;
                    }
                    if ("feature".equals(parser.getName())) {
                        String fname = parser.getAttributeValue(null, "name");
                        if (fname == null) {
                            Slog.w(TAG, "<feature> without name in " + file + " at " + parser.getPositionDescription());
                        } else {
                            Slog.i(TAG, "Got feature " + fname);
                            FeatureInfo fi = new FeatureInfo();
                            fi.name = fname;
                            mOppoAvailableFeatures.put(fname, fi);
                        }
                        XmlUtils.skipCurrentTag(parser);
                    } else {
                        XmlUtils.skipCurrentTag(parser);
                    }
                }
                return mOppoAvailableFeatures;
            } catch (XmlPullParserException e) {
                Slog.w(TAG, "Got exception parsing permissions.", e);
            } catch (IOException e2) {
                Slog.w(TAG, "Got exception parsing permissions.", e2);
            } finally {
                IoUtils.closeQuietly(permReader);
            }
        } catch (FileNotFoundException e3) {
            Slog.w(TAG, "Couldn't find or open permissions file " + file);
            return null;
        }
    }

    void readOppoOperatorFeature(File operateDir, int permissionFlag) {
        if (!operateDir.exists() || !operateDir.isDirectory()) {
            if (permissionFlag == -1) {
                Slog.w(TAG, "No directory " + operateDir + ", skipping");
            }
        } else if (operateDir.canRead()) {
            File operateFile = null;
            String regionName = SystemProperties.get("persist.sys.oppo.region");
            String operateName = SystemProperties.get("ro.oppo.operator");
            if (operateName == null || operateName.length() <= 0 || regionName == null || regionName.length() <= 0) {
                Slog.w(TAG, "operateName is null !");
                return;
            }
            String featureName = regionName + "." + operateName;
            Slog.w(TAG, "featureName " + featureName + " ok!");
            for (File f : operateDir.listFiles()) {
                if (f.getPath().contains(featureName)) {
                    operateFile = f;
                    break;
                }
            }
            if (operateFile != null) {
                Slog.w(TAG, "operateFile " + operateFile + " ok!");
                readPermissionsFromXml(operateFile, permissionFlag);
            }
        } else {
            Slog.w(TAG, "Directory " + operateDir + " cannot be read");
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:22:0x0086 A:{SYNTHETIC, Splitter: B:22:0x0086} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static String readStringFromFile(String path) {
        IOException e;
        Throwable th;
        File file = new File(path);
        BufferedReader reader = null;
        String tempString = PhoneConstants.MVNO_TYPE_NONE;
        try {
            BufferedReader reader2 = new BufferedReader(new FileReader(file));
            try {
                tempString = reader2.readLine();
                if (reader2 != null) {
                    try {
                        reader2.close();
                    } catch (IOException e1) {
                        Slog.e(TAG, "io close exception :" + e1.getMessage());
                    }
                }
                reader = reader2;
            } catch (IOException e2) {
                e = e2;
                reader = reader2;
            } catch (Throwable th2) {
                th = th2;
                reader = reader2;
                if (reader != null) {
                }
                throw th;
            }
        } catch (IOException e3) {
            e = e3;
            try {
                Slog.e(TAG, "io exception:" + e.getMessage());
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e12) {
                        Slog.e(TAG, "io close exception :" + e12.getMessage());
                    }
                }
                return tempString;
            } catch (Throwable th3) {
                th = th3;
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e122) {
                        Slog.e(TAG, "io close exception :" + e122.getMessage());
                    }
                }
                throw th;
            }
        }
        return tempString;
    }

    private boolean is_special_sensor() {
        if (new File(SPECIAL_SENSOR_PATH).exists() && readStringFromFile(SPECIAL_SENSOR_PATH).contains(SPECIAL_SENSOR)) {
            return true;
        }
        return false;
    }

    void readPermissions(File libraryDir, int permissionFlag) {
        if (!libraryDir.exists() || !libraryDir.isDirectory()) {
            if (permissionFlag == -1) {
                Slog.w(TAG, "No directory " + libraryDir + ", skipping");
            }
        } else if (libraryDir.canRead()) {
            File platformFile = null;
            String romFeature = SystemProperties.get("ro.rom.featrue", "allnet");
            String romTestFeature = SystemProperties.get("ro.rom.test.featrue", "allnetcmccdeeptest");
            for (File f : libraryDir.listFiles()) {
                if (f.getPath().endsWith("etc/permissions/platform.xml")) {
                    platformFile = f;
                } else if (!f.getPath().endsWith(".xml")) {
                    Slog.i(TAG, "Non-xml file " + f + " in " + libraryDir + " directory, ignoring");
                } else if (!f.canRead()) {
                    Slog.w(TAG, "Permissions library file " + f + " cannot be read");
                } else if (!PERMISSION_XML_CTTEST.equals(f.getPath()) && !PERMISSION_XML_CUTEST.equals(f.getPath()) && f.getPath().startsWith("/system/etc/permissions/com.oppo.rom.") && !f.getPath().endsWith(romFeature + ".xml") && !f.getPath().endsWith(romTestFeature + ".xml") && !PERMISSION_XML_ROM.equals(f.getPath())) {
                    Slog.i(TAG, "scan feature file : " + f.getPath() + ",ignore!!!");
                } else if (f.getPath().startsWith("/system/etc/permissions/com.oppo.features.allnet.common.") && !f.getPath().endsWith(SystemProperties.get("ro.commonsoft.product", OppoThemeResources.OPPO_PACKAGE) + ".xml")) {
                    Slog.i(TAG, "scan feature file : " + f.getPath() + ",ignore!!!");
                } else if (!SPECIAL_SENSOR_PERMISSION_XML.equals(f.getPath()) || is_special_sensor()) {
                    readPermissionsFromXml(f, permissionFlag);
                } else {
                    Slog.i(TAG, "scan feature file : " + f.getPath() + ",ignore!!!");
                }
            }
            if (platformFile != null) {
                readPermissionsFromXml(platformFile, permissionFlag);
            }
        } else {
            Slog.w(TAG, "Directory " + libraryDir + " cannot be read");
        }
    }

    private void readPermissionsFromXml(File permFile, int permissionFlag) {
        try {
            Reader fileReader = new FileReader(permFile);
            boolean lowRam = ActivityManager.isLowRamDeviceStatic();
            try {
                int type;
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(fileReader);
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
                } else if (parser.getName().equals("permissions") || parser.getName().equals("config")) {
                    boolean allowAll = permissionFlag == -1;
                    boolean allowLibs = (permissionFlag & 2) != 0;
                    boolean allowFeatures = (permissionFlag & 1) != 0;
                    boolean allowPermissions = (permissionFlag & 4) != 0;
                    boolean allowAppConfigs = (permissionFlag & 8) != 0;
                    loop2:
                    while (true) {
                        XmlUtils.nextElement(parser);
                        if (parser.getEventType() == 1) {
                            break loop2;
                        }
                        String name = parser.getName();
                        String perm;
                        String fname;
                        String pkgname;
                        if ("group".equals(name) && allowAll) {
                            String gidStr = parser.getAttributeValue(null, PhoneConstants.MVNO_TYPE_GID);
                            if (gidStr != null) {
                                this.mGlobalGids = ArrayUtils.appendInt(this.mGlobalGids, Process.getGidForName(gidStr));
                            } else {
                                Slog.w(TAG, "<group> without gid in " + permFile + " at " + parser.getPositionDescription());
                            }
                            XmlUtils.skipCurrentTag(parser);
                        } else if ("permission".equals(name) && allowPermissions) {
                            perm = parser.getAttributeValue(null, "name");
                            if (perm == null) {
                                Slog.w(TAG, "<permission> without name in " + permFile + " at " + parser.getPositionDescription());
                                XmlUtils.skipCurrentTag(parser);
                            } else {
                                readPermission(parser, perm.intern());
                            }
                        } else if ("assign-permission".equals(name) && allowPermissions) {
                            perm = parser.getAttributeValue(null, "name");
                            if (perm == null) {
                                Slog.w(TAG, "<assign-permission> without name in " + permFile + " at " + parser.getPositionDescription());
                                XmlUtils.skipCurrentTag(parser);
                            } else {
                                String uidStr = parser.getAttributeValue(null, "uid");
                                if (uidStr == null) {
                                    Slog.w(TAG, "<assign-permission> without uid in " + permFile + " at " + parser.getPositionDescription());
                                    XmlUtils.skipCurrentTag(parser);
                                } else {
                                    int uid = Process.getUidForName(uidStr);
                                    if (uid < 0) {
                                        Slog.w(TAG, "<assign-permission> with unknown uid \"" + uidStr + "  in " + permFile + " at " + parser.getPositionDescription());
                                        XmlUtils.skipCurrentTag(parser);
                                    } else {
                                        perm = perm.intern();
                                        ArraySet<String> perms = (ArraySet) this.mSystemPermissions.get(uid);
                                        if (perms == null) {
                                            perms = new ArraySet();
                                            this.mSystemPermissions.put(uid, perms);
                                        }
                                        perms.add(perm);
                                        XmlUtils.skipCurrentTag(parser);
                                    }
                                }
                            }
                        } else if ("library".equals(name) && allowLibs) {
                            String lname = parser.getAttributeValue(null, "name");
                            String lfile = parser.getAttributeValue(null, "file");
                            if (lname == null) {
                                Slog.w(TAG, "<library> without name in " + permFile + " at " + parser.getPositionDescription());
                            } else if (lfile == null) {
                                Slog.w(TAG, "<library> without file in " + permFile + " at " + parser.getPositionDescription());
                            } else {
                                this.mSharedLibraries.put(lname, lfile);
                            }
                            XmlUtils.skipCurrentTag(parser);
                        } else if ("feature".equals(name) && allowFeatures) {
                            boolean allowed;
                            fname = parser.getAttributeValue(null, "name");
                            int fversion = XmlUtils.readIntAttribute(parser, OppoDaemonListHelper.TAG_VERSION, 0);
                            if (lowRam) {
                                allowed = !"true".equals(parser.getAttributeValue(null, "notLowRam"));
                            } else {
                                allowed = true;
                            }
                            if (fname == null) {
                                Slog.w(TAG, "<feature> without name in " + permFile + " at " + parser.getPositionDescription());
                            } else if (allowed) {
                                addFeature(fname, fversion);
                            }
                            XmlUtils.skipCurrentTag(parser);
                        } else if ("unavailable-feature".equals(name) && allowFeatures) {
                            fname = parser.getAttributeValue(null, "name");
                            if (fname == null) {
                                Slog.w(TAG, "<unavailable-feature> without name in " + permFile + " at " + parser.getPositionDescription());
                            } else {
                                this.mUnavailableFeatures.add(fname);
                            }
                            XmlUtils.skipCurrentTag(parser);
                        } else if ("allow-in-power-save-except-idle".equals(name) && allowAll) {
                            pkgname = parser.getAttributeValue(null, "package");
                            if (pkgname == null) {
                                Slog.w(TAG, "<allow-in-power-save-except-idle> without package in " + permFile + " at " + parser.getPositionDescription());
                            } else {
                                this.mAllowInPowerSaveExceptIdle.add(pkgname);
                            }
                            XmlUtils.skipCurrentTag(parser);
                        } else if ("allow-in-power-save".equals(name) && allowAll) {
                            pkgname = parser.getAttributeValue(null, "package");
                            if (pkgname == null) {
                                Slog.w(TAG, "<allow-in-power-save> without package in " + permFile + " at " + parser.getPositionDescription());
                            } else {
                                this.mAllowInPowerSave.add(pkgname);
                            }
                            XmlUtils.skipCurrentTag(parser);
                        } else if ("allow-in-data-usage-save".equals(name) && allowAll) {
                            pkgname = parser.getAttributeValue(null, "package");
                            if (pkgname == null) {
                                Slog.w(TAG, "<allow-in-data-usage-save> without package in " + permFile + " at " + parser.getPositionDescription());
                            } else {
                                this.mAllowInDataUsageSave.add(pkgname);
                            }
                            XmlUtils.skipCurrentTag(parser);
                        } else if ("app-link".equals(name) && allowAppConfigs) {
                            pkgname = parser.getAttributeValue(null, "package");
                            if (pkgname == null) {
                                Slog.w(TAG, "<app-link> without package in " + permFile + " at " + parser.getPositionDescription());
                            } else {
                                this.mLinkedApps.add(pkgname);
                            }
                            XmlUtils.skipCurrentTag(parser);
                        } else if ("system-user-whitelisted-app".equals(name) && allowAppConfigs) {
                            pkgname = parser.getAttributeValue(null, "package");
                            if (pkgname == null) {
                                Slog.w(TAG, "<system-user-whitelisted-app> without package in " + permFile + " at " + parser.getPositionDescription());
                            } else {
                                this.mSystemUserWhitelistedApps.add(pkgname);
                            }
                            XmlUtils.skipCurrentTag(parser);
                        } else if ("system-user-blacklisted-app".equals(name) && allowAppConfigs) {
                            pkgname = parser.getAttributeValue(null, "package");
                            if (pkgname == null) {
                                Slog.w(TAG, "<system-user-blacklisted-app without package in " + permFile + " at " + parser.getPositionDescription());
                            } else {
                                this.mSystemUserBlacklistedApps.add(pkgname);
                            }
                            XmlUtils.skipCurrentTag(parser);
                        } else if ("default-enabled-vr-app".equals(name) && allowAppConfigs) {
                            pkgname = parser.getAttributeValue(null, "package");
                            String clsname = parser.getAttributeValue(null, "class");
                            if (pkgname == null) {
                                Slog.w(TAG, "<default-enabled-vr-app without package in " + permFile + " at " + parser.getPositionDescription());
                            } else if (clsname == null) {
                                Slog.w(TAG, "<default-enabled-vr-app without class in " + permFile + " at " + parser.getPositionDescription());
                            } else {
                                this.mDefaultVrComponents.add(new ComponentName(pkgname, clsname));
                            }
                            XmlUtils.skipCurrentTag(parser);
                        } else if ("backup-transport-whitelisted-service".equals(name) && allowFeatures) {
                            String serviceName = parser.getAttributeValue(null, TileService.EXTRA_SERVICE);
                            if (serviceName == null) {
                                Slog.w(TAG, "<backup-transport-whitelisted-service> without service in " + permFile + " at " + parser.getPositionDescription());
                            } else {
                                ComponentName cn = ComponentName.unflattenFromString(serviceName);
                                if (cn == null) {
                                    Slog.w(TAG, "<backup-transport-whitelisted-service> with invalid service name " + serviceName + " in " + permFile + " at " + parser.getPositionDescription());
                                } else {
                                    this.mBackupTransportWhitelist.add(cn);
                                }
                            }
                            XmlUtils.skipCurrentTag(parser);
                        } else if ("disabled-until-used-preinstalled-carrier-associated-app".equals(name) && allowAppConfigs) {
                            pkgname = parser.getAttributeValue(null, "package");
                            String carrierPkgname = parser.getAttributeValue(null, "carrierAppPackage");
                            if (pkgname == null || carrierPkgname == null) {
                                Slog.w(TAG, "<disabled-until-used-preinstalled-carrier-associated-app without package or carrierAppPackage in " + permFile + " at " + parser.getPositionDescription());
                            } else {
                                List<String> associatedPkgs = (List) this.mDisabledUntilUsedPreinstalledCarrierAssociatedApps.get(carrierPkgname);
                                if (associatedPkgs == null) {
                                    associatedPkgs = new ArrayList();
                                    this.mDisabledUntilUsedPreinstalledCarrierAssociatedApps.put(carrierPkgname, associatedPkgs);
                                }
                                associatedPkgs.add(pkgname);
                            }
                            XmlUtils.skipCurrentTag(parser);
                        } else {
                            XmlUtils.skipCurrentTag(parser);
                        }
                    }
                    if (StorageManager.isFileEncryptedNativeOnly()) {
                        addFeature("android.software.file_based_encryption", 0);
                        addFeature("android.software.securely_removes_users", 0);
                    }
                    for (String featureName : this.mUnavailableFeatures) {
                        removeFeature(featureName);
                    }
                } else {
                    throw new XmlPullParserException("Unexpected start tag in " + permFile + ": found " + parser.getName() + ", expected 'permissions' or 'config'");
                }
            } catch (XmlPullParserException e) {
                Slog.w(TAG, "Got exception parsing permissions.", e);
            } catch (IOException e2) {
                Slog.w(TAG, "Got exception parsing permissions.", e2);
            } finally {
                IoUtils.closeQuietly(fileReader);
            }
        } catch (FileNotFoundException e3) {
            Slog.w(TAG, "Couldn't find or open permissions file " + permFile);
        }
    }

    private void addFeature(String name, int version) {
        FeatureInfo fi = (FeatureInfo) this.mAvailableFeatures.get(name);
        if (fi == null) {
            fi = new FeatureInfo();
            fi.name = name;
            fi.version = version;
            this.mAvailableFeatures.put(name, fi);
            return;
        }
        fi.version = Math.max(fi.version, version);
    }

    private void removeFeature(String name) {
        if (this.mAvailableFeatures.remove(name) != null) {
            Slog.d(TAG, "Removed unavailable feature " + name);
        }
    }

    void readPermission(XmlPullParser parser, String name) throws IOException, XmlPullParserException {
        if (this.mPermissions.containsKey(name)) {
            throw new IllegalStateException("Duplicate permission definition for " + name);
        }
        PermissionEntry perm = new PermissionEntry(name, XmlUtils.readBooleanAttribute(parser, "perUser", false));
        this.mPermissions.put(name, perm);
        int outerDepth = parser.getDepth();
        while (true) {
            int type = parser.next();
            if (type == 1) {
                return;
            }
            if (type == 3 && parser.getDepth() <= outerDepth) {
                return;
            }
            if (!(type == 3 || type == 4)) {
                if ("group".equals(parser.getName())) {
                    String gidStr = parser.getAttributeValue(null, PhoneConstants.MVNO_TYPE_GID);
                    if (gidStr != null) {
                        perm.gids = ArrayUtils.appendInt(perm.gids, Process.getGidForName(gidStr));
                    } else {
                        Slog.w(TAG, "<group> without gid at " + parser.getPositionDescription());
                    }
                }
                XmlUtils.skipCurrentTag(parser);
            }
        }
    }
}
