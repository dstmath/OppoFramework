package androidx.test.internal.runner;

import java.util.List;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.manipulation.Filter;
import org.junit.runner.manipulation.Filterable;
import org.junit.runner.manipulation.NoTestsRemainException;
import org.junit.runner.notification.RunNotifier;

class NonExecutingRunner extends Runner implements Filterable {
    private final Runner runner;

    NonExecutingRunner(Runner runner2) {
        this.runner = runner2;
    }

    @Override // org.junit.runner.Describable, org.junit.runner.Runner
    public Description getDescription() {
        return this.runner.getDescription();
    }

    @Override // org.junit.runner.Runner
    public void run(RunNotifier notifier) {
        generateListOfTests(notifier, getDescription());
    }

    @Override // org.junit.runner.manipulation.Filterable
    public void filter(Filter filter) throws NoTestsRemainException {
        filter.apply(this.runner);
    }

    private void generateListOfTests(RunNotifier runNotifier, Description description) {
        List<Description> children = description.getChildren();
        if (children.isEmpty()) {
            runNotifier.fireTestStarted(description);
            runNotifier.fireTestFinished(description);
            return;
        }
        for (Description child : children) {
            generateListOfTests(runNotifier, child);
        }
    }
}
