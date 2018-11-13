package com.android.server;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Binder;
import android.os.DropBoxManager.Entry;
import android.os.FileUtils;
import android.os.Handler;
import android.os.Message;
import android.os.OppoManager;
import android.os.StatFs;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings.Global;
import android.telephony.ColorOSTelephonyManager;
import android.text.format.Time;
import android.util.Slog;
import com.android.internal.os.IDropBoxManagerService;
import com.android.internal.os.IDropBoxManagerService.Stub;
import com.android.server.oppo.IElsaManager;
import com.oppo.debug.ASSERT;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.zip.GZIPOutputStream;
import libcore.io.IoUtils;

public final class DropBoxManagerService extends SystemService {
    private static final int DEFAULT_AGE_SECONDS = 259200;
    private static final int DEFAULT_MAX_FILES = 1000;
    private static final int DEFAULT_QUOTA_KB = 5120;
    private static final int DEFAULT_QUOTA_PERCENT = 10;
    private static final int DEFAULT_RESERVE_PERCENT = 10;
    private static final int MSG_SEND_BROADCAST = 1;
    private static final boolean PROFILE_DUMP = false;
    private static final int QUOTA_RESCAN_MILLIS = 5000;
    private static final String TAG = "DropBoxManagerService";
    private static String mIMEI;
    private static String mOtaVersion;
    private static String mProcessName;
    private static String mTime;
    private FileList mAllFiles;
    private int mBlockSize;
    private volatile boolean mBooted;
    private int mCachedQuotaBlocks;
    private long mCachedQuotaUptimeMillis;
    private final ContentResolver mContentResolver;
    private final File mDropBoxDir;
    private HashMap<String, FileList> mFilesByTag;
    private final Handler mHandler;
    private final BroadcastReceiver mReceiver;
    private StatFs mStatFs;
    private final Stub mStub;

    private static final class EntryFile implements Comparable<EntryFile> {
        public final int blocks;
        public final File file;
        public final int flags;
        public final String tag;
        public final long timestampMillis;

        public final int compareTo(EntryFile o) {
            if (this.timestampMillis < o.timestampMillis) {
                return -1;
            }
            if (this.timestampMillis > o.timestampMillis) {
                return 1;
            }
            if (this.file != null && o.file != null) {
                return this.file.compareTo(o.file);
            }
            if (o.file != null) {
                return -1;
            }
            if (this.file != null) {
                return 1;
            }
            if (this == o) {
                return 0;
            }
            if (hashCode() < o.hashCode()) {
                return -1;
            }
            return hashCode() > o.hashCode() ? 1 : 0;
        }

        public EntryFile(File temp, File dir, String tag, long timestampMillis, int flags, int blockSize) throws IOException {
            if ((flags & 1) != 0) {
                throw new IllegalArgumentException();
            }
            if (tag.contains("app_anr") || tag.contains("app_crash")) {
                this.tag = tag;
                this.timestampMillis = timestampMillis;
                this.flags = flags;
                if (DropBoxManagerService.mProcessName != null && DropBoxManagerService.mProcessName.contains(":")) {
                    DropBoxManagerService.mProcessName = DropBoxManagerService.mProcessName.replace(':', '_');
                }
                this.file = new File(dir, DropBoxManagerService.getSaveDate() + "@" + DropBoxManagerService.mOtaVersion + "@" + DropBoxManagerService.mProcessName + "@" + Uri.encode(tag) + "@" + DropBoxManagerService.mIMEI + LocationManagerService.OPPO_FAKE_LOCATION_SPLIT + DropBoxManagerService.mTime + LocationManagerService.OPPO_FAKE_LOCATION_SPLIT + timestampMillis + ((flags & 2) != 0 ? ".txt" : ".dat") + ((flags & 4) != 0 ? ".gz" : IElsaManager.EMPTY_PACKAGE));
            } else {
                this.tag = tag;
                this.timestampMillis = timestampMillis;
                this.flags = flags;
                this.file = new File(dir, Uri.encode(tag) + "@" + timestampMillis + ((flags & 2) != 0 ? ".txt" : ".dat") + ((flags & 4) != 0 ? ".gz" : IElsaManager.EMPTY_PACKAGE));
            }
            if (temp.renameTo(this.file)) {
                this.blocks = (int) (((this.file.length() + ((long) blockSize)) - 1) / ((long) blockSize));
                return;
            }
            throw new IOException("Can't rename " + temp + " to " + this.file);
        }

