package com.color.feature;

public class ColorDisableFeatures {
    /* access modifiers changed from: private */
    public static String getDeptFeature(String dept, String module) {
        return "oppo." + dept + "." + module + ".disable";
    }

    public static class SystemCenter {
        public static final String LONGSHOT = getFeature("longshot");
        public static final String TRANSLATE = getFeature("translate");

        private static String getFeature(String module) {
            return ColorDisableFeatures.getDeptFeature("system_center", module);
        }
    }
}
