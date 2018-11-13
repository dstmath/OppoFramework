package android.widget;

import android.view.Menu;
import com.android.internal.R;

public class ColorEditorUtils {
    public static final int FLAG_DISABLE_CUT_PASTE = 32;
    public static final int FLAG_DISABLE_INSERT_MENU = 4;
    public static final int FLAG_DISABLE_MENU = 1;
    public static final int FLAG_DISABLE_REPLACE = 16;
    public static final int FLAG_DISABLE_SELECT_MENU = 2;
    public static final int FLAG_DISABLE_SHARE = 8;
    public static final int FLAG_HIDE_MENU = 128;
    public static final int FLAG_SELECT_ALL = 64;
    private Editor mEditor;
    private int mMenuFlag = 0;
    private TextView mTextView;

    public ColorEditorUtils(TextView textView) {
        this.mTextView = textView;
    }

    public ColorEditorUtils(Editor editor) {
        this.mEditor = editor;
    }

    public void setMenuFlag(int flag) {
        this.mMenuFlag |= flag;
        if ((this.mMenuFlag & 128) == 128) {
            this.mEditor.stopTextActionMode();
        }
    }

    public boolean isMenuEnabled() {
        if ((this.mMenuFlag & 1) == 1) {
            return false;
        }
        return true;
    }

    public boolean isSelectMenuEnabled() {
        if ((this.mMenuFlag & 2) == 2) {
            return false;
        }
        return true;
    }

    public boolean isInsertMenuEnabled() {
        if ((this.mMenuFlag & 4) == 4) {
            return false;
        }
        return true;
    }

    public boolean isShareEnabled() {
        if ((this.mMenuFlag & 8) == 8) {
            return false;
        }
        return true;
    }

    public boolean needAllSelected() {
        if ((this.mMenuFlag & 64) == 64) {
            return true;
        }
        return false;
    }

    public boolean isCutAndPasteEnabled() {
        if ((this.mMenuFlag & 32) == 32) {
            return false;
        }
        return true;
    }

    public void updateItems(Menu menu) {
        if (!isCutAndPasteEnabled()) {
            menu.removeItem(R.id.cut);
            menu.removeItem(R.id.paste);
        }
        if (!isShareEnabled()) {
            menu.removeItem(R.id.shareText);
        }
    }
}
