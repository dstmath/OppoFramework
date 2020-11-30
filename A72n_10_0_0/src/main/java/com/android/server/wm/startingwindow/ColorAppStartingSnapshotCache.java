package com.android.server.wm.startingwindow;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.LruCache;
import com.android.server.wm.WindowManagerService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ColorAppStartingSnapshotCache {
    private static final Object LOCK = new Object();
    private static final int MAX_SIZE = 52428800;
    private OnSnapshotLoadFinishListener mListener;
    private final ColorAppStartingSnapshotLoader mLoader;
    private ArrayMap<String, BitmapDrawable> mPreloadedSplashMap = new ArrayMap<>();
    private ExecutorService mThreadPool;
    private final LruCache<String, Bitmap> mTokensSnapshotCache = new LruCache<String, Bitmap>((int) Math.min(Runtime.getRuntime().maxMemory() / 8, 52428800L)) {
        /* class com.android.server.wm.startingwindow.ColorAppStartingSnapshotCache.AnonymousClass1 */

        /* access modifiers changed from: protected */
        public int sizeOf(String key, Bitmap value) {
            return value.getAllocationByteCount();
        }
    };
    private WindowManagerService mWMService;

    public interface OnSnapshotLoadFinishListener {
        void onSnapshotLoaded(String str);
    }

    public ColorAppStartingSnapshotCache(WindowManagerService service, ColorAppStartingSnapshotLoader loader) {
        this.mLoader = loader;
        this.mThreadPool = Executors.newSingleThreadExecutor();
        this.mWMService = service;
    }

    public void registerSnapshotLoadListener(OnSnapshotLoadFinishListener listener) {
        this.mListener = listener;
    }

    public void unregisterSnapshotLoadListener() {
        this.mListener = null;
    }

    public void putSnapshot(int userId, String key, Bitmap snapshot) {
        this.mTokensSnapshotCache.put(rebuildKeyByUser(userId, key), snapshot);
    }

    public void cachePreloadedSplash(String packageName, BitmapDrawable drawable) {
        synchronized (LOCK) {
            this.mPreloadedSplashMap.put(packageName, drawable);
        }
    }

    public void clearCachedPreloadedSplash() {
        ColorStartingWindowUtils.logD("clearCachedPreloadedSplash");
        this.mListener = null;
        synchronized (LOCK) {
            this.mPreloadedSplashMap.clear();
        }
    }

    public void preloadSplash(final int userId, final Context context, final String packageName, String token) {
        ColorStartingWindowUtils.logD("preloadSplash packageName =: " + packageName + ",userId =: " + userId);
        clearCachedPreloadedSplash();
        Bitmap snapshot = this.mTokensSnapshotCache.get(rebuildKeyByUser(userId, token));
        if (snapshot != null) {
            cachePreloadedSplash(packageName, new BitmapDrawable(snapshot));
            return;
        }
        final int drawableRes = ColorStartingWindowContants.SPECIAL_APP_DRAWABLE_RES_KEY_MAP.getOrDefault(token, -1).intValue();
        if (drawableRes != -1) {
            this.mThreadPool.execute(new Runnable() {
                /* class com.android.server.wm.startingwindow.ColorAppStartingSnapshotCache.AnonymousClass2 */

                public void run() {
                    ColorAppStartingSnapshotCache.this.loadSplashFromRes(packageName, context, drawableRes);
                }
            });
        } else {
            this.mThreadPool.execute(new Runnable() {
                /* class com.android.server.wm.startingwindow.ColorAppStartingSnapshotCache.AnonymousClass3 */

                public void run() {
                    ColorAppStartingSnapshotCache.this.loadSplashFromDisk(userId, packageName);
                }
            });
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void loadSplashFromRes(String packageName, Context context, int drawableRes) {
        int srceenWidth;
        if (!ColorStartingWindowContants.WECHAT_PACKAGE_NAME.equals(packageName)) {
            Drawable drawable = context.getDrawable(drawableRes);
            if (drawable != null) {
                cachePreloadedSplash(packageName, (BitmapDrawable) drawable);
                ColorStartingWindowUtils.logD("loadSplashFromRes packageName =: " + packageName);
                OnSnapshotLoadFinishListener onSnapshotLoadFinishListener = this.mListener;
                if (onSnapshotLoadFinishListener != null) {
                    onSnapshotLoadFinishListener.onSnapshotLoaded(packageName);
                    return;
                }
                return;
            }
            return;
        }
        long start = System.currentTimeMillis();
        Point realSize = new Point();
        this.mWMService.getInitialDisplaySize(0, realSize);
        int srceenWidth2 = Math.min(realSize.x, realSize.y);
        int screenHeight = Math.max(realSize.x, realSize.y);
        ColorStartingWindowUtils.logD("loadSplashFromRes init screen size spend time = :" + (System.currentTimeMillis() - start));
        long start2 = System.currentTimeMillis();
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inMutable = true;
        Bitmap drawablebitmap = BitmapFactory.decodeResource(context.getResources(), drawableRes, options);
        ColorStartingWindowUtils.logD("loadSplashFromRes load bitmap spend time = :" + (System.currentTimeMillis() - start2));
        long start3 = System.currentTimeMillis();
        if (drawablebitmap != null) {
            Bitmap bitmap = Bitmap.createBitmap(srceenWidth2, screenHeight, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            Paint paint = new Paint(4);
            if (drawablebitmap.getWidth() < srceenWidth2 || drawablebitmap.getHeight() < screenHeight) {
                int topColor = drawablebitmap.getColor((int) (((float) drawablebitmap.getWidth()) * 0.5f), 1).toArgb();
                int bottomColor = drawablebitmap.getColor((int) (((float) drawablebitmap.getWidth()) * 0.5f), drawablebitmap.getHeight() - 1).toArgb();
                paint.setShader(new LinearGradient(0.0f, 0.0f, 0.0f, (float) screenHeight, new int[]{topColor, bottomColor, bottomColor}, new float[]{0.0f, 0.5f, 1.0f}, Shader.TileMode.CLAMP));
                canvas.drawRect(new Rect(0, 0, srceenWidth2, screenHeight), paint);
            }
            int disX = Math.abs((int) (((float) (drawablebitmap.getWidth() - srceenWidth2)) * 0.5f));
            int disY = Math.abs((int) (((float) (drawablebitmap.getHeight() - screenHeight)) * 0.5f));
            Rect sourceRect = new Rect(0, 0, drawablebitmap.getWidth(), drawablebitmap.getHeight());
            Rect dstRect = new Rect(0, 0, srceenWidth2, screenHeight);
            if (drawablebitmap.getWidth() > srceenWidth2) {
                srceenWidth = srceenWidth2;
                sourceRect.set(disX, sourceRect.top, srceenWidth2 + disX, sourceRect.bottom);
            } else {
                srceenWidth = srceenWidth2;
                dstRect.set(disX, dstRect.top, drawablebitmap.getWidth() + disX, dstRect.bottom);
            }
            if (drawablebitmap.getHeight() > screenHeight) {
                sourceRect.set(sourceRect.left, disY, sourceRect.right, screenHeight + disY);
            } else {
                dstRect.set(dstRect.left, disY, dstRect.right, drawablebitmap.getHeight() + disY);
            }
            canvas.drawBitmap(drawablebitmap, sourceRect, dstRect, paint);
            ColorStartingWindowUtils.logD("loadSplashFromRes fix bitmap spend time =:" + (System.currentTimeMillis() - start3));
            System.currentTimeMillis();
            cachePreloadedSplash(packageName, new BitmapDrawable(bitmap));
            ColorStartingWindowUtils.logD("loadSplashFromRes finish window.size =: (" + srceenWidth + "," + screenHeight + "),drawablebitmap.size =: (" + drawablebitmap.getWidth() + "," + drawablebitmap.getHeight() + "),sourceRect =:" + sourceRect + ",dstRect =:" + dstRect);
            OnSnapshotLoadFinishListener onSnapshotLoadFinishListener2 = this.mListener;
            if (onSnapshotLoadFinishListener2 != null) {
                onSnapshotLoadFinishListener2.onSnapshotLoaded(packageName);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void loadSplashFromDisk(int userId, String packageName) {
        Bitmap bitmap = tryRestoreFromDisk(userId, packageName);
        if (bitmap != null) {
            cachePreloadedSplash(packageName, new BitmapDrawable(bitmap));
            ColorStartingWindowUtils.logD("loadSplashFromDisk finish *********************************** packageName =:" + packageName);
            OnSnapshotLoadFinishListener onSnapshotLoadFinishListener = this.mListener;
            if (onSnapshotLoadFinishListener != null) {
                onSnapshotLoadFinishListener.onSnapshotLoaded(packageName);
            }
        }
    }

    public BitmapDrawable getPreloadedSplash(String packageName) {
        BitmapDrawable bitmapDrawable;
        synchronized (LOCK) {
            bitmapDrawable = this.mPreloadedSplashMap.get(packageName);
        }
        return bitmapDrawable;
    }

    public BitmapDrawable getPreloadedOrCachedSplash(int userId, String packageName, String token) {
        Bitmap snapshot;
        BitmapDrawable bitmapDrawable = getPreloadedSplash(packageName);
        if (bitmapDrawable != null || (snapshot = this.mTokensSnapshotCache.get(rebuildKeyByUser(userId, token))) == null) {
            return bitmapDrawable;
        }
        ColorStartingWindowUtils.logD("getPreloadedOrCachedSplash loaded splash in cache token =:" + token);
        return new BitmapDrawable(snapshot);
    }

    public BitmapDrawable getSnapshotForToken(int userId, String packageName, String token, boolean restoreFromDisk) {
        ColorStartingWindowUtils.logD("getSnapshotForToken userId =:" + userId + ",packageName =: " + packageName + "\ntoken =: " + token + ",restoreFromDisk =: " + restoreFromDisk);
        if (TextUtils.isEmpty(token)) {
            return null;
        }
        Bitmap bitmap = this.mTokensSnapshotCache.get(rebuildKeyByUser(userId, token));
        if (bitmap != null) {
            return new BitmapDrawable(bitmap);
        }
        if (restoreFromDisk && !TextUtils.isEmpty(packageName)) {
            return getSnapshotFromDisk(userId, packageName);
        }
        return null;
    }

    public BitmapDrawable getSnapshotFromDisk(int userId, String packageName) {
        ColorStartingWindowUtils.logD("getSnapshotFromDisk userId =:" + userId + ",packageName =: " + packageName);
        Bitmap bitmap = tryRestoreFromDisk(userId, packageName);
        if (bitmap != null) {
            return new BitmapDrawable(bitmap);
        }
        return null;
    }

    private Bitmap tryRestoreFromDisk(int userId, String packageName) {
        Bitmap snapshot = this.mLoader.loadAppSnapshot(userId, packageName);
        if (snapshot == null) {
            return null;
        }
        return snapshot;
    }

    private String rebuildKeyByUser(int userId, String appToken) {
        return userId + "-" + appToken;
    }

    public void clearCache() {
        LruCache<String, Bitmap> lruCache = this.mTokensSnapshotCache;
        if (lruCache != null) {
            lruCache.evictAll();
        }
    }
}
