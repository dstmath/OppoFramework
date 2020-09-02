package android.os;

import android.provider.SettingsStringUtil;
import android.telephony.SmsManager;
import android.util.Log;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OppoBinderRecorder {
    static final int FLAG_ONEWAY = 1;
    static final int STATE_FINISH = 3;
    static final int STATE_FOUND_CONTEXT = 2;
    static final int STATE_FOUND_PROC = 1;
    static final int STATE_NOT_FOUND = 0;
    public static final String TAG = "OppoBinderRecorder";
    private static OppoBinderRecorder mInstance = null;
    private long mMaxTimeUsed = 0;
    private String mMaxTimeUsedDescriptor = null;

    public final class ThreadUsage {
        final ArrayList<ThreadUsageElement> mUsageList = new ArrayList<>();

        public ThreadUsage() {
        }

        public final class ThreadUsageElement {
            private int mCount = 0;
            private String mName = "unknown";
            private int mToPid = 0;

            public ThreadUsageElement(int toPid) {
                this.mToPid = toPid;
                initName(toPid);
            }

            private void initName(int pid) {
                String cmdline = "/proc/" + pid + "/cmdline";
                FileInputStream fis = null;
                try {
                    FileInputStream fis2 = new FileInputStream(cmdline);
                    byte[] buffer = new byte[2048];
                    int count = fis2.read(buffer);
                    if (count > 0) {
                        int i = 0;
                        while (true) {
                            if (i >= count) {
                                break;
                            } else if (buffer[i] == 0) {
                                break;
                            } else {
                                i++;
                            }
                        }
                        this.mName = new String(buffer, 0, i);
                    }
                    try {
                        fis2.close();
                    } catch (Exception e) {
                    }
                } catch (IOException e2) {
                    Log.w(OppoBinderRecorder.TAG, "Failed to read " + cmdline);
                    Log.w(OppoBinderRecorder.TAG, e2);
                    if (fis != null) {
                        fis.close();
                    }
                } catch (Throwable th) {
                    if (fis != null) {
                        try {
                            fis.close();
                        } catch (Exception e3) {
                        }
                    }
                    throw th;
                }
            }

            public final String getName() {
                return this.mName;
            }

            public void increase() {
                this.mCount++;
            }

            public int getUsage() {
                return this.mCount;
            }

            public int getToPid() {
                return this.mToPid;
            }
        }

        public void record(int pid) {
            ThreadUsageElement tu = null;
            for (int i = 0; i < this.mUsageList.size(); i++) {
                tu = this.mUsageList.get(i);
                if (tu.getToPid() == pid) {
                    break;
                }
                tu = null;
            }
            if (tu == null) {
                tu = new ThreadUsageElement(pid);
                this.mUsageList.add(tu);
            }
            tu.increase();
        }

        class UsageListComparator implements Comparator<ThreadUsageElement> {
            UsageListComparator() {
            }

            public int compare(ThreadUsageElement a, ThreadUsageElement b) {
                if (a.getUsage() < b.getUsage()) {
                    return 1;
                }
                if (a.getUsage() > b.getUsage()) {
                    return -1;
                }
                return 0;
            }
        }

        public int getLength() {
            return this.mUsageList.size();
        }

        /* access modifiers changed from: private */
        public void sort() {
            Collections.sort(this.mUsageList, new UsageListComparator());
        }

        public void print() {
            for (int i = 0; i < this.mUsageList.size(); i++) {
                ThreadUsageElement tu = this.mUsageList.get(i);
                Log.i(OppoBinderRecorder.TAG, tu.getName() + "(" + tu.getToPid() + "):" + tu.getUsage());
            }
        }

        public final String getMapString() {
            StringBuilder sb = new StringBuilder("");
            for (int i = 0; i < this.mUsageList.size(); i++) {
                ThreadUsageElement tu = this.mUsageList.get(i);
                sb.append(tu.getName());
                sb.append(SettingsStringUtil.DELIMITER);
                sb.append(Integer.toString(tu.getUsage()));
                sb.append(SmsManager.REGEX_PREFIX_DELIMITER);
            }
            return sb.toString();
        }
    }

    private OppoBinderRecorder() {
    }

    public static synchronized OppoBinderRecorder getInstance() {
        OppoBinderRecorder oppoBinderRecorder;
        synchronized (OppoBinderRecorder.class) {
            if (mInstance == null) {
                mInstance = new OppoBinderRecorder();
            }
            oppoBinderRecorder = mInstance;
        }
        return oppoBinderRecorder;
    }

    public synchronized void recordTimeUsed(Binder binder, long time) {
        if (this.mMaxTimeUsed < time) {
            this.mMaxTimeUsed = time;
            this.mMaxTimeUsedDescriptor = binder.getInterfaceDescriptor();
        }
    }

    public synchronized void uploadMaxTimeUsed() {
        Log.i(TAG, "max time used: " + this.mMaxTimeUsed + " desc: " + this.mMaxTimeUsedDescriptor);
    }

    /* JADX INFO: Multiple debug info for r12v9 long: [D('flags' long), D('p' java.util.regex.Pattern)] */
    public Map<String, String> getBinderUsageDscLogMap() {
        try {
            int pid = Process.myPid();
            int state = 0;
            BufferedReader in = new BufferedReader(new FileReader("/sys/kernel/debug/binder/state"));
            ThreadUsage inComingThreadUsage = new ThreadUsage();
            ThreadUsage outGoingThreadUsage = new ThreadUsage();
            Log.i(TAG, "Uploading binder usage for process " + pid);
            while (true) {
                String line = in.readLine();
                if (line == null || state == 3) {
                    in.close();
                    Map<String, String> logMap = new HashMap<>();
                    outGoingThreadUsage.sort();
                    Log.i(TAG, "Print outgoing thread usage for " + Process.myPid());
                    outGoingThreadUsage.print();
                    String outGoingString = outGoingThreadUsage.getMapString();
                    Log.i(TAG, "DCS mapstring is: " + outGoingString);
                    logMap.put("outGoingThreadUsage", outGoingString);
                    inComingThreadUsage.sort();
                    Log.i(TAG, "Print incoming thread usage for " + Process.myPid());
                    inComingThreadUsage.print();
                    String inComingString = inComingThreadUsage.getMapString();
                    Log.i(TAG, "DCS mapstring is: " + inComingString);
                    logMap.put("inComingThreadUsage", inComingString);
                } else if (state == 0) {
                    if (line.equals("proc " + pid)) {
                        state = 1;
                    }
                } else if (state != 1) {
                    if (state == 2) {
                        if (line.startsWith("    outgoing")) {
                            Log.i(TAG, "found transaction: " + line);
                            Matcher m = Pattern.compile("^    (outgoing|incoming).*from (\\d+):(\\d+) to (\\d+):(\\d+).*flags (\\d+).*").matcher(line);
                            if (m.find()) {
                                String direction = m.group(1);
                                int from = Integer.parseInt(m.group(2));
                                int to = Integer.parseInt(m.group(4));
                                if ((((long) Integer.parseInt(m.group(6))) & 1) == 0) {
                                    if (direction.equals("outgoing")) {
                                        outGoingThreadUsage.record(to);
                                    } else if (direction.equals("incoming")) {
                                        inComingThreadUsage.record(from);
                                    }
                                }
                            }
                        } else if (line.startsWith("proc")) {
                            state = 3;
                        }
                    }
                } else if (line.equals("context binder")) {
                    state = 2;
                } else {
                    state = 0;
                }
            }
            in.close();
            Map<String, String> logMap2 = new HashMap<>();
            outGoingThreadUsage.sort();
            Log.i(TAG, "Print outgoing thread usage for " + Process.myPid());
            outGoingThreadUsage.print();
            String outGoingString2 = outGoingThreadUsage.getMapString();
            Log.i(TAG, "DCS mapstring is: " + outGoingString2);
            logMap2.put("outGoingThreadUsage", outGoingString2);
            inComingThreadUsage.sort();
            Log.i(TAG, "Print incoming thread usage for " + Process.myPid());
            inComingThreadUsage.print();
            String inComingString2 = inComingThreadUsage.getMapString();
            Log.i(TAG, "DCS mapstring is: " + inComingString2);
            logMap2.put("inComingThreadUsage", inComingString2);
            return logMap2;
        } catch (IOException e) {
            Log.w(TAG, "Failed to read binder state");
            return null;
        }
    }
}
