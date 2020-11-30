package androidx.test.internal.runner;

import android.app.Instrumentation;
import android.os.Bundle;
import android.util.Log;
import androidx.test.internal.runner.listener.InstrumentationRunListener;
import androidx.test.internal.util.Checks;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Result;
import org.junit.runner.notification.RunListener;

public final class TestExecutor {
    private final Instrumentation instr;
    private final List<RunListener> listeners;

    private TestExecutor(Builder builder) {
        this.listeners = (List) Checks.checkNotNull(builder.listeners);
        this.instr = builder.instr;
    }

    public Bundle execute(Request request) {
        String str;
        String str2;
        Object[] objArr;
        Bundle resultBundle = new Bundle();
        Result junitResults = new Result();
        try {
            JUnitCore testRunner = new JUnitCore();
            setUpListeners(testRunner);
            Result junitResults2 = testRunner.run(request);
            ByteArrayOutputStream summaryStream = new ByteArrayOutputStream();
            PrintStream summaryWriter = new PrintStream(summaryStream);
            reportRunEnded(this.listeners, summaryWriter, resultBundle, junitResults2);
            summaryWriter.close();
            str = "stream";
            str2 = "\n%s";
            objArr = new Object[]{summaryStream.toString()};
        } catch (Throwable th) {
            ByteArrayOutputStream summaryStream2 = new ByteArrayOutputStream();
            PrintStream summaryWriter2 = new PrintStream(summaryStream2);
            reportRunEnded(this.listeners, summaryWriter2, resultBundle, junitResults);
            summaryWriter2.close();
            resultBundle.putString("stream", String.format("\n%s", summaryStream2.toString()));
            throw th;
        }
        resultBundle.putString(str, String.format(str2, objArr));
        return resultBundle;
    }

    private void setUpListeners(JUnitCore testRunner) {
        for (RunListener listener : this.listeners) {
            String valueOf = String.valueOf(listener.getClass().getName());
            Log.d("TestExecutor", valueOf.length() != 0 ? "Adding listener ".concat(valueOf) : new String("Adding listener "));
            testRunner.addListener(listener);
            if (listener instanceof InstrumentationRunListener) {
                ((InstrumentationRunListener) listener).setInstrumentation(this.instr);
            }
        }
    }

    private void reportRunEnded(List<RunListener> listeners2, PrintStream summaryWriter, Bundle resultBundle, Result jUnitResults) {
        for (RunListener listener : listeners2) {
            if (listener instanceof InstrumentationRunListener) {
                ((InstrumentationRunListener) listener).instrumentationRunFinished(summaryWriter, resultBundle, jUnitResults);
            }
        }
    }

    public static class Builder {
        private final Instrumentation instr;
        private final List<RunListener> listeners = new ArrayList();

        public Builder(Instrumentation instr2) {
            this.instr = instr2;
        }

        public Builder addRunListener(RunListener listener) {
            this.listeners.add(listener);
            return this;
        }

        public TestExecutor build() {
            return new TestExecutor(this);
        }
    }
}
