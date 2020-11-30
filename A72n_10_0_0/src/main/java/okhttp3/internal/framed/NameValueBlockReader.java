package okhttp3.internal.framed;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;
import okio.Buffer;
import okio.BufferedSource;
import okio.ByteString;
import okio.ForwardingSource;
import okio.InflaterSource;
import okio.Okio;

/* access modifiers changed from: package-private */
public class NameValueBlockReader {
    private int compressedLimit;
    private final InflaterSource inflaterSource;
    private final BufferedSource source = Okio.buffer(this.inflaterSource);

    public NameValueBlockReader(BufferedSource source2) {
        this.inflaterSource = new InflaterSource(new ForwardingSource(source2) {
            /* class okhttp3.internal.framed.NameValueBlockReader.AnonymousClass1 */

            @Override // okio.Source, okio.ForwardingSource
            public long read(Buffer sink, long byteCount) throws IOException {
                if (NameValueBlockReader.this.compressedLimit == 0) {
                    return -1;
                }
                long read = super.read(sink, Math.min(byteCount, (long) NameValueBlockReader.this.compressedLimit));
                if (read == -1) {
                    return -1;
                }
                NameValueBlockReader.this.compressedLimit = (int) (((long) NameValueBlockReader.this.compressedLimit) - read);
                return read;
            }
        }, new Inflater() {
            /* class okhttp3.internal.framed.NameValueBlockReader.AnonymousClass2 */

            @Override // java.util.zip.Inflater
            public int inflate(byte[] buffer, int offset, int count) throws DataFormatException {
                int result = super.inflate(buffer, offset, count);
                if (result != 0 || !needsDictionary()) {
                    return result;
                }
                setDictionary(Spdy3.DICTIONARY);
                return super.inflate(buffer, offset, count);
            }
        });
    }

    public List<Header> readNameValueBlock(int length) throws IOException {
        this.compressedLimit += length;
        int numberOfPairs = this.source.readInt();
        if (numberOfPairs < 0) {
            throw new IOException("numberOfPairs < 0: " + numberOfPairs);
        } else if (numberOfPairs <= 1024) {
            List<Header> entries = new ArrayList<>(numberOfPairs);
            for (int i = 0; i < numberOfPairs; i++) {
                ByteString name = readByteString().toAsciiLowercase();
                ByteString values = readByteString();
                if (name.size() != 0) {
                    entries.add(new Header(name, values));
                } else {
                    throw new IOException("name.size == 0");
                }
            }
            doneReading();
            return entries;
        } else {
            throw new IOException("numberOfPairs > 1024: " + numberOfPairs);
        }
    }

    private ByteString readByteString() throws IOException {
        return this.source.readByteString((long) this.source.readInt());
    }

    private void doneReading() throws IOException {
        if (this.compressedLimit > 0) {
            this.inflaterSource.refill();
            if (this.compressedLimit != 0) {
                throw new IOException("compressedLimit > 0: " + this.compressedLimit);
            }
        }
    }

    public void close() throws IOException {
        this.source.close();
    }
}
