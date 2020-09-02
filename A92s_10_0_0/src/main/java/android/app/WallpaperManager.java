package android.app;

import android.annotation.OppoHook;
import android.annotation.SystemApi;
import android.annotation.UnsupportedAppUsage;
import android.app.IWallpaperManagerCallback;
import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.DeadSystemException;
import android.os.FileUtils;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.view.WindowManagerGlobal;
import com.android.internal.R;
import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import libcore.io.IoUtils;

public class WallpaperManager {
    public static final String ACTION_CHANGE_LIVE_WALLPAPER = "android.service.wallpaper.CHANGE_LIVE_WALLPAPER";
    public static final String ACTION_CROP_AND_SET_WALLPAPER = "android.service.wallpaper.CROP_AND_SET_WALLPAPER";
    public static final String ACTION_LIVE_WALLPAPER_CHOOSER = "android.service.wallpaper.LIVE_WALLPAPER_CHOOSER";
    public static final String COMMAND_DROP = "android.home.drop";
    public static final String COMMAND_SECONDARY_TAP = "android.wallpaper.secondaryTap";
    public static final String COMMAND_TAP = "android.wallpaper.tap";
    private static boolean DEBUG = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    public static final String EXTRA_LIVE_WALLPAPER_COMPONENT = "android.service.wallpaper.extra.LIVE_WALLPAPER_COMPONENT";
    public static final String EXTRA_NEW_WALLPAPER_ID = "android.service.wallpaper.extra.ID";
    public static final int FLAG_FAST_SET = 65536;
    public static final int FLAG_LOCK = 2;
    public static final int FLAG_SYSTEM = 1;
    private static final String PROP_LOCK_WALLPAPER = "ro.config.lock_wallpaper";
    private static final String PROP_WALLPAPER = "ro.config.wallpaper";
    private static final String PROP_WALLPAPER_COMPONENT = "ro.config.wallpaper_component";
    /* access modifiers changed from: private */
    public static String TAG = "WallpaperManager";
    public static final String WALLPAPER_PREVIEW_META_DATA = "android.wallpaper.preview";
    /* access modifiers changed from: private */
    @UnsupportedAppUsage
    public static Globals sGlobals;
    private static final Object sSync = new Object[0];
    private final Context mContext;
    private float mWallpaperXStep = -1.0f;
    private float mWallpaperYStep = -1.0f;

    @Retention(RetentionPolicy.SOURCE)
    public @interface SetWallpaperFlags {
    }

    static class FastBitmapDrawable extends Drawable {
        private final Bitmap mBitmap;
        private int mDrawLeft;
        private int mDrawTop;
        private final int mHeight;
        private final Paint mPaint;
        private final int mWidth;

        private FastBitmapDrawable(Bitmap bitmap) {
            this.mBitmap = bitmap;
            this.mWidth = bitmap.getWidth();
            this.mHeight = bitmap.getHeight();
            setBounds(0, 0, this.mWidth, this.mHeight);
            this.mPaint = new Paint();
            this.mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
        }

        @Override // android.graphics.drawable.Drawable
        public void draw(Canvas canvas) {
            canvas.drawBitmap(this.mBitmap, (float) this.mDrawLeft, (float) this.mDrawTop, this.mPaint);
        }

        @Override // android.graphics.drawable.Drawable
        public int getOpacity() {
            return -1;
        }

        @Override // android.graphics.drawable.Drawable
        public void setBounds(int left, int top, int right, int bottom) {
            this.mDrawLeft = (((right - left) - this.mWidth) / 2) + left;
            this.mDrawTop = (((bottom - top) - this.mHeight) / 2) + top;
        }

        @Override // android.graphics.drawable.Drawable
        public void setAlpha(int alpha) {
            throw new UnsupportedOperationException("Not supported with this drawable");
        }

        @Override // android.graphics.drawable.Drawable
        public void setColorFilter(ColorFilter colorFilter) {
            throw new UnsupportedOperationException("Not supported with this drawable");
        }

        @Override // android.graphics.drawable.Drawable
        public void setDither(boolean dither) {
            throw new UnsupportedOperationException("Not supported with this drawable");
        }

        @Override // android.graphics.drawable.Drawable
        public void setFilterBitmap(boolean filter) {
            throw new UnsupportedOperationException("Not supported with this drawable");
        }

        @Override // android.graphics.drawable.Drawable
        public int getIntrinsicWidth() {
            return this.mWidth;
        }

        @Override // android.graphics.drawable.Drawable
        public int getIntrinsicHeight() {
            return this.mHeight;
        }

        @Override // android.graphics.drawable.Drawable
        public int getMinimumWidth() {
            return this.mWidth;
        }

        @Override // android.graphics.drawable.Drawable
        public int getMinimumHeight() {
            return this.mHeight;
        }
    }

    /* access modifiers changed from: private */
    public static class Globals extends IWallpaperManagerCallback.Stub {
        private Bitmap mCachedWallpaper;
        private int mCachedWallpaperUserId;
        private boolean mColorCallbackRegistered;
        private final ArrayList<Pair<OnColorsChangedListener, Handler>> mColorListeners = new ArrayList<>();
        private Bitmap mDefaultWallpaper;
        private Handler mMainLooperHandler;
        /* access modifiers changed from: private */
        public final IWallpaperManager mService;

