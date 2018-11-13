package com.mediatek.common.telephony;

import android.content.Context;

public interface ISupplementaryServiceExt {
    String getOpDefaultQueryCfuMode();

    void registerReceiver(Context context, int i);

    void unRegisterReceiver(int i);
}
