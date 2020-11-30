package com.android.server.favorite;

import android.content.Context;
import com.color.favorite.IColorFavoriteConstans;

public interface IColorServerFavoriteEngine extends IColorFavoriteConstans {
    void startQuery(Context context, String str, ColorServerFavoriteCallback colorServerFavoriteCallback);
}
