package androidx.test.internal.runner.junit4;

import androidx.test.internal.runner.junit4.statement.RunAfters;
import androidx.test.internal.runner.junit4.statement.RunBefores;
import androidx.test.internal.runner.junit4.statement.UiThreadStatement;
import androidx.test.internal.util.AndroidRunnerParams;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.internal.runners.statements.FailOnTimeout;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

public class AndroidJUnit4ClassRunner extends BlockJUnit4ClassRunner {
    private final AndroidRunnerParams androidRunnerParams;

    public AndroidJUnit4ClassRunner(Class<?> klass, AndroidRunnerParams runnerParams) throws InitializationError {
        super(klass);
        this.androidRunnerParams = runnerParams;
    }

    /* access modifiers changed from: protected */
    @Override // org.junit.runners.BlockJUnit4ClassRunner
    public Statement methodInvoker(FrameworkMethod method, Object test) {
        if (UiThreadStatement.shouldRunOnUiThread(method)) {
            return new UiThreadStatement(super.methodInvoker(method, test), true);
        }
        return super.methodInvoker(method, test);
    }

    /* access modifiers changed from: protected */
    @Override // org.junit.runners.BlockJUnit4ClassRunner
    public Statement withBefores(FrameworkMethod method, Object target, Statement statement) {
        List<FrameworkMethod> befores = getTestClass().getAnnotatedMethods(Before.class);
        return befores.isEmpty() ? statement : new RunBefores(method, statement, befores, target);
    }

    /* access modifiers changed from: protected */
    @Override // org.junit.runners.BlockJUnit4ClassRunner
    public Statement withAfters(FrameworkMethod method, Object target, Statement statement) {
        List<FrameworkMethod> afters = getTestClass().getAnnotatedMethods(After.class);
        return afters.isEmpty() ? statement : new RunAfters(method, statement, afters, target);
    }

    /* access modifiers changed from: protected */
    @Override // org.junit.runners.BlockJUnit4ClassRunner
    public Statement withPotentialTimeout(FrameworkMethod method, Object test, Statement next) {
        long timeout = getTimeout((Test) method.getAnnotation(Test.class));
        if (timeout <= 0 && this.androidRunnerParams.getPerTestTimeout() > 0) {
            timeout = this.androidRunnerParams.getPerTestTimeout();
        }
        if (timeout <= 0) {
            return next;
        }
        return new FailOnTimeout(next, timeout);
    }

    private long getTimeout(Test annotation) {
        if (annotation == null) {
            return 0;
        }
        return annotation.timeout();
    }
}
