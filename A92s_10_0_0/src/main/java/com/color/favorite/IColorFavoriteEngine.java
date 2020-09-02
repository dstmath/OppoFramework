package com.color.favorite;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.view.View;

public interface IColorFavoriteEngine extends IColorFavoriteConstans {
    Handler getWorkHandler();

    void init();

    boolean isDebugOn();

    boolean isLogOn();

    boolean isTestOn();

    void loadRule(Context context, String str, ColorFavoriteCallback colorFavoriteCallback);

    void logActivityInfo(Activity activity);

    void logViewInfo(View view);

    void processClick(View view, ColorFavoriteCallback colorFavoriteCallback);

    void processCrawl(View view, ColorFavoriteCallback colorFavoriteCallback);

    void processSave(View view, ColorFavoriteCallback colorFavoriteCallback);

    void release();
}
