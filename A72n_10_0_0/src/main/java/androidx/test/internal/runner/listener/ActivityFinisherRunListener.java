package androidx.test.internal.runner.listener;

import android.app.Instrumentation;
import androidx.test.internal.runner.InstrumentationConnection;
import androidx.test.internal.util.Checks;
import androidx.test.runner.MonitoringInstrumentation;
import org.junit.runner.Description;
import org.junit.runner.notification.RunListener;

public class ActivityFinisherRunListener extends RunListener {
    private final MonitoringInstrumentation.ActivityFinisher activityFinisher;
    private final Instrumentation instrumentation;
    private final Runnable waitForActivitiesToFinishRunnable;

    public ActivityFinisherRunListener(Instrumentation instrumentation2, MonitoringInstrumentation.ActivityFinisher finisher, Runnable waitForActivitiesToFinishRunnable2) {
        this.instrumentation = (Instrumentation) Checks.checkNotNull(instrumentation2);
        this.activityFinisher = (MonitoringInstrumentation.ActivityFinisher) Checks.checkNotNull(finisher);
        this.waitForActivitiesToFinishRunnable = (Runnable) Checks.checkNotNull(waitForActivitiesToFinishRunnable2);
    }

    @Override // org.junit.runner.notification.RunListener
    public void testStarted(Description description) throws Exception {
        this.instrumentation.runOnMainSync(this.activityFinisher);
        this.waitForActivitiesToFinishRunnable.run();
    }

    @Override // org.junit.runner.notification.RunListener
    public void testFinished(Description description) throws Exception {
        InstrumentationConnection.getInstance().requestRemoteInstancesActivityCleanup();
        this.instrumentation.runOnMainSync(this.activityFinisher);
        this.waitForActivitiesToFinishRunnable.run();
    }
}
