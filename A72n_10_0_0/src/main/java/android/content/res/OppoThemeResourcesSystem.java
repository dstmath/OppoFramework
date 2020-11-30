package android.content.res;

import android.content.res.OppoThemeResources;
import android.content.res.OppoThemeZipFile;
import java.io.File;

public class OppoThemeResourcesSystem extends OppoThemeResources {
    private static final String TAG = "OppoThemeResourcesSystem";
    private static OppoThemeResources sOppo;
    private final boolean DEBUG = true;

    public OppoThemeResourcesSystem(OppoThemeResourcesSystem themeResourcesSystem, ColorBaseResourcesImpl resources, OppoThemeResources.MetaData metaData) {
        super(themeResourcesSystem, resources, OppoThemeResources.FRAMEWORK_NAME, metaData);
    }

    public static OppoThemeResourcesSystem getTopLevelThemeResources(ColorBaseResourcesImpl resources) {
        sOppo = OppoThemeResources.getTopLevelThemeResources(resources, OppoThemeResources.OPPO_NAME);
        OppoThemeResourcesSystem themeresourcessystem = null;
        for (int i = 0; i < THEME_PATHS.length; i++) {
            themeresourcessystem = new OppoThemeResourcesSystem(themeresourcessystem, resources, THEME_PATHS[i]);
        }
        return themeresourcessystem;
    }

    @Override // android.content.res.OppoThemeResources
    public boolean checkUpdate() {
        sOppo.checkUpdate();
        return super.checkUpdate();
    }

    @Override // android.content.res.OppoThemeResources
    public CharSequence getThemeCharSequence(int id) {
        CharSequence res = null;
        OppoThemeResources oppoThemeResources = sOppo;
        if (oppoThemeResources != null) {
            res = oppoThemeResources.getThemeCharSequence(id);
        }
        if (res == null) {
            return getThemeCharSequenceInner(id);
        }
        return res;
    }

    private OppoThemeZipFile.ThemeFileInfo getThemeFileStreamSystem(String path, String subPath) {
        return getThemeFileStreamInner(path);
    }

    private OppoThemeZipFile.ThemeFileInfo getThemeFileStreamOPPO(String path, String subPath) {
        OppoThemeResources oppoThemeResources = sOppo;
        if (oppoThemeResources != null) {
            return oppoThemeResources.getThemeFileStream(path);
        }
        return null;
    }

    @Override // android.content.res.OppoThemeResources
    public OppoThemeZipFile.ThemeFileInfo getThemeFileStream(int index, String path) {
        if (path == null) {
            return null;
        }
        String res = path.substring(path.lastIndexOf(47) + 1);
        if (2 == index) {
            return getThemeFileStreamOPPO(path, res);
        }
        return getThemeFileStreamSystem(path, res);
    }

    public Integer getThemeInt(int id, int index) {
        OppoThemeResources oppoThemeResources;
        Integer res = null;
        if (index == 2 && (oppoThemeResources = sOppo) != null) {
            res = oppoThemeResources.getThemeInt(id);
        }
        if (res == null) {
            return getThemeIntInner(id);
        }
        return res;
    }

    @Override // android.content.res.OppoThemeResources
    public boolean hasValues() {
        if (super.hasValues() || sOppo.hasValues()) {
            return true;
        }
        return false;
    }

    @Override // android.content.res.OppoThemeResources
    public boolean hasDrawables() {
        return super.hasDrawables() || sOppo.hasDrawables();
    }

    public File getLockscreenWallpaper() {
        return null;
    }

    @Override // android.content.res.OppoThemeResources
    public void setResource(ColorBaseResourcesImpl res) {
        sOppo.setResource(res);
        super.setResource(res);
    }
}
