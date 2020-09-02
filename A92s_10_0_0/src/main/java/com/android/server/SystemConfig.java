package com.android.server;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.pm.FeatureInfo;
import android.content.pm.PackageManager;
import android.net.wifi.WifiConfiguration;
import android.os.Build;
import android.os.Environment;
import android.os.Process;
import android.os.SystemProperties;
import android.os.storage.StorageManager;
import android.permission.PermissionManager;
import android.provider.SettingsStringUtil;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.Slog;
import android.util.SparseArray;
import android.util.Xml;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.XmlUtils;
import com.color.antivirus.tencent.TRPEngManager;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import libcore.io.IoUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class SystemConfig extends OppoBaseSystemConfig {
    static final int ALLOW_ALL = -1;
    private static final int ALLOW_APP_CONFIGS = 8;
    private static final int ALLOW_ASSOCIATIONS = 128;
    private static final int ALLOW_FEATURES = 1;
    private static final int ALLOW_HIDDENAPI_WHITELISTING = 64;
    private static final int ALLOW_LIBS = 2;
    private static final int ALLOW_OEM_PERMISSIONS = 32;
    private static final int ALLOW_PERMISSIONS = 4;
    private static final int ALLOW_PRIVAPP_PERMISSIONS = 16;
    private static final String SKU_PROPERTY = "ro.boot.product.hardware.sku";
    static final String TAG = "SystemConfig";
    static SystemConfig sInstance;
    final ArraySet<String> mAllowIgnoreLocationSettings = new ArraySet<>();
    final ArraySet<String> mAllowImplicitBroadcasts = new ArraySet<>();
    final ArraySet<String> mAllowInDataUsageSave = new ArraySet<>();
    final ArraySet<String> mAllowInPowerSave = new ArraySet<>();
    final ArraySet<String> mAllowInPowerSaveExceptIdle = new ArraySet<>();
    final ArraySet<String> mAllowUnthrottledLocation = new ArraySet<>();
    final ArrayMap<String, ArraySet<String>> mAllowedAssociations = new ArrayMap<>();
    final ArrayMap<String, FeatureInfo> mAvailableFeatures = new ArrayMap<>();
    final ArraySet<ComponentName> mBackupTransportWhitelist = new ArraySet<>();
    private final ArraySet<String> mBugreportWhitelistedPackages = new ArraySet<>();
    final ArraySet<ComponentName> mDefaultVrComponents = new ArraySet<>();
    final ArraySet<String> mDisabledUntilUsedPreinstalledCarrierApps = new ArraySet<>();
    final ArrayMap<String, List<String>> mDisabledUntilUsedPreinstalledCarrierAssociatedApps = new ArrayMap<>();
    int[] mGlobalGids;
    final ArraySet<String> mHiddenApiPackageWhitelist = new ArraySet<>();
    final ArraySet<String> mLinkedApps = new ArraySet<>();
    final ArrayMap<String, ArrayMap<String, Boolean>> mOemPermissions = new ArrayMap<>();
    final ArrayMap<String, PermissionEntry> mPermissions = new ArrayMap<>();
    final ArrayMap<String, ArraySet<String>> mPrivAppDenyPermissions = new ArrayMap<>();
    final ArrayMap<String, ArraySet<String>> mPrivAppPermissions = new ArrayMap<>();
    final ArrayMap<String, ArraySet<String>> mProductPrivAppDenyPermissions = new ArrayMap<>();
    final ArrayMap<String, ArraySet<String>> mProductPrivAppPermissions = new ArrayMap<>();
    final ArrayMap<String, ArraySet<String>> mProductServicesPrivAppDenyPermissions = new ArrayMap<>();
    final ArrayMap<String, ArraySet<String>> mProductServicesPrivAppPermissions = new ArrayMap<>();
    final ArrayMap<String, SharedLibraryEntry> mSharedLibraries = new ArrayMap<>();
    final ArrayList<PermissionManager.SplitPermissionInfo> mSplitPermissions = new ArrayList<>();
    final SparseArray<ArraySet<String>> mSystemPermissions = new SparseArray<>();
    final ArraySet<String> mSystemUserBlacklistedApps = new ArraySet<>();
    final ArraySet<String> mSystemUserWhitelistedApps = new ArraySet<>();
    final ArraySet<String> mUnavailableFeatures = new ArraySet<>();
    final ArrayMap<String, ArraySet<String>> mVendorPrivAppDenyPermissions = new ArrayMap<>();
    final ArrayMap<String, ArraySet<String>> mVendorPrivAppPermissions = new ArrayMap<>();

    public static final class SharedLibraryEntry {
        public final String[] dependencies;
        public final String filename;
        public final String name;

        SharedLibraryEntry(String name2, String filename2, String[] dependencies2) {
            this.name = name2;
            this.filename = filename2;
            this.dependencies = dependencies2;
        }
    }

    public static final class PermissionEntry {
        public int[] gids;
        public final String name;
        public boolean perUser;

        PermissionEntry(String name2, boolean perUser2) {
            this.name = name2;
            this.perUser = perUser2;
        }
    }

    public static SystemConfig getInstance() {
        SystemConfig systemConfig;
        synchronized (SystemConfig.class) {
            if (sInstance == null) {
                sInstance = new OppoSystemConfig();
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

    public ArrayList<PermissionManager.SplitPermissionInfo> getSplitPermissions() {
        return this.mSplitPermissions;
    }

    public ArrayMap<String, SharedLibraryEntry> getSharedLibraries() {
        return this.mSharedLibraries;
    }

    public ArrayMap<String, FeatureInfo> getAvailableFeatures() {
        return this.mAvailableFeatures;
    }

    public ArrayMap<String, PermissionEntry> getPermissions() {
        return this.mPermissions;
    }

    public ArraySet<String> getAllowImplicitBroadcasts() {
        return this.mAllowImplicitBroadcasts;
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

    public ArraySet<String> getAllowUnthrottledLocation() {
        return this.mAllowUnthrottledLocation;
    }

    public ArraySet<String> getAllowIgnoreLocationSettings() {
        return this.mAllowIgnoreLocationSettings;
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

    public ArraySet<String> getHiddenApiWhitelistedApps() {
        return this.mHiddenApiPackageWhitelist;
    }

    public ArraySet<ComponentName> getDefaultVrComponents() {
        return this.mDefaultVrComponents;
    }

    public ArraySet<ComponentName> getBackupTransportWhitelist() {
        return this.mBackupTransportWhitelist;
    }

    public ArraySet<String> getDisabledUntilUsedPreinstalledCarrierApps() {
        return this.mDisabledUntilUsedPreinstalledCarrierApps;
    }

    public ArrayMap<String, List<String>> getDisabledUntilUsedPreinstalledCarrierAssociatedApps() {
        return this.mDisabledUntilUsedPreinstalledCarrierAssociatedApps;
    }

    public ArraySet<String> getPrivAppPermissions(String packageName) {
        return this.mPrivAppPermissions.get(packageName);
    }

    public ArraySet<String> getPrivAppDenyPermissions(String packageName) {
        return this.mPrivAppDenyPermissions.get(packageName);
    }

    public ArraySet<String> getVendorPrivAppPermissions(String packageName) {
        return this.mVendorPrivAppPermissions.get(packageName);
    }

    public ArraySet<String> getVendorPrivAppDenyPermissions(String packageName) {
        return this.mVendorPrivAppDenyPermissions.get(packageName);
    }

    public ArraySet<String> getProductPrivAppPermissions(String packageName) {
        return this.mProductPrivAppPermissions.get(packageName);
    }

    public ArraySet<String> getProductPrivAppDenyPermissions(String packageName) {
        return this.mProductPrivAppDenyPermissions.get(packageName);
    }

    public ArraySet<String> getProductServicesPrivAppPermissions(String packageName) {
        return this.mProductServicesPrivAppPermissions.get(packageName);
    }

    public ArraySet<String> getProductServicesPrivAppDenyPermissions(String packageName) {
        return this.mProductServicesPrivAppDenyPermissions.get(packageName);
    }

    public Map<String, Boolean> getOemPermissions(String packageName) {
        Map<String, Boolean> oemPermissions = this.mOemPermissions.get(packageName);
        if (oemPermissions != null) {
            return oemPermissions;
        }
        return Collections.emptyMap();
    }

    public ArrayMap<String, ArraySet<String>> getAllowedAssociations() {
        return this.mAllowedAssociations;
    }

    public ArraySet<String> getBugreportWhitelistedPackages() {
        return this.mBugreportWhitelistedPackages;
    }

    SystemConfig() {
        readPermissions(Environment.buildPath(Environment.getRootDirectory(), "etc", "sysconfig"), -1);
        readPermissions(Environment.buildPath(Environment.getRootDirectory(), "etc", "permissions"), -1);
        int vendorPermissionFlag = Build.VERSION.FIRST_SDK_INT <= 27 ? 147 | 12 : 147;
        readPermissions(Environment.buildPath(Environment.getVendorDirectory(), "etc", "sysconfig"), vendorPermissionFlag);
        readPermissions(Environment.buildPath(Environment.getVendorDirectory(), "etc", "permissions"), vendorPermissionFlag);
        readPermissions(Environment.buildPath(Environment.getOdmDirectory(), "etc", "sysconfig"), vendorPermissionFlag);
        readPermissions(Environment.buildPath(Environment.getOdmDirectory(), "etc", "permissions"), vendorPermissionFlag);
        String skuProperty = SystemProperties.get(SKU_PROPERTY, "");
        if (!skuProperty.isEmpty()) {
            String skuDir = "sku_" + skuProperty;
            readPermissions(Environment.buildPath(Environment.getOdmDirectory(), "etc", "sysconfig", skuDir), vendorPermissionFlag);
            readPermissions(Environment.buildPath(Environment.getOdmDirectory(), "etc", "permissions", skuDir), vendorPermissionFlag);
        }
        readPermissions(Environment.buildPath(Environment.getOemDirectory(), "etc", "sysconfig"), 161);
        readPermissions(Environment.buildPath(Environment.getOemDirectory(), "etc", "permissions"), 161);
        readPermissions(Environment.buildPath(Environment.getProductDirectory(), "etc", "sysconfig"), -1);
        readPermissions(Environment.buildPath(Environment.getProductDirectory(), "etc", "permissions"), -1);
        readPermissions(Environment.buildPath(Environment.getProductServicesDirectory(), "etc", "sysconfig"), -1);
        readPermissions(Environment.buildPath(Environment.getProductServicesDirectory(), "etc", "permissions"), -1);
    }

    /* access modifiers changed from: package-private */
    public void readPermissions(File libraryDir, int permissionFlag) {
        if (!libraryDir.exists() || !libraryDir.isDirectory()) {
            if (permissionFlag == -1) {
                Slog.w(TAG, "No directory " + libraryDir + ", skipping");
            }
        } else if (!libraryDir.canRead()) {
            Slog.w(TAG, "Directory " + libraryDir + " cannot be read");
        } else {
            File platformFile = null;
            File[] listFiles = libraryDir.listFiles();
            for (File f : listFiles) {
                if (f.isFile()) {
                    if (f.getPath().endsWith("etc/permissions/platform.xml")) {
                        platformFile = f;
                    } else if (!f.getPath().endsWith(".xml")) {
                        Slog.i(TAG, "Non-xml file " + f + " in " + libraryDir + " directory, ignoring");
                    } else if (!f.canRead()) {
                        Slog.w(TAG, "Permissions library file " + f + " cannot be read");
                    } else if (!filterOppoFeatureFile(f)) {
                        readPermissionsFromXml(f, permissionFlag);
                    }
                }
            }
            if (platformFile != null) {
                readPermissionsFromXml(platformFile, permissionFlag);
            }
        }
    }

    private void logNotAllowedInPartition(String name, File permFile, XmlPullParser parser) {
        Slog.w(TAG, "<" + name + "> not allowed in partition of " + permFile + " at " + parser.getPositionDescription());
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    /* access modifiers changed from: package-private */
    /* JADX WARNING: Removed duplicated region for block: B:11:0x002d A[Catch:{ XmlPullParserException -> 0x0aa7, IOException -> 0x0a98, all -> 0x0a90, all -> 0x0afb }] */
    /* JADX WARNING: Removed duplicated region for block: B:188:0x040b A[Catch:{ XmlPullParserException -> 0x0a8b, IOException -> 0x0a88 }] */
    /* JADX WARNING: Removed duplicated region for block: B:189:0x0413 A[Catch:{ XmlPullParserException -> 0x0a8b, IOException -> 0x0a88 }] */
    /* JADX WARNING: Removed duplicated region for block: B:385:0x0a78 A[Catch:{ XmlPullParserException -> 0x0a8b, IOException -> 0x0a88 }] */
    /* JADX WARNING: Removed duplicated region for block: B:404:0x0abb  */
    /* JADX WARNING: Removed duplicated region for block: B:405:0x0ac7  */
    /* JADX WARNING: Removed duplicated region for block: B:408:0x0ace  */
    /* JADX WARNING: Removed duplicated region for block: B:411:0x0ad9  */
    /* JADX WARNING: Removed duplicated region for block: B:412:0x0adf  */
    /* JADX WARNING: Removed duplicated region for block: B:416:0x0af0 A[LOOP:2: B:414:0x0aea->B:416:0x0af0, LOOP_END] */
    public void readPermissionsFromXml(File permFile, int permissionFlag) {
        FileReader permReader;
        Throwable th;
        int i;
        Iterator<String> it;
        XmlPullParserException e;
        String str;
        IOException e2;
        int type;
        char c;
        int i2;
        boolean allowed;
        boolean vendor2;
        String str2 = "Got exception parsing permissions.";
        try {
            FileReader permReader2 = new FileReader(permFile);
            boolean lowRam = ActivityManager.isLowRamDeviceStatic();
            try {
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(permReader2);
                while (true) {
                    int next = parser.next();
                    type = next;
                    int i3 = 1;
                    if (next == 2 || type == 1) {
                        if (type != 2) {
                            if (!parser.getName().equals("permissions")) {
                                try {
                                    if (!parser.getName().equals(TRPEngManager.CONFIG)) {
                                        throw new XmlPullParserException("Unexpected start tag in " + permFile + ": found " + parser.getName() + ", expected 'permissions' or 'config'");
                                    }
                                } catch (XmlPullParserException e3) {
                                    e = e3;
                                    permReader = permReader2;
                                    Slog.w(TAG, str2, e);
                                    IoUtils.closeQuietly(permReader);
                                    if (StorageManager.isFileEncryptedNativeOnly()) {
                                    }
                                    if (StorageManager.hasAdoptable()) {
                                    }
                                    if (ActivityManager.isLowRamDeviceStatic()) {
                                    }
                                    it = this.mUnavailableFeatures.iterator();
                                    while (it.hasNext()) {
                                    }
                                } catch (IOException e4) {
                                    e2 = e4;
                                    str = str2;
                                    permReader = permReader2;
                                    Slog.w(TAG, str, e2);
                                    IoUtils.closeQuietly(permReader);
                                    if (StorageManager.isFileEncryptedNativeOnly()) {
                                    }
                                    if (StorageManager.hasAdoptable()) {
                                    }
                                    if (ActivityManager.isLowRamDeviceStatic()) {
                                    }
                                    it = this.mUnavailableFeatures.iterator();
                                    while (it.hasNext()) {
                                    }
                                } catch (Throwable th2) {
                                    th = th2;
                                    permReader = permReader2;
                                    IoUtils.closeQuietly(permReader);
                                    throw th;
                                }
                            }
                            boolean allowAll = permissionFlag == -1;
                            boolean allowLibs = (permissionFlag & 2) != 0;
                            boolean allowFeatures = (permissionFlag & 1) != 0;
                            boolean allowPermissions = (permissionFlag & 4) != 0;
                            boolean allowAppConfigs = (permissionFlag & 8) != 0;
                            boolean allowPrivappPermissions = (permissionFlag & 16) != 0;
                            boolean allowOemPermissions = (permissionFlag & 32) != 0;
                            boolean allowApiWhitelisting = (permissionFlag & 64) != 0;
                            boolean allowAssociations = (permissionFlag & 128) != 0;
                            while (true) {
                                XmlUtils.nextElement(parser);
                                if (parser.getEventType() == i3) {
                                    IoUtils.closeQuietly(permReader2);
                                    if (StorageManager.isFileEncryptedNativeOnly()) {
                                        i = 0;
                                        addFeature(PackageManager.FEATURE_FILE_BASED_ENCRYPTION, 0);
                                        addFeature(PackageManager.FEATURE_SECURELY_REMOVES_USERS, 0);
                                    } else {
                                        i = 0;
                                    }
                                    if (StorageManager.hasAdoptable()) {
                                        addFeature(PackageManager.FEATURE_ADOPTABLE_STORAGE, i);
                                    }
                                    if (ActivityManager.isLowRamDeviceStatic()) {
                                        addFeature(PackageManager.FEATURE_RAM_LOW, i);
                                    } else {
                                        addFeature(PackageManager.FEATURE_RAM_NORMAL, i);
                                    }
                                    it = this.mUnavailableFeatures.iterator();
                                    while (it.hasNext()) {
                                        removeFeature(it.next());
                                    }
                                }
                                String name = parser.getName();
                                if (name == null) {
                                    XmlUtils.skipCurrentTag(parser);
                                } else {
                                    switch (name.hashCode()) {
                                        case -2040330235:
                                            if (name.equals("allow-unthrottled-location")) {
                                                c = 10;
                                                break;
                                            }
                                            c = 65535;
                                            break;
                                        case -1882490007:
                                            if (name.equals("allow-in-power-save")) {
                                                c = 8;
                                                break;
                                            }
                                            c = 65535;
                                            break;
                                        case -1005864890:
                                            if (name.equals("disabled-until-used-preinstalled-carrier-app")) {
                                                c = 19;
                                                break;
                                            }
                                            c = 65535;
                                            break;
                                        case -980620291:
                                            if (name.equals("allow-association")) {
                                                c = 23;
                                                break;
                                            }
                                            c = 65535;
                                            break;
                                        case -979207434:
                                            if (name.equals("feature")) {
                                                c = 5;
                                                break;
                                            }
                                            c = 65535;
                                            break;
                                        case -851582420:
                                            if (name.equals("system-user-blacklisted-app")) {
                                                c = 15;
                                                break;
                                            }
                                            c = 65535;
                                            break;
                                        case -828905863:
                                            if (name.equals("unavailable-feature")) {
                                                c = 6;
                                                break;
                                            }
                                            c = 65535;
                                            break;
                                        case -642819164:
                                            if (name.equals("allow-in-power-save-except-idle")) {
                                                c = 7;
                                                break;
                                            }
                                            c = 65535;
                                            break;
                                        case -560717308:
                                            if (name.equals("allow-ignore-location-settings")) {
                                                c = 11;
                                                break;
                                            }
                                            c = 65535;
                                            break;
                                        case -517618225:
                                            if (name.equals("permission")) {
                                                c = 1;
                                                break;
                                            }
                                            c = 65535;
                                            break;
                                        case 98629247:
                                            if (name.equals(WifiConfiguration.GroupCipher.varName)) {
                                                c = 0;
                                                break;
                                            }
                                            c = 65535;
                                            break;
                                        case 166208699:
                                            if (name.equals("library")) {
                                                c = 4;
                                                break;
                                            }
                                            c = 65535;
                                            break;
                                        case 180165796:
                                            if (name.equals("hidden-api-whitelisted-app")) {
                                                c = 22;
                                                break;
                                            }
                                            c = 65535;
                                            break;
                                        case 347247519:
                                            if (name.equals("backup-transport-whitelisted-service")) {
                                                c = 17;
                                                break;
                                            }
                                            c = 65535;
                                            break;
                                        case 508457430:
                                            if (name.equals("system-user-whitelisted-app")) {
                                                c = 14;
                                                break;
                                            }
                                            c = 65535;
                                            break;
                                        case 802332808:
                                            if (name.equals("allow-in-data-usage-save")) {
                                                c = 9;
                                                break;
                                            }
                                            c = 65535;
                                            break;
                                        case 953292141:
                                            if (name.equals("assign-permission")) {
                                                c = 2;
                                                break;
                                            }
                                            c = 65535;
                                            break;
                                        case 1044015374:
                                            if (name.equals("oem-permissions")) {
                                                c = 21;
                                                break;
                                            }
                                            c = 65535;
                                            break;
                                        case 1121420326:
                                            if (name.equals("app-link")) {
                                                c = 13;
                                                break;
                                            }
                                            c = 65535;
                                            break;
                                        case 1269564002:
                                            if (name.equals("split-permission")) {
                                                c = 3;
                                                break;
                                            }
                                            c = 65535;
                                            break;
                                        case 1567330472:
                                            if (name.equals("default-enabled-vr-app")) {
                                                c = 16;
                                                break;
                                            }
                                            c = 65535;
                                            break;
                                        case 1633270165:
                                            if (name.equals("disabled-until-used-preinstalled-carrier-associated-app")) {
                                                c = 18;
                                                break;
                                            }
                                            c = 65535;
                                            break;
                                        case 1723146313:
                                            if (name.equals("privapp-permissions")) {
                                                c = 20;
                                                break;
                                            }
                                            c = 65535;
                                            break;
                                        case 1723586945:
                                            if (name.equals("bugreport-whitelisted")) {
                                                c = 24;
                                                break;
                                            }
                                            c = 65535;
                                            break;
                                        case 1954925533:
                                            if (name.equals("allow-implicit-broadcast")) {
                                                c = 12;
                                                break;
                                            }
                                            c = 65535;
                                            break;
                                        default:
                                            c = 65535;
                                            break;
                                    }
                                    permReader = permReader2;
                                    str = str2;
                                    switch (c) {
                                        case 0:
                                            i2 = 1;
                                            if (allowAll) {
                                                String gidStr = parser.getAttributeValue(null, "gid");
                                                if (gidStr != null) {
                                                    this.mGlobalGids = ArrayUtils.appendInt(this.mGlobalGids, Process.getGidForName(gidStr));
                                                } else {
                                                    Slog.w(TAG, "<" + name + "> without gid in " + permFile + " at " + parser.getPositionDescription());
                                                }
                                            } else {
                                                logNotAllowedInPartition(name, permFile, parser);
                                            }
                                            XmlUtils.skipCurrentTag(parser);
                                            break;
                                        case 1:
                                            i2 = 1;
                                            if (!allowPermissions) {
                                                logNotAllowedInPartition(name, permFile, parser);
                                                XmlUtils.skipCurrentTag(parser);
                                                break;
                                            } else {
                                                String perm = parser.getAttributeValue(null, "name");
                                                if (perm != null) {
                                                    readPermission(parser, perm.intern());
                                                    break;
                                                } else {
                                                    Slog.w(TAG, "<" + name + "> without name in " + permFile + " at " + parser.getPositionDescription());
                                                    XmlUtils.skipCurrentTag(parser);
                                                    break;
                                                }
                                            }
                                        case 2:
                                            i2 = 1;
                                            if (allowPermissions) {
                                                String perm2 = parser.getAttributeValue(null, "name");
                                                if (perm2 == null) {
                                                    Slog.w(TAG, "<" + name + "> without name in " + permFile + " at " + parser.getPositionDescription());
                                                    XmlUtils.skipCurrentTag(parser);
                                                    break;
                                                } else {
                                                    String uidStr = parser.getAttributeValue(null, "uid");
                                                    if (uidStr == null) {
                                                        Slog.w(TAG, "<" + name + "> without uid in " + permFile + " at " + parser.getPositionDescription());
                                                        XmlUtils.skipCurrentTag(parser);
                                                        break;
                                                    } else {
                                                        int uid = Process.getUidForName(uidStr);
                                                        if (uid < 0) {
                                                            Slog.w(TAG, "<" + name + "> with unknown uid \"" + uidStr + "  in " + permFile + " at " + parser.getPositionDescription());
                                                            XmlUtils.skipCurrentTag(parser);
                                                            break;
                                                        } else {
                                                            String perm3 = perm2.intern();
                                                            ArraySet<String> perms = this.mSystemPermissions.get(uid);
                                                            if (perms == null) {
                                                                perms = new ArraySet<>();
                                                                this.mSystemPermissions.put(uid, perms);
                                                            }
                                                            perms.add(perm3);
                                                        }
                                                    }
                                                }
                                            } else {
                                                logNotAllowedInPartition(name, permFile, parser);
                                            }
                                            XmlUtils.skipCurrentTag(parser);
                                            break;
                                        case 3:
                                            i2 = 1;
                                            if (!allowPermissions) {
                                                logNotAllowedInPartition(name, permFile, parser);
                                                XmlUtils.skipCurrentTag(parser);
                                                break;
                                            } else {
                                                readSplitPermission(parser, permFile);
                                                break;
                                            }
                                        case 4:
                                            i2 = 1;
                                            if (allowLibs) {
                                                String lname = parser.getAttributeValue(null, "name");
                                                String lfile = parser.getAttributeValue(null, ContentResolver.SCHEME_FILE);
                                                String ldependency = parser.getAttributeValue(null, "dependency");
                                                if (lname == null) {
                                                    Slog.w(TAG, "<" + name + "> without name in " + permFile + " at " + parser.getPositionDescription());
                                                } else if (lfile == null) {
                                                    Slog.w(TAG, "<" + name + "> without file in " + permFile + " at " + parser.getPositionDescription());
                                                } else {
                                                    this.mSharedLibraries.put(lname, new SharedLibraryEntry(lname, lfile, ldependency == null ? new String[0] : ldependency.split(SettingsStringUtil.DELIMITER)));
                                                }
                                            } else {
                                                logNotAllowedInPartition(name, permFile, parser);
                                            }
                                            XmlUtils.skipCurrentTag(parser);
                                            break;
                                        case 5:
                                            if (allowFeatures) {
                                                String fname = parser.getAttributeValue(null, "name");
                                                int fversion = XmlUtils.readIntAttribute(parser, "version", 0);
                                                if (!lowRam) {
                                                    allowed = true;
                                                    i2 = 1;
                                                } else {
                                                    i2 = 1;
                                                    allowed = !"true".equals(parser.getAttributeValue(null, "notLowRam"));
                                                }
                                                if (fname == null) {
                                                    Slog.w(TAG, "<" + name + "> without name in " + permFile + " at " + parser.getPositionDescription());
                                                } else if (allowed) {
                                                    addFeature(fname, fversion);
                                                }
                                            } else {
                                                i2 = 1;
                                                logNotAllowedInPartition(name, permFile, parser);
                                            }
                                            XmlUtils.skipCurrentTag(parser);
                                            break;
                                        case 6:
                                            if (allowFeatures) {
                                                String fname2 = parser.getAttributeValue(null, "name");
                                                if (fname2 == null) {
                                                    Slog.w(TAG, "<" + name + "> without name in " + permFile + " at " + parser.getPositionDescription());
                                                } else {
                                                    this.mUnavailableFeatures.add(fname2);
                                                }
                                            } else {
                                                logNotAllowedInPartition(name, permFile, parser);
                                            }
                                            XmlUtils.skipCurrentTag(parser);
                                            i2 = 1;
                                            break;
                                        case 7:
                                            if (allowAll) {
                                                String pkgname = parser.getAttributeValue(null, "package");
                                                if (pkgname == null) {
                                                    Slog.w(TAG, "<" + name + "> without package in " + permFile + " at " + parser.getPositionDescription());
                                                } else {
                                                    this.mAllowInPowerSaveExceptIdle.add(pkgname);
                                                }
                                            } else {
                                                logNotAllowedInPartition(name, permFile, parser);
                                            }
                                            XmlUtils.skipCurrentTag(parser);
                                            i2 = 1;
                                            break;
                                        case 8:
                                            if (allowAll) {
                                                String pkgname2 = parser.getAttributeValue(null, "package");
                                                if (pkgname2 == null) {
                                                    Slog.w(TAG, "<" + name + "> without package in " + permFile + " at " + parser.getPositionDescription());
                                                } else {
                                                    this.mAllowInPowerSave.add(pkgname2);
                                                }
                                            } else {
                                                logNotAllowedInPartition(name, permFile, parser);
                                            }
                                            XmlUtils.skipCurrentTag(parser);
                                            i2 = 1;
                                            break;
                                        case 9:
                                            if (allowAll) {
                                                String pkgname3 = parser.getAttributeValue(null, "package");
                                                if (pkgname3 == null) {
                                                    Slog.w(TAG, "<" + name + "> without package in " + permFile + " at " + parser.getPositionDescription());
                                                } else {
                                                    this.mAllowInDataUsageSave.add(pkgname3);
                                                }
                                            } else {
                                                logNotAllowedInPartition(name, permFile, parser);
                                            }
                                            XmlUtils.skipCurrentTag(parser);
                                            i2 = 1;
                                            break;
                                        case 10:
                                            if (allowAll) {
                                                String pkgname4 = parser.getAttributeValue(null, "package");
                                                if (pkgname4 == null) {
                                                    Slog.w(TAG, "<" + name + "> without package in " + permFile + " at " + parser.getPositionDescription());
                                                } else {
                                                    this.mAllowUnthrottledLocation.add(pkgname4);
                                                }
                                            } else {
                                                logNotAllowedInPartition(name, permFile, parser);
                                            }
                                            XmlUtils.skipCurrentTag(parser);
                                            i2 = 1;
                                            break;
                                        case 11:
                                            if (allowAll) {
                                                String pkgname5 = parser.getAttributeValue(null, "package");
                                                if (pkgname5 == null) {
                                                    Slog.w(TAG, "<" + name + "> without package in " + permFile + " at " + parser.getPositionDescription());
                                                } else {
                                                    this.mAllowIgnoreLocationSettings.add(pkgname5);
                                                }
                                            } else {
                                                logNotAllowedInPartition(name, permFile, parser);
                                            }
                                            XmlUtils.skipCurrentTag(parser);
                                            i2 = 1;
                                            break;
                                        case 12:
                                            if (allowAll) {
                                                String action = parser.getAttributeValue(null, "action");
                                                if (action == null) {
                                                    Slog.w(TAG, "<" + name + "> without action in " + permFile + " at " + parser.getPositionDescription());
                                                } else {
                                                    this.mAllowImplicitBroadcasts.add(action);
                                                }
                                            } else {
                                                logNotAllowedInPartition(name, permFile, parser);
                                            }
                                            XmlUtils.skipCurrentTag(parser);
                                            i2 = 1;
                                            break;
                                        case 13:
                                            if (allowAppConfigs) {
                                                String pkgname6 = parser.getAttributeValue(null, "package");
                                                if (pkgname6 == null) {
                                                    Slog.w(TAG, "<" + name + "> without package in " + permFile + " at " + parser.getPositionDescription());
                                                } else {
                                                    this.mLinkedApps.add(pkgname6);
                                                }
                                            } else {
                                                logNotAllowedInPartition(name, permFile, parser);
                                            }
                                            XmlUtils.skipCurrentTag(parser);
                                            i2 = 1;
                                            break;
                                        case 14:
                                            if (allowAppConfigs) {
                                                String pkgname7 = parser.getAttributeValue(null, "package");
                                                if (pkgname7 == null) {
                                                    Slog.w(TAG, "<" + name + "> without package in " + permFile + " at " + parser.getPositionDescription());
                                                } else {
                                                    this.mSystemUserWhitelistedApps.add(pkgname7);
                                                }
                                            } else {
                                                logNotAllowedInPartition(name, permFile, parser);
                                            }
                                            XmlUtils.skipCurrentTag(parser);
                                            i2 = 1;
                                            break;
                                        case 15:
                                            if (allowAppConfigs) {
                                                String pkgname8 = parser.getAttributeValue(null, "package");
                                                if (pkgname8 == null) {
                                                    Slog.w(TAG, "<" + name + "> without package in " + permFile + " at " + parser.getPositionDescription());
                                                } else {
                                                    this.mSystemUserBlacklistedApps.add(pkgname8);
                                                }
                                            } else {
                                                logNotAllowedInPartition(name, permFile, parser);
                                            }
                                            XmlUtils.skipCurrentTag(parser);
                                            i2 = 1;
                                            break;
                                        case 16:
                                            if (allowAppConfigs) {
                                                String pkgname9 = parser.getAttributeValue(null, "package");
                                                String clsname = parser.getAttributeValue(null, "class");
                                                if (pkgname9 == null) {
                                                    Slog.w(TAG, "<" + name + "> without package in " + permFile + " at " + parser.getPositionDescription());
                                                } else if (clsname == null) {
                                                    Slog.w(TAG, "<" + name + "> without class in " + permFile + " at " + parser.getPositionDescription());
                                                } else {
                                                    this.mDefaultVrComponents.add(new ComponentName(pkgname9, clsname));
                                                }
                                            } else {
                                                logNotAllowedInPartition(name, permFile, parser);
                                            }
                                            XmlUtils.skipCurrentTag(parser);
                                            i2 = 1;
                                            break;
                                        case 17:
                                            if (allowFeatures) {
                                                String serviceName = parser.getAttributeValue(null, "service");
                                                if (serviceName == null) {
                                                    Slog.w(TAG, "<" + name + "> without service in " + permFile + " at " + parser.getPositionDescription());
                                                } else {
                                                    ComponentName cn2 = ComponentName.unflattenFromString(serviceName);
                                                    if (cn2 == null) {
                                                        Slog.w(TAG, "<" + name + "> with invalid service name " + serviceName + " in " + permFile + " at " + parser.getPositionDescription());
                                                    } else {
                                                        this.mBackupTransportWhitelist.add(cn2);
                                                    }
                                                }
                                            } else {
                                                logNotAllowedInPartition(name, permFile, parser);
                                            }
                                            XmlUtils.skipCurrentTag(parser);
                                            i2 = 1;
                                            break;
                                        case 18:
                                            if (allowAppConfigs) {
                                                String pkgname10 = parser.getAttributeValue(null, "package");
                                                String carrierPkgname = parser.getAttributeValue(null, "carrierAppPackage");
                                                if (pkgname10 != null) {
                                                    if (carrierPkgname != null) {
                                                        List<String> associatedPkgs = this.mDisabledUntilUsedPreinstalledCarrierAssociatedApps.get(carrierPkgname);
                                                        if (associatedPkgs == null) {
                                                            associatedPkgs = new ArrayList();
                                                            this.mDisabledUntilUsedPreinstalledCarrierAssociatedApps.put(carrierPkgname, associatedPkgs);
                                                        }
                                                        associatedPkgs.add(pkgname10);
                                                    }
                                                }
                                                Slog.w(TAG, "<" + name + "> without package or carrierAppPackage in " + permFile + " at " + parser.getPositionDescription());
                                            } else {
                                                logNotAllowedInPartition(name, permFile, parser);
                                            }
                                            XmlUtils.skipCurrentTag(parser);
                                            i2 = 1;
                                            break;
                                        case 19:
                                            if (allowAppConfigs) {
                                                String pkgname11 = parser.getAttributeValue(null, "package");
                                                if (pkgname11 == null) {
                                                    Slog.w(TAG, "<" + name + "> without package in " + permFile + " at " + parser.getPositionDescription());
                                                } else {
                                                    this.mDisabledUntilUsedPreinstalledCarrierApps.add(pkgname11);
                                                }
                                            } else {
                                                logNotAllowedInPartition(name, permFile, parser);
                                            }
                                            XmlUtils.skipCurrentTag(parser);
                                            i2 = 1;
                                            break;
                                        case 20:
                                            if (!allowPrivappPermissions) {
                                                logNotAllowedInPartition(name, permFile, parser);
                                                XmlUtils.skipCurrentTag(parser);
                                                i2 = 1;
                                                break;
                                            } else {
                                                if (!permFile.toPath().startsWith(Environment.getVendorDirectory().toPath() + "/")) {
                                                    if (!permFile.toPath().startsWith(Environment.getOdmDirectory().toPath() + "/")) {
                                                        vendor2 = false;
                                                        boolean product = permFile.toPath().startsWith(Environment.getProductDirectory().toPath() + "/");
                                                        boolean productServices = permFile.toPath().startsWith(Environment.getProductServicesDirectory().toPath() + "/");
                                                        permFile.toPath().startsWith(Environment.getOppoCustomDirectory().toPath());
                                                        if (!vendor2) {
                                                            readPrivAppPermissions(parser, this.mVendorPrivAppPermissions, this.mVendorPrivAppDenyPermissions);
                                                        } else if (product) {
                                                            readPrivAppPermissions(parser, this.mProductPrivAppPermissions, this.mProductPrivAppDenyPermissions);
                                                        } else if (productServices) {
                                                            readPrivAppPermissions(parser, this.mProductServicesPrivAppPermissions, this.mProductServicesPrivAppDenyPermissions);
                                                        } else {
                                                            readPrivAppPermissions(parser, this.mPrivAppPermissions, this.mPrivAppDenyPermissions);
                                                        }
                                                        i2 = 1;
                                                        break;
                                                    }
                                                }
                                                vendor2 = true;
                                                boolean product2 = permFile.toPath().startsWith(Environment.getProductDirectory().toPath() + "/");
                                                boolean productServices2 = permFile.toPath().startsWith(Environment.getProductServicesDirectory().toPath() + "/");
                                                permFile.toPath().startsWith(Environment.getOppoCustomDirectory().toPath());
                                                if (!vendor2) {
                                                }
                                                i2 = 1;
                                            }
                                        case 21:
                                            if (!allowOemPermissions) {
                                                logNotAllowedInPartition(name, permFile, parser);
                                                XmlUtils.skipCurrentTag(parser);
                                                i2 = 1;
                                                break;
                                            } else {
                                                readOemPermissions(parser);
                                                i2 = 1;
                                                break;
                                            }
                                        case 22:
                                            if (allowApiWhitelisting) {
                                                String pkgname12 = parser.getAttributeValue(null, "package");
                                                if (pkgname12 == null) {
                                                    Slog.w(TAG, "<" + name + "> without package in " + permFile + " at " + parser.getPositionDescription());
                                                } else {
                                                    this.mHiddenApiPackageWhitelist.add(pkgname12);
                                                }
                                            } else {
                                                logNotAllowedInPartition(name, permFile, parser);
                                            }
                                            XmlUtils.skipCurrentTag(parser);
                                            i2 = 1;
                                            break;
                                        case 23:
                                            if (allowAssociations) {
                                                String target = parser.getAttributeValue(null, "target");
                                                if (target == null) {
                                                    Slog.w(TAG, "<" + name + "> without target in " + permFile + " at " + parser.getPositionDescription());
                                                    XmlUtils.skipCurrentTag(parser);
                                                    i2 = 1;
                                                    break;
                                                } else {
                                                    String allowed2 = parser.getAttributeValue(null, "allowed");
                                                    if (allowed2 == null) {
                                                        Slog.w(TAG, "<" + name + "> without allowed in " + permFile + " at " + parser.getPositionDescription());
                                                        XmlUtils.skipCurrentTag(parser);
                                                        i2 = 1;
                                                        break;
                                                    } else {
                                                        String target2 = target.intern();
                                                        String allowed3 = allowed2.intern();
                                                        ArraySet<String> associations = this.mAllowedAssociations.get(target2);
                                                        if (associations == null) {
                                                            associations = new ArraySet<>();
                                                            this.mAllowedAssociations.put(target2, associations);
                                                        }
                                                        Slog.i(TAG, "Adding association: " + target2 + " <- " + allowed3);
                                                        associations.add(allowed3);
                                                    }
                                                }
                                            } else {
                                                logNotAllowedInPartition(name, permFile, parser);
                                            }
                                            XmlUtils.skipCurrentTag(parser);
                                            i2 = 1;
                                            break;
                                        case 24:
                                            try {
                                                String pkgname13 = parser.getAttributeValue(null, "package");
                                                if (pkgname13 == null) {
                                                    Slog.w(TAG, "<" + name + "> without package in " + permFile + " at " + parser.getPositionDescription());
                                                } else {
                                                    this.mBugreportWhitelistedPackages.add(pkgname13);
                                                }
                                                XmlUtils.skipCurrentTag(parser);
                                                i2 = 1;
                                                break;
                                            } catch (XmlPullParserException e5) {
                                                e = e5;
                                                str2 = str;
                                                Slog.w(TAG, str2, e);
                                                IoUtils.closeQuietly(permReader);
                                                if (StorageManager.isFileEncryptedNativeOnly()) {
                                                }
                                                if (StorageManager.hasAdoptable()) {
                                                }
                                                if (ActivityManager.isLowRamDeviceStatic()) {
                                                }
                                                it = this.mUnavailableFeatures.iterator();
                                                while (it.hasNext()) {
                                                }
                                            } catch (IOException e6) {
                                                e2 = e6;
                                                Slog.w(TAG, str, e2);
                                                IoUtils.closeQuietly(permReader);
                                                if (StorageManager.isFileEncryptedNativeOnly()) {
                                                }
                                                if (StorageManager.hasAdoptable()) {
                                                }
                                                if (ActivityManager.isLowRamDeviceStatic()) {
                                                }
                                                it = this.mUnavailableFeatures.iterator();
                                                while (it.hasNext()) {
                                                }
                                            }
                                            break;
                                        default:
                                            i2 = 1;
                                            Slog.w(TAG, "Tag " + name + " is unknown in " + permFile + " at " + parser.getPositionDescription());
                                            XmlUtils.skipCurrentTag(parser);
                                            break;
                                    }
                                    i3 = i2;
                                    type = type;
                                    permReader2 = permReader;
                                    str2 = str;
                                    lowRam = lowRam;
                                }
                            }
                        } else {
                            throw new XmlPullParserException("No start tag found");
                        }
                    }
                }
                if (type != 2) {
                }
            } catch (XmlPullParserException e7) {
                permReader = permReader2;
                e = e7;
                Slog.w(TAG, str2, e);
                IoUtils.closeQuietly(permReader);
                if (StorageManager.isFileEncryptedNativeOnly()) {
                }
                if (StorageManager.hasAdoptable()) {
                }
                if (ActivityManager.isLowRamDeviceStatic()) {
                }
                it = this.mUnavailableFeatures.iterator();
                while (it.hasNext()) {
                }
            } catch (IOException e8) {
                str = str2;
                permReader = permReader2;
                e2 = e8;
                Slog.w(TAG, str, e2);
                IoUtils.closeQuietly(permReader);
                if (StorageManager.isFileEncryptedNativeOnly()) {
                }
                if (StorageManager.hasAdoptable()) {
                }
                if (ActivityManager.isLowRamDeviceStatic()) {
                }
                it = this.mUnavailableFeatures.iterator();
                while (it.hasNext()) {
                }
            } catch (Throwable th3) {
                th = th3;
                IoUtils.closeQuietly(permReader);
                throw th;
            }
        } catch (FileNotFoundException e9) {
            Slog.w(TAG, "Couldn't find or open permissions file " + permFile);
        }
    }

    /* access modifiers changed from: protected */
    public void addFeature(String name, int version) {
        FeatureInfo fi = this.mAvailableFeatures.get(name);
        if (fi == null) {
            FeatureInfo fi2 = new FeatureInfo();
            fi2.name = name;
            fi2.version = version;
            this.mAvailableFeatures.put(name, fi2);
            return;
        }
        fi.version = Math.max(fi.version, version);
    }

    private void removeFeature(String name) {
        if (this.mAvailableFeatures.remove(name) != null) {
            Slog.d(TAG, "Removed unavailable feature " + name);
        }
    }

    /* access modifiers changed from: package-private */
    public void readPermission(XmlPullParser parser, String name) throws IOException, XmlPullParserException {
        if (!this.mPermissions.containsKey(name)) {
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
                    if (WifiConfiguration.GroupCipher.varName.equals(parser.getName())) {
                        String gidStr = parser.getAttributeValue(null, "gid");
                        if (gidStr != null) {
                            perm.gids = ArrayUtils.appendInt(perm.gids, Process.getGidForName(gidStr));
                        } else {
                            Slog.w(TAG, "<group> without gid at " + parser.getPositionDescription());
                        }
                    }
                    XmlUtils.skipCurrentTag(parser);
                }
            }
        } else {
            throw new IllegalStateException("Duplicate permission definition for " + name);
        }
    }

    private void readPrivAppPermissions(XmlPullParser parser, ArrayMap<String, ArraySet<String>> grantMap, ArrayMap<String, ArraySet<String>> denyMap) throws IOException, XmlPullParserException {
        String packageName = parser.getAttributeValue(null, "package");
        if (TextUtils.isEmpty(packageName)) {
            Slog.w(TAG, "package is required for <privapp-permissions> in " + parser.getPositionDescription());
            return;
        }
        ArraySet<String> permissions = grantMap.get(packageName);
        if (permissions == null) {
            permissions = new ArraySet<>();
        }
        ArraySet<String> denyPermissions = denyMap.get(packageName);
        int depth = parser.getDepth();
        while (XmlUtils.nextElementWithin(parser, depth)) {
            String name = parser.getName();
            if ("permission".equals(name)) {
                String permName = parser.getAttributeValue(null, "name");
                if (TextUtils.isEmpty(permName)) {
                    Slog.w(TAG, "name is required for <permission> in " + parser.getPositionDescription());
                } else {
                    permissions.add(permName);
                }
            } else if ("deny-permission".equals(name)) {
                String permName2 = parser.getAttributeValue(null, "name");
                if (TextUtils.isEmpty(permName2)) {
                    Slog.w(TAG, "name is required for <deny-permission> in " + parser.getPositionDescription());
                } else {
                    if (denyPermissions == null) {
                        denyPermissions = new ArraySet<>();
                    }
                    denyPermissions.add(permName2);
                }
            }
        }
        grantMap.put(packageName, permissions);
        if (denyPermissions != null) {
            denyMap.put(packageName, denyPermissions);
        }
    }

    /* access modifiers changed from: package-private */
    public void readOemPermissions(XmlPullParser parser) throws IOException, XmlPullParserException {
        String packageName = parser.getAttributeValue(null, "package");
        if (TextUtils.isEmpty(packageName)) {
            Slog.w(TAG, "package is required for <oem-permissions> in " + parser.getPositionDescription());
            return;
        }
        ArrayMap<String, Boolean> permissions = this.mOemPermissions.get(packageName);
        if (permissions == null) {
            permissions = new ArrayMap<>();
        }
        int depth = parser.getDepth();
        while (XmlUtils.nextElementWithin(parser, depth)) {
            String name = parser.getName();
            if ("permission".equals(name)) {
                String permName = parser.getAttributeValue(null, "name");
                if (TextUtils.isEmpty(permName)) {
                    Slog.w(TAG, "name is required for <permission> in " + parser.getPositionDescription());
                } else {
                    permissions.put(permName, Boolean.TRUE);
                }
            } else if ("deny-permission".equals(name)) {
                String permName2 = parser.getAttributeValue(null, "name");
                if (TextUtils.isEmpty(permName2)) {
                    Slog.w(TAG, "name is required for <deny-permission> in " + parser.getPositionDescription());
                } else {
                    permissions.put(permName2, Boolean.FALSE);
                }
            }
        }
        this.mOemPermissions.put(packageName, permissions);
    }

    private void readSplitPermission(XmlPullParser parser, File permFile) throws IOException, XmlPullParserException {
        String splitPerm = parser.getAttributeValue(null, "name");
        if (splitPerm == null) {
            Slog.w(TAG, "<split-permission> without name in " + permFile + " at " + parser.getPositionDescription());
            XmlUtils.skipCurrentTag(parser);
            return;
        }
        String targetSdkStr = parser.getAttributeValue(null, "targetSdk");
        int targetSdk = 10001;
        if (!TextUtils.isEmpty(targetSdkStr)) {
            try {
                targetSdk = Integer.parseInt(targetSdkStr);
            } catch (NumberFormatException e) {
                Slog.w(TAG, "<split-permission> targetSdk not an integer in " + permFile + " at " + parser.getPositionDescription());
                XmlUtils.skipCurrentTag(parser);
                return;
            }
        }
        int depth = parser.getDepth();
        List<String> newPermissions = new ArrayList<>();
        while (XmlUtils.nextElementWithin(parser, depth)) {
            if ("new-permission".equals(parser.getName())) {
                String newName = parser.getAttributeValue(null, "name");
                if (TextUtils.isEmpty(newName)) {
                    Slog.w(TAG, "name is required for <new-permission> in " + parser.getPositionDescription());
                } else {
                    newPermissions.add(newName);
                }
            } else {
                XmlUtils.skipCurrentTag(parser);
            }
        }
        if (!newPermissions.isEmpty()) {
            this.mSplitPermissions.add(new PermissionManager.SplitPermissionInfo(splitPerm, newPermissions, targetSdk));
        }
    }
}
