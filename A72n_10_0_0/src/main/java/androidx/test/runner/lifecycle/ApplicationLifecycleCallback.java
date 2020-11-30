package androidx.test.runner.lifecycle;

import android.app.Application;

public interface ApplicationLifecycleCallback {
    void onApplicationLifecycleChanged(Application application, ApplicationStage applicationStage);
}
