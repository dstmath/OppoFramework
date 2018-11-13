package android.view;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.Rect;
import android.opengl.GLSurfaceView;
import android.os.ParcelFileDescriptor;
import android.os.StrictMode;
import android.os.StrictMode.ThreadPolicy;
import android.os.SystemClock;
import android.provider.SettingsStringUtil;
import android.util.Log;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.ScrollView;
import com.android.internal.util.FastPrintWriter;
import com.color.screenshot.ColorLongshotComponentName;
import com.color.screenshot.ColorLongshotDump;
import com.color.screenshot.ColorLongshotUtils;
import com.color.screenshot.ColorLongshotViewInfo;
import com.color.screenshot.ColorLongshotViewRoot;
import com.color.screenshot.ColorScreenshotManager;
import com.color.util.ColorLog;
import com.color.view.analysis.ColorWindowNode;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import libcore.io.IoUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ColorLongshotViewDump {
    private static final ContentComparator CONTENT_COMPARATOR = new ContentComparator();
    private static final boolean DBG = ColorLongshotDump.DBG;
    private static final boolean FEATURE_ADJUST_WINDOW = false;
    private static final boolean FEATURE_DUMP_MIN_SIZE = false;
    private static final String JSON_CHILD_HASH = "child_hash";
    private static final String JSON_CHILD_LIST = "child_list";
    private static final String JSON_CHILD_RECT_CLIP = "child_rect_clip";
    private static final String JSON_CHILD_RECT_FULL = "child_rect_full";
    private static final String JSON_CHILD_SCROLLY = "child_scrollY";
    private static final String JSON_FLOAT_LIST = "float_list";
    private static final String JSON_FLOAT_RECT = "float_rect";
    private static final String JSON_PARENT_HASH = "parent_hash";
    private static final String JSON_PARENT_RECT_CLIP = "parent_rect_clip";
    private static final String JSON_PARENT_RECT_FULL = "parent_rect_full";
    private static final String JSON_SCROLL_CHILD = "scroll_child";
    private static final String JSON_SCROLL_LIST = "scroll_list";
    private static final String JSON_SCROLL_RECT = "scroll_rect";
    private static final String JSON_SIDE_LIST = "side_list";
    private static final String JSON_SIDE_RECT = "side_rect";
    private static final String JSON_VIEW_UNSUPPORTED = "view_unsupported";
    private static final String JSON_WINDOW_LAYER = "window_layer";
    private static final String JSON_WINDOW_LIST = "window_list";
    private static final String JSON_WINDOW_NAVIBAR = "window_navibar";
    private static final String JSON_WINDOW_RECT_DECOR = "window_rect_decor";
    private static final String JSON_WINDOW_RECT_VISIBLE = "window_rect_visible";
    private static final String JSON_WINDOW_STATBAR = "window_statbar";
    private static final String TAG = "LongshotDump";
    private int mCoverHeight = 0;
    private int mDumpCount = 0;
    private final List<Rect> mFloatRects = new ArrayList();
    private int mMinListHeight = 0;
    private int mMinScrollDistance = 0;
    private int mMinScrollHeight = 0;
    private int mScreenHeight = 0;
    private int mScreenWidght = 0;
    private ViewNode mScrollNode = null;
    private final List<ViewNode> mScrollNodes = new ArrayList();
    private final List<Rect> mSideRects = new ArrayList();
    private final List<View> mSmallViews = new ArrayList();
    private final Rect mTempRect1 = new Rect();
    private final Rect mTempRect2 = new Rect();
    private final ColorLongshotViewInfo mViewInfo = new ColorLongshotViewInfo();

    private static class ContentComparator implements Comparator<ColorLongshotViewContent> {
        /* synthetic */ ContentComparator(ContentComparator -this0) {
            this();
        }

        private ContentComparator() {
        }

        public int compare(ColorLongshotViewContent content1, ColorLongshotViewContent content2) {
            ColorLongshotViewContent parent1 = content1.getParent();
            ColorLongshotViewContent parent2 = content2.getParent();
            int result = 0;
            if (!(parent1 == null || parent2 == null)) {
                result = rectCompare(parent1.getRect(), parent2.getRect());
                if (result == 0) {
                    result = System.identityHashCode(parent2.getView()) - System.identityHashCode(parent1.getView());
                }
            }
            if (result == 0) {
                return rectCompare(content1.getRect(), content2.getRect());
            }
            return result;
        }

        private int rectCompare(Rect rect1, Rect rect2) {
            if (rect2.top > rect1.top) {
                return 1;
            }
            if (rect2.top < rect1.top) {
                return -1;
            }
            if (rect2.bottom < rect1.bottom) {
                return 1;
            }
            if (rect2.bottom > rect1.bottom) {
                return -1;
            }
            if (rect2.left > rect1.left) {
                return 1;
            }
            if (rect2.left < rect1.left) {
                return -1;
            }
            if (rect2.right < rect1.right) {
                return 1;
            }
            if (rect2.right > rect1.right) {
                return -1;
            }
            return 0;
        }
    }

    private static final class ViewNode {
        private final CharSequence mAccessibilityName;
        private final List<ViewNode> mChildList = new ArrayList();
        private final CharSequence mClassName;
        private final Rect mClipRect = new Rect();
        private final Rect mFullRect = new Rect();
        private int mOverScrollMode = -1;
        private final Rect mScrollRect = new Rect(null);
        private long mSpend = 0;
        private final View mView;

        public ViewNode(View view, CharSequence accessibilityName, Rect clipRect, Rect fullRect) {
            this.mView = view;
            this.mAccessibilityName = accessibilityName;
            this.mClassName = view.getClass().getName();
            this.mClipRect.set(clipRect);
            this.mFullRect.set(fullRect);
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("View[");
            if (this.mAccessibilityName != null) {
                sb.append("accessibility=");
                sb.append(this.mAccessibilityName.toString());
                sb.append(SettingsStringUtil.DELIMITER);
            }
            if (this.mClassName != null) {
                sb.append("class=");
                sb.append(this.mClassName.toString());
                sb.append(SettingsStringUtil.DELIMITER);
            }
            sb.append("clip=");
            sb.append(this.mClipRect);
            sb.append(":full=");
            sb.append(this.mFullRect);
            sb.append(":spend=");
            sb.append(this.mSpend);
            sb.append("]");
            return sb.toString();
        }

        public View getView() {
            return this.mView;
        }

        public void addChild(ViewNode viewNode) {
            this.mChildList.add(viewNode);
        }

        public List<ViewNode> getChildList() {
            return this.mChildList;
        }

        public CharSequence getAccessibilityName() {
            return this.mAccessibilityName;
        }

        public CharSequence getClassName() {
            return this.mClassName;
        }

        public Rect getClipRect() {
            return this.mClipRect;
        }

        public Rect getFullRect() {
            return this.mFullRect;
        }

        public void setSpend(long spend) {
            this.mSpend = spend;
        }

        public void setScrollRect(Rect rect) {
            this.mScrollRect.set(rect);
        }

        public Rect getScrollRect() {
            return this.mScrollRect;
        }

        public void disableOverScroll() {
            ViewRootImpl root = this.mView.getViewRootImpl();
            if (root != null) {
                ColorLongshotViewRoot longshot = root.mViewRootHooks.getLongshotViewRoot();
                this.mOverScrollMode = this.mView.getOverScrollMode();
                ColorLog.d(ColorLongshotViewDump.DBG, "LongshotDump", "disableOverScroll : " + this.mOverScrollMode);
                this.mView.setOverScrollMode(2);
            }
        }

        public void resetOverScroll() {
            ViewRootImpl root = this.mView.getViewRootImpl();
            if (root != null) {
                ColorLongshotViewRoot longshot = root.mViewRootHooks.getLongshotViewRoot();
                if (this.mOverScrollMode >= 0) {
                    ColorLog.d(ColorLongshotViewDump.DBG, "LongshotDump", "resetOverScroll : " + this.mOverScrollMode);
                    this.mView.setOverScrollMode(this.mOverScrollMode);
                    this.mOverScrollMode = -1;
                }
            }
        }
    }

    public ColorWindowNode collectWindow(View view, boolean isStatusBar, boolean isNavigationBar) {
        return new ColorWindowNode(view, isStatusBar, isNavigationBar);
    }

    public void dumpViewRoot(ViewRootImpl viewAncestor, ParcelFileDescriptor fd, List<ColorWindowNode> systemWindows, List<ColorWindowNode> floatWindows) {
        ThreadPolicy oldPolicy = StrictMode.allowThreadDiskWrites();
        try {
            this.mScrollNode = null;
            ColorLog.d(DBG, "LongshotDump", "dumpViewRoot : viewAncestor.mView=" + viewAncestor.mView);
            PrintWriter pw = new FastPrintWriter(new FileOutputStream(fd.getFileDescriptor()));
            ColorLongshotDump result = new ColorLongshotDump();
            this.mDumpCount = 0;
            Resources res = viewAncestor.mContext.getResources();
            this.mMinScrollHeight = res.getDimensionPixelOffset(201655598);
            this.mMinScrollDistance = res.getDimensionPixelOffset(201655599);
            this.mMinListHeight = res.getDimensionPixelOffset(201655397);
            this.mCoverHeight = res.getDimensionPixelOffset(201655398);
            this.mScreenHeight = res.getDisplayMetrics().heightPixels;
            this.mScreenWidght = res.getDisplayMetrics().widthPixels;
            dumpViewHierarchy(result, viewAncestor.mView);
            selectScrollNodes(this.mScrollNodes);
            ColorLog.d(DBG, "LongshotDump", "dumpViewRoot : mScrollNode=" + this.mScrollNode);
            calcScrollRects(result, new ColorLongshotViewUtils(viewAncestor.mContext), this.mScrollNodes, systemWindows, floatWindows);
            String jsonPack = packJsonNode(result, pw, systemWindows, floatWindows);
            if (jsonPack != null) {
                pw.println(jsonPack);
            }
            pw.flush();
        } catch (Exception e) {
            ColorLog.e(DBG, "LongshotDump", "dumpViewRoot ERROR : " + Log.getStackTraceString(e));
        } finally {
            this.mScrollNodes.clear();
            this.mFloatRects.clear();
            this.mSideRects.clear();
            IoUtils.closeQuietly(fd);
            StrictMode.setThreadPolicy(oldPolicy);
        }
    }

    public void injectInputBegin() {
        if (this.mScrollNode != null) {
            this.mScrollNode.disableOverScroll();
        }
    }

    public void injectInputEnd() {
        if (this.mScrollNode != null) {
            this.mScrollNode.resetOverScroll();
        }
    }

    public void reset() {
        injectInputEnd();
        this.mScrollNode = null;
        this.mScrollNodes.clear();
        this.mSmallViews.clear();
        this.mFloatRects.clear();
        this.mSideRects.clear();
    }

    private long getTimeSpend(long timeStart) {
        return SystemClock.uptimeMillis() - timeStart;
    }

    private void printMsg(String msg) {
        ColorLog.d(DBG, "LongshotDump", msg);
    }

    private void printSpend(String tag, long timeSpend) {
        StringBuilder sb = new StringBuilder();
        sb.append(tag);
        sb.append(" : spend=");
        sb.append(timeSpend);
        printMsg(sb.toString());
    }

    private void printTag(String tag) {
        printMsg("========== " + tag);
    }

    private void printScrollNodes(String prefix, List<ViewNode> scrollNodes) {
        if (DBG) {
            for (ViewNode viewNode : scrollNodes) {
                printMsg(prefix + viewNode);
                printScrollNodes(prefix + "    ", viewNode.getChildList());
            }
        }
    }

    private boolean canScrollVertically(View view) {
        if (view.canScrollVertically(1) || view.canScrollVertically(-1)) {
            return true;
        }
        return false;
    }

    private boolean isScrollableView(View view) {
        CharSequence accessibilityName = ColorViewCompat.getAccessibilityClassName(view);
        if (accessibilityName == null || (!accessibilityName.equals("android.widget.ListView") && !accessibilityName.equals("android.widget.ScrollView"))) {
            return false;
        }
        return true;
    }

    private boolean isScrollable(View view) {
        if (canScrollVertically(view) || isScrollableView(view)) {
            return true;
        }
        return false;
    }

    private boolean isInvalidScrollDistance(View view, int height) {
        int scrollExtent = view.computeVerticalScrollExtent();
        return (view.computeVerticalScrollRange() - scrollExtent) * height < this.mMinScrollDistance * scrollExtent;
    }

    private boolean isValidScrollNode(View view, boolean isChildScrollNode) {
        int width = view.getWidth();
        if (this.mScreenWidght <= 0 || width >= this.mScreenWidght / 2) {
            int height = view.getHeight();
            if (view instanceof ScrollView) {
                if (height < this.mMinScrollHeight) {
                    if (isChildScrollNode) {
                        ColorLog.d(DBG, "LongshotDump", "    ! isValidScrollNode 2 : height=" + height + ", mMinScrollHeight=" + this.mMinScrollHeight + ", isChildScrollNode=" + isChildScrollNode);
                        return false;
                    } else if (isInvalidScrollDistance(view, height)) {
                        ColorLog.d(DBG, "LongshotDump", "    ! isValidScrollNode 3 : height=" + height + ", mMinScrollHeight=" + this.mMinScrollHeight + ", mMinScrollDistance=" + this.mMinScrollDistance);
                        return false;
                    }
                }
            } else if (height < this.mMinListHeight) {
                Rect rect = new Rect();
                view.getBoundsOnScreen(rect, true);
                if (rect.bottom < this.mScreenHeight / 2) {
                    ColorLog.d(DBG, "LongshotDump", "    ! isValidScrollNode 4 : mScreenHeight=" + this.mScreenHeight + ", mMinListHeight=" + this.mMinListHeight + ", rect=" + rect + ", height=" + height);
                    return false;
                } else if (this.mScreenHeight - rect.bottom > this.mMinListHeight) {
                    ColorLog.d(DBG, "LongshotDump", "    ! isValidScrollNode 5 : mScreenHeight=" + this.mScreenHeight + ", mMinListHeight=" + this.mMinListHeight + ", rect=" + rect + ", height=" + height);
                    return false;
                }
            }
            return true;
        }
        ColorLog.d(DBG, "LongshotDump", "    ! isValidScrollNode 1 : mScreenWidght=" + this.mScreenWidght + ", width=" + width);
        return false;
    }

    private boolean isGalleryRoot(View view) {
        if (ColorLongshotUtils.isGallery(view.getContext().getPackageName()) && (view instanceof GLSurfaceView)) {
            return true;
        }
        return false;
    }

    private void dumpScrollNodes(ViewNode scrollNode, View view, Point minSize, List<View> small, int recursion, ColorLongshotViewInfo info) {
        if (view != null && view.isVisibleToUser()) {
            if (view instanceof ViewGroup) {
                view.mViewHooks.getLongshotController().findInfo(info);
                view.getBoundsOnScreen(this.mTempRect1, true);
                if (minSize != null) {
                    if (this.mTempRect1.width() >= minSize.x) {
                        if (this.mTempRect1.height() < minSize.y) {
                            if (small != null) {
                                small.add(view);
                            }
                            return;
                        }
                    }
                    return;
                }
                this.mDumpCount++;
                boolean scrollable = isScrollable(view);
                if (scrollable) {
                    long timeStart = SystemClock.uptimeMillis();
                    StringBuilder msg = new StringBuilder();
                    msg.append("    ");
                    AccessibilityNodeInfo nodeInfo = view.createAccessibilityNodeInfo();
                    if (nodeInfo == null || !nodeInfo.isScrollable()) {
                        long timeSpend = getTimeSpend(timeStart);
                        msg.append("---noScrollNode : ");
                        msg.append(view);
                        msg.append(":spend=");
                        msg.append(timeSpend);
                    } else {
                        boolean isChildScrollNode = scrollNode != null;
                        if (isValidScrollNode(view, isChildScrollNode)) {
                            view.getBoundsOnScreen(this.mTempRect2, false);
                            ViewNode viewNode = new ViewNode(view, nodeInfo.getClassName(), this.mTempRect1, this.mTempRect2);
                            if (isChildScrollNode) {
                                scrollNode.addChild(viewNode);
                                msg.append("isChildScrollNode : ");
                            } else {
                                this.mScrollNodes.add(viewNode);
                                msg.append("isScrollNode : ");
                            }
                            scrollNode = viewNode;
                            recursion++;
                            viewNode.setSpend(getTimeSpend(timeStart));
                            msg.append(viewNode);
                        } else {
                            msg.append("----rmScrollNode : ");
                        }
                    }
                    ColorLog.d(DBG, "LongshotDump", msg.toString());
                }
                if (recursion <= 1) {
                    ViewGroup group = (ViewGroup) view;
                    int childrenCount = group.getChildCount();
                    ArrayList<View> preorderedList = group.buildOrderedChildList();
                    boolean noPreorder = preorderedList == null;
                    boolean customOrder = noPreorder ? group.isChildrenDrawingOrderEnabled() : false;
                    int i = 0;
                    while (i < childrenCount) {
                        int childIndex = customOrder ? group.getChildDrawingOrder(childrenCount, i) : i;
                        View child = noPreorder ? group.getChildAt(childIndex) : (View) preorderedList.get(childIndex);
                        if (scrollable) {
                            minSize = null;
                        }
                        if (scrollable) {
                            small = null;
                        }
                        dumpScrollNodes(scrollNode, child, minSize, small, recursion, info);
                        i++;
                    }
                    if (preorderedList != null) {
                        preorderedList.clear();
                    }
                } else {
                    return;
                }
            }
            view.findViewsLongshotInfo(info);
        }
    }

    private void dumpViewHierarchy(ColorLongshotDump result, View view) {
        long timeStart = SystemClock.uptimeMillis();
        String tag = "dumpViewHierarchy";
        printTag(tag);
        this.mViewInfo.reset();
        dumpScrollNodes(null, view, null, null, 0, this.mViewInfo);
        if (this.mScrollNodes.isEmpty()) {
            for (View v : this.mSmallViews) {
                dumpScrollNodes(null, v, null, null, 0, this.mViewInfo);
            }
        }
        this.mSmallViews.clear();
        result.setDumpCount(this.mDumpCount);
        printScrollNodes(tag + " : ", this.mScrollNodes);
        result.setScrollCount(this.mScrollNodes.size());
        long timeSpend = getTimeSpend(timeStart);
        printSpend(tag, timeSpend);
        result.setSpendDump(timeSpend);
    }

    private String getAccessibilityName(ViewNode viewNode) {
        CharSequence accessibilityName = viewNode.getAccessibilityName();
        if (accessibilityName != null) {
            return accessibilityName.toString();
        }
        return null;
    }

    private void selectScrollNodes(List<ViewNode> scrollNodes) {
        if (!scrollNodes.isEmpty()) {
            while (scrollNodes.size() > 1) {
                scrollNodes.remove(0);
            }
            this.mScrollNode = (ViewNode) scrollNodes.get(0);
            selectScrollNodes(this.mScrollNode.getChildList());
        }
    }

    private boolean updateCoverRect(ColorLongshotViewUtils utils, Rect dstRect, Rect srcRect, Rect coverRect) {
        if (!Rect.intersects(coverRect, srcRect)) {
            return false;
        }
        int top;
        int bottom;
        Rect rect = new Rect(srcRect);
        if (rect.bottom - coverRect.bottom > coverRect.top - rect.top) {
            rect.top = coverRect.bottom;
        } else {
            rect.bottom = coverRect.top;
        }
        boolean update = isLargeHeight(rect, srcRect);
        if (!update) {
            top = Math.max(srcRect.top, coverRect.top);
            bottom = Math.min(srcRect.bottom, coverRect.bottom);
            if (bottom > top && bottom - top <= this.mCoverHeight) {
                update = true;
            } else if (utils.isBottomBarRect(coverRect, srcRect)) {
                update = true;
            }
        }
        if (update) {
            this.mFloatRects.add(new Rect(coverRect));
            top = Math.max(dstRect.top, rect.top);
            bottom = Math.min(dstRect.bottom, rect.bottom);
            if (bottom <= top) {
                dstRect.setEmpty();
            } else {
                dstRect.top = top;
                dstRect.bottom = bottom;
            }
        }
        return update;
    }

    private boolean isSmallWidth(Rect dst, Rect src) {
        return dst.width() < src.width() / 3;
    }

    private boolean isLargeHeight(Rect dst, Rect src) {
        return dst.height() > (src.height() * 2) / 5;
    }

    private boolean isVerticalBar(Rect dst, Rect src) {
        return isLargeHeight(dst, src) ? isSmallWidth(dst, src) : false;
    }

    private boolean isInvalidIntersect(Rect rect, Rect srcRect) {
        if (rect.intersect(srcRect) && !isVerticalBar(rect, srcRect)) {
            return false;
        }
        return true;
    }

    private void printContentView(ColorLongshotViewContent content, String tag, Rect rect) {
        if (DBG) {
            StringBuilder msg = new StringBuilder();
            msg.append("    ");
            msg.append(tag);
            msg.append(" : {");
            msg.append(content);
            if (rect != null) {
                msg.append("} => ");
                msg.append(rect);
            }
            printMsg(msg.toString());
        }
    }

    private void checkCoverContents(ColorLongshotViewUtils utils, List<ColorLongshotViewContent> coverContents, Rect rootRect) {
        List<ColorLongshotViewContent> smallContents;
        List<ColorLongshotViewContent> largeContents = new ArrayList();
        Map<ColorLongshotViewContent, List<ColorLongshotViewContent>> smallContentMap = new HashMap();
        for (ColorLongshotViewContent coverContent : coverContents) {
            ColorLongshotViewContent parent = coverContent.getParent();
            if (parent != null) {
                if (utils.isLargeCoverRect(parent.getRect(), rootRect, false)) {
                    largeContents.add(coverContent);
                } else if (smallContentMap.containsKey(parent)) {
                    ((List) smallContentMap.get(parent)).add(coverContent);
                } else {
                    smallContents = new ArrayList();
                    smallContents.add(coverContent);
                    smallContentMap.put(parent, smallContents);
                }
            }
        }
        coverContents.clear();
        coverContents.addAll(largeContents);
        for (Entry<ColorLongshotViewContent, List<ColorLongshotViewContent>> entry : smallContentMap.entrySet()) {
            smallContents = (List) entry.getValue();
            if (!smallContents.isEmpty()) {
                this.mTempRect1.setEmpty();
                for (ColorLongshotViewContent smallContent : smallContents) {
                    this.mTempRect1.union(smallContent.getRect());
                }
                coverContents.add(new ColorLongshotViewContent(((ColorLongshotViewContent) entry.getKey()).getView(), this.mTempRect1, null));
            }
        }
    }

    private void calcScrollRectForViews(ColorLongshotViewUtils utils, Rect dstRect, Rect srcRect, View view) {
        List<ColorLongshotViewContent> coverContents = new ArrayList();
        List<ColorLongshotViewContent> sideContents = new ArrayList();
        ViewParent parent = view.getParent();
        while (parent instanceof ViewGroup) {
            View group = (ViewGroup) parent;
            if (group.getChildCount() > 1) {
                utils.findCoverRect(1, group, view, coverContents, sideContents, srcRect, null, false);
            } else {
                group.getBoundsOnScreen(this.mTempRect1, true);
                ColorLog.d(DBG, "LongshotDump", "    nofindCoverRect : rootRect=" + srcRect + ", srcRect=" + this.mTempRect1 + ", group=" + group);
            }
            view = group;
            parent = group.getParent();
        }
        Collections.sort(coverContents, CONTENT_COMPARATOR);
        checkCoverContents(utils, coverContents, srcRect);
        printMsg("-------------------------calcScrollRectForViews : coverContents=" + coverContents.size() + ", sideContents=" + sideContents.size());
        for (ColorLongshotViewContent coverContent : coverContents) {
            printContentView(coverContent, updateCoverRect(utils, dstRect, srcRect, coverContent.getRect()) ? "update" : "skip  ", dstRect);
        }
        for (ColorLongshotViewContent sideContent : sideContents) {
            this.mSideRects.add(new Rect(sideContent.getRect()));
            printContentView(sideContent, "sidebar", null);
        }
    }

    private void calcScrollRectForWindow(ColorLongshotViewUtils utils, Rect dstRect, Rect srcRect, ColorWindowNode window) {
        this.mTempRect1.set(window.getCoverRect());
        if (!isInvalidIntersect(this.mTempRect1, srcRect)) {
            boolean result = updateCoverRect(utils, dstRect, srcRect, this.mTempRect1);
            if (DBG) {
                String tag = result ? "update" : "skip  ";
                StringBuilder msg = new StringBuilder();
                msg.append("    ");
                msg.append(tag);
                msg.append(" : {");
                msg.append(window);
                msg.append("} => ");
                msg.append(dstRect);
                printMsg(msg.toString());
            }
        }
    }

    private void calcScrollRectForWindows(ColorLongshotViewUtils utils, Rect dstRect, Rect srcRect, List<ColorWindowNode> windows) {
        if (windows != null) {
            for (ColorWindowNode window : windows) {
                calcScrollRectForWindow(utils, dstRect, srcRect, window);
            }
        }
    }

    private void calcScrollRect(ColorLongshotDump result, ColorLongshotViewUtils utils, ViewNode scrollNode, List<ColorWindowNode> list, List<ColorWindowNode> list2) {
        ColorLog.d(DBG, "LongshotDump", "==========calcScrollRect====: ");
        if (scrollNode == null) {
            ColorLog.d(DBG, "LongshotDump", "  calcScrollRect, scrollNode=null");
            return;
        }
        long timeStart = SystemClock.uptimeMillis();
        String tag = "calcScrollRect";
        printTag(tag);
        View view = scrollNode.getView();
        Rect srcRect = new Rect();
        view.getBoundsOnScreen(srcRect, true);
        Rect dstRect = new Rect(srcRect);
        calcScrollRectForViews(utils, dstRect, srcRect, view);
        scrollNode.setScrollRect(dstRect);
        result.setScrollRect(dstRect);
        printMsg(tag + " : scrollRect=" + dstRect);
        long timeSpend = getTimeSpend(timeStart);
        printSpend(tag, timeSpend);
        result.setSpendCalc(timeSpend);
    }

    private void calcScrollRects(ColorLongshotDump result, ColorLongshotViewUtils utils, List<ViewNode> scrollNodes, List<ColorWindowNode> systemWindows, List<ColorWindowNode> floatWindows) {
        for (ViewNode scrollNode : scrollNodes) {
            result.setScrollComponent(ColorLongshotComponentName.create(scrollNode.getView(), getAccessibilityName(scrollNode)));
            calcScrollRect(result, utils, scrollNode, systemWindows, floatWindows);
            calcScrollRects(result, utils, scrollNode.getChildList(), systemWindows, floatWindows);
        }
    }

    private void reportDumpResult(Context context, ColorLongshotDump result) {
        ColorScreenshotManager sm = ColorLongshotUtils.getScreenshotManager(context);
        if (sm != null) {
            sm.reportLongshotDumpResult(result);
        }
    }

    private void scrollNodesToJson(JSONArray jsScrollArray, List<ViewNode> scrollNodes, boolean isChild) {
        for (ViewNode viewNode : scrollNodes) {
            View view = viewNode.getView();
            if (view instanceof ViewGroup) {
                ArrayList preorderedList = null;
                try {
                    ViewGroup group = (ViewGroup) view;
                    JSONObject jsScrollView = new JSONObject();
                    jsScrollView.put(JSON_PARENT_HASH, System.identityHashCode(group));
                    group.getBoundsOnScreen(this.mTempRect1, false);
                    jsScrollView.put(JSON_PARENT_RECT_FULL, this.mTempRect1.flattenToString());
                    group.getBoundsOnScreen(this.mTempRect1, true);
                    jsScrollView.put(JSON_PARENT_RECT_CLIP, this.mTempRect1.flattenToString());
                    jsScrollView.put(JSON_SCROLL_RECT, viewNode.getScrollRect().flattenToString());
                    jsScrollView.put(JSON_SCROLL_CHILD, isChild);
                    JSONArray jsChildArray = new JSONArray();
                    int childrenCount = group.getChildCount();
                    preorderedList = group.buildOrderedChildList();
                    boolean noPreorder = preorderedList == null;
                    boolean customOrder = noPreorder ? group.isChildrenDrawingOrderEnabled() : false;
                    int i = 0;
                    while (i < childrenCount) {
                        int childIndex = customOrder ? group.getChildDrawingOrder(childrenCount, i) : i;
                        View child = noPreorder ? group.getChildAt(childIndex) : (View) preorderedList.get(childIndex);
                        if (child != null && child.isVisibleToUser()) {
                            JSONObject jsChild = new JSONObject();
                            jsChild.put(JSON_CHILD_HASH, System.identityHashCode(child));
                            child.getBoundsOnScreen(this.mTempRect1, false);
                            jsChild.put(JSON_CHILD_RECT_FULL, this.mTempRect1.flattenToString());
                            child.getBoundsOnScreen(this.mTempRect1, true);
                            jsChild.put(JSON_CHILD_RECT_CLIP, this.mTempRect1.flattenToString());
                            if (group instanceof ScrollView) {
                                jsChild.put(JSON_CHILD_SCROLLY, child.getScrollY());
                            }
                            jsChildArray.put(jsChild);
                        }
                        i++;
                    }
                    jsScrollView.put(JSON_CHILD_LIST, jsChildArray);
                    jsScrollArray.put(jsScrollView);
                    if (preorderedList != null) {
                        preorderedList.clear();
                    }
                } catch (JSONException e) {
                    ColorLog.e(DBG, "LongshotDump", "scrollNodesToJson:" + Log.getStackTraceString(e));
                    if (preorderedList != null) {
                        preorderedList.clear();
                    }
                } catch (Exception e2) {
                    ColorLog.e(DBG, "LongshotDump", "scrollNodesToJson:" + Log.getStackTraceString(e2));
                    if (preorderedList != null) {
                        preorderedList.clear();
                    }
                } catch (Throwable th) {
                    if (preorderedList != null) {
                        preorderedList.clear();
                    }
                }
            }
            scrollNodesToJson(jsScrollArray, viewNode.getChildList(), true);
        }
    }

    private List<ColorWindowNode> mergeWindowList(List<ColorWindowNode> systemWindows, List<ColorWindowNode> floatWindows) {
        List<ColorWindowNode> windows = new ArrayList();
        if (systemWindows != null) {
            windows.addAll(systemWindows);
        }
        if (floatWindows != null) {
            windows.addAll(floatWindows);
        }
        return windows;
    }

    private JSONArray windowNodesToJson(List<ColorWindowNode> systemWindows, List<ColorWindowNode> floatWindows) {
        JSONArray jsWindowArray = null;
        List<ColorWindowNode> windows = mergeWindowList(systemWindows, floatWindows);
        if (!windows.isEmpty()) {
            jsWindowArray = new JSONArray();
            for (ColorWindowNode window : windows) {
                try {
                    JSONObject jsWindow = new JSONObject();
                    jsWindow.put(JSON_WINDOW_STATBAR, window.isStatusBar());
                    jsWindow.put(JSON_WINDOW_NAVIBAR, window.isNavigationBar());
                    jsWindow.put(JSON_WINDOW_LAYER, window.getSurfaceLayer());
                    jsWindow.put(JSON_WINDOW_RECT_DECOR, window.getDecorRect().flattenToString());
                    jsWindow.put(JSON_WINDOW_RECT_VISIBLE, window.getCoverRect().flattenToString());
                    jsWindowArray.put(jsWindow);
                } catch (JSONException e) {
                    ColorLog.e(DBG, "LongshotDump", "windowNodesToJson:" + Log.getStackTraceString(e));
                } catch (Exception e2) {
                    ColorLog.e(DBG, "LongshotDump", "windowNodesToJson:" + Log.getStackTraceString(e2));
                }
            }
        }
        return jsWindowArray;
    }

    private JSONArray sideRectsToJson(List<Rect> sideRects) {
        JSONArray jsSideArray = null;
        if (!sideRects.isEmpty()) {
            jsSideArray = new JSONArray();
            for (Rect rect : sideRects) {
                try {
                    JSONObject jsSide = new JSONObject();
                    jsSide.put(JSON_SIDE_RECT, rect.flattenToString());
                    jsSideArray.put(jsSide);
                } catch (JSONException e) {
                    ColorLog.e(DBG, "LongshotDump", "sideRectsToJson:" + Log.getStackTraceString(e));
                } catch (Exception e2) {
                    ColorLog.e(DBG, "LongshotDump", "sideRectsToJson:" + Log.getStackTraceString(e2));
                }
            }
        }
        return jsSideArray;
    }

    private JSONArray floatRectsToJson(List<Rect> floatRects) {
        JSONArray jsFloatArray = null;
        if (!floatRects.isEmpty()) {
            jsFloatArray = new JSONArray();
            for (Rect rect : floatRects) {
                try {
                    JSONObject jsFloat = new JSONObject();
                    jsFloat.put(JSON_FLOAT_RECT, rect.flattenToString());
                    jsFloatArray.put(jsFloat);
                } catch (JSONException e) {
                    ColorLog.e(DBG, "LongshotDump", "floatRectsToJson:" + Log.getStackTraceString(e));
                } catch (Exception e2) {
                    ColorLog.e(DBG, "LongshotDump", "floatRectsToJson:" + Log.getStackTraceString(e2));
                }
            }
        }
        return jsFloatArray;
    }

    private JSONObject getJSONObject(JSONObject jsonNode) {
        if (jsonNode == null) {
            return new JSONObject();
        }
        return jsonNode;
    }

    /* JADX WARNING: Removed duplicated region for block: B:29:0x0112  */
    /* JADX WARNING: Removed duplicated region for block: B:9:0x002d  */
    /* JADX WARNING: Removed duplicated region for block: B:9:0x002d  */
    /* JADX WARNING: Removed duplicated region for block: B:29:0x0112  */
    /* JADX WARNING: Removed duplicated region for block: B:29:0x0112  */
    /* JADX WARNING: Removed duplicated region for block: B:9:0x002d  */
    /* JADX WARNING: Removed duplicated region for block: B:9:0x002d  */
    /* JADX WARNING: Removed duplicated region for block: B:29:0x0112  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private String packJsonNode(ColorLongshotDump result, PrintWriter pw, List<ColorWindowNode> systemWindows, List<ColorWindowNode> floatWindows) {
        JSONException e;
        String jsonPack;
        long timeSpend;
        Exception e2;
        long timeStart = SystemClock.uptimeMillis();
        String tag = "packJsonNode";
        printTag(tag);
        JSONObject jsonNode = null;
        try {
            if (this.mViewInfo.isUnsupported()) {
                JSONObject jsonNode2 = new JSONObject();
                try {
                    jsonNode2.put(JSON_VIEW_UNSUPPORTED, true);
                    jsonNode = jsonNode2;
                } catch (JSONException e3) {
                    e = e3;
                    jsonNode = jsonNode2;
                    ColorLog.e(DBG, "LongshotDump", "packJsonNode:" + Log.getStackTraceString(e));
                    if (jsonNode != null) {
                    }
                    timeSpend = getTimeSpend(timeStart);
                    printSpend(tag, timeSpend);
                    result.setSpendPack(timeSpend);
                    return jsonPack;
                } catch (Exception e4) {
                    e2 = e4;
                    jsonNode = jsonNode2;
                    ColorLog.e(DBG, "LongshotDump", "packJsonNode:" + Log.getStackTraceString(e2));
                    if (jsonNode != null) {
                    }
                    timeSpend = getTimeSpend(timeStart);
                    printSpend(tag, timeSpend);
                    result.setSpendPack(timeSpend);
                    return jsonPack;
                }
            }
            if (!this.mScrollNodes.isEmpty()) {
                JSONArray jsScrollArray = new JSONArray();
                scrollNodesToJson(jsScrollArray, this.mScrollNodes, false);
                jsonNode = getJSONObject(null);
                jsonNode.put(JSON_SCROLL_LIST, jsScrollArray);
            }
            JSONArray jsSideArray = sideRectsToJson(this.mSideRects);
            if (jsSideArray != null) {
                jsonNode = getJSONObject(jsonNode);
                jsonNode.put(JSON_SIDE_LIST, jsSideArray);
            }
            JSONArray jsFloatArray = floatRectsToJson(this.mFloatRects);
            if (jsFloatArray != null) {
                jsonNode = getJSONObject(jsonNode);
                jsonNode.put(JSON_FLOAT_LIST, jsFloatArray);
            }
            JSONArray jsWindowArray = windowNodesToJson(systemWindows, floatWindows);
            if (jsWindowArray != null) {
                jsonNode = getJSONObject(jsonNode);
                jsonNode.put(JSON_WINDOW_LIST, jsWindowArray);
            }
        } catch (JSONException e5) {
            e = e5;
            ColorLog.e(DBG, "LongshotDump", "packJsonNode:" + Log.getStackTraceString(e));
            if (jsonNode != null) {
            }
            timeSpend = getTimeSpend(timeStart);
            printSpend(tag, timeSpend);
            result.setSpendPack(timeSpend);
            return jsonPack;
        } catch (Exception e6) {
            e2 = e6;
            ColorLog.e(DBG, "LongshotDump", "packJsonNode:" + Log.getStackTraceString(e2));
            if (jsonNode != null) {
            }
            timeSpend = getTimeSpend(timeStart);
            printSpend(tag, timeSpend);
            result.setSpendPack(timeSpend);
            return jsonPack;
        }
        jsonPack = jsonNode != null ? jsonNode.toString() : null;
        timeSpend = getTimeSpend(timeStart);
        printSpend(tag, timeSpend);
        result.setSpendPack(timeSpend);
        return jsonPack;
    }
}
