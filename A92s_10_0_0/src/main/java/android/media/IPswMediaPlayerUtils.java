package android.media;

import android.common.IOppoCommonFeature;
import android.common.OppoFeatureList;
import android.os.Parcel;

public interface IPswMediaPlayerUtils extends IOppoCommonFeature {
    public static final IPswMediaPlayerUtils DEFAULT = new IPswMediaPlayerUtils() {
        /* class android.media.IPswMediaPlayerUtils.AnonymousClass1 */
    };
    public static final int KEY_PARAMETER_INTERCEPT = 10011;
    public static final String NAME = "IPswMediaPlayerUtils";

    @Override // android.common.IOppoCommonFeature
    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IPswMediaPlayerUtils;
    }

    @Override // android.common.IOppoCommonFeature
    default IOppoCommonFeature getDefault() {
        return DEFAULT;
    }

    default Parcel checkZenMode() {
        return null;
    }

    default Parcel checkWechatMute() {
        return null;
    }

    default void resetZenModeFlag() {
    }

    default void setAudioStreamType(int type) {
    }
}
