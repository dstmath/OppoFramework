package com.android.server.om;

import android.common.OppoFeatureCache;
import android.content.Context;
import android.content.pm.PackageInfo;
import com.android.server.ColorServiceFactory;
import com.android.server.SystemService;
import java.util.List;
import java.util.Map;

public class OppoBaseOverlayManagerService extends SystemService {
    protected IColorOverlayManagerServiceEx mColorOmsEx = null;
    protected Context mContext = null;

    protected interface IOppoOMSPackageCache {
        void cachePackageInfo(String str, int i, PackageInfo packageInfo);

        PackageInfo getPackageInfo(String str, int i);
    }

    public OppoBaseOverlayManagerService(Context context) {
        super(context);
        this.mContext = context;
        this.mColorOmsEx = ColorServiceFactory.getInstance().getColorOverlayManagerServiceEx(context);
        this.mColorOmsEx.init();
    }

    @Override // com.android.server.SystemService
    public void onStart() {
    }

    /* access modifiers changed from: protected */
    public void initLanguageManager(OverlayManagerServiceImpl overlayManagerService, IOppoOMSPackageCache packageManager, Object lock) {
        OppoFeatureCache.get(IColorLanguageManager.DEFAULT).init(overlayManagerService, packageManager, lock, this.mContext);
    }

    /* access modifiers changed from: protected */
    public void updateLanguagePath(String targetPackageName, int userId, Map<String, List<String>> pendingChanges) {
        OppoFeatureCache.get(IColorLanguageManager.DEFAULT).updateLanguagePath(targetPackageName, userId, pendingChanges);
    }

    protected static class Language {
        boolean flag;
        String overlay;

        protected Language() {
        }
    }
}
