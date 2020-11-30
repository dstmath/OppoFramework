package com.android.server.am;

import android.common.OppoFeatureCache;
import android.content.Intent;
import android.content.pm.ResolveInfo;

public class ColorBroadcastQueueEx extends ColorDummyBroadcastQueueEx {
    public ColorBroadcastQueueEx(BroadcastQueue queue) {
        super(queue);
    }

    public boolean isSkipThisStaticBroadcastReceivers(Intent intent, ResolveInfo info) {
        return OppoFeatureCache.get(IColorBroadcastStaticRegisterWhitelistManager.DEFAULT).isSkipThisStaticBroadcastReceivers(intent, info);
    }
}
