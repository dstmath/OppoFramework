package android.view;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.Rect;
import android.opengl.GLSurfaceView;
import android.os.ParcelFileDescriptor;
import android.os.StrictMode;
import android.os.SystemClock;
import android.util.Log;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.ScrollView;
import com.android.internal.util.FastPrintWriter;
import com.color.screenshot.ColorLongshotComponentName;
import com.color.screenshot.ColorLongshotDump;
import com.color.screenshot.ColorLongshotUtils;
import com.color.screenshot.ColorLongshotViewInfo;
import com.color.screenshot.ColorScreenshotManager;
import com.color.util.ColorLog;
import com.color.util.ColorTypeCastingHelper;
import com.color.view.analysis.ColorWindowNode;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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

    public ColorWindowNode collectWindow(View view, boolean isStatusBar, boolean isNavigationBar) {
        return new ColorWindowNode(view, isStatusBar, isNavigationBar);
    }

    public void dumpViewRoot(ViewRootImpl viewAncestor, ParcelFileDescriptor fd, List<ColorWindowNode> systemWindows, List<ColorWindowNode> floatWindows, boolean isLongshot) {
        Throwable th;
        Exception e;
        StrictMode.ThreadPolicy oldPolicy = StrictMode.allowThreadDiskWrites();
        String tag = isLongshot ? "dumpLongshot" : "dumpScreenshot";
        try {
            boolean z = DBG;
            ColorLog.d(z, TAG, tag + " : viewAncestor.mView=" + viewAncestor.mView);
            PrintWriter pw = new FastPrintWriter(new FileOutputStream(fd.getFileDescriptor()));
            Resources res = viewAncestor.mContext.getResources();
            this.mMinScrollHeight = res.getDimensionPixelOffset(201655598);
            this.mMinScrollDistance = res.getDimensionPixelOffset(201655599);
            this.mMinListHeight = res.getDimensionPixelOffset(201655397);
            this.mCoverHeight = res.getDimensionPixelOffset(201655398);
            this.mScreenHeight = res.getDisplayMetrics().heightPixels;
            this.mScreenWidght = res.getDisplayMetrics().widthPixels;
            if (isLongshot) {
                dumpLongshot(viewAncestor, pw, tag, null, systemWindows, floatWindows);
            } else {
                dumpScreenshot(viewAncestor, pw, tag);
            }
            try {
                String jsonPack = packJsonNode(null, pw, systemWindows, floatWindows);
                if (jsonPack != null) {
                    pw.println(jsonPack);
                }
                pw.flush();
            } catch (Exception e2) {
                e = e2;
                try {
                    boolean z2 = DBG;
                    ColorLog.e(z2, TAG, tag + " ERROR : " + Log.getStackTraceString(e));
                    reportDumpResult(viewAncestor.mContext, null);
                    clearList();
                    IoUtils.closeQuietly(fd);
                    StrictMode.setThreadPolicy(oldPolicy);
                } catch (Throwable th2) {
                    th = th2;
                    reportDumpResult(viewAncestor.mContext, null);
                    clearList();
                    IoUtils.closeQuietly(fd);
                    StrictMode.setThreadPolicy(oldPolicy);
                    throw th;
                }
            }
        } catch (Exception e3) {
            e = e3;
            boolean z22 = DBG;
            ColorLog.e(z22, TAG, tag + " ERROR : " + Log.getStackTraceString(e));
            reportDumpResult(viewAncestor.mContext, null);
            clearList();
            IoUtils.closeQuietly(fd);
            StrictMode.setThreadPolicy(oldPolicy);
        } catch (Throwable th3) {
            th = th3;
            reportDumpResult(viewAncestor.mContext, null);
            clearList();
            IoUtils.closeQuietly(fd);
            StrictMode.setThreadPolicy(oldPolicy);
            throw th;
        }
        reportDumpResult(viewAncestor.mContext, null);
        clearList();
        IoUtils.closeQuietly(fd);
        StrictMode.setThreadPolicy(oldPolicy);
    }

    public void injectInputBegin() {
        ViewNode viewNode = this.mScrollNode;
        if (viewNode != null) {
            viewNode.disableOverScroll();
        }
    }

    public void injectInputEnd() {
        ViewNode viewNode = this.mScrollNode;
        if (viewNode != null) {
            viewNode.resetOverScroll();
        }
    }

    public void reset() {
        ColorLog.d(DBG, TAG, "reset ViewDump");
        injectInputEnd();
        this.mScrollNode = null;
        clearList();
    }

    private long getTimeSpend(long timeStart) {
        return SystemClock.uptimeMillis() - timeStart;
    }

    private void printMsg(String msg) {
        ColorLog.d(DBG, TAG, msg);
    }

    private void printSpend(String tag, long timeSpend) {
        printMsg(tag + " : spend=" + timeSpend);
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
        if (!view.canScrollVertically(1) && !view.canScrollVertically(-1)) {
            return false;
        }
        return true;
    }

    private boolean isScrollableView(View view) {
        CharSequence accessibilityName = ColorViewCompat.getAccessibilityClassName(view);
        if (accessibilityName == null) {
            return false;
        }
        if (!accessibilityName.equals("android.widget.ListView") && !accessibilityName.equals("android.widget.ScrollView")) {
            return false;
        }
        return true;
    }

    private boolean isScrollable(View view) {
        if (!canScrollVertically(view) && !isScrollableView(view)) {
            return false;
        }
        return true;
    }

    private boolean isInvalidScrollDistance(View view, int height) {
        int scrollExtent = view.computeVerticalScrollExtent();
        return (view.computeVerticalScrollRange() - scrollExtent) * height < this.mMinScrollDistance * scrollExtent;
    }

    private boolean isValidScrollNode(View view, boolean isChildScrollNode) {
        int width = view.getWidth();
        int i = this.mScreenWidght;
        if (i <= 0 || width >= i / 2) {
            int height = view.getHeight();
            if (view instanceof ScrollView) {
                if (height < this.mMinScrollHeight) {
                    if (isChildScrollNode) {
                        boolean z = DBG;
                        ColorLog.d(z, TAG, "    ! isValidScrollNode 2 : height=" + height + ", mMinScrollHeight=" + this.mMinScrollHeight + ", isChildScrollNode=" + isChildScrollNode);
                        return false;
                    } else if (isInvalidScrollDistance(view, height)) {
                        boolean z2 = DBG;
                        ColorLog.d(z2, TAG, "    ! isValidScrollNode 3 : height=" + height + ", mMinScrollHeight=" + this.mMinScrollHeight + ", mMinScrollDistance=" + this.mMinScrollDistance);
                        return false;
                    }
                }
            } else if (height < this.mMinListHeight) {
                Rect rect = new Rect();
                view.getBoundsOnScreen(rect, true);
                int i2 = rect.bottom;
                int i3 = this.mScreenHeight;
                if (i2 < i3 / 2) {
                    boolean z3 = DBG;
                    ColorLog.d(z3, TAG, "    ! isValidScrollNode 4 : mScreenHeight=" + this.mScreenHeight + ", mMinListHeight=" + this.mMinListHeight + ", rect=" + rect + ", height=" + height);
                    return false;
                } else if (i3 - rect.bottom > this.mMinListHeight) {
                    boolean z4 = DBG;
                    ColorLog.d(z4, TAG, "    ! isValidScrollNode 5 : mScreenHeight=" + this.mScreenHeight + ", mMinListHeight=" + this.mMinListHeight + ", rect=" + rect + ", height=" + height);
                    return false;
                }
            }
            return true;
        }
        boolean z5 = DBG;
        ColorLog.d(z5, TAG, "    ! isValidScrollNode 1 : mScreenWidght=" + this.mScreenWidght + ", width=" + width);
        return false;
    }

    private boolean isGalleryRoot(View view) {
        if (ColorLongshotUtils.isGallery(view.getContext().getPackageName()) && (view instanceof GLSurfaceView)) {
            return true;
        }
        return false;
    }

    private void appendLongshotInfo(View view, ColorLongshotViewInfo info) {
    }

    private void dumpViewNodes(View view, ColorLongshotViewInfo info) {
        if (view != null && view.isVisibleToUser()) {
            appendLongshotInfo(view, info);
            OppoBaseView oppoBaseView = (OppoBaseView) ColorTypeCastingHelper.typeCasting(OppoBaseView.class, view);
            if (view instanceof ViewGroup) {
                oppoBaseView.findViewsLongshotInfo(info);
                ViewGroup group = (ViewGroup) view;
                int childrenCount = group.getChildCount();
                ArrayList<View> preorderedList = group.buildOrderedChildList();
                boolean customOrder = false;
                boolean noPreorder = preorderedList == null;
                if (noPreorder && group.isChildrenDrawingOrderEnabled()) {
                    customOrder = true;
                }
                for (int i = 0; i < childrenCount; i++) {
                    int childIndex = customOrder ? group.getChildDrawingOrder(childrenCount, i) : i;
                    dumpViewNodes(noPreorder ? group.getChildAt(childIndex) : preorderedList.get(childIndex), info);
                }
                if (preorderedList != null) {
                    preorderedList.clear();
                    return;
                }
                return;
            }
            oppoBaseView.findViewsLongshotInfo(info);
        }
    }

    private void dumpScrollNodes(ViewNode scrollNode, View view, Point minSize, List<View> small, int recursion, ColorLongshotViewInfo info) {
        ViewNode scrollNode2;
        int recursion2;
        int recursion3;
        ViewNode scrollNode3 = scrollNode;
        if (view != null && view.isVisibleToUser()) {
            OppoBaseView oppoBaseView = (OppoBaseView) ColorTypeCastingHelper.typeCasting(OppoBaseView.class, view);
            if (view instanceof ViewGroup) {
                oppoBaseView.findViewsLongshotInfo(info);
                view.getBoundsOnScreen(this.mTempRect1, true);
                if (minSize != null) {
                    if (this.mTempRect1.width() >= minSize.x) {
                        if (this.mTempRect1.height() < minSize.y) {
                            if (small != null) {
                                small.add(view);
                                return;
                            }
                            return;
                        }
                    } else {
                        return;
                    }
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
                        recursion3 = recursion;
                    } else {
                        boolean isChildScrollNode = scrollNode3 != null;
                        if (isValidScrollNode(view, isChildScrollNode)) {
                            view.getBoundsOnScreen(this.mTempRect2, false);
                            ViewNode viewNode = new ViewNode(view, nodeInfo.getClassName(), this.mTempRect1, this.mTempRect2);
                            if (isChildScrollNode) {
                                scrollNode3.addChild(viewNode);
                                msg.append("isChildScrollNode : ");
                            } else {
                                this.mScrollNodes.add(viewNode);
                                msg.append("isScrollNode : ");
                            }
                            scrollNode3 = viewNode;
                            viewNode.setSpend(getTimeSpend(timeStart));
                            msg.append(viewNode);
                            recursion3 = recursion + 1;
                        } else {
                            msg.append("----rmScrollNode : ");
                            recursion3 = recursion;
                        }
                    }
                    ColorLog.d(DBG, TAG, msg.toString());
                    scrollNode2 = scrollNode3;
                    recursion2 = recursion3;
                } else {
                    recursion2 = recursion;
                    scrollNode2 = scrollNode3;
                }
                if (recursion2 <= 1) {
                    ViewGroup group = (ViewGroup) view;
                    int childrenCount = group.getChildCount();
                    ArrayList<View> preorderedList = group.buildOrderedChildList();
                    boolean noPreorder = preorderedList == null;
                    boolean customOrder = noPreorder && group.isChildrenDrawingOrderEnabled();
                    Point minSize2 = minSize;
                    List<View> small2 = small;
                    int i = 0;
                    while (i < childrenCount) {
                        int childIndex = customOrder ? group.getChildDrawingOrder(childrenCount, i) : i;
                        View child = noPreorder ? group.getChildAt(childIndex) : preorderedList.get(childIndex);
                        List<View> list = null;
                        minSize2 = scrollable ? null : minSize2;
                        if (!scrollable) {
                            list = small2;
                        }
                        small2 = list;
                        dumpScrollNodes(scrollNode2, child, minSize2, small2, recursion2, info);
                        i++;
                        preorderedList = preorderedList;
                    }
                    if (preorderedList != null) {
                        preorderedList.clear();
                        return;
                    }
                    return;
                }
                return;
            }
            oppoBaseView.findViewsLongshotInfo(info);
        }
    }

    private void dumpHierarchyLongshot(ColorLongshotDump result, View view) {
        long timeStart = SystemClock.uptimeMillis();
        printTag("dumpHierarchyLongshot");
        this.mViewInfo.reset();
        dumpScrollNodes(null, view, null, null, 0, this.mViewInfo);
        if (this.mScrollNodes.isEmpty()) {
            for (View v : this.mSmallViews) {
                dumpScrollNodes(null, v, null, null, 0, this.mViewInfo);
            }
        }
        this.mSmallViews.clear();
        printScrollNodes("dumpHierarchyLongshot : ", this.mScrollNodes);
        long timeSpend = getTimeSpend(timeStart);
        printSpend("dumpHierarchyLongshot", timeSpend);
        if (result != null) {
            result.setScrollCount(this.mScrollNodes.size());
            result.setDumpCount(this.mDumpCount);
            result.setSpendDump(timeSpend);
        }
    }

    private void dumpHierarchyScreenshot(View view) {
        long timeStart = SystemClock.uptimeMillis();
        printTag("dumpHierarchyScreenshot");
        this.mViewInfo.reset();
        dumpViewNodes(view, this.mViewInfo);
        printSpend("dumpHierarchyScreenshot", getTimeSpend(timeStart));
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
            this.mScrollNode = scrollNodes.get(0);
            selectScrollNodes(this.mScrollNode.getChildList());
        }
    }

    private boolean updateCoverRect(ColorLongshotViewUtils utils, Rect dstRect, Rect srcRect, Rect coverRect) {
        printMsg("    updateCoverRect : dstRect= " + dstRect + ", srcRect= " + srcRect + ", coverRect= " + coverRect);
        if (!Rect.intersects(coverRect, srcRect)) {
            return false;
        }
        Rect rect = new Rect(srcRect);
        int diffT = coverRect.top - rect.top;
        int diffB = rect.bottom - coverRect.bottom;
        if (diffT == 0 && diffB == 0) {
            printMsg("    updateCoverRect : diffT = diffB = 0");
            return false;
        }
        if (diffB > diffT) {
            rect.top = coverRect.bottom;
        } else {
            rect.bottom = coverRect.top;
        }
        boolean update = isLargeHeight(rect, srcRect);
        if (!update) {
            int top = Math.max(srcRect.top, coverRect.top);
            int bottom = Math.min(srcRect.bottom, coverRect.bottom);
            if (bottom > top && bottom - top <= this.mCoverHeight) {
                update = true;
            } else if (utils.isBottomBarRect(coverRect, srcRect)) {
                update = true;
            }
        }
        if (update) {
            this.mFloatRects.add(new Rect(coverRect));
            int top2 = Math.max(dstRect.top, rect.top);
            int bottom2 = Math.min(dstRect.bottom, rect.bottom);
            if (bottom2 <= top2) {
                dstRect.setEmpty();
            } else {
                dstRect.top = top2;
                dstRect.bottom = bottom2;
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
        return isLargeHeight(dst, src) && isSmallWidth(dst, src);
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
        List<ColorLongshotViewContent> largeContents = new ArrayList<>();
        Map<ColorLongshotViewContent, List<ColorLongshotViewContent>> smallContentMap = new HashMap<>();
        for (ColorLongshotViewContent coverContent : coverContents) {
            ColorLongshotViewContent parent = coverContent.getParent();
            if (parent != null) {
                if (utils.isLargeCoverRect(parent.getRect(), rootRect, false)) {
                    largeContents.add(coverContent);
                } else if (smallContentMap.containsKey(parent)) {
                    smallContentMap.get(parent).add(coverContent);
                } else {
                    List<ColorLongshotViewContent> smallContents = new ArrayList<>();
                    smallContents.add(coverContent);
                    smallContentMap.put(parent, smallContents);
                }
            }
        }
        coverContents.clear();
        coverContents.addAll(largeContents);
        for (Map.Entry<ColorLongshotViewContent, List<ColorLongshotViewContent>> entry : smallContentMap.entrySet()) {
            List<ColorLongshotViewContent> smallContents2 = entry.getValue();
            if (!smallContents2.isEmpty()) {
                this.mTempRect1.setEmpty();
                for (ColorLongshotViewContent smallContent : smallContents2) {
                    this.mTempRect1.union(smallContent.getRect());
                }
                coverContents.add(new ColorLongshotViewContent(entry.getKey().getView(), this.mTempRect1, (ColorLongshotViewContent) null));
            }
        }
    }

    /* JADX INFO: Multiple debug info for r14v4 'group'  android.view.ViewGroup: [D('parent' android.view.ViewParent), D('group' android.view.ViewGroup)] */
    private void calcScrollRectForViews(ColorLongshotViewUtils utils, Rect dstRect, Rect srcRect, View view) {
        ViewGroup group;
        List<ColorLongshotViewContent> coverContents = new ArrayList<>();
        List<ColorLongshotViewContent> sideContents = new ArrayList<>();
        View view2 = view;
        ViewParent parent = view.getParent();
        while (parent instanceof ViewGroup) {
            ViewGroup group2 = (ViewGroup) parent;
            if (group2.getChildCount() > 1) {
                group = group2;
                utils.findCoverRect(1, group2, view2, coverContents, sideContents, srcRect, (ColorLongshotViewContent) null, false);
            } else {
                group = group2;
                group.getBoundsOnScreen(this.mTempRect1, true);
                boolean z = DBG;
                ColorLog.d(z, TAG, "    nofindCoverRect : rootRect=" + srcRect + ", srcRect=" + this.mTempRect1 + ", group=" + group);
            }
            view2 = group;
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
                printMsg("    " + tag + " : {" + window + "} => " + dstRect);
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
        ColorLog.d(DBG, TAG, "==========calcScrollRect====: ");
        if (scrollNode == null) {
            ColorLog.d(DBG, TAG, "  calcScrollRect, scrollNode=null");
            return;
        }
        long timeStart = SystemClock.uptimeMillis();
        printTag("calcScrollRect");
        View view = scrollNode.getView();
        Rect srcRect = new Rect();
        view.getBoundsOnScreen(srcRect, true);
        Rect dstRect = new Rect(srcRect);
        calcScrollRectForViews(utils, dstRect, srcRect, view);
        scrollNode.setScrollRect(dstRect);
        printMsg("calcScrollRect : scrollRect=" + dstRect);
        long timeSpend = getTimeSpend(timeStart);
        printSpend("calcScrollRect", timeSpend);
        if (result != null) {
            result.setScrollRect(dstRect);
            result.setSpendCalc(timeSpend);
        }
    }

    private void calcScrollRects(ColorLongshotDump result, ColorLongshotViewUtils utils, List<ViewNode> scrollNodes, List<ColorWindowNode> systemWindows, List<ColorWindowNode> floatWindows) {
        for (ViewNode scrollNode : scrollNodes) {
            if (result != null) {
                result.setScrollComponent(ColorLongshotComponentName.create((OppoBaseView) ColorTypeCastingHelper.typeCasting(OppoBaseView.class, scrollNode.getView()), getAccessibilityName(scrollNode)));
            }
            calcScrollRect(result, utils, scrollNode, systemWindows, floatWindows);
            calcScrollRects(result, utils, scrollNode.getChildList(), systemWindows, floatWindows);
        }
    }

    private void reportDumpResult(Context context, ColorLongshotDump result) {
        ColorScreenshotManager sm;
        if (result != null && (sm = ColorLongshotUtils.getScreenshotManager(context)) != null) {
            sm.reportLongshotDumpResult(result);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:59:0x0148, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:60:0x014a, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:61:0x014c, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:82:0x019e, code lost:
        r9.clear();
     */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Removed duplicated region for block: B:29:0x00a5 A[Catch:{ JSONException -> 0x0096, Exception -> 0x008f, all -> 0x008a }] */
    /* JADX WARNING: Removed duplicated region for block: B:59:0x0148 A[ExcHandler: all (th java.lang.Throwable), PHI: r7 r9 
      PHI: (r7v8 'view' android.view.View) = (r7v0 'view' android.view.View), (r7v0 'view' android.view.View), (r7v9 'view' android.view.View) binds: [B:9:0x0066, B:10:?, B:39:0x00ce] A[DONT_GENERATE, DONT_INLINE]
      PHI: (r9v9 'preorderedList' java.util.ArrayList<android.view.View>) = (r9v0 'preorderedList' java.util.ArrayList<android.view.View>), (r9v0 'preorderedList' java.util.ArrayList<android.view.View>), (r9v10 'preorderedList' java.util.ArrayList<android.view.View>) binds: [B:9:0x0066, B:10:?, B:39:0x00ce] A[DONT_GENERATE, DONT_INLINE], Splitter:B:9:0x0066] */
    /* JADX WARNING: Removed duplicated region for block: B:72:0x0175 A[Catch:{ JSONException -> 0x0176, Exception -> 0x0154, all -> 0x014e, all -> 0x019b }] */
    /* JADX WARNING: Removed duplicated region for block: B:82:0x019e  */
    /* JADX WARNING: Removed duplicated region for block: B:90:0x01a8 A[SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:91:0x01a8 A[SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:92:0x01a8 A[SYNTHETIC] */
    private void scrollNodesToJson(JSONArray jsScrollArray, List<ViewNode> scrollNodes, boolean isChild) {
        Iterator<ViewNode> it;
        Throwable th;
        JSONException e;
        Exception e2;
        boolean customOrder;
        int i;
        View child;
        int childIndex;
        Rect scrollRect;
        View view;
        Iterator<ViewNode> it2 = scrollNodes.iterator();
        while (it2.hasNext()) {
            ViewNode viewNode = it2.next();
            View view2 = viewNode.getView();
            if (view2 instanceof ViewGroup) {
                ArrayList<View> preorderedList = null;
                try {
                    ViewGroup group = (ViewGroup) view2;
                    JSONObject jsScrollView = new JSONObject();
                    jsScrollView.put(JSON_PARENT_HASH, System.identityHashCode(group));
                    group.getBoundsOnScreen(this.mTempRect1, false);
                    jsScrollView.put(JSON_PARENT_RECT_FULL, this.mTempRect1.flattenToString());
                    group.getBoundsOnScreen(this.mTempRect1, true);
                    jsScrollView.put(JSON_PARENT_RECT_CLIP, this.mTempRect1.flattenToString());
                    Rect scrollRect2 = viewNode.getScrollRect();
                    jsScrollView.put(JSON_SCROLL_RECT, scrollRect2.flattenToString());
                    try {
                        jsScrollView.put(JSON_SCROLL_CHILD, isChild);
                        JSONArray jsChildArray = new JSONArray();
                        int childrenCount = group.getChildCount();
                        preorderedList = group.buildOrderedChildList();
                        boolean noPreorder = preorderedList == null;
                        if (noPreorder) {
                            try {
                                if (group.isChildrenDrawingOrderEnabled()) {
                                    customOrder = true;
                                    i = 0;
                                    while (i < childrenCount) {
                                        int childIndex2 = customOrder ? group.getChildDrawingOrder(childrenCount, i) : i;
                                        if (noPreorder) {
                                            childIndex = childIndex2;
                                            child = group.getChildAt(childIndex);
                                        } else {
                                            childIndex = childIndex2;
                                            child = preorderedList.get(childIndex);
                                        }
                                        it = it2;
                                        if (child == null) {
                                            view = view2;
                                            scrollRect = scrollRect2;
                                        } else if (!child.isVisibleToUser()) {
                                            view = view2;
                                            scrollRect = scrollRect2;
                                        } else {
                                            JSONObject jsChild = new JSONObject();
                                            view = view2;
                                            scrollRect = scrollRect2;
                                            try {
                                                jsChild.put(JSON_CHILD_HASH, System.identityHashCode(child));
                                                child.getBoundsOnScreen(this.mTempRect1, false);
                                                jsChild.put(JSON_CHILD_RECT_FULL, this.mTempRect1.flattenToString());
                                                child.getBoundsOnScreen(this.mTempRect1, true);
                                                jsChild.put(JSON_CHILD_RECT_CLIP, this.mTempRect1.flattenToString());
                                                if (group instanceof ScrollView) {
                                                    jsChild.put(JSON_CHILD_SCROLLY, child.getScrollY());
                                                }
                                                jsChildArray.put(jsChild);
                                            } catch (JSONException e3) {
                                                e = e3;
                                                ColorLog.e(DBG, TAG, "scrollNodesToJson:" + Log.getStackTraceString(e));
                                                if (preorderedList == null) {
                                                }
                                                preorderedList.clear();
                                                scrollNodesToJson(jsScrollArray, viewNode.getChildList(), true);
                                                it2 = it;
                                            } catch (Exception e4) {
                                                e2 = e4;
                                                ColorLog.e(DBG, TAG, "scrollNodesToJson:" + Log.getStackTraceString(e2));
                                                if (preorderedList != null) {
                                                }
                                            }
                                        }
                                        i++;
                                        it2 = it;
                                        view2 = view;
                                        scrollRect2 = scrollRect;
                                    }
                                    it = it2;
                                    jsScrollView.put(JSON_CHILD_LIST, jsChildArray);
                                    jsScrollArray.put(jsScrollView);
                                    if (preorderedList == null) {
                                    }
                                    preorderedList.clear();
                                }
                            } catch (JSONException e5) {
                                e = e5;
                                it = it2;
                                ColorLog.e(DBG, TAG, "scrollNodesToJson:" + Log.getStackTraceString(e));
                                if (preorderedList == null) {
                                }
                                preorderedList.clear();
                                scrollNodesToJson(jsScrollArray, viewNode.getChildList(), true);
                                it2 = it;
                            } catch (Exception e6) {
                                e2 = e6;
                                it = it2;
                                ColorLog.e(DBG, TAG, "scrollNodesToJson:" + Log.getStackTraceString(e2));
                                if (preorderedList != null) {
                                }
                            } catch (Throwable th2) {
                                th = th2;
                                if (preorderedList != null) {
                                }
                                throw th;
                            }
                        }
                        customOrder = false;
                        i = 0;
                        while (i < childrenCount) {
                        }
                        it = it2;
                        jsScrollView.put(JSON_CHILD_LIST, jsChildArray);
                        jsScrollArray.put(jsScrollView);
                        if (preorderedList == null) {
                        }
                    } catch (JSONException e7) {
                        e = e7;
                        ColorLog.e(DBG, TAG, "scrollNodesToJson:" + Log.getStackTraceString(e));
                        if (preorderedList == null) {
                        }
                        preorderedList.clear();
                        scrollNodesToJson(jsScrollArray, viewNode.getChildList(), true);
                        it2 = it;
                    } catch (Exception e8) {
                        e2 = e8;
                        ColorLog.e(DBG, TAG, "scrollNodesToJson:" + Log.getStackTraceString(e2));
                        if (preorderedList != null) {
                        }
                    } catch (Throwable th3) {
                    }
                } catch (JSONException e9) {
                    e = e9;
                    it = it2;
                    ColorLog.e(DBG, TAG, "scrollNodesToJson:" + Log.getStackTraceString(e));
                    if (preorderedList == null) {
                    }
                    preorderedList.clear();
                    scrollNodesToJson(jsScrollArray, viewNode.getChildList(), true);
                    it2 = it;
                } catch (Exception e10) {
                    e2 = e10;
                    it = it2;
                    ColorLog.e(DBG, TAG, "scrollNodesToJson:" + Log.getStackTraceString(e2));
                    if (preorderedList != null) {
                    }
                } catch (Throwable th4) {
                    th = th4;
                    if (preorderedList != null) {
                    }
                    throw th;
                }
                preorderedList.clear();
            } else {
                it = it2;
            }
            scrollNodesToJson(jsScrollArray, viewNode.getChildList(), true);
            it2 = it;
        }
    }

    private List<ColorWindowNode> mergeWindowList(List<ColorWindowNode> systemWindows, List<ColorWindowNode> floatWindows) {
        List<ColorWindowNode> windows = new ArrayList<>();
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
                    boolean z = DBG;
                    ColorLog.e(z, TAG, "windowNodesToJson:" + Log.getStackTraceString(e));
                } catch (Exception e2) {
                    boolean z2 = DBG;
                    ColorLog.e(z2, TAG, "windowNodesToJson:" + Log.getStackTraceString(e2));
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
                    boolean z = DBG;
                    ColorLog.e(z, TAG, "sideRectsToJson:" + Log.getStackTraceString(e));
                } catch (Exception e2) {
                    boolean z2 = DBG;
                    ColorLog.e(z2, TAG, "sideRectsToJson:" + Log.getStackTraceString(e2));
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
                    boolean z = DBG;
                    ColorLog.e(z, TAG, "floatRectsToJson:" + Log.getStackTraceString(e));
                } catch (Exception e2) {
                    boolean z2 = DBG;
                    ColorLog.e(z2, TAG, "floatRectsToJson:" + Log.getStackTraceString(e2));
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

    private String packJsonNode(ColorLongshotDump result, PrintWriter pw, List<ColorWindowNode> systemWindows, List<ColorWindowNode> floatWindows) {
        long timeStart = SystemClock.uptimeMillis();
        printTag("packJsonNode");
        JSONObject jsonNode = null;
        try {
            if (this.mViewInfo.isUnsupported()) {
                jsonNode = new JSONObject();
                jsonNode.put(JSON_VIEW_UNSUPPORTED, true);
            } else {
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
            }
        } catch (JSONException e) {
            boolean z = DBG;
            ColorLog.e(z, TAG, "packJsonNode:" + Log.getStackTraceString(e));
        } catch (Exception e2) {
            boolean z2 = DBG;
            ColorLog.e(z2, TAG, "packJsonNode:" + Log.getStackTraceString(e2));
        }
        String jsonPack = jsonNode != null ? jsonNode.toString() : null;
        long timeSpend = getTimeSpend(timeStart);
        printSpend("packJsonNode", timeSpend);
        if (result != null) {
            result.setSpendPack(timeSpend);
        }
        return jsonPack;
    }

    private void dumpLongshot(ViewRootImpl viewAncestor, PrintWriter pw, String tag, ColorLongshotDump result, List<ColorWindowNode> systemWindows, List<ColorWindowNode> floatWindows) {
        this.mScrollNode = null;
        this.mDumpCount = 0;
        dumpHierarchyLongshot(result, viewAncestor.mView);
        selectScrollNodes(this.mScrollNodes);
        boolean z = DBG;
        ColorLog.d(z, TAG, tag + " : mScrollNode=" + this.mScrollNode);
        calcScrollRects(result, new ColorLongshotViewUtils(viewAncestor.mContext), this.mScrollNodes, systemWindows, floatWindows);
    }

    private void dumpScreenshot(ViewRootImpl viewAncestor, PrintWriter pw, String tag) {
        dumpHierarchyScreenshot(viewAncestor.mView);
    }

    private void clearList() {
        this.mScrollNodes.clear();
        this.mSmallViews.clear();
        this.mFloatRects.clear();
        this.mSideRects.clear();
    }

    /* access modifiers changed from: private */
    public static final class ViewNode {
        private final CharSequence mAccessibilityName;
        private final List<ViewNode> mChildList = new ArrayList();
        private final CharSequence mClassName;
        private final Rect mClipRect = new Rect();
        private final Rect mFullRect = new Rect();
        private int mOverScrollMode = -1;
        private final Rect mScrollRect = new Rect();
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
                sb.append(":");
            }
            if (this.mClassName != null) {
                sb.append("class=");
                sb.append(this.mClassName.toString());
                sb.append(":");
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
                IColorBaseViewRoot iColorBaseViewRoot = (IColorBaseViewRoot) ColorTypeCastingHelper.typeCasting(IColorBaseViewRoot.class, root);
                if (!(iColorBaseViewRoot == null || iColorBaseViewRoot.getColorViewRootImplHooks() == null)) {
                    iColorBaseViewRoot.getColorViewRootImplHooks().getLongshotViewRoot();
                }
                this.mOverScrollMode = this.mView.getOverScrollMode();
                boolean z = ColorLongshotViewDump.DBG;
                ColorLog.d(z, ColorLongshotViewDump.TAG, "disableOverScroll : " + this.mOverScrollMode);
                this.mView.setOverScrollMode(2);
            }
        }

        public void resetOverScroll() {
            ViewRootImpl root = this.mView.getViewRootImpl();
            if (root != null) {
                IColorBaseViewRoot iColorBaseViewRoot = (IColorBaseViewRoot) ColorTypeCastingHelper.typeCasting(IColorBaseViewRoot.class, root);
                if (!(iColorBaseViewRoot == null || iColorBaseViewRoot.getColorViewRootImplHooks() == null)) {
                    iColorBaseViewRoot.getColorViewRootImplHooks().getLongshotViewRoot();
                }
                if (this.mOverScrollMode >= 0) {
                    boolean z = ColorLongshotViewDump.DBG;
                    ColorLog.d(z, ColorLongshotViewDump.TAG, "resetOverScroll : " + this.mOverScrollMode);
                    this.mView.setOverScrollMode(this.mOverScrollMode);
                    this.mOverScrollMode = -1;
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public static class ContentComparator implements Comparator<ColorLongshotViewContent> {
        private ContentComparator() {
        }

        public int compare(ColorLongshotViewContent content1, ColorLongshotViewContent content2) {
            ColorLongshotViewContent parent1 = content1.getParent();
            ColorLongshotViewContent parent2 = content2.getParent();
            int result = 0;
            if (!(parent1 == null || parent2 == null || (result = rectCompare(parent1.getRect(), parent2.getRect())) != 0)) {
                result = System.identityHashCode(parent2.getView()) - System.identityHashCode(parent1.getView());
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
}
