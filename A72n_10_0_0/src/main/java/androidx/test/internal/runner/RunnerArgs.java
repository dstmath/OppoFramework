package androidx.test.internal.runner;

import android.app.Instrumentation;
import android.app.UiAutomation;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.text.TextUtils;
import android.util.Log;
import androidx.test.runner.lifecycle.ApplicationLifecycleCallback;
import androidx.test.runner.screenshot.ScreenCaptureProcessor;
import com.alibaba.fastjson.asm.Opcodes;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.runner.manipulation.Filter;
import org.junit.runner.notification.RunListener;
import org.junit.runners.model.RunnerBuilder;

public class RunnerArgs {
    public final String annotation;
    public final List<ApplicationLifecycleCallback> appListeners;
    public final ClassLoader classLoader;
    public final Set<String> classpathToScan;
    public final boolean codeCoverage;
    public final String codeCoveragePath;
    public final boolean debug;
    public final int delayInMillis;
    public final boolean disableAnalytics;
    public final List<Filter> filters;
    public final boolean listTestsForOrchestrator;
    public final List<RunListener> listeners;
    public final boolean logOnly;
    public final boolean newRunListenerMode;
    public final List<String> notAnnotations;
    public final List<String> notTestPackages;
    public final List<TestArg> notTests;
    public final int numShards;
    public final String orchestratorService;
    public final TestArg remoteMethod;
    public final List<Class<? extends RunnerBuilder>> runnerBuilderClasses;
    public final List<ScreenCaptureProcessor> screenCaptureProcessors;
    public final int shardIndex;
    public final String shellExecBinderKey;
    public final boolean suiteAssignment;
    public final String targetProcess;
    public final List<String> testPackages;
    public final String testSize;
    public final long testTimeout;
    public final List<TestArg> tests;

    public static class TestArg {
        public final String methodName;
        public final String testClassName;

        TestArg(String className, String methodName2) {
            this.testClassName = className;
            this.methodName = methodName2;
        }

        TestArg(String className) {
            this(className, null);
        }

        public String toString() {
            if (this.methodName == null) {
                return this.testClassName;
            }
            String str = this.testClassName;
            String str2 = this.methodName;
            StringBuilder sb = new StringBuilder(1 + String.valueOf(str).length() + String.valueOf(str2).length());
            sb.append(str);
            sb.append('#');
            sb.append(str2);
            return sb.toString();
        }
    }

    /* access modifiers changed from: private */
    public static final class TestFileArgs {
        private final List<String> packages;
        private final List<TestArg> tests;

        private TestFileArgs() {
            this.tests = new ArrayList();
            this.packages = new ArrayList();
        }
    }

    private RunnerArgs(Builder builder) {
        this.debug = builder.debug;
        this.suiteAssignment = builder.suiteAssignment;
        this.codeCoverage = builder.codeCoverage;
        this.codeCoveragePath = builder.codeCoveragePath;
        this.delayInMillis = builder.delayInMillis;
        this.logOnly = builder.logOnly;
        this.testPackages = builder.testPackages;
        this.notTestPackages = builder.notTestPackages;
        this.testSize = builder.testSize;
        this.annotation = builder.annotation;
        this.notAnnotations = Collections.unmodifiableList(builder.notAnnotations);
        this.testTimeout = builder.testTimeout;
        this.listeners = Collections.unmodifiableList(builder.listeners);
        this.filters = Collections.unmodifiableList(builder.filters);
        this.runnerBuilderClasses = Collections.unmodifiableList(builder.runnerBuilderClasses);
        this.tests = Collections.unmodifiableList(builder.tests);
        this.notTests = Collections.unmodifiableList(builder.notTests);
        this.numShards = builder.numShards;
        this.shardIndex = builder.shardIndex;
        this.disableAnalytics = builder.disableAnalytics;
        this.appListeners = Collections.unmodifiableList(builder.appListeners);
        this.classLoader = builder.classLoader;
        this.classpathToScan = builder.classpathToScan;
        this.remoteMethod = builder.remoteMethod;
        this.orchestratorService = builder.orchestratorService;
        this.listTestsForOrchestrator = builder.listTestsForOrchestrator;
        this.screenCaptureProcessors = Collections.unmodifiableList(builder.screenCaptureProcessors);
        this.targetProcess = builder.targetProcess;
        this.shellExecBinderKey = builder.shellExecBinderKey;
        this.newRunListenerMode = builder.newRunListenerMode;
    }