        public EntryFile(File dir, String tag, long timestampMillis) throws IOException {
            this.tag = tag;
            this.timestampMillis = timestampMillis;
            this.flags = 1;
            this.file = new File(dir, Uri.encode(tag) + "@" + timestampMillis + ".lost");
            this.blocks = 0;
            new FileOutputStream(this.file).close();
        }

        public EntryFile(File file, int blockSize) {
            this.file = file;
            this.blocks = (int) (((this.file.length() + ((long) blockSize)) - 1) / ((long) blockSize));
            String name = file.getName();
            if (name.contains("@data_app_anr@") || name.contains("@system_app_anr@") || name.contains("@data_app_crash@") || name.contains("@system_app_crash@")) {
                long time;
                int flag = 0;
                if (name.endsWith(".gz")) {
                    flag = 4;
                    name = name.substring(0, name.length() - 3);
                }
                if (name.endsWith(".txt")) {
                    flag |= 2;
                    name = name.substring(0, name.length() - 4);
                }
                this.flags = flag;
                String[] value = name.split("@");
                if (value.length == 7) {
                    this.tag = Uri.decode(value[5]);
                } else {
                    this.tag = null;
                }
                try {
                    time = Long.valueOf(name.substring(name.lastIndexOf(95) + 1, name.length())).longValue();
                } catch (NumberFormatException e) {
                    time = 0;
                }
                this.timestampMillis = time;
                return;
            }
            int at = name.lastIndexOf(64);
            if (at < 0) {
                this.tag = null;
                this.timestampMillis = 0;
                this.flags = 1;
                return;
            }
            long millis;
            int flags = 0;
            this.tag = Uri.decode(name.substring(0, at));
            if (name.endsWith(".gz")) {
                flags = 4;
                name = name.substring(0, name.length() - 3);
            }
            if (name.endsWith(".lost")) {
                flags |= 1;
                name = name.substring(at + 1, name.length() - 5);
            } else if (name.endsWith(".txt")) {
                flags |= 2;
                name = name.substring(at + 1, name.length() - 4);
            } else if (name.endsWith(".dat")) {
                name = name.substring(at + 1, name.length() - 4);
            } else {
                this.flags = 1;
                this.timestampMillis = 0;
                return;
            }
            this.flags = flags;
            try {
                millis = Long.valueOf(name).longValue();
            } catch (NumberFormatException e2) {
                millis = 0;
            }
            this.timestampMillis = millis;
        }

        public EntryFile(long millis) {
            this.tag = null;
            this.timestampMillis = millis;
            this.flags = 1;
            this.file = null;
            this.blocks = 0;
        }
    }

    private static final class FileList implements Comparable<FileList> {
        public int blocks;
        public final TreeSet<EntryFile> contents;

        /* synthetic */ FileList(FileList fileList) {
            this();
        }

        private FileList() {
            this.blocks = 0;
            this.contents = new TreeSet();
        }

        public final int compareTo(FileList o) {
            if (this.blocks != o.blocks) {
                return o.blocks - this.blocks;
            }
            if (this == o) {
                return 0;
            }
            if (hashCode() < o.hashCode()) {
                return -1;
            }
            if (hashCode() > o.hashCode()) {
                return 1;
            }
            return 0;
        }
    }

    public DropBoxManagerService(Context context) {
        this(context, new File("/data/system/dropbox"));
    }

