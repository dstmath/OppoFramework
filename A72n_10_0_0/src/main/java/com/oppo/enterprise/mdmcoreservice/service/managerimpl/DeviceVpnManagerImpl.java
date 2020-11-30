package com.oppo.enterprise.mdmcoreservice.service.managerimpl;

import android.content.ComponentName;
import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.IOppoCustomizeService;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.security.KeyStore;
import android.text.TextUtils;
import android.util.Log;
import com.android.internal.net.VpnProfile;
import com.oppo.enterprise.mdmcoreservice.aidl.DeviceVpnProfile;
import com.oppo.enterprise.mdmcoreservice.aidl.IDeviceVpnManager;
import com.oppo.enterprise.mdmcoreservice.service.PermissionManager;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

public class DeviceVpnManagerImpl extends IDeviceVpnManager.Stub {
    private Context mContext;
    private IOppoCustomizeService mCustService;
    private final KeyStore mKeyStore = KeyStore.getInstance();

    public DeviceVpnManagerImpl(Context context) {
        this.mContext = context;
        this.mCustService = IOppoCustomizeService.Stub.asInterface(ServiceManager.getService("oppocustomize"));
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceVpnManager
    public boolean setVpnProfile(DeviceVpnProfile profile, ComponentName componentName) {
        PermissionManager.getInstance().checkPermission();
        if (profile == null) {
            Log.d("DeviceVpnManagerImpl", "profile is null");
            return false;
        } else if (this.mKeyStore == null) {
            Log.d("DeviceVpnManagerImpl", "KeyStore is null");
            return false;
        } else {
            VpnProfile vpnProfile = getProfile(profile);
            if (vpnProfile == null) {
                return false;
            }
            KeyStore keyStore = this.mKeyStore;
            boolean isSuccess = keyStore.put("VPN_" + vpnProfile.key, vpnProfile.encode(), -1, 0);
            Log.d("DeviceVpnManagerImpl", "setVpnProfile isSuccess :" + isSuccess);
            return isSuccess;
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceVpnManager
    public List<String> getVpnList(ComponentName componentName) {
        PermissionManager.getInstance().checkPermission();
        if (this.mKeyStore != null) {
            return loadVpnKeys(this.mKeyStore, new int[0]);
        }
        Log.d("DeviceVpnManagerImpl", "KeyStore is null");
        return null;
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceVpnManager
    public boolean deleteVpnProfile(ComponentName componentName, String key) {
        PermissionManager.getInstance().checkPermission();
        if (TextUtils.isEmpty(key)) {
            Log.d("DeviceVpnManagerImpl", "key is null");
            return false;
        } else if (this.mKeyStore == null) {
            Log.d("DeviceVpnManagerImpl", "KeyStore is null");
            return false;
        } else {
            KeyStore keyStore = this.mKeyStore;
            boolean isSuccess = keyStore.delete("VPN_" + key);
            Log.d("DeviceVpnManagerImpl", "deleteVpnProfile isSuccess : " + isSuccess);
            return isSuccess;
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceVpnManager
    public DeviceVpnProfile getVpnProfile(ComponentName componentName, String key) {
        PermissionManager.getInstance().checkPermission();
        if (TextUtils.isEmpty(key)) {
            Log.d("DeviceVpnManagerImpl", "getVpnProfile :key is null");
            return null;
        } else if (this.mKeyStore == null) {
            Log.d("DeviceVpnManagerImpl", "KeyStore is null");
            return null;
        } else {
            KeyStore keyStore = this.mKeyStore;
            if (keyStore.get("VPN_" + key) == null) {
                return null;
            }
            KeyStore keyStore2 = this.mKeyStore;
            VpnProfile vpnProfile = VpnProfile.decode(key, keyStore2.get("VPN_" + key));
            if (vpnProfile != null) {
                return getDeviceVpnProfile(vpnProfile);
            }
            Log.d("DeviceVpnManagerImpl", "vpnProfile is null");
            return null;
        }
    }

    public VpnProfile getProfile(DeviceVpnProfile deviceVpnProfile) {
        String key = deviceVpnProfile.getKey();
        if (TextUtils.isEmpty(key)) {
            Log.d("DeviceVpnManagerImpl", "vpn key is null");
            return null;
        }
        VpnProfile vpnProfile = new VpnProfile(key);
        String name = deviceVpnProfile.getName();
        if (TextUtils.isEmpty(name)) {
            return null;
        }
        vpnProfile.name = name;
        vpnProfile.type = deviceVpnProfile.getType();
        String server = deviceVpnProfile.getServer();
        if (TextUtils.isEmpty(server)) {
            return null;
        }
        vpnProfile.server = server;
        String username = deviceVpnProfile.getUserName();
        if (TextUtils.isEmpty(username)) {
            return null;
        }
        vpnProfile.username = username;
        String password = deviceVpnProfile.getPassword();
        if (TextUtils.isEmpty(password)) {
            return null;
        }
        vpnProfile.password = password;
        vpnProfile.mppe = deviceVpnProfile.isMppe();
        String l2tpSecret = deviceVpnProfile.getL2tpSecret();
        if (!TextUtils.isEmpty(l2tpSecret)) {
            vpnProfile.l2tpSecret = l2tpSecret;
        }
        String ipsecIdentifier = deviceVpnProfile.getIpsecIdentifier();
        if (!TextUtils.isEmpty(ipsecIdentifier)) {
            vpnProfile.ipsecIdentifier = ipsecIdentifier;
        }
        String ipsecSecret = deviceVpnProfile.getIpsecSecret();
        if (!TextUtils.isEmpty(ipsecSecret)) {
            vpnProfile.ipsecSecret = ipsecSecret;
        }
        String ipsecUserCert = deviceVpnProfile.getIpsecUserCert();
        if (!TextUtils.isEmpty(ipsecUserCert)) {
            vpnProfile.ipsecUserCert = ipsecUserCert;
        }
        String ipsecCaCert = deviceVpnProfile.getIpsecCaCert();
        if (!TextUtils.isEmpty(ipsecCaCert)) {
            vpnProfile.ipsecCaCert = ipsecCaCert;
        }
        String ipsecServerCert = deviceVpnProfile.getIpsecServerCert();
        if (!TextUtils.isEmpty(ipsecServerCert)) {
            vpnProfile.ipsecServerCert = ipsecServerCert;
        }
        vpnProfile.saveLogin = true;
        return vpnProfile;
    }

    private static List<String> loadVpnKeys(KeyStore keyStore, int... excludeTypes) {
        String[] keys = keyStore.list("VPN_");
        if (keys != null) {
            return Arrays.asList(keys);
        }
        Log.d("DeviceVpnManagerImpl", "keys is null ");
        return null;
    }

    private static DeviceVpnProfile getDeviceVpnProfile(VpnProfile vpnProfile) {
        DeviceVpnProfile deviceVpnProfile = new DeviceVpnProfile(vpnProfile.key);
        deviceVpnProfile.setName(vpnProfile.name);
        deviceVpnProfile.setType(vpnProfile.type);
        deviceVpnProfile.setServer(vpnProfile.server);
        deviceVpnProfile.setUsername(vpnProfile.username);
        deviceVpnProfile.setPassword(vpnProfile.password);
        deviceVpnProfile.setMppe(vpnProfile.mppe);
        deviceVpnProfile.setL2tpSecret(vpnProfile.l2tpSecret);
        deviceVpnProfile.setIpsecIdentifier(vpnProfile.ipsecIdentifier);
        deviceVpnProfile.setIpsecSecret(vpnProfile.ipsecSecret);
        deviceVpnProfile.setIpsecUserCert(vpnProfile.ipsecUserCert);
        deviceVpnProfile.setIpsecCaCert(vpnProfile.ipsecCaCert);
        deviceVpnProfile.setIpsecServerCert(vpnProfile.ipsecServerCert);
        return deviceVpnProfile;
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceVpnManager
    public int getVpnServiceState(ComponentName admin) {
        PermissionManager.getInstance().checkPermission();
        int result = -1;
        try {
            result = this.mCustService.getVpnServiceState();
        } catch (Exception e) {
            Log.d("DeviceVpnManagerImpl", "getVpnServiceState:err", e);
        }
        Log.d("DeviceVpnManagerImpl", "getVpnServiceState result = " + result);
        return result;
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceVpnManager
    public int disestablishVpnConnection(ComponentName admin) {
        PermissionManager.getInstance().checkPermission();
        Log.d("DeviceVpnManagerImpl", "disestablishVpnConnection");
        try {
            this.mCustService.disconnectAllVpn();
            return 1;
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceVpnManager
    public void setVpnDisabled(ComponentName admin, boolean disabled) {
        PermissionManager.getInstance().checkPermission();
        if (disabled) {
            try {
                this.mCustService.disconnectAllVpn();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            Log.d("DeviceVpnManagerImpl", "setVpnDisabled : unconnectable");
            SystemProperties.set("persist.sys.vpn_disable", "1");
            return;
        }
        Log.d("DeviceVpnManagerImpl", "setVpnDisabled : connectable");
        SystemProperties.set("persist.sys.vpn_disable", "0");
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceVpnManager
    public boolean isVpnDisabled(ComponentName admin) {
        PermissionManager.getInstance().checkPermission();
        return "1".equalsIgnoreCase(SystemProperties.get("persist.sys.vpn_disable", "0"));
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceVpnManager
    public boolean setAlwaysOnVpnPackage(ComponentName admin, String vpnPackage, boolean lockdown) {
        SecurityException e;
        Exception e2;
        PermissionManager.getInstance().checkPermission();
        boolean result = false;
        try {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) this.mContext.getSystemService("connectivity");
            if (mConnectivityManager != null) {
                try {
                    if (mConnectivityManager.isAlwaysOnVpnPackageSupportedForUser(UserHandle.myUserId(), vpnPackage)) {
                        String str = null;
                        if (Build.VERSION.SDK_INT > 28) {
                            Method method = mConnectivityManager.getClass().getMethod("setAlwaysOnVpnPackageForUser", Integer.TYPE, String.class, Boolean.TYPE, List.class);
                            Object[] objArr = new Object[4];
                            objArr[0] = Integer.valueOf(UserHandle.myUserId());
                            objArr[1] = lockdown ? vpnPackage : null;
                            objArr[2] = true;
                            objArr[3] = null;
                            result = ((Boolean) method.invoke(mConnectivityManager, objArr)).booleanValue();
                            if (result) {
                                SystemProperties.set("persist.sys.vpn_always_on", lockdown ? "1" : "0");
                            }
                        } else {
                            int myUserId = UserHandle.myUserId();
                            if (lockdown) {
                                str = vpnPackage;
                            }
                            result = mConnectivityManager.setAlwaysOnVpnPackageForUser(myUserId, str, true);
                            if (result) {
                                SystemProperties.set("persist.sys.vpn_always_on", lockdown ? "1" : "0");
                            }
                        }
                    }
                } catch (SecurityException e3) {
                    e = e3;
                    Log.d("DeviceVpnManagerImpl", "setAlwaysOnVpnPackage error! e=" + e);
                    return false;
                } catch (Exception e4) {
                    e2 = e4;
                    Log.d("DeviceVpnManagerImpl", "setAlwaysOnVpnPackage error! e=" + e2);
                    return false;
                }
            }
            return result;
        } catch (SecurityException e5) {
            e = e5;
            Log.d("DeviceVpnManagerImpl", "setAlwaysOnVpnPackage error! e=" + e);
            return false;
        } catch (Exception e6) {
            e2 = e6;
            Log.d("DeviceVpnManagerImpl", "setAlwaysOnVpnPackage error! e=" + e2);
            return false;
        }
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceVpnManager
    public String getAlwaysOnVpnPackage(ComponentName admin) {
        PermissionManager.getInstance().checkPermission();
        try {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) this.mContext.getSystemService("connectivity");
            if (mConnectivityManager != null) {
                return mConnectivityManager.getAlwaysOnVpnPackageForUser(UserHandle.myUserId());
            }
            return null;
        } catch (SecurityException e) {
            Log.d("DeviceVpnManagerImpl", "getAlwaysOnVpnPackage error! e=" + e);
            return null;
        }
    }
}
