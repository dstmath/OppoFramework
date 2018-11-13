package android.app;

import android.annotation.OppoHook;
import android.annotation.OppoHook.OppoHookType;
import android.annotation.OppoHook.OppoRomType;
import android.app.IWallpaperManagerCallback.Stub;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Matrix.ScaleToFit;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.camera2.params.TonemapCurve;
import android.net.Uri;
import android.os.Bundle;
import android.os.DeadSystemException;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.os.ParcelFileDescriptor.AutoCloseOutputStream;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;
import android.view.WindowManagerGlobal;
import com.mediatek.common.MPlugin;
import com.mediatek.common.wallpaper.IWallpaperPlugin;
import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CountDownLatch;
import libcore.io.IoUtils;

/*  JADX ERROR: NullPointerException in pass: ReSugarCode
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
    	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.utils.BlockUtils.isAllBlocksEmpty(BlockUtils.java:546)
    	at jadx.core.dex.visitors.ExtractFieldInit.getConstructorsList(ExtractFieldInit.java:221)
    	at jadx.core.dex.visitors.ExtractFieldInit.moveCommonFieldsInit(ExtractFieldInit.java:121)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:46)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:42)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
/*  JADX ERROR: NullPointerException in pass: ClassModifier
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ClassModifier.removeFieldUsageFromConstructor(ClassModifier.java:100)
    	at jadx.core.dex.visitors.ClassModifier.removeSyntheticFields(ClassModifier.java:75)
    	at jadx.core.dex.visitors.ClassModifier.visit(ClassModifier.java:48)
    	at jadx.core.dex.visitors.ClassModifier.visit(ClassModifier.java:40)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class WallpaperManager {
    public static final String ACTION_CHANGE_LIVE_WALLPAPER = "android.service.wallpaper.CHANGE_LIVE_WALLPAPER";
    public static final String ACTION_CROP_AND_SET_WALLPAPER = "android.service.wallpaper.CROP_AND_SET_WALLPAPER";
    public static final String ACTION_LIVE_WALLPAPER_CHOOSER = "android.service.wallpaper.LIVE_WALLPAPER_CHOOSER";
    public static final String COMMAND_DROP = "android.home.drop";
    public static final String COMMAND_SECONDARY_TAP = "android.wallpaper.secondaryTap";
    public static final String COMMAND_TAP = "android.wallpaper.tap";
    private static boolean DEBUG = false;
    public static final String EXTRA_LIVE_WALLPAPER_COMPONENT = "android.service.wallpaper.extra.LIVE_WALLPAPER_COMPONENT";
    public static final String EXTRA_NEW_WALLPAPER_ID = "android.service.wallpaper.extra.ID";
    public static final int FLAG_LOCK = 2;
    public static final int FLAG_SYSTEM = 1;
    private static final String PROP_LOCK_WALLPAPER = "ro.config.lock_wallpaper";
    private static final String PROP_WALLPAPER = "ro.config.wallpaper";
    private static final String PROP_WALLPAPER_COMPONENT = "ro.config.wallpaper_component";
    private static String TAG = null;
    public static final String WALLPAPER_PREVIEW_META_DATA = "android.wallpaper.preview";
    private static Globals sGlobals;
    private static final Object sSync = null;
    private final Context mContext;
    private float mWallpaperXStep;
    private float mWallpaperYStep;

    static class FastBitmapDrawable extends Drawable {
        private final Bitmap mBitmap;
        private int mDrawLeft;
        private int mDrawTop;
        private final int mHeight;
        private final Paint mPaint;
        private final int mWidth;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.app.WallpaperManager.FastBitmapDrawable.<init>(android.graphics.Bitmap):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        private FastBitmapDrawable(android.graphics.Bitmap r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.app.WallpaperManager.FastBitmapDrawable.<init>(android.graphics.Bitmap):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.app.WallpaperManager.FastBitmapDrawable.<init>(android.graphics.Bitmap):void");
        }

        /* synthetic */ FastBitmapDrawable(Bitmap bitmap, FastBitmapDrawable fastBitmapDrawable) {
            this(bitmap);
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.app.WallpaperManager.FastBitmapDrawable.draw(android.graphics.Canvas):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public void draw(android.graphics.Canvas r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.app.WallpaperManager.FastBitmapDrawable.draw(android.graphics.Canvas):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.app.WallpaperManager.FastBitmapDrawable.draw(android.graphics.Canvas):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: android.app.WallpaperManager.FastBitmapDrawable.getIntrinsicHeight():int, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e3
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public int getIntrinsicHeight() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: android.app.WallpaperManager.FastBitmapDrawable.getIntrinsicHeight():int, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.app.WallpaperManager.FastBitmapDrawable.getIntrinsicHeight():int");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: android.app.WallpaperManager.FastBitmapDrawable.getIntrinsicWidth():int, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e3
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public int getIntrinsicWidth() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: android.app.WallpaperManager.FastBitmapDrawable.getIntrinsicWidth():int, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.app.WallpaperManager.FastBitmapDrawable.getIntrinsicWidth():int");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: android.app.WallpaperManager.FastBitmapDrawable.getMinimumHeight():int, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e3
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public int getMinimumHeight() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: android.app.WallpaperManager.FastBitmapDrawable.getMinimumHeight():int, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.app.WallpaperManager.FastBitmapDrawable.getMinimumHeight():int");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: android.app.WallpaperManager.FastBitmapDrawable.getMinimumWidth():int, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e3
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public int getMinimumWidth() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: android.app.WallpaperManager.FastBitmapDrawable.getMinimumWidth():int, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.app.WallpaperManager.FastBitmapDrawable.getMinimumWidth():int");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: android.app.WallpaperManager.FastBitmapDrawable.setBounds(int, int, int, int):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e3
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public void setBounds(int r1, int r2, int r3, int r4) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: android.app.WallpaperManager.FastBitmapDrawable.setBounds(int, int, int, int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.app.WallpaperManager.FastBitmapDrawable.setBounds(int, int, int, int):void");
        }

        public int getOpacity() {
            return -1;
        }

        public void setAlpha(int alpha) {
            throw new UnsupportedOperationException("Not supported with this drawable");
        }

        public void setColorFilter(ColorFilter colorFilter) {
            throw new UnsupportedOperationException("Not supported with this drawable");
        }

        public void setDither(boolean dither) {
            throw new UnsupportedOperationException("Not supported with this drawable");
        }

        public void setFilterBitmap(boolean filter) {
            throw new UnsupportedOperationException("Not supported with this drawable");
        }
    }

    static class Globals extends Stub {
        private Bitmap mCachedWallpaper;
        private int mCachedWallpaperUserId;
        private Bitmap mDefaultWallpaper;
        private final IWallpaperManager mService;

        Globals(Looper looper) {
            this.mService = IWallpaperManager.Stub.asInterface(ServiceManager.getService(Context.WALLPAPER_SERVICE));
            forgetLoadedWallpaper();
        }

        public void onWallpaperChanged() {
            forgetLoadedWallpaper();
        }

        public Bitmap peekWallpaperBitmap(Context context, boolean returnDefault, int which) {
            return peekWallpaperBitmap(context, returnDefault, which, context.getUserId());
        }

        /* JADX WARNING: Missing block: B:36:0x004b, code:
            if (r8 == false) goto L_0x005d;
     */
        /* JADX WARNING: Missing block: B:37:0x004d, code:
            r0 = r6.mDefaultWallpaper;
     */
        /* JADX WARNING: Missing block: B:38:0x004f, code:
            if (r0 != null) goto L_0x0059;
     */
        /* JADX WARNING: Missing block: B:39:0x0051, code:
            r0 = getDefaultWallpaperLocked(r7, r9);
     */
        /* JADX WARNING: Missing block: B:40:0x0055, code:
            monitor-enter(r6);
     */
        /* JADX WARNING: Missing block: B:42:?, code:
            r6.mDefaultWallpaper = r0;
     */
        /* JADX WARNING: Missing block: B:43:0x0058, code:
            monitor-exit(r6);
     */
        /* JADX WARNING: Missing block: B:44:0x0059, code:
            return r0;
     */
        /* JADX WARNING: Missing block: B:48:0x005d, code:
            return null;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public Bitmap peekWallpaperBitmap(Context context, boolean returnDefault, int which, int userId) {
            if (this.mService != null) {
                try {
                    if (!this.mService.isWallpaperSupported(context.getOpPackageName())) {
                        return null;
                    }
                } catch (RemoteException e) {
                    throw e.rethrowFromSystemServer();
                }
            }
            synchronized (this) {
                Bitmap bitmap;
                if (this.mCachedWallpaper == null || this.mCachedWallpaperUserId != userId) {
                    this.mCachedWallpaper = null;
                    this.mCachedWallpaperUserId = 0;
                    try {
                        this.mCachedWallpaper = getCurrentWallpaperLocked(context, userId);
                        this.mCachedWallpaperUserId = userId;
                    } catch (OutOfMemoryError e2) {
                        Log.w(WallpaperManager.TAG, "No memory load current wallpaper", e2);
                    }
                    if (this.mCachedWallpaper != null) {
                        bitmap = this.mCachedWallpaper;
                        return bitmap;
                    }
                }
                bitmap = this.mCachedWallpaper;
                return bitmap;
            }
        }

        void forgetLoadedWallpaper() {
            synchronized (this) {
                this.mCachedWallpaper = null;
                this.mCachedWallpaperUserId = 0;
                this.mDefaultWallpaper = null;
            }
        }

        @OppoHook(level = OppoHookType.CHANGE_CODE, note = "gaoliang@Plf.Keyguard,2012.08.27:Modify for oppo wallpaper", property = OppoRomType.ROM)
        private Bitmap getCurrentWallpaperLocked(Context context, int userId) {
            if (this.mService == null) {
                Log.w(WallpaperManager.TAG, "WallpaperService not running");
                return null;
            }
            try {
                ParcelFileDescriptor fd = this.mService.getWallpaper(this, 1, new Bundle(), userId);
                if (fd != null) {
                    Bitmap generateBitmap;
                    try {
                        generateBitmap = WallpaperManager.generateBitmap(context, BitmapFactory.decodeFileDescriptor(fd.getFileDescriptor(), null, new Options()));
                        return generateBitmap;
                    } catch (OutOfMemoryError e) {
                        generateBitmap = WallpaperManager.TAG;
                        Log.w(generateBitmap, "Can't decode file", e);
                    } finally {
                        IoUtils.closeQuietly(fd);
                    }
                }
                return null;
            } catch (RemoteException e2) {
                throw e2.rethrowFromSystemServer();
            }
        }

        private InputStream openDefaultWallpaperRes(Context context, int which) {
            IWallpaperPlugin mWallpaperPlugin = null;
            try {
                mWallpaperPlugin = (IWallpaperPlugin) MPlugin.createInstance(IWallpaperPlugin.class.getName(), context);
            } catch (Exception e) {
                Log.e(WallpaperManager.TAG, "Catch IWallpaperPlugin exception: ", e);
            }
            if (mWallpaperPlugin == null || mWallpaperPlugin.getPluginResources(context) == null) {
                return context.getResources().openRawResource(WallpaperManager.getDefaultWallpaperResID(context));
            }
            Log.d(WallpaperManager.TAG, "get the wallpaper image from the plug-in");
            return mWallpaperPlugin.getPluginResources(context).openRawResource(mWallpaperPlugin.getPluginDefaultImage());
        }

        @OppoHook(level = OppoHookType.CHANGE_CODE, note = "gaoliang@Plf.Keyguard,2012.08.27:Modify for oppo wallpaper", property = OppoRomType.ROM)
        private Bitmap getDefaultWallpaperLocked(Context context, int which) {
            InputStream is = openDefaultWallpaperRes(context, which);
            if (is != null) {
                Bitmap generateBitmap;
                try {
                    generateBitmap = WallpaperManager.generateBitmap(context, BitmapFactory.decodeStream(is, null, new Options()));
                    return generateBitmap;
                } catch (OutOfMemoryError e) {
                    generateBitmap = WallpaperManager.TAG;
                    Log.w(generateBitmap, "Can't decode stream", e);
                } finally {
                    IoUtils.closeQuietly(is);
                }
            }
            return null;
        }
    }

    private class WallpaperSetCompletion extends Stub {
        final CountDownLatch mLatch;
        final /* synthetic */ WallpaperManager this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: android.app.WallpaperManager.WallpaperSetCompletion.<init>(android.app.WallpaperManager):void, dex:  in method: android.app.WallpaperManager.WallpaperSetCompletion.<init>(android.app.WallpaperManager):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: jadx.core.utils.exceptions.DecodeException: null in method: android.app.WallpaperManager.WallpaperSetCompletion.<init>(android.app.WallpaperManager):void, dex: 
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
            	... 10 more
            Caused by: java.io.EOFException
            	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
            	at com.android.dx.io.instructions.ShortArrayCodeInput.readLong(ShortArrayCodeInput.java:71)
            	at com.android.dx.io.instructions.InstructionCodec$31.decode(InstructionCodec.java:652)
            	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
            	... 11 more
            */
        public WallpaperSetCompletion(android.app.WallpaperManager r1) {
            /*
            // Can't load method instructions: Load method exception: null in method: android.app.WallpaperManager.WallpaperSetCompletion.<init>(android.app.WallpaperManager):void, dex:  in method: android.app.WallpaperManager.WallpaperSetCompletion.<init>(android.app.WallpaperManager):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.app.WallpaperManager.WallpaperSetCompletion.<init>(android.app.WallpaperManager):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.app.WallpaperManager.WallpaperSetCompletion.onWallpaperChanged():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public void onWallpaperChanged() throws android.os.RemoteException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.app.WallpaperManager.WallpaperSetCompletion.onWallpaperChanged():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.app.WallpaperManager.WallpaperSetCompletion.onWallpaperChanged():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.app.WallpaperManager.WallpaperSetCompletion.waitForCompletion():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public void waitForCompletion() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.app.WallpaperManager.WallpaperSetCompletion.waitForCompletion():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.app.WallpaperManager.WallpaperSetCompletion.waitForCompletion():void");
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.app.WallpaperManager.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 9 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.app.WallpaperManager.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.app.WallpaperManager.<clinit>():void");
    }

    static void initGlobals(Looper looper) {
        synchronized (sSync) {
            if (sGlobals == null) {
                sGlobals = new Globals(looper);
            }
        }
    }

    WallpaperManager(Context context, Handler handler) {
        this.mWallpaperXStep = -1.0f;
        this.mWallpaperYStep = -1.0f;
        this.mContext = context;
        initGlobals(context.getMainLooper());
    }

    public static WallpaperManager getInstance(Context context) {
        return (WallpaperManager) context.getSystemService(Context.WALLPAPER_SERVICE);
    }

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
        return getBuiltInDrawable(0, 0, false, TonemapCurve.LEVEL_BLACK, TonemapCurve.LEVEL_BLACK, 1);
    }

    public Drawable getBuiltInDrawable(int which) {
        return getBuiltInDrawable(0, 0, false, TonemapCurve.LEVEL_BLACK, TonemapCurve.LEVEL_BLACK, which);
    }

    public Drawable getBuiltInDrawable(int outWidth, int outHeight, boolean scaleToFit, float horizontalAlignment, float verticalAlignment) {
        return getBuiltInDrawable(outWidth, outHeight, scaleToFit, horizontalAlignment, verticalAlignment, 1);
    }

    public Drawable getBuiltInDrawable(int outWidth, int outHeight, boolean scaleToFit, float horizontalAlignment, float verticalAlignment, int which) {
        if (sGlobals.mService == null) {
            Log.w(TAG, "WallpaperService not running");
            throw new RuntimeException(new DeadSystemException());
        } else if (which == 1 || which == 2) {
            Resources resources = this.mContext.getResources();
            horizontalAlignment = Math.max(TonemapCurve.LEVEL_BLACK, Math.min(1.0f, horizontalAlignment));
            verticalAlignment = Math.max(TonemapCurve.LEVEL_BLACK, Math.min(1.0f, verticalAlignment));
            InputStream wpStream = sGlobals.openDefaultWallpaperRes(this.mContext, which);
            if (wpStream == null) {
                if (DEBUG) {
                    Log.w(TAG, "default wallpaper stream " + which + " is null");
                }
                return null;
            }
            InputStream bufferedInputStream = new BufferedInputStream(wpStream);
            if (outWidth <= 0 || outHeight <= 0) {
                return new BitmapDrawable(resources, BitmapFactory.decodeStream(bufferedInputStream, null, null));
            }
            Options options = new Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(bufferedInputStream, null, options);
            if (options.outWidth == 0 || options.outHeight == 0) {
                Log.e(TAG, "default wallpaper dimensions are 0");
                return null;
            }
            RectF cropRectF;
            int inWidth = options.outWidth;
            int inHeight = options.outHeight;
            bufferedInputStream = new BufferedInputStream(sGlobals.openDefaultWallpaperRes(this.mContext, which));
            outWidth = Math.min(inWidth, outWidth);
            outHeight = Math.min(inHeight, outHeight);
            if (scaleToFit) {
                cropRectF = getMaxCropRect(inWidth, inHeight, outWidth, outHeight, horizontalAlignment, verticalAlignment);
            } else {
                float left = ((float) (inWidth - outWidth)) * horizontalAlignment;
                float top = ((float) (inHeight - outHeight)) * verticalAlignment;
                cropRectF = new RectF(left, top, left + ((float) outWidth), top + ((float) outHeight));
            }
            Rect roundedTrueCrop = new Rect();
            cropRectF.roundOut(roundedTrueCrop);
            if (roundedTrueCrop.width() <= 0 || roundedTrueCrop.height() <= 0) {
                Log.w(TAG, "crop has bad values for full size image");
                return null;
            }
            int scaleDownSampleSize = Math.min(roundedTrueCrop.width() / outWidth, roundedTrueCrop.height() / outHeight);
            BitmapRegionDecoder decoder = null;
            try {
                decoder = BitmapRegionDecoder.newInstance(bufferedInputStream, true);
            } catch (IOException e) {
                Log.w(TAG, "cannot open region decoder for default wallpaper");
            }
            Bitmap crop = null;
            if (decoder != null) {
                options = new Options();
                if (scaleDownSampleSize > 1) {
                    options.inSampleSize = scaleDownSampleSize;
                }
                crop = decoder.decodeRegion(roundedTrueCrop, options);
                decoder.recycle();
            }
            if (crop == null) {
                bufferedInputStream = new BufferedInputStream(sGlobals.openDefaultWallpaperRes(this.mContext, which));
                options = new Options();
                if (scaleDownSampleSize > 1) {
                    options.inSampleSize = scaleDownSampleSize;
                }
                Bitmap fullSize = BitmapFactory.decodeStream(bufferedInputStream, null, options);
                if (fullSize != null) {
                    crop = Bitmap.createBitmap(fullSize, roundedTrueCrop.left, roundedTrueCrop.top, roundedTrueCrop.width(), roundedTrueCrop.height());
                }
            }
            if (crop == null) {
                Log.w(TAG, "cannot decode default wallpaper");
                return null;
            }
            if (outWidth > 0 && outHeight > 0 && !(crop.getWidth() == outWidth && crop.getHeight() == outHeight)) {
                Matrix m = new Matrix();
                RectF cropRect = new RectF(TonemapCurve.LEVEL_BLACK, TonemapCurve.LEVEL_BLACK, (float) crop.getWidth(), (float) crop.getHeight());
                RectF rectF = new RectF(TonemapCurve.LEVEL_BLACK, TonemapCurve.LEVEL_BLACK, (float) outWidth, (float) outHeight);
                m.setRectToRect(cropRect, rectF, ScaleToFit.FILL);
                Bitmap tmp = Bitmap.createBitmap((int) rectF.width(), (int) rectF.height(), Config.ARGB_8888);
                if (tmp != null) {
                    Canvas c = new Canvas(tmp);
                    Paint p = new Paint();
                    p.setFilterBitmap(true);
                    c.drawBitmap(crop, m, p);
                    crop = tmp;
                }
            }
            return new BitmapDrawable(resources, crop);
        } else {
            throw new IllegalArgumentException("Must request exactly one kind of wallpaper");
        }
    }

    private static RectF getMaxCropRect(int inWidth, int inHeight, int outWidth, int outHeight, float horizontalAlignment, float verticalAlignment) {
        RectF cropRect = new RectF();
        if (((float) inWidth) / ((float) inHeight) > ((float) outWidth) / ((float) outHeight)) {
            cropRect.top = TonemapCurve.LEVEL_BLACK;
            cropRect.bottom = (float) inHeight;
            float cropWidth = ((float) outWidth) * (((float) inHeight) / ((float) outHeight));
            cropRect.left = (((float) inWidth) - cropWidth) * horizontalAlignment;
            cropRect.right = cropRect.left + cropWidth;
        } else {
            cropRect.left = TonemapCurve.LEVEL_BLACK;
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
            return new FastBitmapDrawable(bm, null);
        }
        return null;
    }

    public Drawable peekFastDrawable() {
        Bitmap bm = sGlobals.peekWallpaperBitmap(this.mContext, false, 1);
        if (bm != null) {
            return new FastBitmapDrawable(bm, null);
        }
        return null;
    }

    public Bitmap getBitmap() {
        return getBitmapAsUser(this.mContext.getUserId());
    }

    public Bitmap getBitmapAsUser(int userId) {
        return sGlobals.peekWallpaperBitmap(this.mContext, true, 1, userId);
    }

    public ParcelFileDescriptor getWallpaperFile(int which) {
        return getWallpaperFile(which, this.mContext.getUserId());
    }

    public ParcelFileDescriptor getWallpaperFile(int which, int userId) {
        if (which != 1 && which != 2) {
            throw new IllegalArgumentException("Must request exactly one kind of wallpaper");
        } else if (sGlobals.mService == null) {
            Log.w(TAG, "WallpaperService not running");
            throw new RuntimeException(new DeadSystemException());
        } else {
            try {
                return sGlobals.mService.getWallpaper(null, which, new Bundle(), userId);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public void forgetLoadedWallpaper() {
        sGlobals.forgetLoadedWallpaper();
    }

    public WallpaperInfo getWallpaperInfo() {
        try {
            if (sGlobals.mService != null) {
                return sGlobals.mService.getWallpaperInfo(UserHandle.myUserId());
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
            cropAndSetWallpaperIntent.setPackage(this.mContext.getString(17039473));
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
        Throwable th;
        if (sGlobals.mService == null) {
            Log.w(TAG, "WallpaperService not running");
            throw new RuntimeException(new DeadSystemException());
        }
        Bundle result = new Bundle();
        WallpaperSetCompletion completion = new WallpaperSetCompletion(this);
        try {
            Resources resources = this.mContext.getResources();
            ParcelFileDescriptor fd = sGlobals.mService.setWallpaper("res:" + resources.getResourceName(resid), this.mContext.getOpPackageName(), null, false, result, which, completion, UserHandle.myUserId());
            if (fd != null) {
                FileOutputStream fos = null;
                try {
                    FileOutputStream fos2 = new AutoCloseOutputStream(fd);
                    try {
                        copyStreamToWallpaperFile(resources.openRawResource(resid), fos2);
                        fos2.close();
                        completion.waitForCompletion();
                        IoUtils.closeQuietly(fos2);
                    } catch (Throwable th2) {
                        th = th2;
                        fos = fos2;
                        IoUtils.closeQuietly(fos);
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    IoUtils.closeQuietly(fos);
                    throw th;
                }
            }
            return result.getInt(EXTRA_NEW_WALLPAPER_ID, 0);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setBitmap(Bitmap bitmap) throws IOException {
        setBitmap(bitmap, null, true);
    }

    public int setBitmap(Bitmap fullImage, Rect visibleCropHint, boolean allowBackup) throws IOException {
        return setBitmap(fullImage, visibleCropHint, allowBackup, 3);
    }

    public int setBitmap(Bitmap fullImage, Rect visibleCropHint, boolean allowBackup, int which) throws IOException {
        return setBitmap(fullImage, visibleCropHint, allowBackup, which, UserHandle.myUserId());
    }

    public int setBitmap(Bitmap fullImage, Rect visibleCropHint, boolean allowBackup, int which, int userId) throws IOException {
        Throwable th;
        validateRect(visibleCropHint);
        if (sGlobals.mService == null) {
            Log.w(TAG, "WallpaperService not running");
            throw new RuntimeException(new DeadSystemException());
        }
        Bundle result = new Bundle();
        WallpaperSetCompletion completion = new WallpaperSetCompletion(this);
        try {
            ParcelFileDescriptor fd = sGlobals.mService.setWallpaper(null, this.mContext.getOpPackageName(), visibleCropHint, allowBackup, result, which, completion, userId);
            if (fd != null) {
                FileOutputStream fos = null;
                try {
                    FileOutputStream fos2 = new AutoCloseOutputStream(fd);
                    try {
                        fullImage.compress(CompressFormat.PNG, 90, fos2);
                        fos2.close();
                        completion.waitForCompletion();
                        IoUtils.closeQuietly(fos2);
                    } catch (Throwable th2) {
                        th = th2;
                        fos = fos2;
                        IoUtils.closeQuietly(fos);
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    IoUtils.closeQuietly(fos);
                    throw th;
                }
            }
            return result.getInt(EXTRA_NEW_WALLPAPER_ID, 0);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
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
        byte[] buffer = new byte[32768];
        while (true) {
            int amt = data.read(buffer);
            if (amt > 0) {
                fos.write(buffer, 0, amt);
            } else {
                return;
            }
        }
    }

    public int setStream(InputStream bitmapData, Rect visibleCropHint, boolean allowBackup) throws IOException {
        return setStream(bitmapData, visibleCropHint, allowBackup, 3);
    }

    public int setStream(InputStream bitmapData, Rect visibleCropHint, boolean allowBackup, int which) throws IOException {
        Throwable th;
        validateRect(visibleCropHint);
        if (sGlobals.mService == null) {
            Log.w(TAG, "WallpaperService not running");
            throw new RuntimeException(new DeadSystemException());
        }
        Bundle result = new Bundle();
        WallpaperSetCompletion completion = new WallpaperSetCompletion(this);
        try {
            ParcelFileDescriptor fd = sGlobals.mService.setWallpaper(null, this.mContext.getOpPackageName(), visibleCropHint, allowBackup, result, which, completion, UserHandle.myUserId());
            if (fd != null) {
                FileOutputStream fos = null;
                try {
                    FileOutputStream fos2 = new AutoCloseOutputStream(fd);
                    try {
                        copyStreamToWallpaperFile(bitmapData, fos2);
                        fos2.close();
                        completion.waitForCompletion();
                        IoUtils.closeQuietly(fos2);
                    } catch (Throwable th2) {
                        th = th2;
                        fos = fos2;
                        IoUtils.closeQuietly(fos);
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    IoUtils.closeQuietly(fos);
                    throw th;
                }
            }
            return result.getInt(EXTRA_NEW_WALLPAPER_ID, 0);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean hasResourceWallpaper(int resid) {
        if (sGlobals.mService == null) {
            Log.w(TAG, "WallpaperService not running");
            throw new RuntimeException(new DeadSystemException());
        }
        try {
            return sGlobals.mService.hasNamedWallpaper("res:" + this.mContext.getResources().getResourceName(resid));
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public int getDesiredMinimumWidth() {
        if (sGlobals.mService == null) {
            Log.w(TAG, "WallpaperService not running");
            throw new RuntimeException(new DeadSystemException());
        }
        try {
            return sGlobals.mService.getWidthHint();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public int getDesiredMinimumHeight() {
        if (sGlobals.mService == null) {
            Log.w(TAG, "WallpaperService not running");
            throw new RuntimeException(new DeadSystemException());
        }
        try {
            return sGlobals.mService.getHeightHint();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
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
                minimumHeight = (int) (((double) (((float) maximumTextureSize) * aspect)) + 0.5d);
            } else {
                minimumHeight = maximumTextureSize;
                minimumWidth = (int) (((double) (((float) maximumTextureSize) / aspect)) + 0.5d);
            }
        }
        try {
            if (sGlobals.mService == null) {
                Log.w(TAG, "WallpaperService not running");
                throw new RuntimeException(new DeadSystemException());
            } else {
                sGlobals.mService.setDimensionHints(minimumWidth, minimumHeight, this.mContext.getOpPackageName());
            }
        } catch (RemoteException e2) {
            throw e2.rethrowFromSystemServer();
        }
    }

    public void setDisplayPadding(Rect padding) {
        try {
            if (sGlobals.mService == null) {
                Log.w(TAG, "WallpaperService not running");
                throw new RuntimeException(new DeadSystemException());
            } else {
                sGlobals.mService.setDisplayPadding(padding, this.mContext.getOpPackageName());
            }
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

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

    public void clearWallpaper(int which, int userId) {
        if (sGlobals.mService == null) {
            Log.w(TAG, "WallpaperService not running");
            throw new RuntimeException(new DeadSystemException());
        }
        try {
            sGlobals.mService.clearWallpaper(this.mContext.getOpPackageName(), which, userId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean setWallpaperComponent(ComponentName name) {
        return setWallpaperComponent(name, UserHandle.myUserId());
    }

    public boolean setWallpaperComponent(ComponentName name, int userId) {
        if (sGlobals.mService == null) {
            Log.w(TAG, "WallpaperService not running");
            throw new RuntimeException(new DeadSystemException());
        }
        try {
            sGlobals.mService.setWallpaperComponentChecked(name, this.mContext.getOpPackageName(), userId);
            return true;
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
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
        if (sGlobals.mService == null) {
            Log.w(TAG, "WallpaperService not running");
            throw new RuntimeException(new DeadSystemException());
        }
        try {
            return sGlobals.mService.isWallpaperSupported(this.mContext.getOpPackageName());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean isSetWallpaperAllowed() {
        if (sGlobals.mService == null) {
            Log.w(TAG, "WallpaperService not running");
            throw new RuntimeException(new DeadSystemException());
        }
        try {
            return sGlobals.mService.isSetWallpaperAllowed(this.mContext.getOpPackageName());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
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
        setStream(sGlobals.openDefaultWallpaperRes(this.mContext, 1), null, false);
    }

    public void clear(int which) throws IOException {
        if ((which & 1) != 0) {
            clear();
        }
        if ((which & 2) != 0) {
            clearWallpaper(2, this.mContext.getUserId());
        }
    }

    @OppoHook(level = OppoHookType.CHANGE_RESOURCE, note = "gaoliang@Plf.Keyguard,2012.08.27:add to change the default wallpaper res", property = OppoRomType.ROM)
    public static InputStream openDefaultWallpaper(Context context, int which) {
        if (which == 2) {
            return null;
        }
        return context.getResources().openRawResource(getDefaultWallpaperResID(context));
    }

    public static int getDefaultWallpaperResID(Context context) {
        int wallpaperID = -1;
        String sysOperatorName = SystemProperties.get("ro.oppo.operator", "null");
        if (!"null".equals(sysOperatorName)) {
            wallpaperID = context.getResources().getIdentifier("oppo:drawable/" + ("oppo_default_wallpaper_" + sysOperatorName.toLowerCase()), null, null);
        }
        if (wallpaperID <= 0) {
            String wallpaperName;
            if ("CN".equalsIgnoreCase(SystemProperties.get("persist.sys.oppo.region", "CN"))) {
                wallpaperName = "oppo_default_wallpaper";
            } else {
                wallpaperName = "oppo_default_wallpaper_exp";
            }
            wallpaperID = context.getResources().getIdentifier("oppo:drawable/" + wallpaperName, null, null);
        }
        if (wallpaperID <= 0) {
            return 201850942;
        }
        return wallpaperID;
    }

    @OppoHook(level = OppoHookType.NEW_METHOD, note = "gaoliang@Plf.Keyguard,2012.08.27:add to bug191802", property = OppoRomType.ROM)
    static Bitmap generateBitmap(Context context, Bitmap bm) {
        if (bm == null) {
            Log.d(TAG, "generateBitmap return bm = null");
            return null;
        }
        int desiredWidth;
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getRealMetrics(displayMetrics);
        bm.setDensity(displayMetrics.noncompatDensityDpi);
        int maxDim = Math.max(displayMetrics.widthPixels, displayMetrics.heightPixels);
        int minDim = Math.min(displayMetrics.widthPixels, displayMetrics.heightPixels);
        int bmWidth = bm.getWidth();
        int bmHeight = bm.getHeight();
        int desiredHeight = maxDim;
        float ratio = 1.0f;
        if (bmHeight < maxDim) {
            ratio = (float) (maxDim / bmHeight);
        }
        if (((float) bmWidth) * ratio <= ((float) minDim)) {
            desiredWidth = minDim;
        } else {
            desiredWidth = minDim * 2;
        }
        Log.d(TAG, "generateBitmap desiredWidth = " + desiredWidth + " desiredHeight = " + maxDim);
        if (desiredWidth <= 0 || maxDim <= 0 || (bm.getWidth() == desiredWidth && bm.getHeight() == maxDim)) {
            return bm;
        }
        try {
            Bitmap newbm = Bitmap.createBitmap(desiredWidth, maxDim, Config.ARGB_8888);
            newbm.setDensity(displayMetrics.noncompatDensityDpi);
            Canvas c = new Canvas(newbm);
            Rect targetRect = new Rect();
            targetRect.right = bm.getWidth();
            targetRect.bottom = bm.getHeight();
            int deltaw = desiredWidth - targetRect.right;
            int deltah = maxDim - targetRect.bottom;
            if (deltaw > 0 || deltah > 0) {
                float scalew = ((float) desiredWidth) / ((float) targetRect.right);
                float scaleh = ((float) maxDim) / ((float) targetRect.bottom);
                float scale = scalew > scaleh ? scalew : scaleh;
                targetRect.right = (int) (((float) targetRect.right) * scale);
                targetRect.bottom = (int) (((float) targetRect.bottom) * scale);
                deltaw = desiredWidth - targetRect.right;
                deltah = maxDim - targetRect.bottom;
            }
            targetRect.offset(deltaw / 2, deltah / 2);
            Paint paint = new Paint();
            paint.setFilterBitmap(true);
            paint.setXfermode(new PorterDuffXfermode(Mode.SRC));
            c.drawBitmap(bm, null, targetRect, paint);
            bm.recycle();
            c.setBitmap(null);
            return newbm;
        } catch (Exception e) {
            Log.w(TAG, "Can't generate default bitmap", e);
            return bm;
        }
    }

    public static ComponentName getDefaultWallpaperComponent(Context context) {
        ComponentName cn;
        String flat = SystemProperties.get(PROP_WALLPAPER_COMPONENT);
        if (!TextUtils.isEmpty(flat)) {
            cn = ComponentName.unflattenFromString(flat);
            if (cn != null) {
                return cn;
            }
        }
        flat = context.getString(17039422);
        if (!TextUtils.isEmpty(flat)) {
            cn = ComponentName.unflattenFromString(flat);
            if (cn != null) {
                return cn;
            }
        }
        return null;
    }

    public boolean setLockWallpaperCallback(IWallpaperManagerCallback callback) {
        if (sGlobals.mService == null) {
            Log.w(TAG, "WallpaperService not running");
            throw new RuntimeException(new DeadSystemException());
        }
        try {
            return sGlobals.mService.setLockWallpaperCallback(callback);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean isWallpaperBackupEligible(int which) {
        if (sGlobals.mService == null) {
            Log.w(TAG, "WallpaperService not running");
            throw new RuntimeException(new DeadSystemException());
        }
        try {
            return sGlobals.mService.isWallpaperBackupEligible(which, this.mContext.getUserId());
        } catch (RemoteException e) {
            Log.e(TAG, "Exception querying wallpaper backup eligibility: " + e.getMessage());
            return false;
        }
    }
}
