package androidx.test.runner;

import android.app.Activity;
import android.app.Application;
import android.app.Instrumentation;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.MessageQueue;
import android.util.Log;
import androidx.test.internal.runner.InstrumentationConnection;
import androidx.test.internal.runner.hidden.ExposedInstrumentationApi;
import androidx.test.internal.runner.intent.IntentMonitorImpl;
import androidx.test.internal.runner.intercepting.DefaultInterceptingActivityFactory;
import androidx.test.internal.runner.lifecycle.ActivityLifecycleMonitorImpl;
import androidx.test.internal.runner.lifecycle.ApplicationLifecycleMonitorImpl;
import androidx.test.internal.util.Checks;
import androidx.test.internal.util.ProcSummary;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.runner.intent.IntentMonitorRegistry;
import androidx.test.runner.intent.IntentStubberRegistry;
import androidx.test.runner.intercepting.InterceptingActivityFactory;
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import androidx.test.runner.lifecycle.ApplicationLifecycleMonitorRegistry;
import androidx.test.runner.lifecycle.ApplicationStage;
import androidx.test.runner.lifecycle.Stage;
import com.alibaba.fastjson.asm.Opcodes;
import java.lang.Thread;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class MonitoringInstrumentation extends ExposedInstrumentationApi {
    private static final long MILLIS_TO_POLL_FOR_ACTIVITY_STOP = (MILLIS_TO_WAIT_FOR_ACTIVITY_TO_STOP / 40);
    private static final long MILLIS_TO_WAIT_FOR_ACTIVITY_TO_STOP = TimeUnit.SECONDS.toMillis(2);
    private AtomicBoolean anActivityHasBeenLaunched = new AtomicBoolean(false);
    private ApplicationLifecycleMonitorImpl applicationMonitor = new ApplicationLifecycleMonitorImpl();
    private ExecutorService executorService;
    private volatile boolean finished = false;
    private Handler handlerForMainLooper;
    private MessageQueue.IdleHandler idleHandler = new MessageQueue.IdleHandler() {
        /* class androidx.test.runner.MonitoringInstrumentation.AnonymousClass1 */

        public boolean queueIdle() {
            MonitoringInstrumentation.this.lastIdleTime.set(System.currentTimeMillis());
            return true;
        }
    };
    private IntentMonitorImpl intentMonitor = new IntentMonitorImpl();
    private volatile InterceptingActivityFactory interceptingActivityFactory;
    private ThreadLocal<Boolean> isDexmakerClassLoaderInitialized = new ThreadLocal<>();
    private AtomicBoolean isJsBridgeLoaded = new AtomicBoolean(false);
    private volatile Boolean isOriginalInstr = null;
    private String jsBridgeClassName;
    private AtomicLong lastIdleTime = new AtomicLong(0);
    private ActivityLifecycleMonitorImpl lifecycleMonitor = new ActivityLifecycleMonitorImpl();
    private AtomicInteger startedActivityCounter = new AtomicInteger(0);

    public void onCreate(Bundle arguments) {
        Log.i("MonitoringInstr", "Instrumentation started!");
        logUncaughtExceptions();
        installMultidex();
        InstrumentationRegistry.registerInstance(this, arguments);
        androidx.test.InstrumentationRegistry.registerInstance(this, arguments);
        ActivityLifecycleMonitorRegistry.registerInstance(this.lifecycleMonitor);
        ApplicationLifecycleMonitorRegistry.registerInstance(this.applicationMonitor);
        IntentMonitorRegistry.registerInstance(this.intentMonitor);
        this.handlerForMainLooper = new Handler(Looper.getMainLooper());
        this.executorService = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 0, TimeUnit.SECONDS, new SynchronousQueue(), new ThreadFactory(this) {
            /* class androidx.test.runner.MonitoringInstrumentation.AnonymousClass2 */

            public Thread newThread(Runnable runnable) {
                Thread thread = Executors.defaultThreadFactory().newThread(runnable);
                thread.setName(MonitoringInstrumentation.class.getSimpleName());
                return thread;
            }
        });
        Looper.myQueue().addIdleHandler(this.idleHandler);
        super.onCreate(arguments);
        specifyDexMakerCacheProperty();
        setupDexmakerClassloader();
        useDefaultInterceptingActivityFactory();
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Code restructure failed: missing block: B:10:0x0037, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x003f, code lost:
        throw new java.lang.RuntimeException("multidex is available at runtime, but calling it failed.", r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0040, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0048, code lost:
        throw new java.lang.RuntimeException("multidex is available at runtime, but calling it failed.", r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0053, code lost:
        android.util.Log.i("MonitoringInstr", "No multidex.");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x0033, code lost:
        installOldMultiDex(r0);
     */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Removed duplicated region for block: B:10:0x0037 A[ExcHandler: IllegalAccessException (r0v5 'iae' java.lang.IllegalAccessException A[CUSTOM_DECLARE]), Splitter:B:3:0x0008] */
    /* JADX WARNING: Removed duplicated region for block: B:13:0x0040 A[ExcHandler: InvocationTargetException (r0v4 'ite' java.lang.reflect.InvocationTargetException A[CUSTOM_DECLARE]), Splitter:B:3:0x0008] */
    /* JADX WARNING: Removed duplicated region for block: B:18:0x0052 A[ExcHandler: ClassNotFoundException (e java.lang.ClassNotFoundException), Splitter:B:3:0x0008] */
    public void installMultidex() {
        if (Build.VERSION.SDK_INT < 21) {
            try {
                Class<?> multidex = Class.forName("androidx.multidex.MultiDex");
                multidex.getDeclaredMethod("installInstrumentation", Context.class, Context.class).invoke(null, getContext(), getTargetContext());
            } catch (ClassNotFoundException e) {
            } catch (NoSuchMethodException nsme) {
                Log.i("MonitoringInstr", "No multidex.", nsme);
            } catch (InvocationTargetException ite) {
            } catch (IllegalAccessException iae) {
            }
        }
    }

    /* access modifiers changed from: protected */
    public void installOldMultiDex(Class<?> multidexClass) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        multidexClass.getDeclaredMethod("install", Context.class).invoke(null, getTargetContext());
    }

    /* access modifiers changed from: protected */
    public void specifyDexMakerCacheProperty() {
        System.getProperties().put("dexmaker.dexcache", getTargetContext().getDir("dxmaker_cache", 0).getAbsolutePath());
    }

    /* access modifiers changed from: protected */
    public final void setJsBridgeClassName(String className) {
        if (className == null) {
            throw new NullPointerException("JsBridge class name cannot be null!");
        } else if (!this.isJsBridgeLoaded.get()) {
            this.jsBridgeClassName = className;
        } else {
            throw new IllegalStateException("JsBridge is already loaded!");
        }
    }

    private void setupDexmakerClassloader() {
        if (!Boolean.TRUE.equals(this.isDexmakerClassLoaderInitialized.get())) {
            ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
            ClassLoader newClassLoader = getTargetContext().getClassLoader();
            Log.i("MonitoringInstr", String.format("Setting context classloader to '%s', Original: '%s'", newClassLoader.toString(), originalClassLoader.toString()));
            Thread.currentThread().setContextClassLoader(newClassLoader);
            this.isDexmakerClassLoaderInitialized.set(Boolean.TRUE);
        }
    }

    private void logUncaughtExceptions() {
        final Thread.UncaughtExceptionHandler standardHandler = Thread.currentThread().getUncaughtExceptionHandler();
        Thread.currentThread().setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            /* class androidx.test.runner.MonitoringInstrumentation.AnonymousClass3 */

            public void uncaughtException(Thread t, Throwable e) {
                MonitoringInstrumentation.this.onException(t, e);
                if (standardHandler != null) {
                    Log.w("MonitoringInstr", String.format("Invoking uncaught exception handler %s (a %s)", standardHandler, standardHandler.getClass()));
                    standardHandler.uncaughtException(t, e);
                } else {
                    String valueOf = String.valueOf(t.getName());
                    Log.w("MonitoringInstr", valueOf.length() != 0 ? "Invoking uncaught exception handler for thread: ".concat(valueOf) : new String("Invoking uncaught exception handler for thread: "));
                    t.getThreadGroup().uncaughtException(t, e);
                }
                if (!"robolectric".equals(Build.FINGERPRINT) && Looper.getMainLooper().getThread().equals(t)) {
                    Log.e("MonitoringInstr", "The main thread has died and the handlers didn't care, exiting");
                    System.exit(-10);
                }
            }
        });
    }

    public void onStart() {
        super.onStart();
        if (this.jsBridgeClassName != null) {
            tryLoadingJsBridge(this.jsBridgeClassName);
        }
        waitForIdleSync();
        setupDexmakerClassloader();
        InstrumentationConnection.getInstance().init(this, new ActivityFinisher());
    }

    public void finish(int resultCode, Bundle results) {
        if (this.finished) {
            Log.w("MonitoringInstr", "finish called 2x!");
            return;
        }
        this.finished = true;
        this.handlerForMainLooper.post(new ActivityFinisher());
        long startTime = System.currentTimeMillis();
        waitForActivitiesToComplete();
        Log.i("MonitoringInstr", String.format("waitForActivitiesToComplete() took: %sms", Long.valueOf(System.currentTimeMillis() - startTime)));
        ActivityLifecycleMonitorRegistry.registerInstance(null);
        super.finish(resultCode, results);
    }

    /* access modifiers changed from: protected */
    public void waitForActivitiesToComplete() {
        if (Looper.getMainLooper() != Looper.myLooper()) {
            long endTime = System.currentTimeMillis() + MILLIS_TO_WAIT_FOR_ACTIVITY_TO_STOP;
            int currentActivityCount = this.startedActivityCounter.get();
            while (currentActivityCount > 0 && System.currentTimeMillis() < endTime) {
                try {
                    StringBuilder sb = new StringBuilder(37);
                    sb.append("Unstopped activity count: ");
                    sb.append(currentActivityCount);
                    Log.i("MonitoringInstr", sb.toString());
                    Thread.sleep(MILLIS_TO_POLL_FOR_ACTIVITY_STOP);
                    currentActivityCount = this.startedActivityCounter.get();
                } catch (InterruptedException ie) {
                    Log.i("MonitoringInstr", "Abandoning activity wait due to interruption.", ie);
                }
            }
            if (currentActivityCount > 0) {
                dumpThreadStateToOutputs("ThreadState-unstopped.txt");
                Log.w("MonitoringInstr", String.format("Still %s activities active after waiting %s ms.", Integer.valueOf(currentActivityCount), Long.valueOf(MILLIS_TO_WAIT_FOR_ACTIVITY_TO_STOP)));
                return;
            }
            return;
        }
        throw new IllegalStateException("Cannot be called from main thread!");
    }

    public void onDestroy() {
        Log.i("MonitoringInstr", "Instrumentation Finished!");
        Looper.myQueue().removeIdleHandler(this.idleHandler);
        InstrumentationConnection.getInstance().terminate();
        super.onDestroy();
    }

    public void callApplicationOnCreate(Application app) {
        this.applicationMonitor.signalLifecycleChange(app, ApplicationStage.PRE_ON_CREATE);
        super.callApplicationOnCreate(app);
        this.applicationMonitor.signalLifecycleChange(app, ApplicationStage.CREATED);
    }

    public Activity startActivitySync(final Intent intent) {
        Checks.checkNotMainThread();
        long lastIdleTimeBeforeLaunch = this.lastIdleTime.get();
        if (this.anActivityHasBeenLaunched.compareAndSet(false, true)) {
            intent.addFlags(67108864);
        }
        Future<Activity> startedActivity = this.executorService.submit(new Callable<Activity>() {
            /* class androidx.test.runner.MonitoringInstrumentation.AnonymousClass4 */

            @Override // java.util.concurrent.Callable
            public Activity call() {
                return MonitoringInstrumentation.super.startActivitySync(intent);
            }
        });
        try {
            return startedActivity.get(45, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            dumpThreadStateToOutputs("ThreadState-startActivityTimeout.txt");
            startedActivity.cancel(true);
            throw new RuntimeException(String.format("Could not launch intent %s within %s seconds. Perhaps the main thread has not gone idle within a reasonable amount of time? There could be an animation or something constantly repainting the screen. Or the activity is doing network calls on creation? See the threaddump logs. For your reference the last time the event queue was idle before your activity launch request was %s and now the last time the queue went idle was: %s. If these numbers are the same your activity might be hogging the event queue.", intent, 45, Long.valueOf(lastIdleTimeBeforeLaunch), Long.valueOf(this.lastIdleTime.get())));
        } catch (ExecutionException ee) {
            throw new RuntimeException("Could not launch activity", ee.getCause());
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("interrupted", ie);
        }
    }

    public Instrumentation.ActivityResult execStartActivity(Context who, IBinder contextThread, IBinder token, Activity target, Intent intent, int requestCode, Bundle options) {
        this.intentMonitor.signalIntent(intent);
        Instrumentation.ActivityResult ar = stubResultFor(intent);
        if (ar == null) {
            return super.execStartActivity(who, contextThread, token, target, intent, requestCode, options);
        }
        Log.i("MonitoringInstr", String.format("Stubbing intent %s", intent));
        return ar;
    }

    public Instrumentation.ActivityResult execStartActivity(Context who, IBinder contextThread, IBinder token, String target, Intent intent, int requestCode, Bundle options) {
        this.intentMonitor.signalIntent(intent);
        Instrumentation.ActivityResult ar = stubResultFor(intent);
        if (ar == null) {
            return super.execStartActivity(who, contextThread, token, target, intent, requestCode, options);
        }
        Log.i("MonitoringInstr", String.format("Stubbing intent %s", intent));
        return ar;
    }

    public void execStartActivities(Context who, IBinder contextThread, IBinder token, Activity target, Intent[] intents, Bundle options) {
        Log.d("MonitoringInstr", "execStartActivities(context, ibinder, ibinder, activity, intent[], bundle)");
        for (Intent intent : intents) {
            execStartActivity(who, contextThread, token, target, intent, -1, options);
        }
    }

    /* access modifiers changed from: private */
    public static class StubResultCallable implements Callable<Instrumentation.ActivityResult> {
        private final Intent intent;

        StubResultCallable(Intent intent2) {
            this.intent = intent2;
        }

        @Override // java.util.concurrent.Callable
        public Instrumentation.ActivityResult call() {
            return IntentStubberRegistry.getInstance().getActivityResultForIntent(this.intent);
        }
    }

    private Instrumentation.ActivityResult stubResultFor(Intent intent) {
        if (!IntentStubberRegistry.isLoaded()) {
            return null;
        }
        if (Looper.myLooper() == Looper.getMainLooper()) {
            return IntentStubberRegistry.getInstance().getActivityResultForIntent(intent);
        }
        FutureTask<Instrumentation.ActivityResult> task = new FutureTask<>(new StubResultCallable(intent));
        runOnMainSync(task);
        try {
            return task.get();
        } catch (ExecutionException e) {
            throw new RuntimeException(String.format("Could not retrieve stub result for intent %s", intent), e);
        } catch (InterruptedException e2) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e2);
        }
    }

    public boolean onException(Object obj, Throwable e) {
        Log.e("MonitoringInstr", String.format("Exception encountered by: %s. Dumping thread state to outputs and pining for the fjords.", obj), e);
        dumpThreadStateToOutputs("ThreadState-onException.txt");
        Log.e("MonitoringInstr", "Dying now...");
        return super.onException(obj, e);
    }

    /* access modifiers changed from: protected */
    public void dumpThreadStateToOutputs(String outputFileName) {
        Log.e("THREAD_STATE", getThreadState());
    }

    /* access modifiers changed from: protected */
    public String getThreadState() {
        Set<Map.Entry<Thread, StackTraceElement[]>> threads = Thread.getAllStackTraces().entrySet();
        StringBuilder threadState = new StringBuilder();
        for (Map.Entry<Thread, StackTraceElement[]> threadAndStack : threads) {
            StringBuilder threadMessage = new StringBuilder("  ").append(threadAndStack.getKey());
            threadMessage.append("\n");
            StackTraceElement[] value = threadAndStack.getValue();
            for (StackTraceElement ste : value) {
                threadMessage.append("    ");
                threadMessage.append(ste.toString());
                threadMessage.append("\n");
            }
            threadMessage.append("\n");
            threadState.append(threadMessage.toString());
        }
        return threadState.toString();
    }

    public void callActivityOnDestroy(Activity activity) {
        super.callActivityOnDestroy(activity);
        this.lifecycleMonitor.signalLifecycleChange(Stage.DESTROYED, activity);
    }

    public void callActivityOnRestart(Activity activity) {
        super.callActivityOnRestart(activity);
        this.lifecycleMonitor.signalLifecycleChange(Stage.RESTARTED, activity);
    }

    public void callActivityOnCreate(Activity activity, Bundle bundle) {
        this.lifecycleMonitor.signalLifecycleChange(Stage.PRE_ON_CREATE, activity);
        super.callActivityOnCreate(activity, bundle);
        this.lifecycleMonitor.signalLifecycleChange(Stage.CREATED, activity);
    }

    public void callActivityOnStart(Activity activity) {
        this.startedActivityCounter.incrementAndGet();
        try {
            super.callActivityOnStart(activity);
            this.lifecycleMonitor.signalLifecycleChange(Stage.STARTED, activity);
        } catch (RuntimeException re) {
            this.startedActivityCounter.decrementAndGet();
            throw re;
        }
    }

    public void callActivityOnStop(Activity activity) {
        try {
            super.callActivityOnStop(activity);
            this.lifecycleMonitor.signalLifecycleChange(Stage.STOPPED, activity);
        } finally {
            this.startedActivityCounter.decrementAndGet();
        }
    }

    public void callActivityOnResume(Activity activity) {
        super.callActivityOnResume(activity);
        this.lifecycleMonitor.signalLifecycleChange(Stage.RESUMED, activity);
    }

    public void callActivityOnPause(Activity activity) {
        super.callActivityOnPause(activity);
        this.lifecycleMonitor.signalLifecycleChange(Stage.PAUSED, activity);
    }

    @Override // android.app.Instrumentation
    public Activity newActivity(Class<?> clazz, Context context, IBinder token, Application application, Intent intent, ActivityInfo info, CharSequence title, Activity parent, String id, Object lastNonConfigurationInstance) throws InstantiationException, IllegalAccessException {
        String activityClassPackageName = clazz.getPackage().getName();
        String contextPackageName = context.getPackageName();
        ComponentName intentComponentName = intent.getComponent();
        if (!contextPackageName.equals(intentComponentName.getPackageName()) && activityClassPackageName.equals(intentComponentName.getPackageName())) {
            intent.setComponent(new ComponentName(contextPackageName, intentComponentName.getClassName()));
        }
        return super.newActivity(clazz, context, token, application, intent, info, title, parent, id, lastNonConfigurationInstance);
    }

    @Override // android.app.Instrumentation
    public Activity newActivity(ClassLoader cl, String className, Intent intent) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        if (this.interceptingActivityFactory.shouldIntercept(cl, className, intent)) {
            return this.interceptingActivityFactory.create(cl, className, intent);
        }
        return super.newActivity(cl, className, intent);
    }

    public void useDefaultInterceptingActivityFactory() {
        this.interceptingActivityFactory = new DefaultInterceptingActivityFactory();
    }

    private void tryLoadingJsBridge(final String className) {
        if (className != null) {
            runOnMainSync(new Runnable() {
                /* class androidx.test.runner.MonitoringInstrumentation.AnonymousClass5 */

                public void run() {
                    try {
                        Class.forName(className).getDeclaredMethod("installBridge", new Class[0]).invoke(null, new Object[0]);
                        MonitoringInstrumentation.this.isJsBridgeLoaded.set(true);
                    } catch (ClassNotFoundException | NoSuchMethodException e) {
                        Log.i("MonitoringInstr", "No JSBridge.");
                    } catch (IllegalAccessException | InvocationTargetException ite) {
                        throw new RuntimeException("JSbridge is available at runtime, but calling it failed.", ite);
                    }
                }
            });
            return;
        }
        throw new NullPointerException("JsBridge class name cannot be null!");
    }

    public class ActivityFinisher implements Runnable {
        public ActivityFinisher() {
        }

        public void run() {
            List<Activity> activities = new ArrayList<>();
            Iterator it = EnumSet.range(Stage.CREATED, Stage.STOPPED).iterator();
            while (it.hasNext()) {
                activities.addAll(MonitoringInstrumentation.this.lifecycleMonitor.getActivitiesInStage((Stage) it.next()));
            }
            int size = activities.size();
            StringBuilder sb = new StringBuilder(60);
            sb.append("Activities that are still in CREATED to STOPPED: ");
            sb.append(size);
            Log.i("MonitoringInstr", sb.toString());
            for (Activity activity : activities) {
                if (!activity.isFinishing()) {
                    try {
                        String valueOf = String.valueOf(activity);
                        StringBuilder sb2 = new StringBuilder(20 + String.valueOf(valueOf).length());
                        sb2.append("Finishing activity: ");
                        sb2.append(valueOf);
                        Log.i("MonitoringInstr", sb2.toString());
                        activity.finish();
                    } catch (RuntimeException e) {
                        Log.e("MonitoringInstr", "Failed to finish activity.", e);
                    }
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    @Deprecated
    public boolean isPrimaryInstrProcess(String argsProcessName) {
        return isPrimaryInstrProcess();
    }

    /* access modifiers changed from: protected */
    public final boolean isPrimaryInstrProcess() {
        return isOriginalInstrumentationProcess();
    }

    private boolean isHostingProcess(String wantName, ProcSummary ps) {
        int wantLen = wantName.length();
        int cmdLen = ps.cmdline.length();
        if (wantLen == cmdLen) {
            return wantName.equals(ps.cmdline);
        }
        if (wantLen < cmdLen || !wantName.startsWith(ps.cmdline) || !wantName.endsWith(ps.name)) {
            return false;
        }
        String valueOf = String.valueOf(ps);
        StringBuilder sb = new StringBuilder(Opcodes.IF_ACMPEQ + String.valueOf(valueOf).length() + String.valueOf(wantName).length());
        sb.append("Use smaller processNames in AndroidManifest.xml. Long names are truncated. This process's cmdline is a prefix of the processName and suffix of comm - assuming: ");
        sb.append(valueOf);
        sb.append(" is: ");
        sb.append(wantName);
        Log.w("MonitoringInstr", sb.toString());
        return true;
    }

    private boolean isOriginalInstrumentationProcess() {
        Boolean isOriginal = this.isOriginalInstr;
        if (isOriginal == null) {
            isOriginal = Boolean.valueOf(isOriginalUncached());
            this.isOriginalInstr = isOriginal;
        }
        return isOriginal.booleanValue();
    }

    private List<String> getTargetProcessValues() {
        if (Build.VERSION.SDK_INT < 26) {
            return Collections.emptyList();
        }
        try {
            String tpVal = getContext().getPackageManager().getInstrumentationInfo(getComponentName(), 0).targetProcesses;
            if (tpVal == null) {
                tpVal = "";
            }
            String tpVal2 = tpVal.trim();
            if (tpVal2.length() == 0) {
                return Collections.emptyList();
            }
            List<String> tps = new ArrayList<>();
            for (String tp : tpVal2.split(",", -1)) {
                String tp2 = tp.trim();
                if (tp2.length() > 0) {
                    tps.add(tp2);
                }
            }
            return tps;
        } catch (PackageManager.NameNotFoundException unpossible) {
            String valueOf = String.valueOf(getComponentName());
            StringBuilder sb = new StringBuilder(String.valueOf(valueOf).length() + 25);
            sb.append("Cannot locate ourselves: ");
            sb.append(valueOf);
            Log.wtf("MonitoringInstr", sb.toString(), unpossible);
            String valueOf2 = String.valueOf(getComponentName());
            StringBuilder sb2 = new StringBuilder(25 + String.valueOf(valueOf2).length());
            sb2.append("Cannot locate ourselves: ");
            sb2.append(valueOf2);
            throw new IllegalStateException(sb2.toString(), unpossible);
        }
    }

    private boolean isOriginalUncached() {
        if (Build.VERSION.SDK_INT < 26) {
            return true;
        }
        List<String> targetProcesses = getTargetProcessValues();
        if (targetProcesses.isEmpty()) {
            return true;
        }
        boolean isWildcard = "*".equals(targetProcesses.get(0));
        if (targetProcesses.size() == 1 && !isWildcard) {
            return true;
        }
        try {
            ProcSummary me = ProcSummary.summarize("self");
            if (!isWildcard) {
                return isHostingProcess(targetProcesses.get(0), me);
            }
            String appDefProcessName = getTargetContext().getApplicationInfo().processName;
            if (appDefProcessName == null) {
                appDefProcessName = getTargetContext().getPackageName();
            }
            return isHostingProcess(appDefProcessName, me);
        } catch (ProcSummary.SummaryException se) {
            Log.w("MonitoringInstr", "Could not list apps for this user, running in sandbox? Assuming primary", se);
            return false;
        }
    }
}
