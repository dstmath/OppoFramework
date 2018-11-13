package com.android.server.locksettings;

import android.content.Context;
import android.os.Environment;
import android.os.RemoteException;
import android.os.UserManager;
import android.util.SparseArray;
import com.color.util.ColorLog;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;

public class ColorLockSettingsService extends LockSettingsService {
    private static final boolean DBG = true;
    private static final String LOCK_DATA_FILE = "meqn.dat";
    private static final String SYSTEM_DIRECTORY = "/system/";
    private static final String TAG = "ColorLockSettingsService";
    private static final int TYPE_PASSWORD_DATA = 2;
    private static final int TYPE_PASSWORD_SALT = 0;
    private static final int TYPE_PASSWORD_TYPE = 1;
    private static final int TYPE_PATTERN_DATA = 3;
    private final SparseArray<LockCache> mCaches = new SparseArray();
    private final Object mFileWriteLock = new Object();
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

    public ColorLockSettingsService(Context context) {
        super(context);
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

    public void setLockCredential(String credential, int type, String savedCredential, int requestedQuality, int userId) throws RemoteException {
        super.setLockCredential(credential, type, savedCredential, requestedQuality, userId);
        byte[] bytes;
        LockCache cache;
        if (type == 1) {
            bytes = encodeString(credential);
            cache = getCache(userId);
            if (notEquals(cache.getPattern(), bytes)) {
                cache.setPattern(bytes);
                writeFile(getLockDataFilename(userId), cache);
            }
        } else if (type == 2) {
            bytes = encodeString(credential);
            cache = getCache(userId);
            if (notEquals(cache.getPassword(), bytes)) {
                cache.setPassword(bytes);
                writeFile(getLockDataFilename(userId), cache);
            }
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

    private String getLockCredentialFilePathForUser(int userId, String basename) {
        String dataSystemDirectory = Environment.getDataDirectory().getAbsolutePath() + SYSTEM_DIRECTORY;
        if (userId == 0) {
            return dataSystemDirectory + basename;
        }
        return new File(Environment.getUserSystemDirectory(userId), basename).getAbsolutePath();
    }

    private String getLockDataFilename(int userId) {
        return getLockCredentialFilePathForUser(userId, LOCK_DATA_FILE);
    }

    private boolean notEquals(byte[] value1, byte[] value2) {
        if (value1 == null || value2 == null) {
            return true;
        }
        return Arrays.equals(value1, value2) ^ 1;
    }

    private int writeBytesValue(RandomAccessFile raf, int type, byte[] value) throws IOException {
        if (value == null) {
            return 0;
        }
        int length = (writeInt(raf, type) + 0) + writeInt(raf, value.length);
        raf.write(value);
        length += value.length;
        ColorLog.d(TAG, "writeBytesValue : type=" + type + ", len=" + value.length);
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
        Object[] objArr = new Object[3];
        objArr[0] = Integer.valueOf(type);
        objArr[1] = ", value=";
        objArr[2] = String.format("0x%x", new Object[]{Long.valueOf(value)});
        ColorLog.d(TAG, "writeLongValue : type=", objArr);
        return length;
    }

    /* JADX WARNING: Removed duplicated region for block: B:26:0x007f  */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x00a8  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void writeFile(String name, LockCache cache) {
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
                            ColorLog.e(TAG, "Error closing file " + e2);
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
                        ColorLog.e(TAG, "Error writing to file " + e2);
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
                                ColorLog.e(TAG, "Error closing file " + e22);
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
                ColorLog.e(TAG, "Error writing to file " + e22);
                if (raf != null) {
                    try {
                        raf.setLength((long) length);
                        raf.close();
                    } catch (IOException e222) {
                        ColorLog.e(TAG, "Error closing file " + e222);
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
