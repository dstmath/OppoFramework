package com.android.server.alipay;

import android.content.Context;
import android.content.res.Resources;
import android.hardware.alipay.IAlipayAuthenticatorCallback;
import android.hardware.alipay.IAlipayService;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.IHwBinder;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.JsonReader;
import android.util.Slog;
import android.view.WindowManager;
import com.android.server.SystemService;
import com.android.server.alipay.RomUpdateHelper;
import com.android.server.biometrics.fingerprint.dcs.DcsFingerprintStatisticsUtil;
import com.android.server.connectivity.networkrecovery.dnsresolve.StringUtils;
import com.android.server.oppo.TemperatureProvider;
import com.android.server.pm.PackageManagerService;
import com.android.server.wm.ColorDummyDisplayPolicyEx;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import vendor.oppo.hardware.biometrics.fingerprintpay.V1_0.IAuthenticatorCallback;
import vendor.oppo.hardware.biometrics.fingerprintpay.V1_0.IFingerprintPay;

public class AlipayService extends SystemService {
    public static final int KEY_ALIPAY_AUTHENTICATE = 1;
    public static final int KEY_ALIPAY_ENROLL = 0;
    public static final int KEY_ALIPAY_FAILED = 1;
    public static final int KEY_ALIPAY_SUCCESS = 0;
    public static final String TAG = "AlipayService";
    private IHwBinder.DeathRecipient mAliPayServiceDeathRecipient = new IHwBinder.DeathRecipient() {
        /* class com.android.server.alipay.AlipayService.AnonymousClass1 */

        public void serviceDied(long cookie) {
            Slog.d(AlipayService.TAG, "fingerprintAlipayService died");
            IFingerprintPay unused = AlipayService.this.mFingerprintPay = null;
        }
    };
    /* access modifiers changed from: private */
    public IFAAModelHelper.IFAAModelUpdateInfo.IfaaModelToken mCachedModel;
    /* access modifiers changed from: private */
    public long mCachedVersion = -1;
    /* access modifiers changed from: private */
    public Context mContext;
    /* access modifiers changed from: private */
    public DcsFingerprintStatisticsUtil mDcsStatisticsUtil;
    /* access modifiers changed from: private */
    public IFingerprintPay mFingerprintPay = null;
    /* access modifiers changed from: private */
    public IFAAModelHelper mIFAAModelHelper;

    public AlipayService(Context context) {
        super(context);
        Slog.d(TAG, TAG);
        this.mContext = context;
        this.mDcsStatisticsUtil = DcsFingerprintStatisticsUtil.getDcsFingerprintStatisticsUtil(this.mContext);
    }

    /* JADX WARN: Type inference failed for: r0v2, types: [com.android.server.alipay.AlipayService$AlipayServiceWrapper, android.os.IBinder] */
    @Override // com.android.server.SystemService
    public void onStart() {
        Slog.d(TAG, "onStart");
        this.mIFAAModelHelper = new IFAAModelHelper(this.mContext);
        publishBinderService("alipay", new AlipayServiceWrapper());
    }

    public void systemReady() {
        Slog.d(TAG, "systemReady");
        this.mIFAAModelHelper.registerUpdateBroadcastReceiver();
    }

    public IFingerprintPay getAliPayService() {
        IFingerprintPay fingerprintPay;
        try {
            fingerprintPay = IFingerprintPay.getService();
            fingerprintPay.asBinder().linkToDeath(this.mAliPayServiceDeathRecipient, 0);
        } catch (RemoteException e) {
            Slog.e(TAG, "Failed to open fingerprintAlipayService HAL", e);
            fingerprintPay = null;
        }
        if (fingerprintPay == null) {
            Slog.e(TAG, "alipayService = null, Failed to fingerprintAlipayService HAL");
        }
        return fingerprintPay;
    }

    private final class AlipayServiceWrapper extends IAlipayService.Stub implements IBinder.DeathRecipient {
        private IBinder mToken;

        private AlipayServiceWrapper() {
        }

