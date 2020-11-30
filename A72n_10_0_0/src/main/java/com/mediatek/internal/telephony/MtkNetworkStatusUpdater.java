package com.mediatek.internal.telephony;

import com.android.internal.telephony.Phone;

public class MtkNetworkStatusUpdater {
    private static MtkNetworkStatusUpdater sMtkNetworkStatusUpdater;

    public static MtkNetworkStatusUpdater init(Phone[] phones, int numPhones) {
        MtkNetworkStatusUpdater mtkNetworkStatusUpdater;
        synchronized (MtkNetworkStatusUpdater.class) {
            if (sMtkNetworkStatusUpdater == null) {
                sMtkNetworkStatusUpdater = new MtkNetworkStatusUpdater(phones, numPhones);
            }
            mtkNetworkStatusUpdater = sMtkNetworkStatusUpdater;
        }
        return mtkNetworkStatusUpdater;
    }

    public MtkNetworkStatusUpdater(Phone[] phones, int numPhones) {
        for (int i = 0; i < numPhones; i++) {
            phones[i].getServiceStateTracker().pollState();
        }
    }
}
