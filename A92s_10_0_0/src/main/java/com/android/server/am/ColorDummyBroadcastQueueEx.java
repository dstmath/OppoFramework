package com.android.server.am;

import android.content.Intent;
import android.content.pm.ResolveInfo;

public class ColorDummyBroadcastQueueEx implements IColorBroadcastQueueEx {
    final BroadcastQueue mQueue;

    public ColorDummyBroadcastQueueEx(BroadcastQueue queue) {
        this.mQueue = queue;
    }

    @Override // com.android.server.am.IColorBroadcastQueueEx
    public boolean isSkipThisStaticBroadcastReceivers(Intent intent, ResolveInfo info) {
        return true;
    }
}