        public byte[] alipayInvokeCommand(byte[] param) {
            if (AlipayService.this.mFingerprintPay == null) {
                AlipayService alipayService = AlipayService.this;
                IFingerprintPay unused = alipayService.mFingerprintPay = alipayService.getAliPayService();
            }
            if (AlipayService.this.mFingerprintPay == null) {
                Slog.w(AlipayService.TAG, "alipayInvokeCommand: no FingerprintPayService!");
                return null;
            }
            byte[] receiveBuffer = null;
            try {
                ArrayList<Byte> paramByteArray = new ArrayList<>();
                for (byte b : param) {
                    paramByteArray.add(new Byte(b));
                }
                ArrayList<Byte> receiveBufferByteArray = AlipayService.this.mFingerprintPay.alipay_invoke_command(paramByteArray);
                receiveBuffer = new byte[receiveBufferByteArray.size()];
                for (int i = 0; i < receiveBufferByteArray.size(); i++) {
                    receiveBuffer[i] = receiveBufferByteArray.get(i).byteValue();
                }
            } catch (RemoteException e) {
                Slog.e(AlipayService.TAG, "alipay_invoke_command failed", e);
            }
            return receiveBuffer;
        }

        public byte[] alipayFaceInvokeCommand(byte[] param) {
            if (AlipayService.this.mFingerprintPay == null) {
                AlipayService alipayService = AlipayService.this;
                IFingerprintPay unused = alipayService.mFingerprintPay = alipayService.getAliPayService();
            }
            if (AlipayService.this.mFingerprintPay == null) {
                Slog.w(AlipayService.TAG, "alipayInvokeCommand: no FingerprintPayService!");
                return null;
            }
            byte[] receiveBuffer = null;
            try {
                ArrayList<Byte> paramByteArray = new ArrayList<>();
                for (byte b : param) {
                    paramByteArray.add(new Byte(b));
                }
                new ArrayList();
                ArrayList<Byte> receiveBufferByteArray = AlipayService.this.mFingerprintPay.alipay_face_invoke_command(paramByteArray);
                receiveBuffer = new byte[receiveBufferByteArray.size()];
                for (int i = 0; i < receiveBufferByteArray.size(); i++) {
                    receiveBuffer[i] = receiveBufferByteArray.get(i).byteValue();
                }
            } catch (RemoteException e) {
                Slog.e(AlipayService.TAG, "alipay_invoke_command failed", e);
            }
            return receiveBuffer;
        }

        public void authenticate(IBinder token, String reqId, int flags, final IAlipayAuthenticatorCallback callback) {
            if (AlipayService.this.mFingerprintPay == null) {
                AlipayService alipayService = AlipayService.this;
                IFingerprintPay unused = alipayService.mFingerprintPay = alipayService.getAliPayService();
            }
            if (AlipayService.this.mFingerprintPay == null) {
                Slog.w(AlipayService.TAG, "authenticate: no FingerprintPayService!");
                return;
            }
            try {
                this.mToken = token;
                if (this.mToken != null) {
                    this.mToken.linkToDeath(this, 0);
                }
                AlipayService.this.mFingerprintPay.authenticate(reqId, flags, new IAuthenticatorCallback.Stub() {
                    /* class com.android.server.alipay.AlipayService.AlipayServiceWrapper.AnonymousClass1 */

                    @Override // vendor.oppo.hardware.biometrics.fingerprintpay.V1_0.IAuthenticatorCallback
                    public void onAuthenticationError(int errorCode) {
                        try {
                            callback.onAuthenticationError(errorCode);
                        } catch (RemoteException e) {
                            Slog.e(AlipayService.TAG, "Failed to call onAuthenticationError to receiver", e);
                        }
                    }

                    @Override // vendor.oppo.hardware.biometrics.fingerprintpay.V1_0.IAuthenticatorCallback
                    public void onAuthenticationStatus(int status) {
                        try {
                            callback.onAuthenticationStatus(status);
                        } catch (RemoteException e) {
                            Slog.e(AlipayService.TAG, "Failed to call onAuthenticationStatus to receiver", e);
                        }
                    }

                    @Override // vendor.oppo.hardware.biometrics.fingerprintpay.V1_0.IAuthenticatorCallback
                    public void onAuthenticationSucceeded() {
                        Slog.w(AlipayService.TAG, "alipay verify success");
                        if (AlipayService.this.mDcsStatisticsUtil != null) {
                            AlipayService.this.mDcsStatisticsUtil.sendAlipayEvent(1, 0);
                        }
                        try {
                            callback.onAuthenticationSucceeded();
                        } catch (RemoteException e) {
                            Slog.e(AlipayService.TAG, "Failed to call onAuthenticationSucceeded to receiver", e);
                        }
                    }

                    @Override // vendor.oppo.hardware.biometrics.fingerprintpay.V1_0.IAuthenticatorCallback
                    public void onAuthenticationFailed(int errorCode) {
                        Slog.w(AlipayService.TAG, "alipay verify failed");
                        if (AlipayService.this.mDcsStatisticsUtil != null) {
                            AlipayService.this.mDcsStatisticsUtil.sendAlipayEvent(1, 1);
                        }
                        try {
                            callback.onAuthenticationFailed(errorCode);
                        } catch (RemoteException e) {
                            Slog.e(AlipayService.TAG, "Failed to call onAuthenticationFailed to receiver", e);
                        }
                    }
                });
            } catch (RemoteException e) {
                Slog.e(AlipayService.TAG, "authenticate failed", e);
            }
        }

