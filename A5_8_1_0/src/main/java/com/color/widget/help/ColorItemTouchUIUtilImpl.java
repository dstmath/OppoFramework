package com.color.widget.help;

import android.graphics.Canvas;
import android.view.View;
import com.color.widget.ColorRecyclerView;

class ColorItemTouchUIUtilImpl {

    static class Gingerbread implements ColorItemTouchUIUtil {
        Gingerbread() {
        }

        private void draw(Canvas c, ColorRecyclerView parent, View view, float dX, float dY) {
            c.save();
            c.translate(dX, dY);
            parent.drawChild(c, view, 0);
            c.restore();
        }

        public void clearView(View view) {
            view.setVisibility(0);
        }

        public void onSelected(View view) {
            view.setVisibility(4);
        }

        public void onDraw(Canvas c, ColorRecyclerView recyclerView, View view, float dX, float dY, int actionState, boolean isCurrentlyActive) {
            if (actionState != 2) {
                draw(c, recyclerView, view, dX, dY);
            }
        }

        public void onDrawOver(Canvas c, ColorRecyclerView recyclerView, View view, float dX, float dY, int actionState, boolean isCurrentlyActive) {
            if (actionState == 2) {
                draw(c, recyclerView, view, dX, dY);
            }
        }
    }

    static class Honeycomb implements ColorItemTouchUIUtil {
        Honeycomb() {
        }

        public void clearView(View view) {
            view.setTranslationX(0.0f);
            view.setTranslationY(0.0f);
        }

        public void onSelected(View view) {
        }

        public void onDraw(Canvas c, ColorRecyclerView recyclerView, View view, float dX, float dY, int actionState, boolean isCurrentlyActive) {
            view.setTranslationX(dX);
            view.setTranslationY(dY);
        }

        public void onDrawOver(Canvas c, ColorRecyclerView recyclerView, View view, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        }
    }

    static class Lollipop extends Honeycomb {
        Lollipop() {
        }

        public void onDraw(Canvas c, ColorRecyclerView recyclerView, View view, float dX, float dY, int actionState, boolean isCurrentlyActive) {
            if (isCurrentlyActive && view.getTag(201458907) == null) {
                Object originalElevation = Float.valueOf(view.getElevation());
                view.setElevation(1.0f + findMaxElevation(recyclerView, view));
                view.setTag(201458907, originalElevation);
            }
            super.onDraw(c, recyclerView, view, dX, dY, actionState, isCurrentlyActive);
        }

        private float findMaxElevation(ColorRecyclerView recyclerView, View itemView) {
            int childCount = recyclerView.getChildCount();
            float max = 0.0f;
            for (int i = 0; i < childCount; i++) {
                View child = recyclerView.getChildAt(i);
                if (child != itemView) {
                    float elevation = child.getElevation();
                    if (elevation > max) {
                        max = elevation;
                    }
                }
            }
            return max;
        }

        public void clearView(View view) {
            Object tag = view.getTag(201458907);
            if (tag != null && (tag instanceof Float)) {
                view.setElevation(((Float) tag).floatValue());
            }
            view.setTag(201458907, null);
            super.clearView(view);
        }
    }

    ColorItemTouchUIUtilImpl() {
    }
}
