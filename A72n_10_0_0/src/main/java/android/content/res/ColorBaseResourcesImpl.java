package android.content.res;

import android.app.OppoThemeHelper;
import android.content.res.OppoThemeZipFile;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.StrictMode;
import android.os.Trace;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.util.TypedValue;
import com.oppo.theme.OppoAppIconInfo;
import java.io.IOException;
import java.io.InputStream;
import oppo.content.res.OppoExtraConfiguration;

public abstract class ColorBaseResourcesImpl {
    public static final int COOKIE_TYPE_FRAMEWORK = 1;
    public static final int COOKIE_TYPE_OPPO = 2;
    public static final int COOKIE_TYPE_OTHERPACKAGE = 3;
    private static final boolean DEBUG = false;
    private static final String TAG = "ColorBaseResourcesImpl";
    protected OppoAccessibleResources mAccessibleResources;
    protected SparseArray<CharSequence> mCharSequences = new SparseArray<>();
    protected SparseIntArray mCookies = new SparseIntArray();
    protected OppoThemeResourcesPackage mIconThemeResources;
    protected SparseArray<Integer> mIntegers = new SparseArray<>();
    protected boolean mIsHasAcessDrawables = false;
    protected boolean mIsHasAcessValues = false;
    protected boolean mIsHasDrawables = false;
    protected boolean mIsHasSystemDrawables = false;
    protected boolean mIsHasSystemValues = false;
    protected boolean mIsHasValues = false;
    protected SparseArray<Boolean> mLoadArrary = new SparseArray<>();
    private String mName;
    protected SparseArray<Boolean> mSkipFiles = new SparseArray<>();
    protected SparseArray<CharSequence> mSkipIcons = new SparseArray<>();
    protected OppoThemeResourcesSystem mSystemThemeResources;
    protected boolean mThemeChanged = false;
    protected OppoThemeResources mThemeResources;

    /* access modifiers changed from: package-private */
    public abstract ColorBaseConfiguration getConfiguration();

    /* access modifiers changed from: package-private */
    public abstract String getResourcePackageName(int i);

    public abstract ColorBaseConfiguration getSystemConfiguration();

    /* access modifiers changed from: package-private */
    public abstract void getValue(int i, TypedValue typedValue, boolean z);

    /* access modifiers changed from: package-private */
    public void clearCache() {
        this.mThemeChanged = true;
    }

    public void init(String name) {
        this.mName = name;
        initThemeResource(name);
        OppoThemeResources oppoThemeResources = this.mThemeResources;
        if (oppoThemeResources != null) {
            this.mIsHasValues = oppoThemeResources.hasValues();
            this.mIsHasDrawables = this.mThemeResources.hasDrawables();
        }
        OppoThemeResourcesSystem oppoThemeResourcesSystem = this.mSystemThemeResources;
        if (oppoThemeResourcesSystem != null) {
            this.mIsHasSystemValues = oppoThemeResourcesSystem.hasValues();
            this.mIsHasSystemDrawables = this.mSystemThemeResources.hasDrawables();
        }
        OppoAccessibleResources oppoAccessibleResources = this.mAccessibleResources;
        if (oppoAccessibleResources != null) {
            this.mIsHasAcessValues = oppoAccessibleResources.hasValues();
            this.mIsHasAcessDrawables = this.mAccessibleResources.hasDrawables();
        }
    }

