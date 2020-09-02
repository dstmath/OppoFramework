package com.android.internal.telephony.cdma;

import android.content.Context;
import com.android.internal.telephony.CellBroadcastHandler;
import com.android.internal.telephony.InboundSmsHandler;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.SmsStorageMonitor;

public abstract class AbstractCdmaInboundSmsHandler extends InboundSmsHandler {
    protected AbstractCdmaInboundSmsHandler(String name, Context context, SmsStorageMonitor storageMonitor, Phone phone, CellBroadcastHandler cellBroadcastHandler) {
        super(name, context, storageMonitor, phone, cellBroadcastHandler);
    }
}