    public DropBoxManagerService(Context context, File path) {
        super(context);
        this.mAllFiles = null;
        this.mFilesByTag = null;
        this.mStatFs = null;
        this.mBlockSize = 0;
        this.mCachedQuotaBlocks = 0;
        this.mCachedQuotaUptimeMillis = 0;
        this.mBooted = false;
        this.mReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                DropBoxManagerService.this.mCachedQuotaUptimeMillis = 0;
                new Thread() {
                    public void run() {
                        try {
                            DropBoxManagerService.this.init();
                            DropBoxManagerService.this.trimToFit();
                        } catch (IOException e) {
                            Slog.e(DropBoxManagerService.TAG, "Can't init", e);
                        }
                    }
                }.start();
            }
        };
        this.mStub = new Stub() {
            public void add(Entry entry) {
                DropBoxManagerService.this.add(entry);
            }

            public boolean isTagEnabled(String tag) {
                return DropBoxManagerService.this.isTagEnabled(tag);
            }

            public Entry getNextEntry(String tag, long millis) {
                return DropBoxManagerService.this.getNextEntry(tag, millis);
            }

            public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
                DropBoxManagerService.this.dump(fd, pw, args);
            }
        };
        this.mDropBoxDir = path;
        this.mContentResolver = getContext().getContentResolver();
        this.mHandler = new Handler() {
            public void handleMessage(Message msg) {
                if (msg.what == 1) {
                    DropBoxManagerService.this.getContext().sendBroadcastAsUser((Intent) msg.obj, UserHandle.SYSTEM, "android.permission.READ_LOGS");
                }
            }
        };
    }

    public void onStart() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.DEVICE_STORAGE_LOW");
        getContext().registerReceiver(this.mReceiver, filter);
        this.mContentResolver.registerContentObserver(Global.CONTENT_URI, true, new ContentObserver(new Handler()) {
            public void onChange(boolean selfChange) {
                DropBoxManagerService.this.mReceiver.onReceive(DropBoxManagerService.this.getContext(), (Intent) null);
            }
        });
        publishBinderService("dropbox", this.mStub);
        mOtaVersion = SystemProperties.get("ro.build.version.ota");
    }

    public void onBootPhase(int phase) {
        switch (phase) {
            case 1000:
                this.mBooted = true;
                return;
            default:
                return;
        }
    }

    public IDropBoxManagerService getServiceStub() {
        return this.mStub;
    }

    /* JADX WARNING: Removed duplicated region for block: B:135:0x043a  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void add(Entry entry) {
        boolean assertEnable;
        IOException e;
        Throwable th;
        File temp = null;
        AutoCloseable input = null;
        AutoCloseable output = null;
        String tag = entry.getTag();
        if (SystemProperties.getBoolean("persist.sys.assert.enable", false)) {
            assertEnable = true;
        } else {
            assertEnable = SystemProperties.getBoolean("persist.sys.assert.panic", false);
        }
        try {
            if (tag.contains("app_anr") || tag.contains("app_crash")) {
                initProcessName(entry);
                initTimeValue(entry);
                initIMEI();
            }
            int flags = entry.getFlags();
            if ((flags & 1) != 0) {
                throw new IllegalArgumentException();
            }
            init();
            if (isTagEnabled(tag)) {
                FileOutputStream foutput;
                OutputStream bufferedOutputStream;
                if (tag.equals("SYSTEM_SERVER_GZ") || tag.equals("SYSTEM_SERVER_WATCHDOG") || tag.equals("SYSTEM_SERVER") || tag.equals("SYSTEM_TOMBSTONE_CRASH")) {
                    OppoManager.writeLogToPartition(OppoManager.TYPE_ANDROID_CRASH, "system_restart", "ANDROID", "crash", getContext().getResources().getString(17040923));
                }
                long max = trimToFit();
                long lastTrim = System.currentTimeMillis();
                byte[] buffer = new byte[this.mBlockSize];
                input = entry.getInputStream();
                int read = 0;
                while (read < buffer.length) {
                    int n = input.read(buffer, read, buffer.length - read);
                    if (n <= 0) {
                        break;
                    }
                    read += n;
                }
                File file = new File(this.mDropBoxDir, "drop" + Thread.currentThread().getId() + ".tmp");
                try {
                    int bufferSize = this.mBlockSize;
                    if (bufferSize > 4096) {
                        bufferSize = 4096;
                    }
                    if (bufferSize < 512) {
                        bufferSize = 512;
                    }
                    foutput = new FileOutputStream(file);
                    bufferedOutputStream = new BufferedOutputStream(foutput, bufferSize);
                } catch (IOException e2) {
                    e = e2;
                    temp = file;
                } catch (Throwable th2) {
                    th = th2;
                    temp = file;
                }
                Object output2;
                try {
                    if (read == buffer.length && (flags & 4) == 0) {
                        output = new GZIPOutputStream(bufferedOutputStream);
                        flags |= 4;
                    } else {
                        output2 = bufferedOutputStream;
                    }
                    do {
                        output.write(buffer, 0, read);
                        long now = System.currentTimeMillis();
                        if (now - lastTrim > 30000) {
                            max = trimToFit();
                            lastTrim = now;
                        }
                        read = input.read(buffer);
                        if (read <= 0) {
                            FileUtils.sync(foutput);
                            output.close();
                            output = null;
                        } else {
                            output.flush();
                        }
                        if (file.length() > max) {
                            Slog.w(TAG, "Dropping: " + tag + " (" + file.length() + " > " + max + " bytes)");
                            file.delete();
                            temp = null;
                            break;
                        }
                    } while (read > 0);
                    temp = file;
                    long time = createEntry(temp, tag, flags);
                    temp = null;
                    File[] logFiles = new File("/data/system/dropbox").listFiles();
                    int i = 0;
                    while (logFiles != null && i < logFiles.length) {
                        String name = logFiles[i].getName();
                        if (name.endsWith(".gz")) {
                            name = name.substring(0, name.length() - 3);
                        }
                        if (name.endsWith(".lost")) {
                            name = name.substring(0, name.length() - 5);
                        } else if (name.endsWith(".txt")) {
                            name = name.substring(0, name.length() - 4);
                        } else if (name.endsWith(".dat")) {
                            name = name.substring(0, name.length() - 4);
                        }
                        if (name.contains(String.valueOf(time)) && name.contains(tag)) {
                            new ArrayList().add("/data/system/dropbox/" + logFiles[i].getName());
                            Slog.d(TAG, "file :: /data/system/dropbox/" + logFiles[i].getName());
                            if (assertEnable) {
                                if (tag.equals("system_app_strictmode") || tag.equals("data_app_strictmode") || tag.equals("SYSTEM_BOOT") || tag.equals("system_server_wtf") || tag.equals("SYSTEM_RECOVERY_LOG") || tag.equals("SYSTEM_LAST_KMSG") || tag.equals("SYSTEM_AUDIT") || tag.equals("netstats_error") || tag.equals("system_server_lowmem")) {
                                    Slog.d(TAG, "the tag is  :: " + tag);
                                } else if (tag.startsWith("system_server", 0) || tag.equals("system_app_crash") || tag.equals("system_app_anr") || tag.equals("data_app_crash") || tag.equals("data_app_anr")) {
                                    Slog.d(TAG, "assert append,the tag is  :: " + tag);
                                    ASSERT.epitaph(logFiles[i], tag, flags, getContext());
                                }
                            }
                        }
                        i++;
                    }
                    IoUtils.closeQuietly(output);
                    IoUtils.closeQuietly(input);
                    entry.close();
                } catch (IOException e3) {
                    e = e3;
                    output2 = bufferedOutputStream;
                    temp = file;
                    try {
                        Slog.e(TAG, "Can't write: " + tag, e);
                        IoUtils.closeQuietly(output);
                        IoUtils.closeQuietly(input);
                        entry.close();
                        if (temp != null) {
                            temp.delete();
                        }
                        return;
                    } catch (Throwable th3) {
                        th = th3;
                        IoUtils.closeQuietly(output);
                        IoUtils.closeQuietly(input);
                        entry.close();
                        if (temp != null) {
                        }
                        throw th;
                    }
                } catch (Throwable th4) {
                    th = th4;
                    output2 = bufferedOutputStream;
                    temp = file;
                    IoUtils.closeQuietly(output);
                    IoUtils.closeQuietly(input);
                    entry.close();
                    if (temp != null) {
                        temp.delete();
                    }
                    throw th;
                }
                return;
            }
            IoUtils.closeQuietly(null);
            IoUtils.closeQuietly(null);
            entry.close();
        } catch (IOException e4) {
            e = e4;
        }
    }

    public boolean isTagEnabled(String tag) {
        long token = Binder.clearCallingIdentity();
        try {
            boolean z = !"disabled".equals(Global.getString(this.mContentResolver, new StringBuilder().append("dropbox:").append(tag).toString()));
            Binder.restoreCallingIdentity(token);
            return z;
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(token);
        }
    }

    public synchronized Entry getNextEntry(String tag, long millis) {
        if (getContext().checkCallingOrSelfPermission("android.permission.READ_LOGS") != 0) {
            throw new SecurityException("READ_LOGS permission required");
        }
        try {
            init();
            FileList list = tag == null ? this.mAllFiles : (FileList) this.mFilesByTag.get(tag);
            if (list == null) {
                return null;
            }
            for (EntryFile entry : list.contents.tailSet(new EntryFile(1 + millis))) {
                if (entry.tag != null) {
                    if ((entry.flags & 1) != 0) {
                        return new Entry(entry.tag, entry.timestampMillis);
                    }
                    try {
                        return new Entry(entry.tag, entry.timestampMillis, entry.file, entry.flags);
                    } catch (IOException e) {
                        Slog.e(TAG, "Can't read: " + entry.file, e);
                    }
                }
            }
            return null;
        } catch (IOException e2) {
            Slog.e(TAG, "Can't init", e2);
            return null;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:192:0x0126 A:{SYNTHETIC} */
    /* JADX WARNING: Removed duplicated region for block: B:90:0x0236 A:{Catch:{ IOException -> 0x004c }} */
    /* JADX WARNING: Removed duplicated region for block: B:127:0x02f6 A:{SYNTHETIC, Splitter: B:127:0x02f6} */
    /* JADX WARNING: Removed duplicated region for block: B:130:0x02fb A:{SYNTHETIC, Splitter: B:130:0x02fb} */
    /* JADX WARNING: Removed duplicated region for block: B:90:0x0236 A:{Catch:{ IOException -> 0x004c }} */
    /* JADX WARNING: Removed duplicated region for block: B:192:0x0126 A:{SYNTHETIC} */
    /* JADX WARNING: Removed duplicated region for block: B:127:0x02f6 A:{SYNTHETIC, Splitter: B:127:0x02f6} */
    /* JADX WARNING: Removed duplicated region for block: B:130:0x02fb A:{SYNTHETIC, Splitter: B:130:0x02fb} */
    /* JADX WARNING: Removed duplicated region for block: B:192:0x0126 A:{SYNTHETIC} */
    /* JADX WARNING: Removed duplicated region for block: B:90:0x0236 A:{Catch:{ IOException -> 0x004c }} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        Throwable e;
        Throwable th;
        if (getContext().checkCallingOrSelfPermission("android.permission.DUMP") != 0) {
            pw.println("Permission Denial: Can't dump DropBoxManagerService");
            return;
        }
        try {
            init();
            StringBuilder out = new StringBuilder();
            boolean doPrint = false;
            boolean doFile = false;
            ArrayList<String> searchArgs = new ArrayList();
            int i = 0;
            while (args != null && i < args.length) {
                if (args[i].equals("-p") || args[i].equals("--print")) {
                    doPrint = true;
                } else if (args[i].equals("-f") || args[i].equals("--file")) {
                    doFile = true;
                } else if (args[i].startsWith("-")) {
                    out.append("Unknown argument: ").append(args[i]).append("\n");
                } else {
                    searchArgs.add(args[i]);
                }
                i++;
            }
            out.append("Drop box contents: ").append(this.mAllFiles.contents.size()).append(" entries\n");
            if (!searchArgs.isEmpty()) {
                out.append("Searching for:");
                for (String a : searchArgs) {
                    out.append(" ").append(a);
                }
                out.append("\n");
            }
            int numFound = 0;
            int numArgs = searchArgs.size();
            Time time = new Time();
            out.append("\n");
            for (EntryFile entry : this.mAllFiles.contents) {
                time.set(entry.timestampMillis);
                String date = time.format("%Y-%m-%d %H:%M:%S");
                boolean match = true;
                for (i = 0; i < numArgs && match; i++) {
                    String arg = (String) searchArgs.get(i);
                    match = !date.contains(arg) ? arg.equals(entry.tag) : true;
                }
                if (match) {
                    numFound++;
                    if (doPrint) {
                        out.append("========================================\n");
                    }
                    out.append(date).append(" ").append(entry.tag == null ? "(no tag)" : entry.tag);
                    if (entry.file == null) {
                        out.append(" (no file)\n");
                    } else if ((entry.flags & 1) != 0) {
                        out.append(" (contents lost)\n");
                    } else {
                        out.append(" (");
                        if ((entry.flags & 4) != 0) {
                            out.append("compressed ");
                        }
                        out.append((entry.flags & 2) != 0 ? "text" : "data");
                        out.append(", ").append(entry.file.length()).append(" bytes)\n");
                        if (doFile || (doPrint && (entry.flags & 2) == 0)) {
                            if (!doPrint) {
                                out.append("    ");
                            }
                            out.append(entry.file.getPath()).append("\n");
                        }
                        if ((entry.flags & 2) != 0 && (doPrint || !doFile)) {
                            InputStreamReader isr = null;
                            Entry dbe;
                            try {
                                dbe = new Entry(entry.tag, entry.timestampMillis, entry.file, entry.flags);
                                if (doPrint) {
                                    InputStreamReader inputStreamReader;
                                    try {
                                        inputStreamReader = new InputStreamReader(dbe.getInputStream());
                                    } catch (IOException e2) {
                                        e = e2;
                                        try {
                                            out.append("*** ").append(e.toString()).append("\n");
                                            Slog.e(TAG, "Can't read: " + entry.file, e);
                                            if (dbe != null) {
                                            }
                                            if (isr != null) {
                                            }
                                            if (doPrint) {
                                            }
                                        } catch (Throwable th2) {
                                            th = th2;
                                        }
                                    }
                                    try {
                                        char[] buf = new char[4096];
                                        boolean newline = false;
                                        while (true) {
                                            int n = inputStreamReader.read(buf);
                                            if (n <= 0) {
                                                break;
                                            }
                                            out.append(buf, 0, n);
                                            newline = buf[n + -1] == 10;
                                            if (out.length() > DumpState.DUMP_INSTALLS) {
                                                pw.write(out.toString());
                                                out.setLength(0);
                                            }
                                        }
                                        if (newline) {
                                            isr = inputStreamReader;
                                        } else {
                                            out.append("\n");
                                            isr = inputStreamReader;
                                        }
                                    } catch (IOException e3) {
                                        e = e3;
                                        isr = inputStreamReader;
                                        out.append("*** ").append(e.toString()).append("\n");
                                        Slog.e(TAG, "Can't read: " + entry.file, e);
                                        if (dbe != null) {
                                            dbe.close();
                                        }
                                        if (isr != null) {
                                            try {
                                                isr.close();
                                            } catch (IOException e4) {
                                            }
                                        }
                                        if (doPrint) {
                                        }
                                    } catch (Throwable th3) {
                                        th = th3;
                                        isr = inputStreamReader;
                                        if (dbe != null) {
                                            dbe.close();
                                        }
                                        if (isr != null) {
                                            try {
                                                isr.close();
                                            } catch (IOException e5) {
                                            }
                                        }
                                        throw th;
                                    }
                                }
                                String text = dbe.getText(70);
                                out.append("    ");
                                if (text == null) {
                                    out.append("[null]");
                                } else {
                                    boolean truncated = text.length() == 70;
                                    out.append(text.trim().replace(10, '/'));
                                    if (truncated) {
                                        out.append(" ...");
                                    }
                                }
                                out.append("\n");
                                if (dbe != null) {
                                    dbe.close();
                                }
                                if (isr != null) {
                                    try {
                                        isr.close();
                                    } catch (IOException e6) {
                                    }
                                }
                            } catch (IOException e7) {
                                e = e7;
                                dbe = null;
                                out.append("*** ").append(e.toString()).append("\n");
                                Slog.e(TAG, "Can't read: " + entry.file, e);
                                if (dbe != null) {
                                }
                                if (isr != null) {
                                }
                                if (doPrint) {
                                }
                            } catch (Throwable th4) {
                                th = th4;
                                dbe = null;
                            }
                        }
                        if (doPrint) {
                            out.append("\n");
                        }
                    }
                }
            }
            if (numFound == 0) {
                out.append("(No entries found.)\n");
            }
            if (args == null || args.length == 0) {
                if (!doPrint) {
                    out.append("\n");
                }
                out.append("Usage: dumpsys dropbox [--print|--file] [YYYY-mm-dd] [HH:MM:SS] [tag]\n");
            }
            pw.write(out.toString());
        } catch (Throwable e8) {
            pw.println("Can't initialize: " + e8);
            Slog.e(TAG, "Can't init", e8);
        }
    }

    private synchronized void init() throws IOException {
        if (this.mStatFs == null) {
            if (this.mDropBoxDir.isDirectory() || this.mDropBoxDir.mkdirs()) {
                try {
                    this.mStatFs = new StatFs(this.mDropBoxDir.getPath());
                    this.mBlockSize = this.mStatFs.getBlockSize();
                } catch (IllegalArgumentException e) {
                    throw new IOException("Can't statfs: " + this.mDropBoxDir);
                }
            }
            throw new IOException("Can't mkdir: " + this.mDropBoxDir);
        }
        if (this.mAllFiles == null) {
            File[] files = this.mDropBoxDir.listFiles();
            if (files == null) {
                throw new IOException("Can't list files: " + this.mDropBoxDir);
            }
            this.mAllFiles = new FileList();
            this.mFilesByTag = new HashMap();
            for (File file : files) {
                if (file.getName().endsWith(".tmp")) {
                    Slog.i(TAG, "Cleaning temp file: " + file);
                    file.delete();
                } else {
                    EntryFile entry = new EntryFile(file, this.mBlockSize);
                    if (entry.tag == null) {
                        Slog.w(TAG, "Unrecognized file: " + file);
                    } else if (entry.timestampMillis == 0) {
                        Slog.w(TAG, "Invalid filename: " + file);
                        file.delete();
                    } else {
                        enrollEntry(entry);
                    }
                }
            }
        }
    }

    private synchronized void enrollEntry(EntryFile entry) {
        this.mAllFiles.contents.add(entry);
        FileList fileList = this.mAllFiles;
        fileList.blocks += entry.blocks;
        if (!(entry.tag == null || entry.file == null || entry.blocks <= 0)) {
            FileList tagFiles = (FileList) this.mFilesByTag.get(entry.tag);
            if (tagFiles == null) {
                tagFiles = new FileList();
                this.mFilesByTag.put(entry.tag, tagFiles);
            }
            tagFiles.contents.add(entry);
            tagFiles.blocks += entry.blocks;
        }
    }

    private synchronized long createEntry(File temp, String tag, int flags) throws IOException {
        long t;
        t = System.currentTimeMillis();
        SortedSet<EntryFile> tail = this.mAllFiles.contents.tailSet(new EntryFile(10000 + t));
        EntryFile[] future = null;
        if (!tail.isEmpty()) {
            future = (EntryFile[]) tail.toArray(new EntryFile[tail.size()]);
            tail.clear();
        }
        if (!this.mAllFiles.contents.isEmpty()) {
            t = Math.max(t, ((EntryFile) this.mAllFiles.contents.last()).timestampMillis + 1);
        }
        if (future != null) {
            int i = 0;
            int length = future.length;
            while (true) {
                int i2 = i;
                if (i2 >= length) {
                    break;
                }
                long t2;
                EntryFile late = future[i2];
                FileList fileList = this.mAllFiles;
                fileList.blocks -= late.blocks;
                FileList tagFiles = (FileList) this.mFilesByTag.get(late.tag);
                if (tagFiles != null && tagFiles.contents.remove(late)) {
                    tagFiles.blocks -= late.blocks;
                }
                if ((late.flags & 1) == 0) {
                    t2 = t + 1;
                    enrollEntry(new EntryFile(late.file, this.mDropBoxDir, late.tag, t, late.flags, this.mBlockSize));
                } else {
                    t2 = t + 1;
                    enrollEntry(new EntryFile(this.mDropBoxDir, late.tag, t));
                }
                t = t2;
                i = i2 + 1;
            }
        }
        if (temp == null) {
            enrollEntry(new EntryFile(this.mDropBoxDir, tag, t));
        } else {
            enrollEntry(new EntryFile(temp, this.mDropBoxDir, tag, t, flags, this.mBlockSize));
        }
        return t;
    }

    private synchronized long trimToFit() {
        EntryFile entry;
        FileList tag;
        FileList fileList;
        int ageSeconds = Global.getInt(this.mContentResolver, "dropbox_age_seconds", DEFAULT_AGE_SECONDS);
        int maxFiles = Global.getInt(this.mContentResolver, "dropbox_max_files", 1000);
        long cutoffMillis = System.currentTimeMillis() - ((long) (ageSeconds * 1000));
        while (!this.mAllFiles.contents.isEmpty()) {
            entry = (EntryFile) this.mAllFiles.contents.first();
            if (entry.timestampMillis > cutoffMillis && this.mAllFiles.contents.size() < maxFiles) {
                break;
            }
            tag = (FileList) this.mFilesByTag.get(entry.tag);
            if (tag != null && tag.contents.remove(entry)) {
                tag.blocks -= entry.blocks;
            }
            if (this.mAllFiles.contents.remove(entry)) {
                fileList = this.mAllFiles;
                fileList.blocks -= entry.blocks;
            }
            if (entry.file != null) {
                entry.file.delete();
            }
        }
        long uptimeMillis = SystemClock.uptimeMillis();
        if (uptimeMillis > this.mCachedQuotaUptimeMillis + 5000) {
            int quotaPercent = Global.getInt(this.mContentResolver, "dropbox_quota_percent", 10);
            int reservePercent = Global.getInt(this.mContentResolver, "dropbox_reserve_percent", 10);
            int quotaKb = Global.getInt(this.mContentResolver, "dropbox_quota_kb", DEFAULT_QUOTA_KB);
            if (!(this.mDropBoxDir.exists() || this.mDropBoxDir.isDirectory())) {
                this.mDropBoxDir.mkdirs();
            }
            this.mStatFs.restat(this.mDropBoxDir.getPath());
            this.mCachedQuotaBlocks = Math.min((quotaKb * 1024) / this.mBlockSize, Math.max(0, ((this.mStatFs.getAvailableBlocks() - ((this.mStatFs.getBlockCount() * reservePercent) / 100)) * quotaPercent) / 100));
            this.mCachedQuotaUptimeMillis = uptimeMillis;
        }
        if (this.mAllFiles.blocks > this.mCachedQuotaBlocks) {
            int unsqueezed = this.mAllFiles.blocks;
            int squeezed = 0;
            TreeSet<FileList> treeSet = new TreeSet(this.mFilesByTag.values());
            for (FileList tag2 : treeSet) {
                if (squeezed > 0 && tag2.blocks <= (this.mCachedQuotaBlocks - unsqueezed) / squeezed) {
                    break;
                }
                unsqueezed -= tag2.blocks;
                squeezed++;
            }
            int tagQuota = (this.mCachedQuotaBlocks - unsqueezed) / squeezed;
            for (FileList tag22 : treeSet) {
                if (this.mAllFiles.blocks < this.mCachedQuotaBlocks) {
                    break;
                }
                while (tag22.blocks > tagQuota) {
                    if (tag22.contents.isEmpty()) {
                        break;
                    }
                    entry = (EntryFile) tag22.contents.first();
                    if (tag22.contents.remove(entry)) {
                        tag22.blocks -= entry.blocks;
                    }
                    if (this.mAllFiles.contents.remove(entry)) {
                        fileList = this.mAllFiles;
                        fileList.blocks -= entry.blocks;
                    }
                    try {
                        if (entry.file != null) {
                            entry.file.delete();
                        }
                        enrollEntry(new EntryFile(this.mDropBoxDir, entry.tag, entry.timestampMillis));
                    } catch (IOException e) {
                        Slog.e(TAG, "Can't write tombstone file", e);
                    }
                }
            }
        }
        return (long) (this.mCachedQuotaBlocks * this.mBlockSize);
    }

    private void initIMEI() {
        mIMEI = ColorOSTelephonyManager.getDefault(getContext()).colorGetImei(0);
    }

    private void initProcessName(Entry entry) {
        try {
            String text = entry.getText(500);
            String value = text.substring(text.indexOf("Package: ") + 9, text.indexOf("PID:") - 1);
            mProcessName = value.substring(0, value.indexOf(" v"));
        } catch (Exception e) {
            Slog.d(TAG, "fail to init package name, " + e);
            mProcessName = "unknown";
        }
    }

    private void initTimeValue(Entry entry) {
        try {
            String info = entry.getText(300);
            mTime = info.substring(info.indexOf("Time: ") + 6, info.indexOf("Flags:") - 1);
        } catch (Exception e) {
            Slog.d(TAG, "fail to init time value, " + e);
            mTime = "0";
        }
    }

    private static String getSaveDate() {
        return new SimpleDateFormat("yyyy@MM@dd", Locale.US).format(new Date());
    }
}