    public synchronized Drawable loadOverlayDrawable(Resources wrapper, TypedValue value, int id) {
        if (!this.mIsHasDrawables && !this.mIsHasAcessDrawables && !this.mIsHasSystemDrawables) {
            return null;
        }
        String path = value.string.toString();
        if (TextUtils.isEmpty(path)) {
            return null;
        }
        Drawable drawable = null;
        if (this.mSkipFiles.get(id) == null) {
            OppoThemeZipFile.ThemeFileInfo themeFileInfo = null;
            int cookieId = getCookieType(value.assetCookie, id);
            if (this.mIsHasDrawables && this.mThemeResources != null) {
                themeFileInfo = this.mThemeResources.getThemeFileStream(cookieId, path);
            }
            if (this.mIsHasSystemDrawables && themeFileInfo == null && this.mSystemThemeResources != null && cookieId < 3) {
                themeFileInfo = this.mSystemThemeResources.getThemeFileStream(cookieId, path);
            }
            if (this.mIsHasAcessDrawables && themeFileInfo == null && this.mAccessibleResources != null) {
                themeFileInfo = this.mAccessibleResources.getAccessibleStream(cookieId, path);
            }
            if (themeFileInfo == null) {
                this.mSkipFiles.put(id, true);
                return null;
            }
            try {
                BitmapFactory.Options options = new BitmapFactory.Options();
                if (themeFileInfo.mDensity == 0) {
                    options.inDensity = DisplayMetrics.DENSITY_DEVICE_STABLE;
                } else {
                    options.inDensity = themeFileInfo.mDensity;
                }
                drawable = Drawable.createFromResourceStream(wrapper, value, themeFileInfo.mInput, path, options);
                try {
                    themeFileInfo.mInput.close();
                } catch (Exception e) {
                }
            } catch (OutOfMemoryError e2) {
                Log.e(TAG, "out of memory !!");
                if (themeFileInfo != null) {
                    themeFileInfo.mInput.close();
                }
            } catch (Throwable th) {
                if (themeFileInfo != null) {
                    try {
                        themeFileInfo.mInput.close();
                    } catch (Exception e3) {
                    }
                }
                throw th;
            }
        }
        if (drawable != null) {
            this.mLoadArrary.put(id, true);
        }
        return drawable;
    }

    private int getCookieType(int cookie, int id) {
        if (id == 0) {
            return id;
        }
        int i = this.mCookies.get(id);
        if (i == 0) {
            try {
                String packageName = getResourcePackageName(id);
                if ("android".equals(packageName)) {
                    i = 1;
                } else if (OppoThemeResources.OPPO_PACKAGE.equals(packageName)) {
                    i = 2;
                } else {
                    i = 3;
                }
                this.mCookies.put(id, i);
            } catch (Resources.NotFoundException e) {
                Log.e(TAG, "getCookieType. e = " + e);
            }
        }
        return i;
    }

    public Integer getThemeInt(int id, int cookie) {
        return getThemeInt(id, 0, cookie);
    }

    public synchronized Integer getThemeInt(int id, int resourceId, int cookie) {
        if (!this.mIsHasValues && !this.mIsHasAcessValues && !this.mIsHasSystemValues) {
            return null;
        }
        Integer res = null;
        try {
            int index = this.mIntegers.indexOfKey(id);
            if (index >= 0) {
                res = this.mIntegers.valueAt(index);
            } else {
                if (!(!this.mIsHasValues || this.mThemeResources == null || (res = this.mThemeResources.getThemeInt(id)) != null || resourceId == 0 || resourceId == id)) {
                    res = this.mThemeResources.getThemeInt(resourceId);
                }
                if (this.mIsHasSystemValues && res == null && this.mSystemThemeResources != null) {
                    int cookieId = getCookieType(cookie, id);
                    if (cookieId < 3) {
                        res = this.mSystemThemeResources.getThemeInt(id, cookieId);
                    }
                    int cookieResId = getCookieType(cookie, resourceId);
                    if (res == null && resourceId != 0 && resourceId != id && cookieResId < 3) {
                        res = this.mSystemThemeResources.getThemeInt(resourceId, cookieResId);
                    }
                }
                if (this.mIsHasAcessValues && res == null && this.mAccessibleResources != null && (res = this.mAccessibleResources.getAccessibleInt(id)) == null && resourceId != 0 && resourceId != id) {
                    res = this.mAccessibleResources.getAccessibleInt(resourceId);
                }
                this.mIntegers.put(id, res);
            }
        } catch (Exception e) {
            Log.e(TAG, "getThemeInt. e = " + e);
        }
        return res;
    }

    public TypedArray replaceTypedArray(TypedArray typedarray) {
        Integer res;
        if (this.mIsHasValues || this.mIsHasAcessValues || this.mIsHasSystemValues) {
            int[] ai = typedarray.mData;
            int cookie = typedarray.mValue.assetCookie;
            for (int i = 0; i < ai.length; i += 7) {
                int type = ai[i + 0];
                int id = ai[i + 3];
                if (((type >= 16 && type <= 31) || type == 5) && (res = getThemeInt(id, cookie)) != null) {
                    ai[i + 1] = res.intValue();
                }
            }
        }
        return typedarray;
    }

