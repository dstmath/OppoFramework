package android.view;

import android.common.IOppoCommonFeature;
import android.common.OppoFeatureList;
import android.content.Context;
import android.content.res.Configuration;

public interface IColorBurmeseZgHooks extends IOppoCommonFeature {
    public static final IColorBurmeseZgHooks DEFAULT = new IColorBurmeseZgHooks() {
        /* class android.view.IColorBurmeseZgHooks.AnonymousClass1 */
    };

    @Override // android.common.IOppoCommonFeature
    default IColorBurmeseZgHooks getDefault() {
        return DEFAULT;
    }

    @Override // android.common.IOppoCommonFeature
    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IColorBurmeseZgHooks;
    }

    default void initBurmeseZgFlag(Context context) {
    }

    default void updateBurmeseZgFlag(Context context) {
    }

    default boolean getZgFlag() {
        return false;
    }

    default void updateBurmeseEncodingForUser(Context context, Configuration config, int userId) {
    }
}
