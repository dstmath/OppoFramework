package com.color.favorite;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import java.util.ArrayList;

public abstract class ColorFavoriteCallback implements IColorFavoriteCallback {
    protected static final boolean DBG = false;
    protected final String TAG = getClass().getSimpleName();

    @Override // com.color.favorite.IColorFavoriteCallback
    public Handler createWorkHandler(Context context, String name, int priority) {
        HandlerThread thread = new HandlerThread("Favorite." + name, priority);
        thread.start();
        return new ColorFavoriteHandler(thread, "Client");
    }

    @Override // com.color.favorite.IColorFavoriteCallback
    public void onLoadFinished(Context context, boolean noRule, boolean emptyRule, ArrayList<String> arrayList) {
    }

    @Override // com.color.favorite.IColorFavoriteCallback
    public void onFavoriteStart(Context context, ColorFavoriteResult result) {
    }

    @Override // com.color.favorite.IColorFavoriteCallback
    public void onFavoriteFinished(Context context, ColorFavoriteResult result) {
    }
}
