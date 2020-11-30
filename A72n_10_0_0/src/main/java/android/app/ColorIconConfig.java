package android.app;

import android.graphics.Path;

public class ColorIconConfig {
    private boolean artPlusOn;
    private int foregroundSize;
    private int iconShape;
    private int iconSize;
    private boolean isEmpty = true;
    private boolean isForeign;
    private boolean mNeedUpdate = false;
    private Path shapePath;
    private int theme;

    public int getIconShape() {
        return this.iconShape;
    }

    public void setIconShape(int iconShape2) {
        this.iconShape = iconShape2;
    }

    public int getTheme() {
        return this.theme;
    }

    public void setTheme(int theme2) {
        this.theme = theme2;
    }

    public int getIconSize() {
        return this.iconSize;
    }

    public void setIconSize(int iconSize2) {
        this.iconSize = iconSize2;
    }

    public int getForegroundSize() {
        return this.foregroundSize;
    }

    public void setForegroundSize(int foregroundSize2) {
        this.foregroundSize = foregroundSize2;
    }

    public boolean isArtPlusOn() {
        return this.artPlusOn;
    }

    public void setArtPlusOn(boolean artPlusOn2) {
        this.artPlusOn = artPlusOn2;
    }

    public Path getShapePath() {
        return this.shapePath;
    }

    public void setShapePath(Path shapePath2) {
        this.shapePath = new Path(shapePath2);
    }

    public boolean isForeign() {
        return this.isForeign;
    }

    public void setForeign(boolean foreign) {
        this.isForeign = foreign;
    }

    public void setEmpty(boolean empty) {
        this.isEmpty = empty;
    }

    public boolean isEmpty() {
        return this.isEmpty;
    }

    public void setNeedUpdate(boolean needUpdate) {
        this.mNeedUpdate = needUpdate;
    }

    public boolean isNeedUpdate() {
        return this.mNeedUpdate;
    }

    public String toString() {
        return "ColorIconConfig = [ isForeign : " + this.isForeign + ",theme : " + this.theme + ",iconSize : " + this.iconSize + ",iconShape : " + this.iconShape + ",foregroundSize : " + this.foregroundSize + ",artPlusOn : " + this.artPlusOn + ",shapePath ：" + this.shapePath + " ]";
    }
}
