package com.android.server.wm;

import android.common.OppoFeatureCache;
import com.android.server.am.IColorMultiAppManager;
import java.util.List;

@Deprecated
public class OppoMultiAppManagerUtil {
    private static OppoMultiAppManagerUtil instance = new OppoMultiAppManagerUtil();

    public static OppoMultiAppManagerUtil getInstance() {
        return instance;
    }

    public boolean isMultiAllowedApp(String pkgName) {
        return OppoFeatureCache.get(IColorMultiAppManager.DEFAULT).isMultiAllowedApp(pkgName);
    }

    public List<String> getAllowedMultiApp() {
        return OppoFeatureCache.get(IColorMultiAppManager.DEFAULT).getAllowedMultiApp();
    }

    public List<String> getCreatedMultiApp() {
        return OppoFeatureCache.get(IColorMultiAppManager.DEFAULT).getCreatedMultiApp();
    }

    public String getAliasByPackage(String pkgName) {
        return OppoFeatureCache.get(IColorMultiAppManager.DEFAULT).getAliasMultiApp(pkgName);
    }

    public boolean isMultiApp(String pkgName) {
        return OppoFeatureCache.get(IColorMultiAppManager.DEFAULT).isMultiApp(pkgName);
    }

    public boolean isMainApp(int userId, String pkgName) {
        if (userId == 999 || pkgName == null) {
            return false;
        }
        return OppoFeatureCache.get(IColorMultiAppManager.DEFAULT).isMultiApp(pkgName);
    }

    public boolean isMultiApp(int userId, String pkgName) {
        return OppoFeatureCache.get(IColorMultiAppManager.DEFAULT).isMultiApp(userId, pkgName);
    }

    public void addToCreatedMultiApp(String pkgName) {
        OppoFeatureCache.get(IColorMultiAppManager.DEFAULT).addToCreatedMultiApp(pkgName);
    }

    public void removeFromCreatedMultiApp(String pkgName) {
        OppoFeatureCache.get(IColorMultiAppManager.DEFAULT).removeFromCreatedMultiApp(pkgName);
    }
}
