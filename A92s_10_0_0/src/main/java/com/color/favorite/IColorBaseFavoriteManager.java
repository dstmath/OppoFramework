package com.color.favorite;

import android.app.Activity;
import android.common.IOppoCommonFeature;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import java.util.List;

public interface IColorBaseFavoriteManager extends IColorFavoriteConstans, IOppoCommonFeature {
    public static final String NAME = "ColorFavoriteManager";

    Handler getWorkHandler();

    void init(Context context);

    boolean isSaved(Context context, String str, List<Bundle> list);

    void logActivityInfo(Activity activity);

    void logViewInfo(View view);

    void processClick(View view, ColorFavoriteCallback colorFavoriteCallback);

    void processCrawl(View view, ColorFavoriteCallback colorFavoriteCallback);

    void processSave(View view, ColorFavoriteCallback colorFavoriteCallback);

    void release();

    void setEngine(ColorFavoriteEngines colorFavoriteEngines);
}
