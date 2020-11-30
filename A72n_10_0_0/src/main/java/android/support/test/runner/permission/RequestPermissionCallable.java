package android.support.test.runner.permission;

import java.util.Objects;
import java.util.concurrent.Callable;

public abstract class RequestPermissionCallable implements Callable<Object> {
    private final String mPermission;
    private final String mTargetPackage;

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RequestPermissionCallable that = (RequestPermissionCallable) o;
        if (!Objects.equals(this.mTargetPackage, that.mTargetPackage) || !Objects.equals(this.mPermission, that.mPermission)) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        return Objects.hash(this.mTargetPackage, this.mPermission);
    }
}
