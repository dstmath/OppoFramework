package com.android.internal.telephony.gsm;

import android.content.Context;
import com.android.internal.telephony.CellBroadcastHandler;
import com.android.internal.telephony.InboundSmsHandler;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.SmsStorageMonitor;

public abstract class AbstractGsmInboundSmsHandler extends InboundSmsHandler {
    protected AbstractGsmInboundSmsHandler(String name, Context context, SmsStorageMonitor storageMonitor, Phone phone, CellBroadcastHandler cellBroadcastHandler) {
        super(name, context, storageMonitor, phone, cellBroadcastHandler);
    }
}