        public void enroll(IBinder token, String reqId, int flags, final IAlipayAuthenticatorCallback callback) {
            if (AlipayService.this.mFingerprintPay == null) {
                AlipayService alipayService = AlipayService.this;
                IFingerprintPay unused = alipayService.mFingerprintPay = alipayService.getAliPayService();
            }
            if (AlipayService.this.mFingerprintPay == null) {
                Slog.w(AlipayService.TAG, "enroll: no FingerprintPayService!");
                return;
            }
            try {
                this.mToken = token;
                if (this.mToken != null) {
                    this.mToken.linkToDeath(this, 0);
                }
                AlipayService.this.mFingerprintPay.enroll(reqId, flags, new IAuthenticatorCallback.Stub() {
                    /* class com.android.server.alipay.AlipayService.AlipayServiceWrapper.AnonymousClass2 */

                    @Override // vendor.oppo.hardware.biometrics.fingerprintpay.V1_0.IAuthenticatorCallback
                    public void onAuthenticationError(int errorCode) {
                        try {
                            callback.onAuthenticationError(errorCode);
                        } catch (RemoteException e) {
                            Slog.e(AlipayService.TAG, "Failed to call onAuthenticationError to receiver", e);
                        }
                    }

                    @Override // vendor.oppo.hardware.biometrics.fingerprintpay.V1_0.IAuthenticatorCallback
                    public void onAuthenticationStatus(int status) {
                        try {
                            callback.onAuthenticationStatus(status);
                        } catch (RemoteException e) {
                            Slog.e(AlipayService.TAG, "Failed to call onAuthenticationStatus to receiver", e);
                        }
                    }

                    @Override // vendor.oppo.hardware.biometrics.fingerprintpay.V1_0.IAuthenticatorCallback
                    public void onAuthenticationSucceeded() {
                        Slog.w(AlipayService.TAG, "alipay enroll success");
                        if (AlipayService.this.mDcsStatisticsUtil != null) {
                            Slog.w(AlipayService.TAG, "upload data to dcs wangyanming");
                            AlipayService.this.mDcsStatisticsUtil.sendAlipayEvent(0, 0);
                        }
                        try {
                            callback.onAuthenticationSucceeded();
                        } catch (RemoteException e) {
                            Slog.e(AlipayService.TAG, "Failed to call onAuthenticationSucceeded to receiver", e);
                        }
                    }

                    @Override // vendor.oppo.hardware.biometrics.fingerprintpay.V1_0.IAuthenticatorCallback
                    public void onAuthenticationFailed(int errorCode) {
                        Slog.w(AlipayService.TAG, "alipay enroll failed");
                        if (AlipayService.this.mDcsStatisticsUtil != null) {
                            Slog.w(AlipayService.TAG, "upload data to dcs wangyanming");
                            AlipayService.this.mDcsStatisticsUtil.sendAlipayEvent(0, 1);
                        }
                        try {
                            callback.onAuthenticationFailed(errorCode);
                        } catch (RemoteException e) {
                            Slog.e(AlipayService.TAG, "Failed to call onAuthenticationFailed to receiver", e);
                        }
                    }
                });
            } catch (RemoteException e) {
                Slog.e(AlipayService.TAG, "enroll failed", e);
            }
        }

