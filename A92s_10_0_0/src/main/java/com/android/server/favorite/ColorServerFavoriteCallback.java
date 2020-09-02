package com.android.server.favorite;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import com.color.favorite.ColorFavoriteHandler;

public abstract class ColorServerFavoriteCallback implements IColorServerFavoriteCallback {
    protected static final boolean DBG = true;
    protected final String TAG = getClass().getSimpleName();

    @Override // com.android.server.favorite.IColorServerFavoriteCallback
    public Handler createWorkHandler(Context context, String name, int priority) {
        HandlerThread thread = new HandlerThread("Favorite." + name, priority);
        thread.start();
        return new ColorFavoriteHandler(thread, "Server");
    }
}
