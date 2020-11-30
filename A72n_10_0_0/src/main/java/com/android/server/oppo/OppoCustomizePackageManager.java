package com.android.server.oppo;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemProperties;
import android.util.Slog;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class OppoCustomizePackageManager {
    public static final int ADD_APP_LIST = 1;
    public static final int ADD_SW_LIST = 0;
    public static final int CLEAR_ALL_SW_LIST = 2;
    public static final int CLEAR_SW_LIST = 1;
    private static final boolean DBG = false;
    public static final int DELETE_APP_LIST = 2;
    public static final int INSTALLATION_BLACK_LIST = 0;
    public static final int INSTALLATION_WHITE_LIST = 1;
    private static final String TAG = "OppoCustomizePackageManager";
    private List<String> mAccessibilityServiceWhiteList = new ArrayList();
    private File mAccessibilityServiceWhiteListFile;
    private List<String> mAllowMarketNames = new ArrayList();
    private File mAppUninstallationBlackPolicies;
    private List<String> mAppUninstallationBlackPoliciesList = new ArrayList();
    private File mAppUninstallationWhitePolicies;
    private List<String> mAppUninstallationWhitePoliciesList = new ArrayList();
    private Context mContext = null;
    private List<String> mInstallAppBlackList = new ArrayList();
    private List<String> mInstallAppWhiteList = new ArrayList();
    private Bundle mInstallSysAppData = new Bundle();
    private File mInstallSysAppDataFile;
    private PackageManager mPkgMgr = null;
    private File mSettingsInstallAppBlackList;
    private File mSettingsInstallAppWhiteList;
    private File mSettingsMarketnames;
    private File mSettingsUninstalledNames;
    private List<String> mSuperWhiteList = new ArrayList();
    private File mSuperWhiteListFile;
    private File mSystemDir = null;
    private List<String> mUninstalledAppNames = new ArrayList();

    public OppoCustomizePackageManager(Context context) {
        this.mContext = context;
        initPkgConfig(this.mContext);
        loadAllCustomizeList();
    }

    public boolean isInstallSourceEnable() {
        return SystemProperties.getBoolean("persist.sys.oppo.installsource", false);
    }

    public List<String> getInstalledSourceList() {
        return this.mAllowMarketNames;
    }

    private List<String> getUninstalledAppNames() {
        return this.mUninstalledAppNames;
    }

    public void enableInstallSource(boolean enable) {
        if (!hasSystemFeature("oppo.customize.function.control_app_install", 0) && !SystemProperties.getBoolean("persist.sys.custom.enable", false)) {
            return;
        }
        if (!enable) {
            SystemProperties.set("persist.sys.oppo.installsource", TemperatureProvider.SWITCH_OFF);
        } else if (!SystemProperties.getBoolean("persist.sys.oppo.installsource", false)) {
            SystemProperties.set("persist.sys.oppo.installsource", TemperatureProvider.SWITCH_ON);
            addMarketName("com.ctsi.emm");
        }
    }

    public void addInstallSource(String pkgName) {
        if (hasSystemFeature("oppo.customize.function.control_app_install", 0) || SystemProperties.getBoolean("persist.sys.custom.enable", false)) {
            addMarketName(pkgName);
        }
    }

    public void deleteInstallSource(String pkgName) {
        if (hasSystemFeature("oppo.customize.function.control_app_install", 0) || SystemProperties.getBoolean("persist.sys.custom.enable", false)) {
            deleteMarketName(pkgName);
        }
    }

    public void addDisallowUninstallApps(List<String> packageNames) {
        if (hasSystemFeature("oppo.customize.function.control_app_install", 0) || SystemProperties.getBoolean("persist.sys.custom.enable", false)) {
            addUninstalledAppName(packageNames);
        }
    }

    public void removeDisallowUninstallApps(List<String> packageNames) {
        if (hasSystemFeature("oppo.customize.function.control_app_install", 0) || SystemProperties.getBoolean("persist.sys.custom.enable", false)) {
            deleteUninstalledAppName(packageNames);
        }
    }

    public List<String> getDisallowUninstallApps() {
        return getUninstalledAppNames();
    }

    public void sendBroadcastForArmy() {
        this.mContext.sendBroadcast(new Intent("android.intent.action.OPPO_INSTALL_FOR_ARMY"));
    }

    public void addInstallPackageWhitelist(int mode, List<String> applist) {
        if (hasSystemFeature("oppo.customize.function.control_app_install", 0) || SystemProperties.getBoolean("persist.sys.custom.enable", false)) {
            doAddInstallPackageWhitelist(mode, applist);
        }
    }

    public void addInstallPackageBlacklist(int mode, List<String> applist) {
        if (hasSystemFeature("oppo.customize.function.control_app_install", 0) || SystemProperties.getBoolean("persist.sys.custom.enable", false)) {
            doAddInstallPackageBlacklist(mode, applist);
        }
    }

    public void addAppUninstallationPoliciesWhitelist(int mode, List<String> applist) {
        if (mode != 1) {
            if (this.mAppUninstallationWhitePolicies.exists() && this.mAppUninstallationWhitePolicies.isFile()) {
                this.mAppUninstallationWhitePolicies.delete();
            }
            this.mAppUninstallationWhitePoliciesList.clear();
        } else if (applist != null && applist.size() > 0) {
            if (!this.mAppUninstallationWhitePolicies.exists()) {
                try {
                    this.mAppUninstallationWhitePolicies.createNewFile();
                } catch (IOException e) {
                    return;
                }
            }
            OutputStream out = null;
            try {
                OutputStream out2 = new FileOutputStream(this.mAppUninstallationWhitePolicies, true);
                for (String packageName : applist) {
                    if (!this.mAppUninstallationWhitePoliciesList.contains(packageName)) {
                        out2.write(packageName.getBytes());
                        out2.write(124);
                        this.mAppUninstallationWhitePoliciesList.add(packageName);
                    }
                }
                out2.close();
            } catch (FileNotFoundException e2) {
                Slog.e(TAG, "add Install Package White list:" + e2);
            } catch (IOException e3) {
                Slog.e(TAG, "add Install Package White list:" + e3);
                if (0 != 0) {
                    try {
                        out.close();
                    } catch (IOException e4) {
                    }
                }
            } catch (Exception e5) {
                Slog.e(TAG, "add Install Package White list:" + e5);
            }
        }
    }

    public void addAppUninstallationPoliciesBlacklist(int mode, List<String> applist) {
        if (mode != 1) {
            if (this.mAppUninstallationBlackPolicies.exists() && this.mAppUninstallationBlackPolicies.isFile()) {
                this.mAppUninstallationBlackPolicies.delete();
            }
            this.mAppUninstallationBlackPoliciesList.clear();
        } else if (applist != null && applist.size() > 0) {
            if (!this.mAppUninstallationBlackPolicies.exists()) {
                try {
                    this.mAppUninstallationBlackPolicies.createNewFile();
                } catch (IOException e) {
                    return;
                }
            }
            OutputStream out = null;
            try {
                OutputStream out2 = new FileOutputStream(this.mAppUninstallationBlackPolicies, true);
                for (String packageName : applist) {
                    if (!this.mAppUninstallationBlackPoliciesList.contains(packageName)) {
                        out2.write(packageName.getBytes());
                        out2.write(124);
                        this.mAppUninstallationBlackPoliciesList.add(packageName);
                    }
                }
                out2.close();
            } catch (FileNotFoundException e2) {
                Slog.e(TAG, "add Install Package White list:" + e2);
            } catch (IOException e3) {
                Slog.e(TAG, "add Install Package White list:" + e3);
                if (0 != 0) {
                    try {
                        out.close();
                    } catch (IOException e4) {
                    }
                }
            } catch (Exception e5) {
                Slog.e(TAG, "add Install Package White list:" + e5);
            }
        }
    }

    public List<String> getAppInstallationPolicies(int mode) {
        if (mode == 0) {
            return getInstalledAppBlackList();
        }
        if (mode == 1) {
            return getInstalledAppWhiteList();
        }
        Slog.e(TAG, "getAppInstallationPolicies:unknown mode!");
        return null;
    }

    public List<String> getAppUninstallationPolicies(int mode) {
        if (mode == 1) {
            return getAppUninstallationPoliciesWhitelist();
        }
        if (mode == 0) {
            return getAppUninstallationPoliciesBlacklist();
        }
        Slog.e(TAG, "getAppUninstallationPolicies:unknown mode!");
        return null;
    }

    public void addAccessibilityServiceToWhiteList(List<String> componentNameList) {
        Slog.d(TAG, "addAccessibilityServiceToWhiteList componentNameList: " + componentNameList);
        if (componentNameList != null && componentNameList.size() > 0) {
            if (!this.mAccessibilityServiceWhiteListFile.exists()) {
                Slog.d(TAG, "!mAccessibilityServiceWhiteListFile.exists()");
                try {
                    this.mAccessibilityServiceWhiteListFile.createNewFile();
                } catch (IOException e) {
                    return;
                }
            }
            OutputStream out = null;
            try {
                OutputStream out2 = new FileOutputStream(this.mAccessibilityServiceWhiteListFile, true);
                for (String packageName : componentNameList) {
                    if (this.mAccessibilityServiceWhiteList.contains(packageName)) {
                        Slog.d(TAG, "mAccessibilityServiceWhiteList.contains(packageName)");
                    } else {
                        out2.write(packageName.getBytes());
                        out2.write(124);
                        this.mAccessibilityServiceWhiteList.add(packageName);
                        Slog.d(TAG, "mAccessibilityServiceWhiteList size:" + this.mAccessibilityServiceWhiteList.size());
                    }
                }
                out2.close();
            } catch (FileNotFoundException e2) {
                Slog.e(TAG, "addAccessibilityServiceToWhiteList:" + e2);
            } catch (IOException e3) {
                Slog.e(TAG, "addAccessibilityServiceToWhiteList:" + e3);
                if (0 != 0) {
                    try {
                        out.close();
                    } catch (IOException e4) {
                    }
                }
            } catch (Exception e5) {
                Slog.e(TAG, "addAccessibilityServiceToWhiteList:" + e5);
            }
        }
    }

    public void removeAccessibilityServiceFromWhiteList(List<String> componentNameList) {
        Slog.d(TAG, "removeAccessibilityServiceFromWhiteList componentNameList:" + componentNameList);
        if (this.mAccessibilityServiceWhiteListFile.exists()) {
            Slog.d(TAG, "mAccessibilityServiceWhiteListFile.exists()");
            if (this.mAccessibilityServiceWhiteListFile.isFile()) {
                Slog.d(TAG, "mAccessibilityServiceWhiteListFile.isFile()");
                this.mAccessibilityServiceWhiteListFile.delete();
            }
        }
        if (componentNameList == null || componentNameList.size() <= 0) {
            this.mAccessibilityServiceWhiteList.clear();
            return;
        }
        for (String packageName2 : componentNameList) {
            Slog.d(TAG, "packageName2 - " + packageName2);
            if (this.mAccessibilityServiceWhiteList.contains(packageName2)) {
                this.mAccessibilityServiceWhiteList.remove(packageName2);
            }
        }
        List<String> left = new ArrayList<>();
        for (String packageName3 : this.mAccessibilityServiceWhiteList) {
            Slog.d(TAG, "packageName3 - " + packageName3);
            left.add(packageName3);
        }
        this.mAccessibilityServiceWhiteList.clear();
        addAccessibilityServiceToWhiteList(left);
    }

    public List<String> getAccessibilityServiceWhiteList() {
        return this.mAccessibilityServiceWhiteList;
    }

    public void deleteAccessibilityServiceWhiteList() {
        if (this.mAccessibilityServiceWhiteListFile.exists() && this.mAccessibilityServiceWhiteListFile.isFile()) {
            this.mAccessibilityServiceWhiteListFile.delete();
        }
        List<String> list = this.mAccessibilityServiceWhiteList;
        if (list != null) {
            list.clear();
        }
    }

    public void backupDataApp(String src, String packageName, String out, int requestId) {
        try {
            if (this.mPkgMgr == null) {
                this.mPkgMgr = this.mContext.getPackageManager();
            }
            Class.forName("android.content.pm.PackageManager").getMethod("backupAppData", String.class, String.class, String.class, Integer.TYPE).invoke(this.mPkgMgr, src, packageName, out, Integer.valueOf(requestId));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean hasSystemFeature(String featureName, int version) {
        if (featureName == null) {
            return false;
        }
        if (this.mPkgMgr == null) {
            this.mPkgMgr = this.mContext.getPackageManager();
        }
        return this.mPkgMgr.hasSystemFeature(featureName, version);
    }

    private void addMarketName(String marketName) {
        if (marketName == null) {
            Slog.d(TAG, "market name is null!");
        } else if (this.mAllowMarketNames.contains(marketName)) {
            Slog.d(TAG, "market name has contained!");
        } else if (this.mAllowMarketNames.size() > 7) {
            Slog.d(TAG, "market names is over 8!");
        } else {
            if (!this.mSettingsMarketnames.exists()) {
                try {
                    this.mSettingsMarketnames.createNewFile();
                } catch (IOException e) {
                    return;
                }
            }
            OutputStream out = null;
            try {
                out = new FileOutputStream(this.mSettingsMarketnames, true);
                out.write(marketName.getBytes());
                out.write(124);
                this.mAllowMarketNames.add(marketName);
                out.close();
            } catch (FileNotFoundException e2) {
                Slog.e(TAG, "add Market Name:" + e2);
            } catch (IOException e3) {
                Slog.e(TAG, "add Market Name:" + e3);
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e4) {
                    }
                }
            } catch (Exception e5) {
                Slog.e(TAG, "add Market Name:" + e5);
            }
        }
    }

    private void addUninstalledAppName(List<String> appName) {
        if (appName != null && appName.size() > 0) {
            if (!this.mSettingsUninstalledNames.exists()) {
                try {
                    this.mSettingsUninstalledNames.createNewFile();
                } catch (IOException e) {
                    return;
                }
            }
            OutputStream out = null;
            try {
                OutputStream out2 = new FileOutputStream(this.mSettingsUninstalledNames, true);
                for (String packageName : appName) {
                    if (!this.mUninstalledAppNames.contains(packageName)) {
                        out2.write(packageName.getBytes());
                        out2.write(124);
                        this.mUninstalledAppNames.add(packageName);
                    }
                }
                out2.close();
            } catch (FileNotFoundException e2) {
                Slog.e(TAG, "add Uninstalled App Name:" + e2);
            } catch (IOException e3) {
                Slog.e(TAG, "add Uninstalled App Name:" + e3);
                if (0 != 0) {
                    try {
                        out.close();
                    } catch (IOException e4) {
                    }
                }
            } catch (Exception e5) {
                Slog.e(TAG, "add Uninstalled App Name:" + e5);
            }
        }
    }

    private void deleteMarketName(String marketName) {
        if (marketName == null) {
            Slog.d(TAG, "market name is null!");
        } else if (!this.mAllowMarketNames.contains(marketName)) {
            Slog.d(TAG, "market name is not exist!");
        } else if (this.mAllowMarketNames.contains(marketName)) {
            this.mAllowMarketNames.remove(marketName);
            saveMarketNamesToFile();
        }
    }

    private void deleteUninstalledAppName(List<String> appName) {
        if (this.mSettingsUninstalledNames.exists() && this.mSettingsUninstalledNames.isFile()) {
            this.mSettingsUninstalledNames.delete();
        }
        if (appName == null || appName.size() <= 0) {
            this.mUninstalledAppNames.clear();
            return;
        }
        for (String packageName2 : appName) {
            if (this.mUninstalledAppNames.contains(packageName2)) {
                this.mUninstalledAppNames.remove(packageName2);
            }
        }
        List<String> left = new ArrayList<>();
        for (String packageName3 : this.mUninstalledAppNames) {
            left.add(packageName3);
        }
        this.mUninstalledAppNames.clear();
        addUninstalledAppName(left);
    }

    private void saveMarketNamesToFile() {
        if (this.mSettingsMarketnames.exists() && this.mSettingsMarketnames.isFile()) {
            this.mSettingsMarketnames.delete();
        }
        try {
            this.mSettingsMarketnames.createNewFile();
            OutputStream out = null;
            try {
                OutputStream out2 = new FileOutputStream(this.mSettingsMarketnames, true);
                for (int j = 0; j < this.mAllowMarketNames.size(); j++) {
                    out2.write(this.mAllowMarketNames.get(j).getBytes());
                    out2.write(124);
                }
                out2.close();
            } catch (FileNotFoundException e) {
                Slog.e(TAG, "save Market Names To File:" + e);
            } catch (IOException e2) {
                Slog.e(TAG, "save Market Names To File:" + e2);
                if (0 != 0) {
                    try {
                        out.close();
                    } catch (IOException e3) {
                    }
                }
            } catch (Exception e4) {
                Slog.e(TAG, "save Market Names To File:" + e4);
            }
        } catch (IOException e5) {
        }
    }

    private void doAddInstallPackageWhitelist(int mode, List<String> applist) {
        if (mode != 1) {
            if (this.mSettingsInstallAppWhiteList.exists() && this.mSettingsInstallAppWhiteList.isFile()) {
                this.mSettingsInstallAppWhiteList.delete();
            }
            if (applist == null || applist.size() <= 0) {
                this.mInstallAppWhiteList.clear();
                return;
            }
            for (String packageName2 : applist) {
                if (this.mInstallAppWhiteList.contains(packageName2)) {
                    this.mInstallAppWhiteList.remove(packageName2);
                }
            }
            List<String> left = new ArrayList<>();
            for (String packageName3 : this.mInstallAppWhiteList) {
                left.add(packageName3);
            }
            this.mInstallAppWhiteList.clear();
            doAddInstallPackageWhitelist(1, left);
        } else if (applist != null && applist.size() > 0) {
            if (!this.mSettingsInstallAppWhiteList.exists()) {
                try {
                    this.mSettingsInstallAppWhiteList.createNewFile();
                } catch (IOException e) {
                    return;
                }
            }
            OutputStream out = null;
            try {
                OutputStream out2 = new FileOutputStream(this.mSettingsInstallAppWhiteList, true);
                for (String packageName : applist) {
                    if (!this.mInstallAppWhiteList.contains(packageName)) {
                        out2.write(packageName.getBytes());
                        out2.write(124);
                        this.mInstallAppWhiteList.add(packageName);
                    }
                }
                out2.close();
            } catch (FileNotFoundException e2) {
                Slog.e(TAG, "add Install Package White list:" + e2);
            } catch (IOException e3) {
                Slog.e(TAG, "add Install Package White list:" + e3);
                if (0 != 0) {
                    try {
                        out.close();
                    } catch (IOException e4) {
                    }
                }
            } catch (Exception e5) {
                Slog.e(TAG, "add Install Package White list:" + e5);
            }
        }
    }

    private void doAddInstallPackageBlacklist(int mode, List<String> applist) {
        if (mode != 1) {
            if (this.mSettingsInstallAppBlackList.exists() && this.mSettingsInstallAppBlackList.isFile()) {
                this.mSettingsInstallAppBlackList.delete();
            }
            if (applist == null || applist.size() <= 0) {
                this.mInstallAppBlackList.clear();
                return;
            }
            for (String packageName2 : applist) {
                if (this.mInstallAppBlackList.contains(packageName2)) {
                    this.mInstallAppBlackList.remove(packageName2);
                }
            }
            List<String> left = new ArrayList<>();
            for (String packageName3 : this.mInstallAppBlackList) {
                left.add(packageName3);
            }
            this.mInstallAppBlackList.clear();
            doAddInstallPackageBlacklist(1, left);
        } else if (applist != null && applist.size() > 0) {
            if (!this.mSettingsInstallAppBlackList.exists()) {
                try {
                    this.mSettingsInstallAppBlackList.createNewFile();
                } catch (IOException e) {
                    return;
                }
            }
            OutputStream out = null;
            try {
                OutputStream out2 = new FileOutputStream(this.mSettingsInstallAppBlackList, true);
                for (String packageName : applist) {
                    if (!this.mInstallAppBlackList.contains(packageName)) {
                        out2.write(packageName.getBytes());
                        out2.write(124);
                        this.mInstallAppBlackList.add(packageName);
                    }
                }
                out2.close();
            } catch (FileNotFoundException e2) {
                Slog.e(TAG, "add Install Package Black list:" + e2);
            } catch (IOException e3) {
                Slog.e(TAG, "add Install Package Black list:" + e3);
                if (0 != 0) {
                    try {
                        out.close();
                    } catch (IOException e4) {
                    }
                }
            } catch (Exception e5) {
                Slog.e(TAG, "add Install Package Black list:" + e5);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void readCustomizeListFromFile(File file, List<String> list) {
        if (file.exists()) {
            BufferedReader bufferedReader = null;
            StringBuffer sb = new StringBuffer();
            try {
                BufferedReader bufferedReader2 = new BufferedReader(new FileReader(file));
                while (true) {
                    String temp = bufferedReader2.readLine();
                    if (temp == null) {
                        break;
                    }
                    sb.append(temp);
                }
                bufferedReader2.close();
                for (String name : sb.toString().split("\\|")) {
                    list.add(name);
                }
            } catch (FileNotFoundException e) {
                Slog.e(TAG, "read Customize List From File:" + e);
            } catch (IOException e2) {
                Slog.e(TAG, "read Customize List From File:" + e2);
                if (0 != 0) {
                    try {
                        bufferedReader.close();
                    } catch (IOException e3) {
                    }
                }
            } catch (Exception e4) {
                Slog.e(TAG, "read Customize List From File:" + e4);
            }
        }
    }

    private void loadAllCustomizeList() {
        new Thread() {
            /* class com.android.server.oppo.OppoCustomizePackageManager.AnonymousClass1 */

            public void run() {
                OppoCustomizePackageManager oppoCustomizePackageManager = OppoCustomizePackageManager.this;
                oppoCustomizePackageManager.readCustomizeListFromFile(oppoCustomizePackageManager.mSettingsMarketnames, OppoCustomizePackageManager.this.mAllowMarketNames);
                OppoCustomizePackageManager oppoCustomizePackageManager2 = OppoCustomizePackageManager.this;
                oppoCustomizePackageManager2.readCustomizeListFromFile(oppoCustomizePackageManager2.mSettingsUninstalledNames, OppoCustomizePackageManager.this.mUninstalledAppNames);
                OppoCustomizePackageManager oppoCustomizePackageManager3 = OppoCustomizePackageManager.this;
                oppoCustomizePackageManager3.readCustomizeListFromFile(oppoCustomizePackageManager3.mSettingsInstallAppWhiteList, OppoCustomizePackageManager.this.mInstallAppWhiteList);
                OppoCustomizePackageManager oppoCustomizePackageManager4 = OppoCustomizePackageManager.this;
                oppoCustomizePackageManager4.readCustomizeListFromFile(oppoCustomizePackageManager4.mSettingsInstallAppBlackList, OppoCustomizePackageManager.this.mInstallAppBlackList);
                OppoCustomizePackageManager.this.readInstallSysAppDataFromFile();
                OppoCustomizePackageManager oppoCustomizePackageManager5 = OppoCustomizePackageManager.this;
                oppoCustomizePackageManager5.readCustomizeListFromFile(oppoCustomizePackageManager5.mAccessibilityServiceWhiteListFile, OppoCustomizePackageManager.this.mAccessibilityServiceWhiteList);
                OppoCustomizePackageManager oppoCustomizePackageManager6 = OppoCustomizePackageManager.this;
                oppoCustomizePackageManager6.readCustomizeListFromFile(oppoCustomizePackageManager6.mSuperWhiteListFile, OppoCustomizePackageManager.this.mSuperWhiteList);
            }
        }.start();
    }

    private void initPkgConfig(Context context) {
        this.mSystemDir = new File(Environment.getDataDirectory(), "system");
        this.mSettingsMarketnames = new File(this.mSystemDir, "market-white-list.txt");
        this.mSettingsUninstalledNames = new File(this.mSystemDir, "uninstalled-app-list.txt");
        this.mSettingsInstallAppWhiteList = new File(this.mSystemDir, "installed-app-white-list.txt");
        this.mSettingsInstallAppBlackList = new File(this.mSystemDir, "installed-app-black-list.txt");
        this.mAppUninstallationWhitePolicies = new File(this.mSystemDir, "uninstalled-policy-app-white-list.txt");
        this.mAppUninstallationBlackPolicies = new File(this.mSystemDir, "uninstalled-policy-app-black-list.txt");
        this.mInstallSysAppDataFile = new File(this.mSystemDir, "install-sys-app-list.txt");
        this.mAccessibilityServiceWhiteListFile = new File(this.mSystemDir, "accessibility-service-white-list.txt");
        this.mSuperWhiteListFile = new File(this.mSystemDir, "customize-super-white-list.txt");
        loadAppInstallationPoliciesList();
    }

    public List<String> getInstalledAppWhiteList() {
        return this.mInstallAppWhiteList;
    }

    public List<String> getInstalledAppBlackList() {
        return this.mInstallAppBlackList;
    }

    private List<String> getAppUninstallationPoliciesWhitelist() {
        return this.mAppUninstallationWhitePoliciesList;
    }

    private List<String> getAppUninstallationPoliciesBlacklist() {
        return this.mAppUninstallationBlackPoliciesList;
    }

    private void loadAppInstallationPoliciesList() {
        new Thread() {
            /* class com.android.server.oppo.OppoCustomizePackageManager.AnonymousClass2 */

            public void run() {
                OppoCustomizePackageManager oppoCustomizePackageManager = OppoCustomizePackageManager.this;
                oppoCustomizePackageManager.readCustomizeListFromFile(oppoCustomizePackageManager.mAppUninstallationBlackPolicies, OppoCustomizePackageManager.this.mAppUninstallationBlackPoliciesList);
                OppoCustomizePackageManager oppoCustomizePackageManager2 = OppoCustomizePackageManager.this;
                oppoCustomizePackageManager2.readCustomizeListFromFile(oppoCustomizePackageManager2.mAppUninstallationWhitePolicies, OppoCustomizePackageManager.this.mAppUninstallationWhitePoliciesList);
            }
        }.start();
    }

    public Bundle getInstallSysAppBundle() {
        return this.mInstallSysAppData;
    }

    public void setInstallSysAppBundle(Bundle bundle) {
        this.mInstallSysAppData = bundle;
        writeInstallSysAppDataToFile(bundle);
    }

    public List<String> getPrivInstallSysAppList() {
        List<String> pkgs = new ArrayList<>();
        String totalValue = this.mInstallSysAppData.getString("value");
        if (totalValue != null && !totalValue.isEmpty()) {
            for (String value : totalValue.split(";")) {
                String[] infos = value.split(":");
                if (infos.length == 4 && Boolean.parseBoolean(infos[2])) {
                    pkgs.add(infos[0]);
                }
            }
        }
        return pkgs;
    }

    public List<String> getDetachableInstallSysAppList() {
        List<String> pkgs = new ArrayList<>();
        String totalValue = this.mInstallSysAppData.getString("value");
        if (totalValue != null && !totalValue.isEmpty()) {
            for (String value : totalValue.split(";")) {
                String[] infos = value.split(":");
                if (infos.length == 4 && !Boolean.parseBoolean(infos[3])) {
                    pkgs.add(infos[0]);
                }
            }
        }
        return pkgs;
    }

    public List<String> getAllInstallSysAppList() {
        List<String> pkgs = new ArrayList<>();
        String totalValue = this.mInstallSysAppData.getString("value");
        if (totalValue != null && !totalValue.isEmpty()) {
            for (String value : totalValue.split(";")) {
                String[] infos = value.split(":");
                if (infos.length == 4) {
                    pkgs.add(infos[0]);
                }
            }
        }
        return pkgs;
    }

    public boolean writeInstallSysAppDataToFile(Bundle bean) {
        String s = bean.getString("value");
        try {
            OutputStream out = new FileOutputStream(this.mInstallSysAppDataFile);
            if (s == null) {
                return false;
            }
            out.write(s.getBytes());
            out.close();
            return true;
        } catch (Exception e) {
            Slog.d(TAG, "writeInstallSysAppDataToFile: " + e);
            return false;
        }
    }

    public void readInstallSysAppDataFromFile() {
        Bundle bean = new Bundle();
        if (this.mInstallSysAppDataFile.exists()) {
            try {
                BufferedReader reader = new BufferedReader(new FileReader(this.mInstallSysAppDataFile));
                String s = reader.readLine();
                reader.close();
                bean.putString("value", s);
                this.mInstallSysAppData = bean;
            } catch (Exception e) {
                Slog.d(TAG, "readInstallSysAppDataFromFile: " + e);
            }
        }
    }

    public void changeSuperWhiteList(int mode, List<String> list) {
        if (mode == 0) {
            if (list != null && list.size() > 0) {
                if (!this.mSuperWhiteListFile.exists()) {
                    try {
                        this.mSuperWhiteListFile.createNewFile();
                    } catch (IOException e) {
                        return;
                    }
                }
                OutputStream out = null;
                try {
                    OutputStream out2 = new FileOutputStream(this.mSuperWhiteListFile, true);
                    for (String packageName : list) {
                        if (!this.mSuperWhiteList.contains(packageName)) {
                            out2.write(packageName.getBytes());
                            out2.write(124);
                            this.mSuperWhiteList.add(packageName);
                        }
                    }
                    out2.close();
                } catch (FileNotFoundException e2) {
                    Slog.e(TAG, "add Super White list:" + e2);
                } catch (IOException e3) {
                    Slog.e(TAG, "add Super White list:" + e3);
                    if (0 != 0) {
                        try {
                            out.close();
                        } catch (IOException e4) {
                        }
                    }
                } catch (Exception e5) {
                    Slog.e(TAG, "add Super White list:" + e5);
                }
            }
        } else if (mode == 1) {
            if (this.mSuperWhiteListFile.exists() && this.mSuperWhiteListFile.isFile()) {
                this.mSuperWhiteListFile.delete();
            }
            if (list != null && list.size() > 0) {
                for (String packageName2 : list) {
                    if (this.mSuperWhiteList.contains(packageName2)) {
                        this.mSuperWhiteList.remove(packageName2);
                    }
                }
                List<String> leftList = new ArrayList<>();
                for (String packageName3 : this.mSuperWhiteList) {
                    leftList.add(packageName3);
                }
                this.mSuperWhiteList.clear();
                changeSuperWhiteList(0, leftList);
            }
        } else {
            if (this.mSuperWhiteListFile.exists() && this.mSuperWhiteListFile.isFile()) {
                this.mSuperWhiteListFile.delete();
            }
            this.mSuperWhiteList.clear();
        }
    }

    public List<String> getSuperWhiteList() {
        return this.mSuperWhiteList;
    }
}