    public static class Builder {
        private String annotation = null;
        private List<ApplicationLifecycleCallback> appListeners = new ArrayList();
        private ClassLoader classLoader = null;
        private Set<String> classpathToScan = new HashSet();
        private boolean codeCoverage = false;
        private String codeCoveragePath = null;
        private boolean debug = false;
        private int delayInMillis = -1;
        private boolean disableAnalytics = false;
        private List<Filter> filters = new ArrayList();
        private boolean listTestsForOrchestrator = false;
        private List<RunListener> listeners = new ArrayList();
        private boolean logOnly = false;
        private boolean newRunListenerMode = false;
        private List<String> notAnnotations = new ArrayList();
        private List<String> notTestPackages = new ArrayList();
        private List<TestArg> notTests = new ArrayList();
        private int numShards = 0;
        private String orchestratorService = null;
        private TestArg remoteMethod = null;
        private List<Class<? extends RunnerBuilder>> runnerBuilderClasses = new ArrayList();
        private List<ScreenCaptureProcessor> screenCaptureProcessors = new ArrayList();
        private int shardIndex = 0;
        public String shellExecBinderKey;
        private boolean suiteAssignment = false;
        private String targetProcess = null;
        private List<String> testPackages = new ArrayList();
        private String testSize = null;
        private long testTimeout = -1;
        private List<TestArg> tests = new ArrayList();

        public Builder fromBundle(Instrumentation instr, Bundle bundle) {
            this.debug = parseBoolean(bundle.getString("debug"));
            this.delayInMillis = parseUnsignedInt(bundle.get("delay_msec"), "delay_msec");
            this.tests.addAll(parseTestClasses(bundle.getString("class")));
            this.notTests.addAll(parseTestClasses(bundle.getString("notClass")));
            this.testPackages.addAll(parseTestPackages(bundle.getString("package")));
            this.notTestPackages.addAll(parseTestPackages(bundle.getString("notPackage")));
            TestFileArgs testFileArgs = parseFromFile(instr, bundle.getString("testFile"));
            this.tests.addAll(testFileArgs.tests);
            this.testPackages.addAll(testFileArgs.packages);
            TestFileArgs notTestFileArgs = parseFromFile(instr, bundle.getString("notTestFile"));
            this.notTests.addAll(notTestFileArgs.tests);
            this.notTestPackages.addAll(notTestFileArgs.packages);
            this.listeners.addAll(parseLoadAndInstantiateClasses(bundle.getString("listener"), RunListener.class, null));
            this.filters.addAll(parseLoadAndInstantiateClasses(bundle.getString("filter"), Filter.class, bundle));
            this.runnerBuilderClasses.addAll(parseAndLoadClasses(bundle.getString("runnerBuilder"), RunnerBuilder.class));
            this.testSize = bundle.getString("size");
            this.annotation = bundle.getString("annotation");
            this.notAnnotations.addAll(parseStrings(bundle.getString("notAnnotation")));
            this.testTimeout = parseUnsignedLong(bundle.getString("timeout_msec"), "timeout_msec");
            this.numShards = parseUnsignedInt(bundle.get("numShards"), "numShards");
            this.shardIndex = parseUnsignedInt(bundle.get("shardIndex"), "shardIndex");
            this.logOnly = parseBoolean(bundle.getString("log"));
            this.disableAnalytics = parseBoolean(bundle.getString("disableAnalytics"));
            this.appListeners.addAll(parseLoadAndInstantiateClasses(bundle.getString("appListener"), ApplicationLifecycleCallback.class, null));
            this.codeCoverage = parseBoolean(bundle.getString("coverage"));
            this.codeCoveragePath = bundle.getString("coverageFile");
            this.suiteAssignment = parseBoolean(bundle.getString("suiteAssignment"));
            this.classLoader = (ClassLoader) parseLoadAndInstantiateClass(bundle.getString("classLoader"), ClassLoader.class);
            this.classpathToScan = parseClasspath(bundle.getString("classpathToScan"));
            if (bundle.containsKey("remoteMethod")) {
                this.remoteMethod = parseTestClass(bundle.getString("remoteMethod"));
            }
            this.orchestratorService = bundle.getString("orchestratorService");
            this.listTestsForOrchestrator = parseBoolean(bundle.getString("listTestsForOrchestrator"));
            this.targetProcess = bundle.getString("targetProcess");
            this.screenCaptureProcessors.addAll(parseLoadAndInstantiateClasses(bundle.getString("screenCaptureProcessors"), ScreenCaptureProcessor.class, null));
            this.shellExecBinderKey = bundle.getString("shellExecBinderKey");
            this.newRunListenerMode = parseBoolean(bundle.getString("newRunListenerMode"));
            return this;
        }