        Globals(IWallpaperManager service, Looper looper) {
            this.mService = service;
            this.mMainLooperHandler = new Handler(looper);
            forgetLoadedWallpaper();
        }

        @Override // android.app.IWallpaperManagerCallback
        public void onWallpaperChanged() {
            forgetLoadedWallpaper();
        }

        public void addOnColorsChangedListener(OnColorsChangedListener callback, Handler handler, int userId, int displayId) {
            synchronized (this) {
                if (!this.mColorCallbackRegistered) {
                    try {
                        this.mService.registerWallpaperColorsCallback(this, userId, displayId);
                        this.mColorCallbackRegistered = true;
                    } catch (RemoteException e) {
                        Log.w(WallpaperManager.TAG, "Can't register for color updates", e);
                    }
                }
                this.mColorListeners.add(new Pair<>(callback, handler));
            }
        }

        public void removeOnColorsChangedListener(OnColorsChangedListener callback, int userId, int displayId) {
            synchronized (this) {
                this.mColorListeners.removeIf(new Predicate() {
                    /* class android.app.$$Lambda$WallpaperManager$Globals$2yG7V1sbMECCnlFTLyjKWKqNoYI */

                    @Override // java.util.function.Predicate
                    public final boolean test(Object obj) {
                        return WallpaperManager.Globals.lambda$removeOnColorsChangedListener$0(WallpaperManager.OnColorsChangedListener.this, (Pair) obj);
                    }
                });
                if (this.mColorListeners.size() == 0 && this.mColorCallbackRegistered) {
                    this.mColorCallbackRegistered = false;
                    try {
                        this.mService.unregisterWallpaperColorsCallback(this, userId, displayId);
                    } catch (RemoteException e) {
                        Log.w(WallpaperManager.TAG, "Can't unregister color updates", e);
                    }
                }
            }
        }

        static /* synthetic */ boolean lambda$removeOnColorsChangedListener$0(OnColorsChangedListener callback, Pair pair) {
            return pair.first == callback;
        }

        @Override // android.app.IWallpaperManagerCallback
        public void onWallpaperColorsChanged(WallpaperColors colors, int which, int userId) {
            Handler handler;
            synchronized (this) {
                Iterator<Pair<OnColorsChangedListener, Handler>> it = this.mColorListeners.iterator();
                while (it.hasNext()) {
                    Pair<OnColorsChangedListener, Handler> listener = it.next();
                    Handler handler2 = listener.second;
                    if (listener.second == null) {
                        handler = this.mMainLooperHandler;
                    } else {
                        handler = handler2;
                    }
                    handler.post(new Runnable(listener, colors, which, userId) {
                        /* class android.app.$$Lambda$WallpaperManager$Globals$1AcnQUORvPlCjJoNqdxfQT4o4Nw */
                        private final /* synthetic */ Pair f$1;
                        private final /* synthetic */ WallpaperColors f$2;
                        private final /* synthetic */ int f$3;
                        private final /* synthetic */ int f$4;

                        {
                            this.f$1 = r2;
                            this.f$2 = r3;
                            this.f$3 = r4;
                            this.f$4 = r5;
                        }

                        public final void run() {
                            WallpaperManager.Globals.this.lambda$onWallpaperColorsChanged$1$WallpaperManager$Globals(this.f$1, this.f$2, this.f$3, this.f$4);
                        }
                    });
                }
            }
        }

        public /* synthetic */ void lambda$onWallpaperColorsChanged$1$WallpaperManager$Globals(Pair listener, WallpaperColors colors, int which, int userId) {
            boolean stillExists;
            synchronized (WallpaperManager.sGlobals) {
                stillExists = this.mColorListeners.contains(listener);
            }
            if (stillExists) {
                listener.first.onColorsChanged(colors, which, userId);
            }
        }

        /* access modifiers changed from: package-private */
        public WallpaperColors getWallpaperColors(int which, int userId, int displayId) {
            if (which == 2 || which == 1) {
                try {
                    return this.mService.getWallpaperColors(which, userId, displayId);
                } catch (RemoteException e) {
                    return null;
                }
            } else {
                throw new IllegalArgumentException("Must request colors for exactly one kind of wallpaper");
            }
        }

        public Bitmap peekWallpaperBitmap(Context context, boolean returnDefault, int which) {
            return peekWallpaperBitmap(context, returnDefault, which, context.getUserId(), false);
        }

