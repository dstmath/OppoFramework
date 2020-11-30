package com.android.server.om;

import android.content.om.OverlayInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.Slog;
import com.android.internal.util.ArrayUtils;
import com.android.server.om.OverlayManagerSettings;
import com.android.server.pm.PackageManagerService;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/* access modifiers changed from: package-private */
public final class OverlayManagerServiceImpl {
    private static final int FLAG_OVERLAY_IS_BEING_REPLACED = 2;
    @Deprecated
    private static final int FLAG_TARGET_IS_BEING_REPLACED = 1;
    private final String[] mDefaultOverlays;
    private final IdmapManager mIdmapManager;
    private final OverlayChangeListener mListener;
    private final PackageManagerHelper mPackageManager;
    private final OverlayManagerSettings mSettings;

    /* access modifiers changed from: package-private */
    public interface OverlayChangeListener {
        void onOverlaysChanged(String str, int i);
    }

    /* access modifiers changed from: package-private */
    public interface PackageManagerHelper {
        List<PackageInfo> getOverlayPackages(int i);

        PackageInfo getPackageInfo(String str, int i);

        boolean signaturesMatching(String str, String str2, int i);
    }

    private static boolean mustReinitializeOverlay(PackageInfo theTruth, OverlayInfo oldSettings) {
        if (oldSettings == null || !Objects.equals(theTruth.overlayTarget, oldSettings.targetPackageName) || !Objects.equals(theTruth.targetOverlayableName, oldSettings.targetOverlayableName) || theTruth.isStaticOverlayPackage() != oldSettings.isStatic) {
            return true;
        }
        if (!theTruth.isStaticOverlayPackage() || theTruth.overlayPriority == oldSettings.priority) {
            return false;
        }
        return true;
    }

    OverlayManagerServiceImpl(PackageManagerHelper packageManager, IdmapManager idmapManager, OverlayManagerSettings settings, String[] defaultOverlays, OverlayChangeListener listener) {
        this.mPackageManager = packageManager;
        this.mIdmapManager = idmapManager;
        this.mSettings = settings;
        this.mDefaultOverlays = defaultOverlays;
        this.mListener = listener;
    }

