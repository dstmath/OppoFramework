package androidx.test.internal.runner.intent;

import android.content.Intent;
import android.util.Log;
import androidx.test.runner.intent.IntentCallback;
import androidx.test.runner.intent.IntentMonitor;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public final class IntentMonitorImpl implements IntentMonitor {
    List<WeakReference<IntentCallback>> callbacks = Collections.synchronizedList(new ArrayList());

    public void signalIntent(Intent intent) {
        Iterator<WeakReference<IntentCallback>> refIter = this.callbacks.iterator();
        while (refIter.hasNext()) {
            IntentCallback callback = refIter.next().get();
            if (callback == null) {
                refIter.remove();
            } else {
                try {
                    callback.onIntentSent(new Intent(intent));
                } catch (RuntimeException e) {
                    Log.e("IntentMonitorImpl", String.format("Callback threw exception! (callback: %s intent: %s)", callback, intent), e);
                }
            }
        }
    }
}
