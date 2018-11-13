package com.suntek.rcs.ui.common;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Movie;
import android.os.Build.VERSION;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.View.MeasureSpec;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class GifMovieView extends View {
    private static final int DEFAULT_MOVIEW_DURATION = 1000;
    private static final String TAG = "GifMovieView";
    private int mCurrentAnimationTime;
    private float mLeft;
    private int mMeasuredMovieHeight;
    private int mMeasuredMovieWidth;
    private Movie mMovie;
    private int mMovieResourceId;
    private long mMovieStart;
    private String mMovieString;
    private volatile boolean mPaused;
    private float mScale;
    private float mTop;
    private boolean mVisible;

    public GifMovieView(Context context) {
        this(context, null);
    }

    public GifMovieView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mCurrentAnimationTime = 0;
        this.mPaused = false;
        this.mVisible = true;
    }

    public GifMovieView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mCurrentAnimationTime = 0;
        this.mPaused = false;
        this.mVisible = true;
        setViewAttributes(context, attrs, defStyle);
    }

    @SuppressLint({"NewApi"})
    private void setViewAttributes(Context context, AttributeSet attrs, int defStyle) {
        if (VERSION.SDK_INT >= 11) {
            setLayerType(1, null);
        }
        this.mMovieResourceId = -1;
        this.mPaused = false;
        if (this.mMovieResourceId != -1) {
            this.mMovie = Movie.decodeFile(this.mMovieString);
        }
    }

    public void setMovieResource(String path) {
        this.mMovieString = path;
        Log.d(TAG, path);
        InputStream inputStream = null;
        try {
            inputStream = new BufferedInputStream(new FileInputStream(path));
        } catch (FileNotFoundException e) {
            Log.d(TAG, "FileNotFoundException");
            e.printStackTrace();
        }
        Log.d(TAG, inputStream.toString());
        this.mMovie = Movie.decodeStream(inputStream);
        requestLayout();
    }

    public void setMovie(Movie movie) {
        this.mMovie = movie;
        requestLayout();
    }

    public Movie getMovie() {
        return this.mMovie;
    }

    public void setMovieTime(int time) {
        this.mCurrentAnimationTime = time;
        invalidate();
    }

    public void setPaused(boolean paused) {
        this.mPaused = paused;
        if (!paused) {
            this.mMovieStart = SystemClock.uptimeMillis() - ((long) this.mCurrentAnimationTime);
        }
        invalidate();
    }

    public boolean isPaused() {
        return this.mPaused;
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (this.mMovie != null) {
            int movieWidth = this.mMovie.width();
            int movieHeight = this.mMovie.height();
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
            this.mScale = 2.3f / Math.max(scaleH, scaleW);
            this.mMeasuredMovieWidth = (int) (((float) movieWidth) * this.mScale);
            this.mMeasuredMovieHeight = (int) (((float) movieHeight) * this.mScale);
            setMeasuredDimension(this.mMeasuredMovieWidth, this.mMeasuredMovieHeight);
            return;
        }
        setMeasuredDimension(getSuggestedMinimumWidth(), getSuggestedMinimumHeight());
    }

    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        boolean z = false;
        super.onLayout(changed, l, t, r, b);
        this.mLeft = ((float) (getWidth() - this.mMeasuredMovieWidth)) / 2.0f;
        this.mTop = ((float) (getHeight() - this.mMeasuredMovieHeight)) / 2.0f;
        if (getVisibility() == 0) {
            z = true;
        }
        this.mVisible = z;
    }

    protected void onDraw(Canvas canvas) {
        if (this.mMovie == null) {
            return;
        }
        if (this.mPaused) {
            drawMovieFrame(canvas);
            return;
        }
        updateAnimationTime();
        drawMovieFrame(canvas);
        invalidateView();
    }

    @SuppressLint({"NewApi"})
    private void invalidateView() {
        if (!this.mVisible) {
            return;
        }
        if (VERSION.SDK_INT >= 16) {
            postInvalidateOnAnimation();
        } else {
            invalidate();
        }
    }

    private void updateAnimationTime() {
        long now = SystemClock.uptimeMillis();
        if (this.mMovieStart == 0) {
            this.mMovieStart = now;
        }
        int dur = this.mMovie.duration();
        if (dur == 0) {
            dur = 1000;
        }
        this.mCurrentAnimationTime = (int) ((now - this.mMovieStart) % ((long) dur));
    }

    private void drawMovieFrame(Canvas canvas) {
        this.mMovie.setTime(this.mCurrentAnimationTime);
        canvas.save(1);
        canvas.scale(this.mScale, this.mScale);
        this.mMovie.draw(canvas, this.mLeft / this.mScale, this.mTop / this.mScale);
        canvas.restore();
    }

    @SuppressLint({"NewApi"})
    public void onScreenStateChanged(int screenState) {
        boolean z = true;
        super.onScreenStateChanged(screenState);
        if (screenState != 1) {
            z = false;
        }
        this.mVisible = z;
        invalidateView();
    }

    @SuppressLint({"NewApi"})
    protected void onVisibilityChanged(View changedView, int visibility) {
        boolean z = false;
        super.onVisibilityChanged(changedView, visibility);
        if (visibility == 0) {
            z = true;
        }
        this.mVisible = z;
        invalidateView();
    }

    protected void onWindowVisibilityChanged(int visibility) {
        boolean z = false;
        super.onWindowVisibilityChanged(visibility);
        if (visibility == 0) {
            z = true;
        }
        this.mVisible = z;
        invalidateView();
    }
}
