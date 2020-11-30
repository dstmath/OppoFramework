package androidx.test.runner;

import android.app.Instrumentation;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Debug;
import android.util.Log;
import androidx.test.internal.runner.RunnerArgs;
import androidx.test.internal.runner.TestExecutor;
import androidx.test.internal.runner.TestRequestBuilder;
import androidx.test.internal.runner.listener.ActivityFinisherRunListener;
import androidx.test.internal.runner.listener.CoverageListener;
import androidx.test.internal.runner.listener.DelayInjector;
import androidx.test.internal.runner.listener.InstrumentationResultPrinter;
import androidx.test.internal.runner.listener.LogRunListener;
import androidx.test.internal.runner.listener.SuiteAssignmentPrinter;
import androidx.test.internal.runner.tracker.AnalyticsBasedUsageTracker;
import androidx.test.internal.util.ReflectionUtil;
import androidx.test.orchestrator.instrumentationlistener.OrchestratedInstrumentationListener;
import androidx.test.runner.MonitoringInstrumentation;
import androidx.test.runner.lifecycle.ApplicationLifecycleCallback;
import androidx.test.runner.lifecycle.ApplicationLifecycleMonitorRegistry;
import androidx.test.runner.screenshot.Screenshot;
import java.util.HashSet;
import org.junit.runner.Request;
import org.junit.runner.notification.RunListener;

public class AndroidJUnitRunner extends MonitoringInstrumentation implements OrchestratedInstrumentationListener.OnConnectListener {
    private Bundle arguments;
    private InstrumentationResultPrinter instrumentationResultPrinter = new InstrumentationResultPrinter();
    private OrchestratedInstrumentationListener orchestratorListener;
    private RunnerArgs runnerArgs;
    private UsageTrackerFacilitator usageTrackerFacilitator;

    @Override // androidx.test.runner.MonitoringInstrumentation
    public void onCreate(Bundle arguments2) {
        this.arguments = arguments2;
        parseRunnerArgs(this.arguments);
        if (waitForDebugger(this.runnerArgs)) {
            Log.i("AndroidJUnitRunner", "Waiting for debugger to connect...");
            Debug.waitForDebugger();
            Log.i("AndroidJUnitRunner", "Debugger connected.");
        }
        if (isPrimaryInstrProcess(this.runnerArgs.targetProcess)) {
            this.usageTrackerFacilitator = new UsageTrackerFacilitator(this.runnerArgs);
        } else {
            this.usageTrackerFacilitator = new UsageTrackerFacilitator(false);
        }
        super.onCreate(arguments2);
        for (ApplicationLifecycleCallback listener : this.runnerArgs.appListeners) {
            ApplicationLifecycleMonitorRegistry.getInstance().addLifecycleCallback(listener);
        }
        addScreenCaptureProcessors(this.runnerArgs);
        if (this.runnerArgs.orchestratorService == null || !isPrimaryInstrProcess(this.runnerArgs.targetProcess)) {
            start();
            return;
        }
        this.orchestratorListener = new OrchestratedInstrumentationListener(this);
        this.orchestratorListener.connect(getContext());
    }

    private boolean waitForDebugger(RunnerArgs arguments2) {
        return arguments2.debug && !arguments2.listTestsForOrchestrator;
    }

    @Override // androidx.test.orchestrator.instrumentationlistener.OrchestratedInstrumentationListener.OnConnectListener
    public void onOrchestratorConnect() {
        start();
    }

    private void parseRunnerArgs(Bundle arguments2) {
        this.runnerArgs = new RunnerArgs.Builder().fromManifest(this).fromBundle(this, arguments2).build();
    }

    private Bundle getArguments() {
        return this.arguments;
    }

    /* access modifiers changed from: package-private */
    public InstrumentationResultPrinter getInstrumentationResultPrinter() {
        return this.instrumentationResultPrinter;
    }

    @Override // androidx.test.runner.MonitoringInstrumentation
    public void onStart() {
        setJsBridgeClassName("androidx.test.espresso.web.bridge.JavaScriptBridge");
        super.onStart();
        if (!this.runnerArgs.listTestsForOrchestrator || !isPrimaryInstrProcess(this.runnerArgs.targetProcess)) {
            if (this.runnerArgs.remoteMethod != null) {
                ReflectionUtil.reflectivelyInvokeRemoteMethod(this.runnerArgs.remoteMethod.testClassName, this.runnerArgs.remoteMethod.methodName);
            }
            if (!isPrimaryInstrProcess(this.runnerArgs.targetProcess)) {
                Log.i("AndroidJUnitRunner", "Runner is idle...");
                return;
            }
            Bundle results = new Bundle();
            try {
                TestExecutor.Builder executorBuilder = new TestExecutor.Builder(this);
                addListeners(this.runnerArgs, executorBuilder);
                results = executorBuilder.build().execute(buildRequest(this.runnerArgs, getArguments()));
            } catch (RuntimeException e) {
                Log.e("AndroidJUnitRunner", "Fatal exception when running tests", e);
                String valueOf = String.valueOf("Fatal exception when running tests\n");
                String valueOf2 = String.valueOf(Log.getStackTraceString(e));
                results.putString("stream", valueOf2.length() != 0 ? valueOf.concat(valueOf2) : new String(valueOf));
            }
            finish(-1, results);
            return;
        }
        this.orchestratorListener.addTests(buildRequest(this.runnerArgs, getArguments()).getRunner().getDescription());
        finish(-1, new Bundle());
    }

