package android.widget;

public class ColorOverScrollerHelper extends ColorDummyOverScrollerHelper {
    private boolean mForSpringOverScroller;

    public ColorOverScrollerHelper(OverScroller overScroller) {
        super(overScroller);
        if (overScroller instanceof SpringOverScroller) {
            this.mForSpringOverScroller = true;
        }
    }

    public int getFinalX(int x) {
        if (this.mForSpringOverScroller) {
            return this.mScroller.getOppoFinalX();
        }
        return x;
    }

    public int getFinalY(int y) {
        if (this.mForSpringOverScroller) {
            return this.mScroller.getOppoFinalY();
        }
        return y;
    }

    public int getCurrX(int x) {
        if (this.mForSpringOverScroller) {
            return this.mScroller.getOppoCurrX();
        }
        return x;
    }

    public int getCurrY(int y) {
        if (this.mForSpringOverScroller) {
            return this.mScroller.getOppoCurrY();
        }
        return y;
    }

    public boolean setFriction(float friction) {
        if (!this.mForSpringOverScroller) {
            return false;
        }
        this.mScroller.setOppoFriction(friction);
        return true;
    }

    public boolean isFinished(boolean finished) {
        if (this.mForSpringOverScroller) {
            return this.mScroller.isOppoFinished();
        }
        return finished;
    }
}
