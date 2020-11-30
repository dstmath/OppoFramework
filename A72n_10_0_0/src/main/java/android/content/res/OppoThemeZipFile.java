package android.content.res;

import android.content.res.OppoThemeResources;
import android.os.Trace;
import android.util.Log;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import oppo.content.res.OppoExtraConfiguration;
import oppo.util.OppoDisplayUtils;

public class OppoThemeZipFile extends OppoBaseFile {
    private static final int ASSETS_THEME_FILE_INDEX_CN = 3;
    private static final int ASSETS_THEME_FILE_INDEX_EN = 4;
    private static final int ASSETS_THEME_FILE_INDEX_TW = 5;
    private static final int ASSETS_THEME_FILE_USE_COUNT = 3;
    private static final String[] ASSETS_THEME_VALUE_FILES = {"assets/colors.xml", "framework-res/assets/colors.xml", "oppo-framework-res/assets/colors.xml", "assets/values-cn/colors.xml", "assets/values-en/colors.xml", "assets/values-tw/colors.xml"};
    private static final String[] RESOURCES_PATHS = {"res/drawable", "framework-res/res/drawable", "oppo-framework-res/res/drawable", "res/drawable"};
    private static final String TAG = "OppoThemeZipFile";
    private static final ConcurrentHashMap<String, ZipEntry> mEntryCache = new ConcurrentHashMap<>();
    protected boolean mHasInit;
    private long mLastModifyTime;
    private OppoThemeResources.MetaData mMetaData;
    private String mPath;
    private MultiZipFile mZipFile;

    public static class ThemeFileInfo {
        public int mDensity;
        public InputStream mInput;
        public long mSize;

        ThemeFileInfo(InputStream input, long size) {
            this.mInput = input;
            this.mSize = size;
        }
    }

    public OppoThemeZipFile(String path, OppoThemeResources.MetaData metaData, String packageName, ColorBaseResourcesImpl baseResources) {
        super(packageName, baseResources, metaData.supportInt, metaData.supportCharSequence, metaData.supportFile);
        this.mLastModifyTime = -1;
        this.mPath = null;
        this.mMetaData = null;
        this.mZipFile = null;
        this.mHasInit = false;
        this.mLastModifyTime = -1;
        this.mPath = path;
        this.mMetaData = metaData;
    }

    public boolean initZipFile() {
        Trace.traceBegin(8192, "OppoThemeZipFile#initZipFile");
        checkPathForUser();
        boolean flag = false;
        if (this.mZipFile != null) {
            clear();
        }
        openZipFile();
        if (this.mPackageName.equals("android") || this.mPackageName.equals(OppoThemeResources.OPPO_PACKAGE)) {
            loadThemeValues(0, this.mZipFile);
            flag = true;
        } else if (this.mZipFile != null) {
            for (int i = 0; i < 3; i++) {
                loadThemeValues(i, this.mZipFile);
            }
            flag = true;
        }
        this.mHasInit = flag;
        Trace.traceEnd(8192);
        return flag;
    }

    public ThemeFileInfo getInputStream(String path) {
        return getInputStream(0, path);
    }

    public ThemeFileInfo getInputStream(int index, String path) {
        if (!this.mMetaData.supportFile || this.mZipFile == null) {
            return null;
        }
        if (path.endsWith(".xml")) {
            String path2 = path.substring(0, path.lastIndexOf(".")) + ".png";
            ThemeFileInfo themeFileInfo = getInputStreamInner(index, path2, this.mZipFile, false);
            if (themeFileInfo != null) {
                return themeFileInfo;
            }
            return getInputStreamInner(index, path2.substring(0, path2.lastIndexOf(".")) + ".9.png", this.mZipFile, false);
        }
        ThemeFileInfo themeFileInfo2 = getInputStreamInner(index, path, this.mZipFile, false);
        if (!path.endsWith(".9.png") || themeFileInfo2 != null) {
            return themeFileInfo2;
        }
        return getInputStreamInner(index, path.replace(".9.png", ".png"), this.mZipFile, false);
    }

