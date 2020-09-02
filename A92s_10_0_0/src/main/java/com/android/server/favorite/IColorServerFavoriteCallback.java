package com.android.server.favorite;

import android.content.Context;
import android.os.Handler;
import android.os.IBinder;

public interface IColorServerFavoriteCallback {
    Handler createWorkHandler(Context context, String str, int i);

    void linkBinderToDeath(IBinder.DeathRecipient deathRecipient);

    void onQueryResult(Context context, String str);

    void unlinkBinderToDeath(IBinder.DeathRecipient deathRecipient);
}
