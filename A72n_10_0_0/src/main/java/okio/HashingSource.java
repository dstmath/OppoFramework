package okio;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class HashingSource extends ForwardingSource {
    private final MessageDigest messageDigest;

    public static HashingSource md5(Source source) {
        return new HashingSource(source, "MD5");
    }

    public static HashingSource sha1(Source source) {
        return new HashingSource(source, "SHA-1");
    }

    public static HashingSource sha256(Source source) {
        return new HashingSource(source, "SHA-256");
    }

    private HashingSource(Source source, String algorithm) {
        super(source);
        try {
            this.messageDigest = MessageDigest.getInstance(algorithm);
        } catch (NoSuchAlgorithmException e) {
            throw new AssertionError();
        }
    }

    @Override // okio.Source, okio.ForwardingSource
    public long read(Buffer sink, long byteCount) throws IOException {
        long result = super.read(sink, byteCount);
        if (result != -1) {
            long start = sink.size - result;
            long offset = sink.size;
            Segment s = sink.head;
            while (offset > start) {
                s = s.prev;
                offset -= (long) (s.limit - s.pos);
            }
            while (offset < sink.size) {
                int pos = (int) ((((long) s.pos) + start) - offset);
                this.messageDigest.update(s.data, pos, s.limit - pos);
                offset += (long) (s.limit - s.pos);
                start = offset;
                s = s.next;
            }
        }
        return result;
    }

    public ByteString hash() {
        return ByteString.of(this.messageDigest.digest());
    }
}
