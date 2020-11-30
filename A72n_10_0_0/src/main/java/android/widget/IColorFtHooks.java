package android.widget;

import android.common.IOppoCommonFeature;
import android.common.OppoFeatureList;
import android.content.Context;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import com.android.internal.R;

public interface IColorFtHooks extends IOppoCommonFeature {
    public static final IColorFtHooks DEFAULT = new IColorFtHooks() {
        /* class android.widget.IColorFtHooks.AnonymousClass1 */
    };

    @Override // android.common.IOppoCommonFeature
    default IColorFtHooks getDefault() {
        return DEFAULT;
    }

    @Override // android.common.IOppoCommonFeature
    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IColorFtHooks;
    }

    default int getMinOverscrollSize() {
        return 2;
    }

    default int getMaxOverscrollSize() {
        return 4;
    }

    default Drawable getArrowDrawable(Context context) {
        return context.getResources().getDrawable(R.drawable.ft_avd_tooverflow, context.getTheme());
    }

    default Drawable getOverflowDrawable(Context context) {
        return context.getResources().getDrawable(R.drawable.ft_avd_toarrow, context.getTheme());
    }

    default AnimatedVectorDrawable getToArrowAnim(Context context) {
        return (AnimatedVectorDrawable) context.getResources().getDrawable(R.drawable.ft_avd_toarrow_animation, context.getTheme());
    }

    default AnimatedVectorDrawable getToOverflowAnim(Context context) {
        return (AnimatedVectorDrawable) context.getResources().getDrawable(R.drawable.ft_avd_tooverflow_animation, context.getTheme());
    }

    default int getFirstItemPaddingStart(Context context, int paddingStart) {
        return (int) (((double) paddingStart) * 1.5d);
    }

    default int getLastItemPaddingEnd(Context context, int paddingEnd) {
        return (int) (((double) paddingEnd) * 1.5d);
    }

    default void setOverflowMenuCount(int count) {
    }

    default int calOverflowExtension(int lineHeight) {
        return (int) (((float) lineHeight) * 0.5f);
    }

    default int getOverflowButtonRes() {
        return R.layout.floating_popup_overflow_button;
    }

    default void setOverflowScrollBarSize(ListView listview) {
    }

    default void setConvertViewPosition(int position) {
    }

    default void setConvertViewPadding(View convertView, boolean openOverflowUpward, int sidePadding, int minimumWidth) {
    }

    default void setScrollIndicators(ListView listview) {
        listview.setScrollIndicators(3);
    }

    default int getMenuItemButtonRes() {
        return R.layout.floating_popup_menu_button;
    }

    default int getButtonTextId() {
        return R.id.floating_toolbar_menu_item_text;
    }

    default int getButtonIconId() {
        return R.id.floating_toolbar_menu_item_image;
    }

    default int getContentContainerRes() {
        return R.layout.floating_popup_container;
    }

    default int getFloatingToolBarHeightRes() {
        return R.dimen.floating_toolbar_height;
    }
}
