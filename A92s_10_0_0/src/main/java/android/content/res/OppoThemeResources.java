package android.content.res;

import android.content.res.OppoThemeZipFile;

public class OppoThemeResources {
    public static final String FRAMEWORK_NAME = "framework-res";
    public static final String FRAMEWORK_PACKAGE = "android";
    public static final String ICONS_NAME = "icons";
    public static final String LAUNCHER_PACKAGE = "com.oppo.launcher";
    public static final String LOCKSCREEN_PACKAGE = "lockscreen";
    public static final String OPPO_NAME = "oppo-framework-res";
    public static final String OPPO_PACKAGE = "oppo";
    public static final String SYSTEMUI = "com.android.systemui";
    public static final String SYSTEM_THEME_PATH = "/system/media/theme/default/";
    private static final String TAG = "OppoThemeResources";
    public static final String THEME_PATH = "/data/theme/";
    public static final MetaData[] THEME_PATHS = {new MetaData("/system/media/theme/default/", true, true, true), new MetaData("/data/theme/", true, true, true)};
    private static OppoThemeResourcesSystem sSystem;
    private final boolean debug = true;
    private boolean mHasDrawable;
    private boolean mHasValue;
    private boolean mIsHasWrapped = false;
    private MetaData mMetaData = null;
    private String mPackageName = null;
    private OppoThemeZipFile mPackageZipFile = null;
    private ColorBaseResourcesImpl mResources = null;
    private OppoThemeResources mWrapped = null;

    protected static final class MetaData {
        public boolean supportCharSequence = true;
        public boolean supportFile = true;
        public boolean supportInt = true;
        public String themePath = null;

        public MetaData(String path, boolean supportInt2, boolean supportCharSequence2, boolean supportFile2) {
            this.themePath = path;
        }
    }

    public OppoThemeResources(OppoThemeResources themeResources, ColorBaseResourcesImpl resources, String name, MetaData metaData) {
        if (themeResources != null) {
            if (themeResources.mPackageZipFile != null) {
                this.mWrapped = themeResources;
            } else {
                this.mWrapped = null;
            }
        }
        this.mMetaData = metaData;
        this.mPackageName = name;
        this.mResources = resources;
        checkUpdate();
    }

    public boolean hasValues() {
        return this.mHasValue;
    }

    public static OppoThemeResourcesSystem getSystem(ColorBaseResourcesImpl resources) {
        if (sSystem == null) {
            sSystem = OppoThemeResourcesSystem.getTopLevelThemeResources(resources);
        }
        return sSystem;
    }

    public static OppoThemeResourcesSystem getSystem() {
        return sSystem;
    }

    public static OppoThemeResources getTopLevelThemeResources(ColorBaseResourcesImpl resources, String path) {
        OppoThemeResources themeResources = null;
        int i = 0;
        while (true) {
            MetaData[] metaDataArr = THEME_PATHS;
            if (i >= metaDataArr.length) {
                return themeResources;
            }
            themeResources = new OppoThemeResources(themeResources, resources, path, metaDataArr[i]);
            i++;
        }
    }

    public boolean checkUpdate() {
        ColorBaseResourcesImpl colorBaseResourcesImpl = this.mResources;
        boolean reject = OppoBaseFile.rejectTheme(colorBaseResourcesImpl.typeCasting(colorBaseResourcesImpl), this.mPackageName);
        if (!reject) {
            this.mPackageZipFile = OppoThemeZipFile.getThemeZipFile(this.mMetaData, this.mPackageName, this.mResources);
        } else if (this.mPackageZipFile != null && "/data/theme/".equalsIgnoreCase(this.mMetaData.themePath) && reject) {
            this.mPackageZipFile.clear();
            this.mPackageZipFile = null;
        }
        OppoThemeZipFile oppoThemeZipFile = this.mPackageZipFile;
        if (oppoThemeZipFile != null) {
            oppoThemeZipFile.setResource(this.mResources);
            this.mPackageZipFile.clear();
        }
        boolean z = true;
        this.mIsHasWrapped = this.mWrapped != null;
        if (this.mPackageZipFile == null && !this.mIsHasWrapped) {
            z = false;
        }
        this.mHasValue = z;
        boolean z2 = this.mHasValue;
        this.mHasDrawable = z2;
        return z2 | this.mHasDrawable;
    }