    /* access modifiers changed from: package-private */
    public ArrayList<String> updateOverlaysForUser(int newUserId) {
        int i;
        ArraySet<String> enabledCategories;
        String[] strArr;
        int i2;
        String str;
        OverlayInfo oi;
        Iterator<String> iter;
        ArrayMap<String, OverlayInfo> storedOverlayInfos;
        int i3;
        OverlayManagerSettings.BadKeyException e;
        String str2;
        int tmpSize;
        ArrayMap<String, List<OverlayInfo>> tmp;
        int tmpSize2;
        PackageInfo overlayPackage;
        List<PackageInfo> overlayPackages;
        ArrayMap<String, OverlayInfo> storedOverlayInfos2;
        String str3 = "' for user ";
        Set<String> packagesToUpdateAssets = new ArraySet<>();
        ArrayMap<String, List<OverlayInfo>> tmp2 = this.mSettings.getOverlaysForUser(newUserId);
        int overlayPackagesSize = tmp2.size();
        ArrayMap<String, OverlayInfo> storedOverlayInfos3 = new ArrayMap<>(overlayPackagesSize);
        for (int i4 = 0; i4 < overlayPackagesSize; i4++) {
            List<OverlayInfo> chunk = tmp2.valueAt(i4);
            int chunkSize = chunk.size();
            for (int j = 0; j < chunkSize; j++) {
                OverlayInfo oi2 = chunk.get(j);
                storedOverlayInfos3.put(oi2.packageName, oi2);
            }
        }
        List<PackageInfo> overlayPackages2 = this.mPackageManager.getOverlayPackages(newUserId);
        int overlayPackagesSize2 = overlayPackages2.size();
        int i5 = 0;
        while (i5 < overlayPackagesSize2) {
            PackageInfo overlayPackage2 = overlayPackages2.get(i5);
            OverlayInfo oi3 = storedOverlayInfos3.get(overlayPackage2.packageName);
            if (mustReinitializeOverlay(overlayPackage2, oi3)) {
                if (oi3 != null) {
                    packagesToUpdateAssets.add(oi3.targetPackageName);
                }
                tmp = tmp2;
                overlayPackage = overlayPackage2;
                tmpSize = overlayPackagesSize;
                tmpSize2 = overlayPackagesSize2;
                str2 = str3;
                overlayPackages = overlayPackages2;
                storedOverlayInfos2 = storedOverlayInfos3;
                this.mSettings.init(overlayPackage2.packageName, newUserId, overlayPackage2.overlayTarget, overlayPackage2.targetOverlayableName, overlayPackage2.applicationInfo.getBaseCodePath(), overlayPackage2.isStaticOverlayPackage(), overlayPackage2.overlayPriority, overlayPackage2.overlayCategory);
            } else {
                storedOverlayInfos2 = storedOverlayInfos3;
                str2 = str3;
                tmp = tmp2;
                tmpSize = overlayPackagesSize;
                overlayPackage = overlayPackage2;
                tmpSize2 = overlayPackagesSize2;
                overlayPackages = overlayPackages2;
            }
            storedOverlayInfos2.remove(overlayPackage.packageName);
            i5++;
            storedOverlayInfos3 = storedOverlayInfos2;
            overlayPackages2 = overlayPackages;
            overlayPackagesSize2 = tmpSize2;
            tmp2 = tmp;
            overlayPackagesSize = tmpSize;
            str3 = str2;
        }
        ArrayMap<String, OverlayInfo> storedOverlayInfos4 = storedOverlayInfos3;
        String str4 = str3;
        int storedOverlayInfosSize = storedOverlayInfos4.size();
        for (int i6 = 0; i6 < storedOverlayInfosSize; i6++) {
            OverlayInfo oi4 = storedOverlayInfos4.valueAt(i6);
            this.mSettings.remove(oi4.packageName, oi4.userId);
            removeIdmapIfPossible(oi4);
            packagesToUpdateAssets.add(oi4.targetPackageName);
        }
        int i7 = 0;
        while (true) {
            i = 0;
            if (i7 >= overlayPackagesSize2) {
                break;
            }
            PackageInfo overlayPackage3 = overlayPackages2.get(i7);
            try {
                i3 = newUserId;
                storedOverlayInfos = storedOverlayInfos4;
                try {
                    updateState(overlayPackage3.overlayTarget, overlayPackage3.packageName, i3, 0);
                } catch (OverlayManagerSettings.BadKeyException e2) {
                    e = e2;
                }
            } catch (OverlayManagerSettings.BadKeyException e3) {
                e = e3;
                i3 = newUserId;
                storedOverlayInfos = storedOverlayInfos4;
                Slog.e("OverlayManager", "failed to update settings", e);
                this.mSettings.remove(overlayPackage3.packageName, i3);
                packagesToUpdateAssets.add(overlayPackage3.overlayTarget);
                i7++;
                storedOverlayInfos4 = storedOverlayInfos;
            }
            packagesToUpdateAssets.add(overlayPackage3.overlayTarget);
            i7++;
            storedOverlayInfos4 = storedOverlayInfos;
        }
        Iterator<String> iter2 = packagesToUpdateAssets.iterator();
        while (iter2.hasNext()) {
            if (this.mPackageManager.getPackageInfo(iter2.next(), newUserId) == null) {
                iter2.remove();
            }
        }
        ArraySet<String> enabledCategories2 = new ArraySet<>();
        ArrayMap<String, List<OverlayInfo>> userOverlays = this.mSettings.getOverlaysForUser(newUserId);
        int userOverlayTargetCount = userOverlays.size();
        int i8 = 0;
        while (i8 < userOverlayTargetCount) {
            List<OverlayInfo> overlayList = userOverlays.valueAt(i8);
            int overlayCount = overlayList != null ? overlayList.size() : i;
            int j2 = 0;
            while (j2 < overlayCount) {
                OverlayInfo oi5 = overlayList.get(j2);
                if (oi5.isEnabled()) {
                    iter = iter2;
                    enabledCategories2.add(oi5.category);
                } else {
                    iter = iter2;
                }
                j2++;
                iter2 = iter;
                storedOverlayInfosSize = storedOverlayInfosSize;
            }
            i8++;
            i = 0;
        }
        String[] strArr2 = this.mDefaultOverlays;
        int length = strArr2.length;
        int i9 = 0;
        while (i9 < length) {
            String defaultOverlay = strArr2[i9];
            try {
                OverlayInfo oi6 = this.mSettings.getOverlayInfo(defaultOverlay, newUserId);
                if (!enabledCategories2.contains(oi6.category)) {
                    StringBuilder sb = new StringBuilder();
                    strArr = strArr2;
                    try {
                        sb.append("Enabling default overlay '");
                        sb.append(defaultOverlay);
                        sb.append("' for target '");
                        sb.append(oi6.targetPackageName);
                        sb.append("' in category '");
                        sb.append(oi6.category);
                        str = str4;
                    } catch (OverlayManagerSettings.BadKeyException e4) {
                        oi = e4;
                        enabledCategories = enabledCategories2;
                        str = str4;
                        i2 = length;
                        Slog.e("OverlayManager", "Failed to set default overlay '" + defaultOverlay + str + newUserId, oi);
                        i9++;
                        length = i2;
                        enabledCategories2 = enabledCategories;
                        str4 = str;
                        strArr2 = strArr;
                    }
                    try {
                        sb.append(str);
                        sb.append(newUserId);
                        Slog.w("OverlayManager", sb.toString());
                        i2 = length;
                        try {
                            enabledCategories = enabledCategories2;
                            try {
                                this.mSettings.setEnabled(oi6.packageName, newUserId, true);
                            } catch (OverlayManagerSettings.BadKeyException e5) {
                                oi = e5;
                                Slog.e("OverlayManager", "Failed to set default overlay '" + defaultOverlay + str + newUserId, oi);
                                i9++;
                                length = i2;
                                enabledCategories2 = enabledCategories;
                                str4 = str;
                                strArr2 = strArr;
                            }
                        } catch (OverlayManagerSettings.BadKeyException e6) {
                            oi = e6;
                            enabledCategories = enabledCategories2;
                            Slog.e("OverlayManager", "Failed to set default overlay '" + defaultOverlay + str + newUserId, oi);
                            i9++;
                            length = i2;
                            enabledCategories2 = enabledCategories;
                            str4 = str;
                            strArr2 = strArr;
                        }
                    } catch (OverlayManagerSettings.BadKeyException e7) {
                        oi = e7;
                        i2 = length;
                        enabledCategories = enabledCategories2;
                        Slog.e("OverlayManager", "Failed to set default overlay '" + defaultOverlay + str + newUserId, oi);
                        i9++;
                        length = i2;
                        enabledCategories2 = enabledCategories;
                        str4 = str;
                        strArr2 = strArr;
                    }
                    try {
                        if (updateState(oi6.targetPackageName, oi6.packageName, newUserId, 0)) {
                            packagesToUpdateAssets.add(oi6.targetPackageName);
                        }
                    } catch (OverlayManagerSettings.BadKeyException e8) {
                        oi = e8;
                        Slog.e("OverlayManager", "Failed to set default overlay '" + defaultOverlay + str + newUserId, oi);
                        i9++;
                        length = i2;
                        enabledCategories2 = enabledCategories;
                        str4 = str;
                        strArr2 = strArr;
                    }
                } else {
                    strArr = strArr2;
                    enabledCategories = enabledCategories2;
                    str = str4;
                    i2 = length;
                }
            } catch (OverlayManagerSettings.BadKeyException e9) {
                oi = e9;
                strArr = strArr2;
                enabledCategories = enabledCategories2;
                str = str4;
                i2 = length;
                Slog.e("OverlayManager", "Failed to set default overlay '" + defaultOverlay + str + newUserId, oi);
                i9++;
                length = i2;
                enabledCategories2 = enabledCategories;
                str4 = str;
                strArr2 = strArr;
            }
            i9++;
            length = i2;
            enabledCategories2 = enabledCategories;
            str4 = str;
            strArr2 = strArr;
        }
        return new ArrayList<>(packagesToUpdateAssets);
    }