        /* JADX WARNING: Code restructure failed: missing block: B:36:0x0074, code lost:
            if (r7 == false) goto L_0x0088;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:37:0x0076, code lost:
            r0 = r5.mDefaultWallpaper;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:38:0x0078, code lost:
            if (r0 != null) goto L_?;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:39:0x007a, code lost:
            r1 = getDefaultWallpaper(r6, r8);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:40:0x007e, code lost:
            monitor-enter(r5);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:42:?, code lost:
            r5.mDefaultWallpaper = r1;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:43:0x0081, code lost:
            monitor-exit(r5);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:48:0x0088, code lost:
            return null;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:52:?, code lost:
            return r0;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:53:?, code lost:
            return r1;
         */
        public Bitmap peekWallpaperBitmap(Context context, boolean returnDefault, int which, int userId, boolean hardware) {
            IWallpaperManager iWallpaperManager = this.mService;
            if (iWallpaperManager != null) {
                try {
                    if (!iWallpaperManager.isWallpaperSupported(context.getOpPackageName())) {
                        return null;
                    }
                } catch (RemoteException e) {
                    throw e.rethrowFromSystemServer();
                }
            }
            synchronized (this) {
                if (this.mCachedWallpaper == null || this.mCachedWallpaperUserId != userId || this.mCachedWallpaper.isRecycled()) {
                    this.mCachedWallpaper = null;
                    this.mCachedWallpaperUserId = 0;
                    try {
                        this.mCachedWallpaper = getCurrentWallpaperLocked(context, userId, hardware);
                        this.mCachedWallpaperUserId = userId;
                    } catch (OutOfMemoryError e2) {
                        String access$000 = WallpaperManager.TAG;
                        Log.w(access$000, "Out of memory loading the current wallpaper: " + e2);
                    } catch (SecurityException e3) {
                        if (context.getApplicationInfo().targetSdkVersion < 27) {
                            Log.w(WallpaperManager.TAG, "No permission to access wallpaper, suppressing exception to avoid crashing legacy app.");
                        } else {
                            throw e3;
                        }
                    }
                    if (this.mCachedWallpaper != null) {
                        Bitmap bitmap = this.mCachedWallpaper;
                        return bitmap;
                    }
                } else {
                    Bitmap bitmap2 = this.mCachedWallpaper;
                    return bitmap2;
                }
            }
        }

        /* access modifiers changed from: package-private */
        public void forgetLoadedWallpaper() {
            synchronized (this) {
                this.mCachedWallpaper = null;
                this.mCachedWallpaperUserId = 0;
                this.mDefaultWallpaper = null;
            }
        }

        @OppoHook(level = OppoHook.OppoHookType.CHANGE_CODE, note = "gaoliang@Plf.Keyguard,2012.08.27:Modify for oppo wallpaper", property = OppoHook.OppoRomType.ROM)
        private Bitmap getCurrentWallpaperLocked(Context context, int userId, boolean hardware) {
            if (this.mService == null) {
                Log.w(WallpaperManager.TAG, "WallpaperService not running");
                return null;
            }
            try {
                ParcelFileDescriptor fd = this.mService.getWallpaper(context.getOpPackageName(), this, 1, new Bundle(), userId);
                if (fd != null) {
                    try {
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        if (hardware) {
                            options.inPreferredConfig = Bitmap.Config.HARDWARE;
                        }
                        return OppoWallpaperManagerHelper.generateBitmap(context, BitmapFactory.decodeFileDescriptor(fd.getFileDescriptor(), null, options));
                    } catch (OutOfMemoryError e) {
                        Log.w(WallpaperManager.TAG, "Can't decode file", e);
                    } finally {
                        IoUtils.closeQuietly(fd);
                    }
                }
                return null;
            } catch (RemoteException e2) {
                throw e2.rethrowFromSystemServer();
            }
        }

        @OppoHook(level = OppoHook.OppoHookType.CHANGE_CODE, note = "gaoliang@Plf.Keyguard,2012.08.27:Modify for oppo wallpaper", property = OppoHook.OppoRomType.ROM)
        private Bitmap getDefaultWallpaper(Context context, int which) {
            InputStream is = WallpaperManager.openDefaultWallpaper(context, which);
            if (is != null) {
                try {
                    return OppoWallpaperManagerHelper.generateBitmap(context, BitmapFactory.decodeStream(is, null, new BitmapFactory.Options()));
                } catch (OutOfMemoryError e) {
                    Log.w(WallpaperManager.TAG, "Can't decode stream", e);
                } finally {
                    IoUtils.closeQuietly(is);
                }
            }
            return null;
        }
    }

    static void initGlobals(IWallpaperManager service, Looper looper) {
        synchronized (sSync) {
            if (sGlobals == null) {
                sGlobals = new Globals(service, looper);
            }
        }
    }

    WallpaperManager(IWallpaperManager service, Context context, Handler handler) {
        this.mContext = context;
        initGlobals(service, context.getMainLooper());
    }

    public static WallpaperManager getInstance(Context context) {
        return (WallpaperManager) context.getSystemService(Context.WALLPAPER_SERVICE);
    }

    @UnsupportedAppUsage
    public IWallpaperManager getIWallpaperManager() {
        return sGlobals.mService;
    }

    public Drawable getDrawable() {
        Bitmap bm = sGlobals.peekWallpaperBitmap(this.mContext, true, 1);
        if (bm == null) {
            return null;
        }
        Drawable dr = new BitmapDrawable(this.mContext.getResources(), bm);
        dr.setDither(false);
        return dr;
    }

    public Drawable getBuiltInDrawable() {
        return getBuiltInDrawable(0, 0, false, 0.0f, 0.0f, 1);
    }

    public Drawable getBuiltInDrawable(int which) {
        return getBuiltInDrawable(0, 0, false, 0.0f, 0.0f, which);
    }

    public Drawable getBuiltInDrawable(int outWidth, int outHeight, boolean scaleToFit, float horizontalAlignment, float verticalAlignment) {
        return getBuiltInDrawable(outWidth, outHeight, scaleToFit, horizontalAlignment, verticalAlignment, 1);
    }

