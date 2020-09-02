package com.oppo.internal.telephony.cdma;

import com.android.internal.telephony.cdma.CdmaInboundSmsHandler;
import com.oppo.internal.telephony.OppoInboundSmsHandlerReference;

public class OppoCdmaInboundSmsHandlerReference extends OppoInboundSmsHandlerReference {
    public OppoCdmaInboundSmsHandlerReference(CdmaInboundSmsHandler ref) {
        super(ref);
    }
}