        public int cancel(String reqId) {
            if (AlipayService.this.mFingerprintPay == null) {
                AlipayService alipayService = AlipayService.this;
                IFingerprintPay unused = alipayService.mFingerprintPay = alipayService.getAliPayService();
            }
            if (AlipayService.this.mFingerprintPay == null) {
                Slog.w(AlipayService.TAG, "cancel: no FingerprintPayService!");
                return -1;
            }
            int result = -1;
            try {
                result = AlipayService.this.mFingerprintPay.cancel(reqId);
                if (this.mToken != null) {
                    this.mToken.unlinkToDeath(this, 0);
                }
            } catch (RemoteException e) {
                Slog.e(AlipayService.TAG, "cancel failed", e);
            }
            return result;
        }

        public void upgrade(String path) {
            if (AlipayService.this.mFingerprintPay == null) {
                AlipayService alipayService = AlipayService.this;
                IFingerprintPay unused = alipayService.mFingerprintPay = alipayService.getAliPayService();
            }
            if (AlipayService.this.mFingerprintPay == null) {
                Slog.w(AlipayService.TAG, "upgrade: no FingerprintPayService!");
                return;
            }
            try {
                AlipayService.this.mFingerprintPay.upgrade(path);
            } catch (RemoteException e) {
                Slog.e(AlipayService.TAG, "upgrade failed", e);
            }
        }

        public void binderDied() {
            Slog.w(AlipayService.TAG, "alipay client binder died");
            cancel("");
        }

        public int getSupportBIOTypes() {
            IFAAModelHelper.IFAAModelUpdateInfo.IfaaModelToken token = AlipayService.this.findIfaaModelTokenCached();
            if (token != null) {
                return token.bioType;
            }
            return 0;
        }

        public int getSupportIFAAVersion() {
            IFAAModelHelper.IFAAModelUpdateInfo.IfaaModelToken token = AlipayService.this.findIfaaModelTokenCached();
            if (token != null) {
                return token.ifaaVersions;
            }
            return 3;
        }

        public String getDeviceModel() {
            IFAAModelHelper.IFAAModelUpdateInfo.IfaaModelToken token = AlipayService.this.findIfaaModelTokenCached();
            if (token != null) {
                return token.ifaaModel;
            }
            return "OPPO-Default";
        }

        public int getFingerprintIconDiameter() {
            int iconDiameter = Integer.parseInt(SystemProperties.get("persist.vendor.fingerprint.optical.iconsize", "190"));
            Slog.i(AlipayService.TAG, "getFingerprintIconDiameter! and iconDiameter = " + iconDiameter);
            return iconDiameter;
        }

        public int getFingerprintIconExternalCircleXY(String coord) {
            Slog.i(AlipayService.TAG, "getFingerprintIconExternalCircleXY!");
            WindowManager wm = (WindowManager) AlipayService.this.mContext.getSystemService("window");
            int state = Settings.Secure.getInt(AlipayService.this.mContext.getContentResolver(), ColorDummyDisplayPolicyEx.KEY_NAVIGATIONBAR_MODE, 2);
            DisplayMetrics dm = new DisplayMetrics();
            if (wm != null) {
                wm.getDefaultDisplay().getRealMetrics(dm);
            }
            int width = dm.widthPixels;
            int height = dm.heightPixels;
            StringBuilder sb = new StringBuilder();
            sb.append("width = ");
            sb.append(width);
            sb.append(" height = ");
            sb.append(height);
            sb.append(" getStatusBarHeight() = ");
            sb.append(getStatusBarHeight());
            sb.append(" getNavigationBarHeight() = ");
            sb.append(getNavigationBarHeight());
            sb.append(" hasNavigationBar");
            boolean z = true;
            if (!(state == 0 || state == 1)) {
                z = false;
            }
            sb.append(z);
            Slog.i(AlipayService.TAG, sb.toString());
            int iconDiameter = Integer.parseInt(SystemProperties.get("persist.vendor.fingerprint.optical.iconsize", "190"));
            int iconLocation = Integer.parseInt(SystemProperties.get("persist.vendor.fingerprint.optical.iconlocation", "278"));
            int coordinate_x = (width - iconDiameter) / 2;
            int coordinate_y = ((height - iconLocation) - (iconDiameter / 2)) + getStatusBarHeight();
            Slog.i(AlipayService.TAG, "iconDiameter = " + iconDiameter + " iconLocation = " + iconLocation + " coordinate_x = " + coordinate_x + " coordinate_y = " + coordinate_y);
            if ("X".equals(coord)) {
                return coordinate_x;
            }
            return coordinate_y;
        }

