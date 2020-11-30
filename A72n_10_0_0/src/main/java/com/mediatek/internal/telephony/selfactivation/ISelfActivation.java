package com.mediatek.internal.telephony.selfactivation;

import android.content.Context;
import android.os.Bundle;
import com.android.internal.telephony.CommandsInterface;

public interface ISelfActivation {
    public static final int ACTION_ADD_DATA_SERVICE = 0;
    public static final int ACTION_MO_CALL = 1;
    public static final int ACTION_RESET_520_STATE = 2;
    public static final int ADD_DATA_AGREE = 1;
    public static final int ADD_DATA_DECLINE = 0;
    public static final int CALL_TYPE_EMERGENCY = 1;
    public static final int CALL_TYPE_NORMAL = 0;
    public static final String EXTRA_KEY_ADD_DATA_OP = "key_add_data_operation";
    public static final String EXTRA_KEY_MO_CALL_TYPE = "key_mo_call_type";
    public static final int STATE_520_ACTIVATED = 1;
    public static final int STATE_520_NONE = 0;
    public static final int STATE_520_UNKNOWN = -1;
    public static final int STATE_ACTIVATED = 1;
    public static final int STATE_NONE = 0;
    public static final int STATE_NOT_ACTIVATED = 2;
    public static final int STATE_UNKNOWN = -1;

    ISelfActivation buildParams();

    int getPCO520State();

    int getSelfActivateState();

    int selfActivationAction(int i, Bundle bundle);

    ISelfActivation setCommandsInterface(CommandsInterface commandsInterface);

    ISelfActivation setContext(Context context);
}
