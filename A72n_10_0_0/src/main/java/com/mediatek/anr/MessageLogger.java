package com.mediatek.anr;

import android.os.Looper;
import android.os.Message;
import android.os.MessageQueue;
import android.os.Process;
import android.os.RemoteException;
import android.os.SystemClock;
import android.util.Log;
import android.util.Printer;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MessageLogger implements Printer {
    static final int LONGER_TIME = 200;
    static final int LONGER_TIME_MESSAGE_COUNT = 20;
    static final int MESSAGE_COUNT = 20;
    private static final int MESSAGE_DUMP_SIZE_MAX = 20;
    private static final String TAG = "MessageLogger";
    public static boolean mEnableLooperLog = false;
    private static Method sGetCurrentTimeMicro = getSystemClockMethod("currentTimeMicro");
    private String MSL_Warn = "MSL Waraning:";
    private Method mGetMessageQueue = getLooperMethod("getQueue");
    private String mLastRecord = null;
    private long mLastRecordDateTime;
    private long mLastRecordKernelTime;
    private CircularMessageInfoArray mLongTimeMessageHistory;
    private Field mMessageField = getMessageField("next");
    private CircularMessageInfoArray mMessageHistory;
    private Field mMessageQueueField = getMessageQueueField("mMessages");
    private long mMsgCnt = 0;
    private String mName = null;
    private long mNonSleepLastRecordKernelTime;
    private long mProcessId;
    private int mState = 0;
    private StringBuilder messageInfo;
    public long nonSleepWallStart;
    public long nonSleepWallTime;
    private String sInstNotCreated = (this.MSL_Warn + "!!! MessageLoggerInstance might not be created !!!\n");
    public long wallStart;
    public long wallTime;

    private static Method getSystemClockMethod(String func) {
        try {
            return Class.forName("android.os.SystemClock").getDeclaredMethod(func, new Class[0]);
        } catch (Exception e) {
            return null;
        }
    }

    private Method getLooperMethod(String func) {
        try {
            return Class.forName("android.os.Looper").getDeclaredMethod(func, new Class[0]);
        } catch (Exception e) {
            return null;
        }
    }

    private Field getMessageQueueField(String var) {
        try {
            Field field = Class.forName("android.os.MessageQueue").getDeclaredField(var);
            field.setAccessible(true);
            return field;
        } catch (Exception e) {
            return null;
        }
    }

    private Field getMessageField(String var) {
        try {
            Field field = Class.forName("android.os.Message").getDeclaredField(var);
            field.setAccessible(true);
            return field;
        } catch (Exception e) {
            return null;
        }
    }

    public MessageLogger() {
        init();
    }

    public MessageLogger(boolean mValue) {
        mEnableLooperLog = mValue;
        init();
    }

    public MessageLogger(boolean mValue, String Name) {
        this.mName = Name;
        mEnableLooperLog = mValue;
        init();
    }

    private void init() {
        this.mMessageHistory = new CircularMessageInfoArray(20);
        this.mLongTimeMessageHistory = new CircularMessageInfoArray(20);
        this.messageInfo = new StringBuilder(20480);
        this.mProcessId = (long) Process.myPid();
    }

    public void println(String s) {
        synchronized (this) {
            this.mState++;
            this.mMsgCnt++;
            this.mLastRecordKernelTime = SystemClock.elapsedRealtime();
            this.mNonSleepLastRecordKernelTime = SystemClock.uptimeMillis();
            try {
                if (sGetCurrentTimeMicro != null) {
                    this.mLastRecordDateTime = ((Long) sGetCurrentTimeMicro.invoke(null, new Object[0])).longValue();
                }
            } catch (Exception e) {
            }
            if (this.mState == 1) {
                MessageInfo msgInfo = this.mMessageHistory.add();
                msgInfo.init();
                msgInfo.startDispatch = s;
                msgInfo.msgIdStart = this.mMsgCnt;
                msgInfo.startTimeElapsed = this.mLastRecordDateTime;
                msgInfo.startTimeUp = this.mNonSleepLastRecordKernelTime;
            } else {
                this.mState = 0;
                MessageInfo msgInfo2 = this.mMessageHistory.getLast();
                msgInfo2.finishDispatch = s;
                msgInfo2.msgIdFinish = this.mMsgCnt;
                msgInfo2.durationElapsed = this.mLastRecordDateTime - msgInfo2.startTimeElapsed;
                msgInfo2.durationUp = this.mNonSleepLastRecordKernelTime - msgInfo2.startTimeUp;
                this.wallTime = msgInfo2.durationElapsed;
                if (msgInfo2.durationElapsed >= 200000) {
                    this.mLongTimeMessageHistory.add().copy(msgInfo2);
                }
            }
            if (mEnableLooperLog) {
                if (this.mState == 1) {
                    Log.d(TAG, "Debugging_MessageLogger: " + s + " start");
                } else {
                    Log.d(TAG, "Debugging_MessageLogger: " + s + " spent " + (this.wallTime / 1000) + "ms");
                }
            }
        }
    }

    public void setInitStr(String str_tmp) {
        StringBuilder sb = this.messageInfo;
        sb.delete(0, sb.length());
        this.messageInfo.append(str_tmp);
    }

    private void log(String info) {
        StringBuilder sb = this.messageInfo;
        sb.append(info);
        sb.append("\n");
    }

    public void dumpMessageQueue() {
        try {
            Looper looper = Looper.getMainLooper();
            if (looper == null) {
                log(this.MSL_Warn + "!!! Current MainLooper is Null !!!");
            } else {
                MessageQueue messageQueue = (MessageQueue) this.mGetMessageQueue.invoke(looper, new Object[0]);
                if (messageQueue == null) {
                    log(this.MSL_Warn + "!!! Current MainLooper's MsgQueue is Null !!!");
                } else {
                    dumpMessageQueueImpl(messageQueue);
                }
            }
        } catch (Exception e) {
        }
        log(String.format(this.MSL_Warn + "!!! Calling thread from PID:%d's TID:%d(%s),Thread's type is %s!!!", Integer.valueOf(Process.myPid()), Long.valueOf(Thread.currentThread().getId()), Thread.currentThread().getName(), Thread.currentThread().getClass().getName()));
        StackTraceElement[] stkTrace = Thread.currentThread().getStackTrace();
        log(String.format(this.MSL_Warn + "!!! get StackTrace: !!!", new Object[0]));
        for (int index = 0; index < stkTrace.length; index++) {
            log(String.format(this.MSL_Warn + "File:%s's Linenumber:%d, Class:%s, Method:%s", stkTrace[index].getFileName(), Integer.valueOf(stkTrace[index].getLineNumber()), stkTrace[index].getClassName(), stkTrace[index].getMethodName()));
        }
    }

    public void dumpMessageQueueImpl(MessageQueue messageQueue) throws Exception {
        synchronized (messageQueue) {
            Message mMessages = null;
            if (this.mMessageQueueField != null) {
                mMessages = (Message) this.mMessageQueueField.get(messageQueue);
            }
            if (mMessages != null) {
                log("Dump first 20 messages in Queue: ");
                Message message = mMessages;
                int count = 0;
                while (message != null) {
                    count++;
                    if (count <= 20) {
                        log("Dump Message in Queue (" + count + "): " + message);
                    }
                    message = (Message) this.mMessageField.get(message);
                }
                log("Total Message Count: " + count);
            } else {
                log("mMessages is null");
            }
        }
    }

    public void dumpMessageHistory() {
        synchronized (this) {
            log(">>> Entering MessageLogger.dump. to Dump MSG HISTORY <<<");
            if (this.mMessageHistory != null) {
                if (this.mMessageHistory.size() != 0) {
                    log("MSG HISTORY IN MAIN THREAD:");
                    log("Current kernel time : " + SystemClock.uptimeMillis() + "ms PID=" + this.mProcessId);
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
                    int msgIdx = this.mMessageHistory.size() - 1;
                    if (this.mState == 1) {
                        Date date = new Date(this.mLastRecordDateTime / 1000);
                        long spent = SystemClock.elapsedRealtime() - this.mLastRecordKernelTime;
                        long nonSleepSpent = SystemClock.uptimeMillis() - this.mNonSleepLastRecordKernelTime;
                        MessageInfo msgInfo = this.mMessageHistory.getLast();
                        log("Last record : Msg#:" + msgInfo.msgIdStart + " " + msgInfo.startDispatch);
                        log("Last record dispatching elapsedTime:" + spent + " ms/upTime:" + nonSleepSpent + " ms");
                        StringBuilder sb = new StringBuilder();
                        sb.append("Last record dispatching time : ");
                        sb.append(simpleDateFormat.format(date));
                        log(sb.toString());
                        msgIdx += -1;
                    }
                    while (msgIdx >= 0) {
                        MessageInfo info = this.mMessageHistory.get(msgIdx);
                        Date date2 = new Date(info.startTimeElapsed / 1000);
                        log("Msg#:" + info.msgIdFinish + " " + info.finishDispatch + " elapsedTime:" + (info.durationElapsed / 1000) + " ms/upTime:" + info.durationUp + " ms");
                        StringBuilder sb2 = new StringBuilder();
                        sb2.append("Msg#:");
                        sb2.append(info.msgIdStart);
                        sb2.append(" ");
                        sb2.append(info.startDispatch);
                        sb2.append(" from ");
                        sb2.append(simpleDateFormat.format(date2));
                        log(sb2.toString());
                        msgIdx += -1;
                    }
                    log("=== Finish Dumping MSG HISTORY===");
                    log("=== LONGER MSG HISTORY IN MAIN THREAD ===");
                    for (int msgIdx2 = this.mLongTimeMessageHistory.size() - 1; msgIdx2 >= 0; msgIdx2 += -1) {
                        MessageInfo info2 = this.mLongTimeMessageHistory.get(msgIdx2);
                        Date date3 = new Date(info2.startTimeElapsed / 1000);
                        log("Msg#:" + info2.msgIdStart + " " + info2.startDispatch + " from " + simpleDateFormat.format(date3) + " elapsedTime:" + (info2.durationElapsed / 1000) + " ms/upTime:" + info2.durationUp + "ms");
                    }
                    log("=== Finish Dumping LONGER MSG HISTORY===");
                    try {
                        dumpMessageQueue();
                        AnrManagerNative.getDefault().informMessageDump(new String(this.messageInfo.toString()), Process.myPid());
                        this.messageInfo.delete(0, this.messageInfo.length());
                    } catch (RemoteException ex) {
                        Log.d(TAG, "informMessageDump exception " + ex);
                    }
                    return;
                }
            }
            log(this.sInstNotCreated);
            dumpMessageQueue();
            try {
                AnrManagerNative.getDefault().informMessageDump(this.messageInfo.toString(), Process.myPid());
            } catch (RemoteException ex2) {
                Log.d(TAG, "informMessageDump exception " + ex2);
            }
        }
    }

    public class MessageInfo {
        public long durationElapsed;
        public long durationUp;
        public String finishDispatch;
        public long msgIdFinish;
        public long msgIdStart;
        public String startDispatch;
        public long startTimeElapsed;
        public long startTimeUp;

        public MessageInfo() {
            init();
        }

        public void init() {
            this.startDispatch = null;
            this.finishDispatch = null;
            this.msgIdStart = -1;
            this.msgIdFinish = -1;
            this.startTimeUp = 0;
            this.durationUp = -1;
            this.startTimeElapsed = 0;
            this.durationElapsed = -1;
        }

        public void copy(MessageInfo info) {
            this.startDispatch = info.startDispatch;
            this.finishDispatch = info.finishDispatch;
            this.msgIdStart = info.msgIdStart;
            this.msgIdFinish = info.msgIdFinish;
            this.startTimeUp = info.startTimeUp;
            this.durationUp = info.durationUp;
            this.startTimeElapsed = info.startTimeElapsed;
            this.durationElapsed = info.durationElapsed;
        }
    }

    public class CircularMessageInfoArray {
        private MessageInfo[] mElem;
        private int mHead;
        private MessageInfo mLastElem;
        private int mSize;
        private int mTail;

        public CircularMessageInfoArray(int size) {
            int capacity = size + 1;
            this.mElem = new MessageInfo[capacity];
            for (int i = 0; i < capacity; i++) {
                this.mElem[i] = new MessageInfo();
            }
            this.mHead = 0;
            this.mTail = 0;
            this.mLastElem = null;
            this.mSize = capacity;
        }

        public boolean empty() {
            return this.mHead == this.mTail || this.mElem == null;
        }

        public boolean full() {
            int i = this.mTail;
            int i2 = this.mHead;
            return i == i2 + -1 || i - i2 == this.mSize - 1;
        }

        public int size() {
            int i = this.mTail;
            int i2 = this.mHead;
            if (i - i2 >= 0) {
                return i - i2;
            }
            return (this.mSize + i) - i2;
        }

        private MessageInfo getLocked(int n) {
            int i = this.mHead;
            int i2 = i + n;
            int i3 = this.mSize;
            if (i2 <= i3 - 1) {
                return this.mElem[i + n];
            }
            return this.mElem[(i + n) - i3];
        }

        public synchronized MessageInfo get(int n) {
            if (n >= 0) {
                if (n < size()) {
                    return getLocked(n);
                }
            }
            return null;
        }

        public MessageInfo getLast() {
            return this.mLastElem;
        }

        public synchronized MessageInfo add() {
            if (full()) {
                this.mHead++;
                if (this.mHead == this.mSize) {
                    this.mHead = 0;
                }
            }
            this.mLastElem = this.mElem[this.mTail];
            this.mTail++;
            if (this.mTail == this.mSize) {
                this.mTail = 0;
            }
            return this.mLastElem;
        }
    }
}
