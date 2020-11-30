package com.android.server.pm;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.IPackageDeleteObserver;
import android.content.pm.LauncherActivityInfo;
import android.content.pm.LauncherApps;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Binder;
import android.os.FileObserver;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Slog;
import com.android.internal.util.Preconditions;
import com.android.server.ColorDeviceIdleHelper;
import com.android.server.am.ColorHansManager;
import com.android.server.display.ai.utils.ColorAILog;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ColorIconCachesManager implements IColorIconCachesManager {
    private static final String OPPO_LOWRAM_FEATURE = "oppo.rom.lowram.support";
    public static final String TAG = "ColorIconCachesManager";
    private static ConcurrentHashMap<String, Bitmap> mActivityIconsCache = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<String, Bitmap> mAppIconsCache = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<String, Bitmap> mAppIconsCacheCompress = new ConcurrentHashMap<>();
    private static ColorIconCachesManager sColorIconCachesManager = null;
    public static boolean sDebugfDetail = SystemProperties.getBoolean(ColorAILog.OPPO_LOG_KEY, false);
    boolean DEBUG_SWITCH = (sDebugfDetail | this.mDynamicDebug);
    private boolean mCacheSwitch = false;
    private IColorPackageManagerServiceEx mColorPmsEx = null;
    private Context mContext = null;
    boolean mDynamicDebug = false;
    private HashMap<Integer, IPackageDeleteObserver> mPackageDeleteList = new HashMap<>();
    private PackageManagerService mPms = null;
    private BroadcastReceiver mSkinChangedReceiver = new BroadcastReceiver() {
        /* class com.android.server.pm.ColorIconCachesManager.AnonymousClass3 */

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(ColorIconCachesManager.TAG, "mBroadcastReceiver onReceive action = " + action);
            if ("oppo.intent.action.SKIN_CHANGED".equals(action) || "oppo.intent.config.density.change".equals(action)) {
                Log.d(ColorIconCachesManager.TAG, "recache icons");
                ColorIconCachesManager.this.mThemeIconsChanged = false;
                ColorIconCachesManager.mAppIconsCache.clear();
                ColorIconCachesManager.mAppIconsCacheCompress.clear();
                ColorIconCachesManager.mActivityIconsCache.clear();
                ColorIconCachesManager.this.mPms.mHandler.sendMessage(ColorIconCachesManager.this.mPms.mHandler.obtainMessage(1003));
            }
        }
    };
    private boolean mThemeIconsChanged = false;
    private FileObserver mThemeIconsFileObserver = new FileObserver("/data/theme/icons", 1546) {
        /* class com.android.server.pm.ColorIconCachesManager.AnonymousClass2 */

        public void onEvent(int event, String path) {
            Log.d(ColorIconCachesManager.TAG, "onEvent. event = " + event + " , path = " + path);
            if (8 == event || 512 == event || 1024 == event || 2 == event) {
                ColorIconCachesManager.this.mThemeIconsChanged = true;
            }
        }
    };

    public static ColorIconCachesManager getInstance() {
        if (sColorIconCachesManager == null) {
            sColorIconCachesManager = new ColorIconCachesManager();
        }
        return sColorIconCachesManager;
    }

    private ColorIconCachesManager() {
    }

    public void init(IColorPackageManagerServiceEx pmsEx) {
        if (pmsEx != null) {
            this.mColorPmsEx = pmsEx;
            this.mPms = pmsEx.getPackageManagerService();
        }
        this.mContext = this.mPms.mContext;
    }

    public Map<String, Bitmap> getActivityIconsCache(IPackageDeleteObserver observer) {
        Preconditions.checkNotNull(observer);
        Integer uid = Integer.valueOf(Binder.getCallingUid());
        if (this.mPackageDeleteList.get(uid) != null) {
            this.mPackageDeleteList.remove(uid);
        }
        this.mPackageDeleteList.put(uid, observer);
        return mActivityIconsCache;
    }

    public Bitmap getAppIconBitmap(String packageName) {
        Slog.d(TAG, "OPKMS getAppIconBitmap[" + packageName + "].");
        return mAppIconsCache.get(packageName);
    }

    public Map<String, Bitmap> getAppIconsCache(boolean compress) {
        Slog.d(TAG, "OPKMS getAppIconsCache[" + compress + "].");
        if (compress) {
            return mAppIconsCacheCompress;
        }
        return mAppIconsCache;
    }

    public void onPackageAdded(String packageName) {
        int maxCacheNum;
        if (!this.mCacheSwitch && !TextUtils.isEmpty(packageName) && !this.mThemeIconsChanged) {
            try {
                Drawable cacheIcon = this.mPms.getApplicationInfo(packageName, 0, this.mContext.getUserId()).loadIcon(this.mContext.getPackageManager());
                int h = (((int) this.mContext.getResources().getDimension(17104896)) * 3) / 4;
                int width = cacheIcon.getIntrinsicWidth();
                int height = cacheIcon.getIntrinsicHeight();
                int maxCacheNum2 = ColorPackageManagerHelper.getIconCacheMaxNum();
                Bitmap bitmap = drawableToBitmap(cacheIcon);
                if (bitmap != null) {
                    if (mAppIconsCache.size() < maxCacheNum2) {
                        mAppIconsCache.put(packageName, bitmap);
                    }
                    Matrix matrix = new Matrix();
                    matrix.postScale(((float) h) / ((float) width), ((float) h) / ((float) height));
                    Bitmap newbmp = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
                    maxCacheNum = maxCacheNum2;
                    if (mAppIconsCacheCompress.size() < maxCacheNum) {
                        mAppIconsCacheCompress.put(packageName, newbmp);
                    }
                    Log.i(TAG, "add appIconsCache:" + packageName);
                } else {
                    maxCacheNum = maxCacheNum2;
                }
                if (mActivityIconsCache.size() < maxCacheNum) {
                    cacheActivityIconsData(packageName);
                }
            } catch (Exception e) {
                Log.e(TAG, "add appIconsCache failed!" + packageName + e.getMessage());
            }
        }
    }

    public void onPackageRemoved(String packageName) {
        if (!TextUtils.isEmpty(packageName)) {
            try {
                if (mAppIconsCache.get(packageName) != null) {
                    mAppIconsCache.remove(packageName);
                }
                if (mAppIconsCacheCompress.get(packageName) != null) {
                    mAppIconsCacheCompress.remove(packageName);
                }
                ArrayList<String> deleteList = new ArrayList<>();
                for (Map.Entry<String, Bitmap> entry : mActivityIconsCache.entrySet()) {
                    String key = entry.getKey();
                    if (packageName.equals(key.split("/")[0])) {
                        deleteList.add(key);
                    }
                }
                Iterator<String> it = deleteList.iterator();
                while (it.hasNext()) {
                    mActivityIconsCache.remove(it.next());
                }
                Log.d(TAG, "remove appIconsCache:" + packageName);
                for (Map.Entry<Integer, IPackageDeleteObserver> entryObserver : this.mPackageDeleteList.entrySet()) {
                    final IPackageDeleteObserver observer = entryObserver.getValue();
                    try {
                        new IPackageDeleteObserver.Stub() {
                            /* class com.android.server.pm.ColorIconCachesManager.AnonymousClass1 */

                            public void packageDeleted(String packageName, int returnCode) {
                                IPackageDeleteObserver iPackageDeleteObserver = observer;
                                if (iPackageDeleteObserver != null) {
                                    try {
                                        iPackageDeleteObserver.packageDeleted(packageName, returnCode);
                                    } catch (RemoteException e) {
                                    }
                                }
                            }
                        }.packageDeleted(packageName, 1);
                    } catch (RemoteException re) {
                        re.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void cacheAppIconsData() {
        List<PackageInfo> apps;
        Bitmap defaultBitmap;
        BitmapDrawable defaultIcon;
        PackageManager pm;
        Exception e;
        Matrix matrix;
        if (!this.mCacheSwitch) {
            if (this.mThemeIconsChanged) {
                Log.d(TAG, "cacheAppIconsData. The theme icons file has changed! Not to cache.");
                return;
            }
            Log.d(TAG, "cacheAppIconsData. start");
            try {
                PackageManager pm2 = this.mContext.getPackageManager();
                BitmapDrawable defaultIcon2 = (BitmapDrawable) pm2.getDefaultActivityIcon();
                Bitmap defaultBitmap2 = defaultIcon2.getBitmap();
                List<PackageInfo> apps2 = this.mPms.getInstalledPackages(0, this.mContext.getUserId()).getList();
                int h = (((int) this.mContext.getResources().getDimension(17104896)) * 3) / 4;
                int maxCacheNum = ColorPackageManagerHelper.getIconCacheMaxNum();
                if (maxCacheNum != 0) {
                    this.mThemeIconsFileObserver.startWatching();
                    Iterator<PackageInfo> it = apps2.iterator();
                    int num = 0;
                    while (true) {
                        if (!it.hasNext()) {
                            break;
                        }
                        PackageInfo app = it.next();
                        if (this.mThemeIconsChanged) {
                            Log.d(TAG, "cacheAppIconsData. The theme icons file has changed!");
                            mAppIconsCache.clear();
                            mAppIconsCacheCompress.clear();
                            break;
                        }
                        try {
                            Drawable cacheIcon = app.applicationInfo.loadIcon(pm2);
                            int width = cacheIcon.getIntrinsicWidth();
                            int height = cacheIcon.getIntrinsicHeight();
                            Bitmap bitmap = drawableToBitmap(cacheIcon);
                            if (bitmap == null || bitmap.sameAs(defaultBitmap2)) {
                                pm = pm2;
                                defaultIcon = defaultIcon2;
                                defaultBitmap = defaultBitmap2;
                                apps = apps2;
                                if (cacheIcon instanceof BitmapDrawable) {
                                    Slog.i(TAG, "cacheAppIconsData is default icon:" + app.packageName);
                                }
                                pm2 = pm;
                                defaultIcon2 = defaultIcon;
                                defaultBitmap2 = defaultBitmap;
                                apps2 = apps;
                            } else {
                                pm = pm2;
                                try {
                                    defaultIcon = defaultIcon2;
                                    try {
                                        mAppIconsCache.put(app.packageName, bitmap);
                                        matrix = new Matrix();
                                        defaultBitmap = defaultBitmap2;
                                        apps = apps2;
                                    } catch (Exception e2) {
                                        e = e2;
                                        defaultBitmap = defaultBitmap2;
                                        apps = apps2;
                                        Slog.e(TAG, "init appIconsCache!" + e.getMessage());
                                        pm2 = pm;
                                        defaultIcon2 = defaultIcon;
                                        defaultBitmap2 = defaultBitmap;
                                        apps2 = apps;
                                    }
                                    try {
                                        matrix.postScale(((float) h) / ((float) width), ((float) h) / ((float) height));
                                        mAppIconsCacheCompress.put(app.packageName, Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true));
                                        int num2 = num + 1;
                                        if (num >= maxCacheNum) {
                                            num = num2;
                                            break;
                                        }
                                        num = num2;
                                        pm2 = pm;
                                        defaultIcon2 = defaultIcon;
                                        defaultBitmap2 = defaultBitmap;
                                        apps2 = apps;
                                    } catch (Exception e3) {
                                        e = e3;
                                        Slog.e(TAG, "init appIconsCache!" + e.getMessage());
                                        pm2 = pm;
                                        defaultIcon2 = defaultIcon;
                                        defaultBitmap2 = defaultBitmap;
                                        apps2 = apps;
                                    }
                                } catch (Exception e4) {
                                    e = e4;
                                    defaultIcon = defaultIcon2;
                                    defaultBitmap = defaultBitmap2;
                                    apps = apps2;
                                    Slog.e(TAG, "init appIconsCache!" + e.getMessage());
                                    pm2 = pm;
                                    defaultIcon2 = defaultIcon;
                                    defaultBitmap2 = defaultBitmap;
                                    apps2 = apps;
                                }
                            }
                        } catch (Exception e5) {
                            e = e5;
                            pm = pm2;
                            defaultIcon = defaultIcon2;
                            defaultBitmap = defaultBitmap2;
                            apps = apps2;
                            Slog.e(TAG, "init appIconsCache!" + e.getMessage());
                            pm2 = pm;
                            defaultIcon2 = defaultIcon;
                            defaultBitmap2 = defaultBitmap;
                            apps2 = apps;
                        }
                    }
                    Slog.d(TAG, "cached appIconsCache num:" + num);
                    this.mThemeIconsFileObserver.stopWatching();
                    Log.d(TAG, "cacheAppIconsData. end");
                }
            } catch (Exception e6) {
                Slog.e(TAG, "init appIconsCache failed!" + e6.getMessage());
                e6.printStackTrace();
            }
        }
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap;
        if (!(drawable instanceof BitmapDrawable)) {
            return null;
        }
        BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
        if (bitmapDrawable.getBitmap() != null) {
            return bitmapDrawable.getBitmap();
        }
        if (drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    @TargetApi(ColorHansManager.HansMainHandler.MSG_CHECK_JOB_WAKELOCK)
    public void cacheActivityIconsData(String packageName) {
        LauncherApps launcherApps;
        Exception ex;
        if (!this.mCacheSwitch) {
            if (this.mThemeIconsChanged) {
                Log.d(TAG, "cacheActivityIconsData. The theme icons file has changed! Not to cache.");
                return;
            }
            Log.d(TAG, "cacheActivityIconsData. start");
            try {
                LauncherApps launcherApps2 = (LauncherApps) this.mContext.getSystemService("launcherapps");
                List<LauncherActivityInfo> installApps = launcherApps2.getActivityList(packageName, UserHandle.OWNER);
                int densityDpi = this.mContext.getResources().getDisplayMetrics().densityDpi;
                int maxCacheNum = ColorPackageManagerHelper.getIconCacheMaxNum();
                if (maxCacheNum != 0) {
                    this.mThemeIconsFileObserver.startWatching();
                    Bitmap defaultBitmap = ((BitmapDrawable) this.mContext.getPackageManager().getDefaultActivityIcon()).getBitmap();
                    Iterator<LauncherActivityInfo> it = installApps.iterator();
                    int count = 0;
                    while (true) {
                        if (!it.hasNext()) {
                            break;
                        }
                        LauncherActivityInfo launcherActivityInfo = it.next();
                        if (this.mThemeIconsChanged) {
                            Log.d(TAG, "cacheActivityIconsData. The theme icons file has changed!");
                            mActivityIconsCache.clear();
                            break;
                        }
                        try {
                            ComponentName componentName = launcherActivityInfo.getComponentName();
                            String keyName = componentName.getPackageName() + "/" + componentName.getClassName();
                            Drawable cacheIcon = launcherActivityInfo.getBadgedIcon(densityDpi);
                            Bitmap bitmap = drawableToBitmap(cacheIcon);
                            launcherApps = launcherApps2;
                            if (bitmap != null) {
                                try {
                                    if (!bitmap.sameAs(defaultBitmap)) {
                                        mActivityIconsCache.put(keyName, bitmap);
                                        count++;
                                        if (count >= maxCacheNum) {
                                            break;
                                        }
                                        launcherApps2 = launcherApps;
                                    }
                                } catch (Exception e) {
                                    ex = e;
                                    Slog.e(TAG, "init activityIconsCache!" + ex.getMessage());
                                    launcherApps2 = launcherApps;
                                }
                            }
                            if (cacheIcon instanceof BitmapDrawable) {
                                Slog.i(TAG, "cacheActivityIconsData is default icon:" + keyName);
                            }
                        } catch (Exception e2) {
                            ex = e2;
                            launcherApps = launcherApps2;
                            Slog.e(TAG, "init activityIconsCache!" + ex.getMessage());
                            launcherApps2 = launcherApps;
                        }
                        launcherApps2 = launcherApps;
                    }
                    this.mThemeIconsFileObserver.stopWatching();
                    Log.d(TAG, "cacheActivityIconsData nums:" + count);
                    Log.d(TAG, "cacheActivityIconsData. end");
                }
            } catch (Exception e3) {
                Slog.e(TAG, "init activityIconsCache failed!" + e3.getMessage());
            }
        }
    }

    public void systemReady() {
        boolean z = false;
        if (this.mPms.hasSystemFeature(OPPO_LOWRAM_FEATURE, 0) || ColorPackageManagerHelper.getIconCacheMaxNum() == 0) {
            z = true;
        }
        this.mCacheSwitch = z;
        Slog.i(TAG, "mCacheSwitch:" + this.mCacheSwitch);
        this.mPms.mHandler.postDelayed(new Runnable() {
            /* class com.android.server.pm.ColorIconCachesManager.AnonymousClass4 */

            public void run() {
                ColorIconCachesManager.this.cacheActivityIconsData(null);
                ColorIconCachesManager.this.cacheAppIconsData();
            }
        }, (long) ColorDeviceIdleHelper.DEFAULT_TOTAL_INTERVAL_TO_IDLE);
        if (!this.mCacheSwitch) {
            IntentFilter intentFilterSkinChanged = new IntentFilter();
            intentFilterSkinChanged.addAction("oppo.intent.action.SKIN_CHANGED");
            intentFilterSkinChanged.addAction("oppo.intent.config.density.change");
            this.mContext.registerReceiver(this.mSkinChangedReceiver, intentFilterSkinChanged);
        }
    }

    public void clearIconCache() {
        mAppIconsCache.clear();
        mAppIconsCacheCompress.clear();
        mActivityIconsCache.clear();
    }
}
