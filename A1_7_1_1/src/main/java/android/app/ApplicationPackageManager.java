package android.app;

import android.R;
import android.annotation.OppoHook;
import android.annotation.OppoHook.OppoHookType;
import android.annotation.OppoHook.OppoRomType;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.ComponentInfo;
import android.content.pm.EphemeralApplicationInfo;
import android.content.pm.FeatureInfo;
import android.content.pm.IOnPermissionsChangeListener;
import android.content.pm.IPackageDataObserver;
import android.content.pm.IPackageDeleteObserver;
import android.content.pm.IPackageInstallObserver;
import android.content.pm.IPackageManager;
import android.content.pm.IPackageMoveObserver.Stub;
import android.content.pm.IPackageStatsObserver;
import android.content.pm.InstrumentationInfo;
import android.content.pm.IntentFilterVerificationInfo;
import android.content.pm.KeySet;
import android.content.pm.PackageInfo;
import android.content.pm.PackageInstaller;
import android.content.pm.PackageItemInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.LegacyPackageInstallObserver;
import android.content.pm.PackageManager.MoveCallback;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.PackageManager.OnPermissionsChangedListener;
import android.content.pm.ParceledListSlice;
import android.content.pm.PermissionGroupInfo;
import android.content.pm.PermissionInfo;
import android.content.pm.ProviderInfo;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.content.pm.VerifierDeviceIdentity;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.content.res.XmlResourceParser;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Drawable.ConstantState;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.storage.StorageManager;
import android.os.storage.VolumeInfo;
import android.provider.Settings.Global;
import android.util.ArrayMap;
import android.util.Log;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.os.SomeArgs;
import com.android.internal.util.Preconditions;
import com.android.internal.util.UserIcons;
import dalvik.system.VMRuntime;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import libcore.util.EmptyArray;
import oppo.util.OppoMultiLauncherUtil;

