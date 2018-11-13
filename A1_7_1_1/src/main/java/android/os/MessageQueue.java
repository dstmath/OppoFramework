package android.os;

import android.util.Log;
import android.util.Printer;
import android.util.SparseArray;
import java.io.FileDescriptor;
import java.util.ArrayList;

public final class MessageQueue {
    private static final boolean DEBUG = false;
    private static final int DUMP_MESSAGE_MAX = 10;
    private static final int MESSAGE_DUMP_SIZE_MAX = 20;
    private static final String TAG = "MessageQueue";
    private boolean mBlocked;
    private SparseArray<FileDescriptorRecord> mFileDescriptorRecords;
    private final ArrayList<IdleHandler> mIdleHandlers = new ArrayList();
    Message mMessages;
    private int mNextBarrierToken;
    private IdleHandler[] mPendingIdleHandlers;
    private long mPtr;
    private final boolean mQuitAllowed;
    private boolean mQuitting;

    public interface IdleHandler {
        boolean queueIdle();
    }

    private static final class FileDescriptorRecord {
        public final FileDescriptor mDescriptor;
        public int mEvents;
        public OnFileDescriptorEventListener mListener;
        public int mSeq;

        public FileDescriptorRecord(FileDescriptor descriptor, int events, OnFileDescriptorEventListener listener) {
            this.mDescriptor = descriptor;
            this.mEvents = events;
            this.mListener = listener;
        }
    }

    public interface OnFileDescriptorEventListener {
        public static final int EVENT_ERROR = 4;
        public static final int EVENT_INPUT = 1;
        public static final int EVENT_OUTPUT = 2;

        int onFileDescriptorEvents(FileDescriptor fileDescriptor, int i);
    }

    private static native void nativeDestroy(long j);

    private static native long nativeInit();

    private static native boolean nativeIsPolling(long j);

    private native void nativePollOnce(long j, int i);

    private static native void nativeSetFileDescriptorEvents(long j, int i, int i2);

    private static native void nativeWake(long j);

    MessageQueue(boolean quitAllowed) {
        this.mQuitAllowed = quitAllowed;
        this.mPtr = nativeInit();
    }

    protected void finalize() throws Throwable {
        try {
            dispose();
        } finally {
            super.finalize();
        }
    }

    private void dispose() {
        if (this.mPtr != 0) {
            nativeDestroy(this.mPtr);
            this.mPtr = 0;
        }
    }

    public boolean isIdle() {
        boolean z = true;
        synchronized (this) {
            long now = SystemClock.uptimeMillis();
            if (this.mMessages != null && now >= this.mMessages.when) {
                z = false;
            }
        }
        return z;
    }

    public void addIdleHandler(IdleHandler handler) {
        if (handler == null) {
            throw new NullPointerException("Can't add a null IdleHandler");
        }
        synchronized (this) {
            this.mIdleHandlers.add(handler);
        }
    }

    public void removeIdleHandler(IdleHandler handler) {
        synchronized (this) {
            this.mIdleHandlers.remove(handler);
        }
    }

    public boolean isPolling() {
        boolean isPollingLocked;
        synchronized (this) {
            isPollingLocked = isPollingLocked();
        }
        return isPollingLocked;
    }

    private boolean isPollingLocked() {
        return !this.mQuitting ? nativeIsPolling(this.mPtr) : false;
    }

    public void addOnFileDescriptorEventListener(FileDescriptor fd, int events, OnFileDescriptorEventListener listener) {
        if (fd == null) {
            throw new IllegalArgumentException("fd must not be null");
        } else if (listener == null) {
            throw new IllegalArgumentException("listener must not be null");
        } else {
            synchronized (this) {
                updateOnFileDescriptorEventListenerLocked(fd, events, listener);
            }
        }
    }

    public void removeOnFileDescriptorEventListener(FileDescriptor fd) {
        if (fd == null) {
            throw new IllegalArgumentException("fd must not be null");
        }
        synchronized (this) {
            updateOnFileDescriptorEventListenerLocked(fd, 0, null);
        }
    }

