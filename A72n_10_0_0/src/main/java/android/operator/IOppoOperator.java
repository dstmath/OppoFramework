package android.operator;

import java.util.Map;

public interface IOppoOperator {
    Map getConfigMap(String str);

    void grantCustomizedRuntimePermissions();

    boolean hasFeatureDynamiclyEnabeld(String str);

    boolean isDynamicFeatureEnabled();

    boolean isInSimTriggeredSystemBlackList(String str);

    void notifySmartCustomizationStart();

    void testAidl();
}