    private boolean hasValuesInner() {
        OppoThemeZipFile oppoThemeZipFile = this.mPackageZipFile;
        if ((oppoThemeZipFile == null || !oppoThemeZipFile.hasValues()) && (!this.mIsHasWrapped || !this.mWrapped.hasValuesInner())) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean isMutiPackage() {
        return false;
    }

    public boolean containsEntry(String path) {
        OppoThemeResources oppoThemeResources;
        OppoThemeZipFile oppoThemeZipFile = this.mPackageZipFile;
        if (oppoThemeZipFile == null) {
            return false;
        }
        boolean isExists = oppoThemeZipFile.containsEntry(path);
        if (isExists || this.mPackageZipFile.exists() || (oppoThemeResources = this.mWrapped) == null) {
            return isExists;
        }
        return oppoThemeResources.mPackageZipFile.containsEntry(path);
    }

    public CharSequence getThemeCharSequence(int id) {
        return getThemeCharSequenceInner(id);
    }

    /* access modifiers changed from: protected */
    public CharSequence getThemeCharSequenceInner(int id) {
        OppoThemeResources oppoThemeResources;
        CharSequence res = null;
        OppoThemeZipFile oppoThemeZipFile = this.mPackageZipFile;
        if (oppoThemeZipFile != null) {
            checkAndInitZip(oppoThemeZipFile);
            res = this.mPackageZipFile.getCharSequence(id);
        }
        if (res != null || !this.mIsHasWrapped || (oppoThemeResources = this.mWrapped) == null) {
            return res;
        }
        checkAndInitZip(oppoThemeResources.mPackageZipFile);
        return this.mWrapped.mPackageZipFile.getCharSequence(id);
    }

    public OppoThemeZipFile.ThemeFileInfo getThemeFileStream(int id, String path) {
        return getThemeFileStream(path);
    }

    public OppoThemeZipFile.ThemeFileInfo getPackageThemeFileStream(int index, String path) {
        return getPackageThemeFileStreamInner(index, path);
    }

    public OppoThemeZipFile.ThemeFileInfo getThemeFileStream(String path) {
        return getThemeFileStreamInner(path);
    }

    public OppoThemeZipFile.ThemeFileInfo getThemeFileStream(String path, boolean useWrap) {
        return getThemeFileStreamInner(path, useWrap);
    }

    /* access modifiers changed from: protected */
    public OppoThemeZipFile.ThemeFileInfo getThemeFileStreamInner(String path, boolean useWrap) {
        OppoThemeZipFile.ThemeFileInfo themeFileInfo = null;
        OppoThemeZipFile oppoThemeZipFile = this.mPackageZipFile;
        if (oppoThemeZipFile != null && !useWrap) {
            checkAndInitZip(oppoThemeZipFile);
            themeFileInfo = this.mPackageZipFile.getInputStream(path);
        }
        OppoThemeResources oppoThemeResources = this.mWrapped;
        if (oppoThemeResources == null || !this.mIsHasWrapped || !useWrap) {
            return themeFileInfo;
        }
        checkAndInitZip(oppoThemeResources.mPackageZipFile);
        return this.mWrapped.mPackageZipFile.getInputStream(path);
    }

    /* access modifiers changed from: protected */
    public OppoThemeZipFile.ThemeFileInfo getThemeFileStreamInner(String path) {
        OppoThemeZipFile.ThemeFileInfo themeFileInfo = null;
        OppoThemeZipFile oppoThemeZipFile = this.mPackageZipFile;
        if (oppoThemeZipFile != null) {
            checkAndInitZip(oppoThemeZipFile);
            themeFileInfo = this.mPackageZipFile.getInputStream(path);
        }
        OppoThemeResources oppoThemeResources = this.mWrapped;
        if (oppoThemeResources == null || themeFileInfo != null || !this.mIsHasWrapped) {
            return themeFileInfo;
        }
        checkAndInitZip(oppoThemeResources.mPackageZipFile);
        return this.mWrapped.mPackageZipFile.getInputStream(path);
    }

    /* access modifiers changed from: protected */
    public OppoThemeZipFile.ThemeFileInfo getPackageThemeFileStreamInner(int index, String path) {
        OppoThemeZipFile.ThemeFileInfo themeFileInfo = null;
        OppoThemeZipFile oppoThemeZipFile = this.mPackageZipFile;
        if (oppoThemeZipFile != null) {
            checkAndInitZip(oppoThemeZipFile);
            themeFileInfo = this.mPackageZipFile.getInputStream(index, path);
        }
        OppoThemeResources oppoThemeResources = this.mWrapped;
        if (oppoThemeResources == null || themeFileInfo != null || !this.mIsHasWrapped) {
            return themeFileInfo;
        }
        checkAndInitZip(oppoThemeResources.mPackageZipFile);
        return this.mWrapped.mPackageZipFile.getInputStream(index, path);
    }

    /* access modifiers changed from: protected */
    public OppoThemeZipFile.ThemeFileInfo getIconFileStream(String path, boolean useWrap) {
        OppoThemeZipFile.ThemeFileInfo themeFileInfo = null;
        OppoThemeZipFile oppoThemeZipFile = this.mPackageZipFile;
        if (oppoThemeZipFile != null && !useWrap) {
            checkAndInitZip(oppoThemeZipFile);
            themeFileInfo = this.mPackageZipFile.getIconInputStream(path);
        }
        OppoThemeResources oppoThemeResources = this.mWrapped;
        if (oppoThemeResources == null || !this.mIsHasWrapped || !useWrap) {
            return themeFileInfo;
        }
        checkAndInitZip(oppoThemeResources.mPackageZipFile);
        return this.mWrapped.mPackageZipFile.getIconInputStream(path);
    }

    public Integer getThemeInt(int id) {
        return getThemeIntInner(id);
    }

    /* access modifiers changed from: protected */
    public Integer getThemeIntInner(int id) {
        OppoThemeResources oppoThemeResources;
        Integer res = null;
        OppoThemeZipFile oppoThemeZipFile = this.mPackageZipFile;
        if (oppoThemeZipFile != null) {
            checkAndInitZip(oppoThemeZipFile);
            res = this.mPackageZipFile.getInt(id);
        }
        if (res != null || !this.mIsHasWrapped || (oppoThemeResources = this.mWrapped) == null) {
            return res;
        }
        checkAndInitZip(oppoThemeResources.mPackageZipFile);
        return this.mWrapped.mPackageZipFile.getInt(id);
    }

    public boolean hasDrawables() {
        return this.mHasDrawable;
    }

    /* access modifiers changed from: protected */
    public boolean hasDrawableInner() {
        OppoThemeResources oppoThemeResources;
        boolean isValid = false;
        OppoThemeZipFile oppoThemeZipFile = this.mPackageZipFile;
        if (oppoThemeZipFile != null) {
            isValid = oppoThemeZipFile.hasZipDrawables();
        }
        if (isValid || !this.mIsHasWrapped || (oppoThemeResources = this.mWrapped) == null) {
            return isValid;
        }
        return oppoThemeResources.mPackageZipFile.hasZipDrawables();
    }

    public void setResource(ColorBaseResourcesImpl res) {
        this.mResources = res;
        OppoThemeZipFile oppoThemeZipFile = this.mPackageZipFile;
        if (oppoThemeZipFile != null) {
            oppoThemeZipFile.setResource(this.mResources);
        }
    }

    public boolean checkPackageNoExit() {
        if (this.mPackageZipFile == null) {
            return false;
        }
        ColorBaseResourcesImpl colorBaseResourcesImpl = this.mResources;
        if (!OppoBaseFile.rejectTheme(colorBaseResourcesImpl.typeCasting(colorBaseResourcesImpl), this.mPackageName)) {
            return !this.mPackageZipFile.exists();
        }
        return false;
    }

    public void clearIfNeeded() {
        OppoThemeZipFile oppoThemeZipFile = this.mPackageZipFile;
        if (oppoThemeZipFile != null) {
            oppoThemeZipFile.clear();
            this.mPackageZipFile = null;
        }
        this.mHasValue = this.mWrapped != null;
        this.mHasDrawable = this.mHasValue;
    }

    private void checkAndInitZip(OppoThemeZipFile zipFile) {
        if (zipFile != null && !zipFile.mHasInit) {
            zipFile.initZipFile();
        }
    }
}