        public Builder fromManifest(Instrumentation instr) {
            try {
                Bundle b = instr.getContext().getPackageManager().getInstrumentationInfo(instr.getComponentName(), Opcodes.IOR).metaData;
                if (b == null) {
                    return this;
                }
                return fromBundle(instr, b);
            } catch (PackageManager.NameNotFoundException e) {
                Log.wtf("RunnerArgs", String.format("Could not find component %s", instr.getComponentName()));
                return this;
            }
        }

        private static List<String> parseStrings(String value) {
            if (value == null) {
                return Collections.emptyList();
            }
            return Arrays.asList(value.split(","));
        }

        private static boolean parseBoolean(String booleanValue) {
            return booleanValue != null && Boolean.parseBoolean(booleanValue);
        }

        private static int parseUnsignedInt(Object value, String name) {
            if (value == null) {
                return -1;
            }
            int intValue = Integer.parseInt(value.toString());
            if (intValue >= 0) {
                return intValue;
            }
            throw new NumberFormatException(String.valueOf(name).concat(" can not be negative"));
        }

        private static long parseUnsignedLong(Object value, String name) {
            if (value == null) {
                return -1;
            }
            long longValue = Long.parseLong(value.toString());
            if (longValue >= 0) {
                return longValue;
            }
            throw new NumberFormatException(String.valueOf(name).concat(" can not be negative"));
        }

        private static List<String> parseTestPackages(String packagesArg) {
            List<String> packages = new ArrayList<>();
            if (packagesArg != null) {
                for (String packageName : packagesArg.split(",")) {
                    packages.add(packageName);
                }
            }
            return packages;
        }

        private List<TestArg> parseTestClasses(String classesArg) {
            List<TestArg> tests2 = new ArrayList<>();
            if (classesArg != null) {
                for (String className : classesArg.split(",")) {
                    tests2.add(parseTestClass(className));
                }
            }
            return tests2;
        }

        private static Set<String> parseClasspath(String classpath) {
            if (classpath == null || classpath.isEmpty()) {
                return new HashSet();
            }
            return new HashSet(Arrays.asList(classpath.split(":", -1)));
        }

        private static TestArg parseTestClass(String testClassName) {
            if (TextUtils.isEmpty(testClassName)) {
                return null;
            }
            int methodSeparatorIndex = testClassName.indexOf(35);
            if (methodSeparatorIndex <= 0) {
                return new TestArg(testClassName);
            }
            return new TestArg(testClassName.substring(0, methodSeparatorIndex), testClassName.substring(methodSeparatorIndex + 1));
        }

        private TestFileArgs parseFromFile(Instrumentation instr, String filePath) {
            BufferedReader reader = null;
            TestFileArgs args = new TestFileArgs();
            if (filePath == null) {
                return args;
            }
            try {
                BufferedReader reader2 = openFile(instr, filePath);
                while (true) {
                    String line = reader2.readLine();
                    if (line == null) {
                        break;
                    } else if (isClassOrMethod(line)) {
                        args.tests.add(parseTestClass(line));
                    } else {
                        args.packages.addAll(parseTestPackages(line));
                    }
                }
                if (reader2 != null) {
                    try {
                        reader2.close();
                    } catch (IOException e) {
                    }
                }
                return args;
            } catch (FileNotFoundException e2) {
                String valueOf = String.valueOf(filePath);
                throw new IllegalArgumentException(valueOf.length() != 0 ? "testfile not found: ".concat(valueOf) : new String("testfile not found: "), e2);
            } catch (IOException e3) {
                String valueOf2 = String.valueOf(filePath);
                throw new IllegalArgumentException(valueOf2.length() != 0 ? "Could not read test file ".concat(valueOf2) : new String("Could not read test file "), e3);
            } catch (Throwable th) {
                if (0 != 0) {
                    try {
                        reader.close();
                    } catch (IOException e4) {
                    }
                }
                throw th;
            }
        }

