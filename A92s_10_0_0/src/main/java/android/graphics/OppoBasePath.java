package android.graphics;

import com.color.util.ColorTypeCastingHelper;

public class OppoBasePath {
    private boolean mIsAddArea = false;

    public boolean isAddArea() {
        return this.mIsAddArea;
    }

    public void setIsAddRect(boolean isAddArea) {
        this.mIsAddArea = isAddArea;
    }

    public void setIsAddRect(Path path) {
        OppoBasePath basePath;
        if (path != null && (basePath = (OppoBasePath) ColorTypeCastingHelper.typeCasting(OppoBasePath.class, path)) != null) {
            this.mIsAddArea = basePath.isAddArea();
        }
    }
}
