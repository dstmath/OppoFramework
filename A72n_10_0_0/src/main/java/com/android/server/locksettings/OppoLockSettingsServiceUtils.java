package com.android.server.locksettings;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.RemoteException;
import android.provider.Settings;
import android.util.Slog;
import com.android.internal.util.HexDump;
import com.android.internal.widget.ICheckCredentialProgressCallback;
import com.android.internal.widget.VerifyCredentialResponse;
import com.android.server.locksettings.SyntheticPasswordManager;
import com.color.util.ColorTypeCastingHelper;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;

public class OppoLockSettingsServiceUtils {
    private static final String TAG = "OppoLockSettingsServiceUtils";
    private Context mContext;
    private LockSettingsService mLockSettingsService = null;
    private SyntheticPasswordManager mSpManager = null;
    private Handler myHandler = new Handler();

    public OppoLockSettingsServiceUtils(SyntheticPasswordManager mSpManager2, LockSettingsService mLockSettingsService2, Context context) {
        this.mSpManager = mSpManager2;
        this.mLockSettingsService = mLockSettingsService2;
        this.mContext = context;
    }

    public class WriteSecretToTeeRunnable implements Runnable {
        private byte[] credential;
        private int credentialType;
        private byte[] mAuthToken;
        private int userId;

        public WriteSecretToTeeRunnable(byte[] credential2, int userId2, byte[] authToken, int credentialType2) {
            this.credential = credential2;
            this.userId = userId2;
            this.mAuthToken = authToken;
            this.credentialType = credentialType2;
        }

        public void run() {
            try {
                OppoLockSettingsServiceUtils.this.execSync();
                if (this.credential == null || this.credential.length < 0) {
                    Slog.d(OppoLockSettingsServiceUtils.TAG, "credential is null and  clear tee store");
                    byte[] nullbyte = new byte[128];
                    if (this.mAuthToken == null) {
                        Slog.d(OppoLockSettingsServiceUtils.TAG, "WriteSecretToTeeRunnable mAuthToken == null");
                    } else if (OppoLockSettingsServiceUtils.this.writeSecretCmd(nullbyte, nullbyte.length, this.mAuthToken, this.mAuthToken.length) != 0) {
                        Slog.d(OppoLockSettingsServiceUtils.TAG, "write Secret clear tee Secret failed");
                    } else {
                        Slog.d(OppoLockSettingsServiceUtils.TAG, "write Secret clear tee Secret success");
                    }
                } else {
                    byte[] mSecret = OppoLockSettingsServiceUtils.this.getSecret(this.credential, this.userId);
                    if (mSecret != null && mSecret.length > 0) {
                        String mSecretString = HexDump.toHexString(mSecret);
                        Slog.d(OppoLockSettingsServiceUtils.TAG, "write Secret to tee by writeSecretCmd");
                        byte[] mSecretArray = mSecretString.getBytes();
                        if (this.mAuthToken == null) {
                            byte[] mToken = OppoLockSettingsServiceUtils.this.getResponsePayload(this.credential, this.credentialType, 0, this.userId);
                            if (mToken == null) {
                                Slog.d(OppoLockSettingsServiceUtils.TAG, "WriteSecretToTeeRunnable mAuthToken == null");
                                return;
                            }
                            this.mAuthToken = mToken;
                        }
                        if (OppoLockSettingsServiceUtils.this.writeSecretCmd(mSecretArray, mSecretArray.length, this.mAuthToken, this.mAuthToken.length) != 0) {
                            Slog.d(OppoLockSettingsServiceUtils.TAG, "write Secret in tee fail");
                        } else {
                            Slog.d(OppoLockSettingsServiceUtils.TAG, "write Secret to tee success");
                        }
                    }
                }
            } catch (RemoteException e) {
                Slog.d(OppoLockSettingsServiceUtils.TAG, "WriteSecretToTeeRunnable throw RemoteException");
            }
        }
    }

