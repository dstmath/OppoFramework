package oppo.content.res;

import android.app.OppoThemeHelper;
import android.content.res.ResourcesImpl;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import com.android.internal.util.XmlUtils;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import oppo.util.OppoDisplayUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

public class OppoThemeZipFile {
    private static final String ANDROID_PACKAGE = "android";
    private static final String APP_NAME_ZIP_PATH = "/data/oppo/common/appNameChange/";
    private static final int ASSETS_THEME_FILE_INDEX_CN = 3;
    private static final int ASSETS_THEME_FILE_INDEX_EN = 4;
    private static final int ASSETS_THEME_FILE_INDEX_TW = 5;
    private static final int ASSETS_THEME_FILE_USE_COUNT = 3;
    private static final String[] ASSETS_THEME_VALUE_FILES = new String[]{"assets/colors.xml", "framework-res/assets/colors.xml", "oppo-framework-res/assets/colors.xml", "assets/values-cn/colors.xml", "assets/values-en/colors.xml", "assets/values-tw/colors.xml"};
    private static final String ATTR_NAME = "name";
    private static final String ATTR_PACKAGE = "package";
    private static final int INPUT_STREAM_CACHE_BYTE_COUNT = 8192;
    private static final String OPPO_PACKAGE = "oppo";
    private static final String[] RESOURCES_PATHS = new String[]{"res/drawable", "framework-res/res/drawable", "oppo-framework-res/res/drawable", "res/drawable"};
    private static final String TAG = "OppoThemeZipFile";
    private static final String TAG_BOOLEAN = "bool";
    private static final String TAG_COLOR = "color";
    private static final String TAG_DIMEN = "dimen";
    private static final String TAG_DRAWABLE = "drawable";
    private static final String TAG_INTEGER = "integer";
    private static final String TAG_STRING = "string";
    private static final String TRUE = "true";
    private static int[] sDensities = OppoDisplayUtils.getBestDensityOrder(sDensity);
    private static int sDensity = DisplayMetrics.DENSITY_DEVICE;
    private static Map<String, WeakReference<OppoThemeZipFile>> sThemeZipFiles = new HashMap();
    private final boolean DEBUG;
    private SparseArray mCharSequences;
    private SparseArray mIntegers;
    private long mLastModifyTime;
    private MetaData mMetaData;
    private String mPackageName;
    private String mPath;
    private ResourcesImpl mResources;
    private long mStringLastTime;
    private String mStringPath;
    private ZipFile mStringZipFile;
    private ZipFile mZipFile;
    private Locale mlastLocale;

    public class ThemeFileInfo {
        public int mDensity;
        public InputStream mInput;
        public long mSize;

        ThemeFileInfo(InputStream input, long size) {
            this.mInput = input;
            this.mSize = size;
        }
    }

    public OppoThemeZipFile(String path, MetaData metaData, String packageName, ResourcesImpl resources) {
        this.DEBUG = true;
        this.mLastModifyTime = -1;
        this.mStringLastTime = -1;
        this.mIntegers = null;
        this.mCharSequences = null;
        this.mResources = null;
        this.mPackageName = null;
        this.mPath = null;
        this.mMetaData = null;
        this.mZipFile = null;
        this.mStringZipFile = null;
        this.mStringPath = null;
        this.mlastLocale = null;
        this.mLastModifyTime = -1;
        this.mStringLastTime = -1;
        this.mIntegers = new SparseArray();
        this.mCharSequences = new SparseArray();
        this.mResources = resources;
        this.mPackageName = packageName;
        this.mPath = path;
        this.mMetaData = metaData;
    }

    public void setStringPath(String path) {
        this.mStringPath = path;
    }

