package com.android.server;

import android.content.Context;
import android.content.pm.UserInfo;
import android.os.Environment;
import android.os.RemoteException;
import android.os.UserManager;
import android.util.SparseArray;
import com.color.util.ColorLog;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;

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
public class ColorLockSettingsService extends LockSettingsService {
    private static final String DATA_SYSTEM_DIRECTORY = null;
    private static final String LOCK_DATA_FILE = "meqn.dat";
    private static final int TYPE_PASSWORD_DATA = 2;
    private static final int TYPE_PASSWORD_SALT = 0;
    private static final int TYPE_PASSWORD_TYPE = 1;
    private static final int TYPE_PATTERN_DATA = 3;
    private final SparseArray<LockCache> mCaches;
    private final Object mFileWriteLock;
    protected final Class<?> mTagClass;
    private final UserManager mUserManager;

    private class LockCache {
        private byte[] mPassword = null;
        private long mPasswordSalt = -1;
        private long mPasswordType = -1;
        private byte[] mPattern = null;
        private final int mUserId;

        public LockCache(int userId) {
            this.mUserId = userId;
        }

        public void setPasswordSalt(long passwordSalt) {
            this.mPasswordSalt = passwordSalt;
        }

        public long getPasswordSalt() {
            return this.mPasswordSalt;
        }

        public void setPasswordType(long passwordType) {
            this.mPasswordType = passwordType;
        }

        public long getPasswordType() {
            return this.mPasswordType;
        }

        public void setPattern(byte[] pattern) {
            this.mPattern = pattern;
        }

        public byte[] getPattern() {
            return this.mPattern;
        }

        public void setPassword(byte[] password) {
            this.mPassword = password;
        }

        public byte[] getPassword() {
            return this.mPassword;
        }

        public void clearPassword() {
            setPassword(null);
            setPattern(null);
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.server.ColorLockSettingsService.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.server.ColorLockSettingsService.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.ColorLockSettingsService.<clinit>():void");
    }

    public ColorLockSettingsService(Context context) {
        super(context);
        this.mTagClass = getClass();
        this.mFileWriteLock = new Object();
        this.mCaches = new SparseArray();
        this.mUserManager = (UserManager) context.getSystemService("user");
    }

    public void setLong(String key, long value, int userId) throws RemoteException {
        super.setLong(key, value, userId);
        boolean cacheChanged = false;
        LockCache cache = getCache(userId);
        if ("lockscreen.password_salt".equals(key) && cache.getPasswordSalt() != value) {
            cache.setPasswordSalt(value);
            cacheChanged = true;
        }
        if ("lockscreen.password_type".equals(key) && cache.getPasswordType() != value) {
            cache.setPasswordType(value);
            if (0 == value) {
                cache.clearPassword();
            }
            cacheChanged = true;
        }
        if (cacheChanged) {
            writeFile(getLockDataFilename(userId), cache);
        }
    }

    public void setLockPattern(String pattern, String savedCredential, int userId) throws RemoteException {
        super.setLockPattern(pattern, savedCredential, userId);
        byte[] bytes = encodeString(pattern);
        LockCache cache = getCache(userId);
        if (notEquals(cache.getPattern(), bytes)) {
            cache.setPattern(bytes);
            writeFile(getLockDataFilename(userId), cache);
        }
    }

    public void setLockPassword(String password, String savedCredential, int userId) throws RemoteException {
        super.setLockPassword(password, savedCredential, userId);
        byte[] bytes = encodeString(password);
        LockCache cache = getCache(userId);
        if (notEquals(cache.getPassword(), bytes)) {
            cache.setPassword(bytes);
            writeFile(getLockDataFilename(userId), cache);
        }
    }

    private byte[] encodeString(String p) {
        if (p == null) {
            return null;
        }
        int i;
        int length = p.length();
        for (i = 0; i < length; i++) {
            char c = p.charAt(i);
            if (c >= 0 && c <= 8) {
                c = (char) (c + 49);
            }
        }
        byte[] oldBytes = p.getBytes();
        length = oldBytes.length;
        byte[] newBytes = new byte[length];
        for (i = 0; i < length; i++) {
            newBytes[i] = (byte) (oldBytes[(length - 1) - i] + 2);
        }
        return newBytes;
    }

    private LockCache newCache(int userId) {
        LockCache cache = new LockCache(userId);
        this.mCaches.put(userId, cache);
        return cache;
    }

    private LockCache getCache(int userId) {
        LockCache cache = (LockCache) this.mCaches.get(userId);
        if (cache == null) {
            return newCache(userId);
        }
        return cache;
    }

    private int getUserParentOrSelfId(int userId) {
        if (userId != 0) {
            UserInfo pi = this.mUserManager.getProfileParent(userId);
            if (pi != null) {
                return pi.id;
            }
        }
        return userId;
    }

    private String getLockCredentialFilePathForUser(int userId, String basename) {
        userId = getUserParentOrSelfId(userId);
        if (userId == 0) {
            return DATA_SYSTEM_DIRECTORY + basename;
        }
        return new File(Environment.getUserSystemDirectory(userId), basename).getAbsolutePath();
    }

