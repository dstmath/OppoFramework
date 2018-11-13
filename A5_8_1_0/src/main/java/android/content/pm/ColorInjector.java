package android.content.pm;

class ColorInjector {

    static class PackageParser {
        private static final String PACKAGE_OPPO = "oppo";

        PackageParser() {
        }

        static String filterNameError(String pkgName, String nameError) {
            if (PACKAGE_OPPO.equals(pkgName)) {
                return null;
            }
            return nameError;
        }
    }

    ColorInjector() {
    }
}
