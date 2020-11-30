package com.android.server.am;

import android.content.Intent;
import android.content.pm.ResolveInfo;

public interface IColorBroadcastQueueEx {
    boolean isSkipThisStaticBroadcastReceivers(Intent intent, ResolveInfo resolveInfo);
}
