package com.oppo.theme;

import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.SystemProperties;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class OppoThirdPartUtil {
    public static final String DATA_SYSTEM_THEME = "/data/theme/";
    private static final String[] DIRS_DRAWABLE = new String[]{"res/drawable-hdpi/", "res/drawable-xhdpi/", "res/drawable-xxhdpi/"};
    private static final String[] DIRS_DRAWABLE_XH = new String[]{"res/drawable-xhdpi/", "res/drawable-hdpi/", "res/drawable-xxhdpi/"};
    private static final String[] DIRS_DRAWABLE_XXH = new String[]{"res/drawable-xxhdpi/", "res/drawable-xhdpi/", "res/drawable-hdpi/"};
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
        byte[] buf = new byte[1024000];
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

    public static Drawable getDrawable(int id, Resources res) {
        return getLauncherDrawableByName(res, res.getResourceEntryName(id) + ".png");
    }

    public static Drawable getLauncherDrawableByName(Resources res, String nameString) {
        return getDrawableByName(res, nameString, ZIPLAUNCHER);
    }

    public static Drawable getIconDrawableByName(Resources res, String nameString) {
        return getDrawableByName(res, nameString, "icons");
    }

    public static Drawable getDrawableByName(Resources res, String nameString) {
        return getDrawableByName(res, nameString, ZIPLAUNCHER);
    }

    /* JADX WARNING: Removed duplicated region for block: B:23:0x0046 A:{SYNTHETIC, Splitter: B:23:0x0046} */
    /* JADX WARNING: Removed duplicated region for block: B:26:0x004b A:{SYNTHETIC, Splitter: B:26:0x004b} */
    /* JADX WARNING: Removed duplicated region for block: B:32:0x0056 A:{SYNTHETIC, Splitter: B:32:0x0056} */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x005b A:{SYNTHETIC, Splitter: B:35:0x005b} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static Drawable getDrawableByName(Resources res, String nameString, String zipPath) {
        Throwable th;
        Drawable drawable = null;
        String path = "/data/theme/";
        if (mIsDefaultTheme) {
            path = "/system/media/theme/default/";
        }
        InputStream inputStream = null;
        ZipFile file = null;
        try {
            ZipFile file2 = new ZipFile(path + zipPath);
            try {
                inputStream = getDrawableStream(res, file2, nameString);
                if (inputStream != null) {
                    drawable = new BitmapDrawable(res, BitmapFactory.decodeStream(inputStream));
                }
                if (file2 != null) {
                    try {
                        file2.close();
                    } catch (IOException e) {
                    }
                }
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e2) {
                    }
                }
                file = file2;
            } catch (Exception e3) {
                file = file2;
                if (file != null) {
                    try {
                        file.close();
                    } catch (IOException e4) {
                    }
                }
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e5) {
                    }
                }
                return drawable;
            } catch (Throwable th2) {
                th = th2;
                file = file2;
                if (file != null) {
                    try {
                        file.close();
                    } catch (IOException e6) {
                    }
                }
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e7) {
                    }
                }
                throw th;
            }
        } catch (Exception e8) {
            if (file != null) {
            }
            if (inputStream != null) {
            }
            return drawable;
        } catch (Throwable th3) {
            th = th3;
            if (file != null) {
            }
            if (inputStream != null) {
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

    public static void setDefaultTheme() {
        if ((1 & SystemProperties.getLong("persist.sys.themeflag", 0)) == 0) {
            mIsDefaultTheme = true;
        } else {
            mIsDefaultTheme = false;
        }
    }

    public static boolean getDefaultTheme() {
        if ((1 & SystemProperties.getLong("persist.sys.themeflag", 0)) == 0) {
            return true;
        }
        return false;
    }
}
