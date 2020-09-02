package com.color.favorite;

import android.content.Context;
import android.os.Handler;
import java.util.ArrayList;

public interface IColorFavoriteCallback {
    Handler createWorkHandler(Context context, String str, int i);

    boolean isSettingOn(Context context);

    void onFavoriteFinished(Context context, ColorFavoriteResult colorFavoriteResult);

    void onFavoriteStart(Context context, ColorFavoriteResult colorFavoriteResult);

    void onLoadFinished(Context context, boolean z, boolean z2, ArrayList<String> arrayList);
}
