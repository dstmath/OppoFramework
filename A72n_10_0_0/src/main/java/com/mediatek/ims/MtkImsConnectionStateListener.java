package com.mediatek.ims;

import android.net.Uri;
import android.telephony.ims.feature.ImsFeature;
import com.android.ims.ImsConnectionStateListener;

public class MtkImsConnectionStateListener extends ImsConnectionStateListener {
    public void onImsEmergencyCapabilityChanged(boolean eccSupport) {
    }

    public void onWifiPdnOOSStateChanged(int oosState) {
    }

    public void onCapabilitiesStatusChanged(ImsFeature.Capabilities config) {
    }

    public void onRegistrationImsStateInd(int indType, Uri[] uris, int expireTime, int errCode, String errMsg) {
    }

    public void onRedirectIncomingCallInd(int phoneId, String[] info) {
    }
}
