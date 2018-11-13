package com.suntek.rcs.ui.common;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Movie;
import android.os.Build.VERSION;
import android.os.SystemClock;
import android.view.View;
import android.view.View.MeasureSpec;
import android.widget.ImageView;
import com.suntek.mway.rcs.client.aidl.plugin.entity.cloudfile.Config;
import java.io.ByteArrayInputStream;

public class RcsEmojiGifView extends ImageView {
    private int mCurrentMovieTime = 0;
    private Movie mGifMovie;
    private boolean mIsVisible = true;
    private int mMovieHeight;
    private float mMovieLeft;
    private float mMovieScale;
    private long mMovieStart;
    private float mMovieTop;
    private int mMovieWidth;

    public RcsEmojiGifView(Context context) {
        super(context, null, 0);
        if (VERSION.SDK_INT >= 11) {
            setLayerType(1, null);
        }
    }

    public void setMonieByteData(byte[] data) {
        this.mGifMovie = Movie.decodeStream(new ByteArrayInputStream(data));
        requestLayout();
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (this.mGifMovie != null) {
            int movieWidth = this.mGifMovie.width();
            int movieHeight = this.mGifMovie.height();
            float scaleH = 1.0f;
            if (MeasureSpec.getMode(widthMeasureSpec) != 0) {
                int maximumWidth = MeasureSpec.getSize(widthMeasureSpec);
                if (movieWidth > maximumWidth) {
                    scaleH = ((float) movieWidth) / ((float) maximumWidth);
                }
            }
            float scaleW = 1.0f;
            if (MeasureSpec.getMode(heightMeasureSpec) != 0) {
                int maximumHeight = MeasureSpec.getSize(heightMeasureSpec);
                if (movieHeight > maximumHeight) {
                    scaleW = ((float) movieHeight) / ((float) maximumHeight);
                }
            }
            this.mMovieScale = 1.0f / Math.max(scaleH, scaleW);
            this.mMovieWidth = (int) (((float) movieWidth) * this.mMovieScale);
            this.mMovieHeight = (int) (((float) movieHeight) * this.mMovieScale);
            setMeasuredDimension(this.mMovieWidth, this.mMovieHeight);
            return;
        }
        setMeasuredDimension(getSuggestedMinimumWidth(), getSuggestedMinimumHeight());
    }

    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        boolean z = false;
        super.onLayout(changed, l, t, r, b);
        this.mMovieLeft = ((float) (getWidth() - this.mMovieWidth)) / 2.0f;
        this.mMovieTop = ((float) (getHeight() - this.mMovieHeight)) / 2.0f;
        if (getVisibility() == 0) {
            z = true;
        }
        this.mIsVisible = z;
    }

    protected void onDraw(Canvas canvas) {
        if (this.mGifMovie != null) {
            updateMovieTime();
            drawMovieFraming(canvas);
            disableMovieView();
        }
    }

    public void onScreenStateChanged(int screenState) {
        boolean z = true;
        super.onScreenStateChanged(screenState);
        if (screenState != 1) {
            z = false;
        }
        this.mIsVisible = z;
        disableMovieView();
    }

    protected void onVisibilityChanged(View changedView, int visibility) {
        boolean z = false;
        super.onVisibilityChanged(changedView, visibility);
        if (visibility == 0) {
            z = true;
        }
        this.mIsVisible = z;
        disableMovieView();
    }

    protected void onWindowVisibilityChanged(int visibility) {
        boolean z = false;
        super.onWindowVisibilityChanged(visibility);
        if (visibility == 0) {
            z = true;
        }
        this.mIsVisible = z;
        disableMovieView();
    }

    private void disableMovieView() {
        if (!this.mIsVisible) {
            return;
        }
        if (VERSION.SDK_INT >= 16) {
            postInvalidateOnAnimation();
        } else {
            invalidate();
        }
    }

    private void updateMovieTime() {
        long now = SystemClock.uptimeMillis();
        if (this.mMovieStart == 0) {
            this.mMovieStart = now;
        }
        int dur = this.mGifMovie.duration();
        if (dur == 0) {
            dur = Config.HICLOUD_PROGRESSINTERVAL_DEF;
        }
        this.mCurrentMovieTime = (int) ((now - this.mMovieStart) % ((long) dur));
    }

    private void drawMovieFraming(Canvas canvas) {
        this.mGifMovie.setTime(this.mCurrentMovieTime);
        canvas.save(1);
        canvas.scale(this.mMovieScale, this.mMovieScale);
        this.mGifMovie.draw(canvas, this.mMovieLeft / this.mMovieScale, this.mMovieTop / this.mMovieScale);
        canvas.restore();
    }
}
