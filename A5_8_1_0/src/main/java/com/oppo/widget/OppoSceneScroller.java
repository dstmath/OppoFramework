package com.oppo.widget;

public class OppoSceneScroller {

    public interface OnProgressListener {
        void onProgressChanged(float f);

        void onProgressEnd(float f);

        void onProgressStart(float f);
    }

    public interface OnScrollBarListener {
        void onScrollBarChanged(float f);
    }
}