    public void setIsThemeChanged(boolean changed) {
        this.mThemeChanged = changed;
    }

    public boolean getThemeChanged() {
        return this.mThemeChanged;
    }

    public synchronized Drawable loadIcon(Resources wrapper, int id, String str, boolean useWrap) {
        Drawable drawable = null;
        TypedValue value = new TypedValue();
        getValue(id, value, true);
        String path = value.string.toString();
        if (str != null) {
            path = path.replace(path.substring(path.lastIndexOf("/") + 1), str);
        }
        if (this.mSkipIcons.get(id) == null) {
            if (this.mIconThemeResources == null) {
                this.mIconThemeResources = OppoThemeResourcesPackage.getThemeResources(this, "icons");
            }
            OppoThemeZipFile.ThemeFileInfo themeFileInfo = this.mIconThemeResources.getIconFileStream(path, useWrap);
            if (themeFileInfo == null) {
                this.mSkipIcons.put(id, path);
                return null;
            }
            if (themeFileInfo != null) {
                try {
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    if (themeFileInfo.mDensity == 0) {
                        options.inDensity = DisplayMetrics.DENSITY_DEVICE_STABLE;
                    } else {
                        options.inDensity = themeFileInfo.mDensity;
                    }
                    drawable = Drawable.createFromResourceStream(wrapper, value, themeFileInfo.mInput, path, options);
                } catch (OutOfMemoryError e) {
                    if (themeFileInfo != null) {
                        try {
                            themeFileInfo.mInput.close();
                        } catch (IOException e2) {
                        }
                    }
                } catch (Throwable th) {
                    if (themeFileInfo != null) {
                        try {
                            themeFileInfo.mInput.close();
                        } catch (IOException e3) {
                        }
                    }
                    throw th;
                }
            }
            if (themeFileInfo != null) {
                themeFileInfo.mInput.close();
            }
        }
        return drawable;
    }

    public synchronized InputStream openThemeRawResource(int id, TypedValue outValue) throws Resources.NotFoundException {
        if (!this.mIsHasDrawables && !this.mIsHasAcessDrawables && !this.mIsHasSystemDrawables) {
            return null;
        }
        OppoThemeZipFile.ThemeFileInfo themeFileInfo = null;
        if (this.mSkipFiles.get(id) == null) {
            String path = outValue.string.toString();
            int cookieId = getCookieType(outValue.assetCookie, id);
            if (this.mIsHasDrawables && this.mThemeResources != null) {
                themeFileInfo = this.mThemeResources.getThemeFileStream(cookieId, path);
            }
            if (this.mIsHasSystemDrawables && themeFileInfo == null && this.mSystemThemeResources != null && cookieId < 3) {
                themeFileInfo = this.mSystemThemeResources.getThemeFileStream(cookieId, path);
            }
            if (this.mIsHasAcessDrawables && themeFileInfo == null && this.mAccessibleResources != null) {
                themeFileInfo = this.mAccessibleResources.getAccessibleStream(cookieId, path);
            }
            if (themeFileInfo != null) {
                return themeFileInfo.mInput;
            }
            this.mSkipFiles.put(id, true);
        }
        return null;
    }

    public CharSequence getThemeCharSequence(int id) {
        if (!this.mIsHasValues) {
            return null;
        }
        CharSequence res = null;
        try {
            int index = this.mCharSequences.indexOfKey(id);
            if (index >= 0) {
                res = this.mCharSequences.valueAt(index);
            } else if (this.mThemeResources != null) {
                res = this.mThemeResources.getThemeCharSequence(id);
            }
            if (res != null) {
                this.mCharSequences.put(id, res);
            }
        } catch (Exception e) {
            Log.e(TAG, "getThemeCharSequence exception: ", e);
        }
        return res;
    }

    public SparseArray<Boolean> getLoadArrary() {
        return this.mLoadArrary;
    }

