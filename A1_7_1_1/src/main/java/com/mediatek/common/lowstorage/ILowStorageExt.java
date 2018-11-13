package com.mediatek.common.lowstorage;

import android.content.Context;

public interface ILowStorageExt {
    void checkStorage(long j);

    void init(Context context, long j);
}
