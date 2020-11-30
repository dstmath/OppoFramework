package androidx.test.internal.runner.listener;

import android.util.Log;
import androidx.test.internal.runner.TestSize;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;

public class SuiteAssignmentPrinter extends InstrumentationRunListener {
    long endTime;
    long startTime;
    boolean timingValid;

    @Override // org.junit.runner.notification.RunListener
    public void testStarted(Description description) throws Exception {
        this.timingValid = true;
        this.startTime = getCurrentTimeMillis();
    }

    @Override // org.junit.runner.notification.RunListener
    public void testFinished(Description description) throws Exception {
        this.endTime = getCurrentTimeMillis();
        if (!this.timingValid || this.startTime < 0) {
            sendString("F");
            Log.d("SuiteAssignmentPrinter", String.format("%s#%s: skipping suite assignment due to test failure\n", description.getClassName(), description.getMethodName()));
        } else {
            long runTime = this.endTime - this.startTime;
            TestSize assignmentSuite = TestSize.getTestSizeForRunTime((float) runTime);
            TestSize currentRenameSize = TestSize.fromDescription(description);
            if (!assignmentSuite.equals(currentRenameSize)) {
                sendString(String.format("\n%s#%s: current size: %s. suggested: %s runTime: %d ms\n", description.getClassName(), description.getMethodName(), currentRenameSize, assignmentSuite.getSizeQualifierName(), Long.valueOf(runTime)));
            } else {
                sendString(".");
                Log.d("SuiteAssignmentPrinter", String.format("%s#%s assigned correctly as %s. runTime: %d ms\n", description.getClassName(), description.getMethodName(), assignmentSuite.getSizeQualifierName(), Long.valueOf(runTime)));
            }
        }
        this.startTime = -1;
    }

    @Override // org.junit.runner.notification.RunListener
    public void testFailure(Failure failure) throws Exception {
        this.timingValid = false;
    }

    @Override // org.junit.runner.notification.RunListener
    public void testAssumptionFailure(Failure failure) {
        this.timingValid = false;
    }

    @Override // org.junit.runner.notification.RunListener
    public void testIgnored(Description description) throws Exception {
        this.timingValid = false;
    }

    public long getCurrentTimeMillis() {
        return System.currentTimeMillis();
    }
}
