package com.android.server.am;

import android.util.SparseArray;
import com.android.server.am.ColorHansPackageSelector;

/* access modifiers changed from: package-private */
/* compiled from: ColorHansManager */
public class HansNightScene extends HansSceneBase implements IHansScene {
    private static final int NIGHT_GLOBAL_RESTRICTIONS = 0;
    private static final int NIGHT_RESTRICTIONS = 254;
    private final String FZ_REASON_NIGHT = "night";
    int HANS_NIGHT_SCENE_IMPORTANCE_FLAG = ColorHansImportance.HANS_IMPORTANT_SCENE_FOR_NIGHT_FREEZE;
    int HANS_NIGHT_SCENE_TARGET_FLAG = 1;
    private Runnable NightRunnable = new Runnable() {
        /* class com.android.server.am.$$Lambda$HansNightScene$lrI3SCN8YFVarDvemBUayw9NBrM */

        public final void run() {
            HansNightScene.this.lambda$new$0$HansNightScene();
        }
    };

    public /* synthetic */ void lambda$new$0$HansNightScene() {
        if (!this.mCommonConfig.isCharging() && !this.mCommonConfig.isScreenOn()) {
            hansFreeze();
            sendRepeatFreeze();
        }
    }

    @Override // com.android.server.am.HansSceneBase, com.android.server.am.IHansScene
    public boolean isFreezed(int uid) {
        return super.isFreezed(uid);
    }

    @Override // com.android.server.am.IHansScene
    public void hansFreeze(int uid) {
        if (ColorHansManager.getInstance().getHansScene().equals(ColorHansManager.getInstance().hansNightScene)) {
            freeze(uid, "night");
        }
    }

    @Override // com.android.server.am.IHansScene
    public void hansUnFreeze(int uid, String reason) {
        unfreeze(uid, reason);
    }

    @Override // com.android.server.am.IHansScene
    public void hansFreeze() {
        if (ColorHansManager.getInstance().getHansScene().equals(ColorHansManager.getInstance().hansNightScene)) {
            freeze("night");
        }
    }

    @Override // com.android.server.am.IHansScene
    public void hansUnFreeze(String reason) {
        unfreeze(reason);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.am.HansSceneBase
    public int getScene() {
        return 5;
    }

    @Override // com.android.server.am.IHansScene
    public void onInit() {
        if (ColorHansManager.getInstance().getCommonConfig().isChinaRegion()) {
            this.mRestriction = new HansNightRestriction(NIGHT_RESTRICTIONS);
        } else {
            this.mRestriction = new HansNightRestriction(0);
        }
        this.mManagedMap = ColorHansPackageSelector.getInstance().getHansPackageMap(this.HANS_NIGHT_SCENE_TARGET_FLAG);
        this.mImportantFlag = this.HANS_NIGHT_SCENE_IMPORTANCE_FLAG;
    }

    @Override // com.android.server.am.IHansScene
    public ColorHansRestriction getHansRestriction() {
        return this.mRestriction;
    }

    public void sendRepeatFreeze() {
        this.mMainHandler.postDelayed(this.NightRunnable, ColorHansPackageSelector.getInstance().getNightRepeatTime());
    }

    public void stopNightTrigger() {
        this.mMainHandler.removeCallbacks(this.NightRunnable);
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

    /* compiled from: ColorHansManager */
    class HansNightRestriction extends ColorHansRestriction {
        public HansNightRestriction(int restrictions) {
            super(restrictions);
        }

        @Override // com.android.server.am.ColorHansRestriction
        public boolean isBlockedServicePolicy(int callingUid, String callingPackage, int uid, String pkgName, String cpnName, boolean isBind, int freezeLevel) {
            if (ColorHansManager.getInstance().getCommonConfig().isDisableNetWork()) {
                return true;
            }
            return super.isBlockedServicePolicy(callingUid, callingPackage, uid, pkgName, cpnName, isBind, freezeLevel);
        }

        @Override // com.android.server.am.ColorHansRestriction
        public boolean isBlockedProviderPolicy(int callingUid, String callingPackage, int uid, String pkgName, String cpnName, int freezeLevel) {
            if (ColorHansManager.getInstance().getCommonConfig().isDisableNetWork()) {
                return true;
            }
            return super.isBlockedProviderPolicy(callingUid, callingPackage, uid, pkgName, cpnName, freezeLevel);
        }

        @Override // com.android.server.am.ColorHansRestriction
        public boolean isBlockedBroadcastPolicy(int callingUid, String callingPackage, int uid, String pkgName, String action, boolean order, int freezeLevel) {
            if (ColorHansManager.getInstance().getCommonConfig().isDisableNetWork()) {
                return true;
            }
            return super.isBlockedBroadcastPolicy(callingUid, callingPackage, uid, pkgName, action, order, freezeLevel);
        }

        @Override // com.android.server.am.ColorHansRestriction
        public boolean isBlockedSyncPolicy(int uid, String pkgName, int freezeLevel) {
            if (ColorHansManager.getInstance().getCommonConfig().isDisableNetWork()) {
                return true;
            }
            return super.isBlockedSyncPolicy(uid, pkgName, freezeLevel);
        }

        @Override // com.android.server.am.ColorHansRestriction
        public boolean isBlockedJobPolicy(int uid, String pkgName, int freezeLevel) {
            if (ColorHansManager.getInstance().getCommonConfig().isDisableNetWork()) {
                return true;
            }
            return super.isBlockedJobPolicy(uid, pkgName, freezeLevel);
        }

        @Override // com.android.server.am.ColorHansRestriction
        public boolean isBlockedBinderPolicy(int callingPid, int uid, String pkgName, String aidlName, int code, boolean oneway) {
            if (ColorHansManager.getInstance().getCommonConfig().isDisableNetWork()) {
                return true;
            }
            return super.isBlockedBinderPolicy(callingPid, uid, pkgName, aidlName, code, oneway);
        }
    }
}