    /* JADX WARN: Type inference failed for: r1v3, types: [android.graphics.Rect, android.graphics.BitmapFactory$Options] */
    /* JADX WARN: Type inference failed for: r1v5 */
    /* JADX WARN: Type inference failed for: r1v18 */
    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: ClspMth{java.lang.Math.max(float, float):float}
     arg types: [int, float]
     candidates:
      ClspMth{java.lang.Math.max(double, double):double}
      ClspMth{java.lang.Math.max(int, int):int}
      ClspMth{java.lang.Math.max(long, long):long}
      ClspMth{java.lang.Math.max(float, float):float} */
    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: ClspMth{java.lang.Math.min(float, float):float}
     arg types: [int, float]
     candidates:
      ClspMth{java.lang.Math.min(double, double):double}
      ClspMth{java.lang.Math.min(long, long):long}
      ClspMth{java.lang.Math.min(int, int):int}
      ClspMth{java.lang.Math.min(float, float):float} */
    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: android.graphics.BitmapRegionDecoder.newInstance(java.io.InputStream, boolean):android.graphics.BitmapRegionDecoder
     arg types: [java.io.InputStream, int]
     candidates:
      android.graphics.BitmapRegionDecoder.newInstance(java.io.FileDescriptor, boolean):android.graphics.BitmapRegionDecoder
      android.graphics.BitmapRegionDecoder.newInstance(java.lang.String, boolean):android.graphics.BitmapRegionDecoder
      android.graphics.BitmapRegionDecoder.newInstance(java.io.InputStream, boolean):android.graphics.BitmapRegionDecoder */
    public Drawable getBuiltInDrawable(int outWidth, int outHeight, boolean scaleToFit, float horizontalAlignment, float verticalAlignment, int which) {
        ? r1;
        RectF cropRectF;
        int outWidth2;
        InputStream is;
        if (sGlobals.mService == null) {
            Log.w(TAG, "WallpaperService not running");
            throw new RuntimeException(new DeadSystemException());
        } else if (which == 1 || which == 2) {
            Resources resources = this.mContext.getResources();
            float horizontalAlignment2 = Math.max(0.0f, Math.min(1.0f, horizontalAlignment));
            float verticalAlignment2 = Math.max(0.0f, Math.min(1.0f, verticalAlignment));
            InputStream wpStream = openDefaultWallpaper(this.mContext, which);
            if (wpStream == null) {
                if (DEBUG) {
                    Log.w(TAG, "default wallpaper stream " + which + " is null");
                }
                return null;
            }
            InputStream is2 = new BufferedInputStream(wpStream);
            if (outWidth <= 0) {
                r1 = 0;
            } else if (outHeight <= 0) {
                r1 = 0;
            } else {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeStream(is2, null, options);
                if (options.outWidth == 0 || options.outHeight == 0) {
                    Log.e(TAG, "default wallpaper dimensions are 0");
                    return null;
                }
                int inWidth = options.outWidth;
                int inHeight = options.outHeight;
                InputStream is3 = new BufferedInputStream(openDefaultWallpaper(this.mContext, which));
                int outWidth3 = Math.min(inWidth, outWidth);
                int outHeight2 = Math.min(inHeight, outHeight);
                if (scaleToFit) {
                    is = is3;
                    outWidth2 = outWidth3;
                    cropRectF = getMaxCropRect(inWidth, inHeight, outWidth3, outHeight2, horizontalAlignment2, verticalAlignment2);
                } else {
                    is = is3;
                    outWidth2 = outWidth3;
                    float left = ((float) (inWidth - outWidth2)) * horizontalAlignment2;
                    float top = ((float) (inHeight - outHeight2)) * verticalAlignment2;
                    cropRectF = new RectF(left, top, ((float) outWidth2) + left, ((float) outHeight2) + top);
                }
                Rect roundedTrueCrop = new Rect();
                cropRectF.roundOut(roundedTrueCrop);
                if (roundedTrueCrop.width() > 0) {
                    if (roundedTrueCrop.height() > 0) {
                        int scaleDownSampleSize = Math.min(roundedTrueCrop.width() / outWidth2, roundedTrueCrop.height() / outHeight2);
                        BitmapRegionDecoder decoder = null;
                        try {
                            decoder = BitmapRegionDecoder.newInstance(is, true);
                        } catch (IOException e) {
                            Log.w(TAG, "cannot open region decoder for default wallpaper");
                        }
                        Bitmap crop = null;
                        if (decoder != null) {
                            BitmapFactory.Options options2 = new BitmapFactory.Options();
                            if (scaleDownSampleSize > 1) {
                                options2.inSampleSize = scaleDownSampleSize;
                            }
                            crop = decoder.decodeRegion(roundedTrueCrop, options2);
                            decoder.recycle();
                        }
                        if (crop == null) {
                            InputStream is4 = new BufferedInputStream(openDefaultWallpaper(this.mContext, which));
                            BitmapFactory.Options options3 = new BitmapFactory.Options();
                            if (scaleDownSampleSize > 1) {
                                options3.inSampleSize = scaleDownSampleSize;
                            }
                            Bitmap fullSize = BitmapFactory.decodeStream(is4, null, options3);
                            if (fullSize != null) {
                                crop = Bitmap.createBitmap(fullSize, roundedTrueCrop.left, roundedTrueCrop.top, roundedTrueCrop.width(), roundedTrueCrop.height());
                            }
                        }
                        if (crop == null) {
                            Log.w(TAG, "cannot decode default wallpaper");
                            return null;
                        }
                        if (outWidth2 > 0 && outHeight2 > 0) {
                            if (crop.getWidth() != outWidth2 || crop.getHeight() != outHeight2) {
                                Matrix m = new Matrix();
                                RectF cropRect = new RectF(0.0f, 0.0f, (float) crop.getWidth(), (float) crop.getHeight());
                                RectF returnRect = new RectF(0.0f, 0.0f, (float) outWidth2, (float) outHeight2);
                                m.setRectToRect(cropRect, returnRect, Matrix.ScaleToFit.FILL);
                                Bitmap tmp = Bitmap.createBitmap((int) returnRect.width(), (int) returnRect.height(), Bitmap.Config.ARGB_8888);
                                if (tmp != null) {
                                    Canvas c = new Canvas(tmp);
                                    Paint p = new Paint();
                                    p.setFilterBitmap(true);
                                    c.drawBitmap(crop, m, p);
                                    crop = tmp;
                                }
                            }
                        }
                        return new BitmapDrawable(resources, crop);
                    }
                }
                Log.w(TAG, "crop has bad values for full size image");
                return null;
            }
            return new BitmapDrawable(resources, BitmapFactory.decodeStream(is2, r1, r1));
        } else {
            throw new IllegalArgumentException("Must request exactly one kind of wallpaper");
        }
    }

