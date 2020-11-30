package okhttp3;

import java.io.UnsupportedEncodingException;
import okio.ByteString;
import org.apache.commons.codec.CharEncoding;

public final class Credentials {
    private Credentials() {
    }

    public static String basic(String userName, String password) {
        try {
            String encoded = ByteString.of((userName + ":" + password).getBytes(CharEncoding.ISO_8859_1)).base64();
            return "Basic " + encoded;
        } catch (UnsupportedEncodingException e) {
            throw new AssertionError();
        }
    }
}
