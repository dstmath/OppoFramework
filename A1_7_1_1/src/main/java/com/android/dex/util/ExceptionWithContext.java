package com.android.dex.util;

import java.io.PrintStream;
import java.io.PrintWriter;

public class ExceptionWithContext extends RuntimeException {
    private StringBuffer context;

    public static ExceptionWithContext withContext(Throwable ex, String str) {
        ExceptionWithContext ewc;
        if (ex instanceof ExceptionWithContext) {
            ewc = (ExceptionWithContext) ex;
        } else {
            ewc = new ExceptionWithContext(ex);
        }
        ewc.addContext(str);
        return ewc;
    }

    public ExceptionWithContext(String message) {
        this(message, null);
    }

    public ExceptionWithContext(Throwable cause) {
        this(null, cause);
    }

    public ExceptionWithContext(String message, Throwable cause) {
        if (message == null) {
            message = cause != null ? cause.getMessage() : null;
        }
        super(message, cause);
        if (cause instanceof ExceptionWithContext) {
            String ctx = ((ExceptionWithContext) cause).context.toString();
            this.context = new StringBuffer(ctx.length() + 200);
            this.context.append(ctx);
            return;
        }
        this.context = new StringBuffer(200);
    }

    public void printStackTrace(PrintStream out) {
        super.printStackTrace(out);
        out.println(this.context);
    }

    public void printStackTrace(PrintWriter out) {
        super.printStackTrace(out);
        out.println(this.context);
    }

    public void addContext(String str) {
        if (str == null) {
            throw new NullPointerException("str == null");
        }
        this.context.append(str);
        if (!str.endsWith("\n")) {
            this.context.append(10);
        }
    }

    public String getContext() {
        return this.context.toString();
    }

    public void printContext(PrintStream out) {
        out.println(getMessage());
        out.print(this.context);
    }

    public void printContext(PrintWriter out) {
        out.println(getMessage());
        out.print(this.context);
    }
}