    protected static synchronized OppoThemeZipFile getThemeZipFile(MetaData metadata, String packageName, ResourcesImpl resources) {
        Throwable th;
        synchronized (OppoThemeZipFile.class) {
            if (metadata == null) {
                return null;
            }
            try {
                String path = metadata.themePath + packageName;
                File file = null;
                String stringPath = null;
                if ("/data/theme/".equalsIgnoreCase(metadata.themePath)) {
                    stringPath = APP_NAME_ZIP_PATH + packageName;
                    file = new File(stringPath);
                }
                boolean isPackageFile = false;
                boolean isStringFile = false;
                try {
                    if (new File(path).exists()) {
                        isPackageFile = true;
                    }
                } catch (Exception e) {
                    Log.w(TAG, "getThemeZipFile Exception e: " + e);
                } catch (Throwable th2) {
                }
                if (file != null) {
                    try {
                        if (file.exists()) {
                            isStringFile = true;
                        }
                    } catch (Exception e2) {
                        Log.w(TAG, "getThemeZipFile Exception e: " + e2);
                    } catch (Throwable th3) {
                    }
                }
                if (isStringFile || (isPackageFile ^ 1) == 0) {
                    OppoThemeZipFile themeZipFile;
                    WeakReference weakreference = (WeakReference) sThemeZipFiles.get(path);
                    if (weakreference != null) {
                        themeZipFile = (OppoThemeZipFile) weakreference.get();
                    } else {
                        themeZipFile = null;
                    }
                    if (themeZipFile != null) {
                        return themeZipFile;
                    }
                    OppoThemeZipFile themeZipFile2;
                    try {
                        themeZipFile2 = new OppoThemeZipFile(path, metadata, getPackageName(packageName), resources);
                        if (isStringFile) {
                            themeZipFile2.setStringPath(stringPath);
                        }
                        sThemeZipFiles.put(path, new WeakReference(themeZipFile2));
                        return themeZipFile2;
                    } catch (Throwable th4) {
                        th = th4;
                        themeZipFile2 = themeZipFile;
                        throw th;
                    }
                }
                return null;
            } catch (Throwable th5) {
                th = th5;
            }
        }
    }

    public ThemeFileInfo getInputStream(String path) {
        return getInputStream(0, path);
    }

    public ThemeFileInfo getInputStream(int index, String path) {
        if (!this.mMetaData.supportFile) {
            return null;
        }
        if (this.mZipFile != null) {
            return getInputStreamInner(index, path, this.mZipFile);
        }
        return null;
    }

    private ThemeFileInfo getInputStreamInner(int index, String path, ZipFile file) {
        ThemeFileInfo themeFileInfo = getZipInputStream(path, file);
        if (themeFileInfo == null && isZipFileValid(file)) {
            String str2 = RESOURCES_PATHS[index];
            int i = path.lastIndexOf("/");
            if (i > 0) {
                String str1 = path.substring(i);
                for (int j = 0; j < sDensities.length; j++) {
                    themeFileInfo = getZipInputStream(String.format("%s%s%s", new Object[]{str2, OppoDisplayUtils.getDensitySuffix(sDensities[j]), str1}), file);
                    if (themeFileInfo != null) {
                        if (sDensities[j] > 1) {
                            themeFileInfo.mDensity = sDensities[j];
                        }
                        if (themeFileInfo != null) {
                            break;
                        }
                    }
                }
            }
        }
        return themeFileInfo;
    }

    private ThemeFileInfo getZipInputStream(String path, ZipFile file) {
        ThemeFileInfo themeFileInfo = null;
        if (isZipFileValid(file)) {
            ZipEntry zipEntry = file.getEntry(path);
            if (zipEntry != null) {
                try {
                    InputStream inputStream = file.getInputStream(zipEntry);
                    if (inputStream != null) {
                        ThemeFileInfo themeFileInfo2 = new ThemeFileInfo(inputStream, zipEntry.getSize());
                        if (themeFileInfo2 != null) {
                            return themeFileInfo2;
                        }
                        themeFileInfo = themeFileInfo2;
                    }
                } catch (Exception e) {
                    Log.w(TAG, "OppoThemeZipFile Exception e: " + e + " path= " + path + " file= " + file);
                }
            }
        }
        return themeFileInfo;
    }

