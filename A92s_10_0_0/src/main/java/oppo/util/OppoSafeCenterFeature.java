package oppo.util;

public class OppoSafeCenterFeature {
    private static boolean mAssociateStartFeature = true;

    public static boolean isAssociationStartEnabled() {
        return mAssociateStartFeature;
    }

    public static void setAssociationStartFeature(boolean enabled) {
        mAssociateStartFeature = enabled;
    }

    public static boolean isLaunchRecordEnabled() {
        return true;
    }
}