        private int getStatusBarHeight() {
            Resources resources = AlipayService.this.mContext.getResources();
            int height = resources.getDimensionPixelSize(resources.getIdentifier("status_bar_height", "dimen", PackageManagerService.PLATFORM_PACKAGE_NAME));
            Slog.i(AlipayService.TAG, "Status height:" + height);
            return height;
        }

        private int getNavigationBarHeight() {
            Resources resources = AlipayService.this.mContext.getResources();
            int height = resources.getDimensionPixelSize(resources.getIdentifier("navigation_bar_height", "dimen", PackageManagerService.PLATFORM_PACKAGE_NAME));
            Slog.i(AlipayService.TAG, "navigationbar height:" + height);
            return height;
        }

        /* access modifiers changed from: protected */
        public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
            if (AlipayService.this.mContext.checkCallingOrSelfPermission("android.permission.DUMP") != 0) {
                pw.println("Permission Denial: can't dump Alipay from from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid());
                return;
            }
            AlipayService.this.mIFAAModelHelper.initialize();
            pw.println("System config:");
            pw.println(((IFAAModelHelper.IFAAModelUpdateInfo) AlipayService.this.mIFAAModelHelper.getSystemInfo()).toString());
            pw.println("Data config:");
            pw.println(((IFAAModelHelper.IFAAModelUpdateInfo) AlipayService.this.mIFAAModelHelper.getDataInfo()).toString());
            long unused = AlipayService.this.mCachedVersion = -1;
            IFAAModelHelper.IFAAModelUpdateInfo.IfaaModelToken model = AlipayService.this.findIfaaModelTokenCached();
            if (model == null) {
                pw.println("IFAA model: No match.");
                return;
            }
            pw.println("IFAA model: " + model.toString());
        }
    }

    /* access modifiers changed from: package-private */
    public IFAAModelHelper.IFAAModelUpdateInfo.IfaaModelToken findIfaaModelTokenCached() {
        synchronized (this.mIFAAModelHelper) {
            if (this.mCachedVersion != -1) {
                if (this.mCachedModel != null && "OPPO-R9165".equals(this.mCachedModel.ifaaModel) && getPCBinfo() >= 4) {
                    this.mCachedModel.ifaaModel = "OPPO-R9165P";
                }
                IFAAModelHelper.IFAAModelUpdateInfo.IfaaModelToken ifaaModelToken = this.mCachedModel;
                return ifaaModelToken;
            }
            this.mIFAAModelHelper.lazyInitIfNeed();
            this.mCachedModel = findIfaaModelToken();
            this.mCachedVersion = ((IFAAModelHelper.IFAAModelUpdateInfo) this.mIFAAModelHelper.getNewerInfo()).getVersion();
            this.mIFAAModelHelper.release();
            if (this.mCachedModel != null && "OPPO-R9165".equals(this.mCachedModel.ifaaModel) && getPCBinfo() >= 4) {
                this.mCachedModel.ifaaModel = "OPPO-R9165P";
            }
            IFAAModelHelper.IFAAModelUpdateInfo.IfaaModelToken ifaaModelToken2 = this.mCachedModel;
            return ifaaModelToken2;
        }
    }

