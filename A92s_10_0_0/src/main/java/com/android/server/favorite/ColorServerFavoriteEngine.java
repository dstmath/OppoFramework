package com.android.server.favorite;

import android.content.Context;
import com.color.util.ColorLog;

public abstract class ColorServerFavoriteEngine implements IColorServerFavoriteEngine {
    protected static final boolean DBG = true;
    protected final String TAG = getClass().getSimpleName();

    /* access modifiers changed from: protected */
    public abstract void onStartQuery(Context context, String str, ColorServerFavoriteCallback colorServerFavoriteCallback);

    @Override // com.android.server.favorite.IColorServerFavoriteEngine
    public final void startQuery(Context context, String packageName, ColorServerFavoriteCallback callback) {
        ColorLog.d(true, "AnteaterFavorite", "[" + this.TAG + "] startQuery : " + packageName);
        onStartQuery(context, packageName, callback);
    }
}