    /* access modifiers changed from: protected */
    public int writeSecretCmd(byte[] mSecret, int length, byte[] authToken, int tokenLenght) {
        ArrayList<Byte> out_buf;
        Slog.d(TAG, "writeSecretCmd  length:" + length + " tokenLenght:" + tokenLenght);
        ArrayList<Byte> crypto = new ArrayList<>();
        crypto.add((byte) 8);
        crypto.add((byte) 0);
        crypto.add((byte) 0);
        crypto.add((byte) 0);
        crypto.add(Byte.valueOf((byte) (length & 255)));
        crypto.add(Byte.valueOf((byte) ((length >> 8) & 255)));
        crypto.add(Byte.valueOf((byte) ((length >> 16) & 255)));
        crypto.add(Byte.valueOf((byte) ((length >> 24) & 255)));
        for (byte ele : mSecret) {
            crypto.add(Byte.valueOf(ele));
        }
        crypto.add(Byte.valueOf((byte) (tokenLenght & 255)));
        crypto.add(Byte.valueOf((byte) ((tokenLenght >> 8) & 255)));
        crypto.add(Byte.valueOf((byte) ((tokenLenght >> 16) & 255)));
        crypto.add(Byte.valueOf((byte) ((tokenLenght >> 24) & 255)));
        for (byte ele2 : authToken) {
            crypto.add(Byte.valueOf(ele2));
        }
        if (getCryptoSerice() != null && (out_buf = cryptoengInvokeCommand(crypto)) != null && out_buf.size() >= 4 && out_buf.get(0).byteValue() == 0 && out_buf.get(1).byteValue() == 0 && out_buf.get(2).byteValue() == 0 && out_buf.get(3).byteValue() == 0) {
            return 0;
        }
        return -1;
    }