    private String getLockDataFilename(int userId) {
        return getLockCredentialFilePathForUser(userId, LOCK_DATA_FILE);
    }

    private boolean notEquals(byte[] value1, byte[] value2) {
        boolean z = true;
        if (value1 == null || value2 == null) {
            return true;
        }
        if (Arrays.equals(value1, value2)) {
            z = false;
        }
        return z;
    }

    private int writeBytesValue(RandomAccessFile raf, int type, byte[] value) throws IOException {
        if (value == null) {
            return 0;
        }
        int length = (writeInt(raf, type) + 0) + writeInt(raf, value.length);
        raf.write(value);
        length += value.length;
        Class cls = this.mTagClass;
        Object[] objArr = new Object[4];
        objArr[0] = "writeBytesValue : type=";
        objArr[1] = Integer.valueOf(type);
        objArr[2] = ", len=";
        objArr[3] = Integer.valueOf(value.length);
        ColorLog.d("log.key.lock_pattern.service", cls, objArr);
        return length;
    }

    private byte[] intToBytes(int value) {
        byte[] b = new byte[4];
        for (int i = 0; i < b.length; i++) {
            b[i] = Integer.valueOf(value & 255).byteValue();
            value >>= 8;
        }
        return b;
    }

    private byte[] longToBytes(long value) {
        byte[] b = new byte[8];
        for (int i = 0; i < b.length; i++) {
            b[i] = Long.valueOf(255 & value).byteValue();
            value >>= 8;
        }
        return b;
    }

    private int writeInt(RandomAccessFile raf, int type) throws IOException {
        byte[] bytes = intToBytes(type);
        raf.write(bytes);
        return bytes.length;
    }

    private int writeLongValue(RandomAccessFile raf, int type, long value) throws IOException {
        int length = writeInt(raf, type);
        byte[] bytes = longToBytes(value);
        raf.write(bytes);
        length += bytes.length;
        if (ColorLog.getDebug("log.key.lock_pattern.service")) {
            Class cls = this.mTagClass;
            Object[] objArr = new Object[4];
            objArr[0] = "writeLongValue : type=";
            objArr[1] = Integer.valueOf(type);
            objArr[2] = ", value=";
            Object[] objArr2 = new Object[1];
            objArr2[0] = Long.valueOf(value);
            objArr[3] = String.format("0x%x", objArr2);
            ColorLog.d(cls, objArr);
        }
        return length;
    }

    /* JADX WARNING: Removed duplicated region for block: B:26:0x0089  */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x00b7  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void writeFile(String name, LockCache cache) {
        Class cls;
        Object[] objArr;
        IOException e;
        Throwable th;
        synchronized (this.mFileWriteLock) {
            RandomAccessFile raf = null;
            int length = 0;
            try {
                RandomAccessFile raf2 = new RandomAccessFile(name, "rw");
                try {
                    length = ((writeLongValue(raf2, 0, cache.getPasswordSalt()) + 0) + writeLongValue(raf2, 1, cache.getPasswordType())) + writeBytesValue(raf2, 3, cache.getPattern());
                    length += writeBytesValue(raf2, 2, cache.getPassword());
                    if (raf2 != null) {
                        try {
                            raf2.setLength((long) length);
                            raf2.close();
                        } catch (IOException e2) {
                            cls = this.mTagClass;
                            objArr = new Object[1];
                            objArr[0] = "Error closing file " + e2;
                            ColorLog.e(cls, objArr);
                        } catch (Throwable th2) {
                            th = th2;
                            raf = raf2;
                        }
                    }
                    raf = raf2;
                } catch (IOException e3) {
                    e2 = e3;
                    raf = raf2;
                    try {
                        cls = this.mTagClass;
                        objArr = new Object[1];
                        objArr[0] = "Error writing to file " + e2;
                        ColorLog.e(cls, objArr);
                        if (raf != null) {
                        }
                        return;
                    } catch (Throwable th3) {
                        th = th3;
                        if (raf != null) {
                            try {
                                raf.setLength((long) length);
                                raf.close();
                            } catch (IOException e22) {
                                Class cls2 = this.mTagClass;
                                Object[] objArr2 = new Object[1];
                                objArr2[0] = "Error closing file " + e22;
                                ColorLog.e(cls2, objArr2);
                            }
                        }
                        throw th;
                    }
                } catch (Throwable th4) {
                    th = th4;
                    raf = raf2;
                    if (raf != null) {
                    }
                    throw th;
                }
            } catch (IOException e4) {
                e22 = e4;
                cls = this.mTagClass;
                objArr = new Object[1];
                objArr[0] = "Error writing to file " + e22;
                ColorLog.e(cls, objArr);
                if (raf != null) {
                    try {
                        raf.setLength((long) length);
                        raf.close();
                    } catch (IOException e222) {
                        cls = this.mTagClass;
                        objArr = new Object[1];
                        objArr[0] = "Error closing file " + e222;
                        ColorLog.e(cls, objArr);
                    } catch (Throwable th5) {
                        th = th5;
                    }
                }
                return;
            }
        }
        throw th;
    }
}
