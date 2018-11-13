package com.android.internal.os;

import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.Log;
import com.android.internal.os.IRegionalizationService.Stub;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RegionalizationEnvironment {
    private static final boolean DEBUG = true;
    public static final String ISREGIONAL_APP = "app";
    public static final String ISREGIONAL_FRAMEWORK = "Framework";
    private static final String SPEC_FILE_PATH = "/persist/speccfg/spec";
    private static final boolean SUPPORTED = SystemProperties.getBoolean("ro.regionalization.support", false);
    private static final String TAG = "RegionalizationEnvironment";
    private static boolean isLoaded = false;
    private static ArrayList<String> mExcludedApps = new ArrayList();
    private static ArrayList<Package> mPackages = new ArrayList();
    private static IRegionalizationService mRegionalizationService = null;

    private static class Package {
        private final String mName;
        private final int mPriority;
        private final String mStorage;

        public Package(String name, int priority, String storage) {
            this.mName = name;
            this.mPriority = priority;
            this.mStorage = storage;
        }

        public String getName() {
            return this.mName;
        }

        public int getPriority() {
            return this.mPriority;
        }

        public String getStoragePos() {
            return this.mStorage;
        }

        public File getDirectory() {
            return new File(this.mStorage, this.mName);
        }

        public String getExcludedListFilePath() {
            return getDirectory().getAbsolutePath() + "/exclude.list";
        }
    }

    private static void init() {
        mRegionalizationService = Stub.asInterface(ServiceManager.getService("regionalization"));
        if (mRegionalizationService != null) {
            loadSwitchedPackages();
            loadExcludedApplist();
            isLoaded = true;
        }
    }

    public static boolean isSupported() {
        if (SUPPORTED && (isLoaded ^ 1) != 0) {
            init();
        }
        return SUPPORTED;
    }

    public static int getPackagesCount() {
        return mPackages.size();
    }

    public static List<String> getAllPackageNames() {
        ArrayList<String> packages = new ArrayList();
        for (Package p : mPackages) {
            packages.add(p.getName());
        }
        return packages;
    }

    public static boolean isRegionalizationCarrierOverlayPackage(String overlaypath, String apporFramework) {
        if (!isSupported() || overlaypath == null) {
            return false;
        }
        boolean isPackFound = false;
        for (String pack : getAllPackageNames()) {
            if (overlaypath.indexOf(pack) != -1) {
                isPackFound = true;
            }
            if (isPackFound && ((apporFramework.equals(ISREGIONAL_FRAMEWORK) && overlaypath.indexOf(ISREGIONAL_FRAMEWORK) != -1) || apporFramework.equals(ISREGIONAL_APP))) {
                return true;
            }
        }
        return false;
    }

    public static List<File> getAllPackageDirectories() {
        ArrayList<File> directories = new ArrayList();
        for (Package p : mPackages) {
            Log.v(TAG, "Package Directoriy(" + p.getPriority() + "):" + p.getDirectory());
            directories.add(p.getDirectory());
        }
        return directories;
    }

    public static boolean isExcludedApp(String appName) {
        if (getPackagesCount() == 0) {
            return false;
        }
        if (appName.endsWith(".apk")) {
            return mExcludedApps.contains(appName);
        }
        return mExcludedApps.contains(appName + ".apk");
    }

    public static IRegionalizationService getRegionalizationService() {
        return mRegionalizationService;
    }

    public static String getStoragePos() {
        for (Package pack : mPackages) {
            String pos = pack.getStoragePos();
            if (!TextUtils.isEmpty(pos)) {
                return pos;
            }
        }
        try {
            mPackages.clear();
            throw new IOException("Read wrong package for Carrier!");
        } catch (IOException e) {
            Log.e(TAG, "Get storage pos error, caused by: " + e.getMessage());
            return "";
        }
    }

    private static void loadSwitchedPackages() {
        Log.d(TAG, "load packages for Carrier!");
        ArrayList contents = null;
        try {
            contents = (ArrayList) mRegionalizationService.readFile(SPEC_FILE_PATH, null);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        if (contents != null) {
            try {
                if (contents.size() <= 0) {
                    return;
                }
                if (((String) contents.get(0)).startsWith("packStorage=")) {
                    String storagePos = ((String) contents.get(0)).substring("packStorage=".length());
                    if (TextUtils.isEmpty(storagePos)) {
                        throw new IOException("Storage pos for Carrier package is wrong!");
                    }
                    if (((String) contents.get(1)).matches("^packCount=[0-9]$")) {
                        int packNum = Integer.parseInt(((String) contents.get(1)).substring("packCount=".length()));
                        if (packNum <= 0 || contents.size() <= packNum) {
                            throw new IOException("Package count of Carrier is wrong!");
                        }
                        int i = 2;
                        while (i < packNum + 2) {
                            if (((String) contents.get(i)).matches("^strSpec[0-9]=\\w+$")) {
                                String packName = ((String) contents.get(i)).substring("strSpec".length() + 2);
                                if (!TextUtils.isEmpty(packName)) {
                                    boolean exists = false;
                                    try {
                                        exists = mRegionalizationService.checkFileExists(storagePos + "/" + packName);
                                    } catch (RemoteException e2) {
                                        e2.printStackTrace();
                                    }
                                    if (exists) {
                                        mPackages.add(new Package(packName, i, storagePos));
                                    } else {
                                        mPackages.clear();
                                        throw new IOException("Read wrong packages for Carrier!");
                                    }
                                }
                                i++;
                            } else {
                                mPackages.clear();
                                throw new IOException("Read wrong packages for Carrier!");
                            }
                        }
                        return;
                    }
                    throw new IOException("Can't read package count of Carrier!");
                }
                throw new IOException("Can't read storage pos for Carrier package!");
            } catch (IOException e3) {
                Log.e(TAG, "Load package for carrier error, caused by: " + e3.getMessage());
            }
        }
    }

    private static void loadExcludedApplist() {
        Log.d(TAG, "loadExcludedApps!");
        if (getPackagesCount() != 0) {
            for (Package pack : mPackages) {
                Log.d(TAG, "load excluded apps for " + pack.getDirectory());
                Iterable contents = null;
                try {
                    contents = (ArrayList) mRegionalizationService.readFile(pack.getExcludedListFilePath(), null);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                if (contents != null && contents.size() > 0) {
                    for (String content : contents) {
                        if (!TextUtils.isEmpty(content)) {
                            int pos = content.lastIndexOf("/");
                            if (pos != -1) {
                                String apkName = content.substring(pos + 1);
                                if (!(TextUtils.isEmpty(apkName) || (mExcludedApps.contains(apkName) ^ 1) == 0)) {
                                    mExcludedApps.add(apkName);
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
