package androidx.test.runner.lifecycle;

import android.app.Activity;

public interface ActivityLifecycleCallback {
    void onActivityLifecycleChanged(Activity activity, Stage stage);
}
