package com.color.multiapp;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageManager;
import android.content.pm.PackageParser;
import android.content.pm.ProviderInfo;
import android.content.pm.ResolveInfo;
import android.os.RemoteException;
import android.os.UserHandle;
import com.android.internal.app.ResolverActivity;
import java.util.ArrayList;
import java.util.List;

public class ColorMultiAppDummy implements IColorMultiApp {
    @Override // com.color.multiapp.IColorMultiApp
    public boolean isSupportMultiApp() {
        return false;
    }

    @Override // com.color.multiapp.IColorMultiApp
    public List<String> getAllowedMultiApp() {
        return new ArrayList();
    }

    @Override // com.color.multiapp.IColorMultiApp
    public List<String> getCreatedMultiApp() {
        return new ArrayList();
    }

    @Override // com.color.multiapp.IColorMultiApp
    public String getAliasMultiApp(String pkgName) {
        return "";
    }

    @Override // com.color.multiapp.IColorMultiApp
    public boolean isCreatedMultiApp(String pkgName) {
        return false;
    }

    @Override // com.color.multiapp.IColorMultiApp
    public boolean isMultiAppUserId(int userId) {
        return false;
    }

    @Override // com.color.multiapp.IColorMultiApp
    public boolean isMultiAppUri(Intent intent, String pkgName) {
        return false;
    }

    @Override // com.color.multiapp.IColorMultiApp
    public int getCorrectUserId(int userId) {
        return userId;
    }

    @Override // com.color.multiapp.IColorMultiApp
    public List<ApplicationInfo> getInstalledApplications(IPackageManager packageManager, int flags, int userId) throws RemoteException {
        return null;
    }

    @Override // com.color.multiapp.IColorMultiApp
    public ProviderInfo resolveContentProviderAsUser(IPackageManager packageManager, Context context, String name, int flags, int userId) throws RemoteException {
        return null;
    }

    @Override // com.color.multiapp.IColorMultiApp
    public UserHandle getCorrectUserHandle(UserHandle user, String packageName) {
        return user;
    }

    @Override // com.color.multiapp.IColorMultiApp
    public int fixApplicationInfo(int userId, PackageParser.Package pkg) {
        return userId;
    }

    @Override // com.color.multiapp.IColorMultiApp
    public boolean addMultiAppInfo(Intent intent, List<ResolveInfo> list, List<ResolverActivity.ResolvedComponentInfo> list2) {
        return false;
    }
}
