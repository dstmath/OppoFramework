package androidx.test.internal.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public final class ProcSummary {
    public final String cmdline;
    public final String name;
    public final String parent;
    public final String pid;
    public final String realUid;
    public final long startTime;

    private ProcSummary(Builder b) {
        this.name = (String) Checks.checkNotNull(b.name);
        this.pid = (String) Checks.checkNotNull(b.pid);
        this.realUid = (String) Checks.checkNotNull(b.realUid);
        this.parent = (String) Checks.checkNotNull(b.parent);
        this.cmdline = (String) Checks.checkNotNull(b.cmdline);
        this.startTime = b.startTime;
    }

    public static ProcSummary summarize(String pid2) {
        return parse(readToString(new File(new File("/proc", pid2), "stat")), readToString(new File(new File("/proc", pid2), "status")), readToString(new File(new File("/proc", pid2), "cmdline")));
    }

    private static final String readToString(File path) {
        StringBuilder sb = new StringBuilder();
        char[] buff = new char[1024];
        InputStreamReader isr = null;
        try {
            InputStreamReader isr2 = new InputStreamReader(new FileInputStream(path));
            while (true) {
                int read = isr2.read(buff, 0, buff.length);
                if (read == -1) {
                    break;
                }
                sb.append(buff, 0, read);
            }
            String sb2 = sb.toString();
            try {
                isr2.close();
            } catch (IOException e) {
            }
            return sb2;
        } catch (RuntimeException re) {
            String valueOf = String.valueOf(path);
            StringBuilder sb3 = new StringBuilder(15 + String.valueOf(valueOf).length());
            sb3.append("Error reading: ");
            sb3.append(valueOf);
            throw new SummaryException(sb3.toString(), re);
        } catch (IOException ioe) {
            String valueOf2 = String.valueOf(path);
            StringBuilder sb4 = new StringBuilder(16 + String.valueOf(valueOf2).length());
            sb4.append("Could not read: ");
            sb4.append(valueOf2);
            throw new SummaryException(sb4.toString(), ioe);
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    isr.close();
                } catch (IOException e2) {
                }
            }
            throw th;
        }
    }

    public static class SummaryException extends RuntimeException {
        public SummaryException(String msg, Throwable cause) {
            super(msg, cause);
        }

        public SummaryException(String msg) {
            super(msg);
        }
    }

    static ProcSummary parse(String statLine, String statusContent, String cmdline2) {
        String[] stats = statLine.substring(statLine.lastIndexOf(41) + 2).split(" ", -1);
        String statusContent2 = statusContent.substring(statusContent.indexOf("\nUid:") + 1);
        return new Builder().withPid(statLine.substring(0, statLine.indexOf(32))).withName(statLine.substring(statLine.indexOf(40) + 1, statLine.lastIndexOf(41))).withParent(stats[1]).withRealUid(statusContent2.substring(0, statusContent2.indexOf(10)).split("\\s", -1)[1]).withCmdline(cmdline2.trim().replace((char) 0, ' ')).withStartTime(Long.parseLong(stats[19])).build();
    }

    /* access modifiers changed from: package-private */
    public static class Builder {
        private String cmdline;
        private String name;
        private String parent;
        private String pid;
        private String realUid;
        private long startTime;

        Builder() {
        }

        /* access modifiers changed from: package-private */
        public Builder withParent(String ppid) {
            try {
                Integer.parseInt(ppid);
                this.parent = ppid;
                return this;
            } catch (NumberFormatException e) {
                String valueOf = String.valueOf(ppid);
                throw new IllegalArgumentException(valueOf.length() != 0 ? "not a pid: ".concat(valueOf) : new String("not a pid: "));
            }
        }

        /* access modifiers changed from: package-private */
        public Builder withCmdline(String cmdline2) {
            this.cmdline = cmdline2;
            return this;
        }

        /* access modifiers changed from: package-private */
        public Builder withName(String name2) {
            this.name = name2;
            return this;
        }

        /* access modifiers changed from: package-private */
        public Builder withPid(String pid2) {
            try {
                Integer.parseInt(pid2);
                this.pid = pid2;
                return this;
            } catch (NumberFormatException e) {
                String valueOf = String.valueOf(pid2);
                throw new IllegalArgumentException(valueOf.length() != 0 ? "not a pid: ".concat(valueOf) : new String("not a pid: "));
            }
        }

        /* access modifiers changed from: package-private */
        public Builder withRealUid(String uid) {
            try {
                Integer.parseInt(uid);
                this.realUid = uid;
                return this;
            } catch (NumberFormatException e) {
                String valueOf = String.valueOf(uid);
                throw new IllegalArgumentException(valueOf.length() != 0 ? "not a uid: ".concat(valueOf) : new String("not a uid: "));
            }
        }

        /* access modifiers changed from: package-private */
        public Builder withStartTime(long startTime2) {
            this.startTime = startTime2;
            return this;
        }

        /* access modifiers changed from: package-private */
        public ProcSummary build() {
            return new ProcSummary(this);
        }
    }

    public String toString() {
        return String.format("ProcSummary(name: '%s', cmdline: '%s', pid: '%s', parent: '%s', realUid: '%s', startTime: %d)", this.name, this.cmdline, this.pid, this.parent, this.realUid, Long.valueOf(this.startTime));
    }

    public int hashCode() {
        return this.pid.hashCode();
    }

    public boolean equals(Object o) {
        if (o == null || !(o instanceof ProcSummary)) {
            return false;
        }
        ProcSummary ops = (ProcSummary) o;
        if (!ops.name.equals(this.name) || !ops.pid.equals(this.pid) || !ops.parent.equals(this.parent) || !ops.realUid.equals(this.realUid) || !ops.cmdline.equals(this.cmdline) || ops.startTime != this.startTime) {
            return false;
        }
        return true;
    }
}
