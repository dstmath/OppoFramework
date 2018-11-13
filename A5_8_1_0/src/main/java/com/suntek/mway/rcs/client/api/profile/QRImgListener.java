package com.suntek.mway.rcs.client.api.profile;

import com.suntek.mway.rcs.client.aidl.plugin.callback.IProfileListener.Stub;
import com.suntek.mway.rcs.client.aidl.plugin.entity.profile.Avatar;
import com.suntek.mway.rcs.client.aidl.plugin.entity.profile.Profile;
import com.suntek.mway.rcs.client.api.log.LogHelper;

public abstract class QRImgListener extends Stub {
    public void onProfileUpdated(int resultCode, String resultDesc) {
        LogHelper.d("onProfileUpdated(): resultCode --> " + resultCode + "resultDesc = " + resultDesc);
    }

    public void onAvatarUpdated(int resultCode, String resultDesc) {
        LogHelper.d("onAvatarUpdated(): resultCode --> " + resultCode + "resultDesc = " + resultDesc);
    }

    public void onAvatarGet(Avatar avatar, int resultCode, String resultDesc) {
        LogHelper.d("onAvatarGet(): resultCode --> " + resultCode + "resultDesc = " + resultDesc);
    }

    public void onProfileGet(Profile profile, int resultCode, String resultDesc) {
        LogHelper.d("onProfileGet(): resultCode --> " + resultCode + "resultDesc = " + resultDesc);
    }
}