    /* access modifiers changed from: package-private */
    public void onUserRemoved(int userId) {
        this.mSettings.removeUser(userId);
    }

    /* access modifiers changed from: package-private */
    public void onTargetPackageAdded(String packageName, int userId) {
        updateAndRefreshOverlaysForTarget(packageName, userId, 0);
    }

    /* access modifiers changed from: package-private */
    public void onTargetPackageChanged(String packageName, int userId) {
        updateAndRefreshOverlaysForTarget(packageName, userId, 0);
    }

    /* access modifiers changed from: package-private */
    public void onTargetPackageReplacing(String packageName, int userId) {
        updateAndRefreshOverlaysForTarget(packageName, userId, 0);
    }

    /* access modifiers changed from: package-private */
    public void onTargetPackageReplaced(String packageName, int userId) {
        updateAndRefreshOverlaysForTarget(packageName, userId, 0);
    }

    /* access modifiers changed from: package-private */
    public void onTargetPackageRemoved(String packageName, int userId) {
        updateAndRefreshOverlaysForTarget(packageName, userId, 0);
    }

    private void updateAndRefreshOverlaysForTarget(String targetPackageName, int userId, int flags) {
        List<OverlayInfo> targetOverlays = this.mSettings.getOverlaysForTarget(targetPackageName, userId);
        boolean modified = false;
        for (OverlayInfo oi : targetOverlays) {
            if (this.mPackageManager.getPackageInfo(oi.packageName, userId) == null) {
                modified |= this.mSettings.remove(oi.packageName, oi.userId);
                removeIdmapIfPossible(oi);
            } else {
                try {
                    modified |= updateState(targetPackageName, oi.packageName, userId, flags);
                } catch (OverlayManagerSettings.BadKeyException e) {
                    Slog.e("OverlayManager", "failed to update settings", e);
                    modified |= this.mSettings.remove(oi.packageName, userId);
                }
            }
        }
        if (!modified) {
            List<String> enabledOverlayPaths = new ArrayList<>(targetOverlays.size());
            for (OverlayInfo oi2 : this.mSettings.getOverlaysForTarget(PackageManagerService.PLATFORM_PACKAGE_NAME, userId)) {
                if (oi2.isEnabled()) {
                    enabledOverlayPaths.add(oi2.baseCodePath);
                }
            }
            for (OverlayInfo oi3 : targetOverlays) {
                if (oi3.isEnabled()) {
                    enabledOverlayPaths.add(oi3.baseCodePath);
                }
            }
            PackageInfo packageInfo = this.mPackageManager.getPackageInfo(targetPackageName, userId);
            String[] resourceDirs = null;
            ApplicationInfo appInfo = packageInfo == null ? null : packageInfo.applicationInfo;
            if (appInfo != null) {
                resourceDirs = appInfo.resourceDirs;
            }
            if (ArrayUtils.size(resourceDirs) != enabledOverlayPaths.size()) {
                modified = true;
            } else if (resourceDirs != null) {
                int index = 0;
                while (true) {
                    if (index >= resourceDirs.length) {
                        break;
                    } else if (!resourceDirs[index].equals(enabledOverlayPaths.get(index))) {
                        modified = true;
                        break;
                    } else {
                        index++;
                    }
                }
            }
        }
        if (modified) {
            this.mListener.onOverlaysChanged(targetPackageName, userId);
        }
    }

