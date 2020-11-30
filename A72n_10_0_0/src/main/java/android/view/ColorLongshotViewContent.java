package android.view;

import android.graphics.Rect;
import android.provider.SettingsStringUtil;

public class ColorLongshotViewContent {
    private final ColorLongshotViewContent mParent;
    private final Rect mRect = new Rect();
    private final View mView;

    public ColorLongshotViewContent(View view, Rect rect, ColorLongshotViewContent parent) {
        this.mView = view;
        this.mRect.set(rect);
        this.mParent = parent;
    }

    public String toString() {
        return "{Parent=" + this.mParent + SettingsStringUtil.DELIMITER + this.mRect + SettingsStringUtil.DELIMITER + this.mView + "}";
    }

    public ColorLongshotViewContent getParent() {
        return this.mParent;
    }

    public View getView() {
        return this.mView;
    }

    public Rect getRect() {
        return this.mRect;
    }
}