    private void updateOnFileDescriptorEventListenerLocked(FileDescriptor fd, int events, OnFileDescriptorEventListener listener) {
        int fdNum = fd.getInt$();
        int index = -1;
        FileDescriptorRecord record = null;
        if (this.mFileDescriptorRecords != null) {
            index = this.mFileDescriptorRecords.indexOfKey(fdNum);
            if (index >= 0) {
                record = (FileDescriptorRecord) this.mFileDescriptorRecords.valueAt(index);
                if (record != null && record.mEvents == events) {
                    return;
                }
            }
        }
        if (events != 0) {
            events |= 4;
            if (record == null) {
                if (this.mFileDescriptorRecords == null) {
                    this.mFileDescriptorRecords = new SparseArray();
                }
                this.mFileDescriptorRecords.put(fdNum, new FileDescriptorRecord(fd, events, listener));
            } else {
                record.mListener = listener;
                record.mEvents = events;
                record.mSeq++;
            }
            nativeSetFileDescriptorEvents(this.mPtr, fdNum, events);
        } else if (record != null) {
            record.mEvents = 0;
            this.mFileDescriptorRecords.removeAt(index);
        }
    }

    /* JADX WARNING: Missing block: B:16:0x001a, code:
            r2 = r1.onFileDescriptorEvents(r4.mDescriptor, r10);
     */
    /* JADX WARNING: Missing block: B:17:0x0020, code:
            if (r2 == 0) goto L_0x0024;
     */
    /* JADX WARNING: Missing block: B:18:0x0022, code:
            r2 = r2 | 4;
     */
    /* JADX WARNING: Missing block: B:19:0x0024, code:
            if (r2 == r3) goto L_0x0045;
     */
    /* JADX WARNING: Missing block: B:20:0x0026, code:
            monitor-enter(r8);
     */
    /* JADX WARNING: Missing block: B:22:?, code:
            r0 = r8.mFileDescriptorRecords.indexOfKey(r9);
     */
    /* JADX WARNING: Missing block: B:23:0x002d, code:
            if (r0 < 0) goto L_0x0044;
     */
    /* JADX WARNING: Missing block: B:25:0x0035, code:
            if (r8.mFileDescriptorRecords.valueAt(r0) != r4) goto L_0x0044;
     */
    /* JADX WARNING: Missing block: B:27:0x0039, code:
            if (r4.mSeq != r5) goto L_0x0044;
     */
    /* JADX WARNING: Missing block: B:28:0x003b, code:
            r4.mEvents = r2;
     */
    /* JADX WARNING: Missing block: B:29:0x003d, code:
            if (r2 != 0) goto L_0x0044;
     */
    /* JADX WARNING: Missing block: B:30:0x003f, code:
            r8.mFileDescriptorRecords.removeAt(r0);
     */
    /* JADX WARNING: Missing block: B:31:0x0044, code:
            monitor-exit(r8);
     */
    /* JADX WARNING: Missing block: B:32:0x0045, code:
            return r2;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private int dispatchEvents(int fd, int events) {
        synchronized (this) {
            FileDescriptorRecord record = (FileDescriptorRecord) this.mFileDescriptorRecords.get(fd);
            if (record == null) {
                return 0;
            }
            int oldWatchedEvents = record.mEvents;
            events &= oldWatchedEvents;
            if (events == 0) {
                return oldWatchedEvents;
            }
            OnFileDescriptorEventListener listener = record.mListener;
            int seq = record.mSeq;
        }
    }

    /* JADX WARNING: Missing block: B:30:?, code:
            r18.mBlocked = false;
     */
    /* JADX WARNING: Missing block: B:31:0x0057, code:
            if (r10 == null) goto L_0x0065;
     */
    /* JADX WARNING: Missing block: B:32:0x0059, code:
            r10.next = r5.next;
     */
    /* JADX WARNING: Missing block: B:33:0x005d, code:
            r5.next = null;
            r5.markInUse();
     */
    /* JADX WARNING: Missing block: B:35:0x0064, code:
            return r5;
     */
    /* JADX WARNING: Missing block: B:37:?, code:
            r18.mMessages = r5.next;
     */
    /* JADX WARNING: Missing block: B:59:0x00b9, code:
            r2 = 0;
     */
    /* JADX WARNING: Missing block: B:60:0x00ba, code:
            if (r2 >= r7) goto L_0x00ea;
     */
    /* JADX WARNING: Missing block: B:61:0x00bc, code:
            r3 = r18.mPendingIdleHandlers[r2];
            r18.mPendingIdleHandlers[r2] = null;
            r4 = false;
     */
    /* JADX WARNING: Missing block: B:63:?, code:
            r4 = r3.queueIdle();
     */
    /* JADX WARNING: Missing block: B:70:0x00dc, code:
            r11 = move-exception;
     */
    /* JADX WARNING: Missing block: B:71:0x00dd, code:
            android.util.Log.wtf(TAG, "IdleHandler threw exception", r11);
     */
    /* JADX WARNING: Missing block: B:75:0x00ea, code:
            r7 = 0;
            r6 = 0;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    Message next() {
        long ptr = this.mPtr;
        if (ptr == 0) {
            return null;
        }
        int pendingIdleHandlerCount = -1;
        int nextPollTimeoutMillis = 0;
        while (true) {
            if (nextPollTimeoutMillis != 0) {
                Binder.flushPendingCommands();
            }
            nativePollOnce(ptr, nextPollTimeoutMillis);
            synchronized (this) {
                long now = SystemClock.uptimeMillis();
                Message prevMsg = null;
                Message msg = this.mMessages;
                if (msg != null && msg.target == null) {
                    do {
                        prevMsg = msg;
                        msg = msg.next;
                        if (msg == null) {
                            break;
                        }
                    } while (!msg.isAsynchronous());
                }
                if (msg != null) {
                    if (now >= msg.when) {
                        break;
                    }
                    nextPollTimeoutMillis = (int) Math.min(msg.when - now, 2147483647L);
                } else {
                    nextPollTimeoutMillis = -1;
                }
                if (this.mQuitting) {
                    dispose();
                    return null;
                }
                if (pendingIdleHandlerCount < 0) {
                    if (this.mMessages == null || now < this.mMessages.when) {
                        pendingIdleHandlerCount = this.mIdleHandlers.size();
                    }
                }
                if (pendingIdleHandlerCount <= 0) {
                    this.mBlocked = true;
                } else {
                    if (this.mPendingIdleHandlers == null) {
                        this.mPendingIdleHandlers = new IdleHandler[Math.max(pendingIdleHandlerCount, 4)];
                    }
                    this.mPendingIdleHandlers = (IdleHandler[]) this.mIdleHandlers.toArray(this.mPendingIdleHandlers);
                }
            }
        }
        if (!keep) {
            synchronized (this) {
                this.mIdleHandlers.remove(idler);
            }
        }
        int i++;
    }

    void quit(boolean safe) {
        if (this.mQuitAllowed) {
            synchronized (this) {
                if (this.mQuitting) {
                    return;
                }
                this.mQuitting = true;
                if (safe) {
                    removeAllFutureMessagesLocked();
                } else {
                    removeAllMessagesLocked();
                }
                nativeWake(this.mPtr);
                return;
            }
        }
        throw new IllegalStateException("Main thread not allowed to quit.");
    }

    public int postSyncBarrier() {
        return postSyncBarrier(SystemClock.uptimeMillis());
    }

    private int postSyncBarrier(long when) {
        int token;
        synchronized (this) {
            token = this.mNextBarrierToken;
            this.mNextBarrierToken = token + 1;
            Message msg = Message.obtain();
            msg.markInUse();
            msg.when = when;
            msg.arg1 = token;
            Message prev = null;
            Message p = this.mMessages;
            if (when != 0) {
                while (p != null && p.when <= when) {
                    prev = p;
                    p = p.next;
                }
            }
            if (prev != null) {
                msg.next = p;
                prev.next = msg;
            } else {
                msg.next = p;
                this.mMessages = msg;
            }
        }
        return token;
    }

    public void removeSyncBarrier(int token) {
        synchronized (this) {
            Message prev = null;
            Message p = this.mMessages;
            while (p != null && (p.target != null || p.arg1 != token)) {
                prev = p;
                p = p.next;
            }
            if (p == null) {
                throw new IllegalStateException("The specified message queue synchronization  barrier token has not been posted or has already been removed.");
            }
            boolean needWake;
            if (prev != null) {
                prev.next = p.next;
                needWake = false;
            } else {
                this.mMessages = p.next;
                needWake = this.mMessages == null || this.mMessages.target != null;
            }
            p.recycleUnchecked();
            if (needWake && !this.mQuitting) {
                nativeWake(this.mPtr);
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:26:0x0093  */
    /* JADX WARNING: Missing block: B:29:0x009a, code:
            return true;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    boolean enqueueMessage(Message msg, long when) {
        if (msg.target == null) {
            throw new IllegalArgumentException("Message must have a target.");
        } else if (msg.isInUse()) {
            throw new IllegalStateException(msg + " This message is already in use.");
        } else if (msg.hasRecycle) {
            Log.wtf(TAG, "Warning: message has been recycled. msg=" + msg);
            return false;
        } else {
            synchronized (this) {
                if (this.mQuitting) {
                    IllegalStateException e = new IllegalStateException(msg.target + " sending message to a Handler on a dead thread");
                    Log.w(TAG, e.getMessage(), e);
                    msg.recycle();
                    return false;
                }
                boolean needWake;
                msg.markInUse();
                msg.when = when;
                Message p = this.mMessages;
                if (!(p == null || when == 0)) {
                    if (when >= p.when) {
                        Message prev;
                        needWake = (this.mBlocked && p.target == null) ? msg.isAsynchronous() : false;
                        while (true) {
                            prev = p;
                            p = p.next;
                            if (p == null || when < p.when) {
                                msg.next = p;
                                prev.next = msg;
                            } else if (needWake) {
                                if (p.isAsynchronous()) {
                                    needWake = false;
                                }
                            }
                        }
                        msg.next = p;
                        prev.next = msg;
                        if (needWake) {
                            nativeWake(this.mPtr);
                        }
                    }
                }
                msg.next = p;
                this.mMessages = msg;
                needWake = this.mBlocked;
                if (needWake) {
                }
            }
        }
    }

    boolean hasMessages(Handler h, int what, Object object) {
        if (h == null) {
            return false;
        }
        synchronized (this) {
            Message p = this.mMessages;
            while (p != null) {
                if (p.target == h && p.what == what && (object == null || p.obj == object)) {
                    return true;
                }
                p = p.next;
            }
            return false;
        }
    }

    boolean hasMessages(Handler h, Runnable r, Object object) {
        if (h == null) {
            return false;
        }
        synchronized (this) {
            Message p = this.mMessages;
            while (p != null) {
                if (p.target == h && p.callback == r && (object == null || p.obj == object)) {
                    return true;
                }
                p = p.next;
            }
            return false;
        }
    }

    void removeMessages(Handler h, int what, Object object) {
        if (h != null) {
            synchronized (this) {
                Message n;
                Message p = this.mMessages;
                while (p != null && p.target == h && p.what == what && (object == null || p.obj == object)) {
                    n = p.next;
                    this.mMessages = n;
                    p.recycleUnchecked();
                    p = n;
                }
                while (p != null) {
                    n = p.next;
                    if (n != null && n.target == h && n.what == what && (object == null || n.obj == object)) {
                        Message nn = n.next;
                        n.recycleUnchecked();
                        p.next = nn;
                    } else {
                        p = n;
                    }
                }
            }
        }
    }

    void removeMessages(Handler h, Runnable r, Object object) {
        if (h != null && r != null) {
            synchronized (this) {
                Message n;
                Message p = this.mMessages;
                while (p != null && p.target == h && p.callback == r && (object == null || p.obj == object)) {
                    n = p.next;
                    this.mMessages = n;
                    p.recycleUnchecked();
                    p = n;
                }
                while (p != null) {
                    n = p.next;
                    if (n != null && n.target == h && n.callback == r && (object == null || n.obj == object)) {
                        Message nn = n.next;
                        n.recycleUnchecked();
                        p.next = nn;
                    } else {
                        p = n;
                    }
                }
            }
        }
    }

    void removeCallbacksAndMessages(Handler h, Object object) {
        if (h != null) {
            synchronized (this) {
                Message n;
                Message p = this.mMessages;
                while (p != null && p.target == h && (object == null || p.obj == object)) {
                    n = p.next;
                    this.mMessages = n;
                    p.recycleUnchecked();
                    p = n;
                }
                while (p != null) {
                    n = p.next;
                    if (n != null && n.target == h && (object == null || n.obj == object)) {
                        Message nn = n.next;
                        n.recycleUnchecked();
                        p.next = nn;
                    } else {
                        p = n;
                    }
                }
            }
        }
    }

    private void removeAllMessagesLocked() {
        Message p = this.mMessages;
        while (p != null) {
            Message n = p.next;
            p.recycleUnchecked();
            p = n;
        }
        this.mMessages = null;
    }

    private void removeAllFutureMessagesLocked() {
        long now = SystemClock.uptimeMillis();
        Message p = this.mMessages;
        if (p != null) {
            if (p.when > now) {
                removeAllMessagesLocked();
            } else {
                while (true) {
                    Message n = p.next;
                    if (n != null) {
                        if (n.when > now) {
                            p.next = null;
                            while (true) {
                                p = n;
                                n = n.next;
                                p.recycleUnchecked();
                                if (n == null) {
                                    break;
                                }
                            }
                        } else {
                            p = n;
                        }
                    } else {
                        return;
                    }
                }
            }
        }
    }

    void dump(Printer pw, String prefix) {
        synchronized (this) {
            long now = SystemClock.uptimeMillis();
            int n = 0;
            for (Message msg = this.mMessages; msg != null; msg = msg.next) {
                pw.println(prefix + "Message " + n + ": " + msg.toString(now));
                n++;
            }
            pw.println(prefix + "(Total messages: " + n + ", polling=" + isPollingLocked() + ", quitting=" + this.mQuitting + ")");
        }
    }

    public final synchronized String dumpMessageQueue() {
        String messageQueue;
        messageQueue = "";
        if (this.mMessages != null) {
            Log.d(TAG, "Dump first 20 messages in Queue: ");
            messageQueue = messageQueue + "Dump first " + 20 + " messages in Queue: \n";
            int count = 0;
            for (Message tempMsg = this.mMessages; tempMsg != null; tempMsg = tempMsg.next) {
                count++;
                if (count <= 20) {
                    Log.d(TAG, "Dump Message in Queue (" + count + "): " + tempMsg);
                    messageQueue = messageQueue + "Dump Message in Queue (" + count + "): " + tempMsg + "\n";
                }
            }
            Log.d(TAG, "Total Message Count: " + count);
            messageQueue = messageQueue + "Total Message Count: " + count + "\n";
        } else {
            messageQueue = messageQueue + "mMessages is null" + "\n";
            Log.d(TAG, "mMessages is null");
        }
        return messageQueue;
    }

    public final void dumpMessage() {
        synchronized (this) {
            if (this.mMessages != null) {
                long time = SystemClock.uptimeMillis();
                Log.e(TAG, "Dump messages in Queue: ");
                int count = 0;
                for (Message tempMsg = this.mMessages; tempMsg != null; tempMsg = tempMsg.next) {
                    count++;
                    if (count > 10) {
                        break;
                    }
                    Log.e(TAG, "Current msg <" + count + "> " + " = " + tempMsg.toStringLite(time, false));
                }
            } else {
                Log.d(TAG, "mMessages is null");
            }
        }
    }
}