    public ThemeFileInfo getIconInputStream(String path) {
        MultiZipFile multiZipFile;
        if (!this.mMetaData.supportFile || (multiZipFile = this.mZipFile) == null) {
            return null;
        }
        ThemeFileInfo themeFileInfo = getInputStreamInner(0, path, multiZipFile, true);
        if (!path.endsWith(".xml") || themeFileInfo != null) {
            return themeFileInfo;
        }
        return getInputStreamInner(0, path.substring(0, path.lastIndexOf(".")) + ".png", this.mZipFile, true);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:8:0x001f, code lost:
        if (r4 != r2) goto L_0x0021;
     */
    public boolean isZipFileValid(boolean isCheck) {
        boolean valid = true;
        if (!isCheck) {
            return true;
        }
        File file = new File(this.mPath);
        long modifyTime = file.lastModified();
        if (file.exists()) {
            long j = this.mLastModifyTime;
            if (j != -1) {
            }
            return valid;
        }
        valid = false;
        if (DEBUG_THEME) {
            Log.e(TAG, "check zip invalid: " + this.mPath + " mLastModifyTime= " + this.mLastModifyTime + " modifyTime= " + modifyTime);
        }
        return valid;
    }

    public boolean containsEntry(String s) {
        MultiZipFile multiZipFile = this.mZipFile;
        if (multiZipFile == null || multiZipFile.getEntry(s) == null) {
            return false;
        }
        return true;
    }

    public boolean exists() {
        return new File(this.mPath).exists();
    }

    /* JADX WARNING: Removed duplicated region for block: B:19:0x004e  */
    /* JADX WARNING: Removed duplicated region for block: B:21:0x0053  */
    /* JADX WARNING: Removed duplicated region for block: B:22:0x0066  */
    protected static synchronized OppoThemeZipFile getThemeZipFile(OppoThemeResources.MetaData metadata, String packageName, ColorBaseResourcesImpl resources) {
        String path;
        OppoThemeZipFile themeZipFile;
        OppoExtraConfiguration extraConfig;
        synchronized (OppoThemeZipFile.class) {
            if (metadata == null) {
                return null;
            }
            if ("/system/media/theme/default/".equalsIgnoreCase(metadata.themePath)) {
                path = metadata.themePath + packageName;
            } else {
                int userId = 0;
                if (!OppoThemeResources.FRAMEWORK_NAME.equals(packageName)) {
                    if (!OppoThemeResources.OPPO_NAME.equals(packageName)) {
                        extraConfig = resources.getSystemConfiguration().getOppoExtraConfiguration();
                        if (extraConfig != null) {
                            userId = extraConfig.mUserId;
                        }
                        if (userId != 0) {
                            path = metadata.themePath + packageName;
                        } else {
                            path = metadata.themePath + userId + "/" + packageName;
                        }
                    }
                }
                extraConfig = resources.getConfiguration().getOppoExtraConfiguration();
                if (extraConfig != null) {
                }
                if (userId != 0) {
                }
            }
            boolean isPackageFile = false;
            File packageFile = new File(path);
            try {
                if (packageFile.exists() && !packageFile.isDirectory()) {
                    isPackageFile = true;
                }
            } catch (Exception e) {
                if (DEBUG_THEME) {
                    Log.e(TAG, "getThemeZipFile Exception e: " + e);
                }
            } catch (Throwable th) {
                throw th;
            }
            if (!isPackageFile) {
                return null;
            }
            WeakReference weakreference = (WeakReference) sCacheFiles.get(path);
            if (weakreference != null) {
                themeZipFile = (OppoThemeZipFile) weakreference.get();
            } else {
                themeZipFile = null;
            }
            if (themeZipFile != null) {
                return themeZipFile;
            }
            OppoThemeZipFile themeZipFile2 = new OppoThemeZipFile(path, metadata, getPackageName(packageName), resources);
            sCacheFiles.put(path, new WeakReference(themeZipFile2));
            return themeZipFile2;
        }
    }

    /* access modifiers changed from: protected */
    public boolean hasZipDrawables() {
        boolean hasDrawable = false;
        MultiZipFile multiZipFile = this.mZipFile;
        if (multiZipFile == null) {
            return false;
        }
        synchronized (multiZipFile) {
            try {
                Enumeration<?> entrys = this.mZipFile.entries();
                while (true) {
                    if (entrys.hasMoreElements()) {
                        ZipEntry enumEntry = (ZipEntry) entrys.nextElement();
                        if (enumEntry != null && !enumEntry.isDirectory() && enumEntry.getName() != null && enumEntry.getName().contains(RESOURCES_PATHS[0])) {
                            hasDrawable = true;
                            break;
                        }
                    } else {
                        break;
                    }
                }
            } catch (IllegalArgumentException e) {
                if (DEBUG_THEME) {
                    Log.e(TAG, "Exception when hasZipDrawables, msg = " + e.toString());
                }
            }
        }
        return hasDrawable;
    }

    /* access modifiers changed from: protected */
    public synchronized void clear() {
        clean(this.mZipFile);
        mEntryCache.clear();
        this.mHasInit = false;
    }

    private ThemeFileInfo getInputStreamInner(int index, String path, MultiZipFile file, boolean isCheck) {
        ThemeFileInfo themeFileInfo = getZipInputStream(path, file, isCheck);
        if (themeFileInfo == null && file != null) {
            String str2 = RESOURCES_PATHS[index];
            int i = path.lastIndexOf("/");
            if (i > 0) {
                String str1 = path.substring(i);
                int j = 0;
                while (true) {
                    if (j >= sDensities.length) {
                        break;
                    }
                    String temp = str2 + OppoDisplayUtils.getDensitySuffix(sDensities[j]) + str1;
                    if (path.equalsIgnoreCase(temp) || (themeFileInfo = getZipInputStream(temp, file, isCheck)) == null) {
                        j++;
                    } else if (sDensities[j] > 1) {
                        themeFileInfo.mDensity = sDensities[j];
                    }
                }
            }
        }
        return themeFileInfo;
    }

    private ThemeFileInfo getZipInputStream(String path, MultiZipFile file, boolean isCheck) {
        InputStream inputStream;
        if (file == null) {
            return null;
        }
        ZipEntry zipEntry = mEntryCache.get(path);
        if (zipEntry == null && isZipFileValid(isCheck)) {
            zipEntry = this.mZipFile.getEntry(path);
        }
        if (zipEntry == null) {
            return null;
        }
        try {
            if (!isZipFileValid(isCheck) || (inputStream = file.getInputStream(zipEntry)) == null) {
                return null;
            }
            mEntryCache.put(path, zipEntry);
            return new ThemeFileInfo(inputStream, zipEntry.getSize());
        } catch (Exception e) {
            if (!DEBUG_THEME) {
                return null;
            }
            Log.e(TAG, "OppoThemeZipFile Exception e: " + e + " path= " + path + " file= " + file);
            return null;
        }
    }

    private synchronized void openZipFile() {
        File file = new File(this.mPath);
        if (!file.exists() || file.isDirectory()) {
            this.mZipFile = null;
        } else {
            this.mLastModifyTime = file.lastModified();
            if (this.mLastModifyTime != -1) {
                try {
                    this.mZipFile = new MultiZipFile(file);
                } catch (Exception exception) {
                    if (DEBUG_THEME) {
                        Log.w(TAG, "openZipFile Exception e: " + exception + " path= " + this.mPath);
                    }
                }
            }
        }
    }

    private void loadThemeValues(int index, MultiZipFile file) {
        parseXmlStream(index, getZipInputStream(String.format(ASSETS_THEME_VALUE_FILES[index], OppoDisplayUtils.getDensitySuffix(sDensities[sDensities.length - 1])), file, false));
    }

    /* access modifiers changed from: private */
    public final class MultiZipFile extends ZipFile {
        public MultiZipFile(File file) throws IOException {
            super(file);
        }
    }

    private void checkPathForUser() {
        if ("com.android.systemui".equals(this.mPackageName) && "/data/theme/".equals(this.mMetaData.themePath)) {
            OppoExtraConfiguration extraConfig = this.mBaseResources.getSystemConfiguration().getOppoExtraConfiguration();
            int userId = extraConfig == null ? 0 : extraConfig.mUserId;
            if (userId == 0) {
                this.mPath = this.mMetaData.themePath + this.mPackageName;
                return;
            }
            this.mPath = this.mMetaData.themePath + userId + "/" + this.mPackageName;
        }
    }
}
