package com.android.server.am;

import android.os.SystemClock;
import android.util.SparseArray;
import com.android.server.am.ColorHansManager;
import com.android.server.am.ColorHansPackageSelector;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

/* access modifiers changed from: package-private */
/* compiled from: ColorHansManager */
public class HansFastFreezerScene extends HansSceneBase implements IHansScene {
    private static final int FAST_FREEZER_RESTRICTIONS = 128;
    private final String FZ_REASON_FAST_FREEZER = "fastfreezer";
    int HANS_FAST_FREEZER_SCENE_TARGET_FLAG = 7;
    int HANS_FAST_FREEZER_SCENE_WHITELIST_TARGET_FALG = 6;
    int HANS_FAST_FREEZE_SCENE_IMPORTANCE_FLAG = ColorHansImportance.HANS_IMPORTANT_FOR_FAST_FREEZE;
    boolean mFastFreezerOn = false;
    protected SparseArray<ColorHansPackageSelector.HansPackage> mWhiteListManagedMap = null;
    private Runnable r = null;

    @Override // com.android.server.am.HansSceneBase, com.android.server.am.IHansScene
    public boolean isFreezed(int uid) {
        return super.isFreezed(uid);
    }

    @Override // com.android.server.am.IHansScene
    public void hansFreeze(int uid) {
        if (ColorHansManager.getInstance().getHansScene().equals(ColorHansManager.getInstance().hansFastFreezerScene)) {
            freeze(uid, "fastfreezer");
        }
    }

    @Override // com.android.server.am.IHansScene
    public void hansUnFreeze(int uid, String reason) {
        unfreeze(uid, reason);
    }

    @Override // com.android.server.am.IHansScene
    public void hansFreeze() {
        if (!ColorHansManager.getInstance().getHansScene().equals(ColorHansManager.getInstance().hansFastFreezerScene)) {
            this.mHansLogger.i("scene is different...");
            return;
        }
        freeze("fastfreezer|" + ColorHansManager.getInstance().getCurResumeUid());
    }

