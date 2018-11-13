package android.support.v4.view.accessibility;

import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityNodeInfo.CollectionInfo;
import java.util.List;

class AccessibilityNodeInfoCompatApi21 {

    static class AccessibilityAction {
        AccessibilityAction() {
        }

        static int getId(Object action) {
            return ((android.view.accessibility.AccessibilityNodeInfo.AccessibilityAction) action).getId();
        }

        static CharSequence getLabel(Object action) {
            return ((android.view.accessibility.AccessibilityNodeInfo.AccessibilityAction) action).getLabel();
        }
    }

    static class CollectionItemInfo {
        CollectionItemInfo() {
        }

        public static boolean isSelected(Object info) {
            return ((android.view.accessibility.AccessibilityNodeInfo.CollectionItemInfo) info).isSelected();
        }
    }

    AccessibilityNodeInfoCompatApi21() {
    }

    static List<Object> getActionList(Object info) {
        return ((AccessibilityNodeInfo) info).getActionList();
    }

    static void addAction(Object info, int id, CharSequence label) {
        ((AccessibilityNodeInfo) info).addAction(new android.view.accessibility.AccessibilityNodeInfo.AccessibilityAction(id, label));
    }

    public static Object obtainCollectionInfo(int rowCount, int columnCount, boolean hierarchical, int selectionMode) {
        return CollectionInfo.obtain(rowCount, columnCount, hierarchical, selectionMode);
    }

    public static Object obtainCollectionItemInfo(int rowIndex, int rowSpan, int columnIndex, int columnSpan, boolean heading, boolean selected) {
        return android.view.accessibility.AccessibilityNodeInfo.CollectionItemInfo.obtain(rowIndex, rowSpan, columnIndex, columnSpan, heading, selected);
    }
}
