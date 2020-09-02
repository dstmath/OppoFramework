package org.ifaa.android.manager;

import android.app.KeyguardManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.hardware.alipay.AlipayManager;
import android.hardware.face.Face;
import android.hardware.face.FaceManager;
import android.hardware.fingerprint.Fingerprint;
import android.hardware.fingerprint.FingerprintManager;
import android.provider.Settings;
import android.util.Log;
import java.util.List;

public class IFAAManagerFactory {
    public static final String TAG = "IFAAManagerFactory";
    private static IFAAManager sFAAManager;

    public static IFAAManager getIFAAManager(Context context, int authType) {
        if (authType != 1) {
            return null;
        }
        if (sFAAManager == null) {
            sFAAManager = new IFAAManagerOppo(context);
        }
        return sFAAManager;
    }

    private static class IFAAManagerOppo extends IFAAManagerV4 {
        public static final int BIOMETRIC_NOUSE_NOSET_KEYGUARD = 1003;
        public static final int BIOMETRIC_NOUSE_NOT_ENROLLED = 1002;
        public static final int BIOMETRIC_NOUSE_SYSTEMLOCKED = 1001;
        public static final int BIOMETRIC_USE_READY = 1000;
        private static final String KEY_HIDE_NAVIGATIONBAR_ENABLE = "hide_navigationbar_enable";
        private String X_COORDINATE = "X";
        private String Y_COORDINATE = "Y";
        private AlipayManager mAlipayManager;
        private Context mContext;
        private FaceManager mFaceManager = null;
        private FingerprintManager mFingerprintManager = null;
        private KeyguardManager mKeyguardManager = null;

        IFAAManagerOppo(Context context) {
            this.mContext = context;
            ensureAlipayServiceAvailable();
            ensureNeedServiceAvailable();
        }

        /* access modifiers changed from: package-private */
        public void ensureAlipayServiceAvailable() {
            if (this.mAlipayManager == null) {
                this.mAlipayManager = (AlipayManager) this.mContext.getSystemService("alipay");
            }
        }

        /* access modifiers changed from: package-private */
        public void ensureNeedServiceAvailable() {
            if (this.mFingerprintManager == null) {
                this.mFingerprintManager = (FingerprintManager) this.mContext.getSystemService("fingerprint");
                if (this.mFingerprintManager == null) {
                    Log.e(IFAAManagerFactory.TAG, "getIFAAManager: mFingerprintManager = null!");
                }
            }
            if (this.mFaceManager == null) {
                this.mFaceManager = (FaceManager) this.mContext.getSystemService("face");
                if (this.mFaceManager == null) {
                    Log.e(IFAAManagerFactory.TAG, "getIFAAManager: mFaceManager = null!");
                }
            }
            if (this.mKeyguardManager == null) {
                this.mKeyguardManager = (KeyguardManager) this.mContext.getSystemService("keyguard");
            }
        }

        @Override // org.ifaa.android.manager.IFAAManager
        public int getSupportBIOTypes(Context context) {
            ensureAlipayServiceAvailable();
            AlipayManager alipayManager = this.mAlipayManager;
            if (alipayManager == null) {
                Log.e(IFAAManagerFactory.TAG, "getSupportBIOTypes: no Service!");
                return 0;
            }
            int type = alipayManager.getSupportBIOTypes();
            LogUtil.d(IFAAManagerFactory.TAG, "getSupportBIOTypes: " + type);
            return type;
        }

        @Override // org.ifaa.android.manager.IFAAManager
        public int startBIOManager(Context context, int authType) {
            if (authType != 1) {
                return -1;
            }
            try {
                context.startActivityAsUser(new Intent("oppo.intent.action.FINGERPRINT"), context.getUser());
                LogUtil.d(IFAAManagerFactory.TAG, "startBIOManager user:" + context.getUser());
                return 0;
            } catch (ActivityNotFoundException e) {
                return -1;
            }
        }

        @Override // org.ifaa.android.manager.IFAAManager
        public String getDeviceModel() {
            ensureAlipayServiceAvailable();
            AlipayManager alipayManager = this.mAlipayManager;
            if (alipayManager == null) {
                Log.e(IFAAManagerFactory.TAG, "getDeviceModel: no Service!");
                return "OPPO-Default";
            }
            String model = alipayManager.getDeviceModel();
            LogUtil.d(IFAAManagerFactory.TAG, "getDeviceModel: " + model);
            return model;
        }

