package android.view;

import android.common.OppoFeatureList;
import android.os.Parcel;

public interface IColorDirectViewHelper extends IColorDirectWindow {
    public static final IColorDirectViewHelper DEFAULT = new IColorDirectViewHelper() {
        /* class android.view.IColorDirectViewHelper.AnonymousClass1 */
    };

    @Override // android.common.IOppoCommonFeature
    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IColorDirectViewHelper;
    }

    @Override // android.common.IOppoCommonFeature
    default IColorDirectViewHelper getDefault() {
        return DEFAULT;
    }

    default boolean onTransact(int code, Parcel data, Parcel reply, int flags) {
        return false;
    }
}
