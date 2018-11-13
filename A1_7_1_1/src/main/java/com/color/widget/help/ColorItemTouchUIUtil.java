package com.color.widget.help;

import android.graphics.Canvas;
import android.view.View;
import com.color.widget.ColorRecyclerView;

public interface ColorItemTouchUIUtil {
    void clearView(View view);

    void onDraw(Canvas canvas, ColorRecyclerView colorRecyclerView, View view, float f, float f2, int i, boolean z);

    void onDrawOver(Canvas canvas, ColorRecyclerView colorRecyclerView, View view, float f, float f2, int i, boolean z);

    void onSelected(View view);
}
