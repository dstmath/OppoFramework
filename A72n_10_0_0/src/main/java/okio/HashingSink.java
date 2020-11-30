package okio;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class HashingSink extends ForwardingSink {
    private final MessageDigest messageDigest;

    public static HashingSink md5(Sink sink) {
        return new HashingSink(sink, "MD5");
    }

    public static HashingSink sha1(Sink sink) {
        return new HashingSink(sink, "SHA-1");
    }

    public static HashingSink sha256(Sink sink) {
        return new HashingSink(sink, "SHA-256");
    }

    private HashingSink(Sink sink, String algorithm) {
        super(sink);
        try {
            this.messageDigest = MessageDigest.getInstance(algorithm);
        } catch (NoSuchAlgorithmException e) {
            throw new AssertionError();
        }
    }

    @Override // okio.ForwardingSink, okio.Sink
    public void write(Buffer source, long byteCount) throws IOException {
        Util.checkOffsetAndCount(source.size, 0, byteCount);
        long hashedCount = 0;
        Segment s = source.head;
        while (hashedCount < byteCount) {
            int toHash = (int) Math.min(byteCount - hashedCount, (long) (s.limit - s.pos));
            this.messageDigest.update(s.data, s.pos, toHash);
            hashedCount += (long) toHash;
            s = s.next;
        }
        super.write(source, byteCount);
    }

    public ByteString hash() {
        return ByteString.of(this.messageDigest.digest());
    }
}
