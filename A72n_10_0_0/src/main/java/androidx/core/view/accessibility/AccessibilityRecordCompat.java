package androidx.core.view.accessibility;

import android.os.Build;
import android.view.accessibility.AccessibilityRecord;

public class AccessibilityRecordCompat {
    private final AccessibilityRecord mRecord;

    public static void setMaxScrollX(AccessibilityRecord record, int maxScrollX) {
        if (Build.VERSION.SDK_INT >= 15) {
            record.setMaxScrollX(maxScrollX);
        }
    }

    public static void setMaxScrollY(AccessibilityRecord record, int maxScrollY) {
        if (Build.VERSION.SDK_INT >= 15) {
            record.setMaxScrollY(maxScrollY);
        }
    }

    @Deprecated
    public int hashCode() {
        if (this.mRecord == null) {
            return 0;
        }
        return this.mRecord.hashCode();
    }

    @Deprecated
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        AccessibilityRecordCompat other = (AccessibilityRecordCompat) obj;
        if (this.mRecord == null) {
            if (other.mRecord != null) {
                return false;
            }
        } else if (!this.mRecord.equals(other.mRecord)) {
            return false;
        }
        return true;
    }
}
