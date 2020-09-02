package com.android.server.os;

import android.content.Context;
import android.os.IDeviceIdentifiersPolicyService;
import android.os.RemoteException;
import android.os.SystemProperties;
import com.android.internal.telephony.TelephonyPermissions;
import com.android.server.SystemService;
import com.android.server.UiModeManagerService;

public final class DeviceIdentifiersPolicyService extends SystemService {
    public DeviceIdentifiersPolicyService(Context context) {
        super(context);
    }

    /* JADX WARN: Type inference failed for: r0v0, types: [com.android.server.os.DeviceIdentifiersPolicyService$DeviceIdentifiersPolicy, android.os.IBinder] */
    @Override // com.android.server.SystemService
    public void onStart() {
        publishBinderService("device_identifiers", new DeviceIdentifiersPolicy(getContext()));
    }

    private static final class DeviceIdentifiersPolicy extends IDeviceIdentifiersPolicyService.Stub {
        private final Context mContext;

        public DeviceIdentifiersPolicy(Context context) {
            this.mContext = context;
        }

        public String getSerial() throws RemoteException {
            if (!TelephonyPermissions.checkCallingOrSelfReadDeviceIdentifiers(this.mContext, (String) null, "getSerial")) {
                return UiModeManagerService.Shell.NIGHT_MODE_STR_UNKNOWN;
            }
            return SystemProperties.get("ro.serialno", UiModeManagerService.Shell.NIGHT_MODE_STR_UNKNOWN);
        }

        public String getSerialForPackage(String callingPackage) throws RemoteException {
            if (!TelephonyPermissions.checkCallingOrSelfReadDeviceIdentifiers(this.mContext, callingPackage, "getSerial")) {
                return UiModeManagerService.Shell.NIGHT_MODE_STR_UNKNOWN;
            }
            return SystemProperties.get("ro.serialno", UiModeManagerService.Shell.NIGHT_MODE_STR_UNKNOWN);
        }
    }
}
