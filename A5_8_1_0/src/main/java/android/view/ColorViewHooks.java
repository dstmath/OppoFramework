package android.view;

import android.content.res.Resources;
import com.color.screenshot.ColorLongshotViewController;
import com.color.util.ColorContextUtil;
import com.color.view.ColorScrollBarEffect;
import com.color.view.IColorScrollBarEffect;
import com.color.view.IColorScrollBarEffect.ViewCallback;

public class ColorViewHooks implements ViewCallback {
    private ColorContextUtil mContextUtil = null;
    private final ColorLongshotViewController mLongshotController;
    private final IColorScrollBarEffect mScrollBarEffect;
    private final View mView;

    public ColorViewHooks(View view, Resources res) {
        this.mView = view;
        this.mLongshotController = new ColorLongshotViewController(this.mView);
        this.mScrollBarEffect = createScrollBarEffect(res);
    }

    public boolean awakenScrollBars() {
        return this.mView.awakenScrollBars();
    }

    public boolean isLayoutRtl() {
        return this.mView.isLayoutRtl();
    }

    public ColorLongshotViewController getLongshotController() {
        return this.mLongshotController;
    }

    public IColorScrollBarEffect getScrollBarEffect() {
        return this.mScrollBarEffect;
    }

    public boolean isOppoStyle() {
        if (this.mContextUtil == null) {
            this.mContextUtil = new ColorContextUtil(this.mView.getContext());
        }
        return this.mContextUtil.isOppoStyle();
    }

    public boolean isColorStyle() {
        if (this.mContextUtil == null) {
            this.mContextUtil = new ColorContextUtil(this.mView.getContext());
        }
        return this.mContextUtil.isColorStyle();
    }

    private IColorScrollBarEffect createScrollBarEffect(Resources res) {
        if (res == null) {
            return ColorScrollBarEffect.NO_EFFECT;
        }
        return new ColorScrollBarEffect(res, (ViewCallback) this);
    }
}
