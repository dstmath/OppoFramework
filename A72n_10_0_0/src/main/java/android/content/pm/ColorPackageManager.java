package android.content.pm;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.IntentSender;
import android.content.pm.IPackageDeleteObserver;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.Log;
import com.color.content.ColorRemovableAppInfo;
import com.color.content.ColorRuleInfo;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ColorPackageManager extends ColorBasePackageManager implements IColorPackageManager {
    private static HashMap<String, Bitmap> mActivityIconsCache = new HashMap<>();
    private static HashMap<String, Bitmap> mAppIconsCache = new HashMap<>();
    private static ColorPackageManager mColorPackageManager = null;
    private static boolean mIconCacheDirty = false;
    private static final int sDefaultFlags = 1024;
    private final String TAG;
    private final Context mContext;
    private final PackageDeleteObserver mPackageDeleleteObserver;

    public ColorPackageManager(Context context) {
        this.TAG = "ColorPackageManager";
        this.mPackageDeleleteObserver = new PackageDeleteObserver();
        this.mContext = context;
    }

    public ColorPackageManager() {
        this.TAG = "ColorPackageManager";
        this.mPackageDeleleteObserver = new PackageDeleteObserver();
        this.mContext = null;
    }

    public static ColorPackageManager getColorPackageManager(Context context) {
        ColorPackageManager colorPackageManager = mColorPackageManager;
        if (colorPackageManager != null) {
            return colorPackageManager;
        }
        mColorPackageManager = new ColorPackageManager(context);
        return mColorPackageManager;
    }

    @Override // android.content.pm.IColorPackageManager
    public boolean isClosedSuperFirewall() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorBasePackageManager.DESCRIPTOR);
            this.mRemote.transact(10002, data, reply, 0);
            reply.readException();
            return Boolean.valueOf(reply.readString()).booleanValue();
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    @Override // android.content.pm.IColorPackageManager
    public Bitmap getAppIconBitmap(String packageName) throws RemoteException {
        Bitmap result;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorBasePackageManager.DESCRIPTOR);
            data.writeString(packageName);
            this.mRemote.transact(10004, data, reply, 0);
            reply.readException();
            if (reply.readInt() != 0) {
                result = Bitmap.CREATOR.createFromParcel(reply);
            } else {
                result = null;
            }
            return result;
        } finally {
            reply.recycle();
            data.recycle();
        }
    }

    @Override // android.content.pm.IColorPackageManager
    public Map getAppIconsCache(boolean compress) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorBasePackageManager.DESCRIPTOR);
            data.writeInt(compress ? 1 : 0);
            this.mRemote.transact(10005, data, reply, 0);
            reply.readException();
            return reply.readHashMap(getClass().getClassLoader());
        } finally {
            reply.recycle();
            data.recycle();
        }
    }

    @Override // android.content.pm.IColorPackageManager
    public Map getActivityIconsCache(IPackageDeleteObserver observer) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorBasePackageManager.DESCRIPTOR);
            data.writeStrongBinder(observer != null ? observer.asBinder() : null);
            this.mRemote.transact(10006, data, reply, 0);
            reply.readException();
            return reply.readHashMap(getClass().getClassLoader());
        } finally {
            reply.recycle();
            data.recycle();
        }
    }

    @Override // android.content.pm.IColorPackageManager
    public boolean prohibitChildInstallation(int userId, boolean isInstall) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorBasePackageManager.DESCRIPTOR);
            data.writeInt(userId);
            data.writeBoolean(isInstall);
            boolean result = false;
            this.mRemote.transact(10007, data, reply, 0);
            reply.readException();
            if (reply.readInt() != 0) {
                result = true;
            }
            return result;
        } finally {
            reply.recycle();
            data.recycle();
        }
    }

    @Override // android.content.pm.IColorPackageManager
    public int oppoFreezePackage(String pkgName, int userId, int freezeFlag, int flag, String callingPkg) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorBasePackageManager.DESCRIPTOR);
            data.writeString(pkgName);
            data.writeInt(userId);
            data.writeInt(freezeFlag);
            data.writeInt(flag);
            data.writeString(callingPkg);
            this.mRemote.transact(10008, data, reply, 0);
            reply.readException();
            return reply.readInt();
        } finally {
            reply.recycle();
            data.recycle();
        }
    }

    @Override // android.content.pm.IColorPackageManager
    public int oppoUnFreezePackage(String pkgName, int userId, int freezeFlag, int flag, String callingPkg) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorBasePackageManager.DESCRIPTOR);
            data.writeString(pkgName);
            data.writeInt(userId);
            data.writeInt(freezeFlag);
            data.writeInt(flag);
            data.writeString(callingPkg);
            this.mRemote.transact(10009, data, reply, 0);
            reply.readException();
            return reply.readInt();
        } finally {
            reply.recycle();
            data.recycle();
        }
    }

    @Override // android.content.pm.IColorPackageManager
    public int getOppoFreezePackageState(String pkgName, int userId) throws RemoteException {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        try {
            _data.writeInterfaceToken(IColorBasePackageManager.DESCRIPTOR);
            _data.writeString(pkgName);
            _data.writeInt(userId);
            this.mRemote.transact(10010, _data, _reply, 0);
            _reply.readException();
            return _reply.readInt();
        } finally {
            _reply.recycle();
            _data.recycle();
        }
    }

    @Override // android.content.pm.IColorPackageManager
    public boolean inOppoFreezePackageList(String pkgName, int userId) throws RemoteException {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        try {
            _data.writeInterfaceToken(IColorBasePackageManager.DESCRIPTOR);
            _data.writeString(pkgName);
            _data.writeInt(userId);
            boolean _result = false;
            this.mRemote.transact(10011, _data, _reply, 0);
            _reply.readException();
            if (_reply.readInt() != 0) {
                _result = true;
            }
            return _result;
        } finally {
            _reply.recycle();
            _data.recycle();
        }
    }

    @Override // android.content.pm.IColorPackageManager
    public List<String> getOppoFreezedPackageList(int userId) throws RemoteException {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        try {
            _data.writeInterfaceToken(IColorBasePackageManager.DESCRIPTOR);
            _data.writeInt(userId);
            this.mRemote.transact(10012, _data, _reply, 0);
            _reply.readException();
            return _reply.createStringArrayList();
        } finally {
            _reply.recycle();
            _data.recycle();
        }
    }

    @Override // android.content.pm.IColorPackageManager
    public int getOppoPackageFreezeFlag(String pkgName, int userId) throws RemoteException {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        try {
            _data.writeInterfaceToken(IColorBasePackageManager.DESCRIPTOR);
            _data.writeString(pkgName);
            _data.writeInt(userId);
            this.mRemote.transact(10013, _data, _reply, 0);
            _reply.readException();
            return _reply.readInt();
        } finally {
            _reply.recycle();
            _data.recycle();
        }
    }

    @Override // android.content.pm.IColorPackageManager
    public boolean loadRegionFeature(String name) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorBasePackageManager.DESCRIPTOR);
            data.writeString(name);
            boolean result = false;
            this.mRemote.transact(10014, data, reply, 0);
            reply.readException();
            if (reply.readInt() != 0) {
                result = true;
            }
            return result;
        } finally {
            reply.recycle();
            data.recycle();
        }
    }

    @Override // android.content.pm.IColorPackageManager
    public FeatureInfo[] getOppoSystemAvailableFeatures() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorBasePackageManager.DESCRIPTOR);
            this.mRemote.transact(10015, data, reply, 0);
            reply.readException();
            return (FeatureInfo[]) reply.createTypedArray(FeatureInfo.CREATOR);
        } finally {
            reply.recycle();
            data.recycle();
        }
    }

    @Override // android.content.pm.IColorPackageManager
    public boolean isSecurePayApp(String name) throws RemoteException {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        try {
            _data.writeInterfaceToken(IColorBasePackageManager.DESCRIPTOR);
            _data.writeString(name);
            boolean _result = false;
            this.mRemote.transact(10016, _data, _reply, 0);
            _reply.readException();
            if (_reply.readInt() != 0) {
                _result = true;
            }
            return _result;
        } finally {
            _reply.recycle();
            _data.recycle();
        }
    }

    @Override // android.content.pm.IColorPackageManager
    public boolean isSystemDataApp(String packageName) throws RemoteException {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        try {
            _data.writeInterfaceToken(IColorBasePackageManager.DESCRIPTOR);
            _data.writeString(packageName);
            boolean _result = false;
            this.mRemote.transact(10017, _data, _reply, 0);
            _reply.readException();
            if (_reply.readInt() != 0) {
                _result = true;
            }
            return _result;
        } finally {
            _reply.recycle();
            _data.recycle();
        }
    }

    @Override // android.content.pm.IColorPackageManager
    public boolean inPmsWhiteList(int type, String verifyStr, List<String> defaultList) throws RemoteException {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        try {
            _data.writeInterfaceToken(IColorBasePackageManager.DESCRIPTOR);
            _data.writeInt(type);
            _data.writeString(verifyStr);
            _data.writeStringList(defaultList);
            boolean _result = false;
            this.mRemote.transact(10020, _data, _reply, 0);
            _reply.readException();
            if (_reply.readInt() != 0) {
                _result = true;
            }
            return _result;
        } finally {
            _reply.recycle();
            _data.recycle();
        }
    }

    @Override // android.content.pm.IColorPackageManager
    public boolean setInterceptRuleInfos(List<ColorRuleInfo> infos) throws RemoteException {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        try {
            _data.writeInterfaceToken(IColorBasePackageManager.DESCRIPTOR);
            _data.writeTypedList(infos);
            boolean _result = false;
            this.mRemote.transact(10018, _data, _reply, 0);
            _reply.readException();
            if (_reply.readInt() != 0) {
                _result = true;
            }
            return _result;
        } finally {
            _reply.recycle();
            _data.recycle();
        }
    }

    @Override // android.content.pm.IColorPackageManager
    public List<ColorRuleInfo> getInterceptRuleInfos() throws RemoteException {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        try {
            _data.writeInterfaceToken(IColorBasePackageManager.DESCRIPTOR);
            this.mRemote.transact(10019, _data, _reply, 0);
            _reply.readException();
            return _reply.createTypedArrayList(ColorRuleInfo.CREATOR);
        } finally {
            _reply.recycle();
            _data.recycle();
        }
    }

    @Override // android.content.pm.IColorPackageManager
    public boolean isFullFunctionMode() throws RemoteException {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        try {
            _data.writeInterfaceToken(IColorBasePackageManager.DESCRIPTOR);
            this.mRemote.transact(10002, _data, _reply, 0);
            _reply.readException();
            return Boolean.valueOf(_reply.readString()).booleanValue();
        } finally {
            _reply.recycle();
            _data.recycle();
        }
    }

    @Override // android.content.pm.IColorPackageManager
    public Drawable getApplicationIconCacheAll(ApplicationInfo info) {
        Context context = this.mContext;
        if (context != null) {
            return info.loadIcon(context.getPackageManager());
        }
        Log.e("ColorPackageManager", "App must create ColorPackageManager with context parameter when using this method");
        return null;
    }

    @Override // android.content.pm.IColorPackageManager
    public Drawable getApplicationIconCache(ApplicationInfo info) {
        Context context = this.mContext;
        if (context != null) {
            return info.loadIcon(context.getPackageManager());
        }
        Log.e("ColorPackageManager", "App must create ColorPackageManager with context parameter when using this method");
        return null;
    }

    @Override // android.content.pm.IColorPackageManager
    public Drawable getApplicationIconCache(String packageName) throws PackageManager.NameNotFoundException {
        Context context = this.mContext;
        if (context != null) {
            return context.getPackageManager().getApplicationIcon(this.mContext.getPackageManager().getApplicationInfoAsUser(packageName, 1024, ActivityManager.getCurrentUser()));
        }
        Log.e("ColorPackageManager", "App must create ColorPackageManager with context parameter when using this method");
        return null;
    }

    @Override // android.content.pm.IColorPackageManager
    public Drawable getApplicationIconCacheOrignal(ApplicationInfo info) {
        Context context = this.mContext;
        if (context != null) {
            return info.loadIcon(context.getPackageManager());
        }
        Log.e("ColorPackageManager", "App must create ColorPackageManager with context parameter when using this method");
        return null;
    }

    @Override // android.content.pm.IColorPackageManager
    public Drawable getApplicationIconCacheOrignal(String packageName) throws PackageManager.NameNotFoundException {
        Context context = this.mContext;
        if (context != null) {
            return context.getPackageManager().getApplicationIcon(this.mContext.getPackageManager().getApplicationInfoAsUser(packageName, 1024, ActivityManager.getCurrentUser()));
        }
        Log.e("ColorPackageManager", "App must create ColorPackageManager with context parameter when using this method");
        return null;
    }

    private class PackageDeleteObserver extends IPackageDeleteObserver.Stub {
        private PackageDeleteObserver() {
        }

        @Override // android.content.pm.IPackageDeleteObserver
        public void packageDeleted(String packageName, int returnCode) {
            if (packageName != null) {
                try {
                    if (ColorPackageManager.mAppIconsCache.get(packageName) != null) {
                        ColorPackageManager.mAppIconsCache.remove(packageName);
                    }
                    ArrayList<String> deleteList = new ArrayList<>();
                    for (Map.Entry entry : ColorPackageManager.mActivityIconsCache.entrySet()) {
                        String key = (String) entry.getKey();
                        if (packageName.equals(key.split("/")[0])) {
                            deleteList.add(key);
                        }
                    }
                    Iterator<String> it = deleteList.iterator();
                    while (it.hasNext()) {
                        ColorPackageManager.mActivityIconsCache.remove(it.next());
                    }
                    boolean unused = ColorPackageManager.mIconCacheDirty = true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override // android.content.pm.IColorPackageManager
    public Drawable getActivityIconCache(ComponentName componentName) throws PackageManager.NameNotFoundException {
        Context context = this.mContext;
        if (context != null) {
            return context.getPackageManager().getActivityInfo(componentName, 1024).loadIcon(this.mContext.getPackageManager());
        }
        Log.e("ColorPackageManager", "App must create ColorPackageManager with context parameter when using this method");
        return null;
    }

    @Override // android.content.pm.IColorPackageManager
    public List<String> getRemovableAppList() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorBasePackageManager.DESCRIPTOR);
            this.mRemote.transact(10021, data, reply, 0);
            reply.readException();
            return reply.createStringArrayList();
        } finally {
            reply.recycle();
            data.recycle();
        }
    }

    @Override // android.content.pm.IColorPackageManager
    public List<ColorRemovableAppInfo> getRemovedAppInfos() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorBasePackageManager.DESCRIPTOR);
            this.mRemote.transact(10022, data, reply, 0);
            reply.readException();
            return reply.createTypedArrayList(ColorRemovableAppInfo.CREATOR);
        } finally {
            reply.recycle();
            data.recycle();
        }
    }

    @Override // android.content.pm.IColorPackageManager
    public List<ColorRemovableAppInfo> getRemovableAppInfos() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorBasePackageManager.DESCRIPTOR);
            this.mRemote.transact(10023, data, reply, 0);
            reply.readException();
            return reply.createTypedArrayList(ColorRemovableAppInfo.CREATOR);
        } finally {
            reply.recycle();
            data.recycle();
        }
    }

    @Override // android.content.pm.IColorPackageManager
    public ColorRemovableAppInfo getRemovableAppInfo(String packageName) throws RemoteException {
        ColorRemovableAppInfo result;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorBasePackageManager.DESCRIPTOR);
            data.writeString(packageName);
            this.mRemote.transact(10024, data, reply, 0);
            reply.readException();
            if (reply.readInt() != 0) {
                result = ColorRemovableAppInfo.CREATOR.createFromParcel(reply);
            } else {
                result = null;
            }
            return result;
        } finally {
            reply.recycle();
            data.recycle();
        }
    }

    @Override // android.content.pm.IColorPackageManager
    public boolean restoreRemovableApp(String packageName, IntentSender sender, Bundle bundle) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorBasePackageManager.DESCRIPTOR);
            data.writeString(packageName);
            boolean result = true;
            if (sender != null) {
                data.writeInt(1);
                sender.writeToParcel(data, 0);
            } else {
                data.writeInt(0);
            }
            if (bundle != null) {
                data.writeInt(1);
                bundle.writeToParcel(data, 0);
            } else {
                data.writeInt(0);
            }
            this.mRemote.transact(10025, data, reply, 0);
            reply.readException();
            if (reply.readInt() == 0) {
                result = false;
            }
            return result;
        } finally {
            reply.recycle();
            data.recycle();
        }
    }

    @Override // android.content.pm.IColorPackageManager
    public void checkEMMApkRuntimePermission(ComponentName cn2) throws SecurityException {
        String packageName = cn2.getPackageName();
        if (packageName != null) {
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            try {
                data.writeInterfaceToken(IColorBasePackageManager.DESCRIPTOR);
                data.writeString(packageName);
                this.mRemote.transact(10026, data, reply, 0);
                reply.readException();
                String ret = reply.readString();
                Log.d("ColorPackageManager", "check EMM apk runtime permission:" + ret);
                if ("".equals(ret)) {
                    reply.recycle();
                    data.recycle();
                    return;
                }
                throw new SecurityException(ret);
            } catch (RemoteException e) {
                throw new SecurityException(e.getMessage());
            } catch (Throwable th) {
                reply.recycle();
                data.recycle();
                throw th;
            }
        } else {
            throw new SecurityException("Package name is null");
        }
    }

    @Override // android.content.pm.IColorPackageManager
    public boolean isSupportSessionWrite() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorBasePackageManager.DESCRIPTOR);
            this.mRemote.transact(10027, data, reply, 0);
            reply.readException();
            return Boolean.valueOf(reply.readString()).booleanValue();
        } finally {
            data.recycle();
            reply.recycle();
        }
    }
}
