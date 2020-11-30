package androidx.test.internal.runner.listener;

import android.app.Instrumentation;
import android.os.Bundle;
import android.util.Log;
import java.io.File;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import org.junit.runner.Result;

public class CoverageListener extends InstrumentationRunListener {
    private String coverageFilePath;

    public CoverageListener(String customCoverageFilePath) {
        this.coverageFilePath = customCoverageFilePath;
    }

    @Override // androidx.test.internal.runner.listener.InstrumentationRunListener
    public void setInstrumentation(Instrumentation instr) {
        super.setInstrumentation(instr);
        if (this.coverageFilePath == null) {
            String absolutePath = instr.getTargetContext().getFilesDir().getAbsolutePath();
            String str = File.separator;
            StringBuilder sb = new StringBuilder(11 + String.valueOf(absolutePath).length() + String.valueOf(str).length());
            sb.append(absolutePath);
            sb.append(str);
            sb.append("coverage.ec");
            this.coverageFilePath = sb.toString();
        }
    }

    @Override // androidx.test.internal.runner.listener.InstrumentationRunListener
    public void instrumentationRunFinished(PrintStream writer, Bundle results, Result junitResults) {
        generateCoverageReport(writer, results);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:13:0x008c, code lost:
        reportEmmaError(r10, r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0091, code lost:
        reportEmmaError(r10, r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0096, code lost:
        reportEmmaError(r10, r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x009b, code lost:
        reportEmmaError(r10, r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x00a0, code lost:
        reportEmmaError(r10, r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x00a5, code lost:
        r1 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x00a6, code lost:
        reportEmmaError(r10, "Is Emma/JaCoCo jar on classpath?", r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:3:0x001c, code lost:
        r1 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:4:0x001e, code lost:
        r1 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:5:0x0020, code lost:
        r1 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:6:0x0023, code lost:
        r1 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:7:0x0026, code lost:
        r1 = move-exception;
     */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Removed duplicated region for block: B:3:0x001c A[ExcHandler: InvocationTargetException (r1v7 'e' java.lang.reflect.InvocationTargetException A[CUSTOM_DECLARE]), Splitter:B:1:0x000b] */
    /* JADX WARNING: Removed duplicated region for block: B:4:0x001e A[ExcHandler: IllegalAccessException (r1v6 'e' java.lang.IllegalAccessException A[CUSTOM_DECLARE]), Splitter:B:1:0x000b] */
    /* JADX WARNING: Removed duplicated region for block: B:5:0x0020 A[ExcHandler: IllegalArgumentException (r1v5 'e' java.lang.IllegalArgumentException A[CUSTOM_DECLARE]), Splitter:B:1:0x000b] */
    /* JADX WARNING: Removed duplicated region for block: B:6:0x0023 A[ExcHandler: NoSuchMethodException (r1v4 'e' java.lang.NoSuchMethodException A[CUSTOM_DECLARE]), Splitter:B:1:0x000b] */
    /* JADX WARNING: Removed duplicated region for block: B:7:0x0026 A[ExcHandler: SecurityException (r1v3 'e' java.lang.SecurityException A[CUSTOM_DECLARE]), Splitter:B:1:0x000b] */
    private void generateCoverageReport(PrintStream writer, Bundle results) {
        Class<?> emmaRTClass;
        File coverageFile = new File(this.coverageFilePath);
        try {
            emmaRTClass = Class.forName("com.vladium.emma.rt.RT", true, getInstrumentation().getTargetContext().getClassLoader());
        } catch (ClassNotFoundException e) {
            Class<?> emmaRTClass2 = Class.forName("com.vladium.emma.rt.RT", true, getInstrumentation().getContext().getClassLoader());
            Log.w("CoverageListener", "Generating coverage for alternate test context.");
            writer.format("\nWarning: %s", "Generating coverage for alternate test context.");
            emmaRTClass = emmaRTClass2;
        } catch (SecurityException e2) {
        } catch (NoSuchMethodException e3) {
        } catch (IllegalArgumentException e4) {
        } catch (IllegalAccessException e5) {
        } catch (InvocationTargetException e6) {
        }
        emmaRTClass.getMethod("dumpCoverageData", coverageFile.getClass(), Boolean.TYPE, Boolean.TYPE).invoke(null, coverageFile, false, false);
        results.putString("coverageFilePath", this.coverageFilePath);
        writer.format("\nGenerated code coverage data to %s", this.coverageFilePath);
    }

    private void reportEmmaError(PrintStream writer, Exception e) {
        reportEmmaError(writer, "", e);
    }

    private void reportEmmaError(PrintStream writer, String hint, Exception e) {
        String valueOf = String.valueOf(hint);
        String msg = valueOf.length() != 0 ? "Failed to generate Emma/JaCoCo coverage. ".concat(valueOf) : new String("Failed to generate Emma/JaCoCo coverage. ");
        Log.e("CoverageListener", msg, e);
        writer.format("\nError: %s", msg);
    }
}
