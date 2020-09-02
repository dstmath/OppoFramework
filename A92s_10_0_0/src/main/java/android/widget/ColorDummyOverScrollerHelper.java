package android.widget;

public class ColorDummyOverScrollerHelper implements IColorOverScrollerHelper {
    protected OverScroller mScroller;

    public ColorDummyOverScrollerHelper(OverScroller overScroller) {
        this.mScroller = overScroller;
    }

    @Override // android.widget.IColorOverScrollerHelper
    public int getFinalX(int x) {
        return x;
    }

    @Override // android.widget.IColorOverScrollerHelper
    public int getFinalY(int y) {
        return y;
    }

    @Override // android.widget.IColorOverScrollerHelper
    public boolean setFriction(float friction) {
        return false;
    }

    @Override // android.widget.IColorOverScrollerHelper
    public boolean isFinished(boolean finished) {
        return finished;
    }

    @Override // android.widget.IColorOverScrollerHelper
    public int getCurrX(int x) {
        return x;
    }

    @Override // android.widget.IColorOverScrollerHelper
    public int getCurrY(int y) {
        return y;
    }
}