        @Override // org.ifaa.android.manager.IFAAManagerV4, org.ifaa.android.manager.IFAAManager
        public int getVersion() {
            ensureAlipayServiceAvailable();
            AlipayManager alipayManager = this.mAlipayManager;
            if (alipayManager == null) {
                Log.e(IFAAManagerFactory.TAG, "getVersion: no Service!");
                return 3;
            }
            int version = alipayManager.getSupportIFAAVersion();
            LogUtil.d(IFAAManagerFactory.TAG, "getVersion version: " + version);
            return version;
        }

        @Override // org.ifaa.android.manager.IFAAManager
        public int getFingerprintIconDiameter() {
            ensureAlipayServiceAvailable();
            AlipayManager alipayManager = this.mAlipayManager;
            if (alipayManager == null) {
                Log.e(IFAAManagerFactory.TAG, "getFingerprintIconDiameter: no Service!");
                return 190;
            }
            int iconDiameter = alipayManager.getFingerprintIconDiameter();
            LogUtil.d(IFAAManagerFactory.TAG, "getFingerprintIconDiameter: " + iconDiameter);
            return iconDiameter;
        }

        @Override // org.ifaa.android.manager.IFAAManager
        public int getFingerprintIconExternalCircleXY(String coordinate) {
            ensureAlipayServiceAvailable();
            AlipayManager alipayManager = this.mAlipayManager;
            if (alipayManager == null) {
                Log.e(IFAAManagerFactory.TAG, "getFingerprintIconDiameter: no Service!");
                if ("X".equals(coordinate)) {
                    return 445;
                }
                return 1967;
            }
            int coord = alipayManager.getFingerprintIconExternalCircleXY(coordinate);
            LogUtil.d(IFAAManagerFactory.TAG, "getFingerprintIconExternalCircleXY: " + coord);
            return coord;
        }

        private boolean hasNavigationBar(Context context) {
            int state = Settings.Secure.getInt(context.getContentResolver(), KEY_HIDE_NAVIGATIONBAR_ENABLE, 2);
            LogUtil.d(IFAAManagerFactory.TAG, "state: " + state);
            return state == 0 || state == 1;
        }

        @Override // org.ifaa.android.manager.IFAAManagerV2
        public byte[] processCmdV2(Context context, byte[] param) {
            LogUtil.v(IFAAManagerFactory.TAG, "processCmdV2: called!");
            ensureAlipayServiceAvailable();
            AlipayManager alipayManager = this.mAlipayManager;
            if (alipayManager != null) {
                return alipayManager.alipayInvokeCommand(param);
            }
            LogUtil.e(IFAAManagerFactory.TAG, "processCmdV2: no Service!");
            return null;
        }

        @Override // org.ifaa.android.manager.IFAAManagerV3
        public String getExtInfo(int authType, String keyExtInfo) {
            Log.i(IFAAManagerFactory.TAG, "Enter getExtInfo()!");
            String res = "";
            if (IFAAManagerV3.KEY_GET_SENSOR_LOCATION.equals(keyExtInfo)) {
                res = "{\"type\": 0,\"fullView\": {    \"startX\" : " + getFingerprintIconExternalCircleXY(this.X_COORDINATE) + ",    \"startY\" : " + getFingerprintIconExternalCircleXY(this.Y_COORDINATE) + ",    \"width\" : " + getFingerprintIconDiameter() + ",    \"height\": " + getFingerprintIconDiameter() + ",    \"navConflict\" : " + hasNavigationBar(this.mContext) + "}}";
            }
            Log.i(IFAAManagerFactory.TAG, "res = " + res);
            return res;
        }

        @Override // org.ifaa.android.manager.IFAAManagerV3
        public void setExtInfo(int authType, String keyExtInfo, String valExtInfo) {
        }

