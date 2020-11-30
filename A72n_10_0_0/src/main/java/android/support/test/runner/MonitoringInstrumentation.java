package android.support.test.runner;

import android.app.Activity;
import android.app.Application;
import android.app.Instrumentation;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.MessageQueue;
import android.support.test.InstrumentationRegistry;
import android.support.test.internal.runner.InstrumentationConnection;
import android.support.test.internal.runner.hidden.ExposedInstrumentationApi;
import android.support.test.internal.runner.intent.IntentMonitorImpl;
import android.support.test.internal.runner.intercepting.DefaultInterceptingActivityFactory;
import android.support.test.internal.runner.lifecycle.ActivityLifecycleMonitorImpl;
import android.support.test.internal.runner.lifecycle.ApplicationLifecycleMonitorImpl;
import android.support.test.internal.util.Checks;
import android.support.test.internal.util.ProcessUtil;
import android.support.test.runner.intent.IntentMonitorRegistry;
import android.support.test.runner.intent.IntentStubberRegistry;
import android.support.test.runner.intercepting.InterceptingActivityFactory;
import android.support.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import android.support.test.runner.lifecycle.ApplicationLifecycleMonitorRegistry;
import android.support.test.runner.lifecycle.ApplicationStage;
import android.support.test.runner.lifecycle.Stage;
import android.util.Log;
import java.lang.Thread;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
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
    private AtomicBoolean mAnActivityHasBeenLaunched = new AtomicBoolean(false);
    private ApplicationLifecycleMonitorImpl mApplicationMonitor = new ApplicationLifecycleMonitorImpl();
    private ExecutorService mExecutorService;
    private volatile boolean mFinished = false;
    private Handler mHandlerForMainLooper;
    private MessageQueue.IdleHandler mIdleHandler = new MessageQueue.IdleHandler() {
        /* class android.support.test.runner.MonitoringInstrumentation.AnonymousClass1 */

        public boolean queueIdle() {
            MonitoringInstrumentation.this.mLastIdleTime.set(System.currentTimeMillis());
            return true;
        }
    };
    private IntentMonitorImpl mIntentMonitor = new IntentMonitorImpl();
    private volatile InterceptingActivityFactory mInterceptingActivityFactory;
    private ThreadLocal<Boolean> mIsDexmakerClassLoaderInitialized = new ThreadLocal<>();
    private AtomicBoolean mIsJsBridgeLoaded = new AtomicBoolean(false);
    private String mJsBridgeClassName;
    private AtomicLong mLastIdleTime = new AtomicLong(0);
    private ActivityLifecycleMonitorImpl mLifecycleMonitor = new ActivityLifecycleMonitorImpl();
    private AtomicInteger mStartedActivityCounter = new AtomicInteger(0);

    public void onCreate(Bundle arguments) {
        String valueOf = String.valueOf(ProcessUtil.getCurrentProcessName(getTargetContext()));
        Log.i("MonitoringInstr", valueOf.length() != 0 ? "Instrumentation started on process ".concat(valueOf) : new String("Instrumentation started on process "));
        logUncaughtExceptions();
        installMultidex();
        InstrumentationRegistry.registerInstance(this, arguments);
        ActivityLifecycleMonitorRegistry.registerInstance(this.mLifecycleMonitor);
        ApplicationLifecycleMonitorRegistry.registerInstance(this.mApplicationMonitor);
        IntentMonitorRegistry.registerInstance(this.mIntentMonitor);
        this.mHandlerForMainLooper = new Handler(Looper.getMainLooper());
        this.mExecutorService = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 0, TimeUnit.SECONDS, new SynchronousQueue(), new ThreadFactory(this) {
            /* class android.support.test.runner.MonitoringInstrumentation.AnonymousClass2 */

            public Thread newThread(Runnable runnable) {
                Thread thread = Executors.defaultThreadFactory().newThread(runnable);
                thread.setName(MonitoringInstrumentation.class.getSimpleName());
                return thread;
            }
        });
        Looper.myQueue().addIdleHandler(this.mIdleHandler);
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
                Class<?> multidex = Class.forName("android.support.multidex.MultiDex");
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

    private void setupDexmakerClassloader() {
        if (!Boolean.TRUE.equals(this.mIsDexmakerClassLoaderInitialized.get())) {
            ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
            ClassLoader newClassLoader = getTargetContext().getClassLoader();
            Log.i("MonitoringInstr", String.format("Setting context classloader to '%s', Original: '%s'", newClassLoader.toString(), originalClassLoader.toString()));
            Thread.currentThread().setContextClassLoader(newClassLoader);
            this.mIsDexmakerClassLoaderInitialized.set(Boolean.TRUE);
        }
    }

    private void logUncaughtExceptions() {
        final Thread.UncaughtExceptionHandler standardHandler = Thread.currentThread().getUncaughtExceptionHandler();
        Thread.currentThread().setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            /* class android.support.test.runner.MonitoringInstrumentation.AnonymousClass3 */

            public void uncaughtException(Thread t, Throwable e) {
                MonitoringInstrumentation.this.onException(t, e);
                if (standardHandler != null) {
                    standardHandler.uncaughtException(t, e);
                }
            }
        });
    }

    public void onStart() {
        super.onStart();
        if (this.mJsBridgeClassName != null) {
            tryLoadingJsBridge(this.mJsBridgeClassName);
        }
        waitForIdleSync();
        setupDexmakerClassloader();
        InstrumentationConnection.getInstance().init(this, new ActivityFinisher());
    }

    public void finish(int resultCode, Bundle results) {
        if (this.mFinished) {
            Log.w("MonitoringInstr", "finish called 2x!");
            return;
        }
        this.mFinished = true;
        this.mHandlerForMainLooper.post(new ActivityFinisher());
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
            int currentActivityCount = this.mStartedActivityCounter.get();
            while (currentActivityCount > 0 && System.currentTimeMillis() < endTime) {
                try {
                    StringBuilder sb = new StringBuilder(37);
                    sb.append("Unstopped activity count: ");
                    sb.append(currentActivityCount);
                    Log.i("MonitoringInstr", sb.toString());
                    Thread.sleep(MILLIS_TO_POLL_FOR_ACTIVITY_STOP);
                    currentActivityCount = this.mStartedActivityCounter.get();
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
        Looper.myQueue().removeIdleHandler(this.mIdleHandler);
        InstrumentationConnection.getInstance().terminate();
        super.onDestroy();
    }

    public void callApplicationOnCreate(Application app) {
        this.mApplicationMonitor.signalLifecycleChange(app, ApplicationStage.PRE_ON_CREATE);
        super.callApplicationOnCreate(app);
        this.mApplicationMonitor.signalLifecycleChange(app, ApplicationStage.CREATED);
    }

    public Activity startActivitySync(final Intent intent) {
        Checks.checkNotMainThread();
        long lastIdleTimeBeforeLaunch = this.mLastIdleTime.get();
        if (this.mAnActivityHasBeenLaunched.compareAndSet(false, true)) {
            intent.addFlags(67108864);
        }
        Future<Activity> startedActivity = this.mExecutorService.submit(new Callable<Activity>() {
            /* class android.support.test.runner.MonitoringInstrumentation.AnonymousClass4 */

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
            throw new RuntimeException(String.format("Could not launch intent %s within %s seconds. Perhaps the main thread has not gone idle within a reasonable amount of time? There could be an animation or something constantly repainting the screen. Or the activity is doing network calls on creation? See the threaddump logs. For your reference the last time the event queue was idle before your activity launch request was %s and now the last time the queue went idle was: %s. If these numbers are the same your activity might be hogging the event queue.", intent, 45, Long.valueOf(lastIdleTimeBeforeLaunch), Long.valueOf(this.mLastIdleTime.get())));
        } catch (ExecutionException ee) {
            throw new RuntimeException("Could not launch activity", ee.getCause());
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("interrupted", ie);
        }
    }

    public Instrumentation.ActivityResult execStartActivity(Context who, IBinder contextThread, IBinder token, Activity target, Intent intent, int requestCode, Bundle options) {
        this.mIntentMonitor.signalIntent(intent);
        Instrumentation.ActivityResult ar = stubResultFor(intent);
        if (ar == null) {
            return super.execStartActivity(who, contextThread, token, target, intent, requestCode, options);
        }
        Log.i("MonitoringInstr", String.format("Stubbing intent %s", intent));
        return ar;
    }

    public Instrumentation.ActivityResult execStartActivity(Context who, IBinder contextThread, IBinder token, String target, Intent intent, int requestCode, Bundle options) {
        this.mIntentMonitor.signalIntent(intent);
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
        private final Intent mIntent;

        StubResultCallable(Intent intent) {
            this.mIntent = intent;
        }

        @Override // java.util.concurrent.Callable
        public Instrumentation.ActivityResult call() {
            return IntentStubberRegistry.getInstance().getActivityResultForIntent(this.mIntent);
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
        this.mLifecycleMonitor.signalLifecycleChange(Stage.DESTROYED, activity);
    }

    public void callActivityOnRestart(Activity activity) {
        super.callActivityOnRestart(activity);
        this.mLifecycleMonitor.signalLifecycleChange(Stage.RESTARTED, activity);
    }

    public void callActivityOnCreate(Activity activity, Bundle bundle) {
        this.mLifecycleMonitor.signalLifecycleChange(Stage.PRE_ON_CREATE, activity);
        super.callActivityOnCreate(activity, bundle);
        this.mLifecycleMonitor.signalLifecycleChange(Stage.CREATED, activity);
    }

    public void callActivityOnStart(Activity activity) {
        this.mStartedActivityCounter.incrementAndGet();
        try {
            super.callActivityOnStart(activity);
            this.mLifecycleMonitor.signalLifecycleChange(Stage.STARTED, activity);
        } catch (RuntimeException re) {
            this.mStartedActivityCounter.decrementAndGet();
            throw re;
        }
    }

    public void callActivityOnStop(Activity activity) {
        try {
            super.callActivityOnStop(activity);
            this.mLifecycleMonitor.signalLifecycleChange(Stage.STOPPED, activity);
        } finally {
            this.mStartedActivityCounter.decrementAndGet();
        }
    }

    public void callActivityOnResume(Activity activity) {
        super.callActivityOnResume(activity);
        this.mLifecycleMonitor.signalLifecycleChange(Stage.RESUMED, activity);
    }

    public void callActivityOnPause(Activity activity) {
        super.callActivityOnPause(activity);
        this.mLifecycleMonitor.signalLifecycleChange(Stage.PAUSED, activity);
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
        if (this.mInterceptingActivityFactory.shouldIntercept(cl, className, intent)) {
            return this.mInterceptingActivityFactory.create(cl, className, intent);
        }
        return super.newActivity(cl, className, intent);
    }

    public void useDefaultInterceptingActivityFactory() {
        this.mInterceptingActivityFactory = new DefaultInterceptingActivityFactory();
    }

    private void tryLoadingJsBridge(final String className) {
        if (className != null) {
            runOnMainSync(new Runnable() {
                /* class android.support.test.runner.MonitoringInstrumentation.AnonymousClass5 */

                public void run() {
                    try {
                        Class.forName(className).getDeclaredMethod("installBridge", new Class[0]).invoke(null, new Object[0]);
                        MonitoringInstrumentation.this.mIsJsBridgeLoaded.set(true);
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
                activities.addAll(MonitoringInstrumentation.this.mLifecycleMonitor.getActivitiesInStage((Stage) it.next()));
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
}