    @Override // com.android.server.am.IHansScene
    public void hansUnFreeze(String reason) {
        if (this.mFastFreezerOn) {
            unfreezeForFastFreezer(reason);
            this.mFastFreezerOn = false;
            ColorHansManager.HansLogger hansLogger = this.mHansLogger;
            hansLogger.i("exit fastfreezer, reason:" + reason);
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.am.HansSceneBase
    public int getScene() {
        return 4;
    }

    @Override // com.android.server.am.IHansScene
    public void onInit() {
        this.mManagedMap = ColorHansPackageSelector.getInstance().getHansPackageMap(this.HANS_FAST_FREEZER_SCENE_TARGET_FLAG);
        this.mWhiteListManagedMap = ColorHansPackageSelector.getInstance().getHansPackageMap(this.HANS_FAST_FREEZER_SCENE_WHITELIST_TARGET_FALG);
        this.mRestriction = new HansFastFreezerRestriction(128);
        this.mImportantFlag = this.HANS_FAST_FREEZE_SCENE_IMPORTANCE_FLAG;
        this.logFilterList = new ArrayList(Arrays.asList("S-FDirect", "S-FTimeout", "fastfreezer"));
    }

    @Override // com.android.server.am.IHansScene
    public ColorHansRestriction getHansRestriction() {
        return this.mRestriction;
    }

    @Override // com.android.server.am.IHansScene
    public void updateTargetMap(int updateType, ColorHansPackageSelector.HansPackage hansPackage) {
        synchronized (this.mHansLock) {
            if (1 == updateType) {
                try {
                    SparseArray<ColorHansPackageSelector.HansPackage> addList = ColorHansPackageSelector.getInstance().mTmpAddThirdAppList;
                    SparseArray<ColorHansPackageSelector.HansPackage> rmList = ColorHansPackageSelector.getInstance().mTmpRmThirdAppList;
                    SparseArray<ColorHansPackageSelector.HansPackage> addThirdList = ColorHansPackageSelector.getInstance().mTmpAddThirdWhiteList;
                    SparseArray<ColorHansPackageSelector.HansPackage> rmThirdList = ColorHansPackageSelector.getInstance().mTmpRmThirdWhiteList;
                    SparseArray<ColorHansPackageSelector.HansPackage> addOppoList = ColorHansPackageSelector.getInstance().mTmpAddOppoWhiteList;
                    SparseArray<ColorHansPackageSelector.HansPackage> rmOppoList = ColorHansPackageSelector.getInstance().mTmpRmOppoWhiteList;
                    updateManagedMap(addList, rmList, this.mManagedMap);
                    updateManagedMap(addThirdList, rmThirdList, this.mManagedMap);
                    updateManagedMap(addOppoList, rmOppoList, this.mManagedMap);
                    updateManagedMap(addThirdList, rmThirdList, this.mWhiteListManagedMap);
                    updateManagedMap(addOppoList, rmOppoList, this.mWhiteListManagedMap);
                } catch (Throwable th) {
                    throw th;
                }
            } else {
                if (!(8 == updateType || 2 == updateType)) {
                    if (4 != updateType) {
                        if (16 == updateType) {
                            SparseArray<ColorHansPackageSelector.HansPackage> gmsList = ColorHansPackageSelector.getInstance().getHansPackageMap(256);
                            for (int i = 0; i < gmsList.size(); i++) {
                                int uid = gmsList.keyAt(i);
                                ColorHansPackageSelector.HansPackage gmsHansPackage = gmsList.valueAt(i);
                                if (gmsHansPackage != null) {
                                    if (ColorHansManager.getInstance().getCommonConfig().isRestrictGms()) {
                                        if (this.mManagedMap.get(uid) == null) {
                                            this.mManagedMap.put(uid, gmsHansPackage);
                                        }
                                    } else if (this.mManagedMap.get(uid) != null) {
                                        hansUnFreeze(uid, ColorHansManager.HANS_UFZ_REASON_GMS);
                                        this.mManagedMap.remove(uid);
                                    }
                                }
                            }
                        }
                    }
                }
                int mode = hansPackage.getSaveMode();
                int classType = hansPackage.getAppClass();
                if (1 != classType) {
                    if (2 != classType) {
                        if (3 == classType) {
                            if (mode == 4 || mode == 2 || mode == 16) {
                                ColorHansPackageSelector.HansPackage localHansPkg = (ColorHansPackageSelector.HansPackage) this.mManagedMap.get(hansPackage.getUid());
                                if (localHansPkg == null) {
                                    this.mManagedMap.put(hansPackage.getUid(), hansPackage);
                                } else {
                                    localHansPkg.cloneHans(hansPackage);
                                }
                                if (this.mWhiteListManagedMap.get(hansPackage.getUid()) != null) {
                                    this.mWhiteListManagedMap.remove(hansPackage.getUid());
                                }
                            }
                            if (mode == 8) {
                                this.mManagedMap.remove(hansPackage.getUid());
                                this.mWhiteListManagedMap.remove(hansPackage.getUid());
                            }
                        }
                    }
                }
                if (mode == 4 || mode == 1 || mode == 16) {
                    ColorHansPackageSelector.HansPackage localHansPkg2 = (ColorHansPackageSelector.HansPackage) this.mManagedMap.get(hansPackage.getUid());
                    if (localHansPkg2 == null) {
                        this.mManagedMap.put(hansPackage.getUid(), hansPackage);
                    } else {
                        localHansPkg2.cloneHans(hansPackage);
                    }
                    if (this.mWhiteListManagedMap.get(hansPackage.getUid()) == null) {
                        this.mWhiteListManagedMap.put(hansPackage.getUid(), hansPackage);
                    }
                }
                if (mode == 8) {
                    this.mManagedMap.remove(hansPackage.getUid());
                    this.mWhiteListManagedMap.remove(hansPackage.getUid());
                }
            }
        }
    }

    private void updateManagedMap(SparseArray<ColorHansPackageSelector.HansPackage> addList, SparseArray<ColorHansPackageSelector.HansPackage> rmList, SparseArray<ColorHansPackageSelector.HansPackage> managedList) {
        for (int i = 0; i < addList.size(); i++) {
            int uid = addList.keyAt(i);
            if (managedList.get(uid) == null) {
                managedList.put(uid, addList.valueAt(i));
            }
        }
        for (int i2 = 0; i2 < rmList.size(); i2++) {
            int uid2 = rmList.keyAt(i2);
            ColorHansPackageSelector.HansPackage hansPackage = managedList.get(uid2);
            if (hansPackage != null && hansPackage.getFreezed()) {
                hansUnFreeze(uid2, ColorHansManager.HANS_UFZ_REASON_REMOVE_APP);
            }
            managedList.remove(uid2);
        }
    }

    public void enterFastFreezer(int uid) {
        if (!this.mFastFreezerOn) {
            this.mHansLogger.i("enter fastfreezer");
            String before = ColorHansManager.getInstance().formatDateTime(System.currentTimeMillis());
            this.mFastFreezerOn = true;
            hansFreeze();
            String after = ColorHansManager.getInstance().formatDateTime(System.currentTimeMillis());
            ColorHansManager.HansLogger hansLogger = this.mHansLogger;
            hansLogger.d("fastfreezer:" + before + " ~ " + after);
            this.r = new Runnable() {
                /* class com.android.server.am.$$Lambda$HansFastFreezerScene$lOFq0G8SGh2n8tJrNSEvbiOwNh4 */

                public final void run() {
                    HansFastFreezerScene.this.lambda$enterFastFreezer$0$HansFastFreezerScene();
                }
            };
            this.mMainHandler.postDelayed(this.r, ColorHansPackageSelector.getInstance().getFastFreezeTimeout());
        }
    }

    public /* synthetic */ void lambda$enterFastFreezer$0$HansFastFreezerScene() {
        hansUnFreeze(ColorHansManager.HANS_UFZ_REASON_FASTFREEZER_TIMEOUT);
        if (ColorHansManager.getInstance().getCommonConfig().isScreenOn()) {
            ColorHansManager.getInstance().hansChangeScene(1);
            ColorHansManager.getInstance().hansLcdOnScene.enterStateMachine(-1, "S-FTimeout");
        }
    }

    public void exitFasterFreezer() {
        this.mMainHandler.removeCallbacks(this.r);
        hansUnFreeze(ColorHansManager.HANS_UFZ_REASON_FASTFREEZER_DIRECT);
    }

    public boolean isFastFreezeOn() {
        return this.mFastFreezerOn;
    }

    @Override // com.android.server.am.HansSceneBase
    public boolean postFreeze(String pkgname, int uid, int level) {
        return false;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.am.HansSceneBase
    public boolean unfreeze(int uid, String reason) {
        synchronized (this.mHansLock) {
            ColorHansPackageSelector.HansPackage packageState = (ColorHansPackageSelector.HansPackage) this.mManagedMap.get(uid);
            if (packageState != null) {
                if (packageState.getFreezed()) {
                    if (ColorHansManager.mPendingUFZReasonList == null || !ColorHansManager.mPendingUFZReasonList.contains(reason)) {
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
                        }
                        return isSuccessful;
                    }
                    packageState.setPendUFZ(true);
                    packageState.setPendingReason(reason);
                    return true;
                }
            }
            return false;
        }
    }

    private boolean unfreezeLocked(ColorHansPackageSelector.HansPackage packageState, String reason) {
        if (packageState == null || !packageState.getFreezed()) {
            return false;
        }
        updateHansUidFirewall(packageState.getUid(), true);
        boolean isSuccessful = sendHansSignal(packageState, 2);
        if (!isSuccessful) {
            unfreezeForFrozenPids(packageState);
            isSuccessful = true;
        }
        if (isSuccessful) {
            ColorHansManager.HansLogger hansLogger = this.mHansLogger;
            hansLogger.i("unfreeze uid: " + packageState.getUid() + " package: " + packageState.getPkgName() + " reason: " + reason + " scene: " + ColorHansManager.getInstance().coverSceneIDtoStr(getScene()));
            packageState.setFreezed(false);
            packageState.setUnFreezeTime(System.currentTimeMillis());
            packageState.setUnFreezeReason(reason);
            packageState.getStateMachine().stateToD(packageState.getUid());
            updateFreezedPkgMap(packageState, false);
            postUnFreeze(packageState.getUid(), packageState.getPkgName(), reason);
        }
        return isSuccessful;
    }

    private void unfreezeForFastFreezer(String reason) {
        ArrayList<String> uids = new ArrayList<>();
        ArrayList<Integer> tempList = new ArrayList<>();
        synchronized (this.mHansLock) {
            for (int i = 0; i < this.mWhiteListManagedMap.size(); i++) {
                this.mWhiteListManagedMap.keyAt(i);
                ColorHansPackageSelector.HansPackage hansPackage = this.mWhiteListManagedMap.valueAt(i);
                if (hansPackage != null) {
                    if (hansPackage.getFreezed()) {
                        if (unfreezeLocked(hansPackage, reason)) {
                            uids.add(String.valueOf(hansPackage.getUid()));
                        }
                        updateFreezedPkgMap(hansPackage, false);
                    }
                }
            }
            for (int j = this.mFreezedManagedMap.size() - 1; j >= 0; j--) {
                int uid = this.mFreezedManagedMap.keyAt(j);
                ColorHansPackageSelector.HansPackage hansPackage2 = (ColorHansPackageSelector.HansPackage) this.mFreezedManagedMap.valueAt(j);
                if (hansPackage2.isPendUFZ()) {
                    String pendingReason = "F-P" + hansPackage2.getPendingReason();
                    if (unfreezeLocked(hansPackage2, pendingReason)) {
                        this.mHansLogger.addUFZInfo(pendingReason, uid, hansPackage2.getPkgName(), hansPackage2.getFreezeTime());
                    }
                    hansPackage2.setPendUFZ(false);
                    tempList.add(Integer.valueOf(hansPackage2.getUid()));
                } else if (hansPackage2.getScene() == 4) {
                    if (unfreezeLocked(hansPackage2, reason)) {
                        uids.add(String.valueOf(hansPackage2.getUid()));
                    }
                    tempList.add(Integer.valueOf(hansPackage2.getUid()));
                }
            }
            Iterator<Integer> it = tempList.iterator();
            while (it.hasNext()) {
                this.mFreezedManagedMap.remove(it.next().intValue());
            }
        }
        if (uids.size() != 0) {
            this.mHansLogger.addAllUFZInfo(reason, uids);
        }
    }

    /* compiled from: ColorHansManager */
    class HansFastFreezerRestriction extends ColorHansRestriction {
        public HansFastFreezerRestriction(int restrictions) {
            super(restrictions);
        }

        @Override // com.android.server.am.ColorHansRestriction
        public boolean isBlockedBinderPolicy(int callingPid, int uid, String pkgName, String aidlName, int code, boolean oneway) {
            if (!oneway) {
                return false;
            }
            return true;
        }
    }
}