    /* access modifiers changed from: package-private */
    public IFAAModelHelper.IFAAModelUpdateInfo.IfaaModelToken findIfaaModelToken() {
        IFAAModelHelper.IFAAModelUpdateInfo info = (IFAAModelHelper.IFAAModelUpdateInfo) this.mIFAAModelHelper.getNewerInfo();
        IFAAModelHelper.IFAAModelUpdateInfo.IfaaModelToken matchedModel = null;
        if (!TemperatureProvider.SWITCH_ON.equals(SystemProperties.get("persist.version.confidential"))) {
            for (IFAAModelHelper.IFAAModelUpdateInfo.IfaaModelToken model : info.mPublicIfaaModels) {
                Iterator<String> it = model.oppoModel.iterator();
                while (true) {
                    if (!it.hasNext()) {
                        break;
                    }
                    if (Build.MODEL.equals(it.next())) {
                        matchedModel = model;
                        break;
                    }
                }
            }
        } else {
            String build = SystemProperties.get("ro.build.version.ota");
            for (IFAAModelHelper.IFAAModelUpdateInfo.IfaaModelToken model2 : info.mConfiIfaaModels) {
                Iterator<String> it2 = model2.oppoModel.iterator();
                while (true) {
                    if (it2.hasNext()) {
                        if (build.startsWith(it2.next())) {
                            matchedModel = model2;
                            break;
                        }
                    } else {
                        break;
                    }
                }
            }
        }
        return matchedModel;
    }

