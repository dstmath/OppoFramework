package android.operator;

import java.util.HashSet;

public abstract class OppoOperatorManagerInternal {
    public abstract HashSet<String> getGrantedRuntimePermissionsPostInstall(String str);

    public abstract HashSet<String> getGrantedRuntimePermissionsPreload(String str, boolean z);

    public abstract boolean hasFeatureDynamiclyEnabeld(String str);

    public abstract void testInternal();
}
