package com.oppo.theme;

import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.wifi.WifiScanner;
import android.os.SystemProperties;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class OppoThirdPartUtil {
    public static final String DATA_SYSTEM_THEME = "/data/theme/";
    private static final String[] DIRS_DRAWABLE = {"res/drawable-hdpi/", "res/drawable-xhdpi/", "res/drawable-xxhdpi/"};
    private static final String[] DIRS_DRAWABLE_XH = {"res/drawable-xhdpi/", "res/drawable-hdpi/", "res/drawable-xxhdpi/"};
    private static final String[] DIRS_DRAWABLE_XXH = {"res/drawable-xxhdpi/", "res/drawable-xhdpi/", "res/drawable-hdpi/"};
    private static final int NUM = 3;
    public static final String SYSTEM_THEME_PATH = "/system/media/theme/default/";
    private static final String TAG = "OppoThirdPartUtil";
    public static final String ZIPICONS = "icons";
    public static final String ZIPLAUNCHER = "com.oppo.launcher";
    public static boolean mIsDefaultTheme = true;

    private OppoThirdPartUtil() {
    }

    public static boolean moveFile(String themeFileName, String resourceName, String destName) throws Exception {
        ZipFile zipFile = new ZipFile(themeFileName);
        ZipEntry entry = zipFile.getEntry(resourceName);
        if (entry == null) {
            return false;
        }
        InputStream stream = zipFile.getInputStream(entry);
        FileOutputStream out = new FileOutputStream(destName);
        byte[] buf = new byte[WifiScanner.MAX_SCAN_PERIOD_MS];
        while (true) {
            int count = stream.read(buf);
            if (count > 0) {
                out.write(buf, 0, count);
            } else {
                stream.close();
                out.close();
                return true;
            }
        }
    }

    public static boolean clearDir(String path) {
        try {
            for (String str : new File(path).list()) {
                File oldTheme = new File(path, str);
                if (oldTheme.exists()) {
                    oldTheme.delete();
                }
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static Drawable getDrawableForUser(int id, Resources res, int userId) {
        String nameString = res.getResourceEntryName(id);
        return getLauncherDrawableByNameForUser(res, nameString + ".png", userId);
    }

    public static Drawable getLauncherDrawableByNameForUser(Resources res, String nameString, int userId) {
        return getDrawableByNameForUser(res, nameString, "com.oppo.launcher", userId);
    }

    public static Drawable getIconDrawableByNameForUser(Resources res, String nameString, int userId) {
        return getDrawableByNameForUser(res, nameString, "icons", userId);
    }

    public static Drawable getDrawableByNameForUser(Resources res, String nameString, int userId) {
        return getDrawableByNameForUser(res, nameString, "com.oppo.launcher", userId);
    }

    public static Drawable getDrawableByNameForUser(Resources res, String nameString, String zipPath, int userId) {
        Drawable drawable = null;
        String path = getThemePathForUser(userId);
        if (mIsDefaultTheme) {
            path = "/system/media/theme/default/";
        }
        InputStream iStream = null;
        ZipFile file = null;
        try {
            ZipFile file2 = new ZipFile(path + zipPath);
            InputStream iStream2 = getDrawableStream(res, file2, nameString);
            if (iStream2 != null) {
                drawable = new BitmapDrawable(res, BitmapFactory.decodeStream(iStream2));
            }
            try {
                file2.close();
            } catch (IOException e) {
            }
            if (iStream2 != null) {
                try {
                    iStream2.close();
                } catch (IOException e2) {
                }
            }
        } catch (Exception e3) {
            if (file != null) {
                try {
                    file.close();
                } catch (IOException e4) {
                }
            }
            if (iStream != null) {
                iStream.close();
            }
        } catch (Throwable th) {
            if (file != null) {
                try {
                    file.close();
                } catch (IOException e5) {
                }
            }
            if (iStream != null) {
                try {
                    iStream.close();
                } catch (IOException e6) {
                }
            }
            throw th;
        }
        return drawable;
    }

    public static Drawable getDrawable(int id, Resources res) {
        String nameString = res.getResourceEntryName(id);
        return getLauncherDrawableByNameForUser(res, nameString + ".png", 0);
    }

    public static Drawable getLauncherDrawableByName(Resources res, String nameString) {
        return getDrawableByNameForUser(res, nameString, "com.oppo.launcher", 0);
    }

    public static Drawable getIconDrawableByName(Resources res, String nameString) {
        return getDrawableByNameForUser(res, nameString, "icons", 0);
    }

    public static Drawable getDrawableByName(Resources res, String nameString) {
        return getDrawableByNameForUser(res, nameString, "com.oppo.launcher", 0);
    }

    public static Drawable getDrawableByName(Resources res, String nameString, String zipPath) {
        Drawable drawable = null;
        String path = "/data/theme/";
        if (mIsDefaultTheme) {
            path = "/system/media/theme/default/";
        }
        InputStream iStream = null;
        ZipFile file = null;
        try {
            ZipFile file2 = new ZipFile(path + zipPath);
            InputStream iStream2 = getDrawableStream(res, file2, nameString);
            if (iStream2 != null) {
                drawable = new BitmapDrawable(res, BitmapFactory.decodeStream(iStream2));
            }
            try {
                file2.close();
            } catch (IOException e) {
            }
            if (iStream2 != null) {
                try {
                    iStream2.close();
                } catch (IOException e2) {
                }
            }
        } catch (Exception e3) {
            if (file != null) {
                try {
                    file.close();
                } catch (IOException e4) {
                }
            }
            if (iStream != null) {
                iStream.close();
            }
        } catch (Throwable th) {
            if (file != null) {
                try {
                    file.close();
                } catch (IOException e5) {
                }
            }
            if (iStream != null) {
                try {
                    iStream.close();
                } catch (IOException e6) {
                }
            }
            throw th;
        }
        return drawable;
    }

    private static InputStream getDrawableStream(Resources res, ZipFile file, String entryname) throws Exception {
        String[] temdirs = DIRS_DRAWABLE;
        float dpi = res.getDisplayMetrics().density;
        if (dpi >= 3.0f) {
            temdirs = DIRS_DRAWABLE_XXH;
        } else if (dpi >= 2.0f) {
            temdirs = DIRS_DRAWABLE_XH;
        }
        for (int i = 0; i <= 2; i++) {
            ZipEntry entry = file.getEntry(temdirs[i] + entryname);
            if (entry != null) {
                return file.getInputStream(entry);
            }
        }
        return null;
    }

    protected static String getThemePathForUser(int userId) {
        if (userId <= 0) {
            return "/data/theme/";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("/data/theme/");
        sb.append(userId);
        return sb.append("/").toString();
    }

    public static void setDefaultTheme() {
        setDefaultTheme(0);
    }

    public static boolean getDefaultTheme() {
        return getDefaultTheme(0);
    }

    public static void setDefaultTheme(int userId) {
        if ((1 & SystemProperties.getLong(getThemeKeyForUser(userId), 0)) == 0) {
            mIsDefaultTheme = true;
        } else {
            mIsDefaultTheme = false;
        }
    }

    public static boolean getDefaultTheme(int userId) {
        if ((1 & SystemProperties.getLong(getThemeKeyForUser(userId), 0)) == 0) {
            return true;
        }
        return false;
    }

    private static String getThemeKeyForUser(int userId) {
        if (userId <= 0) {
            return "persist.sys.themeflag";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("persist.sys.themeflag");
        sb.append(".");
        return sb.append(userId).toString();
    }
}