    private int getPCBinfo() {
        int numRead = 0;
        int result = 4;
        FileInputStream pcbStream = null;
        try {
            byte[] pcbBuf = new byte[1];
            FileInputStream pcbStream2 = new FileInputStream(new File("/proc/oppoVersion/pcbVersion"));
            numRead = pcbStream2.read(pcbBuf);
            if (numRead < 0) {
                return 4;
            }
            String pcbStr = new String(pcbBuf, "GB2312");
            Slog.d(TAG, "getPCBinfo, pcbStr: " + pcbStr);
            result = Integer.valueOf(pcbStr).intValue();
            pcbStream2.close();
            Slog.d(TAG, "getPCBinfo, numRead: " + numRead + " result: " + result);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            if (pcbStream != null) {
                try {
                    pcbStream.close();
                } catch (Exception e0) {
                    e0.printStackTrace();
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public class IFAAModelHelper extends RomUpdateHelper<IFAAModelUpdateInfo> {
        private static final String DATA_FILE_DIR = "data/format_unclear/alipay/sys_alipay_model_list.json";
        public static final String FILTER_NAME = "sys_alipay_model_list";
        private static final String SYS_FILE_DIR = "system/etc/sys_alipay_model_list.json";
        private boolean mHasIntialized = false;

        public IFAAModelHelper(Context context) {
            super(context, FILTER_NAME, SYS_FILE_DIR, DATA_FILE_DIR);
        }

        public void lazyInitIfNeed() {
            if (!this.mHasIntialized) {
                super.initialize();
                this.mHasIntialized = true;
            }
        }

        public void release() {
            IFAAModelUpdateInfo info = (IFAAModelUpdateInfo) getSystemInfo();
            info.mConfiIfaaModels = Collections.emptyList();
            info.mPublicIfaaModels = Collections.emptyList();
            IFAAModelUpdateInfo info2 = (IFAAModelUpdateInfo) getDataInfo();
            info2.mConfiIfaaModels = Collections.emptyList();
            info2.mPublicIfaaModels = Collections.emptyList();
        }

        /* access modifiers changed from: protected */
        @Override // com.android.server.alipay.RomUpdateHelper
        public IFAAModelUpdateInfo newUpdateInfo() {
            return new IFAAModelUpdateInfo();
        }

        /* access modifiers changed from: protected */
        @Override // com.android.server.alipay.RomUpdateHelper
        public void onUpdateInfoChanged() {
            synchronized (this) {
                long unused = AlipayService.this.mCachedVersion = -1;
                IFAAModelUpdateInfo.IfaaModelToken unused2 = AlipayService.this.mCachedModel = null;
                this.mHasIntialized = false;
            }
        }

        private class IFAAModelUpdateInfo extends RomUpdateHelper.UpdateInfo {
            static final String AUTH_TYPE_FACE_NAME = "face";
            static final String AUTH_TYPE_FINGERPRINT_NAME = "fingerprint";
            static final String AUTH_TYPE_OPTICAL_FINGERPRINT_NAME = "optical_fingerprint";
            static final String BIOMETRIC_TYPE_NAME = "bioType";
            static final String IFAA_MODEL_NAME = "ifaaModel";
            static final String IFAA_VERSION = "ifaaVersion";
            static final String MODEL_CONFIG_COMMENT = "comment";
            static final String MODEL_CONFIG_VERSION = "version";
            static final String MODEL_CONFI_CATEGROY = "confidential";
            static final String MODEL_PUBLIC_CATEGROY = "public";
            static final String OPPO_MODEL_NAME = "oppoModel";
            List<IfaaModelToken> mConfiIfaaModels;
            List<IfaaModelToken> mPublicIfaaModels;

            private IFAAModelUpdateInfo() {
                super();
                this.mConfiIfaaModels = new ArrayList();
                this.mPublicIfaaModels = new ArrayList();
            }

            /* access modifiers changed from: package-private */
            public class IfaaModelToken {
                int bioType;
                String ifaaModel;
                int ifaaVersions;
                List<String> oppoModel = new ArrayList();

                IfaaModelToken() {
                }

                public String toString() {
                    List<String> bioTypeName = new ArrayList<>();
                    int i = this.bioType;
                    if ((i & 1) != 0) {
                        if ((i & 17) == 17) {
                            bioTypeName.add(IFAAModelUpdateInfo.AUTH_TYPE_OPTICAL_FINGERPRINT_NAME);
                        } else {
                            bioTypeName.add(IFAAModelUpdateInfo.AUTH_TYPE_FINGERPRINT_NAME);
                        }
                    }
                    if ((this.bioType & 4) == 4) {
                        bioTypeName.add(IFAAModelUpdateInfo.AUTH_TYPE_FACE_NAME);
                    }
                    return "ifaaModel:" + this.ifaaModel + ", " + IFAAModelUpdateInfo.OPPO_MODEL_NAME + ":" + this.oppoModel.toString() + ", " + IFAAModelUpdateInfo.BIOMETRIC_TYPE_NAME + ":" + bioTypeName.toString() + ", " + IFAAModelUpdateInfo.IFAA_VERSION + ":" + this.ifaaVersions;
                }
            }

            @Override // com.android.server.alipay.RomUpdateHelper.UpdateInfo
            public void parseContent(String content) {
                try {
                    this.mVersion = parseIfaaModelConfig(content);
                } catch (IOException e) {
                    Slog.d(AlipayService.TAG, "parse content failed", e);
                }
            }

            public String toString() {
                if (!SystemProperties.getBoolean("persist.sys.assert.panic", false)) {
                    return "";
                }
                StringBuilder config = new StringBuilder();
                config.append("<IFAA model config>\n");
                config.append("version:" + this.mVersion + StringUtils.LF);
                config.append("OPPO Model confidential:\n");
                for (IfaaModelToken token : this.mConfiIfaaModels) {
                    config.append(token.toString());
                    config.append(StringUtils.LF);
                }
                config.append("OPPO Model public:\n");
                for (IfaaModelToken token2 : this.mPublicIfaaModels) {
                    config.append(token2.toString());
                    config.append(StringUtils.LF);
                }
                return config.toString();
            }

            /* access modifiers changed from: package-private */
            /* JADX WARNING: Removed duplicated region for block: B:52:0x00fb A[Catch:{ all -> 0x01b2 }] */
            /* JADX WARNING: Removed duplicated region for block: B:59:0x0111 A[Catch:{ all -> 0x01b2 }] */
            /* JADX WARNING: Removed duplicated region for block: B:94:0x01b9  */
            public long parseIfaaModelConfig(String config) throws IOException {
                List<IfaaModelToken> ifaaModels;
                char c;
                IFAAModelUpdateInfo iFAAModelUpdateInfo = this;
                JsonReader reader = null;
                try {
                    try {
                        JsonReader reader2 = new JsonReader(new StringReader(config));
                        reader2.beginObject();
                        String comment = reader2.nextName();
                        if (MODEL_CONFIG_COMMENT.equals(comment)) {
                            reader2.skipValue();
                            if ("version".equals(reader2.nextName())) {
                                long version = reader2.nextLong();
                                while (reader2.hasNext()) {
                                    String name = reader2.nextName();
                                    if (MODEL_CONFI_CATEGROY.equals(name)) {
                                        ifaaModels = iFAAModelUpdateInfo.mConfiIfaaModels;
                                    } else if (MODEL_PUBLIC_CATEGROY.equals(name)) {
                                        ifaaModels = iFAAModelUpdateInfo.mPublicIfaaModels;
                                    } else {
                                        throw new IOException("Unknown ifaa model category");
                                    }
                                    reader2.beginArray();
                                    while (reader2.hasNext()) {
                                        IfaaModelToken token = new IfaaModelToken();
                                        reader2.beginObject();
                                        if (IFAA_MODEL_NAME.equals(reader2.nextName())) {
                                            token.ifaaModel = reader2.nextString();
                                            if (OPPO_MODEL_NAME.equals(reader2.nextName())) {
                                                reader2.beginArray();
                                                while (reader2.hasNext()) {
                                                    token.oppoModel.add(reader2.nextString());
                                                }
                                                reader2.endArray();
                                                if (BIOMETRIC_TYPE_NAME.equals(reader2.nextName())) {
                                                    reader2.beginArray();
                                                    int authType = 0;
                                                    while (reader2.hasNext()) {
                                                        String bioType = reader2.nextString();
                                                        int hashCode = bioType.hashCode();
                                                        if (hashCode != -1375934236) {
                                                            if (hashCode != -1147966691) {
                                                                if (hashCode == 3135069 && bioType.equals(AUTH_TYPE_FACE_NAME)) {
                                                                    c = 0;
                                                                    if (c != 0) {
                                                                        authType |= 4;
                                                                    } else if (c == 1) {
                                                                        authType |= 1;
                                                                    } else if (c == 2) {
                                                                        authType |= 17;
                                                                    } else {
                                                                        throw new IOException("unknown biometric type");
                                                                    }
                                                                    comment = comment;
                                                                }
                                                            } else if (bioType.equals(AUTH_TYPE_OPTICAL_FINGERPRINT_NAME)) {
                                                                c = 2;
                                                                if (c != 0) {
                                                                }
                                                                comment = comment;
                                                            }
                                                        } else if (bioType.equals(AUTH_TYPE_FINGERPRINT_NAME)) {
                                                            c = 1;
                                                            if (c != 0) {
                                                            }
                                                            comment = comment;
                                                        }
                                                        c = 65535;
                                                        if (c != 0) {
                                                        }
                                                        comment = comment;
                                                    }
                                                    if (authType != 0) {
                                                        token.bioType = authType;
                                                        reader2.endArray();
                                                        try {
                                                            if (IFAA_VERSION.equals(reader2.nextName())) {
                                                                token.ifaaVersions = reader2.nextInt();
                                                                reader2.endObject();
                                                                ifaaModels.add(token);
                                                                iFAAModelUpdateInfo = this;
                                                                comment = comment;
                                                            } else {
                                                                throw new IOException("ifaa version is expected");
                                                            }
                                                        } catch (Exception e) {
                                                            Slog.d(AlipayService.TAG, "get ifaaVersion failed:", e);
                                                        }
                                                    } else {
                                                        throw new IOException("none biometric type support");
                                                    }
                                                } else {
                                                    throw new IOException("biometric type is expected");
                                                }
                                            } else {
                                                throw new IOException("oppo model is expected");
                                            }
                                        } else {
                                            throw new IOException("ifaa model is expected");
                                        }
                                    }
                                    reader2.endArray();
                                    iFAAModelUpdateInfo = this;
                                    comment = comment;
                                }
                                reader2.endObject();
                                reader2.close();
                                return version;
                            }
                            throw new IOException("version is expected");
                        }
                        throw new IOException("comment is expected");
                    } catch (Throwable th) {
                        th = th;
                        if (reader != null) {
                            reader.close();
                        }
                        throw th;
                    }
                } catch (Throwable th2) {
                    th = th2;
                    if (reader != null) {
                    }
                    throw th;
                }
            }
        }
    }
}
