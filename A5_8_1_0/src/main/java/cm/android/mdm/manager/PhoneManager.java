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
        if (this.mContext != null) {
            this.mColorOSTelephonyManager = ColorOSTelephonyManager.getDefault(this.mContext);
        } else {
            Log.w(TAG, "mContext is null!");
        }
    }

    public void endCall() {
        if (this.mColorOSTelephonyManager == null) {
            Log.w(TAG, "mColorOSTelephonyManager is null, return!");
            return;
        }
        if (this.mColorOSTelephonyManager.getCallStateGemini(SLOT_ID0) != 0) {
            this.mColorOSTelephonyManager.endCallGemini(SLOT_ID0);
        } else if (this.mColorOSTelephonyManager.getCallStateGemini(SLOT_ID1) != 0) {
            this.mColorOSTelephonyManager.endCallGemini(SLOT_ID1);
        }
    }

    public List<String> getSupportMethods() {
        return MethodSignature.getMethodSignatures(PhoneManager.class);
    }
}
