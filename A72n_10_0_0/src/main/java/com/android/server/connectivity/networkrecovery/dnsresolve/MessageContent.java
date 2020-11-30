package com.android.server.connectivity.networkrecovery.dnsresolve;

import com.android.server.connectivity.networkrecovery.dnsresolve.MessageContent;
import java.io.IOException;
import java.nio.ByteBuffer;

public interface MessageContent<T extends MessageContent<T>> {
    T fromBytes(ByteBuffer byteBuffer) throws IOException;

    T toBytes(ByteBuffer byteBuffer) throws IOException;
}
