package android.view;

import java.util.ArrayList;

public class ColorViewGroupUtils {
    public static ArrayList<View> buildOrderedChildList(ViewGroup group) {
        return group.buildOrderedChildList();
    }

    public static boolean isChildrenDrawingOrderEnabled(ViewGroup group) {
        return group.isChildrenDrawingOrderEnabled();
    }

    public static int getChildDrawingOrder(ViewGroup group, int childCount, int i) {
        return group.getChildDrawingOrder(childCount, i);
    }
}
