package androidx.test.internal.runner.lifecycle;

import android.app.Application;
import android.util.Log;
import androidx.test.internal.util.Checks;
import androidx.test.runner.lifecycle.ApplicationLifecycleCallback;
import androidx.test.runner.lifecycle.ApplicationLifecycleMonitor;
import androidx.test.runner.lifecycle.ApplicationStage;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ApplicationLifecycleMonitorImpl implements ApplicationLifecycleMonitor {
    private final List<WeakReference<ApplicationLifecycleCallback>> callbacks = new ArrayList();

    @Override // androidx.test.runner.lifecycle.ApplicationLifecycleMonitor
    public void addLifecycleCallback(ApplicationLifecycleCallback callback) {
        Checks.checkNotNull(callback);
        synchronized (this.callbacks) {
            boolean needsAdd = true;
            Iterator<WeakReference<ApplicationLifecycleCallback>> refIter = this.callbacks.iterator();
            while (refIter.hasNext()) {
                ApplicationLifecycleCallback storedCallback = refIter.next().get();
                if (storedCallback == null) {
                    refIter.remove();
                } else if (storedCallback == callback) {
                    needsAdd = false;
                }
            }
            if (needsAdd) {
                this.callbacks.add(new WeakReference<>(callback));
            }
        }
    }

    public void signalLifecycleChange(Application app, ApplicationStage stage) {
        synchronized (this.callbacks) {
            Iterator<WeakReference<ApplicationLifecycleCallback>> refIter = this.callbacks.iterator();
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
