package com.android.server.pm;

import android.content.Context;
import android.content.pm.UserInfo;
import android.os.Environment;
import android.os.FileUtils;
import android.os.SystemProperties;
import android.os.storage.StorageManager;
import android.os.storage.VolumeInfo;
import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;
import android.util.Slog;
import android.util.SparseArray;
import com.android.internal.annotations.VisibleForTesting;
import com.android.server.oppo.TemperatureProvider;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/* access modifiers changed from: package-private */
public class UserDataPreparer {
    private static final String TAG = "UserDataPreparer";
    private static final String XATTR_SERIAL = "user.serial";
    private final Context mContext;
    private final Object mInstallLock;
    private final Installer mInstaller;
    private final boolean mOnlyCore;

    UserDataPreparer(Installer installer, Object installLock, Context context, boolean onlyCore) {
        this.mInstallLock = installLock;
        this.mContext = context;
        this.mOnlyCore = onlyCore;
        this.mInstaller = installer;
    }

    /* access modifiers changed from: package-private */
    public void prepareUserData(int userId, int userSerial, int flags) {
        synchronized (this.mInstallLock) {
            for (VolumeInfo vol : ((StorageManager) this.mContext.getSystemService(StorageManager.class)).getWritablePrivateVolumes()) {
                prepareUserDataLI(vol.getFsUuid(), userId, userSerial, flags, true);
            }
        }
    }

