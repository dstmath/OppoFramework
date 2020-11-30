package com.android.server.am;

import android.os.SystemClock;
import android.os.UserHandle;
import android.util.SparseArray;
import com.android.server.am.ColorHansManager;
import com.android.server.am.ColorHansPackageSelector;

/* access modifiers changed from: package-private */
/* compiled from: ColorHansManager */
public class HansPreloadFreezerScene extends HansSceneBase implements IHansScene {
    private static final int PRELOAD_RESTRICTIONS = 254;
    private final String FZ_REASON_PRELOAD = ColorResourcePreloadDatabaseHelper.PRELOAD_TABLE_NAME;
    int HANS_PRELOAD_FREEZER_SCENE_IMPORTANCE_FLAG = 0;
    int HANS_PRELOAD_FREEZER_SCENE_TARGET_FLAG = 1;

    HansPreloadFreezerScene() {
    }

    @Override // com.android.server.am.HansSceneBase, com.android.server.am.IHansScene
    public boolean isFreezed(int uid) {
        return super.isFreezed(uid);
    }

    @Override // com.android.server.am.IHansScene
    public void hansFreeze(int uid) {
        freeze(uid, ColorResourcePreloadDatabaseHelper.PRELOAD_TABLE_NAME);
    }

    public boolean preloadFreeze(int uid) {
        return freeze(uid, ColorResourcePreloadDatabaseHelper.PRELOAD_TABLE_NAME);
    }

    @Override // com.android.server.am.IHansScene
    public void hansUnFreeze(int uid, String reason) {
        unfreeze(uid, reason);
    }

    @Override // com.android.server.am.IHansScene
    public void hansFreeze() {
        freeze(ColorResourcePreloadDatabaseHelper.PRELOAD_TABLE_NAME);
    }

