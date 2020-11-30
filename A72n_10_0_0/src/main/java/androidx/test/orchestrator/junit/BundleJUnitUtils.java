package androidx.test.orchestrator.junit;

import android.os.Bundle;
import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

public final class BundleJUnitUtils {
    public static Bundle getBundleFromDescription(Description description) {
        Bundle bundle = new Bundle();
        bundle.putParcelable("description", new ParcelableDescription(description));
        return bundle;
    }

    public static Bundle getBundleFromFailure(Failure failure) {
        Bundle bundle = new Bundle();
        bundle.putParcelable("failure", new ParcelableFailure(failure));
        return bundle;
    }

    public static Bundle getBundleFromResult(Result result) {
        Bundle bundle = new Bundle();
        bundle.putParcelable("result", new ParcelableResult(result));
        return bundle;
    }
}
