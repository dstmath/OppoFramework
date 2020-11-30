package android.view;

import android.common.IOppoCommonFeature;
import android.common.OppoFeatureList;

public interface IPswFeatureDemo extends IOppoCommonFeature {
    public static final IPswFeatureDemo DEFAULT = new IPswFeatureDemo() {
        /* class android.view.IPswFeatureDemo.AnonymousClass1 */
    };
    public static final String NAME = "IPswFeatureDemo";

    @Override // android.common.IOppoCommonFeature
    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IPswFeatureDemo;
    }

    @Override // android.common.IOppoCommonFeature
    default IOppoCommonFeature getDefault() {
        return DEFAULT;
    }

    default void testfunc() {
    }
}
