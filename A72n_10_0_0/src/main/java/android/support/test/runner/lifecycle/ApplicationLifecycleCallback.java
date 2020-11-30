package android.support.test.runner.lifecycle;

import android.app.Application;

public interface ApplicationLifecycleCallback {
    void onApplicationLifecycleChanged(Application application, ApplicationStage applicationStage);
}