    private static final String getPackageName(String packageName) {
        if (OppoThemeResources.FRAMEWORK_NAME.equals(packageName) || ("icons".equals(packageName) ^ 1) == 0) {
            packageName = "android";
        } else if (!OppoThemeResources.OPPO_NAME.equals(packageName) && !"lockscreen".equals(packageName)) {
            return packageName;
        } else {
            packageName = "oppo";
        }
        return packageName;
    }

    public boolean isValid() {
        return (this.mZipFile == null && this.mStringZipFile == null) ? false : true;
    }

    public boolean isZipFileValid(ZipFile file) {
        return file != null;
    }

    private void clean(ZipFile file) {
        if (file != null) {
            try {
                file.close();
            } catch (Exception exception) {
                Log.w(TAG, "OppoThemeZipFile Exception exception: " + exception);
            }
        }
        this.mIntegers.clear();
        this.mCharSequences.clear();
    }

    public CharSequence getThemeCharSequence(int i) {
        return (CharSequence) this.mCharSequences.get(i);
    }

    public Integer getThemeInt(int id) {
        return (Integer) this.mIntegers.get(id);
    }

    public boolean hasValues() {
        if (this.mIntegers.size() > 0 || this.mCharSequences.size() > 0) {
            return true;
        }
        return false;
    }

    public boolean checkUpdate() {
        long l = new File(this.mPath).lastModified();
        long time = -1;
        boolean languageChanged = false;
        Locale locale = this.mResources.getConfiguration().locale;
        if (!(this.mlastLocale == null || locale == null || (this.mlastLocale.equals(locale) ^ 1) == 0)) {
            languageChanged = true;
        }
        this.mlastLocale = locale;
        if (this.mStringPath != null) {
            time = new File(this.mStringPath).lastModified();
        }
        if (this.mLastModifyTime == l && this.mStringLastTime == time && (languageChanged ^ 1) != 0) {
            return false;
        }
        if (this.mZipFile != null) {
            clean(this.mZipFile);
        }
        if (this.mStringZipFile != null) {
            clean(this.mStringZipFile);
        }
        openZipFile();
        if (!this.mPackageName.equals("android") && !this.mPackageName.equals("oppo")) {
            String country = locale.getCountry();
            String language = locale.getLanguage();
            if (this.mZipFile != null) {
                loadThemeValues(0, this.mZipFile);
            }
            if ("CN".equalsIgnoreCase(country)) {
                loadThemeValues(3, this.mStringZipFile);
            } else if ("TW".equalsIgnoreCase(country)) {
                loadThemeValues(5, this.mStringZipFile);
            } else if ("en".equalsIgnoreCase(language)) {
                loadThemeValues(4, this.mStringZipFile);
            }
            int i = 0;
            while (i < 3) {
                if (!(this.mZipFile == null || i == 0)) {
                    loadThemeValues(i, this.mZipFile);
                }
                if (this.mStringZipFile != null) {
                    loadThemeValues(i, this.mStringZipFile);
                }
                i++;
            }
        } else if (this.mZipFile != null) {
            loadThemeValues(0, this.mZipFile);
        }
        return true;
    }

    private void openZipFile() {
        File file = new File(this.mPath);
        File file2 = null;
        if (this.mStringPath != null) {
            file2 = new File(this.mStringPath);
        }
        if (file2 == null || !file2.exists()) {
            this.mStringZipFile = null;
        } else {
            this.mStringLastTime = file2.lastModified();
            if (this.mStringLastTime != -1) {
                try {
                    this.mStringZipFile = new ZipFile(file2);
                } catch (Exception e) {
                    this.mStringZipFile = null;
                }
            }
        }
        if (file.exists()) {
            this.mLastModifyTime = file.lastModified();
            if (this.mLastModifyTime != -1) {
                try {
                    this.mZipFile = new ZipFile(file);
                    return;
                } catch (Exception e2) {
                    this.mZipFile = null;
                    return;
                }
            }
            return;
        }
        this.mZipFile = null;
    }