    /* access modifiers changed from: package-private */
    public void onOverlayPackageAdded(String packageName, int userId) {
        PackageInfo overlayPackage = this.mPackageManager.getPackageInfo(packageName, userId);
        if (overlayPackage == null) {
            Slog.w("OverlayManager", "overlay package " + packageName + " was added, but couldn't be found");
            onOverlayPackageRemoved(packageName, userId);
            return;
        }
        this.mSettings.init(packageName, userId, overlayPackage.overlayTarget, overlayPackage.targetOverlayableName, overlayPackage.applicationInfo.getBaseCodePath(), overlayPackage.isStaticOverlayPackage(), overlayPackage.overlayPriority, overlayPackage.overlayCategory);
        try {
            if (updateState(overlayPackage.overlayTarget, packageName, userId, 0)) {
                this.mListener.onOverlaysChanged(overlayPackage.overlayTarget, userId);
            }
        } catch (OverlayManagerSettings.BadKeyException e) {
            Slog.e("OverlayManager", "failed to update settings", e);
            this.mSettings.remove(packageName, userId);
        }
    }

    /* access modifiers changed from: package-private */
    public void onOverlayPackageChanged(String packageName, int userId) {
        try {
            OverlayInfo oi = this.mSettings.getOverlayInfo(packageName, userId);
            if (updateState(oi.targetPackageName, packageName, userId, 0)) {
                this.mListener.onOverlaysChanged(oi.targetPackageName, userId);
            }
        } catch (OverlayManagerSettings.BadKeyException e) {
            Slog.e("OverlayManager", "failed to update settings", e);
        }
    }