    /* access modifiers changed from: protected */
    public Object getCryptoSerice() {
        try {
            Class<?> c = Class.forName("vendor.oppo.hardware.cryptoeng.V1_0.ICryptoeng");
            return c.getMethod("getService", new Class[0]).invoke(c, new Object[0]);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /* access modifiers changed from: protected */
    public ArrayList<Byte> cryptoengInvokeCommand(ArrayList<Byte> crypto) {
        try {
            Method get = Class.forName("vendor.oppo.hardware.cryptoeng.V1_0.ICryptoeng").getMethod("cryptoeng_invoke_command", ArrayList.class);
            Object cryptoSerice = getCryptoSerice();
            if (cryptoSerice == null) {
                return null;
            }
            return (ArrayList) get.invoke(cryptoSerice, crypto);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public byte[] getResponsePayload(byte[] credential, int type, long challenge, int userId) {
        try {
            VerifyCredentialResponse mResponse = getResponse(credential, type, true, challenge, userId, null);
            if (mResponse != null) {
                return mResponse.getPayload();
            }
            Slog.d(TAG, "getResponsePayload get token failed");
            return null;
        } catch (Exception e) {
            Slog.d(TAG, "getResponsePayload get token failed:" + e.getMessage());
            return null;
        }
    }

    public void writeSecretToTee(VerifyCredentialResponse response, byte[] credential, int credentialType, int userId) {
        if (this.mLockSettingsService == null || this.mSpManager == null) {
            Slog.d(TAG, "init failed");
        } else if (response == null || response.getResponseCode() != 0) {
            this.myHandler.post(new WriteSecretToTeeRunnable(credential, userId, null, credentialType));
        } else {
            Slog.d(TAG, "spBasedSetLockCredentialInternalLocked  getResponse ok");
            this.myHandler.post(new WriteSecretToTeeRunnable(credential, userId, response.getPayload(), credentialType));
        }
    }

    public static boolean isMemoryLow() {
        return Environment.getDataDirectory().getUsableSpace() < 10485760;
    }

    public VerifyCredentialResponse getResponse(byte[] userCredential, int credentialType, boolean hasChallenge, long challenge, int userId, ICheckCredentialProgressCallback progressCallback) throws RemoteException {
        byte[] userCredential2;
        if (userCredential != null) {
            if (userCredential.length != 0) {
                if (userId != -9999 || Settings.Global.getInt(this.mContext.getContentResolver(), "device_provisioned", 0) == 0) {
                    if (credentialType == -1) {
                        userCredential2 = null;
                    } else {
                        userCredential2 = userCredential;
                    }
                    synchronized (this.mSpManager) {
                        try {
                            if (!isSyntheticPasswordBasedCredential(userId)) {
                                return null;
                            }
                            if (userId == -9999) {
                                return this.mSpManager.verifyFrpCredential(this.mLockSettingsService.getGateKeeperService(), userCredential2, credentialType, progressCallback);
                            }
                            SyntheticPasswordManager.AuthenticationResult authResult = this.mSpManager.unwrapPasswordBasedSyntheticPassword(this.mLockSettingsService.getGateKeeperService(), getSyntheticPasswordHandle(userId), userCredential2, userId, progressCallback);
                            if (authResult.credentialType != credentialType) {
                                Slog.e(TAG, "getResponse Credential type mismatch.");
                                return null;
                            }
                            return authResult.gkResponse;
                        } catch (Throwable th) {
                            th = th;
                            throw th;
                        }
                    }
                } else {
                    Slog.e(TAG, "getResponse FRP userCredential can only be verified prior to provisioning.");
                    return null;
                }
            }
        }
        Slog.d(TAG, "getResponse userCredential can't be null or empty");
        return null;
    }

    public byte[] getSecret(byte[] userCredential, int userId) throws RemoteException {
        if (userId == -9999) {
            Slog.d(TAG, "userId == USER_FRP");
            return null;
        }
        synchronized (this.mSpManager) {
            SyntheticPasswordManager.AuthenticationResult authResult = this.mSpManager.unwrapPasswordBasedSyntheticPassword(this.mLockSettingsService.getGateKeeperService(), getSyntheticPasswordHandle(userId), userCredential, userId, null);
            if (authResult == null) {
                Slog.d(TAG, "authResult is null");
                return null;
            }
            VerifyCredentialResponse response = authResult.gkResponse;
            if (response == null || response.getResponseCode() != 0) {
                Slog.d(TAG, "response.getResponseCode() != RESPONSE_OK");
                return null;
            }
            return authResult.authToken.deriveDiskEncryptionKey();
        }
    }

    static boolean isBootFromOTA() {
        File file = new File("/cache/recovery/intent");
        boolean result = false;
        String resultStr = "";
        if (file.exists() && file.canRead()) {
            Slog.i(TAG, "/cache/recovery/intent file is exist!!!");
            BufferedReader reader = null;
            try {
                BufferedReader reader2 = new BufferedReader(new FileReader(file));
                resultStr = reader2.readLine();
                result = "0".equals(resultStr) || "2".equals(resultStr);
                try {
                    reader2.close();
                } catch (IOException e1) {
                    Slog.e(TAG, "readOTAUpdateResult close the reader failed!!!", e1);
                }
            } catch (IOException e) {
                Slog.e(TAG, "readOTAUpdateResult failed!!!", e);
                if (0 != 0) {
                    reader.close();
                }
            } catch (Throwable th) {
                if (0 != 0) {
                    try {
                        reader.close();
                    } catch (IOException e12) {
                        Slog.e(TAG, "readOTAUpdateResult close the reader failed!!!", e12);
                    }
                }
                throw th;
            }
        }
        Slog.d(TAG, "isBootFromOTA::resultStr = " + resultStr + ", result = " + result);
        return result;
    }

    private long getSyntheticPasswordHandle(int userId) {
        LockSettingsService lockSettingsService = this.mLockSettingsService;
        if (lockSettingsService == null) {
            Slog.w(TAG, "getSyntheticPasswordHandle, mLockSettingsService null!!");
            return 0;
        }
        OppoBaseLockSettingsService oppoBaseLockSettingsService = (OppoBaseLockSettingsService) ColorTypeCastingHelper.typeCasting(OppoBaseLockSettingsService.class, lockSettingsService);
        if (oppoBaseLockSettingsService == null) {
            Slog.w(TAG, "getSyntheticPasswordHandle, type case failed!!");
            return 0;
        }
        IOppoLockSettingsInner lockSettingsInner = oppoBaseLockSettingsService.getOppoLockSettingsInner();
        if (lockSettingsInner != null) {
            return lockSettingsInner.getSyntheticPasswordHandle(userId);
        }
        Slog.w(TAG, "getSyntheticPasswordHandle, lockSettingsInner null!!");
        return 0;
    }

    private boolean isSyntheticPasswordBasedCredential(int userId) {
        LockSettingsService lockSettingsService = this.mLockSettingsService;
        if (lockSettingsService == null) {
            Slog.w(TAG, "isSyntheticPasswordBasedCredential, mLockSettingsService null!!");
            return false;
        }
        OppoBaseLockSettingsService oppoBaseLockSettingsService = (OppoBaseLockSettingsService) ColorTypeCastingHelper.typeCasting(OppoBaseLockSettingsService.class, lockSettingsService);
        if (oppoBaseLockSettingsService == null) {
            Slog.w(TAG, "isSyntheticPasswordBasedCredential, type case failed!!");
            return false;
        }
        IOppoLockSettingsInner lockSettingsInner = oppoBaseLockSettingsService.getOppoLockSettingsInner();
        if (lockSettingsInner != null) {
            return lockSettingsInner.isSyntheticPasswordBasedCredential(userId);
        }
        Slog.w(TAG, "isSyntheticPasswordBasedCredential, lockSettingsInner null!!");
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void execSync() {
        try {
            Slog.d(TAG, "locksetting execSync thread start");
            Runtime.getRuntime().exec("sync");
            Slog.d(TAG, "locksetting execSync thread end");
        } catch (Exception e) {
            Slog.e(TAG, "Error in execSync : " + e);
        }
    }
}
