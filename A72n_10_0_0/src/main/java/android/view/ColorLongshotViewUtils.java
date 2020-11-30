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

    public ColorLongshotViewUtils(Context context) {
    }

    private boolean needUpdateParent(Rect viewRect, Rect rootRect, ColorLongshotViewContent parent, boolean keepLargeRect) {
        if (isLargeCoverRect(viewRect, rootRect, keepLargeRect)) {
            return false;
        }
        return isLargeCoverRect(parent.getRect(), rootRect, keepLargeRect);
    }

    public void findCoverRect(int recursive, ViewGroup group, View view, List<ColorLongshotViewContent> coverContents, List<ColorLongshotViewContent> sideContents, Rect rootRect, ColorLongshotViewContent parent, boolean keepLargeRect) {
        Rect rootRect2;
        ArrayList<View> preorderedList;
        int childrenCount;
        int i;
        ArrayList<View> preorderedList2;
        String prefix;
        String prefix2;
        Rect rootRect3;
        View child;
        ColorLongshotViewContent parent2;
        int childIndex;
        ColorLongshotViewContent parent3;
        int i2;
        ColorLongshotViewContent parent4;
        Rect srcRect = new Rect();
        boolean z = true;
        group.getBoundsOnScreen(srcRect, true);
        if (rootRect == null) {
            rootRect2 = srcRect;
        } else {
            rootRect2 = rootRect;
        }
        boolean customOrder = false;
        boolean initParent = parent == null;
        String prefix3 = getPrefix(recursive);
        String prefix4 = "LongshotDump";
        ColorLog.d(DBG, prefix4, prefix3 + "findCoverRect : rootRect=" + rootRect2 + ", srcRect=" + srcRect + ", group=" + group + ", keepLargeRect=" + keepLargeRect);
        int childrenCount2 = group.getChildCount();
        ArrayList<View> preorderedList3 = group.buildOrderedChildList();
        boolean noPreorder = preorderedList3 == null;
        if (noPreorder && group.isChildrenDrawingOrderEnabled()) {
            customOrder = true;
        }
        ColorLongshotViewContent parent5 = parent;
        int i3 = childrenCount2 - 1;
        while (true) {
            if (i3 < 0) {
                preorderedList = preorderedList3;
                break;
            }
            int childIndex2 = customOrder ? group.getChildDrawingOrder(childrenCount2, i3) : i3;
            View child2 = noPreorder ? group.getChildAt(childIndex2) : preorderedList3.get(childIndex2);
            if (child2 == null) {
                i = i3;
                preorderedList2 = preorderedList3;
                childrenCount = childrenCount2;
                rootRect3 = rootRect2;
                prefix = prefix4;
                prefix2 = prefix3;
            } else if (child2 == view) {
                preorderedList = preorderedList3;
                break;
            } else if (!child2.isVisibleToUser()) {
                i = i3;
                preorderedList2 = preorderedList3;
                childrenCount = childrenCount2;
                rootRect3 = rootRect2;
                prefix = prefix4;
                prefix2 = prefix3;
            } else {
                child2.getBoundsOnScreen(this.mTempRect3, z);
                if (!Rect.intersects(this.mTempRect3, rootRect2)) {
                    i = i3;
                    preorderedList2 = preorderedList3;
                    childrenCount = childrenCount2;
                    rootRect3 = rootRect2;
                    prefix = prefix4;
                    prefix2 = prefix3;
                } else if (isTransparentGroup(child2)) {
                    if (initParent) {
                        childIndex = childIndex2;
                        parent3 = new ColorLongshotViewContent(child2, this.mTempRect3, null);
                    } else {
                        childIndex = childIndex2;
                        parent3 = parent5;
                    }
                    if (isWaterMarkGroup(rootRect2, (ViewGroup) child2)) {
                        ColorLongshotViewContent viewContent = new ColorLongshotViewContent(child2, this.mTempRect3, parent3);
                        boolean z2 = DBG;
                        i2 = i3;
                        StringBuilder sb = new StringBuilder();
                        sb.append(prefix3);
                        preorderedList2 = preorderedList3;
                        sb.append("  skipCoverRect : isWaterMarkGroup=");
                        sb.append(viewContent);
                        ColorLog.d(z2, prefix4, sb.toString());
                    } else {
                        i2 = i3;
                        preorderedList2 = preorderedList3;
                        if (isSideBarGroup(rootRect2, (ViewGroup) child2, sideContents)) {
                            ColorLongshotViewContent viewContent2 = new ColorLongshotViewContent(child2, this.mTempRect3, parent3);
                            ColorLog.d(DBG, prefix4, prefix3 + "  skipCoverRect : isSideBarGroup=" + viewContent2);
                            sideContents.add(viewContent2);
                        } else {
                            if (needUpdateParent(this.mTempRect3, rootRect2, parent3, keepLargeRect)) {
                                parent4 = new ColorLongshotViewContent(child2, this.mTempRect3, null);
                            } else {
                                parent4 = parent3;
                            }
                            List<ColorLongshotViewContent> childCoverContents = new ArrayList<>();
                            i = i2;
                            childrenCount = childrenCount2;
                            findCoverRect(recursive + 1, (ViewGroup) child2, null, childCoverContents, sideContents, rootRect2, parent4, keepLargeRect);
                            coverContents.addAll(childCoverContents);
                            prefix2 = prefix3;
                            parent5 = parent4;
                            prefix = prefix4;
                            rootRect3 = rootRect2;
                        }
                    }
                    i = i2;
                    parent5 = parent3;
                    childrenCount = childrenCount2;
                    rootRect3 = rootRect2;
                    prefix = prefix4;
                    prefix2 = prefix3;
                } else {
                    i = i3;
                    preorderedList2 = preorderedList3;
                    childrenCount = childrenCount2;
                    if (hasVisibleContent(prefix3, child2, rootRect2, keepLargeRect, "noCoverContent")) {
                        if (initParent) {
                            child = child2;
                            parent2 = new ColorLongshotViewContent(child, this.mTempRect3, null);
                        } else {
                            child = child2;
                            parent2 = parent5;
                        }
                        ColorLongshotViewContent viewContent3 = new ColorLongshotViewContent(child, this.mTempRect3, parent2);
                        rootRect3 = rootRect2;
                        if (isSideBarRect(this.mTempRect3, rootRect3)) {
                            boolean z3 = DBG;
                            StringBuilder sb2 = new StringBuilder();
                            prefix2 = prefix3;
                            sb2.append(prefix2);
                            sb2.append("  skipCoverRect : isSideBarView=");
                            sb2.append(viewContent3);
                            prefix = prefix4;
                            ColorLog.d(z3, prefix, sb2.toString());
                            if (sideContents != null) {
                                sideContents.add(viewContent3);
                            }
                        } else {
                            prefix2 = prefix3;
                            prefix = prefix4;
                            ColorLog.d(DBG, prefix, prefix2 + "  addCoverRect : " + viewContent3);
                            coverContents.add(viewContent3);
                        }
                        parent5 = parent2;
                    } else {
                        prefix2 = prefix3;
                        prefix = prefix4;
                        rootRect3 = rootRect2;
                    }
                }
            }
            i3 = i - 1;
            rootRect2 = rootRect3;
            preorderedList3 = preorderedList2;
            childrenCount2 = childrenCount;
            z = true;
            prefix3 = prefix2;
            prefix4 = prefix;
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
            if (!intRect.setIntersect(viewRect, rootRect) || intRect.width() < getMinSize(rootRect.width()) || intRect.height() < getMinSize(rootRect.height())) {
                return false;
            }
            return true;
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
        if (drawable == null || -2 == drawable.getOpacity()) {
            return true;
        }
        return false;
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
            boolean z = DBG;
            ColorLog.d(z, "LongshotDump", prefix + "  " + tag + " : " + type + "=" + rect + SettingsStringUtil.DELIMITER + view);
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
                if (!(TextUtils.isEmpty(contentView.getHint()) && TextUtils.isEmpty(contentView.getText()))) {
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
        boolean customOrder = true;
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
            if (!noPreorder || !group.isChildrenDrawingOrderEnabled()) {
                customOrder = false;
            }
            int i = childrenCount - 1;
            while (true) {
                if (i < 0) {
                    break;
                }
                int childIndex = customOrder ? group.getChildDrawingOrder(childrenCount, i) : i;
                View child = noPreorder ? group.getChildAt(childIndex) : preorderedList.get(childIndex);
                if (child != null) {
                    if (child.isVisibleToUser()) {
                        if (findSideBarContent(child, rootRect, null)) {
                            result = true;
                            break;
                        }
                    }
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
        int i = group.getChildCount() - 1;
        while (true) {
            if (i < 0) {
                break;
            }
            View child = group.getChildAt(i);
            if (!(child instanceof TextView)) {
                allTextView = false;
                break;
            }
            child.getBoundsOnScreen(this.mTempRect1, true);
            this.mTempRect2.union(this.mTempRect1);
            i--;
        }
        initCenterRect(this.mTempRect1, rootRect);
        if (!allTextView || !Rect.intersects(this.mTempRect2, this.mTempRect1)) {
            return false;
        }
        return true;
    }

    private boolean isLargeWidth(View view, Rect rect) {
        return view.getWidth() > (rect.width() * 2) / 3;
    }

    private boolean isSideBarGroup(Rect rootRect, ViewGroup group, List<ColorLongshotViewContent> sideContents) {
        boolean customOrder = false;
        if (sideContents == null || isLargeWidth(group, rootRect)) {
            return false;
        }
        Rect itemRect = new Rect();
        List<Rect> rects = new ArrayList<>();
        int childrenCount = group.getChildCount();
        ArrayList<View> preorderedList = group.buildOrderedChildList();
        boolean noPreorder = preorderedList == null;
        if (noPreorder && group.isChildrenDrawingOrderEnabled()) {
            customOrder = true;
        }
        for (int i = childrenCount - 1; i >= 0; i--) {
            int childIndex = customOrder ? group.getChildDrawingOrder(childrenCount, i) : i;
            View child = noPreorder ? group.getChildAt(childIndex) : preorderedList.get(childIndex);
            if (child != null && child.isVisibleToUser() && !isLargeWidth(child, rootRect) && findSideBarContent(child, rootRect, itemRect)) {
                child.getBoundsOnScreen(this.mTempRect1, true);
                rects.add(new Rect(this.mTempRect1));
            }
        }
        if (preorderedList != null) {
            preorderedList.clear();
        }
        Collections.sort(rects, RECT_COMPARATOR);
        this.mTempRect1.setEmpty();
        Rect last = new Rect();
        for (Rect rect : rects) {
            if (last.isEmpty() || isNeighboringRect(last, rect)) {
                this.mTempRect1.union(rect);
            }
            last.set(rect);
        }
        return isSideBarRect(this.mTempRect1, rootRect);
    }

    /* access modifiers changed from: private */
    public static class RectComparator implements Comparator<Rect> {
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
}
