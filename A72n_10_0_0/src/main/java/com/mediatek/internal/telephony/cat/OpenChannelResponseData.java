package com.mediatek.internal.telephony.cat;

import com.android.internal.telephony.cat.ComprehensionTlvTag;
import com.android.internal.telephony.cat.ResponseData;
import java.io.ByteArrayOutputStream;

/* access modifiers changed from: package-private */
/* compiled from: BipResponseData */
public class OpenChannelResponseData extends ResponseData {
    BearerDesc mBearerDesc = null;
    int mBufferSize = 0;
    ChannelStatus mChannelStatus = null;

    OpenChannelResponseData(ChannelStatus channelStatus, BearerDesc bearerDesc, int bufferSize) {
        if (channelStatus != null) {
            MtkCatLog.d("[BIP]", "OpenChannelResponseData-constructor: channelStatus cid/status : " + channelStatus.mChannelId + "/" + channelStatus.mChannelStatus);
        } else {
            MtkCatLog.d("[BIP]", "OpenChannelResponseData-constructor: channelStatus is null");
        }
        if (bearerDesc != null) {
            MtkCatLog.d("[BIP]", "OpenChannelResponseData-constructor: bearerDesc bearerType " + bearerDesc.bearerType);
        } else {
            MtkCatLog.d("[BIP]", "OpenChannelResponseData-constructor: bearerDesc is null");
        }
        MtkCatLog.d("[BIP]", "OpenChannelResponseData-constructor: buffer size is " + bufferSize);
        this.mChannelStatus = channelStatus;
        this.mBearerDesc = bearerDesc;
        this.mBufferSize = bufferSize;
    }

    public void format(ByteArrayOutputStream buf) {
        if (buf == null) {
            MtkCatLog.d("[BIP]", "OpenChannelResponseData-format: buf is null");
            return;
        }
        BearerDesc bearerDesc = this.mBearerDesc;
        if (bearerDesc == null) {
            MtkCatLog.e("[BIP]", "OpenChannelResponseData-format: mBearerDesc is null");
        } else if (((GPRSBearerDesc) bearerDesc).bearerType != 2) {
            MtkCatLog.d("[BIP]", "OpenChannelResponseData-format: bearer type is not gprs");
        } else if (this.mBufferSize > 0) {
            if (this.mChannelStatus != null) {
                MtkCatLog.d("[BIP]", "OpenChannelResponseData-format: Write channel status into TR");
                int tag = ComprehensionTlvTag.CHANNEL_STATUS.value();
                MtkCatLog.d("[BIP]", "OpenChannelResponseData-format: tag: " + tag);
                buf.write(tag);
                MtkCatLog.d("[BIP]", "OpenChannelResponseData-format: length: 2");
                buf.write(2);
                StringBuilder sb = new StringBuilder();
                sb.append("OpenChannelResponseData-format: channel id & isActivated: ");
                int i = 128;
                sb.append(this.mChannelStatus.mChannelId | (this.mChannelStatus.isActivated ? 128 : 0));
                MtkCatLog.d("[BIP]", sb.toString());
                int i2 = this.mChannelStatus.mChannelId;
                if (!this.mChannelStatus.isActivated) {
                    i = 0;
                }
                buf.write(i2 | i);
                MtkCatLog.d("[BIP]", "OpenChannelResponseData-format: channel status: " + this.mChannelStatus.mChannelStatus);
                buf.write(this.mChannelStatus.mChannelStatus);
            }
            MtkCatLog.d("[BIP]", "Write bearer description into TR");
            int tag2 = ComprehensionTlvTag.BEARER_DESCRIPTION.value();
            MtkCatLog.d("[BIP]", "OpenChannelResponseData-format: tag: " + tag2);
            buf.write(tag2);
            MtkCatLog.d("[BIP]", "OpenChannelResponseData-format: length: 7");
            buf.write(7);
            MtkCatLog.d("[BIP]", "OpenChannelResponseData-format: bearer type: " + ((GPRSBearerDesc) this.mBearerDesc).bearerType);
            buf.write(((GPRSBearerDesc) this.mBearerDesc).bearerType);
            MtkCatLog.d("[BIP]", "OpenChannelResponseData-format: precedence: " + ((GPRSBearerDesc) this.mBearerDesc).precedence);
            buf.write(((GPRSBearerDesc) this.mBearerDesc).precedence);
            MtkCatLog.d("[BIP]", "OpenChannelResponseData-format: delay: " + ((GPRSBearerDesc) this.mBearerDesc).delay);
            buf.write(((GPRSBearerDesc) this.mBearerDesc).delay);
            MtkCatLog.d("[BIP]", "OpenChannelResponseData-format: reliability: " + ((GPRSBearerDesc) this.mBearerDesc).reliability);
            buf.write(((GPRSBearerDesc) this.mBearerDesc).reliability);
            MtkCatLog.d("[BIP]", "OpenChannelResponseData-format: peak: " + ((GPRSBearerDesc) this.mBearerDesc).peak);
            buf.write(((GPRSBearerDesc) this.mBearerDesc).peak);
            MtkCatLog.d("[BIP]", "OpenChannelResponseData-format: mean: " + ((GPRSBearerDesc) this.mBearerDesc).mean);
            buf.write(((GPRSBearerDesc) this.mBearerDesc).mean);
            MtkCatLog.d("[BIP]", "OpenChannelResponseData-format: pdp type: " + ((GPRSBearerDesc) this.mBearerDesc).pdpType);
            buf.write(((GPRSBearerDesc) this.mBearerDesc).pdpType);
            MtkCatLog.d("[BIP]", "Write buffer size into TR");
            int tag3 = ComprehensionTlvTag.BUFFER_SIZE.value();
            MtkCatLog.d("[BIP]", "OpenChannelResponseData-format: tag: " + tag3);
            buf.write(tag3);
            MtkCatLog.d("[BIP]", "OpenChannelResponseData-format: length: 2");
            buf.write(2);
            MtkCatLog.d("[BIP]", "OpenChannelResponseData-format: length(hi-byte): " + (this.mBufferSize >> 8));
            buf.write(this.mBufferSize >> 8);
            MtkCatLog.d("[BIP]", "OpenChannelResponseData-format: length(low-byte): " + (this.mBufferSize & 255));
            buf.write(this.mBufferSize & 255);
        } else {
            MtkCatLog.d("[BIP]", "Miss ChannelStatus, BearerDesc or BufferSize");
        }
    }
}
