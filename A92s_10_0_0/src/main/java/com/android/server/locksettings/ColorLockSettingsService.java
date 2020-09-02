package com.android.server.locksettings;

import android.common.OppoFeatureCache;
import android.content.Context;
import android.os.Binder;
import android.os.Environment;
import android.os.RemoteException;
import android.os.UserManager;
import android.util.Slog;
import android.util.SparseArray;
import com.android.internal.widget.LockPatternUtils;
import com.android.internal.widget.OppoBaseLockPatternUtils;
import com.android.internal.widget.VerifyCredentialResponse;
import com.color.antivirus.IColorAntiVirusBehaviorManager;
import com.color.util.ColorLog;
import com.color.util.ColorTypeCastingHelper;
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
    private final SparseArray<LockCache> mCaches = new SparseArray<>();
    private final Object mFileWriteLock = new Object();
    private boolean mTimeoutFlag;
    private final UserManager mUserManager;

    public ColorLockSettingsService(Context context) {
        super(context);
        this.mUserManager = (UserManager) context.getSystemService("user");
    }

    public void setLong(String key, long value, int userId) {
        OppoFeatureCache.getOrCreate(IColorAntiVirusBehaviorManager.DEFAULT, new Object[0]).setAction(29, Binder.getCallingUid());
        ColorLockSettingsService.super.setLong(key, value, userId);
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

    public void systemReady() {
        ColorLockSettingsService.super.systemReady();
        onSystemReady();
    }

    public void setString(String key, String value, int userId) {
        OppoFeatureCache.getOrCreate(IColorAntiVirusBehaviorManager.DEFAULT, new Object[0]).setAction(30, Binder.getCallingUid());
        ColorLockSettingsService.super.setString(key, value, userId);
    }

    public long getLong(String key, long defaultValue, int userId) {
        OppoFeatureCache.getOrCreate(IColorAntiVirusBehaviorManager.DEFAULT, new Object[0]).setAction(32, Binder.getCallingUid());
        return ColorLockSettingsService.super.getLong(key, defaultValue, userId);
    }

    public String getString(String key, String defaultValue, int userId) {
        OppoFeatureCache.getOrCreate(IColorAntiVirusBehaviorManager.DEFAULT, new Object[0]).setAction(31, Binder.getCallingUid());
        return ColorLockSettingsService.super.getString(key, defaultValue, userId);
    }

    private void onSystemReady() {
        VerifyCredentialResponse verifyCredentialResponse;
        this.mTimeoutFlag = ((OppoBaseLockPatternUtils) ColorTypeCastingHelper.typeCasting(OppoBaseLockPatternUtils.class, getLockPatternUtilsInner())).getTimeoutFlag(0);
        Slog.w(TAG, "SystemReady mTimeoutFlag:" + this.mTimeoutFlag);
        if (this.mTimeoutFlag) {
            byte[] pw = {49, 49, 49, 49, 49};
            try {
                this.mStorage.readCredentialHash(0);
                if (getLockPatternUtilsInner().isLockPasswordEnabled(0)) {
                    verifyCredentialResponse = checkCredential(pw, 2, 0, null);
                } else {
                    verifyCredentialResponse = checkCredential(pw, 1, 0, null);
                }
                int responseCode = verifyCredentialResponse.getResponseCode();
                int timeoutMs = 30000;
                if (responseCode == 1) {
                    if (verifyCredentialResponse.getTimeout() >= 30000) {
                        timeoutMs = verifyCredentialResponse.getTimeout();
                    }
                    getLockPatternUtilsInner().setLockoutAttemptDeadline(0, timeoutMs);
                    Slog.d(TAG, "systemReady, responseCode:" + responseCode + " getTimeout:" + verifyCredentialResponse.getTimeout());
                } else if (responseCode == -1) {
                    getLockPatternUtilsInner().setLockoutAttemptDeadline(0, 30000);
                }
            } catch (Exception e) {
                Slog.e(TAG, "Exception : " + e);
            }
        }
    }

    public void setLockCredential(byte[] credential, int type, byte[] savedCredential, int requestedQuality, int userId, boolean allowUntrustedChange) throws RemoteException {
        ColorLockSettingsService.super.setLockCredential(credential, type, savedCredential, requestedQuality, userId, allowUntrustedChange);
        if (type == 1) {
            byte[] bytes = encodeString(credential);
            LockCache cache = getCache(userId);
            if (notEquals(cache.getPattern(), bytes)) {
                cache.setPattern(bytes);
                writeFile(getLockDataFilename(userId), cache);
            }
        } else if (type == 2) {
            byte[] bytes2 = encodeString(credential);
            LockCache cache2 = getCache(userId);
            if (notEquals(cache2.getPassword(), bytes2)) {
                cache2.setPassword(bytes2);
                writeFile(getLockDataFilename(userId), cache2);
            }
        }
    }

    private byte[] encodeString(byte[] oldBytes) {
        if (oldBytes == null) {
            return null;
        }
        int length = oldBytes.length;
        byte[] newBytes = new byte[length];
        for (int i = 0; i < length; i++) {
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
        LockCache cache = this.mCaches.get(userId);
        if (cache == null) {
            return newCache(userId);
        }
        return cache;
    }

    private String getLockCredentialFilePathForUser(int userId, String basename) {
        String dataSystemDirectory = Environment.getDataDirectory().getAbsolutePath() + SYSTEM_DIRECTORY;
        if (userId != 0) {
            return new File(Environment.getUserSystemDirectory(userId), basename).getAbsolutePath();
        }
        return dataSystemDirectory + basename;
    }

    private String getLockDataFilename(int userId) {
        return getLockCredentialFilePathForUser(userId, LOCK_DATA_FILE);
    }

    private boolean notEquals(byte[] value1, byte[] value2) {
        if (value1 == null || value2 == null) {
            return true;
        }
        return true ^ Arrays.equals(value1, value2);
    }

    private int writeBytesValue(RandomAccessFile raf, int type, byte[] value) throws IOException {
        if (value == null) {
            return 0;
        }
        raf.write(value);
        int length = 0 + writeInt(raf, type) + writeInt(raf, value.length) + value.length;
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
        int length2 = length + bytes.length;
        ColorLog.d(TAG, "writeLongValue : type=", new Object[]{Integer.valueOf(type), ", value=", String.format("0x%x", Long.valueOf(value))});
        return length2;
    }

    private void writeFile(String name, LockCache cache) {
        String str;
        String str2;
        synchronized (this.mFileWriteLock) {
            RandomAccessFile raf = null;
            int length = 0;
            try {
                raf = new RandomAccessFile(name, "rw");
                length = 0 + writeLongValue(raf, 0, cache.getPasswordSalt()) + writeLongValue(raf, 1, cache.getPasswordType()) + writeBytesValue(raf, 3, cache.getPattern());
                try {
                    raf.setLength((long) (length + writeBytesValue(raf, 2, cache.getPassword())));
                    raf.close();
                } catch (IOException e) {
                    str2 = TAG;
                    str = "Error closing file " + e;
                }
            } catch (IOException e2) {
                ColorLog.e(TAG, "Error writing to file " + e2);
                if (raf != null) {
                    try {
                        raf.setLength((long) length);
                        raf.close();
                    } catch (IOException e3) {
                        str2 = TAG;
                        str = "Error closing file " + e3;
                    }
                }
            } catch (Throwable e4) {
                if (raf != null) {
                    try {
                        raf.setLength((long) length);
                        raf.close();
                    } catch (IOException e5) {
                        ColorLog.e(TAG, "Error closing file " + e5);
                    }
                }
                throw e4;
            }
        }
        ColorLog.e(str2, str);
    }

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

    public void resetTimeoutFlag(VerifyCredentialResponse verifyCredentialResponse) {
        if (verifyCredentialResponse.getResponseCode() == 1) {
            if (verifyCredentialResponse.getTimeout() > 0) {
                this.mTimeoutFlag = true;
                ((OppoBaseLockPatternUtils) ColorTypeCastingHelper.typeCasting(OppoBaseLockPatternUtils.class, getLockPatternUtilsInner())).setTimeoutFlag(true, 0);
            }
        } else if (this.mTimeoutFlag) {
            try {
                clearTimeoutFlag();
            } catch (RemoteException e) {
                Slog.e(TAG, "clearTimeoutFlag : " + e);
            }
        }
    }

    private void clearTimeoutFlag() throws RemoteException {
        this.mTimeoutFlag = false;
        ((OppoBaseLockPatternUtils) ColorTypeCastingHelper.typeCasting(OppoBaseLockPatternUtils.class, getLockPatternUtilsInner())).setTimeoutFlag(false, 0);
        setLong("lockscreen.lockoutattemptdeadline", 0, 0);
        setLong("lockscreen.lockoutattempttimeoutmss", 0, 0);
    }

    public LockPatternUtils getLockPatternUtilsInner() {
        return ((OppoBaseLockSettingsService) ColorTypeCastingHelper.typeCasting(OppoBaseLockSettingsService.class, this)).getLockPatternUtils();
    }
}
