package androidx.test.runner.intent;

import android.app.Instrumentation;
import android.content.Intent;

public interface IntentStubber {
    Instrumentation.ActivityResult getActivityResultForIntent(Intent intent);
}