    private static RectF getMaxCropRect(int inWidth, int inHeight, int outWidth, int outHeight, float horizontalAlignment, float verticalAlignment) {
        RectF cropRect = new RectF();
        if (((float) inWidth) / ((float) inHeight) > ((float) outWidth) / ((float) outHeight)) {
            cropRect.top = 0.0f;
            cropRect.bottom = (float) inHeight;
            float cropWidth = ((float) outWidth) * (((float) inHeight) / ((float) outHeight));
            cropRect.left = (((float) inWidth) - cropWidth) * horizontalAlignment;
            cropRect.right = cropRect.left + cropWidth;
        } else {
            cropRect.left = 0.0f;
            cropRect.right = (float) inWidth;
            float cropHeight = ((float) outHeight) * (((float) inWidth) / ((float) outWidth));
            cropRect.top = (((float) inHeight) - cropHeight) * verticalAlignment;
            cropRect.bottom = cropRect.top + cropHeight;
        }
        return cropRect;
    }

    public Drawable peekDrawable() {
        Bitmap bm = sGlobals.peekWallpaperBitmap(this.mContext, false, 1);
        if (bm == null) {
            return null;
        }
        Drawable dr = new BitmapDrawable(this.mContext.getResources(), bm);
        dr.setDither(false);
        return dr;
    }

    public Drawable getFastDrawable() {
        Bitmap bm = sGlobals.peekWallpaperBitmap(this.mContext, true, 1);
        if (bm != null) {
            return new FastBitmapDrawable(bm);
        }
        return null;
    }

    public Drawable peekFastDrawable() {
        Bitmap bm = sGlobals.peekWallpaperBitmap(this.mContext, false, 1);
        if (bm != null) {
            return new FastBitmapDrawable(bm);
        }
        return null;
    }

    @UnsupportedAppUsage
    public Bitmap getBitmap() {
        return getBitmap(false);
    }

    @UnsupportedAppUsage
    public Bitmap getBitmap(boolean hardware) {
        return getBitmapAsUser(this.mContext.getUserId(), hardware);
    }

    public Bitmap getBitmapAsUser(int userId, boolean hardware) {
        return sGlobals.peekWallpaperBitmap(this.mContext, true, 1, userId, hardware);
    }

    public ParcelFileDescriptor getWallpaperFile(int which) {
        return getWallpaperFile(which, this.mContext.getUserId());
    }

    public void addOnColorsChangedListener(OnColorsChangedListener listener, Handler handler) {
        addOnColorsChangedListener(listener, handler, this.mContext.getUserId());
    }

    @UnsupportedAppUsage
    public void addOnColorsChangedListener(OnColorsChangedListener listener, Handler handler, int userId) {
        sGlobals.addOnColorsChangedListener(listener, handler, userId, this.mContext.getDisplayId());
    }

    public void removeOnColorsChangedListener(OnColorsChangedListener callback) {
        removeOnColorsChangedListener(callback, this.mContext.getUserId());
    }

    public void removeOnColorsChangedListener(OnColorsChangedListener callback, int userId) {
        sGlobals.removeOnColorsChangedListener(callback, userId, this.mContext.getDisplayId());
    }

    public WallpaperColors getWallpaperColors(int which) {
        return getWallpaperColors(which, this.mContext.getUserId());
    }

    @UnsupportedAppUsage
    public WallpaperColors getWallpaperColors(int which, int userId) {
        return sGlobals.getWallpaperColors(which, userId, this.mContext.getDisplayId());
    }

    @UnsupportedAppUsage
    public ParcelFileDescriptor getWallpaperFile(int which, int userId) {
        if (which != 1 && which != 2) {
            throw new IllegalArgumentException("Must request exactly one kind of wallpaper");
        } else if (sGlobals.mService != null) {
            try {
                return sGlobals.mService.getWallpaper(this.mContext.getOpPackageName(), null, which, new Bundle(), userId);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            } catch (SecurityException e2) {
                if (this.mContext.getApplicationInfo().targetSdkVersion < 27) {
                    Log.w(TAG, "No permission to access wallpaper, suppressing exception to avoid crashing legacy app.");
                    return null;
                }
                throw e2;
            }
        } else {
            Log.w(TAG, "WallpaperService not running");
            throw new RuntimeException(new DeadSystemException());
        }
    }

