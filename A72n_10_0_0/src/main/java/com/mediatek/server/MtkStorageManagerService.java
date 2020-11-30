package com.mediatek.server;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.storage.VolumeInfo;
import android.util.Slog;
import com.android.server.StorageManagerService;
import com.google.android.collect.Lists;
import java.util.ArrayList;

class MtkStorageManagerService extends StorageManagerService {
    private static final Object FORMAT_LOCK = new Object();
    private static final String PRIVACY_PROTECTION_WIPE = "com.mediatek.ppl.NOTIFY_MOUNT_SERVICE_WIPE";
    private static final String PRIVACY_PROTECTION_WIPE_DONE = "com.mediatek.ppl.MOUNT_SERVICE_WIPE_RESPONSE";
    private static final String TAG = "MtkStorageManagerService";
    private final BroadcastReceiver mPrivacyProtectionReceiver = new BroadcastReceiver() {
        /* class com.mediatek.server.MtkStorageManagerService.AnonymousClass2 */

        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(MtkStorageManagerService.PRIVACY_PROTECTION_WIPE)) {
                Slog.i(MtkStorageManagerService.TAG, "Privacy Protection wipe!");
                MtkStorageManagerService.this.formatPhoneStorageAndExternalSDCard();
            }
        }
    };

    public MtkStorageManagerService(Context context) {
        super(context);
        registerPrivacyProtectionReceiver();
    }

    public static class MtkStorageManagerServiceLifecycle extends StorageManagerService.Lifecycle {
        public MtkStorageManagerServiceLifecycle(Context context) {
            super(context);
        }

        public void onStart() {
            this.mStorageManagerService = new MtkStorageManagerService(getContext());
            publishBinderService("mount", this.mStorageManagerService);
            this.mStorageManagerService.start();
        }
    }

    private void registerPrivacyProtectionReceiver() {
        IntentFilter privacyProtectionFilter = new IntentFilter();
        privacyProtectionFilter.addAction(PRIVACY_PROTECTION_WIPE);
        this.mContext.registerReceiver(this.mPrivacyProtectionReceiver, privacyProtectionFilter, "com.mediatek.permission.MOUNT_SERVICE_WIPE", this.mHandler);
    }

    private ArrayList<VolumeInfo> findVolumeListNeedFormat() {
        Slog.i(TAG, "findVolumeListNeedFormat");
        ArrayList<VolumeInfo> tempVolumes = Lists.newArrayList();
        synchronized (this.mLock) {
            for (int i = 0; i < this.mVolumes.size(); i++) {
                VolumeInfo vol = (VolumeInfo) this.mVolumes.valueAt(i);
                if ((!isUSBOTG(vol) && vol.isVisible() && vol.getType() == 0) || (vol.getType() == 1 && vol.getDiskId() != null)) {
                    tempVolumes.add(vol);
                    Slog.i(TAG, "i will try to format volume= " + vol);
                }
            }
        }
        return tempVolumes;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void formatPhoneStorageAndExternalSDCard() {
        final ArrayList<VolumeInfo> tempVolumes = findVolumeListNeedFormat();
        new Thread() {
            /* class com.mediatek.server.MtkStorageManagerService.AnonymousClass1 */

            public void run() {
                synchronized (MtkStorageManagerService.FORMAT_LOCK) {
                    int i = MtkStorageManagerService.this.mCurrentUserId;
                    for (int i2 = 0; i2 < tempVolumes.size(); i2++) {
                        VolumeInfo vol = (VolumeInfo) tempVolumes.get(i2);
                        if (vol.getType() != 1 || vol.getDiskId() == null) {
                            if (vol.getState() == 1) {
                                Slog.i(MtkStorageManagerService.TAG, "volume is checking, wait..");
                                int j = 0;
                                while (true) {
                                    if (j >= 30) {
                                        break;
                                    }
                                    try {
                                        sleep(1000);
                                    } catch (InterruptedException ex) {
                                        Slog.e(MtkStorageManagerService.TAG, "Exception when wait!", ex);
                                    }
                                    if (vol.getState() != 1) {
                                        Slog.i(MtkStorageManagerService.TAG, "volume wait checking done!");
                                        break;
                                    }
                                    j++;
                                }
                            }
                            if (vol.getState() == 2) {
                                Slog.i(MtkStorageManagerService.TAG, "volume is mounted, unmount firstly, volume=" + vol);
                                MtkStorageManagerService.this.unmount(vol.getId());
                                int j2 = 0;
                                while (true) {
                                    if (j2 >= 30) {
                                        break;
                                    }
                                    try {
                                        sleep(1000);
                                    } catch (InterruptedException ex2) {
                                        Slog.e(MtkStorageManagerService.TAG, "Exception when wait!", ex2);
                                    }
                                    if (vol.getState() == 0) {
                                        Slog.i(MtkStorageManagerService.TAG, "wait unmount done!");
                                        break;
                                    }
                                    j2++;
                                }
                            }
                            MtkStorageManagerService.this.format(vol.getId());
                            Slog.d(MtkStorageManagerService.TAG, "format Succeed! volume=" + vol);
                        } else {
                            Slog.i(MtkStorageManagerService.TAG, "use partition public to format, volume= " + vol);
                            MtkStorageManagerService.this.partitionPublic(vol.getDiskId());
                            if (vol.getFsUuid() != null) {
                                MtkStorageManagerService.this.forgetVolume(vol.getFsUuid());
                            }
                        }
                    }
                    Intent intent = new Intent(MtkStorageManagerService.PRIVACY_PROTECTION_WIPE_DONE);
                    MtkStorageManagerService.this.mContext.sendBroadcast(intent, "com.mediatek.permission.MOUNT_SERVICE_WIPE");
                    Slog.d(MtkStorageManagerService.TAG, "Privacy Protection wipe: send " + intent);
                }
            }
        }.start();
    }

    public boolean isUSBOTG(VolumeInfo vol) {
        String[] idSplit;
        String diskID = vol.getDiskId();
        if (diskID == null || (idSplit = diskID.split(":")) == null || idSplit.length != 2 || !idSplit[1].startsWith("8,")) {
            return false;
        }
        Slog.d(TAG, "this is a usb otg");
        return true;
    }
}
