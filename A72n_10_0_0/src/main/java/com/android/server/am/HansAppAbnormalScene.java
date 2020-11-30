package com.android.server.am;

import com.android.server.am.ColorHansPackageSelector;

/* access modifiers changed from: package-private */
/* compiled from: ColorHansManager */
public class HansAppAbnormalScene extends HansSceneBase implements IHansScene {
    int HANS_APP_ABNORMAL_SCENE_IMPORTANCE_FLAG = 221183;

    @Override // com.android.server.am.HansSceneBase, com.android.server.am.IHansScene
    public boolean isFreezed(int uid) {
        return super.isFreezed(uid);
    }

    @Override // com.android.server.am.IHansScene
    public void hansFreeze(int uid) {
    }

    @Override // com.android.server.am.IHansScene
    public void hansUnFreeze(int uid, String reason) {
        unfreeze(uid, reason);
    }

    @Override // com.android.server.am.IHansScene
    public void hansFreeze() {
    }

    @Override // com.android.server.am.IHansScene
    public void hansUnFreeze(String reason) {
        unfreeze(reason);
    }

    @Override // com.android.server.am.IHansScene
    public void onInit() {
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.am.HansSceneBase
    public int getScene() {
        return 3;
    }

    @Override // com.android.server.am.IHansScene
    public ColorHansRestriction getHansRestriction() {
        return this.mRestriction;
    }

    @Override // com.android.server.am.IHansScene
    public void updateTargetMap(int updateType, ColorHansPackageSelector.HansPackage hansPackage) {
    }

    public void enterAbnormalFreeze(int uid) {
    }
}
