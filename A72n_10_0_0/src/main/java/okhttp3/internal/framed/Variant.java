package okhttp3.internal.framed;

import okhttp3.Protocol;
import okio.BufferedSink;
import okio.BufferedSource;

public interface Variant {
    Protocol getProtocol();

    FrameReader newReader(BufferedSource bufferedSource, boolean z);

    FrameWriter newWriter(BufferedSink bufferedSink, boolean z);
}