    /* access modifiers changed from: package-private */
    public void onOverlayPackageReplacing(String packageName, int userId) {
        try {
            OverlayInfo oi = this.mSettings.getOverlayInfo(packageName, userId);
            if (updateState(oi.targetPackageName, packageName, userId, 2)) {
                removeIdmapIfPossible(oi);
                this.mListener.onOverlaysChanged(oi.targetPackageName, userId);
            }
        } catch (OverlayManagerSettings.BadKeyException e) {
            Slog.e("OverlayManager", "failed to update settings", e);
        }
    }

    /* access modifiers changed from: package-private */
    public void onOverlayPackageReplaced(String packageName, int userId) {
        PackageInfo pkg = this.mPackageManager.getPackageInfo(packageName, userId);
        if (pkg == null) {
            Slog.w("OverlayManager", "overlay package " + packageName + " was replaced, but couldn't be found");
            onOverlayPackageRemoved(packageName, userId);
            return;
        }
        try {
            OverlayInfo oldOi = this.mSettings.getOverlayInfo(packageName, userId);
            if (mustReinitializeOverlay(pkg, oldOi)) {
                if (oldOi != null && !oldOi.targetPackageName.equals(pkg.overlayTarget)) {
                    this.mListener.onOverlaysChanged(pkg.overlayTarget, userId);
                }
                this.mSettings.init(packageName, userId, pkg.overlayTarget, pkg.targetOverlayableName, pkg.applicationInfo.getBaseCodePath(), pkg.isStaticOverlayPackage(), pkg.overlayPriority, pkg.overlayCategory);
            }
            if (updateState(pkg.overlayTarget, packageName, userId, 0)) {
                this.mListener.onOverlaysChanged(pkg.overlayTarget, userId);
            }
        } catch (OverlayManagerSettings.BadKeyException e) {
            Slog.e("OverlayManager", "failed to update settings", e);
        }
    }

    /* access modifiers changed from: package-private */
    public void onOverlayPackageRemoved(String packageName, int userId) {
        try {
            OverlayInfo overlayInfo = this.mSettings.getOverlayInfo(packageName, userId);
            if (this.mSettings.remove(packageName, userId)) {
                removeIdmapIfPossible(overlayInfo);
                this.mListener.onOverlaysChanged(overlayInfo.targetPackageName, userId);
            }
        } catch (OverlayManagerSettings.BadKeyException e) {
            Slog.e("OverlayManager", "failed to remove overlay", e);
        }
    }

    /* access modifiers changed from: package-private */
    public OverlayInfo getOverlayInfo(String packageName, int userId) {
        try {
            return this.mSettings.getOverlayInfo(packageName, userId);
        } catch (OverlayManagerSettings.BadKeyException e) {
            return null;
        }
    }

