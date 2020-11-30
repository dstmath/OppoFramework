package androidx.test.runner;

import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.manipulation.Filter;
import org.junit.runner.manipulation.Filterable;
import org.junit.runner.manipulation.NoTestsRemainException;
import org.junit.runner.notification.RunNotifier;

@Deprecated
public final class AndroidJUnit4 extends Runner implements Filterable {
    private final Runner delegate;

    @Override // org.junit.runner.Describable, org.junit.runner.Runner
    public Description getDescription() {
        return this.delegate.getDescription();
    }

    @Override // org.junit.runner.Runner
    public void run(RunNotifier runNotifier) {
        this.delegate.run(runNotifier);
    }

    @Override // org.junit.runner.manipulation.Filterable
    public void filter(Filter filter) throws NoTestsRemainException {
        ((Filterable) this.delegate).filter(filter);
    }
}
