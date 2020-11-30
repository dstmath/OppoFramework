package android.inputmethodservice;

import android.app.Dialog;
import android.common.IOppoCommonFeature;
import android.common.OppoFeatureList;
import android.content.Context;
import android.inputmethodservice.InputMethodService;
import android.net.Uri;

public interface IColorInputMethodServiceUtils extends IOppoCommonFeature {
    public static final IColorInputMethodServiceUtils DEFAULT = new IColorInputMethodServiceUtils() {
        /* class android.inputmethodservice.IColorInputMethodServiceUtils.AnonymousClass1 */
    };

    @Override // android.common.IOppoCommonFeature
    default IColorInputMethodServiceUtils getDefault() {
        return DEFAULT;
    }

    @Override // android.common.IOppoCommonFeature
    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IColorInputMethodServiceUtils;
    }

    default void init(Context context) {
    }

    default void beforeInputShow() {
    }

    default void afterInputShow() {
    }

    default boolean getDockSide() {
        return false;
    }

    default void onChange(Uri uri) {
    }

    default void updateColorNavigationGuardColor(Dialog window) {
    }

    default void updateColorNavigationGuardColorDelay(Dialog window) {
    }

    default void onComputeRaise(InputMethodService.Insets mTmpInsets, Dialog window) {
    }

    default void uploadData(long time) {
    }
}