    /* access modifiers changed from: package-private */
    public List<OverlayInfo> getOverlayInfosForTarget(String targetPackageName, int userId) {
        return this.mSettings.getOverlaysForTarget(targetPackageName, userId);
    }

    /* access modifiers changed from: package-private */
    public Map<String, List<OverlayInfo>> getOverlaysForUser(int userId) {
        return this.mSettings.getOverlaysForUser(userId);
    }

    /* access modifiers changed from: package-private */
    public boolean setEnabled(String packageName, boolean enable, int userId) {
        PackageInfo overlayPackage = this.mPackageManager.getPackageInfo(packageName, userId);
        if (overlayPackage == null || overlayPackage.isStaticOverlayPackage()) {
            return false;
        }
        try {
            OverlayInfo oi = this.mSettings.getOverlayInfo(packageName, userId);
            if (!this.mSettings.setEnabled(packageName, userId, enable) && !updateState(oi.targetPackageName, oi.packageName, userId, 0)) {
                return true;
            }
            this.mListener.onOverlaysChanged(oi.targetPackageName, userId);
            return true;
        } catch (OverlayManagerSettings.BadKeyException e) {
            return false;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean setEnabledExclusive(String packageName, boolean withinCategory, int userId) {
        if (this.mPackageManager.getPackageInfo(packageName, userId) == null) {
            return false;
        }
        try {
            OverlayInfo oi = this.mSettings.getOverlayInfo(packageName, userId);
            String targetPackageName = oi.targetPackageName;
            List<OverlayInfo> allOverlays = getOverlayInfosForTarget(targetPackageName, userId);
            boolean modified = false;
            allOverlays.remove(oi);
            for (int i = 0; i < allOverlays.size(); i++) {
                String disabledOverlayPackageName = allOverlays.get(i).packageName;
                PackageInfo disabledOverlayPackageInfo = this.mPackageManager.getPackageInfo(disabledOverlayPackageName, userId);
                if (disabledOverlayPackageInfo == null) {
                    modified |= this.mSettings.remove(disabledOverlayPackageName, userId);
                } else if (!disabledOverlayPackageInfo.isStaticOverlayPackage()) {
                    if (!withinCategory || Objects.equals(disabledOverlayPackageInfo.overlayCategory, oi.category)) {
                        modified = modified | this.mSettings.setEnabled(disabledOverlayPackageName, userId, false) | updateState(targetPackageName, disabledOverlayPackageName, userId, 0);
                    }
                }
            }
            if ((modified || this.mSettings.setEnabled(packageName, userId, true)) || updateState(targetPackageName, packageName, userId, 0)) {
                this.mListener.onOverlaysChanged(targetPackageName, userId);
            }
            return true;
        } catch (OverlayManagerSettings.BadKeyException e) {
            return false;
        }
    }

    private boolean isPackageUpdatableOverlay(String packageName, int userId) {
        PackageInfo overlayPackage = this.mPackageManager.getPackageInfo(packageName, userId);
        if (overlayPackage == null || overlayPackage.isStaticOverlayPackage()) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public boolean setPriority(String packageName, String newParentPackageName, int userId) {
        PackageInfo overlayPackage;
        if (!isPackageUpdatableOverlay(packageName, userId) || (overlayPackage = this.mPackageManager.getPackageInfo(packageName, userId)) == null) {
            return false;
        }
        if (!this.mSettings.setPriority(packageName, newParentPackageName, userId)) {
            return true;
        }
        this.mListener.onOverlaysChanged(overlayPackage.overlayTarget, userId);
        return true;
    }

    /* access modifiers changed from: package-private */
    public boolean setHighestPriority(String packageName, int userId) {
        PackageInfo overlayPackage;
        if (!isPackageUpdatableOverlay(packageName, userId) || (overlayPackage = this.mPackageManager.getPackageInfo(packageName, userId)) == null) {
            return false;
        }
        if (!this.mSettings.setHighestPriority(packageName, userId)) {
            return true;
        }
        this.mListener.onOverlaysChanged(overlayPackage.overlayTarget, userId);
        return true;
    }

    /* access modifiers changed from: package-private */
    public boolean setLowestPriority(String packageName, int userId) {
        PackageInfo overlayPackage;
        if (!isPackageUpdatableOverlay(packageName, userId) || (overlayPackage = this.mPackageManager.getPackageInfo(packageName, userId)) == null) {
            return false;
        }
        if (!this.mSettings.setLowestPriority(packageName, userId)) {
            return true;
        }
        this.mListener.onOverlaysChanged(overlayPackage.overlayTarget, userId);
        return true;
    }

    /* access modifiers changed from: package-private */
    public void dump(PrintWriter pw, DumpState dumpState) {
        this.mSettings.dump(pw, dumpState);
        if (dumpState.getPackageName() == null) {
            pw.println("Default overlays: " + TextUtils.join(";", this.mDefaultOverlays));
        }
    }

    /* access modifiers changed from: package-private */
    public String[] getDefaultOverlayPackages() {
        return this.mDefaultOverlays;
    }

    /* access modifiers changed from: package-private */
    public List<String> getEnabledOverlayPackageNames(String targetPackageName, int userId) {
        List<OverlayInfo> overlays = this.mSettings.getOverlaysForTarget(targetPackageName, userId);
        List<String> paths = new ArrayList<>(overlays.size());
        int n = overlays.size();
        for (int i = 0; i < n; i++) {
            OverlayInfo oi = overlays.get(i);
            if (oi.isEnabled()) {
                paths.add(oi.packageName);
            }
        }
        return paths;
    }

    private boolean updateState(String targetPackageName, String overlayPackageName, int userId, int flags) throws OverlayManagerSettings.BadKeyException {
        PackageInfo targetPackage = this.mPackageManager.getPackageInfo(targetPackageName, userId);
        PackageInfo overlayPackage = this.mPackageManager.getPackageInfo(overlayPackageName, userId);
        if (!(targetPackage == null || overlayPackage == null || ((PackageManagerService.PLATFORM_PACKAGE_NAME.equals(targetPackage.packageName) || "oppo".equals(targetPackage.packageName)) && overlayPackage.isStaticOverlayPackage()))) {
            this.mIdmapManager.createIdmap(targetPackage, overlayPackage, userId);
        }
        boolean modified = false;
        if (overlayPackage != null) {
            modified = false | this.mSettings.setBaseCodePath(overlayPackageName, userId, overlayPackage.applicationInfo.getBaseCodePath()) | this.mSettings.setCategory(overlayPackageName, userId, overlayPackage.overlayCategory);
        }
        int currentState = this.mSettings.getState(overlayPackageName, userId);
        int newState = calculateNewState(targetPackage, overlayPackage, userId, flags);
        if (currentState != newState) {
            return modified | this.mSettings.setState(overlayPackageName, userId, newState);
        }
        return modified;
    }

    private int calculateNewState(PackageInfo targetPackage, PackageInfo overlayPackage, int userId, int flags) throws OverlayManagerSettings.BadKeyException {
        if ((flags & 1) != 0) {
            return 4;
        }
        if ((flags & 2) != 0) {
            return 5;
        }
        if (targetPackage == null) {
            return 0;
        }
        if (!this.mIdmapManager.idmapExists(overlayPackage, userId)) {
            return 1;
        }
        if (overlayPackage.isStaticOverlayPackage()) {
            return 6;
        }
        return this.mSettings.getEnabled(overlayPackage.packageName, userId) ? 3 : 2;
    }

    private void removeIdmapIfPossible(OverlayInfo oi) {
        if (this.mIdmapManager.idmapExists(oi)) {
            for (int userId : this.mSettings.getUsers()) {
                try {
                    OverlayInfo tmp = this.mSettings.getOverlayInfo(oi.packageName, userId);
                    if (tmp != null && tmp.isEnabled()) {
                        return;
                    }
                } catch (OverlayManagerSettings.BadKeyException e) {
                }
            }
            this.mIdmapManager.removeIdmap(oi, oi.userId);
        }
    }
}
