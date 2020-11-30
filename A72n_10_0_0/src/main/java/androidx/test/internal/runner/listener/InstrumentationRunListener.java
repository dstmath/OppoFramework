package androidx.test.internal.runner.listener;

import android.app.Instrumentation;
import android.os.Bundle;
import java.io.PrintStream;
import org.junit.runner.Result;
import org.junit.runner.notification.RunListener;

public abstract class InstrumentationRunListener extends RunListener {
    private Instrumentation instr;

    public Instrumentation getInstrumentation() {
        return this.instr;
    }

    public void setInstrumentation(Instrumentation instr2) {
        this.instr = instr2;
    }

    public void sendStatus(int code, Bundle bundle) {
        getInstrumentation().sendStatus(code, bundle);
    }

    public void sendString(String msg) {
        Bundle b = new Bundle();
        b.putString("stream", msg);
        sendStatus(0, b);
    }

    public void instrumentationRunFinished(PrintStream streamResult, Bundle resultBundle, Result junitResults) {
    }
}
