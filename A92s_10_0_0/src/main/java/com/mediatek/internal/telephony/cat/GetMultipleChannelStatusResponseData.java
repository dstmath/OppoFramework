package com.mediatek.internal.telephony.cat;

import com.android.internal.telephony.cat.ComprehensionTlvTag;
import com.android.internal.telephony.cat.ResponseData;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Iterator;

/* compiled from: BipResponseData */
class GetMultipleChannelStatusResponseData extends ResponseData {
    ArrayList mArrList = null;

    GetMultipleChannelStatusResponseData(ArrayList arrList) {
        this.mArrList = arrList;
    }

    public void format(ByteArrayOutputStream buf) {
        if (buf != null) {
            int tag = ComprehensionTlvTag.CHANNEL_STATUS.value() | 128;
            MtkCatLog.d("[BIP]", "ChannelStatusResp: size: " + this.mArrList.size());
            if (this.mArrList.size() > 0) {
                Iterator iterator = this.mArrList.iterator();
                while (iterator.hasNext()) {
                    buf.write(tag);
                    buf.write(2);
                    ChannelStatus chStatus = (ChannelStatus) iterator.next();
                    buf.write((chStatus.mChannelId & 7) | chStatus.mChannelStatus);
                    buf.write(chStatus.mChannelStatusInfo);
                    MtkCatLog.d("[BIP]", "ChannelStatusResp: cid:" + chStatus.mChannelId + ",status:" + chStatus.mChannelStatus + ",info:" + chStatus.mChannelStatusInfo);
                }
                return;
            }
            MtkCatLog.d("[BIP]", "ChannelStatusResp: no channel status.");
            buf.write(tag);
            buf.write(2);
            buf.write(0);
            buf.write(0);
        }
    }
}