    /* JADX WARNING: Removed duplicated region for block: B:36:0x00f6 A:{SYNTHETIC, Splitter: B:36:0x00f6} */
    /* JADX WARNING: Removed duplicated region for block: B:39:0x00fb A:{SYNTHETIC, Splitter: B:39:0x00fb} */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x00a9 A:{SYNTHETIC, Splitter: B:25:0x00a9} */
    /* JADX WARNING: Removed duplicated region for block: B:28:0x00ae A:{SYNTHETIC, Splitter: B:28:0x00ae} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void loadThemeValues(int index, ZipFile file) {
        XmlPullParserException e;
        Throwable th;
        int i = sDensities.length - 1;
        ThemeFileInfo themeFileInfo = getZipInputStream(String.format(ASSETS_THEME_VALUE_FILES[index], new Object[]{OppoDisplayUtils.getDensitySuffix(sDensities[i])}), file);
        if (themeFileInfo != null) {
            InputStream inputStream = null;
            BufferedInputStream bufferedinputstream = null;
            try {
                inputStream = themeFileInfo.mInput;
                XmlPullParser xmlpullparser = XmlPullParserFactory.newInstance().newPullParser();
                BufferedInputStream bufferedinputstream2 = new BufferedInputStream(inputStream, INPUT_STREAM_CACHE_BYTE_COUNT);
                try {
                    xmlpullparser.setInput(bufferedinputstream2, null);
                    mergeThemeValues(index, xmlpullparser);
                    if (bufferedinputstream2 != null) {
                        try {
                            bufferedinputstream2.close();
                        } catch (IOException e2) {
                            Log.e(TAG, "bufferedinputstream IOException happened when loadThemeValues, msg = " + e2.getMessage());
                        }
                    }
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e22) {
                            Log.e(TAG, "in IOException happened when loadThemeValues, msg = " + e22.getMessage());
                        }
                    }
                    bufferedinputstream = bufferedinputstream2;
                } catch (XmlPullParserException e3) {
                    e = e3;
                    bufferedinputstream = bufferedinputstream2;
                    try {
                        Log.e(TAG, "XmlPullParserException happened when loadThemeValues, msg = " + e.getMessage());
                        if (bufferedinputstream != null) {
                            try {
                                bufferedinputstream.close();
                            } catch (IOException e222) {
                                Log.e(TAG, "bufferedinputstream IOException happened when loadThemeValues, msg = " + e222.getMessage());
                            }
                        }
                        if (inputStream != null) {
                            try {
                                inputStream.close();
                            } catch (IOException e2222) {
                                Log.e(TAG, "in IOException happened when loadThemeValues, msg = " + e2222.getMessage());
                            }
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        if (bufferedinputstream != null) {
                            try {
                                bufferedinputstream.close();
                            } catch (IOException e22222) {
                                Log.e(TAG, "bufferedinputstream IOException happened when loadThemeValues, msg = " + e22222.getMessage());
                            }
                        }
                        if (inputStream != null) {
                            try {
                                inputStream.close();
                            } catch (IOException e222222) {
                                Log.e(TAG, "in IOException happened when loadThemeValues, msg = " + e222222.getMessage());
                            }
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    bufferedinputstream = bufferedinputstream2;
                    if (bufferedinputstream != null) {
                    }
                    if (inputStream != null) {
                    }
                    throw th;
                }
            } catch (XmlPullParserException e4) {
                e = e4;
                Log.e(TAG, "XmlPullParserException happened when loadThemeValues, msg = " + e.getMessage());
                if (bufferedinputstream != null) {
                }
                if (inputStream != null) {
                }
            }
        }
    }

    private void mergeThemeValues(int index, XmlPullParser xmlpullparser) {
        String packageName = null;
        String resourceName = null;
        try {
            int eventType = xmlpullparser.getEventType();
            while (eventType != 1) {
                switch (eventType) {
                    case 2:
                        String resourceType = xmlpullparser.getName().trim();
                        if (TextUtils.isEmpty(resourceType)) {
                            continue;
                        } else {
                            int count = xmlpullparser.getAttributeCount();
                            if (count > 0) {
                                for (int i = 0; i < count; i++) {
                                    String attributeName = xmlpullparser.getAttributeName(i).trim();
                                    if (attributeName.equals(ATTR_NAME)) {
                                        resourceName = xmlpullparser.getAttributeValue(i);
                                    } else if (attributeName.equals(ATTR_PACKAGE)) {
                                        packageName = xmlpullparser.getAttributeValue(i);
                                    }
                                }
                                String resourceValue = xmlpullparser.nextText();
                                if (!(TextUtils.isEmpty(resourceName) || (TextUtils.isEmpty(resourceValue) ^ 1) == 0)) {
                                    if (TextUtils.isEmpty(packageName)) {
                                        if (index == 0 || index > 2) {
                                            packageName = this.mPackageName;
                                        } else if (index == 1) {
                                            packageName = "android";
                                        } else if (index == 2) {
                                            packageName = "oppo";
                                        }
                                    }
                                    int resourceId = this.mResources.getIdentifier(resourceName, resourceType, packageName);
                                    if (resourceId > 0) {
                                        if (!resourceType.equals(TAG_BOOLEAN)) {
                                            if (!resourceType.equals(TAG_COLOR) && !resourceType.equals(TAG_INTEGER) && !resourceType.equals(TAG_DRAWABLE)) {
                                                if (!resourceType.equals(TAG_STRING)) {
                                                    if (resourceType.equals(TAG_DIMEN) && this.mMetaData.supportInt && this.mIntegers.indexOfKey(resourceId) < 0) {
                                                        Integer integer = OppoThemeHelper.parseDimension(this.mResources, resourceValue.toString());
                                                        if (integer == null) {
                                                            break;
                                                        }
                                                        this.mIntegers.put(resourceId, integer);
                                                        break;
                                                    }
                                                } else if (this.mMetaData.supportCharSequence && this.mCharSequences.indexOfKey(resourceId) < 0) {
                                                    this.mCharSequences.put(resourceId, resourceValue);
                                                    break;
                                                }
                                            } else if (this.mMetaData.supportInt && this.mIntegers.indexOfKey(resourceId) < 0) {
                                                try {
                                                    this.mIntegers.put(resourceId, Integer.valueOf(XmlUtils.convertValueToUnsignedInt(resourceValue.trim(), 0)));
                                                    break;
                                                } catch (NumberFormatException e) {
                                                    Log.e(TAG, "mergeThemeValues NumberFormatException happened when loadThemeValues, msg = " + e.getMessage());
                                                    break;
                                                }
                                            }
                                        } else if (this.mMetaData.supportInt && this.mIntegers.indexOfKey(resourceId) < 0) {
                                            if (!TRUE.equals(resourceValue.trim())) {
                                                this.mIntegers.put(resourceId, Integer.valueOf(0));
                                                break;
                                            } else {
                                                this.mIntegers.put(resourceId, Integer.valueOf(1));
                                                break;
                                            }
                                        }
                                    }
                                    continue;
                                }
                            } else {
                                continue;
                            }
                        }
                        break;
                    default:
                        break;
                }
                eventType = xmlpullparser.next();
            }
        } catch (XmlPullParserException e2) {
            Log.e(TAG, "mergeThemeValues XmlPullParserException happened when loadThemeValues, msg = " + e2.getMessage());
        } catch (IOException e3) {
            Log.e(TAG, "mergeThemeValues IOException happened when loadThemeValues, msg = " + e3.getMessage());
        }
    }

    public boolean containsEntry(String s) {
        if (!isValid() || this.mZipFile.getEntry(s) == null) {
            return false;
        }
        return true;
    }

    public boolean exists() {
        return new File(this.mPath).exists();
    }

    public void setResource(ResourcesImpl res) {
        this.mResources = res;
    }
}
