package android.graphics;

public class OppoBaseBitmap {
    private int mColorState;
    private boolean mIsAssetSource;
    private boolean mIsCalculatedColor;
    private boolean mIsCanvasBaseBitmap = false;
    private boolean mIsViewSrc = false;

    public boolean hasCalculatedColor() {
        return this.mIsCalculatedColor;
    }

    public void setHasCalculatedColor(boolean isCalculatedColor) {
        this.mIsCalculatedColor = isCalculatedColor;
    }

    public boolean isAssetSource() {
        return this.mIsAssetSource;
    }

    public void setIsAssetSource(boolean isAssetSource) {
        this.mIsAssetSource = isAssetSource;
    }

    public int getColorState() {
        return this.mColorState;
    }

    public void setColorState(int colorState) {
        this.mColorState = colorState;
    }

    public void setIsCanvasBaseBitmap(boolean isCanvasBaseBitmap) {
        this.mIsCanvasBaseBitmap = isCanvasBaseBitmap;
    }

    public boolean isCanvasBaseBitmap() {
        return this.mIsCanvasBaseBitmap;
    }

    public boolean isViewSrc() {
        return this.mIsViewSrc;
    }

    public void setIsViewSrc(boolean isViewSrc) {
        this.mIsViewSrc = isViewSrc;
    }
}
