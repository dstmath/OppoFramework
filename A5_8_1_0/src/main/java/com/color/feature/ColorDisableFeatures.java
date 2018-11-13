package com.color.feature;

public class ColorDisableFeatures {

    public static class SystemCenter {
        public static final String LONGSHOT = getFeature("longshot");
        public static final String TRANSLATE = getFeature("translate");

        private static String getFeature(String module) {
            return ColorDisableFeatures.getDeptFeature("system_center", module);
        }
    }

    private static String getDeptFeature(String dept, String module) {
        return "oppo." + dept + "." + module + ".disable";
    }
}
