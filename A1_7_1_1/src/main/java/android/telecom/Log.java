package android.telecom;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.SystemProperties;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.text.format.DateFormat;
import com.android.internal.telephony.PhoneConstants;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

/*  JADX ERROR: NullPointerException in pass: ReSugarCode
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
    	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
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
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public final class Log {
    public static final boolean DEBUG = false;
    public static final boolean ERROR = false;
    public static final boolean FORCE_LOGGING = false;
    public static final boolean INFO = false;
    private static final String PROP_FORCE_DEBUG_KEY = "persist.log.tag.tel_dbg";
    private static final String TAG = "TelecomFramework";
    public static final boolean VERBOSE = false;
    public static final boolean WARN = false;
    private static MessageDigest sMessageDigest;
    private static final Object sMessageDigestLock = null;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.telecom.Log.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 9 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.telecom.Log.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.telecom.Log.<clinit>():void");
    }

    private Log() {
    }

    public static void initMd5Sum() {
        new AsyncTask<Void, Void, Void>() {
            public Void doInBackground(Void... args) {
                MessageDigest md;
                try {
                    md = MessageDigest.getInstance("SHA-1");
                } catch (NoSuchAlgorithmException e) {
                    md = null;
                }
                synchronized (Log.sMessageDigestLock) {
                    Log.sMessageDigest = md;
                }
                return null;
            }
        }.execute(new Void[0]);
    }

    public static boolean isLoggable(int level) {
        if (TextUtils.equals(Build.TYPE, "eng")) {
            return true;
        }
        boolean loggable;
        switch (level) {
            case 2:
            case 3:
                if (1 != SystemProperties.getInt(PROP_FORCE_DEBUG_KEY, 0)) {
                    loggable = false;
                    break;
                }
                loggable = true;
                break;
            default:
                loggable = true;
                break;
        }
        return loggable;
    }

    public static void d(String prefix, String format, Object... args) {
        if (DEBUG) {
            android.util.Log.d(TAG, buildMessage(prefix, format, args));
        }
    }

    public static void d(Object objectPrefix, String format, Object... args) {
        if (DEBUG) {
            android.util.Log.d(TAG, buildMessage(getPrefixFromObject(objectPrefix), format, args));
        }
    }

    public static void i(String prefix, String format, Object... args) {
        if (INFO) {
            android.util.Log.i(TAG, buildMessage(prefix, format, args));
        }
    }

    public static void i(Object objectPrefix, String format, Object... args) {
        if (INFO) {
            android.util.Log.i(TAG, buildMessage(getPrefixFromObject(objectPrefix), format, args));
        }
    }

    public static void v(String prefix, String format, Object... args) {
        if (VERBOSE) {
            android.util.Log.v(TAG, buildMessage(prefix, format, args));
        }
    }

    public static void v(Object objectPrefix, String format, Object... args) {
        if (VERBOSE) {
            android.util.Log.v(TAG, buildMessage(getPrefixFromObject(objectPrefix), format, args));
        }
    }

    public static void w(String prefix, String format, Object... args) {
        if (WARN) {
            android.util.Log.w(TAG, buildMessage(prefix, format, args));
        }
    }

    public static void w(Object objectPrefix, String format, Object... args) {
        if (WARN) {
            android.util.Log.w(TAG, buildMessage(getPrefixFromObject(objectPrefix), format, args));
        }
    }

    public static void e(String prefix, Throwable tr, String format, Object... args) {
        if (ERROR) {
            android.util.Log.e(TAG, buildMessage(prefix, format, args), tr);
        }
    }

    public static void e(Object objectPrefix, Throwable tr, String format, Object... args) {
        if (ERROR) {
            android.util.Log.e(TAG, buildMessage(getPrefixFromObject(objectPrefix), format, args), tr);
        }
    }

    public static void wtf(String prefix, Throwable tr, String format, Object... args) {
        android.util.Log.wtf(TAG, buildMessage(prefix, format, args), tr);
    }

    public static void wtf(Object objectPrefix, Throwable tr, String format, Object... args) {
        android.util.Log.wtf(TAG, buildMessage(getPrefixFromObject(objectPrefix), format, args), tr);
    }

    public static void wtf(String prefix, String format, Object... args) {
        String msg = buildMessage(prefix, format, args);
        android.util.Log.wtf(TAG, msg, new IllegalStateException(msg));
    }

    public static void wtf(Object objectPrefix, String format, Object... args) {
        String msg = buildMessage(getPrefixFromObject(objectPrefix), format, args);
        android.util.Log.wtf(TAG, msg, new IllegalStateException(msg));
    }

    public static String pii(Object pii) {
        if (pii == null || VERBOSE) {
            return String.valueOf(pii);
        }
        if (pii instanceof Uri) {
            return piiUri((Uri) pii);
        }
        return "[" + secureHash(String.valueOf(pii).getBytes()) + "]";
    }

    private static String piiUri(Uri handle) {
        StringBuilder sb = new StringBuilder();
        String scheme = handle.getScheme();
        if (!TextUtils.isEmpty(scheme)) {
            sb.append(scheme).append(":");
        }
        String value = handle.getSchemeSpecificPart();
        if (!TextUtils.isEmpty(value)) {
            for (int i = 0; i < value.length(); i++) {
                char c = value.charAt(i);
                if (PhoneNumberUtils.isStartsPostDial(c)) {
                    sb.append(c);
                } else if (PhoneNumberUtils.isDialable(c)) {
                    sb.append(PhoneConstants.APN_TYPE_ALL);
                } else if ((DateFormat.AM_PM > c || c > DateFormat.TIME_ZONE) && (DateFormat.CAPITAL_AM_PM > c || c > 'Z')) {
                    sb.append(c);
                } else {
                    sb.append(PhoneConstants.APN_TYPE_ALL);
                }
            }
        }
        return sb.toString();
    }

    private static String secureHash(byte[] input) {
        synchronized (sMessageDigestLock) {
            String encodeHex;
            if (sMessageDigest != null) {
                sMessageDigest.reset();
                sMessageDigest.update(input);
                encodeHex = encodeHex(sMessageDigest.digest());
                return encodeHex;
            }
            encodeHex = "Uninitialized SHA1";
            return encodeHex;
        }
    }

    private static String encodeHex(byte[] bytes) {
        StringBuffer hex = new StringBuffer(bytes.length * 2);
        for (byte b : bytes) {
            int byteIntValue = b & 255;
            if (byteIntValue < 16) {
                hex.append("0");
            }
            hex.append(Integer.toString(byteIntValue, 16));
        }
        return hex.toString();
    }

    private static String getPrefixFromObject(Object obj) {
        return obj == null ? "<null>" : obj.getClass().getSimpleName();
    }

    private static String buildMessage(String prefix, String format, Object... args) {
        String msg;
        Object[] objArr;
        if (args != null) {
            try {
                if (args.length != 0) {
                    msg = String.format(Locale.US, format, args);
                    objArr = new Object[2];
                    objArr[0] = prefix;
                    objArr[1] = msg;
                    return String.format(Locale.US, "%s: %s", objArr);
                }
            } catch (Throwable ife) {
                objArr = new Object[2];
                objArr[0] = format;
                objArr[1] = Integer.valueOf(args.length);
                wtf("Log", ife, "IllegalFormatException: formatString='%s' numArgs=%d", objArr);
                msg = format + " (An error occurred while formatting the message.)";
            }
        }
        msg = format;
        objArr = new Object[2];
        objArr[0] = prefix;
        objArr[1] = msg;
        return String.format(Locale.US, "%s: %s", objArr);
    }
}
