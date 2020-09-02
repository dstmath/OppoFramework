package android.view;

import android.common.OppoFeatureCache;
import android.content.Context;
import android.content.res.Resources;
import android.widget.IColorTextViewRTLUtilForUG;
import com.color.favorite.ColorFavoriteCallback;
import com.color.favorite.IColorFavoriteManager;
import com.color.screenshot.ColorLongshotViewController;
import com.color.screenshot.ColorLongshotViewInfo;
import com.color.screenshot.IColorLongshotController;
import com.color.util.ColorContextUtil;
import com.color.util.ColorTypeCastingHelper;
import com.color.view.ColorScrollBarEffect;
import com.color.view.IColorScrollBarEffect;

public class ColorViewHooksImp implements IColorViewHooks {
    private ColorContextUtil mContextUtil = null;
    private final IColorLongshotController mLongshotController;
    private final IColorScrollBarEffect mScrollBarEffect;
    private final View mView;

    public ColorViewHooksImp(View view, Resources res) {
        this.mView = view;
        this.mLongshotController = new ColorLongshotViewController((OppoBaseView) ColorTypeCastingHelper.typeCasting(OppoBaseView.class, this.mView));
        this.mScrollBarEffect = createScrollBarEffect(res);
        OppoFeatureCache.getOrCreate(IColorTextViewRTLUtilForUG.DEFAULT, new Object[0]).initRtlParameter(res);
    }

    public boolean awakenScrollBars() {
        return this.mView.awakenScrollBars();
    }

    public boolean isLayoutRtl() {
        return this.mView.isLayoutRtl();
    }

    public IColorLongshotController getLongshotController() {
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

    public int getOverScrollMode(int overScrollMode) {
        return getLongshotController().getOverScrollMode(overScrollMode);
    }

    public boolean findViewsLongshotInfo(ColorLongshotViewInfo info) {
        return getLongshotController().findInfo(info);
    }

    public boolean isLongshotConnected() {
        return getLongshotController().isLongshotConnected();
    }

    public boolean overScrollBy(int deltaX, int deltaY, int scrollX, int scrollY, int scrollRangeX, int scrollRangeY, int maxOverScrollX, int maxOverScrollY, boolean isTouchEvent, int oldScrollY, boolean result) {
        return getLongshotController().overScrollBy(deltaX, deltaY, scrollX, scrollY, scrollRangeX, scrollRangeY, maxOverScrollX, maxOverScrollY, isTouchEvent, oldScrollY, result);
    }

    public void performClick() {
        IColorFavoriteManager favoriteManager = OppoFeatureCache.getOrCreate(IColorFavoriteManager.DEFAULT, new Object[0]);
        favoriteManager.processClick(this.mView, (ColorFavoriteCallback) null);
        favoriteManager.logViewInfo(this.mView);
    }

    private IColorScrollBarEffect createScrollBarEffect(Resources res) {
        if (res == null) {
            return ColorScrollBarEffect.NO_EFFECT;
        }
        return new ColorScrollBarEffect(res, this);
    }

    public IColorViewConfigHelper getColorViewConfigHelper(Context context) {
        return new ColorViewConfigHelper(context);
    }
}
