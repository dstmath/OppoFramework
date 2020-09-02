package com.color.screenshot;

import android.content.Context;

public interface ColorLongshotViewBase {
    boolean canLongScroll();

    int computeLongScrollExtent();

    int computeLongScrollOffset();

    int computeLongScrollRange();

    boolean findViewsLongshotInfo(ColorLongshotViewInfo colorLongshotViewInfo);

    Context getContext();

    boolean isLongshotVisibleToUser();
}
