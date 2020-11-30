package android.support.test.internal.runner.lifecycle;

import android.app.Application;
import android.support.test.runner.lifecycle.ApplicationLifecycleCallback;
import android.support.test.runner.lifecycle.ApplicationLifecycleMonitor;
import android.support.test.runner.lifecycle.ApplicationStage;
import android.util.Log;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ApplicationLifecycleMonitorImpl implements ApplicationLifecycleMonitor {
    private final List<WeakReference<ApplicationLifecycleCallback>> mCallbacks = new ArrayList();

    public void signalLifecycleChange(Application app, ApplicationStage stage) {
        synchronized (this.mCallbacks) {
            Iterator<WeakReference<ApplicationLifecycleCallback>> refIter = this.mCallbacks.iterator();
            while (refIter.hasNext()) {
                ApplicationLifecycleCallback callback = refIter.next().get();
                if (callback == null) {
                    refIter.remove();
                } else {
                    try {
                        String valueOf = String.valueOf(callback);
                        StringBuilder sb = new StringBuilder(18 + String.valueOf(valueOf).length());
                        sb.append("running callback: ");
                        sb.append(valueOf);
                        Log.d("ApplicationLifecycleMonitorImpl", sb.toString());
                        callback.onApplicationLifecycleChanged(app, stage);
                        String valueOf2 = String.valueOf(callback);
                        StringBuilder sb2 = new StringBuilder(20 + String.valueOf(valueOf2).length());
                        sb2.append("callback completes: ");
                        sb2.append(valueOf2);
                        Log.d("ApplicationLifecycleMonitorImpl", sb2.toString());
                    } catch (RuntimeException re) {
                        Log.e("ApplicationLifecycleMonitorImpl", String.format("Callback threw exception! (callback: %s stage: %s)", callback, stage), re);
                    }
                }
            }
        }
    }
}
