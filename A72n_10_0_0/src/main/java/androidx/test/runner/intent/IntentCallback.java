package androidx.test.runner.intent;

import android.content.Intent;

public interface IntentCallback {
    void onIntentSent(Intent intent);
}
