package com.color.inner.os;

import android.os.Environment;
import java.io.File;

public class EnvironmentWrapper {
    private static final String TAG = "EnvironmentWrapper";

    public static class UserEnvironmentWrapper {
        private Environment.UserEnvironment mUserEnvironment;

        public UserEnvironmentWrapper(int userId) {
            this.mUserEnvironment = new Environment.UserEnvironment(userId);
        }

        public File getExternalStorageDirectory() {
            return this.mUserEnvironment.getExternalDirs()[0];
        }
    }

    public static File getVendorDirectory(Environment environment) {
        return Environment.getVendorDirectory();
    }
}
