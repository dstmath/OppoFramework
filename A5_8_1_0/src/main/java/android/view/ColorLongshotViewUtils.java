package android.view;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.provider.SettingsStringUtil;
import android.text.TextUtils;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import com.color.screenshot.ColorLongshotDump;
import com.color.util.ColorLog;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ColorLongshotViewUtils {
    private static final boolean DBG = ColorLongshotDump.DBG;
    private static final RectComparator RECT_COMPARATOR = new RectComparator();
    private static final String TAG = "LongshotDump";
    private final Rect mTempRect1 = new Rect();
    private final Rect mTempRect2 = new Rect();
    private final Rect mTempRect3 = new Rect();

    private static class RectComparator implements Comparator<Rect> {
        /* synthetic */ RectComparator(RectComparator -this0) {
            this();
        }

        private RectComparator() {
        }

        public int compare(Rect rect1, Rect rect2) {
            int result = rect2.top - rect1.top;
            if (result != 0) {
                return rect2.left - rect1.left;
            }
            return result;
        }
    }

    public ColorLongshotViewUtils(Context context) {
    }

    private boolean needUpdateParent(Rect viewRect, Rect rootRect, ColorLongshotViewContent parent, boolean keepLargeRect) {
        if (isLargeCoverRect(viewRect, rootRect, keepLargeRect)) {
            return false;
        }
        return isLargeCoverRect(parent.getRect(), rootRect, keepLargeRect);
    }

    public void findCoverRect(int recursive, ViewGroup group, View view, List<ColorLongshotViewContent> coverContents, List<ColorLongshotViewContent> sideContents, Rect rootRect, ColorLongshotViewContent parent, boolean keepLargeRect) {
        Rect srcRect = new Rect();
        group.getBoundsOnScreen(srcRect, true);
        if (rootRect == null) {
            rootRect = srcRect;
        }
        boolean initParent = parent == null;
        String prefix = getPrefix(recursive);
        ColorLog.d(DBG, "LongshotDump", prefix + "findCoverRect : rootRect=" + rootRect + ", srcRect=" + srcRect + ", group=" + group + ", keepLargeRect=" + keepLargeRect);
        int childrenCount = group.getChildCount();
        ArrayList<View> preorderedList = group.buildOrderedChildList();
        boolean noPreorder = preorderedList == null;
        boolean customOrder = noPreorder ? group.isChildrenDrawingOrderEnabled() : false;
        int i = childrenCount - 1;
        while (i >= 0) {
            int childIndex = customOrder ? group.getChildDrawingOrder(childrenCount, i) : i;
            View child = noPreorder ? group.getChildAt(childIndex) : (View) preorderedList.get(childIndex);
            if (child != null) {
                if (child == view) {
                    break;
                } else if (child.isVisibleToUser()) {
                    child.getBoundsOnScreen(this.mTempRect3, true);
                    if (Rect.intersects(this.mTempRect3, rootRect)) {
                        ColorLongshotViewContent colorLongshotViewContent;
                        if (isTransparentGroup(child)) {
                            if (initParent) {
                                colorLongshotViewContent = new ColorLongshotViewContent(child, this.mTempRect3, null);
                            }
                            if (isWaterMarkGroup(rootRect, (ViewGroup) child)) {
                                ColorLog.d(DBG, "LongshotDump", prefix + "  skipCoverRect : isWaterMarkGroup=" + new ColorLongshotViewContent(child, this.mTempRect3, parent));
                            } else {
                                if (isSideBarGroup(rootRect, (ViewGroup) child, sideContents)) {
                                    colorLongshotViewContent = new ColorLongshotViewContent(child, this.mTempRect3, parent);
                                    ColorLog.d(DBG, "LongshotDump", prefix + "  skipCoverRect : isSideBarGroup=" + colorLongshotViewContent);
                                    sideContents.add(colorLongshotViewContent);
                                } else {
                                    if (needUpdateParent(this.mTempRect3, rootRect, parent, keepLargeRect)) {
                                        colorLongshotViewContent = new ColorLongshotViewContent(child, this.mTempRect3, null);
                                    }
                                    List<ColorLongshotViewContent> childCoverContents = new ArrayList();
                                    findCoverRect(recursive + 1, (ViewGroup) child, null, childCoverContents, sideContents, rootRect, parent, keepLargeRect);
                                    coverContents.addAll(childCoverContents);
                                }
                            }
                        } else {
                            if (hasVisibleContent(prefix, child, rootRect, keepLargeRect, "noCoverContent")) {
                                if (initParent) {
                                    colorLongshotViewContent = new ColorLongshotViewContent(child, this.mTempRect3, null);
                                }
                                colorLongshotViewContent = new ColorLongshotViewContent(child, this.mTempRect3, parent);
                                if (isSideBarRect(this.mTempRect3, rootRect)) {
                                    ColorLog.d(DBG, "LongshotDump", prefix + "  skipCoverRect : isSideBarView=" + colorLongshotViewContent);
                                    if (sideContents != null) {
                                        sideContents.add(colorLongshotViewContent);
                                    }
                                } else {
                                    ColorLog.d(DBG, "LongshotDump", prefix + "  addCoverRect : " + colorLongshotViewContent);
                                    coverContents.add(colorLongshotViewContent);
                                }
                            }
                        }
                    }
                }
            }
            i--;
        }
        if (preorderedList != null) {
            preorderedList.clear();
        }
    }

    public boolean isBottomBarRect(Rect viewRect, Rect rootRect) {
        if (viewRect.width() == rootRect.width() && viewRect.bottom == rootRect.bottom) {
            return true;
        }
        return false;
    }

    public boolean isLargeCoverRect(Rect viewRect, Rect rootRect, boolean keepLargeRect) {
        if (!keepLargeRect) {
            if (viewRect.contains(rootRect)) {
                return true;
            }
            Rect intRect = new Rect();
            return intRect.setIntersect(viewRect, rootRect) && intRect.width() >= getMinSize(rootRect.width()) && intRect.height() >= getMinSize(rootRect.height());
        }
        return false;
    }

    private int getMinSize(int size) {
        return (size * 3) / 4;
    }

    private String getPrefix(int recursive) {
        StringBuilder prefix = new StringBuilder();
        for (int i = 0; i < recursive; i++) {
            prefix.append("    ");
        }
        return prefix.toString();
    }

    private boolean isSmallCoverRect(Rect viewRect, Rect rootRect) {
        return viewRect.width() <= 1 && viewRect.height() <= 1;
    }

    private boolean isCenterCoverRect(Rect viewRect, Rect rootRect) {
        Rect centerRect = new Rect();
        centerRect.set(rootRect);
        centerRect.inset(rootRect.width() / 3, rootRect.height() / 3);
        return centerRect.contains(viewRect);
    }

    private boolean isTransparentDrawable(Drawable drawable) {
        boolean z = true;
        if (drawable == null) {
            return true;
        }
        if (-2 != drawable.getOpacity()) {
            z = false;
        }
        return z;
    }

    private boolean isTransparentGroup(View view) {
        if (!(view instanceof GridView) && (view instanceof ViewGroup)) {
            return isTransparentDrawable(view.getBackground());
        }
        return false;
    }

    private void initCenterRect(Rect centerRect, Rect rootRect) {
        centerRect.set(rootRect);
        centerRect.inset(rootRect.width() / 4, rootRect.height() / 4);
    }

    private void printNoContentLog(String prefix, String tag, String type, Rect rect, View view) {
        if (prefix != null && tag != null) {
            ColorLog.d(DBG, "LongshotDump", prefix + "  " + tag + " : " + type + "=" + rect + SettingsStringUtil.DELIMITER + view);
        }
    }

    private boolean hasVisibleContent(String prefix, View view, Rect rootRect, boolean keepLargeRect, String tag) {
        view.getBoundsOnScreen(this.mTempRect1, true);
        if (isCenterCoverRect(this.mTempRect1, rootRect)) {
            printNoContentLog(prefix, tag, "CenterCover", this.mTempRect1, view);
            return false;
        } else if (!isTransparentDrawable(view.getBackground())) {
            return true;
        } else {
            if (view instanceof TextView) {
                TextView contentView = (TextView) view;
                for (Drawable drawable : contentView.getCompoundDrawables()) {
                    if (!isTransparentDrawable(drawable)) {
                        return true;
                    }
                }
                if (!TextUtils.isEmpty(contentView.getHint())) {
                    return true;
                }
                if (!TextUtils.isEmpty(contentView.getText())) {
                    return true;
                }
                printNoContentLog(prefix, tag, "TextView", this.mTempRect1, view);
                return false;
            } else if (view instanceof ImageView) {
                if (!isTransparentDrawable(((ImageView) view).getDrawable())) {
                    return true;
                }
                printNoContentLog(prefix, tag, "ImageView", this.mTempRect1, view);
                return false;
            } else if (isLargeCoverRect(this.mTempRect1, rootRect, keepLargeRect)) {
                printNoContentLog(prefix, tag, "LargeCover", this.mTempRect1, view);
                return false;
            } else if (!isSmallCoverRect(this.mTempRect1, rootRect)) {
                return true;
            } else {
                printNoContentLog(prefix, tag, "SmallCover", this.mTempRect1, view);
                return false;
            }
        }
    }

    private boolean isNeighboringRect(Rect rect1, Rect rect2) {
        return rect1.top == rect2.bottom;
    }

    private boolean isSameLineRect(Rect rect1, Rect rect2) {
        return rect1.top == rect2.top && rect1.bottom == rect2.bottom;
    }

    private boolean isSideBarRect(Rect coverRect, Rect rootRect) {
        if (coverRect.width() <= rootRect.width() / 3 && coverRect.height() >= (rootRect.height() * 2) / 5) {
            return true;
        }
        return false;
    }

    private boolean findSideBarContent(View view, Rect rootRect, Rect itemRect) {
        if (itemRect != null) {
            view.getBoundsOnScreen(this.mTempRect1, true);
            if (itemRect.isEmpty()) {
                itemRect.set(this.mTempRect1);
            }
        }
        if (isTransparentGroup(view)) {
            boolean result = false;
            ViewGroup group = (ViewGroup) view;
            int childrenCount = group.getChildCount();
            ArrayList<View> preorderedList = group.buildOrderedChildList();
            boolean noPreorder = preorderedList == null;
            boolean customOrder = noPreorder ? group.isChildrenDrawingOrderEnabled() : false;
            int i = childrenCount - 1;
            while (i >= 0) {
                int childIndex = customOrder ? group.getChildDrawingOrder(childrenCount, i) : i;
                View child = noPreorder ? group.getChildAt(childIndex) : (View) preorderedList.get(childIndex);
                if (child != null && child.isVisibleToUser() && findSideBarContent(child, rootRect, null)) {
                    result = true;
                    break;
                }
                i--;
            }
            if (preorderedList != null) {
                preorderedList.clear();
            }
            return result;
        } else if (hasVisibleContent(null, view, rootRect, false, null)) {
            return true;
        } else {
            return false;
        }
    }

    private boolean isWaterMarkGroup(Rect rootRect, ViewGroup group) {
        this.mTempRect2.setEmpty();
        boolean allTextView = true;
        for (int i = group.getChildCount() - 1; i >= 0; i--) {
            View child = group.getChildAt(i);
            if (!(child instanceof TextView)) {
                allTextView = false;
                break;
            }
            child.getBoundsOnScreen(this.mTempRect1, true);
            this.mTempRect2.union(this.mTempRect1);
        }
        initCenterRect(this.mTempRect1, rootRect);
        if (allTextView) {
            return Rect.intersects(this.mTempRect2, this.mTempRect1);
        }
        return false;
    }

    private boolean isLargeWidth(View view, Rect rect) {
        return view.getWidth() > (rect.width() * 2) / 3;
    }

    private boolean isSideBarGroup(Rect rootRect, ViewGroup group, List<ColorLongshotViewContent> sideContents) {
        if (sideContents == null) {
            return false;
        }
        if (isLargeWidth(group, rootRect)) {
            return false;
        }
        Rect itemRect = new Rect(null);
        List<Rect> rects = new ArrayList();
        int childrenCount = group.getChildCount();
        ArrayList<View> preorderedList = group.buildOrderedChildList();
        boolean noPreorder = preorderedList == null;
        boolean customOrder = noPreorder ? group.isChildrenDrawingOrderEnabled() : false;
        int i = childrenCount - 1;
        while (i >= 0) {
            int childIndex = customOrder ? group.getChildDrawingOrder(childrenCount, i) : i;
            View child = noPreorder ? group.getChildAt(childIndex) : (View) preorderedList.get(childIndex);
            if (child != null && child.isVisibleToUser() && !isLargeWidth(child, rootRect) && findSideBarContent(child, rootRect, itemRect)) {
                child.getBoundsOnScreen(this.mTempRect1, true);
                rects.add(new Rect(this.mTempRect1));
            }
            i--;
        }
        if (preorderedList != null) {
            preorderedList.clear();
        }
        Collections.sort(rects, RECT_COMPARATOR);
        this.mTempRect1.setEmpty();
        Rect last = new Rect(null);
        for (Rect rect : rects) {
            if (last.isEmpty() || isNeighboringRect(last, rect)) {
                this.mTempRect1.union(rect);
            }
            last.set(rect);
        }
        return isSideBarRect(this.mTempRect1, rootRect);
    }
}
