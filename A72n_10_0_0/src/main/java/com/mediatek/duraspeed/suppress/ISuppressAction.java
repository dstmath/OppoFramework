package com.mediatek.duraspeed.suppress;

import android.content.Context;

public interface ISuppressAction {
    void addToSuppressRestartList(Context context, String str);

    boolean notRemoveAlarm(String str);

    boolean onBeforeStartProcessForStaticReceiver(String str);

    String onReadyToStartComponent(String str, int i, String str2, String str3);
}