        private BufferedReader openFile(Instrumentation instr, String filePath) throws IOException {
            Reader reader;
            if (Build.VERSION.SDK_INT >= 26 && instr.getContext().getPackageManager().isInstantApp()) {
                UiAutomation uiAutomation = instr.getUiAutomation();
                String valueOf = String.valueOf(filePath);
                reader = new InputStreamReader(new ParcelFileDescriptor.AutoCloseInputStream(uiAutomation.executeShellCommand(valueOf.length() != 0 ? "cat ".concat(valueOf) : new String("cat "))));
            } else {
                reader = new FileReader(new File(filePath));
            }
            return new BufferedReader(reader);
        }

        static boolean isClassOrMethod(String line) {
            return line.matches("^([\\p{L}_$][\\p{L}\\p{N}_$]*\\.)*[\\p{Lu}_$][\\p{L}\\p{N}_$]*(#[\\p{L}_$][\\p{L}\\p{N}_$]*)?$");
        }

        private <T> List<T> parseLoadAndInstantiateClasses(String classString, Class<T> type, Bundle bundle) {
            ArrayList arrayList = new ArrayList();
            if (classString != null) {
                for (String className : classString.split(",")) {
                    loadClassByNameInstantiateAndAdd(arrayList, className, type, bundle);
                }
            }
            return arrayList;
        }

        private <T> T parseLoadAndInstantiateClass(String classString, Class<T> type) {
            List<T> classLoaders = parseLoadAndInstantiateClasses(classString, type, null);
            if (classLoaders.isEmpty()) {
                return null;
            }
            if (classLoaders.size() <= 1) {
                return classLoaders.get(0);
            }
            throw new IllegalArgumentException(String.format("Expected 1 class loader, %d given", Integer.valueOf(classLoaders.size())));
        }

        /* JADX DEBUG: Multi-variable search result rejected for r7v0, resolved type: java.util.List<T> */
        /* JADX WARN: Multi-variable type inference failed */
        /* JADX WARNING: Code restructure failed: missing block: B:10:0x001b, code lost:
            if (r10 != null) goto L_0x001d;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:13:0x002b, code lost:
            r2 = new java.lang.Object[]{r10};
            r3 = r0.getConstructor(android.os.Bundle.class);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:16:0x003a, code lost:
            r1 = move-exception;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:17:0x003b, code lost:
            r1.initCause(r3);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:18:0x003e, code lost:
            throw r1;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:19:0x003f, code lost:
            throw r3;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:20:0x0040, code lost:
            r0 = move-exception;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:21:0x0041, code lost:
            r3 = java.lang.String.valueOf(r8);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:22:0x004d, code lost:
            if (r3.length() != 0) goto L_0x004f;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:23:0x004f, code lost:
            r2 = "Failed to create listener: ".concat(r3);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:24:0x0054, code lost:
            r2 = new java.lang.String("Failed to create listener: ");
         */
        /* JADX WARNING: Code restructure failed: missing block: B:26:0x005d, code lost:
            throw new java.lang.IllegalArgumentException(r2, r0);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:27:0x005e, code lost:
            r0 = move-exception;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:28:0x005f, code lost:
            r3 = java.lang.String.valueOf(r8);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:29:0x006b, code lost:
            if (r3.length() != 0) goto L_0x006d;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:30:0x006d, code lost:
            r2 = "Failed to create: ".concat(r3);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:31:0x0072, code lost:
            r2 = new java.lang.String("Failed to create: ");
         */
        /* JADX WARNING: Code restructure failed: missing block: B:33:0x007b, code lost:
            throw new java.lang.IllegalArgumentException(r2, r0);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:34:0x007c, code lost:
            r0 = move-exception;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:35:0x007d, code lost:
            r3 = java.lang.String.valueOf(r8);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:36:0x0089, code lost:
            if (r3.length() != 0) goto L_0x008b;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:37:0x008b, code lost:
            r2 = "Failed to create: ".concat(r3);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:38:0x0090, code lost:
            r2 = new java.lang.String("Failed to create: ");
         */
        /* JADX WARNING: Code restructure failed: missing block: B:40:0x0099, code lost:
            throw new java.lang.IllegalArgumentException(r2, r0);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:42:0x009b, code lost:
            r2 = r9.getName();
            r4 = new java.lang.StringBuilder((17 + java.lang.String.valueOf(r8).length()) + java.lang.String.valueOf(r2).length());
            r4.append(r8);
            r4.append(" does not extend ");
            r4.append(r2);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:43:0x00cc, code lost:
            throw new java.lang.IllegalArgumentException(r4.toString());
         */
        /* JADX WARNING: Code restructure failed: missing block: B:52:0x00ec, code lost:
            r3 = java.lang.String.valueOf(r8);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:53:0x00f8, code lost:
            if (r3.length() != 0) goto L_0x00fa;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:54:0x00fa, code lost:
            r2 = "Could not find extra class ".concat(r3);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:55:0x00ff, code lost:
            r2 = new java.lang.String("Could not find extra class ");
         */
        /* JADX WARNING: Code restructure failed: missing block: B:57:0x0108, code lost:
            throw new java.lang.IllegalArgumentException(r2);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:9:0x001a, code lost:
            r3 = move-exception;
         */
        /* JADX WARNING: Failed to process nested try/catch */
        /* JADX WARNING: Removed duplicated region for block: B:20:0x0040 A[ExcHandler: IllegalAccessException (r0v6 'e' java.lang.IllegalAccessException A[CUSTOM_DECLARE]), Splitter:B:3:0x000a] */
        /* JADX WARNING: Removed duplicated region for block: B:27:0x005e A[ExcHandler: InvocationTargetException (r0v5 'e' java.lang.reflect.InvocationTargetException A[CUSTOM_DECLARE]), Splitter:B:3:0x000a] */
        /* JADX WARNING: Removed duplicated region for block: B:34:0x007c A[ExcHandler: InstantiationException (r0v4 'e' java.lang.InstantiationException A[CUSTOM_DECLARE]), Splitter:B:3:0x000a] */
        /* JADX WARNING: Removed duplicated region for block: B:41:0x009a A[ExcHandler: ClassCastException (e java.lang.ClassCastException), Splitter:B:3:0x000a] */
        /* JADX WARNING: Removed duplicated region for block: B:51:0x00eb A[ExcHandler: ClassNotFoundException (e java.lang.ClassNotFoundException), Splitter:B:3:0x000a] */
        private <T> void loadClassByNameInstantiateAndAdd(List<T> objects, String className, Class<T> type, Bundle bundle) {
            if (className != null && className.length() != 0) {
                try {
                    Class<?> cls = Class.forName(className);
                    Constructor<?> constructor = cls.getConstructor(new Class[0]);
                    Object[] arguments = new Object[0];
                    constructor.setAccessible(true);
                    objects.add(constructor.newInstance(arguments));
                } catch (ClassNotFoundException e) {
                } catch (NoSuchMethodException e2) {
                    String valueOf = String.valueOf(className);
                    throw new IllegalArgumentException(valueOf.length() != 0 ? "Must have no argument constructor for class ".concat(valueOf) : new String("Must have no argument constructor for class "));
                } catch (ClassCastException e3) {
                } catch (InstantiationException e4) {
                } catch (InvocationTargetException e5) {
                } catch (IllegalAccessException e6) {
                }
            }
        }

