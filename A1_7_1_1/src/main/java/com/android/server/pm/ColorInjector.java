package com.android.server.pm;

import android.util.ArraySet;
import java.io.File;

class ColorInjector {

    static class PackageManagerService {
        PackageManagerService() {
        }

        static void addAlreadyDexOpted(File frameworkDir, ArraySet<String> alreadyDexOpted) {
            alreadyDexOpted.add(frameworkDir.getPath() + "/oppo-framework-res.apk");
        }
    }

    ColorInjector() {
    }
}
