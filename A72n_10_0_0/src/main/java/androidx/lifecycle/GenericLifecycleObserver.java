package androidx.lifecycle;

import androidx.lifecycle.Lifecycle;

public interface GenericLifecycleObserver extends LifecycleObserver {
    void onStateChanged(LifecycleOwner lifecycleOwner, Lifecycle.Event event);
}
