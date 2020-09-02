package cm.android.mdm.manager;

import android.content.Context;
import android.telephony.ColorOSTelephonyManager;
import android.util.Log;
import cm.android.mdm.interfaces.IPhoneManager;
import cm.android.mdm.util.MethodSignature;
import java.util.List;

public class PhoneManager implements IPhoneManager {
    private static final int SLOT_ID0 = 0;
    private static final int SLOT_ID1 = 1;
    private static final String TAG = "PhoneManager";
    private ColorOSTelephonyManager mColorOSTelephonyManager = null;
    private Context mContext;

    public PhoneManager(Context context) {
        this.mContext = context;
        Context context2 = this.mContext;
        if (context2 != null) {
            this.mColorOSTelephonyManager = ColorOSTelephonyManager.getDefault(context2);
        } else {
            Log.w(TAG, "mContext is null!");
        }
    }

    @Override // cm.android.mdm.interfaces.IPhoneManager
    public void endCall() {
        ColorOSTelephonyManager colorOSTelephonyManager = this.mColorOSTelephonyManager;
        if (colorOSTelephonyManager == null) {
            Log.w(TAG, "mColorOSTelephonyManager is null, return!");
        } else if (colorOSTelephonyManager.getCallStateGemini((int) SLOT_ID0) != 0) {
            this.mColorOSTelephonyManager.endCallGemini((int) SLOT_ID0);
        } else if (this.mColorOSTelephonyManager.getCallStateGemini((int) SLOT_ID1) != 0) {
            this.mColorOSTelephonyManager.endCallGemini((int) SLOT_ID1);
        }
    }

    @Override // cm.android.mdm.interfaces.IPhoneManager
    public List<String> getSupportMethods() {
        return MethodSignature.getMethodSignatures(PhoneManager.class);
    }
}
