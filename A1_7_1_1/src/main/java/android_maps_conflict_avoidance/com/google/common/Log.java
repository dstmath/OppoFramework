package android_maps_conflict_avoidance.com.google.common;

import android_maps_conflict_avoidance.com.google.common.io.PersistentStore;
import android_maps_conflict_avoidance.com.google.common.util.text.TextUtil;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;

/*  JADX ERROR: NullPointerException in pass: ReSugarCode
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
    	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ExtractFieldInit.checkStaticFieldsInit(ExtractFieldInit.java:58)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class Log {
    private static final long START_TIME = 0;
    private static StringBuffer entryBuffer;
    private static boolean isEventLoggingEnabledForTest;
    private static boolean isExplicitClearForTest;
    private static long lastEventTimeMillis;
    private static final Object lastThrowableLock = null;
    private static String lastThrowableString;
    private static final Vector logEntries = null;
    private static boolean logMemory;
    private static LogSaver logSaver;
    private static boolean logThread;
    private static boolean logTime;
    private static OnScreenPrinter onScreenPrinter;
    private static Printer printer;
    private static int throwableCount;
    private static ThrowableListener throwableListener;
    private static final Hashtable timers = null;

    public interface LogSaver {
        Object uploadEventLog(boolean z, Object obj, byte[] bArr);
    }

    public interface OnScreenPrinter {
        void printToScreen(String str);
    }

    public interface Printer {
    }

    public static class StandardErrorPrinter implements Printer {
    }

    public interface ThrowableListener {
        void onThrowable(String str, Throwable th, boolean z);
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android_maps_conflict_avoidance.com.google.common.Log.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android_maps_conflict_avoidance.com.google.common.Log.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android_maps_conflict_avoidance.com.google.common.Log.<clinit>():void");
    }

    public static void logThrowable(String source, Throwable t) {
        t.printStackTrace();
        addThrowableString(source + ": " + t.toString());
        sendThrowable(source, t, false);
    }

    public static void logQuietThrowable(String source, Throwable t) {
        t.printStackTrace();
        sendThrowable(source, t, true);
    }

    public static void addThrowableString(String message) {
        if (message != null) {
            synchronized (lastThrowableLock) {
                if (lastThrowableString == null) {
                    lastThrowableString = message;
                } else {
                    lastThrowableString += "\n" + message;
                }
                if (lastThrowableString.length() > 300) {
                    lastThrowableString = lastThrowableString.substring(0, 300);
                }
            }
        }
    }

    public static boolean addEvent(short type, String status, String data) {
        long timestamp = System.currentTimeMillis();
        PersistentStore store = getPersistentStore();
        byte[] oldEvents = store.readPreference("EVENT_LOG");
        if (oldEvents == null || oldEvents.length > 600 || timestamp - lastEventTimeMillis > 6553500) {
            if (oldEvents == null) {
                resetPersistentEventLog(timestamp);
            } else if (logSaver != null) {
                uploadEventLog(false, null, timestamp);
            }
            oldEvents = store.readPreference("EVENT_LOG");
        }
        short numEvents = (short) 0;
        if (oldEvents.length > 2) {
            numEvents = (short) (((oldEvents[0] & 255) << 8) | (oldEvents[1] & 255));
        }
        numEvents = (short) (numEvents + 1);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        try {
            dos.writeShort(numEvents);
            dos.write(oldEvents, 2, oldEvents.length - 2);
            dos.writeShort(type);
            dos.writeShort((int) (Math.min(timestamp - lastEventTimeMillis, 6553500) / 100));
            dos.writeUTF(status);
            dos.writeUTF(data);
            getPersistentStore().setPreference("EVENT_LOG", baos.toByteArray());
            lastEventTimeMillis = timestamp;
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static String createEventTuple(String[] elements) {
        if (elements.length == 0) {
            return "";
        }
        StringBuffer buffer = new StringBuffer();
        buffer.append("|");
        for (int i = 0; i < elements.length; i++) {
            if (elements[i] != null) {
                StringBuffer element = new StringBuffer(elements[i]);
                TextUtil.replace("|", "", element);
                buffer.append(element);
                buffer.append("|");
            }
        }
        return buffer.toString();
    }

    public static void setLogSaver(LogSaver logSaver) {
        logSaver = logSaver;
    }

    private static Object uploadEventLog(boolean immediate, Object waitObject, long timestamp) {
        Object uploadTracker = logSaver.uploadEventLog(immediate, waitObject, getPersistentStore().readPreference("EVENT_LOG"));
        resetPersistentEventLog(timestamp);
        return uploadTracker;
    }

    private static void resetPersistentEventLog(long timestamp) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        try {
            dos.writeShort(0);
            dos.writeLong(timestamp);
            lastEventTimeMillis = timestamp;
        } catch (IOException e) {
        } finally {
            getPersistentStore().setPreference("EVENT_LOG", baos.toByteArray());
        }
    }

    private static PersistentStore getPersistentStore() {
        return Config.getInstance().getPersistentStore();
    }

    private static void sendThrowable(String source, Throwable throwable, boolean isQuiet) {
        if (throwableListener != null) {
            throwableListener.onThrowable(source, throwable, isQuiet);
        }
    }

    public static void logToScreen(String logString) {
        if (onScreenPrinter != null) {
            onScreenPrinter.printToScreen(logString);
        }
    }
}
