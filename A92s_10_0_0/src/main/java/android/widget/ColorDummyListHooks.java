package android.widget;

public class ColorDummyListHooks implements IColorListHooks {
    @Override // android.widget.IColorListHooks
    public FastScroller getFastScroller(AbsListView absListView, int style) {
        return new FastScroller(absListView, style);
    }
}
