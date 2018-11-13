package android.view;

import android.graphics.Rect;
import android.provider.SettingsStringUtil;

public class ColorLongshotViewContent {
    private final ColorLongshotViewContent mParent;
    private final Rect mRect = new Rect(null);
    private final View mView;

    public ColorLongshotViewContent(View view, Rect rect, ColorLongshotViewContent parent) {
        this.mView = view;
        this.mRect.set(rect);
        this.mParent = parent;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{Parent=");
        sb.append(this.mParent);
        sb.append(SettingsStringUtil.DELIMITER);
        sb.append(this.mRect);
        sb.append(SettingsStringUtil.DELIMITER);
        sb.append(this.mView);
        sb.append("}");
        return sb.toString();
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
