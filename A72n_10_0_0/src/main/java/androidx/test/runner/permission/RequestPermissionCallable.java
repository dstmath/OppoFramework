package androidx.test.runner.permission;

import java.util.Objects;
import java.util.concurrent.Callable;

public abstract class RequestPermissionCallable implements Callable<Object> {
    private final String permission;
    private final String targetPackage;

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RequestPermissionCallable that = (RequestPermissionCallable) o;
        if (!Objects.equals(this.targetPackage, that.targetPackage) || !Objects.equals(this.permission, that.permission)) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        return Objects.hash(this.targetPackage, this.permission);
    }
}
