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
import java.util.List;

public interface IColorMultiApp {
    public static final int GET_MULTI_APP = 134217728;

    boolean addMultiAppInfo(Intent intent, List<ResolveInfo> list, List<ResolverActivity.ResolvedComponentInfo> list2);

    int fixApplicationInfo(int i, PackageParser.Package v);

    String getAliasMultiApp(String str);

    List<String> getAllowedMultiApp();

    UserHandle getCorrectUserHandle(UserHandle userHandle, String str);

    int getCorrectUserId(int i);

    List<String> getCreatedMultiApp();

    List<ApplicationInfo> getInstalledApplications(IPackageManager iPackageManager, int i, int i2) throws RemoteException;

    boolean isCreatedMultiApp(String str);

    boolean isMultiAppUri(Intent intent, String str);

    boolean isMultiAppUserId(int i);

    boolean isSupportMultiApp();

    ProviderInfo resolveContentProviderAsUser(IPackageManager iPackageManager, Context context, String str, int i, int i2) throws RemoteException;
}
