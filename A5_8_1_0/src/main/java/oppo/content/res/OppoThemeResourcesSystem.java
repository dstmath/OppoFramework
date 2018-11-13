package oppo.content.res;

import android.content.res.ResourcesImpl;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.util.Log;
import java.io.File;
import java.io.IOException;
import oppo.content.res.OppoThemeZipFile.ThemeFileInfo;

public class OppoThemeResourcesSystem extends OppoThemeResources {
    private static final String TAG = "OppoThemeResourcesSystem";
    private static OppoThemeResources sIcons;
    private static OppoThemeResources sOppo;
    private final boolean DEBUG = true;

    public OppoThemeResourcesSystem(OppoThemeResourcesSystem themeResourcesSystem, ResourcesImpl resources, MetaData metaData) {
        super(themeResourcesSystem, resources, OppoThemeResources.FRAMEWORK_NAME, metaData);
    }

    public static OppoThemeResourcesSystem getTopLevelThemeResources(ResourcesImpl resources) {
        sIcons = OppoThemeResources.getTopLevelThemeResources(resources, "icons");
        sOppo = OppoThemeResources.getTopLevelThemeResources(resources, OppoThemeResources.OPPO_NAME);
        OppoThemeResourcesSystem themeresourcessystem = null;
        for (MetaData oppoThemeResourcesSystem : THEME_PATHS) {
            themeresourcessystem = new OppoThemeResourcesSystem(themeresourcessystem, resources, oppoThemeResourcesSystem);
        }
        return themeresourcessystem;
    }

    public boolean checkUpdate() {
        sIcons.checkUpdate();
        sOppo.checkUpdate();
        return super.checkUpdate();
    }

    /* JADX WARNING: Removed duplicated region for block: B:25:0x0085 A:{SYNTHETIC, Splitter: B:25:0x0085} */
    /* JADX WARNING: Removed duplicated region for block: B:19:0x0060 A:{SYNTHETIC, Splitter: B:19:0x0060} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public Bitmap getIcon(ResourcesImpl resources, String path) {
        OutOfMemoryError ex;
        Throwable th;
        Bitmap bitmap = null;
        ThemeFileInfo themeFileInfo = getIconStream(path, true);
        if (themeFileInfo == null) {
            try {
                if (themeFileInfo.mDensity > 0) {
                    Options options = new Options();
                    Options options2;
                    try {
                        options.inDensity = themeFileInfo.mDensity;
                        bitmap = BitmapFactory.decodeStream(themeFileInfo.mInput, null, options);
                        options2 = options;
                    } catch (OutOfMemoryError e) {
                        ex = e;
                        options2 = options;
                        try {
                            Log.w(TAG, "OppoThemeResourcesSystem OutOfMemoryError ex: " + ex);
                            if (themeFileInfo != null) {
                                try {
                                    themeFileInfo.mInput.close();
                                } catch (IOException ex2) {
                                    Log.w(TAG, "OppoThemeResourcesSystem IOException ex: " + ex2);
                                }
                            }
                            return bitmap;
                        } catch (Throwable th2) {
                            th = th2;
                            if (themeFileInfo != null) {
                                try {
                                    themeFileInfo.mInput.close();
                                } catch (IOException ex22) {
                                    Log.w(TAG, "OppoThemeResourcesSystem IOException ex: " + ex22);
                                }
                            }
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        if (themeFileInfo != null) {
                        }
                        throw th;
                    }
                }
            } catch (OutOfMemoryError e2) {
                ex = e2;
                Log.w(TAG, "OppoThemeResourcesSystem OutOfMemoryError ex: " + ex);
                if (themeFileInfo != null) {
                }
                return bitmap;
            }
        }
        if (themeFileInfo != null) {
            try {
                themeFileInfo.mInput.close();
            } catch (IOException ex222) {
                Log.w(TAG, "OppoThemeResourcesSystem IOException ex: " + ex222);
            }
        }
        return bitmap;
    }

    public ThemeFileInfo getIconStream(String path) {
        return sIcons.getThemeFileStream(path);
    }

    public ThemeFileInfo getIconStream(String path, boolean useWrap) {
        return sIcons.getThemeFileStream(path, useWrap);
    }

    public CharSequence getThemeCharSequence(int id) {
        CharSequence res = null;
        if (sOppo != null) {
            res = sOppo.getThemeCharSequence(id);
        }
        if (res == null) {
            return getThemeCharSequenceInner(id);
        }
        return res;
    }

    private ThemeFileInfo getThemeFileStreamSystem(String path, String subPath) {
        return getThemeFileStreamInner(path);
    }

    private ThemeFileInfo getThemeFileStreamOPPO(String path, String subPath) {
        if (sOppo != null) {
            return sOppo.getThemeFileStream(path);
        }
        return null;
    }

    public ThemeFileInfo getThemeFileStream(int index, String path) {
        if (path == null) {
            return null;
        }
        String res = path.substring(path.lastIndexOf(47) + 1);
        if (2 == index) {
            return getThemeFileStreamOPPO(path, res);
        }
        return getThemeFileStreamSystem(path, res);
    }

    public Integer getThemeInt(int id) {
        Integer res = null;
        if (sOppo != null) {
            res = sOppo.getThemeInt(id);
        }
        if (res == null) {
            return getThemeIntInner(id);
        }
        return res;
    }

    public boolean hasIcon(String path) {
        if (sIcons != null) {
            return sIcons.containsEntry(path);
        }
        return false;
    }

    public boolean hasValues() {
        if (super.hasValues() || sOppo.hasValues()) {
            return true;
        }
        return false;
    }

    public boolean isValid() {
        return !isValidInner() ? sOppo.isValid() : true;
    }

    public void resetIcons() {
        sIcons.checkUpdate();
    }

    public File getLockscreenWallpaper() {
        return null;
    }

    public void setResource(ResourcesImpl res) {
        sOppo.setResource(res);
        sIcons.setResource(res);
        super.setResource(res);
    }
}
