package com.android.server.pm;

import com.oppo.reflect.RefBoolean;
import com.oppo.reflect.RefClass;

public class OppoMirrorPackageManagerService {
    public static RefBoolean DEBUG_ABI_SELECTION;
    public static RefBoolean DEBUG_BROADCASTS;
    public static RefBoolean DEBUG_DEXOPT;
    public static RefBoolean DEBUG_INSTALL;
    public static RefBoolean DEBUG_INTENT_MATCHING;
    public static RefBoolean DEBUG_PACKAGE_INFO;
    public static RefBoolean DEBUG_PACKAGE_SCANNING;
    public static RefBoolean DEBUG_PREFERRED;
    public static RefBoolean DEBUG_REMOVE;
    public static RefBoolean DEBUG_SETTINGS;
    public static RefBoolean DEBUG_UPGRADE;
    public static RefBoolean DEBUG_VERIFY;
    public static Class<?> TYPE = RefClass.load(OppoMirrorPackageManagerService.class, PackageManagerService.class);
}