    public void forgetLoadedWallpaper() {
        sGlobals.forgetLoadedWallpaper();
    }

    public WallpaperInfo getWallpaperInfo() {
        return getWallpaperInfo(this.mContext.getUserId());
    }

    public WallpaperInfo getWallpaperInfo(int userId) {
        try {
            if (sGlobals.mService != null) {
                return sGlobals.mService.getWallpaperInfo(userId);
            }
            Log.w(TAG, "WallpaperService not running");
            throw new RuntimeException(new DeadSystemException());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public int getWallpaperId(int which) {
        return getWallpaperIdForUser(which, this.mContext.getUserId());
    }

    public int getWallpaperIdForUser(int which, int userId) {
        try {
            if (sGlobals.mService != null) {
                return sGlobals.mService.getWallpaperIdForUser(which, userId);
            }
            Log.w(TAG, "WallpaperService not running");
            throw new RuntimeException(new DeadSystemException());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public Intent getCropAndSetWallpaperIntent(Uri imageUri) {
        if (imageUri == null) {
            throw new IllegalArgumentException("Image URI must not be null");
        } else if ("content".equals(imageUri.getScheme())) {
            PackageManager packageManager = this.mContext.getPackageManager();
            Intent cropAndSetWallpaperIntent = new Intent(ACTION_CROP_AND_SET_WALLPAPER, imageUri);
            cropAndSetWallpaperIntent.addFlags(1);
            ResolveInfo resolvedHome = packageManager.resolveActivity(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME), 65536);
            if (resolvedHome != null) {
                cropAndSetWallpaperIntent.setPackage(resolvedHome.activityInfo.packageName);
                if (packageManager.queryIntentActivities(cropAndSetWallpaperIntent, 0).size() > 0) {
                    return cropAndSetWallpaperIntent;
                }
            }
            cropAndSetWallpaperIntent.setPackage(this.mContext.getString(R.string.config_wallpaperCropperPackage));
            if (packageManager.queryIntentActivities(cropAndSetWallpaperIntent, 0).size() > 0) {
                return cropAndSetWallpaperIntent;
            }
            throw new IllegalArgumentException("Cannot use passed URI to set wallpaper; check that the type returned by ContentProvider matches image/*");
        } else {
            throw new IllegalArgumentException("Image URI must be of the content scheme type");
        }
    }

    public void setResource(int resid) throws IOException {
        setResource(resid, 3);
    }

    public int setResource(int resid, int which) throws IOException {
        if (sGlobals.mService != null) {
            Bundle result = new Bundle();
            WallpaperSetCompletion completion = new WallpaperSetCompletion();
            try {
                Resources resources = this.mContext.getResources();
                IWallpaperManager access$200 = sGlobals.mService;
                ParcelFileDescriptor fd = access$200.setWallpaper("res:" + resources.getResourceName(resid), this.mContext.getOpPackageName(), null, false, result, which, completion, this.mContext.getUserId());
                if (fd != null) {
                    FileOutputStream fos = null;
                    try {
                        fos = new ParcelFileDescriptor.AutoCloseOutputStream(fd);
                        copyStreamToWallpaperFile(resources.openRawResource(resid), fos);
                        fos.close();
                        completion.waitForCompletion();
                    } finally {
                        IoUtils.closeQuietly(fos);
                    }
                }
                return result.getInt(EXTRA_NEW_WALLPAPER_ID, 0);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        } else {
            Log.w(TAG, "WallpaperService not running");
            throw new RuntimeException(new DeadSystemException());
        }
    }

    public void setBitmap(Bitmap bitmap) throws IOException {
        setBitmap(bitmap, null, true);
    }

    public int setBitmap(Bitmap fullImage, Rect visibleCropHint, boolean allowBackup) throws IOException {
        return setBitmap(fullImage, visibleCropHint, allowBackup, 3);
    }

    public int setBitmap(Bitmap fullImage, Rect visibleCropHint, boolean allowBackup, int which) throws IOException {
        return setBitmap(fullImage, visibleCropHint, allowBackup, which, this.mContext.getUserId());
    }

    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    public int setBitmap(Bitmap fullImage, Rect visibleCropHint, boolean allowBackup, int which, int userId) throws IOException {
        validateRect(visibleCropHint);
        if (sGlobals.mService != null) {
            Bundle result = new Bundle();
            WallpaperSetCompletion completion = new WallpaperSetCompletion();
            try {
                ParcelFileDescriptor fd = sGlobals.mService.setWallpaper(null, this.mContext.getOpPackageName(), visibleCropHint, allowBackup, result, which, completion, userId);
                if (fd != null) {
                    FileOutputStream fos = null;
                    try {
                        fos = new ParcelFileDescriptor.AutoCloseOutputStream(fd);
                        fullImage.compress(Bitmap.CompressFormat.PNG, 90, fos);
                        fos.close();
                        completion.waitForCompletion();
                    } finally {
                        IoUtils.closeQuietly(fos);
                    }
                }
                return result.getInt(EXTRA_NEW_WALLPAPER_ID, 0);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        } else {
            Log.w(TAG, "WallpaperService not running");
            throw new RuntimeException(new DeadSystemException());
        }
    }

    private final void validateRect(Rect rect) {
        if (rect != null && rect.isEmpty()) {
            throw new IllegalArgumentException("visibleCrop rectangle must be valid and non-empty");
        }
    }

    public void setStream(InputStream bitmapData) throws IOException {
        setStream(bitmapData, null, true);
    }

    private void copyStreamToWallpaperFile(InputStream data, FileOutputStream fos) throws IOException {
        FileUtils.copy(data, fos);
    }

    public int setStream(InputStream bitmapData, Rect visibleCropHint, boolean allowBackup) throws IOException {
        return setStream(bitmapData, visibleCropHint, allowBackup, 3);
    }

    public int setStream(InputStream bitmapData, Rect visibleCropHint, boolean allowBackup, int which) throws IOException {
        validateRect(visibleCropHint);
        if (sGlobals.mService != null) {
            Bundle result = new Bundle();
            WallpaperSetCompletion completion = new WallpaperSetCompletion();
            try {
                ParcelFileDescriptor fd = sGlobals.mService.setWallpaper(null, this.mContext.getOpPackageName(), visibleCropHint, allowBackup, result, which, completion, this.mContext.getUserId());
                if (fd != null) {
                    FileOutputStream fos = null;
                    try {
                        fos = new ParcelFileDescriptor.AutoCloseOutputStream(fd);
                        copyStreamToWallpaperFile(bitmapData, fos);
                        fos.close();
                        completion.waitForCompletion();
                    } finally {
                        IoUtils.closeQuietly(fos);
                    }
                }
                return result.getInt(EXTRA_NEW_WALLPAPER_ID, 0);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        } else {
            Log.w(TAG, "WallpaperService not running");
            throw new RuntimeException(new DeadSystemException());
        }
    }

    public boolean hasResourceWallpaper(int resid) {
        if (sGlobals.mService != null) {
            try {
                Resources resources = this.mContext.getResources();
                return sGlobals.mService.hasNamedWallpaper("res:" + resources.getResourceName(resid));
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        } else {
            Log.w(TAG, "WallpaperService not running");
            throw new RuntimeException(new DeadSystemException());
        }
    }

    public int getDesiredMinimumWidth() {
        if (sGlobals.mService != null) {
            try {
                return sGlobals.mService.getWidthHint(this.mContext.getDisplayId());
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        } else {
            Log.w(TAG, "WallpaperService not running");
            throw new RuntimeException(new DeadSystemException());
        }
    }

    public int getDesiredMinimumHeight() {
        if (sGlobals.mService != null) {
            try {
                return sGlobals.mService.getHeightHint(this.mContext.getDisplayId());
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        } else {
            Log.w(TAG, "WallpaperService not running");
            throw new RuntimeException(new DeadSystemException());
        }
    }

    public void suggestDesiredDimensions(int minimumWidth, int minimumHeight) {
        int maximumTextureSize;
        try {
            maximumTextureSize = SystemProperties.getInt("sys.max_texture_size", 0);
        } catch (Exception e) {
            maximumTextureSize = 0;
        }
        if (maximumTextureSize > 0 && (minimumWidth > maximumTextureSize || minimumHeight > maximumTextureSize)) {
            float aspect = ((float) minimumHeight) / ((float) minimumWidth);
            if (minimumWidth > minimumHeight) {
                minimumWidth = maximumTextureSize;
                minimumHeight = (int) (((double) (((float) minimumWidth) * aspect)) + 0.5d);
            } else {
                minimumHeight = maximumTextureSize;
                minimumWidth = (int) (((double) (((float) minimumHeight) / aspect)) + 0.5d);
            }
        }
        try {
            if (sGlobals.mService != null) {
                sGlobals.mService.setDimensionHints(minimumWidth, minimumHeight, this.mContext.getOpPackageName(), this.mContext.getDisplayId());
            } else {
                Log.w(TAG, "WallpaperService not running");
                throw new RuntimeException(new DeadSystemException());
            }
        } catch (RemoteException e2) {
            throw e2.rethrowFromSystemServer();
        }
    }

    public void setDisplayPadding(Rect padding) {
        try {
            if (sGlobals.mService != null) {
                sGlobals.mService.setDisplayPadding(padding, this.mContext.getOpPackageName(), this.mContext.getDisplayId());
            } else {
                Log.w(TAG, "WallpaperService not running");
                throw new RuntimeException(new DeadSystemException());
            }
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @SystemApi
    public void setDisplayOffset(IBinder windowToken, int x, int y) {
        try {
            WindowManagerGlobal.getWindowSession().setWallpaperDisplayOffset(windowToken, x, y);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void clearWallpaper() {
        clearWallpaper(2, this.mContext.getUserId());
        clearWallpaper(1, this.mContext.getUserId());
    }

    @SystemApi
    public void clearWallpaper(int which, int userId) {
        if (sGlobals.mService != null) {
            try {
                sGlobals.mService.clearWallpaper(this.mContext.getOpPackageName(), which, userId);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        } else {
            Log.w(TAG, "WallpaperService not running");
            throw new RuntimeException(new DeadSystemException());
        }
    }

    @SystemApi
    public boolean setWallpaperComponent(ComponentName name) {
        return setWallpaperComponent(name, this.mContext.getUserId());
    }

    @UnsupportedAppUsage
    public boolean setWallpaperComponent(ComponentName name, int userId) {
        if (sGlobals.mService != null) {
            try {
                sGlobals.mService.setWallpaperComponentChecked(name, this.mContext.getOpPackageName(), userId);
                return true;
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        } else {
            Log.w(TAG, "WallpaperService not running");
            throw new RuntimeException(new DeadSystemException());
        }
    }

    public void setWallpaperOffsets(IBinder windowToken, float xOffset, float yOffset) {
        try {
            WindowManagerGlobal.getWindowSession().setWallpaperPosition(windowToken, xOffset, yOffset, this.mWallpaperXStep, this.mWallpaperYStep);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setWallpaperOffsetSteps(float xStep, float yStep) {
        this.mWallpaperXStep = xStep;
        this.mWallpaperYStep = yStep;
    }

    public void sendWallpaperCommand(IBinder windowToken, String action, int x, int y, int z, Bundle extras) {
        try {
            WindowManagerGlobal.getWindowSession().sendWallpaperCommand(windowToken, action, x, y, z, extras, false);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean isWallpaperSupported() {
        if (sGlobals.mService != null) {
            try {
                return sGlobals.mService.isWallpaperSupported(this.mContext.getOpPackageName());
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        } else {
            Log.w(TAG, "WallpaperService not running");
            throw new RuntimeException(new DeadSystemException());
        }
    }

    public boolean isSetWallpaperAllowed() {
        if (sGlobals.mService != null) {
            try {
                return sGlobals.mService.isSetWallpaperAllowed(this.mContext.getOpPackageName());
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        } else {
            Log.w(TAG, "WallpaperService not running");
            throw new RuntimeException(new DeadSystemException());
        }
    }

    public void clearWallpaperOffsets(IBinder windowToken) {
        try {
            WindowManagerGlobal.getWindowSession().setWallpaperPosition(windowToken, -1.0f, -1.0f, -1.0f, -1.0f);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void clear() throws IOException {
        setStream(openDefaultWallpaper(this.mContext, 1), null, false);
    }

    public void clear(int which) throws IOException {
        if ((which & 1) != 0) {
            clear();
        }
        if ((which & 2) != 0) {
            clearWallpaper(2, this.mContext.getUserId());
        }
    }

    @UnsupportedAppUsage
    @OppoHook(level = OppoHook.OppoHookType.CHANGE_RESOURCE, note = "gaoliang@Plf.Keyguard,2012.08.27:add to change the default wallpaper res", property = OppoHook.OppoRomType.ROM)
    public static InputStream openDefaultWallpaper(Context context, int which) {
        return OppoWallpaperManagerHelper.openDefaultWallpaper(context, which);
    }

    public static ComponentName getDefaultWallpaperComponent(Context context) {
        ComponentName cn2 = null;
        String flat = SystemProperties.get(PROP_WALLPAPER_COMPONENT);
        if (!TextUtils.isEmpty(flat)) {
            cn2 = ComponentName.unflattenFromString(flat);
        }
        if (cn2 == null) {
            String flat2 = context.getString(R.string.default_wallpaper_component);
            if (!TextUtils.isEmpty(flat2)) {
                cn2 = ComponentName.unflattenFromString(flat2);
            }
        }
        if (cn2 == null) {
            return cn2;
        }
        try {
            context.getPackageManager().getPackageInfo(cn2.getPackageName(), 786432);
            return cn2;
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    public boolean setLockWallpaperCallback(IWallpaperManagerCallback callback) {
        if (sGlobals.mService != null) {
            try {
                return sGlobals.mService.setLockWallpaperCallback(callback);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        } else {
            Log.w(TAG, "WallpaperService not running");
            throw new RuntimeException(new DeadSystemException());
        }
    }

    public boolean isWallpaperBackupEligible(int which) {
        if (sGlobals.mService != null) {
            try {
                return sGlobals.mService.isWallpaperBackupEligible(which, this.mContext.getUserId());
            } catch (RemoteException e) {
                String str = TAG;
                Log.e(str, "Exception querying wallpaper backup eligibility: " + e.getMessage());
                return false;
            }
        } else {
            Log.w(TAG, "WallpaperService not running");
            throw new RuntimeException(new DeadSystemException());
        }
    }

    private class WallpaperSetCompletion extends IWallpaperManagerCallback.Stub {
        final CountDownLatch mLatch = new CountDownLatch(1);

        public WallpaperSetCompletion() {
        }

        public void waitForCompletion() {
            try {
                this.mLatch.await(30, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
            }
        }

        @Override // android.app.IWallpaperManagerCallback
        public void onWallpaperChanged() throws RemoteException {
            this.mLatch.countDown();
        }

        @Override // android.app.IWallpaperManagerCallback
        public void onWallpaperColorsChanged(WallpaperColors colors, int which, int userId) throws RemoteException {
            WallpaperManager.sGlobals.onWallpaperColorsChanged(colors, which, userId);
        }
    }

    public interface OnColorsChangedListener {
        void onColorsChanged(WallpaperColors wallpaperColors, int i);

        default void onColorsChanged(WallpaperColors colors, int which, int userId) {
            onColorsChanged(colors, which);
        }
    }
}
