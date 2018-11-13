package oppo.content.res;

import android.content.res.ResourcesImpl;
import oppo.content.res.OppoThemeZipFile.ThemeFileInfo;

public class OppoThemeResources {
    public static final String FRAMEWORK_NAME = "framework-res";
    public static final String FRAMEWORK_PACKAGE = "android";
    public static final String ICONS_NAME = "icons";
    public static final String OPPO_NAME = "oppo-framework-res";
    public static final String OPPO_PACKAGE = "oppo";
    public static final String SYSTEM_THEME_PATH = "/system/media/theme/default/";
    private static final String TAG = "OppoThemeResources";
    public static final String THEME_PATH = "/data/theme/";
    public static final MetaData[] THEME_PATHS = new MetaData[]{new MetaData("/system/media/theme/default/", true, true, true), new MetaData("/data/theme/", true, true, true)};
    private static OppoThemeResourcesSystem sSystem;
    private final boolean debug = true;
    protected boolean mHasValue;
    protected boolean mIsHasValue = false;
    protected boolean mIsHasWrapped = false;
    private MetaData mMetaData = null;
    private String mPackageName = null;
    protected OppoThemeZipFile mPackageZipFile = null;
    protected ResourcesImpl mResources = null;
    protected boolean mSupportWrapper = false;
    protected OppoThemeResources mWrapped = null;

    protected static final class MetaData {
        public boolean supportCharSequence = true;
        public boolean supportFile = true;
        public boolean supportInt = true;
        public String themePath = null;

        public MetaData(String path, boolean supportInt, boolean supportCharSequence, boolean supportFile) {
            this.themePath = path;
        }
    }

    public OppoThemeResources(OppoThemeResources themeResources, ResourcesImpl resources, String name, MetaData metaData) {
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

    public static OppoThemeResources getSystem(ResourcesImpl resources) {
        if (sSystem == null) {
            sSystem = OppoThemeResourcesSystem.getTopLevelThemeResources(resources);
        }
        return sSystem;
    }

    public static OppoThemeResourcesSystem getSystem() {
        if (sSystem != null) {
            return sSystem;
        }
        return null;
    }

    public static OppoThemeResources getTopLevelThemeResources(ResourcesImpl resources, String path) {
        OppoThemeResources themeResources = null;
        for (MetaData oppoThemeResources : THEME_PATHS) {
            themeResources = new OppoThemeResources(themeResources, resources, path, oppoThemeResources);
        }
        return themeResources;
    }

    public boolean checkUpdate() {
        boolean isFileUpdata = false;
        boolean isWrappedUpdata = false;
        if (this.mPackageZipFile == null) {
            this.mPackageZipFile = OppoThemeZipFile.getThemeZipFile(this.mMetaData, this.mPackageName, this.mResources);
        }
        if (this.mPackageZipFile != null) {
            this.mPackageZipFile.setResource(this.mResources);
            isFileUpdata = this.mPackageZipFile.checkUpdate();
        }
        if (this.mWrapped != null) {
            this.mIsHasWrapped = true;
        } else {
            this.mIsHasWrapped = false;
        }
        if (this.mIsHasWrapped) {
            if (!this.mWrapped.mPackageZipFile.checkUpdate() || this.mWrapped == null) {
                isWrappedUpdata = false;
            } else {
                isWrappedUpdata = true;
            }
        }
        this.mHasValue = hasValuesInner();
        return isFileUpdata || isWrappedUpdata;
    }

    protected boolean hasValuesInner() {
        if ((this.mPackageZipFile == null || !this.mPackageZipFile.hasValues() || this.mPackageZipFile == null) && (!this.mIsHasWrapped || !this.mWrapped.hasValuesInner())) {
            return false;
        }
        return true;
    }

    protected boolean isMutiPackage() {
        return false;
    }

    public boolean containsEntry(String path) {
        if (this.mPackageZipFile == null) {
            return false;
        }
        boolean isExists = this.mPackageZipFile.containsEntry(path);
        if (isExists || (this.mPackageZipFile.exists() ^ 1) == 0 || this.mWrapped == null) {
            return isExists;
        }
        return this.mWrapped.mPackageZipFile.containsEntry(path);
    }

    public CharSequence getThemeCharSequence(int id) {
        return getThemeCharSequenceInner(id);
    }

    protected CharSequence getThemeCharSequenceInner(int id) {
        CharSequence res = null;
        if (this.mPackageZipFile != null) {
            res = this.mPackageZipFile.getThemeCharSequence(id);
        }
        if (res == null && this.mIsHasWrapped && this.mWrapped != null) {
            return this.mWrapped.mPackageZipFile.getThemeCharSequence(id);
        }
        return res;
    }

    public ThemeFileInfo getThemeFileStream(int id, String path) {
        return getThemeFileStream(path);
    }

    public ThemeFileInfo getPackageThemeFileStream(int index, String path) {
        return getPackageThemeFileStreamInner(index, path);
    }

    public ThemeFileInfo getThemeFileStream(String path) {
        return getThemeFileStreamInner(path);
    }

    public ThemeFileInfo getThemeFileStream(String path, boolean useWrap) {
        return getThemeFileStreamInner(path, useWrap);
    }

    protected ThemeFileInfo getThemeFileStreamInner(String path, boolean useWrap) {
        ThemeFileInfo themeFileInfo = null;
        if (!(this.mPackageZipFile == null || (useWrap ^ 1) == 0)) {
            themeFileInfo = this.mPackageZipFile.getInputStream(path);
        }
        if (this.mWrapped != null && this.mIsHasWrapped && useWrap) {
            return this.mWrapped.mPackageZipFile.getInputStream(path);
        }
        return themeFileInfo;
    }

    protected ThemeFileInfo getThemeFileStreamInner(String path) {
        ThemeFileInfo themeFileInfo = null;
        if (this.mPackageZipFile != null) {
            themeFileInfo = this.mPackageZipFile.getInputStream(path);
        }
        if (this.mWrapped != null && themeFileInfo == null && this.mIsHasWrapped) {
            return this.mWrapped.mPackageZipFile.getInputStream(path);
        }
        return themeFileInfo;
    }

    protected ThemeFileInfo getPackageThemeFileStreamInner(int index, String path) {
        ThemeFileInfo themeFileInfo = null;
        if (this.mPackageZipFile != null) {
            themeFileInfo = this.mPackageZipFile.getInputStream(index, path);
        }
        if (this.mWrapped != null && themeFileInfo == null && this.mIsHasWrapped) {
            return this.mWrapped.mPackageZipFile.getInputStream(index, path);
        }
        return themeFileInfo;
    }

    public Integer getThemeInt(int id) {
        return getThemeIntInner(id);
    }

    protected Integer getThemeIntInner(int id) {
        Integer res = null;
        if (this.mPackageZipFile != null) {
            res = this.mPackageZipFile.getThemeInt(id);
        }
        if (res == null && this.mIsHasWrapped && this.mWrapped != null) {
            return this.mWrapped.mPackageZipFile.getThemeInt(id);
        }
        return res;
    }

    public boolean isValid() {
        return isValidInner();
    }

    protected boolean isValidInner() {
        boolean isValid = false;
        if (this.mPackageZipFile != null) {
            isValid = this.mPackageZipFile.isValid();
        }
        if (isValid || !this.mIsHasWrapped || this.mWrapped == null) {
            return isValid;
        }
        return this.mWrapped.mPackageZipFile.isValid();
    }

    public void setResource(ResourcesImpl res) {
        this.mResources = res;
        if (this.mPackageZipFile != null) {
            this.mPackageZipFile.setResource(res);
        }
    }
}