    private void prepareUserDataLI(String volumeUuid, int userId, int userSerial, int flags, boolean allowRecover) {
        try {
            ((StorageManager) this.mContext.getSystemService(StorageManager.class)).prepareUserStorage(volumeUuid, userId, userSerial, flags);
            if ((flags & 1) != 0 && !this.mOnlyCore) {
                enforceSerialNumber(getDataUserDeDirectory(volumeUuid, userId), userSerial);
                if (Objects.equals(volumeUuid, StorageManager.UUID_PRIVATE_INTERNAL)) {
                    enforceSerialNumber(getDataSystemDeDirectory(userId), userSerial);
                }
            }
            if ((flags & 2) != 0 && !this.mOnlyCore) {
                enforceSerialNumber(getDataUserCeDirectory(volumeUuid, userId), userSerial);
                if (Objects.equals(volumeUuid, StorageManager.UUID_PRIVATE_INTERNAL)) {
                    enforceSerialNumber(getDataSystemCeDirectory(userId), userSerial);
                }
            }
            this.mInstaller.createUserData(volumeUuid, userId, userSerial, flags);
            if ((flags & 2) != 0 && userId == 0) {
                String propertyName = "sys.user." + userId + ".ce_available";
                Slog.d(TAG, "Setting property: " + propertyName + "=true");
                SystemProperties.set(propertyName, TemperatureProvider.SWITCH_ON);
            }
        } catch (Exception e) {
            PackageManagerServiceUtils.logCriticalInfo(5, "Destroying user " + userId + " on volume " + volumeUuid + " because we failed to prepare: " + e);
            destroyUserDataLI(volumeUuid, userId, flags);
            if (allowRecover) {
                prepareUserDataLI(volumeUuid, userId, userSerial, flags | 1, false);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void destroyUserData(int userId, int flags) {
        synchronized (this.mInstallLock) {
            for (VolumeInfo vol : ((StorageManager) this.mContext.getSystemService(StorageManager.class)).getWritablePrivateVolumes()) {
                destroyUserDataLI(vol.getFsUuid(), userId, flags);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void destroyUserDataLI(String volumeUuid, int userId, int flags) {
        StorageManager storage = (StorageManager) this.mContext.getSystemService(StorageManager.class);
        try {
            this.mInstaller.destroyUserData(volumeUuid, userId, flags);
            if (Objects.equals(volumeUuid, StorageManager.UUID_PRIVATE_INTERNAL)) {
                if ((flags & 1) != 0) {
                    FileUtils.deleteContentsAndDir(getUserSystemDirectory(userId));
                    FileUtils.deleteContentsAndDir(getDataSystemDeDirectory(userId));
                }
                if ((flags & 2) != 0) {
                    FileUtils.deleteContentsAndDir(getDataSystemCeDirectory(userId));
                }
            }
            storage.destroyUserStorage(volumeUuid, userId, flags);
        } catch (Exception e) {
            PackageManagerServiceUtils.logCriticalInfo(5, "Failed to destroy user " + userId + " on volume " + volumeUuid + ": " + e);
        }
    }

    /* access modifiers changed from: package-private */
    public void reconcileUsers(String volumeUuid, List<UserInfo> validUsersList) {
        List<File> files = new ArrayList<>();
        Collections.addAll(files, FileUtils.listFilesOrEmpty(Environment.getDataUserDeDirectory(volumeUuid)));
        Collections.addAll(files, FileUtils.listFilesOrEmpty(Environment.getDataUserCeDirectory(volumeUuid)));
        Collections.addAll(files, FileUtils.listFilesOrEmpty(Environment.getDataSystemDeDirectory()));
        Collections.addAll(files, FileUtils.listFilesOrEmpty(Environment.getDataSystemCeDirectory()));
        Collections.addAll(files, FileUtils.listFilesOrEmpty(Environment.getDataMiscCeDirectory()));
        reconcileUsers(volumeUuid, validUsersList, files);
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void reconcileUsers(String volumeUuid, List<UserInfo> validUsersList, List<File> files) {
        int userCount = validUsersList.size();
        SparseArray<UserInfo> users = new SparseArray<>(userCount);
        for (int i = 0; i < userCount; i++) {
            UserInfo user = validUsersList.get(i);
            users.put(user.id, user);
        }
        for (File file : files) {
            if (file.isDirectory()) {
                try {
                    int userId = Integer.parseInt(file.getName());
                    UserInfo info = users.get(userId);
                    boolean destroyUser = false;
                    if (info == null) {
                        PackageManagerServiceUtils.logCriticalInfo(5, "Destroying user directory " + file + " because no matching user was found");
                        destroyUser = true;
                    } else if (!this.mOnlyCore) {
                        try {
                            enforceSerialNumber(file, info.serialNumber);
                        } catch (IOException e) {
                            PackageManagerServiceUtils.logCriticalInfo(5, "Destroying user directory " + file + " because we failed to enforce serial number: " + e);
                            destroyUser = true;
                        }
                    }
                    if (destroyUser) {
                        synchronized (this.mInstallLock) {
                            destroyUserDataLI(volumeUuid, userId, 3);
                        }
                    } else {
                        continue;
                    }
                } catch (NumberFormatException e2) {
                    Slog.w(TAG, "Invalid user directory " + file);
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public File getDataMiscCeDirectory(int userId) {
        return Environment.getDataMiscCeDirectory(userId);
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public File getDataSystemCeDirectory(int userId) {
        return Environment.getDataSystemCeDirectory(userId);
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public File getDataMiscDeDirectory(int userId) {
        return Environment.getDataMiscDeDirectory(userId);
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public File getUserSystemDirectory(int userId) {
        return Environment.getUserSystemDirectory(userId);
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public File getDataUserCeDirectory(String volumeUuid, int userId) {
        return Environment.getDataUserCeDirectory(volumeUuid, userId);
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public File getDataSystemDeDirectory(int userId) {
        return Environment.getDataSystemDeDirectory(userId);
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public File getDataUserDeDirectory(String volumeUuid, int userId) {
        return Environment.getDataUserDeDirectory(volumeUuid, userId);
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public boolean isFileEncryptedEmulatedOnly() {
        return StorageManager.isFileEncryptedEmulatedOnly();
    }

    /* access modifiers changed from: package-private */
    public void enforceSerialNumber(File file, int serialNumber) throws IOException {
        if (isFileEncryptedEmulatedOnly()) {
            Slog.w(TAG, "Device is emulating FBE; assuming current serial number is valid");
            return;
        }
        int foundSerial = getSerialNumber(file);
        Slog.v(TAG, "Found " + file + " with serial number " + foundSerial);
        if (foundSerial == -1) {
            Slog.d(TAG, "Serial number missing on " + file + "; assuming current is valid");
            try {
                setSerialNumber(file, serialNumber);
            } catch (IOException e) {
                Slog.w(TAG, "Failed to set serial number on " + file, e);
            }
        } else if (foundSerial != serialNumber) {
            throw new IOException("Found serial number " + foundSerial + " doesn't match expected " + serialNumber);
        }
    }

    private static void setSerialNumber(File file, int serialNumber) throws IOException {
        try {
            Os.setxattr(file.getAbsolutePath(), XATTR_SERIAL, Integer.toString(serialNumber).getBytes(StandardCharsets.UTF_8), OsConstants.XATTR_CREATE);
        } catch (ErrnoException e) {
            throw e.rethrowAsIOException();
        }
    }

    @VisibleForTesting
    static int getSerialNumber(File file) throws IOException {
        try {
            String serial = new String(Os.getxattr(file.getAbsolutePath(), XATTR_SERIAL));
            try {
                return Integer.parseInt(serial);
            } catch (NumberFormatException e) {
                throw new IOException("Bad serial number: " + serial);
            }
        } catch (ErrnoException e2) {
            if (e2.errno == OsConstants.ENODATA) {
                return -1;
            }
            throw e2.rethrowAsIOException();
        }
    }
}
