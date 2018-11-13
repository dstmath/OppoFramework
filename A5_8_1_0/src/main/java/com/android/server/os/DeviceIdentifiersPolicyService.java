package com.android.server.os;

import android.content.Context;
import android.os.Binder;
import android.os.IDeviceIdentifiersPolicyService.Stub;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.UserHandle;
import com.android.server.SystemService;
import com.android.server.am.OppoPermissionConstants;

public final class DeviceIdentifiersPolicyService extends SystemService {

    private static final class DeviceIdentifiersPolicy extends Stub {
        private final Context mContext;

        public DeviceIdentifiersPolicy(Context context) {
            this.mContext = context;
        }

        public String getSerial() throws RemoteException {
            if (UserHandle.getAppId(Binder.getCallingUid()) == 1000 || this.mContext.checkCallingOrSelfPermission(OppoPermissionConstants.PERMISSION_READ_PHONE_STATE) == 0 || this.mContext.checkCallingOrSelfPermission("android.permission.READ_PRIVILEGED_PHONE_STATE") == 0) {
                return SystemProperties.get("ro.serialno", Shell.NIGHT_MODE_STR_UNKNOWN);
            }
            throw new SecurityException("getSerial requires READ_PHONE_STATE or READ_PRIVILEGED_PHONE_STATE permission");
        }
    }

    public DeviceIdentifiersPolicyService(Context context) {
        super(context);
    }

    public void onStart() {
        publishBinderService("device_identifiers", new DeviceIdentifiersPolicy(getContext()));
    }
}