    public boolean isHasDrawables() {
        return this.mIsHasDrawables || this.mIsHasAcessDrawables || this.mIsHasSystemDrawables;
    }

    public int updateExConfiguration(ColorBaseResourcesImpl impl, Configuration config) {
        Configuration oldConfig;
        ResourcesImpl resources = typeCasting(impl);
        if (resources == null || (oldConfig = resources.getConfiguration()) == null || config == null) {
            return 0;
        }
        return oldConfig.diff(config);
    }

    public void checkUpdate(int changes, boolean languageChaged) {
        Trace.traceBegin(8192, "ColorBaseResourcesImpl#checkUpdate");
        boolean needNew = OppoExtraConfiguration.needNewResources(changes);
        boolean needAccessNew = OppoExtraConfiguration.needAccessNewResources(changes);
        if (!(this.mThemeResources == null && this.mSystemThemeResources == null && this.mIconThemeResources == null) && needNew) {
            clear();
            OppoThemeHelper.reset();
            OppoAppIconInfo.reset();
            OppoThemeResources oppoThemeResources = this.mThemeResources;
            if (oppoThemeResources != null) {
                oppoThemeResources.setResource(this);
                this.mThemeResources.checkUpdate();
                this.mIsHasValues = this.mThemeResources.hasValues();
                this.mIsHasDrawables = this.mThemeResources.hasDrawables();
            }
            OppoThemeResourcesSystem oppoThemeResourcesSystem = this.mSystemThemeResources;
            if (oppoThemeResourcesSystem != null) {
                oppoThemeResourcesSystem.checkUpdate();
                this.mIsHasSystemValues = this.mSystemThemeResources.hasValues();
                this.mIsHasSystemDrawables = this.mSystemThemeResources.hasDrawables();
            }
            OppoThemeResourcesPackage oppoThemeResourcesPackage = this.mIconThemeResources;
            if (oppoThemeResourcesPackage != null) {
                oppoThemeResourcesPackage.checkUpdate();
            }
        }
        if (this.mAccessibleResources != null && needAccessNew) {
            clear();
            this.mAccessibleResources.checkUpdate();
            this.mIsHasAcessValues = this.mAccessibleResources.hasValues();
            this.mIsHasAcessDrawables = this.mAccessibleResources.hasDrawables();
        }
        Trace.traceEnd(8192);
    }

    public void getExValue(int id, TypedValue outValue, boolean resolveRefs) {
        Integer res;
        if (((outValue.type >= 16 && outValue.type <= 31) || outValue.type == 5) && (res = getThemeInt(id, outValue.resourceId, outValue.assetCookie)) != null) {
            outValue.data = res.intValue();
        }
    }

    public void initThemeResource(String name) {
        Trace.traceBegin(8192, "ColorBaseResourcesImpl#initThemeResource");
        if (TextUtils.isEmpty(name) || "android".equals(name) || OppoThemeResources.OPPO_PACKAGE.equals(name)) {
            this.mSystemThemeResources = OppoThemeResources.getSystem(this);
        } else {
            int tempMask = StrictMode.allowThreadDiskWritesMask();
            try {
                this.mThemeResources = OppoThemeResourcesPackage.getThemeResources(this, name);
                this.mAccessibleResources = OppoAccessibleResources.getAccessResources(this, name);
            } finally {
                StrictMode.setThreadPolicyMask(tempMask);
            }
        }
        Trace.traceEnd(8192);
    }

    public ResourcesImpl typeCasting(ColorBaseResourcesImpl impl) {
        return (ResourcesImpl) typeCasting(ResourcesImpl.class, impl);
    }

    /* JADX DEBUG: Multi-variable search result rejected for r3v0, resolved type: java.lang.Object */
    /* JADX WARN: Multi-variable type inference failed */
    private <T> T typeCasting(Class<T> type, Object object) {
        if (object == 0 || !type.isInstance(object)) {
            return null;
        }
        return object;
    }

    private void clear() {
        clearCache();
        this.mIntegers.clear();
        this.mCharSequences.clear();
        this.mSkipFiles.clear();
        this.mSkipIcons.clear();
        this.mLoadArrary.clear();
        this.mCookies.clear();
    }
}
