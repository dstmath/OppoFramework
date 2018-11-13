package com.oppo.widget;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.ListView;
import com.oppo.widget.OppoDragSortListView.FloatViewManager;

public class OppoSimpleFloatViewManager implements FloatViewManager {
    private Bitmap mFloatBitmap;
    private ImageView mImageView;
    private ListView mListView;

    public OppoSimpleFloatViewManager(ListView lv) {
        this.mListView = lv;
    }

    public void setBackgroundColor(int color) {
    }

    public View onCreateFloatView(int position) {
        View v = this.mListView.getChildAt((this.mListView.getHeaderViewsCount() + position) - this.mListView.getFirstVisiblePosition());
        if (v == null) {
            return null;
        }
        v.setPressed(false);
        v.setDrawingCacheEnabled(true);
        this.mFloatBitmap = Bitmap.createBitmap(v.getDrawingCache());
        v.setDrawingCacheEnabled(false);
        if (this.mImageView == null) {
            this.mImageView = new ImageView(this.mListView.getContext());
        }
        this.mImageView.setBackgroundResource(201852038);
        this.mImageView.setPadding(0, 0, 0, 0);
        this.mImageView.setImageBitmap(this.mFloatBitmap);
        this.mImageView.setLayoutParams(new LayoutParams(v.getWidth(), v.getHeight()));
        return this.mImageView;
    }

    public void onDragFloatView(View floatView, Point position, Point touch) {
    }

    public void onDestroyFloatView(View floatView) {
        ((ImageView) floatView).setImageDrawable(null);
        this.mFloatBitmap.recycle();
        this.mFloatBitmap = null;
    }
}
