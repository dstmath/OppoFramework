package android.content.res;

import android.graphics.drawable.Drawable;

public abstract class ColorBaseResources {
    public abstract ColorBaseResourcesImpl getColorImpl();

    public abstract ColorBaseConfiguration getConfiguration();

    public void setIsThemeChanged(boolean changed) {
        getColorImpl().setIsThemeChanged(changed);
    }

    public boolean getThemeChanged() {
        return getColorImpl().getThemeChanged();
    }

    public Drawable loadIcon(int id) {
        return loadIcon(id, null, true);
    }

    public CharSequence getThemeCharSequence(int id) {
        return getColorImpl().getThemeCharSequence(id);
    }

    public Drawable loadIcon(int id, boolean useWrap) {
        return loadIcon(id, null, useWrap);
    }

    public Drawable loadIcon(int id, String str) {
        return loadIcon(id, str, true);
    }

    public void init(String name) {
        getColorImpl().init(name);
    }

    public Drawable loadIcon(int id, String str, boolean useWrap) {
        return getColorImpl().loadIcon(typeCasting(this), id, str, useWrap);
    }

    private Resources typeCasting(ColorBaseResources res) {
        return (Resources) typeCasting(Resources.class, res);
    }

    /* JADX DEBUG: Multi-variable search result rejected for r3v0, resolved type: java.lang.Object */
    /* JADX WARN: Multi-variable type inference failed */
    private <T> T typeCasting(Class<T> type, Object object) {
        if (object == 0 || !type.isInstance(object)) {
            return null;
        }
        return object;
    }
}