        private <T> List<Class<? extends T>> parseAndLoadClasses(String classString, Class<T> type) {
            ArrayList arrayList = new ArrayList();
            if (classString != null) {
                for (String className : classString.split(",")) {
                    loadClassByNameAndAdd(arrayList, className, type);
                }
            }
            return arrayList;
        }

        /* JADX DEBUG: Multi-variable search result rejected for r7v0, resolved type: java.util.List<java.lang.Class<? extends T>> */
        /* JADX WARN: Multi-variable type inference failed */
        private <T> void loadClassByNameAndAdd(List<Class<? extends T>> classes, String className, Class<T> type) {
            if (className != null && className.length() != 0) {
                try {
                    Class<?> klass = Class.forName(className);
                    if (type.isAssignableFrom(klass)) {
                        classes.add(klass);
                        return;
                    }
                    String name = type.getName();
                    StringBuilder sb = new StringBuilder(String.valueOf(className).length() + 17 + String.valueOf(name).length());
                    sb.append(className);
                    sb.append(" does not extend ");
                    sb.append(name);
                    throw new IllegalArgumentException(sb.toString());
                } catch (ClassNotFoundException e) {
                    String valueOf = String.valueOf(className);
                    throw new IllegalArgumentException(valueOf.length() != 0 ? "Could not find extra class ".concat(valueOf) : new String("Could not find extra class "));
                } catch (ClassCastException e2) {
                    String name2 = type.getName();
                    StringBuilder sb2 = new StringBuilder(17 + String.valueOf(className).length() + String.valueOf(name2).length());
                    sb2.append(className);
                    sb2.append(" does not extend ");
                    sb2.append(name2);
                    throw new IllegalArgumentException(sb2.toString());
                }
            }
        }

        public RunnerArgs build() {
            return new RunnerArgs(this);
        }
    }
}
