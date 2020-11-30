package com.oppo.enterprise.mdmcoreservice.service;

import android.app.ActivityManager;
import android.content.Context;
import android.os.IBinder;
import android.util.Log;
import com.oppo.enterprise.mdmcoreservice.aidl.IOppoMdmService;
import com.oppo.enterprise.mdmcoreservice.certificate.OppoCertificateVerifier;
import com.oppo.enterprise.mdmcoreservice.utils.ConstantUtil;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public class OppoMdmService extends IOppoMdmService.Stub {
    private static String TAG = "OppoMdmService";
    private ActivityManager mAM = ((ActivityManager) this.mContext.getSystemService("activity"));
    private Context mContext;

    public OppoMdmService(Context context) {
        this.mContext = context;
        OppoMdmManagerFactory.getInstance().initManager(this.mContext);
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IOppoMdmService
    public IBinder getManager(String strManagerName) {
        return OppoMdmManagerFactory.getInstance().getManager(strManagerName);
    }

    @Override // com.oppo.enterprise.mdmcoreservice.aidl.IOppoMdmService
    public boolean isPackageContainsOppoCertificates(String packageName) {
        OppoCertificateVerifier mOppoCertificateVerifier = PermissionManager.getInstance().getOppoCertificateVerifier();
        if (mOppoCertificateVerifier != null) {
            return mOppoCertificateVerifier.isPackageContainsOppoCertificates(packageName);
        }
        Log.d(TAG, "mOppoCertificateVerifier is null return false");
        return false;
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        int opti = 0;
        while (opti < args.length) {
            String cmd = opti < args.length ? args[opti] : "";
            opti++;
            if ("version".equals(cmd)) {
                pw.println(ConstantUtil.OPPO_CUSTOM_API_VERSION);
            }
        }
    }
}