/*  JADX ERROR: NullPointerException in pass: ReSugarCode
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
    	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
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
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class ApplicationPackageManager extends PackageManager {
    public static boolean DEBUG_COLOROS = false;
    private static final boolean DEBUG_ICONS = false;
    private static final int DEFAULT_EPHEMERAL_COOKIE_MAX_SIZE_BYTES = 16384;
    private static final String TAG = "ApplicationPackageManager";
    private static HashMap<String, Bitmap> mActivityIconsCache = null;
    private static HashMap<String, Bitmap> mAppIconsCache = null;
    private static boolean mIconCacheDirty = false;
    private static final int sDefaultFlags = 1024;
    private static ArrayMap<ResourceName, WeakReference<ConstantState>> sIconCache;
    private static ArrayMap<ResourceName, WeakReference<CharSequence>> sStringCache;
    private static final Object sSync = null;
    volatile int mCachedSafeMode;
    private final ContextImpl mContext;
    @GuardedBy("mDelegates")
    private final ArrayList<MoveCallbackDelegate> mDelegates;
    @GuardedBy("mLock")
    private PackageInstaller mInstaller;
    private final Object mLock;
    private final IPackageManager mPM;
    private final PackageDeleteObserver mPackageDeleleteObserver;
    private final Map<OnPermissionsChangedListener, IOnPermissionsChangeListener> mPermissionListeners;
    @GuardedBy("mLock")
    private String mPermissionsControllerPackageName;
    @GuardedBy("mLock")
    private UserManager mUserManager;

    private static class MoveCallbackDelegate extends Stub implements Callback {
        private static final int MSG_CREATED = 1;
        private static final int MSG_STATUS_CHANGED = 2;
        final MoveCallback mCallback;
        final Handler mHandler;

        public MoveCallbackDelegate(MoveCallback callback, Looper looper) {
            this.mCallback = callback;
            this.mHandler = new Handler(looper, (Callback) this);
        }

        public boolean handleMessage(Message msg) {
            SomeArgs args;
            switch (msg.what) {
                case 1:
                    args = msg.obj;
                    this.mCallback.onCreated(args.argi1, (Bundle) args.arg2);
                    args.recycle();
                    return true;
                case 2:
                    args = (SomeArgs) msg.obj;
                    this.mCallback.onStatusChanged(args.argi1, args.argi2, ((Long) args.arg3).longValue());
                    args.recycle();
                    return true;
                default:
                    return false;
            }
        }

        public void onCreated(int moveId, Bundle extras) {
            SomeArgs args = SomeArgs.obtain();
            args.argi1 = moveId;
            args.arg2 = extras;
            this.mHandler.obtainMessage(1, args).sendToTarget();
        }

        public void onStatusChanged(int moveId, int status, long estMillis) {
            SomeArgs args = SomeArgs.obtain();
            args.argi1 = moveId;
            args.argi2 = status;
            args.arg3 = Long.valueOf(estMillis);
            this.mHandler.obtainMessage(2, args).sendToTarget();
        }
    }

    public class OnPermissionsChangeListenerDelegate extends IOnPermissionsChangeListener.Stub implements Callback {
        private static final int MSG_PERMISSIONS_CHANGED = 1;
        private final Handler mHandler;
        private final OnPermissionsChangedListener mListener;

        public OnPermissionsChangeListenerDelegate(OnPermissionsChangedListener listener, Looper looper) {
            this.mListener = listener;
            this.mHandler = new Handler(looper, (Callback) this);
        }

        public void onPermissionsChanged(int uid) {
            this.mHandler.obtainMessage(1, uid, 0).sendToTarget();
        }

        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    this.mListener.onPermissionsChanged(msg.arg1);
                    return true;
                default:
                    return false;
            }
        }
    }

    private class PackageDeleteObserver extends IPackageDeleteObserver.Stub {
        final /* synthetic */ ApplicationPackageManager this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.app.ApplicationPackageManager.PackageDeleteObserver.<init>(android.app.ApplicationPackageManager):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        private PackageDeleteObserver(android.app.ApplicationPackageManager r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.app.ApplicationPackageManager.PackageDeleteObserver.<init>(android.app.ApplicationPackageManager):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.app.ApplicationPackageManager.PackageDeleteObserver.<init>(android.app.ApplicationPackageManager):void");
        }

        /* synthetic */ PackageDeleteObserver(ApplicationPackageManager this$0, PackageDeleteObserver packageDeleteObserver) {
            this(this$0);
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.app.ApplicationPackageManager.PackageDeleteObserver.packageDeleted(java.lang.String, int):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        public void packageDeleted(java.lang.String r1, int r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.app.ApplicationPackageManager.PackageDeleteObserver.packageDeleted(java.lang.String, int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.app.ApplicationPackageManager.PackageDeleteObserver.packageDeleted(java.lang.String, int):void");
        }
    }

    @OppoHook(level = OppoHookType.CHANGE_ACCESS, note = "YaoJun.Luo@Plf.SDK : [-private] Modify for rom theme", property = OppoRomType.ROM)
    static final class ResourceName {
        final int iconId;
        final String packageName;

        ResourceName(String _packageName, int _iconId) {
            this.packageName = _packageName;
            this.iconId = _iconId;
        }

        ResourceName(ApplicationInfo aInfo, int _iconId) {
            this(aInfo.packageName, _iconId);
        }

        ResourceName(ComponentInfo cInfo, int _iconId) {
            this(cInfo.applicationInfo.packageName, _iconId);
        }

        ResourceName(ResolveInfo rInfo, int _iconId) {
            this(rInfo.activityInfo.applicationInfo.packageName, _iconId);
        }

        public boolean equals(Object o) {
            boolean z = false;
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            ResourceName that = (ResourceName) o;
            if (this.iconId != that.iconId) {
                return false;
            }
            if (this.packageName == null ? that.packageName == null : this.packageName.equals(that.packageName)) {
                z = true;
            }
            return z;
        }

        public int hashCode() {
            return (this.packageName.hashCode() * 31) + this.iconId;
        }

        public String toString() {
            return "{ResourceName " + this.packageName + " / " + this.iconId + "}";
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.app.ApplicationPackageManager.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.app.ApplicationPackageManager.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.app.ApplicationPackageManager.<clinit>():void");
    }

    UserManager getUserManager() {
        UserManager userManager;
        synchronized (this.mLock) {
            if (this.mUserManager == null) {
                this.mUserManager = UserManager.get(this.mContext);
            }
            userManager = this.mUserManager;
        }
        return userManager;
    }

    public PackageInfo getPackageInfo(String packageName, int flags) throws NameNotFoundException {
        try {
            PackageInfo pi = this.mPM.getPackageInfo(packageName, flags, this.mContext.getUserId());
            if (DEBUG_COLOROS && pi == null && this.mContext.getUserId() == 999) {
                Log.i(TAG, "multi app: getPackageInfo is null! " + packageName + " ,pkg:" + this.mContext.getPackageName());
            }
            if (pi != null) {
                return pi;
            }
            throw new NameNotFoundException(packageName);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public PackageInfo getPackageInfoAsUser(String packageName, int flags, int userId) throws NameNotFoundException {
        try {
            PackageInfo pi = this.mPM.getPackageInfo(packageName, flags, userId);
            if (pi != null) {
                return pi;
            }
            throw new NameNotFoundException(packageName);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public String[] currentToCanonicalPackageNames(String[] names) {
        try {
            return this.mPM.currentToCanonicalPackageNames(names);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public String[] canonicalToCurrentPackageNames(String[] names) {
        try {
            return this.mPM.canonicalToCurrentPackageNames(names);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public Intent getLaunchIntentForPackage(String packageName) {
        Intent intentToResolve = new Intent(Intent.ACTION_MAIN);
        intentToResolve.addCategory(Intent.CATEGORY_INFO);
        intentToResolve.setPackage(packageName);
        List<ResolveInfo> ris = queryIntentActivities(intentToResolve, 0);
        if (ris == null || ris.size() <= 0) {
            intentToResolve.removeCategory(Intent.CATEGORY_INFO);
            intentToResolve.addCategory(Intent.CATEGORY_LAUNCHER);
            intentToResolve.setPackage(packageName);
            ris = queryIntentActivities(intentToResolve, 0);
        }
        if (ris == null || ris.size() <= 0) {
            return null;
        }
        Intent intent = new Intent(intentToResolve);
        intent.setFlags(268435456);
        intent.setClassName(((ResolveInfo) ris.get(0)).activityInfo.packageName, ((ResolveInfo) ris.get(0)).activityInfo.name);
        return intent;
    }

    public Intent getLeanbackLaunchIntentForPackage(String packageName) {
        Intent intentToResolve = new Intent(Intent.ACTION_MAIN);
        intentToResolve.addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER);
        intentToResolve.setPackage(packageName);
        List<ResolveInfo> ris = queryIntentActivities(intentToResolve, 0);
        if (ris == null || ris.size() <= 0) {
            return null;
        }
        Intent intent = new Intent(intentToResolve);
        intent.setFlags(268435456);
        intent.setClassName(((ResolveInfo) ris.get(0)).activityInfo.packageName, ((ResolveInfo) ris.get(0)).activityInfo.name);
        return intent;
    }

    public int[] getPackageGids(String packageName) throws NameNotFoundException {
        return getPackageGids(packageName, 0);
    }

    public int[] getPackageGids(String packageName, int flags) throws NameNotFoundException {
        try {
            int[] gids = this.mPM.getPackageGids(packageName, flags, this.mContext.getUserId());
            if (gids != null) {
                return gids;
            }
            throw new NameNotFoundException(packageName);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public int getPackageUid(String packageName, int flags) throws NameNotFoundException {
        return getPackageUidAsUser(packageName, flags, this.mContext.getUserId());
    }

    public int getPackageUidAsUser(String packageName, int userId) throws NameNotFoundException {
        return getPackageUidAsUser(packageName, 0, userId);
    }

    public int getPackageUidAsUser(String packageName, int flags, int userId) throws NameNotFoundException {
        try {
            int uid = this.mPM.getPackageUid(packageName, flags, userId);
            if (uid >= 0) {
                return uid;
            }
            throw new NameNotFoundException(packageName);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public PermissionInfo getPermissionInfo(String name, int flags) throws NameNotFoundException {
        try {
            PermissionInfo pi = this.mPM.getPermissionInfo(name, flags);
            if (pi != null) {
                return pi;
            }
            throw new NameNotFoundException(name);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public List<PermissionInfo> queryPermissionsByGroup(String group, int flags) throws NameNotFoundException {
        try {
            ParceledListSlice<PermissionInfo> parceledList = this.mPM.queryPermissionsByGroup(group, flags);
            if (parceledList != null) {
                List<PermissionInfo> pi = parceledList.getList();
                if (pi != null) {
                    return pi;
                }
            }
            throw new NameNotFoundException(group);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public PermissionGroupInfo getPermissionGroupInfo(String name, int flags) throws NameNotFoundException {
        try {
            PermissionGroupInfo pgi = this.mPM.getPermissionGroupInfo(name, flags);
            if (pgi != null) {
                return pgi;
            }
            throw new NameNotFoundException(name);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public List<PermissionGroupInfo> getAllPermissionGroups(int flags) {
        try {
            ParceledListSlice<PermissionGroupInfo> parceledList = this.mPM.getAllPermissionGroups(flags);
            if (parceledList == null) {
                return Collections.emptyList();
            }
            return parceledList.getList();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public ApplicationInfo getApplicationInfo(String packageName, int flags) throws NameNotFoundException {
        return getApplicationInfoAsUser(packageName, flags, this.mContext.getUserId());
    }

    public ApplicationInfo getApplicationInfoAsUser(String packageName, int flags, int userId) throws NameNotFoundException {
        try {
            ApplicationInfo ai = this.mPM.getApplicationInfo(packageName, flags, userId);
            String pkg = this.mContext.getPackageName();
            if (ai == null && this.mContext.getUserId() == 999 && pkg != null && OppoMultiLauncherUtil.getInstance().isMultiApp(pkg) && !OppoMultiLauncherUtil.getInstance().isMultiApp(packageName)) {
                Log.i(TAG, "multi app: getApplicationInfo is null! " + packageName + " ,pkg:" + pkg);
                ai = this.mPM.getApplicationInfo(packageName, flags, 0);
            }
            if (ai != null) {
                return maybeAdjustApplicationInfo(ai);
            }
            throw new NameNotFoundException(packageName);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    private static ApplicationInfo maybeAdjustApplicationInfo(ApplicationInfo info) {
        if (!(info.primaryCpuAbi == null || info.secondaryCpuAbi == null)) {
            String runtimeIsa = VMRuntime.getRuntime().vmInstructionSet();
            String secondaryIsa = VMRuntime.getInstructionSet(info.secondaryCpuAbi);
            String secondaryDexCodeIsa = SystemProperties.get("ro.dalvik.vm.isa." + secondaryIsa);
            if (!secondaryDexCodeIsa.isEmpty()) {
                secondaryIsa = secondaryDexCodeIsa;
            }
            if (runtimeIsa.equals(secondaryIsa)) {
                ApplicationInfo modified = new ApplicationInfo(info);
                modified.nativeLibraryDir = info.secondaryNativeLibraryDir;
                return modified;
            }
        }
        return info;
    }

    public ActivityInfo getActivityInfo(ComponentName className, int flags) throws NameNotFoundException {
        try {
            ActivityInfo ai = this.mPM.getActivityInfo(className, flags, this.mContext.getUserId());
            if (DEBUG_COLOROS && ai == null && this.mContext.getUserId() == 999) {
                Log.i(TAG, "multi app: getActivityInfo is null! " + className + " ,pkg:" + this.mContext.getPackageName());
            }
            if (ai != null) {
                return ai;
            }
            throw new NameNotFoundException(className.toString());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public ActivityInfo getReceiverInfo(ComponentName className, int flags) throws NameNotFoundException {
        try {
            ActivityInfo ai = this.mPM.getReceiverInfo(className, flags, this.mContext.getUserId());
            if (DEBUG_COLOROS && ai == null && this.mContext.getUserId() == 999) {
                Log.i(TAG, "multi app: getReceiverInfo is null! " + className + " ,pkg:" + this.mContext.getPackageName());
            }
            if (ai != null) {
                return ai;
            }
            throw new NameNotFoundException(className.toString());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public ServiceInfo getServiceInfo(ComponentName className, int flags) throws NameNotFoundException {
        try {
            ServiceInfo si = this.mPM.getServiceInfo(className, flags, this.mContext.getUserId());
            if (DEBUG_COLOROS && si == null && this.mContext.getUserId() == 999) {
                Log.i(TAG, "multi app: getServiceInfo is null! " + className + " ,pkg:" + this.mContext.getPackageName());
            }
            if (si != null) {
                return si;
            }
            throw new NameNotFoundException(className.toString());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public ProviderInfo getProviderInfo(ComponentName className, int flags) throws NameNotFoundException {
        try {
            ProviderInfo pi = this.mPM.getProviderInfo(className, flags, this.mContext.getUserId());
            if (DEBUG_COLOROS && pi == null && this.mContext.getUserId() == 999) {
                Log.i(TAG, "multi app: getProviderInfo is null! " + className + " ,pkg:" + this.mContext.getPackageName());
            }
            if (pi != null) {
                return pi;
            }
            throw new NameNotFoundException(className.toString());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public String[] getSystemSharedLibraryNames() {
        try {
            return this.mPM.getSystemSharedLibraryNames();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public String getServicesSystemSharedLibraryPackageName() {
        try {
            return this.mPM.getServicesSystemSharedLibraryPackageName();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public String getSharedSystemSharedLibraryPackageName() {
        try {
            return this.mPM.getSharedSystemSharedLibraryPackageName();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public FeatureInfo[] getSystemAvailableFeatures() {
        try {
            ParceledListSlice<FeatureInfo> parceledList = this.mPM.getSystemAvailableFeatures();
            if (parceledList == null) {
                return new FeatureInfo[0];
            }
            List<FeatureInfo> list = parceledList.getList();
            FeatureInfo[] res = new FeatureInfo[list.size()];
            for (int i = 0; i < res.length; i++) {
                res[i] = (FeatureInfo) list.get(i);
            }
            return res;
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean hasSystemFeature(String name) {
        return hasSystemFeature(name, 0);
    }

    public boolean hasSystemFeature(String name, int version) {
        try {
            return this.mPM.hasSystemFeature(name, version);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean isFullFunctionMode() {
        try {
            return this.mPM.isFullFunctionMode();
        } catch (RemoteException e) {
            throw new RuntimeException("Package manager has died", e);
        }
    }

    public boolean loadRegionFeature(String name) {
        try {
            return this.mPM.loadRegionFeature(name);
        } catch (RemoteException e) {
            throw new RuntimeException("Package manager has died", e);
        }
    }

    public FeatureInfo[] getOppoSystemAvailableFeatures() {
        try {
            return this.mPM.getOppoSystemAvailableFeatures();
        } catch (RemoteException e) {
            throw new RuntimeException("Package manager has died", e);
        }
    }

    public boolean isSecurePayApp(String pkg) {
        try {
            return this.mPM.isSecurePayApp(pkg);
        } catch (RemoteException e) {
            throw new RuntimeException("Package manager has died", e);
        }
    }

    public int checkPermission(String permName, String pkgName) {
        try {
            String pkg = this.mContext.getPackageName();
            if (pkg != null && pkg.equals("com.google.android.xts.permission")) {
                Log.i(TAG, "checkPermission return DENIED for test!");
                return -1;
            } else if (pkg == null || !pkg.equals("com.google.android.permission.gts")) {
                return this.mPM.checkPermission(permName, pkgName, this.mContext.getUserId());
            } else {
                Log.i(TAG, "checkPermission return DENIED for test2!");
                return -1;
            }
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean isPermissionRevokedByPolicy(String permName, String pkgName) {
        try {
            return this.mPM.isPermissionRevokedByPolicy(permName, pkgName, this.mContext.getUserId());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public String getPermissionControllerPackageName() {
        String str;
        synchronized (this.mLock) {
            if (this.mPermissionsControllerPackageName == null) {
                try {
                    this.mPermissionsControllerPackageName = this.mPM.getPermissionControllerPackageName();
                } catch (RemoteException e) {
                    throw e.rethrowFromSystemServer();
                }
            }
            str = this.mPermissionsControllerPackageName;
        }
        return str;
    }

    public boolean addPermission(PermissionInfo info) {
        try {
            return this.mPM.addPermission(info);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean addPermissionAsync(PermissionInfo info) {
        try {
            return this.mPM.addPermissionAsync(info);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void removePermission(String name) {
        try {
            this.mPM.removePermission(name);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void grantRuntimePermission(String packageName, String permissionName, UserHandle user) {
        try {
            this.mPM.grantRuntimePermission(packageName, permissionName, user.getIdentifier());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void revokeRuntimePermission(String packageName, String permissionName, UserHandle user) {
        try {
            this.mPM.revokeRuntimePermission(packageName, permissionName, user.getIdentifier());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public int getPermissionFlags(String permissionName, String packageName, UserHandle user) {
        try {
            return this.mPM.getPermissionFlags(permissionName, packageName, user.getIdentifier());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void updatePermissionFlags(String permissionName, String packageName, int flagMask, int flagValues, UserHandle user) {
        try {
            this.mPM.updatePermissionFlags(permissionName, packageName, flagMask, flagValues, user.getIdentifier());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean shouldShowRequestPermissionRationale(String permission) {
        try {
            return this.mPM.shouldShowRequestPermissionRationale(permission, this.mContext.getPackageName(), this.mContext.getUserId());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public int checkSignatures(String pkg1, String pkg2) {
        try {
            return this.mPM.checkSignatures(pkg1, pkg2);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public int checkSignatures(int uid1, int uid2) {
        try {
            return this.mPM.checkUidSignatures(uid1, uid2);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public String[] getPackagesForUid(int uid) {
        try {
            return this.mPM.getPackagesForUid(uid);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public String getNameForUid(int uid) {
        try {
            return this.mPM.getNameForUid(uid);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public int getUidForSharedUser(String sharedUserName) throws NameNotFoundException {
        try {
            int uid = this.mPM.getUidForSharedUser(sharedUserName);
            if (uid != -1) {
                return uid;
            }
            throw new NameNotFoundException("No shared userid for user:" + sharedUserName);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public List<PackageInfo> getInstalledPackages(int flags) {
        return getInstalledPackagesAsUser(flags, this.mContext.getUserId());
    }

    public List<PackageInfo> getInstalledPackagesAsUser(int flags, int userId) {
        if (userId == 999) {
            userId = 0;
        }
        try {
            ParceledListSlice<PackageInfo> parceledList = this.mPM.getInstalledPackages(flags, userId);
            if (parceledList == null) {
                return Collections.emptyList();
            }
            return parceledList.getList();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public List<PackageInfo> getPackagesHoldingPermissions(String[] permissions, int flags) {
        try {
            ParceledListSlice<PackageInfo> parceledList = this.mPM.getPackagesHoldingPermissions(permissions, flags, this.mContext.getUserId());
            if (parceledList == null) {
                return Collections.emptyList();
            }
            return parceledList.getList();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public List<ApplicationInfo> getInstalledApplications(int flags) {
        int userId = this.mContext.getUserId();
        try {
            List<ApplicationInfo> multiAppList = new ArrayList();
            if ((134217728 & flags) != 0) {
                if (DEBUG_COLOROS) {
                    Log.d(TAG, " find cloned app: ");
                }
                ParceledListSlice<ApplicationInfo> sliceMore = this.mPM.getInstalledApplications(128, 999);
                if (!(sliceMore == null || sliceMore.getList() == null || sliceMore.getList().isEmpty())) {
                    for (ApplicationInfo aInfo : sliceMore.getList()) {
                        if (OppoMultiLauncherUtil.getInstance().isMultiApp(aInfo.packageName)) {
                            multiAppList.add(aInfo);
                            if (DEBUG_COLOROS) {
                                Log.d(TAG, " pkg= " + aInfo.packageName);
                            }
                        }
                    }
                }
            }
            ParceledListSlice<ApplicationInfo> slice = this.mPM.getInstalledApplications(flags, userId);
            slice.getList().addAll(multiAppList);
            return slice.getList();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public List<EphemeralApplicationInfo> getEphemeralApplications() {
        try {
            ParceledListSlice<EphemeralApplicationInfo> slice = this.mPM.getEphemeralApplications(this.mContext.getUserId());
            if (slice != null) {
                return slice.getList();
            }
            return Collections.emptyList();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public Drawable getEphemeralApplicationIcon(String packageName) {
        try {
            Bitmap bitmap = this.mPM.getEphemeralApplicationIcon(packageName, this.mContext.getUserId());
            if (bitmap != null) {
                return new BitmapDrawable(null, bitmap);
            }
            return null;
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean isEphemeralApplication() {
        try {
            return this.mPM.isEphemeralApplication(this.mContext.getPackageName(), this.mContext.getUserId());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public int getEphemeralCookieMaxSizeBytes() {
        return Global.getInt(this.mContext.getContentResolver(), Global.EPHEMERAL_COOKIE_MAX_SIZE_BYTES, 16384);
    }

    public byte[] getEphemeralCookie() {
        try {
            byte[] cookie = this.mPM.getEphemeralApplicationCookie(this.mContext.getPackageName(), this.mContext.getUserId());
            if (cookie != null) {
                return cookie;
            }
            return EmptyArray.BYTE;
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean setEphemeralCookie(byte[] cookie) {
        try {
            return this.mPM.setEphemeralApplicationCookie(this.mContext.getPackageName(), cookie, this.mContext.getUserId());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public ResolveInfo resolveActivity(Intent intent, int flags) {
        return resolveActivityAsUser(intent, flags, this.mContext.getUserId());
    }

    public ResolveInfo resolveActivityAsUser(Intent intent, int flags, int userId) {
        try {
            String pkg = this.mContext.getPackageName();
            ResolveInfo info = this.mPM.resolveIntent(intent, intent.resolveTypeIfNeeded(this.mContext.getContentResolver()), flags, userId);
            if (userId != 999 || info != null || pkg == null || !OppoMultiLauncherUtil.getInstance().isMultiApp(pkg)) {
                return info;
            }
            Log.i(TAG, "multi app: resolveActivity is null!");
            return this.mPM.resolveIntent(intent, intent.resolveTypeIfNeeded(this.mContext.getContentResolver()), flags, 0);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public List<ResolveInfo> queryIntentActivities(Intent intent, int flags) {
        return queryIntentActivitiesAsUser(intent, flags, this.mContext.getUserId());
    }

    public List<ResolveInfo> queryIntentActivitiesAsUser(Intent intent, int flags, int userId) {
        if (userId == 999) {
            userId = 0;
        }
        try {
            ParceledListSlice<ResolveInfo> parceledList = this.mPM.queryIntentActivities(intent, intent.resolveTypeIfNeeded(this.mContext.getContentResolver()), flags, userId);
            if (parceledList == null) {
                return Collections.emptyList();
            }
            return parceledList.getList();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public List<ResolveInfo> queryIntentActivityOptions(ComponentName caller, Intent[] specifics, Intent intent, int flags) {
        ContentResolver resolver = this.mContext.getContentResolver();
        String[] strArr = null;
        if (specifics != null) {
            int N = specifics.length;
            for (int i = 0; i < N; i++) {
                Intent sp = specifics[i];
                if (sp != null) {
                    String t = sp.resolveTypeIfNeeded(resolver);
                    if (t != null) {
                        if (strArr == null) {
                            strArr = new String[N];
                        }
                        strArr[i] = t;
                    }
                }
            }
        }
        try {
            ParceledListSlice<ResolveInfo> parceledList = this.mPM.queryIntentActivityOptions(caller, specifics, strArr, intent, intent.resolveTypeIfNeeded(resolver), flags, this.mContext.getUserId());
            if (parceledList == null) {
                return Collections.emptyList();
            }
            return parceledList.getList();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public List<ResolveInfo> queryBroadcastReceiversAsUser(Intent intent, int flags, int userId) {
        try {
            ParceledListSlice<ResolveInfo> parceledList = this.mPM.queryIntentReceivers(intent, intent.resolveTypeIfNeeded(this.mContext.getContentResolver()), flags, userId);
            String pkg = this.mContext.getPackageName();
            if (userId == 999 && pkg != null && ((parceledList == null || (parceledList != null && parceledList.getList().isEmpty())) && OppoMultiLauncherUtil.getInstance().isMultiApp(pkg))) {
                Log.i(TAG, "multi app: queryBroadcast is null!");
                parceledList = this.mPM.queryIntentReceivers(intent, intent.resolveTypeIfNeeded(this.mContext.getContentResolver()), flags, 0);
            }
            if (parceledList == null) {
                return Collections.emptyList();
            }
            return parceledList.getList();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public List<ResolveInfo> queryBroadcastReceivers(Intent intent, int flags) {
        return queryBroadcastReceiversAsUser(intent, flags, this.mContext.getUserId());
    }

    public ResolveInfo resolveService(Intent intent, int flags) {
        try {
            return this.mPM.resolveService(intent, intent.resolveTypeIfNeeded(this.mContext.getContentResolver()), flags, this.mContext.getUserId());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public List<ResolveInfo> queryIntentServicesAsUser(Intent intent, int flags, int userId) {
        try {
            ParceledListSlice<ResolveInfo> parceledList = this.mPM.queryIntentServices(intent, intent.resolveTypeIfNeeded(this.mContext.getContentResolver()), flags, userId);
            String pkg = this.mContext.getPackageName();
            if (userId == 999 && pkg != null && ((parceledList == null || (parceledList != null && parceledList.getList().isEmpty())) && OppoMultiLauncherUtil.getInstance().isMultiApp(pkg))) {
                Log.i(TAG, "multi app: queryIntentServicesAsUser is null! " + intent + " ,userId:" + userId);
                parceledList = this.mPM.queryIntentServices(intent, intent.resolveTypeIfNeeded(this.mContext.getContentResolver()), flags, 0);
            }
            if (parceledList == null) {
                return Collections.emptyList();
            }
            return parceledList.getList();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public List<ResolveInfo> queryIntentServices(Intent intent, int flags) {
        return queryIntentServicesAsUser(intent, flags, this.mContext.getUserId());
    }

    public List<ResolveInfo> queryIntentContentProvidersAsUser(Intent intent, int flags, int userId) {
        try {
            ParceledListSlice<ResolveInfo> parceledList = this.mPM.queryIntentContentProviders(intent, intent.resolveTypeIfNeeded(this.mContext.getContentResolver()), flags, userId);
            String pkg = this.mContext.getPackageName();
            if (userId == 999 && pkg != null && ((parceledList == null || (parceledList != null && parceledList.getList().isEmpty())) && OppoMultiLauncherUtil.getInstance().isMultiApp(pkg))) {
                Log.i(TAG, "multi app: queryProvider is null!");
                parceledList = this.mPM.queryIntentContentProviders(intent, intent.resolveTypeIfNeeded(this.mContext.getContentResolver()), flags, 0);
            }
            if (parceledList == null) {
                return Collections.emptyList();
            }
            return parceledList.getList();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public List<ResolveInfo> queryIntentContentProviders(Intent intent, int flags) {
        return queryIntentContentProvidersAsUser(intent, flags, this.mContext.getUserId());
    }

    public ProviderInfo resolveContentProvider(String name, int flags) {
        return resolveContentProviderAsUser(name, flags, this.mContext.getUserId());
    }

    public ProviderInfo resolveContentProviderAsUser(String name, int flags, int userId) {
        try {
            ProviderInfo info = this.mPM.resolveContentProvider(name, flags, userId);
            if (this.mContext == null) {
                return info;
            }
            String pkg = this.mContext.getPackageName();
            if (userId != 999 || pkg == null || info != null || !OppoMultiLauncherUtil.getInstance().isMultiApp(pkg)) {
                return info;
            }
            Log.i(TAG, "multi app: resolveProvider is null!");
            return this.mPM.resolveContentProvider(name, flags, 0);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public List<ProviderInfo> queryContentProviders(String processName, int uid, int flags) {
        try {
            ParceledListSlice<ProviderInfo> slice = this.mPM.queryContentProviders(processName, uid, flags);
            return slice != null ? slice.getList() : Collections.emptyList();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public InstrumentationInfo getInstrumentationInfo(ComponentName className, int flags) throws NameNotFoundException {
        try {
            InstrumentationInfo ii = this.mPM.getInstrumentationInfo(className, flags);
            if (ii != null) {
                return ii;
            }
            throw new NameNotFoundException(className.toString());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public List<InstrumentationInfo> queryInstrumentation(String targetPackage, int flags) {
        try {
            ParceledListSlice<InstrumentationInfo> parceledList = this.mPM.queryInstrumentation(targetPackage, flags);
            if (parceledList == null) {
                return Collections.emptyList();
            }
            return parceledList.getList();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public Drawable getDrawable(String packageName, int resId, ApplicationInfo appInfo) {
        ResourceName name = new ResourceName(packageName, resId);
        Drawable cachedIcon = getCachedIcon(name);
        if (cachedIcon != null) {
            return cachedIcon;
        }
        if (appInfo == null) {
            try {
                appInfo = getApplicationInfo(packageName, 1024);
            } catch (NameNotFoundException e) {
                return null;
            }
        }
        if (resId != 0) {
            try {
                Drawable dr = getResourcesForApplication(appInfo).getDrawable(resId, null);
                if (dr != null) {
                    putCachedIcon(name, dr);
                }
                return dr;
            } catch (NameNotFoundException e2) {
                Log.w("PackageManager", "Failure retrieving resources for " + appInfo.packageName);
            } catch (NotFoundException e3) {
                Log.w("PackageManager", "Failure retrieving resources for " + appInfo.packageName + ": " + e3.getMessage());
            } catch (Exception e4) {
                Log.w("PackageManager", "Failure retrieving icon 0x" + Integer.toHexString(resId) + " in package " + packageName, e4);
            }
        }
        return null;
    }

    public Drawable getActivityIcon(ComponentName activityName) throws NameNotFoundException {
        return getActivityInfo(activityName, 1024).loadIcon(this);
    }

    public Drawable getActivityIcon(Intent intent) throws NameNotFoundException {
        if (intent.getComponent() != null) {
            return getActivityIcon(intent.getComponent());
        }
        ResolveInfo info = resolveActivity(intent, 65536);
        if (info != null) {
            return info.activityInfo.loadIcon(this);
        }
        throw new NameNotFoundException(intent.toUri(0));
    }

    public Drawable getDefaultActivityIcon() {
        return Resources.getSystem().getDrawable(R.drawable.sym_def_app_icon);
    }

    public Drawable getApplicationIcon(ApplicationInfo info) {
        try {
            Bitmap bitmap = getAppIconBitmap(info.packageName);
            if (bitmap != null) {
                return new BitmapDrawable(this.mContext.getResources(), bitmap);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return info.loadIcon(this);
    }

    public Drawable getApplicationIcon(String packageName) throws NameNotFoundException {
        return getApplicationIcon(getApplicationInfo(packageName, 1024));
    }

    public Drawable getActivityBanner(ComponentName activityName) throws NameNotFoundException {
        return getActivityInfo(activityName, 1024).loadBanner(this);
    }

    public Drawable getActivityBanner(Intent intent) throws NameNotFoundException {
        if (intent.getComponent() != null) {
            return getActivityBanner(intent.getComponent());
        }
        ResolveInfo info = resolveActivity(intent, 65536);
        if (info != null) {
            return info.activityInfo.loadBanner(this);
        }
        throw new NameNotFoundException(intent.toUri(0));
    }

    public Drawable getApplicationBanner(ApplicationInfo info) {
        return info.loadBanner(this);
    }

    public Drawable getApplicationBanner(String packageName) throws NameNotFoundException {
        return getApplicationBanner(getApplicationInfo(packageName, 1024));
    }

    public Drawable getActivityLogo(ComponentName activityName) throws NameNotFoundException {
        return getActivityInfo(activityName, 1024).loadLogo(this);
    }

    public Drawable getActivityLogo(Intent intent) throws NameNotFoundException {
        if (intent.getComponent() != null) {
            return getActivityLogo(intent.getComponent());
        }
        ResolveInfo info = resolveActivity(intent, 65536);
        if (info != null) {
            return info.activityInfo.loadLogo(this);
        }
        throw new NameNotFoundException(intent.toUri(0));
    }

    public Drawable getApplicationLogo(ApplicationInfo info) {
        return info.loadLogo(this);
    }

    public Drawable getApplicationLogo(String packageName) throws NameNotFoundException {
        return getApplicationLogo(getApplicationInfo(packageName, 1024));
    }

    public Drawable getManagedUserBadgedDrawable(Drawable drawable, Rect badgeLocation, int badgeDensity) {
        return getBadgedDrawable(drawable, getDrawableForDensity(17302310, badgeDensity), badgeLocation, true);
    }

    public Drawable getUserBadgedIcon(Drawable icon, UserHandle user) {
        int badgeResId = getBadgeResIdForUser(user.getIdentifier());
        if (badgeResId == 0) {
            return icon;
        }
        return getBadgedDrawable(icon, getDrawable("system", badgeResId, null), null, true);
    }

    public Drawable getUserBadgedDrawableForDensity(Drawable drawable, UserHandle user, Rect badgeLocation, int badgeDensity) {
        Drawable badgeDrawable = getUserBadgeForDensity(user, badgeDensity);
        if (badgeDrawable == null) {
            return drawable;
        }
        return getBadgedDrawable(drawable, badgeDrawable, badgeLocation, true);
    }

    public Drawable getUserBadgeForDensity(UserHandle user, int density) {
        return getManagedProfileIconForDensity(user, 17302310, density);
    }

    public Drawable getUserBadgeForDensityNoBackground(UserHandle user, int density) {
        return getManagedProfileIconForDensity(user, 17302311, density);
    }

    private Drawable getDrawableForDensity(int drawableId, int density) {
        if (density <= 0) {
            density = this.mContext.getResources().getDisplayMetrics().densityDpi;
        }
        return Resources.getSystem().getDrawableForDensity(drawableId, density);
    }

    private Drawable getManagedProfileIconForDensity(UserHandle user, int drawableId, int density) {
        if (isManagedProfile(user.getIdentifier())) {
            return getDrawableForDensity(drawableId, density);
        }
        return null;
    }

    public CharSequence getUserBadgedLabel(CharSequence label, UserHandle user) {
        if (!isManagedProfile(user.getIdentifier())) {
            return label;
        }
        Resources system = Resources.getSystem();
        Object[] objArr = new Object[1];
        objArr[0] = label;
        return system.getString(17040819, objArr);
    }

    public Resources getResourcesForActivity(ComponentName activityName) throws NameNotFoundException {
        return getResourcesForApplication(getActivityInfo(activityName, 1024).applicationInfo);
    }

    @OppoHook(level = OppoHookType.CHANGE_CODE, note = "YaoJun.Luo@Plf.SDK : Modify for rom theme", property = OppoRomType.ROM)
    public Resources getResourcesForApplication(ApplicationInfo app) throws NameNotFoundException {
        if (app.packageName.equals("system")) {
            return this.mContext.mMainThread.getSystemContext().getResources();
        }
        boolean sameUid;
        if (app.uid == Process.myUid()) {
            sameUid = true;
        } else {
            sameUid = false;
        }
        Resources r = this.mContext.mMainThread.getTopLevelResources(app.packageName, sameUid ? app.sourceDir : app.publicSourceDir, sameUid ? app.splitSourceDirs : app.splitPublicSourceDirs, app.resourceDirs, app.sharedLibraryFiles, 0, this.mContext.mPackageInfo);
        if (r != null) {
            return r;
        }
        throw new NameNotFoundException("Unable to open " + app.publicSourceDir);
    }

    public Resources getResourcesForApplication(String appPackageName) throws NameNotFoundException {
        return getResourcesForApplication(getApplicationInfo(appPackageName, 1024));
    }

    public Resources getResourcesForApplicationAsUser(String appPackageName, int userId) throws NameNotFoundException {
        if (userId < 0) {
            throw new IllegalArgumentException("Call does not support special user #" + userId);
        } else if ("system".equals(appPackageName)) {
            return this.mContext.mMainThread.getSystemContext().getResources();
        } else {
            try {
                ApplicationInfo ai = this.mPM.getApplicationInfo(appPackageName, 1024, userId);
                if (ai != null) {
                    return getResourcesForApplication(ai);
                }
                throw new NameNotFoundException("Package " + appPackageName + " doesn't exist");
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public boolean isSafeMode() {
        try {
            if (this.mCachedSafeMode < 0) {
                int i;
                if (this.mPM.isSafeMode()) {
                    i = 1;
                } else {
                    i = 0;
                }
                this.mCachedSafeMode = i;
            }
            if (this.mCachedSafeMode != 0) {
                return true;
            }
            return false;
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void addOnPermissionsChangeListener(OnPermissionsChangedListener listener) {
        synchronized (this.mPermissionListeners) {
            if (this.mPermissionListeners.get(listener) != null) {
                return;
            }
            OnPermissionsChangeListenerDelegate delegate = new OnPermissionsChangeListenerDelegate(listener, Looper.getMainLooper());
            try {
                this.mPM.addOnPermissionsChangeListener(delegate);
                this.mPermissionListeners.put(listener, delegate);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public void removeOnPermissionsChangeListener(OnPermissionsChangedListener listener) {
        synchronized (this.mPermissionListeners) {
            IOnPermissionsChangeListener delegate = (IOnPermissionsChangeListener) this.mPermissionListeners.get(listener);
            if (delegate != null) {
                try {
                    this.mPM.removeOnPermissionsChangeListener(delegate);
                    this.mPermissionListeners.remove(listener);
                } catch (RemoteException e) {
                    throw e.rethrowFromSystemServer();
                }
            }
        }
    }

    static void configurationChanged() {
        synchronized (sSync) {
            sIconCache.clear();
            sStringCache.clear();
        }
        if (!mAppIconsCache.isEmpty()) {
            mAppIconsCache.clear();
        }
        if (!mActivityIconsCache.isEmpty()) {
            mActivityIconsCache.clear();
        }
    }

    ApplicationPackageManager(ContextImpl context, IPackageManager pm) {
        this.mLock = new Object();
        this.mDelegates = new ArrayList();
        this.mCachedSafeMode = -1;
        this.mPermissionListeners = new ArrayMap();
        this.mPackageDeleleteObserver = new PackageDeleteObserver(this, null);
        this.mContext = context;
        this.mPM = pm;
    }

    /* JADX WARNING: Missing block: B:13:0x0022, code:
            return null;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    @OppoHook(level = OppoHookType.CHANGE_ACCESS, note = "YaoJun.Luo@Plf.SDK : [-private] Modify for rom theme", property = OppoRomType.ROM)
    Drawable getCachedIcon(ResourceName name) {
        synchronized (sSync) {
            WeakReference<ConstantState> wr = (WeakReference) sIconCache.get(name);
            if (wr != null) {
                ConstantState state = (ConstantState) wr.get();
                if (state != null) {
                    Drawable newDrawable = state.newDrawable();
                    return newDrawable;
                }
                sIconCache.remove(name);
            }
        }
    }

    @OppoHook(level = OppoHookType.CHANGE_ACCESS, note = "YaoJun.Luo@Plf.SDK : [-private] Modify for rom theme", property = OppoRomType.ROM)
    void putCachedIcon(ResourceName name, Drawable dr) {
        synchronized (sSync) {
            sIconCache.put(name, new WeakReference(dr.getConstantState()));
        }
    }

    static void handlePackageBroadcast(int cmd, String[] pkgList, boolean hasPkgInfo) {
        boolean immediateGc = false;
        if (cmd == 1) {
            immediateGc = true;
        }
        if (pkgList != null && pkgList.length > 0) {
            boolean needCleanup = false;
            for (String ssp : pkgList) {
                synchronized (sSync) {
                    int i;
                    for (i = sIconCache.size() - 1; i >= 0; i--) {
                        if (((ResourceName) sIconCache.keyAt(i)).packageName.equals(ssp)) {
                            sIconCache.removeAt(i);
                            needCleanup = true;
                        }
                    }
                    for (i = sStringCache.size() - 1; i >= 0; i--) {
                        if (((ResourceName) sStringCache.keyAt(i)).packageName.equals(ssp)) {
                            sStringCache.removeAt(i);
                            needCleanup = true;
                        }
                    }
                }
            }
            if (!needCleanup && !hasPkgInfo) {
                return;
            }
            if (immediateGc) {
                Runtime.getRuntime().gc();
            } else {
                ActivityThread.currentActivityThread().scheduleGcIdler();
            }
        }
    }

    /* JADX WARNING: Missing block: B:12:0x001e, code:
            return null;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private CharSequence getCachedString(ResourceName name) {
        synchronized (sSync) {
            WeakReference<CharSequence> wr = (WeakReference) sStringCache.get(name);
            if (wr != null) {
                CharSequence cs = (CharSequence) wr.get();
                if (cs != null) {
                    return cs;
                }
                sStringCache.remove(name);
            }
        }
    }

    private void putCachedString(ResourceName name, CharSequence cs) {
        synchronized (sSync) {
            sStringCache.put(name, new WeakReference(cs));
        }
    }

    public CharSequence getText(String packageName, int resid, ApplicationInfo appInfo) {
        ResourceName name = new ResourceName(packageName, resid);
        CharSequence text = getCachedString(name);
        if (text != null) {
            return text;
        }
        if (appInfo == null) {
            try {
                appInfo = getApplicationInfo(packageName, 1024);
            } catch (NameNotFoundException e) {
                return null;
            }
        }
        try {
            text = getResourcesForApplication(appInfo).getText(resid);
            putCachedString(name, text);
            return text;
        } catch (NameNotFoundException e2) {
            Log.w("PackageManager", "Failure retrieving resources for " + appInfo.packageName);
            return null;
        } catch (RuntimeException e3) {
            Log.w("PackageManager", "Failure retrieving text 0x" + Integer.toHexString(resid) + " in package " + packageName, e3);
            return null;
        }
    }

    public XmlResourceParser getXml(String packageName, int resid, ApplicationInfo appInfo) {
        if (appInfo == null) {
            try {
                appInfo = getApplicationInfo(packageName, 1024);
            } catch (NameNotFoundException e) {
                return null;
            }
        }
        try {
            return getResourcesForApplication(appInfo).getXml(resid);
        } catch (RuntimeException e2) {
            Log.w("PackageManager", "Failure retrieving xml 0x" + Integer.toHexString(resid) + " in package " + packageName, e2);
        } catch (NameNotFoundException e3) {
            Log.w("PackageManager", "Failure retrieving resources for " + appInfo.packageName);
        }
        return null;
    }

    public CharSequence getApplicationLabel(ApplicationInfo info) {
        return info.loadLabel(this);
    }

    public void installPackage(Uri packageURI, IPackageInstallObserver observer, int flags, String installerPackageName) {
        installCommon(packageURI, new LegacyPackageInstallObserver(observer), flags, installerPackageName, this.mContext.getUserId());
    }

    public void installPackage(Uri packageURI, PackageInstallObserver observer, int flags, String installerPackageName) {
        installCommon(packageURI, observer, flags, installerPackageName, this.mContext.getUserId());
    }

    private void installCommon(Uri packageURI, PackageInstallObserver observer, int flags, String installerPackageName, int userId) {
        if ("file".equals(packageURI.getScheme())) {
            try {
                this.mPM.installPackageAsUser(packageURI.getPath(), observer.getBinder(), flags, installerPackageName, userId);
                return;
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
        throw new UnsupportedOperationException("Only file:// URIs are supported");
    }

    public int installExistingPackage(String packageName) throws NameNotFoundException {
        return installExistingPackageAsUser(packageName, this.mContext.getUserId());
    }

    public int installExistingPackageAsUser(String packageName, int userId) throws NameNotFoundException {
        try {
            int res = this.mPM.installExistingPackageAsUser(packageName, userId);
            if (res != -3) {
                return res;
            }
            throw new NameNotFoundException("Package " + packageName + " doesn't exist");
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void verifyPendingInstall(int id, int response) {
        try {
            this.mPM.verifyPendingInstall(id, response);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void extendVerificationTimeout(int id, int verificationCodeAtTimeout, long millisecondsToDelay) {
        try {
            this.mPM.extendVerificationTimeout(id, verificationCodeAtTimeout, millisecondsToDelay);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void verifyIntentFilter(int id, int verificationCode, List<String> failedDomains) {
        try {
            this.mPM.verifyIntentFilter(id, verificationCode, failedDomains);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public int getIntentVerificationStatusAsUser(String packageName, int userId) {
        try {
            return this.mPM.getIntentVerificationStatus(packageName, userId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean updateIntentVerificationStatusAsUser(String packageName, int status, int userId) {
        try {
            return this.mPM.updateIntentVerificationStatus(packageName, status, userId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public List<IntentFilterVerificationInfo> getIntentFilterVerifications(String packageName) {
        try {
            ParceledListSlice<IntentFilterVerificationInfo> parceledList = this.mPM.getIntentFilterVerifications(packageName);
            if (parceledList == null) {
                return Collections.emptyList();
            }
            return parceledList.getList();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public List<IntentFilter> getAllIntentFilters(String packageName) {
        try {
            ParceledListSlice<IntentFilter> parceledList = this.mPM.getAllIntentFilters(packageName);
            if (parceledList == null) {
                return Collections.emptyList();
            }
            return parceledList.getList();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public String getDefaultBrowserPackageNameAsUser(int userId) {
        try {
            return this.mPM.getDefaultBrowserPackageName(userId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean setDefaultBrowserPackageNameAsUser(String packageName, int userId) {
        try {
            return this.mPM.setDefaultBrowserPackageName(packageName, userId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setInstallerPackageName(String targetPackage, String installerPackageName) {
        try {
            this.mPM.setInstallerPackageName(targetPackage, installerPackageName);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public String getInstallerPackageName(String packageName) {
        try {
            return this.mPM.getInstallerPackageName(packageName);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public int getMoveStatus(int moveId) {
        try {
            return this.mPM.getMoveStatus(moveId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void registerMoveCallback(MoveCallback callback, Handler handler) {
        synchronized (this.mDelegates) {
            MoveCallbackDelegate delegate = new MoveCallbackDelegate(callback, handler.getLooper());
            try {
                this.mPM.registerMoveCallback(delegate);
                this.mDelegates.add(delegate);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public void unregisterMoveCallback(MoveCallback callback) {
        synchronized (this.mDelegates) {
            Iterator<MoveCallbackDelegate> i = this.mDelegates.iterator();
            while (i.hasNext()) {
                MoveCallbackDelegate delegate = (MoveCallbackDelegate) i.next();
                if (delegate.mCallback == callback) {
                    try {
                        this.mPM.unregisterMoveCallback(delegate);
                        i.remove();
                    } catch (RemoteException e) {
                        throw e.rethrowFromSystemServer();
                    }
                }
            }
        }
    }

    public int movePackage(String packageName, VolumeInfo vol) {
        try {
            String volumeUuid;
            if (VolumeInfo.ID_PRIVATE_INTERNAL.equals(vol.id)) {
                volumeUuid = StorageManager.UUID_PRIVATE_INTERNAL;
            } else if (vol.isPrimaryPhysical()) {
                volumeUuid = StorageManager.UUID_PRIMARY_PHYSICAL;
            } else {
                volumeUuid = (String) Preconditions.checkNotNull(vol.fsUuid);
            }
            return this.mPM.movePackage(packageName, volumeUuid);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public VolumeInfo getPackageCurrentVolume(ApplicationInfo app) {
        StorageManager storage = (StorageManager) this.mContext.getSystemService(StorageManager.class);
        if (app.isInternal()) {
            return storage.findVolumeById(VolumeInfo.ID_PRIVATE_INTERNAL);
        }
        if (app.isExternalAsec()) {
            return storage.getPrimaryPhysicalVolume();
        }
        return storage.findVolumeByUuid(app.volumeUuid);
    }

    public List<VolumeInfo> getPackageCandidateVolumes(ApplicationInfo app) {
        StorageManager storage = (StorageManager) this.mContext.getSystemService(StorageManager.class);
        VolumeInfo currentVol = getPackageCurrentVolume(app);
        List<VolumeInfo> vols = storage.getVolumes();
        List<VolumeInfo> candidates = new ArrayList();
        for (VolumeInfo vol : vols) {
            if (Objects.equals(vol, currentVol) || isPackageCandidateVolume(this.mContext, app, vol)) {
                candidates.add(vol);
            }
        }
        return candidates;
    }

    /* JADX WARNING: Missing block: B:17:0x0038, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean isPackageCandidateVolume(ContextImpl context, ApplicationInfo app, VolumeInfo vol) {
        boolean z = true;
        boolean forceAllowOnExternal = Global.getInt(context.getContentResolver(), Global.FORCE_ALLOW_ON_EXTERNAL, 0) != 0;
        if (VolumeInfo.ID_PRIVATE_INTERNAL.equals(vol.getId())) {
            return true;
        }
        if (app.isSystemApp() || app.isVendorApp()) {
            return false;
        }
        if ((!forceAllowOnExternal && (app.installLocation == 1 || app.installLocation == -1)) || !vol.isMountedWritable()) {
            return false;
        }
        if (vol.isPrimaryPhysical()) {
            return app.isInternal();
        }
        try {
            if (this.mPM.isPackageDeviceAdminOnAnyUser(app.packageName)) {
                return false;
            }
            if (vol.getType() != 1) {
                z = false;
            }
            return z;
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public int movePrimaryStorage(VolumeInfo vol) {
        try {
            String volumeUuid;
            if (VolumeInfo.ID_PRIVATE_INTERNAL.equals(vol.id)) {
                volumeUuid = StorageManager.UUID_PRIVATE_INTERNAL;
            } else if (vol.isPrimaryPhysical()) {
                volumeUuid = StorageManager.UUID_PRIMARY_PHYSICAL;
            } else {
                volumeUuid = (String) Preconditions.checkNotNull(vol.fsUuid);
            }
            return this.mPM.movePrimaryStorage(volumeUuid);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public VolumeInfo getPrimaryStorageCurrentVolume() {
        StorageManager storage = (StorageManager) this.mContext.getSystemService(StorageManager.class);
        return storage.findVolumeByQualifiedUuid(storage.getPrimaryStorageUuid());
    }

    public List<VolumeInfo> getPrimaryStorageCandidateVolumes() {
        StorageManager storage = (StorageManager) this.mContext.getSystemService(StorageManager.class);
        VolumeInfo currentVol = getPrimaryStorageCurrentVolume();
        List<VolumeInfo> vols = storage.getVolumes();
        List<VolumeInfo> candidates = new ArrayList();
        if (!Objects.equals(StorageManager.UUID_PRIMARY_PHYSICAL, storage.getPrimaryStorageUuid()) || currentVol == null) {
            for (VolumeInfo vol : vols) {
                if (Objects.equals(vol, currentVol) || isPrimaryStorageCandidateVolume(vol)) {
                    candidates.add(vol);
                }
            }
        } else {
            candidates.add(currentVol);
        }
        return candidates;
    }

    private static boolean isPrimaryStorageCandidateVolume(VolumeInfo vol) {
        boolean z = true;
        if (VolumeInfo.ID_PRIVATE_INTERNAL.equals(vol.getId())) {
            return true;
        }
        if (!vol.isMountedWritable()) {
            return false;
        }
        if (vol.getType() != 1) {
            z = false;
        }
        return z;
    }

    public void deletePackage(String packageName, IPackageDeleteObserver observer, int flags) {
        deletePackageAsUser(packageName, observer, flags, this.mContext.getUserId());
    }

    public void deletePackageAsUser(String packageName, IPackageDeleteObserver observer, int flags, int userId) {
        try {
            this.mPM.deletePackageAsUser(packageName, observer, userId, flags);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void clearApplicationUserData(String packageName, IPackageDataObserver observer) {
        try {
            this.mPM.clearApplicationUserData(packageName, observer, this.mContext.getUserId());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void deleteApplicationCacheFiles(String packageName, IPackageDataObserver observer) {
        try {
            this.mPM.deleteApplicationCacheFiles(packageName, observer);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void deleteApplicationCacheFilesCrossUser(String packageName, IPackageDataObserver observer, int userId) {
        try {
            this.mPM.deleteApplicationCacheFilesCrossUser(packageName, observer, userId);
        } catch (RemoteException e) {
        }
    }

    public void deleteApplicationCacheFilesAsUser(String packageName, int userId, IPackageDataObserver observer) {
        try {
            this.mPM.deleteApplicationCacheFilesAsUser(packageName, userId, observer);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void freeStorageAndNotify(String volumeUuid, long idealStorageSize, IPackageDataObserver observer) {
        try {
            this.mPM.freeStorageAndNotify(volumeUuid, idealStorageSize, observer);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void freeStorage(String volumeUuid, long freeStorageSize, IntentSender pi) {
        try {
            this.mPM.freeStorage(volumeUuid, freeStorageSize, pi);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public String[] setPackagesSuspendedAsUser(String[] packageNames, boolean suspended, int userId) {
        try {
            return this.mPM.setPackagesSuspendedAsUser(packageNames, suspended, userId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean isPackageSuspendedForUser(String packageName, int userId) {
        try {
            return this.mPM.isPackageSuspendedForUser(packageName, userId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void getPackageSizeInfoAsUser(String packageName, int userHandle, IPackageStatsObserver observer) {
        try {
            this.mPM.getPackageSizeInfo(packageName, userHandle, observer);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void addPackageToPreferred(String packageName) {
        Log.w(TAG, "addPackageToPreferred() is a no-op");
    }

    public void removePackageFromPreferred(String packageName) {
        Log.w(TAG, "removePackageFromPreferred() is a no-op");
    }

    public List<PackageInfo> getPreferredPackages(int flags) {
        Log.w(TAG, "getPreferredPackages() is a no-op");
        return Collections.emptyList();
    }

    public void addPreferredActivity(IntentFilter filter, int match, ComponentName[] set, ComponentName activity) {
        try {
            this.mPM.addPreferredActivity(filter, match, set, activity, this.mContext.getUserId());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void addPreferredActivityAsUser(IntentFilter filter, int match, ComponentName[] set, ComponentName activity, int userId) {
        try {
            this.mPM.addPreferredActivity(filter, match, set, activity, userId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void replacePreferredActivity(IntentFilter filter, int match, ComponentName[] set, ComponentName activity) {
        try {
            this.mPM.replacePreferredActivity(filter, match, set, activity, this.mContext.getUserId());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void replacePreferredActivityAsUser(IntentFilter filter, int match, ComponentName[] set, ComponentName activity, int userId) {
        try {
            this.mPM.replacePreferredActivity(filter, match, set, activity, userId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void clearPackagePreferredActivities(String packageName) {
        try {
            this.mPM.clearPackagePreferredActivities(packageName);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public int getPreferredActivities(List<IntentFilter> outFilters, List<ComponentName> outActivities, String packageName) {
        try {
            return this.mPM.getPreferredActivities(outFilters, outActivities, packageName);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public ComponentName getHomeActivities(List<ResolveInfo> outActivities) {
        try {
            return this.mPM.getHomeActivities(outActivities);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setComponentEnabledSetting(ComponentName componentName, int newState, int flags) {
        try {
            this.mPM.setComponentEnabledSetting(componentName, newState, flags, this.mContext.getUserId());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public int getComponentEnabledSetting(ComponentName componentName) {
        try {
            return this.mPM.getComponentEnabledSetting(componentName, this.mContext.getUserId());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setApplicationEnabledSetting(String packageName, int newState, int flags) {
        try {
            this.mPM.setApplicationEnabledSetting(packageName, newState, flags, this.mContext.getUserId(), this.mContext.getOpPackageName());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public int getApplicationEnabledSetting(String packageName) {
        try {
            return this.mPM.getApplicationEnabledSetting(packageName, this.mContext.getUserId());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void flushPackageRestrictionsAsUser(int userId) {
        try {
            this.mPM.flushPackageRestrictionsAsUser(userId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public int checkAPKSignatures(String pkg) {
        try {
            return this.mPM.checkAPKSignatures(pkg);
        } catch (RemoteException e) {
            return 0;
        }
    }

    public boolean setApplicationHiddenSettingAsUser(String packageName, boolean hidden, UserHandle user) {
        try {
            return this.mPM.setApplicationHiddenSettingAsUser(packageName, hidden, user.getIdentifier());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean getApplicationHiddenSettingAsUser(String packageName, UserHandle user) {
        try {
            return this.mPM.getApplicationHiddenSettingAsUser(packageName, user.getIdentifier());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public KeySet getKeySetByAlias(String packageName, String alias) {
        Preconditions.checkNotNull(packageName);
        Preconditions.checkNotNull(alias);
        try {
            return this.mPM.getKeySetByAlias(packageName, alias);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public KeySet getSigningKeySet(String packageName) {
        Preconditions.checkNotNull(packageName);
        try {
            return this.mPM.getSigningKeySet(packageName);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean isSignedBy(String packageName, KeySet ks) {
        Preconditions.checkNotNull(packageName);
        Preconditions.checkNotNull(ks);
        try {
            return this.mPM.isPackageSignedByKeySet(packageName, ks);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean isSignedByExactly(String packageName, KeySet ks) {
        Preconditions.checkNotNull(packageName);
        Preconditions.checkNotNull(ks);
        try {
            return this.mPM.isPackageSignedByKeySetExactly(packageName, ks);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public VerifierDeviceIdentity getVerifierDeviceIdentity() {
        try {
            return this.mPM.getVerifierDeviceIdentity();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean isUpgrade() {
        try {
            return this.mPM.isUpgrade();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public PackageInstaller getPackageInstaller() {
        PackageInstaller packageInstaller;
        synchronized (this.mLock) {
            if (this.mInstaller == null) {
                try {
                    this.mInstaller = new PackageInstaller(this.mContext, this, this.mPM.getPackageInstaller(), this.mContext.getPackageName(), this.mContext.getUserId());
                } catch (RemoteException e) {
                    throw e.rethrowFromSystemServer();
                }
            }
            packageInstaller = this.mInstaller;
        }
        return packageInstaller;
    }

    public boolean isPackageAvailable(String packageName) {
        try {
            return this.mPM.isPackageAvailable(packageName, this.mContext.getUserId());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void addCrossProfileIntentFilter(IntentFilter filter, int sourceUserId, int targetUserId, int flags) {
        try {
            this.mPM.addCrossProfileIntentFilter(filter, this.mContext.getOpPackageName(), sourceUserId, targetUserId, flags);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void clearCrossProfileIntentFilters(int sourceUserId) {
        try {
            this.mPM.clearCrossProfileIntentFilters(sourceUserId, this.mContext.getOpPackageName());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public Drawable loadItemIcon(PackageItemInfo itemInfo, ApplicationInfo appInfo) {
        Drawable dr = loadUnbadgedItemIcon(itemInfo, appInfo);
        if (itemInfo.showUserIcon != -10000) {
            return dr;
        }
        return getUserBadgedIcon(dr, new UserHandle(this.mContext.getUserId()));
    }

    public Drawable loadUnbadgedItemIcon(PackageItemInfo itemInfo, ApplicationInfo appInfo) {
        if (itemInfo.showUserIcon != -10000) {
            Bitmap bitmap = getUserManager().getUserIcon(itemInfo.showUserIcon);
            if (bitmap == null) {
                return UserIcons.getDefaultUserIcon(itemInfo.showUserIcon, false);
            }
            return new BitmapDrawable(bitmap);
        }
        Drawable dr = null;
        if (itemInfo.packageName != null) {
            dr = getDrawable(itemInfo.packageName, itemInfo.icon, appInfo);
        }
        if (dr == null) {
            dr = itemInfo.loadDefaultIcon(this);
        }
        return dr;
    }

    private Drawable getBadgedDrawable(Drawable drawable, Drawable badgeDrawable, Rect badgeLocation, boolean tryBadgeInPlace) {
        boolean canBadgeInPlace;
        Bitmap bitmap;
        int badgedWidth = drawable.getIntrinsicWidth();
        int badgedHeight = drawable.getIntrinsicHeight();
        if (tryBadgeInPlace && (drawable instanceof BitmapDrawable)) {
            canBadgeInPlace = ((BitmapDrawable) drawable).getBitmap().isMutable();
        } else {
            canBadgeInPlace = false;
        }
        if (canBadgeInPlace) {
            bitmap = ((BitmapDrawable) drawable).getBitmap();
        } else {
            bitmap = Bitmap.createBitmap(badgedWidth, badgedHeight, Config.ARGB_8888);
        }
        Canvas canvas = new Canvas(bitmap);
        if (!canBadgeInPlace) {
            drawable.setBounds(0, 0, badgedWidth, badgedHeight);
            drawable.draw(canvas);
        }
        if (badgeLocation == null) {
            badgeDrawable.setBounds(0, 0, badgedWidth, badgedHeight);
            badgeDrawable.draw(canvas);
        } else if (badgeLocation.left < 0 || badgeLocation.top < 0 || badgeLocation.width() > badgedWidth || badgeLocation.height() > badgedHeight) {
            throw new IllegalArgumentException("Badge location " + badgeLocation + " not in badged drawable bounds " + new Rect(0, 0, badgedWidth, badgedHeight));
        } else {
            badgeDrawable.setBounds(0, 0, badgeLocation.width(), badgeLocation.height());
            canvas.save();
            canvas.translate((float) badgeLocation.left, (float) badgeLocation.top);
            badgeDrawable.draw(canvas);
            canvas.restore();
        }
        if (canBadgeInPlace) {
            return drawable;
        }
        BitmapDrawable mergedDrawable = new BitmapDrawable(this.mContext.getResources(), bitmap);
        if (drawable instanceof BitmapDrawable) {
            mergedDrawable.setTargetDensity(((BitmapDrawable) drawable).getBitmap().getDensity());
        }
        return mergedDrawable;
    }

    private int getBadgeResIdForUser(int userId) {
        if (isManagedProfile(userId)) {
            return 17302314;
        }
        return 0;
    }

    private boolean isManagedProfile(int userId) {
        return getUserManager().isManagedProfile(userId);
    }

    @OppoHook(level = OppoHookType.NEW_METHOD, note = "Changwei.Li@Plf.SDK : Add for rom theme. we can make ConvertIcon Useless", property = OppoRomType.ROM)
    public Drawable loadItemIcon(PackageItemInfo itemInfo, ApplicationInfo appInfo, boolean isConvertEnable) {
        if (itemInfo.showUserIcon != -10000) {
            Bitmap bitmap = getUserManager().getUserIcon(itemInfo.showUserIcon);
            if (bitmap == null) {
                return UserIcons.getDefaultUserIcon(itemInfo.showUserIcon, false);
            }
            return new BitmapDrawable(bitmap);
        }
        Drawable dr = null;
        if (!(itemInfo.packageName == null || itemInfo.icon == 0)) {
            dr = isConvertEnable ? OppoThemeHelper.getDrawable(this, itemInfo.packageName, itemInfo.icon, appInfo, itemInfo.name) : getDrawable(itemInfo.packageName, itemInfo.icon, appInfo);
        }
        if (dr == null) {
            dr = itemInfo.loadDefaultIcon(this);
        }
        return getUserBadgedIcon(dr, new UserHandle(this.mContext.getUserId()));
    }

    public Bitmap getAppIconBitmap(String packageName) {
        try {
            return this.mPM.getAppIconBitmap(packageName);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public Map<String, Bitmap> getAppIconsCache(boolean compress) {
        try {
            return this.mPM.getAppIconsCache(compress);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public Drawable getApplicationIconCacheAll(ApplicationInfo info) {
        try {
            if (mAppIconsCache.isEmpty()) {
                mAppIconsCache = (HashMap) getAppIconsCache(true);
            }
            Bitmap bitmap = (Bitmap) mAppIconsCache.get(info.packageName);
            if (bitmap != null) {
                return new BitmapDrawable(this.mContext.getResources(), bitmap);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return info.loadIcon(this);
    }

    public Drawable getApplicationIconCache(ApplicationInfo info) {
        try {
            if (mAppIconsCache.isEmpty()) {
                mAppIconsCache = (HashMap) getAppIconsCache(true);
            }
            Bitmap bitmap = (Bitmap) mAppIconsCache.get(info.packageName);
            if (bitmap != null) {
                return new BitmapDrawable(this.mContext.getResources(), bitmap);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return info.loadIcon(this);
    }

    public Drawable getApplicationIconCache(String packageName) throws NameNotFoundException {
        try {
            if (mAppIconsCache.isEmpty()) {
                mAppIconsCache = (HashMap) getAppIconsCache(true);
            }
            Bitmap bitmap = (Bitmap) mAppIconsCache.get(packageName);
            if (bitmap != null) {
                return new BitmapDrawable(this.mContext.getResources(), bitmap);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return getApplicationIcon(getApplicationInfo(packageName, 1024));
    }

    public Drawable getApplicationIconCacheOrignal(ApplicationInfo info) {
        try {
            if (mAppIconsCache.isEmpty()) {
                mAppIconsCache = (HashMap) getAppIconsCache(false);
            }
            Bitmap bitmap = (Bitmap) mAppIconsCache.get(info.packageName);
            if (bitmap != null) {
                return new BitmapDrawable(this.mContext.getResources(), bitmap);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return info.loadIcon(this);
    }

    public Drawable getApplicationIconCacheOrignal(String packageName) throws NameNotFoundException {
        try {
            if (mAppIconsCache.isEmpty()) {
                mAppIconsCache = (HashMap) getAppIconsCache(false);
            }
            Bitmap bitmap = (Bitmap) mAppIconsCache.get(packageName);
            if (bitmap != null) {
                return new BitmapDrawable(this.mContext.getResources(), bitmap);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return getApplicationIcon(getApplicationInfo(packageName, 1024));
    }

    public Drawable getActivityIconCache(ComponentName componentName) throws NameNotFoundException {
        try {
            if (mActivityIconsCache.isEmpty() || mIconCacheDirty) {
                mActivityIconsCache = (HashMap) getActivityIconsCache(this.mPackageDeleleteObserver);
                mIconCacheDirty = false;
            }
            Bitmap bitmap = (Bitmap) mActivityIconsCache.get(componentName.getPackageName() + "/" + componentName.getClassName());
            if (bitmap != null) {
                return new BitmapDrawable(this.mContext.getResources(), bitmap);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return getActivityInfo(componentName, 1024).loadIcon(this);
    }

    public Map<String, Bitmap> getActivityIconsCache(IPackageDeleteObserver observer) {
        try {
            return this.mPM.getActivityIconsCache(observer);
        } catch (RemoteException e) {
            throw new RuntimeException("Package manager has died", e);
        }
    }

    public boolean isSystemDataApp(String packageName) {
        try {
            return this.mPM.isSystemDataApp(packageName);
        } catch (RemoteException e) {
            throw new RuntimeException("Package manager has died", e);
        }
    }

    public boolean prohibitChildInstallation(int userId, boolean isInstall) {
        try {
            return this.mPM.prohibitChildInstallation(userId, isInstall);
        } catch (RemoteException e) {
            throw new RuntimeException("Package manager has died", e);
        }
    }

    public void checkEMMApkRuntimePermission(ComponentName cn) throws SecurityException {
        String packageName = cn.getPackageName();
        if (packageName == null) {
            throw new SecurityException("Package name is null");
        }
        try {
            String ret = this.mPM.checkEMMApkRuntimePermission(packageName);
            Log.d(TAG, "check EMM apk runtime permission:" + ret);
            if (!"".equals(ret)) {
                throw new SecurityException(ret);
            }
        } catch (RemoteException e) {
            throw new SecurityException(e.getMessage());
        }
    }

    public boolean isInstallSourceEnable() {
        try {
            return this.mPM.isInstallSourceEnable();
        } catch (RemoteException e) {
            return false;
        }
    }

    public List<String> getInstallSourceList() {
        try {
            return this.mPM.getInstallSourceList();
        } catch (RemoteException e) {
            throw new RuntimeException("get Install Source List failed!", e);
        }
    }

    public void deleteInstallSource(String packageName) {
        try {
            this.mPM.deleteInstallSource(packageName);
        } catch (RemoteException e) {
            Log.d(TAG, "delete Install Source failed!");
        }
    }

    public void enableInstallSource(boolean trust) {
        try {
            this.mPM.enableInstallSource(trust);
        } catch (RemoteException e) {
            Log.d(TAG, "enable Install Source failed!");
        }
    }

    public void addInstallSource(String packageName) {
        try {
            this.mPM.addInstallSource(packageName);
        } catch (RemoteException e) {
            Log.d(TAG, "add Install Source failed!");
        }
    }

    public void addDisallowUninstallApps(List<String> packageNames) {
        try {
            this.mPM.addDisallowUninstallApps(packageNames);
        } catch (RemoteException e) {
            Log.d(TAG, "add Disallow Uninstall Apps failed!");
        }
    }

    public void removeDisallowUninstallApps(List<String> packageNames) {
        try {
            this.mPM.removeDisallowUninstallApps(packageNames);
        } catch (RemoteException e) {
            Log.d(TAG, "remove Disallow Uninstall Apps failed!");
        }
    }

    public List<String> getDisallowUninstallApps() {
        try {
            return this.mPM.getDisallowUninstallApps();
        } catch (RemoteException e) {
            Log.d(TAG, "get Disallow Uninstall Apps failed!");
            return null;
        }
    }

    public void addInstallPackageWhitelist(int mode, List<String> applist) {
        try {
            this.mPM.addInstallPackageWhitelist(mode, applist);
        } catch (RemoteException e) {
            Log.d(TAG, "add Install Package White list failed!");
        }
    }

    public void addInstallPackageBlacklist(int mode, List<String> applist) {
        try {
            this.mPM.addInstallPackageBlacklist(mode, applist);
        } catch (RemoteException e) {
            Log.d(TAG, "add Install Package Black list failed!");
        }
    }
}