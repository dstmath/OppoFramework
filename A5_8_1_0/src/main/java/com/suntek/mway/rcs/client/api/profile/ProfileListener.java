package com.suntek.mway.rcs.client.api.profile;

import com.suntek.mway.rcs.client.aidl.plugin.callback.IProfileListener.Stub;
import com.suntek.mway.rcs.client.aidl.plugin.entity.profile.QRCardImg;

public abstract class ProfileListener extends Stub {
    public void onQRImgGet(QRCardImg qrImgObj, int resultCode, String resultDesc) {
    }
}
