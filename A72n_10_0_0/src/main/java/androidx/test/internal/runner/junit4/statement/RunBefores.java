package androidx.test.internal.runner.junit4.statement;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

public class RunBefores extends UiThreadStatement {
    private final List<FrameworkMethod> befores;
    private final Statement next;
    private final Object target;

    public RunBefores(FrameworkMethod method, Statement next2, List<FrameworkMethod> befores2, Object target2) {
        super(next2, shouldRunOnUiThread(method));
        this.next = next2;
        this.befores = befores2;
        this.target = target2;
    }

    @Override // org.junit.runners.model.Statement, androidx.test.internal.runner.junit4.statement.UiThreadStatement
    public void evaluate() throws Throwable {
        final AtomicReference<Throwable> exceptionRef = new AtomicReference<>();
        for (final FrameworkMethod before : this.befores) {
            if (shouldRunOnUiThread(before)) {
                runOnUiThread(new Runnable() {
                    /* class androidx.test.internal.runner.junit4.statement.RunBefores.AnonymousClass1 */

                    public void run() {
                        try {
                            before.invokeExplosively(RunBefores.this.target, new Object[0]);
                        } catch (Throwable throwable) {
                            exceptionRef.set(throwable);
                        }
                    }
                });
                Throwable throwable = exceptionRef.get();
                if (throwable != null) {
                    throw throwable;
                }
            } else {
                before.invokeExplosively(this.target, new Object[0]);
            }
        }
        this.next.evaluate();
    }
}