    @Override // com.android.server.am.IHansScene
    public void hansUnFreeze(String reason) {
        unfreeze(reason);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.am.HansSceneBase
    public int getScene() {
        return 6;
    }

    @Override // com.android.server.am.HansSceneBase
    public boolean postFreeze(String pkgname, int uid, int level) {
        updateHansUidFirewall(uid, false);
        this.mMainHandler.post(new Runnable(uid) {
            /* class com.android.server.am.$$Lambda$HansPreloadFreezerScene$2aXNMWsCzQ1ZmLR3ihq2fVvOElk */
            private final /* synthetic */ int f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                HansPreloadFreezerScene.this.lambda$postFreeze$0$HansPreloadFreezerScene(this.f$1);
            }
        });
        this.mNativeHandler.postDelayed(new Runnable(uid) {
            /* class com.android.server.am.$$Lambda$HansPreloadFreezerScene$5G0eR60eBUCCbZZocy4fjMG_ew */
            private final /* synthetic */ int f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                HansPreloadFreezerScene.this.lambda$postFreeze$1$HansPreloadFreezerScene(this.f$1);
            }
        }, 2000);
        return true;
    }

    public /* synthetic */ void lambda$postFreeze$0$HansPreloadFreezerScene(int uid) {
        handleExecutingComponent(uid);
        handleWakeLockForHans(uid, true);
    }

    public /* synthetic */ void lambda$postFreeze$1$HansPreloadFreezerScene(int uid) {
        hansTalkWithNative(uid, 3);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.am.HansSceneBase
    public boolean unfreeze(int uid, String reason) {
        synchronized (this.mHansLock) {
            ColorHansPackageSelector.HansPackage packageState = (ColorHansPackageSelector.HansPackage) this.mManagedMap.get(uid);
            if (packageState != null) {
                if (packageState.getFreezed()) {
                    updateHansUidFirewall(uid, true);
                    boolean isSuccessful = sendHansSignal(packageState, 2);
                    if (!isSuccessful) {
                        unfreezeForFrozenPids(packageState);
                        isSuccessful = true;
                    }
                    if (isSuccessful) {
                        ColorHansManager.HansLogger hansLogger = this.mHansLogger;
                        hansLogger.i("unfreeze uid: " + uid + " package: " + packageState.getPkgName() + " reason: " + reason + " scene: " + ColorHansManager.getInstance().coverSceneIDtoStr(getScene()));
                        this.mHansLogger.addUFZInfo(reason, uid, packageState.getPkgName(), packageState.getFreezeTime());
                        ColorHansManager.getInstance().notifyUnFreezeReason(uid, packageState.getPkgName(), reason, ColorHansManager.getInstance().getHansSceneName());
                        ColorHansManager.getInstance().notifyFreezeTime(uid, packageState.getPkgName(), SystemClock.elapsedRealtime() - packageState.getFreezeElapsedTime());
                        packageState.setFreezed(false);
                        packageState.setUnFreezeTime(System.currentTimeMillis());
                        packageState.setUnFreezeReason(reason);
                        updateFreezedPkgMap(packageState, false);
                        postUnFreeze(packageState.getUid(), packageState.getPkgName(), reason);
                        if (ColorHansManager.getInstance().isPreloadPkg(packageState.getPkgName(), UserHandle.getUserId(uid)) && !reason.equals(ColorHansManager.HANS_UFZ_REASON_ACTIVITY) && !reason.equals(ColorHansManager.HANS_UFZ_REASON_TOP_ACTIVITY)) {
                            this.mMainHandler.postDelayed(new Runnable(packageState.getPkgName(), uid) {
                                /* class com.android.server.am.$$Lambda$HansPreloadFreezerScene$Q3B4cS7Hwefn9TFIPQEsTbdnWws */
                                private final /* synthetic */ String f$1;
                                private final /* synthetic */ int f$2;

                                {
                                    this.f$1 = r2;
                                    this.f$2 = r3;
                                }

                                public final void run() {
                                    HansPreloadFreezerScene.this.lambda$unfreeze$2$HansPreloadFreezerScene(this.f$1, this.f$2);
                                }
                            }, 1000);
                        }
                    }
                    return isSuccessful;
                }
            }
            return false;
        }
    }

    public /* synthetic */ void lambda$unfreeze$2$HansPreloadFreezerScene(String pkgName, int uid) {
        if (ColorHansManager.getInstance().isPreloadPkg(pkgName, UserHandle.getUserId(uid))) {
            hansFreeze(uid);
        }
    }

    @Override // com.android.server.am.IHansScene
    public void onInit() {
        this.mRestriction = new ColorHansRestriction(PRELOAD_RESTRICTIONS);
        this.mManagedMap = ColorHansPackageSelector.getInstance().getHansPackageMap(this.HANS_PRELOAD_FREEZER_SCENE_TARGET_FLAG);
        this.mImportantFlag = this.HANS_PRELOAD_FREEZER_SCENE_IMPORTANCE_FLAG;
    }

    @Override // com.android.server.am.IHansScene
    public ColorHansRestriction getHansRestriction() {
        return this.mRestriction;
    }

    @Override // com.android.server.am.IHansScene
    public void updateTargetMap(int updateType, ColorHansPackageSelector.HansPackage hansPackage) {
        synchronized (this.mHansLock) {
            int i = 0;
            if (1 == updateType) {
                try {
                    SparseArray<ColorHansPackageSelector.HansPackage> addList = ColorHansPackageSelector.getInstance().mTmpAddThirdAppList;
                    SparseArray<ColorHansPackageSelector.HansPackage> rmList = ColorHansPackageSelector.getInstance().mTmpRmThirdAppList;
                    for (int i2 = 0; i2 < addList.size(); i2++) {
                        int uid = addList.keyAt(i2);
                        if (this.mManagedMap.get(uid) == null) {
                            this.mManagedMap.put(uid, addList.valueAt(i2));
                        }
                    }
                    while (i < rmList.size()) {
                        int uid2 = rmList.keyAt(i);
                        ColorHansPackageSelector.HansPackage oldHansPkg = (ColorHansPackageSelector.HansPackage) this.mManagedMap.get(uid2);
                        if (oldHansPkg != null && oldHansPkg.getFreezed()) {
                            hansUnFreeze(uid2, ColorHansManager.HANS_UFZ_REASON_REMOVE_APP);
                        }
                        this.mManagedMap.remove(uid2);
                        i++;
                    }
                } catch (Throwable th) {
                    throw th;
                }
            } else {
                if (!(8 == updateType || 2 == updateType)) {
                    if (4 != updateType) {
                        if (16 == updateType) {
                            SparseArray<ColorHansPackageSelector.HansPackage> gmsList = ColorHansPackageSelector.getInstance().getHansPackageMap(256);
                            while (i < gmsList.size()) {
                                int uid3 = gmsList.keyAt(i);
                                ColorHansPackageSelector.HansPackage gmsHansPackage = gmsList.valueAt(i);
                                if (gmsHansPackage != null) {
                                    if (ColorHansManager.getInstance().getCommonConfig().isRestrictGms()) {
                                        if (this.mManagedMap.get(uid3) == null) {
                                            this.mManagedMap.put(uid3, gmsHansPackage);
                                        }
                                    } else if (this.mManagedMap.get(uid3) != null) {
                                        hansUnFreeze(uid3, ColorHansManager.HANS_UFZ_REASON_GMS);
                                        this.mManagedMap.remove(uid3);
                                    }
                                }
                                i++;
                            }
                        }
                    }
                }
                int mode = hansPackage.getSaveMode();
                int classType = hansPackage.getAppClass();
                if (1 != classType) {
                    if (2 != classType) {
                        if (3 == classType) {
                            if ((mode == 4 || mode == 2) && this.mManagedMap.get(hansPackage.getUid()) == null) {
                                this.mManagedMap.put(hansPackage.getUid(), hansPackage);
                            }
                            if (mode == 8) {
                                this.mManagedMap.remove(hansPackage.getUid());
                            }
                        }
                    }
                }
                ColorHansPackageSelector.HansPackage localHansPkg = (ColorHansPackageSelector.HansPackage) this.mManagedMap.get(hansPackage.getUid());
                if (localHansPkg != null) {
                    if (localHansPkg.getFreezed()) {
                        hansUnFreeze(localHansPkg.getUid(), ColorHansManager.HANS_UFZ_REASON_REMOVE_APP);
                    }
                    this.mManagedMap.remove(localHansPkg.getUid());
                }
            }
        }
    }
}
