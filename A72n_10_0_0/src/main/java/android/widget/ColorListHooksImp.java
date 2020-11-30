package android.widget;

import android.view.OppoBaseView;
import com.color.util.ColorTypeCastingHelper;

public class ColorListHooksImp implements IColorListHooks {
    public FastScroller getFastScroller(AbsListView absListView, int style) {
        if (((OppoBaseView) ColorTypeCastingHelper.typeCasting(OppoBaseView.class, absListView)).isColorStyle()) {
            return new ColorFastScroller(absListView, style);
        }
        return new FastScroller(absListView, style);
    }
}