        @Override // org.ifaa.android.manager.IFAAManagerV4
        public int getEnabled(int bioType) {
            ensureNeedServiceAvailable();
            if (!this.mKeyguardManager.isKeyguardSecure()) {
                Log.e(IFAAManagerFactory.TAG, "Security keyguard no set");
                return BIOMETRIC_NOUSE_NOSET_KEYGUARD;
            } else if (bioType == 1) {
                FingerprintManager fingerprintManager = this.mFingerprintManager;
                if (fingerprintManager == null || !fingerprintManager.isHardwareDetected()) {
                    Log.e(IFAAManagerFactory.TAG, "Fingerprint Hardware not available!");
                    return BIOMETRIC_NOUSE_SYSTEMLOCKED;
                } else if (this.mFingerprintManager.getEnrolledFingerprints() == null || this.mFingerprintManager.getEnrolledFingerprints().size() == 0) {
                    Log.e(IFAAManagerFactory.TAG, "Fingerprint not Enrolled");
                    return BIOMETRIC_NOUSE_NOT_ENROLLED;
                } else if (this.mFingerprintManager.getLockoutAttemptDeadline() <= 0) {
                    return BIOMETRIC_USE_READY;
                } else {
                    Log.e(IFAAManagerFactory.TAG, "Fingerprint already attempt 5 times to Deadline");
                    return BIOMETRIC_NOUSE_SYSTEMLOCKED;
                }
            } else if (bioType == 4) {
                FaceManager faceManager = this.mFaceManager;
                if (faceManager == null || !faceManager.isHardwareDetected()) {
                    Log.w(IFAAManagerFactory.TAG, "Face Hardware not available!");
                    return BIOMETRIC_NOUSE_SYSTEMLOCKED;
                } else if (this.mFaceManager.getEnrolledFaces() != null && this.mFaceManager.getEnrolledFaces().size() != 0) {
                    return BIOMETRIC_USE_READY;
                } else {
                    Log.w(IFAAManagerFactory.TAG, "Face not Enrolled");
                    return BIOMETRIC_NOUSE_NOT_ENROLLED;
                }
            } else {
                LogUtil.w(IFAAManagerFactory.TAG, "bioType err:" + bioType);
                return 0;
            }
        }

        @Override // org.ifaa.android.manager.IFAAManagerV4
        public int[] getIDList(int bioType) {
            ensureNeedServiceAvailable();
            if (bioType == 4) {
                if (this.mFaceManager == null) {
                    Log.w(IFAAManagerFactory.TAG, "getIDList: no mFaceManager!");
                    return null;
                }
                LogUtil.w(IFAAManagerFactory.TAG, "getIDList:face list!");
                List<Face> mFaceList = this.mFaceManager.getEnrolledFaces();
                if (!(mFaceList == null || mFaceList.size() == 0)) {
                    int[] myIDList = new int[mFaceList.size()];
                    for (int i = 0; i < mFaceList.size() - 1; i++) {
                        if (mFaceList.get(i) != null) {
                            myIDList[i] = mFaceList.get(i).getBiometricId();
                        }
                    }
                    return myIDList;
                }
            } else if (bioType == 1) {
                if (this.mFingerprintManager == null) {
                    Log.w(IFAAManagerFactory.TAG, "getIDList: no FingerprintManager!");
                    return null;
                }
                LogUtil.w(IFAAManagerFactory.TAG, "getIDList:fingerprint list!");
                List<Fingerprint> myFingerprintList = this.mFingerprintManager.getEnrolledFingerprints();
                if (!(myFingerprintList == null || myFingerprintList.size() == 0)) {
                    int[] myIDList2 = new int[myFingerprintList.size()];
                    for (int i2 = 0; i2 < myFingerprintList.size() - 1; i2++) {
                        if (myFingerprintList.get(i2) != null) {
                            myIDList2[i2] = myFingerprintList.get(i2).getBiometricId();
                        }
                    }
                    return myIDList2;
                }
            }
            return null;
        }

        public String bytesToHexString(byte[] src) {
            StringBuilder stringBuilder = new StringBuilder("");
            if (src == null || src.length <= 0) {
                return null;
            }
            for (byte b : src) {
                String hv = Integer.toHexString(b & 255);
                if (hv.length() < 2) {
                    stringBuilder.append(0);
                }
                stringBuilder.append(hv);
            }
            return stringBuilder.toString();
        }
    }
}
