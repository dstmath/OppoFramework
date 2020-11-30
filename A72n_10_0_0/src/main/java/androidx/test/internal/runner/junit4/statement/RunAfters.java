package androidx.test.internal.runner.junit4.statement;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.MultipleFailureException;
import org.junit.runners.model.Statement;

public class RunAfters extends UiThreadStatement {
    private final List<FrameworkMethod> afters;
    private final Statement next;
    private final Object target;

    public RunAfters(FrameworkMethod method, Statement next2, List<FrameworkMethod> afters2, Object target2) {
        super(next2, shouldRunOnUiThread(method));
        this.next = next2;
        this.afters = afters2;
        this.target = target2;
    }

    @Override // org.junit.runners.model.Statement, androidx.test.internal.runner.junit4.statement.UiThreadStatement
    public void evaluate() throws Throwable {
        final List<Throwable> errors = new CopyOnWriteArrayList<>();
        try {
            this.next.evaluate();
            for (final FrameworkMethod each : this.afters) {
                if (shouldRunOnUiThread(each)) {
                    runOnUiThread(new Runnable() {
                        /* class androidx.test.internal.runner.junit4.statement.RunAfters.AnonymousClass1 */

                        public void run() {
                            try {
                                each.invokeExplosively(RunAfters.this.target, new Object[0]);
                            } catch (Throwable throwable) {
                                errors.add(throwable);
                            }
                        }
                    });
                } else {
                    try {
                        each.invokeExplosively(this.target, new Object[0]);
                    } catch (Throwable e) {
                        errors.add(e);
                    }
                }
            }
        } catch (Throwable e2) {
            errors.add(e2);
        }
        MultipleFailureException.assertEmpty(errors);
    }
}