    @Override // androidx.test.runner.MonitoringInstrumentation
    public void finish(int resultCode, Bundle results) {
        try {
            this.usageTrackerFacilitator.trackUsage("AndroidJUnitRunner", "1.1.1");
            this.usageTrackerFacilitator.sendUsages();
        } catch (RuntimeException re) {
            Log.w("AndroidJUnitRunner", "Failed to send analytics.", re);
        }
        super.finish(resultCode, results);
    }

    /* access modifiers changed from: package-private */
    public final void addListeners(RunnerArgs args, TestExecutor.Builder builder) {
        if (args.newRunListenerMode) {
            addListenersNewOrder(args, builder);
        } else {
            addListenersLegacyOrder(args, builder);
        }
    }

    private void addListenersLegacyOrder(RunnerArgs args, TestExecutor.Builder builder) {
        if (args.logOnly) {
            builder.addRunListener(getInstrumentationResultPrinter());
        } else if (args.suiteAssignment) {
            builder.addRunListener(new SuiteAssignmentPrinter());
        } else {
            builder.addRunListener(new LogRunListener());
            if (this.orchestratorListener != null) {
                builder.addRunListener(this.orchestratorListener);
            } else {
                builder.addRunListener(getInstrumentationResultPrinter());
            }
            builder.addRunListener(new ActivityFinisherRunListener(this, new MonitoringInstrumentation.ActivityFinisher(), new Runnable() {
                /* class androidx.test.runner.AndroidJUnitRunner.AnonymousClass1 */

                public void run() {
                    AndroidJUnitRunner.this.waitForActivitiesToComplete();
                }
            }));
            addDelayListener(args, builder);
            addCoverageListener(args, builder);
        }
        addListenersFromArg(args, builder);
    }

    private void addListenersNewOrder(RunnerArgs args, TestExecutor.Builder builder) {
        addListenersFromArg(args, builder);
        if (args.logOnly) {
            builder.addRunListener(getInstrumentationResultPrinter());
        } else if (args.suiteAssignment) {
            builder.addRunListener(new SuiteAssignmentPrinter());
        } else {
            builder.addRunListener(new LogRunListener());
            addDelayListener(args, builder);
            addCoverageListener(args, builder);
            if (this.orchestratorListener != null) {
                builder.addRunListener(this.orchestratorListener);
            } else {
                builder.addRunListener(getInstrumentationResultPrinter());
            }
            builder.addRunListener(new ActivityFinisherRunListener(this, new MonitoringInstrumentation.ActivityFinisher(), new Runnable() {
                /* class androidx.test.runner.AndroidJUnitRunner.AnonymousClass2 */

                public void run() {
                    AndroidJUnitRunner.this.waitForActivitiesToComplete();
                }
            }));
        }
    }

    private void addScreenCaptureProcessors(RunnerArgs args) {
        Screenshot.addScreenCaptureProcessors(new HashSet(args.screenCaptureProcessors));
    }

    private void addCoverageListener(RunnerArgs args, TestExecutor.Builder builder) {
        if (args.codeCoverage) {
            builder.addRunListener(new CoverageListener(args.codeCoveragePath));
        }
    }

    private void addDelayListener(RunnerArgs args, TestExecutor.Builder builder) {
        if (args.delayInMillis > 0) {
            builder.addRunListener(new DelayInjector(args.delayInMillis));
        } else if (args.logOnly && Build.VERSION.SDK_INT < 16) {
            builder.addRunListener(new DelayInjector(15));
        }
    }

    private void addListenersFromArg(RunnerArgs args, TestExecutor.Builder builder) {
        for (RunListener listener : args.listeners) {
            builder.addRunListener(listener);
        }
    }

    @Override // androidx.test.runner.MonitoringInstrumentation
    public boolean onException(Object obj, Throwable e) {
        InstrumentationResultPrinter instResultPrinter = getInstrumentationResultPrinter();
        if (instResultPrinter != null) {
            instResultPrinter.reportProcessCrash(e);
        }
        return super.onException(obj, e);
    }

    /* access modifiers changed from: package-private */
    public Request buildRequest(RunnerArgs runnerArgs2, Bundle bundleArgs) {
        TestRequestBuilder builder = createTestRequestBuilder(this, bundleArgs);
        builder.addPathsToScan(runnerArgs2.classpathToScan);
        if (runnerArgs2.classpathToScan.isEmpty()) {
            builder.addPathToScan(getContext().getPackageCodePath());
        }
        builder.addFromRunnerArgs(runnerArgs2);
        registerUserTracker();
        return builder.build();
    }

    private void registerUserTracker() {
        Context targetContext = getTargetContext();
        if (targetContext != null) {
            this.usageTrackerFacilitator.registerUsageTracker(new AnalyticsBasedUsageTracker.Builder(targetContext).buildIfPossible());
        }
    }

    /* access modifiers changed from: package-private */
    public TestRequestBuilder createTestRequestBuilder(Instrumentation instr, Bundle arguments2) {
        return new TestRequestBuilder(instr, arguments2);
    }
}
