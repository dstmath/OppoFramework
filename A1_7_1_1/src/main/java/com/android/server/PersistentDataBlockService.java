package com.android.server;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.service.persistentdata.IPersistentDataBlockService.Stub;
import android.util.Slog;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import libcore.io.IoUtils;

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
public class PersistentDataBlockService extends SystemService {
    public static final int DIGEST_SIZE_BYTES = 32;
    private static final String FLASH_LOCK_LOCKED = "1";
    private static final String FLASH_LOCK_PROP = "ro.boot.flash.locked";
    private static final String FLASH_LOCK_UNLOCKED = "0";
    private static final int HEADER_SIZE = 8;
    private static final int MAX_DATA_BLOCK_SIZE = 102400;
    private static final String OEM_UNLOCK_PROP = "sys.oem_unlock_allowed";
    private static final int PARTITION_TYPE_MARKER = 428873843;
    private static final String PERSISTENT_DATA_BLOCK_PROP = "ro.frp.pst";
    private static final String TAG = null;
    private int mAllowedUid;
    private long mBlockDeviceSize;
    private final Context mContext;
    private final String mDataBlockFile;
    private boolean mIsWritable;
    private final Object mLock;
    private final IBinder mService;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.server.PersistentDataBlockService.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.server.PersistentDataBlockService.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.PersistentDataBlockService.<clinit>():void");
    }

    private native long nativeGetBlockDeviceSize(String str);

    private native int nativeWipe(String str);

    public PersistentDataBlockService(Context context) {
        super(context);
        this.mLock = new Object();
        this.mAllowedUid = -1;
        this.mIsWritable = true;
        this.mService = new Stub() {
            public int write(byte[] data) throws RemoteException {
                Slog.i(PersistentDataBlockService.TAG, "mService::write data");
                PersistentDataBlockService.this.enforceUid(Binder.getCallingUid());
                long maxBlockSize = (PersistentDataBlockService.this.getBlockDeviceSize() - 8) - 1;
                if (((long) data.length) > maxBlockSize) {
                    return (int) (-maxBlockSize);
                }
                try {
                    DataOutputStream outputStream = new DataOutputStream(new FileOutputStream(new File(PersistentDataBlockService.this.mDataBlockFile)));
                    ByteBuffer headerAndData = ByteBuffer.allocate(data.length + 8);
                    headerAndData.putInt(PersistentDataBlockService.PARTITION_TYPE_MARKER);
                    headerAndData.putInt(data.length);
                    headerAndData.put(data);
                    synchronized (PersistentDataBlockService.this.mLock) {
                        if (PersistentDataBlockService.this.mIsWritable) {
                            try {
                                outputStream.write(new byte[32], 0, 32);
                                outputStream.write(headerAndData.array());
                                outputStream.flush();
                                Slog.i(PersistentDataBlockService.TAG, "mService::write flush");
                                IoUtils.closeQuietly(outputStream);
                                if (PersistentDataBlockService.this.computeAndWriteDigestLocked()) {
                                    Slog.i(PersistentDataBlockService.TAG, "mService::write return " + data.length);
                                    int length = data.length;
                                    return length;
                                }
                                return -1;
                            } catch (IOException e) {
                                Slog.e(PersistentDataBlockService.TAG, "failed writing to the persistent data block", e);
                                IoUtils.closeQuietly(outputStream);
                                return -1;
                            } catch (Throwable th) {
                                IoUtils.closeQuietly(outputStream);
                            }
                        } else {
                            IoUtils.closeQuietly(outputStream);
                            return -1;
                        }
                    }
                } catch (FileNotFoundException e2) {
                    Slog.e(PersistentDataBlockService.TAG, "partition not available?", e2);
                    return -1;
                }
            }

            /* JADX WARNING: Missing block: B:16:?, code:
            r3.close();
     */
            /* JADX WARNING: Missing block: B:22:0x006d, code:
            android.util.Slog.e(com.android.server.PersistentDataBlockService.-get0(), "failed to close OutputStream");
     */
            /* JADX WARNING: Missing block: B:30:?, code:
            r3.close();
     */
            /* JADX WARNING: Missing block: B:33:0x00ad, code:
            android.util.Slog.e(com.android.server.PersistentDataBlockService.-get0(), "failed to close OutputStream");
     */
            /* JADX WARNING: Missing block: B:39:?, code:
            r3.close();
     */
            /* JADX WARNING: Missing block: B:42:0x00c8, code:
            android.util.Slog.e(com.android.server.PersistentDataBlockService.-get0(), "failed to close OutputStream");
     */
            /* Code decompiled incorrectly, please refer to instructions dump. */
            public byte[] read() {
                byte[] bArr;
                byte[] data;
                Slog.i(PersistentDataBlockService.TAG, "mService::read");
                PersistentDataBlockService.this.enforceUid(Binder.getCallingUid());
                if (PersistentDataBlockService.this.enforceChecksumValidity()) {
                    try {
                        DataInputStream inputStream = new DataInputStream(new FileInputStream(new File(PersistentDataBlockService.this.mDataBlockFile)));
                        try {
                            synchronized (PersistentDataBlockService.this.mLock) {
                                int totalDataSize = PersistentDataBlockService.this.getTotalDataSizeLocked(inputStream);
                                if (totalDataSize == 0) {
                                    Slog.i(PersistentDataBlockService.TAG, "mService::read, totalDataSize == 0, return byte[0]");
                                    bArr = new byte[0];
                                } else {
                                    data = new byte[totalDataSize];
                                    int read = inputStream.read(data, 0, totalDataSize);
                                    if (read < totalDataSize) {
                                        Slog.e(PersistentDataBlockService.TAG, "failed to read entire data block. bytes read: " + read + "/" + totalDataSize);
                                    } else {
                                        Slog.i(PersistentDataBlockService.TAG, "mService::read out");
                                    }
                                }
                            }
                        } catch (IOException e) {
                            try {
                                Slog.e(PersistentDataBlockService.TAG, "failed to read data", e);
                                return null;
                            } finally {
                                try {
                                    inputStream.close();
                                } catch (IOException e2) {
                                    Slog.e(PersistentDataBlockService.TAG, "failed to close OutputStream");
                                }
                            }
                        }
                    } catch (FileNotFoundException e3) {
                        Slog.e(PersistentDataBlockService.TAG, "partition not available?", e3);
                        return null;
                    }
                }
                Slog.i(PersistentDataBlockService.TAG, "mService::read, enforceChecksumValidity is false, return byte[0]");
                return new byte[0];
                return data;
                return null;
                return bArr;
            }

            public void wipe() {
                Slog.i(PersistentDataBlockService.TAG, "mService::wipe");
                PersistentDataBlockService.this.enforceOemUnlockWritePermission();
                synchronized (PersistentDataBlockService.this.mLock) {
                    if (PersistentDataBlockService.this.nativeWipe(PersistentDataBlockService.this.mDataBlockFile) < 0) {
                        Slog.e(PersistentDataBlockService.TAG, "failed to wipe persistent partition");
                    } else {
                        PersistentDataBlockService.this.mIsWritable = false;
                        Slog.i(PersistentDataBlockService.TAG, "persistent partition now wiped and unwritable");
                    }
                }
            }

            public void setOemUnlockEnabled(boolean enabled) throws SecurityException {
                Slog.i(PersistentDataBlockService.TAG, "mService::setOemUnlockEnabled, enabled=" + enabled);
                if (!ActivityManager.isUserAMonkey()) {
                    PersistentDataBlockService.this.enforceOemUnlockWritePermission();
                    PersistentDataBlockService.this.enforceIsAdmin();
                    if (enabled) {
                        PersistentDataBlockService.this.enforceUserRestriction("no_oem_unlock");
                        PersistentDataBlockService.this.enforceUserRestriction("no_factory_reset");
                    }
                    synchronized (PersistentDataBlockService.this.mLock) {
                        PersistentDataBlockService.this.doSetOemUnlockEnabledLocked(enabled);
                        PersistentDataBlockService.this.computeAndWriteDigestLocked();
                    }
                }
            }

            public boolean getOemUnlockEnabled() {
                Slog.i(PersistentDataBlockService.TAG, "mService::getOemUnlockEnabled");
                PersistentDataBlockService.this.enforceOemUnlockReadPermission();
                return PersistentDataBlockService.this.doGetOemUnlockEnabled();
            }

            public int getFlashLockState() {
                PersistentDataBlockService.this.enforceOemUnlockReadPermission();
                String locked = SystemProperties.get(PersistentDataBlockService.FLASH_LOCK_PROP);
                Slog.i(PersistentDataBlockService.TAG, "getFlashLockState, ro.boot.flash.locked=" + locked);
                if (locked.equals("1")) {
                    return 1;
                }
                if (locked.equals(PersistentDataBlockService.FLASH_LOCK_UNLOCKED)) {
                    return 0;
                }
                return -1;
            }

            public int getDataBlockSize() {
                enforcePersistentDataBlockAccess();
                try {
                    DataInputStream inputStream = new DataInputStream(new FileInputStream(new File(PersistentDataBlockService.this.mDataBlockFile)));
                    try {
                        int -wrap3;
                        synchronized (PersistentDataBlockService.this.mLock) {
                            Slog.i(PersistentDataBlockService.TAG, "mService::getDataBlockSize, call getTotalDataSizeLocked");
                            -wrap3 = PersistentDataBlockService.this.getTotalDataSizeLocked(inputStream);
                        }
                        IoUtils.closeQuietly(inputStream);
                        return -wrap3;
                    } catch (IOException e) {
                        try {
                            Slog.e(PersistentDataBlockService.TAG, "error reading data block size");
                            return 0;
                        } finally {
                            IoUtils.closeQuietly(inputStream);
                        }
                    }
                } catch (FileNotFoundException e2) {
                    Slog.e(PersistentDataBlockService.TAG, "partition not available");
                    return 0;
                }
            }

            private void enforcePersistentDataBlockAccess() {
                if (PersistentDataBlockService.this.mContext.checkCallingPermission("android.permission.ACCESS_PDB_STATE") != 0) {
                    PersistentDataBlockService.this.enforceUid(Binder.getCallingUid());
                }
            }

            public long getMaximumDataBlockSize() {
                long actualSize = (PersistentDataBlockService.this.getBlockDeviceSize() - 8) - 1;
                return actualSize <= 102400 ? actualSize : 102400;
            }
        };
        Slog.i(TAG, "PersistentDataBlockService init");
        this.mContext = context;
        this.mDataBlockFile = SystemProperties.get(PERSISTENT_DATA_BLOCK_PROP);
        this.mBlockDeviceSize = -1;
        this.mAllowedUid = getAllowedUid(0);
        Slog.i(TAG, "PersistentDataBlockService, mDataBlockFile=" + this.mDataBlockFile + ", mAllowedUid=" + this.mAllowedUid);
    }

    private int getAllowedUid(int userHandle) {
        Slog.i(TAG, "getAllowedUid, userHandle=" + userHandle);
        String allowedPackage = this.mContext.getResources().getString(17039463);
        int allowedUid = -1;
        try {
            allowedUid = this.mContext.getPackageManager().getPackageUidAsUser(allowedPackage, DumpState.DUMP_DEXOPT, userHandle);
        } catch (NameNotFoundException e) {
            Slog.e(TAG, "not able to find package " + allowedPackage, e);
        }
        Slog.i(TAG, "getAllowedUid, allowedUid=" + allowedUid);
        return allowedUid;
    }

    public void onStart() {
        Slog.i(TAG, "onStart");
        enforceChecksumValidity();
        formatIfOemUnlockEnabled();
        publishBinderService("persistent_data_block", this.mService);
    }

    private void formatIfOemUnlockEnabled() {
        String str;
        Slog.i(TAG, "formatIfOemUnlockEnabled");
        boolean enabled = doGetOemUnlockEnabled();
        if (enabled) {
            synchronized (this.mLock) {
                formatPartitionLocked(true);
            }
        }
        String str2 = OEM_UNLOCK_PROP;
        if (enabled) {
            str = "1";
        } else {
            str = FLASH_LOCK_UNLOCKED;
        }
        SystemProperties.set(str2, str);
    }

    private void enforceOemUnlockReadPermission() {
        Slog.i(TAG, "enforceOemUnlockReadPermission");
        if (this.mContext.checkCallingOrSelfPermission("android.permission.READ_OEM_UNLOCK_STATE") == -1 && this.mContext.checkCallingOrSelfPermission("android.permission.OEM_UNLOCK_STATE") == -1) {
            throw new SecurityException("Can't access OEM unlock state. Requires READ_OEM_UNLOCK_STATE or OEM_UNLOCK_STATE permission.");
        }
    }

    private void enforceOemUnlockWritePermission() {
        Slog.i(TAG, "enforceOemUnlockWritePermission");
        this.mContext.enforceCallingOrSelfPermission("android.permission.OEM_UNLOCK_STATE", "Can't modify OEM unlock state");
    }

    private void enforceUid(int callingUid) {
        if (callingUid != this.mAllowedUid) {
            throw new SecurityException("uid " + callingUid + " not allowed to access PST");
        }
    }

    private void enforceIsAdmin() {
        if (!UserManager.get(this.mContext).isUserAdmin(UserHandle.getCallingUserId())) {
            throw new SecurityException("Only the Admin user is allowed to change OEM unlock state");
        }
    }

    private void enforceUserRestriction(String userRestriction) {
        if (UserManager.get(this.mContext).hasUserRestriction(userRestriction)) {
            throw new SecurityException("OEM unlock is disallowed by user restriction: " + userRestriction);
        }
    }

    private int getTotalDataSizeLocked(DataInputStream inputStream) throws IOException {
        int totalDataSize;
        Slog.i(TAG, "getTotalDataSizeLocked");
        inputStream.skipBytes(32);
        int blockId = inputStream.readInt();
        Slog.i(TAG, "getTotalDataSizeLocked, blockId=" + blockId + ", PARTITION_TYPE_MARKER=" + PARTITION_TYPE_MARKER);
        if (blockId == PARTITION_TYPE_MARKER) {
            totalDataSize = inputStream.readInt();
        } else {
            totalDataSize = 0;
        }
        Slog.i(TAG, "getTotalDataSizeLocked, totalDataSize=" + totalDataSize);
        return totalDataSize;
    }

    private long getBlockDeviceSize() {
        Slog.i(TAG, "getBlockDeviceSize");
        synchronized (this.mLock) {
            if (this.mBlockDeviceSize == -1) {
                this.mBlockDeviceSize = nativeGetBlockDeviceSize(this.mDataBlockFile);
            }
        }
        Slog.i(TAG, "getBlockDeviceSize, mBlockDeviceSize=" + this.mBlockDeviceSize);
        return this.mBlockDeviceSize;
    }

    private boolean enforceChecksumValidity() {
        byte[] storedDigest = new byte[32];
        synchronized (this.mLock) {
            byte[] digest = computeDigestLocked(storedDigest);
            if (digest == null || !Arrays.equals(storedDigest, digest)) {
                Slog.i(TAG, "Formatting FRP partition...");
                formatPartitionLocked(false);
                Slog.i(TAG, "enforceChecksumValidity, return false");
                return false;
            }
            Slog.i(TAG, "enforceChecksumValidity, return true");
            return true;
        }
    }

    private boolean computeAndWriteDigestLocked() {
        Slog.i(TAG, "computeAndWriteDigestLocked");
        byte[] digest = computeDigestLocked(null);
        if (digest == null) {
            return false;
        }
        try {
            DataOutputStream outputStream = new DataOutputStream(new FileOutputStream(new File(this.mDataBlockFile)));
            try {
                outputStream.write(digest, 0, 32);
                outputStream.flush();
                Slog.i(TAG, "computeAndWriteDigestLocked, return true");
                return true;
            } catch (IOException e) {
                Slog.e(TAG, "failed to write block checksum", e);
                return false;
            } finally {
                IoUtils.closeQuietly(outputStream);
            }
        } catch (FileNotFoundException e2) {
            Slog.e(TAG, "partition not available?", e2);
            return false;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:31:0x0080 A:{SYNTHETIC, Splitter: B:31:0x0080} */
    /* JADX WARNING: Removed duplicated region for block: B:14:0x004c A:{Catch:{ IOException -> 0x0051, all -> 0x007b }} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private byte[] computeDigestLocked(byte[] storedDigest) {
        Slog.i(TAG, "computeDigestLocked in");
        try {
            DataInputStream inputStream = new DataInputStream(new FileInputStream(new File(this.mDataBlockFile)));
            Slog.i(TAG, "computeDigestLocked, get MessageDigest isnstance");
            try {
                byte[] data;
                int read;
                MessageDigest md = MessageDigest.getInstance("SHA-256");
                if (storedDigest != null) {
                    try {
                        if (storedDigest.length == 32) {
                            inputStream.read(storedDigest);
                            Slog.i(TAG, "computeDigestLocked, start read partition");
                            data = new byte[DumpState.DUMP_PREFERRED_XML];
                            md.update(data, 0, 32);
                            while (true) {
                                read = inputStream.read(data);
                                if (read == -1) {
                                    md.update(data, 0, read);
                                } else {
                                    Slog.i(TAG, "computeDigestLocked, end read partition");
                                    IoUtils.closeQuietly(inputStream);
                                    byte[] returnValue = md.digest();
                                    Slog.i(TAG, "computeDigestLocked out");
                                    return returnValue;
                                }
                            }
                        }
                    } catch (IOException e) {
                        Slog.e(TAG, "failed to read partition", e);
                        return null;
                    } finally {
                        IoUtils.closeQuietly(inputStream);
                    }
                }
                inputStream.skipBytes(32);
                Slog.i(TAG, "computeDigestLocked, start read partition");
                data = new byte[DumpState.DUMP_PREFERRED_XML];
                md.update(data, 0, 32);
                while (true) {
                    read = inputStream.read(data);
                    if (read == -1) {
                    }
                }
            } catch (NoSuchAlgorithmException e2) {
                Slog.e(TAG, "SHA-256 not supported?", e2);
                IoUtils.closeQuietly(inputStream);
                return null;
            }
        } catch (FileNotFoundException e3) {
            Slog.e(TAG, "partition not available?", e3);
            return null;
        }
    }

    private void formatPartitionLocked(boolean setOemUnlockEnabled) {
        Slog.i(TAG, "formatPartitionLocked");
        try {
            DataOutputStream outputStream = new DataOutputStream(new FileOutputStream(new File(this.mDataBlockFile)));
            try {
                outputStream.write(new byte[32], 0, 32);
                outputStream.writeInt(PARTITION_TYPE_MARKER);
                outputStream.writeInt(0);
                outputStream.flush();
                Slog.i(TAG, "formatPartitionLocked init data, PARTITION_TYPE_MARKER=428873843, size=0 ");
                doSetOemUnlockEnabledLocked(setOemUnlockEnabled);
                computeAndWriteDigestLocked();
            } catch (IOException e) {
                Slog.e(TAG, "failed to format block", e);
            } finally {
                IoUtils.closeQuietly(outputStream);
            }
        } catch (FileNotFoundException e2) {
            Slog.e(TAG, "partition not available?", e2);
        }
    }

    private void doSetOemUnlockEnabledLocked(boolean enabled) {
        byte b = (byte) 1;
        Slog.i(TAG, "doSetOemUnlockEnabledLocked, enabled=" + enabled);
        try {
            FileOutputStream outputStream = new FileOutputStream(new File(this.mDataBlockFile));
            try {
                FileChannel channel = outputStream.getChannel();
                channel.position(getBlockDeviceSize() - 1);
                ByteBuffer data = ByteBuffer.allocate(1);
                if (!enabled) {
                    b = (byte) 0;
                }
                data.put(b);
                data.flip();
                channel.write(data);
                outputStream.flush();
                Slog.i(TAG, "doSetOemUnlockEnabledLocked out");
            } catch (IOException e) {
                Slog.e(TAG, "unable to access persistent partition", e);
            } finally {
                SystemProperties.set(OEM_UNLOCK_PROP, enabled ? "1" : FLASH_LOCK_UNLOCKED);
                IoUtils.closeQuietly(outputStream);
            }
        } catch (FileNotFoundException e2) {
            Slog.e(TAG, "partition not available", e2);
        }
    }

    private boolean doGetOemUnlockEnabled() {
        Slog.i(TAG, "doGetOemUnlockEnabled in");
        try {
            DataInputStream inputStream = new DataInputStream(new FileInputStream(new File(this.mDataBlockFile)));
            try {
                boolean returnValue;
                synchronized (this.mLock) {
                    inputStream.skip(getBlockDeviceSize() - 1);
                    returnValue = inputStream.readByte() != (byte) 0;
                    Slog.i(TAG, "doGetOemUnlockEnabled, return " + returnValue);
                }
                IoUtils.closeQuietly(inputStream);
                return returnValue;
            } catch (IOException e) {
                try {
                    Slog.e(TAG, "unable to access persistent partition", e);
                    return false;
                } finally {
                    IoUtils.closeQuietly(inputStream);
                }
            }
        } catch (FileNotFoundException e2) {
            Slog.e(TAG, "partition not available");
            return false;
        }
    }
}
